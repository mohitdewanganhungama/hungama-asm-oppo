package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class RecentSearchRespModel(
    @SerializedName("data")
    var `data`: ArrayList<Data?>?
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("artist")
        var artist: String = "",
        @SerializedName("contentId")
        var contentId: String = "",
        @SerializedName("image")
        var image: String = "",
        @SerializedName("itype")
        var itype: Int = 0,
        @SerializedName("searchText")
        var searchText: String = "",
        @SerializedName("title")
        var title: String = "",
        @SerializedName("userId")
        var userId: Long = 0
    ) : Parcelable
}