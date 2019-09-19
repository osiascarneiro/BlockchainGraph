package com.osias.blockchain.module

import com.osias.blockchain.view.activity.MainActivity
import com.osias.blockchain.view.fragment.CurrencyFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun contributesMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributesCurrencyFragment(): CurrencyFragment

}