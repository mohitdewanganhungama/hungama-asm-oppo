package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.DownloadManager
import com.hungama.fetch2.AbstractFetchListener
import com.hungama.fetch2.Download
import com.hungama.fetch2.Error
import com.hungama.fetch2.FetchListener
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketChildAdapter
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.HomeViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.CONTENT_ARTIST_RADIO
import com.hungama.music.utils.Constant.CONTENT_LIVE_RADIO
import com.hungama.music.utils.Constant.CONTENT_MOOD_RADIO
import com.hungama.music.utils.Constant.CONTENT_ON_DEMAND_RADIO
import com.hungama.music.utils.Constant.CONTENT_RADIO
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import com.hungama.music.utils.Constant.PLAYER_RADIO
import com.hungama.music.utils.Constant.PLAY_CONTEXT_TYPE
import com.hungama.music.utils.Constant.SELECTED_TRACK_POSITION
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.eventanalytic.eventreporter.BannerClickedEvent
import com.hungama.music.ui.main.adapter.Itype50PagerAdapter
import com.hungama.music.ui.main.viewmodel.AlbumViewModel
import com.hungama.music.ui.main.viewmodel.MovieViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.PodcastViewModel
import com.hungama.music.ui.main.viewmodel.TVShowViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.moe.pushlibrary.MoEHelper
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.model.InAppData
import com.moengage.widgets.NudgeView
import kotlinx.android.synthetic.main.fr_main.*
import kotlinx.android.synthetic.main.fr_my_playlist_detail.playlistDetailroot
import kotlinx.android.synthetic.main.shimmer_layout.view1
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscoverTabFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View, BucketParentAdapter.OnMoreItemClick,
    BaseActivity.OnLocalBroadcastEventCallBack, InAppLifeCycleListener, BannerItemClick,
    OnUserSubscriptionUpdate, BaseActivity.OnDownloadQueueItemChanged,
    BaseActivity.OnDownloadVideoQueueItemChanged {

    var homeViewModel: HomeViewModel? = null
    var tempHomeData:HomeModel?=null
    var tempHomeDataMain:HomeModel?=null
    var bucketRespModel: HomeModel? = null
    private var storyUsersList: ArrayList<BodyDataItem>? = null
    private var bucketParentAdapter: BucketParentAdapter? = null
    private var parentPos: Int = 0
    var rowList: MutableList<RowsItem?>? = null
    var songsList = ArrayList<MusicModel>()
    var playlistSongList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    private lateinit var tracksViewModel: TracksContract.Presenter
    private val mainScope = CoroutineScope(Dispatchers.Main) + CoroutineName("DiscoverTabFragment")
    private val ioScope = CoroutineScope(Dispatchers.IO) + CoroutineName("DiscoverTabFragment")
    var artworkProminentColor = 0
    private var isLastDurationPlay=false
    var playableItem = RowsItem()
    var playableItemPosition = 0
    var CONTENT_TYPE = 0
    var headItemsItem: HeadItemsItem?=null
    var isDirectPlay = 0
    var defaultContentId = ""
    var isRadio = false
    var radioType = Constant.CONTENT_LIVE_RADIO
    var pageName = ""
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""
    var isSearchScreen = false
    var isDeeplinkVoiceSearchText = false
    var EXTRA_PAGE_DETAIL_NAME = ""
    var deeplinkVoiceSearchText = ""
    var isNudgeViewVisible = false
    var flag = true
    private var isOnlineTabSelected: Boolean? = null


    companion object {
        var updateBanner = 0
        val LAUNCH_STORY_DISPLAY_ACTIVITY = 101
        const val UPDATED_STORY_USER_LIST = "updatedStoryUserList"
        lateinit var discoverScrollChange : DiscoverScrollChange
        var completeVisiblePosition1 = 0
        var podcastRespModel: PlaylistDynamicModel? = null
        fun newInstance(mHeadItemsItem: HeadItemsItem?, bundle: Bundle, discoverScrollChange1 :DiscoverScrollChange): DiscoverTabFragment {
            val fragment = DiscoverTabFragment()
            bundle.putParcelable(Constant.BUNDLE_KEY_HEADITEMSITEM, mHeadItemsItem)
            fragment.arguments = bundle
            discoverScrollChange = discoverScrollChange1
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fr_main, container, false)
    }

    override fun initializeComponent(view: View) {
        setLog("onResume", "DiscoverTabFragment-initializeComponent-title-${headItemsItem?.title}")
        baseMainScope.launch {
            if (arguments!=null){
                if(arguments?.containsKey(Constant.BUNDLE_KEY_HEADITEMSITEM)!!){
                    headItemsItem=arguments?.getParcelable(Constant.BUNDLE_KEY_HEADITEMSITEM)
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

                if(arguments?.containsKey(Constant.ONLINE_TAB_SELECTED)!!){
                    isOnlineTabSelected = arguments?.getBoolean(Constant.ONLINE_TAB_SELECTED)!!

                }
            }
            if (isDirectPlay == 1 && isRadio && (radioType == CONTENT_RADIO || radioType == CONTENT_LIVE_RADIO)){
                getPlayableContentUrl(defaultContentId)
            }

            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@DiscoverTabFragment)

            shimmerLayout?.visibility = View.VISIBLE
            shimmerLayout?.startShimmer()

            tempHomeData = HungamaMusicApp.getInstance().getCacheBottomTab("${Constant.Bottom_NAV_DISCOVER}_${headItemsItem?.page}")
            if(tempHomeData!=null){
                setProgressBarVisible(false)
                setData(CommonUtils.checkProUserBucket(tempHomeData),true)
                tempHomeDataMain = CommonUtils.checkProUserBucket(tempHomeData)
                setLog("DiscoverTabFragment", "setUpViewModel static call:${Constant.Bottom_NAV_DISCOVER}_${headItemsItem?.page}")
//                dailyDoseAPICall(DiscoverMainTabFragment.mHomeModel)
                if(headItemsItem?.page?.contains("home") == true && headItemsItem?.title?.contains("All") == true) {
                    flag =false
//                    viewHero.show()
                }else{
                    view1.show()         //story view
//                    viewHero.hide()
                }

            }else{
                setLog("DiscoverTabFragment", "setUpViewModel API called")
                setUpViewModel()
                if(flag) {
                    shimmerLayout?.visibility = View.VISIBLE
//                    viewHero.hide()
                    shimmerLayout?.startShimmer()
                }
            }

            setLocalBroadcast()

            setViewBottomSpacing()

            if (headItemsItem != null){
                pageName = headItemsItem?.page.toString()
            }

            setLog("deepLinkUrl", "DiscoverChildFragment-initializeComponent--tabName=${headItemsItem?.page} && isCategory=$isCategoryPage && categoryName=$categoryName && categoryId=$categoryId")
            if (isCategoryPage && !TextUtils.isEmpty(categoryName) && !TextUtils.isEmpty(categoryId)){
                val bundle = Bundle()
                bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                val categoryDetailFragment = CategoryDetailFragment(categoryId, 0)
                categoryDetailFragment.arguments = bundle
                addFragment(R.id.fl_container, this@DiscoverTabFragment, categoryDetailFragment, false)
            }else if (isSearchScreen){
                val bundle = Bundle()
                bundle.putBoolean(Constant.isDeeplinkVoiceSearchText, isDeeplinkVoiceSearchText)
                bundle.putString(Constant.deeplinkVoiceSearchText, deeplinkVoiceSearchText)
                bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, EXTRA_PAGE_DETAIL_NAME)
                val searchAllTabFragment = SearchAllTabFragment()
                searchAllTabFragment.arguments = bundle
                addFragment(R.id.fl_container, this@DiscoverTabFragment, searchAllTabFragment, false)
            }
            MoEInAppHelper.getInstance().addInAppLifeCycleListener(this@DiscoverTabFragment)
            // setLog("MoengageNudgeView", "DiscoverTabFragment-initializeComponent-addInAppLifeCycleListener")
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && activity!=null){
            (activity as BaseActivity).showBottomNavigationBar()
            //exoPlayerRecyclerView?.playVideo(true)
            bucketParentAdapter?.playOriginalPlayer()
            setLog("discoverFragment", "onHiddenChanged()-true-{$hidden}")
        }else{
            setLog("discoverFragment", "onHiddenChanged()-false-{$hidden}")
            //exoPlayerRecyclerView?.onPausePlayer()
            bucketParentAdapter?.pauseOriginalPlayer()
            if (Itype50PagerAdapter.callPlayerList() != null){
                Itype50PagerAdapter.callPlayerList()?.pause()
            }
        }
    }

    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        try {
            if (ConnectionUtil(activity).isOnline) {
                homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

                var url=""
                setLog(TAG, "setUpViewModel: $headItemsItem")
                if(headItemsItem!=null&&!TextUtils.isEmpty(headItemsItem?.page)){
                    url= WSConstants.HOME_BANNER
                    setLog(TAG, "setUpViewModel url: $url")
                }

                if (headItemsItem != null && !TextUtils.isEmpty(headItemsItem?.page)) {
                    url = WSConstants.METHOD_DETAIL_PAGE + headItemsItem?.page
                    setLog(TAG, "setUpViewModel url: $url")
                }

                homeViewModel?.getHomeListDataLatest(requireContext(), url)?.observe(this)
                {
                    when(it.status){
                        Status.SUCCESS->{
//                                HungamaMusicApp.getInstance().setCacheBottomTab("${Constant.Bottom_NAV_DISCOVER}_${headItemsItem?.page}", it?.data!!)
                            if(MainActivity.headerItemPosition==0 && WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                                setLog(TAG, "dailyDoseAPICall: data position:${MainActivity.headerItemPosition}")
//                                    dailyDoseAPICall(it?.data)
                            } else {
                                setLog(
                                    TAG,
                                    "dailyDoseAPICall: not called position:${MainActivity.headerItemPosition} "
                                )
                                setProgressBarVisible(false)
                                setData(CommonUtils.checkProUserBucket(it?.data), false)
                                tempHomeDataMain = CommonUtils.checkProUserBucket(it?.data)
                                setLog("HomeBannerData", " Home data")

                            }
                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                }

            } else {
                val messageModel = MessageModel(
                    getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true
                )
                CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","setUpViewModel")
            }
        } catch (e: Exception) {

        }

    }


    override fun onStart() {
        super.onStart()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
    }
    override fun onResume() {
        super.onResume()
        setLog("onResume", "DiscoverTabFragment-onResume-title-${headItemsItem?.title}")
        if (activity != null) {
            setLocalBroadcast()
            MoEInAppHelper.getInstance().showInApp(requireActivity())
        }

        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            fetchMusicDownloadListener,
            true,
            false
        )

        BucketParentAdapter.isVisible = true
    }

    fun nudgeViewSetUp() {

        setLog(
            TAG,
            "MainActivity.lastItemClicked:${MainActivity.lastBottomItemPosClicked} MainActivity.headItemsItem:${MainActivity.headerItemPosition}"
        )
        // get instance of the view
        val nudge = view?.findViewById<NudgeView>(R.id.nudge)
//        nudge?.bringToFront()
// initialize
        activity?.let { nudge?.initialiseNudgeView(it) }

//        MoEInAppHelper.getInstance().resetInAppContext()

        setLog(
            TAG,
            "nudgeViewSetUp: isVisible:${nudge?.isVisible} height:${nudge?.rootView?.height} width:${nudge?.rootView?.width}"
        )

    }

    fun removeBrandHubForGold(data: ArrayList<BodyRowsItemsItem?>) {
        for (i in data) {
            if (i!!.data!!.isBrandHub) {
                data.remove(i)
                removeBrandHubForGold(data)
                break
            }
        }
    }

    var lastVisiableItem: Int? = -1
    var mLayoutManager: LinearLayoutManager? = null
    private fun setData(it: HomeModel, isAdsAdded: Boolean) {
        if (BaseActivity.getIsGoldUser()) {
            val thisData = it.data?.body?.rows?.get(0)?.items
            if (thisData != null) {
                removeBrandHubForGold(thisData)
            }
        }
        bucketRespModel = it
        baseIOScope.launch {
            nudgeViewSetUp()
        }
        baseMainScope.launch {
            if (isAdded && context != null && bucketRespModel != null && bucketRespModel?.data?.body != null) {

                setLog(
                    TAG,
                    "setMoengageData 1 setData:setMoengageData setData: before bucketRespModel size :${bucketRespModel?.data?.body?.rows?.size!!} page : ${
                        HungamaMusicApp.getInstance()
                            .getCacheAdsTab("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")
                    }"
                )

                if (HungamaMusicApp.getInstance()
                        .getCacheAdsTab("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")
                        ?.isNullOrBlank()!!
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
//                        delay(3000)
                        runBlocking {
                            bucketRespModel = async {
                                setAdsData(bucketRespModel!!, headItemsItem!!)
                            }.await()
                            setLog(TAG, "setMoengageData 2 getCacheAdsTab  setAdsData called")
                            bucketRespModel = async {
                                addMoengageSelfHandleInAppData(bucketRespModel!!, headItemsItem!!)
                            }.await()
                            setLog(
                                TAG,
                                "setMoengageData 3 getCacheAdsTab  setAdsData addMoengageSelfHandleInAppData called"
                            )

                            if (rvRecentHistory != null) {
                                rvRecentHistory.post(Runnable {
                                    if (bucketParentAdapter != null)
                                        bucketParentAdapter!!.notifyItemRangeChanged(
                                            0,
                                            bucketRespModel?.data?.body?.rows?.size!! - 1
                                        )
                                })
                            }
                        }
                    }
                }

                setLog(
                    TAG,
                    "setMoengageData 4 setData: after bucketRespModel size :${bucketRespModel?.data?.body?.rows?.size!!}"
                )

                var varient = Constant.ORIENTATION_VERTICAL
                if (bucketRespModel?.data?.body?.rows?.size!! > 1) {
                    varient = Constant.ORIENTATION_HORIZONTAL
                }
                bucketParentAdapter = BucketParentAdapter(
                    bucketRespModel?.data?.body?.rows!!,
                    requireContext(),
                    this@DiscoverTabFragment,
                    this@DiscoverTabFragment,
                    Constant.DISCOVER_TAB,
                    headItemsItem,
                    varient,
                    this@DiscoverTabFragment
                )

                mLayoutManager = LinearLayoutManager(
                    activity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                rvRecentHistory?.layoutManager = mLayoutManager
                rvRecentHistory?.isSaveEnabled = true
                rvRecentHistory?.setItemViewCacheSize(30)

                //rvMain?.applyTopBarAndBottomBarInsets(requireContext())
                rvRecentHistory?.adapter = bucketParentAdapter
                rowList = (bucketRespModel?.data?.body?.rows as MutableList<RowsItem?>?)

                try {
                    rowList?.let { it1 -> if (it1.isNotEmpty()) bucketParentAdapter?.addData(it1) }
                }
                catch (e : Exception){
                    setLog("printException", e.message.toString())
                }


                rvRecentHistory?.visibility=View.VISIBLE
                rvRecentHistory?.invalidate()
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE

                rvRecentHistory?.setHasFixedSize(true)

                val lastVisibleAdsMap: HashMap<Int, Int> = HashMap()
                var prevCenterPos = -1
                var isLaunch = true
                rvRecentHistory?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        setLog(
                            "DiscoverTabFragment",
                            "DiscoverTabFragment-setData-onScrolled-findFirstCompletelyVisibleItemPosition" + mLayoutManager?.findFirstCompletelyVisibleItemPosition()
                        )
                        val completeVisiblePosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!
                        val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                        val lastVisiable: Int = mLayoutManager?.findLastVisibleItemPosition()!!
                        val fCompleteVisible = mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!
//                        if (MainActivity.headerItemPosition == 0) {

                        discoverScrollChange.onScroll(completeVisiblePosition)
                        completeVisiblePosition1 = completeVisiblePosition
//                        }

                        BucketParentAdapter.isVisible = completeVisiblePosition <= 0
                        setLog(
                            "DiscoverTabFragmentScroll",
                            " $completeVisiblePosition $firstVisiable $fCompleteVisible")

                        BucketParentAdapter.isVisible = completeVisiblePosition <= 0

                        if (!rowList.isNullOrEmpty() && rowList?.size!! > completeVisiblePosition && completeVisiblePosition >= 0) {
                            setLog(
                                "DiscoverTabFragment",
                                "DiscoverTabFragment-setData-onScrolled-rowList?.get(completeVisiblePosition)?.itype-" + rowList?.get(
                                    completeVisiblePosition
                                )?.itype
                            )
                            if (rowList?.get(completeVisiblePosition)?.itype == BucketChildAdapter.ROW_ITYPE_201) {
                                setLog(
                                    "DiscoverTabFragment",
                                    "DiscoverTabFragment-setData-onScrolled-visibleItem-" + rowList?.get(
                                        completeVisiblePosition
                                    )?.heading
                                )
                                if (!rowList?.get(completeVisiblePosition)?.items.isNullOrEmpty() && rowList?.get(
                                        completeVisiblePosition
                                    )?.items?.get(0)?.data?.isVisible == false
                                ) {
                                    setLog(
                                        "DiscoverTabFragment",
                                        "DiscoverTabFragment-setData-onScrolled-visibleItem-isVisible1-" + rowList?.get(
                                            completeVisiblePosition
                                        )?.items?.get(0)?.data?.isVisible
                                    )
                                    rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible =
                                        true
                                    setLog(
                                        "DiscoverTabFragment",
                                        "DiscoverTabFragment-setData-onScrolled-visibleItem-isVisible2-" + rowList?.get(
                                            completeVisiblePosition
                                        )?.items?.get(0)?.data?.isVisible
                                    )
                                    lastVisibleAdsMap.put(
                                        rowList?.get(completeVisiblePosition)?.itype!!,
                                        completeVisiblePosition
                                    )
                                    CoroutineScope(Dispatchers.Main).launch {
//                                        bucketParentAdapter?.notifyItemChanged(completeVisiblePosition)
                                    }

                                    setLog(
                                        "DiscoverTabFragment",
                                        "onScrolled: lastVisibleAdsMap1:${lastVisibleAdsMap}"
                                    )
                                }
                            }
                        }
                        setLog(
                            TAG,
                            "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                        )
//===================================================================================
                        //ORIGINAL Listing tab code start
                        baseMainScope.launch {
                            if (rvRecentHistory != null && (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE || recyclerView.scrollState == -1)) {
                                setLog(
                                    "DiscoverTabFragment",
                                    "DiscoverTabFragment-setData-onScrolled-centerPos-recyclerView.scrollState-${recyclerView.scrollState}"
                                )
                                val center = rvRecentHistory.width / 2
                                val rvSpace = rvRecentHistory.height / 2
                                val centerView = rvRecentHistory?.findChildViewUnder(
                                    center.toFloat(),
                                    rvSpace.toFloat()
                                )

                                var centerPos = 0
                                if (centerView != null) {
                                    centerPos = rvRecentHistory.getChildAdapterPosition(centerView)
                                    if (!rowList.isNullOrEmpty() &&
                                        rowList?.size!! > centerPos
                                        && centerPos >= 0
                                        && rowList?.get(centerPos)?.itype == BucketChildAdapter.ROW_ITYPE_47
                                    ) {
                                        if (prevCenterPos != centerPos) {
                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-centerView-$centerView--rvSpace-$rvSpace")
                                            withContext(Dispatchers.Main) {
                                                val tempPreviousPos = prevCenterPos
                                                //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-005-tempPreviousPos-$tempPreviousPos")
                                                if (!rowList.isNullOrEmpty() && rowList?.size!! > tempPreviousPos && tempPreviousPos >= 0)
                                                    prevCenterPos = centerPos
                                            }

                                            //playing view in the middle
                                            if (centerView != null) {
                                                withContext(Dispatchers.Main) {
                                                    val tempCenterPos = centerPos
                                                    //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-tempCenterPos-$tempCenterPos")
                                                    if (!rowList.isNullOrEmpty() && rowList?.size!! > tempCenterPos && tempCenterPos >= 0) {
                                                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-002-rowList?.get(tempCenterPos)?.itype-${rowList?.get(tempCenterPos)?.itype}")
                                                        if (rowList?.get(tempCenterPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-003-tempCenterPos-$tempCenterPos")
                                                            if (!rowList?.get(tempCenterPos)?.items.isNullOrEmpty()){
                                                                setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-tempCenterPos-$tempCenterPos-title-${rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                                rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible = true
                                                            }
                                                        }
                                                        bucketParentAdapter?.notifyItemChanged(
                                                            tempCenterPos
                                                        )
                                                    }
                                                }
                                            }

                                            if(centerView != null){
                                                withContext(Dispatchers.Main) {
                                                    val tempNextPos = centerPos+1
                                                    //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-tempNextPos-tempNextPos")
                                                    if (!rowList.isNullOrEmpty() && rowList?.size!! > tempNextPos && tempNextPos >= 0){
                                                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-002-rowList?.get(tempCenterPos)?.itype-${rowList?.get(tempNextPos)?.itype}")
                                                        if (rowList?.get(tempNextPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-003-tempNextPos-tempNextPos")
                                                            if (!rowList?.get(tempNextPos)?.items.isNullOrEmpty()){
                                                                setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-tempNextPos-$tempNextPos-title-${rowList?.get(tempNextPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                                rowList?.get(tempNextPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible = false
                                                            }
                                                        }
                                                        bucketParentAdapter?.notifyItemChanged(tempNextPos)
                                                    }
                                                }
                                            }

                                            setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-$centerPos")
                                        }
                                    }
                                }else{
                                    //if (prevCenterPos != centerPos) {
                                    //playing view in the middle
                                    withContext(Dispatchers.Main) {
                                        val tempCenterPos = 0
                                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-tempCenterPos-$tempCenterPos")
                                        if (!rowList.isNullOrEmpty() && rowList?.size!! > tempCenterPos && tempCenterPos >= 0){
                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-002-rowList?.get(tempCenterPos)?.itype-${rowList?.get(tempCenterPos)?.itype}")
                                            if (rowList?.get(tempCenterPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                                //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-003-tempCenterPos-$tempCenterPos")
                                                if (!rowList?.get(tempCenterPos)?.items.isNullOrEmpty() && rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible == false){
                                                    setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-004-title-${rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                    rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible = true
                                                    bucketParentAdapter?.notifyItemChanged(tempCenterPos)
                                                }
                                            }
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        val tempNextPos = centerPos+1
                                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-tempNextPos-tempNextPos")
                                        if (!rowList.isNullOrEmpty() && rowList?.size!! > tempNextPos && tempNextPos >= 0){
                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-002-rowList?.get(tempCenterPos)?.itype-${rowList?.get(tempNextPos)?.itype}")
                                            if (rowList?.get(tempNextPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                                //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-003-tempNextPos-tempNextPos")
                                                if (!rowList?.get(tempNextPos)?.items.isNullOrEmpty()){
                                                    setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-004-title-${rowList?.get(tempNextPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                    rowList?.get(tempNextPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible = false
                                                }
                                            }
                                            bucketParentAdapter?.notifyItemChanged(tempNextPos)
                                        }
                                    }
                                    //prevCenterPos = centerPos
                                    //}
                                }
                            }
                        }

                        //ORIGINAL Listing tab code end
//===================================================================================
                        if(firstVisiable!=lastVisiable && firstVisiable>0 && lastVisiable>0 && lastVisiable>firstVisiable) {
                            val fromBucket=rowList?.get(firstVisiable)?.heading
                            val toBucket=rowList?.get(lastVisiable)?.heading
                            val sourcePage= MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName



                            if(!fromBucket?.equals(toBucket,true)!! && lastVisiable>lastVisiableItem!!){
                                lastVisiableItem=lastVisiable
                                baseIOScope.launch {
                                    callPageScrolledEvent(
                                        sourcePage,
                                        "" + lastVisiable,
                                        fromBucket!!,
                                        toBucket!!
                                    )
                                }

                            }
                        }

                        setLog(TAG, "onScrolled: findFirstCompletelyVisibleItemPosition(rvMain) :${mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!}")
                    }
                })
            }
            //setLog("TestUserStory", "Sort-000")
            baseIOScope.launch {
                //bucketRespModel?.let { checkAllContentDownloadedOrNot(it) }
                if (!bucketRespModel?.data?.body?.rows.isNullOrEmpty() && !bucketRespModel?.data?.body?.rows?.get(0)?.items.isNullOrEmpty()){
                    val storyList = ArrayList<BodyDataItem>()
                    //setLog("TestUserStory", "Sort-0")
                    bucketRespModel?.data?.body?.rows?.get(0)?.items?.forEachIndexed { index, bodyRowsItemsItem ->
                        if(bodyRowsItemsItem?.data != null && !bodyRowsItemsItem.data?.isBrandHub!!){
                            bodyRowsItemsItem.data?.let { data -> storyList.add(data) }
                        }
                    }

                    //setLog("TestUserStory", "Sort-1")
                    val tempAllRemainingStoryUsersList:Deferred<ArrayList<BodyDataItem>> = baseIOScope.async {
                        setLog("TestUserStory", "Sort-1.1")
                        CommonUtils.getSortedUserStoryByRead(storyList, bucketRespModel?.data?.body?.rows?.get(0)?.items)
                    }
                    //setLog("TestUserStory", "Sort-3")
                    bucketParentAdapter?.updateList(tempAllRemainingStoryUsersList.await(), 0)
                    //setLog("TestUserStory", "Sort-4")

                }

            }
        }
    }


    private var mLastClickTime: Long = 0
    var greeting = ""
    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
            setLog("mLastClickTime", "return")
            return
        }
        setLog("mLastClickTime", "continue${parent.items!!.get(childPosition)?.data?.type!!}")
        mLastClickTime = SystemClock.elapsedRealtime()
        parentPos = parentPosition
        setEventModelDataAppLevel(parent.items?.get(childPosition)?.data?.id!!,parent.items?.get(childPosition)?.data?.title!!,parent?.heading!!)
        sendArtworkTappedEvent(parent,parentPosition,childPosition,headItemsItem)

        if(parent?.heading?.contains("Good Morning")!!){
            CommonUtils.getDayGreetings(requireContext())
            greeting = CommonUtils.greeting.value.toString()
        }

        if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("110",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("77777",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)) {
            if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true)){
                CONTENT_TYPE = Constant.CONTENT_MUSIC
            }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("110",true)){
                CONTENT_TYPE = Constant.CONTENT_PODCAST
            }else if(parent.items!!.get(childPosition)?.data?.type!!.equals("34",true) || parent.items!!.get(childPosition)?.data?.type!!.equals("77777",true)){
                CONTENT_TYPE = Constant.CONTENT_RADIO
            }
            playableItem = parent
            playableItemPosition = childPosition
            getPlayableContentUrl(parent.items?.get(childPosition)?.data?.id!!)
            setLog("getPlay_Detail"," position "+childPosition +" data: " +parent.items?.get(childPosition)?.data)

        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("33",true)){
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_MOOD_RADIO
            getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.moodid!!,
                Constant.CONTENT_MOOD_RADIO
            )

        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("35",true)){
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_ON_DEMAND_RADIO
            getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.id!!,
                Constant.CONTENT_ON_DEMAND_RADIO
            )
        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("36",true)){
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_ARTIST_RADIO
            getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.id!!,
                Constant.CONTENT_ARTIST_RADIO
            )
        }else {
            if (parent.itype == 50)
                onItemDetailPageRedirectionItype50(parent, parentPosition, childPosition, "")
            else
                onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
        }
    }

    override fun onCheckSatusplaylist(isClicked: Boolean, pos: Int, bodyData: BodyRowsItemsItem?) {
        bodyData?.data?.contentTypeId?.let { downlodingLogic(it,false,bodyData?.data) }
    }

    override fun bannerItemClick(
        isClicked: Boolean,
        position: Int,
        bodyData: BodyRowsItemsItem?
    ) {

        if (ConnectionUtil(context).isOnline) {
            val contentTypeId = bodyData?.data?.contentTypeId.toString()
            val secondaryCta = bodyData?.data?.secondaryCta.toString()
            EventButtonClickFunction(position, bodyData?.data)

            if (secondaryCta.contains(Constant.Favorited)) {
                setAddOrRemoveFavourite(bodyData?.data)
            } else if (secondaryCta.contains(Constant.Download)) {
                downlodingLogic(contentTypeId,true,bodyData?.data)
            } else if (secondaryCta.contains(Constant.Follow_Artist)) {
                setFollowUnFollow(bodyData?.data)
            } else if (secondaryCta.contains(Constant.Follow)) {
                setFollowUnFollow(bodyData?.data)
            } else if (secondaryCta.contains(Constant.Watchlist)) {
                Watchlist(bodyData?.data)
            } else if (secondaryCta.contains(Constant.Share)) {
                shareBanner(bodyData?.data)
            } else if (secondaryCta.contains(Constant.View_Plans)) {
                Constant.screen_name = "Home Screen"
                CommonUtils.openSubscriptionDialogPopup(
                    requireContext(),
                    PlanNames.SVOD.name,
                    "home",
                    true,
                    this,
                    "",
                    null,
                    Constant.drawer_default_buy_hungama_gold,"see_all"
                )
            }
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","bannerItemClick")
        }
    }



    fun EventButtonClickFunction(position: Int,data: BodyDataItem?) {
        val hashMapPageView = HashMap<String, String>()

        hashMapPageView[EventConstant.secondary_cta_option_selected] = ""
        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY]= data?.title.toString()
        hashMapPageView[EventConstant.AF_TYPE]= data?.type.toString()
        hashMapPageView[EventConstant.CONTENTTYPE_EPROPERTY]= data?.contentType.toString()
        hashMapPageView[EventConstant.CONTENTID_EPROPERTY]= data?.id.toString()
        hashMapPageView[EventConstant.deeplink]= data?.deeplink_url.toString()
        hashMapPageView[EventConstant.BUCKETPOSITION_EPROPERTY]= position.toString()
        hashMapPageView[EventConstant.GENRE_EPROPERTY]= data?.genre.toString()
        hashMapPageView[EventConstant.LANGUAGE_EPROPERTY]= data?.misc?.languages.toString()
        hashMapPageView[EventConstant.LYRICIST_EPROPERTY]= data?.misc?.lyricist.toString()
        hashMapPageView[EventConstant.MUSICDIRECTOR_EPROPERTY]= data?.misc?.musicdirectorf.toString()

        hashMapPageView[EventConstant.PLAYLIST_NAME_EPROPERTY]= data?.contentTypeId.toString()
        hashMapPageView[EventConstant.SINGER_EPROPERTY]= data?.misc?.singerf.toString()
        hashMapPageView[EventConstant.YEAROFRELEASE_EPROPERTY]= data?.deeplink_url.toString()
        hashMapPageView[EventConstant.SOURCE]= "Hero Banner"

        EventManager.getInstance().sendEvent(BannerClickedEvent(hashMapPageView))

    }


    fun downlodingLogic(
        contentTypeId: String,
        download: Boolean,
        data: BodyDataItem?
    ) {
        when(contentTypeId) {

            "109" -> {
                data?.id?.let {
                    if(it.isNotBlank())
                        setUpPodcastDetailListViewModel(it,data,download) }  //pocast all list
            }

            "55555" -> {
                data?.id?.let {  if(it.isNotBlank())  setUpPlaylistDetailListViewModel(it,data,download) }  ///all playlist
            }

            "107","97","96" -> {
                data?.id?.let {  if(it.isNotBlank())  setUpTVShowDetailListViewModel(it,data,download) }
            }

            "1" -> {
                data?.id?.let {  if(it.isNotBlank())  setUpAlbumDetailListViewModel(it,data,download) }
            }

            "21","22","98","110","111" ->   data?.id?.let {  if(it.isNotBlank()) callSongDownload(data,it)   }

            "4" -> {
                if(data?.primaryCta?.id == "Rent Now" && data?.secondaryCta == "Download")
                {
                    data.id?.let {  if(it.isNotBlank())  setUpMovieTrailerListViewModel(it) }
                }
                else{
                    data?.id?.let {  if(it.isNotBlank())  RentalMoiveLogic(data,it) }
                }
            }

        }
    }

    private fun setUpMovieTrailerListViewModel(selectedContentId:String) {
        try {
            if (isAdded && context != null){
                val movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
                setProgressBarVisible(true)
                if (ConnectionUtil(context).isOnline) {
                    movieViewModel?.getMovieDetail(requireContext(), selectedContentId)?.observe(this,
                        Observer {
                            when(it.status){
                                com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                    directPayment(it?.data!!)
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
                    val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                        MessageType.NEGATIVE, true)
                    CommonUtils.showToast(requireContext(), messageModel,"MovieV1Fragment","setUpMovieTrailerListViewModel")
                }
            }
        }catch (e:Exception){

        }
    }

    private fun directPayment(movieRespModel: PlaylistDynamicModel?){
        baseIOScope.launch {
            if (isAdded && context != null && movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                val dpm = DownloadPlayCheckModel()
                dpm.contentId = movieRespModel?.data?.head?.data?.id!!
                dpm.contentTitle = movieRespModel?.data?.head?.data?.title!!
                dpm.planName = movieRespModel?.data?.head?.data?.misc?.movierights.toString()
                dpm.isAudio = false
                dpm.isDownloadAction = false
                dpm.isDirectPaymentAction = true
//                dpm.queryParam = queryParam
                dpm.isShowSubscriptionPopup = true
                dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                dpm.restrictedDownload =
                    RestrictedDownload.valueOf(movieRespModel?.data?.head?.data?.misc?.restricted_download!!)
                var attributeCensorRating = ""
                if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                    attributeCensorRating = movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
                }

                if (CommonUtils.userCanDownloadContent(requireContext(), rlMain, dpm, this@DiscoverTabFragment,Constant.drawer_svod_download)) {
                    if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating)) {
//                        playAllMovies()
                    }
                }

            }
        }
    }

    private fun callSongDownload(playlistSongList: BodyDataItem?,id: String="") {
        val dpm = DownloadPlayCheckModel()
        dpm.contentId = playlistSongList?.id.toString()
        dpm.contentTitle = playlistSongList?.title.toString()
        dpm.planName = playlistSongList?.movierights.toString()
        dpm.isAudio = true
        dpm.isDownloadAction = true
        dpm.isShowSubscriptionPopup = true
        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
        dpm.restrictedDownload = RestrictedDownload.valueOf(0)
        if (CommonUtils.userCanDownloadContent(
                requireContext(),
                null,
                dpm, this@DiscoverTabFragment, Constant.drawer_restricted_download
            )
        ) {

            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
            var dq = DownloadQueue()
            //for (item in playlistSongList?.iterator()!!){

            dq = DownloadQueue()
            if (!TextUtils.isEmpty(playlistSongList?.id.toString())) {
                dq.contentId =
                    playlistSongList?.id.toString()
            }

            if (!TextUtils.isEmpty(playlistSongList?.title!!)) {
                dq.title =
                    playlistSongList?.title!!
            }

            if (!TextUtils.isEmpty(playlistSongList?.subTitle!!)) {
                dq.subTitle =
                    playlistSongList?.subTitle!!
            }

            if (!TextUtils.isEmpty(playlistSongList?.image!!)) {
                dq.image = playlistSongList?.image!!
            }

            if (!TextUtils.isEmpty(playlistSongList?.id!!)) {
                dq.parentId = playlistSongList?.id!!
            }
            if (!TextUtils.isEmpty(playlistSongList?.title!!)) {
                dq.pName = playlistSongList?.title
            }

            if (!TextUtils.isEmpty(playlistSongList?.subTitle!!)) {
                dq.pSubName = playlistSongList?.subTitle
            }

            if (!TextUtils.isEmpty(playlistSongList?.releasedate!!)) {
                dq.pReleaseDate =
                    playlistSongList?.releasedate
            }

            if (!TextUtils.isEmpty(playlistSongList?.image!!)) {
                dq.pImage = playlistSongList?.image
            }


            if (!TextUtils.isEmpty(playlistSongList?.misc?.movierights.toString()!!)) {
                dq.planName =
                    playlistSongList?.misc?.movierights.toString()
            }

            if (!TextUtils.isEmpty(playlistSongList?.misc?.f_playcount.toString()!!)) {
                dq.f_playcount =
                    playlistSongList?.misc?.f_playcount.toString()
            }

            if (!TextUtils.isEmpty(playlistSongList?.misc?.f_FavCount.toString()!!)) {
                dq.f_fav_count =
                    playlistSongList?.misc?.f_FavCount.toString()
            }

            dq.pType = DetailPages.CHART_DETAIL_PAGE.value
            if(playlistSongList.type?.contains("video") == true){
                dq.contentType = ContentTypes.VIDEO.value
            }else if(id.equals("110")){
                dq.contentType = ContentTypes.PODCAST.value

            }else dq.contentType = ContentTypes.AUDIO.value

            val eventModel =
                HungamaMusicApp.getInstance().getEventData(playlistSongList?.id.toString())
            dq.source = eventModel.sourceName

            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                ?.findByContentId(playlistSongList?.id!!.toString())
            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(playlistSongList?.id!!.toString())
            if ((!downloadQueue?.contentId.equals(
                    playlistSongList?.id!!.toString()
                ))
                && (!downloadedAudio?.contentId.equals(
                    playlistSongList?.id!!.toString()
                ))
            ) {
                downloadQueueList.add(dq)
            }
            // }
            //if (downloadQueueList.size > 0){
            (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                downloadQueueList,
                this@DiscoverTabFragment,
                fetchMusicDownloadListener,
                false,
                true
            )
        }
    }

    private fun RentalMoiveLogic(data: BodyDataItem?, selectedContentId: String) {
        val dpm = DownloadPlayCheckModel()
        dpm.contentId = data?.id?.toString()!!
        dpm.contentTitle = data?.title?.toString()!!
        dpm.planName = data?.misc?.movierights.toString()
        dpm.isAudio = false
        dpm.isDownloadAction = true
        dpm.isShowSubscriptionPopup = true
        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
        dpm.restrictedDownload = RestrictedDownload.valueOf(1)

        if (data != null) {
            var attributeCensorRating = ""
            if (!data?.misc?.attributeCensorRating.isNullOrEmpty()){
                attributeCensorRating = data?.misc?.attributeCensorRating?.get(0).toString()
            }
            if (CommonUtils.userCanDownloadContent(
                    requireContext(),
                    null,
                    dpm,
                    this@DiscoverTabFragment,Constant.drawer_svod_download
                )
            ) {
                if (!CommonUtils.checkUserCensorRating(
                        requireContext(),
                        attributeCensorRating
                    )
                ) {
                    setLog("onDwClick", "Clicked")
                    // self download
                    /*startDRMDownloadSong()
                    downloadIconStates(Download.STATE_QUEUED, ivDownload)
                    tvDownload?.text = getString(R.string.in_queue)*/
                    val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                    var dq = DownloadQueue()
                    //for (item in playlistSongList?.iterator()!!){

                    dq = DownloadQueue()
                    if (data != null) {
                        if (!TextUtils.isEmpty(data?.id!!)) {
                            dq.parentId =  data?.id!!
                            dq.contentId = data?.id!!
                        }
                        if (!TextUtils.isEmpty(data?.title!!)) {
                            dq.pName = data?.title
                            dq.title = data?.title
                        }

                        if (!TextUtils.isEmpty(data?.subTitle!!)) {
                            dq.pSubName = data?.subTitle
                            dq.subTitle = data?.subTitle
                        }

                        if (!TextUtils.isEmpty(data?.releasedate!!)) {
                            dq.pReleaseDate =
                                data?.releasedate
                        }

                        if (!TextUtils.isEmpty(data?.image!!)) {
                            dq.pImage = data?.image
                            dq.image = data?.image
                        }


                        if (!TextUtils.isEmpty(data?.misc?.movierights.toString()!!)) {
                            dq.planName = data?.misc?.movierights.toString()
                            dq.planType = CommonUtils.getContentPlanType(dq.planName)
                        }

                        dq.pType = DetailPages.MOVIE_DETAIL_PAGE.value
                        if (data.contentTypeId=="4") {
                            dq.contentType = ContentTypes.MOVIES.value
                        } else {
                            dq.contentType = ContentTypes.SHORT_FILMS.value
                        }

                        val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                        dq.source = eventModel.sourceName

                        val downloadQueue =
                            AppDatabase.getInstance()?.downloadQueue()?.findByContentId(data?.id!!.toString()
                            )
                        val downloadedAudio =
                            AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(
                                    data?.id!!.toString()
                                )
                        if ((!downloadQueue?.contentId.equals(data?.id!!.toString()))
                            && (!downloadedAudio?.contentId.equals(data?.id!!.toString()))
                        ) {
                            downloadQueueList.add(dq)
                        }

                        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                            downloadQueueList,
                            this@DiscoverTabFragment,
                            false,
                            true
                        )
                    }
                }
            }
        }
    }

    private fun setUpTVShowDetailListViewModel(selectedContentId: String, bodyDataItem: BodyDataItem,download:Boolean) {
        try {

            var tvShowViewModel = ViewModelProvider(this).get(TVShowViewModel::class.java)

            if (ConnectionUtil(context).isOnline) {
                tvShowViewModel?.getTVShowDetailList(requireContext(), selectedContentId,"")?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                var seasonsModel = it.data?.data?.body?.rows?.get(0)!!

                                if(download) {
                                    if (seasonsModel != null && seasonsModel.seasons != null && seasonsModel.seasons?.size!! > 0) {
                                        callTvSeriesListDownload(seasonsModel, bodyDataItem)

                                    }
                                }else {
                                    Constant.notify = true

                                }
                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(
                    getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true
                )
                CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","setUpTVShowDetailListViewModel")
            }
        } catch (e: Exception) {

        }
    }


    private fun setUpPodcastDetailListViewModel(selectedContentId: String, bodyDataItem: BodyDataItem,download:Boolean,flag_itration:Boolean=true) {
        var podcastListViewModel = ViewModelProvider(this).get(PodcastViewModel::class.java)
        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getPodcastDetailList(requireContext(),selectedContentId)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")

                                if(it!=null) {
                                    var isPodcastHaveSeason = false
                                    var seasonTrack: PlaylistModel.Data.Body.Row.Data.Misc.Track? =
                                        null
                                    try {
                                        if (!it?.data?.data?.body?.rows?.get(0)?.data?.misc?.tracks.isNullOrEmpty()) {
                                            it?.data?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.forEach {
                                                if (it != null && it?.data?.type == 111) {
                                                    seasonTrack = it
                                                    isPodcastHaveSeason = true
                                                    return@forEach
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {

                                    }

                                    if (isPodcastHaveSeason && seasonTrack != null) {
                                        getSeasonTrackList(seasonTrack?.data?.id!!,bodyDataItem,download)
                                    } else {
                                        try {
                                            playlistSongList = it?.data?.data?.body?.rows!!
                                            bodyDataItem.playlistSongList = playlistSongList
                                            if (download) {
                                                callPocastListDownload(it, bodyDataItem)
                                            }else {
                                                Constant.notify = true
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                /*playlistSongList = it?.data?.data?.body?.rows!!
                                if (playlistSongList.get(0).data.misc.tracks[0].data.type == 111 && flag_itration) {
                                    var local = playlistSongList.get(0).data.misc.tracks[0].data.id

                                    return@Observer
                                } else {

                                    bodyDataItem.playlistSongList = playlistSongList
                                    setLog("sarvesh",bodyDataItem.playlistSongList.toString())
                                    if (download) {
                                        callPocastListDownload(it, bodyDataItem,bodyDataItem.playlistSongList)
                                    }else {
                                        Constant.notify = true
                                    }
                                }
                            }
*/
                            }
                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","setUpPodcastDetailListViewModel")
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setUpAlbumDetailListViewModel(selectedContentId: String, bodyDataItem: BodyDataItem,download:Boolean) {
        try {
            if (ConnectionUtil(context).isOnline) {
                var albumListViewModel = ViewModelProvider(this).get(AlbumViewModel::class.java)

                albumListViewModel?.getAlbumDetailList(requireContext(), selectedContentId!!)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                playlistSongList = it?.data?.data?.body?.rows!!
                                bodyDataItem.playlistSongList = playlistSongList

                                if(download){
                                    callSongListDownload(it?.data,bodyDataItem)
                                }else {
                                    Constant.notify = true

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
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","setUpAlbumDetailListViewModel")
            }
        }catch (e:Exception){

        }

    }


    private fun setUpPlaylistDetailListViewModel(
        selectedContentId: String,
        bodyDataItem: BodyDataItem,download:Boolean
    ) {
        try {
            if (ConnectionUtil(activity).isOnline) {
                var playlistListViewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)

                playlistListViewModel?.getPlaylistDetailList(requireContext(), selectedContentId!!)
                    ?.observe(this,
                        Observer {
                            when (it.status) {
                                Status.SUCCESS -> {
                                    playlistSongList = it?.data?.data?.body?.rows!!
                                    bodyDataItem.playlistSongList = playlistSongList

                                    if(download) {
                                        callSongListDownload(it?.data,bodyDataItem)
                                    }else {
                                        Constant.notify = true
                                    }
                                }

                                Status.LOADING -> {
                                    setProgressBarVisible(false)
                                }

                                Status.ERROR -> {
                                    setProgressBarVisible(false)
                                    Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                                }
                            }
                        })
            }
        } catch (e: Exception) {

        }

    }

    fun callSongListDownload(
        playlistRespModel: PlaylistDynamicModel?,
        bodyDataItem: BodyDataItem
    )
    {
        baseIOScope.launch {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = playlistRespModel?.data?.head?.data?.id.toString()
            dpm.planName = PlanNames.NONE.name
            dpm.isAudio = true
            dpm.isDownloadAction = true
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_ALL_CONTENT
            dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
            if (CommonUtils.userCanDownloadContent(
                    requireContext(),
                    playlistDetailroot,
                    dpm,
                    this@DiscoverTabFragment, Constant.drawer_download_all
                )
            ) {
                val contentTypes: Array<Int> =
                    arrayOf(ContentTypes.AUDIO.value, ContentTypes.PODCAST.value)
                val allDownloadItemList = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getContentsByContentType(contentTypes)
                var existingQueueItemsCount = 0
                if (allDownloadItemList != null) {
                    existingQueueItemsCount = allDownloadItemList.size
                }

                val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                var dq = DownloadQueue()
                var count = 1
                val eventModel = HungamaMusicApp.getInstance().getEventData(playlistRespModel?.data?.head?.data?.id.toString())
                if (playlistSongList.isNotEmpty()){
                    for (item in playlistSongList?.iterator()!!) {

                        dq = DownloadQueue()

                        dq.contentId = item?.data?.id


                        if (!TextUtils.isEmpty(item?.data?.title)) {
                            dq.title = item?.data?.title!!
                        }

                        if (!TextUtils.isEmpty(item?.data?.subtitle!!)) {
                            dq.subTitle = item?.data?.subtitle!!
                        }

                        if (!TextUtils.isEmpty(item?.data?.image!!)) {
                            dq.image = item?.data?.image!!
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.id!!)) {
                            dq.parentId = playlistRespModel?.data?.head?.data?.id!!
                        }
                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.title!!)) {
                            dq.pName = playlistRespModel?.data?.head?.data?.title
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.subtitle!!)) {
                            dq.pSubName = playlistRespModel?.data?.head?.data?.subtitle
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.releasedate!!)) {
                            dq.pReleaseDate = playlistRespModel?.data?.head?.data?.releasedate
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.image!!)) {
                            dq.pImage = playlistRespModel?.data?.head?.data?.image
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.misc?.f_FavCount!!)) {
                            dq.f_fav_count = playlistRespModel?.data?.head?.data?.misc?.f_FavCount!!
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.misc?.f_playcount!!)) {
                            dq.f_playcount = playlistRespModel?.data?.head?.data?.misc?.f_playcount!!
                        }

                        dq.pType = DetailPages.CHART_DETAIL_PAGE.value
                        dq.contentType = ContentTypes.AUDIO.value
                        dq.downloadAll = 1
                        dq.source = eventModel.sourceName

                        if (!TextUtils.isEmpty(item?.data?.misc?.movierights.toString()!!)) {
                            dq.planName = item?.data?.misc?.movierights.toString()
                        }

                        if (existingQueueItemsCount > CommonUtils.getAvailableDownloadContentSize(requireContext())
                        ) {
                            break
                        }
                        existingQueueItemsCount += 1

                        if (!AppDatabase.getInstance()?.downloadQueue()
                                ?.findByContentId(item?.data?.id.toString())?.contentId.equals(item?.data?.id.toString())
                            && !AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(item?.data?.id.toString())?.contentId.equals(item?.data?.id.toString())
                        ) {
                            //if (count <= 3){
                            downloadQueueList.add(dq)
                            //}
                        }
                        count++
                    }
                }

                //if (downloadQueueList.size > 0){
                (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                    downloadQueueList,
                    this@DiscoverTabFragment,
                    fetchMusicDownloadListener,
                    false,
                    true
                )
                //}

                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        bodyDataItem.isDownloading ="1"
                    }
                }

            }
        }

    }

    fun callTvSeriesListDownload(
        data: PlaylistModel.Data.Body.Row?,
        tvShowDetailRespModel: BodyDataItem)
    {
        baseIOScope.launch {

            var seasonList = data?.seasons?.get(0)?.data?.misc?.tracks

            if (seasonList?.isNotEmpty() == true) {

                var attributeCensorRating = ""
                if (!seasonList.get(0).data?.misc?.attributeCensorRating.isNullOrEmpty()) {
                    attributeCensorRating =
                        seasonList.get(0).data?.misc?.attributeCensorRating?.get(
                            0
                        ).toString()
                }
                if (!CommonUtils.checkUserCensorRating(
                        requireContext(),
                        attributeCensorRating
                    )
                ) {
                    val dpm1 = DownloadPlayCheckModel()

                    dpm1.contentId = seasonList.get(0).data.id.toString()
                    dpm1.contentTitle = seasonList.get(0).data.name.toString()
                    dpm1.planName = seasonList.get(0).data?.misc?.movierights.toString()
                    dpm1.isAudio = false
                    dpm1.isDownloadAction = true
                    dpm1.isShowSubscriptionPopup = true
                    dpm1.clickAction = ClickAction.FOR_SINGLE_CONTENT
                    if (CommonUtils.userCanDownloadContent(
                            requireContext(),
                            null,
                            dpm1,
                            this@DiscoverTabFragment, Constant.drawer_svod_tvshow_episode
                        )) {
                        for (childPosition in seasonList?.iterator()!!) {

                            val dpm = DownloadPlayCheckModel()

                            dpm.contentId = childPosition.data.id.toString()
                            dpm.contentTitle = childPosition.data.name.toString()
                            dpm.planName =
                                childPosition.data?.misc?.movierights.toString()
                            dpm.isAudio = false
                            dpm.isDownloadAction = true
                            dpm.isShowSubscriptionPopup = true
                            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                            dpm.restrictedDownload = RestrictedDownload.valueOf(
                                childPosition.data?.misc?.restricted_download!!
                            )

                            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                            var dq = DownloadQueue()
                            //for (item in podcastEpisodeList?.iterator()!!){

                            dq = DownloadQueue()
                            if (!TextUtils.isEmpty(
                                    childPosition?.data?.id.toString()
                                )
                            ) {
                                dq.contentId =
                                    childPosition.data?.id.toString()
                            }

                            if (!TextUtils.isEmpty(
                                    childPosition.data?.name
                                )
                            ) {
                                dq.title = childPosition.data?.name
                            }

                            if (!TextUtils.isEmpty(
                                    childPosition?.data?.subTitle
                                )
                            ) {
                                dq.subTitle = childPosition.data?.subTitle
                            }

                            if (!TextUtils.isEmpty(childPosition?.data?.image!!)) {
                                dq.image = childPosition.data?.image!!
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel.id!!)) {
                                dq.parentId = tvShowDetailRespModel.id!!
                            }
                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.title!!)) {
                                dq.pName = tvShowDetailRespModel?.title
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.subTitle!!)) {
                                dq.pSubName = tvShowDetailRespModel?.subTitle
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.releasedate!!)) {
                                dq.pReleaseDate = tvShowDetailRespModel?.releasedate
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.image!!)) {
                                dq.pImage = tvShowDetailRespModel?.image
                            }

                            if (!TextUtils.isEmpty(childPosition?.data?.misc?.movierights.toString()!!)
                            ) {
                                dq.planName = childPosition.data?.misc?.movierights.toString()!!
                                dq.planType = CommonUtils.getContentPlanType(dq.planName)
                            }

                            dq.pType = DetailPages.TVSHOW_DETAIL_ADAPTER.value
                            dq.contentType = ContentTypes.TV_SHOWS.value
                            val eventModel = HungamaMusicApp.getInstance()
                                .getEventData(tvShowDetailRespModel?.id.toString())
                            dq.source = eventModel.sourceName

                            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                ?.findByContentId(
                                    childPosition.data?.id!!.toString()
                                )
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(
                                    childPosition.data?.id!!.toString()
                                )
                            if ((!downloadQueue?.contentId.equals(
                                    childPosition?.data?.id!!.toString()
                                ))
                                && (!downloadedAudio?.contentId.equals(
                                    childPosition?.data?.id!!.toString()
                                ))
                            ) {
                                downloadQueueList.add(dq)
                            }
                            // }
                            //if (downloadQueueList.size > 0){
                            (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                                downloadQueueList,
                                this@DiscoverTabFragment,
                                false,
                                true
                            )

                        }
                    }
                }
            }
        }
    }

    fun callPocastListDownload(
        homedata: Resource<PlaylistDynamicModel>,
        tvShowDetailRespModel: BodyDataItem)
    {

        baseIOScope.launch {

            var seasonList = homedata?.data?.data?.body?.rows?.get(0)?.data?.misc?.tracks!!

            if (seasonList?.isNotEmpty() == true) {

                var attributeCensorRating = ""
                if (!seasonList.get(0).data?.misc?.attributeCensorRating.isNullOrEmpty()) {
                    attributeCensorRating =
                        seasonList.get(0).data?.misc?.attributeCensorRating?.get(
                            0
                        ).toString()
                }
                if (!CommonUtils.checkUserCensorRating(
                        requireContext(),
                        attributeCensorRating
                    )
                ) {
                    val dpm1 = DownloadPlayCheckModel()
                    dpm1.contentId = seasonList?.get(0)?.data?.id.toString()
                    dpm1.planName =  seasonList?.get(0)?.data?.misc?.movierights.toString()
                    dpm1.isAudio = true
                    dpm1.isDownloadAction = true
                    dpm1.isShowSubscriptionPopup = true
                    dpm1.clickAction = ClickAction.FOR_ALL_CONTENT
                    dpm1.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
                    if (CommonUtils.userCanDownloadContent(
                            requireContext(),
                            null,
                            dpm1,
                            this@DiscoverTabFragment, Constant.drawer_downloads_exhausted
                        )
                    ) {
                        for (childPosition in seasonList?.iterator()!!) {

                            val dpm = DownloadPlayCheckModel()

                            dpm.contentId = childPosition.data.id
                            dpm.contentTitle = childPosition.data.title
                            dpm.planName = childPosition.data?.misc?.movierights.toString()
                            dpm.isAudio = false
                            dpm.isDownloadAction = true
                            dpm.isShowSubscriptionPopup = true
                            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                            dpm.restrictedDownload =
                                RestrictedDownload.valueOf(childPosition.data?.misc?.restricted_download!!)


                            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                            var dq = DownloadQueue()
                            //for (item in podcastEpisodeList?.iterator()!!){

                            dq = DownloadQueue()
                            if (!TextUtils.isEmpty(
                                    childPosition?.data?.id.toString()
                                )
                            ) {
                                dq.contentId =
                                    childPosition.data?.id.toString()
                            }

                            if (!TextUtils.isEmpty(childPosition.data?.title)
                            ) {
                                dq.title = childPosition.data?.title
                            }

                            if (!TextUtils.isEmpty(childPosition?.data?.subtitle)) {
                                dq.subTitle = childPosition.data?.subtitle
                            }

                            if (!TextUtils.isEmpty(childPosition?.data?.image!!)) {
                                dq.image = childPosition.data?.image!!
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel.id!!)) {
                                dq.parentId = tvShowDetailRespModel.id!!
                            }
                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.title!!)) {
                                dq.pName = tvShowDetailRespModel?.title
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.subTitle!!)) {
                                dq.pSubName = tvShowDetailRespModel?.subTitle
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.releasedate!!)) {
                                dq.pReleaseDate = tvShowDetailRespModel?.releasedate
                            }

                            if (!TextUtils.isEmpty(tvShowDetailRespModel?.image!!)) {
                                dq.pImage = tvShowDetailRespModel?.image
                            }

                            if (!TextUtils.isEmpty(childPosition?.data?.misc?.movierights.toString()!!)
                            ) {
                                dq.planName = childPosition.data?.misc?.movierights.toString()!!
                                dq.planType = CommonUtils.getContentPlanType(dq.planName)
                            }

                            dq.pType = DetailPages.TVSHOW_DETAIL_ADAPTER.value
                            dq.contentType = ContentTypes.PODCAST.value
                            val eventModel = HungamaMusicApp.getInstance()
                                .getEventData(tvShowDetailRespModel?.id.toString())
                            dq.source = eventModel.sourceName

                            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                ?.findByContentId(
                                    childPosition.data?.id!!.toString()
                                )
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(
                                    childPosition.data?.id!!.toString()
                                )
                            if ((!downloadQueue?.contentId.equals(
                                    childPosition?.data?.id!!.toString()
                                ))
                                && (!downloadedAudio?.contentId.equals(
                                    childPosition?.data?.id!!.toString()
                                ))
                            ) {
                                downloadQueueList.add(dq)
                            }
                            // }
                            //if (downloadQueueList.size > 0){
                            (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                                downloadQueueList,
                                this@DiscoverTabFragment,
                                false,
                                true
                            )


                        }
                    }
                }
            }
        }
    }

    private fun setAddOrRemoveFavourite(playlistRespModel: BodyDataItem?) {

        playlistRespModel?.isFollow = !playlistRespModel?.isFollow!!
        val contentId = playlistRespModel?.id
        val typeId = playlistRespModel?.contentTypeId.toString()
        setAddOrRemoveFavourite(contentId, typeId, playlistRespModel?.isFollow!!,false)
        /*  baseIOScope.launch {
              if(playlistRespModel?.isFollow){
                  val messageModel = MessageModel(getString(R.string.album_str_3), getString(R.string.album_str_4),
                      MessageType.NEUTRAL, true)
                  CommonUtils.showToast(requireContext(), messageModel)
                  val hashMap = java.util.HashMap<String, String>()
                  hashMap.put(EventConstant.ACTOR_EPROPERTY,Utils.arrayToString(playlistRespModel?.misc?.actorf!!))
                  hashMap.put(EventConstant.ALBUMID_EPROPERTY,""+playlistRespModel?.id)
                  var newContentId=playlistRespModel?.id!!
                  var contentIdData=newContentId.replace("playlist-","")
                  hashMap.put(EventConstant.CONTENTID_EPROPERTY,""+contentIdData)
                  val albumType=playlistRespModel?.type
                  setLog(
                      TAG,
                      "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" +albumType)} albumType:${albumType}"
                  )
                  hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+playlistRespModel?.type!!))
                  hashMap.put(EventConstant.GENRE_EPROPERTY,Utils.arrayToString(playlistRespModel?.genre))
                  hashMap.put(EventConstant.LANGUAGE_EPROPERTY,Utils.arrayToString(playlistRespModel?.misc?.languages))
                  hashMap.put(EventConstant.LYRICIST_EPROPERTY,Utils.arrayToString(playlistRespModel?.misc?.lyricist!!))
                  hashMap.put(EventConstant.MOOD_EPROPERTY,""+ playlistRespModel?.misc?.mood)
                  hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,Utils.arrayToString(playlistRespModel?.misc?.musicdirectorf!!))
                  hashMap.put(EventConstant.NAME_EPROPERTY,""+ playlistRespModel?.title)
                  hashMap.put(EventConstant.PODCASTHOST_EPROPERTY,"")
                  hashMap.put(EventConstant.SINGER_EPROPERTY,Utils.arrayToString(playlistRespModel?.misc?.singerf!!))
                  hashMap.put(EventConstant.SOURCE_EPROPERTY,""+playlistRespModel?.title)
                  hashMap.put(EventConstant.TEMPO_EPROPERTY,Utils.arrayToString(playlistRespModel?.misc?.tempo!!))
                  hashMap.put(EventConstant.CREATOR_EPROPERTY,"Hungama")
                  hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,playlistRespModel?.date))
                  EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))

              }else{
                  val messageModel = MessageModel(getString(R.string.popup_str_83), getString(R.string.popup_str_84),
                      MessageType.NEUTRAL, true)
                  CommonUtils.showToast(requireContext(), messageModel)
              }
          }*/
    }

    public fun Watchlist(data: BodyDataItem?) {

        data?.isFollow = !data?.isFollow!!
        setAddOrRemoveWatchlist(
            data?.id,
            "" + data?.contentTypeId,
            data?.isFollow!!,
            Constant.MODULE_WATCHLIST, "home_banner"
        )

    }

    fun shareBanner(data: BodyDataItem?) {
        if (data != null && !TextUtils.isEmpty(data?.share)) {
            val shareurl = getString(R.string.music_player_str_18) + " " + data?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else {
            try {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(data?.id.toString())
                if (downloadedAudio != null && downloadedAudio?.contentId.equals(data?.id.toString()) && !TextUtils.isEmpty(
                        downloadedAudio.contentShareLink)) {
                    var shareurl = getString(R.string.music_player_str_18) + " " + downloadedAudio.contentShareLink
                    shareurl += "play/"
                    Utils.shareItem(requireActivity(), shareurl)
                } else {
                    setLog("TAG", "rl_music_player_menu_share share is empty")
                }
            } catch (e: Exception) {
                setLog("TAG", "rl_music_player_menu_share share is empty")
            }
        }
    }

    private fun setFollowUnFollow(data: BodyDataItem?) {
        if (ConnectionUtil(context).isOnline) {

            data?.isFollow = !data?.isFollow!!

            val jsonObject = JSONObject()
            jsonObject.put("followingId", data?.id)
            jsonObject.put("follow", data?.isFollow)

            val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())
            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId", data?.id)
            jsonObject1.put("typeId", "" + data?.type)
            jsonObject1.put("action", data?.isFollow)
            jsonObject1.put("module", Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","setFollowUnFollow")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_STORY_DISPLAY_ACTIVITY && resultCode == AppCompatActivity.RESULT_OK) {
            if (activity != null && activity is MainActivity){
                val isPause = SharedPrefHelper.getInstance().getLastAudioContentPlayingStatus()
                val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                if (status == Constant.pause && !isPause){
                    (activity as MainActivity).playPlayer()
                }

            }
            if (data?.hasExtra(UPDATED_STORY_USER_LIST) == true) {
                baseIOScope.launch {
                    storyUsersList = data.getParcelableArrayListExtra(UPDATED_STORY_USER_LIST)
                    if (!storyUsersList.isNullOrEmpty() && !bucketParentAdapter?.getListData()?.get(parentPos)?.items?.isNullOrEmpty()!!) {
                        val tempAllRemainingStoryUsersList: Deferred<ArrayList<BodyDataItem>> =
                            baseIOScope.async {
                                //setLog("TestUserStory", "Sort-1.1")
                                CommonUtils.getSortedUserStoryByRead(
                                    storyUsersList!!,
                                    bucketParentAdapter?.getListData()?.get(parentPos)?.items
                                )
                            }
                        //setLog("TestUserStory", "Sort-3")
                        bucketParentAdapter?.updateList(
                            tempAllRemainingStoryUsersList.await(),
                            parentPos
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            tracksViewModel.onCleanup()
            (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
            //exoPlayerRecyclerView?.releasePlayer()
            bucketParentAdapter?.stopOriginalPlayer()
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
        }catch (e:Exception){

        }
    }



    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(SELECTED_TRACK_POSITION, selectedTrackPosition)
        if (trackPlayStartPosition > 0) {
            intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        }
        intent.putExtra(PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        try {
            if (ConnectionUtil(context).isOnline) {

                if(playableContentViewModel==null){
                    playableContentViewModel = ViewModelProvider(
                        this
                    ).get(PlayableContentViewModel::class.java)
                }

                playableContentViewModel?.getPlayableContentList(requireContext(), id)
                    ?.observe(this,
                        Observer {
                            when (it.status) {
                                com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                    setProgressBarVisible(false)
                                    if (it?.data!! != null) {
                                        setLog("isViewLoading", "isViewLoading ${it?.data!!}")
                                        if (isDirectPlay == 1 && isRadio) {
                                            baseIOScope.launch {
                                                setDirectPlayableContentListData(it?.data!!)
                                                setLog("isDirectPlay", "isDirectPlay ${isDirectPlay}")
                                            }
                                        }else{
                                            if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                                setLog("isDirectPlayed", "isDirectPlayed ${it?.data?.data?.head?.headData?.misc?.url}")
                                                baseIOScope.launch {
                                                    setPlayableContentListData(it?.data!!)
                                                }
                                            }else{
                                                playableItemPosition = playableItemPosition +1
                                                if (playableItemPosition < playableItem?.items?.size!!){
                                                    if (CONTENT_TYPE == CONTENT_MOOD_RADIO || CONTENT_TYPE == CONTENT_ON_DEMAND_RADIO || CONTENT_TYPE == CONTENT_ARTIST_RADIO){
                                                        getPlayableContentUrl(moodRadioListRespModel?.get(playableItemPosition)?.data?.id!!)
                                                    }else{
                                                        getPlayableContentUrl(playableItem.items?.get(playableItemPosition)?.data?.id!!)
                                                    }
                                                }

                                            }
                                        }
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
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","getPlayableContentUrl")
            }
        }catch (e:Exception){

        }
    }

    /**
     * get Playable url for song : 33, 35
     *
     * @param id
     */
    private fun getPlayableMoodRadioList(id:String, type:Int){
        if (ConnectionUtil(context).isOnline) {
            if (type == CONTENT_MOOD_RADIO){
                playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setLog("IAmIn","MoodRadio${type}")
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }else if (type == CONTENT_ARTIST_RADIO){
                playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
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
            }else {
                playableContentViewModel?.getOnDemandRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }

        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel,"DiscoverTabFragment","getPlayableMoodRadioList")
        }
    }

    var moodRadioListRespModel: MoodRadioListRespModel?=null

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null && playableItem?.items!=null && playableItemPosition<playableItem?.items?.size!!) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()
            isLastDurationPlay = false
            setLog(TAG, "setPlayableContentListData playerType: ${playableItem?.items?.get(playableItemPosition)?.data?.type}")
            if(playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_MOOD_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ON_DEMAND_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ARTIST_RADIO, true)){

                /* if(moodRadioListRespModel.isNullOrEmpty())*/
                for (i in moodRadioListRespModel?.indices!!){

                    /* if (playableContentModel.id == moodRadioListRespModel?.get(i)?.data?.id){
                         setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                     }else{
                         setRadioDataList(null, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                     }*/
                    if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(i)?.data?.id){
                        setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }
                    if (i > playableItemPosition){
                        setRadioDataList(null, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }

                }
                setLog(TAG, "onClick btn_next_play_mini size 2: ${songDataList?.size}")
                val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)

                if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if(it?.data?.id==playableContentModel?.data?.head?.headData?.id){
                            if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(0, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))

                            }else{
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }
                            isLastDurationPlay=true
                            return@forEach
                        }
                    }

                    if(!isLastDurationPlay){
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                }else{
                    tracksViewModel.prepareTrackPlayback(0)

                }
            }else if (playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_LIVE_RADIO, true)){
                setPlayableDataList(playableContentModel, playableItem, playableItemPosition)

                setLog(TAG, "onClick btn_next_play_mini size 2: ${songDataList?.size}")
                val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)

                if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if(it?.data?.id.equals(playableContentModel?.data?.head?.headData?.id,true)){
                            if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                setLog(TAG, "onClick btn_next_play_mini size 3: ${songDataList?.size}")
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(0, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))

                            }else{
                                setLog(TAG, "onClick btn_next_play_mini size 4: ${songDataList?.size}")
//                                tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }

                            isLastDurationPlay=true
                            return@forEach
                        }
                    }

                    if(!isLastDurationPlay){
                        setLog(TAG, "onClick btn_next_play_mini size 5: ${songDataList?.size}")
//                        tracksViewModel.prepareTrackPlayback(playableItemPosition)
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                }else{
                    setLog(TAG, "onClick btn_next_play_mini size 6: ${songDataList?.size}")
//                    tracksViewModel.prepareTrackPlayback(playableItemPosition)
                    tracksViewModel.prepareTrackPlayback(0)

                }
            }else{
                for (i in playableItem.items!!.indices){

                    /*if (playableContentModel.id == playableItem.items?.get(i)?.data?.id){
                        setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                    }else{
                        setPlayableDataList(null, playableItem, i)
                    }*/
                    if (playableContentModel?.data?.head?.headData?.id == playableItem.items?.get(i)?.data?.id){
                        setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                    }else if (i > playableItemPosition){
                        setPlayableDataList(null, playableItem, i)
                    }

                }
                setLog(TAG, "onClick btn_next_play_mini size 2: ${songDataList?.size}")
                val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)
                if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if(it?.data?.id.equals(playableContentModel?.data?.head?.headData?.id,true)){
                            if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                setLog(TAG, "onClick btn_next_play_mini size 3: ${songDataList?.size}")
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(0, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))

                            }else{
                                setLog(TAG, "onClick btn_next_play_mini size 4: ${songDataList?.size}")
//                                tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }

                            isLastDurationPlay=true
                            return@forEach
                        }
                    }

                    if(!isLastDurationPlay){
                        setLog(TAG, "onClick btn_next_play_mini size 5: ${songDataList?.size}")
//                        tracksViewModel.prepareTrackPlayback(playableItemPosition)
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                }else{
                    setLog(TAG, "onClick btn_next_play_mini size 6: ${songDataList?.size}")
//                    tracksViewModel.prepareTrackPlayback(playableItemPosition)
                    tracksViewModel.prepareTrackPlayback(0)

                }


            }


        }

    }

    var songDataList:ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: RowsItem,
        position:Int
    ) {
        val track:Track = Track()

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.id)){
            track.id = playableItem.items?.get(position)?.data?.id!!.trim().toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.title)){
            track.title = playableItem.items?.get(position)?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.subTitle)){
            track.subTitle = playableItem.items?.get(position)?.data?.subTitle
        }else{
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!playableItem.items?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem.items?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)){
            track.playerType = playableItem.items?.get(position)?.data?.type
        }else{
            track.playerType = MUSIC_PLAYER
        }
        if (activity != null && !TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)) {
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_PODCAST){
                track.contentType = ContentTypes.PODCAST.value
            }else if (playType == Constant.CONTENT_MUSIC){
                track.contentType = ContentTypes.AUDIO.value
            }
            else if (playType == Constant.CONTENT_RADIO ||
                playType == Constant.CONTENT_ARTIST_RADIO ||
                playType == Constant.CONTENT_ON_DEMAND_RADIO ||
                playType == Constant.CONTENT_MOOD_RADIO){
                track.contentType = ContentTypes.RADIO.value
            }
            else if(playType == Constant.CONTENT_LIVE_RADIO){
                track.contentType = ContentTypes.Live_Radio.value
            }
        }

        if(playableItem?.heading?.contains("Good Morning")!!){
            var displayName = ""
            if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserFirstname())){
                displayName += SharedPrefHelper.getInstance().getUserFirstname()!! + " "
            }

            if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserLastname())){
                displayName += SharedPrefHelper.getInstance().getUserLastname()!!
            }

            if (TextUtils.isEmpty(displayName)){
                track.heading=""+ greeting
            }else{
                track.heading=""+ greeting + " " + displayName
            }

        }else if (!TextUtils.isEmpty(playableItem.heading)){
            track.heading = playableItem.heading
        }else{
            track.heading = ""
        }

//        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image)){
//            track.image = playableContentModel?.data?.head?.headData?.image
//        }
        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.playble_image)){
            track.image = playableItem.items?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.image)) {
            track.image = playableItem.items?.get(position)?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem.items?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem.items?.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem.items?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.items?.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem.items?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.items?.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        setLog("setPlayableDataList", "play track${track}")
        songDataList.add(track)
    }

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: RowsItem
    ) {
        val track:Track = Track()


        if (!TextUtils.isEmpty(playableItem?.data?.id)){
            track.id = playableItem?.data?.id!!.trim().toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.data?.title)){
            track.title = playableItem?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.subtitle)){
            track.subTitle = playableItem?.data?.subtitle
        }else{
            track.subTitle = ""
        }

        if (!playableItem?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }


        if (playableItem?.data?.misc!=null&&playableItem?.data?.misc?.artist!=null){
            track.artistName = TextUtils.join(",", playableItem?.data?.misc?.artist!!)
        }else{
            track.artistName = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playableItem1.items?.get(position)?.data?.type)){
            track.playerType = playableItem1.items?.get(position)?.data?.type
            setLog("AudioContentType", "DiscoverTabFragment-contentType- ${track.playerType}")
        }else{
            track.playerType = PLAYER_RADIO
        }
        if (!TextUtils.isEmpty(playableItem1.heading)){
            if(playableItem1.heading?.contains("Good Morning",true)!!){
                track.heading = greeting
            }else{
                track.heading = playableItem1.heading
            }

        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem1.items?.get(position)?.data?.playble_image)){
            track.image = playableItem1.items?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem1.items?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem1.items?.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem1.items?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem1.items?.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem1.items?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem1.items?.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        val playType = (activity as MainActivity).getPlayerType(track.playerType)
        if (playType == Constant.CONTENT_RADIO ||
            playType == Constant.CONTENT_ARTIST_RADIO ||
            playType == Constant.CONTENT_ON_DEMAND_RADIO ||
            playType == Constant.CONTENT_MOOD_RADIO){
            track.contentType = ContentTypes.RADIO.value
        }
        else if(playType == Constant.CONTENT_LIVE_RADIO){
            track.contentType = ContentTypes.Live_Radio.value
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        setLog("printTrackData",  track.toString())
        songDataList.add(track)
    }





    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        //setLog("MoreItemHeading", heading)
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        CoroutineScope(Dispatchers.IO).launch {
            val dataMap=HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+headItemsItem?.title)
            var sourcePage = ""
            if (!TextUtils.isEmpty(MainActivity.lastItemClicked)){
                sourcePage = MainActivity.lastItemClicked
            }

            if (!TextUtils.isEmpty(MainActivity.headerItemName)){
                if (!TextUtils.isEmpty(sourcePage)){
                    sourcePage += "_"+MainActivity.headerItemName
                }
            }

            if (!TextUtils.isEmpty(Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))){
                if (!TextUtils.isEmpty(sourcePage)){
                    sourcePage += "_"+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type)
                }
            }
            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY, sourcePage)
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }

    private fun setDirectPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (playableContentModel != null) {
                songDataList = arrayListOf()
                isLastDurationPlay = false

                /*if (playableContentModel?.data?.head?.headData?.id == defaultContentId) {
                    setDirectPlayContentDataList(playableContentModel)
                }*/

                BaseActivity.setTrackListData(setDirectPlayContentDataList(playableContentModel))

                if (HungamaMusicApp?.getInstance()
                        ?.getContinueWhereLeftData() != null && HungamaMusicApp?.getInstance()
                        ?.getContinueWhereLeftData()?.size!! > 0
                ) {

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if (it?.data?.id == playableContentModel?.data?.head?.headData?.id) {
                            if (it?.data?.durationPlay != null && it?.data?.durationPlay?.toLong()!! > 0) {
                                tracksViewModel.prepareTrackPlayback(
                                    0,
                                    TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!)
                                )

                            } else {
                                tracksViewModel.prepareTrackPlayback(0)

                            }
                            isLastDurationPlay = true
                            return@forEach
                        }
                    }

                    if (!isLastDurationPlay) {
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                } else {
                    tracksViewModel.prepareTrackPlayback(0)
                }
            }
        }
    }

    fun setDirectPlayContentDataList(
        playableContentModel: PlayableContentModel?
    ): ArrayList<Track> {
        val track:Track = Track()
        val songDataList:ArrayList<Track> = arrayListOf()

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.id)){
            track.id = playableContentModel?.data?.head?.headData?.id!!.trim().toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.title)){
            track.title = playableContentModel?.data?.head?.headData?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.subtitle)){
            track.subTitle = playableContentModel?.data?.head?.headData?.subtitle
        }else{
            track.subTitle = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (playableContentModel?.data?.head?.headData?.misc!=null&&playableContentModel?.data?.head?.headData?.misc?.artist!=null){
            track.artistName = TextUtils.join(",", playableContentModel?.data?.head?.headData?.misc?.artist!!)
        }else{
            track.artistName = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        setLog(TAG, "setDirectPlayContentDataList playerType:${playableContentModel?.data?.head?.headData?.type}")

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.type.toString())){
            track.playerType = playableContentModel?.data?.head?.headData?.type.toString()
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_RADIO ||
                playType == Constant.CONTENT_ARTIST_RADIO ||
                playType == Constant.CONTENT_ON_DEMAND_RADIO ||
                playType == Constant.CONTENT_MOOD_RADIO){
                track.contentType = ContentTypes.RADIO.value
            }
        }else{
            if (isRadio){
                track.playerType = PLAYER_RADIO
                track.contentType = ContentTypes.RADIO.value
            }else{
                track.playerType = MUSIC_PLAYER
            }
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.playble_image)){
            track.image = playableContentModel?.data?.head?.headData?.playble_image
        }else if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image)){
            track.image = playableContentModel?.data?.head?.headData?.image
        }else{
            track.image = ""
        }

        if (playableContentModel?.data?.head?.headData?.misc?.explicit != null){
            track.explicit = playableContentModel?.data?.head?.headData?.misc?.explicit!!
        }
        if (playableContentModel?.data?.head?.headData?.misc?.restricted_download != null){
            track.restrictedDownload = playableContentModel?.data?.head?.headData?.misc?.restricted_download!!
        }
        if (playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
        }

        songDataList.add(track)
        return songDataList
    }

    private fun setLocalBroadcast(){
        if (isAdded && context != null){
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT));
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(mMessageReceiver, IntentFilter(Constant.DISCOVER_MAIN_TAB_EVENT));
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                mMessageReceiver,
                IntentFilter(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT)
            );
        }
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)

            if (context != null && event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                setLog("DiscoverTabFragment", "onLocalBroadcastEventCallBack-event-$event-title-${headItemsItem?.title}")
                /*when((requireActivity() as MainActivity).getAudioPlayerPlayingStatus()){
                    Constant.playing -> {
                        setLog("MainActivity", "PlayerEvent-playing")
                    }
                    Constant.pause ->{
                        setLog("MainActivity", "PlayerEvent-pause")
                    }
                    else -> {
                        setLog("MainActivity", "PlayerEvent-none")
                    }
                }*/

                bucketRespModel?.let { checkAllContentDownloadedOrNot(it) }

                setLog("MoengageNudgeView", "DiscoverTabFragment-onLocalBroadcastEventCallBack-setViewBotoomSpacing called-title-${headItemsItem?.title}")
                setViewBottomSpacing()
            } else if (context != null && event == Constant.DISCOVER_MAIN_TAB_HIDDEN_RESULT_CODE) {
                if (intent.getBooleanExtra("hidden", true)){
                    bucketParentAdapter?.pauseOriginalPlayer()
                }else{
                    when((requireActivity() as MainActivity).getAudioPlayerPlayingStatus()){
                        Constant.playing -> {
                            bucketParentAdapter?.pauseOriginalPlayer()
                        }
                        else -> {
                            bucketParentAdapter?.playOriginalPlayer()
                        }
                    }
                }
            } else if (context != null && event == Constant.STICKY_ADS_VISIBILITY_CHANGE_RESULT_CODE){
                setViewBottomSpacing()
            }
        }
    }

    var firstTime = true


    private fun initGlide(): RequestManager {
        val options = RequestOptions()
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            if (isAdded) {
                setViewBottomSpacing()
            }
            //exoPlayerRecyclerView?.onPlayPlayer()
            bucketParentAdapter?.playOriginalPlayer()
            setLog("discoverFragment", "$pageName - setUserVisibleHint()-true-{$isVisibleToUser}")
        }else{
            setLog("discoverFragment", "$pageName - setUserVisibleHint()-false-{$isVisibleToUser}")
            //exoPlayerRecyclerView?.onPausePlayer()
            bucketParentAdapter?.pauseOriginalPlayer()
        }
    }



    override fun onPause() {
        super.onPause()
        setLog("discoverFragment", "$pageName - onPause()")
        //exoPlayerRecyclerView?.onPausePlayer()
        /*        bucketParentAdapter?.pauseOriginalPlayer()
                BucketParentAdapter.timer.cancel()
                if (Itype50PagerAdapter.simpleExoplayer!= null)
                {
                    if (Itype50PagerAdapter.simpleExoplayer?.isPlaying == true)
                        Itype50PagerAdapter.simpleExoplayer?.pause()
                }*/
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver-1", "mMessageReceiver-"+intent)
            if (intent != null){
                if (intent.hasExtra("EVENT")){
                    setLog("BroadcastReceiver-1", "mMessageReceiver-"+intent.getIntExtra("EVENT", 0))
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }

    override fun onDismiss(inAppData: InAppData) {
        if (nudge != null){
            //setLog("MoengageNudgeView", "DiscoverTabFragment-onDismiss-inAppData-${inAppData.activity.componentName.className}")
            //setLog("MoengageNudgeView", "DiscoverTabFragment-onDismiss-inAppData-${inAppData.inAppCampaign.campaignName}")
            isNudgeViewVisible = nudge.isShown
            setLog("MoengageNudgeView", "DiscoverTabFragment-onDismiss-isNudgeShown-${isNudgeViewVisible}")
            setLog("MoengageNudgeView", "DiscoverTabFragment-onDismiss-title-${headItemsItem?.title}")
            setLog("MoengageNudgeView", "DiscoverTabFragment-onDismiss-setViewBotoomSpacing called-title-${headItemsItem?.title}")
            setViewBottomSpacing()
            if (activity != null){
                Utils.setWindowProperty(requireActivity())
            }
        }
    }

    override fun onShown(inAppData: InAppData) {
        if (nudge != null){
            //setLog("MoengageNudgeView", "DiscoverTabFragment-onShown-inAppData-${inAppData.activity.componentName.className}")
            //setLog("MoengageNudgeView", "DiscoverTabFragment-onShown-inAppData-${inAppData.inAppCampaign.campaignName}")
            isNudgeViewVisible = nudge.isShown
            setLog("MoengageNudgeView", "DiscoverTabFragment-onShown-isNudgeShown-${isNudgeViewVisible}")
            setLog("MoengageNudgeView", "DiscoverTabFragment-onShown-title-${headItemsItem?.title}")
            setLog("MoengageNudgeView", "DiscoverTabFragment-onShown-setViewBotoomSpacing called-title-${headItemsItem?.title}")
            setViewBottomSpacing()
        }
    }

    private fun setViewBottomSpacing(){
        baseIOScope.launch {
            if (isVisible && context != null){
                if (isNudgeViewVisible){
                    var nudgeHeight = 0
                    var rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_120)

                    if(tempHomeData!=null){
                        rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_10)
                    }

                    if (nudge?.height != null){
                        nudgeHeight = nudge.height
                    }
                    rvTopPadding += nudgeHeight

                    setLog("MoengageNudgeView", "DiscoverTabFragment-setViewBottomSpacing-true-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

                    headItemsItem?.page?.let {
                        if(it=="podcast" && isOnlineTabSelected==false){
                            CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_8),
                                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                        }
                    }
                }else{
                    val nudgeHeight = 0
                    var rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_120)
                    if (tempHomeData != null) {
                        rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_4_minus)
                    }
                    rvTopPadding += nudgeHeight

                    setLog("MoengageNudgeView", "DiscoverTabFragment-setViewBottomSpacing-false-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                    headItemsItem?.page?.let {
                        if(it=="podcast" && isOnlineTabSelected==false){
                            CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_8),
                                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                        }
                    }
                }
            }
        }
    }

    private fun checkAllContentDownloadedOrNot(bucketRespModel: HomeModel) {
        baseIOScope.launch {
            try {
                if (isAdded && context != null) {
                    var isCurrentContentPlayingFromThis = false
                    if (!bucketRespModel.data?.body?.rows.isNullOrEmpty()) {
                        /*setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-1-$isCurrentContentPlayingFromThis"
                        )*/
                        isCurrentContentPlayingFromThis =
                            withContext(Dispatchers.Default) {
                                /*setLog(
                                    "isCurrentContentPlayingFromThis",
                                    "isCurrentContentPlayingFromThis-2-$isCurrentContentPlayingFromThis"
                                )*/
                                checkAllContentDWOrNot(bucketRespModel)
                            }
                        /*setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-5-$isCurrentContentPlayingFromThis"
                        )*/
                        withContext(Dispatchers.Main) {
                            /*setLog(
                                "isCurrentContentPlayingFromThis",
                                "isCurrentContentPlayingFromThis-6-$isCurrentContentPlayingFromThis"
                            )*/
                            if (isCurrentContentPlayingFromThis) {
                                if (bucketParentAdapter != null) {
                                    setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDownloadedOrNot-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                                    bucketParentAdapter?.notifyItemChanged(lastPlayingContentParentIndex)
                                    bucketParentAdapter?.notifyItemChanged(currentPlayingContentParentIndex)
                                }
                                /*setLog(
                                    "isCurrentContentPlayingFromThis",
                                    "isCurrentContentPlayingFromThis-7-$isCurrentContentPlayingFromThis"
                                )*/
                            }
                        }

                        /*setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-8-$isCurrentContentPlayingFromThis"
                        )*/
                    }
                }
            }catch (e:Exception){

            }

        }
    }

    var currentPlayingContentParentIndex = -1
    var currentPlayingContentIndex = -1
    var lastPlayingContentParentIndex = -1
    var lastPlayingContentIndex = -1

    private suspend fun checkAllContentDWOrNot(bucketRespModel: HomeModel):Boolean {
        try {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!bucketRespModel.data?.body?.rows.isNullOrEmpty()) {
                    try {
                        bucketRespModel.data?.body?.rows?.forEachIndexed { i, row ->
                            if (row != null && (row.type == 7 || row.type == 8) && !row.items.isNullOrEmpty()){
                                //setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-0-row.type-${row.type}")
                                row.items?.forEachIndexed { index, it ->
                                    //setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-1-it?.data?.type-${it?.data?.type}-i-$i")
                                    if (it?.data?.type.toString().equals(Constant.PLAYER_AUDIO_SONG)){
                                        if (!BaseActivity.songDataList.isNullOrEmpty()
                                            && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                                        ) {
                                            val currentPlayingContentId =
                                                BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                                            //setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-1-currentPlayingContentId-$currentPlayingContentId-it?.data?.id-${it?.data?.id}-index-$index")
                                            if (currentPlayingContentId?.toString()?.equals(it?.data?.id)!!) {
                                                it?.data?.isCurrentPlaying = true
                                                isCurrentContentPlayingFromThis = true
                                                //setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-1-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex-index-$index")
                                                if (currentPlayingContentIndex >= 0){

                                                } else {
                                                    lastPlayingContentParentIndex = i
                                                    lastPlayingContentIndex = index
                                                }
                                                currentPlayingContentParentIndex = i
                                                currentPlayingContentIndex = index
                                            } else {
                                                if (it?.data?.isCurrentPlaying == true){
                                                    lastPlayingContentParentIndex = i
                                                    lastPlayingContentIndex = index
                                                }
                                                it?.data?.isCurrentPlaying = false
                                            }
                                        } else {
                                            if (it?.data?.isCurrentPlaying == true){
                                                lastPlayingContentParentIndex = i
                                                lastPlayingContentIndex = index
                                            }
                                            it?.data?.isCurrentPlaying = false
                                        }
                                    }
                                }
                            }
                        }
                    }catch (e:Exception){

                    }
                }
                return isCurrentContentPlayingFromThis
            }
        } catch (e: Exception) {

        }

        return false
    }

    override fun onDownloadVideoQueueItemChanged(
        downloadManager: DownloadManager,
        download: androidx.media3.exoplayer.offline.Download
    ) {

    }

    override fun onDownloadProgress(
        downloads: List<androidx.media3.exoplayer.offline.Download?>?,
        progress: Int,
        currentExoDownloadPosition: Int
    ) {

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
    }


    private val fetchMusicDownloadListener: FetchListener = object : AbstractFetchListener() {
        override fun onAdded(download: Download) {
            super.onAdded(download)

        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            super.onQueued(download, waitingOnNetwork)
            setLog("DWProgrss-onQueued", download.id.toString())
        }

        override fun onCompleted(download: Download) {
            super.onCompleted(download)
            setLog("DWProgrss-onCompleted", download.id.toString())
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            setLog("DWProgrss-onError", error.toString())
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
            setLog("DWProgrss", downloadedBytesPerSecond.toString())
        }

        override fun onPaused(download: Download) {
            super.onPaused(download)

        }

        override fun onResumed(download: Download) {
            super.onResumed(download)

        }

        override fun onCancelled(download: Download) {
            super.onCancelled(download)

        }

        override fun onRemoved(download: Download) {
            super.onRemoved(download)

        }

        override fun onDeleted(download: Download) {
            super.onDeleted(download)

        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {

            when (reason) {

                Reason.DOWNLOAD_ADDED -> {
                    setLog("DWProgrss-ADDED", data.id.toString())
                }

                Reason.DOWNLOAD_QUEUED -> {
                    setLog("DWProgrss-QUEUED", data.id.toString())
                }

                Reason.DOWNLOAD_STARTED -> {
                    setLog("DWProgrss-STARTED", data.id.toString())
                }

                Reason.DOWNLOAD_PROGRESS_CHANGED -> {
                    setLog("DWProgrss-CHANGED", data.id.toString())
                }

                Reason.DOWNLOAD_RESUMED -> {
                    setLog("DWProgrss-RESUMED", data.id.toString())
                }

                Reason.DOWNLOAD_PAUSED -> {
                    setLog("DWProgrss-PAUSED", data.id.toString())
                }

                Reason.DOWNLOAD_COMPLETED -> {
                }

                Reason.DOWNLOAD_CANCELLED -> {
                    setLog("DWProgrss-CANCELLED", data.id.toString())
                }

                Reason.DOWNLOAD_REMOVED -> {
                    setLog("DWProgrss-REMOVED", data.id.toString())
                }

                Reason.DOWNLOAD_DELETED -> {
                    setLog("DWProgrss-DELETED", data.id.toString())
                }

                Reason.DOWNLOAD_ERROR -> {
                    setLog("DWProgrss-ERROR", data.id.toString())
                }

                Reason.DOWNLOAD_BLOCK_UPDATED -> {
                    setLog("DWProgrss-UPDATED", data.id.toString())
                }

                Reason.DOWNLOAD_WAITING_ON_NETWORK -> {
                    setLog("DWProgrss-NETWORK", data.id.toString())
                }

                else -> {}
            }
        }
    }
    //listener

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
    }

    private fun getSeasonTrackList(seasonId:String,bodyDataItem: BodyDataItem,download: Boolean) {
        var selectedContentId=seasonId
        var podcastListViewModel = ViewModelProvider(this).get(PodcastViewModel::class.java)

        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getPodcastDetailList(requireContext(), seasonId!!)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")

                                if(it?.data!=null&&it?.data?.data!=null){
                                    podcastRespModel?.data?.body?.rows=it?.data?.data?.body?.rows!!
                                    playlistSongList = it?.data?.data?.body?.rows!!
                                    bodyDataItem.playlistSongList = playlistSongList
                                    podcastRespModel?.data?.head?.data?.misc?.episodeCount= it?.data?.data?.head?.data?.misc?.episodeCount!!

                                    if (download) {
                                        callPocastListDownload(it, bodyDataItem)
                                    }
                                }


                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","getSeasonTrackList")
        }
    }

}