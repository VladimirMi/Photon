package io.github.vladimirmi.photon.features.album

import android.view.MenuItem
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.newcard.NewCardScreenInfo
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavHistory.BottomItem.LOAD
import io.github.vladimirmi.photon.utils.ErrorCompletableObserver
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable

class AlbumPresenter(model: IAlbumModel, rootPresenter: RootPresenter)
    : BasePresenter<AlbumView, IAlbumModel>(model, rootPresenter) {

    private var editMode = false
    private val albumId by lazy { Flow.getKey<AlbumScreen>(view)?.albumId!! }
    private val photosForDelete = ArrayList<PhotocardDto>()
    private var albumSettled = false
    private lateinit var album: AlbumDto


    private val moreActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.edit -> view.showEditDialog()
            R.id.delete -> view.showDeleteDialog()
            R.id.add_photocard -> addPhotocard()
        }
    }
    private val submitAction: (MenuItem) -> Unit = { submitDeletePhotos() }

    override fun initToolbar() {
        val builder = rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.album_title)
                .setBackNavigationEnabled(true)

        if (editMode) builder.addAction(MenuItemHolder("Submit",
                R.drawable.ic_action_submit, submitAction))

        if (albumSettled && album.owner == model.getProfileId() && !album.isFavorite) {
            builder.addAction(MenuItemHolder("Actions",
                    iconResId = R.drawable.ic_action_more,
                    actions = moreActions,
                    popupMenu = R.menu.submenu_album_screen))

        }
        builder.build()
    }

    override fun initView(view: AlbumView) {
        compDisp.add(subscribeOnAlbum(albumId))
        compDisp.add(subscribeOnUpdateAlbum(albumId))
    }

    private fun subscribeOnAlbum(albumId: String): Disposable {
        return model.getAlbum(albumId)
                .subscribeWith(object : ErrorObserver<AlbumDto>() {
                    override fun onNext(it: AlbumDto) {
                        view.setAlbum(it)
                        album = it
                        albumSettled = true
                        initToolbar()
                    }
                })
    }

    private fun subscribeOnUpdateAlbum(albumId: String): Disposable {
        return model.updateAlbum(albumId)
                .subscribeWith(ErrorCompletableObserver())
    }

    fun showPhotoCard(photocard: PhotocardDto) {
        Flow.get(view).set(PhotocardScreen(photocard.id, photocard.owner))
    }

    fun editAlbum(albumDto: AlbumDto) {
        view.closeEditDialog()
        if (albumChange(albumDto)) {
            compDisp.add(model.editAlbum(albumDto)
                    .subscribeWith(ErrorObserver(view)))
        }
    }

    private fun submitDeletePhotos() {
        if (photosForDelete.size > 0) {
            compDisp.add(model.removePhotos(photosForDelete, album)
                    .doOnComplete {
                        photosForDelete.clear()
                        setEditable(false)
                    }
                    .subscribeWith(ErrorObserver(view)))
        }
    }

    private fun albumChange(albumDto: AlbumDto) =
            albumDto.title != album.title || albumDto.description != album.description

    fun setEditable(boolean: Boolean) {
        editMode = boolean
        view.setEditable(editMode)
        initToolbar()
    }

    fun delete() {
        view.closeDeleteDialog()
        compDisp.add(model.deleteAlbum(album.id)
                .doOnComplete { Flow.get(view).goBack() }
                .subscribeWith(ErrorObserver(view)))
    }

    fun deletePhotocard(photocard: PhotocardDto) {
        photosForDelete.add(photocard)
        view.deletePhotocard(photocard)
    }

    private fun addPhotocard() {
        rootPresenter.bottomHistory.historyMap[LOAD] =
                History.single(NewCardScreen(NewCardScreenInfo().apply {
                    returnToAlbum = true
                    album = albumId
                }))
        rootPresenter.navigateTo(LOAD)
    }

    fun onBackPressed(): Boolean {
        return if (editMode) {
            setEditable(false)
            photosForDelete.clear()
            view.setAlbum(album)
            true
        } else false
    }

}

