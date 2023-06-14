package com.hungama.music.player.audioplayer.mediautils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

fun getAlbumArtUri(context: Context, albumId: Long): Uri {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId)
    } else {
        val contentUri =
            ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId)

        val cursor = context.contentResolver
            .query(
                contentUri,
                arrayOf(MediaStore.Audio.Albums.ALBUM_ART),
                null,
                null,
                null
            )

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    val file =
                        File(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)))
                    if (file.exists()) {
                        try {
                            return Uri.fromFile(file)
                        } catch (ignored: FileNotFoundException) {
                        }
                    }
                }
            } catch (ignored: NullPointerException) {
            } finally {
                cursor.close()
            }
        }
        return Uri.EMPTY
    }
}

interface OnBitmapLoadedListener {
    fun onBitmapLoaded(resource: Bitmap)
    fun onBitmapLoadingFailed()
}



suspend fun loadAlbumArtUri(
    context: Context,
    contentUri: String,
    onBitmapLoadedListener: OnBitmapLoadedListener
) {
    withContext(Dispatchers.IO) {
        Glide.with(context)
            .asBitmap()
            .load(contentUri)
            .apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    Log.d("loadAlbumArtUri", "onResourceReady called")

                    onBitmapLoadedListener.onBitmapLoaded(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Log.d("loadAlbumArtUri", "onResourceReady called")
                    onBitmapLoadedListener.onBitmapLoadingFailed()
                }

            })
    }

}