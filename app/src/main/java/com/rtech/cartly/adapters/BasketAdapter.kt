package com.rtech.cartly.adapters

import android.content.res.Configuration
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rtech.cartly.R
import com.rtech.cartly.databinding.ItemBasketBinding
import com.rtech.cartly.model.BasketItem

class BasketAdapter(
    private val onCheckClick: (BasketItem) -> Unit,
    private val onDeleteClick: (BasketItem) -> Unit
) : RecyclerView.Adapter<BasketAdapter.BasketViewHolder>() {

    private val items = mutableListOf<BasketItem>()

    fun submitItems(newItems: List<BasketItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketViewHolder {
        val binding = ItemBasketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BasketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BasketViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val context = b.root.context

        b.root.setBackgroundResource(
            if (isDarkMode(context)) R.drawable.card_background_dark else R.drawable.card_background
        )

        b.basketName.text = item.name
        b.basketStore.text = item.store
        b.basketPrice.text = item.price

        if (item.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_basket)
                .error(R.drawable.ic_basket)
                .into(b.basketImage)
        } else {
            b.basketImage.setImageResource(R.drawable.ic_basket)
        }

        if (item.checked) {
            b.basketName.paintFlags = b.basketName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            b.basketName.setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
            b.basketCheckBtn.text = "✓"
            b.basketCheckBtn.setTextColor(ContextCompat.getColor(context, R.color.cartlyGreen))
        } else {
            b.basketName.paintFlags = b.basketName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            b.basketName.setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
            b.basketCheckBtn.text = "○"
            b.basketCheckBtn.setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
        }

        b.basketCheckBtn.setOnClickListener { onCheckClick(item) }
        b.basketDeleteBtn.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    private fun isDarkMode(context: android.content.Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    class BasketViewHolder(val binding: ItemBasketBinding) : RecyclerView.ViewHolder(binding.root)
}