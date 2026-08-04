package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.crafting.CalculationStrategy
import appeng.api.networking.crafting.ICraftingLink
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.security.IActionSource
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.buildKey
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.keyCounterToLua
import site.siredvin.peripheralworks.integrations.ae2.AE2Helper.stackToMap
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
