package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LanguageModel(
  var sequence: Int,
  var code: String,
  var image: String,
  var isSelected: Boolean = false
) : Parcelable
