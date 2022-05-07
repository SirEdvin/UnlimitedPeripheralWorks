package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.level.block.entity.LecternBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.assertBetween


class LecternPlugin(private val target: LecternBlockEntity): IPeripheralPlugin {

    override val additionalType: String
        get() = "lectern"

    private fun assertBook() {
        if (!target.hasBook())
            throw LuaException("Insert book first")
    }

    @LuaFunction(mainThread = true)
    fun getPageCount(): Int {
        assertBook()
        return target.pageCount
    }

    @LuaFunction(mainThread = true)
    fun hasBook(): Boolean {
        return target.hasBook()
    }

    @LuaFunction(mainThread = true)
    fun getPage(): Int {
        assertBook()
        return target.page
    }

    @LuaFunction(mainThread = true)
    fun setPage(page: Int) {
        assertBook()
        assertBetween(page, 1, 15, "page")
        target.page = page - 1
    }

    @LuaFunction(mainThread = true)
    fun getText(): String {
        assertBook()
        val pages = target.book.tag?.getList("pages", 8)
        return pages?.getString(target.page) ?: ""
    }
}