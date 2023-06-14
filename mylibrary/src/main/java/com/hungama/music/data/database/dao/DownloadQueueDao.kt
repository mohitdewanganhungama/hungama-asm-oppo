package com.hungama.music.data.database.dao

import androidx.room.*
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue

@Dao
interface DownloadQueueDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertOrReplace(downloadQueue: DownloadQueue)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertOrReplaceAll(matchModels: ArrayList<DownloadQueue>)

        @Query("SELECT * from download_queue")
        fun getAllDownloadQueueItems(): List<DownloadQueue>

        @Query("SELECT * from download_queue WHERE downloadStatus != (:statusPause) OR downloadStatus != (:statusFailed) OR downloadStatus != (:statesQueued)")
        fun getAllDownloadQueueItemsByDownloadStates(statusPause:Int, statusFailed:Int, statesQueued:Int): List<DownloadQueue>

        @Query("Delete from download_queue")
        fun deleteAll()

        @Query("update download_queue set downloadStatus=(:downloadStatus) where contentType=(:audio)")
        fun pauseAllAudioDownloads(downloadStatus:Int, audio: Int): Int

        @Query("update download_queue set downloadStatus=(:downloadStatus) where contentType=(:audio)")
        fun resumeAllAudioDownloads(downloadStatus:Int, audio: Int): Int

        @Query("update download_queue set downloadStatus=5")
        fun cancelAll()

        @Query("Delete from download_queue WHERE qId =(:qId)")
        fun deleteDownloadQueueItem(qId: Long)

        @Query("SELECT * FROM download_queue WHERE qId =(:qId) LIMIT 1")
        fun findById(qId: Long): DownloadQueue


        @Query("SELECT * FROM download_queue WHERE contentId =(:contentId) LIMIT 1")
        fun findByContentId(contentId: String): DownloadQueue

        @Update
        fun updateDownloadQueueItem(downloadQueue: DownloadQueue)

        @Query("SELECT * FROM download_queue WHERE downloadManagerId =(:downloadManagerId) LIMIT 1")
        fun findByDownloadManagerId(downloadManagerId: Int): DownloadQueue

        @Query("SELECT * from download_queue WHERE contentType=(:contentType)")
        fun getDownloadQueueItemsByContentType(contentType:Int): List<DownloadQueue>

        @Query("SELECT * from download_queue WHERE contentId=(:contentId) AND contentType=(:contentType) LIMIT 1")
        fun getDownloadQueueItemsByContentIdAndContentType(
                contentId: String, contentType:Int): DownloadQueue

        @Query("SELECT * from download_queue WHERE contentId=(:contentId) AND contentType=(:contentType) AND pType=(:pType) AND pid LIKE (:parentId) LIMIT 1")
        fun getDownloadQueueItemsByContentIdAndContentTypeAndParentTypeAndParentId(
                contentId: String, contentType:Int, pType:Int, parentId:String): DownloadQueue

        @Query("update download_queue set downloadStatus=(:downloadStatus) where qId = (:qId)")
        fun updateQueueItemDownloadStatus(qId:Long, downloadStatus:Int)

        @Query("SELECT * FROM download_queue WHERE downloadUrl =(:playableUrl) LIMIT 1")
        fun findByPlayableUrl(playableUrl: String): DownloadQueue

        @Query("update download_queue set downloadStatus=(:downloadStatus) where contentId = (:contentId)")
        fun updateQueueItemDownloadStatusByContentId(contentId: String, downloadStatus:Int)

        @Query("SELECT * from download_queue WHERE contentType=(:contentTypeAudio) OR contentType=(:contentTypePodcast)")
        fun getDownloadQueueItemsByAudioContentType(contentTypeAudio:Int, contentTypePodcast:Int): List<DownloadQueue>

        @Query("Delete from download_queue WHERE contentType=(:contentTypeAudio) OR contentType=(:contentTypePodcast)")
        fun deleteDownloadQueueItemsByAudioContentType(contentTypeAudio:Int, contentTypePodcast:Int)

        @Query("SELECT * FROM download_queue WHERE contentType IN (:contentTypes)")
        fun getContentsByContentType(contentTypes: Array<Int>): List<DownloadQueue>
}