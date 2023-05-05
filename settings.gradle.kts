pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net") {
            name = "Forge"
            content {
                includeGroup("net.minecraftforge")
                includeGroup("net.minecraftforge.gradle")
            }
        }

        maven("https://maven.parchmentmc.org") {
            name = "Librarian"
            content {
                includeGroupByRegex("^org\\.parchmentmc.*")
            }
        }

        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge"
            content {
                includeGroup("org.spongepowered")
                includeGroup("org.spongepowered.gradle.vanilla")
            }
        }

        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content {
                includeGroup("fabric-loom")
                includeGroup("net.fabricmc")
            }
        }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
}

val minecraftVersion: String by settings
rootProject.name = "UnlimitedPeripheralWorks $minecraftVersion"

include(":core")
include(":forge")
include(":fabric")


for (project in rootProject.children) {
    project.projectDir = file("projects/${project.name}")
}