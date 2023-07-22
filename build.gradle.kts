@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.root") version "0.4.9"
    id("site.siredvin.release") version "0.4.9"
}

subprojectShaking {
    withKotlin.set(true)
    kotlinVersion.set("1.9.0")
}

val setupSubproject = subprojectShaking::setupSubproject


subprojects {
    setupSubproject(this)
}

githubShaking {
    modBranch.set("1.20")
    projectRepo.set("unlimitedperipheralworks")
    shake()
}

repositories {
    mavenCentral()
}