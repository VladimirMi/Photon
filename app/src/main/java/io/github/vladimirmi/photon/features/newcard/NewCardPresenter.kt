package io.github.vladimirmi.photon.features.newcard

import com.jakewharton.rxbinding2.InitialValueObservable
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
        view.setPhotoCard(model.getPhotoCard())
    }

    fun addFilter(filter: Pair<String, String>) {

    }

    fun removeFilter(filter: Pair<String, String>) {

    }

    fun nameChanges(textChanges: InitialValueObservable<CharSequence>) {
        compDisp.add(textChanges.subscribe { model.saveName(it.toString()) })
    }

    fun tagChanges(textChanges: InitialValueObservable<CharSequence>) {
        compDisp.add(textChanges.subscribe { model.saveTag(it.toString()) })
    }

}

