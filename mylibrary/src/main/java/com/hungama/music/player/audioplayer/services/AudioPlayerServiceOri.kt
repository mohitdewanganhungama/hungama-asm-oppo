//package com.hungama.music.player.audioplayer.services
//
//import android.app.Notification
//import android.appwidget.AppWidgetManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.os.Binder
//import android.os.Bundle
//import android.os.IBinder
//import android.support.v4.media.MediaBrowserCompat
//import android.util.Log
//import android.view.ViewGroup
//import androidx.media.MediaBrowserServiceCompat
//import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
//import com.google.android.exoplayer2.C
//import com.google.android.exoplayer2.Player
//import com.google.android.exoplayer2.SimpleExoPlayer
//import com.google.android.exoplayer2.analytics.AnalyticsListener
//import com.google.android.exoplayer2.source.MediaSource
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
//import com.google.android.exoplayer2.util.Util
//import com.google.common.collect.ImmutableList
//import com.hungama.music.R
//import com.hungama.music.data.model.MessageModel
//import com.hungama.music.data.model.MessageType
//import com.hungama.music.player.audioplayer.Injection
//import com.hungama.music.player.audioplayer.mediautils.NotificationManager
//import com.hungama.music.player.audioplayer.model.Track
//import com.hungama.music.player.audioplayer.player.AudioPlayer
//import com.hungama.music.player.audioplayer.player.ExoPlayer
//import com.hungama.music.player.audioplayer.queue.NowPlayingInfo
//import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
//import com.hungama.music.player.audioplayer.queue.QueueManager
//import com.hungama.music.player.audioplayer.repositories.ITrackRepository
//import com.hungama.music.player.audioplayer.services.ad.ImaService
//
//import com.hungama.music.player.audioplayer.widget.BaseWedget.Companion.PLAYPOSITION
//import com.hungama.music.player.audioplayer.widget.BaseWedget.Companion.WIDGET_NO_PLAYING_EXTRA
//import com.hungama.music.player.audioplayer.widget.BaseWedget.Companion.WIDGET_PLAYING_EXTRA
//import com.hungama.music.ui.base.BaseActivity
//import com.hungama.music.utils.CommonUtils
//import com.hungama.music.utils.CommonUtils.setLog
//import com.hungama.music.utils.CommonUtils.songDataList
//import com.hungama.music.utils.Constant
//import com.hungama.music.utils.Constant.ACTION_PAUSE
//import com.hungama.music.utils.Constant.ACTION_PAUSE_PREVIOUS
//import com.hungama.music.utils.Constant.ACTION_PLAY
//import com.hungama.music.utils.Constant.ACTION_PLAY_NEXT
//import com.hungama.music.utils.Constant.BUNDLE_KEY
//import com.hungama.music.utils.Constant.IS_TRACKS_QUEUEITEM
//import com.hungama.music.utils.Constant.ITEM_KEY
//import com.hungama.music.utils.Constant.PLAY_CONTEXT
//import com.hungama.music.utils.Constant.PLAY_CONTEXT_TYPE
//import com.hungama.music.utils.Constant.SELECTED_TRACK_PLAY_START_POSITION
//import com.hungama.music.utils.Constant.SELECTED_TRACK_POSITION
//import com.hungama.music.utils.Constant.SHUFFLE_TRACKS
//import kotlinx.coroutines.*
//
//class AudioPlayerServiceOri : MediaBrowserServiceCompat(), NotificationManager.OnNotificationPostedListener,
//        Player.Listener {
//    private val TAG: String = AudioPlayerServiceOri::class.java.name
//
//    public lateinit var queueManager: QueueManager
//    public lateinit var audioPlayer: AudioPlayer
//    public lateinit var tracksRepository: ITrackRepository
//    public var mItem: ArrayList<Track>? = null
//    public var playPosition:Int = 0
//    public var imaService: ImaService? = null
//    var isStopAudioPlayer = false
//    var baseActivity:BaseActivity? = null
//    override fun onBind(intent: Intent?): IBinder {
//        setLog("NotificationManager", "onPlayerStateChanged-9 ")
//        return AudioServiceBinder()
//    }
//
//    inner class AudioServiceBinder : Binder() {
//
//        fun getPlayerInstance(): SimpleExoPlayer {
//            return (audioPlayer as ExoPlayer).simpleExoPlayer
//        }
//
//        fun getNowPlayingQueue(): NowPlayingQueue {
//            return (audioPlayer as ExoPlayer).nowPlayingQueue
//        }
//
//        val service:AudioPlayerServiceOri
//            get() = this@AudioPlayerServiceOri
//    }
//
//    private var isAdPlaying = false
//    private var songCurrentPosition = 0L
//    inner class SharedAudioPlayer{
//        fun claim() {
//            isAdPlaying = true
//            songCurrentPosition=(audioPlayer as ExoPlayer).simpleExoPlayer.currentPosition
//            (audioPlayer as ExoPlayer).simpleExoPlayer.setPlayWhenReady(false)
//            val songName = (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.mediaMetadata?.title
//            setLog("ImaAdsService", "AudioPlayerService-claim-songCurrentPosition-$songCurrentPosition  songName-$songName")
//        }
//
//        fun release() {
//            if (isAdPlaying) {
//                isAdPlaying = false
//
//                if((audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()!=null && (audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()?.size!! > BaseActivity.nowPlayingCurrentIndex()){
//                    //setLog("ImaAdsService", "AudioPlayerService-release-getConcatenatingMediaSourceFromPlayer-title-${(audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()?.getMediaSource(BaseActivity.nowPlayingCurrentIndex())?.mediaItem?.mediaMetadata?.title}")
//                    //(audioPlayer as ExoPlayer).simpleExoPlayer.setMediaSource((audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()!!)
//                    (audioPlayer as ExoPlayer).simpleExoPlayer.prepare((audioPlayer as ExoPlayer).getConcatenatingMediaSourceFromPlayer()!!)
//                    (audioPlayer as ExoPlayer).simpleExoPlayer.setPlayWhenReady(true)
//                    (audioPlayer as ExoPlayer).simpleExoPlayer?.seekTo(BaseActivity.nowPlayingCurrentIndex(), songCurrentPosition)
//                    val songName = (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.mediaMetadata?.title
//                    setLog("ImaAdsService", "AudioPlayerService-release-songCurrentPosition-$songCurrentPosition  songName-$songName")
//                    setLog(TAG, "release: ConcatenatingMediaSource is set now")
//                }else{
//                    setLog("ImaAdsService", "AudioPlayerService-release-ConcatenatingMediaSource is null")
//                    setLog(TAG, "release: ConcatenatingMediaSource is null")
//                }
//
//
//                // TODO: Seek to where you left off the stream, if desired.
//            }
//        }
//
//        fun prepare(mediaSource: MediaSource?) {
//            (audioPlayer as ExoPlayer).simpleExoPlayer.setMediaSource(mediaSource!!)
//            (audioPlayer as ExoPlayer).simpleExoPlayer.prepare()
//        }
//
//        fun addAnalyticsListener(listener: AnalyticsListener?) {
//            (audioPlayer as ExoPlayer).simpleExoPlayer.addAnalyticsListener(listener!!)
//        }
//
//        fun getPlayer(): ExoPlayer? {
//            return audioPlayer as ExoPlayer
//        }
//
//        fun onAdsError(){
//
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        setLog("NotificationManager", "onPlayerStateChanged-0 ")
//        isAdPlaying = false
//        audioPlayer = Injection.provideAudioPlayer()
//        tracksRepository = Injection.provideTrackRepository()
//
//        audioPlayer.apply {
//            init(this@AudioPlayerServiceOri)
//            setNotificationPostedListener(this@AudioPlayerServiceOri)
//            setPlayerEventListener(this@AudioPlayerServiceOri)
//            //showNotification((audioPlayer as ExoPlayer).simpleExoPlayer)
//        }
//        val dataSourceFactory = DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name)))
//        imaService = ImaService(applicationContext,dataSourceFactory,SharedAudioPlayer())
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        setLog(TAG, "Received ${intent?.action}")
//
//        if (intent == null || intent.action == null)
//            return START_STICKY
//
//        setLog("NotificationManager", "onPlayerStateChanged-10 ")
//        isStopAudioPlayer = false
//        when (intent.action) {
//            PlaybackControls.PLAY.name -> {
//                setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PLAY.name:")
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    var selectedTrackPosition = intent.getIntExtra(SELECTED_TRACK_POSITION, -1)
//                    //BaseActivity.queueNowPlayIndex=selectedTrackPosition
//                    val selectedTrackPlayStratPosition = intent.getLongExtra(SELECTED_TRACK_PLAY_START_POSITION, 0)
//                    val shuffle = intent.getBooleanExtra(SHUFFLE_TRACKS, false)
//                    val isQueueItem = intent.getBooleanExtra(IS_TRACKS_QUEUEITEM, false)
//
//                    val playContext: PLAY_CONTEXT =
//                            intent.getSerializableExtra(PLAY_CONTEXT_TYPE) as PLAY_CONTEXT
//                    var tracksList = mutableListOf<Track>()
//
//                    when (playContext) {
//
//
//                        PLAY_CONTEXT.LIBRARY_TRACKS -> {
//                            setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PLAY.name-LIBRARY_TRACKS")
//                            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
//                            callStream(queueManager, BaseActivity.nowPlayingCurrentIndex())
//                            val trackListDataModel = tracksRepository.getAllTracks(applicationContext, selectedTrackPosition, queueManager as NowPlayingQueue)
//                            selectedTrackPosition = trackListDataModel.selectedTrackIndex
//                            tracksList = trackListDataModel.trackListData
//                            mItem = tracksList as ArrayList<Track>
//                            playPosition = selectedTrackPosition
//                        }
//
//                        PLAY_CONTEXT.VIDEO_TRACK -> {
//                            tracksList = tracksRepository.getAllTracksVideo(applicationContext)
//                        }
//
//                        PLAY_CONTEXT.QUEUE_TRACKS -> {
//                            setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PLAY.name-QUEUE_TRACKS")
//                            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
//                            callStream(queueManager, BaseActivity.nowPlayingCurrentIndex()-1)
//                            val trackListDataModel = tracksRepository.getAllQueuedTracks(applicationContext, selectedTrackPosition,
//                                queueManager as NowPlayingQueue
//                            )
//                            selectedTrackPosition = trackListDataModel.selectedTrackIndex
//                            tracksList = trackListDataModel.trackListData
//                            mItem = tracksList as ArrayList<Track>
//                        }
//
//                        PLAY_CONTEXT.LOCAL_DEVICE_LIBRARY_TRACKS -> {
//                            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
//                            callStream(queueManager, BaseActivity.nowPlayingCurrentIndex())
//                            val trackListDataModel = tracksRepository.getAllTracks(applicationContext, selectedTrackPosition, queueManager as NowPlayingQueue)
//                            selectedTrackPosition = trackListDataModel.selectedTrackIndex
//                            tracksList = trackListDataModel.trackListData
//                            mItem = tracksList as ArrayList<Track>
//                            playPosition = selectedTrackPosition
//                        }
//                    }
//                        audioPlayer.playTracks(applicationContext, tracksList, selectedTrackPosition, shuffle, selectedTrackPlayStratPosition, isQueueItem,false)
//
//
//
//                }
//            }
//            PlaybackControls.PAUSE.name -> {
//                setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PAUSE.name")
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    var selectedTrackPosition = intent.getIntExtra(SELECTED_TRACK_POSITION, -1)
//                    //BaseActivity.queueNowPlayIndex=selectedTrackPosition
//                    val selectedTrackPlayStratPosition = intent.getLongExtra(SELECTED_TRACK_PLAY_START_POSITION, 0)
//                    val shuffle = intent.getBooleanExtra(SHUFFLE_TRACKS, false)
//                    val isQueueItem = intent.getBooleanExtra(IS_TRACKS_QUEUEITEM, false)
//
//                    val playContext: PLAY_CONTEXT =
//                        intent.getSerializableExtra(PLAY_CONTEXT_TYPE) as PLAY_CONTEXT
//                    var tracksList = mutableListOf<Track>()
//
//                    when (playContext) {
//
//                        PLAY_CONTEXT.LIBRARY_TRACKS -> {
//                            setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PAUSE.name-LIBRARY_TRACKS")
//                            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
//                            val trackListDataModel = tracksRepository.getAllTracks(applicationContext, selectedTrackPosition, queueManager as NowPlayingQueue)
//                            selectedTrackPosition = trackListDataModel.selectedTrackIndex
//                            tracksList = trackListDataModel.trackListData
//                            mItem = tracksList as ArrayList<Track>
//                            playPosition = selectedTrackPosition
//                        }
//
//                        PLAY_CONTEXT.VIDEO_TRACK -> {
//                            tracksList = tracksRepository.getAllTracksVideo(applicationContext)
//                        }
//
//                        PLAY_CONTEXT.QUEUE_TRACKS -> {
//                            setLog("SwipablePlayerFragment", "AudioPlayerService-onStartCommand()-PlaybackControls.PAUSE.name-QUEUE_TRACKS")
//                            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
//                            val trackListDataModel = tracksRepository.getAllQueuedTracks(applicationContext, selectedTrackPosition, queueManager as NowPlayingQueue)
//                            selectedTrackPosition = trackListDataModel.selectedTrackIndex
//                            tracksList = trackListDataModel.trackListData
//                        }
//                    }
//                    audioPlayer.playTracks(applicationContext, tracksList, selectedTrackPosition, shuffle, selectedTrackPlayStratPosition, isQueueItem,true)
//
//                }
//            }
//            ACTION_PLAY -> {
//                if ((audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag == null){
//                    audioPlayer.playTracks(applicationContext, mItem!!, playPosition)
//                }
//                (audioPlayer as ExoPlayer).simpleExoPlayer.playWhenReady = true
//            }
//            ACTION_PAUSE -> {
//                (audioPlayer as ExoPlayer).simpleExoPlayer.playWhenReady = false
//            }
//            ACTION_PLAY_NEXT -> {
//                //playPosition = playPosition + 1
//                (audioPlayer as ExoPlayer).simpleExoPlayer.next()
//
//            }
//            ACTION_PAUSE_PREVIOUS -> {
//                //playPosition = playPosition - 1
//                (audioPlayer as ExoPlayer).simpleExoPlayer.previous()
//            }
//            PlaybackControls.SHUFFLE_OFF.name -> {
//
//                audioPlayer.setShuffleMode(this, false)
//            }
//
//            PlaybackControls.SHUFFLE_ON.name -> {
//
//                audioPlayer.setShuffleMode(this, true)
//            }
//            PlaybackControls.RE_ORDER.name -> {
//
//                val tracksList: ArrayList<Track> = ArrayList()
//
//                @Suppress("UNCHECKED_CAST")
//                tracksList.addAll(intent.getSerializableExtra(Constant.TRACKS_LIST) as ArrayList<Track>)
//
//                audioPlayer.updateTracks(this, tracksList)
//            }
//            PlaybackControls.ADD_PRE_CACHED_URL.name,
//            PlaybackControls.ADD_TO_QUEUE.name,
//            PlaybackControls.PLAY_NEXT.name -> {
//
//                val playbackStarted =
//                    (audioPlayer as ExoPlayer).nowPlayingQueue.nowPlayingTracksList.isNotEmpty()
//
//                if (playbackStarted) {
//
//                    if (intent.action == PlaybackControls.ADD_TO_QUEUE.name) {
//                        val isQueueTrackList =
//                            intent.getBooleanExtra(Constant.IS_QUEUE_TRACK_LIST, false)
//                        val showAddToQueueToast = intent.getBooleanExtra(Constant.showAddToQueueToast, true)
//                        if (isQueueTrackList){
//                            val trackList = intent.getSerializableExtra(Constant.TRACKS_LIST) as ArrayList<Track>
//                            audioPlayer.addTrackToQueue(this, trackList)
//                        }else{
//                            val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
//                            audioPlayer.addTrackToQueue(this, track)
//                        }
//                        if (showAddToQueueToast){
//                            val messageModel = MessageModel(getString(R.string.toast_str_39), getString(R.string.toast_str_40),
//                                MessageType.NEUTRAL, true)
//                            CommonUtils.showToast(this, messageModel)
//                        }
//                    }else if (intent.action == PlaybackControls.ADD_PRE_CACHED_URL.name) {
//                        val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
//                        setLog("preCatchContent", "AudioPlayerService-onStartCommand-PlaybackControls.ADD_PRE_CACHED_URL.name-track.url-${track.url}")
//                        audioPlayer.updateTrackData(this, track)
//                    }else {
//                        val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
//                        audioPlayer.playNext(this, track)
//                        val messageModel = MessageModel(getString(R.string.toast_str_44), getString(R.string.toast_str_44),
//                            MessageType.NEUTRAL, true)
//                        CommonUtils.showToast(this, messageModel)
//                    }
//
//                } else {
//
//                    //playback is not initiated
//                    var tracksList: ArrayList<Track> = ArrayList()
//
//                    val isQueueTrackList =
//                        intent.getBooleanExtra(Constant.IS_QUEUE_TRACK_LIST, false)
//                    if (isQueueTrackList){
//                        val trackList = intent.getSerializableExtra(Constant.TRACKS_LIST) as ArrayList<Track>
//                        tracksList = trackList
//                    }else{
//                        val track = intent.getSerializableExtra(Constant.SELECTED_TRACK) as Track
//                        @Suppress("UNCHECKED_CAST")
//                        tracksList.add(track)
//                    }
//                    audioPlayer.playTracks(this, tracksList, 0)
//                }
//            }
//        }
//
//        return START_STICKY
//    }
//
//    override fun onNotificationPosted(notificationId: Int, notification: Notification?) {
//        setLog("NotificationManager", "isForegroundService-$isForegroundService ")
//        /*if(!isForegroundService){
//            *//*ContextCompat.startForegroundService(
//                applicationContext,
//                Intent(applicationContext, this.javaClass)
//            )*//*
//            setLog("NotificationManager", "onPlayerStateChanged-4 ")
//            startForeground(notificationId, notification)
//            isForegroundService = true
//            if (!(audioPlayer as ExoPlayer).simpleExoPlayer.playWhenReady){
//                setLog("NotificationManager", "onPlayerStateChanged-5 ")
//                stopForeground(true)
//                audioPlayer.hideNotification()
//                isForegroundService = false
//            }
//        }*/
//        startForeground(notificationId, notification)
//        isForegroundService = true
//
//        if (isForegroundService && isStopAudioPlayer){
//            setLog("NotificationManager", "onPlayerStateChanged-14")
//            stopPlayerAndNotification()
//        }
//    }
//
//    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//        // removed call to stopSelf() here. This was killing the service when app is restarted
//        // again to play some other track and pressing back
//        sendBroadcastWidget()
//        isForegroundService = false
//    }
//
//    private fun sendBroadcastWidget(){
//        for (clazz in Classes.widgets) {
//            val ids = applicationContext.getAppWidgetsIdsFor(clazz)
//
//            val intent = Intent(applicationContext, clazz).apply {
//                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//                putExtra(PLAYPOSITION, playPosition)
//                putExtra(WIDGET_NO_PLAYING_EXTRA, "")
//                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
//            }
//
//            applicationContext.sendBroadcast(intent)
//        }
//    }
//    private fun updateAllWidget(playWhenReady: Boolean, position:Int){
//        for (clazz in Classes.widgets) {
//            val ids = applicationContext.getAppWidgetsIdsFor(clazz)
//            val serviceBundle = Bundle()
//            serviceBundle.putSerializable(ITEM_KEY, mItem)
//            val intent = Intent(applicationContext, clazz).apply {
//                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//                putExtra(BUNDLE_KEY, serviceBundle)
//                putExtra(WIDGET_PLAYING_EXTRA, playWhenReady)
//                putExtra(PLAYPOSITION, position)
//                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
//            }
//
//            applicationContext.sendBroadcast(intent)
//        }
//    }
//
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        stopSelf()
//    }
//
//    override fun onGetRoot(
//        clientPackageName: String,
//        clientUid: Int,
//        rootHints: Bundle?
//    ): BrowserRoot? {
//        return null
//    }
//
//    override fun onLoadChildren(
//        parentId: String,
//        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
//    ) {
//
//    }
//
//    private var isForegroundService = false
//    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
////        updateAllWidget(playWhenReady, playPosition)
//        when (playbackState) {
//
//            Player.STATE_IDLE, Player.STATE_ENDED -> {
//                setLog("NotificationManager", "onPlayerStateChanged-7 ")
//                stopForeground(true)
//                isForegroundService = false
//            }
//            /*Player.STATE_READY -> {
//                audioPlayer.showNotification()
//                setLog("NotificationManager", "onPlayerStateChanged-1 ")
//                if (playbackState == Player.STATE_READY) {
//                    setLog("NotificationManager", "onPlayerStateChanged-2 ")
//                    if (!playWhenReady) {
//                        setLog("NotificationManager", "onPlayerStateChanged-3 ")
//                        // If playback is paused we remove the foreground state which allows the
//                        // notification to be dismissed. An alternative would be to provide a
//                        // "close" button in the notification which stops playback and clears
//                        // the notification.
//                        stopForeground(false)
//                        isForegroundService = false
//                    }
//                }
//            }*/
//            /*Player.STATE_BUFFERING -> {
//                setLog("NotificationManager", "onPlayerStateChanged-11-playbackState-$playbackState-playWhenReady-$playWhenReady ")
//            }*/
//            else -> {
//                setLog("NotificationManager", "onPlayerStateChanged-6 ")
//                //audioPlayer.hideNotification()
//            }
//        }
//    }
//
//    override fun onPositionDiscontinuity(reason: Int) {
//        updateNowPlaying()
//    }
//
//    private fun updateNowPlaying() {
//        if ((audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag != null) {
//            val nowPlayingInfo: NowPlayingInfo =
//                    (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
//            (audioPlayer as ExoPlayer).simpleExoPlayer.setWakeMode(C.WAKE_MODE_NETWORK)
//            (audioPlayer as ExoPlayer).nowPlayingQueue.apply {
//                currentPlayingTrackId = nowPlayingInfo.id
//                playPosition = currentPlayingTrackIndex
//
//                nowPlayingTracksList.forEachIndexed { index, track ->
//                    if (track.id == currentPlayingTrackId) {
//                        currentPlayingTrackIndex = index
//                        return@forEachIndexed
//                    }
//                }
//            }
//        }
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        setLog("NotificationManager", "onPlayerStateChanged-8 ")
//        audioPlayer.cleanup()
//        sendBroadcastWidget()
//    }
//
//    /*override fun onUnbind(intent: Intent?): Boolean {
//        stopSelf()
//        return super.onUnbind(intent)
//    }*/
//
//    fun onUnbindService(){
//        //stopForeground(true)
//        audioPlayer.notificationCleanup()
//    }
//
//    enum class PlaybackControls {
//        PLAY,
//        PAUSE,
//        SHUFFLE_ON,
//        SHUFFLE_OFF,
//        RE_ORDER,
//        ADD_TO_QUEUE,
//        PLAY_NEXT,
//        ADD_PRE_CACHED_URL
//    }
//
//    fun Context.getAppWidgetsIdsFor(clazz: Class<*>): IntArray {
//        return AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, clazz))
//    }
//
//    object Classes {
//
//        const val WIDGET_ONE = "com.hungama.music.player.audioplayer.widget.PlayerWidget"
//        const val WIDGET_TWO = "com.hungama.music.player.audioplayer.widget.PlayerWidget2"
//        const val WIDGET_THREE = "com.hungama.music.player.audioplayer.widget.PlayerWidget3"
//
//        @JvmStatic
//        val widgets: List<Class<*>> by lazy {
//            listOf(
//                Class.forName(WIDGET_ONE),
//                Class.forName(WIDGET_TWO),
//                Class.forName(WIDGET_THREE)
//            )
//        }
//
//    }
//
//    fun getAudioPlayerInstance(): AudioPlayer {
//        return audioPlayer
//    }
//
//    fun setAudioPlayerInstance(simpleExoplayer: SimpleExoPlayer?) {
//        if (simpleExoplayer != null) {
//            (audioPlayer as ExoPlayer).simpleExoPlayer = simpleExoplayer
//        }
//    }
//
//    fun playNextContent(track: Track, addToQueue:Boolean){
//        val playbackStarted =
//            (audioPlayer as ExoPlayer).nowPlayingQueue.nowPlayingTracksList.isNotEmpty()
//        val nowPlayingInfo: NowPlayingInfo =
//            (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
//        if (playbackStarted) {
//
//            if (addToQueue)
//                audioPlayer.addTrackToQueue(this, track)
//            else
//                audioPlayer.playNext(this, track)
//
//            (audioPlayer as ExoPlayer).simpleExoPlayer.prepare()
//            //(audioPlayer as ExoPlayer).simpleExoPlayer.next()
//
//        } else {
//
//            //playback is not initiated
//
//            val tracksList: ArrayList<Track> = ArrayList()
//
//            @Suppress("UNCHECKED_CAST")
//            tracksList.add(track)
//            audioPlayer.playTracks(this, tracksList, 0)
//        }
//    }
//
//    fun updateTrackContent(trackList: ArrayList<Track>){
//        audioPlayer.updateTracks(this, trackList)
//    }
//    fun playPreviousContent(track: Track){
//        val nowPlayingInfo: NowPlayingInfo =
//            (audioPlayer as ExoPlayer).simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
//        audioPlayer.playPrevious(this, track)
//    }
//
//    fun initializeAds(context: Context?, companionView: ViewGroup?) {
//        val sdkFactory = ImaSdkFactory.getInstance()
//        val container =
//            ImaSdkFactory.createAudioAdDisplayContainer(context, imaService?.imaVideoAdPlayer)
//        val companionAdSlot = sdkFactory.createCompanionAdSlot()
//        companionAdSlot.container = companionView
//        companionAdSlot.setSize(300, 250)
//        container.setCompanionSlots(ImmutableList.of(companionAdSlot))
//        imaService?.init(container)
//    }
//
//
//    fun requestAd(adTagUrl: String?) {
//        imaService?.requestAds(adTagUrl)
//    }
//
//    /*fun setReInitializeInstances(exoPlayer:AudioPlayer): AudioPlayer {
//        exoPlayer.apply {
//            init(this@AudioPlayerService)
//            setNotificationPostedListener(this@AudioPlayerService)
//            setPlayerEventListener(this@AudioPlayerService)
//        }
//        return exoPlayer
//    }
//    fun playNextSong(selectedTrackPosition:Int,selectedTrackPlayStratPosition:Long, shuffle:Boolean, isQueueItem:Boolean, exoPlayer:AudioPlayer){
//        var selectedTrackPosition1 = selectedTrackPosition
//        CoroutineScope(Dispatchers.IO).launch {
//            var tracksList = mutableListOf<Track>()
//            queueManager = (audioPlayer as ExoPlayer).nowPlayingQueue
//            val trackListDataModel = tracksRepository.getAllQueuedTracks(applicationContext, selectedTrackPosition1, queueManager as NowPlayingQueue)
//            selectedTrackPosition1 = trackListDataModel.selectedTrackIndex
//            tracksList = trackListDataModel.trackListData
//            mItem = tracksList as ArrayList<Track>
//            exoPlayer.playTracks(applicationContext, tracksList, selectedTrackPosition1, shuffle, selectedTrackPlayStratPosition, isQueueItem,false)
//        }
//    }*/
//
//    fun stopAudiPlayerService(){
//        if (!isForegroundService){
//            setLog("NotificationManager", "onPlayerStateChanged-12")
//            (audioPlayer as ExoPlayer).simpleExoPlayer.pause()
//            isStopAudioPlayer = true
//        }else{
//            setLog("NotificationManager", "onPlayerStateChanged-13")
//            stopPlayerAndNotification()
//        }
//    }
//
//    fun stopPlayerAndNotification(){
//        setLog("NotificationManager", "onPlayerStateChanged-15")
//        (audioPlayer as ExoPlayer).simpleExoPlayer.stop()
//        onUnbindService()
//        //isForegroundService = false
//        isStopAudioPlayer = false
//    }
//
//    fun setBaseActivityInstance(baseActivity: BaseActivity){
//        this.baseActivity = baseActivity
//    }
//
//    private fun callStream(queueManager:QueueManager, position: Int){
//        if (queueManager != null && !queueManager.getNowPlayingTracks().isNullOrEmpty() && position > -1 && queueManager.getNowPlayingTracks().size > position){
//            setLog(TAG, "onStartCommand: title:${queueManager.getNowPlayingTracks().get(position)?.title}")
//
//            if (baseActivity != null){
//                val track = queueManager.getNowPlayingTracks().get(position)
//                setLog(TAG, "onStartCommand: baseActivity:${baseActivity?.javaClass?.simpleName}")
//                GlobalScope.launch {
//                    withContext(Dispatchers.Main) {
//                        setLog(
//                            "callUserStreamUpdate1",
//                            "callUserStreamUpdate-callStream-position==-:${position} track.title: ${track.title} lastSongPlayDuration:${BaseActivity.lastSongPlayDuration}"
//                        )
//                        baseActivity?.callUserStreamUpdate(-1, track, position)
//                    }
//                }
//
//            }
//        }
//    }
//}