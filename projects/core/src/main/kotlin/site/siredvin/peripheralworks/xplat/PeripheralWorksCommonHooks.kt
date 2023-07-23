package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.CreativeModeTab
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.common.setup.*
import site.siredvin.peripheralworks.data.ModText

object PeripheralWorksCommonHooks {

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        RecipeSerializers.doSomething()
        PocketUpgradeSerializers.doSomething()
        TurtleUpgradeSerializers.doSomething()
        ModPlatform.registerCreativeTab(
            ResourceLocation(PeripheralWorksCore.MOD_ID, "tab"),
            PeripheralWorksCore.configureCreativeTab(PeripheraliumPlatform.createTabBuilder()).build(),
        )
    }

    fun registerUpgradesInCreativeTab(output: CreativeModeTab.Output) {
        ModPlatform.holder.turtleSerializers.forEach {
            val upgrade = PeripheraliumPlatform.getTurtleUpgrade(XplatRegistries.TURTLE_SERIALIZERS.getKey(it.get()).toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createTurtlesWithUpgrade(UpgradeData.ofDefault(upgrade)).forEach(output::accept)
            }
        }
        ModPlatform.holder.pocketSerializers.forEach {
            val upgrade = PeripheraliumPlatform.getPocketUpgrade(XplatRegistries.POCKET_SERIALIZERS.getKey(it.get()).toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createPocketsWithUpgrade(UpgradeData.ofDefault(upgrade)).forEach(output::accept)
            }
        }
    }

    /**
     * So, design is pretty simple, this event do what it should do and then return true if event should be somehow cancelled
     * or false if not
     */
    fun onEntityRightClick(player: Player, entity: Entity): Boolean {
        val itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND)
        if (itemInHand.`is`(Items.ENTITY_CARD.get()) && EntityCard.isEmpty(itemInHand)) {
            if (EntityCard.isEntityMatching(entity)) {
                EntityCard.storeEntity(itemInHand, entity)
                player.setItemInHand(InteractionHand.MAIN_HAND, itemInHand)
                return true
            } else {
                player.displayClientMessage(ModText.ENTITY_CANNOT_BE_STORED.text, false)
            }
        }
        return false
    }
}
