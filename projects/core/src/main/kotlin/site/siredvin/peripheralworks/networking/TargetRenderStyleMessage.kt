package site.siredvin.peripheralworks.networking

import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.PeripheralProxyMode
import site.siredvin.peripheralworks.subsystem.configurator.RemoteObserverMode
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle

class TargetRenderStyleMessage(
    private val pos: BlockPos,
    private val textStyle: Boolean,
    private val value: String,
) : NetworkMessage<ServerNetworkContext> {
    constructor(buf: FriendlyByteBuf) : this(buf.readBlockPos(), buf.readBoolean(), buf.readUtf(MAX_STYLE_LENGTH))

    override fun type(): MessageType<*> = NetworkMessages.TARGET_RENDER_STYLE

    override fun write(buf: FriendlyByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeBoolean(textStyle)
        buf.writeUtf(value, MAX_STYLE_LENGTH)
    }

    override fun handle(context: ServerNetworkContext) {
        val player = context.getSender()
        val stack = player.mainHandItem
        val activeMode = (stack.item as? UltimateConfigurator)?.getActiveMode(stack)
        if (!stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) || activeMode?.second != pos || !UltimateConfigurator.isActiveModeDimension(stack, player.level()) || !player.level().isLoaded(pos)) {
            reject(player)
            return
        }
        val target = player.level().getBlockEntity(pos)
        val validTarget = activeMode.first.modeID == PeripheralProxyMode.modeID &&
            target is PeripheralProxyBlockEntity ||
            activeMode.first.modeID == RemoteObserverMode.modeID &&
            target is RemoteObserverBlockEntity
        if (!validTarget) {
            reject(player)
            return
        }
        val valid = if (textStyle) {
            TextStyle.entries.firstOrNull { it.name.lowercase() == value }?.also {
                when (target) {
                    is PeripheralProxyBlockEntity -> target.setTextStyle(it)
                    is RemoteObserverBlockEntity -> target.setTextStyle(it)
                }
            } != null
        } else {
            BoxStyle.entries.firstOrNull { it.name.lowercase() == value }?.also {
                when (target) {
                    is PeripheralProxyBlockEntity -> target.setBoxStyle(it)
                    is RemoteObserverBlockEntity -> target.setBoxStyle(it)
                }
            } != null
        }
        if (!valid) reject(player)
    }

    private fun reject(player: net.minecraft.world.entity.player.Player) {
        player.displayClientMessage(ModText.TARGET_RENDER_SETTINGS_REQUEST_REJECTED.text, true)
    }

    companion object {
        private const val MAX_STYLE_LENGTH = 16
    }
}
