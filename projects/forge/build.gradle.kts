import site.siredvin.peripheralium.gradle.mavenDependencies

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin)
    id("net.minecraftforge.gradle") version "5.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
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
}

minecraft {
    val extractedLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    mappings("parchment", "${extractedLibs.findVersion("parchmentMc").get()}-${extractedLibs.findVersion("parchment").get()}-$minecraftVersion")

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
                "--mod", "peripheralium", "--all",
                "--output", file("src/generated/resources/"),
                "--existing", project(":core").file("src/main/resources/"),
                "--existing", file("src/main/resources/"),
            )
        }
    }
}

dependencies {
    val extractedLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${extractedLibs.findVersion("forge").get()}")
    implementation(libs.bundles.kotlin)

    compileOnly(project(":core")) {
        exclude("cc.tweaked")
        exclude("fuzs.forgeconfigapiport")
    }
    implementation(libs.bundles.forge.raw)
    implementation(fg.deobf("cc.tweaked:cc-tweaked-1.19.4-forge:${extractedLibs.findVersion("cc-tweaked").get()}"))

    libs.bundles.externalMods.forge.runtime.get().map { runtimeOnly(fg.deobf(it))}
}

sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
}

tasks {
    val extractedLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    val forgeVersion = extractedLibs.findVersion("forge").get()

    processResources {
        from(project(":core").sourceSets.main.get().resources)

        inputs.property("version", project.version)
        inputs.property("forgeVersion", forgeVersion)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("forgeVersion" to forgeVersion, "file" to mapOf("jarVersion" to project.version)))
        }
        exclude(".cache")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        source(project(":core").sourceSets.main.get().allSource)
    }
}

tasks.jar {
    finalizedBy("reobfJar")
    archiveClassifier.set("slim")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = base.archivesName.get()
            from(components["java"])
            fg.component(this)
            // jarJar.component is broken (https://github.com/MinecraftForge/ForgeGradle/issues/914), so declare the
            // artifact explicitly.
            artifact(tasks.jarJar)

            mavenDependencies {
                exclude(dependencies.create("site.siredvin:"))
                exclude(libs.jei.forge.get())

            }
        }
    }
}