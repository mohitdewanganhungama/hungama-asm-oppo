package com.hungama.music.player.audioplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.hungama.music.R
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.Constant.BUNDLE_KEY
import com.hungama.music.utils.Constant.ITEM_KEY

class PlayerWidget : BaseWedget() {

    override fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                appWidgetId: Int) {

        val views = RemoteViews(context.packageName, R.layout.player_widget)
        val intent = Intent(context, MainActivity::class.java)
        val serviceBundle = Bundle()
        serviceBundle.putSerializable(ITEM_KEY, track)
        intent.putExtra(BUNDLE_KEY, serviceBundle)
        val playerPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, playerPendingIntent)
        // Instruct the widget manager to update the widget
        if (noData) {
            /*views.setViewVisibility(R.id.widget_thumbnail, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_title, View.GONE)
            //views.setViewVisibility(R.id.widget_subTitle, View.GONE)
            views.setViewVisibility(R.id.widget_play, View.GONE)
            views.setViewVisibility(R.id.widget_pause, View.GONE)
            //views.setViewVisibility(R.id.widget_next, View.GONE)
            //views.setViewVisibility(R.id.widget_prev, View.GONE)
            views.setViewVisibility(R.id.widget_no_playing, View.VISIBLE)*/
            views.setViewVisibility(R.id.widget_pause, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_play, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_no_playing, View.GONE)
            views.setViewVisibility(R.id.widget_title, View.VISIBLE)
            views.setViewVisibility(R.id.widget_thumbnail, View.VISIBLE)
            views.setTextViewText(R.id.widget_title, mTitle)
            //views.setViewVisibility(R.id.widget_subTitle, View.VISIBLE)
            views.setViewVisibility(R.id.widget_play, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_pause, View.VISIBLE)
            //views.setViewVisibility(R.id.widget_next, View.VISIBLE)
            //views.setViewVisibility(R.id.widget_prev, View.VISIBLE)
            if (mThumbnail != null) {
                val appWidgetTarget = object : AppWidgetTarget(context, R.id.widget_thumbnail, views, appWidgetId) {
                }
                Glide.with(context)
                        .asBitmap()
                        .load(mThumbnail)
                        .override(200, 200)
                        .into<AppWidgetTarget>(appWidgetTarget)
            }
            if (isPlaying) {
                views.setViewVisibility(R.id.widget_play, View.INVISIBLE)
                views.setViewVisibility(R.id.widget_pause, View.VISIBLE)
                views.setOnClickPendingIntent(R.id.widget_pause,
                        getPendingIntent(context, PAUSE_BUTTON_CLICKED_ACTION))
            } else  {
                views.setViewVisibility(R.id.widget_pause, View.INVISIBLE)
                views.setViewVisibility(R.id.widget_play, View.VISIBLE)
                views.setOnClickPendingIntent(R.id.widget_play,
                        getPendingIntent(context, PLAY_BUTTON_CLICKED_ACTION))
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
