package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.DownloadableContentModel
import com.hungama.music.data.model.MoodRadioListRespModel
import com.hungama.music.data.model.PlanInfoContentModel
import com.hungama.music.data.model.PlayableContentModel
import com.hungama.music.data.webservice.repositories.PlayableContentRepos
import com.hungama.music.data.webservice.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class PlayableContentViewModel : ViewModel() {

     var planInfoData = MutableLiveData<Resource<PlanInfoContentModel>>()
    private var playableContentRepos: PlayableContentRepos?=null
//    fun getPlayableContentUrl(context: Context, id:String,contentType:Int): MutableLiveData<Resource<PlayableContentModel>>? {
//        playableContentRepos=PlayableContentRepos()
//        return playableContentRepos?.getPlayableContentUrl(context,id,contentType)
//    }

    fun getPlayableContentList(context: Context, id:String): MutableLiveData<Resource<PlayableContentModel>>? {
        playableContentRepos=PlayableContentRepos()
        return playableContentRepos?.getPlayableContentList(context,id)

    }

    fun getMoodRadioList(context: Context, moodID:String): MutableLiveData<Resource<MoodRadioListRespModel>>? {
        playableContentRepos=PlayableContentRepos()
        return playableContentRepos?.getMoodRadioList(context,moodID)
    }

    fun getOnDemandRadioList(context: Context, ID:String): MutableLiveData<Resource<MoodRadioListRespModel>>? {
        playableContentRepos=PlayableContentRepos()
        return playableContentRepos?.getOnDemandRadioList(context,ID)
    }

    fun getArtistRadioList(context: Context, ID:String): MutableLiveData<Resource<MoodRadioListRespModel>>? {
        playableContentRepos=PlayableContentRepos()
        return playableContentRepos?.getArtistRadioList(context,ID)
    }

    fun getDownloadableContentUrl(context: Context, id:String,contentType:Int): MutableLiveData<Resource<DownloadableContentModel>>? {
        playableContentRepos=PlayableContentRepos()
        return playableContentRepos?.getDownloadableContentUrl(context,id,contentType)
    }

    fun getPlanInfo(context: Context,id:String){
        playableContentRepos= PlayableContentRepos()
         planInfoData = playableContentRepos?.getPlanInfo(context,id)!!

    }



}