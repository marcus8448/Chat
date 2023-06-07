plugins {
    `java-library`
}

dependencies {
    api(libs.annotations)
    api(libs.gson)

    implementation(libs.log4j.api)
    testImplementation(platform("org.junit:junit-bom:${libs.versions.junit.get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
