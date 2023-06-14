package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.SearchRecommendationModel
import com.hungama.music.data.model.SearchRespModel
import com.hungama.music.data.model.SuggestionRespModel
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SearchRepos {
    private val TAG = javaClass.simpleName
    private var recommendationDataResp = MutableLiveData<Resource<SearchRecommendationModel>>()
    private var searchDataResp = MutableLiveData<Resource<SearchRespModel>>()
    private var searchAllDataWithPageWiseResp = MutableLiveData<Resource<SearchRespModel>>()
    private var suggestionDataResp = MutableLiveData<Resource<SuggestionRespModel>>()


    fun getRecommendationDataList(context: Context): MutableLiveData<Resource<SearchRecommendationModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_SEARCH_RECOMMENDATION

            val requestTime = DateUtils.getCurrentDateTime()
            recommendationDataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequestArray(context,url,object : DataValues {
                override fun setJsonArrayDataResponse(response: JSONArray?) {
                    if (recommendationDataResp == null) {
                        recommendationDataResp = MutableLiveData<Resource<SearchRecommendationModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<SearchRecommendationModel>(
                            response.toString(),
                            SearchRecommendationModel::class.java
                        ) as SearchRecommendationModel

                        recommendationDataResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "Recommendation")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("getCountryData").sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData("getCountryData").sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        recommendationDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setJsonDataResponse(response: JSONObject?) {

                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    recommendationDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return recommendationDataResp
    }

    fun getSearchData(context: Context,searchText:String): MutableLiveData<Resource<SearchRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_SEARCH_CONTENT+searchText

            val requestTime = DateUtils.getCurrentDateTime()
            searchDataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (searchDataResp == null) {
                        searchDataResp = MutableLiveData<Resource<SearchRespModel>>()
                    }

                    try {
                        val data = Gson().fromJson<SearchRespModel>(
                            response.toString(),
                            SearchRespModel::class.java
                        ) as SearchRespModel

                        searchDataResp.postValue(Resource.success(data))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "Search")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(searchText).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(searchText).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        searchDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    searchDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return searchDataResp
    }

    fun getAllSearchWithPageWiseData(context: Context,searchText:String,pageNo:Int): MutableLiveData<Resource<SearchRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_ALL_SEARCH_CONTENT+searchText+"?page="+pageNo


            var requestTime = DateUtils.getCurrentDateTime()
            searchAllDataWithPageWiseResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (searchAllDataWithPageWiseResp == null) {
                        searchAllDataWithPageWiseResp = MutableLiveData<Resource<SearchRespModel>>()
                    }

                    try {
                        val data = Gson().fromJson<SearchRespModel>(
                            response.toString(),
                            SearchRespModel::class.java
                        ) as SearchRespModel

                        searchAllDataWithPageWiseResp.postValue(Resource.success(data))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "Search")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(searchText).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(searchText).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        searchAllDataWithPageWiseResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    searchAllDataWithPageWiseResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return searchAllDataWithPageWiseResp
    }

    fun getSuggestionData(context: Context,searchText:String): MutableLiveData<Resource<SuggestionRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_AUTO_SUGGESTION+searchText


            var requestTime = DateUtils.getCurrentDateTime()
            suggestionDataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (suggestionDataResp == null) {
                        suggestionDataResp = MutableLiveData<Resource<SuggestionRespModel>>()
                    }

                    try {
                        val data = Gson().fromJson<SuggestionRespModel>(
                            response.toString(),
                            SuggestionRespModel::class.java
                        ) as SuggestionRespModel

                        setLog(
                            TAG,
                            "getSuggestionData: data:$data"
                        )

                        suggestionDataResp.postValue(Resource.success(data))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "Suggestion")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(searchText).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(searchText).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        suggestionDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    suggestionDataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return suggestionDataResp
    }


}