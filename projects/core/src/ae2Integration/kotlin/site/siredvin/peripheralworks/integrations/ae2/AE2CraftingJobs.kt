package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.CalculationStrategy
import appeng.api.networking.crafting.ICraftingLink
import appeng.api.networking.crafting.ICraftingPlan
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.crafting.ICraftingSimulationRequester
import appeng.api.networking.security.IActionSource
import appeng.me.cluster.implementations.CraftingCPUCluster
import appeng.me.helpers.IGridConnectedBlockEntity
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.AE2Configuration
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.buildKey
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.keyCounterToLua
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.stackToMap
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.owner.BasePeripheralOwner
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.Locale
import java.util.Optional
import java.util.WeakHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

object AE2CraftingJobs {
    private data class Job(val service: WeakReference<ICraftingService>, val target: Map<String, Any>, val amount: Long, val cpu: WeakReference<CraftingCPUCluster>?)

    // ponytail: job counts are tiny; scan weak keys until UUID lookup is proven necessary.
    private val jobs = Collections.synchronizedMap(WeakHashMap<ICraftingLink, Job>())

    fun submit(
        service: ICraftingService,
        source: IActionSource,
        plan: ICraftingPlan,
        publicAmount: Long,
        targetCPU: Optional<String>,
    ): MethodResult {
        if (!plan.missingItems().isEmpty) return MethodResult.of(false, "Missing items", keyCounterToLua(plan.missingItems()))
        val cpu = if (targetCPU.isPresent) {
            service.cpus.firstOrNull { it.name?.string == targetCPU.get() }
                ?: return MethodResult.of(null, "Cannot find target CPU")
        } else {
            null
        }
        val standaloneCPUs = service.cpus.filterIsInstance<CraftingCPUCluster>()
        val previousLinks = standaloneCPUs.mapNotNull { it.craftingLogic.lastLink }.toSet()
        val submitted = service.submitJob(plan, null, cpu, false, source)
        if (!submitted.successful()) {
            return MethodResult.of(null, "Cannot submit crafting job: ${submitted.errorCode()?.name?.lowercase(Locale.ROOT) ?: "unknown error"}")
        }
        // Standalone jobs return their output to network storage and keep the link on the CPU.
        val link = submitted.link() ?: standaloneCPUs.mapNotNull { it.craftingLogic.lastLink }.firstOrNull { it !in previousLinks }
            ?: return MethodResult.of(null, "AE2 did not return a crafting job")
        val jobID = link.craftingID.toString()
        val submittedCPU = standaloneCPUs.firstOrNull { it.craftingLogic.lastLink === link }
        jobs[link] = Job(WeakReference(service), stackToMap(plan.finalOutput()), publicAmount, submittedCPU?.let { WeakReference(it) })
        return MethodResult.of(true, jobID)
    }

    fun get(service: ICraftingService, jobID: String): MethodResult {
        val job = find(service, jobID) ?: return missing(jobID)
        return MethodResult.of(toMap(job.first, job.second))
    }

    fun getAll(service: ICraftingService): List<Map<String, Any>> = synchronized(jobs) {
        jobs.mapNotNull { (link, job) -> if (job.service.get() === service) toMap(link, job) else null }
    }

    fun cancel(service: ICraftingService, jobID: String): MethodResult {
        val (link, job) = find(service, jobID) ?: return missing(jobID)
        when (state(link, job)) {
            "canceled" -> return MethodResult.of(false, "Crafting job '$jobID' is already canceled")
            "done" -> return MethodResult.of(false, "Crafting job '$jobID' is already done")
        }
        link.cancel()
        return MethodResult.of(true)
    }

    private fun find(service: ICraftingService, jobID: String): Pair<ICraftingLink, Job>? = synchronized(jobs) {
        jobs.entries.firstOrNull { (link, job) -> link.craftingID.toString() == jobID && job.service.get() === service }
            ?.let { it.key to it.value }
    }

    private fun state(link: ICraftingLink, job: Job): String {
        if (link.isCanceled) return "canceled"
        if (link.isDone) return "done"
        job.cpu?.let {
            val cpu = it.get() ?: return "canceled"
            if (cpu.isDestroyed) return "canceled"
            // Standalone links have no requester nexus, so AE2's markDone does not update the link itself.
            if (cpu.craftingLogic.lastLink !== link) return "done"
        }
        return "running"
    }

    private fun toMap(link: ICraftingLink, job: Job): Map<String, Any> = mapOf(
        "id" to link.craftingID.toString(),
        "state" to state(link, job),
        "target" to job.target,
        "amount" to job.amount,
    )

    private fun missing(jobID: String): MethodResult = MethodResult.of(null, "Crafting job '$jobID' was not found")
}

class AE2CraftingJobsPlugin private constructor(
    private val resolve: () -> Context?,
    private val withActionSource: (((IActionSource) -> MethodResult) -> MethodResult),
    private val unavailableMessage: String,
) : IPeripheralPlugin {
    private data class Context(val level: Level, val service: ICraftingService, val node: IGridNode)

    @LuaFunction(mainThread = false)
    fun scheduleCrafting(luaContext: ILuaContext, mode: String, id: String, amount: Optional<Long>, targetCPU: Optional<String>): MethodResult {
        val publicAmount = amount.orElse(if (mode == "item") 1 else 1000)
        if (publicAmount <= 0) return MethodResult.of(null, "Amount must be positive")
        var initialContext: Context? = null
        val calculation = AtomicReference<Future<ICraftingPlan>>()
        val abandoned = AtomicBoolean(false)

        fun poll(): MethodResult {
            var pending = false
            val task = luaContext.executeMainThreadTask {
                try {
                    if (abandoned.get()) return@executeMainThreadTask emptyArray()
                    val context = resolve()
                    if (context == null || (initialContext != null && context != initialContext)) {
                        calculation.get()?.cancel(true)
                        return@executeMainThreadTask unavailable().result
                    }
                    if (calculation.get() == null) {
                        val key = buildKey(mode, id)
                        val realAmount = try {
                            if (mode == "fluid") Math.multiplyExact(publicAmount, PlatformToolkit.get().fluidCompactDivider.toLong()) else publicAmount
                        } catch (_: ArithmeticException) {
                            return@executeMainThreadTask MethodResult.of(null, "Amount is too large").result
                        }
                        val started = withActionSource { source ->
                            val requester = object : ICraftingSimulationRequester {
                                override fun getActionSource(): IActionSource = source

                                // Player-only action sources have no machine node; AE2 still needs a node to discover patterns.
                                override fun getGridNode(): IGridNode = context.node
                            }
                            calculation.set(context.service.beginCraftingCalculation(context.level, requester, key, realAmount, CalculationStrategy.REPORT_MISSING_ITEMS))
                            MethodResult.of()
                        }
                        if (calculation.get() == null) return@executeMainThreadTask started.result
                        initialContext = context
                    }
                    val future = calculation.get()!!
                    if (abandoned.get()) return@executeMainThreadTask emptyArray()
                    // Yield through CC's bounded task queue; never wait on an unfinished calculation on either thread.
                    if (!future.isDone) {
                        pending = true
                        return@executeMainThreadTask emptyArray()
                    }
                    val plan = try {
                        future.get()
                    } catch (error: ExecutionException) {
                        throw LuaException("Cannot calculate crafting job: ${error.cause?.message ?: error.message}")
                    }
                    // Resolve access and create the action source again after the asynchronous calculation.
                    withActionSource { source -> AE2CraftingJobs.submit(context.service, source, plan, publicAmount, targetCPU) }.result
                } catch (error: Exception) {
                    calculation.get()?.cancel(true)
                    throw error
                } finally {
                    if (abandoned.get()) calculation.get()?.cancel(true)
                }
            }
            fun resume(result: MethodResult): MethodResult {
                val callback = result.callback ?: return if (pending) poll() else result
                return MethodResult.yield(result.result) { arguments ->
                    try {
                        resume(callback.resume(arguments))
                    } catch (error: Exception) {
                        abandoned.set(true)
                        calculation.get()?.cancel(true)
                        throw error
                    }
                }
            }
            return resume(task)
        }
        return poll()
    }

    @LuaFunction(mainThread = true)
    fun getCraftingJob(jobID: String): MethodResult {
        val context = resolve() ?: return unavailable()
        return AE2CraftingJobs.get(context.service, jobID)
    }

    @LuaFunction(mainThread = true)
    fun getCraftingJobs(): List<Map<String, Any>> = resolve()?.let { AE2CraftingJobs.getAll(it.service) } ?: emptyList()

    @LuaFunction(mainThread = true)
    fun cancelCrafting(jobID: String): MethodResult {
        val context = resolve() ?: return unavailable()
        return AE2CraftingJobs.cancel(context.service, jobID)
    }

    private fun unavailable(): MethodResult = MethodResult.of(null, unavailableMessage)

    companion object {
        @Suppress("DEPRECATION")
        fun <T> forMachine(level: Level, entity: T): AE2CraftingJobsPlugin where T : BlockEntity, T : IGridConnectedBlockEntity = AE2CraftingJobsPlugin(
            resolve = {
                if (entity.isRemoved || !level.hasChunkAt(entity.blockPos) || level.getBlockEntity(entity.blockPos) !== entity) {
                    null
                } else {
                    entity.mainNode.node?.let { Context(level, it.grid.craftingService, it) }
                }
            },
            withActionSource = { callback -> callback(IActionSource.ofMachine(entity)) },
            unavailableMessage = "AE2 network is not connected",
        )

        fun forWirelessComputer(owner: BasePeripheralOwner) = AE2CraftingJobsPlugin(
            resolve = {
                owner.level?.let { level ->
                    val session = resolveWirelessSession(owner)
                    Context(level, session.craftingService, session.accessPointNode)
                }
            },
            withActionSource = { callback -> owner.withPlayer({ callback(IActionSource.ofPlayer(it.fakePlayer)) }, skipInventory = true) },
            unavailableMessage = "Linked AE2 network is unavailable",
        )
    }
}

object AE2CraftingJobsPluginProvider : PeripheralPluginProvider {
    override val pluginType = "ae2_crafting_jobs"

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!AE2Configuration.enableMEInterface) return null
        val entity = level.getBlockEntity(pos) ?: return null
        if (entity !is IGridConnectedBlockEntity) return null
        return AE2CraftingJobsPlugin.forMachine(level, entity)
    }
}
