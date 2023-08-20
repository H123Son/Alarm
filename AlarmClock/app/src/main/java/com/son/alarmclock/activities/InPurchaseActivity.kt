package com.son.alarmclock.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.amazon.device.iap.model.PurchaseUpdatesResponse.RequestStatus.*
import com.son.alarmclock.QuizPref
import com.son.alarmclock.databinding.ActInPurchaseBinding

class InPurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActInPurchaseBinding
    private lateinit var quizPref: QuizPref

    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        PurchasingService.getPurchaseUpdates(false)
        val productSkus: MutableSet<String> = HashSet()
        productSkus.add("com.son.alarmclock.level.01")
        productSkus.add("com.son.alarmclock.level.02")
        productSkus.add("com.son.alarmclock.level.04")
        PurchasingService.getProductData(productSkus)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quizPref = QuizPref.getInstance()
        setupIAPOnCreate()
        binding = ActInPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonDone.setOnClickListener {
            finish()
        }

        binding.btnOpenStore1.setOnClickListener {
            PurchasingService.purchase("com.son.alarmclock.level.01")
        }
        binding.btnOpenStore2.setOnClickListener {
            PurchasingService.purchase("com.son.alarmclock.level.02")
        }
        binding.btnOpenStore3.setOnClickListener {
            PurchasingService.purchase("com.son.alarmclock.level.04")
        }
    }

    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {
                    UserDataResponse.RequestStatus.SUCCESSFUL -> {
                        binding.btnOpenStore1.isEnabled = true
                        binding.btnOpenStore2.isEnabled = true
                        binding.btnOpenStore3.isEnabled = true
                        quizPref.currentUserId(response.userData.userId)
                    }
                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED ->
                        Log.v("IAP SDK", "loading failed")
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                        }
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
                        if (purchaseResponse.receipt.sku == "com.son.alarmclock.level.01") {
                            quizPref.countPlayers(5)
                        }
                        if (purchaseResponse.receipt.sku == "com.son.alarmclock.level.02") {
                            quizPref.countPlayers(10)
                        }
                        if (purchaseResponse.receipt.sku == "com.son.alarmclock.level.04") {
                            quizPref.countPlayers(15)
                        }
                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId,
                            FulfillmentResult.FULFILLED
                        )
                        Log.v("FAILED", "FAILED")
                    }
                    PurchaseResponse.RequestStatus.FAILED -> {}
                    else -> {}
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                when (response.requestStatus!!) {
                    SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            if (!receipt.isCanceled) {
                                Log.d("onPurchaseUpdates", "xxxx1")
                            } else {
                                Log.d("onPurchaseUpdates", "xxxx2")
                            }
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }
                    }
                    FAILED -> Log.d("FAILED", "FAILED")
                    NOT_SUPPORTED -> TODO()
                }
            }
        }
        PurchasingService.registerListener(this.applicationContext, purchasingListener)
    }

}