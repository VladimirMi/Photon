package io.github.vladimirmi.photon.domain.mappers

import io.github.vladimirmi.photon.data.models.realm.Entity

/**
 * Created by Vladimir Mikhalev 05.09.2017.
 */

abstract class BaseCachingMapper<in T : Entity, out R : Any> {

    private val cache = HashMap<String, R>()
    fun get(id: String) = cache[id]

    fun map(list: List<T>): List<R> = list.mapNotNull { e ->
        checkAndMap(e)?.also { cache.put(e.id, it) }
    }

    private fun checkAndMap(it: T): R? =
            if (!it.active) {
                evict(it.id)
                null
            } else map(it)

    abstract fun map(it: T): R

    fun evict(id: String) {
        cache.remove(id)
    }

    fun evictAll() = cache.clear()
}