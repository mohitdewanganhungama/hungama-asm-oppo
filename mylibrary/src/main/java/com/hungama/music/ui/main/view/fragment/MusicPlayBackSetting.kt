package com.hungama.music.ui.main.view.fragment

import android.app.Activity
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.ChooseMusicLanguageActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.getStreamQualityDummyData
import com.hungama.music.utils.Constant.DOWNLOAD_SETTINGS
import com.hungama.music.utils.Constant.MUSIC_PLAYBACK_SETTINGS
import com.hungama.music.utils.Constant.VIDEO_PLAYBACK_SETTINGS
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.EXTRA_MUSIC_SETTING
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_music_playback_setting.*
import kotlinx.android.synthetic.main.fr_music_playback_setting.rlMain
import kotlinx.android.synthetic.main.fr_music_playback_setting.tvQuality
import kotlinx.android.synthetic.main.header_back_transparent.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 7/9/2021.
 * Purpose:
 */
class MusicPlayBackSetting : BaseFragment(), MusicPlayBackSettingStreamQuality.OnItemClick {
    var userViewModel: UserViewModel? = null
    var isSystemEqualizerEnable = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_music_playback_setting, container, false)
    }

    override fun initializeComponent(view: View) {
        val systemEqualizer = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        if (systemEqualizer.resolveActivity(requireActivity().packageManager) != null) {
            rlMainEqualizer.show()
        }else{
            rlMainEqualizer.hide()
        }

        svAutoPlay?.setOnClickListener(this)
        svSleepTimer?.setOnClickListener(this)
        svShowLycris?.setOnClickListener(this)
        svGapless?.setOnClickListener(this)
        svSongTransition?.setOnClickListener(this)
        rlStreamQuality?.setOnClickListener(this)
        rlMusicLangPreference?.setOnClickListener(this)


        rlMainEqualizer?.setOnClickListener(this)
        setUpViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
     return false
    }

    override fun onResume() {
        super.onResume()

        setUpUI()
    }

    private fun setUpUI(){
        ivBack?.setOnClickListener{ view -> backPress() }
        tvActionBarHeading.text = getString(R.string.general_setting_str_4)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "Profile","Settings_music playback settings",
            "")
        tvQuality.text = SharedPrefHelper.getInstance().getMusicPlaybackStreamQualityTitle()
        tvLangName.text=SharedPrefHelper.getInstance().getMusicLanguageTitleList()
        setLog(TAG, "setUpUI: tvLangName a"+SharedPrefHelper.getInstance().getMusicLanguageTitleList())
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v==rlStreamQuality){
            val sheet = MusicPlayBackSettingStreamQuality(getStreamQualityDummyData(QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY), "",this@MusicPlayBackSetting)
            sheet.show(activity?.supportFragmentManager!!, "MusicPlayBackSettingStreamQualityy")

            CommonUtils.PageViewEvent("","","","",
                "Music_playback_settings", "settings_musicplaybacksettings_streamingquality","")
        }else if(v==rlMusicLangPreference){
            val intent = Intent(requireActivity(), ChooseMusicLanguageActivity::class.java)
            intent.putExtra(Constant.EXTRA_IS_FROM_GEN_SETTING,true)
            intent.putExtra(Constant.EXTRA_IS_FROM_GEN_MUSIC_SETTING,EXTRA_MUSIC_SETTING)
            intent.putExtra(Constant.MUSIC_VIDEO_NAME_PAGE, getString(R.string.general_setting_str_4))
            startActivityForResult(intent,9002)
            activity?.overridePendingTransition(R.anim.enter, R.anim.exit)
            CommonUtils.PageViewEvent("","","","",
                "Music_playback_settings", "settings_musicplaybacksettings_musiclanguagepreference","")
        }else if(v==svAutoPlay || v==svSleepTimer || v==svShowLycris || v==svGapless || v==svSongTransition){
            saveSetting()
        }else if (v == rlMainEqualizer){
            val audioSessionId = (activity as MainActivity).getAudioSession()
            try {
                val systemEq = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                systemEq.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                systemEq.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireActivity().packageName)

                if (systemEq.resolveActivity(requireActivity().packageManager) != null) {
                    startActivityForResult(systemEq, Constant.EQUILISER_REQ_CODE)
                    //CommonUtils.bindSystemEqualizer(requireActivity(), audioSessionId)
                } else {
                    val messageModel = MessageModel(getString(R.string.general_str_8), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }

            } catch (exp: Exception) {
                exp.printStackTrace()
                val messageModel = MessageModel(getString(R.string.general_str_8), MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }
    }

    override fun onUserClickOnQuality(position: Int, settingType: Int) {
        setLog("tvQuality","tvQuality${getStreamQualityDummyData().get(position).title}")
//        tvQuality.text = getStreamQualityDummyData().get(position).title\


        if (settingType == MUSIC_PLAYBACK_SETTINGS){
            tvQuality.text = getStreamQualityDummyData().get(position).title

        }else if (settingType == VIDEO_PLAYBACK_SETTINGS){

        }else if (settingType == DOWNLOAD_SETTINGS){

        }

        saveSetting()
    }

    private fun setUpViewModel() {

        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getMusicPlayBackSetting()
    }

    private fun fillUI(userSettingRespModel: UserSettingRespModel) {
        if(userSettingRespModel?.data!=null && userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)!=null){
            svSongTransition.isChecked= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.smooth_song_transition!!
            if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.crossfade!=null&&!TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.crossfade)){
                sliderCrossfade.value= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.crossfade?.toFloat()!!
            }else{
                sliderCrossfade.value= 0f
            }

            svGapless.isChecked= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.gapless!!
            svShowLycris.isChecked= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.show_lyrics!!
            svSleepTimer.isChecked= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.sleep_timer!!
            if(!TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.music_language_preference_title?.toString())){
                tvLangName.text=userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.music_language_preference_title?.toString()
                SharedPrefHelper.getInstance().setMusicLanguageTitleList(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.music_language_preference_title?.toString()!!)
                SharedPrefHelper.getInstance().setMusicLanguageCodeList(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.music_language_preference?.toString()!!)
            }else{
                tvLangName.text=SharedPrefHelper.getInstance().getMusicLanguageTitleList()
            }
            tvQuality.text=userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality?.toString()
            svAutoPlay.isChecked= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.autoPlay!!
            val quality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality?.toString()!!)
            CommonUtils.setStoredSelectedQualityId(QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY, quality?.id!!, quality.qualityName)
        }
    }

    private fun getMusicPlayBackSetting(){
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSettingType(
                requireContext(),
                Constant.TYPE_MUSICPLAYBACK_SETTING
            )?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUI(it?.data!!)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun saveSetting() {
        if (ConnectionUtil(requireContext()).isOnline) {

            try {
                val mainJson = JSONObject()
                val prefArrays = JSONArray()

                val emailSettingJson = JSONObject()
                emailSettingJson.put("autoPlay",svAutoPlay?.isChecked)
                emailSettingJson.put("streaming_quality",tvQuality?.text?.trim().toString())
                emailSettingJson.put("music_language_preference", SharedPrefHelper.getInstance().getMusicLanguageCodeList())
                emailSettingJson.put("music_language_preference_title", SharedPrefHelper.getInstance().getMusicLanguageTitleList())
                emailSettingJson.put("equalizer","equalizer")
                emailSettingJson.put("sleep_timer",svSleepTimer?.isChecked)
                emailSettingJson.put("show_lyrics",svShowLycris?.isChecked)
                emailSettingJson.put("gapless",svGapless?.isChecked)
                emailSettingJson.put("crossfade",sliderCrossfade?.value)
                emailSettingJson.put("smooth_song_transition",svSongTransition?.isChecked)

                mainJson.put("type", Constant.TYPE_MUSICPLAYBACK_SETTING)
                mainJson.put("preference", prefArrays)

                prefArrays.put(emailSettingJson)
                userViewModel?.saveUserPref(
                    requireContext(),
                    mainJson.toString(),
                    Constant.TYPE_MUSICPLAYBACK_SETTING
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(requireContext(),
                    rlMain!!,
                    false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==9002 && resultCode == Activity.RESULT_OK){
            saveSetting()
        }
    }

}