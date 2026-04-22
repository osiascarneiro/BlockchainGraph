package com.osias.blockchain.module

import androidx.room.Room
import com.osias.blockchain.BuildConfig
import com.osias.blockchain.model.local.BancoLocal
import com.osias.blockchain.model.remote.EnumRetrofitConverterFactory
import com.osias.blockchain.model.remote.Service
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import com.osias.blockchain.viewmodel.CurrencyViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    // --- Network ---

    single<Retrofit> {
        val logging = HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor {
                val request = it.request().newBuilder()
                    .header("Accept", "application/json")
                    .build()
                it.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(EnumRetrofitConverterFactory())
            .baseUrl(BuildConfig.SERVER_URL)
            .client(httpClient)
            .build()
    }

    single<Service> { get<Retrofit>().create(Service::class.java) }

    // --- Database ---

    single<BancoLocal> {
        Room.databaseBuilder(androidContext(), BancoLocal::class.java, "local_storage").build()
    }

    // --- Repositories ---

    single { CurrencyRepository(get(), get<BancoLocal>().currencyDao()) }

    single { ChartRepository(get(), get<BancoLocal>().chartDao(), get<BancoLocal>().chartPointDao()) }

    // --- ViewModels ---

    viewModel { CurrencyViewModel(get(), get()) }
}
