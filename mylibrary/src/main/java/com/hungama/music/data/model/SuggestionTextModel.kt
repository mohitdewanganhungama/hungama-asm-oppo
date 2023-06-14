package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class SuggestionTextModel(
    @SerializedName("search")
    var search: String,
    @SerializedName("suggestion")
    var suggestion: String
)