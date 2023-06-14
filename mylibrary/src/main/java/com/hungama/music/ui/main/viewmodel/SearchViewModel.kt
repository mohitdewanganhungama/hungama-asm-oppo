package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.SearchRecommendationModel
import com.hungama.music.data.model.SearchRespModel
import com.hungama.music.data.model.SuggestionRespModel
import com.hungama.music.data.webservice.repositories.SearchRepos
import com.hungama.music.data.webservice.utils.Resource

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class SearchViewModel : ViewModel() {
       private var searchRepos:SearchRepos?=null
    /**
     * getUserDetail list from API
     */
    fun getRecommendation(context: Context): MutableLiveData<Resource<SearchRecommendationModel>>? {

        if(searchRepos==null){
            searchRepos=SearchRepos()
        }


        return searchRepos?.getRecommendationDataList(context)

    }

    /**
     * getUserDetail list from API
     */
    fun getSearchData(context: Context,searchText:String): MutableLiveData<Resource<SearchRespModel>>? {

        if(searchRepos==null){
            searchRepos=SearchRepos()
        }

        return searchRepos?.getSearchData(context,searchText)

    }

    /**
     * getUserDetail list from API
     */
    fun getAllSearchWithPageWiseData(context: Context,searchText:String,pageNo:Int): MutableLiveData<Resource<SearchRespModel>>? {

        if(searchRepos==null){
            searchRepos=SearchRepos()
        }

        return searchRepos?.getAllSearchWithPageWiseData(context,searchText,pageNo)

    }

    /**
     * getUserDetail list from API
     */
    fun getSuggestionData(context: Context,searchText:String): MutableLiveData<Resource<SuggestionRespModel>>? {

        if(searchRepos==null){
            searchRepos=SearchRepos()
        }

        return searchRepos?.getSuggestionData(context,searchText)

    }

}