package com.hungama.music.eventanalytic.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.appsflyer.FirebaseMessagingServiceListener
//import com.apxor.androidsdk.plugins.push.ApxorPushAPI
import com.google.firebase.messaging.RemoteMessage
import com.hungama.music.HungamaMusicApp
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.ui.main.view.activity.DeeplinkActivity
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.R
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper


class MyFirebaseMessagingService : FirebaseMessagingServiceListener() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        setLog("RemoteMessage", "onMessageReceived 1: ${remoteMessage.data}")

        if(remoteMessage.data.containsKey("af-uinstall-tracking")){ // "uinstall" is not a typo
            return;
        }

        /*
         * PROCESS DATA PAYLOAD
         * API will always sent the push data into Data payload only.
         * We do not use NOTIFICATION PAYLOAD at all.
         */

        if (MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.data) && MoEPushHelper.getInstance().isSilentPush(remoteMessage.data)){
            MoEFireBaseHelper.getInstance().passPushPayload(applicationContext, remoteMessage.data)
            return
        }

        setLog("RemoteMessage", "onMessageReceived 2")
        if (MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.data)){
            MoEFireBaseHelper.getInstance().passPushPayload(applicationContext, remoteMessage.data)
            MoEPushHelper.getInstance().logNotificationReceived(HungamaMusicApp.getInstance(), remoteMessage.data)
        }/*else if (ApxorPushAPI.isApxorNotification(remoteMessage)){
            ApxorPushAPI.handleNotification(remoteMessage, getApplicationContext());
        }*/else {
            // your app's business logic to show notification
            sendNotification(remoteMessage)
        }
        setLog("RemoteMessage", "onMessageReceived 3")

    }

    private fun sendNotification(remoteMessageData: RemoteMessage) {
        setLog("sendNotification", "notification: ${remoteMessageData.notification}")
        setLog("sendNotification", "data: ${remoteMessageData.data}")
        val CHANNEL_ID: String = HungamaMusicApp.getInstance().packageName
        var body: String = remoteMessageData.notification?.body.toString()
        var title: String = remoteMessageData.notification?.title.toString()

        if(TextUtils.isEmpty(body)){
            body= remoteMessageData.data.get("body").toString()
        }

        if(TextUtils.isEmpty(title)){
            title= remoteMessageData.data.get("title").toString()
        }

        if (!TextUtils.isEmpty(body) && !body.equals("null", true) && !TextUtils.isEmpty(title) && !title.equals("null", true)) {
            val notificationID = System.currentTimeMillis().toInt()
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this,CHANNEL_ID)
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLights(Color.YELLOW, 500, 1000)

//            intent = Intent(applicationContext, MainActivity::class.java)
            val intent = Intent(applicationContext, DeeplinkActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(Constant.NOTIFICATION_ID, notificationID)
            intent.putExtra(Constant.NOTIFICATION_NAME, title)
            var pendingIntent:PendingIntent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(
                    this,
                    notificationID,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }else{
                pendingIntent = PendingIntent.getActivity(
                    this,
                    notificationID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val dataMap = HashMap<String, String>()

            dataMap[EventConstant.SOURCE_DETAILS_EPROPERTY] = "Notification_" +
                    notificationID + "_" + title + if (intent.action.equals(ACTION_DELETE)) "Delete" else ""

            EventManager.getInstance().sendEvent(PageViewEvent(dataMap))

            notificationBuilder.setContentIntent(pendingIntent)
            notificationBuilder.setAutoCancel(true)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                //Configure Notification Channel
                notificationChannel.description = getString(R.string.app_name)
                notificationChannel.enableLights(true)
                notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                notificationChannel.enableVibration(true)

                notificationManager.createNotificationChannel(notificationChannel)
            }

            notificationManager.notify(Math.random().toInt(), notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        setLog("onNewToken", "onNewToken fcm: $token")
        MoEFireBaseHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)
//        MoEMiPushHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)
//        MoEPushKitHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)

        AppsFlyerLib.getInstance().updateServerUninstallToken(HungamaMusicApp.getInstance(), token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }
}