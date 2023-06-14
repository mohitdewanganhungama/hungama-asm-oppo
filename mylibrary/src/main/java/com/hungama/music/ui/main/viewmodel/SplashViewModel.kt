package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.GetStoreRespModel
import com.hungama.music.splash.SplashRespModel
import com.hungama.music.data.webservice.repositories.SplashRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class SplashViewModel : ViewModel() {
   private var splashResp:SplashRepos?=null

    /**
     * getUserDetail list from API
     */
    fun getSplashData(context: Context): MutableLiveData<Resource<SplashRespModel>>? {
        splashResp= SplashRepos()
        return splashResp?.getSplashData(context)
    }

    /**
     * get store detail from API
     */
    suspend fun getStoreData(context: Context): MutableLiveData<Resource<GetStoreRespModel>>? {
        splashResp= SplashRepos()
        return splashResp?.getStoreData(context)
    }
}