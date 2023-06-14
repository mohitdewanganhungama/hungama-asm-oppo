package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

data class UserPreviewDetails(
        @SerializedName("hungama_user_id")
        var code: String? = "",
        @SerializedName("user_streamed_min")
        var user_streamed_min: Int? = 0,
        @SerializedName("entry_created_time")
        var entry_created_time: String? = "",
        @SerializedName("first_stream_start_time")
        var first_stream_start_time: String? = "",
        @SerializedName("activity_last_updated")
        var activity_last_updated: String? = "",
        @SerializedName("is_first_stream_started")
        var is_first_stream_started: Boolean? = false,
        @SerializedName("stream_max_minutes_allowed")
        var stream_max_minutes_allowed: String? = "",
        @SerializedName("current_timestamp")
        var current_timestamp: String? = "")
