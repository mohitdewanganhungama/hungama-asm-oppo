
package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amplitude.api.Amplitude
import com.appsflyer.AFInAppEventParameterName
import com.google.gson.Gson
import com.hungama.music.BuildConfig
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.LoginMainActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.activity.PaymentWebViewActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.getAllUpiEnabledAppList
import com.hungama.music.utils.CommonUtils.getDeviceId
import com.hungama.music.utils.CommonUtils.isUserHasGoldSubscription
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.PAY_API_REQUEST_CODE
import com.hungama.music.utils.Constant.PAY_API_RESPONSE_FAILED_CODE
import com.hungama.music.utils.Constant.PAY_API_RESPONSE_SUCCESS_CODE
import com.hungama.music.utils.Constant.PLATFORM_ID
import com.hungama.music.utils.Constant.PRODUCT_ID
import com.hungama.music.utils.Constant.SIGNIN_WITH_ALL
import com.hungama.music.utils.Utils.Companion.convertArrayToString
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.subscription_dialog.*
import kotlinx.android.synthetic.main.subscription_dialog.llBuyPlan
import kotlinx.android.synthetic.main.subscription_dialog.tvUpgradePlan
import kotlinx.android.synthetic.main.subscription_new_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SubscriptionDialogBottomsheetFragmentFreeMinute(
    val ctx: Context) : SuperBottomSheetFragment() {
    var action = false
    var url = ""
    var url2 = ""
    var triggerPoint =""
    var isBuyGoldClicked = true
    var height = 0
    var price = -1.0
    var isRentPopup = false
    var isButtonClicked = false
    var ft: SongDurationConfigModel.Ft? = null
    var nonft: SongDurationConfigModel.Nonft? = null
    var button_text2 = ""
    var button_text = ""
    var planId = ""
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.subscription_new_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        applyButtonTheme(requireContext(), llRent)
//        setAppButton2(requireContext(), llBuyPlan)
//        country = CommonUtils.getUserCountry(requireContext()).toString()
//        if (isProbablyRunningOnEmulator()){
//            country = "in"
//        }
        //closeDialog()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        ButtonText()

        setUrlParameters2()

        llBuyPlan.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llBuyPlan!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            isButtonClicked = true
            if (BaseActivity.player11 != null){
                BaseActivity.player11?.pause()
            }
            triggerPoint="payment"
            if (!SharedPrefHelper.getInstance().isUserLoggedIn()){
                action = true
                isBuyGoldClicked = true
                val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                intent.putExtra("action", SIGNIN_WITH_ALL)
                intent.putExtra("isForAudio", true)
                startActivity(intent)
                updateSubscriptionCallback(Constant.LOGIN_PROCESS)
            }else{

                if (CommonUtils.getDrawerPlanId().isNotEmpty()){
                    planId = "plan_id="+CommonUtils.getDrawerPlanId()
                }

                callPayApi(planId)
                updateSubscriptionCallback(Constant.PAYMENT_PROCESS)
            }

            eventClckFunction(CommonUtils.getDrawerPlanId())
        }

        btnSeeAllPlan.setOnClickListener{
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llBuyPlan!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (BaseActivity.player11 != null){
                BaseActivity.player11?.pause()
            }
            isButtonClicked  = true
            if (!SharedPrefHelper.getInstance().isUserLoggedIn()){
                action = true
                isBuyGoldClicked = false
                val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                intent.putExtra("action", SIGNIN_WITH_ALL)
                intent.putExtra("isForAudio", true)
                startActivity(intent)
                updateSubscriptionCallback(Constant.LOGIN_PROCESS)
            }else{
                callPayApi("")
                updateSubscriptionCallback(Constant.PAYMENT_PROCESS)
            }
            eventClckFunction("")
        }

    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED
    override fun isSheetAlwaysExpanded(): Boolean = true

    //calculate height based on three_dot_menu_item_layout and close button
    override fun getExpandedHeight(): Int {

        return requireContext().resources.getDimension(R.dimen.dimen_590).toInt()
    }
    override fun getBackgroundColor(): Int = ContextCompat.getColor(requireContext(), R.color.transparent)

    override fun onResume() {
        super.onResume()

        val isUserLoggedIn = SharedPrefHelper.getInstance().isUserLoggedIn()
        if (isUserLoggedIn && action && !isUserHasGoldSubscription()){
            action = false
            isButtonClicked = true
            setUrlParameters2()
            var planId = "plan_id=3"
            if (!CommonUtils.getDrawerPlanId().isNullOrEmpty()){
                planId = "plan_id=" + CommonUtils.getDrawerPlanId()
            }
            if (isBuyGoldClicked)
            callPayApi(planId)
            else
                callPayApi("")

            (activity as BaseActivity).pausePlayer()

                if(BaseActivity.player11?.isPlaying == true)
                {
                    BaseActivity.player11?.pause()
                }

            updateSubscriptionCallback(Constant.PAYMENT_PROCESS)
        }else if (action){
            if (isUserLoggedIn && isUserHasGoldSubscription()){
                updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_NA)
            }
            closeDialog()
        }
        else{
            if (!isUserHasGoldSubscription() && isButtonClicked){
                (activity as BaseActivity).playNextSong(true)
                BaseActivity.setTouchData()
                isButtonClicked = false
                dismiss()
            }
        }
    }

    private fun setUrlParameters2() {

        var plan_type = ""

        if (!triggerPoint.isEmpty()) {

            url2 = "https://payments.hungama.com/payment?"
            plan_type = "subscription"

        } else {
            url2 = "https://payments.hungama.com/plan?"
            plan_type = "subscription"
        }

        val auth = CommonUtils.md5(
            Constant.PRODUCT_KEY.toString() + ":" + SharedPrefHelper.getInstance().getUserId()
        )
        url2 += "auth=$auth"

        if (!triggerPoint.isEmpty()) {
            if (CommonUtils.getDrawerPlanId().isNotEmpty()){
                planId = CommonUtils.getDrawerPlanId()
            }
            url2 += "&plan_id=$planId"
        }

        val identity = SharedPrefHelper.getInstance().getUserId()
        url2 += "&identity=$identity"

        val product_id = PRODUCT_ID
        url2 += "&product_id=$product_id"

        url2 += "&country=${Constant.DEFAULT_COUNTRY_CODE}"

        val platform_id = PLATFORM_ID
        url2 += "&platform_id=$platform_id"

        val upgradable = Constant.SUBSCRIPTION_UPGRADABLE
        url2 += "&upgradable=$upgradable"

        url2 += "&plan_type=$plan_type"

        val app_version = BuildConfig.VERSION_NAME
        url2 += "&app_version=$app_version"

        val build_number = BuildConfig.VERSION_CODE
        url2 += "&build_number=$build_number"


        val upiList = getAllUpiEnabledAppList(requireContext())

        setLog("upiList", "upiList-$upiList")
        url2 += "&upilist=${convertArrayToString(upiList)}"

        val hardware_id = getDeviceId(requireContext())
        url2 += "&hardware_id=$hardware_id"

        val content_id = ""
        url2 += "&content_id=$content_id"


        val model = HungamaMusicApp.getInstance().getEventData("")
        var source = ""
        if (!TextUtils.isEmpty(model.sourceName)) {
            source = model.sourceName
        }
        if (!TextUtils.isEmpty(model.songName)) {
            if (!TextUtils.isEmpty(source)) {
                source += "_" + model.songName
            } else {
                source = model.songName
            }
        }

        if (!TextUtils.isEmpty(source)) {
            source = source.replace(" ", "_")
        }

        url2 += "&source=$source"


        if (!url2.contains("live_event_id", true)) {
            val live_event_id = ""
            url2 += "&live_event_id=$live_event_id"
        }

        if (!url2.contains("aff_code", true)) {
            val aff_code = ""
            url2 += "&aff_code=$aff_code"
        }

        if (!url2.contains("extra_data", true)) {
            val extra_data = ""
            url2 += "&extra_data=$extra_data"
        }

        if (!url2.contains("utm_source", true)) {
            val utm_source = source
            url2 += "&utm_source=$utm_source"
        }

        if (!url2.contains("utm_medium", true)) {
            val utm_medium = ""
            url2 += "&utm_medium=$utm_medium"
        }

        if (!url2.contains("utm_campaign", true)) {
            val utm_campaign = ""
            url2 += "&utm_campaign=$utm_campaign"
        }


        val lang = SharedPrefHelper.getInstance().getLanguage()
        url2 += "&lang=$lang"

        val amplitude_user_id = Amplitude.getInstance().userId
        url2 += "&amp_user_id=$amplitude_user_id"

        val amplitude_device_id = Amplitude.getInstance().deviceId
        url2 += "&amp_device_id=$amplitude_device_id"

        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = HashMap<String, String>()
            if (!TextUtils.isEmpty(model.songName)) {
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + model.sourceName)
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + model.songName)
            } else {
                hashMap.put(EventConstant.SOURCE_EPROPERTY, Utils.getContentTypeName(content_id))
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + "")
            }

            if (isRentPopup) {
                hashMap.put(EventConstant.PLANTYPE_ENAME, "" + plan_type)
                hashMap.put(EventConstant.ACTION_EPROPERTY, "continue")
                EventManager.getInstance().sendEvent(RentOpenRentPageEvent(hashMap))
            } else {
                if (!TextUtils.isEmpty(source)) {
                    hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + source)
                } else {
                    if (hashMap.containsKey(EventConstant.SOURCE_EPROPERTY) && TextUtils.isEmpty(
                            hashMap.get(EventConstant.SOURCE_EPROPERTY)
                        )
                    ) {
                        if (TextUtils.isEmpty(model.sourceName)) {
                            hashMap.put(
                                EventConstant.SOURCE_EPROPERTY,
                                "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                            )
                        }
                    }
                }

                if (hashMap.containsKey(EventConstant.SOURCE_NAME_EPROPERTY)) {
                    hashMap.remove(EventConstant.SOURCE_NAME_EPROPERTY)
                }
                hashMap.put(EventConstant.ACTION_EPROPERTY, "Buy Gold")
                EventManager.getInstance().sendEvent(SubscriptionPopupPageEvent(hashMap))
            }

            setLog("SubscriptionQueryParam", "url2:${url}")
        }

    }

    fun callPayApi(url:String) {
        if (ConnectionUtil(activity).isOnline) {
            val intent = Intent(requireContext(), PaymentWebViewActivity::class.java)
            val genrtedURL = CommonUtils.genratePaymentPageURL(requireContext(), url)

            intent.putExtra("url", genrtedURL)
            intent.putExtra("planName", CommonUtils.getDrawerPlanId())

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
            CommonUtils.showToast(requireContext(), messageModel,"SubscriptionDialogBottomsheetFragmentFreeMinute","callPayApi")
        }
    }


    fun callPayApiold(url:String) {
        if (ConnectionUtil(activity).isOnline) {
            val intent = Intent(requireContext(), PaymentWebViewActivity::class.java)
            val genrtedURL = CommonUtils.genratePaymentPageURL(requireContext(), url)

            intent.putExtra("url", genrtedURL)
            intent.putExtra("planName", CommonUtils.getNudgePlanId())

            if (BaseActivity.player11 != null){
                BaseActivity.player11?.pause()
            }

            startActivityForResult(intent, PAY_API_REQUEST_CODE)

        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35),
                getString(R.string.toast_message_5),
                MessageType.NEGATIVE,
                true
            )
            CommonUtils.showToast(requireContext(), messageModel,"SubscriptionDialogBottomsheetFragmentFreeMinute","callPayApiold")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == PAY_API_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == PAY_API_RESPONSE_SUCCESS_CODE) {
                getUserSubscriptionStatus()
            }else if (resultCode == PAY_API_RESPONSE_FAILED_CODE) {
                updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_FAIL)
                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String,String>()
                    hashMap[EventConstant.NETWORKTYPE_EPROPERTY] = "" + ConnectionUtil(context).networkType
                    if (isRentPopup){
                        EventManager.getInstance().sendEvent(RentFailedPageEvent(hashMap))
                    }else{
                        EventManager.getInstance().sendEvent(SubscriptionFailedPageEvent(hashMap))
                    }
                }

                closeDialog()
            } else {
                callUpdatePaymentApi()
                updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_NA)
                closeDialog()
            }
        }
    }

    fun getUserSubscriptionStatus() {
        if(isAdded){
            val userSubscriptionViewModel = ViewModelProvider(this)[UserSubscriptionViewModel::class.java]

            if (ConnectionUtil(requireContext()).isOnline) {
                var requestTime: Date? = DateUtils.getCurrentDateTime()
                userSubscriptionViewModel?.getUserSubscriptionStatusDetail(requireContext())?.
                observe(this, Observer {
                        when(it.status){
                            Status.SUCCESS->{

                                if (it?.data != null && it?.data?.success!!) {

                                    setLog("TAG", "getUserSubscriptionStatusDetail response:${it}")

                                    updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_SUCCESS)
                                    closeDialog()

                                    val responseTime = DateUtils.getCurrentDateTime()
                                    val diffInMillies: Long = Math.abs(requestTime?.getTime()!! - responseTime?.getTime()!!)

                                    val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)

                                    CoroutineScope(Dispatchers.IO).launch {
                                                if(it?.data?.data?.subscription!=null){
                                                    val eventValue: MutableMap<String, Any> = HashMap()
                                                    eventValue.put(AFInAppEventParameterName.REVENUE,it?.data?.data?.subscription?.planPrice!!)
                                                    eventValue.put(AFInAppEventParameterName.CURRENCY,it?.data?.data?.subscription?.currency!!)
                                                    eventValue.put(EventConstant.AF_START_DATE_PROPERTY,it?.data?.data?.subscription?.subscriptionStartDate!!)
                                                    eventValue.put(EventConstant.AF_EXPIRATION_DATE_PROPERTY,it?.data?.data?.subscription?.subscriptionEndDate!!)
                                                    eventValue.put(EventConstant.AF_SUBSCRIPTION_PERIOD_PROPERTY,it?.data?.data?.subscription?.planValidityName!!)
                                                    eventValue.put(EventConstant.AF_SUBSCRIPTION_METHOD_PROPERTY,it?.data?.data?.subscription?.paymentSource!!)
//                                                    AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_SUBSCRIBE_ENAME, eventValue)

                                                    val hashMap = HashMap<String,String>()

                                                    hashMap.put(EventConstant.PAYMENT_MODE,it?.data?.data?.subscription?.paymentSource!!)
                                                    hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY,"" + ConnectionUtil(context).networkType)
                                                    hashMap.put(EventConstant.PLAN_SELECTED,it?.data?.data?.subscription?.planName.toString())
                                                    hashMap.put(EventConstant.PROMO,it?.data?.data?.subscription?.couponCode.toString())

                                                    hashMap.put(EventConstant.RESPONSECODE_EPROPERTY,"200")
                                                    hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, diff.toString())

                                        }
                                    }

                                } else {
                                    updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_NA)
                                    closeDialog()
                                    Utils.showSnakbar(requireContext(),
                                        requireView(),
                                        false,
                                        it?.message.toString()
                                    )
                                }
                            }

                            Status.LOADING ->{

                            }

                            Status.ERROR ->{
                                updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_NA)
                                closeDialog()
                            }
                        }
                    })
            } else {
                updateSubscriptionCallback(Constant.CONTENT_ORDER_STATUS_NA)
                val messageModel = MessageModel(getString(R.string.discover_str_3), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel,"SubscriptionDialogBottomsheetFragment","getUserSubscriptionStatus")
            }
        }

    }





    private fun updateSubscriptionCallback(status:Int){
    }

    private fun callPlanDetailApi(){
        val userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)


        if (ConnectionUtil(requireContext()).isOnline) {
            userSubscriptionViewModel?.getPlanDetail(
                requireContext()
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            if (it?.data != null){
                                val detailModel = Gson().fromJson<ContentsPlanDetailModel>(
                                    it?.data,
                                    ContentsPlanDetailModel::class.java
                                ) as ContentsPlanDetailModel
                                if (detailModel?.success!!){
                                    setContentPlanDetails(detailModel, it?.data)
                                }
                            }
                        }

                        Status.LOADING ->{

                        }

                        Status.ERROR ->{

                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.discover_str_3), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel,"SubscriptionDialogBottomsheetFragment","callPlanDetailApi")
        }
    }


    private fun setContentPlanDetails(
        contentsPlanDetailModel: ContentsPlanDetailModel,
        data: String
    ) {
        if (contentsPlanDetailModel?.data != null){
            val json = JSONObject(data)
            val jsonData = json.getJSONObject("data")
            var plan = JSONArray()
            if (!plan.isNull(0) && plan?.get(0) != null && !TextUtils.isEmpty(plan.get(0).toString())){
                try {
                    val planModel = Gson().fromJson<ContentsPlanDetailModel.Data.Plan>(
                        plan.get(0).toString(),
                        ContentsPlanDetailModel.Data.Plan::class.java
                    ) as ContentsPlanDetailModel.Data.Plan

                    if (planModel != null && planModel.planPrice.toDouble() >= 0){
                        price = planModel.planPrice.toDouble()
                        val df = DecimalFormat("###.##")
                        val price2 = df.format(price)
                        if(Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
                            tvPrice?.text = getString(R.string.movie_str_15) + " " + price2
                        }else{
                            tvPrice?.text = getString(R.string.movie_str_16) + " " + price2
                        }
                    }
                }catch (e:Exception){

                }

            }
        }

        if (price >= 0) {
            llRent?.visibility = View.VISIBLE
        } else {
            llRent?.visibility = View.GONE
        }

    }

    private fun callUpdatePaymentApi(){
        if(isAdded){
            val userSubscriptionViewModel = ViewModelProvider(
                this
            ).get(UserSubscriptionViewModel::class.java)


            if (ConnectionUtil(requireContext()).isOnline) {
                userSubscriptionViewModel.getUpdatePaymentStatus(requireContext())
            } else {
                val messageModel = MessageModel(getString(R.string.discover_str_3), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel,"SubscriptionDialogBottomsheetFragmentFreeMinute","callPayApi")
            }
        }
    }

    fun ButtonText(){

        playableContentViewModel = ViewModelProvider(this).get(PlayableContentViewModel::class.java)
        ft = CommonUtils.getSongDurationConfig().drawer_minute_quota_exhausted.ft
        nonft = CommonUtils.getSongDurationConfig().drawer_minute_quota_exhausted.nonft
        if (ft != null || nonft != null) {

            var planId = "plan_id=3"
            if (CommonUtils.getDrawerPlanId().isNotEmpty()){
                planId = CommonUtils.getDrawerPlanId()
            }

            playableContentViewModel.getPlanInfo(requireContext(), planId)

            playableContentViewModel.planInfoData.observe(this, Observer {


                btnSeeAllPlan.text = button_text2

                 CommonUtils.setButtonFromFirebase(ctx,ft,nonft,tvUpgradePlan,btnSeeAllPlan,it)
                button_text = tvUpgradePlan.text.toString()
                eventviewFunction()
            })

        }
        checkValidationJsonData(CommonUtils.getSongDurationConfig().drawer_minute_quota_exhausted)
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

    fun eventviewFunction() {

        val hashMapPageView = HashMap<String, String>()
        hashMapPageView[EventConstant.TYPE_EPROPERTY] = "drawer_minute_quota_exhausted"
        hashMapPageView[EventConstant.SCREEN_NAME_EPROPERTY] = Constant.screen_name

        if (CommonUtils.isUserHasEliableFreeContent()) {
            hashMapPageView[EventConstant.freeTrialEligibility] = "ft"
        } else hashMapPageView[EventConstant.freeTrialEligibility] = "nonft"

            hashMapPageView[EventConstant.plan_id] = CommonUtils.getDrawerPlanId()
            hashMapPageView[EventConstant.button_text_1] = button_text
            hashMapPageView[EventConstant.button_text_2] = button_text2
            EventManager.getInstance().sendEvent(NudgeDrawerView(hashMapPageView))
    }

    fun eventClckFunction(planId:String) {
        setLog("khbdksahg", "isClicked")
        val hashMapPageView = HashMap<String, String>()
        hashMapPageView[EventConstant.TYPE_EPROPERTY] = "drawer_minute_quota_exhausted"

        hashMapPageView[EventConstant.plan_id] = planId

            if (planId.isNotEmpty())
                hashMapPageView[EventConstant.button_text] = tvUpgradePlan.text.toString()
            else hashMapPageView[EventConstant.button_text] = button_text2

            EventManager.getInstance().sendEvent(NudgeDrawerClickedEvent(hashMapPageView))

    }
}

