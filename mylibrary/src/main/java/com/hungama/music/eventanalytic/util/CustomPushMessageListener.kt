package com.hungama.music.eventanalytic.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat.Builder
//import com.apxor.androidsdk.plugins.push.ApxorPushAPI
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.ui.main.view.activity.CommonWebViewActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.home.eventsubscriber.MoengageSubscriber
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import com.moengage.pushbase.PushConstants.NAV_ACTION
import com.moengage.pushbase.model.NotificationPayload
import com.moengage.pushbase.model.action.NavigationAction
import com.moengage.pushbase.push.PushMessageListener
import java.lang.Exception


class CustomPushMessageListener : PushMessageListener() {

    // decide whether notification should be shown or not. If super() returns false this method
    // should return false. In case super() isn't called notification will not be displayed.
    override fun isNotificationRequired(context: Context, payload: Bundle): Boolean {
        val shouldDisplayNotification = super.isNotificationRequired(context, payload)

        setLog("CustomPush", "isNotificationRequired 1 payload:${payload}")
        // do not show notification if MoEngage SDK returns false.
        if (shouldDisplayNotification) {
            var mobileNotification=shouldDisplayNotification
            // app's logic to decide whether to show notification or not.
            // for illustration purpose reading notification preference from SharedPreferences and
            // deciding whether to show notification or not. Logic can vary from application to
            // application.

            val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_GENERAL_SETTING)
            if (userSettingRespModel?.data != null && userSettingRespModel.data?.data?.get(0)?.preference?.get(0) != null) {
                mobileNotification= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.mobileNotification!!

                setLog("CustomPush", "isNotificationRequired 2 mobileNotification:${mobileNotification}")
            }


            return mobileNotification
        }
        return shouldDisplayNotification

    }

    // customise the notification builder object as required
    override fun onCreateNotification(context: Context, notificationPayload: NotificationPayload): Builder {
        // get the object constructed by MoEngage SDK
        val builder = super.onCreateNotification(context, notificationPayload)

        if(notificationPayload?.sound!=null&&!TextUtils.isEmpty(notificationPayload?.sound)){
            MoengageSubscriber.notificationTone = ""+ContentResolver.SCHEME_ANDROID_RESOURCE.toString() + "://" + HungamaMusicApp.getInstance().getPackageName() + "/" + R.raw.notify_
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)

        builder.setAutoCancel(true)
        playNotificationSound()


        // return the builder object to the SDK for posting notification.
        return builder


    }

    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(MoengageSubscriber.notificationTone)
            val r: Ringtone = RingtoneManager.getRingtone(HungamaMusicApp.getInstance(), alarmSound)
            r.play()

            setLog("onNotifi", "playNotificationSound  play${MoengageSubscriber.notificationTone}")
        } catch (e: Exception) {
            setLog("onNotifi", "playNotificationSound  Exception"+e.printStackTrace())
            e.printStackTrace()
        }
    }

    override fun onNotificationCleared(context: Context, payload: Bundle) {
        super.onNotificationCleared(context, payload)
        //callback for notification cleared.
        setLog("onNotifi", "onNotificationCleared payload"+payload)
    }

    override fun onNotificationReceived(context: Context, payload: Bundle) {
        super.onNotificationReceived(context, payload)
        //callback for push notification received.
        setLog("onNotifi", "onNotificationReceived payload"+payload)
        //setLog("onNotifi", "onHandleRedirection gcm_webUrl"+payload.getString("gcm_webUrl"))
        //setLog("onNotifi", "onHandleRedirection moe_webUrl"+payload.getString("moe_webUrl"))


    }

    override fun onHandleRedirection(activity: Activity, payload: Bundle) {
        super.onHandleRedirection(activity, payload)
        //callback for notification clicked. if you want to handle redirection then do not call super()
        // and add the redirection logic here.

        setLog("onNotifi", "onHandleRedirection payload "+payload)
        val action = payload.getParcelable(NAV_ACTION) as NavigationAction?
        var navigationUrl = ""
        if (action != null) {
            setLog("onNotifi", "onHandleRedirection moe_navAction "+action)
            setLog("onNotifi", "onHandleRedirection navigationUrl "+action.navigationUrl)
            navigationUrl = action.navigationUrl
        }
        setLog("onNotifi", "onHandleRedirection gcm_webUrl "+payload.getString("gcm_webUrl"))
        setLog("onNotifi", "onHandleRedirection moe_webUrl "+payload.getString("moe_webUrl"))
        setLog("onNotifi", "onHandleRedirection gcm_notificationType "+payload.getString("gcm_notificationType"))

        MoEPushHelper.getInstance().logNotificationClick(activity, activity.intent)

        if(payload.containsKey("moe_webUrl") && payload.getString("moe_webUrl")!=null && !TextUtils.isEmpty(payload.getString("moe_webUrl"))){
            showDeepLinkUrl(activity, Uri.parse(payload.getString("moe_webUrl")))
        }else if(payload.containsKey("gcm_webUrl") && payload.getString("gcm_webUrl")!=null && !TextUtils.isEmpty(payload.getString("gcm_webUrl"))){
            if (payload.containsKey("gcm_notificationType")){
                var intent=activity?.intent
                intent?.setClass(activity, CommonWebViewActivity::class.java)
                intent?.putExtra(Constant.EXTRA_URL,payload.getString("gcm_webUrl"))
                activity?.startActivity(intent)
                setLog("TAG", "CommonWebViewActivity start main Activity")
            }else{
                showDeepLinkUrl(activity, Uri.parse(payload.getString("gcm_webUrl")))
            }
        }else if(navigationUrl!=null && !TextUtils.isEmpty(navigationUrl)){
            showDeepLinkUrl(activity, Uri.parse(navigationUrl))
        }else{
            activity?.startActivity(Intent(activity, MainActivity::class.java))
        }


    }

    private fun showDeepLinkUrl(activity:Activity,appLinkData: Uri) {
        setLog("TAG", "showDeepLinkUrl: appLinkData:$appLinkData")

        val intent = CommonUtils.getDeeplinkIntentData(appLinkData)
        intent.setClass(activity, MainActivity::class.java)
        activity?.startActivity(intent)
        activity?.finish()



    }

}
