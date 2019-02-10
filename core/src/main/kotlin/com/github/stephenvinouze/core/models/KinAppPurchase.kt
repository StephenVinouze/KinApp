package com.github.stephenvinouze.core.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by stephenvinouze on 10/02/2017.
 */
@Parcelize
data class KinAppPurchase(
        val orderId: String,
        val productId: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val purchaseState: KinAppPurchaseState,
        val packageName: String,
        val developerPayload: String,
        val autoRenewing: Boolean) : Parcelable