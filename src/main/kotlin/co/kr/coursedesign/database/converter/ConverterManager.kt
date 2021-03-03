package co.kr.coursedesign.database.converter

import org.jetbrains.exposed.sql.Table
import kotlin.reflect.KClass

interface ConverterManager<TABLE : Table> {
    fun <T : Any> register(kClass: KClass<T>, converter: Converter<T, TABLE>): ConverterManager<TABLE>
    fun <T : Any> unregister(kClass: KClass<T>): ConverterManager<TABLE>

    fun <T : Any> fetchConverter(kClass: KClass<T>): Converter<T, TABLE>
    fun <T : Any> findConverter(kClass: KClass<T>): Converter<T, TABLE>?
}
