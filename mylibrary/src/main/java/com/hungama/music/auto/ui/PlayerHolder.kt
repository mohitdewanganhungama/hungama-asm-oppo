//package com.hungama.music.auto.ui
//
//import android.app.Notification
//import android.content.Context
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED
//import android.util.Log
//import androidx.annotation.OptIn
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.SimpleExoPlayer
//import androidx.media3.ui.PlayerNotificationManager
//import com.hungama.music.auto.CUSTOM_ACTION_FAVORITE
//import com.hungama.music.auto.NotificationManager
//import com.hungama.music.auto.channel.Channel
//import com.hungama.music.R
//
//@OptIn(UnstableApi::class)
//class PlayerHolder(private val context: Context,
//                   private val session: MediaSessionCompat,
//                   private val favoritesHelper: RecentPlayedHelper) : Player.Listener {
//
//    private var player: SimpleExoPlayer? = null
//    private var currentState = STATE_STOPPED
//
//    private var isForegroundService = false
//    private lateinit var notificationManager: NotificationManager
//    //protected lateinit var mediaSessionConnector: MediaSessionConnector
//
//    companion object {
//        var currentChannel: Channel? = null
//    }
//
//    fun createPlayer() {
//        setPlaybackState(STATE_STOPPED, true)
//        player = SimpleExoPlayer.Builder(context).build()
//        player?.addListener(this)
//
//        // ExoPlayer will manage the MediaSession for us.
////        mediaSessionConnector = MediaSessionConnector(session)
////        mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
////        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(session))
//
////        notificationManager = NotificationManager(
////            context,
////            session?.sessionToken,
////            PlayerNotificationListener()
////        )
////
////        notificationManager.showNotificationForPlayer(player!!)
//
////        mediaSessionConnector.setPlayer(player)
//    }
//
//    fun startPlaying(channel: Channel) {
//        currentChannel = channel
//
//        val metadataList= ArrayList<MediaMetadataCompat>()
//        val currentMediaMetadataCompat=MediaMetadataCompat.Builder()
//            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentChannel?.subtitle)
//            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentChannel?.title)
//            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "${currentChannel?.description}")
//            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,currentChannel?.imageRes)
//            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,currentChannel?.mediaURL)
//            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,currentChannel?.mediaId)
//            .build()
//        metadataList.add(currentMediaMetadataCompat)
//
//        AutoHungamaMusicService.currentChannelList?.forEach {
//            if(currentChannel?.parentID==it?.parentID){
//                val mediaMetadataCompat=MediaMetadataCompat.Builder()
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, it?.subtitle)
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, it?.title)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "${it.description}")
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,it.imageRes)
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,it.mediaURL)
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,it.mediaId)
//                    .build()
//                metadataList?.add(mediaMetadataCompat)
//            }
//        }
//
//
//
//        val initialWindowIndex = if (currentMediaMetadataCompat == null) 0 else metadataList.indexOf(currentMediaMetadataCompat)
//
//        // Set playlist and prepare.
//        player?.setMediaItems(
//            metadataList.map { it.toMediaItem() }, initialWindowIndex, 0)
//        player?.prepare()
//        session.setMetadata(currentMediaMetadataCompat)
//        continuePlaying()
//        Log.d("MediaSessionCallback", "metadataList: size:${metadataList?.size} initialWindowIndex:${initialWindowIndex}")
//
//
//    }
//
//    fun continuePlaying() {
//        requireNotNull(player).playWhenReady = true
//    }
//
//    fun pausePlaying() {
//        requireNotNull(player).playWhenReady = false
//    }
//
//    fun stopPlaying() {
//        requireNotNull(player).stop()
//    }
//
//    fun releasePlayer() {
//        setPlaybackState(STATE_STOPPED)
//        player?.removeListener(this)
//        player?.release()
//        player = null
//    }
//
//    fun updateFavoritedState() {
//        setPlaybackState(currentState)
//    }
//
//    private fun setPlaybackState(state: Int, isInitializing: Boolean = false) {
//        currentState = state
//
//        if (isInitializing) {
//            session.setPlaybackState(PlaybackStateCompat.Builder()
//                    .setState(state, 0, 0f)
//                    .build())
//        } else {
//            var favoriteDrawableRes=R.drawable.ic_round_star_border_24dp
//            if(currentChannel!=null){
//                favoriteDrawableRes =
//                    if (favoritesHelper.isAddedRecentPlayed(currentChannel!!)) R.drawable.ic_round_star_24dp else R.drawable.ic_round_star_border_24dp
//            }
//
//
//            val customAction = PlaybackStateCompat.CustomAction
//                    .Builder(CUSTOM_ACTION_FAVORITE, context.getString(R.string.action_favorite_name), favoriteDrawableRes)
//                    .build()
//
//            session.setPlaybackState(PlaybackStateCompat.Builder()
//                    .setState(state, 0, 0f)
//                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
//                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
//                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
////                    .addCustomAction(customAction)
//                    .build())
//        }
//    }
//
//    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//        when (playbackState) {
//            Player.STATE_READY -> {
////                notificationManager.showNotificationForPlayer(player!!)
//                if (playWhenReady) {
//                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
//                } else {
//                    setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
//                }
//
//                favoritesHelper.addRecentPlayed(currentChannel!!)
//            }
//            Player.STATE_BUFFERING -> setPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
//            Player.STATE_ENDED -> setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
//            Player.STATE_IDLE -> setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
//            else -> setPlaybackState(PlaybackStateCompat.STATE_NONE)
//        }
//    }
//
//    override fun onLoadingChanged(isLoading: Boolean) {
//        if (isLoading) {
//            setPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
//        } else {
//            setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
//        }
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
//                mListener?.let {
//                    mListener?.onStartService(notificationId,notification)
//                }
//
//                isForegroundService = true
//            }
//        }
//
//        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//            releasePlayer()
//            mListener?.let {
//                mListener?.onStopService()
//            }
//            isForegroundService = false
//
////            notificationManager?.hideNotification()
//
//
//        }
//    }
//
//    private var mListener: onAutoServiceStartStopListener? = null
//    fun addAutoServiceStartStopListener(listener: onAutoServiceStartStopListener) {
//        mListener = listener
//    }
//
//
//}
//public interface onAutoServiceStartStopListener {
//    fun onStartService(notificationId:Int,notification:Notification)
//    fun onStopService()
//}