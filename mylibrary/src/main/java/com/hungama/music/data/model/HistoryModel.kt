package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HistoryModel(

    var title1: String? = "",
    var image1: String? = "",
    var title2: String? = "",
    var image2: String? = "",
    var title3: String? = "",
    var image3: String? = null
) : Parcelable
