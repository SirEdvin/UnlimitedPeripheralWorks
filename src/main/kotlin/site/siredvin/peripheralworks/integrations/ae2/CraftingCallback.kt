package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.networking.crafting.CalculationStrategy
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import dan200.computercraft.api.lua.ILuaCallback
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.PeripheralWorks

class CraftingCallback (
    private val computers: Collection<IComputerAccess>, private val craftingService: ICraftingService, private val aeKey: AEKey, private val amount: Long,
    private val source: IActionSource, private val level: Level): ILuaCallback, Runnable {

    private var result: MethodResult? = null
    private var error: Exception? = null

    companion object {
        const val COMPLETE = "ae2_crafting_complete"
    }

    override fun resume(args: Array<out Any>?): MethodResult {
        if (this.error != null) throw error!!
        PeripheralWorks.LOGGER.info("Resume is called!!!!!!!!!!!!")
        return this.result ?: throw LuaException("unexpected state in $COMPLETE");
    }

    override fun run() {
        try {
            PeripheralWorks.LOGGER.info("Starting crafting job")
            val future = craftingService.beginCraftingCalculation(
                level, { source }, aeKey, amount,
                CalculationStrategy.REPORT_MISSING_ITEMS
            )
            val plan = future.get()
            PeripheralWorks.LOGGER.info("Plan ready: $plan")

            if (!plan.missingItems().isEmpty) {
                this.result = MethodResult.of(false, "Missing items", AE2Helper.keyCounterToLua(plan.missingItems()))
                this.computers.forEach { it.queueEvent(COMPLETE, 5) }
                return;
            }

            PeripheralWorks.LOGGER.info("All items present")

            craftingService.submitJob(plan, null, null, false, source)
            PeripheralWorks.LOGGER.info("Crafting job submitted!")
            this.result = MethodResult.of("Hahahahah!!!", "hsdvfjhsdvfsghdf")
            this.computers.forEach { it.queueEvent(COMPLETE, 5) }
        } catch (e: Exception) {
            this.error = e
            this.computers.forEach { it.queueEvent(COMPLETE, 5) }
        }
    }
}