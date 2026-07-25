package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation

data class ConfiguratorTarget(
    val modeID: ResourceLocation,
    val dimensionID: ResourceLocation,
    val pos: BlockPos,
    val name: String? = null,
) {
    fun matches(other: ConfiguratorTarget): Boolean = dimensionID == other.dimensionID && pos == other.pos

    fun toNBT(): CompoundTag = CompoundTag().apply {
        putString(MODE, modeID.toString())
        putString(DIMENSION, dimensionID.toString())
        put(POS, NbtUtils.writeBlockPos(pos))
        name?.takeIf { it.isNotEmpty() && it.length <= MAX_NAME_LENGTH }?.let { putString(NAME, it) }
    }

    companion object {
        const val MAX_NAME_LENGTH = 64
        private const val MODE = "mode"
        private const val DIMENSION = "dimension"
        private const val POS = "pos"
        private const val NAME = "name"

        fun fromNBT(tag: CompoundTag): ConfiguratorTarget? {
            if (!tag.contains(MODE, Tag.TAG_STRING.toInt()) || !tag.contains(DIMENSION, Tag.TAG_STRING.toInt()) || !tag.contains(POS, Tag.TAG_COMPOUND.toInt())) return null
            val mode = ResourceLocation.tryParse(tag.getString(MODE))?.takeIf { ConfiguratorModeRegistry.get(it) != null } ?: return null
            val dimension = ResourceLocation.tryParse(tag.getString(DIMENSION)) ?: return null
            val posTag = tag.getCompound(POS)
            if (!posTag.contains("X", Tag.TAG_ANY_NUMERIC.toInt()) || !posTag.contains("Y", Tag.TAG_ANY_NUMERIC.toInt()) || !posTag.contains("Z", Tag.TAG_ANY_NUMERIC.toInt())) return null
            val name = tag.getString(NAME).takeIf { tag.contains(NAME, Tag.TAG_STRING.toInt()) && it.isNotEmpty() && it.length <= MAX_NAME_LENGTH }
            return ConfiguratorTarget(mode, dimension, NbtUtils.readBlockPos(posTag), name)
        }
    }
}
