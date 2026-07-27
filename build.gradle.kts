@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.loom) apply false
    id("site.siredvin.root") version "0.9.0"
    id("site.siredvin.release") version "0.9.0"
    id("com.dorongold.task-tree") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

tasks.register("gameTest") {
    group = "verification"
    description = "Runs UnlimitedPeripheralWorks GameTests on NeoForge and Fabric."
    dependsOn(":forge:runGameTestServer", ":fabric:runPeripheralWorksGameTest")
}

tasks.register("clientGameTest") {
    group = "verification"
    description = "Runs network manager client GameTests on Forge and Fabric."
    dependsOn(":forge:runClientGameTest", ":fabric:runPeripheralWorksClientGameTest")
}

subprojectShaking {
    withKotlin.set(true)
    javaVersion.set(JavaVersion.VERSION_21)
    kotlinVersion.set("2.2.0")
}

val setupSubproject = subprojectShaking::setupSubproject


subprojects {
    if (name !in setOf("typed-peripheral-unlimitedperipheralworks", "typescript-tests")) {
        setupSubproject(this)
    }
    if (name == "core") {
        pluginManager.apply("net.fabricmc.fabric-loom-companion")
    }
}

githubShaking {
    modBranch.set("1.21")
    projectRepo.set("unlimitedperipheralworks")
    mastodonProjectName.set("UnlimitedPeripheralWorks")
    useForgeJarJar.set(true)
    shake()
}

repositories {
    maven("https://mvn.siredvin.site/minecraft") {
        name = "SirEdvin's Maven proxy"
    }
}
