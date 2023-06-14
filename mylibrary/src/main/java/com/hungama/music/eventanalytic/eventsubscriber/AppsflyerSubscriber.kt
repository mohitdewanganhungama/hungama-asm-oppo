package com.hungama.music.home.eventsubscriber

import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.hungama.music.HungamaMusicApp
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent
import com.hungama.music.eventanalytic.event.ISubscriber
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import java.util.*


/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 8/23/2021.
 * Purpose:
 */
class AppsflyerSubscriber : ISubscriber {
    var TAG = "AppsflyerLog"

    val APPSFLYER_APP_ID = "za6ZAm5SkfE7T7C6PTFJo3"
    var conversionData: Map<String, Any>? = null

    init {
        appsflyerSetUp()
    }


    /**
     * amplitude SetUp
     */
    fun appsflyerSetUp() {
        setLog(TAG, "appsflyerSetUp")


        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionDataMap: Map<String, Any>) {
                for (attrName in conversionDataMap.keys){
                    setLog(
                        TAG,
                        "Conversion attribute: " + attrName + " = " + conversionDataMap[attrName])

                    if(attrName.contains("af_referrer_customer_id",true)){
                        SharedPrefHelper.getInstance().save(PrefConstant.AF_REFERRER_CUSTOMER_ID,conversionDataMap[attrName].toString())
                        setLog(TAG,"AppsFlyerLib conversionListener af_referrer_customer_id:${conversionDataMap[attrName].toString()}")
                    }
                }

                val status = Objects.requireNonNull(conversionDataMap["af_status"]).toString()
                if (status == "Non-organic") {
                    if (Objects.requireNonNull(conversionDataMap["is_first_launch"])
                            .toString() == "true"
                    ) {
                        setLog(
                            TAG,
                            "AppsFlyerLib Conversion: First Launch"
                        )
                    } else {
                        setLog(
                            TAG,
                            "AppsFlyerLib Conversion: Not First Launch"
                        )
                    }
                } else {
                    setLog(
                        TAG,
                        "AppsFlyerLib Conversion: This is an organic install."
                    )
                }
                conversionData = conversionDataMap
            }

            override fun onConversionDataFail(errorMessage: String) {
                setLog(
                    TAG,
                    "AppsFlyerLib error getting conversion data: $errorMessage"
                )
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                setLog(
                    TAG,
                    "onAppOpenAttribution: This is fake call."
                )
            }

            override fun onAttributionFailure(errorMessage: String) {
                setLog(
                    TAG,
                    "AppsFlyerLib error onAttributionFailure : $errorMessage"
                )
            }
        }

        AppsFlyerLib.getInstance().init(
            APPSFLYER_APP_ID,
            conversionListener,
            HungamaMusicApp.getInstance().applicationContext
        ).subscribeForDeepLink(DeepLinkListener { deepLinkResult ->
            val dlStatus = deepLinkResult.status

            if (dlStatus == DeepLinkResult.Status.FOUND) {
                setLog(
                    TAG,
                    "AppsFlyerLib Deep link found"
                )
            } else if (dlStatus == DeepLinkResult.Status.NOT_FOUND) {
                setLog(
                    TAG,
                    "AppsFlyerLib Deep link not found"
                )
            }
            else {
                // dlStatus == DeepLinkResult.Status.ERROR
                val dlError = deepLinkResult.error
                setLog(
                    TAG,
                    "AppsFlyerLib There was an error getting Deep Link data: $dlError"
                )
            }
            val deepLinkObj = deepLinkResult.deepLink
            try {
                setLog(
                    TAG,
                    "AppsFlyerLib The DeepLink data is: ${deepLinkObj}"
                )

                setLog("","AppsFlyerLib af_referrer_customer_id:${deepLinkObj?.getStringValue("af_referrer_customer_id")}")

                SharedPrefHelper.getInstance().save(PrefConstant.AF_REFERRER_CUSTOMER_ID,deepLinkObj?.getStringValue("af_referrer_customer_id"))
            } catch (e: Exception) {
                setLog(
                    TAG,
                    "AppsFlyerLib DeepLink data came back null"
                )
            }
            // An example for using is_deferred
            if (deepLinkObj.isDeferred!!) {
                setLog(
                    TAG,
                    "AppsFlyerLib This is a deferred deep link"
                )
            } else {
                setLog(
                    TAG,
                    "AppsFlyerLib This is a direct deep link"
                )
            }

            setLog(
                TAG,
                "AppsFlyerLib DeepLink url:${deepLinkResult.deepLink?.deepLinkValue}"
            )
        })

        val userId = SharedPrefHelper.getInstance().getUserId().toString()
        if (userId.isNotEmpty()) {
            AppsFlyerLib.getInstance().setCustomerUserId(userId)
        }
        else{
            AppsFlyerLib.getInstance().waitForCustomerUserId(true)
        }

        AppsFlyerLib.getInstance().start(HungamaMusicApp.getInstance().applicationContext, APPSFLYER_APP_ID, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                setLog("AppsFlyerLib", "Launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                setLog(
                    "AppsFlyerLib", "Launch failed to be sent:\n" +
                            "Error code: " + errorCode + "\n"
                            + "Error description: " + errorDesc
                )
            }
        })
        AppsFlyerLib.getInstance().setAppId(APPSFLYER_APP_ID)
        AppsFlyerLib.getInstance().setDebugLog(true)
    }


    override fun sendEvent(iEvent: IEvent) {
        /* Track Events in real time */

    }

    override fun sendUserAttribute(iEvent: IEvent) {

    }
}