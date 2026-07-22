package site.siredvin.peripheralworks.mixins;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import site.siredvin.peripheralworks.computercraft.peripherals.TweakedTurtlePeripheral;

@Mixin(TurtleBlockEntity.class)
public class CursedTurtleMixin {
    @Shadow(remap = false)
    private IPeripheral peripheral;

    @Shadow(remap = false)
    boolean hasMoved() {
        return false;
    }

    public IPeripheral peripheral() {
        if (hasMoved()) return null;
        if (peripheral != null) return peripheral;
        return peripheral = new TweakedTurtlePeripheral((TurtleBlockEntity)(Object)this);
    }
}
