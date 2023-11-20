plugins {
    id("site.siredvin.root") version "0.5.5"
    id("site.siredvin.base") version "0.5.5"
    id("site.siredvin.linting") version "0.5.5"
    id("site.siredvin.release") version "0.5.5"
    id("site.siredvin.fabric") version "0.5.5"
    id("site.siredvin.publishing") version "0.5.5"
    id("site.siredvin.mod-publishing") version "0.5.5"
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

// java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

subprojectShaking {
    withKotlin.set(true)
}

subprojectShaking.setupSubproject(project)

baseShaking {
    projectPart.set("fabric")
    integrationRepositories.set(true)
    shake()
}

fabricShaking {
    commonProjectName.set("")
    createRefmap.set(true)
    accessWidener.set(file("src/main/resources/peripheralworks.accesswidener"))
    extraVersionMappings.set(
        mapOf(
            "computercraft" to "cc-restitched",
            "peripheralium" to "peripheralium",
        ),
    )
    shake()
}

repositories {
    maven {
        url = uri("https://squiddev.cc/maven")
    }
    maven {
        url = uri("https://maven.shedaniel.me/")
    }
    maven {
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.LlamaLad7")
            includeGroup("com.github.mattidragon")
            includeGroup("com.github.cc-tweaked")
            includeGroup("com.github.Technici4n")
            includeGroup("com.github.Draylar.omega-config")
            includeGroup("com.github.Chocohead")
            includeGroup("com.github.llamalad7.mixinextras")
        }
    }
    maven {
        url = uri("https://kneelawk.com/maven/")
        content {
            includeGroup("com.kneelawk")
        }
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        content {
            includeGroup("me.lucko")
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
        name = "Create, Porting Lib, Forge Tags, Milk Lib, Registrate"
        url = uri("https://mvn.devos.one/snapshots/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("io.github.fabricators_of_create")
            includeGroup("me.alphamode")
            includeGroup("com.tterrag.registrate_fabric")
            includeGroup("io.github.tropheusj")
            includeGroupByRegex("io\\.github\\.fabricators_of_create.*")
        }
    }

    maven {
        name = "ladysnake"
        url = uri("https://maven.ladysnake.org/releases")
        content {
            includeGroup("io.github.ladysnake")
        }
    }

    maven {
        name = "Flywheel"
        url = uri("https://maven.tterrag.com/")
        content {
            includeGroup("com.jozufozu.flywheel")
        }
    }
}

dependencies {
    implementation(libs.bundles.java)
    modImplementation(libs.bundles.fabric.core)
    modImplementation(libs.bundles.fabric)
    modImplementation(libs.bundles.ccfabric) {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
        exclude("me.shedaniel")
    }

    libs.bundles.externalMods.fabric.integrations.full.get().map { modCompileOnly(it) }
    libs.bundles.externalMods.fabric.integrations.active.get().map { modRuntimeOnly(it) }
    libs.bundles.externalMods.fabric.integrations.activedep.get().map { modRuntimeOnly(it) }

    // Some mod integrations

    modApi("teamreborn:energy:2.2.0") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    modImplementation("curse.maven:reborncore-237903:3958647") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }
    modImplementation("curse.maven:techreborn-233564:3958646") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }
//    modCompileOnly("curse.maven:lifts-451554:3770970") {
//        exclude(group="net.fabricmc.fabric-api")
//    }

    modImplementation("curse.maven:ae2-223794:4055594") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    modImplementation("com.github.Technici4n:magna:1.8.1-1.19") {
        exclude(group = "io.github.prospector")
        exclude(group = "me.shedaniel.cloth")
        exclude(group = "net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    modImplementation("io.github.ladysnake:PlayerAbilityLib:1.6.0") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:7.0.72") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    modImplementation("com.simibubi.create:create-fabric-$minecraftVersion:0.5.1-c-build.1160+mc1.19.2") {
        exclude("net.fabricmc", "fabric-loader")
    }

//    modRuntimeOnly("curse.maven:wthit-440979:3735869")
//    modRuntimeOnly("curse.maven:spark-361579:3644349")
    // For testing inventory logic
//    modRuntimeOnly("curse.maven:ExtendedDrawers-616602:3902207")
//    modRuntimeOnly("com.github.LlamaLad7:MixinExtras:0.0.10")
//    modRuntimeOnly("com.kneelawk:graphlib:0.2.4+1.18.2")
//    modRuntimeOnly("com.github.mattidragon:mconfig:1.2.0")

    // modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:8.1.449")
    // modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:8.1.449")

    modRuntimeOnly(libs.bundles.externalMods.fabric.runtime) {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    modApi("dev.architectury:architectury-fabric:6.5.85")

    testImplementation(kotlin("test"))
    testCompileOnly(libs.autoService)
    testAnnotationProcessor(libs.autoService)
    testImplementation(libs.byteBuddy)
    testImplementation(libs.byteBuddyAgent)
    testImplementation(libs.bundles.test)
}

githubShaking {
    modBranch.set("1.18")
    useForge.set(false)
    useFabric.set(false)
    useRoot.set(true)
    projectRepo.set("unlimitedperipheralworks")
    mastodonProjectName.set("UnlimitedPeripheralWorks")
    shake()
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
            "peripheralium",
        ),
    )
    requiredDependenciesCurseforge.add("forge-config-api-port-fabric")
    requiredDependenciesModrinth.add("forge-config-api-port")
    shake()
}
