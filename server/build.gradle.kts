plugins {
    application
}

application {
    mainClass.set("io.github.marcus8448.chat.server.Main")
}

dependencies {
    implementation(libs.bundles.log4j)
    implementation(libs.nightconfig)
    implementation(libs.sqlite)
    implementation(project(":core"))
}
