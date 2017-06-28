package io.github.vladimirmi.photon.features.album

import android.view.MenuItem
import flow.Flow
import flow.History
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.realm.Album
import io.github.vladimirmi.photon.data.models.realm.Photocard
import io.github.vladimirmi.photon.features.newcard.NewCardScreen
import io.github.vladimirmi.photon.features.photocard.PhotocardScreen
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.github.vladimirmi.photon.flow.BottomNavDispatcher.BottomItem.LOAD
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class AlbumPresenter(model: IAlbumModel, rootPresenter: RootPresenter)
    : BasePresenter<AlbumView, IAlbumModel>(model, rootPresenter) {

    private var editMode: Boolean = false
    private val album by lazy { Flow.getKey<AlbumScreen>(view)?.album!! }

    val moreActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.edit -> setEditable(true)
            R.id.delete -> view.showDeleteDialog()
            R.id.add_photocard -> addPhotocard()
        }
    }
    val submitAction: (MenuItem) -> Unit = { submit() }

    override fun initToolbar() {
        val builder = rootPresenter.getNewToolbarBuilder()

        if (editMode) builder.addAction(MenuItemHolder("Submit",
                R.drawable.ic_action_submit, submitAction))

        val popupMenu = if (album.isFavorite) R.menu.submenu_album_fav_screen else R.menu.submenu_album_screen

        if (album.owner == model.getProfileId()) {
            builder.addAction(MenuItemHolder("Actions",
                    iconResId = R.drawable.ic_action_more,
                    actions = moreActions,
                    popupMenu = popupMenu))

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

    private fun submit() {
        setEditable(false)
        val name = view.name.text.toString()
        val description = view.description.text.toString()
        var albumChanged = false
        if (album.title != name || album.description != description) albumChanged = true
        album.title = name
        album.description = description
        if (albumChanged) {
            //todo
            model.editAlbum(album)
        }
    }

    private fun setEditable(boolean: Boolean) {
        editMode = boolean
        view.setEditable(editMode)
        initToolbar()
    }

    fun delete() {
        compDisp.add(model.deleteAlbum(album)
                .subscribe({}, {}, {
                    view.closeDeleteDialog()
                    Flow.get(view).goBack()
                }))
    }

    private fun addPhotocard() {
        rootPresenter.bottomNavigator?.historyMap?.set(LOAD, History.single(NewCardScreen(album.id)))
        rootPresenter.navigateTo(LOAD)
    }

}

