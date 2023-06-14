package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.data.model.BaseSuccessRespModel
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.repositories.MusicRepos
import com.hungama.music.data.webservice.utils.Resource
import org.json.JSONObject
import java.util.*

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class MusicViewModel : ViewModel() {

    private var musicRepos: MusicRepos?=null
    /**
     * getUserDetail list from API
     */
    fun getMusicList(context: Context, url: String): MutableLiveData<Resource<HomeModel>>? {
        musicRepos=MusicRepos()
        return musicRepos?.getMusicList(context,url)
    }

    fun getSongLyricsList(context: Context, songLyricsUrl: String): MutableLiveData<Resource<String>>? {
        musicRepos=MusicRepos()
        return musicRepos?.getSongLyricsList(context,songLyricsUrl)
    }

    /**
     * getUserDetail list from API
     */
    fun updateUserAudioStream(context: Context, params: HashMap<String, String>): MutableLiveData<Resource<BaseSuccessRespModel>>? {
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

            musicRepos=MusicRepos()
            return musicRepos?.updateUserAudioStream(context,url,jsonObject?.toString()!!)

    }

    /**
     * getUserDetail list from API
     */
    fun updateRadioListeningStream(context: Context, jsonObject: JSONObject): MutableLiveData<Resource<BaseSuccessRespModel>>? {
            /**
             * event property
             */
            val url = WSConstants.METHOD_USER_UPDATE_STREAM + SharedPrefHelper.getInstance()
                .getUserId() + "/listening"

            musicRepos=MusicRepos()
            return musicRepos?.updateRadioListeningStream(context,url,jsonObject?.toString()!!)

    }
}