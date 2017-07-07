package io.github.vladimirmi.photon.features.photocard

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.data.models.realm.User
import io.github.vladimirmi.photon.features.author.AuthorScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class PhotocardPresenter(model: IPhotocardModel, rootPresenter: RootPresenter) :
        BasePresenter<PhotocardView, IPhotocardModel>(model, rootPresenter) {

    private val actions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_favorite -> addToFavorite()
            R.id.menu_share -> share()
            R.id.menu_download -> download()
        }
    }

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.photocard_title)
                .setBackNavigationEnabled(true)
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        popupMenu = R.menu.submenu_photocard_screen,
                        actions = actions))
                .build()
    }

    lateinit var photocard: Photocard

    override fun initView(view: PhotocardView) {
        val photocard = Flow.getKey<PhotocardScreen>(view)?.photocard!!
        this.photocard = photocard
        compDisp.add(subscribeOnUser(photocard.owner))
        compDisp.add(subscribeOnPhotocard(photocard))
        compDisp.add(subscribeOnIsFavorite(photocard))
    }

    private fun subscribeOnIsFavorite(photocard: Photocard): Disposable {
        return model.isFavorite(photocard).subscribe(view::setFavorite)
    }

    private fun subscribeOnUser(owner: String): Disposable {
        return model.getUser(owner)
                .subscribeWith(object : ErrorObserver<User>() {
                    override fun onNext(it: User) = view.setUser(it)
                })
    }

    private fun subscribeOnPhotocard(photocard: Photocard): Disposable {
        return Observable.just(photocard)
                .mergeWith(model.getPhotocard(photocard.id, photocard.owner))
                .subscribeWith(object : ErrorObserver<Photocard>() {
                    override fun onNext(it: Photocard) = view.setPhotocard(it)
                })
    }


    fun showAuthor() {
        Flow.get(view).set(AuthorScreen(photocard.owner))
    }

    private fun addToFavorite() {
        //todo предлагать войти или менять меню
        if (rootPresenter.isUserAuth()) {
            compDisp.add(model.addToFavorite(photocard).subscribeWith(ErrorObserver()))
        }
    }

    private fun share() {
        //todo implement
    }

    private fun download() {
        //todo implement
    }
}