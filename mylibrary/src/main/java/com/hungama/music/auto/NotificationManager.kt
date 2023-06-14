//package com.hungama.music.auto
//
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.drawable.Drawable
//import android.net.Uri
//import android.support.v4.media.session.MediaControllerCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.util.Log
//import androidx.annotation.OptIn
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.ui.PlayerNotificationManager
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.request.RequestOptions
//import com.bumptech.glide.request.target.CustomTarget
//import com.bumptech.glide.request.transition.Transition
//import com.hungama.music.auto.ui.PlayerHolder
//import com.hungama.music.R
//
//import kotlinx.coroutines.*
//
////const val NOW_PLAYING_CHANNEL_ID = "com.hungama.music.shared.media.NOW_PLAYING"
////const val NOW_PLAYING_NOTIFICATION_ID = 0xb339 // Arbitrary number used to identify our notification
//const val NOW_PLAYING_CHANNEL_ID = "playback_channel 1"
//const val NOW_PLAYING_NOTIFICATION_ID = 2 // Arbitrary number used to identify our notification
//
//
///**
//* A wrapper class for ExoPlayer's PlayerNotificationManager. It sets up the notification shown to
//* the user during audio playback and provides track metadata, such as track title and icon image.
// */
//@OptIn(UnstableApi::class)
//class NotificationManager (
//    private val context: Context,
//    sessionToken: MediaSessionCompat.Token,
//    notificationListener: PlayerNotificationManager.NotificationListener
//) {
//
//    private var TAG= "NotificationManager"
//    private var player: Player? = null
//    private val serviceJob = SupervisorJob()
//    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
//    private val notificationManager: PlayerNotificationManager
//    private var platformNotificationManager: NotificationManager =
//        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//    init {
//        val mediaController = MediaControllerCompat(context, sessionToken)
//
//        val builder = PlayerNotificationManager.Builder(context, NOW_PLAYING_NOTIFICATION_ID, NOW_PLAYING_CHANNEL_ID,DescriptionAdapter(mediaController))
//        with (builder) {
//            setNotificationListener(notificationListener)
//            setChannelNameResourceId(R.string.notification_channel)
//            setChannelDescriptionResourceId(R.string.notification_channel_description)
//        }
//
//
//        notificationManager=builder.build()
//        notificationManager.setMediaSessionToken(sessionToken)
//        notificationManager.setSmallIcon(R.drawable.ic_notifications)
//
//        notificationManager?.setUseStopAction(false)
//        notificationManager?.setUsePlayPauseActions(true)
////        notificationManager?.setUseNextAction(true)
////        notificationManager?.setUsePreviousAction(true)
//
//
//    }
//
//    fun hideNotification() {
//        notificationManager.setPlayer(null)
//
//        platformNotificationManager?.cancel(NOW_PLAYING_NOTIFICATION_ID)
//    }
//
//    fun showNotificationForPlayer(player: Player){
//        notificationManager.setPlayer(player)
//    }
//
//    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
//        PlayerNotificationManager.MediaDescriptionAdapter {
//
//        var currentIconUri: Uri? = null
//        var currentBitmap: Bitmap? = null
//
//        override fun createCurrentContentIntent(player: Player): PendingIntent? =
//            controller.sessionActivity
//
//        override fun getCurrentContentText(player: Player):String{
//            if(controller.metadata!=null&&controller.metadata.description.subtitle!=null){
//                return controller.metadata.description.subtitle.toString()
//            }else{
//                return PlayerHolder.currentChannel?.title!!
//            }
//        }
//
//
//        override fun getCurrentContentTitle(player: Player): String {
//            if(controller.metadata!=null&&controller.metadata.description.title!=null){
//                return controller.metadata.description.title.toString()
//            }else{
//                return PlayerHolder.currentChannel?.title!!
//            }
//
//        }
//
//
//        override fun getCurrentLargeIcon(
//            player: Player,
//            callback: PlayerNotificationManager.BitmapCallback
//        ): Bitmap? {
//            Log.d(TAG, "getCurrentLargeIcon: currentBitmap:${currentBitmap}")
//            Log.d(TAG, "getCurrentLargeIcon: controller:${controller}")
//            Log.d(TAG, "getCurrentLargeIcon: metadata:${controller.metadata}")
//            Log.d(TAG, "getCurrentLargeIcon: description:${controller?.metadata?.description} ")
//            Log.d(TAG, "getCurrentLargeIcon: iconUri:${controller?.metadata?.description?.iconUri}")
//            var iconUri = controller?.metadata?.description?.iconUri
//            iconUri= Uri.parse(PlayerHolder.currentChannel?.imageRes!!)
//            return if (currentIconUri != iconUri || currentBitmap == null) {
//
//                // Cache the bitmap for the current song so that successive calls to
//                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
//                currentIconUri = iconUri
//                serviceScope.launch {
//                    currentBitmap = iconUri?.let {
//                        resolveUriAsBitmap(it)
//                    }
//                    currentBitmap?.let { callback.onBitmap(it) }
//                }
////                loadBitmap(iconUri, callback)
//                null
//            } else {
//                return currentBitmap
//            }
//        }
//
//        private fun loadBitmap(url: Uri?, callback: PlayerNotificationManager.BitmapCallback?) {
//            Glide.with(context)
//                .asBitmap()
//                .load(url)
//                .into(object : CustomTarget<Bitmap>() {
//                    override fun onResourceReady(
//                        resource: Bitmap,
//                        transition: Transition<in Bitmap>?
//                    ) {
//                        callback?.onBitmap(resource)
//                    }
//
//                    override fun onLoadCleared(placeholder: Drawable?) {
//
//                    }
//                })
//        }
//
//        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
//            return withContext(Dispatchers.IO) {
//                // Block on downloading artwork.
//                Glide.with(context).applyDefaultRequestOptions(glideOptions)
//                    .asBitmap()
//                    .load(uri)
//                    .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
//                    .get()
//            }
//        }
//    }
//}
//
//const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px
//
//val glideOptions = RequestOptions()
//    .fallback(R.drawable.bg_gradient_placeholder)
//    .diskCacheStrategy(DiskCacheStrategy.DATA)
//
//private const val MODE_READ_ONLY = "r"
