package com.osias.blockchain.module

import androidx.room.Room
import com.osias.blockchain.BlockchainGraphApplication
import com.osias.blockchain.BuildConfig
import com.osias.blockchain.model.local.BancoLocal
import com.osias.blockchain.model.remote.EnumRetrofitConverterFactory
import com.osias.blockchain.model.remote.Service
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
object ApiModule {

    @JvmStatic
    @Provides
    fun getAmbiente(): String = BuildConfig.SERVER_URL

    @JvmStatic
    @Provides
    @Reusable
    fun getRetrofit(): Retrofit {
        val ambiente = getAmbiente()

        val logging = HttpLoggingInterceptor()
        if(BuildConfig.DEBUG) logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.header("Accept", "application/json")
                it.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(EnumRetrofitConverterFactory())
            .baseUrl(ambiente)
            .client(httpClient)
            .build()
    }

    @JvmStatic
    @Provides
    @Reusable
    fun getCurrencyRepository(service: Service, localDb: BancoLocal) = CurrencyRepository(service, localDb.currencyDao())

    @JvmStatic
    @Provides
    @Reusable
    fun getChartsRepository(service: Service, localDb: BancoLocal) = ChartRepository(service,
                                                                        localDb.chartDao(),
                                                                        localDb.chartPointDao())

    @JvmStatic
    @Provides
    fun getClient(): Service = getRetrofit().create(Service::class.java)

    @JvmStatic
    @Provides
    @Reusable
    fun getDatabase(context: BlockchainGraphApplication): BancoLocal
            = Room.databaseBuilder(context, BancoLocal::class.java, "local_storage").build()

}