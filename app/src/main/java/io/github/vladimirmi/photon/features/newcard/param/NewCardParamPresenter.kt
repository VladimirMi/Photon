package io.github.vladimirmi.photon.features.newcard.param

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.newcard.INewCardModel
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

class NewCardParamPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardParamView, INewCardModel>(model, rootPresenter) {

    override fun initToolbar() {
        //no-op
    }

    override fun initView(view: NewCardParamView) {

    }

    fun addFilter(filter: Pair<String, String>) = model.addFilter(filter)

    fun removeFilter(filter: Pair<String, String>) = model.removeFilter(filter)
}