package io.github.vladimirmi.photon.data.models.realm

import java.util.*

/**
 * Created by Vladimir Mikhalev 25.08.2017.
 */


interface Entity {
    var id: String
    var active: Boolean
    var updated: Date

    companion object {
        const val TEMP = "TEMP_ID_"
        fun tempId() = TEMP + UUID.randomUUID()
    }

    fun transform(): Entity?
}

fun String.isTemp() = contains(Entity.TEMP)