package com.hungama.music.utils

import com.hungama.music.player.audioplayer.model.Track

interface IPreferenceHelper {
    fun setTrackList(trackList: String)
    fun getTrackList(): String?
    fun clearPrefs()
}