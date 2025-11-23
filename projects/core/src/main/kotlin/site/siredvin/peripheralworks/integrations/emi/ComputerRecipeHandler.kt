package site.siredvin.peripheralworks.integrations.emi

import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu
import dev.emi.emi.api.recipe.EmiPlayerInventory
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.handler.EmiCraftContext
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.MapBasedEventMessage

class ComputerRecipeHandler<T : AbstractComputerMenu> : EmiRecipeHandler<T> {
    override fun getInventory(p0: AbstractContainerScreen<T>): EmiPlayerInventory = EmiPlayerInventory(emptyList())

    override fun supportsRecipe(p0: EmiRecipe): Boolean = true

    override fun canCraft(
        p0: EmiRecipe,
        p1: EmiCraftContext<T>,
    ): Boolean = true

    override fun craft(
        p0: EmiRecipe,
        p1: EmiCraftContext<T>,
    ): Boolean {
        ClientNetworking.sendToServer(
            MapBasedEventMessage(
                p1.screenHandler,
                "emi_recipe_paste",
                CommonEntrypoint.mapRecipe(
                    p0,
                    Minecraft.getInstance().level!!.registryAccess(),
                ),
            ),
        )
        return true
    }
}
