pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://mvn.siredvin.site/minecraft") {
            name = "SirEdvin's Minecraft repository"
            content {
                includeGroup("net.minecraftforge")
                includeGroup("net.minecraftforge.gradle")
                includeGroup("org.parchmentmc")
                includeGroup("org.parchmentmc.feather")
                includeGroup("org.parchmentmc.data")
                includeGroup("org.spongepowered")
                includeGroup("org.spongepowered.gradle.vanilla")
                includeGroup("net.fabricmc")
                includeGroup("fabric-loom")
                includeGroup("site.siredvin")
                includeGroupByRegex("site.siredvin.*")
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