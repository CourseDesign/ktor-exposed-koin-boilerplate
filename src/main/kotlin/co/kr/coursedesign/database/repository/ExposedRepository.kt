package co.kr.coursedesign.database.repository

import co.kr.coursedesign.database.converter.ConverterManager
import co.kr.coursedesign.database.converter.DefaultConvertManager
import co.kr.coursedesign.database.transaction.ExposedTransactionAdapter
import co.kr.coursedesign.database.transaction.Propagation
import co.kr.coursedesign.database.transaction.transaction
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import java.util.Optional
import kotlin.reflect.KClass

open class ExposedRepository<ID : Comparable<ID>, TABLE : IdTable<ID>>(
    private val table: TABLE,
    private val database: Database? = null,
) : Repository<ID>, ConverterManager<TABLE> by DefaultConvertManager() {
    private val queryFactory: ExposedQueryFactory<TABLE> = ExposedQueryFactory(table, this)

    override fun <T : Any> save(entity: T, kClass: KClass<T>): T = transaction {
        queryFactory.save(entity, kClass)
    }

    override fun <T : Any> saveAll(entities: Iterable<T>, kClass: KClass<T>): Iterable<T> = transaction {
        queryFactory.saveAll(entities, kClass)
    }

    override fun <T : Any> findById(id: ID, kClass: KClass<T>): T? = find({ table.id eq id }, kClass)

    override fun <T : Any> findAll(kClass: KClass<T>): Iterable<T> = transaction {
        queryFactory.findAll(kClass)
    }

    override fun <T : Any> findAllById(ids: Iterable<ID>, kClass: KClass<T>): Iterable<T> =
        findAll({ table.id inList ids }, kClass)

    fun <T : Any> find(where: SqlExpressionBuilder.() -> Op<Boolean>, kClass: KClass<T>): T? {
        val value = transaction {
            Optional.ofNullable(
                queryFactory.find(where, kClass)
            )
        }

        return when (value.isPresent) {
            true -> value.get()
            false -> null
        }
    }

    fun <T : Any> findAll(where: SqlExpressionBuilder.() -> Op<Boolean>, kClass: KClass<T>): Iterable<T> = transaction {
        queryFactory.findAll(where, kClass)
    }

    // TODO(최적화 하기)
    override fun existsById(id: ID) = exists { table.id eq id }

    fun exists(where: SqlExpressionBuilder.() -> Op<Boolean>): Boolean = transaction {
        queryFactory.exists(where)
    }

    fun count(where: SqlExpressionBuilder.() -> Op<Boolean>): Long = transaction {
        queryFactory.count(where)
    }

    override fun countAll(): Long = transaction {
        queryFactory.countAll()
    }

    override fun deleteById(id: ID): Int = delete { table.id eq id }

    override fun deleteAll(): Int = transaction {
        queryFactory.deleteAll()
    }

    fun delete(where: SqlExpressionBuilder.() -> Op<Boolean>): Int = transaction {
        queryFactory.delete(where)
    }

    override fun <P : Any, T : Any> updateById(id: ID, patch: P, kClass: KClass<T>): T =
        update({ table.id eq id }, patch, kClass)

    @Suppress("UNCHECKED_CAST")
    fun <P : Any, T : Any> update(where: SqlExpressionBuilder.() -> Op<Boolean>, patch: P, kClass: KClass<T>): T =
        transaction {
            queryFactory.update(where, patch, kClass)
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
