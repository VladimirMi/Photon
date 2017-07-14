package io.github.vladimirmi.photon.features.album

import android.view.MenuItem
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.EditAlbumReq
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavHistory.BottomItem.LOAD
import io.github.vladimirmi.photon.utils.ErrorObserver
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class AlbumPresenter(model: IAlbumModel, rootPresenter: RootPresenter)
    : BasePresenter<AlbumView, IAlbumModel>(model, rootPresenter) {

    private var editMode: Boolean = false
    private val album by lazy { Flow.getKey<AlbumScreen>(view)?.album!! }
    private val photosForDelete = ArrayList<Photocard>()

    private val moreActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.edit -> view.showEditDialog()
            R.id.delete -> view.showDeleteDialog()
            R.id.add_photocard -> addPhotocard()
        }
    }
    private val submitAction: (MenuItem) -> Unit = { submit() }

    override fun initToolbar() {
        val builder = rootPresenter.getNewToolbarBuilder()
                .setToolbarTitleId(R.string.album_title)
                .setBackNavigationEnabled(true)

        if (editMode) builder.addAction(MenuItemHolder("Submit",
                R.drawable.ic_action_submit, submitAction))

        if (album.owner == model.getProfileId() && !album.isFavorite) {
            builder.addAction(MenuItemHolder("Actions",
                    iconResId = R.drawable.ic_action_more,
                    actions = moreActions,
                    popupMenu = R.menu.submenu_album_screen))

        }
        builder.build()
    }

    override fun initView(view: AlbumView) {
        compDisp.add(subscribeOnAlbum(album))
    }

    private fun subscribeOnAlbum(album: Album): Disposable {
        return Observable.just(album)
                .mergeWith(model.getAlbum(album.id))
                .subscribe { view.setAlbum(it) }
    }

    fun showPhotoCard(photocard: Photocard) {
        Flow.get(view).set(PhotocardScreen(photocard.id, photocard.owner))
    }

    fun editAlbum(albumReq: EditAlbumReq) {
        view.closeEditDialog()
        if (albumChange(albumReq)) {
            albumReq.id = album.id
            compDisp.add(model.editAlbum(albumReq).subscribe())
        }
    }

    fun submit() {
        if (photosForDelete.size > 0) {
            compDisp.add(model.removePhotos(photosForDelete, album)
                    .subscribeWith(ErrorObserver()))
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
        compDisp.add(model.deleteAlbum(album)
                .subscribeWith(object : ErrorObserver<Int>() {
                    override fun onComplete() {
                        view.closeDeleteDialog()
                        Flow.get(view).goBack()
                    }
                }))
    }

    fun deletePhotocard(photocard: Photocard) {
        photosForDelete.add(photocard)
        view.deletePhotocard(photocard)
    }

    private fun addPhotocard() {
        rootPresenter.bottomHistory?.historyMap?.set(LOAD, History.single(NewCardScreen(album)))
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

