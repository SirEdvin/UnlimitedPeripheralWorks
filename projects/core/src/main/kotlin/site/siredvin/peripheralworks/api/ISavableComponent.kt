package site.siredvin.peripheralworks.api

import net.minecraft.nbt.Tag

interface ISavableComponent {
    fun save(): Tag
    fun load(tag: Tag)
}
