package com.hungama.music.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hungama.music.data.model.BodyRowsItemsItem

@Dao
public interface RecentlyPlayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(track: BodyRowsItemsItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(matchModels: List<BodyRowsItemsItem>)

    @Query("SELECT * FROM BodyRowsItemsItem ORDER BY added_date_Time desc ,sr_no asc LIMIT 50")
    fun getAllItem(): List<BodyRowsItemsItem>

    @Query("SELECT * FROM BodyRowsItemsItem WHERE type IN(:typeList) ORDER BY added_date_Time desc ,sr_no asc")
    fun getAllItemWithQuery(typeList: ArrayList<Int>): List<BodyRowsItemsItem>

    @Query("SELECT * FROM BodyRowsItemsItem WHERE type IN(:typeList) ORDER BY added_date_Time desc ,sr_no asc LIMIT 50")
    fun getAllItemWithDate(typeList: ArrayList<Int>): List<BodyRowsItemsItem>

    @Query("SELECT * FROM BodyRowsItemsItem WHERE itemId =(:itemId) LIMIT 1")
    fun findByID(itemId: Long): BodyRowsItemsItem

    @Query("SELECT * FROM BodyRowsItemsItem ORDER BY added_date_Time desc LIMIT 1")
    fun findLastItemAdded(): BodyRowsItemsItem


//    @Query("SELECT * FROM BodyRowsItemsItem WHERE type IN(:typeList)")
//    fun getAllItemWithQuery(typeList:String): List<BodyRowsItemsItem>


    @Query("Delete from BodyRowsItemsItem")
    fun deleteAll()

    @Query("Delete from BodyRowsItemsItem where itemId=:itemId")
    fun deleteByID(itemId: Int)

}