package co.kr.coursedesign.database.transaction

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> transaction(vararg databases: Database, statement: Transaction.() -> T): T {
    return databases
        .map { datebase -> { statement: Transaction.() -> T -> transaction(datebase, statement) } }
        .reduce { acc, cur -> { statement: Transaction.() -> T -> acc { cur(statement) } } }
        .let { it(statement) }
}
