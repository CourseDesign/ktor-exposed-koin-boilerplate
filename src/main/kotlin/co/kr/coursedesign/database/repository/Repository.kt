package co.kr.coursedesign.database.repository

import co.kr.coursedesign.database.transaction.Transaction
import kotlin.reflect.KClass

interface Repository<ID : Comparable<ID>> {
    fun <T : Any> save(entity: T, kClass: KClass<T>): T
    fun <T : Any> saveAll(entities: Iterable<T>, kClass: KClass<T>): Iterable<T>

    fun <T : Any> findById(id: ID, kClass: KClass<T>): T?
    fun <T : Any> findAll(kClass: KClass<T>): Iterable<T>
    fun <T : Any> findAllById(ids: Iterable<ID>, kClass: KClass<T>): Iterable<T>

    fun existsById(id: ID): Boolean

    fun <P : Any, T : Any> updateById(id: ID, patch: P, kClass: KClass<T>): T

    fun deleteById(id: ID): Int
    fun deleteAll(id: ID): Int

    fun <T : Any> transaction(statement: Transaction.() -> T): T
}

inline fun <ID : Comparable<ID>, reified T : Any> Repository<ID>.save(entity: T) = save(entity, T::class)
inline fun <ID : Comparable<ID>, reified T : Any> Repository<ID>.saveAll(entities: Iterable<T>) = saveAll(entities, T::class)

inline fun <ID : Comparable<ID>, reified T : Any> Repository<ID>.findById(id: ID) = findById(id, T::class)
inline fun <ID : Comparable<ID>, reified T : Any> Repository<ID>.findAll() = findAll(T::class)
inline fun <ID : Comparable<ID>, reified T : Any> Repository<ID>.findAllById(ids: Iterable<ID>) = findAllById(ids, T::class)

inline fun <ID : Comparable<ID>, P : Any, reified T : Any> Repository<ID>.updateById(id: ID, patch: P) = updateById(id, patch, T::class)
