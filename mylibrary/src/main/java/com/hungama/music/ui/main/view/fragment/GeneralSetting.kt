package com.hungama.music.ui.main.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.activity.ChooseLanguageActivity
import com.hungama.music.data.model.UserSettingRespModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_general_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 7/9/2021.
 * Purpose:
 */
class GeneralSetting : BaseFragment() {
    var userViewModel: UserViewModel? = null
    var deeplinkPageName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_general_setting, container, false)
    }

    override fun initializeComponent(view: View) {
        if (arguments != null){
            if (requireArguments().containsKey(Constant.EXTRA_PAGE_DETAIL_NAME)){
                deeplinkPageName = requireArguments().getString(Constant.EXTRA_PAGE_DETAIL_NAME).toString()
            }
        }
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        scrollView.visibility = View.VISIBLE
        rlParentalControl.setOnClickListener(this)
        svEmailNotification?.setOnClickListener(this)
        svMobileNotification?.setOnClickListener(this)
        svExplicit?.setOnClickListener(this)
        if (Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
            tvHeaderLanguage?.show()
            rlLanguage?.show()
            rlLang?.setOnClickListener(this)
        }else{
            tvHeaderLanguage?.hide()
            rlLanguage?.hide()
            rlLang?.setOnClickListener(null)
        }

        svParentalControl?.setOnClickListener(this)
        svAudioContent?.setOnClickListener(this)
        svVideoContent?.setOnClickListener(this)
        redirectToDeeplinkPage()
        svAgeconfirmation?.setOnClickListener(this)
        setLog("GeneralSettings", "initializeComponent: "+scrollView.paddingBottom)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == svEmailNotification || v == svMobileNotification || v == svExplicit ||
            v == svAudioContent || v == svVideoContent || v == svAgeconfirmation) {
            if (v == svAudioContent || v == svVideoContent || v == svAgeconfirmation){
                if (svAgeconfirmation.isChecked){
                    enableParentalControl(false)
                }else{
                    enableParentalControl(true)
                }
            }
            setParentalControl()
            saveSetting()

        } else if (v == rlLang) {
            openAppLangChangePage()
        } else if (v == rlParentalControl || v == svParentalControl){
            if (hidden_chat_view.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    rlParentalControl,
                    AutoTransition()
                )
                hidden_chat_view?.hide()
            }
            else {
                TransitionManager.beginDelayedTransition(
                    rlParentalControl,
                    AutoTransition()
                )
                hidden_chat_view?.show()
            }
            if (v == svParentalControl){
                if (!svAgeconfirmation.isChecked){
                    svAudioContent.isChecked = svParentalControl.isChecked
                    svVideoContent.isChecked = svParentalControl.isChecked
                    setParentalControl()
                    saveSetting()
                }else{
                    enableParentalControl(false)
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        setLog("GeneralSettings", "onResume: ")
        setUpUI()
        setUpViewModel()
        hideBottomNavigationAndMiniplayer()
        CommonUtils.setPageBottomSpacing(scrollView, requireContext(), 0,0,0,0)
    }

    private fun setUpUI() {
        ivBack?.setOnClickListener{ backPress() }
        tvActionBarHeading.text = getString(R.string.general_setting_str_3)

        setLog("alhlghal", MainActivity.lastItemClicked + " " +
                MainActivity.tempLastItemClicked + " " + MainActivity.headerItemName)

            CommonUtils.PageViewEvent("","","","",
                "Profile","Settings_general settings","")

    }


    private fun setUpViewModel() {

        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getSetting()
    }

    private fun fillUI(userSettingRespModel: UserSettingRespModel) {
        if (userSettingRespModel.data != null && userSettingRespModel.data?.data?.get(0)?.preference?.get(0) != null) {
            svEmailNotification.isChecked =
                userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.emailNotification!!
            svMobileNotification.isChecked =
                userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.mobileNotification!!
            SharedPrefHelper.getInstance().setMobileNotificationEnable(svMobileNotification.isChecked)
            svAudioContent.isChecked = userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitAudioContent!!
            svVideoContent.isChecked = userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitVideoContent!!
            if (svVideoContent.isChecked == true){
                hidden_chat_view.visibility = View.VISIBLE
                setLog(TAG, "onClick: if working")
            }else{
                hidden_chat_view.visibility = View.GONE
                setLog(TAG, "onClick: else working")
            }
            setParentalControl()
            if (userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.allowAge18Plus!!){
                SharedPrefHelper.getInstance().setUserCensorRating(18)
                enableParentalControl(false)
            }else{
                SharedPrefHelper.getInstance().setUserCensorRating(0)
                enableParentalControl(true)
            }
            svAgeconfirmation?.isChecked = userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.allowAge18Plus!!
            //svExplicit.isChecked = userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.allowExplicitContentOld!!
//            tvLangName.text =
//                userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.appLanguage.toString()
            tvLangName.text =
                SharedPrefHelper.getInstance().getLanguageTitle()
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            scrollView.visibility = View.VISIBLE
        }
    }

    private fun getSetting() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSettingType(
                requireContext(),
                Constant.TYPE_GENERAL_SETTING
            )?.observe(this,
                {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
//                            setProgressBarVisible(false)
                            shimmerLayout.visibility = View.GONE
                            shimmerLayout.stopShimmer()
                            scrollView.visibility = View.VISIBLE
                            if (it?.data != null) {
                                fillUI(it.data)

                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
//                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
//                            setEmptyVisible(false)
//                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            shimmerLayout.visibility = View.GONE
                            shimmerLayout.stopShimmer()
                            scrollView.visibility = View.VISIBLE
                        }
                    }
                })
        } else {
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
                emailSettingJson.put("emailNotification", svEmailNotification?.isChecked)
                emailSettingJson.put("mobileNotification", svMobileNotification?.isChecked)
                //emailSettingJson.put("allowExplicitContent", svExplicit?.isChecked)
                val parentalJson = JSONObject()
                parentalJson.put("allowExplicitAudioContent", svAudioContent.isChecked)
                parentalJson.put("allowExplicitVideoContent", svVideoContent.isChecked)
                emailSettingJson.put("parentalControl", parentalJson)
                emailSettingJson.put("allowAge18Plus", svAgeconfirmation?.isChecked)
                emailSettingJson.put("appLanguage", SharedPrefHelper.getInstance().getLanguage())

                prefArrays.put(emailSettingJson)


                mainJson.put("type", Constant.TYPE_GENERAL_SETTING)
                mainJson.put("preference", prefArrays)

                if (svAgeconfirmation?.isChecked == true){
                    SharedPrefHelper.getInstance().setUserCensorRating(18)
                }else{
                    SharedPrefHelper.getInstance().setUserCensorRating(0)
                }

                userViewModel?.saveUserPref(
                    requireContext(),
                    mainJson.toString(),
                    Constant.TYPE_GENERAL_SETTING
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
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    var activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        run {
            if (result.resultCode == 9001) {
                saveSetting()
                tvLangName?.text = SharedPrefHelper.getInstance().getLanguageObject(PrefConstant.LANG_DATA)?.code
                restartApp()
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(
            requireContext(),
            MainActivity::class.java
        )
        BaseActivity.isAppLanguageChanged = true
        requireActivity().finish()
        startActivity(intent)


        /*val mPendingIntentId: Int = 1000
        val mPendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            mPendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr: AlarmManager =
            getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)*/
    }

    private fun redirectToDeeplinkPage(){
        if (!TextUtils.isEmpty(deeplinkPageName)){
            if (deeplinkPageName.equals("app-language", true)){
                openAppLangChangePage()
            }
        }
    }

    private fun openAppLangChangePage(){
        val intent = Intent(requireActivity(), ChooseLanguageActivity::class.java)
        intent.putExtra(Constant.EXTRA_IS_FROM_GEN_SETTING, true)
        //startActivityForResult(intent, 9001)
        activityResult.launch(intent)
        activity?.overridePendingTransition(R.anim.enter, R.anim.exit)

        CommonUtils.PageViewEvent("","","","",
            "general_setting", "settings_generalsettings_applanguage","")
    }

    private fun setParentalControl(){
        svParentalControl.isChecked = svAudioContent.isChecked || svVideoContent.isChecked
        setLog("GeneralSettings", "setParentalControl:- " + svParentalControl.isChecked)
        if (svParentalControl.isChecked){
            val explicit = (context as MainActivity).fetchTrackData().explicit
            setLog("GeneralSettings", "setParentalControl:- $explicit")
            if (explicit == 1){
                (context as MainActivity).playNextSong(true)
            }
        }
    }
     private fun enableParentalControl(isEnable:Boolean){
         if (isEnable){
             rlParentalControl?.alpha = 1f
         }else{
             rlParentalControl?.alpha = 0.4f
             svParentalControl?.isChecked =  false
             svAudioContent?.isChecked = false
             svVideoContent?.isChecked = false
         }
     }

}