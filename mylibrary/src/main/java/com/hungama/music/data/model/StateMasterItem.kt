package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LangMasterItem(

	var langId: Int? = null,
	var langCode: String? = null,
	var langName: String? = null
) : Parcelable