package site.siredvin.peripheralworks.computercraft.plugins.specific

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.nbt.StringTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.LecternBlock
import net.minecraft.world.level.block.entity.LecternBlockEntity
import site.siredvin.peripheralium.api.peripheral.IObservingPeripheralPlugin
import site.siredvin.peripheralium.api.peripheral.IPluggablePeripheral
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralium.api.storage.StorageUtils
import site.siredvin.peripheralium.api.storage.TargetableContainer
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralium.util.TextBookUtils
import site.siredvin.peripheralium.util.assertBetween
import java.util.*
import java.util.function.Predicate


class LecternPlugin(private val target: LecternBlockEntity): IObservingPeripheralPlugin {

    companion object {
        val OBSERVED_LECTERNS: MutableMap<BlockPos, WeakHashMap<IPluggablePeripheral, Boolean>> = mutableMapOf()

        fun sendEvent(pos: BlockPos, event: String, vararg arguments: Any) {
            if (OBSERVED_LECTERNS.containsKey(pos)) {
                val map = OBSERVED_LECTERNS[pos]!!
                if (map.size == 0) {
                    OBSERVED_LECTERNS.remove(pos)
                } else {
                    map.keys.forEach {
                        it.queueEvent(event, *arguments)
                    }
                }
            }
        }

        fun subscribe(pos: BlockPos, peripheral: IPluggablePeripheral) {
            if (!OBSERVED_LECTERNS.containsKey(pos))
                OBSERVED_LECTERNS[pos] = WeakHashMap()
            OBSERVED_LECTERNS[pos]!![peripheral] = true
        }

        fun unsubscribe(pos: BlockPos, peripheral: IPluggablePeripheral) {
            if (OBSERVED_LECTERNS.containsKey(pos))
                OBSERVED_LECTERNS[pos]!!.remove(peripheral)
        }
    }

    private var _connectedPeripheral: IPluggablePeripheral? = null
    override var connectedPeripheral: IPluggablePeripheral?
        get() = _connectedPeripheral
        set(value) {
            _connectedPeripheral = value
        }

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
        return target.page + 1
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

        val toStorage = ExtractorProxy.extractTargetableStorageFromUnknown(target.level!!, location.target)
            ?: throw LuaException("Target '$toName' is not an item inventory")

        val moved = TargetableContainer(target.bookAccess).moveTo(toStorage, 1, takePredicate = StorageUtils.ALWAYS)
        if (moved == 0)
            return MethodResult.of(null, "Not enough space in target inventory")
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun injectBook(computer: IComputerAccess, fromName: String, bookQuery: Any?): MethodResult {
        assertNoBook()

        val location: IPeripheral = computer.getAvailablePeripheral(fromName)
            ?: throw LuaException("Target '$fromName' does not exist")

        val fromStorage = ExtractorProxy.extractStorageFromUnknown(target.level!!, location.target)
            ?: throw LuaException("Target '$fromName' is not an item inventory")

        var predicate: Predicate<ItemStack> = Predicate { it.`is`(Items.WRITABLE_BOOK) || it.`is`(Items.WRITTEN_BOOK) }

        if (bookQuery != null)
            predicate = predicate.and(PeripheralPluginUtils.itemQueryToPredicate(bookQuery))

        val extractedBook = fromStorage.takeItems(predicate, 1)
        if (extractedBook.isEmpty)
            return MethodResult.of(null, "Cannot find book in desired inventory")
        LecternBlock.placeBook(null, target.level!!, target.blockPos, target.blockState, extractedBook)
        return MethodResult.of(true)
    }

    override fun onFirstAttach() {
        if (connectedPeripheral != null) {
            subscribe(target.blockPos, _connectedPeripheral!!)
        }
    }

    override fun onLastDetach() {
        if (connectedPeripheral != null) {
            unsubscribe(target.blockPos, _connectedPeripheral!!)
        }
    }
}