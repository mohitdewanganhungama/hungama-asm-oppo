package com.hungama.music.ui.main.view.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.AdsConfigModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.AudioPlayerQualitySelectedEvent
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.ui.main.adapter.MusicPlaybackSettingQualityAdapter
import com.hungama.music.data.model.MusicPlaybackSettingStreamQualityModel
import com.hungama.music.data.model.QualityAction
import com.hungama.music.data.model.SongDurationConfigModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.LoginMainActivity
import com.hungama.music.ui.main.view.activity.PaymentWebViewActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.getSongDurationConfig
import com.hungama.music.utils.CommonUtils.hapticVibration
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.MUSIC_PLAYBACK_SETTINGS
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.utils.show
import com.moengage.pushbase.internal.getClearPendingIntent
import kotlinx.android.synthetic.main.fr_music_playback_setting.*
import kotlinx.android.synthetic.main.music_playback_setting_stream_quality.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MusicPlayBackSettingStreamQuality(
    val streamQualityList: ArrayList<MusicPlaybackSettingStreamQualityModel>,
    val type: String?,
    val onItemClick: OnItemClick,
    var screen: String = "",
) : SuperBottomSheetFragment(){
    var planId = ""
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var ft: SongDurationConfigModel.Ft? = null
    var nonft: SongDurationConfigModel.Nonft? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.music_playback_setting_stream_quality, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        three_dot_menu_close?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hapticVibration(requireContext(), three_dot_menu_close,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
        }
        if(screen.contains("swipable")){
            llBuyPlanNewPreview.show()
        }
        llBuyPlanNewPreview?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hapticVibration(requireContext(), llBuyPlanNewPreview,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (!SharedPrefHelper.getInstance().isUserLoggedIn()) {
                SwipablePlayerFragment.action = true
                val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                intent.putExtra("action", Constant.SIGNIN_WITH_ALL)
                startActivity(intent)
                dismiss()
            } else {
                callPayApi()
                dismiss()
            }
        }
        rvStreamQuality.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 1)
            setLog("TAGGGG", "onUserClick: contentId:${streamQualityList}")
            adapter = MusicPlaybackSettingQualityAdapter(requireContext(), streamQualityList,
                object : MusicPlaybackSettingQualityAdapter.OnItemClick {
                    override fun onUserClick(position: Int, onlyDismissPopUp:Boolean) {
                        if (onItemClick != null){
                            if (!onlyDismissPopUp){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String,String>()
                                    hashMap.put(EventConstant.STREAMQUALITYSELECTED_EPROPERTY,
                                        streamQualityList.get(position).title
                                    )

                                    if(!TextUtils.isEmpty(type)){
                                        hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, Utils.getContentTypeDetailName(type!!))
                                    }else{
                                        hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "")
                                    }

                                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                                    EventManager.getInstance().sendEvent(AudioPlayerQualitySelectedEvent(hashMap))
                                }
                               setLog("MUYYPositoonn","${position}")
                               onItemClick.onUserClickOnQuality(position, MUSIC_PLAYBACK_SETTINGS)
                                setLog("StreamingQuality","Check${CommonUtils.getStreamQualityDummyData().get(position).title}")
                                saveSetting(CommonUtils.getStreamQualityDummyData().get(position).title)

                                if (context != null){
                                    val intent = Intent(Constant.AUDIO_QUALITY_CHANGE_EVENT)
                                    intent.putExtra("EVENT", Constant.AUDIO_QUALITY_CHANGE_RESULT_CODE)
                                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

                                }
                            }
                            dismiss()

                        }
                    }


                }, QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY)
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        if(screen.contains("swipable"))
            requireContext().resources.getDimension(R.dimen.dimen_300).toInt()
          else  requireContext().resources.getDimension(R.dimen.dimen_400).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

 /*   interface OnItemClick1 {
        fun onUserClickOnQuality1(position: Int, settingType:Int)
    }*/
    interface OnItemClick {
        fun onUserClickOnQuality(position: Int, settingType:Int)
    }
    interface OnItemClick1{
        fun onUserClickOnQuality1(position: Int, settingType:Int)
    }

    private fun saveSetting(title: String?) {
        var userViewModel: UserViewModel? = null
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {

            try {

                val userSettingRespModel=
                    SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
                if(userSettingRespModel!=null && userSettingRespModel?.data != null){
                    val mainJson = JSONObject()
                    val prefArrays = JSONArray()

                    val emailSettingJson = JSONObject()
                    val userSettingsData = userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)
                    emailSettingJson.put("autoPlay",userSettingsData?.autoPlay)
                    emailSettingJson.put("streaming_quality",title?.trim().toString())
                    emailSettingJson.put("music_language_preference", userSettingsData?.music_language_preference)
                    emailSettingJson.put("music_language_preference_title", userSettingsData?.music_language_preference_title)
                    emailSettingJson.put("equalizer","equalizer")
                    emailSettingJson.put("sleep_timer",userSettingsData?.sleep_timer)
                    emailSettingJson.put("show_lyrics",userSettingsData?.show_lyrics)
                    emailSettingJson.put("gapless",userSettingsData?.gapless)
                    emailSettingJson.put("crossfade",userSettingsData?.crossfade)
                    emailSettingJson.put("smooth_song_transition",userSettingsData?.smooth_song_transition)

                    mainJson.put("type", Constant.TYPE_MUSICPLAYBACK_SETTING)
                    mainJson.put("preference", prefArrays)

                    prefArrays.put(emailSettingJson)
                    userViewModel.saveUserPref(
                        requireContext(),
                        mainJson.toString(),
                        Constant.TYPE_MUSICPLAYBACK_SETTING
                    )
                }else{
                    setLog("Here","Here${title}");
                    getMusicPlayBackSetting(title)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else {
        }
    }

    private fun getMusicPlayBackSetting(title: String?){
        if (ConnectionUtil(requireContext()).isOnline) {
            var userViewModel: UserViewModel? = null
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)
            userViewModel.getUserSettingType(
                requireContext(),
                Constant.TYPE_MUSICPLAYBACK_SETTING
            ).observe(this,
                Observer {
                    when(it.status){

                        Status.SUCCESS->{

                            if (it?.data != null) {

                                saveSetting(title)
                            }else{

                                saveNewData(title)
                            }
                        }

                        Status.LOADING ->{

                        }

                        Status.ERROR ->{

                            //Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun saveNewData(title: String?){
        var userViewModel: UserViewModel? = null
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {

            try {
                    val mainJson = JSONObject()
                    val prefArrays = JSONArray()

                    val emailSettingJson = JSONObject()
                    emailSettingJson.put("autoPlay",true)
                    emailSettingJson.put("streaming_quality",title?.trim().toString())
                    emailSettingJson.put("music_language_preference", SharedPrefHelper.getInstance().getMusicLanguageCodeList())
                    emailSettingJson.put("music_language_preference_title", SharedPrefHelper.getInstance().getMusicLanguageTitleList())
                    emailSettingJson.put("equalizer","equalizer")
                    emailSettingJson.put("sleep_timer",true)
                    emailSettingJson.put("show_lyrics", true)
                    emailSettingJson.put("gapless", false)
                    emailSettingJson.put("crossfade","")
                    emailSettingJson.put("smooth_song_transition",false)

                    mainJson.put("type", Constant.TYPE_MUSICPLAYBACK_SETTING)
                    mainJson.put("preference", prefArrays)


                    prefArrays.put(emailSettingJson)
                    userViewModel.saveUserPref(
                        requireContext(),
                        mainJson.toString(),
                        Constant.TYPE_MUSICPLAYBACK_SETTING
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else {
        }
    }
    fun callPayApi() {
        if (ConnectionUtil(activity).isOnline) {
            val intent = Intent(requireContext(), PaymentWebViewActivity::class.java)
            val genrtedURL = CommonUtils.genratePaymentPageURL(requireContext(), "")

            intent.putExtra("url", genrtedURL)
            intent.putExtra("planName", CommonUtils.getNudgePlanId())

            if (BaseActivity.player11 != null){
                BaseActivity.player11?.pause()
            }

            startActivity(intent)
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35),
                getString(R.string.toast_message_5),
                MessageType.NEGATIVE,
                true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

}

