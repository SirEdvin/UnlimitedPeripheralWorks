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
    val textColor: Int? = null,
    val boxColor: Int? = null,
) {
    fun matches(other: ConfiguratorTarget): Boolean = dimensionID == other.dimensionID && pos == other.pos

    fun toNBT(): CompoundTag = CompoundTag().apply {
        putString(MODE, modeID.toString())
        putString(DIMENSION, dimensionID.toString())
        put(POS, NbtUtils.writeBlockPos(pos))
        name?.takeIf { it.isNotEmpty() && it.length <= MAX_NAME_LENGTH }?.let { putString(NAME, it) }
        textColor?.let { putInt(TEXT_COLOR, it) }
        boxColor?.let { putInt(BOX_COLOR, it) }
    }

    companion object {
        const val MAX_NAME_LENGTH = 64
        private const val MODE = "mode"
        private const val DIMENSION = "dimension"
        private const val POS = "pos"
        private const val NAME = "name"
        private const val TEXT_COLOR = "textColor"
        private const val BOX_COLOR = "boxColor"

        fun fromNBT(tag: CompoundTag): ConfiguratorTarget? {
            if (!tag.contains(MODE, Tag.TAG_STRING.toInt()) || !tag.contains(DIMENSION, Tag.TAG_STRING.toInt()) || !tag.contains(POS, Tag.TAG_INT_ARRAY.toInt())) return null
            val mode = ResourceLocation.tryParse(tag.getString(MODE))?.takeIf { ConfiguratorModeRegistry.get(it) != null } ?: return null
            val dimension = ResourceLocation.tryParse(tag.getString(DIMENSION)) ?: return null
            val pos = NbtUtils.readBlockPos(tag, POS).orElse(null) ?: return null
            val name = tag.getString(NAME).takeIf { tag.contains(NAME, Tag.TAG_STRING.toInt()) && it.isNotEmpty() && it.length <= MAX_NAME_LENGTH }
            val textColor = tag.getInt(TEXT_COLOR).takeIf { tag.contains(TEXT_COLOR, Tag.TAG_INT.toInt()) && it in 0..0xffffff }
            val boxColor = tag.getInt(BOX_COLOR).takeIf { tag.contains(BOX_COLOR, Tag.TAG_INT.toInt()) && it in 0..0xffffff }
            return ConfiguratorTarget(mode, dimension, pos, name, textColor, boxColor)
        }
    }
}
