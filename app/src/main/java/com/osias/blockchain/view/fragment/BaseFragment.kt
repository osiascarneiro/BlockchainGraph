package com.osias.blockchain.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.osias.blockchain.viewmodel.BaseViewModel
import com.osias.blockchain.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
/**
 * Classe base do projeto,
 * sempre chamar o super do oncreate antes de usar a classe de viewmodel
 */
abstract class BaseFragment<T: BaseViewModel>(private val cls: Class<T>): Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<T>

    /**
     * ViewModel da tela, cada activity ou fragment deve ter uma.
     */
    lateinit var viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(cls)
    }

}