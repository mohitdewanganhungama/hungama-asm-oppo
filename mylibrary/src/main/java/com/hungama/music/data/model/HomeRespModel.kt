package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HomeRespModel(
    @field:SerializedName("data")
    val data: HomeDataModel? = null,
    val footer: HomeFooter? = null,
    val body: HomeBody? = null
) : Parcelable


@Parcelize
data class HomeFooterRowsItem(
    val heading: String? = "",
    val more: Int? = null,
    val hscroll: Int? = null,
    val type: Int? = null,
    val numrow: Int? = null,
    var itype: Int? = null,
    var items: List<HomeItem?>? = null,
    val subhead: String? = null
) : Parcelable

@Parcelize
data class HomeItem(
    var data: HomeData? = null,
    val itype: Int? = null
) : Parcelable

@Parcelize
data class HomeFooter(
    val rows: List<HomeFooterRowsItem?>? = null
) : Parcelable

@Parcelize
data class HomeBody(
    val rows: MutableList<HomeFooterRowsItem?>? = null
) : Parcelable

@Parcelize
data class HomeData(
    var image: String? = "",
    var title: String? = "",
    val subTitle: String? = "",
    var location: String? = "",
    var viewInex: Int? = 0,
    var isAllStorySeen: Boolean? = false,
    var stories: MutableList<HomeStory>? = mutableListOf()
) : Parcelable

@Parcelize
data class HomeDataModel(

    @field:SerializedName("head")
    val head: HomeDataHead? = null,
) : Parcelable

@Parcelize
data class DataItemsItem(

    @field:SerializedName("id")
    val id: String? = "",

    @field:SerializedName("title")
    val title: String? = null
) : Parcelable

@Parcelize
data class HomeDataHead(

    @field:SerializedName("hscroll")
    val hscroll: Int? = null,

    @field:SerializedName("id")
    val id: String? = "",

    @field:SerializedName("type")
    val type: Int? = null,

    @field:SerializedName("items")
    val items: List<DataItemsItem?>? = null
) : Parcelable
