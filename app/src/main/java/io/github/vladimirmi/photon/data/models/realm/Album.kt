package io.github.vladimirmi.photon.data.models.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

open class Album(
        @PrimaryKey
        var id: String = "",
        var owner: String = "",
        var title: String = "",
        var description: String = "",
        var views: Int = 0,
        var favorits: Int = 0,
        var isFavorite: Boolean = false,
        var photocards: RealmList<Photocard> = RealmList(),
        var updated: Date = Date()
) : RealmObject(), java.io.Serializable