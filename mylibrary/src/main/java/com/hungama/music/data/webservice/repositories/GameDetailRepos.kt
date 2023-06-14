package com.hungama.music.data.webservice.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.GamelistModel
import com.hungama.music.data.model.RecommendationTrendingRespModel
import com.hungama.music.data.model.RecommendedSongListRespModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.DateUtils
import com.hungama.music.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GameDetailRepos {

    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<GamelistModel>>()
    private var recommendedListResp = MutableLiveData<Resource<RecommendedSongListRespModel>>()


    fun getgameDetail(context: Context, id: String): MutableLiveData<Resource<GamelistModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            val url = WSConstants.METHOD_PLAYABLE_CONTENT_DYNAMIC + id + "/games/detail"

            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (dataResp == null) {
                            dataResp = MutableLiveData<Resource<GamelistModel>>()
                        }

                        try {
                            val detailModel = Gson().fromJson<GamelistModel>(
                                response.toString(),
                                GamelistModel::class.java
                            ) as GamelistModel

                            dataResp.postValue(Resource.success(detailModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.time - responseTime.time)

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                val hashMap = HashMap<String, String>()
                                hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                                hashMap.put(
                                    EventConstant.NETWORKTYPE_EPROPERTY,
                                    "" + ConnectionUtil(context).networkType
                                )
                                hashMap.put(EventConstant.NAME_EPROPERTY, "game_detailsscreen")
                                hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                                hashMap.put(
                                    EventConstant.RESPONSECODE_EPROPERTY,
                                    EventConstant.RESPONSE_CODE_200
                                )

                                hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                                hashMap.put(
                                    EventConstant.URL_EPROPERTY,
                                    com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                                )

                                EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))
                            }


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
                        dataResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return dataResp
    }


}