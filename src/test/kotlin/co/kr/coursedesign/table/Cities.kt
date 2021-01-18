package co.kr.coursedesign.table

import org.jetbrains.exposed.sql.Table

object Cities : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(id, name = "PK_Cities_ID")
}
