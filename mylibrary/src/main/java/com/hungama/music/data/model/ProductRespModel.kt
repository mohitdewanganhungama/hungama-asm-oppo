package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ProductRespModel(
    @SerializedName("products")
    var products: List<Product?>? = listOf(),
    @SerializedName("product")
    var product: Product = Product()
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
        @SerializedName("variants")
        var variants: List<Variant?>? = listOf(),
        @SerializedName("vendor")
        var vendor: String? = ""
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
            var productId: Long? = 0,
            @SerializedName("values")
            var values: List<String?>? = listOf()
        ) : Parcelable

        @Keep
        @Parcelize
        data class Variant(
            @SerializedName("admin_graphql_api_id")
            var adminGraphqlApiId: String? = "",
            @SerializedName("barcode")
            var barcode: String? = "",
            @SerializedName("compare_at_price")
            var compareAtPrice: String? = "",
            @SerializedName("created_at")
            var createdAt: String? = "",
            @SerializedName("fulfillment_service")
            var fulfillmentService: String? = "",
            @SerializedName("grams")
            var grams: Int? = 0,
            @SerializedName("id")
            var id: Long? = 0,
            @SerializedName("image_id")
            var imageId: Long? = 0,
            @SerializedName("inventory_item_id")
            var inventoryItemId: Long? = 0,
            @SerializedName("inventory_management")
            var inventoryManagement: String? = "",
            @SerializedName("inventory_policy")
            var inventoryPolicy: String? = "",
            @SerializedName("inventory_quantity")
            var inventoryQuantity: Int? = 0,
            @SerializedName("old_inventory_quantity")
            var oldInventoryQuantity: Int? = 0,
            @SerializedName("option1")
            var option1: String? = "",
            @SerializedName("option2")
            var option2: String? = "",
            @SerializedName("option3")
            var option3: String? = "",
            @SerializedName("position")
            var position: Int? = 0,
            @SerializedName("price")
            var price: String? = "",
            @SerializedName("product_id")
            var productId: Long? = 0,
            @SerializedName("requires_shipping")
            var requiresShipping: Boolean? = false,
            @SerializedName("sku")
            var sku: String? = "",
            @SerializedName("taxable")
            var taxable: Boolean? = false,
            @SerializedName("title")
            var title: String? = "",
            @SerializedName("updated_at")
            var updatedAt: String? = "",
            @SerializedName("weight")
            var weight: Double? = 0.0,
            @SerializedName("weight_unit")
            var weightUnit: String? = ""
        ) : Parcelable
    }
}