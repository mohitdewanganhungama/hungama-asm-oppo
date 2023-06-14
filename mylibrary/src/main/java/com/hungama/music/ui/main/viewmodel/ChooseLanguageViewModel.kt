package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.MusicLanguageSelectionModel
import com.hungama.music.model.LangRespModel
import com.hungama.music.data.webservice.repositories.AppLangRepos
import com.hungama.music.data.webservice.repositories.MusicLangRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 05/29/2021.
 * Purpose: User releated every API calling
 */
class ChooseLanguageViewModel : ViewModel() {

    private var appLangRepos: AppLangRepos?=null

    fun getLanguageData(context: Context): MutableLiveData<Resource<LangRespModel>>? {
        appLangRepos=AppLangRepos()
        return appLangRepos?.getLanguageData(context)

    }

    /**
     * getMusicLanguageList list from API
     */
    fun getMusicLanguageList(context: Context, isFromGenMusicSetting: Int): MutableLiveData<Resource<MusicLanguageSelectionModel>> {
        val musicLangRepos= MusicLangRepos()
        return musicLangRepos.getMusicLanguageList(context, isFromGenMusicSetting)
    }
}