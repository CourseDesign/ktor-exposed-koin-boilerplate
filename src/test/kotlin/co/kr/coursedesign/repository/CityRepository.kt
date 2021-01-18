package co.kr.coursedesign.repository

import co.kr.coursedesign.converter.CityConverter
import co.kr.coursedesign.database.repository.ExposedRepository
import co.kr.coursedesign.model.City
import co.kr.coursedesign.table.Cities
import org.jetbrains.exposed.sql.Database

class CityRepository(database: Database? = null) : ExposedRepository<Int, Cities>(Cities, database) {
    init {
        register(City::class, CityConverter)
    }
}
