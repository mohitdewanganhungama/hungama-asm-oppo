package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ContentsPlanDetailModel(
    @SerializedName("data")
    var `data`: Data? = Data(),
    @SerializedName("message")
    var message: String? = "",
    @SerializedName("messageCode")
    var messageCode: String? = "",
    @SerializedName("statusCode")
    var statusCode: Int? = 0,
    @SerializedName("success")
    var success: Boolean? = false
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("live_concert")
        var liveConcert: List<Plan?>? = listOf(),
        @SerializedName("ptvod")
        var ptvod: List<Plan?>? = listOf(),
        @SerializedName("ptvod150")
        var ptvod150: List<Plan?>? = listOf(),
        @SerializedName("subscription")
        var subscription: List<Plan?>? = listOf(),
        @SerializedName("tvod")
        var tvod: List<Plan?>? = listOf()
    ) : Parcelable {

        @Keep
        @Parcelize
        data class Plan(
            @SerializedName("coins")
            var coins: String? = "",
            @SerializedName("is_banner_price")
            var isBannerPrice: String? = "",
            @SerializedName("is_featured")
            var isFeatured: String? = "",
            @SerializedName("offer_text")
            var offerText: String? = "",
            @SerializedName("offer_text_details")
            var offerTextDetails: String? = "",
            @SerializedName("original_price")
            var originalPrice: String? = "0",
            @SerializedName("payment_id")
            var paymentId: Int? = 0,
            @SerializedName("payment_logo")
            var paymentLogo: String? = "",
            @SerializedName("payment_name")
            var paymentName: String? = "",
            @SerializedName("plan_country")
            var planCountry: String? = "",
            @SerializedName("plan_currency")
            var planCurrency: String? = "",
            @SerializedName("plan_detail_name")
            var planDetailName: String? = "",
            @SerializedName("plan_details")
            var planDetails: String? = "",
            @SerializedName("plan_details_id")
            var planDetailsId: Int? = 0,
            @SerializedName("plan_duration")
            var planDuration: String? = "",
            @SerializedName("plan_id")
            var planId: Int? = 0,
            @SerializedName("plan_price")
            var planPrice: String = "-1",
            @SerializedName("plan_type")
            var planType: String? = "",
            @SerializedName("platform_id")
            var platformId: Int? = 0,
            @SerializedName("product_id")
            var productId: Int? = 0,
            @SerializedName("provider_id")
            var providerId: String? = "",
            @SerializedName("store_payment_id")
            var storePaymentId: String? = ""
        ) : Parcelable
    }
}