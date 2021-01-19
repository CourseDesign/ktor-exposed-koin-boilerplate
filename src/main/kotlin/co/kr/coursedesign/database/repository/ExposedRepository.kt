package co.kr.coursedesign.database.repository

import co.kr.coursedesign.database.converter.Converter
import co.kr.coursedesign.database.converter.ConverterManager
import co.kr.coursedesign.database.converter.DefaultConvertManager
import co.kr.coursedesign.database.transaction.ExposedTransactionAdapter
import co.kr.coursedesign.database.transaction.Propagation
import co.kr.coursedesign.database.transaction.transaction
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.Optional
import kotlin.reflect.KClass

open class ExposedRepository<ID : Comparable<ID>, TABLE : IdTable<ID>>(
    private val table: TABLE,
    private val database: Database? = null,
) : Repository<ID>, ConverterManager<TABLE> by DefaultConvertManager() {
    override fun <T : Any> save(entity: T, kClass: KClass<T>): T = transaction {
        val converter = fetchConverter(kClass)
        val result = table.insert(converter.serialize(entity))
            .resultedValues?.firstOrNull() ?: error("No key generated")
        converter.deserialize(result)
    }

    override fun <T : Any> saveAll(entities: Iterable<T>, kClass: KClass<T>): Iterable<T> = transaction {
        val repositoryTable = table
        val converter = fetchConverter(kClass)
        repositoryTable.batchInsert(entities) { converter.serialize(it)(repositoryTable, this) }
            .map(converter::deserialize)
    }

    override fun <T : Any> findById(id: ID, kClass: KClass<T>): T? = find({ table.id eq id }, kClass)

    override fun <T : Any> findAll(kClass: KClass<T>): Iterable<T> = transaction {
        val converter = fetchConverter(kClass)
        findFieldSet(converter)
            .selectAll()
            .map { converter.deserialize(it) }
    }

    override fun <T : Any> findAllById(ids: Iterable<ID>, kClass: KClass<T>): Iterable<T> = transaction {
        findAll({ table.id inList ids }, kClass)
    }

    fun <T : Any> find(where: SqlExpressionBuilder.() -> Op<Boolean>, kClass: KClass<T>): T? {
        val converter = fetchConverter(kClass)
        val value = transaction {
            Optional.ofNullable(
                findFieldSet(converter)
                    .select(where)
                    .limit(1)
                    .firstOrNull()
            )
        }.map { converter.deserialize(it) }

        return when (value.isPresent) {
            true -> value.get()
            false -> null
        }
    }

    fun <T : Any> findAll(where: SqlExpressionBuilder.() -> Op<Boolean>, kClass: KClass<T>): Iterable<T> = transaction {
        val converter = fetchConverter(kClass)
        findFieldSet(converter)
            .select(where)
            .map { converter.deserialize(it) }
    }

    private fun <T : Any> findFieldSet(converter: Converter<T, TABLE>): FieldSet = transaction {
        converter.requires?.let { table.slice(*it.toTypedArray()) } ?: table
    }

    // TODO(최적화 하기)
    override fun existsById(id: ID) = exists { table.id eq id }

    fun exists(where: SqlExpressionBuilder.() -> Op<Boolean>): Boolean = count(where) > 0

    fun count(where: SqlExpressionBuilder.() -> Op<Boolean>): Long = transaction {
        table.select(where)
            .count()
    }

    override fun countAll(): Long = transaction {
        table.selectAll()
            .count()
    }

    override fun deleteById(id: ID): Int = delete { table.id eq id }

    override fun deleteAll(id: ID): Int = transaction {
        table.deleteAll()
    }

    fun delete(where: SqlExpressionBuilder.() -> Op<Boolean>): Int = transaction {
        table.deleteWhere(op = where)
    }

    override fun <P : Any, T : Any> updateById(id: ID, patch: P, kClass: KClass<T>): T =
        update({ table.id eq id }, patch, kClass)

    @Suppress("UNCHECKED_CAST")
    fun <P : Any, T : Any> update(where: SqlExpressionBuilder.() -> Op<Boolean>, patch: P, kClass: KClass<T>): T =
        transaction {
            val converter = fetchConverter(patch::class as KClass<P>)
            table.update(where, body = converter.serialize(patch))

            find(where, kClass) ?: throw CantFindException()
        }

    fun <T : Any> transaction(statement: co.kr.coursedesign.database.transaction.Transaction.() -> T): T =
        transaction(Propagation.REQUIRED, statement)

    override fun <T : Any> transaction(
        propagation: Propagation,
        statement: co.kr.coursedesign.database.transaction.Transaction.() -> T,
    ): T =
        transaction(database, propagation) {
            ExposedTransactionAdapter(this).statement()
        }
}

inline fun <ID : Comparable<ID>, TABLE : IdTable<ID>, reified T : Any> ExposedRepository<ID, TABLE>.find(noinline where: SqlExpressionBuilder.() -> Op<Boolean>) = find(where, T::class)
inline fun <ID : Comparable<ID>, TABLE : IdTable<ID>, reified T : Any> ExposedRepository<ID, TABLE>.findAll(noinline where: SqlExpressionBuilder.() -> Op<Boolean>) = findAll(where, T::class)
inline fun <ID : Comparable<ID>, TABLE : IdTable<ID>, P : Any, reified T : Any> ExposedRepository<ID, TABLE>.update(noinline where: SqlExpressionBuilder.() -> Op<Boolean>, patch: P) = update(where, patch, T::class)
