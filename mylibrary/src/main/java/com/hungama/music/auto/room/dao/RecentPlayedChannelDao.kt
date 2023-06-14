package com.hungama.music.auto.room.dao

import androidx.room.*
import com.hungama.music.auto.channel.Channel

@Dao
interface RecentPlayedChannelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentPlayed(recentPlayedChannel: Channel)

    @Query("SELECT * FROM channel ORDER BY id DESC")
    fun loadRecentPlayed(): List<Channel>

    @Delete
    fun deleteRecentPlayed(recentPlayedChannel: Channel)

    @Query("SELECT * FROM channel WHERE mediaId LIKE :mediaId ")
    fun findChannel(mediaId: String): Channel?
}
