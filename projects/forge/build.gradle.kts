import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension
import java.util.UUID

// ForgeGradle 6 (site.siredvin.forge) refuses Gradle 9, which Loom 1.18 needs for CC:Tweaked 1.121.0.
plugins {
    id("net.neoforged.moddev.legacyforge")
    id("site.siredvin.publishing")
    id("site.siredvin.mod-publishing")
}
val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra
val minimalTestEnvironment = providers.gradleProperty("minimalTestEnvironment").isPresent
val gtceuTestEnvironment = providers.gradleProperty("gtceuTestEnvironment").isPresent
val neuralTestEnvironment = providers.gradleProperty("neuralTestEnvironment").orElse("none").get()
require(neuralTestEnvironment in setOf("none", "hnn", "extra")) { "neuralTestEnvironment must be none, hnn or extra" }
val testWithoutAE2 = providers.gradleProperty("testWithoutAE2").isPresent
val gameTestRunDirectory = layout.buildDirectory.dir("gametest-runs/${UUID.randomUUID()}")
require(!testWithoutAE2 || minimalTestEnvironment) { "testWithoutAE2 requires minimalTestEnvironment" }

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    source(project(":core").fileTree("src/ae2Integration/kotlin"))
}

baseShaking {
    projectPart.set("forge")
    integrationRepositories.set(true)
    shake()
}

if (minimalTestEnvironment) {
    val excludedIntegrations = file("src/main/kotlin/site/siredvin/peripheralworks/integrations").listFiles().orEmpty()
        .filter { it.isDirectory && it.name !in setOf("ae2", "hostilenetworks", "extrahnn") && !(gtceuTestEnvironment && it.name == "gtceu") }
        .map { "**/integrations/${it.name}/**" }
    sourceSets.main { kotlin.exclude(excludedIntegrations) }
    tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") { exclude(excludedIntegrations) }
}

val testMod = sourceSets.create("testMod") {
    compileClasspath += sourceSets.main.get().compileClasspath
    compileClasspath += sourceSets.main.get().output
    compileClasspath += project(":core").sourceSets["testMod"].output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().output
    runtimeClasspath += project(":core").sourceSets["testMod"].output
}
tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileTestModKotlin") {
    source(project(":core").fileTree("src/ae2Test/kotlin"))
    if (!minimalTestEnvironment || gtceuTestEnvironment) {
        source(fileTree("src/gtceuTest/kotlin"))
    }
}

val testSystemProperties = mapOf(
    "forge.enabledGameTestNamespaces" to "peripheralworks_testmod",
    "testiarium.structures" to project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath,
)
val fixtureSystemProperties = mapOf(
    "testiarium.fixture-source" to project(":core").file("src/testMod/resources/gameteststructures").absolutePath,
    "testiarium.cct-fixtures" to project(":core").layout.buildDirectory.dir("resources/testMod/computer").get().asFile.absolutePath,
)

// Resolved outside the extension, whose own minecraftVersion is unset until enable().
val forgeArtifactVersion = "$minecraftVersion-${libs.versions.forge.asProvider().get()}"
legacyForge {
    enable { forgeVersion = forgeArtifactVersion }
    parchment {
        minecraftVersion = libs.versions.parchmentMc.get()
        mappingsVersion = libs.versions.parchment.get()
    }
    mods {
        create("peripheralworks") { sourceSet(sourceSets.main.get()) }
        create("peripheralworks_testmod") {
            sourceSet(testMod)
            sourceSet(project(":core").sourceSets["testMod"])
        }
    }
    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            loadedMods.add(mods.named("peripheralworks"))
        }
        create("client") {
            client()
            gameDirectory = file("run")
        }
        create("server") {
            server()
            gameDirectory = file("run/server")
            programArgument("--nogui")
        }
        create("data") {
            data()
            gameDirectory = file("run")
            programArguments.addAll(
                "--mod", "peripheralworks", "--all", "--output", file("src/generated/resources").absolutePath,
                "--existing", rootProject.file("projects/core/src/main/resources").absolutePath,
                "--existing", file("src/main/resources").absolutePath,
            )
        }
        create("gameTestServer") {
            type = "gameTestServer"
            sourceSet = testMod
            // Saved fixture computers reboot before tests replace them; each run needs an isolated world.
            gameDirectory = gameTestRunDirectory
            systemProperties.putAll(testSystemProperties + fixtureSystemProperties)
            systemProperty("testiarium.tags", providers.gradleProperty("testiariumTags").orElse(if (minimalTestEnvironment) "peripheralworks,ae2,ae2-configurable-peripherals" else "peripheralworks").get())
            systemProperty("testiarium.gametest-report", layout.buildDirectory.file("test-results/peripheralworks-gametest.xml").get().asFile.absolutePath)
            jvmArgument("-ea")
            programArgument("--nogui")
            loadedMods.add(mods.named("peripheralworks_testmod"))
        }
        create("clientGameTest") {
            client()
            sourceSet = testMod
            gameDirectory = file("run/network-manager-client-gametest")
            systemProperties.putAll(testSystemProperties)
            systemProperty("testiarium.tags", providers.gradleProperty("testiariumTags").orElse("network-manager-client").get())
            systemProperty("testiarium.gametest-report", layout.buildDirectory.file("test-results/network-manager-client-gametest.xml").get().asFile.absolutePath)
            systemProperty("testiarium.screenshots", layout.buildDirectory.dir("screenshots/network-manager-client").get().asFile.absolutePath)
            jvmArguments.addAll("-ea", "-Dtestiarium.client=true", "-Dforge.disableEarlyProgressWindow=true")
            programArguments.addAll("--mixin.config", "testiarium-testmod.mixins.json")
            loadedMods.add(mods.named("peripheralworks_testmod"))
        }
        create("testClient") {
            client()
            sourceSet = testMod
            gameDirectory = file("run/test-client")
            systemProperties.putAll(testSystemProperties + fixtureSystemProperties)
            systemProperty("testiarium.tags", providers.gradleProperty("testiariumTags").orElse("peripheralworks,ae2-configurable-peripherals").get())
            jvmArgument("-ea")
            programArguments.addAll("--mixin.config", "testiarium-testmod.mixins.json")
            loadedMods.add(mods.named("peripheralworks_testmod"))
        }
    }
}

mixin {
    add(sourceSets.main.get(), "peripheralworks.refmap.json")
    config("peripheralworks.mixins.json")
}

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content { includeModule("curse.maven", "extra-hostile-neural-networks-1162278") }
    }
    mavenLocal()
    maven("https://thedarkcolour.github.io/KotlinForForge/") { content { includeGroup("thedarkcolour") } }
    maven("https://maven.blamejared.com/") { content { includeGroup("mezz.jei") } }
    maven("https://dl.cloudsmith.io/public/klikli-dev/mods/maven/") { content { includeGroup("com.klikli_dev") } }
    maven("https://maven.theillusivec4.top/") { content { includeGroup("top.theillusivec4.curios") } }
    maven("https://dl.cloudsmith.io/public/tslat/sbl/maven/") { content { includeGroup("net.tslat.smartbrainlib") } }
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") { content { includeGroup("com.eliotlash.mclib"); includeGroupByRegex("software\\.bernie.*") } }
    maven("https://maven.tterrag.com/") { content { includeGroup("com.tterrag.registrate"); includeGroup("com.jozufozu.flywheel") } }
    maven("https://maven.createmod.net") { content { includeGroup("com.simibubi.create"); includeGroup("net.createmod.ponder"); includeGroup("dev.engine-room.flywheel") } }
    maven("https://maven.latvian.dev/releases") { content { includeGroup("dev.latvian.mods"); includeGroup("dev.latvian.apps") } }
    maven("https://jitpack.io") { content { includeGroup("com.github.rtyley") } }
}

val obfuscation = extensions.getByType<ObfuscationExtension>()
val modTestModImplementation = obfuscation.createRemappingConfiguration(configurations.getByName(testMod.implementationConfigurationName))

dependencies {
    compileOnly(project(":core")) {
        exclude("cc.tweaked")
        exclude("fuzs.forgeconfigapiport")
        exclude("dan200.computercraft")
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    if (minimalTestEnvironment && gtceuTestEnvironment) {
        modImplementation(libs.gregetch)
        modImplementation(libs.ldlib)
    }
    listOf(libs.hostile.networks, libs.extra.hnn, libs.placebo).forEach { modCompileOnly(it) }
    if (!minimalTestEnvironment || neuralTestEnvironment != "none") {
        modRuntimeOnly(libs.hostile.networks)
        modRuntimeOnly(libs.placebo)
    }
    if (!minimalTestEnvironment || neuralTestEnvironment == "extra") {
        modRuntimeOnly(libs.extra.hnn)
    }
    implementation(libs.bundles.forge.raw)
    modImplementation(libs.bundles.forge.base)
    modImplementation(libs.bundles.forge.include)
    modRuntimeOnly(libs.bundles.externalMods.forge.runtime)

    jarJar(libs.bundles.forge.jjar) {
        isTransitive = false
    }

//    // WHY ?!?!?!
//    // Well, I didn't find any way to actually provide `configuration` information to
//    // a libs.version.toml, so I ended up with this garbabe of solution
    modCompileOnly("com.simibubi.create:create-1.20.1:6.0.6-150:all")
    modCompileOnly("com.tterrag.registrate:Registrate:MC1.20-1.3.3")
    modCompileOnly("net.createmod.ponder:Ponder-Forge-1.20.1:1.0.51")

    if (minimalTestEnvironment) {
        if (testWithoutAE2) {
            modCompileOnly(libs.ae2.forge)
        } else {
            modImplementation(libs.ae2.forge)
        }
    } else {
        modRuntimeOnly(libs.jade.forge)
        modCompileOnly(libs.bundles.externalMods.forge.integrations.full)
        modCompileOnly(libs.bundles.externalMods.forge.integrations.raw.full)
        modRuntimeOnly(libs.bundles.externalMods.forge.integrations.active)
        modRuntimeOnly(libs.bundles.externalMods.forge.integrations.raw.active)
        modRuntimeOnly(libs.bundles.externalMods.forge.integrations.activedep)
    }

    add(modTestModImplementation.name, libs.testiarium.forge)
    for (feature in listOf("test-mod", "cct-test-mod")) {
        add(modTestModImplementation.name, libs.testiarium.forge.get().toString()) {
            capabilities { requireCapability("site.siredvin:testiarium-forge-$feature") }
        }
    }
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") { source(project(":core").sourceSets.main.get().allSource) }
tasks.named<JavaCompile>("compileJava") { source(project(":core").sourceSets.main.get().allSource) }
tasks.processResources {
    from(project(":core").sourceSets.main.get().resources)
    val versions = mapOf(
        "version" to project.version,
        "file" to mapOf("jarVersion" to project.version),
        "forgeVersion" to libs.versions.forge.asProvider().get(),
        "computercraftVersion" to libs.versions.cc.tweaked.get(),
        "peripheraliumVersion" to libs.versions.peripheralium.asProvider().get(),
        "broccoliumVersion" to libs.versions.broccolium.asProvider().get(),
        "tweakiumVersion" to libs.versions.tweakium.asProvider().get(),
    )
    inputs.properties(versions)
    filesMatching("META-INF/mods.toml") { expand(versions) }
    exclude(".cache")
}
tasks.jar { manifest.attributes["MixinConfigs"] = "peripheralworks.mixins.json" }
extra["releaseJar"] = "reobfJar"

publishingShaking {
    shake()
}

tasks.withType<JavaExec>().configureEach {
    if (name == "runGameTestServer" && providers.gradleProperty("neuralTestDisabled").isPresent) {
        doFirst {
            val config = gameTestRunDirectory.get().asFile.resolve("config/peripheralworks.toml")
            config.parentFile.mkdirs()
            config.writeText("[integrations.hostilenetworks]\nenabled=false\n[integrations.extrahnn]\nenabled=false\n")
        }
    }
}

modPublishing {
    output.set(tasks.named<Jar>("reobfJar"))
    requiredDependencies.set(
        listOf(
            "cc-tweaked",
            "kotlin-for-forge",
        ),
    )
    shake()
}

// Buildenv and Minotaur only detect ForgeGradle; name the loader for the MDG legacy build.
modrinth { loaders.add("forge") }
tasks.withType<TaskPublishCurseForge>().configureEach {
    uploadArtifacts.forEach {
        it.addModLoader("Forge")
        it.addEnvironment("Client", "Server")
    }
}

val copyPowah by tasks.register<Copy>("copyPowah") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/powah"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/powah"))
}

val copyLanterns by tasks.register<Copy>("copyAdditionalLanterns") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/additionallanterns"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/additionallanterns"))
}

val copyAutomobility by tasks.register<Copy>("copyAutomobility") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/automobility"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/automobility"))
}

val copyCreate by tasks.register<Copy>("copyCreate") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/create"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/create"))
}

val copyKubeJS by tasks.register<Copy>("copyKubeJS") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/kubejs"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/kubejs"))
}

val fullCopy by tasks.register("fullCopy") {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyKubeJS)
}

tasks.compileKotlin {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyKubeJS)
}

tasks.spotlessJava {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyKubeJS)
}

tasks.spotlessKotlin {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyKubeJS)
}
