package io.github.vladimirmi.photon.features.main

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.features.search.SearchScreen
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainPresenter(model: IMainModel, rootPresenter: RootPresenter) :
        BasePresenter<MainView, IMainModel>(model, rootPresenter) {

    override fun initView(view: MainView) {
        compDisp.add(subscribeOnPhotocards())
        rootPresenter.getNewRootBuilder()
                .addAction(MenuItemHolder("Search", R.drawable.ic_action_search,
                        MenuItem.OnMenuItemClickListener {
                            Flow.get(view).set(SearchScreen())
                            return@OnMenuItemClickListener true
                        }))
                .build()
    }

    private fun subscribeOnPhotocards(): Disposable? {
        return model.getPhotoCards()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.setData(it) })
    }
}


