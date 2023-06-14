package com.hungama.music.ui.main.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.media3.common.util.Util
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.CallSearchResultClicked
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.CategoryClickedEvent
import com.hungama.music.eventanalytic.eventreporter.PagescrollSearchresults
import com.hungama.music.eventanalytic.eventreporter.SearchResultFilterEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SearchAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.SearchViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.fr_search_bucket_data.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SearchAllFragment(
    val searchRespModel: SearchRespModel,
    val tabId: Int,
    val callSearchResultClicked: CallSearchResultClicked?,
    val isRecommanded:Boolean = false
) : BaseFragment(), SearchAdapter.SearchResult, TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var rowList:ArrayList<BodyRowsItemsItem> = ArrayList()
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var CONTENT_TYPE = 0
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableItemPosition = 0
    var playableItem = BodyRowsItemsItem()
    private var isLastDurationPlay=false

    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var page = 1
    var searchAdapter:SearchAdapter?=null
    var pageName = ""


    override fun initializeComponent(view: View) {
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        BaseActivity.setTrackView(tracksViewModel)
        rowList = ArrayList()
        if (tabId == SearchAllTabFragment.all){
            if (!searchRespModel?.data?.body?.all.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.all!!)

                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.all?.size,SearchAllTabFragment.lastSearchText.trim(),"All")
                }
            }

            pageName = "all"

        }else if (tabId == SearchAllTabFragment.song){
            if (!searchRespModel?.data?.body?.song.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.song!!)
                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.song?.size,SearchAllTabFragment.lastSearchText.trim(),"Song")
                }
            }
            pageName = "song"

        }else if (tabId == SearchAllTabFragment.album){
            if (!searchRespModel?.data?.body?.album.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.album!!)

                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.album?.size,SearchAllTabFragment.lastSearchText.trim(),"Album")
                }
            }
            pageName = "album"
        }else if (tabId == SearchAllTabFragment.artist){
            if (!searchRespModel?.data?.body?.artist.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.artist!!)


                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.artist?.size,SearchAllTabFragment.lastSearchText.trim(),"Artist")
                }
            }
            pageName = "artist"
        }else if (tabId == SearchAllTabFragment.podcast){
            if (!searchRespModel?.data?.body?.podcast.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.podcast!!)

                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.podcast?.size,SearchAllTabFragment.lastSearchText.trim(),"Podcast")
                }
            }
            pageName = "podscast"
        }else if (tabId == SearchAllTabFragment.radio){
            if (!searchRespModel?.data?.body?.radio.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.radio!!)

                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.radio?.size,SearchAllTabFragment.lastSearchText.trim(),"Radio")
                }
            }
            pageName = "radio"
        }else if (tabId == SearchAllTabFragment.playlist){
            if (!searchRespModel?.data?.body?.playlist.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.playlist!!)

                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.playlist?.size,SearchAllTabFragment.lastSearchText.trim(),"Playlist")
                }
            }
            pageName = "playlist"
        }else if (tabId == SearchAllTabFragment.movie){
            if (!searchRespModel?.data?.body?.movie.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.movie!!)


                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.movie?.size,SearchAllTabFragment.lastSearchText.trim(),"Movie")
                }
            }
            pageName = "movie"
        }else if (tabId == SearchAllTabFragment.tvshow){
            if (!searchRespModel?.data?.body?.tvshow.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.tvshow!!)


                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.tvshow?.size,SearchAllTabFragment.lastSearchText.trim(),"tvshow")
                }
            }
            pageName = "tvShow"
        }else if (tabId == SearchAllTabFragment.musicVideo){
            if (!searchRespModel?.data?.body?.musicvideo.isNullOrEmpty()){
                rowList?.addAll(searchRespModel?.data?.body?.musicvideo!!)

                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.musicvideo?.size,SearchAllTabFragment.lastSearchText.trim(),"musicvideo")
                }
            }
            pageName = "music_video"
        } else if (tabId == SearchAllTabFragment.shortVideo) {
            if (!searchRespModel?.data?.body?.shortvideo.isNullOrEmpty()) {
                rowList?.addAll(searchRespModel?.data?.body?.shortvideo!!)


                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.shortvideo?.size,SearchAllTabFragment.lastSearchText.trim(),"shortvideo")
                }
            }
            pageName = "short_video"
        }else if (tabId == SearchAllTabFragment.shortFilm) {
            if (!searchRespModel?.data?.body?.shortfilm.isNullOrEmpty()) {
                rowList?.addAll(searchRespModel?.data?.body?.shortfilm!!)


                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.shortfilm?.size,SearchAllTabFragment.lastSearchText.trim(),"shortfilm")
                }
            }
            pageName = "short_film"
        }else if (tabId == SearchAllTabFragment.liveEvent) {
            if (!searchRespModel?.data?.body?.liveEvent.isNullOrEmpty()) {
                rowList?.addAll(searchRespModel?.data?.body?.liveEvent!!)


                CoroutineScope(Dispatchers.IO).launch {
                    calSearchResultFilterEvent(SearchAllTabFragment.lastSearchText.trim(),""+searchRespModel?.data?.body?.liveEvent?.size,SearchAllTabFragment.lastSearchText.trim(),"liveEvent")
                }
            }
            pageName = "live_event"
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvTab.layoutManager = layoutManager
        rvTab?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var startPosition =0
            var endPosition =0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                    setLog("SCROLL_STATE","SCROLL_STATE_FLING");
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    setLog("SCROLL_STATE","SCROLL_STATE_TOUCH_SCROLL");
                    //slideUp(party_view);
                    // Do something
                } else if (newState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
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
                            dataMap.put(EventConstant.PAGENAME_EPROPERTY, pageName)

                            setLog("PageType","Type${dataMap}")

                            EventManager.getInstance().sendEvent(PagescrollSearchresults(dataMap))
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                    startPosition = layoutManager.findFirstVisibleItemPosition()
                    endPosition = layoutManager.findLastVisibleItemPosition()


                setLog(
                    TAG,
                    "SCROLL_STATE:${startPosition} lastVisiable:${endPosition}"
                )

            }
        })

        if (!rowList.isNullOrEmpty()) {
            rvTab?.visibility = View.VISIBLE
//            searchAdapter.setSearchlist(rowList!!)
            page = 0
            searchAdapter = SearchAdapter(requireContext(), this, rowList,tabId)
            rvTab.adapter = searchAdapter
            baseIOScope.launch {
                delay(2000)
                checkAllContentDownloadedOrNot(rowList)
            }

        } else {
            rvTab?.visibility = View.GONE
        }

        setUpPlayableContentListViewModel()
        CommonUtils.setPageBottomSpacing(
            rvTab,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_72)
        )
        setLog("setPageBottomSpacing", "SearchAllFragment-rvTab-"+rvTab.paddingBottom)

        if(tabId == SearchAllTabFragment.all){
            rvTab?.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }

                override fun loadMoreItems() {
                    isLoading = true
                    //you have to call loadmore items to get more data
                    page++
                    setLog("Moremore", page.toString())
                    callAllSearchPageWiseApi(SearchAllTabFragment.lastSearchText,page)
                }
            })
        }
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

    fun saveRecentSearchData(searchdata: BodyRowsItemsItem) {
        val recentSearchList = SearchAllTabFragment.getRecentSearchList()
        var isAlreadyAvailable = false
        if (!recentSearchList.isNullOrEmpty()){
            recentSearchList.forEach {
                if (!TextUtils.isEmpty(searchdata.data?.id) && !TextUtils.isEmpty(it.data?.id)
                    && searchdata.data?.id.equals(it.data?.id.toString(), true)){
                    isAlreadyAvailable = true
                }
            }
        }
        if (!isAlreadyAvailable){
            recentSearchList.add(searchdata)
        }

        val str2 = Gson().toJson(recentSearchList)
        setLog("searchTab", "SearchAllTabFragment-saveRecentSearchData()-str2-$str2")
        SharedPrefHelper.getInstance().setRecentSearchList(str2)
        callAddRecentSearh(searchdata)
    }

    override fun searchItemClick(searchdata: BodyRowsItemsItem, position: Int) {
        if(!searchdata?.data?.type?.isEmpty()!!){
            saveRecentSearchData(searchdata)
            if (searchdata?.data?.type?.toInt()==21||searchdata?.data?.type?.toInt()==110||searchdata?.data?.type?.toInt()==77777||searchdata?.data?.type?.toInt()==34) {
                if (searchdata?.data?.type?.toInt()==21 || searchdata?.data?.type?.toInt()==34){
                    playableItem = searchdata
                    playableItemPosition = position
                    getPlayableContentUrl(searchdata?.data?.id!!)
                    BaseActivity.setTouchData()
                }else if (searchdata?.data?.type?.toInt()==110){
                    playableItem = searchdata
                    playableItemPosition = position
                    CONTENT_TYPE = Constant.CONTENT_PODCAST
                    getPlayableContentUrl(searchdata?.data?.id!!)
                }else if (searchdata?.data?.type?.toInt()==34||searchdata?.data?.type?.toInt()==77777){
                    playableItem = searchdata
                    playableItemPosition = position
                    CONTENT_TYPE = Constant.CONTENT_RADIO
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

        if (callSearchResultClicked != null){
            callSearchResultClicked.contentClicked(searchdata, position)
        }

    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
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

    private fun callAddRecentSearh(searchdata: BodyRowsItemsItem) {
        val userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(requireActivity()).isOnline) {

            val addRecentSearchJson=JSONObject()
            addRecentSearchJson.put("uid",SharedPrefHelper.getInstance().getUserId())
            addRecentSearchJson.put("searchText",SearchAllTabFragment.lastSearchText)
            addRecentSearchJson.put("itype",searchdata?.data?.type)
            addRecentSearchJson.put("contentId",searchdata?.data?.id)
            addRecentSearchJson.put("image",searchdata?.data?.image)
            addRecentSearchJson.put("title",searchdata?.data?.title)
            if(!searchdata?.data?.subTitle?.isNullOrEmpty()!!) {
                addRecentSearchJson.put("artist", searchdata?.data?.subTitle)
            }else if(!searchdata?.data?.misc?.sArtist?.isNullOrEmpty()!!){
                addRecentSearchJson.put("artist", searchdata?.data?.misc?.sArtist?.get(0))
            }else{
                addRecentSearchJson.put("artist", "")
            }

            userViewModel?.addRecentSearch(requireContext(),addRecentSearchJson)?.observe(this,
            Observer {
                when(it.status){
                    com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                        setProgressBarVisible(false)
                        if (it?.data != null) {
                            setLog(TAG, "addRecentSearch ${it?.data}")

                        }
                    }
                    com.hungama.music.data.webservice.utils.Status.LOADING->{
                        setProgressBarVisible(false)

                    }
                    com.hungama.music.data.webservice.utils.Status.ERROR->{
                        setProgressBarVisible(false)
                        setLog(TAG, "addRecentSearch ${it?.message}")
                    }
                }
            });
        }
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
                        setLog(TAG, "moodRadioListRespModel 1 playableItemPosition: ${playableItemPosition}")
                        setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }
                    setLog(TAG, "moodRadioListRespModel 2 playableItemPosition: ${playableItemPosition}")
                    if (i > playableItemPosition){
                        setLog(TAG, "moodRadioListRespModel 3 playableItemPosition: ${playableItemPosition}")
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
            }
            else if (playableItem?.data?.type?.equals(Constant.PLAYER_RADIO, true)!! || playableItem?.data?.type?.equals(Constant.PLAYER_LIVE_RADIO, true)!!){
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
            }
            else{

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
        playableItem: BodyRowsItemsItem,
        position:Int
    ) {
        val track = Track()

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

        if (!TextUtils.isEmpty(playableItem?.data?.subTitle)){
            track.subTitle = playableItem?.data?.subTitle
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

        if (!TextUtils.isEmpty(playableItem?.data?.type.toString())){
            track.playerType = playableItem?.data?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (activity != null && !TextUtils.isEmpty(playableItem.data?.type)) {
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_PODCAST){
                track.contentType = ContentTypes.PODCAST.value
            }else if (playType == Constant.CONTENT_MUSIC){
                track.contentType = ContentTypes.AUDIO.value
            }
        }
/*        if (!TextUtils.isEmpty(playableItem.data?.subTitle)){
            track.heading = playableItem.data?.subTitle
        }else{
            track.heading = ""
        }*/
        track.heading = ""
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.pName.toString())){
            track.pName = playableContentModel?.data?.head?.headData?.misc?.pName.toString()
        }else{
            track.pName = ""
        }
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
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

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
    }

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: BodyRowsItemsItem?
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
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
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
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
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

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
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
        try {
            if (activity != null){
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
        }catch (e:Exception){

        }

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun onItemDetailPageRedirection(searchdata: BodyRowsItemsItem, position: Int) {

        val type=searchdata?.data?.type.toString()
        val image=searchdata?.data?.image
        val id=searchdata?.data?.id
        val title=searchdata?.data?.title
        val varientType=searchdata?.data?.variant

        setLog("DataTaype1","Type${type}")
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
                hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+MainActivity.lastItemClicked+"_"+MainActivity.headerItemName+"_"+"Search")

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
        setLog("SearchAllFragment", "SearchAllFragment-onResume-tabId-$tabId")
    }
    private fun setLocalBroadcast(){
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiverSearch)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverSearch, IntentFilter(Constant.AUDIO_PLAYER_EVENT))
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (!rowList.isNullOrEmpty()
                    && (tabId == SearchAllTabFragment.all || tabId == SearchAllTabFragment.song)) {
                    setLog("SearchAllFragment", "SearchAllFragment-onLocalBroadcastEventCallBack-tabId-$tabId")
                    checkAllContentDownloadedOrNot(rowList)
                }
                CommonUtils.setPageBottomSpacing(rvTab, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_72))
                setLog("setPageBottomSpacing", "SearchAllFragment-tabId-$tabId-rvTab-"+rvTab.paddingBottom)
            }
        }
    }

    private fun checkAllContentDownloadedOrNot(rowList:ArrayList<BodyRowsItemsItem>) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!rowList.isNullOrEmpty()) {
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
                                checkAllContentDWOrNot(rowList)
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
                                if (searchAdapter != null) {
                                    //setLog("isCurrentPlaying", "DetailChartAdapter-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                                    searchAdapter?.notifyItemChanged(lastPlayingContentIndex)
                                    searchAdapter?.notifyItemChanged(currentPlayingContentIndex)
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
        }
    }

    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1
    private suspend fun checkAllContentDWOrNot(rowList:ArrayList<BodyRowsItemsItem>):Boolean {
        try {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!rowList.isNullOrEmpty()) {
                    try {
                        rowList.forEachIndexed { index, it ->
                            if (it.data != null){
                                if (!isCurrentContentPlayingFromThis && !BaseActivity.songDataList.isNullOrEmpty()
                                    && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                                ) {
                                    val currentPlayingContentId =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                                    if (currentPlayingContentId?.toString()?.equals(it.data?.id)!!) {
                                        it.data?.isCurrentPlaying = true
                                        isCurrentContentPlayingFromThis = true
                                        setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-1-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex-index-$index")
                                        if (currentPlayingContentIndex >= 0){

                                        }else{
                                            lastPlayingContentIndex = index
                                        }
                                        currentPlayingContentIndex = index
                                    } else {
                                        if (it.data?.isCurrentPlaying == true){
                                            lastPlayingContentIndex = index
                                        }
                                        it.data?.isCurrentPlaying = false
                                    }
                                } else {
                                    if (it.data?.isCurrentPlaying == true){
                                        lastPlayingContentIndex = index
                                    }
                                    it.data?.isCurrentPlaying = false
                                }
                            }
                        }
                        setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-2-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                    }catch (e:Exception){

                    }
                    setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-3-$isCurrentContentPlayingFromThis"
                    )
                }
                setLog(
                    "isCurrentContentPlayingFromThis",
                    "isCurrentContentPlayingFromThis-4-$isCurrentContentPlayingFromThis"
                )
                return isCurrentContentPlayingFromThis
            }
        } catch (e: Exception) {

        }

        return false
    }

    private fun callAllSearchPageWiseApi(searchText:String,pageNo:Int){
        val searchViewModel = ViewModelProvider(
            this
        ).get(SearchViewModel::class.java)


        if (ConnectionUtil(activity).isOnline) {
            searchViewModel?.getAllSearchWithPageWiseData(requireActivity(), searchText.trim(),pageNo)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {

                                if(!it?.data?.data?.body?.all?.isNullOrEmpty()!!){
                                    isLoading=false
                                    searchAdapter?.addData(it?.data?.data?.body?.all!!)
                                }else{
                                    isLastPage=true
                                }


                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
//                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }

    private val mMessageReceiverSearch: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            CommonUtils.setLog(
                "BroadcastReceiver-1",
                "LibraryMusicAllFragment-mMessageReceiver-" + intent
            )
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    CommonUtils.setLog(
                        "BroadcastReceiver-1",
                        "LibraryMusicAllFragment-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                    )
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }
    fun calSearchResultFilterEvent(keyword:String,resultCount:String,search:String,filterName:String){
        val hashMap = HashMap<String,String>()
        hashMap.put(EventConstant.KEYWORD_EPROPERTY,keyword)
        hashMap.put(EventConstant.RESULTCOUNT_EPROPERTY,resultCount)
        hashMap.put(EventConstant.SEARCH_EPROPERTY,search)
        hashMap.put(EventConstant.FILTERNAME_EPROPERTY,filterName)

        EventManager.getInstance().sendEvent(SearchResultFilterEvent(hashMap))
    }


}