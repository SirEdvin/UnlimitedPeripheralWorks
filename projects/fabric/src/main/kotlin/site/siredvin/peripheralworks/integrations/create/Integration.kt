package site.siredvin.peripheralworks.integrations.create

import com.google.gson.JsonSyntaxException
import com.simibubi.create.AllItems
import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity
import com.simibubi.create.content.processing.recipe.ProcessingOutput
import com.simibubi.create.content.processing.recipe.ProcessingRecipe
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour
import com.simibubi.create.foundation.fluid.FluidIngredient
import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import dan200.computercraft.shared.util.NBTUtil
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.broccolium.modules.storage.fluid.FabricAgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.FabricStorageWrapper
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.CreateConfiguration
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit.GSON
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import java.util.function.Function

class Integration : Runnable {

    object FilteringBehaviourPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "create_filter"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!CreateConfiguration.enableCreateIntegration || blockEntity == null) return null
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
            if (!CreateConfiguration.enableCreateIntegration || blockEntity == null) return null
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
            if (!CreateConfiguration.enableCreateIntegration || blockEntity == null) return null

            if (blockEntity is SmartBlockEntity) {
                if (blockEntity is BlazeBurnerBlockEntity) {
                    return CreateBlazeBurnerPeripheralPlugin(blockEntity)
                }
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
        AgnosticItemStorageLookup.addEntityLookup { level, entity, direction ->
            if (entity is AbstractContraptionEntity) {
                return@addEntityLookup FabricStorageWrapper(entity.contraption.sharedInventory)
            }
            return@addEntityLookup null
        }
        AgnosticFluidStorageLookup.addEntityLookup { level, entity, direction ->
            if (entity is AbstractContraptionEntity) {
                return@addEntityLookup FabricAgnosticFluidStorage(entity.contraption.sharedFluidTanks)
            }
            return@addEntityLookup null
        }
        EntityCard.EXTRA_SEARCHES[AllItems.WRENCH.get()] = Function<Player, Entity?> {
            val box = it.boundingBox.inflate(2.0)
            it.level().getEntitiesOfClass(AbstractContraptionEntity::class.java, box).firstOrNull()
        }

        @Suppress("UNCHECKED_CAST")
        RecipeRegistryToolkit.registerRecipeSerializer(
            ProcessingRecipe::class.java as Class<ProcessingRecipe<Container>>,
            CreateProcessingRecipeTransformer(),
        )

        RecipeRegistryToolkit.registerRecipeSerializer(
            SequencedAssemblyRecipe::class.java,
            CreateSequenceRecipeTransformer(),
        )

        RecipeRegistryToolkit.registerSerializer(FluidIngredient::class.java) {
            try {
                return@registerSerializer GSON.fromJson(it.serialize(), HashMap::class.java)
            } catch (ignored: JsonSyntaxException) {
                try {
                    return@registerSerializer GSON.fromJson(it.serialize(), ArrayList::class.java)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
            return@registerSerializer null
        }
        RecipeRegistryToolkit.registerSerializer(ProcessingOutput::class.java) {
            try {
                return@registerSerializer GSON.fromJson(it.serialize(), HashMap::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
            return@registerSerializer null
        }
        RecipeRegistryToolkit.registerSerializer(FluidStack::class.java) {
            return@registerSerializer LuaRepresentation.forFluidStack(AgnosticFluidStack(it.fluid, it.amount.toDouble(), it.tag))
        }

        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                if (stack.tag != null) {
                    if (stack.tag!!.contains("SequencedAssembly")) {
                        data["SequencedAssembly"] = NBTUtil.toLua(stack.tag!!.getCompound("SequencedAssembly"))
                    }
                }
            },
        )
    }
}
