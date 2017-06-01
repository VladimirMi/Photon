package io.github.vladimirmi.photon.flow

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import flow.Flow

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

    override fun onStop() {
        dispatcher.onViewDestroyed(false)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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
}
