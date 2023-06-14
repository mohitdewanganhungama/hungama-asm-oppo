package com.hungama.music.ui.main.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.AppLaunchEvent
import com.hungama.music.eventanalytic.eventreporter.LoginSuccessEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.viewmodel.*
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.getSplashAdsType
import com.hungama.music.utils.CommonUtils.getSplashWaitingTime
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.isDisplayAds
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.BuildConfig
import com.hungama.music.R
import com.hungama.music.databinding.ActivitySplashBinding
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_phone_number.ivLogo
import kotlinx.android.synthetic.main.header_main.*

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Display splash screen
 */
class SplashActivity : AppCompatActivity(), OnUserSubscriptionUpdate, OnUserSubscriptionDialogIsFinish {
    private val TAG = SplashActivity::class.java.name
    var userViewModel: UserViewModel? = null
    var splashViewModel: SplashViewModel? = null
    var binding: ActivitySplashBinding? = null
    var countDownTimer: CountDownTimer?= null

    private var mInterstitialAd: AdManagerInterstitialAd? = null
    private var mAdIsLoading: Boolean = false

    //creating Object of AdLoader
    private var adLoader: AdLoader? = null

    // simple boolean to check the status of ad
    private var adLoaded = false
    private var isVideoAd = false
    var isPopupShow = false

    private var progressStatus = 0

    //The number of milliseconds in the future from the
    //call to start() until the count down is done
    private var millisInFuture: Long = 6000 //6 seconds (make it dividable by 1000)
    private var millisInFuture1: Long = 0 //6 seconds (make it dividable by 1000)

    //The interval along the way to receive onTick() callbacks
    private val countDownInterval: Long = 1000 //1 second (don't change this value)

    private var maxWaitingTime = 5L
    private var callMain = false

    private val handler = Handler(Looper.getMainLooper())
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        NewRelic.withApplicationToken( "AA4fa3d5617ca53cbdfd3f2505941e02588a4fdaeb-NRMA" ).start(this.applicationContext)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding?.lifecycleOwner = this
        tvVersion?.text = "v${BuildConfig.VERSION_CODE.toString()}"
        Constant.VERSION = BuildConfig.VERSION_CODE.toString()
        setLog("adType", "SplashActivity-isDisplayAds-"+isDisplayAds() + " " + CommonUtils.isUserHasGoldSubscription())

            if (isDisplayAds() && !CommonUtils.isUserHasGoldSubscription()) {
                val adType = getSplashAdsType()
                setLog("adType", "SplashActivity-adType-" + adType)
                if (!TextUtils.isEmpty(adType) && adType.equals("new", true)) {
                    preCatchNativeAds()
                } else {
                    preCatchInterstritialAds()
                }
            }


        val amIsUserLoggedIn = SharedPrefHelper.getInstance().isUserLoggedIn()
        val amGetUserId = SharedPrefHelper.getInstance().getUserId()
        val amGetRealUserId = SharedPrefHelper.getInstance().get(PrefConstant.USER_ID, "")
        val amGetSilentUserId = SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID, "")
        val amLoginLog = "Splash-amIsUserLoggedIn=$amIsUserLoggedIn---amGetUserId=$amGetUserId---amGetRealUserId=$amGetRealUserId---amGetSilentUserId=$amGetSilentUserId"
        setLog("AppMigration", amLoginLog)


        try {
            val remoteConfig = Firebase.remoteConfig
            val isDisplayDiscover = remoteConfig.getBoolean("launch_section_discover")
            setLog("displayDiscover", "Splash-onCreate-isDisplayDiscover-$isDisplayDiscover")


            if(remoteConfig.getString("audio_download_count").isNotEmpty()) {
                SharedPrefHelper.getInstance().save(
                    PrefConstant.Download_Limit,
                    remoteConfig.getString("audio_download_count").toInt()
                )
            }else  SharedPrefHelper.getInstance().save(PrefConstant.Download_Limit,0)


            SharedPrefHelper.getInstance().setDisplayDiscover(isDisplayDiscover)

            setUpViewModel()

        }catch (e:Exception){
            e.printStackTrace()
        }


    }

    override fun onResume() {
        super.onResume()

        setStaticData()
        setLog("setIsFirstLaunchSong", "SplashActivity-onResume-isFirstLaunchSong-${HungamaMusicApp.getInstance().getIsFirstLaunchSong()}")
        setLog("displayDiscover", "he_api-${CommonUtils.getFirebaseConfigHEAPIData()}")
        HungamaMusicApp.getInstance().setIsFirstLaunchSong(true)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setStaticData() {
        CoroutineScope(Dispatchers.Main).launch {
            bgSplash.visibility=View.VISIBLE
            imgSplash.visibility=View.VISIBLE
            if (CommonUtils.isUserHasGoldSubscription()){
                //imgSplash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_splash_logo_gold))
                imgSplash.setImageResource(R.drawable.ic_splash_logo_gold)
            }else{
                //imgSplash.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_splash_logo))
                imgSplash.setImageResource(R.drawable.ic_splash_logo)
            }

            setLog(TAG, "showInterstitial setStaticData 3")


            val anim = AnimationUtils.loadAnimation(this@SplashActivity,R.anim.splash_transation)
            binding?.imgSplash?.animation = anim

            sendEvent()

            handler.postDelayed(runable, 6000)
        }
    }

    private fun sendEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap=HashMap<String,String>()
            dataMap[EventConstant.AB_NC_EPROPERTY] = "1"
            dataMap[EventConstant.SOURCE_EPROPERTY] = "Android"
            EventManager.getInstance().sendEvent(AppLaunchEvent(dataMap))
        }
    }

    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        splashViewModel = ViewModelProvider(
            this
        ).get(SplashViewModel::class.java)

        if (ConnectionUtil(this@SplashActivity).isOnline(false)) {

            GlobalScope.async {

                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                    callCheckSSOUserExist()
                }
                if (TextUtils.isEmpty(
                        SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID, "")
                    )
                ) {
                    setLog(
                        "UserId:=",
                        "Slient api called userid:${
                            SharedPrefHelper.getInstance().getUserId().toString()
                        }"
                    )
                    callSilentLogin()
                } else {
                    setLog("UserId:=", SharedPrefHelper.getInstance().getUserId().toString())
                    //startUpApis()
                }

                if (!SharedPrefHelper.getInstance().getUserId()?.isNullOrBlank()!!) {
                    userViewModel?.getUserSettingType(
                        this@SplashActivity,
                        Constant.TYPE_MUSICPLAYBACK_SETTING
                    )
                }

                splashViewModel?.getStoreData(this@SplashActivity)?.observe(this@SplashActivity,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                getVideoTabData()
                                getListenTabData()
                                getHomeTabData()
                                setLog(
                                    "SplashActivity",
                                    "getStoreData called store id : ${Constant.DEFAULT_STORE_ID}"
                                )
                            }

                            else -> {}
                        }
                    })

                callContinueWhereLeftListViewModel()
            }


        }


    }

    var retryCount = 0
    private fun callSilentLogin() {
        try {
            setLog(TAG, "callSilentLogin called")
            if(userViewModel==null){
                userViewModel = ViewModelProvider(
                    this@SplashActivity
                ).get(UserViewModel::class.java)
            }
            val mainJson = JSONObject()
            val clientDataJson = JSONObject()


            val usernameJson = JSONObject()
            usernameJson.put(
                "value",
                Utils.getDeviceId(this@SplashActivity)
            )
            clientDataJson.put("username", usernameJson)

            mainJson.put("process", "silent_login")
            mainJson.put("method", "signup_login")
            mainJson.put("client_data", clientDataJson)
            /*userViewModel?.silentLogin(
                this@SplashActivity,
                mainJson.toString()
            )*/
            userViewModel?.silentLogin(this@SplashActivity, mainJson.toString())


        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(this@SplashActivity,
                parentMobile!!,
                false,
                getString(R.string.discover_str_2)
            )
        }


    }

    private suspend fun callCheckSSOUserExist() {
        CoroutineScope(Dispatchers.Main).async {
            try {
                setLog(TAG, "callCheckSSOUserExist called")

                if(userViewModel==null){
                    userViewModel = ViewModelProvider(
                        this@SplashActivity
                    ).get(UserViewModel::class.java)
                }
                userViewModel?.checkSSLUserExist(this@SplashActivity)?.observe(this@SplashActivity,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setLog(TAG, "checkSSLUserExist  $it")
                                if (!it?.data?.result!!) {
                                    retryCount = 0
                                    SharedPrefHelper.getInstance().logOut()
                                    getUserSubscriptionStatus()
                                    val hashMap1 =
                                        java.util.HashMap<String, String>()
                                    hashMap1.put(EventConstant.METHOD_EPROPERTY,"app_update")
                                    setLog("appupdate","Success${hashMap1}")
                                    EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                }
                            }

                            Status.LOADING ->{
                            }

                            Status.ERROR ->{
                            }
                        }
                    })


            } catch (e: Exception) {
                e.printStackTrace()

            }
        }


    }
    fun updateViewBasedOnSubscription() {
        BaseActivity.setIsGoldUser()
        if (CommonUtils.isUserHasNoAdsSubscription()){
//            setLog(
//                "setPageBottomSpacing",
//                "MainActivity-true-isBottomStickyAdLoaded: ${BaseActivity.isBottomStickyAdLoaded}"
//            )
//            BaseActivity.isBottomStickyAdLoaded = false
        } else {
//            setLog(
//                "setPageBottomSpacing",
//                "MainActivity-false-isBottomStickyAdLoaded: ${BaseActivity.isBottomStickyAdLoaded}"
//            )
        }

        if (ivLogo != null) {
            CommonUtils.applyAppLogo(this, ivLogo)
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


    private fun movieToScreen(){
        CoroutineScope(Dispatchers.Main).launch {
            handler.removeCallbacks(runable)
            handler.removeCallbacks(adsLoadRunable)

            val intent:Intent
            setLog(TAG, "movieToScreen: Constant.DEFAULT_COUNTRY_CODE:${Constant.DEFAULT_COUNTRY_CODE}")
            if (SharedPrefHelper.getInstance().get(PrefConstant.CHOOES_LANGUAGE,true)){
                if (Constant.DEFAULT_COUNTRY_CODE?.equals("IN",true) == true){
                    intent = Intent(this@SplashActivity, ChooseLanguageActivity::class.java)
                }else{
                    SharedPrefHelper.getInstance().save(PrefConstant.CHOOES_LANGUAGE, false)
                    intent = Intent(this@SplashActivity, LoginMainActivity::class.java)
                }
            }else if (SharedPrefHelper.getInstance().isUserLoggedIn() || SharedPrefHelper.getInstance().isUserGuestLogdIn()){
                intent = Intent(this@SplashActivity, MainActivity::class.java)
            }else{
                intent = Intent(this@SplashActivity, LoginMainActivity::class.java)
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.enter, R.anim.exit)
            finish()
        }
    }

    private val runable = Runnable {

        if (isDisplayAds()){
            maxWaitingTime = getSplashWaitingTime().toLong()
            val adType = getSplashAdsType()
            if (!TextUtils.isEmpty(adType) && adType.equals("new", true)){
                initNativeAds()
            }else{
                loadAd()
            }

            //OnClickListener listeners for loadAdBtn and showAdBtn buttons
            loadNativeBtn.setOnClickListener { //calling the loadNativeAd method to load  the Native Ad
                loadNativeAd()
            }

            showNativeBtn.setOnClickListener { //calling the showNativeAd method to show the Native Ad
                showNativeAd()
            }

            hideNativeBtn.setOnClickListener { //calling the showNativeAd method to hide the Native Ad
                hideNativeAd()
                movieToScreen()
            }

            ivCloseAd?.setOnClickListener {
//                hideNativeAd()
                movieToScreen()
            }

            clStoryV2?.setOnClickListener {
                isPopupShow = true
                countDownTimer?.cancel()
                Constant.screen_name ="Splash Screen"
                CommonUtils.openSubscriptionDialogPopup(this, PlanNames.SVOD.name, "", true, this, "", null,"","",this)
            }
        }else{
            movieToScreen()
        }


    }




    override fun onDestroy() {
        super.onDestroy()

        if(mInterstitialAd!=null){
            mInterstitialAd=null
            mAdIsLoading=false
        }
        handler.removeCallbacks(runable)
        hideKeyboard()
    }


    private fun loadNativeAd() {
        // Creating  an Ad Request
        val adRequest: AdRequest = AdRequest.Builder().build()

        setLog("isEMSSSS", adRequest.isTestDevice(this).toString())
        // load Native Ad with the Request
        adLoader?.loadAd(adRequest)

        // Showing a simple Toast message to user when Native an ad is Loading
        //Toast.makeText(this, "Native Ad is loading ", Toast.LENGTH_LONG).show()
    }

    private fun showNativeAd() {
        if (adLoaded) {
            nativeTemplateView.visibility = View.VISIBLE
            ivCloseAd?.visibility = View.GONE
            if (isVideoAd){

            }else{
                setProgressbar()
            }
            clStoryV2?.visibility = View.VISIBLE
            // Showing a simple Toast message to user when an Native ad is shown to the user
            /*Toast.makeText(
                this,
                "Native Ad  is loaded and Now showing ad  ",
                Toast.LENGTH_LONG
            ).show()*/
        } else {
            //Load the Native ad if it is not loaded
            loadNativeAd()

            // Showing a simple Toast message to user when Native ad is not loaded
            //Toast.makeText(this, "Native Ad is not Loaded ", Toast.LENGTH_LONG).show()
        }
    }

    private fun hideNativeAd() {
        if (adLoaded) {
            // hiding the Native Ad when it is loaded
            nativeTemplateView.visibility = View.GONE
            ivCloseAd?.visibility = View.GONE
            clStoryV2?.visibility = View.GONE
            clProgressView?.visibility = View.GONE
            // Showing a simple Toast message to user when Native ad
            //Toast.makeText(this, "Native Ad is hidden ", Toast.LENGTH_LONG).show()
        }
    }

    private val adsLoadRunable = Runnable {
        setLog("adLogger-3", "End")
        movieToScreen()
    }

    private fun callAdsLoadHandler(){
        setLog("adLogger-2", (maxWaitingTime*1000).toString())
        handler.postDelayed(adsLoadRunable, maxWaitingTime*1000)
    }

    private fun preCatchInterstritialAds(){
        CoroutineScope(Dispatchers.Main).launch {
            val adRequest = AdManagerAdRequest.Builder().build()

            setLog("adLogger-1", "start")
            AdManagerInterstitialAd.load(
                this@SplashActivity, Constant.AD_UNIT_ID_SPLASH, adRequest,
                object : AdManagerInterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        setLog(TAG, adError.message)
                        mInterstitialAd = null
                        mAdIsLoading = false
                        val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                                "message: ${adError.message}"

                        setLog(TAG, "onAdFailedToLoad: adError:${adError}")
                        handler.removeCallbacks(adsLoadRunable)
                        setLog("adLogger-4", "Close")
                        //movieToScreen()
                    }

                    override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                        setLog(TAG, "Ad was loaded.")
                        setLog("adLogger-1", "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                        mAdIsLoading = false
                    }
                }
            )
        }
    }

    private fun loadAd() {
        setLog("adLogger-1", "loadAd-showInterstitial")
        showInterstitial()
        //callAdsLoadHandler()
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            handler.removeCallbacks(adsLoadRunable)
            setLog("adLogger-5", "Close")
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    setLog(TAG, "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    movieToScreen()
                    setLog("adLogger-6", "Close")

                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    setLog(TAG, "Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    movieToScreen()
                    setLog("adLogger-7", "Close")
                }

                override fun onAdShowedFullScreenContent() {
                    setLog(TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(this)
        } else {
            setLog(TAG, "Ad wasn't loaded.")
            movieToScreen()
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun setProgressbar(){
        clProgressView?.visibility = View.VISIBLE
        pb.progressDrawable.setColorFilter(Color.parseColor("#2b68e8"), PorterDuff.Mode.SRC_IN);
        progressStatus = 0
        //Cast long value to int value
        //When defining above variables, make sure 'progressBarMaximumValue' always rerun integer value
        //Cast long value to int value
        //When defining above variables, make sure 'progressBarMaximumValue' always rerun integer value
        val progressBarMaximumValue = (millisInFuture / countDownInterval).toInt()
        //Set ProgressBar maximum value
        //ProgressBar range (0 to maximum value)
        //Set ProgressBar maximum value
        //ProgressBar range (0 to maximum value)
        pb.max = progressBarMaximumValue

        //Initialize a new CountDownTimer instance
        //Initialize a new CountDownTimer instance
        countDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                //Another one second passed
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                val text = "$minutes:$seconds"

                //tv.setText(millisUntilFinished / 1000.toString() + "  Seconds...")
                tvProgress?.text = text
                setLog("TimeStart", millisInFuture.toString() + " " + (millisUntilFinished / 1000).toString() + "  Seconds...")
                //Each second ProgressBar progress counter added one
                progressStatus += 1
                pb.progress = progressStatus


                millisInFuture1 = millisUntilFinished
            }

            override fun onFinish() {
//                ivCloseAd?.visibility = View.VISIBLE
                //Do something when count down end.
                progressStatus += 1
                pb.progress = progressStatus
                //tv.setText(progressStatus - pb.max.toString() + "  Seconds...")
                setLog("TimeStart", (progressStatus - pb.max).toString() + "  Seconds...")
                if (!isPopupShow) {
                    movieToScreen()
                }
            }
        }.start()
    }

    private fun preCatchNativeAds(){
        CoroutineScope(Dispatchers.Main).launch {
            //val testDeviceIds = Arrays.asList("E3E4B1D1BAF3339A44199F739A633150")
            //val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            //MobileAds.setRequestConfiguration(configuration)
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

            //Initializing the AdLoader   objects
            //AD_MANAGER_NATIVE_AD_UNIT_ID_1

            adLoader = AdLoader.Builder(this@SplashActivity, Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_1).forNativeAd(object : NativeAd.OnNativeAdLoadedListener{
                private val background: ColorDrawable? = null
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    val styles =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(background).build()

                    nativeTemplateView.setStyles(styles)
                    nativeTemplateView.setNativeAd(nativeAd)
                    adLoaded = true
                    setLog("MyApp", "-onNativeAdLoaded-adLoaded - $adLoaded")
                    setLog("SplashNativeAd", "adLoaded-$adLoaded")
                    // Showing a simple Toast message to user when Native an ad is Loaded and ready to show
                    /*Toast.makeText(
                        this@SplashActivity,
                        "Native Ad is loaded ,now you can show the native ad",
                        Toast.LENGTH_LONG
                    ).show()*/
                    if (nativeAd.mediaContent != null){
                        isVideoAd = nativeAd.mediaContent?.hasVideoContent()!!
                        if (isVideoAd){
                            millisInFuture = (nativeAd.mediaContent?.duration?.toLong()!! * 1000) + 1000
                            nativeAd.mediaContent?.videoController?.videoLifecycleCallbacks =
                                object : VideoLifecycleCallbacks() {
                                    /** Called when video playback first begins.  */
                                    override fun onVideoStart() {
                                        // Do something when the video starts the first time.
                                        setLog("MyApp", "Video Started")
                                    }

                                    /** Called when video playback is playing.  */
                                    override fun onVideoPlay() {
                                        // Do something when the video plays.
                                        setLog("MyApp", "Video Played")
                                        setProgressbar()
                                    }

                                    /** Called when video playback is paused.  */
                                    override fun onVideoPause() {
                                        // Do something when the video pauses.
                                        setLog("MyApp", "Video Paused")
                                    }

                                    /** Called when video playback finishes playing.  */
                                    override fun onVideoEnd() {
                                        // Do something when the video ends.
                                        setLog("MyApp", "Video Ended")
                                        ivCloseAd?.visibility = View.VISIBLE
                                    }

                                    /** Called when the video changes mute state.  */
                                    override fun onVideoMute(isMuted: Boolean) {
                                        // Do something when the video is muted.
                                        setLog("MyApp", "Video Muted")
                                    }
                                }
                        }
                    }
                }
            }).withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    handler.removeCallbacks(adsLoadRunable)
                    //showNativeAd()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    val messageModel = MessageModel(p0.message+":-", MessageType.NEUTRAL, true)
                    //CommonUtils.showToast(this@SplashActivity, messageModel)
                    adLoaded = false
                    setLog("SplashNativeAd", "onAdFailedToLoad-adLoaded-$adLoaded - p0.message-${p0.message}")
                    handler.removeCallbacks(adsLoadRunable)
                    hideNativeAd()
                    //movieToScreen()
                }
            }).withNativeAdOptions(adOptions).build()
            loadNativeAd()
        }

    }

    private fun initNativeAds(){
        setLog("SplashNativeAd", "initNativeAds-adLoaded-$adLoaded")
        if (adLoaded) {
            handler.removeCallbacks(adsLoadRunable)
            showNativeAd()
        }else{
            movieToScreen()
        }

        //callAdsLoadHandler()
    }

    private suspend fun callContinueWhereLeftListViewModel() {
        if(!SharedPrefHelper.getInstance().getUserId()?.isNullOrBlank()!!){
            GlobalScope.async {

                setLog(TAG, "callContinueWhereLeftListViewModel called")

                if (ConnectionUtil(this@SplashActivity).isOnline(false)) {
                    val continueWhereLeftViewModel = ViewModelProvider(
                        this@SplashActivity
                    ).get(RecentlyPlayViewModel::class.java)

                    continueWhereLeftViewModel?.getContinueWhereLeftList(this@SplashActivity,Constant.DISCOVER_TAB, HeadItemsItem())?.observe(this@SplashActivity,
                        Observer {
                            when(it.status){
                                Status.SUCCESS->{
                                    setLog("continue-watching", "continue-watching 1")
                                    if (it != null) {
                                        HungamaMusicApp?.getInstance()?.setContinueWhereLeftData(it?.data!!)
                                    }
                                }

                                Status.LOADING ->{
                                }

                                Status.ERROR ->{
                                    setLog("continue-watching", "setUpContinueWhereLeftListViewModel: ${it.message!!}")

                                }
                            }
                        })
                }
            }
        }

    }


    /**
     * initialise view model and setup-observer
     */
    private fun getVideoTabData() {
        try {
            val videoViewModel = ViewModelProvider(
                this
            ).get(VideoViewModel::class.java)


            MainActivity.lastBottomItemPosClicked = 2
            val url = WSConstants.METHOD_VIDEO
            videoViewModel?.getWatchVidelList(this@SplashActivity, url)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            HungamaMusicApp.getInstance().setCacheBottomTab(
                                Constant.CACHE_VIDEOS_PAGE,
                                it?.data!!
                            )
                            setLog(
                                "SplashActivity",
                                "setUpViewModel static call:${Constant.CACHE_VIDEOS_PAGE} videoHomeModel"
                            )
                        }

                        else -> {}
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * initialise view model and setup-observer
     */
    private fun getListenTabData() {
        try {
            val musicViewModel = ViewModelProvider(
                this
            ).get(MusicViewModel::class.java)
            MainActivity.lastBottomItemPosClicked = 1
            val url = WSConstants.METHOD_MUSIC
            musicViewModel?.getMusicList(this@SplashActivity, url)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            HungamaMusicApp.getInstance().setCacheBottomTab(
                                Constant.CACHE_MUSIC_PAGE,
                                it?.data!!
                            )
                            setLog(
                                "SplashActivity",
                                "setUpViewModel static call:${Constant.CACHE_MUSIC_PAGE} musicHomeModel"
                            )
                        }
                        else -> {}
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * initialise view model and setup-observer
     */
    private fun getHomeTabData() {
        try {
            val homeViewModel = ViewModelProvider(
                this
            ).get(HomeViewModel::class.java)
            MainActivity.lastBottomItemPosClicked = 0
            homeViewModel?.getHomeListDataLatest(this@SplashActivity, WSConstants.METHOD_HOME)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                HungamaMusicApp.getInstance().setCacheBottomTab(
                                    Constant.CACHE_DISCOVER_PAGE,
                                    it?.data!!
                                )
                                setLog(
                                    "SplashActivity",
                                    "setUpViewModel static call:${Constant.CACHE_DISCOVER_PAGE} discoverHomeModel"
                                )
                            }

                            else -> {}
                        }
                    })
        } catch (ee: Exception) {
            ee.printStackTrace()
        }

    }

    override fun isFinish(isFinish: Boolean) {
        setLog("hfakhfa", "true")
        if (!callMain)
        {
            isPopupShow = false
        }
        millisInFuture = millisInFuture1
        setProgressbar()
    }

    override fun isDestroy(destroy: Boolean) {
        callMain = destroy
    }
}