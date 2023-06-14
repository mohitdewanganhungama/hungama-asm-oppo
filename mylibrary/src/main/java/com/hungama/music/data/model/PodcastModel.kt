package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PodcastModel(
    @SerializedName("body")
    var body: Body? = Body()
) : Parcelable {
    @Keep
    @Parcelize
    data class Body(
        @SerializedName("artist")
        var artist: List<String?>? = listOf(),
        @SerializedName("data")
        var `data`: Data? = Data(),
        @SerializedName("description")
        var description: String? = "",
        @SerializedName("genre")
        var genre: List<String?>? = listOf(),
        @SerializedName("id")
        var id: String? = "",
        @SerializedName("image")
        var image: String? = "",
        @SerializedName("imageFileSubTypeId")
        var imageFileSubTypeId: Int? = 0,
        @SerializedName("items")
        var items: List<Item?>? = listOf(),
        @SerializedName("itype")
        var itype: Int? = 0,
        @SerializedName("language")
        var language: List<String?>? = listOf(),
        @SerializedName("releasedate")
        var releasedate: String? = "",
        @SerializedName("similar")
        var similar: List<Similar?>? = listOf(),
        @SerializedName("title")
        var title: String? = ""
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Data(
            @SerializedName("misc")
            var misc: Misc? = Misc()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Misc(
                @SerializedName("fav_count")
                var favCount: String? = "",
                @SerializedName("playcount")
                var playcount: String? = ""
            ) : Parcelable
        }

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
                @SerializedName("artist")
                var artist: List<String?>? = listOf(),
                @SerializedName("description")
                var description: String? = "",
                @SerializedName("genre")
                var genre: List<String?>? = listOf(),
                @SerializedName("id")
                var id: String? = "",
                @SerializedName("image")
                var image: String? = "",
                @SerializedName("imageFileSubTypeId")
                var imageFileSubTypeId: Int? = 0,
                @SerializedName("language")
                var language: List<String?>? = listOf(),
                @SerializedName("misc")
                var misc: Misc? = Misc(),
                @SerializedName("releasedate")
                var releasedate: String? = "",
                @SerializedName("title")
                var title: String? = ""
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Misc(
                    @SerializedName("duration")
                    var duration: String? = ""
                ) : Parcelable
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
                @SerializedName("artist")
                var artist: List<String?>? = listOf(),
                @SerializedName("description")
                var description: String? = "",
                @SerializedName("genre")
                var genre: List<String?>? = listOf(),
                @SerializedName("id")
                var id: String? = "",
                @SerializedName("image")
                var image: String? = "",
                @SerializedName("imageFileSubTypeId")
                var imageFileSubTypeId: Int? = 0,
                @SerializedName("language")
                var language: List<String?>? = listOf(),
                @SerializedName("misc")
                var misc: Misc? = Misc(),
                @SerializedName("releasedate")
                var releasedate: String? = "",
                @SerializedName("title")
                var title: String? = ""
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Misc(
                    @SerializedName("playcount")
                    var playcount: String? = ""
                ) : Parcelable
            }
        }
    }
}