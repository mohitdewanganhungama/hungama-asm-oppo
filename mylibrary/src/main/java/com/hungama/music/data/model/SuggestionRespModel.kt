package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SuggestionRespModel(
    @SerializedName("data")
    var `data`: List<Data?>?
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("name")
        var name: String?
    ) : Parcelable
}