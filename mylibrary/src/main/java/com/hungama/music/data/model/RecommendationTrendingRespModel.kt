package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Keep
@Parcelize
data class RecommendationTrendingRespModel(
    @SerializedName("body")
    var body: Body?,
    @SerializedName("head")
    var head: Head?
) : Parcelable {
    @Keep
    @Parcelize
    data class Body(
        @SerializedName("rows")
        var rows: List<Row?>?
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Row(
            @SerializedName("artwork")
            var artwork: Artwork?,
            @SerializedName("bucketQuery")
            var bucketQuery: String?,
            @SerializedName("bucketQueryPage1")
            var bucketQueryPage1: String?,
            @SerializedName("continueImage")
            var continueImage: List<String>?,
            @SerializedName("continueStatus")
            var continueStatus: Boolean?,
            @SerializedName("heading")
            var heading: String?,
            @SerializedName("headings")
            var headings: Headings?,
            @SerializedName("hscroll")
            var hscroll: Int?,
            @SerializedName("id")
            var id: String?,
            @SerializedName("items")
            var items: ArrayList<RecommendedSongListRespModel.Data.Body.Similar>?,
            @SerializedName("keywords")
            var keywords: List<String>?,
            @SerializedName("misc")
            var misc: Misc?,
            @SerializedName("more")
            var more: Int?,
            @SerializedName("moreQuery")
            var moreQuery: String?,
            @SerializedName("numrow")
            var numrow: Int?,
            @SerializedName("sequence")
            var sequence: String?,
            @SerializedName("size")
            var size: Int?,
            @SerializedName("stencil")
            var stencil: String?,
            @SerializedName("type")
            var type: Int?,
            @SerializedName("variant")
            var variant: String?
        ) : Parcelable {
            @Keep
            @Parcelize
            class Artwork : Parcelable

            @Keep
            @Parcelize
            data class Headings(
                @SerializedName("bn")
                var bn: String?,
                @SerializedName("en")
                var en: String?,
                @SerializedName("gu")
                var gu: String?,
                @SerializedName("hi")
                var hi: String?,
                @SerializedName("kn")
                var kn: String?,
                @SerializedName("ml")
                var ml: String?,
                @SerializedName("mr")
                var mr: String?,
                @SerializedName("or")
                var or: String?,
                @SerializedName("pa")
                var pa: String?,
                @SerializedName("ta")
                var ta: String?,
                @SerializedName("te")
                var te: String?,
                @SerializedName("ur")
                var ur: String?
            ) : Parcelable

        }
    }

    @Keep
    @Parcelize
    class Head : Parcelable
}