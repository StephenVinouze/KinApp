package com.github.stephenvinouze.core.managers

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import com.android.vending.billing.IInAppBillingService
import com.github.stephenvinouze.core.models.KinAppProduct
import com.github.stephenvinouze.core.models.KinAppPurchase
import com.github.stephenvinouze.core.models.KinAppPurchaseResult
import com.github.stephenvinouze.core.models.KinAppPurchaseState
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.json.JSONObject


/**
 * Created by stephenvinouze on 13/03/2017.
 */
class KinAppManager(private val context: Context, private val developerPayload: String) {

    companion object {
        val KINAPP_TEST_PURCHASE_PREFIX = "android.test"
        val KINAPP_TEST_PURCHASE_SUCCESS = KINAPP_TEST_PURCHASE_PREFIX + ".purchased"
        val KINAPP_TEST_PURCHASE_CANCELED = KINAPP_TEST_PURCHASE_PREFIX + ".canceled"
        val KINAPP_TEST_PURCHASE_REFUNDED = KINAPP_TEST_PURCHASE_PREFIX + ".refunded"
        val KINAPP_TEST_PURCHASE_UNAVAILABLE = KINAPP_TEST_PURCHASE_PREFIX + ".item_unavailable"

        private const val KINAPP_REQUEST_CODE = 1001
        private const val KINAPP_RESPONSE_RESULT_OK = 0
        private const val KINAPP_RESPONSE_RESULT_ALREADY_OWNED = 7

        private const val KINAPP_API_VERSION = 3

        private const val KINAPP_INTENT = "com.android.vending.billing.InAppBillingService.BIND"
        private const val KINAPP_PACKAGE = "com.android.vending"

        private const val KINAPP_INAPP_TYPE = "inapp"
        private const val KINAPP_SUBS_TYPE = "subs"

        private const val GET_ITEM_LIST = "ITEM_ID_LIST"

        private const val RESPONSE_CODE = "RESPONSE_CODE"
        private const val RESPONSE_ITEM_LIST = "DETAILS_LIST"
        private const val RESPONSE_BUY_INTENT = "BUY_INTENT"
        private const val RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        private const val RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE"
        private const val RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
    }

    private var billingService: IInAppBillingService? = null
    private var listener: KinAppListener? = null
    private var billingConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            billingService = IInAppBillingService.Stub.asInterface(service)
            listener?.onBillingReady()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            billingService = null
        }
    }

    fun bind(listener: KinAppListener? = null) {
        this.listener = listener
        val billingIntent = Intent(KINAPP_INTENT)
        billingIntent.`package` = KINAPP_PACKAGE
        context.bindService(billingIntent, billingConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (billingService != null) {
            context.unbindService(billingConnection)
        }
    }

    fun isInAppBillingSupported(): Boolean {
        return isBillingSupported(KINAPP_INAPP_TYPE)
    }

    fun isSubscriptionBillingSupported(): Boolean {
        return isBillingSupported(KINAPP_SUBS_TYPE)
    }

    suspend fun fetchProducts(productIds: ArrayList<String>): List<KinAppProduct>? {
        return async(CommonPool) {
            val bundle = Bundle()
            bundle.putStringArrayList(GET_ITEM_LIST, productIds)
            try {
                val responseBundle = billingService?.getSkuDetails(KINAPP_API_VERSION, this@KinAppManager.context.packageName, KINAPP_INAPP_TYPE, bundle)
                if (getResult(responseBundle, RESPONSE_CODE) == KINAPP_RESPONSE_RESULT_OK) {
                    val inappProducts = responseBundle?.getStringArrayList(RESPONSE_ITEM_LIST)
                    val products = arrayListOf<KinAppProduct>()
                    inappProducts?.forEach {
                        products.add(getProduct(it))
                    }
                    return@async products
                }
                return@async null
            } catch (e: RemoteException) {
                e.printStackTrace()
                return@async null
            }
        }.await()
    }

    fun restorePurchases(): List<KinAppPurchase>? {
        try {
            val responseBundle = billingService?.getPurchases(KINAPP_API_VERSION, context.packageName, KINAPP_INAPP_TYPE, null)
            if (getResult(responseBundle, RESPONSE_CODE) == KINAPP_RESPONSE_RESULT_OK) {
                val inappPurchases = responseBundle?.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST)
                if (inappPurchases != null) {
                    val purchases = arrayListOf<KinAppPurchase>()
                    inappPurchases.forEach {
                        purchases.add(getPurchase(it))
                    }
                    return purchases
                }
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    fun purchase(activity: Activity, productId: String) {
        try {
            val responseBundle = billingService?.getBuyIntent(KINAPP_API_VERSION, context.packageName, productId, KINAPP_INAPP_TYPE, developerPayload)
            val result = getResult(responseBundle, RESPONSE_CODE)
            if (result == KINAPP_RESPONSE_RESULT_OK) {
                val pendingIntent = responseBundle?.getParcelable<PendingIntent>(RESPONSE_BUY_INTENT)
                activity.startIntentSenderForResult(pendingIntent?.intentSender, KINAPP_REQUEST_CODE, Intent(), 0, 0, 0)
            } else if (result == KINAPP_RESPONSE_RESULT_ALREADY_OWNED) {
                listener?.onPurchaseFinished(KinAppPurchaseResult.ALREADY_OWNED, null)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun verifyPurchase(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == KINAPP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val purchaseData = data?.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA)
                val dataSignature = data?.getStringExtra(RESPONSE_INAPP_SIGNATURE)
                if (purchaseData != null) {
                    val purchase = getPurchase(purchaseData)
                    if (purchase.productId.startsWith(KINAPP_TEST_PURCHASE_PREFIX) ||
                            (dataSignature != null && SecurityManager.verifyPurchase(developerPayload, purchaseData, dataSignature))) {
                        listener?.onPurchaseFinished(KinAppPurchaseResult.SUCCESS, purchase)
                    } else {
                        listener?.onPurchaseFinished(KinAppPurchaseResult.INVALID_SIGNATURE, null)
                    }
                } else {
                    listener?.onPurchaseFinished(KinAppPurchaseResult.INVALID_PURCHASE, null)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                listener?.onPurchaseFinished(KinAppPurchaseResult.CANCEL, null)
            } else {
                listener?.onPurchaseFinished(KinAppPurchaseResult.INVALID_PURCHASE, null)
            }
            return true
        }
        return false
    }

    suspend fun consumePurchase(purchase: KinAppPurchase): Boolean {
        return async(CommonPool) {
            try {
                val response = billingService?.consumePurchase(KINAPP_API_VERSION, this@KinAppManager.context.packageName, purchase.purchaseToken)
                return@async response == KINAPP_RESPONSE_RESULT_OK
            } catch (e: RemoteException) {
                e.printStackTrace()
                return@async false
            }
        }.await()
    }

    private fun isBillingSupported(type: String): Boolean {
        return billingService?.isBillingSupported(KINAPP_API_VERSION, context.packageName, type) == KINAPP_RESPONSE_RESULT_OK
    }

    private fun getResult(responseBundle: Bundle?, responseExtra: String): Int? {
        return responseBundle?.getInt(responseExtra)
    }

    private fun getProduct(productData: String): KinAppProduct {
        val inappProduct = JSONObject(productData)
        return KinAppProduct(
                product_id = inappProduct.optString("productId"),
                title = inappProduct.optString("title"),
                type = inappProduct.optString("type"),
                description = inappProduct.optString("description"),
                price = inappProduct.optString("price"),
                priceAmountMicros = inappProduct.optLong("price_amount_micros"),
                priceCurrencyCode = inappProduct.optString("price_currency_code"))
    }

    private fun getPurchase(purchaseData: String): KinAppPurchase {
        val item = JSONObject(purchaseData)
        return KinAppPurchase(
                orderId = item.optString("orderId"),
                productId = item.optString("productId"),
                purchaseTime = item.optLong("purchaseTime"),
                purchaseToken = item.optString("purchaseToken"),
                purchaseState = KinAppPurchaseState.values()[item.optInt("purchaseState", KinAppPurchaseState.CANCELED.value)],
                packageName = item.optString("purchaseToken"),
                developerPayload = item.optString("developerPayload"),
                autoRenewing = item.optBoolean("autoRenewing")
        )
    }

    interface KinAppListener {
        fun onBillingReady()
        fun onPurchaseFinished(purchaseResult: KinAppPurchaseResult, purchase: KinAppPurchase?)
    }

}