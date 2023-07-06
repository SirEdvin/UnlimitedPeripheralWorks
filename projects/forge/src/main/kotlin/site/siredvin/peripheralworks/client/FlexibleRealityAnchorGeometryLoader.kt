package site.siredvin.peripheralworks.client

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import net.minecraftforge.client.model.geometry.IGeometryLoader

class FlexibleRealityAnchorGeometryLoader : IGeometryLoader<FlexibleRealityAnchorGeometry> {
    override fun read(
        jsonObject: JsonObject,
        deserializationContext: JsonDeserializationContext,
    ): FlexibleRealityAnchorGeometry {
        return FlexibleRealityAnchorGeometry()
    }
}
