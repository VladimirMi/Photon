package io.github.vladimirmi.photon.flow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.ViewGroup
import flow.Flow
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.core.IView
import io.github.vladimirmi.photon.di.DaggerService
import kotlinx.android.synthetic.main.activity_root.*
import mortar.bundler.BundleServiceRunner

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

abstract class FlowActivity : AppCompatActivity(), IView {

    private lateinit var dispatcher: FlowDispatcher<BaseScreen<*>>

    override fun attachBaseContext(base: Context) {
        dispatcher = FlowDispatcher(this)
        val newBase = Flow.configure(base, this)
                .defaultKey(defaultKey)
                .dispatcher(dispatcher)
                .addServicesFactory(FlowServiceFactory())
                .install()
        super.attachBaseContext(newBase)
    }

    protected abstract val defaultKey: Any

    protected fun setupFlowDispatcher(activityContainer: ViewGroup, viewContainer: ViewGroup) {
        dispatcher.activityContainer = activityContainer
        dispatcher.viewContainer = viewContainer
    }

    override fun getSystemService(name: String): Any {
        val rootActivityScope = DaggerService.rootActivityScope
        return if (rootActivityScope.hasService(name)) {
            rootActivityScope.getService<Any>(name)
        } else {
            super.getSystemService(name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BundleServiceRunner.getBundleServiceRunner(this).onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        BundleServiceRunner.getBundleServiceRunner(this).onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        dispatcher.onStop()
        dispatcher.onViewDestroyed(removedByFlow = false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        dispatcher.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        dispatcher.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        if (!(dispatcher.onBackPressed())) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            Flow.get(this).goBack()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun showMessage(stringId: Int) {
        showMessage(getString(stringId))
    }

    override fun showMessage(string: String) {
        Snackbar.make(root_container, string, Snackbar.LENGTH_LONG).show()
    }

    override fun showError(stringId: Int, vararg formatArgs: Any) {
        val string = resources.getString(stringId, *formatArgs)
        Snackbar.make(root_container, string, Snackbar.LENGTH_SHORT).show()
    }

    override fun showNetError() {
        Snackbar.make(root_container, R.string.message_err_net, Snackbar.LENGTH_SHORT).show()
    }

    override fun showAuthError() {
        Snackbar.make(root_container, R.string.message_err_auth, Snackbar.LENGTH_SHORT).show()
    }
}
