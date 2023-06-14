package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.ContinueWhereLeftModel
import com.hungama.music.data.model.HeadItemsItem
import com.hungama.music.data.webservice.repositories.RecentlyPlayRepos
import com.hungama.music.data.webservice.utils.Resource

class RecentlyPlayViewModel : ViewModel() {
    private var recentlyPlayRepos: RecentlyPlayRepos?=null

    fun getContinueWhereLeftList(context: Context, bottomTabID: Int, headItemsItem: HeadItemsItem?): MutableLiveData<Resource<ContinueWhereLeftModel>>? {
        recentlyPlayRepos=RecentlyPlayRepos()
        return recentlyPlayRepos?.getContinueWhereLeftList(context,bottomTabID,headItemsItem)
    }
}