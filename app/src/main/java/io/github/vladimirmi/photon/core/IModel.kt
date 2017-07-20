package io.github.vladimirmi.photon.core

import io.github.vladimirmi.photon.data.models.realm.Changeable
import io.github.vladimirmi.photon.utils.AppConfig
import java.util.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IModel {
    //todo return string
    fun getUpdated(obj: Changeable?): Date {
        var updated = obj?.updated ?: Date(0)
        if (updated.time != 0L && Date().time - AppConfig.UPDATE_TIMEOUT > updated.time) updated = Date(0)
        return updated
    }
}
