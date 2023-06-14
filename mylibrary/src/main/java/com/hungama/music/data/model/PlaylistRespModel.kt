package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PlaylistRespModel(
    @SerializedName("count")
    var count: Int = 0,
    @SerializedName("data")
    var `data`: ArrayList<Data> = ArrayList()
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("data")
        var `data`: Data = Data(),
        @SerializedName("itype")
        var itype: Int = 0,
        @SerializedName("modified")
        var modified: String = "",
        @SerializedName("public")
        var `public`: Boolean = false,
        var isOwnerPlaylist:Boolean=true
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Data(
            @SerializedName("duration")
            var duration: Int = 0,
            @SerializedName("id")
            var id: String = "",
            @SerializedName("image")
            var image: String = "",
            @SerializedName("songCount")
            var songCount: String = "",
            @SerializedName("misc")
            var misc: Misc = Misc(),
            @SerializedName("subtitle")
            var subtitle: String = "",
            @Ignore
            var cp_subtitle: String = "",

            @SerializedName("genre")
            var genre: List<String> = listOf(),
            @SerializedName("type")
            var type: Int = 0,
            @SerializedName("releasedate")
            var releasedate: String = "",
            @SerializedName("title")
            var title: String = ""
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Misc(
                @SerializedName("artist")
                var artist: List<String> = listOf(),
                @SerializedName("attribute_censor_rating")
                var attributeCensorRating: List<String> = listOf(),
                @SerializedName("actorf")
                var actorf: List<String> = listOf(),
                @SerializedName("singerf")
                var singerf: List<String> = listOf(),
                @SerializedName("lang")
                var lang: List<String> = listOf(),
                @SerializedName("musicdirectorf")
                var musicdirectorf: List<String> = listOf(),
                @SerializedName("lyricist")
                var lyricist: List<String> = listOf(),
                @SerializedName("mood")
                var mood: String = "",
                @SerializedName("song_count")
                var song_count: String = "",
                @SerializedName("tempo")
                var tempo: List<String> = listOf(),
                @SerializedName("description")
                var description: String = "",
                @SerializedName("fav_count")
                var favCount: String = "0",
                @SerializedName("movierights")
                var movierights: List<String> = listOf(),
                @SerializedName("playcount")
                var playcount: String = "0",
                @SerializedName("rating_critic")
                var ratingCritic: Int = 0,
                @SerializedName("s_artist")
                var sArtist: List<String> = listOf(),
                @SerializedName("synopsis")
                var synopsis: String = ""
            ) : Parcelable
        }
    }
}