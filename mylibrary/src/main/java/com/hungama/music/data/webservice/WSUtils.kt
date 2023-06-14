//package com.hungama.music.data.webservice
//
//import android.util.Log
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.OkHttpClient
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.ResponseBody
//import okhttp3.logging.HttpLoggingInterceptor
//import org.json.JSONArray
//import org.json.JSONException
//import org.json.JSONObject
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.io.IOException
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//class WSUtils {
//    companion object {
//        private var retroWsCallback: ApiInterface? = null
//
//        fun getClient(): ApiInterface {
//
//            try {
//                if (retroWsCallback == null) {
//
//                    val httpClient = OkHttpClient.Builder()
//
//                    httpClient.connectTimeout(WSConstants.WS_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
//                    httpClient.readTimeout(WSConstants.WS_READ_TIMEOUT, TimeUnit.SECONDS)
//                    val logging = HttpLoggingInterceptor()
//                    logging.level = HttpLoggingInterceptor.Level.BODY
//                    httpClient.addInterceptor(logging)  // <-- this is the important line!
//
//                    val client = Retrofit.Builder()
//                        .baseUrl(WSConstants.BASE_URL)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                        .client(httpClient.build())
//                        .build()
//
//                    retroWsCallback = client.create(ApiInterface::class.java)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            return retroWsCallback as ApiInterface
//        }
//
//        fun getResponseJSON(response: Response<ResponseBody>): JSONObject? {
//            try {
//                var responseString = ""
//                val body = response.body()
//                val errorBody = response.errorBody()
//
//                if (body != null) {
//                    responseString = body.string()
//                } else if (errorBody != null) {
//                    responseString = errorBody.string()
//                }
//                //setLog("getResponseJSON", "RESPONSE : $responseString")
//                val jsonObject = JSONObject(responseString)
//                return jsonObject
//
//            } catch (e: JSONException) {
//                e.printStackTrace()
//                return getErrorResponse(e)
//            } catch (e: IOException) {
//                e.printStackTrace()
//                return getErrorResponse(e)
//            }
//
//        }
//
//        fun getResponseJSONArray(response: Response<ResponseBody>): JSONArray? {
//            var jsonArray: JSONArray? = null
//            try {
//                var responseString = ""
//                val body = response.body()
//                val errorBody = response.errorBody()
//
//                if (body != null) {
//                    responseString = body.string()
//                } else if (errorBody != null) {
//                    responseString = errorBody.string()
//                }
//                setLog("getResponseJSON", "RESPONSE : $responseString")
//                jsonArray = JSONArray(responseString)
////                return if (jsonObject.has(WSConstants.STATUS)) jsonObject else getErrorResponse(JSONException(""))
//                return jsonArray
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return jsonArray
//            }
//        }
//
//        fun getErrorResponse(exception: Exception): JSONObject? {
//            try {
//                val message: String
//                if (exception is IOException) {
//                    message = "Network error! Please try again later."
//
//                } else {
//                    message = "Something went wrong. Please try again later."
//
//                }
//                val jsonObject = JSONObject()
//                jsonObject.put(WSConstants.STATUS, WSConstants.STATUS_FAILURE)
//                jsonObject.put(WSConstants.MESSAGE, message)
//                setLog("getResponseJSON", "ERROR RESPONSE : " + jsonObject.toString())
//                return jsonObject
//            } catch (e: JSONException) {
//                e.printStackTrace()
//                return null
//            }
//
//        }
//
//        fun getRequestBodyFromJSON(jsonString: String): RequestBody {
//            return jsonString
//                .toRequestBody(WSConstants.WS_REQUEST_TYPE_JSON.toMediaTypeOrNull()!!)
//        }
//
////        fun getRequestBodyFromJSON(jsonString: String) = RequestBody.create(MediaType.parse(WSConstants.WS_REQUEST_TYPE_JSON)!!, jsonString)
//
//
//        fun checkResponseIsValid(responseJson: JSONObject): Boolean {
//            try {
//                if (responseJson.has(WSConstants.STATUS)) {
//                    val statusCode = responseJson.optInt(WSConstants.STATUS)
//                    return statusCode == 1
//                }
//                return false
//            } catch (ignore: Exception) {
//                return false
//            }
//
//        }
//
//        fun getHeader(): HashMap<String, String> {
//            val hashMap = HashMap<String, String>()
////            hashMap.put(
////                WSConstants.HEADER_AUTHORIZATION,
////                WSConstants.BEARER + SharedPrefHelper.getInstance().get(
////                    PrefConstant.TOKEN, ""
////                )
////            )
//            return hashMap
//        }
//
//        fun getHeaderURLEncoded(): HashMap<String, String> {
//            val hashMap = HashMap<String, String>()
//            hashMap.put(
//                WSConstants.WS_HEADER_CONTENT_TYPE,
//                WSConstants.WS_HEADER_CONTENT_TYPE_FORM_URLENCODED
//            )
//            return hashMap
//        }
//
//    }
//}