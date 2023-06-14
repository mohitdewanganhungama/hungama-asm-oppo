package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class GetStoreRespModel(
    @SerializedName("country_code")
    var countryCode: String = "",
    @SerializedName("store_id")
    var storeId: Int = 0
) : Parcelable