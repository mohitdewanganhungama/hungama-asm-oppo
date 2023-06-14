package com.hungama.music.data.webservice.remote.data

import com.android.volley.VolleyError
import org.json.JSONArray
import org.json.JSONObject

interface DataValues {
    fun setJsonDataResponse(response: JSONObject?)
    fun setVolleyError(volleyError: VolleyError?)
    fun setJsonArrayDataResponse(jsonArray: JSONArray?) {}
    fun setJsonStringDataResponse(jsonString: String?) {}
}