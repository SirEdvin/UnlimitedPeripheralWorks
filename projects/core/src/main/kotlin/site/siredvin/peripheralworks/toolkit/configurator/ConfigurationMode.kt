package site.siredvin.peripheralworks.toolkit.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

interface ConfigurationMode {
    val modeID: ResourceLocation
    val description: Component
    fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }
    fun onEntityClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: EntityHitResult, level: Level): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }
}
