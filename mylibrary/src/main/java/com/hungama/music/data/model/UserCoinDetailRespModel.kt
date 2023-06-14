package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserCoinDetailRespModel(
    @SerializedName("actions")
    var actions: ArrayList<Action> = ArrayList(),
    @SerializedName("challenges")
    var challenges: ArrayList<Challenge?>? = ArrayList()
) : Parcelable {
    @Keep
    @Parcelize
    data class Action(
        @SerializedName("add_preference")
        var addPreference: String? = "",
        @SerializedName("anniversary2")
        var anniversary2: String? = "",
        @SerializedName("CRM_Adjustment")
        var cRMAdjustment: String? = "",
        @SerializedName("create_playlist")
        var createPlaylist: String? = "",
        @SerializedName("discover")
        var discover: String? = "",
        @SerializedName("download")
        var download: String? = "",
        @SerializedName("favorite")
        var favorite: String? = "",
        @SerializedName("ISP_TVOD")
        var iSPTVOD: String? = "",
        @SerializedName("invite_friends")
        var inviteFriends: String? = "",
        @SerializedName("listen")
        var listen: String? = "",
        @SerializedName("music_subscription")
        var musicSubscription: String? = "",
        @SerializedName("_newConnection")
        var newConnection: String? = "",
        @SerializedName("_newSiteUser")
        var newSiteUser: String? = "",
        @SerializedName("_points")
        var points: String? = "",
        @SerializedName("purchase")
        var purchase: String? = "",
        @SerializedName("_redeemPoints")
        var redeemPoints: String? = "",
        @SerializedName("_share")
        var share: String? = "",
        @SerializedName("_siteLogin")
        var siteLogin: String? = "",
        @SerializedName("total")
        var total: Int? = 0,
        @SerializedName("uid")
        var uid: String? = "",
        @SerializedName("watch_fullmovie")
        var watchFullmovie: String? = "",
        @SerializedName("watch_video")
        var watchVideo: String? = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class Challenge(
        @SerializedName("challengeID")
        var challengeID: String? = "",
        @SerializedName("level")
        var level: Level? = Level(),
        @SerializedName("totalPoints")
        var totalPoints: Int? = 0
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Level(
            @SerializedName("badgeURL")
            var badgeURL: String? = "",
            @SerializedName("level")
            var level: Int? = 0
        ) : Parcelable
    }
}