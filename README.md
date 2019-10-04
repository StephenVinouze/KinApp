# KinApp
[![Release](https://jitpack.io/v/StephenVinouze/KinApp.svg)](https://jitpack.io/#StephenVinouze/KinApp)
[![Build Status](https://travis-ci.org/StephenVinouze/KinApp.svg?branch=master)](https://travis-ci.org/StephenVinouze/KinApp)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-KinApp-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5744)
[![GitHub
license](http://img.shields.io/badge/license-APACHE2-blue.svg)](https://github.com/StephenVinouze/AdvancedRecyclerView/blob/master/LICENSE)

## Gradle Dependency

Add this in your root `build.gradle` file:

```gradle
allprojects {
	repositories {
		// ... other repositories
		maven { url "https://jitpack.io" }
	}
}
```
Then add the following dependency in your project.

```gradle
dependencies {
  compile "com.github.StephenVinouze:KinApp:{latest_version}"
}
```

:warning: Do not forget to enable coroutines in your project as this library needs them

## Configuring your InApp Purchase

Before integrating this library to manage your IAP, you probably must have already configured your application to integrate some products that you want your end users to buy. Understanding how this is done is rather well documented, but can be quite confusing as to how it really works. In addition to this library, I redacted [an article on Medium](https://medium.com/@s.vinouze/testing-in-app-purchase-for-android-4b62a7d1da42) that describes a complete workflow to properly configure your IAP step by step.

## Getting started

In your *AndroidManifest.xml* file, add the following line if your manifest merger is disabled :

```xml
 <uses-permission android:name="com.android.vending.BILLING" />
 ```

You will need a *developer payload* that can be found in your Google Developer Console from your already set up application. This id is uniquely attached to this application and will refer to your InApp products that you will declare.

In your calling Activity, instanciate the **KinAppManager** by adding the following lines :

```kotlin
private val billingManager = KinAppManager(this, "<YOUR_DEVELOPER_PAYLOAD_HERE>")

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.<your_activity_layout>)
    billingManager.bind(this)
}

override fun onDestroy() {
    billingManager.unbind()
    super.onDestroy()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (!billingManager.verifyPurchase(requestCode, resultCode, data)) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
```

Then implement the **KinAppListener** interface :

```kotlin
override fun onBillingReady() {
    // From this point you can use the Manager to fetch/purchase/consume/restore items
}

override fun onPurchaseFinished(purchaseResult: KinAppPurchaseResult, purchase: KinAppPurchase?) {
    // Handle your purchase result here
    when (purchaseResult) {
        KinAppPurchaseResult.SUCCESS -> {
            // Purchase successful with a non-null KinAppPurchase object.
            // You may choose to consume this item right now if you want to be able to re-buy it
        }
        KinAppPurchaseResult.ALREADY_OWNED -> {
            // You already own this item. If you need to buy it again, consider consuming it first (you may need to restore your purchases before that)
        }
        KinAppPurchaseResult.INVALID_PURCHASE -> {
            // Purchase invalid and cannot be processed
        }
        KinAppPurchaseResult.INVALID_SIGNATURE -> {
            // Marked as success from the Google Store but signature detected as invalid and should not be processed
        }
        KinAppPurchaseResult.CANCEL -> {
            // Manual cancel from the user
        }
    }
}
```

## Basic usage

Once your **KinAppManager** is configured, you can use the various features offered by this library. Be advised that the library uses the experimental Kotlin coroutines to elegantly handle asynchronous requests in order to avoid blocking the main thread. You can refer to the *sample* application if you need some help.

### Fetching products

As soon as you have configured your application on the Developer console and added some InApp products, you can retrieve them in your application ( :warning: Suspend call) :

```kotlin
launch(UI) {
	val products = billingManager.fetchProductsAsync(<your_products_id_here>, KinAppProductType.INAPP).await()
}
```

You can specify the nature of the product you want to fetch. It can be either of type **INAPP** or **SUBSCRIPTION**. This call is marked as *suspend* as it needs to be run asynchronously without blocking the main thread.

### Purchase product

Purchasing a product can be done using a *product_id* and by specifying the calling activity. This is mandatory in order to be called back in the `onActivityResult` method. Finally, same as fetching product, just specify the nature oy your product :

```kotlin
billingManager.purchase(this, <your_products_id_here>, KinAppProductType.INAPP)
```

### Consuming product

This part is only relevant to **INAPP** product types. A InApp product can be either consumable or one-time-purchase type. When you purchase an item, you own it. Unless you decide to consume it, you won't be able to re-purchase this item. This is important to understand this and distinguish between the two types of InApp products. You can consume an item by using these lines ( :warning: Suspend call) :

```kotlin
launch(UI) {
	val success = billingManager.consumePurchaseAsync(<your_purchase_object>).await()
}
```

### Restoring purchases

Finally, you must be able to handle purchase restoration in your application. This can be easily done using these lines :

```kotlin
val purchases = billingManager.restorePurchases(KinAppProductType.INAPP)
```

Note that this will only bring you all the purchases you own.

## Subscriptions

This library should be able to manage subscription and was designed that way. However, no subscription were made while developing this library hence cannot be marked as fully supported. Any test/review/PR is welcomed to fully support this ;)

## Testing InApp purchase

You can use the static responses provided by Google to test the basic cases and ensure your application is correctly responding even before adding any products to your *Developer Console*. Just use this :

```kotlin
billingManager.purchase(this, KinAppManager.TEST_PURCHASE_SUCCESS, KinAppProductType.INAPP)
```

This will present to you a test product dialog that always succeed. You can try the other static responses :

* TEST_PURCHASE_CANCELED
* TEST_PURCHASE_REFUNDED
* TEST_PURCHASE_UNAVAILABLE

## Pull requests

I welcome and encourage all pull requests. I might not be able to respond as fast as I would want to but I endeavor to be as responsive as possible.

All PR must:

1. Be written in Kotlin
2. Maintain code style
3. Indicate whether it is a enhancement, bug fix or anything else
4. Provide a clear description of what your PR brings
5. Enjoy coding in Kotlin :)
