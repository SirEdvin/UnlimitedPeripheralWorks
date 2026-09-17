package site.siredvin.peripheralworks.testmod

import com.gregtechceu.gtceu.api.capability.forge.GTCapability
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.CentralMonitorPeripheral
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.ControllablePeripheral
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.CoverHolderPeripheral
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.EnergyInfoPeripheral
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.TurbineMachinePeripheral
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
import net.minecraftforge.common.capabilities.Capability
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.gtceu.CentralMonitorPeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.ControllablePeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.CoverHolderPeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.EnergyInfoPeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.TurbineMachinePeripheralPlugin
import site.siredvin.peripheralworks.integrations.gtceu.WorkablePeripheralPlugin
import site.siredvin.testiarium.api.TestGroup

@TestGroup("gtceu")
class GTCEUPeripheralGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun nativeMethodParity(helper: GameTestHelper) {
        val groups = listOf(
            Triple(GTCapability.CAPABILITY_WORKABLE, WorkablePeripheral::class.java, WorkablePeripheralPlugin.TYPE),
            Triple(GTCapability.CAPABILITY_CONTROLLABLE, ControllablePeripheral::class.java, ControllablePeripheralPlugin.TYPE),
            Triple(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, EnergyInfoPeripheral::class.java, EnergyInfoPeripheralPlugin.TYPE),
            Triple(GTCapability.CAPABILITY_TURBINE_MACHINE, TurbineMachinePeripheral::class.java, TurbineMachinePeripheralPlugin.TYPE),
            Triple(GTCapability.CAPABILITY_COVERABLE, CoverHolderPeripheral::class.java, CoverHolderPeripheralPlugin.TYPE),
            Triple(GTCapability.CAPABILITY_CENTRAL_MONITOR, CentralMonitorPeripheral::class.java, CentralMonitorPeripheralPlugin.TYPE),
        )
        val observed = mutableSetOf<String>()
        val pos = helper.absolutePos(BlockPos(1, 1, 1))
        for (name in listOf("lv_macerator", "steam_large_turbine", "central_monitor")) {
            val id = ResourceLocation("gtceu", name)
            check(BuiltInRegistries.BLOCK.containsKey(id)) { "Missing fixture $id" }
            helper.level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(id).defaultBlockState())
            val entity = checkNotNull(helper.level.getBlockEntity(pos))
            val plugins = ComputerCraftProxy.collectPlugins(helper.level, pos, Direction.NORTH)
            val peripheral = ComputerCraftProxy.lazyPeripheralProvider(helper.level, pos, Direction.NORTH)?.get() as IDynamicPeripheral
            val names = peripheral.methodNames.toSet()
            check("getRecipeTypes" in names) { "$id lost UPW recipe helpers" }
            if (name != "lv_macerator") check("getPartNames" in names) { "$id lost UPW multiblock helpers" }
            for ((capability, nativeClass, type) in groups) {
                @Suppress("UNCHECKED_CAST")
                val target = entity.getCapability(capability as Capability<Any>).resolve().orElse(null) ?: continue
                val plugin = checkNotNull(plugins[type]) { "$id lost $type" }
                check(type in peripheral.additionalTypes) { "$id lost peripheral type $type" }
                observed.add(type)
                for (method in nativeClass.methods.filter { it.isAnnotationPresent(LuaFunction::class.java) }) {
                    check(method.name in names) { "$id did not expose ${method.name} to CC:Tweaked" }
                    val wrapper = plugin.javaClass.getMethod(method.name, *method.parameterTypes.drop(1).toTypedArray())
                    check(wrapper.getAnnotation(LuaFunction::class.java)?.mainThread == true) { "${method.name} must use the server thread" }
                    val args: Array<Any> = when (method.name) {
                        "setWorkingEnabled", "setSuspendAfterFinish" -> arrayOf(false)
                        "setBufferedText" -> arrayOf("invalid", 0, "test")
                        "parsePlaceholders" -> arrayOf("invalid", "test")
                        else -> emptyArray()
                    }
                    val expected = method.invoke(null, target, *args)
                    val actual = wrapper.invoke(plugin, *args)
                    if (expected is MethodResult && actual is MethodResult) {
                        // Central monitor groups are fresh native wrapper objects, not value objects.
                        if (method.name == "getGroups") {
                            check((actual.result!![0] as List<*>).size == (expected.result!![0] as List<*>).size)
                        } else {
                            check(actual.result.contentDeepEquals(expected.result)) { "$type.${method.name} differs from GTCEu" }
                        }
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
