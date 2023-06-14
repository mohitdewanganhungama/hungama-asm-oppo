package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.media3.common.util.Util
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketChildAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.MoreBucketViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.R
import kotlinx.android.synthetic.main.fragment_more_bucket_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MoreBucketListFragment : BaseFragment(), TracksContract.View, BaseActivity.OnLocalBroadcastEventCallBack {
    var selectedMoreBucket: RowsItem? = RowsItem()
    var moreBucketViewModel = MoreBucketViewModel()
    var moreBucketDataModel = MoreBucketDataModel()
    var moreBucketDataList = ArrayList<BodyRowsItemsItem?>()
    var bucketChildAdapter: BucketChildAdapter? = null
    var playableItem = RowsItem()
    var playableItemPosition = 0
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel?=null
    var page = 2
    var limit = 10
    lateinit var layoutManager : LinearLayoutManager
    var CONTENT_TYPE = 0
    private var isLastDurationPlay=false
    var gridCount = -1
    var moodRadioListRespModel: MoodRadioListRespModel?=null
    var lastTotalItemsSize = 0

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more_bucket_list, container, false)
    }

    override fun initializeComponent(view: View) {
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        if (arguments != null){
            if (requireArguments().containsKey("selectedMoreBucket")){
                selectedMoreBucket = requireArguments().getParcelable<RowsItem>("selectedMoreBucket")!!
            }
        }
        if (!TextUtils.isEmpty(selectedMoreBucket?.heading!!)){
            tvHeading.text = selectedMoreBucket?.heading!!
        }

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        img_back.setOnClickListener {
            selectedMoreBucket!!.heading
            CommonUtils.PageViewEvent("","","","",
                selectedMoreBucket!!.heading.toString(),
                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName,
                MainActivity.headerItemPosition.toString())
            backPress()
        }
        layoutManager = LinearLayoutManager(requireContext())
        setLog("MoreBucketListFragment", "initializeComponent-selectedMoreBucket?.items.size-${selectedMoreBucket?.items?.size}")
        if (selectedMoreBucket != null && !selectedMoreBucket?.items.isNullOrEmpty()) {
            setLog(
                "MoreBucketListFragment",
                "initializeComponent-moreBucketDataList.size-${moreBucketDataList.size}"
            )
            selectedMoreBucket?.items?.let {
                if (it.get(0)?.itype == BucketChildAdapter.ROW_ITYPE_21) {
                    it.forEachIndexed { index, bodyRowsItemsItem ->
                        bodyRowsItemsItem?.data?.sequence = index
                    }
                    lastTotalItemsSize = it.size
                }
                setData3(it)
            }

            CoroutineScope(Dispatchers.IO).launch {
                CommonUtils.PageViewEventMore(
                    "",
                    "",
                    "", selectedMoreBucket!!.heading.toString(),
                    MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + MainActivity.subHeaderItemName,
                    "viewall_" + selectedMoreBucket!!.heading.toString().replace(" ","").lowercase(),
                    ""
                )
                MainActivity.subHeaderItemName = ""

                setLog("printContentType"," " +selectedMoreBucket!!.heading.toString())

            }
        }

        setUpPlayableContentListViewModel()

        CommonUtils.setPageBottomSpacing(rvBucketItem, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_20), resources.getDimensionPixelSize(R.dimen.dimen_66),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }





    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel(page: Int, limit: Int) {
        if (ConnectionUtil(activity).isOnline) {
            moreBucketViewModel = ViewModelProvider(
                this
            ).get(MoreBucketViewModel::class.java)
            moreBucketViewModel.getMoreBucketListData(requireActivity(),selectedMoreBucket?.id!!, page, selectedMoreBucket?.bucketQuery!!, limit, selectedMoreBucket?.type)
                .observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if(it?.data!=null){

                                    setLog("MoreBucketListFragment", "setData2 setUpViewModel size-${it?.data?.data?.body?.rows?.size} moreBucketDataList size:${moreBucketDataList?.size}")

                                    if (!it.data.data?.body?.rows.isNullOrEmpty() && it.data.data?.body?.rows?.get(0)?.itype == BucketChildAdapter.ROW_ITYPE_21){
                                        it.data.data?.body?.rows?.forEachIndexed { index, bodyRowsItemsItem ->
                                            bodyRowsItemsItem?.data?.sequence = lastTotalItemsSize+index
                                        }
                                        lastTotalItemsSize += it.data.data?.body?.rows?.size!!
                                    }
                                    setData2(it?.data)
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
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    var isLastPage: Boolean = false
    var isLoading: Boolean = false


    private fun onUserClickEvent(child: List<BodyRowsItemsItem?>, childPosition: Int) {
        selectedMoreBucket?.items = ArrayList()
        selectedMoreBucket?.items?.addAll(child)
        val parent = selectedMoreBucket
        val parentPosition = 0
        setEventModelDataAppLevel(parent?.items?.get(childPosition)?.data?.id!!,parent.items?.get(childPosition)?.data?.title!!,parent?.heading!!)

        if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true)
            ||parent.items!!.get(childPosition)?.data?.type!!.equals("110",true)
            ||parent.items!!.get(childPosition)?.data?.type!!.equals("77777",true)
            ||parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)) {

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
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }
    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        try {
            if (context != null && activity != null){
                val intent = Intent(requireActivity(), AudioPlayerService::class.java)
                intent.action = AudioPlayerService.PlaybackControls.PLAY.name
                intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
                intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
                Util.startForegroundService(requireActivity(), intent)
                (requireActivity() as MainActivity).reBindService()
            }
        }catch (e:Exception){

        }
    }

    override fun getViewActivity(): AppCompatActivity {
        return requireActivity() as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (requireActivity() as AppCompatActivity).applicationContext
    }

    private fun setData2(it: MoreBucketDataModel) {
        moreBucketDataModel = it
        if (!moreBucketDataModel?.data?.body?.rows.isNullOrEmpty()){
            var bucketsItemsList:ArrayList<BodyRowsItemsItem?>?=null
            moreBucketDataModel?.data?.body?.rows?.let { it1 ->
                bucketsItemsList=setAdsDataBetweenItem(
                    it1
                )
            }


            moreBucketDataModel?.data?.body?.rows = bucketsItemsList
            bucketsItemsList?.let {
                setLog("MoreBucketListFragment", "setData2-it.size-before-${bucketsItemsList?.size}")
                setLog("MoreBucketListFragment", "setData2-moreBucketDataList.size-before-${moreBucketDataList.size}")



                bucketChildAdapter?.addData(bucketsItemsList!!)

                setLog("MoreBucketListFragment", "setData2 bucketsItemsList size-${bucketsItemsList?.size} moreBucketDataList size:${moreBucketDataList?.size}")
            }


        }else{
            isLastPage=true
        }

        isLoading = false


        rlHead.visibility = View.VISIBLE
        rvBucketItem.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
    }

    private fun setData3(it: ArrayList<BodyRowsItemsItem?>) {
        moreBucketDataList = ArrayList<BodyRowsItemsItem?>()

        setLog("MoreBucketListFragment", "setData3-it.size-before-${it.size}")
        setLog("MoreBucketListFragment", "setData3-moreBucketDataList.size-before-${moreBucketDataList.size}")
        val bucketsItemsList = setAdsDataBetweenItem(it)
        bucketsItemsList.let {
            moreBucketDataList?.addAll(bucketsItemsList)
        }
        setLog("MoreBucketListFragment", "setData3-moreBucketDataList.size-after-${moreBucketDataList.size}")
        if (!moreBucketDataList.isNullOrEmpty()) {

            bucketChildAdapter = BucketChildAdapter(
                requireContext(),moreBucketDataList, Constant.ORIENTATION_VERTICAL,object : BucketChildAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int, view: View?) {
                        onUserClickEvent(moreBucketDataList, childPosition)
                    }

                    override fun onInAppSubmitClick(
                        childPosition: InAppSelfHandledModel?,
                        position: Int
                    ) {

                    }
                })
            var layoutManager: GridLayoutManager? = null
            if (selectedMoreBucket?.items?.get(0)?.itype == 7 || selectedMoreBucket?.items?.get(0)?.itype == 8 || selectedMoreBucket?.items?.get(0)?.itype == 21 || selectedMoreBucket?.items?.get(0)?.itype == 23 || selectedMoreBucket?.items?.get(0)?.itype == 18 || selectedMoreBucket?.items?.get(0)?.itype == 19 || selectedMoreBucket?.items?.get(0)?.itype == 3) {
                if(gridCount<0){
                    gridCount = 1
                }

                layoutManager = GridLayoutManager(
                    requireContext(),
                    gridCount
                )
            } else {
                if(gridCount<0){
                    gridCount = 2
                }

                layoutManager =
                    GridLayoutManager(requireContext(), gridCount)
            }
            if (selectedMoreBucket?.items?.get(0)?.itype == 23){
                rvBucketItem?.setPadding(resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_66),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_142))
                rvBucketItem?.requestLayout()
            }else{
                rvBucketItem?.setPadding(resources.getDimensionPixelSize(R.dimen.dimen_20),
                    resources.getDimensionPixelSize(R.dimen.dimen_66),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_142))
                rvBucketItem?.requestLayout()
            }
            /*rvBucketItem?.layoutManager =
                LinearLayoutManager(
                    activity,
                    LinearLayoutManager.VERTICAL,
                    false
                )*/

            rvBucketItem?.layoutManager = layoutManager
            rvBucketItem?.adapter = bucketChildAdapter
            layoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {

                    if (moreBucketDataList?.get(position)?.itype == 201){
                        setLog("spnaAds-1.1", selectedMoreBucket?.items?.get(0)?.itype.toString()+":- 1")
                        return gridCount
                    }else{
                        setLog("spnaAds-1.2", selectedMoreBucket?.items?.get(0)?.itype.toString()+":- "+layoutManager.spanCount)
                        return 1
                    }
                }
            }

            rvBucketItem?.visibility=View.VISIBLE
            rvBucketItem.invalidate()
            //addScrollerListener()


            rvBucketItem?.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }

                override fun loadMoreItems() {
                    isLoading = true
                    //you have to call loadmore items to get more data

                    setLog("Moremore", page.toString())
                    if (!TextUtils.isEmpty(selectedMoreBucket?.bucketQuery)){
                        setUpViewModel(page, limit)
                    }
                    page++
                }
            })
            rlHead.visibility = View.VISIBLE
            rvBucketItem.visibility = View.VISIBLE
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
        }
    }


    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        if (ConnectionUtil(context).isOnline) {

            if(playableContentViewModel==null){
                playableContentViewModel = ViewModelProvider(
                    this
                ).get(PlayableContentViewModel::class.java)
            }

            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            try {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    //setLog(TAG, "isViewLoading $it")

                                    if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                        setPlayableContentListData(it?.data!!)
                                    }else{
                                        playableItemPosition = playableItemPosition +1
                                        if (playableItem.items?.size!! > playableItemPosition ){
                                            if (CONTENT_TYPE == Constant.CONTENT_MOOD_RADIO || CONTENT_TYPE == Constant.CONTENT_ON_DEMAND_RADIO || CONTENT_TYPE == Constant.CONTENT_ARTIST_RADIO){
                                                if (moodRadioListRespModel?.size!! > playableItemPosition ){
                                                    getPlayableContentUrl(moodRadioListRespModel?.get(playableItemPosition)?.data?.id!!)
                                                }
                                            }else{
                                                getPlayableContentUrl(playableItem.items?.get(playableItemPosition)?.data?.id!!)
                                            }
                                        }

                                    }
                                }
                            }catch (e:Exception){

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
            if(playableContentViewModel==null){
                playableContentViewModel = ViewModelProvider(
                    this
                ).get(PlayableContentViewModel::class.java)
            }

            if (type == Constant.CONTENT_MOOD_RADIO){
                playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
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
            }else if (type == Constant.CONTENT_ARTIST_RADIO){
                playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
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

    private fun setUpPlayableContentListViewModel() {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)

    }








    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null ) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()
            isLastDurationPlay = false
            setLog(TAG, "onClick btn_next_play_mini size 1: ${songDataList?.size}")
            if(playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_MOOD_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ON_DEMAND_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ARTIST_RADIO, true)){
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
                BaseActivity.setTrackListData(songDataList)

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
                BaseActivity.setTrackListData(songDataList)

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
                BaseActivity.setTrackListData(songDataList)
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

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)){
            track.playerType = playableItem.items?.get(position)?.data?.type
        }else{
            track.playerType = Constant.MUSIC_PLAYER
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

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
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

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
    }

    private fun setAdsDataBetweenItem(bodyRowsItemsItem: ArrayList<BodyRowsItemsItem?>): ArrayList<BodyRowsItemsItem?> {
        val finalBodyRowsItemsItem:ArrayList<BodyRowsItemsItem?> = ArrayList()
        if (!bodyRowsItemsItem.isNullOrEmpty()){
            finalBodyRowsItemsItem.addAll(bodyRowsItemsItem)
            if (CommonUtils.isHomeScreenBannerAds()){
                if (finalBodyRowsItemsItem.get(0)?.itype == 7|| finalBodyRowsItemsItem.get(0)?.itype == 8 || finalBodyRowsItemsItem.get(0)?.itype == 21 || finalBodyRowsItemsItem.get(0)?.itype == 23 || finalBodyRowsItemsItem.get(0)?.itype == 18 || finalBodyRowsItemsItem.get(0)?.itype == 19 || finalBodyRowsItemsItem.get(0)?.itype == 3) {
                    if(gridCount<0){
                        gridCount = 1
                    }

                }else{
                    if(gridCount<0){
                        gridCount = 2
                    }

                }
                val adDisplayFirstPosition = CommonUtils.getHomescreenBannerAds().firstAdPositionAfterRows * gridCount
                val adDisplayPositionFrequency = CommonUtils.getHomescreenBannerAds().repeatFrequencyAfterRows * gridCount
                var adDisplayPosition = adDisplayFirstPosition

                var isFirstAds = true
                val adUnitIdList = arrayListOf(
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_1,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_2,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_3,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_4,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_5
                )
                val adTotalIds = adUnitIdList.size

                setLog("adInserted", "bodyRowsItemsItem size:${finalBodyRowsItemsItem?.size} adDisplayFirstPosition:${adDisplayFirstPosition} adDisplayPositionFrequency:${adDisplayPositionFrequency} adTotalIds:${adTotalIds}")

                var adIdCount = 0
                var i = 0
                var k = 0

                val iterator = finalBodyRowsItemsItem.listIterator()
                while (iterator.hasNext()) {
                    var isAdsAdded = false
                    if (k>0 && k % adDisplayPosition == 0) {
                        if (isFirstAds){
                            k = 0
                            isFirstAds = false
                            adDisplayPosition = adDisplayPositionFrequency

                            setLog("adInserted","isFirstAds:${isFirstAds} adDisplayPosition:${adDisplayPosition} adTotalIds:${adTotalIds} k:${k}")
                        }
                        //setLog("adInserted-2", i.toString())
                        //setLog("adInserted", "Befor==" + homeModel.data?.body?.rows?.get(i)?.heading)

                        setLog("adInserted","before adIdCount:${adIdCount} adTotalIds:${adTotalIds} adUnitIdList:${adUnitIdList} k:${k}")
                        val model= BodyRowsItemsItem()

                        model.itype= BucketChildAdapter.ROW_ITYPE_201
                        model.data=BodyDataItem()
                        if (adTotalIds > adIdCount){

                            //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                            model.adUnitId = adUnitIdList.get(adIdCount)
                            adIdCount++
                            setLog("adInserted", "adUnitId:${model.adUnitId}")
                        }else{
                            adIdCount = 0
                            model.adUnitId = adUnitIdList.get(adIdCount)
                            setLog("adInserted", "adUnitId:${model.adUnitId}")
                            //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                            adIdCount++
                        }
                        iterator.add(model)
                        isAdsAdded = true
                        setLog("adInserted","iterator previousIndex:${iterator?.previousIndex()}")
                        setLog("adInserted","after adIdCount:${adIdCount} adTotalIds:${adTotalIds} adUnitIdList:${adUnitIdList} k:${k}")
                    }
                    val item = iterator.next()
                    /*if (!isAdsAdded){
                        setLog("adInserted","ELSE-itype-${item?.itype}-title-${item?.data?.title}-adUnitId-${item?.adUnitId}")
                        if (item?.itype == BucketChildAdapter.ROW_ITYPE_201){
                            setLog("adInserted","ELSE-removed-itype-${item?.itype}-adUnitId-${item?.adUnitId}")
                            iterator.remove()
                        }
                    }*/
                    i++
                    k++
                }
            }
        }
        return finalBodyRowsItemsItem!!

    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvBucketItem, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_20), resources.getDimensionPixelSize(R.dimen.dimen_66),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}