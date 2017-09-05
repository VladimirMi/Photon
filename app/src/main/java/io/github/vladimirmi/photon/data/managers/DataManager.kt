package io.github.vladimirmi.photon.data.managers

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

//@DaggerScope(App::class)
//class DataManager
//@Inject
//constructor(private val restService: RestService,
//            private val preferencesManager: PreferencesManager,
//            private val realmManager: RealmManager,
//            private val context: Context) {
//
//    //region =============== Network ==============
//
////    fun getTagsFromNet(): Observable<List<Tag>> {
////        if (!jobsManager.syncComplete) return Observable.empty()
////        val tag = Tag::class.java.simpleName
////        return restService.getTags(getLastUpdate(tag))
////                .parseGetResponse { saveLastUpdate(tag, it) }
////    }
//
//    //endregion
//
//    //region =============== DataBase ==============
//
//    fun <T : RealmObject> saveFromServer(realmObject: T) {
//        realmManager.saveFromServer(realmObject)
//    }
//
//    fun <T : RealmObject> saveFromServer(list: List<T>) {
//        realmManager.saveFromServer(list)
//    }
//
//    fun <T : RealmObject> save(realmObject: T) {
//        realmManager.save(realmObject)
//    }
//
//    fun <T : RealmObject> getListFromDb(clazz: Class<T>,
//                                        sortBy: String? = null,
//                                        order: Sort = Sort.ASCENDING): Observable<List<T>> =
//            search(clazz, null, sortBy, order)
//
//    fun <T : RealmObject> getObjectFromDb(clazz: Class<T>,
//                                          id: String,
//                                          detach: Boolean = false): Observable<T> =
//            realmManager.getObject(clazz, id, detach)
//
//    fun <T : RealmObject> getDetachedObjFromDb(clazz: Class<T>, id: String): T? =
//            realmManager.getUnmanagedObject(clazz, id)
//
//    fun <T : RealmObject> search(clazz: Class<T>,
//                                 query: List<Query>?,
//                                 sortBy: String? = null,
//                                 order: Sort = Sort.ASCENDING,
//                                 detach: Boolean = false): Observable<List<T>> =
//            realmManager.search(clazz, query, sortBy, order, detach)
//
//    fun <T : RealmObject> removeFromDb(clazz: Class<T>, id: String) {
//        realmManager.remove(clazz, id)
//    }
//
//
//    inline fun <reified T : RealmObject, R : Cached> getCached(id: String): Observable<R> =
//            DaggerService.appComponent.realmManager().getCached(T::class.java, id)
//
//    private fun <T : RealmObject> getLastUpdate(clazz: Class<T>, id: String): String {
//        val obj = getDetachedObjFromDb(clazz, id) as? Synchronizable
//        return (obj?.updated ?: Date(0)).toString()
//    }
//
//
//    //endregion
//
//    //region =============== Shared Preferences ==============
//
//    fun getProfileId() = preferencesManager.getProfileId()
//
//    //endregion
//
//    //todo remake on receiver
//    fun checkNetAvail() = cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
//
//    fun isNetworkAvailable(): Observable<Boolean> =
//            Observable.interval(0, 2, TimeUnit.SECONDS)
//                    .map { checkNetAvail() }
//                    .distinctUntilChanged()
//
//    private val cm by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
//}
//
//
