package io.github.vladimirmi.photon.data.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

open class User(
        @PrimaryKey
        var id: String = "",
        var name: String = "",
        var login: String = "",
        var mail: String = "",
        var avatar: String = "",
        var token: String = "",
        var albumCount: Int = 0,
        var photocardCount: Int = 0,
        var albums: RealmList<Album> = RealmList()
) : RealmObject()
