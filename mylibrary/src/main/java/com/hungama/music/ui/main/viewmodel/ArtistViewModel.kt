package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.ArtistModel
import com.hungama.music.data.model.LiveEventCountModel
import com.hungama.music.data.model.LiveEventDetailModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.webservice.repositories.ArtistRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class ArtistViewModel : ViewModel() {

    private var artistRepos:ArtistRepos?=null

    /**
     * getArtistDetail list from API
     */
    fun getArtistDetail(context: Context, id:String): MutableLiveData<Resource<PlaylistDynamicModel>> {
        if(artistRepos==null){
            artistRepos=ArtistRepos()
        }


        return artistRepos?.getArtistDetail(context,id)!!
    }

    /**
     * getLiveEventDetail list from API
     */
    fun getLiveEventDetail(context: Context, id:String, eventId:String): MutableLiveData<Resource<LiveEventDetailModel>> {
        if(artistRepos==null){
            artistRepos=ArtistRepos()
        }

        return artistRepos?.getLiveEventDetail(context,id,eventId)!!

    }

    /**
     * getLiveEventDetail list from API
     */
    fun getLiveEventCountDetail(context: Context, eventId:String): MutableLiveData<Resource<LiveEventCountModel>> {
        if(artistRepos==null){
            artistRepos=ArtistRepos()
        }

        return artistRepos?.getLiveEventCount(context,eventId)!!

    }
}