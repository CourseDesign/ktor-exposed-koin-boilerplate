package co.kr.coursedesign.repository

import co.kr.coursedesign.converter.UserConverter
import co.kr.coursedesign.database.repository.ExposedRepository
import co.kr.coursedesign.model.User
import co.kr.coursedesign.table.Users
import org.jetbrains.exposed.sql.Database

class UserRepository(database: Database? = null) : ExposedRepository<Int, Users>(Users, database) {
    init {
        register(User::class, UserConverter)
    }
}
