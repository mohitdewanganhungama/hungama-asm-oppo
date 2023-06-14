package com.hungama.music.player.audioplayer.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.SimpleExoPlayer
import com.hungama.music.player.audioplayer.mediautils.MediaSessionManager
import com.hungama.music.player.audioplayer.mediautils.NotificationManager
import com.hungama.music.player.audioplayer.model.Track

interface AudioPlayer {

    fun init(context: Context)

    fun playTracks(
            context: Context,
            tracksList: MutableList<Track>,
            selectedTrackPosition: Int,
            shuffle: Boolean = false,
            playStartPosition:Long = 0,
            isQueueItem: Boolean = false,
            isPause: Boolean = false
    )

    /*fun shufflePlayTracks(
            context: Context,
            tracksList: MutableList<Track>
    )*/

    fun setShuffleMode(context: Context, shuffle: Boolean)

    fun updateTracks(
            context: Context,
            tracksList: MutableList<Track>
    )

    fun addTrackToQueue(context: Context, track: Track)

    fun addTrackToQueue(context: Context, trackList: ArrayList<Track>)

    fun playNext(context: Context, track: Track)

    fun setNotificationPostedListener(listener: NotificationManager.OnNotificationPostedListener) {
        //default method
    }

    fun setPlayerEventListener(listener: Player.Listener) {
        //default method
    }

    fun getMediaSession(): MediaSessionManager?

    fun setMediaItem(track: Track, index:Int): MediaItem

    fun setMediaItem(track: ArrayList<Track>): List<MediaItem>

    fun playPrevious(context: Context, track: Track)

    fun cleanup()

    fun notificationCleanup()

    fun showNotification(simpleExoPlayer: SimpleExoPlayer)

    fun hideNotification()

    fun updateTrackData(context: Context, track: Track)
}