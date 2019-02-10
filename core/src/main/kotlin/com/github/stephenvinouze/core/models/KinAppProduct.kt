package com.github.stephenvinouze.core.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by stephenvinouze on 10/02/2017.
 */
@Parcelize
data class KinAppProduct(
        val product_id: String,
        val title: String,
        val description: String,
        val price: String,
        val priceAmountMicros: Long,
        val priceCurrencyCode: String,
        val type: KinAppProductType) : Parcelable