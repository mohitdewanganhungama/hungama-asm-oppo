package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SocialLoginRespModel(

    @field:SerializedName("result")
	val result: Result? = null,

    @field:SerializedName("error")
	val error: String? = null
) : Parcelable

@Parcelize
data class SocialLoginData(

	@field:SerializedName("new_user")
	val newUser: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("gigya_login_session_secret")
	val gigyaLoginSessionSecret: String? = null,

	@field:SerializedName("real_user")
	val realUser: String? = null,

	@field:SerializedName("gametoken")
	val gametoken: String? = null,

	@field:SerializedName("user_name")
	val userName: String? = null,

	@field:SerializedName("last_name")
	val lastName: String? = null,

	@field:SerializedName("gigya_login_session_token")
	val gigyaLoginSessionToken: String? = null,

	@field:SerializedName("first_name")
	val firstName: String? = null,

	@field:SerializedName("doy")
	val doy: String? = null
) : Parcelable

@Parcelize
data class Result(

    @field:SerializedName("code")
	val code: Int? = null,

    @field:SerializedName("data")
	val data: SocialLoginData? = null,

    @field:SerializedName("message")
	val message: String? = null
) : Parcelable
