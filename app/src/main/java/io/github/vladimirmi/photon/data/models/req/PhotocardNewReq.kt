package io.github.vladimirmi.photon.data.models.req

import io.github.vladimirmi.photon.data.models.realm.Filter
import io.github.vladimirmi.photon.data.models.realm.Photocard

/**
 * Created by Vladimir Mikhalev 25.08.2017.
 */


class PhotocardNewReq(photocard: Photocard) {
    @Transient
    var id: String = photocard.id
    var album: String = photocard.album
    var title: String = photocard.title
    var photo: String = photocard.photo
    var tags: List<String> = photocard.tags.map { it.value }
    var filters: FilterReq = FilterReq(photocard.filters)
}

class FilterReq(filter: Filter) {
    var dish: String = filter.dish
    var nuances: String = filter.nuances
    var decor: String = filter.decor
    var temperature: String = filter.temperature
    var light: String = filter.light
    var lightDirection: String = filter.lightDirection
    var lightSource: String = filter.lightSource
}

