package com.rtech.cartly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rtech.cartly.adapters.BasketAdapter
import com.rtech.cartly.databinding.FragmentBasketBinding
import com.rtech.cartly.viewmodel.BasketViewModel

class BasketFragment : Fragment() {

    private val viewModel: BasketViewModel by activityViewModels()

    private var _binding: FragmentBasketBinding? = null
    private val binding get() = _binding!!

    private lateinit var basketAdapter: BasketAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasketBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        basketAdapter = BasketAdapter(
            onCheckClick = { item -> viewModel.toggleChecked(item) },
            onDeleteClick = { item -> viewModel.deleteItem(item) }
        )

        binding.basketRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.basketRecycler.adapter = basketAdapter

        binding.btnClearBasket.setOnClickListener {
            viewModel.clearBasket()
        }

        viewModel.items.observe(viewLifecycleOwner) { renderBasket() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBasket()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderBasket() {
        val items = viewModel.items.value.orEmpty()
        basketAdapter.submitItems(items)
        binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.totalView.text = "Total: R%.2f".format(viewModel.getTotal())
    }
}