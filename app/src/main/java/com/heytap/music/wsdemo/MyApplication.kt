package com.heytap.music.wsdemo

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.hungama.music.HungamaMusicApp
import com.hungama.music.utils.CommonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    companion object {
        var hungamaMusicApp: MyApplication? = null
        fun getInstance(): MyApplication {
            return hungamaMusicApp!!
        }

    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
/*        val appLicatn = HungamaMusicApp()
        appLicatn.onCreate()*/
        try {
            CommonUtils.setLog("RemoteWorkDone", "setUpRemoteConfig: 1.1")
            CoroutineScope(Dispatchers.IO).launch{
                CommonUtils.setLog("RemoteWorkDone", "setUpRemoteConfig: 1.2")
                Firebase.remoteConfig.fetchAndActivate()
                CommonUtils.setLog("RemoteWorkDone", "setUpRemoteConfig: 1.3")
            }
        }catch (e:Exception){
            CommonUtils.setLog("RemoteWorkDone", "setUpRemoteConfig: 1.4")
        }
    }
}