package com.hungama.music.ui.main.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.data.model.CancelSubscriptionModel
import com.hungama.music.data.model.ContentOrderStatusModel
import com.hungama.music.data.model.RentedMovieRespModel
import com.hungama.music.data.model.UserSubscriptionModel
import com.hungama.music.data.webservice.repositories.UserSubscriptionRepos
import com.hungama.music.data.webservice.utils.Resource
import org.json.JSONObject

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Avatar all api
 */
class UserSubscriptionViewModel : ViewModel() {
    private var userSubscriptionRepos: UserSubscriptionRepos?=null

    /**
     * getUserSubscriptionStatusDetail API
     */
    fun getRentedMovie(context: Context): MutableLiveData<Resource<RentedMovieRespModel>>? {
        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }

        return userSubscriptionRepos?.getRentedMovieList(context)

    }

    /**
     * getUserSubscriptionStatusDetail API
     */
    fun getUserSubscriptionStatusDetail(context: Context): MutableLiveData<Resource<UserSubscriptionModel>>? {
        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }

        return userSubscriptionRepos?.getUserSubscriptionStatusDetail(context)

    }

    /**
     * getUserContentOrderStatusCheck API
     */
    fun getUserContentOrderStatusCheck(context: Context, contentId:String): MutableLiveData<Resource<ContentOrderStatusModel>>? {
        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }

        return userSubscriptionRepos?.getUserContentOrderStatusCheck(context,contentId)
    }

    /**
     * getPlanDetail API
     */
    fun getPlanDetail(context: Context): MutableLiveData<Resource<String>>? {

        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }

        return userSubscriptionRepos?.getPlanDetail(context)
    }

    /**
     * getPlanDetail API
     */
    fun getCancelPlan(
        context: Context,
        orderId: String
    ): MutableLiveData<Resource<CancelSubscriptionModel>>? {

        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }

        return userSubscriptionRepos?.getCancelPlan(context, orderId)
    }

    /**
     * getPlanDetail API
     */
    fun googleStoreNotifyBilling(
        context: Context,
        json: JSONObject
    ): MutableLiveData<Resource<CancelSubscriptionModel>>? {

        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }


        return userSubscriptionRepos?.googleStoreNotifyBilling(context, json)
    }

    /**
     * getUpdatePaymentStatus API
     */
    fun getUpdatePaymentStatus(context: Context){
        if (userSubscriptionRepos == null) {
            userSubscriptionRepos = UserSubscriptionRepos()
        }
        userSubscriptionRepos?.getUpdatePaymentStatus(context)


    }

}