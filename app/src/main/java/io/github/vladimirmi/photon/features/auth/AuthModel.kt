package io.github.vladimirmi.photon.features.auth

import io.github.vladimirmi.photon.data.managers.DataManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Vladimir Mikhalev 25.06.2017.
 */

class AuthModel(private val dataManager: DataManager) : IAuthModel {
    override fun isNetAvail(): Observable<Boolean> {
        return dataManager.isNetworkAvailable()
                .observeOn(AndroidSchedulers.mainThread())
    }
}