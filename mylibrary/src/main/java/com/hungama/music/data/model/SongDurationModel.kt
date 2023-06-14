package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.hungama.music.utils.DateUtils
import kotlinx.android.parcel.Parcelize
import java.util.*

@Keep
@Parcelize
@Entity(tableName = "songDuration")
data class SongDurationModel(
    @PrimaryKey(autoGenerate = true)
    var uniquePosition:Int = 0,
    var hungama_user_id: String = "", // or foodId: Int? = null
    var user_streamed_min: Int? = 0,
    var entry_created_time: Date?= DateUtils.getCurrentDateTime(),
    var first_stream_start_time: Date?= DateUtils.getCurrentDateTime(),
    var activity_last_updated: Date?= DateUtils.getCurrentDateTime(),
    var current_timestampm: String = "",
    var Is_first_stream_started: Int?= 0,
    var stream_max_min_allowed: Int?= 0):Parcelable
