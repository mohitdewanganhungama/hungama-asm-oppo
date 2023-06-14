package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.MoodRadioContentList
import com.hungama.music.data.model.MoodRadioFilterModel
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
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class MoodRadioRepos {
    private val TAG = javaClass.simpleName
    private var moodRadioPopupMoodListResp = MutableLiveData<Resource<MoodRadioFilterModel>>()
    private var moodRadioPopupTempoListResp = MutableLiveData<Resource<MoodRadioFilterModel>>()
    private var moodRadioPopupLanguageListResp = MutableLiveData<Resource<MoodRadioFilterModel>>()
    private var moodRadioContentListResp = MutableLiveData<Resource<MoodRadioContentList>>()
    fun getMoodRadioMoodPopupList(context: Context): MutableLiveData<Resource<MoodRadioFilterModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_DETAIL_PAGE+"mood-filter-mood"

            var requestTime = DateUtils.getCurrentDateTime()
            moodRadioPopupMoodListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (moodRadioPopupMoodListResp == null) {
                        moodRadioPopupMoodListResp = MutableLiveData<Resource<MoodRadioFilterModel>>()
                    }

                    try {
                        setLog("ResponseData", " Mood " + response.toString())
                        val moodRadioFilterModel = Gson().fromJson<MoodRadioFilterModel>(
                            response.toString(),
                            MoodRadioFilterModel::class.java
                        ) as MoodRadioFilterModel

                        moodRadioPopupMoodListResp.postValue(Resource.success(moodRadioFilterModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "mood_filter_mood")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("mood-filter-mood").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("mood-filter-mood").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioPopupMoodListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioPopupMoodListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return moodRadioPopupMoodListResp
    }

    fun getMoodRadioTempoPopupList(context: Context): MutableLiveData<Resource<MoodRadioFilterModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_DETAIL_PAGE+"mood-filter-tempo"

            val requestTime = DateUtils.getCurrentDateTime()
            moodRadioPopupTempoListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (moodRadioPopupTempoListResp == null) {
                        moodRadioPopupTempoListResp = MutableLiveData<Resource<MoodRadioFilterModel>>()
                    }

                    try {
                        setLog("ResponseData", " Tempo " + response.toString())
                        val moodRadioFilterModel = Gson().fromJson<MoodRadioFilterModel>(
                            response.toString(),
                            MoodRadioFilterModel::class.java
                        ) as MoodRadioFilterModel

                        moodRadioPopupTempoListResp.postValue(Resource.success(moodRadioFilterModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "mood_filter_tempo")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("mood-filter-tempo").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("mood-filter-tempo").sourceName)

                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioPopupTempoListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioPopupTempoListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return moodRadioPopupTempoListResp
    }

    fun getMoodRadioLanguagePopupList(context: Context): MutableLiveData<Resource<MoodRadioFilterModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_DETAIL_PAGE+"mood-filter-lang"
            var requestTime = DateUtils.getCurrentDateTime()
            moodRadioPopupLanguageListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (moodRadioPopupLanguageListResp == null) {
                        moodRadioPopupLanguageListResp = MutableLiveData<Resource<MoodRadioFilterModel>>()
                    }

                    try {
                        setLog("ResponseData", " Lang " + response.toString())
                        val moodRadioFilterModel = Gson().fromJson<MoodRadioFilterModel>(
                            response.toString(),
                            MoodRadioFilterModel::class.java
                        ) as MoodRadioFilterModel

                        moodRadioPopupLanguageListResp.postValue(Resource.success(moodRadioFilterModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "mood_filter_lang")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("mood-filter-lang").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("mood-filter-lang").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioPopupLanguageListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioPopupLanguageListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return moodRadioPopupLanguageListResp
    }

    fun getMoodRadioContentList(context: Context,moodId:Int, page:Int, size:Int, language:String, era:String, tempo:String): MutableLiveData<Resource<MoodRadioContentList>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_DETAIL_CONTENT+"moodradio/"+moodId+"?pageNo="+page+"&size="+size+"&lang="+ SharedPrefHelper.getInstance().getLanguage()+"&data-lang="+language+"&data-era="+era+"&data-tempo="+tempo
            var requestTime = DateUtils.getCurrentDateTime()
            moodRadioContentListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (moodRadioContentListResp == null) {
                        moodRadioContentListResp = MutableLiveData<Resource<MoodRadioContentList>>()
                    }

                    try {
                        val moodRadioContentList = Gson().fromJson<MoodRadioContentList>(
                            response.toString(),
                            MoodRadioContentList::class.java
                        ) as MoodRadioContentList

                        moodRadioContentListResp.postValue(Resource.success(moodRadioContentList))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "mood_radio")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("moodradio").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("moodradio").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioContentListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioContentListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return moodRadioContentListResp
    }
}