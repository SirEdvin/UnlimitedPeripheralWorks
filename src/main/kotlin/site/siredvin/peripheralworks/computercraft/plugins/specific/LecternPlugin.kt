package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.nbt.StringTag
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.LecternBlock
import net.minecraft.world.level.block.entity.LecternBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.common.ExtractorProxy
import site.siredvin.peripheralium.util.assertBetween
import site.siredvin.peripheralworks.util.TextBookUtils
import java.util.*
import java.util.function.Predicate


class LecternPlugin(private val target: LecternBlockEntity): IPeripheralPlugin {

    private fun assertBook() {
        if (!target.hasBook())
            throw LuaException("Lectern should contain book")
    }

    private fun assertNoBook() {
        if (target.hasBook())
            throw LuaException("Lectern shouldn't contain book")
    }

    private fun assertEditableBook() {
        if (!isBookEditable())
            throw LuaException("Book should be editable")
    }


    @LuaFunction(mainThread = true)
    fun hasBook(): Boolean {
        return target.hasBook()
    }

    @LuaFunction(mainThread = true)
    fun getPageCount(): Int {
        assertBook()
        return target.pageCount
    }

    @LuaFunction(mainThread = true)
    fun getActivePage(): Int {
        assertBook()
        return target.page
    }

    @LuaFunction(mainThread = true)
    fun setActivePage(page: Int) {
        assertBook()
        assertBetween(page, 1, target.pageCount, "page")
        target.page = page - 1
    }

    @LuaFunction(mainThread = true)
    fun getText(): List<String> {
        assertBook()
        return TextBookUtils.getBookText(target.book)
    }

    @LuaFunction(mainThread = true)
    fun isBookEditable(): Boolean {
        assertBook()
        return target.book.`is`(Items.WRITABLE_BOOK)
    }

    @LuaFunction(mainThread = true)
    fun addPage(text: Optional<String>): MethodResult {
        assertEditableBook()
        val pagesData = target.book.tag!!.getList("pages", 8)
        pagesData.add(StringTag.valueOf(TextBookUtils.stripText(text.orElse(""))))
        target.book.tag!!.put("pages", pagesData)
        target.pageCount += 1
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun removePage(page: Int): MethodResult {
        assertEditableBook()
        assertBetween(page, 1, target.pageCount, "page")
        val pagesData = target.book.tag!!.getList("pages", 8)
        pagesData.removeAt(page - 1)
        target.book.tag!!.put("pages", pagesData)
        target.pageCount -= 1
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun editPage(page: Int, text: String): MethodResult {
        assertEditableBook()
        assertBetween(page, 1, target.pageCount, "page")
        val pagesData = target.book.tag!!.getList("pages", 8)
        pagesData[page - 1] = StringTag.valueOf(TextBookUtils.stripText(text))
        target.book.tag!!.put("pages", pagesData)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun ejectBook(computer: IComputerAccess, toName: String): MethodResult {
        assertBook()

        val location: IPeripheral = computer.getAvailablePeripheral(toName)
            ?: throw LuaException("Target '$toName' does not exist")

        val toStorage = ExtractorProxy.extractItemStorage(target.level!!, location.target)
            ?: throw LuaException("Target '$toName' is not an item inventory")

        Transaction.openOuter().use {
            val amount = toStorage.insert(ItemVariant.of(target.book), 1, it)
            if (amount != 1L) {
                it.abort()
                return MethodResult.of(null, "Not enough space in target inventory")
            }
            target.clearContent()
            target.level!!.setBlockAndUpdate(target.blockPos, target.blockState.setValue(LecternBlock.HAS_BOOK, false))
            it.commit()
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun injectBook(computer: IComputerAccess, fromName: String, bookName: Optional<String>): MethodResult {
        assertNoBook()

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractItemStorage(target.level!!, location.target)
            ?: throw LuaException("Target '$fromName' is not an item inventory")

        var predicate: Predicate<ItemVariant> = Predicate { it.isOf(Items.WRITABLE_BOOK) || it.isOf(Items.WRITTEN_BOOK) }

        if (bookName.isPresent)
            predicate = predicate.and {
                val stack = it.toStack()
                stack.item.getName(stack).string == bookName.get()
            }

        Transaction.openOuter().use {
            val extractionTarget = StorageUtil.findExtractableContent(fromStorage, predicate, it)
                ?: return MethodResult.of(null, "Cannot find book in desired inventory")
            if (extractionTarget.amount == 0L)
                return MethodResult.of(null, "Cannot find book in desired inventory")
            val amount = fromStorage.extract(extractionTarget.resource, 1, it)
            if (amount != 1L) {
                it.abort()
                return MethodResult.of(null, "Cannot find book in desired inventory")
            }
            target.book = extractionTarget.resource.toStack()
            target.level!!.setBlockAndUpdate(target.blockPos, target.blockState.setValue(LecternBlock.HAS_BOOK, true))
            it.commit()
        }
        return MethodResult.of(true)
    }
}