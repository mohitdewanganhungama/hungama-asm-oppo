package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

class MoodRadioContentList : ArrayList<MoodRadioContentList.MoodRadioPlayableContentItem>(){
    @Keep
    @Parcelize
    data class MoodRadioPlayableContentItem(
        @SerializedName("data")
        var `data`: Data? = Data(),
        @SerializedName("itype")
        var itype: Int? = 0
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Data(
            @SerializedName("duration")
            var duration: Int? = 0,
            @SerializedName("genre")
            var genre: List<String?>? = listOf(),
            @SerializedName("id")
            var id: String? = "",
            @SerializedName("image")
            var image: String? = "",
            @SerializedName("playble_image")
            var playble_image: String? = "",
            @SerializedName("misc")
            var misc: Misc? = Misc(),
            @SerializedName("releasedate")
            var releasedate: String? = "",
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
                var attributeCensorRating: @RawValue List<Any?>? = listOf(),
                @SerializedName("cast")
                var cast: String? = "",
                @SerializedName("description")
                var description: String? = "",
                @SerializedName("fav_count")
                var favCount: Int? = 0,
                @SerializedName("lang")
                var lang: List<String?>? = listOf(),
                @SerializedName("lyricist")
                var lyricist: @RawValue List<Any?>? = listOf(),
                @SerializedName("movierights")
                var movierights: @RawValue List<Any?>? = listOf(),
                @SerializedName("nudity")
                var nudity: String? = "",
                @SerializedName("pid")
                var pid: List<Int?>? = listOf(),
                @SerializedName("playcount")
                var playcount: Int? = 0,
                @SerializedName("rating_critic")
                var ratingCritic: Int? = 0,
                @SerializedName("s_artist")
                var sArtist: List<String?>? = listOf(),
                @SerializedName("skipIntro")
                var skipIntro: SkipIntro? = SkipIntro(),
                @SerializedName("sl")
                var sl: Sl? = Sl(),
                @SerializedName("synopsis")
                var synopsis: String? = "",
                @SerializedName("url")
                var url: String? = "",
                @SerializedName("vendor")
                var vendor: String? = ""
            ) : Parcelable {
                @Keep
                @Parcelize
                data class SkipIntro(
                    @SerializedName("skipCreditET")
                    var skipCreditET: Int? = 0,
                    @SerializedName("skipCreditST")
                    var skipCreditST: Int? = 0,
                    @SerializedName("skipIntroET")
                    var skipIntroET: Int? = 0,
                    @SerializedName("skipIntroST")
                    var skipIntroST: Int? = 0
                ) : Parcelable

                @Keep
                @Parcelize
                class Sl(
                ) : Parcelable
            }
        }
    }
}