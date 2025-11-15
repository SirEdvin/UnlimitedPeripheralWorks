package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.item.IGTTool
import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import kotlin.collections.set
import kotlin.math.max

class Integration : Runnable {
    override fun run() {
        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                val item = stack.item
                val tag = stack.tag
                if (tag != null && item is IGTTool) {
                    val stats = item.toolStats
                    val gregData = mutableMapOf<String, Any>()
                    val remainingDamage = item.getTotalMaxDurability(stack) - stack.damageValue + 1
                    if (stats.isSuitableForCrafting(stack)) {
                        gregData["craftingUses"] = remainingDamage / max(1, stats.getDamagePerCraftingAction(stack))
                    }
                    gregData["maxUses"] = item.getTotalMaxDurability(stack)
                    gregData["generalUses"] = remainingDamage
                    data["gtceu"] = gregData
                }
            },
        )
    }
}
