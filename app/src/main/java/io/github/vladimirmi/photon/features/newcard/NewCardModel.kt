package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.data.models.Photocard

class NewCardModel : INewCardModel {
    override fun saveName(text: String) {
        TODO("not implemented")
    }

    override fun saveTag(text: String) {
        TODO("not implemented")
    }

    override fun getPhotoCard(): Photocard {
        TODO("not implemented")
    }

    fun addFilter(filter: Pair<String, String>) {

    }

    fun removeFilter(filter: Pair<String, String>) {

    }
}