package com.hungama.music.player.audioplayer.queue

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.Track_State
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import java.lang.Exception
import kotlin.properties.Delegates

class NowPlayingInfo(var id: Long, var index: Int)
class NowPlayingQueue : QueueManager {

    var nowPlayingTracksList = mutableListOf<Track>()

    var trackListUnShuffled = mutableListOf<Track>()
    var shuffleEnabled: Boolean = false

    var onChangeShuffle: OnChangeShuffle? = null
    var onQueueItemChanged:OnQueueItemChanged? = null

    var currentPlayingTrackIndex: Int by Delegates.observable(0) { _, oldValue, newValue ->
        try {
            onCurrentPlayingTrackChanged?.invoke(oldValue, newValue)
        }catch (e:Exception){

        }
    }

    private var onCurrentPlayingTrackChanged: ((Int, Int) -> Unit)? = { oldIndex, newIndex ->
        try {
            nowPlayingTracksList.forEachIndexed { index, track ->
                if (index == newIndex)
                    track.state = Track_State.PLAYING
                else if (index == oldIndex)
                    track.state = Track_State.PLAYED
            }
        }catch (e:Exception){

        }

    }

    var currentPlayingTrackId: Long = 0

    override fun getNowPlayingTracks(): MutableList<Track> {
        return nowPlayingTracksList
    }

    override fun keepUnShuffledTracks(tracksList: MutableList<Track>) {
        //trackListUnShuffled.clear()
        trackListUnShuffled = ArrayList()
        trackListUnShuffled.addAll(tracksList)
    }

    override fun setupQueue(tracksList: MutableList<Track>, enableShuffle: Boolean, isQueueReordered:Boolean) {
        //setLog("queue12", tracksList.size.toString())
        //setLog("queue123", nowPlayingTracksList.size.toString())
        //nowPlayingTracksList.clear()
        setNowPlayingTracks(tracksList)
        shuffleEnabled = enableShuffle
        BaseActivity.setTrackListData(nowPlayingTracksList as ArrayList<Track>)
        //setLog("queue1234", tracksList.toString())
        //setLog("queue12345", nowPlayingTracksList.size.toString())
        //setLog("queue--1", "NowPlayingQueue-setupQueue-callOnQueueItemChanged - called- track.title-${tracksList.get(currentPlayingTrackIndex).title} - track.state-${tracksList.get(currentPlayingTrackIndex).state}")
        callOnQueueItemChanged(tracksList, isQueueReordered)
    }

    override fun addTrackToQueue(track: Track) {
        nowPlayingTracksList.add(track)
    }

    override fun addTrackToQueue(tracksList: MutableList<Track>) {
        nowPlayingTracksList.addAll(tracksList)
        BaseActivity.setTrackListData(nowPlayingTracksList as ArrayList<Track>)
    }

    override fun removeTrackFromQueue(track: Track) {
        nowPlayingTracksList.remove(track)
    }

    override fun removeTrackFromQueue(index: Int, track: Track) {
        nowPlayingTracksList.removeAt(index)
    }

    override fun addAlbumToQueue(albumTracks: MutableList<Track>, tracksViewModel: TracksContract.Presenter) {
        nowPlayingTracksList.addAll(albumTracks)
    }

    override fun playNext(track: Track) {
        if (currentPlayingTrackIndex == -1)
            nowPlayingTracksList.add(track)
        else
            nowPlayingTracksList.add(currentPlayingTrackIndex + 1, track)

        //setLog("PlayNextSong", "NowPlayingQueue-currentPlayingTrackIndex-$currentPlayingTrackIndex-size-${nowPlayingTracksList.size}-NextSong-${nowPlayingTracksList.get(currentPlayingTrackIndex + 1).title}")
        BaseActivity.setTrackListData(nowPlayingTracksList as ArrayList<Track>)
        //setLog("PlayNextSong", "NowPlayingQueue-NextSong-${nowPlayingTracksList.get(currentPlayingTrackIndex + 1).title}")

    }

    override fun updateNextTrack(track: Track) {
        if (currentPlayingTrackIndex == -1) {
            nowPlayingTracksList.add(track)
        }else {
            /*nowPlayingTracksList.remove(track)
            val tempPos=currentPlayingTrackIndex + 1
            if (nowPlayingTracksList.size >= tempPos){
                nowPlayingTracksList.add(tempPos, track)
            }else{
                nowPlayingTracksList.add(track)
            }*/
            updateTrack(track)

        }

    }

    override fun updatePreviousTrack(track: Track) {
        if (currentPlayingTrackIndex == -1) {
            nowPlayingTracksList.add(track)
        }else {
            /*val tempPos=currentPlayingTrackIndex - 1
            if(tempPos>0){
                nowPlayingTracksList.remove(track)
                nowPlayingTracksList.add(tempPos, track)
            }*/
            updateTrack(track)

        }

    }

    override fun updateTrack(track: Track) {
        CommonUtils.setLog("preCatchContent", "NowPlayingQueue-updateTrack-track.url-${track.url}")
        nowPlayingTracksList.forEachIndexed { index, it ->
            if (track.id == it.id){
                it.url = track.url
                it.drmlicence = track.drmlicence
                it.songLyricsUrl = track.songLyricsUrl
                CommonUtils.setLog("preCatchContent", "NowPlayingQueue-updateTrack-forEach-track.url-${track.url}")
            }
        }
    }

    override fun clearNowPlayingQueue() {
        nowPlayingTracksList.clear()
    }

    fun changeShuffle(onChangeShuffle: OnChangeShuffle){
        this.onChangeShuffle = onChangeShuffle
    }


    override fun updateUpComingNextPlayingQueue() {
        if (onChangeShuffle != null){
            onChangeShuffle?.onShuffleChanged()
        }
    }

    fun changeQueueItem(onQueueItemChanged: OnQueueItemChanged?){
        this.onQueueItemChanged = onQueueItemChanged
    }

    fun callOnQueueItemChanged(arrayList: MutableList<Track>, isQueueReordered:Boolean) {
        if (onQueueItemChanged != null){
            setLog("queue--2", arrayList.toString())
            //setLog("queue--2", "NowPlayingQueue-callOnQueueItemChanged-callOnQueueItemChanged - called- track.title-${arrayList.get(currentPlayingTrackIndex).title} - track.state-${arrayList.get(currentPlayingTrackIndex).state}")
            onQueueItemChanged?.onQueueItemChanged(arrayList, isQueueReordered)
        }
    }

    interface OnChangeShuffle {
        fun onShuffleChanged()
    }

    interface OnQueueItemChanged {
        fun onQueueItemChanged(arrayList: MutableList<Track>, isQueueReordered:Boolean)
    }

    override fun setNowPlayingTracks(tracksList: MutableList<Track>) {
        nowPlayingTracksList = ArrayList()
        nowPlayingTracksList.addAll(tracksList)
    }

}