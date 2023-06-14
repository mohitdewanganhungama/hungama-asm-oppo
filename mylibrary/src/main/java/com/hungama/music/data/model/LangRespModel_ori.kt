//package com.hungama.music.model
//
//import android.os.Parcelable
//import com.google.gson.annotations.SerializedName
//import kotlinx.android.parcel.Parcelize
//
//@Parcelize
//data class LangRespModel(
//
//	@field:SerializedName("data")
//	val data: Data? = null
//) : Parcelable
//
//@Parcelize
//data class RowsItem(
//
//	@field:SerializedName("configuration")
//	val configuration: String? = null,
//
//	@field:SerializedName("headding")
//	val headding: String? = null,
//
//	@field:SerializedName("type")
//	val type: String? = null,
//
//	@field:SerializedName("transliteration-key")
//	val transliterationKey: String? = null,
//
//	@field:SerializedName("image")
//	val image: String? = null,
//
//	@field:SerializedName("sequence")
//	val sequence: Int? = null,
//
//	@field:SerializedName("code")
//	val code: String? = null,
//
//	var isSelected:Boolean=false,
//
//	@field:SerializedName("subheadding")
//	val subheadding: String? = null
//) : Parcelable
//
//@Parcelize
//data class Subheadding(
//
//	@field:SerializedName("hi")
//	val hi: String? = null,
//
//	@field:SerializedName("pa")
//	val pa: String? = null,
//
//	@field:SerializedName("te")
//	val te: String? = null,
//
//	@field:SerializedName("kn")
//	val kn: String? = null,
//
//	@field:SerializedName("mr")
//	val mr: String? = null,
//
//	@field:SerializedName("en")
//	val en: String? = null,
//
//	@field:SerializedName("bn")
//	val bn: String? = null,
//
//	@field:SerializedName("ta")
//	val ta: String? = null,
//
//	@field:SerializedName("gu")
//	val gu: String? = null,
//
//	@field:SerializedName("ml")
//	val ml: String? = null
//) : Parcelable
//
//@Parcelize
//data class Transliteration(
//
//	@field:SerializedName("next")
//	val next: Next? = null,
//
//	@field:SerializedName("headding")
//	val headding: Headding? = null,
//
//	@field:SerializedName("subheadding")
//	val subheadding: Subheadding? = null
//) : Parcelable
//
//@Parcelize
//data class Body(
//
//	@field:SerializedName("rows")
//	val rows: List<RowsItem?>? = null,
//
//	@field:SerializedName("transliteration")
//	val transliteration: Transliteration? = null
//) : Parcelable
//
//@Parcelize
//data class Next(
//
//	@field:SerializedName("hi")
//	val hi: String? = null,
//
//	@field:SerializedName("pa")
//	val pa: String? = null,
//
//	@field:SerializedName("te")
//	val te: String? = null,
//
//	@field:SerializedName("kn")
//	val kn: String? = null,
//
//	@field:SerializedName("mr")
//	val mr: String? = null,
//
//	@field:SerializedName("en")
//	val en: String? = null,
//
//	@field:SerializedName("bn")
//	val bn: String? = null,
//
//	@field:SerializedName("ta")
//	val ta: String? = null,
//
//	@field:SerializedName("gu")
//	val gu: String? = null,
//
//	@field:SerializedName("ml")
//	val ml: String? = null
//) : Parcelable
//
//@Parcelize
//data class Headding(
//
//	@field:SerializedName("hi")
//	val hi: String? = null,
//
//	@field:SerializedName("pa")
//	val pa: String? = null,
//
//	@field:SerializedName("te")
//	val te: String? = null,
//
//	@field:SerializedName("kn")
//	val kn: String? = null,
//
//	@field:SerializedName("mr")
//	val mr: String? = null,
//
//	@field:SerializedName("en")
//	val en: String? = null,
//
//	@field:SerializedName("bn")
//	val bn: String? = null,
//
//	@field:SerializedName("ta")
//	val ta: String? = null,
//
//	@field:SerializedName("gu")
//	val gu: String? = null,
//
//	@field:SerializedName("ml")
//	val ml: String? = null
//) : Parcelable
//
//@Parcelize
//data class Data(
//
//	@field:SerializedName("body")
//	val body: Body? = null
//) : Parcelable
