package co.kr.coursedesign.database

import io.ktor.application.Application

data class DatabaseConnectionConfig(
    val url: String,
    val driver: String,
    val user: String? = null,
    val password: String? = null
) {
    companion object {
        fun from(application: Application) = DatabaseConnectionConfig(
            application.envDatabaseUrl,
            application.envDatabaseDriver,
            application.envDatabaseUser,
            application.envDatabasePassword
        )
    }
}
