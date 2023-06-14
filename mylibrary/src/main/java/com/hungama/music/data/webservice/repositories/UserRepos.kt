package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.WSConstants.METHOD_FETCH_USER_COINS
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.PLATFORM_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class UserRepos {
    private val TAG = javaClass.simpleName
    private var socialLoginResp = MutableLiveData<Resource<SocialLoginRespModel>>()
    private var deleteUserAccountResp = MutableLiveData<Resource<BaseRespModel>>()
    private var genrateOTPResp = MutableLiveData<Resource<BaseRespModel>>()
    private var checkUserExistResp = MutableLiveData<Resource<BaseRespModel>>()
    private var editProfileResp = MutableLiveData<Resource<UserProfileModel>>()
    private var countryDataResp = MutableLiveData<Resource<CountryDataModel>>()
    private var telcoHEDataResp = MutableLiveData<Resource<HERespModel>>()
    private var userPrefSettingResp = MutableLiveData<Resource<UserSettingRespModel>>()
    private var userSocialResp = MutableLiveData<Resource<UserSocialData>>()
    private var userPlaylistResp = MutableLiveData<Resource<PlaylistRespModel>>()
    private var userBookmarkResp = MutableLiveData<Resource<BookmarkDataModel>>()
    private var userBookmarkCountResp = MutableLiveData<Resource<BookmarkCountRespModel>>()
    private var userCoinDetailResp = MutableLiveData<Resource<UserCoinDetailRespModel>>()
    private var userContactResp = MutableLiveData<Resource<ContactListModel>>()
    private var userFbResp = MutableLiveData<Resource<FacebookListModel>>()
    private var createPlaylistResp = MutableLiveData<Resource<CreatePlaylistRespModel>>()
    private var addRecentSearchResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
    private var getRecentSearchResp = MutableLiveData<Resource<RecentSearchRespModel>>()
    private var deleteRecentSearchResp = MutableLiveData<Resource<DeleteRecentSearchModel>>()
    private var libraryMusicAllResp = MutableLiveData<Resource<LibraryAllRespModel>>()
    private var saveUserPrefSettingResp = MutableLiveData<Resource<Boolean>>()


    fun createPlayList(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<CreatePlaylistRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            createPlaylistResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (createPlaylistResp == null) {
                            createPlaylistResp =
                                MutableLiveData<Resource<CreatePlaylistRespModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<BaseSuccessRespModel>(
                                response.toString(),
                                CreatePlaylistRespModel::class.java
                            ) as CreatePlaylistRespModel

                            createPlaylistResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_created_playlist")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("user_created_playlist").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("user_created_playlist").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            createPlaylistResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        createPlaylistResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return createPlaylistResp
    }


    fun followUnfollowModule(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<Boolean>> {

        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            saveUserPrefSettingResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (saveUserPrefSettingResp == null) {
                            saveUserPrefSettingResp = MutableLiveData<Resource<Boolean>>()
                        }

                        try {

                            saveUserPrefSettingResp.postValue(Resource.success(true))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav_listing")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("followfav_listing").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("followfav_listing").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            saveUserPrefSettingResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        saveUserPrefSettingResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return saveUserPrefSettingResp
    }

    fun followAll(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<Boolean>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            saveUserPrefSettingResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (saveUserPrefSettingResp == null) {
                            saveUserPrefSettingResp = MutableLiveData<Resource<Boolean>>()
                        }

                        try {

                            saveUserPrefSettingResp.postValue(Resource.success(true))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav_listing")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("social/bulk").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("social/bulk").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            saveUserPrefSettingResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        saveUserPrefSettingResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return saveUserPrefSettingResp
    }

    fun getUserSettingType(
        context: Context,
        type: String
    ): MutableLiveData<Resource<UserSettingRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            val url = WSConstants.METHOD_UPDATE_PREFERENCE + SharedPrefHelper.getInstance()
                .getUserId() + "/preference/type/" + type

            var requestTime = DateUtils.getCurrentDateTime()
            userPrefSettingResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userPrefSettingResp == null) {
                            userPrefSettingResp = MutableLiveData<Resource<UserSettingRespModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<UserSettingRespModel>(
                                response.toString(),
                                UserSettingRespModel::class.java
                            ) as UserSettingRespModel
                            SharedPrefHelper.getInstance().saveUserPlayBackSetting(type, dataModel)
                            userPrefSettingResp.postValue(Resource.success(dataModel))


                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, type)
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(type).sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(type).sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userPrefSettingResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        userPrefSettingResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return userPrefSettingResp
    }

    fun saveUserPref(
        context: Context,
        url: String,
        json: String,
        type: String
    ): MutableLiveData<Resource<Boolean>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            saveUserPrefSettingResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (saveUserPrefSettingResp == null) {
                            saveUserPrefSettingResp = MutableLiveData<Resource<Boolean>>()
                        }

                        try {

                            saveUserPrefSettingResp.postValue(Resource.success(true))
                            getUserSettingType(context, type)
                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "general_setting_details")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("general_setting_details").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("general_setting_details").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            saveUserPrefSettingResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        saveUserPrefSettingResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return saveUserPrefSettingResp
    }

    fun SSOmobileLogin(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<SocialLoginRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "SSOmobileLogin url:${url} json:${json}")


            var requestTime = DateUtils.getCurrentDateTime()
            socialLoginResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (socialLoginResp == null) {
                            socialLoginResp = MutableLiveData<Resource<SocialLoginRespModel>>()
                        }

                        try {
                            val socialLoginRespModel = Gson().fromJson<SocialLoginRespModel>(
                                response.toString(),
                                SocialLoginRespModel::class.java
                            ) as SocialLoginRespModel
                            socialLoginResp.postValue(Resource.success(socialLoginRespModel))


                            setLog(
                                TAG,
                                "socialLoginRespModel newUser:${Gson().toJson(socialLoginRespModel)}"
                            )

                            SharedPrefHelper.getInstance()
                                .save(
                                    PrefConstant.USER_ID,
                                    socialLoginRespModel?.result?.data?.userId
                                )

                            if (!TextUtils.isEmpty(socialLoginRespModel?.result?.data?.userName)) {
                                SharedPrefHelper.getInstance().save(
                                    PrefConstant.USER_NAME,
                                    socialLoginRespModel?.result?.data?.userName
                                )
                            }

                            SharedPrefHelper.getInstance().save(PrefConstant.ISLOGIN, true)

                            val userDataMap = HashMap<String, String>()
                            userDataMap.put(
                                EventConstant.HUNGAMA_ID,
                                SharedPrefHelper.getInstance().getUserId()!!
                            )
                            userDataMap.put(
                                EventConstant.MOREANONYMOUS_ID,
                                "" + SharedPrefHelper.getInstance().get(
                                    PrefConstant.SILENT_USER_ID, ""
                                )
                            )
                            EventManager.getInstance()
                                .sendUserAttribute(UserAttributeEvent(userDataMap))

                            if (JSONObject(json)?.has("mobNo")!!) {
                                SharedPrefHelper.getInstance()
                                    .save(
                                        PrefConstant.USER_MOBILE,
                                        JSONObject(json)?.optString("mobNo")
                                    )

                                if (socialLoginRespModel?.result?.data?.newUser != null && !TextUtils.isEmpty(
                                        socialLoginRespModel?.result?.data?.newUser
                                    ) && socialLoginRespModel?.result?.data?.newUser?.contains(
                                        "true",
                                        true
                                    )!!
                                ) {
                                    setLog(TAG, "setJsonDataResponse: registerUserMethod_AF:Mobile")
                                    Utils.registerUserMethod_AF("Mobile")
                                }

                            } else if (JSONObject(json)?.has("emailId")!!) {
                                SharedPrefHelper.getInstance()
                                    .save(
                                        PrefConstant.USER_EMAIL,
                                        JSONObject(json)?.optString("emailId")
                                    )

                                if (socialLoginRespModel?.result?.data?.newUser != null && !TextUtils.isEmpty(
                                        socialLoginRespModel?.result?.data?.newUser
                                    ) && socialLoginRespModel?.result?.data?.newUser?.contains(
                                        "true",
                                        true
                                    )!!
                                ) {
                                    setLog(TAG, "setJsonDataResponse: registerUserMethod_AF:Email")
                                    Utils.registerUserMethod_AF("Email")

                                }


                            }


                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "webservice/music")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("webservice/music").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("webservice/music").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))


                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            socialLoginResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        socialLoginResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return socialLoginResp
    }

    fun editProfile(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<UserProfileModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            val requestTime = DateUtils.getCurrentDateTime()
            editProfileResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (editProfileResp == null) {
                            editProfileResp = MutableLiveData<Resource<UserProfileModel>>()
                        }

                        try {
                            val profileModel = Gson().fromJson<UserProfileModel>(
                                response.toString(),
                                UserProfileModel::class.java
                            ) as UserProfileModel
                            editProfileResp.postValue(Resource.success(profileModel))


                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "editProfile")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("editProfile").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("editProfile").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            editProfileResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        editProfileResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return editProfileResp
    }

    var retryCount = 0
    fun silentLogin(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<SocialLoginRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            socialLoginResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (socialLoginResp == null) {
                            socialLoginResp = MutableLiveData<Resource<SocialLoginRespModel>>()
                        }

                        try {

                            val socialLoginRespModel = Gson().fromJson<SocialLoginRespModel>(
                                response.toString(),
                                SocialLoginRespModel::class.java
                            ) as SocialLoginRespModel
                            socialLoginResp.postValue(Resource.success(socialLoginRespModel))

                            SharedPrefHelper.getInstance().save(PrefConstant.SILENT_USER_ID,
                                socialLoginRespModel?.result?.data?.userId
                            )

                            SharedPrefHelper.getInstance().save(PrefConstant.ISGUEST, true)

                            val userDataMap = HashMap<String, String>()
                            userDataMap.put(
                                EventConstant.HUNGAMA_ID,
                                "" + SharedPrefHelper.getInstance()
                                    .get(PrefConstant.SILENT_USER_ID, "")
                            )
                            userDataMap.put(
                                EventConstant.MOREANONYMOUS_ID, SharedPrefHelper.getInstance().get(
                                    PrefConstant.SILENT_USER_ID, ""
                                )
                            )
                            EventManager.getInstance()
                                .sendUserAttribute(UserAttributeEvent(userDataMap))


                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "silentLogin")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("silentLogin").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("silentLogin").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            socialLoginResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        if (retryCount < 3) {
                            retryCount++
                            silentLogin(context, url, json)
                        } else {
                            retryCount = 0
                        }
                        socialLoginResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                }, 3)

        }

        return socialLoginResp
    }

    fun checkUserExist(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            checkUserExistResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getOpenVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (checkUserExistResp == null) {
                            checkUserExistResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {
                            setLog(TAG, "url:$url")

                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel
                            checkUserExistResp.postValue(Resource.success(baseRespModel))


                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "sso user exist")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("check user exist").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("check user exist").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            checkUserExistResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        if (retryCount < 3) {
                            retryCount++
                            silentLogin(context, url, json)
                        } else {
                            retryCount = 0
                        }
                        checkUserExistResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return checkUserExistResp
    }

    fun socialLogin(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<SocialLoginRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url  json:${json}")


            var requestTime = DateUtils.getCurrentDateTime()
            socialLoginResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (socialLoginResp == null) {
                            socialLoginResp = MutableLiveData<Resource<SocialLoginRespModel>>()
                        }

                        try {

                            val socialLoginRespModel = Gson().fromJson<SocialLoginRespModel>(
                                response.toString(),
                                SocialLoginRespModel::class.java
                            ) as SocialLoginRespModel
                            socialLoginResp.postValue(Resource.success(socialLoginRespModel))
                            if (socialLoginRespModel.result?.code == WSConstants.STATUS_200) {

                                setLog(
                                    TAG,
                                    "getHomeListData: socialLoginRespModel: $socialLoginRespModel"
                                )

                                /**
                                 * event property start
                                 */
                                val responseTime = DateUtils.getCurrentDateTime()
                                val diffInMillies: Long =
                                    Math.abs(requestTime.getTime() - responseTime.getTime())

                                val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                                setLog(
                                    TAG,
                                    "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                                )

                                val hashMap = java.util.HashMap<String, String>()
                                hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                                hashMap.put(
                                    EventConstant.NETWORKTYPE_EPROPERTY,
                                    "" + ConnectionUtil(context).networkType
                                )
                                hashMap.put(EventConstant.NAME_EPROPERTY, "socialLogin")
                                hashMap.put(
                                    EventConstant.RESPONSECODE_EPROPERTY,
                                    EventConstant.RESPONSE_CODE_200
                                )
                                hashMap.put(
                                    EventConstant.SOURCE_NAME_EPROPERTY,
                                    "" + HungamaMusicApp.getInstance()
                                        .getEventData("socialLogin").sourceName
                                )
                                hashMap.put(
                                    EventConstant.SOURCE_EPROPERTY,
                                    "" + HungamaMusicApp.getInstance()
                                        .getEventData("socialLogin").sourceName
                                )
                                hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                                hashMap.put(EventConstant.URL_EPROPERTY, url)

                                EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))


//                        if(socialLoginRespModel?.result?.data?.newUser!=null&&!TextUtils.isEmpty(socialLoginRespModel?.result?.data?.newUser) && socialLoginRespModel?.result?.data?.newUser?.contains("true",true)){
//                            try {
//                                val login_provider=JSONObject(json).optJSONObject("client_data").optString("login_provider")
//                                setLog(TAG, "setJsonDataResponse login_provider:${login_provider} login_provider:${socialLoginRespModel}")
//                                if(!TextUtils.isEmpty(login_provider!!)){
//                                    Utils.registerUserMethod_AF(login_provider)
//                                }
//
//                            }catch (exp:Exception){
//                                exp.printStackTrace()
//                            }
//                        }else{
//                            setLog(TAG, "setJsonDataResponse condition fall json:${json} login_provider:${socialLoginRespModel}")
//                        }


                                /**
                                 * event property end
                                 */
                                SharedPrefHelper.getInstance()
                                    .save(
                                        PrefConstant.USER_ID,
                                        socialLoginRespModel?.result?.data?.userId
                                    )
                                SharedPrefHelper.getInstance().save(PrefConstant.ISLOGIN, true)

                                if (!TextUtils.isEmpty(socialLoginRespModel?.result?.data?.userName)) {
                                    SharedPrefHelper.getInstance().save(
                                        PrefConstant.USER_NAME,
                                        socialLoginRespModel?.result?.data?.userName
                                    )
                                    SharedPrefHelper.getInstance()
                                        .save(
                                            PrefConstant.USER_EMAIL,
                                            socialLoginRespModel?.result?.data?.userName
                                        )

                                }

                            } else if (socialLoginRespModel.result?.code == WSConstants.STATUS_500) {
                                socialLoginResp.postValue(
                                    Resource.error(
                                        socialLoginRespModel?.result?.message!!,
                                        null
                                    )
                                )
                            } else {
                                setLog("Error", "Message->" + socialLoginRespModel?.error)
                                socialLoginResp.postValue(
                                    Resource.error(
                                        socialLoginRespModel?.result?.message!!,
                                        null
                                    )
                                )
                            }
                            val userDataMap = HashMap<String, String>()
                            userDataMap.put(
                                EventConstant.HUNGAMA_ID,
                                SharedPrefHelper.getInstance().getUserId()!!
                            )
                            userDataMap.put(
                                EventConstant.MOREANONYMOUS_ID,
                                "" + SharedPrefHelper.getInstance()
                                    .get(PrefConstant.SILENT_USER_ID, "")
                            )
                            EventManager.getInstance()
                                .sendUserAttribute(UserAttributeEvent(userDataMap))

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            socialLoginResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        socialLoginResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return socialLoginResp
    }

    fun validateOtp(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            genrateOTPResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (genrateOTPResp == null) {
                            genrateOTPResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {


                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel
                            genrateOTPResp.postValue(Resource.success(baseRespModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "validateOtp")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("validateOtp").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("validateOtp").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            genrateOTPResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        genrateOTPResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return genrateOTPResp
    }

    fun generateOtp(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            genrateOTPResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (genrateOTPResp == null) {
                            genrateOTPResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {

                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel
                            genrateOTPResp.postValue(Resource.success(baseRespModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "generateOtp")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("generateOtp").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("generateOtp").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            genrateOTPResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        genrateOTPResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return genrateOTPResp
    }

    fun verifyOTPNumber(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")

            var requestTime = DateUtils.getCurrentDateTime()
            genrateOTPResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (genrateOTPResp == null) {
                            genrateOTPResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {

                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel

                            if (!TextUtils.isEmpty(baseRespModel?.message)
                                && (baseRespModel?.message?.equals(
                                    "Otp verified Successfully",
                                    true
                                )!!
                                        || baseRespModel?.message?.equals("success", true)!!)
                            ) {
                                SharedPrefHelper.getInstance().save(PrefConstant.ISLOGIN, true)
                                genrateOTPResp.postValue(Resource.success(baseRespModel))
                            } else {
                                genrateOTPResp.postValue(
                                    Resource.error(
                                        baseRespModel?.message!!,
                                        baseRespModel
                                    )
                                )
                            }
                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "otp_verify")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("otp/verify").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("otp/verify").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            genrateOTPResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        genrateOTPResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return genrateOTPResp
    }

    fun generateOTPEmail(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            genrateOTPResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (genrateOTPResp == null) {
                            genrateOTPResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {

                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel
                            genrateOTPResp.postValue(Resource.success(baseRespModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "generateOTPEmail")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("generateOTPEmail").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("generateOTPEmail").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            genrateOTPResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        genrateOTPResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return genrateOTPResp
    }

    fun verifyOTPEmail(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            genrateOTPResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (genrateOTPResp == null) {
                            genrateOTPResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {

                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel
                            if (!TextUtils.isEmpty(baseRespModel?.message)
                                && (baseRespModel?.message?.equals(
                                    "Otp verified Successfully",
                                    true
                                )!!
                                        || baseRespModel?.message?.equals("success", true)!!)
                            ) {
                                SharedPrefHelper.getInstance().save(PrefConstant.ISLOGIN, true)
                                genrateOTPResp.postValue(Resource.success(baseRespModel))
                            } else {
                                genrateOTPResp.postValue(
                                    Resource.error(
                                        baseRespModel?.message!!,
                                        baseRespModel
                                    )
                                )
                            }

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "otp_verify")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("otp_verify").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("otp_verify").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))


                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            genrateOTPResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        genrateOTPResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return genrateOTPResp
    }

    fun MultipleBookmarkApi(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<Boolean>> {

        CoroutineScope(Dispatchers.IO).launch {
            var requestTime = DateUtils.getCurrentDateTime()
            saveUserPrefSettingResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (saveUserPrefSettingResp == null) {
                            saveUserPrefSettingResp = MutableLiveData<Resource<Boolean>>()
                            setLog(
                                TAG,
                                "setJsonDataResponse: if saveUserPrefSettingResp" + saveUserPrefSettingResp.toString()
                            )

                        }
                        try {
                            saveUserPrefSettingResp.postValue(Resource.success(true))
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "setJsonDataResponse: saveUserPrefSettingResp" + saveUserPrefSettingResp.toString()
                            )

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            saveUserPrefSettingResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        saveUserPrefSettingResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }

        return saveUserPrefSettingResp
    }

    fun callBookmarkApi(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<Boolean>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")

            var requestTime = DateUtils.getCurrentDateTime()
            saveUserPrefSettingResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (saveUserPrefSettingResp == null) {
                            saveUserPrefSettingResp = MutableLiveData<Resource<Boolean>>()
                        }

                        try {

                            saveUserPrefSettingResp.postValue(Resource.success(true))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("followfav").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("followfav").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            saveUserPrefSettingResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        saveUserPrefSettingResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return saveUserPrefSettingResp
    }

    fun getCountryData(context: Context): MutableLiveData<Resource<CountryDataModel>> {

        CoroutineScope(Dispatchers.IO).launch {
            val url = WSConstants.METHOD_COUNTRY_LIST

            var requestTime = DateUtils.getCurrentDateTime()
            countryDataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequestArray(context, url, object : DataValues {
                    override fun setJsonArrayDataResponse(response: JSONArray?) {
                        if (countryDataResp == null) {
                            countryDataResp = MutableLiveData<Resource<CountryDataModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<CountryDataModel>(
                                response.toString(),
                                CountryDataModel::class.java
                            ) as CountryDataModel

                            countryDataResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "get_country_code")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("get_country_code").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("get_country_code").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            countryDataResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setJsonDataResponse(response: JSONObject?) {

                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        countryDataResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return countryDataResp
    }

    fun getUserSocialData(
        context: Context,
        userId: String
    ): MutableLiveData<Resource<UserSocialData>> {
        CoroutineScope(Dispatchers.IO).launch {
            val url = WSConstants.METHOD_USER_SOCIAL_CONTENT + userId + "/social"

            var requestTime = DateUtils.getCurrentDateTime()
            userSocialResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (countryDataResp == null) {
                            countryDataResp = MutableLiveData<Resource<CountryDataModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<UserSocialData>(
                                response.toString(),
                                UserSocialData::class.java
                            ) as UserSocialData

                            userSocialResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "follwers_following_list")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(userId).sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(userId).sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userSocialResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        userSocialResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return userSocialResp
    }

    fun getUserPlaylistData(
        context: Context,
        otherUserId: String
    ): MutableLiveData<Resource<PlaylistRespModel>> {
        val userId = SharedPrefHelper.getInstance().getUserId().toString()
        CoroutineScope(Dispatchers.IO).launch {
            val url =
                WSConstants.METHOD_USER_PLAYLIST + otherUserId + "/playlist?requestinguid=" + userId

            var requestTime = DateUtils.getCurrentDateTime()
            userPlaylistResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userPlaylistResp == null) {
                            userPlaylistResp = MutableLiveData<Resource<PlaylistRespModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<PlaylistRespModel>(
                                response.toString(),
                                PlaylistRespModel::class.java
                            ) as PlaylistRespModel

                            userPlaylistResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_created_playlist")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(userId).sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(userId).sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userPlaylistResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        userPlaylistResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return userPlaylistResp
    }


    fun getFollwingWithFilter(
        context: Context,
        module: Int,
        typeQuery: String
    ): MutableLiveData<Resource<BookmarkDataModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            var url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance()
                .getUserId() + "/" + module + "/content/follow"
            if (!TextUtils.isEmpty(typeQuery)) {
                url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance()
                    .getUserId() + "/" + module + "/content/follow/type?typeId=" + typeQuery
            }

            var requestTime = DateUtils.getCurrentDateTime()
            userBookmarkResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userBookmarkResp == null) {
                            userBookmarkResp = MutableLiveData<Resource<BookmarkDataModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<BookmarkDataModel>(
                                response.toString(),
                                BookmarkDataModel::class.java
                            ) as BookmarkDataModel

                            userBookmarkResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData("Follw").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData("Follw").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userBookmarkResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
//                userBookmarkResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })

        }

        return userBookmarkResp
    }

    fun getUserBookmarkedDataWithFilter(
        context: Context,
        module: Int,
        typeQuery: String
    ): MutableLiveData<Resource<BookmarkDataModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            val url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance()
                .getUserId() + "/" + module + "/bookmark/type?typeId=" + typeQuery


            var requestTime = DateUtils.getCurrentDateTime()
            userBookmarkResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userBookmarkResp == null) {
                            userBookmarkResp = MutableLiveData<Resource<BookmarkDataModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<BookmarkDataModel>(
                                response.toString(),
                                BookmarkDataModel::class.java
                            ) as BookmarkDataModel

                            userBookmarkResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("bookmark/type").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("bookmark/type").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userBookmarkResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
//                userBookmarkResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })

        }

        return userBookmarkResp
    }

    fun getBookmarkCountTypeWise(
        context: Context,
        module: Int,
        typeId: String? = null
    ): MutableLiveData<Resource<BookmarkCountRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            var url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance()
                .getUserId() + "/" + module + "/count"


            var requestTime = DateUtils.getCurrentDateTime()
            userBookmarkCountResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userBookmarkCountResp == null) {
                            userBookmarkCountResp =
                                MutableLiveData<Resource<BookmarkCountRespModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<BookmarkCountRespModel>(
                                response.toString(),
                                BookmarkCountRespModel::class.java
                            ) as BookmarkCountRespModel

                            userBookmarkCountResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("bookmark/type").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("bookmark/type").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userBookmarkCountResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
//                userBookmarkCountResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })

        }

        return userBookmarkCountResp
    }

    fun getUserBookmarkedData(
        context: Context,
        module: String,
        typeId: String? = null
    ): MutableLiveData<Resource<BookmarkDataModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            var url = ""
            if (typeId != null && !TextUtils.isEmpty(typeId)) {
                url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance()
                    .getUserId() + "/" + module + "/bookmark/type?typeId=" + typeId
            } else {
                url = WSConstants.METHOD_USER_BOOKMARK + SharedPrefHelper.getInstance()
                    .getUserId() + "/" + module + "/bookmark"
            }

            var requestTime = DateUtils.getCurrentDateTime()
            userBookmarkResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userBookmarkResp == null) {
                            userBookmarkResp = MutableLiveData<Resource<BookmarkDataModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<BookmarkDataModel>(
                                response.toString(),
                                BookmarkDataModel::class.java
                            ) as BookmarkDataModel

                            userBookmarkResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "followfav")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("followfav").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("followfav").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userBookmarkResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
//                userBookmarkResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })

        }

        return userBookmarkResp
    }

    fun getUserProfileData(
        context: Context,
        userId: String
    ): MutableLiveData<Resource<UserProfileModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            var url = WSConstants.METHOD_USER_BOOKMARK + userId + "/profile"
            if (userId?.length!! > 15) {
                url = url + "?encrypt=true"
            }

            var requestTime = DateUtils.getCurrentDateTime()
            editProfileResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (editProfileResp == null) {
                            editProfileResp = MutableLiveData<Resource<UserProfileModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<UserProfileModel>(
                                response.toString(),
                                UserProfileModel::class.java
                            ) as UserProfileModel

                            editProfileResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_profile_details")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(userId).sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance().getEventData(userId).sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            editProfileResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        editProfileResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return editProfileResp
    }


    fun syncContactList(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<ContactListModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            userContactResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userContactResp == null) {
                            userContactResp = MutableLiveData<Resource<ContactListModel>>()
                        }

                        try {

                            val baseRespModel = Gson().fromJson<ContactListModel>(
                                response.toString(),
                                ContactListModel::class.java
                            ) as ContactListModel
                            userContactResp.postValue(Resource.success(baseRespModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "content_sync")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("content_sync").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("content_sync").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userContactResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        userContactResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return userContactResp
    }

    fun syncFbList(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<FacebookListModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            userFbResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (userFbResp == null) {
                            userFbResp = MutableLiveData<Resource<FacebookListModel>>()
                        }

                        try {

                            val respModel = Gson().fromJson<FacebookListModel>(
                                response.toString(),
                                FacebookListModel::class.java
                            ) as FacebookListModel
                            userFbResp.postValue(Resource.success(respModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "content_sync")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("content_sync").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("content_sync").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            userFbResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        userFbResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return userFbResp
    }

    fun getLibraryAllData(
        context: Context,
        url: String,
        json: String
    ): MutableLiveData<Resource<LibraryAllRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "url:$url")

            var requestTime = DateUtils.getCurrentDateTime()
            libraryMusicAllResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.putVolleyRequest(context, url, JSONObject(json), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (libraryMusicAllResp == null) {
                            libraryMusicAllResp = MutableLiveData<Resource<LibraryAllRespModel>>()
                        }

                        try {

                            val baseRespModel = Gson().fromJson<LibraryAllRespModel>(
                                response.toString(),
                                LibraryAllRespModel::class.java
                            ) as LibraryAllRespModel
                            libraryMusicAllResp.postValue(Resource.success(baseRespModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_content_details")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "bottom_tab"
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "bottom_tab"
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            libraryMusicAllResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        libraryMusicAllResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return libraryMusicAllResp
    }

    fun deleteUserAccount(
        context: Context,
        url: String
    ): MutableLiveData<Resource<BaseRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JSONObject()
            jsonObject.put("user_id", SharedPrefHelper.getInstance().getUserId())


            setLog(TAG, "url:$url")


            var requestTime = DateUtils.getCurrentDateTime()
            deleteUserAccountResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, jsonObject, object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (deleteUserAccountResp == null) {
                            deleteUserAccountResp = MutableLiveData<Resource<BaseRespModel>>()
                        }

                        try {

                            CommonUtils.setLog("response", "deleteUserAccount response:${response}")

                            val baseRespModel = Gson().fromJson<BaseRespModel>(
                                response.toString(),
                                BaseRespModel::class.java
                            ) as BaseRespModel
                            deleteUserAccountResp.postValue(Resource.success(baseRespModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_delete")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "Profile"
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "Profile"
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            deleteUserAccountResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        deleteUserAccountResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return deleteUserAccountResp
    }

    fun getHEApiCall(context: Context, heApi_url: String): MutableLiveData<Resource<HERespModel>> {

        CoroutineScope(Dispatchers.IO).launch {
//            val url=WSConstants.METHOD_HE_API
            val url = heApi_url


            var requestTime = DateUtils.getCurrentDateTime()
            countryDataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (telcoHEDataResp == null) {
                            telcoHEDataResp = MutableLiveData<Resource<HERespModel>>()
                        }

                        try {

                            val dataModel = Gson().fromJson<HERespModel>(
                                response.toString(),
                                HERespModel::class.java
                            ) as HERespModel

                            setLog(TAG, "getHEApiCall resp:${dataModel?.data}")

                            telcoHEDataResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "vodaHeApi")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("get_country_code").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("get_country_code").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            telcoHEDataResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                            Toast.makeText(
                                context,
                                "HE RESP:${context.getString(R.string.discover_str_2)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        telcoHEDataResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                        Toast.makeText(
                            context,
                            "HE RESP:${context.getString(R.string.discover_str_2)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })

        }

        return telcoHEDataResp
    }

    fun addRecentSearch(
        context: Context,
        url: String,
        json: JSONObject
    ): MutableLiveData<Resource<BaseSuccessRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "addRecentSearch url:${url} json:${json}")


            var requestTime = DateUtils.getCurrentDateTime()
            addRecentSearchResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.postVolleyRequest(context, url, json, object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (addRecentSearchResp == null) {
                            addRecentSearchResp = MutableLiveData<Resource<BaseSuccessRespModel>>()
                        }

                        try {
                            val dataModel = Gson().fromJson<BaseSuccessRespModel>(
                                response.toString(),
                                BaseSuccessRespModel::class.java
                            ) as BaseSuccessRespModel

                            Log.d(
                                TAG,
                                "addRecentSearch url:${url} json:${json} dataModel:${dataModel}"
                            )

                            addRecentSearchResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            Log.d(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_recent_search")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "user_recent_search"
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "user_recent_search"
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            addRecentSearchResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        addRecentSearchResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })

        }

        return addRecentSearchResp
    }

    fun getRecentSearch(context: Context): MutableLiveData<Resource<RecentSearchRespModel>> {

        CoroutineScope(Dispatchers.IO).launch {
            var url =
                WSConstants.METHOD_RECENT_SEARCH + "/" + SharedPrefHelper.getInstance().getUserId()


            var requestTime = DateUtils.getCurrentDateTime()
            getRecentSearchResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val dataModel = Gson().fromJson<RecentSearchRespModel>(
                                response.toString(),
                                RecentSearchRespModel::class.java
                            ) as RecentSearchRespModel

                            getRecentSearchResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            Log.d(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "user_recent_search")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "user_recent_search")
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, "user_recent_search")
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            getRecentSearchResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        getRecentSearchResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }

                })
        }


        return getRecentSearchResp
    }

    fun deleteRecentSearch(context: Context): MutableLiveData<Resource<DeleteRecentSearchModel>> {
        CoroutineScope(Dispatchers.IO).launch {
            var url = WSConstants.METHOD_DELETE_RECENT_SEARCH + "/" + SharedPrefHelper.getInstance()
                .getUserId()

            var requestTime = DateUtils.getCurrentDateTime()
            deleteRecentSearchResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)
                ?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val dataModel = Gson().fromJson<DeleteRecentSearchModel>(
                                response.toString(),
                                DeleteRecentSearchModel::class.java
                            ) as DeleteRecentSearchModel

                            deleteRecentSearchResp.postValue(Resource.success(dataModel))

                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            Log.d(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "delete_recent_search")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "delete_recent_search")
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, "delete_recent_search")
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY,
                                com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
//                        deleteRecentSearchResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
//                    deleteRecentSearchResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })

        }

        return deleteRecentSearchResp
    }


    fun logout_silent_user(context: Context): MutableLiveData<Resource<SocialLoginRespModel>> {
        CoroutineScope(Dispatchers.IO).launch {9

            val url =  WSConstants.METHOD_SSO_LOGOUT+ "?username=" + Utils.getDeviceId(context)

            val requestTime = DateUtils.getCurrentDateTime()
            socialLoginResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url,JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        if (socialLoginResp == null) {
                            socialLoginResp = MutableLiveData<Resource<SocialLoginRespModel>>()
                        }

                        try {

                            val socialLoginRespModel = Gson().fromJson<SocialLoginRespModel>(
                                response.toString(),
                                SocialLoginRespModel::class.java
                            ) as SocialLoginRespModel
                            socialLoginResp.postValue(Resource.success(socialLoginRespModel))

                            SharedPrefHelper.getInstance().save(
                                PrefConstant.SILENT_USER_ID,
                                socialLoginRespModel?.result?.data?.userId
                            )

                            SharedPrefHelper.getInstance().save(PrefConstant.ISGUEST, true)

                            val userDataMap = HashMap<String, String>()
                            userDataMap.put(
                                EventConstant.HUNGAMA_ID,
                                "" + SharedPrefHelper.getInstance()
                                    .get(PrefConstant.SILENT_USER_ID, "")
                            )
                            userDataMap.put(
                                EventConstant.MOREANONYMOUS_ID, SharedPrefHelper.getInstance().get(
                                    PrefConstant.SILENT_USER_ID, ""
                                )
                            )
                            EventManager.getInstance()
                                .sendUserAttribute(UserAttributeEvent(userDataMap))


                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                            hashMap.put(
                                EventConstant.NETWORKTYPE_EPROPERTY,
                                "" + ConnectionUtil(context).networkType
                            )
                            hashMap.put(EventConstant.NAME_EPROPERTY, "silentLogin")
                            hashMap.put(
                                EventConstant.RESPONSECODE_EPROPERTY,
                                EventConstant.RESPONSE_CODE_200
                            )
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("silentLogin").sourceName
                            )
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + HungamaMusicApp.getInstance()
                                    .getEventData("silentLogin").sourceName
                            )
                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                            hashMap.put(
                                EventConstant.URL_EPROPERTY, CommonUtils.getUrlWithoutParameters(url)!!
                            )

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            socialLoginResp.postValue(
                                Resource.error(
                                    context.getString(R.string.discover_str_2),
                                    null
                                )
                            )
                        }
                        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        socialLoginResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                })
        }

        return socialLoginResp
    }
}