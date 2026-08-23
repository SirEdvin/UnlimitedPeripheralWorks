package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.crafting.CalculationStrategy
import appeng.api.networking.crafting.ICraftingLink
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.security.IActionSource
import appeng.blockentity.grid.AENetworkBlockEntity
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
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

object AE2CraftingJobs {
    private data class Job(val service: WeakReference<ICraftingService>, val target: Map<String, Any>, val amount: Long)

    // ponytail: job counts are tiny; scan weak keys until UUID lookup is proven necessary.
    private val jobs = Collections.synchronizedMap(WeakHashMap<ICraftingLink, Job>())

    fun schedule(
        level: Level,
        service: ICraftingService,
        source: IActionSource,
        mode: String,
        id: String,
        amount: Optional<Long>,
        targetCPU: Optional<String>,
    ): MethodResult {
        val publicAmount = amount.orElse(if (mode == "item") 1 else 1000)
        if (publicAmount <= 0) return MethodResult.of(null, "Amount must be positive")
        val key = buildKey(mode, id)
        val realAmount = try {
            if (mode == "fluid") Math.multiplyExact(publicAmount, PlatformToolkit.get().fluidCompactDivider.toLong()) else publicAmount
        } catch (_: ArithmeticException) {
            return MethodResult.of(null, "Amount is too large")
        }
        val plan = service.beginCraftingCalculation(level, { source }, key, realAmount, CalculationStrategy.REPORT_MISSING_ITEMS).get()
        if (!plan.missingItems().isEmpty) return MethodResult.of(false, "Missing items", keyCounterToLua(plan.missingItems()))
        val cpu = if (targetCPU.isPresent) {
            service.cpus.firstOrNull { it.name?.string == targetCPU.get() }
                ?: return MethodResult.of(null, "Cannot find target CPU")
        } else {
            null
        }
        val submitted = service.submitJob(plan, null, cpu, false, source)
        if (!submitted.successful()) {
            return MethodResult.of(null, "Cannot submit crafting job: ${submitted.errorCode()?.name?.lowercase(Locale.ROOT) ?: "unknown error"}")
        }
        val link = submitted.link() ?: return MethodResult.of(null, "AE2 did not return a crafting job")
        val jobID = link.craftingID.toString()
        jobs[link] = Job(WeakReference(service), stackToMap(plan.finalOutput()), publicAmount)
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
        val (link) = find(service, jobID) ?: return missing(jobID)
        if (link.isCanceled) return MethodResult.of(false, "Crafting job '$jobID' is already canceled")
        if (link.isDone) return MethodResult.of(false, "Crafting job '$jobID' is already done")
        link.cancel()
        return MethodResult.of(true)
    }

    private fun find(service: ICraftingService, jobID: String): Pair<ICraftingLink, Job>? = synchronized(jobs) {
        jobs.entries.firstOrNull { (link, job) -> link.craftingID.toString() == jobID && job.service.get() === service }
            ?.let { it.key to it.value }
    }

    private fun toMap(link: ICraftingLink, job: Job): Map<String, Any> = mapOf(
        "id" to link.craftingID.toString(),
        "state" to when {
            link.isCanceled -> "canceled"
            link.isDone -> "done"
            else -> "running"
        },
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
    private data class Context(val level: Level, val service: ICraftingService)

    @LuaFunction(mainThread = false)
    fun scheduleCrafting(mode: String, id: String, amount: Optional<Long>, targetCPU: Optional<String>): MethodResult {
        val context = resolve() ?: return unavailable()
        return withActionSource { source -> AE2CraftingJobs.schedule(context.level, context.service, source, mode, id, amount, targetCPU) }
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
        fun forMachine(level: Level, entity: AENetworkBlockEntity) = AE2CraftingJobsPlugin(
            resolve = { entity.mainNode.grid?.craftingService?.let { Context(level, it) } },
            withActionSource = { callback -> callback(IActionSource.ofMachine(entity)) },
            unavailableMessage = "AE2 network is not connected",
        )

        fun forWirelessComputer(owner: BasePeripheralOwner) = AE2CraftingJobsPlugin(
            resolve = {
                owner.level?.let { level -> Context(level, resolveWirelessSession(owner).craftingService) }
            },
            withActionSource = { callback -> owner.withPlayer({ callback(IActionSource.ofPlayer(it.fakePlayer)) }, skipInventory = true) },
            unavailableMessage = "Linked AE2 network is unavailable",
        )
    }
}

object AE2CraftingJobsPluginProvider : PeripheralPluginProvider {
    override val pluginType = "ae2_crafting_jobs"

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!Configuration.enableMEInterface) return null
        val entity = level.getBlockEntity(pos) as? AENetworkBlockEntity ?: return null
        return AE2CraftingJobsPlugin.forMachine(level, entity)
    }
}
