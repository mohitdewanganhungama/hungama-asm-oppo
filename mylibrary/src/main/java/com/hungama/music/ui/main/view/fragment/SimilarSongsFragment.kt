package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.SimilarSongAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.SimilarSongViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_similar_songs_detail.*
import kotlinx.coroutines.*

class SimilarSongsFragment() : BaseFragment(), TracksContract.View,
    BaseFragment.OnMenuItemClicked, BaseActivity.OnDownloadQueueItemChanged,
    OnUserSubscriptionUpdate, BaseActivity.OnLocalBroadcastEventCallBack {
    var selectedContentId: String? = null
    var playerType: String? = null

    var similarSongListViewModel: SimilarSongViewModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var similarSongSongList: List<RecommendedSongListRespModel.Data.Body.Similar?>? = null
    var ascending = true
    var similarSongAdpter: SimilarSongAdapter? = null
    var similarSongModel: RecommendedSongListRespModel?=null

    companion object{
        var similarSongRespModel: RecommendedSongListRespModel? = null
        var playableItemPosition = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_similar_songs_detail, container, false)
    }

    override fun initializeComponent(view: View) {
        selectedContentId = requireArguments().getString("id").toString()
        ivBack?.setOnClickListener { view -> backPress() }
        setUpSimilarSongListViewModel()
        Constant.screen_name ="Song Screen"
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        threeDotMenu?.visibility = View.INVISIBLE
        CommonUtils.setPageBottomSpacing(rvSimilarSong, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setUpSimilarSongListViewModel() {

        if (ConnectionUtil(activity).isOnline) {
            similarSongListViewModel = ViewModelProvider(
                this
            ).get(SimilarSongViewModel::class.java)
            similarSongListViewModel?.getSimilarSongList(requireContext(), selectedContentId!!)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            similarSongModel=it.data
                            setSimilarSongListData(it.data!!)
                            similarSongRespModel = it.data
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
        }
    }


    fun setSimilarSongListData(similarSongModel: RecommendedSongListRespModel) {

        if (!similarSongModel?.data?.body?.similar.isNullOrEmpty()) {
            similarSongSongList = similarSongModel.data?.body?.similar!!
            rvSimilarSong.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)

            }
            setSimilarSongAdapter(ascending)

        }
    }

    private fun setSimilarSongAdapter(asc: Boolean) {
        similarSongAdpter = SimilarSongAdapter(
            requireContext(), similarSongSongList as ArrayList<RecommendedSongListRespModel.Data.Body.Similar>,
            object : SimilarSongAdapter.OnChildItemClick {
                override fun onUserClick(childPosition: Int, isMenuClick: Boolean, isDownloadClick:Boolean) {
                    playableItemPosition = childPosition
                    if (isMenuClick){
                        if(isOnClick()) {
                            commonThreeDotMenuItemSetup(Constant.SIMILAR_SONG_DETAIL_ADAPTER)
                        }
                    }else if (isDownloadClick){
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = similarSongSongList?.get(childPosition)?.data?.id?.toString()!!
                        dpm.contentTitle = similarSongSongList?.get(childPosition)?.data?.title?.toString()!!
                        dpm.planName =  similarSongSongList?.get(childPosition)?.data?.misc?.movierights.toString()
                        dpm.isAudio = true
                        dpm.isDownloadAction = true
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload = RestrictedDownload.valueOf( similarSongSongList?.get(childPosition)?.data?.misc?.restricted_download!!)
                        if (CommonUtils.userCanDownloadContent(requireContext(), similarSongroot, dpm, this@SimilarSongsFragment,Constant.drawer_downloads_exhausted)){
                            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                            var dq = DownloadQueue()
                            //for (item in similarSongSongList?.iterator()!!){

                            dq = DownloadQueue()
                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.id.toString())){
                                dq.contentId = similarSongSongList?.get(childPosition)?.data?.id.toString()
                            }

                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.title!!)){
                                dq.title = similarSongSongList?.get(childPosition)?.data?.title!!
                            }

                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.subtitle!!)){
                                dq.subTitle = similarSongSongList?.get(childPosition)?.data?.subtitle!!
                            }

                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.image!!)){
                                dq.image = similarSongSongList?.get(childPosition)?.data?.image!!
                            }

                            dq.parentId = ""
                            dq.pName = ""
                            dq.pSubName = ""
                            dq.pReleaseDate = ""
                            dq.pImage = ""

                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.misc?.movierights.toString()!!)){
                                dq.planName = similarSongSongList?.get(childPosition)?.data?.misc?.movierights.toString()
                            }

                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.misc?.favCount.toString()!!)){
                                dq.f_fav_count = similarSongSongList?.get(childPosition)?.data?.misc?.favCount.toString()
                            }

                            if (!TextUtils.isEmpty(similarSongSongList?.get(childPosition)?.data?.misc?.playcount.toString()!!)){
                                dq.f_playcount = similarSongSongList?.get(childPosition)?.data?.misc?.playcount.toString()
                            }

                            dq.pType = DetailPages.SIMILAR_SONG_LIST_ADAPTER.value
                            dq.contentType = ContentTypes.AUDIO.value
                            val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                            dq.source = eventModel.sourceName

                            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(similarSongSongList?.get(childPosition)?.data?.id!!.toString())
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(similarSongSongList?.get(childPosition)?.data?.id!!.toString())
                            if ((!downloadQueue?.contentId.equals(similarSongSongList?.get(childPosition)?.data?.id!!.toString()))
                                && (!downloadedAudio?.contentId.equals(similarSongSongList?.get(childPosition)?.data?.id!!.toString()))){
                                downloadQueueList.add(dq)
                            }
                            // }
                            //if (downloadQueueList.size > 0){
                            (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                                downloadQueueList,
                                this@SimilarSongsFragment,
                                null,
                                false,
                                true
                            )
                            //}
                        }

                    }else{
                        if(isOnClick()) {
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(similarSongSongList?.get(childPosition)?.data?.id!!.toString())
                            if (downloadedAudio != null && downloadedAudio?.contentId.equals(
                                    similarSongSongList?.get(childPosition)?.data?.id!!.toString()
                                )
                            ) {
                                val playableContentModel = PlayableContentModel()
                                playableContentModel.data?.head?.headData?.id =
                                    downloadedAudio.contentId!!
                                playableContentModel.data?.head?.headData?.misc?.url =
                                    downloadedAudio.downloadedFilePath
                                playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url =
                                    downloadedAudio.downloadUrl!!
                                playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                                    downloadedAudio.drmLicense
                                playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                                    downloadedAudio.lyricsUrl
                                setPlayableContentListData(playableContentModel)
                            } else {
                                setUpPlayableContentListViewModel(
                                    similarSongSongList?.get(
                                        childPosition
                                    )?.data?.id!!
                                )
                                setEventModelDataAppLevel(similarSongSongList?.get(childPosition)?.data?.id!!,similarSongSongList?.get(childPosition)?.data?.title!!,"")
                            }
                        }

                    }


                }


            })
        rvSimilarSong.adapter = similarSongAdpter
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

    private fun setUpPlayableContentListViewModel(id:String) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setPlayableContentListData(it?.data)
                                }else{
                                    playableItemPosition = playableItemPosition +1
                                    if (playableItemPosition < similarSongSongList?.size!!) {
                                        setUpPlayableContentListViewModel(similarSongSongList?.get(playableItemPosition)?.data?.id!!)
                                        setEventModelDataAppLevel(similarSongSongList?.get(playableItemPosition)?.data?.id!!,similarSongSongList?.get(
                                            playableItemPosition
                                        )?.data?.title!!,"")
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
    }


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null ) {
            setLog("PlayableItem", "setPlayableContentListData id:"+playableContentModel?.data?.head?.headData?.id.toString())
            setLog("PlayableItem", "setSongLyricsData setPlayableContentListData"+playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link.toString())
            songDataList = arrayListOf()

            for (i in similarSongSongList?.indices!!) {
                if (playableContentModel?.data?.head?.headData?.id == similarSongSongList?.get(i)?.data?.id) {
                    setSimilarSongList(
                        playableContentModel,
                        similarSongSongList,
                        playableItemPosition
                    )
                } else if(i > playableItemPosition && similarSongSongList?.get(i)?.data != null) {
                    setSimilarSongList(null, similarSongSongList, i)
                }
            }
            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)
        }
    }
    var songDataList:ArrayList<Track> = arrayListOf()
    fun setSimilarSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<RecommendedSongListRespModel.Data.Body.Similar?>?,
        position: Int
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)){
            track.id = playableItem?.get(position)?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)){
            track.title = playableItem?.get(position)?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subtitle)){
            track.subTitle = playableItem?.get(position)?.data?.subtitle
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
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.type.toString())){
            track.playerType = playableItem?.get(position)?.data?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }

        track.heading = ""

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.playble_image)){
            track.image = playableItem?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)){
            track.image = playableItem?.get(position)?.data?.image
        }else{
            track.image = ""
        }
        track.parentId = ""
        track.pName = ""
        track.pSubName = ""
        track.pImage = ""

        track.pType = DetailPages.SIMILAR_SONG_LIST_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
        return songDataList
    }

    override fun onClick(v: View) {
        super.onClick(v)

    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
            if(isAdded()){
                setLog("DWProgrss-onChangedid", data.id.toString())
                setLog("DWProgrss-onChanged", reason.toString())
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByDownloadManagerId(data.id)
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

                when(reason){
                    Reason.DOWNLOAD_ADDED -> {
                        setLog("DWProgrss-ADDED", data.id.toString())
                    }
                    Reason.DOWNLOAD_QUEUED ->{
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-QUEUED", data.id.toString())
                            if (similarSongAdpter != null){
                                if (downloadQueue != null){
                                    val index = similarSongSongList?.indexOfFirst{
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        similarSongAdpter?.notifyItemChanged(index)
                                    }
                                }else if (downloadedAudio != null){
                                    val index = similarSongSongList?.indexOfFirst{
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        similarSongAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_STARTED->{
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-STARTED", data.id.toString())
                            if (similarSongAdpter != null){
                                if (downloadQueue != null){
                                    val index = similarSongSongList?.indexOfFirst{
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        similarSongAdpter?.notifyItemChanged(index)
                                    }
                                }else if (downloadedAudio != null){
                                    val index = similarSongSongList?.indexOfFirst{
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        similarSongAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_PROGRESS_CHANGED->{
                        setLog("DWProgrss-CHANGED", data.id.toString())
                    }
                    Reason.DOWNLOAD_RESUMED->{
                        setLog("DWProgrss-RESUMED", data.id.toString())
                    }
                    Reason.DOWNLOAD_PAUSED->{
                        setLog("DWProgrss-PAUSED", data.id.toString())
                    }
                    Reason.DOWNLOAD_COMPLETED->{
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-COMPLETED", data.id.toString())
                            if (similarSongAdpter != null){
                                if (downloadQueue != null){
                                    val index = similarSongSongList?.indexOfFirst{
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        similarSongAdpter?.notifyItemChanged(index)
                                    }
                                }else if (downloadedAudio != null){
                                    val index = similarSongSongList?.indexOfFirst{
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        similarSongAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_CANCELLED->{
                        setLog("DWProgrss-CANCELLED", data.id.toString())
                    }
                    Reason.DOWNLOAD_REMOVED->{
                        setLog("DWProgrss-REMOVED", data.id.toString())
                    }
                    Reason.DOWNLOAD_DELETED->{
                        setLog("DWProgrss-DELETED", data.id.toString())
                    }
                    Reason.DOWNLOAD_ERROR->{
                        setLog("DWProgrss-ERROR", data.id.toString())
                    }
                    Reason.DOWNLOAD_BLOCK_UPDATED->{
                        setLog("DWProgrss-UPDATED", data.id.toString())
                    }
                    Reason.DOWNLOAD_WAITING_ON_NETWORK->{
                        setLog("DWProgrss-NETWORK", data.id.toString())
                    }
                    else -> {}
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvSimilarSong, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        similarSongRespModel?.let {
            similarSongRespModel=null
        }
    }
}