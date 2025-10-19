package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.CreativeModeTab
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.common.setup.*
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.tweakium.modules.platform.ComputerPlatformRegistries
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit

object PeripheralWorksCommonHooks {

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        RecipeSerializers.doSomething()
        PocketUpgradeSerializers.doSomething()
        TurtleUpgradeSerializers.doSomething()
        ModEnergies.doSomething()
        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
        ModPlatform.registerCreativeTab(
            ResourceLocation(PeripheralWorksCore.MOD_ID, "tab"),
            PeripheralWorksCore.configureCreativeTab(PlatformToolkit.get().createTabBuilder()).build(),
        )
    }

    fun registerUpgradesInCreativeTab(output: CreativeModeTab.Output) {
        ModPlatform.holder.turtleSerializers.forEach {
            val upgrade = ComputerPlatformToolkit.get().getTurtleUpgrade(ComputerPlatformRegistries.TURTLE_SERIALIZERS.getKey(it.get()).toString())
            if (upgrade != null) {
                ComputerPlatformToolkit.get().createTurtlesWithUpgrade(UpgradeData.ofDefault(upgrade)).forEach(output::accept)
            }
        }
        ModPlatform.holder.pocketSerializers.forEach {
            val upgrade = ComputerPlatformToolkit.get().getPocketUpgrade(ComputerPlatformRegistries.POCKET_SERIALIZERS.getKey(it.get()).toString())
            if (upgrade != null) {
                ComputerPlatformToolkit.get().createPocketsWithUpgrade(UpgradeData.ofDefault(upgrade)).forEach(output::accept)
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
            } else {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(ModText.ENTITY_CANNOT_BE_STORED.text, false)
                }
            }
            return true
        }
        if (itemInHand.`is`(Items.ANALYZER.get())) {
            if (player is ServerPlayer) {
                player.displayClientMessage(
                    Component.literal("Entity class: ${entity.javaClass}, entityType: ${entity.type}"),
                    false,
                )
            }
            return true
        }
        return false
    }
}
