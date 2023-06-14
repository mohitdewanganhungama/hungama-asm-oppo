package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class LibraryAllRespModel(
    @SerializedName("rows")
    var rows: ArrayList<Row> = ArrayList()
) : Parcelable {
    @Keep
    @Parcelize
    data class Row(
        @SerializedName("data")
        var `data`: Data = Data(),
        @SerializedName("itype")
        var itype: Int = 0,
        @SerializedName("modified")
        var modified: String = "",
        @SerializedName("public")
        var `public`: Boolean = false
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
            @SerializedName("releasedate")
            var releasedate: String = "",
            @SerializedName("subtitle")
            var subtitle: String = "",
            @SerializedName("title")
            var title: String = "",
            @SerializedName("type")
            var type: Int = -1
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Misc(
                @SerializedName("artist")
                var artist: ArrayList<String> = ArrayList(),
                @SerializedName("attribute_censor_rating")
                var attributeCensorRating: ArrayList<String> = ArrayList(),
                @SerializedName("description")
                var description: String = "",
                @SerializedName("fav_count")
                var favCount: String = "0",
                @SerializedName("lang")
                var lang: ArrayList<String> = ArrayList(),
                @SerializedName("movierights")
                var movierights: ArrayList<String> = ArrayList(),
                @SerializedName("nudity")
                var nudity: String = "",
                @SerializedName("playcount")
                var playcount: String = "0",
                @SerializedName("rating_critic")
                var ratingCritic: Int = 0,
                @SerializedName("s_artist")
                var sArtist: ArrayList<String> = ArrayList(),
                @SerializedName("synopsis")
                var synopsis: String = ""
            ) : Parcelable
        }
    }
}