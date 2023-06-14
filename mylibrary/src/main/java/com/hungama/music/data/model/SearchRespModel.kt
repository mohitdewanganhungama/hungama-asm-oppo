package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SearchRespModel(
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
            @SerializedName("all")
            var all: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("artist")
            var artist: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("movie")
            var movie: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("podcast")
            var podcast: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("song")
            var song: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("album")
            var album: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("radio")
            var radio: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("playlist")
            var playlist: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("tv_show")
            var tvshow: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("video")
            var musicvideo: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("short_film")
            var shortfilm: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("short_video")
            var shortvideo: List<BodyRowsItemsItem> = listOf(),
            @SerializedName("live_event")
            var liveEvent: List<BodyRowsItemsItem> = listOf()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Row(
                @SerializedName("data")
                var `data`: Data? = Data(),
                @SerializedName("itype")
                var itype: Int? = 0
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Data(
                    @SerializedName("category")
                    var category: List<String?>? = listOf(),
                    @SerializedName("duration")
                    var duration: Int? = 0,
                    @SerializedName("genre")
                    var genre: List<String?>? = listOf(),
                    @SerializedName("id")
                    var id: String? = "",
                    @SerializedName("image")
                    var image: String? = "",
                    @SerializedName("misc")
                    var misc: Misc? = Misc(),
                    @SerializedName("releasedate")
                    var releasedate: String? = "",
                    @SerializedName("subgenre_name")
                    var subgenreName: List<String?>? = listOf(),
                    @SerializedName("subtitle")
                    var subtitle: String? = "",
                    @SerializedName("title")
                    var title: String? = "",
                    @SerializedName("type")
                    var type: Int? = 0,
                    @field:SerializedName("moodid")
                    val moodid: String? = "",
                    @SerializedName("variant")
                    var variant:String = "",
                    var images: MutableList<String> = mutableListOf(),
                    @SerializedName("variant-images")
                    var variant_images:List<String>? = listOf(),
                    ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Misc(
                        @SerializedName("actorf")
                        var actorf: List<String?>? = listOf(),
                        @SerializedName("artist")
                        var artist: List<String?>? = listOf(),
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<String?>? = listOf(),
                        @SerializedName("cast")
                        var cast: String? = "",
                        @SerializedName("count_era_from")
                        var countEraFrom: String? = "",
                        @SerializedName("count_era_to")
                        var countEraTo: String? = "",
                        @SerializedName("description")
                        var description: String? = "",
                        @SerializedName("dl")
                        var dl: String? = "",
                        @SerializedName("explicit")
                        var explicit: Int? = 0,
                        @SerializedName("fav_count")
                        var favCount: Int? = 0,
                        @SerializedName("keywords")
                        var keywords: List<String?>? = listOf(),
                        @SerializedName("lang")
                        var lang: List<String?>? = listOf(),
                        @SerializedName("lyricist")
                        var lyricist: List<String?>? = listOf(),
                        @SerializedName("lyricistf")
                        var lyricistf: List<String?>? = listOf(),
                        @SerializedName("mood")
                        var mood: String? = "",
                        @SerializedName("movierights")
                        var movierights: List<String?>? = listOf(),
                        @SerializedName("musicdirectorf")
                        var musicdirectorf: List<String?>? = listOf(),
                        @SerializedName("nudity")
                        var nudity: String? = "",
                        @SerializedName("p_name")
                        var pName: List<String?>? = listOf(),
                        @SerializedName("pid")
                        var pid: List<Int?>? = listOf(),
                        @SerializedName("playcount")
                        var playcount: Int? = 0,
                        @SerializedName("rating_critic")
                        var ratingCritic: String? = "",
                        @SerializedName("rating_user")
                        var ratingUser: Int? = 0,
                        @SerializedName("s_artist")
                        var sArtist: List<String?>? = listOf(),
                        @SerializedName("share")
                        var share: String? = "",
                        @SerializedName("singerf")
                        var singerf: List<String?>? = listOf(),
                        @SerializedName("skipIntro")
                        var skipIntro: SkipIntro? = SkipIntro(),
                        @SerializedName("sl")
                        var sl: Sl? = Sl(),
                        @SerializedName("synopsis")
                        var synopsis: String? = "",
                        @SerializedName("tempo")
                        var tempo: List<String?>? = listOf(),
                        @SerializedName("url")
                        var url: String? = "",
                        @SerializedName("vendor")
                        var vendor: String? = "",
                        @SerializedName("vendorid")
                        var vendorid: Int? = 0,
                        @field:SerializedName("artistid")
                        val artistid: String? = "",
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

        @Keep
        @Parcelize
        class Head(
        ) : Parcelable
    }
}