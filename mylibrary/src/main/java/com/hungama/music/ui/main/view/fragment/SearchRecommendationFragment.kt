package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Util
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.CategoryClickedEvent
import com.hungama.music.eventanalytic.eventreporter.PagescrollSearchreco
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SearchRecommendedAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.R
import kotlinx.android.synthetic.main.fr_search_bucket_data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SearchRecommendationFragment(
    val searchRespModel: SearchRecommendationModel,
    val tabId: Int,
    val isRecommanded:Boolean = false) : BaseFragment(), SearchRecommendedAdapter.SearchResult, TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack {
    lateinit var searchRecommendedAdapter : SearchRecommendedAdapter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var CONTENT_TYPE = 0
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableItemPosition = 0
    var playableItem = SearchRecommendationModel.SearchRecommendationModelItem()
    private var isLastDurationPlay=false
    var startPosition = 0
    var endPosition = 0

    override fun initializeComponent(view: View) {
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)


        // initialize staggered grid layout manager
       val layoutManager = StaggeredGridLayoutManager(
            2, // span count
            StaggeredGridLayoutManager.VERTICAL // orientation
        ).apply {
            // specify the layout manager for recycler view
            rvTab.layoutManager = this
        }


        // finally, data bind the recycler view with adapter
        searchRecommendedAdapter = SearchRecommendedAdapter(requireContext(),this, searchRespModel)
        rvTab.adapter = searchRecommendedAdapter

        if (!searchRespModel.isNullOrEmpty()){
            rvTab?.visibility = View.VISIBLE
            searchRecommendedAdapter.setSearchlist(searchRespModel!!)
        }else{
            rvTab?.visibility = View.GONE
        }

        setUpPlayableContentListViewModel()
        rvTab?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                setLog("SCROLL_STATE", );
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                    setLog("SCROLL_STATE","SCROLL_STATE_FLING");
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    setLog("SCROLL_STATE","SCROLL_STATE_TOUCH_SCROLL");
                    //slideUp(party_view);
                    // Do something
                } else if (newState==AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    // Do something
                    //slideDown(party_view);
                    setLog("SCROLL_STATE","SCROLL_STATE_IDLE");
                    CoroutineScope(Dispatchers.IO).launch {
                        if (startPosition>=0 && endPosition >= 0) {
                            if (startPosition<10)
                                startPosition = 1
                            else
                                startPosition /= 10
                            if (endPosition < 10)
                                endPosition = 1
                            else
                                endPosition /= 10

                        val dataMap = HashMap<String, String>()
                        dataMap.put(EventConstant.FROM_POSITION, startPosition.toString())
                        dataMap.put(EventConstant.TO_POSITION, "" + endPosition)
                        dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY, MainActivity.lastItemClicked + "_" + MainActivity.headerItemName)

                        setLog("PageType","Type${dataMap}")

                            EventManager.getInstance().sendEvent(PagescrollSearchreco(dataMap))
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                setLog("SCROLL_POSITION",dx.toString() + " " + dy.toString());
                val into = IntArray(layoutManager.spanCount)
                layoutManager.findFirstVisibleItemPositions(into)

                startPosition = Int.MAX_VALUE
                for (pos in into) {
                    startPosition = pos.coerceAtMost(startPosition)
                }
                layoutManager.findLastCompletelyVisibleItemPositions(into)
                endPosition = Int.MAX_VALUE
                for (pos in into) {
                    endPosition = pos.coerceAtMost(endPosition)
                }

                setLog(
                    TAG,
                    "SCROLL_STATE:${startPosition} lastVisiable:${endPosition}"
                )

            }
        })
        CommonUtils.setPageBottomSpacing(rvTab, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_180),
            resources.getDimensionPixelSize(R.dimen.dimen_8), 0)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_all, container, false)
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
                                //setLog(TAG, "isViewLoading $it")

                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setPlayableContentListData(it?.data)
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
                                    moodRadioListRespModel = it?.data
                                    if (it?.data?.size!! > 0) {
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
    var moodRadioListRespModel: MoodRadioListRespModel?=null
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
            if(playableItem?.data?.type?.toInt()==33 || playableItem?.data?.type?.toInt()==35 || playableItem?.data?.type?.toInt()==36){
                for (i in moodRadioListRespModel?.indices!!){
                    if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(i)?.data?.id){
                        setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
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
            }else{

                    if (playableContentModel?.data?.head?.headData?.id == playableItem?.data?.id){
                        setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
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
        playableItem: SearchRecommendationModel.SearchRecommendationModelItem,
        position:Int
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

        if (!TextUtils.isEmpty(playableItem?.data?.type.toString())){
            track.playerType = playableItem?.data?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (activity != null && !TextUtils.isEmpty(playableItem?.data?.type.toString())) {
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_PODCAST){
                track.contentType = ContentTypes.PODCAST.value
            }else if (playType == Constant.CONTENT_MUSIC){
                track.contentType = ContentTypes.AUDIO.value
            }
        }
        /*if (!TextUtils.isEmpty(playableItem.heading)){
            track.heading = playableItem.heading
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem.data?.misc?.explicit != null){
            track.explicit = playableItem.data?.misc?.explicit!!
        }
        if (playableItem.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.data?.misc?.restricted_download!!
        }
        if (playableItem.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.data?.misc?.attributeCensorRating.toString()
        }

        songDataList.add(track)
    }

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: SearchRecommendationModel.SearchRecommendationModelItem?
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

        if (!TextUtils.isEmpty(playableItem1?.data?.type.toString())){
            track.playerType = playableItem1?.data?.type.toString()
        }else{
            track.playerType = Constant.PLAYER_RADIO
        }
        /*if (!TextUtils.isEmpty(playableItem1.heading)){
            track.heading = playableItem1.heading
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem1?.data?.misc?.explicit != null){
            track.explicit = playableItem1.data?.misc?.explicit!!
        }
        if (playableItem1?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem1.data?.misc?.restricted_download!!
        }
        if (playableItem1?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem1.data?.misc?.attributeCensorRating.toString()
        }
        songDataList.add(track)
    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
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
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun onItemDetailPageRedirection(searchdata: SearchRecommendationModel.SearchRecommendationModelItem, position: Int) {

        val type=searchdata?.data?.type.toString()
        val image=searchdata?.data?.image
        val id=searchdata?.data?.id
        val title=searchdata?.data?.title
        val varientType=searchdata?.data?.variant

        if (type.equals("0", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    bundle.putBoolean("varient", true)
                }else{
                    bundle.putBoolean("varient", false)
                }
            }else{
                bundle.putBoolean("varient", false)
            }
            val artistDetailsFragment = ArtistDetailsFragment()
            artistDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, artistDetailsFragment, false)

        } else if (type.equals("1", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)

            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, albumDetailFragment, false)

        } else if (type.equals("15", true)
            || type.equals("44444", true)
            || type.equals("66666", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)

            val collectionDetailsFragment = CollectionDetailsFragment()
            collectionDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, collectionDetailsFragment, false)
        } else if (type.equals("19", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            if (searchdata?.data?.images != null && searchdata?.data?.images?.size!! > 0 && searchdata?.itype == 42
            ) {
                bundle.putStringArrayList(
                    "imageArray",
                    searchdata?.data?.images as java.util.ArrayList<String>?
                )
            }

            if (searchdata?.data?.variant_images != null && searchdata?.data?.variant_images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "variant_images",
                    searchdata?.data?.variant_images as java.util.ArrayList<String>?
                )
            }


            bundle.putString("id", id)
            bundle.putString("playerType", type)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val chartDetailFragment = ChartDetailFragment.newInstance(varient)
            chartDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, chartDetailFragment, false)

        } else if (type.equals("20", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            if(searchdata?.data?.misc?.artistid!=null){
                bundle.putString("artistid", searchdata?.data?.misc?.artistid)
            }

            bundle.putString("playerType", type)
            bundle.putBoolean("varient", true)

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val eventDetailFragment = EventDetailFragment()
            eventDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, eventDetailFragment, false)

        }else if (type.equals("25", true)) {
            val bundle = Bundle()
            bundle.putString("heading", searchdata?.data?.title)
            val categoryDetailFragment = CategoryDetailFragment(
                searchdata?.data?.id!!.toString(),
                10
            )
            categoryDetailFragment.arguments = bundle
            addFragment(R.id.fl_container, this, categoryDetailFragment, false)

            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.TYPE_EPROPERTY,""+searchdata?.data?.type)
                hashMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+Utils.getContentTypeName(searchdata?.data?.type.toString()))
                hashMap.put(EventConstant.CATEGORYNAME_EPROPERTY,""+searchdata?.data?.title)
                //hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+MainActivity.lastItemClicked+"_"+MainActivity.headerItemName+"_"+parent?.heading)

                EventManager.getInstance().sendEvent(CategoryClickedEvent(hashMap))
            }

        }
        else if (type.equals(
                "93",
                true
            ) || type.equals(
                "4",
                true
            )  || type.equals(
                "65",
                true
            ) || type.equals("66", true)
        ) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            bundle.putBoolean("varient", true)

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                    bundle.putString("variant_image", searchdata?.data!!.variant_images?.get(0))
                }
            }
            val movieDetailsFragment = MovieV1Fragment(varient)
            movieDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, movieDetailsFragment, false)

        } else if (type.equals(
                "96",
                true
            ) || type.equals(
                "97",
                true
            ) || type.equals(
                "98",
                true
            ) || type.equals(
                "102",
                true
            ) || type.equals("107", true)
        ) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putParcelable("child_item", searchdata)
            if (position % 2 == 0)
                bundle.putBoolean("varient", true)
            else
                bundle.putBoolean("varient", false)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            bundle.putString("playerType", type)
            val tvShowDetailsFragment = TvShowDetailsFragment(varient)
            tvShowDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, tvShowDetailsFragment, false)

        } else if (type.equals("109", true)) {

            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            //bundle.putParcelable("details", parent)
            //bundle.putInt("childPosition", childPosition)
            if (position % 2 == 0)
                bundle.putBoolean("varient", true)
            else
                bundle.putBoolean("varient", false)


            val podcastDetailsFragment = PodcastDetailsFragment()
            podcastDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, podcastDetailsFragment, false)

        } else if (type.equals("55555", true)) {

            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            if (searchdata?.data?.images != null && searchdata.data?.images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "imageArray",
                    searchdata?.data?.images as java.util.ArrayList<String>?
                )
            }

            if (searchdata?.data?.variant_images != null && searchdata?.data?.variant_images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "variant_images",
                    searchdata?.data?.variant_images as java.util.ArrayList<String>?
                )
            }

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
            playlistDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, playlistDetailFragment, false)

        } else if (type.equals(
                "22",
                true
            ) || type.equals(
                "53",
                true
            ) || type.equals("88888", true)
        ) {
            if (activity != null && activity is MainActivity){
                (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
            }
            val bundle = Bundle()
            bundle.putString("id", id)

            val videoDetailsFragment = MusicVideoDetailsFragment()
            videoDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, videoDetailsFragment, false)
        } else if (type.equals("99999", true)) {
            var varient = 1
            val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                override fun backPressItem(status: Boolean) {
                    setUpPlayableContentListViewModel()
                }

            })
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            playlistDetailFragment.arguments = bundle
            addFragment(
                R.id.fl_container,
                this,
                playlistDetailFragment,
                false
            )

        } else {
            Utils.showSnakbar(requireContext(),requireView(), false, "coming soon")
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvTab, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_180),
                    resources.getDimensionPixelSize(R.dimen.dimen_8), 0)
            }
        }
    }

    override fun SearchItemClick(
        searchdata: SearchRecommendationModel.SearchRecommendationModelItem,
        position: Int
    ) {
        if (searchdata?.data?.type?.toInt()==21||searchdata?.data?.type?.toInt()==110||searchdata?.data?.type?.toInt()==77777||searchdata?.data?.type?.toInt()==34) {
            if (searchdata?.data?.type?.toInt()==21 || searchdata?.data?.type?.toInt()==34){
                playableItem = searchdata
                playableItemPosition = position
                getPlayableContentUrl(searchdata?.data?.id!!)
            }else if (searchdata?.data?.type?.toInt()==110){
                playableItem = searchdata
                playableItemPosition = position
                CONTENT_TYPE = Constant.CONTENT_PODCAST
                getPlayableContentUrl(searchdata?.data?.id!!)
            }


        }else if (searchdata?.data?.type?.toInt()==33){
            playableItem = searchdata
            playableItemPosition = position
            CONTENT_TYPE = Constant.CONTENT_MOOD_RADIO
            getPlayableMoodRadioList(searchdata?.data?.moodid!!,
                Constant.CONTENT_MOOD_RADIO
            )

        }else if (searchdata?.data?.type?.toInt()==35){
            playableItem = searchdata
            playableItemPosition = position
            CONTENT_TYPE = Constant.CONTENT_ON_DEMAND_RADIO
            getPlayableMoodRadioList(searchdata?.data?.id!!,
                Constant.CONTENT_ON_DEMAND_RADIO
            )
        }else if (searchdata?.data?.type?.toInt()==36){
            playableItem = searchdata
            playableItemPosition = position
            CONTENT_TYPE = Constant.CONTENT_ARTIST_RADIO
            getPlayableMoodRadioList(searchdata?.data?.id!!,
                Constant.CONTENT_ARTIST_RADIO
            )
        }else {
            onItemDetailPageRedirection(searchdata, position)
        }
    }
}