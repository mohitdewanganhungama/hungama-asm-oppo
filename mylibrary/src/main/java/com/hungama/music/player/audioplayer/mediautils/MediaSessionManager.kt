package com.hungama.music.player.audioplayer.mediautils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.session.MediaSession
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.Constant

private const val TAG = "Hungama"
class MediaSessionManager(context: Context, private val player: androidx.media3.exoplayer.ExoPlayer) {
    var mediaSession: MediaSession? = null
    /*private val sessionActivityPendingIntent =
        context.packageManager?.getLaunchIntentForPackage(context.packageName)?.let { sessionIntent ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(context, 1, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
            }else{
                PendingIntent.getActivity(context, 1, sessionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }*/
    init {
        /*mediaSession = MediaSession.Builder(context, player)
            //.setSessionCallback(MySessionCallback())
            .setSessionActivity(sessionActivityPendingIntent!!)
            .setId(Constant.SESSION_ID_AUDIO)
            .build()*/
    }

    fun cleanup() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
    }
}

