package com.hungama.music.data.model

enum class DetailPages(val value: Int) {
    EMPTY_PAGE(0),
    ALBUM_DETAIL_PAGE(1),
    ARTIST_DETAIL_PAGE(2),
    CHART_DETAIL_PAGE(3),
    EVENT_DETAIL_PAGE(4),
    MOVIE_DETAIL_PAGE(5),
    PLAYLIST_DETAIL_PAGE(6),
    PODCAST_DETAIL_PAGE(7),
    SONG_DETAIL_PAGE(8),
    TVSHOW_DETAIL_PAGE(9),
    PODCAST_DETAIL_ADAPTER(10),
    PLAYLIST_DETAIL_ADAPTER(11),
    ALBUM_DETAIL_ADAPTER(12),
    CHART_DETAIL_ADAPTER(13),
    COLLECTION_DETAIL_PAGE(14),
    FAVORITE_DETAIL_PAGE(15),
    MY_PLAYLIST_DETAIL_PAGE(16),
    MUSIC_VIDEO_DETAIL_PAGE(17),
    TVSHOW_DETAIL_ADAPTER(18),
    LOCAL_DEVICE_SONG_PAGE(19),
    SIMILAR_SONG_LIST_PAGE(20),
    SIMILAR_SONG_LIST_ADAPTER(21),
    CATEGORY_DETAIL_PAGE(22),
    RECOMMENDED_SONG_LIST_PAGE(23),
    VIDEO_WATCHLIST_DETAIL_ADAPTER(24);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): DetailPages {
            return when (value) {
                1 -> ALBUM_DETAIL_PAGE
                2 -> ARTIST_DETAIL_PAGE
                3 -> CHART_DETAIL_PAGE
                4 -> EVENT_DETAIL_PAGE
                5 -> MOVIE_DETAIL_PAGE
                6 -> PLAYLIST_DETAIL_PAGE
                7 -> PODCAST_DETAIL_PAGE
                8 -> SONG_DETAIL_PAGE
                9 -> TVSHOW_DETAIL_PAGE
                10 -> PODCAST_DETAIL_ADAPTER
                11 -> PLAYLIST_DETAIL_ADAPTER
                12 -> ALBUM_DETAIL_ADAPTER
                13 -> CHART_DETAIL_ADAPTER
                14 -> COLLECTION_DETAIL_PAGE
                15 -> FAVORITE_DETAIL_PAGE
                16 -> MY_PLAYLIST_DETAIL_PAGE
                17 -> MUSIC_VIDEO_DETAIL_PAGE
                18 -> TVSHOW_DETAIL_ADAPTER
                19 -> LOCAL_DEVICE_SONG_PAGE
                20 -> SIMILAR_SONG_LIST_PAGE
                21 -> SIMILAR_SONG_LIST_ADAPTER
                22 -> CATEGORY_DETAIL_PAGE
                23 -> RECOMMENDED_SONG_LIST_PAGE
                else -> EMPTY_PAGE
            }
        }

    }
}