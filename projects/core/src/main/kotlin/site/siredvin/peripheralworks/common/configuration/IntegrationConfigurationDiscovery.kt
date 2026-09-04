package site.siredvin.peripheralworks.common.configuration

import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

object IntegrationConfigurationDiscovery {
    private const val PACKAGE = "site.siredvin.peripheralworks.common.configuration.integration"
    private val packagePath = PACKAGE.replace('.', '/')

    fun discover(isModPresent: (String) -> Boolean): List<IntegrationConfiguration> {
        val classLoader = IntegrationConfigurationDiscovery::class.java.classLoader
        return classLoader.getResources(packagePath).asSequence()
            .flatMap(::classNames)
            .distinct()
            .map { Class.forName(it, true, classLoader) }
            .filter { IntegrationConfiguration::class.java.isAssignableFrom(it) }
            .map { it.getField("INSTANCE").get(null) as IntegrationConfiguration }
            .filter { isModPresent(it.modID) }
            .sortedBy(IntegrationConfiguration::name)
            .toList()
            .also(::validate)
    }

    private fun classNames(url: URL): Sequence<String> = when (url.protocol) {
        "file", "union" -> Files.list(Path.of(url.toURI())).use { files ->
            files.iterator().asSequence().map(Path::getFileName).map(Path::toString).toList().asSequence()
        }
        "jar" -> (url.openConnection() as JarURLConnection).jarFile.entries().asSequence()
            .map { it.name.removePrefix("$packagePath/") }
            .filter { '/' !in it }
        else -> error("Unsupported configuration class location: $url")
    }.filter { it.endsWith("Configuration.class") && '$' !in it }
        .map { "$PACKAGE.${it.removeSuffix(".class")}" }

    private fun validate(configurations: List<IntegrationConfiguration>) {
        val duplicateNames = configurations.groupingBy(IntegrationConfiguration::name)
            .eachCount().filterValues { count -> count > 1 }.keys
        require(duplicateNames.isEmpty()) { "Duplicate integration configuration sections: ${duplicateNames.joinToString()}" }
    }
}
