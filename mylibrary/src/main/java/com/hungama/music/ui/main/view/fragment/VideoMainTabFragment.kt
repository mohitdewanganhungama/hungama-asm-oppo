package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.ui.main.adapter.TabsViewPagerAdapter
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.fr_home.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.android.synthetic.main.row_itype_1001.*
import kotlinx.coroutines.*
import java.io.*


/**
 * A simple [Fragment] subclass.
 * Use the [VideoMainTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoMainTabFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    var videoViewModel: VideoViewModel? = null
    var fragmentList: ArrayList<Fragment> = ArrayList()
    var fragmentName: ArrayList<String> = ArrayList()


    var isTabSelected = false
    var defaultSelectedTabPosition = 0
    var tabName = ""
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""


    companion object {
        var mHomeModel: HomeModel? = null
        fun newInstance(mContext: Context?, bundle: Bundle): VideoMainTabFragment {
            var fragment = VideoMainTabFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_home, container, false)
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            if (isAdded){
                if (arguments != null){
                    if (arguments?.containsKey(Constant.isTabSelection)!!){
                        isTabSelected = arguments?.getBoolean(Constant.isTabSelection, false)!!
                    }
                    if (arguments?.containsKey(Constant.tabName)!!){
                        tabName = arguments?.getString(Constant.tabName)!!
                    }
                    if (arguments?.containsKey(Constant.EXTRA_IS_CATEGORY_PAGE)!!){
                        isCategoryPage = arguments?.getBoolean(Constant.EXTRA_IS_CATEGORY_PAGE)!!
                    }
                    if (arguments?.containsKey(Constant.EXTRA_CATEGORY_NAME)!!){
                        categoryName = arguments?.getString(Constant.EXTRA_CATEGORY_NAME)!!
                    }
                    if (arguments?.containsKey(Constant.EXTRA_CATEGORY_ID)!!){
                        categoryId = arguments?.getString(Constant.EXTRA_CATEGORY_ID)!!
                    }
                }

                ivSearch?.setOnClickListener(this@VideoMainTabFragment)
                ivUserPersonalImage?.setOnClickListener(this@VideoMainTabFragment)

                shimmerLayout?.visibility = View.VISIBLE
                shimmerLayoutTab?.visibility = View.VISIBLE
                shimmerLayoutTab?.startShimmer()
                shimmerLayout?.startShimmer()

                val videoModel=HungamaMusicApp.getInstance().getCacheBottomTab(Constant.CACHE_VIDEOS_PAGE)
                if(videoModel!=null){
                    setProgressBarVisible(false)
                    setData(videoModel!!)
                    setLog("VideoTabFragment", "setUpViewModel static call:${Constant.CACHE_VIDEOS_PAGE}")
                }else{
                    setLog("VideoTabFragment", "setUpViewModel API called")
                    setUpViewModel()
                }
//        getDataFromJSON()

//        viewPagerSetUp()
                //BaseFragment.headerHomeMain = view.findViewById(R.id.rlHeader)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded){
            val intent = Intent(Constant.VIDEO_MAIN_TAB_EVENT)
            intent.putExtra("EVENT", Constant.VIDEO_MAIN_TAB_HIDDEN_RESULT_CODE)
            intent.putExtra("hidden", hidden)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            if(!hidden && activity!=null){
                CommonUtils.setLog("videoMainTabFragment", "onHiddenChanged()-true-{$hidden}")
                (activity as BaseActivity).showBottomNavigationBar()
            }else{
                CommonUtils.setLog("videoMainTabFragment", "onHiddenChanged()-false-{$hidden}")
            }
        }
    }

    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        try {
            videoViewModel = ViewModelProvider(
                this
            ).get(VideoViewModel::class.java)


            if (ConnectionUtil(activity).isOnline) {
                val url= WSConstants.METHOD_VIDEO
                videoViewModel?.getWatchVidelList(requireContext(), url)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setData(it?.data)
                                    HungamaMusicApp.getInstance().setCacheBottomTab(Constant.CACHE_VIDEOS_PAGE, it?.data)
                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }catch (e:Exception){

        }

    }


    private fun setData(homeModel: HomeModel) {
        mHomeModel=homeModel
        baseMainScope.launch {
            if (homeModel != null && homeModel.data != null) {
                if (homeModel.data.head?.items != null) {
                    homeModel.data.head.items.forEachIndexed { index, headItemsItem ->
                        val bundle = Bundle()
                        if (isTabSelected && !TextUtils.isEmpty(tabName)
                            && (tabName.contains(headItemsItem?.page.toString(),true) ||  tabName.contains(headItemsItem?.title.toString(),true))){
                            defaultSelectedTabPosition = index
                        }
                        fragmentList.add(VideoTabFragment.newInstance(headItemsItem, bundle))
                        fragmentName.add(headItemsItem?.title!!)
                        setLog(TAG, "setData: isTabSelected:${isTabSelected} defaultSelectedTabPosition :${defaultSelectedTabPosition} tabName:${tabName} headItemsItem?.page:${headItemsItem?.page}")
                    }
                    if (!fragmentList.isNullOrEmpty() && fragmentList.size > defaultSelectedTabPosition && !homeModel.data.head.items.isNullOrEmpty() && homeModel.data.head.items.size > defaultSelectedTabPosition){
                        var bundle = fragmentList.get(defaultSelectedTabPosition).arguments

                        if (bundle == null) {
                            bundle = Bundle()
                        }else{
                            CommonUtils.setLog("deepLinkUrl", "VideoMainFragment-setData--bundle=$bundle")
                        }
                        bundle.putBoolean(Constant.EXTRA_IS_CATEGORY_PAGE, isCategoryPage)
                        bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                        bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                        CommonUtils.setLog("deepLinkUrl", "VideoMainFragment-setData--tabName=${homeModel.data.head.items.get(defaultSelectedTabPosition)?.page} && isCategory=$isCategoryPage && categoryName=$categoryName && categoryId=$categoryId")
                        fragmentList.set(defaultSelectedTabPosition, VideoTabFragment.newInstance(homeModel.data.head.items.get(defaultSelectedTabPosition), bundle))
                        fragmentName.set(defaultSelectedTabPosition, homeModel.data.head.items.get(defaultSelectedTabPosition)?.title!!)
                    }
                    if (isAdded){
                        viewPagerSetUp()

                    }
                }
            }
        }
    }


    private fun viewPagerSetUp() {
        baseMainScope.launch {
            /*vpTransactions?.adapter =
             TransactionPagerAdapter(activity, fragmentList, fragmentName)

         TabLayoutMediator(
             tabs, vpTransactions
         ) { tab, position ->
             tab.text = fragmentName.get(position)

         }.attach()


         vpTransactions?.registerOnPageChangeCallback(pageChangeCallback3)
         vpTransactions?.setCurrentItem(0, false)
         vpTransactions?.isUserInputEnabled = false
         vpTransactions?.offscreenPageLimit=1
         tabs?.addOnTabSelectedListener(this)
         //onTabSelected(tabs.getTabAt(0))
         tabs?.getTabAt(0)?.select()*/

            if (vpTransactions != null && isAdded){
                setupViewPager(vpTransactions, fragmentList, fragmentName)

                // If we dont use setupWithViewPager() method then
                // tabs are not used or shown when activity opened
                tabs.setupWithViewPager(vpTransactions)
                vpTransactions?.setCurrentItem(defaultSelectedTabPosition, false)
                //vpTransactions?.offscreenPageLimit=fragmentList.size
                vpTransactions.addOnPageChangeListener(pageChangeCallback)
                tabs?.addOnTabSelectedListener(this@VideoMainTabFragment)
                tabs?.getTabAt(defaultSelectedTabPosition)?.select()

                MainActivity.headerItemName= tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString()
                MainActivity.headerItemNameForBTab= MainActivity.clickedLastTopNav(tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString())
                MainActivity.headerItemPosition = defaultSelectedTabPosition

                setLog("checkCount", " Launch")

                    CommonUtils.PageViewEventTab("","","","",MainActivity.lastItemClickedTop+"_"+ MainActivity.headerItemNameForBTab + MainActivity.subHeaderItemName,
                        MainActivity.lastItemClicked + "_"+ tabs?.getTabAt(defaultSelectedTabPosition)?.text.toString(),
                        tabs.selectedTabPosition.toString(),"listing")

                MainActivity.subHeaderItemName = ""
                MainActivity.lastClickedDataSubTopNav.clear()

                shimmerLayoutTab?.stopShimmer()
                shimmerLayout?.stopShimmer()
                shimmerLayoutTab?.visibility = View.GONE
                shimmerLayout?.visibility = View.GONE

//                delay(2000)
//                vpTransactions?.offscreenPageLimit=fragmentList.size
            }
        }
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

    var pageChangeCallback = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
/*            MainActivity.headerItemName=fragmentName?.get(position)
            MainActivity.headerItemPosition=position*/


            setLog("onPageSelected", "Selected position:" + position)
            //vpTransactions?.adapter?.notifyItemChanged(position)
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    override fun onClick(v: View) {
        super.onClick(v)

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {

        MainActivity.lastItemClickedForBTab = MainActivity.lastItemClicked
        MainActivity.headerItemName= tab!!.text.toString()
        MainActivity.headerItemNameForBTab= MainActivity.clickedLastTopNav(tab!!.text.toString())
        MainActivity.headerItemPosition = tab.position

        setLog("checkCount", " TabSelected")

        CommonUtils.PageViewEventTab("","","","",MainActivity.lastItemClickedForBTab+"_"+ MainActivity.headerItemNameForBTab,
            MainActivity.lastItemClickedForBTab+"_" + tab.text.toString(),
            tabs.selectedTabPosition.toString(),"listing")
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (tab != null) {
                    CommonUtils.hapticVibration(requireContext(), tab.view,
                        HapticFeedbackConstants.CONTEXT_CLICK, false
                    )
                }
            }
        }catch (e:Exception){

        }
        baseMainScope.launch {
            val typeface = ResourcesCompat.getFont(
                requireContext(),
                R.font.sf_pro_text_bold
            )
            tab?.let {
                setStyleForTab(it, Typeface.BOLD, typeface)
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        baseMainScope.launch {
            val typeface = ResourcesCompat.getFont(
                requireContext(),
                R.font.sf_pro_text_medium
            )
            tab?.let {
                setStyleForTab(it, Typeface.NORMAL, typeface)
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        baseMainScope.launch {
            val typeface = ResourcesCompat.getFont(
                requireContext(),
                R.font.sf_pro_text_bold
            )
            tab?.let {
                setStyleForTab(it, Typeface.BOLD, typeface)
            }
        }
    }

    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        baseMainScope.launch {
            tab.view.children.find { it is TextView }?.let { tv ->
                (tv as TextView).post {
                    tv.setTypeface(typeface, style)
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        vpTransactions?.removeOnPageChangeListener(pageChangeCallback)
        baseServiceJob.cancel()
        baseIOServiceJob.cancel()
    }

    // This function is used to add items in arraylist and assign
    // the adapter to view pager
    private fun setupViewPager(
        viewpager: ViewPager,
        fragmentList: ArrayList<Fragment>,
        fragmentName: ArrayList<String>
    ) {
        val adapter = TabsViewPagerAdapter(childFragmentManager)
        adapter.addFragment(fragmentList, fragmentName)
        // setting adapter to view pager.
        viewpager.adapter = adapter
    }
}