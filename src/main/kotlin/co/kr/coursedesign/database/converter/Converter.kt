package co.kr.coursedesign.database.converter

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder

interface Converter<ENTITY : Any, TABLE : Table> {
    val requires: Collection<Expression<*>>?
        get() = null

    fun serialize(userObject: ENTITY): TABLE.(UpdateBuilder<*>) -> Unit
    fun deserialize(databaseObject: ResultRow): ENTITY
}
