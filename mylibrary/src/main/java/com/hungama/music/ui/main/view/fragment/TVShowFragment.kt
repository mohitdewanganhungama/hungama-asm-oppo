package com.hungama.music.ui.main.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.common.util.Util
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseActivity.Companion.setVideoTrackListData
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.DetailTVShowAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.TVSHOW_DETAIL
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.fragment_tvshow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [DynamicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TVShowFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View, BaseActivity.OnDownloadVideoQueueItemChanged,
    OnUserSubscriptionUpdate, BaseActivity.OnLocalBroadcastEventCallBack {

    var v: View? = null
    var data: Int = 0
    var season: PlaylistModel.Data.Body.Row.Season?=null
    var tvShowDetailRespModel: PlaylistDynamicModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playerType:String? = null
    var detailTVShowAdapter: DetailTVShowAdapter? = null
    var seasonList = ArrayList<PlaylistModel.Data.Body.Row.Season>()

    companion object {

        fun addfrag(
            tabIndex: Int,
            seasonList: List<PlaylistModel.Data.Body.Row.Season?>?,
            season: PlaylistModel.Data.Body.Row.Season?,
            tvShowDetailRespModel: PlaylistDynamicModel?
        ): TVShowFragment {
            val fragment = TVShowFragment()
            val args = Bundle()
            args.putInt("tabIndex", tabIndex)
            args.putParcelable("season", season)
            args.putParcelable("detail", tvShowDetailRespModel)
            fragment.arguments = args
            return fragment
        }

        var tvShowEpisode: PlaylistModel.Data.Body.Row.Season.SeasonData.Misc.Track?=null
        var childposition=0
    }

    override fun initializeComponent(view: View) {
        //data = requireArguments().getInt("tabIndex", 0)
        //setLog("tabIndex", data.toString())
        //tvIndex.text = data.toString()
        Constant.screen_name ="TVShow Details"
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        playerType = "96"
        if(requireArguments()!=null&&requireArguments().containsKey("season")){
            season=requireArguments().getParcelable("season")
        }
        if (requireArguments()!=null&&requireArguments().containsKey("detail")){
            tvShowDetailRespModel = requireArguments().getParcelable("detail")!!
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setLog("tabIndex2", "OnCreateView")
        return inflater.inflate(R.layout.fragment_tvshow, container, false)
    }

    fun setTVShowDetailsListData(season1: PlaylistModel.Data.Body.Row.Season?) {
        if (season1 != null && season1?.data?.misc != null && season1?.data?.misc?.tracks?.size!! > 0) {
            seasonList = ArrayList<PlaylistModel.Data.Body.Row.Season>()
            seasonList.add(season1!!)
            rvPodcastMain.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            setTvShowAdapter()

            setEmptyVisible(false)
        } else {
            setEmptyVisible(false)
            setProgressBarVisible(false)
        }
    }

    private fun setTvShowAdapter(){
        Constant.screen_name ="TvShow Screen"
        detailTVShowAdapter = DetailTVShowAdapter(requireContext(), seasonList?.get(0)?.data?.misc?.tracks,
            object : DetailTVShowAdapter.OnChildItemClick {
                override fun onUserClick(childPosition: Int, isMenuClick: Boolean, isDownloadClick:Boolean) {
                    tvShowEpisode =seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)
                    childposition =childPosition
                    if (isMenuClick){
                        if(isOnClick()) {
                            commonThreeDotMenuItemSetup(TVSHOW_DETAIL)
                        }
                    }else if (isDownloadClick){
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.id?.toString()!!
                        dpm.contentTitle = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.name?.toString()!!
                        dpm.planName = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.movierights.toString()
                        dpm.isAudio = false
                        dpm.isDownloadAction = true
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload = RestrictedDownload.valueOf(seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.restricted_download!!)
                        var attributeCensorRating = ""
                        if (!seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                            attributeCensorRating = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.attributeCensorRating?.get(0).toString()
                        }

                        if (CommonUtils.userCanDownloadContent(
                                requireContext(),
                                null,
                                dpm,
                                this@TVShowFragment,Constant.drawer_svod_tvshow_episode
                            )
                        ) {
                            if (!CommonUtils.checkUserCensorRating(
                                    requireContext(),
                                    attributeCensorRating
                                )
                            ) {
                                val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                                var dq = DownloadQueue()
                                //for (item in podcastEpisodeList?.iterator()!!){

                                dq = DownloadQueue()
                                if (!TextUtils.isEmpty(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.id.toString()
                                    )
                                ) {
                                    dq.contentId =
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.id.toString()
                                }

                                if (!TextUtils.isEmpty(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.name
                                    )
                                ) {
                                    dq.title =
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.name
                                }

                                if (!TextUtils.isEmpty(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.subTitle
                                    )
                                ) {
                                    dq.subTitle =
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.subTitle
                                }

                                if (!TextUtils.isEmpty(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.image!!
                                    )
                                ) {
                                    dq.image =
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.image!!
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.id!!)) {
                                    dq.parentId = tvShowDetailRespModel?.data?.head?.id!!
                                }
                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.title!!)) {
                                    dq.pName = tvShowDetailRespModel?.data?.head?.title
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.subTitle!!)) {
                                    dq.pSubName = tvShowDetailRespModel?.data?.head?.subTitle
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.releasedate!!)) {
                                    dq.pReleaseDate = tvShowDetailRespModel?.data?.head?.releasedate
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.image!!)) {
                                    dq.pImage = tvShowDetailRespModel?.data?.head?.image
                                }

                                if (!TextUtils.isEmpty(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.misc?.movierights.toString()!!
                                    )
                                ) {
                                    dq.planName =
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.movierights.toString()!!
                                    dq.planType = CommonUtils.getContentPlanType(dq.planName)
                                }

                                dq.pType = DetailPages.TVSHOW_DETAIL_ADAPTER.value
                                dq.contentType = ContentTypes.TV_SHOWS.value
                                val eventModel = HungamaMusicApp.getInstance().getEventData(tvShowDetailRespModel?.data?.head?.id.toString())
                                dq.source = eventModel.sourceName

                                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                    ?.findByContentId(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.id!!.toString()
                                    )
                                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                    ?.findByContentId(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.id!!.toString()
                                    )
                                if ((!downloadQueue?.contentId.equals(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.id!!.toString()
                                    ))
                                    && (!downloadedAudio?.contentId.equals(
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(
                                            childPosition
                                        )?.data?.id!!.toString()
                                    ))
                                ) {
                                    downloadQueueList.add(dq)
                                }
                                // }
                                //if (downloadQueueList.size > 0){
                                (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                                    downloadQueueList,
                                    this@TVShowFragment,
                                    false,
                                    true
                                )
                                //}
                                if (detailTVShowAdapter != null){
                                    detailTVShowAdapter?.notifyItemChanged(childPosition)
                                }
                            }
                        }

                    }else{
                        if(isOnClick()) {
                            val dpm = DownloadPlayCheckModel()
                            dpm.contentId = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.id?.toString()!!
                            dpm.contentTitle = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.name?.toString()!!
                            dpm.planName = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.movierights.toString()
                            dpm.isAudio = false
                            dpm.isDownloadAction = false
                            dpm.isDirectPaymentAction = false
                            dpm.queryParam = ""
                            dpm.isShowSubscriptionPopup = true
                            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                            dpm.restrictedDownload = RestrictedDownload.valueOf(seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.restricted_download!!)
                            var attributeCensorRating = ""
                            if (!seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                                attributeCensorRating = seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.misc?.attributeCensorRating?.get(0).toString()
                            }

                            if (CommonUtils.userCanDownloadContent(
                                    requireContext(),
                                    null,
                                    dpm,
                                    this@TVShowFragment,Constant.drawer_svod_tvshow_episode
                                )
                            ) {
                                if (!CommonUtils.checkUserCensorRating(
                                        requireContext(),
                                        attributeCensorRating
                                    )
                                ) {
                                    BaseActivity.tvshowDetail =
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)!!
                                    val songsList =
                                        CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                                    val intent =
                                        Intent(requireContext(), VideoPlayerActivity::class.java)
                                    val serviceBundle = Bundle()
                                    serviceBundle.putParcelableArrayList(
                                        Constant.ITEM_KEY,
                                        songsList
                                    )
                                    serviceBundle.putParcelableArrayList(
                                        Constant.SEASON_LIST,
                                        seasonList
                                    )
                                    serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                                    serviceBundle.putString(
                                        Constant.SELECTED_CONTENT_ID,
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.id
                                    )
                                    serviceBundle.putInt(
                                        Constant.CONTENT_TYPE,
                                        Constant.CONTENT_TV_SHOW
                                    )
                                    serviceBundle.putInt(
                                        Constant.TYPE_ID,
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.type!!
                                    )
                                    intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                                    intent.putExtra(
                                        "thumbnailImg",
                                        seasonList?.get(0)?.data?.misc?.tracks?.get(childPosition)?.data?.image
                                    )
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    //startActivity(intent)
                                    serviceBundle.putLong(
                                        Constant.VIDEO_START_POSITION,
                                        TimeUnit.SECONDS.toMillis(
                                            HungamaMusicApp.getInstance().getContentDuration(
                                                seasonList?.get(0)?.data?.misc?.tracks?.get(
                                                    childPosition
                                                )?.data?.id!!
                                            )!!
                                        )
                                    )
                                    (requireActivity() as MainActivity).setLocalBroadcastEventCall(
                                        this@TVShowFragment,
                                        Constant.VIDEO_PLAYER_EVENT
                                    )
                                    if (activity != null){
                                        val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                                        if (status == Constant.pause){
                                            SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                                        }else{
                                            SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                                        }
                                        (activity as MainActivity).pausePlayer()
                                    }
                                    startActivity(intent)
                                    /*startActivityForResult(
                                        intent,
                                        Constant.VIDEO_ACTIVITY_RESULT_CODE
                                    )*/


                                }
                            }

                        }

                    }

                }
            })
        rvPodcastMain.adapter = detailTVShowAdapter
    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun playAllVideo(
        model: PlayableContentModel,
        trackPlayStartPosition: Long,
        selectedTrackPosition: Int
    ) {
        val videoUrl = model.data.head.headData.misc.url
        val videoDrmLicense = model.data.head.headData.misc.downloadLink.drm.token
        val videoTitle = model.data.head.headData.title
        val videoSubTitle = model.data.head.headData.subtitle
        val videoArtwork = model.data.head.headData.image
        videoUrl?.let {
            videoTitle?.let { it1 ->
                playerType?.let { it2 ->
                    CommonUtils.setVideoTrackList(
                        requireContext(),
                        model.data.head.headData.id,
                        it,
                        it1,
                        it2,
                        videoSubTitle,
                        videoArtwork,
                        videoDrmLicense,
                        ContentTypes.TV_SHOWS.value
                    )
                    tracksViewModel.prepareTrackPlayback(BaseActivity.nowPlayingCurrentIndex()+1, trackPlayStartPosition)
                }
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.VIDEO_ACTIVITY_RESULT_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                showMiniVideoPlayer(data)
            }
        }else{
            setLog("VIDEO_START_POSITION", requestCode.toString() + ", " + resultCode)
        }
    }

    override fun onDestroy() {
        (requireActivity() as MainActivity).removeVideoDownloadListener()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mTVShowEventReceiver);
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
        intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
        intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
        //(activity as MainActivity).reBindService()
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    override fun onDownloadVideoQueueItemChanged(
        downloadManager: DownloadManager,
        download: Download
    ) {
        baseMainScope.launch {
            try {
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                    ?.findByPlayableUrl(download.request.uri.toString())
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                    ?.findByPlayableUrl(download.request.uri.toString())

                if (detailTVShowAdapter != null) {
                    if (downloadQueue != null) {
                        val index = seasonList?.get(0)?.data?.misc?.tracks?.indexOfFirst {
                            it?.data?.id == downloadQueue.contentId
                        }
                        if (index != null) {
                            detailTVShowAdapter?.notifyItemChanged(index)
                        }

                    } else if (downloadedAudio != null) {
                        val index = seasonList?.get(0)?.data?.misc?.tracks?.indexOfFirst {
                            it?.data?.id == downloadedAudio.contentId!!
                        }
                        if (index != null) {
                            detailTVShowAdapter?.notifyItemChanged(index)
                        }

                    } else {
                        return@launch
                    }
                } else {
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    override fun onDownloadProgress(downloads: List<Download?>?, progress: Int, currentExoDownloadPosition:Int) {

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        if (downloadsPaused!!){
            setLog("VideoDownloadLog:9", downloadsPaused.toString())
        }else{
            setLog("VideoDownloadLog:10", downloadsPaused.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        setTVShowDetailsListData(season)
        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
            ArrayList(),
            this,
            true,
            false
        )
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun showMiniVideoPlayer(intent: Intent) {
        if (intent?.hasExtra(Constant.VIDEO_START_POSITION) == true) {
            val video_start_position = intent.getLongExtra(Constant.VIDEO_START_POSITION, 0)
            val videoListModel:ArrayList<PlayableContentModel> = intent.getParcelableArrayListExtra(
                Constant.VIDEO_LIST_DATA
            )!!
            val videoSeasonListModel:ArrayList<PlaylistModel.Data.Body.Row.Season> = intent.getParcelableArrayListExtra(
                Constant.SEASON_LIST
            )!!
            val selectedTrackPosition = intent.getIntExtra(Constant.SELECTED_TRACK_POSITION, 0)
            setVideoTrackListData(videoSeasonListModel)
            playAllVideo(videoListModel.get(selectedTrackPosition), video_start_position, selectedTrackPosition)
            setLog("VIDEO_START_POSITION-1", video_start_position.toString())
        }
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }
    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.VIDEO_ACTIVITY_RESULT_CODE) {
                showMiniVideoPlayer(intent)
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                /*CommonUtils.setPageBottomSpacing(rvPodcastMain, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)*/
            }
        }
    }

    private val mTVShowEventReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent != null) {
                if (detailTVShowAdapter != null){
                    val index = intent.getIntExtra("index", 0)
                    detailTVShowAdapter?.notifyItemChanged(index)
                }
            }
        }
    }
}

