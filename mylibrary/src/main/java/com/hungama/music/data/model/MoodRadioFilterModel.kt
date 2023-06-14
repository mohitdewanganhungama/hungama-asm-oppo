package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MoodRadioFilterModel
    (
    @SerializedName("data")
    var `data`: Data? = Data()
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("body")
        var body: Body? = Body()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Body(
            @SerializedName("rows")
            var rows: List<Row?>? = listOf()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Row(
                @SerializedName("configuration")
                var configuration: String? = "",
                @SerializedName("id")
                var id: String? = "",
                @SerializedName("items")
                var items: List<Item?>? = listOf()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Item(
                    @SerializedName("code")
                    var code: String? = "",
                    @SerializedName("moodid")
                    var moodid: Int? = 0,
                    @SerializedName("tempoid")
                    var tempoid: Int? = 0,
                    @SerializedName("title")
                    var title: String? = "",
                    var isSelected:Boolean = false
                ) : Parcelable
            }
        }
    }
}