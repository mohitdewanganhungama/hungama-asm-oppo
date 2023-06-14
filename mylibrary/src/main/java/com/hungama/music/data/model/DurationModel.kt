package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DurationModel(
	var start_fg_time: Long? = 0L,
	var start_bg_time: Long? = 0L
) : Parcelable