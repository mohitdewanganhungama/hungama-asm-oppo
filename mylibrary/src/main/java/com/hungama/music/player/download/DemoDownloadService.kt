/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.hungama.music.player.download

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.getDeeplinkIntentData
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

@OptIn(UnstableApi::class) /**
 * A service for downloading media.
 */
class DemoDownloadService  //private static final int tempPos = new Random().nextInt();
//private static final int JOB_ID = tempPos;
//private static final int FOREGROUND_NOTIFICATION_ID = tempPos;
    : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DemoUtil.DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    androidx.media3.exoplayer.R.string.exo_download_notification_channel_name,  /* channelDescriptionResourceId= */
    0
) {
    var contentIntent: PendingIntent? = null
    override fun getDownloadManager(): DownloadManager {
        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        val downloadManager = DemoUtil.getDownloadManager( /* context= */this)
        val downloadNotificationHelper = DemoUtil.getDownloadNotificationHelper( /* context= */this)
        setLog("showDeepLinkUrl", "DemoDownloadService-getDownloadManager-showDeepLinkUrl-called")
        val deeplinkUrl = "https://www.hungama.com/library/video/downloaded-video"
        val intent = getDeeplinkIntentData(Uri.parse(deeplinkUrl))
        intent.setClass(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }else{
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        /*downloadManager.addListener(
            new TerminalStateNotificationHelper(
                    this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1));*/downloadManager.addListener(
            TerminalStateNotificationHelper(
                this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID, contentIntent
            )
        )
        return downloadManager
    }

    override fun getScheduler(): PlatformScheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(downloads: List<Download>, notMetRequirements: Int): Notification {
        CoroutineScope(Dispatchers.IO).launch {
            DemoUtil.getDownloadTracker(this@DemoDownloadService).updateDownloadProgress(downloads)
            setLog(
                "showDeepLinkUrl",
                "DemoDownloadService-getForegroundNotification-showDeepLinkUrl-called"
            )
        }

        return DemoUtil.getDownloadNotificationHelper( /* context= */this)
            .buildProgressNotification( /* context= */
                this,
                R.drawable.ic_download,  /* contentIntent= */
                contentIntent,  /* message= */
                "",
                downloads
            )
    }

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     */
    private class TerminalStateNotificationHelper(
        context: Context, notificationHelper: DownloadNotificationHelper,
        firstNotificationId: Int, contentIntent: PendingIntent?
    ) : DownloadManager.Listener {
        private val context: Context
        private val notificationHelper: DownloadNotificationHelper
        private var nextNotificationId: Int
        var contentIntent: PendingIntent? = null
        override fun onDownloadChanged(
            downloadManager: DownloadManager, download: Download, finalException: Exception?
        ) {
            setLog(
                "OnExoDownload--12",
                "DemoDownloadService-onDownloadChanged-uri-" + download.request.uri
            )
            setLog(
                "OnExoDownload--12",
                "DemoDownloadService-onDownloadChanged-download.state-" + download.state
            )
            setLog(
                "showDeepLinkUrl",
                "DemoDownloadService-onDownloadChanged-showDeepLinkUrl-download.state-" + download.state
            )
            val deeplinkUrl = "https://www.hungama.com/library/video/downloaded-video"
            val intent = getDeeplinkIntentData(Uri.parse(deeplinkUrl))
            intent.setClass(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }else{
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            val notification: Notification
            notification = if (download.state == Download.STATE_COMPLETED) {
                notificationHelper.buildDownloadCompletedNotification(
                    context,
                    R.drawable.ic_download_done,  /* contentIntent= */
                    contentIntent,
                    Util.fromUtf8Bytes(download.request.data)
                )
            } else if (download.state == Download.STATE_FAILED) {
                notificationHelper.buildDownloadFailedNotification(
                    context,
                    R.drawable.ic_download_done,  /* contentIntent= */
                    contentIntent,
                    Util.fromUtf8Bytes(download.request.data)
                )
            } else {
                return
            }
            //NotificationUtil.setNotification(context, nextNotificationId++, notification);
            nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1
            setLog(
                "OnExoDownload--12",
                "DemoDownloadService-onDownloadChanged-nextNotificationId-$nextNotificationId"
            )
            NotificationUtil.setNotification(context, nextNotificationId, notification)
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            setLog(
                "OnExoDownload--12",
                "DemoDownloadService-onDownloadRemoved-uri-" + download.request.uri
            )
            setLog(
                "OnExoDownload--12",
                "DemoDownloadService-onDownloadRemoved-download.state-" + download.state
            )
            if (download.state == Download.STATE_REMOVING) {
                nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1
                setLog(
                    "OnExoDownload--12",
                    "DemoDownloadService-onDownloadRemoved-nextNotificationId-$nextNotificationId"
                )
                NotificationUtil.setNotification(context, nextNotificationId, null)
            }
        }

        init {
            this.context = context.applicationContext
            this.notificationHelper = notificationHelper
            nextNotificationId = firstNotificationId
            this.contentIntent = contentIntent
        }
    }

    companion object {
        private const val JOB_ID = 1000
        const val FOREGROUND_NOTIFICATION_ID = 1000
    }
}