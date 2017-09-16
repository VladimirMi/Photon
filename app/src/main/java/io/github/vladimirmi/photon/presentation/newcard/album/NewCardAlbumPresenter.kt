package io.github.vladimirmi.photon.presentation.newcard.album

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.presentation.newcard.NewCardInteractor
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

@DaggerScope(NewCardScreen::class)
class NewCardAlbumPresenter
@Inject constructor(model: NewCardInteractor, rootPresenter: RootPresenter)
    : BasePresenter<NewCardAlbumView, NewCardInteractor>(model, rootPresenter) {

    override fun initToolbar() {
        //no-op
    }

    override fun initView(view: NewCardAlbumView) {
        compDisp.add(subscribeOnAlbums())
        view.selectAlbum(model.screenInfo.album)
    }

    private fun subscribeOnAlbums(): Disposable {
        return model.getAlbums()
                .subscribeWith(object : ErrorObserver<List<AlbumDto>>() {
                    override fun onNext(it: List<AlbumDto>) {
                        view.setAlbums(it)
                    }
                })
    }

    fun setAlbum(album: AlbumDto) {
        model.screenInfo.album = album.id
        view.selectAlbum(album.id)
    }
}