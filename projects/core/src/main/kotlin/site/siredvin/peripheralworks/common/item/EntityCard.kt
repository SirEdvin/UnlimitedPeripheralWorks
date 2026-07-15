package site.siredvin.peripheralworks.common.item

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import site.siredvin.broccolium.modules.base.item.DescriptiveItem
import site.siredvin.peripheralworks.common.block.EntityLink
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup
import site.siredvin.peripheralworks.tags.EntityTags
import java.util.UUID
import java.util.function.Function

class EntityCard : DescriptiveItem(Properties().stacksTo(1)) {
    companion object {
        private val CUSTOM_MODEL_DATA_TAG = "CustomModelData"
        private val ENTITY_UUID_TAG = "entityUUID"
        val EXTRA_SEARCHES: MutableMap<Item, Function<Player, Entity?>> = mutableMapOf()
        fun isEmpty(itemStack: ItemStack): Boolean {
            val itemTag = itemStack.get(DataComponents.CUSTOM_DATA) ?: return true
            return !itemTag.contains(CUSTOM_MODEL_DATA_TAG)
        }

        fun isEntityMatching(entity: Entity): Boolean {
            if (entity.type.`is`(EntityTags.LINK_BLOCKLIST)) return false
            return EntityPeripheralLookup.collectPlugins(entity).isNotEmpty()
        }

        fun storeEntity(itemStack: ItemStack, entity: Entity) {
            val itemTag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            itemTag.putByte(CUSTOM_MODEL_DATA_TAG, 1)
            itemTag.putUUID(ENTITY_UUID_TAG, entity.uuid)
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTag))
        }

        fun getEntityUUID(itemStack: ItemStack): UUID? {
            val itemTag = itemStack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
            if (!itemTag.contains(ENTITY_UUID_TAG)) return null
            return itemTag.getUUID(ENTITY_UUID_TAG)
        }
    }

    override fun isFoil(stack: ItemStack): Boolean = !isEmpty(stack)

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        if (!isEmpty(itemStack)) {
            list.add(ModText.SOMETHING_STORED_INSIDE_CARD.text)
        }
        super.appendHoverText(itemStack, context, list, tooltipFlag)
    }

    override fun use(level: Level, player: Player, interactionHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemInHand = player.getItemInHand(interactionHand)
        val blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE)
        if (interactionHand == InteractionHand.MAIN_HAND) {
            if (!isEmpty(itemInHand)) {
                // TODO: So ... this part is not working nice on forge for some reason (?)
                // it is called only for client side
                if (blockHitResult.type == HitResult.Type.BLOCK) {
                    val blockEntity = level.getBlockEntity(blockHitResult.blockPos) as? EntityLinkBlockEntity
                        ?: return InteractionResultHolder.pass(itemInHand)
                    if (blockEntity.blockState.getValue(EntityLink.CONFIGURED)) {
                        return InteractionResultHolder.pass(
                            itemInHand,
                        )
                    }
                    blockEntity.storedStack = itemInHand
                    return InteractionResultHolder.consume(ItemStack.EMPTY)
                }
                if (level is ServerLevel) {
                    val entityUUID = getEntityUUID(itemInHand) ?: return InteractionResultHolder.pass(itemInHand)
                    val entity = level.getEntity(entityUUID) ?: return InteractionResultHolder.pass(itemInHand)
                    player.displayClientMessage(
                        ModText.TARGET_ENTITY.format(
                            entity.name.string,
                            entity.blockPosition().toString(),
                        ),
                        false,
                    )
                }
            } else {
                val itemInOffhand = player.getItemInHand(InteractionHand.OFF_HAND)
                val extraLogic = EXTRA_SEARCHES.get(itemInOffhand.item)
                if (extraLogic != null) {
                    val newEntity = extraLogic.apply(player)
                    if (newEntity != null) {
                        storeEntity(itemInHand, newEntity)
                    }
                }
                return InteractionResultHolder.consume(itemInHand)
            }
        }
        return InteractionResultHolder.pass(itemInHand)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND)
        if (itemInHand.`is`(Items.ENTITY_CARD.get()) && isEmpty(itemInHand)) {
            val itemInOffhand = player.getItemInHand(InteractionHand.OFF_HAND)
            val extraLogic = EXTRA_SEARCHES.get(itemInOffhand.item)
            if (extraLogic != null) {
                val newEntity = extraLogic.apply(player)
                if (newEntity != null) {
                    storeEntity(itemInHand, newEntity)
                }
            }
            return InteractionResult.CONSUME
        }
        return super.useOn(context)
    }
}
