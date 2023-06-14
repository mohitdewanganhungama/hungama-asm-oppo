package com.hungama.music.ui.main.view.activity

import android.content.ComponentName
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.browser.customtabs.*
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.activity_chrome_custom_tab.*

class ChromeCustomTab : AppCompatActivity() {
    lateinit var serviceConnection: CustomTabsServiceConnection
    lateinit var client: CustomTabsClient
    lateinit var session: CustomTabsSession
    var builder = CustomTabsIntent.Builder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chrome_custom_tab)
        serviceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, mClient: CustomTabsClient) {
                setLog("Service", "Connected")
                client = mClient
                client.warmup(0L)
                val callback = RabbitCallback()
                session = mClient.newSession(callback)!!
                builder.setSession(session)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                setLog("Service", "Disconnected")
            }
        }
        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", serviceConnection)
        button.setOnClickListener {
            val url = "https://payments.hungama.com/payment?auth=13951dcd3d8c096ded040b10b108ee06&identity=717783271&product_id=1&country=in&platform_id=1&plan_type=TVOD-Premium&app_version=1.0&build_number=172&upilist=&hardware_id=49f4ef266efd8b90&aff_code=&content_id=77408690&live_event_id=&extra_data=&utm_source=&utm_medium=&utm_campaign=&lang=en&amp_user_id=null&amp_device_id=49f4ef266efd8b90"
            //val url = "https://www.wikipedia.org"
            val customTabsIntent: CustomTabsIntent = builder.build()
            builder.setShowTitle(false)
            customTabsIntent.launchUrl(this, Uri.parse(url))
        }
    }

    override fun onStart() {
        super.onStart()
        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", serviceConnection)
    }

    class RabbitCallback : CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            super.onNavigationEvent(navigationEvent, extras)
            setLog("Nav", navigationEvent.toString())
            when (navigationEvent) {
                1 -> {
                    setLog("Navigation", "Start") // NAVIGATION_STARTED
                }
                2 -> setLog("Navigation", "Finished") // NAVIGATION_FINISHED
                3 -> setLog("Navigation", "Failed") // NAVIGATION_FAILED
                4 -> setLog("Navigation", "Aborted") // NAVIGATION_ABORTED
                5 -> setLog("Navigation", "Tab Shown") // TAB_SHOWN
                6 -> setLog("Navigation", "Tab Hidden") // TAB_HIDDEN
                else -> setLog("Navigation", "Else")
            }
        }
    }

}