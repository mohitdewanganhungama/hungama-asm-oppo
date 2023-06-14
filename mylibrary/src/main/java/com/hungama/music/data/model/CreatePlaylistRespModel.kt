package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CreatePlaylistRespModel(
    @SerializedName("data")
    var `data`: Data = Data(),
    @SerializedName("itype")
    var itype: Int = 0,
    @SerializedName("modified")
    var modified: String = "",
    @SerializedName("public")
    var `public`: Boolean = false,
    @SerializedName("message")
    var message: String = ""
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
        @SerializedName("misc")
        var misc: Misc = Misc(),
        @SerializedName("subtitle")
        var subtitle: String = "",
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