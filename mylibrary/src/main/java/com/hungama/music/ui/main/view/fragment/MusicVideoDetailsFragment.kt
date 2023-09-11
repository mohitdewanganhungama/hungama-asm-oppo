package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.player.videoplayer.AudioSubtitleSelectBottomSheetFragment
import com.hungama.music.player.videoplayer.SubtitleSelectBottomSheetFragment
import com.hungama.music.player.videoplayer.VideoQualitySelectBottomSheetFragment
import com.hungama.music.player.videoplayer.services.VideoPlayerService
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.VideoDetailAdapter
import com.hungama.music.ui.main.adapter.VideoLikeAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.HomeViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.ui.main.viewmodel.VideoDetailsViewModel
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.isVideoAutoPlayEnable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.stopReasonPause
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.download.Data
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.fragment_video_details.*
import kotlinx.android.synthetic.main.video_player_view.*
import kotlinx.android.synthetic.main.video_player_view_control.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.hungama.music.player.download.DownloadTracker
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.video_player_custum_control.*
import kotlinx.android.synthetic.main.video_player_view_control.brightnessIcon
import kotlinx.android.synthetic.main.video_player_view_control.brightness_center_text
import kotlinx.android.synthetic.main.video_player_view_control.brightness_image
import kotlinx.android.synthetic.main.video_player_view_control.brightness_slider
import kotlinx.android.synthetic.main.video_player_view_control.brightness_slider_container
import kotlinx.android.synthetic.main.video_player_view_control.brigtness_perc_center_text
import kotlinx.android.synthetic.main.video_player_view_control.btn_pause
import kotlinx.android.synthetic.main.video_player_view_control.btn_play
import kotlinx.android.synthetic.main.video_player_view_control.exo_progress
import kotlinx.android.synthetic.main.video_player_view_control.head
import kotlinx.android.synthetic.main.video_player_view_control.img_cast_menu_dots
import kotlinx.android.synthetic.main.video_player_view_control.img_fwd
import kotlinx.android.synthetic.main.video_player_view_control.previewFrameLayout
import kotlinx.android.synthetic.main.video_player_view_control.rlBottomControll
import kotlinx.android.synthetic.main.video_player_view_control.rlCenterControll
import kotlinx.android.synthetic.main.video_player_view_control.rlExtraFeature
import kotlinx.android.synthetic.main.video_player_view_control.rlLock
import kotlinx.android.synthetic.main.video_player_view_control.rlSettings
import kotlinx.android.synthetic.main.video_player_view_control.rlUnLock
import kotlinx.android.synthetic.main.video_player_view_control.sbBrightness
import kotlinx.android.synthetic.main.video_player_view_control.sbVolume
import kotlinx.android.synthetic.main.video_player_view_control.surfaceView
import kotlinx.android.synthetic.main.video_player_view_control.tvHeading
import kotlinx.android.synthetic.main.video_player_view_control.video_player_skip_credit
import kotlinx.android.synthetic.main.video_player_view_control.video_player_skip_intro
import kotlinx.android.synthetic.main.video_player_view_control.volIcon
import kotlinx.android.synthetic.main.video_player_view_control.vol_center_text
import kotlinx.android.synthetic.main.video_player_view_control.vol_image
import kotlinx.android.synthetic.main.video_player_view_control.vol_perc_center_text
import kotlinx.android.synthetic.main.video_player_view_control.volume_slider
import kotlinx.android.synthetic.main.video_player_view_control.volume_slider_container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.text.DecimalFormat

@OptIn(UnstableApi::class)
class MusicVideoDetailsFragment : BaseFragment(), BucketParentAdapter.OnMoreItemClick,
    VideoQualitySelectBottomSheetFragment.OnVideoQualityItemClick,
    SubtitleSelectBottomSheetFragment.OnSubTitleItemClick, View.OnTouchListener,
    View.OnClickListener, AudioSubtitleSelectBottomSheetFragment.OnAudioSubTitleItemClick,
    BaseActivity.OnDownloadVideoQueueItemChanged, OnUserSubscriptionUpdate, TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack, SimilarVideoFragment.OnSimilarVideoClick,
    OnParentItemClickListener{

    var isAddWatchlist = false
    var is13PlusVideoSettingPopupVisible = true
    var similarVideoIndex = -1

    companion object {
        fun newInstance() = MusicVideoDetailsFragment()
        var videoDetailRespModel: PlaylistDynamicModel? = null
        var playableItemPosition = 0
        var videoListModel: PlayableContentModel? = null
        var mPlayerView: PlayerView? = null
    }


    private lateinit var videoDetailViewModel: VideoDetailsViewModel
    var selectedContentId = ""
    var oriSelectedContentId = ""
    var default_attribute_censor_rating = ""
    private var mService: VideoPlayerService? = null
    private var mPlayer: ExoPlayer? = null
    private var mBound = false
    private var playerPosition: Long = 0
    private var getPlayerWhenReady: Boolean = false
    var thumbnailUrl = ""
    var videoStartPosition: Long = 0
    var lastWindowIndex = 0

    var isInPipMode: Boolean = false
    var isPIPModeeEnabled: Boolean = true //Has the user disabled PIP mode in AppOpps?
    private var currentPlayingMediaItem: MediaItem? = null
    private var isScreenLandscape: Boolean = false

    private var downloadTracker: DownloadTracker? = null
    var REQUEST_CODE_WRITE_STORAGE_PERMISION = 105
    var downloadState = Status.NONE.value
    var isEnableNextPrvious = false
    var isEnableForwardBackward = true
    var isEnableShuffle = false
    var isEnableRepeat = false
    var aSessionId: Int = -1
    lateinit var requiredFormat: String

    private var trackSelector: DefaultTrackSelector? = null
    var subTitleSheetFragment: SubtitleSelectBottomSheetFragment? = null
    var videoQualitySheetFragment: VideoQualitySelectBottomSheetFragment? = null
    var audioSubtitleSelectBottomSheetFragment: AudioSubtitleSelectBottomSheetFragment? = null
    private var sWidth = 0
    private var sHeight: Int = 0
    private var size: Point? = null
    private var intLeft = true
    private var intRight = false
    private var intTop = false
    private var intBottom = false
    private var seekDur: String? = null
    private var baseX = 0f
    private var baseY = 0f
    private var diffX: Long = 0
    private var diffY = 0
    private var calculatedTime = 0
    private var MIN_DISTANCE = 150
    private var tested_ok = false

    private var videoList = ArrayList<PlayableContentModel>()
    private var serviceIntent: Intent? = null;

    var userViewModel: UserViewModel? = null
    var currentVideoPlaylistPosition = -1
    var childPosition = -1
    var musicvideoList = ArrayList<DownloadedAudio>()

    enum class ControlsMode {
        LOCK, FULLCONTORLS
    }

    private var controlsState: ControlsMode? = null
    private var cResolver: ContentResolver? = null
    private var screen_swipe_move = false
    private var brightness = 150
    private var mediavolume: Int = 0
    private var device_height: Int = 0
    private var device_width: Int = 0
    private var audioManager: AudioManager? = null
    private var lastVolumeData = 0
    private var lastVolumeProgress = 0
    private var isMute = false
    var skipIntroST = 0L
    var skipIntroET = 0L
    var skipCreditST = 0L
    var skipCreditET = 0L

    private var mainHandler: Handler? = null
    private var hideVolumeBar: Runnable? = null
    private var hideControls: Runnable? = null
    private val playerControlVisibilityTimeout: Long = 3000
    var content_type = 0
    var seasonList = ArrayList<PlaylistModel.Data.Body.Row.Season>()
    var playIndex = 0
    var _arrayOfBitrate = ArrayList<Int>()
    val _arrayOfBandwidth = HashMap<String, Int>()
    var screenHeightInPotratitMode = 0

    var videoListViewModel: VideoViewModel? = null
    var mVideoDetailAdapter: VideoDetailAdapter? = null

    private var mSavedState: Bundle? = null
    var playerType: String? = "22"
    private lateinit var tracksViewModel: TracksContract.Presenter
    var isShowNoInternetPopUp = true

    private var isChomeCastClick: Boolean=false

    var isShareClick = false
    private var mediaSession: MediaSession? = null

    /** Intent action for pip controls from Picture-in-Picture mode.  */
    private val ACTION_PIP_CONTROL = "pip_control"

    /** Intent extra for pip controls from Picture-in-Picture mode.  */
    private val EXTRA_CONTROL_TYPE = "control_type"
    private val CONTROL_TYPE_START_OR_PAUSE = 2
    private val REQUEST_START_OR_PAUSE = 4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_details, container, false)
    }


    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            setLog("LifeCycle:--", "onServiceConnected: $componentName")
            val binder = iBinder as VideoPlayerService.LocalBinder
            mService = binder.service
            mBound = true
            initializePlayer()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            setLog("LifeCycle:--", "onServiceDisconnected: $componentName")
            mBound = false
            video_player_view.player = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Utils.setStopScreenrecord(requireActivity())
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun initializeComponent(view: View) {
        if (activity != null && activity is MainActivity){
            (activity as MainActivity).closePIPVideoPlayer()
        }
        if (arguments != null){
            if (requireArguments().containsKey(Constant.defaultContentPlayerType)){
                playerType = requireArguments().getString(Constant.defaultContentPlayerType).toString()
            }
            if (requireArguments().containsKey(Constant.VIDEO_START_POSITION)){
                playerPosition = requireArguments().getLong(Constant.VIDEO_START_POSITION)
            }
        }
        Constant.screen_name ="MusicVideo Player"
        mPlayerView = video_player_view
        mediaRouteButton= view?.findViewById(R.id.media_route_button)
        mediaRouteButton?.setRemoteIndicatorDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.cast_button_bg
            )
        )

        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton!!)
        CastContext.getSharedInstance()?.setReceiverApplicationId(getString(R.string.chormecast_app_id))

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        screenHeightInPotratitMode = getPlayerHeight()
        setLog("pipMode", "MusicVideoDetailsFragment-initializeComponent-isInPipMode-$isInPipMode-playerPosition-$playerPosition-video_player_view!=null-${video_player_view != null}-screenHeightInPotratitMode-$screenHeightInPotratitMode")
        if (video_player_view != null) {
            val params: ViewGroup.LayoutParams = video_player_view.getLayoutParams()
            params.height = screenHeightInPotratitMode
            video_player_view?.requestLayout()
        }

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        videoDetailroot?.visibility = View.GONE
        // Handle events from the action icons on the picture-in-picture mode.
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(ACTION_PIP_CONTROL))
        registerBroadcastReceiver()
        (requireActivity() as MainActivity).hideMiniPlayer()
        selectedContentId = requireArguments().getString(Constant.defaultContentId).toString()

        if(requireArguments().getBoolean(Constant.isPlayFromBanner)){
            setUpVideoListViewModel(selectedContentId)
        }

        if(requireArguments()!=null&&requireArguments().containsKey(Constant.EXTRA_LIST)){
            musicvideoList=
                (requireArguments().getSerializable(Constant.EXTRA_LIST) as ArrayList<DownloadedAudio>?)!!
            setLog("musicvideoList","musicvideoList${musicvideoList}")
        }

        if(requireArguments()!=null&&requireArguments().containsKey(Constant.CHILD_POSITION)){
            childPosition= (requireArguments().getInt(Constant.CHILD_POSITION))
            selectedContentId = musicvideoList?.get(childPosition)?.contentId!!
        }

        oriSelectedContentId = selectedContentId
        downloadTracker = DemoUtil.getDownloadTracker(requireContext())

        startupApiCalls()

        llWatchlist?.setOnClickListener(this)
        rlShare?.setOnClickListener(this)
        img_cast_menu_dots?.setOnClickListener(this)
        exo_next_btn?.setOnClickListener(this)
        exo_previous_btn?.setOnClickListener(this)


        ivMusicVideoBack.setOnClickListener {
            baseMainScope.launch {
                try {
                    if (isAdded && context != null) {
                        val tempIsScreenLandscape = isScreenLandscape
                        onBackPressedCall()
                        if (tempIsScreenLandscape) {
                            onBackPressedCall()
                            delay(500)
                        }
                        backPress()
                    }
                }catch (e:Exception){

                }
            }
        }



        size = Point()
        requireActivity().window.windowManager.defaultDisplay.getSize(size)
        sWidth = size?.x!!
        sHeight = size?.y!!

        val displaymetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displaymetrics)
        device_height = displaymetrics.heightPixels
        device_width = displaymetrics.widthPixels
        surfaceView.setOnTouchListener(this)
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                setLog("onBackPressed", "MusicVideoDetailsFragment-initializeComponent-event.action-${event.action}")
                baseMainScope.launch {
                    try {
                        if (isAdded && context != null) {
                            val tempIsScreenLandscape = isScreenLandscape
                            onBackPressedCall()
                            if (tempIsScreenLandscape) {
                                onBackPressedCall()
                                delay(500)
                            }
                            backPress()
                        }
                    }catch (e:Exception){

                    }
                }
                true
            } else false
        }

        rlSimilarVideos.setOnClickListener {
            if (videoDetailRespModel?.data?.body != null && !videoDetailRespModel?.data?.body?.recomendation.isNullOrEmpty()) {
                val similarVideoFragment = SimilarVideoFragment(videoDetailRespModel, this)
                similarVideoFragment.show(requireFragmentManager(), "open")
            }
        }

        CommonUtils.setPageBottomSpacing(
            scrollView,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
    }

    private fun initializePlayer() {
        setLog("pipMode", "MusicVideoDetailsFragment-initializePlayer-isInPipMode-$isInPipMode-playerPosition-$playerPosition-mBound-$mBound-mService!=null-${mService != null}-mService?.playerInstance!=null-${mService?.playerInstance != null}")
        if (isAdded && mBound && mService != null && mService?.playerInstance != null) {
            mPlayer = mService?.playerInstance as ExoPlayer?
            video_player_view?.player = mPlayer
            this.currentPlayer=mPlayer

            currentPlayer?.addListener(PlayerEventListener())
            mPlayer?.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)

            if (videoDetailRespModel != null && videoDetailRespModel?.data != null && videoDetailRespModel?.data?.head != null && videoDetailRespModel?.data?.head?.data != null
                && videoDetailRespModel?.data?.head?.data?.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
                mPlayer?.repeatMode = Player.REPEAT_MODE_ALL
            }else{
                mPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            }



            if (currentPlayer != null && !videoList.isNullOrEmpty() && videoList.size > playableItemPosition && videoList.get(playableItemPosition) != null) {
                callVideoPlayAction(videoList.get(playableItemPosition))
                videoTitle?.text = videoList.get(playableItemPosition)?.data?.head?.headData?.title
                videoTitle?.isSelected = true
                tvHeading?.text = videoList.get(playableItemPosition)?.data?.head?.headData?.title
            }

            //Use Media Session Connector from the EXT library to enable MediaSession Controls in PIP.
           /* mediaSession = MediaSessionCompat(requireContext(), requireContext().packageName)
            val mediaSessionConnector = MediaSessionConnector(mediaSession!!)
            mediaSessionConnector.setPlayer(currentPlayer)
            mediaSession?.isActive = true*/
            if (mService == null){
                //mediaSession = mService?.mediaSessionInstance
            }

            setLog("pipMode", "MusicVideoDetailsFragment-initializePlayer-mPlayer!=null-${currentPlayer != null}-videoList.size-${videoList.size}}")
            if (currentPlayer != null && !videoList.isNullOrEmpty()) {
                currentPlayingMediaItem = currentPlayer?.getMediaItemAt(currentPlayer?.currentWindowIndex!!)
                //initPlayPause()
                //initDownloadMusic()
                initBwd()
                initFwd()
                initLock()
                videoQualitySettings()
                initVolume()
                initBrightness()
                initSeekbarThumbnail()
                initSkipIntro()
                initSkipCredit()
                mainHandler = Handler()
                hideControls = Runnable { hideAllControls() }
                hideVolumeBar = Runnable { volume_slider_container.visibility = View.INVISIBLE }
                mainHandler?.postDelayed(hideControls!!, playerControlVisibilityTimeout)
                controlsState = ControlsMode.FULLCONTORLS
                btn_play.setOnClickListener(this)
                btn_pause.setOnClickListener(this)


                if (videoListModel != null && videoListModel?.data != null && videoListModel?.data?.head != null && videoListModel?.data?.head?.headData != null && !TextUtils.isEmpty(videoListModel?.data?.head?.headData?.id)){
                    setLog(TAG, "initializePlayer before: playerPosition:${playerPosition} musicVideoID:${videoListModel?.data?.head?.headData?.id}")
                    //playerPosition= HungamaMusicApp.getInstance().getContentDuration(videoListModel?.data?.head?.headData?.id!!)!!
                    setLog(TAG, "initializePlayer after: playerPosition:${playerPosition}")
                }
                if(playerPosition>0){
                    setLog("LifeCycle:--", "initializePlayer-playerPosition-1-$playerPosition  mPlayer.duration-${mPlayer?.duration}")
                    playerPosition=playerPosition
                    setLog("LifeCycle:--", "initializePlayer-playerPosition-2-$playerPosition")
                    currentPlayer?.seekTo(playerPosition)

                }

                mPlayer?.playWhenReady = true
                startPlayerDurationCallback()
                if(mPlayer!=null&&videoList!=null&&videoList.get(mPlayer?.currentWindowIndex!!)!=null) {
                    tvHeading.text =
                        videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.title
                    setLog(TAG, "initializePlayer: tvTitle " + tvTitle)
                    setLog(TAG, "initializePlayer: tvHeading" + tvHeading)

                    CoroutineScope(Dispatchers.Main).launch {
                        val hashMapPageView = java.util.HashMap<String, String>()

                        if(videoList!=null &&videoList.get(mPlayer?.currentWindowIndex!!).data!=null
                            && videoList.get(mPlayer?.currentWindowIndex!!).data?.head!=null
                            &&videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData !=null
                            ){
                        if(videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.title !=null)
                        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =
                            videoList.get(mPlayer?.currentWindowIndex!!).data.head.headData.title
                        if(videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type !=null)
                        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                            "" + Utils.getContentTypeNameForStream("" + videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type)
                        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                            "" + videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.id
                        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] =
                            MainActivity.lastItemClicked
                        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                            "" + MainActivity.lastItemClicked + "," + MainActivity.headerItemName
                        if(videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type !=null)
                        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "player_video_" +
                                Utils.getContentTypeNameForStream("" + videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type)
                                    .replace("_", "").lowercase()

                            }
                        setLog("VideoPlayerPageView", hashMapPageView.toString())
//                        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                    }

                    if(videoList.get(mPlayer?.currentWindowIndex!!)!=null)
                    callStreamEventAnalyticsStart(videoList.get(mPlayer?.currentWindowIndex!!),false)
                }
                    setUpChormeCast()
                Log.d(TAG, "setCurrentPlayer: isCastSessionAvailable :${castPlayer?.isCastSessionAvailable}")
                setCurrentPlayer((if (castPlayer != null && castPlayer?.isCastSessionAvailable!!) castPlayer else mPlayer)!!)

            }

            full_screen_enter_exit.setOnClickListener {
                handleFullScreenEnterExit()
            }

        }
    }

    var durationHandler: Handler? = null

    private fun startPlayerDurationCallback(){
        if (durationHandler != null){
            removePlayerDurationCallback()
            durationHandler?.post(updatePlayerDurationTask)
        }else{
            durationHandler = Handler(Looper.getMainLooper())
            durationHandler?.post(updatePlayerDurationTask)
        }
    }

    private fun removePlayerDurationCallback(){
        if (durationHandler != null){
            durationHandler?.removeCallbacks(updatePlayerDurationTask)
        }
    }


    private val updatePlayerDurationTask = object : Runnable {
        override fun run() {
            if (mPlayer!=null){
                updatePlayerDuration()
                durationHandler?.postDelayed(this, 1000)
            }

        }
    }

    fun updatePlayerDuration(){
        if (mPlayer!=null&&mPlayer?.isPlaying!!){
            val diff: Long = mPlayer?.duration!! - mPlayer?.currentPosition!!
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(minutes)

            val totDur = String.format("%02d:%02d",minutes,seconds);

            exo_duration?.setText(totDur)
        }else{
        }

    }

    private fun setUpChormeCast() {

        setLog(TAG, "setUpChormeCast called")
        if(castPlayer==null || BaseFragment.isCastPlayerAudio){
            castPlayer = CastContext.getSharedInstance()?.let { CastPlayer(it) }
            BaseFragment.isCastPlayerAudio=false
        }

        castPlayer?.setSessionAvailabilityListener(object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                setLog(TAG, "onCastSessionAvailable: ")
                if(castPlayer!=null){
                    setCurrentPlayer(castPlayer!!)
                }else{
                    castPlayer = CastContext.getSharedInstance()?.let { CastPlayer(it) }
                    BaseFragment.isCastPlayerAudio=false
                    if(castPlayer!=null){
                        setCurrentPlayer(castPlayer!!)
                    }
                }

            }

            override fun onCastSessionUnavailable() {
                setLog(TAG, "onCastSessionUnavailable: ")
                setLog("pipMode", "MusicVideoDetailsFragment-setUpChormeCastpausePlayer-onCastSessionUnavailable-pausePlayer-called")
                mPlayer?.let {
                    setCurrentPlayer(mPlayer!!)
                }
                activity?.let {
                    (activity as BaseActivity).stopChormeCast()
                }


            }

        })

    }



    private var isPlayNextVideoCastPlayerCalled=false
    private inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            setLog("MusicVideo", "MusicVideoDetailFragment-PlayerEventListener-onPlaybackStateChanged-playbackState-${playbackState} ${currentPlayer == castPlayer}")
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    if(isAdded() && isVideoAutoPlayEnable()){
                        if(currentPlayer == castPlayer && !isPlayNextVideoCastPlayerCalled && CastContext.getSharedInstance()?.sessionManager?.currentCastSession?.remoteMediaClient?.idleReason == MediaStatus.IDLE_REASON_FINISHED){
                            if (videoDetailRespModel != null && videoDetailRespModel?.data != null && videoDetailRespModel?.data?.body != null
                                && !videoDetailRespModel?.data?.body?.rows.isNullOrEmpty() && videoDetailRespModel?.data?.body?.rows?.size!! > 0){
                                setLog("MusicVideo", "MusicVideoDetailFragment-onPositionDiscontinuity-id-${videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id.toString()}")
                                playNextVideo(videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id.toString())
                                isPlayNextVideoCastPlayerCalled=true
                            }
                        }
                    }

                }
                ExoPlayer.STATE_BUFFERING -> {
                    if(isAdded()){
                        loading_exoplayer?.let {
                            loading_exoplayer.visibility = View.VISIBLE
                        }
                    }


                }
                ExoPlayer.STATE_READY -> {
                    if(isAdded()){
                        loading_exoplayer?.let {
                            loading_exoplayer.visibility = View.GONE
                        }
                    }


                }
                ExoPlayer.STATE_ENDED -> {
                    if(isAdded() && isVideoAutoPlayEnable()){
                        if (videoDetailRespModel != null && videoDetailRespModel?.data != null && videoDetailRespModel?.data?.body != null
                            && !videoDetailRespModel?.data?.body?.rows.isNullOrEmpty() && videoDetailRespModel?.data?.body?.rows?.size!! > 0){
                            setLog("MusicVideo", "MusicVideoDetailFragment-onPositionDiscontinuity-id-${videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id.toString()}")
                            playNextVideo(videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id.toString())

                        }
                    }


                }
                else -> {
                }
            }
        }

        private fun callStreamFailedEvent(
            error: PlaybackException
        ) {

            if (videoListModel!=null){
                val hashMap = java.util.HashMap<String, String>()


                hashMap.put(EventConstant.CONNECTION_TYPE_EPROPERTY, ConnectionUtil.NETWORK_TYPE)
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, EventConstant.CONSUMPTIONTYPE_ONLINE)
                var newContentId= videoListModel?.data?.head?.headData?.id
                var contentIdData=newContentId?.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY, ""+contentIdData)
                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+videoListModel?.data?.head?.headData?.type))
                hashMap.put(EventConstant.DURATION_EPROPERTY,"00:00")
                hashMap.put(EventConstant.ERRORCODE_EPROPERTY,"")
                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,""+error.errorCodeName)
                hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"MusicVideo Player")
                hashMap.put(EventConstant.PCODE_EPROPERTY,""+videoListModel?.data?.head?.headData?.misc?.pName)
                hashMap.put(EventConstant.SCODE_EPROPERTY,""+videoListModel?.data?.head?.headData?.misc?.pName)
                hashMap.put(EventConstant.AP_EPROPERTY,"")
                hashMap.put(EventConstant.BUFF_EPROPERTY,"")
                hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+""+ MainActivity.lastItemClicked+","+ MainActivity.headerItemName)
                hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY,""+""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)

                EventManager.getInstance().sendEvent(StreamFailedEvent(hashMap))

            }

        }


        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)

            setLog("TAG", "onPlayerError MusicVideoDetailsFragment callStreamFailedEvent:${error} ")

            callStreamFailedEvent(error)

            setLog("TAG", "onPlayerError VideoPlayerActivity callStreamFailedEvent:${error} ")
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                noInternetPopupOpen()
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            super.onPositionDiscontinuity(reason)

            val latestWindowIndex = currentPlayer?.currentWindowIndex!!
            setLog("MusicVideo", "latestWindowIndex:${latestWindowIndex}")

            if(lastWindowIndex<currentPlayer?.mediaItemCount!!){
                currentPlayingMediaItem = currentPlayer?.getMediaItemAt(latestWindowIndex)

                //setLog("MusicVideo", "MusicVideoDetailFragment-onPositionDiscontinuity-lastWindowIndex-$lastWindowIndex")
                //setLog("MusicVideo", "MusicVideoDetailFragment-onPositionDiscontinuity-latestWindowIndex-$latestWindowIndex")
                if (latestWindowIndex != lastWindowIndex) {
                    if (currentPlayer != null && videoList != null && videoList.get(latestWindowIndex) != null) {
                        videoTitle.text = videoList.get(latestWindowIndex)?.data?.head?.headData?.title
                        videoTitle?.isSelected = true
                        tvHeading.text = videoList.get(latestWindowIndex)?.data?.head?.headData?.title
                    }
                    lastWindowIndex = latestWindowIndex
                }
            }


        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            try {
                if (isPlaying) {
                    val contentDownloaded = AppDatabase?.getInstance()?.downloadedAudio()
                        ?.findByContentId(selectedContentId)
                    if (contentDownloaded != null && contentDownloaded.contentStreamDate <= 0) {
                        AppDatabase?.getInstance()?.downloadedAudio()
                            ?.updateDownloadedContentStreamDate(
                                System.currentTimeMillis(),
                                selectedContentId
                            )
                    }
                    startPlayerDurationCallback()
                    btn_pause?.setVisibility(View.VISIBLE);
                    btn_play?.setVisibility(View.GONE);
                } else {
                    btn_pause?.setVisibility(View.GONE);
                    btn_play?.setVisibility(View.VISIBLE);
                }
            } catch (e: Exception) {

            }

        }
    }



    private fun startPlayerService(item: ArrayList<PlayableContentModel>): Boolean {
        setLog(
            "videoListModel",
            "startPlayerService playableItemPosition:${playableItemPosition} size:${item?.size}"
        )
        setLog("MusicVideo", "MusicVideoDetailFragment-startPlayerService-playableItemPosition-$playableItemPosition")


        if (!item.isNullOrEmpty() && item.size > playableItemPosition) {
            val track =  Track()
            track.id = item.get(playableItemPosition).data.head.headData.id.toLong()
            track.playerType = item.get(playableItemPosition).data.head.headData.type.toString()
            track.title = item?.get(playableItemPosition)?.data?.head?.headData?.title?.toString()!!




            //track.heading = ""
            if (context != null){
                setLog("MusicVideo", "MusicVideoDetailFragment-startPlayerService-track-${track}")
                CommonUtils.callStreamTriggerEvent(requireContext(), track, "Video Player", "")
            }
            setLog("MusicVideo", "MusicVideoDetailFragment-startPlayerService-contentId-${item?.get(playableItemPosition)?.data?.head?.headData?.id?.toString()!!}")
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = item?.get(playableItemPosition)?.data?.head?.headData?.id?.toString()!!
            dpm.contentTitle =
                item?.get(playableItemPosition)?.data?.head?.headData?.title?.toString()!!
            dpm.planName =
                item?.get(playableItemPosition)?.data?.head?.headData?.misc?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload =
                RestrictedDownload.valueOf(item?.get(playableItemPosition)?.data?.head?.headData?.misc?.restricted_download!!)
            var attributeCensorRating = ""
            if (!item.get(playableItemPosition).data.head.headData.misc.attributeCensorRating.isNullOrEmpty()) {
                attributeCensorRating =
                    item.get(playableItemPosition).data.head.headData.misc.attributeCensorRating.get(
                        0
                    )
            }

            if(attributeCensorRating.isEmpty()){
                attributeCensorRating=default_attribute_censor_rating
            }
            setLog("checkUserCensorRating", "attributeCensorRating:${attributeCensorRating} is13PlusVideoSettingPopupVisible:${is13PlusVideoSettingPopupVisible}")
            var isPlayVideo=CommonUtils.checkUserCensorRating(
                requireContext(),
                attributeCensorRating,
                object : UserCensorRatingPopup.OnUserCensorRatingChange {
                    override fun onCensorRatingChange(rating: Int) {
                        if(rating!=0){
                            startPlayerServer(item!!)
                            setLog("checkUserCensorRating", "startPlayerServer called")
                        }
                    }

                },
                is13PlusVideoSettingPopupVisible
            )
            setLog("checkUserCensorRating", "isPlayVideo:${isPlayVideo}")
            if (!isPlayVideo) {
              startPlayerServer(item!!)
            } else {
                setLog(TAG, "Music-Video-Play content check")
            }

            return true
        } else {
            return false
        }
    }

    private fun startPlayerServer(item: ArrayList<PlayableContentModel>) {
        //if (CommonUtils.userCanDownloadContent(requireContext(), musicVideoDetailroot, dpm, this)) {
        if (mPlayer == null) {

            serviceIntent = Intent(requireContext(), VideoPlayerService::class.java)
            val serviceBundle = Bundle()

            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, item)
            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
            serviceBundle.putInt(Constant.SELECTED_TRACK_POSITION, playableItemPosition)
            serviceIntent?.putExtra(Constant.BUNDLE_KEY, serviceBundle)

            /*bindService(intent, mConnection, BIND_AUTO_CREATE)
//                startService(intent)
            Util.startForegroundService(this, intent)*/
            setLog("selectedContentId2", selectedContentId)
            requireContext().stopService(serviceIntent)
            Util.startForegroundService(requireContext(), serviceIntent!!)
            requireContext().bindService(
                serviceIntent,
                mConnection,
                AppCompatActivity.BIND_AUTO_CREATE
            )
            initializePlayer()
            setLog("videoListModel", "startPlayerService initializePlayer")
        } else {
            setLog("videoListModel", "startPlayerService mPlayer not null")
        }
    }

    private fun isMyMusicServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            requireContext().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if ((serviceClass.name == service.service.className)) {
                return true
            }
        }
        return false
    }



    private fun initBwd() {
        if (img_bwd_musicVideo != null) {

            img_bwd_musicVideo?.requestFocus()
            img_bwd_musicVideo?.setOnClickListener {
                mPlayer?.seekTo(mPlayer?.currentPosition!! - 10000)
                skipItroCreditShowHide(mPlayer?.currentPosition!!)
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videobackword${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerSkipBackwardEvent(hashMap))
            }
        }
    }

    private fun initFwd() {
        if (img_fwd != null&&isAdded()) {
            img_fwd?.requestFocus()
            img_fwd?.setOnClickListener {
                mPlayer?.seekTo(mPlayer?.currentPosition!! + 10000)
                skipItroCreditShowHide(mPlayer?.currentPosition!!)
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videoplayerforward${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerSkipForwardEvent(hashMap))
            }
        }

    }

    var isDisplaySubTite = false

    var isLockScreen = false
    private fun initLock() {
        if (rlLock != null&&isAdded()) {
            /*rlLock!!.setOnClickListener {
                setLog("TAG", "initLock: $isLockScreen")
                if(isLockScreen){

//                    video_player_view?.keepScreenOn=false
                    video_player_view.hideController()
                }else{
//                    video_player_view?.keepScreenOn = true
                    video_player_view.showController()
                }
                isLockScreen = !isLockScreen
            }*/
            rlLock?.setOnClickListener {
                controlsState = ControlsMode.LOCK
                rlCenterControll.visibility = View.INVISIBLE
                //llBritness.visibility = View.INVISIBLE
                //llVolume.visibility = View.INVISIBLE
                brightness_slider_container.visibility = View.INVISIBLE
                volume_slider_container.visibility = View.INVISIBLE
                rlBottomControll.visibility = View.INVISIBLE
                head.visibility = View.INVISIBLE
                //root.visibility = View.GONE
                rlUnLock.visibility = View.VISIBLE
                rlUnLock.visibility = View.VISIBLE
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, "lock")
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                /*As per https://hungama.atlassian.net/browse/HU-5704 -
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)*/
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videoPlaylock${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerLockTappedEvent(hashMap))
            }

            rlUnLock?.setOnClickListener {
                controlsState = ControlsMode.FULLCONTORLS
                rlCenterControll.visibility = View.VISIBLE
                //llBritness.visibility = View.VISIBLE
                //llVolume.visibility = View.VISIBLE
                brightness_slider_container.visibility = View.VISIBLE
                volume_slider_container.visibility = View.VISIBLE
                rlBottomControll.visibility = View.VISIBLE
                head.visibility = View.VISIBLE
                //root.visibility = View.VISIBLE
                rlUnLock.visibility = View.INVISIBLE
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, "unlock")
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                /*As per https://hungama.atlassian.net/browse/HU-5704 -
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)*/
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videoPlayunlock${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerLockTappedEvent(hashMap))
            }

        }

    }

    private fun initBrightness() {
        if (sbBrightness != null&&isAdded()) {


            // Check whether has the write settings permission or not.
//            val settingsCanWrite = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                hasWriteSettingsPermission(this@VideoPlayerActivity)
//            } else {
//                true
//            }
//            // If do not have then open the Can modify system settings panel.
//            if (!settingsCanWrite) {
//                changeWriteSettingsPermission(this@VideoPlayerActivity)
//            }

            sbBrightness!!.requestFocus()
            sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                    setLog("TAG", "onProgressChanged: $progress")
//                    if (!settingsCanWrite) {
//                        changeWriteSettingsPermission(this@VideoPlayerActivity)
//                    } else {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            setBrightness(this@VideoPlayerActivity,progress)
//                        }
//                    }

                    initBrightnessTouch(progress)


                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    private fun initBrightnessTouch(progress: Int) {

        var brightnessValue = progress / 255f
        val lp = requireActivity().window.attributes

        setLog("TAG", "initBrightnessTouch: $brightnessValue")
        lp.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        lp.screenBrightness = brightnessValue.toFloat()
        requireActivity().window.attributes = lp
    }

    private fun initVolume() {
        volIcon?.setOnClickListener {
            //setLog("OnMute", "True")
            if (isMute) {
                isMute = false
                volume_slider.progress = lastVolumeProgress
                volIcon.setImageResource(R.drawable.volume)
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    lastVolumeData,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                )
            } else {
                isMute = true
                //lastVolumeData = volume_slider.progress
                volume_slider.progress = 0
                volIcon.setImageResource(R.drawable.volume_mute)
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    0,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                )
            }

        }
        if (sbVolume != null&&isAdded()) {

            sbVolume.setProgress(mPlayer?.volume?.toInt()!!)
            sbVolume!!.requestFocus()
            sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                    var volume = (progress.toFloat() / 100f).toFloat()
                    setLog("TAG", "onProgressChanged: $volume")
                    mPlayer?.volume = volume
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    private fun videoQualitySettings() {
        if (rlSettings != null&&isAdded()) {

            rlSettings.setOnClickListener {
                _arrayOfBitrate = ArrayList()
                trackSelector = mPlayer?.trackSelector as DefaultTrackSelector?
                if (trackSelector != null) {
//                    val mappedTrackInfo: MappingTrackSelector.MappedTrackInfo? = trackSelector?.currentMappedTrackInfo
//                    if (mappedTrackInfo != null) {
//                        if (!isShowingTrackSelectionDialog!! && TrackSelectionDialog.willHaveContent(
//                                trackSelector
//                            )
//                        ) {
//                            isShowingTrackSelectionDialog = true
//                            val trackSelectionDialog =
//                                TrackSelectionDialog.createForTrackSelector(trackSelector)  /* onDismissListener= */{ dismissedDialog ->
//                                    isShowingTrackSelectionDialog = false
//                                }
//                            trackSelectionDialog.show(supportFragmentManager,  /* tag= */null)
//                        }
//                    }

                    // These two could be fields OR passed around
                    var videoRendererIndex: Int
                    var trackGroups: TrackGroupArray
                    val mappedTrackInfo: MappingTrackSelector.MappedTrackInfo? =
                        trackSelector?.currentMappedTrackInfo
                    if (mappedTrackInfo != null){
                        for (i in 0 until mappedTrackInfo.rendererCount) {
                            val trackGroups = mappedTrackInfo.getTrackGroups(i)
                            for (groupIndex in 0 until trackGroups?.length!!) {
                                val group = trackGroups.get(groupIndex);
                                setLog("TAG", "trackGroups: $trackGroups")
                                for (trackIndex in 0 until group?.length!!) {
                                    if (group.getFormat(trackIndex) != null && group.getFormat(
                                            trackIndex
                                        ).containerMimeType != null
                                    ) {
                                        //if (group.getFormat(trackIndex).containerMimeType?.contains("video/mp4")!!) {
                                            setLog(
                                                "TAG",
                                                "videoQualitySettings format:" + group.getFormat(
                                                    trackIndex
                                                )
                                            )
                                            _arrayOfBitrate.add(group.getFormat(trackIndex)?.bitrate!!)
                                        //}
                                    } else if (group.getFormat(trackIndex) != null && group.getFormat(
                                            trackIndex
                                        ).sampleMimeType != null
                                    ) {
                                        //if (group.getFormat(trackIndex).sampleMimeType?.contains("video/avc")!!) {
                                            setLog(
                                                "TAG",
                                                "videoQualitySettings format:" + group.getFormat(
                                                    trackIndex
                                                )
                                            )
                                            _arrayOfBitrate.add(group.getFormat(trackIndex)?.bitrate!!)
                                        //}
                                    }
                                }
                            }


                        }
                    }

                    setLog("TAG", "videoQualitySettings: ${_arrayOfBitrate.toString()}")
                    if (_arrayOfBitrate != null && _arrayOfBitrate.size > 0) {
                        getVideoBandwidth()
                    }
                }
            }
        }

    }


    private fun getVideoBandwidth() {
        var bandWidth: Int
        _arrayOfBandwidth.put(getString(R.string.quality_1), 50000)
        for (i in 0 until _arrayOfBitrate.size) {
            try {
                bandWidth = _arrayOfBitrate.get(i).toInt()
                setLog("bandWidth", "getNewBitrateText: $bandWidth")
                if (bandWidth > 0 && bandWidth <= 950000) {
                    if (!_arrayOfBandwidth.contains(getString(R.string.video_player_str_27))) {
                        _arrayOfBandwidth.put(getString(R.string.video_player_str_27), bandWidth)
                    }
                    setLog("bandWidth", "Data Saver $bandWidth")
                }
                if (bandWidth > 950000 && bandWidth <= 2200000) {
                    if (!_arrayOfBandwidth.containsKey(getString(R.string.video_player_str_28))) {
                        _arrayOfBandwidth.put(getString(R.string.video_player_str_28), bandWidth)
                    }
                    setLog("bandWidth", "Good $bandWidth")
                }
                /*if (bandWidth > 2200000 && bandWidth <= 3500000) {
                    if (!_arrayOfBandwidth.containsKey(getString(R.string.video_player_str_29))) {
                        _arrayOfBandwidth.put(getString(R.string.video_player_str_29), bandWidth)
                    }
                    setLog("bandWidth", "Better $bandWidth")
                }*/
                if (bandWidth > 2200000) {
                    if (!_arrayOfBandwidth.containsKey(getString(R.string.video_player_str_29))) {
                        _arrayOfBandwidth.put(getString(R.string.video_player_str_29), bandWidth)
                    }
                    setLog("bandWidth", "Best $bandWidth")
                }
                /*if (bandWidth > 3500000) {
                    if (!_arrayOfBandwidth.containsKey(getString(R.string.video_player_str_30))) {
                        _arrayOfBandwidth.put(getString(R.string.video_player_str_30), bandWidth)
                    }
                    setLog("bandWidth", "Best $bandWidth")
                }*/
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        setLog("TAG", "getVideoBandwidth: $_arrayOfBandwidth")

        if (_arrayOfBandwidth != null && _arrayOfBandwidth?.size!! > 0) {
            videoQualitySheetFragment = VideoQualitySelectBottomSheetFragment(_arrayOfBandwidth)
            videoQualitySheetFragment?.addVideoListener(this)
            videoQualitySheetFragment?.show(
                requireActivity().supportFragmentManager,
                "VideoQualitySelectBottomSheetFragment"
            )

        }
    }

    private fun getPlayerHeight(): Int {
        return CommonUtils.getDeviceWidth(requireContext()) * 9 / 16
    }

    private fun handleFullScreenEnterExit() {
        val display =
            (requireContext().getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val orientation = display.orientation
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        setLog("pipMode", "MusicVideoDetailsFragment-handleFullScreenEnterExit-isInPipMode-$isInPipMode-playerPosition-$playerPosition-video_player_view!=null-${video_player_view != null}-screenHeightInPotratitMode-$screenHeightInPotratitMode-orientation-$orientation")
        if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
            centerGradient.visibility = View.VISIBLE
            llBottomView.visibility = View.VISIBLE
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val dpValue = screenHeightInPotratitMode
            //val dpValue = resources.getDimensionPixelSize(R.dimen.dimen_200)
            val params: ViewGroup.LayoutParams = video_player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = dpValue
            //params.height = ViewGroup.LayoutParams.MATCH_PARENT
            video_player_view.requestLayout()

            /*val params2: ViewGroup.LayoutParams = videoPlayer.layoutParams
            params2.width = width
            params2.height = dpValue
            videoPlayer.requestLayout()*/
            isScreenLandscape = false
            //fullScreenCall()
            requireActivity().getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requireActivity().currentFocus?.requestLayout()
            show()
        } else {
            centerGradient.visibility = View.INVISIBLE
            llBottomView.visibility = View.GONE
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            //fullScreenCall()
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requireActivity().getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            requireActivity().currentFocus?.requestLayout()
            val params: ViewGroup.LayoutParams = video_player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            video_player_view.requestLayout()
            /*val params2: ViewGroup.LayoutParams = videoPlayer.layoutParams
            params2.width = ViewGroup.LayoutParams.MATCH_PARENT
            params2.height = width
            videoPlayer.requestLayout()*/
            isScreenLandscape = true
            hide()
        }
    }

    private fun fullScreenCall() {
        if (Build.VERSION.SDK_INT >= 21) {
            //for new api versions.
            val decorView = requireActivity().window.decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
            decorView.systemUiVisibility = uiOptions
        }
    }

    private fun hide() {
        try {
            // Hide UI first
            /*val actionBar = supportActionBar
            actionBar?.hide()
            head.visibility = View.GONE*/
            enableDisablePreviousNext(false)
            enableDisableForwardBackward(true)
            tvHeading.visibility = View.VISIBLE
            rlExtraFeature.visibility = View.VISIBLE
            //full_screen_enter_exit.visibility = View.GONE
            full_screen_enter_exit.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_minimize_video
                )
            )
            (activity as MainActivity).hideMiniPlayer()
            (activity as MainActivity).hideBottomNavigationBar()

        }catch (e:Exception){

        }

    }

    private fun show() {
        /*
        val actionBar = supportActionBar
        actionBar?.show()
        head.visibility = View.VISIBLE*/

        enableDisablePreviousNext(true)
        enableDisableForwardBackward(false)
        tvHeading.visibility = View.GONE
        rlExtraFeature.visibility = View.GONE
        hideBrightnessAndVolume()
        // full_screen_enter_exit.visibility = View.VISIBLE
        full_screen_enter_exit.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_full_screen_video
            )
        )
        if (!isScreenLandscape){
            (activity as MainActivity).showBottomNavigationBar()
        }
    }

    override fun onVideoResolutionClick(videoQuality: VideoQuality) {
        setLog("TAG", "onVideoResolutionClick:" + videoQuality)

        val hashMap = java.util.HashMap<String, String>()
        hashMap.put(EventConstant.STREAMQUALITYSELECTED_EPROPERTY, "" + videoQuality?.bandwidth)
        hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
        /*As per https://hungama.atlassian.net/browse/HU-5705 -
        hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)*/
        hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
        setLog("TAG", "videoplayerforward${hashMap}")
        EventManager.getInstance().sendEvent(VideoPlayerVideoQualitySelectedEvent(hashMap))

        SharedPrefHelper.getInstance().save(PrefConstant.LAST_VIDEO_QUALITY, videoQuality.title)
        trackSelector = mPlayer?.trackSelector as DefaultTrackSelector?
//        trackSelector?.setParameters(
//            trackSelector?.buildUponParameters()?.setMaxVideoBitrate(videoQuality.bitrate!!)!!
//        )

        val param =
            DefaultTrackSelector.ParametersBuilder().setMaxVideoBitrate(videoQuality.bitrate!!)
                .build()

        trackSelector?.parameters = param


        if (videoQualitySheetFragment != null) {
            videoQualitySheetFragment?.dismiss()
        }
    }

    fun enterPIPMode() {
        try {
            setLog(
                "enterPIPMode",
                "enterPIPMode isAdded:${isAdded()} activityVisible:${HungamaMusicApp.getInstance().activityVisible!!}"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && mPlayer != null && isAdded()
            ) {
                val appOpsManager =
                    requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                if (appOpsManager != null && appOpsManager.checkOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        requireContext().packageManager.getApplicationInfo(
                            requireContext().packageName,
                            PackageManager.GET_META_DATA
                        ).uid,
                        requireContext().packageName
                    ) == AppOpsManager.MODE_ALLOWED
                ) {
                    playerPosition = mPlayer?.currentPosition!!
                    video_player_view.useController = false
                    hide()
                    setLog(
                        "enterPIPMode",
                        "enterPIPMode 1 isAdded:${isAdded()} activityVisible:${HungamaMusicApp.getInstance().activityVisible!!}"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val sourceRectHint = Rect()
                        video_player_view.getGlobalVisibleRect(sourceRectHint)
                        val aspectRatio = Rational(16, 9)
                        val params = PictureInPictureParams
                            .Builder()
                            // Set action items for the picture-in-picture mode. These are the only custom controls
                            // available during the picture-in-picture mode.
                            .setActions(
                                listOf(
                                    if (shouldShowPauseButton(mPlayer!!)) {
                                        // "Pause" action when the pip is already started.
                                        createRemoteAction(
                                            androidx.media3.ui.R.drawable.exo_notification_pause,
                                            R.string.notification_pause,
                                            REQUEST_START_OR_PAUSE,
                                            CONTROL_TYPE_START_OR_PAUSE
                                        )
                                    } else {
                                        // "Start" action when the pip is not started.
                                        createRemoteAction(
                                            androidx.media3.ui.R.drawable.exo_notification_play,
                                            R.string.notification_play,
                                            REQUEST_START_OR_PAUSE,
                                            CONTROL_TYPE_START_OR_PAUSE
                                        )
                                    }
                                )
                            )
                            .setAspectRatio(aspectRatio)
                            .setAutoEnterEnabled(true)
                            .setSeamlessResizeEnabled(true)
                            .setSourceRectHint(sourceRectHint)
                            .build()
                        requireActivity().setPictureInPictureParams(params)
                        requireActivity().enterPictureInPictureMode(params)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val aspectRatio = Rational(16, 9)
                        val params = PictureInPictureParams
                            .Builder()
                            // Set action items for the picture-in-picture mode. These are the only custom controls
                            // available during the picture-in-picture mode.
                            .setActions(
                                listOf(
                                    if (shouldShowPauseButton(mPlayer!!)) {
                                        // "Pause" action.
                                        createRemoteAction(
                                            androidx.media3.ui.R.drawable.exo_notification_pause,
                                            R.string.notification_pause,
                                            REQUEST_START_OR_PAUSE,
                                            CONTROL_TYPE_START_OR_PAUSE
                                        )
                                    } else {
                                        // "Start" action.
                                        createRemoteAction(
                                            androidx.media3.ui.R.drawable.exo_notification_play,
                                            R.string.notification_play,
                                            REQUEST_START_OR_PAUSE,
                                            CONTROL_TYPE_START_OR_PAUSE
                                        )
                                    }
                                )
                            )
                            .setAspectRatio(aspectRatio)
                            .build()
                        requireActivity().setPictureInPictureParams(params)
                        requireActivity().enterPictureInPictureMode(params)

                    } else {
                        requireActivity().enterPictureInPictureMode()
                    }
                    /* We need to check this because the system permission check is publically hidden for integers for non-manufacturer-built apps
                       https://github.com/aosp-mirror/platform_frameworks_base/blob/studio-3.1.2/core/java/android/app/AppOpsManager.java#L1640
                       ********* If we didn't have that problem *********
                        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                        if(appOpsManager.checkOpNoThrow(AppOpManager.OP_PICTURE_IN_PICTURE, packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).uid, packageName) == AppOpsManager.MODE_ALLOWED)
                        30MS window in even a restricted memory device (756mb+) is more than enough time to check, but also not have the system complain about holding an action hostage.
                     */
                } else {
                    setLog(
                        "enterPIPMode",
                        "enterPIPMode 2 isAdded:${isAdded()} activityVisible:${HungamaMusicApp.getInstance().activityVisible!!}"
                    )
                }
            } else {
                setLog(
                    "enterPIPMode",
                    "enterPIPMode 3 isAdded:${isAdded()} activityVisible:${HungamaMusicApp.getInstance().activityVisible!!}"
                )
            }
        } catch (e: Exception) {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkPIPPermission() {
        isPIPModeeEnabled = requireActivity().isInPictureInPictureMode
        //setLog("LifeCycle:--", "requireActivity().isInPictureInPictureMode-"+requireActivity().isInPictureInPictureMode +" requireActivity()-"+requireActivity().localClassName)
        if (!requireActivity().isInPictureInPictureMode) {
            requireActivity().onBackPressed()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        setLog("pipMode", "MusicVideoDetailsFragment-onPictureInPictureModeChanged-1-isInPipMode-$isInPipMode-playerPosition-$playerPosition")
        if (mPlayer != null) {
            /*if (newConfig != null) {
                playerPosition = mPlayer?.currentPosition!!
                isInPipMode = !isInPictureInPictureMode
            }*/
            playerPosition = mPlayer?.currentPosition!!
        }
        setLog("pipMode", "MusicVideoDetailsFragment-onPictureInPictureModeChanged-2-isInPipMode-$isInPipMode-playerPosition-$playerPosition")
        if (isInPictureInPictureMode) {
            setLog("pipOn", "Yes")
            isInPipMode = true
        } else {
            setLog("pipOn", "Noo")
            isInPipMode = false
        }
        setLog("pipMode", "MusicVideoDetailsFragment-onPictureInPictureModeChanged-3-isInPipMode-$isInPipMode-playerPosition-$playerPosition")
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }


    fun playPause() {
        if (currentPlayer != null) {
            if (isPlaying()) {
                pausePlayer()
            } else {
                playPlayer()
            }
        }
    }

    fun pausePlayer(){
        if(isAdded && context != null&&currentPlayer!=null){
            setLog("pipMode", "MusicVideoDetailsFragment-pausePlayer-called")
            currentPlayer?.pause()
            btn_pause.visibility = View.GONE
            btn_play.visibility = View.VISIBLE
            if (!videoList.isNullOrEmpty() && videoList?.size!! > playableItemPosition){
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                // As per jira bug we are remove content name property - https://hungama.atlassian.net/browse/HU-3742
                //hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)
                if (isScreenLandscape){
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                }else{
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_PORTRAIT_PLAYER)
                }
                setLog("TAG", "videopause${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerPauseEvent(hashMap))
            }
        }

    }

    fun playPlayer(){
        if(isAdded && context != null&&currentPlayer!=null){
            currentPlayer?.play()
            btn_pause.visibility = View.VISIBLE;
            btn_play.visibility = View.GONE;
            if (!videoList.isNullOrEmpty() && videoList?.size!! > playableItemPosition){
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                // As per jira bug we are remove content name property - https://hungama.atlassian.net/browse/HU-3742
                //hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)
                if (isScreenLandscape){
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                }else{
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_PORTRAIT_PLAYER)
                }
                setLog("TAG", "videoPlay${hashMap}")


                CoroutineScope(Dispatchers.Main).launch {
                    val hashMapPageView = java.util.HashMap<String, String>()

                    hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = videoList.get(playableItemPosition).data.head.headData.title
                    hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] = "details_" + Utils.getContentTypeNameForStream("" + videoList.get(playableItemPosition).data?.head?.headData?.type).replace("_","").lowercase()
                    hashMapPageView[EventConstant.SOURCEPAGETYPE_EPROPERTY] = "" + Utils.getContentTypeNameForStream("" + videoList.get(playableItemPosition).data?.head?.headData?.type)
                    hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = "" + videoList.get(playableItemPosition).data?.head?.headData?.id
                    hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                    hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] = "" + MainActivity.lastItemClicked + "," + MainActivity.headerItemName
                    hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "player_video_" + Utils.getContentTypeNameForStream("" + videoList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type).replace("_", "").lowercase()

                    setLog("VideoPlayerPageView", hashMapPageView.toString())
                    EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                }

                EventManager.getInstance().sendEvent(VideoPlayerPlayEvent(hashMap))
            }
        }

    }

    fun isPlaying(): Boolean {
        if (currentPlayer != null) {
            return currentPlayer?.isPlaying!!
        }
        return false
    }


    private fun enableDisablePreviousNext(action: Boolean) {
        if (action) {
            isEnableNextPrvious = action
            exo_previous_btn.show()
            exo_next_btn.show()
        } else {
            isEnableNextPrvious = action
            exo_previous_btn.hide()
            exo_next_btn.hide()
        }
    }

    private fun enableDisableForwardBackward(action: Boolean) {
        if (action) {
            isEnableForwardBackward = action
            img_bwd_musicVideo.show()
            img_fwd.show()
        } else {
            isEnableForwardBackward = action
            img_bwd_musicVideo.hide()
            img_fwd.hide()
        }
    }

    private fun getUserBookmarkedData() {
        setLog("MusicVideoDetailFragment", "getUserBookmarkedData-isShowNoInternetPopUp-$isShowNoInternetPopUp")
        if (ConnectionUtil(requireContext()).isOnline(isShowNoInternetPopUp)) {
            userViewModel?.getUserBookmarkedData(requireContext(), Constant.MODULE_WATCHLIST)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    fillUI(it?.data)
                                }

                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR -> {
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
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun fillUI(bookmarkDataModel: BookmarkDataModel) {
        if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0) {
            for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!) {
                setLog(
                    TAG,
                    "setFollowingStatus videoDetailRespModel id: ${videoDetailRespModel?.data?.head?.data?.id}"
                )
                setLog(TAG, "setFollowingStatus bookmark?.data?.id: ${bookmark?.data?.id}")
                if (videoDetailRespModel?.data?.head?.data?.id?.equals(bookmark?.data?.id)!!) {
                    isAddWatchlist = true
                }
            }
            setFollowingStatus()
        }
    }

    fun setFollowingStatus() {

        setLog(TAG, "setFollowingStatus: $isAddWatchlist")
        if (isAddWatchlist) {
            /*val drawable = FontDrawable(requireContext(), R.string.icon_tick)
            drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            ivWatchlist.setImageDrawable(drawable)*/
            ivWatchlist.setImageDrawable(
                requireContext().faDrawable(R.string.icon_tick, R.color.colorWhite)
            )
        } else {
            /*val drawable = FontDrawable(requireContext(), R.string.icon_watchlist)
            drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            ivWatchlist.setImageDrawable(drawable)*/
            ivWatchlist.setImageDrawable(
                requireContext().faDrawable(R.string.icon_watch_later, R.color.colorWhite)
            )
        }

    }

    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
    }

    private fun setupVideoDetailViewModel() {
        videoDetailViewModel = ViewModelProvider(
            this
        ).get(VideoDetailsViewModel::class.java)
        setLog("MusicVideoDetailFragment", "setupVideoDetailViewModel-isShowNoInternetPopUp-$isShowNoInternetPopUp")
        if (ConnectionUtil(context).isOnline(isShowNoInternetPopUp)) {
            progress?.visibility = View.VISIBLE
            videoDetailViewModel.getVideoDetail(requireContext(), selectedContentId.toString())
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                fillMusicVidoDetail(it?.data!!)

                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR -> {
                                setEmptyVisible(true)
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
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun fillMusicVidoDetail(data: PlaylistDynamicModel) {
        setProgressBarVisible(false)
        if (data != null) {
            setLog(TAG, "videoDetailRespObserver $data")
            setVideoDetailData(data)
        } else {
            setLog(
                TAG,
                "setupVideoDetailViewModel -> setUpVideoListViewMode"
            )
            setUpVideoListViewModel(selectedContentId)
        }
    }

    fun getYouMayLikeCall(homedata: PlaylistDynamicModel?) {
        val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url= WSConstants.METHOD_YOU_MAY_LIKE_MUSIC_VIDEO+"?contentId="+selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
            Observer {
                when(it.status){
                    com.hungama.music.data.webservice.utils.Status.SUCCESS->{

                        setLog(TAG, "getYouMayLikePodcastCall: data:${it?.data} ")
                        setLog(TAG, "getYouMayLikePodcastCall: before size:${homedata?.data?.body?.recomendation?.size}")

                        var dailyDoseIndex=0
                        var podcastSize=homedata?.data?.body?.recomendation?.size
                        if(it?.data!=null&&it?.data?.data?.body!=null&&it?.data?.data?.body?.searchRecommendations?.size!!>0){
                            it?.data?.data?.body?.searchRecommendations?.forEach {
                                if (podcastSize != null) {
                                    homedata?.data?.body?.recomendation?.add(podcastSize+dailyDoseIndex,it)
                                }
                                dailyDoseIndex+=1

                                setLog(TAG, "getYouMayLikePodcastCall: dailyDoseIndex:${dailyDoseIndex} ")
                            }
                        }
                        fillMusicVidoDetail(homedata!!)



                        setLog(TAG, "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}")
                    }

                    com.hungama.music.data.webservice.utils.Status.LOADING ->{
                        setProgressBarVisible(true)
                    }

                    com.hungama.music.data.webservice.utils.Status.ERROR ->{
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        fillMusicVidoDetail(homedata!!)
                    }
                }
            })
    }

    fun setVideoDetailData(videoDetailModel: PlaylistDynamicModel) {
        setLog(TAG, "setVideoDetailData")

        if (videoDetailModel != null && videoDetailModel.data != null) {
            var attributeCensorRating = ""
            if (!videoDetailModel.data.head.data.misc?.attributeCensorRating.isNullOrEmpty()) {
                attributeCensorRating =
                    videoDetailModel.data.head.data.misc?.attributeCensorRating?.get(0).toString()
            }
            //attributeCensorRating = "13+"
//            if (!CommonUtils.checkUserCensorRating(
//                    requireContext(),
//                    attributeCensorRating,
//                    null,
//                    is13PlusVideoSettingPopupVisible
//                )
//            ) {
//
//            } else {
//                setLog(TAG, "Music-Video-detail content check")
//                is13PlusVideoSettingPopupVisible = false
//            }

            videoDetailRespModel = videoDetailModel
            if (videoDetailModel?.data?.body != null && !videoDetailModel?.data?.body?.recomendation.isNullOrEmpty()) {
                videoDetailModel?.data?.body?.recomendation?.forEachIndexed { index, rowsItem ->
                    if (rowsItem != null && !rowsItem.keywords.isNullOrEmpty()) {
                        if (rowsItem.keywords?.get(0).equals("similar-videos")) {
                            similarVideoIndex = index
                        }
                    }
                }
            }
            setVideoData(videoDetailModel)
            if (videoDetailModel.data.head.data.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
                setMusicVideoData(videoDetailModel)
            } else {
                setVideoLikeData(videoDetailModel)
            }

            //setSimilarVideoData(videoDetailModel)
            setRecommendedVideoData(videoDetailModel)
            getUserBookmarkedData()
        }
    }

    private fun setVideoData(videoDetailModel: PlaylistDynamicModel) {
        setLog(TAG, "setVideoData")
        if (videoDetailModel.data.head != null && videoDetailModel.data.head.data != null) {
            playerType = videoDetailModel.data.head.data.type.toString()
            llButton.visibility = View.VISIBLE
//                videoTitle.text = videoDetailModel.data.head.data.title
            var lang =
                videoDetailModel.data.head.data.misc?.lang?.joinToString(separator = "|") { it }
            /*videoSubtitle.text =
            lang + " - " + CommonUtils.ratingWithSuffix(videoDetailModel.data.head.data.misc.playcount.toString()) + "+ Views"*/
            videoSubtitle.text =
                lang + " • " + videoDetailModel.data.head.data.misc?.f_playcount + " " + getString(
                    R.string.discover_str_25
                )
        }
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        videoDetailroot?.visibility = View.VISIBLE
    }

    private fun setVideoLikeData(videoDetailModel: PlaylistDynamicModel) {

        videoDetailModel.data.body.rows =
            getAdsData(videoDetailModel.data.body.rows)
        if (videoDetailModel.data.body != null && !videoDetailModel.data.body.rows.isNullOrEmpty()) {
            rvVideoLike.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = VideoLikeAdapter(context, videoDetailModel.data.body.rows,
                    object : VideoLikeAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            /*val bundle = Bundle()
                        bundle.putString(
                            "id",
                            videoDetailModel.data.body.artistvideos.get(childPosition).data.id
                        )

                        val videoDetailsFragment = MusicVideoDetailsFragment()
                        videoDetailsFragment.arguments = bundle
                        refreshCurrentFragment(requireActivity(), videoDetailsFragment)*/
                            playNextVideo(
                                videoDetailModel.data.body.rows.get(
                                    childPosition
                                ).data.id
                            )
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            videoHeading?.visibility = View.VISIBLE
            llVideoLike.visibility = View.VISIBLE
        } else {
            llVideoLike.visibility = View.GONE
        }
    }

    private fun setMusicVideoData(videoDetailModel: PlaylistDynamicModel) {
        var allVideo = videoDetailModel.data.head?.data?.items?.size
        videoDetailModel.data.head?.data?.items =
            getAdsData(videoDetailModel.data.head.data.items)

        if (videoDetailModel.data.head != null && !videoDetailModel.data.head.data.items.isNullOrEmpty()) {
            var totalDuration = 0L
            for (item in videoDetailModel.data.head.data.items.iterator()) {
                if (!TextUtils.isEmpty(item.data.duration)){
                    totalDuration += item.data.duration.toInt()
                }

            }
            var hours: Int = (totalDuration / 3600).toInt()
            var temp: Int = (totalDuration - hours * 3600).toInt()
            var mins: Int = (temp / 60).toInt()
            temp = temp - mins * 60
            if (hours > 0) {
                requiredFormat = hours.toString() + "h" + mins + "min"
            } else {
                requiredFormat = mins.toString() + "min"
            }

            videoHeading.visibility = View.GONE

            videoDetail.text =
                "" + allVideo + getString(R.string.discover_str_7) + " • " + requiredFormat

            mVideoDetailAdapter =
                VideoDetailAdapter(requireActivity(), videoDetailModel.data.head.data.items,
                    object : VideoDetailAdapter.OnItemClick {
                        override fun onUserClick(position: Int) {
                            currentVideoPlaylistPosition = position

                            if (videoDetailRespModel?.data?.head?.data?.items?.get(
                                    currentVideoPlaylistPosition
                                )?.itype == Constant.musicVideoNativeAds
                            ) {
                                currentVideoPlaylistPosition = currentVideoPlaylistPosition + 1
                            }
                            changeMusicVideo(
                                videoDetailRespModel?.data?.head?.data?.items?.get(
                                    currentVideoPlaylistPosition
                                )?.data?.id.toString()
                            )
                        }
                    })

            rvVideoLike2.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = mVideoDetailAdapter
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llVideoLike2.visibility = View.VISIBLE
        } else {
            llVideoLike2.visibility = View.GONE
        }
    }

    private fun setRecommendedVideoData(videoDetailModel: PlaylistDynamicModel){
        if (videoDetailModel.data?.body?.recomendation != null && videoDetailModel.data?.body?.recomendation?.size!! > 0) {
            rvRecomendation.visibility = View.VISIBLE

            var varient = Constant.ORIENTATION_HORIZONTAL

            val bucketParentAdapter = BucketParentAdapter(
                videoDetailModel.data?.body?.recomendation!!,
                requireContext(),
                this,
                this,
                Constant.WATCH_TAB,
                HeadItemsItem(),
                varient
            )

            val mLayoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
            )
            rvRecomendation?.layoutManager = mLayoutManager
            rvRecomendation?.adapter = bucketParentAdapter


            bucketParentAdapter?.addData(videoDetailModel.data?.body?.recomendation!!)
            rvRecomendation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                    val lastVisiable: Int =
                        mLayoutManager?.findLastCompletelyVisibleItemPosition()!!

                    setLog(
                        TAG,
                        "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                    )
                    if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                        var fromBucket =
                            videoDetailModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                        var toBucket =
                            videoDetailModel.data?.body?.recomendation?.get(lastVisiable)?.heading
                        var sourcePage =
                            MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                        if (!fromBucket?.equals(toBucket, true)!!) {
                            callPageScrolledEvent(
                                sourcePage,
                                "" + lastVisiable,
                                fromBucket!!,
                                toBucket!!
                            )
                        }

                    }
                }
            })
            rvRecomendation.setPadding(0, 0, 0, 0)
        }
    }


    private fun setUpVideoListViewModel(selectedContentId: String) {
        videoListViewModel = ViewModelProvider(this)[VideoViewModel::class.java]
        setLog("MusicVideoDetailFragment", "setUpVideoListViewModel-isShowNoInternetPopUp-$isShowNoInternetPopUp")
        if (ConnectionUtil(requireContext()).isOnline(isShowNoInternetPopUp)) {
            videoListViewModel?.getVideoList(requireContext(), selectedContentId, 5)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            setLog(
                                TAG,
                                "setUpVideoListViewModel getVideoList selectedContentId ${selectedContentId}"
                            )
                            if (it?.data != null) {
                                setVideoListData(it?.data)
                            } else {
                                setLog(TAG, "setUpVideoListViewModel -> setUpVideoListViewMode")
                                setUpVideoListViewModel(selectedContentId)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBarVisible(false)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
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
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    fun setVideoListData(model: PlayableContentModel) {
        if (model != null) {
            if (videoDetailRespModel != null && videoDetailRespModel?.data?.head?.data?.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
                videoList.clear()
            }

            videoListModel = model

            val dpm = DownloadPlayCheckModel()
            dpm.contentId = videoListModel?.data?.head?.headData?.id?.toString()!!
            dpm.contentTitle = videoListModel?.data?.head?.headData?.title?.toString()!!
            dpm.planName = videoListModel?.data?.head?.headData?.misc?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload =
                RestrictedDownload.valueOf(videoListModel?.data?.head?.headData?.misc?.restricted_download!!)
            setLog("videoListModel", "setVideoListDatavideoListModel")
            setLog("MusicVideo", "MusicVideoDetailFragment-setVideoListData-videoList.size-"+videoList.size + "  playableItemPosition- $playableItemPosition")
            //if (CommonUtils.userCanDownloadContent(requireContext(), musicVideoDetailroot, dpm, this)){
            videoList.add(videoListModel!!)
            if (videoList.size > 0) {
                startPlayerService(videoList)
                ////startPlayerService(getVideoDummyData3("https://hunstream.hungama.com/c/5/968/9d6/65999361/65999361_,100,400,750,1000,1600,.mp4.m3u8?Zfr3jvMVuMCuuwu46DZZ5uqb28GroeK9Yb1HM4rXa3xxr1RKK2k8AIJJBVF6zK6fMd0xwPubXH5QQyW-viS58-Yeq0vivoyo6KVpFHlAZeQLRQKNi8BbceKIwBYQ"))
            } else {
                setLog("NoSong", "NoSong")
            }
            //}
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            videoDetailroot?.visibility = View.VISIBLE

            if (videoDetailRespModel != null && videoDetailRespModel?.data?.head?.data?.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
                videoDetailRespModel?.data?.head?.data?.items?.forEach {
                    if (currentVideoPlaylistPosition >= 0 && videoDetailRespModel?.data?.head?.data?.items?.get(
                            currentVideoPlaylistPosition
                        )?.data?.id.equals(it?.data?.id)
                    ) {
                        it?.data?.isCurrentPlaying = true
                    } else {
                        it?.data?.isCurrentPlaying = false
                    }
                }
                mVideoDetailAdapter?.addData(videoDetailRespModel?.data?.head?.data?.items!!)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mPlayer != null) {
            playerPosition = mPlayer?.currentPosition!!
            outState.putLong("PLAYER_POSITION", playerPosition)
            if(playerPosition>0){
                playerPosition=playerPosition
                setLog("LifeCycle:--", "onSaveInstanceState-playerPosition-$playerPosition")
                mPlayer?.seekTo(playerPosition)
            }

            getPlayerWhenReady = mPlayer!!.playWhenReady
            outState.putBoolean("PLAY_WHEN_READY", getPlayerWhenReady)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        if (mBound && isMyMusicServiceRunning(VideoPlayerService::class.java)) {
            setLog("LifeCycle:--", "ReBind Service")
            serviceIntent = Intent(requireContext(), VideoPlayerService::class.java)
            val serviceBundle = Bundle()
            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, videoList)
            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
            serviceIntent?.putExtra(Constant.BUNDLE_KEY, serviceBundle)
            /*if(mService != null){
                mService?.bindService(intent, mConnection, BIND_AUTO_CREATE)
            }*/
            requireContext().bindService(
                serviceIntent,
                mConnection,
                AppCompatActivity.BIND_AUTO_CREATE
            )
        }
//        initializePlayer()
        setLog("pipMode", "MusicVideoDetailsFragment-onResume-isInPipMode-$isInPipMode-playerPosition-$playerPosition-mPlayer!=null-${mPlayer != null}")
        if (mPlayer != null && playerPosition > 0L && !isInPipMode && !isShareClick) {
            setLog("LifeCycle:--", "Resume-playerPosition-$playerPosition")
            mPlayer?.seekTo(playerPosition)
        }
        isShareClick = false
        if (isInPipMode) {
            show()
        } else {
            if (isScreenLandscape){
                hide()
            }else{
                show()
            }

        }
        //Makes sure that the media controls pop up on resuming and when going between PIP and non-PIP states.
        video_player_view.useController = true
        setLog("LifeCycle:--", "Resume")
        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
            ArrayList(),
            this,
            true,
            false
        )
    }

    override fun onStop() {
        super.onStop()
        setLog("LifeCycle:--", "Stop")
        setLog("pipMode", "MusicVideoDetailsFragment-onStop-isInPipMode-$isInPipMode-playerPosition-$playerPosition")
        //PIPmode activity.finish() does not remove the activity from the recents stack.
        //Only finishAndRemoveTask does this.
        //But here we are using finish() because our Mininum SDK version is 16
        //If you use minSdkVersion as 21+ then remove finish() and use finishAndRemoveTask() instead
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            && requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {

            setLog("LifeCycle:--", "isInPipMode: " + isInPipMode.toString())
            if (isInPipMode) {
                setLog("LifeCycle:--", "Stop with stop service")
                pausePlayer()
            }

        }

        removePlayerDurationCallback()
    }

    override fun onDestroy() {

        try {
            setLog("pipMode", "MusicVideoDetailsFragment-onDestroy-isInPipMode-$isInPipMode-playerPosition-$playerPosition")
            onBackPressedCall()
//            stopChormeCast()
            mPlayerView = null
        } catch (e: Exception) {

        }

        val display =
            (requireContext().getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val orientation = display.orientation
        if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            requireActivity().getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requireActivity().currentFocus?.requestLayout()
            (activity as MainActivity).showBottomNavigationBar()
        }
        stopPlayerBGService()
        playableItemPosition = 0
        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).showMiniPlayer()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        //(requireActivity() as MainActivity).resumePlayer()

        removePlayerDurationCallback()
        super.onDestroy()
    }

    fun stopPlayerBGService() {
        if (mBound) {
            mBound = false;
            if(videoListModel!=null){
                callStreamEventAnalytics(videoListModel!!)
            }

//            stopChormeCast()
            if (null != serviceIntent) {
                requireContext().stopService(serviceIntent)
            }
            requireContext().unbindService(mConnection)
            if (mediaSession != null){
                mediaSession?.release()
            }
            mPlayer?.release()
            mPlayer = null
            video_player_view?.player = null
            mainHandler?.removeCallbacks(hideControls!!)
            (requireActivity() as MainActivity).removeVideoDownloadListener()
            try {
                requireContext().unregisterReceiver(broadcastReceiver)
            }catch (e:Exception){

            }

            //video_player_view.player = null
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, minflater: MenuInflater) {

        super.onCreateOptionsMenu(menu, minflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return onPrepareOptionsMenu(menu)
    }

    var touchGesture = 0
    var touchGestureValue = ""
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val display =
            (requireContext().getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val orientation = display.orientation
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                setLog("ActionTouch", "ACTION_DOWN")
                if (event.x < (sWidth / 2)) {
                    setLog("ActionTouch", "ACTION_DOWN 10")
                    intLeft = true
                    intRight = false
                } else if (event.x > (sWidth / 2)) {
                    setLog("ActionTouch", "ACTION_DOWN 9")
                    intLeft = false
                    intRight = true
                }
                val upperLimit = (sHeight / 4) + 100
                val lowerLimit = ((sHeight / 4) * 3) - 150
                if (event.y < upperLimit) {
                    intBottom = false
                    intTop = true
                } else if (event.y > lowerLimit) {
                    intBottom = true
                    intTop = false
                } else {
                    intBottom = false
                    intTop = false
                }
                // seekSpeed = (TimeUnit.MILLISECONDS.toSeconds(mPlayer?.getDuration()!!) * 0.1);
                diffX = 0
                calculatedTime = 0;
                seekDur = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(diffX) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffX)),
                    TimeUnit.MILLISECONDS.toSeconds(diffX) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffX))
                );

                //TOUCH STARTED
                baseX = event.getX()
                baseY = event.getY()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                setLog("ActionTouch", "ACTION_MOVE")
                screen_swipe_move = true;
                if (controlsState == ControlsMode.FULLCONTORLS) {
                    setLog("ActionTouch", "ACTION_MOVE 1")
                    diffX = (Math.ceil((event.getX() - baseX).toDouble())).toLong()
                    diffY = Math.ceil((event.getY() - baseY).toDouble()).toInt()
                    val brightnessSpeed = 0.05
                    if (Math.abs(diffY) > MIN_DISTANCE) {
                        tested_ok = true;
                    }
                    rlCenterControll.visibility = View.INVISIBLE
                    rlBottomControll.visibility = View.INVISIBLE
                    head.visibility = View.INVISIBLE
                    if (Math.abs(diffY) > Math.abs(diffX)) {
                        setLog("ActionTouch", "ACTION_MOVE 2")
                        if ((orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270)) {
                            if (intLeft) {
                                setLog("ActionTouch", "ACTION_MOVE 4")
                                cResolver = requireActivity().getContentResolver()
                                try {
                                    /*var writePermission = true
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            writePermission = Settings.System.canWrite(this)
                                    }
                                    if (writePermission) {
                                        Settings.System.putInt(
                                            cResolver,
                                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                                        )
                                        brightness = Settings.System.getInt(
                                            cResolver,
                                            Settings.System.SCREEN_BRIGHTNESS
                                        )
                                    }else {
                                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                            .setData(Uri.parse("package:" + getPackageName()))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        return false
                                    }*/

                                } catch (e: Settings.SettingNotFoundException) {
                                    e.printStackTrace()
                                }
                                var new_brightness = (brightness - diffY * brightnessSpeed).toInt()
                                if (new_brightness > 250) {
                                    new_brightness = 250;
                                } else if (new_brightness < 1) {
                                    new_brightness = 1;
                                }
                                val brightPerc =
                                    Math.ceil(new_brightness.toDouble() / 250.toDouble() * 100.toDouble())
                                brightness_slider_container.setVisibility(View.VISIBLE)
                                //brightness_center_text.setVisibility(View.VISIBLE)
                                brightness_slider.setProgress(brightPerc.toInt())
                                brightness = new_brightness
                                if (brightPerc < 30) {
                                    brightnessIcon.setImageResource(R.drawable.brightness_minimum);
                                    brightness_image.setImageResource(R.drawable.brightness_minimum);
                                    touchGesture = 1
                                    touchGestureValue = "low"
                                    //callBrightnessEvent("low")
                                } else if (brightPerc > 30 && brightPerc < 80) {
                                    brightnessIcon.setImageResource(R.drawable.brightness_medium);
                                    brightness_image.setImageResource(R.drawable.brightness_medium);
                                    touchGesture = 1
                                    touchGestureValue = "high"
                                    //callBrightnessEvent("high")
                                } else if (brightPerc > 80) {
                                    brightnessIcon.setImageResource(R.drawable.brightness_maximum);
                                    brightness_image.setImageResource(R.drawable.brightness_maximum);
                                    touchGesture = 1
                                    touchGestureValue = "high"
                                    //callBrightnessEvent("high")
                                }
                                brigtness_perc_center_text.text = " " + brightPerc.toInt()
                                /*Settings.System.putInt(
                                    cResolver, Settings.System.SCREEN_BRIGHTNESS,
                                    new_brightness
                                )*/
                                val layoutpars: WindowManager.LayoutParams =
                                    requireActivity().window.getAttributes()
                                layoutpars.screenBrightness = brightness / 255.toFloat()
                                requireActivity().window.setAttributes(layoutpars)
                            } else if (intRight) {
                                //vol_center_text.visibility = View.VISIBLE
                                mediavolume =
                                    audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
                                val maxVol: Int =
                                    audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!!
                                val cal =
                                    diffY.toDouble() * (maxVol.toDouble() / (device_height * 0.7).toDouble())
                                var newMediaVolume = mediavolume - cal.toInt()
                                if (newMediaVolume > maxVol) {
                                    newMediaVolume = maxVol
                                } else if (newMediaVolume < 1) {
                                    newMediaVolume = 0
                                }
                                audioManager?.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    newMediaVolume,
                                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                                )
                                lastVolumeData = newMediaVolume
                                val volPerc =
                                    Math.ceil(newMediaVolume.toDouble() / maxVol.toDouble() * 100.toDouble())
                                vol_perc_center_text.text = " " + volPerc.toInt()
                                if (volPerc < 1) {
                                    volIcon.setImageResource(R.drawable.volume_mute)
                                    vol_image.setImageResource(R.drawable.volume_mute)
                                    vol_perc_center_text.visibility = View.GONE
                                    isMute = true
                                    touchGesture = 2
                                    touchGestureValue = "Up"
                                    //callVolumeEvent("Up")
                                } else if (volPerc >= 1) {
                                    volIcon.setImageResource(R.drawable.volume)
                                    vol_image.setImageResource(R.drawable.volume)
                                    vol_perc_center_text.visibility = View.VISIBLE
                                    isMute = false
                                    touchGesture = 2
                                    touchGestureValue = "down"
                                    //callVolumeEvent("down")
                                }
                                volume_slider_container.setVisibility(View.VISIBLE)
                                volume_slider.setProgress(volPerc.toInt())
                                lastVolumeProgress = volPerc.toInt()
                            }
                        }
                    } else if (Math.abs(diffX) > Math.abs(diffY)) {
                        setLog("ActionTouch", "ACTION_MOVE 3")
                        touchGesture = 0
                        touchGestureValue = ""
                    }
                }
                return true
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                setLog("ActionTouch", "ACTION_UP 111")
                screen_swipe_move = false
                tested_ok = false
                brightness_center_text.setVisibility(View.GONE)
                vol_center_text.setVisibility(View.GONE)
                //brightness_slider_container.setVisibility(View.GONE)
                //volume_slider_container.setVisibility(View.GONE)
                //rlCenterControll.visibility = View.VISIBLE
                //rlBottomControll.visibility = View.VISIBLE
                //head.visibility = View.VISIBLE
                showControls()
                if (touchGesture == 1){
                    callBrightnessEvent(touchGestureValue)
                }else if (touchGesture == 2){
                    callVolumeEvent(touchGestureValue)
                }
                touchGesture = 0
                touchGestureValue = ""
            }
        }

        return true
    }

    private fun initSeekbarThumbnail() {
        exo_progress.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                //Thumbnail feature ------------------------
                /*thumbnailUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg"
                previewFrameLayout.visibility = View.VISIBLE
                val targetX = updatePreviewX(position.toInt(), mPlayer?.duration!!.toInt())
                previewFrameLayout.x = targetX.toFloat()
                val hms = String.format(
                    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(position),
                    TimeUnit.MILLISECONDS.toMinutes(position) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(position)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(position)
                    )
                )
                scrubbingPreviewDuration.text = hms
                Glide.with(scrubbingPreview)
                    .load(thumbnailUrl)
                    //.load(songsList.get(mPlayer!!.currentWindowIndex).url)
                    .override(SIZE_ORIGINAL,SIZE_ORIGINAL)
                    .transform(GlideThumbnailTransformation(position))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(scrubbingPreview)*/
                //----------------------------------------------------
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                //previewFrameLayout.visibility = View.INVISIBLE
                skipItroCreditShowHide(position)
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {}
        })
    }

    private fun updatePreviewX(progress: Int, max: Int): Int {
        if (max == 0) {
            return 0
        }

        val parent = previewFrameLayout.parent as ViewGroup
        val layoutParams = previewFrameLayout.layoutParams as ViewGroup.MarginLayoutParams
        val offset = progress.toFloat() / max
        val minimumX: Int = previewFrameLayout.left
        val maximumX = (parent.width - parent.paddingRight - layoutParams.rightMargin)

// We remove the padding of the scrubbing, if you have a custom size juste use dimen to calculate this
        val previewPaddingRadius: Int =
            dpToPx(resources.displayMetrics, DefaultTimeBar.DEFAULT_SCRUBBER_DRAGGED_SIZE_DP).div(2)
        val previewLeftX = (exo_progress as View).left.toFloat()
        val previewRightX = (exo_progress as View).right.toFloat()
        val previewSeekBarStartX: Float = previewLeftX + previewPaddingRadius
        val previewSeekBarEndX: Float = previewRightX - previewPaddingRadius
        val currentX = (previewSeekBarStartX + (previewSeekBarEndX - previewSeekBarStartX) * offset)
        val startX: Float = currentX - previewFrameLayout.width / 2f
        val endX: Float = startX + previewFrameLayout.width

        // Clamp the moves
        return if (startX >= minimumX && endX <= maximumX) {
            startX.toInt()
        } else if (startX < minimumX) {
            minimumX
        } else {
            maximumX - previewFrameLayout.width
        }
    }

    private fun dpToPx(displayMetrics: DisplayMetrics, dps: Int): Int {
        return (dps * displayMetrics.density).toInt()
    }

    private fun initSkipIntro() {

        if (!videoList.isNullOrEmpty() && videoList.size > playableItemPosition) {
            //Skip intro
            skipIntroST =
                videoList.get(playableItemPosition)?.data?.head?.headData?.misc?.skipIntro?.skipIntroST!!
            skipIntroST = TimeUnit.SECONDS.toMillis(skipIntroST!!)
            skipIntroET =
                videoList.get(playableItemPosition)?.data?.head?.headData?.misc?.skipIntro?.skipIntroET!!
            //skipIntroET = 10
            skipIntroET = TimeUnit.SECONDS.toMillis(skipIntroET!!)
            if (skipIntroET > skipIntroST) {
                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_credit.visibility = View.INVISIBLE
                    video_player_skip_intro.visibility = View.VISIBLE
                }!!.setLooper(Looper.getMainLooper()).setPosition(0, skipIntroST!!)
                    .setDeleteAfterDelivery(false)
                    .send()

                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_credit.visibility = View.INVISIBLE
                    video_player_skip_intro.visibility = View.INVISIBLE
                }!!.setLooper(Looper.getMainLooper()).setPosition(0, skipIntroET)
                    .setDeleteAfterDelivery(false)
                    .send()

                video_player_skip_intro.setOnClickListener {
                    mPlayer?.seekTo(skipIntroET)
                }
            }
            //
        } else {
            setLog("EmptyListVideo", "true")
        }
    }

    private fun initSkipCredit() {
        if (!videoList.isNullOrEmpty() && videoList.size > playableItemPosition) {
            //Skip credit
            skipCreditST =
                videoList.get(playableItemPosition)?.data?.head?.headData?.misc?.skipIntro?.skipCreditST!!
            //skipCreditST = 15
            skipCreditST = TimeUnit.SECONDS.toMillis(skipCreditST!!)

            skipCreditET =
                videoList.get(playableItemPosition)?.data?.head?.headData?.misc?.skipIntro?.skipCreditET!!
            //skipCreditET = 25
            skipCreditET = TimeUnit.SECONDS.toMillis(skipCreditET!!)

            if (skipCreditST > 0 && skipCreditET > skipCreditST) {
                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_intro.visibility = View.INVISIBLE
                    video_player_skip_credit.visibility = View.VISIBLE
                }!!.setLooper(Looper.getMainLooper()).setPosition(0, skipCreditST!!)
                    .setDeleteAfterDelivery(false)
                    .send()

                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_intro.visibility = View.INVISIBLE
                    video_player_skip_credit.visibility = View.INVISIBLE
                }!!.setLooper(Looper.getMainLooper()).setPosition(0, skipCreditET)
                    .setDeleteAfterDelivery(false)
                    .send()

                video_player_skip_credit.setOnClickListener {
                    mPlayer?.seekTo(skipCreditET)
                }
            }

            //
        }
    }

    private fun skipItroCreditShowHide(position: Long) {
        if (skipIntroST < position && skipIntroET > position) {
            video_player_skip_intro.visibility = View.VISIBLE
        } else {
            video_player_skip_intro.visibility = View.INVISIBLE
        }
        if (skipCreditST < position && skipCreditET > position) {
            video_player_skip_credit.visibility = View.VISIBLE
        } else {
            video_player_skip_credit.visibility = View.INVISIBLE
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.btn_pause) {
            playPause()
        } else if (id == R.id.btn_play) {
            playPause()
        } else if (id == rlShare.id) {
            isShareClick = true
            val shareurl =
                getString(R.string.music_player_str_18) + " " + videoDetailRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (id == llWatchlist.id) {
            videoDetailRespModel?.let { setAddOrRemoveFavourite(it) }
        } else if (v == img_cast_menu_dots) {
            isChomeCastClick=true
            mediaRouteButton?.performClick()
        } else if (v == exo_previous_btn) {
            if (videoDetailRespModel != null && videoDetailRespModel?.data?.head?.data?.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
                setLog(TAG, "onClick: before exo_previous_btn:${currentVideoPlaylistPosition} ")
                var prevPosition = currentVideoPlaylistPosition - 1
                if (prevPosition >= 0 && videoDetailRespModel?.data?.head?.data?.items?.get(
                        prevPosition
                    )?.itype == Constant.musicVideoNativeAds
                ) {
                    prevPosition = prevPosition - 1
                }
                if (prevPosition >= 0) {
                    changeMusicVideo(videoDetailRespModel?.data?.head?.data?.items?.get(prevPosition)?.data?.id.toString())
                    currentVideoPlaylistPosition = prevPosition
                } else {
                    changeMusicVideo(oriSelectedContentId)
                    currentVideoPlaylistPosition = -1
                }

            }else {
                playPreviousVideo()
            }
            setLog(TAG, "onClick: after exo_previous_btn:${currentVideoPlaylistPosition} musicvideoList:${musicvideoList?.size} childPosition:${childPosition}")


        } else if (v == exo_next_btn) {

            removePlayerDurationCallback()
            if (videoDetailRespModel != null && videoDetailRespModel?.data?.head?.data?.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
                setLog(TAG, "onClick: before exo_next_btn:${currentVideoPlaylistPosition} ")
                var nextPosition = currentVideoPlaylistPosition + 1
                if (nextPosition < videoDetailRespModel?.data?.head?.data?.items?.size!! && videoDetailRespModel?.data?.head?.data?.items?.get(
                        nextPosition
                    )?.itype == Constant.musicVideoNativeAds
                ) {
                    nextPosition = nextPosition + 1
                }
                if (nextPosition < videoDetailRespModel?.data?.head?.data?.items?.size!!) {
                    changeMusicVideo(videoDetailRespModel?.data?.head?.data?.items?.get(nextPosition)?.data?.id.toString())
                    currentVideoPlaylistPosition = nextPosition
                } else {
                    changeMusicVideo(oriSelectedContentId)
                    currentVideoPlaylistPosition = -1
                }

            } else if (videoDetailRespModel != null && !videoDetailRespModel?.data?.body?.rows.isNullOrEmpty() && !TextUtils.isEmpty(
                    videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id)) {
                setLog(TAG, "onClick: exo_next_btn:${videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id} ")
                playNextVideo(videoDetailRespModel?.data?.body?.rows?.get(0)?.data?.id.toString())
            }
//            else if (musicvideoList != null && musicvideoList?.size!!>1&&!ConnectionUtil(context).isOnline(false)) {
//                childPosition++
//                if(childPosition>musicvideoList?.size-1){
//                    childPosition=0
//                }
//
//                playNextVideo(musicvideoList?.get(childPosition)?.contentId.toString())
//
//                setLog(TAG, "onClick: playNextVideo exo_next_btn:${currentVideoPlaylistPosition} musicvideoList:${musicvideoList?.size} childPosition:${childPosition} isOnline:${ConnectionUtil(requireContext()).isOnline(false)}")
//            }
            setLog(TAG, "onClick: after exo_next_btn:${currentVideoPlaylistPosition} musicvideoList:${musicvideoList?.size} childPosition:${childPosition} isOnline:${ConnectionUtil(requireContext()).isOnline(false)}")
        }

    }

    private fun setAddOrRemoveFavourite(videoDetailRespModel: PlaylistDynamicModel) {
        setLog("MusicVideoDetailFragment", "setAddOrRemoveFavourite-isShowNoInternetPopUp-$isShowNoInternetPopUp")
        if (ConnectionUtil(context).isOnline(isShowNoInternetPopUp)) {

            isAddWatchlist = !isAddWatchlist
            val jsonObject = JSONObject()
            jsonObject.put("contentId", videoDetailRespModel?.data?.head?.data?.id!!)
            jsonObject.put("typeId", videoDetailRespModel?.data?.head?.data?.type!!)
            jsonObject.put("action", isAddWatchlist)
            jsonObject.put("module", Constant.MODULE_WATCHLIST)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()

            if (isAddWatchlist) {
                //if (videoDetailRespModel?.data?.head?.data?.type.toString().equals(Constant.VIDEO_MUSIC_VIDEO_TRACK, true)){
                val messageModel = MessageModel(
                    getString(R.string.toast_str_29),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(requireContext(), messageModel)
                //}

                val hashMap = java.util.HashMap<String, String>()
                var newContentId=Companion.videoDetailRespModel?.data?.head?.data?.id!!
                var contentIdData=newContentId.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY, contentIdData)
                hashMap.put(
                    EventConstant.CONTENTNAME_EPROPERTY,
                    Companion.videoDetailRespModel?.data?.head?.data?.title!!
                )
                hashMap.put(
                    EventConstant.CONTENTTYPE_EPROPERTY,
                    "" + Utils.getContentTypeName("" + Companion.videoDetailRespModel?.data?.head?.data?.type!!)
                )
                if (!Companion.videoDetailRespModel?.data?.head?.data?.misc?.genre.isNullOrEmpty()) {
                    hashMap.put(
                        EventConstant.GENRE_EPROPERTY,  Utils.arrayToString(
                            Companion.videoDetailRespModel?.data?.head?.data?.misc?.genre!!
                        )
                    )
                }

                hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "")
                if (!Companion.videoDetailRespModel?.data?.head?.data?.misc?.lang.isNullOrEmpty()) {
                    hashMap.put(
                        EventConstant.LANGUAGE_EPROPERTY,  Utils.arrayToString(
                            Companion.videoDetailRespModel?.data?.head?.data?.misc?.lang!!
                        )
                    )
                }

                hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "")
                hashMap.put(
                    EventConstant.YEAROFRELEASE_EPROPERTY, "" + DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                        DateUtils.DATE_YYYY,
                        Companion.videoDetailRespModel?.data?.head?.data?.releasedate
                    )
                )
                var eventModel: EventModel?= HungamaMusicApp?.getInstance()?.getEventData(videoDetailRespModel?.data?.head?.data?.id!!)
                if(eventModel!=null&&!TextUtils.isEmpty(eventModel?.bucketName)){
                    hashMap.put(EventConstant.SOURCE_EPROPERTY,""+eventModel?.bucketName)
                    hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,""+eventModel?.bucketName)
                }
                EventManager.getInstance().sendEvent(AddedToWatchlist(hashMap))

            } else {

                val hashMap = java.util.HashMap<String, String>()
                var newContentId= Companion.videoDetailRespModel?.data?.head?.data?.id!!
                var contentIdData=newContentId.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY,contentIdData)
                hashMap.put(
                    EventConstant.CONTENTNAME_EPROPERTY,
                    Companion.videoDetailRespModel?.data?.head?.data?.title!!
                )
                hashMap.put(
                    EventConstant.CONTENTTYPE_EPROPERTY,
                    "" + Utils.getContentTypeName("" + Companion.videoDetailRespModel?.data?.head?.data?.type!!)
                )
                if (!videoDetailRespModel?.data?.head?.data?.misc?.genre.isNullOrEmpty()) {
                    hashMap.put(
                        EventConstant.GENRE_EPROPERTY,  Utils.arrayToString(
                            Companion.videoDetailRespModel?.data?.head?.data?.misc?.genre!!
                        )
                    )
                }

                hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "")
                if (!videoDetailRespModel?.data?.head?.data?.misc?.lang.isNullOrEmpty()) {
                    hashMap.put(
                        EventConstant.LANGUAGE_EPROPERTY,  Utils.arrayToString(
                            Companion.videoDetailRespModel?.data?.head?.data?.misc?.lang!!
                        )
                    )
                }

                hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "")
                hashMap.put(
                    EventConstant.YEAROFRELEASE_EPROPERTY, "" + DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                        DateUtils.DATE_YYYY,
                        Companion.videoDetailRespModel?.data?.head?.data?.releasedate
                    )
                )
                /*var eventModel: EventModel?= HungamaMusicApp?.getInstance()?.getEventData(videoDetailRespModel?.data?.head?.data?.id!!)
                if(eventModel!=null&&!TextUtils.isEmpty(eventModel?.bucketName)){
                    hashMap.put(EventConstant.SOURCE_EPROPERTY,""+eventModel?.bucketName)
                    hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,""+eventModel?.bucketName)
                }*/
                EventManager.getInstance().sendEvent(RemovedFromWatchListEvent(hashMap))
            }

        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun hideAllControls() {
        if (controlsState === ControlsMode.FULLCONTORLS) {
            /*if (root.getVisibility() === View.VISIBLE) {
                root.setVisibility(View.GONE)
            }*/
            bottomGradient?.hide()
            hideBrightnessAndVolume()
            rlCenterControll?.visibility = View.INVISIBLE
            rlBottomControll?.visibility = View.INVISIBLE
            head?.visibility = View.INVISIBLE
        } else if (controlsState === ControlsMode.LOCK) {
            if (rlUnLock?.getVisibility() === View.VISIBLE) {
                rlUnLock?.setVisibility(View.GONE)
            }
        }
    }

    private fun showControls() {
        if (controlsState === ControlsMode.FULLCONTORLS) {
            /*if (root.getVisibility() === View.GONE) {
                root.setVisibility(View.VISIBLE)
            }*/
            bottomGradient?.show()
            rlCenterControll?.visibility = View.VISIBLE
            rlBottomControll?.visibility = View.VISIBLE
            head?.visibility = View.VISIBLE
        } else if (controlsState === ControlsMode.LOCK) {
            if (rlUnLock?.getVisibility() === View.GONE) {
                rlUnLock?.visibility = View.VISIBLE
            }
        }
        mainHandler?.removeCallbacks(hideControls!!)
        mainHandler?.postDelayed(hideControls!!, playerControlVisibilityTimeout)
    }

    private fun hideBrightnessAndVolume() {
        brightness_slider_container?.visibility = View.INVISIBLE
        volume_slider_container?.visibility = View.INVISIBLE
    }

    /*override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val action = event!!.action
        val keyCode = event.keyCode
        volume_slider.max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!!
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    audioManager?.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                    volume_slider.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (isMute){
                        isMute = false
                        volIcon.setImageResource(R.drawable.volume)
                    }
                    volume_slider_container.visibility = View.VISIBLE
                    lastVolumeData = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    lastVolumeProgress = volume_slider.progress
                    *//*setLog("Volume++",
                        audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toString()
                    )*//*

                    mainHandler!!.removeCallbacks(hideVolumeBar!!)
                    mainHandler!!.postDelayed(hideVolumeBar!!, playerControlVisibilityTimeout)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    audioManager?.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                    volume_slider.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    volume_slider_container.visibility = View.VISIBLE
                    lastVolumeData = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    lastVolumeProgress = volume_slider.progress
                    *//*setLog("Volume--",
                        audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toString()
                    )*//*
                    mainHandler!!.removeCallbacks(hideVolumeBar!!)
                    mainHandler!!.postDelayed(hideVolumeBar!!, playerControlVisibilityTimeout)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }*/


    private fun downloadIconStates(status: Int, ivAudioDownload: ImageView) {
        when (status) {
            Status.NONE.value -> {
                /*val drawable = FontDrawable(requireContext(), R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)*/
                ivAudioDownload.setImageDrawable(
                    requireContext().faDrawable(R.string.icon_download, R.color.colorWhite)
                )
            }
            Status.QUEUED.value -> {
                /*val drawable = FontDrawable(requireContext(), R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)*/
                ivAudioDownload.setImageDrawable(
                    requireContext().faDrawable(R.string.icon_download_queue, R.color.colorWhite)
                )
            }
            Status.DOWNLOADING.value -> {
                /*val drawable = FontDrawable(requireContext(), R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)*/
                ivAudioDownload.setImageDrawable(
                    requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite)
                )
            }
            Status.COMPLETED.value -> {
                /*val drawable = FontDrawable(requireContext(), R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)*/
                ivAudioDownload.setImageDrawable(
                    requireContext().faDrawable(R.string.icon_downloaded2, R.color.colorWhite)
                )
            }
            Status.PAUSED.value -> {
                ivAudioDownload.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_pause_round
                    )
                )
            }
        }
    }

    fun isPlayOffline():Boolean{
        val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
            ?.findByContentId(selectedContentId.toString())
        if (downloadedAudio != null && CommonUtils.isUserHasGoldSubscription()) {
            return true
        }
        return false
    }

    private fun initDownloadMusic() {
        if (ivDownload != null) {
            ivDownload?.requestFocus()
            if (!TextUtils.isEmpty(selectedContentId)) {
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                    ?.findByContentId(selectedContentId.toString())
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                    ?.findByContentId(selectedContentId.toString())
                if (downloadedAudio != null && CommonUtils.isUserHasGoldSubscription()) {

                   CoroutineScope(Dispatchers.IO).launch {
                       CommonUtils.setDownloadEventModelDataAppLevel(selectedContentId)
                   }

                    var isDownloadedFromExoplayer = true
                    if (!TextUtils.isEmpty(downloadedAudio.downloadedFilePath)){
                        isDownloadedFromExoplayer = false
                    }
                    setLog(TAG, "initDownloadMusic -> setUpVideoListViewMode: downloadedAudio${downloadedAudio}")
                    val isDownloaded = downloadTracker?.isDownloaded(downloadedAudio.downloadUrl!!)
                    if (!isDownloaded!! && isDownloadedFromExoplayer) {
                        setUpVideoListViewModel(selectedContentId)
                        AppDatabase.getInstance()?.downloadedAudio()
                            ?.deleteDownloadQueueItemByContentId(selectedContentId.toString())
                    } else {


                        val vlm = PlayableContentModel()
                        vlm.data?.head?.headData?.id = downloadedAudio.contentId!!
                        vlm.data?.head?.headData?.title = downloadedAudio.title!!
                        vlm.data?.head?.headData?.image = downloadedAudio.image!!
                        vlm.data?.head?.headData?.type = downloadedAudio.type!!
                        if (isDownloadedFromExoplayer){
                            vlm.data?.head?.headData?.misc?.url = downloadedAudio.downloadUrl.toString()
                        }else{
                            val newFileName = Data.getSaveVideoDir(context)+"temp.cache"
                            CommonUtils.decryptAudioContent(
                                downloadedAudio.downloadedFilePath.toString(),
                                newFileName
                            )
                            Thread.sleep(1000)
                            vlm.data?.head?.headData?.misc?.url = newFileName
                        }
                        vlm.data?.head?.headData?.misc?.downloadLink?.drm?.url = downloadedAudio.downloadUrl!!
                        vlm.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                            downloadedAudio.drmLicense
                        vlm.data?.head?.headData?.misc?.movierights =
                            CommonUtils.getStringToArray(downloadedAudio.movierights!!)
                        //vlm.drmlicence = downloadedAudio.
                        vlm.data?.head?.headData?.misc?.skipIntro =
                            PlayableContentModel.Data.Head.HeadData.Misc.SkipIntro(
                                downloadedAudio.skipCreditET?.toLong()!!,
                                downloadedAudio.skipCreditST?.toLong()!!,
                                downloadedAudio.skipIntroET?.toLong()!!,
                                downloadedAudio.skipIntroST?.toLong()!!
                            )
                        vlm.data?.head?.headData?.misc?.explicit = downloadedAudio.explicit!!
                        vlm.data?.head?.headData?.misc?.attributeCensorRating =
                            CommonUtils.getStringToArray(downloadedAudio.attribute_censor_rating!!)
                        videoList.add(vlm)
                        startPlayerService(videoList)
                        downloadState = Status.COMPLETED.value
                        shimmerLayout.visibility = View.GONE
                        shimmerLayout.stopShimmer()
                        videoDetailroot?.visibility = View.VISIBLE

                        setLog(TAG, "initDownloadMusic startPlayerService -> setUpVideoList size:${videoList?.size}")
                    }
                } else if (downloadQueue != null) {
                    setLog(TAG, "initDownloadMusic downloadQueue -> setUpVideoListViewMode")
                    setUpVideoListViewModel(selectedContentId)
                    if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.QUEUED.value) {
                        downloadState = Status.QUEUED.value
                    } else if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.DOWNLOADING.value) {
                        downloadState = Status.DOWNLOADING.value
                    } else if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.PAUSED.value) {
                        downloadState = Status.PAUSED.value
                    }
                } else {
                    setLog(TAG, "initDownloadMusic downloadedAudio -> setUpVideoListViewMode")
                    downloadState = Status.NONE.value
                    setUpVideoListViewModel(selectedContentId)
                }
            } else {
                return
            }

            //isDownloaded = downloadTracker!!.isDownloaded(currentPlayingMediaItem)

            if (downloadState == Status.COMPLETED.value) {
                /*ivDownload?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_download_completed
                    )
                )*/
                downloadIconStates(Status.COMPLETED.value, ivDownload)
                tvDownload?.text = getString(R.string.video_player_str_2)

            } else if (downloadState == Status.QUEUED.value) {
                downloadIconStates(Status.QUEUED.value, ivDownload)
                tvDownload?.text = getString(R.string.download_str_11)

            } else if (downloadState == Status.DOWNLOADING.value) {
                downloadIconStates(Status.DOWNLOADING.value, ivDownload)
                tvDownload?.text = getString(R.string.download_str_2)

            } else if (downloadState == Status.PAUSED.value) {
                downloadIconStates(Status.PAUSED.value, ivDownload)
                tvDownload?.text = getString(R.string.general_setting_str_25)

            } else {
                downloadIconStates(Status.NONE.value, ivDownload)
                tvDownload?.text = getString(R.string.movie_str_3)
                /*ivDownload?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_download
                    )
                )*/
                rlDownload?.setOnClickListener {
                    if (videoDetailRespModel != null) {
                        setLog("AfterClicked","ShortVideo")
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = videoDetailRespModel?.data?.head?.data?.id?.toString()!!
                        dpm.contentTitle =
                            videoDetailRespModel?.data?.head?.data?.title?.toString()!!
                        dpm.planName =
                            videoDetailRespModel?.data?.head?.data?.misc?.movierights.toString()
                        dpm.isAudio = false
                        dpm.isDownloadAction = true
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload =
                            RestrictedDownload.valueOf(videoDetailRespModel?.data?.head?.data?.misc?.restricted_download!!)
                        if (CommonUtils.userCanDownloadContent(
                                requireContext(),
                                musicVideoDetailroot,
                                dpm,
                                this,Constant.drawer_svod_purchase
                            )
                        ) {
                            // self download
                            /*startDRMDownloadSong()
                            downloadIconStates(Download.STATE_QUEUED, ivDownload)
                            tvDownload?.text = getString(R.string.in_queue)*/
                            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                            var dq = DownloadQueue()
                            //for (item in playlistSongList?.iterator()!!){

                            dq = DownloadQueue()
                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.id!!)) {
                                dq.parentId = videoDetailRespModel?.data?.head?.data?.id!!
                                dq.contentId = videoDetailRespModel?.data?.head?.data?.id!!
                            }
                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.title!!)) {
                                dq.pName = videoDetailRespModel?.data?.head?.data?.title
                                dq.title = videoDetailRespModel?.data?.head?.data?.title
                            }

                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.subtitle!!)) {
                                dq.pSubName = videoDetailRespModel?.data?.head?.data?.subtitle
                                dq.subTitle = videoDetailRespModel?.data?.head?.data?.subtitle
                            }

                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.releasedate!!)) {
                                dq.pReleaseDate =
                                    videoDetailRespModel?.data?.head?.data?.releasedate
                            }
                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.releasedate!!)) {
                                dq.pReleaseDate = videoDetailRespModel?.data?.head?.data?.releasedate
                            }

                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.image!!)) {
                                dq.pImage = videoDetailRespModel?.data?.head?.data?.image
                                dq.image = videoDetailRespModel?.data?.head?.data?.image
                            }
                            setLog("MyDarar","kkkkk${ videoDetailRespModel?.data?.head?.data?.misc?.f_playcount.toString()}")
                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.misc?.f_playcount.toString())) {
                                //dq.f_playcount = videoDetailRespModel?.data?.head?.data?.misc?.f_playcount
                                dq.f_playcount = videoDetailRespModel?.data?.head?.data?.misc?.f_playcount.toString()
                            }

                            if (!TextUtils.isEmpty(videoDetailRespModel?.data?.head?.data?.misc?.movierights.toString())) {
                                dq.planName =
                                    videoDetailRespModel?.data?.head?.data?.misc?.movierights.toString()
                                dq.planType = CommonUtils.getContentPlanType(dq.planName)
                            }

                            dq.pType = DetailPages.MUSIC_VIDEO_DETAIL_PAGE.value
                          setLog("PlayertYPE","TYPE${playerType}")
                            if (playerType != null && playerType.equals(
                                    Constant.VIDEO_EVENTS_BROADCAST_VIDEO,
                                    true
                                )
                            ) {
                                dq.contentType = ContentTypes.SHORT_VIDEO.value
                            } else {
                                dq.contentType = ContentTypes.VIDEO.value
                            }
                            val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                            dq.source = eventModel.sourceName

                            val downloadQueue =
                                AppDatabase.getInstance()?.downloadQueue()?.findByContentId(
                                    videoDetailRespModel?.data?.head?.data?.id.toString()
                                )
                            val downloadedAudio =
                                AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                                    videoDetailRespModel?.data?.head?.data?.id.toString()
                                )
                            if ((!downloadQueue?.contentId.equals(videoDetailRespModel?.data?.head?.data?.id!!.toString()))
                                && (!downloadedAudio?.contentId.equals(videoDetailRespModel?.data?.head?.data?.id!!.toString()))
                            ) {
                                downloadQueueList.add(dq)
                            }
                            downloadIconStates(Status.QUEUED.value, ivDownload)
                            tvDownload?.text = getString(R.string.download_str_11)
                            (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                                downloadQueueList,
                                this,
                                false,
                                true
                            )
                        }
                    }
                }
            }

        }
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
                if (downloadQueue != null && videoDetailRespModel?.data?.head?.data?.id?.equals(
                        downloadQueue.contentId
                    )!!
                ) {

                } else if (downloadedAudio != null && videoDetailRespModel?.data?.head?.data?.id?.equals(
                        downloadedAudio.contentId
                    )!!
                ) {

                } else {
                    return@launch
                }
                when (download.state) {
                    Download.STATE_DOWNLOADING -> {
                        setLog("VideoDownloadLog:1", download.state.toString())
                        //Toast.makeText(requireContext(), "Download started.", Toast.LENGTH_LONG).show()
                        downloadIconStates(Status.DOWNLOADING.value, ivDownload)
                        tvDownload?.text = getString(R.string.download_str_2)
                    }
                    Download.STATE_QUEUED -> {
                        setLog("VideoDownloadLog:2", download.state.toString())
                        //Toast.makeText(requireContext(), "Download queue.", Toast.LENGTH_LONG).show()
                        downloadIconStates(Status.QUEUED.value, ivDownload)
                        tvDownload?.text = getString(R.string.download_str_11)
                    }
                    Download.STATE_STOPPED -> {
                        setLog("VideoDownloadLog:3", download.state.toString())
                        //Toast.makeText(requireContext(), "Download stopped.", Toast.LENGTH_LONG).show()
                        if(download.stopReason == stopReasonPause){
                            downloadIconStates(Status.PAUSED.value, ivDownload)
                            tvDownload?.text = getString(R.string.movie_str_3)
                        }
                    }
                    Download.STATE_COMPLETED -> {
                        setLog("VideoDownloadLog:4", download.state.toString())
                        //Toast.makeText(requireContext(), "Download completed.", Toast.LENGTH_LONG).show()
                        rlDownload?.setOnClickListener(null)
                        downloadIconStates(Status.COMPLETED.value, ivDownload)
                        tvDownload?.text = getString(R.string.video_player_str_2)
                    }
                    Download.STATE_REMOVING -> {
                        setLog("VideoDownloadLog:5", download.state.toString())
                        //Toast.makeText(requireContext(), "Download removed.", Toast.LENGTH_LONG).show()
                        downloadIconStates(Status.NONE.value, ivDownload)
                        tvDownload?.text = getString(R.string.movie_str_3)
                    }
                    Download.STATE_FAILED -> {
                        setLog("VideoDownloadLog:6", download.state.toString())
                        //Toast.makeText(requireContext(), "Download failed.", Toast.LENGTH_LONG).show()
                        downloadIconStates(Status.NONE.value, ivDownload)
                        tvDownload?.text = getString(R.string.movie_str_3)
                    }
                    Download.STATE_RESTARTING -> {
                        setLog("VideoDownloadLog:7", download.state.toString())
                    }
                }
            } catch (e: Exception) {
e.printStackTrace()
            }
        }


    }

    override fun onDownloadProgress(
        downloads: List<Download?>?,
        progress: Int,
        currentExoDownloadPosition: Int
    ) {

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {

    }

    override fun onAudioSubTitleItemClick(
        model: PlayableContentModel?,
        subtitleItem: PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?
    ) {
        setLog("TAG", "onSubTitleItemClick:" + model)
        mdeiaArrayList = ArrayList()
        var mimType = MimeTypes.BASE_TYPE_AUDIO
        if (model?.data?.head?.headData?.misc?.url?.contains(".m3u8", true)!!) {
            mimType = MimeTypes.APPLICATION_M3U8
        } else if (model?.data?.head?.headData?.misc?.url?.contains(".mp3", true)!!) {
            mimType = MimeTypes.BASE_TYPE_AUDIO
        } else if (model?.data?.head?.headData?.misc?.url?.contains(".mpd", true)!!) {
            mimType = MimeTypes.APPLICATION_MPD
        } else if (model?.data?.head?.headData?.misc?.url?.contains(".mp4", true)!!) {
            mimType = MimeTypes.APPLICATION_MP4
        }

        val subtitles = ArrayList<MediaItem.Subtitle>()
        var subTitleMenuItem: MediaItem.Subtitle? = null
        subTitleMenuItem = MediaItem.Subtitle(
            Uri.parse(subtitleItem?.link),
            MimeTypes.TEXT_VTT!!,
            "en",
            C.SELECTION_FLAG_DEFAULT
        )
        subtitles.add(subTitleMenuItem)

        if (!TextUtils.isEmpty(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
            val mediaItem1 = MediaItem.Builder()
                .setDrmUuid(drmSchemeUuid)
                .setDrmLicenseUri(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(model?.data?.head?.headData?.title)
                        .setDisplayTitle(model?.data?.head?.headData?.title)
                        .setSubtitle(model?.data?.head?.headData?.subtitle)
                        .setArtworkUri(Uri.parse(model?.data?.head?.headData?.image!!))
                        //.setMediaUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                        .setDescription(model?.data?.head?.headData?.misc?.description)
                        .build()
                )
                .setMimeType(mimType)
                .build()
            mdeiaArrayList.add(mediaItem1)
            setLog("TAG", "music video setMediaItem: DRM content play url:${model?.data?.head?.headData?.misc?.url}")
        } else {
            val mediaItem1 = MediaItem.Builder()
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(model?.data?.head?.headData?.title)
                        .setDisplayTitle(model?.data?.head?.headData?.title)
                        .setSubtitle(model?.data?.head?.headData?.subtitle)
                        .setArtworkUri(Uri.parse(model?.data?.head?.headData?.image!!))
                        //.setMediaUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                        .setDescription(model?.data?.head?.headData?.misc?.description)
                        .build()
                )
                .setMimeType(mimType)
                .build()
            mdeiaArrayList.add(mediaItem1)

            setLog("TAG", "setMediaItem: Non DRM content play url:${model?.data?.head?.headData?.misc?.url}")
        }


        if (mdeiaArrayList.size > 0 && mPlayer != null) {

            val lastPosition = mPlayer?.currentPosition
            mPlayer?.setMediaItems(mdeiaArrayList)
            mPlayer?.prepare()

            if (lastPosition != null && lastPosition > 0) {
                mPlayer?.seekTo(lastPosition)
            }

        }

        if (subTitleSheetFragment != null) {
            subTitleSheetFragment?.dismiss()
        }
    }
    var mdeiaArrayList: MutableList<MediaItem> = ArrayList()
    override fun onSubTitleItemClick(
        model: PlayableContentModel?,
        subtitleItem: PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?
    ) {
        setLog("TAG", "onSubTitleItemClick:" + model)
        mdeiaArrayList = ArrayList()
        var mimType = MimeTypes.BASE_TYPE_AUDIO
        if (model?.data?.head?.headData?.misc?.url?.contains(".m3u8", true)!!) {
            mimType = MimeTypes.APPLICATION_M3U8
        } else if (model?.data?.head?.headData?.misc?.url?.contains(".mp3", true)!!) {
            mimType = MimeTypes.BASE_TYPE_AUDIO
        } else if (model?.data?.head?.headData?.misc?.url?.contains(".mpd", true)!!) {
            mimType = MimeTypes.APPLICATION_MPD
        } else if (model?.data?.head?.headData?.misc?.url?.contains(".mp4", true)!!) {
            mimType = MimeTypes.APPLICATION_MP4
        }

        val subtitles = ArrayList<MediaItem.Subtitle>()
        var subTitleMenuItem: MediaItem.Subtitle? = null
        subTitleMenuItem = MediaItem.Subtitle(
            Uri.parse(subtitleItem?.link),
            MimeTypes.TEXT_VTT!!,
            "en",
            C.SELECTION_FLAG_DEFAULT
        )
        subtitles.add(subTitleMenuItem)

        if (!TextUtils.isEmpty(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
            val mediaItem1 = MediaItem.Builder()
                .setDrmUuid(drmSchemeUuid)
                .setDrmLicenseUri(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(model?.data?.head?.headData?.title)
                        .setDisplayTitle(model?.data?.head?.headData?.title)
                        .setSubtitle(model?.data?.head?.headData?.subtitle)
                        .setArtworkUri(Uri.parse(model?.data?.head?.headData?.image!!))
                        //.setMediaUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                        .setDescription(model?.data?.head?.headData?.misc?.description)
                        .build()
                )
                .setMimeType(mimType)
                .build()
            mdeiaArrayList.add(mediaItem1)
        } else {
            val mediaItem1 = MediaItem.Builder()
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(model?.data?.head?.headData?.title)
                        .setDisplayTitle(model?.data?.head?.headData?.title)
                        .setSubtitle(model?.data?.head?.headData?.subtitle)
                        .setArtworkUri(Uri.parse(model?.data?.head?.headData?.image!!))
                        //.setMediaUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                        .setDescription(model?.data?.head?.headData?.misc?.description)
                        .build()
                )
                .setMimeType(mimType)
                .build()
            mdeiaArrayList.add(mediaItem1)
        }

        if (mdeiaArrayList.size > 0 && mPlayer != null) {

            val lastPosition = mPlayer?.currentPosition

            mPlayer?.setMediaItems(mdeiaArrayList)
            mPlayer?.prepare()

            if (lastPosition != null && lastPosition > 0) {
                mPlayer?.seekTo(lastPosition)
            }

        }

        if (subTitleSheetFragment != null) {
            subTitleSheetFragment?.dismiss()
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        if (status == Constant.LOGIN_PROCESS || status == Constant.PAYMENT_PROCESS){
            pausePlayer()
        }else{
            playPause()
        }
    }

    private fun registerBroadcastReceiver() {
        val theFilter = IntentFilter()
        /** System Defined Broadcast  */
        theFilter.addAction(Intent.ACTION_SCREEN_ON)
        theFilter.addAction(Intent.ACTION_SCREEN_OFF)
        theFilter.addAction(Intent.ACTION_USER_PRESENT)
        val screenOnOffReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val strAction = intent.action
                val myKM: KeyguardManager =
                    context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
                if (strAction == Intent.ACTION_USER_PRESENT || strAction == Intent.ACTION_SCREEN_OFF || strAction == Intent.ACTION_SCREEN_ON) if (myKM.inKeyguardRestrictedInputMode()) {
                    println("Screen off " + "LOCKED")
                    if (mPlayer != null) {
                        mPlayer?.pause()
                    }
                } else {
                    println("Screen off " + "UNLOCKED")
                    if (mPlayer != null) {
                        mPlayer?.play()
                    }
                }
            }
        }
        requireActivity().registerReceiver(screenOnOffReceiver, theFilter)
    }

    private fun showMiniVideoPlayer(intent: Intent) {
        setLog("showMiniVideoPlayer", "showMiniVideoPlayer called")
        if (intent?.hasExtra(Constant.VIDEO_START_POSITION) == true) {
            val video_start_position = intent.getLongExtra(Constant.VIDEO_START_POSITION, 0)
            val videoListModel: ArrayList<PlayableContentModel> =
                intent.getParcelableArrayListExtra(
                    Constant.VIDEO_LIST_DATA
                )!!
            val selectedTrackPosition = intent.getIntExtra(Constant.SELECTED_TRACK_POSITION, 0)
            playAllVideo(videoListModel.get(selectedTrackPosition), video_start_position)
            setLog("VIDEO_START_POSITION-1", video_start_position.toString())
        }
    }

    private fun playAllVideo(model: PlayableContentModel, trackPlayStartPosition: Long) {
        val videoUrl = model.data?.head?.headData?.misc?.url
        val videoDrmLicense = model.data.head.headData.misc.downloadLink.drm.token
        val videoTitle = model.data.head.headData.title
        val videoSubTitle = model.data.head.headData.subtitle
        val videoArtwork = model.data.head.headData.image
        videoUrl?.let {
            videoTitle?.let { it1 ->
                playerType?.let { it2 ->
                    CommonUtils.setVideoTrackList(
                        requireContext(),
                        selectedContentId!!,
                        it,
                        it1,
                        it2,
                        videoSubTitle,
                        videoArtwork,
                        videoDrmLicense,
                        ContentTypes.VIDEO.value
                    )
                    tracksViewModel.prepareTrackPlayback(BaseActivity.nowPlayingCurrentIndex()+1, trackPlayStartPosition)
                }
            }
        }

    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        try {
            if (isAdded && context != null){
                setLog("startTrackPlayback", "musicVideo-startTrackPlayback-if")
                val intent = Intent(requireContext(), AudioPlayerService::class.java)
                intent.action = AudioPlayerService.PlaybackControls.PLAY.name
                intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
                intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
                intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
                intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
                Util.startForegroundService(requireContext(), intent)
            }else{
                setLog("startTrackPlayback", "musicVideo-startTrackPlayback-else")
            }
        }catch (e:Exception){
            setLog("startTrackPlayback", "musicVideo-startTrackPlayback-error-${e.message}")
        }

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun onBackPressedCall() {
        try {
            if (!videoList.isNullOrEmpty() && videoList.size > playableItemPosition){
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                /*As per https://hungama.atlassian.net/browse/HU-5703 -
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)*/
                if (isScreenLandscape) {
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                }else{
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_PORTRAIT_PLAYER)
                }
                setLog("VideoPlayerBackTappedEvent", "MusicVideoDetailFragment-VideoPlayerBackTappedEvent-$hashMap")
                EventManager.getInstance().sendEvent(VideoPlayerBackTappedEvent(hashMap))
            }
        }catch (e:Exception){

        }
        try {
            if (mPlayer != null && !videoList.isNullOrEmpty()) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    && requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                ) {
                    if (isScreenLandscape) {
                        handleFullScreenEnterExit()
                    } else {
                        if (videoListModel != null && videoListModel?.data != null && videoListModel?.data?.head != null && videoListModel?.data?.head?.headData != null && !TextUtils.isEmpty(videoListModel?.data?.head?.headData?.id)){
                            setLog(TAG, "onBackPressedCall: ID:${videoListModel?.data?.head?.headData?.id!!} duration:${TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!)}")
                            HungamaMusicApp.getInstance().userStreamList.put(videoListModel?.data?.head?.headData?.id!!,TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!))
                        }
                        if (mPlayer?.isPlaying == true && MainActivity.lastItemClicked != Constant.BOTTOM_NAV_PLAYER){
                            val intent = Intent(Constant.MUSIC_VIDEO_PLAYER_EVENT)
                            intent.putExtra(Constant.VIDEO_START_POSITION, mPlayer?.currentPosition)
                            intent.putParcelableArrayListExtra(Constant.VIDEO_LIST_DATA, videoList)
                            intent.putParcelableArrayListExtra(Constant.SEASON_LIST, seasonList)
                            intent.putExtra(Constant.SELECTED_TRACK_POSITION, playableItemPosition)
                            //intent.putExtra("EVENT", Constant.MUSIC_VIDEO_RESULT_CODE)
                            showMiniVideoPlayer(intent)
                        }

                        stopPlayerBGService()
                    }
                }
            } else {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    && requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                ) {
                    if (isScreenLandscape) {
                        handleFullScreenEnterExit()
                    } else {
                        stopPlayerBGService()
                    }
                }
            }
        }catch (e:Exception){

        }
    }

    private fun startupApiCalls() {
        if (isPlayOffline() && !ConnectionUtil(requireContext()).isOnline(false)){
            isShowNoInternetPopUp = false
        }else{
            isShowNoInternetPopUp = true
        }

        setLog("MusicVideoDetailFragment", "startupApiCalls-false-isShowNoInternetPopUp-${isShowNoInternetPopUp} videoDetailRespModel:${videoDetailRespModel}")

        isAddWatchlist = false

        if (videoDetailRespModel != null && videoDetailRespModel?.data?.head?.data?.type == Constant.VIDEO_MUSIC_VIDEO_PLAYLIST_INT) {
            initDownloadMusic()
        } else {
            setFollowingStatus()
            setupUserViewModel()
            setupVideoDetailViewModel()
            initDownloadMusic()
        }


    }

    private fun changeMusicVideo(contentId: String) {
        if (!TextUtils.isEmpty(contentId)) {
            stopPlayerBGService()
            selectedContentId = contentId
            startupApiCalls()
            (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                ArrayList(),
                this,
                true,
                false
            )
        }
    }

    private fun playNextVideo(contentId: String) {
        setLog("MusicVideo", "MusicVideoDetailFragment-playNextVideo-contentId-$contentId")
        setLog("MusicVideo", "MusicVideoDetailFragment-playNextVideo-playableItemPosition1-$playableItemPosition")
        playableItemPosition += 1
        setLog("MusicVideo", "MusicVideoDetailFragment-playNextVideo-playableItemPosition2-$playableItemPosition")
        changeMusicVideo(contentId)
    }

    private fun playPreviousVideo() {
        if (playableItemPosition > 0) {
            playableItemPosition -= 1
            if (!videoList.isNullOrEmpty() && videoList.size > playableItemPosition
                && !TextUtils.isEmpty(videoList?.get(playableItemPosition)?.data?.head?.headData?.id)
            ) {
                changeMusicVideo(videoList.get(playableItemPosition).data.head.headData.id)
            } else {
                //exo_previous_btn?.isEnabled = false
            }
            //exo_previous_btn?.isEnabled = playableItemPosition != 0
        } else {
            playableItemPosition = 0
            //exo_previous_btn?.isEnabled = false
        }
    }

    private fun setLocalBroadcast() {
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
            this,
            Constant.MUSIC_VIDEO_PLAYER_EVENT
        )
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
            this,
            Constant.AUDIO_PLAYER_EVENT
        )
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            val key = intent.getIntExtra("key", -1)
            setLog("MusicVideoDetailFragment", "onLocalBroadcastEventCallBack-event-$event-key-$key")
            if (event == Constant.MUSIC_VIDEO_RESULT_CODE && key == KeyEvent.KEYCODE_HOME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                    && isPIPModeeEnabled && mPlayer != null && mPlayer?.isPlaying!!
                ) {
                    videoStartPosition = mPlayer?.currentPosition!!
                    enterPIPMode()

                }
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(
                    scrollView,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
            }
        }
    }

    private fun getAdsData(playlistSongList: ArrayList<PlaylistModel.Data.Body.Row>): ArrayList<PlaylistModel.Data.Body.Row> {
        if (!playlistSongList.isNullOrEmpty() && CommonUtils.isDisplayAds() && CommonUtils.getFirebaseConfigAdsData().videoPlayerPortraitNativeAd.displayAd) {
            val adDisplayFirstPosition =
                CommonUtils.getFirebaseConfigAdsData().videoPlayerPortraitNativeAd.firstAdPositionAfterRows
            val adDisplayPositionFrequency =
                CommonUtils.getFirebaseConfigAdsData().videoPlayerPortraitNativeAd.repeatFrequencyAfterRows
            var adDisplayPosition = adDisplayFirstPosition
            //val adDisplayPosition = 4
            var isFirstAds = true
            val adUnitIdList = arrayListOf(
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_1,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_2,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_3,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_4,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_5
            )
            val adTotalIds = adUnitIdList.size
            var adIdCount = 0
            var i = 0
            var k = 0
            val iterator = playlistSongList.listIterator()
            while (iterator.hasNext()) {
                //setLog("adInserted-1", i.toString())

                if (k > 0 && k % adDisplayPosition == 0) {
                    if (isFirstAds) {
                        k = 0
                        isFirstAds = false
                        adDisplayPosition = adDisplayPositionFrequency
                    }
                    //setLog("adInserted-2", i.toString())
                    //setLog("adInserted", "Befor==" + homeModel.data?.body?.rows?.get(i)?.heading)

                    val videoRow = PlaylistModel.Data.Body.Row()
                    videoRow.itype = Constant.musicVideoNativeAds

                    if (adTotalIds > adIdCount) {
                        //setLog("adInserted-3", adIdCount.toString())
                        //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                        videoRow.adUnitId = adUnitIdList.get(adIdCount)
                        adIdCount++
                    } else {
                        adIdCount = 0
                        videoRow.adUnitId = adUnitIdList.get(adIdCount)
                        //setLog("adInserted-4", adIdCount.toString())
                        //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                        adIdCount++
                    }

                    iterator.add(videoRow)
                }
                val item = iterator.next()
                i++
                k++
            }
        }
        return playlistSongList
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        val dataMap= java.util.HashMap<String, String>()
        dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
        dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ videoDetailRespModel?.data?.head?.data?.title)

        dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
        EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
    }
    private fun redirectToMoreBucketListPage(bodyRowsItemsItem: ArrayList<BodyRowsItemsItem?>?, heading: String) {
        val bundle = Bundle()
        val selectedMoreBucket = RowsItem()
        selectedMoreBucket.heading = heading
        selectedMoreBucket.items = bodyRowsItemsItem
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)
    }

    override fun onSimilarVideoClick(contentData: BodyRowsItemsItem?) {
        if (contentData != null){
            setLog("MusicVideo", "MusicVideoDetailFragment-onSimilarVideoClick-ContentId-"+contentData.data?.id.toString())
            playableItemPosition = videoList.size
            setLog("MusicVideo", "MusicVideoDetailFragment-onSimilarVideoClick-playableItemPosition2-$playableItemPosition")
            changeMusicVideo(contentData.data?.id.toString())
        }else{
            setLog("MusicVideo", "MusicVideoDetailFragment-onSimilarVideoClick()-contentData-$contentData")
        }
    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        /*if (parent != null && !parent.keywords.isNullOrEmpty() && parent.keywords?.get(0).equals("similar-videos")){
            if (!parent.items.isNullOrEmpty() && parent.items?.size!! > childPosition && parent.items?.get(childPosition)?.data != null){
                playNextVideo(parent.items?.get(childPosition)?.data?.id.toString())
            }
        }*/
        if (parent != null
            && !parent.items.isNullOrEmpty()
            && parent.items?.size!! > childPosition
            && parent.items?.get(childPosition)?.data != null
            && parent.items?.get(childPosition)?.data?.type !=null
            && (parent.items?.get(childPosition)?.data?.type.equals("22", true)
                    || parent.items?.get(childPosition)?.data?.type.equals("53", true)
                    || parent.items?.get(childPosition)?.data?.type.equals("88888", true))){
            playNextVideo(parent.items?.get(childPosition)?.data?.id.toString())
        }else{
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
        }
    }

    private fun callStreamEventAnalytics(
        track: PlayableContentModel
    ) {
        try {
            val eventModel =
                HungamaMusicApp.getInstance().getEventData("" + track?.data?.head?.headData?.id)
            var totalDurationPlayed = 0L
            if (mPlayer != null && mPlayer?.currentPosition != null && mPlayer?.currentPosition!! > 0){
                totalDurationPlayed = TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!)
            }
            setLog("AllData","Print${eventModel.toString()}")
            val hashMap = java.util.HashMap<String, String>()
            hashMap.put(EventConstant.DURATION_EPROPERTY, "" + totalDurationPlayed)
            hashMap.put(EventConstant.DURATION_BG_EPROPERTY, "")
            hashMap.put(EventConstant.DURATION_FG_EPROPERTY, "" + totalDurationPlayed)
            var totalTime=0L
            totalTime = mPlayer?.duration!! //length
            var length = TimeUnit.MILLISECONDS.toSeconds(totalTime)
            if(!TextUtils.isEmpty(eventModel.duration!!) && !eventModel.duration.equals("0")){
                length=eventModel.duration.toLong()
            }
            if (length > 0){
                hashMap.put(EventConstant.LENGTH_EPROPERTY, "" + length)
            }else{
                hashMap.put(EventConstant.LENGTH_EPROPERTY, "0")
            }

            if (activity != null){
                hashMap.put(
                    EventConstant.CONNECTION_TYPE_EPROPERTY,
                    ConnectionUtil(requireActivity()!!).networkType
                )
                if (ConnectionUtil(requireActivity()).isOnline(false)){
                    hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + eventModel?.consumptionType)
                }else{
                    hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + EventConstant.CONSUMPTIONTYPE_OFFLINE)
                }
            }
            baseIOScope.launch {
                try {
                    hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
//        hashMap.put(EventConstant.ALBUM_ID_EPROPERTY, "" + eventModel?.album_ID)
//            hashMap.put(EventConstant.APPBOY_PUSH_RECEIVED_TIMESTAMP_EPROPERTY,"")
                    hashMap.put(EventConstant.AUDIO_QUALITY_EPROPERTY, "" + eventModel?.audioQuality)
                    hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + eventModel?.bucketName)
                    var newContentId= track?.data?.head?.headData?.id
                    var contentIdData=newContentId?.replace("playlist-","")
                    hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" +contentIdData)
                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeNameForStream(""+track?.data?.head?.headData?.type))
                    hashMap.put(EventConstant.DEVICE_MODEL_EPROPERTY, "" + Utils.getDeviceName())

                    hashMap.put(EventConstant.GENRE_EPROPERTY, "" + eventModel?.genre)
                    hashMap.put(EventConstant.LABEL_EPROPERTY, "" + eventModel?.label)
                    hashMap.put(EventConstant.LABEL_ID_EPROPERTY, "" + eventModel?.label_id)
                    hashMap.put(EventConstant.LANGUAGE_EPROPERTY, "" + eventModel?.language)



                    hashMap.put(EventConstant.LYRICIST_EPROPERTY, "" + eventModel?.lyricist)
                    hashMap.put(EventConstant.MOOD_EPROPERTY, "" + eventModel?.mood)
                    hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY, "" + eventModel?.musicDirectorComposer)
//        hashMap.put(EventConstant.NAME_EPROPERTY, "" + eventModel.songName)
//        hashMap.put(EventConstant.NID_EPROPERTY, "")
                    hashMap.put(EventConstant.ORIGINAL_ALBUM_NAME_EPROPERTY, "" + eventModel?.originalAlbumName)

                    var complitionPer=0.0
                    try {
                        if(totalDurationPlayed>0 && length>0){
                            complitionPer = ((totalDurationPlayed.toDouble() * 100) / length.toDouble()).toDouble()
                            setLog(
                                TAG,
                                "callStreamEventAnalytics: complitionPer:${complitionPer} totalTime:${length}"
                            )

                        }
                    }catch (e:Exception){

                    }

                    try {
                        val df = DecimalFormat("###.##")
                        complitionPer = df.format(complitionPer).toDouble()
                    }catch (e:Exception){

                    }



                    if (complitionPer!! > 0) {
                        val intComplitionPer: Int = complitionPer.toInt()
                        hashMap.put(EventConstant.PERCENTAGE_COMPLETION_EPROPERTY, "" + intComplitionPer)
                    }
                    //hashMap.put(EventConstant.PERCENTAGE_COMPLETION_EPROPERTY, "")
//        hashMap.put(EventConstant.PLAYLIST_ID_EPROPERTY, "" + eventModel?.playlistID)
//        hashMap.put(EventConstant.PLAYLIST_NAME_EPROPERTY, "" + eventModel?.playlistName)
                    hashMap.put(
                        EventConstant.PODCAST_ALBUM_NAME_EPROPERTY,
                        "" + "" + eventModel?.podcast_album_name
                    )
                    hashMap.put(EventConstant.SINGER_EPROPERTY, "" + eventModel?.singer)
                    hashMap.put(EventConstant.SONG_NAME_EPROPERTY, "" + track?.data?.head?.headData?.title)
                    hashMap.put(EventConstant.CONTENT_NAME_EPROPERTY, "" + track?.data?.head?.headData?.title)
                    hashMap.put(EventConstant.CONTENT_TYPE_ID_EPROPERTY,""+track?.data?.head?.headData?.type)
                    hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + eventModel?.sourceName)
                    hashMap.put(EventConstant.SUB_GENRE_EPROPERTY, "" + eventModel?.subGenre)
                    hashMap.put(EventConstant.LYRICS_TYPE_EPROPERTY, "" + eventModel?.lyrics_type)
                    hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY, "" + eventModel.bucketName)
                    hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+eventModel?.bucketName)
                    hashMap.put(EventConstant.SOURCE_PAGE_EPROPERTY, "" + MainActivity.lastItemClicked+","+ MainActivity.headerItemName+","+eventModel?.bucketName)


//        hashMap.put(
//            EventConstant.SUBSCRIPTION_STATUS_EPROPERTY,
//            "" + eventModel?.subscriptionStatus
//        )
                    hashMap.put(EventConstant.TEMPO_EPROPERTY, "" + eventModel?.tempo)
                    hashMap.put(
                        EventConstant.YEAROFRELEASE_EPROPERTY,
                        "" + DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_YYYY,
                            eventModel?.release_Date
                        )
                    )
                    hashMap.put(EventConstant.RATING_EPROPERTY, "" + eventModel?.rating)
                    hashMap.put(EventConstant.IS_ORIGINAL_EPROPERTY, "" + eventModel?.is_original)
                    hashMap.put(EventConstant.CATEGORY_EPROPERTY, "" + eventModel?.category)
                    if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable!!){
                        hashMap.put(EventConstant.CAST_ENABLED_EPROPERTY, "true")
                    }else{
                        hashMap.put(EventConstant.CAST_ENABLED_EPROPERTY, "false")
                    }
                    hashMap.put(EventConstant.AGE_RATING_EPROPERTY, eventModel.age_rating)
                    hashMap.put(EventConstant.CONTENT_PAY_TYPE_EPROPERTY, "" + eventModel?.content_Pay_Type)
                    hashMap.put(EventConstant.CRITIC_RATING_EPROPERTY, "" + eventModel?.critic_Rating)
                    hashMap.put(EventConstant.KEYWORDS_EPROPERTY, "" + eventModel?.keywords)
                    hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
                    hashMap.put(EventConstant.PTYPE_EPROPERTY, "" + eventModel.ptype)
                    hashMap.put(EventConstant.PID_EPROPERTY, "" + eventModel.pid)
                    hashMap.put(EventConstant.PNAME_EPROPERTY, "" + eventModel.pName)

                    hashMap.put(
                        EventConstant.RELEASE_DATE_EPROPERTY,
                        "" + DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_FORMAT_DD_MM_YYYY_slash,
                            eventModel?.release_Date
                        )
                    )

                    if(BaseActivity.tvshowDetail!=null&&BaseActivity?.tvshowDetail?.data!=null){
                        if(eventModel.contentID.equals(BaseActivity.tvshowDetail.data?.id)){
                            hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + BaseActivity.tvshowDetail.data?.season)
                            hashMap.put(EventConstant.EPISODENUMBER_EPROPERTY, "" + BaseActivity.tvshowDetail.data?.episode)
                        }else{
                            hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel.season_Number)
                            hashMap.put(EventConstant.EPISODENUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
                        }
                    }else{
                        hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel.season_Number)
                        hashMap.put(EventConstant.EPISODENUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
                    }


//        hashMap.put(EventConstant.STATUS_EPROPERTY, "" + eventModel?.status)
                    hashMap.put(
                        EventConstant.SUBTITLE_LANGUAGE_SELECTED_EPROPERTY,
                        "" + eventModel?.subtitleLanguageSelected
                    )
                    hashMap.put(EventConstant.SUBTITLE_ENABLE_EPROPERTY, "" + eventModel?.subtitleEnable)
                    hashMap.put(EventConstant.USER_RATING_EPROPERTY, "" + eventModel?.userRating)
                    hashMap.put(EventConstant.VIDEO_QUALITY_EPROPERTY, "" + eventModel?.videoQuality)
                    hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"Video Player")
                    hashMap.put(EventConstant.CATEGORYNAME_EPROPERTY,""+eventModel?.category)
                    hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY,""+""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
                    if (Constant.API_DEVICE_TYPE.equals("Android",ignoreCase = true)){
                        hashMap.put(EventConstant.CARPLAY, "false")
                    }else{
                        hashMap.put(EventConstant.CARPLAY, "true")
                    }

                    setLog("TAG", "callStreamEventAnalytics hashMap:$hashMap")
                    if(hashMap.containsKey(EventConstant.SONG_NAME_EPROPERTY)){
                        if(!TextUtils.isEmpty(hashMap.get(EventConstant.SONGNAME_EPROPERTY))){
                            EventManager.getInstance().sendEvent(StreamEvent(hashMap))
                        }

                    }

                }catch (e:Exception){

                }
            }
        }catch (e:Exception){

        }
    }
    private fun callStreamEventAnalyticsStart(track: PlayableContentModel, isStream:Boolean) {
        try {
            val eventModel =
                HungamaMusicApp.getInstance().getEventData("" + track?.data?.head?.headData?.id)
            var totalDurationPlayed = 0L
            if (mPlayer != null && mPlayer?.currentPosition != null && mPlayer?.currentPosition!! > 0){
                totalDurationPlayed = TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!)
            }
            val hashMap = java.util.HashMap<String, String>()
            hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
            hashMap.put(EventConstant.AUDIO_QUALITY_EPROPERTY, "" + eventModel?.audioQuality)
            hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + eventModel?.bucketName)
            hashMap.put(
                EventConstant.CONNECTION_TYPE_EPROPERTY,
                ConnectionUtil(getApplicationContext()).networkType
            )
            if (ConnectionUtil(getApplicationContext()).isOnline(false)){
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + eventModel?.consumptionType)
            }else{
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + EventConstant.CONSUMPTIONTYPE_OFFLINE)
            }
            var newContentId= track?.data?.head?.headData?.id
            var contentIdData=newContentId?.replace("playlist-","")
            hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)
            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeNameForStream(""+track?.data?.head?.headData?.type))
            hashMap.put(EventConstant.DEVICE_MODEL_EPROPERTY, "" + Utils.getDeviceName())
            hashMap.put(EventConstant.DURATION_EPROPERTY, "" + totalDurationPlayed)
            hashMap.put(EventConstant.DURATION_BG_EPROPERTY, "")
            hashMap.put(EventConstant.DURATION_FG_EPROPERTY, "" + totalDurationPlayed)
            hashMap.put(EventConstant.GENRE_EPROPERTY, "" + eventModel?.genre)
            hashMap.put(EventConstant.LABEL_EPROPERTY, "" + eventModel?.label)
            hashMap.put(EventConstant.LABEL_ID_EPROPERTY, "" + eventModel?.label_id)
            hashMap.put(EventConstant.LANGUAGE_EPROPERTY, "" + eventModel?.language)

            var totalTime=0L
            totalTime = mPlayer?.duration!! //length
            var length = TimeUnit.MILLISECONDS.toSeconds(totalTime)
            if(!TextUtils.isEmpty(eventModel.duration!!) && !eventModel.duration.equals("0")){
                length=eventModel.duration.toLong()
            }
            if (length > 0){
                hashMap.put(EventConstant.LENGTH_EPROPERTY, "" + length)
            }else{
                hashMap.put(EventConstant.LENGTH_EPROPERTY, "0")
            }
            hashMap.put(EventConstant.LYRICIST_EPROPERTY, "" + eventModel?.lyricist)
            hashMap.put(EventConstant.MOOD_EPROPERTY, "" + eventModel?.mood)
            hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY, "" + eventModel?.musicDirectorComposer)

            hashMap.put(EventConstant.ORIGINAL_ALBUM_NAME_EPROPERTY, "" + eventModel?.originalAlbumName)

            var complitionPer=0.0
            try {
                if(totalDurationPlayed>0 && length>0){
                    complitionPer = ((totalDurationPlayed.toDouble() * 100) / length.toDouble()).toDouble()
                    setLog(
                        TAG,
                        "callStreamEventAnalytics: complitionPer:${complitionPer} totalTime:${length}"
                    )

                }
            }catch (e:Exception){

            }

            try {
                val df = DecimalFormat("###.##")
                complitionPer = df.format(complitionPer).toDouble()
            }catch (e:Exception){

            }

            if (complitionPer!! > 0) {
                val intComplitionPer: Int = complitionPer.toInt()
                hashMap.put(EventConstant.PERCENTAGE_COMPLETION_EPROPERTY, "" + intComplitionPer)
            }

            hashMap.put(
                EventConstant.PODCAST_ALBUM_NAME_EPROPERTY,
                "" + "" + eventModel?.podcast_album_name
            )
            hashMap.put(EventConstant.SINGER_EPROPERTY, "" + eventModel?.singer)
            hashMap.put(EventConstant.SONG_NAME_EPROPERTY, "" + track?.data?.head?.headData?.title)
            hashMap.put(EventConstant.CONTENT_NAME_EPROPERTY, "" + track?.data?.head?.headData?.title)
            hashMap.put(EventConstant.CONTENT_TYPE_ID_EPROPERTY,""+track?.data?.head?.headData?.type)
            hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY, "" + eventModel.bucketName)
            hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + eventModel?.sourceName)
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+eventModel?.bucketName)
            hashMap.put(EventConstant.SOURCE_PAGE_EPROPERTY, "" + MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+eventModel?.bucketName)

            hashMap.put(EventConstant.SUB_GENRE_EPROPERTY, "" + eventModel?.subGenre)
            hashMap.put(EventConstant.LYRICS_TYPE_EPROPERTY, "" + eventModel?.lyrics_type)

            hashMap.put(EventConstant.TEMPO_EPROPERTY, "" + eventModel?.tempo)
            hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,
                "" + DateUtils.convertDate(
                    DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                    DateUtils.DATE_YYYY,
                    eventModel?.release_Date
                )
            )
            hashMap.put(EventConstant.RATING_EPROPERTY, "" + eventModel?.rating)
            hashMap.put(EventConstant.IS_ORIGINAL_EPROPERTY, "" + eventModel?.is_original)
            hashMap.put(EventConstant.CATEGORY_EPROPERTY, "" + eventModel?.category)
            if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable!!){
                hashMap.put(EventConstant.CAST_ENABLED_EPROPERTY, "true")
            }else{
                hashMap.put(EventConstant.CAST_ENABLED_EPROPERTY, "false")
            }

            hashMap.put(EventConstant.AGE_RATING_EPROPERTY, eventModel.age_rating)
            hashMap.put(EventConstant.CONTENT_PAY_TYPE_EPROPERTY, "" + eventModel?.content_Pay_Type)
            hashMap.put(EventConstant.CRITIC_RATING_EPROPERTY, "" + eventModel?.critic_Rating)
            hashMap.put(EventConstant.KEYWORDS_EPROPERTY, "" + eventModel?.keywords)
            hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
            hashMap.put(EventConstant.PTYPE_EPROPERTY, "" + eventModel.ptype)
            hashMap.put(EventConstant.PID_EPROPERTY, "" + eventModel.pid)
            hashMap.put(EventConstant.PNAME_EPROPERTY, "" + eventModel.pName)

            hashMap.put(
                EventConstant.RELEASE_DATE_EPROPERTY,
                "" + DateUtils.convertDate(
                    DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                    DateUtils.DATE_FORMAT_DD_MM_YYYY_slash,
                    eventModel?.release_Date
                )
            )

            if(BaseActivity.tvshowDetail!=null&&BaseActivity?.tvshowDetail?.data!=null){
                if(eventModel.contentID.equals(BaseActivity.tvshowDetail.data?.id)){
                    hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + BaseActivity.tvshowDetail.data?.season)
                    hashMap.put(EventConstant.EPISODENUMBER_EPROPERTY, "" + BaseActivity.tvshowDetail.data?.episode)
                }else{
                    hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel.season_Number)
                    hashMap.put(EventConstant.EPISODENUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
                }
            }else{
                hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel.season_Number)
                hashMap.put(EventConstant.EPISODENUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
            }

            hashMap.put(
                EventConstant.SUBTITLE_LANGUAGE_SELECTED_EPROPERTY,
                "" + eventModel?.subtitleLanguageSelected
            )
            hashMap.put(EventConstant.SUBTITLE_ENABLE_EPROPERTY, "" + eventModel?.subtitleEnable)
            hashMap.put(EventConstant.USER_RATING_EPROPERTY, "" + eventModel?.userRating)
            hashMap.put(EventConstant.VIDEO_QUALITY_EPROPERTY, "" + eventModel?.videoQuality)
            hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"Video Player")
            if (Constant.API_DEVICE_TYPE.equals("Android",ignoreCase = true)){
                hashMap.put(EventConstant.CARPLAY, "false")
            }else{
                hashMap.put(EventConstant.CARPLAY, "true")
            }

            setLog("TAG", "callStreamEventAnalytics hashMap:$hashMap")
            if(hashMap.containsKey(EventConstant.SONG_NAME_EPROPERTY)){
                if(!TextUtils.isEmpty(hashMap[EventConstant.SONGNAME_EPROPERTY])){
                    if (isStream)
                        EventManager.getInstance().sendEvent(StreamEvent(hashMap))
                    else{
                        hashMap.remove(EventConstant.DURATION_EPROPERTY)
                        hashMap.remove(EventConstant.DURATION_BG_EPROPERTY)
                        hashMap.remove(EventConstant.DURATION_FG_EPROPERTY)
                        hashMap.remove(EventConstant.PERCENTAGE_COMPLETION_EPROPERTY)

                        EventManager.getInstance().sendEvent(StreamStartEvent(hashMap))
                    }
                }

            }

        }catch (e:Exception){

        }

    }

    private fun callVideoPlayAction(track: PlayableContentModel) {
        baseIOScope?.launch {
            val eventModel =
                HungamaMusicApp.getInstance().getEventData("" + track?.data?.head?.headData?.id)
            val hashMap = java.util.HashMap<String, String>()
            hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
            hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + eventModel.songName)
            hashMap.put(EventConstant.FROMBUCKET_EPROPERTY, "" + eventModel.bucketName)
            hashMap.put(EventConstant.LASTVISIBLEROWPOSITION_EPROPERTY, "" + playableItemPosition)
            hashMap.put(EventConstant.LISTINGSCREENNAME_EPROPERTY, "" + MainActivity.lastItemClicked+",_"+ MainActivity.headerItemName)
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "${MainActivity.lastItemClicked}_${MainActivity.headerItemName}_${eventModel.bucketName}")
            hashMap.put(EventConstant.TOBUCKET_EPROPERTY, eventModel.bucketName)

            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+track?.data?.head?.headData?.type))
            EventManager.getInstance().sendEvent(VideoPlayerActionsEvent(hashMap))
        }

    }

    private fun callVolumeEvent(action: String) {
        if(videoList.size>0){
        baseIOScope.launch {
            //if(isOnClick()){
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, action)
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + videoList.get(playableItemPosition).data?.head?.headData?.title!!)
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videoPlay${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerAudioAction(hashMap))
            //}
        }
    }
    }
    private fun callBrightnessEvent(action: String) {
        baseIOScope.launch {
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, action)
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+videoList.get(playableItemPosition).data?.head?.headData?.type)!!)
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videoPlay${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerBrightnessActionEvent(hashMap))
        }
    }

    fun noInternetPopupOpen(){
        ConnectionUtil(requireContext()).isOnline
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setLog("MusicVideoDetailFragment", "onHiddenChanged-hidden-$hidden")
        if (hidden){
            if (mPlayer?.isPlaying == true && MainActivity.lastItemClicked != Constant.BOTTOM_NAV_PLAYER){
                val intent = Intent(Constant.MUSIC_VIDEO_PLAYER_EVENT)
                intent.putExtra(Constant.VIDEO_START_POSITION, mPlayer?.currentPosition)
                intent.putParcelableArrayListExtra(Constant.VIDEO_LIST_DATA, videoList)
                intent.putParcelableArrayListExtra(Constant.SEASON_LIST, seasonList)
                intent.putExtra(Constant.SELECTED_TRACK_POSITION, playableItemPosition)
                //intent.putExtra("EVENT", Constant.MUSIC_VIDEO_RESULT_CODE)
                showMiniVideoPlayer(intent)
            }
            pausePlayer()
        }else{
            if (activity != null && activity is MainActivity){
                (activity as MainActivity).closeVideoMiniplayer(false)
            }
            playPlayer()
        }
    }
    private var currentPlayer: Player? = null
    private fun setCurrentPlayer(mCurrentPlayer: Player) {
        if(isAdded() && videoListModel!=null && mPlayerView!=null){
            Log.d(TAG, "setCurrentPlayer: currentPlayer:${mCurrentPlayer==mPlayer}")
            if (this.currentPlayer == mCurrentPlayer) {
                return
            }
            mPlayerView?.setPlayer(mCurrentPlayer)
            mPlayerView?.setControllerHideOnTouch(mCurrentPlayer === mPlayer)
            if (mCurrentPlayer === castPlayer && castPlayer?.isCastSessionAvailable!!) {
                Log.d(TAG, "setCurrentPlayer: castPlayer called:${castPlayer}")

                mPlayerView?.setControllerShowTimeoutMs(0)
                mPlayerView?.showController()
                mPlayerView?.setDefaultArtwork(
                    ResourcesCompat.getDrawable(
                        context?.resources!!,
                        R.drawable.ic_baseline_cast_connected_400,  /* theme= */
                        null
                    )
                )
                isPlayNextVideoCastPlayerCalled=false
            } else { // currentPlayer == localPlayer
                mPlayerView?.setControllerShowTimeoutMs(PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS)
                mPlayerView?.setDefaultArtwork(null)
            }

            var playWhenReady = false
            val previousPlayer: Player = this.currentPlayer!!
            // Player state management.
            var playbackPositionMs = C.TIME_UNSET
            if (previousPlayer != null) {
                // Save state from the previous player.
                val playbackState = previousPlayer.playbackState
                if (playbackState != Player.STATE_ENDED) {
                    playWhenReady = previousPlayer.playWhenReady
                    playbackPositionMs=previousPlayer.currentPosition
                }
                previousPlayer.stop()
                previousPlayer.clearMediaItems()

                Log.d(TAG, "setCurrentPlayer: previousPlayer called:")
            }
            this.currentPlayer = mCurrentPlayer
            this.currentPlayer?.addListener(PlayerEventListener())

            if(videoListModel!=null){
                videoListModel?.let {
                    var mimType = MimeTypes.BASE_TYPE_AUDIO
                    if (videoListModel?.data?.head?.headData?.misc?.url?.contains(".m3u8", true)!!) {
                        mimType = MimeTypes.APPLICATION_M3U8
                    } else if (videoListModel?.data?.head?.headData?.misc?.url?.contains(".mp3", true)!!) {
                        mimType = MimeTypes.BASE_TYPE_AUDIO
                    } else if (videoListModel?.data?.head?.headData?.misc?.url?.contains(".mpd", true)!!) {
                        mimType = MimeTypes.APPLICATION_MPD
                    } else if (videoListModel?.data?.head?.headData?.misc?.url?.contains(".mp4", true)!!) {
                        mimType = MimeTypes.APPLICATION_MP4
                    }

                    val mediaItem1 = MediaItem.Builder()
                        .setUri(Uri.parse(videoListModel?.data?.head?.headData?.misc?.url))
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(videoListModel?.data?.head?.headData?.title)
                                .setDisplayTitle(videoListModel?.data?.head?.headData?.title)
                                .setSubtitle(videoListModel?.data?.head?.headData?.subtitle)
                                .setArtworkUri(Uri.parse(videoListModel?.data?.head?.headData?.image!!))
                                .setDescription(videoListModel?.data?.head?.headData?.misc?.description)
                                .build()
                        )
                        .setMimeType(mimType)
                        .build()

                    currentPlayer?.setMediaItem(mediaItem1!!, playbackPositionMs!!)
                    currentPlayer?.playWhenReady = playWhenReady
                    currentPlayer?.prepare()

                    Log.d(TAG, "setCurrentPlayer: title${currentPlayingMediaItem?.mediaMetadata?.title} artworkUri:${currentPlayingMediaItem?.mediaMetadata?.artworkUri} displayTitle:${currentPlayingMediaItem?.mediaMetadata?.displayTitle} ")
                    Log.d(TAG, "setCurrentPlayer: mediaItem1${mediaItem1}")
                }
            }else if(currentPlayingMediaItem!=null){
                currentPlayer?.setMediaItem(currentPlayingMediaItem!!, playbackPositionMs!!)
                currentPlayer?.playWhenReady = playWhenReady
                currentPlayer?.prepare()
            }
        }
    }

    /**
     * A [BroadcastReceiver] for handling action items on the picture-in-picture mode.
     */
    private val broadcastReceiver = object : BroadcastReceiver() {

        // Called when an item is clicked.
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_PIP_CONTROL) {
                return
            }
            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                CONTROL_TYPE_START_OR_PAUSE -> {
                    playPause()
                    enterPIPMode()
                }
            }
        }
    }

    private fun shouldShowPauseButton(player: Player): Boolean {
        return player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE && player.playWhenReady
    }

    /**
     * Creates a [RemoteAction]. It is used as an action icon on the overlay of the
     * picture-in-picture mode.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        @StringRes titleResId: Int,
        requestCode: Int,
        controlType: Int
    ): RemoteAction {
        return RemoteAction(
            Icon.createWithResource(requireActivity(), iconResId),
            getString(titleResId),
            getString(titleResId),
            PendingIntent.getBroadcast(
                requireActivity(),
                requestCode,
                Intent(ACTION_PIP_CONTROL)
                    .putExtra(EXTRA_CONTROL_TYPE, controlType),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}