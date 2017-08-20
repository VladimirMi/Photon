package io.github.vladimirmi.photon.data.jobs.queue

/**
 * Created by Vladimir Mikhalev 17.08.2017.
 */

interface JobTask {
    var parentEntityId: String
    var entityId: String
    val tag: String
    val type: Type

    enum class Type {CREATE, DELETE, UNIQUE, NORMAL, OTHER }
}