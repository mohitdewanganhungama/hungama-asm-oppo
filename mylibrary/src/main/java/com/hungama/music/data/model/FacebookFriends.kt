package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class FacebookFriends(
    @SerializedName("data")
    var `data`: List<Data?>? = listOf(),
    @SerializedName("paging")
    var paging: Paging? = Paging(),
    @SerializedName("summary")
    var summary: Summary? = Summary()
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("id")
        var id: String? = "",
        @SerializedName("name")
        var name: String? = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class Paging(
        @SerializedName("cursors")
        var cursors: Cursors? = Cursors()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Cursors(
            @SerializedName("after")
            var after: String? = "",
            @SerializedName("before")
            var before: String? = ""
        ) : Parcelable
    }

    @Keep
    @Parcelize
    data class Summary(
        @SerializedName("total_count")
        var totalCount: Int? = 0
    ) : Parcelable
}