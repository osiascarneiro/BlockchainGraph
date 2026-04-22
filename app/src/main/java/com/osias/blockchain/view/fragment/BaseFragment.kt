package com.osias.blockchain.view.fragment

import androidx.fragment.app.Fragment
import com.osias.blockchain.viewmodel.BaseViewModel

/**
 * Classe base do projeto.
 * O ViewModel é injetado via Koin no fragment concreto com: override val viewModel by viewModel()
 */
abstract class BaseFragment : Fragment() {

    /**
     * ViewModel da tela. Cada fragment concreto declara:
     *   override val viewModel: XyzViewModel by viewModel()
     */
    abstract val viewModel: BaseViewModel
}
