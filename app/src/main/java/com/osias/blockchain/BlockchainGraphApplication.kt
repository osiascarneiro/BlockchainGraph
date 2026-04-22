package com.osias.blockchain

import android.app.Application
import com.osias.blockchain.module.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BlockchainGraphApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BlockchainGraphApplication)
            modules(appModule)
        }
    }
}
