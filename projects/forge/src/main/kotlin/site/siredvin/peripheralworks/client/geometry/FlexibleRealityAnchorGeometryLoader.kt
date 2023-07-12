package site.siredvin.peripheralworks.client.geometry

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import net.minecraftforge.client.model.geometry.IGeometryLoader

object FlexibleRealityAnchorGeometryLoader : IGeometryLoader<FlexibleRealityAnchorGeometry> {
    override fun read(
        jsonObject: JsonObject,
        deserializationContext: JsonDeserializationContext,
    ): FlexibleRealityAnchorGeometry {
        return FlexibleRealityAnchorGeometry
    }
}
