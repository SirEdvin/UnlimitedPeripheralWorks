package site.siredvin.peripheralworks.networking

import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorTarget
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle

class ConfiguratorTargetActionMessage(
    private val action: Action,
    private val dimension: ResourceLocation,
    private val pos: BlockPos,
    private val name: String = "",
    private val color: Int = -1,
) : NetworkMessage<ServerNetworkContext> {
    enum class Action { SELECT, TOGGLE_FAVORITE, RENAME, FAVORITE_TEXT_COLOR, FAVORITE_BOX_COLOR, CONFIGURATOR_NAME, DEFAULT_TEXT_STYLE, DEFAULT_BOX_STYLE }

    constructor(buf: FriendlyByteBuf) : this(
        buf.readEnum(Action::class.java),
        buf.readResourceLocation(),
        buf.readBlockPos(),
        buf.readUtf(MAX_WIRE_NAME_LENGTH),
        buf.readInt(),
    )

    override fun type(): MessageType<*> = NetworkMessages.CONFIGURATOR_TARGET_ACTION

    override fun write(buf: FriendlyByteBuf) {
        buf.writeEnum(action)
        buf.writeResourceLocation(dimension)
        buf.writeBlockPos(pos)
        buf.writeUtf(name, MAX_WIRE_NAME_LENGTH)
        buf.writeInt(color)
    }

    override fun handle(context: ServerNetworkContext) {
        val player = context.getSender()
        val stack = player.mainHandItem
        val configurator = stack.item as? UltimateConfigurator
        if (!stack.`is`(Items.ULTIMATE_CONFIGURATOR.get()) || configurator == null || configurator.getActiveMode(stack) != null) {
            player.displayClientMessage(ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED.text, true)
            return
        }
        val target = ConfiguratorTarget(PLACEHOLDER_MODE, dimension, pos)
        val result = when (action) {
            Action.SELECT -> when (configurator.selectTarget(stack, player.level(), target)) {
                UltimateConfigurator.SelectionResult.SUCCESS -> null
                UltimateConfigurator.SelectionResult.UNAVAILABLE -> ModText.CONFIGURATOR_HISTORY_TARGET_UNAVAILABLE
                UltimateConfigurator.SelectionResult.REJECTED -> ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            }
            Action.TOGGLE_FAVORITE -> when (configurator.toggleFavorite(stack, target)) {
                UltimateConfigurator.FavoriteResult.ADDED, UltimateConfigurator.FavoriteResult.REMOVED -> null
                UltimateConfigurator.FavoriteResult.LIMIT -> ModText.CONFIGURATOR_HISTORY_FAVORITE_LIMIT
                UltimateConfigurator.FavoriteResult.REJECTED -> ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            }
            Action.RENAME -> if (configurator.renameFavorite(stack, target, name)) null else ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            Action.FAVORITE_TEXT_COLOR -> if (configurator.setFavoriteColor(stack, target, color, true)) null else ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            Action.FAVORITE_BOX_COLOR -> if (configurator.setFavoriteColor(stack, target, color, false)) null else ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            Action.CONFIGURATOR_NAME -> if (configurator.setSettings(stack, name)) null else ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            Action.DEFAULT_TEXT_STYLE -> if (runCatching { TextStyle.valueOf(name) }.getOrNull()?.let { configurator.setSettings(stack, null, textStyle = it) } == true) null else ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
            Action.DEFAULT_BOX_STYLE -> if (runCatching { BoxStyle.valueOf(name) }.getOrNull()?.let { configurator.setSettings(stack, null, boxStyle = it) } == true) null else ModText.CONFIGURATOR_HISTORY_REQUEST_REJECTED
        }
        if (result == null) player.inventoryMenu.broadcastChanges() else player.displayClientMessage(result.text, true)
    }

    companion object {
        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
        private val PLACEHOLDER_MODE = ResourceLocation.fromNamespaceAndPath("minecraft", "empty")
        private const val MAX_WIRE_NAME_LENGTH = 256
    }
}
