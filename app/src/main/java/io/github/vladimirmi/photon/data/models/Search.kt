package io.github.vladimirmi.photon.data.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Vladimir Mikhalev 09.06.2017.
 */

open class Search(
        @PrimaryKey var value: String = "",
        var date: Date = Date()
) : RealmObject()