@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin)
}

val mavenGroup by properties

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    group = mavenGroup!!

    val javaVersion = JavaVersion.VERSION_17
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            sourceCompatibility = javaVersion.toString()
            targetCompatibility = javaVersion.toString()
            options.release.set(javaVersion.toString().toInt())
        }
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions { jvmTarget = javaVersion.toString() }
        }
    }
}

repositories {
    mavenCentral()
}

//idea.project.settings.compiler.javac {
//    // We want ErrorProne to be present when compiling via IntelliJ, as it offers some helpful warnings
//    // and errors. Loop through our source sets and find the appropriate flags.
//    moduleJavacAdditionalOptions = subprojects
//        .asSequence()
//        .map { evaluationDependsOn(it.path) }
//        .flatMap { project ->
//            val sourceSets = project.extensions.findByType(SourceSetContainer::class) ?: return@flatMap sequenceOf()
//            sourceSets.asSequence().map { sourceSet ->
//                val name = "${idea.project.name}.${project.name}.${sourceSet.name}"
//                val compile = project.tasks.named(sourceSet.compileJavaTaskName, JavaCompile::class).get()
//                name to compile.options.allCompilerArgs.joinToString(" ") { if (it.contains(" ")) "\"$it\"" else it }
//            }
//
//        }
//        .toMap()
//}
