package com.hungama.music.player.audioplayer.repositories

import android.content.Context
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.TracklistDataModel
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue

interface ITrackRepository {

    suspend fun getAllTracks(context: Context, selectedTrackPosition: Int,
                             queueManager: NowPlayingQueue): TracklistDataModel

    suspend fun getTrackById(context: Context, trackId: Long): Track?

    suspend fun searchTracksByName(context: Context, query: String): MutableList<Track>

    suspend fun getAllTracksVideo(context: Context): MutableList<Track>

    suspend fun getTrackByIdAndUniquePosition(context: Context, trackId: Long, uniquePosition:Long): Track?

    suspend fun getAllLocalDeviceTracks(context: Context): MutableList<Track>

    suspend fun getAllQueuedTracks(
        context: Context,
        selectedTrackPosition: Int,
        queueManager: NowPlayingQueue
    ): TracklistDataModel
}