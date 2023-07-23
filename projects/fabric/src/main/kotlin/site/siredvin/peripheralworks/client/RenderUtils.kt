package site.siredvin.peripheralworks.client

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.mojang.math.Transformation
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Quaternionf
import java.util.concurrent.TimeUnit

object RenderUtils {

    @Suppress("DEPRECATION")
    val TEXTURE_ATLAS by lazy {
        Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
    }

    private val TEXTURE_CACHE = CacheBuilder.newBuilder()
        .concurrencyLevel(1).expireAfterAccess(2, TimeUnit.MINUTES)
        .build(CacheLoader.from(::getTextureRaw))

    private val TRANSFORMATION_CACHE = CacheBuilder.newBuilder()
        .concurrencyLevel(1).expireAfterAccess(2, TimeUnit.MINUTES)
        .build(CacheLoader.from(::buildTransformation))

    private val MODEL_STATE_CACHE = CacheBuilder.newBuilder()
        .concurrencyLevel(1).expireAfterAccess(2, TimeUnit.MINUTES)
        .build(CacheLoader.from(::buildModelState))

    private fun buildTransformation(rotation: Quaternionf): Transformation {
        return Transformation(Matrix4f().rotate(rotation))
    }

    private fun buildModelState(transformation: Transformation): ModelState {
        return object : ModelState {
            override fun getRotation(): Transformation {
                return transformation
            }
        }
    }

    private fun getTextureRaw(id: ResourceLocation): TextureAtlasSprite {
        return TEXTURE_ATLAS.apply(id)
    }

    fun getTexture(id: ResourceLocation): TextureAtlasSprite {
        return TEXTURE_CACHE[id]
    }

    fun getModelState(rotation: Quaternionf): ModelState {
        return MODEL_STATE_CACHE.get(TRANSFORMATION_CACHE.get(rotation))
    }
}
