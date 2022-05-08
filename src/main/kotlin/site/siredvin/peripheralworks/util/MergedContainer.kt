package site.siredvin.peripheralworks.util

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class MergedContainer(private val containers: List<Container>): Container {

    private fun fromIndex(index: Int): Pair<Container, Int>? {
        var passedCounter = 0
        for (container in containers) {
            if (index < container.containerSize + passedCounter) {
                return Pair(container, index - passedCounter)
            } else {
                passedCounter += container.containerSize
            }
        }
        return null
    }

    override fun clearContent() {
        containers.forEach { it.clearContent() }
    }

    override fun getContainerSize(): Int {
        return containers.sumOf { it.containerSize }
    }

    override fun isEmpty(): Boolean {
        return containers.all { it.isEmpty }
    }

    override fun getItem(i: Int): ItemStack {
        val access = fromIndex(i) ?: return ItemStack.EMPTY
        return access.first.getItem(access.second)
    }

    override fun removeItem(i: Int, j: Int): ItemStack {
        val access = fromIndex(i) ?: return ItemStack.EMPTY
        return access.first.removeItem(access.second, j)
    }

    override fun removeItemNoUpdate(i: Int): ItemStack {
        val access = fromIndex(i) ?: return ItemStack.EMPTY
        return access.first.removeItemNoUpdate(access.second)
    }

    override fun setItem(i: Int, itemStack: ItemStack) {
        val access = fromIndex(i)
        access?.first?.setItem(access.second, itemStack)
    }

    override fun setChanged() {
        containers.forEach { it.setChanged() }
    }

    override fun stillValid(player: Player): Boolean {
        return containers.all { it.stillValid(player) }
    }
}