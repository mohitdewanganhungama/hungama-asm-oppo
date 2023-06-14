package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ContactListModel(
    @SerializedName("data")
    var `data`: List<List<Data?>?>? = listOf(),
    @SerializedName("messsage")
    var messsage: String? = "",
    @SerializedName("statusCode")
    var statusCode: Int? = 0
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
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
        @SerializedName("name")
        var name: String? = "",
        @SerializedName("nickname")
        var nickname: String? = "",
        @SerializedName("number")
        var number: List<String?>? = listOf(),
        @SerializedName("phone")
        var phone: String? = "",
        @SerializedName("profileImage")
        var profileImage: String? = "",
        @SerializedName("uId")
        var uId: String? = "",
        var isAdded:Boolean = false
    ) : Parcelable
}