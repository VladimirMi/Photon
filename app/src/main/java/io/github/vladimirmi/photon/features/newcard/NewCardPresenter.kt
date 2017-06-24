package io.github.vladimirmi.photon.features.newcard

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.Album
import io.github.vladimirmi.photon.data.models.Tag
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class NewCardPresenter(model: INewCardModel, rootPresenter: RootPresenter)
    : BasePresenter<NewCardView, INewCardModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: NewCardView) {
        compDisp.add(subscribeOnTagField())
        view.setTags(model.getSavedTags())
        compDisp.add(subscribeOnAlbums())
    }

    private fun subscribeOnTagField(): Disposable {
        return view.tagObs.doOnNext { view.setTagActionIcon(it.isNotEmpty()) }
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .flatMap { model.search(it.toString()) }
                .subscribe { view.setTagSuggestions(it) }
    }

    private fun subscribeOnAlbums(): Disposable {
        return model.getAlbums()
                .subscribe { view.setAlbums(it) }
    }

    fun addFilter(filter: Pair<String, String>) {
        model.addFilter(filter)
    }

    fun removeFilter(filter: Pair<String, String>) {
        model.removeFilter(filter)
    }

    fun saveTag(tag: String) {
        model.addTag(Tag(tag))
        view.setTags(model.getSavedTags())
    }

    fun setAlbum(album: Album) {
        view.selectAlbum(album)
    }


}

