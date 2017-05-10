package com.github.stephenvinouze.kinapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stephenvinouze.core.managers.BillingManager
import com.github.stephenvinouze.core.models.Purchase
import kotlinx.coroutines.experimental.runBlocking

class MainActivity : AppCompatActivity(), BillingManager.BillingListener {

    @BindView(R.id.buy_available_product_button)
    lateinit var availableProductButton: Button

    @BindView(R.id.buy_unavailable_product_button)
    lateinit var unavailableProductButton: Button

    private val billingManager = BillingManager(this, "YOUR_DEVELOPER_PAYLOAD_HERE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        availableProductButton.isEnabled = false
        unavailableProductButton.isEnabled = false

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

    @OnClick(R.id.buy_available_product_button)
    fun onBuyAvailableClick() {
        billingManager.purchase(this, BillingManager.BILLING_TEST_PURCHASE_SUCCESS)
    }

    @OnClick(R.id.buy_unavailable_product_button)
    fun onBuyUnavailableClick() {
        billingManager.purchase(this, BillingManager.BILLING_TEST_PURCHASE_UNAVAILABLE)
    }

    override fun onBillingReady() {
        // Fetch all purchased items that has not been consumed yet and consume them
        billingManager.restorePurchases()?.forEach {
            runBlocking {
                billingManager.consumePurchase(it)
            }
        }

        availableProductButton.isEnabled = true
        unavailableProductButton.isEnabled = true
    }

    override fun onPurchaseFinished(purchaseResult: BillingManager.PurchaseResult, purchase: Purchase?) {
        when (purchaseResult) {
            BillingManager.PurchaseResult.SUCCESS -> {
                purchase?.let {
                    // Consume this purchase to be able to buy it again
                    runBlocking {
                        billingManager.consumePurchase(purchase)
                    }

                    displayPurchaseDialog(title = "Purchase successful", content = "You have successfully purchased this item")
                }
            }
            BillingManager.PurchaseResult.INVALID_PURCHASE -> {
                displayPurchaseDialog(title = "Error while buying item", content = "Purchase invalid")
            }
            BillingManager.PurchaseResult.INVALID_SIGNATURE -> {
                displayPurchaseDialog(title = "Error while buying item", content = "Signature invalid")
            }
            BillingManager.PurchaseResult.CANCEL -> {
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
