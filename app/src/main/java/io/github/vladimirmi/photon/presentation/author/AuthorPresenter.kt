package io.github.vladimirmi.photon.presentation.author

import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.UserDto
import io.github.vladimirmi.photon.presentation.album.AlbumScreen
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@DaggerScope(AuthorScreen::class)
class AuthorPresenter
@Inject constructor(model: AuthorInteractor, rootPresenter: RootPresenter)
    : BasePresenter<AuthorView, AuthorInteractor>(model, rootPresenter) {

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
                .subscribeWith(object : ErrorObserver<UserDto>() {
                    override fun onNext(it: UserDto) {
                        view.setUser(it)
                    }
                })
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

