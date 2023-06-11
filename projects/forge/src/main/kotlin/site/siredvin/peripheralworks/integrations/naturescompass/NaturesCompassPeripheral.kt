package site.siredvin.peripheralworks.integrations.naturescompass

import com.chaosthedude.naturescompass.NaturesCompass
import com.chaosthedude.naturescompass.items.NaturesCompassItem
import com.chaosthedude.naturescompass.util.BiomeUtils
import com.chaosthedude.naturescompass.util.CompassState
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.ext.toRelative

class NaturesCompassPeripheral<O : IPeripheralOwner>(peripheralOwner: O, override val isEnabled: Boolean) :
    OwnedPeripheral<O>(TYPE, peripheralOwner) {
    companion object {
        const val TYPE = "natures_compass"
    }

    var compassStack: ItemStack = NaturesCompass.naturesCompass.defaultInstance.copy()

    val compass: NaturesCompassItem
        get() = NaturesCompass.naturesCompass

    @LuaFunction
    fun getBiomes(): List<String> {
        return BiomeUtils.getAllowedBiomeKeys(peripheralOwner.level).map { it.toString() }
    }

    @LuaFunction
    fun scheduleSearch(biome: String): MethodResult {
        if (compass.getState(compassStack) == CompassState.SEARCHING) {
            return MethodResult.of(null, "Another compass search is running, stop it to start another")
        }
        val biomeLoc = ResourceLocation(biome)
        val optionalBiome = BiomeUtils.getBiomeForKey(level, biomeLoc)
        if (optionalBiome.isEmpty) {
            return MethodResult.of(null, "Incorrect biome id $biome")
        }
        return peripheralOwner.withPlayer({
            compass.searchForBiome(it.fakePlayer.serverLevel(), it.fakePlayer, biomeLoc, peripheralOwner.pos, compassStack)
            return@withPlayer MethodResult.of(true)
        })
    }

    @LuaFunction
    fun getState(): String {
        return compass.getState(compassStack).name
    }

    @LuaFunction
    fun getResult(): Map<String, Any>? {
        if (compass.getState(compassStack) != CompassState.FOUND) {
            return null
        }
        val x = compass.getFoundBiomeX(compassStack)
        val z = compass.getFoundBiomeZ(compassStack)
        val ownerPos = peripheralOwner.pos
        var facing = peripheralOwner.facing
        if (facing == Direction.UP || facing == Direction.DOWN) {
            facing = Direction.EAST
        }
        val relativePos = BlockPos(x, 0, z).subtract(ownerPos).toRelative(facing)
        return mapOf(
            "x" to relativePos.x,
            "z" to relativePos.z,
            "distance" to BiomeUtils.getDistanceToBiome(peripheralOwner.pos, x, z),
            "biome" to compass.getBiomeKey(compassStack).toString(),
        )
    }
}
