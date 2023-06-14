package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DeleteRecentSearchModel(
    @SerializedName("data")
    var `data`: Data?
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("error")
        var error: Boolean?,
        @SerializedName("reply")
        var reply: Int?
    ) : Parcelable
}