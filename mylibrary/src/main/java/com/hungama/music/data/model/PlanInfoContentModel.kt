package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
open class PlanInfoContentModel(

    @SerializedName("code") var code: String ="",
    @SerializedName("pageTitle") var pageTitle: String="",
    @SerializedName("pageDescription") var pageDescription: String="",
    @SerializedName("productInfo") var productInfo: ProductInfo? = ProductInfo(),
    @SerializedName("planInfo") var planInfo: PlanInfo? = PlanInfo(),
    @SerializedName("couponInfo") var couponInfo: CouponInfo? = CouponInfo(),
    @SerializedName("payments") var payments: ArrayList<Payments> = arrayListOf(),
    @SerializedName("walletPayments") var walletPayments: ArrayList<WalletPayments> = arrayListOf(),
    @SerializedName("upiPayments") var upiPayments: ArrayList<UpiPayments> = arrayListOf(),
    @SerializedName("netBankingPayments") var netBankingPayments: ArrayList<NetBankingPayments> = arrayListOf(),
    @SerializedName("total_user_coins") var totalUserCoins: Int=0
) : Parcelable {
    @Keep
    @Parcelize
    data class ProductInfo(
        @SerializedName("product_id") var productId: Int= 0,
        @SerializedName("product_code") var productCode: String="",
        @SerializedName("product_name") var productName: String="",
        @SerializedName("product_desc") var productDesc: String="",
        @SerializedName("product_logo") var productLogo: String=""
    ) : Parcelable

    @Keep
    @Parcelize
    data class PlanInfo(
        @SerializedName("plan_dname") var planDname: String="",
        @SerializedName("plan_image") var planImage: String="",
        @SerializedName("plan_image_desktop") var planImageDesktop: String="",
        @SerializedName("plan_currency") var planCurrency: String="",
        @SerializedName("plan_price") var planPrice: Double=0.0,
        @SerializedName("plan_currency_symbol") var planCurrencySymbol: String="",
        @SerializedName("plan_name") var planName: String="",
        @SerializedName("plan_des") var planDes: String="",
        @SerializedName("plan_valid") var planValid: String="",
        @SerializedName("event_time") var eventTime: String=""
    ) : Parcelable

    @Keep
    @Parcelize
    data class CouponInfo(
        @SerializedName("is_coupon") var isCoupon: Boolean= false,
        @SerializedName("coupon_text") var couponText: String=""
    ) : Parcelable

    @Keep
    @Parcelize
    data class Payments(
        @SerializedName("display_order") var displayOrder: Int=0,
        @SerializedName("plan_details_id") var planDetailsId: Int=0,
        @SerializedName("payment_id") var paymentId: Int=0,
        @SerializedName("payment_logo") var paymentLogo: String="",
        @SerializedName("payment_name") var paymentName: String="",
        @SerializedName("offer_text") var offerText: String="",
        @SerializedName("offer_text_detail") var offerTextDetail: String="",
        @SerializedName("plan_group_type") var planGroupType: String="",
        @SerializedName("is_featured") var isFeatured: Int=0
    ) : Parcelable

    @Keep
    @Parcelize
    data class WalletPayments(
        @SerializedName("display_order") var displayOrder: Int=0,
        @SerializedName("plan_details_id") var planDetailsId: Int=0,
        @SerializedName("payment_id") var paymentId: Int=0,
        @SerializedName("payment_logo") var paymentLogo: String="",
        @SerializedName("payment_name") var paymentName: String="",
        @SerializedName("offer_text") var offerText: String=""
    ) : Parcelable

    @Keep
    @Parcelize
    data class UpiPayments(
        @SerializedName("display_order") var displayOrder: Int=0,
        @SerializedName("plan_details_id") var planDetailsId: Int=0,
        @SerializedName("payment_id") var paymentId: Int=0,
        @SerializedName("payment_logo") var paymentLogo: String="",
        @SerializedName("payment_name") var paymentName: String="",
        @SerializedName("offer_text") var offerText: String="",
        @SerializedName("upi_type") var upiType: String=""
    ) : Parcelable

    @Keep
    @Parcelize
    data class FeaturedPayment(
        @SerializedName("product_id") var productId: Int= 0,
        @SerializedName("payment_logo") var productLogo: String="",
        @SerializedName("product_name") var productName: String="",
        @SerializedName("offer_text") var offerText: String=""
    ) : Parcelable
    @Keep
    @Parcelize
    data class NetBankingPayments(

        @SerializedName("display_order") var displayOrder: Int=0,
        @SerializedName("plan_details_id") var planDetailsId: Int=0,
        @SerializedName("payment_id") var paymentId: Int=0,
        @SerializedName("payment_logo") var paymentLogo: String="",
        @SerializedName("payment_name") var paymentName: String="",
        @SerializedName("offer_text") var offerText: String="",
        @SerializedName("offer_text_detail") var offerTextDetail: String="",
        @SerializedName("bank_code") var bankCode: String="",
        @SerializedName("bank_display_name") var bankDisplayName: String=""
    ) : Parcelable


}