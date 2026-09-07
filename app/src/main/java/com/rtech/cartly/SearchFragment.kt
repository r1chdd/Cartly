package com.rtech.cartly

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rtech.cartly.model.Deal
import com.rtech.cartly.ui.screens.SearchScreen
import com.rtech.cartly.ui.theme.CartlyTheme
import com.rtech.cartly.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            CartlyTheme {
                SearchScreen(
                    viewModel = viewModel,
                    onDealClick = ::openDealDetail
                )
            }
        }
    }

    private fun openDealDetail(deal: Deal) {
        val intent = Intent(requireContext(), DealDetailActivity::class.java)
        intent.putExtra("name", deal.name)
        intent.putExtra("store", deal.store)
        intent.putExtra("distance", deal.distance)
        intent.putExtra("price_now", deal.priceNow)
        intent.putExtra("price_was", deal.priceWas)
        intent.putExtra("discount", deal.discount)
        intent.putExtra("image_url", deal.imageUrl)
        startActivity(intent)
    }
}