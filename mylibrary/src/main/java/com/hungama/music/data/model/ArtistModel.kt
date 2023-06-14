package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ArtistModel(
    @SerializedName("data")
    var artistData: ArtistData? = ArtistData()
) : Parcelable {
    @Keep
    @Parcelize
    data class ArtistData(
        @SerializedName("body")
        var body: Body? = Body(),
        @SerializedName("head")
        var head: Head? = Head()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Body(
            @SerializedName("albums")
            var albums: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("movies")
            var movies: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("latestnews")
            var latestnews: ArrayList<BodyRowsItemsItem?>?? = arrayListOf(),
            @SerializedName("newrelease")
            var newrelease: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("similar")
            var similar: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("songs")
            var songs: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("tvshows")
            var tvshows: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("liveperformance")
            var liveperformance: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("merchandise")
            var merchandise: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("videos")
            var videos: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Album(
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
                        @SerializedName("pid")
                        var pid: List<String?>? = listOf(),
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

            @Keep
            @Parcelize
            data class Movies(
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
                        @SerializedName("pid")
                        var pid: List<String?>? = listOf(),
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
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        var lang: List<String?>? = listOf(),
                        @SerializedName("lyricist")
                        var lyricist: List<String?>? = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<String?>? = listOf(),
                        @SerializedName("pid")
                        var pid: List<String?>? = listOf(),
                        @SerializedName("playcount")
                        var playcount: Int? = 0,
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
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

            @Keep
            @Parcelize
            data class Song(
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
                        @SerializedName("pid")
                        var pid: List<Int?>? = listOf(),
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

            @Keep
            @Parcelize
            data class Tvshow(
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
                        @SerializedName("pid")
                        var pid: List<String?>? = listOf(),
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

            @Keep
            @Parcelize
            data class Video(
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
                        @SerializedName("pid")
                        var pid: List<Int?>? = listOf(),
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
                    var attributeCensorRating: List<String?>? = listOf(),
                    @SerializedName("cast")
                    var cast: String? = "",
                    @SerializedName("description")
                    var description: String? = "",
                    @SerializedName("fav_count")
                    var favCount: Int? = 0,
                    @SerializedName("f_fav_count")
                    var f_FavCount: String = "",
                    @SerializedName("lang")
                    var lang: List<String?>? = listOf(),
                    @SerializedName("lyricist")
                    var lyricist: List<String?>? = listOf(),
                    @SerializedName("movierights")
                    var movierights: List<String?>? = listOf(),
                    @SerializedName("pid")
                    var pid: List<String?>? = listOf(),
                    @SerializedName("playcount")
                    var playcount: Int? = 0,
                    @SerializedName("f_playcount")
                    var f_playcount: String = "",
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
}