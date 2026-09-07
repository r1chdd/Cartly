package com.rtech.cartly.adapters

import android.content.res.Configuration
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rtech.cartly.R
import com.rtech.cartly.databinding.ItemSearchBinding
import com.rtech.cartly.model.Deal

class SearchAdapter(
    private val onDealClick: (Deal) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val items = mutableListOf<Deal>()

    fun submitDeals(deals: List<Deal>) {
        items.clear()
        items.addAll(deals)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val deal = items[position]
        val b = holder.binding
        val context = b.root.context

        b.root.setBackgroundResource(
            if (isDarkMode(context)) R.drawable.card_background_dark else R.drawable.card_background
        )

        b.searchName.text = deal.name
        b.searchStore.text = "${deal.store} • ${deal.distance}"
        b.searchDiscount.text = deal.discount
        b.searchPriceNow.text = deal.priceNow
        b.searchPriceWas.text = deal.priceWas
        b.searchPriceWas.paintFlags = b.searchPriceWas.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        if (deal.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(deal.imageUrl)
                .placeholder(R.drawable.ic_basket)
                .error(R.drawable.ic_basket)
                .into(b.searchImage)
        } else {
            b.searchImage.setImageResource(R.drawable.ic_basket)
        }

        b.root.setOnClickListener { onDealClick(deal) }
    }

    override fun getItemCount(): Int = items.size

    private fun isDarkMode(context: android.content.Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    class SearchViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)
}