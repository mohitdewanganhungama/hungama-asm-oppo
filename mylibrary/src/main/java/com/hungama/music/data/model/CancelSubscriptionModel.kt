package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CancelSubscriptionModel(
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
        @SerializedName("response")
        var response: Response? = Response()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Response(
            @SerializedName("response")
            var response: String? = ""
        ) : Parcelable
    }
}