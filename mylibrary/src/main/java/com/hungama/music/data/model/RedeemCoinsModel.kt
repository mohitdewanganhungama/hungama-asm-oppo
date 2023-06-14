package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class RedeemCoinsModel(
    @SerializedName("code")
    var code: Int = 0,
    @SerializedName("message")
    var message: String = "",
    @SerializedName("status")
    var status: String = ""
) : Parcelable