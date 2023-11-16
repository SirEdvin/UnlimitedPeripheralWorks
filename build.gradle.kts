plugins {
    id("site.siredvin.root") version "0.5.3"
    id("site.siredvin.base") version "0.5.3"
    id("site.siredvin.linting") version "0.5.3"
    id("site.siredvin.release") version "0.5.3"
    id("site.siredvin.fabric") version "0.5.3"
    id("site.siredvin.publishing") version "0.5.3"
    id("site.siredvin.mod-publishing") version "0.5.3"
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

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
    maven { url = uri("https://jitpack.io") }
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
        name = "Ladysnake Mods"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
        content {
            includeGroup("io.github.ladysnake")
            includeGroupByRegex("io\\.github\\.onyxstudios.*")
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
    }

    libs.bundles.externalMods.fabric.integrations.full.get().map { modCompileOnly(it) }
    libs.bundles.externalMods.fabric.integrations.active.get().map { modRuntimeOnly(it) }
    libs.bundles.externalMods.fabric.integrations.activedep.get().map { modRuntimeOnly(it) }

    // Some mod integrations

    modApi("teamreborn:energy:2.2.0") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modCompileOnly("curse.maven:reborncore-237903:3761010") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modCompileOnly("curse.maven:techreborn-233564:3761009") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modCompileOnly("curse.maven:lifts-451554:3770970") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modCompileOnly("curse.maven:ae2-223794:4023496") {
        exclude(group = "net.fabricmc.fabric-api")
    }
//
//    modImplementation("com.github.Technici4n:magna:1.7.1-1.18") {
//        exclude(group="io.github.prospector")
//        exclude(group="me.shedaniel.cloth")
//        exclude(group="net.fabricmc.fabric-api")
//    }
//
//    modImplementation("io.github.ladysnake:PlayerAbilityLib:1.5.0") {
//        exclude(group="net.fabricmc.fabric-api")
//    }
//
    modCompileOnly("com.simibubi.create:create-fabric-$minecraftVersion:0.5.0.i-999+1.18.2")

//    modRuntimeOnly("curse.maven:wthit-440979:3735869")
    modCompileOnly("curse.maven:spark-361579:3644349")
    // For testing inventory logic
//    modRuntimeOnly("curse.maven:ExtendedDrawers-616602:3902207")
    modCompileOnly("com.github.LlamaLad7:MixinExtras:0.0.10")
    modCompileOnly("com.kneelawk:graphlib:0.2.4+1.18.2")
    modCompileOnly("com.github.mattidragon:mconfig:1.2.0")

    modRuntimeOnly(libs.bundles.externalMods.fabric.runtime) {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    testImplementation(kotlin("test"))
    testCompileOnly(libs.autoService)
    testAnnotationProcessor(libs.autoService)
    testImplementation(libs.byteBuddy)
    testImplementation(libs.byteBuddyAgent)
    testImplementation(libs.bundles.test)
}

githubShaking {
    modBranch.set("1.18")
    shake()
}

publishingShaking {
    shake()
}

// modPublishing {
//    output.set(tasks.remapJar)
//    requiredDependencies.set(
//        listOf(
//            "cc-tweaked",
//            "fabric-language-kotlin",
//            "peripheralium",
//        ),
//    )
//    requiredDependenciesCurseforge.add("forge-config-api-port-fabric")
//    requiredDependenciesModrinth.add("forge-config-api-port")
//    shake()
// }
