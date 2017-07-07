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
        override var id: String = "",
        var owner: String = "",
        var title: String = "",
        var description: String = "",
        var views: Int = 0,
        var favorits: Int = 0,
        var isFavorite: Boolean = false,
        var photocards: RealmList<Photocard> = RealmList(),
        override var updated: Date = Date(),
        override var active: Boolean = true


) : RealmObject(), Changeable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Album

        if (title != other.title) return false
        if (description != other.description) return false
        if (views != other.views) return false
        if (favorits != other.favorits) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + views
        result = 31 * result + favorits
        return result
    }
}
