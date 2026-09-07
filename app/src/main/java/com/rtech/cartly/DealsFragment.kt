package com.rtech.cartly

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rtech.cartly.adapters.DealAdapter
import com.rtech.cartly.databinding.FragmentDealsBinding
import com.rtech.cartly.model.Deal
import com.rtech.cartly.viewmodel.DealsViewModel

class DealsFragment : Fragment() {

    private val viewModel: DealsViewModel by activityViewModels()

    private var _binding: FragmentDealsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dealAdapter: DealAdapter
    private var selectedStore = "All"
    private var selectedCategory = "All"
    private val filterButtons = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDealsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dealAdapter = DealAdapter(
            onDealClick = { deal -> openDealDetail(deal) },
            onFavouriteClick = { deal -> viewModel.toggleFavourite(deal.name) }
        )

        binding.dealsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.dealsRecycler.adapter = dealAdapter

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.cartlyGreen)
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        val prefs = requireContext().getSharedPreferences("CartlyPrefs", Context.MODE_PRIVATE)
        binding.locationLabel.text = prefs.getString("location", "Sandton, Johannesburg")

        filterButtons.addAll(listOf(
            binding.filterAll, binding.filterCheckers, binding.filterPnP,
            binding.filterShoprite, binding.filterSpar
        ))
        val filterNames = listOf("All", "Checkers", "Pick n Pay", "Shoprite", "Spar")

        filterButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedStore = filterNames[index]
                updateFilterButtons(index)
                viewModel.setStoreFilter(selectedStore)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.loadingSpinner.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.deals.observe(viewLifecycleOwner) { deals ->
            if (!viewModel.isLoaded && deals.isEmpty()) return@observe
            binding.swipeRefresh.isRefreshing = false
            filterDeals()
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            buildCategoryButtons(categories)
        }

        viewModel.favourites.observe(viewLifecycleOwner) { names ->
            dealAdapter.setFavouriteNames(names)
        }

        viewModel.loadData()
    }

    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences("CartlyPrefs", Context.MODE_PRIVATE)
        binding.locationLabel.text = prefs.getString("location", "Sandton, Johannesburg")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun updateFilterButtons(activeIndex: Int) {
        filterButtons.forEachIndexed { index, button ->
            if (index == activeIndex) {
                button.setBackgroundResource(R.drawable.filter_active)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.filter_inactive)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.cartlyGreen))
            }
        }
    }

    private fun buildCategoryButtons(categories: List<String>) {
        binding.categoryContainer.removeAllViews()
        val allCategories = listOf("All") + categories.distinct().sorted()
        for (category in allCategories) {
            val btn = TextView(requireContext()).apply {
                text = category
                textSize = 12f
                setTextColor(if (category == selectedCategory) Color.WHITE else Color.parseColor("#444444"))
                setBackgroundResource(if (category == selectedCategory) R.drawable.filter_active else R.drawable.filter_inactive)
                setPadding(24, 12, 24, 12)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 16, 0)
                layoutParams = params
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectedCategory = category
                    buildCategoryButtons(categories)
                    viewModel.setCategoryFilter(selectedCategory)
                }
            }
            binding.categoryContainer.addView(btn)
        }
    }

    private fun filterDeals() {
        val filtered = viewModel.deals.value.orEmpty()
        dealAdapter.submitDeals(filtered)
        binding.dealsEmptyView.visibility =
            if (viewModel.isLoaded && filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}