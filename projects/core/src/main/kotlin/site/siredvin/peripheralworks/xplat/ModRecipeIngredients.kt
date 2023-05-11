package site.siredvin.peripheralworks.xplat

import net.minecraft.world.item.crafting.Ingredient

interface ModRecipeIngredients {

    companion object {
        private var _IMPL: ModRecipeIngredients? = null

        fun configure(impl: ModRecipeIngredients) {
            _IMPL = impl
        }

        fun get(): ModRecipeIngredients {
            if (_IMPL == null)
                throw IllegalStateException("You should init PeripheralWorks Platform first")
            return _IMPL!!
        }
    }


    val enderModem: Ingredient
    val peripheralium: Ingredient
    val netheriteIngot: Ingredient
    val emerald: Ingredient
    val diamond: Ingredient
}