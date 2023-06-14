package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SimilarProductAdapter
import com.hungama.music.ui.main.adapter.SliderAdapter
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.isUserHasRentedSubscription
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.showToast
import com.hungama.music.utils.Constant
import com.hungama.music.utils.DateUtils
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.earncoin_product_header.*
import kotlinx.android.synthetic.main.earncoin_product_header.rvProduct
import kotlinx.android.synthetic.main.earncoin_product_header.tabs
import kotlinx.android.synthetic.main.earncoin_product_header.vpTransactions
import kotlinx.android.synthetic.main.fragment_earn_coin_product.*

class EarnCoinProductFragment(val datalist: ProductRespModel.Product?) : BaseFragment(),TabLayout.OnTabSelectedListener, SimilarProductAdapter.RedeemInterface,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var fragmentChildList: ArrayList<Fragment> = ArrayList()
    var fragmentChildName: ArrayList<String> = ArrayList()

    lateinit var slideradapter : SliderAdapter
    lateinit var myslides : ArrayList<SliderModel>
    var AllProductList: ProductRespModel? = null
    lateinit var earnCoinProductAdapter : SimilarProductAdapter
    var productList:ArrayList<ProductRespModel.Product?>? = ArrayList()
    var userOrderList:ArrayList<UserOrdersModel.Order> = ArrayList()
    var userCoins = 0

    private fun displayUserCoins(){
        setLog("redeemCoin", "displayUserCoins-EarnCoinProductFragment-before-userCoins-$userCoins")
        setLog("redeemCoin", "displayUserCoins-EarnCoinProductFragment-after-userCoins-$userCoins")
    }

    override fun initializeComponent(view: View) {
        userOrderList = SharedPrefHelper.getInstance().getUserOrders().orders
        AllProductList = SharedPrefHelper?.getInstance()?.getObjectProductList(PrefConstant.PRODUCT_LIST)
        shimmerLayout.visibility  = View.GONE
        shimmerLayout.stopShimmer()
        ivBack?.setOnClickListener{ backPress() }
        tvActionBarHeading.text = getString(R.string.profile_str_37)
        ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_my_order, R.color.colorWhite))
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener {
            addFragment(R.id.fl_container,this, MyOrderListFragment(),false)
        }
        displayUserCoins()
        setUpViewModel()
        sliderList()
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })


        /*val userCoinDetailRespModel=
            SharedPrefHelper?.getInstance()?.getObjectUserCoin(PrefConstant.USER_COIN)
        setLog("MyOrder", "EarnCoinProductFragment - initializeComponent - userCoinModel =${userCoinDetailRespModel?.actions}")
        if(userCoinDetailRespModel!=null && !userCoinDetailRespModel?.actions.isNullOrEmpty()){
            if (userCoinDetailRespModel.actions?.get(0)?.total != null){
                userCoin = userCoinDetailRespModel.actions?.get(0)?.total!!
            }

        }*/

        setLog("MyOrder", "EarnCoinProductFragment - initializeComponent - userCoin =$userCoins")

        productList = ArrayList()
        AllProductList?.products?.forEach {
            if (datalist?.productType?.equals(it?.productType, true)!!){
                if (!it?.variants.isNullOrEmpty()){
                    it?.variants?.get(0)?.price = it?.variants?.get(0)?.price?.toDouble()?.toInt().toString()
                }
                productList?.add(it)
            }
            if (datalist?.id == it?.id){
                if (!it?.variants.isNullOrEmpty()){
                    if (userCoins >= it?.variants?.get(0)?.price?.toDouble()?.toInt()!!
                        && it?.variants?.get(0)?.inventoryQuantity!! > 0){
                        tvOutOfStock?.text = getString(R.string.profile_str_50)
                        ivOutOfStock?.setImageDrawable(requireContext().faDrawable(R.string.icon_redeem_coin, R.color.colorWhite))
                        val product = it
                        btnOutOfStock.setOnClickListener {
                            try {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    CommonUtils.hapticVibration(requireContext(), btnOutOfStock!!,
                                        HapticFeedbackConstants.CONTEXT_CLICK
                                    )
                                }
                            }catch (e:Exception){

                            }
                            val userSubscriptionDetail = SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
                            var isAlreadyPurchasedToday = false
                            var isAlreadyPurchasedPhysicalProductToday = false
                            var isAlreadyActiveSubscription = false
                            var isAlreadyActiveTvodSubscription = false
                            val productType =
                                CommonUtils.getRedeemProductType(product.productType)
                            if (!userOrderList.isNullOrEmpty()){
                                val currentDate = DateUtils.getCurrentDate()
                                userOrderList.forEachIndexed { index, order ->
                                    val orderDate = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_TX, DateUtils.DATE_FORMAT_YYYY_MM_DD, order.createdAt)
                                    val difference = DateUtils.compareToDay(orderDate, currentDate)
                                    if (order.lineItems.get(0).productId == product.id){
                                        setLog("MyOrder", "EarnCoinProductFragment-ByProduct-OrderDate=$orderDate and today=$currentDate")
                                        if (difference == 0){
                                            isAlreadyPurchasedToday = true
                                        }
                                    }else if (difference == 0 && productType == Constant.physicalProduct){
                                        if (!TextUtils.isEmpty(order.lineItems.get(0).productType)){
                                            val orderProductType =
                                                CommonUtils.getRedeemProductType(order.lineItems.get(0).productType)
                                            if (orderProductType == Constant.physicalProduct){
                                                isAlreadyPurchasedPhysicalProductToday = true
                                            }
                                        }
                                    } else if (productType == Constant.SubscriptionProduct){
                                        if (userSubscriptionDetail != null && userSubscriptionDetail.data?.subscription?.subscriptionStatus == 1){
                                            isAlreadyActiveSubscription = true
                                        }
                                    }else if (productType == Constant.tvodProduct
                                        && order.shippingAddress == null || (order.shippingAddress != null && TextUtils.isEmpty(order.shippingAddress?.zip))){
                                        if (userSubscriptionDetail != null && !userSubscriptionDetail.data?.tvod.isNullOrEmpty()){
                                            userSubscriptionDetail.data?.tvod?.forEachIndexed { index, tvod ->
                                                if (tvod != null && tvod.orderId.equals(order.id.toString(), true)){
                                                    if (isUserHasRentedSubscription(tvod.contentId.toString())){
                                                        isAlreadyActiveTvodSubscription = true
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }else{
                                if (productType == Constant.SubscriptionProduct){
                                    if (userSubscriptionDetail != null && userSubscriptionDetail.data?.subscription?.subscriptionStatus == 1){
                                        isAlreadyActiveSubscription = true
                                    }
                                }
                            }
                            if (!isAlreadyPurchasedToday && !isAlreadyPurchasedPhysicalProductToday
                                && !isAlreadyActiveSubscription && !isAlreadyActiveTvodSubscription) {

                                if (productType != Constant.physicalProduct) {
                                    addFragment(
                                        R.id.fl_container, this,
                                        ReviewOrderFragment(product), false
                                    )
                                } else {
                                    addFragment(
                                        R.id.fl_container, this,
                                        ShippingDetailFragment(product), false
                                    )
                                }
                            }else{
                                var messageModel:MessageModel? = null
                                if (isAlreadyPurchasedToday){
                                    messageModel = MessageModel("You have already purchased this item today. Please try again tomorrow.", MessageType.NEUTRAL, true)
                                }else if (isAlreadyPurchasedPhysicalProductToday){
                                    messageModel = MessageModel("You have already purchased one physical item today. Please try again tomorrow.", MessageType.NEUTRAL, true)
                                }else if (isAlreadyActiveSubscription){
                                    messageModel = MessageModel("You already have active subscription.", MessageType.NEUTRAL, true)
                                }else if (isAlreadyActiveTvodSubscription){
                                    messageModel = MessageModel("You already have active subscription.", MessageType.NEUTRAL, true)
                                }
                                if (messageModel != null) {
                                    showToast(requireContext(), messageModel)
                                }
                            }
                        }
                        //btnOutOfStock?.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius_18_bg_blue)
                        CommonUtils.applyButtonTheme(requireContext(), btnOutOfStock)
                    }else if (userCoins < it?.variants?.get(0)?.price?.toDouble()?.toInt()!!){
                        tvEarn.visibility = View.VISIBLE
                        ivEarnCoin.visibility = View.VISIBLE
                        tvEarn?.text = getString(R.string.profile_str_46)+" "
                        val coin = CommonUtils.covertNumberToCurrencyFormat((it?.variants?.get(0)?.price?.toDouble()?.toInt()!! - userCoins).toString())
                        tvOutOfStock?.text =" "+ coin + " " + getString(
                            R.string.profile_str_52)
                        ivOutOfStock?.setImageDrawable(requireContext().faDrawable(R.string.icon_earn_coin, R.color.colorWhite))
                        btnOutOfStock.setOnClickListener {
                            addFragment(R.id.fl_container,this,
                                EarnCoinDetailFragment(),false)
                        }
                        //btnOutOfStock?.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius_18_bg_blue)
                        CommonUtils.applyButtonTheme(requireContext(), btnOutOfStock)
                    }else if (it?.variants?.get(0)?.inventoryQuantity!! <= 0){
                        tvOutOfStock?.text = getString(R.string.profile_str_54)
                        ivOutOfStock?.setImageDrawable(requireContext().faDrawable(R.string.icon_out_of_stock, R.color.colorWhite))
                        btnOutOfStock.setOnClickListener(null)
                        btnOutOfStock?.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius_18_bg_out_of_stock)
                    }
                }

            }
        }

        rvProduct.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        earnCoinProductAdapter = SimilarProductAdapter(requireContext(),this, false)
        rvProduct.adapter = earnCoinProductAdapter
      if (!productList.isNullOrEmpty()){
            earnCoinProductAdapter.setcart(productList!!)
        }


    }
    fun sliderList(){
        myslides = ArrayList()
        datalist?.images?.forEach {
            myslides.add(SliderModel(R.drawable.bg_gradient_placeholder, it?.src!!))
        }
        slideradapter = SliderAdapter(requireContext(),myslides)

        viewPager.adapter = slideradapter
    }
    /*
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        if (datalist != null){
            tvHeading?.text = datalist?.title
            if (!datalist?.variants.isNullOrEmpty()){
                val coin = CommonUtils.covertNumberToCurrencyFormat(datalist?.variants?.get(0)?.price?.toDouble()?.toInt().toString())
                tvCoinCount?.text = coin
            }

        }
        tabs.addTab(tabs.newTab().setText(getString(R.string.profile_str_47)))
        val productType = CommonUtils.getRedeemProductType(datalist?.productType)
        if (productType == Constant.tvodProduct || productType == Constant.SubscriptionProduct){
            tabs.addTab(tabs.newTab().setText(getString(R.string.reward_str_72)))
        }else{
            tabs.addTab(tabs.newTab().setText(getString(R.string.profile_str_48)))
        }

        viewPagerSetUp(0)
        constraintLayout2.visibility = View.VISIBLE
        mainView.visibility = View.VISIBLE
//        shimmerLayout.visibility = View.GONE
//        shimmerLayout.stopShimmer()
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
        constraintLayout2.visibility = View.VISIBLE
        mainView.visibility = View.VISIBLE
//        shimmerLayout.visibility = View.GONE
//        shimmerLayout.stopShimmer()
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

            fragmentChildList.add(DescriptionFragment(datalist?.bodyHtml.toString()))
            fragmentChildName.add(getString(R.string.profile_str_47))
        }else if (i == 1){
            fragmentChildList.add(ShippingReturnFragment())
            val productType = CommonUtils.getRedeemProductType(datalist?.productType)
            if (productType == Constant.tvodProduct || productType == Constant.SubscriptionProduct){
                fragmentChildName.add(getString(R.string.reward_str_72))
            }else{
                fragmentChildName.add(getString(R.string.profile_str_48))
            }

        }
        return fragmentChildName
    }
    class TransactionPagerAdapter(
        fa: EarnCoinProductFragment,
        val mFragments: ArrayList<Fragment>,
        fragmentChildName: ArrayList<String>
    ) : FragmentStateAdapter(fa!!) {

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
                vpTransactions?.let {
                    vpTransactions?.adapter?.notifyItemChanged(position)
                }
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
        return inflater.inflate(R.layout.fragment_earn_coin_product, container, false)
    }

    override fun btnRedeem(datalist: ProductRespModel.Product?) {
        addFragment(R.id.fl_container,this, EarnCoinProductFragment(datalist),false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        hideBottomNavigationAndMiniplayer()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        showBottomNavigationAndMiniplayer()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        //setLocalBroadcast()
        setLog("redeemCoin", "onResume-EarnCoinProductFragment-userCoins-$userCoins")
        displayUserCoins()
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                /*CommonUtils.setPageBottomSpacing(, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)*/
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            setLog("redeemCoin", "onHiddenChanged-EarnCoinProductFragment-userCoins-$userCoins")
            displayUserCoins()
        }
    }

}