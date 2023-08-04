package com.hungama.music.home.eventsubscriber

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.core.content.pm.PackageInfoCompat
import com.hungama.music.HungamaMusicApp
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent
import com.hungama.music.eventanalytic.event.ISubscriber
import com.hungama.music.eventanalytic.util.CustomPushMessageListener
import com.hungama.music.eventanalytic.util.FcmEventListener
import com.hungama.music.eventanalytic.util.callbacks.inapp.ApplicationBackgroundListener
import com.hungama.music.eventanalytic.util.callbacks.inapp.InAppCallback
import com.hungama.music.ui.main.view.activity.PaymentWebViewActivity
import com.hungama.music.ui.main.view.activity.SplashActivity
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.*
import com.moengage.core.Properties
import com.moengage.core.config.*
import com.moengage.core.model.AppStatus
import com.moengage.core.model.UserGender
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.inapp.MoEInAppHelper
import com.moengage.pushbase.MoEPushHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/23/2021.
 * Purpose:
 */
class MoengageSubscriber(context: Context) : ISubscriber {
    var TAG = "MoengageLog"
    val mContext = context
    var MOENGAGE_APP_ID = ""
    var MOENGAGE_APP_KEY = ""
    var SENDERID = "431177551402"
    var iGnore = ArrayList<EventType>()
    var iGnoreProperty = ArrayList<String>()
    var lastStreamEventSongId = ""
    var lastStreamStartEventSongId = ""
    companion object{
        var notificationTone=""
    }

    init {
        moengageSetUp()

        iGnoreProperty.add(EventConstant.THREEDOTSCLICKED_ENAME)
        iGnoreProperty.add(EventConstant.APIPERFORMANCE_ENAME)
        /*As per jira - https://hungama.atlassian.net/browse/HU-5744
        iGnoreProperty.add(EventConstant.LANGUAGECHANGE_ENAME)*/
    //    iGnoreProperty.add(EventConstant.LOGINOTPPAGELOADSUCCESS_ENAME)
        /*As per jira - https://hungama.atlassian.net/browse/HU-5747
        iGnoreProperty.add(EventConstant.LOGINERROR_ENAME)*/
        /*As per jira - https://hungama.atlassian.net/browse/HU-5763
        iGnoreProperty.add(EventConstant.MORECLICKED_ENAME)*/
        iGnoreProperty.add(EventConstant.VIDEOPLAYERSUBTITLESELECTED_ENAME)
        iGnoreProperty.add(EventConstant.VIDEOPLAYERVIDEOQUALITYSELECTED_ENAME)
        iGnoreProperty.add(EventConstant.VIDEOPLAYERLOCKTAPPED_ENAME)
        iGnoreProperty.add(EventConstant.AUDIOPLAYERPAUSE_ENAME)
        iGnoreProperty.add(EventConstant.AUDIOPLAYERPLAY_ENAME)
        iGnoreProperty.add(EventConstant.HEARTBEAT_ENAME)

        notificationTone = ""+ ContentResolver.SCHEME_ANDROID_RESOURCE.toString() + "://" + HungamaMusicApp.getInstance().getPackageName() + "/" + R.raw.notify_
//        notificationTone= "android.resource://" + HungamaMusicApp.getInstance()?.getPackageName() + "/" + R.raw.t1
        setLog(TAG, "notificationTone:"+ HungamaMusicApp.getInstance().getPackageName())

    }


    /**
     * amplitude SetUp
     */
    fun moengageSetUp() {
        setLog(TAG, "moengageSetUp")

        if(BuildConfig.DEBUG){
            MOENGAGE_APP_ID = "QCQSCVFUMHFCN7HF0CGSACD0"
//            MOENGAGE_APP_KEY = "PXGA4LZ3VNHO"
            SENDERID = "431177551402"
        }else{
            MOENGAGE_APP_ID = "QCQSCVFUMHFCN7HF0CGSACD0"
//            MOENGAGE_APP_KEY = "e1mrWX5J3MFYbrBOVVG+FALL"
            SENDERID = "431177551402"
        }
        // Set of activity classes on which in-app should not be shown
        val inAppOptOut = mutableSetOf<Class<*>>()
        inAppOptOut.add(SplashActivity::class.java)
        inAppOptOut.add(PaymentWebViewActivity::class.java)

        // configure MoEngage initializer
        val moEngage = MoEngage.Builder(HungamaMusicApp.getInstance(), MOENGAGE_APP_ID)//enter your own app id
                .configureLogs(LogConfig(LogLevel.VERBOSE, true))
                .configureNotificationMetaData(NotificationConfig(R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.color.colorPrimary, notificationTone, true, isBuildingBackStackEnabled = true, isLargeIconDisplayEnabled = true))
                .configurePushKit(PushKitConfig(true))
                .configureFcm(FcmConfig(true,SENDERID))
                .configureInApps(InAppConfig(true, inAppOptOut))
                .configureGeofence(GeofenceConfig (
                    isGeofenceEnabled = true,
                    isBackgroundSyncEnabled = true))
//                .configureMiPush(MiPushConfig(
//                    MOENGAGE_APP_ID,
//                    MOENGAGE_APP_KEY,
//                    true))
                .build()


        // initialize MoEngage SDK

        MoEngage.initialise(moEngage)
        MoEngage.enableSdk(HungamaMusicApp.getInstance())

        trackInstallOrUpdate()
        // Setting CustomPushMessageListener for notification customisation
        MoEPushHelper.getInstance().messageListener = CustomPushMessageListener()

        // PushKit Event listener
//        MoEPushKitHelper.getInstance().addEventListener(PushKitListener())

        //FCM Event Listener.
        MoEFireBaseHelper.getInstance().addEventListener(FcmEventListener())

        //register for app background listener
        MoECallbacks.getInstance().addAppBackgroundListener(ApplicationBackgroundListener())

        // register in-app listener
        MoEInAppHelper.getInstance().registerListener(InAppCallback())



    }

    /**
     * Tell MoEngage SDK whether the user is a new user of the application or an existing user.
     */
    private fun trackInstallOrUpdate() {
        var isCalled=false
        //keys are just sample keys, use suitable keys for the apps
        val preferences = HungamaMusicApp.getInstance().getSharedPreferences(
            HungamaMusicApp.getInstance().getString(R.string.app_name), 0)
        var appStatus = AppStatus.INSTALL

        var versionCode = 0
        try {
            val pInfo1: PackageInfo = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
            val longVersionCode: Long = PackageInfoCompat.getLongVersionCode(pInfo1)
            versionCode = longVersionCode.toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

            if (preferences.getInt("AppVersion", 0) < versionCode){
                appStatus = AppStatus.UPDATE

                MoEHelper.getInstance(HungamaMusicApp.getInstance()).setAppStatus(appStatus)
                isCalled=true
                preferences.edit().putInt("AppVersion", versionCode).apply()
            }

        if(!isCalled){
            // passing install/update to MoEngage SDK
            MoEHelper.getInstance(HungamaMusicApp.getInstance()).setAppStatus(appStatus)
        }

        setLog("lahglhalhog", preferences.getBoolean("has_sent_install", false).toString() + "\n" +
                preferences.getBoolean("existing", false).toString())

    }


    override fun sendEvent(iEvent: IEvent) {
        CoroutineScope(Dispatchers.IO).launch{
            sendMoengageLogEvent(iEvent.getName(),iEvent.getProperty())
        }

    }

    override fun sendUserAttribute(iEvent: IEvent) {

        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
            setLog(TAG, "Moengage isUserLogdIn ID :${SharedPrefHelper.getInstance().getUserId()!!}")
            MoEHelper.getInstance(HungamaMusicApp.getInstance()).setUniqueId(SharedPrefHelper.getInstance().getUserId()!!)
            MoEHelper.getInstance(HungamaMusicApp.getInstance()).setUserAttribute(EventConstant.HUNGAMA_ID_LOGIN,SharedPrefHelper.getInstance().getUserId()!!)
        }
        iEvent?.getProperty()?.keys?.forEach {

            if(it.equals(EventConstant.FIRST_NAME)){
                MoEHelper.getInstance(HungamaMusicApp.getInstance()).setFirstName(iEvent.getProperty().get(it)!!)
            }else if(it.equals(EventConstant.LAST_NAME)){
                MoEHelper.getInstance(com.hungama.music.HungamaMusicApp.getInstance()).setLastName(iEvent.getProperty().get(it)!!)
            }else if(it.equals(EventConstant.NAME)){
                MoEHelper.getInstance(com.hungama.music.HungamaMusicApp.getInstance()).setFullName(iEvent.getProperty().get(it)!!)
            } else if(it.equals(EventConstant.GENDER)){
                if(iEvent.getProperty().get(it)?.contains("Male",true)!!){
                    MoEHelper.getInstance(com.hungama.music.HungamaMusicApp.getInstance()).setGender(UserGender.MALE)
                }else if(iEvent.getProperty().get(it)?.contains("Female",true)!!){
                    MoEHelper.getInstance(com.hungama.music.HungamaMusicApp.getInstance()).setGender(UserGender.FEMALE)
                }else if(iEvent.getProperty().get(it)?.contains("Prefer not to say",true)!!){
                    MoEHelper.getInstance(com.hungama.music.HungamaMusicApp.getInstance()).setGender(UserGender.OTHER)
                }

            }else if(it.equals(EventConstant.PHONE)){
                MoEHelper.getInstance(HungamaMusicApp.getInstance()).setNumber(iEvent.getProperty().get(it)!!)
            }else if(it.equals(EventConstant.AGE)){
                val date= Date(DateUtils.convertDateTimeIntoMilesecond(iEvent.getProperty().get(it)!!,DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T))
                MoEHelper.getInstance(HungamaMusicApp.getInstance()).setBirthDate(date)
            }else if(it.equals(EventConstant.EMAIL)){
                MoEHelper.getInstance(com.hungama.music.HungamaMusicApp.getInstance()).setEmail(iEvent.getProperty().get(it)!!)
            }

            MoEHelper.getInstance(HungamaMusicApp.getInstance()).setUserAttribute(it,iEvent.getProperty().get(it)!!)

            MoEHelper.getInstance(HungamaMusicApp.getInstance()).setUserAttribute(EventConstant.HUNGAMA_UN, true)
            MoEHelper.getInstance(HungamaMusicApp.getInstance()).setUserAttribute("UN_App_Version",com.hungama.music.BuildConfig.VERSION_CODE.toInt())
        }

    }

    private suspend fun sendMoengageLogEvent(
        eventName: String,
        proprtyMap: java.util.HashMap<String, String>
    ) {

        try {
            if(!iGnoreProperty.contains(eventName)!!) {
                val properties1 = Properties()
                proprtyMap.keys?.forEach {
                    if (proprtyMap.get(it) != null && !TextUtils.isEmpty(proprtyMap.get(it)) && !proprtyMap?.get(it)?.equals("[]")!!) {
                        properties1.addAttribute(it, proprtyMap?.get(it))
                    }
                }

                try {
                    if(proprtyMap.containsKey(EventConstant.BUCKETNAME_EPROPERTY) || proprtyMap.containsKey(EventConstant.BUCKETTYPE_EPROPERTY)){
                        setLog(TAG, "setMoengageData Check BUCKETNAME_EPROPERTY:${proprtyMap.get(EventConstant.BUCKETNAME_EPROPERTY)} BUCKETTYPE_EPROPERTY:${proprtyMap.get(EventConstant.BUCKETTYPE_EPROPERTY)}")
                        if(proprtyMap.get(EventConstant.BUCKETNAME_EPROPERTY)!=null&&proprtyMap.get(EventConstant.BUCKETNAME_EPROPERTY)?.contains("Good",true)!!){
                            proprtyMap.put(EventConstant.BUCKETNAME_EPROPERTY,EventConstant.CONTINUE_WATCHING_NAME)
                        }else if(proprtyMap.get(EventConstant.BUCKETTYPE_EPROPERTY)!=null&&proprtyMap.get(EventConstant.BUCKETTYPE_EPROPERTY)?.contains("Good",true)!!){
                            proprtyMap.put(EventConstant.BUCKETTYPE_EPROPERTY,EventConstant.CONTINUE_WATCHING_NAME)
                        }
                    }

                    var durationFG=0
                    var durationBG=0
                    if(proprtyMap.containsKey(EventConstant.DURATION_FG_EPROPERTY) && !proprtyMap?.get(EventConstant.DURATION_FG_EPROPERTY)?.isNullOrEmpty()!!){
                        durationFG=proprtyMap?.get(EventConstant.DURATION_FG_EPROPERTY)?.toInt()!!
                    }

                    if(proprtyMap.containsKey(EventConstant.DURATION_BG_EPROPERTY) && !proprtyMap?.get(EventConstant.DURATION_BG_EPROPERTY)?.isNullOrEmpty()!!){
                        durationBG=proprtyMap?.get(EventConstant.DURATION_BG_EPROPERTY)?.toInt()!!
                    }

                    setLog(
                        TAG,
                        "setMoengageData STREAM_ENAME eventName:$eventName durationFG:${durationFG} durationBG:${durationBG} proprtyMap:$proprtyMap"
                    )


                    if (eventName.equals(EventConstant.STREAM_ENAME, true)) {
                        if (!lastStreamEventSongId.equals(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!) && (durationFG>10 || durationBG>10)) {

                            //sample track event
                            MoEHelper.getInstance(HungamaMusicApp.getInstance())
                                .trackEvent(eventName, properties1)
                            lastStreamEventSongId = proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!

                            setLog(
                                TAG,
                                "setMoengageData  STREAM_ENAME eventName:$eventName eventProperties:$proprtyMap"
                            )
                        }else{
                            setLog(
                                TAG,
                                "setMoengageData duplicate STREAM_ENAME eventName:$eventName eventProperties:$proprtyMap"
                            )
                        }
                    }else if (eventName.equals(EventConstant.STREAM_START_ENAME, true)) {
                        if (!lastStreamStartEventSongId.equals(proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!)) {

                            //sample track event
                            MoEHelper.getInstance(HungamaMusicApp.getInstance()).trackEvent(eventName, properties1)
                            lastStreamStartEventSongId =
                                proprtyMap?.get(EventConstant.CONTENTID_EPROPERTY)!!

                            setLog(
                                TAG,
                                "setMoengageData STREAM_ENAME eventName:$eventName eventProperties:$proprtyMap"
                            )
                        }else{
                            setLog(
                                TAG,
                                "setMoengageData duplicate STREAM_ENAME eventName:$eventName eventProperties:$proprtyMap"
                            )
                        }
                    } else {
                        //sample track event
                        MoEHelper.getInstance(HungamaMusicApp.getInstance())
                            .trackEvent(eventName, properties1)
                        setLog(
                            TAG,
                            "setMoengageData eventName:$eventName eventProperties:$proprtyMap"
                        )
                    }


                } catch (e: Exception) {
                    setLog(
                        TAG,
                        "setMoengageData error 1:${e.printStackTrace()}"
                    )
                }


            }

        } catch (exp: Exception) {
            setLog(
                TAG,
                "setMoengageData error 2:${exp.printStackTrace()}"
            )
        }

    }


}