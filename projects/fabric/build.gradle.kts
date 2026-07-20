@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.fabric")
    id("site.siredvin.publishing")
    id("site.siredvin.mod-publishing")
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra
val minimalTestEnvironment = providers.gradleProperty("minimalTestEnvironment").isPresent

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

if (minimalTestEnvironment) {
    sourceSets.main { kotlin.exclude("site/siredvin/peripheralworks/integrations/**") }
    tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") { exclude("**/integrations/**") }
}

val testMod = sourceSets.create("testMod") {
    compileClasspath += sourceSets.main.get().compileClasspath
    compileClasspath += sourceSets.main.get().output
    compileClasspath += project(":core").sourceSets["testMod"].output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().output
    runtimeClasspath += project(":core").sourceSets["testMod"].output
}

net.fabricmc.loom.configuration.RemapConfigurations.setupForSourceSet(project, testMod)

val testiariumCctArtifacts = configurations.detachedConfiguration(
    project.dependencies.create("site.siredvin:testiarium-core-1.20.1:0.1.1:cct-test-mod@jar"),
    project.dependencies.create("site.siredvin:testiarium-fabric-1.20.1:0.1.1:cct-test-mod@jar"),
).apply { isTransitive = false }

val testiariumTestModArtifacts = configurations.detachedConfiguration(
    project.dependencies.create("site.siredvin:testiarium-core-1.20.1:0.1.1:test-mod@jar"),
    project.dependencies.create("site.siredvin:testiarium-fabric-1.20.1:0.1.1:test-mod@jar"),
).apply { isTransitive = false }

val testiariumMainArtifacts = configurations.detachedConfiguration(
    project.dependencies.create("site.siredvin:testiarium-core-1.20.1:0.1.1"),
    project.dependencies.create("site.siredvin:testiarium-fabric-1.20.1:0.1.1"),
).apply { isTransitive = false }

loom {
    mods {
        register("peripheralworks-testmod") {
            sourceSet(testMod)
            sourceSet(project(":core").sourceSets["testMod"])
        }
    }
    runs {
        create("peripheralWorksGameTest") {
            server()
            source(testMod)
            property("fabric-api.gametest", "true")
            property("fabric.debug.disableModIds", "create")
            property("fabric.debug.loadLate", "testiarium_cct_testmod")
            property("testiarium.tags", providers.gradleProperty("testiariumTags").orElse("peripheralworks").get())
            property("testiarium.structures", project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath)
            property("testiarium.fixture-source", project(":core").file("src/testMod/resources/gameteststructures").absolutePath)
            property("testiarium.cct-fixtures", project(":core").layout.buildDirectory.dir("resources/testMod/computer").get().asFile.absolutePath)
            property("testiarium.gametest-report", layout.buildDirectory.file("test-results/peripheralworks-gametest.xml").get().asFile.absolutePath)
            vmArg("-ea")
            runDir("run/peripheralworks-gametest")
        }
        create("peripheralWorksClientGameTest") {
            client()
            source(testMod)
            property("fabric-api.gametest", "true")
            property("fabric.debug.disableModIds", "create,testiarium_testmod,testiarium_cct_testmod")
            property("fabric.debug.loadLate", "testiarium_testmod")
            property("testiarium.client", "true")
            property("testiarium.tags", "network-manager-client")
            property("testiarium.structures", project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath)
            property("testiarium.gametest-report", layout.buildDirectory.file("test-results/network-manager-client-gametest.xml").get().asFile.absolutePath)
            property("testiarium.screenshots", layout.buildDirectory.dir("screenshots/network-manager-client").get().asFile.absolutePath)
            vmArg("-ea")
            runDir("run/network-manager-client-gametest")
        }
    }
}

repositories {
    mavenLocal()
    maven {
        name = "SirEdvin's Maven proxy"
        url = uri("https://mvn.siredvin.site/minecraft")
    }
}

dependencies {
    if (!minimalTestEnvironment) {
        modApi(libs.bundles.externalMods.fabric.integrations.api) {
            exclude("net.fabricmc.fabric-api")
        }
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

    if (!minimalTestEnvironment) {
        libs.bundles.externalMods.fabric.integrations.full.get().map { modCompileOnly(it) }
        libs.bundles.externalMods.fabric.integrations.active.get().map { modRuntimeOnly(it) }
        libs.bundles.externalMods.fabric.integrations.activedep.get().map { modRuntimeOnly(it) }
    }

    add("modTestModImplementation", libs.bundles.kotlin)
    add("modTestModImplementation", libs.bundles.fabric.core)
    add("modTestModImplementation", libs.bundles.ccfabric)
    add("modTestModImplementation", files(testiariumMainArtifacts))
    add("modTestModImplementation", files(testiariumTestModArtifacts))
    add("modTestModImplementation", files(testiariumCctArtifacts))
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
