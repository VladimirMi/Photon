package io.github.vladimirmi.photon.utils

import io.github.vladimirmi.photon.data.models.realm.Deletable
import io.reactivex.Observable
import io.realm.RealmObject

/**
 * Created by Vladimir Mikhalev 28.06.2017.
 */


fun <T : RealmObject> Observable<T>.check(o: T): Observable<T> {
    if (o is Deletable) {
        return filter { (it as Deletable).active }
    } else {
        return this
    }
}