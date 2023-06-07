plugins {
    `java-library`
}

dependencies {
    api(libs.annotations)

    testImplementation(platform("org.junit:junit-bom:${libs.versions.junit.get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
