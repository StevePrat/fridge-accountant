plugins {
    id("org.flywaydb.flyway") version "8.5.7"
}

val flywayMigration by configurations.creating
val postgresVersion: String by project

dependencies {
    flywayMigration("org.postgresql:postgresql:$postgresVersion")
}

flyway {
    configurations = arrayOf("flywayMigration")
}

tasks.register<org.flywaydb.gradle.task.FlywayMigrateTask>("devMigrate") {
    url = "jdbc:postgresql://localhost:5432/fridge_dev?user=fridge_user&password=fridge_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayCleanTask>("devClean") {
    url = "jdbc:postgresql://localhost:5432/fridge_dev?user=fridge_user&password=fridge_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayMigrateTask>("testMigrate") {
    url = "jdbc:postgresql://localhost:5432/fridge_test?user=fridge_user&password=fridge_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayCleanTask>("testClean") {
    url = "jdbc:postgresql://localhost:5432/fridge_test?user=fridge_user&password=fridge_password"
}