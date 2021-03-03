package co.kr.coursedesign.converter

import co.kr.coursedesign.database.converter.Converter
import co.kr.coursedesign.model.City
import co.kr.coursedesign.table.Cities
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object CityConverter : Converter<City, Cities> {
    override fun serialize(userObject: City): Cities.(UpdateBuilder<*>) -> Unit = {
        it[name] = userObject.name
    }

    override fun deserialize(databaseObject: ResultRow): City = City(
        databaseObject[Cities.name],
        databaseObject[Cities.id].value,
    )
}
