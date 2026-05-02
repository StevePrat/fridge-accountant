plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "unspecified"

dependencies {
    implementation(project(":components:database-support"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}