package com.hungama.music.data.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "commentsmodel")
@Parcelize
data class CommentsModel(
    var userComment : String? = "",
    var userProfileUrl : String? = "") : Parcelable
