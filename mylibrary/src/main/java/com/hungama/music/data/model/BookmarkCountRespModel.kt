package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BookmarkCountRespModel(
    @SerializedName("data")
    var `data`: List<Data> = listOf(),
    @SerializedName("message")
    var message: String = "",
    @SerializedName("statusCode")
    var statusCode: Int = 0,
    @SerializedName("totalCount")
    var totalCount: Int = 0
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("count")
        var count: Int = 0,
        @SerializedName("type")
        var type: Int = 0
    ) : Parcelable
}