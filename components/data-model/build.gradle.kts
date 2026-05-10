plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "unspecified"

dependencies {
    implementation(project(":components:database-support"))
}

kotlin {
    jvmToolchain(21)
}
