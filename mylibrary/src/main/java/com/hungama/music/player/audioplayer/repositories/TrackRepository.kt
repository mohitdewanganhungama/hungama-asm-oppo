package com.hungama.music.player.audioplayer.repositories

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.player.audioplayer.mediautils.getAlbumArtUri
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.getAllLocalDeviceTracksQuery
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.data.model.DetailPages
import com.hungama.music.player.audioplayer.model.TracklistDataModel
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import java.lang.Exception
import java.util.ArrayList

private const val TAG = "TrackRepository"
class TrackRepository : ITrackRepository {

    override suspend fun getTrackById(context: Context, trackId: Long): Track? {
        val trackList: MutableList<Track>
        //trackList = CommonUtils.getTrackList(context)
        trackList = BaseActivity.songDataList!!
        //setLog("SongData", BaseActivity.songDataList.toString())
        /*for(track in trackList){
            if (track.id == trackId ){
                return track
            }
        }*/
        setLog("SwipablePlayerFragment", "TrackRepository-getTrackById()-BaseActivity.nowPlayingCurrentIndex()-${BaseActivity.nowPlayingCurrentIndex()}")
        setLog("SwipablePlayerFragment", "TrackRepository-getTrackById()-trackId-${trackId}")
        trackList.forEachIndexed { index, track ->
            if (track.id == trackId && index == BaseActivity.nowPlayingCurrentIndex()){
                setLog("SwipablePlayerFragment", "TrackRepository-getTrackById()-track.title-${track.title}")
                return track
            }
        }
        return null
    }

    override suspend fun getAllTracks(context: Context, selectedTrackPosition: Int, queueManager: NowPlayingQueue): TracklistDataModel {
        setLog("SwipablePlayerFragment", "TrackRepository-getAllTracks-queueManager.getNowPlayingTracks-${queueManager.getNowPlayingTracks()?.size}")
        val finalTrackQueueList = mutableListOf<Track>()
        val queueTrackList = queueManager.getNowPlayingTracks()
        var selectedTrackIndex = selectedTrackPosition
        /*val iterator: MutableIterator<Track> = queueManager.getNowPlayingTracks().iterator() // it will return iterator
        var index = 0
        while (iterator.hasNext()) {
            val track = iterator.next()
            if (!CommonUtils.isSongOrPodcastContent(track) || CommonUtils.checkExplicitContent(context, track.explicit, false) || (track.id==0L && (track.contentType != ContentTypes.Audio_Ad.value || !CommonUtils.isAdsEnable() || (track.contentType == ContentTypes.Audio_Ad.value && selectedTrackIndex>index)))){
                setLog("TrackRepository", "getAllTracks-Removed content: " +track.id +"-" + track.title)
                if (selectedTrackIndex > index){
                    selectedTrackIndex -= 1
                }
            }else{
                finalTrackQueueList.add(track)
            }
            index++
        }*/

        try {
            queueTrackList.forEachIndexed { index, track ->
                if (!CommonUtils.isSongOrPodcastContent(track) || CommonUtils.checkExplicitContent(context, track.explicit, false) || (track.id==0L && (track.contentType != ContentTypes.Audio_Ad.value || !CommonUtils.isAdsEnable() || (track.contentType == ContentTypes.Audio_Ad.value && selectedTrackIndex>index)))){
                    setLog("TrackRepository", "getAllTracks-Removed content: " +track.id +"-" + track.title)
                    if (selectedTrackIndex > index){
                        selectedTrackIndex -= 1
                    }
                }else{
                    finalTrackQueueList.add(track)
                }
            }
        }catch (e:Exception){
            selectedTrackIndex = selectedTrackPosition
            finalTrackQueueList.addAll(queueTrackList)
        }


        if (selectedTrackIndex < 0){
            selectedTrackIndex = 0
        }
        queueManager.setNowPlayingTracks(finalTrackQueueList)
        setLog("SwipablePlayerFragment", "TrackRepository-getAllTracks-queueManager.getNowPlayingTracks()-finalTrackQueueList-${queueManager.getNowPlayingTracks()?.size}")

        /*val finalTrackList = mutableListOf<Track>()
        val trackList = BaseActivity.songDataList!!
        setLog("SwipablePlayerFragment", "TrackRepository-getAllTracks-BaseActivity.songDataList-${BaseActivity.songDataList?.size}")
        val trackIterator: MutableIterator<Track> = trackList.iterator()
        while (trackIterator.hasNext()) {
            val track = trackIterator.next()
            setLog("TrackRepository", "getAllTracks2-content: " +track.id +"-" + track.contentType)
            if (CommonUtils.checkExplicitContent(context, track.explicit, false) || (track.id==0L && (track.contentType != ContentTypes.Audio_Ad.value || !CommonUtils.isAdsEnable() || (track.contentType == ContentTypes.Audio_Ad.value && selectedTrackIndex>index)))){
                setLog("TrackRepository", "getAllTracks2-Removed content: " +track.id +"-" + track.title)
                if (selectedTrackIndex > index){
                    selectedTrackIndex -= 1
                }
            }else{
                finalTrackList.add(track)
            }
            index++
        }
        if (selectedTrackIndex < 0){
            selectedTrackIndex = 0
        }
        setLog("SwipablePlayerFragment", "TrackRepository-getAllTracks-finalTrackList-${finalTrackList?.size}")
        return TracklistDataModel(finalTrackList, selectedTrackIndex)*/
        return CommonUtils.filterAudioContent(context,
            BaseActivity.songDataList!!, selectedTrackIndex)
    }

    override suspend fun searchTracksByName(context: Context, query: String): MutableList<Track> {

        return mutableListOf()
    }

    override suspend fun getAllTracksVideo(context: Context): MutableList<Track> {
        val trackList: MutableList<Track>
        trackList = CommonUtils.getVideoDummyData()
        return trackList
    }

    override suspend fun getTrackByIdAndUniquePosition(
        context: Context,
        trackId: Long,
        uniquePosition: Long
    ): Track? {
        val trackList: MutableList<Track>
        //trackList = CommonUtils.getTrackList(context)
        trackList = BaseActivity.songDataList!!
        //setLog("SongData", BaseActivity.songDataList.toString())
        for(track in trackList){
            if (track.id == trackId && track.uniquePosition == uniquePosition+1){
                return track
            }
        }
        return null
    }

    override suspend fun getAllLocalDeviceTracks(context: Context): MutableList<Track> {
        setLog(TAG, "Fetching tracks from SD Card")

        val trackList: MutableList<Track> = mutableListOf()

        val cursor: Cursor? = getAllLocalDeviceTracksQuery(context)

        getTracksListFromCursor(context, cursor, trackList)

        return trackList
    }

    private fun getTracksListFromCursor(
        context: Context,
        cursor: Cursor?,
        trackList: MutableList<Track>
    ) {
        when {
            cursor == null -> {
                throw RuntimeException("Problem with Media Content Provider")
            }
            !cursor.moveToNext() -> {
                setLog(TAG, "No tracks on SD Card")
                cursor.close()
            }
            else -> {

                do {
                    val track = Track()
                    track.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    track.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    track.subTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    track.artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    track.url = getLocalDeviceSongPlaybackUri(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))).toString()
                    track.image = getLocalDeviceSongPlaybackArtworkUri(context, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))).toString()
                    track.pType = DetailPages.LOCAL_DEVICE_SONG_PAGE.value
                    track.playerType = Constant.MUSIC_PLAYER
                    trackList.add(track)

                } while (cursor.moveToNext())

                cursor.close()
            }
        }
    }

    fun getLocalDeviceSongPlaybackUri(id: Long): Uri {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    fun getLocalDeviceSongPlaybackArtworkUri(context: Context, id: Long): Uri {
        return getAlbumArtUri(context, id)
    }

    override suspend fun getAllQueuedTracks(
        context: Context,
        selectedTrackPosition: Int,
        queueManager: NowPlayingQueue
    ): TracklistDataModel {
        /*setLog("SwipablePlayerFragment", "TrackRepository-getAllQueuedTracks-queueManager.getNowPlayingTracks-${queueManager.getNowPlayingTracks().size}")
        val finalTrackList = mutableListOf<Track>()
        val trackList = queueManager.getNowPlayingTracks()
        val iterator: MutableIterator<Track> = trackList.iterator() // it will return iterator
        var selectedTrackIndex = selectedTrackPosition
        var index = 0
        var isSongOrPodcastContent = true
        if (!trackList.isNullOrEmpty() && trackList.size > selectedTrackIndex){
            isSongOrPodcastContent = CommonUtils.isSongOrPodcastContent(trackList?.get(selectedTrackIndex))
        }

        while (iterator.hasNext()) {
            val track = iterator.next()
            if ((isSongOrPodcastContent && selectedTrackIndex != index && !CommonUtils.isSongOrPodcastContent(track)) || CommonUtils.checkExplicitContent(context, track.explicit, false) || (track.id==0L && (track.contentType != ContentTypes.Audio_Ad.value || !CommonUtils.isAdsEnable() || (track.contentType == ContentTypes.Audio_Ad.value && selectedTrackIndex>index)))){
                setLog("TrackRepository", "getAllQueuedTracks-Removed content: " +track.id +"-" + track.title)
                if (selectedTrackIndex > index){
                    selectedTrackIndex -= 1
                }
            }else{
                finalTrackList.add(track)
            }
            index++
        }
        if (selectedTrackIndex < 0){
            selectedTrackIndex = 0
        }
        setLog("SwipablePlayerFragment", "TrackRepository-getAllQueuedTracks-queueManager.getNowPlayingTracks-finalTrackList-${queueManager.getNowPlayingTracks().size}")
        return TracklistDataModel(finalTrackList, selectedTrackIndex)*/
        return CommonUtils.filterAudioContent(context,
            queueManager.getNowPlayingTracks() as ArrayList<Track>, selectedTrackPosition)
    }

    companion object {

        private var INSTANCE: TrackRepository? = null

        @JvmStatic
        fun getInstance(): TrackRepository {
            return INSTANCE ?: TrackRepository().apply {
                INSTANCE = this
            }
        }
    }
}