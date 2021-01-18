package co.kr.coursedesign.database.transaction

interface Transaction {
    fun commit()
    fun rollback()
    fun close()
}
