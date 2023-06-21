package com.heytap.music.wsdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.hungama.music.data.model.SongDurationConfigModel
import com.hungama.music.ui.main.view.activity.SplashActivity
import kotlinx.coroutines.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        try {
            CoroutineScope(Dispatchers.IO).launch{
                Firebase.remoteConfig.fetchAndActivate()
                startActivity(Intent(this@MainActivity, SplashActivity::class.java))
            }
        }catch (e:Exception){
        }
    }

    fun getFirebaseData():Unit = runBlocking(Dispatchers.IO){
        val remoteConfig = Firebase.remoteConfig
        val nudge_stream_preview = remoteConfig.getString("nudge_stream_preview")
        Log.v("haohgdsa", nudge_stream_preview)
    }
}