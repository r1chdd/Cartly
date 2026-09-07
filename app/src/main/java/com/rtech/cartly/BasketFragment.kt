package com.rtech.cartly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rtech.cartly.ui.screens.BasketScreen
import com.rtech.cartly.ui.theme.CartlyTheme
import com.rtech.cartly.viewmodel.BasketViewModel

class BasketFragment : Fragment() {

    private val viewModel: BasketViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            CartlyTheme {
                BasketScreen(viewModel = viewModel)
            }
        }
    }
}