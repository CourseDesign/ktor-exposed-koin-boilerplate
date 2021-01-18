package co.kr.coursedesign.database.transaction

class ExposedTransactionAdapter(
    private val exposedTransaction: org.jetbrains.exposed.sql.Transaction
) : Transaction {
    override fun commit() {
        exposedTransaction.commit()
    }

    override fun rollback() {
        exposedTransaction.rollback()
    }

    override fun close() {
        exposedTransaction.close()
    }
}
