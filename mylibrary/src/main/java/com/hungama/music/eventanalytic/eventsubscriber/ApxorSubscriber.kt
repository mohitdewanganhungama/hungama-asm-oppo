//package com.hungama.music.home.eventsubscriber
//
//import android.text.TextUtils
//import android.util.Log
////import com.apxor.androidsdk.core.ApxorSDK
////import com.apxor.androidsdk.core.Attributes
//import com.hungama.music.HungamaMusicApp
//import com.hungama.music.eventanalytic.EventConstant
//import com.hungama.music.eventanalytic.event.EventType
//import com.hungama.music.eventanalytic.event.IEvent
//import com.hungama.music.eventanalytic.event.ISubscriber
//import com.hungama.music.utils.CommonUtils.setLog
//import com.hungama.music.utils.preference.SharedPrefHelper
//import com.moe.pushlibrary.MoEHelper
//import kotlin.collections.ArrayList
//
//
///**
// * Created by Chetan(chetan@saeculumsolutions.com) on 8/23/2021.
// * Purpose:
// */
//class ApxorSubscriber : ISubscriber {
//    var TAG = "ApxorLog"
//
////    val APXOR_APP_ID = "d9b0a361-79e6-4ea2-9276-e75a540cedf5"
//
//    companion object{
//        val APXOR_APP_ID = "3e11cb32-52bb-4fd2-bba2-a27496f5a574"
//    }
//
//
//    var iGnore = ArrayList<EventType>()
//    var iGnoreProperty = ArrayList<String>()
//    var conversionData: Map<String, Any>? = null
//    var lastEvent=""
//    init {
//        basicSetUp()
//    }
//
//
//    /**
//     * amplitude SetUp
//     */
//    fun basicSetUp() {
//        setLog(TAG, "ApxorSetUp")
//
////        ApxorSDK.initialize(APXOR_APP_ID, HungamaMusicApp.getInstance());
//    }
//
//
//
//    override fun sendEvent(iEvent: IEvent) {
//        /* Track Events in real time */
//
//        if(!lastEvent?.equals(iEvent.getName())){
//            if(!iGnoreProperty.contains(iEvent?.getName())!!){
//                val attributes = Attributes()
//                iEvent?.getProperty()?.keys?.forEach {
//                    if (iEvent?.getProperty().get(it) != null && !TextUtils.isEmpty(iEvent?.getProperty().get(it)) && !iEvent?.getProperty().get(it)?.equals("[]")!!) {
//                        attributes.putAttribute(it,iEvent?.getProperty().get(it))
//                    }
//
//                }
//                ApxorSDK.logAppEvent(iEvent.getName(), attributes)
//
//                lastEvent=iEvent.getName()
//            }
//        }
//
//
//
//
//
//    }
//
//    override fun sendUserAttribute(iEvent: IEvent) {
//        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
//            setLog(TAG, "ApxorSubscriber isUserLogdIn ID :${SharedPrefHelper.getInstance().getUserId()!!}")
//            ApxorSDK.setUserIdentifier(SharedPrefHelper.getInstance().getUserId()!!)
//
//            val userInfo = Attributes()
//            userInfo?.putAttribute(EventConstant.HUNGAMA_ID_LOGIN,SharedPrefHelper.getInstance().getUserId()!!)
//            ApxorSDK.setUserCustomInfo(userInfo)
//        }
//
//        iEvent?.getProperty()?.keys?.forEach {
//            val userInfo = Attributes()
//            userInfo?.putAttribute(it,iEvent?.getProperty().get(it)!!)
//            ApxorSDK.setUserCustomInfo(userInfo)
//        }
//
//        val attributes = Attributes()
//        attributes?.putAttribute(EventConstant.HUNGAMA_UN,true)
//        ApxorSDK.setUserCustomInfo(attributes)
//
//    }
//
//}