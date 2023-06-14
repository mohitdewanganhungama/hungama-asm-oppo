package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ProductForRedeemRespModel(
    @SerializedName("products")
    var products: List<Product?>? = listOf()
) : Parcelable {
    @Keep
    @Parcelize
    data class Product(
        @SerializedName("admin_graphql_api_id")
        var adminGraphqlApiId: String? = "",
        @SerializedName("body_html")
        var bodyHtml: String? = "",
        @SerializedName("created_at")
        var createdAt: String? = "",
        @SerializedName("handle")
        var handle: String? = "",
        @SerializedName("id")
        var id: Long? = 0,
        @SerializedName("image")
        var image: Image? = Image(),
        @SerializedName("images")
        var images: List<Images?>? = listOf(),
        @SerializedName("options")
        var options: List<Option?>? = listOf(),
        @SerializedName("product_type")
        var productType: String? = "",
        @SerializedName("published_at")
        var publishedAt: String? = "",
        @SerializedName("published_scope")
        var publishedScope: String? = "",
        @SerializedName("status")
        var status: String? = "",
        @SerializedName("tags")
        var tags: String? = "",
        @SerializedName("template_suffix")
        var templateSuffix: String? = "",
        @SerializedName("title")
        var title: String? = "",
        @SerializedName("updated_at")
        var updatedAt: String? = "",
        @SerializedName("vendor")
        var vendor: String? = "",
        var price: String? = ""
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Images(
            @SerializedName("admin_graphql_api_id")
            var adminGraphqlApiId: String? = "",
            @SerializedName("alt")
            var alt: String? = "",
            @SerializedName("created_at")
            var createdAt: String? = "",
            @SerializedName("height")
            var height: Int? = 0,
            @SerializedName("id")
            var id: Long? = 0,
            @SerializedName("position")
            var position: Int? = 0,
            @SerializedName("product_id")
            var productId: Long? = 0,
            @SerializedName("src")
            var src: String? = "",
            @SerializedName("updated_at")
            var updatedAt: String? = "",
            @SerializedName("variant_ids")
            var variantIds: List<Long?>? = listOf(),
            @SerializedName("width")
            var width: Int? = 0
        ) : Parcelable

        @Keep
        @Parcelize
        data class Image(
            @SerializedName("admin_graphql_api_id")
            var adminGraphqlApiId: String? = "",
            @SerializedName("alt")
            var alt: String? = "",
            @SerializedName("created_at")
            var createdAt: String? = "",
            @SerializedName("height")
            var height: Int? = 0,
            @SerializedName("id")
            var id: Long? = 0,
            @SerializedName("position")
            var position: Int? = 0,
            @SerializedName("product_id")
            var productId: Long? = 0,
            @SerializedName("src")
            var src: String? = "",
            @SerializedName("updated_at")
            var updatedAt: String? = "",
            @SerializedName("variant_ids")
            var variantIds: List<Long?>? = listOf(),
            @SerializedName("width")
            var width: Int? = 0
        ) : Parcelable

        @Keep
        @Parcelize
        data class Option(
            @SerializedName("id")
            var id: Long? = 0,
            @SerializedName("name")
            var name: String? = "",
            @SerializedName("position")
            var position: Int? = 0,
            @SerializedName("product_id")
            var productId: Long? = 0
        ) : Parcelable
    }
}