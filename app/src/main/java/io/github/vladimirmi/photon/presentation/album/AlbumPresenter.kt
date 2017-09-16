package io.github.vladimirmi.photon.presentation.album

import android.view.MenuItem
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.req.AlbumEditReq
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.models.AlbumDto
import io.github.vladimirmi.photon.domain.models.PhotocardDto
import io.github.vladimirmi.photon.flow.BottomNavigationHistory.BottomItem.LOAD
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreenInfo
import io.github.vladimirmi.photon.presentation.photocard.PhotocardScreen
import io.github.vladimirmi.photon.presentation.root.MenuItemHolder
import io.github.vladimirmi.photon.presentation.root.RootPresenter
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.disposables.Disposable
import javax.inject.Inject


@DaggerScope(AlbumScreen::class)
class AlbumPresenter
@Inject constructor(interactor: AlbumInteractor, rootPresenter: RootPresenter)
    : BasePresenter<AlbumView, AlbumInteractor>(interactor, rootPresenter) {

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

        if (albumSettled && model.isOwner(album.owner) && !album.isFavorite) {
            builder.addAction(MenuItemHolder("Actions",
                    iconResId = R.drawable.ic_action_more,
                    actions = moreActions,
                    popupMenu = R.menu.submenu_album_screen))

        }
        builder.build()
    }

    override fun initView(view: AlbumView) {
        compDisp.add(subscribeOnAlbum(albumId))
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

    fun showPhotoCard(photocard: PhotocardDto) {
        Flow.get(view).set(PhotocardScreen(photocard.id, photocard.owner))
    }

    fun editAlbum(request: AlbumEditReq) {
        view.closeEditDialog()
        if (albumChange(request)) {
            compDisp.add(model.editAlbum(request)
                    .subscribeWith(ErrorObserver(view)))
        }
    }

    private fun submitDeletePhotos() {
        if (photosForDelete.size > 0) {
            compDisp.add(model.removePhotos(photosForDelete, album)
                    .doOnNext {
                        photosForDelete.clear()
                        setEditable(false)
                    }
                    .subscribeWith(ErrorObserver(view)))
        }
    }

    private fun albumChange(request: AlbumEditReq) =
            request.title != album.title || request.description != album.description

    fun setEditable(boolean: Boolean) {
        editMode = boolean
        view.setEditable(editMode)
        initToolbar()
    }

    fun deleteAlbum() {
        view.closeDeleteDialog()
        compDisp.add(model.deleteAlbum(album.id)
                .doOnNext { Flow.get(view).goBack() }
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

