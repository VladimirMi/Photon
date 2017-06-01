package io.github.vladimirmi.photon.di.modules

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import io.github.vladimirmi.photon.core.App
import io.github.vladimirmi.photon.data.network.api.RestService
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.utils.AppConfig
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Developer Vladimir Mikhalev, 30.05.2016.
 */

@Module
class NetworkModule {

    @Provides
    @DaggerScope(App::class)
    fun provideOkHttpClient(): OkHttpClient = createClient()

    @Provides
    @DaggerScope(App::class)
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = createRetrofit(okHttp)

    @Provides
    @DaggerScope(App::class)
    fun provideRestService(retrofit: Retrofit): RestService = retrofit.create(RestService::class.java)

    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addNetworkInterceptor(StethoInterceptor())
                .connectTimeout(AppConfig.CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(AppConfig.READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(AppConfig.WRITE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .build()
    }

    private fun createRetrofit(okHttp: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(createConvertFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttp)
                .build()
    }

    private fun createConvertFactory(): Converter.Factory = GsonConverterFactory.create()
}
