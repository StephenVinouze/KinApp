# Configure In App Purchase for Android
Adding a payment system on any mobile platform is a critical step that must be treated with special care. It often raises a number of questions, such as:


- How do I add my products in my application?
- Which types of products should I use?
- How to ensure that my purchase flow is fully supported?
  - Can I easily test my purchases?
  - Is there a way to test failure cases?
  - Will I be charged while testing my purchase flow?
  - How to distinguish between Test purchases and Real purchases?

This document intends to guide you through the process of **In App Purchase (IAP)** configuration so that you feel comfortable when shipping your product on the store. We limit here the scope to the Android platform, although there are many similarities with the iOS platform.

We assume here that your IAP development process is complete and fully working. We also assume that it has been tested thanks to the Static Responses that is offered to test mock products to determine if your code is correctly responding. More information is available [here](https://developer.android.com/google/play/billing/billing_testing.html#billing-testing-static).

# TL;DR


1. Create an application from the console and configure it.
2. Create a merchant account.
3. Add/update your In-app products.
4. Build a signed, release variant of your application.
5. Upload the application in the Alpha/Beta channel, publish it, and wait for it to be reviewed (can take several hours).
6. Download the release version onto your device either from the Play Store or from a release APK signed by the same keystore file.
7. Add your Gmail address as a test account in **Settings > Account Details > License Testing**.
8. Go to https://play.google.com/apps/testing/{your.package.name} from the Gmail account that has been added to the tester list. Accept the invitation.
9. When you get to the payment screen, you will have to enter real payment information, even though you will not be charged for your purchases (if you are logged with a tester account).


# Configure your PlayStore account

Before going any further, you need to be aware that adding **IAP** to your application requires that you create a developer account on the developer console if you don’t have one yet. This step is not free (25$ one-time registration) and will let you publish to the PlayStore. More information about how to use the Google developer console is available [here](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en).

Once you are logged in on your developer account, create your application that will integrate the **IAP**. Keep going by filling all mandatory information about your application. This may look premature at this step of your development as you may not be ready to launch to production yet but you won’t be able to add your products and test your purchase flow until your application is fully configured. Note that most of the required information are to be filled in the **Store presence** panel. This includes :


- Basic information (title, description, etc.)
- A couple of screenshots to illustrate your application.
- A couple of images such as your application’s icon, your application’s banner that will be displayed on the PlayStore mobile application, etc.
- Answering a few forms about what your application does.



https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494582146587_Group+19.png


Once every field has been filled, your application is ready for launch. Now go to **Store presence > In-app products**. If you haven’t already done it you will be prompted to create a merchant account that will allow your company to be paid for any **IAP** you received from your application.

🔥 🔥 🔥 Be aware that creating merchant account is available to only a limited set of countries. be sure that your company matches this requirement before going any further or you won’t be able to integrate **IAP** for your application.

With your merchant account configured you can now create your products that you want to propose to your end users.

# Configure your In-app products

When adding In-app products you will be asked which type of products you want to use in your application. There are two distinct types of products:


- Managed products
- Subscriptions

Subscriptions behave a bit differently than In-app products. Because we haven’t had the opportunity to implement subscription as of now, this won’t be discussed in this document. We can have more information [here](https://developer.android.com/google/play/billing/billing_subscriptions.html) though. Let us focus on the managed products.

Click on ADD NEW PRODUCT and select a product id. If you are using an external API to fetch product information (for instance if you are developing cross-platform application) be sure that the product id matches the one you will receive from your API. 

♨️ ♨️ ♨️ The product id cannot be changed once created!


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494582171890_Group+20.png


Now fill in the title, description and price that will be applied to this newly created product. 

🔥 🔥 🔥 The price may vary from one country to another so be sure that you manage. Also, the price is tax excluded so you must take the tax into account when declaring your tax-free price (which depends on the country).

Once your product is properly configured, save it. Remember to activate the product once saved or it won’t be reachable when fetching your In-app products. You should have the following screen :


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494582186808_Group+21.png


You are now ready to test your **IAP** flow by fetching your In-app products and attempt payment on them.

# Test your In-app products

Testing **IAP** is a critical step that must not be overlooked. You must ensure that your application can safely provide successful payment as well as handling payment errors. Additionally, you should provide and test that any payment that are successful are valid by checking the Google receipt in order to prevent fake payments. Also, while verifying all those cases, you want to be sure that you won’t be charged for any payment you will have to do for your tests.


## Activate your In-app products

Once your In-app products are created and activated, your first reflex will be to launch your application to see if you can retrieve and buy your In-app products. Although fetching your products will be possible, none of them will be accessible for payment. This is what you should see :


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494579741601_Capture+decran+2017-05-12+a+11.01.25.png


In fact **IAP** is only available for all applications that have been submitted to the PlayStore. This is why we’ve been all this trouble previously to configure your PlayStore account so that you could publish your application and test your **IAP**.

You will need to build a signed APK in release mode and publish it to the PlayStore. Use the Alpha or Beta channel in order to keep the publication limited to a set of known users. This can be achieved in the Release management panel, on the App releases section :


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1493212612084_Capture+decran+2017-04-26+a+15.15.36.png


Configure your Alpha/Beta publication and upload your release artifact. Save it and validate it for review.

🌟 The review can take up to a few hours before your application will be published on the PlayStore.

Remember to manage your Beta-tester list that will be able to download the application once published. This can be done while creating your Alpha/Beta release or more generically from **Settings > Manage testers** :


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494582213165_Group+23.png


Any user on this list will be invited to test your application. Before being able to download it (assuming it is published), log in to your Gmail account that is listed in the Beta tester list and go to the following link :

https://play.google.com/apps/testing/{your.package.name}

where `{your.package.name}` must be replaced by your application’s package name. Accept the invitation. You should now be able to download the application from the PlayStore once validated by Google. You will then be able to process your purchases. 

🌟 As a developer you are not limited to use the application from the PlayStore to test your **IAP**. This can be particularly difficult to debug your application by using this channel. We recommend you to create a specific  `InApp BuildType`  that is a duplicate from the `Release BuildType` that is debuggable by adding `debuggable true` . Deploy on your device a {flavor}InApp BuildVariant` (if you support product flavors) that will mimic the release version from the PlayStore but in a debuggable state.


## Distinguish Test purchases between Real purchases

You have now an application fully configured, deployed to the PlayStore with some In-app products that you are able to fetch. If you try now to purchase your product, without any other configuration, you are testing Real purchases and **will be charged accordingly** !

To prevent this to happen you will need to add your email address in the License testing list. This will bypass the payment process and you will be able to process the **IAP** without being charged.

♨️ ♨️ ♨️ Do not confuse the License testing list with the Beta tester list. Although it looks similar, they are not located at the same place and have a different purpose :


- Beta tester list allows users to download Alpha/Beta application submitted on the PlayStore
- License testing list allows users to process IAP without being charged

License testing list can be found in the **Settings > Developer account > Account details**


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494582233533_Group+17.png


Go back to your application and re-attempt to buy a product. Your payment dialog should be slightly different as it now presents a “debug” label disclaiming that this is a test purchase. That should reassure you about the nature of your payment 😎.


https://d2mxuefqeaa7sj.cloudfront.net/s_757AD9C7B16DC9184751AE3B9EA39F4B7C6F77F739843AED6DA610D1B2B63613_1494582258422_Group+18.png


You are now ready to fully test your IAP flow without having to worry about if you will be charged while debugging your application purchases. 

