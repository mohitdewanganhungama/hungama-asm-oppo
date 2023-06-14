package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.R
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.DateUtils
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.WSConstants.METHOD_DETAIL_CONTENT
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PlaylistRepos {
    private var playlistResp = MutableLiveData<Resource<PlaylistModel>>()
    private var playlistRespDynamic = MutableLiveData<Resource<PlaylistDynamicModel>>()
    private var recommendedListResp = MutableLiveData<Resource<RecommendedSongListRespModel>>()
    private var recommendedTrendingListResp = MutableLiveData<Resource<RecommendationTrendingRespModel>>()
    private var addSongResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
    private var playlistDeleted = MutableLiveData<Resource<Boolean>>()

    private var similarSongListResp = MutableLiveData<Resource<RecommendedSongListRespModel>>()
    private var updatePlaylistData = MutableLiveData<Resource<UpdateMyPlaylistModel>>()
    private var updateUserPlaylistContentReorderedDataResp = MutableLiveData<Resource<BaseRespModel>>()


    fun getPlaylistDetailListDynamic(
        context: Context,
        id: String,
        queryParam: String?
    ): MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var url = WSConstants.METHOD_PLAYABLE_CONTENT_DYNAMIC + id + "/playlist/detail"
            if(!queryParam?.isNullOrEmpty()!!){
                url=url+"?"+queryParam
            }

            val requestTime = DateUtils.getCurrentDateTime()
            playlistRespDynamic.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val playlistModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        playlistRespDynamic.postValue(Resource.success(playlistModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "playlist_detailscreen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistRespDynamic.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistRespDynamic.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return playlistRespDynamic
    }

    fun getPlaylistDetailList(
        context: Context,
        id: String
    ): MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url = WSConstants.METHOD_PLAYABLE_CONTENT_DYNAMIC + id + "/playlist/detail"

            val requestTime = DateUtils.getCurrentDateTime()
            playlistRespDynamic.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val playlistModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        playlistRespDynamic.postValue(Resource.success(playlistModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "playlist_detailscreen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistRespDynamic.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistRespDynamic.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return playlistRespDynamic
    }

    fun getMyPlaylistDetailList(
        context: Context,
        id: String
    ): MutableLiveData<Resource<PlaylistModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url =
                WSConstants.METHOD_USER_PLAYLIST + "playlist/" + id


            val requestTime = DateUtils.getCurrentDateTime()
            playlistResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val playlistModel = Gson().fromJson<PlaylistModel>(
                            response.toString(),
                            PlaylistModel::class.java
                        ) as PlaylistModel

                        playlistResp.postValue(Resource.success(playlistModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "user_playlist_detailscreen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return playlistResp
    }

    fun getRecommendedList(
        context: Context
    ): MutableLiveData<Resource<RecommendedSongListRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.PAGE+"/content/61314471/similar"


            val requestTime = DateUtils.getCurrentDateTime()
            recommendedListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val model = Gson().fromJson<RecommendedSongListRespModel>(
                            response.toString(),
                            RecommendedSongListRespModel::class.java
                        ) as RecommendedSongListRespModel

                        recommendedListResp.postValue(Resource.success(model))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "recommenation_similar")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("recommenation_similar").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("recommenation_similar").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        recommendedListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    recommendedListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return recommendedListResp
    }


    fun getRecommendedPlayList(
        context: Context,
        page: String="1"
    ): MutableLiveData<Resource<RecommendedSongListRespModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            var url=WSConstants.METHOD_PLAY_LIST_RECOMMENDATION
            if(WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                url=WSConstants.METHOD_RECOMMENDATION_TRENDING+"?limit=30&offset=${page}"
            }


            setLog("TAG", "PLAYLIST URL = " +url )


            val requestTime = DateUtils.getCurrentDateTime()
            recommendedListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    var model:RecommendedSongListRespModel?=null
                    try {

                        if(WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                            val trendingRespModel = Gson().fromJson<RecommendationTrendingRespModel>(
                                response.toString(),
                                RecommendationTrendingRespModel::class.java
                            ) as RecommendationTrendingRespModel



                            model=RecommendedSongListRespModel()

                            trendingRespModel?.body?.rows?.let {
                                trendingRespModel?.body?.rows?.get(0)?.items?.forEach {
                                    model?.data?.body?.similar?.add(it!!)
                                }
                            }



                        }else{
                            model = Gson().fromJson<RecommendedSongListRespModel>(
                            response.toString(),
                            RecommendedSongListRespModel::class.java
                            ) as RecommendedSongListRespModel
                        }




                        recommendedListResp.postValue(Resource.success(model))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "recommenation_similar")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("recommenation_similar").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("recommenation_similar").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        recommendedListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    recommendedListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }
        return recommendedListResp
    }


    fun addSong(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseSuccessRespModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            val requestTime = DateUtils.getCurrentDateTime()
            addSongResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context, url,JSONObject(json) , object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (addSongResp == null) {
                        addSongResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<BaseSuccessRespModel>(
                            response.toString(),
                            BaseSuccessRespModel::class.java
                        ) as BaseSuccessRespModel

                        addSongResp.postValue(Resource.success(dataModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            "TAG",
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "add_song_to_playlist")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("add_song_to_playlist").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("add_song_to_playlist").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        addSongResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    addSongResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return addSongResp
    }

    fun deleteMyPlaylist(
        context: Context,
        id: String
    ): MutableLiveData<Resource<Boolean>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_USER_PLAYLIST+"playlist/"+id
            var requestTime = DateUtils.getCurrentDateTime()
            playlistDeleted.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.deleteVolleyRequest(context, url,JSONObject() , object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (playlistDeleted == null) {
                        playlistDeleted = MutableLiveData<Resource<Boolean>>()
                    }

                    try {

                        playlistDeleted.postValue(Resource.success(true))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            "TAG",
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "deleteMyPlaylist")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistDeleted.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistDeleted.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }


        return playlistDeleted
    }


    fun deleteMyPlaylistContent(
        context: Context, contentId:String, playListId: String
    ): MutableLiveData<Resource<Boolean>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_USER_PLAYLIST+ SharedPrefHelper.getInstance().getUserId()+"/playlist/"+playListId+"/content/"+contentId


            var requestTime = DateUtils.getCurrentDateTime()
            playlistDeleted.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.deleteVolleyRequest(context, url,JSONObject() , object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (playlistDeleted == null) {
                        playlistDeleted = MutableLiveData<Resource<Boolean>>()
                    }

                    try {

                        playlistDeleted.postValue(Resource.success(true))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            "TAG",
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "deleteMyPlaylistContent")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(contentId).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(contentId).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistDeleted.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistDeleted.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return playlistDeleted
    }

    fun getSimilarSongList(
        context: Context,
        contentId: String
    ): MutableLiveData<Resource<RecommendedSongListRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url= "$METHOD_DETAIL_CONTENT$contentId/similar"


            val requestTime = DateUtils.getCurrentDateTime()
            similarSongListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val model = Gson().fromJson<RecommendedSongListRespModel>(
                            response.toString(),
                            RecommendedSongListRespModel::class.java
                        ) as RecommendedSongListRespModel

                        similarSongListResp.postValue(Resource.success(model))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "recommenation_similar")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("recommenation_similar").sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData("recommenation_similar").sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        similarSongListResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    similarSongListResp.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })

        }

        return similarSongListResp
    }

    fun getRecommendedContentListMyPlayList(context: Context, id1: String, id2: String, id3: String):
            MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var url=""
            try {
                if (WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY) {
                    url = WSConstants.METHOD_DISCOVER_RECOMMENDATION_REEL
                    /**
                     * 0-1 index both for recommendation real api
                     * >=2 index call auto-play api
                     */
                    val seed1=id1
                    val seed2=id2
                    val seed3=id3

//                var param="seed1=${HungamaMusicApp?.getInstance().userStreamIDList.keys.elementAt(Random.nextInt(0, HungamaMusicApp?.getInstance().userStreamIDList.keys?.size-1))}&seed2=${HungamaMusicApp?.getInstance().userStreamIDList.keys.elementAt(Random.nextInt(0, HungamaMusicApp?.getInstance().userStreamIDList.keys?.size-1))}&seed3=${HungamaMusicApp?.getInstance().userStreamIDList.keys.elementAt(Random.nextInt(0, HungamaMusicApp?.getInstance().userStreamIDList.keys?.size-1))}"
                    val param="seed1=${seed1}&seed2=${seed2}&seed3=${seed3}"
                    url = WSConstants.METHOD_RECOMMENDATION_AUTO_PLAY+"?"+param

                    setLog("canCallAutoPlayAPI","getRecommendedList -> userStreamIDList:${HungamaMusicApp.getInstance().userStreamIDList}")
                    setLog("canCallAutoPlayAPI","getRecommendedList -> param:${param}")


                    if(WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                        val messageModel = MessageModel(
                            "", param,
                            MessageType.NEGATIVE, true
                        )
//                            CommonUtils.showToast(context, messageModel)
                    }
                }else{
                    url = WSConstants.METHOD_DISCOVER_RECOMMENDATION
                }
            }catch (e:Exception){

            }
            val requestTime = DateUtils.getCurrentDateTime()
            playlistRespDynamic.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val playlistModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        setLog("getRecommendedList", "getRecommendedList: url:${url} playlistModel:${playlistModel}")

                        playlistRespDynamic.postValue(Resource.success(playlistModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "recommendation_similar")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id1).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id1).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistRespDynamic.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistRespDynamic.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })
        }


        return playlistRespDynamic
    }

    fun getRecommendedContentList(
        context: Context,
        id: String
    ): MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var url=""
            try {
                if (WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY)
                {
                    url = WSConstants.METHOD_DISCOVER_RECOMMENDATION
                    /**
                     * 0-1 index both for recommendation real api
                     * >=2 index call auto-play api
                     */
                    if(WSConstants.canCallAutoPlayAPI>=1!!){
                        var seed1="0"
                        var seed2="0"
                        var seed3="0"
                        if(HungamaMusicApp.getInstance().userStreamIDList.size>1){
                            seed1=HungamaMusicApp.getInstance().userStreamIDList.get(HungamaMusicApp.getInstance().userStreamIDList.size-1)
                        }

                        if(HungamaMusicApp.getInstance().userStreamIDList.size>2){
                            seed2=HungamaMusicApp.getInstance().userStreamIDList.get(HungamaMusicApp.getInstance().userStreamIDList.size-2)
                        }

                        if(HungamaMusicApp.getInstance().userStreamIDList.size>3){
                            seed3=HungamaMusicApp.getInstance().userStreamIDList.get(HungamaMusicApp.getInstance().userStreamIDList.size-3)
                        }


//                var param="seed1=${HungamaMusicApp?.getInstance().userStreamIDList.keys.elementAt(Random.nextInt(0, HungamaMusicApp?.getInstance().userStreamIDList.keys?.size-1))}&seed2=${HungamaMusicApp?.getInstance().userStreamIDList.keys.elementAt(Random.nextInt(0, HungamaMusicApp?.getInstance().userStreamIDList.keys?.size-1))}&seed3=${HungamaMusicApp?.getInstance().userStreamIDList.keys.elementAt(Random.nextInt(0, HungamaMusicApp?.getInstance().userStreamIDList.keys?.size-1))}"
                        val param="seed1=${seed1}&seed2=${seed2}&seed3=${seed3}"
                        url = WSConstants.METHOD_RECOMMENDATION_AUTO_PLAY+"?"+param

                        setLog("canCallAutoPlayAPI","getRecommendedList -> userStreamIDList:${HungamaMusicApp.getInstance().userStreamIDList}")
                        setLog("canCallAutoPlayAPI","getRecommendedList -> param:${param}")


                        if(WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                            val messageModel = MessageModel(
                                "", param,
                                MessageType.NEGATIVE, true
                            )
//                            CommonUtils.showToast(context, messageModel)
                        }

                    }

                    WSConstants.canCallAutoPlayAPI++

                }
                else{
                    url = WSConstants.METHOD_DISCOVER_RECOMMENDATION
                }
            }catch (e:Exception){

            }

            val requestTime = DateUtils.getCurrentDateTime()
            playlistRespDynamic.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY = false

                    try {
                        val playlistModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        setLog("getRecommendedList", "getRecommendedList: url:${url} playlistModel:${playlistModel}")

                        playlistRespDynamic.postValue(Resource.success(playlistModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "recommendation_similar")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistRespDynamic.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistRespDynamic.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })
        }


        return playlistRespDynamic
    }

    fun getBrandHubData(
        context: Context,
        deepLink: String
    ): MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var url=deepLink


            val requestTime = DateUtils.getCurrentDateTime()
            playlistRespDynamic.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {
                        val playlistModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        setLog("getBrandHubData", "getBrandHubData: url:${url} playlistModel:${playlistModel}")

                        playlistRespDynamic.postValue(Resource.success(playlistModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "Brandhub")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "Story"
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "Story"
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        playlistRespDynamic.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    playlistRespDynamic.postValue(
                        Resource.error(
                            context.getString(R.string.discover_str_2),
                            null
                        )
                    )
                }

            })
        }


        return playlistRespDynamic
    }

    fun updatePlaylistData(
        context: Context,
        url: String,
        json: JSONObject
    ): MutableLiveData<Resource<UpdateMyPlaylistModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            var requestTime = DateUtils.getCurrentDateTime()
            updatePlaylistData.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.putVolleyRequest(context, url, json, object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (updatePlaylistData == null) {
                        updatePlaylistData = MutableLiveData<Resource<UpdateMyPlaylistModel>>()
                    }

                    try {

                        val baseRespModel = Gson().fromJson<UpdateMyPlaylistModel>(
                            response.toString(),
                            UpdateMyPlaylistModel::class.java
                        ) as UpdateMyPlaylistModel
                        updatePlaylistData.postValue(Resource.success(baseRespModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "user_content_details")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "bottom_tab"
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "bottom_tab"
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        updatePlaylistData.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    updatePlaylistData.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }


        return updatePlaylistData
    }

    fun setPlaylistCountData(
        context: Context,
        url: String
    ) {

        CoroutineScope(Dispatchers.IO).launch{
            var requestTime = DateUtils.getCurrentDateTime()
            DataManager.getInstance(context)?.putVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {

                    try {

                        /*val baseRespModel = Gson().fromJson<UpdateMyPlaylistModel>(
                            response.toString(),
                            UpdateMyPlaylistModel::class.java
                        ) as UpdateMyPlaylistModel*/


                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "user_content_details")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "bottom_tab"
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "bottom_tab"
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        //EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

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

    fun updatePlaylistReorderedData(
        context: Context,
        url: String,
        json: JSONObject
    ): MutableLiveData<Resource<BaseRespModel>> {

        CoroutineScope(Dispatchers.IO).launch{
            updateUserPlaylistContentReorderedDataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context, url, json, object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (updateUserPlaylistContentReorderedDataResp == null) {
                        updateUserPlaylistContentReorderedDataResp = MutableLiveData<Resource<BaseRespModel>>()
                    }

                    try {

                        val baseRespModel = Gson().fromJson<BaseRespModel>(
                            response.toString(),
                            BaseRespModel::class.java
                        ) as BaseRespModel
                        /*if (!TextUtils.isEmpty(baseRespModel?.message)
                            && baseRespModel?.message?.equals("success", true)!!){
                            updateUserPlaylistContentReorderedDataResp.postValue(Resource.success(baseRespModel))
                        }else{
                            updateUserPlaylistContentReorderedDataResp.postValue(Resource.error(baseRespModel?.message!!, baseRespModel))
                        }*/

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        updateUserPlaylistContentReorderedDataResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    updateUserPlaylistContentReorderedDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }


        return updateUserPlaylistContentReorderedDataResp
    }
}