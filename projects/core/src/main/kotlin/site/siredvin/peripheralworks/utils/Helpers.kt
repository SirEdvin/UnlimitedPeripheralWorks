package site.siredvin.peripheralworks.utils

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore


fun modId(text: String): ResourceLocation {
    return ResourceLocation(PeripheralWorksCore.MOD_ID, text)
}


// argument tricks
@Throws(LuaException::class)
fun getResourceLocation(arguments: IArguments, index: Int): ResourceLocation {
    return toResourceLocation(arguments.getString(index))
}

@Throws(LuaException::class)
fun toResourceLocation(value: String): ResourceLocation {
    return try {
        ResourceLocation(value)
    } catch (e: ResourceLocationException) {
        throw LuaException(e.message)
    }
}