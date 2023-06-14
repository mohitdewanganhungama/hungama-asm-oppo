package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appsflyer.AppsFlyerLib
import com.appsflyer.CreateOneLinkHttpTask
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.gson.Gson
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.LoginErrorEvent
import com.hungama.music.eventanalytic.eventreporter.LoginSuccessEvent
import com.hungama.music.home.eventreporter.LoginEvent
import com.hungama.music.home.eventreporter.TappedTCContinueEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.base.WorkManagerClass
import com.hungama.music.ui.main.view.activity.EnterEmailActivity
import com.hungama.music.ui.main.view.activity.EnterMobileNumberActivity
import com.hungama.music.ui.main.view.activity.LoginMainActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getUserSubscriptionTypeId
import com.hungama.music.utils.CommonUtils.openCommonWebView
import com.hungama.music.utils.CommonUtils.setAppButton2
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.EXTRA_MORE_PAGE_NAME
import com.hungama.music.utils.Constant.EXTRA_PAGE_DETAIL_NAME
import com.hungama.music.utils.Constant.SIGNIN_WITH_ALL
import com.hungama.music.utils.Constant.SIGNIN_WITH_EMAIL
import com.hungama.music.utils.Constant.SIGNIN_WITH_MOBILE
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_ADS_FREE
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_FREE
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_WITH_ADS
import com.hungama.music.utils.Constant.aboutHungama
import com.hungama.music.utils.Constant.helpAndSupport
import com.hungama.music.utils.Constant.isProfileEditPage
import com.hungama.music.utils.Constant.privacyPolicy
import com.hungama.music.utils.Constant.termAndConditionLink
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.progress
import kotlinx.coroutines.*
import org.json.JSONObject

class ProfileFragment : BaseFragment(), LogoutDialog.LogoutListener, OnUserSubscriptionUpdate {
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    var subscription_type = -1
    var isUpgradable = 1
    var action = false
    var userViewModel: UserViewModel? = null
    val RC_SIGN_IN = 100
    lateinit var googleSignInClient: GoogleSignInClient
    var callbackManager: CallbackManager?=null
    lateinit var auth: FirebaseAuth
    val provider = OAuthProvider.newBuilder("apple.com")
    var SIGNUPMETHOD = "EMAIL"
    var deeplinkPageName = ""
    var deeplinkPageName2 = ""
    var userCoins = 0

    private fun displayUserCoins(){

        try {
            if(isAdded()){
                setLog("GM-SDK-APP", "displayUserCoins-ProfileFragment-after-userCoins-$userCoins")
                //            val userCoinDetailRespModel= SharedPrefHelper?.getInstance()?.getObjectUserCoin(
//                PrefConstant.USER_COIN)

                var userCoinDetailRespModel= SharedPrefHelper?.getInstance()?.getObjectUserCoin(
                    PrefConstant.USER_COIN)
                setLog("GM-SDK-APP", "fillUI: before userCoinDetailRespModel-"+userCoinDetailRespModel?.actions?.get(0)?.total)

                var gmfSDKCoins = userCoins
                setLog("GM-SDK-APP", "fillUI: before GamificationSDK.getPoints()-${gmfSDKCoins} userCoins:${userCoins}")
                if (gmfSDKCoins < 0){
                    gmfSDKCoins = 0
                }
                setLog("GM-SDK-APP", "fillUI: after GamificationSDK.getPoints()-${gmfSDKCoins} userCoins:${userCoins}")

                setLog("GM-SDK-APP", "fillUI: after userCoinDetailRespModel-"+userCoinDetailRespModel?.actions?.get(0)?.total)

                if(userCoinDetailRespModel?.actions != null){
                    userCoinDetailRespModel?.actions?.get(0)?.total=gmfSDKCoins
                    if(userCoinDetailRespModel?.actions?.get(0)?.total!!<=0){
                        userCoinDetailRespModel?.actions?.get(0)?.total=0
                    }
                    val coin = CommonUtils.covertNumberToCurrencyFormat(userCoinDetailRespModel?.actions?.get(0)?.total.toString())
                    tvCoinCount?.setText(coin)

                    setLog("GM-SDK-APP", " withoud actions tvCoinCount-${coin}")
                }else{
                    userCoinDetailRespModel = UserCoinDetailRespModel()
                    val action = UserCoinDetailRespModel.Action()
                    action.total = gmfSDKCoins
                    if(action.total!!<=0){
                        action.total=0
                    }

                    userCoinDetailRespModel?.actions=ArrayList()
                    userCoinDetailRespModel.actions.add(action)
                    setLog("GM-SDK-APP", "fillUI: after userCoinDetailRespModel123-"+userCoinDetailRespModel?.actions?.get(0)?.total)
                    val coin = CommonUtils.covertNumberToCurrencyFormat(userCoinDetailRespModel?.actions?.get(0)?.total.toString())
                    tvCoinCount?.setText(coin)

                    setLog("GM-SDK-APP", " withoud actions tvCoinCount-${coin}")
                }


                if (userCoinDetailRespModel != null){
                    SharedPrefHelper.getInstance().saveObjectUserCoin(PrefConstant.USER_COIN, userCoinDetailRespModel)
                }
            }

        }catch (e:Exception){

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.setLog("ProfileFragment", "onCreate: hide")
        hideBottomNavigationAndMiniplayer()
        setLog(TAG, "onCreate:oncreate work ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

/*        if (SharedPrefHelper.getInstance().isUserLoggedIn())
            SharedPrefHelper.getInstance().getUserId()
                ?.let { (activity as BaseActivity).setSongDuration(it) }*/

        val viewProfileFragment: View =
            inflater.inflate(R.layout.fragment_profile, container, false)


        return viewProfileFragment
    }

    @SuppressLint("WrongConstant")
    override fun initializeComponent(view: View) {
        setLog(TAG, "initializeComponent: wrking")
        if (arguments != null){
            if (requireArguments().containsKey(EXTRA_PAGE_DETAIL_NAME)){
                deeplinkPageName = requireArguments().getString(EXTRA_PAGE_DETAIL_NAME).toString()
            }
            if (requireArguments().containsKey(EXTRA_MORE_PAGE_NAME)){
                deeplinkPageName2 = requireArguments().getString(EXTRA_MORE_PAGE_NAME).toString()
            }
        }

        if (SharedPrefHelper.getInstance().isUserLoggedIn()){
        }
        else
        {
                SharedPrefHelper.getInstance().getUserId()?.let { (activity as BaseActivity).setSongDuration(it) }
        }


        setAppButton2(requireContext(), llUpgradePlan)
        llLoginPlan.visibility = View.GONE
        llNonLogin.visibility = View.GONE

        configureGoogleClient()
        configureFacebook()


        ivBack?.setOnClickListener {

            CommonUtils.PageViewEvent("","","","",
                "Profile", MainActivity.lastItemClicked + "_" + MainActivity.headerItemName,
            MainActivity.headerItemPosition.toString())
            backPress()
        }
        //tvActionBarHeading.text = "nitinsing09"
        //tvActionBarHeading?.text = SharedPrefHelper?.getInstance()?.getUsername()


        llGeneralSettings?.setOnClickListener(this)
        llMusicSettings?.setOnClickListener(this)
        llVideoSettings?.setOnClickListener(this)
        llDownloadSettings?.setOnClickListener(this)
        llVoiceAssistants.setOnClickListener(this)
        llSubscriptionSettings.setOnClickListener(this)
        llAllLogins.setOnClickListener(this)
        llLoginBtn.setOnClickListener(this)
        llGoogle.setOnClickListener(this)
        llFacebook.setOnClickListener(this)
        llAppleLogin.setOnClickListener(this)
        llLoginWithMobile.setOnClickListener(this)
        llLoginWithEmail.setOnClickListener(this)
        llHelpAndSupport.setOnClickListener(this)
        llTermsAndConditions.setOnClickListener(this)
        llPrivacyPolicy.setOnClickListener(this)
        llActivateTV.setOnClickListener(this)
        llDeleteAccount.setOnClickListener(this)
        llAboutHungama.setOnClickListener(this)
        llLogout?.setOnClickListener(this)
        llUpgradePlan.setOnClickListener(this)
        rlEarnCoin.setOnClickListener(this)
        rlRedeemCoin.setOnClickListener(this)
        llShareApp.setOnClickListener(this)


        initApiCall()
        auth = FirebaseAuth.getInstance()


        setLog("alhlghal", MainActivity.lastItemClicked + " " +
                MainActivity.tempLastItemClicked + " " + MainActivity.headerItemName)

        CoroutineScope(Dispatchers.IO).launch {

            CommonUtils.PageViewEvent("","","","",
                MainActivity.headerItemName + "_" +MainActivity.lastItemClicked + MainActivity.subHeaderItemName,
            "profile","")
            MainActivity.subHeaderItemName = ""
        }
    }

    private fun initApiCall(){
        val isUserLoggedIn = SharedPrefHelper.getInstance().isUserLoggedIn()
        setLog("UserProfile", "ProfileFragment-initApiCall()-isUserLoggedIn=$isUserLoggedIn")
        if (isUserLoggedIn) {
            progress?.visibility = View.VISIBLE
            setLog("setProfileData","initApiCall-setProfileData-called")
            setProfileData()
            getUserProfile()
            setUpViewModel()
            llUser?.setOnClickListener(this)
        } else {
            setUserSubscription(subscription_type, isUpgradable)
        }
        if (!TextUtils.isEmpty(deeplinkPageName)){
            redirectToDeeplinkPage()
        }
    }



    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(v: View) {
        super.onClick(v)
            if (v == llUser) {
                showBottomNavigationAndMiniplayer()
                addFragment(R.id.fl_container, this, SettingsFragment(), false)
            } else if (v == llGeneralSettings) {
                addFragment(R.id.fl_container, this, GeneralSetting(), false)
            } else if (v == llMusicSettings) {
                addFragment(R.id.fl_container, this, MusicPlayBackSetting(), false)
            } else if (v == llVideoSettings) {
                addFragment(R.id.fl_container, this, VideoPlayBackSetting(), false)
            } else if (v == llDownloadSettings) {
                addFragment(R.id.fl_container, this, DownloadsSetting(), false)
            } else if (v == llVoiceAssistants) {
                addFragment(R.id.fl_container, this, UserProfileVoiceAssistantSettingsFragment(), false)
            } else if (v == llSubscriptionSettings) {
                showBottomNavigationAndMiniplayer()
                addFragment(R.id.fl_container, this, SubscriptionFragment(), false)
            } else if (v == llGoogle) {
                callLoginEvent("Gmail")
                signInToGoogle()
            } else if (v == llFacebook) {
                callLoginEvent("Facebook")
                signInToFacebook()
            } else if (v == llAppleLogin) {

                /*action = true
                val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                intent.putExtra("action", SIGNIN_WITH_ALL)
                startActivity(intent)*/
                callLoginEvent("Apple")
                signInToApple()
                //signInToFacebook()
            } else if (v == llLoginWithMobile) {
                callLoginEvent("Phone")
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(), llLoginWithMobile!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                } catch (e: Exception) {

                }
                /* val intent = Intent(requireActivity(), EnterMobileNumberActivity::class.java)
            startActivity(intent)*/
                action = true
                val intent = Intent(requireActivity(), EnterMobileNumberActivity::class.java)
                intent.putExtra("action", SIGNIN_WITH_MOBILE)
                startActivity(intent)
            } else if (v == llLoginWithEmail) {
                callLoginEvent("Email")
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(), llLoginWithEmail!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                } catch (e: Exception) {

                }
                /*val intent = Intent(requireActivity(), EnterEmailActivity::class.java)
            startActivity(intent)*/
                action = true
                val intent = Intent(requireActivity(), EnterEmailActivity::class.java)
                intent.putExtra("action", SIGNIN_WITH_EMAIL)
                startActivity(intent)

            } else if (v == llLogout) {
                val logoutDialog = LogoutDialog(this)
                if (!logoutDialog.isVisible) {
                    logoutDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
                    CommonUtils.PageViewEvent("","","","",
                        "Profile", "Settings_logout","")

                } else {
                    logoutDialog.dismiss()
                }
            } else if (v == llUpgradePlan) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(), llUpgradePlan!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                } catch (e: Exception) {

                }
                //addFragment(R.id.fl_container,this, BlankFragment(),false)
                setEventModelDataAppLevel(
                    "profile",
                    "",
                    "profile"
                )
                Constant.screen_name ="profile Screen"
                CommonUtils.openSubscriptionDialogPopup(
                    requireContext(),
                    PlanNames.SVOD.name,
                    "profile",
                    true,
                    this,
                    "",
                    null,Constant.drawer_default_buy_hungama_gold
                )

            } else if (v == rlEarnCoin) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(), rlEarnCoin!!,
                            HapticFeedbackConstants.CONTEXT_CLICK, false
                        )
                    }
                } catch (e: Exception) {

                }
                addFragment(R.id.fl_container, this, EarnCoinDetailFragment(), false)
            } else if (v == rlRedeemCoin) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(), rlRedeemCoin!!,
                            HapticFeedbackConstants.CONTEXT_CLICK, false
                        )
                    }
                } catch (e: Exception) {

                }
                addFragment(R.id.fl_container, this, EarnCoinAllTabFragement(), false)
            } else if (v == llPrivacyPolicy) {
                openCommonWebView(privacyPolicy, requireActivity())
                CommonUtils.PageViewEvent("","","","",
                    "Profile", "Settings_privacypolicy","")

            } else if (v == llTermsAndConditions) {
                val hashMap = HashMap<String,String>()
                EventManager.getInstance().sendEvent(TappedTCContinueEvent(hashMap))
                openCommonWebView(termAndConditionLink, requireActivity())
                CommonUtils.PageViewEvent("","","","",
                    "Profile", "Settings_terms&conditions","")

            } else if (v == llDeleteAccount) {
                val deleteAccountBottomsheet = DeleteAccountBottomsheet()
                deleteAccountBottomsheet.show(activity?.supportFragmentManager!!, "open dialog")
                CommonUtils.PageViewEvent("","","","",
                    "Profile", "Settings_deleteyouraccount","")

            }else if (v == llAboutHungama) {
                openCommonWebView(aboutHungama, requireActivity())

            }else if (v == llHelpAndSupport) {
                openCommonWebView(helpAndSupport, requireActivity())
                CommonUtils.PageViewEvent("","","","",
                    "Profile", "Settings_help&support","")

            }else if (v == llShareApp) {
                if (isShareAppOnClick()){
                    setLog(TAG, "initializeComponent: if condition working")
                    generateInviteLink()
                }
                else{
                    setLog(TAG, "initializeComponent: else condition working")
                }
            }

    }

    private fun callLoginEvent(loginWith: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap=HashMap<String,String>()
            dataMap.put(EventConstant.LOGIN_EPROPERTY,""+false)
            dataMap.put(EventConstant.METHOD_EPROPERTY,loginWith)
            dataMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"Setting")
            dataMap.put(EventConstant.SOURCE_EPROPERTY,"Setting")
            EventManager.getInstance().sendEvent(LoginEvent(dataMap))
        }
    }

    var lastShareAppClickedTime: Long = 0
    open fun isShareAppOnClick(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastShareAppClickedTime < Constant.MAX_CLICK_INTERVAL) {
            lastShareAppClickedTime = SystemClock.elapsedRealtime()
            return false
        }
        lastShareAppClickedTime = SystemClock.elapsedRealtime()
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onDestroy() {
        CommonUtils.setLog("ProfileFragment", "onDestroy: show")
        showBottomNavigationAndMiniplayer()
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden && activity != null) {
            setLog("onHiddenChanged", "ProfileFragment: $hidden")
            showBottomNavigationAndMiniplayer()
        } else {
            setLog("onHiddenChanged", "ProfileFragment: $hidden")
            hideBottomNavigationAndMiniplayer()
            setLog("redeemCoin", "onHiddenChanged-ProfileFragment-userCoins-$userCoins")
            displayUserCoins()
        }
    }

    private fun setUserSubscription(subscriptionType: Int, isUpgradable:Int) {
        setLog("UserProfile", "ProfileFragment-setUserSubscription()-subscriptionType=$subscriptionType")
        //val cornerRadius = 7f
        val cornerRadius = floatArrayOf(
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//bottom left
            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()//bottom left
        )
        when (subscriptionType) {
            SUBSCRIPTION_TYPE_FREE -> {
                val colors = intArrayOf(Color.parseColor("#FF2B68E8"),
                    Color.parseColor("#FF2CA1F7"))
                val position = floatArrayOf(
                    0f,
                    1f
                )

                val startX = 84.564f
                val startY = 256.688f
                val endX = 268.92f
                val endY = 12.688f
                CommonUtils.applyAppButtonGradient(
                    startX,
                    startY,
                    endX,
                    endY,
                    cornerRadius,
                    colors,
                    position,
                    requireContext(),
                    llLoginPlan
                )
                //llLoginPlan.setBackgroundResource(R.drawable.bg_profile_free)
                llEditProfile.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_profile_subscription_type_free
                )
                divider?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                subscriptionPlanOrNot(0, subscriptionType, isUpgradable)
            }
            SUBSCRIPTION_TYPE_GOLD -> {
                val colors = intArrayOf(Color.parseColor("#FFB46A11"),
                    Color.parseColor("#FFD68D15"),
                    Color.parseColor("#FFE7AC18"),
                    Color.parseColor("#FFF8C73D"))
                val position = floatArrayOf(
                    0f,
                    0.345f,
                    0.675f,
                    1f
                )

                val startX = -103.356f
                val startY = 130.54f
                val endX = 326.268f
                val endY = 128.832f
                CommonUtils.applyAppButtonGradient(
                    startX,
                    startY,
                    endX,
                    endY,
                    cornerRadius,
                    colors,
                    position,
                    requireContext(),
                    llLoginPlan
                    )
                //llLoginPlan.setBackgroundResource(R.drawable.bg_profile_gold)
                llEditProfile.setBackgroundResource(R.drawable.bg_profile_black)
                divider?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWarmGrey))
                subscriptionPlanOrNot(1, subscriptionType, isUpgradable)
            }
            SUBSCRIPTION_TYPE_ADS_FREE -> {
                val colors = intArrayOf(Color.parseColor("#FFDDB562"),
                    Color.parseColor("#FFB18323"))
                val position = floatArrayOf(
                    0f,
                    1f
                )

                val startX = 216.432f
                val startY = 100.528f
                val endX = -2.9220477E-16f
                val endY = 140.544f
                CommonUtils.applyAppButtonGradient(
                    startX,
                    startY,
                    endX,
                    endY,
                    cornerRadius,
                    colors,
                    position,
                    requireContext(),
                    llLoginPlan
                )
                //llLoginPlan.setBackgroundResource(R.drawable.bg_profile_ad_free)
                llEditProfile.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_profile_subscription_type_add_free
                )
                divider?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWarmGrey))
                subscriptionPlanOrNot(1, subscriptionType, isUpgradable)
            }
            SUBSCRIPTION_TYPE_GOLD_WITH_ADS -> {
                val colors = intArrayOf(Color.parseColor("#FFD79617"),
                    Color.parseColor("#FFB46A11"),
                    Color.parseColor("#FFD68D15"),
                    Color.parseColor("#FFE7AC18"),
                    Color.parseColor("#FFF8C73D"))
                val position = floatArrayOf(
                    0f,
                    0f,
                    0.32f,
                    0.68f,
                    1f
                )

                val startX = -2.2727037E-16f
                val startY = 122f
                val endX = 324f
                val endY = 122f
                CommonUtils.applyAppButtonGradient(
                    startX,
                    startY,
                    endX,
                    endY,
                    cornerRadius,
                    colors,
                    position,
                    requireContext(),
                    llLoginPlan
                )
                //llLoginPlan.setBackgroundResource(R.drawable.bg_profile_gamification)
                llEditProfile.setBackgroundResource(R.drawable.bg_profile_black)
                divider?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWarmGrey))
                subscriptionPlanOrNot(1, subscriptionType, isUpgradable)
            }
            -1->{
                subscriptionPlanOrNot(-1, subscriptionType, isUpgradable)
            }
            else -> {
                val colors = intArrayOf(Color.parseColor("#FF2B68E8"),
                    Color.parseColor("#FF2CA1F7"))
                val position = floatArrayOf(
                    0f,
                    1f
                )

                val startX = 84.564f
                val startY = 256.688f
                val endX = 268.92f
                val endY = 12.688f
                CommonUtils.applyAppButtonGradient(
                    startX,
                    startY,
                    endX,
                    endY,
                    cornerRadius,
                    colors,
                    position,
                    requireContext(),
                    llLoginPlan
                )
                //llLoginPlan.setBackgroundResource(R.drawable.bg_profile_free)
                llEditProfile.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_profile_subscription_type_free
                )
                divider?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                subscriptionPlanOrNot(0, subscriptionType, isUpgradable)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setLog(TAG, "onResume: resume work ")
        setLog("redeemCoin", "onResume-ProfileFragment-userCoins-$userCoins")
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            displayUserCoins()
        }
        if (action){
            setLog("UserProfile", "ProfileFragment-onResume()-action=$action")
            action = false
            initApiCall()
        }

    }
    private fun setUpViewModel() {
        /*if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE,""))){
            ImageLoader.loadImage(requireContext(),ivUser,SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE,""),R.drawable.ic_no_user_img)
        }*/
        userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)

        getUserSubscriptionStatus()
    }


    fun getUserSubscriptionStatus() {
        if (ConnectionUtil(requireContext()).isOnline) {

            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(
                requireContext()
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                fillUI(it?.data!!)
                            }
                            (requireActivity() as MainActivity)?.updateViewBasedOnSubscription()
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
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



    private fun fillUI(userSubscriptionModel: UserSubscriptionModel) {
        if (userSubscriptionModel != null && userSubscriptionModel?.data != null && userSubscriptionModel?.data?.status!!) {



            subscription_type = getUserSubscriptionTypeId(userSubscriptionModel)
            if (!TextUtils.isEmpty(userSubscriptionModel?.data?.user?.userMembershipType)) {
                tvMembershipType?.text =
                    userSubscriptionModel?.data?.user?.userMembershipType
                tvSubscriptionPlanName.text =
                    getString(R.string.general_setting_str_41) + " " + userSubscriptionModel?.data?.user?.userMembershipType
            } else if (subscription_type == SUBSCRIPTION_TYPE_FREE) {
                tvMembershipType?.text = getString(R.string.profile_str_31)
                tvSubscriptionPlanName.text =
                    getString(R.string.general_setting_str_41) + " " + getString(R.string.profile_str_31)
            } else {
                tvMembershipType?.text = "-"
                tvSubscriptionPlanName.text = "-"
            }
            isUpgradable = 0
            if (userSubscriptionModel?.data?.profile_app_config != null){
                isUpgradable = userSubscriptionModel?.data?.profile_app_config?.upgradable!!
            }

            setUserSubscription(subscription_type, isUpgradable)

            displayUserCoins()

            if(userSubscriptionModel?.data?.subscription!=null){
                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.NPAY_CURRENCY, ""+ userSubscriptionModel?.data?.subscription?.currency)
                userDataMap.put(EventConstant.NPAY_EXPIRY, ""+ userSubscriptionModel?.data?.subscription?.subscriptionEndDate)
                userDataMap.put(EventConstant.NPAY_FREE_TRIAL_EXPIRY, ""+ userSubscriptionModel?.data?.subscription?.trialExpiryDaysLeft)
                userDataMap.put(EventConstant.NPAY_IS_FREE_TRIAL, ""+ userSubscriptionModel?.data?.subscription?.trialTaken)
                userDataMap.put(EventConstant.NPAY_PAYMENTMODE, ""+ userSubscriptionModel?.data?.subscription?.paymentSourceDetails)
                userDataMap.put(EventConstant.NPAY_PLANID, ""+ userSubscriptionModel?.data?.subscription?.planDetailsId)
                userDataMap.put(EventConstant.NPAY_PLANNAME, ""+ userSubscriptionModel?.data?.subscription?.planName)
                userDataMap.put(EventConstant.NPAY_SUBSCRIBER_TYPE, ""+ userSubscriptionModel?.data?.subscription?.planType)
                userDataMap.put(EventConstant.NPAY_TRANSACTION_MODE, ""+ userSubscriptionModel?.data?.subscription?.paymentSource)

                userDataMap.put(EventConstant.SUBSCRIPTION_STATUS, ""+ userSubscriptionModel?.data?.user?.userMembershipType)
                userDataMap.put(EventConstant.SUBSCRIPTION_START_DATE, ""+ userSubscriptionModel?.data?.subscription?.subscriptionStartDate)
                userDataMap.put(EventConstant.SUBSCRIPTION_END_DATE, ""+ userSubscriptionModel?.data?.subscription?.subscriptionEndDate)
                userDataMap.put(EventConstant.SUBSCRIPTION_PAYMENT_SOURCE, ""+ userSubscriptionModel?.data?.subscription?.paymentSource)
                userDataMap.put(EventConstant.SUBSCRIPTION_PAYMENT_SOURCE_DETAIL, ""+ userSubscriptionModel?.data?.subscription?.paymentSourceDetails)
                userDataMap.put(EventConstant.SUBSCRIPTION_PLAN_TYPE, ""+ userSubscriptionModel?.data?.subscription?.planType)

                if(userSubscriptionModel?.data?.autoRenewal!=null){
                    userDataMap.put(EventConstant.NPAY_IS_AUTORENEWABLE, ""+ userSubscriptionModel?.data?.autoRenewal?.status)
                }
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            }

        }
    }

    private fun subscriptionPlanOrNot(id: Int, subscriptionType: Int, isUpgradable:Int) {
        setLog("profileFragment", "subscriptionPlanOrNot-id-$id-subscriptionType-$subscriptionType-isUpgradable-$isUpgradable")
        llLogout?.visibility = View.VISIBLE
        llDeleteAccount?.visibility = View.VISIBLE
        tvLogout?.text = getString(R.string.profile_str_33)
        divider11?.visibility = View.VISIBLE
        divider12?.visibility = View.VISIBLE

        if (id == 0) {
            tvUserName?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvHeaderTitle?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            ivProfileDetail?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            tvCoinTitle?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvCoinCount?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvInfo?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvEarnCoint?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvRedeemCoin?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            ivEarnCoin?.setImageDrawable(requireContext().faDrawable(R.string.icon_earn_coin, R.color.colorWhite))
            ivRedeemCoin?.setImageDrawable(requireContext().faDrawable(R.string.icon_redeem_coin, R.color.colorWhite))
            tvUnlimited?.visibility = View.VISIBLE
            tvUpgradePlan?.text = getString(R.string.drawer_download_all_CTA)
            rlUpgradePlan?.visibility = View.VISIBLE
            ivGoldSubscriptionType.visibility = View.GONE
            gold_user.visibility = View.GONE
            llLoginPlan.visibility = View.VISIBLE
            llNonLogin.visibility = View.GONE
        } else if (id == 1) {
            tvUserName?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            tvHeaderTitle?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorBlack
                )
            )
            ivProfileDetail?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorBlack
                )
            )
            tvCoinTitle?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            tvCoinCount?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            tvInfo?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            tvEarnCoint?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            tvRedeemCoin?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            ivEarnCoin?.setImageDrawable(requireContext().faDrawable(R.string.icon_earn_coin, R.color.colorBlack))
            ivRedeemCoin?.setImageDrawable(requireContext().faDrawable(R.string.icon_redeem_coin, R.color.colorBlack))
            if (subscriptionType == SUBSCRIPTION_TYPE_GOLD) {
                rlUpgradePlan?.visibility = View.GONE
//                ivGoldSubscriptionType.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.ic_coin
//                    )
//                )
                gold_user.visibility =View.VISIBLE
                tvUnlimited?.visibility = View.GONE
                tvUpgradePlan?.text = getString(R.string.general_setting_str_46)
            } else if (subscriptionType == SUBSCRIPTION_TYPE_ADS_FREE) {
                ivGoldSubscriptionType.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_ad_subscription_type
                    )
                )
                tvUnlimited?.visibility = View.GONE
                tvUpgradePlan?.text = getString(R.string.general_setting_str_46)
            } else if (subscriptionType == SUBSCRIPTION_TYPE_GOLD_WITH_ADS) {
//                ivGoldSubscriptionType.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.ic_coin
//                    )
//                )
                gold_user.visibility = View.VISIBLE
                tvUnlimited?.visibility = View.GONE
                tvUpgradePlan?.text = getString(R.string.general_setting_str_46)
            }

            llLoginPlan.visibility = View.VISIBLE
            llNonLogin.visibility = View.GONE
            if (isUpgradable == 1){
                rlUpgradePlan?.visibility = View.VISIBLE
            }else{
                rlUpgradePlan?.visibility = View.GONE
            }
        } else {
            llDeleteAccount?.visibility = View.GONE
            //tvLogout?.text = getString(R.string.login)
            llLogout?.visibility = View.GONE
            divider11?.visibility = View.GONE
            divider12?.visibility = View.GONE
            llLoginPlan.visibility = View.GONE
            llNonLogin.visibility = View.VISIBLE
            tvActionBarHeading?.text = getString(R.string.profile_str_45)
            tvUnlimited?.visibility = View.VISIBLE
            tvUpgradePlan?.text = getString(R.string.drawer_download_all_CTA)
            tvSubscriptionPlanName.text =
                getString(R.string.general_setting_str_41) + " " + getString(R.string.profile_str_91)
            setUpLoginViewModel()
            if (isUpgradable == 1){
                rlUpgradePlan?.visibility = View.VISIBLE
            }else{
                rlUpgradePlan?.visibility = View.GONE
            }
        }
    }

    private fun getUserProfile() {

            userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        if (ConnectionUtil(requireContext()).isOnline && userViewModel!=null) {
            userViewModel?.getUserProfileData(
                requireContext(), SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                try {
                                    baseMainScope.launch {
                                        //setLog("setProfileData","getUserProfile-1")
                                        async { CommonUtils.saveUserProfileDetails(it.data) }.await()
                                        //setLog("setProfileData","getUserProfile-3")
                                        (activity as BaseActivity).callEventUserProperty(it?.data)

                                        (requireActivity() as MainActivity)?.updateProfile()
                                        if (it?.data.statusCode == 200 && it?.data.result != null && it?.data?.result?.size!! > 0) {
                                            baseIOScope.launch {
                                                //setLog("setProfileData","getUserProfile-setProfileData-called")
                                                setProfileData()
                                            }
                                        }
                                    }

                                }catch (e:Exception){

                                }
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })

            //userViewModel?.getUserCoinDetail(requireContext())
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun configureGoogleClient() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setUpLoginViewModel() {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        setProgressBarVisible(false)
    }


    private fun signInToGoogle() {
        setProgressBarVisible(true)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(callbackManager!=null){
            // Pass the activity result back to the Facebook SDK
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                parseGoogleData(task)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                setLog( "Google sign in failed", e.message.toString())
                setProgressBarVisible(false)
            }catch (e:Exception){
                setProgressBarVisible(false)
            }
        }
    }

    private fun parseGoogleData(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            setLog(
                "Tag",
                "GoogleDATA" + "-->\nId " + account!!.id + "\n-->FirstName " + account.givenName + "\n-->LastName " + account.familyName + "\n-->email " + account.email + "\n-->Photo " + account.photoUrl
            )
            //googleSignInClient!!.signOut()

            SIGNUPMETHOD="Google"
            var id = ""
            var idToken = ""
            var email = ""
            var displayName = ""
            var image = ""
            var name = ""
            var fullName = ""

            if (!TextUtils.isEmpty(account.id)){
                id = account.id.toString()
            }
            if (!TextUtils.isEmpty(account.idToken)){
                idToken = account.idToken.toString()
            }
            if (!TextUtils.isEmpty(account.email)){
                email = account.email.toString()
            }
            if (!TextUtils.isEmpty(account.displayName)){
                displayName = account.displayName.toString()
            }
            if (account.photoUrl != null && !TextUtils.isEmpty(""+account.photoUrl)){
                image = account.photoUrl.toString()
            }
            if (!TextUtils.isEmpty(account.givenName)){
                name = account.givenName.toString()
            }
            if (!TextUtils.isEmpty(account.familyName)){
                fullName = account.familyName.toString()
            }
            callSocialLogin(
                id,
                idToken,
                email,
                displayName,
                image,
                "googleplus",
                name,
                fullName
            )


        } catch (e: ApiException) {
            setLog("TAG", " signInResult:failed code=" + e.statusCode)
        } catch (e:Exception){
            setLog("TAG", " signInResult:Exceptione=" + e.message)
        }

    }

    private fun callSocialLogin(
        uid: String,
        providerUid: String,
        email: String,
        userName: String,
        userImage: String,
        provider: String,
        firstname: String,
        lastname: String
    ) {
        setProgressBarVisible(false)
        if (ConnectionUtil(requireActivity()).isOnline) {

            try {
                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val login_provider_uidJson = JSONObject()
                login_provider_uidJson.put(
                    "value",
                    uid
                )
                clientDataJson.put("login_provider_uid", login_provider_uidJson)

                val silentUserIdJson = JSONObject()
                silentUserIdJson.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID, "")
                )
                clientDataJson.put("silent_user_id", silentUserIdJson)
                val uidJsonObject = JSONObject()
                uidJsonObject.put("value", SharedPrefHelper.getInstance().getUserId())
                clientDataJson.put("uid", uidJsonObject)

                val usernameJson = JSONObject()
                if(!TextUtils.isEmpty(email)){
                    usernameJson.put(
                        "value",
                        email
                    )
                }else{
                    usernameJson.put(
                        "value",
                        uid
                    )
                }

                clientDataJson.put("username", usernameJson)

                val emailJson = JSONObject()
                emailJson.put(
                    "value",
                    email
                )
                clientDataJson.put("email", emailJson)

                val nameJson = JSONObject()
                nameJson.put(
                    "value",
                    firstname + " " + lastname
                )
                clientDataJson.put("name", nameJson)

                val imageJson = JSONObject()
                imageJson.put(
                    "value",
                    userImage
                )
                //clientDataJson.put("image", imageJson)

                val uidJson = JSONObject()
                uidJson.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("uid", uidJson)

                val login_providerJson = JSONObject()
                //facebook //googleplus
                login_providerJson.put(
                    "value",
                    provider
                )
                clientDataJson.put("login_provider", login_providerJson)

                val is_site_uidJson = JSONObject()
                is_site_uidJson.put(
                    "value",
                    false
                )
                clientDataJson.put("is_site_uid", is_site_uidJson)

                mainJson.put("process", "gigya_login")
                mainJson.put("method", "signup_login")
                mainJson.put("client_data", clientDataJson)

                SharedPrefHelper.getInstance().save(PrefConstant.USER_NAME, userName)
                //SharedPrefHelper.getInstance().save(PrefConstant.USER_IMAGE,userImage)
                userViewModel?.socialLogin(
                    requireActivity(),
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {


                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it.data.result.data.newUser?.contains("true",true) == true){
                                        Utils.registerUserMethod_AF(provider)
                                    }else{
                                        val hashMap1 =
                                            java.util.HashMap<String, String>()
                                        hashMap1.put(EventConstant.METHOD_EPROPERTY,provider)
                                        setLog("LOGIN","Success${hashMap1}")
                                            EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                    }
                                    setLog("TAG", "socialLoginRespObserver:: $it?.data")
                                    val userDataMap= java.util.HashMap<String, String>()
                                    userDataMap.put(EventConstant.LOG_IN_SOURCE, "Profile")
                                    userDataMap.put(EventConstant.LOG_IN_METHOD, ""+provider)
                                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(2000)
                                        redirectToProfile()
                                        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
                                        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                                    }
                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                val messageModel = MessageModel(it?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(requireContext(), messageModel)

                                val hashMap = HashMap<String,String>()
                                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it?.message!!)
                                hashMap.put(EventConstant.SOURCE_EPROPERTY,SIGNUPMETHOD)
                                setLog("LOGIN","Success${hashMap}")
                                EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                            }
                        }
                    })

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

    private fun redirectToProfile() {
        getUserProfile()
        setUpViewModel()
        llUser?.setOnClickListener(this)
    }

    private fun signInToFacebook() {
        setProgressBarVisible(true)
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile", "user_friends", "email"))
    }

    /*
     * Facebook JSON fields for user's basic profile.
     */
    companion object FacebookFields {
        internal val ID = "id"
        internal val NAME = "name"
        internal val GENDER = "gender"
        internal val EMAIL = "email"
        internal val FIRST_NAME = "first_name"
        internal val LAST_NAME = "last_name"
        internal val PICTURE = "picture"
//        internal val USER_FRIENDS = "user_friends"

        internal val permissions: String
            get() = ID + "," +
                    NAME + "," +
                    GENDER + "," +
                    EMAIL + "," +
                    FIRST_NAME + "," +
                    LAST_NAME + "," +
                    PICTURE
    }

    fun configureFacebook() {
// Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                setLog("TAG", "facebook:onSuccess:$loginResult")
                //handleFacebookAccessToken(loginResult.accessToken)
                getFacebookProfileData(loginResult)
            }

            override fun onCancel() {
                setLog("TAG", "facebook:onCancel")
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }

            override fun onError(error: FacebookException) {
                setLog("facebook:onError", error.message.toString())
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        })
    }

    /**
     * Get user's basic profile data from facebook.
     */
    private fun getFacebookProfileData(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
            loginResult.accessToken
        ) { `object`, response ->
            parseFacebookData(`object`, AccessToken.getCurrentAccessToken()?.token!!)
            setLog("TAG", "FACEBOOK TOKEN : " + AccessToken.getCurrentAccessToken()?.token!!)
//            try {
//                LoginManager.getInstance().logOut()
//            } catch (ignore: Exception) {
//            }
        }
        val parameters = Bundle()
        val REQUIRED_FIELDS = "fields"
        parameters.putString(REQUIRED_FIELDS, LoginMainActivity.permissions)
        request.parameters = parameters
        request.executeAsync()
    }

    private fun parseFacebookData(graphJson: JSONObject?, token: String) {
        try {
            if (graphJson != null) {
                val ids = graphJson.optString(LoginMainActivity.ID)
                val names = graphJson.optString(LoginMainActivity.NAME)
                val gender = graphJson.optString(LoginMainActivity.GENDER)
                val emails = graphJson.optString(LoginMainActivity.EMAIL)
                val first_name = graphJson.optString(LoginMainActivity.FIRST_NAME)
                val last_name = graphJson.optString(LoginMainActivity.LAST_NAME)
                val image_url = "http://graph.facebook.com/$ids/picture?type=large"
                setLog(
                    "Tag",
                    "FBDATA" + "-->\nId " + ids + "\n-->Name " + names + "\n-->email " + emails + names + "\n-->email " + emails + "\n-->FirstName " + first_name + "\n-->LAstName " + last_name
                )
                SIGNUPMETHOD="Facebook"

                var id = ""
                var idToken = ""
                var email = ""
                var displayName = ""
                var image = ""
                var name = ""
                var fullName = ""

                if (!TextUtils.isEmpty(ids)){
                    id = ids.toString()
                }
                if (!TextUtils.isEmpty(token)){
                    idToken = token.toString()
                }
                if (!TextUtils.isEmpty(emails)){
                    email = emails.toString()
                }
                if (!TextUtils.isEmpty("$first_name$last_name")){
                    displayName = "$first_name $last_name"
                }
                if (image_url != null && !TextUtils.isEmpty(""+image_url)){
                    image = image_url.toString()
                }
                if (!TextUtils.isEmpty(first_name)){
                    name = first_name.toString()
                }
                if (!TextUtils.isEmpty(last_name)){
                    fullName = last_name.toString()
                }
                callSocialLogin(
                    id,
                    idToken,
                    email,
                    displayName,
                    image,
                    "facebook",
                    name,
                    fullName
                )
            }
        }catch (e:Exception){

        }

    }
    private fun callSilentLogin() {
        try {

            userViewModel?.logout_silent_user(requireContext())

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(requireContext(),
                parentMobile!!,
                false,
                getString(R.string.discover_str_2)
            )
        }


    }

    fun callSongDurationSaveData(userId:String){
        CoroutineScope(Dispatchers.IO).launch {
            if(BaseActivity.totalPlayedSongDuration >=0) {

                val sondDbData = AppDatabase.getInstance()?.songDuration()?.getSongDuration()
                sondDbData?.user_streamed_min = BaseActivity.localDuration

                sondDbData?.let { AppDatabase.getInstance()?.songDuration()?.updateSongDuration(it) }
                val inputData = androidx.work.Data.Builder()
                    .putString("duration", sondDbData?.user_streamed_min.toString()).
                    putString("date", sondDbData?.first_stream_start_time?.toString())
                    .putString("uid", userId).build()
                val uploadDataConstraints = Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build()
                val uploadWorkRequest = OneTimeWorkRequestBuilder<WorkManagerClass>()
                    .setConstraints(uploadDataConstraints)
                    .setInputData(inputData).build()
                setLog("SongDurationData", " logout "+ Gson().toJson(inputData))

                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
                BaseActivity.totalPlayedSongDuration = -2
            }
        }
    }

    override fun LogoutUser() {
        SharedPrefHelper.getInstance().getUserId()?.let { (activity as BaseActivity).setSongDuration(it) }
        BaseActivity.totalPlayedSongDuration = 0
        val isUserLoggedIn = SharedPrefHelper.getInstance().isUserLoggedIn()
        setLog(TAG, "LogoutUser: isUserLoggedIn"+isUserLoggedIn)
        AppsFlyerLib.getInstance().waitForCustomerUserId(true)
        isUpgradable = 1
        subscription_type = -1
            if (isUserLoggedIn) {
                val gs = GoogleSignIn.getLastSignedInAccount(requireActivity())
                if (gs != null){
                    try {
                        googleSignInClient.signOut()
                    }catch(e:Exception){

                    }
                }
                SharedPrefHelper.getInstance().logOut()
                setUserSubscription(subscription_type, isUpgradable)
                (requireActivity() as MainActivity)?.updateProfile()
                (requireActivity() as MainActivity)?.updateViewBasedOnSubscription()
            } else {
                SharedPrefHelper.getInstance().logOut()
                val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
        (requireActivity() as MainActivity)?.getUserSubscriptionStatus()
        (requireActivity() as MainActivity)?.loadBottomAds()
        callSilentLogin()
        try {
            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.LOGGED_IN_STATUS,"logged-out")
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }catch (e:Exception){

        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        initApiCall()
    }

    private fun redirectToDeeplinkPage(){
        setLog("deepLinkUrl", "profileFragment-redirectToDeeplinkPage--deeplinkPageName=$deeplinkPageName && deeplinkPageName2=$deeplinkPageName2")
        if (!TextUtils.isEmpty(deeplinkPageName)){
            val isUserLoggedIn = SharedPrefHelper.getInstance().isUserLoggedIn()
            val bundle = Bundle()
           if (deeplinkPageName.equals("edit") && isUserLoggedIn){

               bundle.putBoolean(isProfileEditPage, true)
               val settingsFragment = SettingsFragment()
               settingsFragment.arguments = bundle
               addFragment(R.id.fl_container, this, settingsFragment, false)
           }else if (deeplinkPageName.equals("earn-coin") && isUserLoggedIn){
               addFragment(R.id.fl_container,this, EarnCoinDetailFragment(),false)
           }else if (deeplinkPageName.equals("redeem-coins") && isUserLoggedIn){
               addFragment(R.id.fl_container,this, EarnCoinAllTabFragement(),false)
           }else if (deeplinkPageName.equals("general") && !TextUtils.isEmpty(deeplinkPageName2)){
               if (deeplinkPageName2.equals("voice-assistant")){
                   addFragment(R.id.fl_container, this, UserProfileVoiceAssistantSettingsFragment(), false)
               }else if (deeplinkPageName2.equals("share-app")){

               } else if (deeplinkPageName2.equals("help&support")){
                   openCommonWebView(helpAndSupport, requireActivity())
               }else if (deeplinkPageName2.equals("terms&conditions")){
                   openCommonWebView(termAndConditionLink, requireActivity())
               }else if (deeplinkPageName2.equals("privacy-policy")){
                   openCommonWebView(privacyPolicy, requireActivity())
               }else if (deeplinkPageName2.equals("activate-tv")){

               }else if (deeplinkPageName2.equals("about-hungama")){
                   openCommonWebView(aboutHungama, requireActivity())
               }else if (deeplinkPageName2.equals("delete-account")){

               }else if (deeplinkPageName2.equals("logout") && isUserLoggedIn){
                   setLog(TAG, "redirectToDeeplinkPage: isUserLoggedIn "+isUserLoggedIn)
                   LogoutUser()
               }
           }else if (deeplinkPageName.equals("general-setting") && !TextUtils.isEmpty(deeplinkPageName2)){
               bundle.putString(EXTRA_PAGE_DETAIL_NAME, deeplinkPageName2)
               val generalSetting = GeneralSetting()
               generalSetting.arguments = bundle
               addFragment(R.id.fl_container, this, generalSetting, false)
           }else if (isUserLoggedIn && deeplinkPageName.length > 15){
               bundle.putBoolean(isProfileEditPage, false)
               val settingsFragment = SettingsFragment()
               settingsFragment.arguments = bundle
               addFragment(R.id.fl_container, this, settingsFragment, false)
           }
        }
    }

    var errorCount = 0
    val linkGeneratorListener = object : CreateOneLinkHttpTask.ResponseListener{
        override fun onResponse(string: String?) {
            if (isAdded && context != null){
                setLog("inviteLink", "onResponse "+string.toString())
                CommonUtils.shareLink(requireContext(), string.toString())
            }
        }

        override fun onResponseError(string: String?) {
            if (isAdded && context != null){
                setLog("inviteLink", "onResponseError "+string.toString())
                if (errorCount < 2){
                    generateInviteLink()
                    errorCount++
                }
            }
        }
    }

    private fun generateInviteLink(){
        CommonUtils.generateAppsFlyerInviteLink(requireActivity(), linkGeneratorListener,
            "mobile_share", "50", "coin", "www.hungama.com")
    }

    private fun setProfileData(){
        baseMainScope.launch {
            val handleName = SharedPrefHelper.getInstance().getHandleName()
            val firstName=SharedPrefHelper.getInstance().getUserFirstname()
            val lastName=SharedPrefHelper.getInstance().getUserLastname()
            val profileImage = SharedPrefHelper.getInstance().getProfileImage()

            setLog("setProfileData", "handleName-$handleName -- firstName-$firstName -- lastName-$lastName")
            if (!TextUtils.isEmpty(handleName)) {
                tvActionBarHeading?.text = handleName
            } else {
                tvActionBarHeading?.text = ""
            }

            if (profileImage != null && !TextUtils.isEmpty(profileImage.toString())
            ) {
                ImageLoader.loadImage(
                    requireContext(),
                    ivUser,
                    profileImage,
                    R.drawable.ic_no_user_img
                )
            }
            var username = ""
            if (!TextUtils.isEmpty(firstName)) {
                username += firstName + " "
            }
            if (!TextUtils.isEmpty(lastName)) {
                username += lastName
            }

            if (!TextUtils.isEmpty(username)) {
                tvUserName.text = getString(R.string.profile_str_1) + ", " + username
            } else {
                if (!TextUtils.isEmpty(handleName)) {
                    tvUserName?.text =
                        getString(R.string.profile_str_1) + ", " + handleName
                } else {
                    tvUserName?.text = getString(R.string.profile_str_1) + "!"
                }
            }

            displayUserCoins()
        }
    }

    fun signInToApple(){
        setLog("UserLogin", "LoginMainActivity-signInToApple()")
        // Localize the Apple authentication screen in French.
        provider.addCustomParameter("locale", "en")
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                setLog("UserLogin", "LoginMainActivity-checkPending:onSuccess:$authResult")
                // Get the user profile with authResult.getUser() and
                // authResult.getAdditionalUserInfo(), and the ID
                // token from Apple with authResult.getCredential().
                val user = authResult.user
            }.addOnFailureListener { e ->
                setLog("UserLogin", "LoginMainActivity-checkPending:onFailure-${e.message.toString()}")
            }
        } else {
            auth.startActivityForSignInWithProvider(requireActivity(), provider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!
                    setLog("UserLogin", "LoginMainActivity-activitySignIn:onSuccess:${authResult.user}")
                    val user = authResult.user
                    loginSuccess(user, "apple", "")
                    // ...
                }
                .addOnFailureListener { e ->
                    setLog("UserLogin", "LoginMainActivity-activitySignIn:onFailure-${e.message.toString()}")
                    val messageModel = MessageModel("Authentication failed.", MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
        }
    }

    fun loginSuccess(user: FirebaseUser?, provider: String, providerToken: String) {
        if (user != null) {
            setLog("loginSuccess", "provider-$provider - user.uid-${user.uid} - providerToken-${providerToken} - user.email-${user.email} - user.displayName-${user.displayName} - user.photoUrl-${user.photoUrl}")
            callSocialLogin(
                user.uid.toString(),
                providerToken.toString(),
                user.email.toString(),
                user.displayName.toString(),
                user.photoUrl.toString(),
                provider.toString(),
                user.displayName.toString(),
                user.displayName.toString()
            )
        }

    }
}