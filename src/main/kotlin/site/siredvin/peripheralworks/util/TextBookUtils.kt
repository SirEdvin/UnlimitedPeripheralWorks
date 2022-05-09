package site.siredvin.peripheralworks.util

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object TextBookUtils {
    private const val BOOK_MAX_LINES = 14
    private const val MAX_WIDTH_PER_LINE = 95

    private fun resolvePage(string: String): String {
        var component: Component?
        try {
            component  = Component.Serializer.fromJsonLenient(string)
            if (component == null)
                throw IllegalArgumentException("Cannot parse it, fallback")
            component = ComponentUtils.updateForEntity(null, component, null, 0)
        } catch (var5: Exception) {
            component = TextComponent(string)
        }
        return component!!.string
    }

    fun getBookText(book: ItemStack): List<String> {
        return when (book.item) {
            Items.WRITABLE_BOOK -> {
                val pagesData = book.tag?.getList("pages", 8) ?: return emptyList()
                val pages: MutableList<String> = mutableListOf()
                for (i in 0 until pagesData.size)
                    pages.add(pagesData.getString(i))
                return pages
            }
            Items.WRITTEN_BOOK -> {
                val pagesData = book.tag?.getList("pages", 8) ?: return emptyList()
                val pages: MutableList<String> = mutableListOf()
                for (i in 0 until pagesData.size)
                    pages.add(resolvePage(pagesData.getString(i)))
                return pages
            }
            else -> emptyList()
        }
    }

    fun getCharacterWidth(c: Char): Int {
        return when(c) {
            ' ', '!', '\'', ',', '.', ':', ';', 'i', '|' -> 1
            '`', 'l' -> 2
            '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}' -> 3
            '<', '>', 'f', 'k' -> 4
            '@', '~' -> 6
            else -> 5
        }
    }

    fun stripText(text: String): String {
        var lineCounter = 0
        var currentWidth = 0
        var skipNext = false
        val buffer = StringBuffer()
        for (c in text) {
            if (skipNext) {
                skipNext = false
                continue
            }
            when (c) {
                '\n' -> {
                    currentWidth = 0
                    lineCounter++
                    if (lineCounter > BOOK_MAX_LINES)
                        return buffer.toString()
                }
                '§' -> skipNext = true
                else -> {
                    val nextWidth = getCharacterWidth(c)
                    if (currentWidth + nextWidth > MAX_WIDTH_PER_LINE) {
                        lineCounter++
                        currentWidth = nextWidth
                        if (lineCounter > BOOK_MAX_LINES)
                            return buffer.toString()
                    } else {
                        currentWidth += nextWidth
                    }
                }
            }
            buffer.append(c)
        }
        return buffer.toString()
    }
}