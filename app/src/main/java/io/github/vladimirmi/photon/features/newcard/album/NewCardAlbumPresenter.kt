package io.github.vladimirmi.photon.features.newcard.album

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.features.newcard.INewCardModel
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

class NewCardAlbumPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardAlbumView, INewCardModel>(model, rootPresenter) {

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