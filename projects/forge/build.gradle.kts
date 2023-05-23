import site.siredvin.peripheralium.gradle.mavenDependencies
import org.jetbrains.changelog.date
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseUploadTask
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin)
    id("net.minecraftforge.gradle") version "5.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    alias(libs.plugins.mixinGradle)
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("org.jetbrains.changelog") version "1.3.1"
    id("com.modrinth.minotaur") version "2.+"
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

base {
    archivesName.set("${modBaseName}-forge-${minecraftVersion}")
    version = modVersion
}

repositories {
    mavenCentral()
    mavenLocal()
    // For CC:T common code
    maven {
        url = uri("https://squiddev.cc/maven/")
        content {
            includeGroup("cc.tweaked")
            includeModule("org.squiddev", "Cobalt")
        }
    }
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
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }

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
        url = uri("https://api.modrinth.com/maven")
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven {
        url = uri("https://maven.shedaniel.me/")
        content {
            includeGroup("me.shedaniel.cloth")
            includeGroup("me.shedaniel")
        }
    }

    maven {
        url = uri("https://maven.architectury.dev/")
        content {
            includeGroup("dev.architectury")
        }
    }
}

minecraft {
    val extractedLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    mappings("parchment", "${extractedLibs.findVersion("parchmentMc").get()}-${extractedLibs.findVersion("parchment").get()}-$minecraftVersion")

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        all {
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")

            forceExit = false
        }

        val client by registering {
            workingDirectory(file("run"))
        }

        val server by registering {
            workingDirectory(file("run/server"))
            arg("--nogui")
        }

        val data by registering {
            workingDirectory(file("run"))
            args(
                "--mod", "peripheralworks", "--all",
                "--output", file("src/generated/resources/"),
                "--existing", project(":core").file("src/main/resources/"),
                "--existing", file("src/main/resources/"),
            )
        }
    }
}

mixin {
    add(sourceSets.main.get(), "peripheralworks.refmap.json")

    config("peripheralworks.mixins.json")
}

dependencies {
    val extractedLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${extractedLibs.findVersion("forge").get()}")
    implementation(libs.bundles.kotlin)

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    compileOnly(project(":core")) {
        exclude("cc.tweaked")
        exclude("fuzs.forgeconfigapiport")
    }
    implementation(libs.bundles.forge.raw)
    libs.bundles.forge.base.get().map { implementation(fg.deobf(it)) }
    libs.bundles.externalMods.forge.runtime.get().map { runtimeOnly(fg.deobf(it))}

    libs.bundles.externalMods.forge.integrations.full.get().map { compileOnly(fg.deobf(it)) }
    libs.bundles.externalMods.forge.integrations.active.get().map { runtimeOnly(fg.deobf(it)) }
    libs.bundles.externalMods.forge.integrations.activedep.get().map { runtimeOnly(fg.deobf(it)) }
}

sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
}

tasks {
    val extractedLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    val forgeVersion = extractedLibs.findVersion("forge").get()
    val computercraftVersion = extractedLibs.findVersion("cc-tweaked").get()
    val peripheraliumVersion = extractedLibs.findVersion("peripheralium").get()

    processResources {
        from(project(":core").sourceSets.main.get().resources)

        inputs.property("version", project.version)
        inputs.property("forgeVersion", forgeVersion)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf(
                "forgeVersion" to forgeVersion,
                "file" to mapOf("jarVersion" to project.version),
                "computercraftVersion" to computercraftVersion,
                "peripheraliumVersion" to peripheraliumVersion
            ))
        }
        exclude(".cache")
    }
    withType<JavaCompile> {
        if (name != "testJavaCompile") {
            source(project(":core").sourceSets.main.get().allSource)
        }
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        source(project(":core").sourceSets.main.get().allSource)
    }
}

tasks.jar {
    finalizedBy("reobfJar")
    archiveClassifier.set("")
}

tasks.jarJar {
    finalizedBy("reobfJarJar")
    archiveClassifier.set("jarjar")
}

val rootProjectDir: File by extra

changelog {
    version.set(modVersion)
    path.set("${rootProjectDir}/CHANGELOG.md")
    header.set(provider { "[${version.get()}] - ${date()}" })
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf())
}


val CURSEFORGE_RELEASE_TYPE: String by extra
val CURSEFORGE_ID: String by extra
val curseforgeKey: String by extra

curseforge {
    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
    apiKey = curseforgeKey
    project(closureOf<CurseProject> {
        id = CURSEFORGE_ID
        releaseType = CURSEFORGE_RELEASE_TYPE
        addGameVersion("Forge")
        addGameVersion(minecraftVersion)
        try {
            changelog = "${project.changelog.get(project.version as String).withHeader(false).toText()}"
            changelogType = "markdown"
        } catch (ignored: Exception) {
            changelog = "Seems not real release"
            changelogType = "markdown"
        }
        mainArtifact(tasks.jar.get().archivePath, closureOf<CurseArtifact> {
            displayName = "UnlimitedPeripheralWorks $version for $minecraftVersion"
            relations(closureOf<CurseRelation> {
                requiredDependency("peripheralium")
                requiredDependency("cc-tweaked")
                requiredDependency("kotlin-for-forge")
            })
        })
    })
}
project.afterEvaluate {
    tasks.getByName<CurseUploadTask>("curseforge${CURSEFORGE_ID}") {
        dependsOn(tasks.jar)
    }
}


val MODRINTH_ID: String by extra
val MODRINTH_RELEASE_TYPE: String by extra
val modrinthKey: String by extra

modrinth {
    token.set(modrinthKey)
    projectId.set(MODRINTH_ID)
    versionNumber.set("${minecraftVersion}-${project.version}")
    versionName.set("UnlimitedPeripheralWorks ${version} for ${minecraftVersion}")
    versionType.set(MODRINTH_RELEASE_TYPE)
    uploadFile.set(tasks.jar.get())
    gameVersions.set(listOf(minecraftVersion))
    loaders.set(listOf("forge")) // Must also be an array - no need to specify this if you're using Loom or ForgeGradl
    try {
        changelog.set("${project.changelog.get(project.version as String).withHeader(false).toText()}")
    } catch (ignored: Exception) {
        changelog.set("")
    }
    dependencies {
        required.project("peripheralium")
        required.project("kotlin-for-forge")
        required.project("cc-tweaked")
    }
}

tasks.create("uploadMod") {
    dependsOn(tasks.modrinth, tasks.curseforge)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = base.archivesName.get()
            from(components["java"])
            fg.component(this)

            mavenDependencies {
                exclude(dependencies.create("site.siredvin:"))
            }
        }
    }
}