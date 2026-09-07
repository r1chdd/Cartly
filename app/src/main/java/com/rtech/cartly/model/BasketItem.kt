package com.rtech.cartly.model

data class BasketItem(
    val id: String,
    val name: String,
    val price: String,
    val store: String,
    val imageUrl: String,
    val checked: Boolean
)