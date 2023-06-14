package com.hungama.music.splash

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contents(

	@field:SerializedName("bg_image")
	val bgImage: String? = null,

	@field:SerializedName("fg_image")
	val fgImage: String? = null
) : Parcelable

@Parcelize
data class SplashRespModel(

	@field:SerializedName("data")
	val data: Data? = null
) : Parcelable

@Parcelize
data class RowsItem(

	@field:SerializedName("configuration")
	val configuration: String? = null,

	@field:SerializedName("contents")
	val contents: Contents? = null,

	@field:SerializedName("made_in_india")
	val madeInIndia: Boolean? = null,

	@field:SerializedName("type")
	val type: String? = null
) : Parcelable

@Parcelize
data class Data(

	@field:SerializedName("body")
	val body: Body? = null
) : Parcelable

@Parcelize
data class Body(

	@field:SerializedName("rows")
	val rows: List<RowsItem?>? = null
) : Parcelable
