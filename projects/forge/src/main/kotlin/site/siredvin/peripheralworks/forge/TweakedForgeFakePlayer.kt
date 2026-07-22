package site.siredvin.peripheralworks.forge

import com.mojang.authlib.GameProfile
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import site.siredvin.tweakium.modules.player.ForgeFakePlayer

class TweakedForgeFakePlayer(level: ServerLevel, profile: GameProfile, val originalPlayer: Player?) : ForgeFakePlayer(level, profile)
