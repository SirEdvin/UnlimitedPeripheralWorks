import net.darkhax.curseforgegradle.TaskPublishCurseForge
import site.siredvin.peripheralium.gradle.mavenDependencies

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.publishing")
    id("site.siredvin.mod-publishing")
    id("site.siredvin.neoforge")
}
val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

evaluationDependsOn(":core")

val embeddedIntegrationDependencies = configurations.create("embeddedIntegrationDependencies")
val embeddedIntegrationJars = layout.buildDirectory.dir("embedded-integration-dependencies")
val developmentRuntime = configurations.create("developmentRuntime")
val cctTestMod = configurations.create("cctTestMod")
configurations.runtimeClasspath {
    extendsFrom(developmentRuntime)
}

baseShaking {
    projectPart.set("forge")
    integrationRepositories.set(true)
    shake()
}

neoforgeShaking {
    commonProjectName.set("core")
    useAT.set(true)
    extraVersionMappings.set(
        mapOf(
            "computercraft" to "cc-tweaked",
            "peripheralium" to "peripheralium",
            "broccolium" to "broccolium",
            "tweakium" to "tweakium",
        ),
    )
    shake()
}

sourceSets.main {
    kotlin {
        exclude("site/siredvin/peripheralworks/integrations/embers/**")
        exclude("site/siredvin/peripheralworks/integrations/mna/**")
    }
}

val testMod = sourceSets.create("testMod") {
    compileClasspath += sourceSets.main.get().compileClasspath
    compileClasspath += sourceSets.main.get().output
    compileClasspath += project(":core").sourceSets["testMod"].output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().output
    runtimeClasspath += project(":core").sourceSets["testMod"].output
}
configurations.named(testMod.implementationConfigurationName) {
    extendsFrom(cctTestMod)
}

repositories {
    mavenLocal()
    // location of the maven that hosts JEI files since January 2023
    maven {
        name = "Jared's maven"
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content {
            includeGroup("thedarkcolour")
        }
    }
    // Integration dependencies
    maven {
        name = "KliKli Dev Repsy Maven (Occultism)"
        url = uri("https://repo.repsy.io/mvn/klikli-dev/mods")
        content {
            includeGroup("com.klikli_dev")
        }
    }

    maven {
        name = "Curios Maven"
        url = uri("https://maven.theillusivec4.top/")
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }

    maven {
        name = "SBL Maven"
        url = uri("https://dl.cloudsmith.io/public/tslat/sbl/maven/")
        content {
            includeGroup("net.tslat.smartbrainlib")
        }
    }
    maven {
        name = "Geckolib Maven"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroup("software.bernie.geckolib")
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven {
        name = "tterrag maven"
        url = uri("https://maven.tterrag.com/")
        content {
            includeGroup("com.tterrag.registrate")
            includeGroup("com.jozufozu.flywheel")
        }
    }
    maven {
        name = "Create maven"
        url = uri("https://maven.createmod.net")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("net.createmod.ponder")
            includeGroup("dev.engine-room.flywheel")
        }
    }
    maven {
        name = "Occultism maven"
        url = uri("https://dl.cloudsmith.io/public/klikli-dev/mods/maven/")
        content {
            includeGroup("com.klikli_dev")
        }
    }
    maven {
        name = "Latvian mods, mostly KubeJS"
        url = uri("https://maven.latvian.dev/releases")
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }

    maven {
        name = "Dependencies for kubej"
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.rtyley")
        }
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.forge.raw)
    implementation(libs.bundles.forge.base)
    implementation(libs.bundles.forge.include)

    runtimeOnly(libs.bundles.externalMods.forge.runtime)

    libs.bundles.externalMods.forge.integrations.full.get().map { compileOnly(it) }
    libs.bundles.externalMods.forge.integrations.active.get().map { runtimeOnly(it) }
    libs.bundles.externalMods.forge.integrations.activedep.get().map { runtimeOnly(it) }

    jarJar(libs.bundles.forge.jjar) {
        isTransitive = false
    }

    add(embeddedIntegrationDependencies.name, libs.create.forge)
    add(embeddedIntegrationDependencies.name, libs.gtceu)
    compileOnly(fileTree(embeddedIntegrationJars) { include("*.jar") })

    add(testMod.implementationConfigurationName, libs.testiarium.core)
    add(testMod.implementationConfigurationName, libs.testiarium.forge)
    add(testMod.implementationConfigurationName, "site.siredvin:testiarium-core-1.21.1:0.1.1:test-mod@jar") {
        isTransitive = false
    }
    add(testMod.implementationConfigurationName, "site.siredvin:testiarium-forge-1.21.1:0.1.1:test-mod@jar") {
        isTransitive = false
    }
    add(cctTestMod.name, "site.siredvin:testiarium-core-1.21.1:0.1.1:cct-test-mod@jar") {
        isTransitive = false
    }
    add(developmentRuntime.name, libs.testiarium.forge)
    add(developmentRuntime.name, "site.siredvin:testiarium-forge-1.21.1:0.1.1:test-mod@jar") {
        isTransitive = false
    }
    add(developmentRuntime.name, "maven.modrinth:refined-storage:lHHiI26k")
}

tasks.named<ProcessResources>(testMod.processResourcesTaskName) {
    from(cctTestMod.map { zipTree(it) }) {
        include("site/siredvin/testiarium/cct/**")
    }
}

neoForge {
    val peripheralworks = mods.named("peripheralworks")
    val peripheralworksTestMod by mods.registering {
        sourceSet(testMod)
        sourceSet(project(":core").sourceSets["testMod"])
    }
    runs {
        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("run/peripheralworks-gametest")
            systemProperty("neoforge.enabledGameTestNamespaces", "peripheralworks_testmod")
            systemProperty("testiarium.tags", providers.gradleProperty("testiariumTags").orElse("peripheralworks").get())
            systemProperty("testiarium.structures", project.project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath)
            systemProperty("testiarium.fixture-source", project.project(":core").file("src/testMod/resources/gameteststructures").absolutePath)
            systemProperty("testiarium.cct-fixtures", project.project(":core").layout.buildDirectory.dir("resources/testMod/computer").get().asFile.absolutePath)
            systemProperty("testiarium.gametest-report", layout.buildDirectory.file("test-results/peripheralworks-gametest.xml").get().asFile.absolutePath)
            jvmArgument("-ea")
            programArgument("--nogui")
            loadedMods.add(peripheralworks.get())
            loadedMods.add(peripheralworksTestMod.get())
        }
        register("clientGameTest") {
            type = "client"
            gameDirectory = file("run/network-manager-client-gametest")
            systemProperty("neoforge.enabledGameTestNamespaces", "peripheralworks_testmod")
            systemProperty("testiarium.client", "true")
            systemProperty("testiarium.tags", providers.gradleProperty("testiariumClientTags").orElse("network-manager-client,display-pedestal-client").get())
            systemProperty("testiarium.structures", project.project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath)
            systemProperty("testiarium.gametest-report", layout.buildDirectory.file("test-results/network-manager-client-gametest.xml").get().asFile.absolutePath)
            systemProperty("testiarium.screenshots", layout.buildDirectory.dir("screenshots/network-manager-client").get().asFile.absolutePath)
            jvmArgument("-ea")
            loadedMods.add(peripheralworks.get())
            loadedMods.add(peripheralworksTestMod.get())
        }
    }
}

val extractEmbeddedIntegrationDependencies by tasks.register<Copy>("extractEmbeddedIntegrationDependencies") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(embeddedIntegrationDependencies.map { zipTree(it) }) {
        include(
            "META-INF/jarjar/Registrate-MC1.21-1.3.0+67.jar",
            "META-INF/jarjar/flywheel-neoforge-1.21.1-1.0.6.jar",
            "META-INF/jarjar/ponder-neoforge-1.0.82+mc1.21.1.jar",
            "META-INF/jarjar/ldlib-neoforge-1.21.1-1.0.35.a.jar",
        )
        eachFile { path = name }
        includeEmptyDirs = false
    }
    into(embeddedIntegrationJars)
}

tasks.compileKotlin {
    dependsOn(extractEmbeddedIntegrationDependencies)
    compilerOptions.freeCompilerArgs.add("-Xwarning-level=OVERRIDE_DEPRECATION:disabled")
}

publishingShaking {
    shake()
    project.publishing {
        publications {
            named<MavenPublication>("maven") {
                mavenDependencies {
                    exclude(libs.testiarium.forge.get())
                    exclude(dependencies.create("maven.modrinth:refined-storage:lHHiI26k"))
                }
            }
        }
    }
}

modPublishing {
    output.set(tasks.jar)
    requiredDependencies.set(
        listOf(
            "cc-tweaked",
            "kotlin-for-forge",
        ),
    )
    shake()
}
tasks.named<TaskPublishCurseForge>("publishCurseForge") {
    uploadArtifacts.forEach { it.addEnvironment("Client", "Server") }
}
