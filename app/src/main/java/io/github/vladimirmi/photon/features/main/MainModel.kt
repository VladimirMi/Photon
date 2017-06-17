package io.github.vladimirmi.photon.features.main

import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.data.models.Photocard
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.models.User
import io.github.vladimirmi.photon.features.search.SearchView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Sort
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev, 03.06.2017.
 */

class MainModel(private val dataManager: DataManager) : IMainModel {

    override val searchQuery = HashMap<String, MutableList<String>>()
    val query = HashMap<String, MutableList<String>>()

    override fun makeQuery(searchQuery: HashMap<String, MutableList<String>>, currentPage: SearchView.Page) {
        query.clear()
        searchQuery.forEach { (key, value) ->
            if (currentPage == SearchView.Page.TAGS && (key == "search" || key == "tags")) {
                query[key] = value
            } else if (currentPage == SearchView.Page.FILTERS && key != "search" && key != "tags") {
                query[key] = value
            }
        }
        Timber.e(query.toString())
    }

    override fun getPhotoCards(): Observable<List<Photocard>> {
        return dataManager.getListFromDb(
                Photocard::class.java, sortBy = "views", order = Sort.DESCENDING)
    }

    override fun register(req: SignUpReq): Observable<User> {
        return dataManager.signUp(req)
                .doOnNext { saveUser(it) }
                .delay(1000, TimeUnit.MILLISECONDS, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun login(req: SignInReq): Observable<User> {
        return dataManager.signIn(req)
                .doOnNext { saveUser(it) }
                .delay(1000, TimeUnit.MILLISECONDS, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun logout() {
        dataManager.logout()
    }

    private fun saveUser(it: User) {
        dataManager.saveToDB(it)
        dataManager.saveUserId(it.id)
        dataManager.saveUserToken(it.token)
    }
}