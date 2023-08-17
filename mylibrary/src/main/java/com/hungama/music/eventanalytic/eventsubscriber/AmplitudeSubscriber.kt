package com.hungama.music.eventanalytic.eventsubscriber

import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.hungama.music.HungamaMusicApp
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.*
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/23/2021.
 * Purpose:
 */
class AmplitudeSubscriber : ISubscriber {
    var TAG = "AmplitudeLog"

    var iGnoreUserProperty = ArrayList<String>()

    var lastDownloadEventId = ""
    var lastStreamEventSongId = ""
    var lastStreamStartEventSongId = ""
    var lastStreamTriggerEventSongId = ""
    private var mp: MixpanelAPI? = null
    var PROJECT_TOKEN = "de8f8410bc7c8326fe280906655bae0e"


    init {
        amplitudeSetUp()
        gamificationSetUp()
        iGnoreUserProperty.add(EventConstant.NAME)
        iGnoreUserProperty.add(EventConstant.FIRST_NAME)
        iGnoreUserProperty.add(EventConstant.LAST_NAME)
        iGnoreUserProperty.add(EventConstant.EMAIL)
        iGnoreUserProperty.add(EventConstant.PHONE)
        iGnoreUserProperty.add(EventConstant.IMEI)
    }

    companion object {
        private var instance: EventManager? = null
        fun getInstance(): EventManager {
            if (instance == null) {
                instance = EventManager()
            }
            return instance as EventManager
        }
    }

    fun amplitudeSetUp() {

        setLog(TAG, "amplitudeSetUp")
        mp = MixpanelAPI.getInstance(HungamaMusicApp.getInstance().applicationContext!!, PROJECT_TOKEN,true)
    }

    override fun sendEvent(iEvent: IEvent) {
        CoroutineScope(Dispatchers.IO).launch{
            sendAmplitudeLogEvent(iEvent.getName(),iEvent.getProperty())
        }
    }

    private suspend fun sendAmplitudeLogEvent(eventName: String, proprtyMap: java.util.HashMap<String, String>) {
        val eventProperties = JSONObject()

        proprtyMap?.keys?.forEach {
            if (proprtyMap?.get(it) != null && !TextUtils.isEmpty(proprtyMap?.get(it)) && !proprtyMap?.get(it)?.equals("[]")!!) {
                eventProperties.put(it, proprtyMap.get(it))
            }
        }



        setLog(TAG, "eventName:${eventName} " +
                "\nContent_Type: ${proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)} " +
                "\nlastStreamEventSongId: ${lastStreamEventSongId} " +
                "\nCONTENTID_EPROPERTY: ${proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)} " +
                "\nDuration: ${proprtyMap?.get(EventConstant.DURATION_FG_EPROPERTY)})")



        if (eventName?.equals(EventConstant.LOGINSUCCESS_ENAME, true) == true || eventName?.equals(EventConstant.REGISTRATION_SUCCESS_ENAME, true) == true) {
            setLog(TAG, "GM-SDK-APP initialize eventName:$eventName isUserLogdIn:${SharedPrefHelper.getInstance().isUserLoggedIn()} getUserId:${SharedPrefHelper?.getInstance()?.getUserId()}")
            gamificationSetUp()

            val loginMethod= proprtyMap[EventConstant.METHOD_EPROPERTY]
            val userDataMap = java.util.HashMap<String, String>()
            if (loginMethod.equals("HE")){
                userDataMap.put(EventConstant.HE_LOGIN, ""+true)
            }else{
                userDataMap.put(EventConstant.HE_LOGIN, ""+false)
            }
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))


        }else if(eventName?.equals(EventConstant.OFFLINESONG_ENAME,true) == true){
            var contentType=proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)
            var contentID=proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)
            if (contentType != null && contentID != null && !TextUtils.isEmpty(contentType) && !TextUtils.isEmpty(
                    contentID
                ) && !lastDownloadEventId?.contains(contentID!!, true)!!
            ) {
                val eventValue: HashMap<String, Any> = HashMap()
                eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, contentType)
                eventValue.put(AFInAppEventParameterName.CONTENT_ID, contentID)
                if (contentType?.contains("Audio", true) == true || contentType?.contains(
                        "Podcast",
                        true
                    ) == true || contentType?.contains(
                        "Radio",
                        true
                    ) == true || contentType?.contains(
                        "Audio Album",
                        true
                    ) == true || contentType?.contains(
                        "Chart",
                        true
                    ) == true || contentType?.contains(
                        "Audio Track",
                        true
                    ) == true || contentType?.contains("Playlist", true) == true
                ) {
                    AppsFlyerLib.getInstance().logEvent(
                        HungamaMusicApp.getInstance(),
                        EventConstant.AF_MEDIA_DOWNLOADED,
                        eventValue
                    )
                } else {
                    AppsFlyerLib.getInstance().logEvent(
                        HungamaMusicApp.getInstance(),
                        EventConstant.AF_MEDIA_DOWNLOADED_VIDEO,
                        eventValue
                    )
                }
                setLog(TAG, "AppsFlyerLib eventName:$eventName eventProperties:$eventValue")

                lastDownloadEventId = contentID
            }

        }else if(eventName?.equals(EventConstant.SUBSCRIPTION_PLAN_PAGE_OPEN,true) == true){
            /* Track Events in real time */
            val eventValue: HashMap<String, Any> = HashMap()
            AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_OPENED_PRO_PAGE, eventValue)
        }else if(eventName?.equals(EventConstant.FAVOURITED_ENAME,true) == true){
            var contentType=proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)
            var contentID=proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)
            setLog(TAG, "AppsFlyerLib contentType:$contentType contentID:$contentID")

            if(contentType!=null&&contentID!=null&&!TextUtils.isEmpty(contentType)&&!TextUtils.isEmpty(contentID)){
                /* Track Events in real time */
                val eventValue: HashMap<String, Any> = HashMap()
                eventValue.put(EventConstant.AF_TYPE, contentType)
                eventValue.put(EventConstant.AF_ID,contentID)

                AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,
                    EventConstant.AF_FAVOURITE,
                    eventValue,
                    object : AppsFlyerRequestListener {
                        override fun onSuccess() {
                            setLog("AppsFlyerLib", "Event sent successfully")
                        }

                        override fun onError(i: Int, @NonNull s: String) {
                            setLog(
                                "AppsFlyerLib", """Event failed to be sent:Error code: $i Error description: $s""".trimIndent()
                            )
                        }
                    })

                setLog(TAG, "AppsFlyerLib eventName:$eventName eventProperties:$eventValue")
            }
        }else if (eventName?.equals(EventConstant.CREATEDPLAYLIST_ENAME, true) == true) {
            /* Track Events in real time */
            val eventValue: MutableMap<String, Any> = HashMap()
            AppsFlyerLib.getInstance().logEvent(
                HungamaMusicApp.getInstance(),
                EventConstant.AF_CREATE_PLAYLIST,
                eventValue
            )
        }

        if(EventConstant.gamiFicationActions?.contains(eventName) == true && SharedPrefHelper.getInstance().isUserLoggedIn()){
            setLog(TAG, "GM-SDK-APP eventName:$eventName isUserLogdIn:${SharedPrefHelper.getInstance().isUserLoggedIn()} getUserId:${SharedPrefHelper?.getInstance()?.getUserId()}")
            setLog("GamificationActionAmp"," " +
                    SharedPrefHelper.getInstance().isUserLoggedIn().toString() + " "+ eventName)
            try {
                if (eventName?.contains(EventConstant.STREAM_ENAME, true) == true && !lastStreamEventSongId?.contains(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!, true)!!)
                {
                    setLog(
                        TAG, "GamificationSDK eventName:${eventName} " +
                                "\nContent_Type: ${proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)} " +
                                "\nlastStreamEventSongId: ${lastStreamEventSongId} " +
                                "\nCONTENTID_EPROPERTY: ${proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)} " +
                                "\nDuration: ${proprtyMap?.get(EventConstant.DURATION_FG_EPROPERTY)})"
                    )



                    var mEventName = ""
                    var duration = 0
                    val streamVideoMap = SharedPrefHelper.getInstance()
                        .loadMap(PrefConstant.STREAM_10_VIDEO_MAP) as HashMap
                    val streamAudioMap = SharedPrefHelper.getInstance()
                        .loadMap(PrefConstant.STREAM_20_MAP) as HashMap


                    if (proprtyMap?.containsKey(EventConstant.CONTENTTYPE_EPROPERTY) == true && proprtyMap?.containsKey(EventConstant.DURATION_EPROPERTY)!!
                    ) {
                        if (!TextUtils.isEmpty(proprtyMap?.get(EventConstant.DURATION_EPROPERTY))){
                            duration = proprtyMap?.get(EventConstant.DURATION_EPROPERTY)?.toInt()!!
                        }

                        if (proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)
                                ?.contains("Live Concert", true)!! && duration >= 600
                        ) {
                            mEventName = EventConstant.STREAM_LIVESHOW_ENAME
                        } else if ((proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)
                                ?.contains("Movie Videos", true)!!) && duration >= 300
                        ) {
                            mEventName = EventConstant.STREAM_SHORTMOVIE_ENAME
                            streamVideoMap.put(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)
                        }else if ((proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("TV Show",true)!!
                                    || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("tv_show_episode", true)!!) && duration >= 600) {
                            mEventName = EventConstant.STREAM_TVSHOW_ENAME
                            streamVideoMap.put(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)
                        }else if (proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Short Films",true)!!
                            || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("short_film", true)!! && duration >= 600) {
                            mEventName = EventConstant.STREAM_SHORTVIDEO_ENAME
//                            mEventName = EventConstant.STREAM_SHORTFILM_ENAME
                            streamVideoMap.put(
                                proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,
                                proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!
                            )
                        }else if (proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Movie",true)!! && duration >= 600) {
                            mEventName = EventConstant.STREAM_MOVIE_ENAME
                            streamVideoMap.put(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)
                        }else if ((proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Music Video",true)!!
                                    || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Video",true)!!
                                    || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("short_video",true)!!
                                    || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("music_video", true)!!) && duration >=60) {
                            mEventName = EventConstant.STREAM_MUSICVIDEO_ENAME
                            streamVideoMap.put(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)
                        }else if ((proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Podcast",true)!!)&& duration >= 30) {
                            mEventName = EventConstant.STREAM_PODCAST_ENAME
                            streamAudioMap.put(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)
                        }else if ((proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Audio",true)!! || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Playlist",true)!! || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Album",true)!! || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("Radio",true)!! || proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)?.contains("song",true)!!) && duration >= 30) {
                            mEventName = EventConstant.STREAM_SONG_ENAME
                            streamAudioMap.put(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!,proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)
                        }
                    }

                    setLog("isTriggerNewUser", "ContentType s" + proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY) + " " +mEventName + "\n")

                    if(!TextUtils.isEmpty(mEventName)){
                        CoroutineScope(Dispatchers.IO).launch {
//                            delay(5000)
                            if (CommonUtils.isUserHasGoldSubscription()){
                                callTriggerPointsEvent(mEventName)
                            }else if(eventName?.equals(EventConstant.LOGINSUCCESS_ENAME, true)!! || mEventName.equals(EventConstant.REGISTRATION_SUCCESS_ENAME, true)){
                                callTriggerPointsEvent(mEventName)
                            }
                            else {
                                if (proprtyMap.get(EventConstant.CONTENTTYPE_EPROPERTY) != null) {
                                    if (CommonUtils.getSongDurationConfig().enable_minutes_quota) {
                                        if ((proprtyMap.get(EventConstant.CONTENTTYPE_EPROPERTY)
                                                ?.contains("Audio", true) == true || proprtyMap.get(
                                                EventConstant.CONTENTTYPE_EPROPERTY
                                            )?.contains("Playlist", true) == true || proprtyMap.get(
                                                EventConstant.CONTENTTYPE_EPROPERTY
                                            )?.contains("Album", true) == true || proprtyMap.get(
                                                EventConstant.CONTENTTYPE_EPROPERTY
                                            )?.contains("song", true) == true)
                                        ) {
                                            if (BaseActivity.localDuration > 0) {
                                                callTriggerPointsEvent(mEventName)
                                            }
                                        } else {
                                            callTriggerPointsEvent(mEventName)
                                        }
                                    }
                                    else{
                                        callTriggerPointsEvent(mEventName)
                                    }
                                }
                            }
                        }

                    }

                    if (streamVideoMap.size >= 10) {
                        if (SharedPrefHelper.getInstance()
                                .get(PrefConstant.STREAM_10_VIDEO, true)
                        ) {
                            val eventValue: HashMap<String, Any> = HashMap()
                            AppsFlyerLib.getInstance().logEvent(
                                HungamaMusicApp.getInstance(),
                                EventConstant.AF_STREAM_10_VIDEO_ENAME,
                                eventValue
                            )
                            SharedPrefHelper.getInstance().save(PrefConstant.STREAM_10_VIDEO, false)
                            SharedPrefHelper.getInstance()
                                .saveMap(PrefConstant.STREAM_10_VIDEO_MAP, streamVideoMap)
                            setLog(TAG, "sendAmplitudeLogEvent: STREAM_10_VIDEO called")
                        }
                    } else {
                        SharedPrefHelper.getInstance()
                            .saveMap(PrefConstant.STREAM_10_VIDEO_MAP, streamVideoMap)
                    }

                    if (streamAudioMap.size >= 20) {
                        if (SharedPrefHelper.getInstance().get(PrefConstant.STREAM_20, true)) {
                            val eventValue: HashMap<String, Any> = HashMap()
                            AppsFlyerLib.getInstance().logEvent(
                                HungamaMusicApp.getInstance(),
                                EventConstant.AF_STREAM_20_ENAME,
                                eventValue
                            )
                            SharedPrefHelper.getInstance().save(PrefConstant.STREAM_20, false)
                            setLog(TAG, "sendAmplitudeLogEvent: STREAM_20 called")
                            SharedPrefHelper.getInstance().saveMap(PrefConstant.STREAM_20_MAP, streamAudioMap!!)
                        }
                    } else {
                        SharedPrefHelper.getInstance().saveMap(PrefConstant.STREAM_20_MAP, streamAudioMap!!)
                    }

                    setLog(TAG, "sendAmplitudeLogEvent: STREAM_20 size:${streamAudioMap?.size} hMap:${streamAudioMap}")
                    setLog(
                        TAG,
                        "sendAmplitudeLogEvent: STREAM_10_VIDEO size:${streamVideoMap?.size} hMap:${streamVideoMap}"
                    )

                }
                else {
                    setLog("isTriggerNewUser", " 356 " + proprtyMap.toString()+ " "+ eventName.toString())

                    CoroutineScope(Dispatchers.IO).launch {
                        delay(5000)
                        if (CommonUtils.isUserHasGoldSubscription()){
                            callTriggerPointsEvent(eventName)
                        }else if(eventName?.equals(EventConstant.LOGINSUCCESS_ENAME, true) == true || eventName.equals(EventConstant.REGISTRATION_SUCCESS_ENAME, true)){
                            callTriggerPointsEvent(eventName)
                        }
                        else{
                            if (proprtyMap?.get(EventConstant.CONTENTTYPE_EPROPERTY) != null){
                                if (CommonUtils.getSongDurationConfig().enable_minutes_quota) {

                                    if ((proprtyMap.get(EventConstant.CONTENTTYPE_EPROPERTY)
                                            ?.contains("Audio", true) == true || proprtyMap.get(
                                            EventConstant.CONTENTTYPE_EPROPERTY
                                        )?.contains("Playlist", true) == true || proprtyMap.get(
                                            EventConstant.CONTENTTYPE_EPROPERTY
                                        )?.contains("Album", true) == true || proprtyMap.get(
                                            EventConstant.CONTENTTYPE_EPROPERTY
                                        )?.contains("song", true) == true)
                                    ) {
                                        if (BaseActivity.localDuration > 0) {
                                            callTriggerPointsEvent(eventName)
                                        }
                                    } else {
                                        callTriggerPointsEvent(eventName)
                                    }
                                }
                                else{
                                    callTriggerPointsEvent(eventName)
                                }
                            }
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

        try {
            if(proprtyMap.containsKey(EventConstant.BUCKETNAME_EPROPERTY) || proprtyMap.containsKey(EventConstant.BUCKETTYPE_EPROPERTY)){
                setLog(TAG, "AmplitudeManger BUCKETNAME_EPROPERTY:${proprtyMap.get(EventConstant.BUCKETNAME_EPROPERTY)} BUCKETTYPE_EPROPERTY:${proprtyMap.get(EventConstant.BUCKETTYPE_EPROPERTY)}")
                if(proprtyMap.get(EventConstant.BUCKETNAME_EPROPERTY)!=null&&proprtyMap.get(EventConstant.BUCKETNAME_EPROPERTY)?.contains("Good",true)!!){
                    proprtyMap.put(EventConstant.BUCKETNAME_EPROPERTY,EventConstant.CONTINUE_WATCHING_NAME)
                }else if(proprtyMap.get(EventConstant.BUCKETTYPE_EPROPERTY)!=null&&proprtyMap.get(EventConstant.BUCKETTYPE_EPROPERTY)?.contains("Good",true)!!){
                    proprtyMap.put(EventConstant.BUCKETTYPE_EPROPERTY,EventConstant.CONTINUE_WATCHING_NAME)
                }
            }

            setLog(
                TAG,
                "AmplitudeManger eventName:$eventName  duration BG:${proprtyMap?.get(EventConstant.DURATION_BG_EPROPERTY)}"
            )

            var durationFG=0
            var durationBG=0
            if(proprtyMap?.containsKey(EventConstant.DURATION_FG_EPROPERTY) == true && !proprtyMap?.get(EventConstant.DURATION_FG_EPROPERTY)?.isNullOrEmpty()!!){
                durationFG=proprtyMap?.get(EventConstant.DURATION_FG_EPROPERTY)?.toInt()!!
            }

            if(proprtyMap?.containsKey(EventConstant.DURATION_BG_EPROPERTY) == true && !proprtyMap?.get(EventConstant.DURATION_BG_EPROPERTY)?.isNullOrEmpty()!!){
                durationBG=proprtyMap?.get(EventConstant.DURATION_BG_EPROPERTY)?.toInt()!!
            }


            setLog(
                TAG,
                "AmplitudeManger-1 eventName:$eventName durationFG:${durationFG} durationBG:${durationBG} eventProperties:$eventProperties"
            )
            if(eventName?.equals(EventConstant.STREAM_ENAME, true) == true || eventName?.equals(EventConstant.PREVIEW_STREAM_ENAME, true) == true){

                setLog(
                    TAG,
                    "AmplitudeManger-2 called lastStreamEventSongId:$lastStreamEventSongId CONTENTID_EPROPERTY:${proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)}"
                )
//                if(!lastStreamEventSongId?.equals(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)&& (durationFG>0 || durationBG>0)){
                //   Amplitude.getInstance().logEvent(eventName, eventProperties)
                mp?.track(eventName, eventProperties)


                lastStreamEventSongId = proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!
                setLog(
                    TAG,
                    "AmplitudeManger-3 called eventName:$eventName eventProperties:$eventProperties"
                )
                setLog(
                    TAG,
                    "AmplitudeManger called eventName:$eventName eventProperties:$eventProperties"
                )

//                }
            }else if(eventName?.equals(EventConstant.STREAM_TRIGGER_ENAME, true) == true || eventName?.equals(EventConstant.PREVIEW_STREAM_TRIGGER_ENAME, true) == true){
                if(!lastStreamTriggerEventSongId?.equals(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)!!){
                    //   Amplitude.getInstance().logEvent(eventName, eventProperties)
                    mp?.track(eventName, eventProperties)
                    lastStreamTriggerEventSongId = proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!

                    setLog(
                        TAG,
                        "AmplitudeManger called eventName:$eventName eventProperties:$eventProperties"
                    )
                }
            }else if(eventName?.equals(EventConstant.STREAM_START_ENAME, true) == true || eventName?.equals(EventConstant.PREVIEW_STREAM_START_ENAME, true) == true){
                if(!lastStreamStartEventSongId?.equals(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)!!){
                    //  Amplitude.getInstance().logEvent(eventName, eventProperties)
                    mp?.track(eventName, eventProperties)
                    lastStreamStartEventSongId = proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!


                    setLog(
                        TAG,
                        "AmplitudeManger called eventName:$eventName eventProperties:$eventProperties"
                    )
                }
            } else {
                //  Amplitude.getInstance().logEvent(eventName, eventProperties)
                mp?.track(eventName, eventProperties)
                setLog(TAG, "AmplitudeManger eventName:$eventName eventProperties:$eventProperties")
            }
        }catch (e:Exception){
            setLog(TAG, "AmplitudeManger error:${e.printStackTrace()}")

        }
    }
    fun callTriggerPointsEvent(eventName: String){

    }

    override fun sendUserAttribute(iEvent: IEvent) {
        CoroutineScope(Dispatchers.IO).launch{
            if(SharedPrefHelper.getInstance().isUserLoggedIn()){
                // Initialize
                //  Amplitude.getInstance().userId = SharedPrefHelper.getInstance().getUserId()
                mp?.identify(SharedPrefHelper.getInstance().getUserId())
                val jsonUserProperties = JSONObject()
                jsonUserProperties.put(EventConstant.HUNGAMA_ID_LOGIN,SharedPrefHelper.getInstance().getUserId())
                mp?.people?.set(jsonUserProperties)

                setLog(TAG, "AmplitudeManger userAttribute: ${jsonUserProperties}")

                val jsonObject2 = JSONObject()
                jsonObject2.put(EventConstant.HUNGAMA_UN,true)
                mp?.people?.set(jsonObject2)
                setLog(TAG, "userAttribute: ${jsonObject2}")

//                As discussed with Gaurav kumar on 15-07-2021 to replace device id with hungama id.
//                Amplitude.getInstance().setDeviceId(SharedPrefHelper.getInstance().getUserId())
            }else{
                mp?.identify(SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID, ""))
            }


            val jsonObject1 = JSONObject()
            if (!CommonUtils.isUserHasGoldSubscription())
                jsonObject1.put("balance_minutes", (BaseActivity.totalGetted - (BaseActivity.totalPlayedSongDuration /1000/60)))
            else
                jsonObject1.put("balance_minutes", "NA")

            iEvent.getProperty().keys.forEach {
                if(!iGnoreUserProperty.contains(it)){
                    jsonObject1.put(it, iEvent.getProperty()[it])
                }
            }
            mp?.people?.set(jsonObject1)

            setLog(TAG, "AmplitudeManger userAttribute: $jsonObject1")

//            Sentry.configureScope { scope: Scope ->
//                scope.setTag("my-tag", "my value")
//                val user = User()
//                user.setId(SharedPrefHelper.getInstance().getUserId())
//                user.setEmail(SharedPrefHelper.getInstance().getUserEmail())
//                scope.user = user
//            }
        }

    }

    fun gamificationSetUp() {

        setLog("initializedGamification" ,
            " " + "Yes" + " "+
                    SharedPrefHelper?.getInstance()?.getUserId() + " " +
                    Utils.getDeviceId(HungamaMusicApp.getInstance().applicationContext).toString())
        setLog("GM_SDK_APPGamification","GM-SDK-APP before${SharedPrefHelper.getInstance().isUserLoggedIn()} UserId:${SharedPrefHelper?.getInstance()?.getUserId()}" + " "+ CommonUtils.isUserHasGoldSubscription() + " " + BaseActivity.getIsGoldUser())
        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
            CoroutineScope(Dispatchers.IO).launch {
                setLog("GM-SDK-APP","GM-SDK-APP after gamificationSetUp called isUserLogdIn${SharedPrefHelper.getInstance().isUserLoggedIn()} UserId:${SharedPrefHelper?.getInstance()?.getUserId()} DeviceId:${Utils.getDeviceId(HungamaMusicApp.getInstance().applicationContext)}")
                setLog("initializedGamification"," GamificaitonPoints initialized")

                delay(2000)
            }

        }
    }



    var gamificationSDKUpdateListener: GamificationUpdateListener?=null
    fun registerGamificationListener(listener: GamificationUpdateListener) {
        setLog("GM-SDK-APP","registerGamificationListener called")
        gamificationSDKUpdateListener=listener
    }

    interface GamificationUpdateListener{
        fun gamificationOnPointsAdded(eventName: String?, added: Int, total: Int)
        fun gamificationOnPointsUpdated(points: Int)
    }
}