package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CommonThreeDotsMenuItemModel(
    @SerializedName("id")
    var id: Int? = 0,
    @SerializedName("isSelected")
    var isSelected: Boolean? = false,
    @SerializedName("title")
    var title: String = "",
    var icon: Int? = 0,
    var detailPageId: Int? = 0
) : Parcelable