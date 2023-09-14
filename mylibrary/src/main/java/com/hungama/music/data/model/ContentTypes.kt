package com.hungama.music.data.model

enum class ContentTypes(val contentTypeName: String, val value: Int) {
    NONE("",0),
    AUDIO("Audio", 1),
    VIDEO("Music Video", 2),
    SHORT_VIDEO("Short Video", 3),
    MOVIES("Movies", 4),
    PODCAST("Podcast", 5),
    SHORT_FILMS("Short Films", 6),
    RADIO("Radio", 7),
    LIVE_CONCERT("Live Concert", 8),
    TV_SHOWS("Tv Show", 9),
    Audio_Ad("Audio Ad", 10),
    Live_Radio("Live Radio", 11);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): ContentTypes {
            return when (value) {
                1 -> AUDIO
                2 -> VIDEO
                3 -> SHORT_VIDEO
                4 -> MOVIES
                5 -> PODCAST
                6 -> SHORT_FILMS
                7 -> RADIO
                8 -> LIVE_CONCERT
                9 -> TV_SHOWS
                10 -> Audio_Ad
                else -> NONE
            }
        }

    }
}