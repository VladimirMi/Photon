package io.github.vladimirmi.photon.data.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Developer Vladimir Mikhalev, 02.06.2017.
 */

open class Photocard(
        @PrimaryKey
        var id: String = "",
        var owner: String = "",
        var title: String = "",
        var photo: String = "",
        var views: Int = 0,
        var favorits: Int = 0,
        var filters: Filter = Filter(),
        var tags: RealmList<Tag> = RealmList(),
        @Ignore var album: String = ""
) : RealmObject() {
    fun withId(): Photocard {
        if (id.isEmpty()) id = UUID.randomUUID().toString()
        filters.generateId()
        return this
    }

    override fun toString(): String {
        return "Photocard(id='$id', owner='$owner', title='$title', photo='$photo', views=$views, favorits=$favorits, filters=$filters, tags=$tags, album=$album)"
    }


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

    override fun hashCode(): Int {
        var result = dish.hashCode()
        result = 31 * result + nuances.hashCode()
        result = 31 * result + decor.hashCode()
        result = 31 * result + temperature.hashCode()
        result = 31 * result + light.hashCode()
        result = 31 * result + lightDirection.hashCode()
        result = 31 * result + lightSource.hashCode()
        return result
    }

    override fun toString(): String {
        return "Filter(id='$id', dish='$dish', nuances='$nuances', decor='$decor', temperature='$temperature', light='$light', lightDirection='$lightDirection', lightSource='$lightSource')"
    }

    //endregion

}

open class Tag(@PrimaryKey var tag: String = "") : RealmObject() {
    //region =============== hash and equals ==============
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Tag

        if (tag != other.tag) return false
        return true
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
    //endregion
}
