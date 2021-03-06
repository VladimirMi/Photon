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
        override var id: String = Entity.tempId(),
        var owner: String = "",
        var title: String = "",
        var description: String = "",
        var views: Int = 0,
        var favorits: Int = 0,
        var isFavorite: Boolean = false,
        var photocards: RealmList<Photocard> = RealmList(),
        override var updated: Date = Date(),
        override var active: Boolean = true)
    : RealmObject(), Entity {

    override fun transform(): Album? =
            if (active) {
                photocards = photocards.mapNotNullTo(RealmList()) {
                    it.album = id
                    it.transform()
                }
                this
            } else null
}
