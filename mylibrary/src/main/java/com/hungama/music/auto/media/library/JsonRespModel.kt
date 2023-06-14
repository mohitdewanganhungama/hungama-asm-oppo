package com.hungama.music.auto.media.library


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class JsonRespModel(
    @SerializedName("music")
    var music: List<Music> = ArrayList()
) : Parcelable {
    @Keep
    @Parcelize
    data class Music(
        @SerializedName("album")
        var album: String = "",
        @SerializedName("artist")
        var artist: String = "",
        @SerializedName("duration")
        var duration: Int = 0,
        @SerializedName("genre")
        var genre: String = "",
        @SerializedName("id")
        var id: String = "",
        @SerializedName("image")
        var image: String = "",
        @SerializedName("site")
        var site: String = "",
        @SerializedName("source")
        var source: String = "",
        @SerializedName("title")
        var title: String = "",
        @SerializedName("totalTrackCount")
        var totalTrackCount: Int = 0,
        @SerializedName("trackNumber")
        var trackNumber: Int = 0
    ) : Parcelable {
        override fun toString(): String {
            return "Music(album='$album', artist='$artist', duration=$duration, genre='$genre', id='$id', image='$image', site='$site', source='$source', title='$title', totalTrackCount=$totalTrackCount, trackNumber=$trackNumber)"
        }
    }

    override fun toString(): String {
        return "JsonRespModel(music=$music)"
    }
}