import dan200.computercraft.shared.util.NBTUtil.getNBTHash
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import site.siredvin.peripheralium.tests.WithMinecraft
import kotlin.test.assertEquals

@WithMinecraft
internal class NBTests {

    @RepeatedTest(3)
    fun testCompoundTagSorting() {
        val nbt1 = makeCompoundTag(false)
        val hash1 = getNBTHash(nbt1)
        val nbt2 = makeCompoundTag(true)
        val hash2 = getNBTHash(nbt2)
        assertNotNull(hash1, "NBT hash should not be null")
        assertNotNull(hash2, "NBT hash should not be null")
        assertEquals(hash1, hash2, "NBT hashes should be equal")
    }

    @RepeatedTest(3)
    fun testListTagSorting() {
        val nbt1 = CompoundTag()
        nbt1.put("Items", makeListTag(false))
        val hash1 = getNBTHash(nbt1)
        val nbt2 = CompoundTag()
        nbt2.put("Items", makeListTag(true))
        val hash2 = getNBTHash(nbt2)
        assertNotNull(hash1, "NBT hash should not be null")
        assertNotNull(hash2, "NBT hash should not be null")
        assertEquals(hash1, hash2, "NBT hashes should be equal")
    }

    @Test
    fun testTagsHaveDifferentOrders() {
        val nbt1 = makeCompoundTag(false)
        val nbt2 = makeCompoundTag(true)
        assertNotEquals(
            nbt1.allKeys.toList(),
            nbt2.allKeys.toList(),
            "Expected makeCompoundTag to return keys with different orders.",
        )
    }

    private fun makeCompoundTag(grow: Boolean): CompoundTag {
        val nbt = CompoundTag()
        nbt.putString("Slot", "Slot 1")
        nbt.putString("Count", "64")
        nbt.putString("id", "123")

        // Grow the map to cause a rehash and (hopefully) change the order around a little bit.
        if (grow) {
            for (i in 0..63) nbt.putBoolean("x$i", true)
            for (i in 0..63) nbt.remove("x$i")
        }
        return nbt
    }

    private fun makeListTag(reverse: Boolean): ListTag {
        val list = ListTag()
        for (i in 0..2) {
            list.add(makeCompoundTag(reverse))
        }
        return list
    }
}
