package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.CollectionDetailModel
import com.hungama.music.data.webservice.repositories.CollectionRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class CollectionViewModel : ViewModel() {

    private var collectionRepos:CollectionRepos?=null

    /**
     * getCollectionDetail list from API
     */
    fun getCollectionDetail(context: Context, id:String): MutableLiveData<Resource<CollectionDetailModel>>? {
        collectionRepos = CollectionRepos()
        return collectionRepos?.getCollectionDetail(context, id)
    }

}