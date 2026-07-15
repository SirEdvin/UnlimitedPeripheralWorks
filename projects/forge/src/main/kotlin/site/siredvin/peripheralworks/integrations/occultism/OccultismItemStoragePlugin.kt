package site.siredvin.peripheralworks.integrations.occultism

import com.klikli_dev.occultism.api.common.blockentity.IStorageController
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.plugins.AbstractItemStoragePlugin

class OccultismItemStoragePlugin(private val storageController: IStorageController, override val level: Level) : AbstractItemStoragePlugin() {
    override val storage = OccultismItemStorage(storageController)
    override val itemStorageTransferLimit: Int
        get() = PeripheralWorksConfig.itemStorageTransferLimit

    @LuaFunction(mainThread = true)
    fun getMaxSlots(): Int = storageController.maxSlots

    @LuaFunction(mainThread = true)
    fun getUsedSlots(): Int = storageController.usedSlots

    @LuaFunction(mainThread = true)
    fun isBlacklisted(item: String): Boolean {
        val itemInstance = PlatformRegistries.ITEMS.get(ResourceLocation.parse(item))
        if (itemInstance == Items.AIR) {
            throw LuaException("Cannot find item with id $item")
        }
        return storageController.isBlacklisted(itemInstance.defaultInstance)
    }
}
