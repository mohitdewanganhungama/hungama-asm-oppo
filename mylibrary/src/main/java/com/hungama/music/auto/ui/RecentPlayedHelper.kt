package com.hungama.music.auto.ui

import android.content.Context
import com.hungama.music.auto.channel.Channel
import com.hungama.music.auto.lazyAndroid
import com.hungama.music.auto.room.AutoHungamaDatabase

class RecentPlayedHelper(context: Context) {

    private val database by lazyAndroid { AutoHungamaDatabase.getInstance(context) }

    fun getRecentlyPlayedChannels() = database.recentPlayedDao().loadRecentPlayed().toMutableList()

    fun isAddedRecentPlayed(channel: Channel): Boolean {
        return database.recentPlayedDao().findChannel(channel.mediaId) != null
    }

    fun addRecentPlayed(channel: Channel) {
        if(!isAddedRecentPlayed(channel)){
            database.recentPlayedDao().insertRecentPlayed(channel)
        }

    }

    fun deleteRecentPlayed(channel: Channel) {
        database.recentPlayedDao().deleteRecentPlayed(channel)
    }
}
