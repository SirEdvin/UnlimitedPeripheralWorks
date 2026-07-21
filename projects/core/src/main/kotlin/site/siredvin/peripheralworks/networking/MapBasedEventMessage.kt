package site.siredvin.peripheralworks.networking

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dan200.computercraft.shared.computer.menu.ComputerMenu
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.inventory.AbstractContainerMenu
import java.lang.reflect.Type

class MapBasedEventMessage : ComputerServerMessage {

    companion object {
        var type: Type = object : TypeToken<MutableMap<String, Any>>() {}.type
        val GSON = Gson()
        val NAMES = listOf("emi_recipe_paste", "emi_ingredient_paste")
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, MapBasedEventMessage> = StreamCodec.ofMember(
            MapBasedEventMessage::write,
            { buf -> MapBasedEventMessage(buf) },
        )
    }

    private val data: Map<String, Any>
    private val name: Int

    constructor(menu: AbstractContainerMenu, name: String, data: Map<String, Any>) : super(menu) {
        this.name = NAMES.indexOf(name)
        this.data = data
        if (this.name == -1) {
            throw IllegalArgumentException("How is this possible? $name not found")
        }
    }

    constructor(buf: FriendlyByteBuf) : super(buf) {
        this.name = buf.readInt()
        this.data = GSON.fromJson(buf.readUtf(), type)
    }

    override fun handle(
        context: ServerNetworkContext,
        container: ComputerMenu,
    ) {
        container.computer.queueEvent(NAMES[this.name], arrayOf(this.data))
    }

    override fun type(): MessageType<*> = NetworkMessages.GENERIC_EVENT

    override fun write(buf: FriendlyByteBuf) {
        super.write(buf)
        buf.writeInt(this.name)
        buf.writeUtf(GSON.toJson(this.data))
    }
}
