package com.hungama.music.auto.channel

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "channel")
data class Channel(

        @ColumnInfo(name = "mediaId")
        val mediaId: String,

        @ColumnInfo(name = "title")
        val title: String,

        @ColumnInfo(name = "imageRes")
        val imageRes: String,

        @ColumnInfo(name = "mediaURL")
        var mediaURL: String,

        @ColumnInfo(name = "type")
        val type: String,

        @ColumnInfo(name = "parentID")
        val parentID: String,

        @ColumnInfo(name = "subtitle")
        val subtitle: String,

        @ColumnInfo(name = "description")
        val description: String,

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Int = 0

) : Parcelable {
        override fun toString(): String {
                return "Channel(mediaId='$mediaId', type='$type', parentID='$parentID', title='$title', imageRes='$imageRes', mediaURL='$mediaURL', id=$id)"
        }
}
