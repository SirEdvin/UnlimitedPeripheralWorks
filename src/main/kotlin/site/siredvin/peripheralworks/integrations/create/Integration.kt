package site.siredvin.peripheralworks.integrations.create

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity
import net.minecraft.core.BlockPos
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class Integration : Runnable {
    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration("create", Configuration)
        ExtractorProxy.addCCItemStorageExtractor { level, obj ->
            val extractionTarget: SidedStorageBlockEntity = if (obj is BlockPos) {
                level.getBlockEntity(obj) as? SidedStorageBlockEntity
            } else {
                obj as? SidedStorageBlockEntity
            } ?: return@addCCItemStorageExtractor null
            val extractedStorage = extractionTarget.getItemStorage(null) as? CombinedStorage<*, *> ?: return@addCCItemStorageExtractor null
            // Making sure that all of parts are slotted variants
            if (extractedStorage.parts.any { it !is ItemStackHandler }) {
                return@addCCItemStorageExtractor null
            }
            return@addCCItemStorageExtractor CreateCCItemStorage(extractedStorage.parts.map { it as ItemStackHandler })
        }
    }
}
