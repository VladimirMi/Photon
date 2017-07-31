package io.github.vladimirmi.photon.features.album

import android.view.MenuItem
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.dto.AlbumDto
import io.github.vladimirmi.photon.data.models.dto.PhotocardDto
import io.github.vladimirmi.photon.data.models.req.EditAlbumReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.newcard.NewCardScreenInfo
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavHistory.BottomItem.LOAD
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.github.vladimirmi.photon.utils.ErrorSingleObserver
import io.github.vladimirmi.photon.utils.JobStatus
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

    fun editAlbum(albumReq: EditAlbumReq) {
        view.closeEditDialog()
        if (albumChange(albumReq)) {
            albumReq.id = album.id
            compDisp.add(model.editAlbum(albumReq).subscribeWith(ErrorObserver()))
        }
    }

    fun submitDeletePhotos() {
        if (photosForDelete.size > 0) {
            compDisp.add(model.removePhotos(photosForDelete, album)
                    .subscribeWith(object : ErrorSingleObserver<Unit>() {
                        override fun onError(e: Throwable) {
                            if (e is ApiError) view.showError(e.errorResId)
                            super.onError(e)
                        }
                    }))
            photosForDelete.clear()
        }
        setEditable(false)
    }

    private fun albumChange(albumReq: EditAlbumReq): Boolean {
        val name = view.name.text.toString()
        val description = view.description.text.toString()
        var albumChanged = false
        if (albumReq.title != name || albumReq.description != description) {
            albumChanged = true
        }
        return albumChanged
    }

    fun setEditable(boolean: Boolean) {
        editMode = boolean
        view.setEditable(editMode)
        initToolbar()
    }

    fun delete() {
        compDisp.add(model.deleteAlbum(album.id)
                .subscribeWith(object : ErrorObserver<JobStatus>() {
                    override fun onNext(it: JobStatus) {
                        view.closeDeleteDialog()
                        Flow.get(view).goBack()
                    }

                    override fun onError(e: Throwable) {
                        if (e is ApiError) view.showError(e.errorResId)
                        super.onError(e)
                    }
                }))
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
        if (editMode) {
            setEditable(false)
            photosForDelete.clear()
            view.setAlbum(album)
            return true
        } else return false
    }

}

