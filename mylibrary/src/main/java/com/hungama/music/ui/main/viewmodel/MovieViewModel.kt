package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.webservice.repositories.MovieRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class MovieViewModel : ViewModel() {

    private var movieRepos: MovieRepos?=null

    /**
     * getSimilarPodcast list from API
     */
    fun getMovieDetail(context: Context, id: String): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        movieRepos=MovieRepos()
        return movieRepos?.getMovieDetail(context,id)

    }


}