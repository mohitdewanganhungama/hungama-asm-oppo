package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.GetStoreRespModel
import com.hungama.music.data.model.UserPreviewDetails
import com.hungama.music.R
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.splash.SplashRespModel
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
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SongDurationRepos {
    private val TAG = javaClass.simpleName
    private var userPreviewDetails = MutableLiveData<Resource<UserPreviewDetails>>()

    fun getUserPreviewDetails(context: Context, masAllowedDuration:String): MutableLiveData<Resource<UserPreviewDetails>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.USER_PREVIEW_DETAILS + "?stream_max_minutes_allowed=$masAllowedDuration"

            val requestTime = DateUtils.getCurrentDateTime()
            userPreviewDetails.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (userPreviewDetails == null) {
                        userPreviewDetails = MutableLiveData<Resource<UserPreviewDetails>>()
                    }

                    try {
                        val detailModel = Gson().fromJson(
                            response.toString(),
                            UserPreviewDetails::class.java
                        ) as UserPreviewDetails

                        userPreviewDetails.postValue(Resource.success(detailModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "splash")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("splash").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("splash").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        userPreviewDetails.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    userPreviewDetails.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return userPreviewDetails
    }

    fun setUserPreviewDetails(context: Context, jsonData:JSONObject) {
        CoroutineScope(Dispatchers.IO).launch{
            val url= WSConstants.UPDATE_USER_PREVIEW_DETAILS

            val requestTime = DateUtils.getCurrentDateTime()
            userPreviewDetails.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonData,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    userPreviewDetails.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }
    }



}