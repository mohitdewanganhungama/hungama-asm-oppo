package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserStoryReadPostModel(
    @SerializedName("message")
    var message: String = ""
) : Parcelable