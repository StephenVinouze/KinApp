package com.github.stephenvinouze.kinapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stephenvinouze.core.managers.KinAppManager
import com.github.stephenvinouze.core.models.KinAppProduct
import com.github.stephenvinouze.core.models.KinAppProductType
import com.github.stephenvinouze.core.models.KinAppPurchase
import com.github.stephenvinouze.core.models.KinAppPurchaseResult
import kotlinx.coroutines.experimental.runBlocking

class MainActivity : AppCompatActivity(), KinAppManager.KinAppListener {

    @BindView(R.id.fetch_products_button)
    lateinit var fetchProductsButton: Button

    @BindView(R.id.buy_available_product_button)
    lateinit var buyProductButton: Button

    @BindView(R.id.consume_purchases_button)
    lateinit var consumePurchasesButton: Button

    @BindView(R.id.restore_purchases_button)
    lateinit var restorePurchasesButton: Button

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     *  You must insert your developer payload here in order to test products from your app with their product_id
     *
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    private val billingManager = KinAppManager(this, "YOUR_DEVELOPER_PAYLOAD_HERE")
    private var products: MutableList<KinAppProduct>? = null
    private var purchases: MutableList<KinAppPurchase>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        fetchProductsButton.isEnabled = false
        buyProductButton.isEnabled = false
        consumePurchasesButton.isEnabled = false
        restorePurchasesButton.isEnabled = false

        billingManager.bind(this)
    }

    override fun onDestroy() {
        billingManager.unbind()
        super.onDestroy()
    }

    private fun displayPurchaseDialog(title: String, content: String) {
        MaterialDialog.Builder(this)
                .title(title)
                .content(content)
                .positiveText(android.R.string.ok)
                .show()
    }

    @OnClick(R.id.fetch_products_button)
    fun onFetchProductsClick() {
        runBlocking {
            products = billingManager.fetchProducts(arrayListOf(KinAppManager.TEST_PURCHASE_SUCCESS), KinAppProductType.INAPP) as MutableList<KinAppProduct>
            Toast.makeText(this@MainActivity, "Fetched " + products?.size + " products", Toast.LENGTH_LONG).show()
            if (products?.isNotEmpty() == true)
                buyProductButton.isEnabled = true
        }
    }

    @OnClick(R.id.buy_available_product_button)
    fun onBuyAvailableProductClick() {
        products?.first()?.let {
            billingManager.purchase(this, it.product_id, KinAppProductType.INAPP)
        }
    }

    @OnClick(R.id.consume_purchases_button)
    fun onConsumePurchasesClick() {
        purchases?.forEach {
            runBlocking {
                billingManager.consumePurchase(it)
                consumePurchasesButton.isEnabled = false
            }
        }
        Toast.makeText(this, "Consumed " + purchases?.size + " purchases", Toast.LENGTH_LONG).show()
    }

    @OnClick(R.id.restore_purchases_button)
    fun onRestorePurchasesClick() {
        purchases = billingManager.restorePurchases(KinAppProductType.INAPP) as MutableList<KinAppPurchase>
        Toast.makeText(this, "Restored " + purchases?.size + " purchases", Toast.LENGTH_LONG).show()
        if (purchases?.isNotEmpty() == true)
            consumePurchasesButton.isEnabled = true
    }

    override fun onBillingReady() {
        fetchProductsButton.isEnabled = true
        restorePurchasesButton.isEnabled = true
    }

    override fun onPurchaseFinished(purchaseResult: KinAppPurchaseResult, purchase: KinAppPurchase?) {
        when (purchaseResult) {
            KinAppPurchaseResult.SUCCESS -> {
                purchase?.let {
                    purchases?.add(purchase)
                    consumePurchasesButton.isEnabled = true
                    Toast.makeText(this, "Product successfully bought", Toast.LENGTH_LONG).show()
                }
            }
            KinAppPurchaseResult.ALREADY_OWNED -> {
                displayPurchaseDialog(title = "Purchase already owned", content = "You already own this item. If you need to buy it again, consider consuming it first (you may need to restore your purchases before that)")
            }
            KinAppPurchaseResult.INVALID_PURCHASE -> {
                displayPurchaseDialog(title = "Error while buying item", content = "Purchase invalid")
            }
            KinAppPurchaseResult.INVALID_SIGNATURE -> {
                displayPurchaseDialog(title = "Error while buying item", content = "Signature invalid")
            }
            KinAppPurchaseResult.CANCEL -> {
                displayPurchaseDialog(title = "Purchase canceled", content = "You have canceled your purchase")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingManager.verifyPurchase(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
