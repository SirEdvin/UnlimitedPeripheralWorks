@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.publishing")
    id("site.siredvin.mod-publishing")
    id("site.siredvin.forge")
}
val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

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

    libs.bundles.externalMods.forge.integrations.full.get().map { compileOnly(fg.deobf(it)) }
    libs.bundles.externalMods.forge.integrations.raw.full.get().map { compileOnly(it) }
    libs.bundles.externalMods.forge.integrations.active.get().map { runtimeOnly(fg.deobf(it)) }
    libs.bundles.externalMods.forge.integrations.raw.active.get().map { runtimeOnly(it) }
    libs.bundles.externalMods.forge.integrations.activedep.get().map { runtimeOnly(fg.deobf(it)) }
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
