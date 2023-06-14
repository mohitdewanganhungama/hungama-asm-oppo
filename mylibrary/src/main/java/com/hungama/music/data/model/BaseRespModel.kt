package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BaseRespModel(

    @field:SerializedName("result")
	val result: Boolean? = false,

    @field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("error")
	val error: String? = null,

	@field:SerializedName("message")
	val message: String? = null
) : Parcelable

