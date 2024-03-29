plugins {
    application
    alias(libs.plugins.javafx)
}

application {
    mainClass.set("io.github.marcus8448.chat.client.Main")
    mainModule.set("chat.client")
}

dependencies {
    implementation(libs.bundles.log4j)
    implementation(project(":core"))
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
}
