package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.webservice.repositories.AlbumRepos
import com.hungama.music.data.webservice.utils.Resource

class AlbumViewModel : ViewModel() {

    private var albumRepos: AlbumRepos?=null

    /**
     * getAlbum list from API
     */
    fun getAlbumDetailList(context: Context, id:String): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        albumRepos=AlbumRepos()
        return albumRepos?.getAlbumDetailList(context,id)
    }

}