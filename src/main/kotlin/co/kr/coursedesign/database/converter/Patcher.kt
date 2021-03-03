package co.kr.coursedesign.database.converter

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder

abstract class Patcher<ENTITY : Any, TABLE : Table> : Converter<ENTITY, TABLE> {
    final override val requires: Collection<Expression<*>>?
        get() = null

    final override fun serialize(userObject: ENTITY): TABLE.(UpdateBuilder<*>) -> Unit = patch(userObject)
    final override fun deserialize(databaseObject: ResultRow): ENTITY {
        TODO("Not yet implemented")
    }

    abstract fun patch(userObject: ENTITY): TABLE.(UpdateBuilder<*>) -> Unit
}
