package site.siredvin.peripheralworks.xplat

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.math.Fraction
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyRegistry
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.common.setup.*
import site.siredvin.peripheralworks.data.ModText

object PeripheralWorksCommonHooks {

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        RecipeSerializers.doSomething()
        ModPocketUpgrades.doSomething()
        ModTurtleUpgrades.doSomething()
        ModDataComponents.doSomething()
        ModEnergies.doSomething()
        ModPlatform.registerCreativeTab(
            ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "tab"),
            PeripheralWorksCore.configureCreativeTab(PlatformToolkit.get().createTabBuilder()).build(),
        )
    }

    fun afterConfigurationLoaded() {
        if (PeripheralWorksConfig.enableTurtleRefuelWithEnergy) {
            EnergyRegistry.registerConversion(PlatformToolkit.get().commonEnergy, Energies.TURTLE_FUEL, Fraction.getFraction(1, PeripheralWorksConfig.energyToFuelRate))
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
