package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.item.ItemStack
import site.siredvin.tweakium.modules.peripheral.util.NBTUtil
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import kotlin.jvm.optionals.getOrNull

enum class NBTCheckMode {
    FULL,
    SUBSET,
    SUPERSET,
    NONE,
    ;

    fun itemStackEquals(result: ItemStack, targetingResult: ItemStack): Boolean {
        if (result.item != targetingResult.item) {
            return false
        }
        if (this == NONE) {
            return true
        }
        ComputerPlatformToolkit.get()
        val targetingNBT = DataComponentPatch.CODEC.encodeStart(
            NbtOps.INSTANCE,
            targetingResult.componentsPatch,
        ).result().getOrNull()
        val resultNBT = DataComponentPatch.CODEC.encodeStart(
            NbtOps.INSTANCE,
            result.componentsPatch,
        ).result().getOrNull()
        if (targetingNBT == null) {
            return resultNBT == null || this == SUBSET
        }
        if (resultNBT == null) {
            return this == SUPERSET
        }
        if (this == FULL) {
            return resultNBT == targetingNBT
        }
        if (resultNBT !is CompoundTag || targetingNBT !is CompoundTag) {
            return false
        }
        if (this == SUBSET) {
            return NBTUtil.isSubSet(resultNBT, targetingNBT)
        }
        return NBTUtil.isSubSet(targetingNBT, resultNBT)
    }
}
