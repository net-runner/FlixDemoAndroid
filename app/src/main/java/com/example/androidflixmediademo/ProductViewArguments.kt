package com.example.androidflixmediademo

data class ProductViewArguments(
    val mpn: String,
    val ean: String,
    val distId: String,
    val isoCode: String,
    val flIsoCode: String,
    val brand: String,
    val title: String,
    val price: String,
    val currency: String,
    val imageUrl: String? = null
)