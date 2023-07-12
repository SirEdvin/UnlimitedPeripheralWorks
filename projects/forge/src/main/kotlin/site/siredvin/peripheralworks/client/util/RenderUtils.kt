package site.siredvin.peripheralworks.client.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.mojang.math.Transformation
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.concurrent.TimeUnit

object RenderUtils {
    val BLOCK_TRANSFORMS by lazy {
        val thirdPerson = ItemTransform(
            Vector3f(75f, 45f, 0f),
            Vector3f(0f, 2.5f, 0f),
            Vector3f(0.375f),
        )
        val firstPersonRight = ItemTransform(
            Vector3f(0f, 45f, 0f),
            Vector3f(),
            Vector3f(0.4f),
        )
        val firstPersonLeft = ItemTransform(
            Vector3f(0f, 225f, 0f),
            Vector3f(),
            Vector3f(0.4f),
        )
        val ground = ItemTransform(
            Vector3f(),
            Vector3f(0f, 3f, 0f),
            Vector3f(0.5f),
        )
        val gui = ItemTransform(
            Vector3f(30f, 225f, 0f),
            Vector3f(),
            Vector3f(0.625f),
        )
        val head = ItemTransform(
            Vector3f(0f, 180f, 0f),
            Vector3f(0f, 13f, 7f),
            Vector3f(),
        )
        val fixed = ItemTransform(
            Vector3f(),
            Vector3f(),
            Vector3f(0.5f),
        )
        ItemTransforms(
            thirdPerson,
            thirdPerson,
            firstPersonLeft,
            firstPersonRight,
            head,
            gui,
            ground,
            fixed,
        )
    }

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
