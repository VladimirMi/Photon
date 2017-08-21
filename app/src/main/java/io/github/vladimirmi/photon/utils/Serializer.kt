package io.github.vladimirmi.photon.utils

import java.io.*

/**
 * Created by Vladimir Mikhalev 21.08.2017.
 */

class Serializer {

    @Throws(IOException::class)
    fun serialize(any: Any?) = any?.let { obj ->
        ByteArrayOutputStream().use {
            ObjectOutputStream(it).writeObject(obj)
            it.toByteArray()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> deserialize(bytes: ByteArray) =
            ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as T }
}