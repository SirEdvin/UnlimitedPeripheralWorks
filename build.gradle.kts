@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.root") version "0.8.16"
    id("site.siredvin.release") version "0.8.16"
    id("com.dorongold.task-tree") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

subprojectShaking {
    withKotlin.set(true)
    javaVersion.set(JavaVersion.VERSION_21)
    kotlinVersion.set("2.0.0")
}

val setupSubproject = subprojectShaking::setupSubproject


subprojects {
    setupSubproject(this)
}

githubShaking {
    modBranch.set("1.20")
    projectRepo.set("unlimitedperipheralworks")
    mastodonProjectName.set("UnlimitedPeripheralWorks")
    shake()
}

repositories {
    mavenCentral()
}