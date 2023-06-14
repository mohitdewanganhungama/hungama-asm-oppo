package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MorningRadioModel(

    var title: String? = "",
    var subTitle: String? = "",
    var image1: String? = null
) : Parcelable
