package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class AlbumModel(
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
            @SerializedName("artists")
            var artists:  ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("rows")
            var rows: ArrayList<Row> = ArrayList(),
            @SerializedName("similar")
            var similar: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
        ) {
            @Keep
            data class Artist(
                @SerializedName("data")
                var `data`: Data? = Data(),
                @SerializedName("itype")
                var itype: Int? = 0
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    var duration: Int? = 0,
                    @SerializedName("id")
                    var id: String? = "",
                    @SerializedName("image")
                    var image: String? = "",
                    @SerializedName("misc")
                    var misc: Misc? = Misc(),
                    @SerializedName("subtitle")
                    var subtitle: String? = "",
                    @SerializedName("title")
                    var title: String? = "",
                    @SerializedName("type")
                    var type: Int? = 0
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        var artist: List<String?>? = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String?>? = listOf(),
                        @SerializedName("description")
                        var description: String? = "",
                        @SerializedName("fav_count")
                        var favCount: String? = "",
                        @SerializedName("movierights")
                        var movierights: List<String?>? = listOf(),
                        @SerializedName("playcount")
                        var playcount: String? = "",
                        @SerializedName("rating_critic")
                        var ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        var sArtist: List<String?>? = listOf(),
                        @SerializedName("synopsis")
                        var synopsis: String? = ""
                    )
                }
            }

            @Keep
            data class Row(
                @SerializedName("data")
                var `data`: Data = Data(),
                @SerializedName("itype")
                var itype: Int = 0,
                var adUnitId: String = ""
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    var duration: Int? = 0,
                    @SerializedName("id")
                    var id: String? = "",
                    @SerializedName("image")
                    var image: String? = "",
                    @SerializedName("misc")
                    var misc: Misc = Misc(),
                    @SerializedName("releasedate")
                    var releasedate: String? = "",
                    @SerializedName("subtitle")
                    var subtitle: String? = "",
                    @SerializedName("title")
                    var title: String? = "",
                    @SerializedName("type")
                    var type: Int? = 0,
                    var isFavorite:Boolean = false,
                    @SerializedName("genre")
                    var genre: List<String> = listOf()
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("singerf")
                        var singerf: List<String> = listOf(),
                        @SerializedName("actorf")
                        var actorf: List<String> = listOf(),
                        @SerializedName("artist")
                        var artist: List<String?>? = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String?>? = listOf(),
                        @SerializedName("description")
                        var description: String? = "",
                        @SerializedName("fav_count")
                        var favCount: Int? = 0,
                        @SerializedName("lang")
                        var lang: List<String?>? = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<String?>? = listOf(),
                        @SerializedName("nudity")
                        var nudity: String? = "",
                        @SerializedName("playcount")
                        var playcount: Int? = 0,
                        @SerializedName("rating_critic")
                        var ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        var sArtist: List<String?>? = listOf(),
                        @SerializedName("synopsis")
                        var synopsis: String? = "",
                        @SerializedName("share")
                        var share: String? = "",
                        @SerializedName("explicit")
                        var explicit: Int = 0,
                        @SerializedName("pid")
                        var pid: List<Int> = listOf(),
                        @SerializedName("p_name")
                        var p_name: List<String> = listOf(),
                        @SerializedName("restricted_download")
                        var restricted_download: Int = 1
                    )
                }
            }

            @Keep
            data class Similar(
                @SerializedName("data")
                var `data`: Data? = Data(),
                @SerializedName("itype")
                var itype: Int? = 0
            ) {
                @Keep
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
                    var type: Int? = 0
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        var artist: List<String?>? = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String?>? = listOf(),
                        @SerializedName("description")
                        var description: String? = "",
                        @SerializedName("fav_count")
                        var favCount: Int? = 0,
                        @SerializedName("lang")
                        var lang: List<String?>? = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<String?>? = listOf(),
                        @SerializedName("playcount")
                        var playcount: Int? = 0,
                        @SerializedName("rating_critic")
                        var ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        var sArtist: List<String?>? = listOf(),
                        @SerializedName("synopsis")
                        var synopsis: String? = ""
                    )
                }
            }
        }

        @Keep
        data class Head(
            @SerializedName("data")
            var `data`: Data = Data(),
            @SerializedName("itype")
            var itype: Int? = 0
        ) {
            @Keep
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
                @SerializedName("category")
                var category: List<String> = listOf(),
                @SerializedName("genre")
                var genre: List<String> = listOf(),
                @SerializedName("title")
                var title: String? = "",
                @SerializedName("type")
                var type: Int? = 0
            ) {
                @Keep
                @Parcelize
                data class Misc(
                    @SerializedName("singerf")
                    var singerf: List<String> = listOf(),
                    @SerializedName("actorf")
                    var actorf: List<String> = listOf(),
                    @SerializedName("keywords")
                    var keywords: List<String> = listOf(),
                    @SerializedName("artist")
                    var artist: List<String> = listOf(),
                    @SerializedName("attribute_censor_rating")
                    var attributeCensorRating: List<String> = listOf(),
                    @SerializedName("cast")
                    var cast: String = "",
                    @SerializedName("rating_user")
                    var rating_user: String = "",
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
                    @SerializedName("f_fav_count")
                    var f_FavCount: String = "",
                    @SerializedName("lang")
                    var lang: List<String> = listOf(),
                    @SerializedName("lyricist")
                    var lyricist: List<String> = listOf(),
                    @SerializedName("lyricistf")
                    var lyricistf: List<String> = listOf(),
                    @SerializedName("mood")
                    var mood: String = "",
                    @SerializedName("movierights")
                    var movierights: List<String> = listOf(),
                    @SerializedName("musicdirectorf")
                    var musicdirectorf: List<String> = listOf(),
                    @SerializedName("nudity")
                    var nudity: String = "",
                    @SerializedName("p_name")
                    var pName: List<String> = listOf(),
                    @SerializedName("pid")
                    var pid: List<Int> = listOf(),
                    @SerializedName("playcount")
                    var playcount: String = "0",
                    @SerializedName("f_playcount")
                    var f_playcount: String = "",
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
                    @SerializedName("tempo")
                    var tempo: List<String> = listOf(),
                    @SerializedName("url")
                    var url: String = "",
                    @SerializedName("share")
                    var share: String = "",
                    @SerializedName("vendor")
                    var vendor: String = "",
                    @SerializedName("vendorid")
                    var vendorid: Int = 0
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
                    data class Sl(
                        @SerializedName("lyric")
                        var lyric: Lyric? = Lyric()
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Lyric(
                            @SerializedName("lang")
                            var lang: String? = "",
                            @SerializedName("lang_id")
                            var langId: Int? = 0,
                            @SerializedName("link")
                            var link: String? = ""
                        ) : Parcelable
                    }
                }
            }
        }
    }
}