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

    fun replace(tag: String?, jobHolder: QueueJobHolder) {
        tag?.let { removeFromQueue(it) }
        val parentQueue = findParentRecurs { it.tag == tag }
        parentQueue?.add(jobHolder)
    }

    fun cancel(tag: String?) {
        tag?.let { removeFromQueue(it) }
    }

    fun add(tag: String, jobHolder: QueueJobHolder) {
        val parentQueue = findRecurs { it.tag == tag }
        parentQueue?.add(jobHolder)
    }

    private fun findParentRecurs(predicate: (QueueJobHolder) -> Boolean): QueueJobHolder? {
        find(predicate)?.let { return it }
        forEach {
            it.findParentRecurs(predicate)?.let { return it }
        }
        return null
    }

    private fun findRecurs(predicate: (QueueJobHolder) -> Boolean): QueueJobHolder? {
        if (predicate(this)) return this
        forEach {
            it.findRecurs(predicate)?.let { return it }
        }
        return null
    }

    private fun removeFromQueue(tag: String) {
        val parentQueueJobHolder = findParentRecurs { it.tag == tag || it.groupTag == tag }
        parentQueueJobHolder?.removeAll { it.tag == tag || it.groupTag == tag }
    }
}