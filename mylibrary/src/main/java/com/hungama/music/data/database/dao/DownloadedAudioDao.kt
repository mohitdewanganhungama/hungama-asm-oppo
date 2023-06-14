package com.hungama.music.data.database.dao

import androidx.room.*
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio

@Dao
interface DownloadedAudioDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertOrReplace(downloadedAudio: DownloadedAudio)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertOrReplaceAll(matchModels: ArrayList<DownloadedAudio>)

        @Query("SELECT * from downloaded_audio")
        fun getAllDownloadedItems(): List<DownloadedAudio>

        @Query("Delete from downloaded_audio")
        fun deleteAll()

        @Query("Delete from downloaded_audio WHERE aId =(:aId)")
        fun deleteDownloadQueueItem(aId: Long)

        @Query("SELECT * FROM downloaded_audio WHERE aId =(:aId) LIMIT 1")
        fun findById(aId: Long): DownloadedAudio

        @Query("SELECT * FROM downloaded_audio WHERE contentId =(:contentId) LIMIT 1")
        fun findByContentId(contentId: String): DownloadedAudio

        @Update
        fun updateDownloadQueueItem(downloadedAudio: DownloadedAudio)

        @Query("UPDATE downloaded_audio SET playableUrl = :playableUrl, downloadUrl = :downloadUrl WHERE aId =:aId")
        fun updateDownloadedAudioUrls(
                playableUrl: String?,
                downloadUrl: String?,
                aId: String?
        )

        @Query("SELECT * FROM downloaded_audio WHERE downloadManagerId =(:downloadManagerId) LIMIT 1")
        fun findByDownloadManagerId(downloadManagerId: Int): DownloadedAudio

        @Query("Delete from downloaded_audio WHERE contentId =(:contentId)")
        fun deleteDownloadQueueItemByContentId(contentId: String)

        @Query("UPDATE downloaded_audio SET thumbnailPath = :thumbnailPath WHERE contentId =:contentId")
        fun updateDownloadedImageThumbnailPath(
                thumbnailPath: String?,
                contentId: String?
        )

        @Query("UPDATE downloaded_audio SET parentThumbnailPath = :parentThumbnailPath WHERE parentId =:parentId")
        fun updateDownloadedImageParentThumbnailPath(
                parentThumbnailPath: String?,
                parentId: String?
        )

        @Query("UPDATE downloaded_audio SET lyricsFilePath = :lyricsFilePath WHERE contentId =:contentId")
        fun updateDownloadedlyricsFilePathPath(
                lyricsFilePath: String?,
                contentId: String?
        )

        @Query("SELECT * from downloaded_audio WHERE contentType=(:contentType) group by contentId order by createdDT desc")
        fun getDownloadQueueItemsByContentType(contentType:Int): List<DownloadedAudio>

        @Query("SELECT * from downloaded_audio WHERE contentType=(:contentType) group by contentId order by createdDT asc")
        fun getDownloadQueueItemsByContentTypeByAsc(contentType:Int): List<DownloadedAudio>

        @Query("SELECT * from downloaded_audio WHERE contentId=(:contentId) AND contentType=(:contentType) LIMIT 1")
        fun getDownloadedAudioItemsByContentIdAndContentType(
                contentId: String, contentType:Int): DownloadedAudio

        @Query("SELECT * from downloaded_audio WHERE contentId=(:contentId) AND contentType=(:contentType) AND pType=(:pType) AND pid LIKE (:parentId) LIMIT 1")
        fun getDownloadedAudioItemsByContentIdAndContentTypeAndParentTypeAndParentId(
                contentId: String, contentType:Int, pType:Int, parentId:String): DownloadedAudio

        @Query("select * from downloaded_audio where pType=6 and downloadAll=1 Group By pName")
        fun getPlayList(): List<DownloadedAudio>

        @Query("select * from downloaded_audio where pType=16 Group By pName")
        fun getUserPlayList(): List<DownloadedAudio>

        @Query("select * from downloaded_audio where pType=6 and downloadAll=1 and parentId=(:parentId)")
        fun getPlayList(parentId:String): List<DownloadedAudio>

        @Query("select * from downloaded_audio where pType=16 and parentId=(:parentId)")
        fun getUserPlayList(parentId:String): List<DownloadedAudio>

        @Query("select * from downloaded_audio where pType=1 and downloadAll=1 Group By pName")
        fun getAlbumList(): List<DownloadedAudio>

        @Query("select * from downloaded_audio where pType=7 and downloadAll=0 Group By pName")
        fun getPodcastList(): List<DownloadedAudio>

        @Query("select * from downloaded_audio where pType=1 and downloadAll=1 and parentId=(:parentId)")
        fun getAlbumList(parentId:String): List<DownloadedAudio>

        @Query("SELECT * FROM downloaded_audio WHERE downloadUrl =(:playableUrl) LIMIT 1")
        fun findByPlayableUrl(playableUrl: String): DownloadedAudio

        @Query("SELECT * FROM downloaded_audio WHERE contentType IN (:contentTypes) group by contentId")
        fun getContentsByContentType(contentTypes: Array<Int>): List<DownloadedAudio>

        @Query("UPDATE downloaded_audio SET contentStartDate = :contentStartDate, contentExpiryDate = :contentExpiryDate, contentPlayValidity = :contentPlayValidity WHERE contentId =:contentId")
        fun updateDownloadedContentValidityData(
                contentStartDate: String,
                contentExpiryDate: String,
                contentPlayValidity:Int,
                contentId: String
        )

        @Query("UPDATE downloaded_audio SET contentStreamDate = :contentStreamDate WHERE contentId =:contentId")
        fun updateDownloadedContentStreamDate(
                contentStreamDate: Long,
                contentId: String
        )

        @Query("SELECT * FROM downloaded_audio WHERE contentType IN (:contentTypes) GROUP BY contentId")
        fun getUniqueContentsByContentType(contentTypes: Array<Int>): List<DownloadedAudio>

        @Query("UPDATE downloaded_audio SET isFavorite = :isFavorite WHERE contentId =:contentId")
        fun updateDownloadedFavorite(
                isFavorite: Boolean?,
                contentId: String?
        )
}