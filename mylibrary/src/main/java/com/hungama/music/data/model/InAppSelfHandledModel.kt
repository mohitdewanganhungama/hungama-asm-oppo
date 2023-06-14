package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.moengage.inapp.model.MoEInAppCampaign

@Keep
data class InAppSelfHandledModel(
    @SerializedName("bottom_nav_position")
    var bottom_nav_position: Int = 0,
    @SerializedName("header")
    var header: String = "",
    @SerializedName("options")
    var options: ArrayList<String> = ArrayList(),
    @SerializedName("display_userMembershipTypeId")
    var display_userMembershipTypeId: ArrayList<String> = ArrayList(),
    @SerializedName("position")
    var position: Int = 0,
    @SerializedName("template_id")
    var templateId: String = "",
    @SerializedName("campaign_id")
    var campaign_id: String = "",
    @SerializedName("title")
    var title: String = "",
    @SerializedName("subtitle")
    var subTitle: String = "",
    @SerializedName("image_url")
    var image_url: String = "",
    @SerializedName("deeplink")
    var deeplink: String = "",
    @SerializedName("top_nav_position")
    var top_nav_position: Int = 0,
    var userAnswer: String = "",
    var campaignId: String = "",
    var resolution: String = "",
    var inAppCampaign: MoEInAppCampaign,
)