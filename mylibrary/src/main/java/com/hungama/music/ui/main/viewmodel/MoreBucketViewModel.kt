package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.MoreBucketDataModel
import com.hungama.music.data.webservice.repositories.MoreBucketRepos
import com.hungama.music.data.webservice.utils.Resource

class MoreBucketViewModel : ViewModel() {

    /**
     * getMoreBucketListData list from API
     */
    fun getMoreBucketListData(context: Context, bucketId:String, pageNo:Int, query:String, itemSize:Int, type: Int?): MutableLiveData<Resource<MoreBucketDataModel>> {
        var moreRepo = MoreBucketRepos()
        return moreRepo.getMoreBucketListData(context,bucketId,pageNo,query,itemSize,type)

    }

}