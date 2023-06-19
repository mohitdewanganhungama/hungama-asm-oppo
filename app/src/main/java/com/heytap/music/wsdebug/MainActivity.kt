package com.heytap.music.wsdebug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hungama.music.ui.main.view.activity.SplashActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
CommonUtillls.getFirebaseConfigAdsData()
        startActivity(Intent(this, SplashActivity::class.java))
    }
}