package com.osias.blockchain

import android.app.Application

/**
 * Test application that does NOT start Koin automatically.
 * Individual tests manage Koin lifecycle via KoinTestRule.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Do not start Koin here - tests will manage it
    }
}
