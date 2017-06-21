package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.data.models.Filter
import timber.log.Timber

class NewCardModel : INewCardModel {
    val filters = Filter()

    override fun addFilter(filter: Pair<String, String>) {
        changeFilterField(filter.first, filter.second)
    }

    override fun removeFilter(filter: Pair<String, String>) {
        changeFilterField(filter.first, filter.second, remove = true)
    }

    fun changeFilterField(name: String, value: String, remove: Boolean = false) {
        Timber.e("changeFilterField: with $name to $value, remove = $remove")
        when (name) {
            "dish" -> filters.dish = if (remove) "" else value
            "decor" -> filters.decor = if (remove) "" else value
            "light" -> filters.light = if (remove) "" else value
            "lightDirection" -> filters.lightDirection = if (remove) "" else value
            "lightSource" -> filters.lightSource = if (remove) "" else value
            "temperature" -> filters.temperature = if (remove) "" else value
            "nuances" -> {
                val field = filters.nuances
                val mValue = if (field.isBlank()) value else ", $value"
                filters.nuances = if (remove) field.replace(mValue, "") else field + mValue
            }
        }
    }

}