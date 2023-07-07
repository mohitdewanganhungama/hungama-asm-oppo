package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.LanguageSelectEvent
import com.hungama.music.eventanalytic.eventreporter.MoviesGenreSelectedEvent
import com.hungama.music.eventanalytic.eventreporter.MoviesLanguageSelectedEvent
import com.hungama.music.ui.main.adapter.MusicLanguageSelectionAdapter
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.data.model.MusicLanguageSelectionModel
import com.hungama.music.utils.CommonUtils.getNextOnboardingScreen
import com.hungama.music.ui.main.viewmodel.MusicLanguageViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.language_artist_selecter_sheet.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class LanguageArtistSelectBottomSheetFragment(var type:Int) : SuperBottomSheetFragment(),
    TextWatcher {
    var musicLanguageListViewModel: MusicLanguageViewModel? = null
    var myLocale: Locale? = null

    var userPrefType1=HashMap<String,String>()
    var userPrefType2=HashMap<String,String>()
    var userPrefType3=HashMap<String,String>()
    var userPrefType4=HashMap<String,String>()
    var userViewModel: UserViewModel? = null
    var adapter: MusicLanguageSelectionAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //hideSystemNavigationBar()
        setUpMusicLanguageListViewModel()
        return inflater.inflate(R.layout.language_artist_selecter_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyButtonTheme(requireContext(), llSave)
        /*val searchIcon = country_search.findViewById<ImageView>(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE)
        val cancelIcon = country_search.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(Color.WHITE)
        val textView = country_search.findViewById<TextView>(R.id.search_src_text)
        textView.setTextColor(Color.WHITE)
        textView.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.half_opacity_white_color))*/
        country_search?.addTextChangedListener(this@LanguageArtistSelectBottomSheetFragment)
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    //override fun isSheetCancelableOnTouchOutside(): Boolean = false
    //override fun isSheetCancelable(): Boolean = false
    //override fun isCancelable(): Boolean = false
    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_500).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int = requireContext().resources.getColor(R.color.transparent)

    private fun setUpMusicLanguageListViewModel() {
        if (ConnectionUtil(context).isOnline) {
            musicLanguageListViewModel = ViewModelProvider(
                this
            ).get(MusicLanguageViewModel::class.java)

            if(type == 1){
                musicLanguageListViewModel?.getMusicLanguageList(requireContext())?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setMusicLanguageListData(it?.data!!)
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                //Utils.showSnakbar(requireView(), true, it.message!!)
                            }
                        }
                    })
            }else if(type == 2){
                musicLanguageListViewModel?.getMusicArtistList(requireContext())?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setMusicLanguageListData(it?.data!!)
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                //Utils.showSnakbar(requireView(), true, it.message!!)
                            }
                        }
                    })
            }else if(type == 3){
                musicLanguageListViewModel?.getVideoLanguageList(requireContext())?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setMusicLanguageListData(it?.data!!)
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                //Utils.showSnakbar(requireView(), true, it.message!!)
                            }
                        }
                    })
            }else if(type == 4){
                musicLanguageListViewModel?.getVideoGenreList(requireContext())?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setMusicLanguageListData(it?.data!!)
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                //Utils.showSnakbar(requireView(), true, it.message!!)
                            }
                        }
                    })
            }else{
                musicLanguageListViewModel?.getMusicLanguageList(requireContext())?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setMusicLanguageListData(it?.data!!)
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
            progress?.visibility = View.VISIBLE
        } else {
            progress?.visibility = View.GONE
        }
    }


    fun setMusicLanguageListData(musicLanguageSelectionModel: MusicLanguageSelectionModel) {



        llSave.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llSave!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (type == 1){
                if (userPrefType1.size >= 1){
                    saveUserType1(Constant.APPMUSICLANG,userPrefType1.keys)
                    SharedPrefHelper.getInstance().setMusicLanguage(true)
                    saveAndNextScreen()


                    //SharedPrefHelper.getInstance().save(Constant.APPMUSICLANG,Utils.convertArrayToString(userPrefType1.keys?.toList()))
                    SharedPrefHelper?.getInstance()?.setMusicLanguageCodeList(
                        Utils.convertArrayToString(
                            userPrefType1.keys?.toList()
                        )
                    )
                    SharedPrefHelper?.getInstance()?.setMusicLanguageTitleList(
                        Utils.convertArrayToString(
                            userPrefType1.values.toList()
                        )
                    )
                    setLog("MusicLlang", SharedPrefHelper.getInstance().getMusicLanguageCodeList().toString())
                    setLog("MusicLlangTitle", SharedPrefHelper.getInstance().getMusicLanguageTitleList().toString())
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,Utils.arrayToString(userPrefType1.values?.toList()))
                    dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)

                    EventManager.getInstance().sendEvent(LanguageSelectEvent(dataMap))

                    val userDataMap= java.util.HashMap<String, String>()
                    userDataMap.put(EventConstant.LANGUAGES_SELECTED,Utils.arrayToString(userPrefType1.values?.toList()))
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                }else{
                    val messageModel = MessageModel(getString(R.string.popup_str_55), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }

            }else if (type == 2){
                saveUserType1(Constant.APPARTIST,userPrefType2.keys)
                SharedPrefHelper.getInstance().setMusicArtist(true)
                saveAndNextScreen()

                SharedPrefHelper.getInstance().save(Constant.APPARTIST,Utils.convertArrayToString(userPrefType2.keys?.toList()))

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.ARTIST_SELECTED,  Utils.arrayToString(userPrefType2.values?.toList()))
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))

            }else if (type == 3){
                if (userPrefType3.size >= 1){
                    saveUserType1(Constant.APPVIDEOLANG,userPrefType3.keys)
                    SharedPrefHelper.getInstance().setVideoLanguage(true)
                    saveAndNextScreen()

                    //SharedPrefHelper.getInstance().save(Constant.APPVIDEOLANG,Utils.convertArrayToString(userPrefType3.keys?.toList()))
                    SharedPrefHelper?.getInstance()?.setVideoLanguageCodeList(Utils.convertArrayToString(userPrefType3.keys?.toList()))
                    SharedPrefHelper?.getInstance()?.setVideoLanguageTitleList(Utils.convertArrayToString(userPrefType3.values.toList()))
                    setLog(TAG, "setMusicLanguageListData: share prefrences"+SharedPrefHelper?.getInstance()?.setVideoLanguageTitleList((Utils.convertArrayToString(userPrefType3.values.toList()))))
                    setLog("VideoLlang", SharedPrefHelper.getInstance().getVideoLanguageCodeList().toString())
                    setLog("VideoLlangTitle", SharedPrefHelper.getInstance().getVideoLanguageTitleList().toString())
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,Utils.arrayToString(userPrefType3.values?.toList()))
                    dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
                    EventManager.getInstance().sendEvent(MoviesLanguageSelectedEvent(dataMap))

                    val userDataMap= java.util.HashMap<String, String>()
                    userDataMap.put(EventConstant.VIDEO_LANGUAGE_SELECTED,Utils.arrayToString(userPrefType3.values?.toList()))
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                }else{
                    val messageModel = MessageModel(getString(R.string.popup_str_55), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }

            }else if (type == 4){
                saveUserType1(Constant.APPVIDEOGENRE,userPrefType4.keys)
                SharedPrefHelper.getInstance().setVideoGenre(true)
                saveAndNextScreen()

                SharedPrefHelper.getInstance().save(Constant.APPVIDEOGENRE,Utils.convertArrayToString(userPrefType4.keys?.toList()))

                val dataMap=HashMap<String,String>()
                dataMap.put(EventConstant.LANGUAGECHOSEN_EPROPERTY,""+Utils.arrayToString(userPrefType4.values?.toList()))
                dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
                EventManager.getInstance().sendEvent(MoviesGenreSelectedEvent(dataMap))

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.VIDEO_GENRE_SELECTED, ""+ Utils.arrayToString(userPrefType4.values?.toList()))
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            }

        }

        llAskLater.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llAskLater!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (type == 1){
                SharedPrefHelper.getInstance().setMusicLanguage(true)
                SharedPrefHelper.getInstance().setMusicArtist(true)
                saveAndNextScreen()
            }else if (type == 2){
                SharedPrefHelper.getInstance().setMusicArtist(true)
                saveAndNextScreen()
            }else if (type == 3) {
                SharedPrefHelper.getInstance().setVideoLanguage(true)
                SharedPrefHelper.getInstance().setVideoGenre(true)
                saveAndNextScreen()
            }else if (type == 4) {
                SharedPrefHelper.getInstance().setVideoGenre(true)
                saveAndNextScreen()
            }
        }


        if (musicLanguageSelectionModel != null && musicLanguageSelectionModel?.data != null) {
            val lang = SharedPrefHelper.getInstance().getLanguage()

            if (musicLanguageSelectionModel.data != null &&
                !musicLanguageSelectionModel.data?.body?.rows.isNullOrEmpty() &&
                !musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items.isNullOrEmpty()) {
                Utils.setMargins(rvSelection, resources.getDimensionPixelSize(R.dimen.dimen_18))
                if (type == 1){
                    setLocale(lang!!, musicLanguageSelectionModel.data?.body!!)
                    rlSearch.visibility = View.GONE
                    /*tvMusicLanguage.text = musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.heading
                    tvSelectAtLeast.text = musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.subheadding*/

                    rvSelection.apply {
                        layoutManager =
                            GridLayoutManager(requireContext(), 2)
                        adapter = MusicLanguageSelectionAdapter(type, requireContext(), musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items,
                            object : MusicLanguageSelectionAdapter.OnChildItemClick {
                                override fun onUserClick(childPosition: Int, selected: Boolean) {
                                    if(selected){
                                        userPrefType1.put(musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.code!!,musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.title!!)

                                    }else{
                                        if(userPrefType1.containsKey(musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.code!!)){
                                            userPrefType1.remove(musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.code!!)
                                        }
                                    }



                                    setLog("TAG", "type 1:"+userPrefType1)
                                }


                            })
                        setRecycledViewPool(RecyclerView.RecycledViewPool())
                        setHasFixedSize(true)
                    }
                    //rvSelection?.addItemDecoration(GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.dimen_85), false, requireContext(), 2))
                }else if (type == 2){
                    rlSearch.visibility = View.VISIBLE
                    if (musicLanguageSelectionModel?.data != null &&
                        musicLanguageSelectionModel?.data?.body != null &&
                        !musicLanguageSelectionModel?.data?.body?.rows.isNullOrEmpty()){
                        tvMusicLanguage.text = musicLanguageSelectionModel.data?.body?.rows?.get(0)?.heading
                        tvSelectAtLeast.text = musicLanguageSelectionModel.data?.body?.rows?.get(0)?.subheadding
                        if (tvSelectAtLeast != null){
                            tvSelectAtLeast.visibility = View.VISIBLE
                        }else{
                            tvSelectAtLeast.visibility = View.VISIBLE
                        }
                    }else{
                        tvMusicLanguage.visibility = View.INVISIBLE
                        tvSelectAtLeast.visibility = View.GONE
                    }

                    if (musicLanguageSelectionModel?.data != null &&
                        musicLanguageSelectionModel.data?.body != null &&
                        !musicLanguageSelectionModel.data?.body?.rows.isNullOrEmpty() &&
                        musicLanguageSelectionModel.data?.body?.rows?.size!! > 1){
                        btnAsk.text = musicLanguageSelectionModel.data?.body?.rows?.get(1)?.items?.get(0)?.headding
                        btnSave.text = musicLanguageSelectionModel.data?.body?.rows?.get(1)?.items?.get(1)?.headding
                        ivAsk?.visibility = View.GONE
                        ivSave?.visibility = View.GONE
                    }else{
                        tvMusicLanguage.visibility = View.INVISIBLE
                        tvSelectAtLeast.visibility = View.INVISIBLE
                        ivAsk?.visibility = View.GONE
                        ivSave?.visibility = View.GONE
                    }

                    Utils.setMarginsEnd(rvSelection, resources.getDimensionPixelSize(R.dimen.dimen_10))
                    Utils.setMarginsTop(rvSelection, resources.getDimensionPixelSize(R.dimen.dimen_18))
                    rvSelection.apply {
                        layoutManager =
                            GridLayoutManager(requireContext(), 3)
                        setRecycledViewPool(RecyclerView.RecycledViewPool())
                        setHasFixedSize(true)
                    }

                    adapter = MusicLanguageSelectionAdapter(type, requireContext(), musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items,
                        object : MusicLanguageSelectionAdapter.OnChildItemClick {
                            override fun onUserClick(childPosition: Int, selected: Boolean) {
                                if(selected){
                                    userPrefType2.put(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.id!!,musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.data?.title!!)
                                }else{
                                    if(userPrefType2.containsKey(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.id!!)){
                                        userPrefType2.remove(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.id!!)
                                    }
                                }
                                setLog("TAG", "type 2:"+userPrefType2)
                                setFollowUnFollow(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data!!, selected)
                            }


                        })
                    rvSelection?.adapter = adapter
                    //rvSelection?.addItemDecoration(GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.dimen_85), false, requireContext(), 2))
                }else if (type == 3){
                    setLocale(lang!!, musicLanguageSelectionModel.data?.body!!)
                    rlSearch.visibility = View.GONE
                    /*tvMusicLanguage.text = musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.heading
                    tvSelectAtLeast.text = musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.subheadding*/

                    rvSelection.apply {
                        layoutManager =
                            GridLayoutManager(requireContext(), 2)
                        adapter = MusicLanguageSelectionAdapter(type, requireContext(), musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items,
                            object : MusicLanguageSelectionAdapter.OnChildItemClick {
                                override fun onUserClick(childPosition: Int, selected: Boolean) {
                                    if(selected){
                                        userPrefType3.put(musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.code!!,musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.title!!)
                                    }else{
                                        if(userPrefType3.containsKey(musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.code!!)){
                                            userPrefType3.remove(musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.code!!)
                                        }
                                    }
                                    setLog("TAG", "type 3:"+userPrefType3)
                                    setLog("TAG", "type 2:"+userPrefType2)

                                }


                            })
                        setRecycledViewPool(RecyclerView.RecycledViewPool())
                        setHasFixedSize(true)
                    }
                    //rvSelection?.addItemDecoration(GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.dimen_85), false, requireContext(), 2))
                }else if (type == 4){
                    setLocale(lang!!, musicLanguageSelectionModel.data?.body!!)
                    rlSearch.visibility = View.GONE
                    /*tvMusicLanguage.text = musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.heading
                    tvSelectAtLeast.text = musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.subheadding*/

                    rvSelection.apply {
                        layoutManager =
                            GridLayoutManager(requireContext(), 2)
                        adapter = MusicLanguageSelectionAdapter(type, requireContext(), musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items,
                            object : MusicLanguageSelectionAdapter.OnChildItemClick {
                                override fun onUserClick(childPosition: Int, selected: Boolean) {
                                    if(selected){
                                        userPrefType4.put(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.title!!,musicLanguageSelectionModel.data!!.body!!.rows!![0]!!.items?.get(childPosition)?.title!!)
                                    }else{
                                        if(userPrefType4.containsKey(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.title!!)){
                                            userPrefType4.remove(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.title!!)
                                        }
                                    }
                                    if (musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.id != null && musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.type != null){
                                        setAddOrRemoveFavourite(musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.id!!,
                                            musicLanguageSelectionModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.type.toString(), selected)
                                    }
                                    setLog("TAG", "type 1:"+userPrefType4)
                                }


                            })
                        setRecycledViewPool(RecyclerView.RecycledViewPool())
                        setHasFixedSize(true)
                    }
                    //rvSelection?.addItemDecoration(GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.dimen_85), false, requireContext(), 2))
                }
                bottomButtonView?.show()
//                bottomView?.show()
            }else{
                closeDialog()
            }
        }else{
            closeDialog()
        }
    }

    private fun saveUserType1(type:String, preference: MutableSet<String>) {
        if (ConnectionUtil(requireActivity()).isOnline) {

            try {
                val mainJson = JSONObject()
                val prefArrays = JSONArray()

                preference.forEach {
                    prefArrays.put(it)
                }


                setLog("TAG", "saveUserType1: "+preference)
                setLog("TAG", "saveUserType Array: "+prefArrays)
                mainJson.put("type", type)
                mainJson.put("preference", prefArrays)


                musicLanguageListViewModel?.saveUserPref(
                    requireActivity(),
                    mainJson.toString()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(requireContext(),requireView()!!, false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setLocale(localeName: String, body: MusicLanguageSelectionModel.Data.Body ) {
        myLocale = Locale(localeName)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)

        var title: String = ""
        var subtitle: String= ""
        var ask:String = ""
        var save:String = ""
        when(localeName) {
            "en" -> {
                title = body.transliteration?.headding?.en.toString()
                subtitle = body.transliteration?.subheadding?.en.toString()
                ask = body.transliteration?.asklater!!.en.toString()
                save = body.transliteration?.save!!.en.toString()
            }
            "hi" -> {
                title = body.transliteration?.headding?.hi.toString()
                subtitle = body.transliteration?.headding?.hi.toString()
                ask = body.transliteration?.asklater!!.hi.toString()
                save = body.transliteration?.save!!.hi.toString()
            }
            "mr" -> {
                title = body.transliteration?.headding?.mr.toString()
                subtitle = body.transliteration?.headding?.mr.toString()
                ask = body.transliteration?.asklater!!.mr.toString()
                save = body.transliteration?.save!!.mr.toString()
            }
            "ml" -> {
                title = body.transliteration?.headding?.ml.toString()
                subtitle = body.transliteration?.subheadding?.ml.toString()
                ask = body.transliteration?.asklater!!.ml.toString()
                save = body.transliteration?.save!!.ml.toString()
            }
            "ta" -> {
                title = body.transliteration?.headding?.ta.toString()
                subtitle = body.transliteration?.subheadding?.ta.toString()
                ask = body.transliteration?.asklater!!.ta.toString()
                save = body.transliteration?.save!!.ta.toString()
            }
            "te" -> {
                title = body.transliteration?.headding?.te.toString()
                subtitle = body.transliteration?.subheadding?.te.toString()
                ask = body.transliteration?.asklater!!.te.toString()
                save = body.transliteration?.save!!.te.toString()
            }
            "kn" -> {
                title = body.transliteration?.headding?.kn.toString()
                subtitle = body.transliteration?.subheadding?.kn.toString()
                ask = body.transliteration?.asklater!!.kn.toString()
                save = body.transliteration?.save!!.kn.toString()
            }
            "gu" -> {
                title = body.transliteration?.headding?.gu.toString()
                subtitle = body.transliteration?.subheadding?.gu.toString()
                ask = body.transliteration?.asklater!!.gu.toString()
                save = body.transliteration?.save!!.gu.toString()
            }
            "bn" -> {
                title = body.transliteration?.headding?.bn.toString()
                subtitle = body.transliteration?.subheadding?.bn.toString()
                ask = body.transliteration?.asklater!!.bn.toString()
                save = body.transliteration?.save!!.bn.toString()
            }
            "pa" -> {
                title = body.transliteration?.headding?.pa.toString()
                subtitle = body.transliteration?.subheadding?.pa.toString()
                ask = body.transliteration?.asklater!!.pa.toString()
                save = body.transliteration?.save!!.pa.toString()
            }
            "ml" -> {
                title = body.transliteration?.headding?.ml.toString()
                subtitle = body.transliteration?.subheadding?.ml.toString()
                ask = body.transliteration?.asklater!!.ml.toString()
                save = body.transliteration?.save!!.ml.toString()
            }
            else ->{
                title = body.transliteration?.headding?.en.toString()
                subtitle = body.transliteration?.subheadding?.en.toString()
                ask = body.transliteration?.asklater!!.en.toString()
                save = body.transliteration?.save!!.en.toString()
            }
        }


        tvMusicLanguage.text = title
        tvSelectAtLeast.text = subtitle
        if(TextUtils.isEmpty(ask)){
            btnAsk.text = getString(R.string.popup_str_57)
        }else{
            btnAsk.text = ask
        }

        if(TextUtils.isEmpty(save)){
            btnSave.text = getString(R.string.profile_str_43)
        }else{
            btnSave.text = save
        }
        ivAsk?.visibility = View.GONE
        ivSave?.visibility = View.GONE



    }

    fun saveAndNextScreen(){
        val nextScreen = getNextOnboardingScreen(1)
        if (nextScreen > 0){
            type = nextScreen
            setUpMusicLanguageListViewModel()
        }else{
            closeDialog()
        }
    }
    private fun setFollowUnFollow(data: MusicLanguageSelectionModel.Data.Body.Row.Item.Data, isFollowing: Boolean) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            jsonObject.put("followingId",data?.id)
            jsonObject.put("follow",isFollowing)
            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())

            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId",data?.id)
            jsonObject1.put("typeId",""+data?.type)
            jsonObject1.put("action",isFollowing)
            jsonObject1.put("module", Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())
        }/* else {
            Utils.showSnakbar(
                requireView(),
                false,
                getString(R.string.discover_str_4)
            )
        }*/
    }

    fun setAddOrRemoveFavourite(contentId: String?, type: String?, isFavourite: Boolean) {
        var userViewModelBookmark = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            jsonObject.put("contentId", contentId)
            jsonObject.put("typeId", type)
            jsonObject.put("action",isFavourite)
            jsonObject.put("module", Constant.MODULE_FAVORITE)
            userViewModelBookmark?.callBookmarkApi(requireContext(), jsonObject.toString())
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(p0: Editable?) {
        val searchText = country_search?.text.toString().trim()
        adapter?.filter?.filter(searchText)
    }
}

