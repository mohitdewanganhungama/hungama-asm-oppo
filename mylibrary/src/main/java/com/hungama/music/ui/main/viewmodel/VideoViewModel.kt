package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.repositories.VideoRepos
import com.hungama.music.data.webservice.utils.Resource
import org.json.JSONObject
import java.util.*

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class VideoViewModel : ViewModel() {
    private var videoRepos: VideoRepos? = null

    /**
     * getUserDetail list from API
     */
    fun getWatchVidelList(context: Context, url: String): MutableLiveData<Resource<HomeModel>>? {

        videoRepos = VideoRepos()
        return videoRepos?.getWatchVidelList(context, url)

    }

    /**
     * getUserDetail list from API
     */
    fun getVideoList(
        context: Context,
        selectedContentId: String,
        contentType:Int
    ): MutableLiveData<Resource<PlayableContentModel>>? {
        videoRepos = VideoRepos()
        return videoRepos?.getVideoList(context, selectedContentId, contentType)
    }

    /**
     * getUserDetail list from API
     */
    fun updateUserVideoStream(
        context: Context,
        params: HashMap<String, String>
    ): MutableLiveData<Resource<BaseSuccessRespModel>>? {
        /**
         * event property
         */
        val url = WSConstants.METHOD_USER_UPDATE_STREAM + SharedPrefHelper.getInstance()
            .getUserId() + "/stream"

        val jsonObject: JSONObject?
        jsonObject = if (params != null) {
            JSONObject(params as Map<*, *>)
        } else {
            null
        }

        videoRepos = VideoRepos()
        return videoRepos?.updateUserAudioStream(context, url, jsonObject?.toString()!!)

    }

}