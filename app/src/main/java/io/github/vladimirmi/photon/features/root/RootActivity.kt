package io.github.vladimirmi.photon.features.root

import android.app.ProgressDialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerService
import io.github.vladimirmi.photon.features.main.MainScreen
import io.github.vladimirmi.photon.flow.FlowActivity
import kotlinx.android.synthetic.main.activity_root.*
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class RootActivity : FlowActivity(), IRootView {

    @Inject internal lateinit var presenter: RootPresenter


    //region =============== Life cycle ==============

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupFlowDispatcher(coordinator_container, view_container)

        initToolbar()
        initDagger()
    }

    override fun onStart() {
        presenter.takeView(this)
        super.onStart()
    }

    override fun onStop() {
        presenter.dropView(this)
        super.onStop()
    }

    //endregion

    private fun initToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun initDagger() {
        DaggerService.rootActivityComponent.inject(this)
    }

    override val defaultKey: BaseScreen<*>
        get() = MainScreen()

    //region =============== IRootView ==============

    override fun setToolbarVisible(visible: Boolean) {
        if (visible) {
            toolbar.visibility = View.VISIBLE
        } else {
            toolbar.visibility = View.GONE
        }
    }

    override fun setToolbarTitle(@StringRes titleId: Int) {
        supportActionBar?.setTitle(titleId)
    }

    private var progressDialog: ProgressDialog? = null

    override fun showLoading() {
        hideLoading()
        progressDialog = ProgressDialog(this).apply {
            setMessage(this@RootActivity.getString(R.string.message_wait))
            isIndeterminate = true
            setCancelable(false)
            show()
        }
    }

    override fun hideLoading() {
        if (progressDialog != null) {
            progressDialog?.apply { hide(); dismiss() }
            progressDialog = null
        }
    }

    override fun showMessage(@StringRes stringId: Int) {
        showMessage(getString(stringId))
    }

    override fun showMessage(string: String) {
        Snackbar.make(coordinator_container, string, Snackbar.LENGTH_LONG).show()
    }

    //endregion
}
