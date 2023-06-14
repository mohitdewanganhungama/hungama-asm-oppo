package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BookmarkDataModel(
    @SerializedName("data")
    var `data`: Data = Data()
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("body")
        var body: Body = Body(),
        @SerializedName("head")
        var head: Head = Head()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Body(
            @SerializedName("rows")
            var rows: ArrayList<Row> = ArrayList()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Row(
                @SerializedName("data")
                var `data`: Data = Data(),
                @SerializedName("itype")
                var itype: Int? = 0
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Data(
                    @SerializedName("duration")
                    var duration: Long = 0,
                    @SerializedName("id")
                    var id: String? = "",
                    @SerializedName("image")
                    var image: String? = "",
                    @SerializedName("playble_image")
                    var playble_image: String? = "",
                    @SerializedName("songCount")
                    var songCount: String = "",
                    @SerializedName("misc")
                    var misc: Misc = Misc(),
                    @SerializedName("releasedate")
                    var releasedate: String? = "",
                    @SerializedName("subtitle")
                    var subtitle: String? = "",
                    @SerializedName("title")
                    var title: String? = "",
                    @SerializedName("type")
                    var type: String? = "0",
                    var isFavorite:Boolean = false,
                    var isSelected:Int = 0,
                    var isDeleted:Int = 0,
                    @Expose
                    var isCurrentPlaying: Boolean = false
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
                        @SerializedName("fav_count")
                        var favCount: String? = "0",
                        @SerializedName("lang")
                        var lang: List<String?>? = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<String?>? = listOf(),
                        @SerializedName("nudity")
                        var nudity: String? = "",
                        @SerializedName("song_count")
                        var song_count: String = "",
                        @SerializedName("playcount")
                        var playcount: Int? = 0,
                        @SerializedName("rating_critic")
                        var ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        var sArtist: List<String?>? = listOf(),
                        @SerializedName("synopsis")
                        var synopsis: String? = "",
                        @SerializedName("explicit")
                        var explicit: Int = 0,
                        @SerializedName("restricted_download")
                        var restricted_download: Int = 1,
                        @SerializedName("share")
                        var share: String = ""

                    ) : Parcelable
                }
            }
        }

        @Keep
        @Parcelize
        class Head(
        ) : Parcelable
    }
}