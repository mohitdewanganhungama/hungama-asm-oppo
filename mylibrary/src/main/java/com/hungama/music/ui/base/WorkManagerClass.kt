package com.hungama.music.ui.base

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.preference.SharedPrefHelper
import org.json.JSONObject


class WorkManagerClass(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    val context = appContext
    override fun doWork(): Result {
        val durationData = inputData.getString("duration")
        val date = inputData.getString("date")
        val uid = inputData.getString("uid")

        val reqJsonObject = JSONObject()
        reqJsonObject.put("uid", uid)
        reqJsonObject.put("user_streamed_min", durationData)
        reqJsonObject.put("first_stream_start_time", date)
        reqJsonObject.put("is_first_stream_started", "true")

        CommonUtils.setLog("SongDurationData", " workManager " +reqJsonObject.toString() + "\n"+ durationData.toString())

        val cacheRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, WSConstants.UPDATE_USER_PREVIEW_DETAILS, JSONObject(reqJsonObject.toString()),
            Response.Listener {
                CommonUtils.setLog("backgroundData", it.toString())

//                Toast.makeText(context, "Success " , Toast.LENGTH_LONG).show()
                WorkManager.getInstance(context).cancelAllWork()
            }, Response.ErrorListener { error -> //showData(error.toString());
                CommonUtils.setLog("backgroundData",  "Error" + Gson().toJson(error))
//                Toast.makeText(context, error.message.toString(), Toast.LENGTH_LONG).show()
                WorkManager.getInstance(context).cancelAllWork()
            })
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()
                params.put(
                    WSConstants.WS_HEADER_CONTENT_TYPE,
                    WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                )
                return params
            }
        }
        Volley.newRequestQueue(context).add(cacheRequest)
        return Result.success()
    }

}