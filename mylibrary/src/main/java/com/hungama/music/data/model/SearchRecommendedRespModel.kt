package com.hungama.music.search.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchRecommendedRespModel(

	@field:SerializedName("body")
	val body: Body? = null
) : Parcelable

@Parcelize
data class SearchData(

	@field:SerializedName("image")
	val image: String? = "",

	@field:SerializedName("id")
	val id: String? = "",

	@field:SerializedName("title")
	val title: String? = "",

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("misc")
	val misc: Misc? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("subtitle")
	val subtitle: String? = null
) : Parcelable

@Parcelize
data class Misc(

	@field:SerializedName("header_image")
	val headerImage: String? = null,

	@field:SerializedName("brand_type")
	val brandType: String? = null,

	@field:SerializedName("full_page_image")
	val fullPageImage: String? = null,

	@field:SerializedName("store_id")
	val storeId: String? = null,

	@field:SerializedName("url_np")
	val urlNp: String? = null,

	@field:SerializedName("moviecount")
	val moviecount: Int? = null,

	@field:SerializedName("live_url")
	val liveUrl: String? = null,

	@field:SerializedName("lang")
	val lang: String? = null,

	@field:SerializedName("image_500")
	val image500: String? = null,

	@field:SerializedName("territory_rights")
	val territoryRights: String? = null,

	@field:SerializedName("certificate")
	val certificate: String? = null,

	@field:SerializedName("lang_id")
	val langId: String? = null,

	@field:SerializedName("releasedate")
	val releasedate: String? = null,

	@field:SerializedName("priority")
	val priority: String? = null,

	@field:SerializedName("genre_id")
	val genreId: Int? = null,

	@field:SerializedName("duration")
	val duration: Int? = null,

	@field:SerializedName("fav_count")
	val favCount: Int? = null,

	@field:SerializedName("playcount")
	val playcount: Int? = null,

	@field:SerializedName("vendor_id")
	val vendorId: Int? = null,

	@field:SerializedName("genre")
	val genre: String? = null,

	@field:SerializedName("ctag")
	val ctag: List<String?>? = null,

	@field:SerializedName("explicit_i")
	val explicitI: Int? = null,

	@field:SerializedName("propertyid")
	val propertyid: Int? = null,

	@field:SerializedName("section_id")
	val sectionId: String? = null,

	@field:SerializedName("typeid")
	val typeid: String? = null,

	@field:SerializedName("api")
	val api: String? = null,

	@field:SerializedName("p_name")
	val pName: String? = null,

	@field:SerializedName("p_id")
	val pId: Int? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("movierights")
	val movierights: List<String?>? = null,

	@field:SerializedName("content_text")
	val contentText: String? = null,

	@field:SerializedName("raw")
	val raw: String? = null,

	@field:SerializedName("content_title")
	val contentTitle: String? = null
) : Parcelable

@Parcelize
data class Body(

	@field:SerializedName("rows")
	val rows: List<SearchRowItem?>? = null
) : Parcelable

@Parcelize
data class SearchRowItem(

		@field:SerializedName("data")
	val data: SearchData? = null,

		@field:SerializedName("itype")
	val itype: Int? = null
) : Parcelable
