plugins {
    application
    alias(libs.plugins.javafx)
}

application {
    mainClass.set("io.github.marcus8448.chat.client.Main")
    mainModule.set("chat.client")
}

tasks.getByName<JavaExec>("run") {
    if (project.hasProperty("workDir")) {
        mkdir(projectDir.resolve(project.property("workDir").toString()))
        workingDir(projectDir.resolve(project.property("workDir").toString()))
    }
}

dependencies {
    implementation(libs.bundles.log4j)
    implementation(project(":core"))
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
}
