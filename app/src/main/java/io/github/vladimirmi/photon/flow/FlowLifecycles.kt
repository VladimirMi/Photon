package io.github.vladimirmi.photon.flow

import android.content.Intent
import android.os.Bundle


interface FlowLifecycles {
    interface BackPressListener {
        fun onBackPressed(): Boolean
    }

    interface CreateDestroyListener {
        fun onCreate(bundle: Bundle)

        fun onDestroy()
    }

    interface StartStopListener {
        fun onStart()

        fun onStop()
    }

    interface ResumePauseListener {
        fun onResume()

        fun onPause()
    }

    interface ViewLifecycleListener {
        fun onViewRestored()

        fun onViewDestroyed(removedByFlow: Boolean)
    }

    interface PreSaveViewStateListener {
        fun preSaveViewState()
    }

    interface SaveInstanceStateListener {
        fun onSaveInstanceState(outState: Bundle)
    }

    interface ActivityResultListener {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    interface PermissionRequestListener {
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    }
}
