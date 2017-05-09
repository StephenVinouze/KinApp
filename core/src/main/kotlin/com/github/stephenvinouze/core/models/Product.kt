package com.github.stephenvinouze.core.models

import org.parceler.Parcel
import org.parceler.ParcelConstructor

/**
 * Created by stephenvinouze on 10/02/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
data class Product @ParcelConstructor constructor(
        val product_id: String,
        val name: String,
        var title: String,
        var description: String,
        var price: String,
        val duration: Int)