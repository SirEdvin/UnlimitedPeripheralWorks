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
    integrationRepositories.set(true)
    shake()
}

forgeShaking {
    commonProjectName.set("core")
    useAT.set(true)
    useMixins.set(true)
    extraVersionMappings.set(
        mapOf(
            "computercraft" to "cc-tweaked",
            "peripheralium" to "peripheralium",
        ),
    )
    shake()
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
        }
    }
    maven {
        name = "tterrag maven"
        url = uri("https://maven.tterrag.com/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("com.tterrag.registrate")
            includeGroup("com.jozufozu.flywheel")
        }
    }
}

dependencies {
    implementation(libs.bundles.forge.raw)
    libs.bundles.forge.base.get().map { implementation(fg.deobf(it)) }
    libs.bundles.forge.include.get().map { implementation(fg.deobf(it)) }
    libs.bundles.externalMods.forge.runtime.get().map { runtimeOnly(fg.deobf(it)) }

    // WHY ?!?!?!
    // Well, I didn't find any way to actually provide `configuration` information to
    // a libs.version.toml, so I ended up with this garbabe of solution
    compileOnly(fg.deobf("com.simibubi.create:create-1.20.1:0.5.1.f-26:all"))
    runtimeOnly(fg.deobf("com.simibubi.create:create-1.20.1:0.5.1.f-26:all"))

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
    output.set(tasks.jar)
    requiredDependencies.set(
        listOf(
            "cc-tweaked",
            "kotlin-for-forge",
            "peripheralium",
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

// TODO: make this possible, probably (?) This would be really nice
val copyAE2 by tasks.register<Copy>("copyAE2") {
    from(project(":fabric").file("src/main/kotlin/site/siredvin/peripheralworks/integrations/ae2"))
    into(project.file("src/main/kotlin/site/siredvin/peripheralworks/integrations/ae2"))
}

val fullCopy by tasks.register("fullCopy") {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyCreate, copyAE2)
}

tasks.compileKotlin {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyCreate)
}

tasks.spotlessJava {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyCreate)
}

tasks.spotlessKotlin {
    dependsOn(copyPowah, copyLanterns, copyAutomobility, copyCreate)
}
