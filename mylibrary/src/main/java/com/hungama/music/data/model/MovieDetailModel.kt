package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MovieDetailModel(
    @SerializedName("data")
    var `data`: Data? = Data()
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("body")
        var body: Body? = Body(),
        @SerializedName("head")
        var head: Head? = Head()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Body(
            @SerializedName("rows")
            var rows: List<Row?>? = listOf()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Row(
                @SerializedName("headding")
                var headding: String? = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
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
                        @SerializedName("id")
                        var id: String? = "",
                        @SerializedName("image")
                        var image: String? = "",
                        @SerializedName("misc")
                        var misc: Misc? = Misc(),
                        @SerializedName("releasedate")
                        var releasedate: String? = "",
                        @SerializedName("subtitle")
                        var subtitle: String? = "",
                        @SerializedName("title")
                        var title: String? = "",
                        @SerializedName("type")
                        var type: Int? = 0,
                        @SerializedName("variant")
                        var variant:String = "",
                        @SerializedName("variant-images")
                        var variant_images:List<String>? = null
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Misc(
                            @SerializedName("artist")
                            var artist: List<String?>? = listOf(),
                            @SerializedName("attribute_censor_rating")
                            var attributeCensorRating: List<String?>? = listOf(),
                            @SerializedName("cast")
                            var cast: String? = "",
                            @SerializedName("description")
                            var description: String? = "",
                            @SerializedName("fav_count")
                            var favCount: Int? = 0,
                            @SerializedName("lang")
                            var lang: List<String?>? = listOf(),
                            @SerializedName("lyricist")
                            var lyricist: List<String?>? = listOf(),
                            @SerializedName("movierights")
                            var movierights: List<String?>? = listOf(),
                            @SerializedName("nudity")
                            var nudity: String? = "",
//                            @SerializedName("pid")
//                            var pid: Int? = 0,
                            @SerializedName("playcount")
                            var playcount: Int? = 0,
                            @SerializedName("rating_critic")
                            var ratingCritic: Double? = 0.0,
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
                            var vendor: String? = "",
                            @SerializedName("restricted_download")
                            var restricted_download: Int = 1
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
        }

        @Keep
        @Parcelize
        data class Head(
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
                @SerializedName("id")
                var id: String? = "",
                @SerializedName("image")
                var image: String? = "",
                @SerializedName("misc")
                var misc: Misc? = Misc(),
                @SerializedName("releasedate")
                var releasedate: String? = "",
                @SerializedName("genre")
                var genre: List<String> = listOf(),
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
                    var attributeCensorRating: List<String> = listOf(),
                    @SerializedName("cast")
                    var cast: String? = "",
                    @SerializedName("description")
                    var description: String? = "",
                    @SerializedName("fav_count")
                    var favCount: Int? = 0,
                    @SerializedName("lang")
                    var lang: List<String> = listOf(),
                    @SerializedName("lyricist")
                    var lyricist: List<String?>? = listOf(),
                    @SerializedName("movierights")
                    var movierights: List<String?>? = listOf(),
                    @SerializedName("nudity")
                    var nudity: String? = "",
//                    @SerializedName("pid")
//                    var pid: Int? = 0,
                    @SerializedName("playcount")
                    var playcount: Int? = 0,
                    @SerializedName("rating_critic")
                    var ratingCritic: Double? = 0.0,
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
                    @SerializedName("share")
                    var share: String? = "",
                    @SerializedName("vendor")
                    var vendor: String? = "",
                    @SerializedName("restricted_download")
                    var restricted_download: Int = 1
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
}