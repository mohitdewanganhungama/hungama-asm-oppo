package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.BaseSuccessRespModel
import com.hungama.music.data.model.MusicLanguageSelectionModel
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
import com.hungama.music.utils.Constant.EXTRA_VIDEO_SETTING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class MusicLangRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
    private var musicLanguageListResp = MutableLiveData<Resource<MusicLanguageSelectionModel>>()


    fun saveUserPref(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseSuccessRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            setLog(TAG, "url:$url")
            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<BaseSuccessRespModel>(
                            response.toString(),
                            BaseSuccessRespModel::class.java
                        ) as BaseSuccessRespModel

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "general_setting_details")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("user").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("user").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
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

    fun getMusicLanguageList(context: Context, isFromGenMusicSetting:Int = 0): MutableLiveData<Resource<MusicLanguageSelectionModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            var url = WSConstants.METHOD_MUSIC_LANGUAGE
            if (isFromGenMusicSetting == EXTRA_VIDEO_SETTING){
                url = WSConstants.METHOD_VIDEO_LANGUAGE
            }

            var requestTime = DateUtils.getCurrentDateTime()
            musicLanguageListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (musicLanguageListResp == null) {
                        musicLanguageListResp = MutableLiveData<Resource<MusicLanguageSelectionModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MusicLanguageSelectionModel>(
                            response.toString(),
                            MusicLanguageSelectionModel::class.java
                        ) as MusicLanguageSelectionModel

                        setLog(
                            TAG,
                            "getMusicLanguageList: resp:$dataModel"
                        )

                        musicLanguageListResp.postValue(Resource.success(dataModel))

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

                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )

                        if (isFromGenMusicSetting == EXTRA_VIDEO_SETTING){
                            url = WSConstants.METHOD_VIDEO_LANGUAGE
                            hashMap.put(EventConstant.NAME_EPROPERTY, "video_language")

                        }else{
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_music_language")
                        }
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("video_language").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("video_language").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        musicLanguageListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    musicLanguageListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return musicLanguageListResp
    }

    fun getMusicArtistList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url = WSConstants.METHOD_MUSIC_ARTIST + "?artistlang=" + SharedPrefHelper.getInstance()
                .getLanguage()


            var requestTime = DateUtils.getCurrentDateTime()
            musicLanguageListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (musicLanguageListResp == null) {
                        musicLanguageListResp = MutableLiveData<Resource<MusicLanguageSelectionModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MusicLanguageSelectionModel>(
                            response.toString(),
                            MusicLanguageSelectionModel::class.java
                        ) as MusicLanguageSelectionModel

                        musicLanguageListResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "artist_language")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "onboarding"
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "onboarding"
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        musicLanguageListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    musicLanguageListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return musicLanguageListResp
    }

    fun getVideoLanguageList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url = WSConstants.METHOD_VIDEO_LANGUAGE


            var requestTime = DateUtils.getCurrentDateTime()
            musicLanguageListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (musicLanguageListResp == null) {
                        musicLanguageListResp = MutableLiveData<Resource<MusicLanguageSelectionModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MusicLanguageSelectionModel>(
                            response.toString(),
                            MusicLanguageSelectionModel::class.java
                        ) as MusicLanguageSelectionModel

                        musicLanguageListResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "video_language")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "onboarding"
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "onboarding"
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        musicLanguageListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    musicLanguageListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return musicLanguageListResp
    }

    fun getVideoGenreList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url = WSConstants.METHOD_VIDEO_GENRE


            var requestTime = DateUtils.getCurrentDateTime()
            musicLanguageListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (musicLanguageListResp == null) {
                        musicLanguageListResp = MutableLiveData<Resource<MusicLanguageSelectionModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MusicLanguageSelectionModel>(
                            response.toString(),
                            MusicLanguageSelectionModel::class.java
                        ) as MusicLanguageSelectionModel

                        musicLanguageListResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "onboarding_movie_genre")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "onboarding"
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "onboarding"
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        musicLanguageListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    musicLanguageListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return musicLanguageListResp
    }
}