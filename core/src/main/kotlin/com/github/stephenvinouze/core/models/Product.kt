package com.github.stephenvinouze.core.models

import org.parceler.Parcel
import org.parceler.ParcelConstructor

/**
 * Created by stephenvinouze on 10/02/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
data class Product @ParcelConstructor constructor(
        val product_id: String,
        val type: String,
        val title: String,
        val description: String,
        val price: String,
        val priceAmountMicros: Long,
        val priceCurrencyCode: String)