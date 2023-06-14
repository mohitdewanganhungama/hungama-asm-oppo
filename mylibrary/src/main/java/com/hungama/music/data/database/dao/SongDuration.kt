package com.hungama.music.data.database.dao

import androidx.room.*
import com.hungama.music.data.model.SongDurationModel
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue

@Dao
interface SongDuration {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertOrReplace(songDuration: SongDurationModel)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertOrReplaceAll(songDuration: SongDurationModel)

        @Query("SELECT * from songDuration")
        fun getAllSongDuration(): List<SongDurationModel>

        @Query("SELECT * from songDuration")
        fun getSongDuration(): SongDurationModel

        @Query("Delete from songDuration")
        fun deleteAll()

        @Query("update songDuration set Is_first_stream_started=(:Is_first_stream_started) where hungama_user_id=(:hungama_user_id)")
        fun updateFirstStreamStarted(Is_first_stream_started:Int, hungama_user_id: String): Int

        @Query("update songDuration set user_streamed_min=(:user_streamed_min) where hungama_user_id=(:hungama_user_id)")
        fun updateUserStreamedMin(user_streamed_min:Int, hungama_user_id: String): Int

        @Update(onConflict = OnConflictStrategy.REPLACE)
        fun updateSongDuration(songDuration:SongDurationModel): Int

        @Query("update songDuration set stream_max_min_allowed=(:stream_max_min_allowed) where hungama_user_id=(:hungama_user_id)")
        fun updateStreamMaxMinAllowed(stream_max_min_allowed:Int, hungama_user_id: String): Int

        @Query("Delete from download_queue WHERE qId =(:qId)")
        fun deleteDownloadQueueItem(qId: Long)

        @Update
        fun updateDownloadQueueItem(downloadQueue: DownloadQueue)

        @Query("SELECT * from songDuration WHERE hungama_user_id=(:hungama_user_id) LIMIT 1")
        fun getSongDurationData(hungama_user_id: String): SongDurationModel

        @Query("update download_queue set downloadStatus=(:downloadStatus) where qId = (:qId)")
        fun updateQueueItemDownloadStatus(qId:Long, downloadStatus:Int)

        @Query("SELECT * FROM download_queue WHERE downloadUrl =(:playableUrl) LIMIT 1")
        fun findByPlayableUrl(playableUrl: String): DownloadQueue

}