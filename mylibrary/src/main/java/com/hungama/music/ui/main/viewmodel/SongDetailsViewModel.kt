package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.SongDetailModel
import com.hungama.music.data.webservice.repositories.SongDetailRepos
import com.hungama.music.data.webservice.utils.Resource

class SongDetailsViewModel : ViewModel() {

    private var songDetailRepos: SongDetailRepos?=null

    /**
     * getVideoDetails from API
     */
    fun getSongDetail(context: Context, id: String): MutableLiveData<Resource<SongDetailModel>>? {

        songDetailRepos=SongDetailRepos()
        return songDetailRepos?.getSongDetail(context,id)

    }

}