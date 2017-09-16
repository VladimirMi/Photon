package io.github.vladimirmi.photon.presentation.newcard.info

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import com.jakewharton.rxbinding2.view.focusChanges
import com.jakewharton.rxbinding2.widget.textChanges
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseView
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreen
import io.github.vladimirmi.photon.presentation.newcard.NewCardScreenInfo
import io.github.vladimirmi.photon.presentation.search.tags.StringAdapter
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_newcard_name.view.*
import kotlinx.android.synthetic.main.view_newcard_step1.view.*
import kotlinx.android.synthetic.main.view_newcard_tags.view.*
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 28.07.2017.
 */

class NewCardInfoView(context: Context, attrs: AttributeSet)
    : BaseView<NewCardInfoPresenter, NewCardInfoView>(context, attrs) {

    private val nameTx by lazy { name_field }
    private val tagTx by lazy { tag_field }
    private val clearNameIcon by lazy { ic_clear_name }
    private val clearTagIcon by lazy { ic_clear_tag }
    private val tagList by lazy { tag_list }
    private val tagActionIcon by lazy { ic_action }
    private val suggestionTagList by lazy { suggestion_tag_list }

    private val tagAction: (String) -> Unit = {
        tagTx.setText(it)
        tagTx.setSelection(it.length)
    }
    private val tagsAdapter = StringAdapter(tagAction)
    private val suggestTagAdapter = StringAdapter(tagAction)

    val nameObs by lazy { nameTx.textChanges() }
    val tagObs by lazy { tagTx.textChanges() }

    private lateinit var disposable: Disposable

    override fun initDagger(context: Context) {
        DaggerService.getComponent<NewCardScreen.Component>(context).inject(this)
    }

    override fun initView() {
        clearNameIcon.setOnClickListener { nameTx.setText("") }
        clearTagIcon.setOnClickListener { tagTx.setText("") }
        initTagSection()
        disposable = tagTx.focusChanges()
                .filter { it }
                .subscribe { scroll_view.smoothScrollTo(0, tags_title.y.toInt()) }
    }

    private fun initTagSection() {
        tagList.layoutManager = LinearLayoutManager(context)
        tagList.adapter = tagsAdapter

        tagActionIcon.setOnClickListener {
            val tag = tagTx.text.toString()
            if (tag.isNotEmpty()) {
                presenter.saveTag(tag)
                tagTx.setText("")
            }
        }

        suggestionTagList.layoutManager = LinearLayoutManager(context)
        suggestionTagList.adapter = suggestTagAdapter
    }

    override fun onViewDestroyed(removedByFlow: Boolean) {
        super.onViewDestroyed(removedByFlow)
        disposable.dispose()
    }

    fun setTagActionIcon(submit: Boolean) {
        tagActionIcon.setImageResource(if (submit) R.drawable.ic_action_submit else R.drawable.ic_action_back_arrow)
    }

    fun setTagSuggestions(tags: List<String>) {
        suggestTagAdapter.updateData(tags)
    }

    fun restoreFromModel(newCardScreenInfo: NewCardScreenInfo) {
        nameTx.setText(newCardScreenInfo.title)
        tagTx.setText(newCardScreenInfo.tag)
        Timber.e("restoreFromModel: ${newCardScreenInfo.tags}")
        tagsAdapter.updateData(newCardScreenInfo.tags)
    }
}