package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.IModel
import io.github.vladimirmi.photon.data.models.Photocard

interface INewCardModel : IModel {
    fun saveName(text: String)
    fun saveTag(text: String)
    fun getPhotoCard(): Photocard
}