package io.github.vladimirmi.photon.data.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

open class Album(
        @PrimaryKey
        var id: String = "",
        var owner: String = "",
        var title: String = "",
        var preview: String = "",
        var description: String = "",
        var views: Int = 0,
        var favorits: Int = 0,
        var photocards: RealmList<Photocard> = RealmList()
) : RealmObject()
