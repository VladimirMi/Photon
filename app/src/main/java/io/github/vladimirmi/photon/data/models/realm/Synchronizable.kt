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

    fun isTemp() = id.startsWith(TEMP)

    companion object {
        const val TEMP = "TEMP"
        fun tempId() = TEMP + UUID.randomUUID()
    }
}