package com.hungama.music.player.audioplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant

abstract class BaseWedget: AppWidgetProvider() {
    var track: ArrayList<Track>? = null
    var playPosition: Int = 0
    var mTitle: String? = null
    var mSubTitle: String? = null
    var mThumbnail: String? = null
    var isPlaying: Boolean = false
    var noData: Boolean = true
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

            if (intent.hasExtra(WIDGET_NO_PLAYING_EXTRA)) {
                this.noData = true
                this.mTitle = null
                this.isPlaying = false
            } else if (intent.hasExtra(Constant.BUNDLE_KEY) && intent.hasExtra(WIDGET_PLAYING_EXTRA)) {
                val b = intent.getBundleExtra(Constant.BUNDLE_KEY)
                if (b != null) {
                    this.noData = false
                    if (b.getSerializable(Constant.ITEM_KEY) != null){
                        track = b.getSerializable(Constant.ITEM_KEY) as ArrayList<Track>
                        playPosition = intent.getIntExtra(PLAYPOSITION, 0)
                        this.mTitle = track!!.get(playPosition).title
                        this.mSubTitle = track!!.get(playPosition).subTitle
                        this.mThumbnail = track!!.get(playPosition).image
                    }


                }
                this.isPlaying = intent.getBooleanExtra(WIDGET_PLAYING_EXTRA, false)
            }
            if (appWidgetIds != null) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
        if (action == PLAY_BUTTON_CLICKED_ACTION) {
            val playEpisodeIntent = Intent(context, AudioPlayerService::class.java)
            if (CommonUtils.isServiceRunning(AudioPlayerService::class.java, context)){
                playEpisodeIntent.action = Constant.ACTION_PLAY
            }else{
                playEpisodeIntent.action = AudioPlayerService.PlaybackControls.PLAY.name
                playEpisodeIntent.putExtra(Constant.SELECTED_TRACK_POSITION, playPosition)
                playEpisodeIntent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            }
            context.startService(playEpisodeIntent)
        }
        if (action == PAUSE_BUTTON_CLICKED_ACTION) {
            val pauseEpisodeIntent = Intent(context, AudioPlayerService::class.java)
            pauseEpisodeIntent.action = Constant.ACTION_PAUSE
            context.startService(pauseEpisodeIntent)
        }
        if (action == PLAYNEXT) {
            val playEpisodeIntent = Intent(context, AudioPlayerService::class.java)
            playEpisodeIntent.action = Constant.ACTION_PLAY_NEXT
            context.startService(playEpisodeIntent)
        }
        if (action == PLAYPREVIOUS) {
            val pauseEpisodeIntent = Intent(context, AudioPlayerService::class.java)
            pauseEpisodeIntent.action = Constant.ACTION_PAUSE_PREVIOUS
            context.startService(pauseEpisodeIntent)
            BaseActivity.setTouchData()
        }
        super.onReceive(context, intent)
    }

    fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    protected abstract fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                appWidgetId: Int)

    companion object {

        const val WIDGET_PLAYING_EXTRA = "widget-is-playing"
        const val WIDGET_NO_PLAYING_EXTRA = "widget-no-episode-playing"
        const val PLAYPOSITION = "playPosition"
        const val PLAY_BUTTON_CLICKED_ACTION = "PLAY_BUTTON_CLICKED_ACTION"
        const val PAUSE_BUTTON_CLICKED_ACTION = "PAUSE_BUTTON_CLICKED_ACTION"
        const val PLAYNEXT = "next"
        const val PLAYPREVIOUS = "previous"
    }
}