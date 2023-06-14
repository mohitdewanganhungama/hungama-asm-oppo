package com.hungama.music.ui.main.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.AppLaunchEvent
import com.hungama.music.utils.CommonUtils.getDeeplinkIntentData
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeeplinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manageIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageIntent(intent)
    }


    private fun manageIntent(intent: Intent?) {
        if (intent != null && intent?.data != null) {
            val appLinkAction: String? = intent?.action
            val appLinkData: Uri? = intent?.data
            showDeepLinkUrl(appLinkAction, appLinkData)
        }else{
            sendEvent()
            startActivity(Intent(this, MainActivity::class.java).putExtra(EventConstant.SOURCE,"deeplink"))
        }
    }

    private fun showDeepLinkUrl(appLinkAction: String?, appLinkData: Uri?) {
        setLog("TAG", "showDeepLinkUrl: appLinkData:$appLinkData appLinkAction:$appLinkAction")
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            sendEvent()
            val intent = getDeeplinkIntentData(appLinkData)
            intent.setClass(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun sendEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.AB_NC_EPROPERTY, "1")
            dataMap.put(EventConstant.SOURCE_EPROPERTY, "Android")
            EventManager.getInstance().sendEvent(AppLaunchEvent(dataMap))
        }
    }
}