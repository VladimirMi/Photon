package io.github.vladimirmi.photon.data.network

import android.content.Context
import android.net.ConnectivityManager
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 04.09.2017.
 */

class NetworkChecker(context: Context) {

    private val cm by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    fun isAvailable() = cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected

    fun singleAvailable(): Single<Boolean> = available().filter { it }.firstOrError()

    fun available(): Observable<Boolean> =
            Observable.interval(0, 2, TimeUnit.SECONDS)
                    .map { isAvailable() }
                    .distinctUntilChanged()
}