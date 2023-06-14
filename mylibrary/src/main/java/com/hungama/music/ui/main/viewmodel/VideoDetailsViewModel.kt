package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.model.VideoDetailModel
import com.hungama.music.data.webservice.repositories.VideoDetailRepos
import com.hungama.music.data.webservice.utils.Resource

class VideoDetailsViewModel : ViewModel() {

    private var videoDetailRepos:VideoDetailRepos?=null
    /**
     * getVideoDetails from API
     */
    fun getVideoDetail(context: Context, id: String): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        videoDetailRepos=VideoDetailRepos()

        return videoDetailRepos?.getVideoDetail(context,id)

    }

}