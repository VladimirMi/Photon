package io.github.vladimirmi.photon.flow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.ViewGroup
import flow.Flow
import io.github.vladimirmi.photon.di.DaggerService
import mortar.bundler.BundleServiceRunner

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

abstract class FlowActivity : AppCompatActivity() {

    private lateinit var dispatcher: FlowDispatcher

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
        if (rootActivityScope.hasService(name)) {
            return rootActivityScope.getService<Any>(name)
        } else {
            return super.getSystemService(name)
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
        dispatcher.onStop()
        dispatcher.onViewDestroyed(false)
        super.onStop()
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
        if (item?.itemId == android.R.id.home) {
            Flow.get(this).goBack()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}
