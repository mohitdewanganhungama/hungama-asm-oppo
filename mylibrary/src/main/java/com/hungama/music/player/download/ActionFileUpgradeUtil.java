package com.hungama.music.player.download;


import static androidx.media3.exoplayer.offline.Download.FAILURE_REASON_NONE;
import static androidx.media3.exoplayer.offline.Download.STATE_QUEUED;
import static androidx.media3.exoplayer.offline.Download.STATE_REMOVING;
import static androidx.media3.exoplayer.offline.Download.STATE_RESTARTING;
import static androidx.media3.exoplayer.offline.Download.STATE_STOPPED;
import static androidx.media3.exoplayer.offline.Download.STOP_REASON_NONE;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.WorkerThread;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.DefaultDownloadIndex;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadRequest;


import java.io.File;
import java.io.IOException;

/** Utility class for upgrading legacy action files into {@link DefaultDownloadIndex}. */
@OptIn(markerClass = UnstableApi.class)
public final class ActionFileUpgradeUtil {

    /** Provides download IDs during action file upgrade. */
    public interface DownloadIdProvider {

        /**
         * Returns a download id for given request.
         *
         * @param downloadRequest The request for which an ID is required.
         * @return A corresponding download ID.
         */
        String getId(DownloadRequest downloadRequest);
    }

    private ActionFileUpgradeUtil() {
    }

     /**
     * Merges {@link DownloadRequest DownloadRequests} contained in a legacy action file into a {@link
     * DefaultDownloadIndex}, deleting the action file if the merge is successful or if {@code
     * deleteOnFailure} is {@code true}.
     *
     * <p>This method must not be called while the {@link DefaultDownloadIndex} is being used by a
     * {@link DownloadManager}.
     *
     * <p>This method may be slow and shouldn't normally be called on the main thread.
     *
     * @param actionFilePath The action file path.
     * @param downloadIdProvider A download ID provider, or {@code null}. If {@code null} then ID of
     *     each download will be its custom cache key if one is specified, or else its URL.
     * @param downloadIndex The index into which the requests will be merged.
     * @param deleteOnFailure Whether to delete the action file if the merge fails.
     * @param addNewDownloadsAsCompleted Whether to add new downloads as completed.
     * @throws IOException If an error occurs loading or merging the requests.
     */
    @WorkerThread
    public static void upgradeAndDelete(
            File actionFilePath,
            @Nullable ActionFileUpgradeUtil.DownloadIdProvider downloadIdProvider,
            DefaultDownloadIndex downloadIndex,
            boolean deleteOnFailure,
            boolean addNewDownloadsAsCompleted)
            throws IOException {
        ActionFile actionFile = new ActionFile(actionFilePath);
        if (actionFile.exists()) {
            boolean success = false;
            try {
                long nowMs = System.currentTimeMillis();
                for (DownloadRequest request : actionFile.load()) {
                    if (downloadIdProvider != null) {
                        request = request.copyWithId(downloadIdProvider.getId(request));
                    }
                    mergeRequest(request, downloadIndex, addNewDownloadsAsCompleted, nowMs);
                }
                success = true;
            } finally {
                if (success || deleteOnFailure) {
                    actionFile.delete();
                }
            }
        }
    }

    /**
     * Merges a {@link DownloadRequest} into a {@link DefaultDownloadIndex}.
     *
     * @param request The request to be merged.
     * @param downloadIndex The index into which the request will be merged.
     * @param addNewDownloadAsCompleted Whether to add new downloads as completed.
     * @throws IOException If an error occurs merging the request.
     */
    /* package */ static void mergeRequest(
            DownloadRequest request,
            DefaultDownloadIndex downloadIndex,
            boolean addNewDownloadAsCompleted,
            long nowMs)
            throws IOException {
        @Nullable Download download = downloadIndex.getDownload(request.id);
        if (download != null) {
            download = mergeRequest(download, request, download.stopReason, nowMs);
        } else {
            download =
                    new Download(
                            request,
                            addNewDownloadAsCompleted ? Download.STATE_COMPLETED : STATE_QUEUED,
                            /* startTimeMs= */ nowMs,
                            /* updateTimeMs= */ nowMs,
                            /* contentLength= */ C.LENGTH_UNSET,
                            Download.STOP_REASON_NONE,
                            Download.FAILURE_REASON_NONE);
        }
        downloadIndex.putDownload(download);
    }
    static Download mergeRequest(
            Download download, DownloadRequest request, int stopReason, long nowMs) {
        @Download.State int state = download.state;
        // Treat the merge as creating a new download if we're currently removing the existing one, or
        // if the existing download is in a terminal state. Else treat the merge as updating the
        // existing download.
        long startTimeMs =
                state == STATE_REMOVING || download.isTerminalState() ? nowMs : download.startTimeMs;
        if (state == STATE_REMOVING || state == STATE_RESTARTING) {
            state = STATE_RESTARTING;
        } else if (stopReason != STOP_REASON_NONE) {
            state = STATE_STOPPED;
        } else {
            state = STATE_QUEUED;
        }
        return new Download(
                download.request.copyWithMergedRequest(request),
                state,
                startTimeMs,
                /* updateTimeMs= */ nowMs,
                /* contentLength= */ C.LENGTH_UNSET,
                stopReason,
                FAILURE_REASON_NONE);
    }

}
