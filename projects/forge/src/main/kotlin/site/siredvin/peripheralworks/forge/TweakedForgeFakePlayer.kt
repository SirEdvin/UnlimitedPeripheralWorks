package site.siredvin.peripheralworks.forge

import com.mojang.authlib.GameProfile
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import site.siredvin.tweakium.modules.player.ForgeFakePlayer

class TweakedForgeFakePlayer(level: ServerLevel, profile: GameProfile, val originalPlayer: Player?) : ForgeFakePlayer(level, profile) {
    override fun <T> getCapability(cap: Capability<T>): LazyOptional<T> {
        if (originalPlayer == null) {
            return super.getCapability(cap)
        }
        return originalPlayer.getCapability(cap)
    }

    override fun <T> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T> {
        if (originalPlayer == null) {
            return super.getCapability(capability, facing)
        }
        return originalPlayer.getCapability(capability, facing)
    }
}
