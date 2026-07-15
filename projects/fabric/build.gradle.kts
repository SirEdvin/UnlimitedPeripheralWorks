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
    integrationRepositories.set(true)
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
        exclude("site/siredvin/peripheralworks/integrations/additionallanterns/**")
        exclude("site/siredvin/peripheralworks/integrations/ae2/**")
        exclude("site/siredvin/peripheralworks/integrations/alloy_forgery/**")
        exclude("site/siredvin/peripheralworks/integrations/create/**")
        exclude("site/siredvin/peripheralworks/integrations/modern_industrialization/**")
        exclude("site/siredvin/peripheralworks/integrations/naturescompass/**")
        exclude("site/siredvin/peripheralworks/integrations/powah/**")
        exclude("site/siredvin/peripheralworks/integrations/toms_storage/**")
        exclude("site/siredvin/peripheralworks/integrations/universal_shops/**")
    }
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
        name = "OwO maven"
        url = uri("https://maven.wispforest.io")
        content {
            includeGroup("io.wispforest")
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

    maven {
        name = "Jitpack for kubejs deps"
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.rtyley")
        }
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
