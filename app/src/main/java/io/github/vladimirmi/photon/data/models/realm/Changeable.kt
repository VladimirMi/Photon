package io.github.vladimirmi.photon.data.models.realm

import java.util.*

/**
 * Created by Vladimir Mikhalev 28.06.2017.
 */

internal interface Changeable {
    var id: String
    var active: Boolean
    var updated: Date
}