package site.siredvin.peripheralworks.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.siredvin.peripheralworks.computercraft.plugins.specific.LecternPlugin;

@Mixin(LecternBlockEntity.class)
public class LecternMixin {
    @Inject(method = "setPage", at = @At("TAIL"))
    public void setPage(int i, CallbackInfo ci) {
        LecternPlugin.Companion.sendEvent(((LecternBlockEntity)(Object)this).getBlockPos(), "lectern_page_changed", i);
    }

    @Inject(method = "setBook(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    public void setBook(ItemStack itemStack, Player player, CallbackInfo ci) {
        if (player != null) {
            LecternPlugin.Companion.sendEvent(
                    ((LecternBlockEntity) (Object) this).getBlockPos(),
                    "lectern_book_changed",
                    itemStack.getHoverName().getString(),
                    player.getName().getString()
            );
        } else {
            LecternPlugin.Companion.sendEvent(
                    ((LecternBlockEntity) (Object) this).getBlockPos(),
                    "lectern_book_changed",
                    itemStack.getHoverName().getString()
            );
        }
    }

    @Inject(method = "onBookItemRemove", at = @At("TAIL"))
    public void onBookItemRemove(CallbackInfo ci) {
        LecternPlugin.Companion.sendEvent(((LecternBlockEntity)(Object)this).getBlockPos(), "lectern_book_removed");
    }
}