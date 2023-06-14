package com.hungama.music.ui.main.view.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.R
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SliderAdapter
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_order_details.*
import kotlinx.android.synthetic.main.order_detail_view.*


class OrderDetailsFragment(val datalist: UserOrdersModel.Order, var position: Int) : BaseFragment(), TabLayout.OnTabSelectedListener {
    var fragmentChildList: ArrayList<Fragment> = ArrayList()
    var fragmentChildName: ArrayList<String> = ArrayList()
    lateinit var slideradapter : SliderAdapter
    lateinit var myslides : ArrayList<SliderModel>
    var allProductList: ProductRespModel? = null
    //var orderProductDetail = ProductRespModel.Product()
    override fun initializeComponent(view: View) {
        /*allProductList = SharedPrefHelper.getInstance().getObjectProductList(PrefConstant.PRODUCT_LIST)
        if (allProductList != null && !allProductList?.products.isNullOrEmpty()){
            for (product in allProductList?.products?.iterator()!!){
                if (product != null && !product.variants.isNullOrEmpty() &&
                        product.variants?.get(0)?.productId == datalist.lineItems?.get(0)?.productId){
                    orderProductDetail = product
                }
            }
        }*/
        ivBack?.setOnClickListener { backPress() }
        tvActionBarHeading.text = getString(R.string.profile_str_37)
        ivMenu?.setImageDrawable(
            requireContext().faDrawable(
                R.string.icon_my_order,
                R.color.colorWhite
            )
        )
        rlMenu?.visibility = View.GONE
        setUpViewModel()
        sliderList()
        if (datalist.shippingAddress != null && !TextUtils.isEmpty(datalist.shippingAddress?.address1)
            && !TextUtils.isEmpty(datalist.shippingAddress?.city) && !TextUtils.isEmpty(datalist.shippingAddress?.zip)
        ) {
            var shippingDetail =
                datalist.shippingAddress?.firstName + " " + datalist.shippingAddress?.lastName
            shippingDetail += "\n" + datalist.shippingAddress?.address1 + ", " + datalist.shippingAddress?.address2
            shippingDetail += "\n" + datalist.shippingAddress?.city + ", " + datalist.shippingAddress?.state + " - " + datalist.shippingAddress?.zip
            tvShippingDetails?.text = shippingDetail
            clShippingAddress?.visibility = View.VISIBLE
        } else {
            clShippingAddress?.visibility = View.GONE
        }

        var userCoin = 0
        val userCoinDetailRespModel =
            SharedPrefHelper?.getInstance()?.getObjectUserCoin(PrefConstant.USER_COIN)
        if (userCoinDetailRespModel != null && !userCoinDetailRespModel?.actions.isNullOrEmpty()) {
            if (userCoinDetailRespModel.actions?.get(0)?.total != null) {
                userCoin = userCoinDetailRespModel.actions?.get(0)?.total!!
            }

        }
        CommonUtils.applyButtonTheme(requireContext(), btnGoToHome)
        when (CommonUtils.getRedeemProductType(datalist.lineItems.get(0).productType)) {
            Constant.tvodProduct -> {
                tvButtonText?.text = getString(R.string.reward_str_114)
                ivButtonIcon?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_play_2,
                        R.color.colorWhite
                    )
                )
                constraintLayout2?.visibility = View.VISIBLE
            }
            Constant.SubscriptionProduct -> {
                tvButtonText?.text = getString(R.string.reward_str_70)
                ivButtonIcon?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_search,
                        R.color.colorWhite
                    )
                )
                constraintLayout2?.visibility = View.VISIBLE
            }
            Constant.digitalProduct -> {
                tvButtonText?.text = getString(R.string.reward_str_69)
                ivButtonIcon?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_discover,
                        R.color.colorWhite
                    )
                )
                constraintLayout2?.visibility = View.VISIBLE
            }
            else -> {
                constraintLayout2?.visibility = View.GONE
            }
        }

        btnGoToHome.setOnClickListener {
            setLog(TAG, "initializeComponent: btngotoGom click")
                if (!watchButtonClicked && 4 == Constant.tvodProduct && !TextUtils.isEmpty(datalist?.lineItems?.get(0)?.sku.toString())) {
                    if (!TextUtils.isEmpty(dpm.contentId)){
                        play(dpm, attributeCensorRating)
                    }else{
                        watchButtonClicked = true
                        setUpPlayableContentListViewModel(datalist?.lineItems?.get(0)?.sku.toString())
                    }

                }
        }
    }
    fun sliderList(){
        myslides = ArrayList()

        datalist.lineItems.get(0).images?.forEach {
                    myslides.add(SliderModel(R.drawable.bg_gradient_placeholder, it?.src!!))
                }
                slideradapter = SliderAdapter(requireContext(),myslides)

                viewPager.adapter = slideradapter

    }
    /*
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        if (datalist != null && !datalist.lineItems.isNullOrEmpty()){
            tvHeading?.text = datalist.lineItems.get(0).name
            val coin = CommonUtils.covertNumberToCurrencyFormat(datalist.lineItems.get(0).price?.toDouble()?.toInt().toString())
            tvCoinCount?.text =  coin + " " +getString(R.string.profile_str_49)
            tvOrderIdNumber?.text = datalist.orderNumber.toString()
            tvCreatedDate?.text = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_TX, DateUtils.DATE_FORMAT_DD_MMMM_YYYY,
                datalist.createdAt
            )
        }
        tabs.addTab(tabs.newTab().setText(getString(R.string.profile_str_47)))
        val productType = CommonUtils.getRedeemProductType(datalist.lineItems.get(0).productType)
        if (productType == Constant.tvodProduct || productType == Constant.SubscriptionProduct){
            tabs.addTab(tabs.newTab().setText(getString(R.string.reward_str_72)))
        }else{
            tabs.addTab(tabs.newTab().setText(getString(R.string.profile_str_48)))
        }

        viewPagerSetUp(0)
    }


    private fun viewPagerSetUp(mainTabIndex: Int) {
        getChildFragmentList(mainTabIndex)
        vpTransactions?.adapter =
            TransactionPagerAdapter(this, fragmentChildList, fragmentChildName)
        setProgressBarVisible(false)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.sf_pro_text_bold
                )
                tab?.let {
                    setStyleForTab(it, Typeface.BOLD, typeface)
                }

                viewPagerSetUp(tab?.position!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.sf_pro_text_medium
                )
                tab?.let {
                    setStyleForTab(it, Typeface.NORMAL, typeface)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.sf_pro_text_bold
                )
                tab?.let {
                    setStyleForTab(it, Typeface.BOLD, typeface)
                }
            }

        })

        vpTransactions?.registerOnPageChangeCallback(pageChangeCallback)
        vpTransactions.isUserInputEnabled = false


        vpTransactions?.offscreenPageLimit = 1

        vpTransactions?.isUserInputEnabled = false
        setProgressBarVisible(false)
    }
    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        tab.view.children.find { it is TextView }?.let { tv ->
            (tv as TextView).post {
                tv.setTypeface(typeface, style)
            }
        }
    }

    fun getChildFragmentList(i:Int):java.util.ArrayList<String>{
        fragmentChildList = ArrayList()
        fragmentChildName = ArrayList()

        if(i == 0){
            fragmentChildList.add(DescriptionFragment(datalist.lineItems.get(0).bodyHtml.toString()))
            fragmentChildName.add(getString(R.string.profile_str_47))
        }else if (i == 1){
            fragmentChildList.add(ShippingReturnFragment())
            val productType = CommonUtils.getRedeemProductType(datalist.lineItems.get(0).productType)
            if (productType == Constant.tvodProduct || productType == Constant.SubscriptionProduct){
                fragmentChildName.add(getString(R.string.reward_str_72))
            }else{
                fragmentChildName.add(getString(R.string.profile_str_48))
            }

        }
        return fragmentChildName
    }
    class TransactionPagerAdapter(
        fa: OrderDetailsFragment,
        val mFragments: ArrayList<Fragment>,
        fragmentChildName: ArrayList<String>
    ) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int {
            return mFragments.size //Number of fragments displayed
        }

        @NonNull
        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }

    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            try {
                setLog("onPageSelected", "Selected position:" + position)
                vpTransactions?.adapter?.notifyItemChanged(position)
            }catch (e:Exception){

            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold
        )
        tab?.let {
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_medium
        )
        tab?.let {
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold
        )
        tab?.let {
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
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        hideBottomNavigationAndMiniplayer()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        showBottomNavigationAndMiniplayer()
        super.onDestroy()
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
        Constant.screen_name ="Order Details Screen"
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

///

