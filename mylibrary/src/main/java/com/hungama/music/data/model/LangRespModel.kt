package com.hungama.music.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LangRespModel(

	@field:SerializedName("data")
	val data: Data? = null
) : Parcelable

@Parcelize
data class Transliteration(

	@field:SerializedName("next")
	val next: Next? = null,

	@field:SerializedName("headding")
	val headding: Headding? = null,

	@field:SerializedName("subheadding")
	val subheadding: Subheadding? = null
) : Parcelable

@Parcelize
data class LangItem(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("title")
	val title: String? = "",

	@field:SerializedName("sequence")
	val sequence: Int? = null,

	@field:SerializedName("code")
	val code: String? = null,

	var isSelected:Boolean=false,

	@field:SerializedName("configuration")
	val configuration: String? = null,

	@field:SerializedName("itype")
	val itype: Int? = null
) : Parcelable


@Parcelize
data class Next(

	@field:SerializedName("hi")
	val hi: String? = null,

	@field:SerializedName("pa")
	val pa: String? = null,

	@field:SerializedName("te")
	val te: String? = null,

	@field:SerializedName("kn")
	val kn: String? = null,

	@field:SerializedName("mr")
	val mr: String? = null,

	@field:SerializedName("en")
	val en: String? = null,

	@field:SerializedName("bn")
	val bn: String? = null,

	@field:SerializedName("ta")
	val ta: String? = null,

	@field:SerializedName("gu")
	val gu: String? = null,

	@field:SerializedName("ml")
	val ml: String? = null
) : Parcelable

@Parcelize
data class Body(

	@field:SerializedName("rows")
	val rows: List<RowsItem?>? = null,

	@field:SerializedName("transliteration")
	val transliteration: Transliteration? = null
) : Parcelable

@Parcelize
data class Subheadding(

	@field:SerializedName("hi")
	val hi: String? = null,

	@field:SerializedName("pa")
	val pa: String? = null,

	@field:SerializedName("te")
	val te: String? = null,

	@field:SerializedName("kn")
	val kn: String? = null,

	@field:SerializedName("mr")
	val mr: String? = null,

	@field:SerializedName("en")
	val en: String? = null,

	@field:SerializedName("bn")
	val bn: String? = null,

	@field:SerializedName("ta")
	val ta: String? = null,

	@field:SerializedName("gu")
	val gu: String? = null,

	@field:SerializedName("ml")
	val ml: String? = null
) : Parcelable

@Parcelize
data class Data(

	@field:SerializedName("body")
	val body: Body? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("imageFileSubTypeId")
	val imageFileSubTypeId: Int? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("type")
	val type: Int? = null
) : Parcelable

@Parcelize
data class Headding(

	@field:SerializedName("hi")
	val hi: String? = null,

	@field:SerializedName("pa")
	val pa: String? = null,

	@field:SerializedName("te")
	val te: String? = null,

	@field:SerializedName("kn")
	val kn: String? = null,

	@field:SerializedName("mr")
	val mr: String? = null,

	@field:SerializedName("en")
	val en: String? = null,

	@field:SerializedName("bn")
	val bn: String? = null,

	@field:SerializedName("ta")
	val ta: String? = null,

	@field:SerializedName("gu")
	val gu: String? = null,

	@field:SerializedName("ml")
	val ml: String? = null
) : Parcelable

@Parcelize
data class RowsItem(

	@field:SerializedName("more")
	val more: Int? = null,

	@field:SerializedName("hscroll")
	val hscroll: Int? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("numrow")
	val numrow: Int? = null,

	@field:SerializedName("items")
	val items: List<LangItem?>? = null,

	@field:SerializedName("heading")
	val heading: String? = null,

	@field:SerializedName("subheadding")
	val subheadding: String? = null,

	@field:SerializedName("type")
	val type: Int? = null
) : Parcelable
