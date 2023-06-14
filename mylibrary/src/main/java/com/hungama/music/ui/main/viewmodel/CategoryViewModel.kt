package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.webservice.repositories.CategoryRepos
import com.hungama.music.data.webservice.utils.Resource

class CategoryViewModel : ViewModel() {

    private var categoryRepos:CategoryRepos?=null

    /**
     * getUserDetail list from API
     */
    fun getSelectedListData(context: Context, url: String): MutableLiveData<Resource<HomeModel>>? {
        categoryRepos= CategoryRepos()

        return categoryRepos?.getSelectedListData(context,url)
    }
}