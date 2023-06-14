package com.hungama.music.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.Track_State

@Dao
public interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(matchModels: List<Track>)

    @Query("SELECT * from track")
    fun getAllSong(): List<Track>

    @Query("Delete from track")
    fun deleteAll()

    @Query("Delete from track where state =(:state)")
    fun deletePlayedSong(state: Track_State)

    @Query("Delete from track where id =(:songid)")
    fun deleteSong(songid: Long)

    @Query("SELECT * FROM track WHERE id =(:id) LIMIT 1")
    fun findById(id: Int): Track

    @Query("SELECT * FROM track WHERE state =(:state) LIMIT 1")
    fun findByState(state: Track_State): Track

    @Query("Delete from track where uniquePosition =(:uniquePosition)")
    fun deleteSongByUniquePosition(uniquePosition: Long)
}