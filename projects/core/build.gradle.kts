@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.vanillaGradle)
    alias(libs.plugins.kotlin)
    idea
}

val modVersion: String by extra
val minecraftVersion: String by extra
val modBaseName: String by extra

base {
    archivesName.set("${modBaseName}-common-${minecraftVersion}")
    version = modVersion
}

sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
}

minecraft {
    version(minecraftVersion)
    accessWideners(
        "src/main/resources/peripheralworks.accesswidener"
    )
}

repositories {
    mavenCentral()
    mavenLocal()
    // For CC:T common code
    maven {
        url = uri("https://squiddev.cc/maven/")
        content {
            includeGroup("cc.tweaked")
            includeModule("org.squiddev", "Cobalt")
        }
    }
    // For Forge configuration common code
    maven {
        name = "Fuzs Mod Resources"
        url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        content {
            includeGroup("fuzs.forgeconfigapiport")
        }
    }
}


dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.cccommon)
    api(libs.bundles.apicommon)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}