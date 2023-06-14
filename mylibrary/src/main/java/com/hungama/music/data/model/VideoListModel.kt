package com.hungama.music.data.model//package com.hungama.music.model
//
//import android.os.Parcelable
//import androidx.annotation.Keep
//import com.google.gson.annotations.SerializedName
//import kotlinx.android.parcel.Parcelize
//
//@Parcelize
//data class VideoListModel(
//
//    @field:SerializedName("subtitle")
//    var subtitle: List<SubtitleItem?>? = null,
//
//    @field:SerializedName("name")
//    var name: String? = null,
//
//    @field:SerializedName("drmlicence")
//    var drmlicence: String? = null,
//
//    @field:SerializedName("id")
//    var id: String? = null,
//
//    @field:SerializedName("lyrics")
//    val lyrics: List<String?>? = null,
//
//    @field:SerializedName("url")
//    var url: String? = null,
//
//    @SerializedName("skipIntro")
//    var skipIntro: SkipIntro? = SkipIntro()
//
//
//
//) : Parcelable
//
//@Parcelize
//data class SubtitleItem(
//
//    @field:SerializedName("link")
//    var link: String? = null,
//
//    @field:SerializedName("lang_id")
//    var langId: Int? = null,
//
//    var isSelected: Boolean = false,
//
//    @field:SerializedName("lang")
//    var lang: String? = null
//) : Parcelable
//
//@Parcelize
//data class SkipIntro(
//    @SerializedName("skipCreditET")
//    var skipCreditET: Long? = 0,
//    @SerializedName("skipCreditST")
//    var skipCreditST: Long? = 0,
//    @SerializedName("skipIntroET")
//    var skipIntroET: Long? = 0,
//    @SerializedName("skipIntroST")
//    var skipIntroST: Long? = 0
//): Parcelable