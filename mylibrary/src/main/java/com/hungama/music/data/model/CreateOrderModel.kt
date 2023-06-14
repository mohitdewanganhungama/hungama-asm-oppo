package com.hungama.music.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CreateOrderModel(
    @SerializedName("status")
    var status: Boolean = false,
    @SerializedName("message")
    var message: String = "",
    @SerializedName("order_id")
    var orderId: Long = 0,
    @SerializedName("errors")
    var errors: Errors = Errors()
) : Parcelable

@Keep
@Parcelize
data class Errors(
    @SerializedName("customer")
    var customer: String = ""
) : Parcelable