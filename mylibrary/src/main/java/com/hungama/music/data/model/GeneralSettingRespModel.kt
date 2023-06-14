package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GeneralSettingRespModel(

	@field:SerializedName("data")
	val data: GeneralSettingData? = null
) : Parcelable

@Parcelize
data class GeneralSettingData(

	@field:SerializedName("data")
	val data: List<GeneralDataItem?>? = null
) : Parcelable

@Parcelize
data class GeneralSettingPreferenceItem(

	@field:SerializedName("allowExplicitContent")
	val allowExplicitContent: Boolean? = null,

	@field:SerializedName("appLanguage")
	val appLanguage: String? = null,

	@field:SerializedName("mobileNotification")
	val mobileNotification: Boolean? = null,

	@field:SerializedName("emailNotification")
	val emailNotification: Boolean? = null
) : Parcelable

@Parcelize
data class GeneralDataItem(

    @field:SerializedName("uid")
	val uid: String? = null,

    @field:SerializedName("preference")
	val preference: List<GeneralSettingPreferenceItem?>? = null,

    @field:SerializedName("__v")
	val V: Int? = null,

    @field:SerializedName("_id")
	val id: String? = null,

    @field:SerializedName("type")
	val type: String? = null
) : Parcelable
