package io.github.vladimirmi.photon.data.jobs

import java.io.Serializable

/**
 * Created by Vladimir Mikhalev 14.09.2017.
 */

data class QueueJobHolder(val jobTag: String,
                          val group: String,
                          val entityId: String,
                          var result: String? = null,
                          val queue: ArrayList<QueueJobHolder> = ArrayList())
    : MutableList<QueueJobHolder> by queue, Serializable {

    val tag = jobTag + entityId
    val groupTag = group + entityId

    fun findRecurs(predicate: (QueueJobHolder) -> Boolean): QueueJobHolder? {
        find(predicate)?.let { return it }
        forEach {
            it.findRecurs(predicate)?.let { return it }
        }
        return null
    }

    fun removeFromQueue(tag: String) {
        val parentQueueJobHolder = findRecurs { it.tag == tag || it.groupTag == tag }
        parentQueueJobHolder?.removeAll { it.tag == tag || it.groupTag == tag }
    }
}