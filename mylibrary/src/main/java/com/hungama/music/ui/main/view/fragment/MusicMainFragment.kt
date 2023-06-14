package com.hungama.music.ui.main.view.fragment

import android.content.Context
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
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.HomeModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.TabsViewPagerAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.MusicViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.moengage.inapp.MoEInAppHelper
import kotlinx.android.synthetic.main.fr_home.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.*
import java.io.*


/**
 * A simple [Fragment] subclass.
 * Use the [MusicMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MusicMainFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    var musicViewModel: MusicViewModel? = null
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
        fun newInstance(mContext: Context?, bundle: Bundle): MusicMainFragment {
            var fragment = MusicMainFragment()
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

    override fun onResume() {
        super.onResume()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
    }

    override fun onStart() {
        super.onStart()
        setLog("BaseAct", "onStart")
        MoEInAppHelper.getInstance().showInApp(requireActivity())
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


                ivSearch?.setOnClickListener(this@MusicMainFragment)
                ivUserPersonalImage?.setOnClickListener(this@MusicMainFragment)
//        getDataFromJSON()
                shimmerLayout?.visibility = View.VISIBLE
                shimmerLayoutTab?.visibility = View.VISIBLE
                shimmerLayoutTab?.startShimmer()
                shimmerLayout?.startShimmer()
                val musicModel=HungamaMusicApp.getInstance().getCacheBottomTab(Constant.CACHE_MUSIC_PAGE)
                if(musicModel!=null){
                    setProgressBarVisible(false)
                    setData(musicModel!!)
                    setLog("MusicMainFragment", "setUpViewModel static call:${Constant.CACHE_MUSIC_PAGE}")
                }else{

                    setUpViewModel()
                    setLog("MusicMainFragment", "setUpViewModel API call:${Constant.CACHE_MUSIC_PAGE}")
                }
                //BaseFragment.headerHomeMain = view.findViewById(R.id.rlHeader)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && activity!=null){
            CommonUtils.setLog("SwipablePlayerFragment", "MusicMainFragment-onHiddenChanged-hidden=$hidden - showBottomNavigationBar()")
            (activity as BaseActivity).showBottomNavigationBar()
        }
    }

    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        try {
            if (ConnectionUtil(activity).isOnline) {
                musicViewModel = ViewModelProvider(
                    this
                ).get(MusicViewModel::class.java)

                val url= WSConstants.METHOD_MUSIC
                musicViewModel?.getMusicList(requireContext(), url)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setData(it?.data!!)
                                HungamaMusicApp.getInstance().setCacheBottomTab(Constant.CACHE_MUSIC_PAGE, it?.data)
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
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }catch (e:Exception){

        }
    }
    private fun setData(homeModel: HomeModel) {
        mHomeModel=homeModel
        baseIOScope.launch {
            if (homeModel != null && homeModel.data != null) {
                if (homeModel.data.head?.items != null) {
                    homeModel.data.head.items.forEachIndexed { index, headItemsItem ->

                        val bundle = Bundle()
                        if (isTabSelected && !TextUtils.isEmpty(tabName)
                            && (headItemsItem?.page.toString()
                                .contains(tabName, true) || headItemsItem?.title.toString()
                                .contains(tabName, true))
                        ) {
/*                            defaultSelectedTabPosition = if (BaseActivity.isDeeplink)
                                0
                            else
                                index*/
                            defaultSelectedTabPosition = index
                        }
                        fragmentList.add(MusicChildFragment.newInstance(headItemsItem, bundle))
                        fragmentName.add(headItemsItem?.title!!)
                    }
                    if (!fragmentList.isNullOrEmpty() && fragmentList.size > defaultSelectedTabPosition && !homeModel.data.head.items.isNullOrEmpty() && homeModel.data.head.items.size > defaultSelectedTabPosition) {
                        var bundle = fragmentList.get(defaultSelectedTabPosition).arguments
                        if (bundle == null) {
                            bundle = Bundle()
                        }
                        bundle.putBoolean(Constant.EXTRA_IS_CATEGORY_PAGE, isCategoryPage)
                        bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                        bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                        CommonUtils.setLog(
                            "deepLinkUrl",
                            "MusicMainFragment-setData--tabName=${
                                homeModel.data.head.items.get(defaultSelectedTabPosition)?.page
                            } && isCategory=$isCategoryPage && categoryName=$categoryName && categoryId=$categoryId"
                        )
                        fragmentList.set(
                            defaultSelectedTabPosition,
                            MusicChildFragment.newInstance(
                                homeModel.data.head.items.get(defaultSelectedTabPosition), bundle
                            )
                        )
                        fragmentName.set(
                            defaultSelectedTabPosition,
                            homeModel.data.head.items.get(defaultSelectedTabPosition)?.title!!
                        )
                    }
                    withContext(Dispatchers.Main){
                        if (isAdded){
                            viewPagerSetUp()
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

        vpTransactions?.registerOnPageChangeCallback(pageChangeCallback2)
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
                tabs?.addOnTabSelectedListener(this@MusicMainFragment)
                tabs?.getTabAt(defaultSelectedTabPosition)?.select()

                MainActivity.headerItemName= tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString()
                MainActivity.headerItemNameForBTab = MainActivity.clickedLastTopNav(tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString())
                MainActivity.headerItemPosition = defaultSelectedTabPosition

                callPageViewEventForTab( MainActivity.lastItemClickedTop+"_"+ MainActivity.headerItemNameForBTab + MainActivity.subHeaderItemName,
                    MainActivity.lastItemClicked + "_"+ tabs?.getTabAt(defaultSelectedTabPosition)?.text.toString(),
                    defaultSelectedTabPosition.toString(),"listing")
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
            MainActivity.headerItemName=fragmentName.get(position)
            MainActivity.headerItemPosition=position


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
        setLog("onPageSelected", "Tab position:" + tab?.position)

        MainActivity.lastItemClickedForBTab= MainActivity.lastItemClicked
        MainActivity.headerItemName= tab!!.text.toString()
        MainActivity.headerItemNameForBTab= MainActivity.clickedLastTopNav(tab.text.toString())
        MainActivity.headerItemPosition = tab.position

        callPageViewEventForTab(MainActivity.lastItemClickedForBTab+"_"+ MainActivity.headerItemNameForBTab,
            MainActivity.lastItemClicked + "_"+ tab.text.toString(),
            tab.position.toString(),"listing")

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
        setLog("onDestroy", "MusicMainFragment")
        vpTransactions?.removeOnPageChangeListener(pageChangeCallback)
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