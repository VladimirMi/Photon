package io.github.vladimirmi.photon.features.newcard

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService

/**
 * Created by Vladimir Mikhalev 19.06.2017.
 */

class NewCardView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardPresenter, NewCardView>(context, attrs) {

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    override fun initView() {

    }
}

