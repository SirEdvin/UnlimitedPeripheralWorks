package site.siredvin.peripheralworks.api

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.Tag

interface ISavableComponent {
    fun save(registries: HolderLookup.Provider): Tag
    fun load(tag: Tag, registries: HolderLookup.Provider)
}
