package com.hungama.music.ui.main.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.ChooseMusicLanguageActivity
import com.hungama.music.utils.CommonUtils.getStreamQualityDummyData
import com.hungama.music.utils.Constant.DOWNLOAD_SETTINGS
import com.hungama.music.utils.Constant.MUSIC_PLAYBACK_SETTINGS
import com.hungama.music.utils.Constant.VIDEO_PLAYBACK_SETTINGS
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.EXTRA_VIDEO_SETTING
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_video_playback_setting.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 7/9/2021.
 * Purpose:
 */
class VideoPlayBackSetting : BaseFragment(), VideoPlayBackSettingStreamQuality.OnItemClick {
    var userViewModel: UserViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_video_playback_setting, container, false)
    }

    override fun initializeComponent(view: View) {


        rlStreamQuality?.setOnClickListener(this)
        svAutoPlay?.setOnClickListener(this)
        rlMusicLangPreference?.setOnClickListener(this)
        tvLangName.text=SharedPrefHelper.getInstance().getVideoLanguageTitleList()


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
        setUpViewModel()
    }

    private fun setUpUI() {
        ivBack?.setOnClickListener{ view -> backPress() }
        tvActionBarHeading.text = getString(R.string.general_setting_str_5)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "Profile","Settings_video playback settings",
            "")
        tvLangName.text=SharedPrefHelper.getInstance().getVideoLanguageTitleList()
        setLog(TAG, "setUpUI: tvLangName a"+SharedPrefHelper.getInstance().getVideoLanguageTitleList())
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == rlStreamQuality) {
            val sheet = VideoPlayBackSettingStreamQuality(
                getStreamQualityDummyData(QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY),
                this@VideoPlayBackSetting)
            sheet.show(activity?.supportFragmentManager!!, "VideoPlayBackSettingStreamQuality")

            CommonUtils.PageViewEvent("","","","",
                "Video_playback_settings", "Settings_videoplaybacksettings_streamingquality","")
        } else if (v == svAutoPlay) {
            saveSetting()
        } else if(v==rlMusicLangPreference){
            val intent = Intent(requireActivity(), ChooseMusicLanguageActivity::class.java)
            intent?.putExtra(Constant.EXTRA_IS_FROM_GEN_SETTING,true)
            intent?.putExtra(Constant.EXTRA_IS_FROM_GEN_MUSIC_SETTING,EXTRA_VIDEO_SETTING)
            intent.putExtra(Constant.MUSIC_VIDEO_NAME_PAGE, getString(R.string.general_setting_str_4))
            startActivityForResult(intent,9002)
            activity?.overridePendingTransition(R.anim.enter, R.anim.exit)
            CommonUtils.PageViewEvent("","","","",
                "Video_playback_settings", "Settings_videoplaybacksettings_musiclanguagepreference","")
        }
    }

    override fun onUserClick(position: Int, settingType: Int) {
        if (settingType == MUSIC_PLAYBACK_SETTINGS) {
            tvQuality.text = getStreamQualityDummyData().get(position).title
        } else if (settingType == VIDEO_PLAYBACK_SETTINGS) {

        } else if (settingType == DOWNLOAD_SETTINGS) {

        }

        saveSetting()
    }


    private fun setUpViewModel() {

        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getSetting()
    }


    private fun fillUI(userSettingRespModel: UserSettingRespModel) {
        if (userSettingRespModel?.data != null && userSettingRespModel?.data?.data?.get(0)?.preference?.get(
                0
            ) != null
        ) {
            svAutoPlay.isChecked =
                userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.autoPlay!!
            tvQuality.text =
                userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality.toString()
            val quality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality.toString()!!)
            CommonUtils.setStoredSelectedQualityId(QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY, quality?.id!!, quality.qualityName)
            if(!TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.video_language_preference_title?.toString())){
                tvLangName.text=userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.video_language_preference_title?.toString()
                SharedPrefHelper.getInstance().setVideoLanguageTitleList(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.video_language_preference_title?.toString()!!)
                SharedPrefHelper.getInstance().setVideoLanguageCodeList(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.video_language_preference?.toString()!!)
            }else{
                tvLangName.text=SharedPrefHelper.getInstance().getVideoLanguageTitleList()
            }
        }
    }




    private fun getSetting() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSettingType(
                requireContext(),
                Constant.TYPE_VIDEOPLAYBACK_SETTING
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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
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
                emailSettingJson.put("autoPlay", svAutoPlay?.isChecked)
                emailSettingJson.put("streaming_quality", tvQuality?.text?.trim().toString())
                emailSettingJson.put("video_language_preference", SharedPrefHelper.getInstance().getVideoLanguageCodeList())
                emailSettingJson.put("video_language_preference_title", SharedPrefHelper.getInstance().getVideoLanguageTitleList())

                prefArrays.put(emailSettingJson)
                mainJson.put("type", Constant.TYPE_VIDEOPLAYBACK_SETTING)
                mainJson.put("preference", prefArrays)


                userViewModel?.saveUserPref(
                    requireContext(),
                    mainJson.toString(),
                    Constant.TYPE_VIDEOPLAYBACK_SETTING
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(requireContext(),
                    requireView(),
                    false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
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