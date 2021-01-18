package co.kr.coursedesign

import co.kr.coursedesign.table.Cities
import co.kr.coursedesign.table.Users
import org.jetbrains.exposed.sql.insert
import kotlin.test.Test

class DatabaseTest {
    @Test
    fun testConnectDatabase() {
        withDatabase {
        }
    }

    @Test
    fun testInsert() {
        withTables(Users, Cities) {
            val saintPetersburgId = Cities.insert {
                it[name] = "St. Petersburg"
            } get Cities.id
            Users.insert {
                it[id] = "andrey"
                it[name] = "Andrey"
                it[cityId] = saintPetersburgId
            }
        }
    }
}
