package io.github.vladimirmi.photon.utils

import io.github.vladimirmi.photon.BuildConfig

/**
 * Developer Vladimir Mikhalev 13.03.2017
 */

object AppConfig {
    const val BASE_URL = "http://207.154.248.163:5000"
    const val CONNECT_TIMEOUT = 5000 //ms
    const val READ_TIMEOUT = 5000 //ms
    const val WRITE_TIMEOUT = 5000 //ms
    const val UPDATE_TIMEOUT = 600_000 //ms

    const val PHOTOCARDS_PAGE_SIZE = 60

    const val RETRY_REQUEST_BASE_DELAY = 500L //ms
    const val RETRY_REQUEST_COUNT = 5

    const val MIN_CONSUMER_COUNT = 1
    const val MAX_CONSUMER_COUNT = 3
    const val LOAD_FACTOR = 3
    const val CONSUMER_KEEP_ALIVE = 120
    const val INITIAL_BACK_OFF_IN_MS = 1000L

    const val IMAGE_SIZE_LIMIT = 4 // megabyte

    const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"

    const val SPLASH_TIMEOUT = 3000L // ms

    const val CHANGE_SCREEN_ANIMATION = 300L //ms
}
