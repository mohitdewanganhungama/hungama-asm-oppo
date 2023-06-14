package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.getStreamQualityDummyData
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.DOWNLOAD_MUSICSTREAMQUALITY
import com.hungama.music.utils.Constant.DOWNLOAD_VIDEOSTREAMQUALITY
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_download_setting.*
import kotlinx.android.synthetic.main.header_back_transparent.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 7/9/2021.
 * Purpose:
 */
class DownloadsSetting : BaseFragment(), DownloadMusicStreamQuality.OnItemClick,
    DownloadVideoStreamQuality.OnItemClick, BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel: UserViewModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_download_setting, container, false)
    }

    override fun initializeComponent(view: View) {


        svCellularDownload?.setOnClickListener(this)
        rlMusicStreamQuality?.setOnClickListener(this)
        rlVideoStreamQuality?.setOnClickListener(this)

        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        setUpUI()
        setUpViewModel()
    }

    private fun setUpUI() {
        ivBack?.setOnClickListener{ view -> backPress() }
        tvActionBarHeading.text = getString(R.string.general_setting_str_44)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "Profile",
            "Settings_downloads",
            "")
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == rlMusicStreamQuality) {
            val sheet = DownloadMusicStreamQuality(
                getStreamQualityDummyData(QualityAction.MUSIC_PLAYBACK_DOWNLOAD_QUALITY),
                this@DownloadsSetting
            )
            sheet.show(activity?.supportFragmentManager!!, "DownloadMusicStreamQuality")
            CommonUtils.PageViewEvent("","","","",
                "Download", "Settings_downloads_musicdownloadquality","")
        }else if (v == rlVideoStreamQuality) {
            val sheet = DownloadVideoStreamQuality(
                getStreamQualityDummyData(QualityAction.VIDEO_PLAYBACK_DOWNLOAD_QUALITY),
                this@DownloadsSetting
            )
            sheet.show(activity?.supportFragmentManager!!, "DownloadVideoStreamQuality")
            CommonUtils.PageViewEvent("","","","",
                "Download", "Settings_downloadssettings_videodownloadquality","")
        } else if (v == svCellularDownload) {
            saveSetting()
        }
    }

    override fun onUserClick(position: Int, settingType: Int) {
        if (settingType == DOWNLOAD_MUSICSTREAMQUALITY) {
            tvQuality.text = getStreamQualityDummyData().get(position).title
            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.MUSIC_DOWNLOAD_QUALITY, ""+getStreamQualityDummyData().get(position).title)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        } else if (settingType == DOWNLOAD_VIDEOSTREAMQUALITY) {
            tvVideoQuality.text = getStreamQualityDummyData().get(position).title

            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.VIDEO_DOWNLOAD_QUALITY, ""+getStreamQualityDummyData().get(position).title)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
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
        if(userSettingRespModel?.data!=null && userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)!=null){
            svCellularDownload.isChecked= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.cellularDownload!!
            tvQuality.text= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.musicDownloadQuality
            tvVideoQuality.text= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.videoDownloadQuality
            val musicQuality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.musicDownloadQuality.toString()!!)
            CommonUtils.setStoredSelectedQualityId(QualityAction.MUSIC_PLAYBACK_DOWNLOAD_QUALITY, musicQuality?.id!!, musicQuality.qualityName)
            val videoQuality = Quality.getQualityByName(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.videoDownloadQuality.toString()!!)
            CommonUtils.setStoredSelectedQualityId(QualityAction.VIDEO_PLAYBACK_DOWNLOAD_QUALITY, videoQuality?.id!!, videoQuality.qualityName)
        }
    }


    private fun getSetting() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSettingType(
                requireContext(),
                Constant.TYPE_DOWNLOADS_SETTING
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
                emailSettingJson.put("cellular_download", svCellularDownload?.isChecked)
                emailSettingJson.put("music_download_quality", tvQuality?.text?.trim().toString())
                emailSettingJson.put("video_download_quality", tvVideoQuality?.text?.trim().toString())
                prefArrays.put(emailSettingJson)


                mainJson.put("type", Constant.TYPE_DOWNLOADS_SETTING)
                mainJson.put("preference", prefArrays)


                userViewModel?.saveUserPref(
                    requireContext(),
                    mainJson.toString(),
                    Constant.TYPE_DOWNLOADS_SETTING
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(requireContext(),
                    parentMobile!!,
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

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}