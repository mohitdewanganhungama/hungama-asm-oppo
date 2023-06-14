package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PickSongModel(

    var title1: String? = "",
    var subtitle1: String? = "",
    var image1: String? = "",
    var title2: String? = "",
    var subtitle2: String? = "",
    var image2: String? = "",
    var title3: String? = "",
    var subtitle3: String? = "",
    var image3: String? = "",
    var title4: String? = "",
    var subtitle4: String? = "",
    var image4: String? = "",
    var title5: String? = "",
    var subtitle5: String? = "",
    var image5: String? = null
) : Parcelable
