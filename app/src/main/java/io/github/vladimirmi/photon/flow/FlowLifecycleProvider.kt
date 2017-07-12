package io.github.vladimirmi.photon.flow

import android.content.Intent
import android.os.Bundle
import android.support.annotation.CheckResult
import android.view.View


object FlowLifecycleProvider {

    fun onCreate(view: View?, savedInstanceState: Bundle) {
        if (view is FlowLifecycles.CreateDestroyListener) {
            view.onCreate(savedInstanceState)
        }
    }

    fun onStart(view: View?) {
        if (view is FlowLifecycles.StartStopListener) {
            view.onStart()
        }
    }

    fun onResume(view: View?) {
        if (view is FlowLifecycles.ResumePauseListener) {
            view.onResume()
        }
    }

    fun onPause(view: View?) {
        if (view is FlowLifecycles.ResumePauseListener) {
            view.onPause()
        }
    }

    fun onStop(view: View?) {
        if (view is FlowLifecycles.StartStopListener) {
            view.onStop()
        }
    }

    fun onDestroy(view: View?) {
        if (view is FlowLifecycles.CreateDestroyListener) {
            view.onDestroy()
        }
        if (view is FlowLifecycles.ViewLifecycleListener) {
            view.onViewDestroyed(false)
        }
    }

    @CheckResult
    fun onBackPressed(view: View?): Boolean {
        return view is FlowLifecycles.BackPressListener && view.onBackPressed()
    }

    fun onActivityResult(view: View?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (view is FlowLifecycles.ActivityResultListener) {
            view.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun onRequestPermissionsResult(view: View?, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (view is FlowLifecycles.PermissionRequestListener) {
            view.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun preSaveViewState(view: View?) {
        if (view is FlowLifecycles.PreSaveViewStateListener) {
            view.preSaveViewState()
        }
    }

    fun onSaveInstanceState(view: View?, bundle: Bundle) {
        if (view is FlowLifecycles.SaveInstanceStateListener) {
            view.onSaveInstanceState(bundle)
        }
    }

    fun onViewRestored(view: View?) {
        if (view is FlowLifecycles.ViewLifecycleListener) {
            view.onViewRestored()
        }
    }

    fun onViewDestroyed(view: View?, removedByFlow: Boolean) {
        if (view is FlowLifecycles.ViewLifecycleListener) {
            view.onViewDestroyed(removedByFlow)
        }
    }
}
