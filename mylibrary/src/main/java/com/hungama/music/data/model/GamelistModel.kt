package com.hungama.music.data.model

import androidx.annotation.Keep
import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName

@Keep
data class GamelistModel(
    @SerializedName("data") var `data`: GameData
)

data class GameData (

    @SerializedName("head") var head : GameHead

)


data class GameHead(
    @SerializedName("data") var data: GameHeadDetail,
    @SerializedName("itype") var itype: String,
)

data class GameHeadDetail(
    @SerializedName("id") var id: String,
    @SerializedName("title") var title: String,
    @SerializedName("type") var type: Int,
    @SerializedName("image") var image: String,
    @SerializedName("playble_image") var playble_image: String,
    @SerializedName("releasedate") var releasedate: String,
    @SerializedName("genre") var genre: ArrayList<String>,
    @SerializedName("details_play") var details_play: String,
    @SerializedName("attribute_details_play_ad") var attribute_details_play_ad: ArrayList<String>,
    @SerializedName("attribute_game_rating") var attribute_game_rating: ArrayList<String>,
    @SerializedName("genre_en") var genre_en: ArrayList<String>,
    @SerializedName("category") var category: ArrayList<String>,
    @SerializedName("subgenre_name") var subgenre_name: ArrayList<String>,
    @SerializedName("subtitle") var subtitle: String="",
    @SerializedName("duration") var duration: Int,
    @SerializedName("mode") var mode: String="",
    @SerializedName("image_preview") var image_preview: ArrayList<String> = arrayListOf(),
    @SerializedName("misc") var misc: Misc? = Misc()
)




