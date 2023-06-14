package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.R
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.HeadItemsItem
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.fr_home.*
import kotlinx.android.synthetic.main.fr_library_main.*
import kotlinx.android.synthetic.main.fr_library_main.appbar
import kotlinx.android.synthetic.main.fr_library_main.tabs
import kotlinx.android.synthetic.main.fr_library_main.vpTransactions
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverMainTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LibraryMainTabFragment : BaseFragment(), TabLayout.OnTabSelectedListener{
    var headItemsItem: HeadItemsItem?=null
    var fragmentChildList: ArrayList<Fragment> = ArrayList()
    var fragmentChildName: ArrayList<String> = ArrayList()
    var isTabSelected = false
    var defaultSelectedTabPosition = 0
    var tabName = ""
    var isDirectPlay = 0
    var defaultContentId = ""
    var isRadio = false
    var radioType = Constant.CONTENT_LIVE_RADIO
    var isSubTabSelected = false
    var subTabName = ""
    var defaultSelectedSubTabPosition = 0
    var mListener:onReloadListener?=null
    var childTabnName = ""
    var parentTabName = ""
    companion object{

        fun newInstance(mContext: Context?, bundle: Bundle): LibraryMainTabFragment {
            val fragment = LibraryMainTabFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_library_main, container, false)
    }

    override fun initializeComponent(view: View) {
        if (arguments != null){

            baseMainScope.launch {
                if (arguments != null) {
                    if (arguments?.containsKey(Constant.BUNDLE_KEY_HEADITEMSITEM)!!) {
                        headItemsItem = arguments?.getParcelable(Constant.BUNDLE_KEY_HEADITEMSITEM)
                    }
                }
            }
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
            if (arguments?.containsKey(Constant.EXTRA_IS_SUB_TAB_SELECTED)!!){
                isSubTabSelected = arguments?.getBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED)!!
            }
            if (arguments?.containsKey(Constant.subTabName)!!){
                subTabName = arguments?.getString(Constant.subTabName)!!
            }
        }

        ivSearch?.setOnClickListener(this@LibraryMainTabFragment)
        ivUserPersonalImage?.setOnClickListener(this@LibraryMainTabFragment)
        setUpViewModel()
        disableAppBarScrolling()
    }

    private fun disableAppBarScrolling() {
        val params = appbar.layoutParams as CoordinatorLayout.LayoutParams
        if (params.behavior == null)
            params.behavior = AppBarLayout.Behavior()
        val behaviour = params.behavior as AppBarLayout.Behavior
        behaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if(isAdded()&& activity != null){
            if (!hidden && activity != null) {
                (activity as BaseActivity).showBottomNavigationBar()
            }
            if (!hidden){
                val intent = Intent(Constant.LIBRARY_CONTENT_EVENT)
                intent.putExtra("EVENT", Constant.LIBRARY_CONTENT_RESULT_CODE)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            if(!hidden&&mListener!=null){
                mListener?.onRefresh()
                setLog(TAG, "onHiddenChanged: onRefresh called:")
            }
            setLog(TAG, "onHiddenChanged: LibraryMainTabFragment called:${hidden} mOnReloadListener:${mListener}")
        }

    }

    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        tabs.addTab(tabs.newTab().setText(getString(R.string.library_str_1)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.library_str_2)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.library_str_3)))
//        tabs.addTab(tabs.newTab().setText(getString(R.string.library_str_4)))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.sf_pro_text_bold
                )
                tab?.let {
                    setStyleForTab(it, Typeface.BOLD, typeface)
                }
                defaultSelectedSubTabPosition = 0
                defaultSelectedTabPosition = tab?.position!!
                getChildFragmentList(defaultSelectedTabPosition)

                viewPagerSetUp()
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
        setDefaultSelectedTab()
    }


    private fun viewPagerSetUp() {

        vpTransactions?.adapter = TransactionPagerAdapter(this, fragmentChildList, fragmentChildName)

        TabLayoutMediator(childTabs, vpTransactions) { tab, position ->
            tab.text = fragmentChildName.get(position)
        }.attach()


        vpTransactions?.setCurrentItem(defaultSelectedSubTabPosition, false)

        tabs?.getTabAt(defaultSelectedTabPosition)?.select()
        childTabs.addOnTabSelectedListener(this)
        childTabs?.getTabAt(defaultSelectedSubTabPosition)?.select()

        vpTransactions?.registerOnPageChangeCallback(pageChangeCallback)
        vpTransactions.isUserInputEnabled = false


        vpTransactions?.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT

        vpTransactions?.isUserInputEnabled = false
        setProgressBarVisible(false)
    }

    private fun getChildFragmentList(i: Int): java.util.ArrayList<String> {
        fragmentChildList = ArrayList()
        fragmentChildName = ArrayList()
        if (i == 0) {
            fragmentChildList.add(LibraryMusicAllFragment(tabName))
            fragmentChildName.add(getString(R.string.library_music_str_1))

            fragmentChildList.add(PlaylistFragment())
            fragmentChildName.add(getString(R.string.library_music_str_2))

            fragmentChildList.add(AlbumsFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_3))

            fragmentChildList.add(LibraryPodcastFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_4))

            fragmentChildList.add(ArtistFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_5))

            fragmentChildList.add(RadioFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_6))
        } else if (i == 1) {
            val fragement=VideoDownloadAllFragment()

            fragmentChildList.add(fragement)
            fragmentChildName.add(getString(R.string.library_maintab_str_7))

            fragmentChildList.add(VideoWatchlistFragment())
            fragmentChildName.add(getString(R.string.general_str_9))

            fragmentChildList.add(SavedVideoFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_9))

        } else if (i == 2) {
            fragmentChildList.add(TicketDetailsUnderPurchasesFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_10))

            fragmentChildList.add(LibraryRentedMovieFragment())
            fragmentChildName.add(getString(R.string.library_maintab_str_11))
        } else if (i == 3) {
            fragmentChildList.add(LocalDeviceSongsDetailFragment())
            fragmentChildName.add(getString(R.string.library_str_17))

        }
//        else {
//            fragmentChildList.add(LibraryBlankFragment())
//            fragmentChildName.add(getString(R.string.library_str_17))
//        }

        return fragmentChildName
    }

    class TransactionPagerAdapter(
        fa: LibraryMainTabFragment,
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
            setLog("onPageSelected", "Selected position:" + position)
            baseMainScope.launch {
                vpTransactions?.adapter?.notifyItemChanged(position)
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
            setStyleForTab(it, Typeface.BOLD, typeface)
        }

        if (parentTabName != tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString()
            || childTabnName != tab!!.text.toString()) {
            MainActivity.headerItemName = tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString()
            MainActivity.headerItemNameForBTab = MainActivity.clickedLastTopNav(tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString())
            MainActivity.headerItemPosition = defaultSelectedTabPosition
            MainActivity.subHeaderItemName = "_" + tab!!.text.toString()
            if (MainActivity.subHeaderItemNameForBTab.isNotEmpty())
            MainActivity.subHeaderItemNameForBTab = MainActivity.clickedLastSubTopNav(tab.text.toString())

            setLog(
                "checkCount", "" + MainActivity.lastItemClicked + " " +
                        MainActivity.lastItemClickedForBTab + " " +
                        MainActivity.headerItemName + " " +
                        MainActivity.headerItemNameForBTab + " " +
                        MainActivity.subHeaderItemNameForBTab + " " +
                        tabs.getTabAt(tabs.selectedTabPosition)!!.text.toString() + " " +
                        tab.text.toString())

            var subHead = ""
            subHead = if (MainActivity.subHeaderItemNameForBTab.isEmpty())
                "All"
            else
                MainActivity.subHeaderItemNameForBTab

            callPageViewEventForTab(MainActivity.lastItemClickedTop + "_" + MainActivity.headerItemNameForBTab + MainActivity.subHeaderItemNameForBTab,
                MainActivity.tempLastItemClicked + "_"+ tabs.getTabAt(tabs.selectedTabPosition)!!.text.toString() + "_" + tab.text.toString(),
                tabs.selectedTabPosition.toString(),"listing")
            MainActivity.lastItemClickedTop = MainActivity.clickedLastTop(MainActivity.lastItemClicked)
            MainActivity.lastItemClickedForBTab = MainActivity.lastItemClicked

            if (MainActivity.subHeaderItemNameForBTab.isEmpty())
            MainActivity.subHeaderItemNameForBTab = MainActivity.clickedLastSubTopNav(tab.text.toString())


        }
        parentTabName = tabs?.getTabAt(defaultSelectedTabPosition)!!.text.toString()
        childTabnName = tab.text.toString()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setDefaultSelectedTab(){
        if (isTabSelected && !TextUtils.isEmpty(tabName)){
            val tabCount = tabs.tabCount
            for(i in 0 until tabCount){
                if (tabs.getTabAt(i)?.text?.contains(tabName, true)!!){
                    defaultSelectedTabPosition = i
                }
            }
        }
        val mainTabList = getChildFragmentList(defaultSelectedTabPosition)
        if (!mainTabList.isNullOrEmpty()){
            mainTabList?.forEachIndexed { index, s ->
                val bundle = Bundle()
                if (index == 0){
                    bundle.putInt(Constant.isPlay, isDirectPlay)
                    bundle.putString(Constant.defaultContentId, defaultContentId)
                    bundle.putBoolean(Constant.isRadio, isRadio)
                    bundle.putInt(Constant.radioType, radioType)
                }
                if (isSubTabSelected && !TextUtils.isEmpty(subTabName)
                    && s.contains(subTabName, true)){
                    defaultSelectedSubTabPosition = index
                }
            }
        }
        if (!TextUtils.isEmpty(defaultContentId) && !TextUtils.isEmpty(subTabName) ){
            if (subTabName.equals("podcast")){
                val bundle = Bundle()
                bundle.putString("id",""+defaultContentId)
                bundle.putString("image", "")
                bundle.putString("playerType", Constant.PLAYER_PODCAST_AUDIO_TRACK)
                bundle.putBoolean("varient", false)
                val podcastDetailsFragment = PodcastDetailsFragment()
                podcastDetailsFragment.arguments = bundle
                addFragment(
                    R.id.fl_container,
                    this,
                    podcastDetailsFragment,
                    false
                )
            }else if (subTabName.equals("playlist")){
                val bundle = Bundle()
                bundle.putString("id",""+defaultContentId)
                bundle.putString("image", "")
                bundle.putString("playerType", Constant.MUSIC_PLAYER)
                bundle.putBoolean("varient", false)
                val playlistDetailFragment = MyPlaylistDetailFragment(1,
                    object : MyPlaylistDetailFragment.onBackPreesHendel {
                        override fun backPressItem(status: Boolean) {
                            setUpViewModel()
                        }

                    })
                playlistDetailFragment.arguments = bundle
                addFragment(
                    R.id.fl_container,
                    this,
                    playlistDetailFragment,
                    false
                )
            }
        }
        else if (defaultSelectedSubTabPosition == 0 && !TextUtils.isEmpty(subTabName)){
            if (subTabName.equals("favourite-song", true)){
                val favoritedSongsDetailFragment = FavoritedSongsDetailFragment()
                addFragment(
                    R.id.fl_container,
                    this,
                    favoritedSongsDetailFragment,
                    false
                )
            }else if (subTabName.equals("downloaded-songs", true)){
                val downloadedContentDetailFragment = DownloadedContentDetailFragment(ContentTypes.AUDIO.value)
                addFragment(
                    R.id.fl_container,
                    this,
                    downloadedContentDetailFragment,
                    false
                )
            }else if (subTabName.equals("downloaded-podcasts", true)){
                val downloadedContentDetailFragment = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
                addFragment(
                    R.id.fl_container,
                    this,
                    downloadedContentDetailFragment,
                    false
                )
            }
        }
        viewPagerSetUp()
    }

    fun addReloadListener(listener:onReloadListener){
        mListener=listener

        setLog(TAG, "mOnReloadListener: called onReloadListener:${listener} mOnReloadListener:${mListener}")
    }
    interface onReloadListener {
        fun onRefresh()
    }

}