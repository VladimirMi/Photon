package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.features.root.RootPresenter

class NewCardPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardView, INewCardModel>(model, rootPresenter) {
    val photoCard = Photocard()

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: NewCardView) {

    }

    fun addFilter(filter: Pair<String, String>) {
        model.addFilter(filter)
    }

    fun removeFilter(filter: Pair<String, String>) {
        model.removeFilter(filter)
    }
}

