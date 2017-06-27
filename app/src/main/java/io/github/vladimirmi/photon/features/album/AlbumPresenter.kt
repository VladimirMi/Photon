package io.github.vladimirmi.photon.features.album

import android.view.MenuItem
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class AlbumPresenter(model: IAlbumModel, rootPresenter: RootPresenter)
    : BasePresenter<AlbumView, IAlbumModel>(model, rootPresenter) {

    private var editMode: Boolean = false
    private val album by lazy { Flow.getKey<AlbumScreen>(view)?.album!! }

    val moreActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.edit -> editAlbum()
            R.id.delete -> deleteAlbum()
            R.id.add_photocard -> addPhotocard()
        }
    }
    val submitAction: (MenuItem) -> Unit = { submit() }

    override fun initToolbar() {
        val builder = rootPresenter.getNewToolbarBuilder()

        if (editMode) builder.addAction(MenuItemHolder("Submit",
                R.drawable.ic_action_submit, submitAction))

        if (album.owner == model.getProfileId()) {
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
        Flow.get(view).set(PhotocardScreen(photocard))
    }

    private fun editAlbum() {
        setEditable(true)
    }

    private fun submit() {
        setEditable(false)
        album.title = view.name.text.toString()
        album.description = view.description.text.toString()
        model.editAlbum(album)
    }

    private fun setEditable(boolean: Boolean) {
        editMode = boolean
        view.setEditable(editMode)
        initToolbar()
    }

    private fun deleteAlbum() {
        TODO("not implemented")
    }

    private fun addPhotocard() {
        TODO("not implemented")
    }

}

