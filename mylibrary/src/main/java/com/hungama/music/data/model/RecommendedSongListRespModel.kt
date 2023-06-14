package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class RecommendedSongListRespModel(
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
            @SerializedName("similar")
            var rows: ArrayList<Similar> = ArrayList(),
            @SerializedName("rows")
            var similar: ArrayList<Similar> = ArrayList()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Similar(
                @SerializedName("data")
                var `data`: Data = Data(),
                @SerializedName("itype")
                var itype: Int = 0
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Data(
                    @SerializedName("duration")
                    var duration: String = "",
                    @SerializedName("genre")
                    var genre: List<String> = listOf(),
                    @SerializedName("id")
                    var id: String = "",
                    @SerializedName("image")
                    var image: String = "",
                    @SerializedName("playble_image")
                    var playble_image: String = "",
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
                        @SerializedName("actorf")
                        var actorf: List<String> = listOf(),
                        @SerializedName("singerf")
                        var singerf: List<String> = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String> = listOf(),
                        @SerializedName("cast")
                        var cast: String = "",
                        @SerializedName("count_era_from")
                        var countEraFrom: String = "",
                        @SerializedName("count_era_to")
                        var countEraTo: String = "",
                        @SerializedName("description")
                        var description: String = "",
                        @SerializedName("dl")
                        var dl: String = "",
                        @SerializedName("explicit")
                        var explicit: Int = 0,
                        @SerializedName("fav_count")
                        var favCount: String = "0",
                        @SerializedName("lang")
                        var lang: List<String> = listOf(),
                        @SerializedName("musicdirectorf")
                        var musicdirectorf: List<String> = listOf(),
                        @SerializedName("lyricist")
                        var lyricist: List<String> = listOf(),
                        @SerializedName("mood")
                        var mood: String = "",
                        @SerializedName("movierights")
                        var movierights: List<String> = listOf(),
                        @SerializedName("nudity")
                        var nudity: String = "",
                        @SerializedName("pid")
                        var pid: List<Int> = listOf(),
                        @SerializedName("p_name")
                        var pName: List<String> = listOf(),
                        @SerializedName("tempo")
                        var tempo: List<String> = listOf(),
                        @SerializedName("playcount")
                        var playcount: String = "0",
                        @SerializedName("rating_critic")
                        var ratingCritic: Int = 0,
                        @SerializedName("s_artist")
                        var sArtist: List<String> = listOf(),
                        @SerializedName("skipIntro")
                        var skipIntro: SkipIntro = SkipIntro(),
                        @SerializedName("sl")
                        var sl: Sl = Sl(),
                        @SerializedName("synopsis")
                        var synopsis: String = "",
                        @SerializedName("url")
                        var url: String = "",
                        @SerializedName("vendor")
                        var vendor: String = "",
                        @SerializedName("restricted_download")
                        var restricted_download: Int = 1
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class SkipIntro(
                            @SerializedName("skipCreditET")
                            var skipCreditET: Int = 0,
                            @SerializedName("skipCreditST")
                            var skipCreditST: Int = 0,
                            @SerializedName("skipIntroET")
                            var skipIntroET: Int = 0,
                            @SerializedName("skipIntroST")
                            var skipIntroST: Int = 0
                        ) : Parcelable

                        @Keep
                        @Parcelize
                        class Sl(
                        ) : Parcelable
                    }
                }
            }
        }

        @Keep
        @Parcelize
        class Head(
        ) : Parcelable
    }
}