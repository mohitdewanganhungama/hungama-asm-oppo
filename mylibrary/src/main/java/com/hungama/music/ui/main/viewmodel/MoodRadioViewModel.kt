package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.MoodRadioContentList
import com.hungama.music.data.model.MoodRadioFilterModel
import com.hungama.music.data.webservice.repositories.MoodRadioRepos
import com.hungama.music.data.webservice.utils.Resource

class MoodRadioViewModel : ViewModel() {

    private var moodRadioRepos: MoodRadioRepos?=null
    fun getMoodRadioMoodPopupList(context: Context): MutableLiveData<Resource<MoodRadioFilterModel>>? {
        moodRadioRepos=MoodRadioRepos()
        return moodRadioRepos?.getMoodRadioMoodPopupList(context)

    }
    fun getMoodRadioTempoPopupList(context: Context): MutableLiveData<Resource<MoodRadioFilterModel>>? {
        moodRadioRepos=MoodRadioRepos()
        return moodRadioRepos?.getMoodRadioTempoPopupList(context)


    }
    fun getMoodRadioLanguagePopupList(context: Context): MutableLiveData<Resource<MoodRadioFilterModel>>? {

        moodRadioRepos=MoodRadioRepos()
        return moodRadioRepos?.getMoodRadioLanguagePopupList(context)


    }
    fun getMoodRadioContentList(context: Context, moodId:Int, page:Int, size:Int, language:String, era:String, tempo:String): MutableLiveData<Resource<MoodRadioContentList>>? {
        moodRadioRepos=MoodRadioRepos()
        return moodRadioRepos?.getMoodRadioContentList(context,moodId,page,size,language,era,tempo)

    }


}