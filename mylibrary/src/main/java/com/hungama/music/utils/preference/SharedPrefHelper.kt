package com.hungama.music.utils.preference

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.model.LangItem
import com.hungama.music.utils.CommonUtils.deleteUserProfileDetails
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.APPMUSICLANG
import com.hungama.music.utils.Constant.APPVIDEOLANG
import com.hungama.music.utils.Constant.default_music_language_code
import com.hungama.music.utils.Constant.default_music_language_title
import com.hungama.music.utils.Constant.default_video_language_code
import com.hungama.music.utils.Constant.default_video_language_title
import com.hungama.music.utils.preference.PrefConstant.Companion.USER_LAST_SHIPPING_DETAILS
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class SharedPrefHelper {
    private val SHARED_PREFS_NAME = "preferences_application_configurations"//HungamaMusicApp.getInstance().packageName
    private var sharedPreferences: SharedPreferences?

    init {
        instance = this
        sharedPreferences =
            HungamaMusicApp.getInstance().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private var instance: SharedPrefHelper? = null
        fun getInstance(): SharedPrefHelper {
            if (instance == null) {
                instance = SharedPrefHelper()
            }
            return instance as SharedPrefHelper
        }

    }

    fun delete(key: String) {
        if (sharedPreferences?.contains(key) == true) {
            getEditor()?.remove(key)?.commit()
        }
    }

    fun save(key: String, value: Any?) {

        try {
            val editor = getEditor()
            if (value is Boolean) {
                editor?.putBoolean(key, (value as Boolean?)!!)
            } else if (value is Int) {
                editor?.putInt(key, (value as Int?)!!)
            } else if (value is Float) {
                editor?.putFloat(key, (value as Float?)!!)
            } else if (value is Long) {
                editor?.putLong(key, (value as Long?)!!)
            } else if (value is String) {
                editor?.putString(key, value as String?)
            } else if (value is Enum<*>) {
                editor?.putString(key, value.toString())
            }
            editor?.commit()
        }catch (exp: Exception){
            exp.printStackTrace()
        }

    }

    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences?.all?.get(key) as T
        return returnValue ?: defValue
    }

    /**
     * Save and get HashMap in SharedPreference
     */

    fun savePayUserDetail(key: String, obj: UserSubscriptionModel) {

        if (obj == null) {
            return
        }


        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(key, json)?.commit()


        setLog("getHashMap(key)", "getHashMap(key)->>" + getLanguageObject(key))
    }

    fun getPayUserDetail(key: String): UserSubscriptionModel? = runBlocking {

        val gson = Gson()

        val json = sharedPreferences?.getString(key, "")

        if(json!=null) {
            gson.fromJson(json, UserSubscriptionModel::class.java)
        }else{
            null
        }

    }

    /**
     * Save and get HashMap in SharedPreference
     */

    fun saveObjectUserCoin(key: String, obj: UserCoinDetailRespModel) {

        if (obj == null) {
            return
        }


        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(key, json)?.commit()


        setLog("getHashMap(key)", "getHashMap(key)->>" + getLanguageObject(key))
    }

    fun getObjectUserCoin(key: String): UserCoinDetailRespModel? {

        val gson = Gson()

        val json = sharedPreferences?.getString(key, "")

        if(json!=null) {
            return gson.fromJson(json, UserCoinDetailRespModel::class.java)
        }

        return null
    }

    /**
     * Save and get HashMap in SharedPreference
     */

    fun saveUserPlayBackSetting(key: String, obj: UserSettingRespModel) {

        if (obj == null) {
            return
        }


        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(key, json)?.commit()


        setLog("getHashMap(key)", "getHashMap(key)->>" + getLanguageObject(key))
    }


    fun getUserPlayBackSetting(key: String): UserSettingRespModel? {
        try {
            val gson = Gson()

            val json = sharedPreferences?.getString(key, "")

            if(json!=null) {
                return gson.fromJson(json, UserSettingRespModel::class.java)
            }
        }catch (e:Exception){
            setLog("Exception", "SharedPrefHelper-getUserPlayBackSetting-error-${e.message}")
        }


        return null
    }

    fun getPlayList(key: String) : ArrayList<LibraryAllRespModel.Row>? {
        try {
            val gson = Gson()

            val json = sharedPreferences?.getString(key, "")

            if(json!=null) {
                val jsonArray = JSONArray(json)
                val playListData : ArrayList<LibraryAllRespModel.Row> = ArrayList()
                for (itemPosition in 0 until jsonArray.length()){
                  playListData.add(gson.fromJson(jsonArray[itemPosition].toString(), LibraryAllRespModel.Row::class.java))
                }

                setLog("printPlayListData", " " + gson.toJson(playListData))


                return playListData
            }
        }catch (e:Exception){
            setLog("Exception", "SharedPrefHelper-getUserPlayBackSetting-error-${e.message}")
        }


        return null
    }

    fun savePlayList(key: String, rows: ArrayList<LibraryAllRespModel.Row>) {

        if (rows == null) {
            return
        }

        val gson = Gson()
        val json = gson.toJson(rows)
        getEditor()?.putString(key, json)?.commit()
    }


    /**
     * Save and get HashMap in SharedPreference
     */

    fun saveLanguageObject(key: String, obj: LangItem) {

        if (obj == null) {
            return
        }


        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(key, json)?.commit()


        setLog("getHashMap(key)", "getHashMap(key)->>" + getLanguageObject(key))
    }


    fun getLanguageObject(key: String): LangItem? {

        val gson = Gson()

        val json = sharedPreferences?.getString(key, "")

        if(json!=null) {
            return gson.fromJson(json, LangItem::class.java)
        }

        return null
    }

    private fun getEditor(): SharedPreferences.Editor? {
        return sharedPreferences?.edit()
    }

    fun getUserId(): String? {

        if(!TextUtils.isEmpty(sharedPreferences?.getString(PrefConstant.USER_ID, ""))){
            return sharedPreferences?.getString(PrefConstant.USER_ID, "")
        }else  if(!TextUtils.isEmpty(sharedPreferences?.getString(PrefConstant.SILENT_USER_ID, ""))){
            return sharedPreferences?.getString(PrefConstant.SILENT_USER_ID, "")
        }else{
            return ""

        }
    }
    fun isUserLoggedIn(): Boolean {
        return has(PrefConstant.ISLOGIN)
    }

    fun isUserGuestLogdIn(): Boolean {
        return sharedPreferences?.getBoolean(PrefConstant.ISGUEST, false)!!
    }

    fun logOut() {
        if (AccessToken.isCurrentAccessTokenActive()){
            LoginManager.getInstance().logOut()
        }
        try {
            Firebase.auth.signOut()
        }catch (e:Exception){

        }
        delete(PrefConstant.USER_ID)
        delete(PrefConstant.ISLOGIN)
        delete(PrefConstant.USER_PAY_DATA)
        delete(PrefConstant.USER_COIN)
        delete(PrefConstant.AF_COMPLETE_REGISTRATION)
        deleteUserProfileDetails()
    }


    fun has(key: String): Boolean {
        if(sharedPreferences!=null){
            return sharedPreferences!!.contains(key)
        }else return false
    }
    fun clearAllData() {
        val editor = getEditor()
        editor?.clear()?.commit()
    }


    fun getLanguage(): String? {

        return sharedPreferences?.getString(PrefConstant.PREFERENCE_LANGUAGE, Constant.EN)
    }

    fun setLanguage(language: String) {

        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_LANGUAGE, language)?.apply()
    }

    fun getLanguageTitle(): String? {

        return sharedPreferences?.getString(PrefConstant.PREFERENCE_LANGUAGE_TITLE, Constant.ENGLISH)
    }

    fun setLanguageTitle(language: String) {

        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_LANGUAGE_TITLE, language)?.apply()
    }

    fun setMusicLanguage(status:Boolean){
        sharedPreferences?.edit()?.putBoolean(PrefConstant.PREFERENCE_MUSIC_LANGUAGE, status)?.apply()
    }
    fun getMusicLanguage():Boolean{
        return sharedPreferences?.getBoolean(PrefConstant.PREFERENCE_MUSIC_LANGUAGE, false)!!
        //return false
    }

    fun setMusicArtist(status:Boolean){
        sharedPreferences?.edit()?.putBoolean(PrefConstant.PREFERENCE_MUSIC_ARTIST, status)?.apply()
    }
    fun getMusicArtist():Boolean{
        return sharedPreferences?.getBoolean(PrefConstant.PREFERENCE_MUSIC_ARTIST, false)!!
        //return false
    }

    fun setVideoLanguage(status:Boolean){
        sharedPreferences?.edit()?.putBoolean(PrefConstant.PREFERENCE_VIDEO_LANGUAGE, status)?.apply()
    }
    fun getVideoLanguage():Boolean{
        return sharedPreferences?.getBoolean(PrefConstant.PREFERENCE_VIDEO_LANGUAGE, false)!!
        //return false
    }

    fun setVideoGenre(status:Boolean){
        sharedPreferences?.edit()?.putBoolean(PrefConstant.PREFERENCE_VIDEO_GENRE, status)?.apply()
    }
    fun getVideoGenre():Boolean{
        return sharedPreferences?.getBoolean(PrefConstant.PREFERENCE_VIDEO_GENRE, false)!!
        //return false
    }

    fun getUsername(): String? {
        return sharedPreferences?.getString(PrefConstant.USER_NAME, "")
    }

    fun setMoodRadioMoodFilterTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MOOD_RADIO_MOOD_FILTER_TITLE, title)?.apply()
    }

    fun getMoodRadioMoodFilterTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MOOD_RADIO_MOOD_FILTER_TITLE, "Party")
    }

    fun setMoodRadioMoodFilterId(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_MOOD_RADIO_MOOD_FILTER_ID, id)?.apply()
    }

    fun getMoodRadioMoodFilterId(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_MOOD_RADIO_MOOD_FILTER_ID, 8)
    }

    fun setMoodRadioTempoFilterTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MOOD_RADIO_TEMPO_FILTER_TITLE, title)?.apply()
    }

    fun getMoodRadioTempoFilterTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MOOD_RADIO_TEMPO_FILTER_TITLE, "Auto")
    }

    fun setMoodRadioTempoFilterId(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_MOOD_RADIO_TEMPO_FILTER_ID, id)?.apply()
    }

    fun getMoodRadioTempoFilterId(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_MOOD_RADIO_TEMPO_FILTER_ID, 1)
    }

    fun setMoodRadioLanguageFilterTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_TITLE, title)?.apply()
    }

    fun getMoodRadioLanguageFilterTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_TITLE, "English")
    }


    fun setMoodRadioLanguageFilterId(id: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_ID, id)?.apply()
    }
    fun getMoodRadioLanguageFilterId(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_ID, "en")
    }

    fun setMoodRadioEraFilterMinRange(minRange: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_MOOD_RADIO_ERA_FILTER_MIN_RANGE, minRange)?.apply()
    }

    fun getMoodRadioEraFilterMinRange(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_MOOD_RADIO_ERA_FILTER_MIN_RANGE, 1950)
    }

    fun setMoodRadioEraFilterMaxRange(maxRange: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_MOOD_RADIO_ERA_FILTER_MAX_RANGE, maxRange)?.apply()
    }

    fun getMoodRadioEraFilterMaxRange(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_MOOD_RADIO_ERA_FILTER_MAX_RANGE, Calendar.getInstance().get(Calendar.YEAR))
    }

    fun setAppleClientSecret(secret: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_APPLE_CLIENT_SECRET, secret)?.apply()
    }

    fun getAppleClientSecret(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_APPLE_CLIENT_SECRET, "")
    }

    fun setAppleRefreshToken(token: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_APPLE_REFRESH_TOKEN, token)?.apply()
    }

    fun getAppleRefreshToken(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_APPLE_REFRESH_TOKEN, "")
    }

    fun setAppleVerifyRefreshToken(timer: Long) {
        sharedPreferences?.edit()?.putLong(PrefConstant.PREFERENCE_APPLE_VERIFY_REFRESH_TOKEN_TIMER, timer)?.apply()
    }

    fun getAppleVerifyRefreshToken(): Long? {
        return sharedPreferences?.getLong(PrefConstant.PREFERENCE_APPLE_VERIFY_REFRESH_TOKEN_TIMER, 0)
    }

    fun setMusicPlaybackStreamQualityId(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_ID, id)?.apply()
    }

    fun getMusicPlaybackStreamQualityId(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_ID, 1)
    }

    fun setMusicPlaybackStreamQualityTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_TITLE, title)?.apply()
    }

    fun getMusicPlaybackStreamQualityTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_TITLE, "Auto (Recommended)")
    }

    fun setMusicPlaybackDownloadQualityId(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_ID, id)?.apply()
    }

    fun getMusicPlaybackDownloadQualityId(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_ID, 1)
    }

    fun setMusicPlaybackDownloadQualityTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_TITLE, title)?.apply()
    }

    fun getMusicPlaybackDownloadQualityTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_TITLE, "Auto (Recommended)")
    }

    fun setUserSession(session: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_USER_SESSION, session)?.apply()
    }

    fun getUserSession(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_USER_SESSION, 0)
    }

    fun setUserCurrentSubscriptionPlan(planName: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_USER_CURRENT_SUBSCRIPTION_PLAN, planName)?.apply()
    }

    fun getUserCurrentSubscriptionPlan(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_USER_CURRENT_SUBSCRIPTION_PLAN, "Free User")
    }

    fun getHandleName(): String? {
        return sharedPreferences?.getString(PrefConstant.HANDLE_NAME, "")
    }
    fun getProfileImage(): String? {
        return sharedPreferences?.getString(PrefConstant.USER_IMAGE, "")
    }
    fun getUserEmail(): String? {
        return sharedPreferences?.getString(PrefConstant.USER_EMAIL, "")
    }
    fun getUserPhone(): String? {
        return sharedPreferences?.getString(PrefConstant.USER_MOBILE, "")
    }
    fun getUserFirstname(): String? {
        return sharedPreferences?.getString(PrefConstant.FIRST_NAME, "")
    }
    fun getUserLastname(): String? {
        return sharedPreferences?.getString(PrefConstant.LAST_NAME, "")
    }
    fun getUserGender(): String? {
        return sharedPreferences?.getString(PrefConstant.USER_GENDER, "")
    }
    fun getUserDob(): String? {
        return sharedPreferences?.getString(PrefConstant.USER_DOB, "")
    }

    fun setVideoPlaybackStreamQualityId(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_ID, id)?.apply()
    }

    fun getVideoPlaybackStreamQualityId(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_ID, 1)
    }

    fun setVideoPlaybackStreamQualityTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_TITLE, title)?.apply()
    }

    fun getVideoPlaybackStreamQualityTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_TITLE, "Auto (Recommended)")
    }

    fun setVideoPlaybackDownloadQualityId(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_ID, id)?.apply()
    }

    fun getVideoPlaybackDownloadQualityId(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_ID, 1)
    }

    fun setVideoPlaybackDownloadQualityTitle(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_TITLE, title)?.apply()
    }

    fun getVideoPlaybackDownloadQualityTitle(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_TITLE, "Auto (Recommended)")
    }

    fun clearSleepTimerData(){
        delete(PrefConstant.SLEEP_TIMER)
    }

    fun saveObjectProductList(key: String, obj: ProductRespModel) {

        if (obj == null) {
            return
        }


        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(key, json)?.commit()


        setLog("getHashMap(key)", "getHashMap(key)->>" + getLanguageObject(key))
    }

    fun getObjectProductList(key: String): ProductRespModel? {

        val gson = Gson()

        val json = sharedPreferences?.getString(key, "")

        if(json!=null) {
            return gson.fromJson(json, ProductRespModel::class.java)
        }

        return null
    }

    fun setMusicLanguageTitleList(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_MUSIC_TITLE_LIST, title)?.apply()
    }

    fun getMusicLanguageTitleList(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_MUSIC_TITLE_LIST, default_music_language_title)
    }

    fun setMusicLanguageCodeList(title: String) {
        sharedPreferences?.edit()?.putString(APPMUSICLANG, title)?.apply()
    }

    fun getMusicLanguageCodeList(): String? {
        return sharedPreferences?.getString(APPMUSICLANG, default_music_language_code)
    }

    fun setVideoLanguageTitleList(title: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_VIDEO_TITLE_LIST, title)?.apply()
    }

    fun getVideoLanguageTitleList(): String? {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_VIDEO_TITLE_LIST, default_video_language_title)
    }

    fun setVideoLanguageCodeList(title: String) {
        sharedPreferences?.edit()?.putString(APPVIDEOLANG, title)?.apply()
    }

    fun getVideoLanguageCodeList(): String? {
        return sharedPreferences?.getString(APPVIDEOLANG, default_video_language_code)
    }

    fun setUserAppInstallDate(date: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_USER_APP_INSTALL_DATE, date)?.apply()
    }

    fun getUserAppInstallDate():String {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_USER_APP_INSTALL_DATE, "").toString()
    }

    fun setLastConnectedDeviceName(name: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.PREFERENCE_LAST_CONNECTED_DEVICE_NAME, name)?.apply()
    }

    fun getLastConnectedDeviceName():String {
        return sharedPreferences?.getString(PrefConstant.PREFERENCE_LAST_CONNECTED_DEVICE_NAME, "This Phone").toString()
    }

    /**
     * Save user shopify orders
     */

    fun setUsersOrder(obj: UserOrdersModel) {
        if (obj == null) {
            return
        }


        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(PrefConstant.PREFERENCE_USER_ORDERS, json)?.commit()
        setLog("getUsersOrder", "getUsersOrder()->>" + getUserOrders())
    }

    fun getUserOrders(): UserOrdersModel {
        val gson = Gson()

        val json = sharedPreferences?.getString(PrefConstant.PREFERENCE_USER_ORDERS, null)

        if(json!=null) {
            return gson.fromJson(json, UserOrdersModel::class.java)
        }

        return UserOrdersModel()
    }

    fun setUserCensorRating(id: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.PREFERENCE_USER_CENSOR_RATING, id)?.apply()
    }

    fun getUserCensorRating(): Int? {
        return sharedPreferences?.getInt(PrefConstant.PREFERENCE_USER_CENSOR_RATING, 0)
    }

    fun setUserShopifyId(id: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.USER_SHOPIFY_ID, id)?.apply()
    }

    fun getUserShopifyId(): String {
        return sharedPreferences?.getString(PrefConstant.USER_SHOPIFY_ID, "0")!!
    }

    fun setDisplayDiscover(isDisplayDiscover: Boolean) {
        setLog("displayDiscover", "sharedPreferences-setDisplayDiscover-isDisplayDiscover-$isDisplayDiscover")
        sharedPreferences?.edit()?.putBoolean(PrefConstant.USER_IS_DISPLAY_DISCOVER, isDisplayDiscover)?.apply()
    }

    fun getDisplayDiscover(): Boolean {
//        return sharedPreferences?.getBoolean(PrefConstant.USER_IS_DISPLAY_DISCOVER, false)!!
        return false
    }

    fun setLastAudioContentPlayingStatus(isPause: Boolean) {
        setLog("displayDiscover", "sharedPreferences-setLastAudioContentPlayingStatus-isPause-$isPause")
        sharedPreferences?.edit()?.putBoolean(PrefConstant.IS_PAUSE, isPause)?.apply()
    }

    fun getLastAudioContentPlayingStatus(): Boolean {
        return sharedPreferences?.getBoolean(PrefConstant.IS_PAUSE, false)!!
    }

    fun setRecentSearchList(list: String) {
        sharedPreferences?.edit()?.putString(PrefConstant.RECENT_SEARCH_LIST, list)?.apply()
    }

    fun getRecentSearchList(): String {
        return sharedPreferences?.getString(PrefConstant.RECENT_SEARCH_LIST, "")!!
    }

    fun saveMap(key:String,inputMap: Map<Any, Any>) {
        if (sharedPreferences != null) {
            val jsonObject = JSONObject(inputMap)
            val jsonString = jsonObject.toString()
            sharedPreferences?.edit()?.remove(key)?.putString(key, jsonString)?.apply()
        }
    }

    fun loadMap(key:String): Map<Any, Any> {
        val outputMap: HashMap<Any, Any> = HashMap()
        try {
            if (sharedPreferences != null) {
                val jsonString = sharedPreferences?.getString(key, JSONObject().toString())
                if (jsonString != null) {
                    val jsonObject = JSONObject(jsonString)
                    val keysItr = jsonObject.keys()
                    while (keysItr.hasNext()) {
                        val keyOBJ = keysItr.next()
                        val value = jsonObject.get(keyOBJ)
                        outputMap[keyOBJ] = value
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return outputMap
    }

    fun setTotalDownloadedAudioContent(total: Int) {
        sharedPreferences?.edit()?.putInt(PrefConstant.TOTAL_DOWNLOADED_AUDIO_CONTENT, total)?.apply()
    }

    fun getTotalDownloadedAudioContent(): Int? {
        return sharedPreferences?.getInt(PrefConstant.TOTAL_DOWNLOADED_AUDIO_CONTENT, 0)
    }

    fun setUserLastShippingDetails(obj: ShippingDetailModel) {

        if (obj == null) {
            return
        }
        try {

        }catch (e:Exception){

        }
        val gson = Gson()
        val json = gson.toJson(obj)
        getEditor()?.putString(USER_LAST_SHIPPING_DETAILS, json)?.commit()
    }

    fun getUserLastShippingDetails(): ShippingDetailModel {
        try {
            val gson = Gson()
            val json = sharedPreferences?.getString(USER_LAST_SHIPPING_DETAILS, "")
            if(json!=null) {
                return gson.fromJson(json, ShippingDetailModel::class.java)
            }
        }catch (e:Exception){

        }

        return ShippingDetailModel()
    }

    fun setMobileNotificationEnable(status: Boolean) {
        sharedPreferences?.edit()?.putBoolean(PrefConstant.IS_Notification_ENABLE, status)?.apply()
    }

    fun getMobileNotificationEnable(): Boolean {
        return sharedPreferences?.getBoolean(PrefConstant.IS_Notification_ENABLE, true)!!
    }
}