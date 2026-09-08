package site.siredvin.peripheralworks.testmod

import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.ICraftingLink
import appeng.api.networking.crafting.ICraftingPlan
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.crafting.ICraftingSimulationRequester
import appeng.api.networking.crafting.ICraftingSubmitResult
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import com.google.common.collect.ImmutableSet
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaTask
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.integrations.ae2.AE2CraftingJobsPlugin
import site.siredvin.testiarium.api.TestGroup
import java.lang.reflect.Proxy
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.FutureTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@TestGroup("ae2")
class AE2CraftingJobsGameTests {
    @GameTest(template = "empty")
    fun craftingThreadHandoffsAndRevalidation(helper: GameTestHelper) {
        val success = CraftingRequestHarness(helper)
        var result = success.start()
        check(success.starts == 0) { "Scheduling touched the world before its server task" }
        result = success.advance(result)
        check(success.starts == 1 && success.submissions == 0 && result.callback != null) { "Pending calculation did not yield" }
        success.calculation.complete(success.plan)
        result = success.advance(result)
        check(result.result!!.contentEquals(arrayOf<Any>(true, success.jobID.toString())))
        check(success.submissions == 1 && success.sourceResolutions == 2) { "Submission did not reacquire its action source" }

        val lost = CraftingRequestHarness(helper)
        val waiting = lost.advance(lost.start())
        lost.available = false
        val rejected = lost.advance(waiting)
        check(rejected.result!![0] == null && lost.calculation.isCancelled && lost.submissions == 0) { "Lost access still submitted a crafting job" }

        val relinked = CraftingRequestHarness(helper)
        val relinkWaiting = relinked.advance(relinked.start())
        relinked.node = craftingProxy { _, _ -> error("Unexpected node call") }
        relinked.calculation.complete(relinked.plan)
        check(relinked.advance(relinkWaiting).result!![0] == null && relinked.submissions == 0) { "Relinked request submitted the old plan" }

        val failed = CraftingRequestHarness(helper)
        val failureWaiting = failed.advance(failed.start())
        failed.calculation.completeExceptionally(IllegalStateException("test calculation failed"))
        val error = runCatching { failed.advance(failureWaiting) }.exceptionOrNull()
        check(error is LuaException && error.message!!.contains("test calculation failed")) { "Calculation failure was not reported to Lua: $error" }
        check(failed.submissions == 0)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun terminatedCraftingCannotStartOrSubmit(helper: GameTestHelper) {
        for (startCalculation in listOf(false, true)) {
            val harness = CraftingRequestHarness(helper)
            var result = harness.start()
            if (startCalculation) result = harness.advance(result)
            check(runCatching { onCraftingComputerThread { result.callback!!.resume(arrayOf("terminate")) } }.exceptionOrNull() is LuaException)
            harness.runTask()
            check(harness.submissions == 0)
            if (startCalculation) check(harness.calculation.isCancelled) else check(harness.starts == 0)
        }
        helper.succeed()
    }
}

// Keep timing deterministic without introducing a test-only scheduler into production.
// The other wireless GameTest exercises real AE2 calculation/submission from Lua.
private class CraftingRequestHarness(private val helper: GameTestHelper) : ILuaContext {
    val calculation = CompletableFuture<ICraftingPlan>()
    val jobID: UUID = UUID.randomUUID()
    var starts = 0
    var submissions = 0
    var sourceResolutions = 0
    var available = true
    var node: IGridNode = craftingProxy { _, _ -> error("Unexpected node call") }
    private val tasks = LinkedBlockingQueue<Pair<Long, LuaTask>>()
    private val taskIds = AtomicLong()
    private val player = helper.makeMockPlayer()
    private val link: ICraftingLink = craftingProxy { name, _ ->
        when (name) {
            "getCraftingID" -> jobID
            else -> error("Unexpected link call: $name")
        }
    }
    val plan: ICraftingPlan = craftingProxy { name, _ ->
        when (name) {
            "missingItems" -> KeyCounter()
            "finalOutput" -> GenericStack(AEItemKey.of(Items.STONE), 1)
            else -> error("Unexpected plan call: $name")
        }
    }
    private val service: ICraftingService = craftingProxy { name, args ->
        check(helper.level.server.isSameThread) { "$name ran off the server thread" }
        when (name) {
            "getCpus" -> ImmutableSet.of<appeng.api.networking.crafting.ICraftingCPU>()
            "beginCraftingCalculation" -> {
                starts++
                val requester = args[1] as ICraftingSimulationRequester
                check(requester.gridNode === node && requester.actionSource?.player()?.orElse(null) === player)
                calculation
            }
            "submitJob" -> {
                submissions++
                check(args[0] === plan && (args[4] as IActionSource).player().orElse(null) === player)
                craftingProxy<ICraftingSubmitResult> { method, _ ->
                    when (method) {
                        "successful" -> true
                        "link" -> link
                        else -> error("Unexpected submission result call: $method")
                    }
                }
            }
            else -> error("Unexpected service call: $name")
        }
    }
    private val plugin: AE2CraftingJobsPlugin

    init {
        val contextClass = AE2CraftingJobsPlugin::class.java.declaredClasses.single { it.simpleName == "Context" }
        val contextConstructor = contextClass.getDeclaredConstructor(Level::class.java, ICraftingService::class.java, IGridNode::class.java).apply { isAccessible = true }
        val resolve: () -> Any? = {
            check(helper.level.server.isSameThread) { "Access resolution ran off the server thread" }
            if (available) contextConstructor.newInstance(helper.level, service, node) else null
        }
        val withSource: ((IActionSource) -> MethodResult) -> MethodResult = { callback ->
            check(helper.level.server.isSameThread) { "Player setup ran off the server thread" }
            sourceResolutions++
            callback(IActionSource.ofPlayer(player))
        }
        val constructor = AE2CraftingJobsPlugin::class.java.declaredConstructors.single { it.parameterCount == 3 }.apply { isAccessible = true }
        plugin = constructor.newInstance(resolve, withSource, "Access lost") as AE2CraftingJobsPlugin
    }

    override fun issueMainThreadTask(task: LuaTask): Long = taskIds.incrementAndGet().also { tasks.add(it to task) }

    fun start(): MethodResult = onCraftingComputerThread { plugin.scheduleCrafting(this, "item", "minecraft:stone", Optional.of(1L), Optional.empty()) }

    fun runTask(): Array<Any?> {
        val (id, task) = tasks.remove()
        return try {
            task.execute()
            arrayOf("task_complete", id, true)
        } catch (error: Exception) {
            arrayOf("task_complete", id, false, error.message)
        }
    }

    fun advance(result: MethodResult): MethodResult {
        val event = runTask()
        return onCraftingComputerThread { result.callback!!.resume(event) }
    }
}

private fun onCraftingComputerThread(action: () -> MethodResult): MethodResult {
    val task = FutureTask(action)
    val thread = Thread(task, "UPW crafting test computer")
    thread.start()
    return try {
        task.get(1, TimeUnit.SECONDS)
    } catch (error: java.util.concurrent.ExecutionException) {
        throw error.cause!!
    } finally {
        thread.interrupt()
        thread.join(1000)
    }
}

private inline fun <reified T> craftingProxy(noinline invoke: (String, Array<out Any?>) -> Any?): T = Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
    when (method.name) {
        "hashCode" -> System.identityHashCode(proxy)
        "equals" -> proxy === args?.get(0)
        "toString" -> "CraftingTest${T::class.java.simpleName}"
        else -> invoke(method.name, args ?: emptyArray())
    }
} as T
