package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.IModel

interface INewCardModel : IModel {
    fun addFilter(filter: Pair<String, String>)
    fun removeFilter(filter: Pair<String, String>)
}