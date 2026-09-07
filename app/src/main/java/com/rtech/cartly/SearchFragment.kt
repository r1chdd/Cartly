package com.rtech.cartly

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rtech.cartly.adapters.SearchAdapter
import com.rtech.cartly.databinding.FragmentSearchBinding
import com.rtech.cartly.model.Deal
import com.rtech.cartly.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by activityViewModels()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchAdapter = SearchAdapter { deal -> openDealDetail(deal) }

        binding.searchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResults.adapter = searchAdapter

        binding.searchInput.addTextChangedListener { text ->
            renderSearch(text.toString())
        }

        viewModel.deals.observe(viewLifecycleOwner) {
            val query = binding.searchInput.text.toString()
            if (query.isNotEmpty()) renderSearch(query)
        }

        viewModel.loadDeals()
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

    private fun renderSearch(query: String) {
        val filtered = viewModel.search(query)
        searchAdapter.submitDeals(filtered)

        binding.searchEmptyView.visibility = when {
            query.isEmpty() -> {
                binding.searchEmptyView.text = "Type to search for deals..."
                View.VISIBLE
            }
            filtered.isEmpty() -> {
                binding.searchEmptyView.text = "No results for \"$query\""
                View.VISIBLE
            }
            else -> View.GONE
        }
    }
}