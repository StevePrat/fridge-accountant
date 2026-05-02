plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "unspecified"

val rabbitVersion: String by rootProject

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.rabbitmq:amqp-client:$rabbitVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}