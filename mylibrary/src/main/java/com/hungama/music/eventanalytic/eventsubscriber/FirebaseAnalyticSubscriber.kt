package com.hungama.music.home.eventsubscriber

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent
import com.hungama.music.eventanalytic.event.ISubscriber
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.BuildConfig
import kotlin.collections.ArrayList


/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/23/2021.
 * Purpose:
 */
class FirebaseAnalyticSubscriber : ISubscriber {
    var TAG = "FirebaseAnalytics_Log"

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    var iGnore = ArrayList<EventType>()
    var iGnoreProperty = ArrayList<String>()
    var conversionData: Map<String, Any>? = null
    init {
        basicSetUp()
    }


    /**
     * amplitude SetUp
     */
    fun basicSetUp() {
        setLog(TAG, "FirebaseAnalytics SetUp")
// Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics?.setAnalyticsCollectionEnabled(true)
        setFirebaseUserProperty()

    }
    private fun setFirebaseUserProperty() {
        Firebase.analytics.setUserId(SharedPrefHelper.getInstance().getUserId())
        Firebase.analytics.setUserProperty("version_code", BuildConfig.VERSION_CODE.toString())
        if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().get(PrefConstant.USER_EMAIL, ""))) {
            Firebase.analytics.setUserProperty(
                "email",
                SharedPrefHelper.getInstance().get(PrefConstant.USER_EMAIL, "")
            )
        }

        if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().get(PrefConstant.USER_MOBILE, ""))) {
            Firebase.analytics.setUserProperty(
                "mobile",
                SharedPrefHelper.getInstance().get(PrefConstant.USER_MOBILE, "")
            )
        }

    }


    override fun sendEvent(iEvent: IEvent) {
        /* Track Events in real time */

        if(!iGnoreProperty.contains(iEvent?.getName())!!){
            val bundle = Bundle()
            iEvent?.getProperty()?.keys?.forEach {
                var key = it.replace(" - ", "_")
                key = key.replace("- ", "_")
                key = key.replace(" ", "_")
                key = key.replace("-", "_")
                var value = iEvent.getProperty().get(it)?.replace(" - ", "_")
                value = value?.replace("- ", "_")
                value = value?.replace(" ", "_")
                value = value?.replace("-", "_")
                bundle.putString(key,value)
            }
            var eventNameLatest = iEvent.getName().replace(" ", "_")
             eventNameLatest = eventNameLatest.replace("-", "_")

            FirebaseAnalytics.Event.SIGN_UP
            firebaseAnalytics.logEvent(eventNameLatest,bundle)
        }




    }

    override fun sendUserAttribute(iEvent: IEvent) {
        firebaseAnalytics?.setUserProperty(EventConstant.HUNGAMA_UN,""+true)
        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
            setLog(TAG, "Firebase Subscriber HUNGAMA_ID_OLD_1:${SharedPrefHelper.getInstance().getUserId()!!}")
            firebaseAnalytics?.setUserId(SharedPrefHelper.getInstance().getUserId()!!)
            firebaseAnalytics?.setUserProperty(EventConstant.HUNGAMA_ID_LOGIN,SharedPrefHelper.getInstance().getUserId()!!)
        }

        iEvent?.getProperty()?.keys?.forEach {
            var key = it.replace(" - ", "_")
            key = key.replace("- ", "_")
            key = key.replace(" ", "_")
            key = key.replace("-", "_")
            var value = iEvent?.getProperty()?.get(it)?.replace(" - ", "_")
            value = value?.replace("- ", "_")
            value = value?.replace(" ", "_")
            value = value?.replace("-", "_")
            firebaseAnalytics?.setUserProperty(key,value)
        }


    }

}