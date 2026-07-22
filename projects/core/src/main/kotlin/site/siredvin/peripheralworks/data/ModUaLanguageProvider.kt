package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
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
        add(Items.PERIPHERALIUM_HUB.get(), "Перифераліумний осередок")
        add(Items.NETHERITE_PERIPHERALIUM_HUB.get(), "Незеритовий перифераліумний осередок")
        add(Items.ULTIMATE_CONFIGURATOR.get(), "Універсальний конфігуратор", "§3§oУніверсальний інструмент для налаштування всього. Використайте (присівши) його на будь-якому блоку, щоб перевірити, чи можна його налаштувати")
        add(Items.ANALYZER.get(), "Аналізатор", "§3§oПредмет в розробці, який наразі показує лише сутність, що прив'язана до блоків")
        add(Items.ENTITY_CARD.get(), "Карта сутності", "§3§oМоже записувати сігнатури живих або ефімерних сутностей")

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
        add(Blocks.FLEXIBLE_STATUE.get(), "Гнучка статуя")
        add(Blocks.STATUE_WORKBENCH.get(), "Верстак для статуй", "§3§oУява ваше єдине обмеження. Іще ліміт у розмірах на 48 точок, але це не так важливо")
        add(Blocks.ENTITY_LINK.get(), "Коннектор до сутності", "§3§oДопомагає утворити з'єднання із будь-якою сутністю, потрібно лише вставити карту")
        add(Blocks.NETWORK_MANAGER.get(), text = "Менеджер мережі", "§3§oПросто підключіть його до вашої провідної мережі і він стане дуже корисний!")
        add(Blocks.HOLOGRAM_PROJECTOR.get(), "Проектор голограм", "§3§oВідтвроюй свої мрії за допомогою маленьких сутностей")

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
        add(ModText.PERIPHERAL_PROXY_CONNECTED_PERIPHERALS, "Підключені переферійні присторії:")
        add(ModText.DEFINITELY_NOT, "§3§oНу точно не ")
        add(ModText.TARGET_ENTITY, "Сутність %s записана в цій карті, наразі вона знаходиться у точці %s")
        add(ModText.ENTITY_CANNOT_BE_STORED, "Ця сутність не може бути записана")
        add(ModText.SOMETHING_STORED_INSIDE_CARD, "§e§oКартка вказує на сутність, використаєте, щоб дізнатися більше")
        add(ModText.ITEM_IS_NOT_SUITABLE_FOR_UPGRADE, "Цей предмет не підходить у якості покращення")
        add(ModText.ENTITY_LINK_UPGRADES, "Список покращень:")
        add(ModText.ENTITY_LINK_DOES_NOT_HAVE_UPGRADES, "Коннектор до сутності наразі немає покращень")
        add(ModText.ENTITY_LINK_UPGRADE_SCANNER, "Сканер")
        add(ModText.NETWORK_MANAGER_GROUP_SELECT_REQUIRED, "Спочатку виберіть групу менеджера мережі")
        add(ModText.NETWORK_MANAGER_GROUP_STALE, "Вибраної групи більше не існує; виберіть іншу групу")
        add(ModText.NETWORK_MANAGER_PERIPHERAL_MISSING, "Цей периферійний пристрій не підключено до менеджера мережі")
        add(ModText.NETWORK_MANAGER_GROUP_MEMBERSHIP_TOGGLED, "Членство периферійного пристрою в групі оновлено")
        add(ModText.NETWORK_MANAGER_UNAVAILABLE, "Прив'язаний менеджер мережі недоступний")
        add(ModText.NETWORK_MANAGER_REQUEST_REJECTED, "Запит до менеджера мережі відхилено")
        add(ModText.NETWORK_MANAGER_REQUEST_SUCCEEDED, "Групу менеджера мережі оновлено")
        add(ModText.NETWORK_MANAGER_REQUEST_FAILED, "Не вдалося оновити групу менеджера мережі: %s")
        add(ModText.NETWORK_MANAGER_SCREEN_TITLE, "Групи менеджера мережі")
        add(ModText.NETWORK_MANAGER_TAB_GROUPS, "Групи")
        add(ModText.NETWORK_MANAGER_TAB_MEMBERSHIP, "Членство")
        add(ModText.NETWORK_MANAGER_TAB_SETTINGS, "Налаштування")
        add(ModText.NETWORK_MANAGER_SEARCH, "Пошук або повна назва нової групи")
        add(ModText.NETWORK_MANAGER_MEMBERSHIP_SEARCH, "Пошук периферійних пристроїв або типів")
        add(ModText.NETWORK_MANAGER_CREATE, "Створити")
        add(ModText.NETWORK_MANAGER_RENAME, "Перейменувати вибрану групу")
        add(ModText.NETWORK_MANAGER_COLOR, "Колір (#RRGGBB або -1)")
        add(ModText.NETWORK_MANAGER_COLOR_PICKER, "Відкрити палітру кольорів RGB")
        add(ModText.NETWORK_MANAGER_VISIBILITY, "Видимість групи: %s")
        add(ModText.NETWORK_MANAGER_VISIBILITY_DEFAULT, "за режимом")
        add(ModText.NETWORK_MANAGER_VISIBILITY_SHOW, "завжди показувати")
        add(ModText.NETWORK_MANAGER_VISIBILITY_HIDE, "завжди приховувати")
        add(ModText.NETWORK_MANAGER_VISUALIZATION, "Оверлей: %s")
        add(ModText.NETWORK_MANAGER_VISUALIZATION_ALL, "усе")
        add(ModText.NETWORK_MANAGER_VISUALIZATION_SELECTED, "вибрана група")
        add(ModText.NETWORK_MANAGER_VISUALIZATION_SELECTED_AND_UNGROUPED, "вибрана група + без групи")
        add(ModText.NETWORK_MANAGER_VISUALIZATION_UNGROUPED, "без групи")
        add(ModText.NETWORK_MANAGER_DELIMITER, "Роздільник ієрархії")
        add(ModText.NETWORK_MANAGER_RANGE, "Радіус оверлея")
        add(ModText.NETWORK_MANAGER_APPLY, "Застосувати")
        add(ModText.NETWORK_MANAGER_SAVE_SETTINGS, "Зберегти налаштування")
        add(ModText.NETWORK_MANAGER_DELETE, "Видалити")
        add(ModText.NETWORK_MANAGER_DELETE_TOOLTIP, "Назавжди видаляє групу та всі членства")
        add(ModText.NETWORK_MANAGER_DELETE_CONFIRM, "Видалити групу %s?")
        add(ModText.NETWORK_MANAGER_DELETE_WARNING, "Усі членства також буде видалено без можливості відновлення.")
        add(ModText.NETWORK_MANAGER_SELECT_GROUP, "Виберіть групу на вкладці «Групи»")
        add(ModText.NETWORK_MANAGER_SELECTED, "Вибрано: %s")
        add(ModText.NETWORK_MANAGER_INVALID_NAME, "Введіть назву групи від 1 до 64 символів")
        add(ModText.NETWORK_MANAGER_DUPLICATE_NAME, "Група з такою повною назвою вже існує")
        add(ModText.NETWORK_MANAGER_INVALID_COLOR, "Використовуйте #RRGGBB або -1 для типового кольору")
        add(ModText.NETWORK_MANAGER_INVALID_RANGE, "Радіус має бути від %s до %s")
        add(ModText.NETWORK_MANAGER_REQUEST_SENT, "Зміну надіслано на сервер")

        add(ModText.TECH_REBORN_ENERGY, "Енергія з Tech reborn")

        add(ModTooltip.ITEM_DISABLED, "  §4§nПредмет заборонений до використання в налаштуваннях")
        add(ModTooltip.PERIPHERALIUM_HUB_MAX_PERIPHERALS, "  §6Максимальна кількість периферійних пристроїв: %s")
        add(ModTooltip.PERIPHERALIUM_HUB_STORED, "  §6Підключені периферійні пристрої:")
        add(ModTooltip.PERIPHERALIUM_HUB_POCKET, "  §6Активний режим: портативний комп'ютер")
        add(ModTooltip.PERIPHERALIUM_HUB_TURTLE, "  §6Активний режим: черепашка")
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
        add(ModTooltip.FLEXIBLE_STATUE_AUTHOR, "Автор: %s")
        add(ModTooltip.ENTITY_LINK_MODE, "  Налаштування коннектора до сутності")
        add(ModTooltip.NETWORK_MANAGER_MODE, "  Оверлей менеджера мережі")
        add(ModTooltip.NETWORK_MANAGER_SELECTED_GROUP, "  Вибрана група: %s")

        add(ModEnergiesText.MERCURY_FLUX, "Ртутний флюс")
        add(ModEnergiesText.EMBER, "Вуглечки")
        add(ModEnergiesText.SOURCE, "Енергія джерела")

        addUpgrades(PeripheraliumHubPeripheral.ID, "З вбудованим осередком")
        addUpgrades(PeripheraliumHubPeripheral.NETHERITE_ID, "З вбудованим незеритовим осередком")
        addTurtle(UniversalScannerPeripheral.UPGRADE_ID, "Скануюча")
        addTurtle(UltimateSensorPeripheral.UPGRADE_ID, "Зондуюча")
        addTurtle(HologramProjectorPeripheral.UPGRADE_ID, "Проекуюча")
        addPocket(UniversalScannerPeripheral.UPGRADE_ID, "Скануючий")
        addPocket(UltimateSensorPeripheral.UPGRADE_ID, "Зондуючий")
        addPocket(HologramProjectorPeripheral.UPGRADE_ID, "Проекуючий")

        hooks.forEach { it.accept(this) }
    }
}
