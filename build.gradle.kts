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
    id("com.modrinth.minotaur") version "2.+"

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
val minecraftVersion: String by project

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
        create("data") {
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
    kotlin.exclude("**/integrations/lifts/**")
}

repositories {
    maven {
        url = uri("https://squiddev.cc/maven")
    }
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroupByRegex("com.github.*")
        }
    }
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
    maven {
        url = uri("https://repo.repsy.io/mvn/siredvin/default")
    }

    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
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
        url = uri("https://maven.ladysnake.org/releases")
        content {
            includeGroup("io.github.ladysnake")
            includeGroupByRegex("io\\.github\\.onyxstudios.*")
        }
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }

    maven {
        name = "Create, Porting Lib, Forge Tags, Milk Lib, Registrate"
        url = uri("https://mvn.devos.one/snapshots/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("io.github.fabricators_of_create")
            includeGroup("io.github.fabricators_of_create.Porting-Lib")
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
    maven("https://mvn.siredvin.site/minecraft") {
        name = "SirEdvin's Minecraft repository"
        content {
            includeGroup("siredvin.site")
            includeGroup("site.siredvin")
        }
    }
    maven("https://mvn.siredvin.site/snapshots") {
        name = "SirEdvin's Minecraft repository"
        content {
            includeGroup("siredvin.site")
            includeGroup("site.siredvin")
        }
    }

    mavenLocal()
}

dependencies {
    val fabricVersion: String by project
    val loaderVersion: String by project
    val yarnMappings: String by project
    val fabricKotlinVersion: String by project
    val peripheraliumVersion: String by project

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    modImplementation("com.github.cc-tweaked:cc-restitched:v1.19.1-1.101.2-ccr")
    modImplementation("curse.maven:forgeconfigapirt-fabric-547434:3960064")
    modImplementation("siredvin.site:peripheralium:${peripheraliumVersion}-${minecraftVersion}") {
        exclude(group="net.fabricmc.fabric-api")
    }

    // Some mod integrations

    modApi("teamreborn:energy:2.2.0") {
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("curse.maven:reborncore-237903:3958647")  {
        exclude(group="net.fabricmc.fabric-api")
    }
    modImplementation("curse.maven:techreborn-233564:3958646") {
        exclude(group="net.fabricmc.fabric-api")
    }
//    modCompileOnly("curse.maven:lifts-451554:3770970") {
//        exclude(group="net.fabricmc.fabric-api")
//    }

    modImplementation("curse.maven:ae2-223794:4055594") {
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("curse.maven:modernindust-405388:4673551") {
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("com.github.Technici4n:magna:1.8.1-1.19") {
        exclude(group="io.github.prospector")
        exclude(group="me.shedaniel.cloth")
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("io.github.ladysnake:PlayerAbilityLib:1.6.0") {
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:7.0.72") {
        exclude(group="net.fabricmc.fabric-api")
    }

    modImplementation("com.simibubi.create:create-fabric-${minecraftVersion}:0.5.0.i-991+1.19.2")


//    modRuntimeOnly("curse.maven:wthit-440979:3735869")
//    modRuntimeOnly("curse.maven:spark-361579:3644349")
    // For testing inventory logic
//    modRuntimeOnly("curse.maven:ExtendedDrawers-616602:3902207")
//    modRuntimeOnly("com.github.LlamaLad7:MixinExtras:0.0.10")
//    modRuntimeOnly("com.kneelawk:graphlib:0.2.4+1.18.2")
//    modRuntimeOnly("com.github.mattidragon:mconfig:1.2.0")

    // modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:8.1.449")
    // modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:8.1.449")

    testImplementation(kotlin("test"))
    testCompileOnly(libs.autoService)
    testAnnotationProcessor(libs.autoService)
    testImplementation(libs.byteBuddy)
    testImplementation(libs.byteBuddyAgent)
    testImplementation(libs.bundles.test)
}


tasks.test {
    dependsOn(tasks.generateDLIConfig)
    useJUnitPlatform()
    systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
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
        addGameVersion(minecraftVersion)
        try {
            changelog = "${project.changelog.get(project.version as String).withHeader(false).toText()}"
            changelogType = "markdown"
        } catch (ignored: Exception) {
            changelog = "Seems not real release"
            changelogType = "markdown"
        }
        mainArtifact(tasks.remapJar.get().archivePath, closureOf<CurseArtifact> {
            displayName = "UnlimitedPeripheralWorks $version $minecraftVersion"
            relations(closureOf<CurseRelation> {
                requiredDependency("cc-restitched")
                requiredDependency("forge-config-api-port-fabric")
                requiredDependency("peripheralium")
                requiredDependency("fabric-language-kotlin")
            })
        })
    })
}
project.afterEvaluate {
    tasks.getByName<CurseUploadTask>("curseforge${CURSEFORGE_ID}") {
        dependsOn(tasks.remapJar)
    }
}

val MODRINTH_ID: String by project
val MODRINTH_RELEASE_TYPE: String by project

modrinth {
    token.set(modrinthKey)
    projectId.set(MODRINTH_ID)
    versionNumber.set("${minecraftVersion}-${project.version}")
    versionName.set("UnlimitedPeripheralWorks ${minecraftVersion} ${version}")
    versionType.set(MODRINTH_RELEASE_TYPE)
    uploadFile.set(tasks.remapJar.get())
    gameVersions.set(listOf(minecraftVersion))
    loaders.set(listOf("fabric")) // Must also be an array - no need to specify this if you're using Loom or ForgeGradl
    try {
        changelog.set("${project.changelog.get(project.version as String).withHeader(false).toText()}")
    } catch (ignored: Exception) {
        changelog.set("")
    }
    dependencies {
        required.project("peripheralium")
    }
}

tasks.create("uploadMod") {
    dependsOn(tasks.modrinth, tasks.curseforge)
}