package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.model.PodcastDetailsRespModel
import com.hungama.music.data.webservice.repositories.PodcastRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class PodcastViewModel : ViewModel() {
    private val TAG = javaClass.simpleName
    var podcastDetailListResp = MutableLiveData<PodcastDetailsRespModel>()
    var podcastEpisodeListResp = MutableLiveData<PodcastDetailsRespModel>()

    val isViewLoading = MutableLiveData<Boolean>()
    val onMessageError = MutableLiveData<String>()
    val isEmptyList = MutableLiveData<Boolean>()

    var bucketRespModel: HomeModel?=null

    private var podcastRepos:PodcastRepos?=null

    /**
     * getPodcast list from API
     */
    fun getPodcastDetailList(context: Context, id:String): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        podcastRepos=PodcastRepos()
        return podcastRepos?.getPodcastDetailList(context,id)
    }

    /**
     * getPodcast list from API
     */
    fun getPodcastEpisodeList(context: Context, id:String, page:Int): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        podcastRepos=PodcastRepos()
        return podcastRepos?.getPodcastEpisodeList(context,id, page)
    }


}