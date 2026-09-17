package site.siredvin.peripheralworks.testmod

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.ControllablePeripheral
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.EnergyInfoPeripheral
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.WorkablePeripheral
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IDynamicPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.gtceu.ControllablePeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.EnergyInfoPeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.WorkablePeripheralPlugin
import site.siredvin.testiarium.api.TestGroup

@TestGroup("gtceu")
class GTCEUPeripheralGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun nativeMethodParity(helper: GameTestHelper) {
        val groups = listOf(
            Triple(GTCapabilityHelper::getWorkable, WorkablePeripheral::class.java, WorkablePeripheralPlugin.TYPE),
            Triple(GTCapabilityHelper::getControllable, ControllablePeripheral::class.java, ControllablePeripheralPlugin.TYPE),
            Triple(GTCapabilityHelper::getEnergyInfoProvider, EnergyInfoPeripheral::class.java, EnergyInfoPeripheralPlugin.TYPE),
        )
        val observed = mutableSetOf<String>()
        val pos = helper.absolutePos(BlockPos(1, 1, 1))
        for (name in listOf("lv_macerator", "steam_large_turbine")) {
            val id = ResourceLocation.fromNamespaceAndPath("gtceu", name)
            check(BuiltInRegistries.BLOCK.containsKey(id)) { "Missing fixture $id" }
            helper.level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(id).defaultBlockState())

            val plugins = ComputerCraftProxy.collectPlugins(helper.level, pos, Direction.NORTH)
            val peripheral = ComputerCraftProxy.lazyPeripheralProvider(helper.level, pos, Direction.NORTH)?.get() as IDynamicPeripheral
            val names = peripheral.methodNames.toSet()
            check("getRecipeTypes" in names) { "$id lost UPW recipe helpers" }
            if (name != "lv_macerator") check("getPartNames" in names) { "$id lost UPW multiblock helpers" }
            for ((getCapability, nativeClass, type) in groups) {
                val target = getCapability(helper.level, pos, null) ?: continue
                val plugin = checkNotNull(plugins[type]) { "$id lost $type" }
                check(type in peripheral.additionalTypes) { "$id lost peripheral type $type" }
                observed.add(type)
                for (method in nativeClass.methods.filter { it.isAnnotationPresent(LuaFunction::class.java) }) {
                    check(method.name in names) { "$id did not expose ${method.name} to CC:Tweaked" }
                    val wrapper = plugin.javaClass.getMethod(method.name, *method.parameterTypes.drop(1).toTypedArray())
                    check(wrapper.getAnnotation(LuaFunction::class.java)?.mainThread == true) { "${method.name} must use the server thread" }
                    val args: Array<Any> = when (method.name) {
                        "setWorkingEnabled", "setSuspendAfterFinish" -> arrayOf(false)
                        else -> emptyArray()
                    }
                    val expected = method.invoke(null, target, *args)
                    val actual = wrapper.invoke(plugin, *args)
                    if (expected is MethodResult && actual is MethodResult) {
                        check(actual.result.contentDeepEquals(expected.result)) { "$type.${method.name} differs from GTCEu" }
                    } else {
                        check(actual == expected)
                    }
                }
            }
        }
        check(observed == groups.map { it.third }.toSet()) { "Fixture did not exercise all native groups: $observed" }
        helper.succeed()
    }
}
