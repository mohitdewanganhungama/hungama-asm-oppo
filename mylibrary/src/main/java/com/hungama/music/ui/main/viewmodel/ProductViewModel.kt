package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.repositories.ProductRepos
import com.hungama.music.data.webservice.utils.Resource
import org.json.JSONArray

class ProductViewModel : ViewModel() {

    private var productRepos:ProductRepos?=null
    /**
     * getProduct list from API
     */

    fun getProductList(context: Context): MutableLiveData<Resource<ProductRespModel>>? {
        productRepos= ProductRepos()
        return productRepos?.getProductList(context)

    }

    /**
     * getProduct details from API
     */

    fun getProductDetails(context: Context, productId:String): MutableLiveData<Resource<ProductRespModel>>? {
        productRepos= ProductRepos()
        return productRepos?.getProductDetails(context, productId)

    }

    /**
     * getProductCategory list from API
     */

    fun getProductCategoryList(context: Context): MutableLiveData<Resource<ProductCategoryRespModel>>? {
        productRepos= ProductRepos()
        return productRepos?.getProductCategoryList(context)


    }

    /**
     * getProductCategoryProduct list from API
     */

    fun getProductCategoryProductList(context: Context, id: Long): MutableLiveData<Resource<ProductRespModel>>? {
        productRepos= ProductRepos()
        return productRepos?.getProductCategoryProductList(context,id)
    }

    /**
     * redeem coin API
     */

    fun setRedeemCoin(context: Context, coins:Int): MutableLiveData<Resource<RedeemCoinsModel>>? {
        productRepos= ProductRepos()
        return productRepos?.setRedeemCoin(context, coins)

    }

    /**
     * create order API
     */

    fun setCreateOrder(
        context: Context,
        productIds: JSONArray,
        shippingDetailModel: ShippingDetailModel,
        redeemableCoins: Int
    ): MutableLiveData<Resource<CreateOrderModel>>? {
        productRepos= ProductRepos()
        return productRepos?.setCreateOrder(context, productIds, shippingDetailModel, redeemableCoins)
    }

    /**
     * getUserOrdersList list from API
     */

    fun getUserOrdersList(context: Context): MutableLiveData<Resource<UserOrdersModel>>? {
        productRepos= ProductRepos()
        return productRepos?.getUserOrdersList(context)
    }
}