package site.siredvin.peripheralworks.subsystem.recipe

interface RecipeSerializableRecord {
    fun serializeForToolkit(): Map<String, Any>
}
