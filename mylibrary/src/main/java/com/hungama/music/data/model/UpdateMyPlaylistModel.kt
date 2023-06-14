package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UpdateMyPlaylistModel(
    @SerializedName("data")
    var `data`: Data = Data(),
    @SerializedName("itype")
    var itype: Int? = 0,
    @SerializedName("modified")
    var modified: String? = "",
    @SerializedName("public")
    var `public`: Boolean = false
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("duration")
        var duration: Int? = 0,
        @SerializedName("id")
        var id: String? = "",
        @SerializedName("image")
        var image: String? = "",
        @SerializedName("misc")
        var misc: Misc? = Misc(),
        @SerializedName("subtitle")
        var subtitle: String? = "",
        @SerializedName("title")
        var title: String? = "",
        @SerializedName("type")
        var type: Int? = 0
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Misc(
            @SerializedName("artist")
            var artist: List<String?>? = listOf(),
            @SerializedName("attribute_censor_rating")
            var attributeCensorRating: List<String?>? = listOf(),
            @SerializedName("description")
            var description: String? = "",
            @SerializedName("explicit")
            var explicit: Int? = 0,
            @SerializedName("fav_count")
            var favCount: String? = "",
            @SerializedName("movierights")
            var movierights: List<String?>? = listOf(),
            @SerializedName("playcount")
            var playcount: Int? = 0,
            @SerializedName("rating_critic")
            var ratingCritic: Int? = 0,
            @SerializedName("s_artist")
            var sArtist: List<String?>? = listOf(),
            @SerializedName("share")
            var share: String? = "",
            @SerializedName("song_count")
            var songCount: Int? = 0,
            @SerializedName("synopsis")
            var synopsis: String? = ""
        ) : Parcelable
    }
}