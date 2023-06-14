package com.hungama.music.ui.main.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.R
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.SubscriptionSuccessfulPageEvent
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.SubscriptionProduct
import com.hungama.music.utils.Constant.digitalProduct
import com.hungama.music.utils.Constant.physicalProduct
import com.hungama.music.utils.Constant.tvodProduct
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_order_conformation.*
import kotlinx.android.synthetic.main.fragment_order_conformation.btnMyOrder
import kotlinx.android.synthetic.main.layout_order_conformation.*
import java.util.concurrent.TimeUnit


class OrderConformationFragment(val productType: Int) : BaseFragment() {
    var orderId = 0L
    var mobileNo = ""
    var redeemableCoin = 0
    var sku = ""
    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnContinue)
        if (arguments != null){
            if (requireArguments().containsKey(Constant.orderId)){
                orderId = requireArguments().getLong(Constant.orderId)
                tvOrderId.text = orderId.toString()
            }

            if (requireArguments().containsKey(Constant.mobile)){
                mobileNo = requireArguments().getString(Constant.mobile).toString()
            }

            if (requireArguments().containsKey(Constant.coinCount)){
                redeemableCoin = requireArguments().getInt(Constant.coinCount)
                val coin = CommonUtils.covertNumberToCurrencyFormat(redeemableCoin.toString())
                tvRedeemableCoin.text = coin
            }

            if (requireArguments().containsKey(Constant.sku)){
                sku = requireArguments().getString(Constant.sku).toString()
            }
            setLog(TAG, redeemableCoin.toString())
        }
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        ivBack?.setOnClickListener{ backPress() }
        tvActionBarHeading.text = getString(R.string.reward_str_50)
        ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_my_order, R.color.colorWhite))
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener {
            addFragment(R.id.fl_container,this, MyOrderListFragment(),false)
        }

        when (productType) {
            physicalProduct -> {
                ivHungamaLogo?.visibility = View.GONE
                btnCode?.visibility = View.GONE
                copyToClipBoard?.visibility = View.GONE
                tvContinue?.text = getString(R.string.download_str_9)
                tvDescription?.text = "Your Product has redeemed and will be \n" +
                        "delivered to your address.\n" +
                        "You should receive an sms \n" +
                        "on $mobileNo"
            }
            digitalProduct -> {
                ivHungamaLogo?.visibility = View.GONE
                btnCode?.visibility = View.VISIBLE
                copyToClipBoard?.visibility = View.VISIBLE
                tvContinue?.text = getString(R.string.download_str_9)
                tvDescription?.text = "Your coupon has been redeemed, \n" +
                        "You should receive a mail at \n" + SharedPrefHelper.getInstance().getUserEmail()
            }
            SubscriptionProduct -> {
                ivHungamaLogo?.visibility = View.VISIBLE
                ivHungamaLogo.layoutParams.width = resources.getDimensionPixelSize(R.dimen.dimen_167_5)
                ivHungamaLogo.requestLayout()
                ivHungamaLogo.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.gold_subscription_detail_bg))
                btnCode?.visibility = View.GONE
                copyToClipBoard?.visibility = View.GONE
                CommonUtils.setAppButton2(requireContext(), btnContinue)
                tvContinue?.text = getString(R.string.download_str_9)
                tvDescription?.text = "Your are now a Hungama Gold member.\n" +
                        "Enjoy ad free music, podcasts, Movies, TV \n" +
                        "shows & Hungama Originals."


            }
            tvodProduct -> {
                ivHungamaLogo?.visibility = View.VISIBLE
                ivHungamaLogo.layoutParams.width = resources.getDimensionPixelSize(R.dimen.dimen_109_5)
                ivHungamaLogo.requestLayout()
                ivHungamaLogo.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.hungama_text_icon_new))
                btnCode?.visibility = View.GONE
                copyToClipBoard?.visibility = View.GONE
                tvContinue?.text = getString(R.string.reward_str_114)
                tvDescription?.text = "Your are now a Hungama Gold member.\n" +
                        "Enjoy ad free music, podcasts, Movies, TV \n" +
                        "shows & Hungama Originals."



            }
        }

        val orderDt = DateUtils.getCurrentDateTimeNewFormat()
        val strDate = DateUtils.convertDate(DateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS, DateUtils.DATE_FORMAT_DD_MMM_YYYY, orderDt)
        val strTime = DateUtils.convertDate(DateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS, DateUtils.DATE_FORMAT_hh_mm_A, orderDt)
        CommonUtils.setLog(TAG, "orderDT - $strDate | $strTime")
        tvOrderDt.text = "$strDate | $strTime"

        var userCoin = 0
        val userCoinDetailRespModel=
            SharedPrefHelper.getInstance().getObjectUserCoin(PrefConstant.USER_COIN)
        if(userCoinDetailRespModel!=null && !userCoinDetailRespModel.actions.isNullOrEmpty()){
            if (!TextUtils.isEmpty(userCoinDetailRespModel.actions?.get(0)?.total.toString())){
                userCoin  = userCoinDetailRespModel.actions?.get(0)?.total!!
            }

        }
        val coinBalance = userCoin - redeemableCoin
        val coin = CommonUtils.covertNumberToCurrencyFormat(coinBalance.toString())
        tvCoinBalance.text = coin

        ivOrderCart.visibility = View.VISIBLE
        ivOrderCart2.visibility = View.VISIBLE
        tvNeedeHelp.visibility = View.VISIBLE
        btnMyOrder.visibility = View.VISIBLE
        btnContinue.visibility = View.VISIBLE
        copyToClipBoard.setOnClickListener {
            CommonUtils.copyToTheClipboard(requireContext(), btnCode?.text.toString())
            val messageModel = MessageModel(getString(R.string.reward_str_115), MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
        btnMyOrder?.setOnClickListener {
            addFragment(R.id.fl_container,this, MyOrderListFragment(),false)
        }
        btnContinue?.setOnClickListener {
            if (productType == tvodProduct && !TextUtils.isEmpty(sku)){
                if (!TextUtils.isEmpty(dpm.contentId)){
                    play(dpm, attributeCensorRating)
                }else{
                    watchButtonClicked = true
                    setUpPlayableContentListViewModel(sku.toString())
                }
            }else{
                //-----------------------------------------------------------------------------
                //Do not remove these back press. It is used for navigate to redeem home screen
                backPress()
                backPress()
                backPress()
                if (productType == physicalProduct){
                    backPress()
                }

                //-----------------------------------------------------------------------------
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_conformation, container, false)
    }


    var watchButtonClicked = false
    val dpm = DownloadPlayCheckModel()
    var attributeCensorRating = ""
    private fun setUpPlayableContentListViewModel(id:String) {
        val videoListViewModel = ViewModelProvider(
            this
        ).get(VideoViewModel::class.java)

        if (ConnectionUtil(requireContext()).isOnline) {
            videoListViewModel.getVideoList(requireContext(), id, 5)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                watchButtonClicked = false

                                dpm.contentId = it.data.data.head.headData.id
                                dpm.contentTitle = it.data.data.head.headData.title
                                dpm.planName = it.data.data.head.headData.misc.movierights.toString()
                                dpm.isAudio = false
                                dpm.isDownloadAction = false
                                dpm.isDirectPaymentAction = true
                                dpm.isShowSubscriptionPopup = true
                                dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                                dpm.restrictedDownload = RestrictedDownload.valueOf(it.data.data.head.headData.misc.restricted_download)
                                if (!it.data.data.head.headData.misc.attributeCensorRating.isNullOrEmpty()){
                                    attributeCensorRating = it.data.data.head.headData.misc.attributeCensorRating.get(0)
                                }
                                play(dpm, attributeCensorRating)
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            watchButtonClicked = false
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            watchButtonClicked = false
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun play(dpm: DownloadPlayCheckModel, attributeCensorRating: String) {
        Constant.screen_name ="Order Conformation Screen"
        if (CommonUtils.userCanDownloadContent(requireContext(), requireView(), dpm, null,Constant.drawer_svod_download)) {
            if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating)) {
                setLog(TAG, "initializeComponent: btngotoGom  if condition click")
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                val serviceBundle = Bundle()
                serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                serviceBundle.putString(Constant.SELECTED_CONTENT_ID, dpm.contentId)
                serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_MOVIE)
                serviceBundle.putInt(Constant.TYPE_ID, Constant.CONTENT_MOVIE)
                intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                requireContext().startActivity(intent)
            }
        }
    }
}