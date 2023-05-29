package site.siredvin.peripheralworks.integrations.occultism

import com.github.klikli_dev.occultism.api.common.blockentity.IStorageController
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.extra.plugins.AbstractItemStoragePlugin
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class OccultismItemStoragePlugin(private val storageController: IStorageController, override val level: Level) : AbstractItemStoragePlugin() {
    override val storage = OccultismItemStorage(storageController)
    override val itemStorageTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit

    @LuaFunction(mainThread = true)
    fun getMaxSlots(): Int {
        return storageController.maxSlots
    }

    @LuaFunction(mainThread = true)
    fun getUsedSlots(): Int {
        return storageController.usedSlots
    }

    @LuaFunction(mainThread = true)
    fun isBlacklisted(item: String): Boolean {
        val itemInstance = XplatRegistries.ITEMS.get(ResourceLocation(item))
        if (itemInstance == Items.AIR) {
            throw LuaException("Cannot find item with id $item")
        }
        return storageController.isBlacklisted(itemInstance.defaultInstance)
    }
}
