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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.music.HungamaMusicApp
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.data.model.OnParentItemClickListener
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.data.model.MusicModel
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.ui.main.adapter.BucketChildAdapter
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.moe.pushlibrary.MoEHelper
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.model.InAppData
import com.moengage.widgets.NudgeView
import kotlinx.android.synthetic.main.fr_main.*
import kotlinx.coroutines.*
import java.util.HashMap


/**
 * A simple [Fragment] subclass.
 * Use the [VideoTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoTabFragment : BaseFragment(), OnParentItemClickListener, BucketParentAdapter.OnMoreItemClick,
    TracksContract.View, BaseActivity.OnLocalBroadcastEventCallBack, InAppLifeCycleListener {
    var bucketRespModel: HomeModel? = null
    private var storyUsersList: ArrayList<BodyDataItem>? = null
    private var bucketParentAdapter: BucketParentAdapter? = null
    private var parentPos: Int = 0
    var rowList: MutableList<RowsItem?>? = null
    var songsList = ArrayList<MusicModel>()
    var videoViewModel: VideoViewModel? = null

    private lateinit var tracksViewModel: TracksContract.Presenter

    var playableItem = RowsItem()
    var playableItemPosition = 0
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var CONTENT_TYPE = 0
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""

    var headItemsItem: HeadItemsItem?=null
    var isNudgeViewVisible = false

    companion object {
           fun newInstance(mHeadItemsItem: HeadItemsItem?, bundle: Bundle) : VideoTabFragment {
            val fragment = VideoTabFragment()
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
        baseMainScope.launch {
            if (arguments!=null){
                if(arguments?.containsKey(Constant.BUNDLE_KEY_HEADITEMSITEM)!!){
                    headItemsItem=arguments?.getParcelable(Constant.BUNDLE_KEY_HEADITEMSITEM)
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

            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@VideoTabFragment)

            shimmerLayout?.visibility = View.VISIBLE
            shimmerLayout?.startShimmer()
            val childVideoModel=HungamaMusicApp.getInstance().getCacheBottomTab("${Constant.BOTTOM_NAV_VIDEOS}_${headItemsItem?.page}")
            if(childVideoModel!=null){
                setProgressBarVisible(false)
                setData(CommonUtils.checkProUserBucket(childVideoModel))
                setLog("VideoTabFragment", "setUpViewModel static call:${Constant.BOTTOM_NAV_VIDEOS}_${headItemsItem?.page}")
            }else{

                setLog("VideoTabFragment", "setUpViewModel API called")
                setUpViewModel()
            }
            setUpPlayableContentListViewModel()

            setLog("MoengageNudgeView", "VideoTabFragment-initializeComponent-setViewBotoomSpacing called-title-${headItemsItem?.title}")
            setViewBottomSpacing()

            CommonUtils.setLog(
                "deepLinkUrl",
                "VideoChildFragment-initializeComponent--tabName=${headItemsItem?.page} && isCategory=$isCategoryPage && categoryName=$categoryName && categoryId=$categoryId"
            )
            if (isCategoryPage && !TextUtils.isEmpty(categoryName) && !TextUtils.isEmpty(categoryId)){
                val bundle = Bundle()
                bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                val categoryDetailFragment = CategoryDetailFragment(categoryId, 0)
                categoryDetailFragment.arguments = bundle
                addFragment(R.id.fl_container, this@VideoTabFragment, categoryDetailFragment, false)
            }
            MoEInAppHelper.getInstance().addInAppLifeCycleListener(this@VideoTabFragment)
            // setLog("MoengageNudgeView", "VideoTabFragment-initializeComponent-addInAppLifeCycleListener")
        }
    }

    override fun onStart() {
        super.onStart()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
    }
    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
        nudgeViewSetUp()
    }

    fun nudgeViewSetUp(){
        setLog(TAG, "nudgeViewSetUp: ")
        setLog(TAG, "MainActivity.lastItemClicked:${MainActivity.lastBottomItemPosClicked} MainActivity.headItemsItem:${MainActivity.headerItemPosition}")
        // get instance of the view
        val nudge = view?.findViewById<NudgeView>(R.id.nudge)
// initialize
        nudge?.initialiseNudgeView(activity)

        MoEHelper.getInstance(requireContext()).resetAppContext()
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
                var url=""
                setLog(TAG, "setUpViewModel: $headItemsItem")
                if(headItemsItem!=null&&!TextUtils.isEmpty(headItemsItem?.page)){
                    url= WSConstants.METHOD_DETAIL_PAGE+headItemsItem?.page+"?lang="+ SharedPrefHelper.getInstance().getLanguage()
                    setLog(TAG, "setUpViewModel url: $url")
                }else{
//                url= "http://35.200.196.46:3000/v2/app/home?lang="+ SharedPrefHelper.getInstance().getLanguage()
                }


                videoViewModel?.getWatchVidelList(requireActivity(),url)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    HungamaMusicApp.getInstance().setCacheBottomTab("${Constant.BOTTOM_NAV_VIDEOS}_${headItemsItem?.page}", it?.data!!)
                                    setData(CommonUtils.checkProUserBucket(it?.data))

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

    var mLayoutManager:LinearLayoutManager?=null
    private fun setData(it: HomeModel) {
        bucketRespModel = it
        baseMainScope.launch {
            if (isAdded && context != null && bucketRespModel != null && bucketRespModel?.data?.body != null) {

                setLog(TAG, "setMoengageData setData:setMoengageData setData: before bucketRespModel size :${bucketRespModel?.data?.body?.rows?.size!!} page : ${HungamaMusicApp.getInstance().getCacheAdsTab("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")}")

                if(HungamaMusicApp.getInstance().getCacheAdsTab("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")?.isNullOrBlank()!!){
                    runBlocking{
                        bucketRespModel =  async {setAdsData(bucketRespModel!!, headItemsItem!!)}.await()
                        setLog(TAG, "setMoengageData 2 getCacheAdsTab  setAdsData called")
                        bucketRespModel =  async {addMoengageSelfHandleInAppData(bucketRespModel!!, headItemsItem!!)}.await()
                        setLog(TAG, "setMoengageData 3 getCacheAdsTab  setAdsData addMoengageSelfHandleInAppData called")
                        if (bucketParentAdapter != null)
                            bucketParentAdapter!!.notifyItemRangeChanged(0, bucketRespModel?.data?.body?.rows?.size!! -1)
                    }

                }


                var varient = Constant.ORIENTATION_VERTICAL
                if (bucketRespModel?.data?.body?.rows?.size!! > 1){
                    varient = Constant.ORIENTATION_HORIZONTAL
                }
                bucketParentAdapter = BucketParentAdapter(
                    bucketRespModel?.data?.body?.rows!!,
                    requireContext(),
                    this@VideoTabFragment,
                    this@VideoTabFragment,
                    Constant.WATCH_TAB,
                    headItemsItem,
                    varient
                )

                mLayoutManager=LinearLayoutManager(
                    activity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                rvRecentHistory?.layoutManager = mLayoutManager
                //rvMain?.applyTopBarAndBottomBarInsets(requireContext())
                rvRecentHistory?.adapter = bucketParentAdapter
                rowList = (bucketRespModel?.data?.body?.rows as MutableList<RowsItem?>?)!!

                bucketParentAdapter?.addData(rowList!!)
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE
                val lastVisibleAdsMap:HashMap<Int, Int> = HashMap()
                var prevCenterPos = -1
                rvRecentHistory?.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val completeVisiblePosition = mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!
                        val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                        val lastVisiable: Int = mLayoutManager?.findLastCompletelyVisibleItemPosition()!!
                        val fCompleteVisible = mLayoutManager?.findFirstCompletelyVisibleItemPosition()!!

                        if (!rowList.isNullOrEmpty() && rowList?.size!! > completeVisiblePosition && completeVisiblePosition >= 0){
                            if (rowList?.get(completeVisiblePosition)?.itype == BucketChildAdapter.ROW_ITYPE_201){
                                if (!rowList?.get(completeVisiblePosition)?.items.isNullOrEmpty() && rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible == false){
                                    rowList?.get(completeVisiblePosition)?.items?.get(0)?.data?.isVisible = true
                                    lastVisibleAdsMap.put(rowList?.get(completeVisiblePosition)?.itype!!, completeVisiblePosition)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        bucketParentAdapter?.notifyItemChanged(completeVisiblePosition)
                                    }


                                }
                            }
                        }

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
                            if(!fromBucket?.equals(toBucket,true)!!){
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

                    }
                })
            }
        }
    }



    private var mLastClickTime: Long = 0
    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
            setLog("mLastClickTime", "return")
            return
        }
        setLog("mLastClickTime", "continue")
        mLastClickTime = SystemClock.elapsedRealtime()
        setEventModelDataAppLevel(parent.items?.get(childPosition)?.data?.id!!,parent.items?.get(childPosition)?.data?.title!!,parent?.heading!!)
        sendArtworkTappedEvent(parent,parentPosition,childPosition,headItemsItem)
        if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("110",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("77777",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)) {
            if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true) || parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)){
                playableItem = parent
                playableItemPosition = childPosition
                getPlayableContentUrl(parent.items?.get(childPosition)?.data?.id!!)
            }else {
                val songData =
                    bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.items!!.get(
                        childPosition
                    )!!.data
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
            }
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
        } else {
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            tracksViewModel.onCleanup()
            (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
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
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
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

    private fun setUpPlayableContentListViewModel() {
        try {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)
        }catch (e:Exception){

        }

    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    baseIOScope.launch {
                                        setPlayableContentListData(it?.data)
                                    }
                                }else{
                                    playableItemPosition = playableItemPosition +1
                                    if (playableItemPosition < playableItem?.items?.size!!) {
                                        if (CONTENT_TYPE == Constant.CONTENT_MOOD_RADIO || CONTENT_TYPE == Constant.CONTENT_ON_DEMAND_RADIO || CONTENT_TYPE == Constant.CONTENT_ARTIST_RADIO) {
                                            getPlayableContentUrl(moodRadioListRespModel?.get(playableItemPosition)?.data?.id!!)
                                        } else {
                                            getPlayableContentUrl(playableItem.items?.get(playableItemPosition)?.data?.id!!)
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
    }

    /**
     * get Playable url for song : 33, 35
     *
     * @param id
     */
    private fun getPlayableMoodRadioList(id:String, type:Int){
        if (ConnectionUtil(context).isOnline) {
            if (type == Constant.CONTENT_MOOD_RADIO){
                playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)!!.data?.id!!)
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
            }else if (type == Constant.CONTENT_ARTIST_RADIO){
                playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)!!.data?.id!!)
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
            }else {
                playableContentViewModel?.getOnDemandRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)!!.data?.id!!)
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
        baseIOScope.launch {
            if (playableContentModel != null) {
                setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
                songDataList = arrayListOf()


                if (playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(
                        Constant.PLAYER_MOOD_RADIO,
                        true
                    ) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(
                        Constant.PLAYER_ON_DEMAND_RADIO,
                        true
                    ) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(
                        Constant.PLAYER_ARTIST_RADIO,
                        true
                    )
                ) {
                    for (i in moodRadioListRespModel?.indices!!) {

                        if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(
                                i
                            )?.data?.id
                        ) {
                            setRadioDataList(
                                playableContentModel,
                                moodRadioListRespModel?.get(i),
                                playableItemPosition,
                                playableItem
                            )
                        } else if (i > playableItemPosition) {
                            setRadioDataList(
                                null,
                                moodRadioListRespModel?.get(i),
                                playableItemPosition,
                                playableItem
                            )
                        }

                    }
                    val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                    BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)
                    tracksViewModel.prepareTrackPlayback(0)
                } else {
                    for (i in playableItem.items!!.indices) {

                        if (playableContentModel?.data?.head?.headData?.id == playableItem.items?.get(
                                i
                            )?.data?.id
                        ) {
                            setPlayableDataList(
                                playableContentModel,
                                playableItem,
                                playableItemPosition
                            )
                        } else if (i > playableItemPosition) {
                            setPlayableDataList(null, playableItem, i)
                        }

                    }
                    val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                    BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)
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
        val track: Track = Track()
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

        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)){
            track.playerType = playableItem.items?.get(position)?.data?.type
        }else{
            track.playerType = ""
        }
        if (activity != null && !TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)) {
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_PODCAST){
                track.contentType = ContentTypes.PODCAST.value
            }else if (playType == Constant.CONTENT_MUSIC){
                track.contentType = ContentTypes.AUDIO.value
            }
        }
        if (!TextUtils.isEmpty(playableItem.heading)){
            track.heading = playableItem.heading
        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.playble_image)){
            track.image = playableItem.items?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.image)){
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
        songDataList.add(track)
    }

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: RowsItem
    ) {
        val track: Track = Track()


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
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableItem1.items?.get(position)?.data?.type)){
            track.playerType = playableItem1.items?.get(position)?.data?.type
        }else{
            track.playerType = Constant.PLAYER_RADIO
        }
        if (!TextUtils.isEmpty(playableItem1.heading)){
            track.heading = playableItem1.heading
        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
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
        songDataList.add(track)
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        //setLog("MoreItemHeading", heading)
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        val dataMap= HashMap<String,String>()
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
        dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,
            sourcePage)
        EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && requireContext()!=null){
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.transparent)
            bucketParentAdapter?.playOriginalPlayer()
        }else{
            bucketParentAdapter?.pauseOriginalPlayer()
        }
    }

    private fun setLocalBroadcast(){
        if (isAdded && context != null){
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT));
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.VIDEO_MAIN_TAB_EVENT));
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT));
        }
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                /*when((requireActivity() as MainActivity).getAudioPlayerPlayingStatus()){
                    Constant.playing -> {
                        CommonUtils.setLog("MainActivity", "PlayerEvent-playing")
                    }
                    Constant.pause ->{
                        CommonUtils.setLog("MainActivity", "PlayerEvent-pause")
                    }
                    else -> {
                        CommonUtils.setLog("MainActivity", "PlayerEvent-none")
                    }
                }*/
                setLog("MoengageNudgeView", "VideoTabFragment-onLocalBroadcastEventCallBack-setViewBotoomSpacing called-title-${headItemsItem?.title}")
                setViewBottomSpacing()
            } else if (context != null && event == Constant.VIDEO_MAIN_TAB_HIDDEN_RESULT_CODE) {
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser){
            if (isAdded){
                setViewBottomSpacing()
            }
            bucketParentAdapter?.playOriginalPlayer()
        }else{
            bucketParentAdapter?.pauseOriginalPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        bucketParentAdapter?.pauseOriginalPlayer()
    }

    override fun onDismiss(inAppData: InAppData) {
        if (nudge != null){
            //setLog("MoengageNudgeView", "VideoTabFragment-onDismiss-inAppData-${inAppData.activity.componentName.className}")
            //setLog("MoengageNudgeView", "VideoTabFragment-onDismiss-inAppData-${inAppData.inAppCampaign.campaignName}")
            isNudgeViewVisible = nudge.isShown
            setLog("MoengageNudgeView", "VideoTabFragment-onDismiss-isNudgeShown-${isNudgeViewVisible}")
            setLog("MoengageNudgeView", "VideoTabFragment-onDismiss-title-${headItemsItem?.title}")
            setLog("MoengageNudgeView", "VideoTabFragment-onDismiss-setViewBotoomSpacing called-title-${headItemsItem?.title}")
            setViewBottomSpacing()
            if (activity != null){
                Utils.setWindowProperty(requireActivity())
            }
        }
    }

    override fun onShown(inAppData: InAppData) {
        if (nudge != null){
            //setLog("MoengageNudgeView", "VideoTabFragment-onShown-inAppData-${inAppData.activity.componentName.className}")
            //setLog("MoengageNudgeView", "VideoTabFragment-onShown-inAppData-${inAppData.inAppCampaign.campaignName}")
            isNudgeViewVisible = nudge.isShown
            setLog("MoengageNudgeView", "VideoTabFragment-onShown-isNudgeShown-${isNudgeViewVisible}")
            setLog("MoengageNudgeView", "VideoTabFragment-onShown-title-${headItemsItem?.title}")
            setLog("MoengageNudgeView", "VideoTabFragment-onShown-setViewBotoomSpacing called-title-${headItemsItem?.title}")
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

                    setLog("MoengageNudgeView", "VideoTabFragment-setViewBottomSpacing-true-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                }else{
                    val nudgeHeight = 0
                    var rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_120)
                    rvTopPadding += nudgeHeight

                    setLog("MoengageNudgeView", "VideoTabFragment-setViewBottomSpacing-false-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                }
            }
        }
    }
}