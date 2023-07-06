package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import java.util.function.Consumer

class ModUaLanguageProvider(
    output: PackOutput,
) : ModLanguageProvider(output, "uk_ua") {

    companion object {
        private val hooks: MutableList<Consumer<ModUaLanguageProvider>> = mutableListOf()

        fun addHook(hook: Consumer<ModUaLanguageProvider>) {
            hooks.add(hook)
        }
    }

    override fun addTranslations() {
        add(Items.ULTIMATE_CONFIGURATOR.get(), "Перифераліумний осередок")
        add(Items.PERIPHERALIUM_HUB.get(), "Незеритовий перифераліумний осередок")
        add(Items.NETHERITE_PERIPHERALIUM_HUB.get(), "Універсальний конфігуратор", "§3§oУніверсальний інструмент для налаштування всього. Використайте (присівши) його на будь-якому блоку, щоб перевірити, чи можна його налаштувати")

        add(Blocks.PERIPHERAL_CASING.get(), "Оболонка периферійного пристрою")
        add(Blocks.UNIVERSAL_SCANNER.get(), "Універсальний сканер", "§3Найкращий инструмент для дослідження навколишнього світу, особливо якщо у вас немає третього ока. Працює також із черепашками та портативними комп'ютерами")
        add(Blocks.ULTIMATE_SENSOR.get(), "Надпотужний датчик", "§3§oБезмежні можливості вивчення світу завдякі цій маленькій коробочці. Працює також із черепашками та портативними комп'ютерами")
        add(Blocks.ITEM_PEDESTAL.get(), "П'єдестал для предметів", "§3§oМиленький п'єдестал, що може зберігати лише один стак предметів. Але здається, ви можете отримати значно більше інформації про них.")
        add(Blocks.MAP_PEDESTAL.get(), "П'єдестал для мап", "§3§oМиленький п'єдестал, що може зберігати лише один стак предметів. Посилений компасом, він тепер може розуміти мапи.")
        add(Blocks.DISPLAY_PEDESTAL.get(), "П'єдестал для демонстрацій", "§3§oМиленький п'єдестал, який не може зберігати предмети, але може показати будь-який предмет")
        add(Blocks.REMOTE_OBSERVER.get(), "Віддалений спостерігач", "§3§oПрацює як звичайний спостерічаг, але може бачити трішки далі")
        add(Blocks.PERIPHERAL_PROXY.get(), "Проксі для периферійних пристроїв", "§3§oДає можливість підключати периферійні присторії, що знаходяться на відстані, до комп'ютера без використання провідної мережі")
        add(Blocks.FLEXIBLE_REALITY_ANCHOR.get(), "Гнучкий якор реальності")
        add(Blocks.REALITY_FORGER.get(), "Кузня реальності", "§3§oСкладний девайс, який здатний перековувати якорі реальності на щось інше")
        add(Blocks.RECIPE_REGISTRY.get(), "Реєстр рецептів", "§3§oПрилад знань, який має силу розуміти будь-який рецепт, але іноді йому бракує сили переказати цю інформацію")
        add(Blocks.INFORMATIVE_REGISTRY.get(), "Реєстр інформації", "§3§oПрилад знань, якии має список усіх можливих предметів у світі")

        add(ModText.CREATIVE_TAB, "Перифіральних пристроїв нескінченний край")

        add(ModText.REMOTE_OBSERVER_NOT_SELF, "Неможливо підключити віддаленний спостерігач до самого себе")
        add(ModText.REMOTE_OBSERVER_TOO_FAR, "Віддаленний спостерігач задалеко від цього блоку")
        add(ModText.REMOTE_OBSERVER_TOO_MANY, "Забагато блоків вже підключено до віддаленного спостерігача")
        add(ModText.REMOTE_OBSERVER_BLOCK_ADDED, "Віддаленний спостерігач тепер буде спостерігати за цим блоком")
        add(ModText.REMOTE_OBSERVER_BLOCK_REMOVED, "Віддалений спостерігач більше не буде спостерігати за цим блоком")

        add(ModText.PERIPHERAL_PROXY_NOT_SELF, "Неможливо підключити проксі для периферійних пристроїв до самого себе")
        add(ModText.PERIPHERAL_PROXY_TOO_MANY, "Проксі для периферійних пристроїв задалеко від цьогь блоку")
        add(ModText.PERIPHERAL_PROXY_TOO_FAR, "Забагато периферійних пристоїв вже під'єднано до проксі для периферійних пристроїв")
        add(ModText.PERIPHERAL_PROXY_IS_NOT_A_PERIPHERAL, "Цей блок не є периферійним пристоєм")
        add(ModText.PERIPHERAL_PROXY_FORBIDDEN, "Цей блок заборонено додавати до проксі для периферійних пристроїв")
        add(ModText.PERIPHERAL_PROXY_BLOCK_ADDED, "Цей периферійний пристрій тепер під'єднано до проксі для периферійних пристроїв")
        add(ModText.PERIPHERAL_PROXY_BLOCK_REMOVED, "Цей периферійний пристрій тепер від'єднано від проксі для периферійних пристроїв")
        add(ModText.DEFINITELY_NOT, "§3§oНу точно не ")

        add(ModText.TECH_REBORN_ENERGY, "Енергія з Tech reborn")

        add(ModTooltip.ITEM_DISABLED, "  §4§nПредмет заборонений до використання в налаштуваннях")
        add(ModTooltip.PERIPHERALIUM_HUB_MAX_PERIPHERALS, "  §6Максимальна кількість периферійних пристроїв: %s")
        add(ModTooltip.UNIVERSAL_SCANNER_FREE_RANGE, "  §6Бескоштовний радіус сканування: %s")
        add(ModTooltip.UNIVERSAL_SCANNER_MAX_RANGE, "  §6Максимальний радіус сканування: %s")
        add(ModTooltip.REMOTE_OBSERVER_MODE, "  Налаштування віддаленного спостерічага")
        add(ModTooltip.PERIPHERAL_PROXY_MODE, "  Налаштування проксі для периферійних пристроїв")
        add(ModTooltip.ACTIVE_CONFIGURATION_MODE, "Поточний режим налаштування:")
        add(ModTooltip.CONFIGURATION_TARGET_BLOCK, "Блок, що налаштовується %s")
        add(ModTooltip.REMOTE_OBSERVER_RANGE, "  §6Максимальна дальність стостерігання: %s")
        add(ModTooltip.REMOTE_OBSERVER_MAX_CAPACITY, "  §6Максимальна кількість блоків, що можно підключити: %s")
        add(ModTooltip.PERIPHERAL_PROXY_RANGE, "  §6Максимальна дальність для під'єднання перифейрійних пристроїв: %s")
        add(ModTooltip.PERIPHERAL_PROXY_MAX_CAPACITY, "  §6Максимальна кількість під'єднаних перифейрійних пристроїв: %s")
        add(ModTooltip.REALITY_FORGER_RANGE, "  §6Максимальна дальність ковання: %s")

        addUpgrades(PeripheraliumHubPeripheral.ID, "З вбудованим осередком")
        addUpgrades(PeripheraliumHubPeripheral.NETHERITE_ID, "З вбудованим незеритовим осередком")
        addTurtle(UniversalScannerPeripheral.UPGRADE_ID, "Скануюча")
        addTurtle(UltimateSensorPeripheral.UPGRADE_ID, "Зондуюча")
        addPocket(UniversalScannerPeripheral.UPGRADE_ID, "Скануючий")
        addPocket(UltimateSensorPeripheral.UPGRADE_ID, "Зондуючий")

        hooks.forEach { it.accept(this) }
    }
}
