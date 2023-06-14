package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.data.model.BaseSuccessRespModel
import com.hungama.music.data.model.MusicLanguageSelectionModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.repositories.MusicLangRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class MusicLanguageViewModel : ViewModel() {

    /**
     * getMusicLanguageList list from API
     */
    fun saveUserPref(context: Context,json: String): MutableLiveData<Resource<BaseSuccessRespModel>> {
        val musicLangRepos= MusicLangRepos()
        val url=WSConstants.METHOD_UPDATE_PREFERENCE+SharedPrefHelper.getInstance().getUserId()+"/preference"
        return musicLangRepos.saveUserPref(context,url,json)

    }

    /**
     * getMusicLanguageList list from API
     */
    fun getMusicLanguageList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        val musicLangRepos= MusicLangRepos()
        return musicLangRepos.getMusicLanguageList(context)
    }


    /**
     * getMusicArtistList list from API
     */
    fun getMusicArtistList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        val musicLangRepos= MusicLangRepos()
        return musicLangRepos.getMusicArtistList(context)
    }

    /**
     * getVideoLanguageList list from API
     */
    fun getVideoLanguageList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        val musicLangRepos= MusicLangRepos()
        return musicLangRepos.getVideoLanguageList(context)

    }

    /**
     * getVideoGenreList list from API
     */
    fun getVideoGenreList(context: Context): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        val musicLangRepos= MusicLangRepos()
        return musicLangRepos.getVideoGenreList(context)

    }

}