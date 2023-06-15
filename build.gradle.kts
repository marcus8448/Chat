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

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(20))
        }
    }

    configure<org.cadixdev.gradle.licenser.LicenseExtension> {
        setHeader(rootProject.file("LICENSE_HEADER"))
        include("**/io/github/marcus8448/**/*.java")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
    }

    tasks.withType<JavaExec> {
        jvmArguments.add("--enable-preview")
    }
}
