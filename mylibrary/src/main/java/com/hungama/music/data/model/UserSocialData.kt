package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserSocialData(
    @SerializedName("follower")
    var follower: List<Follower?>? = listOf(),
    @SerializedName("followerCount")
    var followerCount: Int? = 0,
    @SerializedName("following")
    var following: List<Following?>? = listOf(),
    @SerializedName("followingCount")
    var followingCount: Int? = 0
) : Parcelable {
    @Keep
    @Parcelize
    data class Follower(
        @SerializedName("alternateProfileImage")
        var alternateProfileImage: String? = "",
        @SerializedName("dob")
        var dob: String? = "",
        @SerializedName("email")
        var email: String? = "",
        @SerializedName("firstName")
        var firstName: String? = "",
        @SerializedName("followerCount")
        var followerCount: Int? = 0,
        @SerializedName("followingCount")
        var followingCount: Int? = 0,
        @SerializedName("gender")
        var gender: String? = "",
        @SerializedName("handleName")
        var handleName: String? = "",
        @SerializedName("lastName")
        var lastName: String? = "",
        @SerializedName("nickname")
        var nickname: String? = "",
        @SerializedName("phone")
        var phone: String? = "",
        @SerializedName("profileImage")
        var profileImage: String? = "",
        @SerializedName("uId")
        var uId: String? = "",
        var isAdded:Boolean = false
    ) : Parcelable

    @Keep
    @Parcelize
    data class Following(
        @SerializedName("alternateProfileImage")
        var alternateProfileImage: String? = "",
        @SerializedName("dob")
        var dob: String? = "",
        @SerializedName("email")
        var email: String? = "",
        @SerializedName("firstName")
        var firstName: String? = "",
        @SerializedName("followerCount")
        var followerCount: Int? = 0,
        @SerializedName("followingCount")
        var followingCount: Int? = 0,
        @SerializedName("gender")
        var gender: String? = "",
        @SerializedName("handleName")
        var handleName: String? = "",
        @SerializedName("lastName")
        var lastName: String? = "",
        @SerializedName("nickname")
        var nickname: String? = "",
        @SerializedName("phone")
        var phone: String? = "",
        @SerializedName("profileImage")
        var profileImage: String? = "",
        @SerializedName("uId")
        var uId: String? = "",
        var isAdded:Boolean = true
    ) : Parcelable
}