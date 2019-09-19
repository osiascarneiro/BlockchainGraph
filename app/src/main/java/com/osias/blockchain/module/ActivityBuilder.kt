package com.osias.blockchain.module

import com.osias.blockchain.view.activity.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun contributesLoginActivity(): MainActivity

}