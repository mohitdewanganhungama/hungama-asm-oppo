package com.hungama.music.auto.api.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class PlaylistModel(
    @SerializedName("data")
    var `data`: Data = Data()
) {
    @Keep
    data class Data(
        @SerializedName("body")
        var body: Body = Body(),
        @SerializedName("head")
        var head: Head = Head()
    ) {
        @Keep
        data class Body(
            @SerializedName("rows")
            var rows: ArrayList<Row> = ArrayList(),
            @SerializedName("similar")
            var similar: ArrayList<BodyRowsItemsItem> = arrayListOf(),
            @SerializedName("recomendation")
            var recomendation:ArrayList<BodyRowsItemsItem> = arrayListOf(),
        ) {
            @Keep
            @Parcelize
            data class Row(
                @SerializedName("data")
                var `data`: Data = Data(),
                @SerializedName("itype")
                var itype: Int = 0,
                var adUnitId: String = "",
                @SerializedName("seasons")
                var seasons: List<Season> = listOf()
            ): Parcelable {
                @Keep
                @Parcelize
                data class Data(
                    @SerializedName("duration")
                    var duration: String = "",
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
                    var type: Int = 0,
                    var isFavorite:Boolean = false,
                    @SerializedName("genre")
                    var genre: List<String> = listOf(),
                    @Expose
                    var isCurrentPlaying: Boolean = false
                ): Parcelable {
                    @Keep
                    @Parcelize
                    data class Misc(
                        @SerializedName("singerf")
                        var singerf: List<String> = listOf(),
                        @SerializedName("actorf")
                        var actorf: List<String> = listOf(),
                        @SerializedName("artist")
                        var artist: List<String> = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String> = listOf(),
                        @SerializedName("description")
                        var description: String = "",
                        @SerializedName("fav_count")
                        var favCount: String = "0",
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        var lang: List<String> = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<String> = listOf(),
                        @SerializedName("nudity")
                        var nudity: String = "",
                        @SerializedName("playcount")
                        var playcount: String = "0",
                        @SerializedName("rating_critic")
                        var ratingCritic: Double = 0.0,
                        @SerializedName("chart")
                        var chart: Chart = Chart(),
                        @SerializedName("s_artist")
                        var sArtist: List<String> = listOf(),
                        @SerializedName("synopsis")
                        var synopsis: String = "",
                        @SerializedName("share")
                        var share: String = "",
                        @SerializedName("explicit")
                        var explicit: Int = 0,
                        @SerializedName("pid")
                        var pid: List<Int> = listOf(),
                        @SerializedName("p_name")
                        var p_name: List<String> = listOf(),
                        @SerializedName("restricted_download")
                        var restricted_download: Int = 0,
                        @SerializedName("tracks")
                        var tracks: ArrayList<Track> = ArrayList(),
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
                        @SerializedName("keywords")
                        var keywords: List<String> = listOf(),
                        @SerializedName("category")
                        var category: List<String> = listOf(),
                        @SerializedName("lyricistf")
                        var lyricistf: List<String> = listOf(),
                        @SerializedName("musicdirectorf")
                        var musicdirectorf: List<String> = listOf(),
                        @SerializedName("tempo")
                        var tempo: List<String> = listOf(),
                        @SerializedName("mood")
                        var mood: String = "",
                        @SerializedName("vendor")
                        var vendor: String = "",
                        @SerializedName("vendorid")
                        var vendorid: Int = 0
                    ): Parcelable {
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
                                var id: String = "",
                                @SerializedName("image")
                                var image: String = "",
                                @SerializedName("misc")
                                var misc: Misc = Misc(),
                                @SerializedName("title")
                                var title: String = "",
                                @SerializedName("subtitle")
                                var subtitle: String = "",
                                @SerializedName("duration")
                                var duration: String = "",
                                @SerializedName("releasedate")
                                var releasedate: String = "",
                                @SerializedName("name")
                                var name: String = "",
                                @SerializedName("order")
                                var order: Int = 0,
                                @SerializedName("type")
                                var type: Int = 0,
                                var isFavorite:Boolean = false
                            ) : Parcelable {
                                @Keep
                                @Parcelize
                                data class Misc(
                                    @SerializedName("description")
                                    var description: String = "",
                                    @SerializedName("share")
                                    var share: String = "",
                                    @SerializedName("duration")
                                    var duration: String = "",
                                    @SerializedName("synopsis")
                                    var synopsis: String = "",
                                    @SerializedName("movierights")
                                    var movierights: List<String> = listOf(),
                                    @SerializedName("restricted_download")
                                    var restricted_download: Int = 1,
                                    @SerializedName("explicit")
                                    var explicit: Int = 0,
                                    @SerializedName("attribute_censor_rating")
                                    var attributeCensorRating: List<String> = listOf()
                                ) : Parcelable
                            }
                        }
                    }

                    @Keep
                    @Parcelize
                    class Chart(
                        @SerializedName("indicator")
                        var indicator: Int? = null,
                    ): Parcelable
                }
                @Keep
                @Parcelize
                data class Season(
                    @SerializedName("data")
                    var `data`: SeasonData = SeasonData(),
                    @SerializedName("itype")
                    var itype: Int = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class SeasonData(
                        @SerializedName("id")
                        var id: String = "",
                        @SerializedName("misc")
                        var misc: Misc = Misc(),
                        @SerializedName("order")
                        var order: Int = 0,
                        @SerializedName("title")
                        var title: String = "",
                        @SerializedName("type")
                        var type: Int = 0
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Misc(
                            @SerializedName("tracks")
                            var tracks: List<Track> = listOf()
                        ) : Parcelable {
                            @Keep
                            @Parcelize
                            data class Track(
                                @SerializedName("data")
                                var `data`: TrackData = TrackData(),
                                @SerializedName("itype")
                                var itype: Int = 0
                            ) : Parcelable {
                                @Keep
                                @Parcelize
                                data class TrackData(
                                    @SerializedName("id")
                                    var id: String = "",
                                    @SerializedName("image")
                                    var image: String = "",
                                    @SerializedName("misc")
                                    var misc: Misc = Misc(),
                                    @SerializedName("name")
                                    var name: String = "",
                                    @SerializedName("subTitle")
                                    var subTitle: String = "",
                                    @SerializedName("releasedate")
                                    var releasedate: String = "",
                                    @SerializedName("order")
                                    var order: Int = 0,
                                    @SerializedName("season")
                                    var season: String = "",
                                    @SerializedName("episode")
                                    var episode: String = "",
                                    @SerializedName("type")
                                    var type: Int = 0
                                ) : Parcelable {
                                    @Keep
                                    @Parcelize
                                    data class Misc(
                                        @SerializedName("description")
                                        var description: String = "",
                                        @SerializedName("share")
                                        var share: String = "",
                                        @SerializedName("duration")
                                        var duration: String = "",
                                        @SerializedName("synopsis")
                                        var synopsis: String = "",
                                        @SerializedName("restricted_download")
                                        var restricted_download: Int = 1,
                                        @SerializedName("movierights")
                                        var movierights: List<String> = listOf(),
                                        @SerializedName("attribute_censor_rating")
                                        var attributeCensorRating: List<String> = listOf()
                                    ) : Parcelable
                                }
                            }
                        }
                    }
                }
            }

            @Keep
            data class Similar(
                @SerializedName("data")
                var `data`: Data = Data(),
                @SerializedName("itype")
                var itype: Int = 0
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    var duration: String = "",
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
                    var type: Int = 0,
                    @SerializedName("variant")
                    var variant:String = "",
                    @SerializedName("variant-images")
                    var variant_images:List<String>? = null,
                    var images: MutableList<String> = mutableListOf()
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        var artist: List<Any> = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String> = listOf(),
                        @SerializedName("description")
                        var description: String = "",
                        @SerializedName("fav_count")
                        var favCount: String = "0",
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        var lang: List<String> = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<String> = listOf(),
                        @SerializedName("playcount")
                        var playcount: String = "0",
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
                        @SerializedName("rating_critic")
                        var ratingCritic: Double = 0.0,
                        @SerializedName("s_artist")
                        var sArtist: List<String> = listOf(),
                        @SerializedName("synopsis")
                        var synopsis: String = ""
                    )
                }
            }

        }

        @Keep
        @Parcelize
        data class Head(
            @SerializedName("data")
            var `data`: Data = Data(),
            @SerializedName("itype")
            var itype: Int = 0,
            @SerializedName("description")
            var description: String = "",
            @SerializedName("genre")
            var genre: List<String> = listOf(),
            @SerializedName("id")
            var id: String = "",
            @SerializedName("duration")
            var duration: String = "",
            @SerializedName("rating_critic")
            var ratingCritic: String = "",
            @SerializedName("image")
            var image: String = "",
            @SerializedName("share")
            var share: String = "",
            @SerializedName("lang")
            var lang: List<String> = listOf(),
            @SerializedName("movierights")
            var movierights: List<String> = listOf(),
            @SerializedName("nudity")
            var nudity: String = "",
            @SerializedName("releasedate")
            var releasedate: String = "",
            @SerializedName("title")
            var title: String = "",
            @SerializedName("subTitle")
            var subTitle: String = "",
            @SerializedName("subtitle")
            var subtitle: String = "",
            @SerializedName("type")
            var type: Int = 0,
            @SerializedName("attribute_censor_rating")
            var attributeCensorRating: List<String> = listOf()
        ): Parcelable  {
            @Keep
            @Parcelize
            data class Data(
                @SerializedName("duration")
                var duration: String = "",
                @SerializedName("id")
                var id: String = "",
                @SerializedName("image")
                var image: String = "",
                @SerializedName("misc")
                var misc: Misc = Misc(),
                @SerializedName("releasedate")
                var releasedate: String = "",
                @SerializedName("category")
                var category: List<String> = listOf(),
                @SerializedName("genre")
                var genre: List<String> = listOf(),
                @SerializedName("subtitle")
                var subtitle: String = "",
                @SerializedName("subTitle")
                var subTitle: String = "",
                @SerializedName("handlename")
                var handlename: String = "",
                @SerializedName("title")
                var title: String = "",
                @SerializedName("type")
                var type: Int = 0,
                @SerializedName("uid")
                var uid: String = "",
                var isFavorite:Boolean = false,
                @SerializedName("items")
                var items: ArrayList<Body.Row> = ArrayList(),
                @SerializedName("adsId")
                var adsId: String = "",
                @SerializedName("shareableURL")
                var shareableURL: String = ""
            ): Parcelable {
                @Keep
                @Parcelize
                data class Misc(
                    @SerializedName("genre")
                    var genre: List<String> = listOf(),
                    @SerializedName("singerf")
                    var singerf: List<String> = listOf(),
                    @SerializedName("actorf")
                    var actorf: List<String> = listOf(),
                    @SerializedName("lyricist")
                    var lyricist: List<String> = listOf(),
                    @SerializedName("lyricistf")
                    var lyricistf: List<String> = listOf(),
                    @SerializedName("mood")
                    var mood: String = "",
                    @SerializedName("musicdirectorf")
                    var musicdirectorf: List<String> = listOf(),
                    @SerializedName("tempo")
                    var tempo: List<String> = listOf(),
                    @SerializedName("artist")
                    var artist: List<String> = listOf(),
                    @SerializedName("attribute_censor_rating")
                    var attributeCensorRating: List<String> = listOf(),
                    @SerializedName("description")
                    var description: String = "",
                    @SerializedName("fav_count")
                    var favCount: String = "",
                    @SerializedName("f_fav_count")
                    var f_FavCount: String = "",
                    @SerializedName("share")
                    var share: String = "",
                    @SerializedName("items")
                    var items: Int = 0,
                    @SerializedName("lang")
                    var lang: List<String> = listOf(),
                    @SerializedName("movierights")
                    var movierights: List<String> = listOf(),
                    @SerializedName("playcount")
                    var playcount: String = "",
                    @SerializedName("f_playcount")
                    var f_playcount: String = "",
                    @SerializedName("rating_critic")
                    var ratingCritic: Double = 0.0,
                    @SerializedName("s_artist")
                    var sArtist: List<String> = listOf(),
                    @SerializedName("synopsis")
                    var synopsis: String = "",
                    @SerializedName("vendor")
                    var vendor: String = "",
                    @SerializedName("vendorid")
                    var vendorid: Int = 0,
                    @SerializedName("episodeCount")
                    var episodeCount: Int = 0,
                    @SerializedName("restricted_download")
                    var restricted_download: Int = 0,
                    @SerializedName("nudity")
                    var nudity: String = ""
                ): Parcelable
            }
        }
    }
}