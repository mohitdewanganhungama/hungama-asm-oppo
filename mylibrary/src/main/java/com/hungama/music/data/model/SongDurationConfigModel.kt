package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import com.google.common.collect.ObjectArrays
import com.hungama.music.utils.CommonUtils
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.json.JSONObject
import java.util.Objects

@Keep
@Parcelize
data class SongDurationConfigModel(
    @SerializedName("enable_minutes_quota")
    var enable_minutes_quota:Boolean = false,
    @SerializedName("global_limited_minutes_quota")
    var global_limited_minutes_quota:Int = 0,
    @SerializedName("global_limited_stream_preview_quota")
    var global_limited_stream_preview_quota:Int = 10,
    @SerializedName("is_free_trial_eligible")
    var is_free_trial_eligible:Boolean = false ,
    @SerializedName("nudge_stream_preview")
    var nudge_stream_preview: DrawerMinuteQuotaExhausted  = DrawerMinuteQuotaExhausted() ,
    @SerializedName("drawer_minute_quota_exhausted")
    var drawer_minute_quota_exhausted:DrawerMinuteQuotaExhausted = DrawerMinuteQuotaExhausted(),
    @SerializedName("nudge_minute_quota_exhausted")
    var nudge_minute_quota_exhausted:DrawerMinuteQuotaExhausted = DrawerMinuteQuotaExhausted()):Parcelable
{

    @Keep
    @Parcelize
    data class DrawerMinuteQuotaExhausted(
        @SerializedName("nonft")
        var nonft: Nonft = Nonft(),
        @SerializedName("ft")
        var ft: Ft = Ft()
    ) : Parcelable

    @Keep
    @Parcelize
    data class Nonft(
        @SerializedName("button_text_1")
        var button_text_1: String = "",
        @SerializedName("button_text_2")
        var button_text_2: String = "",
        @SerializedName("plan_id")
        var plan_id: String = "",
        @SerializedName("image_url")
        var image_url: String = "",
        @SerializedName("design_template_id")
        var design_template_id: Int = 1,
        @SerializedName("Audio_id")
        var audio_id: @RawValue Any? = null
    ) : Parcelable

    @Keep
    @Parcelize
    data class Ft(
        @SerializedName("button_text_1")
        var button_text_1: String = "",
        @SerializedName("button_text_2")
        var button_text_2: String = "",
        @SerializedName("plan_id")
        var plan_id: String = "",
        @SerializedName("image_url")
        var image_url: String = "",
        @SerializedName("design_template_id")
        var design_template_id: Int = 1,
        @SerializedName("Audio_id")
        var audio_id: @RawValue Any? = null
    ) : Parcelable
}