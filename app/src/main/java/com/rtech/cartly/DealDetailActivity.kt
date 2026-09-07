package com.rtech.cartly

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rtech.cartly.data.BasketRepository
import com.rtech.cartly.data.UserProvider
import com.rtech.cartly.R

class DealDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal_detail)

        val name = intent.getStringExtra("name") ?: ""
        val store = intent.getStringExtra("store") ?: ""
        val distance = intent.getStringExtra("distance") ?: ""
        val priceNow = intent.getStringExtra("price_now") ?: ""
        val priceWas = intent.getStringExtra("price_was") ?: ""
        val discount = intent.getStringExtra("discount") ?: ""
        val imageUrl = intent.getStringExtra("image_url") ?: ""

        findViewById<TextView>(R.id.detailName).text = name
        findViewById<TextView>(R.id.detailStore).text = "$store • $distance"
        findViewById<TextView>(R.id.detailStoreName).text = store
        findViewById<TextView>(R.id.detailDistance).text = "$distance away"
        findViewById<TextView>(R.id.detailPriceNow).text = priceNow
        findViewById<TextView>(R.id.detailPriceWas).text = priceWas
        findViewById<TextView>(R.id.detailSaving).text = discount

        val detailImage = findViewById<ImageView>(R.id.detailImage)
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_basket)
                .error(R.drawable.ic_basket)
                .into(detailImage)
        } else {
            detailImage.setImageResource(R.drawable.ic_basket)
        }

        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val btnAddToBasket = findViewById<TextView>(R.id.btnAddToBasket)
        btnAddToBasket.setOnClickListener {
            UserProvider.ensureSignedIn { uid ->
                if (uid != null) {
                    BasketRepository.addItem(uid, name, priceNow, store, imageUrl)
                    Toast.makeText(this, "$name added to basket!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}