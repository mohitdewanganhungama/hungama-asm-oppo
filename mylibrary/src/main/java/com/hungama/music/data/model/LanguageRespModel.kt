package com.hungama.music.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hungama.music.data.model.LanguageModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Data1(

	@field:SerializedName("body")
	val bodyData: BodyData? = null,

) : Parcelable

@Parcelize
data class BodyData(

	@field:SerializedName("transliteration")
	val transliterationData: Transliteration? = null,

	@field:SerializedName("rows")
	val listOfLanguage: MutableList<LanguageModel>? = null
) : Parcelable

@Parcelize
data class Transliteration(

	@field:SerializedName("headding")
	val headdingData: HeaddingData? = null,

	@field:SerializedName("subheadding")
	val subheaddingData: SubHeaddingData? = null,

	@field:SerializedName("next")
	val nextData: NextData? = null
) : Parcelable

@Parcelize
data class HeaddingData(

	@field:SerializedName("en")
	val en: String? = "",
	@field:SerializedName("gu")
	val gu: String? = "",
	@field:SerializedName("hi")
	val hi: String? = "",
	@field:SerializedName("kn")
	val kn: String? = "",
	@field:SerializedName("ml")
	val ml: String? = "",
	@field:SerializedName("mr")
	val mr: String? = "",
	@field:SerializedName("pa")
	val pa: String? = "",
	@field:SerializedName("ta")
	val ta: String? = "",
	@field:SerializedName("te")
	val te: String? = "",
	@field:SerializedName("bn")
	val bn: String? = "",

) : Parcelable

@Parcelize
data class SubHeaddingData(

	@field:SerializedName("en")
	val en: String? = "",
	@field:SerializedName("gu")
	val gu: String? = "",
	@field:SerializedName("hi")
	val hi: String? = "",
	@field:SerializedName("kn")
	val kn: String? = "",
	@field:SerializedName("ml")
	val ml: String? = "",
	@field:SerializedName("mr")
	val mr: String? = "",
	@field:SerializedName("pa")
	val pa: String? = "",
	@field:SerializedName("ta")
	val ta: String? = "",
	@field:SerializedName("te")
	val te: String? = "",
	@field:SerializedName("bn")
	val bn: String? = "",

	) : Parcelable

@Parcelize
data class NextData(

	@field:SerializedName("en")
	val en: String? = "",
	@field:SerializedName("gu")
	val gu: String? = "",
	@field:SerializedName("hi")
	val hi: String? = "",
	@field:SerializedName("kn")
	val kn: String? = "",
	@field:SerializedName("ml")
	val ml: String? = "",
	@field:SerializedName("mr")
	val mr: String? = "",
	@field:SerializedName("pa")
	val pa: String? = "",
	@field:SerializedName("ta")
	val ta: String? = "",
	@field:SerializedName("te")
	val te: String? = "",
	@field:SerializedName("bn")
	val bn: String? = "",

	) : Parcelable
@Parcelize
data class LanguageRespModel(

	@field:SerializedName("data")
	val data: Data1? = null,

	@field:SerializedName("message")
	val message: String? = "",

	@field:SerializedName("status")
	val status: Int? = null
) : Parcelable