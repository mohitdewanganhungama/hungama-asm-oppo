package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaObject (
    var id:Int = 0,
    var title: String? = null,
    var url: String? = null,
    var coverUrl: String? = null,
    var userHandle: String? = null,
): Parcelable