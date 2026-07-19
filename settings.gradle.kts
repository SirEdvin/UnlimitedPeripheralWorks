pluginManagement {
    repositories {
        maven("https://mvn.siredvin.site/minecraft") {
            name = "SirEdvin's Maven proxy"
        }
        gradlePluginPortal {
            content { includeGroup("com.github.node-gradle.node") }
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
include(":typescript-tests")


for (project in rootProject.children) {
    project.projectDir = file("projects/${project.name}")
}
