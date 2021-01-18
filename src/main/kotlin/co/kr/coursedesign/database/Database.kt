package co.kr.coursedesign.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

fun Database.Companion.connect(
    config: DatabaseConnectionConfig,
    setupConnection: (Connection) -> Unit = {},
    manager: (Database) -> TransactionManager = { ThreadLocalTransactionManager(it, DEFAULT_REPETITION_ATTEMPTS) }
): Database {
    return this.connect(
        url = config.url,
        driver = config.driver,
        user = config.user ?: "",
        password = config.password ?: "",
        setupConnection = setupConnection,
        manager = manager,
    )
}
