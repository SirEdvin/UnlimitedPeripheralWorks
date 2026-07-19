@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.fabric")
    id("site.siredvin.publishing")
    id("site.siredvin.mod-publishing")
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

baseShaking {
    projectPart.set("fabric")
    integrationRepositories.set(false)
    shake()
}

fabricShaking {
    commonProjectName.set("core")
    createRefmap.set(true)
    stablePlayer.set(true)
    accessWidener.set(project(":core").file("src/main/resources/peripheralworks.accesswidener"))
    extraVersionMappings.set(
        mapOf(
            "computercraft" to "cc-tweaked",
            "peripheralium" to "peripheralium",
            "tweakium" to "tweakium",
            "broccolium" to "broccolium",
        ),
    )
    shake()
}

repositories {
    mavenLocal()
    maven {
        name = "SirEdvin's Maven proxy"
        url = uri("https://mvn.siredvin.site/minecraft")
    }
}

dependencies {
    modApi(libs.bundles.externalMods.fabric.integrations.api) {
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation(libs.bundles.fabric.core)
    modImplementation(libs.bundles.fabric)
    modImplementation(libs.bundles.ccfabric) {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
        exclude("mezz.jei")
    }
    modImplementation(libs.bundles.fabric.include) {
        isTransitive = false
    }
    include(libs.bundles.fabric.include)

    modRuntimeOnly(libs.bundles.externalMods.fabric.runtime) {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    // I hate this, but since someone is not clearing their mess, I need to do it

    modCompileOnly("dev.draylar:magna:1.10.1+1.20.1") {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
        exclude("com.github.Draylar.omega-config", "omega-config-base")
    }

    modCompileOnly("dev.draylar.omega-config:omega-config-base:1.3.0+1.19.2")

    libs.bundles.externalMods.fabric.integrations.full.get().map { modCompileOnly(it) }
    libs.bundles.externalMods.fabric.integrations.active.get().map { modRuntimeOnly(it) }
    libs.bundles.externalMods.fabric.integrations.activedep.get().map { modRuntimeOnly(it) }
}

publishingShaking {
    shake()
}

modPublishing {
    output.set(tasks.remapJar)
    requiredDependencies.set(
        listOf(
            "cc-tweaked",
            "fabric-language-kotlin",
        ),
    )
    requiredDependenciesCurseforge.add("forge-config-api-port")
    requiredDependenciesModrinth.add("forge-config-api-port")
    shake()
}
