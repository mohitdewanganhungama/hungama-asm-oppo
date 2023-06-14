package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserSettingData(

	@field:SerializedName("data")
	var data: List<DataItem?>? = null
) : Parcelable

@Parcelize
data class PreferenceItem(

	@field:SerializedName("video_download_quality")
	var videoDownloadQuality: String? = "",

	@field:SerializedName("music_download_quality")
	var musicDownloadQuality: String? = "",

	@field:SerializedName("autoPlay")
	var autoPlay: Boolean = false,

	@field:SerializedName("streaming_quality")
	var streaming_quality: String? = "",

	@field:SerializedName("music_language_preference")
	var music_language_preference: String? = "",

	@field:SerializedName("equalizer")
	var equalizer: String? = "",

	@field:SerializedName("sleep_timer")
	var sleep_timer: Boolean? = true,

	@field:SerializedName("show_lyrics")
	var show_lyrics: Boolean? = true,

	@field:SerializedName("gapless")
	var gapless: Boolean? = false,

	@field:SerializedName("crossfade")
	var crossfade: String? = "",

	@field:SerializedName("smooth_song_transition")
	var smooth_song_transition: Boolean? = false,

	@field:SerializedName("emailNotification")
	var emailNotification: Boolean? = false,

	@field:SerializedName("mobileNotification")
	var mobileNotification: Boolean? = false,

	@field:SerializedName("appLanguage")
	var appLanguage: String? = "",

	@field:SerializedName("allowExplicitContent")
	var allowExplicitContentOld: Boolean? = false,

	@field:SerializedName("cellular_download")
	var cellularDownload: Boolean? = false,

	@field:SerializedName("music_language_preference_title")
	var music_language_preference_title: String? = "",

	@field:SerializedName("video_language_preference_title")
	var video_language_preference_title: String? = "",

	@field:SerializedName("video_language_preference")
	var video_language_preference: String? = "",

	@field:SerializedName("parentalControl")
	var parentalControl: ParentalControlModel = ParentalControlModel(),

	@field:SerializedName("allowAge18Plus")
	var allowAge18Plus: Boolean = false
) : Parcelable

@Parcelize
data class UserSettingRespModel(

	@field:SerializedName("data")
	var data: UserSettingData? = null
) : Parcelable

@Parcelize
data class DataItem(

    @field:SerializedName("uid")
	var uid: String? = "",

    @field:SerializedName("preference")
	var preference: List<PreferenceItem?>? = null,

    @field:SerializedName("_id")
	var id: String? = "",

    @field:SerializedName("type")
	var type: String? = ""
) : Parcelable

@Parcelize
data class ParentalControlModel(
	@field:SerializedName("allowExplicitAudioContent")
	var allowExplicitAudioContent: Boolean = false,
	@field:SerializedName("allowExplicitVideoContent")
	var allowExplicitVideoContent: Boolean = false
) : Parcelable
