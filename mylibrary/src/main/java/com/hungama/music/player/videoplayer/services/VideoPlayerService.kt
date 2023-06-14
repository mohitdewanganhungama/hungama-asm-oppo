package com.hungama.music.player.videoplayer.services

import android.app.*
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MediaSourceFactory
import androidx.media3.exoplayer.source.ads.AdsLoader
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerView
import com.hungama.music.data.model.PlanNames
import com.hungama.music.data.model.PlayableContentModel
import com.hungama.music.player.audioplayer.mediautils.OnBitmapLoadedListener
import com.hungama.music.player.audioplayer.mediautils.loadAlbumArtUri
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.player.download.IntentUtil
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.MusicVideoDetailsFragment
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.ACTION_PAUSE
import com.hungama.music.utils.Constant.ACTION_PLAY
import com.hungama.music.utils.Constant.BUNDLE_KEY
import com.hungama.music.utils.Constant.ITEM_KEY
import com.hungama.music.utils.Constant.PLAYBACK_CHANNEL_ID
import com.hungama.music.utils.Constant.PLAYBACK_NOTIFICATION_ID
import com.hungama.music.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class VideoPlayerService : Service(), Player.Listener {

    private val mBinder = LocalBinder()
    private var mPlayer: ExoPlayer? = null
    private var mItem = ArrayList<PlayableContentModel>()
    private lateinit var mPlayerNotificationManager: NotificationManagerCompat
    private var mediaSession: MediaSession? = null
    private var sessionToken: MediaBrowserServiceCompat? = null
    private var intent1: Intent? = null
    private var dataSourceFactory2: DataSource.Factory? = null
    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private var selectedTrackPosition = 0

    private var adsLoader: ImaAdsLoader? = null
    val playerInstance: ExoPlayer?
        get() {
            if (mItem.size > 0 && mPlayer == null) {
                startPlayer()
            }
            return mPlayer
        }
    val mediaSessionInstance: MediaSession?
    get(){
        return mediaSession
    }
    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    fun releasePlayer() {
        if (mPlayer != null) {
            mPlayerNotificationManager.cancel(PLAYBACK_NOTIFICATION_ID)
            mPlayer?.release()
            mPlayer = null
            if (mediaSession != null){
                mediaSession?.release()
            }
            stopForeground(true)
        }
    }

    fun updatePlayerList(songsList: ArrayList<PlayableContentModel>) {
        mItem = ArrayList()

        startPlayer()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        intent1 = service
        val b = service!!.getBundleExtra(BUNDLE_KEY)
        if (b != null) {
            releasePlayer()
            mItem = ArrayList()
            mItem = b.getParcelableArrayList<PlayableContentModel>(ITEM_KEY) as ArrayList<PlayableContentModel>
            val listType = b.getString(Constant.LIST_TYPE)
            selectedTrackPosition = b.getInt(Constant.SELECTED_TRACK_POSITION, 0)
            setLog("Audio_Video", listType!!)
            setLog("LifeCycle:--3", mItem.toString())
        }
        if (mItem.size > 0 && mPlayer == null) {
            startPlayer()
        }
        return super.bindService(service, conn, flags)
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
        releasePlayer()
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent1 = intent
        val b = intent.getBundleExtra(BUNDLE_KEY)


        if (b != null) {
            releasePlayer()
            mItem = ArrayList()
            mItem = b.getParcelableArrayList<PlayableContentModel>(ITEM_KEY) as ArrayList<PlayableContentModel>
            val listType = b.getString(Constant.LIST_TYPE)
            selectedTrackPosition = b.getInt(Constant.SELECTED_TRACK_POSITION, 0)
            setLog("Audio_Video", listType!!)
        }
        if (mItem.size > 0 && mPlayer == null) {
            startPlayer()
        }
        val action = intent.action
        if (mPlayer != null) {
            if (!TextUtils.isEmpty(action) && action!!.equals(
                            ACTION_PLAY,
                            ignoreCase = true
                    )) {
                mPlayer!!.playWhenReady = true

            }
            if (!TextUtils.isEmpty(action) && action!!.equals(
                            ACTION_PAUSE,
                            ignoreCase = true
                    )) {
                mPlayer!!.playWhenReady = false
            }
        }
        return Service.START_NOT_STICKY
    }



    private fun startPlayer() {
        try {
            // Create an AdsLoader.
            adsLoader = ImaAdsLoader.Builder( /* context= */this)
                .setDebugModeEnabled(true)
                .setAdErrorListener {
                    setLog("adsLoader", "setAdErrorListener: "+it?.error)
                }.build()

            if(mItem!=null&&mItem.size>0){
                val context = this
                dataSourceFactory2 = DemoUtil.getDataSourceFactory( /* context= */context)

                dataSourceFactory = DefaultDataSourceFactory(context, dataSourceFactory2!!)


                var mPlayerView: PlayerView?=null
                if (VideoPlayerActivity.mPlayerView != null){
                    mPlayerView = VideoPlayerActivity.mPlayerView
                }else if (MusicVideoDetailsFragment.mPlayerView != null){
                    mPlayerView = MusicVideoDetailsFragment.mPlayerView
                }
                val preferExtensionDecoders = false
                val renderersFactory =
                    DemoUtil.buildRenderersFactory( /* context= */this, preferExtensionDecoders)
                val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
                    .setAdsLoaderProvider { unusedAdTagUri: MediaItem.AdsConfiguration? -> adsLoader }
                    .setAdViewProvider(mPlayerView)

                val trackSelector = DefaultTrackSelector( /* context= */this)
                trackSelector.parameters = DefaultTrackSelector.ParametersBuilder( /* context= */this).build()

                mPlayer = ExoPlayer.Builder( /* context= */this, renderersFactory)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setTrackSelector(trackSelector)
                    .build().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                                .setUsage(C.USAGE_MEDIA).build(), true
                        )
                        setHandleAudioBecomingNoisy(true)
                    }


                mPlayer?.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
                var arrayList: MutableList<MediaItem> = ArrayList()
                val trackList: ArrayList<Track> = ArrayList()
                for(item in mItem){
                    val track = Track()
                    track.title = item?.data?.head?.headData?.title
                    trackList.add(track)
                    var mimType= MimeTypes.BASE_TYPE_AUDIO
                    if(item?.data?.head?.headData?.misc?.url?.contains(".m3u8", true)!!){
                        mimType=MimeTypes.APPLICATION_M3U8
                    }else if(item?.data?.head?.headData?.misc?.url?.contains(".mp3", true)!!){
                        mimType=MimeTypes.BASE_TYPE_AUDIO
                    }else if(item?.data?.head?.headData?.misc?.url?.contains(".mpd", true)!!){
                        mimType=MimeTypes.APPLICATION_MPD
                    }else if(item?.data?.head?.headData?.misc?.url?.contains(".mp4",true)!!){
                        mimType=MimeTypes.APPLICATION_MP4
                    }


                    setLog("TAG", "video player mimType:${mimType} url:${item?.data?.head}")
                    setLog("chekingMovieRights " ,item.data.head.headData.misc.movierights.toString())

                    if(!TextUtils.isEmpty(item?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
                        val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
                        val mBuilder = MediaItem.Builder()

                        if(item?.data?.head?.headData?.misc?.sl!=null&&!TextUtils.isEmpty(item?.data?.head?.headData?.misc?.sl?.subtitle?.link)){
                            val subtitles = ArrayList<MediaItem.Subtitle>()
                            var subTitleMenuItem = MediaItem.Subtitle(
                                Uri.parse(item?.data?.head?.headData?.misc?.sl?.subtitle?.link),
                                MimeTypes.TEXT_VTT!!,
                                "en",
                                C.SELECTION_FLAG_DEFAULT
                            )
                            subtitles.add(subTitleMenuItem)
                            mBuilder.setSubtitles(subtitles)
                        }

                        mBuilder.setDrmUuid(drmSchemeUuid)
                        mBuilder.setDrmLicenseUri(item?.data?.head?.headData?.misc?.downloadLink?.drm?.token)
                        mBuilder.setUri(Uri.parse(item.data.head.headData.misc.url))
                        mBuilder.setMimeType(mimType)
//                    mBuilder.setUri(Uri.parse("http://85mum-content.hungama.com/1524/FF-2019-00000116/stream.mpd"))
//                    mBuilder.setDrmLicenseUri(Uri.parse("https://wv.service.expressplay.com/hms/wv/rights/?ExpressPlayToken=BQAAAA9JKckAJDM5NjQ4NTYyLTBjZmQtNGM2OS1hYTZiLWRhMWE0ZDVkZWY1ZgAAAGCYjkjr3sCIUcHl1mwIbffkdaqmsC2D-yMA_wTJVSM5WVeD3nF2nX2LIt4DW_XSH4jwuBIJjmH8zudsRS2YyZ8HXY-JRCVPEqBN3t68QqE81yYGahO_1ca_KtlZcLAmZvNIcRRBO4CrZ3tPKXEUlvneFmFPHA"))
//                mBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
                        mBuilder.setMediaMetadata(
                            MediaMetadata.Builder().setTitle(item?.data?.head?.headData?.title).build()
                        )

//                    if(CommonUtils.isPrerollAds() && item?.data?.head?.headData?.misc?.movierights?.contains(
//                            PlanNames.AVOD.name)){
//                        mBuilder?.setAdTagUri(""+Constant.AD_VIDEO_TEST)
//                    }
                        arrayList.add(mBuilder.build())
                        setLog("TAG", "video service setMediaItem: DRM content play "
                                + "\ntitle:${item?.data?.head?.headData?.title}"
                                + "\nurl:${item.data.head.headData.misc.url}"
                                +"\ntoken:${item?.data?.head?.headData?.misc?.downloadLink?.drm?.token}"
                                +"\ndrmSchemeUuid:${drmSchemeUuid}"
                        )
                        break
                    }
                    else{

                        val mBuilder = MediaItem.Builder()

                        if(item?.data?.head?.headData?.misc?.sl!=null&&!TextUtils.isEmpty(item?.data?.head?.headData?.misc?.sl?.subtitle?.link)){
                            val subtitles = ArrayList<MediaItem.Subtitle>()
                            var subTitleMenuItem = MediaItem.Subtitle(
                                Uri.parse(item?.data?.head?.headData?.misc?.sl?.subtitle?.link),
                                MimeTypes.TEXT_VTT!!,
                                "en",
                                C.SELECTION_FLAG_DEFAULT
                            )
                            subtitles.add(subTitleMenuItem)
                            mBuilder.setSubtitles(subtitles)
                        }

                        if(CommonUtils.isPrerollAds() && item.data.head.headData.misc.movierights.contains(
                                PlanNames.AVOD.name)){
//                            mBuilder.setAdTagUri(""+Constant.AD_VIDEO_TEST)
                            val addd = MediaItem.AdsConfiguration.Builder(Uri.parse(Constant.AD_VIDEO_TEST)).
                            setAdsId(Constant.AD_VIDEO_TEST).build()
                            mBuilder.setAdsConfiguration(addd)
                        }


                        mBuilder.setUri(Uri.parse(item.data.head.headData.misc.url))
                        mBuilder.setMediaMetadata(MediaMetadata.Builder().setTitle(item?.data?.head?.headData?.title).build())
                        mBuilder.setMimeType(mimType)
                        arrayList.add( mBuilder.build())
                    }


                }
                //setLog("VideoPlayerNotification","mediaMetadata-title-1-${arrayList.get(selectedTrackPosition).mediaMetadata.title}")
                val intent = intent1
                IntentUtil.addToIntent(arrayList, intent)
                val mediaItems = intent?.let { CommonUtils.createMediaItems(it, DemoUtil.getDownloadTracker( /* context= */this), trackList) }
                if (!mediaItems.isNullOrEmpty()){
                    arrayList = ArrayList()
                    arrayList.addAll(mediaItems)
                }
                //setLog("VideoPlayerNotification","mediaMetadata-title-2-${arrayList.get(selectedTrackPosition).mediaMetadata.title}")
                adsLoader?.setPlayer(mPlayer)
                mPlayer?.addListener(this)
                mPlayer?.playWhenReady = true
                if(arrayList.size>0 && mPlayer!=null){

                    setLog("TAG", "video player service startPlayer: arrayList:${arrayList}")
                    //setLog("VideoPlayerNotification","title-0-${arrayList.get(selectedTrackPosition).mediaMetadata.title}")
                    mPlayer?.setMediaItems(arrayList)
                    mPlayer?.seekTo(selectedTrackPosition, C.TIME_UNSET)
                    mPlayer?.prepare()

                }
                mPlayer?.setWakeMode(C.WAKE_MODE_NONE)

                val sessionActivityPendingIntent =
                    packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                        PendingIntent.getActivity(this, 1, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
                    }
                if (mediaSession == null){
                    mediaSession = MediaSession.Builder(this, mPlayer!!)
                        .setId(Constant.SESSION_ID_VIDEO)
                        .setSessionActivity(sessionActivityPendingIntent!!)
                        .build()
                }

                val token: MediaSessionCompat.Token = mediaSession?.sessionCompatToken as MediaSessionCompat.Token
                sessionToken?.sessionToken = token

                mPlayerNotificationManager = NotificationManagerCompat.from(this)

                if (shouldCreateNowPlayingChannel(mPlayerNotificationManager)) {
                    createNowPlayingChannel(mPlayerNotificationManager)
                }

                sessionToken.let {
                    val playerNotificationManager = PlayerNotificationManager.Builder(this@VideoPlayerService,
                        PLAYBACK_NOTIFICATION_ID,
                        PLAYBACK_CHANNEL_ID
                    ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                        //val controller = MediaControllerCompat(this@VideoPlayerService, mediaSession)

                        override fun getCurrentContentText(player: Player): String? {
                            //return mItem.get(0).subTitle
                            return mItem.get(player.currentWindowIndex).data?.head?.headData?.title
                        }

                        override fun getCurrentContentTitle(player: Player): String {
                            //return mItem.get(0).title ?: "Buffering..."
                            return mItem.get(player.currentWindowIndex).data?.head?.headData?.title ?: "Buffering..."
                        }

                        override fun getCurrentLargeIcon(
                            player: Player,
                            callback: PlayerNotificationManager.BitmapCallback
                        ): Bitmap? {
                            //return controller.metadata?.description?.iconBitmap
                            val imageURL=mItem.get(player.currentWindowIndex).data?.head?.headData?.image!!
                            GlobalScope.launch(Dispatchers.IO){
                                loadAlbumArtUri(context,imageURL, object : OnBitmapLoadedListener {
                                    override fun onBitmapLoaded(resource: Bitmap) {
                                        callback.onBitmap(resource)
                                    }

                                    override fun onBitmapLoadingFailed() {
                                        callback.onBitmap(
                                            AppCompatResources.getDrawable(
                                                context,
                                                R.drawable.transparent_image_png
                                            )!!.toBitmap()
                                        )
                                    }

                                })
                            }

                            return BitmapFactory.decodeResource(context.resources, R.drawable.placeholder_png)
                        }

                        override fun createCurrentContentIntent(player: Player): PendingIntent? {
                            return PendingIntent.getActivity(
                                this@VideoPlayerService,
                                0,
                                Intent(this@VideoPlayerService, MainActivity::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                    })
                        .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                            override fun onNotificationPosted(
                                notificationId: Int,
                                notification: Notification,
                                ongoing: Boolean
                            ) {
//                            if (ongoing) {
//                                ContextCompat.startForegroundService(
//                                    applicationContext,
//                                    Intent(applicationContext, this@VideoPlayerService.javaClass)
//                                )
//                                startForeground(PLAYBACK_NOTIFICATION_ID, notification)
//                            } else {
//                                stopForeground(false)
//                            }
                                Log.d("TAG", "onNotificationPosted: called ${Thread.currentThread().name}")
                                startForeground(notificationId, notification)
                            }

                            override fun onNotificationCancelled(
                                notificationId: Int,
                                dismissedByUser: Boolean
                            ) {
                                stopForeground(true)
                            }
                        }).build()
                    playerNotificationManager.setPriority(NotificationCompat.PRIORITY_LOW)
                    playerNotificationManager.setPlayer(mPlayer)
                    playerNotificationManager.setMediaSessionToken(mediaSession?.sessionCompatToken as MediaSessionCompat.Token)
                }

                /*MediaSessionConnector(mediaSession!!).also {
                    it.setPlayer(mPlayer)
                    it.setPlaybackPreparer(PlaybackPreparer(mPlayer!!))
                }*/
            }
        }catch (e:Exception){
            throw e
        }
    }


    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //updateWidget(playWhenReady)
    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onSeekProcessed() {

    }

    inner class LocalBinder : Binder() {
        val service: VideoPlayerService
            get() = this@VideoPlayerService
    }

    private fun shouldCreateNowPlayingChannel(notificationManager: NotificationManagerCompat) =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists(
                    notificationManager
            )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists(notificationManager: NotificationManagerCompat) =
            notificationManager.getNotificationChannel(PLAYBACK_CHANNEL_ID) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel(notificationManager: NotificationManagerCompat) {
        try {
            val notificationChannel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                Constant.PLAYBACK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =  Constant.PLAYBACK_CHANNEL_NAME
            }
            notificationChannel?.setSound(null,null)
            notificationManager.createNotificationChannel(notificationChannel)
        }catch (e:Exception){

        }
    }

    fun playOrPause(url: String) {
        if (mPlayer!=null) {
            if (!mPlayer?.isPlaying!!) {
                mPlayer?.play()
            }else{
                mPlayer?.pause()
            }

        } else {
            startPlayer()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        setLog("TAG", "onPlayerError: message: ${error.message}")
    }
}
