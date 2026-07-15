package site.siredvin.peripheralworks.client.geometry

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader

object FlexibleStatueGeometryLoader : IGeometryLoader<FlexibleStatueGeometry> {
    override fun read(
        jsonObject: JsonObject,
        deserializationContext: JsonDeserializationContext,
    ): FlexibleStatueGeometry = FlexibleStatueGeometry
}
