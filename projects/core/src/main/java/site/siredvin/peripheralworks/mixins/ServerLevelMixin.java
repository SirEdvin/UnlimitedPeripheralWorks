package site.siredvin.peripheralworks.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.siredvin.peripheralworks.common.events.BlockStateUpdateEventBus;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(at = @At("HEAD"), method = "onBlockStateChange")
    public void hookOnBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2, CallbackInfo ci) {
        BlockStateUpdateEventBus.INSTANCE.onBlockStateChange(blockPos, blockState, blockState2);
    }
}
