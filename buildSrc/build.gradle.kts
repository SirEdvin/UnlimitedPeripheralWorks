// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0
//
// Shameless copy from CC:Tweaked, again

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

// Duplicated in settings.gradle.kts
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
        }
    }

    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
        content {
            includeGroup("net.fabricmc")
        }
    }
}

dependencies {
//    implementation(libs.errorProne.plugin)
//    implementation(libs.kotlin.plugin)
//    implementation(libs.spotless)
}