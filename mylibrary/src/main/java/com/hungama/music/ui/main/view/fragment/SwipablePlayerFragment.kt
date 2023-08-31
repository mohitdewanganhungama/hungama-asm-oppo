package com.hungama.music.ui.main.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.View.OnClickListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.Player
import androidx.viewpager2.widget.ViewPager2
import com.amplitude.api.Amplitude
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.SongDurationConfigModel
import com.hungama.music.R
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SwipablePlayerPagerAdapter
import com.hungama.music.ui.main.view.activity.LoginMainActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.activity.PaymentWebViewActivity
import com.hungama.music.utils.*
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.getSongDurationConfig
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.SIGNIN_WITH_ALL
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.BuildConfig
import kotlinx.android.synthetic.main.fragment_swipable_player.*
import kotlinx.android.synthetic.main.free_minutes_exhausted.*
import kotlinx.android.synthetic.main.new_preview_model_layout.*
import kotlinx.android.synthetic.main.song_preview_model_layout.*
import kotlinx.android.synthetic.main.song_preview_model_layout.tnSeeAllPreview

import kotlinx.coroutines.*


class SwipablePlayerFragment : BaseFragment(), BaseActivity.OnLocalBroadcastEventCallBack, OnClickListener  {
    private var storiesPagerAdapter: SwipablePlayerPagerAdapter? = null
    var playerData:ArrayList<Track>? = arrayListOf()
    var ft: SongDurationConfigModel.Ft? = null
    var nonft: SongDurationConfigModel.Nonft? = null
    var nudge_minute_ft: SongDurationConfigModel.Ft? = null
    var nudge_minute_nonft: SongDurationConfigModel.Nonft? = null
    var button_text2 = ""
    var button_text = ""
    var isBuyGoldClicked = true
    var isClickedPay = false
    var is_perview = false
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playableContentViewModel1: PlayableContentViewModel = PlayableContentViewModel()
    private val mainScope = CoroutineScope(Dispatchers.Main) + CoroutineName("SwipablePlayerFragment")
    private val ioScope = CoroutineScope(Dispatchers.IO) + CoroutineName("SwipablePlayerFragment")
    companion object {
        var currentViewPagerIndex = 0
        var action = false
    }
    override fun initializeComponent(view: View) {
        //requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setLog("alhglhal", "MusicPlayer2")
        playerData = BaseActivity.songDataList

        llBuyPlanExhausted.setOnClickListener(this)
        llBuyPlanPreview.setOnClickListener(this)
        llBuyPlanNewPreview.setOnClickListener(this)

        tnSeeAllNewPreview.setOnClickListener(this)
        tvSellAllExhausted.setOnClickListener(this)
        tnSeeAllPreview.setOnClickListener(this)
        newPreviewModel.setOnClickListener(null)
        songPreviewModel.setOnClickListener(null)
        includeFreeMinute.setOnClickListener(null)
        ButtonText()
        if (!playerData.isNullOrEmpty()) {
            mainScope.launch {
                storiesPagerAdapter = SwipablePlayerPagerAdapter(this@SwipablePlayerFragment, playerData!!)
                view_pager_stories?.adapter = storiesPagerAdapter
                currentViewPagerIndex = BaseActivity.nowPlayingCurrentIndex()
                //setLog("PlayNextSong", "SwipablePlayerFragment-NextSong-${playerData?.get(currentViewPagerIndex+1)?.title}")
                view_pager_stories?.setCurrentItem(currentViewPagerIndex, false)
                view_pager_stories?.registerOnPageChangeCallback(onPageChangeCallback)

                setLog(
                    "alhlghal", MainActivity.lastItemClicked + " " +
                            MainActivity.tempLastItemClicked + " " + MainActivity.headerItemName
                )

                playerData?.let {
                    if (it.isNotEmpty()) {
                        CommonUtils.PageViewEvent(
                            playerData!![currentViewPagerIndex].title.toString(),
                            Utils.getContentTypeNameForStream(playerData!![currentViewPagerIndex].contentType.toString()),
                            playerData!![currentViewPagerIndex].id.toString(),
                            MainActivity.lastItemClicked,
                            "" + MainActivity.tempLastItemClicked + "_" + MainActivity.headerItemName + MainActivity.subHeaderItemName,
                            "player_music", ""
                        )
                    }

                }

                MainActivity.subHeaderItemName = ""
            }

                if (activity != null){
                    val playerPlaybackStatus = (activity as MainActivity).audioPlayer?.playbackState
                    ioScope.launch {
                        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-initializeComponent-playerPlaybackStatus-$playerPlaybackStatus")
                        if (playerPlaybackStatus == Player.STATE_IDLE){
                            (activity as MainActivity).playContent(playerData!!, true)
                        }
                    }
                }
        }
        else{
            mainScope.launch {
                if (activity != null){
                    setLog("SwipablePlayerFragment", "SwipablePlayerFragment-initializeComponent()-playerData- null")
                    (activity as MainActivity).getRecommendedContentList()
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        BaseActivity.includeFreeMinute = includeFreeMinute
        BaseActivity.songPreviewModel = songPreviewModel
        BaseActivity.newPreviewModel = newPreviewModel
        BaseActivity.isSwipableActive = true
        BaseActivity.m_view_papger = view_pager_stories
    }

    override fun onClick(v: View) {
        super.onClick(v)
        var planId = "plan_id=3"
        when(v.id){

            R.id.llBuyPlanNewPreview,R.id.llBuyPlanExhausted,R.id.llBuyPlanPreview->{

                is_perview = v.id == R.id.llBuyPlanNewPreview

                if (!SharedPrefHelper.getInstance().isUserLoggedIn()) {
                    isBuyGoldClicked = true
                    action = true
                    val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                    intent.putExtra("action", SIGNIN_WITH_ALL)
                    startActivity(intent)
                } else {
                    if(is_perview) {

                        if (!CommonUtils.getNudgePlanId().isNullOrEmpty())
                            planId = "plan_id=" + CommonUtils.getNudgePlanId()
                    }else{
                        planId = "plan_id=" + CommonUtils.getNudgeMinuteQuotaExhaustedPlanId()
                    }
                    callPayApi(planId)
                }
                if(BaseActivity.player11 != null)
                {
                    if(BaseActivity.player11?.isPlaying == true)
                        BaseActivity.player11?.pause()
                }
                isClickedPay = true
            }
            R.id.tnSeeAllNewPreview,R.id.tnSeeAllPreview,R.id.tvSellAllExhausted->{
                is_perview = v.id== R.id.tnSeeAllNewPreview
                if (!SharedPrefHelper.getInstance().isUserLoggedIn()) {
                    action = true
                    isBuyGoldClicked = false
                    val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                    intent.putExtra("action", SIGNIN_WITH_ALL)
                    startActivity(intent)
                } else {
                    callPayApi("")
                }
                if(BaseActivity.player11 != null)
                {
                    if(BaseActivity.player11?.isPlaying == true)
                        BaseActivity.player11?.pause()
                }
                isClickedPay = true

            }
        }


    }


    fun callPayApi(url:String) {
        if (ConnectionUtil(activity).isOnline) {
            val intent = Intent(requireContext(), PaymentWebViewActivity::class.java)
            val genrtedURL = CommonUtils.genratePaymentPageURL(requireContext(), url)

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

    fun setUrlParameters(urll:String):String {
        var plan_type = ""
        var url = urll

        plan_type = "subscription"


        val auth = CommonUtils.md5(
            Constant.PRODUCT_KEY.toString() + ":" + SharedPrefHelper.getInstance().getUserId()
        )
        url += "auth=$auth"


        val identity = SharedPrefHelper.getInstance().getUserId()
        url += "&identity=$identity"

        val product_id = Constant.PRODUCT_ID
        url += "&product_id=$product_id"

        url += "&country=${Constant.DEFAULT_COUNTRY_CODE}"

        val platform_id = Constant.PLATFORM_ID
        url += "&platform_id=$platform_id"

        val upgradable = Constant.SUBSCRIPTION_UPGRADABLE
        url += "&upgradable=$upgradable"

        url += "&plan_type=$plan_type"

        val app_version = BuildConfig.VERSION_NAME
        url += "&app_version=$app_version"

        val build_number = BuildConfig.VERSION_CODE
        url += "&build_number=$build_number"


        val upiList = CommonUtils.getAllUpiEnabledAppList(requireContext())

        setLog("upiList", "upiList-$upiList")
//        url += "&upilist=${Utils.convertArrayToString(upiList)}"

        val hardware_id = CommonUtils.getDeviceId(requireContext())
        url += "&hardware_id=$hardware_id"

        url += "&content_id=$"

        var source = "swipablePlayer"

        if (!TextUtils.isEmpty(source)) {
            source = source.replace(" ", "_")
        }

        url += "&source=$source"

        setLog(com.hungama.music.utils.TAG, "pay url setUrlParameters: source:${source}")


        if (!url.contains("live_event_id", true)) {
            val live_event_id = ""
            url += "&live_event_id=$live_event_id"
        }

        if (!url.contains("aff_code", true)) {
            val aff_code = ""
            url += "&aff_code=$aff_code"
        }

        if (!url.contains("extra_data", true)) {
            val extra_data = ""
            url += "&extra_data=$extra_data"
        }

        if(!url?.contains("utm_source",true)!!){
            val utm_source = source
            url += "&utm_source=$utm_source"
        }

        if(!url?.contains("utm_medium",true)!!){
            val utm_medium = ""
            url += "&utm_medium=$utm_medium"
        }

        if(!url?.contains("utm_campaign",true)!!){
            val utm_campaign = ""
            url += "&utm_campaign=$utm_campaign"
        }


        val lang = SharedPrefHelper.getInstance().getLanguage()
        url += "&lang=$lang"

        val amplitude_user_id = Amplitude.getInstance().userId
        url += "&amp_user_id=$amplitude_user_id"

        val amplitude_device_id = Amplitude.getInstance().deviceId
        url += "&amp_device_id=$amplitude_device_id"
        url+= "&product=oppo"

        setLog("uyfuyfyigkkuytdf ", url)
        return url
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipable_player, container, false)
    }

    override fun onResume() {
        super.onResume()
        var planId = "plan_id=3"
        if(is_perview) {
            if (!CommonUtils.getNudgePlanId().isNullOrEmpty())
                planId = "plan_id=" + CommonUtils.getNudgePlanId()
        }else{
            planId = "plan_id=" + CommonUtils.getNudgeMinuteQuotaExhaustedPlanId()
        }

        setLog("alkhglhal", " $action $isClickedPay")

        if (!CommonUtils.isUserHasGoldSubscription() && SharedPrefHelper.getInstance().isUserLoggedIn() && action){
            val intent = Intent(requireActivity(), PaymentWebViewActivity::class.java)
            var payUrl = ""
            isClickedPay = true

            payUrl = if(isBuyGoldClicked)
                CommonUtils.genratePaymentPageURL(requireContext(), planId)
            else
                CommonUtils.genratePaymentPageURL(requireContext(), "")

            intent.putExtra("url", payUrl)
            startActivity(intent)
            (activity as BaseActivity).pausePlayer()

                if(BaseActivity.player11?.isPlaying == true)
                {
                    BaseActivity.player11?.pause()
                }

            action = false
        }
        else{
            if (isClickedPay && !CommonUtils.isUserHasGoldSubscription()){
                BaseActivity.setTouchData()
                currentViewPagerIndex = BaseActivity.nowPlayingCurrentIndex() + 1
                view_pager_stories?.setCurrentItem(currentViewPagerIndex, false)
                isClickedPay = false
            }
        }

        mainScope.launch {
            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onResume()")
            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onResume()-view_pager_stories.currentItem=${view_pager_stories?.currentItem}")
            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onResume()-BaseActivity.nowPlayingCurrentIndex()=${BaseActivity.nowPlayingCurrentIndex()}")
            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onResume()-currentViewPagerIndex=${currentViewPagerIndex}")

            if (activity != null){
                (activity as MainActivity).isMiniplayerVisible = false
                (activity as MainActivity).showBottomNavigationAndMiniplayerBlurView()
            }
        }
        setLocalBroadcast()
    }


    override fun onDestroy() {
        super.onDestroy()
        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onDestroy()")
        BaseActivity.isNewSwipablePlayerOpen = false
        if (activity != null) {
            (activity as BaseActivity).showBottomNavigationBar()
            (activity as BaseActivity).showMiniPlayer()
            (activity as BaseActivity).changeHomeBg(false)
            (activity as MainActivity).removePlayerProgressChangeEventCallBack()
        }
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
        mainScope.cancel()
        BaseActivity.isSwipableActive = false
    }

    override fun onDetach() {
        super.onDetach()
        BaseActivity.isSwipableActive = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(isAdded() && requireActivity()!=null){
            setLog("SwipablePlayerFragment", "onResume SwipablePlayerFragment-onHiddenChanged-$hidden")
            if(!hidden){
                setLocalBroadcast()
                if (activity != null){
                    mainScope.launch {
                        BaseActivity.isNewSwipablePlayerOpen = true
                        (activity as MainActivity).hideMiniPlayer()
                        (activity as MainActivity).hideStickyAds()
                    }

                    ioScope.launch{
                        (activity as BaseActivity).changeStatusbarcolor((activity as MainActivity).statusBarColor)
                    }
                }
                    if (view_pager_stories?.currentItem != BaseActivity.nowPlayingCurrentIndex()) {
                        currentViewPagerIndex = BaseActivity.nowPlayingCurrentIndex()
                        view_pager_stories?.setCurrentItem(currentViewPagerIndex, false)
                    }

                setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onHiddenChanged()-visible")
            }else{
                BaseActivity.isNewSwipablePlayerOpen = false
                (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
                ioScope.launch {
                    if (activity != null){
                        (activity as BaseActivity).changeHomeBg(false)
                    }
                    setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onHiddenChanged-gone-ioScope")
                }

                setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onHiddenChanged()-gone")
            }
        }
    }


    private fun setLocalBroadcast(){
        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-setLocalBroadcast")
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT))
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            setLog(TAG, "onLocalBroadcastEventCallBack: event:${event}")
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (activity != null){
                    mainScope.launch {
                        BaseActivity.isNewSwipablePlayerOpen = true
                        //setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack()-visible")
                        if ((activity as MainActivity).miniplayerHeight > 0 && !Utils.getCurrentFragment(requireContext())?.javaClass?.simpleName.equals(
                                QueueFragment().javaClass.simpleName, true)){
                            (activity as MainActivity).hideMiniPlayer()
                            (activity as MainActivity).hideStickyAds()
                        }
                        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-view_pager_stories.currentItem=${view_pager_stories?.currentItem}")
                        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-BaseActivity.nowPlayingCurrentIndex()=${BaseActivity.nowPlayingCurrentIndex()}")
                        val currentViewIndex = view_pager_stories?.currentItem
                        if (!playerData.isNullOrEmpty() && currentViewIndex != null && playerData?.size!! > currentViewIndex && playerData?.get(currentViewIndex)?.contentType == ContentTypes.Audio_Ad.value
                            && !BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex() && BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.contentType != ContentTypes.Audio_Ad.value){
                            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-removeContent=${playerData?.get(currentViewIndex)?.contentType}")
                            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-playerData.size1=${playerData?.size}")
                            playerData = BaseActivity.songDataList
                            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-playerData.size2=${playerData?.size}")
                            if (storiesPagerAdapter != null){
                                setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-playerData?.get(currentViewIndex)-${playerData?.get(currentViewIndex)?.id}")
                                //view_pager_stories?.isUserInputEnabled = true
                                //storiesPagerAdapter.updateDataList(playerData!!)
                                storiesPagerAdapter = SwipablePlayerPagerAdapter(this@SwipablePlayerFragment, playerData!!)
                                view_pager_stories?.adapter = storiesPagerAdapter
                                currentViewPagerIndex = BaseActivity.nowPlayingCurrentIndex()
                                view_pager_stories?.setCurrentItem(currentViewPagerIndex, false)
                            }
                        }else{
                            setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-else-playerData.size-${playerData?.size}-BaseActivity.songDataList-${BaseActivity.songDataList?.size}-BaseActivity.nowPlayingCurrentIndex()=${BaseActivity.nowPlayingCurrentIndex()}")
                            if (currentViewIndex != BaseActivity.nowPlayingCurrentIndex()){
                                if (!playerData.isNullOrEmpty() && !BaseActivity.songDataList.isNullOrEmpty()
                                    && playerData?.size != BaseActivity.songDataList?.size){
                                    playerData = BaseActivity.songDataList
                                    storiesPagerAdapter = SwipablePlayerPagerAdapter(this@SwipablePlayerFragment, playerData!!)
                                    view_pager_stories?.adapter = storiesPagerAdapter
                                    setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-else-ResetViewPager")
                                }
                                currentViewPagerIndex = BaseActivity.nowPlayingCurrentIndex()
                                view_pager_stories?.setCurrentItem(currentViewPagerIndex, false)
                            }
                        }
                    }
                }
            }else if (event == Constant.AUDIO_PLAYER_END_RESULT_CODE){
                ioScope.launch {
                    val isPlayerContentEnd = intent.getBooleanExtra(Constant.isPlayerContentEnd, false)
                    if (isPlayerContentEnd && !playerData.isNullOrEmpty() && !BaseActivity.songDataList.isNullOrEmpty()){
                        setLog("SwipablePlplayNextSong-songDataList.sizeayerFragment", "AUDIO_PLAYER_END_RESULT_CODE")
                        playerData = BaseActivity.songDataList
                        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack()-playerData.size2=${playerData?.size}")
                        if (storiesPagerAdapter != null){
                            playerData?.let { storiesPagerAdapter?.updateDataList(it) }
                        }
                    }
                }
            }else if (event == Constant.AUDIO_PLAYER_ADS_STARTED_RESULT_CODE){
                mainScope.launch {
                    view_pager_stories?.isUserInputEnabled = false
                    if (activity != null){
                        (activity as BaseActivity).hideBottomNavigationBar()
                    }
                }
            }else if (event == Constant.AUDIO_PLAYER_ADS_END_RESULT_CODE){
                mainScope.launch {
                    view_pager_stories?.isUserInputEnabled = true
                    if (activity != null){
                        (activity as BaseActivity).showBottomNavigationBar()
                    }
                }
            }else if (event == Constant.AUDIO_PLAYER_SHUFFLE_MODE_CHANGED_RESULT_CODE || event == Constant.AUDIO_PLAYER_SONG_LIST_CHANGED_RESULT_CODE){
                mainScope.launch {
                    view_pager_stories?.isUserInputEnabled = true
                    if (!playerData.isNullOrEmpty() && !BaseActivity.songDataList.isNullOrEmpty()){
                        playerData = BaseActivity.songDataList
                        /*storiesPagerAdapter = SwipablePlayerPagerAdapter(this@SwipablePlayerFragment, playerData!!)
                        view_pager_stories?.adapter = storiesPagerAdapter*/
                        if (storiesPagerAdapter != null){
                            playerData?.let { storiesPagerAdapter?.updateDataList(it) }
                        }else{
                            storiesPagerAdapter = SwipablePlayerPagerAdapter(this@SwipablePlayerFragment, playerData!!)
                            view_pager_stories?.adapter = storiesPagerAdapter
                        }
                        setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onLocalBroadcastEventCallBack-AUDIO_PLAYER_SHUFFLE_MODE_CHANGED_RESULT_CODE")
                    }
                    currentViewPagerIndex = BaseActivity.nowPlayingCurrentIndex()
                    view_pager_stories?.setCurrentItem(currentViewPagerIndex, false)
                }
            }
        }
    }

    val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            currentViewPagerIndex = position
            mainScope.launch {
                setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onPageScrolled-onPageScrolled-positionOffset-$positionOffset")
                if (positionOffset == 0.0F && !playerData.isNullOrEmpty() && playerData?.get(position)?.contentType == ContentTypes.Audio_Ad.value){
                    setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onPageScrolled-onPageScrolled-swipe disable")
                    view_pager_stories?.isUserInputEnabled = false
                }else{
                    setLog("SwipablePlayerFragment", "SwipablePlayerFragment-onPageScrolled-onPageScrolled-swipe enable")
                    view_pager_stories?.isUserInputEnabled = true
                }
            }
        }
    }


    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver-1", "SwipablePlayerFragment-mMessageReceiver-$intent")
            if (intent.hasExtra("EVENT")) {
                setLog(
                    "BroadcastReceiver-1",
                    "SwipablePlayerFragment-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                )
                onLocalBroadcastEventCallBack(context, intent)
            }
        }
    }

    fun ButtonText() {
        var planId =""
        var planId2 =""

        playableContentViewModel = ViewModelProvider(this).get(PlayableContentViewModel::class.java)
        playableContentViewModel1 = ViewModelProvider(this).get(PlayableContentViewModel::class.java)

             ft = getSongDurationConfig().nudge_minute_quota_exhausted.ft
             nonft = getSongDurationConfig().nudge_minute_quota_exhausted.nonft


        nudge_minute_ft = getSongDurationConfig().nudge_stream_preview.ft
        nudge_minute_nonft = getSongDurationConfig().nudge_stream_preview.nonft

        if (CommonUtils.isUserHasEliableFreeContent()) {
            planId = ft?.plan_id!!
            planId2 = nudge_minute_ft?.plan_id!!
        }else {
            planId = nonft?.plan_id!!
            planId2 = nudge_minute_nonft?.plan_id!!
        }

        if (ft != null || nonft != null) {

            playableContentViewModel.getPlanInfo(requireContext(), planId)

            playableContentViewModel.planInfoData.observe(this, Observer {

                tnSeeAllPreview.text = button_text2
                tvSellAllExhausted.text = button_text2
                CommonUtils.setButtonFromFirebase(requireContext(),ft,nonft,tvUpgradePlanExhausted,tvSellAllExhausted,it)
                CommonUtils.setButtonFromFirebase(requireContext(),ft,nonft,tvUpgradePlanPreview,tnSeeAllPreview,it)

                button_text = tvUpgradePlanExhausted.text.toString()

            })

            checkValidationJsonData(CommonUtils.getSongDurationConfig().nudge_minute_quota_exhausted)

        }
         setDataForPerviewScrren(planId2)
}

    fun setDataForPerviewScrren(planId2: String) {
        if (nudge_minute_ft != null || nudge_minute_nonft != null) {

            playableContentViewModel1.getPlanInfo(requireContext(), planId2)

            playableContentViewModel1.planInfoData.observe(this, Observer {


                CommonUtils.setButtonFromFirebase(requireContext(),nudge_minute_ft,nudge_minute_nonft,tvUpgradePlanNewPreview,tnSeeAllNewPreview,it)

                button_text = tvUpgradePlanExhausted.text.toString()

            })

            if (CommonUtils.isUserHasEliableFreeContent()) {
                if (getSongDurationConfig().nudge_stream_preview?.ft != null) {
                    nudge_minute_ft = getSongDurationConfig().nudge_stream_preview.ft
                    tnSeeAllPreview.text = CommonUtils.getStringResourceByName(getSongDurationConfig().nudge_stream_preview?.ft?.button_text_2!!).toString()
                }else nudge_minute_ft = null

            } else {
                if (getSongDurationConfig().nudge_stream_preview?.nonft != null) {
                    nudge_minute_nonft = getSongDurationConfig().nudge_stream_preview.nonft
                    tnSeeAllPreview.text =
                        CommonUtils.getStringResourceByName(getSongDurationConfig().nudge_stream_preview?.nonft?.button_text_2!!)
                            .toString()
                } else nudge_minute_nonft = null
            }
        }

    }

fun checkValidationJsonData(data: SongDurationConfigModel.DrawerMinuteQuotaExhausted) {

    if (CommonUtils.isUserHasEliableFreeContent()) {
        if (data?.ft != null) {
            ft = data.ft
            button_text2 = CommonUtils.getStringResourceByName(data?.ft?.button_text_2!!).toString()
        }else ft = null

    } else {
        if (data?.nonft != null) {
            nonft = data.nonft
            button_text2 = CommonUtils.getStringResourceByName(data?.nonft?.button_text_2!!).toString()
        }else nonft = null

    }
      }


  }