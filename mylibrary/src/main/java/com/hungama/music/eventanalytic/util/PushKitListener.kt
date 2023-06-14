//package com.hungama.music.eventanalytic.util
//
//import android.util.Log
//import com.huawei.hms.push.RemoteMessage
//import com.hungama.music.HungamaMusicApp
//import com.hungama.music.eventanalytic.EventConstant
//import com.hungama.music.eventanalytic.event.EventManager
//import com.hungama.music.home.eventreporter.UserAttributeEvent
//import com.hungama.music.utils.CommonUtils.setLog
//import com.moengage.firebase.MoEFireBaseHelper
//import com.moengage.hms.pushkit.MoEPushKitHelper
//import com.moengage.hms.pushkit.listener.PushKitEventListener
//import com.moengage.mi.MoEMiPushHelper
//import com.moengage.pushbase.MoEPushHelper
//
//class PushKitListener : PushKitEventListener() {
//
//    override fun onNonMoEngageMessageReceived(remoteMessage: RemoteMessage) {
//        if (MoEPushHelper.getInstance()?.isFromMoEngagePlatform(remoteMessage.dataOfMap) && MoEPushHelper.getInstance()
//                .isSilentPush(remoteMessage.dataOfMap)
//        ) {
//            MoEFireBaseHelper.getInstance()
//                .passPushPayload(HungamaMusicApp.getInstance(), remoteMessage.dataOfMap)
//            return
//        }
//
//        if (MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.dataOfMap)) {
//            MoEPushHelper.getInstance()
//                .logNotificationReceived(HungamaMusicApp.getInstance(), remoteMessage.dataOfMap)
//        }
//
//        super.onNonMoEngageMessageReceived(remoteMessage)
//    }
//    override fun onTokenAvailable(token: String) {
//        super.onTokenAvailable(token)
//        setLog("PushKitListener","onTokenAvailable(): Token Callback Received. Token: %s"+token)
//        // push token received, add your processing logic here
//        if(!HungamaMusicApp.getInstance().isFcmTokenPass!!) {
//            // push token received, add your processing logic here
//            MoEFireBaseHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(), token)
//            MoEMiPushHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)
//            MoEPushKitHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)
//            setLog("onTokenAvailable()", "Token Callback Received. Token: $token")
//            val userDataMap= java.util.HashMap<String, String>()
//            userDataMap.put(EventConstant.NOTIFICATION_TOKEN, token)
//            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
//
//            HungamaMusicApp.getInstance().isFcmTokenPass=true
//        }
//    }
//}