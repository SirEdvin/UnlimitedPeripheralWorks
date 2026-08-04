import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.artifacts.ExternalModuleDependency

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.publishing")
    id("site.siredvin.mod-publishing")
    id("site.siredvin.forge")
}
val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra
val minimalTestEnvironment = providers.gradleProperty("minimalTestEnvironment").isPresent

baseShaking {
    projectPart.set("forge")
    integrationRepositories.set(false)
    shake()
}

forgeShaking {
    commonProjectName.set("core")
    useAT.set(true)
    useMixins.set(true)
    useJarJar.set(true)
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

if (minimalTestEnvironment) {
    val excludedIntegrations = file("src/main/kotlin/site/siredvin/peripheralworks/integrations").listFiles()!!
        .filter { it.isDirectory && it.name != "ae2" }
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

repositories {
    mavenLocal()
    maven {
        name = "SirEdvin's Maven proxy"
        url = uri("https://mvn.siredvin.site/minecraft")
    }
}

dependencies {
    implementation(libs.bundles.forge.raw)
    libs.bundles.forge.base.get().map { implementation(fg.deobf(it)) }
    libs.bundles.forge.include.get().map { implementation(fg.deobf(it)) }
    libs.bundles.externalMods.forge.runtime.get().map { runtimeOnly(fg.deobf(it)) }

    jarJar(libs.bundles.forge.jjar) {
        isTransitive = false
    }

//    // WHY ?!?!?!
//    // Well, I didn't find any way to actually provide `configuration` information to
//    // a libs.version.toml, so I ended up with this garbabe of solution
    compileOnly(fg.deobf("com.simibubi.create:create-1.20.1:6.0.6-150:all"))
    compileOnly(fg.deobf("com.tterrag.registrate:Registrate:MC1.20-1.3.3"))
//    runtimeOnly(fg.deobf("com.simibubi.create:create-1.20.1:6.0.0-84:all"))
    compileOnly(fg.deobf("net.createmod.ponder:Ponder-Forge-1.20.1:1.0.51"))

    if (minimalTestEnvironment) {
        implementation(fg.deobf(libs.ae2.forge.get()))
    } else {
        libs.bundles.externalMods.forge.integrations.full.get().map { compileOnly(fg.deobf(it)) }
        libs.bundles.externalMods.forge.integrations.raw.full.get().map { compileOnly(it) }
        libs.bundles.externalMods.forge.integrations.active.get().map { runtimeOnly(fg.deobf(it)) }
        libs.bundles.externalMods.forge.integrations.raw.active.get().map { runtimeOnly(it) }
        libs.bundles.externalMods.forge.integrations.activedep.get().map { runtimeOnly(fg.deobf(it)) }
    }

    listOf(
        "site.siredvin:testiarium-forge-1.20.1:0.1.1",
        "site.siredvin:testiarium-forge-1.20.1:0.1.1:cct-test-mod@jar",
    ).forEach { notation ->
        add(
            testMod.implementationConfigurationName,
            fg.deobf((project.dependencies.create(notation) as ExternalModuleDependency).apply { isTransitive = false }),
        )
    }
    add(testMod.implementationConfigurationName, fg.deobf("site.siredvin:testiarium-forge-1.20.1:0.1.1:test-mod@jar"))
}

minecraft {
    runs {
        create("gameTestServer") {
            workingDirectory(file("run/peripheralworks-gametest"))
            property("forge.enabledGameTestNamespaces", "peripheralworks_testmod")
            property("testiarium.tags", providers.gradleProperty("testiariumTags").orElse("peripheralworks").get())
            property("testiarium.structures", project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath)
            property("testiarium.fixture-source", project(":core").file("src/testMod/resources/gameteststructures").absolutePath)
            property("testiarium.cct-fixtures", project(":core").layout.buildDirectory.dir("resources/testMod/computer").get().asFile.absolutePath)
            property("testiarium.gametest-report", layout.buildDirectory.file("test-results/peripheralworks-gametest.xml").get().asFile.absolutePath)
            jvmArgs("-ea")
            args("--nogui")
            mods {
                create("peripheralworks") { source(sourceSets.main.get()) }
                create("peripheralworks_testmod") {
                    source(testMod)
                    source(project(":core").sourceSets["testMod"])
                }
            }
        }
        create("clientGameTest") {
            parent(runs.getByName("client"))
            workingDirectory(file("run/network-manager-client-gametest"))
            property("forge.enabledGameTestNamespaces", "peripheralworks_testmod")
            property("testiarium.tags", "network-manager-client")
            property("testiarium.structures", project(":core").layout.buildDirectory.dir("resources/testMod/gameteststructures").get().asFile.absolutePath)
            property("testiarium.gametest-report", layout.buildDirectory.file("test-results/network-manager-client-gametest.xml").get().asFile.absolutePath)
            property("testiarium.screenshots", layout.buildDirectory.dir("screenshots/network-manager-client").get().asFile.absolutePath)
            jvmArgs("-ea", "-Dtestiarium.client=true", "-Dforge.disableEarlyProgressWindow=true")
            args("--mixin.config", "testiarium-testmod.mixins.json")
            mods {
                create("peripheralworks") { source(sourceSets.main.get()) }
                create("peripheralworks_testmod") {
                    source(testMod)
                    source(project(":core").sourceSets["testMod"])
                }
            }
        }
    }
}

publishingShaking {
    shake()
}

modPublishing {
    output.set(tasks.jarJar)
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

// TODO: make this possible, probably (?) This would be really nice
val copyAE2 by tasks.register<Copy>("copyAE2") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/ae2"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/ae2"))
}

val fullCopy by tasks.register("fullCopy") {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyKubeJS, copyAE2)
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
