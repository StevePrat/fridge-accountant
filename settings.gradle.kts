rootProject.name = "fridge-accountant"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

include(
    "applications:basic-server",
    "applications:data-analyzer-server",
    "applications:data-collector-server",

    "components:data-collector",
    "components:data-analyzer",

    "support:logging-support",
    "support:workflow-support"
)

include("components:rabbit-support")
include("components:database-support")
include("components:data-model")
include("databases")
include("databases:fridge-db")