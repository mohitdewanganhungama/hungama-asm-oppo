package com.hungama.music.player.audioplayer.mediautils

import android.util.Log
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.utils.CommonUtils.setLog

class MediaSourceListUpdateCallback(
        private val concatenatingMediaSource: ConcatenatingMediaSource
) :
        ListUpdateCallback {

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        setLog("Change", "onMoved fromPos - $fromPosition  toPos - $toPosition")
//        concatenatingMediaSource.moveMediaSource(fromPosition, toPosition)
    }

    override fun onInserted(position: Int, count: Int) {
        setLog("Change", "onInserted pos - $position and count - $count")
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        setLog("Change", "onChanged pos - $position and count - $count")
    }
    override fun onRemoved(position: Int, count: Int) {
        setLog("Change", "onRemoved pos - $position and count - $count")
    }
}

class MediaSourceDiffUtilCallback(
        private val oldTracksList: MutableList<Track>,
        private val newTracksList: MutableList<Track>
) :
        DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTracksList[oldItemPosition].id == newTracksList[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldTracksList.size
    }

    override fun getNewListSize(): Int {
        return newTracksList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return true
    }

}