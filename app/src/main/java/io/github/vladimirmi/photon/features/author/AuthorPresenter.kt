package io.github.vladimirmi.photon.features.author

import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.Disposable

class AuthorPresenter(model: IAuthorModel, rootPresenter: RootPresenter)
    : BasePresenter<AuthorView, IAuthorModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.author_title)
                .setBackNavigationEnabled(true)
                .build()
    }

    override fun initView(view: AuthorView) {
        val userId = Flow.getKey<AuthorScreen>(view)!!.userId
        compDisp.add(subscribeOnUser(userId))
        compDisp.add(subscribeOnAlbums(userId))
    }

    private fun subscribeOnUser(userId: String): Disposable {
        return model.getUser(userId)
                .subscribe { view.setUser(it) }
    }

    private fun subscribeOnAlbums(userId: String): Disposable {
        return model.getAlbums(userId)
                .subscribe { view.setAlbums(it) }
    }

    fun showAlbum(album: Album) = Flow.get(view).set(AlbumScreen(album))
}

