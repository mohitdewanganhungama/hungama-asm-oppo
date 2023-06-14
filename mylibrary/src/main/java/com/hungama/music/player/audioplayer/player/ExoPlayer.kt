package com.hungama.music.player.audioplayer.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.annotation.OptIn
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.util.EventLogger
import androidx.recyclerview.widget.DiffUtil
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.player.audioplayer.mediautils.MediaSessionManager
import com.hungama.music.player.audioplayer.mediautils.MediaSourceDiffUtilCallback
import com.hungama.music.player.audioplayer.mediautils.MediaSourceListUpdateCallback
import com.hungama.music.player.audioplayer.mediautils.NotificationManager
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.Track_State
import com.hungama.music.player.audioplayer.queue.NowPlayingInfo
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.copyFile
import com.hungama.music.utils.CommonUtils.decryptAudioContent
import com.hungama.music.utils.CommonUtils.getDownloadedContent
import com.hungama.music.utils.CommonUtils.isFilePath
import com.hungama.music.utils.CommonUtils.removeItemsFromFirst
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.totalQueueSize
import com.hungama.music.utils.download.Data
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

@OptIn(UnstableApi::class)
class ExoPlayer : AudioPlayer,
    CoroutineScope {

    lateinit var context: Context
    lateinit var simpleExoPlayer: SimpleExoPlayer
    lateinit var nowPlayingQueue: NowPlayingQueue
    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var concatenatingMediaSource: ConcatenatingMediaSource
    var mediaSessionManager: MediaSessionManager? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var playerEventListener: Player.Listener
    private val job = Job()
    override val coroutineContext: CoroutineContext = (Dispatchers.IO + job)
    private val TAG: String = ExoPlayer::class.java.name
    val arrayList: ArrayList<MediaItem> = ArrayList()
    private var dataSourceFactory2: DataSource.Factory? = null
    val mainLooper = Looper.getMainLooper()

    public fun getConcatenatingMediaSourceFromPlayer(): ConcatenatingMediaSource? {
        if(concatenatingMediaSource!=null){
            return concatenatingMediaSource!!
        }
        return null

    }

    override fun init(context: Context) {
        val trackSelector = DefaultTrackSelector(context)
        this.context = context

        setLog("init","init->context:${context}")

//        val exoPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(context!!).build()
//        simpleExoPlayer= exoPlayer as SimpleExoPlayer
        simpleExoPlayer = SimpleExoPlayer.Builder(context, DefaultRenderersFactory(context)).build()
        dataSourceFactory2 = DemoUtil.getDataSourceFactory( /* context= */context)
        /*val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(
            dataSourceFactory2!!
        )*/
        /*dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name))
        )*/
        dataSourceFactory = DefaultDataSourceFactory(context, dataSourceFactory2!!)

        simpleExoPlayer.addAnalyticsListener(EventLogger(trackSelector))

        concatenatingMediaSource = ConcatenatingMediaSource()

        nowPlayingQueue = NowPlayingQueue()

        mediaSessionManager = MediaSessionManager(context, simpleExoPlayer)

    }

    override fun getMediaSession(): MediaSessionManager? {
        return mediaSessionManager
    }

    override fun setNotificationPostedListener(listener: NotificationManager.OnNotificationPostedListener) {
        notificationManager =
            mediaSessionManager?.mediaSession?.let {
                NotificationManager(
                    context,
                    it,
                    simpleExoPlayer,
                    nowPlayingQueue,
                    listener
                )
            }!!
    }

    override fun setPlayerEventListener(listener: Player.Listener) {
        playerEventListener = listener
        simpleExoPlayer.addListener(playerEventListener)
    }

    override fun setMediaItem(track: Track, index: Int): MediaItem {
        var offlineSongId = 0L
        var offlineSongOriginalUrl = ""
        try {
            val downloadedContent = getDownloadedContent(track?.id.toString())
            if (downloadedContent != null){
                offlineSongId = track.id
                offlineSongOriginalUrl = downloadedContent.downloadUrl.toString()
            }
        }catch (e:Exception){

        }

        var mimType = MimeTypes.BASE_TYPE_AUDIO
        if (track.url?.contains(".m3u8", true)!! || (offlineSongId == track.id && !TextUtils.isEmpty(offlineSongOriginalUrl) && offlineSongOriginalUrl?.contains(".m3u8", true)!!)) {
            //setLog("FileDecryption", "decryptAudioContent-setMediaItem-m3u8---track.url-${track.url}   offlineSongId-$offlineSongId   offlineSongOriginalUrl-$offlineSongOriginalUrl")
            mimType = MimeTypes.APPLICATION_M3U8
        } else if (track.url?.contains(".mp3", true)!!) {
            mimType = MimeTypes.BASE_TYPE_AUDIO
        } else if (track.url?.contains(".mpd", true)!! || (offlineSongId == track.id && !TextUtils.isEmpty(offlineSongOriginalUrl) && offlineSongOriginalUrl?.contains(".mpd", true)!!)){
            //setLog("FileDecryption", "decryptAudioContent-setMediaItem-mpd---track.url-${track.url}   offlineSongId-$offlineSongId   offlineSongOriginalUrl-$offlineSongOriginalUrl")
            mimType = MimeTypes.APPLICATION_MPD
        } else if (track.url?.contains(".mp4", true)!!) {
            mimType = MimeTypes.APPLICATION_MP4
        }

       

        if (!TextUtils.isEmpty(track.drmlicence)){
            val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
            val mediaItem = MediaItem.Builder()
                .setDrmUuid(drmSchemeUuid)
                .setDrmLicenseUri(track.drmlicence)
                .setUri(Uri.parse(track.url))
                .setMediaMetadata(MediaMetadata.Builder().setTitle(track.title).build())
                .setMimeType(mimType)
                .setTag(NowPlayingInfo(track.id, index))
                .build()

            //setLog(TAG, "exoplayer setMediaItem: DRM content play url:${track.url}")
            return mediaItem
        }else{
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(track.url))
                .setMediaMetadata(MediaMetadata.Builder().setTitle(track.title).build())
                .setMimeType(mimType)
                .setTag(NowPlayingInfo(track.id, index))
                .build()

            //setLog(TAG, "setMediaItem: Non DRM content play url:${track.url}")
            return mediaItem
        }
    }

    override fun playTracks(
        context: Context,
        tracksList: MutableList<Track>,
        selectedTrackPosition: Int,
        shuffle: Boolean,
        playStartPosition:Long,
        isQueueItem: Boolean,
        isPause: Boolean
    ) {

        launch {

            var currentItemPlayStartPosition=playStartPosition;
            val updatedSelectedTrackPosition = prepareMediaSource(tracksList, selectedTrackPosition, shuffle, isQueueItem)
            setLog(TAG, "playTracks")
            withContext(Dispatchers.Main) {
                simpleExoPlayer.setWakeMode(C.WAKE_MODE_NETWORK)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()
                simpleExoPlayer.setAudioAttributes(audioAttributes, true)

                simpleExoPlayer.prepare(concatenatingMediaSource)

                //simpleExoPlayer.setMediaItems(arrayList)
                //simpleExoPlayer.prepare()
                if(isPause){
                    simpleExoPlayer.playWhenReady = false
                }else{
                    simpleExoPlayer.playWhenReady = true
                }
                if (updatedSelectedTrackPosition != -1)
                    //simpleExoPlayer.seekTo(selectedTrackPosition, C.TIME_UNSET)
                        try {

                            if(currentItemPlayStartPosition<=0){
                                if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0)
                                {
                                    if (!nowPlayingQueue.nowPlayingTracksList.isNullOrEmpty() && nowPlayingQueue.nowPlayingTracksList.size > updatedSelectedTrackPosition){
                                        val track=nowPlayingQueue.nowPlayingTracksList?.get(updatedSelectedTrackPosition)
                                        setLog(TAG, "playTracks track title:${track?.title} id:${track?.id} parentId:${track?.parentId}")
                                        if(track!=null&&track?.id!=null&&track?.id!!>0 && track.contentType == ContentTypes.PODCAST.value){
                                            HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                                                setLog(TAG, "getContinueWhereLeftData track title:${it?.data?.title} id:${it?.data?.id}")

                                                if(it?.data?.id!!.contains(""+track?.id)){
                                                    if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                                        currentItemPlayStartPosition=TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!)
                                                    }
                                                    return@forEach
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            simpleExoPlayer.seekTo(updatedSelectedTrackPosition, currentItemPlayStartPosition)
                        }catch (e:IllegalSeekPositionException){
                            //simpleExoPlayer.seekTo(updatedSelectedTrackPosition, C.TIME_UNSET)
                        }catch (e:Exception){

                        }

                setLog("NotificationManager", "onPlayerStateChanged-100-playerPlaybackStatus-${simpleExoPlayer.playbackState} isPause-$isPause")
                updateExternalMetadata(context)
            }
        }
    }



    override fun addTrackToQueue(context: Context, track: Track) {

        launch {
            nowPlayingQueue.addTrackToQueue(track)

            val trackIndex = nowPlayingQueue.nowPlayingTracksList.size - 1

            val uri: Uri = Uri.parse(track.url)
            val mediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    //.setTag(NowPlayingInfo(track.id.toLong(), trackIndex))
                    .createMediaSource(setMediaItem(track, trackIndex))

            concatenatingMediaSource.addMediaSource(mediaSource)

            withContext(Dispatchers.Main) {

                updateExternalMetadata(context)
            }

        }

    }

    override fun addTrackToQueue(context: Context, trackList: ArrayList<Track>) {
        launch {
            nowPlayingQueue.addTrackToQueue(trackList)

            trackList.forEachIndexed { index, track ->
                var mediaSource: MediaSource? = null

                if (track.url?.contains(".m3u8", true)!!) {
                    mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                        //.setTag(NowPlayingInfo(track.id, index))
                        .createMediaSource(setMediaItem(track, index))
                } else if (track.url?.contains(".mpd", true)!!){
                    mediaSource = DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(setMediaItem(track, index))
                } else {
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            //.setTag(NowPlayingInfo(track.id, index))
                            //.createMediaSource(uri)
                            .createMediaSource(setMediaItem(track, index))
                    //setLog("Track-Data", "${track.id}" + " -$index")
                }

                concatenatingMediaSource.addMediaSource(mediaSource)
            }

            withContext(Dispatchers.Main) {

                updateExternalMetadata(context)
            }

        }
    }


    override fun playNext(context: Context, track: Track) {

        CoroutineScope(Dispatchers.IO).launch {
            if (nowPlayingQueue != null && track != null)
            {
                nowPlayingQueue.playNext(track)
            }

            val trackIndex = nowPlayingQueue.currentPlayingTrackIndex + 1

            var mediaSource:MediaSource? = null

            if(track.url?.contains(".m3u8", true)!!){
                mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    //.setTag(NowPlayingInfo(track.id?.toLong(), trackIndex))
                    .createMediaSource(setMediaItem(track, trackIndex))
            }else if (track.url?.contains(".mpd", true)!!){
                mediaSource = DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(setMediaItem(track, trackIndex))
            } else{
                mediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        //.setTag(NowPlayingInfo(track.id, trackIndex))
                        .createMediaSource(setMediaItem(track, trackIndex))
            }

            withContext(Dispatchers.Main) {
                simpleExoPlayer.currentMediaItem?.let {
                    if (simpleExoPlayer.currentMediaItem?.playbackProperties != null && simpleExoPlayer.currentMediaItem?.playbackProperties?.tag != null) {
                        val nowPlayingInfo = simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
                        concatenatingMediaSource.addMediaSource(nowPlayingInfo.index + 1, mediaSource)
                    }
                }
                updateExternalMetadata(context)
            }
        }

    }

    override fun updateTracks(context: Context, tracksList: MutableList<Track>) {

        launch {
            val mediaSourceDiffUtilCallback =
                MediaSourceDiffUtilCallback(
                    nowPlayingQueue.nowPlayingTracksList,
                    tracksList
                )
            val diffResult = DiffUtil.calculateDiff(mediaSourceDiffUtilCallback)

            diffResult.dispatchUpdatesTo(
                MediaSourceListUpdateCallback(concatenatingMediaSource)
            )

            nowPlayingQueue.setupQueue(tracksList, nowPlayingQueue.shuffleEnabled, true)

            withContext(Dispatchers.Main) {

                updateExternalMetadata(context)
            }
        }

    }

    private fun prepareMediaSource(
        tracksList: MutableList<Track>,
        selectedTrackPosition: Int,
        shuffle: Boolean,
        isQueueItem: Boolean
    ): Int {
        val tracksPlaylist = mutableListOf<Track>()
        var updatedSelectedTrackPosition = selectedTrackPosition
        try {
            //setLog("updatedSelectedTrackPosition", "updatedSelectedTrackPosition-first-$updatedSelectedTrackPosition")
            if (!isQueueItem){
                // TODO : Set Queue size-100 Logic start - Removed content from queue if queue size is grater then 100
                setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-Before Remove-nowPlayingTracksList.size-"+nowPlayingQueue.nowPlayingTracksList.size)
                setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-trackListSize-${tracksList.size}")
                val nowPlayingTrackListSize = nowPlayingQueue.nowPlayingTracksList.size
                val trackListSize = tracksList.size
                val totalContentSize = nowPlayingTrackListSize + trackListSize
                var removableContentSize = 0
                val totalQueueSize = totalQueueSize
                setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-totalQueueSize-$totalQueueSize")
                setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-totalContentSize-$totalContentSize")
                if (totalContentSize > totalQueueSize){
                    if (trackListSize >= totalQueueSize){
                        //delete full nowPlayingTrackList
                        removableContentSize = nowPlayingTrackListSize
                    }else{
                        //delete removableContentSize content from nowPlayingTrackList
                        val allowedHistoryItemsSize = totalQueueSize - trackListSize
                        setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-allowedHistoryItemsSize-$allowedHistoryItemsSize")
                        if (allowedHistoryItemsSize < nowPlayingTrackListSize){
                            removableContentSize = nowPlayingTrackListSize - allowedHistoryItemsSize
                        }else{
                            removableContentSize = nowPlayingTrackListSize
                        }
                    }
                }
                setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-removableContentSize-$removableContentSize")
                if (removableContentSize >= nowPlayingTrackListSize){
                    nowPlayingQueue.nowPlayingTracksList = mutableListOf<Track>()
                }else if (removableContentSize > 0){
                    nowPlayingQueue.nowPlayingTracksList.removeItemsFromFirst(removableContentSize)
                }
                setLog("queue100Logic", "ExoPlayer-prepareMediaSource()-After Remove-nowPlayingTracksList.size-"+nowPlayingQueue.nowPlayingTracksList.size)
                // TODO : Set Queue size-100 Logic end

                try {
                    nowPlayingQueue.nowPlayingTracksList.mapIndexed { index, track ->
                        track.state = Track_State.PLAYED
                    }
                }catch (e:Exception){

                }


                updatedSelectedTrackPosition = nowPlayingQueue.nowPlayingTracksList.size
                //setLog("updatedSelectedTrackPosition", "updatedSelectedTrackPosition-second-$updatedSelectedTrackPosition")
                nowPlayingQueue.nowPlayingTracksList.addAll(tracksList)
                nowPlayingQueue.keepUnShuffledTracks(nowPlayingQueue.nowPlayingTracksList)
            }else{
                nowPlayingQueue.keepUnShuffledTracks(tracksList)
                nowPlayingQueue.setNowPlayingTracks(tracksList)
            }
            val newFileName = Constant.filePrefix+Data.getSaveAudioDir(context)+"temp.cache"
            var offlineSongId = 0L
            var offlineSongOriginalUrl = ""
            try {
                val isOfflinePlay =
                    CommonUtils.isContentDownloaded(nowPlayingQueue.nowPlayingTracksList as ArrayList<Track>, updatedSelectedTrackPosition)
                if (isOfflinePlay && !TextUtils.isEmpty(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url) &&
                    isFilePath(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url.toString())){

                    decryptAudioContent(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url.toString(), newFileName)
                    val downloadedContent = getDownloadedContent(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].id.toString())
                    if (downloadedContent != null){
                        offlineSongId = nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].id
                        offlineSongOriginalUrl = downloadedContent.downloadUrl.toString()
                    }
                    Thread.sleep(1000)
                    nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url = newFileName

                }else if (isOfflinePlay){
                    val downloadedContent = getDownloadedContent(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].id.toString())
                    if (downloadedContent != null && !TextUtils.isEmpty(downloadedContent.downloadedFilePath) &&
                        isFilePath(downloadedContent.downloadedFilePath)){

                        decryptAudioContent(downloadedContent.downloadedFilePath, newFileName)
                        offlineSongId = nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].id
                        offlineSongOriginalUrl = downloadedContent.downloadUrl.toString()
                        Thread.sleep(1000)
                        nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url = newFileName

                    }
                }

            }
            catch (e:Exception){

            }
            catch (e:NoSuchFileException){
                if (!TextUtils.isEmpty(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url)){
                    copyFile(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url.toString(), newFileName.toString())
                    nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].url = newFileName
                    val downloadedContent = getDownloadedContent(nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].id.toString())
                    if (downloadedContent != null){
                        offlineSongId = nowPlayingQueue.nowPlayingTracksList[updatedSelectedTrackPosition].id
                        offlineSongOriginalUrl = downloadedContent.downloadUrl.toString()
                    }
                }
            }



            tracksPlaylist.addAll(nowPlayingQueue.getNowPlayingTracks())
            //setLog("updatedSelectedTrackPosition", "updatedSelectedTrackPosition-third-$updatedSelectedTrackPosition")
            if (updatedSelectedTrackPosition != -1) {
                tracksPlaylist.mapIndexed { index, track ->
                    try {
                        if (track != null){
                            when {
                                index == updatedSelectedTrackPosition -> track.state = Track_State.PLAYING
                                index < updatedSelectedTrackPosition -> track.state = Track_State.PLAYED
                                else -> track.state = Track_State.IN_QUEUE
                            }
                        }
                    }catch (e:Exception){

                    }
                }
                //setLog("updatedSelectedTrackPosition", "current track.state-${tracksPlaylist?.get(updatedSelectedTrackPosition).state}")
                nowPlayingQueue.currentPlayingTrackIndex = updatedSelectedTrackPosition
                BaseActivity.updateNowPlayingCurrentIndex(updatedSelectedTrackPosition)
                if (BaseActivity.lastSongPlayDuration >= CommonUtils.getAudioAdPreference().minPlayDurationSeconds){
                    BaseActivity.totalSongsPlayedAfterLastAudioAd += 1
                    BaseActivity.lastSongPlayDuration = 0
                    setLog("lastSongPlayDuration-exo", BaseActivity.lastSongPlayDuration.toString())
                    setLog("lastSongPlayDuration-exo", BaseActivity.totalSongsPlayedAfterLastAudioAd.toString())
                }else{
                    setLog("lastSongPlayDuration-exo", BaseActivity.lastSongPlayDuration.toString())
                }
            }


            concatenatingMediaSource.clear()

            tracksPlaylist.forEachIndexed { index, track ->
                //arrayList.add(setMediaItem(track))
                var mediaSource: MediaSource? = null

                if (track.url?.contains(".m3u8", true)!! || (offlineSongId == track.id && !TextUtils.isEmpty(offlineSongOriginalUrl) && offlineSongOriginalUrl?.contains(".m3u8", true)!!)) {
                    setLog("FileDecryption", "decryptAudioContent-m3u8---track.url-${track.url}   offlineSongId-$offlineSongId   offlineSongOriginalUrl-$offlineSongOriginalUrl")
                    mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                        //.setTag(NowPlayingInfo(track.id, index))
                        .createMediaSource(setMediaItem(track, index))
                } else if (track.url?.contains(".mpd", true)!! || (offlineSongId == track.id && !TextUtils.isEmpty(offlineSongOriginalUrl) && offlineSongOriginalUrl?.contains(".mpd", true)!!)){
                    setLog("FileDecryption", "decryptAudioContent-mpd---track.url-${track.url}   offlineSongId-$offlineSongId   offlineSongOriginalUrl-$offlineSongOriginalUrl")
                    mediaSource = DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(setMediaItem(track, index))
                } else {
                    mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            //.setTag(NowPlayingInfo(track.id, index))
                            //.createMediaSource(uri)
                            .createMediaSource(setMediaItem(track, index))
                    //setLog("Track-Data", "${track.id}" + " -$index")
                }


                /*val textFormat = Format.createTextSampleFormat(
                    null, MimeTypes.APPLICATION_SUBRIP,
                    Format.NO_VALUE, "hi"
                )*/
                /*val subtitle: MediaItem.Subtitle = MediaItem.Subtitle(
                    Uri.parse("https://storage.googleapis.com/exoplayer-test-media-1/webvtt/numeric-lines.vtt"),
                    MimeTypes.TEXT_VTT,  // The correct MIME type.
                    "en",  // The subtitle language. May be null.
                    C.SELECTION_FLAG_DEFAULT
                ) // Selection flags for the track.

                //val subtitleSource1 = SingleSampleMediaSource(track.url, dataSourceFactory, textFormat, C.TIME_UNSET)
                val subtitleSource:MediaSource = SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(subtitle,
                    C.TIME_UNSET
                )
                val mergedSource = MergingMediaSource(mediaSource, subtitleSource)*/
                concatenatingMediaSource.addMediaSource(mediaSource)
            }
            nowPlayingQueue.setupQueue(tracksPlaylist, shuffle, false)
        }catch (e:Exception){

        }

        return updatedSelectedTrackPosition
    }

    private fun updateExternalMetadata(context: Context) {
        CoroutineScope(Dispatchers.Main).launch{
            CommonUtils.setLog(
                "NotificationManager",
                "updateExternalMetadata-101-setupPlayerNotification "
            )
            //notificationManager.cleanup()
            //notificationManager.setupPlayerNotification(context, nowPlayingQueue)
            //mediaSessionManager.setupMediaSessionConnector(context, nowPlayingQueue)
        }

    }

    override fun cleanup() {
        try {
            job.cancel()
        }catch (e:Exception){

        }
        try {
            mediaSessionManager?.cleanup()
            //notificationManager.cleanup()
        }catch (e:Exception){

        }
        try {
            if (simpleExoPlayer !=  null)
            simpleExoPlayer.stop()
            simpleExoPlayer.release()
            simpleExoPlayer.removeListener(playerEventListener)
        }catch (e:Exception){

        }
    }

    override fun setMediaItem(trackList: ArrayList<Track>): List<MediaItem> {
        trackList.forEachIndexed { index, track ->
            arrayList.add(setMediaItem(track, index))
        }
        /*for (track in trackList){
            arrayList.add(setMediaItem(track))
        }*/
        return arrayList
    }

    override fun playPrevious(context: Context, track: Track) {

        launch {
            nowPlayingQueue.playNext(track)

            val trackIndex = nowPlayingQueue.currentPlayingTrackIndex - 1

            val uri: Uri = Uri.parse(track.url)
            /*val mediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                            .setTag(NowPlayingInfo(track.id, trackIndex))
                            .createMediaSource(uri)*/
            var mediaSource:MediaSource? = null

            if(track.url?.contains(".m3u8", true)!!){
                mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    //.setTag(NowPlayingInfo(track.id, trackIndex))
                    .createMediaSource(setMediaItem(track, trackIndex))
            }else if (track.url?.contains(".mpd", true)!!){
                mediaSource = DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(setMediaItem(track, trackIndex))
            } else{
                mediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        //.setTag(NowPlayingInfo(track.id, trackIndex))
                        //.createMediaSource(uri)
                        .createMediaSource(setMediaItem(track, trackIndex))
            }

            //simpleExoPlayer.setThrowsWhenUsingWrongThread(false)
            val nowPlayingInfo = simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo

            concatenatingMediaSource.addMediaSource(nowPlayingInfo.index - 1, mediaSource)

            withContext(Dispatchers.Main) {

                updateExternalMetadata(context)
            }
        }

    }

    override fun notificationCleanup() {
        //mediaSessionManager.cleanup()
        //notificationManager.cleanup()

        //simpleExoPlayer.stop()
        //simpleExoPlayer.release()
        //simpleExoPlayer.removeListener(playerEventListener)
    }

    override fun setShuffleMode(context: Context, shuffle: Boolean) {

        GlobalScope.launch {
            val tracksList: ArrayList<Track> = ArrayList()

            tracksList.addAll(nowPlayingQueue.trackListUnShuffled)


            if (shuffle) {
                val historyTrackList:ArrayList<Track> = ArrayList()
                val nowPlayingTrackList:ArrayList<Track> = ArrayList()
                val inQueueTrackList:ArrayList<Track> = ArrayList()
                for (track in tracksList){
                    if (track.state == Track_State.PLAYED){
                        historyTrackList.add(track)
                    }else if (track.state == Track_State.PLAYING || track.state == Track_State.PAUSED){
                        nowPlayingTrackList.add(track)
                    }else if (track.state == Track_State.IN_QUEUE){
                        inQueueTrackList.add(track)
                    }
                }
                inQueueTrackList.shuffle()
                tracksList.clear()
                if (historyTrackList.size > 0){
                    tracksList.addAll(historyTrackList)
                }
                if (nowPlayingTrackList.size > 0){
                    tracksList.addAll(nowPlayingTrackList)
                }
                if (inQueueTrackList.size > 0){
                    tracksList.addAll(inQueueTrackList)
                }



                /*tracksList.shuffle()
                val newTrackList:ArrayList<Track> = ArrayList()
                newTrackList.addAll(tracksList)
                tracksList.clear()
                tracksList.addAll(newTrackList.sortedBy { it.state.value })*/
            }
            Handler(mainLooper).post {
                try {
                    val nowPlayingInfo = simpleExoPlayer.currentMediaItem?.playbackProperties?.tag as NowPlayingInfo
                    //var nowPlayingItemIndex = simpleExoPlayer.currentWindowIndex

                    var tempIndex = -1
                    tracksList.forEachIndexed { index, track ->
                        if (nowPlayingInfo.id == track.id) {

                            tempIndex = index
                            return@forEachIndexed
                        }
                    }

                    if (shuffle) {
                        tempIndex = if (tempIndex != -1) tempIndex else 0

                        tracksList.mapIndexed { index, track ->
                            when {
                                index == tempIndex -> track.state = Track_State.PLAYING
                                index < tempIndex -> track.state = Track_State.PLAYED
                                else -> track.state = Track_State.IN_QUEUE
                            }
                        }

                        //Collections.rotate(tracksList, 0 - tempIndex)

                        /*tracksList.mapIndexed { index, track ->
                            when {
                                index == 0 -> track.state = Track_State.PLAYING
                                else -> track.state = Track_State.IN_QUEUE
                            }
                        }*/
                    } else {
                        tempIndex = if (tempIndex != -1) tempIndex else 0

                        tracksList.mapIndexed { index, track ->
                            when {
                                index == tempIndex -> track.state = Track_State.PLAYING
                                index < tempIndex -> track.state = Track_State.PLAYED
                                else -> track.state = Track_State.IN_QUEUE
                            }
                        }
                    }

                    val mediaSourceDiffUtilCallback =
                        MediaSourceDiffUtilCallback(
                            nowPlayingQueue.nowPlayingTracksList,
                            tracksList
                        )
                    val diffResult = DiffUtil.calculateDiff(mediaSourceDiffUtilCallback)

                    diffResult.dispatchUpdatesTo(MediaSourceListUpdateCallback(concatenatingMediaSource))

                    nowPlayingQueue.setupQueue(tracksList, shuffle, false)
                }catch (e:Exception){

                }
            }
            withContext(Dispatchers.Main) {

                updateExternalMetadata(context)
                nowPlayingQueue.updateUpComingNextPlayingQueue()
            }
        }
    }

    override fun showNotification(simpleExoPlayer: SimpleExoPlayer) {
        //notificationManager.showNotificationForPlayer(simpleExoPlayer)
    }

    override fun hideNotification(){
        //notificationManager.hideNotification()
    }

    override fun updateTrackData(context: Context, track: Track) {
        GlobalScope.launch {
            nowPlayingQueue.updateTrack(track)
            setLog("preCatchContent", "ExoPlayer-updateTrackData-track.url-${track.url}")
            var trackIndex = nowPlayingQueue.currentPlayingTrackIndex + 1
            if (!nowPlayingQueue.getNowPlayingTracks().isNullOrEmpty() && nowPlayingQueue.getNowPlayingTracks().size > BaseActivity.nowPlayingCurrentIndex()){
                run loop@{
                    nowPlayingQueue.getNowPlayingTracks().forEachIndexed { index, item ->
                        if (nowPlayingQueue.getNowPlayingTracks().size > BaseActivity.nowPlayingCurrentIndex() && nowPlayingQueue.getNowPlayingTracks().get(BaseActivity.nowPlayingCurrentIndex()).id != track.id && item.id == track.id){
                            trackIndex = index
                            setLog("preCatchContent", "ExoPlayer-updateTrackData-IF-true-Return")
                            return@loop
                        }
                    }
                }
            }


            var mediaSource:MediaSource? = null
            if(track.url?.contains(".m3u8", true)!!){
                mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    //.setTag(NowPlayingInfo(track.id?.toLong(), trackIndex))
                    .createMediaSource(setMediaItem(track, trackIndex))
            }else if (track.url?.contains(".mpd", true)!!){
                mediaSource = DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(setMediaItem(track, trackIndex))
            } else{
                mediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        //.setTag(NowPlayingInfo(track.id, trackIndex))
                        .createMediaSource(setMediaItem(track, trackIndex))
            }
            Handler(mainLooper).post {
                try {
                    var updateMediaSorceIndex = 0
                    var isIndexfound = false
                    setLog("preCatchContent", "ExoPlayer-updateTrackData-concatenatingMediaSource.size-${concatenatingMediaSource.size}  updateMediaSorceIndex-$updateMediaSorceIndex  isIndexfound-$isIndexfound")
                    run loop@{
                        for (i in 0..concatenatingMediaSource.size){
                            setLog("preCatchContent", "ExoPlayer-updateTrackData-for-concatenatingMediaSource.size-${concatenatingMediaSource.size}  i-$i  updateMediaSorceIndex-$updateMediaSorceIndex  isIndexfound-$isIndexfound")
                            if (concatenatingMediaSource.size > i && !isIndexfound){
                                val npi = concatenatingMediaSource.getMediaSource(i).mediaItem.playbackProperties?.tag as NowPlayingInfo
                                //setLog("preCatchContent", "ExoPlayer-updateTrackData-for-npi.id-${npi.id}  track.id-${track.id}")
                                if (npi.id == track.id){
                                    updateMediaSorceIndex = npi.index
                                    isIndexfound = true
                                    setLog("preCatchContent", "ExoPlayer-updateTrackData-for-updateMediaSorceIndex-$updateMediaSorceIndex  isIndexfound-$isIndexfound")
                                    return@loop
                                }
                            }
                        }
                    }

                    setLog("preCatchContent", "ExoPlayer-updateTrackData-for-updateMediaSorceIndex-$updateMediaSorceIndex  isIndexfound-$isIndexfound")
                    if (isIndexfound){
                        concatenatingMediaSource.removeMediaSource(updateMediaSorceIndex)
                        concatenatingMediaSource.addMediaSource(updateMediaSorceIndex, mediaSource)
                    }
                }catch (e:Exception){
                    return@post
                }

            }
        }
    }
}