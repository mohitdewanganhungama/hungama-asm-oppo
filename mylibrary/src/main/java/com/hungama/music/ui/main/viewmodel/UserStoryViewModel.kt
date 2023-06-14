package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.UserStoryModel
import com.hungama.music.data.model.UserStoryReadPostModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.repositories.UserStoryRepos
import com.hungama.music.data.webservice.utils.Resource

class UserStoryViewModel : ViewModel()  {
    private var userStoryRepos: UserStoryRepos?=null

    fun getUserStoryDataLatest(context: Context, id: String): MutableLiveData<Resource<UserStoryModel>>? {
        userStoryRepos= UserStoryRepos()
        val url = WSConstants.METHOD_DETAIL_CONTENT + id + "/story/detail"
        return userStoryRepos?.getUserStoryDataLatest(context,url)
    }

    fun postUserStory(context: Context, childStoryId: String):MutableLiveData<Resource<UserStoryReadPostModel>>?{
        userStoryRepos= UserStoryRepos()
        return userStoryRepos?.postUserStoryReadData(context, childStoryId)
    }
}