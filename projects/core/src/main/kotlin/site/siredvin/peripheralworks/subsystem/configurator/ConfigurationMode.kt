package site.siredvin.peripheralworks.subsystem.configurator

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

interface ConfigurationMode {
    val modeID: ResourceLocation
    val description: Component
    fun onBlockClick(configurationTarget: BlockPos, stack: ItemStack, player: Player, hit: BlockHitResult, level: Level): InteractionResultHolder<ItemStack> = InteractionResultHolder.pass(stack)
    fun onBlockMiss(configurationTarget: BlockPos, stack: ItemStack, player: Player, level: Level): InteractionResultHolder<ItemStack> = InteractionResultHolder.pass(stack)
    fun onSwing(configurationTarget: BlockPos, stack: ItemStack, owner: Player): Boolean = false
    fun onHurtEntity(configurationTarget: BlockPos, stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean = false
    fun extraTooltips(itemStack: ItemStack, tooltip: MutableList<Component>) {}
}
