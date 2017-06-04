package io.github.vladimirmi.photon.data.network

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class ApiError(message: String, val statusCode: Int) : Exception(message)