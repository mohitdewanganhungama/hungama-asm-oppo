package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.TabsViewPagerAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.HomeViewModel
import com.hungama.music.ui.main.viewmodel.MusicViewModel
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.fr_home.*
import kotlinx.android.synthetic.main.fr_home.shimmerLayout
import kotlinx.android.synthetic.main.fr_main.*
import kotlinx.android.synthetic.main.header_back_transparent.view.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.*
import java.io.*


/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverMainTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscoverMainTabFragment : BaseFragment(), TabLayout.OnTabSelectedListener, BaseActivity.OnLocalBroadcastEventCallBack {
    var homeViewModel: HomeViewModel? = null
    var fragmentList: ArrayList<Fragment> = ArrayList()
    var fragmentName: ArrayList<String> = ArrayList()

    var isTabSelected = false
    var defaultSelectedTabPosition = 0
    var tabName = ""
    var isDirectPlay = 0
    var defaultContentId = ""
    var isRadio = false
    var radioType = Constant.CONTENT_LIVE_RADIO
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""
    var isSearchScreen = false
    var isDeeplinkVoiceSearchText = false
    var EXTRA_PAGE_DETAIL_NAME = ""
    var deeplinkVoiceSearchText = ""

    companion object {
        var mHomeModel: HomeModel? = null
        fun newInstance(mContext: Context?, bundle: Bundle): DiscoverMainTabFragment {
            var fragment=DiscoverMainTabFragment()
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
                    if (arguments?.containsKey(Constant.isPlay)!!){
                        isDirectPlay = arguments?.getInt(Constant.isPlay)!!
                    }
                    if (arguments?.containsKey(Constant.defaultContentId)!!){
                        defaultContentId = arguments?.getString(Constant.defaultContentId)!!
                    }
                    if (arguments?.containsKey(Constant.isRadio)!!){
                        isRadio = arguments?.getBoolean(Constant.isRadio)!!
                    }
                    if (arguments?.containsKey(Constant.radioType)!!){
                        radioType = arguments?.getInt(Constant.radioType)!!
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
                    if (arguments?.containsKey(Constant.isSearchScreen)!!){
                        isSearchScreen = arguments?.getBoolean(Constant.isSearchScreen)!!
                    }
                    if (arguments?.containsKey(Constant.isDeeplinkVoiceSearchText)!!){
                        isDeeplinkVoiceSearchText = arguments?.getBoolean(Constant.isDeeplinkVoiceSearchText)!!
                    }
                    if (arguments?.containsKey(Constant.EXTRA_PAGE_DETAIL_NAME)!!){
                        EXTRA_PAGE_DETAIL_NAME = arguments?.getString(Constant.EXTRA_PAGE_DETAIL_NAME)!!
                    }
                    if (arguments?.containsKey(Constant.deeplinkVoiceSearchText)!!){
                        deeplinkVoiceSearchText = arguments?.getString(Constant.deeplinkVoiceSearchText)!!
                    }
                }

                ivSearch?.setOnClickListener(this@DiscoverMainTabFragment)
                ivUserPersonalImage?.setOnClickListener(this@DiscoverMainTabFragment)

                shimmerLayout?.visibility = View.GONE
                shimmerLayoutTab?.visibility = View.GONE
                shimmerLayoutTab?.startShimmer()
                shimmerLayout?.startShimmer()

                val discoverHomeModel=HungamaMusicApp.getInstance().getCacheBottomTab(Constant.CACHE_DISCOVER_PAGE)

                if(discoverHomeModel!=null){
                    setProgressBarVisible(false)
                    setData(HungamaMusicApp.getInstance().getCacheBottomTab(Constant.CACHE_DISCOVER_PAGE)!!)
                    setLog("DiscoverMainTabFragment", "setUpViewModel static call:${Constant.CACHE_DISCOVER_PAGE}")
                    BucketParentAdapter.isVisible = true
                    if (discoverHomeModel.data?.body?.rows?.get(0)?.itype != 50)
                        requireActivity().resources?.getDimensionPixelSize(R.dimen.dimen_96)?.let { it1 -> rlMainDashboard.setPadding(0,it1,0,0) }
                }
                else{

                    setUpViewModel()
                    setLog("DiscoverMainTabFragment", "setUpViewModel online call:${Constant.CACHE_DISCOVER_PAGE}")
                }
            }
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded){
            val intent = Intent(Constant.DISCOVER_MAIN_TAB_EVENT)
            intent.putExtra("EVENT", Constant.DISCOVER_MAIN_TAB_HIDDEN_RESULT_CODE)
            intent.putExtra("hidden", hidden)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            if(!hidden && activity!=null){
                CommonUtils.setLog("discoverMainTabFragment", "onHiddenChanged()-true-{$hidden}")
                (activity as BaseActivity).showBottomNavigationBar()
            }else{
                CommonUtils.setLog("discoverMainTabFragment", "onHiddenChanged()-false-{$hidden}")
            }
        }
    }

    /**
     * initialise view model and setup-observer
     */

    fun removeMainData(id: String, items: MutableList<HeadItemsItem>){
        for (item in items.indices) {
            if (item >= items.size)
                break

            if (items[item].id == id) {
                items.removeAt(item)
            }
        }
    }

    private fun setUpViewModel() {
        try {
            if (ConnectionUtil(activity).isOnline) {
                homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
                var homeData: HomeModel? = null

                homeViewModel?.getHomeListDataLatest(requireContext(), WSConstants.METHOD_MUSIC)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                val items: MutableList<HeadItemsItem> = it.data?.data?.head?.items as MutableList<HeadItemsItem>
                                setLog("ITEMS LIST",items.toString())

                                removeMainData("1", items)
//                                removeMainData("2", items)
                                removeMainData("4", items)
                                removeMainData("5", items)
                                removeMainData("9", items)
                                removeMainData("3", items)

                                homeData = it?.data

                                homeViewModel?.getHomeBanner(requireContext(), WSConstants.HOME_BANNER)?.observe(this)
                                {
                                    when(it.status){
                                        Status.SUCCESS->{
                                            if (homeData != null){
                                                it?.data?.data?.body?.rows?.let { it1 ->
                                                    setLog("kghashfoasg", it1[0]?.items?.size.toString())

                                                    if (CommonUtils.isUserHasGoldSubscription()){
                                                        it1[0]?.items?.let { it2 -> removeBanner(it2) }
                                                    }

                                                    val lastData = it1[0]?.items?.get(0)
                                                    setLog("kghashfoasg", it1[0]?.items?.size.toString())
                                                    it1[0]?.items?.add((it1[0]?.items?.size!!), lastData)
                                                    setLog("kghashfoasg", it1[0]?.items?.size.toString())
                                                    homeData?.data?.body?.rows?.addAll(0, it1)
                                                }
                                                setData(homeData!!)
                                                HungamaMusicApp.getInstance().setCacheBottomTab(Constant.CACHE_DISCOVER_PAGE, homeData!!)
                                            }
                                        }

                                        Status.LOADING ->{
                                            setProgressBarVisible(true)
                                        }

                                        Status.ERROR ->{

                                            requireActivity().resources?.getDimensionPixelSize(R.dimen.dimen_96)?.let { it1 -> rlMainDashboard.setPadding(0,it1,0,0) }
                                            setData(homeData!!)
                                            HungamaMusicApp.getInstance().setCacheBottomTab(Constant.CACHE_DISCOVER_PAGE, homeData!!)
                                            setProgressBarVisible(false)
                                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                        }
                                    }
                                }

//                                setData(it?.data)
//                                HungamaMusicApp.getInstance().setCacheBottomTab(Constant.CACHE_DISCOVER_PAGE, it?.data!!)
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }catch (ee:Exception){

        }
    }

    fun removeBanner(data: ArrayList<BodyRowsItemsItem?>){
        for (i in data.indices){
            if (i>= data.size)
                break

            if (data[i]?.data?.user.equals("0")){
                data.removeAt(i)
                removeBanner(data)
            }
        }
    }


    private fun setData(homeModel: HomeModel) {

        mHomeModel=homeModel
        baseMainScope.launch {
            if (homeModel != null && homeModel.data != null) {
                if (homeModel.data.head?.items != null) {
                    homeModel.data.head.items.forEachIndexed { index, headItemsItem ->
                        val bundle = Bundle()
                        if (index == 0){
                            bundle.putInt(Constant.isPlay, isDirectPlay)
                            bundle.putString(Constant.defaultContentId, defaultContentId)
                            bundle.putBoolean(Constant.isRadio, isRadio)
                            bundle.putInt(Constant.radioType, radioType)
                            bundle.putBoolean(Constant.isSearchScreen, isSearchScreen)
                            bundle.putBoolean(Constant.isDeeplinkVoiceSearchText, isDeeplinkVoiceSearchText)
                            bundle.putString(Constant.deeplinkVoiceSearchText, deeplinkVoiceSearchText)
                            bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, EXTRA_PAGE_DETAIL_NAME)
                            //bundle.putInt(Constant.EXTRA_PAGE_INDEX, index)
                            //bundle.putParcelable(Constant.BUNDLE_KEY_BODYDATA, homeModel)

                        }

                        if (isTabSelected && !TextUtils.isEmpty(tabName)
                            && (headItemsItem?.page.toString().contains(tabName, true) ||  headItemsItem?.title.toString().contains(tabName, true))){
                            defaultSelectedTabPosition = index
                        }
                        val fragment = DiscoverTabFragment.newInstance(headItemsItem, bundle)
                        fragmentList.add(fragment)
                        fragmentName.add(headItemsItem?.title!!)
                    }
                    if (!fragmentList.isNullOrEmpty() && fragmentList.size > defaultSelectedTabPosition && !homeModel.data.head.items.isNullOrEmpty() && homeModel.data.head.items.size > defaultSelectedTabPosition){
                        var bundle = fragmentList.get(defaultSelectedTabPosition).arguments

                        if (bundle == null) {
                            bundle = Bundle()
                        }else{
                            CommonUtils.setLog("deepLinkUrl", "DiscoverMainFragment-setData--bundle=$bundle")
                        }
                        bundle.putBoolean(Constant.EXTRA_IS_CATEGORY_PAGE, isCategoryPage)
                        bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                        bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                        CommonUtils.setLog("deepLinkUrl", "DiscoverMainFragment-setData--tabName=${homeModel.data.head.items.get(defaultSelectedTabPosition)?.page} && isCategory=$isCategoryPage && categoryName=$categoryName && categoryId=$categoryId")
                        fragmentList.set(defaultSelectedTabPosition, DiscoverTabFragment.newInstance(homeModel.data.head.items.get(defaultSelectedTabPosition), bundle))
                        fragmentName.set(defaultSelectedTabPosition, homeModel.data.head.items.get(defaultSelectedTabPosition)?.title!!)
                    }
                    withContext(Dispatchers.Main){
                        if (isAdded){
                            viewPagerSetUp()
                            /*shimmerLayoutTab?.stopShimmer()
                            shimmerLayout?.stopShimmer()
                            shimmerLayoutTab?.visibility = View.GONE
                            shimmerLayout?.visibility = View.GONE*/
                        }

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


        vpTransactions?.registerOnPageChangeCallback(pageChangeCallback1)
        vpTransactions?.setCurrentItem(0, false)
        vpTransactions?.isUserInputEnabled = false


        vpTransactions?.offscreenPageLimit=1
        tabs?.addOnTabSelectedListener(this)
        //onTabSelected(tabs.getTabAt(0))
        tabs?.getTabAt(0)?.select()*/
            if (vpTransactions != null && isAdded){
                setupViewPager(vpTransactions!!, fragmentList, fragmentName)

                // If we dont use setupWithViewPager() method then
                // tabs are not used or shown when activity opened
                tabs.setupWithViewPager(vpTransactions)
                vpTransactions?.setCurrentItem(defaultSelectedTabPosition, false)
                //enableLayoutBehaviour()
                //vpTransactions?.offscreenPageLimit=fragmentList.size
                vpTransactions?.addOnPageChangeListener(pageChangeCallback)
                tabs?.addOnTabSelectedListener(this@DiscoverMainTabFragment)
                tabs?.getTabAt(defaultSelectedTabPosition)?.select()

                for (i in 0 until tabs.tabCount) {
                    val tab = (tabs.getChildAt(0) as ViewGroup).getChildAt(i)
                    val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                    p.setMargins(10, 0, 40, 0)
                    tab.requestLayout()
                }

                MainActivity.lastItemClickedForBTab = MainActivity.lastItemClicked
                MainActivity.headerItemName = tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString()
                MainActivity.headerItemNameForBTab = MainActivity.clickedLastTopNav(tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString())
                MainActivity.headerItemPosition = defaultSelectedTabPosition

                if (MainActivity.lastClickedData.size == 1)
                CommonUtils.PageViewEventTab("","","","", "AppLaunch",
                    MainActivity.lastItemClicked + "_"+ tabs?.getTabAt(defaultSelectedTabPosition)?.text.toString(),
                    tabs.selectedTabPosition.toString(), "listing")
                else
                    CommonUtils.PageViewEventTab("","","","",
                        MainActivity.lastItemClickedTop +"_"+ MainActivity.headerItemNameForBTab + MainActivity.subHeaderItemName,
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



    var pageChangeCallback = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {

            setLog("onPageSelected", "Selected position:" + position)
            //vpTransactions?.adapter?.notifyItemChanged(position)
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        MainActivity.lastItemClickedForBTab= MainActivity.lastItemClicked
        MainActivity.headerItemName= tab!!.text.toString()
        MainActivity.headerItemNameForBTab= MainActivity.clickedLastTopNav(tab!!.text.toString())
        MainActivity.headerItemPosition = tab.position
        BucketParentAdapter.isVisible = tab.position <= 0

        setLog("alkghlasdhgf", " " +tab.position.toString())

/*        if(tab.position==0){
            headerHome.setBackgroundColor(resources.getColor(R.color.transparent))
        }else
            headerHome.setBackgroundColor(resources.getColor(R.color.colorWhite1))*/

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (tab != null) {
                    CommonUtils.hapticVibration(requireContext(), tab.view,
                        HapticFeedbackConstants.CONTEXT_CLICK, false
                    )
                }
            }
        }
        catch (e:Exception){

        }
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    val typeface = ResourcesCompat.getFont(
                        requireContext(),
                        R.font.sf_pro_text_bold
                    )
                    tab?.let {
                        setStyleForTab(it, Typeface.BOLD, typeface)
                    }
                }
            }catch (e:Exception){

            }


        }

        setLog("alhlghal", MainActivity.lastItemClicked + " " +
                MainActivity.tempLastItemClicked + " " + MainActivity.headerItemNameForBTab)

            CommonUtils.PageViewEventTab("","","","",MainActivity.lastItemClickedForBTab+"_"+ MainActivity.headerItemNameForBTab,
                MainActivity.lastItemClickedForBTab+"_" + tab.text.toString(),
                tabs.selectedTabPosition.toString(), "listing")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    val typeface = ResourcesCompat.getFont(
                        requireContext(),
                        R.font.sf_pro_text_medium
                    )
                    tab?.let {
                        setStyleForTab(it, Typeface.NORMAL, typeface)
                    }
                }
            }catch (e:Exception){

            }


        }

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    val typeface = ResourcesCompat.getFont(
                        requireContext(),
                        R.font.sf_pro_text_bold
                    )
                    tab?.let {
                        setStyleForTab(it, Typeface.BOLD, typeface)
                    }
                }
            }catch (e:Exception){

            }


        }
    }

    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    tab.view.children.find { it is TextView }?.let { tv ->
                        (tv as TextView).post {
                            tv.setTypeface(typeface, style)
                        }
                    }
                }
            }catch (e:Exception){

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
        setLog("onDestroy", "DiscoverMainTabFragment")
        vpTransactions?.removeOnPageChangeListener(pageChangeCallback)
        BucketParentAdapter.handler?.removeCallbacksAndMessages(null)
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

    class ScrollingFABBehavior(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<RelativeLayout?>(context, attrs) {
        private val toolbarHeight: Int
        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            fab: RelativeLayout,
            dependency: View
        ): Boolean {
            return dependency is AppBarLayout
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            fab: RelativeLayout,
            dependency: View
        ): Boolean {
            CoroutineScope(Dispatchers.Main).launch {
                if (dependency is AppBarLayout) {
                    //val lp = fab.getLayoutParams() as CoordinatorLayout.LayoutParams
                    //val fabBottomMargin = lp.bottomMargin
                    val distanceToScroll: Int = toolbarHeight
                    val ratio = (dependency.getY() + toolbarHeight.toFloat())/ toolbarHeight.toFloat()
                    setLog("scrollUpp", (distanceToScroll * ratio).toString())

                    for (index in 0 until fab.childCount) {
                        val nextChild = fab.getChildAt(index)
                        if (nextChild.id == R.id.fullBlur){
                            setLog("scrollUpp1", (-distanceToScroll * ratio).toInt().toString())
                            //nextChild.layoutParams.height = (-distanceToScroll * ratio).toInt()
                            if ((-distanceToScroll * ratio).toInt() == toolbarHeight){
                                setLog("scrollUpp2", (-distanceToScroll * ratio).toInt().toString())
                                if (nextChild.id == R.id.fullBlur){
                                    nextChild.visibility = View.GONE
                                }
                            }else if((-distanceToScroll * ratio).toInt() <= 0 || (-distanceToScroll * ratio).toInt() < toolbarHeight){
                                setLog("scrollUpp3", (-distanceToScroll * ratio).toInt().toString())
                                if (nextChild.id == R.id.fullBlur){
                                    nextChild.visibility = View.GONE
                                }
                            }
                        }
                        if (nextChild.id == R.id.fullBlur2){
                            setLog("scrollUpp4", (-distanceToScroll * ratio).toInt().toString())
                            //nextChild.layoutParams.height = (-distanceToScroll * ratio).toInt()
                            if ((-distanceToScroll * ratio).toInt() == 0){
                                setLog("scrollUpp5", (-distanceToScroll * ratio).toInt().toString())
                                if (nextChild.id == R.id.fullBlur2){
//                                    nextChild.visibility = View.GONE
                                }
                            }else if((-distanceToScroll * ratio).toInt() <= 0 || (-distanceToScroll * ratio).toInt() < toolbarHeight){
                                setLog("scrollUpp6", (-distanceToScroll * ratio).toInt().toString())
                                if (nextChild.id == R.id.fullBlur2){
//                                    nextChild.visibility = View.GONE
                                }
                            }
                        }
                    }
                    fab.setTranslationY(distanceToScroll * ratio)
                    //headerHome.setTranslationY(-distanceToScroll * ratio)
                }
            }

            return true
        }

        init {
            toolbarHeight = context?.resources?.getDimensionPixelSize(R.dimen.dimen_46)?.toInt()!!
        }
    }


    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {

            }
        }
    }




}