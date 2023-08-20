package com.son.app3.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.son.alarmclock.QuizPref
import com.son.alarmclock.activities.InPurchaseActivity
import com.son.alarmclock.databinding.ActUpPremiumBinding

class UpToPremiumAct : AppCompatActivity() {
    private lateinit var binding: ActUpPremiumBinding

    private val skuWeek = "com.son.alarmclock.weekpremium"
    private val skuMonth = "com.son.alarmclock.monthpremium"
    private val skuQuarter = "com.son.alarmclock.quarterpremium"

    private lateinit var currentUserId: String
    private lateinit var currentMarketplace: String
    private lateinit var quizPref: QuizPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActUpPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quizPref = QuizPref.getInstance()
        setupIAPOnCreate()
    }


    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {
                    UserDataResponse.RequestStatus.SUCCESSFUL -> {
                        currentUserId = response.userData.userId
                        currentMarketplace = response.userData.marketplace
                        quizPref.currentUserId(currentUserId)
                        Log.v("IAP SDK", "loaded userdataResponse")
                    }

                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED ->                         // Fail gracefully.
                        Log.v("IAP SDK", "loading failed")
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {

                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                            Log.v(
                                "Product:", String.format(
                                    "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                    product!!.title,
                                    product.productType,
                                    product.sku,
                                    product.price,
                                    product.description
                                )
                            )
                        }
                        //get all unavailable SKUs
                        for (s in productDataResponse.unavailableSkus) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s")
                        }
                    }

                    ProductDataResponse.RequestStatus.FAILED -> Log.v("FAILED", "FAILED")
                    else -> {}
                }
            }

            override fun onPurchaseResponse(purchaseResponse: PurchaseResponse) {
                when (purchaseResponse.requestStatus) {
                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId,
                            FulfillmentResult.FULFILLED
                        )
                        if (!purchaseResponse.receipt.isCanceled) {
                            quizPref.setIsPremium(true)
                        } else {
                            quizPref.setIsPremium(false)
                        }
                        Log.v("FAILED", "FAILED")
                    }

                    PurchaseResponse.RequestStatus.FAILED -> {}
                    else -> {}
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                // Process receipts
                when (response.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            if (!receipt.isCanceled) {
                                quizPref.setIsPremium(true)
                            } else {
                                quizPref.setIsPremium(false)
                            }
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }
                    }

                    PurchaseUpdatesResponse.RequestStatus.FAILED -> Log.d("FAILED", "FAILED")
                    else -> {}
                }
            }
        }
        PurchasingService.registerListener(this.applicationContext, purchasingListener)
        Log.d(
            "DetailBuyAct",
            "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        )
    }

    override fun onStart() {
        super.onStart()
        val productSkus: MutableSet<String> = HashSet()
        productSkus.add(skuWeek)
        productSkus.add(skuMonth)
        productSkus.add(skuQuarter)
        PurchasingService.getProductData(productSkus)
    }

    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        binding.cvWeek.setOnClickListener { PurchasingService.purchase(skuWeek) }
        binding.cvMonth.setOnClickListener { PurchasingService.purchase(skuMonth) }
        binding.cvQuarter.setOnClickListener { PurchasingService.purchase(skuQuarter) }
        binding.cvInApp.setOnClickListener { startActivity(Intent(this, InPurchaseActivity::class.java)) }
        binding.cvExit.setOnClickListener { finish() }
        PurchasingService.getPurchaseUpdates(false)
    }
}