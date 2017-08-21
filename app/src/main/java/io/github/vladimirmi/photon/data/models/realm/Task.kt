package io.github.vladimirmi.photon.data.models.realm

import io.github.vladimirmi.photon.data.jobs.queue.JobTask
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Vladimir Mikhalev 16.08.2017.
 */

open class Task(@PrimaryKey var id: String = "",
                var parentEntityId: String = "",
                var entityId: String = "",
                var tag: String = "",
                var type: String = "",
                var queue: RealmList<Task> = RealmList(),
                var request: ByteArray? = null


) : RealmObject()

fun Task.find(predicate: (Task) -> Boolean): Task? {
    checkIfRoot()
    val queue = LinkedList<Task>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val task = queue.poll()
        if (predicate(task)) return task
        if (task.queue.isNotEmpty()) queue.addAll(task.queue)
    }
    return null
}

fun Task.findParent(parentEntityId: String): Task {
    checkIfRoot()
    return find { it.entityId == parentEntityId && it.type == JobTask.Type.CREATE.name } ?: this
}

fun Task.deleteAll(predicate: (Task) -> Boolean): ArrayList<Task> {
    checkIfRoot()
    val deleted = ArrayList<Task>()
    val queue = LinkedList<Task>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val task = queue.poll()
        task.queue.forEach {
            if (predicate(it)) {
                deleted.add(it)
                task.queue.remove(it)
            }
        }
        if (task.queue.isNotEmpty()) queue.addAll(task.queue)
    }
    return deleted
}

private fun Task.checkIfRoot() {
    if (this.id != "ROOT") throw IllegalStateException("Function is applicable only for the root task")
}
