package co.kr.coursedesign.table

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = varchar("id", 10)
    val name = varchar("name", length = 50)
    val cityId = (integer("city_id") references Cities.id).nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")
}
