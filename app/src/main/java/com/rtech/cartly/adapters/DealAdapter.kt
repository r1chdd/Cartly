package com.rtech.cartly.adapters

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rtech.cartly.R
import com.rtech.cartly.databinding.ItemDealBinding
import com.rtech.cartly.model.Deal

class DealAdapter(
    private val onDealClick: (Deal) -> Unit,
    private val onFavouriteClick: (Deal) -> Unit
) : RecyclerView.Adapter<DealAdapter.DealViewHolder>() {

    private val items = mutableListOf<Deal>()
    private var favouriteNames = emptySet<String>()

    fun submitDeals(deals: List<Deal>) {
        items.clear()
        items.addAll(deals)
        notifyDataSetChanged()
    }

    fun setFavouriteNames(names: Set<String>) {
        favouriteNames = names
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val binding = ItemDealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val deal = items[position]
        val b = holder.binding
        val context = b.root.context

        b.root.setBackgroundResource(
            if (isDarkMode(context)) R.drawable.card_background_dark else R.drawable.card_background
        )

        b.dealName.text = deal.name
        b.dealStore.text = "${deal.store} • ${deal.distance}"
        b.dealCategory.text = deal.category
        b.dealDiscount.text = deal.discount
        b.dealPriceNow.text = deal.priceNow
        b.dealPriceWas.text = deal.priceWas
        b.dealPriceWas.paintFlags = b.dealPriceWas.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        if (deal.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(deal.imageUrl)
                .placeholder(R.drawable.ic_basket)
                .error(R.drawable.ic_basket)
                .into(b.dealImage)
        } else {
            b.dealImage.setImageResource(R.drawable.ic_basket)
        }

        val isFav = deal.name in favouriteNames
        b.favBtn.setImageResource(R.drawable.ic_heart)
        b.favBtn.setColorFilter(
            if (isFav) Color.parseColor("#E24B4A")
            else ContextCompat.getColor(context, R.color.textSecondary)
        )

        b.root.setOnClickListener { onDealClick(deal) }
        b.favBtn.setOnClickListener { onFavouriteClick(deal) }
    }

    override fun getItemCount(): Int = items.size

    private fun isDarkMode(context: android.content.Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    class DealViewHolder(val binding: ItemDealBinding) : RecyclerView.ViewHolder(binding.root)
}