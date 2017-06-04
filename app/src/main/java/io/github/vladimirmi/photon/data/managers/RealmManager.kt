package io.github.vladimirmi.photon.data.managers

import io.realm.Realm
import io.realm.RealmObject

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */


class RealmManager {

    val realmInstance: Realm by lazy { Realm.getDefaultInstance() }

    fun saveToDB(realmObject: RealmObject) {
        realmInstance.executeTransaction { realm -> realm.insertOrUpdate(realmObject) }
    }
}