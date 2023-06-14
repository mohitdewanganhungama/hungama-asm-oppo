package com.hungama.music.player.audioplayer.viewmodel

import com.hungama.music.player.audioplayer.TracksContract

import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.repositories.ITrackRepository
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

private val TAG = TracksViewModel::class.java.name

class TracksViewModel(
        private val trackRepository: ITrackRepository,
        private val view: TracksContract.View
) : TracksContract.Presenter, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = (Dispatchers.IO + job)

    private var tracksList = mutableListOf<Track>()

    override fun prepareTrackPlayback(selectedTrackPosition: Int, trackPlayStartPosition:Long) {
        CoroutineScope(Dispatchers.Main).launch {
            setLog(TAG, "Track selected for playback $selectedTrackPosition")
            view.startTrackPlayback(selectedTrackPosition, tracksList, trackPlayStartPosition)
        }
    }

    override fun setNowPlayingTrack(trackId: Long) {
        //TODO:
    }

    override fun onCleanup() {
        job.cancel()
        tracksList.clear()
    }
}