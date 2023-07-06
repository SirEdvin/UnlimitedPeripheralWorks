package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.util.NBTUtil

enum class NBTCheckMode {
    FULL, SUBSET, SUPERSET, NONE;

    fun itemStackEquals(result: ItemStack, targetingResult: ItemStack): Boolean {
        if (result.item != targetingResult.item)
            return false;
        if (this == NONE)
            return true;
        val targetingNBT = targetingResult.tag;
        val resultNBT = result.tag;
        if (targetingNBT == null)
            return resultNBT == null || this == SUBSET;
        if (resultNBT == null)
            return this == SUPERSET;
        if (this == FULL)
            return resultNBT == targetingNBT;
        if (this == SUBSET)
            return NBTUtil.isSubSet(resultNBT, targetingNBT);
        return NBTUtil.isSubSet(targetingNBT, resultNBT);
    }
}