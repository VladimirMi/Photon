package io.github.vladimirmi.photon.data.network

import io.github.vladimirmi.photon.R
import okhttp3.ResponseBody

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class ApiError(message: String, val statusCode: Int, body: ResponseBody?) : Exception(message) {

    val defaultErr = R.string.message_api_err_unknown
    var readableError: String? = null
    var errorResId = defaultErr

    init {
        when (statusCode) {
            500 -> if (body != null) parse(body)
        }

    }

    fun parse(body: ResponseBody) {
        val reader = body.charStream()
        val result = reader.readText()
                .substringBefore("_1 dup key")
                .substringAfterLast("index:")
                .trim()
        reader.close()

        when (result) {
            "login" -> {
                readableError = "A user with the same login exists"
                errorResId = R.string.message_api_err_login
            }
            "email" -> {
                readableError = "A user with the same email exists"
                errorResId = R.string.message_api_err_mail
            }
        }
    }
}