package com.osias.blockchain

import com.osias.blockchain.module.DaggerApiComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class BlockchainGraphApplication: DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerApiComponent
                                                                                .factory()
                                                                                .create(this)

}