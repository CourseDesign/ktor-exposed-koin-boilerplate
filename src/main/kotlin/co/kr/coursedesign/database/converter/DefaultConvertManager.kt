package co.kr.coursedesign.database.converter

import org.jetbrains.exposed.sql.Table
import kotlin.reflect.KClass

class DefaultConvertManager<TABLE : Table> : ConverterManager<TABLE> {
    private val converters = mutableMapOf<KClass<*>, Converter<*, TABLE>>()

    override fun <T : Any> register(kClass: KClass<T>, converter: Converter<T, TABLE>): ConverterManager<TABLE> = apply {
        converters[kClass] = converter
    }

    override fun <T : Any> unregister(kClass: KClass<T>): ConverterManager<TABLE> = apply {
        converters.remove(kClass)
    }

    override fun <T : Any> fetchConverter(kClass: KClass<T>): Converter<T, TABLE> {
        return findConverter(kClass) ?: throw CantFindConverterException()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> findConverter(kClass: KClass<T>): Converter<T, TABLE>? {
        return converters[kClass] as? Converter<T, TABLE>?
    }
}
