package com.hungama.music.ui.main.view.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.hungama.music.utils.preference.PrefConstant.Companion.PRODUCT_LIST
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.model.ProductCategoryRespModel
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.ProductViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_home.*
import kotlinx.android.synthetic.main.fragment_earn_coin_all_tab_fragement.*
import kotlinx.android.synthetic.main.fragment_earn_coin_all_tab_fragement.shimmerLayout
import kotlinx.android.synthetic.main.fragment_earn_coin_all_tab_fragement.tabs
import kotlinx.android.synthetic.main.fragment_earn_coin_all_tab_fragement.vpTransactions
import kotlin.collections.ArrayList

class EarnCoinAllTabFragement : BaseFragment() , TabLayout.OnTabSelectedListener {

    var fragmentList: ArrayList<Fragment> = ArrayList()
    var fragmentName: ArrayList<String> = ArrayList()
    var productViewModel:ProductViewModel? = null

    var selectedCategoryName: String? = null
    var defaultSelectedTabPosition = 0
    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener{ backPress() }
        tvActionBarHeading.text = getString(R.string.profile_str_37)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "UserProfile" + "_" + getString(R.string.profile_str_37),
            getString(R.string.profile_str_37),
            "")
        headBarBlur?.visibility = View.GONE
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        if (arguments != null){
            if (requireArguments().containsKey(Constant.defaultContentId)){
                selectedCategoryName = requireArguments().getString(Constant.defaultContentId).toString()

                setLog(TAG, "initializeComponent: selectedBucketId:${selectedCategoryName}")
            }

        }

        getAllProduct()
        getUserOrders()
        //setUpViewModel()
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && activity != null) {
            (activity as BaseActivity).showBottomNavigationBar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_earn_coin_all_tab_fragement, container, false)
    }

    private fun getAllProduct(){
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {
            productViewModel?.getProductList(requireContext())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog("aohgahl", "\n"+Gson().toJson(it?.data) + "\n")
                                SharedPrefHelper?.getInstance()?.saveObjectProductList(PRODUCT_LIST, it?.data)
                                productViewModel?.getProductCategoryList(requireContext())?.observe(this,
                                    Observer {
                                        when(it.status){
                                            Status.SUCCESS->{
                                                setProgressBarVisible(false)
                                                if (it?.data != null && !it?.data.smartCollections.isNullOrEmpty()){
                                                    setAllTabs(it?.data)
                                                }

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

    private fun setAllTabs(productCategoryRespModel: ProductCategoryRespModel) {
        fragmentList = ArrayList()
        fragmentName = ArrayList()
        fragmentList.add(RedeemCoinFragment(null))
        fragmentName.add("ALL")

        productCategoryRespModel.smartCollections?.forEachIndexed { index, smartCollection ->
            if (smartCollection?.id != 122785038436 && smartCollection?.id != 122784940132){
                fragmentList.add(RedeemCoinFragment(smartCollection))
                fragmentName.add(smartCollection?.title!!)
                if(!TextUtils.isEmpty(selectedCategoryName)&&selectedCategoryName?.contains(smartCollection?.title!!,true)!!){
                    defaultSelectedTabPosition=index+1

                }
            }
        }

        //fragmentList.add(PurchaseFragment())
        //fragmentName.add("Purchases")
        viewPagerSetUp()
        clHeader.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
    }

    private fun viewPagerSetUp() {
        vpTransactions?.adapter = TransactionPagerAdapter(activity, fragmentList, fragmentName)

        TabLayoutMediator(
            tabs, vpTransactions
        ) { tab, position ->
            tab.text = fragmentName.get(position).lowercase().replaceFirstChar(Char::titlecase)
        }.attach()
        tabs.addOnTabSelectedListener(this)

        CommonUtils.setLog("viewPagerSetUp","viewPagerSetUp defaultSelectedTabPosition:${defaultSelectedTabPosition}")


        tabs?.getTabAt(defaultSelectedTabPosition)?.select()

        vpTransactions?.registerOnPageChangeCallback(pageChangeCallback)
        vpTransactions.isUserInputEnabled = false


        vpTransactions?.offscreenPageLimit=1
    }

    class TransactionPagerAdapter(
        fa: FragmentActivity?,
        val mFragments: ArrayList<Fragment>,
        fragmentName: ArrayList<String>
    ) : FragmentStateAdapter(fa!!) {

        override fun getItemCount(): Int {
            return mFragments.size //Number of fragments displayed
        }

        @NonNull
        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold
        )
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }
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

    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        tab.view.children.find { it is TextView }?.let { tv ->
            (tv as TextView).post {
                tv.setTypeface(typeface, style)
            }
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
                                ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_my_order2, R.color.colorWhite))
                                rlMenu?.visibility = View.VISIBLE
                                rlMenu?.setOnClickListener {
                                    addFragment(R.id.fl_container,this, MyOrderListFragment(),false)
                                }
                            }
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
}