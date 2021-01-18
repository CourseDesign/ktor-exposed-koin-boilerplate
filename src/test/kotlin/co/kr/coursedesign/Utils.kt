package co.kr.coursedesign

import co.kr.coursedesign.database.DatabaseConnectionConfig
import co.kr.coursedesign.database.connect
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

inline fun <T> withTables(vararg tables: Table, crossinline statement: Transaction.() -> T): T = withDatabase {
    transaction(this) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(*tables)
        val result = statement(this)
        SchemaUtils.drop(*tables)
        result
    }
}

inline fun <T> withDatabase(statement: Database.() -> T): T = withDatabase(
    DatabaseConnectionConfig(
        url = "jdbc:h2:mem:test-${UUID.randomUUID()}",
        driver = "org.h2.Driver"
    ),
    statement
)

inline fun <T> withDatabase(config: DatabaseConnectionConfig, statement: Database.() -> T): T {
    return statement(Database.connect(config))
}
