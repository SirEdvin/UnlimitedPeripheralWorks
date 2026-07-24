import java.io.ByteArrayInputStream

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

evaluationDependsOn(":core")

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

sourceSets.main {
    kotlin {
        exclude("site/siredvin/peripheralworks/integrations/ae2/**")
        exclude("site/siredvin/peripheralworks/integrations/create/**")
        exclude("site/siredvin/peripheralworks/integrations/kubejs/**")
        exclude("site/siredvin/peripheralworks/integrations/modern_industrialization/**")
        exclude("site/siredvin/peripheralworks/integrations/powah/**")
    }
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

val testiariumMainArtifacts = configurations.detachedConfiguration(
    project.dependencies.create("site.siredvin:testiarium-core-1.21.1:0.1.1"),
    project.dependencies.create("site.siredvin:testiarium-fabric-1.21.1:0.1.1"),
).apply {
    isTransitive = false
}

val testiariumTestArtifacts = configurations.detachedConfiguration(
    project.dependencies.create("site.siredvin:testiarium-core-1.21.1:0.1.1:test-mod@jar"),
    project.dependencies.create("site.siredvin:testiarium-core-1.21.1:0.1.1:cct-test-mod@jar"),
    project.dependencies.create("site.siredvin:testiarium-fabric-1.21.1:0.1.1:test-mod@jar"),
).apply {
    isTransitive = false
}

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
            property("fabric.debug.loadLate", "testiarium_testmod")
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

tasks.named<JavaExec>("runPeripheralWorksGameTest") {
    standardInput = ByteArrayInputStream("true\n".toByteArray())
}

repositories {
    mavenLocal()
    maven {
        name = "SirEdvin's Maven proxy"
        url = uri("https://mvn.siredvin.site/minecraft")
    }
    // location of the maven that hosts JEI files since January 2023
    maven {
        name = "Jared's maven"
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        name = "OwO maven"
        url = uri("https://maven.wispforest.io")
        content {
            includeGroup("io.wispforest")
            includeGroup("io.wispforest.endec")
        }
    }
    maven {
        name = "Polymer repo"
        url = uri("https://maven.nucleoid.xyz")
        content {
            includeGroup("eu.pb4")
            includeGroup("xyz.nucleoid")
        }
    }
    maven {
        name = "OSS Sonatype Repo"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        content {
            includeGroup("me.lucko")
        }
    }
    maven {
        name = "ModMenu maven"
        url = uri("https://maven.terraformersmc.com/releases")
        content {
            includeGroup("com.terraformersmc")
        }
    }
    // for reach entity attributes, required by Magna
    maven {
        url = uri("https://maven.jamieswhiteshirt.com/libs-release/")
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }
    maven {
        name = "Draylar maven"
        url = uri("https://maven.draylar.dev/releases")
        content {
            includeGroup("dev.draylar")
            includeGroup("dev.draylar.omega-config")
        }
    }
    maven {
        name = "Jitpack for MI"
        url = uri("https://jitpack.io")
        content {
            /* For Magna */
            includeGroup("com.github.Draylar.omega-config")
        }
    }
    maven {
        name = "Ladysnake Mods"
        url = uri("https://maven.ladysnake.org/releases")
        content {
            includeGroup("io.github.ladysnake")
            includeGroupByRegex("io\\.github\\.onyxstudios.*")
        }
    }
    maven {
        name = "devOS"
        url = uri("https://mvn.devos.one/snapshots/")
        content {
            includeGroup("com.simibubi.create")
            includeGroupByRegex("io\\.github\\.fabricators_of_create.*")
            includeGroup("com.tterrag.registrate_fabric")
            includeGroup("io.github.tropheusj")
        }
    }
    maven {
        name = "github packages via jitpack"
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.llamalad7.mixinextras")
            includeGroup("com.github.Chocohead")
        }
    }
    maven {
        name = "Mod maven"
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("com.jozufozu.flywheel")
        }
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
        content {
            includeGroup("dev.emi")
        }
    }
    maven {
        name = "KubeJS's author maven"
        url = uri("https://maven.latvian.dev/releases")
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }
}

dependencies {
    compileOnly(libs.endec)

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
        libs.bundles.externalMods.fabric.integrations.raw.full.get().map { compileOnly(it) }
        libs.bundles.externalMods.fabric.integrations.active.get().map { modRuntimeOnly(it) }
        libs.bundles.externalMods.fabric.integrations.activedep.get().map { modRuntimeOnly(it) }
        libs.bundles.externalMods.fabric.integrations.raw.active.get().map { runtimeOnly(it) }
    }
    runtimeOnly(libs.endec)
    runtimeOnly(libs.endec.gson)
    runtimeOnly(libs.endec.jankson)
    runtimeOnly(libs.endec.netty)
    add("modTestModImplementation", files(testiariumMainArtifacts))
    add("modTestModImplementation", files(testiariumTestArtifacts))
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
