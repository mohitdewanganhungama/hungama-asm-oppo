package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import androidx.room.Ignore
import com.google.gson.annotations.Expose

@Keep
data class VideoDetailModel(
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
            @SerializedName("artistvideos")
            var artistvideos: ArrayList<Artistvideo> = ArrayList(),
            @SerializedName("recommended")
            val recommended: ArrayList<BodyRowsItemsItem?>? = arrayListOf(),
            @SerializedName("similar")
            val similar:ArrayList<BodyRowsItemsItem?>? = arrayListOf()
        ) {
            @Keep
            data class Artistvideo(
                @SerializedName("data")
                var `data`: Data = Data(),
                @SerializedName("itype")
                var itype: Int = 0,
                var adUnitId: String = ""
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    val duration: Long = 0,
                    @SerializedName("id")
                    val id: String = "",
                    @SerializedName("image")
                    val image: String = "",
                    @SerializedName("misc")
                    val misc: Misc = Misc(),
                    @SerializedName("releasedate")
                    val releasedate: String = "",
                    @SerializedName("subtitle")
                    val subtitle: String = "",
                    @SerializedName("title")
                    val title: String = "",
                    @Expose
                    var isCurrentPlaying: Boolean = false,
                    @SerializedName("type")
                    val type: Int = 0
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        val artist: List<String> = listOf(),
                        @SerializedName("attribute_censor_rating")
                        val attributeCensorRating: List<Any> = listOf(),
                        @SerializedName("description")
                        val description: String = "",
                        @SerializedName("fav_count")
                        val favCount: Int = 0,
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        val lang: List<String> = listOf(),
                        @SerializedName("lyricist")
                        val lyricist: List<Any> = listOf(),
                        @SerializedName("movierights")
                        val movierights: List<String> = listOf(),
                        @SerializedName("nudity")
                        val nudity: String = "",
                        @SerializedName("playcount")
                        val playcount: Int = 0,
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
                        @SerializedName("rating_critic")
                        val ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        val sArtist: List<String> = listOf(),
                        @SerializedName("synopsis")
                        val synopsis: String = "",
                        @SerializedName("vendor")
                        val vendor: String = ""
                    )
                }
            }

            @Keep
            data class Recommended(
                @SerializedName("data")
                val `data`: Data,
                @SerializedName("itype")
                val itype: Int
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    val duration: Long,
                    @SerializedName("id")
                    val id: String,
                    @SerializedName("image")
                    val image: String,
                    @SerializedName("misc")
                    val misc: Misc,
                    @SerializedName("releasedate")
                    val releasedate: String,
                    @SerializedName("subtitle")
                    val subtitle: String,
                    @SerializedName("title")
                    val title: String,
                    @SerializedName("type")
                    val type: Int
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        val artist: List<Any>,
                        @SerializedName("attribute_censor_rating")
                        val attributeCensorRating: List<Any>,
                        @SerializedName("description")
                        val description: String,
                        @SerializedName("fav_count")
                        val favCount: Int,
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        val lang: List<String>,
                        @SerializedName("lyricist")
                        val lyricist: List<Any>,
                        @SerializedName("movierights")
                        val movierights: List<Any>,
                        @SerializedName("playcount")
                        val playcount: Int,
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
                        @SerializedName("rating_critic")
                        val ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        val sArtist: List<String>,
                        @SerializedName("synopsis")
                        val synopsis: String,
                        @SerializedName("vendor")
                        val vendor: String
                    )
                }
            }

            @Keep
            data class Similar(
                @SerializedName("data")
                val `data`: Data,
                @SerializedName("itype")
                val itype: Int
            ) {
                @Keep
                data class Data(
                    @SerializedName("duration")
                    val duration: Long,
                    @SerializedName("id")
                    val id: String,
                    @SerializedName("image")
                    val image: String,
                    @SerializedName("misc")
                    val misc: Misc,
                    @SerializedName("releasedate")
                    val releasedate: String,
                    @SerializedName("subtitle")
                    val subtitle: String,
                    @SerializedName("title")
                    val title: String,
                    @SerializedName("type")
                    val type: Int
                ) {
                    @Keep
                    data class Misc(
                        @SerializedName("artist")
                        val artist: List<Any>,
                        @SerializedName("attribute_censor_rating")
                        val attributeCensorRating: List<Any>,
                        @SerializedName("description")
                        val description: String,
                        @SerializedName("fav_count")
                        val favCount: Int,
                        @SerializedName("f_fav_count")
                        var f_FavCount: String = "",
                        @SerializedName("lang")
                        val lang: List<String>,
                        @SerializedName("lyricist")
                        val lyricist: List<Any>,
                        @SerializedName("movierights")
                        val movierights: List<Any>,
                        @SerializedName("playcount")
                        val playcount: Int,
                        @SerializedName("f_playcount")
                        var f_playcount: String = "",
                        @SerializedName("rating_critic")
                        val ratingCritic: Double? = 0.0,
                        @SerializedName("s_artist")
                        val sArtist: List<String>,
                        @SerializedName("synopsis")
                        val synopsis: String,
                        @SerializedName("vendor")
                        val vendor: String
                    )
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
                val duration: Long,
                @SerializedName("id")
                val id: String,
                @SerializedName("image")
                val image: String,
                @SerializedName("misc")
                val misc: Misc,
                @SerializedName("releasedate")
                val releasedate: String,
                @SerializedName("subtitle")
                val subtitle: String,
                @SerializedName("title")
                val title: String,
                @SerializedName("type")
                val type: Int,
                @SerializedName("items")
                var items: ArrayList<Body.Artistvideo> = ArrayList()
            ) {
                @Keep
                data class Misc(
                    @SerializedName("genre")
                    var genre: List<String> = listOf(),
                    @SerializedName("artist")
                    val artist: List<String>,
                    @SerializedName("attribute_censor_rating")
                    val attributeCensorRating: List<String>,
                    @SerializedName("share")
                    val share: String,
                    @SerializedName("description")
                    val description: String,
                    @SerializedName("fav_count")
                    val favCount: Int,
                    @SerializedName("f_fav_count")
                    var f_FavCount: String = "",
                    @SerializedName("lang")
                    val lang: List<String>,
                    @SerializedName("lyricist")
                    val lyricist: List<Any>,
                    @SerializedName("movierights")
                    val movierights: List<String>,
                    @SerializedName("nudity")
                    val nudity: String,
                    @SerializedName("playcount")
                    val playcount: Int,
                    @SerializedName("f_playcount")
                    var f_playcount: String = "",
                    @SerializedName("rating_critic")
                    val ratingCritic: Double? = 0.0,
                    @SerializedName("s_artist")
                    val sArtist: List<String>,
                    @SerializedName("synopsis")
                    val synopsis: String,
                    @SerializedName("vendor")
                    val vendor: String,
                    @SerializedName("restricted_download")
                    var restricted_download: Int = 1
                )
            }
        }
    }
}