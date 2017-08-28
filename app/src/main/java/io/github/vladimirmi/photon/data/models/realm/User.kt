package io.github.vladimirmi.photon.data.models.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Vladimir Mikhalev 14.06.2017.
 */

open class User(
        @PrimaryKey
        override var id: String = Synchronizable.tempId(),
        var name: String = "",
        var login: String = "",
        var avatar: String = "",
        var albumCount: Int = 0,
        var photocardCount: Int = 0,
        var albums: RealmList<Album> = RealmList(),
        override var updated: Date = Date(),
        override var active: Boolean = true,
        override var sync: Boolean = true,
        @Ignore var token: String = ""
) : RealmObject(), Synchronizable
