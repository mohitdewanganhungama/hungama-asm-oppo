package com.hungama.music.data.webservice


object WSConstants {
        //        const val WS_CONNECTION_TIMEOUT_INT = 2*60*1000//2 min
        const val WS_CONNECTION_TIMEOUT_INT = 60*1000//10 sec
        const val DEFAULT_MAX_RETRIES = 3
        const val MESSAGE = "message"
        const val STATUS_200 = 200
        const val STATUS_500 = 500

        const val DATA = "data"

        const val WS_HEADER_CONTENT_TYPE = "Content-Type"
        const val WS_HEADER_CONTENT_TYPE_JSON = "application/json"
        const val PAY_API_KEY = "API-KEY"
        const val HEADER_IDENTIFIER_KEY = "identifier"
        const val PAY_API_KEY_VALUE = "eedca4a2d4b0b360628958dd0fd210a6"


        /*
            Page api region start
         */
        //Production
       private var PAGE_BASE_URL = "https://cpage.api.hungama.com"
       private var PAGE_DETAIL_URL = "https://page.api.hungama.com"

     //   private var PAGE_BASE_URL = "https://stagpage.api.hungama.com";
     //   private var PAGE_DETAIL_URL = "https://stagpage.api.hungama.com"

        private val SEARCH_BASE_URL = "https://csearch.api.hungama.com"
        private val RECOMMENDED_BASE_URL = "https://recommendation.api.hungama.com"
        private var URL_BASE_URL = "https://raurls.api.hungama.com"
        var METHOD_SEARCH_RECOMMENDATION = "$RECOMMENDED_BASE_URL/v1/search/recommendation"
        var IS_SEARCH_RECOMMENDED_DISPLAY = false
        var canCallAutoPlayAPI = 0
        private var UGC_BASE_URL = "https://cugc.api.hungama.com"
        //        private var UGC_BASE_URL = "https://unugc.api.hungama.com"
        private var EVENTS_BASE_URL = "https://events.api.hungama.com"
        private var PAY_BASE_URL = "https://payapi.hungama.com"
        private val REWARDS_BASE_URL = "https://rewards.api.hungama.com"

        /*
            Page api region start
         */
        //Production http build
//        private var PAGE_BASE_URL = "http://page.api.hungama.com"
//        private var PAGE_DETAIL_URL = "http://page.api.hungama.com"
//        private val SEARCH_BASE_URL = "http://search.api.hungama.com"
//        private var URL_BASE_URL = "http://urls.api.hungama.com"
//        var METHOD_SEARCH_RECOMMENDATION = "$SEARCH_BASE_URL/v1/recommendation"
//        var IS_SEARCH_RECOMMENDED_DISPLAY = false
//        var canCallAutoPlayAPI = 0
//        private var UGC_BASE_URL = "http://ugc.api.hungama.com"
//        private var EVENTS_BASE_URL = "http://events.api.hungama.com"
//        private var PAY_BASE_URL = "http://payapi.hungama.com"
//        private val REWARDS_BASE_URL = "http://rewards.api.hungama.com"

        //Staging
//        private var PAGE_BASE_URL = "https://stagpage.api.hungama.com"
//        private var PAGE_DETAIL_URL = "https://stagpage.api.hungama.com"
//        private val SEARCH_BASE_URL = "https://stagsearch.api.hungama.com"
//        private var URL_BASE_URL = "https://stgurls.api.hungama.com"
//        var METHOD_SEARCH_RECOMMENDATION = "https://stagrecommendation.api.hungama.com/v1/recommendation"
//        var IS_SEARCH_RECOMMENDED_DISPLAY = true
//        var canCallAutoPlayAPI = 0
//        private var UGC_BASE_URL = "https://stagugc.api.hungama.com"
//        private var EVENTS_BASE_URL = "https://events.api.hungama.com"
//        private var PAY_BASE_URL = "https://payapi.hungama.com"
//        private val REWARDS_BASE_URL = "https://rewards.api.hungama.com"

        var METHOD_SSO_LOGIN = "https://h-accounts.hungama.com/webservice/music/"
        var METHOD_SSO_MAIN = "https://communication.api.hungama.com/v1/communication/"
        var METHOD_FETCH_USER_COINS = "https://35.200.232.228:8081/v1/userCoins"
        var METHOD_REDEEM_USER_COINS = "https://35.200.137.158:8081/v1/redeem"
        var METHOD_USER_ACTION = "https://h-accounts.hungama.com/webservice/api/user.php"
        var METHOD_SSO_USER_EXIST = "https://user.api.hungama.com/v1/sso/user/"
         var METHOD_SSO_LOGOUT = "https://h-accounts.hungama.com/webservice/music/logout_silent_user.php"
        var SONG_DURATION_BASE_URL = "https://preview.api.hungama.com/v1"


    var PAGE_BASE_V1 = "/v1/page"
        var PAGE = PAGE_BASE_URL + PAGE_BASE_V1
        var PAGE_DETAIL = PAGE_DETAIL_URL + PAGE_BASE_V1
        var USER_PREVIEW_DETAILS = SONG_DURATION_BASE_URL + "/getUserPreviewDetails"
        var UPDATE_USER_PREVIEW_DETAILS = SONG_DURATION_BASE_URL + "/updateUserPreviewDetails"
        var METHOD_HOME = "$PAGE/home"
        var METHOD_PODCAST = "$PAGE/podcast"
        var METHOD_MUSIC = "$PAGE/listen"
        var METHOD_VIDEO = "$PAGE/watch"
        var METHOD_DETAIL_PAGE = "$PAGE/"
        var METHOD_SPLASH = "$PAGE/splash"
        var METHOD_DETAIL_CONTENT = "$PAGE_DETAIL/content/"
        var METHOD_PLAYABLE_CONTENT_DYNAMIC = "$PAGE_DETAIL_URL/v2/page/content/"
        var METHOD_APP_LANGUAGE = "$PAGE/applang"
        var METHOD_MUSIC_LANGUAGE = "$PAGE/musiclang"
        var METHOD_VIDEO_LANGUAGE = "$PAGE/videolang"
        var METHOD_VIDEO_GENRE = "$PAGE/onboarding_moviegenre"
        var METHOD_MUSIC_ARTIST = "$PAGE/onboarding_artist"
        var METHOD_COUNTRY_LIST = "$PAGE/countries"
        var METHOD_MORE_BUCKET_LIST = "$PAGE/bucket/"
        var METHOD_MOOD_RADID_LIST = "$PAGE/content/moodradio/"
        var METHOD_ON_DEMAND_RADIO_LIST = "$PAGE/content/"
        //        var METHOD_USER_STORY = "$PAGE_DETAIL/content/"
        var METHOD_DOWNLOADABLE_CONTENT_LIST = "$PAGE/content/"
        var METHOD_LIVE_DETAIL_CONTENT = "$PAGE_DETAIL_URL/v2/page/content/"
        var METHOD_PLAYABLE_CONTENT = "$URL_BASE_URL/v1/content/"
//        var METHOD_PLAYABLE_CONTENT = "https://stagurls.api.hungama.com/v1/content/"
        var METHOD_LIVE_EVENT_COUNT = "$EVENTS_BASE_URL/live/start/"


        /* Page api region end */


        /*
            UGC api region start
         */


        var UGC_BASE_VER = "/v1/user/"
        var UGC = UGC_BASE_URL + UGC_BASE_VER

        var METHOD_UPDATE_PREFERENCE = UGC
        var METHOD_USER_UPDATE_STREAM = UGC
        var METHOD_USER_SOCIAL_CONTENT = UGC
        var METHOD_USER_PLAYLIST = UGC
        var METHOD_USER_BOOKMARK = UGC
        var METHOD_USER_CONTACT_LIST_SYNC = UGC
        var METHOD_USER_FACEBOOK_LIST_SYNC = UGC
        var METHOD_LIB_MUSIC_ALL = UGC
        //var METHOD_GET_STORE = UGC+"get-store"
        var METHOD_GET_STORE = "https://ugc.api.hungama.com/v1/user/get-store"

        /* UGC api region end */

        var METHOD_SEARCH_CONTENT = "$SEARCH_BASE_URL/v1/search/"
        var METHOD_ALL_SEARCH_CONTENT = "$SEARCH_BASE_URL/v1/allsearch/"
        var METHOD_AUTO_SUGGESTION = "$SEARCH_BASE_URL/v1/auto/suggestion/"

        var METHOD_DISCOVER_RECOMMENDATION = "$SEARCH_BASE_URL/v1/discover/recommendation"
        var METHOD_PLAY_LIST_RECOMMENDATION = "$RECOMMENDED_BASE_URL/v1/recommendation/trending/songs"
        var METHOD_RECOMMENDATION_TRENDING = "https://stgsearch.api.hungama.com/v2/recommendation/trending"

        var METHOD_USER_READ_STORY = "$UGC_BASE_URL/v1/user/"


        /*
            PAY api region start
         */
        var METHOD_USER_SUBSCRIPTION = "$PAY_BASE_URL/v1/user/subscription_status"
        var METHOD_USER_CONTENT_ORDER_STATUS = "$PAY_BASE_URL/v1/user/check_content_status"
        var METHOD_PLAN_DETAILS = "$PAY_BASE_URL/v1/billing/contentplans"
        var METHOD_CANCEL_PLAN = "$PAY_BASE_URL/v1/user/unsubscription"
        var METHOD_NOTIFY_BILLING = "$PAY_BASE_URL/v1/billing/notifybilling"
        var METHOD_UPDATE_ORDER_STATUS = "$PAY_BASE_URL/v1/billing/orders/update_order_status"
        var METHOD_UPDATE_CONTENT_VALIDITY = "$PAY_BASE_URL/v1/billing/orders/update_content_validity"
        var METHOD_PLAN_INFO = "http://payapi.hungama.com/v1/billing/payments"
        /* PAY api region end */


        /*
            Rewards api region start
         */

        var METHOD_REWARDS_PRODUCTLIST = REWARDS_BASE_URL+"/v1/page/"
        var METHOD_REWARDS_PRODUCT_DETAIL = REWARDS_BASE_URL+"/v1/product/products/"
        var METHOD_CREATE_ORDER = REWARDS_BASE_URL+"/v1/order"
        var METHOD_USER_ORDER = REWARDS_BASE_URL+"/v1/customer/customers/"

        /* Rewards api region end */



//        var METHOD_RECOMMENDATION_AUTO_PLAY = "$SEARCH_BASE_URL/v2/recommendation/auto-play"
        var METHOD_RECOMMENDATION_AUTO_PLAY = "$RECOMMENDED_BASE_URL/v1/recommendation/auto-play"
        var METHOD_DISCOVER_RECOMMENDATION_REEL = "$SEARCH_BASE_URL/v2/recommendation/reels"
        var METHOD_DAILY_DOSE_XRM_PLAYLISTS = "$SEARCH_BASE_URL/v2/recommendation/xrm_playlists"
        var METHOD_TRENDING_PLAYLIST = "$SEARCH_BASE_URL/v2/recommendation/Trending-Playlist"
        var METHOD_TRENDING_ARTIST = "$SEARCH_BASE_URL/v2/recommendation/Trending-Artist"
        var METHOD_TRENDING_MOVIE = "$SEARCH_BASE_URL/v2/recommendation/Trending-Movie"
        var METHOD_TRENDING_PODCASTS = "$SEARCH_BASE_URL/v2/recommendation/Trending-Podcasts"
        var METHOD_YOU_MAY_LIKE_PODCAST = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-Podacsts"
        var METHOD_YOU_MAY_LIKE_MUSIC_VIDEO = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-Music-Video"
        var METHOD_YOU_MAY_LIKE_MOVIE = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-Movie"
        var METHOD_YOU_MAY_LIKE_TVSHOW = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-TVShow"
        var METHOD_YOU_MAY_LIKE_ALBUM = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-Album"
        var METHOD_ALBUM_DETAIL_FEATURED_PLAYLIST = "$SEARCH_BASE_URL/v2/recommendation/Album-Detail-Featured-Playlists"
        var METHOD_ALBUM_DETAIL_ARTIST_FOR_YOU = "$SEARCH_BASE_URL/v2/recommendation/Album-Detail-Artists-For-You"
        var METHOD_YOU_MAY_LIKE_PLAYLIST = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-Playlist"
        var METHOD_YOU_MAY_LIKE_ARTIST = "$SEARCH_BASE_URL/v2/recommendation/You-May-Like-Artist"
        var METHOD_BECAUES_YOU_PLAYED = "$SEARCH_BASE_URL/v2/recommendation/becaues-you-played"
        var METHOD_LISTEN_AGAIN = "$SEARCH_BASE_URL/v2/recommendation/listen-again"
        var METHOD_MORE_FROM_GENRE = "$SEARCH_BASE_URL/v2/recommendation/more-from-genre"
        var METHOD_SIMILAR = "$SEARCH_BASE_URL/v2/recommendation/similar"
        var METHOD_TRENDING = "$SEARCH_BASE_URL/v2/recommendation/trending"
        var METHOD_MORE_FROM_SINGER = "$SEARCH_BASE_URL/v2/recommendation/more-from-singer"
        var METHOD_NEW_RELEASE = "$SEARCH_BASE_URL/v2/recommendation/new-release"
        var METHOD_GENERIC_TRENDING_PLAYLIST = "$SEARCH_BASE_URL/v2/recommendation/generic-trending-playlist"
        var METHOD_RECENT_SEARCH = "$SEARCH_BASE_URL/v1/users/recent"
        var METHOD_DELETE_RECENT_SEARCH = "$SEARCH_BASE_URL/v1/deleteusers/recent"

}