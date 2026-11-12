@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    // Buildenv bundles an older Loom; CC:Tweaked 1.121.0 Fabric jars need 1.18+.
    alias(libs.plugins.loom) apply false
    id("net.neoforged.moddev.legacyforge") version "2.0.142" apply false
    id("site.siredvin.root") version "0.9.4"
    id("site.siredvin.release") version "0.9.4"
    id("com.dorongold.task-tree") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

tasks.register("gameTest") {
    group = "verification"
    description = "Runs UnlimitedPeripheralWorks GameTests on Forge and Fabric."
    dependsOn(":forge:runGameTestServer", ":fabric:runPeripheralWorksGameTest")
}

tasks.register("clientGameTest") {
    group = "verification"
    description = "Runs network manager client GameTests on Forge and Fabric."
    dependsOn(":forge:runClientGameTest", ":fabric:runPeripheralWorksClientGameTest")
}

subprojectShaking {
    withKotlin.set(true)
    kotlinVersion.set("2.0.0")
}

val setupSubproject = subprojectShaking::setupSubproject


subprojects {
    if (name !in setOf("typed-peripheral-unlimitedperipheralworks", "typescript-tests")) {
        setupSubproject(this)
    }
}

githubShaking {
    modBranch.set("1.20")
    projectRepo.set("unlimitedperipheralworks")
    mastodonProjectName.set("UnlimitedPeripheralWorks")
    useForgeJarJar.set(true)
    shake()
}

repositories {
    mavenCentral()
}
