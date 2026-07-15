package site.siredvin.peripheralworks.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.siredvin.peripheralworks.common.setup.Items;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("TAIL"))
    void hookOnInteractionHand(InteractionHand hand, boolean $$1, CallbackInfo ci) {
        var owner = ((LivingEntity) (Object) this);
        if (owner instanceof ServerPlayer player) {
            var itemStack = player.getItemInHand(hand);
            var configurator = Items.INSTANCE.getULTIMATE_CONFIGURATOR().get();
            if (itemStack.is(configurator)) {
                var activeMode = configurator.getActiveMode(itemStack);
                if (activeMode != null) {
                    activeMode.getFirst().onSwing(activeMode.getSecond(), itemStack, player);
                }
            }
        }
    }
}
