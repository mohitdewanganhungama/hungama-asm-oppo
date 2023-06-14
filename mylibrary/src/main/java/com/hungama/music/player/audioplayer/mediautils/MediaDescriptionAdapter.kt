package com.hungama.music.player.audioplayer.mediautils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import com.hungama.music.R
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.ui.main.view.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MediaDescriptionAdapter(
        private val context: Context,
        private val nowPlayingQueue: NowPlayingQueue
) :
        PlayerNotificationManager.MediaDescriptionAdapter {

    override fun createCurrentContentIntent(player: Player): PendingIntent? {

        return PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getCurrentSubText(player: Player): String? {
        if (!TextUtils.isEmpty(getNowPlayingTrack().subTitle?.trim())){
            return getNowPlayingTrack().subTitle
        }else{
            return "-"
        }
    }

    override fun getCurrentContentText(player: Player): String? {
        if (!TextUtils.isEmpty(getNowPlayingTrack().subTitle?.trim())){
            return getNowPlayingTrack().subTitle
        }else{
            return "-"
        }
    }

    override fun getCurrentContentTitle(player: Player): String {
        if (!TextUtils.isEmpty(getNowPlayingTrack().title?.trim())){
            return getNowPlayingTrack().title!!
        }else{
            return "-"
        }
    }

    override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {

        CoroutineScope(Dispatchers.IO).launch{
            loadAlbumArtUri(context, getNowPlayingTrack().image!!, object : OnBitmapLoadedListener {
                override fun onBitmapLoaded(resource: Bitmap) {
                    callback.onBitmap(resource)
                }

                override fun onBitmapLoadingFailed() {
                    callback.onBitmap(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.ic_audiotrack
                        )!!.toBitmap()
                    )
                }

            })
        }

        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_audiotrack)
    }

    private fun getNowPlayingTrack(): Track {
        var track = Track()
        try {
            if (nowPlayingQueue != null && nowPlayingQueue.nowPlayingTracksList.size > 0){
                track = nowPlayingQueue.nowPlayingTracksList[nowPlayingQueue.currentPlayingTrackIndex]
            }
        }catch (e:Exception){
            return track
        }

        return track
    }

}