package site.siredvin.peripheralworks.client.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.collect.ImmutableMap
import com.mojang.math.Transformation
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.concurrent.TimeUnit

object RenderUtils {
    private fun makeTransform(
        rotationX: Float,
        rotationY: Float,
        rotationZ: Float,
        translationX: Float,
        translationY: Float,
        translationZ: Float,
        scaleX: Float,
        scaleY: Float,
        scaleZ: Float,
    ): ItemTransform {
        val translation = Vector3f(translationX, translationY, translationZ)
        translation.mul(0.0625f)
        translation[Mth.clamp(translation.x, -5.0f, 5.0f), Mth.clamp(translation.y, -5.0f, 5.0f)] =
            Mth.clamp(translation.z, -5.0f, 5.0f)
        return ItemTransform(Vector3f(rotationX, rotationY, rotationZ), translation, Vector3f(scaleX, scaleY, scaleZ))
    }

    val TRANSFORM_BLOCK_GUI = makeTransform(30f, 225f, 0f, 0f, 0f, 0f, 0.625f, 0.625f, 0.625f)
    val TRANSFORM_BLOCK_GROUND = makeTransform(0f, 0f, 0f, 0f, 3f, 0f, 0.25f, 0.25f, 0.25f)
    val TRANSFORM_BLOCK_FIXED = makeTransform(0f, 0f, 0f, 0f, 0f, 0f, 0.5f, 0.5f, 0.5f)
    val TRANSFORM_BLOCK_3RD_PERSON_RIGHT = makeTransform(75f, 45f, 0f, 0f, 2.5f, 0f, 0.375f, 0.375f, 0.375f)
    val TRANSFORM_BLOCK_1ST_PERSON_RIGHT = makeTransform(0f, 45f, 0f, 0f, 0f, 0f, 0.4f, 0.4f, 0.4f)
    val TRANSFORM_BLOCK_1ST_PERSON_LEFT = makeTransform(0f, 225f, 0f, 0f, 0f, 0f, 0.4f, 0.4f, 0.4f)

    /**
     * Mimics the vanilla model transformation used for most vanilla blocks,
     * and should be suitable for most custom block-like models.
     */
    val MODEL_TRANSFORM_BLOCK = ItemTransforms(
        TRANSFORM_BLOCK_3RD_PERSON_RIGHT,
        TRANSFORM_BLOCK_3RD_PERSON_RIGHT,
        TRANSFORM_BLOCK_1ST_PERSON_LEFT,
        TRANSFORM_BLOCK_1ST_PERSON_RIGHT,
        ItemTransform.NO_TRANSFORM,
        TRANSFORM_BLOCK_GUI,
        TRANSFORM_BLOCK_GROUND,
        TRANSFORM_BLOCK_FIXED,
        ImmutableMap.of()
    )

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
