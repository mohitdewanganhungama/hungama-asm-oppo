package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.BaseSuccessRespModel
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.webservice.WSConstants
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
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.HashMap
import java.util.concurrent.TimeUnit

class MusicRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<HomeModel>>()
    private var subtitleResp = MutableLiveData<Resource<String>>()
    private var updateUserAudioStreamResp = MutableLiveData<Resource<BaseSuccessRespModel>>()

    fun getMusicList(context: Context, url: String): MutableLiveData<Resource<HomeModel>> {
//        val currentSpan = Sentry.getSpan()
//        val span = currentSpan?.startChild("musicscreen_listing", javaClass.simpleName)
//            ?: Sentry.startTransaction("musicscreen_listing", "task")
        CoroutineScope(Dispatchers.IO).launch{
            setLog(TAG, "getMoreBucketListData: url:$url")
            var requestTime = DateUtils.getCurrentDateTime()
            updateUserAudioStreamResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (updateUserAudioStreamResp == null) {
                        dataResp = MutableLiveData<Resource<HomeModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<HomeModel>(
                            response.toString(),
                            HomeModel::class.java
                        ) as HomeModel

                        dataResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "musicscreen_listing")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "bottom_tab")
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, "bottom_tab")
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

//                        span.finish(SpanStatus.OK)
//
//                        val transaction = Sentry.getSpan()
//                        transaction?.finish(SpanStatus.OK)

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
//                        Sentry.captureException(exp!!)
//                        val transaction = Sentry.getSpan()
//                        transaction?.finish(SpanStatus.INTERNAL_ERROR)
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
//                dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
//                    Sentry.captureException(volleyError?.cause!!)
//                    val transaction = Sentry.getSpan()
//                    transaction?.finish(SpanStatus.INTERNAL_ERROR)
                }

            })

        }

        return dataResp
    }

    fun getSongLyricsList(context: Context, url: String): MutableLiveData<Resource<String>> {
        CoroutineScope(Dispatchers.IO).launch{
            setLog(TAG, "getMoreBucketListData: url:$url")

            var requestTime = DateUtils.getCurrentDateTime()
            subtitleResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getStringVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonStringDataResponse(response: String?) {
                    if (subtitleResp == null) {
                        subtitleResp = MutableLiveData<Resource<String>>()
                    }

                    try {

                        var newStr = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"),"UTF-8");
                        subtitleResp.postValue(Resource.success(newStr))


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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "songLyricsUrl")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "player"+HungamaMusicApp.getInstance().getEventData(url).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, "player"+HungamaMusicApp.getInstance().getEventData(url).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        subtitleResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }
                override fun setJsonDataResponse(response: JSONObject?) {
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    subtitleResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return subtitleResp
    }

    fun updateUserAudioStream(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseSuccessRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            setLog(TAG, "url:${url} json:${json}")
            updateContentValidity(context,json)

            var requestTime = DateUtils.getCurrentDateTime()
            updateUserAudioStreamResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (updateUserAudioStreamResp == null) {
                        updateUserAudioStreamResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<BaseSuccessRespModel>(
                            response.toString(),
                            BaseSuccessRespModel::class.java
                        ) as BaseSuccessRespModel

                        updateUserAudioStreamResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "stream")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "player" + HungamaMusicApp.getInstance().getEventData("stream").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "player" + HungamaMusicApp.getInstance().getEventData("stream").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        updateUserAudioStreamResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    updateUserAudioStreamResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return updateUserAudioStreamResp
    }

    fun updateContentValidity(
        context: Context,
        json: String){
        CoroutineScope(Dispatchers.IO).launch{
            val url= WSConstants.METHOD_UPDATE_CONTENT_VALIDITY
            setLog(TAG, "url:$url")

            val requestTime = DateUtils.getCurrentDateTime()
            DataManager.getInstance(context)?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {

                        setLog(
                            TAG,
                            "updateContentValidity response:${response}"
                        )


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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "update_content_validity")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("stream").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("stream").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                }

            })
        }


    }

    fun updateRadioListeningStream(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseSuccessRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var requestTime = DateUtils.getCurrentDateTime()
            updateUserAudioStreamResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context, url,JSONObject(json) , object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (updateUserAudioStreamResp == null) {
                        updateUserAudioStreamResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<BaseSuccessRespModel>(
                            response.toString(),
                            BaseSuccessRespModel::class.java
                        ) as BaseSuccessRespModel

                        updateUserAudioStreamResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "listening")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "player" + HungamaMusicApp.getInstance().getEventData("listening").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "player" + HungamaMusicApp.getInstance().getEventData("listening").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        updateUserAudioStreamResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    updateUserAudioStreamResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }


        return updateUserAudioStreamResp
    }
}