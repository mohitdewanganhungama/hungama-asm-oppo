package com.hungama.music.data.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize
@Entity
@Parcelize
data class HomeStory(var url: String? = "",
                     var storyDate: Long ? = 0L,
                     var commentsList : MutableList<CommentsModel>? = mutableListOf(),
                     var ctaText : String? = "",
                     var ctaUrl : String? = "") : Parcelable {

    fun isVideo() =  url?.contains(".mp4")
}