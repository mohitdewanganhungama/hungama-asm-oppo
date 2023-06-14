package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class HERespModel(
    @SerializedName("data")
    var `data`: Data?
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("cg_url")
        var cgUrl: String?,
        @SerializedName("country")
        var country: String,
        @SerializedName("currency")
        var currency: String?,
        @SerializedName("dislpay_name")
        var dislpayName: String?,
        @SerializedName("isd_code")
        var isdCode: String,
        @SerializedName("msisdn")
        var msisdn: String,
        @SerializedName("msisdn_with_isd_code")
        var msisdnWithIsdCode: String,
        @SerializedName("plan_id")
        var planId: String?,
        @SerializedName("pricepoint")
        var pricepoint: String?,
        @SerializedName("telco")
        var telco: String?,
        @SerializedName("validity")
        var validity: String?
    ) : Parcelable

    override fun toString(): String {
        return "HERespModel(`data`=$`data`)"
    }
}