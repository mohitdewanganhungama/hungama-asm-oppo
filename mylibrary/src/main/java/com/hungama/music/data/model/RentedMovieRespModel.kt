package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class RentedMovieRespModel(
    @SerializedName("data")
    var `data`: Data = Data(),
    @SerializedName("success")
    var success: Boolean = false
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("movie")
        var movie: ArrayList<Movie> =ArrayList()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Movie(
            @SerializedName("data")
            var `data`: Data = Data(),
            @SerializedName("itype")
            var itype: Int = 0,
            @SerializedName("start_date")
            var startDate: String = "",
            @SerializedName("end_date")
            var endDate: String = "",
            @SerializedName("expired")
            var isExpired: Boolean = true
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
                var type: Int = 0
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
                    @SerializedName("explicit")
                    var explicit: Int = 0,
                    @SerializedName("fav_count")
                    var favCount: String = "0",
                    @SerializedName("lang")
                    var lang: List<String> = listOf(),
                    @SerializedName("movierights")
                    var movierights: List<String> = listOf(),
                    @SerializedName("nudity")
                    var nudity: String = "",
                    @SerializedName("playcount")
                    var playcount: Int = 0,
                    @SerializedName("rating_critic")
                    var ratingCritic: Double = 0.0,
                    @SerializedName("s_artist")
                    var sArtist: List<String> = listOf(),
                    @SerializedName("share")
                    var share: String = "",
                    @SerializedName("synopsis")
                    var synopsis: String = ""
                ) : Parcelable
            }
        }
    }
}