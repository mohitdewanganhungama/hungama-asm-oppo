package com.hungama.music.data.model

import androidx.annotation.Keep

@Keep
data class VideoQuality(
    var id: Int = 0,
    var bitrate: Int? = -1,
    var bandwidth: String? = "",
    var title: String? = "",
    var isSelected: Boolean = false
)