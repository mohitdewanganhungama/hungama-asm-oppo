package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class GCEventModel(
    @SerializedName("event_name")
    var eventName: String = "",
    @SerializedName("is_display")
    var isDisplay: Boolean = false,
    @SerializedName("popup_text")
    var popupText: String = "",
    @Expose
    var addedCoin: Int = 0,
    @Expose
    var totalCoin: Int = 0,
    @SerializedName("popup_type")
    var popupType: String = ""
) : Parcelable