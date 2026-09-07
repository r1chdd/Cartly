package com.rtech.cartly.model

data class Deal(
    val id: String,
    val name: String,
    val store: String,
    val distance: String,
    val priceNow: String,
    val priceWas: String,
    val discount: String,
    val category: String,
    val imageUrl: String
)