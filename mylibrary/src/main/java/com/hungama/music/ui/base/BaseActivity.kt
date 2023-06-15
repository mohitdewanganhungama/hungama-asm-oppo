package com.hungama.music.ui.base

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.viewpager2.widget.ViewPager2
import androidx.work.*
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.facebook.share.model.*
import com.facebook.share.widget.ShareDialog
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.hungama.fetch2.*
import com.hungama.fetch2.NetworkType
import com.hungama.fetch2.Request
import com.hungama.fetch2core.*
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.repositories.PlayableContentRepos
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.home.eventsubscriber.AmplitudeSubscriber
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.Injection.provideAudioPlayer
import com.hungama.music.player.audioplayer.NowPlayingContract
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.lyrics.Lrc
import com.hungama.music.player.audioplayer.lyrics.LrcHelper
import com.hungama.music.player.audioplayer.lyrics.LrcView
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.queue.NowPlayingInfo
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.NowPlayingViewModel
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.player.download.DownloadTracker
import com.hungama.music.player.download.IntentUtil
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.*
import com.hungama.music.ui.main.viewmodel.*
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.callStreamTriggerEvent
import com.hungama.music.utils.CommonUtils.convertByteToHumanReadableFormat
import com.hungama.music.utils.CommonUtils.downloadFile
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getContentPlanType
import com.hungama.music.utils.CommonUtils.getOnPlayerOverlayAds
import com.hungama.music.utils.CommonUtils.getStreamUrl
import com.hungama.music.utils.CommonUtils.isContentDownloaded
import com.hungama.music.utils.CommonUtils.saveThumbnail
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.userCanDownloadContent
import com.hungama.music.utils.Constant.CONTENT_MOOD_RADIO
import com.hungama.music.utils.Constant.CONTENT_MOVIE
import com.hungama.music.utils.Constant.CONTENT_MUSIC
import com.hungama.music.utils.Constant.CONTENT_MUSIC_VIDEO
import com.hungama.music.utils.Constant.CONTENT_ON_DEMAND_RADIO
import com.hungama.music.utils.Constant.CONTENT_PODCAST
import com.hungama.music.utils.Constant.CONTENT_RADIO
import com.hungama.music.utils.Constant.CONTENT_TV_SHOW
import com.hungama.music.utils.Constant.MAX_DOWNLOAD_RETRY
import com.hungama.music.utils.Constant.PLAYER_LIVE_RADIO
import com.hungama.music.utils.Constant.PLAYER_MOOD_RADIO
import com.hungama.music.utils.Constant.PLAYER_ON_DEMAND_RADIO
import com.hungama.music.utils.Constant.PLAYER_PODCAST_AUDIO_ALBUM
import com.hungama.music.utils.Constant.PLAYER_PODCAST_AUDIO_TRACK
import com.hungama.music.utils.Constant.PLAYER_PODCAST_AUDIO_TRACK2
import com.hungama.music.utils.Constant.PLAYER_RADIO
import com.hungama.music.utils.Constant.SLEEP_TIMER_MENU_ITEM
import com.hungama.music.utils.Constant.TV_FASHION_TV
import com.hungama.music.utils.Constant.TV_LINEAR_TV_CHANNEL
import com.hungama.music.utils.Constant.TV_LINEAR_TV_CHANNEL_ALBUM
import com.hungama.music.utils.Constant.TV_SERIES
import com.hungama.music.utils.Constant.TV_SERIES_EPISODE
import com.hungama.music.utils.Constant.TV_SERIES_SEASON
import com.hungama.music.utils.Constant.VIDEO_EVENTS_BROADCAST_VIDEO
import com.hungama.music.utils.Constant.VIDEO_HD_MOVIE
import com.hungama.music.utils.Constant.VIDEO_MOVIE
import com.hungama.music.utils.Constant.VIDEO_MUSIC_VIDEO_PLAYLIST
import com.hungama.music.utils.Constant.VIDEO_MUSIC_VIDEO_TRACK
import com.hungama.music.utils.Constant.VIDEO_SD_MOVIE
import com.hungama.music.utils.Constant.VIDEO_SHORT_FILMS
import com.hungama.music.utils.Constant.VIDEO_START_POSITION
import com.hungama.music.utils.Constant.audioAction
import com.hungama.music.utils.Constant.isVideoStoryPlaying
import com.hungama.music.utils.Constant.noneAudio
import com.hungama.music.utils.Constant.pause
import com.hungama.music.utils.Constant.playerExpand
import com.hungama.music.utils.Constant.playing
import com.hungama.music.utils.Constant.stopReasonPause
import com.hungama.music.utils.Constant.stopReasonRemove
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.utils.download.Data
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.utils.tritonbannerview.BannersWrapper
import com.hungama.music.BuildConfig
import com.hungama.music.R
import com.moengage.inapp.MoEInAppHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.sticky_ad_view_container
import kotlinx.android.synthetic.main.dialog_sleep.*
import kotlinx.android.synthetic.main.exo_player_bottom_sheet_controller.*
import kotlinx.android.synthetic.main.layout_swipable_player_view.*
import kotlinx.android.synthetic.main.layout_tab_view.*
import kotlinx.android.synthetic.main.new_now_playing_bottom_sheet.*
import kotlinx.android.synthetic.main.new_preview_model_layout.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Base of all activity
 */
private const val KEY_BOTTOM_SHEET_STATE = "bottomSheetState"
@OptIn(UnstableApi::class)
@SuppressLint("Registered")
abstract class BaseActivity : BaseServiceBoundedActivity(), View.OnClickListener,
    NowPlayingContract.View,
    Player.Listener, DownloadTracker.Listener,
    MusicPlayerThreeDotsBottomSheetFragment.OnMusicMenuItemClick, FetchObserver<Download>, SpeedChangeDialog.OnSpeedChangeItemClick,
    ConnectedDeviceDialog.OnConnectedDeviceMenuItemClick, SleepDialog.OnSleepTimeChangeItemClick,
    AnalyticsListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener, ShareStoryPlatformDialog.OnStoryPlatformItemClick,
    AmplitudeSubscriber.GamificationUpdateListener {


    /**
     * Contains last clicked time
     */
    private var lastClickedTime: Long = 0

    private val TAG = BaseActivity::class.java.name

    public var nowPlayingQueue: NowPlayingQueue? = null

    private lateinit var nowPlayingViewModel: NowPlayingViewModel
    private var bottomSheetState: Int = BottomSheetBehavior.STATE_HIDDEN
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    protected var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private var downloadTracker: DownloadTracker? = null
    var REQUEST_CODE_WRITE_STORAGE_PERMISION = 105
    private var exoPlayer = provideAudioPlayer()
    private var trackData: Track = Track()
    var musicViewModel: MusicViewModel? = null
    lateinit var songLyrics: String
    val START_STATUS = 1003
    var isNextClick = START_STATUS
    var isLastContentPlayAfterMiniVideoPlayer = false
    val NEXT_CLICK_STATUS = 1001
    val PREVIOUS_CLICK_STATUS = 1002

    var duratoionList = ArrayList<DurationModel>()
    var durationMap = HashMap<String, ArrayList<DurationModel>>()

    private var mHandler: Handler? = null
    var artImageUrl: String? = null
    var statusBarColor = 0
    private var tapCount = 1.0

    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()

    var audioPlayerService: AudioPlayerService? = null
    var currentPlayerType = 1
    var mainHandler: Handler? = null
    var hasLyrics = false
    public var audioPlayer: SimpleExoPlayer? = null
    var downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
    private var fetch: Fetch? = null
    private var request: Request? = null
    var onDownloadQueueItemChanged: OnDownloadQueueItemChanged? = null
    var onDownloadVideoQueueItemChanged: OnDownloadVideoQueueItemChanged? = null
    var fetchMusicDownloadListener: FetchListener? = null
    var hasActiveAudioDownload = false
    var userViewModelBookmark: UserViewModel? = null
    private var sleepTimerHandler: Handler? = null
    private var isTimerRuning: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private var onLocalBroadcastEventCallBack: OnLocalBroadcastEventCallBack? = null
    val STREAM_POSITION_PREVIOUS = 1004
    val STREAM_POSITION_CURRENT = 1005
    val STREAM_POSITION_NEXT = 1006
    var isMiniplayerVisible = false
    var isBottomNavigationVisible = false
    var miniplayerHeight = 0
    var bottomNavigationHeight = 0
    var statusBarLastColor = 0
    var isSleepTimerSetToEndOfCurrentPlay = false
    var apiRetryCount = 0
    val googleImaAudioAds = 2001
    val tritonAudioAds = 2002
    var currentAudioAdsType = 0

    val playerAdsDisappearTimeInSeconds = getOnPlayerOverlayAds().disappearTimeInSec.toLong()
    val playerAdsDisplayIntervalInSecond =
        getOnPlayerOverlayAds().displayAgainCoolingTimeInSec.toLong()
    val playerAdsHandler = Handler(Looper.getMainLooper())
    var isFirstTimeAdsLoaded = true

    private var mBannersWrapper: BannersWrapper? = null
    private var mAudioPlayer: MediaPlayer = MediaPlayer()
    var isBottomStickyAdLoaded = false

    var audioSessionId: Int = Constant.NO_AUDIO_SESSION_ID
    var initialPlayerVolume = 0F
    var maxPlayerErrorRetryCount = 0
    var currentPlayer: Player?=null
    var songDuration = SongDurationModel()
    var receiver : BroadcastReceiver? = null
    lateinit var songDurationViewModel: SongDurationConfigViewModel
    lateinit var subscriptionDialogBottomsheetFragment : SubscriptionDialogBottomsheetFragmentFreeMinute

    companion object {
        var tvSleepTimer = TextView(HungamaMusicApp.getInstance().applicationContext)
        var pbDuration = ProgressBar(HungamaMusicApp.getInstance().applicationContext)
        var includeFreeMinute = View(HungamaMusicApp.getInstance().applicationContext)
        var songPreviewModel = View(HungamaMusicApp.getInstance().applicationContext)
        var newPreviewModel = View(HungamaMusicApp.getInstance().applicationContext)
        var m_view_papger = ViewPager2(HungamaMusicApp.getInstance().applicationContext)
        var maxMinAllowed = 0
        var isSwipableActive = false
        var inputStream : InputStream? = null
        var bodyRowsItemsItem : BodyRowsItemsItem = BodyRowsItemsItem()
        var isDeeplink = false
        var eventManagerStreamName = ""
        var streamIndexPrent = 0
        var isApplaunch = true
        var isPresvious = false
        var streamName = ""
        var songDataList: ArrayList<Track> = arrayListOf()
        var videoSeasonDataList: ArrayList<PlaylistModel.Data.Body.Row.Season>? = arrayListOf()
        var isFirstSong = true
        var playAudioAdsAfterCounts = 0
        var totalSongsPlayedAfterLastAudioAd = 0
        var lastSongPlayDuration = 0
        var lastSongPlayedDuration = 0
        var isAudioAdPlaying = false
        var isAdsLoadRequestInProgress = false
        var tvshowDetail = PlaylistModel.Data.Body.Row.Season.SeasonData.Misc.Track()
        var isNewSwipablePlayerOpen = false
        var isDisplayDiscover = SharedPrefHelper.getInstance().getDisplayDiscover()
        var isAppLanguageChanged = false
        var totalGetted = 0
        var localDuration = 0
        var totalPlayedSongDuration = -2
        var tracksViewModel : TracksContract.Presenter? = null
        var player11 : ExoPlayer?= null
        var mediaUrlAd = ""
        var isTouch = false
        var showAd = 0
        var nundgeAduioAdUrl = ""
        var drawerAduioAdUrl = ""

        fun setTouchData(){
             isTouch = true
             showAd = 0

            if(player11 != null) {
                player11?.pause()
            }
        }

        fun showjson() {
            if (inputStream != null) {
                inputStream?.let { setAnimation(it) }
            }
            else {
                val thread = Thread {
                    try {
                        val url = URL(CommonUtils.getNudgeImageUrl())
                        val urlConnection = url.openConnection() as HttpURLConnection
                        urlConnection.connect()
                        inputStream = urlConnection.inputStream
                        setAnimation(inputStream)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                thread.start()
            }
        }

        fun setAnimation(inputStream: InputStream?){
            val animationView: LottieAnimationView = newPreviewModel.backgroundAnimation1
            animationView.setAnimation(inputStream, "animate")
            animationView.playAnimation()
            newPreviewModel.backgroundAnimation1.visibility = View.VISIBLE
        }

        fun setTrackView(tracksViewModel1 : TracksContract.Presenter){
            tracksViewModel = tracksViewModel1
        }
        fun setTrackListData(songList: ArrayList<Track>) {
            //setLog("queue11", songList.size.toString())
            //setLog("PlayNextSong", "BaseActivity-1--size-songDataList-${songDataList.size}-${nowPlayingCurrentIndex()}")
            songDataList = songList
            /*setLog("PlayNextSong", "BaseActivity-2--size-songDataList-${songDataList.size}-${nowPlayingCurrentIndex()}")
            if (songDataList?.size!! > nowPlayingCurrentIndex()){
                setLog("PlayNextSong", "BaseActivity-NextSong-${songDataList?.get(nowPlayingCurrentIndex()+1)?.title}")
            }*/
            CoroutineScope(Dispatchers.IO).launch{
                //setLog("dbOperation", "BaseActivity-setTrackListData-1")
//                withContext(Dispatchers.Default) {
                    //setLog("dbOperation", "BaseActivity-setTrackListData-2")
                    try {
                        AppDatabase.getInstance()?.trackDao()?.deleteAll()
                    }catch (e:Exception){

                    }
                    /*setLog(
                        "dbOperation",
                        "BaseActivity-setTrackListData-2.1-songDataList size:${songDataList?.size}"
                    )*/
                    //setLog("queue11", "deleteAll called songDataList size:${songDataList?.size}")
//                }
                //setLog("dbOperation", "BaseActivity-setTrackListData-3")
                /*withContext(Dispatchers.Default) {
                    setLog("dbOperation", "BaseActivity-setTrackListData-4")
                    val sList = AppDatabase.getInstance()?.trackDao()?.getAllSong()
                    setLog("dbOperation", "BaseActivity-setTrackListData-4.1-sList?.size-${sList?.size}")
                    setLog("queue11", "deleteAll called songDataList size:${sList?.size}")
                }
                setLog("dbOperation", "BaseActivity-setTrackListData-5")*/
                withContext(Dispatchers.Default) {
                    try {
                    //setLog("dbOperation", "BaseActivity-setTrackListData-6")
                    songList.forEachIndexed { index, track ->
                        if (track != null){
                            track.uniquePosition = index + 1.toLong()
                        }
                    }
                    //setLog("dbOperation", "BaseActivity-setTrackListData-6.1-songList size:${songList.size}")
                    //setLog("queue11", "songDataList after size:${songList.size}")

                        AppDatabase.getInstance()?.trackDao()?.insertOrReplaceAll(songList)
                    }catch (e:Exception){

                    }

                    //setLog("dbOperation", "BaseActivity-setTrackListData-6.2-songList size:${songList.size}")

                    isFirstSong = true

                    //setLog("queue11", "insertOrReplaceAll songList size:${songList.size}")
                }

            }
            CoroutineScope(Dispatchers.Main).launch {
                setTouchData()
            }

        }

        fun setVideoTrackListData(videoList: ArrayList<PlaylistModel.Data.Body.Row.Season>) {
            videoSeasonDataList = videoList
        }

        var currentDownloadingIndex = 0
        var playIndex = 0
        public fun nowPlayingCurrentIndex(): Int {
            return playIndex
        }

        public fun updateNowPlayingCurrentIndex(index: Int) {
            playIndex = index
        }

        public fun nowPlayingCurrentTrack(): Track? {
            if (!songDataList.isNullOrEmpty() && songDataList.size > nowPlayingCurrentIndex()){
                return songDataList.get(nowPlayingCurrentIndex())
            }else{
                return null
            }
        }

        var isGoldUser = false
        fun setIsGoldUser(){
            isGoldUser = CommonUtils.isUserHasGoldSubscription()
            setIsNoAdsUser()
        }

        fun getIsGoldUser(): Boolean {
            return isGoldUser
        }

        var isNoAdsUser = false
        fun setIsNoAdsUser(){
            isNoAdsUser = CommonUtils.isUserHasNoAdsSubscription()
        }

        fun getIsNoAdsUser(): Boolean {
            return isNoAdsUser
        }

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LanguageUtil.getLocal(base!!))
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        /**
         * Prevent Screenshot Or Screen Recorder in Android
         */
//        Utils.setStopScreenrecord(this@BaseActivity)
        CommonUtils.getSongDurationConfig()
        val mId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        setLog("DeveiceIdBase", " " + mId.toString())
        super.onCreate(savedInstanceState)
        val resourceId = getLayoutResourceId()
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(resourceId, null)
        songDurationViewModel = ViewModelProvider(this).get(SongDurationConfigViewModel::class.java)

        if (view.findViewById<View>(R.id.nowPlayingBottomSheet) == null) {
            throw IllegalAccessException("Child layout must have have a BottomSheet with id R.id.nowPlayingBottomSheet")
        }
        if(!CommonUtils.isUserHasGoldSubscription()) {
            callSongDurationAPI()
//            getPlayableContentUrl(CommonUtils.getNudgeAudioId(), true)
        }

        var nonRepeatBroadCast = 0
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        subscriptionDialogBottomsheetFragment = SubscriptionDialogBottomsheetFragmentFreeMinute(
            this,
            "",
            "",
            null,
            "",
            null,
            null
        )
        subscriptionDialogBottomsheetFragment.isCancelable = false
        player11 = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = true
            }
        player11?.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY){
                    pausePlayer()
                   m_view_papger.setOnClickListener(null)
                }
                else if(playbackState == Player.STATE_ENDED){
                    setTouchData()
                    if (songPreviewModel.isVisible)
                    {
                        songPreviewModel.visibility = View.GONE
                    }
                        if (HungamaMusicApp.getInstance().activityVisible && subscriptionDialogBottomsheetFragment.isVisible){
                            subscriptionDialogBottomsheetFragment.dismiss()
                        }
                        playNextSong(true)
                }

            }
        })

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                if(!CommonUtils.isUserHasGoldSubscription() && nonRepeatBroadCast == 0 && CommonUtils.getSongDurationConfig().enable_minutes_quota) {
                    setLog("lsahghsdghoa", "3")

                    callSongDurationAPI()
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(10000)
                        nonRepeatBroadCast = 0
                    }
                }
                nonRepeatBroadCast = 1
                setLog("ahglahighsia", "BaseActivity Broadcast")
            }
        }

        if (!CommonUtils.isUserHasGoldSubscription() && CommonUtils.getSongDurationConfig().enable_minutes_quota) {
            val lbm = LocalBroadcastManager.getInstance(this)
            lbm.registerReceiver(
                receiver as BroadcastReceiver,
                IntentFilter(Constant.SONG_DURATION_BROADCAST)
            )
        }
        setContentView(resourceId)

        if (savedInstanceState != null) {
            bottomSheetState = savedInstanceState.getInt(KEY_BOTTOM_SHEET_STATE, BottomSheetBehavior.STATE_HIDDEN)
        }
//        setBottomSheet(bottomSheetState)

        nowPlayingViewModel = NowPlayingViewModel(
            this,
            Injection.provideTrackRepository()
        )
        onViewCreated(savedInstanceState)
        mainHandler = Handler(Looper.getMainLooper())
        CoroutineScope(Dispatchers.Main).launch {
            statusBarLastColor = resources.getColor(R.color.home_bg_color)
            durationMap = HashMap<String, ArrayList<DurationModel>>()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            DemoUtil.getDownloadManager(this@BaseActivity).maxParallelDownloads = 1
            downloadTracker = DemoUtil.getDownloadTracker(this@BaseActivity)
            downloadTracker?.addListener(this@BaseActivity)
            fetch = Fetch.getDefaultInstance()
            statusBarBg()
            firebaseAnalytics = FirebaseAnalytics.getInstance(this@BaseActivity)
            userViewModelBookmark = ViewModelProvider(
                this@BaseActivity
            ).get(UserViewModel::class.java)
            musicViewModel = ViewModelProvider(
                this@BaseActivity
            ).get(MusicViewModel::class.java)
            playAudioAdsAfterCounts = CommonUtils.getAudioAdPreference().firstServe
            totalSongsPlayedAfterLastAudioAd = 0
            setLog("songsPlayedCurrent", totalSongsPlayedAfterLastAudioAd.toString())
            setBluetoothBroadcast()
            updateAudioAdPlayingStatusAndProvider(false, currentAudioAdsType)

            LocalBroadcastManager.getInstance(this@BaseActivity).registerReceiver(mNotificationMessageReceiver, IntentFilter(Constant.NOTIFICATION_PLAYER_EVENT))
            LocalBroadcastManager.getInstance(this@BaseActivity).registerReceiver(mNotificationMessageReceiver, IntentFilter(Constant.STORY_PLAYER_EVENT))
            LocalBroadcastManager.getInstance(this@BaseActivity).registerReceiver(mNotificationMessageReceiver, IntentFilter(Constant.AUDIO_QUALITY_CHANGE_EVENT));
            setLog("BaseActivity", "Lifecycle-onCreate")
            val homeWatcher = HomeWatcher(this@BaseActivity)
            homeWatcher.setOnHomePressedListener(homeWatcherListener)
            homeWatcher.startWatch()

            /**
             * Gamification listener set
             */
            EventManager.getInstance().findAmplitudeSubscriber().registerGamificationListener(this@BaseActivity)
            loadBottomAds()
        }
    }

    fun callSongDurationAPI(){
        val songDurationData = AppDatabase.getInstance()?.songDuration()?.getSongDuration()
        if (songDurationData != null){
            songDuration = songDurationData
        }
        setLog("SongDurationData ", " database " + songDurationData.toString())


        if (ConnectionUtil(this).isOnline(false)) {
            songDurationViewModel.getUserPreviewDetails(this, CommonUtils.getSongDurationConfig().global_limited_minutes_quota.toString())?.observe(this){

                if(it.status != com.hungama.music.data.webservice.utils.Status.LOADING){
                    setLog("SongDurationData ", " firebaseData " + CommonUtils.getSongDurationConfig())
                    songDuration.stream_max_min_allowed = it.data?.stream_max_minutes_allowed?.toInt()

                    if(it.status == com.hungama.music.data.webservice.utils.Status.SUCCESS){
                        songDuration.current_timestampm = it.data?.current_timestamp.toString()
                        setLog("SongDurationData ", " isStreamed " + it.data?.is_first_stream_started.toString())
                        maxMinAllowed = it.data?.user_streamed_min?.toInt()!!

                        if (it.data?.is_first_stream_started.toString().contains("false")){
/*                            if (songDurationData == null)
                            {
                                songDuration.user_streamed_min = it.data?.stream_max_minutes_allowed?.toInt()
                            }
                            else
                            {
*//*                               if (songDuration.hungama_user_id != SharedPrefHelper.getInstance().getUserId().toString()) {
                                   songDuration.user_streamed_min = CommonUtils.getSongDurationConfig().global_limited_minutes_quota
                               }*//*

                            }*/
                            setLog("SongDurationData ", " firebaseData " + CommonUtils.getSongDurationConfig())
                            songDuration.user_streamed_min = it.data?.stream_max_minutes_allowed?.toInt()
                            songDuration.Is_first_stream_started = 0
                        }
                        else{
                            songDuration.user_streamed_min = it.data?.user_streamed_min

                            songDuration.Is_first_stream_started = 1
                        }
                    }
                    else
                    {
                        songDuration.user_streamed_min = CommonUtils.getSongDurationConfig().global_limited_minutes_quota
                    }
                    songDuration.hungama_user_id = SharedPrefHelper.getInstance().getUserId().toString()
                }

                if (songDurationData == null){
                    AppDatabase.getInstance()?.songDuration()?.insertOrReplace(songDuration)
                }
                else{
                    AppDatabase.getInstance()?.songDuration()?.updateSongDuration(songDuration)
                }
                totalGetted = songDuration.user_streamed_min!!
                totalPlayedSongDuration = -2
                setLog("SongDurationData", " APi \n\n" +  Gson().toJson(songDurationData) + "\n\n " + Gson().toJson(it.data) + "\n\n$totalGetted")
            }
        }
    }

    fun getPlayableContentUrl(id:String, isNundge:Boolean){
        if (ConnectionUtil(this).isOnline) {
            playableContentViewModel.getPlayableContentList(applicationContext, id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS-> {

                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    mediaUrlAd = it?.data?.data?.head?.headData?.misc?.url.toString()
                                    if (isNundge)
                                    {
                                        nundgeAduioAdUrl = it?.data?.data?.head?.headData?.misc?.url.toString()
                                        getPlayableContentUrl(CommonUtils.getDrawerAudioId(), false)
                                    }
                                    else
                                        drawerAduioAdUrl = it?.data?.data?.head?.headData?.misc?.url.toString()
                                }
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{

                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            Utils.showSnakbar(this,rootLayout, true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    override fun onResume() {

        LanguageUtil.getLocal(this@BaseActivity)
        HungamaMusicApp.getInstance().activityResumed()
        super.onResume()
        setLog("BaseActivityLifecycleMethods", "onResume")
        setLog("countFgTime", "1")

        addFGTime()
        Constant.API_DEVICE_TYPE="Android"

        MoEInAppHelper.getInstance().showInApp(this@BaseActivity)
        setLocalBroadcastEventCall(null, Constant.NOTIFICATION_PLAYER_EVENT)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onStart() {
        super.onStart()
        setLog("BaseActivityLifecycleMethods", "onStart")
        MoEInAppHelper.getInstance().showInApp(this@BaseActivity)

        if (player11?.isPlaying == false && subscriptionDialogBottomsheetFragment.isVisible){
                subscriptionDialogBottomsheetFragment.dismiss()
        }
    }

    override fun onPause() {
        setLog("BaseActivityLifecycleMethods", "onPause")
        LanguageUtil.getLocal(this@BaseActivity!!)
        addBGTime()

        HungamaMusicApp.getInstance().activityPaused()
        super.onPause()
    }

    fun addFGTime() {
        try {
            setLog("BaseActivityLifecycleMethods", "addFGTime")
            if (audioPlayer != null) {
                setLog("BaseActivityLifecycleMethods", "addFGTime- " + audioPlayer?.isPlaying)
                if (audioPlayer?.isPlaying == true){
                    startPlayerDurationCallback()
                }else{
                    removePlayerDurationCallback()
                }

                var durationLastModel = DurationModel()
                val durationModel = DurationModel()
                if (!duratoionList.isNullOrEmpty()) {
                    durationLastModel = duratoionList.get(duratoionList.size - 1)
                    if (durationLastModel != null && durationLastModel?.start_bg_time!! > 0) {
                        durationModel.start_bg_time = durationLastModel?.start_bg_time
                    }
                }

                if (audioPlayer?.currentPosition != null && audioPlayer?.currentPosition!! > 0) {
                    if (!duratoionList.isNullOrEmpty()){
                        if (duratoionList.get(duratoionList.size - 1).start_fg_time!! < audioPlayer?.currentPosition!!){
                            durationModel.start_fg_time = audioPlayer?.currentPosition
                        }else{
                            return
                        }

                    }else {
                        durationModel.start_fg_time = audioPlayer?.currentPosition
                    }
                }

                duratoionList.add(durationModel)
                if (durationMap != null && !songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                    durationMap?.put(
                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id,
                        duratoionList
                    )
                }
            }
        }catch (e:Exception){

        }
    }

    fun addBGTime() {
        try {
            setLog("BaseActivityLifecycleMethods", "addBGTime")
            if (audioPlayer != null) {
                setLog("BaseActivityLifecycleMethods", "addBGTime- " + audioPlayer?.isPlaying)
                var durationLastModel = DurationModel()
                val durationModel = DurationModel()
                if (!duratoionList.isNullOrEmpty()) {
                    durationLastModel = duratoionList.get(duratoionList.size - 1)
                    if (durationLastModel != null && durationLastModel?.start_fg_time!! > 0) {
                        durationModel.start_fg_time = durationLastModel?.start_fg_time
                    }
                }
                if (audioPlayer?.currentPosition != null && audioPlayer?.currentPosition!! > 0) {
                    if (!duratoionList.isNullOrEmpty()){
                        if (duratoionList.get(duratoionList.size - 1).start_bg_time!! < audioPlayer?.currentPosition!!){
                            durationModel.start_bg_time = audioPlayer?.currentPosition
                        }else{
                            return
                        }
                    }else {
                        durationModel.start_bg_time = audioPlayer?.currentPosition
                    }

                }
                duratoionList.add(durationModel)
                if (durationMap != null && !songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                    durationMap?.put(
                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id,
                        duratoionList
                    )
                }
            }
        }catch (e:Exception){

        }
    }

    override fun onStop() {
        super.onStop()
        setLog("BaseActivityLifecycleMethods", "onStop")
        removePlayerProgressChangeEventCallBack()
        removeMusicPlayerThreeDotMenuEventCallBack()
        if (audioPlayer != null) {
            if (currentPlayerType == CONTENT_MOVIE
                || currentPlayerType == CONTENT_TV_SHOW
                || currentPlayerType == CONTENT_MUSIC_VIDEO
            ) {
                setLog("BaseActivityLifecycleMethods", "audioPlayer?.pause()")
                audioPlayer?.pause()
            }
        }
        removePlayerDurationCallback()
        setLog("BaseActivityLifecycleMethods", "boundToService-$boundToService")
        if (boundToService) {
            shortPlayerControlView.player = null
            player_view.player = null

            audioPlayer?.removeListener(this)
        }
        SharedPrefHelper.getInstance().getUserId()?.let { setSongDuration(it) }
    }

    fun setSongDuration(userId:String){
        if(!CommonUtils.isUserHasGoldSubscription()) {
            if (totalPlayedSongDuration >= 0) {

                val sondDbData = AppDatabase.getInstance()?.songDuration()?.getSongDuration()
                sondDbData?.user_streamed_min = localDuration

                sondDbData?.let {
                    AppDatabase.getInstance()?.songDuration()?.updateSongDuration(it)
                }
                val reqJsonObject = JSONObject()
                reqJsonObject.put("uid", userId)
                reqJsonObject.put("user_streamed_min", localDuration.toString())
                reqJsonObject.put("first_stream_start_time", sondDbData?.current_timestampm)
                reqJsonObject.put("is_first_stream_started", "true")

                songDurationViewModel.setUserPreviewDetails(this@BaseActivity, reqJsonObject)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setLog("BaseActivityLifecycleMethods", "onDestroy-isAppLanguageChanged-$isAppLanguageChanged")
        if (!isAppLanguageChanged){
            try {
                stopAudioPlayer()
            }catch (e:Exception){

            }

            try {
                removeLocalBroadcastEventCallBack()
                removePlayerDurationCallback()
                removeSleepTimerCallback()
                removePlayerAdsCallBack()
                removeDurationCallback()
            }catch (e:Exception){

            }

            try {
                nowPlayingViewModel.onCleanup()
            }catch (e:Exception){

            }

            try {
                fetch?.close()
                if (downloadTracker != null) {
                    DemoUtil.getDownloadManager(this).pauseDownloads()
                    removeVideoDownloadListener()
                    downloadTracker?.removeListener(this)
                }
            }catch (e:Exception){

            }


            try {
                stopTritonAudioAd()
                removeBluetoothBroadcast()
                mBannersWrapper?.release()
            }catch (e:Exception){

            }

            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationMessageReceiver)
            }catch (e:Exception){

            }

            try {
                closePIPVideoPlayer()
            }catch (e:Exception){

            }

            try {
                HomeWatcher(this).stopWatch()
            }catch (e:Exception){

            }


            setLog("On Destory","On Destroy BaseActivity hidePlayerNotification called")
        }else{
            isAppLanguageChanged = false
        }


/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceIntent = Intent(this, SongDurationService::class.java)
            serviceIntent.putExtra("songDurationData", (forgroundPlayed + totalBG).toString())
            startForegroundService(serviceIntent)
        }*/

        if(!CommonUtils.isUserHasGoldSubscription()) {
            if (totalPlayedSongDuration >= 0) {

                val sondDbData = AppDatabase.getInstance()?.songDuration()?.getSongDuration()
                sondDbData?.user_streamed_min = localDuration

                sondDbData?.let {
                    AppDatabase.getInstance()?.songDuration()?.updateSongDuration(it)
                }
                val inputData = androidx.work.Data.Builder()
                    .putString("duration", sondDbData?.user_streamed_min.toString())
                    .putString("date", sondDbData?.current_timestampm)
                    .putString("uid", SharedPrefHelper.getInstance().getUserId()).build()
                val uploadDataConstraints = Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build()
                val uploadWorkRequest = OneTimeWorkRequestBuilder<WorkManagerClass>()
                    .setConstraints(uploadDataConstraints)
                    .setInputData(inputData).build()
                    WorkManager.getInstance(this@BaseActivity).enqueue(uploadWorkRequest)
                val reqJsonObject = JSONObject()
                reqJsonObject.put("uid", SharedPrefHelper.getInstance().getUserId())
                reqJsonObject.put("user_streamed_min", sondDbData?.user_streamed_min.toString())
                reqJsonObject.put("first_stream_start_time", sondDbData?.current_timestampm)
                reqJsonObject.put("is_first_stream_started", "true")

                songDurationViewModel.setUserPreviewDetails(this@BaseActivity, reqJsonObject)
            }
        }
        totalPlayedSongDuration = -2

    }

    /**
     * Adds the Fragment into layout container
     *
     * @param fragmentContainerResourceId Resource id of the layout in which Fragment will be added
     * @param currentFragment             Current loaded Fragment to be hide
     * @param nextFragment                New Fragment to be loaded into fragmentContainerResourceId
     * @param requiredAnimation           true if screen transition animation is required
     * @param commitAllowingStateLoss     true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */

    @Throws(IllegalStateException::class)
    fun addFragment(
        fragmentContainerResourceId: Int,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        commitAllowingStateLoss: Boolean
    ): Boolean {
        if (currentFragment == null || nextFragment == null || isDestroyed()) {
            return false
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

//        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        fragmentTransaction.add(
            fragmentContainerResourceId,
            nextFragment,
            nextFragment.javaClass.simpleName
        )
        fragmentTransaction.addToBackStack(nextFragment.javaClass.simpleName)
        val parentFragment = currentFragment.parentFragment
        fragmentTransaction.hide(parentFragment ?: currentFragment)

        if (!commitAllowingStateLoss) {
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.commitAllowingStateLoss()
        }
        return true
    }


    /**
     * Replaces the Fragment into layout container
     *
     * @param fragmentContainerResourceId Resource id of the layout in which Fragment will be added
     * @param fragmentManager             FRAGMENT MANGER
     * @param nextFragment                New Fragment to be loaded into fragmentContainerResourceId
     * @param requiredAnimation           true if screen transition animation is required
     * @param commitAllowingStateLoss     true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */

    fun replaceFragment(
        fragmentContainerResourceId: Int,
        nextFragment: Fragment?,
        commitAllowingStateLoss: Boolean
    ): Boolean {
        try {
            val backStackCount: Int = supportFragmentManager.backStackEntryCount
            for (i in 0 until backStackCount) {

                // Get the back stack fragment id.
                val backStackId: Int = supportFragmentManager.getBackStackEntryAt(i).getId()
                supportFragmentManager.popBackStack(
                    backStackId,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
            if (nextFragment == null || supportFragmentManager == null) {
                return false
            }

            val fragmentTransaction = supportFragmentManager.beginTransaction()
//            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            fragmentTransaction.replace(
                fragmentContainerResourceId,
                nextFragment,
                nextFragment?.javaClass?.simpleName
            )
            fragmentTransaction.addToBackStack(null)
            if (!commitAllowingStateLoss) {
                fragmentTransaction.commit()
            } else {
                fragmentTransaction.commitAllowingStateLoss()
            }
        } catch (e: Exception) {
            return false
        }

        return true
    }


    suspend fun changeStatusbarcolor(color: Int = Color.TRANSPARENT) {
        withContext(Dispatchers.Main) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color

            trackData.statusBarColor = color
        }

    }

//    fun fragmentonBackPressed(fm: FragmentManager): Boolean {
//        if (fm != null) {
//            setLog(TAG, "fragmentonBackPressed: 1")
//            if (fm.getBackStackEntryCount() > 0) {
//                setLog(TAG, "fragmentonBackPressed: 2")
//                fm.popBackStack()
//                return true
//            }
//            val fragList: List<Fragment> = fm.getFragments()
//            if (fragList != null && fragList.size > 0) {
//                for (frag in fragList) {
//                    if (frag == null) {
//                        continue
//                    }
//                    if (frag.isVisible) {
//                        if (fragmentonBackPressed(frag.childFragmentManager)) {
//                            setLog(TAG, "fragmentonBackPressed: 3")
//                            return true
//                        }
//                    }
//                }
//            }
//        } else {
//            setLog(TAG, "fragmentonBackPressed: null")
//        }
//        return false
//    }


    override fun onBackPressed() {
        setLog("onBackPressed", "BaseActivity-onBackPressed-isFinishing-$isFinishing")
        if (!isFinishing) {
            if (isSwipableActive) {
                if (includeFreeMinute.visibility == View.VISIBLE || songPreviewModel.visibility == View.VISIBLE || newPreviewModel.visibility == View.VISIBLE) {
                    return
                }
            }

            if (getMiniPlayerState() == BottomSheetBehavior.STATE_EXPANDED) {
                toggleSheetBehavior()
            } else if (supportFragmentManager.backStackEntryCount > 0) {
                setLog("onBackPressed", "BaseActivity-onBackPressed-3-backStackEntryCount:${supportFragmentManager.backStackEntryCount}")
                statusBarBg()
                if (supportFragmentManager.backStackEntryCount == 1) {
                    /*val backStackCount: Int = supportFragmentManager.getBackStackEntryCount()
                    for (i in 0 until backStackCount) {

                        // Get the back stack fragment id.
                        val backStackId: Int = supportFragmentManager.getBackStackEntryAt(i).getId()
                        supportFragmentManager.popBackStack(
                            backStackId,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }*/
                    moveTaskToBack(true);
                    //super.onBackPressed()
                } else {
                    supportFragmentManager?.popBackStack()
                }
            } else {
                setLog("onBackPressed", "BaseActivity-onBackPressed-called")
                //moveTaskToBack(true);
                super.onBackPressed();
                //overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
            }
        }

    }

    fun isOnClick(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Constant.MAX_CLICK_INTERVAL) {
            lastClickedTime = SystemClock.elapsedRealtime()
            return false
        }
        lastClickedTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (supportFragmentManager != null && supportFragmentManager?.fragments != null && data != null) {
            for (fragment in supportFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }

        }else if(requestCode == 100){
                val dataMap= java.util.HashMap<String, String>()

                if (resultCode == RESULT_OK)
                    SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Allowed, Default Allowed")
                else
                    SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Allowed, Default Denied")
            }
    }

    open fun onViewCreated(savedInstanceState: Bundle?) {
        // client can override to take any action on view created

    }

    open fun getLayoutResourceId(): Int {
        return R.layout.base_now_playing_footer_layout
    }

    override fun getViewActivity(): AppCompatActivity {
        return this
    }

    override fun getContext(): Context {
        return applicationContext
    }

    override fun onConnectedToService(
        audioPlayerInstance: SimpleExoPlayer,
        nowPlayingQueueInstance: NowPlayingQueue,
        service: AudioPlayerService.AudioServiceBinder
    ) {
        CoroutineScope(Dispatchers.Main).launch{
            setLog("BaseActivityLifecycleMethods", "onConnectedToService")
            audioPlayer = audioPlayerInstance
            currentPlayer=audioPlayer
            nowPlayingQueue = nowPlayingQueueInstance
            shortPlayerControlView.player = audioPlayer
            //audioPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            //changeRepeateModes(audioPlayer?.repeatMode)
            //initialPlayerVolume = audioPlayer?.deviceVolume?.toFloat() ?: 0F
            initialPlayerVolume = audioPlayer?.volume ?: 0F

            audioPlayer?.apply {
                addListener(this@BaseActivity)
                fetchTrackMetadata()
                //addAnalyticsListener(this@BaseActivity)
                getAudioSession()
            }
            audioPlayerService = service.service
            audioPlayerService?.setBaseActivityInstance(this@BaseActivity)
//        if (companionAdSlotFrame != null) {
//            audioPlayerService?.initializeAds(this, companionAdSlotFrame)
//        }
            setLog("BaseActivity", "Lifecycle-onConnectedToService")
        }
    }

    private fun SimpleExoPlayer.fetchTrackMetadata() {
        /*if (currentTag != null) {
            val nowPlayingInfo: NowPlayingInfo = currentTag as NowPlayingInfo
            nowPlayingViewModel.fetchTrackMetadata(nowPlayingInfo.id)
            //nowPlayingViewModel.fetchTrackMetadata(nowPlayingInfo.id, nowPlayingInfo.index)
        }*/
        setLog(
            "SwipablePlayerFragment",
            "BaseActivity-fetchTrackMetadata()-${currentMediaItem?.playbackProperties?.tag}"
        )
        if (currentMediaItem?.playbackProperties?.tag != null) {
            val nowPlayingInfo: NowPlayingInfo =
                currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
            nowPlayingViewModel.fetchTrackMetadata(nowPlayingInfo.id)
            //nowPlayingViewModel.fetchTrackMetadata(nowPlayingInfo.id, nowPlayingInfo.index)
        }
    }

    open fun fetchTrackData(): Track {
        return trackData
    }

    open fun getSongLyricsData(): String {
        return songLyrics
    }

    private fun setBottomSheet(defaultState: Int) {
        bottomSheetBehavior = BottomSheetBehavior.from(nowPlayingBottomSheet)
        bottomSheetBehavior?.isDraggable = false
        if (defaultState == BottomSheetBehavior.STATE_EXPANDED) {
            shortPlayerControlView.alpha = 0f
            hideBottomNavigationBar()
        } else {
            setMiniPlayerState(BottomSheetBehavior.STATE_HIDDEN)
            showBottomNavigationBar()
        }
        bottomNavigationHeight = resources.getDimensionPixelSize(R.dimen.dimen_62).toInt()
        miniplayerHeight = 0
        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, state: Int) {
                setLog(TAG, "onStateChanged $state")

                bottomSheetState = state

                changeMiniPlayerState(state)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset in 0f..1f) {
                    shortPlayerControlView.alpha = (1 - slideOffset)
                    bottomSheetToolbar.alpha = slideOffset
                }
            }

        })

        nowPlayingTitleTextView?.isSelected = true
        nowPlayingSubtitleTextView?.isSelected = true
        titleTextView?.isSelected = true
        subtitleTextView?.isSelected = true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    public fun toggleSheetBehavior() {

        if (getMiniPlayerState() != BottomSheetBehavior.STATE_EXPANDED) {
            val intent = Intent(Constant.AUDIO_MINI_PLAYER_EVENT)
            intent.putExtra(Constant.miniPlayerAction, playerExpand)
            intent.putExtra("EVENT", Constant.AUDIO_MINI_PLAYER_CLICK_RESULT_CODE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }


    override fun showNowPlayingTrackMetadata(track: Track) {
        CoroutineScope(Dispatchers.Main).launch {
            if (HungamaMusicApp.getInstance().activityVisible) {
                addFGTime()
                setLog("countFgTime", "2")
            } else {
                addBGTime()
            }

            //setLog("setSongLyricsData", "setSongLyricsData 1-> track:${track?.songLyricsUrl}")
            //setLog("setSongLyricsData", "setSongLyricsData 1-> track:${track.image}")

            val playType = getPlayerType(track.playerType)
            if (playType == CONTENT_MOVIE || playType == CONTENT_TV_SHOW || playType == CONTENT_MUSIC_VIDEO) {
                trackData = track
                titleTextView.text = track.title
                subtitleTextView.text = track.subTitle
                shortPlayerControlView?.setOnClickListener {
                    if (playType == CONTENT_MUSIC_VIDEO) {
                        closeVideoMiniplayer(false)
                        val bundle = Bundle()
                        bundle.putString(Constant.defaultContentId, track.id.toString())
                        bundle.putString(Constant.defaultContentPlayerType, track.playerType)
                        if (audioPlayer != null && audioPlayer?.currentPosition != null){
                            HungamaMusicApp?.getInstance()?.userStreamList?.put(track.id.toString(),audioPlayer?.currentPosition!!)
                            bundle.putLong(VIDEO_START_POSITION, audioPlayer?.currentPosition!!)
                        }

                        val videoDetailsFragment = MusicVideoDetailsFragment()
                        videoDetailsFragment.arguments = bundle
                        refreshCurrentFragment(this@BaseActivity, videoDetailsFragment)
                    } else {
                        val intent = Intent(applicationContext, VideoPlayerActivity::class.java)
                        val serviceBundle = Bundle()
                        serviceBundle.putString(Constant.SELECTED_CONTENT_ID, track.id.toString())
                        serviceBundle.putParcelableArrayList(Constant.SEASON_LIST, videoSeasonDataList)
                        if (playType == CONTENT_TV_SHOW) {
                            serviceBundle.putInt(Constant.CONTENT_TYPE, CONTENT_TV_SHOW)
                        } else {
                            serviceBundle.putInt(Constant.CONTENT_TYPE, CONTENT_MOVIE)
                        }
                        serviceBundle.putInt(Constant.TYPE_ID, track.playerType!!.toInt())
                        intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                        intent.putExtra("thumbnailImg", track.image)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        serviceBundle.putLong(VIDEO_START_POSITION, audioPlayer?.currentPosition!!)
                        setLocalBroadcastEventCall(null, Constant.VIDEO_PLAYER_EVENT)
                        startActivity(intent)
                        /*startActivityForResult(
                        intent,
                        VIDEO_ACTIVITY_RESULT_CODE
                    )*/

                        //closeVideoMiniplayer()
                    }
                    setMiniPlayerState(BottomSheetBehavior.STATE_HIDDEN)
                }
                if (getMiniPlayerState() != BottomSheetBehavior.STATE_EXPANDED) {
                    setPlayerType(playType)
                    setMiniPlayerState(BottomSheetBehavior.STATE_COLLAPSED)
//            cardImageCorner.alpha = 0f
                }
                setLog("setSongLyricsData", "setSongLyricsData 2-> playType:${playType}")
            }
            else {
                val intent = Intent(Constant.AUDIO_PLAYER_UI_EVENT)
                setLog("setSongLyricsjj","${track.image.toString()}")
                intent.putExtra(Constant.playerArtworkChange, track.image.toString())
                intent.putExtra("EVENT", Constant.AUDIO_PLAYER_UI_RESULT_CODE)
                LocalBroadcastManager.getInstance(this@BaseActivity).sendBroadcast(intent)
                var artwork = ""

                artwork = track.image.toString()
                //currentPlayingMediaItem = exoPlayer.setMediaItem(track)
                initFavoriteMusic(track)
                if (isFirstSong || (trackData != null && trackData.id != track.id)) {
                    isFirstSong = false
                    trackData = track
                    artImageUrl = artwork
                    if (getMiniPlayerState() == BottomSheetBehavior.STATE_EXPANDED) {
                        setArtImageBg(true)
                    } else {
                        setArtImageBg(false)
                    }
                    if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable==true){

                        makeCastPlayer()
                        setAudioCurrentPlayer(BaseFragment.castPlayer!!)

                        setLog(TAG, "setUpChormeCast setAudioCurrentPlayer called BaseFragment.castPlayer:${BaseFragment.castPlayer} isCastPlayerAudio:${BaseFragment.isCastPlayerAudio}")
                    }
                    //callSongLyricsApi(track, lrc_view_full)
                }
                trackData = track
                artImageUrl = artwork
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                CoroutineScope(Dispatchers.Main).launch{
                    titleTextView?.text = track.title
                    subtitleTextView?.text = track.subTitle
                    nowPlayingTitleTextView?.text = track.title
                    nowPlayingSubtitleTextView?.text = track.subTitle
                    shortPlayerControlView?.setOnClickListener { toggleSheetBehavior() }
                    if (audioPlayer != null && audioPlayer?.repeatMode != Player.REPEAT_MODE_ALL){
                        callRecommendedApiOnEndOfSong()
                    }
                }


                setPlayerType(playType)
                // pop up bottom sheet
                if (getMiniPlayerState() != BottomSheetBehavior.STATE_EXPANDED) {
                    setMiniPlayerState(BottomSheetBehavior.STATE_COLLAPSED)
                } else if (getMiniPlayerState() == BottomSheetBehavior.STATE_EXPANDED) {

                }



            }

            if (playItemListener != null) {
                playItemListener?.playItemChange()
            }

            loadAds()
        }
    }

    fun getVideoDurationSeconds(player: SimpleExoPlayer): Int {
        val timeMs = player.contentDuration.toInt()
        return timeMs / 1000
    }

    fun getVideoCurrentDurationSeconds(player: SimpleExoPlayer): Int {
        val timeMs = player.currentPosition.toInt()
        return timeMs / 1000
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        setLog(TAG, "onPlayerStateChanged with $playbackState")
        when (playbackState) {
            PlaybackState.STATE_PLAYING -> {
                try {
                    if (audioPlayer != null) {
                        setLog(
                            "playerStates",
                            "BaseActivity-onPlayerStateChanged()-STATE_PLAYING-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} ${trackData?.title}"
                        )
                        var eventModel =
                            HungamaMusicApp.getInstance().getEventData("" + trackData?.id)

                        eventModel?.duration = "" + getVideoDurationSeconds(audioPlayer!!)

                        HungamaMusicApp?.getInstance()?.setEventData("" + trackData?.id, eventModel!!)
                    }

                    audioPlayerActionBroadcast(playing)
                    if (isAudioAdPlaying) {
                        pausePlayer()
                    }

                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                        setLog("EventCheck", "Stream" + "1188")
                       // callStreamEventAnalytics(songDataList?.get(nowPlayingCurrentIndex())!!, EventType.STREAM_START)
                        if(HungamaMusicApp.getInstance().getIsFirstLaunchSong()){
                            setLog("IsFirstTimeLaunch","Launch${HungamaMusicApp.getInstance().getIsFirstLaunchSong()}")
                        }else{
                            callStreamEventAnalytics(songDataList?.get(nowPlayingCurrentIndex())!!, EventType.STREAM_START)
                        }

                    }
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }

            }
            PlaybackState.STATE_PAUSED -> {
                try {
                    setLog("Player_state", "0")
                    if (audioPlayer != null) {
                        setLog(
                            "playerStates",
                            "BaseActivity-onPlayerStateChanged()-STATE_PAUSED-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                        )

                        var eventModel =
                            HungamaMusicApp.getInstance().getEventData("" + trackData?.id)

                        eventModel?.duration_fg = "" + getVideoCurrentDurationSeconds(audioPlayer!!)

                        HungamaMusicApp?.getInstance()?.setEventData("" + trackData?.id!!, eventModel!!)
                    }


                    audioPlayerActionBroadcast(pause)
                    setLog(
                        "callUserStreamUpdate1",
                        "BaseActivity-onPlayerStateChanged()-STATE_PAUSED contentDuration:${audioPlayer?.contentDuration} id:${trackData?.id} title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                    )

                    //callUserStreamUpdate(STREAM_POSITION_CURRENT)
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }
            }
            PlaybackState.STATE_NONE -> {
                try {
                    setLog("Player_state", "1")
                    if (audioPlayer != null) {
                        setLog(
                            "playerStates",
                            "BaseActivity-onPlayerStateChanged()-STATE_NONE-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                        )
                        //ivTrackImage.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }

            }
            PlaybackState.STATE_FAST_FORWARDING -> {
                try {
                    setLog("Player_state", "3")
                    if (audioPlayer != null) {
                        setLog(
                            "playerStates",
                            "BaseActivity-onPlayerStateChanged()-STATE_FAST_FORWARDING-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                        )
                        //ivTrackImage.visibility = View.GONE
                    }

                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                        if (songDataList?.get(nowPlayingCurrentIndex())?.contentType == 110 || songDataList?.get(
                                nowPlayingCurrentIndex()
                            )?.contentType == 109
                        ) {
                            callForwardEvent()
                        }
                    }
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }


            }
            PlaybackState.STATE_REWINDING -> {
                try {
                    setLog("Player_state", "4")
                    setLog(
                        "playerStates",
                        "BaseActivity-onPlayerStateChanged()-STATE_REWINDING-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                    )
                    //ivTrackImage.visibility = View.GONE
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                        if (songDataList?.get(nowPlayingCurrentIndex())?.contentType == 110 || songDataList?.get(
                                nowPlayingCurrentIndex()
                            )?.contentType == 109
                        ) {
                            callBackwardEvent()
                        }
                    }
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }

            }
            PlaybackState.STATE_STOPPED -> {
                try {
                    if (audioPlayer != null) {
                        setLog(
                            "playerStates",
                            "BaseActivity-onPlayerStateChanged()-STATE_STOPPED-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                        )

                        var eventModel =
                            HungamaMusicApp.getInstance().getEventData("" + trackData?.id)

                        if (HungamaMusicApp.getInstance().activityVisible!!) {
                            eventModel?.duration_fg =
                                "" + getVideoCurrentDurationSeconds(audioPlayer!!)
                        } else {
                            eventModel?.duration_bg =
                                "" + getVideoCurrentDurationSeconds(audioPlayer!!)
                        }


                        HungamaMusicApp?.getInstance()?.setEventData("" + trackData?.id!!, eventModel!!)
                    }
                    setLog("callUserStreamUpdate1", "onPlayerStateChanged")
//                callUserStreamUpdate(STREAM_POSITION_CURRENT)
                    //callUserStreamUpdate(STREAM_POSITION_PREVIOUS)
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }

            }
            PlaybackState.STATE_ERROR -> {
                try {
                    if (audioPlayer != null) {
                        setLog(
                            "playerStates",
                            "BaseActivity-onPlayerStateChanged()-STATE_ERROR-$playbackState title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} trackData?.title ${trackData?.title}"
                        )
                    }
                    setMiniPlayerState(BottomSheetBehavior.STATE_HIDDEN)
                    showBottomNavigationBar()
                    setLog("callUserStreamUpdate1", "onPlayerStateChanged")
                    callUserStreamUpdate(STREAM_POSITION_CURRENT)
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }

            }
            else -> {
                try {
                    setLog("Player_state", "2")
                    setLog("Player_state-2", playbackState.toString())
                    setLog(
                        "playerStates",
                        "BaseActivity-onPlayerStateChanged()-ELSE-$playbackState "
                    )
                } catch (e: Exception) {
                    setLog(TAG, "onPlayerStateChanged with $playbackState - error: ${e.message}")
                }

            }
        }
    }

    private fun callForwardEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            setLog("callForwardEvent","callForwardEvent called")
            val hashMap = HashMap<String, String>()
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                hashMap.put(
                    EventConstant.CONTENTTYPESTREAMING_EPROPERTY,
                    "" + songDataList?.get(nowPlayingCurrentIndex())?.title!!
                )
                if (getMiniPlayerState() == BottomSheetBehavior.STATE_EXPANDED) {
                    hashMap.put(
                        EventConstant.PLAYERTYPE_EPROPERTY,
                        EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER
                    )
                } else if (getMiniPlayerState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    hashMap.put(
                        EventConstant.PLAYERTYPE_EPROPERTY,
                        EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER
                    )
                } else {
                    hashMap.put(
                        EventConstant.PLAYERTYPE_EPROPERTY,
                        EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER
                    )
                }

                EventManager.getInstance().sendEvent(AudioPlayerSkipForwardEvent(hashMap))
            }
        }



    }

    private fun callBackwardEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            setLog("callForwardEvent","callForwardEvent called")
            val hashMap = HashMap<String, String>()
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                hashMap.put(
                    EventConstant.CONTENTTYPESTREAMING_EPROPERTY,
                    "" + songDataList?.get(nowPlayingCurrentIndex())?.title!!
                )
                if (getMiniPlayerState() == BottomSheetBehavior.STATE_EXPANDED) {
                    hashMap.put(
                        EventConstant.PLAYERTYPE_EPROPERTY,
                        EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER
                    )
                } else if (getMiniPlayerState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    hashMap.put(
                        EventConstant.PLAYERTYPE_EPROPERTY,
                        EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER
                    )
                } else {
                    hashMap.put(
                        EventConstant.PLAYERTYPE_EPROPERTY,
                        EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER
                    )
                }

                EventManager.getInstance().sendEvent(AudioPlayerSkipBackwardEvent(hashMap))
            }
        }



    }

    // gets called when current track playback completes and playback of next track starts
    override fun onPositionDiscontinuity(reason: Int) {
        setLog("prepareNextSong", "BaseActivity-onPositionDiscontinuity-reason-$reason")
        audioPlayer?.fetchTrackMetadata()
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION || reason == Player.DISCONTINUITY_REASON_SKIP) {
            setLog("prepareNextSong", "true - nowPlayingCurrentIndex-" + nowPlayingCurrentIndex())
            setLog(
                "prepareNextSong",
                "true - audioPlayer?.currentWindowIndex - " + audioPlayer?.currentWindowIndex
            )
            if (nowPlayingCurrentIndex() != audioPlayer?.currentWindowIndex
                && nowPlayingCurrentIndex() < audioPlayer?.currentWindowIndex!!
            ) {
                val nextIndex = nowPlayingCurrentIndex() + 1
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nextIndex) {
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                        setLog(
                            "callUserStreamUpdate1",
                            "onPositionDiscontinuity-playNextSong title:${
                                songDataList?.get(
                                    nowPlayingCurrentIndex()
                                )?.title
                            }"
                        )
                        callUserStreamUpdate(-1, songDataList?.get(nowPlayingCurrentIndex()), nowPlayingCurrentIndex(), true)
                    }
                    if (!TextUtils.isEmpty(songDataList?.get(nextIndex)?.url)) {
                        if (!CommonUtils.isFilePath(
                                songDataList?.get(nextIndex)?.url.toString()
                            )){
                            setLog(
                                "prepareNextSong",
                                "true - updateNowPlayingCurrentIndex(nextIndex) - " + nextIndex
                            )
                            updateNowPlayingCurrentIndex(nextIndex)
                            callStreamEventAnalytics(songDataList.get(nowPlayingCurrentIndex()), EventType.STREAM_START)
                            audioPlayer?.fetchTrackMetadata()
                            setLog("preCatchApiCall", "BaseActitvity-onPositionDiscontinuity")
                            preCatchApiCall(songDataList, nextIndex, false)
                        }else{
                            isNextSongInProgress = false
                            playNextSong(false)
                        }

                        if (isSleepTimerSetToEndOfCurrentPlay) {
                            setLog(
                                "sleepTimer",
                                "onPositionDiscontinuity-called from-playNextSong()"
                            )
                            onSleepTimeOver()
                        }
                    } else {
                        setLog("prepareNextSong", "false - playNextSong(false)")
                        setLog("prepareNextSong", "onPositionDiscontinuity - isNextSongInProgress-$isNextSongInProgress")
                        try {
                            val isOfflinePlay =
                                isContentDownloaded(songDataList, nowPlayingCurrentIndex())
                            if (isOfflinePlay){
                                isNextSongInProgress = false
                            }
                        }catch (e:Exception){
                            isNextSongInProgress = false
                        }

                        playNextSong(false)
                        setLog("prepareNextSong", "onPositionDiscontinuity - isNextSongInProgress-$isNextSongInProgress")
                        isNextSongInProgress = true
                    }
                }
            }
            setLog("prepareNextSong 2", nowPlayingCurrentIndex().toString())
            audioPlayerActionBroadcast(getAudioPlayerPlayingStatus())
        }
        else if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            setLog(
                "prepareNextSong",
                "BaseActivity-onPositionDiscontinuity-reason-$reason - Seek bar seek"
            )
            if (audioPlayer != null && audioPlayer?.duration != null && audioPlayer?.currentPosition != null) {
                setLog(
                    "prepareNextSong",
                    "BaseActivity-onPositionDiscontinuity-reason-$reason - duration-${audioPlayer?.duration} - currentPotision-${audioPlayer?.currentPosition}"
                )
                if(audioPlayer?.currentPosition in (audioPlayer?.duration!!-100)..audioPlayer?.duration!!){
                    setLog(
                        "prepareNextSong",
                        "BaseActivity-onPositionDiscontinuity-(audioPlayer?.duration!!-10)-${(audioPlayer?.duration!!-100)}")
                    val position = audioPlayer?.currentPosition!! - 3000
                    setLog(
                        "prepareNextSong",
                        "BaseActivity-onPositionDiscontinuity-reason-$reason - position-$position"
                    )
                    if (position > 0) {
                        audioPlayer?.seekTo(position)
                        if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable==true){
                            BaseFragment.castPlayer?.seekTo(position)
                        }
                    }
                }
            }
        } else {
            setLog("prepareNextSong", "false")
        }

        /*when (reason) {
            Player.DISCONTINUITY_REASON_PERIOD_TRANSITION -> {
                setLog("Gapless", "DISCONTINUITY_REASON_PERIOD_TRANSITION")
                // Get new track duration
                val trackDuration: Long = audioPlayer?.duration ?:0L
                setLog("Gapless", volumeDecreaseOperationDone.toString())
                if (volumeDecreaseOperationDone) {
                    volumeDecreaseOperationDone = false
                    setLog("Gapless", "trackDuration-"+trackDuration.toString())
                    // Check track duration whether it is longer than 1000 ms or not before sending message
                    if (trackDuration > 1000) {
                        audioPlayer?.createMessage { messageType, payload ->
                            setLog("Gapless", "Audio starting in 1 seconds")
                            setIncreasPlayerVolume(messageType, payload)
                        }?.setLooper(mainLooper)
                            ?.setPosition(1000L) // in milliseconds
                            ?.setPayload(arrayOf(20, 20))
                            ?.setHandler(Handler()) // A shared handler may also be used
                            ?.send()
                    }

                    // Check track duration whether it is longer than 2000 ms or not before sending message
                    if (trackDuration > 2000) {
                        audioPlayer?.createMessage { messageType, payload ->
                            setLog("Gapless", "Audio starting in 2 seconds")
                            setIncreasPlayerVolume(messageType, payload)
                        }?.setLooper(mainLooper)
                            ?.setPosition(2000L) // in milliseconds
                            ?.setPayload(arrayOf(20, 40))
                            ?.setHandler(Handler()) // A shared handler may also be used
                            ?.send()
                    }

                    // Check track duration whether it is longer than 3000 ms or not before sending message
                    if (trackDuration > 3000) {
                        audioPlayer?.createMessage { messageType, payload ->
                            setLog("Gapless", "Audio starting in 3 seconds")
                            setIncreasPlayerVolume(messageType, payload)
                        }?.setLooper(mainLooper)
                            ?.setPosition(3000L) // in milliseconds
                            ?.setPayload(arrayOf(20, 60))
                            ?.setHandler(Handler()) // A shared handler may also be used
                            ?.send()
                    }

                    // Check track duration whether it is longer than 4000 ms or not before sending message
                    if (trackDuration > 4000) {
                        audioPlayer?.createMessage { messageType, payload ->
                            setLog("Gapless", "Audio starting in 4 seconds")
                            setIncreasPlayerVolume(messageType, payload)
                        }?.setLooper(mainLooper)
                            ?.setPosition(4000L) // in milliseconds
                            ?.setPayload(arrayOf(20, 80))
                            ?.setHandler(Handler()) // A shared handler may also be used
                            ?.send()
                    }

                    // Check track duration whether it is longer than 5000 ms or not before sending message
                    if (trackDuration > 5000) {
                        audioPlayer?.createMessage { messageType, payload ->
                            setLog("Gapless", "Audio starting in 5 seconds")
                            setIncreasPlayerVolume(messageType, payload)
                        }?.setLooper(mainLooper)
                            ?.setPosition(5000L) // in milliseconds
                            ?.setPayload(arrayOf(20, 100))
                            ?.setHandler(Handler()) // A shared handler may also be used
                            ?.send()
                    }
                }
            }
            Player.DISCONTINUITY_REASON_SEEK -> {
            }
            Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
            }
            Player.DISCONTINUITY_REASON_AD_INSERTION -> {
            }
            Player.DISCONTINUITY_REASON_INTERNAL -> {
            }
        }*/
    }

    override fun displayError(error: String) {
        Snackbar.make(rootLayout, error, Snackbar.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        if (outState != null) {
            outState.putInt(KEY_BOTTOM_SHEET_STATE, bottomSheetState)
        }
        super.onSaveInstanceState(outState)

    }


    open fun pausePlayer() {
        if (audioPlayer != null) {
            audioPlayer?.pause()
        }

        if (BaseFragment.castPlayer != null && BaseFragment.castPlayer?.isCastSessionAvailable == true) {
            BaseFragment.castPlayer?.pause()
        }
    }

    open fun resumePlayer() {
        if (audioPlayer != null) {
            audioPlayer?.play()
        }
        if (BaseFragment.castPlayer != null && BaseFragment.castPlayer?.isCastSessionAvailable == true) {
            BaseFragment.castPlayer?.play()
        }
    }

    open fun stopAudioPlayer() {
        try {
            removePlayerDurationCallback()
            removeDurationCallback()
            setLog(
                "callUserStreamUpdate1",
                "stopAudioPlayer duration:${audioPlayer?.contentDuration} boundToService:${boundToService}"
            )
            /*if (!songDataList.isNullOrEmpty()){
                callUserStreamUpdate(STREAM_POSITION_CURRENT)
            }*/
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                setLog(
                    "callUserStreamUpdate1",
                    "stopAudioPlayer- title:${songDataList?.get(nowPlayingCurrentIndex())?.title}"
                )
                callUserStreamUpdate(-1, songDataList?.get(nowPlayingCurrentIndex()), nowPlayingCurrentIndex())
            }
/*        if (boundToService) {
            //pausePlayer()
            *//*playerControlView.player = null
            shortPlayerControlView.player = null
            player_view.player = null
            audioPlayer?.removeListener(this)*//*
            audioPlayerService?.stopAudiPlayerService()
        }*/
            audioPlayerService?.stopAudiPlayerService()
        }catch (e:Exception){

        }
    }

    fun playPlayer() {
        if (player11?.isPlaying == true) return
        audioPlayer?.play()
        if (BaseFragment.castPlayer != null && BaseFragment.castPlayer?.isCastSessionAvailable == true) {
            BaseFragment.castPlayer?.play()
        }
    }

    open fun getAudioPlayerInstance(): SimpleExoPlayer? {
        return audioPlayer
    }

    fun callSongLyricsApi(track: Track, lrcViewFull: LrcView) {
        CoroutineScope(Dispatchers.Main).launch{
            //val isOfflineLyrics = isContentLyricsDownloaded(songDataList, nowPlayingCurrentIndex())
            /*if (isOfflineLyrics) {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                    songDataList?.get(nowPlayingCurrentIndex())!!.id!!.toString())
                setSongLyricsData(downloadedAudio?.lyricsFilePath)
            } else {*/
            var songLyricsUrl = track.songLyricsUrl
            //songLyricsUrl = "https://images.hungama.com/c/9/bab/42f/82165267/82165267_lyrics-eng.lrc"
            CommonUtils.setLog(
                "isGotoDownloadClicked",
                "BaseActivity-callSongLyricsApi-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
            )
            if (lrcViewFull != null && songLyricsUrl != null && !TextUtils.isEmpty(songLyricsUrl) && ConnectionUtil(this@BaseActivity).isOnline(
                    false
                )
            ) {
                setLog("setSongLyricsData", "setSongLyricsData 4-> getSongLyricsList:${songLyricsUrl}")
                musicViewModel?.getSongLyricsList(this@BaseActivity, songLyricsUrl)?.observe(this@BaseActivity,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                if (it?.data != null) {
                                    songLyrics = it?.data
                                }
                                setSongLyricsData(it?.data, lrcViewFull)
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING -> {

                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR -> {

                            }
                        }
                    })
            } else {
                songLyrics = ""
                // moreBottomsheet.visibility = View.GONE
                setSongLyricsData("", lrcViewFull)
            }
            //}
        }
    }


    var lrcView:LrcView? = null
    fun setSongLyricsData(lrc: String?, lrcViewFull: LrcView?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                lrcView = lrcViewFull
                if (lrcView != null){
                    try {
                        val userSettingRespModel = SharedPrefHelper.getInstance()
                            .getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
                        var isLyricsAllow = true
                        if (userSettingRespModel != null && userSettingRespModel?.data != null && !userSettingRespModel?.data?.data?.get(
                                0
                            )?.preference.isNullOrEmpty()
                        ) {
                            isLyricsAllow =
                                userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.show_lyrics!!
                        }
                        removeLrcRunnableCallback()
                        setLog(TAG, "setSongLyricsData: isLyricsAllow:${isLyricsAllow}")
                        if (!TextUtils.isEmpty(lrc) && isLyricsAllow) {
                            setLog(TAG, "setSongLyricsData: lrc:${lrc}")
                            setLog(TAG, "setSongLyricsData: lrcViewFull:${lrcView}")
                            hasLyrics = true

                            lrcView?.visibility = View.VISIBLE
                            val typeface = ResourcesCompat.getFont(
                                getContext(),
                                R.font.sf_pro_text_semibold
                            )
                            lrcView?.setTypeFace(typeface)
                            lrcView?.setEnableShowIndicator(false)
                            //val isOfflineLyrics = isContentLyricsDownloaded(songDataList, nowPlayingCurrentIndex())
                            var lrcss: List<Lrc>? = null
                            //if (isOfflineLyrics) {
                            //    lrcss = LrcHelper.parseLrcFromFile(File(lrc))
                            //}else{
                            lrcss = LrcHelper.parseLrcFromUri(lrc)
                            //}
                            //lrcss = LrcHelper.parseLrcFromAssets(getContext(), "wafa_lrc.lrc")
                            lrcView?.setLrcData(lrcss)
                            /*lrcViewFull?.setOnPlayIndicatorLineListener { time: Long, content: String ->
                                audioPlayer?.seekTo(time)
                                if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable==true){
                                    BaseFragment.castPlayer?.seekTo(time)
                                }
                            }*/
                            lrcView?.setOnTouchListener { v, event ->
                                setLog("lrcTouch", "True")
                                lrcView?.onTouchEvent(event)
                                val view: ViewParent? =
                                    lrcView?.rootView?.findViewById(R.id.view_pager_stories)
                                setLog(
                                    "SwipablePlayerViewFragment",
                                    "SwipablePlayerViewFragment-setLyrics-lrc_view_full.getParent()-" + view
                                )
                                if (view != null && view is ViewPager2) {
                                    setLog(
                                        "SwipablePlayerViewFragment",
                                        "SwipablePlayerViewFragment-setLyrics-lrc_view_full.getParent()2-" + view
                                    )
                                    view.isUserInputEnabled = event.action == MotionEvent.ACTION_UP
                                    setLog(
                                        "SwipablePlayerViewFragment",
                                        "SwipablePlayerViewFragment-setLyrics-lrc_view_full.getParent()3-" + view.isUserInputEnabled
                                    )
                                }
                                return@setOnTouchListener true
                            }
                            startLrcRunnableCallback()
                        } else {
                            setLog(TAG, "setSongLyricsData: No lyrics")

                            hasLyrics = false
                            if (lrcView != null){
                                lrcView?.resetView("No lyrics.")
                                lrcView?.visibility = View.INVISIBLE
                            }

                            /*if (!songDataList.isNullOrEmpty()) {
                                setLog(
                                    "setSongLyricsData",
                                    "setSongLyricsData-songDataListSize-${songDataList?.size}"
                                )
                                setLog(
                                    "setSongLyricsData",
                                    "setSongLyricsData-nowPlayingCurrentIndex-" + nowPlayingCurrentIndex()
                                )
                            }*/
                        }
                    } catch (e: Exception) {
                        setLog(TAG, "setSongLyricsData: Exception:${e.printStackTrace()}")
                    }
                }
            }catch (e:Exception){
                setLog(TAG, "setSongLyricsData: error:${e.printStackTrace()}")
            }
        }

    }
    //var mRunnableCount = 0
    private val mRunnable = object : Runnable {
        override fun run() {
            val currentPosition = audioPlayer?.currentPosition
            if (currentPosition != null && lrcView != null) {
                lrcView?.updateTime(currentPosition)
            }
            //mRunnableCount++
            //setLog("BaseActivity", "mRunnable-call-$mRunnableCount")
            if (hasLyrics && isNewSwipablePlayerOpen) {
                mHandler?.postDelayed(this, 1500)
            }
        }
    }

    private fun startLrcRunnableCallback() {
        if (hasLyrics && isNewSwipablePlayerOpen) {
            if (mHandler != null) {
                mHandler?.removeCallbacks(mRunnable)
                mHandler?.post(mRunnable)
            } else {
                mHandler = Handler(Looper.getMainLooper())
                mHandler?.post(mRunnable)
                //mRunnableCount = 0
            }
        } else {
            removeLrcRunnableCallback()
        }
    }

    private fun removeLrcRunnableCallback(){
        if (mHandler != null){
            mHandler?.removeCallbacks(mRunnable)
            //mRunnableCount = 0
        }
    }

    fun changeHomeBg(status: Boolean) {
        if (status) {
            fl_container.visibility = View.GONE
            if (!TextUtils.isEmpty(artImageUrl)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        changeStatusbarcolor(statusBarColor)
                    }
            }

        } else {
            fl_container.visibility = View.VISIBLE
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    changeStatusbarcolor(statusBarLastColor)
                }

            } catch (e: Exception) {
            }
        }
    }


    suspend fun setArtImageBg(status: Boolean) {

        if (!TextUtils.isEmpty(artImageUrl)) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
            val gradient: Drawable? =
                ContextCompat.getDrawable(getContext(), R.drawable.audio_player_gradient_drawable)
            val result: Deferred<Bitmap?> = CoroutineScope(Dispatchers.IO).async(Dispatchers.IO) {
                try {
                    setLog(
                        "ImageArtworkDW",
                        "BaseActivity-setArtImageBg-artImageUrl-1-$artImageUrl"
                    )
                    val urlImage = URL(artImageUrl)
                    setLog("ImageArtworkDW", "BaseActivity-setArtImageBg-urlImage-2-$urlImage")
                    urlImage.toBitmap()
                } catch (e: Exception) {
                    setLog(
                        "ImageArtworkDW",
                        "BaseActivity-setArtImageBg-artImageUrl-3-$artImageUrl"
                    )
                    if (artImageUrl.toString().contains(".thbn")) {
                        val contentArtworkFileName =
                            Constant.filePrefix + artImageUrl
                        setLog(
                            "ImageArtworkDW",
                            "BaseActivity-setArtImageBg-contentArtworkFileName-4-$contentArtworkFileName"
                        )
                        val urlImage = URL(contentArtworkFileName)
                        setLog("ImageArtworkDW", "BaseActivity-setArtImageBg-urlImage-5-$urlImage")
                        urlImage.toBitmap()
                    } else {
                        setLog(
                            "ImageArtworkDW",
                            "BaseActivity-setArtImageBg-artImageUrl-6-$artImageUrl"
                        )
                        null
                    }
                }

            }


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // get the downloaded bitmap
                    val bitmap: Bitmap? = result.await()
                    if (bitmap != null){
                        val artImage = BitmapDrawable(resources, bitmap)
                        statusBarColor = CommonUtils.calculateAverageColor(bitmap, 1)
                    }
                    //val alfaBitmat = CommonUtils.adjustOpacity(bitmap!!, 125)

                    CoroutineScope(Dispatchers.IO).launch {
                        if (getMiniPlayerState() == BottomSheetBehavior.STATE_COLLAPSED || getMiniPlayerState() == BottomSheetBehavior.STATE_HIDDEN) {
                            changeStatusbarcolor(statusBarLastColor)
                        } else {
                            changeStatusbarcolor(statusBarColor)
                        }
                    }

                } catch (exp: Exception) {
                    exp.printStackTrace()
                }


            }
        }

    }

    fun statusBarBg() {
        Utils.setWindowProperty(this@BaseActivity)
    }

    fun hideSystemNavigationBar() {
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    private fun setPlayerType(playType: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (getAudioPlayerPlayingStatus() == playing) {
                playAudio()
            } else {
                pauseAudio()
            }

            if (playType == CONTENT_PODCAST) {
                //mini player
                llMiniExoNext?.visibility = View.GONE
                /*-------------------------------------------------------*/

                /*Video player*/
                //mini player
                player_view?.visibility = View.GONE
                llMiniClose?.visibility = View.GONE
                btn_play_mini?.visibility = View.GONE
                btn_pause_mini?.visibility = View.GONE
                /*-------------------------------------------------------*/

                //mini player
                ivTrackImage?.visibility = View.VISIBLE
                llMiniImgFwd?.visibility = View.VISIBLE
                /*-------------------------------------------------------*/

                if (img_fwd_mini != null) {
                    img_fwd_mini?.alpha = 1f
                    img_fwd_mini?.requestFocus()
                    img_fwd_mini?.setOnClickListener {
                        setForwardDuration()
                    }
                }

            } else if (playType == CONTENT_RADIO) {
                //mini player
                ivTrackImage?.visibility = View.VISIBLE
                llMiniExoNext?.visibility = View.GONE
                /*-------------------------------------------------------*/

                /*Video player*/
                //mini player
                player_view?.visibility = View.GONE
                llMiniClose?.visibility = View.GONE
                btn_play_mini?.visibility = View.GONE
                btn_pause_mini?.visibility = View.GONE
                /*-------------------------------------------------------*/
                //mini player
                llMiniImgFwd?.visibility = View.GONE
                /*-------------------------------------------------------*/

            } else if (playType == CONTENT_MOOD_RADIO) {
                //mini player
                ivTrackImage?.visibility = View.VISIBLE
                llMiniExoNext?.visibility = View.VISIBLE
                btn_next_play_mini?.setOnClickListener(this@BaseActivity)
                /*-------------------------------------------------------*/

                /*Video player*/
                //mini player
                player_view?.visibility = View.GONE
                llMiniClose?.visibility = View.GONE
                btn_play_mini?.visibility = View.GONE
                btn_pause_mini?.visibility = View.GONE
                /*-------------------------------------------------------*/

                //mini player
                llMiniImgFwd?.visibility = View.GONE
                /*-------------------------------------------------------*/

            } else if (playType == CONTENT_ON_DEMAND_RADIO) {

                //mini player
                ivTrackImage?.visibility = View.VISIBLE
                llMiniExoNext?.visibility = View.VISIBLE
                btn_next_play_mini?.setOnClickListener(this@BaseActivity)
                /*-------------------------------------------------------*/

                /*Video player*/
                //mini player
                player_view?.visibility = View.GONE
                llMiniClose?.visibility = View.GONE
                btn_play_mini?.visibility = View.GONE
                btn_pause_mini?.visibility = View.GONE
                /*-------------------------------------------------------*/

                //mini player
                llMiniImgFwd?.visibility = View.GONE
                /*-------------------------------------------------------*/

            } else if (playType == CONTENT_MOVIE || playType == CONTENT_TV_SHOW || playType == CONTENT_MUSIC_VIDEO) {

                //mini player
                llMiniImgFwd?.visibility = View.GONE
                /*-------------------------------------------------------*/

                //mini player
                ivTrackImage?.visibility = View.GONE
                llMiniExoNext?.visibility = View.GONE
                /*-------------------------------------------------------*/

                /*Video player*/
                //mini player
                player_view?.visibility = View.VISIBLE
                llMiniClose?.visibility = View.VISIBLE
                if (getAudioPlayerPlayingStatus() == playing){
                    btn_play_mini?.visibility = View.GONE
                    btn_pause_mini?.visibility = View.VISIBLE
                }else{
                    btn_play_mini?.visibility = View.VISIBLE
                    btn_pause_mini?.visibility = View.GONE
                }
                btn_play_mini?.setImageDrawable(
                    getContext().faDrawable(
                        R.string.icon_play,
                        R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_18).toFloat()
                    )
                )
                btn_pause_mini?.setImageDrawable(
                    getContext().faDrawable(
                            R.string.icon_pause_2,
                            R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_18).toFloat()
                        )
                    )
                img_close?.setImageDrawable(
                    getContext().faDrawable(
                        R.string.icon_delete,
                        R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_18).toFloat()
                    )
                )
                btn_play_mini.setOnClickListener(this@BaseActivity)
                btn_pause_mini.setOnClickListener(this@BaseActivity)


                /*-------------------------------------------------------*/

                if (audioPlayer != null) {
                    player_view?.player = audioPlayer
                }
                llMiniClose?.setOnClickListener {
                    closeVideoMiniplayer()
                }
            } else {

                //mini player
                llMiniImgFwd?.visibility = View.GONE
                /*-------------------------------------------------------*/

                /*Video player*/
                //mini player
                player_view?.visibility = View.GONE
                llMiniClose?.visibility = View.GONE
                btn_play_mini?.visibility = View.GONE
                btn_pause_mini?.visibility = View.GONE
                /*-------------------------------------------------------*/

                //mini player
                ivTrackImage?.visibility = View.VISIBLE
                llMiniExoNext?.visibility = View.VISIBLE
                btn_next_play_mini?.setOnClickListener(this@BaseActivity)
                /*-------------------------------------------------------*/
            }
            setEnableDisableNextPreviousIcons()
        }
    }

    fun setEnableDisableNextPreviousIcons() {
        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex() + 1) {
            btn_next_play_mini?.isEnabled = true
            btn_next_play_mini?.setColorFilter(
                ContextCompat.getColor(this, R.color.colorWhite),
                PorterDuff.Mode.SRC_ATOP
            )
        } else {
            btn_next_play_mini?.isEnabled = false
            btn_next_play_mini?.setColorFilter(
                ContextCompat.getColor(this, R.color.disable_icons),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    fun getPlayerType(playerType: String?): Int {
        if (playerType.equals(PLAYER_PODCAST_AUDIO_ALBUM) || playerType.equals(
                PLAYER_PODCAST_AUDIO_TRACK
            ) || playerType.equals(PLAYER_PODCAST_AUDIO_TRACK2)
        ) {
            currentPlayerType = CONTENT_PODCAST
            return CONTENT_PODCAST
        } else if (playerType.equals(PLAYER_RADIO) || playerType.equals(PLAYER_LIVE_RADIO)|| playerType.equals(Constant.PLAYER_ON_LIVE_RADIO)) {
            currentPlayerType = CONTENT_RADIO
            return CONTENT_RADIO
        } else if (playerType.equals(PLAYER_MOOD_RADIO)) {
            currentPlayerType = CONTENT_MOOD_RADIO
            return CONTENT_MOOD_RADIO
        } else if (playerType.equals(PLAYER_ON_DEMAND_RADIO) || playerType.equals(Constant.PLAYER_ARTIST_RADIO)) {
            currentPlayerType = CONTENT_ON_DEMAND_RADIO
            return CONTENT_ON_DEMAND_RADIO
        } else if (playerType.equals(VIDEO_MOVIE) || playerType.equals(VIDEO_HD_MOVIE) || playerType.equals(
                VIDEO_SD_MOVIE
            ) || playerType.equals(
                VIDEO_SHORT_FILMS
            )
        ) {
            currentPlayerType = CONTENT_MOVIE
            return CONTENT_MOVIE
        } else if (playerType.equals(TV_SERIES) || playerType.equals(TV_SERIES_SEASON) || playerType.equals(
                TV_SERIES_EPISODE
            ) || playerType.equals(TV_FASHION_TV) || playerType.equals(TV_LINEAR_TV_CHANNEL_ALBUM) || playerType.equals(
                TV_LINEAR_TV_CHANNEL
            )
        ) {
            currentPlayerType = CONTENT_TV_SHOW
            return CONTENT_TV_SHOW
        } else if (playerType.equals(VIDEO_MUSIC_VIDEO_TRACK) || playerType.equals(
                VIDEO_EVENTS_BROADCAST_VIDEO
            )
            || playerType.equals(VIDEO_MUSIC_VIDEO_PLAYLIST)
        ) {
            currentPlayerType = CONTENT_MUSIC_VIDEO
            return CONTENT_MUSIC_VIDEO
        } else {
            currentPlayerType = CONTENT_MUSIC
            return CONTENT_MUSIC
        }
    }

    fun closeVideoMiniplayer(isLastContentPlay:Boolean = true) {
        setMiniPlayerState(BottomSheetBehavior.STATE_HIDDEN)
        stopAudioPlayer()
        if (isLastContentPlay && !songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
            playLastPlayedAudio(nowPlayingCurrentIndex())
        }

    }

    private fun playLastPlayedAudio(lastPlaySongIndex: Int) {
        try {
            setLog(
                "lastPlayedSong",
                "BaseActivity-playLastPlayedAudio-music content-lastPlaySongIndex-$lastPlaySongIndex"
            )
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > lastPlaySongIndex) {
                if (CommonUtils.isSongOrPodcastContent(songDataList?.get(lastPlaySongIndex)!!)) {
                    updateNowPlayingCurrentIndex(lastPlaySongIndex)
                    if (lastPlaySongIndex > 0) {
                        updateNowPlayingCurrentIndex(lastPlaySongIndex)
                    } else {
                        updateNowPlayingCurrentIndex(0)
                    }
                    val isPause = SharedPrefHelper.getInstance().getLastAudioContentPlayingStatus()
                    isLastContentPlayAfterMiniVideoPlayer = true
                    playCurrentSong(isPause)
                    setLog(
                        "lastPlayedSong",
                        "BaseActivity-playLastPlayedAudio-music content-lastPlaySongIndex-$lastPlaySongIndex title-${
                            songDataList?.get(lastPlaySongIndex)?.title
                        }"
                    )
                } else {
                    CoroutineScope(Dispatchers.Main).launch{
                        withContext(Dispatchers.Default) {
                            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > lastPlaySongIndex) {
                                setLog(
                                    "lastPlayedSong",
                                    "BaseActivity-playLastPlayedAudio-video content-lastPlaySongIndex-$lastPlaySongIndex title-${
                                        songDataList?.get(lastPlaySongIndex)?.title
                                    }"
                                )
                                AppDatabase.getInstance()?.trackDao()?.deleteSong(
                                    songDataList?.get(lastPlaySongIndex)?.id!!
                                )
                            }
                        }
                        songDataList?.removeAt(lastPlaySongIndex)
                        playLastPlayedAudio(lastPlaySongIndex - 1)
                    }
                }
            }
        } catch (e: Exception) {

        }

    }

    public fun closeMusicMiniplayer() {
        setMiniPlayerState(BottomSheetBehavior.STATE_HIDDEN)
        //audioPlayer?.pause()
        stopAudioPlayer()
    }


    var isPause: Boolean = false

    fun setUpPlayableContentListViewModel(id: Long, contentType: Int?, isPause: Boolean) {
        setLog("isPause", "BaseActivity-setUpPlayableContentListViewModel-1-isPause-$isPause")
        this.isPause = isPause
        setLog("isPause", "BaseActivity-setUpPlayableContentListViewModel-2-isPause-$isPause")
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)

        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "BaseActivity-setUpPlayableContentListViewModel-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )

        if (ConnectionUtil(this@BaseActivity).isOnline(false)) {
            CoroutineScope(Dispatchers.IO).launch {
                getPlayableContentUrl(applicationContext, id.toString(), contentType!!)
            }

        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35),
                getString(R.string.toast_message_5),
                MessageType.NEGATIVE,
                true
            )
            CommonUtils.showToast(this@BaseActivity, messageModel)
        }

    }


    private fun playableObserverResponse(it: PlayableContentModel) {
        setLog("isPause", "BaseActivity-playableObserverResponse-1-isPause-$isPause")
        if (it != null) {
            //setLog(TAG, "isViewLoading $it")
            if (!TextUtils.isEmpty(it?.data?.head?.headData?.misc?.url)) {
                /*setLog(
                    "ContentOffline",
                    "setSongLyricsData playableObserverResponse: ${it?.data?.head?.headData?.misc?.sl?.lyric?.link} title=> ${it?.data?.head?.headData?.title}"
                )*/

                setPlayableContentListData(it)
            } else {
                //preCatch Playable Url logic
                var index = nowPlayingCurrentIndex()
                val track = Track()
                track.id = it.data.head.headData.id.toLong()
                if (isPreCatchContent(songDataList, track)) {
                    setLog(
                        "preCatchContent",
                        "BaseActivity-playableObserverResponse-isPreCatchContent-true"
                    )
                    index += 2
                } else {
                    setLog(
                        "preCatchContent",
                        "BaseActivity-playableObserverResponse-isPreCatchContent-false"
                    )
                    index += 1
                    updateNowPlayingCurrentIndex(index)

                }
                setLog("preCatchContent", "BaseActivity-playableObserverResponse-index-$index")
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > index) {
                    setLog(
                        "preCatchContent",
                        "BaseActivity-playableObserverResponse-track.id-${songDataList?.get(index)?.id}"
                    )
                    setUpPlayableContentListViewModel(
                        songDataList?.get(index)?.id!!,
                        songDataList?.get(index)?.playerType?.toInt(),
                        false
                    )
                }

            }

        }
    }

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            CoroutineScope(Dispatchers.Main).launch {
                setLog("playingId12", playableContentModel?.data?.head?.headData?.id.toString())
                setLog(
                    "playingId12",
                    " setPlayableContentListData: ${playableContentModel?.data?.head?.headData?.id.toString()}")
                setLog("isPause", "BaseActivity-setPlayableContentListData-1-isPause-$isPause")
                setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-1")

                async {
                    setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-2")
                    updateSongUrl(
                        playableContentModel,
                        isPause
                    )
                    setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-2.3")
                }.await()
                async {
                    setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-4")
                    preCatchApiCall(songDataList, nowPlayingCurrentIndex(), false)
                    setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-5")
                }.await()
            }
        }
    }


    fun callEventUserProperty(userProfileModel: UserProfileModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val userDataMap = HashMap<String, String>()

            userDataMap.put(EventConstant.APP_CODE, "UN")
            userDataMap.put(EventConstant.MUSIC_DOWNLOAD_QUALITY, "Auto")
            userDataMap.put(
                EventConstant.MOBILE_CARRIER,
                CommonUtils.getUserCarrier(this@BaseActivity)!!
            )
            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.email!!)) {
                userDataMap.put(EventConstant.EMAIL, userProfileModel.result?.get(0)?.email!!)
            }

            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.gender!!)) {
                userDataMap.put(EventConstant.GENDER, userProfileModel.result?.get(0)?.gender!!)
            }

            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.firstName!!)) {
                userDataMap.put(EventConstant.FIRST_NAME, userProfileModel.result?.get(0)?.firstName!!)
            }

            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.lastName!!)) {
                userDataMap.put(EventConstant.LAST_NAME, userProfileModel.result?.get(0)?.lastName!!)
            }

            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.firstName!!)) {
                userDataMap.put(EventConstant.NAME, userProfileModel.result?.get(0)?.firstName!!)
            }

            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.phone!!)) {
                userDataMap.put(EventConstant.PHONE, userProfileModel.result?.get(0)?.phone!!)
            }

            if (!TextUtils.isEmpty(userProfileModel.result?.get(0)?.dob!!)) {
                userDataMap.put(EventConstant.AGE, userProfileModel.result?.get(0)?.dob!!)
                var age:Int?=0
                if(!DateUtils.getCurrentDate().isNullOrEmpty() &&
                    !userProfileModel.result?.get(0)?.dob.isNullOrEmpty()){
                    val currentYear = DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD,
                        DateUtils.DATE_YYYY,
                        DateUtils.getCurrentDate()!!
                    )
                    val dobYear = DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                        DateUtils.DATE_YYYY,
                        userProfileModel.result?.get(0)?.dob!!
                    )
                    if(!currentYear.isNullOrEmpty() && !dobYear.isNullOrEmpty()){
                        age = currentYear?.toInt()!! - dobYear?.toInt()!!
                    }
                }
//                val age = currentYear?.toInt()!! - dobYear?.toInt()!!
                if (age != null) {
                    if (age > 18) {
                        userDataMap.put(EventConstant.AGE_18_PLUS, "true")
                    } else {
                        userDataMap.put(EventConstant.AGE_18_PLUS, "false")
                    }
                }
            }

            userDataMap.put(EventConstant.IMEI, Utils.getDeviceId(this@BaseActivity))


            val langItem = SharedPrefHelper.getInstance().getLanguageObject(PrefConstant.LANG_DATA)
            if (langItem != null && !TextUtils.isEmpty(langItem?.title)) {
                userDataMap.put(EventConstant.APP_LANGUAGE, langItem?.title!!)
            }

            userDataMap.put(EventConstant.APP_VERSION, BuildConfig.VERSION_NAME)
            userDataMap.put(EventConstant.BUILD_NUMBER, "" + BuildConfig.VERSION_CODE)

            userDataMap.put(EventConstant.DEVICE_MODEL, Build.MODEL)
            userDataMap.put(EventConstant.OS, "Android")
            userDataMap.put(EventConstant.OS_VERSION, "" + Build.VERSION.RELEASE)


            userDataMap.put(EventConstant.VERSION_CODE, "" + BuildConfig.VERSION_CODE)
            userDataMap.put(
                EventConstant.CURRENT_CONNECTIVITY,
                ConnectionUtil(this@BaseActivity).networkType
            )
            userDataMap.put(EventConstant.DEVICE_NAME, Utils.getDeviceName()!!)

            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                userDataMap.put(EventConstant.LOGGED_IN_STATUS, "logged-in")
                userDataMap.put(
                    EventConstant.HUNGAMA_ID,
                    "" + SharedPrefHelper.getInstance().getUserId()
                )
                userDataMap.put(
                    EventConstant.MOREANONYMOUS_ID, "" + SharedPrefHelper.getInstance().get(
                        PrefConstant.SILENT_USER_ID, ""
                    )
                )
            } else {
                userDataMap.put(EventConstant.LOGGED_IN_STATUS, "silent")
                userDataMap.put(
                    EventConstant.HUNGAMA_ID, "" + SharedPrefHelper.getInstance().get(
                        PrefConstant.SILENT_USER_ID, ""
                    )
                )
                userDataMap.put(
                    EventConstant.MOREANONYMOUS_ID, "" + SharedPrefHelper.getInstance().get(
                        PrefConstant.SILENT_USER_ID, ""
                    )
                )
            }


            val batteryOptimization = SharedPrefHelper.getInstance()[PrefConstant.backgroundActivity, ""]
            if (batteryOptimization.isNotEmpty())
            userDataMap[EventConstant.BACKGROUND_ACTIVITY] = batteryOptimization
            else
                userDataMap[EventConstant.BACKGROUND_ACTIVITY] = "-"
            setLog("BatteryOptimization", batteryOptimization)

            val upiAps =Utils.arrayToString(CommonUtils.getAllUpiEnabledAppList(this@BaseActivity))
            val musicApps =Utils.arrayToString(CommonUtils.getAllMusicEnabledAppList(this@BaseActivity))
            val videoApps =Utils.arrayToString(CommonUtils.getAllVideoEnabledAppList(this@BaseActivity))
            setLog("InstalledAps", "upiAps-$upiAps")
            setLog("InstalledAps", "musicApps-$musicApps")
            setLog("InstalledAps", "videoApps-$videoApps")

            userDataMap.put(EventConstant.MUSIC_APPS, musicApps)
            userDataMap.put(EventConstant.VIDEO_OTT_APPS, videoApps)
            userDataMap.put(EventConstant.PAYMENT_APPS, upiAps)
            userDataMap.put(EventConstant.NOTIFICATION_ON, SharedPrefHelper.getInstance().getMobileNotificationEnable().toString())

            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }

    }

    public fun setEventModelDataAppLevel(
        contentID: String,
        songName: String,
        bucketName: String,
        consumptionType: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var eventModel: EventModel? = HungamaMusicApp?.getInstance()?.getEventData(contentID!!)
                if (eventModel == null || TextUtils.isEmpty(eventModel?.contentID)) {
                    eventModel = EventModel()
                }

                if (TextUtils.isEmpty(eventModel?.contentID)) {
                    eventModel?.contentID = "" + contentID
                }

                if (TextUtils.isEmpty(eventModel?.songName)) {
                    eventModel?.songName = "" + songName
                }

                if (TextUtils.isEmpty(eventModel?.bucketName)) {
                    eventModel?.bucketName = "" + bucketName
                }
                if (eventModel.bucketName.contains("Good", true)) {
                    eventModel?.bucketName = EventConstant.CONTINUE_WATCHING_NAME
                }

                eventModel?.consumptionType = consumptionType

                var source = ""
                if (!TextUtils.isEmpty(MainActivity.lastItemClicked)){
                    source = MainActivity.lastItemClicked
                }

                if (!TextUtils.isEmpty(MainActivity.headerItemName)){
                    if (!TextUtils.isEmpty(source)){
                        source += "_"+MainActivity.headerItemName
                    }else{
                        source = MainActivity.headerItemName
                    }
                }

                if (!TextUtils.isEmpty(eventModel.bucketName)){
                    if (!TextUtils.isEmpty(source)){
                        source += "_"+ eventModel.bucketName
                    }else{
                        source = eventModel.bucketName
                    }
                }

                eventModel?.sourceName = source

                HungamaMusicApp.getInstance().setEventData(eventModel.contentID, eventModel)

                setLog(TAG, "setEventModelDataAppLevel: eventModel:${eventModel}")
            } catch (e: Exception) {

            }
        }


    }

    fun updateSongUrl(
        playableContentModel: PlayableContentModel,
        isPause: Boolean
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-2.1")
                setLog("isPause", "BaseActivity-updateSongUrl-1-isPause-$isPause")
                setLog("BaseActivity", "Lifecycle-updateSongUrl-startForegroundService")

                var track = Track()
                songDataList.forEachIndexed { index, song ->
                    if (song.id == playableContentModel.data.head.headData.id.toLong()) {
                        song.url = playableContentModel.data.head.headData.misc.url
                        song.onErrorPlayableUrl = playableContentModel.data.body.data.url.apiResponse.toString()
                        var drmlicence = ""
                        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.drm.token)) {
                            drmlicence =
                                playableContentModel.data.head.headData.misc.downloadLink.drm.token
                        } else if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.mdn.token)) {
                            drmlicence =
                                playableContentModel.data.head.headData.misc.downloadLink.mdn.token
                        }
                        song.drmlicence = drmlicence
                        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.sl.lyric?.link)) {
                            if (!playableContentModel.data.head.headData.misc.sl.lyric?.link?.contains(
                                    ".txt"
                                )!!
                            ) {
                                song.songLyricsUrl =
                                    playableContentModel.data.head.headData.misc.sl.lyric?.link
                            }
                        }



                        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.playble_image)){
                            song.image = playableContentModel.data.head.headData.playble_image
                        }else if (song.image?.isNullOrBlank()!!&&!TextUtils.isEmpty(playableContentModel.data.head.headData.image)){
                            song.image = playableContentModel.data.head.headData.image
                        }

                        song.pid = playableContentModel.data.head.headData.misc.pid.toString()
                        song.shareUrl = playableContentModel.data.head.headData.misc.share
                        song.favCount = playableContentModel.data.head.headData.misc.f_fav_count
                        setLog("playbackQuality", "BaseActivity-Before-id-${song.id}-title-${song.title} - key-${song.urlKey} - isGoldUser-${getIsGoldUser()}")
                        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.urlKey)){
                            song.urlKey = playableContentModel.data.head.headData.misc.urlKey
                        }
                        setLog("playbackQuality", "BaseActivity-after-id-${song.id}-title-${song.title} - key-${song.urlKey} - isGoldUser-${getIsGoldUser()}")
                        track = song


                    }
                }
                //preCatch Playable Url logic
                if (isPreCatchContent(songDataList, track)) {
                    setLog("preCatchContent", "BaseActivity-updateSongUrl-isPreCatchContent-true")
                    /*val intent = Intent(this@BaseActivity, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_PRE_CACHED_URL.name
                        putExtra(Constant.SELECTED_TRACK, track)
                    }
                    this.let {
                        Util.startForegroundService(this@BaseActivity, intent)
                    }*/
                    withContext(Dispatchers.Main){
                        audioPlayerService?.setPreCachedContent(track)
                    }
                } else {
                    setLog("preCatchContent", "BaseActivity-updateSongUrl-isPreCatchContent-false")
                    removePlayerDurationCallback()
                    setLog("callUserStreamUpdate1", "updateSongUrl")
                    //callUserStreamUpdate(STREAM_POSITION_CURRENT)
                    val intent = Intent(this@BaseActivity, AudioPlayerService::class.java)
                    intent.putExtra(Constant.IS_PAUSE, isPause)
                    intent.action = AudioPlayerService.PlaybackControls.PLAY.name
                    intent.putExtra(Constant.SELECTED_TRACK_POSITION, nowPlayingCurrentIndex())
                    setLog(
                        "SwipablePlayerFragment",
                        "BaseActivity-updateSongUrl()-isNextClick-$isNextClick"
                    )
                    var trackPlayStartPosition = 0L
                    if (isNextClick == NEXT_CLICK_STATUS) {
                        nowPlayingQueue?.updateNextTrack(track)
                        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
                        intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
                    } else if (isNextClick == PREVIOUS_CLICK_STATUS) {
                        nowPlayingQueue?.updatePreviousTrack(track)
                        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
                        intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
                    } else {
                        if (isNextClick == START_STATUS) {
                            if (HungamaMusicApp?.getInstance()
                                    ?.getContinueWhereLeftData() != null && HungamaMusicApp?.getInstance()
                                    ?.getContinueWhereLeftData()?.size!! > 0
                            ) {
                                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                                    val track =
                                        songDataList?.get(nowPlayingCurrentIndex())

                                    if (track != null && track?.id != null && track.id > 0) {
                                        HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()
                                            ?.forEach {

                                                if (it?.data?.id.toString().contains("" + track?.id)) {
                                                    if (it?.data?.durationPlay != null && it?.data?.durationPlay?.toLong()!! > 0) {
                                                        trackPlayStartPosition =
                                                            TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!)
                                                    }
                                                    return@forEach
                                                }
                                            }
                                    }
                                }
                            }
                        }
                        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
                        intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
                    }
                    if (isLastContentPlayAfterMiniVideoPlayer) {
                        trackPlayStartPosition = HungamaMusicApp.getInstance().getContentDuration(track.id.toString())!!
                    }
                    if (trackPlayStartPosition > 0){
                        intent.putExtra(
                            Constant.SELECTED_TRACK_PLAY_START_POSITION,
                            trackPlayStartPosition
                        )
                    }
                    intent.putExtra(Constant.SHUFFLE_TRACKS, nowPlayingQueue?.shuffleEnabled)
                    withContext(Dispatchers.Main){
                        try {
                            setLog(
                                "SwipablePlayerFragment",
                                "BaseActivity-updateSongUrl()-2- trackPlayStartPosition-${trackPlayStartPosition}"
                            )
                            setLog("NotificationManager", "BaseActivity-updateSongUrl-isPause-$isPause")
                            //Handler(Looper.getMainLooper()).postDelayed({
                            Util.startForegroundService(getViewActivity(), intent)
                            reBindService()
                            //}, 1000)
                        }catch (e:Exception){

                        }
                    }

                }
                setLog("preCatchApiCall", "BaseActitvity-setPlayableContentListData-2.2")
            }catch (e:Exception){

            }
        }

    }

    override fun onClick(v: View) {
        Utils.hideSoftKeyBoard(v.context, v)
        setLog("BaseActivityLifecycleMethods", "onClick-isAudioAdPlaying-$isAudioAdPlaying")
        when (v.id) {
            R.id.btn_next_play_mini -> {
                playNextSong(true)
            }
            R.id.btn_play_mini -> {
               playPlayer()
            }
            R.id.btn_pause_mini -> {
                pausePlayer()
            }
        }
    }

    override fun onMusicMenuItemClick(menuId: Int) {
        setLog(TAG, "onMusicMenuItemClick: " + menuId)
        //setLog("onMusicMenuItemClick", "True")
        //setSongLike()
        if (menuId == SLEEP_TIMER_MENU_ITEM) {
            //openTimePickerDialog()
            openSleepTimeChangeDialog()
        } else if (menuId == Constant.SHARE_STORY_MENU_ITEM) {
            //openTimePickerDialog()
            setLog(TAG, "onMusicMenuItemClick: menuId " + menuId)
            openStoryPlatformDialog()
        }else if (menuId == Constant.CAST_SCREEN_MENU_ITEM) {
            BaseFragment.mediaRouteButton?.let {
                setUpChormeCast()
                BaseFragment.mediaRouteButton?.performClick()
                setLog(TAG, "onMusicMenuItemClick: CAST_SCREEN_MENU_ITEM:" + menuId)
            }

        }
    }

    fun setSongLike() {
        if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
            CommonUtils.setLog(
                "isGotoDownloadClicked",
                "BaseActivity-setSongLike-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
            )
            if (ConnectionUtil(this).isOnline(false)) {
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                    callFavRadio(
                        songDataList?.get(nowPlayingCurrentIndex())?.id.toString(),
                        songDataList?.get(nowPlayingCurrentIndex())?.playerType,
                        !songDataList?.get(nowPlayingCurrentIndex())?.isLiked!!
                    )

                    if (songDataList?.get(nowPlayingCurrentIndex())?.isLiked!!) {
                        songDataList?.get(nowPlayingCurrentIndex())?.isLiked = false
                    } else {
                        songDataList?.get(nowPlayingCurrentIndex())?.isLiked = true
                    }
                    if (onSwipablePlayerListener != null) {
                        setLog(
                            TAG, "setSongLike: isLike baseActivity ${
                                songDataList?.get(
                                    nowPlayingCurrentIndex()
                                )?.isLiked
                            }"
                        )
                        onSwipablePlayerListener?.onFavoritedContentStateChange(
                            songDataList?.get(
                                nowPlayingCurrentIndex()
                            )?.isLiked!!
                        )
                        onMusicPlayerThreeDotMenuListener?.onFavoritedContentStateChange(
                            songDataList?.get(
                                nowPlayingCurrentIndex()
                            )?.isLiked!!
                        )
                    }
                }
            }
        }
    }

    fun initFavoriteMusic(track: Track) {
        if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex() && songDataList?.get(
                    nowPlayingCurrentIndex()
                )?.pType != DetailPages.LOCAL_DEVICE_SONG_PAGE.value
            ) {
                if (onSwipablePlayerListener != null) {
                    onSwipablePlayerListener?.onFavoritedContentStateChange(track.isLiked)
                    onMusicPlayerThreeDotMenuListener?.onFavoritedContentStateChange(track.isLiked)
                }
            }
        }
    }

    fun callFavRadio(contentId: String?, type: String?, action: Boolean) {
        CoroutineScope(Dispatchers.IO).launch{
            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId", "" + contentId)
            jsonObject1.put("typeId", "" + type)
            jsonObject1.put("action", action)
            jsonObject1.put("module", Constant.MODULE_FAVORITE)


            userViewModelBookmark?.callBookmarkApi(
                this@BaseActivity,
                jsonObject1.toString()
            )
            /*if (action) {
                val messageModel = MessageModel(
                    getString(R.string.toast_str_17),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(this@BaseActivity, messageModel)
            } else {
                val messageModel = MessageModel(
                    getString(R.string.toast_str_18),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(this@BaseActivity, messageModel)
            }*/
            if (action){
                val eventModel = HungamaMusicApp.getInstance().getEventData(
                    "" + songDataList?.get(
                        nowPlayingCurrentIndex()
                    )?.id
                )
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel?.actor)
                hashMap.put(EventConstant.ALBUMID_EPROPERTY, "" + eventModel?.album_ID)
                hashMap.put(EventConstant.CATEGORY_EPROPERTY, "" + eventModel?.category)

                var newContentId=songDataList?.get(nowPlayingCurrentIndex())?.id
                var contentIdData=newContentId.toString().replace("playlist-","")

                hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" +contentIdData )
                hashMap.put(
                    EventConstant.CONTENTNAME_EPROPERTY,
                    "" + songDataList?.get(nowPlayingCurrentIndex())?.title
                )

                hashMap.put(
                    EventConstant.CONTENTTYPE_EPROPERTY,
                    "" + Utils.getContentTypeName("" + songDataList?.get(nowPlayingCurrentIndex())?.playerType!!)
                )
                hashMap.put(EventConstant.GENRE_EPROPERTY, "" + eventModel.genre)
                hashMap.put(EventConstant.LANGUAGE_EPROPERTY, "" + eventModel?.language)
                hashMap.put(EventConstant.LYRICIST_EPROPERTY, "" + eventModel?.lyricist)
                hashMap.put(EventConstant.MOOD_EPROPERTY, "" + eventModel?.mood)
                hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY, "" + eventModel?.musicDirectorComposer)
                hashMap.put(
                    EventConstant.NAME_EPROPERTY,
                    "" + songDataList?.get(nowPlayingCurrentIndex())?.title
                )
                hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "" + eventModel.podcast_host)
                hashMap.put(EventConstant.SINGER_EPROPERTY, "" + eventModel.singer)
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + eventModel.sourceName)
                hashMap.put(EventConstant.TEMPO_EPROPERTY, "" + eventModel.tempo)
                hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                hashMap.put(
                    EventConstant.YEAROFRELEASE_EPROPERTY,
                    "" + DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                        DateUtils.DATE_YYYY,
                        eventModel?.release_Date
                    )
                )
                EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
            }
        }

    }

    private fun callUserListing(action: Boolean) {
        if (songDataList != null && songDataList?.size!! > nowPlayingCurrentIndex() && songDataList?.get(
                nowPlayingCurrentIndex()
            )?.playerType.equals(Constant.PLAYER_LIVE_RADIO)
        ) {
            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId", "" + songDataList?.get(nowPlayingCurrentIndex())?.id)
            jsonObject1.put("typeId", "" + songDataList?.get(nowPlayingCurrentIndex())?.playerType)
            jsonObject1.put("action", action)
            jsonObject1.put("module", Constant.MODULE_RADIO_LISTENING)


            setLog(TAG, "callUserListing: callStreamEventAnalytics action:${action}")
            /*if (action) {
                callStreamEventAnalytics(songDataList?.get(nowPlayingCurrentIndex())!!, EventType.STREAM_START)
                isLiveRadioStreamStratCalled = true
            } else {
                if (isLiveRadioStreamStratCalled){
                    callStreamEventAnalytics(songDataList?.get(nowPlayingCurrentIndex())!!, EventType.STREAM)
                }
            }*/


            musicViewModel?.updateRadioListeningStream(
                this@BaseActivity,
                jsonObject1
            )
        }
    }


    private val updatePlayerDurationTask = object : Runnable {
        override fun run() {
            updatePlayerDuration()
            mainHandler?.postDelayed(this, 1000)
        }
    }

    fun updatePlayerDuration() {
        MainScope().launch(Dispatchers.Main) {
            /*val currMinute = (TimeUnit.MILLISECONDS.toMinutes(audioPlayer?.currentPosition!!) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(audioPlayer?.currentPosition!!)))
            val currSecond = TimeUnit.MILLISECONDS.toSeconds(audioPlayer?.currentPosition!!) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(audioPlayer?.currentPosition!!))
            val curDur = String.format(
                "%02d:%02d",
                currMinute,
                currSecond
            )*/
            //setLog("totalSonDuration", "inLong-audioPlayer?.duration-${audioPlayer?.duration}")
            lastSongPlayedDuration = lastSongPlayDuration
            lastSongPlayDuration = TimeUnit.MILLISECONDS.toSeconds(audioPlayer?.currentPosition!!).toInt()
            /*setLog(
                "PlayerAds","BaseActivity-updatePlayerDuration-lastSongPlayDuration-"+BaseActivity.lastSongPlayDuration.toString()
            )*/

            /*val toMinute = TimeUnit.MILLISECONDS.toMinutes(audioPlayer?.duration!!) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(audioPlayer?.duration!!))
            val toSecond = TimeUnit.MILLISECONDS.toSeconds(audioPlayer?.duration!!) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(audioPlayer?.duration!!))*/

            val diff: Long = audioPlayer?.duration!! - audioPlayer?.currentPosition!!
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(minutes)
            var totDur = ""
            if (minutes >= 0 && minutes <= 120 && seconds >= 0 && seconds <= 120) {
                totDur = String.format(
                    "%02d:%02d",
                    minutes,
                    seconds
                )

                setLog(
                    "songsPlayedCurrentDuration",
                    "title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} totDur:${totDur}"
                )
            }


            /*if (isSleepTimerSetToEndOfCurrentPlay) {
                tvSleepTimer?.visibility = View.VISIBLE
                tvSleepTimer?.setText(totDur)
            }*/
            if (onSwipablePlayerListener != null && isNewSwipablePlayerOpen) {
                onSwipablePlayerListener?.onPlayerProgressChange(
                    audioPlayer?.currentPosition,
                    audioPlayer?.duration,
                    totDur
                )
            }
            setTabPlayerProgress(audioPlayer?.currentPosition, audioPlayer?.duration)
        }
    }

    private fun startPlayerDurationCallback() {
        if (mainHandler != null) {
            removePlayerDurationCallback()
            mainHandler?.post(updatePlayerDurationTask)
        } else {
            mainHandler = Handler(Looper.getMainLooper())
            mainHandler?.post(updatePlayerDurationTask)
        }
        startLrcRunnableCallback()
    }

    private fun removePlayerDurationCallback() {
        if (mainHandler != null) {
            mainHandler?.removeCallbacks(updatePlayerDurationTask)
        }
        removeLrcRunnableCallback()
    }

    override fun onPlaybackStateChanged(state: Int) {
        setLog("callUserStreamUpdate1", "onPlaybackStateChanged state:${state}")

        when (audioPlayer?.getPlaybackState()) {
            ExoPlayer.STATE_BUFFERING -> {
                removePlayerDurationCallback()
                removeDurationCallback()
            }
            ExoPlayer.STATE_ENDED -> {
                callUserStreamUpdate(STREAM_POSITION_CURRENT)
                removePlayerDurationCallback()
                removeDurationCallback()
                setLog("Gapless", "ExoPlayer.STATE_ENDED-setAudioPlaybackActionEvents()")
                //audioPlayer?.volume = (initialPlayerVolume / 100.0F)
                //callRecommendedApiOnEndOfSong()
            }
            ExoPlayer.STATE_IDLE -> {
                removePlayerDurationCallback()
                removeDurationCallback()
            }
            ExoPlayer.STATE_READY -> {
                if (audioPlayer != null && audioPlayer?.isPlaying == true){
                    startPlayerDurationCallback()
                }else{
                    removePlayerDurationCallback()
                }
                startDurationCallback()
                setLog("Gapless", "ExoPlayer.STATE_READY-setAudioPlaybackActionEvents()")
                setLog("callUserStreamUpdate1", "onPlaybackStateChanged STATE_READY")
                //setAudioPlaybackActionEvents()
                //isAudioPlaybackActionEventSet = true
            }
            else -> {
//                callUserStreamUpdate()
                removePlayerDurationCallback()
                removeDurationCallback()
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super<Player.Listener>.onIsPlayingChanged(isPlaying)
        setLog("playerStates", "BaseActivity-onIsPlayingChanged()-isPlaying-$isPlaying-isVideoStoryPlaying-$isVideoStoryPlaying")
        if (isPlaying) {
            if (isVideoStoryPlaying){
                pausePlayer()
            }else{
                startPlayerDurationCallback()
                startDurationCallback()
                playAudio()
                maxPlayerErrorRetryCount = 0
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                    if (currentPlayerType == CONTENT_MUSIC_VIDEO) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val hashMap = HashMap<String, String>()
                            hashMap.put(
                                EventConstant.CONTENTTYPESTREAMING_EPROPERTY,
                                "" + Utils.getContentTypeName("" + songDataList?.get(nowPlayingCurrentIndex())?.playerType!!)!!
                            )
                            hashMap.put(
                                EventConstant.PLAYERTYPE_EPROPERTY,
                                EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER
                            )
                            setLog("TAG", "videopause${hashMap}")
                            EventManager.getInstance().sendEvent(VideoPlayerPlayEvent(hashMap))
                        }
                        btn_play_mini?.visibility = View.GONE
                        btn_pause_mini?.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            removePlayerDurationCallback()
            removeDurationCallback()
            pauseAudio()
            displayBandwidth(0)
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                if (currentPlayerType == CONTENT_MUSIC_VIDEO) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val hashMap = HashMap<String, String>()
                        hashMap.put(
                            EventConstant.CONTENTTYPESTREAMING_EPROPERTY,
                            "" + Utils.getContentTypeName("" + songDataList?.get(nowPlayingCurrentIndex())?.playerType!!)!!
                        )
                        hashMap.put(
                            EventConstant.PLAYERTYPE_EPROPERTY,
                            EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER
                        )
                        setLog("TAG", "videopause${hashMap}")
                        EventManager.getInstance().sendEvent(VideoPlayerPauseEvent(hashMap))
                    }

                }
                btn_play_mini?.visibility = View.VISIBLE
                btn_pause_mini?.visibility = View.GONE
            }

        }
    }

    var playItemListener: PlayItemChangeListener? = null
    public fun addPlayItemChangeListener(playItemChangeListener: PlayItemChangeListener) {
        playItemListener = playItemChangeListener
    }

    interface PlayItemChangeListener {
        fun playItemChange()
    }

    var isFirstTimeTriggerEvent = true
    private fun callStreamEventAnalytics(track: Track, eventType: EventType, isFullSongPlayed: Boolean = false)
    {
        try{
            if (track.playerType.equals("22", true) || track.playerType.equals(
                    "51",
                    true
                ) || track.playerType.equals("88888", true)
            ){
                return
            }
            else {
                val eventModel = HungamaMusicApp.getInstance().getEventData("" + track?.id)
                setLog(TAG, "callStreamEventAnalytics : eventType :${eventType} track id:${track?.id}  track titl:${track?.title} trackData id:${trackData?.id}  trackData titl:${trackData?.title} eventModel:${eventModel?.toString()}")
                setLog("callUserStreamUpdate1", "callUserStreamUpdate: event called:stream 3 :${eventType}")
                setLog("callUserStreamEventModel", " " + Gson().toJson(eventModel))
                setLog("callUserStreamEventModel", "\n " + Gson().toJson(trackData))
                val hashMap = HashMap<String, String>()

                hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
                hashMap.put(EventConstant.AUDIO_QUALITY_EPROPERTY, "" + eventModel?.audioQuality)
                hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + eventModel?.bucketName)
                hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"Audio Player")
                hashMap.put(EventConstant.CONNECTION_TYPE_EPROPERTY, ConnectionUtil(this@BaseActivity).networkType)
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + eventModel?.consumptionType)
                val newContentId=eventModel?.contentID
                val contentIdData=newContentId?.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)
                setLog("ActualContentType","${trackData.playerType}")
//                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "" + Utils.getContentTypeNameForStream("" + trackData.playerType))
                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "" + Utils.getContentTypeNameForStream("" + track.playerType))

                var durationBG = 0L
                var durationFG = 0L
                try {
                    setLog(
                        TAG,
                        "callStreamEventAnalytics duration:${eventModel.duration} duration_fg: ${eventModel.duration_fg} duration_bg:${eventModel.duration_bg}"
                    )
                    setLog(TAG, "callStreamEventAnalytics durationMap:$durationMap")
                    if (durationMap != null && durationMap.containsKey(eventModel.contentID)) {
                        val list = durationMap.get(eventModel.contentID)


                        if (list != null && list.size > 0) {

//                var fistModel=list?.get(0)
                            var fistModel: DurationModel? = null
                            var lastModel = list?.get(list.size - 1)
                            var isFirstModelSave = false
                            list.forEach {
                                if (it.start_fg_time!! > 0 && !isFirstModelSave) {
                                    fistModel = it
                                    isFirstModelSave = true
                                    setLog(TAG, "callStreamEventAnalytics durationMap list fistModel:$fistModel")
                                    return@forEach
                                }
                            }

                            var currentTime = 0L

                            if (audioPlayer?.currentPosition!! > 0) {
                                currentTime = audioPlayer?.currentPosition!!
                            }

                            setLog(TAG, "callStreamEventAnalytics durationMap currentTime-1:${currentTime}")
                            var diffBG = 0L
                            var diffFG = 0L

                            if (currentTime!! <= 0 && lastSongPlayDuration != null) {
                                currentTime = lastSongPlayDuration.toLong()
                            }
                            setLog(TAG, "callStreamEventAnalytics durationMap currentTime-2:${currentTime}")
                            if (currentTime!! <= 0 && !TextUtils.isEmpty(eventModel?.duration_fg)) {
                                currentTime = eventModel.duration_fg.toLong()

                            }

                            if (isFullSongPlayed && !TextUtils.isEmpty(eventModel?.duration)) {
                                currentTime = eventModel.duration.toLong() * 1000
                            }
                            setLog(TAG, "callStreamEventAnalytics durationMap currentTime:${currentTime}")
                            setLog(TAG, "callStreamEventAnalytics durationMap list fistModel:$fistModel")
                            setLog(TAG, "callStreamEventAnalytics durationMap list lastModel:$lastModel")

                            /*if (fistModel != null){
                                diffFG=currentTime!!-fistModel?.start_fg_time!!
                                diffBG=lastModel.start_bg_time!!-fistModel?.start_bg_time!!
                            }else{
                                diffBG = currentTime - lastModel.start_bg_time!!
                                diffFG = currentTime - diffBG

                            }*/

                            var lastB = 0L
                            var lastF = 0L
                            var BG = 0L
                            var totalBG = 0L

                            var lastBB = 0L
                            var lastFF = 0L
                            var FG = 0L
                            var totalFG = 0L
                            println("items_list" + " " +list)
                            for (item in list) {
                                println("items_list"+item)
                                val B = item.start_bg_time!!
                                val F = item.start_fg_time!!
                                if (B > F) {
                                    println("B>F:"+B+">"+F)
                                    if (lastF == F) {
                                        println("lastF==F:"+lastF+"=="+F)
                                        BG = B - lastB
                                        println("BG=B-lastB:"+B+"-"+lastB)
                                    } else {
                                        BG = B - F
                                        println("BG=B-F:"+B+"-"+F)
                                        lastF = F
                                        println("lastF=F:"+F)
                                    }
                                    lastB = B
                                    println("lastB=B:"+B)
                                    println("BG="+BG)
                                    println("totalBG=totalBG+BG:"+totalBG+"+"+BG+"\n")
                                    totalBG += BG

                                } else if (F > B) {
                                    println("F>B:"+F+">"+B)
                                    if (lastBB == B) {
                                        println("lastBB==B:"+lastBB+"=="+B)
                                        FG = F - lastFF
                                        println("FG=F-lastFF:"+F+"-"+lastFF)
                                    } else {
                                        FG = F - B
                                        println("FG=F-B:"+F+"-"+B)
                                        lastBB = B
                                        println("lastBB=B:"+B)
                                    }
                                    lastFF = F
                                    println("lastFF=F:"+F)
                                    println("FG="+FG)
                                    println("totalFG=totalFG+FG:"+totalFG+"+"+FG+"\n")
                                    totalFG += FG
                                    println("totalFG=:"+totalFG+"\n")

                                }
                            }
                            println("totalFG="+totalFG)
                            println("totalBG="+totalBG)
                            diffFG = totalFG
                            diffBG = totalBG
                            setLog(
                                TAG,
                                "callStreamEventAnalytics durationMap list currentTime:${
                                    TimeUnit.MILLISECONDS.toSeconds(currentTime)
                                }"
                            )
                            setLog(TAG, "callStreamEventAnalytics durationMap list diffFG:$diffFG")
                            setLog(TAG, "callStreamEventAnalytics durationMap list diffBG:$diffBG")

                            durationFG = TimeUnit.MILLISECONDS.toSeconds(diffFG)
                            durationBG = TimeUnit.MILLISECONDS.toSeconds(diffBG)
                            setLog(TAG, "callStreamEventAnalytics durationMap list durationFG:$durationFG")
                            setLog(TAG, "callStreamEventAnalytics durationMap list durationBG:$durationBG")
                        }
                    }
                }
                catch (exp: Exception) {
                    exp.printStackTrace()
                }

                if (isFullSongPlayed && !TextUtils.isEmpty(eventModel.duration) && durationBG == 0L) {
                    durationFG = eventModel.duration.toLong()
                }

                if (durationBG > 0) {
                    hashMap.put(EventConstant.DURATION_BG_EPROPERTY, "" + durationBG)
                }

                if (durationFG > 0) {
                    hashMap.put(EventConstant.DURATION_FG_EPROPERTY, "" + durationFG)
                }
                hashMap.put(EventConstant.GENRE_EPROPERTY, "" + eventModel?.genre)
                hashMap.put(EventConstant.LABEL_EPROPERTY, "" + eventModel?.label)
                hashMap.put(EventConstant.LABEL_ID_EPROPERTY, "" + eventModel?.label_id)
                hashMap.put(EventConstant.LANGUAGE_EPROPERTY, "" + eventModel?.language)


                hashMap.put(EventConstant.LYRICIST_EPROPERTY, "" + eventModel?.lyricist)
                hashMap.put(EventConstant.MOOD_EPROPERTY, "" + eventModel?.mood)
                hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY, "" + eventModel?.musicDirectorComposer)
                hashMap.put(EventConstant.ORIGINAL_ALBUM_NAME_EPROPERTY, "" + eventModel?.originalAlbumName)
                var totalTime=0L

                totalTime = audioPlayer?.duration!! //length
                var length = TimeUnit.MILLISECONDS.toSeconds(totalTime)
                if(!TextUtils.isEmpty(eventModel.duration!!)){
                    /*if(eventModel.duration?.toLong()>length){
                        totalTime=eventModel.duration?.toLong()
                        hashMap.put(EventConstant.LENGTH_EPROPERTY, "" + eventModel.duration?.toLong())
                    }else{
                        totalTime=length
                        hashMap.put(EventConstant.LENGTH_EPROPERTY, "" + length)
                    }*/
                    length=eventModel.duration.toLong()
                }else if (length > 0) {
                    /*totalTime=length
                    hashMap.put(EventConstant.LENGTH_EPROPERTY, "" + length)*/
                }
                hashMap.put(EventConstant.LENGTH_EPROPERTY, "" + length)

                var totalDurationPlayed = durationBG + durationFG
                if (track.playerType.equals("34", true) || track.playerType.equals("77777", true)) {

                }else{
                    if(!TextUtils.isEmpty(eventModel.duration)){
                        if(isFullSongPlayed){
                            totalDurationPlayed=eventModel.duration.toLong()
                        }else if (totalDurationPlayed > eventModel.duration.toLong()){
                            totalDurationPlayed=eventModel.duration.toLong()
                        }
                    }
                }
                hashMap.put(EventConstant.DURATION_EPROPERTY, "" + totalDurationPlayed)
                /*if(currentTime>0){
                    hashMap.put(EventConstant.DURATION_EPROPERTY, "" + TimeUnit.MILLISECONDS.toSeconds(currentTime))
                }else{
                    hashMap.put(EventConstant.DURATION_EPROPERTY, "" + durationFG)
                }*/
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
                    EventConstant.PODCAST_ALBUM_NAME_EPROPERTY, "" + eventModel?.podcast_album_name
                )

                hashMap.put(EventConstant.SINGER_EPROPERTY, "" + eventModel?.singer)
                hashMap.put(EventConstant.SONG_NAME_EPROPERTY, "" + eventModel.songName)
                hashMap.put(EventConstant.CONTENT_NAME_EPROPERTY, "" + eventModel.songName)
                hashMap.put(EventConstant.CONTENT_TYPE_ID_EPROPERTY, "" + trackData.playerType)
                hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + eventModel?.sourceName)
                hashMap.put(EventConstant.SUB_GENRE_EPROPERTY, "" + eventModel?.subGenre)
                hashMap.put(EventConstant.LYRICS_TYPE_EPROPERTY, "" + eventModel?.lyrics_type)

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
                hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel?.season_Number)
                hashMap.put(EventConstant.SUBTITLE_ENABLE_EPROPERTY, "" + eventModel?.subtitleEnable)
                hashMap.put(
                    EventConstant.SUBTITLE_LANGUAGE_SELECTED_EPROPERTY,
                    "" + eventModel?.subtitleLanguageSelected
                )
                hashMap.put(EventConstant.USER_RATING_EPROPERTY, "" + eventModel?.userRating)

                hashMap.put(EventConstant.VIDEO_QUALITY_EPROPERTY, "" + eventModel?.videoQuality)
                hashMap.put(
                    EventConstant.SOURCEPAGE_EPROPERTY,
                    MainActivity.lastItemClicked + "," + MainActivity.headerItemName + "," + trackData?.heading
                )
                hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY, trackData?.heading!!)

                setLog("API_DEVICE_TYPE","Constant.API_DEVICE_TYPE-${Constant.API_DEVICE_TYPE}")
                if (Constant.API_DEVICE_TYPE.equals("Android",ignoreCase = true)){
                    hashMap.put(EventConstant.CARPLAY, "false")
                }else{
                    hashMap.put(EventConstant.CARPLAY, "true")
                }

                setLog(TAG, "callStreamEventAnalytics stream *** eventType:${eventType} hashMap:$hashMap")

                if (eventType == EventType.STREAM && streamName == "StreamStart" || streamName == "StreamFailed") {
                    setLog(
                        "callUserStreamUpdate1",
                        "callUserStreamUpdate: event called:stream 4"
                    )
                    setLog(TAG, "callUserStreamUpdate : STREAM hashMap-${hashMap}")
                    if (!TextUtils.isEmpty(hashMap.get(EventConstant.SONG_NAME_EPROPERTY))) {
                        setLog(
                            "callUserStreamUpdate1",
                            "callUserStreamUpdate: event called:stream 5"
                        )
                        durationMap.remove(hashMap.get(EventConstant.CONTENTID_EPROPERTY))
                        duratoionList = ArrayList()
                        if(CommonUtils.isUserHasGoldSubscription())
                        {
                            EventManager.getInstance().sendEvent(StreamEvent(hashMap))
                        }
                        else{
                            setLog("CheckLocalDuration", " " + localDuration.toString())
                            if (localDuration > 0) {
                                EventManager.getInstance().sendEvent(StreamEvent(hashMap))
                            }
                            else
                            {
                                EventManager.getInstance().sendEvent(PreviewStreamEvent(hashMap))
                            }
                        }
                        streamName = "Stream"

                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.LAST_PLAY_SONG_ID, eventModel.contentID)
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.LAST_PLAY_SONG_NAME, eventModel.songName)

                        val eventValue: MutableMap<String, Any> = HashMap()

                        var v_media_duration=""
                        if(durationFG>durationBG){
                            v_media_duration=""+durationFG
                        }else if(durationBG>durationFG){
                            v_media_duration=""+durationBG
                        }else{
                            v_media_duration=eventModel.duration
                        }
                        eventValue.put("media_duration", v_media_duration)

                        eventValue.put(
                            "stream_type",
                            hashMap?.get(EventConstant.CONSUMPTION_TYPE_EPROPERTY)!!
                        )
                        eventValue.put(
                            AFInAppEventParameterName.CONTENT_TYPE, hashMap?.get(EventConstant.CONTENTTYPE_EPROPERTY)!!
                        )
                        eventValue.put(
                            AFInAppEventParameterName.CONTENT_ID,
                            hashMap?.get(EventConstant.CONTENTID_EPROPERTY)!!
                        )


                        if(!v_media_duration.isNullOrBlank()){
                            AppsFlyerLib.getInstance().logEvent(
                                HungamaMusicApp.getInstance().applicationContext!!,
                                EventConstant.AF_MEDIA_PLAY,
                                eventValue
                            )
                        }
                        setLog(TAG, "callUserStream : eventValue called:${eventValue}")

                    }
                }
                else if (eventType == EventType.STREAM && streamName == "Stream" || streamName == "StreamFailed") {
                    callStreamEventAnalytics(songDataList.get(nowPlayingCurrentIndex()), EventType.STREAM_START)
                }
                else if (eventType == EventType.STREAM_START) {

                    if(!HungamaMusicApp.getInstance().userStreamIDList.contains(hashMap.get(EventConstant.CONTENTID_EPROPERTY)!!)){
                        HungamaMusicApp.getInstance().userStreamIDList.add(hashMap.get(EventConstant.CONTENTID_EPROPERTY)!!)
                    }


                    setLog(
                        "callUserStreamUpdate1",
                        "userStreamIDList:${HungamaMusicApp.getInstance().userStreamIDList}"
                    )

                    setLog(
                        "callUserStreamUpdate1",
                        "callUserStreamUpdate: event called:stream 6"
                    )
                    setLog(TAG, "callUserStreamUpdate : STREAM_START hashMap-${hashMap}")
                    if (!TextUtils.isEmpty(hashMap[EventConstant.SONG_NAME_EPROPERTY])) {
                        CoroutineScope(Dispatchers.IO).launch {

                            if(songDataList.size > streamIndexPrent && streamName == "StreamStart")
                                callStreamEventAnalytics(songDataList[streamIndexPrent], EventType.STREAM)
                            else {
                                try {
                                    setLog(
                                        "isFirstTimeTriggerEvent",
                                        "isFirstTimeTriggerEvent-1-$isFirstTimeTriggerEvent"
                                    )
//                                if (!isFirstTimeTriggerEvent){
                                    setLog(
                                        "isFirstTimeTriggerEvent",
                                        "isFirstTimeTriggerEvent-2-$isFirstTimeTriggerEvent"
                                    )
                                    callStreamTriggerEvent(this@BaseActivity, track,"Audio Player", "")
                                    delay(500)
//                                }
                                    isFirstTimeTriggerEvent = false
                                    setLog(
                                        "isFirstTimeTriggerEvent",
                                        "isFirstTimeTriggerEvent-3-$isFirstTimeTriggerEvent"
                                    )
                                    hashMap.remove(EventConstant.DURATION_EPROPERTY)
                                    hashMap.remove(EventConstant.DURATION_BG_EPROPERTY)
                                    hashMap.remove(EventConstant.DURATION_FG_EPROPERTY)
                                    hashMap.remove(EventConstant.PERCENTAGE_COMPLETION_EPROPERTY)

                                    setLog(
                                        "callUserStreamUpdate1",
                                        "callUserStreamUpdate: event called:stream 7"
                                    )
                                    if(CommonUtils.isUserHasGoldSubscription())
                                    {
                                        EventManager.getInstance().sendEvent(StreamStartEvent(hashMap))
                                    }
                                    else{
                                        setLog("CheckLocalDuration", " " + localDuration.toString())
                                        if (localDuration > 0) {
                                            EventManager.getInstance().sendEvent(StreamStartEvent(hashMap))
                                        }
                                        else
                                        {
                                            EventManager.getInstance().sendEvent(PreviewStreamStartEvent(hashMap))
                                        }
                                    }
                                    streamName = "StreamStart"
                                    setLog(
                                        TAG,
                                        "callUserStream : eventType event 123 called:${eventType}"
                                    )
                                } catch (e: Exception) {

                                }
                            }
                        }
                    }
                }

                setLog("CheckStramLog", eventType.toString() + ", " + streamName)
            }
        }catch (e:Exception){

        }
    }

    private fun playAudio() {
        if (currentPlayerType == CONTENT_RADIO) {
//            callUserListing(true)
        }
        setLog(
            TAG,
            "playAudio: currentPosition duration:${audioPlayer?.currentPosition} activityVisible:${HungamaMusicApp?.getInstance()?.activityVisible}"
        )



        if (HungamaMusicApp?.getInstance()?.activityVisible!!) {
            addFGTime()
            setLog("countFgTime", "3")
        } else {
            addBGTime()
        }


    }

    public fun pauseAudio() {
        setLog(TAG, "pauseAudio: ")

//        callUserListing(false)
//        setLog("callUserStreamUpdate1", "pauseAudio")
//        callUserStreamUpdate(STREAM_POSITION_CURRENT)

        if (HungamaMusicApp.getInstance()?.activityVisible!!) {
            addFGTime()
            setLog("countFgTime", "4")
        } else {
            addBGTime()
        }
    }


    fun hideBottomNavigationBar() {
        setLog("BaseActivity:-", "hideBottomNavigationBar - hide")
        bottomNavigationHeight = resources.getDimensionPixelSize(R.dimen.dimen_0).toInt()
        isBottomNavigationVisible = false
        showBottomNavigationAndMiniplayerBlurView()
        tabMenu?.hide()
        hideStickyAds()
    }

    fun showBottomNavigationBar() {
        bottomNavigationHeight = resources.getDimensionPixelSize(R.dimen.dimen_62).toInt()
        isBottomNavigationVisible = true
        setLog("SwipablePlayerFragment", "BaseActivity-showBottomNavigationBar()")
        showBottomNavigationAndMiniplayerBlurView()
        tabMenu?.show()
        showStickyAds()
    }

    fun hideStickyAds() {
        rlStickyAdsView?.hide()
    }

    fun showStickyAds() {
        if (CommonUtils.isHomeScreenBannerAds() && !isNewSwipablePlayerOpen) {
            if (isBottomStickyAdLoaded) {
                rlStickyAdsView?.show()
                val intent = Intent(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT)
                intent.putExtra("EVENT", Constant.STICKY_ADS_VISIBILITY_CHANGE_RESULT_CODE)
                LocalBroadcastManager.getInstance(this@BaseActivity).sendBroadcast(intent)
            }
        }
    }


    fun hideMiniPlayer() {
        miniplayerHeight = resources.getDimensionPixelSize(R.dimen.dimen_0).toInt()
        if (getMiniPlayerState() == BottomSheetBehavior.STATE_COLLAPSED) {
            setLog(
                "SwipablePlayerFragment",
                "BaseActivity-hideMiniPlayer - isNewSwipablePlayerOpen=$isNewSwipablePlayerOpen"
            )
            //setMiniPlayerState(BottomSheetBehavior.STATE_HIDDEN)
            isMiniplayerVisible = false
            showBottomNavigationAndMiniplayerBlurView()
            nowPlayingBottomSheet?.visibility = View.GONE
        }
    }

    fun showMiniPlayer() {
        setLog("SwipablePlayerFragment", "showMiniPlayer()-audioPlayer-$audioPlayer")
        setLog(
            "SwipablePlayerFragment",
            "showMiniPlayer()-getAudioPlayerPlayingStatus()-" + getAudioPlayerPlayingStatus()
        )
        setLog(
            "SwipablePlayerFragment",
            "showMiniPlayer()-audioPlayer.playbackState-${audioPlayer?.playbackState}"
        )
        val audioPlayerPlayingStatus = getAudioPlayerPlayingStatus()
        if (audioPlayer != null && (audioPlayerPlayingStatus == playing || audioPlayerPlayingStatus == pause)) {
            //if (getMiniPlayerState() == BottomSheetBehavior.STATE_HIDDEN){
            //setMiniPlayerState(BottomSheetBehavior.STATE_COLLAPSED)
            miniplayerHeight = resources.getDimensionPixelSize(R.dimen.dimen_62).toInt()
            isMiniplayerVisible = true
            setLog("SwipablePlayerFragment", "showMiniPlayer()")
            showBottomNavigationAndMiniplayerBlurView()
            nowPlayingBottomSheet?.visibility = View.VISIBLE
            //}
        }
    }

    fun addOrUpdateDownloadMusicQueue(
        downloadQueueList: ArrayList<DownloadQueue>,
        onDownloadQueueItemChanged: OnDownloadQueueItemChanged?,
        fetchMusicDownloadListener: FetchListener?,
        isOnlyForAttachListener: Boolean,
        isShowSubscriptionPopup: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            setLog("DownloadContent", "BaseActivity-addOrUpdateDownloadMusicQueue")
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = "0"
            dpm.planName = PlanNames.NONE.name
            dpm.isAudio = true
            dpm.isDownloadAction = true
            dpm.isShowSubscriptionPopup = isShowSubscriptionPopup
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT

            Constant.screen_name ="addOrUpdate Download Music Queue"

            var isOnlyForAttachListener = isOnlyForAttachListener
            if (userCanDownloadContent(this@BaseActivity, rootLayout, dpm,null,Constant.drawer_downloads_exhausted)) {
                this@BaseActivity.fetchMusicDownloadListener = fetchMusicDownloadListener
                this@BaseActivity.onDownloadQueueItemChanged = onDownloadQueueItemChanged
                if (downloadQueueList != null && downloadQueueList.size > 0) {
                    AppDatabase.getInstance()?.downloadQueue()?.insertOrReplaceAll(downloadQueueList)
                    setLog("DownloadContent", "BaseActivity-addOrUpdateDownloadMusicQueue-2")
                    callNextDownloadableItemFromQueue()
                    isOnlyForAttachListener = false
                }

                if (isOnlyForAttachListener) {
                    setLog("DownloadContent", "BaseActivity-addOrUpdateDownloadMusicQueue-3")
                    callNextDownloadableItemFromQueue()
                }
            }
        }

    }

    fun addOrUpdateDownloadVideoQueue(
        downloadQueueList: ArrayList<DownloadQueue>,
        onDownloadVideoQueueItemChanged: OnDownloadVideoQueueItemChanged?,
        isOnlyForAttachListener: Boolean,
        isShowSubscriptionPopup: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var isOnlyForAttachListener = isOnlyForAttachListener
            this@BaseActivity.onDownloadVideoQueueItemChanged = onDownloadVideoQueueItemChanged
            if (downloadQueueList != null && downloadQueueList.size > 0) {
                AppDatabase.getInstance()?.downloadQueue()?.insertOrReplaceAll(downloadQueueList)
                isOnlyForAttachListener = false
                callNextDownloadableItemFromQueue()
            }

            if (isOnlyForAttachListener) {
                callNextDownloadableItemFromQueue()
            }
        }



    }

    fun callNextDownloadableItemFromQueue() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue")
                this@BaseActivity.downloadQueueList = AppDatabase.getInstance()?.downloadQueue()
                    ?.getAllDownloadQueueItemsByDownloadStates(
                        Status.PAUSED.value,
                        Status.FAILED.value,
                        Status.QUEUED.value
                    ) as ArrayList<DownloadQueue>
                if (!downloadQueueList.isNullOrEmpty()) {
                    setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-hasActiveAudioDownload-$hasActiveAudioDownload")
                    if (!hasActiveAudioDownload) {
                        currentDownloadingIndex = 0
                        for (queueItemIndex in downloadQueueList.indices) {
                            if (downloadQueueList.get(queueItemIndex).downloadStatus == Status.DOWNLOADING.value
                                || downloadQueueList.get(queueItemIndex).downloadStatus == Status.ADDED.value
                                || downloadQueueList.get(queueItemIndex).downloadStatus == Status.QUEUED.value
                            ) {
                                currentDownloadingIndex = queueItemIndex
                                setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-currentDownloadingIndex-$currentDownloadingIndex")
                                break
                            }
                        }
                        if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > currentDownloadingIndex
                            && downloadQueueList.get(currentDownloadingIndex).contentId != null) {
                            setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-downloadManagerExoPlayerId-${downloadQueueList?.get(currentDownloadingIndex)?.downloadManagerExoPlayerId}")
                            if (downloadQueueList.get(currentDownloadingIndex).downloadRetry >= MAX_DOWNLOAD_RETRY) {
                                restrictUserToDownloadingContent(
                                    downloadQueueList.get(
                                        currentDownloadingIndex
                                    )
                                )
                            } else if (downloadQueueList.get(currentDownloadingIndex).downloadStatus == Status.PAUSED.value) {
                                setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-Paused---downloadManagerExoPlayerId-${downloadQueueList?.get(currentDownloadingIndex)?.downloadManagerExoPlayerId}")
                                return@launch
                            } else {
                                AppDatabase.getInstance()?.downloadQueue()
                                    ?.updateDownloadQueueItem(downloadQueueList.get(currentDownloadingIndex))
                                downloadQueueList.get(currentDownloadingIndex).planType =
                                    getContentPlanType(downloadQueueList.get(currentDownloadingIndex).planName)
                                if (downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.NONE.value
                                    || downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.AUDIO.value
                                    || downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.PODCAST.value
                                    || downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.RADIO.value
                                ) {
                                    setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-downloadQueueList.get(currentDownloadingIndex).contentType-${downloadQueueList.get(currentDownloadingIndex).contentType}-downloadManagerExoPlayerId-${downloadQueueList?.get(currentDownloadingIndex)?.downloadManagerExoPlayerId}")
                                    val dpm = DownloadPlayCheckModel()
                                    dpm.contentId =
                                        downloadQueueList.get(currentDownloadingIndex).contentId?.toString()!!
                                    dpm.contentTitle =
                                        downloadQueueList.get(currentDownloadingIndex).title?.toString()!!
                                    dpm.planName =
                                        downloadQueueList.get(currentDownloadingIndex).planName.toString()
                                    dpm.isAudio = true
                                    dpm.isDownloadAction = true
                                    dpm.isShowSubscriptionPopup = true
                                    dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                                    dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
                                    Constant.screen_name ="acallNextDownloadableItem From Queue"

                                    if (userCanDownloadContent(this@BaseActivity, rootLayout, dpm, null,Constant.drawer_downloads_exhausted)) {
                                        setLog(
                                            "DownloadContent",
                                            "BaseActivity-callNextDownloadableItemFromQueue-2"
                                        )
                                        if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > currentDownloadingIndex){
                                            setUpDownloadableContentListViewModel(downloadQueueList.get(currentDownloadingIndex).contentId!!, 4)
                                        }

                                    } else {
                                        if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > currentDownloadingIndex){
                                            restrictUserToDownloadingContent(downloadQueueList.get(currentDownloadingIndex))
                                        }
                                    }
                                } else {
                                    setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-downloadQueueList.get(currentDownloadingIndex).contentType-${downloadQueueList.get(currentDownloadingIndex).contentType}-downloadManagerExoPlayerId-${downloadQueueList?.get(currentDownloadingIndex)?.downloadManagerExoPlayerId}")
                                    val dpm = DownloadPlayCheckModel()
                                    dpm.contentId =
                                        downloadQueueList.get(currentDownloadingIndex).contentId?.toString()!!
                                    dpm.contentTitle =
                                        downloadQueueList.get(currentDownloadingIndex).title?.toString()!!
                                    dpm.planName = downloadQueueList.get(currentDownloadingIndex).planName?.toString()!!
                                    dpm.isAudio = false
                                    dpm.isDownloadAction = true
                                    dpm.isShowSubscriptionPopup = true
                                    dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                                    dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
                                    if (userCanDownloadContent(this@BaseActivity, rootLayout, dpm, null,Constant.drawer_downloads_exhausted)) {
                                        val dwState = downloadTracker?.getDownloadState(
                                            downloadQueueList.get(currentDownloadingIndex).downloadUrl
                                        )
                                        setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-dwState-$dwState-downloadManagerExoPlayerId-${downloadQueueList?.get(currentDownloadingIndex)?.downloadManagerExoPlayerId}")
                                        if (dwState == -1 || dwState == androidx.media3.exoplayer.offline.Download.STATE_FAILED) {
                                            if (dwState == androidx.media3.exoplayer.offline.Download.STATE_FAILED) {
                                                removeVideoContentFromDownload(
                                                    Uri.parse(
                                                        downloadQueueList.get(
                                                            currentDownloadingIndex
                                                        ).downloadUrl
                                                    )
                                                )
                                            }
                                            setLog(
                                                "DownloadContent",
                                                "BaseActivity-callNextDownloadableItemFromQueue-3"
                                            )
                                            setUpDownloadableContentListViewModel(
                                                downloadQueueList.get(
                                                    currentDownloadingIndex
                                                ).contentId!!, 5
                                            )
                                        } else if (!isVideoDownloadActive() && (dwState == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING
                                                    || dwState == androidx.media3.exoplayer.offline.Download.STATE_QUEUED)
                                        ) {
                                            resumeAllVideoDownloads()
                                        } else if (dwState == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED) {
                                            val download = downloadTracker?.getDownload(
                                                downloadQueueList.get(currentDownloadingIndex).downloadUrl
                                            )
                                            if (download != null) {
                                                downloadQueueList?.get(currentDownloadingIndex)!!.downloadedFilePath =
                                                    ""
                                                downloadQueueList?.get(currentDownloadingIndex)!!!!.downloadStatus =
                                                    Status.COMPLETED.value
                                                downloadQueueList?.get(currentDownloadingIndex)!!.downloadNetworkType =
                                                    NetworkType.ALL.value
                                                if (download?.contentLength!! < 0) {
                                                    downloadQueueList?.get(currentDownloadingIndex)!!.totalDownloadBytes =
                                                        0
                                                } else {
                                                    downloadQueueList?.get(currentDownloadingIndex)?.totalDownloadBytes =
                                                        download.contentLength
                                                }

                                                if (download.bytesDownloaded < 0) {
                                                    downloadQueueList?.get(currentDownloadingIndex)?.downloadedBytes =
                                                        0
                                                } else {
                                                    downloadQueueList?.get(currentDownloadingIndex)?.downloadedBytes =
                                                        download.bytesDownloaded
                                                }

                                                downloadQueueList?.get(currentDownloadingIndex)?.createdDT =
                                                    download.updateTimeMs
                                                downloadQueueList?.get(currentDownloadingIndex)?.percentDownloaded =
                                                    100
                                                addOrUpdateDownloadedAudio(
                                                    downloadQueueList.get(
                                                        currentDownloadingIndex
                                                    )
                                                )
                                                if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentDownloadingIndex)) {
                                                    setLog(
                                                        TAG,
                                                        "setDownloadableContentListData: deleteDownloadQueueItem 3"
                                                    )
                                                    AppDatabase.getInstance()?.downloadQueue()
                                                        ?.deleteDownloadQueueItem(
                                                            downloadQueueList?.get(currentDownloadingIndex)?.qId!!
                                                        )
                                                }
                                                callNextDownloadableItemFromQueue()
                                            } else {
                                                setLog(
                                                    "DownloadContent",
                                                    "BaseActivity-callNextDownloadableItemFromQueue-4"
                                                )
                                                setUpDownloadableContentListViewModel(
                                                    downloadQueueList.get(
                                                        currentDownloadingIndex
                                                    ).contentId!!, 5
                                                )
                                            }
                                        }else if(dwState == androidx.media3.exoplayer.offline.Download.STATE_STOPPED){
                                            val download = downloadTracker?.getDownload(
                                                downloadQueueList.get(currentDownloadingIndex).downloadUrl
                                            )
                                            if (download != null && download.stopReason == stopReasonRemove) {
                                                removeVideoContentFromDownload(
                                                    Uri.parse(
                                                        downloadQueueList.get(
                                                            currentDownloadingIndex
                                                        ).downloadUrl
                                                    )
                                                )
                                            }else if(download != null && download.stopReason == stopReasonPause && !isVideoDownloadActive()){
                                                pauseAllVideoDownloads(false, downloadQueueList.get(currentDownloadingIndex).downloadUrl)
                                            }
                                            setLog("DownloadContent", "BaseActivity-callNextDownloadableItemFromQueue-download.stopReason-${download?.stopReason}-downloadManagerExoPlayerId-${downloadQueueList?.get(currentDownloadingIndex)?.downloadManagerExoPlayerId}")
                                            //hasActiveAudioDownload = false
                                            //callNextDownloadableItemFromQueue()
                                        }
                                    } else {
                                        restrictUserToDownloadingContent(downloadQueueList.get(currentDownloadingIndex))
                                    }
                                }
                            }
                        } else {
                            if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > currentDownloadingIndex) {
                                restrictUserToDownloadingContent(
                                    downloadQueueList.get(
                                        currentDownloadingIndex
                                    )
                                )
                            }
                        }
                    } else if (!isAudioOrVideoDownloadActive()) {
                        hasActiveAudioDownload = false
                        callNextDownloadableItemFromQueue()
                    }
                }
            }catch (e:Exception){

            }
        }

    }
    var isDownloadApiCallInProgress = false
    fun setUpDownloadableContentListViewModel(id: String, contentType: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (!TextUtils.isEmpty(id)) {
                setLog(
                    "DownloadContent",
                    "BaseActivity-setUpDownloadableContentListViewModel-API call Start - isDownloadApiCallInProgress-$isDownloadApiCallInProgress"
                )
                if (!isDownloadApiCallInProgress) {
                    playableContentViewModel = ViewModelProvider(
                        this@BaseActivity
                    ).get(PlayableContentViewModel::class.java)

                    CommonUtils.setLog(
                        "isGotoDownloadClicked",
                        "BaseActivity-setUpDownloadableContentListViewModel-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                    )
                    if (ConnectionUtil(this@BaseActivity).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
                        isDownloadApiCallInProgress = true
                        playableContentViewModel?.getDownloadableContentUrl(
                            this@BaseActivity,
                            id,
                            contentType!!
                        )
                            ?.observe(this@BaseActivity,
                                Observer {
                                    when (it.status) {
                                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                            isDownloadApiCallInProgress = false
                                            if (it?.data != null) {

                                                setLog(
                                                    TAG,
                                                    "downloadableContentNextListRespObserver: "
                                                )
                                                setLog(
                                                    "DownloadContent",
                                                    "BaseActivity-setUpDownloadableContentListViewModel-API End - isDownloadApiCallInProgress-$isDownloadApiCallInProgress"
                                                )
                                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.downloadLink?.drm?.url) || !TextUtils.isEmpty(
                                                        it?.data?.data?.head?.headData?.misc?.downloadLink?.mdn?.url
                                                    )
                                                ) {
                                                    setDownloadableContentListData(it?.data)
                                                } else {
                                                    val messageModel = MessageModel(
                                                        getString(R.string.discover_str_17),
                                                        getString(R.string.discover_str_2),
                                                        MessageType.NEGATIVE,
                                                        false
                                                    )
                                                    CommonUtils.showToast(
                                                        this@BaseActivity,
                                                        messageModel
                                                    )
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentDownloadingIndex)) {
                                                            AppDatabase.getInstance()?.downloadQueue()
                                                                ?.deleteDownloadQueueItem(
                                                                    downloadQueueList?.get(
                                                                        currentDownloadingIndex
                                                                    )?.qId!!
                                                                )
                                                            callNextDownloadableItemFromQueue()
                                                        }
                                                    }

                                                }

                                            }
                                        }

                                        com.hungama.music.data.webservice.utils.Status.LOADING -> {

                                        }

                                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                                            isDownloadApiCallInProgress = false
                                            setLog(TAG, "onErrorObserver ${it.message}")
                                        }
                                    }
                                })
                    } else {
                        isDownloadApiCallInProgress = false
                        val messageModel = MessageModel(
                            getString(R.string.toast_str_35),
                            getString(R.string.toast_message_5),
                            MessageType.NEGATIVE,
                            true
                        )
                        CommonUtils.showToast(this@BaseActivity, messageModel)
                    }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    isDownloadApiCallInProgress = false
                    if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentDownloadingIndex)) {
                        AppDatabase.getInstance()?.downloadQueue()
                            ?.deleteDownloadQueueItem(
                                downloadQueueList?.get(currentDownloadingIndex)?.qId!!
                            )
                        callNextDownloadableItemFromQueue()
                    }
                }

            }
        }
    }

    fun setDownloadableContentListData(playableContentModel: DownloadableContentModel) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var downloadUrl = ""
                if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.drm.url)) {
                    downloadUrl =
                        playableContentModel.data.head.headData.misc.downloadLink.drm.url
                } else if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.mdn.url)) {
                    downloadUrl =
                        playableContentModel.data.head.headData.misc.downloadLink.mdn.url
                }else if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.url)) {
                    downloadUrl = playableContentModel.data.head.headData.misc.url
                }

                var drmlicence = ""
                if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.drm.token)) {
                    drmlicence =
                        playableContentModel.data.head.headData.misc.downloadLink.drm.token
                } else if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.mdn.token)) {
                    drmlicence =
                        playableContentModel.data.head.headData.misc.downloadLink.mdn.token
                }

                val playableUrl = playableContentModel.data.head.headData.misc.url
                val lyricsUrl = playableContentModel.data.head.headData.misc.sl.lyric.link.toString()
                if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > currentDownloadingIndex) {
                    setLog(
                        TAG,
                        "setDownloadableContentListData:" + playableContentModel.data.head.headData.id.toString()
                    )
                    downloadQueueList.get(currentDownloadingIndex).contentId =
                        playableContentModel.data.head.headData.id.toString()
                    downloadQueueList.get(currentDownloadingIndex).downloadUrl = downloadUrl
                    downloadQueueList.get(currentDownloadingIndex).playableUrl = playableUrl
                    downloadQueueList.get(currentDownloadingIndex).lyricsUrl = lyricsUrl
                    downloadQueueList.get(currentDownloadingIndex).drmLicense = drmlicence

                    downloadQueueList.get(currentDownloadingIndex).originalAlbumName = ""
                    downloadQueueList.get(currentDownloadingIndex).podcastAlbumName = ""
                    downloadQueueList.get(currentDownloadingIndex).releaseDate =
                        playableContentModel.data.head.headData.releasedate
                    downloadQueueList.get(currentDownloadingIndex).actor =
                        "" + playableContentModel.data.head.headData.misc.actorf
                    downloadQueueList.get(currentDownloadingIndex).singer =
                        "" + playableContentModel.data.head.headData.misc.singerf
                    downloadQueueList.get(currentDownloadingIndex).lyricist =
                        playableContentModel.data.head.headData.misc.lyricist.toString()
                    downloadQueueList.get(currentDownloadingIndex).genre =
                        playableContentModel.data.head.headData.genre.toString()
                    downloadQueueList.get(currentDownloadingIndex).subGenre = ""
                    downloadQueueList.get(currentDownloadingIndex).mood =
                        playableContentModel.data.head.headData.misc.mood
                    downloadQueueList.get(currentDownloadingIndex).tempo =
                        "" + playableContentModel.data.head.headData.misc.tempo
                    downloadQueueList.get(currentDownloadingIndex).language =
                        playableContentModel.data.head.headData.misc.lang.toString()
                    downloadQueueList.get(currentDownloadingIndex).musicDirectorComposer = ""
                    downloadQueueList.get(currentDownloadingIndex).releaseYear = DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                        DateUtils.DATE_YYYY,
                        playableContentModel.data.head.headData.releasedate
                    )
                    downloadQueueList.get(currentDownloadingIndex).category = ""
                    downloadQueueList.get(currentDownloadingIndex).rating = ""
                    downloadQueueList.get(currentDownloadingIndex).cast_enabled = 0
                    downloadQueueList.get(currentDownloadingIndex).ageRating = ""
                    downloadQueueList.get(currentDownloadingIndex).criticRating =
                        playableContentModel.data.head.headData.misc.ratingCritic.toString()
                    downloadQueueList.get(currentDownloadingIndex).keywords = ""
                    downloadQueueList.get(currentDownloadingIndex).episodeNumber = ""
                    downloadQueueList.get(currentDownloadingIndex).seasonNumber = ""
                    downloadQueueList.get(currentDownloadingIndex).subtitleEnabled = 0
                    downloadQueueList.get(currentDownloadingIndex).selectedSubtitleLanguage = ""
                    downloadQueueList.get(currentDownloadingIndex).lyricsType = ""
                    downloadQueueList.get(currentDownloadingIndex).userRating = ""
                    downloadQueueList.get(currentDownloadingIndex).videoQuality = ""
                    downloadQueueList.get(currentDownloadingIndex).audioQuality = ""
                    downloadQueueList.get(currentDownloadingIndex).label = ""
                    downloadQueueList.get(currentDownloadingIndex).labelId = ""
                    downloadQueueList.get(currentDownloadingIndex).isOriginal = ""
                    downloadQueueList.get(currentDownloadingIndex).contentPayType = ""

                    downloadQueueList.get(currentDownloadingIndex).cast =
                        playableContentModel?.data?.head?.headData?.misc?.cast
                    downloadQueueList.get(currentDownloadingIndex).pid =
                        playableContentModel?.data?.head?.headData?.misc?.pid.toString()
                    downloadQueueList.get(currentDownloadingIndex).movierights =
                        playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
                    downloadQueueList.get(currentDownloadingIndex).attribute_censor_rating =
                        playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
                    downloadQueueList.get(currentDownloadingIndex).nudity =
                        playableContentModel?.data?.head?.headData?.misc?.nudity
                    downloadQueueList.get(currentDownloadingIndex).f_playcount =
                        playableContentModel?.data?.head?.headData?.misc?.playcount.toString()!!
                    downloadQueueList.get(currentDownloadingIndex).s_artist =
                        playableContentModel?.data?.head?.headData?.misc?.sArtist.toString()
                    downloadQueueList.get(currentDownloadingIndex).artist =
                        playableContentModel?.data?.head?.headData?.misc?.artist.toString()
                    downloadQueueList.get(currentDownloadingIndex).lyricsLanguage =
                        playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.lang
                    downloadQueueList.get(currentDownloadingIndex).lyricsLanguageId =
                        playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.langId.toString()
                    downloadQueueList.get(currentDownloadingIndex).lyricsFilePath = ""
                    downloadQueueList.get(currentDownloadingIndex).f_fav_count =
                        playableContentModel?.data?.head?.headData?.misc?.favCount.toString()!!
                    downloadQueueList.get(currentDownloadingIndex).synopsis =
                        playableContentModel?.data?.head?.headData?.misc?.synopsis
                    downloadQueueList.get(currentDownloadingIndex).description =
                        playableContentModel?.data?.head?.headData?.misc?.description
                    downloadQueueList.get(currentDownloadingIndex).vendor =
                        playableContentModel?.data?.head?.headData?.misc?.vendor
                    downloadQueueList.get(currentDownloadingIndex).countEraFrom =
                        playableContentModel?.data?.head?.headData?.misc?.countEraFrom
                    downloadQueueList.get(currentDownloadingIndex).countEraTo =
                        playableContentModel?.data?.head?.headData?.misc?.countEraTo
                    downloadQueueList.get(currentDownloadingIndex).skipCreditET =
                        playableContentModel?.data?.head?.headData?.misc?.skipIntro?.skipCreditET?.toInt()!!
                    downloadQueueList.get(currentDownloadingIndex).skipCreditST =
                        playableContentModel?.data?.head?.headData?.misc?.skipIntro?.skipCreditST!!.toInt()!!
                    downloadQueueList.get(currentDownloadingIndex).skipIntroET =
                        playableContentModel?.data?.head?.headData?.misc?.skipIntro?.skipIntroET!!.toInt()!!
                    downloadQueueList.get(currentDownloadingIndex).skipIntroST =
                        playableContentModel?.data?.head?.headData?.misc?.skipIntro?.skipIntroST!!.toInt()!!
                    downloadQueueList.get(currentDownloadingIndex).userId =
                        SharedPrefHelper.getInstance().getUserId()
                    downloadQueueList.get(currentDownloadingIndex).thumbnailPath = ""
                    downloadQueueList.get(currentDownloadingIndex).heading = ""
                    downloadQueueList.get(currentDownloadingIndex).contentShareLink =
                        playableContentModel.data.head.headData.misc.share

                    if (playableContentModel?.data?.head?.headData?.type != null && !TextUtils.isEmpty(
                            playableContentModel?.data?.head?.headData?.type.toString()
                        )
                    ) {
                        downloadQueueList.get(currentDownloadingIndex).type =
                            playableContentModel?.data?.head?.headData?.type!!
                    }

                    if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.title!!)) {
                        downloadQueueList.get(currentDownloadingIndex).title =
                            playableContentModel?.data?.head?.headData?.title
                    }

                    if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.subtitle!!)) {
                        downloadQueueList.get(currentDownloadingIndex).subTitle =
                            playableContentModel?.data?.head?.headData?.subtitle
                    }

                    if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.playble_image!!)) {
                        downloadQueueList.get(currentDownloadingIndex).image =
                            playableContentModel?.data?.head?.headData?.playble_image
                    }else if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image!!)) {
                        downloadQueueList.get(currentDownloadingIndex).image =
                            playableContentModel?.data?.head?.headData?.image
                    }

                    if (playableContentModel?.data?.head?.headData?.duration != null && !TextUtils.isEmpty(
                            playableContentModel?.data?.head?.headData?.duration?.toString()
                        )
                    ) {
                        downloadQueueList.get(currentDownloadingIndex).duration =
                            playableContentModel.data.head.headData.duration.toLong()
                    }

                    if (playableContentModel?.data?.head?.headData?.misc?.explicit != null && !TextUtils.isEmpty(
                            playableContentModel?.data?.head?.headData?.misc?.explicit!!.toString()
                        )
                    ) {
                        downloadQueueList.get(currentDownloadingIndex).explicit =
                            playableContentModel?.data?.head?.headData?.misc?.explicit!!
                    }

                    if (playableContentModel?.data?.head?.itype != null && !TextUtils.isEmpty(
                            playableContentModel?.data?.head?.itype!!.toString()
                        )
                    ) {
                        downloadQueueList.get(currentDownloadingIndex).itype =
                            playableContentModel?.data?.head?.itype!!
                    }

                    downloadQueueList.get(currentDownloadingIndex).restrictedDownload =
                        playableContentModel.data.head.headData.misc.restricted_download

                    AppDatabase.getInstance()?.downloadQueue()
                        ?.updateDownloadQueueItem(downloadQueueList.get(currentDownloadingIndex))

                    callOfflineSongEventAnalytics(
                        downloadQueueList.get(currentDownloadingIndex),
                        playableContentModel
                    )

                    if (downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.NONE.value
                        || downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.AUDIO.value
                        || downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.PODCAST.value
                        || downloadQueueList.get(currentDownloadingIndex).contentType == ContentTypes.RADIO.value
                    ) {
                        val audioUrl =
                            arrayOf<String>(downloadQueueList.get(currentDownloadingIndex).downloadUrl!!)
                        Data.sampleUrls = audioUrl
                        enqueueDownload()
                        setLog(TAG, "enqueueDownload:")
                    } else {
                        val renderersFactory = DemoUtil.buildRenderersFactory( /* context= */
                            this@BaseActivity,
                            false
                        )

                        val track = Track()
                        track.id = downloadQueueList.get(currentDownloadingIndex).contentId?.toLong()!!
                        track.title = downloadQueueList.get(currentDownloadingIndex).title
                        track.subTitle = downloadQueueList.get(currentDownloadingIndex).subTitle
                        track.image = downloadQueueList.get(currentDownloadingIndex).image
                        track.url = downloadQueueList.get(currentDownloadingIndex).downloadUrl
                        track.drmlicence = downloadQueueList.get(currentDownloadingIndex).drmLicense

                        //val currentDownloadingMediaItem: MediaItem = exoPlayer.setMediaItem(track, currentDownloadingIndex)
                        val trackList = arrayListOf<Track>(track)
                        IntentUtil.addToIntent(exoPlayer.setMediaItem(trackList), intent)
                        val mediaItems =
                            CommonUtils.createMediaItems(intent, downloadTracker, trackList)
                        if (!mediaItems.isNullOrEmpty()) {
                            var currentDownloadingMediaItem: MediaItem? = null
                            for (items in mediaItems.iterator()) {
                                if (items.playbackProperties != null
                                    && items.playbackProperties?.uri.toString().equals(track.url, true)
                                ) {
                                    currentDownloadingMediaItem = items
                                }
                            }


                            if (downloadTracker != null && currentDownloadingMediaItem != null) {
                                val isDownloaded =
                                    downloadTracker?.isDownloaded(currentDownloadingMediaItem)
                                if (isDownloaded!!) {
                                    val downloadState =
                                        downloadTracker?.getDownloadState(currentDownloadingMediaItem)
                                    if (downloadState == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING) {

                                    } else if (downloadState == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED) {
                                        val downloadedItem =
                                            AppDatabase?.getInstance()?.downloadedAudio()
                                                ?.findByContentId(
                                                    downloadQueueList.get(currentDownloadingIndex).contentId!!
                                                )

                                        if (downloadedItem != null) {
                                            downloadQueueList.get(currentDownloadingIndex).downloadedFilePath =
                                                downloadedItem.downloadedFilePath
                                            if (downloadedItem.totalDownloadBytes < 0) {
                                                downloadQueueList.get(currentDownloadingIndex).totalDownloadBytes =
                                                    0
                                            } else {
                                                downloadQueueList.get(currentDownloadingIndex).totalDownloadBytes =
                                                    downloadedItem.totalDownloadBytes
                                            }

                                            if (downloadedItem.downloadedBytes < 0) {
                                                downloadedItem.downloadedBytes = 0
                                            } else {
                                                downloadQueueList.get(currentDownloadingIndex).downloadedBytes =
                                                    downloadedItem.downloadedBytes
                                            }

                                            downloadQueueList.get(currentDownloadingIndex).downloadStatus =
                                                downloadedItem.downloadStatus
                                            downloadQueueList.get(currentDownloadingIndex).downloadNetworkType =
                                                downloadedItem.downloadNetworkType
                                            downloadQueueList.get(currentDownloadingIndex).createdDT =
                                                downloadedItem.createdDT
                                            downloadQueueList.get(currentDownloadingIndex).percentDownloaded =
                                                100
                                            addOrUpdateDownloadedAudio(
                                                downloadQueueList.get(
                                                    currentDownloadingIndex
                                                )
                                            )

                                            if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentDownloadingIndex)) {
                                                setLog(
                                                    TAG,
                                                    "setDownloadableContentListData: deleteDownloadQueueItem 1"
                                                )
                                                AppDatabase.getInstance()?.downloadQueue()
                                                    ?.deleteDownloadQueueItem(
                                                        downloadQueueList.get(currentDownloadingIndex).qId!!
                                                    )
                                            }
                                            callNextDownloadableItemFromQueue()
                                        }
                                        return@launch
                                    }
                                } else {
                                    val dwState = downloadTracker?.getDownloadState(
                                        downloadQueueList.get(currentDownloadingIndex).downloadUrl
                                    )
                                    if (dwState == -1 || dwState == androidx.media3.exoplayer.offline.Download.STATE_FAILED) {
                                        if (dwState == androidx.media3.exoplayer.offline.Download.STATE_FAILED) {
                                            removeVideoContentFromDownload(
                                                Uri.parse(
                                                    downloadQueueList.get(
                                                        currentDownloadingIndex
                                                    ).downloadUrl
                                                )
                                            )
                                        }

                                        setLog(
                                            TAG,
                                            "DRM toggleDownload currentDownloadingMediaItem:${currentDownloadingMediaItem}"
                                        )
                                        downloadTracker?.setStopReasonToAllDownload(stopReasonRemove, Uri.parse(downloadQueueList.get(currentDownloadingIndex).downloadUrl))
                                        downloadTracker?.toggleDownload(
                                            supportFragmentManager,
                                            currentDownloadingMediaItem,
                                            renderersFactory
                                        )
                                    } else if (!isVideoDownloadActive() && (dwState == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING
                                                || dwState == androidx.media3.exoplayer.offline.Download.STATE_QUEUED)
                                    ) {
                                        resumeAllVideoDownloads()
                                    }
                                }
                            }
                        }
                    }

                } else {
                    setLog(TAG, "setDownloadableContentListData: not event added")
                }
            }catch (e:Exception){

            }
        }

    }

    private fun callOfflineSongEventAnalytics(
        downloadQueue: DownloadQueue,
        playableContentModel: DownloadableContentModel
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val eventModel = HungamaMusicApp.getInstance().getEventData("" + downloadQueue?.contentId)
            setLog(TAG, "playableContentModel--:${playableContentModel}")
            setLog(
                TAG,
                "callOfflineSongEventAnalytics eventModel sourceName:${eventModel?.sourceName}  bucketName: ${eventModel?.bucketName} downloadQueue.audioQuality:${eventModel.audioQuality}"
            )
            if (eventModel != null) {
                val hashMap = HashMap<String, String>()
                hashMap.put(
                    EventConstant.ACTOR_EPROPERTY,Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.actorf)
                )
                hashMap.put(
                    EventConstant.ALBUM_ID_EPROPERTY,
                    "" + playableContentModel?.data?.head?.headData?.id
                )
                hashMap.put(EventConstant.ALBUMNAME_EPROPERTY, "" + downloadQueue?.pName)
                var newContentId=playableContentModel?.data?.head?.headData?.id
                var contentIdData=newContentId?.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" +contentIdData )
                hashMap.put(
                    EventConstant.CONTENTTYPE_EPROPERTY,
                    "" + Utils.getContentTypeName("" + downloadQueue.type)
                )
               setLog("downloadQueue12","type${downloadQueue.type}")


                if (!TextUtils.isEmpty(eventModel.audioQuality)) {
                    hashMap.put(EventConstant.DOWNLOADQUALITY_EPROPERTY, "" + eventModel.audioQuality)
                } else {
                    hashMap.put(EventConstant.DOWNLOADQUALITY_EPROPERTY, "Low")
                }

                hashMap.put(EventConstant.EXTRA_EPROPERTY, "")
                hashMap.put(
                    EventConstant.GENRE_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.genre)
                )
                hashMap.put(
                    EventConstant.LANGUAGE_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.lang)
                )
                hashMap.put(
                    EventConstant.LYRICIST_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.lyricist)
                )
                hashMap.put(
                    EventConstant.MOOD_EPROPERTY,
                    "" + playableContentModel?.data?.head?.headData?.misc?.mood
                )
                hashMap.put(
                    EventConstant.MUSICDIRECTOR_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.musicdirectorf)
                )

                hashMap.put(
                    EventConstant.PLAYLIST_ID_EPROPERTY,
                    TextUtils.join(",", playableContentModel.data.head.headData.misc.pid)
                )
                hashMap.put(
                    EventConstant.PLAYLIST_NAME_EPROPERTY,
                    Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.pName)
                )
                hashMap.put(
                    EventConstant.PODCAST_ALBUM_NAME_EPROPERTY,
                    "" + downloadQueue.podcastAlbumName
                )
                hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "" + eventModel?.podcast_host)
                hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "" + eventModel?.episodeNumber)
                hashMap.put(
                    EventConstant.RELEASE_DATE_EPROPERTY,
                    "" + DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                        DateUtils.DATE_FORMAT_DD_MM_YYYY_slash,
                        playableContentModel?.data?.head?.headData?.releasedate
                    )
                )
                hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel?.season_Number)
                hashMap.put(
                    EventConstant.PTYPE_EPROPERTY,
                    "" + playableContentModel?.data?.head?.headData?.type
                )
                hashMap.put(
                    EventConstant.CONNECTION_TYPE_EPROPERTY,
                    ConnectionUtil(this@BaseActivity).networkType
                )
                hashMap.put(
                    EventConstant.SINGER_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.singerf)
                )
                hashMap.put(
                    EventConstant.SONG_NAME_EPROPERTY,
                    "" + playableContentModel?.data?.head?.headData?.title
                )

                hashMap.put(
                    EventConstant.SUB_GENRE_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.subgenre_name)
                )

                try {
                    val model =
                        SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
                    if (model != null && model.data?.subscription != null && model.data?.profile_app_config != null) {
                        hashMap.put(
                            EventConstant.SUBSCRIPTION_STATUS_EPROPERTY,
                            "" + model.data?.profile_app_config?.profile_status
                        )
                        hashMap.put(
                            EventConstant.CONTENT_PAY_TYPE_EPROPERTY,
                            "" + model.data?.profile_app_config?.profileTypeId
                        )
                    } else {
                        hashMap.put(EventConstant.SUBSCRIPTION_STATUS_EPROPERTY, "Free User")
                        hashMap.put(EventConstant.CONTENT_PAY_TYPE_EPROPERTY, "1000")
                    }

                    if (downloadQueue?.planType == PlanTypes.RENTAL.value) {
                        val contentData =
                            CommonUtils.getRentedContentData("" + downloadQueue?.contentId)
                        if (contentData != null) {
                            hashMap.put(
                                EventConstant.CONTENT_PAY_TYPE_EPROPERTY,
                                "" + contentData.paymentSource
                            )
                        }
                    }
                } catch (e: Exception) {
                    hashMap.put(EventConstant.SUBSCRIPTION_STATUS_EPROPERTY, "Free User")
                    hashMap.put(EventConstant.CONTENT_PAY_TYPE_EPROPERTY, "1000")
                }


                hashMap.put(
                    EventConstant.TEMPO_EPROPERTY,
                     Utils.arrayToString(playableContentModel?.data?.head?.headData?.misc?.tempo)
                )
                hashMap.put(
                    EventConstant.YEAROFRELEASE_EPROPERTY,
                    "" + DateUtils.convertDate(
                        DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                        DateUtils.DATE_YYYY,
                        playableContentModel?.data?.head?.headData?.releasedate
                    )
                )

                hashMap.put(
                    EventConstant.SOURCE_EPROPERTY, ""+ downloadQueue.source)

                hashMap.put(
                    EventConstant.SOURCE_NAME_EPROPERTY, ""+ DetailPages.valueOf(downloadQueue.pType))

                setLog(TAG, "callOfflineSongEventAnalytics hashMap:$hashMap")
                EventManager.getInstance().sendEvent(OfflinedSongEvent(hashMap))
            } else {
                setLog(TAG, "callOfflineSongEventAnalytics: playlist model null in hashmap")
            }
        }

    }

    fun enqueueDownload() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url: String = Data.sampleUrls.get(0)
                //val url: String = ""
                var filePath: String = ""
                var contentTitle = "-"
                if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > currentDownloadingIndex) {
                    filePath =
                        Data.getSaveAudioDir(this@BaseActivity).toString() + Data.getSaveAudioFileName(
                            downloadQueueList.get(currentDownloadingIndex).contentId
                        )
                    contentTitle = downloadQueueList?.get(currentDownloadingIndex)?.title.toString()
                } else {
                    filePath =
                        Data.getSaveAudioDir(this@BaseActivity).toString() + Data.getNameFromUrl(url)
                }
                request = Request(url, filePath)
                //request.setExtras(getExtrasForRequest(request!!))
                request?.extras = getExtrasForRequest(request!!, url, contentTitle)!!
                val deeplinkUrl = "https://www.hungama.com/library/music/downloaded-songs"
                val intent = CommonUtils.getDeeplinkIntentData(Uri.parse(deeplinkUrl))
                intent.setClass(this@BaseActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                val fetchConfiguration = FetchConfiguration.Builder(this@BaseActivity)
                    .setAutoRetryMaxAttempts(3)
                    .setNotificationManager(object : DefaultFetchNotificationManager(this@BaseActivity) {
                        override fun getFetchInstanceForNamespace(namespace: String): Fetch {
                            return fetch!!
                        }

                        override fun updateNotification(
                            notificationBuilder: NotificationCompat.Builder,
                            downloadNotification: DownloadNotification,
                            context: Context, contentIntent: Intent?
                        ) {

                            /*val currentIndex = getCurrentDownloadContentByUrl(url)
                            if(downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentIndex)){
                                downloadNotification.title = downloadQueueList.get(currentDownloadingIndex).title.toString()
                                notificationBuilder.setContentTitle(downloadQueueList.get(currentDownloadingIndex).title)
                            }*/
                            super.updateNotification(
                                notificationBuilder,
                                downloadNotification,
                                context,
                                intent
                            )


                        }

                    })
                    .build()
                fetch = Fetch.getInstance(fetchConfiguration)
                fetch?.attachFetchObserversForDownload(request!!.id, this@BaseActivity)
                    ?.enqueue(request!!,
                        { result -> request = result },
                        { result ->
                            setLog(
                                "AudioDownload", "SingleDownloadActivity Error: %1\$s$result"
                            )
                        })
                if (fetchMusicDownloadListener != null) {
                    fetch?.addListener(fetchMusicDownloadListener!!)
                }
                /*fetch.hasActiveDownloads(true) {
                    hasActiveAudioDownload = it
                }*/
            } catch (e: Exception) {

            }
        }


    }

    private fun getExtrasForRequest(
        request: Request,
        downloadUrl: String,
        contentTitle: String
    ): Extras? {
        val extras = MutableExtras()
        if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserId()!!)) {
            extras.putString("userId", SharedPrefHelper.getInstance().getUserId()!!)
        }
        if (!TextUtils.isEmpty(downloadUrl)) {
            extras.putString("downloadUrl", downloadUrl)
        }
        if (!TextUtils.isEmpty(contentTitle)) {
            extras.putString("contentTitle", contentTitle)
        }
        return extras
    }

    override fun onChanged(data: Download, reason: Reason) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = data.extras.getString("downloadUrl", "")
            val contentIndex = getCurrentDownloadContentByUrl(url.toString())
            if (reason === Reason.DOWNLOAD_COMPLETED || reason === Reason.DOWNLOAD_CANCELLED
                || reason === Reason.DOWNLOAD_REMOVED || reason === Reason.DOWNLOAD_DELETED
                || reason === Reason.DOWNLOAD_ERROR || reason === Reason.DOWNLOAD_BLOCK_UPDATED
            ) {
                if (reason === Reason.DOWNLOAD_COMPLETED) {
                    hasActiveAudioDownload = false
                    if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > contentIndex)) {
                        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()
                            && downloadQueueList?.get(contentIndex)?.contentId.equals(
                                songDataList?.get(
                                    nowPlayingCurrentIndex()
                                )?.id.toString()
                            )
                        ) {
                            onPlayerContentDownloadStatusChange(data.status.value)
                        }
                        downloadQueueList?.get(contentIndex)?.downloadedFilePath = data.file
                        if (data.total < 0) {
                            downloadQueueList?.get(contentIndex)?.totalDownloadBytes = 0
                        } else {
                            downloadQueueList?.get(contentIndex)?.totalDownloadBytes = data.total
                        }
                        if (data.downloaded < 0) {
                            downloadQueueList?.get(contentIndex)?.downloadedBytes = 0
                        } else {
                            downloadQueueList?.get(contentIndex)?.downloadedBytes = data.downloaded
                        }

                        downloadQueueList?.get(contentIndex)?.downloadStatus = data.status.value
                        downloadQueueList?.get(contentIndex)?.downloadNetworkType =
                            data.networkType.value
                        downloadQueueList?.get(contentIndex)?.createdDT = data.created
                        downloadQueueList.get(contentIndex).percentDownloaded = 100
                        addOrUpdateDownloadedAudio(downloadQueueList.get(contentIndex))

                        if (SharedPrefHelper.getInstance().get(PrefConstant.FIRST_DOWNLOAD, true) && !downloadQueueList?.get(contentIndex)?.contentId?.isNullOrEmpty()!!) {
                            /* Track Events in real time */
                            val eventValue: MutableMap<String, Any> = HashMap()
//                        eventValue.put(
//                            AFInAppEventParameterName.CONTENT_TYPE,
//                            Utils?.getContentTypeName("" + downloadQueueList?.get(contentIndex).contentType)
//                        )
                            eventValue.put(
                                AFInAppEventParameterName.CONTENT_ID,
                                "" + downloadQueueList?.get(contentIndex)?.contentId
                            )
                            AppsFlyerLib.getInstance().logEvent(
                                HungamaMusicApp.getInstance().applicationContext!!,
                                EventConstant.AF_FIRST_DOWNLOAD,
                                eventValue
                            )
                            SharedPrefHelper.getInstance().save(PrefConstant.FIRST_DOWNLOAD, false)
                        }
                    }
                    /*val messageModel = MessageModel(getString(R.string.toast_head_11), getString(R.string.toast_message_11),
                        MessageType.NEUTRAL, true)
                    CommonUtils.showToast(this, messageModel)*/


                } else if (reason == Reason.DOWNLOAD_STARTED) {
                    hasActiveAudioDownload = true
                }
                if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > contentIndex)) {
                    AppDatabase.getInstance()?.downloadQueue()
                        ?.deleteDownloadQueueItem(downloadQueueList?.get(contentIndex)?.qId!!)
                }
                callNextDownloadableItemFromQueue()

            } else if (reason === Reason.DOWNLOAD_ADDED || reason === Reason.DOWNLOAD_QUEUED || reason === Reason.DOWNLOAD_STARTED) {
                if (downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > contentIndex)) {
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()
                        && downloadQueueList?.get(contentIndex)?.contentId.equals(
                            songDataList?.get(
                                nowPlayingCurrentIndex()
                            )?.id.toString()
                        )
                    ) {
                        onPlayerContentDownloadStatusChange(data.status.value)
                    }
                    downloadQueueList?.get(contentIndex)?.downloadManagerId = data.id
                    downloadQueueList?.get(contentIndex)?.downloadStatus = data.status.value

                    if (reason == Reason.DOWNLOAD_STARTED) {
                        hasActiveAudioDownload = true
                        downloadQueueList.get(contentIndex).percentDownloaded = data.progress

                        setLog(TAG, "onChanged:  PROGRESS + " + data.progress)
                    }
                    AppDatabase.getInstance()?.downloadQueue()
                        ?.updateDownloadQueueItem(downloadQueueList.get(contentIndex))
                }
            }
            if (onDownloadQueueItemChanged != null) {
                onDownloadQueueItemChanged?.onDownloadQueueItemChanged(data, reason)
            }
        }

    }

    interface OnDownloadQueueItemChanged {
        fun onDownloadQueueItemChanged(data: Download, reason: Reason)
    }

    interface OnDownloadVideoQueueItemChanged {
        fun onDownloadVideoQueueItemChanged(
            downloadManager: DownloadManager,
            download: androidx.media3.exoplayer.offline.Download
        )

        fun onDownloadProgress(
            downloads: List<androidx.media3.exoplayer.offline.Download?>?,
            progress: Int,
            currentExoDownloadPosition: Int
        )

        fun onDownloadsPausedChanged(downloadManager: DownloadManager, downloadsPaused: Boolean?)
    }

    private fun addOrUpdateDownloadedAudio(downloadQueue: DownloadQueue) {
        CoroutineScope(Dispatchers.IO).launch {
            val da = DownloadedAudio()
            da.contentId = downloadQueue.contentId
            da.title = downloadQueue.title
            da.subTitle = downloadQueue.subTitle
            da.playableUrl = downloadQueue.playableUrl
            da.downloadUrl = downloadQueue.downloadUrl
            da.lyricsUrl = downloadQueue.lyricsUrl
            da.downloadManagerId = downloadQueue.downloadManagerId

            da.downloadedFilePath = downloadQueue.downloadedFilePath
            if (downloadQueue.totalDownloadBytes < 0) {
                da.totalDownloadBytes = 0
            } else {
                da.totalDownloadBytes = downloadQueue.totalDownloadBytes
            }
            if (downloadQueue.downloadedBytes < 0) {
                downloadQueue.downloadedBytes = 0
            } else {
                da.downloadedBytes = downloadQueue.downloadedBytes
            }

            da.downloadStatus = downloadQueue.downloadStatus
            da.downloadNetworkType = downloadQueue.downloadNetworkType
            da.createdDT = downloadQueue.createdDT

            da.parentId = downloadQueue.parentId
            da.pName = downloadQueue.pName
            da.pType = downloadQueue.pType
            da.contentType = downloadQueue.contentType
            da.originalAlbumName = downloadQueue.originalAlbumName
            da.podcastAlbumName = downloadQueue.podcastAlbumName
            da.releaseDate = downloadQueue.releaseDate
            da.actor = downloadQueue.actor
            da.singer = downloadQueue.singer
            da.lyricist = downloadQueue.lyricist
            da.genre = downloadQueue.genre
            da.subGenre = downloadQueue.subGenre
            da.mood = downloadQueue.mood
            da.tempo = downloadQueue.tempo
            da.language = downloadQueue.language
            da.musicDirectorComposer = downloadQueue.musicDirectorComposer
            da.releaseYear = downloadQueue.releaseYear
            da.category = downloadQueue.category
            da.rating = downloadQueue.rating
            da.cast_enabled = downloadQueue.cast_enabled
            da.ageRating = downloadQueue.ageRating
            da.criticRating = downloadQueue.criticRating
            da.keywords = downloadQueue.keywords
            da.episodeNumber = downloadQueue.episodeNumber
            da.seasonNumber = downloadQueue.seasonNumber
            da.subtitleEnabled = downloadQueue.subtitleEnabled
            da.selectedSubtitleLanguage = downloadQueue.selectedSubtitleLanguage
            da.lyricsType = downloadQueue.lyricsType
            da.userRating = downloadQueue.userRating
            da.videoQuality = downloadQueue.videoQuality
            da.audioQuality = downloadQueue.audioQuality
            da.label = downloadQueue.label
            da.labelId = downloadQueue.labelId
            da.isOriginal = downloadQueue.isOriginal
            da.contentPayType = downloadQueue.contentPayType

            da.itype = downloadQueue.itype
            da.type = downloadQueue.type
            da.image = downloadQueue.image
            da.duration = downloadQueue.duration
            da.cast = downloadQueue.cast
            da.explicit = downloadQueue.explicit
            da.pid = downloadQueue.pid
            da.movierights = downloadQueue.movierights
            da.attribute_censor_rating = downloadQueue.attribute_censor_rating
            da.nudity = downloadQueue.nudity
            da.playcount = downloadQueue.playcount
            da.f_playcount = downloadQueue.playcount.toString()
            da.s_artist = downloadQueue.s_artist
            da.artist = downloadQueue.artist
            da.lyricsLanguage = downloadQueue.lyricsLanguage
            da.lyricsLanguageId = downloadQueue.lyricsLanguageId
            da.lyricsFilePath = downloadQueue.lyricsFilePath
            da.fav_count = downloadQueue.fav_count
            da.f_fav_count = downloadQueue.fav_count.toString()
            da.synopsis = downloadQueue.synopsis
            da.description = downloadQueue.description
            da.vendor = downloadQueue.vendor
            da.countEraFrom = downloadQueue.countEraFrom
            da.countEraTo = downloadQueue.countEraTo
            da.skipCreditET = downloadQueue.skipCreditET
            da.skipCreditST = downloadQueue.skipCreditST
            da.skipIntroET = downloadQueue.skipIntroET
            da.skipIntroST = downloadQueue.skipIntroST
            da.userId = downloadQueue.userId
            da.thumbnailPath = downloadQueue.thumbnailPath

            da.pSubName = downloadQueue.pSubName
            da.pReleaseDate = downloadQueue.pReleaseDate
            da.pDescription = downloadQueue.pDescription
            da.pNudity = downloadQueue.pNudity
            da.pRatingCritics = downloadQueue.pRatingCritics
            da.pMovieRights = downloadQueue.pMovieRights
            da.pGenre = downloadQueue.pGenre
            da.pLanguage = downloadQueue.pLanguage
            da.pImage = downloadQueue.pImage
            da.heading = downloadQueue.heading
            da.downloadAll = downloadQueue.downloadAll

            da.f_playcount = downloadQueue.f_playcount
            da.f_fav_count = downloadQueue.f_fav_count
            da.planName = downloadQueue.planName
            da.planType = downloadQueue.planType
            da.percentDownloaded = downloadQueue.percentDownloaded
            da.downloadRetry = downloadQueue.downloadRetry
            da.isFavorite = downloadQueue.isFavorite
            da.contentStreamDate = downloadQueue.contentStreamDate
            da.contentStreamDuration = downloadQueue.contentStreamDuration
            da.contentStartDate = downloadQueue.contentStartDate
            da.contentExpiryDate = downloadQueue.contentExpiryDate
            da.contentPlayValidity = downloadQueue.contentPlayValidity
            da.drmLicense = downloadQueue.drmLicense
            da.contentShareLink = downloadQueue.contentShareLink
            da.restrictedDownload = downloadQueue.restrictedDownload
            da.downloadManagerExoPlayerId = downloadQueue.downloadManagerExoPlayerId
            da.source = downloadQueue.source

            if(da.title!!.isNotEmpty()) {
                AppDatabase.getInstance()?.downloadedAudio()?.insertOrReplace(da)
            }
            setPostDownloadedContent(da.contentId, da.type!!, true)
            val contentArtworkFileName =
                Data.getSaveAudioDir(applicationContext)
                    .toString() + Data.getSaveAudioThumbnailFileName(da.contentId.toString())
            saveThumbnail(
                this@BaseActivity,
                da?.image.toString(),
                da.contentId.toString(),
                contentArtworkFileName,
                false
            )
            val parentArtworkFileName = Data.getSaveAudioDir(applicationContext)
                .toString() + Data.getSaveAudioParentThumbnailFileName(da.parentId.toString())
            val file = File(parentArtworkFileName)
            if (!file.exists()) {
                saveThumbnail(
                    this@BaseActivity,
                    da?.image.toString(),
                    da.parentId.toString(),
                    parentArtworkFileName,
                    true
                )
            } else {
                AppDatabase.getInstance()?.downloadedAudio()?.updateDownloadedImageParentThumbnailPath(
                    parentArtworkFileName.toString(),
                    da.parentId
                )
            }
            if (!TextUtils.isEmpty(da.lyricsUrl)) {
                val fileWithExtension = File(da.lyricsUrl).extension
                val contentLrcFileName =
                    Data.getSaveAudioDir(applicationContext).toString() + Data.getSaveAudioLrcFileName(
                        da.contentId.toString() + "." + fileWithExtension
                    )
                downloadFile(da.lyricsUrl.toString(), contentLrcFileName, da.contentId.toString())
            }
            try {
                val contentTypes:Array<Int> = arrayOf(ContentTypes.AUDIO.value, ContentTypes.PODCAST.value)
                val allDownloadItemList = AppDatabase.getInstance()?.downloadedAudio()?.getContentsByContentType(contentTypes)
                if (!allDownloadItemList.isNullOrEmpty()){
//                    setLog("audioDownload", "BaseActivity-addOrUpdateDownloadedAudio-allDownloadItemList?.size-${allDownloadItemList?.size}")
//                    setLog("audioDownload", "BaseActivity-addOrUpdateDownloadedAudio-getMaxDownloadContentSize-${CommonUtils.getAvailableDownloadContentSize(this@BaseActivity)}")
                    SharedPrefHelper.getInstance().setTotalDownloadedAudioContent(allDownloadItemList.size)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }



    }

    fun playContentOnline(songDataList: ArrayList<Track>?, index: Int, isPause: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!songDataList.isNullOrEmpty() && songDataList.size > index) {
                    setLog(
                        "preCatchContent",
                        "BaseActivity-playContentOnline-track.id-${songDataList?.get(index)?.id}  track.url-${
                            songDataList?.get(index)?.url
                        }"
                    )
                    if (TextUtils.isEmpty(songDataList?.get(index)?.url)) {
                        setLog("isPause", "BaseActivity-playContentOnline-1-isPause-$isPause")
                        setUpPlayableContentListViewModel(
                            songDataList.get(index).id,
                            songDataList?.get(index)?.playerType?.toInt(),
                            isPause
                        )
                    } else {
                        setLog(
                            "preCatchContent",
                            "BaseActivity-playContentOnline-PlayDirectWithoutApiCall-track.id-${
                                songDataList?.get(index)?.id
                            }  track.url-${songDataList?.get(index)?.url}"
                        )
                        val playableContentModel = PlayableContentModel()
                        playableContentModel.data?.head?.headData?.id =
                            songDataList?.get(index)?.id.toString()
                        playableContentModel.data?.head?.headData?.misc?.url =
                            songDataList?.get(index)?.url.toString()
                        playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url =
                            songDataList?.get(index)?.url.toString()
                        playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                            songDataList?.get(index)?.drmlicence.toString()
                        playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                            songDataList?.get(index)?.songLyricsUrl
                        this@BaseActivity.isPause = isPause
                        setLog(
                            "ContentOffline",
                            "playContentOfflineDeviceSongs: ${playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link} title=> ${playableContentModel.data?.head?.headData?.misc?.url}"
                        )
                        setLog("isPause", "BaseActivity-playContentOnline-2-isPause-$isPause")
                        updateSongUrl(playableContentModel, isPause)
                        setLog("preCatchApiCall", "BaseActitvity-playContentOnline")
                        preCatchApiCall(Companion.songDataList, index, false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    fun playContentOffline(songDataList: ArrayList<Track>?, index: Int, isPause: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > index) {
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                        songDataList?.get(index)!!.id!!.toString()
                    )
                    val playableContentModel = PlayableContentModel()
                    playableContentModel.data?.head?.headData?.id = downloadedAudio?.contentId!!
                    playableContentModel.data?.head?.headData?.misc?.url =
                        downloadedAudio?.downloadedFilePath!!
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url =
                        downloadedAudio?.downloadUrl!!
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                        downloadedAudio?.drmLicense!!
                    playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                        downloadedAudio?.lyricsUrl
                    this@BaseActivity.isPause = isPause
                    setLog(
                        "ContentOffline",
                        "setSongLyricsData playContentOfflineDeviceSongs: ${playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link} title=> ${playableContentModel.data?.head?.headData?.title}"
                    )
                    updateSongUrl(playableContentModel, isPause)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun playContentOfflineDeviceSongs(
        songDataList: ArrayList<Track>?,
        index: Int,
        isPause: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > index) {
                    val playableContentModel = PlayableContentModel()
                    playableContentModel.data?.head?.headData?.id =
                        songDataList?.get(index)?.id.toString()
                    playableContentModel.data?.head?.headData?.misc?.url =
                        songDataList?.get(index)?.url.toString()
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url = ""
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token = ""
                    playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link = ""
                    this@BaseActivity.isPause = isPause
                    setLog(
                        "OfflineSongs",
                        "setSongLyricsData playContentOfflineDeviceSongs: ${playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link} title=> ${playableContentModel.data?.head?.headData?.title}"
                    )
                    updateSongUrl(playableContentModel, isPause)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    private fun onPlayerContentDownloadStatusChange(status: Int){
        if (onSwipablePlayerListener != null) {
            onSwipablePlayerListener?.onDownloadContentStateChange(status)
            onMusicPlayerThreeDotMenuListener?.onDownloadContentStateChange(status)
        }
    }

    private fun setPostDownloadedContent(contentId: String?, type: Int?, isDownloaed: Boolean) {
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "BaseActivity-setPostDownloadedContent-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline(false)) {
            val jsonObject = JSONObject()
            jsonObject.put("contentId", contentId)
            jsonObject.put("typeId", type)
            jsonObject.put("action", isDownloaed)
            jsonObject.put("module", Constant.MODULE_DOWNLOAD)

            userViewModelBookmark?.callBookmarkApi(application, jsonObject.toString())
        }
    }

    override fun onDownloadsChanged(
        downloadManager: DownloadManager,
        download: androidx.media3.exoplayer.offline.Download
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val contentIndex = getCurrentDownloadContentByUrl(download.request.uri.toString())
            setLog("OnExoDownload", "BaseActivity-onDownloadsChanged-uri-${download.request.uri}")
            setLog("OnExoDownload", "BaseActivity-onDownloadsChanged-downloadsPaused-${downloadManager.downloadsPaused}")
            setLog("OnExoDownload", "BaseActivity-onDownloadsChanged-download.state-${download.state}")
            setLog("OnExoDownload", "BaseActivity-onDownloadsChanged-download.stopReason-${download.stopReason}")
            //setLog("OnExoDownload1", download.toString())
            /*if (downloadManager.downloadsPaused!!) {
                for (i in downloadManager.currentDownloads?.indices!!) {
                    if (downloadQueueList != null && downloadQueueList.size > 0) {
                        for (k in downloadQueueList.indices) {
                            if (downloadManager.currentDownloads.get(i).request.uri.toString().equals(
                                    downloadQueueList.get(
                                        k
                                    ).downloadUrl
                                )
                            ) {
                                AppDatabase.getInstance()?.downloadQueue()
                                    ?.updateQueueItemDownloadStatus(
                                        downloadQueueList?.get(k)?.qId!!,
                                        Status.PAUSED.value
                                    )
                            }
                        }
                    }
                }
                hasActiveAudioDownload = false
                return
            }*/
            setLog("videoDownload", "BaseActivity-onDownloadsChanged-download.state-${download.state}-downloadManagerExoPlayerId-${download.request.id}")
            when (download.state) {
                androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING -> {
                    val state: Int = Status.DOWNLOADING.value
                    if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > contentIndex) {
                        if (!songDataList.isNullOrEmpty() && songDataList.size > nowPlayingCurrentIndex()
                            && downloadQueueList.get(contentIndex).contentId.equals(
                                songDataList.get(nowPlayingCurrentIndex()).id.toString()
                            )
                        ) {
                            onPlayerContentDownloadStatusChange(state)
                        }
                        hasActiveAudioDownload = true
                        downloadQueueList.get(contentIndex).downloadManagerId = 0
                        downloadQueueList.get(contentIndex).downloadManagerExoPlayerId = download.request.id
                        setLog("videoDownload", "BaseActivity-onDownloadsChanged-STATE_DOWNLOADING-downloadManagerExoPlayerId-${download.request.id}")
                        downloadQueueList.get(contentIndex).downloadStatus = state
                        downloadQueueList.get(contentIndex).percentDownloaded =
                            download.percentDownloaded.toInt()
                        AppDatabase.getInstance()?.downloadQueue()
                            ?.updateDownloadQueueItem(downloadQueueList.get(contentIndex))
                    }
                }
                androidx.media3.exoplayer.offline.Download.STATE_STOPPED -> {
                    var state = Status.PAUSED.value
                    if (download.stopReason == stopReasonPause){
                        state = Status.PAUSED.value
                    }else{
                        state = Status.QUEUED.value
                    }

                    if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > contentIndex) {
                        if (!songDataList.isNullOrEmpty() && songDataList.size > nowPlayingCurrentIndex()
                            && downloadQueueList.get(contentIndex).contentId.equals(
                                songDataList.get(
                                    nowPlayingCurrentIndex()
                                ).id.toString()
                            )
                        ) {
                            onPlayerContentDownloadStatusChange(state)
                        }
                        hasActiveAudioDownload = false
                        downloadQueueList.get(contentIndex).downloadManagerId = 0
                        downloadQueueList.get(contentIndex).downloadManagerExoPlayerId = download.request.id
                        setLog("videoDownload", "BaseActivity-onDownloadsChanged-STATE_DOWNLOADING-downloadManagerExoPlayerId-${download.request.id}")
                        downloadQueueList.get(contentIndex).downloadStatus = state
                        downloadQueueList.get(contentIndex).percentDownloaded =
                            download.percentDownloaded.toInt()
                        AppDatabase.getInstance()?.downloadQueue()
                            ?.updateDownloadQueueItem(downloadQueueList.get(contentIndex))
                    }
                }
                androidx.media3.exoplayer.offline.Download.STATE_COMPLETED -> {
                    hasActiveAudioDownload = false
                    if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > contentIndex) {
                        if (!songDataList.isNullOrEmpty() && songDataList.size > nowPlayingCurrentIndex()
                            && downloadQueueList.get(contentIndex).contentId.equals(
                                songDataList.get(
                                    nowPlayingCurrentIndex()
                                ).id.toString()
                            )
                        ) {
                            onPlayerContentDownloadStatusChange(Status.COMPLETED.value)
                        }
                        downloadQueueList.get(contentIndex).downloadedFilePath = ""
                        downloadQueueList.get(contentIndex).downloadStatus = Status.COMPLETED.value
                        downloadQueueList.get(contentIndex).downloadNetworkType = NetworkType.ALL.value
                        if (download.contentLength < 0) {
                            downloadQueueList.get(contentIndex).totalDownloadBytes = 0
                        } else {
                            downloadQueueList.get(contentIndex).totalDownloadBytes =
                                download.contentLength
                        }
                        if (download.bytesDownloaded < 0) {
                            downloadQueueList.get(contentIndex).downloadedBytes = 0
                        } else {
                            downloadQueueList.get(contentIndex).downloadedBytes =
                                download.bytesDownloaded
                        }

                        downloadQueueList.get(contentIndex).createdDT = download.updateTimeMs
                        downloadQueueList.get(contentIndex).percentDownloaded = 100

                        if (SharedPrefHelper.getInstance()
                                .get(PrefConstant.FIRST_DOWNLOAD_VIDEO, true)
                        ) {
                            /* Track Events in real time */
                            val eventValue: MutableMap<String, Any> = HashMap()
//                        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, Utils?.getContentTypeName(""+downloadQueueList?.get(contentIndex).contentType))
                            eventValue.put(
                                AFInAppEventParameterName.CONTENT_ID,
                                "" + downloadQueueList.get(contentIndex).contentId
                            )
                            AppsFlyerLib.getInstance().logEvent(
                                HungamaMusicApp.getInstance().applicationContext!!,
                                EventConstant.AF_FIRST_DOWNLOAD_VIDEO,
                                eventValue
                            )
                            SharedPrefHelper.getInstance()
                                .save(PrefConstant.FIRST_DOWNLOAD_VIDEO, false)
                        }
                        addOrUpdateDownloadedAudio(downloadQueueList.get(contentIndex))

                    }
                    if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > contentIndex) {
                        setLog(TAG, "setDownloadableContentListData: deleteDownloadQueueItem 2")
                        AppDatabase.getInstance()?.downloadQueue()
                            ?.deleteDownloadQueueItem(downloadQueueList.get(contentIndex).qId!!)
                    }
                    callNextDownloadableItemFromQueue()
                    /*val messageModel = MessageModel(getString(R.string.toast_head_24), getString(R.string.toast_message_24),
                        MessageType.POSITIVE, true)
                    CommonUtils.showToast(this, messageModel)*/
                }
                androidx.media3.exoplayer.offline.Download.STATE_REMOVING -> {
                    if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > contentIndex) {
                        AppDatabase.getInstance()?.downloadQueue()
                            ?.deleteDownloadQueueItem(downloadQueueList.get(contentIndex).qId!!)
                    }
                    hasActiveAudioDownload = false
                    callNextDownloadableItemFromQueue()
                }
                androidx.media3.exoplayer.offline.Download.STATE_FAILED -> {
                    if (!downloadQueueList.isNullOrEmpty() && downloadQueueList.size > contentIndex) {
                        AppDatabase.getInstance()?.downloadQueue()?.updateQueueItemDownloadStatus(
                            downloadQueueList.get(contentIndex).qId!!,
                            Status.FAILED.value
                        )
                        downloadQueueList.get(contentIndex).downloadRetry =
                            downloadQueueList.get(contentIndex).downloadRetry + 1
                        AppDatabase.getInstance()?.downloadQueue()
                            ?.updateDownloadQueueItem(downloadQueueList.get(contentIndex))
                    }
                    hasActiveAudioDownload = false
                    callNextDownloadableItemFromQueue()
                }
                androidx.media3.exoplayer.offline.Download.STATE_RESTARTING -> {

                }
            }

            if (onDownloadVideoQueueItemChanged != null) {
                onDownloadVideoQueueItemChanged?.onDownloadVideoQueueItemChanged(
                    downloadManager,
                    download
                )
            }
        }

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        /*if (downloadsPaused!!){
            if(downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentDownloadingIndex)){
                AppDatabase.getInstance()?.downloadQueue()?.updateQueueItemDownloadStatus(downloadQueueList?.get(currentDownloadingIndex)?.qId!!, Status.PAUSED.value)
            }
            hasActiveAudioDownload = false
            callNextDownloadableItemFromQueue()
        }else{
            if(downloadQueueList != null && (downloadQueueList.size > 0 && downloadQueueList.size > currentDownloadingIndex)){
                AppDatabase.getInstance()?.downloadQueue()?.updateQueueItemDownloadStatus(downloadQueueList?.get(currentDownloadingIndex)?.qId!!, Status.QUEUED.value)
            }
        }*/

        /*if (downloadsPaused!!) {
            for (i in downloadManager.currentDownloads?.indices!!) {
                if (downloadQueueList != null && downloadQueueList.size > 0) {
                    for (k in downloadQueueList.indices) {
                        if (downloadManager.currentDownloads.get(i).request.uri.toString().equals(
                                downloadQueueList.get(
                                    k
                                ).downloadUrl
                            )
                        ) {
                            setLog("videoDownload", "BaseActivity-onDownloadsPausedChanged-downloadsPaused-$downloadsPaused-stopReason-${downloadManager.currentDownloads.get(i).stopReason}")
                            if (downloadManager.currentDownloads.get(i).stopReason == stopReasonPause){
                                setLog("videoDownload", "BaseActivity-onDownloadsPausedChanged-downloadsPaused-$downloadsPaused-downloadManagerExoPlayerId-${downloadManager.currentDownloads.get(i).request.id}")
                                AppDatabase.getInstance()?.downloadQueue()
                                    ?.updateQueueItemDownloadStatus(
                                        downloadQueueList?.get(k)?.qId!!,
                                        Status.PAUSED.value
                                    )
                            }
                        }
                    }
                }
            }
            hasActiveAudioDownload = false
        } else {
            for (i in downloadManager.currentDownloads?.indices!!) {
                if (downloadQueueList != null && downloadQueueList.size > 0) {
                    for (k in downloadQueueList.indices) {
                        if (downloadManager.currentDownloads.get(i).request.uri.toString().equals(
                                downloadQueueList.get(
                                    k
                                ).downloadUrl
                            )
                        ) {
                            setLog("videoDownload", "BaseActivity-onDownloadsPausedChanged-downloadsPaused-$downloadsPaused-stopReason-${downloadManager.currentDownloads.get(i).stopReason}")
                            if (downloadManager.currentDownloads.get(i).stopReason != stopReasonPause){
                                setLog("videoDownload", "BaseActivity-onDownloadsPausedChanged-downloadsPaused-$downloadsPaused-downloadManagerExoPlayerId-${downloadManager.currentDownloads.get(i).request.id}")
                                AppDatabase.getInstance()?.downloadQueue()
                                    ?.updateQueueItemDownloadStatus(
                                        downloadQueueList?.get(k)?.qId!!,
                                        Status.QUEUED.value
                                    )
                            }
                        }
                    }
                }
            }
        }*/


        if (onDownloadVideoQueueItemChanged != null) {
            onDownloadVideoQueueItemChanged?.onDownloadsPausedChanged(
                downloadManager,
                downloadsPaused
            )
        }
    }

    override fun onDownloadProgress(
        downloads: List<androidx.media3.exoplayer.offline.Download>?, progress: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (onDownloadVideoQueueItemChanged != null) {
                    /*for (i in downloads?.indices!!){
                        if (downloadQueueList != null && downloadQueueList.size > 0){
                            if (downloads.get(i).request.uri.toString().equals(downloadQueueList.get(
                                    currentDownloadingIndex).downloadUrl)){
                            }
                        }

                    }*/

                    if (!downloads.isNullOrEmpty()){
                        downloads.forEachIndexed { exoDownloadIndex, download ->
                            if (!downloadQueueList.isNullOrEmpty()) {
                                for (downloadQueue in downloadQueueList.iterator()) {
                                    setLog(TAG, "onChanged:  PROGRESS 3 = " +  download.request.uri.toString())
                                    setLog(TAG, "onChanged:  PROGRESS 4 = " +   downloadQueue.downloadUrl)
                                    if (download.request.uri.toString() == downloadQueue.downloadUrl) {
                                        setLog("Percentage31", download.contentLength.toString())

                                        var state: Int = Status.DOWNLOADING.value
                                        if (download.stopReason == stopReasonPause){
                                            state = Status.PAUSED.value
                                        }
                                        if (nowPlayingCurrentTrack() != null
                                            && downloadQueue.contentId.equals(nowPlayingCurrentTrack()?.id.toString()))
                                        {
                                            onPlayerContentDownloadStatusChange(state)
                                        }
                                        hasActiveAudioDownload = true
                                        downloadQueue.downloadManagerId = 0
                                        downloadQueue.downloadManagerExoPlayerId = download.request.id
                                        setLog("videoDownload", "BaseActivity-onDownloadProgress-downloadManagerExoPlayerId-${download.request.id}")
                                        downloadQueue.downloadStatus = state
                                        downloadQueue.percentDownloaded = download.percentDownloaded.toInt()
                                        if (download.contentLength < 0) {
                                            downloadQueue.totalDownloadBytes = 0
                                        } else {
                                            downloadQueue.totalDownloadBytes =
                                                download.contentLength
                                        }
                                        if (download.bytesDownloaded < 0) {
                                            downloadQueue.downloadedBytes = 0
                                        } else {
                                            downloadQueue.downloadedBytes =
                                                download.bytesDownloaded
                                        }

                                        setLog(TAG, "onChanged:  PROGRESS 2 = " +  download.bytesDownloaded )
                                        AppDatabase.getInstance()?.downloadQueue()
                                            ?.updateDownloadQueueItem(downloadQueue)
                                        onDownloadVideoQueueItemChanged?.onDownloadProgress(
                                            downloads,
                                            progress,
                                            exoDownloadIndex
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (e:Exception){

            }
        }
    }

    public fun removeVideoDownloadListener() {
        onDownloadVideoQueueItemChanged = null
    }

    fun getCurrentDownloadContentByUrl(uri: String): Int = runBlocking(Dispatchers.IO) {
        for (queueItemIndex in downloadQueueList.indices) {
            if (downloadQueueList.get(queueItemIndex).downloadUrl.equals(uri.toString())) {
                return@runBlocking queueItemIndex
            }
        }
        return@runBlocking currentDownloadingIndex
    }

    fun pauseAllAudioDownloads(continueOtherDownloads: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            if (fetch != null) {
                val updatedRowsCount = AppDatabase?.getInstance()?.downloadQueue()
                    ?.pauseAllAudioDownloads(Status.PAUSED.value, ContentTypes.AUDIO.value)
                fetch?.pauseAll()
                hasActiveAudioDownload = false
                if (continueOtherDownloads) {
                    callNextDownloadableItemFromQueue()
                }
            }
        }

    }

    fun resumeAllAudioDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            if (fetch != null) {
                val updatedRowsCount = AppDatabase?.getInstance()?.downloadQueue()
                    ?.resumeAllAudioDownloads(Status.QUEUED.value, ContentTypes.AUDIO.value)
                fetch?.resumeAll()
                if (!isAudioDownloadActive()) {
                    callNextDownloadableItemFromQueue()
                }
            }
        }

    }

    fun cancelAllAudioDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            if (fetch != null) {
                AppDatabase?.getInstance()?.downloadQueue()?.deleteDownloadQueueItemsByAudioContentType(
                    ContentTypes.AUDIO.value, ContentTypes.PODCAST.value
                )
                fetch?.cancelAll()
                if (!isAudioDownloadActive()) {
                    callNextDownloadableItemFromQueue()
                }
            }
        }

    }

    fun isAudioDownloadActive(): Boolean {
        var isActive = false
        if (fetch != null) {
            try {
                fetch?.hasActiveDownloads(true) {
                    isActive = true
                }
            } catch (e: Exception) {
                isActive = false
            }
        }
        return isActive
    }

    fun restrictUserToDownloadingContent(downloadQueue:DownloadQueue) {
        CoroutineScope(Dispatchers.IO).launch {
            if (downloadQueue != null){
                if (downloadQueue.contentType == ContentTypes.NONE.value
                    || downloadQueue.contentType == ContentTypes.AUDIO.value
                    || downloadQueue.contentType == ContentTypes.PODCAST.value
                    || downloadQueue.contentType == ContentTypes.RADIO.value
                ){

                }else{
                    setLog("DownloadContent", "BaseActivity-restrictUserToDownloadingContent-downloadQueue.downloadUrl-${downloadQueue.downloadUrl}")
                    removeVideoContentFromDownload(Uri.parse(downloadQueue.downloadUrl))
                }

                downloadQueue.qId?.let {
                    AppDatabase.getInstance()?.downloadQueue()?.deleteDownloadQueueItem(
                        it
                    )
                }
                callNextDownloadableItemFromQueue()
            }
        }

    }

    fun pauseAllVideoDownloads(continueOtherDownloads: Boolean, url:String?) {
        if (downloadTracker != null) {
            downloadTracker?.pauseDownload(url)
            hasActiveAudioDownload = false
            if (continueOtherDownloads) {
                callNextDownloadableItemFromQueue()
            }
        }
    }

    fun resumeAllVideoDownloads() {
        if (downloadTracker != null) {
            downloadTracker?.resumeDownload()
            /*if(!isAudioDownloadActive()){
                callNextDownloadableItemFromQueue()
            }*/
        }
    }

    fun removeVideoContentFromDownload(uri: Uri) {
        setLog(
            "videoDeleted",
            "BaseActivity-removeVideoContentFromDownload-delete file from exoDatabase"
        )
        //First resume download then remove download
        //downloadTracker?.resumeDownload()
        downloadTracker?.removeDownload(uri)
    }

    fun isVideoDownloadActive(): Boolean {
        return !DemoUtil.getDownloadManager(this).isIdle
    }

    fun isAudioOrVideoDownloadActive(): Boolean {
        return isAudioDownloadActive() || isVideoDownloadActive()
    }

    suspend fun getPlayableContentUrl(context: Context, id: String, contentType: Int) {
        var type = 4
        var userSettingRespModel = SharedPrefHelper.getInstance()
            .getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
        if (contentType == 4 || contentType == 93 || contentType == 98 || contentType == 22) {
            type = 5
            userSettingRespModel = SharedPrefHelper.getInstance()
                .getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
        }

        val quality = Quality.AUTO.qualityPrefix

        if (userSettingRespModel?.data != null && !userSettingRespModel?.data?.data.isNullOrEmpty()) {
            if (userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.streaming_quality != null && !TextUtils.isEmpty(
                    userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.streaming_quality
                )
            ) {
                val streamQuality = Quality.getQualityByName(
                    userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality.toString()
                )
                val quality = streamQuality.qualityPrefix
            }
        }
        setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-id-${id} - quality-$quality - isGoldUser-${getIsGoldUser()}")
        val url = WSConstants.METHOD_PLAYABLE_CONTENT + id + "/url/playable?quality=" + quality + "&contentType=" + type + "&certificate=widevine"+ if(type == 4) "&user=" + if (CommonUtils.isUserHasGoldSubscription()) "gold" else "free" else ""
        HungamaMusicApp.getInstance().userStreamIDList.add(id)
        val mlang = SharedPrefHelper.getInstance().get(
            Constant.APPMUSICLANG,
            Constant.default_music_language_code
        )
        val vlang = SharedPrefHelper.getInstance().get(
            Constant.APPVIDEOLANG,
            Constant.default_video_language_code
        )

        val version = DataManager.getInstance(this)?.getApiVersion(url)
        var finalURL = ""
        finalURL = if (url?.contains("?", true)!!) {
            "$url&alang=" + SharedPrefHelper.getInstance()
                .getLanguage() + "&vlang=" + vlang + "&mlang=" + mlang + "&platform=a&device=android&variant=" + version
        } else {
            "$url?alang=" + SharedPrefHelper.getInstance()
                .getLanguage() + "&vlang=" + vlang + "&mlang=" + mlang + "&platform=a&device=android&variant=" + version
        }

        try {
            /**
             * event property
             */
            var requestTime = DateUtils.getCurrentDateTime()
            setLog(TAG, "getPlayableContentUrl: finalURL:${finalURL}")
            val cacheRequest: StringRequest = object : StringRequest(
                Method.GET,
                finalURL, Response.Listener { response ->

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            isNextSongInProgress = false
                            var playableContentModel = Gson().fromJson<PlayableContentModel>(
                                response.toString(),
                                PlayableContentModel::class.java
                            ) as PlayableContentModel


                            if (playableContentModel.data.body.data.url.playable.size > 0) {
                                playableContentModel.data.body.data.url.apiResponse = response
                                val streamQuality =
                                    Quality.getServerKeyByName(quality).toString()

                                run loop@{
                                    playableContentModel.data.body.data.url.playable.forEach {
                                        setLog(
                                            TAG,
                                            "getPlayableContentUrl: isUserHasGoldSubscription:${getIsGoldUser()} streamQuality:${it?.key} "
                                        )
                                        setLog(
                                            "playbackQuality",
                                            "BaseActivity-getPlayableContentUrl-main===id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - streamQuality-$streamQuality- isGoldUser-${getIsGoldUser()}"
                                        )
                                        playableContentModel =
                                            getStreamUrl(it, playableContentModel, streamQuality)

                                        /*if (getIsGoldUser()) {

                                            if (it.key.equals("dolby", true) || it.key.equals(
                                                    "hd",
                                                    true
                                                )
                                            ) {

                                                playableContentModel.data.head.headData.misc.url =
                                                    it.data
                                                if (!TextUtils.isEmpty(it.token)) {
                                                    playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                        it.token
                                                }
                                                playableContentModel.data.head.headData.misc.urlKey =
                                                    it.key

                                                setLog(
                                                    TAG,
                                                    "getPlayableContentUrl: quality url match and update streamQuality if 0 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                                )
                                                setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-if 0==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                                return@loop
                                            }else{
                                                playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)
                                                setLog(
                                                    TAG,
                                                    "getPlayableContentUrl: quality url match and update streamQuality else 1 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                                )
                                                setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-else 1==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                            }

                                        }else{
                                            playableContentModel = getStreamUrl(it, playableContentModel, streamQuality)
                                            setLog(
                                                TAG,
                                                "getPlayableContentUrl: quality url match and update streamQuality else 2 id-${playableContentModel.data.head.headData.id}-url:${it?.data} "
                                            )
                                            setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-else 2==id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey}--${it.key} - isGoldUser-${getIsGoldUser()}")
                                        }*/

                                    }
                                }


                                if (TextUtils.isEmpty(playableContentModel.data.head.headData.misc.url) && playableContentModel.data.body.data.url.playable.size > 0) {

                                    if(BaseActivity.getIsGoldUser()){
                                        var tmpList=playableContentModel.data.body.data.url.playable?.filter { !it.key.equals("preview")}

                                        if(tmpList?.size!!>0){
                                            playableContentModel.data.head.headData.misc.url =
                                                tmpList.get(0).data

                                            if (!TextUtils.isEmpty(tmpList.get(0).token)) {
                                                playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                    tmpList.get(0).token
                                            }
                                            playableContentModel.data.head.headData.misc.urlKey = tmpList.get(0).key
                                            setLog(TAG, "getPlayableContentUrl: getIsGoldUser:${BaseActivity.getIsGoldUser()} quality url default first ser:${tmpList.get(0).key} ")
                                        }else{
                                            playableContentModel.data.head.headData.misc.url =
                                                playableContentModel.data.body.data.url.playable.get(0).data

                                            if (!TextUtils.isEmpty(
                                                    playableContentModel.data.body.data.url.playable.get(
                                                        0
                                                    ).token
                                                )
                                            ) {
                                                playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                    playableContentModel.data.body.data.url.playable.get(
                                                        0
                                                    ).token
                                            }
                                            playableContentModel.data.head.headData.misc.urlKey =
                                                playableContentModel.data.body.data.url.playable.get(0).key
                                            setLog(
                                                TAG,
                                                "getPlayableContentUrl: quality url default first ser:${
                                                    playableContentModel.data.body.data.url.playable.get(
                                                        0
                                                    ).key
                                                } "
                                            )
                                        }

                                    }else{
                                        playableContentModel.data.head.headData.misc.url =
                                            playableContentModel.data.body.data.url.playable.get(0).data

                                        if (!TextUtils.isEmpty(
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).token
                                            )
                                        ) {
                                            playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).token
                                        }
                                        playableContentModel.data.head.headData.misc.urlKey =
                                            playableContentModel.data.body.data.url.playable.get(0).key
                                        setLog(
                                            TAG,
                                            "getPlayableContentUrl: quality url default first ser:${
                                                playableContentModel.data.body.data.url.playable.get(
                                                    0
                                                ).key
                                            } "
                                        )
                                    }

                                }

                            }
                            setLog("playbackQuality", "BaseActivity-getPlayableContentUrl-End-id-${playableContentModel.data.head.headData.id}-title-${playableContentModel.data.head.headData.title} - key-${playableContentModel.data.head.headData.misc.urlKey} - isGoldUser-${getIsGoldUser()}")

                            apiRetryCount = 0
                            playableObserverResponse(playableContentModel)
                            PlayableContentRepos().setEventModelDataAppLevel(
                                playableContentModel,
                                type
                            )
                            /**
                             * event property start
                             */
                            val responseTime = DateUtils.getCurrentDateTime()
                            val diffInMillies: Long =
                                Math.abs(requestTime.getTime() - responseTime.getTime())

                            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                            setLog(
                                TAG,
                                "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                            )

                            val hashMap = java.util.HashMap<String, String>()
                            hashMap[EventConstant.ERRORCODE_EPROPERTY] = ""
                            hashMap[EventConstant.NETWORKTYPE_EPROPERTY] = "" + ConnectionUtil(context).networkType
                            hashMap[EventConstant.NAME_EPROPERTY] = "playableURL"
                            hashMap[EventConstant.RESPONSECODE_EPROPERTY] = EventConstant.RESPONSE_CODE_200
                            hashMap[EventConstant.SOURCE_NAME_EPROPERTY] = ""
                            hashMap[EventConstant.SOURCE_EPROPERTY] = ""
                            hashMap[EventConstant.RESPONSETIME_EPROPERTY] = "" + diff
                            hashMap[EventConstant.URL_EPROPERTY] = com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!

                            EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                            /**
                             * event property end
                             */

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }
                    }




                }, Response.ErrorListener { error ->
                    isNextSongInProgress = false
                    setLog(Constant.Tag, "Error: " + error.message)
                    error.printStackTrace()

                    /*if (apiRetryCount < 3 && (error is TimeoutError || error is NoConnectionError)) {
                        apiRetryCount += apiRetryCount
                        getPlayableContentUrl(context, id,contentType)
                    }else if (apiRetryCount > 3){
                        apiRetryCount = 0
                    }*/
                    setLog(
                        "apiRetryCount",
                        "BaseActivity-getPlayableContentUrl-1-apiRetryCount-$apiRetryCount"
                    )
                    if (apiRetryCount < 3) {
                        apiRetryCount++
                        setLog(
                            "apiRetryCount",
                            "BaseActivity-getPlayableContentUrl-2-apiRetryCount-$apiRetryCount"
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            getPlayableContentUrl(context, id, contentType)
                        }



                    } else if (apiRetryCount > 3) {
                        apiRetryCount = 0
                        setLog(
                            "apiRetryCount",
                            "BaseActivity-getPlayableContentUrl-3-apiRetryCount-$apiRetryCount"
                        )
                    }

                    /**
                     * event property start
                     */
                    val responseTime = DateUtils.getCurrentDateTime()
                    val diffInMillies: Long =
                        Math.abs(requestTime.getTime() - responseTime.getTime())
                    val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                    setLog(
                        TAG,
                        "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                    )
                    val hashMap = java.util.HashMap<String, String>()
                    hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "" + error.message)
                    hashMap.put(
                        EventConstant.NETWORKTYPE_EPROPERTY,
                        "" + ConnectionUtil(context).networkType
                    )
                    hashMap.put(EventConstant.NAME_EPROPERTY, "playableURL")
                    hashMap.put(
                        EventConstant.RESPONSECODE_EPROPERTY,
                        "" + error?.networkResponse?.statusCode
                    )
                    hashMap.put(
                        EventConstant.SOURCE_NAME_EPROPERTY,
                        "" + HungamaMusicApp?.getInstance()?.getEventData(id)?.sourceName
                    )
                    hashMap.put(
                        EventConstant.SOURCE_EPROPERTY,
                        "" + HungamaMusicApp?.getInstance()?.getEventData(id)?.sourceName
                    )
                    hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                    hashMap[EventConstant.URL_EPROPERTY] = com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!

                    EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))
                    /**
                     * event property end
                     */

                }) {
                override fun getParams(): Map<String, String>? {
                    val param: MutableMap<String, String> = java.util.HashMap()
                    param.putAll(params!!)
                    return param
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = java.util.HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    return params
                }
            }
            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            //            val queue = Volley.newRequestQueue(context)
//            queue.add(cacheRequest)

            com.hungama.music.data.webservice.remote.VolleySinglton.getInstance(context)
                ?.getRequestQueue()?.add(cacheRequest)
        } catch (e: java.lang.Exception) {
            isNextSongInProgress = false
            e.printStackTrace()
        }
    }

    private fun setSleepTimer() {
        setLog("sleepTimer", "BaseActivity-setSleepTimer")
        if (sleepTimerHandler != null) {
            removeSleepTimerCallback()
            sleepTimerHandler?.post(updateSleepTimerTask)
        } else {
            sleepTimerHandler = Handler(Looper.myLooper()!!)
            sleepTimerHandler?.post(updateSleepTimerTask)
        }

    }

    private val updateSleepTimerTask = Runnable {
        val nowCal = Calendar.getInstance()
        val sleepCal = Calendar.getInstance()
        val sleepTime = SharedPrefHelper.getInstance().get(PrefConstant.SLEEP_TIMER, 0L)
        setLog(TAG, ":sleepTime  $sleepTime")
        setLog(TAG, ":sleepCal  $sleepCal")
        setLog(TAG, ":nowCal  $nowCal")
        if (sleepTime > 0) {
            sleepCal.timeInMillis = sleepTime
        }
        val diff: Long = sleepCal.timeInMillis - nowCal.timeInMillis
        if (diff > 1) {
            countDownTimer = object : CountDownTimer(diff, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    isTimerRuning = true
                    val seconds = millisUntilFinished / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24

                    val dHours = hours % 24
                    val dMinutes = minutes % 60
                    val dSeconds = seconds % 60

                    val time =
                        "$days days :" + String.format(
                            "%02d",
                            dHours
                        ) + ":" + String.format(
                            "%02d",
                            dMinutes
                        ) + ":" + String.format(
                            "%02d",
                            dSeconds
                        )

                    setLog(TAG, "onTick: $time")
                    setLog("sleepTimer", "BaseActivity-onTick-$time")
                    /*tvSleepTimer?.setText(
                        "" + String.format(
                            "%02d",
                            dHours
                        ) + ":" + String.format("%02d", dMinutes)
                                + ":" + String.format(
                            "%02d",
                            dSeconds
                        )
                    )
                    tvSleepTimer?.visibility = View.VISIBLE*/
                }

                override fun onFinish() {
                    isTimerRuning = false
                    setLog("sleepTimer", "BaseActivity-onSleepTimeOver-called from-onFinish()")
                    onSleepTimeOver()
                    val messageModel = MessageModel(
                        getString(R.string.toast_str_20),
                        MessageType.NEUTRAL, true
                    )
                    CommonUtils.showToast(this@BaseActivity, messageModel)
                }
            }.start()
        } else {
            isTimerRuning = false
        }
    }

    private fun removeSleepTimerCallback() {
        if (sleepTimerHandler != null) {
            sleepTimerHandler?.removeCallbacks(updateSleepTimerTask)
        }
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
    }

    fun setLocalBroadcastEventCall(
        onLocalBroadcastEventCallBack: OnLocalBroadcastEventCallBack?,
        event: String
    ) {
        setLog("BroadcastReceiver", "mMessageReceiver-$intent")
//        removeLocalBroadcastEventCallBack()
        this.onLocalBroadcastEventCallBack = onLocalBroadcastEventCallBack
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(event));
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver", "mMessageReceiver-$intent")
            if (intent != null){
                if (onLocalBroadcastEventCallBack != null && intent.hasExtra("EVENT")){
                    setLog("BroadcastReceiver", "mMessageReceiver-"+intent.getIntExtra("EVENT", 0))
                    onLocalBroadcastEventCallBack?.onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }

    interface OnLocalBroadcastEventCallBack {
        fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent)
    }

    fun removeLocalBroadcastEventCallBack() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        onLocalBroadcastEventCallBack = null
    }

    override fun onSpeedChangeItemClick(data: SpeedChangeDialogModel) {
        when (data.value) {
            1.0 -> {
                tapCount = data.value
                val param = PlaybackParameters(data.value.toFloat())
                audioPlayer?.setPlaybackParameters(param)
            }
            1.25 -> {
                tapCount = data.value
                val param = PlaybackParameters(data.value.toFloat())
                audioPlayer?.setPlaybackParameters(param)
            }
            1.5 -> {
                tapCount = data.value
                val param = PlaybackParameters(data.value.toFloat())
                audioPlayer?.setPlaybackParameters(param)
            }
            else -> {
                /*tapCount = 0
                audioPlayer?.setPlaybackParameters(null)
                tv_play_back_speed?.text = "" + 1*/
            }
        }
    }

    fun updatePlayerView() {
        audioPlayer?.fetchTrackMetadata()
    }

    var isNextSongInProgress = false
    fun playNextSong(isFromMiniplayer: Boolean) {
        try {
            if (player11?.isPlaying == true)
                return

            setTouchData()
            setLog(
                "PlayerAds:-",
                "tritonAds=> isSwipablePlayerAudioAdPlaying - isSleepTimerSetToEndOfCurrentPlay-$isSleepTimerSetToEndOfCurrentPlay isNextSongInProgress-$isNextSongInProgress"
            )
            setLog("prepareNextSong", "playNextSong - isNextSongInProgress-$isNextSongInProgress")
            if (!isAudioAdPlaying && !isNextSongInProgress) {
                if (!isSleepTimerSetToEndOfCurrentPlay) {
                    val nextIndex = nowPlayingCurrentIndex() + 1
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nextIndex) {
                        updateNowPlayingCurrentIndex(nextIndex)
                        if (nowPlayingCurrentIndex() < 0) {
                            isNextSongInProgress = false
                            //btn_next_play?.isEnabled = false
                        } else {
                            isNextClick = NEXT_CLICK_STATUS
                            //setLog("getPlayerRepeatMode", "playNextSong-getPlayerRepeatMode-${getPlayerRepeatMode()}")
                            if (getPlayerRepeatMode() == Player.REPEAT_MODE_ONE){
                                changeRepeateModes(Player.REPEAT_MODE_OFF)
                            }
                            val isOfflinePlay =
                                isContentDownloaded(songDataList, nowPlayingCurrentIndex())

                            if (isOfflinePlay) {
                                if (songDataList?.get(nowPlayingCurrentIndex())?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                                    playContentOfflineDeviceSongs(
                                        songDataList,
                                        nowPlayingCurrentIndex(),
                                        false
                                    )
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_LOCAL
                                    )
                                } else {
                                    playContentOffline(songDataList, nowPlayingCurrentIndex(), false)
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_OFFLINE
                                    )
                                }
                                setLog("prepareNextSong", "prepareNextSong-offline-1 - isNextSongInProgress-$isNextSongInProgress")
                                isNextSongInProgress = false
                                setLog("prepareNextSong", "prepareNextSong-offline-2 - isNextSongInProgress-$isNextSongInProgress")
                            } else {

                                if (songDataList?.get(nowPlayingCurrentIndex())?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                                    playContentOfflineDeviceSongs(
                                        BaseActivity.songDataList,
                                        nowPlayingCurrentIndex(),
                                        false
                                    )
                                    isNextSongInProgress = false
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_LOCAL
                                    )
                                } else {
                                    if (TextUtils.isEmpty(songDataList?.get(nowPlayingCurrentIndex())?.url)) {
                                        playContentOnline(songDataList, nowPlayingCurrentIndex(), false)
                                    } else {
                                        if (audioPlayer != null && audioPlayer?.hasNext() == true && audioPlayer?.playbackState != Player.STATE_IDLE && audioPlayer?.playbackState != Player.STATE_ENDED) {
                                            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex() - 1) {
                                                setLog(
                                                    "callUserStreamUpdate1",
                                                    "playNextSong-playNextSong title:${
                                                        songDataList?.get(
                                                            nowPlayingCurrentIndex() - 1
                                                        )?.title
                                                    }"
                                                )
                                                callUserStreamUpdate(-1, songDataList?.get(nowPlayingCurrentIndex() - 1), nowPlayingCurrentIndex() - 1, false)
                                            }
                                            setLog(
                                                "preCatchContent",
                                                "BaseActivity-playNextSong-audioPlayer?.next()-track.id-${
                                                    songDataList?.get(nowPlayingCurrentIndex())?.id
                                                }  title-${songDataList?.get(nowPlayingCurrentIndex())?.title}  track.url-${
                                                    songDataList?.get(
                                                        nowPlayingCurrentIndex()
                                                    )?.url
                                                }"
                                            )
                                            setLog(
                                                "preCatchContent",
                                                "BaseActivity-playNextSong-audioPlayer?.next()  isForegroundService-${audioPlayerService?.isForegroundService} - audioPlayer.state-${audioPlayer?.playbackState}")
                                            updateAudioAdsSongCounts()
                                            HungamaMusicApp.getInstance().setIsFirstLaunchSong(false)
                                            audioPlayer?.next()
                                            playPlayer()
                                            isPause = false
                                            setLog("preCatchApiCall", "BaseActitvity-playNextSong")
                                            preCatchApiCall(
                                                songDataList,
                                                nowPlayingCurrentIndex(),
                                                false
                                            )
                                        } else {
                                            setLog(
                                                "preCatchContent",
                                                "BaseActivity-playNextSong-playContentOnline()  isForegroundService-${audioPlayerService?.isForegroundService} - audioPlayer.state-${audioPlayer?.playbackState}")
                                            playContentOnline(
                                                songDataList,
                                                nowPlayingCurrentIndex(),
                                                false
                                            )
                                        }
                                    }

                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_ONLINE
                                    )
                                }


                            }


                            setLog(
                                TAG,
                                "onClick btn_next_play nowPlayingCurrentIndex():${nowPlayingCurrentIndex()}"
                            )
                            setLog(
                                TAG,
                                "onClick btn_next_play: ${songDataList?.get(nowPlayingCurrentIndex())}"
                            )
                        }
                        setEnableDisableNextPreviousIcons()
                    }
                } else {
                    setLog("sleepTimer", "playNextSong-called from-playNextSong()")
                    onSleepTimeOver()
                }
            }
            //callRecommendedApiOnEndOfSong()
        }catch (e:Exception){

        }
    }

    fun playPreviousSong() {
        if (player11?.isPlaying == true)
            return
        isPresvious = true
        setTouchData()
        try {
            if (!isAudioAdPlaying) {
                val previousIndex = nowPlayingCurrentIndex() - 1
                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > previousIndex && previousIndex >= 0) {
                    updateNowPlayingCurrentIndex(previousIndex)

                    if (nowPlayingCurrentIndex() < 0) {
                        //btn_previous_play?.isEnabled = false
                    } else {
                        //btn_previous_play?.isEnabled = true
                        isNextClick = PREVIOUS_CLICK_STATUS
                        setLog("getPlayerRepeatMode", "playNextSong-getPlayerRepeatMode-${getPlayerRepeatMode()}")
                        if (getPlayerRepeatMode() == Player.REPEAT_MODE_ONE){
                            changeRepeateModes(Player.REPEAT_MODE_OFF)
                        }
                        val isOfflinePlay = isContentDownloaded(songDataList, nowPlayingCurrentIndex())
//                setLog("callUserStreamUpdate1", "playPreviousSong")
//                callUserStreamUpdate(STREAM_POSITION_CURRENT)

                        if (isOfflinePlay) {
                            if (BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                                playContentOfflineDeviceSongs(
                                    BaseActivity.songDataList,
                                    nowPlayingCurrentIndex(),
                                    false
                                )
                                setEventModelDataAppLevel(
                                    "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                    songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                    BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                    EventConstant.CONSUMPTIONTYPE_LOCAL
                                )
                            } else {
                                playContentOffline(songDataList, nowPlayingCurrentIndex(), false)
                                setEventModelDataAppLevel(
                                    "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                    songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                    BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                    EventConstant.CONSUMPTIONTYPE_OFFLINE
                                )
                            }

                        } else {

                            if (BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                                playContentOfflineDeviceSongs(
                                    BaseActivity.songDataList,
                                    nowPlayingCurrentIndex(),
                                    false
                                )
                                setEventModelDataAppLevel(
                                    "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                    songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                    BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                    EventConstant.CONSUMPTIONTYPE_LOCAL
                                )
                            } else {

                                if (TextUtils.isEmpty(songDataList?.get(nowPlayingCurrentIndex())?.url)) {
                                    playContentOnline(songDataList, nowPlayingCurrentIndex(), false)
                                } else {
                                    if (audioPlayer != null && audioPlayer?.hasPrevious() == true) {
                                        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex() + 1) {
                                            setLog(
                                                "callUserStreamUpdate1",
                                                "playPreviousSong-playPreviousSong title:${
                                                    songDataList?.get(
                                                        nowPlayingCurrentIndex() + 1
                                                    )?.title
                                                }"
                                            )
                                            callUserStreamUpdate(-1, songDataList?.get(nowPlayingCurrentIndex() + 1), nowPlayingCurrentIndex() + 1, false)
                                        }
                                        setLog(
                                            "preCatchContent",
                                            "BaseActivity-playPreviousSong-audioPlayer?.previous()-track.id-${
                                                songDataList?.get(nowPlayingCurrentIndex())?.id
                                            }  title-${songDataList?.get(nowPlayingCurrentIndex())?.title}  track.url-${
                                                songDataList?.get(
                                                    nowPlayingCurrentIndex()
                                                )?.url
                                            }"
                                        )
                                        setLog(
                                            "preCatchContent",
                                            "BaseActivity-playPreviousSong-audioPlayer?.previous()  isForegroundService-${audioPlayerService?.isForegroundService}")
                                        updateAudioAdsSongCounts()
                                        HungamaMusicApp.getInstance().setIsFirstLaunchSong(false)
                                        audioPlayer?.previous()
                                        playPlayer()
                                        isPause = false
                                        setLog("preCatchApiCall", "BaseActitvity-playPreviousSong")
                                        preCatchApiCall(songDataList, nowPlayingCurrentIndex(), false)
                                    } else {
                                        playContentOnline(songDataList, nowPlayingCurrentIndex(), false)
                                    }
                                }
                                setEventModelDataAppLevel(
                                    "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                    songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                    BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                    EventConstant.CONSUMPTIONTYPE_ONLINE
                                )
                            }


                        }

                    }
                }
                setEnableDisableNextPreviousIcons()
            }
        }catch (e:Exception){

        }
    }

    fun playCurrentSong(isPause: Boolean = false) {
        try {
            if (!isAudioAdPlaying) {
                if (!isSleepTimerSetToEndOfCurrentPlay) {
                    val nextIndex = nowPlayingCurrentIndex()
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nextIndex) {
                        updateNowPlayingCurrentIndex(nextIndex)
                        if (nowPlayingCurrentIndex() < 0) {
                            //btn_next_play?.isEnabled = false
                        } else {

//                    setLog("callUserStreamUpdate1", "playCurrentSong")
//                    callUserStreamUpdate(STREAM_POSITION_CURRENT)
                            //btn_next_play?.isEnabled = true
                            isNextClick = NEXT_CLICK_STATUS
                            val isOfflinePlay =
                                isContentDownloaded(songDataList, nowPlayingCurrentIndex())

                            if (isOfflinePlay) {
                                if (BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                                    playContentOfflineDeviceSongs(
                                        BaseActivity.songDataList,
                                        nowPlayingCurrentIndex(),
                                        isPause
                                    )
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_LOCAL
                                    )
                                } else {
                                    playContentOffline(songDataList, nowPlayingCurrentIndex(), isPause)
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_OFFLINE
                                    )
                                }

                            } else {
                                if (songDataList?.get(nowPlayingCurrentIndex())?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                                    playContentOfflineDeviceSongs(
                                        BaseActivity.songDataList,
                                        nowPlayingCurrentIndex(),
                                        isPause
                                    )
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_LOCAL
                                    )
                                } else {
                                    playContentOnline(songDataList, nowPlayingCurrentIndex(), isPause)
                                    setEventModelDataAppLevel(
                                        "" + songDataList?.get(nowPlayingCurrentIndex())?.id!!,
                                        songDataList?.get(nowPlayingCurrentIndex())?.title!!,
                                        BaseActivity.songDataList?.get(nowPlayingCurrentIndex())?.pName!!,
                                        EventConstant.CONSUMPTIONTYPE_ONLINE
                                    )
                                }

                        }


                            setLog(
                                TAG,
                                "onClick btn_next_play nowPlayingCurrentIndex():${nowPlayingCurrentIndex()}"
                            )
                            setLog(
                                TAG,
                                "onClick btn_next_play: ${songDataList?.get(nowPlayingCurrentIndex())}"
                            )
                        }
                        setEnableDisableNextPreviousIcons()
                    }
                } else {
                    setLog("onSleepTimeOver", "onSleepTimeOver-called from-playCurrentSong()")
                    onSleepTimeOver()
                }
            }
        }catch (e:Exception){

        }
    }

    fun setMiniPlayerState(state: Int) {
        //bottomSheetBehavior?.state = state
        //New Tab Player - Start//
        setLog(
            "SwipablePlayerFragment",
            "BaseActivity-setMiniPlayerState - isNewSwipablePlayerOpen=$isNewSwipablePlayerOpen currentPlayerType-$currentPlayerType"
        )
        if (currentPlayerType != CONTENT_MUSIC_VIDEO) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            bottomSheetBehavior?.state = state
        }
        //New Tab Player - End//

    }

    fun getMiniPlayerState(): Int {
        if (bottomSheetBehavior != null && bottomSheetBehavior?.state != null) {
            return bottomSheetBehavior?.state!!
        } else {
            return BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun changeMiniPlayerState(state: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            when (state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    setLog("BaseActivity:-", "setBottomSheet - collapsed")
                    miniplayerHeight = resources.getDimensionPixelSize(R.dimen.dimen_62).toInt()
                    showBottomNavigationAndMiniplayerBlurView()
                    changeHomeBg(false)
                    shortPlayerControlView?.alpha = 1f
                    if (currentPlayerType == CONTENT_MUSIC || currentPlayerType == CONTENT_RADIO || currentPlayerType == CONTENT_MOOD_RADIO || currentPlayerType == CONTENT_ON_DEMAND_RADIO || currentPlayerType == CONTENT_PODCAST) {
                        rootMiniPlayer.visibility = View.VISIBLE
                    }
                    changeMiniPlayerProgressAlignment()
                    //showBottomNavigationBar()
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    setLog("BaseActivity:-", "setBottomSheet - draging")
                    //setSupportActionBar(bottomSheetToolbar)
                    //supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    //bottomSheetToolbar.title = ""
                    //shortPlayerControlView.visibility = View.VISIBLE
                    showBottomNavigationBar()
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    statusBarLastColor = window?.statusBarColor!!
                    setLog("BaseActivity:-", "setBottomSheet - expand")
                    miniplayerHeight = resources.getDimensionPixelSize(R.dimen.dimen_0).toInt()
                    showBottomNavigationAndMiniplayerBlurView()
                    shortPlayerControlView.alpha = 0f
                    if (currentPlayerType == CONTENT_MUSIC || currentPlayerType == CONTENT_RADIO || currentPlayerType == CONTENT_MOOD_RADIO || currentPlayerType == CONTENT_ON_DEMAND_RADIO || currentPlayerType == CONTENT_PODCAST) {
                        rootMiniPlayer.visibility = View.GONE
                    }
                    bottomSheetToolbar.alpha = 1f
                    //shortPlayerControlView.visibility = View.GONE
                    hideBottomNavigationBar()
                    changeHomeBg(true)
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    setLog("BaseActivity:-", "setBottomSheet - hide")
                    miniplayerHeight = resources.getDimensionPixelSize(R.dimen.dimen_0).toInt()
                    showBottomNavigationAndMiniplayerBlurView()
                    setLog("stateHide1", "true")
                    changeHomeBg(false)
                    showBottomNavigationBar()
                    if (!songDataList.isNullOrEmpty() && audioPlayer != null && (audioPlayer?.playbackState == Player.STATE_READY)) {
                        setMiniPlayerState(BottomSheetBehavior.STATE_COLLAPSED)
                    }
                }
                else -> {
                    //do nothing
                    //changeHomeBg(false)
                }
            }
        }
    }

    fun changeMiniPlayerProgressAlignment() {
        try {
            val lp: RelativeLayout.LayoutParams =
                exo_progress.getLayoutParams() as RelativeLayout.LayoutParams
            if (Utils?.getCurrentFragment(this@BaseActivity)?.javaClass?.simpleName.equals(
                    QueueFragment().javaClass.simpleName,
                    true
                )
            ) {
                setLog("QueueOpen", "true")
                setLog("SwipablePlayerFragment", "changeMiniPlayerProgressAlignment()-true")
                lp.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                exo_progress.layoutParams = lp
                exo_progress.invalidate()
            } else {
                setLog("QueueOpen", "false")
                setLog("SwipablePlayerFragment", "changeMiniPlayerProgressAlignment()-false")
                lp.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                exo_progress.layoutParams = lp
                exo_progress.invalidate()
            }
        } catch (e: java.lang.Exception) {

        }

    }

    override fun onConnectedDeviceMenuItemClick(connectedData: ConnectedDialogModel) {
        /*tvConnectDevice?.text = connectedData.Title
        SharedPrefHelper.getInstance().setLastConnectedDeviceName(connectedData.Title)
        tvConnectDevice?.setTextColor(ContextCompat.getColor(this, R.color.connectedDevice))
        var iconWidth = 0
        var iconHeight = 0
        if (connectedData.id == 1) {
            iconWidth = resources.getDimensionPixelSize(R.dimen.dimen_22)
            iconHeight = resources.getDimensionPixelSize(R.dimen.dimen_17)
        } else if (connectedData.id == 2) {
            iconWidth = resources.getDimensionPixelSize(R.dimen.dimen_22)
            iconHeight = resources.getDimensionPixelSize(R.dimen.dimen_17)
        } else if (connectedData.id == 3) {
            iconWidth = resources.getDimensionPixelSize(R.dimen.dimen_22)
            iconHeight = resources.getDimensionPixelSize(R.dimen.dimen_17)
        } else {
            iconWidth = resources.getDimensionPixelSize(R.dimen.dimen_22)
            iconHeight = resources.getDimensionPixelSize(R.dimen.dimen_17)
        }
        imgBluetooth?.layoutParams?.width = iconWidth
        imgBluetooth?.layoutParams?.height = iconHeight
        imgBluetooth?.requestLayout()
        imgBluetooth?.setImageDrawable(
            this.faDrawable(
                connectedData.icon!!,
                R.color.connectedDevice
            )
        )*/
    }

    fun showBottomNavigationAndMiniplayerBlurView() {
        setLog(
            "SwipablePlayerFragment",
            "BaseActivity-showBottomNavigationAndMiniplayerBlurView - isNewSwipablePlayerOpen=$isNewSwipablePlayerOpen currentPlayerType-$currentPlayerType"
        )
        //New Tab Player - Start//
        if (currentPlayerType != CONTENT_MUSIC_VIDEO) {
            miniplayerHeight = 0
        }
        //New Tab Player - End//
        if (!isNewSwipablePlayerOpen) {
            rlBottomSheetAndMiniPlayerBlurView?.layoutParams?.height =
                bottomNavigationHeight + miniplayerHeight
        } else {
            rlBottomSheetAndMiniPlayerBlurView?.layoutParams?.height = 0
        }
        rlBottomSheetAndMiniPlayerBlurView?.requestLayout()
        setLog(
            "setPageBottomSpacing",
            "showBottomNavigationAndMiniplayerBlurView--$bottomNavigationHeight , $miniplayerHeight"
        )
        audioPlayerActionBroadcast(getAudioPlayerPlayingStatus())

    }

    private fun openSleepTimeChangeDialog() {
        val sheet = SleepDialog(this, songDataList?.get(nowPlayingCurrentIndex()))
        sheet.show(supportFragmentManager, "SleepTimeDialog")
    }

    fun openStoryPlatformDialog() {
        setLog(
            TAG,
            "openStoryPlatformDialog: working openStoryPlatformDialog" + songDataList?.get(
                nowPlayingCurrentIndex()
            )
        )
        val sheet = ShareStoryPlatformDialog(this, songDataList?.get(nowPlayingCurrentIndex()))
        sheet.show(supportFragmentManager, "openStoryPlatformDialog")
    }

    var selectedStoryDataModel: ShareStoryPlatformDialogModel? = null
    override fun onStoryPlatformClick(data: ShareStoryPlatformDialogModel) {
        setLog("TAG", " share onStoryPlatformClick data:${data?.currentTrack}")

        selectedStoryDataModel = data
        if (data.title.equals("Facebook")) {
            setLog("TAG", " share fb story 1: ")
            shareStoryImage(
                CommonUtils.STORY_SHARE.FACEBOOK,
                CommonUtils.STORY_TYPE.PHOTO,
                data?.currentTrack?.title!!,
                data?.currentTrack?.url!!,
                data?.currentTrack?.image!!
            )
        } else if (data.title.equals("Instagram")) {
            shareStoryImage(
                CommonUtils.STORY_SHARE.INSTAGRAM,
                CommonUtils.STORY_TYPE.PHOTO,
                data?.currentTrack?.title!!,
                data?.currentTrack?.url!!,
                data?.currentTrack?.image!!
            )
        }
    }

    override fun onSleepTimeChangeItemClick(data: SleepDialogModel) {
        setLog(TAG, "onSleepTimeChangeItemClick: data" + data)
        val now = Calendar.getInstance()
        if (data.value > 0) {
            isSleepTimerSetToEndOfCurrentPlay = false

            setLog(TAG, "now time:" + now.timeInMillis)
            /*now.set(Calendar.MINUTE,
                (now.timeInMillis + (data.value * 60 * 1000)).toInt()
            )*/
            val date = Date(now.timeInMillis + (data.value.toInt() * 60 * 1000))
            val minute = date.time
            SharedPrefHelper.getInstance().save(PrefConstant.SLEEP_TIMER, minute)
            setLog("sleepTimer", "BaseActivity-onSleepTimeChangeItemClick-true")
            setSleepTimer()
        } else {
            setLog("sleepTimer", "BaseActivity-onSleepTimeChangeItemClick-false")
            removeSleepTimerCallback()
            isSleepTimerSetToEndOfCurrentPlay = true
            if (audioPlayer != null && audioPlayer?.duration != null && audioPlayer?.duration!! > 0) {
                /*val date = Date(now.timeInMillis + audioPlayer?.duration!!)
                val minute = date.time
                SharedPrefHelper.getInstance().save(PrefConstant.SLEEP_TIMER, minute)
                setSleepTimer()*/
            }
        }
    }

    private fun onSleepTimeOver() {
        removeSleepTimerCallback()
        //tvSleepTimer?.visibility = View.GONE
        if (audioPlayer != null) {
            if (audioPlayer?.isPlaying!!) {
                audioPlayer?.pause()
            }

            if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable==true){
                BaseFragment.castPlayer?.pause()
            }
            //val currentPosition = audioPlayer?.currentPosition
            /* val duration = audioPlayer?.duration
            setLog("onSleepTimeOver", "onSleepTimeOver-duration-$duration")
            if (duration != null && duration > 5000){
                audioPlayer?.seekTo(duration - 5000)
            }*/

        }
        isSleepTimerSetToEndOfCurrentPlay = false
        SharedPrefHelper.getInstance().clearSleepTimerData()
        Handler(Looper.myLooper()!!).postDelayed({
            isNextSongInProgress = false
        }, 1000)
    }

    override fun onPlayerError(error: PlaybackException) {
        super<Player.Listener>.onPlayerError(error)
        setLog("playerStates", "onPlayerError() BaseActivity-TYPE-error-$error")
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
            || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            || error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED) {
            if (maxPlayerErrorRetryCount < 3) {
                //setLog("ExoSourceError", "currentPosition - ${songDataList?.get(nowPlayingCurrentIndex())?.title}")
                if (nowPlayingCurrentIndex() != audioPlayer?.currentWindowIndex && nowPlayingCurrentIndex() < audioPlayer?.currentWindowIndex!!) {
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()) {
                        setLog(
                            "callUserStreamUpdate1",
                            "onPlayerError-TYPE_SOURCE-playNextSong title:${
                                songDataList?.get(
                                    nowPlayingCurrentIndex()
                                )?.title
                            }"
                        )
                        callUserStreamUpdate(-1, songDataList?.get(nowPlayingCurrentIndex()), nowPlayingCurrentIndex())
                    }
                    //callUserStreamUpdate(STREAM_POSITION_PREVIOUS)
                    val nextIndex = nowPlayingCurrentIndex() + 1
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nextIndex) {
                        playNextSong(false)
                    }
                    setLog("ExoSourceError-1", "PlayNextSong")
                } else if (nowPlayingCurrentIndex() != audioPlayer?.currentWindowIndex && nowPlayingCurrentIndex() > audioPlayer?.currentWindowIndex!!) {
                    val previousIndex = nowPlayingCurrentIndex() - 1
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > previousIndex) {
                        playPreviousSong()
                    }
                    setLog("ExoSourceError-2", "PlayPreviousSong")
                } else if (nowPlayingCurrentIndex() == audioPlayer?.currentWindowIndex) {
                    /*if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()){
                        setLog(
                            "callUserStreamUpdate1",
                            "onPlayerError-TYPE_SOURCE-playCurrentSong title:${songDataList?.get(nowPlayingCurrentIndex())?.title}"
                        )
                        callUserStreamUpdate(-1, songDataList?.get(nowPlayingCurrentIndex()), nowPlayingCurrentIndex())
                    }*/
                    //callUserStreamUpdate(STREAM_POSITION_CURRENT)
                    if (error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED){
                        //This error occur when something whent wrong with content file
                        val nextIndex = nowPlayingCurrentIndex() + 1
                        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nextIndex) {
                            playNextSong(false)
                        }
                    }else{
                        val nextIndex = nowPlayingCurrentIndex()
                        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nextIndex) {
                            playCurrentSong()
                        }
                    }

                    /*if (maxPlayerErrorRetryCount > 0) {
                        callStreamFailedEvent(error)
                        setLog("ExoSourceError-3", "callStreamFailedEvent")
                    }*/
                    setLog("ExoSourceError-4", "PlayCurrentSong")
                }

                maxPlayerErrorRetryCount++
            } else {
                callStreamFailedEvent(error)
                setLog("ExoSourceError-4", "callStreamFailedEvent")
            }
        }
    }

    private fun callStreamFailedEvent(
        error: PlaybackException
    ) {
        val currentIndex = nowPlayingCurrentIndex()
        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > currentIndex) {
            val hashMap = HashMap<String, String>()
            val mSongTrack = songDataList?.get(currentIndex)

            hashMap.put(EventConstant.CONNECTION_TYPE_EPROPERTY, ConnectionUtil.NETWORK_TYPE)
            hashMap.put(
                EventConstant.CONSUMPTION_TYPE_EPROPERTY,
                EventConstant.CONSUMPTIONTYPE_ONLINE
            )
            var newContentId=mSongTrack?.id
            var contentIdData=newContentId.toString().replace("playlist-","")
            hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)
            hashMap.put(
                EventConstant.CONTENTTYPE_EPROPERTY,
                Utils.getContentTypeNameForStream("" + mSongTrack?.contentType)
            )
            hashMap.put(EventConstant.DURATION_EPROPERTY, "00:00")
            hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "" + error.errorCodeName)
            hashMap.put(EventConstant.ERRORTYPE_EPROPERTY, "TYPE_SOURCE")
            hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY, "Audio Player")
            hashMap.put(EventConstant.PCODE_EPROPERTY, mSongTrack?.pName!!)
            hashMap.put(EventConstant.SCODE_EPROPERTY, mSongTrack?.pSubName!!)
            hashMap.put(EventConstant.AP_EPROPERTY, "")
            hashMap.put(EventConstant.BUFF_EPROPERTY, "")
            hashMap.put(
                EventConstant.SOURCEPAGE_EPROPERTY,
                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + mSongTrack?.heading
            )
            hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY, mSongTrack?.heading!!)

            EventManager.getInstance().sendEvent(StreamFailedEvent(hashMap))
             streamName = "streamFailed"
        }

    }

    fun getAudioPlayerPlayingStatus(): Int {
        if (audioPlayer != null && audioPlayer?.isPlaying!!) {
            return playing
        } else if (audioPlayer != null && !audioPlayer?.isPlaying!!) {
            return pause
        } else {
            return noneAudio
        }
    }

    private fun audioPlayerActionBroadcast(action: Int) {
        setLog(
            "setPageBottomSpacing",
            "audioPlayerActionBroadcast--$bottomNavigationHeight , $miniplayerHeight"
        )
        val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
        intent.putExtra(audioAction, action)
        if (action == playing) {

        } else if (action == pause) {

        } else if (action == noneAudio) {

        }
        intent.putExtra("EVENT", Constant.AUDIO_PLAYER_RESULT_CODE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    public fun refreshCurrentFragment(
        activity: FragmentActivity,
        fragment: Fragment
    ) {
        if (Utils?.getCurrentFragment(activity) != null) {
            if (!Utils?.getCurrentFragment(activity)?.javaClass?.simpleName.equals(
                    fragment.javaClass.simpleName, true
                )
            ) {
                addFragment(
                    R.id.fl_container,
                    Utils?.getCurrentFragment(activity)!!, fragment, false
                )
            } else {
                activity.supportFragmentManager.beginTransaction().remove(fragment).commit()
                activity.supportFragmentManager.popBackStack()
                addFragment(
                    R.id.fl_container,
                    Utils?.getCurrentFragment(activity)!!, fragment, false
                )
            }
        }
    }

    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        setLog(
            "bndlMeter-1",
            "eventTime:- ${eventTime.totalBufferedDurationMs} AND loadEventInfo:- ${
                convertByteToHumanReadableFormat(loadEventInfo.bytesLoaded)
            } AND mediaLoadData:- ${mediaLoadData.mediaStartTimeMs}"
        )
        super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        setLog(
            "bndlMeter-2",
            "eventTime:- ${eventTime.totalBufferedDurationMs} AND loadEventInfo:- ${
                convertByteToHumanReadableFormat(loadEventInfo.bytesLoaded)
            } AND mediaLoadData:- ${mediaLoadData.mediaStartTimeMs}"
        )
        super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadCanceled(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        setLog(
            "bndlMeter-3",
            "eventTime:- ${eventTime.totalBufferedDurationMs} AND loadEventInfo:- ${
                convertByteToHumanReadableFormat(loadEventInfo.bytesLoaded)
            } AND mediaLoadData:- ${mediaLoadData.mediaStartTimeMs}"
        )
        super.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        setLog(
            "bndlMeter-4",
            "eventTime:- ${eventTime.totalBufferedDurationMs} AND loadEventInfo:- ${
                convertByteToHumanReadableFormat(loadEventInfo.bytesLoaded)
            } AND mediaLoadData:- ${mediaLoadData.mediaStartTimeMs}"
        )
        super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
    }

    override fun onBandwidthEstimate(
        eventTime: AnalyticsListener.EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
        setLog(
            "bndlMeter-5",
            "eventTime:- ${totalLoadTimeMs} AND loadEventInfo:- ${
                convertByteToHumanReadableFormat(totalBytesLoaded)
            } AND mediaLoadData:- ${convertByteToHumanReadableFormat(bitrateEstimate / 8)}"
        )
        displayBandwidth(totalBytesLoaded)
        super.onBandwidthEstimate(eventTime, totalLoadTimeMs, totalBytesLoaded, bitrateEstimate)
    }

    private fun displayBandwidth(bytes: Long) {
        /*if (audioPlayer != null && audioPlayer?.isPlaying!!) {
            tvBandwidthMeter?.text = convertByteToHumanReadableFormat(bytes) + "/s"
        } else {
            tvBandwidthMeter?.text = convertByteToHumanReadableFormat(0) + "/s"
        }*/
    }

    fun loadAds() {
        loadAudioAds()

    }

    val playerAdsFirstTimeVisibleHandler = Runnable {
        setLog("PlayerAds:-", "playerAds=> FirstTimeAds.")
        isFirstTimeAdsLoaded = false
        showPlayerAds()
    }

    val playerAdsDisappearHandler = Runnable {
        setLog("PlayerAds:-", "playerAds=> HideAds.")
        hidePlayerAds()
    }

    val playerAdsIntervalHandler = Runnable {
        setLog("PlayerAds:-", "playerAds=> afterIntervalTimeAds.")
        showPlayerAds()
    }

    fun showPlayerAds() {
        setLog(
            "PlayerAds:-",
            "playerAds=> showPlayerAds()-- isScreenOn:${CommonUtils.isScreenOn(this)}"
        )
        playerAdsHandler.postDelayed(
            playerAdsDisappearHandler,
            playerAdsDisappearTimeInSeconds * 1000
        )
    }

    fun hidePlayerAds() {
        setLog("PlayerAds:-", "playerAds=> showPlayerAds()")
        playerAdsHandler.postDelayed(
            playerAdsIntervalHandler,
            playerAdsDisplayIntervalInSecond * 1000
        )
    }

    private fun removePlayerAdsCallBack() {
        setLog("PlayerAds:-", "playerAds=> removePlayerAdsCallBack()")
        if (playerAdsFirstTimeVisibleHandler != null) {
            playerAdsHandler?.removeCallbacks(playerAdsFirstTimeVisibleHandler)
        }
        if (playerAdsDisappearHandler != null) {
            playerAdsHandler?.removeCallbacks(playerAdsDisappearHandler)
        }
        if (playerAdsIntervalHandler != null) {
            playerAdsHandler?.removeCallbacks(playerAdsIntervalHandler)
        }
    }

    fun loadBottomAds(isOtherAdId:Boolean = false, otherStickyAdId:String = "") {
        if (CommonUtils.isHomeScreenBannerAds()) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(5000)
                var stickyAdId = Constant.AD_UNIT_ID_STICKY_LIST
                if (isOtherAdId && !TextUtils.isEmpty(otherStickyAdId)){
                    stickyAdId = otherStickyAdId
                }
                val result: Deferred<AdManagerAdView> = CoroutineScope(Dispatchers.Main).async {
                    CommonUtils.loadBannerAds(
                        this@BaseActivity,
                        stickyAdId,
                        AdSize.BANNER,
                        sticky_ad_view_container
                    )
                }
                val adView: AdManagerAdView = result.await()
                adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        setLog("loadBottomAds", "Loaded")
                        val colors = intArrayOf(
                            Color.parseColor("#2b68e8"),
                            Color.parseColor("#2ca1f7")
                        )
                        val position = floatArrayOf(
                            0f,
                            1f
                        )
                        //val cornerRadius = 7f
                        val cornerRadius = floatArrayOf(
                            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                            resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                            resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat(),//Bottom Right
                            resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat(),//Bottom Right
                            resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat(),//bottom left
                            resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat()//bottom left
                        )
                        val startX = 84.564f
                        val startY = 44.184f
                        val endX = 268.92f
                        val endY = 2.184f
                        CommonUtils.applyAppButtonGradient(
                            startX,
                            startY,
                            endX,
                            endY,
                            cornerRadius,
                            colors,
                            position,
                            this@BaseActivity,
                            clRemoveAds
                        )
                        isBottomStickyAdLoaded = true
                        if (isBottomNavigationVisible) {
                            showStickyAds()
                        }
                        clRemoveAds?.setOnClickListener {
                            Constant.screen_name ="Home Screen"
                            CommonUtils.openSubscriptionDialogPopup(
                                this@BaseActivity,
                                PlanNames.SVOD.name,
                                "",
                                true,
                                null,
                                "",
                                null,Constant.drawer_remove_ads
                            )
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Code to be executed when an ad request fails.
                        setLog("loadBottomAds", "Failed-" + adError.message)
                        isBottomStickyAdLoaded = false
                    }

                    override fun onAdOpened() {
                        // Code to be executed when an ad opens an overlay that
                        // covers the screen.
                    }

                    override fun onAdClicked() {
                        // Code to be executed when the user clicks on an ad.
                    }

                    override fun onAdClosed() {
                        // Code to be executed when the user is about to return
                        // to the app after tapping on an ad.
                        isBottomStickyAdLoaded = false
                    }
                }
            }
        }
    }


    private fun loadGoogleImaAds() {
        setLog("PlayerAds:-", "googleIma=> isAdsLoadRequestInProgress-$isAdsLoadRequestInProgress -- isAudioAdPlaying-$isAudioAdPlaying -- totalSongsPlayedAfterLastAudioAd-$totalSongsPlayedAfterLastAudioAd -- playAudioAdsAfterCounts-$playAudioAdsAfterCounts")
        if (!isAdsLoadRequestInProgress && !isAudioAdPlaying && totalSongsPlayedAfterLastAudioAd > playAudioAdsAfterCounts) {
            isAdsLoadRequestInProgress = true
            setLog("PlayerAds:-", "googleIma=> Request for ads.")
/*            if (companionAdSlotFrame != null) {
                audioPlayerService?.initializeAds(this, companionAdSlotFrame)
            }*/
            if (audioPlayerService?.imaService != null) {
                //audioAdsLoaded(googleImaAudioAds)
                setLog("BannerAddShowing", "True")

                audioPlayerService?.imaService?.setImaAdsListener(imaAudioAdsListener)
            }
            setLog("BannerAddShowing", "false")

            audioPlayerService?.requestAd(Constant.AD_AUDIO_MANGER_AD_UNIT_ID)
        }
    }

    fun loadTritonAds() {
        try {
            setLog("totalSondLasPlayed " + totalSongsPlayedAfterLastAudioAd.toString() + " " +playAudioAdsAfterCounts.toString())
            if (!isAudioAdPlaying && totalSongsPlayedAfterLastAudioAd > playAudioAdsAfterCounts) {
                isAdsLoadRequestInProgress = true
            }
        }catch (e:Exception){

        }
    }

    private fun playAudioAd(ad: Bundle) {
        try {
            setLog("PlayerAds:-", "tritonAds=> Ad buffering")
            mAudioPlayer = MediaPlayer()
            //mAudioPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mAudioPlayer.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            mAudioPlayer.setOnCompletionListener(this)
            mAudioPlayer.setOnErrorListener(this)
            mAudioPlayer.setOnPreparedListener { mp ->
                setLog("PlayerAds:-", "tritonAds=> Ad started.")
                mp.start()
                pausePlayer()
                audioAdsStarted(tritonAudioAds)
            }

            /*val renderersFactory = DefaultRenderersFactory(this)
            var simpleExoplayer = SimpleExoPlayer.Builder(this,renderersFactory).setHandleAudioBecomingNoisy(true)
                .build()
            audioAdUrl = ad.getString(Ad.URL).toString()

            if (!TextUtils.isEmpty(audioAdUrl)) {
                setStatus("Start audio buffering")
                val track = Track()
                track.url = audioAdUrl
                val mediaSource = buildMediaSource(track)
                simpleExoplayer.setMediaSource(mediaSource)
                simpleExoplayer.prepare()
                simpleExoplayer.playWhenReady = true
            }*/
        } catch (e: Exception) {
            setLog("PlayerAds:-", "tritonAds=> Audio prepare exception: $e")
            updateAudioAdPlayingStatusAndProvider(false, tritonAudioAds)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopTritonAudioAd()
        setLog("PlayerAds:-", "tritonAds=> Ad completed.")
        updateAudioAdPlayingStatusAndProvider(false, tritonAudioAds)
        playPlayer()
        audioAdsCompleted()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        stopTritonAudioAd()
        setLog("PlayerAds:-", "tritonAds=> Playback error: $what/$extra")
        updateAudioAdPlayingStatusAndProvider(false, tritonAudioAds)
        playPlayer()
        val audioAdPreference = CommonUtils.getAudioAdPreference()
        setLog(
            "PlayerAds:-",
            "loadAudioAds: onError-tritonAds-audioAdPreference:${audioAdPreference}"
        )
        if (audioAdPreference.firstPriority.equals("google", true)) {
            setLog("PlayerAds:-", "loadInterstrialAds")
            loadInterstitialAds()
        } else {
            loadGoogleImaAds()
        }

        return true
    }

    private fun stopTritonAudioAd() {
        if (mAudioPlayer.isPlaying) {
            setLog("PlayerAds:-", "Stop playing audio ads")
            mAudioPlayer.stop()
            mAudioPlayer.release()
            // Clear the previous banner content without destroying the view.
            mBannersWrapper?.clear()
        }
    }

    fun updateAudioAdsParams() {
        setLog("PlayerAds:-", "Audio ads param updated")
        playAudioAdsAfterCounts = CommonUtils.getAudioAdPreference().servingFrequency
        totalSongsPlayedAfterLastAudioAd = 0
    }

    private val mBluthoothBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            /*val action = intent.action
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            setLog("bluetoothBroadcast-3", "onReceive()")
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                var connectedData =
                    ConnectedDialogModel(4, getString(R.string.this_phone), R.string.icon_device)
                if (state == BluetoothAdapter.STATE_ON) {
                    if (device != null) {
                        connectedData = ConnectedDialogModel(
                            3,
                            device?.name.toString(),
                            R.string.icon_bluetooth
                        )
                    }
                    setLog("bluetoothBroadcast-8", "STATE_ON")
                    setLog(
                        "bluetoothBroadcast-8",
                        "Bluetooth Device STATE_ON: - ${connectedData.Title}"
                    )
                } else {
                    setLog("bluetoothBroadcast-8", "STATE_OFF")
                    setLog(
                        "bluetoothBroadcast-8",
                        "Bluetooth Device STATE_OFF: - ${connectedData.Title}"
                    )
                }
                onConnectedDeviceMenuItemClick(connectedData)
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                var connectedData =
                    ConnectedDialogModel(4, getString(R.string.this_phone), R.string.icon_device)
                if (device != null && !TextUtils.isEmpty(device.name)) {
                    connectedData =
                        ConnectedDialogModel(3, device?.name.toString(), R.string.icon_bluetooth)
                }

                onConnectedDeviceMenuItemClick(connectedData)
                setLog("bluetoothBroadcast-4", "ACL_CONNECTED")
                setLog(
                    "bluetoothBroadcast-4",
                    "Bluetooth Device Connected: - ${connectedData.Title}"
                )
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                setLog("bluetoothBroadcast-6", "ACTION_DISCOVERY_FINISHED")
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                setLog("bluetoothBroadcast-7", "ACTION_ACL_DISCONNECT_REQUESTED")
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                val connectedData =
                    ConnectedDialogModel(4, getString(R.string.this_phone), R.string.icon_device)
                if (device != null) {
                    setLog(
                        "bluetoothBroadcast-5",
                        "Bluetooth Device Disconnected 1: - ${device.name}"
                    )
                }

                onConnectedDeviceMenuItemClick(connectedData)
                setLog("bluetoothBroadcast-5", "ACTION_ACL_DISCONNECTED")
                setLog(
                    "bluetoothBroadcast-5",
                    "Bluetooth Device Disconnected 2: - ${connectedData.Title}"
                )
            }*/
        }
    }

    private fun setBluetoothBroadcast() {
        /*val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        this.registerReceiver(mBluthoothBroadcastReceiver, filter)
        setLog("bluetoothBroadcast-1", "setBluetoothBroadcast()")
        val device = CommonUtils.getCurrentConnectedBTDevice(this)
        if (device != null && !TextUtils.isEmpty(device.name)) {
            val connectedData =
                ConnectedDialogModel(3, device?.name.toString(), R.string.icon_bluetooth)
            onConnectedDeviceMenuItemClick(connectedData)
        }*/
    }

    private fun removeBluetoothBroadcast() {
        if (mBluthoothBroadcastReceiver != null) {
            try {
                setLog("bluetoothBroadcast-2", "removeBluetoothBroadcast()")
                this.unregisterReceiver(mBluthoothBroadcastReceiver)
            } catch (e: Exception) {

            }
        }
    }

    interface OnImaAdsListener {
        fun onImaAdEvent(adEvent: AdEvent)
        fun onImaAdError(adErrorEvent: AdErrorEvent)
    }

    val imaAudioAdsListener = object : OnImaAdsListener {
        override fun onImaAdError(adErrorEvent: AdErrorEvent) {
            setLog("PlayerAds:-", "googleIma=> onError ${adErrorEvent.error.message}")
            setLog("PlayerAds:-", "googleIma=> onError ${adErrorEvent.error.errorCode}")
            audioAdsOnError(googleImaAudioAds)
            val audioAdPreference = CommonUtils.getAudioAdPreference()
            setLog(
                "PlayerAds:-",
                "loadAudioAds: onImaAdError-googleImaAds-audioAdPreference:${audioAdPreference}"
            )
            if (audioAdPreference.firstPriority.equals("triton", true)) {
                setLog("PlayerAds:-", "loadInterstrialAds")
                loadInterstitialAds()
            } else {
                setLog("PlayerAds:-", "onImaAdError-loadTritonAds")
                loadTritonAds()
            }


        }

        override fun onImaAdEvent(adEvent: AdEvent) {
            setLog("PlayerAds:-", "googleIma=> onImaAdEvent type:${adEvent.type}")
            when (adEvent.type) {

                AdEventType.LOADED -> {
                    setLog("PlayerAds:-", "googleIma=> Ad loaded.")
                    audioAdsLoaded(googleImaAudioAds)
                }
                AdEventType.STARTED -> {
                    setLog("PlayerAds:-", "googleIma=> Ad started.")
                    audioAdsStarted(googleImaAudioAds)
                }
                AdEventType.COMPLETED, AdEventType.ALL_ADS_COMPLETED -> {
                    setLog("PlayerAds:-", "googleIma=> Ad completed.")
                    updateAudioAdPlayingStatusAndProvider(false, googleImaAudioAds)
                    audioAdsCompleted()
                }
                else -> {}
            }
        }
    }

    private fun updateAudioAdPlayingStatus(status: Boolean) {
        isAudioAdPlaying = status
        isAdsLoadRequestInProgress = status
        setLog("PlayerAds:-", "AdProvider-${getCurrentAudioAdProvider()}=> isAudioAdPlaying-$isAudioAdPlaying")
    }

    private fun updateAudioAdProvider(audioAdProvider: Int) {
        currentAudioAdsType = audioAdProvider
        setLog("PlayerAds:-", "audioAdsProvider=> $currentAudioAdsType")
    }

    fun getCurrentAudioAdProvider(): Int {
        return currentAudioAdsType
    }

    fun updateAudioAdPlayingStatusAndProvider(status: Boolean, audioAdProvider: Int) {
        updateAudioAdPlayingStatus(status)
        updateAudioAdProvider(audioAdProvider)
        showHideAudioAdView()
    }

    private fun showHideAudioAdView() {
        setLog(
            "PlayerAds:-",
            "showHideAudioAdView()=> isAudioAdPlaying-$isAudioAdPlaying ${getCurrentAudioAdProvider()}"
        )

        if (isAudioAdPlaying) {
            if (getCurrentAudioAdProvider() == googleImaAudioAds) {
                //setLog("PlayerAds:-", "googleIma=> Ad banner show.")
//                companionAdSlotFrame?.show()
                mBannersWrapper?.clear()
            } else if (getCurrentAudioAdProvider() == tritonAudioAds) {
                setLog("PlayerAds:-", "tritonAds=> Ad banner show.")
//                companionAdSlotFrame?.hide()
            } else {
                //setLog("PlayerAds:-", "showHideAudioAdView() -- All Ad banner hide.")
//                companionAdSlotFrame?.hide()
                mBannersWrapper?.clear()
            }
        } else {
            //setLog("PlayerAds:-", "showHideAudioAdView() -- No audio Ad playing.")
            //setLog("PlayerAds:-", "showHideAudioAdView() -- All Ad banner hide.")
//            companionAdSlotFrame?.hide()
            mBannersWrapper?.clear()
        }
    }

    private fun loadAudioAds() {
        setLog(TAG, "loadAudioAds: loadAudioAds-Call")
        if (CommonUtils.isAudioAds()) {
            val audioAdPreference = CommonUtils.getAudioAdPreference()
            setLog(TAG, "loadAudioAds: audioAdPreference:${audioAdPreference}")
            if (audioAdPreference.firstPriority.equals("google", true)) {
                loadGoogleImaAds()
            } else if (audioAdPreference.firstPriority.equals("triton", true)) {
                loadTritonAds()
            } else {
                loadGoogleImaAds()
            }
        }
    }

    private fun audioAdsStarted(audioAdProvider: Int) {
        removePlayerAdsCallBack()
        updateAudioAdsParams()
        updateAudioAdPlayingStatusAndProvider(true, audioAdProvider)
        if (onSwipablePlayerListener != null) {
            var adsDuration = 0L
            if (audioAdProvider == googleImaAudioAds) {
                if (audioPlayer != null && audioPlayer?.duration != null) {
                    adsDuration = audioPlayer?.duration!!
                }
            } else if (audioAdProvider == tritonAudioAds) {
                if (mAudioPlayer != null) {
                    adsDuration = mAudioPlayer.duration.toLong()
                }
            }
            val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.AUDIO_PLAYER_ADS_STARTED_RESULT_CODE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            onSwipablePlayerListener?.onAudioAdsStarted(adsDuration)
        }
    }

    private fun audioAdsLoaded(audioAdProvider: Int) {
        updateAudioAdPlayingStatusAndProvider(false, audioAdProvider)
        if (onSwipablePlayerListener != null) {
            val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.AUDIO_PLAYER_ADS_STARTED_RESULT_CODE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    private fun audioAdsOnError(audioAdProvider: Int) {
        updateAudioAdPlayingStatusAndProvider(false, audioAdProvider)
        audioAdsCompleted()
    }

    fun audioAdsCompleted() {
        if (onSwipablePlayerListener != null) {
            val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.AUDIO_PLAYER_ADS_END_RESULT_CODE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            onSwipablePlayerListener?.onAudioAdsCompleted()
        }
    }

    override fun onAudioSessionIdChanged(
        eventTime: AnalyticsListener.EventTime,
        audioSessionId: Int
    ) {
        super<AnalyticsListener>.onAudioSessionIdChanged(eventTime, audioSessionId)
        this.audioSessionId = audioSessionId
        setLog("Equalizer", "SessionId-$audioSessionId")
        setLog("Equalizer", "SessionId2-" + audioPlayer?.audioSessionId)
        val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, this.audioSessionId)
        equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(equalizerIntent)
    }

    fun getAudioSession(): Int {
        if (audioPlayer != null) {
            audioSessionId = audioPlayer?.audioSessionId!!
            setLog("Equalizer", "SessionId3-$audioSessionId")
        }
        return audioSessionId
    }


    public fun shareStoryImage(
        shareStory: CommonUtils.STORY_SHARE,
        storyType: CommonUtils.STORY_TYPE,
        contentTitle: String,
        contentUrl: String,
        contentImage: String
    ) {
        try {
            val appId = getString(R.string.facebook_app_id)

            if (storyType == CommonUtils.STORY_TYPE.PHOTO) {
                val urlImage = URL(contentImage)
                // async task to get / download bitmap from url
                val result: Deferred<Bitmap?> = GlobalScope.async {
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    // get the downloaded bitmap
                    var bitmap: Bitmap? = null
                    if (selectedStoryDataModel != null && selectedStoryDataModel?.currentTrack?.albumArtBitmap != null) {
                        bitmap = selectedStoryDataModel?.currentTrack?.albumArtBitmap
                        setLog(
                            "TAG",
                            " share fb story : albumArtBitmap" + selectedStoryDataModel?.currentTrack?.albumArtBitmap
                        )
                        setLog(
                            "TAG",
                            " share fb story : color" + selectedStoryDataModel?.currentTrack?.statusBarColor
                        )
                    } else {
                        bitmap = result.await()
                    }



                    setLog("TAG", " share fb story : launch")

                    if (bitmap != null) {
                        // if downloaded then saved it to internal storage
                        bitmap?.apply {
                            var photoFile: File? = null
                            try {
                                photoFile = saveToInternalStorage()

                                val source_application = applicationContext.packageName

                                if (photoFile != null) {
                                    setLog("TAG", " share fb story : photoFile" + photoFile)

                                    val photoURI = FileProvider.getUriForFile(
                                        applicationContext!!,
                                        "com.hungama.myplay.activity",
                                        photoFile
                                    )
                                    setLog("TAG", " share fb story : photoURI" + photoURI)
                                    setLog("TAG", " share fb story : appId" + appId)
                                    setLog(
                                        "TAG",
                                        " share fb story : source_application" + source_application
                                    )
                                    setLog(
                                        "TAG",
                                        " share fb story resolveActivity ShareDialog called: shareStory" + shareStory
                                    )
                                    if (shareStory == CommonUtils.STORY_SHARE.FACEBOOK) {

                                        /**
                                         * story start op1
                                         */
//                                    val intent = Intent("com.facebook.stories.ADD_TO_STORY")
//                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                    intent.setType("image/jpeg");
//                                    intent.setDataAndType(photoURI,  "image/*")
////                                    intent.putExtra("interactive_asset_uri", photoURI)
//                                    intent.putExtra("content_title", contentTitle)
//                                    intent.putExtra("content_url", contentUrl)
////                                    intent.putExtra("attribution_url", contentUrl)
//                                    if(selectedStoryDataModel!=null&&selectedStoryDataModel?.currentTrack?.statusBarColor!!>0){
//                                        intent.putExtra("top_background_color", "#"+selectedStoryDataModel?.currentTrack?.statusBarColor)
//                                        intent.putExtra("bottom_background_color", "#FF00FF")
//                                    }
//
//
//                                   grantUriPermission(
//                                        "com.facebook.katana", photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                                    setLog("TAG", " share fb story resolveActivity :"+packageManager?.resolveActivity(intent, 0))
//
//                                    if (packageManager?.resolveActivity(intent, 0) != null) {
//                                        startActivityForResult(intent, 0)
//                                        setLog("TAG", " share fb story launch :")
//                                    }else{
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Facebook story sharing have failed",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }
                                        /**
                                         * story end op1
                                         */

                                        /**
                                         * story start op2
                                         */

                                        val photo = SharePhoto.Builder()
                                            .setImageUrl(photoURI)
                                            .setCaption(contentTitle)
                                            .setUserGenerated(true)
                                            .build()
                                        val story = ShareStoryContent.Builder()
                                            .setBackgroundAsset(photo)
                                            .setContentUrl(Uri.parse(contentUrl))
                                            .setAttributionLink(contentUrl)
                                            .setShareHashtag(
                                                ShareHashtag.Builder()
                                                    .setHashtag("#HungamaMusic")
                                                    .build()
                                            )
                                            .build()
                                        ShareDialog(this@BaseActivity)?.show(story);
                                        setLog(
                                            "TAG",
                                            " share fb story resolveActivity ShareDialog called: story" + story
                                        )
                                        /**
                                         * story end op2
                                         */
                                    } else if (shareStory == CommonUtils.STORY_SHARE.INSTAGRAM) {
                                        /*val intent = Intent("com.instagram.share.ADD_TO_STORY")
                                        intent.putExtra(
                                            "source_application",
                                            source_application
                                        );

                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.setDataAndType(photoURI, "image/*")
//                                    intent.putExtra("interactive_asset_uri", photoURI)
                                        intent.putExtra("content_url", contentUrl)
                                        intent.putExtra("content_title", contentTitle)
//                                    intent.putExtra("attribution_url", contentUrl)
                                        setLog("ShareStory", "Instagram - selectedStoryDataModel-$selectedStoryDataModel - statusBarColor-${selectedStoryDataModel?.currentTrack?.statusBarColor}")
                                        if (selectedStoryDataModel != null && selectedStoryDataModel?.currentTrack?.statusBarColor!! > 0) {
                                            intent.putExtra(
                                                "top_background_color",
                                                "#" + selectedStoryDataModel?.currentTrack?.statusBarColor
                                            )
                                            intent.putExtra("bottom_background_color", "#FF00FF")
                                        }


                                        grantUriPermission(
                                            "com.instagram.android",
                                            photoURI,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                                        setLog(
                                            "TAG",
                                            " share fb story resolveActivity :" + applicationContext?.packageManager?.resolveActivity(
                                                intent,
                                                0
                                            )
                                        )

                                        if (packageManager?.resolveActivity(intent, 0) != null) {
                                            startActivityForResult(intent, 0)
                                            setLog("TAG", " share fb story launch :")
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "Instagram story sharing have failed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }*/

                                         */
                                        // Define image asset URI
                                        // Define image asset URI
                                        val stickerAssetUri = photoURI
                                        val sourceApplication = source_application

// Instantiate implicit intent with ADD_TO_STORY action,
// sticker asset, and background colors

// Instantiate implicit intent with ADD_TO_STORY action,
// sticker asset, and background colors
                                        val intent = Intent("com.instagram.share.ADD_TO_STORY")
                                        intent.putExtra("source_application", sourceApplication)

                                        intent.type = "image/*"
                                        intent.putExtra("interactive_asset_uri", stickerAssetUri)
                                        setLog("ShareStory", "Instagram - selectedStoryDataModel-$selectedStoryDataModel - statusBarColor-${selectedStoryDataModel?.currentTrack?.statusBarColor}")
                                        if (selectedStoryDataModel != null) {
                                            try {
                                                val hexColor = String.format(
                                                    "#%06X",
                                                    0xFFFFFF and selectedStoryDataModel?.currentTrack?.statusBarColor!!
                                                )
                                                setLog("ShareStory", "Instagram - hexColor-$hexColor")
                                                intent.putExtra("top_background_color", hexColor)
                                                intent.putExtra("bottom_background_color", hexColor)
                                            }catch (e:Exception){

                                            }

                                        }


// Instantiate activity and verify it will resolve implicit intent

// Instantiate activity and verify it will resolve implicit intent
                                        grantUriPermission(
                                            "com.instagram.android",
                                            stickerAssetUri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                        if (getPackageManager()
                                                .resolveActivity(intent, 0) != null
                                        ) {
                                            startActivityForResult(intent, 0)
                                        }
                                    }

                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Story sharing have failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    setLog("TAG", " share fb story : stickerAssetUri is null")
                                }
                            } catch (ex: Exception) {
                                setLog("TAG", " share fb story Exception:${ex.message}")
                            }
                        }

                    }


                }
            } else if (storyType == CommonUtils.STORY_TYPE.VIDEO) {
                val urlImage = URL(contentImage)
                // async task to get / download bitmap from url
                val result: Deferred<Bitmap?> = GlobalScope.async {
                    setLog("TAG", " share fb story : toBitmap")
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.Main) {
                    // get the downloaded bitmap
                    var bitmap: Bitmap? = null
                    if (selectedStoryDataModel != null && selectedStoryDataModel?.currentTrack?.albumArtBitmap != null) {
                        bitmap = selectedStoryDataModel?.currentTrack?.albumArtBitmap
                        setLog(
                            "TAG",
                            " share fb story : albumArtBitmap" + selectedStoryDataModel?.currentTrack?.albumArtBitmap
                        )
                        setLog(
                            "TAG",
                            " share fb story : color" + selectedStoryDataModel?.currentTrack?.statusBarColor
                        )
                    } else {
                        bitmap = result.await()
                    }

                    setLog("TAG", " share fb story : launch")

                    if (bitmap != null) {
                        // if downloaded then saved it to internal storage
                        bitmap?.apply {
                            var photoFile: File? = null
                            try {
                                photoFile = saveToInternalStorage()

                                val source_application = applicationContext.packageName

                                if (photoFile != null) {
                                    setLog("TAG", " share fb story : photoFile" + photoFile)

                                    val photoURI = FileProvider.getUriForFile(
                                        applicationContext!!,
                                        "com.hungama.myplay.activity",
                                        photoFile!!
                                    )
                                    setLog("TAG", " share fb story : photoURI" + photoURI)
                                    setLog("TAG", " share fb story : appId" + appId)
                                    setLog(
                                        "TAG",
                                        " share fb story : source_application" + source_application
                                    )
                                    setLog(
                                        "TAG",
                                        " share fb story resolveActivity shareStory" + shareStory
                                    )
                                    if (shareStory == CommonUtils.STORY_SHARE.FACEBOOK) {

                                        /**
                                         * story start op1
                                         */
//                                    val intent = Intent("com.facebook.stories.ADD_TO_STORY")
//                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                    intent.setType("image/jpeg");
//                                    intent.setDataAndType(photoURI,  "image/*")
////                                    intent.putExtra("interactive_asset_uri", photoURI)
//                                    intent.putExtra("content_title", contentTitle)
//                                    intent.putExtra("content_url", contentUrl)
////                                    intent.putExtra("attribution_url", contentUrl)
//                                    if(selectedStoryDataModel!=null&&selectedStoryDataModel?.currentTrack?.statusBarColor!!>0){
//                                        intent.putExtra("top_background_color", "#"+selectedStoryDataModel?.currentTrack?.statusBarColor)
//                                        intent.putExtra("bottom_background_color", "#FF00FF")
//                                    }
//
//
//                                   grantUriPermission(
//                                        "com.facebook.katana", photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                                    setLog("TAG", " share fb story resolveActivity :"+packageManager?.resolveActivity(intent, 0))
//
//                                    if (packageManager?.resolveActivity(intent, 0) != null) {
//                                        startActivityForResult(intent, 0)
//                                        setLog("TAG", " share fb story launch :")
//                                    }else{
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Facebook story sharing have failed",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }
                                        /**
                                         * story end op1
                                         */

                                        /**
                                         * story start op2
                                         */

                                        val video = SharePhoto.Builder()
                                            .setImageUrl(photoURI)
                                            .setCaption(contentTitle)
                                            .setUserGenerated(true)
                                            .build()
                                        val story = ShareStoryContent.Builder()
                                            .setBackgroundAsset(video)
                                            .setContentUrl(Uri.parse(contentUrl))
                                            .setAttributionLink(contentUrl)
                                            .setShareHashtag(
                                                ShareHashtag.Builder()
                                                    .setHashtag("#HungamaMusic")
                                                    .build()
                                            )
                                            .build()
                                        ShareDialog(this@BaseActivity)?.show(story);
                                        setLog(
                                            "TAG",
                                            " share fb story resolveActivity ShareDialog called: story" + story
                                        )
                                        /**
                                         * story end op2
                                         */
                                    } else if (shareStory == CommonUtils.STORY_SHARE.INSTAGRAM) {
                                        val intent = Intent("com.instagram.share.ADD_TO_STORY")
                                        intent.putExtra("source_application", applicationContext?.packageName);

                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.setDataAndType(photoURI,  "video/*")
                                        intent.setType("video/*");
//                                    intent.putExtra("interactive_asset_uri", photoURI)
                                        intent.putExtra("content_url", contentUrl)
                                        intent.putExtra("content_title", contentTitle)
//                                    intent.putExtra("attribution_url", contentUrl)
                                        if(selectedStoryDataModel!=null&&selectedStoryDataModel?.currentTrack?.statusBarColor!!>0){
                                            intent.putExtra("top_background_color", "#"+selectedStoryDataModel?.currentTrack?.statusBarColor)
                                            intent.putExtra("bottom_background_color", "#FF00FF")
                                        }


                                        grantUriPermission(
                                            "com.instagram.android", photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                        setLog("TAG", " share fb story resolveActivity :"+applicationContext?.packageManager?.resolveActivity(intent, 0))

                                        if (packageManager?.resolveActivity(intent, 0) != null) {
                                            startActivityForResult(intent, 0)
                                            setLog("TAG", " share fb story launch :")
                                        }else{
                                            Toast.makeText(
                                                applicationContext,
                                                "Instagram story sharing have failed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Story sharing have failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    setLog("TAG", " share fb story : stickerAssetUri is null")
                                }
                            } catch (ex: Exception) {
                                setLog("TAG", " share fb story Exception:${ex.message}")
                            }
                        }

                    }


                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Facebook story sharing have failed",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    suspend fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            setLog("Exception", "BaseActivity-toBitmap-error-${e.message}")
            null
        }
    }


    // extension function to save an image to internal storage
    fun Bitmap.saveToInternalStorage(): File? {
        setLog("TAG", " share fb story : saveToInternalStorage")


        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File =
            applicationContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val fileInFolder = File.createTempFile(
            imageFileName,  /* prefix */
            ".png",  /* suffix */
            storageDir /* directory */
        )


        setLog("TAG", "share fb story :  fileInFolder : ${fileInFolder.absolutePath}");


        return try {
            // get the file output stream
            val stream: OutputStream = FileOutputStream(fileInFolder)
            // compress bitmap
            compress(Bitmap.CompressFormat.PNG, 100, stream)
            // flush the stream
            stream.flush()
            // close stream
            stream.close()
            setLog("TAG", " share fb story : saveToInternalStorage file" + fileInFolder.absolutePath)

            // return the saved image uri
            fileInFolder
        } catch (e: IOException) { // catch the exception
            e.printStackTrace()
            null
        }

    }
    private fun sendCoinsEarnEvent(points: Int, eventName: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (points > 0) {
                try {
                    val dataMap = HashMap<String, String>()
                    dataMap.put(EventConstant.ACTION_EPROPERTY, "" + eventName)
                    dataMap.put(EventConstant.VALUE, "" + points)
                    dataMap.put(EventConstant.TIME_STAMP, "" + DateUtils.getCurrentDateTimeForCoin())
                    setLog("CoinsCredited","${dataMap}")
                    EventManager.getInstance().sendEvent(EarnCoinsEvent(dataMap))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun updateUserCoins() {
        CoroutineScope(Dispatchers.IO).launch {
            var userCoinDetailRespModel = SharedPrefHelper?.getInstance()?.getObjectUserCoin(
                PrefConstant.USER_COIN
            )

            if (userCoinDetailRespModel?.actions != null) {
            } else {
                userCoinDetailRespModel = UserCoinDetailRespModel()
                val action = UserCoinDetailRespModel.Action()
                userCoinDetailRespModel?.actions = ArrayList()
                userCoinDetailRespModel.actions.add(action)
                setLog(
                    TAG,
                    "fillUI: after userCoinDetailRespModel123" + userCoinDetailRespModel?.actions?.get(0)?.total
                )
            }

            if (userCoinDetailRespModel != null) {
                SharedPrefHelper.getInstance().saveObjectUserCoin(PrefConstant.USER_COIN, userCoinDetailRespModel)
            }
        }

    }

    fun openQueueFragment() {
        if (Utils?.getCurrentFragment(this@BaseActivity) != null && (currentPlayerType == CONTENT_MUSIC || currentPlayerType == CONTENT_PODCAST)) {
            if (!Utils?.getCurrentFragment(this@BaseActivity)?.javaClass?.simpleName.equals(
                    QueueFragment().javaClass.simpleName, true
                )
            ) {
                setLog(
                    "openQueue",
                    "BaseActivity-openQueueFragment-true-getCurrentFragment=${
                        Utils?.getCurrentFragment(this@BaseActivity)?.javaClass?.simpleName
                    }"
                )
                addFragment(
                    R.id.fl_container,
                    Utils?.getCurrentFragment(this@BaseActivity),
                    QueueFragment(),
                    false
                )
            } else {
                setLog(
                    "openQueue",
                    "BaseActivity-openQueueFragment-false-getCurrentFragment=${
                        Utils?.getCurrentFragment(this@BaseActivity)?.javaClass?.simpleName
                    }"
                )
                supportFragmentManager.beginTransaction().remove(QueueFragment()).commit()
                supportFragmentManager.popBackStack()
                addFragment(
                    R.id.fl_container,
                    Utils?.getCurrentFragment(this@BaseActivity),
                    QueueFragment(),
                    false
                )
            }
            toggleSheetBehavior()
        } else {
            setLog(TAG, "setBottomSheet: Current Fragent is null")
            setLog("openQueue", "BaseActivity-openQueueFragment-false-Current Fragent is null")
        }
    }

    fun downloadAudioTrack() {
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "BaseActivity-downloadAudioTrack-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline) {
            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
            val dq = DownloadQueue()
            val playingIndex = nowPlayingCurrentIndex()
            if (songDataList != null && songDataList?.size!! > 0 && songDataList?.size!! > playingIndex) {

                dq.contentId = songDataList?.get(playingIndex)?.id.toString()
                if (!TextUtils.isEmpty(songDataList?.get(playingIndex)?.parentId)) {
                    dq.parentId = songDataList?.get(playingIndex)?.parentId
                }
                if (!TextUtils.isEmpty(songDataList?.get(playingIndex)?.pName)) {
                    dq.pName = songDataList?.get(playingIndex)?.pName
                }

                if (!TextUtils.isEmpty(songDataList?.get(playingIndex)?.pSubName)) {
                    dq.pSubName = songDataList?.get(playingIndex)?.pSubName
                }

                if (!TextUtils.isEmpty(songDataList?.get(playingIndex)?.pImage)) {
                    dq.pImage = songDataList?.get(playingIndex)?.pImage
                }

                if (!TextUtils.isEmpty(songDataList?.get(playingIndex)?.favCount)) {
                    dq.f_fav_count = songDataList.get(playingIndex).favCount
                }


                dq.pType = songDataList?.get(playingIndex)?.pType!!
                dq.contentType = songDataList?.get(playingIndex)?.contentType!!

                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                    ?.findByContentId(songDataList?.get(playingIndex)?.id!!.toString())
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                    ?.findByContentId(songDataList?.get(playingIndex)?.id!!.toString())
                if ((downloadQueue == null || !downloadQueue?.contentId.equals(
                        songDataList?.get(
                            playingIndex
                        )?.id.toString()
                    ))
                    && (downloadedAudio == null || !downloadedAudio?.contentId.equals(
                        songDataList?.get(
                            playingIndex
                        )?.id.toString()
                    ))
                ) {
                    downloadQueueList.add(dq)
                }
                setLog("DownloadContent", "BaseActivity-downloadAudioTrack")
                addOrUpdateDownloadMusicQueue(downloadQueueList, null, null, true, true)
            }
        }
    }

    interface OnSwipablePlayerListener {
        fun onPlayerProgressChange(currentPosition: Long?, duration: Long?, totDur: String) {}
        fun onDownloadContentStateChange(status: Int) {}
        fun onFavoritedContentStateChange(isFavorite: Boolean) {}
        fun onAudioAdsStarted(adsDuration: Long) {}
        fun onAudioAdsCompleted() {}
        fun loadIntestitialAds() {}
    }

    var onSwipablePlayerListener: OnSwipablePlayerListener? = null
    var onMusicPlayerThreeDotMenuListener: OnSwipablePlayerListener? = null
    fun setPlayerProgressChangeEventCallBack(onPlayerProgressChange: OnSwipablePlayerListener) {
        setLog(
            "SwipablePlayerFragment",
            "playerProgressChangeEventCallBack-BaseActivity-setPlayerProgressChangeEventCallBack()"
        )
        this.onSwipablePlayerListener = onPlayerProgressChange

    }

    fun removePlayerProgressChangeEventCallBack() {
        setLog(
            "SwipablePlayerFragment",
            "playerProgressChangeEventCallBack-BaseActivity-removePlayerProgressChangeEventCallBack()"
        )
        onSwipablePlayerListener = null
    }

    fun setMusicPlayerThreeDotMenuEventCallBack(onPlayerProgressChange: OnSwipablePlayerListener) {
        onMusicPlayerThreeDotMenuListener = onPlayerProgressChange
    }

    fun removeMusicPlayerThreeDotMenuEventCallBack() {
        onMusicPlayerThreeDotMenuListener = null
        removeMusicPlayerThreedotMenuListener()
    }

    fun setPlayerSeekProgress(position: Long) {
        audioPlayer?.seekTo(position)
        if(BaseFragment.castPlayer!=null&&BaseFragment.castPlayer?.isCastSessionAvailable==true){
            BaseFragment.castPlayer?.seekTo(position)
        }
    }

    fun openThreeDotPopup() {
        setLog(TAG, "setBottomSheet img_cast_menu_dots called")
        if (!songDataList.isNullOrEmpty() && audioPlayer != null && songDataList?.size!! > nowPlayingCurrentIndex()) {
            setLog(TAG, "openThreeDotPopup:  songDataList?.toString()" + songDataList?.toString())
            setLog(TAG, "openThreeDotPopup:  songDataList?.size" + songDataList?.size)
            val sheet = MusicPlayerThreeDotsBottomSheetFragment.newInstance(
                this,
                songDataList?.get(nowPlayingCurrentIndex())
            )
            sheet.show(supportFragmentManager, "MusicPlayerThreeDotsBottomSheetFragment")
        }
    }

    private fun setForwardDuration() {
        if (audioPlayer != null) {
            val currentPosition = audioPlayer?.currentPosition
            val duration = audioPlayer?.duration
            var remainDuration: Long = 0
            if (duration != null && currentPosition != null) {
                remainDuration = duration - currentPosition
            }
            var fwDuration: Long = 0
            if (remainDuration > Constant.forwardDuration + 3000) {
                fwDuration = Constant.forwardDuration.toLong()
            }

            if (fwDuration > 0) {
                audioPlayer?.seekTo(audioPlayer?.currentPosition!! + fwDuration)
            } else {
                img_fwd_mini?.alpha = 0.5f
            }
        }
    }

    private fun setBackwardDuration() {
        if (audioPlayer != null) {
            val currentPosition = audioPlayer?.currentPosition
            val duration = audioPlayer?.duration
            var remainDuration: Long = 0
            if (currentPosition != null) {
                remainDuration = currentPosition
            }
            var bwDuration: Long = 0
            if (remainDuration > Constant.backwardDuration) {
                bwDuration = Constant.backwardDuration.toLong()
            } else if (remainDuration == Constant.backwardDuration.toLong()) {
                bwDuration = Constant.backwardDuration.toLong() - 1000
            } else if (remainDuration < Constant.backwardDuration.toLong()) {
                bwDuration = remainDuration - 1000
            }

            if (bwDuration > 0) {
                audioPlayer?.seekTo(audioPlayer?.currentPosition!! - bwDuration)
            }
        }
    }

    interface OnTritonAdListener {
        fun onTritonAdLoaded() {}
        fun onTritonAdCompleted() {}
        fun onTritonPlayerStarted(duration: Long?) {}
    }

    var recommendedListModel: PlaylistViewModel? = null
    var recommendedContentList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    var isRecommendedListApiCalled = false
    fun getRecommendedList() {
        setLog(
            "getRecommendedList",
            "BaseActivity-getRecommendedContentList-isDisplayDiscover-$isDisplayDiscover"
        )
        setLog(
            "getRecommendedList",
            "BaseActivity-getRecommendedContentList()-1-songDataList-${BaseActivity.songDataList?.size}"
        )
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "BaseActivity-getRecommendedList-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline(false)) {
//            isRecommendedListApiCalled = true
            isRecommendedListApiCalled = false
            recommendedListModel = ViewModelProvider(
                this
            ).get(PlaylistViewModel::class.java)
            recommendedListModel?.getRecommendedContentList(this, "")?.observe(this
            ) {
                when (it.status) {
                    com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            setLog("getRecommendedList", "getRecommendedList: response called")
                            if (it?.data != null) {
                                recommendedContentList = it.data.data.body.rows
                                setLog(
                                    "getRecommendedList",
                                    "BaseActivity-playNextSong-getRecommendedList-recommendedContentList.size-${recommendedContentList?.size.toString()}"
                                )
                                if (!recommendedContentList.isNullOrEmpty()) {
                                    val songList: ArrayList<Track> = arrayListOf()
                                    for (item in recommendedContentList.iterator()) {
                                        var playerImage = ""
                                        if (!item.data.playble_image.isNullOrEmpty()) {
                                            playerImage = item.data.playble_image
                                        } else {
                                            playerImage = item.data.image
                                        }
                                        val track = CommonUtils.setPlayerSongList(
                                            item.data.id,
                                            item.data.title,
                                            item.data.subtitle,
                                            "",
                                            "",
                                            "",
                                            item.data.type,
                                            playerImage,
                                            "Recommended",
                                            it.data.data.head.data.id,
                                            it.data.data.head.data.title,
                                            it.data.data.head.data.subtitle,
                                            it.data.data.head.data.image,
                                            DetailPages.RECOMMENDED_SONG_LIST_PAGE.value,
                                            ContentTypes.AUDIO.value,
                                            item.data.misc.explicit,
                                            item.data.misc.restricted_download,
                                            item.data.misc.attributeCensorRating.toString(),
                                            item.data.misc.movierights.toString()
                                        )
                                        songList.add(track)
                                    }
                                    if (!songList.isNullOrEmpty()) {
                                        val intent = Intent(
                                            this@BaseActivity,
                                            AudioPlayerService::class.java
                                        )
                                        intent.apply {
                                            action =
                                                AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                                            putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                                            putExtra(Constant.TRACKS_LIST, ArrayList(songList))
                                            putExtra(Constant.showAddToQueueToast, false)
                                        }
                                        applicationContext.let {
                                            Util.startForegroundService(it, intent)
                                        }
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            val i = Intent(Constant.AUDIO_PLAYER_EVENT)
                                            i.putExtra(Constant.isPlayerContentEnd, true)
                                            i.putExtra(
                                                "EVENT",
                                                Constant.AUDIO_PLAYER_END_RESULT_CODE
                                            )
                                            LocalBroadcastManager.getInstance(this@BaseActivity)
                                                .sendBroadcast(i)
                                        }, 1000)
                                        setLog(
                                            "getRecommendedList",
                                            "getRecommendedList: AUDIO_PLAYER_END_RESULT_CODE called"
                                        )
                                    }
                                }
                            }
                            isRecommendedListApiCalled = false
                        }

                    }

                    com.hungama.music.data.webservice.utils.Status.LOADING -> {

                    }

                    com.hungama.music.data.webservice.utils.Status.ERROR -> {
                        isRecommendedListApiCalled = false
                    }
                }
            }
        }
    }
    var isGoldProgressBar = false
    private fun setTabPlayerProgress(currentPosition: Long?, duration: Long?) {
        if (pbTabPlayer != null) {
            setLog(
                "songsPlayedCurrentDuration2",
                "lastSongPlayDuration-$lastSongPlayDuration currentPosition-$currentPosition duration-$duration"
            )
            if (getIsGoldUser()) {
                if (!isGoldProgressBar){
                    isGoldProgressBar = true
                    pbTabPlayer?.progressDrawable =
                        ContextCompat.getDrawable(this@BaseActivity, R.drawable.circular_progress_bar_gold_user)
                }
            } else {
                if (isGoldProgressBar){
                    isGoldProgressBar = false
                    pbTabPlayer?.progressDrawable =
                        ContextCompat.getDrawable(this@BaseActivity, R.drawable.circular_progress_bar_free_user)
                }
            }
            if (currentPlayerType != CONTENT_MUSIC_VIDEO) {
                if (currentPosition != null && duration != null) {
                    try {
                        setLog(
                            "songsPlayedCurrentDuration2",
                            "currentPosition-$currentPosition duration-$duration"
                        )
                        if (pbTabPlayer?.max != duration.toInt()){
                            pbTabPlayer?.max = duration.toInt()
                        }
                        pbTabPlayer?.progress = currentPosition.toInt()
                    } catch (e: Exception) {
                        setLog(
                            "songsPlayedCurrentDuration2",
                            "Exception-${e.message}"
                        )
                    }

                } else {
                    pbTabPlayer?.max = 100
                    pbTabPlayer?.progress = 0
                }
            } else {
                pbTabPlayer?.max = 100
                pbTabPlayer?.progress = 0
            }
        }

    }


    fun callUserStreamUpdate(
        streamPosition: Int,
        track: Track? = null,
        streamedContentIndex: Int = -1,
        isFullSongPlayed: Boolean = false
    ) {

        CoroutineScope(Dispatchers.Main).launch {
            try {
                var totalDuration: Long = 0
                var playedDuration: Long = 0
                var contentId = ""
                var playerType = ""
                var streamIndex = 0

                var eventModel = EventModel()

                if (track == null) {
                    eventModel = HungamaMusicApp.getInstance().getEventData("" + trackData?.id)
                    /*setLog(
                        "callUserStreamUpdate1",
                        "callUserStreamUpdate-streamPosition:${streamPosition}  lastSongPlayDuration:${lastSongPlayDuration}"
                    )*/
                    when (streamPosition) {
                        STREAM_POSITION_PREVIOUS -> {
                            /*setLog(
                                "callUserStreamUpdate1",
                                "callUserStreamUpdate-STREAM_POSITION_PREVIOUS title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} mediaId:${audioPlayer?.currentMediaItem?.mediaId}"
                            )*/
                            if (audioPlayer?.hasPrevious()!!) {
                                /*setLog(
                                    "callUserStreamUpdate1",
                                    "callUserStreamUpdate-STREAM_POSITION_PREVIOUS-2"
                                )*/
                                if (musicViewModel != null && songDataList != null && audioPlayer != null && songDataList?.size!! > audioPlayer?.previousWindowIndex!!) {
                                    /*setLog(
                                        "callUserStreamUpdate1",
                                        "callUserStreamUpdate-STREAM_POSITION_PREVIOUS-3"
                                    )*/
                                    streamIndex = audioPlayer?.previousWindowIndex!!
                                    streamIndexPrent = streamIndex
                                    if (audioPlayer?.currentTimeline?.windowCount!! > streamIndex) {
                                        totalDuration = audioPlayer?.currentTimeline?.getWindow(
                                            streamIndex,
                                            Timeline.Window()
                                        )?.durationMs!!;
                                    }

                                    if (totalDuration < 0) {
                                        totalDuration = audioPlayer?.contentDuration!!
                                    }

                                    if (totalDuration < 0 && !TextUtils.isEmpty(eventModel?.duration)) {
                                        totalDuration = eventModel.duration.toLong()
                                    }

                                    playedDuration = audioPlayer?.currentPosition!!
                                    contentId = songDataList?.get(streamIndex)?.id.toString()
                                    playerType = songDataList?.get(streamIndex)?.playerType.toString()

                                    if (playedDuration < 0 && !TextUtils.isEmpty(eventModel?.duration_fg)) {
                                        playedDuration = eventModel.duration_fg.toLong()
                                    }

                                    /*setLog(
                                        "callUserStreamUpdate1",
                                        "callUserStreamUpdate-STREAM_POSITION_PREVIOUS-2 title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} playedDuration:${playedDuration} totalDuration:${totalDuration} contentDuration:${audioPlayer?.contentDuration} currentPosition: ${audioPlayer?.currentPosition}"
                                    )*/
                                }
                            }
                        }
                        STREAM_POSITION_CURRENT -> {
                            /*setLog(
                                "callUserStreamUpdate1",
                                "callUserStreamUpdate-STREAM_POSITION_CURRENT title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} mediaId:${audioPlayer?.currentMediaItem?.mediaId}"
                            )*/
                            if (musicViewModel != null && songDataList != null && audioPlayer != null && songDataList?.size!! > audioPlayer?.currentWindowIndex!!) {


                                streamIndex = audioPlayer?.currentWindowIndex!!
                                streamIndexPrent = streamIndex
                                if (audioPlayer?.currentTimeline?.windowCount!! > streamIndex) {
                                    totalDuration = audioPlayer?.currentTimeline?.getWindow(
                                        streamIndex,
                                        Timeline.Window()
                                    )?.durationMs!!
                                }

                                if (totalDuration < 0) {
                                    totalDuration = audioPlayer?.contentDuration!!
                                }

                                if (totalDuration < 0 && !TextUtils.isEmpty(eventModel?.duration)) {
                                    totalDuration = eventModel.duration.toLong()
                                }

                                playedDuration = audioPlayer?.currentPosition!!
                                contentId = songDataList?.get(streamIndex)?.id.toString()
                                playerType = songDataList?.get(streamIndex)?.playerType.toString()

                                if (playedDuration < 0 && !TextUtils.isEmpty(eventModel?.duration_fg)) {
                                    playedDuration = eventModel.duration_fg.toLong()
                                }

                                /*setLog(
                                    "callUserStreamUpdate1",
                                    "callUserStreamUpdate-STREAM_POSITION_CURRENT-2 title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} playedDuration:${playedDuration} totalDuration:${totalDuration} contentDuration:${audioPlayer?.contentDuration} currentPosition: ${audioPlayer?.currentPosition}"
                                )*/
                            }
                        }
                        STREAM_POSITION_NEXT -> {
                            /*setLog(
                                "callUserStreamUpdate1",
                                "callUserStreamUpdate-STREAM_POSITION_NEXT title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} mediaId:${audioPlayer?.currentMediaItem?.mediaId}"
                            )*/
                            if (audioPlayer?.hasNext()!!) {
                                /*setLog(
                                    "callUserStreamUpdate1",
                                    "callUserStreamUpdate-STREAM_POSITION_NEXT-2"
                                )*/
                                if (musicViewModel != null && songDataList != null && audioPlayer != null && songDataList?.size!! > audioPlayer?.nextWindowIndex!!) {
                                    /*setLog(
                                        "callUserStreamUpdate1",
                                        "callUserStreamUpdate-STREAM_POSITION_NEXT-3"
                                    )*/
                                    streamIndex = audioPlayer?.nextWindowIndex!!
                                    streamIndexPrent = streamIndex
                                    if (audioPlayer?.currentTimeline?.windowCount!! > streamIndex) {
                                        totalDuration = audioPlayer?.currentTimeline?.getWindow(
                                            streamIndex,
                                            Timeline.Window()
                                        )?.durationMs!!;
                                    }

                                    if (totalDuration < 0) {
                                        totalDuration = audioPlayer?.contentDuration!!
                                    }

                                    if (totalDuration < 0 && !TextUtils.isEmpty(eventModel?.duration)) {
                                        totalDuration = eventModel.duration.toLong()
                                    }

                                    playedDuration = audioPlayer?.currentPosition!!
                                    contentId = songDataList?.get(streamIndex)?.id.toString()
                                    playerType = songDataList?.get(streamIndex)?.playerType.toString()

                                    /*setLog(
                                        "callUserStreamUpdate1",
                                        "callUserStreamUpdate-STREAM_POSITION_NEXT-2 title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} playedDuration:${playedDuration} totalDuration:${totalDuration} contentDuration:${audioPlayer?.contentDuration} currentPosition: ${audioPlayer?.currentPosition}"
                                    )*/
                                }
                            }
                        }

                    }
                }
                else {
                    try {
                        eventModel = HungamaMusicApp.getInstance().getEventData("" + track?.id)
                        /*setLog(
                            "callUserStreamUpdate1",
                            "callUserStreamUpdate-streamPosition==-:${streamPosition}  title:${track?.title} trackData.title:${trackData.title} eventModel:${eventModel.songName} lastSongPlayDuration:${lastSongPlayDuration}"
                        )
                        setLog(
                            "callUserStreamUpdate1",
                            "callUserStreamUpdate-STREAM_POSITION_CURRENT-from service title:${track?.title}"
                        )*/
                        if (musicViewModel != null && audioPlayer != null && streamedContentIndex > -1) {


                            streamIndex = streamedContentIndex
                            streamIndexPrent = streamIndex
                            if (audioPlayer?.duration != null) {
                                totalDuration = audioPlayer?.duration!!
                            }
                            //setLog("callUserStreamUpdate1", "totalDuration-1" + totalDuration.toString())
                            if (totalDuration < 0 && !TextUtils.isEmpty(eventModel?.duration)) {
                                totalDuration = eventModel.duration.toLong()
                            }

                            if (audioPlayer?.currentPosition != null) {
                                playedDuration = audioPlayer?.currentPosition!!
                            }

                            if (isFullSongPlayed && !TextUtils.isEmpty(eventModel?.duration)) {
                                playedDuration = eventModel.duration.toLong() * 1000
                                totalDuration = eventModel.duration.toLong() * 1000
                            }

                            //setLog("callUserStreamUpdate1", "playedDuration-1" + playedDuration.toString())
                            contentId = track?.id.toString()
                            playerType = track?.playerType.toString()

                            /*if (playedDuration < 0 && !TextUtils.isEmpty(eventModel?.duration_fg)) {
                                playedDuration = eventModel?.duration_fg?.toLong()
                            }*/

                            /*setLog(
                                "callUserStreamUpdate1",
                                "callUserStreamUpdate-STREAM_POSITION_CURRENT-2-from service title:${track?.title} playedDuration:${playedDuration} totalDuration:${totalDuration} contentDuration:${audioPlayer?.contentDuration} currentPosition: ${audioPlayer?.currentPosition}"
                            )*/
                        }
                    }catch (e:Exception){

                    }
                }

                try {
                    if (totalDuration < 0) {
                        totalDuration = 0
                    }
                    if (playedDuration < 0) {
                        playedDuration = 0
                    }
                    /*if (!songDataList.isNullOrEmpty() && songDataList?.size!! > streamIndex) {
                        setLog(
                            "callUserStreamUpdate1",
                            "title" + songDataList?.get(streamIndex)?.title.toString()
                        )
                    }
                    setLog(
                        "callUserStreamUpdate1",
                        "audioPlayer title:${audioPlayer?.currentMediaItem?.mediaMetadata?.title} contentDuration:${audioPlayer?.contentDuration} currentPosition:${audioPlayer?.currentPosition}"
                    )
                    setLog("callUserStreamUpdate1", "streamPosition" + streamPosition.toString())
                    setLog("callUserStreamUpdate1", "totalDuration" + totalDuration.toString())
                    setLog("callUserStreamUpdate1", "playedDuration" + playedDuration.toString())
                    setLog("callUserStreamUpdate1", "contentId" + contentId)
                    if (!songDataList.isNullOrEmpty() && songDataList?.size!! > streamIndex) {
                        setLog(
                            "callUserStreamUpdate1",
                            "callUserStreamUpdate1 audioPlayer song id: " + songDataList?.get(streamIndex)?.id
                        )
                        setLog(
                            "callUserStreamUpdate1",
                            "callUserStreamUpdate1 audioPlayer song name: " + songDataList?.get(streamIndex)?.title
                        )
                    }

                    setLog("callUserStreamUpdate1", "playerType" + playerType)*/
                }
                catch (e: java.lang.Exception) {
                    setLog("callUserStreamUpdate1", "Error-1" + e.message)
                    //e.printStackTrace()
                }

                try {
                    if (!TextUtils.isEmpty(contentId) && !TextUtils.isEmpty(playerType)) {
                        try {
                            val param: java.util.HashMap<String, String> = java.util.HashMap()

                            if (totalDuration > 0) {
                                param.put(
                                    "totalDuration",
                                    "" + TimeUnit.MILLISECONDS.toSeconds(totalDuration)
                                )
                            } else {
                                param.put("totalDuration", "0")
                            }

                            if (playedDuration!! > 0) {
                                param.put("playDuration", "" + TimeUnit.MILLISECONDS.toSeconds(playedDuration))
                            } else {
                                param.put("playDuration", "0")
                            }

                            param.put(
                                "contentId",
                                "" + contentId
                            )
                            param.put(
                                "typeId",
                                "" + playerType
                            )

                            param.put(
                                "userId",SharedPrefHelper.getInstance().getUserId()!!)

                            //setLog("updateUserAudioStream", "callUserStreamUpdate1 param:" + param)
                            if (ConnectionUtil(this@BaseActivity).isOnline(false)){
                                musicViewModel?.updateUserAudioStream(
                                    this@BaseActivity,
                                    param
                                )
                            }



                            /*setLog(TAG, "callUserStreamUpdate audioPlayer isPlaying: " + audioPlayer?.isPlaying)
                            setLog(TAG, "callUserStreamUpdate: audioPlayer state:${audioPlayer?.playbackState}")
                            setLog(TAG, "callUserStreamUpdate: audioPlayer total duration:${totalDuration}")
                            setLog(TAG, "callUserStreamUpdate: audioPlayer currentPosition:${playedDuration}")
                            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > streamIndex) {
                                setLog(
                                    TAG,
                                    "callUserStreamUpdate audioPlayer song name: " + songDataList?.get(
                                        streamIndex
                                    )?.title
                                )

                                setLog(
                                    TAG,
                                    "callUserStreamUpdate: event called:${songDataList?.get(streamIndex)}"
                                )

                            }*/

                            if (track == null) {
                                if (!songDataList.isNullOrEmpty() && songDataList?.size!! > streamIndex) {
                                    /*setLog(
                                        "callUserStreamUpdate1",
                                        "callUserStreamUpdate: event called:stream 1"
                                    )*/
                                    callStreamEventAnalytics(songDataList?.get(streamIndex)!!, EventType.STREAM)
                                }
                            } else {
                                /*setLog(
                                    "callUserStreamUpdate1",
                                    "callUserStreamUpdate: event called:stream 2"
                                )*/
                                setLog("EventCheck", "Stream" + "1188")
                                callStreamEventAnalytics(track, EventType.STREAM, isFullSongPlayed)
                            }


                            if (SharedPrefHelper.getInstance().get(
                                    PrefConstant.FIRST_STREAM,
                                    true
                                ) && !TextUtils.isEmpty(contentId) && playedDuration > 10
                            ) {
                                /* Track Events in real time */
                                val eventValue: MutableMap<String, Any> = HashMap()
                                eventValue.put(AFInAppEventParameterName.CONTENT_ID, contentId)
                                AppsFlyerLib.getInstance().logEvent(
                                    HungamaMusicApp.getInstance(),
                                    EventConstant.AF_FIRST_STREAM,
                                    eventValue
                                )
                                //setLog(TAG, "AppsFlyerLib eventName:first_stream eventProperties:$eventValue")
                                SharedPrefHelper.getInstance().save(PrefConstant.FIRST_STREAM, false)

                            }


                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val model = BodyRowsItemsItem()
                                    if (track == null) {
                                        if (!songDataList.isNullOrEmpty() && songDataList.size > streamIndex){
                                            model.itemId = songDataList.get(streamIndex).id.toInt()
                                            model.type = "" + songDataList.get(streamIndex).playerType
                                            model.data?.title = songDataList.get(streamIndex).title
                                            model.data?.subTitle = songDataList.get(streamIndex).subTitle
                                            model.data?.image = songDataList.get(streamIndex).image!!
                                            model.data?.type = "" + songDataList.get(streamIndex).playerType
                                        }
                                    } else {
                                        model.itemId = track.id.toInt()
                                        model.type = "" + track.playerType
                                        model.data?.title = track.title
                                        model.data?.subTitle =
                                            track.subTitle
                                        model.data?.image = track.image!!
                                        model.data?.type = "" + track.playerType
                                    }

                                    model.itype = 7
                                    model.sr_no = 0
                                    model.data = BodyDataItem()
                                    model.data?.duration = "" + TimeUnit.MILLISECONDS.toSeconds(totalDuration)
                                    model.data?.durationPlay =
                                        TimeUnit.MILLISECONDS.toSeconds(playedDuration)
                                    model.data?.itype = 7

                                    model.addedDateTime = DateUtils.getCurrentDateTime()


                                    //setLog(TAG, "callUserStreamUpdate: " + model)
                                    AppDatabase.getInstance()?.recentlyPlayDao()?.insertOrReplace(model)
                                }catch (e:Exception){

                                }
                            }



                        } catch (e: Exception) {
                            setLog("callUserStreamUpdate1", "Error-2" + e.message)
                            e.printStackTrace()
                        }
                    }
                }catch (e:Exception){

                }
            }catch (e:Exception){

            }
        }


    }

    private fun loadInterstitialAds() {
        setLog("PlayerAds:-", "loadInterstitialAds=> loadInterstitialAds ====>")
        if (onSwipablePlayerListener != null) {
            setLog(
                "PlayerAds:-",
                "loadInterstitialAds=> loadInterstitialAds ====> onSwipablePlayerListener"
            )
            onSwipablePlayerListener?.loadIntestitialAds()
        } else {
            setLog("PlayerAds:-", "loadInterstitialAds=> onSwipablePlayerListener ====> null")
            if (audioPlayer != null && audioPlayer?.isPlaying == false) {
                playPlayer()
            }
        }
    }

    var durationHandler: Handler? = null
    private fun startDurationCallback() {
        if (durationHandler != null) {
            removeDurationCallback()
            durationHandler?.post(updateDurationTask)
        } else {
            durationHandler = Handler(Looper.getMainLooper())
            durationHandler?.post(updateDurationTask)
        }
    }

    fun removeDurationCallback() {
        if (durationHandler != null) {
            durationHandler?.removeCallbacks(updateDurationTask)
        }
    }

    private val updateDurationTask = object : Runnable {
        override fun run() {
            updateDuration()
            durationHandler?.postDelayed(this, 1000)//1 seconds
        }
    }

    private fun updateDuration() {
        if (audioPlayer != null && audioPlayer?.isPlaying == true) {

            if (!CommonUtils.isUserHasGoldSubscription() && CommonUtils.getSongDurationConfig().enable_minutes_quota) {
                if (totalPlayedSongDuration < 0) {
                    totalPlayedSongDuration = 0
                }
                val localCount = totalGetted - (totalPlayedSongDuration/1000/60)
                setLog("kagkfa", trackData.movierights.toString() + " " + trackData.contentType + " $localCount")
                if (!trackData.movierights.contains("AMOD") && trackData.contentType == ContentTypes.AUDIO.value && localCount>0) {
                    totalPlayedSongDuration += 1000
                }
            }

            if (HungamaMusicApp.getInstance().activityVisible) {
                addFGTime()
                setLog("countFgTime", "5  " + totalPlayedSongDuration.toString())
            }
            else {
                addBGTime()
            }
            if (!CommonUtils.isUserHasGoldSubscription() && CommonUtils.getSongDurationConfig().enable_minutes_quota) {

                val sondDbData = AppDatabase.getInstance()?.songDuration()?.getSongDuration()
                if (sondDbData?.Is_first_stream_started == 0 && sondDbData.stream_max_min_allowed!! > 0) {
                    val currentDate: Date = Calendar.getInstance(TimeZone.getDefault()).time
                    val inputFormat = SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSS'Z'")
                    val formattedDate = inputFormat.format(currentDate)
                    sondDbData.Is_first_stream_started = 1
                    sondDbData.first_stream_start_time = inputFormat.parse(formattedDate)

                    AppDatabase.getInstance()?.songDuration()?.updateSongDuration(sondDbData)
                }
            }
            if (!songDataList.isNullOrEmpty() && songDataList.size > nowPlayingCurrentIndex()) {
                val list = durationMap.get(songDataList.get(nowPlayingCurrentIndex()).id.toString())
                setLog("updatedDurationMapList", "list-${list.toString()}")
            }
//            setLog("SongDurationData", " updateDuration $totalPlayedSongDuration")
            if (!CommonUtils.isUserHasGoldSubscription() && !trackData.movierights.contains("AMOD") && trackData.contentType == ContentTypes.AUDIO.value && CommonUtils.getSongDurationConfig().enable_minutes_quota) {
                updateTimer()
            }
        }
    }

    fun updateTimer(){

        localDuration = totalGetted - (totalPlayedSongDuration/1000/60)
        maxMinAllowed = localDuration
        if (isTouch) {
            showAd += 1
        }
        if(localDuration>0){
            isTouch = false
            showAd = 0
        }
        if (player11 != null){
            if (player11?.isPlaying == true)
            {
                player11?.pause()
            }
        }
        if (localDuration <= 0){
            localDuration = 0
            SharedPrefHelper.getInstance().getUserId()
                ?.let { AppDatabase.getInstance()?.songDuration()?.updateUserStreamedMin(0, it) }
            val global_limited_stream_preview_quota = CommonUtils.getSongDurationConfig().global_limited_stream_preview_quota + 1
            setLog("BaseActivityLifec ", showAd.toString() + " " + isTouch + " " + CommonUtils.getSongDurationConfig().enable_minutes_quota)


            if (showAd == global_limited_stream_preview_quota && isTouch){
                setLog("BaseActivityLifec ", "AudioId " + CommonUtils.getNudgeAudioId())
                pausePlayer()
                showAudioAd(true)
                if (HungamaMusicApp.getInstance().activityVisible) {
                    if (isSwipableActive) {
                        if (newPreviewModel.visibility == View.GONE) {
                            songPreviewModel.visibility = View.GONE
                            includeFreeMinute.visibility = View.GONE
                            if (CommonUtils.getNudgeImageUrl().isEmpty()) {
                                songPreviewModel.visibility = View.VISIBLE
                                newPreviewModel.visibility = View.GONE
                            } else {
                                songPreviewModel.visibility = View.GONE
                                newPreviewModel.visibility = View.VISIBLE

                                showjson()
                            }
                        }
                    }
                    else{
                        if (!supportFragmentManager.isDestroyed)
                            subscriptionDialogBottomsheetFragment.show(supportFragmentManager, "subscriptionDialogBottomsheetFragment")
                    }
                    }

            }
            else if(showAd in 1.. global_limited_stream_preview_quota && isTouch) {
                if (player11 != null){
                    if(player11?.isPlaying == true)
                    {
                        player11?.pause()
                    }
                }
                if (isSwipableActive) {
                    if (songPreviewModel.visibility == View.GONE) {
                        includeFreeMinute.visibility = View.GONE
                        songPreviewModel.visibility = View.GONE
                        newPreviewModel.visibility = View.GONE
                    }
                }
            }
            else{
                if (showAd <= 0) {
                    pausePlayer()
                    showAudioAd(false)
                    if (isSwipableActive) {
                        if (includeFreeMinute.visibility == View.GONE) {
                            includeFreeMinute.visibility = View.VISIBLE
                            songPreviewModel.visibility = View.GONE
                            newPreviewModel.visibility = View.GONE
                        }
                    } else {
                        if(HungamaMusicApp.getInstance().activityVisible && !supportFragmentManager.isDestroyed) {
                            subscriptionDialogBottomsheetFragment.show(supportFragmentManager, "subscriptionDialogBottomsheetFragment")
                        }
                    }
                }
            }
        }
            if (BaseActivity.tvSleepTimer != null && localDuration>=0) {
                BaseActivity.tvSleepTimer.text = if (localDuration<10)"0$localDuration" else localDuration.toString()
            }

/*
            var progress = if(maxMinAllowed>0)((localDuration.toDouble().div(maxMinAllowed) * 100)) else 0
            progress = "100".toDouble().minus(progress.toDouble())

        var progress = if(CommonUtils.getSongDurationConfig().global_limited_minutes_quota>0)((localDuration.toDouble().div(CommonUtils.getSongDurationConfig().global_limited_minutes_quota) * 100)) else 0
*/

        var progress = if(songDuration.stream_max_min_allowed!! >0)((maxMinAllowed.toDouble().div(
            songDuration.stream_max_min_allowed!!) * 100)) else 0
        progress = "100".toDouble().minus(progress.toDouble())
        setLog("SongDurationData", "Swipable " + progress.toString() + " " + songDuration.stream_max_min_allowed.toString() + " " + maxMinAllowed.toString())

        if (BaseActivity.pbDuration != null)
            BaseActivity.pbDuration.secondaryProgress = progress.toInt()
    }

    fun showAudioAd(isNewPreviewShow:Boolean){
        if(isNewPreviewShow) {
            if (nundgeAduioAdUrl.isNotEmpty()) {
                mediaUrlAd = nundgeAduioAdUrl
            }
        }
        else {
            if (drawerAduioAdUrl.isNotEmpty()) {
                mediaUrlAd = drawerAduioAdUrl
            }
        }
        if (mediaUrlAd.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(mediaUrlAd)
            player11?.setMediaItem(mediaItem)
            player11?.prepare()
            player11?.play()
        }
    }

    fun getTrackData(playableContentModel: PlayableContentModel):Track
    {
        val track = Track()
        track.url = playableContentModel.data.head.headData.misc.url
        track.onErrorPlayableUrl = playableContentModel.data.body.data.url.apiResponse.toString()
        var drmlicence = ""
        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.drm.token)) {
            drmlicence =
                playableContentModel.data.head.headData.misc.downloadLink.drm.token
        } else if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.downloadLink.mdn.token)) {
            drmlicence =
                playableContentModel.data.head.headData.misc.downloadLink.mdn.token
        }
        track.drmlicence = drmlicence
        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.sl.lyric?.link)) {
            if (!playableContentModel.data.head.headData.misc.sl.lyric?.link?.contains(
                    ".txt"
                )!!
            ) {
                track.songLyricsUrl =
                    playableContentModel.data.head.headData.misc.sl.lyric?.link
            }
        }



        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.playble_image)){
            track.image = playableContentModel.data.head.headData.playble_image
        }else if (track.image?.isNullOrBlank()!!&&!TextUtils.isEmpty(playableContentModel.data.head.headData.image)){
            track.image = playableContentModel.data.head.headData.image
        }

        track.pid = playableContentModel.data.head.headData.misc.pid.toString()
        track.shareUrl = playableContentModel.data.head.headData.misc.share
        track.favCount = playableContentModel.data.head.headData.misc.f_fav_count
        setLog("playbackQuality", "BaseActivity-Before-id-${track.id}-title-${track.title} - key-${track.urlKey} - isGoldUser-${getIsGoldUser()}")
        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.urlKey)){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        setLog("playbackQuality", "BaseActivity-after-id-${track.id}-title-${track.title} - key-${track.urlKey} - isGoldUser-${getIsGoldUser()}")
        return track
    }

    fun isPreCatchContent(songList: ArrayList<Track>?, contentTrack: Track): Boolean {
        var isPreCatchContent = false
        setLog(
            "preCatchContent",
            "BaseActivity-isPreCatchContent()-songList.size-${songList?.size}  nowPlayingCurrentIndex()-${nowPlayingCurrentIndex()}"
        )
        if (!songList.isNullOrEmpty() && songList.size > nowPlayingCurrentIndex()) {
            if (songList.get(nowPlayingCurrentIndex()).id != contentTrack.id) {
                run loop@{
                    songList?.forEachIndexed { index, track ->
                        /*setLog(
                            "preCatchContent",
                            "BaseActivity-isPreCatchContent()-index-$index  songList.track.id-${
                                songList.get(nowPlayingCurrentIndex()).id
                            }  contentTrack.id-${contentTrack.id}  track.id-${track.id}"
                        )*/
                        if (index > nowPlayingCurrentIndex() && track.id == contentTrack.id) {
                            isPreCatchContent = true
                            /*setLog(
                                "preCatchContent",
                                "BaseActivity-isPreCatchContent()-IF-true-Return"
                            )*/
                            return@loop
                        }
                    }
                }
            }
        }
        setLog(
            "preCatchContent",
            "BaseActivity-isPreCatchContent()-isPreCatchContent-$isPreCatchContent"
        )
        return isPreCatchContent
    }

    private fun preCatchApiCall(songDataList: ArrayList<Track>?, index: Int, isPause: Boolean, isForcefullyCallApi:Boolean = false) {
        //preCatch Playable Url logic
        val nextContentIndex = index + 1
        if (!songDataList.isNullOrEmpty() && songDataList.size > nextContentIndex) {
            setLog(
                "preCatchContent",
                "BaseActivity-preCatchApiCall-track.id-${songDataList?.get(nextContentIndex)?.id}  track.url-${
                    songDataList?.get(nextContentIndex)?.url
                }"
            )
            if (TextUtils.isEmpty(songDataList?.get(nextContentIndex)?.url) || isForcefullyCallApi) {
                setUpPlayableContentListViewModel(
                    songDataList.get(nextContentIndex).id,
                    songDataList.get(nextContentIndex)?.playerType?.toInt(),
                    isPause
                )

                setLog(
                    "queue--1",
                    "BaseActivity-preCatchApiCall-setUpPlayableContentListViewModel - called"
                )
            }
            setLog("queue--1", "BaseActivity-preCatchApiCall-callOnQueueItemChanged - called")
            nowPlayingQueue?.callOnQueueItemChanged(songDataList, false)
        }
    }

    private fun updateAudioAdsSongCounts() {
        CoroutineScope(Dispatchers.IO).launch {
            if (BaseActivity.lastSongPlayDuration >= CommonUtils.getAudioAdPreference().minPlayDurationSeconds) {
                setLog(
                    "PlayerAds","BaseActivity-lastSongPlayDuration-"+BaseActivity.lastSongPlayDuration.toString()
                )
                BaseActivity.totalSongsPlayedAfterLastAudioAd += 1
                BaseActivity.lastSongPlayDuration = 0
                setLog(
                    "PlayerAds","BaseActivity-totalSongsPlayedAfterLastAudioAd-"+BaseActivity.totalSongsPlayedAfterLastAudioAd.toString()
                )
            } else {
                setLog(
                    "PlayerAds","BaseActivity-lastSongPlayDuration-"+BaseActivity.lastSongPlayDuration.toString()
                )
            }
        }

    }

    fun closePIPVideoPlayer() {
        val intent = Intent(Constant.VIDEO_PLAYER_PIP_EVENT)
        intent.putExtra("EVENT", Constant.VIDEO_PIP_ACTIVITY_RESULT_CODE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun callRecommendedApiOnEndOfSong() {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(
                "getRecommendedList",
                "BaseActivity-callRecommendedApiOnEndOfSong-isRecommendedListApiCalled-$isRecommendedListApiCalled"
            )
            if (!songDataList.isNullOrEmpty() && !isRecommendedListApiCalled) {
                val listSize = songDataList?.size
                if (listSize != null) {
                    setLog(
                        "getRecommendedList",
                        "BaseActivity-callRecommendedApiOnEndOfSong-listSize-$listSize  nowPlayingCurrentIndex-${nowPlayingCurrentIndex()}"
                    )
                    val finalPosition = listSize - nowPlayingCurrentIndex()
                    setLog(
                        "getRecommendedList",
                        "BaseActivity-callRecommendedApiOnEndOfSong-finalPosition-$finalPosition"
                    )
                    if (finalPosition in 0..2) {
                        setLog(
                            "getRecommendedList",
                            "BaseActivity-callRecommendedApiOnEndOfSong-getRecommendedList-Called"
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
                            intent.putExtra("EVENT", Constant.AUDIO_PLAYER_END_RESULT_CODE)
                            LocalBroadcastManager.getInstance(this@BaseActivity).sendBroadcast(intent)
                            WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY = true
                            getRecommendedList()
                        }

                    }
                }
            }
        }


    }

    fun setPauseMusicPlayerOnVideoPlay() {
        setLog(
            "setPauseMusicPlayerOnVideoPlay",
            "BaseActivity-setPauseMusicPlayerOnVideoPlay-currentPlayerType-$currentPlayerType"
        )
        if (currentPlayerType != CONTENT_MUSIC_VIDEO) {
            val status = getAudioPlayerPlayingStatus()
            if (status == Constant.pause) {
                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
            } else {
                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
            }
        }
        if (audioPlayer != null && audioPlayer?.currentPosition != null && !songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()){
            setLog(
                "setPauseMusicPlayerOnVideoPlay",
                "BaseActivity-setPauseMusicPlayerOnVideoPlay-id-${songDataList?.get(nowPlayingCurrentIndex())?.id} - audioPlayer?.currentPosition-${audioPlayer?.currentPosition!!}"
            )
            HungamaMusicApp.getInstance().userStreamList.put(songDataList?.get(nowPlayingCurrentIndex())?.id.toString(),audioPlayer?.currentPosition!!)
        }
        pausePlayer()
    }

    private val mNotificationMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver", "mMessageReceiver-Notification-$intent")
            if (intent.hasExtra("EVENT")){
                if (intent.getIntExtra("EVENT", 0) == Constant.NOTIFICATION_RESULT_CODE) {
                    if (intent.hasExtra("action")) {
                        val action = intent.getStringExtra("action")
                        //setLog("onCustomAction", "BaseActivity-action-$action")
                        if (action.equals(Constant.CUSTOM_ACTION_PLAY)) {
                            playPlayer()
                        } else if (action.equals(Constant.CUSTOM_ACTION_PAUSE)) {
                            pausePlayer()
                        } else if (action.equals(Constant.CUSTOM_ACTION_PREVIOUS)) {
                            playPreviousSong()
                        } else if (action.equals(Constant.CUSTOM_ACTION_NEXT)) {
                            playNextSong(false)
                        }
                    }
                    /*if (intent.hasExtra("isNext")) {
                        if (intent.getBooleanExtra("isNext", false)) {
                            //Next song play
                            setLog("OnNotifActio", "Next song")
                            playNextSong(false)
                        } else {
                            //Previous song play
                            setLog("OnNotifActio", "Previous song")
                            playPreviousSong()
                        }
                    }*/
                }else if(intent.getIntExtra("EVENT", 0) == Constant.STORY_RESULT_CODE){
                    if (intent.hasExtra(Constant.isVideoStory)) {
                        if (intent.getBooleanExtra(Constant.isVideoStory, false)) {
                            isVideoStoryPlaying = true
                            val status = getAudioPlayerPlayingStatus()
                            if (status == Constant.pause){
                                //SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                            }else{
                                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                            }
                            pausePlayer()
                            audioPlayerService?.hidePlayerNotification()
                        }else{
                            isVideoStoryPlaying = false
                            val isPause = SharedPrefHelper.getInstance().getLastAudioContentPlayingStatus()
                            val status = getAudioPlayerPlayingStatus()
                            if (status == Constant.pause && !isPause){
                                playPlayer()
                            }
                            audioPlayerService?.showPlayerNotification()
                        }
                    }

                }else if(intent.getIntExtra("EVENT", 0) == Constant.VIDEO_PIP_ACTIVITY_RESULT_CODE){
                    if (intent.hasExtra(Constant.isPIPVideoPlayerVisible)) {
                        setLog("VideoPlayerPipMode", "isPIPVideoPlayerVisible-${intent.getBooleanExtra(Constant.isPIPVideoPlayerVisible, false)}")
                        if (intent.getBooleanExtra(Constant.isPIPVideoPlayerVisible, false)) {
                            hideMiniPlayer()
                        }else{
                            /*val isPause = SharedPrefHelper.getInstance().getLastAudioContentPlayingStatus()
                            val status = getAudioPlayerPlayingStatus()
                            if (status == Constant.pause && !isPause){
                                playPlayer()
                            }
                            showMiniPlayer()*/
                            audioPlayerService?.hidePlayerNotification()
                        }
                    }
                }else if(intent.getIntExtra("EVENT", 0) == Constant.AUDIO_QUALITY_CHANGE_RESULT_CODE){
                    CoroutineScope(Dispatchers.IO).launch {
                        //Update next song url by updated stream quality
                        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > nowPlayingCurrentIndex()+1){
                            songDataList?.get(nowPlayingCurrentIndex()+1)?.url = ""
                            delay(1500)
                            preCatchApiCall(songDataList, nowPlayingCurrentIndex(), false, true)
                        }
                    }
                }
            }
        }
    }

    fun updateFavoriteInCurrentPlayerList(id: String, isLike: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!songDataList.isNullOrEmpty()) {
                    songDataList?.forEachIndexed { _, track ->
                        if (track.id.toString() == id) {
                            track.isLiked = isLike
                        }
                    }
                }
            } catch (e: Exception) {
                setLog("updateFavoriteInCurrentPlayerList","e $e" )
            }
        }

    }

    val homeWatcherListener:OnHomePressedListener = object : OnHomePressedListener{
        override fun onHomePressed() {
            setLog("homeWatcherListener", "onHomePressed")
            val intent = Intent(Constant.MUSIC_VIDEO_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.MUSIC_VIDEO_RESULT_CODE)
            intent.putExtra("key", KeyEvent.KEYCODE_HOME)
            LocalBroadcastManager.getInstance(this@BaseActivity).sendBroadcast(intent)
        }

        override fun onHomeLongPressed() {
            setLog("homeWatcherListener", "onHomeLongPressed")
        }
    }

    fun getPlayerRepeatMode():Int {
        try {
            if (audioPlayer != null){
                return audioPlayer?.repeatMode!!
            }
        }catch (e:Exception){

        }
        return Player.REPEAT_MODE_OFF
    }

    override fun onRepeatModeChanged(repeatMode: Int) {

        when (repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                musicPlayerThreedotMenu?.repeatModeChanged(Player.REPEAT_MODE_ONE)
                /*val messageModel = MessageModel(
                    getString(R.string.auto_repeat_current_song),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(this@BaseActivity, messageModel)*/
            }
            Player.REPEAT_MODE_ALL -> {
                musicPlayerThreedotMenu?.repeatModeChanged(Player.REPEAT_MODE_ALL)
                /*val messageModel = MessageModel(
                    getString(R.string.repeat_all_songs),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(this@BaseActivity, messageModel)*/
            }
            Player.REPEAT_MODE_OFF -> {
                musicPlayerThreedotMenu?.repeatModeChanged(Player.REPEAT_MODE_OFF)
            }
        }
    }

    fun changeRepeateModes(repeatMode: Int) {
        when (repeatMode) {
            /*Player.REPEAT_MODE_ONE -> {
                audioPlayer?.repeatMode = Player.REPEAT_MODE_ALL
            }
            Player.REPEAT_MODE_ALL -> {
                audioPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            }
            Player.REPEAT_MODE_OFF -> {
                audioPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            }*/

            Player.REPEAT_MODE_ONE -> {
                audioPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            }
            Player.REPEAT_MODE_ALL -> {
                audioPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            }
            Player.REPEAT_MODE_OFF -> {
                audioPlayer?.repeatMode = Player.REPEAT_MODE_ALL
            }
        }
    }
    var musicPlayerThreedotMenu:OnMusicPlayerThreedotMenuListener? = null
    fun setMusicPlayerThreedotMenuListener(menuListener: OnMusicPlayerThreedotMenuListener){
        this.musicPlayerThreedotMenu = menuListener
    }

    fun removeMusicPlayerThreedotMenuListener(){
        musicPlayerThreedotMenu = null
    }

    interface OnMusicPlayerThreedotMenuListener{
        fun repeatModeChanged(repeatMode: Int)
        fun shuffleModeChanged(shuffleMode: Boolean)
    }

    fun changeShuffleMode(){
        nowPlayingViewModel.changeShuffleMode(!nowPlayingQueue?.shuffleEnabled!!)
    }
    fun getShuffleModeStatus(): Boolean {
        return nowPlayingQueue?.shuffleEnabled!!
    }

    override fun updateShuffleMode(shuffleMode: Boolean) {
        audioPlayerService?.updateShuffleMode(shuffleMode)
        nowPlayingQueue?.changeShuffle(object : NowPlayingQueue.OnChangeShuffle {
            override fun onShuffleChanged() {
                musicPlayerThreedotMenu?.shuffleModeChanged(shuffleMode)
                val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
                intent.putExtra("EVENT", Constant.AUDIO_PLAYER_SHUFFLE_MODE_CHANGED_RESULT_CODE)
                LocalBroadcastManager.getInstance(this@BaseActivity).sendBroadcast(intent)
            }
        })
    }


    fun makeCastPlayer(){
        setLog(TAG, "setUpChormeCast called BaseFragment.castPlayer:${BaseFragment.castPlayer} isCastPlayerAudio:${BaseFragment.isCastPlayerAudio}")
//        if(!BaseFragment.isCastPlayerAudio){
//
//            BaseFragment.castPlayer = CastPlayer(CastContext.getSharedInstance(this@BaseActivity))
//            BaseFragment.isCastPlayerAudio=true
//
//            setLog(TAG, "setUpChormeCast called 222 BaseFragment.castPlayer: isCastPlayerAudio:${BaseFragment.isCastPlayerAudio}")
//        }

        if(BaseFragment.castPlayer ==null || !BaseFragment.isCastPlayerAudio){
            BaseFragment.castPlayer = CastContext.getSharedInstance()?.let { CastPlayer(it) }
            BaseFragment.isCastPlayerAudio=true

            setLog(TAG, "setUpChormeCast called 222 BaseFragment.castPlayer: isCastPlayerAudio:${BaseFragment.isCastPlayerAudio}")
        }
    }
    public fun setUpChormeCast() {


        makeCastPlayer()

        BaseFragment.castPlayer?.setSessionAvailabilityListener(object :
            SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                setLog(TAG, "onCastSessionAvailable: ")
                if(BaseFragment.castPlayer!=null){
                    setAudioCurrentPlayer(BaseFragment.castPlayer!!)
                }else{
                    makeCastPlayer()
                    if(BaseFragment.castPlayer!=null){
                        setAudioCurrentPlayer(BaseFragment.castPlayer!!)
                    }

                }

            }

            override fun onCastSessionUnavailable() {
                setLog(TAG, "onCastSessionUnavailable: ")
                setLog(
                    "pipMode",
                    "MusicVideoDetailsFragment-setUpChormeCastpausePlayer-onCastSessionUnavailable-pausePlayer-called"
                )
                audioPlayer?.let {
                    setAudioCurrentPlayer((audioPlayer as ExoPlayer))
                }


            }

        })

    }


    public fun setAudioCurrentPlayer(mCurrentPlayer: Player) {
        try {
            if (!isDestroyed() && songDataList != null && player_view != null) {
                Log.d(
                    TAG,
                    "setCurrentPlayer: currentPlayer:${mCurrentPlayer == (audioPlayer as ExoPlayer)}"
                )

                var playWhenReady = false
                val previousPlayer: Player = this.currentPlayer!!
                // Player state management.
                var playbackPositionMs = C.TIME_UNSET
                if (previousPlayer != null) {
                    // Save state from the previous player.
                    val playbackState = previousPlayer.playbackState
                    if (playbackState != Player.STATE_ENDED) {
                        playWhenReady = previousPlayer.playWhenReady
                        playbackPositionMs = previousPlayer.currentPosition
                    }

                    Log.d(TAG, "setCurrentPlayer: previousPlayer called:")
                }
                this.currentPlayer = mCurrentPlayer
                this.currentPlayer?.addListener(this@BaseActivity)

                if (songDataList != null && nowPlayingCurrentIndex() != null && nowPlayingCurrentIndex() < songDataList?.size!!) {

                    var track = songDataList?.get(nowPlayingCurrentIndex())!!

                    Log.d(TAG, "setCurrentPlayer: track :${track}")
                    track?.let {
                        var mimType = MimeTypes.BASE_TYPE_AUDIO
                        if (track?.url?.contains(".m3u8", true)!!) {
                            mimType = MimeTypes.APPLICATION_M3U8
                        } else if (track?.url?.contains(".mp3", true)!!) {
                            mimType = MimeTypes.BASE_TYPE_AUDIO
                        } else if (track?.url?.contains(".mpd", true)!!) {
                            mimType = MimeTypes.APPLICATION_MPD
                        } else if (track?.url?.contains(".mp4", true)!!) {
                            mimType = MimeTypes.APPLICATION_MP4
                        }

                        val mediaItem1 = MediaItem.Builder()
                            .setUri(Uri.parse(track?.url))
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(track?.title)
                                    .setDisplayTitle(track?.title)
                                    .setSubtitle(track?.subTitle)
                                    .setArtworkUri(Uri.parse(track?.image!!))
                                    //.setMediaUri(Uri.parse(track?.url))
                                    .setDescription(track?.subTitle)
                                    .setAlbumTitle(track?.pName)
                                    .setArtist(track?.artistName)
                                    .build()
                            )
                            .setMediaId(track?.url!!)
                            .setMimeType(mimType)
                            .build()

                        currentPlayer?.setMediaItem(mediaItem1!!, 0L!!)
                        currentPlayer?.playWhenReady = playWhenReady
                        currentPlayer?.prepare()

                        Log.d(TAG, "setCurrentPlayer: mediaItem1${mediaItem1}")
                    }
                } else {
                    Log.d(TAG, "setCurrentPlayer: cast data not found")
                }
            }
        }catch (exp:Exception){
            exp.printStackTrace()
        }

    }

    override fun gamificationOnPointsAdded(eventName: String?, added: Int, total: Int) {
        try {
            updateUserCoins()

            val remoteConfig = Firebase.remoteConfig


            if (added > 0 && EventConstant.gamiFicationActions.contains(eventName?.trim())) {
                sendCoinsEarnEvent(added, eventName)
                setLog(
                    "GM-SDK-APP",
                    "BaseActivity onPointsAdded in list: eventName:${eventName} added:${added}}")

                val gcEventName = GCEvent.getGCEventName(eventName!!)
                if (gcEventName != null && !TextUtils.isEmpty(gcEventName)) {
                    val gcEvent = remoteConfig.getString(gcEventName)
                    val gcEventModel = Gson().fromJson<GCEventModel>(
                        gcEvent,
                        GCEventModel::class.java
                    ) as GCEventModel
                    setLog("GM-SDK-APP", "onPointsAdded: gcEventModel:${gcEventModel?.toString()}")

                    if (gcEventModel.isDisplay!!) {
                        if (gcEventModel?.popupType?.contains("toast") == true) {
                            var msg = ""
                            if (!TextUtils.isEmpty(gcEventModel.popupText)) {
                                msg = gcEventModel.popupText.toString()
                                    .replace("@coin_amount", "" + added)
                            } else {
                                msg = "+ $added"
                            }
                            setLog("GM-SDK-APP", "onPointsAdded: msg:${msg}")
                            val messageModel = MessageModel(msg, MessageType.GAMIFICATION, true)
                            CommonUtils.showToast(this!!, messageModel)
                        } else if (gcEventModel?.popupType?.contains("popup", true) == true) {
                            gcEventModel.addedCoin = added
                            gcEventModel.totalCoin = total
                            CommonUtils.displayPopup(this, gcEventModel)
                        }
                    }
                }else{
                    setLog(
                        "GM-SDK-APP",
                        "BaseActivity onPointsAdded gc event not found eventName:${eventName} added:${added}"
                    )
                }


            }else{
                setLog(
                    "GM-SDK-APP",
                    "BaseActivity onPointsAdded zero point eventName:${eventName} added:${added}"
                )
            }


        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }

    override fun gamificationOnPointsUpdated(points: Int) {
        try {
            updateUserCoins()
        } catch (e: Exception) {

        }
    }

    fun stopChormeCast(){
        BaseFragment.castPlayer?.let {
            it?.setSessionAvailabilityListener(null);
            it?.release()
            BaseFragment.castPlayer=null
        }
    }
}