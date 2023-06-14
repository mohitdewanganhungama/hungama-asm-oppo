package com.hungama.music.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hungama.music.data.database.converters.BodyItemArrayConverter
import com.hungama.music.data.database.converters.HomeStoryItemArrayConverter
import com.hungama.music.data.database.converters.MiscItemArrayConverter
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hungama.music.HungamaMusicApp
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.converters.StringTypeConverter
import com.hungama.music.data.database.converters.TimestampConverter
import com.hungama.music.data.database.dao.*
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.DurationModel
import com.hungama.music.data.model.SongDurationModel


@Database(version = 25,exportSchema=true,entities = arrayOf(Track::class, BodyRowsItemsItem::class, DownloadQueue::class, DownloadedAudio::class, SongDurationModel::class))
@TypeConverters(
    MiscItemArrayConverter::class,
    StringTypeConverter::class,
    BodyItemArrayConverter::class,
    HomeStoryItemArrayConverter::class,
    TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        val DATABASE_NAME = "music.db"
        var appDatabase: AppDatabase? = null

        public fun getInstance(): AppDatabase? {
            if (appDatabase == null) {
                appDatabase = buildDatabase(HungamaMusicApp.getInstance())
            }
            return appDatabase
        }

        private fun buildDatabase(appContext: Context): AppDatabase? {
            appDatabase = Room.databaseBuilder(
                appContext,
                AppDatabase::class.java, DATABASE_NAME
                //).allowMainThreadQueries().build()
            ).allowMainThreadQueries().fallbackToDestructiveMigration().addMigrations(
                MIGRATION_1_2,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20,
                MIGRATION_20_21,
                MIGRATION_21_22,
                MIGRATION_22_23,
                MIGRATION_23_24,
                MIGRATION_24_25
            ).build()
            return appDatabase
        }

        fun destroyDataBase() {
            appDatabase = null
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE track RENAME TO temp_track")
                database.execSQL("CREATE TABLE IF NOT EXISTS `track` (`uniquePosition` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` INTEGER NOT NULL, `title` TEXT, `subTitle` TEXT, `image` TEXT, `url` TEXT, `drmlicence` TEXT, `defaultAlbumArtRes` INTEGER NOT NULL, `state` TEXT NOT NULL, `songLyricsUrl` TEXT, `heading` TEXT, `artistName` TEXT, `playerType` TEXT, `isLiked` INTEGER NOT NULL)")
                database.execSQL(
                    "INSERT INTO track(id, title, subTitle, image, url, drmlicence, defaultAlbumArtRes, state, songLyricsUrl, heading, artistName, playerType, isLiked)\n" +
                            "SELECT id, title, subTitle, image, url, drmlicence, defaultAlbumArtRes, state, songLyricsUrl, heading, artistName, playerType, isLiked\n" +
                            "FROM temp_track"
                )
                database.execSQL("DROP TABLE temp_track")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BodyRowsItemsItem ADD COLUMN 'added_date_Time' TEXT DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BodyRowsItemsItem ADD COLUMN 'sr_no' INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue RENAME TO download_queue_temp")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `download_queue` (`qId` INTEGER PRIMARY KEY AUTOINCREMENT, `contentId` TEXT, `title` TEXT, `subTitle` TEXT, `playableUrl` TEXT, `downloadUrl` TEXT, `lyricsUrl` TEXT, `downloadManagerId` INTEGER NOT NULL, `downloadStatus` INTEGER NOT NULL, `parentId` TEXT, `pName` TEXT, `pType` INTEGER NOT NULL, `contentType` INTEGER NOT NULL, `originalAlbumName` TEXT, `podcastAlbumName` TEXT, `releaseDate` TEXT, `actor` TEXT, `singer` TEXT, `lyricist` TEXT, `genre` TEXT, `subGenre` TEXT, `mood` TEXT, `tempo` TEXT, `language` TEXT, `musicDirectorComposer` TEXT, `releaseYear` TEXT, `category` TEXT, `rating` TEXT, `cast_enabled` INTEGER NOT NULL, `ageRating` TEXT, `criticRating` TEXT, `keywords` TEXT, `episodeNumber` TEXT, `seasonNumber` TEXT, `subtitleEnabled` INTEGER NOT NULL, `selectedSubtitleLanguage` TEXT, `lyricsType` TEXT, `userRating` TEXT, `videoQuality` TEXT, `audioQuality` TEXT, `label` TEXT, `labelId` TEXT, `isOriginal` TEXT, `contentPayType` TEXT, `itype` INTEGER NOT NULL, `type` INTEGER NOT NULL, `image` TEXT, `duration` INTEGER NOT NULL, `cast` TEXT, `explicit` INTEGER NOT NULL, `pid` TEXT, `movierights` TEXT, `attribute_censor_rating` TEXT, `nudity` TEXT, `playcount` INTEGER NOT NULL, `s_artist` TEXT, `artist` TEXT, `lyricsLanguage` TEXT, `lyricsLanguageId` TEXT, `lyricsFilePath` TEXT, `fav_count` INTEGER NOT NULL, `synopsis` TEXT, `description` TEXT, `vendor` TEXT, `countEraFrom` TEXT, `countEraTo` TEXT, `skipCreditET` INTEGER NOT NULL, `skipCreditST` INTEGER NOT NULL, `skipIntroET` INTEGER NOT NULL, `skipIntroST` INTEGER NOT NULL, `userId` TEXT, `thumbnailPath` TEXT)"
                )
                database.execSQL(
                    "INSERT INTO download_queue(contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadStatus, parentId, pType, contentType)\n" +
                            "SELECT contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadStatus, chartId, 3, 1\n" +
                            "FROM download_queue_temp"
                )
                database.execSQL("DROP TABLE download_queue_temp")

                database.execSQL("ALTER TABLE downloaded_audio RENAME TO downloaded_audio_temp")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `downloaded_audio` (`aId` INTEGER PRIMARY KEY AUTOINCREMENT, `contentId` TEXT, `title` TEXT, `subTitle` TEXT, `playableUrl` TEXT, `downloadUrl` TEXT, `lyricsUrl` TEXT, `downloadManagerId` INTEGER NOT NULL, `downloadedFilePath` TEXT NOT NULL, `totalDownloadBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `downloadStatus` INTEGER NOT NULL, `downloadNetworkType` INTEGER NOT NULL, `createdDT` INTEGER NOT NULL, `parentId` TEXT, `pName` TEXT, `pType` INTEGER NOT NULL, `contentType` INTEGER NOT NULL, `originalAlbumName` TEXT, `podcastAlbumName` TEXT, `releaseDate` TEXT, `actor` TEXT, `singer` TEXT, `lyricist` TEXT, `genre` TEXT, `subGenre` TEXT, `mood` TEXT, `tempo` TEXT, `language` TEXT, `musicDirectorComposer` TEXT, `releaseYear` TEXT, `category` TEXT, `rating` TEXT, `cast_enabled` INTEGER, `ageRating` TEXT, `criticRating` TEXT, `keywords` TEXT, `episodeNumber` TEXT, `seasonNumber` TEXT, `subtitleEnabled` INTEGER, `selectedSubtitleLanguage` TEXT, `lyricsType` TEXT, `userRating` TEXT, `videoQuality` TEXT, `audioQuality` TEXT, `label` TEXT, `labelId` TEXT, `isOriginal` TEXT, `contentPayType` TEXT, `itype` INTEGER, `type` INTEGER, `image` TEXT, `duration` INTEGER, `cast` TEXT, `explicit` INTEGER, `pid` TEXT, `movierights` TEXT, `attribute_censor_rating` TEXT, `nudity` TEXT, `playcount` INTEGER, `s_artist` TEXT, `artist` TEXT, `lyricsLanguage` TEXT, `lyricsLanguageId` TEXT, `lyricsFilePath` TEXT, `fav_count` INTEGER, `synopsis` TEXT, `description` TEXT, `vendor` TEXT, `countEraFrom` TEXT, `countEraTo` TEXT, `skipCreditET` INTEGER, `skipCreditST` INTEGER, `skipIntroET` INTEGER, `skipIntroST` INTEGER, `userId` TEXT, `thumbnailPath` TEXT)"
                )
                database.execSQL(
                    "INSERT INTO downloaded_audio(contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadedFilePath, totalDownloadBytes, downloadedBytes, downloadStatus, downloadNetworkType, createdDT, parentId, pType, contentType)\n" +
                            "SELECT contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadedFilePath, totalDownloadBytes, downloadedBytes, downloadStatus, downloadNetworkType, createdDT, chartId, 3, 1\n" +
                            "FROM downloaded_audio_temp"
                )
                database.execSQL("DROP TABLE downloaded_audio_temp")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue RENAME TO download_queue_temp")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `download_queue` (`qId` INTEGER PRIMARY KEY AUTOINCREMENT, `contentId` TEXT, `title` TEXT, `subTitle` TEXT, `playableUrl` TEXT, `downloadUrl` TEXT, `lyricsUrl` TEXT, `downloadManagerId` INTEGER NOT NULL, `downloadStatus` INTEGER NOT NULL, `parentId` TEXT, `pName` TEXT, `pType` INTEGER NOT NULL, `contentType` INTEGER NOT NULL, `originalAlbumName` TEXT, `podcastAlbumName` TEXT, `releaseDate` TEXT, `actor` TEXT, `singer` TEXT, `lyricist` TEXT, `genre` TEXT, `subGenre` TEXT, `mood` TEXT, `tempo` TEXT, `language` TEXT, `musicDirectorComposer` TEXT, `releaseYear` TEXT, `category` TEXT, `rating` TEXT, `cast_enabled` INTEGER NOT NULL, `ageRating` TEXT, `criticRating` TEXT, `keywords` TEXT, `episodeNumber` TEXT, `seasonNumber` TEXT, `subtitleEnabled` INTEGER NOT NULL, `selectedSubtitleLanguage` TEXT, `lyricsType` TEXT, `userRating` TEXT, `videoQuality` TEXT, `audioQuality` TEXT, `label` TEXT, `labelId` TEXT, `isOriginal` TEXT, `contentPayType` TEXT, `itype` INTEGER NOT NULL, `type` INTEGER NOT NULL, `image` TEXT, `duration` INTEGER NOT NULL, `cast` TEXT, `explicit` INTEGER NOT NULL, `pid` TEXT, `movierights` TEXT, `attribute_censor_rating` TEXT, `nudity` TEXT, `playcount` INTEGER NOT NULL, `s_artist` TEXT, `artist` TEXT, `lyricsLanguage` TEXT, `lyricsLanguageId` TEXT, `lyricsFilePath` TEXT, `fav_count` INTEGER NOT NULL, `synopsis` TEXT, `description` TEXT, `vendor` TEXT, `countEraFrom` TEXT, `countEraTo` TEXT, `skipCreditET` INTEGER NOT NULL, `skipCreditST` INTEGER NOT NULL, `skipIntroET` INTEGER NOT NULL, `skipIntroST` INTEGER NOT NULL, `userId` TEXT, `thumbnailPath` TEXT, `pSubName` TEXT, `pReleaseDate` TEXT, `pDescription` TEXT, `pNudity` TEXT, `pRatingCritics` TEXT, `pMovieRights` TEXT, `pGenre` TEXT, `pLanguage` TEXT, `pImage` TEXT, `heading` TEXT)"
                )
                database.execSQL(
                    "INSERT INTO download_queue(contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadStatus, parentId, pType, contentType)\n" +
                            "SELECT contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadStatus, parentId, 3, 1\n" +
                            "FROM download_queue_temp"
                )
                database.execSQL("DROP TABLE download_queue_temp")

                database.execSQL("ALTER TABLE downloaded_audio RENAME TO downloaded_audio_temp")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `downloaded_audio` (`aId` INTEGER PRIMARY KEY AUTOINCREMENT, `contentId` TEXT, `title` TEXT, `subTitle` TEXT, `playableUrl` TEXT, `downloadUrl` TEXT, `lyricsUrl` TEXT, `downloadManagerId` INTEGER NOT NULL, `downloadedFilePath` TEXT NOT NULL, `totalDownloadBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `downloadStatus` INTEGER NOT NULL, `downloadNetworkType` INTEGER NOT NULL, `createdDT` INTEGER NOT NULL, `parentId` TEXT, `pName` TEXT, `pType` INTEGER NOT NULL, `contentType` INTEGER NOT NULL, `originalAlbumName` TEXT, `podcastAlbumName` TEXT, `releaseDate` TEXT, `actor` TEXT, `singer` TEXT, `lyricist` TEXT, `genre` TEXT, `subGenre` TEXT, `mood` TEXT, `tempo` TEXT, `language` TEXT, `musicDirectorComposer` TEXT, `releaseYear` TEXT, `category` TEXT, `rating` TEXT, `cast_enabled` INTEGER, `ageRating` TEXT, `criticRating` TEXT, `keywords` TEXT, `episodeNumber` TEXT, `seasonNumber` TEXT, `subtitleEnabled` INTEGER, `selectedSubtitleLanguage` TEXT, `lyricsType` TEXT, `userRating` TEXT, `videoQuality` TEXT, `audioQuality` TEXT, `label` TEXT, `labelId` TEXT, `isOriginal` TEXT, `contentPayType` TEXT, `itype` INTEGER, `type` INTEGER, `image` TEXT, `duration` INTEGER, `cast` TEXT, `explicit` INTEGER, `pid` TEXT, `movierights` TEXT, `attribute_censor_rating` TEXT, `nudity` TEXT, `playcount` INTEGER, `s_artist` TEXT, `artist` TEXT, `lyricsLanguage` TEXT, `lyricsLanguageId` TEXT, `lyricsFilePath` TEXT, `fav_count` INTEGER, `synopsis` TEXT, `description` TEXT, `vendor` TEXT, `countEraFrom` TEXT, `countEraTo` TEXT, `skipCreditET` INTEGER, `skipCreditST` INTEGER, `skipIntroET` INTEGER, `skipIntroST` INTEGER, `userId` TEXT, `thumbnailPath` TEXT, `pSubName` TEXT, `pReleaseDate` TEXT, `pDescription` TEXT, `pNudity` TEXT, `pRatingCritics` TEXT, `pMovieRights` TEXT, `pGenre` TEXT, `pLanguage` TEXT, `pImage` TEXT, `heading` TEXT)"
                )
                database.execSQL(
                    "INSERT INTO downloaded_audio(contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadedFilePath, totalDownloadBytes, downloadedBytes, downloadStatus, downloadNetworkType, createdDT, parentId, pType, contentType)\n" +
                            "SELECT contentId, title, subTitle, playableUrl, downloadUrl, lyricsUrl, downloadManagerId, downloadedFilePath, totalDownloadBytes, downloadedBytes, downloadStatus, downloadNetworkType, createdDT, parentId, 3, 1\n" +
                            "FROM downloaded_audio_temp"
                )
                database.execSQL("DROP TABLE downloaded_audio_temp")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE downloaded_audio SET pType = 3, contentType = 1")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'downloadAll' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'parentThumbnailPath' TEXT DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'downloadRetry' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'isFavorite' INTEGER  NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'downloadAll' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'parentThumbnailPath' TEXT DEFAULT ''")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'downloadRetry' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'isFavorite' INTEGER  NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'downloadedFilePath' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'totalDownloadBytes' INTEGER  NOT NULL DEFAULT -1")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'downloadedBytes' INTEGER  NOT NULL DEFAULT -1")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'downloadNetworkType' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'createdDT' INTEGER  NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'planName' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'planType' INTEGER  NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'planName' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'planType' INTEGER  NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'contentStreamDate' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'contentStreamDuration' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'percentDownloaded' INTEGER  NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'contentStreamDate' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'contentStreamDuration' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'percentDownloaded' INTEGER  NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'contentStartDate' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'contentExpiryDate' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'contentPlayValidity' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'drmLicense' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'contentShareLink' TEXT  NOT NULL DEFAULT ''")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'contentStartDate' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'contentExpiryDate' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'contentPlayValidity' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'drmLicense' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'contentShareLink' TEXT  NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'isSelected' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'isDeleted' INTEGER  NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'isSelected' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'isDeleted' INTEGER  NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BodyRowsItemsItem ADD COLUMN 'adUnitId' TEXT  NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'restrictedDownload' INTEGER  NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'restrictedDownload' INTEGER  NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE track ADD COLUMN 'explicit' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE track ADD COLUMN 'restrictedDownload' INTEGER  NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE track ADD COLUMN 'attributeCensorRating' TEXT  NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'downloadManagerExoPlayerId' TEXT  NOT NULL DEFAULT ''")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'downloadManagerExoPlayerId' TEXT  NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE track ADD COLUMN 'pid' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE track ADD COLUMN 'shareUrl' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE track ADD COLUMN 'favCount' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE track ADD COLUMN 'urlKey' TEXT  NOT NULL DEFAULT ''")
            }
        }


        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'source' TEXT  NOT NULL DEFAULT ''")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'source' TEXT  NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("ALTER TABLE download_queue ADD COLUMN 'source' TEXT  NOT NULL DEFAULT ''")

                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'f_playcount' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE download_queue ADD COLUMN 'f_fav_count' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("UPDATE download_queue SET f_fav_count=fav_count")
                database.execSQL("UPDATE download_queue SET f_playcount=playcount")

                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'f_playcount' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE downloaded_audio ADD COLUMN 'f_fav_count' TEXT  NOT NULL DEFAULT ''")
                database.execSQL("UPDATE downloaded_audio SET f_fav_count=fav_count")
                database.execSQL("UPDATE downloaded_audio SET f_playcount=playcount")
            }
        }
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE track ADD COLUMN 'onErrorPlayableUrl' TEXT  NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `songDuration` (`uniquePosition` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`hungama_user_id` TEXT NOT NULL, `user_streamed_min` INTEGER, `entry_created_time` TEXT, `first_stream_start_time` TEXT, `activity_last_updated` TEXT, `current_timestampm` TEXT NOT NULL, `Is_first_stream_started` INTEGER, `stream_max_min_allowed` INTEGER)")
                database.execSQL(
                    "INSERT INTO songDuration(hungama_user_id, user_streamed_min, entry_created_time, first_stream_start_time, activity_last_updated, current_timestampm, Is_first_stream_started, stream_max_min_allowed)\n" +
                            "SELECT hungama_user_id, user_streamed_min, entry_created_time, first_stream_start_time, activity_last_updated, current_timestampm, Is_first_stream_started, stream_max_min_allowed\n" +
                            "FROM songDuration")
                database.execSQL("ALTER TABLE track ADD COLUMN 'movierights' TEXT NOT NULL DEFAULT ''")
            }
        }

    }


        abstract fun trackDao(): TrackDao
        abstract fun recentlyPlayDao(): RecentlyPlayDao
        abstract fun downloadQueue(): DownloadQueueDao
        abstract fun downloadedAudio(): DownloadedAudioDao
        abstract fun songDuration(): SongDuration
}