package com.hungama.music.player.videoplayer

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
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
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.session.MediaSession
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import androidx.mediarouter.app.MediaRouteButton
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.*
import com.google.android.gms.common.images.WebImage
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.player.download.DownloadTracker
import com.hungama.music.player.videoplayer.services.VideoPlayerService
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.UserCensorRatingPopup
import com.hungama.music.ui.main.viewmodel.VideoViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.callStreamTriggerEvent
import com.hungama.music.utils.CommonUtils.getStringToArray
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.LIST_TYPE
import com.hungama.music.utils.Constant.SELECTED_TRACK_POSITION
import com.hungama.music.utils.Constant.VIDEO_START_POSITION
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.activity_video_player.loading_exoplayer
import kotlinx.android.synthetic.main.activity_video_player.mainFullScreeen
import kotlinx.android.synthetic.main.video_player_custum_control.*
import kotlinx.android.synthetic.main.video_player_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class VideoPlayerActivity : AppCompatActivity(), DownloadTracker.Listener,
    VideoQualitySelectBottomSheetFragment.OnVideoQualityItemClick,
    SubtitleSelectBottomSheetFragment.OnSubTitleItemClick, View.OnTouchListener,
    View.OnClickListener, AudioSubtitleSelectBottomSheetFragment.OnAudioSubTitleItemClick,
    OnUserSubscriptionUpdate, UserCensorRatingPopup.OnUserCensorRatingChange {

    private var isChomeCastClick: Boolean=false
    private var mService: VideoPlayerService? = null
    private var mPlayer: ExoPlayer? = null
    private var mBound = false
    private var playerPosition: Long = 0
    private var mSavedState: Bundle? = null
    private var getPlayerWhenReady: Boolean = false
    var songsList = ArrayList<PlayableContentModel>()
    private var firstDuration = 0L
    private var isFirstDuration = true
    var selectedContentId = ""
    var thumbnailUrl = ""
    var videoStartPosition:Long = 0
    var lastWindowIndex = 0
    var playableItemPosition1 = 0

    private var currentPlayingMediaItem: MediaItem? = null
    private var isScreenLandscape: Boolean? = false
    private val mHideHandler = Handler(Looper.myLooper()!!)
    private val UI_ANIMATION_DELAY = 300

    var isInPipMode:Boolean = false
    var isOnStrop:Boolean = false
    var isPIPModeeEnabled:Boolean = true //Has the user disabled PIP mode in AppOpps?

    private var downloadTracker: DownloadTracker? = null
    var REQUEST_CODE_WRITE_STORAGE_PERMISION = 105
    var downloadState = Status.NONE.value
    var isEnableNextPrvious = false
    var isEnableForwardBackward = true
    var isEnableShuffle = false
    var isEnableRepeat = false
    var isEnablePlaySpeed = false
    private var tapCount = 1
    var isEnableEquilizer = false
    var aSessionId: Int = -1
    private var EQUILISER_REQ_CODE = 999

    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    var toolbar: Toolbar? = null

    var videoListViewModel: VideoViewModel? = null
    var videoListModel: PlayableContentModel? = null

    var subTitleSheetFragment: SubtitleSelectBottomSheetFragment? = null
    var videoQualitySheetFragment: VideoQualitySelectBottomSheetFragment? = null
    var audioSubtitleSelectBottomSheetFragment: AudioSubtitleSelectBottomSheetFragment? = null
    private var sWidth = 0
    private  var sHeight:Int = 0
    private var size: Point? = null
    private val immersiveMode = false
    private  var intLeft = true
    private  var intRight = false
    private  var intTop = false
    private  var intBottom = false
    private  var finLeft = false
    private  var finRight = false
    private  var finTop = false
    private  var finBottom = false
    private var seekSpeed = 0.0
    private var seekDur: String? = null
    private var baseX = 0f
    private  var baseY = 0f
    private var diffX: Long = 0
    private  var diffY = 0
    private var calculatedTime = 0
    private var MIN_DISTANCE = 150
    private var tested_ok = false

    enum class ControlsMode {
        LOCK, FULLCONTORLS
    }

    private var controlsState: ControlsMode? = null
    private var cResolver: ContentResolver? = null
    private var screen_swipe_move = false
    private var brightness = 150
    private  var mediavolume:Int = 0
    private  var device_height:Int = 0
    private  var device_width:Int = 0
    private var audioManager: AudioManager? = null
    private var lastVolumeData = 0
    private var lastVolumeProgress = 0
    private var isMute = false
    var skipIntroST = 0L
    var skipIntroET = 0L
    var skipCreditST = 0L
    var skipCreditET = 0L

    private var mainHandler: Handler? = null
    private val updatePlayer: Runnable? = null
    private var hideVolumeBar: Runnable? = null
    private  var hideControls: Runnable? = null
    private val playerControlVisibilityTimeout:Long = 3000
    var content_type = 0
    var typeId = 4
    var seasonList = ArrayList<PlaylistModel.Data.Body.Row.Season>()
    var originalSeasonList = ArrayList<OrignalSeason.OrignalData.OrignalMisc.OrignalMiscTrack>()
    var isOriginal = false
    var playIndex = 0
    var durationHandler: Handler? = null
    var backFromPip = false


    private val TAG = "VideoPlayerActivity"
    var isRatingVisible = true

    private var mediaSession: MediaSession? = null

    /** Intent action for pip controls from Picture-in-Picture mode.  */
    private val ACTION_PIP_CONTROL = "pip_control"

    /** Intent extra for pip controls from Picture-in-Picture mode.  */
    private val EXTRA_CONTROL_TYPE = "control_type"
    private val CONTROL_TYPE_START_OR_PAUSE = 2
    private val REQUEST_START_OR_PAUSE = 4

    companion object{
        var mPlayerView: PlayerView?=null
    }



    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val binder = iBinder as VideoPlayerService.LocalBinder
            mService = binder.service
            mBound = true
            initializePlayer()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
        }
    }

    private lateinit var mediaRouteButton: MediaRouteButton
    private lateinit var img_cast_menu_dots: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * Prevent Screenshot Or Screen Recorder in Android
         */
        Utils.setStopScreenrecord(this@VideoPlayerActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mPIPMessageReceiver, IntentFilter(Constant.VIDEO_PLAYER_PIP_EVENT))
        // Handle events from the action icons on the picture-in-picture mode.
        registerReceiver(broadcastReceiver, IntentFilter(ACTION_PIP_CONTROL))
        registerBroadcastReceiver()
        landscapMode()
        downloadTracker = DemoUtil.getDownloadTracker(this)

        mPlayerView=findViewById(R.id.player_view)
        Constant.screen_name ="Video Player"

        val b = intent.getBundleExtra(Constant.BUNDLE_KEY)
        if (b != null) {
            selectedContentId = b.getString(Constant.SELECTED_CONTENT_ID, "")
            //selectedContentId = "66857847" //Un-comment this only for testing purpose
            thumbnailUrl = b.getString("thumbnailImg", "")
            videoStartPosition = b.getLong(VIDEO_START_POSITION, 0)
            content_type = b.getInt(Constant.CONTENT_TYPE, 0)
            typeId = b.getInt(Constant.TYPE_ID, 4)
            isOriginal = b.getBoolean(Constant.IS_ORIGINAL, false)
            if(b.containsKey(Constant.SEASON_LIST)){
                if (content_type == Constant.CONTENT_TV_SHOW) {
                    seasonList = b.getParcelableArrayList(Constant.SEASON_LIST)!!
                }
            }else if(b.containsKey(Constant.ORIGINAL_SEASON_LIST) && b.containsKey(Constant.IS_ORIGINAL)){
                if (content_type == Constant.CONTENT_TV_SHOW && isOriginal) {
                    originalSeasonList = b.getParcelableArrayList(Constant.ORIGINAL_SEASON_LIST)!!
                }
            }

            if (!TextUtils.isEmpty(selectedContentId)){
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(selectedContentId.toString())
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(selectedContentId.toString())
                if (downloadedAudio != null){
                    val isDownloaded = downloadTracker!!.isDownloaded(downloadedAudio.downloadUrl!!)
                    setLog(TAG, "onCreate: isDownloaded:${isDownloaded}")
                    if (!isDownloaded){
                        playOnline(selectedContentId)
                        AppDatabase.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(selectedContentId.toString())
                    }else{
                        if (content_type == Constant.CONTENT_TV_SHOW) {
                            val vlm = PlayableContentModel()
                            vlm.data?.head?.headData?.id = downloadedAudio.contentId!!
                            vlm.data?.head?.headData?.title = downloadedAudio.title!!
                            vlm.data?.head?.headData?.image = downloadedAudio.image!!
                            vlm.data?.head?.headData?.type = downloadedAudio.type!!
                            vlm.data?.head?.headData?.misc?.url = downloadedAudio.downloadUrl!!
                            vlm.data?.head?.headData?.misc?.downloadLink?.drm?.token = downloadedAudio.drmLicense
                            vlm.data?.head?.headData?.misc?.movierights = getStringToArray(downloadedAudio.movierights!!)
                            //vlm.drmlicence = downloadedAudio.
                            vlm.data?.head?.headData?.misc?.skipIntro =
                                PlayableContentModel.Data.Head.HeadData.Misc.SkipIntro(
                                    downloadedAudio.skipCreditET?.toLong()!!,
                                    downloadedAudio.skipCreditST?.toLong()!!,
                                    downloadedAudio.skipIntroET?.toLong()!!,
                                    downloadedAudio.skipIntroST?.toLong()!!
                                )
                            setPlayableContentListData(vlm)
                        } else {
                            val vlm = PlayableContentModel()
                            vlm.data?.head?.headData?.id = downloadedAudio.contentId!!
                            vlm.data?.head?.headData?.title = downloadedAudio.title!!
                            vlm.data?.head?.headData?.image = downloadedAudio.image!!
                            vlm.data?.head?.headData?.type = downloadedAudio.type!!
                            vlm.data?.head?.headData?.misc?.url = downloadedAudio.downloadUrl!!
                            vlm.data?.head?.headData?.misc?.downloadLink?.drm?.token = downloadedAudio.drmLicense
                            vlm.data?.head?.headData?.misc?.movierights = getStringToArray(downloadedAudio.movierights!!)
                            //vlm.drmlicence = downloadedAudio.
                            vlm.data?.head?.headData?.misc?.skipIntro =
                                PlayableContentModel.Data.Head.HeadData.Misc.SkipIntro(
                                    downloadedAudio.skipCreditET?.toLong()!!,
                                    downloadedAudio.skipCreditST?.toLong()!!,
                                    downloadedAudio.skipIntroET?.toLong()!!,
                                    downloadedAudio.skipIntroST?.toLong()!!
                                )
                            vlm.data?.head?.headData?.misc?.attributeCensorRating = getStringToArray(
                                downloadedAudio.attribute_censor_rating.toString()
                            )
                            vlm.data?.head?.headData?.misc?.keywords = getStringToArray(
                                downloadedAudio.keywords.toString()
                            )
                            songsList.add(vlm)
                            playableItemPosition1 = 0
                            startPlayerService(songsList, 0)

                            downloadState = Status.COMPLETED.value
                        }
                    }
                } else if (downloadQueue != null){
                    playOnline(selectedContentId)
                    if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.QUEUED.value){
                        downloadState = Status.QUEUED.value
                    }else if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.DOWNLOADING.value){
                        downloadState = Status.DOWNLOADING.value
                    }
                }else{
                    playOnline(selectedContentId)
                }
            }else{
                return
            }


            //setLog("VIDEO_START_POS-act", videoStartPosition.toString())
        }else{
            mPlayer?.release()
        }

        /*if (isMyMusicServiceRunning(MusicPlayerService::class.java) == false) {
            playIntent = Intent(applicationContext, MusicPlayerService::class.java)
            bindService(playIntent, mConnection, BIND_AUTO_CREATE)
            startService(playIntent)
        }*/

        img_back_player?.setOnClickListener{
            try {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
                /*As per https://hungama.atlassian.net/browse/HU-5703 -
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)*/
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                setLog("TAG", "videoplayerforward${hashMap}")
                EventManager.getInstance().sendEvent(VideoPlayerBackTappedEvent(hashMap))
                //backToLastTask(this)
                /*if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                        stopPlayerBGService()
                        finishAndRemoveTask()
                }else{
                    finish()
                }*/
            }catch (e:Exception){

            }

            onBackPressed()
        }

        /*img_full_screen_enter_exit?.setOnClickListener {
            handleFullScreenEnterExit()
        }*/


        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if(isEnableNextPrvious){
            enableDisablePreviousNext(true)
        }else{
            enableDisablePreviousNext(false)
        }
        if (isEnableForwardBackward){
            enableDisableForwardBackward(true)
        }else{
            enableDisableForwardBackward(false)
        }
        if (isEnableShuffle){
            enableDisableShuffle(true)
        }else{
            enableDisableShuffle(false)
        }
        if (isEnableRepeat){
            enableDisableRepeat(true)
        }else{
            enableDisableRepeat(false)
        }
        if (isEnablePlaySpeed){
            enableDisablePlaySpeed(true)
        }else{
            enableDisablePlaySpeed(false)
        }
        llSpeedControl?.setOnClickListener {
            controlPlaybackSpeed()
        }
        tv_play_back_speed?.text = tapCount.toString()

        if (isEnableEquilizer){
            enableDisableEquilizer(true)
        }else{
            enableDisableEquilizer(false)
        }

        exo_equilizer?.setOnClickListener {
            try {
                getAudioSession()
                val systemEq = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                systemEq.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, aSessionId)
                systemEq.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)

                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(systemEq, EQUILISER_REQ_CODE)
                } else {
                    val messageModel = MessageModel(getString(R.string.general_str_8), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(this@VideoPlayerActivity, messageModel)
                }

            } catch (exp: Exception) {
                exp.printStackTrace()
                val messageModel = MessageModel(getString(R.string.general_str_8), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this@VideoPlayerActivity, messageModel)
            }
        }

        size = Point()
        window.windowManager.defaultDisplay.getSize(size)
        sWidth = size!!.x
        sHeight = size!!.y

        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        device_height = displaymetrics.heightPixels
        device_width = displaymetrics.widthPixels
        surfaceView.setOnTouchListener(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        if (content_type == Constant.CONTENT_TV_SHOW){
            if (!seasonList.isNullOrEmpty() && !seasonList?.get(0)?.data?.misc?.tracks.isNullOrEmpty()
                && seasonList?.get(0)?.data?.misc?.tracks?.size!! > 1){
                rlNextEpisode?.setOnClickListener {
                    try {
                        setLog("rlNextEpisode","currentPlayer?.nextWindowIndex:${currentPlayer?.nextWindowIndex!!}")
                        videoStartPosition = 0
                        if (currentPlayer?.nextWindowIndex!! < 0){
                            videoStartPosition = 0
                            playIndex=nextIndex
                            if(playIndex>songsList.size){
                                rlNextEpisode?.visibility = View.GONE
                            }
                            playNextVideo()

                        }else{
                            playIndex = currentPlayer?.nextWindowIndex!!
                            videoStartPosition = 0
                            playNextVideo()
                        }
                    }catch (e:Exception){

                    }

                }
            }else if (!originalSeasonList.isNullOrEmpty() && !originalSeasonList.isNullOrEmpty()
                && originalSeasonList?.size!! > 1){
                rlNextEpisode?.setOnClickListener {
                    try {
                        setLog("rlNextEpisode","currentPlayer?.nextWindowIndex:${currentPlayer?.nextWindowIndex!!}")
                        videoStartPosition = 0
                        if (currentPlayer?.nextWindowIndex!! < 0){
                            videoStartPosition = 0
                            playIndex=nextIndex
                            if(playIndex>songsList.size){
                                rlNextEpisode?.visibility = View.GONE
                            }
                            playNextVideo()

                        }else{
                            playIndex = currentPlayer?.nextWindowIndex!!
                            videoStartPosition = 0
                            playNextVideo()
                        }
                    }catch (e:Exception){

                    }

                }
            }else{
                rlNextEpisode?.visibility = View.GONE
            }
        }else{
            rlNextEpisode?.visibility = View.GONE
        }

    }



    private fun buildMediaQueueItem(videoUrl: String, title: String): MediaQueueItem {
        val movieMetadata =
            com.google.android.gms.cast.MediaMetadata(com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE, title)
        movieMetadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_ALBUM_ARTIST,songsList.get(mPlayer!!.currentWindowIndex).data.head.headData.subtitle)
        movieMetadata.addImage(WebImage(Uri.parse(songsList.get(mPlayer!!.currentWindowIndex)?.data?.head?.headData?.image)));

        var mimType = MimeTypes.BASE_TYPE_AUDIO
        if (videoUrl.contains(".m3u8", true)!!) {
            mimType = MimeTypes.APPLICATION_M3U8
        } else if (videoUrl.contains(".mp3", true)!!) {
            mimType = MimeTypes.BASE_TYPE_AUDIO
        } else if (videoUrl.contains(".mpd", true)!!) {
            mimType = MimeTypes.APPLICATION_MPD
        } else if (videoUrl.contains(".mp4", true)!!) {
            mimType = MimeTypes.APPLICATION_MP4
        }

        val mediaInfo = MediaInfo.Builder(Uri.parse(videoUrl).toString())
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType(mimType)
            .setMetadata(movieMetadata).build()
        return MediaQueueItem.Builder(mediaInfo).build()
    }
    var nextIndex=-1
    private fun startPlayerService(
        item: ArrayList<PlayableContentModel>,
        playableItemPosition: Int
    ): Boolean {
        if (item.size > playableItemPosition) {
      /*      val track =  Track()
            track.id = item[playableItemPosition].data.head.headData.id.toLong()
            track.title = item[playableItemPosition].data.head.headData.title
            track.playerType = item[playableItemPosition].data.head.headData.type.toString()
            track.heading = item[playableItemPosition].data.head.headData.title
            if (applicationContext != null){

                     callStreamTriggerEvent(applicationContext, track, "Video Player")
            }*/
            if (currentPlayer == null) {
                if (content_type == Constant.CONTENT_TV_SHOW){
                    nextIndex = playableItemPosition + 1
                    if (!item.isNullOrEmpty() && item.size > nextIndex){
                        rlNextEpisode?.visibility = View.VISIBLE
                    }else{
                        rlNextEpisode?.visibility = View.GONE
                    }
                }
                var attributeCensorRating = ""
                if (!item.get(playableItemPosition).data.head.headData.misc.attributeCensorRating.isNullOrEmpty()){
                    attributeCensorRating = item.get(playableItemPosition).data.head.headData.misc.attributeCensorRating.get(0)
                }
                if (!CommonUtils.checkUserCensorRating(this, attributeCensorRating,object : UserCensorRatingPopup.OnUserCensorRatingChange {
                        override fun onCensorRatingChange(rating: Int) {
                            if(rating!=0){
                                startPlayerServer(item!!,playableItemPosition)
                                setLog("checkUserCensorRating", "startPlayerServer called")
                            }
                        }

                    },true)){
                    startPlayerServer(item!!,playableItemPosition)

                }else{
                    head.visibility = View.VISIBLE
                    img_back_player.visibility = View.VISIBLE


                }
            }
            return true
        } else {
            return true
        }
    }

    private fun startPlayerServer(item: ArrayList<PlayableContentModel>, playableItemPosition: Int) {
        var isIndia = false
        var isKeywordAvailable = false
        if (Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
            tvTitle.show()
            isIndia = true
        }else{
            tvTitle.hide()
        }
        if (!item.get(playableItemPosition).data.head.headData.misc.keywords.isNullOrEmpty()){
            tvSubTitle.text = Utils.convertArrayToString(item.get(playableItemPosition).data.head.headData.misc.keywords)
            tvSubTitle.show()
            isKeywordAvailable = true
        }else{
            tvSubTitle.hide()
        }
        if (!isIndia && !isKeywordAvailable){
            rlRattingKeyword.hide()
        }
        Handler().postDelayed({
            isRatingVisible = false
            hideAllControls()
        }, 5000)
        val dpm = DownloadPlayCheckModel()
        dpm.contentId = item.get(playableItemPosition)?.data?.head?.headData?.id?.toString()!!
        dpm.contentTitle = item.get(playableItemPosition)?.data?.head?.headData?.title?.toString()!!
        dpm.planName = item.get(playableItemPosition)?.data?.head?.headData?.misc?.movierights.toString()
        dpm.isAudio = false
        dpm.isDownloadAction = false
        dpm.isDirectPaymentAction = false
        dpm.queryParam = ""
        dpm.isShowSubscriptionPopup = true
        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
        dpm.restrictedDownload = RestrictedDownload.valueOf(item.get(playableItemPosition)?.data?.head?.headData?.misc?.restricted_download!!)

        if (CommonUtils.userCanDownloadContent(this, mainFullScreeen, dpm, this,Constant.drawer_svod_purchase)){
            removePlayerDurationCallback()
            intent = Intent(this, VideoPlayerService::class.java)
            val serviceBundle = Bundle()

            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, item)
            serviceBundle.putString(LIST_TYPE, Constant.VIDEO_LIST)
            serviceBundle.putInt(SELECTED_TRACK_POSITION, playableItemPosition)
            intent?.putExtra(Constant.BUNDLE_KEY, serviceBundle)

            /*bindService(intent, mConnection, BIND_AUTO_CREATE)
//                startService(intent)
            Util.startForegroundService(this, intent)*/
            setLog("selectedContentId2", selectedContentId)
            stopService(intent)
            Util.startForegroundService(this, intent)
            bindService(intent, mConnection, BIND_AUTO_CREATE)
            initializePlayer()
        }else{
            head.visibility = View.VISIBLE
            img_back_player.visibility = View.VISIBLE
        }
    }

    private fun isMyMusicServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if ((serviceClass.name == service.service.className)) {
                return true
            }
        }
        return false
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    private fun restoreSeekPos(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && mPlayer != null) {
            playerPosition = savedInstanceState.getLong("PLAYER_POSITION", 0)
            getPlayerWhenReady = savedInstanceState.getBoolean("PLAY_WHEN_READY")
            if (playerPosition > videoStartPosition){
                mPlayer?.seekTo(playerPosition)
            }else{
                mPlayer?.seekTo(videoStartPosition)
            }
            mPlayer?.playWhenReady = getPlayerWhenReady
        }else{
            mPlayer?.seekTo(videoStartPosition)
        }
    }

    var timer:Timer?=null
    private fun initializePlayer() {
        try {
            if (mBound && mService!=null&&mService?.playerInstance!=null) {
                mPlayer = mService?.playerInstance
                player_view?.player = mPlayer
                this.currentPlayer=mPlayer

                isFirstDuration = false
                firstDuration = mPlayer!!.currentPosition

                setLog("chxhgcghc", firstDuration.toString() + " "+ isFirstDuration)

                restoreSeekPos(mSavedState)
                mPlayer?.addListener(PlayerEventListener())
                mPlayer?.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
                mPlayer?.playWhenReady = true

                startPlayerDurationCallback()
                if(mPlayer!=null&&songsList!=null&&songsList.get(mPlayer?.currentWindowIndex!!)!=null){
                    tvHeading.text = songsList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.title
                    setLog(TAG, "initializePlayer: tvTitle "+tvTitle)
                    setLog(TAG, "initializePlayer: tvHeading"+tvHeading)

                    CoroutineScope(Dispatchers.Main).launch {
                        val hashMapPageView = HashMap<String, String>()

                        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =
                             songsList.get(mPlayer?.currentWindowIndex!!).data.head.headData.title
                        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                            "" + Utils.getContentTypeNameForStream("" + songsList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type)
                        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                            "" + songsList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.id
                        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                            "" + MainActivity.lastItemClicked + "," + MainActivity.headerItemName
                        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "player_video_" +
                                Utils.getContentTypeNameForStream("" + songsList.get(mPlayer?.currentWindowIndex!!).data?.head?.headData?.type).replace(" ","").lowercase()

                        setLog("VideoPlayerPageView", hashMapPageView.toString())
                        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))


                    }
/*
                    if (songsList.size > mPlayer?.currentWindowIndex!!) {
                        val track =  Track()
                        track.id = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.id.toLong()
                        track.title = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.title
                        track.playerType = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.type.toString()
                        track.heading = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.title
                            callStreamTriggerEvent(applicationContext, track, "Video Player")

                            callStreamEventAnalytics(songsList.get(mPlayer?.currentWindowIndex!!), false)
                    }*/


                }

                //Use Media Session Connector from the EXT library to enable MediaSession Controls in PIP.
                /*mediaSession = MediaSessionCompat(this, packageName)
                val mediaSessionConnector = MediaSessionConnector(mediaSession!!)
                mediaSessionConnector.setPlayer(mPlayer)
                mediaSession?.isActive = true
                mediaSession?.setCallback(pipCallback)*/
                //mediaSession = mService?.mediaSessionInstance
                if(songsList.size > 0){
                    currentPlayingMediaItem = mPlayer?.getMediaItemAt(mPlayer?.currentWindowIndex!!)
                    initDownloadMusic()
                    initBwd()
                    initFwd()
                    initSubtitle()
                    initLock()
                    videoQualitySettings()
                    initVolume()
                    initBrightness()
                    initSeekbarThumbnail()
                    initSkipIntro()
                    initSkipCredit()
                    mainHandler = Handler(Looper.getMainLooper())
                    hideControls = Runnable { hideAllControls() }
                    hideVolumeBar = Runnable { volume_slider_container.visibility = View.INVISIBLE }
                    mainHandler?.postDelayed(hideControls!!, playerControlVisibilityTimeout)
                    controlsState = ControlsMode.FULLCONTORLS
                    btn_play.setOnClickListener(this)
                    btn_pause.setOnClickListener(this)

                    callVideoPlayAction(songsList[mPlayer?.currentWindowIndex!!],mPlayer?.currentWindowIndex!!)

                    mediaRouteButton = findViewById(R.id.media_route_button)
                    img_cast_menu_dots = findViewById(R.id.img_cast_menu_dots)
                    mediaRouteButton?.setRemoteIndicatorDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.cast_button_bg
                        )
                    )
                    CastButtonFactory.setUpMediaRouteButton(applicationContext, mediaRouteButton)
                    CastContext.getSharedInstance()
                    CastContext.getSharedInstance()?.setReceiverApplicationId(getString(R.string.chormecast_app_id))

                    img_cast_menu_dots.setOnClickListener {
                        try {
                            setLog(TAG, "initializePlayer:img_cast_menu_dots click ")
                            isChomeCastClick=true
                            mediaRouteButton.performClick()
                        }catch (e:Exception){

                        }
                    }

                    setUpChormeCast()
                    Log.d(TAG, "setCurrentPlayer: isCastSessionAvailable :${BaseFragment.castPlayer?.isCastSessionAvailable} currentPlayingMediaItem:${currentPlayingMediaItem}")
                    setCurrentPlayer((if (BaseFragment.castPlayer != null && BaseFragment.castPlayer?.isCastSessionAvailable!!) BaseFragment.castPlayer else mPlayer)!!)

                    if(timer==null){
                        timer = Timer()
                        timer?.scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                val hashMap = HashMap<String,String>()
                                EventManager.getInstance().sendEvent(HeartbeatEvent(hashMap))
                            }

                        }, 5*60*1000, 13*60*1000) //wait 0 ms before doing the action and do it evry 1000ms (1second)
                    }

                    mPlayer!!.addListener(object : Player.Listener {
                        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                if(mPlayer!=null&&songsList!=null&&songsList.get(mPlayer?.currentWindowIndex!!)!=null){
                                    if (songsList.size > mPlayer?.currentWindowIndex!!) {
                                        val track =  Track()
                                        track.id = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.id.toLong()
                                        track.title = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.title
                                        track.playerType = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.type.toString()
                                        track.heading = songsList[mPlayer?.currentWindowIndex!!].data.head.headData.title

                                        val realDurationMillis: Long = mPlayer!!.getDuration()
                                        setLog("PlayerLength", (realDurationMillis.toInt() / 1000).toString())

                                        callStreamTriggerEvent(applicationContext, track, "Video Player", (realDurationMillis.toInt() / 1000).toString())

                                        firstDuration = (realDurationMillis.toInt() / 1000).toString().toLong()
                                        callStreamEventAnalytics(songsList.get(mPlayer?.currentWindowIndex!!), false, (realDurationMillis.toInt() / 1000).toString())
                                    }
                                }
                            }
                        }
                    })

                }
            }
        }catch (e:Exception){

        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        if (mPlayer != null) {
            playerPosition = mPlayer?.currentPosition!!
            outState.putLong("PLAYER_POSITION", playerPosition)
            getPlayerWhenReady = mPlayer?.playWhenReady!!
            outState.putBoolean("PLAY_WHEN_READY", getPlayerWhenReady)
        }
        super.onSaveInstanceState(outState)
    }


    public override fun onStart() {
        super.onStart()
        setLog("LifeCycle:--", "Start")
        /*if (isMyMusicServiceRunning(MusicPlayerService::class.java) == false) {
            intent = Intent(applicationContext, MusicPlayerService::class.java)
            bindService(intent, mConnection, BIND_AUTO_CREATE)
            startService(intent)
        }
        if (mBound == false && isMyMusicServiceRunning(MusicPlayerService::class.java)) {
            intent = Intent(applicationContext, MusicPlayerService::class.java)
            bindService(intent, mConnection, BIND_AUTO_CREATE)
        }
        initializePlayer()*/
    }


    override fun onResume() {
        super.onResume()
        //handleFullScreenEnterExit()
        landscapMode()
        if (mBound == true && isMyMusicServiceRunning(VideoPlayerService::class.java)) {
            setLog("LifeCycle:--", "ReBind Service")
            intent = Intent(applicationContext, VideoPlayerService::class.java)
            val serviceBundle = Bundle()
            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
            serviceBundle.putString(LIST_TYPE, Constant.VIDEO_LIST)
            intent!!.putExtra(Constant.BUNDLE_KEY, serviceBundle)
            /*if(mService != null){
                mService?.bindService(intent, mConnection, BIND_AUTO_CREATE)
            }*/
            bindService(intent, mConnection, BIND_AUTO_CREATE)
        }
//        initializePlayer()
        if(playerPosition > 0L && !isInPipMode){
            mPlayer?.seekTo(playerPosition)
        }
        show()
        //Makes sure that the media controls pop up on resuming and when going between PIP and non-PIP states.
        player_view.useController = true
        setLog("LifeCycle:--", "Resume")
    }

    override fun onStop() {
        super.onStop()
        setLog("LifeCycle:--", "Stop")

        //PIPmode activity.finish() does not remove the activity from the recents stack.
        //Only finishAndRemoveTask does this.
        //But here we are using finish() because our Mininum SDK version is 16
        //If you use minSdkVersion as 21+ then remove finish() and use finishAndRemoveTask() instead
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            //finish()
            setLog("LifeCycle:--", "isInPipMode:${isInPipMode}")
            if(isInPipMode){
                isInPipMode = false
                setLog("LifeCycle:--", "Stop with stop service")
                stopPlayerBGService(mPlayer!!.currentWindowIndex)
                isOnStrop = true
                finishAndRemoveTask()
            }

        }
    }

    override fun onDestroy() {
        if (!isOnStrop){
            if(mPlayer!=null && mPlayer!!.currentWindowIndex !=null)
            stopPlayerBGService(mPlayer!!.currentWindowIndex)

        }
        isOnStrop = false
        setLog("LifeCycle:--", "onDestroy-VideoPlayerService")
        super.onDestroy()
    }


    fun stopPlayerBGService(currentPlayerPosition:Int){
        try {
            removePlayerDurationCallback()
            setLog("StopPlayer", "StopPlayerCalled928")
            callStreamEventAnalytics(songsList[currentPlayerPosition], true, "")

            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPIPMessageReceiver)
        }catch (e:Exception){
            setLog("LifeCycle:--", "stopPlayerBGService-error-${e.message}")
        }
        callUserStreamUpdate()
        if (mBound){
            setLog("LifeCycle:--", "stopPlayerBGService")
            mBound=false
            if (mediaSession != null){
                mediaSession?.release()
            }

            mService?.let {
                mService?.releasePlayer()
            }
            player_view.player = null
            currentPlayer?.release()
            currentPlayer = null
            stopChormeCast()

            stopService(intent)
            unbindService(mConnection)
            val intent = Intent(Constant.NOTIFICATION_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.VIDEO_PIP_ACTIVITY_RESULT_CODE)
            intent.putExtra(Constant.isPIPVideoPlayerVisible, false)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            try {
                unregisterReceiver(broadcastReceiver)
            }catch (e:Exception){

            }
        }
    }


    private var isPlayNextVideoCastPlayerCalled=false
    private inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            setLog("TAG", "onPlaybackStateChanged playbackState:${playbackState} ")


            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    if(!isFinishing()){

                        removePlayerDurationCallback()
                        if(currentPlayer==BaseFragment.castPlayer && !isPlayNextVideoCastPlayerCalled && CastContext.getSharedInstance()?.sessionManager?.currentCastSession?.remoteMediaClient?.idleReason == MediaStatus.IDLE_REASON_FINISHED){
                            setLog("TAG", "onPlaybackStateChanged idleReason:${CastContext.getSharedInstance()?.sessionManager?.currentCastSession?.remoteMediaClient?.idleReason} playIndex:${playIndex} nextIndex:${nextIndex} size:${songsList?.size}")
                            playIndex=nextIndex
                            if(playIndex>songsList.size){
                                playIndex=0
                            }

                            playNextVideo()
                            isPlayNextVideoCastPlayerCalled=true

                        }
                    }

                }
                ExoPlayer.STATE_BUFFERING -> {
                    if(!isFinishing()){
                        removePlayerDurationCallback()
                        loading_exoplayer.visibility = View.VISIBLE
                    }

                }
                ExoPlayer.STATE_READY -> {
                    if(!isFinishing()){
                        startPlayerDurationCallback()
                        loading_exoplayer.visibility = View.GONE
                    }


                }
                ExoPlayer.STATE_ENDED -> {
                    if(!isFinishing()){
                        removePlayerDurationCallback()

                    }

                }
                else -> {
                    removePlayerDurationCallback()
                }
            }
        }

        private fun callStreamFailedEvent(
            error: PlaybackException
        ) {

            if (videoListModel!=null){
                val hashMap = HashMap<String, String>()


                hashMap.put(EventConstant.CONNECTION_TYPE_EPROPERTY, ConnectionUtil.NETWORK_TYPE)
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, EventConstant.CONSUMPTIONTYPE_ONLINE)
                var newContentId=videoListModel?.data?.head?.headData?.id
                var contentIdData=newContentId?.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY, ""+contentIdData)
                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+videoListModel?.data?.head?.headData?.type))
                hashMap.put(EventConstant.DURATION_EPROPERTY,"00:00")
                hashMap.put(EventConstant.ERRORCODE_EPROPERTY,"")
                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,""+error.errorCodeName)
                hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"Video Player")
                hashMap.put(EventConstant.PCODE_EPROPERTY,""+videoListModel?.data?.head?.headData?.misc?.pName)
                hashMap.put(EventConstant.SCODE_EPROPERTY,""+videoListModel?.data?.head?.headData?.misc?.pName)
                hashMap.put(EventConstant.AP_EPROPERTY,"")
                hashMap.put(EventConstant.BUFF_EPROPERTY,"")
                hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
                hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY,""+""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)

                EventManager.getInstance().sendEvent(StreamFailedEvent(hashMap))
            }

        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            callStreamFailedEvent(error)

            setLog("TAG", "onPlayerError VideoPlayerActivity callStreamFailedEvent:${error} ")
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                openNoInternetPopup()
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            super.onPositionDiscontinuity(reason)
            setLog("onPositionDiscontinuity", "onPositionDiscontinuity reason:${reason}")

            currentPlayer?.let {
                val latestWindowIndex = currentPlayer?.currentWindowIndex
                if(latestWindowIndex!!<currentPlayer?.mediaItemCount!!){
                    currentPlayingMediaItem = currentPlayer?.getMediaItemAt(latestWindowIndex!!)
                    setLog("MediaItem 2", currentPlayingMediaItem.toString())
                    if(latestWindowIndex != lastWindowIndex){
                        if(currentPlayer!=null&&songsList!=null&&songsList.get(latestWindowIndex!!)!=null){
                            tvHeading.text = songsList.get(latestWindowIndex!!).data?.head?.headData?.title
                            setLog(TAG, "onPositionDiscontinuity: tvHeading "+tvHeading)
                        }
                        lastWindowIndex = latestWindowIndex!!
                    }
                    if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION || reason == Player.DISCONTINUITY_REASON_SKIP){
                        setLog("prepareNextSong", "true - "+playIndex)
                        if (playIndex != currentPlayer?.currentWindowIndex){
                            playIndex = currentPlayer?.currentWindowIndex!!
                            setLog("prepareNextSong", playIndex.toString())
                            if (playIndex < 0){
                                setLog("prepareNextSong", "t-1"+playIndex.toString())
                            }else{
                                setLog("prepareNextSong", "f-1"+playIndex.toString())
                                videoStartPosition = 0
                                playNextVideo()
                            }
                        }
                        setLog("prepareNextSong 2", playIndex.toString())
                    }else{
                        setLog("prepareNextSong", "false")
                    }
                }
            }


        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying){
                val contentDownloaded = AppDatabase?.getInstance()?.downloadedAudio()?.findByContentId(selectedContentId)
                if (contentDownloaded != null && contentDownloaded.contentStreamDate <= 0){
                    AppDatabase?.getInstance()?.downloadedAudio()?.updateDownloadedContentStreamDate(System.currentTimeMillis(), selectedContentId)
                }
                startPlayerDurationCallback()
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
            }else{
                removePlayerDurationCallback()
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
            }
        }


    }

    var isStreamEventAnalyticsCalled_AF = ""
    private fun callUserStreamUpdate() {
        if(videoListViewModel!=null && mPlayer!=null&& mPlayer?.currentPosition!=null){
            try {
                val param: java.util.HashMap<String, String> = java.util.HashMap()

                /*param.put("contentId", ""+ songsList?.get(mPlayer?.currentWindowIndex!!)?.id)
                param.put("duration",""+TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!))
                param.put("typeId",""+ ""+Constant.PLAYABLE_CONTENT_VIDEO_ID)*/
                val contentId=songsList?.get(mPlayer?.currentWindowIndex!!)?.data?.head?.headData?.id
                param.put(
                    "contentId",
                    "" + contentId
                )
                var playedDuration=TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!)
                param.put(
                    "playDuration",
                    "" + playedDuration
                )
                param.put("userId",SharedPrefHelper.getInstance().getUserId()!!)
                param.put("totalDuration",""+TimeUnit.MILLISECONDS.toSeconds(mPlayer?.duration!!))
                //param.put("typeId",""+ ""+Constant.PLAYABLE_CONTENT_VIDEO_ID)
                if (content_type == Constant.CONTENT_TV_SHOW){
                    if (isOriginal){
                        param.put("typeId",""+ ""+originalSeasonList?.get(mPlayer?.currentWindowIndex!!)?.data?.type)
                        typeId= originalSeasonList.get(mPlayer?.currentWindowIndex!!)?.data?.type!!
                    }else{
                        param.put("typeId",""+ ""+seasonList.get(0).data?.misc?.tracks?.get(mPlayer?.currentWindowIndex!!)?.data?.type)
                        typeId= seasonList.get(0).data?.misc?.tracks?.get(mPlayer?.currentWindowIndex!!)?.data?.type!!
                    }

                }else{
                    param.put("typeId",""+ ""+typeId)
                }


                videoListViewModel?.updateUserVideoStream(
                    this@VideoPlayerActivity,
                    param
                )

                val model= BodyRowsItemsItem()
                model.itemId =
                    songsList?.get(mPlayer?.currentWindowIndex!!)?.data?.head?.headData?.id?.toInt()!!
                model.type = "" + typeId
                model.itype=15
                model.sr_no=0
                model.data= BodyDataItem()
                model.data?.duration=""+TimeUnit.MILLISECONDS.toSeconds(mPlayer?.duration!!)
                model.data?.durationPlay =
                    TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!)
                model.data?.title =
                    songsList?.get(mPlayer?.currentWindowIndex!!)?.data?.head?.headData?.title!!

                model.data?.type=""+ typeId
                model.data?.itype=15
                model.addedDateTime= DateUtils.getCurrentDateTime()
                setLog("TAG", "callUserStreamUpdate: "+model)
                AppDatabase?.getInstance()?.recentlyPlayDao()?.insertOrReplace(model)
                setLog("VideoPlayerContent", "VideoPlayerActivity-playedDuration-${mPlayer?.currentPosition}")
                HungamaMusicApp?.getInstance()?.userStreamList?.put(contentId.toString(),TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!))

                if(!isStreamEventAnalyticsCalled_AF.equals(contentId) && playedDuration>0){


                    if(playedDuration>30){


                        if(SharedPrefHelper.getInstance().get(PrefConstant.FIRST_STREAM_VIDEO,true)){
                            if(contentId!=null&&!TextUtils.isEmpty(contentId)){
                                /* Track Events in real time */
                                val eventValue: MutableMap<String, Any> = HashMap()
                                eventValue.put(AFInAppEventParameterName.CONTENT_ID,contentId)

                                AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_FIRST_STREAM_VIDEO, eventValue)

                                setLog("TAG", "AppsFlyerLib eventName:first_stream eventProperties:$eventValue")
                                SharedPrefHelper.getInstance().save(PrefConstant.FIRST_STREAM_VIDEO,false)
                            }
                        }


                        /* Track Events in real time */
                        val eventValue: MutableMap<String, Any> = HashMap()
                        eventValue.put("media_duration",playedDuration)
                        if(ConnectionUtil(this@VideoPlayerActivity).networkType?.contains("NO NETWORK AVAILABLE")!!){
                            eventValue.put("stream_type",EventConstant.CONSUMPTIONTYPE_OFFLINE)
                        }else{
                            eventValue.put("stream_type",EventConstant.CONSUMPTIONTYPE_ONLINE)
                        }

                        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE,Utils.getContentTypeName(""+typeId))
                        contentId?.let { eventValue.put(AFInAppEventParameterName.CONTENT_ID, it) }

                        if(playedDuration>0){
                            AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_MEDIA_PLAY, eventValue)
                            if (contentId != null) {
                                isStreamEventAnalyticsCalled_AF=contentId
                            }
                        }

                        setLog("TAG", "callUserStreamUpdate : AppsFlyerLib event called")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(this,
                    mainFullScreeen!!,
                    false,
                    getString(R.string.discover_str_2)
                )
            }
        }
    }

    private fun initBrightness() {
        if (sbBrightness != null) {


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
                    setLog("TAG", "sbBrightness: $progress")

                    initBrightnessTouch(progress)

                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    private fun initVolume() {
        volIcon?.setOnClickListener {
            try {
                //setLog("OnMute", "True")
                if(isMute){
                    isMute = false
                    volume_slider.progress = lastVolumeProgress
                    volIcon.setImageResource(R.drawable.volume)
                    audioManager!!.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        lastVolumeData,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                    )
                }else{
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
            }catch (e:Exception){

            }
        }
        if (sbVolume != null) {

            sbVolume.setProgress(mPlayer?.volume?.toInt()!!)
            sbVolume!!.requestFocus()
            sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                    var volume = (progress.toFloat() / 100f).toFloat()
                    setLog("TAG", "onProgressChanged: $volume")
                    mPlayer?.volume=volume
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    private fun initBwd() {
        if (img_bwd != null) {

            img_bwd?.requestFocus()
            img_bwd?.setOnClickListener {
                try {
                    currentPlayer?.seekTo(currentPlayer!!.currentPosition - 10000)
                    skipItroCreditShowHide(currentPlayer?.currentPosition!!)
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
                    hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                    setLog("TAG", "videobackword${hashMap}")
                    EventManager.getInstance().sendEvent(VideoPlayerSkipBackwardEvent(hashMap))
                }catch (e:Exception){

                }
            }
        }
    }

    private fun initFwd() {
        if (img_fwd != null) {
            img_fwd?.requestFocus()
            img_fwd?.setOnClickListener {
                try {
                    currentPlayer?.seekTo(currentPlayer?.currentPosition!! + 10000)
                    skipItroCreditShowHide(currentPlayer?.currentPosition!!)
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
                    hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                    setLog("TAG", "videoplayerforward${hashMap}")
                    EventManager.getInstance().sendEvent(VideoPlayerSkipForwardEvent(hashMap))
                }catch (e:Exception){}

            }
        }

    }

    var isDisplaySubTite=false

    private fun initSubtitle() {
        if (rlSubtitle != null) {
            if (typeId != 51 && videoListModel != null && !TextUtils.isEmpty(videoListModel?.data?.head?.headData?.misc?.sl?.subtitle?.link)) {
                rlSubtitle?.visibility = View.VISIBLE
                setLog(TAG, "initSubtitle:rlSubtitle "+rlSubtitle)
                tvAudioSubtitle?.text = getString(R.string.video_player_str_16)
            } else {
                rlSubtitle?.visibility = View.GONE
                if (typeId == 51){
                    rlSettings?.visibility = View.GONE
                }
                tvAudioSubtitle?.text = getString(R.string.movie_str_9)
            }
            rlSubtitle?.setOnClickListener {
                try {
                    //exo_subtitle.performClick()
                    if (videoListModel != null && !TextUtils.isEmpty(videoListModel?.data?.head?.headData?.misc?.sl?.subtitle?.link)) {
//                    subTitleSheetFragment = SubtitleSelectBottomSheetFragment(videoListModel)
//                    subTitleSheetFragment?.addSubTitleListener(this@VideoPlayerActivity)
//                    subTitleSheetFragment?.show(supportFragmentManager, "DemoBottomSheetFragment")
                        rlSubtitle?.visibility = View.VISIBLE
                        tvAudioSubtitle?.text = getString(R.string.video_player_str_16)
                    } else {
                        rlSubtitle?.visibility = View.GONE
                        tvAudioSubtitle?.text = getString(R.string.movie_str_9)
                        //Toast.makeText(this, getString(R.string.video_player_str_23), Toast.LENGTH_LONG).show()
                    }
                    val audioVideoListModel = PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
                    val audioSubtitleList: ArrayList<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem> =
                        ArrayList()

                    audioVideoListModel.isSelected = true
                    audioVideoListModel.lang = "English"
                    audioVideoListModel.link = ""
                    audioVideoListModel.langId = 1
                    audioSubtitleList.add(audioVideoListModel)
                    val audioVideoListModel2 = PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
                    audioVideoListModel2.isSelected = false
                    audioVideoListModel2.lang = "Hindi"
                    audioVideoListModel2.link = ""
                    audioVideoListModel2.langId = 2
                    audioSubtitleList.add(audioVideoListModel2)
                    val audioVideoListModel3 = PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
                    audioVideoListModel3.isSelected = false
                    audioVideoListModel3.lang = "Gujarati"
                    audioVideoListModel3.link = ""
                    audioVideoListModel3.langId = 3
                    audioSubtitleList.add(audioVideoListModel3)
                    audioSubtitleSelectBottomSheetFragment = AudioSubtitleSelectBottomSheetFragment(videoListModel, audioSubtitleList)
                    audioSubtitleSelectBottomSheetFragment?.addSubTitleListener(this@VideoPlayerActivity)
                    audioSubtitleSelectBottomSheetFragment?.show(supportFragmentManager, "DemoBottomSheetFragment")
                }catch (e:Exception){

                }

            }

        }

    }
    var isLockScreen=false
    private fun initLock() {
        if (rlLock != null) {
            /*rlLock!!.setOnClickListener {
                setLog("TAG", "initLock: $isLockScreen")
                if(isLockScreen){

//                    player_view?.keepScreenOn=false
                    player_view.hideController()
                }else{
//                    player_view?.keepScreenOn = true
                    player_view.showController()
                }
                isLockScreen = !isLockScreen
            }*/
            rlLock?.setOnClickListener {
                try {
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
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.ACTION_EPROPERTY, "lock")
                    hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
                    /*As per https://hungama.atlassian.net/browse/HU-5704 -
                    hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)*/
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                    setLog("TAG", "videoPlaylock${hashMap}")
                    EventManager.getInstance().sendEvent(VideoPlayerLockTappedEvent(hashMap))
                }catch (e:Exception){}

            }

            rlUnLock?.setOnClickListener {
                try {
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
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.ACTION_EPROPERTY, "unlock")
                    hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
                    /*As per https://hungama.atlassian.net/browse/HU-5704 -
                    hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)*/
                    hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                    setLog("TAG", "videoPlayunlock${hashMap}")
                    EventManager.getInstance().sendEvent(VideoPlayerLockTappedEvent(hashMap))
                }catch (e:Exception){}

            }

        }

    }

    private var isShowingTrackSelectionDialog = false
    val _arrayOfBitrate = ArrayList<Int>()
    val _arrayOfBitrateMap = HashMap<Int, Format>()
    val _arrayOfBandwidth = HashMap<String, Int>()
    private fun videoQualitySettings() {
        if (rlSettings != null) {

            rlSettings?.setOnClickListener {
                try {
                    _arrayOfBitrate.clear()
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
                            for (i in 0 until mappedTrackInfo?.rendererCount!!) {
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
                }catch (e:Exception){

                }
            }
        }

    }

    private fun getVideoBandwidth() {
        var bandWidth: Int
        val defaultMaxInitialBitrate = Int.MAX_VALUE.toLong()
        val defaultBandwidthMeter: DefaultBandwidthMeter = DefaultBandwidthMeter.Builder(this)
            .setInitialBitrateEstimate(defaultMaxInitialBitrate)
            .build()
        _arrayOfBandwidth.put(getString(R.string.quality_1), defaultBandwidthMeter.bitrateEstimate.toInt())
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
            videoQualitySheetFragment?.addVideoListener(this@VideoPlayerActivity)
            videoQualitySheetFragment?.show(
                supportFragmentManager,
                "VideoQualitySelectBottomSheetFragment"
            )

        }
    }


    private fun handleFullScreenEnterExit() {
        val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val orientation = display.orientation

        if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
            val dpValue = resources.getDimensionPixelSize(R.dimen.dimen_200)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val params: ViewGroup.LayoutParams = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            //params.height = dpValue
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            player_view.requestLayout()

            val params2: ViewGroup.LayoutParams = mainFullScreeen.layoutParams
            params2.width = ViewGroup.LayoutParams.MATCH_PARENT
            params2.height = ViewGroup.LayoutParams.MATCH_PARENT
            mainFullScreeen.requestLayout()
            img_full_screen_enter_exit?.setImageResource(R.drawable.ic_full_screen_video)
            isScreenLandscape = false
            fullScreenCall()
            show()
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            fullScreenCall()
            val params: ViewGroup.LayoutParams = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            player_view.requestLayout()
            val params2: ViewGroup.LayoutParams = mainFullScreeen.layoutParams
            params2.width = ViewGroup.LayoutParams.MATCH_PARENT
            params2.height = ViewGroup.LayoutParams.MATCH_PARENT
            mainFullScreeen.requestLayout()
            img_full_screen_enter_exit?.setImageResource(R.drawable.ic_full_screen_video)
            isScreenLandscape = true
            hide()
        }
    }

    private fun landscapMode(){
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        fullScreenCall()
        val params: ViewGroup.LayoutParams = player_view.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        player_view.requestLayout()
        val params2: ViewGroup.LayoutParams = mainFullScreeen.layoutParams
        params2.width = ViewGroup.LayoutParams.MATCH_PARENT
        params2.height = ViewGroup.LayoutParams.MATCH_PARENT
        mainFullScreeen.requestLayout()
        img_full_screen_enter_exit?.setImageResource(R.drawable.ic_full_screen_video)
        isScreenLandscape = true
        hide()
    }

    private fun fullScreenCall() {
        if (Build.VERSION.SDK_INT >= 21) {
            //for new api versions.
            val decorView = window.decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
            decorView.systemUiVisibility = uiOptions
        }
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        head.visibility = View.GONE
    }

    private fun show() {
        val actionBar = supportActionBar
        actionBar?.show()
        head.visibility = View.VISIBLE
    }

    override fun onBackPressed(){
        /*if(mPlayer!=null && songsList!=null){
            val intent = Intent(VIDEO_PLAYER_EVENT)
            intent.putExtra(VIDEO_START_POSITION, mPlayer?.currentPosition)
            intent.putParcelableArrayListExtra(VIDEO_LIST_DATA, songsList)
            intent.putParcelableArrayListExtra(SEASON_LIST, seasonList)
            intent.putExtra(SELECTED_TRACK_POSITION, mPlayer?.currentWindowIndex)
            intent.putExtra("EVENT", Constant.VIDEO_ACTIVITY_RESULT_CODE)
            //setResult(Activity.RESULT_OK, intent)
            sendMessage(intent)
            finish()

        }
        super.onBackPressed()*/



        setLog("pipOn", "onUserLeaveHint isPIPModeeEnabled:${isPIPModeeEnabled} mPlayer:${mPlayer}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            && isPIPModeeEnabled && mPlayer!=null && mPlayer?.isPlaying!! && !isDestroyed) {
            videoStartPosition = mPlayer?.currentPosition!!
            enterPIPMode()
        } else {
            setLog("pipOn:--", "onBackPressed")
            super.onBackPressed()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        setLog("pipOn:--", "isInPictureInPictureMode:${isInPictureInPictureMode}")
        if(mPlayer!=null) {
            //if (newConfig != null) {
                playerPosition = mPlayer?.currentPosition!!
                videoStartPosition = mPlayer?.currentPosition!!
                isInPipMode = !isInPictureInPictureMode
            //}
        }
        if (isInPictureInPictureMode) {
            setLog("pipOn", "Yes isInPictureInPictureMode:${isInPictureInPictureMode}")
            isInPipMode = true
            val intent = Intent(Constant.NOTIFICATION_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.VIDEO_PIP_ACTIVITY_RESULT_CODE)
            intent.putExtra(Constant.isPIPVideoPlayerVisible, true)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else {
            setLog("pipOn", "Noo isInPictureInPictureMode:${isInPictureInPictureMode}")
            isInPipMode = false
            backFromPip = true
            isPIPModeeEnabled=true
        }

        setLog("pipOn", "Pip CurrentState:${getLifecycle().getCurrentState()}")
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            setLog("StopPlayerAuto", "StopPlayerAutoCalled")
            stopPlayerBGService(mPlayer!!.currentWindowIndex)

        }else if (lifecycle.currentState == Lifecycle.State.STARTED){
            isInPipMode = true
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }
    //Called when the user touches the Home or Recents button to leave the app.
    override fun onUserLeaveHint() {

        setLog("pipOn", "onUserLeaveHint isPIPModeeEnabled:${isPIPModeeEnabled} mPlayer:${mPlayer}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            && isPIPModeeEnabled && mPlayer != null && mPlayer?.isPlaying!!) {
            videoStartPosition = mPlayer?.currentPosition!!

            enterPIPMode()

        } else {
            setLog("pipOn:--", "onUserLeaveHint")
            super.onUserLeaveHint()
        }
    }

    fun enterPIPMode(){
        isInPipMode = true
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && mPlayer!=null && !isDestroyed) {
                playerPosition = mPlayer!!.currentPosition
                player_view.useController = false
                hide()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        val sourceRectHint = Rect()
                        video_player_view?.getGlobalVisibleRect(sourceRectHint)
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
                        this.setPictureInPictureParams(params)
                        this.enterPictureInPictureMode(params)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        val params = PictureInPictureParams.Builder()
                        // Calculate the aspect ratio of the PiP screen.
                        val aspectRatio = Rational(player_view.getWidth(), player_view.getHeight())
                        params
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
                            .setAspectRatio(aspectRatio).build()
                        this.setPictureInPictureParams(params.build())
                        this.enterPictureInPictureMode(params.build())
                    }catch (e:Exception){
e.printStackTrace()
                    }

                } else {
                    this.enterPictureInPictureMode()
                }

                /* We need to check this because the system permission check is publically hidden for integers for non-manufacturer-built apps
                   https://github.com/aosp-mirror/platform_frameworks_base/blob/studio-3.1.2/core/java/android/app/AppOpsManager.java#L1640
                   ********* If we didn't have that problem *********
                    val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                    if(appOpsManager.checkOpNoThrow(AppOpManager.OP_PICTURE_IN_PICTURE, packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).uid, packageName) == AppOpsManager.MODE_ALLOWED)
                    30MS window in even a restricted memory device (756mb+) is more than enough time to check, but also not have the system complain about holding an action hostage.
                 */

                Handler(Looper.getMainLooper()).postDelayed({checkPIPPermission()}, 30)
            }
        }catch (e:Exception){
e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkPIPPermission(){
        isPIPModeeEnabled = isInPictureInPictureMode
        if(!isInPictureInPictureMode){
            onBackPressed()
        }
    }


    fun playPause() {
        if (mPlayer != null) {
            if (isPlaying()) {
                pausePlayer()
            } else {
                playPlayer()
            }
        }
    }

    fun isPlaying(): Boolean{
        if (mPlayer != null) {
            return mPlayer?.isPlaying!!
        }
        return false
    }

    private fun initDownloadMusic() {
        setLog("MediaItem 3", currentPlayingMediaItem.toString())
        //isDownloaded = downloadTracker!!.isDownloaded(currentPlayingMediaItem)
        /*if (txtDownloadVideo != null) {
            txtDownloadVideo!!.requestFocus()
            setLog("MediaItem 3", currentPlayingMediaItem.toString())
            isDownloaded = downloadTracker!!.isDownloaded(currentPlayingMediaItem)

            if(isDownloaded){
                *//*txtDownloadVideo?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_download_completed
                    )
                )*//*
                txtDownloadVideo!!.text = "Downloaded"
            }else{
                *//*txtDownloadVideo?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_download
                    )
                )*//*
                txtDownloadVideo!!.text = "Download"
                txtDownloadVideo!!.setOnClickListener {
                    // self download
                    checkStoragePermissions(this)
                }
            }

        }*/
    }

    private fun checkStoragePermissions(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_WRITE_STORAGE_PERMISION
                    )
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_WRITE_STORAGE_PERMISION
                    )
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )) {
                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            REQUEST_CODE_WRITE_STORAGE_PERMISION
                        )
                    }
                }
            } else {
                startDRMDownloadSong()
//               startDownloadSong()
            }
        }
    }

    fun startDRMDownloadSong(){
        val renderersFactory = DemoUtil.buildRenderersFactory( /* context= */
            this,
            false
        )
        if(downloadTracker!=null && currentPlayingMediaItem != null){
            downloadTracker?.toggleDownload(
                supportFragmentManager, currentPlayingMediaItem!!, renderersFactory
            )
        }
    }

    override fun onDownloadsChanged(downloadManager: DownloadManager, download: Download) {

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {

    }

    override fun onDownloadProgress(downloads: List<Download>?, progress: Int) {

    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_video, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_download_video ->{
                *//*if (isDownloaded){
                    Toast.makeText(this, "Downloaded", Toast.LENGTH_LONG).show()
                }else{
                    checkStoragePermissions(this)
                }*//*
            }
            R.id.action_enable_next_previous->{
                if (isEnableNextPrvious){
                    enableDisablePreviousNext(false)
                }else{
                    enableDisablePreviousNext(true)
                }
            }
            R.id.action_enable_forword_backword->{
                if (isEnableForwardBackward){
                    enableDisableForwardBackward(false)
                }else{
                    enableDisableForwardBackward(true)
                }
            }
            R.id.action_enable_shuffle->{
                if (isEnableShuffle){
                    enableDisableShuffle(false)
                }else{
                    enableDisableShuffle(true)
                }
            }

            R.id.action_enable_repeat->{
                if (isEnableRepeat){
                    enableDisableRepeat(false)
                }else{
                    enableDisableRepeat(true)
                }
            }
            R.id.action_enable_play_speed->{
                if (isEnablePlaySpeed){
                    enableDisablePlaySpeed(false)
                }else{
                    enableDisablePlaySpeed(true)
                }
            }

            R.id.action_enable_equilizer->{
                if (isEnableEquilizer){
                    enableDisableEquilizer(false)
                }else{
                    enableDisableEquilizer(true)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        *//*if (isDownloaded){
            menu!!.findItem(R.id.action_download_video).title = resources.getString(R.string.video_player_str_2)
        }else{
            menu!!.findItem(R.id.action_download_video).title = resources.getString(R.string.video_player_str_1)
        }*//*

        if (isEnableNextPrvious){
            menu?.findItem(R.id.action_enable_next_previous)?.title = resources.getString(R.string.video_player_str_4)
        }else{
            menu?.findItem(R.id.action_enable_next_previous)?.title = resources.getString(R.string.video_player_str_3)
        }

        if (isEnableForwardBackward){
            menu?.findItem(R.id.action_enable_forword_backword)?.title = resources.getString(R.string.video_player_str_6)
        }else{
            menu?.findItem(R.id.action_enable_forword_backword)?.title = resources.getString(R.string.video_player_str_5)
        }

        if (isEnableShuffle){
            menu?.findItem(R.id.action_enable_shuffle)?.title = resources.getString(R.string.video_player_str_8)
        }else{
            menu?.findItem(R.id.action_enable_shuffle)?.title = resources.getString(R.string.video_player_str_7)
        }

        if (isEnableRepeat){
            menu?.findItem(R.id.action_enable_repeat)?.title = resources.getString(R.string.video_player_str_10)
        }else{
            menu?.findItem(R.id.action_enable_repeat)?.title = resources.getString(R.string.video_player_str_9)
        }

        if (isEnablePlaySpeed){
            menu?.findItem(R.id.action_enable_play_speed)?.title = resources.getString(R.string.video_player_str_12)
        }else{
            menu?.findItem(R.id.action_enable_play_speed)?.title = resources.getString(R.string.video_player_str_11)
        }

        if (isEnableEquilizer){
            menu?.findItem(R.id.action_enable_equilizer)?.title = resources.getString(R.string.video_player_str_14)
        }else{
            menu?.findItem(R.id.action_enable_equilizer)?.title = resources.getString(R.string.video_player_str_13)
        }
        return super.onPrepareOptionsMenu(menu)
    }*/

    private fun enableDisablePreviousNext(action: Boolean){
        if (action){
            isEnableNextPrvious = action
            exo_prev.visibility = View.VISIBLE
            exo_next.visibility = View.VISIBLE
        }else{
            isEnableNextPrvious = action
            exo_prev.visibility = View.GONE
            exo_next.visibility = View.GONE
        }
    }

    private fun enableDisableForwardBackward(action: Boolean){
        if (action){
            isEnableForwardBackward = action
            img_bwd.visibility = View.VISIBLE
            img_fwd.visibility = View.VISIBLE
        }else{
            isEnableForwardBackward = action
            img_bwd.visibility = View.GONE
            img_fwd.visibility = View.GONE
        }
    }
    private fun enableDisableShuffle(action: Boolean){
        if (action){
            isEnableShuffle = action
            exo_shuffle.visibility = View.VISIBLE
        }else{
            isEnableShuffle = action
            exo_shuffle.visibility = View.GONE
        }
    }
    private fun enableDisableRepeat(action: Boolean){
        if (action){
            isEnableRepeat = action
            exo_repeat_toggle.visibility = View.VISIBLE
        }else{
            isEnableRepeat = action
            exo_repeat_toggle.visibility = View.GONE
        }
    }

    private fun enableDisablePlaySpeed(action: Boolean){
        if (action){
            isEnablePlaySpeed = action
            llSpeedControl.visibility = View.VISIBLE
        }else{
            isEnablePlaySpeed = action
            llSpeedControl.visibility = View.GONE
        }
    }

    private fun controlPlaybackSpeed() {
        when (tv_play_back_speed.text) {
            "1" -> {
                tapCount++
                val param = PlaybackParameters(1.25f)
                mPlayer!!.setPlaybackParameters(param)
                tv_play_back_speed.text = "" + 1.25
            }
            "1.25" -> {
                tapCount++
                val param = PlaybackParameters(1.5f)
                mPlayer!!.setPlaybackParameters(param)
                tv_play_back_speed.text = "" + 1.5
            }
            "1.5" -> {
                tapCount++
                val param = PlaybackParameters(1.75f)
                mPlayer!!.setPlaybackParameters(param)
                tv_play_back_speed.text = "" + 1.75
            }
            "1.75" -> {
                tapCount++
                val param = PlaybackParameters(2f)
                mPlayer!!.setPlaybackParameters(param)
                tv_play_back_speed.text = "" + 2
            }
            else -> {
                tapCount = 0
                mPlayer!!.setPlaybackParameters(null!!)
                tv_play_back_speed.text = "" + 1
            }
        }

    }

    private fun enableDisableEquilizer(action: Boolean){
        if (action){
            isEnableEquilizer = action
            exo_equilizer.visibility = View.VISIBLE
        }else{
            isEnableEquilizer = action
            exo_equilizer.visibility = View.GONE
        }
    }

    private fun getAudioSession() {
        mPlayer?.addAnalyticsListener(object : AnalyticsListener {
            /**
             * Called when the audio session id is set.
             *
             * @param eventTime      The event time.
             * @param audioSessionId The audio session id.
             */
            fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {
                // do something with audioSessionId
                aSessionId = audioSessionId
                val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, aSessionId)
                equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                sendBroadcast(equalizerIntent)
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EQUILISER_REQ_CODE && resultCode == RESULT_OK) {
            return
        }
    }

    private fun setUpVideoListViewModel() {
        videoListViewModel = ViewModelProvider(
            this
        ).get(VideoViewModel::class.java)


        if (ConnectionUtil(this).isOnline) {
            videoListViewModel?.getVideoList(applicationContext, selectedContentId, 5)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setVideoListData(it?.data!!)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(this,mainFullScreeen, true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }


    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
            progress?.visibility = View.VISIBLE
        } else {
            progress?.visibility = View.GONE
        }
    }

    fun setVideoListData(model: PlayableContentModel) {
        if (model != null) {
            songsList.clear()
            videoListModel = model
            if (videoListModel != null && !TextUtils.isEmpty(videoListModel?.data?.head?.headData?.misc?.sl?.subtitle?.link)) {

            } else {
                tvAudioSubtitle?.text = getString(R.string.movie_str_9)
            }

            songsList.add(videoListModel!!)


            if (songsList.size > 0) {
                setLog("LifeCycle:--", "Create")
                playableItemPosition1 = 0
                startPlayerService(songsList, 0)
                ////startPlayerService(getVideoDummyData3("https://hunstream.hungama.com/c/5/968/9d6/65999361/65999361_,100,400,750,1000,1600,.mp4.m3u8?Zfr3jvMVuMCuuwu46DZZ5uqb28GroeK9Yb1HM4rXa3xxr1RKK2k8AIJJBVF6zK6fMd0xwPubXH5QQyW-viS58-Yeq0vivoyo6KVpFHlAZeQLRQKNi8BbceKIwBYQ"))
            }else{
                setLog("NoSong", "NoSong")
            }

        }
    }

    override fun onVideoResolutionClick(videoQuality: VideoQuality) {
        try {
            setLog("TAG", "onVideoResolutionClick:" + videoQuality)

            val hashMap = java.util.HashMap<String, String>()
            hashMap.put(EventConstant.STREAMQUALITYSELECTED_EPROPERTY, "" + videoQuality?.bandwidth)
            hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
            hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
            setLog("TAG", "videoplayerforward${hashMap}")
            EventManager.getInstance().sendEvent(VideoPlayerVideoQualitySelectedEvent(hashMap))

            SharedPrefHelper.getInstance().save(PrefConstant.LAST_VIDEO_QUALITY, videoQuality.title)
            trackSelector = mPlayer?.trackSelector as DefaultTrackSelector?
//        trackSelector?.setParameters(
//            trackSelector?.buildUponParameters()?.setMaxVideoBitrate(videoQuality.bitrate!!)!!
//        )
            val param=DefaultTrackSelector.ParametersBuilder().setMaxVideoBitrate(videoQuality.bitrate!!).build()

            trackSelector?.parameters=param


            if (videoQualitySheetFragment != null) {
                videoQualitySheetFragment?.dismiss()
            }
        }catch (e:Exception){}
    }



    /**
     * Set window brightness, cannot change system brightness.
     *
     * @param activity
     * @param screenBrightness
     *          0-255
     */
    fun setWindowBrightness(activity: Activity,screenBrightness: Int){
        var brightness: Int = screenBrightness
        if (brightness < 1) {
            brightness = 1;
        } else if (screenBrightness > 255) {
            brightness = screenBrightness % 255;
            if (brightness == 0) {
                brightness = 255;
            }
        }
        val window = activity?.getWindow();
        val localLayoutParams = window?.getAttributes();
        localLayoutParams?.screenBrightness = brightness / 255.0f;
        window?.setAttributes(localLayoutParams);
    }

    // Check whether this app has android write settings permission.

    private fun hasWriteSettingsPermission(context: Context): Boolean {
        var ret = true
        // Get the result from below code.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ret = Settings.System.canWrite(context)
        }
        return ret
    }

    // Start can modify system settings panel to let user change the write
    // settings permission.
    private fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        context.startActivity(intent)
    }

    private fun initBrightnessTouch(progress: Int) {

        var brightnessValue=progress/255f
        val lp = window.attributes

        setLog("TAG", "initBrightnessTouch: $brightnessValue")
        lp.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        lp.screenBrightness = brightnessValue.toFloat()
        window.attributes = lp
    }

    var touchGesture = 0
    var touchGestureValue = ""
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                setLog("ActionTouch", "ACTION_DOWN")
                if (event.x < (sWidth / 2)){
                    setLog("ActionTouch", "ACTION_DOWN 10")
                    intLeft = true
                    intRight = false
                }else if(event.x > (sWidth / 2)){
                    setLog("ActionTouch", "ACTION_DOWN 9")
                    intLeft = false
                    intRight = true
                }
                val upperLimit = (sHeight / 4) + 100
                val lowerLimit = ((sHeight / 4) * 3) - 150
                if (event.y < upperLimit){
                    intBottom = false
                    intTop = true
                } else if(event.y > lowerLimit){
                    intBottom = true
                    intTop = false
                }else{
                    intBottom = false
                    intTop = false
                }
                if (mPlayer != null && mPlayer?.duration != null){
                    seekSpeed = (TimeUnit.MILLISECONDS.toSeconds(mPlayer?.duration!!) * 0.1);
                }

                diffX = 0
                calculatedTime = 0;
                seekDur = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(diffX) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffX)),
                    TimeUnit.MILLISECONDS.toSeconds(diffX) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffX)));

                //TOUCH STARTED
                baseX = event.getX()
                baseY = event.getY()
                //updateOnTouch(event)
                return true
            }
            MotionEvent.ACTION_MOVE ->{
                setLog("ActionTouch", "ACTION_MOVE")
                screen_swipe_move=true;
                if(controlsState==ControlsMode.FULLCONTORLS){
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
                        if (intLeft) {
                            setLog("ActionTouch", "ACTION_MOVE 4")
                            cResolver = getContentResolver()
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
                                window.getAttributes()
                            layoutpars.screenBrightness = brightness / 255.toFloat()
                            window.setAttributes(layoutpars)
                        }else if (intRight) {
                            //vol_center_text.visibility = View.VISIBLE
                            mediavolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val maxVol: Int =
                                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            setLog("vol221", diffY.toString())
                            setLog("vol222", maxVol.toString())
                            setLog("vol223", device_height.toString())
                            val cal =
                                diffY.toDouble() * (maxVol.toDouble() / (device_height * 0.5).toDouble())
                            setLog("vol224", cal.toString())
                            var newMediaVolume = mediavolume - cal.toInt()
                            setLog("vol225", cal.toString())
                            if (newMediaVolume > maxVol) {
                                newMediaVolume = maxVol
                            } else if (newMediaVolume < 1) {
                                newMediaVolume = 0
                            }
                            audioManager!!.setStreamVolume(
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
                            //updateOnTouch(event)


                        }
                    }else if (Math.abs(diffX) > Math.abs(diffY)) {
                        setLog("ActionTouch", "ACTION_MOVE 3")
                        touchGesture = 0
                        touchGestureValue = ""
                    }
                }
                return true
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP->{
                setLog("ActionTouch", "ACTION_UP 111")
                screen_swipe_move=false
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

    private fun updateOnTouch(event: MotionEvent) {
        val mTouch: Double = convertTouchEventPoint(event.y)
        val progress = Math.round(mTouch).toInt()
        updateProgress(progress)
    }

    private fun convertTouchEventPoint(yPos: Float): Double {
        val wReturn: Float
        if (yPos > device_height * 2) {
            wReturn = (device_height * 2).toFloat()
            return wReturn.toDouble()
        } else if (yPos < 0) {
            wReturn = 0f
        } else {
            wReturn = yPos
        }
        return wReturn.toDouble()
    }
    private val MAX = 100
    private val MIN = 0

    /**
     * The min value of progress value.
     */
    private val mMin = MIN

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private val mMax = MAX

    /**
     * The increment/decrement value for each movement of progress.
     */
    private val mStep = 2
    private var mPoints = 0
    private fun updateProgress(progress: Int) {

        var progress = progress
        progress = if (progress > device_height) device_height else progress
        progress = if (progress < 0) 0 else progress

        //convert progress to min-max range
        mPoints = progress * (mMax - mMin) / device_height + mMin
        //reverse value because progress is descending
        mPoints = mMax + mMin - mPoints
        //if value is not max or min, apply step
        if (mPoints !== mMax && mPoints !== mMin) {
            mPoints = mPoints - mPoints % mStep + mMin % mStep
        }
        volume_slider_container.setVisibility(View.VISIBLE)
        volume_slider.progress = mPoints
    }

    /**
     * Contains last clicked time
     */
    private var lastClickedTime: Long = 0
    fun isOnClick(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < 5000) {
            return false
        }
        lastClickedTime = SystemClock.elapsedRealtime()
        return true
    }
    private fun callVolumeEvent(action: String) {

        //if(isOnClick()){
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.ACTION_EPROPERTY, action)
            hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
            hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
            hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
            setLog("TAG", "videoPlay${hashMap}")
            EventManager.getInstance().sendEvent(VideoPlayerAudioAction(hashMap))
        //}

    }
    private fun callBrightnessEvent(action: String) {
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.ACTION_EPROPERTY, action)
            hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
            hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
            setLog("TAG", "videoPlay${hashMap}")
            EventManager.getInstance().sendEvent(VideoPlayerBrightnessActionEvent(hashMap))
    }

    private fun initSeekbarThumbnail(){
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
        if (max == 0) { return 0 }

        val parent = previewFrameLayout.parent as ViewGroup
        val layoutParams = previewFrameLayout.layoutParams as ViewGroup.MarginLayoutParams
        val offset = progress.toFloat() / max
        val minimumX: Int = previewFrameLayout.left
        val maximumX = (parent.width - parent.paddingRight - layoutParams.rightMargin)

// We remove the padding of the scrubbing, if you have a custom size juste use dimen to calculate this
        val previewPaddingRadius: Int = dpToPx(resources.displayMetrics, DefaultTimeBar.DEFAULT_SCRUBBER_DRAGGED_SIZE_DP).div(2)
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

    private fun initSkipIntro(){

        if (!songsList.isNullOrEmpty()){
            //Skip intro
            skipIntroST = songsList.get(0)?.data?.head?.headData?.misc?.skipIntro?.skipIntroST!!
            skipIntroST = TimeUnit.SECONDS.toMillis(skipIntroST!!)
            skipIntroET = songsList.get(0)?.data?.head?.headData?.misc?.skipIntro?.skipIntroET!!
            //skipIntroET = 10
            skipIntroET = TimeUnit.SECONDS.toMillis(skipIntroET!!)
            if (skipIntroET > skipIntroST){
                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_credit.visibility = View.INVISIBLE
                    video_player_skip_intro.visibility = View.VISIBLE
                }!!.setLooper(mainLooper).setPosition(0, skipIntroST!!)
                    .setDeleteAfterDelivery(false)
                    .send()

                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_credit.visibility = View.INVISIBLE
                    video_player_skip_intro.visibility = View.INVISIBLE
                }!!.setLooper(mainLooper).setPosition(0, skipIntroET)
                    .setDeleteAfterDelivery(false)
                    .send()

                video_player_skip_intro?.setOnClickListener {
                    try {
                        mPlayer?.seekTo(skipIntroET)
                        video_player_skip_intro.visibility = View.INVISIBLE
                        val hashMap = java.util.HashMap<String, String>()
                        hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
                        hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
                        hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
                        setLog("AUDIO", "SkipIntro${hashMap}")
                        EventManager.getInstance().sendEvent(VideoPlayerSkipIntroEvent(hashMap))
                    }catch (e:Exception){}

                }
            }
            //
        }else{
            setLog("EmptyListVideo", "true")
        }
    }

    private fun initSkipCredit(){
        if (!songsList.isNullOrEmpty() && songsList.size    > playIndex){
            //Skip credit
            skipCreditST = songsList.get(playIndex).data.head.headData.misc.skipIntro.skipCreditST
            //skipCreditST = 15
            skipCreditST = TimeUnit.SECONDS.toMillis(skipCreditST)

            skipCreditET = songsList.get(playIndex).data.head.headData.misc.skipIntro.skipCreditET
            //skipCreditET = 25
            skipCreditET = TimeUnit.SECONDS.toMillis(skipCreditET)

            if (skipCreditST > 0 && skipCreditET > skipCreditST) {
                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_intro.visibility = View.INVISIBLE
                    video_player_skip_credit.visibility = View.VISIBLE
                }!!.setLooper(mainLooper).setPosition(0, skipCreditST)
                    .setDeleteAfterDelivery(false)
                    .send()

                mPlayer?.createMessage { messageType, payload ->
                    video_player_skip_intro.visibility = View.INVISIBLE
                    video_player_skip_credit.visibility = View.INVISIBLE
                }!!.setLooper(mainLooper).setPosition(0, skipCreditET)
                    .setDeleteAfterDelivery(false)
                    .send()
                if (content_type == Constant.CONTENT_TV_SHOW) {
                    btnSkipCredit.text = getString(R.string.skip_to_next_episode)
                }else{
                    btnSkipCredit.text = getString(R.string.video_player_str_24)
                }
                video_player_skip_credit?.setOnClickListener {
                    try {
                        mPlayer?.seekTo(skipCreditET)
                        video_player_skip_credit.visibility = View.INVISIBLE
                        if (content_type == Constant.CONTENT_TV_SHOW) {
                            val tempIndex = playIndex+1
                            if (tempIndex < 0){
                                setLog("prepareNextSong", "t-1"+tempIndex.toString())
                            }else{
                                if (!songsList.isNullOrEmpty() && songsList.size > tempIndex){
                                    playIndex = tempIndex
                                    setLog("prepareNextSong", "f-1"+playIndex.toString())
                                    videoStartPosition = 0
                                    playNextVideo()
                                }
                            }
                        }
                    }catch (e:Exception){}

                }
            }

            //
        }
    }

    private fun skipItroCreditShowHide(position: Long){
        if (skipIntroST < position && skipIntroET > position){
            video_player_skip_intro.visibility = View.VISIBLE
        }else{
            video_player_skip_intro.visibility = View.INVISIBLE
        }
        if (skipCreditST < position && skipCreditET> position){
            video_player_skip_credit.visibility = View.VISIBLE
        }else{
            video_player_skip_credit.visibility = View.INVISIBLE
        }
    }

    override fun onClick(v: View?) {
        val id = v?.id
        if (id == R.id.btn_pause) {
            pausePlayer()
        }else if (id == R.id.btn_play) {
            playPlayer()
        }
    }

    private fun hideAllControls() {
        if (controlsState === ControlsMode.FULLCONTORLS) {
            /*if (root.getVisibility() === View.VISIBLE) {
                root.setVisibility(View.GONE)
            }*/
            brightness_slider_container.visibility = View.INVISIBLE
            volume_slider_container.visibility = View.INVISIBLE
            rlCenterControll.visibility = View.INVISIBLE
            rlBottomControll.visibility = View.INVISIBLE
            img_back_player.visibility = View.INVISIBLE
            tvHeading.visibility = View.INVISIBLE
            mediaRouteButton?.visibility = View.INVISIBLE
            vPlayerBg.hide()
            if (isRatingVisible){
                img_back_player.show()
                rlRattingKeyword.show()
            }else{
                rlRattingKeyword.hide()
            }
        } else if (controlsState === ControlsMode.LOCK) {
            if (rlUnLock.getVisibility() === View.VISIBLE) {
                rlUnLock.setVisibility(View.GONE)
            }
        }
    }

    private fun showControls() {
        if (controlsState === ControlsMode.FULLCONTORLS) {
            /*if (root.getVisibility() === View.GONE) {
                root.setVisibility(View.VISIBLE)
            }*/
            rlCenterControll.visibility = View.VISIBLE
            rlBottomControll.visibility = View.VISIBLE
            head.visibility = View.VISIBLE
            img_back_player.visibility = View.VISIBLE
            mediaRouteButton?.visibility = View.INVISIBLE
            vPlayerBg.show()
            if (isRatingVisible){
                tvHeading.visibility = View.INVISIBLE
                rlRattingKeyword.show()
            }else{
                tvHeading.visibility = View.VISIBLE
                rlRattingKeyword.hide()
            }
        } else if (controlsState === ControlsMode.LOCK) {
            if (rlUnLock.getVisibility() === View.GONE) {
                rlUnLock.visibility = View.VISIBLE
            }
        }
        hideControls?.let{ mainHandler?.removeCallbacks(it) }
        hideControls?.let{ mainHandler?.postDelayed(it, playerControlVisibilityTimeout) }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val action = event?.action
        val keyCode = event?.keyCode
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
                    /*setLog("Volume++",
                        audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toString()
                    )*/

                    hideVolumeBar?.let { mainHandler?.removeCallbacks(it) }
                    hideVolumeBar?.let { mainHandler?.postDelayed(it, playerControlVisibilityTimeout) }
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
                    /*setLog("Volume--",
                        audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toString()
                    )*/
                    hideVolumeBar?.let { mainHandler?.removeCallbacks(it) }
                    hideVolumeBar?.let { mainHandler?.postDelayed(it, playerControlVisibilityTimeout) }
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun setUpPlayableContentListViewModel(id:String) {
        videoListViewModel = ViewModelProvider(
            this
        ).get(VideoViewModel::class.java)


        if (ConnectionUtil(this).isOnline) {
            videoListViewModel?.getVideoList(this, id, 5)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setPlayableContentListData(it?.data)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(this,mainFullScreeen, true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            songsList = arrayListOf()
            var playableItemPosition = 0
            if (isOriginal){

                if (!originalSeasonList.isNullOrEmpty()){
                    for (i in originalSeasonList?.indices!!) {
                        if (playableContentModel.data?.head?.headData?.id == originalSeasonList?.get(
                                i
                            )?.data?.id
                        ) {
                            videoListModel=playableContentModel
                            playableItemPosition = i
                            setOriginalVideoList(
                                playableContentModel,
                                originalSeasonList,
                                i
                            )
                        } else {
                            setOriginalVideoList(null, originalSeasonList, i)
                        }
                    }
                }
            }else{
                if (!seasonList.isNullOrEmpty()){
                    for (i in seasonList?.get(0)?.data?.misc?.tracks?.indices!!) {
                        if (playableContentModel.data?.head?.headData?.id == seasonList.get(0).data?.misc?.tracks?.get(
                                i
                            )?.data?.id
                        ) {
                            videoListModel=playableContentModel
                            playableItemPosition = i
                            setVidelList(
                                playableContentModel,
                                seasonList.get(0).data?.misc?.tracks,
                                i
                            )
                        } else {
                            setVidelList(null, seasonList.get(0).data?.misc?.tracks, i)
                        }
                    }
                }
            }


            if (!songsList.isNullOrEmpty() && songsList.size > 0) {
                setLog("LifeCycle:--", "Season-All")
                playableItemPosition1 = playableItemPosition
                startPlayerService(songsList, playableItemPosition)
            }else{
                setLog("NoSong", "NoSong")
            }
        }
    }

    fun setVidelList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row.Season.SeasonData.Misc.Track?>?,
        position: Int
    ) {
        val track = PlayableContentModel()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            track.data?.head?.headData?.id = playableItem?.get(position)?.data?.id!!.toString()
        } else {
            track.data?.head?.headData?.id = "0"
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.name)) {
            track.data?.head?.headData?.title = playableItem?.get(position)?.data?.name!!
        } else {
            track.data?.head?.headData?.title = ""
        }

        if (playableContentModel?.data?.head?.headData?.subtitle != null) {
            track.data?.head?.headData?.subtitle = playableContentModel.data.head.headData.subtitle
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track?.data?.head?.headData?.misc?.url =
                playableContentModel?.data?.head?.headData?.misc?.url!!
        } else {
            track?.data?.head?.headData?.misc?.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track?.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token.toString()
        } else {
            track?.data?.head?.headData?.misc?.downloadLink?.drm?.token = ""
        }

        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()) {
            track?.data?.head?.headData?.misc?.movierights =
                playableItem?.get(position)?.data?.misc?.movierights!!
        } else {
            track?.data?.head?.headData?.misc?.movierights = listOf()
        }

        /*if (!TextUtils.isEmpty(playerType)){
            track.playerType = playerType
        }else{
            track.playerType = Constant.PLAYER_PODCAST_AUDIO_TRACK
        }*/
        /*if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title)){
            track.heading = podcastRespModel?.data?.head?.data?.title
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)){
            track.data?.head?.headData?.image = playableItem?.get(position)?.data?.image.toString()
        }else{
            track.data?.head?.headData?.image = ""
        }
        if (playableContentModel != null){
            track.data.head.headData.type = playableContentModel.data.head.headData.type
            track.data.head.headData.misc.attributeCensorRating = playableContentModel.data.head.headData.misc.attributeCensorRating
            track.data.head.headData.misc.keywords = playableContentModel.data.head.headData.misc.keywords
            track.data?.head?.headData?.misc?.skipIntro =
                PlayableContentModel.Data.Head.HeadData.Misc.SkipIntro(
                    playableContentModel.data.head.headData.misc.skipIntro.skipCreditET?.toLong()!!,
                    playableContentModel.data.head.headData.misc.skipIntro.skipCreditST?.toLong()!!,
                    playableContentModel.data.head.headData.misc.skipIntro.skipIntroET?.toLong()!!,
                    playableContentModel.data.head.headData.misc.skipIntro.skipIntroST?.toLong()!!
                )
        }
        songsList.add(track)
    }

    fun setOriginalVideoList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<OrignalSeason.OrignalData.OrignalMisc.OrignalMiscTrack>?,
        position: Int
    ) {
        val track = PlayableContentModel()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            track.data?.head?.headData?.id = playableItem?.get(position)?.data?.id!!.toString()
        } else {
            track.data?.head?.headData?.id = "0"
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.name)) {
            track.data?.head?.headData?.title = playableItem?.get(position)?.data?.name!!
        } else {
            track.data?.head?.headData?.title = ""
        }

        if (playableContentModel?.data?.head?.headData?.subtitle != null) {
            track.data?.head?.headData?.subtitle = playableContentModel.data.head.headData.subtitle
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track?.data?.head?.headData?.misc?.url =
                playableContentModel?.data?.head?.headData?.misc?.url!!
        } else {
            track?.data?.head?.headData?.misc?.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track?.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token.toString()
        } else {
            track?.data?.head?.headData?.misc?.downloadLink?.drm?.token = ""
        }

        if (!playableItem?.get(position)?.data?.movierights.isNullOrEmpty()) {
            track?.data?.head?.headData?.misc?.movierights =
                playableItem?.get(position)?.data?.movierights!!
        } else {
            track?.data?.head?.headData?.misc?.movierights = listOf()
        }

        /*if (!TextUtils.isEmpty(playerType)){
            track.playerType = playerType
        }else{
            track.playerType = Constant.PLAYER_PODCAST_AUDIO_TRACK
        }*/
        /*if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title)){
            track.heading = podcastRespModel?.data?.head?.data?.title
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)){
            track.data?.head?.headData?.image = playableItem?.get(position)?.data?.image.toString()
        }else{
            track.data?.head?.headData?.image = ""
        }
        if (playableContentModel != null){
            track.data.head.headData.type = playableContentModel.data.head.headData.type
            track.data.head.headData.misc.attributeCensorRating = playableContentModel.data.head.headData.misc.attributeCensorRating
            track.data.head.headData.misc.keywords = playableContentModel.data.head.headData.misc.keywords
            track.data?.head?.headData?.misc?.skipIntro =
                PlayableContentModel.Data.Head.HeadData.Misc.SkipIntro(
                    playableContentModel.data.head.headData.misc.skipIntro.skipCreditET?.toLong()!!,
                    playableContentModel.data.head.headData.misc.skipIntro.skipCreditST?.toLong()!!,
                    playableContentModel.data.head.headData.misc.skipIntro.skipIntroET?.toLong()!!,
                    playableContentModel.data.head.headData.misc.skipIntro.skipIntroST?.toLong()!!
                )
        }
        songsList.add(track)
    }


    private fun playOnline(selectedContentId: String) {
        if (content_type == Constant.CONTENT_TV_SHOW) {
            this.selectedContentId = selectedContentId
            setUpPlayableContentListViewModel(selectedContentId)
        } else {
            setUpVideoListViewModel()
        }
    }

    private fun callVideoPlayAction(track: PlayableContentModel, currentWindowIndex: Any) {
        val eventModel = HungamaMusicApp.getInstance().getEventData("" + track?.data?.head?.headData?.id)
        val hashMap = HashMap<String, String>()
        hashMap.put(EventConstant.ACTION_EPROPERTY, "Play")
        hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
        hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + eventModel.songName)
        hashMap.put(EventConstant.FROMBUCKET_EPROPERTY, "" + eventModel.bucketName)
        hashMap.put(EventConstant.LASTVISIBLEROWPOSITION_EPROPERTY, "${currentWindowIndex}")
        hashMap.put(EventConstant.LISTINGSCREENNAME_EPROPERTY, MainActivity.headerItemName)
        hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+track?.data?.head?.headData?.title)
        hashMap.put(EventConstant.TOBUCKET_EPROPERTY, eventModel.bucketName)

        hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+track?.data?.head?.headData?.type))
        EventManager.getInstance().sendEvent(VideoPlayerActionsEvent(hashMap))
    }

    private fun callStreamEventAnalytics(track: PlayableContentModel, isStream:Boolean, duration:String) {
        try {
            val eventModel = HungamaMusicApp.getInstance().getEventData("" + track?.data?.head?.headData?.id)
            var totalDurationPlayed = 0L

            setLog("EvendModelData", ""+ mPlayer + " " + mPlayer!!.currentPosition)

            if (duration.isNotEmpty()){
                totalDurationPlayed = duration.toLong()
            }
            else{
                if (mPlayer != null && mPlayer?.currentPosition != null && mPlayer?.currentPosition!! > 0){
                    totalDurationPlayed = TimeUnit.MILLISECONDS.toSeconds(mPlayer?.currentPosition!!)
                }
            }

            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
            hashMap.put(EventConstant.AUDIO_QUALITY_EPROPERTY, "" + eventModel?.audioQuality)
            hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + eventModel?.bucketName)
            hashMap.put(
                EventConstant.CONNECTION_TYPE_EPROPERTY,
                ConnectionUtil(this@VideoPlayerActivity).networkType
            )
            if (ConnectionUtil(this@VideoPlayerActivity).isOnline(false)){
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + eventModel?.consumptionType)
            }else{
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + EventConstant.CONSUMPTIONTYPE_OFFLINE)
            }
            val newContentId= track?.data?.head?.headData?.id
            val contentIdData=newContentId?.replace("playlist-","")
            hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" +contentIdData)
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

            if (TextUtils.isEmpty(duration)){
                if(!TextUtils.isEmpty(eventModel.duration) && !eventModel.duration.equals("0")){
                    length=eventModel.duration.toLong()
                }
            }
            else {
                length = duration.toLong()
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

            hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+ if(!TextUtils.isEmpty(eventModel?.bucketName)) "_"+ eventModel?.bucketName else "_"+ eventModel?.sourceName)
            hashMap.put(EventConstant.SOURCE_PAGE_EPROPERTY, "" + MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName + if(!TextUtils.isEmpty(eventModel?.bucketName)) "_"+ eventModel?.bucketName else "_"+ eventModel?.sourceName)

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
                    if (isStream) {
                        setLog("StopPlayer", "StreamEventCalled ")
                        EventManager.getInstance().sendEvent(StreamEvent(hashMap))
                    }
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

    var mediaItemArrayList: MutableList<MediaItem>?=null
    override fun onAudioSubTitleItemClick(
        model: PlayableContentModel?,
        subtitleItem: PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?
    ) {
        setLog("TAG", "onSubTitleItemClick:" + model)

        val hashMap = java.util.HashMap<String, String>()
        hashMap.put(EventConstant.ACTION_EPROPERTY, "" + subtitleItem?.lang)
        hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
        hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
        hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
        setLog("AUDIO", "VideoPlayerSubtitle${hashMap}")
        EventManager.getInstance().sendEvent(VideoPlayerSubtitleSelectedEvent(hashMap))

        mediaItemArrayList = ArrayList()
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
            subtitleItem?.lang,
            C.SELECTION_FLAG_DEFAULT
        )
        subtitles.add(subTitleMenuItem)
        if (!TextUtils.isEmpty(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
            val mediaItem1 = MediaItem.Builder()
                .setDrmUuid(drmSchemeUuid)
                .setDrmLicenseUri(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(model?.data?.head?.headData?.title).build()
                )
                .setMimeType(mimType)
                .build()

            mediaItemArrayList?.add(mediaItem1)

            setLog(TAG, "onAudioSubTitleItemClick: DRM content play url:${model?.data?.head?.headData?.misc?.url}")
        }else{
            val mediaItem1 = MediaItem.Builder()
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(model?.data?.head?.headData?.title).build()
                )
                .setMimeType(mimType)
                .build()
            mediaItemArrayList?.add(mediaItem1)

            setLog(TAG, "onAudioSubTitleItemClick: Non DRM content play url:${model?.data?.head?.headData?.misc?.url}")
        }


        if (mediaItemArrayList?.size!! > 0 && mPlayer != null) {

            val lastPosition = mPlayer?.currentPosition

            mPlayer?.setMediaItems(mediaItemArrayList!!)
            mPlayer?.prepare()

            if (lastPosition != null && lastPosition > 0) {
                mPlayer?.seekTo(lastPosition)
            }

        }

        if (subTitleSheetFragment != null) {
            subTitleSheetFragment?.dismiss()
        }
    }

    override fun onSubTitleItemClick(
        model: PlayableContentModel?,
        subtitleItem: PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?
    ) {
        setLog("TAG", "onSubTitleItemClick:" + model)
        val arrayList: MutableList<MediaItem> = ArrayList()
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
            subtitleItem?.lang,
            C.SELECTION_FLAG_DEFAULT
        )
        subtitles.add(subTitleMenuItem)
        if (!TextUtils.isEmpty(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
            val mediaItem1 = MediaItem.Builder()
                .setDrmUuid(drmSchemeUuid)
                .setDrmLicenseUri(model?.data?.head?.headData?.misc?.downloadLink?.drm?.token)
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(model?.data?.head?.headData?.title).build()
                )
                .setMimeType(mimType)
                .build()
            arrayList.add(mediaItem1)
            setLog(TAG, " videoplayer setMediaItem: DRM content play url:${model?.data?.head?.headData?.misc?.url}")
        }else{
            val mediaItem1 = MediaItem.Builder()
                .setUri(Uri.parse(model?.data?.head?.headData?.misc?.url))
                .setSubtitles(subtitles)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(model?.data?.head?.headData?.title).build()
                )
                .setMimeType(mimType)
                .build()
            arrayList.add(mediaItem1)

            setLog(TAG, "setMediaItem: Non DRM content play url:${model?.data?.head?.headData?.misc?.url}")
        }


        if (arrayList.size > 0 && mPlayer != null) {

            val lastPosition = mPlayer?.currentPosition

            mPlayer?.setMediaItems(arrayList)
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

    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should
    // be received by the ReceiverActivity.
    private fun sendMessage(intent: Intent) {
        setLog("sender-11", "Broadcasting message")
        //val intent2 = Intent("custom-event-name")
        // You can also include some extra data.
        //intent2.putExtra("message", "This is my message!")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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
                    context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                if (strAction == Intent.ACTION_USER_PRESENT || strAction == Intent.ACTION_SCREEN_OFF || strAction == Intent.ACTION_SCREEN_ON) if (myKM.inKeyguardRestrictedInputMode()) {
                    println("Screen off " + "LOCKED")
                    if (mPlayer != null){
                        mPlayer?.pause()
                    }
                } else {
                    println("Screen off " + "UNLOCKED")
                    if (mPlayer != null){
                        mPlayer?.play()
                    }
                }
            }
        }
        applicationContext.registerReceiver(screenOnOffReceiver, theFilter)
    }

    private val updatePlayerDurationTask = object : Runnable {
        override fun run() {
            updatePlayerDuration()
            durationHandler?.postDelayed(this, 1000)
        }
    }
    fun updatePlayerDuration(){
        val currMinute = (TimeUnit.MILLISECONDS.toMinutes(currentPlayer?.currentPosition!!) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(currentPlayer?.currentPosition!!)))
        val currSecond = TimeUnit.MILLISECONDS.toSeconds(currentPlayer?.currentPosition!!) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPlayer?.currentPosition!!))
        val curDur = String.format(
            "%02d:%02d",
            currMinute,
            currSecond
        )

        val toMinute = TimeUnit.MILLISECONDS.toMinutes(currentPlayer?.duration!!) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(currentPlayer?.duration!!))
        val toSecond = TimeUnit.MILLISECONDS.toSeconds(currentPlayer?.duration!!) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPlayer?.duration!!))
        val diff: Long = currentPlayer?.duration!! - currentPlayer?.currentPosition!!
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(minutes)

        /*val totDur = String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )*/


        if (typeId == 51){
            val totDur = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(diff) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(diff) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
            //mm:ss
            exo_duration_video?.setText(totDur)
        }else{
            val totDur = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(diff),
                TimeUnit.MILLISECONDS.toMinutes(diff) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(diff) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
            //hh:mm:ss
            exo_duration_video?.setText(totDur)
        }

        //exo_position_new?.setText(curDur)
        //exo_duration_video?.setText(totDur)

    }

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

    /**
     * Bring up MainActivity task to front
     */
    fun backToLastTask(appContext: Context) {
        setLog(TAG, "backToLastTask: backFromPip:${backFromPip}")

        if (backFromPip){
            val activityManager = (appContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            val appTasks = activityManager.appTasks
            for (task in appTasks) {
                val baseIntent = task.taskInfo.baseIntent
                val categories = baseIntent.categories
                if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                    task.moveToFront()
                    return
                }
            }
        }
    }

    private fun setUpChormeCast() {

        setLog(TAG, "setUpChormeCast called player:${BaseFragment.castPlayer} session:${BaseFragment.castPlayer?.isCastSessionAvailable}")

       if(BaseFragment.castPlayer==null){
            BaseFragment.castPlayer = CastContext.getSharedInstance()?.let { CastPlayer(it) }
            BaseFragment.isCastPlayerAudio=false
        }


       BaseFragment.castPlayer?.setSessionAvailabilityListener(object :
            SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
//                playChormecast()
                setLog(TAG, "onCastSessionAvailable: castPlayer: ${BaseFragment.castPlayer} session${BaseFragment.castPlayer?.isCastSessionAvailable}")
                if(BaseFragment.castPlayer!=null){
                    setCurrentPlayer(BaseFragment.castPlayer!!)
                }else{
                    BaseFragment.castPlayer = CastContext.getSharedInstance()?.let { CastPlayer(it) }
                    BaseFragment.isCastPlayerAudio=false
                    if(BaseFragment.castPlayer!=null){
                        setCurrentPlayer(BaseFragment.castPlayer!!)
                    }
                }


            }

            override fun onCastSessionUnavailable() {
                setLog(TAG, "onCastSessionUnavailable: ")
                setLog("pipMode", "MusicVideoDetailsFragment-setUpChormeCastpausePlayer-onCastSessionUnavailable-pausePlayer-called")
//                playPlayer()
                mPlayer?.let {
                    setCurrentPlayer(mPlayer!!)
                }

                stopChormeCast()
            }

        })

    }
    fun stopChormeCast(){
        BaseFragment.castPlayer?.let {
            it.setSessionAvailabilityListener(null)
            it.release()
            BaseFragment.castPlayer=null
        }
    }

    private fun playNextVideo(){
        if (!TextUtils.isEmpty(songsList?.get(playIndex)?.data?.head?.headData?.id!!)) {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = songsList?.get(playIndex)?.data?.head?.headData?.id?.toString()!!
            dpm.contentTitle = songsList?.get(playIndex)?.data?.head?.headData?.title?.toString()!!
            dpm.planName = songsList?.get(playIndex)?.data?.head?.headData?.misc?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload = RestrictedDownload.valueOf(songsList?.get(playIndex)?.data?.head?.headData?.misc?.restricted_download!!)
            setLog("VideoPlayer", "VideoPlayerActivity-rlNextEpisode-dpm.planName-1-${dpm.planName}")
            if (CommonUtils.userCanDownloadContent(this, mainFullScreeen, dpm, this,Constant.drawer_svod_tvshow_episode)){
                setLog("VideoPlayer", "VideoPlayerActivity-rlNextEpisode-dpm.planName-2-${dpm.planName}")
                stopPlayerBGService(mPlayer!!.currentWindowIndex-1)
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                    ?.findByContentId(selectedContentId.toString())
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                    ?.findByContentId(selectedContentId.toString())
                if (downloadedAudio != null) {
                    val isDownloaded =
                        downloadTracker!!.isDownloaded(downloadedAudio.downloadUrl!!)
                    if (!isDownloaded) {
                        playOnline(songsList?.get(playIndex)?.data?.head?.headData?.id!!)
                        AppDatabase.getInstance()?.downloadedAudio()
                            ?.deleteDownloadQueueItemByContentId(selectedContentId.toString())
                    } else {
                        val vlm = PlayableContentModel()
                        vlm.data?.head?.headData?.id = downloadedAudio.contentId!!
                        vlm.data?.head?.headData?.title = downloadedAudio.title!!
                        vlm.data?.head?.headData?.image = downloadedAudio.image!!
                        vlm.data?.head?.headData?.misc?.url = downloadedAudio.downloadUrl!!
                        vlm.data?.head?.headData?.misc?.downloadLink?.drm?.token = downloadedAudio.drmLicense
                        vlm.data?.head?.headData?.misc?.movierights = getStringToArray(downloadedAudio.movierights!!)
                        //vlm.drmlicence = downloadedAudio.
                        vlm.data?.head?.headData?.misc?.skipIntro =
                            PlayableContentModel.Data.Head.HeadData.Misc.SkipIntro(
                                downloadedAudio.skipCreditET?.toLong()!!,
                                downloadedAudio.skipCreditST?.toLong()!!,
                                downloadedAudio.skipIntroET?.toLong()!!,
                                downloadedAudio.skipIntroST?.toLong()!!
                            )
                        setPlayableContentListData(vlm)
                    }
                } else if (downloadQueue != null){
                    playOnline(songsList?.get(playIndex)?.data?.head?.headData?.id!!)
                    if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.QUEUED.value){
                        downloadState = Status.QUEUED.value
                    }else if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.DOWNLOADING.value){
                        downloadState = Status.DOWNLOADING.value
                    }
                }else{
                    playOnline(songsList?.get(playIndex)?.data?.head?.headData?.id!!)
                }
            }else{
                setLog("VideoPlayer", "VideoPlayerActivity-rlNextEpisode-dpm.planName-3-${dpm.planName}")
            }
        }
    }


    private val mPIPMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver-1", "VideoPlayerActivity-mMessageReceiver-" + intent)
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    setLog(
                        "BroadcastReceiver-1",
                        "VideoPlayerActivity-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                    )
                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                    ) {
                        if (mPlayer!=null)
                        stopPlayerBGService(mPlayer!!.currentWindowIndex)
                        finishAndRemoveTask()
                    } else {
                        finish()
                    }
                }
            }
        }
    }

    var pipCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            setLog("PIPEvents", "VideoPlayerActivity-PIPEvents-onPlay-Clicked")
            playPlayer()
        }

        override fun onPause() {
            super.onPause()
            setLog("PIPEvents", "VideoPlayerActivity-PIPEvents-onPause-Clicked")
            pausePlayer()
        }
    }

    fun pausePlayer(){
        try {
            currentPlayer?.pause()
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
            // As per jira bug we are remove content name property - https://hungama.atlassian.net/browse/HU-3742
            //hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
            if (isInPipMode){
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER)
            }else{
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
            }

            setLog("TAG", "videopause${hashMap}")
            EventManager.getInstance().sendEvent(VideoPlayerPauseEvent(hashMap))
        }catch (e:Exception){}

    }

    fun playPlayer(){
        try {
            if (currentPlayer?.isPlaying == false) {
                currentPlayer?.play()
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
            }
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "" + Utils.getContentTypeName(""+songsList.get(playIndex).data?.head?.headData?.type)!!)
            // As per jira bug we are remove content name property - https://hungama.atlassian.net/browse/HU-3742
            //hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + songsList.get(playIndex).data?.head?.headData?.title!!)
            if (isInPipMode){
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER)
            }else{
                hashMap.put(EventConstant.PLAYERTYPE_EPROPERTY, EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER)
            }
            setLog("TAG", "videoPlay${hashMap}")
            EventManager.getInstance().sendEvent(VideoPlayerPlayEvent(hashMap))
        }catch (e:Exception){}
    }

    private fun openNoInternetPopup(){
        ConnectionUtil(this).isOnline
    }
    private var currentPlayer: Player? = null
    private fun setCurrentPlayer(mCurrentPlayer: Player) {
        Log.d(TAG, "setCurrentPlayer: isFinishing:${isFinishing} videoListModel:${videoListModel} mPlayerView:${mPlayerView}")

        if(!isFinishing() && videoListModel!=null && mPlayerView!=null){
            Log.d(TAG, "setCurrentPlayer: currentPlayer:${mCurrentPlayer==mPlayer}")
            if (currentPlayer == mCurrentPlayer) {
                return
            }
            mPlayerView?.setPlayer(mCurrentPlayer)
            mPlayerView?.setControllerHideOnTouch(mCurrentPlayer === mPlayer)
            if (mCurrentPlayer == BaseFragment.castPlayer && BaseFragment.castPlayer?.isCastSessionAvailable!!) {
                Log.d(TAG, "setCurrentPlayer: castPlayer called:${BaseFragment.castPlayer}")

                mPlayerView?.setControllerShowTimeoutMs(0)
                mPlayerView?.showController()
                mPlayerView?.setDefaultArtwork(
                    ResourcesCompat.getDrawable(
                        resources!!,
                        R.drawable.ic_baseline_cast_connected_400,  /* theme= */
                        null
                    )
                )
                isPlayNextVideoCastPlayerCalled=false
            } else { // currentPlayer == localPlayer
                mPlayerView?.setControllerShowTimeoutMs(
                    PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS)
                mPlayerView?.setDefaultArtwork(null)
            }

            var playWhenReady = false
            var playbackPositionMs = C.TIME_UNSET
            currentPlayer?.let {
                val previousPlayer: Player = currentPlayer!!
                // Player state management.

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
            }

            currentPlayer = mCurrentPlayer
            currentPlayer?.addListener(PlayerEventListener())

            Log.d(TAG, "setCurrentPlayer: currentPlayer playIndex:${playIndex} currentPlayingMediaItem:${currentPlayingMediaItem} videoListModel:${videoListModel}")

            if(videoListModel!=null && currentPlayer!=null){
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
                        .setMediaId(videoListModel?.data?.head?.headData?.id!!)
                        .setMimeType(mimType)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(videoListModel?.data?.head?.headData?.title)
                                .setDisplayTitle(videoListModel?.data?.head?.headData?.title)
                                .setSubtitle(videoListModel?.data?.head?.headData?.subtitle)
                                .setArtworkUri(Uri.parse(videoListModel?.data?.head?.headData?.image!!))
                                .setDescription(videoListModel?.data?.head?.headData?.misc?.description)
                                .build()
                        ).build()

                    currentPlayer?.setMediaItem(mediaItem1!!, playbackPositionMs!!)
                    currentPlayer?.playWhenReady = true
                    currentPlayer?.prepare()
                    currentPlayer?.play()
                    Log.d(TAG, "setCurrentPlayer: videoListModel url:${videoListModel?.data?.head?.headData?.misc?.url} mimType:${mimType} title${videoListModel?.data?.head?.headData?.title} artworkUri:${videoListModel?.data?.head?.headData?.image} displayTitle:${videoListModel?.data?.head?.headData?.title} ")
                }

            }else if(currentPlayingMediaItem!=null && currentPlayer!=null ){
                Log.d(TAG, "setCurrentPlayer: currentPlayingMediaItem title${currentPlayingMediaItem?.mediaMetadata?.title} artworkUri:${currentPlayingMediaItem?.mediaMetadata?.artworkUri} displayTitle:${currentPlayingMediaItem?.mediaMetadata?.displayTitle} ")
                currentPlayer?.setMediaItem(currentPlayingMediaItem!!, playbackPositionMs!!)
                currentPlayer?.playWhenReady = true
                currentPlayer?.prepare()
                currentPlayer?.play()
            }
        }


    }

    override fun onCensorRatingChange(rating: Int) {
        Log.d(TAG, "onCensorRatingChange 1: rating${rating}")
        if(rating!=0 && isDestroyed()!=null && songsList!=null&&songsList?.size!!>0){
            onCreate(intent?.extras)
            Log.d(TAG, "onCensorRatingChange 2: initializeComponent called")
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
                Icon.createWithResource(this, iconResId),
                getString(titleResId),
                getString(titleResId),
                PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    Intent(ACTION_PIP_CONTROL)
                        .putExtra(EXTRA_CONTROL_TYPE, controlType),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
     }
}
