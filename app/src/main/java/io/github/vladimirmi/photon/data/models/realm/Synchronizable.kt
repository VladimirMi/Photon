package io.github.vladimirmi.photon.data.models.realm

import java.util.*

/**
 * Created by Vladimir Mikhalev 25.08.2017.
 */


interface Synchronizable {
    var id: String
    var sync: Boolean
    var active: Boolean
    var updated: Date

    val isTemp: Boolean
        get() = id.contains(TEMP)

    companion object {
        const val TEMP = "TEMP_ID_"
        fun tempId() = TEMP + UUID.randomUUID()
    }

    fun transform(): Synchronizable?
}

fun String.isTemp() = contains(Synchronizable.TEMP)