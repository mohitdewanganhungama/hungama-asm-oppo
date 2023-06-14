package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.DailyDoseRespModel
import com.hungama.music.R
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.DateUtils
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class HomeRepos {
    private val TAG = javaClass.simpleName
    private var homeResp = MutableLiveData<Resource<HomeModel>>()
    private var dailyDoseResp = MutableLiveData<Resource<DailyDoseRespModel>>()
    private var trendPodcastResp = MutableLiveData<Resource<PlaylistDynamicModel>>()
    fun getHomeListDataLatest(context: Context, url: String): MutableLiveData<Resource<HomeModel>> {
//        val currentSpan = Sentry.getSpan()
//        val span = currentSpan?.startChild("discoverscreen_listing", javaClass.simpleName)
//            ?: Sentry.startTransaction("discoverscreen_listing", "task")

        CoroutineScope(Dispatchers.IO).launch{
            try {
                var requestTime = DateUtils.getCurrentDateTime()
                homeResp.postValue(Resource.loading(null))
                DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (homeResp == null) {
                            homeResp = MutableLiveData<Resource<HomeModel>>()
                        }

                        try {
                            val homeModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            //setLog("HomeApiResponse", "url-$url \nresponse-$response")
                            homeResp.postValue(Resource.success(homeModel))

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
                            hashMap.put(EventConstant.NAME_EPROPERTY, "discoverscreen_listing")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "bottom_tab")
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, "bottom_tab")
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                            hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

//                            span.finish(SpanStatus.OK)
//
//                            val transaction = Sentry.getSpan()
//                            transaction?.finish(SpanStatus.OK)

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            homeResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
//                            Sentry.captureException(exp!!)
//                            val transaction = Sentry.getSpan()
//                            transaction?.finish(SpanStatus.INTERNAL_ERROR)
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
//                        Sentry.captureException(volleyError?.cause!!)
//                        val transaction = Sentry.getSpan()
//                        transaction?.finish(SpanStatus.INTERNAL_ERROR)
//                homeResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })
            }catch (e:Exception){
                homeResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
//                Sentry.captureException(e)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
            }

        }


        return homeResp
    }

    fun getCommonRecommendation(context: Context, url: String): MutableLiveData<Resource<DailyDoseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var requestTime = DateUtils.getCurrentDateTime()
            homeResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dailyDoseResp == null) {
                        dailyDoseResp = MutableLiveData<Resource<DailyDoseRespModel>>()
                    }

                    try {
                        val homeModel = Gson().fromJson<DailyDoseRespModel>(
                            response.toString(),
                            DailyDoseRespModel::class.java
                        ) as DailyDoseRespModel

                        dailyDoseResp.postValue(Resource.success(homeModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "Daily_dose")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("home").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("home").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dailyDoseResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    dailyDoseResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return dailyDoseResp
    }

    fun getTrendingPodcastList(context: Context, url: String): MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var requestTime = DateUtils.getCurrentDateTime()
            homeResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (trendPodcastResp == null) {
                        trendPodcastResp = MutableLiveData<Resource<PlaylistDynamicModel>>()
                    }

                    try {
                        val homeModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        trendPodcastResp.postValue(Resource.success(homeModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "TrendingPodcastList")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("home").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("home").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        trendPodcastResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    trendPodcastResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return trendPodcastResp
    }
}