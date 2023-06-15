package com.hungama.music.ui.main.view.fragment

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
import com.moe.pushlibrary.MoEHelper
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.model.InAppData
import com.moengage.widgets.NudgeView
import kotlinx.android.synthetic.main.fr_main.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscoverTabFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View, BucketParentAdapter.OnMoreItemClick,
    BaseActivity.OnLocalBroadcastEventCallBack, InAppLifeCycleListener {

    var homeViewModel: HomeViewModel? = null
    var tempHomeData:HomeModel?=null
    var tempHomeDataMain:HomeModel?=null
    var bucketRespModel: HomeModel? = null
    private var storyUsersList: ArrayList<BodyDataItem>? = null
    private var bucketParentAdapter: BucketParentAdapter? = null
    private var parentPos: Int = 0
    var rowList: MutableList<RowsItem?>? = null
    var songsList = ArrayList<MusicModel>()
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    private lateinit var tracksViewModel: TracksContract.Presenter

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

    companion object {
        val LAUNCH_STORY_DISPLAY_ACTIVITY = 101
        const val UPDATED_STORY_USER_LIST = "updatedStoryUserList"


        fun newInstance(mHeadItemsItem: HeadItemsItem?, bundle: Bundle): DiscoverTabFragment {
            val fragment = DiscoverTabFragment()
            bundle.putParcelable(Constant.BUNDLE_KEY_HEADITEMSITEM, mHeadItemsItem)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
            }else{
                setLog("DiscoverTabFragment", "setUpViewModel API called")
                setUpViewModel()
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
        }
    }
    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        try {
            if (ConnectionUtil(activity).isOnline) {
                homeViewModel = ViewModelProvider(
                    this
                ).get(HomeViewModel::class.java)

                var url=""
                setLog(TAG, "setUpViewModel: $headItemsItem")
                if(headItemsItem!=null&&!TextUtils.isEmpty(headItemsItem?.page)){
                    url= WSConstants.METHOD_DETAIL_PAGE+headItemsItem?.page
                    setLog(TAG, "setUpViewModel url: $url")
                }

                homeViewModel?.getHomeListDataLatest(requireContext(), url)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
//                                HungamaMusicApp.getInstance().setCacheBottomTab("${Constant.Bottom_NAV_DISCOVER}_${headItemsItem?.page}", it?.data!!)
                                if(MainActivity.headerItemPosition==0 && WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                                    setLog(TAG, "dailyDoseAPICall: data position:${MainActivity.headerItemPosition}")
//                                    dailyDoseAPICall(it?.data)
                                }else{
                                    setLog(TAG, "dailyDoseAPICall: not called position:${MainActivity.headerItemPosition} ")
                                    setProgressBarVisible(false)
                                    setData(CommonUtils.checkProUserBucket(it?.data), false)
                                    tempHomeDataMain = CommonUtils.checkProUserBucket(it?.data)

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
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }catch (e:Exception){

        }

    }


    override fun onStart() {
        super.onStart()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
    }
    override fun onResume() {
        super.onResume()
        setLog("onResume", "DiscoverTabFragment-onResume-title-${headItemsItem?.title}")
        if (activity != null){
            setLocalBroadcast()
            MoEInAppHelper.getInstance().showInApp(requireActivity())
        }
        //setLog("MoengageNudgeView", "DiscoverTabFragment-onResume-addInAppLifeCycleListener")
//        nudgeViewSetUp()
    }

    fun nudgeViewSetUp(){

        setLog(TAG, "MainActivity.lastItemClicked:${MainActivity.lastBottomItemPosClicked} MainActivity.headItemsItem:${MainActivity.headerItemPosition}")
        // get instance of the view
        val nudge = view?.findViewById<NudgeView>(R.id.nudge)
//        nudge?.bringToFront()
// initialize
        nudge?.initialiseNudgeView(activity)

        MoEHelper.getInstance(requireContext()).resetAppContext()

        setLog(TAG, "nudgeViewSetUp: isVisible:${nudge?.isVisible} height:${nudge?.rootView?.height} width:${nudge?.rootView?.width}")

    }

    fun removeBrandHubForGold(data:ArrayList<BodyRowsItemsItem?>){
        for(i in data){
            if (i!!.data!!.isBrandHub){
                data.remove(i)
                removeBrandHubForGold(data)
                break
            }
        }
    }

    var lastVisiableItem:Int?=-1
    var mLayoutManager:LinearLayoutManager?=null
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

                setLog(TAG, "setMoengageData 1 setData:setMoengageData setData: before bucketRespModel size :${bucketRespModel?.data?.body?.rows?.size!!} page : ${HungamaMusicApp.getInstance().getCacheAdsTab("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")}")

                if(HungamaMusicApp.getInstance().getCacheAdsTab("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")?.isNullOrBlank()!!)
                {
                    CoroutineScope(Dispatchers.IO).launch {
//                        delay(3000)
                    runBlocking{
                        bucketRespModel =  async {setAdsData(bucketRespModel!!, headItemsItem!!)}.await()
                        setLog(TAG, "setMoengageData 2 getCacheAdsTab  setAdsData called")
//                        bucketRespModel =  async {addMoengageSelfHandleInAppData(bucketRespModel!!, headItemsItem!!)}.await()
                        setLog(TAG, "setMoengageData 3 getCacheAdsTab  setAdsData addMoengageSelfHandleInAppData called")

                        if(rvRecentHistory != null) {
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

                setLog(TAG, "setMoengageData 4 setData: after bucketRespModel size :${bucketRespModel?.data?.body?.rows?.size!!}")

                var varient = Constant.ORIENTATION_VERTICAL
                if (bucketRespModel?.data?.body?.rows?.size!! > 1){
                    varient = Constant.ORIENTATION_HORIZONTAL
                }
                bucketParentAdapter = BucketParentAdapter(
                    bucketRespModel?.data?.body?.rows!!, requireContext(), this@DiscoverTabFragment, this@DiscoverTabFragment,Constant.DISCOVER_TAB, headItemsItem, varient)

                mLayoutManager=LinearLayoutManager(
                    activity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                rvRecentHistory?.layoutManager =mLayoutManager

                //rvMain?.applyTopBarAndBottomBarInsets(requireContext())
                rvRecentHistory?.adapter = bucketParentAdapter
                rowList = (bucketRespModel?.data?.body?.rows as MutableList<RowsItem?>?)!!

                bucketParentAdapter?.addData(rowList!!)
                rvRecentHistory?.visibility=View.VISIBLE
                rvRecentHistory?.invalidate()
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE

                rvRecentHistory?.setHasFixedSize(true)

                val lastVisibleAdsMap:HashMap<Int, Int> = HashMap()
                var prevCenterPos = -1
                rvRecentHistory?.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-findFirstCompletelyVisibleItemPosition"+ mLayoutManager?.findFirstCompletelyVisibleItemPosition())
                        val completeVisiblePosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!
                        val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                        val lastVisiable: Int = mLayoutManager?.findLastVisibleItemPosition()!!
                        val fCompleteVisible = mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!
                        setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-findFirstCompletelyVisibleItemPosition-"+ completeVisiblePosition)
                        if (!rowList.isNullOrEmpty() && rowList?.size!! > completeVisiblePosition && completeVisiblePosition >= 0){
                            setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-rowList?.get(completeVisiblePosition)?.itype-"+ rowList?.get(completeVisiblePosition)?.itype)
                            if (rowList?.get(completeVisiblePosition)?.itype == BucketChildAdapter.ROW_ITYPE_201){
                                setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-visibleItem-"+ rowList?.get(completeVisiblePosition)?.heading)
                                if (!rowList?.get(completeVisiblePosition)?.items.isNullOrEmpty() && rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible == false){
                                    setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-visibleItem-isVisible1-"+ rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible)
                                    rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible = true
                                    setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-visibleItem-isVisible2-"+ rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible)
                                    lastVisibleAdsMap.put(rowList?.get(completeVisiblePosition)?.itype!!, completeVisiblePosition)
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
                            if (rvRecentHistory != null && (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE || recyclerView.scrollState == -1)){
                                setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-recyclerView.scrollState-${recyclerView.scrollState}")
                                val center = rvRecentHistory.width / 2
                                val rvSpace = rvRecentHistory.height / 2
                                val centerView = rvRecentHistory?.findChildViewUnder(center.toFloat(), rvSpace.toFloat())

                                var centerPos = 0
                                if (centerView != null){
                                    centerPos = rvRecentHistory.getChildAdapterPosition(centerView)
                                    if (!rowList.isNullOrEmpty() &&
                                        rowList?.size!! > centerPos
                                        && centerPos >= 0
                                        && rowList?.get(centerPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                        if (prevCenterPos != centerPos) {
                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-centerView-$centerView--rvSpace-$rvSpace")
                                            withContext(Dispatchers.Main){
                                                val tempPreviousPos = prevCenterPos
                                                //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-005-tempPreviousPos-$tempPreviousPos")
                                                if (!rowList.isNullOrEmpty() && rowList?.size!! > tempPreviousPos && tempPreviousPos >= 0){
                                                    //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-006-title-${rowList?.get(tempPreviousPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                    if (rowList?.get(tempPreviousPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-007-title-${rowList?.get(tempPreviousPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                        if (!rowList?.get(tempPreviousPos)?.items.isNullOrEmpty()){
                                                            setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-tempPreviousPos-$tempPreviousPos-title-${rowList?.get(tempPreviousPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                            rowList?.get(tempPreviousPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible = false
                                                        }
                                                    }
                                                    bucketParentAdapter?.notifyItemChanged(tempPreviousPos)
                                                }
                                                prevCenterPos = centerPos
                                            }

                                            //playing view in the middle
                                            if(centerView != null){
                                                withContext(Dispatchers.Main) {
                                                    val tempCenterPos = centerPos
                                                    //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-001-tempCenterPos-$tempCenterPos")
                                                    if (!rowList.isNullOrEmpty() && rowList?.size!! > tempCenterPos && tempCenterPos >= 0){
                                                        //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-002-rowList?.get(tempCenterPos)?.itype-${rowList?.get(tempCenterPos)?.itype}")
                                                        if (rowList?.get(tempCenterPos)?.itype == BucketChildAdapter.ROW_ITYPE_47){
                                                            //setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-003-tempCenterPos-$tempCenterPos")
                                                            if (!rowList?.get(tempCenterPos)?.items.isNullOrEmpty()){
                                                                setLog("DiscoverTabFragment", "DiscoverTabFragment-setData-onScrolled-centerPos-tempCenterPos-$tempCenterPos-title-${rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.data?.title}")
                                                                rowList?.get(tempCenterPos)?.items?.get(0)?.orignalItems?.get(0)?.isVisible = true
                                                            }
                                                        }
                                                        bucketParentAdapter?.notifyItemChanged(tempCenterPos)
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
            }/*else{
                val songData =
                    bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.items!!.get(childPosition)!!.data
                val allSongs = bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.items!!
                val heading = bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.heading
                CommonUtils.setTrackList(
                    getApplicationContext(),
                    songData,
                    allSongs,
                    heading,
                    childPosition
                )

                tracksViewModel.prepareTrackPlayback(0)
                
            }*/
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
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
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
                    if (!storyUsersList.isNullOrEmpty() && !bucketParentAdapter?.getListData()?.get(parentPos)?.items?.isNullOrEmpty()!!){
                        val tempAllRemainingStoryUsersList:Deferred<ArrayList<BodyDataItem>> = baseIOScope.async {
                            //setLog("TestUserStory", "Sort-1.1")
                            CommonUtils.getSortedUserStoryByRead(storyUsersList!!, bucketParentAdapter?.getListData()?.get(parentPos)?.items)
                        }
                        //setLog("TestUserStory", "Sort-3")
                        bucketParentAdapter?.updateList(tempAllRemainingStoryUsersList.await(), parentPos)
                        //setLog("TestUserStory", "Sort-4")
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
        if(trackPlayStartPosition>0){
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

                playableContentViewModel.getPlayableContentList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data!! != null) {
                                    setLog("isViewLoading", "isViewLoading ${it?.data!!}")
                                    if (isDirectPlay == 1 && isRadio){
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
                CommonUtils.showToast(requireContext(), messageModel)
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
            CommonUtils.showToast(requireContext(), messageModel)
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
            track.id = playableItem.items?.get(position)?.data?.id!!.toLong()
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
            track.id = playableItem?.data?.id!!.toLong()
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

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
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
            track.id = playableContentModel?.data?.head?.headData?.id!!.toLong()
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
        }else{
            if (isRadio){
                track.playerType = PLAYER_RADIO
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
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT));
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.DISCOVER_MAIN_TAB_EVENT));
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT));
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

        if(isVisibleToUser){
            if (isAdded){
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
        bucketParentAdapter?.pauseOriginalPlayer()
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

                    if (nudge?.height != null){
                        nudgeHeight = nudge.height
                    }
                    rvTopPadding += nudgeHeight

                    setLog("MoengageNudgeView", "DiscoverTabFragment-setViewBottomSpacing-true-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                }else{
                    val nudgeHeight = 0
                    var rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_120)
                    rvTopPadding += nudgeHeight

                    setLog("MoengageNudgeView", "DiscoverTabFragment-setViewBottomSpacing-false-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
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

                                                }else{
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
                        //setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-2-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                    }catch (e:Exception){

                    }
                    /*setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-3-$isCurrentContentPlayingFromThis"
                    )*/
                }
                /*setLog(
                    "isCurrentContentPlayingFromThis",
                    "isCurrentContentPlayingFromThis-4-$isCurrentContentPlayingFromThis"
                )*/
                return isCurrentContentPlayingFromThis
            }
        } catch (e: Exception) {

        }

        return false
    }
}