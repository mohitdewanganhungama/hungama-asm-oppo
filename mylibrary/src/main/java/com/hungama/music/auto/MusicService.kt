//package com.hungama.music.auto
//
//import android.app.Notification
//import android.app.PendingIntent
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.os.ResultReceiver
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.MediaBrowserCompat.MediaItem
//import android.support.v4.media.MediaDescriptionCompat
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import androidx.media.MediaBrowserServiceCompat
//import com.google.android.exoplayer2.*
//import com.google.android.exoplayer2.audio.AudioAttributes
//import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
//import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
//import com.google.android.exoplayer2.source.ConcatenatingMediaSource
//import com.google.android.exoplayer2.source.MediaSource
//import com.google.android.exoplayer2.source.ProgressiveMediaSource
//import com.google.android.exoplayer2.source.dash.DashMediaSource
//import com.google.android.exoplayer2.source.hls.HlsMediaSource
//import com.google.android.exoplayer2.ui.PlayerNotificationManager
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
//import com.google.android.exoplayer2.util.MimeTypes
//import com.google.android.exoplayer2.util.Util
//import com.hungama.music.auto.api.model.HomeModel
//import com.hungama.music.auto.channel.ChannelHelper
//import com.hungama.music.auto.media.extensions.*
//import com.hungama.music.auto.media.library.*
//import com.hungama.music.R
//import kotlinx.coroutines.*
//
///**
// * This class is the entry point for browsing and playback commands from the APP's UI
// * and other apps that wish to play music via UAMP (for example, Android Auto or
// * the Google Assistant).
// *
// * Browsing begins with the method [MusicService.onGetRoot], and continues in
// * the callback [MusicService.onLoadChildren].
// *
// * For more information on implementing a MediaBrowserService,
// * visit [https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html).
// *
// * This class also handles playback for Cast sessions.
// * When a Cast session is active, playback commands are passed to a
// * [CastPlayer](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/ext/cast/CastPlayer.html),
// * otherwise they are passed to an ExoPlayer for local playback.
// */
//open class MusicService : MediaBrowserServiceCompat() {
//
//    private lateinit var notificationManager: NotificationManager
//    private lateinit var mediaSource: MusicSource
//    private lateinit var packageValidator: PackageValidator
//
//    // The current player will either be an ExoPlayer (for local playback) or a CastPlayer (for
//    // remote playback through a Cast device).
//    private lateinit var currentPlayer: Player
//
//    private val serviceJob = SupervisorJob()
//    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
//
//    protected lateinit var mediaSession: MediaSessionCompat
//    protected lateinit var mediaSessionConnector: MediaSessionConnector
//    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
//
//    private lateinit var storage: PersistentStorage
//
//    val concatenatingMediaSource = ConcatenatingMediaSource()
//
//    companion object{
//        var podcastBucketRespModel: HomeModel?=null
//        var discoverBucketRespModel:HomeModel?=null
//        val podcastBucketListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//        var podcastListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//        val discoverBucketListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//        var discoverListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//        var podcastTrackListingListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//        var discoverBucketList = arrayListOf<MediaItem>()
//        var discoverBucketWiseList = arrayListOf<MediaItem>()
//        var podcastBucketList = arrayListOf<MediaItem>()
//        var podcastBucketWiseList = arrayListOf<MediaItem>()
//
//    }
//    /**
//     * This must be `by lazy` because the source won't initially be ready.
//     * See [MusicService.onLoadChildren] to see where it's accessed (and first
//     * constructed).
//     */
//    private val browseTree: BrowseTree by lazy {
//        BrowseTree(applicationContext, mediaSource)
//    }
//
//    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
//        DefaultDataSourceFactory(
//            /* context= */ this,
//            Util.getUserAgent(/* context= */ this, UAMP_USER_AGENT), /* listener= */
//            null
//        )
//    }
//
//    private var isForegroundService = false
//
//    private val remoteJsonSource: Uri =
//        Uri.parse("https://storage.googleapis.com/uamp/catalog.json")
//
////    private val remoteJsonSource: Uri =
////        Uri.parse("https://drive.google.com/file/d/1ZEC-RQmM9ICJHx2Khu8VyVy7dDMNjmd2/view?usp=sharing")
//
//    private val uAmpAudioAttributes = AudioAttributes.Builder()
//        .setContentType(C.CONTENT_TYPE_MUSIC)
//        .setUsage(C.USAGE_MEDIA)
//        .build()
//
//    private val playerListener = PlayerEventListener()
//
//    /**
//     * Configure ExoPlayer to handle audio focus for us.
//     * See [Player.AudioComponent.setAudioAttributes] for details.
//     */
//    private val exoPlayer: ExoPlayer by lazy {
//        SimpleExoPlayer.Builder(this).build().apply {
//            setAudioAttributes(uAmpAudioAttributes, true)
//            setHandleAudioBecomingNoisy(true)
//            addListener(playerListener)
//        }
//    }
//
//    /**
//     * If Cast is available, create a CastPlayer to handle communication with a Cast session.
//     */
////    private val castPlayer: CastPlayer? by lazy {
////        try {
////            val castContext = CastContext.getSharedInstance(this)
////            CastPlayer(castContext).apply {
////                setSessionAvailabilityListener(UampCastSessionAvailabilityListener())
////                addListener(playerListener)
////            }
////        } catch (e : Exception) {
////            // We wouldn't normally catch the generic `Exception` however
////            // calling `CastContext.getSharedInstance` can throw various exceptions, all of which
////            // indicate that Cast is unavailable.
////            // Related internal bug b/68009560.
////            Log.i(TAG, "Cast is not available on this device. " +
////                    "Exception thrown when attempting to obtain CastContext. " + e.message)
////            null
////        }
////    }
//
//    var isCretaeFunCalled=false
//
//    @ExperimentalCoroutinesApi
//    override fun onCreate() {
//        super.onCreate()
//
////        if(!isCretaeFunCalled){
////            cretaeFunCalled()
////            isCretaeFunCalled=true
////        }
//        cretaeFunCalled()
//
//        Log.d(TAG, "onCreate: cretaeFunCalled")
//    }
//
//    fun cretaeFunCalled(){
//        // Build a PendingIntent that can be used to launch the UI.
//        val sessionActivityPendingIntent =
//            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
//                PendingIntent.getActivity(this, 0, sessionIntent, 0)
//            }
//
//        // Create a new MediaSession.
//        mediaSession = MediaSessionCompat(this, "MusicService")
//            .apply {
//                setSessionActivity(sessionActivityPendingIntent)
//                isActive = true
//            }
//
//        /**
//         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
//         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat].
//         *
//         * It is possible to wait to set the session token, if required for a specific use-case.
//         * However, the token *must* be set by the time [MediaBrowserServiceCompat.onGetRoot]
//         * returns, or the connection will fail silently. (The system will not even call
//         * [MediaBrowserCompat.ConnectionCallback.onConnectionFailed].)
//         */
//        sessionToken = mediaSession.sessionToken
//
//        /**
//         * The notification manager will use our player and media session to decide when to post
//         * notifications. When notifications are posted or removed our listener will be called, this
//         * allows us to promote the service to foreground (required so that we're not killed if
//         * the main UI is not visible).
//         */
//        notificationManager = NotificationManager(
//            this,
//            mediaSession.sessionToken,
//            PlayerNotificationListener()
//        )
//
//        // The media library is built from a remote JSON file. We'll create the source here,
//        // and then use a suspend function to perform the download off the main thread.
//        mediaSource = JsonSource(source = remoteJsonSource, context = this)
//        serviceScope.launch {
//            Log.d(TAG, "onCreate: mediaSource.load() called:${remoteJsonSource}")
//            mediaSource.load()
//        }
//
//        // ExoPlayer will manage the MediaSession for us.
//        mediaSessionConnector = MediaSessionConnector(mediaSession)
//        mediaSessionConnector.setPlaybackPreparer(PlaybackPreparer())
//        mediaSessionConnector.setQueueNavigator(QueueNavigator(mediaSession))
//
////        switchToPlayer(
////            previousPlayer = null,
////            newPlayer = if (castPlayer?.isCastSessionAvailable == true) castPlayer!! else exoPlayer
////        )
//
//        currentPlayer=exoPlayer
//        notificationManager.showNotificationForPlayer(currentPlayer)
//
//        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
//
//        storage = PersistentStorage.getInstance(applicationContext)
//
//        Log.d(TAG, "cretaeFunCalled called")
//    }
//
//    /**
//     * This is the code that causes UAMP to stop playing when swiping the activity away from
//     * recents. The choice to do this is app specific. Some apps stop playback, while others allow
//     * playback to continue and allow users to stop it with the notification.
//     */
//    override fun onTaskRemoved(rootIntent: Intent) {
//        saveRecentSongToStorage()
//        super.onTaskRemoved(rootIntent)
//
//        /**
//         * By stopping playback, the player will transition to [Player.STATE_IDLE] triggering
//         * [Player.Listener.onPlayerStateChanged] to be called. This will cause the
//         * notification to be hidden and trigger
//         * [PlayerNotificationManager.NotificationListener.onNotificationCancelled] to be called.
//         * The service will then remove itself as a foreground service, and will call
//         * [stopSelf].
//         */
//        currentPlayer.stop(/* reset= */true)
//    }
//
//    override fun onDestroy() {
//        mediaSession.run {
//            isActive = false
//            release()
//        }
//
//        // Cancel coroutines when the service is going away.
//        serviceJob.cancel()
//
//        // Free ExoPlayer resources.
//        exoPlayer.removeListener(playerListener)
//        exoPlayer.release()
//    }
//
//    /**
//     * Returns the "root" media ID that the client should request to get the list of
//     * [MediaItem]s to browse/play.
//     */
//    override fun onGetRoot(
//        clientPackageName: String,
//        clientUid: Int,
//        rootHints: Bundle?
//    ): BrowserRoot? {
//
//
//        Log.d(TAG, "onGetRoot isCretaeFunCalled:${isCretaeFunCalled}")
//
//        /*
//         * By default, all known clients are permitted to search, but only tell unknown callers
//         * about search if permitted by the [BrowseTree].
//         */
//        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
//        val rootExtras = Bundle().apply {
//            putBoolean(
//                MEDIA_SEARCH_SUPPORTED,
//                isKnownCaller || browseTree.searchableByUnknownCaller
//            )
//            putBoolean(CONTENT_STYLE_SUPPORTED, true)
//            putInt(CONTENT_STYLE_BROWSABLE_GRID_HINT, CONTENT_STYLE_GRID)
//            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST)
//            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
//
//            // All our media are playable offline.
//            // Forward OFFLINE hint when specified.
//            if (rootHints?.getBoolean(BrowserRoot.EXTRA_OFFLINE) == true) {
//                putBoolean(BrowserRoot.EXTRA_OFFLINE, true)
//            }
//        }
//
//        Log.d(TAG, "onGetRoot: isKnownCaller${isKnownCaller}")
//
//        return if (isKnownCaller) {
//            /**
//             * By default return the browsable root. Treat the EXTRA_RECENT flag as a special case
//             * and return the recent root instead.
//             */
//            val isRecentRequest = rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) ?: false
//            val browserRootPath = if (isRecentRequest) HUNGAMA_RECENT_ROOT else HUNGAMA_BROWSABLE_ROOT
//
//            Log.d(TAG, "onGetRoot: isRecentRequest:${isRecentRequest} BrowserRoot${browserRootPath}")
//
//            BrowserRoot(browserRootPath, rootExtras)
//
//
//        } else {
//            Log.d(TAG, "onGetRoot: BrowserRoot${HUNGAMA_EMPTY_ROOT}")
//
//            /**
//             * Unknown caller. There are two main ways to handle this:
//             * 1) Return a root without any content, which still allows the connecting client
//             * to issue commands.
//             * 2) Return `null`, which will cause the system to disconnect the app.
//             *
//             * UAMP takes the first approach for a variety of reasons, but both are valid
//             * options.
//             */
//            BrowserRoot(HUNGAMA_EMPTY_ROOT, rootExtras)
//        }
//    }
//
//    /**
//     * Returns (via the [result] parameter) a list of [MediaItem]s that are child
//     * items of the provided [parentMediaId]. See [BrowseTree] for more details on
//     * how this is build/more details about the relationships.
//     */
//    override fun onLoadChildren(
//        parentMediaId: String,
//        result: Result<List<MediaItem>>
//    ) {
//        Log.d("TAG", "onLoadChildren called mediaSource:${mediaSource} parentMediaId:${parentMediaId} result:${result}")
//        /**
//         * If the caller requests the recent root, return the most recently played song.
//         */
//        if (parentMediaId == HUNGAMA_RECENT_ROOT) {
//            result.sendResult(storage.loadRecentSong()?.let { song -> listOf(song) })
//        }else  if (parentMediaId == HUNGAMA_PODCAST_ROOT || parentMediaId == HUNGAMA_DISCOVER_ROOT) {
//            Log.d("TAG", "onLoadChildren getBucketListing called")
//
//            result.sendResult(ChannelHelper.getBucketListing(this, parentMediaId))
//        }
//        else  if (parentMediaId?.contains(HUNGAMA_PODCAST_DETAIL_ROOT,true)) {
//            Log.d("TAG", "onLoadChildren getPodcastDetail called")
//            ChannelHelper.getPodcastDetail(this, parentMediaId,result)
//            Log.d("TAG", "getPodcastDetail list size:${MusicService.podcastTrackListingListing?.size}")
//            Log.d("TAG", "getPodcastDetail after list size:${MusicService.podcastTrackListingListing?.size}")
////            result.sendResult(MusicService.podcastTrackListingListing)
//
//            result.detach()
//        } else  if (parentMediaId?.contains(HUNGAMA_PODCAST_ROOT,true) || parentMediaId?.contains(HUNGAMA_DISCOVER_ROOT,true)) {
//            Log.d("TAG", "onLoadChildren getHeaderWiseListing called")
//            result.sendResult(ChannelHelper.getHeaderWiseListing(this, parentMediaId))
//        } else {
//            // If the media source is ready, the results will be set synchronously here.
//            val resultsSent = mediaSource.whenReady { successfullyInitialized ->
//                Log.d("TAG", "0 successfullyInitialized:${successfullyInitialized}")
//
//                if (successfullyInitialized) {
//                    val children = browseTree[parentMediaId]?.map { item ->
//                        Log.d("TAG", "successfullyInitialized item:${item}")
//                        Log.d("TAG", "successfullyInitialized item.description:${item.description}")
//                        Log.d("TAG", "successfullyInitialized item.mediaMetadata:${item.mediaMetadata}")
//                        Log.d("TAG", "successfullyInitialized item.title:${item.title}")
//                        Log.d("TAG", "successfullyInitialized item.flag:${item.flag}")
//                        MediaItem(item.description, item.flag)
//                    }
//                    result.sendResult(children)
//                } else {
//                    mediaSession.sendSessionEvent(NETWORK_FAILURE, null)
//                    result.sendResult(null)
//                }
//            }
//
//            Log.d("TAG", "onLoadChildren resultsSent:${resultsSent}")
//            // If the results are not ready, the service must "detach" the results before
//            // the method returns. After the source is ready, the lambda above will run,
//            // and the caller will be notified that the results are ready.
//            //
//            // See [MediaItemFragmentViewModel.subscriptionCallback] for how this is passed to the
//            // UI/displayed in the [RecyclerView].
//            if (!resultsSent) {
//                result.detach()
//            }
//        }
//    }
//
//    /**
//     * Returns a list of [MediaItem]s that match the given search query
//     */
//    override fun onSearch(
//        query: String,
//        extras: Bundle?,
//        result: Result<List<MediaItem>>
//    ) {
//
//        val resultsSent = mediaSource.whenReady { successfullyInitialized ->
//            if (successfullyInitialized) {
//                val resultsList = mediaSource.search(query, extras ?: Bundle.EMPTY)
//                    .map { mediaMetadata ->
//                        MediaItem(mediaMetadata.description, mediaMetadata.flag)
//                    }
//                result.sendResult(resultsList)
//            }
//        }
//
//        if (!resultsSent) {
//            result.detach()
//        }
//    }
//
//    /**
//     * Load the supplied list of songs and the song to play into the current player.
//     */
//    private fun preparePlaylist(
//        metadataList: List<MediaMetadataCompat>,
//        itemToPlay: MediaMetadataCompat?,
//        playWhenReady: Boolean,
//        playbackStartPositionMs: Long
//    ) {
//        // Since the playlist was probably based on some ordering (such as tracks
//        // on an album), find which window index to play first so that the song the
//        // user actually wants to hear plays first.
//        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
//        currentPlaylistItems = metadataList
//
//        currentPlayer.playWhenReady = playWhenReady
//        currentPlayer.stop(/* reset= */ true)
//        Log.d(TAG, "preparePlaylist currentPlaylistItems size: ${currentPlaylistItems?.size}")
//
//
//        currentPlaylistItems?.forEach {
//                Log.d(TAG, "preparePlaylist METADATA_KEY_MEDIA_URI: ${it?.bundle?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}")
//                Log.d(TAG, "preparePlaylist METADATA_KEY_DISPLAY_TITLE: ${it?.bundle?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)}")
//
//                val mURL=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
//                val title=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)
//                val artist=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
//                val album=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
//                val album_artist=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST)
//                val subtitle=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)
//                val description=it?.bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)
//                Log.d(TAG, "preparePlaylist: ${it?.bundle.get(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}")
//
//                var mimType = MimeTypes.BASE_TYPE_AUDIO
//                if (mURL?.contains(".m3u8", true)!!) {
//                    mimType = MimeTypes.APPLICATION_M3U8
//                } else if (mURL?.contains(".mp3", true)!!) {
//                    mimType = MimeTypes.BASE_TYPE_AUDIO
//                } else if (mURL?.contains(".mpd", true)!!) {
//                    mimType = MimeTypes.APPLICATION_MPD
//                } else if (mURL?.contains(".mp4", true)!!) {
//                    mimType = MimeTypes.APPLICATION_MP4
//                }
//
//
//                val mediaItem = com.google.android.exoplayer2.MediaItem.Builder()
//                    .setMediaId(mURL)
//                    .setUri(Uri.parse(mURL))
//                    .setMediaMetadata(
//                        MediaMetadata.Builder()
//                        .setTitle(title)
//                        .setDisplayTitle(title)
//                        .setDescription(description)
//                        .setArtist(artist)
//                        .setAlbumTitle(album)
//                        .setArtworkUri(Uri.parse(mURL))
//                        .setMediaUri(Uri.parse(mURL))
//                        .setAlbumArtist(album_artist)
//                        .setSubtitle(subtitle)
//                        .build())
//                    .setMimeType(mimType)
//                    .build()
//
//
//                var mediaSource: MediaSource? = null
//                val dataSourceFactory2 = DemoUtil.getDataSourceFactory( /* context= */this)
//                val dataSourceFactory = DefaultDataSourceFactory(this, dataSourceFactory2!!)
//
//                if (mURL?.contains(".m3u8", true)!!) {
//                    mediaSource = HlsMediaSource.Factory(dataSourceFactory)
//                        //.setTag(NowPlayingInfo(track.id, index))
//                        .createMediaSource(mediaItem)
//
//                    Log.d(TAG, "preparePlaylist mediaItem m3u8 :${mURL} ")
//
//                } else if (mURL?.contains(".mpd", true)!!){
//                    mediaSource = DashMediaSource.Factory(dataSourceFactory)
//                        .createMediaSource(mediaItem)
//
//                    Log.d(TAG, "preparePlaylist mediaItem mpd :${mURL} ")
//                } else {
//                    mediaSource =
//                        ProgressiveMediaSource.Factory(dataSourceFactory)
//                            //.setTag(NowPlayingInfo(track.id, index))
//                            //.createMediaSource(uri)
//                            .createMediaSource(mediaItem)
//                    Log.d(TAG, "preparePlaylist mediaItem genral :${mURL} ")
//                }
//
//                Log.d(TAG, "preparePlaylist mediaItem: ${mediaItem}")
//
//                concatenatingMediaSource?.addMediaSource(mediaSource)
//            }
//
//            exoPlayer.setMediaSource(concatenatingMediaSource)
////            exoPlayer.seekTo(initialWindowIndex, playbackStartPositionMs)
//            exoPlayer?.playWhenReady=true
//
//    }
//
//
//    private fun saveRecentSongToStorage() {
//
//        if(currentPlaylistItems?.isNullOrEmpty()||currentPlayer==null||currentPlayer?.currentWindowIndex<0){
//            return
//        }
//
//        // Obtain the current song details *before* saving them on a separate thread, otherwise
//        // the current player may have been unloaded by the time the save routine runs.
//        val description = currentPlaylistItems[currentPlayer.currentWindowIndex].description
//        val position = currentPlayer.currentPosition
//
//        Log.d(TAG, "saveRecentSongToStorage: ")
//
//        serviceScope.launch {
//            storage.saveRecentSong(
//                description,
//                position
//            )
//        }
//    }
//
//
//    private inner class QueueNavigator(
//        mediaSession: MediaSessionCompat
//    ) : TimelineQueueNavigator(mediaSession) {
//        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
//            currentPlaylistItems[windowIndex].description
//    }
//
//    private inner class PlaybackPreparer : MediaSessionConnector.PlaybackPreparer {
//
//        /**
//         * UAMP supports preparing (and playing) from search, as well as media ID, so those
//         * capabilities are declared here.
//         *
//         * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
//         */
//        override fun getSupportedPrepareActions(): Long =
//            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
//                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
//                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
//                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
//
//        override fun onPrepare(playWhenReady: Boolean) {
//            val recentSong = storage.loadRecentSong() ?: return
//            onPrepareFromMediaId(
//                recentSong.mediaId!!,
//                playWhenReady,
//                recentSong.description.extras
//            )
//        }
//
//        override fun onPrepareFromMediaId(
//            mediaId: String,
//            playWhenReady: Boolean,
//            extras: Bundle?
//        ) {
//            mediaSource.whenReady {
//                var itemToPlay: MediaMetadataCompat? = mediaSource.find { item ->
//                    item.id == mediaId
//                }
//                Log.w(TAG, "Content found: 1 itemToPlay=${itemToPlay.toString()}")
//                if(itemToPlay==null){
//                    podcastTrackListingListing?.forEach {
//                        if(it?.mediaId==mediaId){
//                            itemToPlay=MediaMetadataCompat.Builder()
//                                .apply {
//                                    displayIconUri =it?.description.iconUri?.toString()
//                                    albumArtUri = it?.description.iconUri?.toString()
//                                    displayTitle= it?.description.title.toString()
//                                    album=it?.description?.description?.toString()
//                                    displaySubtitle= it?.description.subtitle.toString()
//                                    mediaUri= "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3"
////                                    mediaUri= it?.description?.mediaUri?.toString()
//                                    putString(JsonSource.ORIGINAL_ARTWORK_URI_KEY, it?.description.iconUri?.toString())
//                                }
//                                .build()
//                            Log.w(TAG, "Content found: 2 itemToPlay=${it.toString()}")
//                            return@forEach
//
//                        }
//                    }
//
//
//                }
//
//
//                if (itemToPlay == null) {
//
//
//
//                    Log.w(TAG, "Content not found: MediaID=$mediaId")
//                    // TODO: Notify caller of the error.
//                } else {
//
//                    val playbackStartPositionMs =
//                        extras?.getLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, C.TIME_UNSET)
//                            ?: C.TIME_UNSET
//
//                    preparePlaylist(
//                        buildPlaylist(itemToPlay!!),
//                        itemToPlay,
//                        playWhenReady,
//                        playbackStartPositionMs
//                    )
//                }
//            }
//        }
//
//        /**
//         * This method is used by the Google Assistant to respond to requests such as:
//         * - Play Geisha from Wake Up on UAMP
//         * - Play electronic music on UAMP
//         * - Play music on UAMP
//         *
//         * For details on how search is handled, see [AbstractMusicSource.search].
//         */
//        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
//            mediaSource.whenReady {
//                val metadataList = mediaSource.search(query, extras ?: Bundle.EMPTY)
//                if (metadataList.isNotEmpty()) {
//                    preparePlaylist(
//                        metadataList,
//                        metadataList[0],
//                        playWhenReady,
//                        playbackStartPositionMs = C.TIME_UNSET
//                    )
//                }
//            }
//        }
//
//        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
//
//        override fun onCommand(
//            player: Player,
//            controlDispatcher: ControlDispatcher,
//            command: String,
//            extras: Bundle?,
//            cb: ResultReceiver?
//        )=false
//
//
//        /**
//         * Builds a playlist based on a [MediaMetadataCompat].
//         *
//         * TODO: Support building a playlist by artist, genre, etc...
//         *
//         * @param item Item to base the playlist on.
//         * @return a [List] of [MediaMetadataCompat] objects representing a playlist.
//         */
//        private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
//            mediaSource.filter { it.album == item.album }.sortedBy { it.trackNumber }
//    }
//
//    /**
//     * Listen for notification events.
//     */
//    private inner class PlayerNotificationListener :
//        PlayerNotificationManager.NotificationListener {
//        override fun onNotificationPosted(
//            notificationId: Int,
//            notification: Notification,
//            ongoing: Boolean
//        ) {
//            if (ongoing && !isForegroundService) {
//                ContextCompat.startForegroundService(
//                    applicationContext,
//                    Intent(applicationContext, this@MusicService.javaClass)
//                )
//
//                startForeground(notificationId, notification)
//                isForegroundService = true
//            }
//        }
//
//        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//            stopForeground(true)
//            isForegroundService = false
//            stopSelf()
//        }
//    }
//
//    /**
//     * Listen for events from ExoPlayer.
//     */
//    private inner class PlayerEventListener : Player.Listener {
//        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            when (playbackState) {
//                Player.STATE_BUFFERING,
//                Player.STATE_READY -> {
//                    notificationManager.showNotificationForPlayer(currentPlayer)
//                    if (playbackState == Player.STATE_READY) {
//
//                        // When playing/paused save the current media item in persistent
//                        // storage so that playback can be resumed between device reboots.
//                        // Search for "media resumption" for more information.
//                        saveRecentSongToStorage()
//
//                        if (!playWhenReady) {
//                            // If playback is paused we remove the foreground state which allows the
//                            // notification to be dismissed. An alternative would be to provide a
//                            // "close" button in the notification which stops playback and clears
//                            // the notification.
//                            stopForeground(false)
//                            isForegroundService = false
//                        }
//                    }
//                }
//                else -> {
//                    notificationManager.hideNotification()
//                }
//            }
//        }
//
////        override fun onPlayerError(error: ExoPlaybackException) {
////            var message = R.string.generic_error
////            when (error.type) {
////                // If the data from MediaSource object could not be loaded the Exoplayer raises
////                // a type_source error.
////                // An error message is printed to UI via Toast message to inform the user.
////                ExoPlaybackException.TYPE_SOURCE -> {
////                    message = R.string.error_media_not_found
////                    Log.e(TAG, "TYPE_SOURCE: " + error.sourceException.message)
////                }
////                // If the error occurs in a render component, Exoplayer raises a type_remote error.
////                ExoPlaybackException.TYPE_RENDERER -> {
////                    Log.e(TAG, "TYPE_RENDERER: " + error.rendererException.message)
////                }
////                // If occurs an unexpected RuntimeException Exoplayer raises a type_unexpected error.
////                ExoPlaybackException.TYPE_UNEXPECTED -> {
////                    Log.e(TAG, "TYPE_UNEXPECTED: " + error.unexpectedException.message)
////                }
////                // If the error occurs in a remote component, Exoplayer raises a type_remote error.
////                ExoPlaybackException.TYPE_REMOTE -> {
////                    Log.e(TAG, "TYPE_REMOTE ****: " + error.message)
////                }
////            }
////            Toast.makeText(
////                applicationContext,
////                message,
////                Toast.LENGTH_LONG
////            ).show()
////        }
//    }
//}
//
///*
// * (Media) Session events
// */
//const val NETWORK_FAILURE = "com.hungama.music.shared.media.session.NETWORK_FAILURE"
//
///** Content styling constants */
//
//private const val CONTENT_STYLE_BROWSABLE_GRID_HINT = "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT"
//private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
//private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
//private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
//private const val CONTENT_STYLE_LIST = 1
//private const val CONTENT_STYLE_GRID = 2
//
//private const val UAMP_USER_AGENT = "hungama.music"
//
//val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"
//
//private const val TAG = "MusicService"