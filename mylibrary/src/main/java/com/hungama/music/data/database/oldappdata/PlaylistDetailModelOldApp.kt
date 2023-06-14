package com.hungama.music.data.database.oldappdata


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PlaylistDetailModelOldApp(
    @SerializedName("content_id")
    var contentId: Int = 0,
    @SerializedName("fav_count")
    var favCount: String = "0",
    @SerializedName("mediaType")
    var mediaType: String = "",
    @SerializedName("music_tracks_count")
    var musicTracksCount: Int = 0,
    @SerializedName("playlist_artwork")
    var playlistArtwork: String = "",
    @SerializedName("plays_count")
    var playsCount: String = "0",
    @SerializedName("release_year")
    var releaseYear: String = "",
    @SerializedName("title")
    var title: String = "",
    @SerializedName("images")
    var images: Images = Images()
) : Parcelable {
    @Keep
    @Parcelize
    data class Images(
        @SerializedName("image_150x150")
        var image150x150: List<String> = listOf(),
        @SerializedName("image_200x200")
        var image200x200: List<String> = listOf(),
        @SerializedName("image_300x300")
        var image300x300: List<String> = listOf(),
        @SerializedName("image_400x400")
        var image400x400: List<String> = listOf(),
        @SerializedName("image_500x500")
        var image500x500: List<String> = listOf()
    ) : Parcelable
}