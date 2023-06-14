package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserProfileModel(
    @SerializedName("message")
    var message: String? = "",
    @SerializedName("result")
    var result: List<Result?>? = listOf(),
    @SerializedName("statusCode")
    var statusCode: Int? = 0
) : Parcelable {
    @Keep
    @Parcelize
    data class Result(
        @SerializedName("alternateProfileImage")
        var alternateProfileImage: String? = "",
        @SerializedName("dob")
        var dob: String? = "",
        @SerializedName("email")
        var email: String? = "",
        @SerializedName("firstName")
        var firstName: String? = "",
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
        @SerializedName("shareableURL")
        var share: String? = "",
        @SerializedName("uId")
        var uId: String? = "",
        @SerializedName("shopifyId")
        var shopifyId: String? = ""
    ) : Parcelable

    override fun toString(): String {
        return "UserProfileModel(message=$message, result=$result, statusCode=$statusCode)"
    }
}