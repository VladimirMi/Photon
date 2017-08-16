package io.github.vladimirmi.photon.data.models.realm

import io.realm.RealmList
import io.realm.RealmObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

open class Task(var entityId: String = "",
                var tag: String = "",
                var type: String = "",
                var next: RealmList<Task> = RealmList(),
                var task: ByteArray = ByteArray(0)
) : RealmObject()

fun Task.find(predicate: (Task) -> Boolean): Task? {
    val queue = LinkedList<Task>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val task = queue.poll()
        if (predicate(task)) return task
        if (task.next.isNotEmpty()) queue.addAll(task.next)
    }
    return null
}

fun Task.deleteAll(predicate: (Task) -> Boolean): ArrayList<Task> {
    val deleted = ArrayList<Task>()
    val queue = LinkedList<Task>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val task = queue.poll()
        task.next.forEach {
            if (predicate(it)) {
                deleted.add(it)
                task.next.remove(it)
            }
        }
        if (task.next.isNotEmpty()) queue.addAll(task.next)
    }
    return deleted
}