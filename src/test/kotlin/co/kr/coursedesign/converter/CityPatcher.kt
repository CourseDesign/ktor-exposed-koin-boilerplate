package co.kr.coursedesign.converter

import co.kr.coursedesign.database.converter.Patcher
import co.kr.coursedesign.model.CityPatch
import co.kr.coursedesign.table.Cities
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object CityPatcher : Patcher<CityPatch, Cities>() {
    override fun patch(userObject: CityPatch): Cities.(UpdateBuilder<*>) -> Unit = { table ->
        userObject.name.ifPresent { table[name] = it }
    }
}
