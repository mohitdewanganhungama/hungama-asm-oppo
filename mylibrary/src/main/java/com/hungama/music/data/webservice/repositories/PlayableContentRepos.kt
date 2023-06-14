package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.telephony.TelephonyManager
import android.text.TextUtils
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
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.getStreamUrl
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class PlayableContentRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<PlayableContentModel>>()
    private var downloadResp = MutableLiveData<Resource<DownloadableContentModel>>()
    private var moodRadioListResp = MutableLiveData<Resource<MoodRadioListRespModel>>()
    private var planInfoData = MutableLiveData<Resource<PlanInfoContentModel>>()

    fun getPlayableContentUrl(context: Context, id:String,contentType:Int): MutableLiveData<Resource<PlayableContentModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var type=4
            var userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
            if(contentType==4||contentType==93||contentType==98||contentType==22){
                type=5
                userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
            }

            var quality = Quality.AUTO.qualityPrefix
            if(!BaseActivity.isGoldUser && type == 4)
                quality = Quality.MEDIUM.qualityPrefix

            if(userSettingRespModel!=null){
                if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                    val streamQuality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality.toString()!!)
                    quality = streamQuality.qualityPrefix
                }
            }
            val url= WSConstants.METHOD_PLAYABLE_CONTENT+id+"/url/playable?quality="+quality+"&contentType="+type+"&certificate=widevine" + if(type == 4) "&user=" + if (CommonUtils.isUserHasGoldSubscription()) "gold" else "free" else ""

            setLog(TAG, "getMoreBucketListData: url:$url")
            HungamaMusicApp.getInstance().userStreamIDList.add(id)

            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<PlayableContentModel>>()
                    }

                    try {
                        var playableContentModel = Gson().fromJson<PlayableContentModel>(
                            response.toString(),
                            PlayableContentModel::class.java
                        ) as PlayableContentModel


                        if (playableContentModel.data.body.data.url.playable.size > 0) {
                            val streamQuality =
                                Quality.getServerKeyByName(quality).toString()
                            run loop@{
                                playableContentModel.data.body.data.url.playable.forEach {
                                    setLog(
                                        TAG,
                                        "getPlayableContentUrl: isUserHasGoldSubscription:${BaseActivity.getIsGoldUser()} streamQuality:${it?.key} "
                                    )
                                    setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-main===id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - streamQuality-$streamQuality- isGoldUser-${BaseActivity.getIsGoldUser()}")
                                    playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)

                                    /*if (getIsGoldUser()) {

                                        if (it.key.equals("dolby", true) || it.key.equals(
                                                "hd",
                                                true
                                            )
                                        ) {

                                            playableContentModel.data.head.headData.misc.url =
                                                it.data
                                            if (!TextUtils.isEmpty(it.token)) {
                                                playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                    it.token
                                            }
                                            playableContentModel.data.head.headData.misc.urlKey =
                                                it.key

                                            setLog(
                                                TAG,
                                                "getPlayableContentUrl: quality url match and update streamQuality if 0 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                            )
                                            setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-if 0==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                            return@loop
                                        }else{
                                            playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)
                                            setLog(
                                                TAG,
                                                "getPlayableContentUrl: quality url match and update streamQuality else 1 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                            )
                                            setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-else 1==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                        }

                                    }else{
                                        playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)
                                        setLog(
                                            TAG,
                                            "getPlayableContentUrl: quality url match and update streamQuality else 2 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                        )
                                        setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-else 2==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                    }*/

                                }
                            }


                            if (TextUtils.isEmpty(playableContentModel.data.head.headData.misc.url) && playableContentModel.data.body.data.url.playable.size > 0) {

                                if(BaseActivity.getIsGoldUser()){
                                    var tmpList=playableContentModel.data.body.data.url.playable?.filter { !it.key.equals("preview")}

                                    if(tmpList?.size!!>0){
                                        playableContentModel.data.head.headData.misc.url =
                                            tmpList.get(0).data

                                        if (!TextUtils.isEmpty(tmpList.get(0).token)) {
                                            playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                tmpList.get(0).token
                                        }
                                        playableContentModel.data.head.headData.misc.urlKey = tmpList.get(0).key
                                        setLog(TAG, "getPlayableContentUrl: getIsGoldUser:${BaseActivity.getIsGoldUser()} quality url default first ser:${tmpList.get(0).key} ")
                                    }else{
                                        playableContentModel.data.head.headData.misc.url =
                                            playableContentModel.data.body.data.url.playable.get(0).data

                                        if (!TextUtils.isEmpty(
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).token
                                            )
                                        ) {
                                            playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).token
                                        }
                                        playableContentModel.data.head.headData.misc.urlKey =
                                            playableContentModel.data.body.data.url.playable.get(0).key
                                        setLog(
                                            TAG,
                                            "getPlayableContentUrl: quality url default first ser:${
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).key
                                            } "
                                        )
                                    }

                                }else{
                                    playableContentModel.data.head.headData.misc.url =
                                        playableContentModel.data.body.data.url.playable.get(0).data

                                    if (!TextUtils.isEmpty(
                                            playableContentModel.data.body.data.url.playable.get(
                                                0
                                            ).token
                                        )
                                    ) {
                                        playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                            playableContentModel.data.body.data.url.playable.get(
                                                0
                                            ).token
                                    }
                                    playableContentModel.data.head.headData.misc.urlKey =
                                        playableContentModel.data.body.data.url.playable.get(0).key
                                    setLog(
                                        TAG,
                                        "getPlayableContentUrl: quality url default first ser:${
                                            playableContentModel.data.body.data.url.playable.get(
                                                0
                                            ).key
                                        } "
                                    )
                                }

                            }

                        }
                        setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-End-id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey} - isGoldUser-${BaseActivity.getIsGoldUser()}")


                        setEventModelDataAppLevel(playableContentModel,type)
                        dataResp.postValue(Resource.success(playableContentModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "playable_url")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
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


    fun getPlayableContentList(context: Context, id:String): MutableLiveData<Resource<PlayableContentModel>> {
        CoroutineScope(Dispatchers.IO).launch{

            var quality = Quality.AUTO.qualityPrefix
            if(!BaseActivity.isGoldUser)
                quality = Quality.MEDIUM.qualityPrefix

            val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
            if(userSettingRespModel!=null && userSettingRespModel?.data != null){
                if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                    val streamQuality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality.toString()!!)
                    quality = streamQuality.qualityPrefix
                }
            }
            val contentType=4
            val url= WSConstants.METHOD_PLAYABLE_CONTENT+id+"/url/playable?quality="+quality+"&contentType="+contentType+"&certificate=widevine"+ "&user=" + if (CommonUtils.isUserHasGoldSubscription()) "gold" else "free"
            HungamaMusicApp.getInstance().userStreamIDList.add(id)
            setLog("playableApi", "PlayableContentList: getPlayableContentList- $url")

            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<PlayableContentModel>>()
                    }

                    try {
                        var playableContentModel = Gson().fromJson<PlayableContentModel>(
                            response.toString(),
                            PlayableContentModel::class.java
                        ) as PlayableContentModel


                        if (playableContentModel.data.body.data.url.playable.size > 0) {
                            val streamQuality =
                                Quality.getServerKeyByName(quality).toString()
                            run loop@{
                                playableContentModel.data.body.data.url.playable.forEach {
                                    setLog(
                                        TAG,
                                        "getPlayableContentUrl: isUserHasGoldSubscription:${BaseActivity.getIsGoldUser()} streamQuality:${it?.key} "
                                    )
                                    setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-main===id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - streamQuality-$streamQuality- isGoldUser-${BaseActivity.getIsGoldUser()}")
                                    playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)

                                    /*if (getIsGoldUser()) {

                                        if (it.key.equals("dolby", true) || it.key.equals(
                                                "hd",
                                                true
                                            )
                                        ) {

                                            playableContentModel.data.head.headData.misc.url =
                                                it.data
                                            if (!TextUtils.isEmpty(it.token)) {
                                                playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                    it.token
                                            }
                                            playableContentModel.data.head.headData.misc.urlKey =
                                                it.key

                                            setLog(
                                                TAG,
                                                "getPlayableContentUrl: quality url match and update streamQuality if 0 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                            )
                                            setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-if 0==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                            return@loop
                                        }else{
                                            playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)
                                            setLog(
                                                TAG,
                                                "getPlayableContentUrl: quality url match and update streamQuality else 1 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                            )
                                            setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-else 1==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                        }

                                    }else{
                                        playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)
                                        setLog(
                                            TAG,
                                            "getPlayableContentUrl: quality url match and update streamQuality else 2 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                        )
                                        setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-else 2==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                    }*/

                                }
                            }


                            if (TextUtils.isEmpty(playableContentModel.data.head.headData.misc.url) && playableContentModel.data.body.data.url.playable.size > 0) {

                                if(BaseActivity.getIsGoldUser()){
                                    var tmpList=playableContentModel.data.body.data.url.playable?.filter { !it.key.equals("preview")}

                                    if(tmpList?.size!!>0){
                                        playableContentModel.data.head.headData.misc.url =
                                            tmpList.get(0).data

                                        if (!TextUtils.isEmpty(tmpList.get(0).token)) {
                                            playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                tmpList.get(0).token
                                        }
                                        playableContentModel.data.head.headData.misc.urlKey = tmpList.get(0).key
                                        setLog(TAG, "getPlayableContentUrl: getIsGoldUser:${BaseActivity.getIsGoldUser()} quality url default first ser:${tmpList.get(0).key} ")
                                    }else{
                                        playableContentModel.data.head.headData.misc.url =
                                            playableContentModel.data.body.data.url.playable.get(0).data

                                        if (!TextUtils.isEmpty(
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).token
                                            )
                                        ) {
                                            playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).token
                                        }
                                        playableContentModel.data.head.headData.misc.urlKey =
                                            playableContentModel.data.body.data.url.playable.get(0).key
                                        setLog(
                                            TAG,
                                            "getPlayableContentUrl: quality url default first ser:${
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).key
                                            } "
                                        )
                                    }

                                }else{
                                    playableContentModel.data.head.headData.misc.url =
                                        playableContentModel.data.body.data.url.playable.get(0).data

                                    if (!TextUtils.isEmpty(
                                            playableContentModel.data.body.data.url.playable.get(
                                                0
                                            ).token
                                        )
                                    ) {
                                        playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                            playableContentModel.data.body.data.url.playable.get(
                                                0
                                            ).token
                                    }
                                    playableContentModel.data.head.headData.misc.urlKey =
                                        playableContentModel.data.body.data.url.playable.get(0).key
                                    setLog(
                                        TAG,
                                        "getPlayableContentUrl: quality url default first ser:${
                                            playableContentModel.data.body.data.url.playable.get(
                                                0
                                            ).key
                                        } "
                                    )
                                }

                            }

                        }
                        setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-End-id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey} - isGoldUser-${BaseActivity.getIsGoldUser()}")


                        setEventModelDataAppLevel(playableContentModel,contentType)

                        dataResp.postValue(Resource.success(playableContentModel))

                        /**
                         * event property start
                         */

                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long = Math.abs(requestTime.time - responseTime.time)

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "playable_url")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
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

    fun getMoodRadioList(context: Context, id:String): MutableLiveData<Resource<MoodRadioListRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url=WSConstants.METHOD_MOOD_RADID_LIST+id+"?pageNo=0&size=20"


            var requestTime = DateUtils.getCurrentDateTime()
            moodRadioListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequestArray(context,url,object : DataValues {
                override fun setJsonArrayDataResponse(response: JSONArray?) {
                    if (moodRadioListResp == null) {
                        moodRadioListResp = MutableLiveData<Resource<MoodRadioListRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MoodRadioListRespModel>(
                            response.toString(),
                            MoodRadioListRespModel::class.java
                        ) as MoodRadioListRespModel

                        moodRadioListResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "moodradio_detailsscreen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setJsonDataResponse(response: JSONObject?) {

                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return moodRadioListResp
    }

    fun getOnDemandRadioList(context: Context, id:String): MutableLiveData<Resource<MoodRadioListRespModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url = WSConstants.METHOD_ON_DEMAND_RADIO_LIST + id + "/ondemandradio/detail?pageNo=0&size=20"


            var requestTime = DateUtils.getCurrentDateTime()
            moodRadioListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequestArray(context,url,object : DataValues {
                override fun setJsonArrayDataResponse(response: JSONArray?) {
                    if (moodRadioListResp == null) {
                        moodRadioListResp = MutableLiveData<Resource<MoodRadioListRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MoodRadioListRespModel>(
                            response.toString(),
                            MoodRadioListRespModel::class.java
                        ) as MoodRadioListRespModel

                        moodRadioListResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "radio_detailsscreen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setJsonDataResponse(response: JSONObject?) {

                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })


        }
        return moodRadioListResp
    }

    fun getArtistRadioList(context: Context, id:String): MutableLiveData<Resource<MoodRadioListRespModel>>  {
        CoroutineScope(Dispatchers.IO).launch{
            val url =
                WSConstants.METHOD_ON_DEMAND_RADIO_LIST + id + "/artistradio/detail?pageNo=0&size=20"


            var requestTime = DateUtils.getCurrentDateTime()
            moodRadioListResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequestArray(context,url,object : DataValues {
                override fun setJsonArrayDataResponse(response: JSONArray?) {
                    if (moodRadioListResp == null) {
                        moodRadioListResp = MutableLiveData<Resource<MoodRadioListRespModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<MoodRadioListRespModel>(
                            response.toString(),
                            MoodRadioListRespModel::class.java
                        ) as MoodRadioListRespModel

                        moodRadioListResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "artistradio_detail_screen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        moodRadioListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setJsonDataResponse(response: JSONObject?) {
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    moodRadioListResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return moodRadioListResp
    }

    fun getDownloadableContentUrl(context: Context, id:String,contentType:Int): MutableLiveData<Resource<DownloadableContentModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var quality = Quality.AUTO.qualityPrefix
            if(!BaseActivity.isGoldUser && contentType == 4)
                quality = Quality.MEDIUM.qualityPrefix

            val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_DOWNLOADS_SETTING)
            if(contentType==5){
                if(userSettingRespModel!=null && userSettingRespModel?.data != null){
                    if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.videoDownloadQuality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.videoDownloadQuality)){
                        val streamQuality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.videoDownloadQuality.toString()!!)
                        quality = streamQuality.qualityPrefix
                    }
                }
            }else{
                if(userSettingRespModel!=null && userSettingRespModel?.data != null){
                    if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.musicDownloadQuality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.musicDownloadQuality)){
                        val streamQuality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.musicDownloadQuality.toString()!!)
                        quality = streamQuality.qualityPrefix
                    }
                }
            }
            val url=WSConstants.METHOD_DOWNLOADABLE_CONTENT_LIST+id+"/downloadURL?quality=" + quality + "&contentType="+contentType + "&certificate=widevine"


            var requestTime = DateUtils.getCurrentDateTime()
            downloadResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        downloadResp = MutableLiveData<Resource<DownloadableContentModel>>()
                    }

                    try {
                        val dataModel = Gson().fromJson<DownloadableContentModel>(
                            response.toString(),
                            DownloadableContentModel::class.java
                        ) as DownloadableContentModel

                        setDownloadEventModelDataAppLevel(dataModel,contentType)

                        downloadResp.postValue(Resource.success(dataModel))

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "downloadURL")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        downloadResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    downloadResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return downloadResp
    }

    public fun setEventModelDataAppLevel(playableContentModel: PlayableContentModel, contentType: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            var eventModel: EventModel?=HungamaMusicApp?.getInstance()?.getEventData(playableContentModel.data.head.headData.id)

            if(eventModel==null ||TextUtils.isEmpty(eventModel?.contentID)){
                eventModel= EventModel()
            }

            if(TextUtils.isEmpty(eventModel?.contentID)){
                eventModel?.contentID=playableContentModel.data.head.headData.id
            }

            if(TextUtils.isEmpty(eventModel?.duration)){
                eventModel?.duration=""+playableContentModel?.data?.head?.headData?.duration
            }

            if(TextUtils.isEmpty(eventModel?.songName)){
                eventModel?.songName=""+playableContentModel?.data?.head?.headData?.title
            }

            if(playableContentModel?.data?.head?.headData?.misc?.actorf!=null&&playableContentModel.data.head.headData.misc.actorf.size>0){
//                eventModel?.actor=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.actorf)
                eventModel?.actor=playableContentModel?.data?.head?.headData?.misc?.actorf.toString()
            }
            setLog(TAG, "setEventModelDataAppLevel: eventModel?.actor:${eventModel?.actor}")

            if(playableContentModel?.data?.head?.headData?.misc?.pid!=null&&playableContentModel.data.head.headData.misc.pid.size>0){
//                eventModel?.album_ID=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pid)
                eventModel?.album_ID=playableContentModel?.data?.head?.headData?.misc?.pid.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.album_name=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.album_name=playableContentModel?.data?.head?.headData?.misc?.pName.toString()
            }


            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.originalAlbumName=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.originalAlbumName=playableContentModel?.data?.head?.headData?.misc?.pName.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.keywords!=null&&playableContentModel.data.head.headData.misc.keywords.size>0){
//                eventModel?.keywords=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.keywords)
                eventModel?.keywords=playableContentModel?.data?.head?.headData?.misc?.keywords.toString()
            }

            if(playableContentModel?.data?.head?.headData?.category!=null&&playableContentModel.data.head.headData.category.size>0){
//                eventModel?.category=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.category)
                eventModel?.category=playableContentModel?.data?.head?.headData?.category.toString()
            }
            eventModel?.rating=""+playableContentModel?.data?.head?.headData?.misc?.ratingCritic
            eventModel?.userRating=""+playableContentModel?.data?.head?.headData?.misc?.rating_user

            if(playableContentModel?.data?.head?.headData?.misc?.movierights!=null&&playableContentModel.data.head.headData.misc.movierights.size>0){
//                eventModel?.content_Pay_Type=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.movierights)
                eventModel?.content_Pay_Type=playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
            }

            eventModel?.release_Date=""+playableContentModel?.data?.head?.headData?.releasedate

            if(playableContentModel?.data?.head?.headData?.genre!=null&&playableContentModel.data.head.headData.genre.size>0){
//                eventModel?.genre=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.genre)
                eventModel?.genre=playableContentModel?.data?.head?.headData?.genre.toString()
            }

            if(playableContentModel?.data?.head?.headData?.subgenre_name!=null&&playableContentModel.data.head.headData.subgenre_name.size>0){
//                eventModel?.genre=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.subgenre_name)
                eventModel?.subGenre=playableContentModel?.data?.head?.headData?.subgenre_name.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating!=null&&playableContentModel.data.head.headData.misc.attributeCensorRating.size>0){
//                eventModel?.age_rating=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating)
                eventModel?.age_rating=playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.lyricistf!=null&&playableContentModel.data.head.headData.misc.lyricistf.size>0){
//                eventModel?.lyricist=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.lyricistf)
                eventModel?.lyricist=playableContentModel?.data?.head?.headData?.misc?.lyricistf.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.musicdirectorf!=null&&playableContentModel.data.head.headData.misc.musicdirectorf.size>0){
//                eventModel?.musicDirectorComposer=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.musicdirectorf)
                eventModel?.musicDirectorComposer=playableContentModel?.data?.head?.headData?.misc?.musicdirectorf.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.singerf!=null&&playableContentModel.data.head.headData.misc.singerf.size>0){
//                eventModel?.singer=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.singerf)
                eventModel?.singer=playableContentModel?.data?.head?.headData?.misc?.singerf.toString()
            }
            eventModel?.mood=""+playableContentModel?.data?.head?.headData?.misc?.mood
            if(playableContentModel?.data?.head?.headData?.misc?.tempo!=null&&playableContentModel.data.head.headData.misc.tempo.size>0){
//                eventModel?.tempo=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.tempo)
                eventModel?.tempo=playableContentModel?.data?.head?.headData?.misc?.tempo.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.lang!=null&&playableContentModel.data.head.headData.misc.lang.size>0){
//                eventModel?.language=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.lang)
                eventModel?.language=playableContentModel?.data?.head?.headData?.misc?.lang.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.sl!=null&&playableContentModel?.data?.head?.headData?.misc?.sl?.lyric!=null && !TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link!!)){
                val someFilepath = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
                val extension = someFilepath?.substring(someFilepath.lastIndexOf("."))
                eventModel?.lyrics_type=""+extension
            }
            eventModel?.label=""+playableContentModel?.data?.head?.headData?.misc?.vendor
            eventModel?.label_id=""+playableContentModel?.data?.head?.headData?.misc?.vendorid
            eventModel?.share=""+playableContentModel?.data?.head?.headData?.misc?.share
            eventModel?.favCount=""+playableContentModel?.data?.head?.headData?.misc?.f_fav_count
            eventModel?.f_fav_count=""+playableContentModel?.data?.head?.headData?.misc?.f_fav_count

            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.ptype=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.ptype=playableContentModel?.data?.head?.headData?.misc?.pName.toString()
            }
            //setLog("PlayableApiData", "pid-${playableContentModel?.data?.head?.headData?.misc?.pid}")
            if(playableContentModel?.data?.head?.headData?.misc?.pid!=null&&playableContentModel.data.head.headData.misc.pid.size>0){
//                eventModel?.pid=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pid)
                eventModel?.pid=playableContentModel?.data?.head?.headData?.misc?.pid.toString()
            }
            //setLog("PlayableApiData", "pName-${playableContentModel?.data?.head?.headData?.misc?.pName}")
            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.pName=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.pName=playableContentModel?.data?.head?.headData?.misc?.pName.toString()
                eventModel.podcast_album_name = playableContentModel?.data?.head?.headData?.misc?.pName.toString()
            }

            val userSubscriptionDetail= SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
            if(userSubscriptionDetail!=null){
                eventModel?.subscriptionStatus=""+userSubscriptionDetail?.data?.user?.userMembershipType
            }
            setLog(TAG, "setEventModelDataAppLevel contentType: "+contentType)

            if(contentType==4){
                val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
                setLog(TAG, "setEventModelDataAppLevel userSettingRespModel:${userSettingRespModel} playableContentModel:${playableContentModel?.data?.head?.headData?.title}")
                if(userSettingRespModel!=null){
                    if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                        eventModel?.audioQuality= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                        setLog(TAG, "setEventModelDataAppLevel userSettingRespModel: "+userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!)
                    }
                }
                if(TextUtils.isEmpty(eventModel?.audioQuality)){
                    eventModel?.audioQuality="Auto"
                }

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.AUDIO_QUALITY, ""+ eventModel.audioQuality)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            }else if(contentType==5){
                val userSettingVideoModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
                if(userSettingVideoModel!=null){
                    if(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                        eventModel?.videoQuality= userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                    }
                }

                if(TextUtils.isEmpty(eventModel?.videoQuality)){
                    eventModel?.audioQuality="Auto"
                }

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.VIDEO_DOWNLOAD_QUALITY, ""+ eventModel.videoQuality)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            }

            eventModel.urlKey=playableContentModel.data.head.headData.misc.urlKey

            HungamaMusicApp.getInstance().setEventData(eventModel.contentID,eventModel)

            setLog(TAG, "callOfflineSongEventAnalytics eventModel sourceName:${eventModel?.sourceName}  bucketName: ${eventModel?.bucketName} downloadQueue.audioQuality:${eventModel.audioQuality}")
        }


    }

    public fun setDownloadEventModelDataAppLevel(playableContentModel: DownloadableContentModel, contentType: Int) {

        var eventModel: EventModel?=HungamaMusicApp?.getInstance()?.getEventData(playableContentModel.data.head.headData.id)

        if(eventModel==null ||TextUtils.isEmpty(eventModel?.contentID)){
            eventModel= EventModel()
        }

        if(TextUtils.isEmpty(eventModel?.contentID)){
            eventModel?.contentID=playableContentModel.data.head.headData.id
        }

        if(TextUtils.isEmpty(eventModel?.songName)){
            eventModel?.songName=""+playableContentModel?.data?.head?.headData?.title
        }

        if(playableContentModel?.data?.head?.headData?.misc?.actorf!=null&&playableContentModel.data.head.headData.misc.actorf.size>0){
//            eventModel?.actor=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.actorf)
            eventModel?.actor=playableContentModel.data.head.headData.misc.actorf.toString()
        }
        setLog(TAG, "setEventModelDataAppLevel: eventModel?.actor:${eventModel?.actor}")

        if(playableContentModel?.data?.head?.headData?.misc?.pid!=null&&playableContentModel.data.head.headData.misc.pid.size>0){
//            eventModel?.album_ID=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pid)
            eventModel?.album_ID=playableContentModel.data.head.headData.misc.pid.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//            eventModel?.album_name=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
            eventModel?.album_name=playableContentModel.data.head.headData.misc.pName.toString()
        }


        if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//            eventModel?.originalAlbumName=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
            eventModel?.originalAlbumName=playableContentModel.data.head.headData.misc.pName.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.keywords!=null&&playableContentModel.data.head.headData.misc.keywords.size>0){
//            eventModel?.keywords=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.keywords)
            eventModel?.keywords=playableContentModel.data.head.headData.misc.keywords.toString()
        }

        if(playableContentModel?.data?.head?.headData?.category!=null&&playableContentModel.data.head.headData.category.size>0){
//            eventModel?.category=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.category)
            eventModel?.category=playableContentModel.data.head.headData.category.toString()
        }
        eventModel?.rating=""+playableContentModel?.data?.head?.headData?.misc?.ratingCritic
        eventModel?.userRating=""+playableContentModel?.data?.head?.headData?.misc?.rating_user

        if(playableContentModel?.data?.head?.headData?.misc?.movierights!=null&&playableContentModel.data.head.headData.misc.movierights.size>0){
//            eventModel?.content_Pay_Type=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.movierights)
            eventModel?.content_Pay_Type=playableContentModel.data.head.headData.misc.movierights.toString()
        }

        eventModel?.release_Date=""+playableContentModel?.data?.head?.headData?.releasedate

        if(playableContentModel?.data?.head?.headData?.genre!=null&&playableContentModel.data.head.headData.genre.size>0){
//            eventModel?.genre=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.genre)
            eventModel?.genre=playableContentModel?.data?.head?.headData?.genre.toString()
        }

        if(playableContentModel?.data?.head?.headData?.subgenre_name!=null&&playableContentModel.data.head.headData.subgenre_name.size>0){
//            eventModel?.genre=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.subgenre_name)
            eventModel?.subGenre=playableContentModel?.data?.head?.headData?.subgenre_name.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating!=null&&playableContentModel.data.head.headData.misc.attributeCensorRating.size>0){
//            eventModel?.age_rating=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating)
            eventModel?.age_rating=playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.lyricistf!=null&&playableContentModel.data.head.headData.misc.lyricistf.size>0){
//            eventModel?.lyricist=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.lyricistf)
            eventModel?.lyricist=playableContentModel?.data?.head?.headData?.misc?.lyricistf.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.musicdirectorf!=null&&playableContentModel.data.head.headData.misc.musicdirectorf.size>0){
//            eventModel?.musicDirectorComposer=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.musicdirectorf)
            eventModel?.musicDirectorComposer=playableContentModel?.data?.head?.headData?.misc?.musicdirectorf.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.singerf!=null&&playableContentModel.data.head.headData.misc.singerf.size>0){
            eventModel?.singer=playableContentModel.data.head.headData.misc.singerf.toString()
        }
        eventModel?.mood=""+playableContentModel?.data?.head?.headData?.misc?.mood
        if(playableContentModel?.data?.head?.headData?.misc?.tempo!=null&&playableContentModel.data.head.headData.misc.tempo.size>0){
            eventModel?.tempo=playableContentModel.data.head.headData.misc.tempo.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.lang!=null&&playableContentModel.data.head.headData.misc.lang.size>0){
//            eventModel?.language=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.lang)
            eventModel?.language=playableContentModel.data.head.headData.misc.lang.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.sl!=null&&playableContentModel?.data?.head?.headData?.misc?.sl?.lyric!=null && !TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link!!)){
            val someFilepath = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
            val extension = someFilepath?.substring(someFilepath.lastIndexOf("."))
            eventModel?.lyrics_type=""+extension
        }
        eventModel?.label=""+playableContentModel?.data?.head?.headData?.misc?.vendor
        eventModel?.label_id=""+playableContentModel?.data?.head?.headData?.misc?.vendorid
        eventModel?.share=""+playableContentModel?.data?.head?.headData?.misc?.share

        if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//            eventModel?.ptype=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
            eventModel?.ptype=playableContentModel?.data?.head?.headData?.misc?.pName.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.pid!=null&&playableContentModel.data.head.headData.misc.pid.size>0){
//            eventModel?.pid=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pid)
            eventModel?.pid=playableContentModel?.data?.head?.headData?.misc?.pid.toString()
        }

        if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//            eventModel?.pName=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
            eventModel?.pName=playableContentModel?.data?.head?.headData?.misc?.pName.toString()
            eventModel.podcast_album_name = playableContentModel?.data?.head?.headData?.misc?.pName.toString()
        }

        val userSubscriptionDetail= SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        if(userSubscriptionDetail!=null){
            eventModel?.subscriptionStatus=""+userSubscriptionDetail?.data?.user?.userMembershipType
        }


        if(contentType==4){
            val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
            if(userSettingRespModel!=null){
                if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                    eventModel?.audioQuality= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                }
            }

            if(TextUtils.isEmpty(eventModel?.audioQuality)){
                eventModel?.audioQuality="Auto"
            }

            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.AUDIO_QUALITY, ""+ eventModel.audioQuality)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }else if(contentType==5){
            val userSettingVideoModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
            if(userSettingVideoModel!=null){
                if(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                    eventModel?.videoQuality= userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                }
            }

            if(TextUtils.isEmpty(eventModel?.videoQuality)){
                eventModel?.videoQuality="Auto"
            }

            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.VIDEO_DOWNLOAD_QUALITY, ""+ eventModel.videoQuality)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }




        HungamaMusicApp.getInstance().setEventData(eventModel?.contentID!!,eventModel)
    }

    fun getPlanInfo(context:Context,id:String): MutableLiveData<Resource<PlanInfoContentModel>> {
        CoroutineScope(Dispatchers.IO).launch{

            val url=WSConstants.METHOD_PLAN_INFO

            val jsonObject = JSONObject()
            val locale = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            jsonObject.put("country", if (TextUtils.isEmpty(locale.networkCountryIso)) locale.networkCountryIso else Constant.DEFAULT_COUNTRY_CODE)
            setLog("CCode", if (TextUtils.isEmpty(locale.networkCountryIso)) locale.networkCountryIso else Constant.DEFAULT_COUNTRY_CODE)
            jsonObject.put("product_id", Constant.PRODUCT_ID)
            jsonObject.put("platform_id", Constant.PLATFORM_ID)
            jsonObject.put("plan_id", id)
            jsonObject.put("plan_type","subscription")

            downloadResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    try {
                        val dataModel = Gson().fromJson<PlanInfoContentModel>(
                            response.toString(),
                            PlanInfoContentModel::class.java
                        ) as PlanInfoContentModel

                        planInfoData.postValue(Resource.success(dataModel))


                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        planInfoData.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    planInfoData.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }
        return planInfoData
    }

}