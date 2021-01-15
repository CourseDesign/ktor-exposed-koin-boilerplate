rootProject.name = "ktor"

pluginManagement {
    val klint_version: String by settings

    plugins {
        id("org.jlleitschuh.gradle.ktlint") version klint_version
    }
}
