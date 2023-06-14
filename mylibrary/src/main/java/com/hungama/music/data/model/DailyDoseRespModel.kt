package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DailyDoseRespModel(
    @SerializedName("body")
    var body: Body = Body(),
    @SerializedName("head")
    var head: Head = Head()
) : Parcelable {
    @Keep
    @Parcelize
    data class Body(
        @SerializedName("rows")
        var rows: ArrayList<RowsItem> = ArrayList()

    ) : Parcelable {

    }

    @Keep
    @Parcelize
    class Head : Parcelable
}