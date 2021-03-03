package co.kr.coursedesign.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val name = varchar("name", length = 50)
    val cityId = integer("city_id").references(Cities.id).nullable()
}
