package io.github.vladimirmi.photon.data.network

import io.github.vladimirmi.photon.R
import okhttp3.ResponseBody

/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class ApiError(message: String, val statusCode: Int, body: ResponseBody?) : Exception(message) {

    private val defaultErr = R.string.message_api_err_unknown
    var readableError: String? = null
    var errorResId = defaultErr

    override val message: String?
        get() = if (readableError != null) readableError else super.message

    init {
        when (statusCode) {
            500 -> if (body != null) parse(body)
        }

    }

    private fun parse(body: ResponseBody) {
        val result = body.charStream().use {
            it.readText()
                    .substringBefore("_1 dup key")
                    .substringAfterLast("index:")
                    .trim()
        }

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