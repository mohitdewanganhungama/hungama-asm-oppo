package com.hungama.music.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Data(

	@field:SerializedName("contents")
	val contents: Contents? = null,

	@field:SerializedName("made_in_india")
	val madeInIndia: Boolean? = null,

	@field:SerializedName("type")
	val type: String? = null
) : Parcelable

@Parcelize
data class Contents(

	@field:SerializedName("bg_image")
	val bgImage: String? = "",

	@field:SerializedName("fg_image")
	val fgImage: String? = null
) : Parcelable

@Parcelize
data class EnterMobileRespModel(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = "",

	@field:SerializedName("status")
	val status: Int? = null
) : Parcelable