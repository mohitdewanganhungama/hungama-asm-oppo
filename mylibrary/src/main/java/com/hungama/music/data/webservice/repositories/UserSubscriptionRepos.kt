package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UserSubscriptionRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<UserSubscriptionModel>>()
    private var userContentOrderStatusResp = MutableLiveData<Resource<ContentOrderStatusModel>>()
    private var contentsPlanDetailResp = MutableLiveData<Resource<String>>()
    private var cancelSubscriptionResp = MutableLiveData<Resource<CancelSubscriptionModel>>()
    private var rentedMovieListResp = MutableLiveData<Resource<RentedMovieRespModel>>()


    fun getRentedMovieList(context: Context): MutableLiveData<Resource<RentedMovieRespModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.UGC+ SharedPrefHelper.getInstance().getUserId()+"/purchasehistory"
//        val url = "https://ugc.api.hungama.com/v1/user/877752045/purchasehistory"

            var requestTime = DateUtils.getCurrentDateTime()
            rentedMovieListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (rentedMovieListResp == null) {
                        rentedMovieListResp = MutableLiveData<Resource<RentedMovieRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<RentedMovieRespModel>(
                            response.toString(),
                            RentedMovieRespModel::class.java
                        ) as RentedMovieRespModel

                        rentedMovieListResp.postValue(Resource.success(dataModel))



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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "purchasehistory")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "purchasehistory")
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, "purchasehistory")
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        rentedMovieListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    rentedMovieListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return rentedMovieListResp
    }

    fun getUserSubscriptionStatusDetail(context: Context): MutableLiveData<Resource<UserSubscriptionModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_USER_SUBSCRIPTION

            val jsonObject = JSONObject()
            jsonObject.put("identity", SharedPrefHelper.getInstance().getUserId())
            jsonObject.put("product_id", Constant.PRODUCT_ID)


            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<UserSubscriptionModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<UserSubscriptionModel>(
                            response.toString(),
                            UserSubscriptionModel::class.java
                        ) as UserSubscriptionModel

                        if(detailModel!=null){
                            Constant.SUBSCRIPTION_UPGRADABLE =
                                "" + detailModel?.data?.profile_app_config?.upgradable
                            SharedPrefHelper.getInstance().savePayUserDetail(PrefConstant.USER_PAY_DATA,detailModel)
                        }



                        dataResp.postValue(Resource.success(detailModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "subscription_status_check")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("subscription_status").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("subscription_status").sourceName)
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

    fun getUserContentOrderStatusCheck(context: Context,contentId:String): MutableLiveData<Resource<ContentOrderStatusModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject = JSONObject()
            jsonObject.put("identity", SharedPrefHelper.getInstance().getUserId())
            jsonObject.put("product_id", Constant.PRODUCT_ID)
            jsonObject.put("content_id", contentId)


            setLog(TAG, "getUserSubscriptionStatusDetail: request:$jsonObject")
            val url=WSConstants.METHOD_USER_CONTENT_ORDER_STATUS



            var requestTime = DateUtils.getCurrentDateTime()
            userContentOrderStatusResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (userContentOrderStatusResp == null) {
                        userContentOrderStatusResp = MutableLiveData<Resource<ContentOrderStatusModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<ContentOrderStatusModel>(
                            response.toString(),
                            ContentOrderStatusModel::class.java
                        ) as ContentOrderStatusModel

                        userContentOrderStatusResp.postValue(Resource.success(detailModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "check_content_status")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(contentId).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(contentId).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        userContentOrderStatusResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    userContentOrderStatusResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }



        return userContentOrderStatusResp
    }

    fun getPlanDetail(context: Context): MutableLiveData<Resource<String>> {

        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject = JSONObject()

            jsonObject.put("country", Constant.DEFAULT_COUNTRY_CODE)
            setLog("CCode", Constant.DEFAULT_COUNTRY_CODE)
            jsonObject.put("product_id", Constant.PRODUCT_ID)
            jsonObject.put("platform_id", Constant.PLATFORM_ID)




            val url=WSConstants.METHOD_PLAN_DETAILS

            setLog(TAG, "getUserSubscriptionStatusDetail: request:$jsonObject")

            val requestTime = DateUtils.getCurrentDateTime()
            contentsPlanDetailResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (contentsPlanDetailResp == null) {
                        contentsPlanDetailResp = MutableLiveData<Resource<String>>()
                    }

                    try {
                       /* val detailModel = Gson().fromJson<ContentsPlanDetailModel>(
                            response.toString(),
                            ContentsPlanDetailModel::class.java
                        ) as ContentsPlanDetailModel*/

                        contentsPlanDetailResp.postValue(Resource.success(response.toString()))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "pay_content_plans")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("pay_content_plans").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("pay_content_plans").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        contentsPlanDetailResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    contentsPlanDetailResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return contentsPlanDetailResp
    }

    fun getCancelPlan(context: Context, orderId: String): MutableLiveData<Resource<CancelSubscriptionModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject = JSONObject()
            jsonObject.put("country", Constant.DEFAULT_COUNTRY_CODE)
            setLog("CCode", Constant.DEFAULT_COUNTRY_CODE)
            jsonObject.put("product_id", Constant.PRODUCT_ID)
            jsonObject.put("platform_id", Constant.PLATFORM_ID)
            jsonObject.put("order_id", orderId)
            jsonObject.put("identity", SharedPrefHelper.getInstance().getUserId())



            setLog(TAG, "getUserSubscriptionStatusDetail: request:$jsonObject")
            val url=WSConstants.METHOD_CANCEL_PLAN



            var requestTime = DateUtils.getCurrentDateTime()
            cancelSubscriptionResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (cancelSubscriptionResp == null) {
                        cancelSubscriptionResp = MutableLiveData<Resource<CancelSubscriptionModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<CancelSubscriptionModel>(
                            response.toString(),
                            CancelSubscriptionModel::class.java
                        ) as CancelSubscriptionModel

                        cancelSubscriptionResp.postValue(Resource.success(detailModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "unsubscription")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("contentplans").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("contentplans").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        cancelSubscriptionResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    cancelSubscriptionResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return cancelSubscriptionResp
    }

    fun googleStoreNotifyBilling(context: Context, json: JSONObject): MutableLiveData<Resource<CancelSubscriptionModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_NOTIFY_BILLING
            var requestTime = DateUtils.getCurrentDateTime()
            cancelSubscriptionResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, json,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (cancelSubscriptionResp == null) {
                        cancelSubscriptionResp = MutableLiveData<Resource<CancelSubscriptionModel>>()
                    }

                    try {


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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "notifybilling")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("notifybilling").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("notifybilling").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                        val detailModel = Gson().fromJson<CancelSubscriptionModel>(
                            response.toString(),
                            CancelSubscriptionModel::class.java
                        ) as CancelSubscriptionModel

                        cancelSubscriptionResp.postValue(Resource.success(detailModel))

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        cancelSubscriptionResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    cancelSubscriptionResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }



        return cancelSubscriptionResp
    }

    fun getUpdatePaymentStatus(context: Context) {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_UPDATE_ORDER_STATUS

            val jsonObject = JSONObject()
            jsonObject.put("identity", SharedPrefHelper.getInstance().getUserId())
            jsonObject.put("product_id", Constant.PRODUCT_ID)

            DataManager.getInstance(context)?.putVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    setLog("getUpdatePaymentStatus", "response-${response.toString()}")
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                }

            })

        }
    }

}