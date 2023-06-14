package com.hungama.music.player.audioplayer.queue

import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track

interface QueueManager {

    fun getNowPlayingTracks(): MutableList<Track>
    fun setNowPlayingTracks(tracksList: MutableList<Track>)

    fun keepUnShuffledTracks(tracksList: MutableList<Track>)

    fun setupQueue(tracksList: MutableList<Track>, enableShuffle: Boolean, isQueueReordered:Boolean)

    fun addTrackToQueue(track: Track)

    fun addTrackToQueue(tracksList: MutableList<Track>)

    fun removeTrackFromQueue(track: Track)

    fun removeTrackFromQueue(index: Int, track: Track)

    fun addAlbumToQueue(albumTracks: MutableList<Track>, tracksViewModel: TracksContract.Presenter)

    fun playNext(track: Track)

    fun updateNextTrack(track: Track)

    fun updatePreviousTrack(track: Track)

    fun updateTrack(track: Track)

    fun clearNowPlayingQueue()

    fun updateUpComingNextPlayingQueue()

}