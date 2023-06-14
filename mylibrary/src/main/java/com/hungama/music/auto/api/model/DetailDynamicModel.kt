package com.hungama.music.auto.api.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DetailDynamicModel(
    @SerializedName("data")
    var `data`: Data = Data()
): Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("body")
        var body: Body = Body(),
        @SerializedName("head")
        var head: PlaylistModel.Data.Head = PlaylistModel.Data.Head()
    ): Parcelable  {
        @Keep
        @Parcelize
        data class Body(
            @SerializedName("recomendation")
            val recomendation: ArrayList<RowsItem?>? = ArrayList(),
            @SerializedName("recommendations")
            val searchRecommendations: ArrayList<RowsItem?>? = ArrayList(),
            @SerializedName("rows")
            var rows: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
        ): Parcelable
    }
}