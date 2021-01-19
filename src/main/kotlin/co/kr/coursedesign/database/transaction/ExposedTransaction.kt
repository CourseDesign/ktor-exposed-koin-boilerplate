package co.kr.coursedesign.database.transaction

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.inTopLevelTransaction
import org.jetbrains.exposed.sql.transactions.transactionManager

fun <T> transaction(vararg databases: Database, statement: Transaction.() -> T): T {
    return databases
        .map { datebase -> { statement: Transaction.() -> T -> transaction(db = datebase, statement = statement) } }
        .reduce { acc, cur -> { statement: Transaction.() -> T -> acc { cur(statement) } } }
        .let { it(statement) }
}

fun <T> transaction(
    db: Database? = null,
    propagation: Propagation = Propagation.REQUIRED,
    statement: Transaction.() -> T,
): T =
    transaction(
        db.transactionManager.defaultIsolationLevel,
        db.transactionManager.defaultRepetitionAttempts,
        propagation,
        db,
        statement
    )

fun <T> transaction(
    transactionIsolation: Int,
    repetitionAttempts: Int,
    propagation: Propagation,
    db: Database? = null,
    statement: Transaction.() -> T,
): T = when (propagation) {
    Propagation.MANDATORY -> mandatoryTransaction(db, statement)
    Propagation.NESTED -> nestedTransaction(transactionIsolation, repetitionAttempts, db, statement)
    Propagation.REQUIRED -> requireTransaction(transactionIsolation, repetitionAttempts, db, statement)
    Propagation.REQUIRES_NEW -> requireNewTransaction(transactionIsolation, repetitionAttempts, db, statement)
    else -> throw TransactionException("Not supported propagation type")
}

internal fun <T> mandatoryTransaction(
    db: Database? = null,
    statement: Transaction.() -> T,
): T = keepAndRestoreTransactionRefAfterRun(db) {
    val outer = TransactionManager.currentOrNull()

    if (outer != null && (db == null || outer.db == db)) {
        outer.statement()
    } else {
        val existingForDb = db?.transactionManager
        existingForDb?.currentOrNull()?.let { transaction ->
            val currentManager = outer?.db.transactionManager
            try {
                TransactionManager.resetCurrent(existingForDb)
                transaction.statement()
            } finally {
                TransactionManager.resetCurrent(currentManager)
            }
        } ?: throw TransactionException("Cant find current transaction")
    }
}

internal fun <T> nestedTransaction(
    transactionIsolation: Int,
    repetitionAttempts: Int,
    db: Database? = null,
    statement: Transaction.() -> T,
): T = keepAndRestoreTransactionRefAfterRun(db) {
    val outer = TransactionManager.currentOrNull()

    if (outer != null && (db == null || outer.db == db)) {
        val outerManager = outer.db.transactionManager
        val transaction = outerManager.newTransaction(transactionIsolation, outer)
        try {
            transaction.statement()
        } finally {
            TransactionManager.resetCurrent(outerManager)
        }
    } else {
        val existingForDb = db?.transactionManager
        existingForDb?.let { outerManager ->
            val currentManager = outer?.db.transactionManager
            val transaction = outerManager.newTransaction(transactionIsolation, outer)
            try {
                TransactionManager.resetCurrent(existingForDb)
                transaction.statement()
            } finally {
                TransactionManager.resetCurrent(currentManager)
            }
        } ?: inTopLevelTransaction(transactionIsolation, repetitionAttempts, db, null, statement)
    }
}

internal fun <T> requireTransaction(
    transactionIsolation: Int,
    repetitionAttempts: Int,
    db: Database? = null,
    statement: Transaction.() -> T,
): T = keepAndRestoreTransactionRefAfterRun(db) {
    val outer = TransactionManager.currentOrNull()

    if (outer != null && (db == null || outer.db == db)) {
        val outerManager = outer.db.transactionManager
        try {
            outer.statement()
        } finally {
            TransactionManager.resetCurrent(outerManager)
        }
    } else {
        val existingForDb = db?.transactionManager
        existingForDb?.currentOrNull()?.let { transaction ->
            val currentManager = outer?.db.transactionManager
            try {
                TransactionManager.resetCurrent(existingForDb)
                transaction.statement()
            } finally {
                TransactionManager.resetCurrent(currentManager)
            }
        } ?: inTopLevelTransaction(transactionIsolation, repetitionAttempts, db, null, statement)
    }
}

internal fun <T> requireNewTransaction(
    transactionIsolation: Int,
    repetitionAttempts: Int,
    db: Database? = null,
    statement: Transaction.() -> T,
): T = keepAndRestoreTransactionRefAfterRun(db) {
    val outer = TransactionManager.currentOrNull()

    if (outer != null && (db == null || outer.db == db)) {
        val outerManager = outer.db.transactionManager
        val transaction = outerManager.newTransaction(transactionIsolation, outer)
        try {
            transaction.statement().apply {
                transaction.commit()
            }
        } catch (exception: Exception) {
            transaction.rollback()
            throw exception
        } finally {
            TransactionManager.resetCurrent(outerManager)
        }
    } else {
        val existingForDb = db?.transactionManager
        existingForDb?.let { outerManager ->
            val currentManager = outer?.db.transactionManager
            val transaction = outerManager.newTransaction(transactionIsolation, outer)
            try {
                TransactionManager.resetCurrent(existingForDb)
                transaction.statement().apply {
                    transaction.commit()
                }
            } catch (exception: Exception) {
                transaction.rollback()
                throw exception
            } finally {
                TransactionManager.resetCurrent(currentManager)
            }
        } ?: inTopLevelTransaction(transactionIsolation, repetitionAttempts, db, null, statement)
    }
}

internal fun <T> keepAndRestoreTransactionRefAfterRun(db: Database? = null, block: () -> T): T {
    val manager = db.transactionManager
    val currentTransaction = manager.currentOrNull()
    return try {
        block()
    } finally {
        manager.bindTransactionToThread(currentTransaction)
    }
}
