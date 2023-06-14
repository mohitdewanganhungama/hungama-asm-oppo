package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserStoryModel(
    @SerializedName("child")
    var child: List<Child> = listOf(),
    @SerializedName("created_at")
    var createdAt: String = "",
    @SerializedName("enabled")
    var enabled: Boolean = false,
    @SerializedName("expiry")
    var expiry: String = "",
    @SerializedName("_id")
    var id: String = "",
    @SerializedName("live_date_time")
    var liveDateTime: String = "",
    @SerializedName("media")
    var media: List<String> = listOf(),
    @SerializedName("modified_at")
    var modifiedAt: String = "",
    @SerializedName("name")
    var name: List<Name> = listOf(),
    @SerializedName("type")
    var type: String = "",
    @SerializedName("__v")
    var v: Int = 0,
    var commentsList : MutableList<CommentsModel>? = mutableListOf()
) : Parcelable {
    @Keep
    @Parcelize
    data class Child(
        @SerializedName("created_at")
        var createdAt: String = "",
        @SerializedName("enabled")
        var enabled: Boolean = false,
        @SerializedName("_id")
        var id: String = "",
        @SerializedName("media")
        var media: List<String> = listOf(),
        @SerializedName("modified_at")
        var modifiedAt: String = "",
        @SerializedName("parentId")
        var parentId: String = "",
        @SerializedName("poll")
        var poll: String = "",
        @SerializedName("pollName")
        var pollName: String = "",
        @SerializedName("polls")
        var polls: Polls = Polls(),
        @SerializedName("sequence")
        var sequence: Int = 0,
        @SerializedName("type")
        var type: String = "",
        @SerializedName("__v")
        var v: Int = 0
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Polls(
            @SerializedName("id")
            var id: String = "",
            @SerializedName("name")
            var name: String = "",
            @SerializedName("options")
            var options: List<Option> = listOf(),
            @SerializedName("title")
            var title: String = "",
            @SerializedName("varient")
            var varient: String = ""
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Option(
                @SerializedName("bgcolor")
                var bgcolor: String = "",
                @SerializedName("external")
                var `external`: Boolean = false,
                @SerializedName("fgcolor")
                var fgcolor: String = "",
                @SerializedName("name")
                var name: String = "",
                @SerializedName("url")
                var url: String = ""
            ) : Parcelable
        }
    }

    @Keep
    @Parcelize
    data class Name(
        @SerializedName("en")
        var en: String = "",
        @SerializedName("gu_in")
        var guIn: String = "",
        @SerializedName("hn")
        var hn: String = "",
        @SerializedName("kn_in")
        var knIn: String = "",
        @SerializedName("mr")
        var mr: String = "",
        @SerializedName("pa_in")
        var paIn: String = "",
        @SerializedName("ta_in")
        var taIn: String = "",
        @SerializedName("te_in")
        var teIn: String = ""
    ) : Parcelable

    fun isVideo(url:String): Boolean {
        return url.contains(".mp4")
    }
}