/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hungama.music.player.download;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.offline.Download;

import com.hungama.music.R;

import java.util.List;

/** Helper for creating download notifications. */
public final class DownloadNotificationHelper {

  private static final @StringRes int NULL_STRING_ID = 0;

  private final NotificationCompat.Builder notificationBuilder;

  /**
   * @param context A context.
   * @param channelId The id of the notification channel to use.
   */
  public DownloadNotificationHelper(Context context, String channelId) {
    this.notificationBuilder =
        new NotificationCompat.Builder(context.getApplicationContext(), channelId);
  }

  @OptIn(markerClass = UnstableApi.class) /**
   * Returns a progress notification for the given downloads.
   *
   * @param context A context.
   * @param smallIcon A small icon for the notification.
   * @param contentIntent An optional content intent to send when the notification is clicked.
   * @param message An optional message to display on the notification.
   * @param downloads The downloads.
   * @return The notification.
   */
  public Notification buildProgressNotification(
      Context context,
      @DrawableRes int smallIcon,
      @Nullable PendingIntent contentIntent,
      @Nullable String message,
      List<Download> downloads) {
    float totalPercentage = 0;
    int downloadTaskCount = 0;
    boolean allDownloadPercentagesUnknown = true;
    boolean haveDownloadedBytes = false;
    boolean haveDownloadTasks = false;
    boolean haveRemoveTasks = false;
    String contentTitle = "";
    for (int i = 0; i < downloads.size(); i++) {
      Download download = downloads.get(i);
      if (download.state == Download.STATE_REMOVING) {
        haveRemoveTasks = true;
        continue;
      }
      if (download.state != Download.STATE_RESTARTING
          && download.state != Download.STATE_DOWNLOADING) {
        continue;
      }
      haveDownloadTasks = true;
      float downloadPercentage = download.getPercentDownloaded();
      if (downloadPercentage != C.PERCENTAGE_UNSET) {
        allDownloadPercentagesUnknown = false;
        totalPercentage += downloadPercentage;
      }
      contentTitle = Util.fromUtf8Bytes(download.request.data);
      haveDownloadedBytes |= download.getBytesDownloaded() > 0;
      downloadTaskCount++;
    }

    int titleStringId =
        haveDownloadTasks ? androidx.media3.exoplayer.R.string.exo_download_downloading : (haveRemoveTasks ? androidx.media3.exoplayer.R.string.exo_download_removing : NULL_STRING_ID);
    int progress = 0;
    boolean indeterminate = true;
    if (haveDownloadTasks) {
      progress = (int) (totalPercentage / downloadTaskCount);
      indeterminate = allDownloadPercentagesUnknown && haveDownloadedBytes;
    }
    return buildNotification(
        context,
        smallIcon,
        contentIntent,
        message,
        titleStringId,
        /* maxProgress= */ 100,
        progress,
        indeterminate,
        /* ongoing= */ true,
        /* showWhen= */ false,
            contentTitle);
  }

  /**
   * Returns a notification for a completed download.
   *
   * @param context A context.
   * @param smallIcon A small icon for the notifications.
   * @param contentIntent An optional content intent to send when the notification is clicked.
   * @param message An optional message to display on the notification.
   * @return The notification.
   */
  public Notification buildDownloadCompletedNotification(
      Context context,
      @DrawableRes int smallIcon,
      @Nullable PendingIntent contentIntent,
      @Nullable String message) {
    int titleStringId = androidx.media3.exoplayer.R.string.exo_download_completed;
    return buildEndStateNotification(context, smallIcon, contentIntent, message, titleStringId);
  }

  /**
   * Returns a notification for a failed download.
   *
   * @param context A context.
   * @param smallIcon A small icon for the notifications.
   * @param contentIntent An optional content intent to send when the notification is clicked.
   * @param message An optional message to display on the notification.
   * @return The notification.
   */
  public Notification buildDownloadFailedNotification(
      Context context,
      @DrawableRes int smallIcon,
      @Nullable PendingIntent contentIntent,
      @Nullable String message) {
    @StringRes int titleStringId = androidx.media3.exoplayer.R.string.exo_download_failed;
    return buildEndStateNotification(context, smallIcon, contentIntent, message, titleStringId);
  }

  private Notification buildEndStateNotification(
      Context context,
      @DrawableRes int smallIcon,
      @Nullable PendingIntent contentIntent,
      @Nullable String message,
      @StringRes int titleStringId) {
    return buildNotification(
        context,
        smallIcon,
        contentIntent,
        message,
        titleStringId,
        /* maxProgress= */ 0,
        /* currentProgress= */ 0,
        /* indeterminateProgress= */ false,
        /* ongoing= */ false,
        /* showWhen= */ true,
            message);
  }
//  public static PendingIntent button1PI;
//  public static PendingIntent button2PI;
//  public static Intent button1I,button2I;
  public static PendingIntent actionIntentPause;
  public static PendingIntent actionIntentStop;
  private Notification buildNotification(
      Context context,
      @DrawableRes int smallIcon,
      @Nullable PendingIntent contentIntent,
      @Nullable String message,
      @StringRes int titleStringId,
      int maxProgress,
      int currentProgress,
      boolean indeterminateProgress,
      boolean ongoing,
      boolean showWhen,
      @Nullable String contentTitle) {
    if(actionIntentPause==null) {
//      button1I = new Intent(DownloadReceiver.ACTION_1);
//      button1PI = PendingIntent.getBroadcast(context, 0, button1I, 0);
      Intent broadcastIntentPause = new Intent(context, DownloadReceiver.class);
      broadcastIntentPause.putExtra("action", DownloadReceiver.ACTION_1);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        actionIntentPause = PendingIntent.getBroadcast(context,
                0, broadcastIntentPause, PendingIntent.FLAG_IMMUTABLE);
      }else{
        actionIntentPause = PendingIntent.getBroadcast(context,
                0, broadcastIntentPause, PendingIntent.FLAG_UPDATE_CURRENT);
      }
    }
    if(actionIntentStop==null) {
//      button2I = new Intent(DownloadReceiver.ACTION_2);
//      button2PI = PendingIntent.getBroadcast(context, 0, button2I, 0);
      Intent broadcastIntentStop = new Intent(context, DownloadReceiver.class);
      broadcastIntentStop.putExtra("action", DownloadReceiver.ACTION_2);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        actionIntentStop = PendingIntent.getBroadcast(context,
                0, broadcastIntentStop, PendingIntent.FLAG_IMMUTABLE);
      }else{
        actionIntentStop = PendingIntent.getBroadcast(context,
                0, broadcastIntentStop, PendingIntent.FLAG_UPDATE_CURRENT);
      }
    }

    notificationBuilder.setSmallIcon(smallIcon);
    notificationBuilder.setContentTitle(contentTitle);
    notificationBuilder.setContentText(titleStringId == NULL_STRING_ID ? null : context.getResources().getString(titleStringId));
    notificationBuilder.setContentIntent(contentIntent);
    //notificationBuilder.addAction(R.drawable.exo_notification_pause,"Pause",actionIntentPause);
    //notificationBuilder.addAction(R.drawable.exo_notification_stop,"Stop",actionIntentStop);

    /*notificationBuilder.setStyle(
        message == null ? null : new NotificationCompat.BigTextStyle().bigText(message));*/
    notificationBuilder.setProgress(maxProgress, currentProgress, indeterminateProgress);
    notificationBuilder.setOngoing(ongoing);
    notificationBuilder.setShowWhen(showWhen);
    return notificationBuilder.build();
  }
}
