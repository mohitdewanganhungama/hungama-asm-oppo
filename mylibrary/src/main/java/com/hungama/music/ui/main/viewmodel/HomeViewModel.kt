package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.DailyDoseRespModel
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.webservice.repositories.HomeRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: home all api
 */
class HomeViewModel : ViewModel() {
    private var homeRepos:HomeRepos?=null

    fun getHomeListDataLatest(context: Context,url: String): MutableLiveData<Resource<HomeModel>>? {
        homeRepos= HomeRepos()
        return homeRepos?.getHomeListDataLatest(context,url)
    }

    fun getCommonRecommendation(context: Context, url: String): MutableLiveData<Resource<DailyDoseRespModel>>? {
        homeRepos= HomeRepos()
        return homeRepos?.getCommonRecommendation(context,url)
    }

    fun getHomeBanner(context: Context,url: String): MutableLiveData<Resource<HomeModel>>? {
        homeRepos= HomeRepos()
        return homeRepos?.getHomeBanner(context,url)
    }

    fun getTrendingPodcastList(context: Context,url: String): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        homeRepos= HomeRepos()
        return homeRepos?.getTrendingPodcastList(context,url)
    }


}