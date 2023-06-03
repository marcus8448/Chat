plugins {
    alias(libs.plugins.licenser) apply(false)
}

group = project.property("project.group").toString()
version = project.property("project.version").toString()

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.cadixdev.licenser")

    group = rootProject.group
    version = rootProject.version

    configure<org.cadixdev.gradle.licenser.LicenseExtension> {
        setHeader(rootProject.file("LICENSE_HEADER"))
        include("**/io/github/marcus8448/**/*.java")
    }

    repositories {
        mavenCentral()
    }
}
