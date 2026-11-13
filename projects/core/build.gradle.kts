@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("site.siredvin.vanilla")
    id("site.siredvin.publishing")
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

spotless { kotlin { target("src/**/*.kt") } }

baseShaking {
    projectPart.set("common")
    integrationRepositories.set(true)
    shake()
}

vanillaShaking {
    accessWideners.add("src/main/resources/peripheralworks-common.accesswidener")
    accessWideners.add("src/main/resources/peripheralworks.accesswidener")
    shake()
}

val testMod = sourceSets.create("testMod") {
    compileClasspath += sourceSets.main.get().compileClasspath
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().output
}

val minimalTestEnvironment = providers.gradleProperty("minimalTestEnvironment").isPresent
if (minimalTestEnvironment) {
    sourceSets.main { kotlin.exclude("site/siredvin/peripheralworks/integrations/**") }
}

repositories {
    mavenLocal()
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.cccommon.minimal)
    compileOnly(libs.emi.common)
    api(libs.bundles.apicommon)
    compileOnly(libs.mixin)
    add(testMod.implementationConfigurationName, libs.testiarium.core)
    add(testMod.implementationConfigurationName, "site.siredvin:testiarium-core-1.21.1:${libs.versions.testiarium.get()}:test-mod@jar")
    add(testMod.implementationConfigurationName, "site.siredvin:testiarium-core-1.21.1:${libs.versions.testiarium.get()}:cct-test-mod@jar")
    add(testMod.compileOnlyConfigurationName, libs.bundles.cccommon)
}

tasks.named<ProcessResources>(testMod.processResourcesTaskName) {
    dependsOn(":typescript-tests:compileTestLua")
    from(project(":typescript-tests").layout.buildDirectory.dir("generated/test-lua")) {
        into("computer/tests")
    }
}
