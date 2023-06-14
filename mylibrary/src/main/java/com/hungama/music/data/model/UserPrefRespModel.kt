package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserPrefRespModel(

    @field:SerializedName("data")
	val data: UserPrefData? = null,

    @field:SerializedName("message")
	val message: String? = null
) : Parcelable

@Parcelize
data class UserPrefData(

	@field:SerializedName("nModified")
	val nModified: Int? = null,

	@field:SerializedName("ok")
	val ok: Int? = null
) : Parcelable
