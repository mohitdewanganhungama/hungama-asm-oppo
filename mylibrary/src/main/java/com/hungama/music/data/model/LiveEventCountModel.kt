package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class LiveEventCountModel(
    @SerializedName("count")
    var count: Int = 0,
    @SerializedName("key")
    var key: String = "",
    @SerializedName("streamid")
    var streamid: String = ""
) : Parcelable