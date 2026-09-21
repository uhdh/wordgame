package com.gamehub.wordgame.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BillingUiState(
    val ready: Boolean = false,
    val ownedThemes: Set<String> = emptySet(),
    val pendingThemes: Set<String> = emptySet(),
    val prices: Map<String, String> = emptyMap(),
    val message: String? = null
)

class BillingViewModel(application: Application) : AndroidViewModel(application), PurchasesUpdatedListener {
    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    private val detailsByTheme = mutableMapOf<String, ProductDetails>()
    private val themeByProductId = THEME_PRODUCT_IDS.entries.associate { (theme, product) -> product to theme }

    private val billingClient = BillingClient.newBuilder(application)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    init {
        connect()
    }

    private fun connect() {
        if (billingClient.isReady) {
            refresh()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    refresh()
                } else {
                    _state.update { it.copy(ready = false, message = "결제 서비스를 사용할 수 없습니다.") }
                }
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    private fun queryProducts() {
        val products = THEME_PRODUCT_IDS.values.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val prices = buildMap {
                queryResult.productDetailsList.forEach { details ->
                    val theme = themeByProductId[details.productId] ?: return@forEach
                    detailsByTheme[theme] = details
                    val offer = details.oneTimePurchaseOfferDetailsList?.firstOrNull()
                        ?: details.oneTimePurchaseOfferDetails
                    offer?.formattedPrice?.let { put(theme, it) }
                }
            }
            _state.update { it.copy(prices = prices) }
        }
    }

    fun refresh() {
        if (!billingClient.isReady) {
            connect()
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases, replaceOwned = true)
                _state.update { it.copy(ready = true) }
            }
        }
    }

    fun purchase(activity: Activity, themeKey: String) {
        if (canAccessTheme(themeKey, _state.value.ownedThemes)) return
        val details = detailsByTheme[themeKey]
        if (!billingClient.isReady || details == null) {
            _state.update { it.copy(message = "상품 정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.") }
            if (!billingClient.isReady) connect() else queryProducts()
            return
        }

        val offerToken = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
            ?: details.oneTimePurchaseOfferDetails?.offerToken
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { if (!offerToken.isNullOrBlank()) setOfferToken(offerToken) }
            .build()
        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.update { it.copy(message = "구매 화면을 열지 못했습니다. 다시 시도해 주세요.") }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty(), replaceOwned = false)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refresh()
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _state.update { it.copy(message = "구매를 완료하지 못했습니다. 다시 시도해 주세요.") }
        }
    }

    private fun processPurchases(purchases: List<Purchase>, replaceOwned: Boolean) {
        val purchasedThemes = purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap(Purchase::getProducts)
            .mapNotNull(themeByProductId::get)
            .toSet()
        val pendingThemes = purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PENDING }
            .flatMap(Purchase::getProducts)
            .mapNotNull(themeByProductId::get)
            .toSet()

        _state.update {
            it.copy(
                ownedThemes = if (replaceOwned) purchasedThemes else it.ownedThemes + purchasedThemes,
                pendingThemes = pendingThemes,
                message = if (pendingThemes.isNotEmpty()) "결제가 완료되면 테마가 자동으로 열립니다." else it.message
            )
        }

        purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach { purchase ->
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        _state.update { it.copy(message = "구매 승인 처리에 실패했습니다. 앱을 다시 열어 주세요.") }
                    }
                }
            }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    override fun onCleared() {
        billingClient.endConnection()
        super.onCleared()
    }
}
