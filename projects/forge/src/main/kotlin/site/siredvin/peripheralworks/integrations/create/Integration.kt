package site.siredvin.peripheralworks.integrations.create

import com.google.gson.JsonSyntaxException
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.simibubi.create.AllDataComponents
import com.simibubi.create.AllItems
import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity
import com.simibubi.create.content.processing.recipe.ProcessingOutput
import com.simibubi.create.content.processing.recipe.ProcessingRecipe
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour
import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.broccolium.modules.storage.fluid.ForgeAgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit.GSON
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import java.util.function.Function
import kotlin.collections.set

class Integration : Runnable {

    object FilteringBehaviourPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create_filter"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!Configuration.enableCreateIntegration || blockEntity == null) return null
            if (blockEntity is SmartBlockEntity) {
                val behavior = blockEntity.getBehaviour(FilteringBehaviour.TYPE)
                if (behavior != null) {
                    return CreateFilterableBehaviorPeripheralPlugin(blockEntity, behavior)
                }
            }
            return null
        }
    }

    object ScrollingBehaviourPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create_scroll"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!Configuration.enableCreateIntegration || blockEntity == null) return null
            if (blockEntity is SmartBlockEntity) {
                val behaviour = blockEntity.getBehaviour(ScrollOptionBehaviour.TYPE)
                if (behaviour != null) {
                    return CreateScrollOptionPeripheralPlugin(blockEntity, behaviour)
                }
            }
            return null
        }
    }

    object CreatePluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!Configuration.enableCreateIntegration || blockEntity == null) return null

            if (blockEntity is SmartBlockEntity) {
                if (blockEntity is LinearActuatorBlockEntity) {
                    return CreateLinearActuatorPeripheralPlugin(blockEntity)
                } else {
                    val filterBehavior = blockEntity.getBehaviour(FilteringBehaviour.TYPE)
                    if (filterBehavior != null) {
                        return CreateFilterableBehaviorPeripheralPlugin(blockEntity, filterBehavior)
                    }
                }
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(CreatePluginProvider)
        ComputerCraftProxy.addProvider(FilteringBehaviourPluginProvider)
        ComputerCraftProxy.addProvider(ScrollingBehaviourPluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        AgnosticItemStorageLookup.addEntityLookup { level, entity, direction ->
            if (entity is AbstractContraptionEntity) {
                return@addEntityLookup AgnosticItemHandlerWrapper(entity.contraption.storage.allItems)
            }
            return@addEntityLookup null
        }
        AgnosticFluidStorageLookup.addEntityLookup { level, entity, direction ->
            if (entity is AbstractContraptionEntity) {
                return@addEntityLookup ForgeAgnosticFluidStorage(entity.contraption.storage.fluids)
            }
            return@addEntityLookup null
        }
        EntityCard.EXTRA_SEARCHES[AllItems.WRENCH.get()] = Function<Player, Entity?> {
            val box = it.boundingBox.inflate(2.0)
            it.level().getEntitiesOfClass(AbstractContraptionEntity::class.java, box).firstOrNull()
        }

        @Suppress("UNCHECKED_CAST")
        RecipeRegistryToolkit.registerRecipeSerializer(
            ProcessingRecipe::class.java as Class<ProcessingRecipe<RecipeInput, com.simibubi.create.content.processing.recipe.ProcessingRecipeParams>>,
            CreateProcessingRecipeTransformer(),
        )

        RecipeRegistryToolkit.registerRecipeSerializer(
            SequencedAssemblyRecipe::class.java,
            CreateSequenceRecipeTransformer(),
        )

        fun <T> serializeCodec(codec: Codec<T>, value: T): Any? {
            try {
                return GSON.fromJson(codec.encodeStart(JsonOps.INSTANCE, value).result().get(), HashMap::class.java)
            } catch (e: JsonSyntaxException) {
                return GSON.fromJson(codec.encodeStart(JsonOps.INSTANCE, value).result().get(), ArrayList::class.java)
            }
        }
        RecipeRegistryToolkit.registerSerializer(SizedFluidIngredient::class.java) { serializeCodec(SizedFluidIngredient.FLAT_CODEC, it) }
        RecipeRegistryToolkit.registerSerializer(FluidStack::class.java) { serializeCodec(FluidStack.CODEC, it) }
        RecipeRegistryToolkit.registerSerializer(ProcessingOutput::class.java) { serializeCodec(ProcessingOutput.CODEC_NEW, it) }
        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                stack.get(AllDataComponents.SEQUENCED_ASSEMBLY)?.let {
                    data["SequencedAssembly"] = serializeCodec(SequencedAssemblyRecipe.SequencedAssembly.CODEC, it)
                }
            },
        )
    }
}
