package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ProductCategoryRespModel(
    @SerializedName("smart_collections")
    var smartCollections: List<SmartCollection?>? = listOf()
) : Parcelable {
    @Keep
    @Parcelize
    data class SmartCollection(
        @SerializedName("admin_graphql_api_id")
        var adminGraphqlApiId: String? = "",
        @SerializedName("body_html")
        var bodyHtml: String? = "",
        @SerializedName("disjunctive")
        var disjunctive: Boolean? = false,
        @SerializedName("handle")
        var handle: String? = "",
        @SerializedName("id")
        var id: Long? = 0,
        @SerializedName("image")
        var image: Image = Image(),
        @SerializedName("published_at")
        var publishedAt: String? = "",
        @SerializedName("published_scope")
        var publishedScope: String? = "",
        @SerializedName("rules")
        var rules: List<Rule?>? = listOf(),
        @SerializedName("sort_order")
        var sortOrder: String? = "",
        @SerializedName("template_suffix")
        var templateSuffix: String? = "",
        @SerializedName("title")
        var title: String? = "",
        @SerializedName("updated_at")
        var updatedAt: String? = ""
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Image(
            @SerializedName("alt")
            var alt: String = "",
            @SerializedName("created_at")
            var createdAt: String = "",
            @SerializedName("height")
            var height: Int = 0,
            @SerializedName("src")
            var src: String = "",
            @SerializedName("width")
            var width: Int = 0
        ) : Parcelable

        @Keep
        @Parcelize
        data class Rule(
            @SerializedName("column")
            var column: String? = "",
            @SerializedName("condition")
            var condition: String? = "",
            @SerializedName("relation")
            var relation: String? = ""
        ) : Parcelable
    }
}