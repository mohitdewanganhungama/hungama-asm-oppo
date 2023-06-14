package com.hungama.music.player.audioplayer.mediautils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import androidx.media3.common.C
import androidx.media3.common.Player
import com.hungama.music.player.audioplayer.model.Track


fun getMediaDescriptionForLockScreen(
    player: Player,
    context: Context,
    track: Track,
    callback: () -> Unit
): MediaDescriptionCompat {


    val bundle = Bundle()

    var bitmap: Bitmap?

    /*if (track.albumArtBitmap == null) {

        val bitmapDrawable =
                BitmapFactory.decodeResource(context.resources, track.defaultAlbumArtRes)

        bitmap = bitmapDrawable

        CoroutineScope(Dispatchers.IO).launch {
            try {
                bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bg_background)
                track.albumArtBitmap = bitmap

                withContext(Dispatchers.Main) {
                    callback.invoke()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } else {
        bitmap = track.albumArtBitmap
    }*/

    //bundle.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
    //bundle.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
    bundle.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse(track.image))
    bundle.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse(track.image))
    var duration:Long = 0
    if (player.duration == C.TIME_UNSET){
        duration = -1
    }else{
        duration = player.duration
    }
    bundle.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration)

    track.albumArtBitmap = null
    var mediaTitle = "-"
    var mediaSubtitle = "-"

    if (!TextUtils.isEmpty(track.title)){
        mediaTitle = track.title.toString()
    }
    if (!TextUtils.isEmpty(track.subTitle)){
        mediaSubtitle = track.subTitle.toString()
    }
    bundle.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, mediaTitle)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaTitle)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mediaSubtitle)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, mediaSubtitle)
    return MediaDescriptionCompat.Builder()
            .setTitle(mediaTitle)
            .setDescription(mediaSubtitle)
            .setSubtitle(mediaSubtitle)
            //.setIconBitmap(bitmap)
            .setIconUri(Uri.parse(track.image))
            .setExtras(bundle)
            .build()

}