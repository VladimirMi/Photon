package io.github.vladimirmi.photon.presentation.newcard.param

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.newcard.NewCardInteractor
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

@DaggerScope(NewCardScreen::class)
class NewCardParamPresenter
@Inject constructor(model: NewCardInteractor, rootPresenter: RootPresenter)
    : BasePresenter<NewCardParamView, NewCardInteractor>(model, rootPresenter) {

    override fun initToolbar() {
        //no-op
    }

    override fun initView(view: NewCardParamView) {

    }

    fun addFilter(filter: Pair<String, String>) = model.addFilter(filter)

    fun removeFilter(filter: Pair<String, String>) = model.removeFilter(filter)
}