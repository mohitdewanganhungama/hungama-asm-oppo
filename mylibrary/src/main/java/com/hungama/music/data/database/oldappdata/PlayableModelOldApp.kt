package com.hungama.music.data.database.oldappdata


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PlayableModelOldApp(
    @SerializedName("firebase_sources")
    var firebaseSources: List<String?>? = listOf(),
    @SerializedName("last_modified")
    var lastModified: Int? = 0,
    @SerializedName("playlistname")
    var playlistname: String? = "",@SerializedName("bucketname")
    var bucketname: String? = "",
    @SerializedName("response")
    var response: Response? = Response(),
    @SerializedName("source")
    var source: String? = "",
    @SerializedName("is_adult")
    var is_adult: Boolean = false,
    @SerializedName("myPlaylistID")
    var myPlaylistID: String = ""
) : Parcelable {
    @Keep
    @Parcelize
    data class Response(
        @SerializedName("podcast")
        var podcast: Podcast? = Podcast(),
        @SerializedName("podcastlisting")
        var podcastlisting: Podcastlisting? = Podcastlisting(),
        @SerializedName("album_id")
        var albumId: Int = 0,
        @SerializedName("album_name")
        var albumName: String = "",
        @SerializedName("artist_name")
        var artistName: String = "",
        @SerializedName("attribute_tempo")
        var attributeTempo: String = "",
        @SerializedName("attribute_type")
        var attributeType: String = "",
        @SerializedName("attribute_censor_rating")
        var attributeCensorRating: String = "",
        @SerializedName("attribute_Keyword")
        var attributeKeyword: String = "",
        @SerializedName("cast")
        var cast: String = "",
        @SerializedName("comments_count")
        var commentsCount: Int = 0,
        @SerializedName("content_id")
        var contentId: Int = 0,
        @SerializedName("delivery_id")
        var deliveryId: Int = 0,
        @SerializedName("dolby_content")
        var dolbyContent: Int = 0,
        @SerializedName("fav_count")
        var favCount: String = "0",
        @SerializedName("genre")
        var genre: String = "",
        @SerializedName("has_download")
        var hasDownload: Int = 0,
        @SerializedName("has_lyrics")
        var hasLyrics: Int = 0,
        @SerializedName("has_trivia")
        var hasTrivia: Int = 0,
        @SerializedName("has_video")
        var hasVideo: Int = 0,
        @SerializedName("images")
        var images: Images = Images(),
        @SerializedName("intl_content")
        var intlContent: Int = 0,
        @SerializedName("label")
        var label: String = "",
        @SerializedName("language")
        var language: String = "",
        @SerializedName("lrc")
        var lrc: String = "",
        @SerializedName("lyricist")
        var lyricist: String = "",
        @SerializedName("lyrics")
        var lyrics: String = "",
        @SerializedName("mood")
        var mood: String = "",
        @SerializedName("music_director")
        var musicDirector: String = "",
        @SerializedName("plays_count")
        var playsCount: String = "0",
        @SerializedName("releasedate")
        var releasedate: Long = 0,
        @SerializedName("relyear")
        var relyear: Int = 0,
        @SerializedName("singers")
        var singers: String = "",
        @SerializedName("song_tags")
        var songTags: String = "",
        @SerializedName("subgenre_name")
        var subgenreName: String = "",
        @SerializedName("title")
        var title: String = "",
        @SerializedName("typeid")
        var typeid: Int = 0,
        @SerializedName("user_fav")
        var userFav: Int = 0,
        @SerializedName("duration")
        var duration: Long = 0
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Images(
            @SerializedName("image_150x150")
            var image150x150: List<String> = listOf(),
            @SerializedName("image_300x300")
            var image300x300: List<String> = listOf(),
            @SerializedName("image_400x400")
            var image400x400: List<String> = listOf(),
            @SerializedName("image_500x500")
            var image500x500: List<String> = listOf(),
            @SerializedName("image_700x394")
            var image700x394: List<String> = listOf()
        ) : Parcelable

            @Keep
            @Parcelize
            data class Podcast(
                @SerializedName("album_id")
                var albumId: Int? = 0,
                @SerializedName("album_name")
                var albumName: String? = "",
                @SerializedName("artist_name")
                var artistName: String? = "",
                @SerializedName("category")
                var category: String? = "",
                @SerializedName("content_id")
                var contentId: Int? = 0,
                @SerializedName("description")
                var description: String? = "",
                @SerializedName("duration")
                var duration: Long? = 0,
                @SerializedName("fav_count")
                var favCount: String? = "0",
                @SerializedName("image")
                var image: String? = "",
                @SerializedName("label")
                var label: String? = "",
                @SerializedName("language")
                var language: String? = "",
                @SerializedName("plays_count")
                var playsCount: String? = "0",
                @SerializedName("release_year")
                var releaseYear: Int? = 0,
                @SerializedName("releasedate")
                var releasedate: Long? = 0,
                @SerializedName("singers")
                var singers: String? = "",
                @SerializedName("title")
                var title: String? = "",
                @SerializedName("typeid")
                var typeid: Int? = 0,
                @SerializedName("user_fav")
                var userFav: Int? = 0,
                @SerializedName("vendor")
                var vendor: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class Podcastlisting(
                @SerializedName("track")
                var track: List<Podcast?>? = listOf()
            ) : Parcelable
    }
}