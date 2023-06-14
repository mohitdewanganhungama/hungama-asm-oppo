package com.hungama.music.ui.main.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.hungama.music.HungamaMusicApp
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.model.ProductRespModel
import com.hungama.music.data.model.ShippingDetailModel
import com.hungama.music.data.webservice.WSConstants.STATUS_200
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.RedeemCoinsEvent
import com.hungama.music.eventanalytic.eventreporter.SubscriptionSuccessfulPageEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.ProductViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getRedeemProductType
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.shippingDetailsKey
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_review_order.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray

class ReviewOrderFragment(val product: ProductRespModel.Product) : BaseFragment() {
    var shippingDetailModel = ShippingDetailModel()
    var productViewModel: ProductViewModel? = null
    var isConfirmOrderClick = false
    var userCoin = 0
    private fun displayUserCoins(){
        val coin = CommonUtils.covertNumberToCurrencyFormat(userCoin.toString())
        userCoins.text = coin
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnOrderContinue)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        ivBack?.setOnClickListener{ backPress() }
        tvActionBarHeading.text = getString(R.string.reward_str_40)
        ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_my_order, R.color.colorWhite))
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener {
            addFragment(R.id.fl_container,this, MyOrderListFragment(),false)
        }
        displayUserCoins()
        if (arguments != null && arguments?.containsKey(shippingDetailsKey)!!){
            shippingDetailModel = requireArguments().getSerializable(shippingDetailsKey) as ShippingDetailModel
        }

        if (shippingDetailModel.isPhisicalProduct){
            clcard2?.visibility = View.VISIBLE
            vDelivery?.visibility = View.GONE
            tvDeliveryTime?.visibility = View.GONE
            var shippingDetail = shippingDetailModel.firstName + " " + shippingDetailModel.lastName
            shippingDetail += "\n" + shippingDetailModel.address1 + ", " + shippingDetailModel.city
            shippingDetail += "\n" + shippingDetailModel.state + " - " + shippingDetailModel.pincode
            tvShippingDetails.text = shippingDetail
        }else{
            tvShippingDetails?.text = ""
            vDelivery?.visibility = View.GONE
            tvDeliveryTime?.visibility = View.GONE
            clcard2?.visibility = View.GONE
        }

        /*val userCoinDetailRespModel=
            SharedPrefHelper.getInstance().getObjectUserCoin(PrefConstant.USER_COIN)
        if(userCoinDetailRespModel!=null && !userCoinDetailRespModel.actions.isNullOrEmpty()){
            if (!TextUtils.isEmpty(userCoinDetailRespModel.actions?.get(0)?.total.toString())){
                userCoin  = userCoinDetailRespModel.actions?.get(0)?.total!!
                val coin = CommonUtils.covertNumberToCurrencyFormat(userCoin.toString())
                userCoins.text = coin
            }
        }*/

        if (product != null){
            if (!product.images.isNullOrEmpty()){
                ImageLoader.loadImage(
                    context,
                    ivproduct,
                    product.images?.get(0)?.src!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            tvproductTitle?.text = product?.title
            if (!product.variants.isNullOrEmpty()){
                val redeemedCoin = CommonUtils.covertNumberToCurrencyFormat(product?.variants?.get(0)?.price.toString())
                redeemCoin?.text = "-  " + redeemedCoin
                val balancedCoin = CommonUtils.covertNumberToCurrencyFormat((userCoin - product?.variants?.get(0)?.price?.toInt()!!).toString())
                tvBalanceCoin.text = balancedCoin
            }

        }

        btnOrderContinue.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnOrderContinue!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (!isConfirmOrderClick){
                if (shippingDetailModel.isPhisicalProduct){
                if (!TextUtils.isEmpty(shippingDetailModel.firstName)
                    && !TextUtils.isEmpty(shippingDetailModel.lastName)
                    && !TextUtils.isEmpty(shippingDetailModel.mobile)
                    && !TextUtils.isEmpty(shippingDetailModel.address1)
                    && !TextUtils.isEmpty(shippingDetailModel.address2)
                    && !TextUtils.isEmpty(shippingDetailModel.pincode)
                    && !TextUtils.isEmpty(shippingDetailModel.city)
                    && !TextUtils.isEmpty(shippingDetailModel.state)){
                    callCreateOrderApi(product, shippingDetailModel)
                }else{
                    val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
            }else{
                callCreateOrderApi(product, shippingDetailModel)
            }
            }

            //val productType = getRedeemProductType(product.productType)
            //addFragment(R.id.fl_container,this, OrderConformationFragment(productType),false)
        }

        ivEditShippingDetail?.setOnClickListener{
            backPress()
        }

        btnMyOrder?.setOnClickListener{
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnMyOrder!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            addFragment(R.id.fl_container,this, EarnCoinAllTabFragement(),false)
        }
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review_order, container, false)
    }

    private fun callCreateOrderApi(
        product: ProductRespModel.Product,
        shippingDetailModel: ShippingDetailModel
    ) {
        isConfirmOrderClick = true
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (!product.variants.isNullOrEmpty()){
            setLog("ReviewOrder", "ReviewOrder-product-sku-${product.variants?.get(0)?.sku}")
        }
        val productId = product.variants?.get(0)?.productId
        val productIdsJsonArray = JSONArray()
        productIdsJsonArray.put(productId)
        val productIds:ArrayList<Long> = ArrayList()
        if (productId != null) {
            productIds.add(productId)
        }
        var redeemableCoins = product.variants?.get(0)?.price?.toDouble()?.toInt()!!
        /*redirectToOrderSuccessPage(product, shippingDetailModel, 12345, redeemableCoins)
        return
        redeemableCoins = 1*/
        if (ConnectionUtil(requireContext()).isOnline && !productIdsJsonArray.isNull(0)) {
            productViewModel?.setCreateOrder(
                requireContext(), productIdsJsonArray, shippingDetailModel, redeemableCoins
            )?.observe(this
            ) {
                when (it.status) {
                    Status.SUCCESS -> {
                        setProgressBarVisible(false)
                        if (it?.data?.status == true && it.data.message.equals("ok", true)) {
                            redirectToOrderSuccessPage(
                                product,
                                shippingDetailModel,
                                it.data.orderId,
                                redeemableCoins
                            )
                        } else {
                            isConfirmOrderClick = false
                            if (!TextUtils.isEmpty(it?.data?.errors?.customer)) {
                                val messageModel = MessageModel(
                                    it?.data?.errors?.customer.toString(),
                                    MessageType.NEUTRAL,
                                    true
                                )
                                CommonUtils.showToast(requireContext(), messageModel)
                            } else if (it?.data?.status == false && !TextUtils.isEmpty(it.data.message)
                                && it.data.message.equals("UnAvailable zip code", true)
                                && getRedeemProductType(product.productType) == Constant.physicalProduct
                            ) {
                                //Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_LONG).show()
                                val intent = Intent(Constant.SHIPPING_DETAILS_EVENT)
                                intent.putExtra(
                                    "EVENT",
                                    Constant.SHIPPING_DETAILS_EVENT_RESULT_CODE
                                )
                                intent.putExtra("error", it.data.message)
                                LocalBroadcastManager.getInstance(requireContext())
                                    .sendBroadcast(intent)
                                backPress()
                            } else if (it?.data?.status == false && !TextUtils.isEmpty(it.data.message)) {
                                val messageModel = MessageModel(
                                    it.data.message.toString(),
                                    MessageType.NEUTRAL,
                                    true
                                )
                                CommonUtils.showToast(requireContext(), messageModel)
                            } else {
                                val messageModel = MessageModel(
                                    getString(R.string.discover_str_2),
                                    MessageType.NEUTRAL,
                                    true
                                )
                                CommonUtils.showToast(requireContext(), messageModel)
                            }
                        }
                    }

                    Status.LOADING -> {
                        setProgressBarVisible(false)
                    }

                    Status.ERROR -> {
                        isConfirmOrderClick = false
                        setProgressBarVisible(false)
                        if (!TextUtils.isEmpty(it.message)
                            && it.message.equals("UnAvailable zip code", true)
                            && getRedeemProductType(product.productType) == Constant.physicalProduct
                        ) {
                            //Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                            val intent = Intent(Constant.SHIPPING_DETAILS_EVENT)
                            intent.putExtra("EVENT", Constant.SHIPPING_DETAILS_EVENT_RESULT_CODE)
                            intent.putExtra("error", it.message)
                            LocalBroadcastManager.getInstance(requireContext())
                                .sendBroadcast(intent)
                            backPress()
                        } else {
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                }
            }

        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun getUserOrders() {
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {
            productViewModel?.getUserOrdersList(requireContext())?.observe(this,
                {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null){
                                SharedPrefHelper.getInstance().setUsersOrder(it.data)
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
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun redirectToOrderSuccessPage(
        product: ProductRespModel.Product,
        shippingDetailModel: ShippingDetailModel,
        orderId: Long,
        redeemableCoins: Int
    ) {
        if (activity != null){
            (requireActivity() as MainActivity).getUserSubscriptionStatus()
            (requireActivity() as MainActivity).getUserProfile()
        }
        getUserOrders()

        val productType = getRedeemProductType(product.productType)
        if (orderId > 0){
            sendCoinsRedeemEvent(redeemableCoins, productType)
            val fragment = OrderConformationFragment(productType)
            val bundle = Bundle()
            bundle.putLong(Constant.orderId, orderId)
            bundle.putInt(Constant.coinCount, redeemableCoins)
            setLog(TAG, redeemableCoins.toString())
            if (productType == Constant.physicalProduct){
                if (!TextUtils.isEmpty(shippingDetailModel.mobile)){
                    bundle.putString(Constant.mobile, shippingDetailModel.mobile)
                }
                SharedPrefHelper.getInstance().setUserLastShippingDetails(shippingDetailModel)
            }
            if (!product.variants.isNullOrEmpty()){
                setLog("ReviewOrder", "ReviewOrder-product-sku-${product.variants?.get(0)?.sku}")
                bundle.putString(Constant.sku, product.variants?.get(0)?.sku)
            }
            fragment.arguments = bundle
            addFragment(R.id.fl_container,this, fragment,false)


            when (productType) {
                Constant.SubscriptionProduct -> {

                    val eventValue: MutableMap<String, Any> = HashMap()
                    eventValue.put(AFInAppEventParameterName.REVENUE,product.variants?.get(0)?.price!!)
                    eventValue.put(AFInAppEventParameterName.CURRENCY,"INR")
                    eventValue.put(EventConstant.AF_START_DATE_PROPERTY,DateUtils.getCurrentDateTimeForCoin())
                    eventValue.put(EventConstant.AF_SUBSCRIPTION_METHOD_PROPERTY,"Coin")
                    eventValue.put(EventConstant.AF_SUBSCRIPTION_PERIOD_PROPERTY,product.handle!!)
                    AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,
                        EventConstant.AF_SUBSCRIBED_BY_COINS, eventValue)
                    eventValue.put(EventConstant.CUID, SharedPrefHelper.getInstance().getUserId().toString())


                    AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_SUBSCRIBE_ENAME, eventValue)


                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.PAYMENT_MODE,"Coin")
                    hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY,"" + ConnectionUtil(context).networkType)
                    hashMap.put(EventConstant.PLAN_SELECTED,product.handle!!)
                    EventManager.getInstance().sendEvent(SubscriptionSuccessfulPageEvent(hashMap))

                    setLog("ReviewOrder", "ReviewOrder-product- eventValue${eventValue}")
                }
                Constant.tvodProduct -> {
                    val eventValue: MutableMap<String, Any> = HashMap()
                    eventValue.put(AFInAppEventParameterName.REVENUE,product.variants?.get(0)?.price!!)
                    eventValue.put(AFInAppEventParameterName.CURRENCY,"INR")
                    eventValue.put(EventConstant.AF_SUBSCRIPTION_METHOD_PROPERTY,"Coin")
                    eventValue.put(AFInAppEventParameterName.CONTENT_ID, product?.id!!)
                    eventValue.put(EventConstant.AF_ID,"Coin")
                    AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,
                        EventConstant.AF_PURCHASE_ENAME, eventValue)

                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.PAYMENT_MODE,"Coin")
                    hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY,"" + ConnectionUtil(context).networkType)
                    hashMap.put(EventConstant.PLAN_SELECTED,"tvodProduct")
                    EventManager.getInstance().sendEvent(SubscriptionSuccessfulPageEvent(hashMap))

                    setLog("ReviewOrder", "ReviewOrder-product- eventValue${eventValue}")
                }
            }


        }
    }

    private fun sendCoinsRedeemEvent(coins: Int, productType: Int) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val dataMap=HashMap<String,String>()
                    var productTypeMain = ""
                    if (productType == Constant.physicalProduct){
                        productTypeMain = "Physical Product"
                    }else{
                        productTypeMain = "Digital Product"
                    }
                    dataMap.put(EventConstant.ACTION_EPROPERTY,""+productTypeMain)
                    dataMap.put(EventConstant.VALUE,""+coins)
                    val dateTime = DateUtils.getCurrentDateTime(DateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_ss)
                    dataMap.put(EventConstant.TIME_STAMP,""+dateTime)
                    EventManager.getInstance().sendEvent(RedeemCoinsEvent(dataMap))
                }

            }catch (e:java.lang.Exception){

            }
    }

    override fun onResume() {
        super.onResume()
        setLog("redeemCoin", "onResume-ReviewOrderFragment-userCoins-$userCoin")
        displayUserCoins()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            setLog("redeemCoin", "onHiddenChanged-ReviewOrderFragment-userCoins-$userCoins")
            displayUserCoins()
        }
    }
}