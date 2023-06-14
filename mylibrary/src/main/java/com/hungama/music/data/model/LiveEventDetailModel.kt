package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class LiveEventDetailModel(
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
            @SerializedName("albums")
            var albums: Albums = Albums(),
            @SerializedName("movies")
            var movies: Movies = Movies(),
            @SerializedName("newreleasesong")
            var newreleasesong: Newreleasesong = Newreleasesong(),
            @SerializedName("similar")
            var similar: Similar = Similar(),
            @SerializedName("songs")
            var songs: Songs = Songs(),
            @SerializedName("tvshows")
            var tvshows: Tvshows = Tvshows(),
            @SerializedName("videos")
            var videos: Videos = Videos()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Albums(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
                    @SerializedName("data")
                    var `data`: Data = Data(),
                    @SerializedName("itype")
                    var itype: Int = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Data(
                        @SerializedName("duration")
                        var duration: Int = 0,
                        @SerializedName("genre")
                        var genre: List<String> = listOf(),
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
                        var type: Int = 0
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Misc(
                            @SerializedName("artist")
                            var artist: List<String> = listOf(),
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
                            @SerializedName("lyricist")
                            var lyricist: List<String> = listOf(),
                            @SerializedName("mood")
                            var mood: String = "",
                            @SerializedName("movierights")
                            var movierights: List<String> = listOf(),
                            @SerializedName("nudity")
                            var nudity: String = "",
                            @SerializedName("pid")
                            var pid: List<String> = listOf(),
                            @SerializedName("playcount")
                            var playcount: String = "0",
                            @SerializedName("rating_critic")
                            var ratingCritic: Double? = 0.0,
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
                            var vendor: String = ""
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
            data class Movies(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable

            @Keep
            @Parcelize
            data class Newreleasesong(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: List<Item> = listOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
                    @SerializedName("data")
                    var `data`: Data = Data(),
                    @SerializedName("itype")
                    var itype: Int = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Data(
                        @SerializedName("duration")
                        var duration: Int = 0,
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
                            @SerializedName("playcount")
                            var playcount: String = "0",
                            @SerializedName("rating_critic")
                            var ratingCritic: Double? = 0.0,
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
                            var vendor: String = ""
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
            data class Similar(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
                    @SerializedName("data")
                    var `data`: Data = Data(),
                    @SerializedName("itype")
                    var itype: Int = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Data(
                        @SerializedName("duration")
                        var duration: Int = 0,
                        @SerializedName("id")
                        var id: String = "",
                        @SerializedName("image")
                        var image: String = "",
                        @SerializedName("misc")
                        var misc: Misc = Misc(),
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
                            @SerializedName("f_fav_count")
                            var f_FavCount: String = "",
                            @SerializedName("lang")
                            var lang: List<String> = listOf(),
                            @SerializedName("lyricist")
                            var lyricist: List<String> = listOf(),
                            @SerializedName("mood")
                            var mood: String = "",
                            @SerializedName("movierights")
                            var movierights: List<String> = listOf(),
                            @SerializedName("pid")
                            var pid: List<String> = listOf(),
                            @SerializedName("playcount")
                            var playcount: String = "0",
                            @SerializedName("rating_critic")
                            var ratingCritic: Double? = 0.0,
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
                            var vendor: String = ""
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
            data class Songs(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
                    @SerializedName("data")
                    var `data`: Data = Data(),
                    @SerializedName("itype")
                    var itype: Int = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Data(
                        @SerializedName("duration")
                        var duration: Int = 0,
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
                            @SerializedName("playcount")
                            var playcount: String = "0",
                            @SerializedName("rating_critic")
                            var ratingCritic: Double? = 0.0,
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
            data class Tvshows(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: List<Items> = listOf()
            ) : Parcelable

            @Keep
            @Parcelize
            data class Items(
                @SerializedName("itype")
                var itype: String = "") : Parcelable

            @Keep
            @Parcelize
            data class Videos(
                @SerializedName("bucketQuery")
                var bucketQuery: String = "",
                @SerializedName("items")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
                    @SerializedName("data")
                    var `data`: Data = Data(),
                    @SerializedName("itype")
                    var itype: Int = 0
                ) : Parcelable {
                    @Keep
                    @Parcelize
                    data class Data(
                        @SerializedName("duration")
                        var duration: Int = 0,
                        @SerializedName("genre")
                        var genre: List<String> = listOf(),
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
                        var type: Int = 0
                    ) : Parcelable {
                        @Keep
                        @Parcelize
                        data class Misc(
                            @SerializedName("artist")
                            var artist: List<String> = listOf(),
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
                            @SerializedName("playcount")
                            var playcount: String = "0",
                            @SerializedName("rating_critic")
                            var ratingCritic: Double? = 0.0,
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
                            var vendor: String = ""
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
        }

        @Keep
        @Parcelize
        data class Head(
            @SerializedName("data")
            var `data`: Data = Data(),
            @SerializedName("event")
            var event: Event = Event(),
            @SerializedName("itype")
            var itype: Int = 0
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Data(
                @SerializedName("duration")
                var duration: Int = 0,
                @SerializedName("id")
                var id: String = "",
                @SerializedName("image")
                var image: String = "",
                @SerializedName("misc")
                var misc: Misc = Misc(),
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
                    @SerializedName("attribute_censor_rating")
                    var attributeCensorRating: List<String> = listOf(),
                    @SerializedName("cast")
                    var cast: String = "",
                    @SerializedName("count_era_from")
                    var countEraFrom: String = "",
                    @SerializedName("count_era_to")
                    var countEraTo: String = "",
                    @SerializedName("share")
                    var share: String = "",
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
                    @SerializedName("mood")
                    var mood: String = "",
                    @SerializedName("movierights")
                    var movierights: ArrayList<String> = ArrayList(),
                    @SerializedName("pid")
                    var pid: List<String> = listOf(),
                    @SerializedName("playcount")
                    var playcount: String = "0",
                    @SerializedName("f_playcount")
                    var f_playcount: String = "",
                    @SerializedName("rating_critic")
                    var ratingCritic: Double? = 0.0,
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
                    var vendor: String = ""
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

            @Keep
            @Parcelize
            data class Event(
                @SerializedName("about")
                var about: String = "",
                @SerializedName("artistId")
                var artistId: String = "",
                @SerializedName("artistName")
                var artistName: String = "",
                @SerializedName("date")
                var date: String = "",
                @SerializedName("_id")
                var id: String = "",
                @SerializedName("image")
                var image: List<String> = listOf(),
                @SerializedName("name")
                var name: String = "",
                @SerializedName("thumbnail")
                var thumbnail: List<String> = listOf(),
                @SerializedName("ticketCost")
                var ticketCost: Double = 0.0,
                @SerializedName("type")
                var type: String = "",
                @SerializedName("url")
                var url: String = "",
                @SerializedName("mode")
                var mode: String = "",
                @SerializedName("storeId")
                var storeId: String = "",
                @SerializedName("contentId")
                var contentId: String = "",
                @SerializedName("movierights")
                var movierights: ArrayList<String> = ArrayList(),
                @SerializedName("__v")
                var v: Int = 0
            ) : Parcelable
        }
    }
}