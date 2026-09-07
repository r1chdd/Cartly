package com.rtech.cartly

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rtech.cartly.data.BasketRepository
import com.rtech.cartly.data.UserProvider
import com.rtech.cartly.ui.screens.DealDetailScreen
import com.rtech.cartly.ui.theme.CartlyTheme

class DealDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name") ?: ""
        val store = intent.getStringExtra("store") ?: ""
        val distance = intent.getStringExtra("distance") ?: ""
        val priceNow = intent.getStringExtra("price_now") ?: ""
        val priceWas = intent.getStringExtra("price_was") ?: ""
        val discount = intent.getStringExtra("discount") ?: ""
        val imageUrl = intent.getStringExtra("image_url") ?: ""

        setContent {
            CartlyTheme {
                DealDetailScreen(
                    name = name,
                    store = store,
                    distance = distance,
                    priceNow = priceNow,
                    priceWas = priceWas,
                    discount = discount,
                    imageUrl = imageUrl,
                    onBack = { finish() },
                    onAddToBasket = {
                        UserProvider.ensureSignedIn { uid ->
                            if (uid != null) {
                                BasketRepository.addItem(uid, name, priceNow, store, imageUrl)
                                Toast.makeText(this, "$name added to basket!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}