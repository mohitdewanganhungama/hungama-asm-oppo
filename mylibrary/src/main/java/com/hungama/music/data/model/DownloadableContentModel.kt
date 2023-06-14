package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
open class DownloadableContentModel(
    @SerializedName("data")
    var `data`: Data = Data()
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("head")
        var head: Head = Head()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Head(
            @SerializedName("data")
            var headData: HeadData = HeadData(),
            @SerializedName("itype")
            var itype: Int = 0
        ) : Parcelable {
            @Keep
            @Parcelize
            data class HeadData(
                @SerializedName("duration")
                var duration: String = "",
                @SerializedName("genre")
                var genre: List<String> = listOf(),
                @SerializedName("subgenre_name")
                var subgenre_name: List<String> = listOf(),
                @SerializedName("category")
                var category: List<String> = listOf(),
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
                    @SerializedName("download_link")
                    var downloadLink: DownloadLink = DownloadLink(),
                    @SerializedName("explicit")
                    var explicit: Int = 0,
                    @SerializedName("fav_count")
                    var favCount: String = "0",
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
                    @SerializedName("rating_critic")
                    var ratingCritic: Double = 0.0,
                    @SerializedName("s_artist")
                    var sArtist: List<String> = listOf(),
                    @SerializedName("singerf")
                    var singerf: List<String> = listOf(),
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
                    var vendorid: Int = 0,
                    @SerializedName("restricted_download")
                    var restricted_download: Int = 1
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class SkipIntro(
                        @SerializedName("skipCreditET")
                        var skipCreditET: Long = 0L,
                        @SerializedName("skipCreditST")
                        var skipCreditST: Long = 0L,
                        @SerializedName("skipIntroET")
                        var skipIntroET: Long = 0L,
                        @SerializedName("skipIntroST")
                        var skipIntroST: Long = 0L
                    ) : Parcelable

                    @Keep
                    @Parcelize
                    data class DownloadLink(

                        @field:SerializedName("mdn")
                        var mdn: Drm = Drm(),

                        @field:SerializedName("drm")
                        var drm: Drm = Drm()
                    ) : Parcelable{
                        @Keep
                        @Parcelize
                        data class Drm(
                            @SerializedName("fileSubTypeId")
                            var fileSubTypeId: String = "",
                            @SerializedName("url")
                            var url: String = "",
                            @SerializedName("token")
                            var token: String = "",
                            @SerializedName("tokenType")
                            var tokenType: String = ""
                        ) : Parcelable
                    }

                    @Keep
                    @Parcelize
                    data class Sl(
                        @SerializedName("lyric")
                        var lyric: Lyric = Lyric(),
                        @SerializedName("subtitle")
                        var subtitle: SubtitleItem? = SubtitleItem()
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

                        @Keep
                        @Parcelize
                        data class SubtitleItem(

                            @field:SerializedName("link")
                            var link: String? = null,

                            @field:SerializedName("lang_id")
                            var langId: Int? = null,

                            var isSelected: Boolean = false,

                            @field:SerializedName("lang")
                            var lang: String? = null
                        ) : Parcelable
                    }

                }
            }
        }
    }
}