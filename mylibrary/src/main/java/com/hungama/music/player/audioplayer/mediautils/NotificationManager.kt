package com.hungama.music.player.audioplayer.mediautils

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.session.MediaSession
import com.hungama.music.R
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.PLAYBACK_CHANNEL_ID
import com.hungama.music.utils.Constant.PLAYBACK_NOTIFICATION_ID

class NotificationManager(
    context: Context,
    private val mediaSession: MediaSession,
    private val player: androidx.media3.exoplayer.ExoPlayer,
    nowPlayingQueue: NowPlayingQueue,
    private val listener: OnNotificationPostedListener
) {

    /*private var playerNotificationManager: PlayerNotificationManager

    init {
        playerNotificationManager =
                PlayerNotificationManager.createWithNotificationChannel(
                        context,
                    PLAYBACK_CHANNEL_ID,
                        R.string.login_str_2,
                        R.string.login_str_2,
                    PLAYBACK_NOTIFICATION_ID, MediaDescriptionAdapter(
                        context,
                        nowPlayingQueue)
                )


    }

    interface OnNotificationPostedListener {
        fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean)

        fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean)
    }

    fun setupPlayerNotification(context: Context, nowPlayingQueue: NowPlayingQueue) {

        playerNotificationManager =
                PlayerNotificationManager(
                        context,
                        PLAYBACK_CHANNEL_ID,
                    PLAYBACK_NOTIFICATION_ID,
                        MediaDescriptionAdapter(
                                context,
                                nowPlayingQueue
                        ),
                        object : PlayerNotificationManager.NotificationListener {
                            override fun onNotificationPosted(
                                    notificationId: Int,
                                    notification: Notification,
                                    ongoing: Boolean
                            ) {
                                CommonUtils.setLog(
                                    "NotificationManager",
                                    "setupPlayerNotification-103-ongoing-$ongoing "
                                )
                                //if (ongoing)
                                    //listener.onNotificationPosted(notificationId, notification, ongoing)
                            }

                            override fun onNotificationCancelled(
                                    notificationId: Int, dismissedByUser: Boolean
                            ) {
                                CommonUtils.setLog(
                                    "NotificationManager",
                                    "setupPlayerNotification-104-dismissedByUser-$dismissedByUser "
                                )
                                //listener.onNotificationCancelled(notificationId, dismissedByUser)
                            }

                            fun onNotificationStarted(
                                notificationId: Int,
                                notification: Notification
                            ) {
                                CommonUtils.setLog(
                                    "NotificationManager",
                                    "setupPlayerNotification-105-onNotificationStarted "
                                )
                            }
                        })


        playerNotificationManager.apply {
            setPlayer(player)
            DefaultControlDispatcher(0,0)
            setUseStopAction(false)
        }
        playerNotificationManager.setControlDispatcher(object : ControlDispatcher{
            override fun dispatchPrepare(player: Player): Boolean {
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchPrepare-10001 "
                )
                //player.prepare()
                return true
            }

            override fun dispatchSetPlayWhenReady(player: Player, playWhenReady: Boolean): Boolean {
                player.playWhenReady = playWhenReady
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchSetPlayWhenReady-10002 "
                )
                return true
            }

            override fun dispatchSeekTo(
                player: Player,
                windowIndex: Int,
                positionMs: Long
            ): Boolean {
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchSeekTo-10003 "
                )
                player.seekTo(windowIndex, positionMs)
                return true
            }

            override fun dispatchPrevious(player: Player): Boolean {
                setLog("NotificationPP", "Previous")
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchPrevious-10004 "
                )
                onNotificationButtonClickAction(context, false)
                return true
            }

            override fun dispatchNext(player: Player): Boolean {
                setLog("NotificationPP", "Next")
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchNext-10005 "
                )
                onNotificationButtonClickAction(context, true)
                return true
            }

            override fun dispatchRewind(player: Player): Boolean {
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchRewind-10006 "
                )
                return true
            }

            override fun dispatchFastForward(player: Player): Boolean {
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchFastForward-10007 "
                )
                return true
            }

            override fun dispatchSetRepeatMode(player: Player, repeatMode: Int): Boolean {
                //player.setRepeatMode(repeatMode)
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchSetRepeatMode-10008 "
                )
                return true
            }

            override fun dispatchSetShuffleModeEnabled(
                player: Player,
                shuffleModeEnabled: Boolean
            ): Boolean {
                //player.setShuffleModeEnabled(shuffleModeEnabled)
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchSetShuffleModeEnabled-10009 "
                )
                return true
            }

            override fun dispatchStop(player: Player, reset: Boolean): Boolean {
                //player.stop(reset)
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchStop-10010 "
                )
                return true
            }

            override fun dispatchSetPlaybackParameters(
                player: Player,
                playbackParameters: PlaybackParameters
            ): Boolean {
                //player.setPlaybackParameters(playbackParameters)
                CommonUtils.setLog(
                    "NotificationManager",
                    "dispatchSetPlaybackParameters-10011 "
                )
                return true
            }

            override fun isRewindEnabled(): Boolean {
                CommonUtils.setLog(
                    "NotificationManager",
                    "isRewindEnabled-10012 "
                )
                return false
            }

            override fun isFastForwardEnabled(): Boolean {
                CommonUtils.setLog(
                    "NotificationManager",
                    "isFastForwardEnabled-10013 "
                )
                return false
            }

        })

        mediaSession.isActive = true
        CommonUtils.setLog(
            "NotificationManager",
            "setupPlayerNotification-102-mediaSession.sessionToken-${mediaSession.sessionToken} "
        )
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)
    }

    fun cleanup() {
        playerNotificationManager.setPlayer(null)
    }

    fun onNotificationButtonClickAction(context: Context, isNext:Boolean){
        val intent = Intent(Constant.NOTIFICATION_PLAYER_EVENT)
        intent.putExtra("EVENT", Constant.NOTIFICATION_RESULT_CODE)
        intent.putExtra("isNext", isNext)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun hideNotification() {
        playerNotificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player){
        playerNotificationManager.setPlayer(player)
    }*/
    interface OnNotificationPostedListener {
        fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean)

        fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean)
    }
}