package io.github.vladimirmi.photon.data.models.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Developer Vladimir Mikhalev, 02.06.2017.
 */

open class Photocard(
        @PrimaryKey
        override var id: String = Entity.tempId(),
        var owner: String = "",
        var searchName: String = "",
        var title: String = "",
        var photo: String = "",
        var views: Int = 0,
        var favorits: Int = 0,
        var filters: Filter = Filter(),
        var tags: RealmList<Tag> = RealmList(),
        override var updated: Date = Date(),
        override var active: Boolean = true,
        var album: String = Entity.tempId())
    : RealmObject(), Entity {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Photocard

        if (owner != other.owner) return false
        if (title != other.title) return false
        if (photo != other.photo) return false
        if (filters != other.filters) return false
        if (tags != other.tags) return false
        if (album != other.album) return false

        return true
    }

    override fun hashCode(): Int {
        var result = owner.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + photo.hashCode()
        result = 31 * result + filters.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + album.hashCode()
        return result
    }

    override fun transform(): Photocard? =
            if (active) {
                searchName = title.toLowerCase()
                filters.generateId()
                this
            } else null
}

open class Filter(
        @PrimaryKey var id: String = "",
        var dish: String = "",
        var nuances: String = "",
        var decor: String = "",
        var temperature: String = "",
        var light: String = "",
        var lightDirection: String = "",
        var lightSource: String = ""
) : RealmObject() {

    fun generateId() {
        id = hashCode().toString()
    }


    //region =============== hash and equals ==============

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Filter
        if (dish != other.dish) return false
        if (nuances != other.nuances) return false
        if (decor != other.decor) return false
        if (temperature != other.temperature) return false
        if (light != other.light) return false
        if (lightDirection != other.lightDirection) return false
        if (lightSource != other.lightSource) return false

        return true
    }

    final override fun hashCode(): Int {
        var result = dish.hashCode()
        result = 31 * result + nuances.hashCode()
        result = 31 * result + decor.hashCode()
        result = 31 * result + temperature.hashCode()
        result = 31 * result + light.hashCode()
        result = 31 * result + lightDirection.hashCode()
        result = 31 * result + lightSource.hashCode()
        return result
    }

    //endregion

}

open class Tag(@PrimaryKey var value: String = "") : RealmObject() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tag

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
