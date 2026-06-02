plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "pr-zombie-hunter"

include(
    "gateway",
    "collector",
    "grader",
    "notifier",
    "graphql-service"
)