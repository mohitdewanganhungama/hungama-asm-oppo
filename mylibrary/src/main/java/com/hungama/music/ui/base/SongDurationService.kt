package com.hungama.music.ui.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.ui.main.viewmodel.SongDurationConfigViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.preference.SharedPrefHelper
import org.json.JSONObject
import com.hungama.music.R
import java.util.HashMap


class SongDurationService : Service() {

    lateinit var songDurationConfigViewModel : SongDurationConfigViewModel
    lateinit var context : Context
    override fun onBind(intent: Intent): IBinder? {

        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("lngagagasfgsafag", intent?.getStringExtra("songDurationData").toString())

        val CHANNELID = "Foreground Service ID"
        val channel = NotificationChannel(
            CHANNELID,
            CHANNELID,
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification.Builder = Notification.Builder(this, CHANNELID)
            .setContentText("Service is running")
            .setContentTitle("Service enabled")
//            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setSmallIcon(R.drawable.logo_bg_old)
            .setColor(ContextCompat.getColor(context,R.color.background))

        startForeground(1001, notification.build())

        val reqJsonObject = JSONObject()
        reqJsonObject.put("uid", SharedPrefHelper.getInstance().getUserId())
        reqJsonObject.put("user_streamed_min", 3)
        reqJsonObject.put("first_stream_start_time", "023-03-23T11:10:01.845Z")
        reqJsonObject.put("is_first_stream_started", "true")

        CommonUtils.setLog("dutduyfci", reqJsonObject.toString())

        val cacheRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, WSConstants.UPDATE_USER_PREVIEW_DETAILS, JSONObject(reqJsonObject.toString()),
            Response.Listener {
                if (it != null) {
                    CommonUtils.setLog("backgroundData", it.toString())
                    Toast.makeText(this, "Success ", Toast.LENGTH_LONG).show()
                    stopSelf()
                }
            }, Response.ErrorListener { error -> //showData(error.toString());
                CommonUtils.setLog("backgroundData",  "Error" + Gson().toJson(error))
                Toast.makeText(this, error.message.toString(), Toast.LENGTH_LONG).show()
                stopSelf()
            })
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params.put(
                    WSConstants.WS_HEADER_CONTENT_TYPE,
                    WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                )
                return params
            }
        }
        Volley.newRequestQueue(this).add(cacheRequest)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        context = this
    }
}