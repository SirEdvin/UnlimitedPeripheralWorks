package site.siredvin.peripheralworks.xplat

import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks

interface ModRecipeIngredients {

    companion object {
        private var impl: ModRecipeIngredients? = null

        fun configure(impl: ModRecipeIngredients) {
            this.impl = impl
        }

        fun get(): ModRecipeIngredients {
            if (impl == null) {
                throw IllegalStateException("You should init PeripheralWorks Platform first")
            }
            return impl!!
        }
    }

    val daylightDetector: Ingredient
        get() = Ingredient.of(Blocks.DAYLIGHT_DETECTOR.asItem())

    val blackstone: Ingredient
        get() = Ingredient.of(Blocks.BLACKSTONE.asItem())

    val enderModem: Ingredient
    val printer: Ingredient
    val peripheralium: Ingredient
    val peripheraliumUpgrade: Ingredient
    val netheriteIngot: Ingredient
    val emerald: Ingredient
    val diamond: Ingredient
    val ironIngot: Ingredient
    val peripheraliumBlock: Ingredient
    val observer: Ingredient
    val smoothStone: Ingredient
    val smoothBasalt: Ingredient
    val smoothStoneSlab: Ingredient
    val compass: Ingredient
    val stick: Ingredient
    val blueDye: Ingredient
    val craftingTable: Ingredient
    val bookshelf: Ingredient
    val book: Ingredient
}
