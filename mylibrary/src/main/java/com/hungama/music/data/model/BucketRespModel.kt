package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BucketRespModel(

    @field:SerializedName("buckets")
    val buckets: ArrayList<BucketsItem>? = null
) : Parcelable

@Parcelize
data class BucketItemsItem(

    @field:SerializedName("image")
    val image: String? = "",

    @field:SerializedName("sub-title")
    val subTitle: String? = "",

    @field:SerializedName("show_square_type")
    val showSquareType: Boolean? = null,

    @field:SerializedName("item-id")
    val itemId: Int? = null,

    @field:SerializedName("show_round_type")
    val showRoundType: Boolean? = null,

    @field:SerializedName("type-id")
    val typeId: Int? = null,

    @field:SerializedName("show-sub-title")
    val showSubTitle: Boolean? = null,

    @field:SerializedName("shape-id")
    val shapeId: Int? = null,

    @field:SerializedName("show-title")
    val showTitle: Boolean? = null,

    @field:SerializedName("title")
    val title: String? = null
) : Parcelable

@Parcelize
data class BucketsItem(

    @field:SerializedName("bucket-type-id")
    val bucketTypeId: Int? = null,

    @field:SerializedName("sub-title")
    val subTitle: String? = "",

    @field:SerializedName("show-sub-title")
    val showSubTitle: Boolean? = null,

    @field:SerializedName("show-title")
    val showTitle: Boolean? = null,

    @field:SerializedName("shape-id")
    val shapeId: Int? = null,

    @field:SerializedName("row_number")
    val rowNumber: Int? = null,

    @field:SerializedName("type")
    val type: String? = "",

    @field:SerializedName("title")
    val title: String? = "",

    @field:SerializedName("show-more")
    val showMore: Boolean? = null,

    @field:SerializedName("items")
    val items: List<BucketItemsItem?>? = null
) : Parcelable
