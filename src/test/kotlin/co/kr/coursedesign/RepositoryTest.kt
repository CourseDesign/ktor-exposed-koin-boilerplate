package co.kr.coursedesign

import co.kr.coursedesign.database.repository.findById
import co.kr.coursedesign.database.repository.save
import co.kr.coursedesign.database.repository.updateById
import co.kr.coursedesign.model.City
import co.kr.coursedesign.model.CityPatch
import co.kr.coursedesign.model.User
import co.kr.coursedesign.repository.CityRepository
import co.kr.coursedesign.repository.UserRepository
import co.kr.coursedesign.table.Cities
import co.kr.coursedesign.table.Users
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RepositoryTest {
    @Test
    fun testSave() {
        withTables(Users, Cities) {
            val userRepository = UserRepository(this.db)
            val cityRepository = CityRepository(this.db)

            val city = cityRepository.save(City("Test"))
            assertNotNull(city.id)

            val user = userRepository.save(User("Test", city.id))
            assertNotNull(user.id)
            assertEquals(user.cityId, city.id)
        }
    }

    @Test
    fun testFind() {
        withTables(Users, Cities) {
            val userRepository = UserRepository(this.db)
            val cityRepository = CityRepository(this.db)

            val city = cityRepository.save(City("Test"))
            val user = userRepository.save(User("Test", city.id))

            val existedCity: City? = cityRepository.findById(city.id!!)
            assertEquals(existedCity?.id, city.id)

            val existedUses: User? = userRepository.findById(user.id!!)
            assertEquals(existedUses?.id, user.id)
            assertEquals(existedUses?.cityId, existedCity?.id)
        }
    }

    @Test
    fun testExits() {
        withTables(Users, Cities) {
            val userRepository = UserRepository(this.db)
            val cityRepository = CityRepository(this.db)

            val city = cityRepository.save(City("Test"))
            val user = userRepository.save(User("Test", city.id))

            assert(cityRepository.existsById(city.id!!))
            assert(userRepository.existsById(user.id!!))

            assert(!cityRepository.existsById(city.id!! + 1))
            assert(!userRepository.existsById(user.id!! + 1))
        }
    }

    @Test
    fun testUpdate() {
        withTables(Users, Cities) {
            val cityRepository = CityRepository(this.db)

            val city = cityRepository.save(City("Test"))
            val newCity: City = cityRepository.updateById(
                city.id!!,
                CityPatch(
                    Optional.of("New")
                )
            )
            assertEquals(newCity.id, city.id)
            assertEquals(newCity.name, "New")
        }
    }

    @Test
    fun testDelete() {
        withTables(Users, Cities) {
            val cityRepository = CityRepository(this.db)

            val city = cityRepository.save(City("Test"))
            cityRepository.deleteById(city.id!!)
            assert(!cityRepository.existsById(city.id!!))
        }
    }
}
