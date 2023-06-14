package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

class CountryDataModel : ArrayList<CountryDataModel.CountryDataModelItem>(){
    @Keep
    data class CountryDataModelItem(
        @SerializedName("code")
        var code: String? = "",
        @SerializedName("dialCode")
        var dialCode: String? = "",
        @SerializedName("flag")
        var flag: String? = "",
        @SerializedName("name")
        var name: String? = "",
        var isSelected: Boolean = false
    )
}