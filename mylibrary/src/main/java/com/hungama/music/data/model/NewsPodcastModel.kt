package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NewsPodcastModel(

    var title1: String? = "",
    var subtitle1: String? = "",
    var image1: String? = "",
    var title2: String? = "",
    var subtitle2: String? = "",
    var image2: String? = "",
) : Parcelable
