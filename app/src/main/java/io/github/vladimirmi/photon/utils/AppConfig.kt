package io.github.vladimirmi.photon.utils

/**
 * Developer Vladimir Mikhalev 13.03.2017
 */

interface AppConfig {
    companion object {
        val BASE_URL = "http://api/"
        val CONNECT_TIMEOUT = 5000
        val READ_TIMEOUT = 5000
        val WRITE_TIMEOUT = 5000
    }
}
