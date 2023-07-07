package com.hungama.music.ui.main.view.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
//import com.hungama.gamification.GamificationSDK
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.Track_State
import com.hungama.music.player.videoplayer.services.ChangeAppIconWorker
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.fragment.*
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.ProductViewModel
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyAppLogo
import com.hungama.music.utils.CommonUtils.getDeeplinkIntentData
import com.hungama.music.utils.CommonUtils.getNextOnboardingScreen
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.isContentDownloaded
import com.hungama.music.utils.CommonUtils.isUserHasNoAdsSubscription
import com.hungama.music.utils.CommonUtils.saveUserProfileDetails
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.setPlayerSongList
import com.hungama.music.utils.CommonUtils.showToast
import com.hungama.music.utils.Constant.CONTENT_LIVE_RADIO
import com.hungama.music.utils.Constant.CONTENT_RADIO
import com.hungama.music.utils.Constant.isPlay
import com.hungama.music.utils.Constant.isProfilePage
import com.hungama.music.utils.Constant.isRadio
import com.hungama.music.utils.Constant.isSearchScreen
import com.hungama.music.utils.Constant.isTabSelection
import com.hungama.music.utils.Constant.miniPlayerAction
import com.hungama.music.utils.Constant.playerArtworkChange
import com.hungama.music.utils.Constant.radioType
import com.hungama.music.utils.Constant.tabName
import com.hungama.music.utils.customview.CustomTabView
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.moengage.cards.MoECardHelper
import com.moengage.cards.listener.CardListener
import com.moengage.cards.model.Card
import com.moengage.cards.model.action.NavigationAction
import com.moengage.cards.ui.CardActivity
import com.moengage.inapp.MoEInAppHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.*
import java.lang.Runnable


const val READ_EXTERNAL_STORAGE_REQ_CODE: Int = 101
val TAG = MainActivity::class.java.name

class MainActivity : BaseActivity(), BaseActivity.OnLocalBroadcastEventCallBack {
    private lateinit var tabMenu : CustomTabView
    var isMangeIntentCall = true
    var isDestroyedMainActivity = false
    var pageName = ""
    var pageId = ""
    var pageDetailName = ""
    var pageContentPlay = ""
    var pageContentPayment = ""
    var isSeason = false
    var seasonNumber = 0
    var isMorePage = false
    var morePageName = ""
    var isSubTabSelected = false
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""
    var isTrailer = false
    var trailerId = ""
    var isEpisode = false
    var episodeName = ""
    var episodeId = ""
    var appLinkUrl = ""
    var liveShowArtistId = ""
    var queryParam = ""

    companion object {
        var lastItemClicked = ""
        var lastItemClickedTop = ""
        var headerItemName = ""
        var lastItemClickedForBTab = ""
        var headerItemNameForBTab = ""
        var subHeaderItemName = ""
        var subHeaderItemNameForBTab = ""
        var subHeader2nd = ""
        var headerItemPosition = 0
        var lastBottomItemPosClicked = 0
        var tempLastItemClicked = ""
        var tempLastBottomItemPosClicked = 0
        var isLaunched = true

        var lastClickedDataTopNav = ArrayList<String>()
        var lastClickedData = ArrayList<String>()
        var lastClickedDataSubTopNav = ArrayList<String>()

        fun clickedLastTop(value:String):String {
            var lastClicked = ""

            lastClickedData.add(0, value)

            when (lastClickedData.size) {
                1 -> lastClicked = lastClickedData[0]
                2 -> lastClicked = lastClickedData[1]
                3 -> {
                    lastClickedData.removeLast()
                    lastClicked = lastClickedData[1]
                }
            }

            setLog("lastClickedNew", "Top " + lastClicked + lastClickedData.size)

            return lastClicked.ifEmpty { "All" }
        }

        fun clickedLastTopNav(value:String):String{
            var lastClicked = ""

            lastClickedDataTopNav.add(0, value)

            when (lastClickedDataTopNav.size) {
                1 -> lastClicked = lastClickedDataTopNav[0]
                2 -> lastClicked = lastClickedDataTopNav[1]
                3 -> {
                    lastClickedDataTopNav.removeLast()
                    lastClicked = lastClickedDataTopNav[1]
                }
            }


            setLog("lastClickedNew", "Top " + lastClicked + lastClickedDataTopNav.size)

            return lastClicked.ifEmpty { "All" }
        }

        fun clickedLastSubTopNav(value:String):String{
            var lastClicked = ""

            lastClickedDataSubTopNav.add(0, value)

            when (lastClickedDataSubTopNav.size) {
                1 -> lastClicked = lastClickedDataSubTopNav[0]
                2 -> lastClicked = lastClickedDataSubTopNav[1]
                3 -> {
                    lastClickedDataSubTopNav.removeLast()
                    lastClicked = lastClickedDataSubTopNav[1]
                }
            }


            setLog("lastClickedNew", "Top " + lastClicked + lastClickedDataSubTopNav.size)

            return if (lastClicked.isNotEmpty()) "_$lastClicked" else ""
        }
    }


    var userViewModel: UserViewModel? = null
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    //    var remoteConfig = FirebaseRemoteConfig.getInstance()
    var defaultValue = HashMap<String, Any>()


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            SavedInstanceFragment.getInstance(supportFragmentManager).popData()
            setLog("TooLargeTool", "MainActivity-onRestoreInstanceState")
        }
        tabMenu = findViewById(R.id.tabMenu)

        getUserSubscriptionStatus()
        HungamaMusicApp.getInstance().deleteCacheData()
        initializeComponents()


        /*        val remoteConfig = Firebase.remoteConfig
                var splashAd1 = remoteConfig.getString("allow_background_activity")
                setLog("allowBackgroundActivity", " " + splashAd1)
                val un_menu = remoteConfig.getString("un_menu_bottom_nav_sequence_preselect")
                setLog("allowBackgroundActivity", " " + un_menu)
                val un_seq = remoteConfig.getString("un_menu_bottom_nav_sequence")
                setLog("allowBackgroundActivity", " " + un_seq)

                val packageName = packageName
                val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
                if (splashAd1.isEmpty())
                {
                    splashAd1 = "60000"
                }

                    if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            !pm.isIgnoringBatteryOptimizations(packageName)
                        } else {
                            TODO("VERSION.SDK_INT < M")
                        }
                    ) {

                       val batteryOpt = SharedPrefHelper.getInstance()[PrefConstant.backgroundActivity, ""]
                        if (batteryOpt.contains("Custom Allowed, Default Allowed")){
                            SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "-")
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            delay(splashAd1.toLong())

                            val batteryOptimizationPermissionDialog = BatteryOptimizationPermissionDialog(object :
                                    BatteryOptimizationPermissionDialog.BatteryPermission {

                                    override fun onBatteryPermission() {
                                        checkBatteryPermisssion()
                                    }
                                })

                            if(!isFinishing && isDestroyedMainActivity)
                                if(!batteryOptimizationPermissionDialog.isVisible && !supportFragmentManager.isDestroyed) {
                            batteryOptimizationPermissionDialog.show(supportFragmentManager, "open logout dialog")
                            } else {
                                batteryOptimizationPermissionDialog.dismiss()
                            }
                        }
                    }*/

        if (intent.getStringExtra(Constant.DeepLink_Payment) != null && intent.getStringExtra(Constant.DeepLink_Payment).toString().isNotEmpty()) {
            val url = intent.getStringExtra(Constant.DeepLink_Payment).toString()

            val intent = Intent(this@MainActivity, PaymentWebViewActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        }
    }

    private fun checkBatteryPermisssion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 100)

            }
        }



/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST)
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100){
            val dataMap= java.util.HashMap<String, String>()

            if (resultCode == RESULT_OK)
            {
                SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Allowed, Default Allowed")
            }
            else
            {
                SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Allowed, Default Denied")
            }
        }
    }

/*    override fun getLayoutResourceId(): Int {

        CommonUtils.setLog("getLayoutResourceId", "Main Activity getLayoutResourceId called")
        return R.layout.activity_main
    }*/

    override fun onResume() {
        super.onResume()
        hideKeyboard()
        MoEInAppHelper.getInstance().getSelfHandledInApp(HungamaMusicApp.getInstance())
        setLocalBroadcast()
        getUserProfile()

    }

/*    override fun onStart() {
        super.onStart()
        isDestroyedMainActivity = true
        MoEInAppHelper.getInstance().getSelfHandledInApp(HungamaMusicApp.getInstance())
    }*/

    override fun onStop() {
        isDestroyedMainActivity = false
        super.onStop()

    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        setLog("MainActivity", "onViewCreated-isAppLanguageChanged-$isAppLanguageChanged")

        MoECardHelper.getInstance().registerListener(cardListner)
//        callContinueWhereLeftListViewModel()
        setLog(TAG, "showDeepLinkUrl onViewCreated")

        displayLanguageDialog()
        isMangeIntentCall = true
        setLog("BaseActivityLifecycleMethods", "MainActivity-onViewCreated-$isAppLanguageChanged")
        if (!isAppLanguageChanged){
            getLocalQueueFromDB()
        }



        defaultValue.put("review_close_app_version_android", 145)
        setUpRemoteConfig()

        addDefaultFragment()

    }

    private fun addDefaultFragment(){


        val fragment = DiscoverMainTabFragment.newInstance(this, Bundle())
        replaceFragment(R.id.fl_container,fragment,false)
        lastItemClicked = Constant.Bottom_NAV_DISCOVER
        lastBottomItemPosClicked = 0
        headerItemPosition = 0
        headerItemName = "All"
        lastItemClickedTop = clickedLastTop(Constant.Bottom_NAV_DISCOVER)
        tempLastItemClicked = lastItemClicked
        tempLastBottomItemPosClicked = lastBottomItemPosClicked

    }

    private fun setUpRemoteConfig(){
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "setUpRemoteConfig: 1")
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)//3600
                .build()

/*            remoteConfig.setConfigSettingsAsync(configSettings)

            remoteConfig.setDefaultsAsync(defaultValue)
            setLog(TAG, "setUpRemoteConfig: 2")
            val fetch = remoteConfig.fetch(0)
            fetch.addOnSuccessListener {
                setLog(TAG, "setUpRemoteConfig: 3")
                remoteConfig.fetchAndActivate()
                updateReview()
            }*/
        }
    }

    /* private fun updateReview() {
         val versionNumber = remoteConfig.getLong("review_close_app_version_android")
         setLog(
             TAG,
             "setUpRemoteConfig: versionNumber:${1} BuildConfig.VERSION_CODE:${1}"
         )
         if (1 <= versionNumber) {
             displayReviewCloseDialog()
         }
     }*/

    private fun displayReviewCloseDialog() {
        CoroutineScope(Dispatchers.Main).launch{
            val reviewAlertDialog = AlertDialog.Builder(this@MainActivity).create()
            reviewAlertDialog.setCancelable(false)
            val message = getString(R.string.login_str_56)
            reviewAlertDialog.setMessage(message)
/*        reviewAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, which ->
            dialog?.dismiss()
            finish()
        }*/
//        reviewAlertDialog.show()
        }
    }

    private fun getLocalQueueFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            setLog(
                "displayDiscover",
                "MainActivity-getLocalQueueFromDB-isDisplayDiscover-$isDisplayDiscover"
            )
            val songDataList = AppDatabase?.getInstance()?.trackDao()?.getAllSong() as ArrayList<Track>?

            var isPlayingSongDetected = false
            if (songDataList != null && songDataList?.size!! > 0) {

                songDataList.forEachIndexed { index, track ->
                    if (track.state == Track_State.PLAYING) {
                        updateNowPlayingCurrentIndex(index)
                        isPlayingSongDetected = true
                        return@forEachIndexed
                    }
                }
                withContext(Dispatchers.Main){
                    if (!isPlayingSongDetected) {
                        updateNowPlayingCurrentIndex(0)
                        setLog("applyScreen", "MainActivity-getLocalQueueFromDB-1")
                        callRecommendedApi()
                    }else{

                        if(isAppLanguageChanged){
                            isAppLanguageChanged = false
                        }else{
                            setLog("MainActivity", "getLocalQueueFromDB-isAppLanguageChanged-$isAppLanguageChanged")
                            if (songDataList.size > nowPlayingCurrentIndex() && !isAppLanguageChanged){
                                val track = songDataList.get(nowPlayingCurrentIndex())
                                setLog("applyScreen", "MainActivity-getLocalQueueFromDB-1--track-$track")
                                if (track.pType == DetailPages.RECOMMENDED_SONG_LIST_PAGE.value
                                    || (track.playerType.equals(Constant.PLAYER_RADIO)
                                            || track.playerType.equals(Constant.PLAYER_LIVE_RADIO)
                                            || track.playerType.equals(Constant.PLAYER_ARTIST_RADIO)
                                            || track.playerType.equals(Constant.PLAYER_ON_DEMAND_RADIO)
                                            || track.playerType.equals(Constant.PLAYER_MOOD_RADIO))){
                                    setLog("applyScreen", "MainActivity-getLocalQueueFromDB-2")
                                    callRecommendedApi()
                                }else{
                                    playContent(songDataList, true, START_STATUS)
                                    setLog("applyScreen", "MainActivity-getLocalQueueFromDB-applyScreen-0")
                                    //applyScreen(0, Bundle())
                                }
                            }
                        }
                    }
                }

            }else{
                withContext(Dispatchers.Main){
                    setLog("applyScreen", "MainActivity-getLocalQueueFromDB-3")
                    callRecommendedApi()
                }
            }
        }
    }

    fun callRecommendedApi(){
        isDisplaySkeleton(true)
        getRecommendedContentList()
    }

    private fun displayLanguageDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            if (SharedPrefHelper.getInstance().isUserLoggedIn() || SharedPrefHelper.getInstance()
                    .isUserGuestLogdIn()
            ) {
                val type = withContext(Dispatchers.Default) {
                    getNextOnboardingScreen(1)
                }
                if (type > 0) {
                    withContext(Dispatchers.Main){
                        delay(2000)
                        try {
                            if(!isFinishing){
                                val sheet = LanguageArtistSelectBottomSheetFragment(type)
                                sheet.show(supportFragmentManager, "OnboardingBottomSheetFragment")
                            }
                        }catch (e:Exception){

                        }
                    }
                }
            }
        }
    }

    private fun initializeComponents() {
        tabMenu.setItemChangeListener(object : CustomTabView.OnTabItemChange {
            override fun onTabItemClick(position: Int) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            this@MainActivity, tabMenu,
                            HapticFeedbackConstants.CONTEXT_CLICK, false
                        )
                    }
                } catch (e: Exception) {

                }
                applyScreen(position)
            }
        })
        getIntentData(intent, false)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntentData(intent, true)
    }

    private fun getIntentData(intent: Intent?, isNewIntent: Boolean) {
        BaseActivity.isDeeplink = false
        if (intent != null && intent?.hasExtra(Constant.EXTRA_PAGE_NAME)!!) {
            if (isNewIntent) {
                isMangeIntentCall = true
                setLog(TAG, "showDeepLinkUrl onNewIntent")
            } else {
                setLog(TAG, "showDeepLinkUrl initializeComponents")
            }
            if (intent?.hasExtra(Constant.EXTRA_LIVESHOWARTIST_ID)!!) {
                liveShowArtistId = intent?.getStringExtra(Constant.EXTRA_LIVESHOWARTIST_ID)!!
            }

            if (intent?.hasExtra(Constant.EXTRA_APPLINKURL)!!) {
                appLinkUrl = intent?.getStringExtra(Constant.EXTRA_APPLINKURL)!!
            }

            if (intent?.hasExtra(Constant.EXTRA_PAGE_NAME)!!) {
                pageName = intent?.getStringExtra(Constant.EXTRA_PAGE_NAME)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_PAGE_DETAIL_ID)!!) {
                pageId = intent?.getStringExtra(Constant.EXTRA_PAGE_DETAIL_ID)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!) {
                pageDetailName = intent?.getStringExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_PAGE_CONTENT_PLAY)!!) {
                pageContentPlay = intent?.getStringExtra(Constant.EXTRA_PAGE_CONTENT_PLAY)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_QUERYPARAM)!!) {
                queryParam = intent?.getStringExtra(Constant.EXTRA_QUERYPARAM)!!
            }

            if (intent?.hasExtra(Constant.EXTRA_PAGE_CONTENT_PAYMENT)!!) {
                pageContentPayment = intent?.getStringExtra(Constant.EXTRA_PAGE_CONTENT_PAYMENT)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_IS_SEASON)!!) {
                isSeason = intent?.getBooleanExtra(Constant.EXTRA_IS_SEASON, false)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_SEASON_NUMBER)!!) {
                seasonNumber = intent?.getIntExtra(Constant.EXTRA_SEASON_NUMBER, 0)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_IS_MORE_PAGE)!!) {
                isMorePage = intent?.getBooleanExtra(Constant.EXTRA_IS_MORE_PAGE, false)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_MORE_PAGE_NAME)!!) {
                morePageName = intent?.getStringExtra(Constant.EXTRA_MORE_PAGE_NAME)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_IS_SUB_TAB_SELECTED)!!) {
                isSubTabSelected = intent?.getBooleanExtra(Constant.EXTRA_IS_SUB_TAB_SELECTED, false)!!
            }
            if (intent.hasExtra(Constant.EXTRA_IS_CATEGORY_PAGE)) {
                isCategoryPage = intent.getBooleanExtra(Constant.EXTRA_IS_CATEGORY_PAGE, false)
            }
            if (intent.hasExtra(Constant.EXTRA_CATEGORY_NAME)) {
                categoryName = intent.getStringExtra(Constant.EXTRA_CATEGORY_NAME)!!
            }
            if (intent.hasExtra(Constant.EXTRA_CATEGORY_ID)) {
                categoryId = intent.getStringExtra(Constant.EXTRA_CATEGORY_ID)!!
            }
            if (intent.hasExtra(Constant.EXTRA_IS_TRAILER)) {
                isTrailer = intent.getBooleanExtra(Constant.EXTRA_IS_TRAILER, false)
            }
            if (intent.hasExtra(Constant.EXTRA_TRAILER_ID)) {
                trailerId = intent.getStringExtra(Constant.EXTRA_TRAILER_ID)!!
            }
            if (intent.hasExtra(Constant.EXTRA_IS_EPISODE)) {
                isEpisode = intent.getBooleanExtra(Constant.EXTRA_IS_EPISODE, false)
            }
            if (intent.hasExtra(Constant.EXTRA_EPISODE_NAME)) {
                episodeName = intent.getStringExtra(Constant.EXTRA_EPISODE_NAME)!!
            }
            if (intent.hasExtra(Constant.EXTRA_EPISODE_ID)) {
                episodeId = intent.getStringExtra(Constant.EXTRA_EPISODE_ID)!!
            }

            manageIntent()
        } else {
//            setLog(TAG, "getIntentData applyScreen")
            //if (isDisplayDiscover){
            //setLog("applyScreen", "MainActivity-getIntentData-applyScreen-0")
            //applyScreen(0)
            //}
        }
    }


    private fun manageIntent() {
        setLog("deepLinkUrlMain", "showDeepLinkUrl isMangeIntentCall:$isMangeIntentCall pageName:$pageName pageId:$pageId pageDetailName:$pageDetailName isCategoryPage:$isCategoryPage")
        if (isMangeIntentCall) {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                val currentFragment = Utils.getCurrentFragment(this)

                if (!TextUtils.isEmpty(pageName) && !TextUtils.isEmpty(pageId) && !isCategoryPage)
                {
                    val bundle = Bundle()
                    bundle.putString(Constant.defaultContentImage, "")
                    bundle.putString(Constant.defaultContentId, "" + pageId)
                    bundle.putString(Constant.defaultContentPlayerType, "")
                    bundle.putBoolean(Constant.defaultContentVarient, true)
                    bundle.putBoolean(Constant.EXTRA_IS_SEASON, isSeason)
                    bundle.putInt(Constant.EXTRA_SEASON_NUMBER, seasonNumber)
                    bundle.putBoolean(Constant.EXTRA_IS_MORE_PAGE, isMorePage)
                    bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
                    bundle.putBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
                    bundle.putBoolean(Constant.EXTRA_IS_TRAILER, isTrailer)
                    bundle.putString(Constant.EXTRA_TRAILER_ID, trailerId)
                    bundle.putBoolean(Constant.EXTRA_IS_EPISODE, isEpisode)
                    bundle.putString(Constant.EXTRA_EPISODE_NAME, episodeName)
                    bundle.putString(Constant.EXTRA_EPISODE_ID, episodeId)
                    setLog(TAG, "manageIntent: pageContentPlay:${pageContentPlay}")
                    if (!TextUtils.isEmpty(pageContentPlay)) {
                        bundle.putInt(isPlay, 1)
                    } else {
                        bundle.putInt(isPlay, 0)
                    }

                    if (!TextUtils.isEmpty(pageContentPayment)) {
                        bundle.putInt(Constant.isPayment, 1)
                        bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                    }
                    val varient = 1

                    setLog("DeeplinkPageName", " " + pageName.toString())

                    when (pageName) {
                        "song", "songs" -> {
                            val songDetailFragment = SongDetailFragment()
                            songDetailFragment.arguments = bundle
                            if (currentFragment != null) {
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    songDetailFragment,
                                    false
                                )

//                                openPlayerScreen(5, bundle, true)

                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }

                        }

                        "music" -> {
                            applyScreen(1)
                        }

                        "videos" -> {
                            applyScreen(2)
                        }

                        "library" -> {
                            if (!TextUtils.isEmpty(pageDetailName)) {
                                bundle.putBoolean(isTabSelection, true)
                                bundle.putString(tabName, pageDetailName)
                                if (isSubTabSelected) {
                                    bundle.putString(Constant.subTabName, morePageName)
                                    bundle.putBoolean(
                                        Constant.EXTRA_IS_SUB_TAB_SELECTED,
                                        isSubTabSelected
                                    )
                                }
                            }
                            applyScreen(4, bundle)
                        }

                        "video" -> {
                            applyScreen(2)
                            setPauseMusicPlayerOnVideoPlay()

                            val videoDetailsFragment = MusicVideoDetailsFragment()
                            videoDetailsFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    videoDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "chart", "charts" -> {
                            if(pageId?.contains("Daily",true) == true){
                                bundle.putString("id", "" + pageId)
                            }else{
                                bundle.putString("id", "" + pageId)
                            }

                            val nextFragment = ChartDetailFragment.newInstance(varient)
                            nextFragment.arguments = bundle

                            if (currentFragment != null) {
                                isMangeIntentCall = false
                                addFragment(R.id.fl_container, currentFragment, nextFragment, false)
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "tv-show", "tv-shows" -> {
                            applyScreen(2)
                            val tvShowDetailsFragment = TvShowDetailsFragment(varient)
                            tvShowDetailsFragment.arguments = bundle
                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    tvShowDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "original", "originals" -> {

                        }

                        "music-video", "music-videos" -> {
                            applyScreen(2)
                            setPauseMusicPlayerOnVideoPlay()

                            val videoDetailsFragment = MusicVideoDetailsFragment()
                            videoDetailsFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    videoDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "game", "games" -> {
                            applyScreen(0)
                            val gameDetailFragment = GameDetailFragment()
                            gameDetailFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    gameDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "music-video-playlist" -> {

                        }

                        "artist", "artists" -> {
                            bundle.putString("id", "artist-" + pageId)
                            val artistDetailsFragment = ArtistDetailsFragment()
                            artistDetailsFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    artistDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }

                        }

                        "podcast", "podcasts" -> {

                            val podcastDetailsFragment = PodcastDetailsFragment()
                            podcastDetailsFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    podcastDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }

                        }

                        "playlist", "playlists" -> {
                            if(pageId?.contains("Daily",true) == true){
                                bundle.putString("id", "" + pageId)
                                bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                            }else{
                                bundle.putString("id", "playlist-" + pageId)
                            }

                            val nextFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                            nextFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(R.id.fl_container, currentFragment, nextFragment, false)
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "movie", "movies", "short-film", "short-films" -> {
                            applyScreen(2)
                            val movieDetailsFragment = MovieV1Fragment(varient)
                            movieDetailsFragment.arguments = bundle
                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    movieDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "radio" -> {
                            setLog(
                                TAG,
                                "showDeepLinkUrlcurrentFragment11:${bundle}"
                            )
                            bundle.putInt(isPlay, 1)
                            bundle.putBoolean(isRadio, true)
                            bundle.putInt(radioType, CONTENT_RADIO)
                            applyScreen(0, bundle)

                        }

                        "live-radio" -> {
                            setLog(
                                TAG,
                                "showDeepLinkUrlcurrentFragment1111:${bundle}"
                            )
                            bundle.putInt(isPlay, 1)
                            bundle.putBoolean(isRadio, true)
                            bundle.putInt(radioType, CONTENT_LIVE_RADIO)
                            applyScreen(0, bundle)

                        }

                        "live-show", "live-shows" -> {
                            bundle.putString(Constant.defaultArtistId, liveShowArtistId)
//                            bundle.putString(Constant.defaultArtistId, "artist-81488978")
                            val eventDetailFragment = EventDetailFragment()
                            eventDetailFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    eventDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "user-profile" -> {

                        }

                        "premium" -> {

                        }

                        "notification" -> {

                        }

                        "search" -> {

                        }

                        "stories" -> {
                            val storyUsersList = ArrayList<BodyDataItem>()
                            val storyUser = BodyDataItem()
                            storyUser?.id = pageId
                            storyUsersList.add(storyUser)


                            val status = getAudioPlayerPlayingStatus()
                            if (status == Constant.pause){
                                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                            }else{
                                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                            }
                            pausePlayer()

                            val intent = Intent(this, StoryDisplayActivity::class.java)
                            intent.putExtra("position", 0)
                            intent.putParcelableArrayListExtra("list", storyUsersList)
                            startActivityForResult(
                                intent,
                                DiscoverTabFragment.LAUNCH_STORY_DISPLAY_ACTIVITY
                            )
                        }

                        "recently-played" -> {

                        }

                        "downloaded-songs" -> {

                        }

                        "album" -> {
                            val albumDetailFragment = AlbumDetailFragment()
                            albumDetailFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    albumDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }

                        }

                        "collection", "collections" -> {
                            val collectionDetailsFragment = CollectionDetailsFragment()
                            collectionDetailsFragment.arguments = bundle
                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    collectionDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "redeem" -> {
                            if (pageDetailName?.contains("category", true) == true) {
                                if (currentFragment != null) {
                                    val earnCoinAllTabFragement = EarnCoinAllTabFragement()
                                    earnCoinAllTabFragement.arguments = bundle
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment!!,
                                        earnCoinAllTabFragement,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }
                            } else if (pageDetailName?.contains("product", true) == true) {
                                if (currentFragment != null) {
                                    callProductApi(pageId, currentFragment,bundle)
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }


                            }

                        }

                        "user-playlist" -> {
                            val myPlaylistDetailFragment = MyPlaylistDetailFragment(1,object :MyPlaylistDetailFragment.onBackPreesHendel{
                                override fun backPressItem(status: Boolean) {
                                }

                            })
                            myPlaylistDetailFragment.arguments = bundle

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    myPlaylistDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                    }
                }
                else if (!TextUtils.isEmpty(pageName) && !TextUtils.isEmpty(pageDetailName)) {
                    val bundle = Bundle()
                    bundle.putBoolean(isTabSelection, true)
                    bundle.putString(tabName, pageDetailName)
                    setLog("deepLinkUrl", "topTab == tabName-$pageDetailName")
                    if (isSubTabSelected) {
                        bundle.putString(Constant.subTabName, morePageName)
                        bundle.putBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
                    }
                    bundle.putBoolean(Constant.EXTRA_IS_CATEGORY_PAGE, isCategoryPage)
                    bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                    bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                    when (pageName) {
                        "payment" -> {
                            setLog(
                                "isGotoDownloadClicked",
                                "MainActivity-payment-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                            )
                            if (ConnectionUtil(this@MainActivity).isOnline) {
                                val genrtedURL = CommonUtils.genratePaymentPageURL(this@MainActivity, queryParam)
                                var intent = Intent()
                                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                    intent = Intent(this@MainActivity, PaymentWebViewActivity::class.java)
                                    setLog("payment", "payment genrtedURL:${genrtedURL} queryParam:${queryParam}")
                                }
                                else{
                                    intent = Intent(this@MainActivity, LoginMainActivity::class.java)
                                }
                                setLog("PrintUrl", " deeplink " + genrtedURL)

                                intent.putExtra("url", genrtedURL)
                                startActivity(intent)

                            } else {
                                val messageModel = MessageModel(
                                    getString(R.string.toast_str_35),
                                    getString(R.string.toast_message_5),
                                    MessageType.NEGATIVE,
                                    true
                                )
                                showToast(this@MainActivity, messageModel)
                            }

                        }
                        "retry" -> {
                            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                setLog(
                                    "isGotoDownloadClicked",
                                    "MainActivity-retry-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                                )
                                if (ConnectionUtil(this@MainActivity).isOnline) {
                                    val intent =
                                        Intent(
                                            this@MainActivity,
                                            PaymentWebViewActivity::class.java
                                        )
                                    intent.putExtra("url", appLinkUrl)
                                    startActivity(intent)
                                } else {
                                    val messageModel = MessageModel(
                                        getString(R.string.toast_str_35),
                                        getString(R.string.toast_message_5),
                                        MessageType.NEGATIVE,
                                        true
                                    )
                                    showToast(this@MainActivity, messageModel)
                                }

                            }
                        }
                        "redeem" -> {
                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinAllTabFragement(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "earn-coins" -> {
                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinDetailFragment(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "music" -> {
/*                            if (categoryName.contains("rewind"))
                            BaseActivity.isDeeplink = true*/
                            applyScreen(1, bundle)
                        }
                        "rewind-2022" -> {
                            applyScreen(0, bundle)
                        }
                        "games" -> {
                            applyScreen(0, bundle)
                        }
                        "videos" -> {
                            applyScreen(2, bundle)
                        }
                        "search" -> {
                            bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
                            bundle.putBoolean(isSearchScreen, true)
                            applyScreen(0, bundle)
                        }
                        "library" -> {
                            applyScreen(4, bundle)
                        }
                        "user-profile" -> {
                            bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
                            bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
                            if (!TextUtils.isEmpty(pageDetailName) && pageDetailName.length < 15){
                                val profileFragment = ProfileFragment()
                                profileFragment.arguments = bundle

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    profileFragment,
                                    false
                                )
                            }else{
                                callUserAPI(bundle,pageDetailName,pageName,morePageName,currentFragment)
                            }
                        }
                        "user-signup" -> {
                            if(!SharedPrefHelper.getInstance().isUserLoggedIn())
                                startActivity(Intent(this@MainActivity, LoginMainActivity::class.java))
                        }
                        else -> {
                            if (CommonUtils.getMusicPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                setLog("deepLinkUrl", "bottomTab-Music == pageName-$pageName")
                                applyScreen(1, bundle)
                            } else if (CommonUtils.getVideoPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                setLog("deepLinkUrl", "bottomTab-Video == pageName-$pageName")
                                applyScreen(2, bundle)
                            } else {
                                setLog("deepLinkUrl", "bottomTab-Discover == pageName-$pageName")
                                applyScreen(0, bundle)
                            }
                        }
                    }
                }
                else if (!TextUtils.isEmpty(pageName))
                {

                    when (pageName) {
                        "pllf"-> {
                            setLog("DeeplinkPageName", " " + pageName)

                            val bundle = Bundle()
                            bundle.putString(Constant.defaultContentImage, "")

                            val url = appLinkUrl
                            val split = url.split("&").toTypedArray()
                            for (domain in split) {
                                println("applinkarray " +domain)
                            }

                            pageId = split[5].replace("af_sub5=", "")

                            bundle.putString(Constant.defaultContentId, "" + pageId)
                            bundle.putString(Constant.defaultContentPlayerType, "")
                            bundle.putBoolean(Constant.defaultContentVarient, true)
                            bundle.putBoolean(Constant.EXTRA_IS_SEASON, isSeason)
                            bundle.putInt(Constant.EXTRA_SEASON_NUMBER, seasonNumber)
                            bundle.putBoolean(Constant.EXTRA_IS_MORE_PAGE, isMorePage)
                            bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
                            bundle.putBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
                            bundle.putBoolean(Constant.EXTRA_IS_TRAILER, isTrailer)
                            bundle.putString(Constant.EXTRA_TRAILER_ID, trailerId)
                            bundle.putBoolean(Constant.EXTRA_IS_EPISODE, isEpisode)
                            bundle.putString(Constant.EXTRA_EPISODE_NAME, episodeName)
                            bundle.putString(Constant.EXTRA_EPISODE_ID, episodeId)
                            setLog(TAG, "manageIntent: pageContentPlay:${pageId}")
                            val varient = 1
                            if(pageId?.contains("Daily",true) == true){
                                bundle.putString("id", "" + pageId)
                                bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                            }else{
                                bundle.putString("id", "playlist-" + pageId)
                            }

                            val nextFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                            nextFragment.arguments = bundle

                            if (currentFragment != null) {
                                isMangeIntentCall = false
                                addFragment(R.id.fl_container, currentFragment, nextFragment, false)
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "music" -> {
                            applyScreen(1)
                        }
                        "videos" -> {
                            applyScreen(2)
                        }
                        "search" -> {
                            val bundle = Bundle()
                            bundle.putBoolean(isSearchScreen, true)
                            applyScreen(0, bundle)
                        }
                        "library" -> {
                            applyScreen(4)
                        }
                        "notification" -> {
                            startActivity(Intent(this, CardActivity::class.java))
                        }
                        "payment","paymnet" -> {
                            setLog(
                                "isGotoDownloadClicked",
                                "MainActivity-payment-2-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                            )
                            if (ConnectionUtil(this@MainActivity).isOnline) {
                                var intent = Intent()
                                val genrtedURL = CommonUtils.genratePaymentPageURL(this@MainActivity, queryParam)
                                setLog("PaymentUrl", genrtedURL)
                                if(SharedPrefHelper.getInstance().isUserLoggedIn()){
                                    intent = Intent(this@MainActivity, PaymentWebViewActivity::class.java)
                                    intent.putExtra("url", genrtedURL)
                                }
                                else{
                                    setLog("PrintUrl", " deeplink 1196 " + genrtedURL)
                                    intent = Intent(this@MainActivity, LoginMainActivity::class.java)
                                    intent.putExtra(Constant.DeepLink_Payment, genrtedURL)
                                }

                                startActivity(intent)
                            } else {
                                val messageModel = MessageModel(
                                    getString(R.string.toast_str_35),
                                    getString(R.string.toast_message_5),
                                    MessageType.NEGATIVE,
                                    true
                                )
                                showToast(this@MainActivity, messageModel)
                            }

                        }
                        "retry" -> {
                            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                setLog(
                                    "isGotoDownloadClicked",
                                    "MainActivity-retry-2-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                                )
                                if (ConnectionUtil(this@MainActivity).isOnline) {
                                    val intent =
                                        Intent(
                                            this@MainActivity,
                                            PaymentWebViewActivity::class.java
                                        )
                                    intent.putExtra("url", appLinkUrl)
                                    startActivity(intent)
                                } else {
                                    val messageModel = MessageModel(
                                        getString(R.string.toast_str_35),
                                        getString(R.string.toast_message_5),
                                        MessageType.NEGATIVE,
                                        true
                                    )
                                    showToast(this@MainActivity, messageModel)
                                }

                            }
                        }
                        "redeem" -> {
                            if (currentFragment != null) {
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinAllTabFragement(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "earn-coins" -> {
                            if (currentFragment != null) {
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinDetailFragment(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "user-profile" -> {
                            val bundle = Bundle()
                            bundle.putBoolean(isProfilePage, true)

                            val profileFragment=ProfileFragment()
                            profileFragment?.arguments = bundle

                            getUserProfile()

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1000)
                                }
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    profileFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        else -> {

                            setLog(TAG, "showDeepLinkUrl SEO URL is pageName:" + pageName)
                            val bundle = Bundle()
                            bundle.putBoolean(isTabSelection, true)
                            bundle.putString(tabName, pageName)

                            if (CommonUtils.getMusicPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                setLog("deepLinkUrl", "bottomTab-Music == pageName-$pageName")
                                applyScreen(1, bundle)
                            } else if (CommonUtils.getVideoPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                setLog("deepLinkUrl", "bottomTab-Video == pageName-$pageName")
                                applyScreen(2, bundle)
                            } else {
                                setLog("deepLinkUrl", "bottomTab-Discover == pageName-$pageName")
                                applyScreen(0, bundle)
                            }

                        }
                    }
                } else
                {
                    setLog(TAG, "showDeepLinkUrl null applyScreen")
                    setLog("applyScreen", "MainActivity-manageIntent-applyScreen-0")
                    applyScreen(0)

                }

                if (!TextUtils.isEmpty(pageName)){
                    val dataMap = HashMap<String, String>()
                    dataMap[EventConstant.SOURCE_DETAILS_EPROPERTY] = appLinkUrl
                    dataMap[EventConstant.SOURCE] = "deeplink"
                    dataMap[EventConstant.CONTENTNAME_EPROPERTY] = pageDetailName
                    dataMap[EventConstant.CONTENT_TYPE_EPROPERTY] = pageName
                    val newContentId=pageId
                    val contentIdData=newContentId.replace("playlist-","")
                    dataMap[EventConstant.CONTENTID_EPROPERTY] = contentIdData
                    dataMap[EventConstant.PAGE_NAME_EPROPERTY] = "details_"+ pageName
                    EventManager.getInstance().sendEvent(PageViewEvent(dataMap))
                }
                setLog("IType104ViewHolder", "IType104ViewHolder deeplink call End:${DateUtils.getCurrentDateTimeNewFormat()}")
            }, 1000)
        }

    }



    private fun callUserAPI(
        bundle: Bundle,
        pageDetailName: String,
        pageName: String,
        morePageName: String,
        currentFragment: Fragment?
    ) {
        setLog(TAG, "showDeepLinkUrl pageDetailName:${pageDetailName} USER_SHARE:${SharedPrefHelper.getInstance().get(PrefConstant.USER_SHARE,"")}")
        //bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
        //bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
        var profileFragment:Fragment? = null
        if (ConnectionUtil(this@MainActivity).isOnline) {
            userViewModel?.getUserProfileData(
                this@MainActivity, pageDetailName
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{

                            setLog(TAG, "showDeepLinkUrl user profile:${it?.data}")


                            if(it?.data?.result?.get(0)?.uId?.equals(SharedPrefHelper.getInstance().getUserId())!!){
                                setLog(TAG, "showDeepLinkUrl user profile:${it?.data}")
                                profileFragment=ProfileFragment()
                                profileFragment?.arguments = bundle

                            }else{
                                setLog(TAG, "showDeepLinkUrl other user profile:${it?.data}")
                                val otherUserId= it?.data?.result?.get(0)?.uId.toString()
                                setLog(TAG, "showDeepLinkUrl otherUserId:${otherUserId}")
                                profileFragment=UserProfileOtherUserProfileFragment(otherUserId)

                            }

                            if (profileFragment != null) {
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    profileFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        Status.LOADING ->{

                        }

                        Status.ERROR ->{
                            profileFragment?.arguments = bundle
                            profileFragment=ProfileFragment()
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(this@MainActivity, messageModel)
        }


    }

    public fun applyScreen(position: Int, bundle: Bundle = Bundle.EMPTY) {
        setLog(TAG, "applyScreen position:$position")
        headerItemPosition = 0
//        headerItemName = "All"

        when (position) {
            0 -> {
                var fragment: Fragment? = null
                setLog(
                    "displayDiscover",
                    "MainActivity-applyScreen-isDisplayDiscover-$isDisplayDiscover"
                )
                fragment = DiscoverMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.Bottom_NAV_DISCOVER, position)
            }
            1 -> {
                isDisplaySkeleton(false)
                val fragment = MusicMainFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_MUSIC, position)
//                val fragment = DiscoverTabFragment.newInstance(headItemsItem, bundle)
//                replaceFragment(R.id.fl_container, fragment, false)
//                setLastClickedBottomMenu(Constant.BOTTOM_NAV_PODCAST, position)


//                fragmentList.add(fragment)
            }
            2 -> {
                isDisplaySkeleton(false)
                val fragment = VideoMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                val hashMap = HashMap<String, String>()
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_VIDEOS, position)
            }
            3 -> {
                isDisplaySkeleton(false)
                val fragment = SearchAllTabFragment()
                fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_SEARCH, position)
            }
            4 -> {
                isDisplaySkeleton(false)
                val fragment = LibraryMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_LIBRARY, position)
            }
            5 -> {
                openPlayerScreen(position, bundle)
            }
            6 -> {

                var fragment: Fragment? = null
                setLog(
                    "podcastTab",
                    "on podcast tab click"
                )
                fragment = PodcastMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_PODCAST, position)

            }
            else -> {
                isDisplaySkeleton(false)
                val fragment = DiscoverMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
            }
        }
    }

    fun getUserProfile() {
        userViewModel = ViewModelProvider(
            this@MainActivity
        ).get(UserViewModel::class.java)
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-getUserProfile-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this@MainActivity).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            userViewModel?.getUserProfileData(
                this@MainActivity, SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this@MainActivity,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it?.data != null) {
                                saveUserProfileDetails(it?.data)
                                updateProfile()
                                callEventUserProperty(it?.data)

                            }
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {

                        }
                    }
                })
        }

    }


    fun updateProfile() {
        try {
            setLog(TAG, "Gamification"+ ivUserPersonalImage.toString())

            if (ivUserPersonalImage != null) {
                if (!TextUtils.isEmpty(
                        SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE, "")
                    )
                ) {
                    ImageLoader.loadImage(
                        this@MainActivity, ivUserPersonalImage!!,
                        SharedPrefHelper.getInstance().get(
                            PrefConstant.USER_IMAGE, ""
                        ), R.drawable.ic_no_user_img
                    )
                } else {
                    ImageLoader.loadImage(
                        this@MainActivity,
                        ivUserPersonalImage!!,
                        "",
                        R.drawable.ic_no_user_img
                    )
                }
/*                if(SharedPrefHelper.getInstance().isUserLoggedIn()){
                    var gmfSDKCoins = GamificationSDK.getPoints()

                    if (gmfSDKCoins < 0){
                        gmfSDKCoins = 0
                    }
                    val userCoinDetailRespModel = SharedPrefHelper?.getInstance()?.getObjectUserCoin(
                        PrefConstant.USER_COIN
                    )
                    userCoinDetailRespModel?.actions?.get(0)?.total = gmfSDKCoins
                    if (userCoinDetailRespModel != null && userCoinDetailRespModel?.actions != null) {
                        tvCoinCount?.text =
                            CommonUtils?.ratingWithSuffix("" + userCoinDetailRespModel?.actions?.get(0)?.total!!)
                    } else {
                        tvCoinCount?.text = "" + gmfSDKCoins
                    }
                    setLog(TAG, "updateProfile tvCoinCount 2 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")
                }else{
                    tvCoinCount?.text = "0"
                    setLog(TAG, "updateProfile tvCoinCount 3 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")
                }*/

                if (CommonUtils.isUserHasGoldSubscription()) {
                    ivMenuCount?.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_round_count_bg_home_action_bar_gold
                    )
                    ivCoin?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.bg_coin_profile_black
                        )
                    )
                    tvCoinCount?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorBlack))
                } else {
                    ivMenuCount?.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_round_count_bg_home_action_bar
                    )
                    ivCoin?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.bg_coin_profile
                        )
                    )
                    tvCoinCount?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorWhite))
                }
                ivMenuCount?.visibility=View.VISIBLE
            }
        } catch (e: Exception) {

        }
    }

    fun getUserSubscriptionStatus() {
        userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-getUserSubscriptionStatus-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(this)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {

                            /*if (it?.data != null && it?.data?.success!! && it?.data?.data != null && it?.data?.data?.status!!) {

                            }*/
                            updateViewBasedOnSubscription()
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {

                        }
                    }
                })
        }
    }

    fun updateViewBasedOnSubscription() {
        setIsGoldUser()
        if (isUserHasNoAdsSubscription()){
            setLog(
                "setPageBottomSpacing",
                "MainActivity-true-isBottomStickyAdLoaded: ${isBottomStickyAdLoaded}"
            )
            isBottomStickyAdLoaded = false
        } else {
            setLog(
                "setPageBottomSpacing",
                "MainActivity-false-isBottomStickyAdLoaded: ${isBottomStickyAdLoaded}"
            )
        }

        if (ivLogo != null) {
            applyAppLogo(this, ivLogo)
        }
    }

    val cardListner = object : CardListener() {
        override fun onCardClick(card: Card, navigationAction: NavigationAction): Boolean {
            val intent = getDeeplinkIntentData(Uri.parse(navigationAction.value))
            intent.setClass(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
            return true
        }
    }

    override fun onDestroy() {

        /**
         * Logic for app icon change
         * FreeAlias=> free user
         * GoldAlias=> Gold user
         */
        ChangeAppIconWorker.enqueue(this)
        stopChormeCast()
        if (!isAppLanguageChanged){
            audioPlayerService?.hidePlayerNotification(true)
        }
        setLog(TAG, "Main activity onDestroy: ")
        super.onDestroy()
    }



    private fun setLocalBroadcast() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT));
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_MINI_PLAYER_EVENT));
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_UI_EVENT));
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        val event = intent.getIntExtra("EVENT", 0)
        setLog("MainActivity", "PlayerEvent-$intent")
        if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
            try {
                when (getAudioPlayerPlayingStatus()) {
                    Constant.playing -> {
                        setLog("MainActivity", "PlayerEvent-playing")
                        //tabMenu?.updatePlayPauseIcon(Constant.playing)
                    }
                    Constant.pause -> {
                        setLog("MainActivity", "PlayerEvent-pause")
                        //tabMenu?.updatePlayPauseIcon(Constant.pause)
                    }
                    else -> {
                        setLog("MainActivity", "PlayerEvent-none")
                        //tabMenu?.updatePlayPauseIcon(Constant.noneAudio)
                    }
                }
            } catch (e: java.lang.Exception) {

            }
        } else if (event == Constant.AUDIO_MINI_PLAYER_CLICK_RESULT_CODE) {
            val miniPlayerAction = intent.getIntExtra(miniPlayerAction, 0)
            if (miniPlayerAction == Constant.playerCollapsed) {
                setLog(
                    "miniplayerAction",
                    "MainActivity-onLocalBroadcastEventCallBack-action-playerCollapsed"
                )
            } else if (miniPlayerAction == Constant.playerExpand) {

                setLog(TAG, "miniplayerAction applyScreen")
                setLog(
                    "miniplayerAction",
                    "MainActivity-onLocalBroadcastEventCallBack-action-playerExpand"
                )

                applyScreen(5)
            }
        }else if (event == Constant.AUDIO_PLAYER_UI_RESULT_CODE) {
            setLog("MainActivity", "PlayerUIEvent-change")
            val miniPlayerAction = intent.getStringExtra(playerArtworkChange)
            tabMenu?.updatePlayerTabArtwork(miniPlayerAction)
        }else if (event == Constant.AUDIO_PLAYER_END_RESULT_CODE){
            setLog("MainActivity", "AUDIO_PLAYER_END_RESULT_CODE")
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver-1", "MainActivity-mMessageReceiver-" + intent)
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    setLog(
                        "BroadcastReceiver-1",
                        "MainActivity-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                    )
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }

    var recommendedListViewModel: PlaylistViewModel? = null
    var recommendedSongList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    fun getRecommendedContentList(bottomTabPosition:Int = -1, isReplaceScreen:Boolean = false) {
        setLog(
            "displayDiscover",
            "MainActivity-getRecommendedContentList-isDisplayDiscover-$isDisplayDiscover"
        )
        setLog("SwipablePlayerFragment", "MainActivity-getRecommendedContentList()-1-songDataList-${BaseActivity.songDataList?.size}")
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-getRecommendedContentList-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline) {
            recommendedListViewModel = ViewModelProvider(
                this
            ).get(PlaylistViewModel::class.java)
            recommendedListViewModel?.getRecommendedContentList(this, "")?.observe(this,
                {
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it?.data != null) {
                                recommendedSongList = it.data.data.body.rows
                                if (!recommendedSongList.isNullOrEmpty()) {
                                    //if (isOnClick()) {
                                    val songList: ArrayList<Track> = arrayListOf()
                                    for (item in recommendedSongList.iterator()) {
                                        var playerImage=""
                                        if(item?.data?.playble_image!=null&&!TextUtils.isEmpty(item?.data?.playble_image)){
                                            playerImage=item.data.playble_image
                                        }else{
                                            playerImage=item.data.image
                                        }

                                        val track =
                                            item.data.misc.restricted_download?.let { it1 ->
                                                setPlayerSongList(
                                                    item.data.id,
                                                    item.data.title,
                                                    item.data.subtitle,
                                                    "",
                                                    "",
                                                    "",
                                                    item.data.type,
                                                    playerImage,
                                                    "Recommended",
                                                    it.data.data.head.data.id,
                                                    it.data.data.head.data.title,
                                                    it.data.data.head.data.subtitle,
                                                    it.data.data.head.data.image,
                                                    DetailPages.RECOMMENDED_SONG_LIST_PAGE.value,
                                                    ContentTypes.AUDIO.value,
                                                    item.data.misc.explicit,
                                                    it1,
                                                    item?.data?.misc?.attributeCensorRating.toString(),
                                                it.data.data.head.movierights.toString())
                                            }
                                        track?.let { it1 -> songList.add(it1) }
                                    }
                                    updateNowPlayingCurrentIndex(0)
                                    playContent(songList, true, START_STATUS)
                                    setLog(TAG, "getRecommendedContentList applyScreen")
                                    setLog("applyScreen", "MainActivity-getRecommendedContentList-applyScreen-$bottomTabPosition")
                                    if (bottomTabPosition > -1){
                                        if (isReplaceScreen){
                                            openPlayerScreen(bottomTabPosition, Bundle(), isReplaceScreen)
                                        }else{
                                            applyScreen(bottomTabPosition, Bundle())
                                        }
                                    }
                                    //}
                                }
                            }
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {
                            val messageModel = it.message?.let { it1 ->
                                MessageModel(
                                    "", it1,
                                    MessageType.NEGATIVE, true
                                )
                            }
                            if (messageModel != null) {
                                showToast(this, messageModel)
                            }
                            isDisplaySkeleton(false)
                        }
                    }
                })
        }
    }

    fun playContent(songDataList: ArrayList<Track>, isPause:Boolean, isNextClick:Int = START_STATUS) {
        try {
            setLog("NotificationManager", "MainActivity-playContent-isPause-$isPause")
            setTrackListData(songDataList)
            this.isNextClick = isNextClick
            val playingIndex=nowPlayingCurrentIndex()

            val isOfflinePlay = isContentDownloaded(BaseActivity.songDataList, playingIndex)
            if (isOfflinePlay && !BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > playingIndex) {
                setEventModelDataAppLevel(
                    "" + BaseActivity.songDataList?.get(playingIndex)?.id!!,
                    BaseActivity.songDataList?.get(playingIndex)?.title!!,
                    BaseActivity.songDataList?.get(playingIndex)?.pName!!,
                    EventConstant.CONSUMPTIONTYPE_OFFLINE
                )
                playContentOffline(BaseActivity.songDataList, playingIndex, isPause)
            } else {
                if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > playingIndex) {
                    if (BaseActivity.songDataList?.get(playingIndex)?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value){
                        setEventModelDataAppLevel(
                            "" + BaseActivity.songDataList?.get(playingIndex)?.id!!,
                            BaseActivity.songDataList?.get(playingIndex)?.title!!,
                            BaseActivity.songDataList?.get(playingIndex)?.pName!!,
                            EventConstant.CONSUMPTIONTYPE_LOCAL
                        )
                        playContentOfflineDeviceSongs(
                            BaseActivity.songDataList,
                            playingIndex,
                            isPause
                        )

                    }else{
                        setEventModelDataAppLevel(
                            "" + BaseActivity.songDataList?.get(playingIndex)?.id!!,
                            BaseActivity.songDataList?.get(playingIndex)?.title!!,
                            BaseActivity.songDataList?.get(playingIndex)?.pName!!,
                            EventConstant.CONSUMPTIONTYPE_ONLINE
                        )
                        playContentOnline(BaseActivity.songDataList, playingIndex, isPause)

                    }

                }

            }
        }catch (e:Exception){

        }
    }

    private fun callProductApi(productId: String, currentFragment: Fragment, bundle: Bundle) {
        setLog(TAG, "callProductApi called")
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-callProductApi-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        val productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (ConnectionUtil(this@MainActivity).isOnline) {
            productViewModel.getProductDetails(this@MainActivity, productId)?.observe(this,
                {
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it?.data?.product != null) {
                                CommonUtils.setLog(
                                    "MyOrder",
                                    "MyOrderListFragment-callProductApi-Responce-${it.data}"
                                )

                                if (currentFragment != null) {
                                    val earnCoinProductFragment = EarnCoinProductFragment(it?.data?.product)
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment!!,
                                        earnCoinProductFragment,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }

                            }
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(this@MainActivity, messageModel)
        }
    }

    fun isDisplaySkeleton(isDisplay:Boolean){
        /*if(isDisplay){
            swipablePlayerShimmerLayout.visibility = View.VISIBLE
            swipablePlayerShimmerLayout.startShimmer()
        }else{
            swipablePlayerShimmerLayout.stopShimmer()
            swipablePlayerShimmerLayout.visibility = View.GONE
        }*/
        swipablePlayerShimmerLayout.stopShimmer()
        swipablePlayerShimmerLayout.visibility = View.GONE
    }


    fun unSelectAllTab(){
        tabMenu?.unselectAll()
    }

    fun openPlayerScreen(position: Int, bundle: Bundle = Bundle.EMPTY, isReplaceScreen:Boolean = false){
        closePIPVideoPlayer()
        var fragment: Fragment = SwipablePlayerFragment()
        setLog(
            "displayDiscover",
            "MainActivity-applyScreen()-isDisplayDiscover-$isDisplayDiscover"
        )

        if (!BaseActivity.songDataList.isNullOrEmpty()){
            if (songDataList?.size!! > nowPlayingCurrentIndex()){
                setLog("SwipablePlayerFragment", "MainActivity-applyScreen()-songDataList-${BaseActivity.songDataList?.size}-contentType-${songDataList?.get(nowPlayingCurrentIndex())?.contentType}")
                if (songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.VIDEO.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.SHORT_VIDEO.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.SHORT_FILMS.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.LIVE_CONCERT.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.TV_SHOWS.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.MOVIES.value){
                    setLog("SwipablePlayerFragment", "MainActivity-applyScreen()-closeVideoMiniplayer()-${songDataList?.get(nowPlayingCurrentIndex())?.title}")
                    closeVideoMiniplayer()
                }
            }
            isNewSwipablePlayerOpen = true
            hideMiniPlayer()
            hideStickyAds()
            fragment = SwipablePlayerFragment()

            if (songDataList != null){
                //SharedPrefHelper.getInstance().save("songlist", Gson().toJson(BaseActivity.songDataList))
                /*val str = Gson().toJson(BaseActivity.songDataList)
                val list:ArrayList<Track> = ArrayList()
                for (item in BaseActivity.songDataList?.iterator()!!){
                    list.add(item)
                }
                bundle.putSerializable("songlist", list)*/
            }
            isDisplaySkeleton(false)
        }else{
            getRecommendedContentList(5, isReplaceScreen)
            return
        }
        fragment.arguments = bundle
        if (isReplaceScreen){
            replaceFragment(R.id.fl_container, fragment, false)
        }else{
            val currentFragment = Utils.getCurrentFragment(this)
            if (currentFragment?.javaClass?.simpleName.equals(
                    MusicVideoDetailsFragment().javaClass.simpleName,
                    true
                )
            ) {
              onBackPressed()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                addFragment(R.id.fl_container, currentFragment, fragment, false)
            }
        }

        setLastClickedBottomMenu(Constant.BOTTOM_NAV_PLAYER, position)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            SavedInstanceFragment.getInstance(supportFragmentManager).pushData(outState.clone() as Bundle)
            outState.clear() // We don't want a TransactionTooLargeException, so we handle things via the SavedInstanceFragment
            setLog("TooLargeTool", "MainActivity-onSaveInstanceState")
        }catch (e:Exception){

        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        try {
            SavedInstanceFragment.getInstance(supportFragmentManager).popData()
                ?.let { super.onRestoreInstanceState(it) }
            setLog("TooLargeTool", "MainActivity-onRestoreInstanceState")
        }catch (e:Exception){

        }
    }

    fun setLastClickedBottomMenu(bottomNavMenu: String, position: Int) {
        lastItemClicked = bottomNavMenu
        lastItemClickedTop = clickedLastTop(bottomNavMenu)
        lastBottomItemPosClicked = position
        if (!bottomNavMenu.equals(Constant.BOTTOM_NAV_PLAYER)) {
            tempLastItemClicked = bottomNavMenu
            tempLastBottomItemPosClicked = position
        }
        tabMenu?.setBottomTabSelection(position)

        setLog(
            "setLastClickedBottomMenu",
            "MainActivity bottomNavMenu:${bottomNavMenu} position:${position}"
        )
    }

}
