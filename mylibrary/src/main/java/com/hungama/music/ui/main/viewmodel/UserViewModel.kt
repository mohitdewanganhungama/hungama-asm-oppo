package com.hungama.music.ui.main.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.data.model.*
import com.hungama.music.utils.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.repositories.UserRepos
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.GameExitEvent
import com.hungama.music.utils.CommonUtils.setLog
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap
import androidx.lifecycle.MutableLiveData as MutableLiveData1

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 05/29/2021.
 * Purpose: User releated every API calling
 */
class UserViewModel : ViewModel() {

    private val TAG = javaClass.simpleName


    /**
     * Guset user regiser API
     */
    fun createPlayList(context: Context, json: JSONObject): androidx.lifecycle.MutableLiveData<Resource<CreatePlaylistRespModel>> {
        val url=WSConstants.METHOD_UPDATE_PREFERENCE+SharedPrefHelper.getInstance().getUserId()+"/playlist"
        val userRepos=UserRepos()
        json.put("uid", SharedPrefHelper.getInstance().getUserId())
        return userRepos.createPlayList(context,url,json.toString())
    }





    /**
     * Guset user regiser API
     */
    fun followUnfollowModule(context: Context, json: String): androidx.lifecycle.MutableLiveData<Resource<Boolean>> {
        var url = WSConstants.METHOD_USER_UPDATE_STREAM + SharedPrefHelper.getInstance()
            .getUserId() + "/content/follow"
        val userRepos=UserRepos()
        return userRepos.followUnfollowModule(context,url,json)
    }

    fun followUnfollowSocial(context: Context, json:String): androidx.lifecycle.MutableLiveData<Resource<Boolean>> {
        var url = WSConstants.METHOD_USER_UPDATE_STREAM + SharedPrefHelper.getInstance()
            .getUserId() + "/social"
        val userRepos=UserRepos()
        return userRepos.followUnfollowModule(context,url,json.toString())
    }

    fun followAll(context: Context, jsonObject:JSONObject): androidx.lifecycle.MutableLiveData<Resource<Boolean>> {
        var url = WSConstants.METHOD_USER_UPDATE_STREAM + SharedPrefHelper.getInstance()
            .getUserId() + "/social/bulk"
        val userRepos=UserRepos()
        return userRepos.followAll(context,url,jsonObject.toString())
    }

    fun getUserSettingType(context: Context, type: String): androidx.lifecycle.MutableLiveData<Resource<UserSettingRespModel>> {

        val userRepos=UserRepos()
        return userRepos.getUserSettingType(context,type)
    }

    fun saveUserPref(context: Context, json: String, type: String): androidx.lifecycle.MutableLiveData<Resource<Boolean>> {

        val url=WSConstants.METHOD_UPDATE_PREFERENCE+SharedPrefHelper.getInstance().getUserId()+"/preference"
        val userRepos=UserRepos()

        return userRepos.saveUserPref(context,url,json,type)
    }
    fun SSOmobileLogin(context: Context, json: String): androidx.lifecycle.MutableLiveData<Resource<SocialLoginRespModel>> {

        val jsonObject=JSONObject(json)

        setLog(
            TAG,
            "SSOmobileLogin json:${json}"
        )


        jsonObject.put("silent_user_id",SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,""))


        val mainJson = JSONObject()
        val clientDataJson = JSONObject()
        val username = JSONObject()
        if(jsonObject.has("mobNo")){
            username.put(
                "value",
                jsonObject?.optString("mobNo")
            )
            mainJson.put("process", "mobile_login")
        }else if(jsonObject.has("emailId")){
            username.put(
                "value",
                jsonObject?.optString("emailId")
            )

            mainJson.put("process", "email_wplogin")
        }
        clientDataJson.put("username", username)



        val silent_user_id = JSONObject()
        silent_user_id.put(
            "value",
            SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
        )
        clientDataJson.put("silent_user_id", silent_user_id)

        val uidJsonObject = JSONObject()
        uidJsonObject.put("value", SharedPrefHelper.getInstance().getUserId())
        clientDataJson.put("uid", uidJsonObject)




        mainJson.put("method", "signup_login")
        mainJson.put("client_data", clientDataJson)

        setLog(
            TAG,
            "SSOmobileLogin mainJson:${mainJson}"
        )

        val url= WSConstants.METHOD_SSO_LOGIN
        val userRepos=UserRepos()
        if(jsonObject.has("HE")){
            return userRepos.SSOmobileLogin(context,url,jsonObject.toString())
        }else{
            return userRepos.SSOmobileLogin(context,url,mainJson.toString())
        }


    }

    fun editProfile(context: Context, json:JSONObject): androidx.lifecycle.MutableLiveData<Resource<UserProfileModel>> {
        val url=WSConstants.METHOD_USER_BOOKMARK+SharedPrefHelper.getInstance().getUserId()+"/profile"
        val userRepos=UserRepos()
        return userRepos.editProfile(context,url,json.toString())
    }

    fun silentLogin(context: Context, json:String): MutableLiveData1<Resource<SocialLoginRespModel>> {
        var url= WSConstants.METHOD_SSO_LOGIN
        val userRepos=UserRepos()
        return userRepos.silentLogin(context,url,json)
    }

    fun socialLogin(context: Context, json:String): MutableLiveData1<Resource<SocialLoginRespModel>> {
        var url= WSConstants.METHOD_SSO_LOGIN
        val userRepos=UserRepos()
        return userRepos.socialLogin(context,url,json)
    }

    fun checkSSLUserExist(context: Context): MutableLiveData1<Resource<BaseRespModel>> {
        var url= WSConstants.METHOD_SSO_USER_EXIST+SharedPrefHelper.getInstance().getUserId()
        val userRepos=UserRepos()
        val mainJson = JSONObject()
        return userRepos.checkUserExist(context,url,mainJson.toString())
    }

    fun validateOtp(context: Context, uname: String,otp: String): MutableLiveData1<Resource<BaseRespModel>> {
        val mainJson = JSONObject()
        val clientDataJson = JSONObject()
        val username = JSONObject()
        username.put(
            "value",
            uname
        )
        clientDataJson.put("username", username)

        val password = JSONObject()
        password.put(
            "value",
            "12345"
        )
        clientDataJson.put("password", password)

        val confirm_password = JSONObject()
        confirm_password.put(
            "value",
            "12345"
        )
        clientDataJson.put("confirm_password", confirm_password)

        val pin = JSONObject()
        pin.put(
            "value",
            otp
        )
        clientDataJson.put("pin", pin)

        val is_site_uidJson = JSONObject()
        is_site_uidJson.put(
            "value",
            false
        )
        clientDataJson.put("is_site_uid", is_site_uidJson)

        mainJson.put("process", "signup_validation")
        mainJson.put("method", "signup_login")
        mainJson.put("client_data", clientDataJson)

        var url= WSConstants.METHOD_SSO_LOGIN
        val userRepos=UserRepos()
        return userRepos.validateOtp(context,url,mainJson.toString())
    }

    fun generateOTPNumber(context: Context, number: String, countryCode:String): MutableLiveData1<Resource<BaseRespModel>> {
        val mainJson = JSONObject()
        /*mainJson.put("mobNo", number)
        mainJson.put("priority", "1")
        mainJson.put("appCode", "un")
        mainJson.put("subject", "register")*/

        mainJson.put("appCode", "un")
        mainJson.put("mobileNo", number)
        mainJson.put("countryCode", countryCode)
        mainJson.put("messageId", "1")
        mainJson.put("emailId", " ")
        mainJson.put("subject", "Register")
        mainJson.put("priority", "1")

        var url=WSConstants.METHOD_SSO_MAIN + "otp"
        val userRepos=UserRepos()
        return userRepos.generateOtp(context,url,mainJson.toString())
    }

    fun verifyOTPNumber(context: Context, number: String,otp: String, countryCode:String): MutableLiveData1<Resource<BaseRespModel>> {
        val mainJson = JSONObject()
        /*mainJson.put("mobNo", number)
        mainJson.put("otp", otp)
        mainJson.put("priority", "1")
        mainJson.put("appCode", "un")
        mainJson.put("subject", "verify")*/

        mainJson.put("appCode", "un")
        mainJson.put("mobileNo", number)
        mainJson.put("countryCode", countryCode)
        mainJson.put("emailId", " ")
        mainJson.put("otp", otp)
        mainJson.put("priority", "1")
        mainJson.put("subject", "verify")

        var url=WSConstants.METHOD_SSO_MAIN + "otp/verify"
        val userRepos=UserRepos()
        return userRepos.verifyOTPNumber(context,url,mainJson.toString())
    }

    fun generateOTPEmail(context: Context, email: String): MutableLiveData1<Resource<BaseRespModel>> {
        val mainJson = JSONObject()
        /*mainJson.put("emailIds", email)
        mainJson.put("priority", "1")
        mainJson.put("appCode", "un")
        mainJson.put("subject", "Register")*/

        mainJson.put("appCode", "un")
        mainJson.put("mobileNo", " ")
        mainJson.put("countryCode", " ")
        mainJson.put("messageId", "1")
        mainJson.put("emailId", email)
        mainJson.put("subject", "Register")
        mainJson.put("priority", "1")

        var url=WSConstants.METHOD_SSO_MAIN+"otp"
        val userRepos=UserRepos()
        return userRepos.generateOTPEmail(context,url,mainJson.toString())
    }

    fun verifyOTPEmail(context: Context, email: String, otp: String): MutableLiveData1<Resource<BaseRespModel>> {
        val mainJson = JSONObject()

        /*mainJson.put("emailId", email)
        mainJson.put("otp", otp)
        mainJson.put("priority", "1")
        mainJson.put("appCode", "un")
        mainJson.put("subject", "verify")*/

        mainJson.put("appCode", "un")
//        mainJson.put("mobileNo", " ")
//        mainJson.put("countryCode", " ")
        mainJson.put("email", email)
        mainJson.put("otp", otp)
        mainJson.put("priority", "1")
        mainJson.put("subject", "verify")

        var url=WSConstants.METHOD_SSO_MAIN+"otp/verify"
        val userRepos=UserRepos()
        return userRepos.verifyOTPEmail(context,url,mainJson.toString())
    }
    fun getCountryData(context: Context): MutableLiveData1<Resource<CountryDataModel>> {
        val userRepos=UserRepos()
        return userRepos.getCountryData(context)
    }

    fun getUserSocialData(context: Context, userId: String): MutableLiveData1<Resource<UserSocialData>> {
        val userRepos=UserRepos()
        return userRepos.getUserSocialData(context,userId!!)
    }

    fun getUserPlaylistData(context: Context, userId: String): MutableLiveData1<Resource<PlaylistRespModel>> {
        val userRepos=UserRepos()
        return userRepos.getUserPlaylistData(context,userId!!)
    }

    fun multipleBookmarkApi(context: Context,json: String):MutableLiveData1<Resource<Boolean>>{
        val url = WSConstants.METHOD_USER_BOOKMARK+SharedPrefHelper.getInstance().getUserId()+"/bookmark"
        val userRepos = UserRepos()
        return userRepos.MultipleBookmarkApi(context,url,json)
    }

    fun callBookmarkApi(context: Context, json: String): MutableLiveData1<Resource<Boolean>> {
        val url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance().getUserId() + "/bookmark"
        setLog(TAG, "syncFbList: req:"+json)

        val userRepos=UserRepos()
        return userRepos.callBookmarkApi(context,url,json)
    }


    fun getFollwingWithFilter(context: Context, module:Int, typeQuery:String? = null): MutableLiveData1<Resource<BookmarkDataModel>> {
        val userRepos=UserRepos()
        return userRepos.getFollwingWithFilter(context,module, typeQuery!!)
    }

    fun getUserBookmarkedDataWithFilter(context: Context, module:Int, typeQuery:String? = null): MutableLiveData1<Resource<BookmarkDataModel>> {
        val userRepos=UserRepos()
        return userRepos.getUserBookmarkedDataWithFilter(context,module, typeQuery!!)
    }

    fun getBookmarkCountTypeWise(context: Context, module:Int, typeId:String? = null): MutableLiveData1<Resource<BookmarkCountRespModel>> {
        val userRepos=UserRepos()
        return userRepos.getBookmarkCountTypeWise(context,module, typeId)
    }

    fun getUserBookmarkedData(context: Context, module:Int, typeId:String? = null): MutableLiveData1<Resource<BookmarkDataModel>> {
        val userRepos=UserRepos()
        return userRepos.getUserBookmarkedData(context,""+module, typeId)
    }


    fun getUserProfileData(context: Context, userId: String): MutableLiveData1<Resource<UserProfileModel>> {
        val userRepos=UserRepos()
        return userRepos.getUserProfileData(context,userId)
    }

    fun syncContactList(context: Context, json: JSONObject): MutableLiveData1<Resource<ContactListModel>> {

            var url=WSConstants.METHOD_USER_CONTACT_LIST_SYNC +SharedPrefHelper?.getInstance()?.getUserId()+"/social/connect/phone"

            setLog(TAG, "syncFbList: req:"+json)

            val userRepos=UserRepos()
            return userRepos.syncContactList(context,url,json.toString())

    }

    fun syncFbList(context: Context, json: JSONObject): MutableLiveData1<Resource<FacebookListModel>> {

            setLog(TAG, "syncFbList: req:"+json)

            var url=WSConstants.METHOD_USER_FACEBOOK_LIST_SYNC +SharedPrefHelper?.getInstance()?.getUserId()+"/social/connect/fb"


            val userRepos=UserRepos()
            return userRepos.syncFbList(context,url,json.toString())


    }

    fun getLibraryAllData(context: Context): MutableLiveData1<Resource<LibraryAllRespModel>> {
            val json=JSONObject()
            json.put("playlist",true)
            json.put("favourite",true)
            json.put("follow",true)

            val favArrayType=JSONArray()
            favArrayType.put("55555")
            favArrayType.put("1")
            favArrayType.put("34")
            favArrayType.put("77777")
            json.put("favouriteType",favArrayType)

            val followType=JSONArray()
            followType.put("0")
            followType.put("109")
            json.put("followType",followType)


            val url=WSConstants.METHOD_LIB_MUSIC_ALL+SharedPrefHelper?.getInstance()?.getUserId()+"/library"
        setLog(TAG, "getLibraryAllData: url "+url)

            val userRepos=UserRepos()
            return userRepos.getLibraryAllData(context,url,json.toString())

    }

    fun deleteAccount(context: Context): MutableLiveData1<Resource<BaseRespModel>> {

        var url= WSConstants.METHOD_USER_ACTION+"?action=post_delete&device_id="+Utils.getDeviceId(context)
        val userRepos=UserRepos()
        return userRepos.deleteUserAccount(context,url)
    }

    fun getHEApiCall(context: Context, heApi_url: String): MutableLiveData1<Resource<HERespModel>> {
        val userRepos=UserRepos()
        return userRepos.getHEApiCall(context,heApi_url)
    }

    fun getRecentSearchCall(context: Context): MutableLiveData1<Resource<RecentSearchRespModel>> {
        val userRepos=UserRepos()
        return userRepos.getRecentSearch(context)
    }

    fun deleteRecentSearchCall(context: Context): MutableLiveData1<Resource<DeleteRecentSearchModel>> {
        val userRepos=UserRepos()
        return userRepos.deleteRecentSearch(context)
    }

    fun addRecentSearch(context: Context, json: JSONObject): MutableLiveData1<Resource<BaseSuccessRespModel>> {

        Log.d(TAG, "addRecentSearch: req:"+json)

        var url=WSConstants.METHOD_RECENT_SEARCH


        val userRepos=UserRepos()
        return userRepos.addRecentSearch(context,url,json)


    }

    fun logout_silent_user(context: Context): MutableLiveData1<Resource<SocialLoginRespModel>> {
        val userRepos=UserRepos()
        return userRepos.logout_silent_user(context)
    }
}