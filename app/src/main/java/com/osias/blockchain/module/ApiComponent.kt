package com.osias.blockchain.module

import com.osias.blockchain.BlockchainGraphApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class,
    ActivityBuilder::class,
    ApiModule::class])
interface ApiComponent: AndroidInjector<BlockchainGraphApplication> {

    @Component.Factory
    interface Builder : AndroidInjector.Factory<BlockchainGraphApplication> {
        override fun create(@BindsInstance instance: BlockchainGraphApplication): AndroidInjector<BlockchainGraphApplication>
    }

}
