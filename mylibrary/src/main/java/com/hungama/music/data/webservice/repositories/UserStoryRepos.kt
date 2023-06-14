package com.hungama.music.data.webservice.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.R
import com.hungama.music.data.model.UserStoryModel
import com.hungama.music.data.model.UserStoryReadPostModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserStoryRepos {
    private val TAG = javaClass.simpleName
    private var userStoryResp = MutableLiveData<Resource<UserStoryModel>>()
    private var userStoryReadPostResp = MutableLiveData<Resource<UserStoryReadPostModel>>()


    fun getUserStoryDataLatest(context: Context, url: String): MutableLiveData<Resource<UserStoryModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            userStoryResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (userStoryResp == null) {
                        userStoryResp = MutableLiveData<Resource<UserStoryModel>>()
                    }

                    try {
                        val userStoryModel = Gson().fromJson<UserStoryModel>(
                            response.toString(),
                            UserStoryModel::class.java
                        ) as UserStoryModel

                        userStoryResp.postValue(Resource.success(userStoryModel))

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        userStoryResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    userStoryResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return userStoryResp
    }

    fun postUserStoryReadData(context: Context, childStoryId: String): MutableLiveData<Resource<UserStoryReadPostModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject = JSONObject()
            jsonObject.put("storyId", childStoryId)
            val userId = SharedPrefHelper.getInstance().getUserId()
            val url = WSConstants.METHOD_USER_READ_STORY+"$userId/story"

            userStoryReadPostResp.postValue(Resource.loading(null))

            DataManager.getInstance(context)?.postVolleyRequest(context,url, jsonObject,object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (userStoryReadPostResp == null) {
                        userStoryReadPostResp = MutableLiveData<Resource<UserStoryReadPostModel>>()
                    }

                    try {
                        val userStoryModel = Gson().fromJson<UserStoryReadPostModel>(
                            response.toString(),
                            UserStoryReadPostModel::class.java
                        ) as UserStoryReadPostModel

                        userStoryReadPostResp.postValue(Resource.success(userStoryModel))

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        userStoryReadPostResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    userStoryReadPostResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })

        }

        return userStoryReadPostResp
    }
}