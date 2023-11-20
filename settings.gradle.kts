// pluginManagement {
//    repositories {
//        maven("https://maven.fabricmc.net") { name = "Fabric" }
//        mavenCentral()
//        gradlePluginPortal()
//    }
//    plugins {
//        val loomVersion: String by settings
//        id("fabric-loom").version(loomVersion)
//        val kotlinVersion: String by System.getProperties()
//        kotlin("jvm").version(kotlinVersion)
//    }
// }

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
}

val minecraftVersion: String by settings
rootProject.name = "UnlimitedPeripheralWorks $minecraftVersion"
