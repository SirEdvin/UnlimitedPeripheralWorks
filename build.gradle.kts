@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.root") version "0.8.26"
    id("site.siredvin.release") version "0.8.26"
    id("com.dorongold.task-tree") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

tasks.register("gameTest") {
    group = "verification"
    description = "Runs UnlimitedPeripheralWorks GameTests on Forge and Fabric."
    dependsOn(":forge:runGameTestServer", ":fabric:runPeripheralWorksGameTest")
}

subprojectShaking {
    withKotlin.set(true)
    kotlinVersion.set("2.0.0")
}

val setupSubproject = subprojectShaking::setupSubproject


subprojects {
    if (name != "typescript-tests") {
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
    maven("https://mvn.siredvin.site/minecraft") {
        name = "SirEdvin's Maven proxy"
    }
}
