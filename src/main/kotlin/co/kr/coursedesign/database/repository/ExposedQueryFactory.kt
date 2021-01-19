package co.kr.coursedesign.database.repository

import co.kr.coursedesign.database.converter.Converter
import co.kr.coursedesign.database.converter.ConverterManager
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import kotlin.reflect.KClass

class ExposedQueryFactory<TABLE : Table>(
    private val table: TABLE,
    private val converterManager: ConverterManager<TABLE>,
) {
    fun <T : Any> save(entity: T, kClass: KClass<T>): T {
        val converter = converterManager.fetchConverter(kClass)
        val result = table.insert(converter.serialize(entity))
            .resultedValues?.firstOrNull() ?: error("No key generated")
        return converter.deserialize(result)
    }

    fun <T : Any> saveAll(entities: Iterable<T>, kClass: KClass<T>): Iterable<T> {
        val repositoryTable = table
        val converter = converterManager.fetchConverter(kClass)
        return repositoryTable.batchInsert(entities) { converter.serialize(it)(repositoryTable, this) }
            .map(converter::deserialize)
    }

    fun <T : Any> findAll(kClass: KClass<T>): Iterable<T> {
        val converter = converterManager.fetchConverter(kClass)
        return findFieldSet(converter)
            .selectAll()
            .map { converter.deserialize(it) }
    }

    fun <T : Any> find(where: SqlExpressionBuilder.() -> Op<Boolean>, kClass: KClass<T>): T? {
        val converter = converterManager.fetchConverter(kClass)
        return findFieldSet(converter)
            .select(where)
            .limit(1)
            .firstOrNull()
            ?.let { converter.deserialize(it) }
    }

    fun <T : Any> findAll(where: SqlExpressionBuilder.() -> Op<Boolean>, kClass: KClass<T>): Iterable<T> {
        val converter = converterManager.fetchConverter(kClass)
        return findFieldSet(converter)
            .select(where)
            .map { converter.deserialize(it) }
    }

    private fun <T : Any> findFieldSet(converter: Converter<T, TABLE>): FieldSet {
        return converter.requires?.let { table.slice(*it.toTypedArray()) } ?: table
    }

    fun exists(where: SqlExpressionBuilder.() -> Op<Boolean>): Boolean = count(where) > 0

    fun count(where: SqlExpressionBuilder.() -> Op<Boolean>): Long {
        return table.select(where)
            .count()
    }

    fun countAll(): Long {
        return table.selectAll()
            .count()
    }

    fun delete(where: SqlExpressionBuilder.() -> Op<Boolean>): Int {
        return table.deleteWhere(op = where)
    }

    fun deleteAll(): Int {
        return table.deleteAll()
    }

    @Suppress("UNCHECKED_CAST")
    fun <P : Any, T : Any> update(where: SqlExpressionBuilder.() -> Op<Boolean>, patch: P, kClass: KClass<T>): T {
        val converter = converterManager.fetchConverter(patch::class as KClass<P>)
        table.update(where, body = converter.serialize(patch))

        return find(where, kClass) ?: throw CantFindException()
    }
}
