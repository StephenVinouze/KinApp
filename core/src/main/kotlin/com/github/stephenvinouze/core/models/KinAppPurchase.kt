package com.github.stephenvinouze.core.models

import org.parceler.Parcel
import org.parceler.ParcelConstructor

/**
 * Created by stephenvinouze on 10/02/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
data class KinAppPurchase @ParcelConstructor constructor(
        val orderId: String,
        val productId: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val purchaseState: KinAppPurchaseState,
        val packageName: String,
        val developerPayload: String,
        val autoRenewing: Boolean)