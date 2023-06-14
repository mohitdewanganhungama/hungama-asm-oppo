package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.CompoundButton
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.PrefConstant.Companion.PRODUCT_LIST
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.ProductListAdapter
import com.hungama.music.ui.main.adapter.SliderAdapter
import com.hungama.music.ui.main.viewmodel.ProductViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getCommaSeparatedStringToArray
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.SubscriptionProduct
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.earn_coin_tab.*
import kotlinx.android.synthetic.main.fr_redeem_coin.*
import kotlinx.android.synthetic.main.fr_redeem_coin.shimmerLayout
import kotlinx.android.synthetic.main.fragment_earn_coin_all_tab_fragement.*
import java.util.*
import kotlin.collections.ArrayList

class RedeemCoinFragment(val smartCollection: ProductCategoryRespModel.SmartCollection?) : BaseFragment(), ProductListAdapter.RedeemInterface,
    CompoundButton.OnCheckedChangeListener, BaseActivity.OnLocalBroadcastEventCallBack {
    lateinit var earnCoinCartadapter : ProductListAdapter
    var cartlist = mutableListOf<EarnCoinCartModel>()

    lateinit var myslider : ArrayList<SliderModel>
    lateinit var sliderAdapter : SliderAdapter

    var productList:ArrayList<ProductRespModel.Product?>? = ArrayList()
    var tempMainProductList:ArrayList<ProductRespModel.Product?>? = ArrayList()
    var productViewModel:ProductViewModel? = null
    var AllProductList: ProductRespModel? = null
    var userCoins = 0
    var filteredProductList:ArrayList<ProductRespModel.Product?> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_redeem_coin, container, false)
    }

    private fun displayUserCoins(){
        val coin = CommonUtils.covertNumberToCurrencyFormat(userCoins.toString())
        tvCountPoint.text = coin
    }

    override fun initializeComponent(view: View) {
        showHideSkeletonView(true)
        /*val userCoinDetailRespModel=
            SharedPrefHelper?.getInstance()?.getObjectUserCoin(PrefConstant.USER_COIN)
        if(userCoinDetailRespModel!=null && !userCoinDetailRespModel?.actions.isNullOrEmpty()){
            if (!TextUtils.isEmpty(userCoinDetailRespModel.actions?.get(0)?.total.toString())){
                userCoins = userCoinDetailRespModel.actions?.get(0)?.total!!
                val coin = CommonUtils.covertNumberToCurrencyFormat(userCoins.toString())
                tvCountPoint.text = coin
            }

        }*/
        displayUserCoins()
        ivBack?.setOnClickListener{ v -> backPress() }
        tvActionBarHeading.text = getString(R.string.profile_str_37)
        ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_my_order2, R.color.colorWhite))
        rlMenu?.visibility = View.VISIBLE
        AllProductList = SharedPrefHelper.getInstance()?.getObjectProductList(PRODUCT_LIST)
        if (smartCollection?.id != null){
            getAllProduct(smartCollection.id!!)
            loadCards(smartCollection.image)
        }else{
            if (AllProductList != null && !AllProductList?.products.isNullOrEmpty()){
                setProductList(AllProductList!!)
            }else{
                showHideSkeletonView(false)
            }
            loadCards(null)
        }

        /*val productForRedeemRespModel = Gson().fromJson<ProductForRedeemRespModel>(
            CommonUtils.getProductDataFromJSON(
                requireContext(),
                requireContext().resources.openRawResource(R.raw.product_for_redeem)
            ).toString(),
            ProductForRedeemRespModel::class.java
        ) as ProductForRedeemRespModel*/

        viewPagerRedeem.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
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
        CommonUtils.applyButtonTheme(requireContext(), btnEarnCoin)
        btnEarnCoin.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnEarnCoin!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
//            clNoCoinAvailable.visibility = View.GONE
//            rvCart.visibility = View.VISIBLE

            addFragment(R.id.fl_container,this,
                EarnCoinDetailFragment(),false)
        }

        svMobileNotification?.setOnCheckedChangeListener(this)
        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    private fun loadCards(image: ProductCategoryRespModel.SmartCollection.Image?) {
        myslider = ArrayList()
        if (image != null && !TextUtils.isEmpty(image.src)){
            /*val lp = rlSlider?.layoutParams
            lp?.height = image.height
            rlSlider?.requestLayout()*/
            rlSlider?.visibility = View.VISIBLE
            myslider.add(SliderModel(R.drawable.bg_gradient_placeholder, image.src, image.width, image.height))
            /*myslider.add(SliderModel(R.drawable.bg_gradient_placeholder, ""))
            myslider.add(SliderModel(R.drawable.bg_gradient_placeholder, bannerImage))
            myslider.add(SliderModel(R.drawable.bg_gradient_placeholder, ""))*/
            sliderAdapter = SliderAdapter(requireContext(),myslider)
            viewPagerRedeem.adapter = sliderAdapter
        }else{
            val lp = rlSlider?.layoutParams
            lp?.height = 0
            rlSlider?.requestLayout()
            rlSlider?.visibility = View.INVISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun btnRedeem(datalist: ProductRespModel.Product?) {
        addFragment(R.id.fl_container,this, EarnCoinProductFragment(datalist),false)
    }

    private fun getAllProduct(id: Long) {
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {
            productViewModel?.getProductCategoryProductList(requireContext(), id)?.observe(this,
                {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null && !it.data.products.isNullOrEmpty()){
                                setLog("aohgahl","\nReddemFragment " + Gson().toJson(it?.data) + "\n")
                                setProductList(it?.data)
                            }else{
                                showHideSkeletonView(false)
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            showHideSkeletonView(false)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun setProductList(productRespModel: ProductRespModel) {
        productList = ArrayList()
        productRespModel.products?.forEachIndexed { i, productForRedeem ->
            AllProductList?.products?.forEachIndexed { k, product ->
                if (productForRedeem?.id == product?.id){
                    if (!product?.variants.isNullOrEmpty()){
                        if (!productForRedeem?.variants.isNullOrEmpty()){
                            productForRedeem?.variants?.get(0)?.price = product?.variants?.get(0)?.price?.toDouble()?.toInt().toString()
                            productForRedeem?.variants?.get(0)?.inventoryQuantity = product?.variants?.get(0)?.inventoryQuantity
                        }else{
                            productForRedeem?.variants = product?.variants
                        }
                    }

                    //As discussed with Sushant shinde on 08-08-2022
                    //Don't add/show already active subscription plan
                    val productType = CommonUtils.getRedeemProductType(productForRedeem?.productType)
                    if (productType == SubscriptionProduct && productForRedeem?.tags?.contains("plan_id_", true)!!){
                        val userSubscriptionDetail = SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
                        //For test purpose
                        //userSubscriptionDetail?.data?.subscription?.planDetailsId = 10
                        val list = getCommaSeparatedStringToArray(productForRedeem.tags.toString())
                        if (userSubscriptionDetail != null
                            && list.contains("plan_id_"+userSubscriptionDetail.data?.subscription?.planId.toString())
                            && userSubscriptionDetail.data?.subscription?.subscriptionStatus == 1){

                        }else{
                            productList?.add(productForRedeem)
                        }
                    }else if (productType != SubscriptionProduct){
                        productList?.add(productForRedeem)
                    }
                }
            }
        }
        tempMainProductList = productList
        setupProductAdapter(productList!!)
        setFilteredList(productList)
        showHideSkeletonView(false)
    }

    private fun setupProductAdapter(productList: ArrayList<ProductRespModel.Product?>) {
        rvCart.layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false)
        earnCoinCartadapter = ProductListAdapter(requireContext(),this)
        rvCart.adapter = earnCoinCartadapter
        /*cartlist.add(EarnCoinCartModel("JBL C15 Wireless \n Earbuds","430 Coins",R.drawable.bg_gradient_placeholder,R.drawable.bg_gradient_placeholder))
        cartlist.add(EarnCoinCartModel("Pop Socket \n Black","160 Coins",R.drawable.bg_gradient_placeholder,R.drawable.bg_gradient_placeholder))
        cartlist.add(EarnCoinCartModel("Smart Band  \n braclet","340 Coins ",R.drawable.bg_gradient_placeholder,R.drawable.bg_gradient_placeholder))
        cartlist.add(EarnCoinCartModel("coster Rock","160 Coins",R.drawable.bg_gradient_placeholder,R.drawable.bg_gradient_placeholder))
        cartlist.add(EarnCoinCartModel("earPhone Case","360 Coins",R.drawable.bg_gradient_placeholder,R.drawable.bg_gradient_placeholder))
        cartlist.add(EarnCoinCartModel("Adidas Discount Coupon","160 Coins",R.drawable.bg_gradient_placeholder,R.drawable.bg_gradient_placeholder))*/
        setupView(productList)
    }

    private fun setFilteredList(productList: ArrayList<ProductRespModel.Product?>?) {
        if (!productList.isNullOrEmpty()){
            for (product in productList.iterator()){
                if (!product?.variants.isNullOrEmpty()
                    && product?.variants?.get(0)?.price.toString().toDouble().toInt() <= userCoins){
                    filteredProductList.add(product)
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked){
            productList = filteredProductList
            setupView(productList!!)
            /*if (!productList.isNullOrEmpty()){
                setupProductAdapter(productList!!)
            }else{
                setupView(productList!!)
            }*/
        }else{
            productList = tempMainProductList
            setupView(productList!!)
            /*if (!productList.isNullOrEmpty()){
                setupProductAdapter(productList!!)
            }else{
                setupView(productList!!)
            }*/
        }
    }

    private fun setupView(productList: ArrayList<ProductRespModel.Product?>) {
        if (!productList.isNullOrEmpty()){
            earnCoinCartadapter.setcart(productList)
            clNoCoinAvailable.visibility = View.GONE
            rvCart.visibility = View.VISIBLE
        }else{
            clNoCoinAvailable.visibility = View.VISIBLE
            rvCart.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        setLog("redeemCoin", "onResume-RedeemCoinFragment-userCoins-$userCoins")
        displayUserCoins()
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

    private fun showHideSkeletonView(status:Boolean){
        if (status){
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
            scrollView.visibility = View.GONE
        }else{
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            scrollView.visibility = View.VISIBLE
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            setLog("redeemCoin", "onHiddenChanged-RedeemCoinFragment-userCoins-$userCoins")
            displayUserCoins()
        }
    }
}