import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "7.1.0"
}

node {
    version.set("22.14.0")
    download.set(true)
    nodeProjectDir.set(projectDir)
    workDir.set(rootProject.layout.projectDirectory.dir(".gradle/nodejs"))
    npmWorkDir.set(rootProject.layout.projectDirectory.dir(".gradle/npm"))
    npmInstallCommand.set("ci")
}

val compileTestLua by tasks.registering(NpmTask::class) {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    inputs.files(fileTree(projectDir) {
        include("package.json", "package-lock.json", "tsconfig.json", "build.mjs", "src/**/*.ts")
        exclude("node_modules/**")
    })
    outputs.dir(layout.buildDirectory.dir("generated/test-lua"))
}
