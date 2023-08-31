
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
import com.hungama.music.HungamaMusicApp
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
import com.hungama.music.BuildConfig
import com.hungama.music.R
import kotlinx.android.synthetic.main.subscription_dialog.*
import kotlinx.android.synthetic.main.subscription_dialog.llBuyPlan
import kotlinx.android.synthetic.main.subscription_dialog.tvBody
import kotlinx.android.synthetic.main.subscription_dialog.tvSubTitle
import kotlinx.android.synthetic.main.subscription_dialog.tvTitle
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
    val ctx: Context,
    val planName: String,
    val contentId: String,
    val onUserSubscriptionUpdateCall: OnUserSubscriptionUpdate?,
    val contentTitle:String,
    val downloadPlayCheck:DownloadPlayCheckModel?,
    val onUserSubscriptionDialogIsFinish:OnUserSubscriptionDialogIsFinish? = null
) : SuperBottomSheetFragment() {
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
        if (planName.contains(PlanNames.TVOD.name, true) || planName.contains(PlanNames.PTVOD.name, true)|| planName.contains(PlanNames.LE.name, true)){
            isRentPopup = true
            clSubscriptionPlan?.visibility = View.GONE
            clRentPlan?.visibility = View.VISIBLE
            height = requireContext().resources.getDimension(R.dimen.dimen_250).toInt()
            tvHeading?.text = getString(R.string.discover_str_16) + " " + contentTitle
//            callPlanDetailApi()

            if(downloadPlayCheck != null &&downloadPlayCheck?.isDirectPaymentAction!!){
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    setLog("DirectPayment","llRent performClick isDirectPaymentAction:${downloadPlayCheck?.isDirectPaymentAction}")
                    llRent?.performClick()
                },100)

            }
        }else{
            isRentPopup = false
            height = requireContext().resources.getDimension(R.dimen.dimen_430).toInt()
            clRentPlan?.visibility = View.GONE
            clSubscriptionPlan?.visibility = View.VISIBLE
            if (downloadPlayCheck != null && downloadPlayCheck.restrictedDownload == RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT && downloadPlayCheck.isAudio){
                tvTitle.text = getString(R.string.popup_str_65)
                tvSubTitle.hide()
                tvBody.text = getString(R.string.popup_str_66)
                tvUpgradePlan.text = getString(R.string.drawer_download_all_CTA)
            }else if (downloadPlayCheck != null && downloadPlayCheck.restrictedDownload == RestrictedDownload.ALLOW_DOWNLOAD_CONTENT && downloadPlayCheck.isAudio){
                tvTitle.text = getString(R.string.popup_str_65)
                tvSubTitle.show()
                tvSubTitle.text = getString(R.string.popup_str_67)
                tvBody.text = getString(R.string.popup_str_70)
                tvUpgradePlan.text = getString(R.string.drawer_download_all_CTA)
            }else if (downloadPlayCheck != null && downloadPlayCheck.restrictedDownload == RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT && !downloadPlayCheck.isAudio){
                tvTitle.text = getString(R.string.popup_str_68)
                tvSubTitle.hide()
                tvBody.text = getString(R.string.popup_str_69)
                tvUpgradePlan.text = getString(R.string.drawer_download_all_CTA)
            }else{
//                tvSubTitle.show()
            }

            if(downloadPlayCheck != null &&downloadPlayCheck?.isDirectPaymentAction!!){
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    setLog("DirectPayment","llBuyPlan performClick isDirectPaymentAction:${downloadPlayCheck?.isDirectPaymentAction}")
                    llBuyPlan?.performClick()
                },100)

            }
        }

        setUrlParameters()
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
                startActivity(intent)
                updateSubscriptionCallback(Constant.LOGIN_PROCESS)
            }else{

                if (CommonUtils.getDrawerPlanId().isNotEmpty()){
                    planId = "plan_id="+CommonUtils.getDrawerPlanId()
                }

                callPayApi(planId)
                updateSubscriptionCallback(Constant.PAYMENT_PROCESS)
            }
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
                startActivity(intent)
                updateSubscriptionCallback(Constant.LOGIN_PROCESS)
            }else{
                callPayApi("")
                updateSubscriptionCallback(Constant.PAYMENT_PROCESS)
            }
        }

    }


    override fun onDismiss(dialog: DialogInterface) {
        if (onUserSubscriptionDialogIsFinish != null)
        {
            onUserSubscriptionDialogIsFinish.isFinish(true)
        }
        super.onDismiss(dialog)
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
            setUrlParameters()
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

    private fun setUrlParameters() {
        var plan_type = ""
        if (planName.contains(PlanNames.TVOD.name, true) || planName.contains(PlanNames.PTVOD.name, true)){
            url = "https://payapihmopftd.hungama.com/payment?"
            plan_type = planName
        }else if (planName.contains("LE")){
            url = "https://payapihmopftd.hungama.com/payment?"
//            plan_type = "live_concert"
            plan_type = planName
        }else{
            url = "https://payapihmopftd.hungama.com/plan?"
            plan_type = "subscription"
        }
        val auth = CommonUtils.md5(Constant.PRODUCT_KEY.toString()+":"+ SharedPrefHelper.getInstance().getUserId())
        url += "auth=$auth"

        val identity = SharedPrefHelper.getInstance().getUserId()
        url += "&identity=$identity"

        val product_id = PRODUCT_ID
        url += "&product_id=$product_id"

        url += "&country=${Constant.DEFAULT_COUNTRY_CODE}"

        val platform_id = PLATFORM_ID
        url += "&platform_id=$platform_id"

        val upgradable = Constant.SUBSCRIPTION_UPGRADABLE
        url += "&upgradable=$upgradable"

        url += "&plan_type=$plan_type"

        val app_version = BuildConfig.VERSION_NAME
        url += "&app_version=$app_version"

        val build_number = BuildConfig.VERSION_CODE
        url += "&build_number=$build_number"

        val upiList = getAllUpiEnabledAppList(requireContext())

        setLog("upiList", "upiList-$upiList")
        url += "&upilist=${convertArrayToString(upiList)}"

        val hardware_id = getDeviceId(requireContext())
        url += "&hardware_id=$hardware_id"

        val content_id = contentId
        url += "&content_id=$content_id"

        val model=HungamaMusicApp.getInstance().getEventData(contentId)
        var source = ""
        if (!TextUtils.isEmpty(model.sourceName)){
            source = model.sourceName
        }
        if (!TextUtils.isEmpty(model?.songName)){
            if (!TextUtils.isEmpty(source)){
                source += "_"+model?.songName
            }else{
                source = model.songName
            }
        }

        if (!TextUtils.isEmpty(source)){
            source=source.replace(" ","_")
        }

        url += "&source=$source"

        setLog(TAG, "pay url setUrlParameters: source:${source}")




        if(downloadPlayCheck != null &&downloadPlayCheck?.isDirectPaymentAction!! && !TextUtils.isEmpty(downloadPlayCheck?.queryParam!!)){
            CommonUtils.setLog("downloadPlayCheck?.queryParam","url:${url} downloadPlayCheck?.queryParam:${downloadPlayCheck?.queryParam}")
            url=url+"&"+downloadPlayCheck?.queryParam
        }

        if(!url.contains("live_event_id",true)){
            val live_event_id = ""
            url += "&live_event_id=$live_event_id"
        }

        if(!url.contains("aff_code",true)){
            val aff_code = ""
            url += "&aff_code=$aff_code"
        }

        if(!url.contains("extra_data",true)){
            val extra_data = ""
            url += "&extra_data=$extra_data"
        }

        if(!url.contains("utm_source",true)){
            val utm_source = source
            url += "&utm_source=$utm_source"
        }

        if(!url.contains("utm_medium",true)){
            val utm_medium = ""
            url += "&utm_medium=$utm_medium"
        }

        if(!url.contains("utm_campaign",true)){
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



        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = HashMap<String,String>()
            if(!TextUtils.isEmpty(model?.songName)){
                hashMap.put(EventConstant.SOURCE_EPROPERTY,""+model?.sourceName)
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,""+model?.songName)
            }else{
                hashMap.put(EventConstant.SOURCE_EPROPERTY,Utils.getContentTypeName(content_id))
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,""+contentId)
            }

            if (isRentPopup){
                hashMap.put(EventConstant.PLANTYPE_ENAME,""+plan_type)
                hashMap.put(EventConstant.ACTION_EPROPERTY,"continue")
                EventManager.getInstance().sendEvent(RentOpenRentPageEvent(hashMap))
            }else{
                if (!TextUtils.isEmpty(source)){
                    hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+ source)
                }else{
                    if (hashMap.containsKey(EventConstant.SOURCE_EPROPERTY)
                        && TextUtils.isEmpty(hashMap.get(EventConstant.SOURCE_EPROPERTY))){
                        if(TextUtils.isEmpty(model?.sourceName)){
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
                        }
                    }
                }

                if (hashMap.containsKey(EventConstant.SOURCE_NAME_EPROPERTY)){
                    hashMap.remove(EventConstant.SOURCE_NAME_EPROPERTY)
                }
                hashMap.put(EventConstant.ACTION_EPROPERTY,"Buy Gold")
                EventManager.getInstance().sendEvent(SubscriptionPopupPageEvent(hashMap))
            }

            setLog("SubscriptionQueryParam","url:${url}")
        }


    }

    private fun setUrlParameters2() {

        var plan_type = ""

        if (!triggerPoint.isEmpty()) {

            url2 = "https://payapihmopftd.hungama.com/payment?"
            plan_type = "subscription"

        } else {
            url2 = "https://payapihmopftd.hungama.com/plan?"
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

        val content_id = contentId
        url2 += "&content_id=$content_id"


        val model = HungamaMusicApp.getInstance().getEventData(contentId)
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

        setLog(TAG, "pay url2 seturl2Parameters: source:${source}")



        if (downloadPlayCheck != null && downloadPlayCheck.isDirectPaymentAction && !TextUtils.isEmpty(
                downloadPlayCheck.queryParam
            )
        ) {
            setLog(
                "downloadPlayCheck?.queryParam",
                "url2:${url2} downloadPlayCheck?.queryParam:${downloadPlayCheck.queryParam}"
            )
            url2 = url2 + "&" + downloadPlayCheck.queryParam
        }

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

        url2+= "&product=oppo"

        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = HashMap<String, String>()
            if (!TextUtils.isEmpty(model.songName)) {
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + model.sourceName)
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + model.songName)
            } else {
                hashMap.put(EventConstant.SOURCE_EPROPERTY, Utils.getContentTypeName(content_id))
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + contentId)
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
            CommonUtils.showToast(requireContext(), messageModel)
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
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == PAY_API_REQUEST_CODE) {
            setLog("TAG", "onActivityResult: planName:${planName}")
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
                                    setLog("TAG", "AppsFlyerLib planName:${planName} contentId:${contentId}")

                                    val responseTime = DateUtils.getCurrentDateTime()
                                    val diffInMillies: Long = Math.abs(requestTime?.getTime()!! - responseTime?.getTime()!!)

                                    val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        if(!TextUtils.isEmpty(planName)){
                                            if (planName.contains(PlanNames.TVOD.name, true) || planName.contains(PlanNames.PTVOD.name, true)|| planName.contains(PlanNames.LE.name, true)){
                                                /* Track Events in real time */

                                                val eventValue: MutableMap<String, Any> = HashMap()

                                                if(it?.data?.data?.tvod!=null && it?.data?.data?.tvod?.size!!>0){
                                                    it?.data?.data?.tvod?.forEach {
                                                        if(it?.contentId?.contains(contentId,true)!!){
                                                            eventValue.put(AFInAppEventParameterName.CONTENT_ID,it?.contentId!!)
                                                            eventValue.put(AFInAppEventParameterName.REVENUE,it?.planPrice!!)
                                                            eventValue.put(AFInAppEventParameterName.CURRENCY,it?.currency!!)
                                                            eventValue.put(EventConstant.AF_PURCHASE_METHOD_PROPERTY,it?.paymentSource!!)

                                                            val hashMap = HashMap<String,String>()
                                                            hashMap.put(EventConstant.PAYMENT_MODE,it?.paymentSource!!)
                                                            hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY,"" + ConnectionUtil(context).networkType)
                                                            hashMap.put(EventConstant.PLAN_SELECTED,it?.planName.toString())
                                                            hashMap.put(EventConstant.PROMO,it?.couponCode.toString())
                                                            hashMap.put(EventConstant.RESPONSECODE_EPROPERTY,"200")
//                                                            hashMap.put(EventConstant.LOAD_TIME,diff.toString())
                                                            hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, diff.toString())
//                                                            EventManager.getInstance().sendEvent(SubscriptionSuccessfulPageEvent(hashMap))

                                                            val hashMap1 = HashMap<String,String>()
                                                            hashMap1.put(EventConstant.MODE_EPROPERTY,it?.paymentSource!!)
                                                            hashMap1.put(EventConstant.METHOD_EPROPERTY,it?.paymentSourceDetails!!)
                                                            hashMap1.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
//                                                            EventManager.getInstance().sendEvent(RentPaymentOptionEvent(hashMap1))
                                                        }
                                                    }

//                                                    AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_PURCHASE_ENAME, eventValue)

                                                }

                                                setLog("TAG", "AppsFlyerLib eventName:${EventConstant.AF_PURCHASE_ENAME} eventProperties:${eventValue}")
                                            }else{

                                                /* Track Events in real time */

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
//                                                    hashMap.put(EventConstant.LOAD_TIME,diff.toString())
                                                    hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, diff.toString())

//                                                    EventManager.getInstance().sendEvent(SubscriptionSuccessfulPageEvent(hashMap))

                                                    setLog("TAG", "AppsFlyerLib eventName:${EventConstant.AF_SUBSCRIBE_ENAME} eventProperties:${eventValue}")
                                                }



                                            }
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
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }





    private fun updateSubscriptionCallback(status:Int){
        if(onUserSubscriptionUpdateCall!=null){
            onUserSubscriptionUpdateCall?.onUserSubscriptionUpdateCall(status, contentId)
        }

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
            CommonUtils.showToast(requireContext(), messageModel)
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
            if (jsonData.has(planName.lowercase())){
                plan = jsonData.getJSONArray(planName.lowercase())

            }else if (jsonData.has(planName)){
                plan = jsonData.getJSONArray(planName)
            }else if (planName.equals("TVOD-Premium") && jsonData.has("ptvod")){
                plan = jsonData.getJSONArray("ptvod")
            }else if (jsonData.has("tvod")){
                plan = jsonData.getJSONArray("tvod")
            }
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
                CommonUtils.showToast(requireContext(), messageModel)
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

}

