import org.jetbrains.changelog.date
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseUploadTask
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    id("fabric-loom")
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("org.jetbrains.changelog") version "1.3.1"

    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup

fun getenv(path: String = ".env"): Map<String, String> {
    val env = hashMapOf<String, String>()

    val file = File(path)
    if (file.exists()) {
        file.readLines().forEach { line ->
            val splitResult = line.split("=")
            if (splitResult.size > 1) {
                env[splitResult[0].trim()] = splitResult[1].trim()
            }
        }
    }

    return env
}

val secretEnv = getenv()
val curseforgeKey = secretEnv["CURSEFORGE_KEY"] ?: System.getenv("CURSEFORGE_KEY") ?: ""
val modrinthKey = secretEnv["MODRINTH_KEY"] ?: System.getenv("MODRINTH_KEY") ?: ""

loom {
    accessWidenerPath.set(file("src/main/resources/peripheralworks.accesswidener"))
    runs {
        create("datagen") {
            client()
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.modid=peripheralworks")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.strict-validation")
        }
    }
}


sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
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
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    mavenLocal()
}

dependencies {
    val minecraftVersion: String by project
    minecraft("com.mojang:minecraft:$minecraftVersion")
    val yarnMappings: String by project
    mappings(loom.officialMojangMappings())
    val loaderVersion: String by project
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    modImplementation("com.github.cc-tweaked:cc-restitched:v1.18.2-1.100.5-ccr")
    modImplementation("curse.maven:forgeconfigapirt-fabric-547434:3671141")
    modImplementation("siredvin.site:Peripheralium:0.1.0-SNAPSHOT")

    // Some mod integrations

    modApi("teamreborn:energy:2.2.0") {
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("curse.maven:reborncore-237903:3761010")  {
        exclude(group="net.fabricmc.fabric-api")
    }
    modImplementation("curse.maven:techreborn-233564:3761009") {
        exclude(group="net.fabricmc.fabric-api")
    }


    modRuntimeOnly("curse.maven:wthit-440979:3735869")
    modRuntimeOnly("curse.maven:spark-361579:3644349")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:8.1.449")
    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:8.1.449")
}

tasks {
    val javaVersion = JavaVersion.VERSION_17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }
    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}

changelog {
    version.set(modVersion)
    path.set("${project.projectDir}/CHANGELOG.md")
    header.set(provider { "[${version.get()}] - ${date()}" })
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf())
}

val CURSEFORGE_RELEASE_TYPE: String by project
val CURSEFORGE_ID: String by project
curseforge {
    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
    apiKey = curseforgeKey
    project(closureOf<CurseProject> {
        id = CURSEFORGE_ID
        releaseType = CURSEFORGE_RELEASE_TYPE
        addGameVersion("Fabric")
        addGameVersion("1.18.2")
        try {
            changelog = "${project.changelog.get(project.version as String).withHeader(false).toText()}"
            changelogType = "markdown"
        } catch (ignored: Exception) {
            changelog = "Seems not real release"
            changelogType = "markdown"
        }
        mainArtifact(tasks.remapJar.get().archivePath, closureOf<CurseArtifact> {
            displayName = "UnlimitedPeripheralWorks $version"
            relations(closureOf<CurseRelation> {
                requiredDependency("cc-restitched")
                requiredDependency("forge-config-api-port-fabric")
            })
        })
    })
}
project.afterEvaluate {
    tasks.getByName<CurseUploadTask>("curseforge${CURSEFORGE_ID}") {
        dependsOn(tasks.remapJar)
    }
}
