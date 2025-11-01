package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.common.blockentity.InformativeRegistryBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.jvm.optionals.getOrNull

class InformativeRegistryPeripheral(
    blockEntity: InformativeRegistryBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<InformativeRegistryBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        val TYPE = "informative_registry"
        private val EXTRACTORS = mutableMapOf<String, Function<Level, MethodResult>>()
        private val DESCRIPTORS = mutableMapOf<String, BiFunction<Level, String, MethodResult>>()
        private val LIST_DESCRIPTIONS = mutableMapOf<String, String>()

        fun addList(name: String, description: String, extractor: Function<Level, MethodResult>, descriptor: BiFunction<Level, String, MethodResult>) {
            LIST_DESCRIPTIONS[name] = description
            DESCRIPTORS[name] = descriptor
            EXTRACTORS[name] = extractor
        }

        fun <T> addTagList(name: String, description: String, key: ResourceKey<Registry<T>>) {
            addList(
                name,
                description,
                {
                    MethodResult.of(it.registryAccess().registryOrThrow(key).tagNames.map { x -> x.location.toString() }.toList())
                },
                { level, it ->
                    val registry = level.registryAccess().registryOrThrow(key)
                    val tagID = ResourceLocation.tryParse(it) ?: return@addList MethodResult.of(null)
                    val holder = level.registryAccess().registryOrThrow(key).getTag(TagKey.create(key, tagID)).getOrNull() ?: return@addList MethodResult.of(null)
                    return@addList MethodResult.of(holder.stream().map { x -> registry.getKey(x.value()!!).toString() }.filter { x -> x != null }.toList())
                },
            )
        }

        init {
            addTagList("itemTags", "Item tags", Registries.ITEM)
            addTagList("blockTags", "Block tags", Registries.BLOCK)
            addTagList("entityTypeTags", "Entity type tags", Registries.ENTITY_TYPE)
            addTagList("fluidTags", "Fluid tags", Registries.FLUID)
            addList(
                "mods",
                "Minecraft mods",
                {
                    MethodResult.of(ModPlatform.modList.filter { !PeripheralWorksConfig.informativeRegistryModBlocklist.contains(it) })
                },
                { _level, it ->
                    if (PeripheralWorksConfig.informativeRegistryModBlocklist.contains(it)) {
                        return@addList MethodResult.of(null)
                    }
                    MethodResult.of(ModPlatform.getModInformation(it))
                },
            )
            addList(
                "entity",
                "Minecraft living entities",
                {
                    MethodResult.of(
                        PlatformRegistries.ENTITY_TYPES.keySet().filter {
                            PlatformRegistries.ENTITY_TYPES.get(it)?.baseClass?.isAssignableFrom(LivingEntity::class.java) ?: false
                        }.map(ResourceLocation::toString),
                    )
                },
                { _level, it ->
                    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
                    val entityType = PlatformRegistries.ENTITY_TYPES.get(ResourceLocation(it))
                    val data: MutableMap<String, Any> = mutableMapOf()
                    data["category"] = entityType.category.name
                    data["type"] = entityType.description.string
                    data["defaultLootTable"] = entityType.defaultLootTable.toString()
                    data["registryID"] = PlatformRegistries.ENTITY_TYPES.getId(entityType)
                    return@addList MethodResult.of(data)
                },
            )
            addList(
                "item",
                "Minecraft items",
                {
                    MethodResult.of(PlatformRegistries.ITEMS.keySet().map(ResourceLocation::toString))
                },
                { _level, it ->
                    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
                    val item = PlatformRegistries.ITEMS.get(ResourceLocation(it))
                    if (item == net.minecraft.world.item.Items.AIR) {
                        return@addList MethodResult.of(null)
                    }
                    val base = LuaRepresentation.forItem(item)
                    base["registryID"] = PlatformRegistries.ITEMS.getId(item)
                    return@addList MethodResult.of(base)
                },
            )

            addList(
                "block",
                "Minecraft blocks",
                {
                    MethodResult.of(PlatformRegistries.BLOCKS.keySet().map(ResourceLocation::toString))
                },
                { _level, it ->
                    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
                    val blockState = PlatformRegistries.BLOCKS.get(ResourceLocation(it)).defaultBlockState()
                    if (blockState.`is`(Blocks.AIR)) {
                        return@addList MethodResult.of(null)
                    }
                    val base = LuaRepresentation.forBlockState(blockState)
                    base["registryID"] = PlatformRegistries.BLOCKS.getId(blockState.block)
                    return@addList MethodResult.of(base)
                },
            )

            addList(
                "fluid",
                "Minecraft fluids",
                {
                    MethodResult.of(PlatformRegistries.FLUIDS.keySet().map(ResourceLocation::toString))
                },
                { _level, it ->
                    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
                    val fluid = PlatformRegistries.FLUIDS.get(ResourceLocation(it))
                    if (fluid == Fluids.EMPTY) {
                        return@addList MethodResult.of(null)
                    }
                    val base = LuaRepresentation.forFluid(fluid)
                    base["registryID"] = PlatformRegistries.FLUIDS.getId(fluid)
                    return@addList MethodResult.of(base)
                },
            )
            addList(
                "list",
                "Lists of all possible lists",
                {
                    MethodResult.of(LIST_DESCRIPTIONS.keys)
                },
                { _level, it ->
                    MethodResult.of(LIST_DESCRIPTIONS[it])
                },
            )
        }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableInformativeRegistry

    @LuaFunction
    fun list(target: String): MethodResult {
        val extractor = EXTRACTORS[target] ?: throw LuaException("Cannot list $target, there is not function for it")
        return extractor.apply(peripheralOwner.level!!)
    }

    @LuaFunction
    fun describe(target: String, id: String): MethodResult {
        val descriptor = DESCRIPTORS[target] ?: throw LuaException("Cannot describe $target, there is not function for it")
        return descriptor.apply(peripheralOwner.level!!, id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InformativeRegistryPeripheral) return false
        if (!super.equals(other)) return false

        if (isEnabled != other.isEnabled) return false
        if (peripheralOwner != other.peripheralOwner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isEnabled.hashCode()
        result = 31 * result + peripheralOwner.hashCode()
        return result
    }
}
