package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class GCAddedToPlaylistModel(
    @SerializedName("event_name")
    var eventName: String = "",
    @SerializedName("is_display")
    var isDisplay: Boolean = false,
    @SerializedName("popup_text")
    var popupText: String = "",
    @SerializedName("popup_type")
    var popupType: String = ""
) : Parcelable