package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.CommonUtils.getUserSubscriptionTypeId
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_FREE
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.setAppButton2
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_WITH_ADS
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_subscription.*
import kotlinx.android.synthetic.main.fragment_subscription.llPlan
import kotlinx.android.synthetic.main.fragment_subscription.llUpgradePlan

class SubscriptionFragment : BaseFragment(), OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    var subscription_type = -1
    var isUpgradable = 1
    var orderId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewProfileFragment: View =
            inflater.inflate(R.layout.fragment_subscription, container, false)

        return viewProfileFragment
    }

    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener{ view -> backPress() }
        tvActionBarHeading.text = getString(R.string.general_setting_str_16)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "Profile","Settings_managemysubscription",
            "")
        rlMenu?.hide()
        val isUserLoggedIn = SharedPrefHelper.getInstance().isUserLoggedIn()
        if (isUserLoggedIn){
            setUpViewModel()
        }else{
            llPlan?.visibility = View.GONE
            llBuyGoldPlan?.visibility = View.VISIBLE
        }

        setAppButton2(requireContext(), llUpgradePlan2)
        setAppButton2(requireContext(), llUpgradePlan)
        setAppButton2(requireContext(), llCancelSubscription)
        llUpgradePlan2?.setOnClickListener(this)
        llUpgradePlan?.setOnClickListener(this)
        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v == llUpgradePlan || v == llUpgradePlan2){
            setEventModelDataAppLevel(
                "managemysubscription",
                "",
                "manage_my_subscription"
            )
            Constant.screen_name ="Manage My Subscription Screen"

            CommonUtils.openSubscriptionDialogPopup(requireContext(), PlanNames.SVOD.name, "managemysubscription", true, this, "", null,Constant.drawer_svod_purchase)
        }else if (v == llCancelSubscription){
            callUnsubscribeApi()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }
    private fun setUpViewModel() {
        userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)

        getUserSubscriptionStatus()
    }

    fun getUserSubscriptionStatus(){
        if (ConnectionUtil(requireContext()).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(requireContext())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null && it?.data?.success!!) {
                                setLog(TAG, "isViewLoading $it")
                                fillUI(it?.data)
                            }else{
                                if (!TextUtils.isEmpty(it?.data?.message)){
                                    Utils.showSnakbar(requireContext(),
                                        requireView(),
                                        false,
                                        it?.data?.message.toString()
                                    )
                                }
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun fillUI(userSubscriptionModel: UserSubscriptionModel) {
        if (userSubscriptionModel != null && userSubscriptionModel?.data != null
            && userSubscriptionModel?.data?.status!!){
            subscription_type = getUserSubscriptionTypeId(userSubscriptionModel)
            if (!TextUtils.isEmpty(userSubscriptionModel?.data?.user?.userMembershipType)){
                tvSubscriptionPlan?.text = userSubscriptionModel?.data?.user?.userMembershipType
                SharedPrefHelper.getInstance().setUserCurrentSubscriptionPlan(userSubscriptionModel?.data?.user?.userMembershipType!!)
            }else if (subscription_type == SUBSCRIPTION_TYPE_FREE){
                tvSubscriptionPlan?.text = getString(R.string.profile_str_31)
                SharedPrefHelper.getInstance().setUserCurrentSubscriptionPlan(getString(R.string.profile_str_31))
            }else{
                tvSubscriptionPlan?.text = "-"
            }

            if(userSubscriptionModel?.data?.profile_app_config != null
                && userSubscriptionModel?.data?.profile_app_config?.upgradable == 1 && subscription_type != SUBSCRIPTION_TYPE_GOLD){
                llUpgradeToGoldPlan?.visibility = View.VISIBLE
            }else{
                llUpgradeToGoldPlan?.visibility = View.GONE
            }

            if (subscription_type == SUBSCRIPTION_TYPE_GOLD || subscription_type == SUBSCRIPTION_TYPE_GOLD_WITH_ADS){
                ivHungama?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.gold_subscription_detail_bg))
                progressBarDays?.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.color_subscription_progress_gold))
            }else{
                ivHungama?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.hungama_text_icon_new))
                llBuyGoldPlan?.visibility = View.VISIBLE
                if (subscription_type == SUBSCRIPTION_TYPE_FREE){
                    llPlan?.visibility = View.GONE
                }
                progressBarDays?.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.color_subscription_progress_free))
            }

            if (userSubscriptionModel?.data?.subscription?.subscriptionStatus == 1){
                tvStatus?.text = getString(R.string.general_setting_str_51)
                tvStatus?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            }else{
                tvStatus?.text = getString(R.string.general_setting_str_50)
                tvStatus?.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_subscription_expired))
                llUpgradeToGoldPlan?.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(userSubscriptionModel?.data?.subscription?.orderId)){
                orderId = userSubscriptionModel?.data?.subscription?.orderId.toString()
            }


            if (userSubscriptionModel?.data?.subscription?.daysRemaining !=null && userSubscriptionModel?.data?.subscription?.daysRemaining!! > 0){
                tvDaysRemaining?.text = userSubscriptionModel?.data?.subscription?.daysRemaining!!.toString() + " " + getString(R.string.general_setting_str_52)
            }else{
                tvDaysRemaining?.text = "0 "+getString(R.string.general_setting_str_52)
            }

            if (!TextUtils.isEmpty(userSubscriptionModel?.data?.subscription?.planPrice)){
                tvAmount?.text = getString(R.string.general_setting_str_58) + " " +userSubscriptionModel?.data?.subscription?.planPrice!!.toString()
                tvDuration?.text = userSubscriptionModel?.data?.subscription?.planValidityName!!.toString()
            }else{
                tvAmount?.text = ""
                tvDuration?.text = "-"
            }

            if (userSubscriptionModel?.data?.autoRenewal?.status.equals("1", true)){
                tvPaymentType?.text = getString(R.string.general_setting_str_53)
            }else{
                tvPaymentType?.text = getString(R.string.general_setting_str_54)
            }

            if (!TextUtils.isEmpty(userSubscriptionModel?.data?.subscription?.subscriptionEndDate)){
                tvSubscriptionValidity?.text = DateUtils.convertDate(
                    DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_ss,
                    DateUtils.DATE_FORMAT_MMMM_DD_YYYY,userSubscriptionModel?.data?.subscription?.subscriptionEndDate)
            }else{
                tvSubscriptionValidity?.text = "-"
            }
            if (userSubscriptionModel?.data?.subscription?.totalDays!! > 0){
                progressBarDays?.max = userSubscriptionModel?.data?.subscription?.totalDays!!
                progressBarDays?.progress = userSubscriptionModel?.data?.subscription?.daysRemaining!!
            }else{
                progressBarDays?.max = 100
                progressBarDays?.progress = 100
            }

            if (userSubscriptionModel?.data?.subscription?.unsubButton == 1){
                llCancelSubscription?.visibility = View.VISIBLE
                llCancelSubscription?.setOnClickListener(this)
            }else{
                llCancelSubscription?.visibility = View.GONE
                llCancelSubscription?.setOnClickListener(null)
            }
        }

    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        setUpViewModel()
    }

    private fun callUnsubscribeApi(){
        if (ConnectionUtil(requireContext()).isOnline) {
            if (!TextUtils.isEmpty(orderId)){
                userSubscriptionViewModel?.getCancelPlan(requireContext(), orderId)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                if (it?.data != null && it?.data?.success!!){
                                    val messageModel = MessageModel(it?.data?.data?.response?.response.toString(), MessageType.NEUTRAL, true)
                                    CommonUtils.showToast(requireContext(), messageModel)
                                    llCancelSubscription?.visibility = View.GONE
                                    //setUpViewModel()
                                }
                            }

                            Status.LOADING ->{

                            }

                            Status.ERROR ->{

                            }
                        }
                    })
            }
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}