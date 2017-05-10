# KinApp
[![Release](https://jitpack.io/v/StephenVinouze/KinApp.svg)](https://jitpack.io/#StephenVinouze/KinApp)
[![Build Status](https://travis-ci.org/StephenVinouze/KinApp.svg)](https://travis-ci.org/StephenVinouze/KinApp)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)
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
  compile "com.github.StephenVinouze.KinApp:core:1.0.0"
}
```

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
            // Purchase successfull with a non-null KinAppPurchase object.
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

## Pull requests

I welcome and encourage all pull requests. I might not be able to respond as fast as I would want to but I endeavor to be as responsive as possible.

All PR must:

1. Be written in Kotlin
2. Maintain code style
3. Indicate whether it is a enhancement, bug fix or anything else
4. Provide a clear description of what your PR brings
5. Enjoy coding in Kotlin :)
