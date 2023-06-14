package com.hungama.music.data.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SongDetailModel(
    @SerializedName("data")
    val `data`: Data
) {
    @Keep
    data class Data(
        @SerializedName("body")
        val body: Body,
        @SerializedName("head")
        val head: Head
    ) {
        @Keep
        data class Body(
            @SerializedName("recomendation")
            val recomendation: ArrayList<RowsItem?>? = ArrayList(),
            @SerializedName("artist")
            val artist: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("playlist")
            val playlist: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
        ) {
            @Keep
            data class Artist(
                @SerializedName("data")
                val `data`: Data,
                @SerializedName("itype")
                val itype: Int
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    val duration: Int,
                    @SerializedName("id")
                    val id: String,
                    @SerializedName("image")
                    val image: String? = "",
                    @SerializedName("misc")
                    val misc: Misc,
                    @SerializedName("subtitle")
                    val subtitle: String? = "",
                    @SerializedName("title")
                    val title: String? = "",
                    @SerializedName("type")
                    val type: Int
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        val artist: List<String>,
                        @SerializedName("attribute_censor_rating")
                        val attributeCensorRating: List<Any>,
                        @SerializedName("cast")
                        val cast: String? = "",
                        @SerializedName("count_era_from")
                        val countEraFrom: String? = "",
                        @SerializedName("count_era_to")
                        val countEraTo: String? = "",
                        @SerializedName("description")
                        val description: String? = "",
                        @SerializedName("fav_count")
                        val favCount: Int,
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        val lang: List<String>,
                        @SerializedName("lyricist")
                        val lyricist: List<Any>,
                        @SerializedName("mood")
                        val mood: String? = "",
                        @SerializedName("movierights")
                        val movierights: List<Any>,
                        @SerializedName("pid")
                        val pid: List<Any>,
                        @SerializedName("playcount")
                        val playcount: Int,
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
                        @SerializedName("rating_critic")
                        val ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        val sArtist: List<String>,
                        @SerializedName("skipIntro")
                        val skipIntro: SkipIntro,
                        @SerializedName("sl")
                        val sl: Sl,
                        @SerializedName("synopsis")
                        val synopsis: String? = "",
                        @SerializedName("url")
                        val url: String? = "",
                        @SerializedName("vendor")
                        val vendor: String
                    ) {
                        @Keep
                        data class SkipIntro(
                            @SerializedName("skipCreditET")
                            val skipCreditET: Int,
                            @SerializedName("skipCreditST")
                            val skipCreditST: Int,
                            @SerializedName("skipIntroET")
                            val skipIntroET: Int,
                            @SerializedName("skipIntroST")
                            val skipIntroST: Int
                        )

                        @Keep
                        class Sl(
                        )
                    }
                }
            }

            @Keep
            data class Playlist(
                @SerializedName("data")
                val `data`: Data,
                @SerializedName("itype")
                val itype: Int
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    val duration: Int,
                    @SerializedName("genre")
                    val genre: List<String>,
                    @SerializedName("id")
                    val id: String,
                    @SerializedName("image")
                    val image: String? = "",
                    @SerializedName("misc")
                    val misc: Misc,
                    @SerializedName("releasedate")
                    val releasedate: String? = "",
                    @SerializedName("subtitle")
                    val subtitle: String? = "",
                    @SerializedName("title")
                    val title: String? = "",
                    @SerializedName("type")
                    val type: Int
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        val artist: List<Any>,
                        @SerializedName("attribute_censor_rating")
                        val attributeCensorRating: List<Any>,
                        @SerializedName("cast")
                        val cast: String? = "",
                        @SerializedName("count_era_from")
                        val countEraFrom: String? = "",
                        @SerializedName("count_era_to")
                        val countEraTo: String? = "",
                        @SerializedName("description")
                        val description: String? = "",
                        @SerializedName("fav_count")
                        val favCount: Int,
                        @SerializedName("lang")
                        val lang: List<String>,
                        @SerializedName("lyricist")
                        val lyricist: List<Any>,
                        @SerializedName("mood")
                        val mood: String? = "",
                        @SerializedName("movierights")
                        val movierights: List<Any>,
                        @SerializedName("pid")
                        val pid: List<Any>,
                        @SerializedName("playcount")
                        val playcount: Int,
                        @SerializedName("rating_critic")
                        val ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        val sArtist: List<String>,
                        @SerializedName("skipIntro")
                        val skipIntro: SkipIntro,
                        @SerializedName("sl")
                        val sl: Sl,
                        @SerializedName("synopsis")
                        val synopsis: String? = "",
                        @SerializedName("url")
                        val url: String? = "",
                        @SerializedName("vendor")
                        val vendor: String
                    ) {
                        @Keep
                        data class SkipIntro(
                            @SerializedName("skipCreditET")
                            val skipCreditET: Int,
                            @SerializedName("skipCreditST")
                            val skipCreditST: Int,
                            @SerializedName("skipIntroET")
                            val skipIntroET: Int,
                            @SerializedName("skipIntroST")
                            val skipIntroST: Int
                        )

                        @Keep
                        class Sl(
                        )
                    }
                }
            }
        }

        @Keep
        data class Head(
            @SerializedName("data")
            val `data`: Data,
            @SerializedName("itype")
            val itype: Int
        ) {
            @Keep
            data class Data(
                @SerializedName("duration")
                val duration: Int,
                @SerializedName("genre")
                val genre: List<String>,
                @SerializedName("id")
                val id: String,
                @SerializedName("image")
                val image: String? = "",
                @SerializedName("playble_image")
                val playble_image: String? = "",
                @SerializedName("misc")
                val misc: Misc,
                @SerializedName("releasedate")
                val releasedate: String? = "",
                @SerializedName("subtitle")
                val subtitle: String? = "",
                @SerializedName("title")
                val title: String? = "",
                @SerializedName("type")
                val type: Int,
                var isFavorite:Boolean = false
            ) {
                @Keep
                data class Misc(
                    @SerializedName("artist")
                    val artist: List<String>,
                    @SerializedName("attribute_censor_rating")
                    val attributeCensorRating: List<Any>,
                    @SerializedName("cast")
                    val cast: String? = "",
                    @SerializedName("count_era_from")
                    val countEraFrom: String? = "",
                    @SerializedName("count_era_to")
                    val countEraTo: String? = "",
                    @SerializedName("description")
                    val description: String? = "",
                    @SerializedName("fav_count")
                    val favCount: Int,
                    @SerializedName("lang")
                    val lang: List<String>,
                    @SerializedName("lyricist")
                    val lyricist: List<String>,
                    @SerializedName("mood")
                    val mood: String? = "",
                    @SerializedName("movierights")
                    val movierights: List<Any>,
                    @SerializedName("nudity")
                    val nudity: String? = "",
                    @SerializedName("pid")
                    val pid: List<Int>,
                    @SerializedName("p_name")
                    val p_name: List<String>,
                    @SerializedName("playcount")
                    val playcount: Int,
                    @SerializedName("rating_critic")
                    val ratingCritic: Double? = 0.0,
                    @SerializedName("s_artist")
                    val sArtist: List<String>,
                    @SerializedName("skipIntro")
                    val skipIntro: SkipIntro,
                    @SerializedName("sl")
                    val sl: Sl,
                    @SerializedName("synopsis")
                    val synopsis: String? = "",
                    @SerializedName("url")
                    val url: String? = "",
                    @SerializedName("share")
                    val share: String? = "",
                    @SerializedName("vendor")
                    val vendor: String? = "",
                    @SerializedName("explicit")
                    var explicit: Int = 0,
                    @SerializedName("restricted_download")
                    var restricted_download: Int = 1
                ) {
                    @Keep
                    data class SkipIntro(
                        @SerializedName("skipCreditET")
                        val skipCreditET: Int,
                        @SerializedName("skipCreditST")
                        val skipCreditST: Int,
                        @SerializedName("skipIntroET")
                        val skipIntroET: Int,
                        @SerializedName("skipIntroST")
                        val skipIntroST: Int
                    )

                    @Keep
                    class Sl(
                    )
                }
            }
        }
    }
}