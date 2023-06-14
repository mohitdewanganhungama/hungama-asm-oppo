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

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.AsyncTask
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.media3.common.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.showToast
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.utils.Constant.stopReasonPause
import com.hungama.music.utils.Constant.stopReasonRemove
import com.hungama.music.R
import java.io.IOException
import java.lang.Exception
import java.util.HashMap
import java.util.concurrent.CopyOnWriteArraySet
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.OfflineLicenseHelper
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector

@OptIn(markerClass = [UnstableApi::class])
/** Tracks media that has been downloaded.  */
class DownloadTracker(
    context: Context,
    httpDataSourceFactory: HttpDataSource.Factory,
    downloadManager: DownloadManager
) {
    /** Listens for changes in the tracked downloads.  */
    interface Listener {
        /** Called when the tracked downloads changed.  */
        fun onDownloadsChanged(
            downloadManager: DownloadManager,
            download: Download
        )

        /*void onDownloadRemoved(@NonNull DownloadManager downloadManager,
                           @NonNull Download download);*/
        fun onDownloadsPausedChanged(downloadManager: DownloadManager, downloadsPaused: Boolean?)

        //void onRequirementsStateChanged(DownloadManager downloadManager, Requirements requirements, int notMetRequirements);
        fun onDownloadProgress(downloads: List<Download>?, progress: Int)
    }

    private val context: Context
    private val httpDataSourceFactory: HttpDataSource.Factory
    private val listeners: CopyOnWriteArraySet<Listener>
    private val downloads: HashMap<Uri, Download>
    private val downloadIndex: DownloadIndex
    private val trackSelectorParameters: DefaultTrackSelector.Parameters
    private var startDownloadDialogHelper: StartDownloadDialogHelper? = null
    fun addListener(listener: Listener) {
        Assertions.checkNotNull(listener)
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isDownloaded(mediaItem: MediaItem): Boolean {
        val download = downloads[Assertions.checkNotNull(mediaItem.playbackProperties).uri]
        return download != null && download.state != Download.STATE_FAILED
    }

    fun getDownloadState(mediaItem: MediaItem): Int {
        val download = downloads[Assertions.checkNotNull(
            mediaItem.playbackProperties
        ).uri]
        return download?.state ?: -1
    }

    fun getDownloadState(url: String?): Int {
        val uri = Uri.parse(url)
        val download = downloads[uri]
        return download?.state ?: -1
    }

    fun isDownloaded(url: String?): Boolean {
        val uri = Uri.parse(url)
        val download = downloads[uri]
        return download != null && download.state != Download.STATE_FAILED
    }

    fun getDownloadRequest(uri: Uri): DownloadRequest? {
        val download = downloads[uri]
        return download?.request
    }

    fun getDownload(url: String?): Download? {
        val uri = Uri.parse(url)
        return downloads[uri]
    }

    fun toggleDownload(
        fragmentManager: FragmentManager?, mediaItem: MediaItem, renderersFactory: RenderersFactory?
    ) {
        val download = downloads[Assertions.checkNotNull(
            mediaItem.playbackProperties
        ).uri]
        setLog(TAG, "DRM toggleDownload download:$download")
        if (download != null && (download.state == Download.STATE_QUEUED || download.state == Download.STATE_DOWNLOADING)) {
            //resumeDownload();
        } else if (download != null && download.state != Download.STATE_FAILED) {
            DownloadService.sendRemoveDownload(
                context,
                DemoDownloadService::class.java,
                download.request.id,  /* foreground= */
                false
            )
        } else {
            if (startDownloadDialogHelper != null) {
                startDownloadDialogHelper!!.release()
            }
            startDownloadDialogHelper = StartDownloadDialogHelper(
                fragmentManager!!,
                DownloadHelper.forMediaItem(
                    context, mediaItem, renderersFactory, httpDataSourceFactory
                ),
                mediaItem
            )
        }
    }

    private fun loadDownloads() {
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    downloads[download.request.uri] = download
                }
            }
        } catch (e: IOException) {
            setLog("Failed to query downloads", e.message.toString())
        }
    }

    private inner class DownloadManagerListener : DownloadManager.Listener {
        override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
            downloads[download.request.uri] = download
            for (listener in listeners) {
                setLog("OnExoDownload1", download.toString())
                listener.onDownloadsChanged(downloadManager, download)
            }
        }

        override fun onDownloadRemoved(
            downloadManager: DownloadManager, download: Download
        ) {
            downloads.remove(download.request.uri)
            for (listener in listeners) {
                setLog("OnExoDownload2", download.toString())
                listener.onDownloadsChanged(downloadManager, download)
            }
        }

        override fun onDownloadsPausedChanged(
            downloadManager: DownloadManager,
            downloadsPaused: Boolean
        ) {
            for (listener in listeners) {
                setLog("OnExoDownload3", downloadsPaused.toString())
                listener.onDownloadsPausedChanged(downloadManager, downloadsPaused)
            }
        }

        override fun onRequirementsStateChanged(
            downloadManager: DownloadManager,
            requirements: Requirements,
            notMetRequirements: Int
        ) {
            for (listener in listeners) {
                setLog("OnExoDownload4", notMetRequirements.toString())
                //listener.onRequirementsStateChanged(downloadManager, requirements, notMetRequirements);
            }
        }
    }

    private inner class StartDownloadDialogHelper(
        private val fragmentManager: FragmentManager,
        private val downloadHelper: DownloadHelper,
        private val mediaItem: MediaItem
    ) : DownloadHelper.Callback, DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, TrackSelectionDialog.TrackSelectionListener {
        private var trackSelectionDialog: TrackSelectionDialog? = null
        private var mappedTrackInfo: MappingTrackSelector.MappedTrackInfo? = null
        private var widevineOfflineLicenseFetchTask: WidevineOfflineLicenseFetchTask? = null
        private var keySetId: ByteArray?=null
        fun release() {
            downloadHelper.release()
            if (trackSelectionDialog != null && trackSelectionDialog!!.isAdded) {
                trackSelectionDialog!!.dismiss()
            }
            if (widevineOfflineLicenseFetchTask != null) {
                widevineOfflineLicenseFetchTask!!.cancel(false)
            }
        }

        // DownloadHelper.Callback implementation.
        override fun onPrepared(helper: DownloadHelper) {
            val format = getFirstFormatWithDrmInitData(helper)
            if (format == null) {
                onDownloadPrepared(helper)
                return
            }

            // The content is DRM protected. We need to acquire an offline license.
            if (Util.SDK_INT < 18) {
                val messageModel = MessageModel(
                    context.getString(R.string.music_player_str_5),
                    MessageType.NEUTRAL,
                    true
                )
                showToast(context, messageModel)
                setLog(
                    TAG,
                    "Downloading DRM protected content is not supported on API versions below 18"
                )
                return
            }
            // TODO(internal b/163107948): Support cases where DrmInitData are not in the manifest.
            if (!hasSchemaData(format.drmInitData)) {
                val messageModel = MessageModel(
                    context.getString(R.string.music_player_str_3),
                    MessageType.NEUTRAL,
                    true
                )
                showToast(context, messageModel)
                setLog(
                    TAG,
                    "Downloading content where DRM scheme data is not located in the manifest is not"
                            + " supported"
                )
                return
            }
            widevineOfflineLicenseFetchTask = WidevineOfflineLicenseFetchTask(
                format,
                mediaItem.playbackProperties!!.drmConfiguration,
                httpDataSourceFactory,  /* dialogHelper= */
                this,
                helper
            )
            widevineOfflineLicenseFetchTask!!.execute()
        }

        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            val isLiveContent = e is DownloadHelper.LiveContentUnsupportedException
            val toastStringId =
                if (isLiveContent) R.string.music_player_str_4 else R.string.music_player_str_2
            val logMessage =
                if (isLiveContent) "Downloading live content unsupported" else "Failed to start download"
            val messageModel =
                MessageModel(context.getString(toastStringId), MessageType.NEUTRAL, true)
            showToast(context, messageModel)
            setLog(logMessage, e.message.toString())
        }

        // DialogInterface.OnClickListener implementation.
        override fun onClick(dialog: DialogInterface, which: Int) {
            for (periodIndex in 0 until downloadHelper.periodCount) {
                downloadHelper.clearTrackSelections(periodIndex)
                /*for (i in 0 until mappedTrackInfo!!.rendererCount) {
                    if (!trackSelectionDialog!!.getIsDisabled( *//* rendererIndex= *//*i)) {
                        downloadHelper.addTrackSelectionForSingleRenderer(
                            periodIndex,  *//* rendererIndex= *//*
                            i,
                            trackSelectorParameters,
                            trackSelectionDialog!!.getOverrides( *//* rendererIndex= *//*i)
                        )
                    }
                }*/
                for (periodIndex in 0 until downloadHelper.periodCount) {
                    downloadHelper.clearTrackSelections(periodIndex)
                    downloadHelper.addTrackSelection(periodIndex, trackSelectorParameters)
                }
            }
            val downloadRequest = buildDownloadRequest()
            if (downloadRequest.streamKeys.isEmpty()) {
                // All tracks were deselected in the dialog. Don't start the download.
                return
            }
            startDownload(downloadRequest)
        }

        // DialogInterface.OnDismissListener implementation.
        override fun onDismiss(dialogInterface: DialogInterface) {
            trackSelectionDialog = null
            downloadHelper.release()
        }
        // Internal methods.
        /**
         * Returns the first [Format] with a non-null [Format.drmInitData] found in the
         * content's tracks, or null if none is found.
         */
        private fun getFirstFormatWithDrmInitData(helper: DownloadHelper): Format? {
            for (periodIndex in 0 until helper.periodCount) {
                val mappedTrackInfo = helper.getMappedTrackInfo(periodIndex)
                for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                    val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                    for (trackGroupIndex in 0 until trackGroups.length) {
                        val trackGroup = trackGroups[trackGroupIndex]
                        for (formatIndex in 0 until trackGroup.length) {
                            val format = trackGroup.getFormat(formatIndex)
                            if (format.drmInitData != null) {
                                return format
                            }
                        }
                    }
                }
            }
            return null
        }

        fun onOfflineLicenseFetched(helper: DownloadHelper, keySetId: ByteArray) {
            this.keySetId = keySetId
            onDownloadPrepared(helper)
        }

        fun onOfflineLicenseFetchedError(e: DrmSession.DrmSessionException) {
            val messageModel = MessageModel(
                context.getString(R.string.music_player_str_3),
                MessageType.NEUTRAL,
                true
            )
            showToast(context, messageModel)
            setLog("Failed to fetch offline DRM license", e.message.toString())
        }

        private fun onDownloadPrepared(helper: DownloadHelper) {
            if (helper.periodCount == 0) {
                setLog(TAG, "No periods found. Downloading entire stream.")
                startDownload()
                downloadHelper.release()
                return
            }
            mappedTrackInfo = downloadHelper.getMappedTrackInfo( /* periodIndex= */0)
            val tracks = downloadHelper.getTracks( /* periodIndex= */0)
            if (!TrackSelectionDialog.willHaveContent(tracks)) {
                setLog(TAG, "No dialog content. Downloading entire stream.")
                startDownload()
                downloadHelper.release()
                return
            }
            trackSelectionDialog = TrackSelectionDialog.createForTracksAndParameters( /* titleId= */
                androidx.media3.exoplayer.R.string.exo_download_description,
                tracks,
                DownloadHelper.getDefaultTrackSelectorParameters(context),  /* allowAdaptiveSelections= */
                false,  /* allowMultipleOverrides= */
                true,  /* onTracksSelectedListener= */
                this,  /* onDismissListener= */
                this
            )
            //trackSelectionDialog.show(fragmentManager,  /* tag= */null)
            /*for (periodIndex in 0 until downloadHelper.periodCount) {
                downloadHelper.clearTrackSelections(periodIndex)
                for (i in 0 until mappedTrackInfo?.rendererCount!!) {
                    if (!trackSelectionDialog?.getIsDisabled( *//* rendererIndex= *//*i)!!) {
                        downloadHelper.addTrackSelectionForSingleRenderer(
                            periodIndex,  *//* rendererIndex= *//*
                            i,
                            trackSelectorParameters,
                            trackSelectionDialog?.getOverrides( *//* rendererIndex= *//*i)!!
                        )
                    }
                }
            }*/
            for (periodIndex in 0 until downloadHelper.periodCount) {
                downloadHelper.clearTrackSelections(periodIndex)
                downloadHelper.addTrackSelection(periodIndex, trackSelectorParameters)
            }
            val downloadRequest = buildDownloadRequest()
            if (downloadRequest.streamKeys.isEmpty()) {
                // All tracks were deselected in the dialog. Don't start the download.
                return
            }
            startDownload(downloadRequest)
        }

        /**
         * Returns whether any the [DrmInitData.SchemeData] contained in `drmInitData` has
         * non-null [DrmInitData.SchemeData.data].
         */
        private fun hasSchemaData(drmInitData: DrmInitData?): Boolean {
            for (i in 0 until drmInitData!!.schemeDataCount) {
                if (drmInitData[i].hasData()) {
                    return true
                }
            }
            return false
        }

        private fun startDownload(downloadRequest: DownloadRequest = buildDownloadRequest()) {
            DownloadService.sendAddDownload(
                context, DemoDownloadService::class.java, downloadRequest,  /* foreground= */false
            )
        }

        private fun buildDownloadRequest(): DownloadRequest {
            return downloadHelper
                .getDownloadRequest(
                    Util.getUtf8Bytes(
                        Assertions.checkNotEmpty(
                            mediaItem.mediaMetadata.title.toString()
                        )
                    )
                )
                .copyWithKeySetId(keySetId)
        }

        init {
            downloadHelper.prepare(this)
        }

        override fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?) {
            for (periodIndex in 0 until downloadHelper.periodCount) {
                downloadHelper.clearTrackSelections(periodIndex)
                downloadHelper.addTrackSelection(periodIndex, trackSelectionParameters!!)
            }
            val downloadRequest = buildDownloadRequest()
            if (downloadRequest.streamKeys.isEmpty()) {
                // All tracks were deselected in the dialog. Don't start the download.
                return
            }
            startDownload(downloadRequest)
        }
    }

    /** Downloads a Widevine offline license in a background thread.  */
    @RequiresApi(18)
    private class WidevineOfflineLicenseFetchTask(
        private val format: Format,
        private val drmConfiguration: MediaItem.DrmConfiguration?,
        private val httpDataSourceFactory: HttpDataSource.Factory,
        private val dialogHelper: StartDownloadDialogHelper,
        private val downloadHelper: DownloadHelper
    ) : AsyncTask<Void?, Void?, Void?>() {
        private var keySetId: ByteArray?=null
        private var drmSessionException: DrmSession.DrmSessionException? = null
        override fun doInBackground(vararg p0: Void?): Void? {
            val offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
                drmConfiguration!!.licenseUri.toString(),
                drmConfiguration.forceDefaultLicenseUri,
                httpDataSourceFactory,
                drmConfiguration.requestHeaders,
                DrmSessionEventListener.EventDispatcher()
            )
            try {
                keySetId = offlineLicenseHelper.downloadLicense(format)
                val licenseDurationRemainingSec =
                    offlineLicenseHelper.getLicenseDurationRemainingSec(
                        keySetId!!
                    )
                setLog(
                    TAG,
                    "doInBackground licenseDurationRemainingSec:$licenseDurationRemainingSec"
                )
            } catch (e: DrmSession.DrmSessionException) {
                drmSessionException = e
            } finally {
                offlineLicenseHelper.release()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            if (drmSessionException != null&& dialogHelper!=null) {
                dialogHelper.onOfflineLicenseFetchedError(drmSessionException!!)
            } else {
                if(dialogHelper!=null&&keySetId!=null){
                    dialogHelper.onOfflineLicenseFetched(
                        downloadHelper,
                        Assertions.checkStateNotNull(keySetId)
                    )
                }

            }
        }
    }

    fun setStopReasonToAllDownload(stopReason: Int, uri: Uri): Boolean {
        // Set the stop reason for a single download.
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    if (download.state != Download.STATE_COMPLETED) {
                        if (download.request.uri == uri) {
                            setLog(
                                "DownloadContent",
                                "DownloadTracker-setStopReasonToAllDownload-DownloadRequest-stop reason set 0 for current download"
                            )
                            DownloadService.sendSetStopReason(
                                context,
                                DemoDownloadService::class.java,
                                download.request.id,
                                0,
                                false
                            )
                        } else {
                            if (stopReason == stopReasonPause) {
                                setLog(
                                    "DownloadContent",
                                    "DownloadTracker-setStopReasonToAllDownload-sendSetStopReason-stop reason set 300 for other download"
                                )
                                DownloadService.sendSetStopReason(
                                    context,
                                    DemoDownloadService::class.java,
                                    download.request.id,
                                    stopReason,
                                    false
                                )
                            } else if (stopReason == stopReasonRemove && download.stopReason != stopReasonPause) {
                                setLog(
                                    "DownloadContent",
                                    "DownloadTracker-setStopReasonToAllDownload-sendRemoveDownload-stop reason set for other download"
                                )
                                DownloadService.sendRemoveDownload(
                                    context,
                                    DemoDownloadService::class.java,
                                    download.request.id,
                                    false
                                )
                            }
                        }
                    }
                }
                return true
            }
        } catch (e: IOException) {
            setLog("Failed to query downloads", e.message.toString())
        } catch (e: Exception) {
        }
        return false
    }

    fun setStopReasonForPauseResumeDownload(stopReason: Int, uri: Uri): Boolean {
        // Set the stop reason for a single download.
        /*try {
      Download download = getDownload(uri);
      if (download != null){
        if (download.stopReason == stopReasonPause){
          CommonUtils.INSTANCE.setLog("DownloadContent", "DownloadTracker-setStopReasonToAllDownload-DownloadRequest-stop reason set 0 for current download");
          DownloadService.sendSetStopReason(
                  context, DemoDownloadService.class, download.request.id,0,false);
          resumeDownload();
        }else {
          CommonUtils.INSTANCE.setLog("DownloadContent", "DownloadTracker-setStopReasonToAllDownload-DownloadRequest-stop reason set 300 for current download");
          DownloadService.sendSetStopReason(
                  context, DemoDownloadService.class, download.request.id,stopReason,false);
        }
      }
      return true;
    } catch (Exception e) {
      setLog(TAG, "Failed to query downloads", e);
      return false;
    }*/
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    if (download.state != Download.STATE_COMPLETED) {
                        setLog(
                            "DownloadContent",
                            "DownloadTracker-setStopReasonForPauseResumeDownload-uri-$uri"
                        )
                        if (download.request.uri == uri && download.stopReason == stopReasonPause) {
                            setLog(
                                "DownloadContent",
                                "DownloadTracker-setStopReasonForPauseResumeDownload-DownloadRequest-stop reason set 0 for current download"
                            )
                            DownloadService.sendSetStopReason(
                                context,
                                DemoDownloadService::class.java,
                                download.request.id,
                                0,
                                false
                            )
                        } else {
                            if (stopReason == stopReasonPause) {
                                setLog(
                                    "DownloadContent",
                                    "DownloadTracker-setStopReasonForPauseResumeDownload-sendSetStopReason-stop reason set 300 for other download"
                                )
                                DownloadService.sendSetStopReason(
                                    context,
                                    DemoDownloadService::class.java,
                                    download.request.id,
                                    stopReason,
                                    false
                                )
                            } else if (stopReason == stopReasonRemove && download.stopReason != stopReasonPause) {
                                setLog(
                                    "DownloadContent",
                                    "DownloadTracker-setStopReasonForPauseResumeDownload-sendRemoveDownload-stop reason set for other download"
                                )
                                DownloadService.sendRemoveDownload(
                                    context,
                                    DemoDownloadService::class.java,
                                    download.request.id,
                                    false
                                )
                            }
                        }
                    }
                }
                return true
            }
        } catch (e: IOException) {
            setLog("Failed to query downloads", e.message.toString())
        } catch (e: Exception) {
        }
        return false
    }

    fun pauseDownload(url: String?) {
        /*DownloadService.sendPauseDownloads(
            context, DemoDownloadService.class, */
        /* foreground= */ /* false);*/
        try {
            val uri = Uri.parse(url)
            setStopReasonForPauseResumeDownload(stopReasonPause, uri)
        } catch (e: Exception) {
        }
    }

    fun resumeDownload() {
        DownloadService.sendResumeDownloads(
            context, DemoDownloadService::class.java,  /* foreground= */false
        )
    }

    fun removeDownload(uri: Uri) {
        val dr = getDownloadRequest(uri)
        setLog("videoDeleted", "DownloadTracker-removeDownload-DownloadRequest-$dr")
        if (dr != null) {
            setLog("videoDeleted", "DownloadTracker-removeDownload-DownloadRequest-delete file")
            DownloadService.sendRemoveDownload(
                context, DemoDownloadService::class.java, dr.id, false
            )
            setLog(
                "videoDeleted",
                "DownloadTracker-removeDownload-DownloadRequest-after delete file"
            )
        }
    }

    fun removeAllDownload() {
        DownloadService.sendRemoveAllDownloads(
            context, DemoDownloadService::class.java,  /* foreground= */false
        )
    }

    suspend fun updateDownloadProgress(downloads: List<Download>) {
        var totalPercentage = 0f
        var downloadTaskCount = 0
        var allDownloadPercentagesUnknown = true
        var haveDownloadedBytes = false
        var haveDownloadTasks = false
        var haveRemoveTasks = false
        for (i in downloads.indices) {
            val download = downloads[i]
            if (download.state == Download.STATE_REMOVING) {
                haveRemoveTasks = true
                continue
            }
            if (download.state != Download.STATE_RESTARTING
                && download.state != Download.STATE_DOWNLOADING
            ) {
                continue
            }
            haveDownloadTasks = true
            val downloadPercentage = download.percentDownloaded
            if (downloadPercentage != C.PERCENTAGE_UNSET.toFloat()) {
                allDownloadPercentagesUnknown = false
                totalPercentage += downloadPercentage
            }
            haveDownloadedBytes = haveDownloadedBytes or (download.bytesDownloaded > 0)
            downloadTaskCount++
        }
        var progress = 0
        var indeterminate = true
        if (haveDownloadTasks) {
            progress = (totalPercentage / downloadTaskCount).toInt()
            setLog("Percentage3", progress.toString())
            indeterminate = allDownloadPercentagesUnknown && haveDownloadedBytes
            for (listener in listeners) {
                listener.onDownloadProgress(downloads, progress)
            }
        }
    }

    companion object {
        private const val TAG = "DownloadTracker"
    }

    init {
        this.context = context.applicationContext
        this.httpDataSourceFactory = httpDataSourceFactory
        listeners = CopyOnWriteArraySet()
        downloads = HashMap()
        downloadIndex = downloadManager.downloadIndex
        trackSelectorParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        downloadManager.addListener(DownloadManagerListener())
        loadDownloads()
    }
}