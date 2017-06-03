package io.github.vladimirmi.photon.data.network.models

/**
 * Developer Vladimir Mikhalev, 02.06.2017.
 */

class PhotocardRes(
        val id: String,
        val owner: String,
        val title: String,
        val photo: String,
        val views: Int,
        val favorits: Int,
        val filters: Filters,
        val tags: List<String>
)

class Filters(
        val dish: String,
        val nuances: String,
        val decor: String,
        val temperature: String,
        val light: String,
        val lightDirection: String,
        val lightSource: String
)
