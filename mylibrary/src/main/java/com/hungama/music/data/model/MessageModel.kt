package com.hungama.music.data.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class MessageType constructor(val contentTypeName: String, val value: Int) {
    NEUTRAL("Neutral", 1),
    POSITIVE("Positive", 2),
    NEGATIVE("Negative", 3),
    GAMIFICATION("Gamification", 4);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): MessageType {
            return when (value) {
                1 -> NEUTRAL
                2 -> POSITIVE
                3 -> NEGATIVE
                4 -> GAMIFICATION
                else -> NEUTRAL
            }
        }

    }
}

class MessageModel {
    var header = ""
    var message = ""
    var messageType = MessageType.NEUTRAL
    var isDisplayForLongTime = true

    constructor(
        message: String,
        messageType: MessageType = MessageType.NEUTRAL,
        isDisplayForLongTime: Boolean = true
    ) {
        this.message = message
        this.messageType = messageType
        this.isDisplayForLongTime = isDisplayForLongTime
    }

    constructor(
        header: String,
        message: String,
        messageType: MessageType = MessageType.NEUTRAL,
        isDisplayForLongTime: Boolean = true
    ) {
        this.header = header
        this.message = message
        this.messageType = messageType
        this.isDisplayForLongTime = isDisplayForLongTime
    }
}

