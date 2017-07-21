package io.github.vladimirmi.photon.core

import io.github.vladimirmi.photon.utils.AppConfig
import java.util.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

interface IModel {
    fun getUpdated(date: Date): String {
        val updated = if (date.time != 0L && Date().time - AppConfig.UPDATE_TIMEOUT > date.time) {
            Date(0)
        } else {
            date
        }
        return updated.toString()
    }
}
