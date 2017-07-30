package io.github.vladimirmi.photon.features.newcard.info

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.newcard.INewCardModel
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

class NewCardInfoPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardInfoView, INewCardModel>(model, rootPresenter) {

    override fun initToolbar() {
        //no-op
    }

    override fun initView(view: NewCardInfoView) {
        compDisp.add(subscribeOnTitleField())
        compDisp.add(subscribeOnTagField())
        view.restoreFromModel(model.screenInfo)
    }

    private fun subscribeOnTitleField(): Disposable {
        return view.nameObs
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { model.screenInfo.title = it.toString() }
    }

    private fun subscribeOnTagField(): Disposable {
        return view.tagObs
                .doOnNext { view.setTagActionIcon(it.isNotEmpty()) }
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { model.screenInfo.tag = it.toString() }
                .flatMap { model.searchTag(it.toString()) }
                .subscribe { view.setTagSuggestions(it) }
    }

    fun saveTag(tag: String) {
        model.addTag(tag)
        view.restoreFromModel(model.screenInfo)
    }
}