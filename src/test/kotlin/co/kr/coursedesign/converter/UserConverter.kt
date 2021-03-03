package co.kr.coursedesign.converter

import co.kr.coursedesign.database.converter.Converter
import co.kr.coursedesign.model.User
import co.kr.coursedesign.table.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object UserConverter : Converter<User, Users> {
    override fun serialize(userObject: User): Users.(UpdateBuilder<*>) -> Unit = {
        it[name] = userObject.name
        it[cityId] = userObject.cityId
    }

    override fun deserialize(databaseObject: ResultRow): User = User(
        databaseObject[Users.name],
        databaseObject[Users.cityId],
        databaseObject[Users.id].value,
    )
}
