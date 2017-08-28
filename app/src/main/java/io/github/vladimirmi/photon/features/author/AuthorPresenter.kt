package io.github.vladimirmi.photon.features.author

import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.UserDto
import io.github.vladimirmi.photon.features.album.AlbumScreen
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.ErrorCompletableObserver
import io.github.vladimirmi.photon.utils.ErrorObserver
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
        compDisp.add(subscribeOnUpdateUser(userId))
        compDisp.add(subscribeOnAlbums(userId))
    }

    private fun subscribeOnUser(userId: String): Disposable {
        return model.getUser(userId)
                .subscribeWith(object : ErrorObserver<UserDto>() {
                    override fun onNext(it: UserDto) {
                        view.setUser(it)
                    }
                })
    }

    private fun subscribeOnUpdateUser(userId: String): Disposable {
        return model.updateUser(userId)
                .subscribeWith(ErrorCompletableObserver())
    }

    private fun subscribeOnAlbums(userId: String): Disposable {
        return model.getAlbums(userId)
                .subscribeWith(object : ErrorObserver<List<AlbumDto>>() {
                    override fun onNext(it: List<AlbumDto>) {
                        view.setAlbums(it)
                    }
                })
    }

    fun showAlbum(album: AlbumDto) {
        Flow.get(view).set(AlbumScreen(album.id))
    }
}

