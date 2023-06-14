package com.hungama.music.player.audioplayer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.session.*
import androidx.media3.ui.PlayerNotificationManager
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.hungama.music.HungamaMusicApp
import com.hungama.music.auto.*
import com.hungama.music.auto.api.model.HomeModel
import com.hungama.music.auto.channel.Channel
import com.hungama.music.auto.channel.HungamaChannelHelper
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.mediautils.NotificationManager
import com.hungama.music.player.audioplayer.mediautils.OnBitmapLoadedListener
import com.hungama.music.player.audioplayer.mediautils.loadAlbumArtUri
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.player.AudioPlayer
import com.hungama.music.player.audioplayer.player.ExoPlayer
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.player.audioplayer.queue.QueueManager
import com.hungama.music.player.audioplayer.repositories.ITrackRepository
import com.hungama.music.player.audioplayer.services.ad.ImaService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.audioplayer.widget.BaseWedget.Companion.PLAYPOSITION
import com.hungama.music.player.audioplayer.widget.BaseWedget.Companion.WIDGET_NO_PLAYING_EXTRA
import com.hungama.music.player.audioplayer.widget.BaseWedget.Companion.WIDGET_PLAYING_EXTRA
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.ACTION_PAUSE
import com.hungama.music.utils.Constant.ACTION_PAUSE_PREVIOUS
import com.hungama.music.utils.Constant.ACTION_PLAY
import com.hungama.music.utils.Constant.ACTION_PLAY_NEXT
import com.hungama.music.utils.Constant.BUNDLE_KEY
import com.hungama.music.utils.Constant.IS_TRACKS_QUEUEITEM
import com.hungama.music.utils.Constant.ITEM_KEY
import com.hungama.music.utils.Constant.PLAYBACK_NOTIFICATION_ID
import com.hungama.music.utils.Constant.PLAY_CONTEXT
import com.hungama.music.utils.Constant.PLAY_CONTEXT_TYPE
import com.hungama.music.utils.Constant.SELECTED_TRACK_PLAY_START_POSITION
import com.hungama.music.utils.Constant.SELECTED_TRACK_POSITION
import com.hungama.music.utils.Constant.SHUFFLE_TRACKS
import com.hungama.music.BuildConfig
import com.hungama.music.R
import kotlinx.coroutines.*
import java.util.HashMap


@OptIn(UnstableApi::class)
class AudioPlayerService : MediaLibraryService(),
    NotificationManager.OnNotificationPostedListener,
    Player.Listener,TracksContract.View {
    private val TAG: String = AudioPlayerService::class.java.name

    public lateinit var queueManager: QueueManager

    public lateinit var audioPlayer: AudioPlayer

    public lateinit var tracksRepository: ITrackRepository
    public var mItem: ArrayList<Track>? = null
    public var playPosition: Int = 0
    public var imaService: ImaService? = null
    var isStopAudioPlayer = false
    var baseActivity: BaseActivity? = null
    private lateinit var mPlayerNotificationManager: NotificationManagerCompat
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaLibrarySession? = null


    companion object {
        public const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
        const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

        public const val CONTENT_STYLE_BROWSABLE_HINT =
            "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
        public const val CONTENT_STYLE_PLAYABLE_HINT =
            "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
        public const val CONTENT_STYLE_GROUP_TITLE_HINT =
            "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT"
        public const val CONTENT_STYLE_SINGLE_ITEM_HINT =
            "android.media.browse.CONTENT_STYLE_SINGLE_ITEM_HINT"

        public const val CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1
        public const val CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2
        public const val CONTENT_STYLE_CATEGORY_LIST_ITEM_VALUE = 3



        public var bookmarkDataModel: HomeModel? = null
        public var podcastBucketRespModel: HomeModel? = null
        public var discoverBucketRespModel: HomeModel? = null
        public var discoverBucketList = mutableListOf<MediaItem>()
        public var mediaDescListing = mutableListOf<MediaItem>()
        public var libListing = mutableListOf<MediaItem>()
        public var libAllList = mutableListOf<MediaItem>()
        public var discoverBucketWiseList = mutableListOf<MediaItem>()
        public var podcastBucketList = mutableListOf<MediaItem>()
        public var playListLibraryList = mutableListOf<MediaItem>()
        public var albumLibraryList = mutableListOf<MediaItem>()
        public var podcastLibraryList = mutableListOf<MediaItem>()
        public var artistLibraryList = mutableListOf<MediaItem>()
        public var recentPlayedList = mutableListOf<MediaItem>()
        public var radioLibList = mutableListOf<MediaItem>()
        public var favSongLibList = mutableListOf<MediaItem>()
        public var podcastBucketWiseList = mutableListOf<MediaItem>()
        public var podcastTrackListingListing = mutableListOf<MediaItem>()
        public var songList = mutableListOf<MediaItem>()

        public var currentChannelList = ArrayList<Channel>()
        public var discoverBucketWiseChannelList = ArrayList<Channel>()

        public const val PREF_LISTENER_KEY = "pref_listener_key"
        public const val PREF_LAST_MEDIA_ID = "pref_last_media_id"


        public const val MEDIA_ID_FAVORITES = "media_id_favorites"
        public const val MEDIA_ID_LIB_ALL = "media_id_lib_all"
        public const val MEDIA_ID_LIB_DOWNLOAD = "media_id_lib_download"
        public const val MEDIA_ID_LIB_FAV = "media_id_lib_fav"
        public const val MEDIA_ID_LIB_PLAYLIST = "media_id_lib_playlist"
        public const val MEDIA_ID_LIB_ALBUM = "media_id_lib_album"
        public const val MEDIA_ID_LIB_PODCAST = "media_id_lib_podcast"
        public const val MEDIA_ID_LIB_ARTIST = "media_id_lib_artist"
        public const val MEDIA_ID_LIB_RADIO = "media_id_lib_radio"
        public const val MEDIA_ID_DISCOVER = "media_id_discover"
        public const val MEDIA_ID_RECENT = "media_id_recent"
        public const val MEDIA_ID_PODCAST = "media_id_podcast"
        public const val MEDIA_ID_LIBRARY = "media_id_library"
        public const val CUSTOM_ACTION_FAVORITE = "com.hungama.music.shared.ui.custom_action_favorite"
        public const val EXTRA_CHANNEL = "extra_channel"

        public const val HUNGAMA_ROOT = "root"
        public const val HUNGAMA_DISCOVER_ROOT = "__DISCOVER__"
        public const val HUNGAMA_PODCAST_ROOT = "__PODCAST__"
        public const val HUNGAMA_PODCAST_DETAIL_ROOT = "__PODCAST__DETAIL__"
        public const val HUNGAMA_DISCOVER_DETAIL_ROOT = "__DISCOVER__DETAIL__"
        public const val HUNGAMA_CATEGORY_DETAIL_ROOT = "__CATEGORY__DETAIL__"
        public const val HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT = "__LIBRARY_ALBUM_DETAIL__"
        public const val HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT = "__LIBRARY_PLAYLIST_DETAIL__"
        public const val HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT = "__LIBRARY_ARTIST_DETAIL__"


        public const val RESOURCE_ROOT_URI = "android.resource://com.hungama.myplay.activity/drawable/"

        /*
         * (Media) Session events
         */
        public const val NETWORK_FAILURE = "com.hungama.music.shared.media.session.NETWORK_FAILURE"
        public val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

        public const val TAG = "MusicService"

        const val ORIGINAL_ARTWORK_URI_KEY = "com.hungama.myplay.activity.JSON_ARTWORK_URI"

        var currentChannel:Channel?=null
    }




    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        setLog("NotificationManager", "onPlayerStateChanged-9 action:${intent?.getAction()}")
        if (MediaBrowserServiceCompat.SERVICE_INTERFACE.equals(intent?.getAction())) {
            return super.onBind(intent)!!
        }
        return AudioServiceBinder()
    }

    inner class AudioServiceBinder : Binder() {

        fun getPlayerInstance(): SimpleExoPlayer {
            return (audioPlayer as ExoPlayer).simpleExoPlayer
        }

        fun getNowPlayingQueue(): NowPlayingQueue {
            return (audioPlayer as ExoPlayer).nowPlayingQueue
        }

        val service:AudioPlayerService
            get() = this@AudioPlayerService
    }

    private var isAdPlaying = false
    private var songCurrentPosition = 0L
    inner class SharedAudioPlayer{
        fun claim() {
            isAdPlaying = true
            songCurrentPosition=(audioPlayer as ExoPlayer).simpleExoPlayer.currentPosition
            (audioPlayer as ExoPlayer).simpleExoPlayer.setPlayWhenReady(false)
            val songName = (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.mediaMetadata?.title
            setLog("ImaAdsService", "AudioPlayerService-claim-songCurrentPosition-$songCurrentPosition  songName-$songName")
        }

        fun release() {
            if (isAdPlaying) {
                isAdPlaying = false

                if((audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()!=null && (audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()?.size!! > BaseActivity.nowPlayingCurrentIndex()){
                    //setLog("ImaAdsService", "AudioPlayerService-release-getConcatenatingMediaSourceFromPlayer-title-${(audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()?.getMediaSource(BaseActivity.nowPlayingCurrentIndex())?.mediaItem?.mediaMetadata?.title}")
                    //(audioPlayer as ExoPlayer).simpleExoPlayer.setMediaSource((audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()!!)
                    (audioPlayer as ExoPlayer).simpleExoPlayer.prepare((audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()!!)
                    (audioPlayer as ExoPlayer).simpleExoPlayer.setPlayWhenReady(true)
                    (audioPlayer as ExoPlayer).simpleExoPlayer?.seekTo(
                        BaseActivity.nowPlayingCurrentIndex(),
                        songCurrentPosition
                    )
                    val songName = (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.mediaMetadata?.title
                    setLog("ImaAdsService", "AudioPlayerService-release-songCurrentPosition-$songCurrentPosition  songName-$songName")
                    setLog(TAG, "release: ConcatenatingMediaSource is set now")
                }else{
                    setLog("ImaAdsService", "AudioPlayerService-release-ConcatenatingMediaSource is null")
                    setLog(TAG, "release: ConcatenatingMediaSource is null")
                }


                // TODO: Seek to where you left off the stream, if desired.
            }
        }

        fun prepare(mediaSource: MediaSource?) {
            (audioPlayer as ExoPlayer).simpleExoPlayer.setMediaSource(mediaSource!!)
            (audioPlayer as ExoPlayer).simpleExoPlayer.prepare()
        }

        fun addAnalyticsListener(listener: AnalyticsListener?) {
            (audioPlayer as ExoPlayer).simpleExoPlayer.addAnalyticsListener(listener!!)
        }

        fun getPlayer(): ExoPlayer? {
            return audioPlayer as ExoPlayer
        }

        fun onAdsError(){

        }
    }

    override fun onCreate() {
        super.onCreate()
        setLog("NotificationManager", "onCreate onPlayerStateChanged-0 ")
        isAdPlaying = false
        audioPlayer = Injection.provideAudioPlayer()

        tracksRepository = Injection.provideTrackRepository()
        audioPlayer?.let {
            audioPlayer.apply {
                init(this@AudioPlayerService)
                //setNotificationPostedListener(this@AudioPlayerService)
                setPlayerEventListener(this@AudioPlayerService)
                showPlayerNotification()
            }
        }


        val dataSourceFactory = DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name)))

        imaService = ImaService(applicationContext,dataSourceFactory,SharedAudioPlayer())

//        mediaSession = audioPlayer.getMediaSession()?.mediaSession
        audioPlayer?.let {
            Log.d(TAG, "mediaSession called packageName:${packageName}")
            mediaSession = with(MediaLibrarySession.Builder(
                this,
                (audioPlayer as ExoPlayer).simpleExoPlayer,
                MusicServiceCallback())) {
                setId(packageName)
                packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                    setSessionActivity(
                        PendingIntent.getActivity(
                            /* context= */ this@AudioPlayerService,
                            /* requestCode= */ 0,
                            sessionIntent,
                            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE
                            else PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                }
                build()
            }


        }

        /*mediaSession = MediaSessionCompat(this@AudioPlayerService, "Hungama!")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }
        val token: MediaSession.Token = mediaSession?.sessionToken!!
        sessionToken?.sessionToken = token*/
        //mediaSessionConnector=MediaSessionConnector(mediaSession!!)
        //mediaSessionConnector?.setPlayer((audioPlayer as ExoPlayer).simpleExoPlayer)
        //mediaSessionConnector?.setPlaybackPreparer(PlaybackPreparer((audioPlayer as ExoPlayer).simpleExoPlayer))

//        currentPlayer=(audioPlayer as ExoPlayer).simpleExoPlayer
//        setUpChormeCast()
        /*if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                Constant.PLAYBACK_CHANNEL_ID,
                Constant.PLAYBACK_CHANNEL_NAME,
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager).createNotificationChannel(
                notificationChannel
            )
            val notification = NotificationCompat.Builder(this, Constant.PLAYBACK_CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(PLAYBACK_NOTIFICATION_ID, notification)
        }*/
    }

    @Suppress("UNCHECKED_CAST")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        setLog(TAG, "NotificationManager Received ${intent?.action}")

        if (intent == null || intent.action == null)
            return START_STICKY



        /*audioPlayer?.let {
            setLog("NotificationManager", "onPlayerStateChanged-10 intent.action:${intent.action}")
            setPlayerNotification(ArrayList(), 0, (audioPlayer as ExoPlayer).simpleExoPlayer)
        }*/

        isStopAudioPlayer = false
        when (intent.action) {
            PlaybackControls.PLAY.name -> {
                setLog(
                    "SwipablePlayerFragment",
                    "AudioPlayerService-onStartCommand()-PlaybackControls.PLAY.name:"
                )
                CoroutineScope(Dispatchers.Main).launch {
                    var selectedTrackPosition = intent.getIntExtra(SELECTED_TRACK_POSITION, -1)
                    //BaseActivity.queueNowPlayIndex=selectedTrackPosition
                    val selectedTrackPlayStratPosition =
                        intent.getLongExtra(SELECTED_TRACK_PLAY_START_POSITION, 0)
                    val shuffle = intent.getBooleanExtra(SHUFFLE_TRACKS, false)
                    val isQueueItem = intent.getBooleanExtra(IS_TRACKS_QUEUEITEM, false)
                    val isPause = intent.getBooleanExtra(Constant.IS_PAUSE, false)

                    val playContext: PLAY_CONTEXT =
                        intent.getSerializableExtra(PLAY_CONTEXT_TYPE) as PLAY_CONTEXT
                    var tracksList = mutableListOf<Track>()

                    when (playContext) {


                        PLAY_CONTEXT.LIBRARY_TRACKS -> {
                            try {
                                setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PLAY.name-LIBRARY_TRACKS")
                                queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
                                if (!BaseActivity.isApplaunch)
                                {
                                    callStream(queueManager, BaseActivity.nowPlayingCurrentIndex())
                                }
                                BaseActivity.isApplaunch = false
                                val trackListDataModel = tracksRepository.getAllTracks(applicationContext, selectedTrackPosition, queueManager as NowPlayingQueue)
                                selectedTrackPosition = trackListDataModel.selectedTrackIndex
                                tracksList = trackListDataModel.trackListData
                                mItem = tracksList as ArrayList<Track>
                                playPosition = selectedTrackPosition
                            }catch (e:Exception){

                            }
                        }

                        PLAY_CONTEXT.VIDEO_TRACK -> {
                            try {
                                tracksList = tracksRepository.getAllTracksVideo(applicationContext)
                            }catch (e:Exception){

                            }
                        }

                        PLAY_CONTEXT.QUEUE_TRACKS -> {
                            try {
                                setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PLAY.name-QUEUE_TRACKS")
                                queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
                                if (BaseActivity.isPresvious) {
                                    callStream(queueManager, BaseActivity.nowPlayingCurrentIndex())
                                    BaseActivity.isPresvious = false
                                }
                                else
                                    callStream(queueManager, BaseActivity.nowPlayingCurrentIndex()-1)
                                val trackListDataModel = tracksRepository.getAllQueuedTracks(applicationContext, selectedTrackPosition,
                                    queueManager as NowPlayingQueue
                                )
                                selectedTrackPosition = trackListDataModel.selectedTrackIndex
                                tracksList = trackListDataModel.trackListData
                                mItem = tracksList as ArrayList<Track>
                            }catch (e:Exception){

                            }
                        }

                        PLAY_CONTEXT.LOCAL_DEVICE_LIBRARY_TRACKS -> {
                            try {
                                queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
                                callStream(queueManager, BaseActivity.nowPlayingCurrentIndex())
                                val trackListDataModel = tracksRepository.getAllTracks(applicationContext, selectedTrackPosition, queueManager as NowPlayingQueue)
                                selectedTrackPosition = trackListDataModel.selectedTrackIndex
                                tracksList = trackListDataModel.trackListData
                                mItem = tracksList as ArrayList<Track>
                                playPosition = selectedTrackPosition
                            }catch (e:Exception){

                            }
                        }
                    }
                    setPlayerNotification(tracksList, selectedTrackPosition, (audioPlayer as ExoPlayer).simpleExoPlayer)
                    audioPlayer.playTracks(
                        applicationContext,
                        tracksList,
                        selectedTrackPosition,
                        shuffle,
                        selectedTrackPlayStratPosition,
                        isQueueItem,
                        isPause
                    )



                }
            }
            ACTION_PLAY -> {
                if ((audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag == null){
                    setPlayerNotification(mItem!!, playPosition, (audioPlayer as ExoPlayer).simpleExoPlayer)
                    audioPlayer.playTracks(applicationContext, mItem!!, playPosition)
                }
                (audioPlayer as ExoPlayer).simpleExoPlayer.playWhenReady = true
            }
            ACTION_PAUSE -> {
                (audioPlayer as ExoPlayer).simpleExoPlayer.playWhenReady = false
            }
            ACTION_PLAY_NEXT -> {
                //playPosition = playPosition + 1
                if (BaseActivity.player11 != null){
                    if (BaseActivity.player11?.isPlaying == true){
//                        return
                    }
                }
                (audioPlayer as ExoPlayer).simpleExoPlayer.next()
                BaseActivity.setTouchData()
            }
            ACTION_PAUSE_PREVIOUS -> {
                //playPosition = playPosition - 1
                (audioPlayer as ExoPlayer).simpleExoPlayer.previous()
                BaseActivity.setTouchData()
            }
            PlaybackControls.SHUFFLE_OFF.name -> {

                //audioPlayer.setShuffleMode(this, false)
            }

            PlaybackControls.SHUFFLE_ON.name -> {

                //audioPlayer.setShuffleMode(this, true)
            }
            PlaybackControls.RE_ORDER.name -> {

                val tracksList: ArrayList<Track> = ArrayList()

                @Suppress("UNCHECKED_CAST")
                tracksList.addAll(intent.getSerializableExtra(Constant.TRACKS_LIST) as ArrayList<Track>)
                setPlayerNotification(ArrayList(), 0, (audioPlayer as ExoPlayer).simpleExoPlayer)
                audioPlayer.updateTracks(this, tracksList)
            }
            PlaybackControls.ADD_PRE_CACHED_URL.name,
            PlaybackControls.ADD_TO_QUEUE.name,
            PlaybackControls.PLAY_NEXT.name -> {

                val playbackStarted =
                    (audioPlayer as ExoPlayer).nowPlayingQueue.nowPlayingTracksList.isNotEmpty()

                Log.d("NotificationManager", "onStartCommand playbackStarted called playbackStarted:${playbackStarted}")

                if (playbackStarted) {

                    if (intent.action == PlaybackControls.ADD_TO_QUEUE.name) {
                        val isQueueTrackList =
                            intent.getBooleanExtra(Constant.IS_QUEUE_TRACK_LIST, false)
                        val showAddToQueueToast = intent.getBooleanExtra(Constant.showAddToQueueToast, true)
                        if (isQueueTrackList){
                            val trackList = intent.getSerializableExtra(Constant.TRACKS_LIST) as ArrayList<Track>
                            audioPlayer.addTrackToQueue(this, trackList)
                        }else{
                            val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
                            audioPlayer.addTrackToQueue(this, track)
                        }
                        setPlayerNotification(ArrayList(), 0, (audioPlayer as ExoPlayer).simpleExoPlayer)
                        if (showAddToQueueToast){
                            val messageModel = MessageModel(getString(R.string.toast_str_39), getString(R.string.toast_str_40),
                                MessageType.NEUTRAL, true)
                            CommonUtils.showToast(this, messageModel)
                        }
                    }else {
                        val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
                        audioPlayer.playNext(this, track)
                        setPlayerNotification(ArrayList(), 0, (audioPlayer as ExoPlayer).simpleExoPlayer)
                        val messageModel = MessageModel(getString(R.string.toast_str_44), getString(R.string.toast_str_44),
                            MessageType.NEUTRAL, true)
                        CommonUtils.showToast(this, messageModel)
                    }

                } else {
                    Log.d("NotificationManager", "onStartCommand playback is not initiated${playbackStarted}")
                    //playback is not initiated
                    var tracksList: ArrayList<Track> = ArrayList()

                    val isQueueTrackList =
                        intent.getBooleanExtra(Constant.IS_QUEUE_TRACK_LIST, false)
                    if (isQueueTrackList){
                        val trackList = intent.getSerializableExtra(Constant.TRACKS_LIST) as ArrayList<Track>
                        tracksList = trackList
                    } else {
                        val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
                        @Suppress("UNCHECKED_CAST")
                        tracksList.add(track)
                    }
                    setPlayerNotification(tracksList, 0, (audioPlayer as ExoPlayer).simpleExoPlayer)
                    audioPlayer.playTracks(this, tracksList, 0)
                }
            }
        }
        return START_STICKY
    }

    override fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean) {
        setLog("NotificationManager", "isForegroundService-$isForegroundService ")
        /*if(!isForegroundService){
            *//*ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, this.javaClass)
            )*//*
            setLog("NotificationManager", "onPlayerStateChanged-4 ")
            startForeground(notificationId, notification)
            isForegroundService = true
            if (!(audioPlayer as ExoPlayer).simpleExoPlayer.playWhenReady){
                setLog("NotificationManager", "onPlayerStateChanged-5 ")
                stopForeground(true)
                audioPlayer.hideNotification()
                isForegroundService = false
            }
        }*/
//        if(ongoing && !isForegroundService){
//
//            ContextCompat.startForegroundService(
//            applicationContext,
//            Intent(applicationContext, this.javaClass)
//            )
//            setLog("NotificationManager", "onPlayerStateChanged-4 ")
//            startForeground(notificationId, notification)
//            isForegroundService = true
//        }else if (!ongoing){
//            stopForeground(false)
//            isForegroundService = false
//        }
//
//
//        if (isForegroundService && isStopAudioPlayer){
//            setLog("NotificationManager", "onPlayerStateChanged-14")
//            stopPlayerAndNotification()
//        }


        startForeground(notificationId, notification)
    }

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        // removed call to stopSelf() here. This was killing the service when app is restarted
        // again to play some other track and pressing back
        //sendBroadcastWidget()
        stopForeground(true)
        isForegroundService = false
    }

    private fun sendBroadcastWidget(){
        for (clazz in Classes.widgets) {
            val ids = applicationContext.getAppWidgetsIdsFor(clazz)

            val intent = Intent(applicationContext, clazz).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(PLAYPOSITION, playPosition)
                putExtra(WIDGET_NO_PLAYING_EXTRA, "")
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }

            applicationContext.sendBroadcast(intent)
        }
    }
    private fun updateAllWidget(playWhenReady: Boolean, position:Int){
        for (clazz in Classes.widgets) {
            val ids = applicationContext.getAppWidgetsIdsFor(clazz)
            val serviceBundle = Bundle()
            serviceBundle.putSerializable(ITEM_KEY, mItem)
            val intent = Intent(applicationContext, clazz).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(BUNDLE_KEY, serviceBundle)
                putExtra(WIDGET_PLAYING_EXTRA, playWhenReady)
                putExtra(PLAYPOSITION, position)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }

            applicationContext.sendBroadcast(intent)
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        setLog("AudioPlayerService","AudioPlayerService onTaskRemoved called")
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        mediaSession?.let {
            setLog(TAG, "onGetSession mediaSession:${mediaSession} packageName:${controllerInfo.packageName}")
        }


        return if ("android.media.session.MediaController" == controllerInfo.packageName) {
            setLog(TAG, "onGetSession mediaSession: called final")
            mediaSession
        } else null
    }

    var isForegroundService = false
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//        updateAllWidget(playWhenReady, playPosition)
        when (playbackState) {
//            Player.STATE_IDLE, Player.STATE_ENDED -> {
//                setLog("NotificationManager", "onPlayerStateChanged-7 ")
//                stopForeground(true)
//                hidePlayerNotification()
//                isForegroundService = false
//            }
            Player.STATE_BUFFERING,
            Player.STATE_READY -> {
                showPlayerNotification()
                setLog("NotificationManager", "onPlayerStateChanged-1 ")
                if (playbackState == Player.STATE_READY) {
                    setLog("NotificationManager", "onPlayerStateChanged-2 - isFirstLaunchSong-${HungamaMusicApp.getInstance().getIsFirstLaunchSong()}")
                    if (!playWhenReady) {
                        setLog("NotificationManager", "onPlayerStateChanged-3")
                        // If playback is paused we remove the foreground state which allows the
                        // notification to be dismissed. An alternative would be to provide a
                        // "close" button in the notification which stops playback and clears
                        // the notification.

                        if (HungamaMusicApp.getInstance().getIsFirstLaunchSong()) {
                            setLog("NotificationManager", "onPlayerStateChanged-5 ")
                            hidePlayerNotification()
                            isForegroundService = false
                        } else {
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }else{
                        HungamaMusicApp.getInstance().setIsFirstLaunchSong(false)
                    }
                }
            }
            else -> {
                setLog("NotificationManager", "onPlayerStateChanged-6 ")
                //audioPlayer.hideNotification()
            }
        }
    }


    override fun onPositionDiscontinuity(reason: Int) {
        updateNowPlaying()
    }

    private fun updateNowPlaying() {
        if ((audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag != null) {
            //val nowPlayingInfo: NowPlayingInfo = (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
            //(audioPlayer as ExoPlayer).simpleExoPlayer.setWakeMode(C.WAKE_MODE_NETWORK)
            (audioPlayer as ExoPlayer).nowPlayingQueue.apply {
                //currentPlayingTrackId = nowPlayingInfo.id
                if (BaseActivity.nowPlayingCurrentTrack() != null){
                    currentPlayingTrackId = BaseActivity.nowPlayingCurrentTrack()?.id!!
                }
                playPosition = currentPlayingTrackIndex
                currentPlayingTrackIndex = BaseActivity.nowPlayingCurrentIndex()
                /*nowPlayingTracksList.forEachIndexed { index, track ->
                    if (track.id == currentPlayingTrackId) {
                        setLog("PlayNextSong", "AudioPlayerService-currentPlayingTrackIndex-$index")
                        currentPlayingTrackIndex = index
                        return@forEachIndexed
                    }
                }*/
            }
        }

    }

    override fun onDestroy() {
        hidePlayerNotification()
        if (audioPlayer !=null)
        {
            audioPlayer.cleanup()
        }
        //sendBroadcastWidget()
        if (mediaSession != null){
            //mediaSession?.release()
            mediaSession?.run {
                player.release()
                release()
                mediaSession = null
            }
        }
        super.onDestroy()
        setLog("NotificationManager", "onPlayerStateChanged-8 ")

    }


    /*override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }*/

    fun onUnbindService(){
//        stopForeground(true)
//        audioPlayer.notificationCleanup()
        hidePlayerNotification()
    }

    enum class PlaybackControls {
        PLAY,
        PAUSE,
        SHUFFLE_ON,
        SHUFFLE_OFF,
        RE_ORDER,
        ADD_TO_QUEUE,
        PLAY_NEXT,
        ADD_PRE_CACHED_URL
    }

    fun Context.getAppWidgetsIdsFor(clazz: Class<*>): IntArray {
        return AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, clazz))
    }

    object Classes {

        const val WIDGET_ONE = "com.hungama.music.player.audioplayer.widget.PlayerWidget"
        const val WIDGET_TWO = "com.hungama.music.player.audioplayer.widget.PlayerWidget2"
        const val WIDGET_THREE = "com.hungama.music.player.audioplayer.widget.PlayerWidget3"

        @JvmStatic
        val widgets: List<Class<*>> by lazy {
            listOf(
                Class.forName(WIDGET_ONE),
                Class.forName(WIDGET_TWO),
                Class.forName(WIDGET_THREE)
            )
        }

    }

    fun getAudioPlayerInstance(): AudioPlayer {
        return audioPlayer
    }

    fun setAudioPlayerInstance(simpleExoplayer: SimpleExoPlayer?) {
        if (simpleExoplayer != null) {
            (audioPlayer as ExoPlayer).simpleExoPlayer = simpleExoplayer
        }
    }

    fun initializeAds(context: Context?, companionView: ViewGroup?) {
        val sdkFactory = ImaSdkFactory.getInstance()
        val container =
            ImaSdkFactory.createAudioAdDisplayContainer(context, imaService?.imaVideoAdPlayer)
        val companionAdSlot = sdkFactory.createCompanionAdSlot()
        companionAdSlot.container = companionView
        companionAdSlot.setSize(300, 250)
        container.setCompanionSlots(ImmutableList.of(companionAdSlot))
        imaService?.let {
            imaService?.init(container)
        }


    }


    fun requestAd(adTagUrl: String?) {
        imaService?.let {
            imaService?.requestAds(adTagUrl)
        }

    }

    /*fun setReInitializeInstances(exoPlayer:AudioPlayer): AudioPlayer {
        exoPlayer.apply {
            init(this@AudioPlayerService)
            setNotificationPostedListener(this@AudioPlayerService)
            setPlayerEventListener(this@AudioPlayerService)
        }
        return exoPlayer
    }
    fun playNextSong(selectedTrackPosition:Int,selectedTrackPlayStratPosition:Long, shuffle:Boolean, isQueueItem:Boolean, exoPlayer:AudioPlayer){
        var selectedTrackPosition1 = selectedTrackPosition
        CoroutineScope(Dispatchers.IO).launch {
            var tracksList = mutableListOf<Track>()
            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
            val trackListDataModel = tracksRepository.getAllQueuedTracks(applicationContext, selectedTrackPosition1, queueManager as NowPlayingQueue)
            selectedTrackPosition1 = trackListDataModel.selectedTrackIndex
            tracksList = trackListDataModel.trackListData
            mItem = tracksList as ArrayList<Track>
            exoPlayer.playTracks(applicationContext, tracksList, selectedTrackPosition1, shuffle, selectedTrackPlayStratPosition, isQueueItem,false)
        }
    }*/

    fun stopAudiPlayerService(){
        try {
            audioPlayer?.let {
                if (!isForegroundService){
                    setLog("NotificationManager", "onPlayerStateChanged-12")
                    (audioPlayer as ExoPlayer).simpleExoPlayer.pause()
                    isStopAudioPlayer = true
                }else{
                    setLog("NotificationManager", "onPlayerStateChanged-13")
                    stopPlayerAndNotification()
                }
            }
        }catch (e:Exception){

        }

    }

    fun stopPlayerAndNotification(){
        audioPlayer?.let {
            setLog("NotificationManager", "onPlayerStateChanged-15")
            (audioPlayer as ExoPlayer).simpleExoPlayer.stop()
            onUnbindService()
            //isForegroundService = false
            isStopAudioPlayer = false
        }

    }

    fun setBaseActivityInstance(baseActivity: BaseActivity){
        this.baseActivity = baseActivity
    }

    private fun callStream(queueManager:QueueManager, position: Int){
        if (queueManager != null && !queueManager.getNowPlayingTracks().isNullOrEmpty() && position > -1 && queueManager.getNowPlayingTracks().size > position){
            setLog(TAG, "onStartCommand: title:${queueManager.getNowPlayingTracks().get(position)?.title}")

            if (baseActivity != null){
                val track = queueManager.getNowPlayingTracks().get(position)
                setLog(TAG, "onStartCommand: baseActivity:${baseActivity?.javaClass?.simpleName}")
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        setLog(
                            "callUserStreamUpdate1",
                            "callUserStreamUpdate-callStream-position==-:${position} track.title: ${track.title} lastSongPlayDuration:${BaseActivity.lastSongPlayDuration}"
                        )
                        baseActivity?.callUserStreamUpdate(-1, track, position)
                    }
                }

            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        setLog("NotificationManager", "onPlayerStateChanged-16-error.errorCode-${error.errorCode}")
        /*if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
            || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
            setLog("NotificationManager", "onPlayerStateChanged-17 isForegroundService:${isForegroundService}")
            if(isForegroundService){
                stopForeground(true)
                hidePlayerNotification()
                isForegroundService = false
            }
        }*/
    }

    fun hidePlayerNotification(isReleasePlayer:Boolean=false){
        if (playerNotificationManager != null){
            playerNotificationManager?.setPlayer(null)
        }
        setLog("On Destory","audioPlayerService hidePlayerNotification called isReleasePlayer:${isReleasePlayer} audioPlayer:${audioPlayer}")
        if(isReleasePlayer && audioPlayer!=null){
            (audioPlayer as ExoPlayer).simpleExoPlayer?.release()
        }



    }

    fun showPlayerNotification(){
        //audioPlayer.showNotification((audioPlayer as ExoPlayer).simpleExoPlayer)
        if (playerNotificationManager != null){
            playerNotificationManager?.setPlayer((audioPlayer as ExoPlayer).simpleExoPlayer)
        }


    }

    private fun setPlayerNotification(
        tracksList2: MutableList<Track>,
        selectedTrackPosition: Int,
        mPlayer: SimpleExoPlayer
    ) {
        CoroutineScope(Dispatchers.Main).launch{
            try {


                mPlayerNotificationManager = NotificationManagerCompat.from(this@AudioPlayerService)

                if (shouldCreateNowPlayingChannel(mPlayerNotificationManager)) {
                    //createNowPlayingChannel(mPlayerNotificationManager)
                }

                //setLog("NotificationManager", "setPlayerNotification-900")
                mediaSession.let {
                    //setLog("NotificationManager", "setPlayerNotification-901")
                    playerNotificationManager = PlayerNotificationManager.Builder(this@AudioPlayerService,
                        PLAYBACK_NOTIFICATION_ID,
                        Constant.PLAYBACK_CHANNEL_ID,
                        object : PlayerNotificationManager.MediaDescriptionAdapter {
                            /*val controller = MediaControllerCompat(this@AudioPlayerService,
                                mediaSession!!
                            )*/

                            override fun getCurrentContentText(player: Player): String? {
                                var subTitle = "Buffering..."
                                if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()) {
                                    subTitle =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.subTitle
                                            ?: "Buffering..."
                                }
                                return subTitle
                            }

                            override fun getCurrentContentTitle(player: Player): String {
                                var title = "Buffering..."
                                if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()) {
                                    title =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.title
                                            ?: "Buffering..."
                                }
                                return title
                            }

                            override fun getCurrentLargeIcon(
                                player: Player,
                                callback: PlayerNotificationManager.BitmapCallback
                            ): Bitmap? {
                                //return controller.metadata?.description?.iconBitmap
                                var image = ""
                                if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()) {
                                    image =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.image.toString()
                                }
                                GlobalScope.launch(Dispatchers.IO) {
                                    loadAlbumArtUri(
                                        applicationContext,
                                        image,
                                        object : OnBitmapLoadedListener {
                                            override fun onBitmapLoaded(resource: Bitmap) {
                                                callback.onBitmap(resource)
                                            }

                                            override fun onBitmapLoadingFailed() {
                                                try {
                                                    callback.onBitmap(
                                                        AppCompatResources.getDrawable(
                                                            applicationContext,
                                                            R.drawable.placeholder_png
                                                        )!!.toBitmap()
                                                    )
                                                } catch (e: Exception) {

                                                }

                                            }

                                        })
                                }

                                return BitmapFactory.decodeResource(
                                    applicationContext.resources,
                                    R.drawable.placeholder_png
                                )
                            }

                            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    return PendingIntent.getActivity(
                                        this@AudioPlayerService,
                                        0,
                                        Intent(this@AudioPlayerService, MainActivity::class.java),
                                        PendingIntent.FLAG_IMMUTABLE
                                    )
                                }else{
                                    return PendingIntent.getActivity(
                                        this@AudioPlayerService,
                                        0,
                                        Intent(this@AudioPlayerService, MainActivity::class.java),
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                }

                            }

                        })
                        .setChannelNameResourceId(R.string.default_notification_channel_id)
                        .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                            override fun onNotificationPosted(
                                notificationId: Int,
                                notification: Notification,
                                ongoing: Boolean
                            ) {
                                setLog(
                                    "NotificationManager",
                                    "onNotificationPosted ongoing:${ongoing} isForegroundService:${isForegroundService} currentChannel?.id:${currentChannel?.id}"
                                )

                                /*val eventModel = HungamaMusicApp.getInstance().getEventData("" + currentChannel?.id)
                                setLog(
                                    TAG,
                                    "onNotificationPosted : eventModel :${eventModel}"
                                )*/
                                Log.d(TAG, "onNotificationPosted: called notificationId:${notificationId} ")
                                if (ongoing && !isForegroundService) {

                                    ContextCompat.startForegroundService(
                                        applicationContext,
                                        Intent(
                                            applicationContext,
                                            this@AudioPlayerService.javaClass
                                        )
                                    )

                                    setLog(
                                        "NotificationManager",
                                        "startForegroundService Inner Player notification ongoing:${ongoing} isForegroundService:${isForegroundService} notificationId:${notificationId}"
                                    )
                                    startForeground(PLAYBACK_NOTIFICATION_ID, notification)
                                    isForegroundService = true
                                }
                                /*setLog(
                                    "NotificationManager",
                                    "Inner Player notification ongoing:${ongoing} isForegroundService:${isForegroundService} notificationId:${notificationId}"
                                )*/
                            }

                            override fun onNotificationCancelled(
                                notificationId: Int,
                                dismissedByUser: Boolean
                            ) {
                                stopForeground(true)
                                isForegroundService = false
                            }
                        }).setCustomActionReceiver(object : PlayerNotificationManager.CustomActionReceiver{
                            override fun createCustomActions(
                                context: Context,
                                instanceId: Int
                            ): MutableMap<String, NotificationCompat.Action> {
                                val actions: MutableMap<String, NotificationCompat.Action> = HashMap()
                                actions[Constant.CUSTOM_ACTION_PLAY] =
                                    NotificationCompat.Action(
                                        androidx.media3.ui.R.drawable.exo_notification_play,
                                        context.getString(androidx.media3.ui.R.string.exo_controls_play_description),
                                        createBroadcastIntent(
                                            Constant.CUSTOM_ACTION_PLAY,
                                            context,
                                            instanceId
                                        )
                                    )
                                actions[Constant.CUSTOM_ACTION_PAUSE] =
                                    NotificationCompat.Action(
                                        androidx.media3.ui.R.drawable.exo_notification_pause,
                                        context.getString(androidx.media3.ui.R.string.exo_controls_pause_description),
                                        createBroadcastIntent(
                                            Constant.CUSTOM_ACTION_PAUSE,
                                            context,
                                            instanceId
                                        )
                                    )
                                actions[Constant.CUSTOM_ACTION_PREVIOUS] =
                                    NotificationCompat.Action(
                                        androidx.media3.ui.R.drawable.exo_notification_previous,
                                        context.getString(androidx.media3.ui.R.string.exo_controls_previous_description),
                                        createBroadcastIntent(
                                            Constant.CUSTOM_ACTION_PREVIOUS, context, instanceId
                                        )
                                    )
                                actions[Constant.CUSTOM_ACTION_NEXT] =
                                    NotificationCompat.Action(
                                        androidx.media3.ui.R.drawable.exo_notification_next,
                                        context.getString(androidx.media3.ui.R.string.exo_controls_next_description),
                                        createBroadcastIntent(
                                            Constant.CUSTOM_ACTION_NEXT,
                                            context,
                                            instanceId
                                        )
                                    )
                                return actions
                            }

                            override fun getCustomActions(player: Player): MutableList<String> {
                                val enablePrevious =
                                    player.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)
                                val enableNext =
                                    player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)
                                val stringActions: MutableList<String> = ArrayList()

                                if (enablePrevious) {
                                    stringActions.add(Constant.CUSTOM_ACTION_PREVIOUS)
                                }
                                if (shouldShowPauseButton(player)) {
                                    stringActions.add(Constant.CUSTOM_ACTION_PAUSE)
                                } else {
                                    stringActions.add(Constant.CUSTOM_ACTION_PLAY)
                                }
                                if (enableNext) {
                                    stringActions.add(Constant.CUSTOM_ACTION_NEXT)
                                }

                                return stringActions
                            }

                            override fun onCustomAction(
                                player: Player,
                                action: String,
                                intent: Intent
                            ) {
                                onNotificationButtonClickAction(this@AudioPlayerService, action)
                            }

                        }).build()


                    //setLog("NotificationManager", "setPlayerNotification-902")
                    playerNotificationManager?.setPriority(NotificationCompat.PRIORITY_LOW)
                    setLog("PlayerNotificationInner", "player-$mPlayer")
                    //playerNotificationManager?.setPlayer(mPlayer)
                    playerNotificationManager?.apply {
                        setPlayer(mPlayer)
                        //DefaultControlDispatcher(0,0)
                        setUseFastForwardAction(false)
                        setUseRewindAction(false)
                        setUseStopAction(false)
                        setUsePlayPauseActions(false)
                        setUsePreviousAction(false)
                        setUseNextAction(false)
                    }

                    /*playerNotificationManager?.setControlDispatcher(object : ControlDispatcher {
                        override fun dispatchPrepare(player: Player): Boolean {
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchPrepare-10001 "
                            )*//*
                            //player.prepare()
                            return true
                        }

                        override fun dispatchSetPlayWhenReady(player: Player, playWhenReady: Boolean): Boolean {
                            player.playWhenReady = playWhenReady
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchSetPlayWhenReady-10002 "
                            )*//*
                            return true
                        }

                        override fun dispatchSeekTo(
                            player: Player,
                            windowIndex: Int,
                            positionMs: Long
                        ): Boolean {
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchSeekTo-10003 "
                            )*//*
                            player.seekTo(windowIndex, positionMs)
                            return true
                        }

                        override fun dispatchPrevious(player: Player): Boolean {
                            setLog("NotificationPP", "Previous")
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchPrevious-10004 "
                            )*//*
                            onNotificationButtonClickAction(this@AudioPlayerService, false)
                            return true
                        }

                        override fun dispatchNext(player: Player): Boolean {
                            setLog("NotificationPP", "Next")
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchNext-10005 "
                            )*//*
                            onNotificationButtonClickAction(this@AudioPlayerService, true)
                            return true
                        }

                        override fun dispatchRewind(player: Player): Boolean {
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchRewind-10006 "
                            )*//*
                            return true
                        }

                        override fun dispatchFastForward(player: Player): Boolean {
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchFastForward-10007 "
                            )*//*
                            return true
                        }

                        override fun dispatchSetRepeatMode(player: Player, repeatMode: Int): Boolean {
                            //player.setRepeatMode(repeatMode)
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchSetRepeatMode-10008 "
                            )*//*
                            return true
                        }

                        override fun dispatchSetShuffleModeEnabled(
                            player: Player,
                            shuffleModeEnabled: Boolean
                        ): Boolean {
                            //player.setShuffleModeEnabled(shuffleModeEnabled)
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchSetShuffleModeEnabled-10009 "
                            )*//*
                            return true
                        }

                        override fun dispatchStop(player: Player, reset: Boolean): Boolean {
                            //player.stop(reset)
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchStop-10010 "
                            )*//*
                            return true
                        }

                        override fun dispatchSetPlaybackParameters(
                            player: Player,
                            playbackParameters: PlaybackParameters
                        ): Boolean {
                            //player.setPlaybackParameters(playbackParameters)
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "dispatchSetPlaybackParameters-10011 "
                            )*//*
                            return true
                        }

                        override fun isRewindEnabled(): Boolean {
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "isRewindEnabled-10012 "
                            )*//*
                            return false
                        }

                        override fun isFastForwardEnabled(): Boolean {
                            *//*CommonUtils.setLog(
                                "NotificationManager",
                                "isFastForwardEnabled-10013 "
                            )*//*
                            return false
                        }

                    })*/
                    playerNotificationManager?.setMediaSessionToken(mediaSession?.sessionCompatToken as MediaSessionCompat.Token)
                    //setLog("NotificationManager", "setPlayerNotification-903")

                }



            }catch (e:Exception){
                setLog("NotificationManager", "Error-${e.message}")
                if (BuildConfig.DEBUG){
                    throw e
                }
            }
        }

    }
    private fun shouldCreateNowPlayingChannel(notificationManager: NotificationManagerCompat) =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists(
            notificationManager
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists(notificationManager: NotificationManagerCompat) =
        notificationManager.getNotificationChannel(Constant.PLAYBACK_CHANNEL_ID) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel(notificationManager: NotificationManagerCompat) {
        val notificationChannel = NotificationChannel(
            Constant.PLAYBACK_CHANNEL_ID,
            Constant.PLAYBACK_CHANNEL_NAME,
            android.app.NotificationManager.IMPORTANCE_LOW
        )
            .apply {
                description = Constant.PLAYBACK_CHANNEL_NAME
            }
        notificationChannel?.setSound(null,null)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun onNotificationButtonClickAction(context: Context, action: String){
        val intent = Intent(Constant.NOTIFICATION_PLAYER_EVENT)
        intent.putExtra("EVENT", Constant.NOTIFICATION_RESULT_CODE)
        intent.putExtra("action", action)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        //setLog("onCustomAction", "Service-action-$action")
    }

    fun setPreCachedContent(track: Track){
        if (isForegroundService){
            setLog("preCatchContent", "AudioPlayerService-onStartCommand-PlaybackControls.ADD_PRE_CACHED_URL.name-track.url-${track.url}")
            audioPlayer.updateTrackData(this, track)


        }

    }

    fun updateShuffleMode(shuffleMode: Boolean){
        setLog("updateShuffleMode", "AudioPlayerService-onStartCommand-shuffleMode-${shuffleMode}")
        audioPlayer.setShuffleMode(this, shuffleMode)
    }

    private fun createBroadcastIntent(
        action: String, context: Context, instanceId: Int
    ): PendingIntent? {
        val intent = Intent(action).setPackage(context.packageName)
        intent.putExtra(PlayerNotificationManager.EXTRA_INSTANCE_ID, instanceId)
        val pendingFlags: Int
        pendingFlags = if (Util.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, instanceId, intent, pendingFlags)
    }

    private fun shouldShowPauseButton(player: Player): Boolean {
        return player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE && player.playWhenReady
    }


    private inner class MusicServiceCallback: MediaLibrarySession.Callback {

        /*val REWIND_30 = "123"
        val FAST_FWD_30 = "124"
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands =
                connectionResult.availableSessionCommands
                    .buildUpon()
                    // Add custom commands
                    .add(SessionCommand(REWIND_30, Bundle()))
                    .add(SessionCommand(FAST_FWD_30, Bundle()))
                    .build()
            return MediaSession.ConnectionResult.accept(
                sessionCommands, connectionResult.availablePlayerCommands)
        }*/

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return super.onConnect(session, controller)
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(TAG, "MusicServiceCallback onGetLibraryRoot called")

            val rootExtras = Bundle().apply {
                putBoolean(CONTENT_STYLE_SUPPORTED, true)
                putBoolean(MEDIA_SEARCH_SUPPORTED, true)
                putInt(
                    CONTENT_STYLE_BROWSABLE_HINT,
                    CONTENT_STYLE_GRID_ITEM_HINT_VALUE
                )
                putInt(
                    CONTENT_STYLE_PLAYABLE_HINT,
                    CONTENT_STYLE_LIST_ITEM_HINT_VALUE
                )
                putInt(
                    CONTENT_STYLE_GROUP_TITLE_HINT,
                    CONTENT_STYLE_CATEGORY_LIST_ITEM_VALUE
                )
                putInt(
                    CONTENT_STYLE_SINGLE_ITEM_HINT,
                    CONTENT_STYLE_CATEGORY_LIST_ITEM_VALUE
                )
            }
            val libraryParams = LibraryParams.Builder().setExtras(rootExtras).build()
            val rootMediaItem = catalogueRootMediaItem

            return Futures.immediateFuture(LibraryResult.ofItem(rootMediaItem, libraryParams))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentMediaId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {

            Log.d(TAG, "MusicServiceCallback onGetChildren: parentMediaId:${parentMediaId}")
            MainActivity.lastBottomItemPosClicked=0
            HungamaChannelHelper.updateUserID()
            if(!parentMediaId.equals(HUNGAMA_ROOT,true)){
                setLog("API_DEVICE_TYPE","Constant.API_DEVICE_TYPE-before-${Constant.API_DEVICE_TYPE}")
                Constant.API_DEVICE_TYPE="carPlay"
                setLog("API_DEVICE_TYPE","Constant.API_DEVICE_TYPE-after-${Constant.API_DEVICE_TYPE}")
            }

            if(parentMediaId.equals(HUNGAMA_ROOT,true)){
                Log.d(TAG, "MusicServiceCallback onGetChildren: HUNGAMA_ROOT parentMediaId:${parentMediaId}")
                return HungamaChannelHelper.createBrowsableListing(this@AudioPlayerService)!!


            }else if (parentMediaId.equals(MEDIA_ID_RECENT,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_RECENT parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getUserRecentPlay(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            }else if (parentMediaId.equals(MEDIA_ID_PODCAST,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_PODCAST parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getPodcastBucketList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if(parentMediaId.equals(MEDIA_ID_DISCOVER,true)){
                Log.d(TAG, "MusicServiceCallback onGetChildren: MEDIA_ID_DISCOVER parentMediaId:${parentMediaId}")
                return HungamaChannelHelper.getDiscoverBucketList(this@AudioPlayerService,parentMediaId,)!!

            }else if (parentMediaId.contains(HUNGAMA_PODCAST_DETAIL_ROOT, true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: HUNGAMA_PODCAST_DETAIL_ROOT parentMediaId:${parentMediaId}")
                return HungamaChannelHelper.getPodcastDetailMain(this@AudioPlayerService!!,parentMediaId)
            } else if (parentMediaId.contains(HUNGAMA_DISCOVER_DETAIL_ROOT, true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: HUNGAMA_DISCOVER_DETAIL_ROOT parentMediaId:${parentMediaId}")
                return HungamaChannelHelper.getDiscoverDetail(this@AudioPlayerService!!,parentMediaId)
            } else if(parentMediaId?.contains(HUNGAMA_PODCAST_ROOT, true) == true || parentMediaId.contains(HUNGAMA_DISCOVER_ROOT,true)){
                Log.d(TAG, "MusicServiceCallback onGetChildren: HUNGAMA_PODCAST_ROOT parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getHeaderWiseListing(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            }else if (parentMediaId.equals(MEDIA_ID_LIBRARY, true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren: MEDIA_ID_LIBRARY parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.createLibraryListing(this@AudioPlayerService)!!,
                        LibraryParams.Builder().build()))
            }else if (parentMediaId.equals(MEDIA_ID_LIB_FAV,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_RECENT parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getFavSongList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if (parentMediaId.equals(MEDIA_ID_LIB_PLAYLIST,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_LIB_PLAYLIST parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getFavPlaylistList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if (parentMediaId.equals(MEDIA_ID_LIB_ALBUM,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_LIB_ALBUM parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getFavAlbumList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if (parentMediaId.equals(MEDIA_ID_LIB_PODCAST,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_RECENT parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getFavPodcastList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if (parentMediaId.equals(MEDIA_ID_LIB_ARTIST,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_RECENT parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getFavArtistList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if (parentMediaId.equals(MEDIA_ID_LIB_RADIO,true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: MEDIA_ID_RECENT parentMediaId:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        HungamaChannelHelper.getRadioList(this@AudioPlayerService,parentMediaId,)!!,
                        LibraryParams.Builder().build()))
            } else if (parentMediaId.contains(HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT, true)) {
                Log.d(TAG, "MusicServiceCallback onGetChildren 1: HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT parentMediaId:${parentMediaId}")
                return HungamaChannelHelper.getPlaylistDetailMain(this@AudioPlayerService!!,parentMediaId)
            }else if (parentMediaId.contains(HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT, true)) {

//                var updateID = HungamaChannelHelper.findRealID(parentMediaId)
//                var tmpChannel = Channel(updateID,"","","","1","","","",0)
//                discoverBucketWiseChannelList?.add(tmpChannel)
//                Log.d(TAG, "MusicServiceCallback onGetChildren 1: HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT parentMediaId:${parentMediaId}")
//
//                return HungamaChannelHelper.getDiscoverDetail(this@AudioPlayerService!!,parentMediaId)
                return HungamaChannelHelper.getAlbumDetailMain(this@AudioPlayerService!!,parentMediaId)
            }else if (parentMediaId.contains(HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT, true)) {

                Log.d("TAG", "MusicServiceCallback HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT called:${parentMediaId}")

                return HungamaChannelHelper.getArtistDetailMain(this@AudioPlayerService!!,parentMediaId)

            }
//            else if (parentMediaId?.contains(HUNGAMA_CATEGORY_DETAIL_ROOT, true)) {
//
//                Log.d("TAG", "onLoadChildren HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT called")
//                return Futures.immediateFuture(
//                    LibraryResult.ofItemList(
//                        ImmutableList.of(),
//                        LibraryParams.Builder().build()))
//
//            }
            else{
                Log.d(TAG, "MusicServiceCallback onGetChildren: emoty display:${parentMediaId}")
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        ImmutableList.of(),
                        LibraryParams.Builder().build()))
            }
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(TAG, "MusicServiceCallback onGetItem: mediaId:${mediaId}")
            val playlistListing = mutableListOf<MediaItem>()

            mediaId.let {
                val channel=HungamaChannelHelper.getChannelForId(mediaId)
                setLog(TAG, "MusicServiceCallback onAddMediaItems: mediaId:${mediaId} channel:${channel?.toString()}")
                val mMediaItem = MediaItem.Builder()
                    .setMediaId(channel.mediaId)
                    .setUri(channel.mediaURL)
                    .setRequestMetadata(MediaItem.RequestMetadata.Builder()
                        .setMediaUri(Uri.parse(channel?.mediaURL))
                        .build())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(channel.title)
                            .setDisplayTitle(channel?.title)
                            .setDescription(channel.description)
                            .setSubtitle(channel.subtitle)
                            .setArtist(channel.subtitle)
                            .setAlbumArtist(channel.subtitle)
                            .setArtworkUri(Uri.parse(channel.imageRes))
                            .setIsPlayable(true)
                            .build()
                    ).build()

                playlistListing.add(mMediaItem)
                setLog(TAG, "MusicServiceCallback onAddMediaItems: mediaId:${mediaId} mMediaItem:${mMediaItem?.toString()} channel:${channel?.toString()} playListListing size:${playlistListing?.size}")

                return callWhenMusicSourceReady {
                    LibraryResult.ofItem(mMediaItem,
                        LibraryParams.Builder().build())
                }
            }

            return callWhenMusicSourceReady {
                LibraryResult.ofItem(MediaItem.EMPTY,
                    LibraryParams.Builder().build())
            }
        }
        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return callWhenMusicSourceReady {
                Log.d(TAG, "MusicServiceCallback onSearch: query:${query}")
                LibraryResult.ofVoid()
            }
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Log.d(TAG, "MusicServiceCallback onGetSearchResult: query:${query}")
            return super.onGetSearchResult(session, browser, query, page, pageSize, params)
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            //setLog("onPlayerCommandRequest", "MusicServiceCallback onPlayerCommandRequest-playerCommand-${playerCommand} baseActivity:${baseActivity}")
            if (playerCommand == Player.COMMAND_SEEK_TO_NEXT){
                //setLog("onPlayerCommandRequest", "MusicServiceCallback title:${session?.player?.currentMediaItem?.mediaMetadata?.title} nowPlayingCurrentIndex:${BaseActivity.nowPlayingCurrentIndex()}")
                //BaseActivity.updateNowPlayingCurrentIndex(BaseActivity.nowPlayingCurrentIndex()-1)
                baseActivity?.playNextSong(false)
                return SessionResult.RESULT_ERROR_SESSION_SETUP_REQUIRED
            }else if (playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS){
                baseActivity?.playPreviousSong()
                return SessionResult.RESULT_ERROR_SESSION_SETUP_REQUIRED
            }
            //return SessionResult.RESULT_SUCCESS;
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            setLog(TAG, "MusicServiceCallback onAddMediaItems: mediaItems:${mediaItems?.size} first item:${mediaItems?.get(0)}")


            val playListListing = mutableListOf<MediaItem>()

            mediaItems?.let {
                val channel=HungamaChannelHelper.getChannelForId(mediaItems.get(0).mediaId)
                currentChannel=channel

//                val imageUri = AlbumArtContentProvider.mapUri(Uri.parse(channel.imageRes))

                val extras = Bundle()
                extras.putString(ORIGINAL_ARTWORK_URI_KEY, Uri.parse(channel.imageRes)?.toString())

                val mMediaItem = MediaItem.Builder()
                    .setMediaId(channel.mediaId)
                    .setUri(channel.mediaURL)
                    .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(Uri.parse(channel.mediaURL)).build())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(channel.title)
                            .setDisplayTitle(channel?.title)
                            .setArtist(channel?.subtitle)
                            .setDescription(channel.description)
                            .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                            .setSubtitle(channel.subtitle)
                            .setArtworkUri(Uri.parse(channel.imageRes))
                            .setIsPlayable(true)
                            .setExtras(extras)
                            .build()
                    ).build()
                playListListing.add(mMediaItem)
                Log.d(TAG, "MusicServiceCallback onAddMediaItems: " +
                        "title:${mMediaItem?.mediaMetadata?.title}" +
                        "\n albumTitle:${mMediaItem?.mediaMetadata?.albumTitle}" +
                        "\n artworkUri:${mMediaItem?.mediaMetadata?.artworkUri}")

//                setLog(TAG, "MusicServiceCallback onAddMediaItems: mediaId:${mediaItems?.get(0)?.mediaId} mMediaItem:${mMediaItem?.toString()} channel:${channel?.toString()} playListListing size:${playListListing?.size}")

                setPlayableContentListData(channel)
            }

            Constant.API_DEVICE_TYPE="carPlay"

            return super.onAddMediaItems(mediaSession, controller, mutableListOf<MediaItem>())
        }
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.d(TAG, "MusicServiceCallback onCustomCommand:")
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    /**
     * Returns a future that executes the action when the music source is ready. This may be an
     * immediate execution if the music source is ready, or a deferred asynchronous execution if the
     * music source is still loading.
     *
     * @param action The function to be called when the music source is ready.
     */
    private fun <T> callWhenMusicSourceReady(action: () -> T): ListenableFuture<T> {
        val conditionVariable = ConditionVariable()
        return  Futures.immediateFuture(action())
    }

    /** Returns a function that opens the condition variable when called. */
    private fun openWhenReady(conditionVariable: ConditionVariable): (Boolean) -> Unit = {
        val successfullyInitialized = it
        if (!successfullyInitialized) {
            Log.e(TAG, "loading music source failed")
        }
        conditionVariable.open()
    }

    private val catalogueRootMediaItem: MediaItem by lazy {
        MediaItem.Builder()
            .setMediaId(HUNGAMA_ROOT)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                    .setIsPlayable(false)
                    .build())
            .build()
    }

    private var songDataList:ArrayList<Track> = arrayListOf()
    private var selectedTrackPosition=0
    private var findPlaySong=false
    private lateinit var tracksViewModel: TracksContract.Presenter

    fun setPlayableContentListData(currentChannel: Channel) {
        setLog(TAG, "setPlayableDataList: channel:${currentChannel} currentChannelList size ${currentChannelList?.size}")
        songDataList = arrayListOf()
        findPlaySong=false
        currentChannelList.forEachIndexed { index, channel ->

            if(currentChannel?.mediaId==channel?.mediaId){
                findPlaySong=true
            }
            if(findPlaySong){
                if(currentChannel?.parentID==channel?.parentID){
                    val track:Track = Track()
                    if (!TextUtils.isEmpty(channel?.mediaId)){
                        track.id = channel.mediaId.toLong()
                    }else{
                        track.id = 0
                    }
                    if (!TextUtils.isEmpty(channel?.title)){
                        track.title =  channel?.title
                    }else{
                        track.title = ""
                    }

                    if (!TextUtils.isEmpty(channel?.subtitle)){
                        track.subTitle =  channel?.subtitle
                    }else{
                        track.subTitle = ""
                    }

                    if (!TextUtils.isEmpty(channel?.mediaURL)){
                        track.url =  channel?.mediaURL
                    }else{
                        track.url = ""
                    }

                    track.drmlicence = ""

                    track.songLyricsUrl = ""

                    if (!TextUtils.isEmpty(channel?.type)){
                        track.playerType = channel?.type
                    }else{
                        track.playerType = ""
                    }
                    track.contentType = ContentTypes.AUDIO.value
                    track.heading = ""

                    if (!TextUtils.isEmpty(channel?.imageRes)){
                        track.image = channel?.imageRes
                    }else{
                        track.image = ""
                    }

                    songDataList.add(track)
                }
            }

        }

//        songDataList?.forEachIndexed { index, track ->
//            if(currentChannel.mediaId?.equals(""+track?.id)){
//                selectedTrackPosition=index
//                setLog(TAG,"setPlayableDataList selectedTrackPosition:${selectedTrackPosition} currentChannel:${currentChannel}")
//            }
//        }


        setLog(TAG, "setPlayableDataList: channel:${currentChannel} selectedTrackPosition:${selectedTrackPosition} songDataList size:${songDataList?.size}")

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@AudioPlayerService)
        BaseActivity.setTrackListData(songDataList)

        tracksViewModel.prepareTrackPlayback(0)

    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        Log.d("PlaybackPreparer", "startTrackPlayback selectedTrackPosition:${selectedTrackPosition} tracksList:${tracksList?.size} trackPlayStartPosition:${trackPlayStartPosition}")

        val intent = Intent(this@AudioPlayerService, AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        intent.putExtra(Constant.TRACKS_LIST, ArrayList(songDataList))
        Util.startForegroundService(HungamaMusicApp.getInstance().applicationContext, intent)

    }

    override fun getViewActivity(): AppCompatActivity {
        baseActivity?.let {
            return baseActivity!!
        }
        return MainActivity()
    }
}