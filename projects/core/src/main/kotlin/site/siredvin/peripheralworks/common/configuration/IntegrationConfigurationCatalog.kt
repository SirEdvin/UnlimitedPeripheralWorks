package site.siredvin.peripheralworks.common.configuration

import site.siredvin.peripheralworks.api.IForgeConfigHandler

object IntegrationConfigurationCatalog {
    private val shared = listOf(
        site.siredvin.peripheralworks.common.configuration.integration.additionallanterns.Configuration,
        site.siredvin.peripheralworks.common.configuration.integration.ae2.Configuration,
        site.siredvin.peripheralworks.common.configuration.integration.automobility.Configuration,
        site.siredvin.peripheralworks.common.configuration.integration.create.Configuration,
        site.siredvin.peripheralworks.common.configuration.integration.naturescompass.Configuration,
        site.siredvin.peripheralworks.common.configuration.integration.powah.Configuration,
        site.siredvin.peripheralworks.common.configuration.integration.toms_storage.Configuration,
    )

    val forge: List<IForgeConfigHandler> = catalog(
        shared +
            listOf(
                site.siredvin.peripheralworks.common.configuration.integration.ars_nouveau.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.deepresonance.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.easy_villagers.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.embers.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.fluxnetworks.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.integrateddynamics.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.occultism.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.projecte.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.theurgy.Configuration,
            ),
    )

    val fabric: List<IForgeConfigHandler> = catalog(
        shared +
            listOf(
                site.siredvin.peripheralworks.common.configuration.integration.alloy_forgery.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.modern_industrialization.Configuration,
                site.siredvin.peripheralworks.common.configuration.integration.universal_shops.Configuration,
            ),
    )

    private fun catalog(configurations: List<IForgeConfigHandler>): List<IForgeConfigHandler> = configurations.also {
        val duplicateNames = it.groupingBy(IForgeConfigHandler::name).eachCount().filterValues { count -> count > 1 }.keys
        require(duplicateNames.isEmpty()) { "Duplicate integration configuration sections: ${duplicateNames.joinToString()}" }
    }
}
