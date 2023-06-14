package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.GetStoreRespModel
import com.hungama.music.data.model.UserPreviewDetails
import com.hungama.music.data.webservice.repositories.SongDurationRepos
import com.hungama.music.splash.SplashRespModel
import com.hungama.music.data.webservice.repositories.SplashRepos
import com.hungama.music.data.webservice.utils.Resource
import org.json.JSONObject

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class SongDurationConfigViewModel : ViewModel() {
   private var songDurationResp: SongDurationRepos?=null

    /**
     * getUserDetail list from API
     */
    fun getUserPreviewDetails(context: Context, masAllowedDuration:String): MutableLiveData<Resource<UserPreviewDetails>>? {
        songDurationResp= SongDurationRepos()
        return songDurationResp?.getUserPreviewDetails(context, masAllowedDuration)
    }

    /**
     * get store detail from API
     */
    fun setUserPreviewDetails(context: Context, jsonData:JSONObject){
        songDurationResp= SongDurationRepos()
        songDurationResp?.setUserPreviewDetails(context, jsonData)
    }
}