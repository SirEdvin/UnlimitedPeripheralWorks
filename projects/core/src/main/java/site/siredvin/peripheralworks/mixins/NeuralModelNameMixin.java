package site.siredvin.peripheralworks.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import site.siredvin.peripheralworks.computercraft.NeuralModelNameGuard;

@Mixin(ItemStack.class)
public class NeuralModelNameMixin {
    @Redirect(method = "getHoverName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;"))
    private Component peripheralworks$modelName(Item item, ItemStack stack) {
        // CC:Tweaked reads the native display name before additional detail providers run.
        return NeuralModelNameGuard.nativeName(stack);
    }
}
