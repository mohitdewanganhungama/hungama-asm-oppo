package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.DateUtils
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class ProductRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<ProductRespModel>>()
    private var dataRespProduct = MutableLiveData<Resource<ProductRespModel>>()
    private var productCategoryResp = MutableLiveData<Resource<ProductCategoryRespModel>>()
    private var redeemCoinsModelResp = MutableLiveData<Resource<RedeemCoinsModel>>()
    private var createOrderModelResp = MutableLiveData<Resource<CreateOrderModel>>()
    private var userOrdersResp = MutableLiveData<Resource<UserOrdersModel>>()



    fun getProductList(context: Context): MutableLiveData<Resource<ProductRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_REWARDS_PRODUCTLIST+"products"


            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<ProductRespModel>>()
                    }

                    try {
                        val respModel = Gson().fromJson<ProductRespModel>(
                            response.toString(),
                            ProductRespModel::class.java
                        ) as ProductRespModel

                        dataResp.postValue(Resource.success(respModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            TAG,
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "rewardproduct_list")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("products").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("products").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return dataResp
    }

    fun getProductDetails(context: Context, productId:String): MutableLiveData<Resource<ProductRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_REWARDS_PRODUCT_DETAIL+productId


            var requestTime = DateUtils.getCurrentDateTime()
            dataRespProduct.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataRespProduct == null) {
                        dataRespProduct = MutableLiveData<Resource<ProductRespModel>>()
                    }

                    try {
                        setLog(
                            "MyOrder",
                            "ProductRepos-getProductDetails-Responce-1-${response}"
                        )
                        val respModel = Gson().fromJson<ProductRespModel>(
                            response.toString(),
                            ProductRespModel::class.java
                        ) as ProductRespModel
                        setLog(
                            "MyOrder",
                            "ProductRepos-getProductDetails-Responce-2-${respModel}"
                        )
                        dataRespProduct.postValue(Resource.success(respModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            TAG,
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "rewardproduct_detail")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(productId).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(productId).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataRespProduct.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    dataRespProduct.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return dataRespProduct
    }

    fun getProductCategoryList(context: Context): MutableLiveData<Resource<ProductCategoryRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_REWARDS_PRODUCTLIST+"smart_collections"


            var requestTime = DateUtils.getCurrentDateTime()
            productCategoryResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (productCategoryResp == null) {
                        productCategoryResp = MutableLiveData<Resource<ProductCategoryRespModel>>()
                    }

                    try {
                        val respModel = Gson().fromJson<ProductCategoryRespModel>(
                            response.toString(),
                            ProductCategoryRespModel::class.java
                        ) as ProductCategoryRespModel

                        productCategoryResp.postValue(Resource.success(respModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            TAG,
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "reward_smart_collections")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("reward_smart_collections").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("reward_smart_collections").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        productCategoryResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    productCategoryResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return productCategoryResp
    }

    fun getProductCategoryProductList(context: Context, id: Long): MutableLiveData<Resource<ProductRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_REWARDS_PRODUCTLIST+"product/"+id


            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<ProductRespModel>>()
                    }

                    try {
                        val respModel = Gson().fromJson<ProductRespModel>(
                            response.toString(),
                            ProductRespModel::class.java
                        ) as ProductRespModel

                        dataResp.postValue(Resource.success(respModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            TAG,
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "reward_product_detail")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(""+id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(""+id).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return dataResp
    }

    fun setRedeemCoin(context: Context,coins:Int): MutableLiveData<Resource<RedeemCoinsModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject = JSONObject()
            jsonObject.put("uid", SharedPrefHelper.getInstance().getUserId())
            jsonObject.put("platformCode", Constant.PLATFORM_CODE)
            jsonObject.put("points", coins)



            setLog(TAG, "redeem coins: request:$jsonObject")
            val url=WSConstants.METHOD_REDEEM_USER_COINS



            redeemCoinsModelResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (redeemCoinsModelResp == null) {
                        redeemCoinsModelResp = MutableLiveData<Resource<RedeemCoinsModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<RedeemCoinsModel>(
                            response.toString(),
                            RedeemCoinsModel::class.java
                        ) as RedeemCoinsModel

                        redeemCoinsModelResp.postValue(Resource.success(detailModel))

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        redeemCoinsModelResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    redeemCoinsModelResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return redeemCoinsModelResp
    }

    fun setCreateOrder(
        context: Context,
        productIds: JSONArray,
        shippingDetailModel: ShippingDetailModel,
        redeemableCoins: Int
    ): MutableLiveData<Resource<CreateOrderModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject = JSONObject()
            jsonObject.put("user_id", SharedPrefHelper.getInstance().getUserShopifyId())
            jsonObject.put("uid", SharedPrefHelper.getInstance().getUserId())
            //jsonObject.put("user_id", "5769158131905")
            jsonObject.put("productId", productIds)
            jsonObject.put("platformCode", Constant.PLATFORM_CODE2)
            jsonObject.put("email", SharedPrefHelper.getInstance().getUserEmail())
            jsonObject.put("phone", SharedPrefHelper.getInstance().getUserPhone())
            jsonObject.put("points", redeemableCoins)
            if (shippingDetailModel.isPhisicalProduct){
                val shippingJsonObject = JSONObject()
                shippingJsonObject.put("first_name", shippingDetailModel.firstName)
                shippingJsonObject.put("last_name", shippingDetailModel.lastName)
                shippingJsonObject.put("name", shippingDetailModel.firstName + " " + shippingDetailModel.lastName)
                shippingJsonObject.put("phone", shippingDetailModel.mobile)
                shippingJsonObject.put("zip", shippingDetailModel.pincode)
                shippingJsonObject.put("address1", shippingDetailModel.address1)
                shippingJsonObject.put("address2", shippingDetailModel.address2)
                shippingJsonObject.put("city", shippingDetailModel.city)
                shippingJsonObject.put("province", shippingDetailModel.state)
                shippingJsonObject.put("province_code", "")
                shippingJsonObject.put("country", "")
                shippingJsonObject.put("country_code", Constant.DEFAULT_COUNTRY_CODE)
                shippingJsonObject.put("company", "")
                shippingJsonObject.put("latitude", "")
                shippingJsonObject.put("longitude", "")


                jsonObject.put("billing_address", shippingJsonObject)
            }
            val jsonObjectMain = JSONObject().put("line_items", jsonObject)




            setLog(TAG, "createOrder: request:$jsonObjectMain")
            val url=WSConstants.METHOD_CREATE_ORDER


            createOrderModelResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObjectMain,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (createOrderModelResp == null) {
                        createOrderModelResp = MutableLiveData<Resource<CreateOrderModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<CreateOrderModel>(
                            response.toString(),
                            CreateOrderModel::class.java
                        ) as CreateOrderModel

                        createOrderModelResp.postValue(Resource.success(detailModel))

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        createOrderModelResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    //createOrderModelResp.postValue(Resource.error("UnAvailable zip code", null))
                    createOrderModelResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }


        return createOrderModelResp
    }

    fun getUserOrdersList(context: Context): MutableLiveData<Resource<UserOrdersModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val userId = SharedPrefHelper.getInstance().getUserShopifyId()
            val url=WSConstants.METHOD_USER_ORDER+"$userId/orders/"
            //val url="https://rewards.api.hungama.com/v1/order/orders"


            userOrdersResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (userOrdersResp == null) {
                        userOrdersResp = MutableLiveData<Resource<UserOrdersModel>>()
                    }

                    try {
                        val respModel = Gson().fromJson<UserOrdersModel>(
                            response.toString(),
                            UserOrdersModel::class.java
                        ) as UserOrdersModel

                        userOrdersResp.postValue(Resource.success(respModel))
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        userOrdersResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    userOrdersResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return userOrdersResp
    }
}