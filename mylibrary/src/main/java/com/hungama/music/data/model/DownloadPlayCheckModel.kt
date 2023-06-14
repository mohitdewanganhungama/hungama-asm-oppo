package com.hungama.music.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadPlayCheckModel(
    var contentId:String = "",
    var contentTitle:String = "",
    var planName:String = "",
    var isAudio: Boolean = true,
    var isDownloadAction: Boolean = false,
    var isDirectPaymentAction: Boolean = false,
    var queryParam: String = "",
    var isShowSubscriptionPopup: Boolean = true,
    var clickAction:ClickAction = ClickAction.FOR_ALL_CONTENT,
    var restrictedDownload:RestrictedDownload = RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT) :Parcelable

enum class ClickAction(val value: Int) {
    FOR_ALL_CONTENT(0),
    FOR_SINGLE_CONTENT(1);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): ClickAction {
            return when (value) {
                1 -> FOR_ALL_CONTENT
                2 -> FOR_SINGLE_CONTENT
                else -> FOR_ALL_CONTENT
            }
        }
    }
}

enum class RestrictedDownload(val value: Int) {
    NONE_DOWNLOAD_CONTENT(-1),
    ALLOW_DOWNLOAD_CONTENT(0),
    RESTRICT_DOWNLOAD_CONTENT(1);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): RestrictedDownload {
            return when (value) {
                0 -> ALLOW_DOWNLOAD_CONTENT
                1 -> RESTRICT_DOWNLOAD_CONTENT
                else -> NONE_DOWNLOAD_CONTENT
            }
        }
    }
}