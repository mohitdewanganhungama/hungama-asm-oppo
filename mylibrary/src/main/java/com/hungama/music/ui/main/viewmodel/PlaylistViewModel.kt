package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.*
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.repositories.PlaylistRepos
import com.hungama.music.data.webservice.utils.Resource
import org.json.JSONObject

class PlaylistViewModel : ViewModel() {

    private var playlistRepos: PlaylistRepos? = null

    fun getPlaylistDetailListDynamic(
        context: Context,
        id: String,
        queryParam: String?
    ): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getPlaylistDetailListDynamic(context, id,queryParam)
    }

    fun getPlaylistDetailList(
        context: Context,
        id: String
    ): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getPlaylistDetailList(context, id)
    }

    /**
     * getPlaylist list from API
     */
    fun getMyPlaylistDetailList(context: Context, id: String): MutableLiveData<Resource<PlaylistModel>>? {

        playlistRepos = PlaylistRepos()

        return playlistRepos?.getMyPlaylistDetailList(context, id)

    }

    fun getRecommendedContentListMyPlayList(context: Context, id1: String, id2: String, id3: String):
            MutableLiveData<Resource<PlaylistDynamicModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getRecommendedContentListMyPlayList(context, id1, id2, id3)
    }

    /**
     * getPlaylist list from API
     */
    fun getFollowPodcastList(context: Context): MutableLiveData<Resource<RecommendedSongListRespModel>>? {

        playlistRepos = PlaylistRepos()

        return playlistRepos?.getRecommendedList(context)
    }

    /**
     * getPlaylist list from API
     */
    fun getRecommendedSongList(context: Context, page:String): MutableLiveData<Resource<RecommendedSongListRespModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getRecommendedPlayList(context,page)

    }

    fun addSong(context: Context, jsonObject: JSONObject,playListId:String): MutableLiveData<Resource<BaseSuccessRespModel>>? {
        playlistRepos = PlaylistRepos()
        val url = WSConstants.METHOD_USER_UPDATE_STREAM + SharedPrefHelper.getInstance()
            .getUserId() + "/playlist/"+playListId+"/content"
        jsonObject.put("uid", SharedPrefHelper.getInstance().getUserId())
        return playlistRepos?.addSong(context,url,jsonObject.toString())
    }

    /**
     * deletePlaylist list from API
     */
    fun deleteMyPlaylist(context: Context, id:String): MutableLiveData<Resource<Boolean>>? {
        playlistRepos = PlaylistRepos()
        return playlistRepos?.deleteMyPlaylist(context,id)



    }

    /**
     * deletePlaylist list from API
     */
    fun deleteMyPlaylistContent(context: Context, contentId:String, playListId: String): MutableLiveData<Resource<Boolean>>? {
        playlistRepos = PlaylistRepos()
        return playlistRepos?.deleteMyPlaylistContent(context,contentId,playListId)


    }

    fun getRecommendedContentList(
        context: Context,
        id: String
    ): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getRecommendedContentList(context, id)
    }

    fun getBrandHubData(
        context: Context,
        deeplink: String
    ): MutableLiveData<Resource<PlaylistDynamicModel>>? {
        playlistRepos = PlaylistRepos()

        return playlistRepos?.getBrandHubData(context, deeplink)
    }

    fun updatePlaylistData(context: Context, jsonObject: JSONObject, playListId:String): MutableLiveData<Resource<UpdateMyPlaylistModel>>? {
        val url =
            WSConstants.METHOD_USER_PLAYLIST + "playlist/" + playListId
        playlistRepos = PlaylistRepos()

        return playlistRepos?.updatePlaylistData(context, url, jsonObject)
    }

    fun setPlaylistCountData(context: Context, playListId:String){
        val url =
            WSConstants.METHOD_USER_PLAYLIST + "playcount/playlist/" + playListId
        playlistRepos = PlaylistRepos()
        playlistRepos?.setPlaylistCountData(context, url)
    }

    fun updatePlaylistReorderedData(context: Context, jsonObject: JSONObject, playListId:String): MutableLiveData<Resource<BaseRespModel>>? {
        val url =
            WSConstants.METHOD_USER_PLAYLIST + SharedPrefHelper.getInstance()
                .getUserId() + "/playlist/"+playListId+"/shuffle"
        playlistRepos = PlaylistRepos()

        return playlistRepos?.updatePlaylistReorderedData(context, url, jsonObject)
    }
}

