package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PodcastDetailsRespModel(
    @SerializedName("data")
    var `data`: MainData = MainData()
) : Parcelable {
    @Keep
    @Parcelize
    data class MainData(
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
                var `data`: RowData = RowData(),
                @SerializedName("similar")
                var similar:ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class RowData(
                    @SerializedName("misc")
                    var misc: Misc = Misc()
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Misc(
                        @SerializedName("tracks")
                        var tracks: ArrayList<Track> = ArrayList()
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Track(
                            @SerializedName("data")
                            var `data`: Data = Data(),
                            @SerializedName("itype")
                            var itype: Int = 0,
                            var adUnitId: String = ""
                        ) : Parcelable {
                            @Keep
                            @Parcelize
                            data class Data(
                                @SerializedName("id")
                                var id: String? = "",
                                @SerializedName("image")
                                var image: String? = "",
                                @SerializedName("misc")
                                var misc: Misc? = Misc(),
                                @SerializedName("title")
                                var title: String? = "",
                                @SerializedName("subtitle")
                                var subtitle: String? = "",
                                @SerializedName("duration")
                                var duration: Int? = 0,
                                @SerializedName("releasedate")
                                var releasedate: String? = "",
                                @SerializedName("name")
                                var name: String? = "",
                                @SerializedName("order")
                                var order: Int? = 0,
                                @SerializedName("type")
                                var type: Int? = 0,
                                var isFavorite:Boolean = false
                            ) : Parcelable {
                                @Keep
                                @Parcelize
                                data class Misc(
                                    @SerializedName("description")
                                    var description: String? = "",
                                    @SerializedName("share")
                                    var share: String? = "",
                                    @SerializedName("duration")
                                    var duration: Int? = 0,
                                    @SerializedName("synopsis")
                                    var synopsis: String? = "",
                                    @SerializedName("movierights")
                                    var movierights: List<String?>? = listOf(),
                                    @SerializedName("restricted_download")
                                    var restricted_download: Int = 1,
                                    @SerializedName("explicit")
                                    var explicit: Int = 0,
                                    @SerializedName("attribute_censor_rating")
                                    var attributeCensorRating: List<String?>? = listOf()
                                ) : Parcelable
                            }
                        }
                    }
                }

                @Keep
                @Parcelize
                data class Similar(
                    @SerializedName("data")
                    var `data`: Data? = Data(),
                    @SerializedName("itype")
                    var itype: Int? = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Data(
                        @SerializedName("id")
                        var id: String? = "",
                        @SerializedName("image")
                        var image: String? = "",
                        @SerializedName("releasedate")
                        var releasedate: String? = "",
                        @SerializedName("subtitle")
                        var subtitle: String? = "",
                        @SerializedName("title")
                        var title: String? = "",
                        @SerializedName("type")
                        var type: Int? = 0
                    ) : Parcelable
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
                @SerializedName("subtitle")
                var subtitle: String? = "",
                @SerializedName("title")
                var title: String? = "",
                var isFavorite:Boolean = false,
                @SerializedName("type")
                var type: Int? = 0,
            ) : Parcelable{
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
                    var favCount: String? = "",
                    @SerializedName("f_fav_count")
                    var f_FavCount: String = "",
                    @SerializedName("share")
                    var share: String? = "",
                    @SerializedName("items")
                    var items: Int? = 0,
                    @SerializedName("lang")
                    var lang: List<String?>? = listOf(),
                    @SerializedName("movierights")
                    var movierights: List<String?>? = listOf(),
                    @SerializedName("nudity")
                    var nudity: String? = "",
                    @SerializedName("playcount")
                    var playcount: String? = "",
                    @SerializedName("f_playcount")
                    var f_playcount: String = "",
                    @SerializedName("rating_critic")
                    var ratingCritic: Double? = 0.0,
                    @SerializedName("s_artist")
                    var sArtist: List<String?>? = listOf(),
                    @SerializedName("synopsis")
                    var synopsis: String? = "",
                    @SerializedName("episodeCount")
                    var episodeCount: Int? = 0
                ): Parcelable
            }
        }
    }
}