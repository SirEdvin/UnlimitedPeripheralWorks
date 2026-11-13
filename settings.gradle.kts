pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://repo.repsy.io/siredvin/default") {
            name = "Repsy"
            content { includeGroupByRegex("site.siredvin.*") }
        }
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.parchmentmc.org/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
            if (requested.id.id.startsWith("site.siredvin.")) {
                useModule("site.siredvin:modding-buildenv:${requested.version}")
            }
        }
    }
}

val minecraftVersion: String by settings
rootProject.name = "UnlimitedPeripheralWorks $minecraftVersion"

include(":core")
include(":forge")
include(":fabric")
include(":typed-peripheral-unlimitedperipheralworks")
include(":typescript-tests")


for (project in rootProject.children) {
    project.projectDir = file("projects/${project.name}")
}
