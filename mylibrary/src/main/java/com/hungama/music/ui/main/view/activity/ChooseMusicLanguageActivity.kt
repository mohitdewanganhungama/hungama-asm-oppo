package com.hungama.music.ui.main.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.HungamaMusicApp
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.MusicLanguageSelectionModel
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.Utils.Companion.convertArrayToString
import com.hungama.music.ui.main.viewmodel.ChooseLanguageViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.LanguageChangedEvent
import com.hungama.music.eventanalytic.eventreporter.LanguageSelectEvent
import com.hungama.music.eventanalytic.eventreporter.MoviesLanguageSelectedEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.adapter.MusicLanguageGridRecyclerAdapter
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant.EXTRA_MUSIC_SETTING
import com.hungama.music.utils.Constant.EXTRA_VIDEO_SETTING
import kotlinx.android.synthetic.main.activity_choose_language.*
import kotlinx.android.synthetic.main.fr_music_playback_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ChooseMusicLanguageActivity : AppCompatActivity() {
    var chooseLanguageViewModel: ChooseLanguageViewModel? = null
    var layoutManger: LinearLayoutManager? = null
    var listOfLanguage = ArrayList<MusicLanguageSelectionModel.Data.Body.Row.Item>()
    val listOfLanguageNameSelected = mutableListOf<String>()
    val listOfLanguageTitleSelected = mutableListOf<String>()
    var myLocale: Locale? = null
    var languageRespModel1: MusicLanguageSelectionModel? = null
    var isFromGenSetting=false
    var isFromGenMusicSetting = 0
    var userViewModel: UserViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_language)


        Utils.setWindowProperty(this@ChooseMusicLanguageActivity)

        if(intent?.hasExtra(Constant.EXTRA_IS_FROM_GEN_SETTING)!!){
            isFromGenSetting= intent?.getBooleanExtra(Constant.EXTRA_IS_FROM_GEN_SETTING,false)!!
            if (isFromGenSetting){
                if(intent?.hasExtra(Constant.EXTRA_IS_FROM_GEN_MUSIC_SETTING)!!){
                   isFromGenMusicSetting = intent?.getIntExtra(Constant.EXTRA_IS_FROM_GEN_MUSIC_SETTING,0)!!
                }
            }

            if (intent.hasExtra(Constant.MUSIC_VIDEO_NAME_PAGE))
            CommonUtils.PageViewEvent("",
                "",
                "","",
                "Profile" + "_" + intent.hasExtra(Constant.MUSIC_VIDEO_NAME_PAGE),
                getString(R.string.login_str_55),
                "")
        }else{
            isFromGenSetting=false
        }

        CommonUtils.applyButtonTheme(this, ll_next)
       // fill otp and call the on click on button
        ll_next.setOnClickListener {
            saveLanguageData()
        }
        setUpViewModel(isFromGenMusicSetting)
    }

    private fun initRecyclerView() {
        recyclerLanguage.layoutManager = GridLayoutManager(this,2)

        //This will for default android divider
        //recyclerLanguage.addItemDecoration(GridItemDecoration(10, 2))
        //var titleList:List<String> = ArrayList()
        var codeList:List<String> = ArrayList()
        if (isFromGenMusicSetting == EXTRA_MUSIC_SETTING){
            codeList =
                SharedPrefHelper?.getInstance()?.getMusicLanguageCodeList().toString().split(",").toTypedArray()
                    .toList()
            //titleList = SharedPrefHelper?.getInstance()?.getMusicLanguageTitleList().toString().toString().split(",").toTypedArray()
            //                    .toList()
        }else if (isFromGenMusicSetting == EXTRA_VIDEO_SETTING){
            codeList = SharedPrefHelper?.getInstance()?.getVideoLanguageCodeList().toString().toString().split(",").toTypedArray()
                .toList()
            //titleList = SharedPrefHelper?.getInstance()?.getVideoLanguageTitleList().toString().toString().split(",").toTypedArray()
            //                    .toList()
        }
        listOfLanguage?.forEachIndexed { index, item ->
            for (code in codeList.iterator()){
                if (code.equals(listOfLanguage.get(index).code)){
                    listOfLanguageNameSelected.add(listOfLanguage.get(index).code.toString())
                    listOfLanguageTitleSelected.add(listOfLanguage.get(index).title)
                    listOfLanguage.get(index).isSelected = true
                }
            }
        }
        val movieListAdapter = MusicLanguageGridRecyclerAdapter(this).apply {
            itemClick = { languageModel ->
                if(!TextUtils.isEmpty(languageModel.code)){
                    if(!listOfLanguageNameSelected.contains(languageModel.code)){
                        listOfLanguageNameSelected.add(languageModel.code!!)
                        listOfLanguageTitleSelected.add(languageModel.title!!)
                        languageModel.isSelected=true
                    }else{
                        listOfLanguageNameSelected.remove(languageModel.code)
                        listOfLanguageTitleSelected.remove(languageModel.title!!)
                        languageModel.isSelected=false
                    }
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        recyclerLanguage.adapter = movieListAdapter
        movieListAdapter.setLanguageList(listOfLanguage)

    }

    private fun setData(languageRespModel: MusicLanguageSelectionModel) {
        languageRespModel1 = languageRespModel
        listOfLanguage =
            (languageRespModel.data?.body?.rows?.get(0)?.items as ArrayList<MusicLanguageSelectionModel.Data.Body.Row.Item>?)!!
        if (listOfLanguage != null && listOfLanguage.size > 0) {
//            listOfLanguage.removeAt(0)
//            listOfLanguage.removeAt(0)

//            val lastIndex=listOfLanguage?.size-1
//            if(lastIndex<listOfLanguage.size&&lastIndex!=-1){
//                listOfLanguage.removeAt(lastIndex)
//            }


            tv_select_language.setText(languageRespModel.data?.body?.transliteration?.headding?.en)
            tv_sub_title.setText(languageRespModel.data?.body?.transliteration?.subheadding?.en)
            if(isFromGenSetting){
                tv_next.setText(getString(R.string.profile_str_43))
                ivNextBtnIcon?.setImageDrawable(this.faDrawable(R.string.icon_save, R.color.colorWhite))
            }else{
                tv_next.setText(getString(R.string.profile_str_43))
                ivNextBtnIcon?.setImageDrawable(this.faDrawable(R.string.icon_save, R.color.colorWhite))
            }

            //setUpUI()
            initRecyclerView()
        }
    }


    private fun setUpViewModel(isFromGenMusicSetting: Int) {
        if (ConnectionUtil(this@ChooseMusicLanguageActivity).isOnline) {
            chooseLanguageViewModel = ViewModelProvider(
                this
            ).get(ChooseLanguageViewModel::class.java)
            chooseLanguageViewModel?.getMusicLanguageList(this@ChooseMusicLanguageActivity,
                isFromGenMusicSetting
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            pb_progress_lan.visibility=View.GONE
                            setData(it?.data!!)
                        }

                        Status.LOADING ->{
                            pb_progress_lan.visibility=View.VISIBLE
                        }

                        Status.ERROR ->{
                            pb_progress_lan.visibility=View.GONE
                            Utils.showSnakbar(this,recyclerLanguage, true, it.message!!)
                        }
                    }
                })
        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    private fun saveLanguageData(){
        HungamaMusicApp.getInstance().deleteCacheData()
        if(listOfLanguageNameSelected.size >= 1){
            if (ConnectionUtil(this).isOnline) {
                if (isFromGenMusicSetting == EXTRA_MUSIC_SETTING){
                    SharedPrefHelper.getInstance().setMusicLanguageCodeList(convertArrayToString(listOfLanguageNameSelected))
                    SharedPrefHelper.getInstance().setMusicLanguageTitleList(convertArrayToString(listOfLanguageTitleSelected))

                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,Utils.arrayToString(listOfLanguageTitleSelected))
                    dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)

                    EventManager.getInstance().sendEvent(LanguageSelectEvent(dataMap))

                    val userDataMap= java.util.HashMap<String, String>()
                    userDataMap.put(EventConstant.LANGUAGES_SELECTED,Utils.arrayToString(listOfLanguageTitleSelected))
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))

                }else if (isFromGenMusicSetting == EXTRA_VIDEO_SETTING){
                    SharedPrefHelper.getInstance().setVideoLanguageCodeList(convertArrayToString(listOfLanguageNameSelected))
                    SharedPrefHelper.getInstance().setVideoLanguageTitleList(convertArrayToString(listOfLanguageTitleSelected))

                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,""+Utils.arrayToString(listOfLanguageTitleSelected))
                    dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
                    EventManager.getInstance().sendEvent(MoviesLanguageSelectedEvent(dataMap))

                    val userDataMap= java.util.HashMap<String, String>()
                    userDataMap.put(EventConstant.VIDEO_LANGUAGE_SELECTED,Utils.arrayToString(listOfLanguageTitleSelected))
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                }
                if(isFromGenSetting){
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            val dataMap=HashMap<String,String>()
                            dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,Utils.arrayToString(listOfLanguageTitleSelected))
                            dataMap.put(EventConstant.SOURCE_EPROPERTY,"settings")
                            EventManager.getInstance().sendEvent(LanguageChangedEvent(dataMap))
                        }

                    }catch (e:Exception){

                    }
                    val intent=Intent()
                    setResult(Activity.RESULT_OK, intent);
                    finish()
                }else{
                    //set Local
                    val intent:Intent
                    intent = Intent(this@ChooseMusicLanguageActivity, EnterMobileNumberActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.enter, R.anim.exit)
                    finish()
                }
            }else{
                val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
            }
        }else{
            val messageModel = MessageModel(getString(R.string.popup_str_55), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this, messageModel)
        }
    }
}