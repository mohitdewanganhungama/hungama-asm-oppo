package com.hungama.music.data.model

enum class QualityAction(val qualityActionName: String, val value: Int) {
    NONE("NONE", 0),
    MUSIC_PLAYBACK_STREAM_QUALITY("MUSIC_PLAYBACK_STREAM_QUALITY", 1),
    MUSIC_PLAYBACK_DOWNLOAD_QUALITY("MUSIC_PLAYBACK_DOWNLOAD_QUALITY", 2),
    VIDEO_PLAYBACK_STREAM_QUALITY("VIDEO_PLAYBACK_STREAM_QUALITY", 3),
    VIDEO_PLAYBACK_DOWNLOAD_QUALITY("VIDEO_PLAYBACK_DOWNLOAD_QUALITY", 4);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): QualityAction {
            return when (value) {
                1 -> MUSIC_PLAYBACK_STREAM_QUALITY
                2 -> MUSIC_PLAYBACK_DOWNLOAD_QUALITY
                3 -> VIDEO_PLAYBACK_STREAM_QUALITY
                4 -> VIDEO_PLAYBACK_DOWNLOAD_QUALITY
                else -> NONE
            }
        }

    }
}