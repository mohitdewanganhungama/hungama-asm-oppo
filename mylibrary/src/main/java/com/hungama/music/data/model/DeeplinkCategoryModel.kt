package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DeeplinkCategoryModel(
    @SerializedName("deeplink_category_list")
    var deeplinkCategoryList: DeeplinkCategoryList = DeeplinkCategoryList()
) : Parcelable {
    @Keep
    @Parcelize
    data class DeeplinkCategoryList(
        @SerializedName("movies")
        var movies: List<String?>? = listOf(),
        @SerializedName("music")
        var music: List<String?>? = listOf(),
        @SerializedName("music-videos")
        var musicVideos: List<String?>? = listOf(),
        @SerializedName("podcasts")
        var podcasts: List<String?>? = listOf(),
        @SerializedName("videos")
        var videos: List<String?>? = listOf()
    ) : Parcelable
}