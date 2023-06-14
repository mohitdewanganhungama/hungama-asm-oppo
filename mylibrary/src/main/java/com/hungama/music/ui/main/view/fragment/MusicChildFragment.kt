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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.Util
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.hungama.music.ui.main.viewmodel.MusicViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
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
 * Use the [MusicChildFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MusicChildFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View, BucketParentAdapter.OnMoreItemClick,
    BaseActivity.OnLocalBroadcastEventCallBack, InAppLifeCycleListener {
    var bucketRespModel: HomeModel? = null
    private var storyUsersList: ArrayList<BodyDataItem>? = null
    private var bucketParentAdapter: BucketParentAdapter? = null
    private var parentPos: Int = 0
    var songsList = ArrayList<MusicModel>()
    var rowList: MutableList<RowsItem?>? = null

    var musicViewModel: MusicViewModel? = null

    private lateinit var tracksViewModel: TracksContract.Presenter
    private var isLastDurationPlay=false
    var playableItem = RowsItem()
    var playableItemPosition = 0
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var CONTENT_TYPE = 0
    var headItemsItem: HeadItemsItem?=null
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""
    var isNudgeViewVisible = false




    companion object {
        val LAUNCH_STORY_DISPLAY_ACTIVITY = 101
        const val UPDATED_STORY_USER_LIST = "updatedStoryUserList"

        fun newInstance(mHeadItemsItem: HeadItemsItem?, bundle: Bundle) : MusicChildFragment {
            var fragment = MusicChildFragment()
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
        if(arguments!=null){
            if (arguments?.containsKey(Constant.BUNDLE_KEY_HEADITEMSITEM)!!){
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

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@MusicChildFragment)
        shimmerLayout?.visibility = View.VISIBLE
        shimmerLayout?.startShimmer()
        val musicHomeModel=HungamaMusicApp.getInstance().getCacheBottomTab("${Constant.BOTTOM_NAV_MUSIC}_${headItemsItem?.page}")
        setLog("MusicChildFragment", "setUpViewModel static call:${Constant.BOTTOM_NAV_MUSIC}_${headItemsItem?.page} homeModel:${musicHomeModel}")

        if(musicHomeModel!=null){
            setProgressBarVisible(false)
            setData(CommonUtils.checkProUserBucket(musicHomeModel))

            setLog("MusicChildFragment", "setUpViewModel static called")
        }else{

            setLog("MusicChildFragment", "setUpViewModel API called")
            setUpViewModel()
        }
        setUpPlayableContentListViewModel()

        setViewBottomSpacing()
        setLog("deepLinkUrl", "MusicChildFragment-initializeComponent--tabName=${headItemsItem?.page} && isCategory=$isCategoryPage && categoryName=$categoryName && categoryId=$categoryId")
        if (isCategoryPage && !TextUtils.isEmpty(categoryName) && !TextUtils.isEmpty(categoryId)){
            val bundle = Bundle()
            bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
            bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
            val categoryDetailFragment = CategoryDetailFragment(categoryId, 0)
            categoryDetailFragment.arguments = bundle
            addFragment(R.id.fl_container, this@MusicChildFragment, categoryDetailFragment, false)
        }
        MoEInAppHelper.getInstance().addInAppLifeCycleListener(this@MusicChildFragment)
        // setLog("MoengageNudgeView", "MusicChildFragment-initializeComponent-addInAppLifeCycleListener")
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
            musicViewModel = ViewModelProvider(
                this
            ).get(MusicViewModel::class.java)


            if (ConnectionUtil(activity).isOnline) {
                var url=""
                setLog(TAG, "setUpViewModel: $headItemsItem")
                if(headItemsItem!=null&&!TextUtils.isEmpty(headItemsItem?.page)){
                    url= WSConstants.METHOD_DETAIL_PAGE+headItemsItem?.page+"?lang="+ SharedPrefHelper.getInstance().getLanguage()
                    setLog(TAG, "setUpViewModel url: $url")
                }
                musicViewModel?.getMusicList(requireActivity(),url)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                HungamaMusicApp.getInstance().setCacheBottomTab("${Constant.BOTTOM_NAV_MUSIC}_${headItemsItem?.page}", it?.data!!)
                                setData(CommonUtils.checkProUserBucket(it?.data))

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

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        try {
            if (context != null && ConnectionUtil(context).isOnline) {
                playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    //setLog(TAG, "isViewLoading $it")
                                    if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                        baseIOScope.launch {
                                            setPlayableContentListData(it?.data!!)
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
                    this@MusicChildFragment,
                    this@MusicChildFragment,
                    Constant.LISTEN_TAB,
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
        setLog("clickedItem", "MusicChildFragment - onParentItemClick() - ${parent.items?.get(childPosition)?.data?.type}")

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
        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("36", true)) {
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_ARTIST_RADIO
            getPlayableMoodRadioList(
                parent.items?.get(childPosition)?.data?.id!!,
                Constant.CONTENT_ARTIST_RADIO
            )
        } else {
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
        }
    }

    /**
     * get Playable url for song : 33, 35
     *
     * @param id
     */
    private fun getPlayableMoodRadioList(id:String, type:Int){
        if (isAdded && context != null) {
            if (ConnectionUtil(context).isOnline) {
                if (type == Constant.CONTENT_MOOD_RADIO){
                    playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
                        {
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
                }else if (type == Constant.CONTENT_ARTIST_RADIO){
                    playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
                        {
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
                        {
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
                }

            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            tracksViewModel.onCleanup()
            (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
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
        if(trackPlayStartPosition>0){
            intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        }
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        activity?.let {
            return activity as AppCompatActivity
        }

        return requireActivity() as AppCompatActivity
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

    var moodRadioListRespModel: MoodRadioListRespModel?=null


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            try {
                if (playableContentModel != null ) {
                    setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
                    songDataList = arrayListOf()
                    isLastDurationPlay = false

                    if(playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_MOOD_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ON_DEMAND_RADIO, true)|| playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ARTIST_RADIO, true)){
                        for (i in moodRadioListRespModel?.indices!!){

                            if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(i)?.data?.id){
                                setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                            }else if(i > playableItemPosition){
                                setRadioDataList(null, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                            }

                        }
                        val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                        BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)

                        if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&&HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                            HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                                if(it?.data?.id==playableContentModel?.data?.head?.headData?.id){
                                    if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                        tracksViewModel.prepareTrackPlayback(0, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))
                                    }else{
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

                            if (playableContentModel?.data?.head?.headData?.id == playableItem.items?.get(i)?.data?.id){
                                setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                            }else if(i > playableItemPosition){
                                setPlayableDataList(null, playableItem, i)
                            }

                        }
                        val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                        BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)
                        if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                            HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                                if(it?.data?.id==playableContentModel?.data?.head?.headData?.id){
                                    if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                        tracksViewModel.prepareTrackPlayback(0,TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))
                                    }else{
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
                    }


                }
            }catch (e:Exception){

            }
        }
    }

    var songDataList:ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: RowsItem,
        position:Int
    ) {
        if (!playableItem.items.isNullOrEmpty() && playableItem.items?.size!! > position){
            setLog(TAG, "setPlayableDataList: playableItem:${playableItem}")
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

            if (playableContentModel != null){
                track.urlKey = playableContentModel.data.head.headData.misc.urlKey
            }


            songDataList.add(track)
        }
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

            if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
                track.url = playableContentModel?.data?.head?.headData?.misc?.url
            }else{
                track.url = ""
            }

                if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
                    track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
                }else{
                    track.movierights = ""
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
            }else{
                track.playerType = Constant.PLAYER_RADIO
            }
            if(playableItem1.items?.get(position)?.data?.type.equals(Constant.PLAYER_MOOD_RADIO)){
                if (!TextUtils.isEmpty(playableItem1.items?.get(position)?.data?.title)){
                    track.heading = playableItem1.items?.get(position)?.data?.title
                }else{
                    track.heading = ""
                }
            }else{

                if(playableItem1?.keywords?.contains("Continue Watching")!!){
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

                }else if (!TextUtils.isEmpty(playableItem1.heading)){
                    track.heading = playableItem1.heading
                }else{
                    track.heading = ""
                }
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

        baseIOScope?.async {
            val dataMap= HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+headItemsItem?.title)
            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,
                MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }

    private fun setLocalBroadcast(){
        if (isAdded && context != null){
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT));
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver, IntentFilter(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT));
        }
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                when((requireActivity() as MainActivity).getAudioPlayerPlayingStatus()){
                    Constant.playing -> {
                        CommonUtils.setLog("MainActivity", "PlayerEvent-playing")
                    }
                    Constant.pause ->{
                        CommonUtils.setLog("MainActivity", "PlayerEvent-pause")
                    }
                    else -> {
                        CommonUtils.setLog("MainActivity", "PlayerEvent-none")
                    }
                }
                setLog("MoengageNudgeView", "MusicChildFragment-onLocalBroadcastEventCallBack-setViewBotoomSpacing called-title-${headItemsItem?.title}")
                setViewBottomSpacing()
            }else if (context != null && event == Constant.STICKY_ADS_VISIBILITY_CHANGE_RESULT_CODE){
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_STORY_DISPLAY_ACTIVITY && resultCode == AppCompatActivity.RESULT_OK) {

            if (activity != null){
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser){
            if (isAdded){
                setViewBottomSpacing()
            }
        }
    }

    override fun onDismiss(inAppData: InAppData) {
        if (nudge != null){
            //setLog("MoengageNudgeView", "MusicChildFragment-onDismiss-inAppData-${inAppData.activity.componentName.className}")
            //setLog("MoengageNudgeView", "MusicChildFragment-onDismiss-inAppData-${inAppData.inAppCampaign.campaignName}")
            isNudgeViewVisible = nudge.isShown
            setLog("MoengageNudgeView", "MusicChildFragment-onDismiss-isNudgeShown-${isNudgeViewVisible}")
            setLog("MoengageNudgeView", "MusicChildFragment-onDismiss-title-${headItemsItem?.title}")
            setLog("MoengageNudgeView", "MusicChildFragment-onDismiss-setViewBotoomSpacing called-title-${headItemsItem?.title}")
            setViewBottomSpacing()
            if (activity != null){
                Utils.setWindowProperty(requireActivity())
            }
        }
    }

    override fun onShown(inAppData: InAppData) {
        if (nudge != null){
            //setLog("MoengageNudgeView", "MusicChildFragment-onShown-inAppData-${inAppData.activity.componentName.className}")
            //setLog("MoengageNudgeView", "MusicChildFragment-onShown-inAppData-${inAppData.inAppCampaign.campaignName}")
            isNudgeViewVisible = nudge.isShown
            setLog("MoengageNudgeView", "MusicChildFragment-onShown-isNudgeShown-${isNudgeViewVisible}")
            setLog("MoengageNudgeView", "MusicChildFragment-onShown-title-${headItemsItem?.title}")
            setLog("MoengageNudgeView", "MusicChildFragment-onShown-setViewBotoomSpacing called-title-${headItemsItem?.title}")
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

                    setLog("MoengageNudgeView", "MusicChildFragment-setViewBottomSpacing-true-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                }else{
                    val nudgeHeight = 0
                    var rvTopPadding = resources.getDimensionPixelSize(R.dimen.dimen_120)
                    rvTopPadding += nudgeHeight

                    setLog("MoengageNudgeView", "MusicChildFragment-setViewBottomSpacing-false-rvTopPadding-$rvTopPadding - nudgeHeight-$nudgeHeight")
                    CommonUtils.setPageBottomSpacing(rvRecentHistory, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), rvTopPadding,
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                }
            }
        }
    }
}