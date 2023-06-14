package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class MoreBucketDataModel(
    @SerializedName("data")
    var `data`: Data? = Data()
) {
    @Keep
    data class Data(
        @SerializedName("body")
        var body: Body? = Body()
    ) {
        @Keep
        data class Body(
            @SerializedName("rows")
            var rows: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
        ) {
            @Keep
            data class Row(
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
                        @SerializedName("attribute_censor_rating")
                        var attributeCensorRating: List<Any?>? = listOf(),
                        @SerializedName("fav_count")
                        var favCount: Int? = 0,
                        @SerializedName("lang")
                        var lang: List<String?>? = listOf(),
                        @SerializedName("movierights")
                        var movierights: List<Any?>? = listOf(),
                        @SerializedName("nudity")
                        var nudity: String? = "",
                        @SerializedName("playcount")
                        var playcount: Int? = 0,
                        @SerializedName("rating_critic")
                        var ratingCritic: Int? = 0,
                        @SerializedName("synopsis")
                        var synopsis: String? = ""
                    )
                }
            }
        }
    }
}