package io.github.vladimirmi.photon.features.photocard

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.author.AuthorScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class PhotocardPresenter(model: IPhotocardModel, rootPresenter: RootPresenter) :
        BasePresenter<PhotocardView, IPhotocardModel>(model, rootPresenter) {

    override fun initToolbar() {
        val actions: (MenuItem) -> Unit = {
            when (it.itemId) {
            //todo
            }
        }
        rootPresenter.getNewToolbarBuilder()
                .setBackNavigationEnabled(true)
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        popupMenu = R.menu.submenu_photocard_screen,
                        actions = actions))
                .build()
    }

    var ownerId = ""

    override fun initView(view: PhotocardView) {
        val photocard = Flow.getKey<PhotocardScreen>(view)?.photocard!!
        ownerId = photocard.owner
        compDisp.add(subscribeOnUser(ownerId))
        compDisp.add(subscribeOnPhotocard(photocard))
    }

    private fun subscribeOnUser(owner: String): Disposable {
        return model.getUser(owner)
                .subscribe(view::setUser)
    }

    private fun subscribeOnPhotocard(photocard: Photocard): Disposable {
        return Observable.just(photocard)
                .mergeWith(model.getPhotocard(photocard.id, photocard.owner))
                .subscribe(view::setPhotoCard)
    }

    fun showAuthor() {
        Flow.get(view).set(AuthorScreen(ownerId))
    }

}