package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.RecommendedSongListRespModel
import com.hungama.music.data.webservice.repositories.PlaylistRepos
import com.hungama.music.data.webservice.utils.Resource

class SimilarSongViewModel : ViewModel() {

    private var playlistRepos: PlaylistRepos? = null

    /**
     * getSimilarSong list from API
     */
    fun getSimilarSongList(context: Context, contentId:String): MutableLiveData<Resource<RecommendedSongListRespModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getSimilarSongList(context, contentId)

    }
}