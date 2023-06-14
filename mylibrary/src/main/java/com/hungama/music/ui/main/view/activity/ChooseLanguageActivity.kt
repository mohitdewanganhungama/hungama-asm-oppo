package com.hungama.music.ui.main.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.HungamaMusicApp
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.AppLanguageSelectedEvent
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.adapter.LanguageGridRecyclerAdapter
import com.hungama.music.model.LangItem
import com.hungama.music.model.LangRespModel
import com.hungama.music.ui.main.viewmodel.ChooseLanguageViewModel
import com.hungama.music.ui.main.viewmodel.MusicLanguageViewModel
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.eventreporter.LanguageChangedEvent
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.activity_choose_language.*
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.fragment_album_detail.*
import kotlinx.android.synthetic.main.layout_progress.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChooseLanguageActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.name
    var selectedLanguage:String = Constant.EN
    var selectedLanguageTitle:String = Constant.ENGLISH
    var selectedLangItem:LangItem?=null
    var chooseLanguageViewModel: ChooseLanguageViewModel? = null
    var musicLanguageListViewModel: MusicLanguageViewModel? = null
    var layoutManger: LinearLayoutManager? = null
    var listOfLanguage = ArrayList<LangItem>()
    var myLocale: Locale? = null
    var languageRespModel1: LangRespModel? = null
    var isFromGenSetting=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_language)
        setUpViewModel()
        selectedLanguage = SharedPrefHelper?.getInstance()?.getLanguage().toString()
        selectedLanguageTitle = SharedPrefHelper?.getInstance()?.getLanguageTitle().toString()


        Utils.setWindowProperty(this@ChooseLanguageActivity)

        if(intent?.hasExtra(Constant.EXTRA_IS_FROM_GEN_SETTING)!!){
            isFromGenSetting= intent?.getBooleanExtra(Constant.EXTRA_IS_FROM_GEN_SETTING,false)!!
        }else{
            isFromGenSetting=false
        }
        setLog("displayDiscover", "he_api-${CommonUtils.getFirebaseConfigHEAPIData()}")
        CommonUtils.applyButtonTheme(this, ll_next)
        // fill otp and call the on click on button
        ll_next.setOnClickListener {
            try {
                HungamaMusicApp.getInstance().deleteCacheData()
                setLog("displayDiscover", "he_api-${CommonUtils.getFirebaseConfigHEAPIData()}")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(this, ll_next!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
                val dataMap= java.util.HashMap<String, String>()
                dataMap[EventConstant.LANGUAGECHOSEN_EPROPERTY] = selectedLangItem?.title!!
                dataMap[EventConstant.SOURCE_EPROPERTY] = "settings"
                EventManager.getInstance().sendEvent(LanguageChangedEvent(dataMap))
            }catch (e:Exception){

            }
            if(!TextUtils.isEmpty(selectedLanguage)){
                if (ConnectionUtil(this).isOnline) {
                    if (selectedLangItem != null) {
                        SharedPrefHelper.getInstance().saveLanguageObject(
                            PrefConstant.LANG_DATA,
                            selectedLangItem!!
                        )
                    }
                    SharedPrefHelper.getInstance().setLanguage(selectedLanguage)
                    SharedPrefHelper.getInstance().setLanguageTitle(selectedLanguageTitle)
                    SharedPrefHelper.getInstance().save(PrefConstant.CHOOES_LANGUAGE, false)
                    saveUserLangPref("APPLANG ", selectedLanguage)

                    val userDataMap=HashMap<String,String>()
                    userDataMap.put(EventConstant.APP_LANGUAGE,""+selectedLangItem?.title)
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))

                    if (isFromGenSetting) {
                        val intent = Intent()
                        setResult(9001, intent)
                        finish()
                    } else {
                        val intent:Intent
                        if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                            intent = Intent(this@ChooseLanguageActivity, MainActivity::class.java)
                        }else{
                            intent = Intent(this@ChooseLanguageActivity, LoginMainActivity::class.java)
                        }
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter, R.anim.exit)
                        finish()
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        if(isFromGenSetting){
                            val dataMap=HashMap<String,String>()
                            dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,""+selectedLangItem?.title)
                            dataMap.put(EventConstant.SOURCE_EPROPERTY,"settings")
                            EventManager.getInstance().sendEvent(AppLanguageSelectedEvent(dataMap))
                        }else{
                            val dataMap=HashMap<String,String>()
                            dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,""+selectedLangItem?.title)
                            dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
                            EventManager.getInstance().sendEvent(AppLanguageSelectedEvent(dataMap))

                        }
                    }





                }else{
                    //Utils.showSnakbar(parentMobile, false, getString(R.string.please_check_your_internet_conncetion))
                    val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
                    CommonUtils.showToast(this, messageModel)
                }
            }else{
                val messageModel = MessageModel(getString(R.string.login_str_55), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel)
            }
        }

    }

    fun setLocale(localeName: String) {
        myLocale = Locale(localeName)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        onConfigurationChanged(conf);

        var title: String = ""
        var subtitle: String= ""
        var next: String= ""
        when(localeName) {
            "en" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.en.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.en.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.en.toString()
            }
            "hi" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.hi.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.headding?.hi.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.hi.toString()
            }
            "mr" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.mr.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.headding?.mr.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.mr.toString()
            }
            "ml" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.ml.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.ml.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.ml.toString()
            }
            "ta" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.ta.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.ta.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.ta.toString()
            }
            "te" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.te.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.te.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.te.toString()
            }
            "kn" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.kn.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.kn.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.kn.toString()
            }
            "gu" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.gu.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.gu.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.gu.toString()
            }
            "bn" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.bn.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.bn.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.bn.toString()
            }
            "pa" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.pa.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.pa.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.pa.toString()
            }
            "ml" -> {
                title = languageRespModel1?.data?.body?.transliteration?.headding?.ml.toString()
                subtitle = languageRespModel1?.data?.body?.transliteration?.subheadding?.ml.toString()
                next = languageRespModel1?.data?.body?.transliteration?.next?.ml.toString()
            }
        }

        tv_select_language.setText(title)
        tv_sub_title.setText(subtitle)
        if(isFromGenSetting){
            tv_next.setText(getString(R.string.profile_str_43))
            ivNextBtnIcon?.setImageDrawable(this.faDrawable(R.string.icon_save, R.color.colorWhite))
            setLog("TAG"," data:${SharedPrefHelper.getInstance().getLanguageObject(PrefConstant.LANG_DATA)?.code}")

        }else{
            tv_next.setText(next)
            ivNextBtnIcon?.setImageDrawable(this.faDrawable(R.string.icon_next, R.color.colorWhite))
        }


    }

    private fun initRecyclerView() {
        recyclerLanguage.layoutManager = GridLayoutManager(this,2)

        //This will for default android divider
        //recyclerLanguage.addItemDecoration(GridItemDecoration(10, 2))

        val recyclerAdapter = LanguageGridRecyclerAdapter(this).apply {
            itemClick = { movieTitle ->
                setLog(">>>>>>>>>",movieTitle?.code!!)
                if(movieTitle.code != null){
                    selectedLangItem=movieTitle
                    selectedLanguage=movieTitle.code
                    selectedLanguageTitle = movieTitle.title!!
                    setLog("TAG","data $selectedLanguage")
                    setLocale(selectedLanguage)
                    listOfLanguage.forEach{it ->
                        if(it.code.equals(selectedLanguage)){
                            it.isSelected=true
                        }else{
                            it.isSelected=false
                        }
                        Handler(Looper.getMainLooper()).post {
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        recyclerLanguage.adapter = recyclerAdapter
        recyclerAdapter.setLanguageList(listOfLanguage)

    }

    private fun setData(languageRespModel: LangRespModel) {
        languageRespModel1 = languageRespModel
        listOfLanguage =
            (languageRespModel.data?.body?.rows?.get(0)?.items as ArrayList<LangItem>?)!!
        if (listOfLanguage != null && listOfLanguage.size > 0) {

            listOfLanguage?.forEach {
                if(it.code.equals(SharedPrefHelper.getInstance().getLanguage(),true)){
                    selectedLangItem=it
                    it.isSelected=true
                }
            }


            tv_select_language.setText(languageRespModel.data?.body?.transliteration?.headding?.en)
            tv_sub_title.setText(languageRespModel.data?.body?.transliteration?.subheadding?.en)
            if(isFromGenSetting){
                tv_next.setText(getString(R.string.profile_str_43))
                ivNextBtnIcon?.setImageDrawable(this.faDrawable(R.string.icon_save, R.color.colorWhite))
            }else{
                tv_next.setText(languageRespModel.data?.body?.transliteration?.next?.en)
                ivNextBtnIcon?.setImageDrawable(this.faDrawable(R.string.icon_next, R.color.colorWhite))
            }
            //setUpUI()
            initRecyclerView()
        }
    }

    private fun setUpViewModel() {


        musicLanguageListViewModel = ViewModelProvider(
            this
        ).get(MusicLanguageViewModel::class.java)

        if (ConnectionUtil(this@ChooseLanguageActivity).isOnline) {
            chooseLanguageViewModel = ViewModelProvider(
                this
            ).get(ChooseLanguageViewModel::class.java)

            chooseLanguageViewModel?.getLanguageData(this@ChooseLanguageActivity)?.observe(this,
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
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }

    }



    private fun saveUserLangPref(type:String, preference: String) {
        if (ConnectionUtil(this@ChooseLanguageActivity).isOnline) {

            try {
                val mainJson = JSONObject()
                val prefArrays = JSONArray()
                prefArrays.put(preference)

                mainJson.put("type", type)
                mainJson.put("preference", prefArrays)


                musicLanguageListViewModel?.saveUserPref(
                    this@ChooseLanguageActivity,
                    mainJson.toString()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(this,
                    parentMobile!!,
                    false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }
}