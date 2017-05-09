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
import com.github.stephenvinouze.core.models.Product
import com.github.stephenvinouze.core.models.Purchase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.json.JSONObject


/**
 * Created by stephenvinouze on 13/03/2017.
 */
class BillingManager(private val activity: Activity, private val developerPayload: String) {

    companion object {
        var products: List<Product>? = null

        val BILLING_TEST_PURCHASE_PREFIX = "android.test"
        val BILLING_TEST_PURCHASE_SUCCESS = BILLING_TEST_PURCHASE_PREFIX + ".purchased"
        val BILLING_TEST_PURCHASE_CANCELED = BILLING_TEST_PURCHASE_PREFIX + ".canceled"
        val BILLING_TEST_PURCHASE_REFUNDED = BILLING_TEST_PURCHASE_PREFIX + ".refunded"
        val BILLING_TEST_PURCHASE_UNAVAILABLE = BILLING_TEST_PURCHASE_PREFIX + ".item_unavailable"

        private const val BILLING_REQUEST_CODE = 1001

        private const val BILLING_API_VERSION = 3

        private const val BILLING_INTENT = "com.android.vending.billing.InAppBillingService.BIND"
        private const val BILLING_PACKAGE = "com.android.vending"

        private const val BILLING_INAPP_TYPE = "inapp"
        private const val BILLING_SUBS_TYPE = "subs"

        private const val GET_ITEM_LIST = "ITEM_ID_LIST"

        private const val BILLING_RESPONSE_RESULT_OK = 0

        private const val RESPONSE_CODE = "RESPONSE_CODE"
        private const val RESPONSE_ITEM_LIST = "DETAILS_LIST"
        private const val RESPONSE_BUY_INTENT = "BUY_INTENT"
        private const val RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        private const val RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE"
        private const val RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
    }

    enum class PurchaseResult {
        SUCCESS, CANCEL, INVALID_PURCHASE, INVALID_SIGNATURE
    }

    private var billingService: IInAppBillingService? = null
    private var listener: BillingListener? = null

    interface BillingListener {
        fun onBillingReady()
        fun onPurchaseFinished(purchaseResult: PurchaseResult, purchase: Purchase?)
    }

    var billingConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            billingService = IInAppBillingService.Stub.asInterface(service)
            listener?.onBillingReady()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            billingService = null
        }
    }

    fun bind(listener: BillingListener? = null) {
        this.listener = listener
        val billingIntent = Intent(BILLING_INTENT)
        billingIntent.`package` = BILLING_PACKAGE
        activity.bindService(billingIntent, billingConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (billingService != null) {
            activity.unbindService(billingConnection)
        }
    }

    fun isInAppBillingSupported(): Boolean {
        return isBillingSupported(BILLING_INAPP_TYPE)
    }

    fun isSubscriptionBillingSupported(): Boolean {
        return isBillingSupported(BILLING_SUBS_TYPE)
    }

    suspend fun fetchProducts(): List<Product>? {
        return async(CommonPool) {
            val skuDetails = products?.map { it.product_id } as ArrayList<String>
            val bundle = Bundle()
            bundle.putStringArrayList(GET_ITEM_LIST, skuDetails)
            try {
                val responseBundle = billingService?.getSkuDetails(BILLING_API_VERSION, activity.packageName, BILLING_INAPP_TYPE, bundle)
                if (getResult(responseBundle, RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                    val inappProducts = responseBundle?.getStringArrayList(RESPONSE_ITEM_LIST)
                    inappProducts?.forEach {
                        val inappProduct = JSONObject(it)
                        val product = products?.filter { it.product_id == inappProduct.getString("productId") }?.first()
                        product?.title = inappProduct.getString("title")
                        product?.description = inappProduct.getString("description")
                        product?.price = inappProduct.getString("price")
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

    fun restorePurchases(): List<Purchase>? {
        try {
            val responseBundle = billingService?.getPurchases(BILLING_API_VERSION, activity.packageName, BILLING_INAPP_TYPE, null)
            if (getResult(responseBundle, RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                val inappPurchases = responseBundle?.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST)
                if (inappPurchases != null) {
                    val purchases = arrayListOf<Purchase>()
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

    fun purchase(sku: String) {
        try {
            val responseBundle = billingService?.getBuyIntent(BILLING_API_VERSION, activity.packageName, sku, BILLING_INAPP_TYPE, developerPayload)
            if (getResult(responseBundle, RESPONSE_CODE) == BILLING_RESPONSE_RESULT_OK) {
                val pendingIntent = responseBundle?.getParcelable<PendingIntent>(RESPONSE_BUY_INTENT)
                activity.startIntentSenderForResult(pendingIntent?.intentSender, BILLING_REQUEST_CODE, Intent(), 0, 0, 0)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun verifyPurchase(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == BILLING_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val purchaseData = data?.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA)
                val dataSignature = data?.getStringExtra(RESPONSE_INAPP_SIGNATURE)
                if (purchaseData != null) {
                    val purchase = getPurchase(purchaseData)
                    if (!purchase.productId.startsWith(BILLING_TEST_PURCHASE_PREFIX) &&
                            dataSignature != null &&
                            SecurityManager.verifyPurchase(developerPayload, purchaseData, dataSignature)) {
                        listener?.onPurchaseFinished(PurchaseResult.SUCCESS, purchase)
                    } else {
                        listener?.onPurchaseFinished(PurchaseResult.INVALID_SIGNATURE, null)
                    }
                } else {
                    listener?.onPurchaseFinished(PurchaseResult.INVALID_PURCHASE, null)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                listener?.onPurchaseFinished(PurchaseResult.CANCEL, null)
            } else {
                listener?.onPurchaseFinished(PurchaseResult.INVALID_PURCHASE, null)
            }
            return true
        }
        return false
    }

    suspend fun consumePurchase(purchase: Purchase): Boolean {
        return async(CommonPool) {
            try {
                val response = billingService?.consumePurchase(BILLING_API_VERSION, activity.packageName, purchase.purchaseToken)
                return@async response == BILLING_RESPONSE_RESULT_OK
            } catch (e: RemoteException) {
                e.printStackTrace()
                return@async false
            }
        }.await()
    }

    private fun isBillingSupported(type: String): Boolean {
        return billingService?.isBillingSupported(BILLING_API_VERSION, activity.packageName, type) == BILLING_RESPONSE_RESULT_OK
    }

    private fun getResult(responseBundle: Bundle?, responseExtra: String): Int? {
        return responseBundle?.getInt(responseExtra)
    }

    private fun getPurchase(purchaseData: String): Purchase {
        val item = JSONObject(purchaseData)
        return Purchase(
                orderId = if (item.has("orderId")) item.getString("orderId") else null,
                productId = item.getString("productId"),
                purchaseTime = item.getLong("purchaseTime"),
                purchaseToken = item.getString("purchaseToken"),
                developerPayload = item.getString("developerPayload")
        )
    }

}