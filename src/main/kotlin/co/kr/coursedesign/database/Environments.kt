package co.kr.coursedesign.database

import io.ktor.application.Application

val Application.envDatabaseUrl get() = environment.config.property("database.url").getString()
val Application.envDatabaseDriver get() = environment.config.property("database.driver").getString()
val Application.envDatabaseUser get() = environment.config.propertyOrNull("database.user")?.getString()
val Application.envDatabasePassword get() = environment.config.propertyOrNull("database.password")?.getString()
