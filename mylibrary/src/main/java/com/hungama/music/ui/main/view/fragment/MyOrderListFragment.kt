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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.MyOrderListAdapter
import com.hungama.music.data.model.MyOrderListModel
import com.hungama.music.data.model.UserOrdersModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.ProductViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_my_order_list.*

class MyOrderListFragment : BaseFragment(), MyOrderListAdapter.OrderDetail,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var orderListAdapter : MyOrderListAdapter? = null
    var orderlist  = mutableListOf<MyOrderListModel>()
    var userOrderList:ArrayList<UserOrdersModel.Order> = ArrayList()
    var productViewModel:ProductViewModel? = null
    var apiCallIndex = -1
    override fun initializeComponent(view: View) {
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        CommonUtils.applyButtonTheme(requireContext(), btnRedeemNow)
        ivBack?.setOnClickListener{ backPress() }
        tvActionBarHeading.text = getString(R.string.reward_str_51)
        rlMyOrderList?.visibility = View.GONE
        clMyOrder?.visibility = View.GONE
        userOrderList = SharedPrefHelper.getInstance().getUserOrders().orders

        rlMyOrderList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        orderListAdapter = MyOrderListAdapter(requireContext(),this)
        rlMyOrderList.adapter = orderListAdapter
        orderlist.add(MyOrderListModel(R.drawable.bg_gradient_placeholder,"JBL C115 TWS by Hungama,True \n Wireless Earnbuds with Mic","Processing","22 Aug 2021 at 2:30 PM","459","7-10 Business Days"))
        orderlist.add(MyOrderListModel(R.drawable.bg_gradient_placeholder,"Adidas Discount Coupon \n Get 50% off","Deliverd","12 Aug 2021 at 5:30 PM","130","7- 10 Business Day"))
        orderlist.add(MyOrderListModel(R.drawable.bg_gradient_placeholder,"All Harry Potter Movies Collection","Cancelled","8 Aug 2021","500","7 - 10 Business Delivery"))

        if (!userOrderList.isNullOrEmpty()){
            reCallProductDetailApi()
        }else{
            getUserOrders()
        }

        btnRedeemNow?.setOnClickListener {
            addFragment(R.id.fl_container,this,
                EarnCoinAllTabFragement(),false)
        }
        CommonUtils.setPageBottomSpacing(nestedScrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_60),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

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
        return inflater.inflate(R.layout.fragment_my_order_list, container, false)
    }

    override fun ShowOrderDetail(orderData: UserOrdersModel.Order, position: Int) {
        setLog(TAG, "ShowOrderDetail: "+orderData.lineItems?.get(0)?.sku)
        addFragment(R.id.fl_container,this, OrderDetailsFragment(orderData,position),false)
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
                                userOrderList = SharedPrefHelper.getInstance().getUserOrders().orders
                            }
                            reCallProductDetailApi()
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(false)
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

    private fun callProductApi(productId:String){
        val productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (ConnectionUtil(context).isOnline) {
            productViewModel.getProductDetails(requireContext(), productId)?.observe(this,
                {
                    when(it.status){
                        Status.SUCCESS->{
                            if (it?.data?.product != null) {
                                CommonUtils.setLog(
                                    "MyOrder",
                                    "MyOrderListFragment-callProductApi-Responce-${it.data}"
                                )
                                userOrderList.forEachIndexed { index, order ->
                                    if (order.lineItems.get(0).productId == it.data.product.id){
                                        CommonUtils.setLog("MyOrder", "MyOrderListFragment-callProductApi-1-${it.data.product.title}")
                                        order.lineItems.get(0).productType = it.data.product.productType
                                        order.lineItems.get(0).image = it.data.product.image
                                        order.lineItems.get(0).images = it.data.product.images
                                        order.lineItems.get(0).bodyHtml = it.data.product.bodyHtml
                                    }
                                }
                                reCallProductDetailApi()
                            }
                        }

                        Status.LOADING ->{

                        }

                        Status.ERROR ->{
                            reCallProductDetailApi()
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun reCallProductDetailApi(){
        apiCallIndex++
        if (!userOrderList.isNullOrEmpty() && userOrderList.size > apiCallIndex){
            if (TextUtils.isEmpty(userOrderList.get(apiCallIndex).lineItems.get(0).productType)){
                CommonUtils.setLog(
                    "MyOrder",
                    "MyOrderListFragment-callProductApi-true-${userOrderList.get(apiCallIndex).lineItems.get(0).title}"
                )
                callProductApi(userOrderList.get(apiCallIndex).lineItems.get(0).productId.toString())
            }else{
                reCallProductDetailApi()
            }
        }else{
            CommonUtils.setLog(
                "MyOrder",
                "MyOrderListFragment-callProductApi-false-userOrderListSize-${userOrderList.size} < $apiCallIndex"
            )
            orderListAdapter?.setUserOrderData(userOrderList)
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            if (!userOrderList.isNullOrEmpty()){
                rlMyOrderList?.visibility = View.VISIBLE
                clMyOrder?.visibility = View.GONE
            }else{
                rlMyOrderList?.visibility = View.GONE
                clMyOrder?.visibility = View.VISIBLE
            }
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
                CommonUtils.setPageBottomSpacing(nestedScrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_60),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}