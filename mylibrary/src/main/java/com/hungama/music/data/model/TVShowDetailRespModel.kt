package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TVShowDetailRespModel(
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
                @SerializedName("heading")
                var heading: String? = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
                @SerializedName("seasons")
                var seasons: List<Season?>? = listOf()
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
                        var type: Int? = 0,
                        @SerializedName("variant")
                        var variant:String = "",
                        @SerializedName("variant-images")
                        var variant_images:List<String>? = null
                    ) : Parcelable
                }

                @Keep
                @Parcelize
                data class Season(
                    @SerializedName("data")
                    var `data`: SeasonData? = SeasonData(),
                    @SerializedName("itype")
                    var itype: Int? = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class SeasonData(
                        @SerializedName("id")
                        var id: String? = "",
                        @SerializedName("misc")
                        var misc: Misc? = Misc(),
                        @SerializedName("order")
                        var order: Int? = 0,
                        @SerializedName("title")
                        var title: String? = "",
                        @SerializedName("type")
                        var type: Int? = 0
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Misc(
                            @SerializedName("tracks")
                            var tracks: List<Track?>? = listOf()
                        ) : Parcelable {
                            @Keep
                            @Parcelize
                            data class Track(
                                @SerializedName("data")
                                var `data`: TrackData? = TrackData(),
                                @SerializedName("itype")
                                var itype: Int? = 0
                            ) : Parcelable {
                                @Keep
                                @Parcelize
                                data class TrackData(
                                    @SerializedName("id")
                                    var id: String? = "",
                                    @SerializedName("image")
                                    var image: String? = "",
                                    @SerializedName("misc")
                                    var misc: Misc? = Misc(),
                                    @SerializedName("name")
                                    var name: String? = "",
                                    @SerializedName("subTitle")
                                    var subTitle: String? = "",
                                    @SerializedName("releasedate")
                                    var releasedate: String? = "",
                                    @SerializedName("order")
                                    var order: Int? = 0,
                                    @SerializedName("season")
                                    var season: String? = "",
                                    @SerializedName("episode")
                                    var episode: String? = "",
                                    @SerializedName("type")
                                    var type: Int? = 0
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
                                        @SerializedName("restricted_download")
                                        var restricted_download: Int = 1,
                                        @SerializedName("movierights")
                                        var movierights: List<String?>? = listOf(),
                                        @SerializedName("attribute_censor_rating")
                                        var attributeCensorRating: List<String> = listOf()
                                    ) : Parcelable
                                }
                            }
                        }
                    }
                }
            }
        }

        @Keep
        @Parcelize
        data class Head(
            @SerializedName("description")
            var description: String? = "",
            @SerializedName("genre")
            var genre: List<String> = listOf(),
            @SerializedName("id")
            var id: String? = "",
            @SerializedName("duration")
            var duration: String? = "",
            @SerializedName("rating_critic")
            var ratingCritic: String? = "",
            @SerializedName("image")
            var image: String? = "",
            @SerializedName("share")
            var share: String? = "",
            @SerializedName("itype")
            var itype: Int? = 0,
            @SerializedName("lang")
            var lang: List<String> = listOf(),
            @SerializedName("movierights")
            var movierights: List<String?>? = listOf(),
            @SerializedName("nudity")
            var nudity: String? = "",
            @SerializedName("releasedate")
            var releasedate: String? = "",
            @SerializedName("title")
            var title: String? = "",
            @SerializedName("subTitle")
            var subTitle: String? = "",
            @SerializedName("type")
            var type: Int? = 0,
            @SerializedName("attribute_censor_rating")
            var attributeCensorRating: List<String> = listOf()
        ) : Parcelable
    }
}