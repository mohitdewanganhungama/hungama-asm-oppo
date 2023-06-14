package com.hungama.music.data.database.oldappdata

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.*
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.hungama.fetch2.Status
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.DetailPages
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.convertStringToJsonObject
import com.hungama.music.utils.CommonUtils.saveToInternalStorage
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.toBitmap
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.utils.download.Data
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.BuildConfig
import kotlinx.coroutines.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import kotlin.Exception


class DatabaseDataMigrationWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    var downloadedItems:List<MediaItemOldApp> = ArrayList()
    var downloadedItemsForVideo:List<MediaItemOldApp> = ArrayList()
    companion object {
        var lastUpdatedItemPosition = 0
        var errorCount = 0
        var lastUpdatedItemPositionForVideo = 0
        var errorCountForVideo = 0
        var lastUpdatedItemArtworkPosition = 0
        var errorCountForAudioArtwork = 0
        fun enqueue(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()

                val workRequestBuilder =
                    OneTimeWorkRequestBuilder<DatabaseDataMigrationWorker>().setConstraints(
                        constraints
                    )

                WorkManager.getInstance(context).enqueueUniqueWork(
                    DatabaseDataMigrationWorker::class.java.simpleName,
                    ExistingWorkPolicy.KEEP, workRequestBuilder.build()
                )
            }catch (e:Exception){
                try {
                    if (!BuildConfig.DEBUG){
                        Firebase.crashlytics.recordException(e)
                    }else{
                        //throw e
                    }
                } catch (e:Exception) {

                }
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Block on the task for a maximum of 60 seconds, otherwise time out.
            //val taskResult = Tasks.await(doUpdationTask(), 60, TimeUnit.SECONDS)
            doMusicLanguageMigration()
            downloadedItems = DBOHandler.getAllCachedTracks(context)
            setLog("downloadMigration","downloadedItems"+downloadedItems.toString())
            downloadedItemsForVideo = DBOHandler.getAllCachedVideoTracks(context)
            setLog("downloadMigration","downloadedItemsForVideo"+downloadedItemsForVideo.toString())
            doDownloadMigration()
            doDownloadMigrationForVideo()
            /*for (item in downloadedItems.iterator()){
                setLog("downloadMigration " + item.id, "TrackId - " + item.trackId)
                setLog("downloadMigration " + item.id, "FilePath - " + item.filePath)
                setLog("downloadMigration " + item.id, "JsonDetails - " + item.jsonDetails)
                setLog("downloadMigration " + item.id, "State - " + item.state)
                setLog("downloadMigration " + item.id, "Progress - " + item.progress)
                setLog("downloadMigration " + item.id, "TimeStamp - " + item.timeStamp)
            }*/
            setLog("downloadMigration","Success")
            return Result.success()
        } catch (e: ExecutionException) {
            setLog("downloadMigration","ExecutionException")
            return Result.retry()
        } catch (e: InterruptedException) {
            // An interrupt occurred while waiting for the task to complete.
            setLog("downloadMigration","InterruptedException")
            return Result.retry()
        } catch (e: TimeoutException) {
            // Task timed out before it could complete.
            setLog("downloadMigration","TimeoutException")
            return Result.retry()
        } catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
            return Result.failure()
        }
    }

    private fun doDownloadMigration() {
        try {
            //setLog("downloadMigration","doUpdationTask() - lastUpdatedItemPosition-$lastUpdatedItemPosition")
            if (!downloadedItems.isNullOrEmpty() && downloadedItems.size > lastUpdatedItemPosition){
                try {
                    val item = downloadedItems.get(lastUpdatedItemPosition)
                    //setLog("downloadMigration " + item.id, "TrackId - " + item.trackId)
                    //setLog("downloadMigration " + item.id, "FilePath - " + item.filePath)
                    //setLog("downloadMigration " + item.id, "JsonDetails - " + item.jsonDetails)
                    //setLog("downloadMigration " + item.id, "State - " + item.state)
                    //setLog("downloadMigration " + item.id, "Progress - " + item.progress)
                    //setLog("downloadMigration " + item.id, "TimeStamp - " + item.timeStamp)

                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(item.trackId)
                    if (downloadedAudio != null && downloadedAudio.contentId.equals(item.trackId, true)){
                        //setLog("downloadMigration " + item.id, "TrackId == contentId " + item.trackId +"==" + downloadedAudio.contentId)
                    }else{
                        //setLog("downloadMigration " + item.id, "No trackId found = " + item.trackId)
                        //setLog("downloadMigration", "jsonDetails-1-=${item.jsonDetails}")
                        var jsonDetails = PlayableModelOldApp()
                        if (!TextUtils.isEmpty(item.jsonDetails.toString())){
                            jsonDetails = Gson().fromJson<PlayableModelOldApp>(
                                item.jsonDetails,
                                PlayableModelOldApp::class.java
                            ) as PlayableModelOldApp
                        }
                        //setLog("downloadMigration", "jsonDetails-2-=$jsonDetails")
                        if (jsonDetails?.response != null && !TextUtils.isEmpty(item.filePath)){
                            insertDataIntoDownloadedTable(jsonDetails?.response!!, item, jsonDetails.myPlaylistID)
                        }
                    }
                    lastUpdatedItemPosition++
                }catch (e:Exception){
                    errorCount++
                    setLog("downloadMigration","doUpdationTask() - errorCount-$errorCount")
                    if (errorCount < 3){
                        errorCount = 0
                        lastUpdatedItemPosition++
                    }
                }

                doDownloadMigration()
            }else{
                downloadAllArtwork()
            }
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

    private fun insertDataIntoDownloadedTable(
        response: PlayableModelOldApp.Response,
        item: MediaItemOldApp,
        myPlaylistId : String
    ) {
        try {
            val da = DownloadedAudio()
            if (response.podcast != null && response.podcast?.contentId != 0){
                setLog("downloadMigration", "podcastContentId="+response.podcast?.contentId.toString())
                setLog("downloadMigration", response.podcast?.albumId.toString())
                setLog("downloadMigration", response.podcast?.albumName.toString())
                setLog("downloadMigration", response.podcast?.title.toString())
                da.contentId = response.podcast?.contentId.toString()
                da.parentId = response.podcast?.albumId.toString()
                da.pid = listOf(response.podcast?.albumId.toString()).toString()
                da.pName = response.podcast?.albumName
                da.pType = DetailPages.PODCAST_DETAIL_PAGE.value
                da.contentType = ContentTypes.PODCAST.value
                da.originalAlbumName = response.podcast?.albumName
                da.podcastAlbumName = response.podcast?.albumName
                da.title = response.podcast?.title.toString()
                da.subTitle = response.podcast?.singers
                da.image = response.podcast?.image
                da.thumbnailPath = response.podcast?.image
                da.releaseYear = response.podcast?.releaseYear.toString()
                if (!TextUtils.isEmpty(response.podcast?.releasedate.toString())){
                    val releaseDate = DateUtils.convertLongTodateTime(response.podcast?.releasedate)
                    da.releaseDate = releaseDate
                }
                da.language = listOf(response.podcast?.language).toString()
                da.label = response.podcast?.label
                da.vendor = response.podcast?.vendor
                da.artist = listOf(response.podcast?.singers).toString()
                da.s_artist = listOf(response.podcast?.artistName).toString()
                da.singer = listOf(response.podcast?.singers).toString()
                da.type = response.podcast?.typeid
                da.f_fav_count = response.podcast?.favCount!!
                da.f_playcount = response.podcast?.playsCount!!
                da.description = response.podcast?.description
                da.category = response.podcast?.category
                da.duration = response.podcast?.duration
            }else{
                //setLog("downloadMigration", "albumContentId="+response.contentId.toString())
                //setLog("downloadMigration", response.albumId.toString())
                //setLog("downloadMigration", response.albumName.toString())
                //setLog("downloadMigration", response.title.toString())
                var albumId = ""
                setLog("downloadMigration", "trackId-${item.trackId}")
                val playListId = DBOHandler.getPlaylistIdFromTrackListTable(context, item.trackId)
                setLog("downloadMigration", "playListId-$playListId")
                var playListDetail = PlaylistDetailModelOldApp()
                var albumDetail = PlaylistDetailModelOldApp()
                if (!playListId.isNullOrEmpty() && !playListId.equals("0", true)){
                    val data = DBOHandler.getDataFromPlayListCacheTable(context, playListId)
                    if (data != null)
                    playListDetail = data
                }else{
                    albumId = DBOHandler.getAlbumIdFromTrackListTable(context, item.trackId)
                    setLog("downloadMigration", "albumId-$albumId")
                    if (!TextUtils.isEmpty(albumId) && !albumId.equals("0", true)){
                        albumDetail = DBOHandler.getDataFromAlbumCacheTable(context, albumId)
                    }
                }
                setLog("downloadMigration", "playListDetail.contentId-${playListDetail.contentId}")
                setLog("downloadMigration", "albumDetail.contentId-${albumDetail.contentId}")
                if (playListDetail.contentId != 0){
                    da.parentId = "playlist-"+playListDetail.contentId.toString()
                    da.pid = listOf("playlist-"+playListDetail.contentId.toString()).toString()
                    da.pName = playListDetail.title
                    da.pType = DetailPages.PLAYLIST_DETAIL_PAGE.value
                    da.parentThumbnailPath = playListDetail.playlistArtwork
                    da.pImage = playListDetail.playlistArtwork
                    da.downloadAll = 1
                }
                else if (albumDetail.contentId != 0){
                    da.parentId = albumDetail.contentId.toString()
                    da.pid = listOf(albumDetail.contentId.toString()).toString()
                    da.pName = albumDetail.title
                    da.pType = DetailPages.ALBUM_DETAIL_PAGE.value
                    da.downloadAll = 1

                    var image = ""
                    if (!albumDetail.images.image500x500.isNullOrEmpty()){
                        image = albumDetail.images.image500x500.get(0)
                    }else if (!albumDetail.images.image400x400.isNullOrEmpty()){
                        image = albumDetail.images.image400x400.get(0)
                    }else if (!albumDetail.images.image300x300.isNullOrEmpty()){
                        image = albumDetail.images.image300x300.get(0)
                    }else if (!albumDetail.images.image150x150.isNullOrEmpty()){
                        image = albumDetail.images.image150x150.get(0)
                    }
                    da.parentThumbnailPath = image
                    da.pImage = image
                }
                else{
                    da.pid = listOf(response.albumId.toString()).toString()
                    da.pName = response.albumName
                    setLog("downloadMigration", "myPlaylistId-$myPlaylistId")
                    /*if (!TextUtils.isEmpty(myPlaylistId)){
                        da.parentId = myPlaylistId
                        da.pType = DetailPages.MY_PLAYLIST_DETAIL_PAGE.value
                        da.pName = "Dummy static name"
                    }else{
                        da.parentId = response.albumId.toString()
                        da.pType = DetailPages.ALBUM_DETAIL_PAGE.value
                    }*/
                    da.parentId = response.albumId.toString()
                    da.pType = DetailPages.ALBUM_DETAIL_PAGE.value
                }
                da.contentId = response.contentId.toString()
                da.contentType = ContentTypes.AUDIO.value
                da.originalAlbumName = response.albumName
                da.title = response.title
                da.subTitle = response.singers
                var image = ""
                if (!response.images.image500x500.isNullOrEmpty()){
                    image = response.images.image500x500.get(0)
                }else if (!response.images.image400x400.isNullOrEmpty()){
                    image = response.images.image400x400.get(0)
                }else if (!response.images.image300x300.isNullOrEmpty()){
                    image = response.images.image300x300.get(0)
                }else if (!response.images.image150x150.isNullOrEmpty()){
                    image = response.images.image150x150.get(0)
                }
                da.image = image
                da.thumbnailPath = image
                da.tempo = listOf(response.attributeTempo).toString()
                da.releaseYear = response.relyear.toString()
                if (!TextUtils.isEmpty(response.releasedate.toString())){
                    val releaseDate = DateUtils.convertLongTodateTime(response.releasedate)
                    da.releaseDate = releaseDate
                }
                da.genre = listOf(response.genre).toString()
                da.subGenre = listOf(response.subgenreName).toString()
                da.language = listOf(response.language).toString()
                da.label = response.label
                da.vendor = response.label
                da.artist = listOf(response.singers).toString()
                da.s_artist = listOf(response.artistName).toString()
                da.mood = response.mood
                da.musicDirectorComposer = listOf(response.musicDirector).toString()
                da.singer = listOf(response.singers).toString()
                da.lyricist = listOf(response.lyricist).toString()
                da.cast = response.cast
                da.type = response.typeid
                //da.restrictedDownload = response.hasDownload
                da.f_fav_count = response.favCount
                da.f_playcount = response.playsCount
                da.lyricsUrl = response.lrc
            }
            da.downloadStatus = Status.COMPLETED.value
            da.downloadedFilePath = item.filePath
            if (!TextUtils.isEmpty(item.timeStamp)){
                da.createdDT = item.timeStamp?.toLong()!!
            }else{
                da.createdDT = DateUtils.curreentTimeStamp()
            }
            setLog("downloadMigration", "DA==$da")
            if(da.title!!.isNotEmpty()) {
                AppDatabase.getInstance()?.downloadedAudio()?.insertOrReplace(da)
            }
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

    private fun doMusicLanguageMigration(){
        try {
            val isAppLangSet = SharedPrefHelper.getInstance().get(PrefConstant.setlanguage, false)
            val appLanguageCode = SharedPrefHelper.getInstance().get(PrefConstant.app_language_code, "")
            val appLanguageTitle = SharedPrefHelper.getInstance().get(PrefConstant.user_selected_language_text, "")
            setLog("musicLangMigration","isAppLangSet-$isAppLangSet")
            setLog("musicLangMigration","appLanguageCode-$appLanguageCode")
            setLog("musicLangMigration","appLanguageTitle-$appLanguageTitle")
            val isAppLanguageNotSelectedInNewApp = SharedPrefHelper.getInstance().get(PrefConstant.CHOOES_LANGUAGE,true)
            setLog("musicLangMigration","isAppLanguageNotSelectedInNewApp-$isAppLanguageNotSelectedInNewApp")
            if (isAppLangSet && !TextUtils.isEmpty(appLanguageCode) && isAppLanguageNotSelectedInNewApp){
                SharedPrefHelper.getInstance().setLanguage(appLanguageCode)
                SharedPrefHelper.getInstance().setLanguageTitle(appLanguageTitle)
                //SharedPrefHelper.getInstance().save(PrefConstant.CHOOES_LANGUAGE, false)
            }
            val selectedMusicLanguage = SharedPrefHelper.getInstance().get(PrefConstant.user_language_list, "")
            setLog("musicLangMigration","selectedMusicLanguage-$selectedMusicLanguage")
            val isMusicLanguageSelectedInNewApp = SharedPrefHelper.getInstance().getMusicLanguage()
            setLog("musicLangMigration","isMusicLanguageSelectedInNewApp-$isMusicLanguageSelectedInNewApp")
            if (!TextUtils.isEmpty(selectedMusicLanguage) && !isMusicLanguageSelectedInNewApp){
                val threeCharMusicLangList : HashMap<String, String> = HashMap()
                threeCharMusicLangList.put("hin", "hi")
                threeCharMusicLangList.put("mar", "mr")
                threeCharMusicLangList.put("eng", "en")
                threeCharMusicLangList.put("kan", "kn")
                threeCharMusicLangList.put("tel", "te")
                threeCharMusicLangList.put("pan", "pa")
                threeCharMusicLangList.put("tam", "ta")
                threeCharMusicLangList.put("guj", "gu")
                threeCharMusicLangList.put("ori", "or")
                threeCharMusicLangList.put("urd", "ur")
                threeCharMusicLangList.put("ben", "bn")
                threeCharMusicLangList.put("mal", "ml")
                threeCharMusicLangList.put("sin", "si")
                threeCharMusicLangList.put("ara", "ar")
                threeCharMusicLangList.put("pas", "af")
                threeCharMusicLangList.put("kas", "ka")
                threeCharMusicLangList.put("ohin", "old_hindi")
                threeCharMusicLangList.put("hin", "hi")
                val json = JSONObject(selectedMusicLanguage.trim())
                setLog("musicLangMigration","json-$json")
                val keys: Iterator<String> = json.keys()
                setLog("musicLangMigration","keys-$keys")
                val listOfLanguageNameSelected = mutableListOf<String>()
                val listOfLanguageTitleSelected = mutableListOf<String>()
                while (keys.hasNext()) {
                    val key = keys.next()
                    try {
                        setLog("musicLangMigration","key-$key")
                        if (!TextUtils.isEmpty(key)){
                            val value = json.getString(key)
                            setLog("musicLangMigration","value-$value")
                            if (!TextUtils.isEmpty(value)){
                                if (threeCharMusicLangList.containsKey(key)){
                                    listOfLanguageNameSelected.add(threeCharMusicLangList.get(key).toString())
                                    listOfLanguageTitleSelected.add(value)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        try {
                            if (!BuildConfig.DEBUG){
                                Firebase.crashlytics.recordException(e)
                            }else{
                                //throw e
                            }
                        } catch (e:Exception) {

                        }
                    }
                }
                setLog("musicLangMigration","listOfLanguageNameSelected-$listOfLanguageNameSelected")
                setLog("musicLangMigration","listOfLanguageTitleSelected-$listOfLanguageTitleSelected")
                if (!listOfLanguageNameSelected.isNullOrEmpty() && !listOfLanguageTitleSelected.isNullOrEmpty()){
                    SharedPrefHelper.getInstance().setMusicLanguageCodeList(
                        Utils.convertArrayToString(
                            listOfLanguageNameSelected
                        )
                    )
                    SharedPrefHelper.getInstance().setMusicLanguageTitleList(
                        Utils.convertArrayToString(
                            listOfLanguageTitleSelected
                        )
                    )
                    //SharedPrefHelper.getInstance().setMusicLanguage(true)
                }
            }
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

    private fun doDownloadMigrationForVideo() {
        try {
            setLog(
                "downloadMigration",
                "doDownloadMigrationForVideo() - lastUpdatedItemPositionForVideo-$lastUpdatedItemPositionForVideo"
            )
            if (!downloadedItemsForVideo.isNullOrEmpty() && downloadedItemsForVideo.size > lastUpdatedItemPositionForVideo) {
                try {
                    val item = downloadedItemsForVideo.get(lastUpdatedItemPositionForVideo)
                    setLog("downloadMigration " + item.id, "TrackId - " + item.trackId)
                    setLog("downloadMigration " + item.id, "FilePath - " + item.filePath)
                    setLog("downloadMigration " + item.id, "JsonDetails - " + item.jsonDetails)
                    setLog("downloadMigration " + item.id, "State - " + item.state)
                    //setLog("downloadMigration " + item.id, "Progress - " + item.progress)
                    //setLog("downloadMigration " + item.id, "TimeStamp - " + item.timeStamp)

                    val downloadedAudio =
                        AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(item.trackId)
                    if (downloadedAudio != null && downloadedAudio.contentId.equals(
                            item.trackId,
                            true
                        )
                    ) {
                        setLog(
                            "downloadMigration " + item.id,
                            "TrackId == contentId " + item.trackId + "==" + downloadedAudio.contentId
                        )
                    } else {
                        setLog("downloadMigration " + item.id, "No trackId found = " + item.trackId)
                        //setLog("downloadMigration", "jsonDetails-1-=${item.jsonDetails}")
                        var jsonDetails = PlayableModelOldApp()
                        if (!TextUtils.isEmpty(item.jsonDetails.toString())) {
                            jsonDetails = Gson().fromJson<PlayableModelOldApp>(
                                item.jsonDetails,
                                PlayableModelOldApp::class.java
                            ) as PlayableModelOldApp
                        }
                        //setLog("downloadMigration", "jsonDetails-2-=$jsonDetails")
                        if (jsonDetails?.response != null && !TextUtils.isEmpty(item.filePath)) {
                            insertDataIntoDownloadedTableForVideo(jsonDetails?.response!!, item, jsonDetails.is_adult)
                        }
                    }
                    lastUpdatedItemPositionForVideo++
                } catch (e: Exception) {
                    errorCountForVideo++
                    setLog("downloadMigration", "doDownloadMigrationForVideo() - errorCountForVideo-$errorCountForVideo")
                    if (errorCountForVideo < 3) {
                        errorCountForVideo = 0
                        lastUpdatedItemPositionForVideo++
                    }
                }

                doDownloadMigrationForVideo()
            }
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

    private fun insertDataIntoDownloadedTableForVideo(
        response: PlayableModelOldApp.Response,
        item: MediaItemOldApp,
        isAdult:Boolean
    ) {
        try {
            val da = DownloadedAudio()

            setLog("downloadMigration", "insertDataIntoDownloadedTableForVideo()-albumContentId=" + response.contentId.toString())
            setLog("downloadMigration", response.albumId.toString())
            setLog("downloadMigration", response.albumName.toString())
            setLog("downloadMigration", response.title.toString())
            da.contentId = response.contentId.toString()
            da.parentId = response.albumId.toString()
            da.pid = listOf(response.albumId.toString()).toString()
            da.pName = response.albumName
            da.pType = DetailPages.MUSIC_VIDEO_DETAIL_PAGE.value
            da.contentType = ContentTypes.VIDEO.value
            da.originalAlbumName = response.albumName
            da.title = response.title
            da.subTitle = response.singers
            var image = ""
            if (!response.images.image700x394.isNullOrEmpty()) {
                image = response.images.image700x394.get(0)
            } else if (!response.images.image500x500.isNullOrEmpty()) {
                image = response.images.image500x500.get(0)
            } else if (!response.images.image400x400.isNullOrEmpty()) {
                image = response.images.image400x400.get(0)
            } else if (!response.images.image300x300.isNullOrEmpty()) {
                image = response.images.image300x300.get(0)
            } else if (!response.images.image150x150.isNullOrEmpty()) {
                image = response.images.image150x150.get(0)
            }
            da.image = image
            da.thumbnailPath = image
            da.tempo = listOf(response.attributeTempo).toString()
            da.releaseYear = response.relyear.toString()
            if (!TextUtils.isEmpty(response.releasedate.toString())) {
                val releaseDate = DateUtils.convertLongTodateTime(response.releasedate)
                da.releaseDate = releaseDate
            }
            da.genre = listOf(response.genre).toString()
            da.subGenre = listOf(response.subgenreName).toString()
            da.language = listOf(response.language).toString()
            da.label = response.label
            da.vendor = response.label
            da.artist = listOf(response.singers).toString()
            da.s_artist = listOf(response.artistName).toString()
            da.mood = response.mood
            da.musicDirectorComposer = listOf(response.musicDirector).toString()
            da.singer = listOf(response.singers).toString()
            da.lyricist = listOf(response.lyricist).toString()
            da.cast = response.cast
            da.type = response.typeid
            //da.restrictedDownload = response.hasDownload
            da.f_fav_count = response.favCount
            da.f_playcount = response.playsCount
            da.lyricsUrl = response.lrc
            da.duration = response.duration
            da.attribute_censor_rating = listOf(response.attributeCensorRating).toString()
            da.keywords = listOf(response.attributeKeyword).toString()
            var explicit = 0
            if (isAdult){
                explicit = 1
            }
            da.explicit = explicit
            da.downloadStatus = Status.COMPLETED.value
            da.downloadedFilePath = item.filePath
            if (!TextUtils.isEmpty(item.timeStamp)) {
                da.createdDT = item.timeStamp?.toLong()!!
            } else {
                da.createdDT = DateUtils.curreentTimeStamp()
            }
            setLog("downloadMigration", "DA==$da")
            if(da.title!!.isNotEmpty()) {
                AppDatabase.getInstance()?.downloadedAudio()?.insertOrReplace(da)
            }
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

    private fun downloadAllArtwork(){
        try {
            setLog("downloadMigration", "downloadAllArtwork-lastUpdatedItemArtworkPosition-$lastUpdatedItemArtworkPosition")
            if (!downloadedItems.isNullOrEmpty() && downloadedItems.size > lastUpdatedItemArtworkPosition){
                try {
                    val item = downloadedItems.get(lastUpdatedItemArtworkPosition)
                    setLog("downloadMigration " + item.id, "downloadAllArtwork-TrackId - " + item.trackId)
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(item.trackId)
                    if (downloadedAudio != null && downloadedAudio.contentId.equals(item.trackId, true)
                        && !TextUtils.isEmpty(downloadedAudio?.thumbnailPath) && CommonUtils.isFilePath(downloadedAudio?.thumbnailPath.toString())
                    ){
                        setLog("downloadMigration " + item.id, "downloadAllArtwork-TrackId == contentId " + item.trackId +"==" + downloadedAudio.contentId)
                        setLog("downloadMigration", "downloadAllArtwork-downloadAllArtwork-called")
                        lastUpdatedItemArtworkPosition++
                        downloadAllArtwork()
                        return
                    }else{
                        var jsonDetails = PlayableModelOldApp()
                        if (!TextUtils.isEmpty(item.jsonDetails.toString())){
                            jsonDetails = Gson().fromJson<PlayableModelOldApp>(
                                item.jsonDetails,
                                PlayableModelOldApp::class.java
                            ) as PlayableModelOldApp
                        }
                        setLog("downloadMigration", "downloadAllArtwork-jsonDetails-$jsonDetails")
                        if (jsonDetails?.response != null && !TextUtils.isEmpty(item.filePath)){
                            var image = ""
                            var contentId = ""
                            var contentArtworkFileName = ""
                            if (jsonDetails?.response?.podcast != null
                                && jsonDetails?.response?.podcast?.contentId != 0
                                && !TextUtils.isEmpty(jsonDetails?.response?.podcast?.image)){
                                image = jsonDetails?.response?.podcast?.image.toString()
                                contentId = jsonDetails?.response?.podcast?.contentId.toString()
                                contentArtworkFileName =
                                    Data.getSaveAudioDir(context)
                                        .toString() + Data.getSaveAudioThumbnailFileName(contentId)
                            }else{
                                contentId = jsonDetails?.response?.contentId.toString()
                                if (!jsonDetails?.response?.images?.image500x500.isNullOrEmpty()){
                                    image = jsonDetails?.response?.images?.image500x500?.get(0).toString()
                                }else if (!jsonDetails?.response?.images?.image400x400.isNullOrEmpty()){
                                    image = jsonDetails?.response?.images?.image400x400?.get(0).toString()
                                }else if (!jsonDetails?.response?.images?.image300x300.isNullOrEmpty()){
                                    image = jsonDetails?.response?.images?.image300x300?.get(0).toString()
                                }else if (!jsonDetails?.response?.images?.image150x150.isNullOrEmpty()){
                                    image = jsonDetails?.response?.images?.image150x150?.get(0).toString()
                                }
                                contentArtworkFileName =
                                    Data.getSaveAudioDir(context)
                                        .toString() + Data.getSaveAudioThumbnailFileName(contentId)

                            }
                            if (context != null && !TextUtils.isEmpty(image) && !TextUtils.isEmpty(contentId) && !TextUtils.isEmpty(contentArtworkFileName)){
                                if (ConnectionUtil(context).isOnline(false)){
                                    setLog("downloadMigration", "downloadAllArtwork-image-$image-contentId-$contentId-contentArtworkFileName-$contentArtworkFileName")
                                    saveThumbnail(context, image, contentId, contentArtworkFileName, false)
                                }else{
                                    setLog("downloadMigration", "downloadAllArtwork-Offline")
                                }
                            }
                        }
                    }
                    lastUpdatedItemArtworkPosition++
                }catch (e:Exception){
                    errorCountForAudioArtwork++
                    setLog("downloadMigration","downloadAllArtwork- - errorCount-$errorCount")
                    if (errorCountForAudioArtwork < 3){
                        errorCountForAudioArtwork = 0
                        lastUpdatedItemArtworkPosition++
                    }
                    setLog("downloadMigration", "downloadAllArtwork-downloadAllArtwork-Exception-called")
                    downloadAllArtwork()
                }
            }
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

    fun saveThumbnail(context: Context, imageUri: String, id:String, destinationPath:String, isParentTumbnail:Boolean){
        try {
            setLog("downloadMigration", "saveThumbnail-image-$imageUri-contentId-$id-contentArtworkFileName-$destinationPath")
            if (!TextUtils.isEmpty(imageUri)){
                val urlImage = URL(imageUri)
                // async task to get / download bitmap from url
                val result: Deferred<Bitmap?> = GlobalScope.async(Dispatchers.IO) {
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // get the downloaded bitmap
                        val bitmap : Bitmap? = result.await()

                        // if downloaded then saved it to internal storage
                        bitmap?.apply {
                            // get saved bitmap internal storage uri
                            val savedUri : Uri? = saveToInternalStorage(context, id, destinationPath)
                            setLog("downloadMigration", "saveThumbnail-contentId-$id-contentArtworkFileName-$destinationPath")
                            if (isParentTumbnail){
                                AppDatabase.getInstance()?.downloadedAudio()?.updateDownloadedImageParentThumbnailPath(savedUri.toString(), id)
                            }else{
                                AppDatabase.getInstance()?.downloadedAudio()?.updateDownloadedImageThumbnailPath(savedUri.toString(), id)
                            }

                        }
                    }catch (e:Exception){

                    } finally {
                        setLog("downloadMigration", "saveThumbnail-finally-downloadAllArtwork-called")
                        downloadAllArtwork()
                    }
                }
            }
        }catch (e:Exception){
            setLog("downloadMigration", "saveThumbnail-exception-downloadAllArtwork-called")
            downloadAllArtwork()
        }
    }
}