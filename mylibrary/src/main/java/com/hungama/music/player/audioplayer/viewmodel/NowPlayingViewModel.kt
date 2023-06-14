package com.hungama.music.player.audioplayer.viewmodel

import android.util.Log
import com.hungama.music.player.audioplayer.NowPlayingContract
import com.hungama.music.player.audioplayer.repositories.ITrackRepository
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class NowPlayingViewModel(private val view: NowPlayingContract.View,
                          private val trackRepository: ITrackRepository
) : NowPlayingContract.Presenter, CoroutineScope {
    private val TAG = NowPlayingViewModel::class.java.name
    private val job = Job()
    override val coroutineContext: CoroutineContext = (Dispatchers.IO + job)


    override fun fetchTrackMetadata(trackId: Long, index: Int) {

        setLog(TAG, "fetchTrackMetadata $trackId")

        // fetch metadata of currently playing track
        launch {
            try {
                //val track = trackRepository.getTrackById(view.getContext(), trackId)
                val track = trackRepository.getTrackByIdAndUniquePosition(view.getContext(), trackId, index.toLong())
//                withContext(Dispatchers.Main) {
                    if (track != null) {
                        //setLog(TAG, "setSongLyricsData fetchTrackMetadata track: ${track?.songLyricsUrl}")
                        view.showNowPlayingTrackMetadata(track!!)
                    }
//                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
                setLog(TAG, "Exception: ${e.message}")
                //view.displayError(view.getContext().getString(R.string.error_sdcard))
            }
        }
    }

    override fun fetchTrackMetadata(trackId: Long) {

        setLog(TAG, "fetchTrackMetadata $trackId")

        // fetch metadata of currently playing track
        launch {
            try {
                //val track = trackRepository.getTrackById(view.getContext(), trackId)
                CommonUtils.setLog(
                    "SwipablePlayerFragment",
                    "NowPlayingViewModel-fetchTrackMetadata()-trackId-${trackId}"
                )
                val track = trackRepository.getTrackById(view.getContext(), trackId)
                CommonUtils.setLog(
                    "SwipablePlayerFragment",
                    "NowPlayingViewModel-fetchTrackMetadata()-track-${track}"
                )
                if (track != null){
//                    withContext(Dispatchers.Main) {
                        //setLog(TAG, "setSongLyricsData fetchTrackMetadata track: ${track?.songLyricsUrl} ${track?.title}")
                        CommonUtils.setLog(
                            "SwipablePlayerFragment",
                            "NowPlayingViewModel-fetchTrackMetadata()-track.id-${track.id}"
                        )
                        view.showNowPlayingTrackMetadata(track)
                    }
//                }

            } catch (e: RuntimeException) {
                e.printStackTrace()
                setLog(TAG, "Exception: ${e.message}")
                //view.displayError(view.getContext().getString(R.string.error_sdcard))
            }
        }

        BaseActivity.nowPlayingCurrentTrack()
    }

    override fun changeShuffleMode(shuffleMode: Boolean) {
        view.updateShuffleMode(shuffleMode)
    }

    override fun onCleanup() {
        job.cancel()
    }
}
