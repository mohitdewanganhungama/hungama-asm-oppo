package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MusicModel(

        var title: String? = "",
        var subTitle: String? = "",
        var image: String? = "",
        var url: String? = "",
        var drmlicence:String? = null
) : Parcelable

