package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.webservice.repositories.TVShowDetailRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class TVShowViewModel : ViewModel() {

    private var tvShowDetailRepos: TVShowDetailRepos?=null

    /**
     * getPodcast list from API
     */
    fun getTVShowDetailList(context: Context, id: String, type: String?): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        tvShowDetailRepos=TVShowDetailRepos()
        return tvShowDetailRepos?.getTVShowDetailList(context,id,type)
    }



}