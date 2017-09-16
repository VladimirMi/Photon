package io.github.vladimirmi.photon.presentation.newcard.info

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.presentation.newcard.NewCardInteractor
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

@DaggerScope(NewCardScreen::class)
class NewCardInfoPresenter
@Inject constructor(model: NewCardInteractor, rootPresenter: RootPresenter)
    : BasePresenter<NewCardInfoView, NewCardInteractor>(model, rootPresenter) {

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