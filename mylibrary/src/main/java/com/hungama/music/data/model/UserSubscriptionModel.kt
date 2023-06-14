package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserSubscriptionModel(
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
        @SerializedName("auto_renewal")
        var autoRenewal: AutoRenewal? = AutoRenewal(),
        @SerializedName("live_concert")
        var liveConcert: List<LiveConcert?>? = listOf(),
        @SerializedName("subscriptions")
        var subscription: Subscription? = Subscription(),
        @SerializedName("tvod")
        var tvod: List<Tvod?>? = listOf(),
        @SerializedName("user")
        var user: User? = User(),
        @SerializedName("status")
        var status: Boolean = true,
        @SerializedName("code")
        var code: String? = "",
        @SerializedName("msg")
        var msg: String? = "",
        @SerializedName("Error_Description")
        var Error_Description: String? = "",
        @SerializedName("is_free_trial_eligible")
        var Is_Free_Trial_Eligible: Boolean = false,
        @SerializedName("profile_app_config")
        var profile_app_config: ProfileAppConfig? = ProfileAppConfig()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class AutoRenewal(
            @SerializedName("order_id")
            var orderId: String? = "",
            @SerializedName("status")
            var status: String? = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class Subscription(
            @SerializedName("currency")
            var currency: String? = "",
            @SerializedName("days_remaining")
            var daysRemaining: Int? = 0,
            @SerializedName("order_id")
            var orderId: String? = "",
            @SerializedName("payment_source")
            var paymentSource: String? = "",
            @SerializedName("payment_source_details")
            var paymentSourceDetails: String? = "",
            @SerializedName("plan_benefits")
            var planBenefits: String? = "",
            @SerializedName("plan_details")
            var planDetails: String? = "",
            @SerializedName("plan_details_id")
            var planDetailsId: Int? = 0,
            @SerializedName("plan_id")
            var planId: Int? = 0,
            @SerializedName("plan_name")
            var planName: String? = "",
            @SerializedName("plan_validity_name")
            var planValidityName: String? = "",
            @SerializedName("plan_price")
            var planPrice: String? = "",
            @SerializedName("plan_title")
            var planTitle: String? = "",
            @SerializedName("plan_type")
            var planType: String? = "",
            @SerializedName("show_ads")
            var showAds: String? = "",
            @SerializedName("subscription_end_date")
            var subscriptionEndDate: String? = "",
            @SerializedName("subscription_start_date")
            var subscriptionStartDate: String? = "",
            @SerializedName("subscription_status")
            var subscriptionStatus: Int? = 0,
            @SerializedName("total_days")
            var totalDays: Int? = 0,
            @SerializedName("trial_expiry_days_left")
            var trialExpiryDaysLeft: Int? = 0,
            @SerializedName("trial_taken")
            var trialTaken: Int? = 0,
            @SerializedName("unsub_button")
            var unsubButton: Int? = 0,
            @SerializedName("coupon_code")
            var couponCode: String = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class Tvod(
            @SerializedName("content_id")
            var contentId: String? = "",
            @SerializedName("currency")
            var currency: String? = "",
            @SerializedName("expiry_date")
            var expiryDate: String? = "",
            @SerializedName("order_id")
            var orderId: String? = "",
            @SerializedName("payment_source")
            var paymentSource: String? = "",
            @SerializedName("payment_source_details")
            var paymentSourceDetails: String? = "",
            @SerializedName("plan_details_id")
            var planDetailsId: Int? = 0,
            @SerializedName("plan_name")
            var planName: String? = "",
            @SerializedName("plan_price")
            var planPrice: String? = "",
            @SerializedName("rental_status")
            var rentalStatus: String? = "",
            @SerializedName("start_date")
            var startDate: String? = "",
            @SerializedName("validity")
            var validity: Int? = 0,
            @SerializedName("coupon_code")
            var couponCode: String = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class LiveConcert(
            @SerializedName("content_id")
            var contentId: String? = "",
            @SerializedName("currency")
            var currency: String? = "",
            @SerializedName("expiry_date")
            var expiryDate: String? = "",
            @SerializedName("order_id")
            var orderId: String? = "",
            @SerializedName("payment_source")
            var paymentSource: String? = "",
            @SerializedName("payment_source_details")
            var paymentSourceDetails: String? = "",
            @SerializedName("plan_details_id")
            var planDetailsId: Int? = 0,
            @SerializedName("plan_id")
            var planId: Int? = 0,
            @SerializedName("plan_name")
            var planName: String? = "",
            @SerializedName("plan_price")
            var planPrice: String? = "",
            @SerializedName("rental_status")
            var rentalStatus: String? = "",
            @SerializedName("start_date")
            var startDate: String? = "",
            @SerializedName("validity")
            var validity: String? = "",
            @SerializedName("coupon_code")
            var couponCode: String = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class User(
            @SerializedName("firstname")
            var firstname: String? = "",
            @SerializedName("identity")
            var identity: String? = "",
            @SerializedName("lastname")
            var lastname: String? = "",
            @SerializedName("profile_image")
            var profileImage: String? = "",
            @SerializedName("user_membership_type")
            var userMembershipType: String? = "",
            @SerializedName("user_membership_type_id")
            var userMembershipTypeId: String? = "",
            @SerializedName("username")
            var username: String? = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class ProfileAppConfig(
            @SerializedName("profile_type")
            var profile_type: String? = "",
            @SerializedName("profile_type_id")
            var profileTypeId: String? = "",
            @SerializedName("upgradable")
            var upgradable: Int? = 0,
            @SerializedName("download_limit")
            var download_limit: Int = 0,
            @SerializedName("download_restricted")
            var download_restricted: Int? = 0,
            @SerializedName("enable_ad")
            var enable_ad: Int? = 0,
            @SerializedName("hd_quality")
            var hd_quality: Int? = 0,
            @SerializedName("premium_video")
            var premium_video: Int? = 0,
            @SerializedName("profile_status")
            var profile_status: String? = ""
        ) : Parcelable
    }
}
