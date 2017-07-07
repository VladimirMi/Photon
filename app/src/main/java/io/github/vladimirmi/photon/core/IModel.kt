package io.github.vladimirmi.photon.core

import io.github.vladimirmi.photon.data.models.realm.Changeable
import java.util.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IModel {
    fun getUpdated(obj: Changeable?): Date {
        val hour = 3600_000L // milliseconds
        var updated = obj?.updated ?: Date(0)
        if (updated.time != 0L && Date().time - hour > updated.time) updated = Date(0)
        return updated
    }
}
