package com.hungama.music.eventanalytic


class EventConstant {
    companion object {

        val gamiFicationActions = arrayOf( "Registration - Success",
            "Added to Playlist", "Added To WatchList", "App Launch", "app_language_selected",
            "Artwork Tapped", "category_clicked", "collection", "Created Playlist",
            "Favourited", "Language Changed", "Language Selected", "live_concert",
            "Login - Success", "more clicked", "movies_genre_selected", "movies_language_selected",
            "Page Scrolled", "Page View", "Profile Clicked", "Removed From WatchList","af_app_invites",
            "Rent - open Rent Page", "story", "stream", "stream_start","stream_podcast","stream_liveshow",
            "stream_shortmovie","stream_tvshow","stream_shortvideo","stream_shortfilm","stream_musicvideo","stream_movie","stream_song", "Video Player Actions"
        )

        val gamiFicationStreamActions = arrayOf(
           "stream_podcast","stream_liveshow", "stream_shortmovie","stream_tvshow","stream_shortvideo","stream_shortfilm","stream_musicvideo","stream_movie","stream_song"
        )

        const val EVENT_UPLOAD_THRESHOLD = 100
        const val EVENT_UPLOAD_MAX_BATCH_SIZE = 200
        const val EVENT_MAX_COUNT = 1000
        const val EVENT_REMOVE_BATCH_SIZE = 20
        const val EVENT_UPLOAD_PERIOD_MILLIS = (30 * 1000).toLong()// 30s

        //        const val MIN_TIME_BETWEEN_SESSIONS_MILLIS = (15*60* 1000).toLong()// 15m
        const val MIN_TIME_BETWEEN_SESSIONS_MILLIS = 900000L// 5m
        const val SESSION_TIMEOUT_MILLIS = (30 * 60 * 1000).toLong()// 5m

        const val MAX_STRING_LENGTH = 5 * 1024
        const val MAX_PROPERTY_KEYS = 1024
        const val CONTINUE_WATCHING_NAME = "Recently played"


        const val REGISTRATION_SUCCESS_ENAME = "Registration - Success"
        const val LOGINSUCCESS_ENAME = "Login-Success"

        const val APPLAUNCH_ENAME = "App Launch"
        const val AB_NC_EPROPERTY = "ab_nc"

        const val LANGUAGESELECTED_ENAME = "Language_Selected"
        const val LANGUAGECHOSEN_EPROPERTY = "Language Chosen"
        const val SOURCE_EPROPERTY = "Source"
        const val KEYWORD_DERIVED_EPROPERTY = "keyword derived"
        const val RECENT_SEARCH_COUNT_EPROPERTY = "recent search count"
        const val CONTENT_SELECTED_EPROPERTY = "content selected"
        const val SUCCESS_EPROPERTY = "success"
        const val CUID = "CUID"


        const val LANGUAGECHANGE_ENAME = "Language Changed"
        const val LANGUAGECHANGEEDCHOSEN_EPROPERTY = "Language Chosen"
        const val LANGUAGECHANGEDSOURCE_EPROPERTY = "Source"

        const val THREEDOTSCLICKED_ENAME = "3 Dots Clicked"
        const val ACTION_EPROPERTY = "Action"

        const val LOGOUT_ENAME = "Log Out"
        const val LOGO_CLICK = "LogoClick"

        const val SOCIALOGIN_ENAME = "Social Login"

        const val MOREANONYMOUS_ID = "moe_anonymous_id"
        const val HUNGAMA_ID = "Hungama_Id"
        const val HUNGAMA_ID_LOGIN = "Hungama ID"


        const val SOURCE_ONBOARDING = "onboarding"
        const val SOURCE_FAV_SONG = "Favorite Song"
        const val RESPONSE_CODE_200 = "200"

        const val BUCKETSWIPED_ENAME = "Bucket Swiped"
        const val BUCKETSWIPED_EPROPERTY = "bucket swiped"
        const val BUCKETNAME_EPROPERTY = "bucket name"
        const val LASTPOSITIONOFBUCKET_EPROPERTY = "Last visible Position within Bucket"


        const val RECENT_SEARCH_SELECTED_ENAME = "recent_search_selected"
        const val REMIND_CONCERT_ENAME = "remind_concert"
        const val RECENT_SEARCH_CLEAR_TAPPED_ENAME = "recent_search_clear_tapped"
        const val ADDEDTOPLAYLIST_ENAME = "Added to Playlist"
        const val ADDEDTOWATCHLIST_ENAME = "Added To WatchList"
        const val ACTOR_EPROPERTY = "Actor"
        const val CONTENTID_EPROPERTY = "Content ID"
        const val CONTENTNAME_EPROPERTY = "Content Name"
        const val PAGE_NAME_EPROPERTY = "page_name"
        const val CONTENTTYPE_EPROPERTY = "Content Type"
        const val GENRE_EPROPERTY = "Genre"
        const val LANGUAGE_EPROPERTY = "Language"
        const val LYRICIST_EPROPERTY = "Lyricist"
        const val MOOD_EPROPERTY = "Mood"
        const val MUSICDIRECTOR_EPROPERTY = "Music Director/Composer"
        const val PLAYLISTNAME_EPROPERTY = "Playlist Name"
        const val PODCASTNAME_EPROPERTY = "Podcast Name"
        const val PODCASTHOST_EPROPERTY = "Podcast host"
        const val SINGER_EPROPERTY = "Singer"
        const val TEMPO_EPROPERTY = "Tempo"
        const val YEAROFRELEASE_EPROPERTY = "Year of release"
        const val EPISODE_NUMBER_EPROPERTY = "Episode Number"
        const val SEASON_NUMBER_EPROPERTY = "Season Number"
        const val SOURCE_NAME_EPROPERTY = "Source Name"
        const val STREAM_ENAME = "stream"
        const val PREVIEW_STREAM_ENAME = "preview_stream"
        const val STREAM_TRIGGER_ENAME = "stream_trigger"
        const val PREVIEW_STREAM_TRIGGER_ENAME = "preview_stream_trigger"
        const val STREAM_SONG_ENAME = "stream_song"
        const val STREAM_PODCAST_ENAME = "stream_podcast"
        const val STREAM_MUSICVIDEO_ENAME = "stream_musicvideo"
        const val STREAM_MOVIE_ENAME = "stream_movie"
        const val STREAM_SHORTMOVIE_ENAME = "stream_shortmovie"
        const val STREAM_TVSHOW_ENAME = "stream_tvshow"
        const val STREAM_SHORTVIDEO_ENAME = "stream_shortvideo"
        const val STREAM_SHORTFILM_ENAME = "stream_shortfilm"
        const val STREAM_LIVESHOW_ENAME = "stream_liveshow"
        const val STREAM_START_ENAME = "stream_start"
        const val PREVIEW_STREAM_START_ENAME = "preview_stream_start"
        const val STORY_PAGE_VIEWED_ENAME = "story_page_viewed"
        const val STORY_PAGE_CTA_CLICKED_ENAME = "story_page_CTA_clicked"
        const val PROGRESSIVE_SURVEY_VIEW_ENAME = "Progressive_survey_view"
        const val PROGRESSIVE_SURVEY_TAPPED_ENAME = "Progressive_survey_tapped"
        const val AF_STREAM_20_ENAME = "stream_20"
        const val AF_STREAM_10_VIDEO_ENAME = "stream_10_video"
        const val AF_PURCHASE_ENAME = "purchase"
        const val AF_SUBSCRIBE_ENAME = "af_subscribe"
        const val AF_CREATE_PLAYLIST = "create_playlist"
        const val AF_FAVOURITE = "favourite"
        const val AF_ID = "id"
        const val AF_TYPE = "type"
        const val AF_OPENED_PRO_PAGE = "opened_pro_page"
        const val AF_COMPLETED_20_FREE_DOWNLOADS = "completed_20_free_downloads"
        const val AF_FIRST_STREAM = "first_stream"
        const val AF_MEDIA_DOWNLOADED = "media_downloaded"
        const val AF_MEDIA_DOWNLOADED_VIDEO = "media_downloaded_video"
        const val AF_MEDIA_PLAY = "media_play"
        const val AF_PURCHASE_METHOD_PROPERTY = "purchase_method"
        const val AF_START_DATE_PROPERTY = "start_date"
        const val AF_EXPIRATION_DATE_PROPERTY = "expiration_date"
        const val AF_SUBSCRIPTION_PERIOD_PROPERTY = "subscription_period"
        const val AF_SUBSCRIPTION_METHOD_PROPERTY = "subscription_method"
        const val AF_FIRST_DOWNLOAD_VIDEO = "first_download_video"
        const val AF_FIRST_STREAM_VIDEO = "first_stream_video"
        const val AF_FIRST_DOWNLOAD = "first_download"
        const val AF_LOCAL_PLAY = "local_play"
        const val AF_SUBSCRIBED_BY_COINS = "subscribed_by_coins"

// ------------- API Performance  ---------------------

        const val APIPERFORMANCE_ENAME = "api_performance"
        const val ERRORCODE_EPROPERTY = "error_code"
        const val NETWORKTYPE_EPROPERTY = "network_type"
        const val RESPONSECODE_EPROPERTY = "response_code"
        const val RESPONSETIME_EPROPERTY = "response_time"
        const val URL_EPROPERTY = "url"

//  ------------- Artwork Tapped --------------------------

        const val ARTWORKTAPPED_ENAME = "Artwork Tapped"
        const val ARTWORKTYPE_EPROPERTY = "Artwork Type"
        const val TOPNAVNAME_EPROPERTY = "top_nav_name"
        const val FILTERSELECTED_EPROPERTY = "filter_selected"
        const val TOPNAVPOSITION_EPROPERTY = "top_nav_position"
        const val BUCKETTYPE_EPROPERTY = "bucket_type"
        const val BUCKETPOSITION_EPROPERTY = "bucket_position"
        const val PROGRAMMINGTYPE_EPROPERTY = "programming_type"
        const val BUCKETID_EPROPERTY = "bucket_id"
        const val ARTISTNAME_EPROPERTY = "Artist Name"
        const val EXTRA_EPROPERTY = "extra"
        const val ARTWORKPLAYLISTNAME_EPROPERTY = "playlist_name"
        const val POSITIONWITHINBUCKET_EPROPERTY = "Position within Bucket"
        const val SOURCEBUCKET_EPROPERTY = "Source Bucket"
        const val SOURCEPAGE_EPROPERTY = "source_page name"
        const val SOURCEPAGETYPE_EPROPERTY = "page_type"
        const val SOURCE_DETAILS_EPROPERTY = "source_details"
        const val SOURCE= "source"
        const val SOURCE_PAGE_EPROPERTY = "source_page"

//  -------------------- Offline Song ------------------------------

        const val OFFLINESONG_ENAME = "Offlined Song"
        const val ALBUMID_EPROPERTY = "Album ID"
        const val ALBUMNAME_EPROPERTY = "Album Name"
        const val DOWNLOADQUALITY_EPROPERTY = "Download Quality"
        const val PLAYLISTID_EPROPERTY = "Playlist ID"
        const val PODCASTALBUMNAME_PROPERTY = "podcast_album_name"
        const val EPISODENUMBER_EPROPERTY = "Episode Number"
        const val RELEASEDATE_EPROPERTY = "Release Date"
        const val SEASONNUMBER_EPROPERTY = "Season Number"
        const val CONTENTPAYTYPE_EPROPERTY = "Content PayType"
        const val CONNECTIONTYPE_EPROPERTY = "Connection Type"
        const val SONGNAME_EPROPERTY = "Song Name"
        const val SUBGENRE_EPROPERTY = "Sub Genre"
        const val SUBSCRIPTIONSTATUS_EPROPERTY = "Subscription Status"

//  --------------- Favourited --------------------

        const val FAVOURITED_ENAME = "Favourited"
        const val PRODCASTHOST_EPROPERTY = "podcast_host"
        const val CREATOR_EPROPERTY = "creator"

        //  ----------------- category clicked ------------------------
        const val CATEGORY_ENAME = "category_clicked"
        const val TYPE_EPROPERTY = "type"
        const val CONTENT_TYPE_EPROPERTY = "content_type"
        const val CONTENT_TYPE_ID_EPROPERTY = "content_type_id"
        const val CATEGORYNAME_EPROPERTY = "category_name"

        const val SEARCH_NO_RESULT_ENAME = "no_result_try_again_clicked"
        const val SEARCH_BAR_CLICK = "click_search_bar"
        const val SEARCH_RESULT_CLICK = "search_result_click"
        const val SEARCH_KEYWORD_ENTER = "search_keyword_entered"
        const val SEARCH_AUTOSUGGEST_DISPLAY = "autosuggest_display"
        const val PAGENAME_EPROPERTY = "pagename"
        const val ACTIONS_EPROPERTY = "action"
        const val BUTTONNAME_EPROPERTY = "button_name"
        const val KEYWORD_ENTERED_EPROPERTY = "keyword_entered"
        const val KEYWORD_UTTERED_EPROPERTY = "keyword_uttered"
        const val CLICK_POSITION_EPROPERTY = "click_position"
        const val RESULT_COUNT_EPROPERTY = "result_count"

        const val APPLANGUAGESELECTED_ENAME = "app_language_selected"

        const val MOVIESLANGUAGESELECTED_ENAME = "movies_language_selected"

        const val MOVIESGENRESELECTED_ENAME = "movies_genre_selected"

        const val LOGIN_ENAME = "login"
        const val LOGIN_EPROPERTY = "login"

        const val LOGINEMAILCLICKED_ENAME = "Login - Email Clicked"

        const val LOGINEMAILFILLED_ENAME = "Login - Email Filled"

        const val LOGINOTP_ENAME = "Login OTP"

        const val LOGINOTPPAGELOADSUCCESS_ENAME = "Login-OTP_Page_load_success"

        const val LOGINOTPRESEND_ENAME = "Login- OTP_resend"

        const val LOGINERROR_ENAME = "Login - Error"
        const val ERRORTYPE_EPROPERTY = "Error Type"
        const val SCREEN_NAME_EPROPERTY = "screen_name"

        const val LOGINMOBILECLICKED_ENAME = "Login-Mobile Clicked"

        const val LOGINMOBILENUMBERFILLED_ENAME = "Login - Mobile Number Filled"

        const val LOGINMOBILENUMBERCOUNTRYCODE_ENAME = "Login - Mobile_Number_country_code"

        const val MORECLICKED_ENAME = "more clicked"

        const val PAGESCROLLED_ENAME = "Page Scrolled"
        const val LASTVISIBLEROWPOSITION_EPROPERTY = "Last visible row position"
        const val FROMBUCKET_EPROPERTY = "from_bucket"
        const val TOBUCKET_EPROPERTY = "to_bucket"

        const val PAGEVIEW_ENAME = "Page View"
        const val VARIENT_EPROPERTY = "varient"

        const val PROFILECLICKED_ENAME = "Profile Clicked"

        const val RATINGPOPUP_ENAME = "Rating Pop Up"
        const val RATINGGIVEN_EPROPERTY = "rating given"

        const val REMOVEDFROMWATCHLIST_ENAME = "Removed From WatchList"

        const val RENTBACKFROMRENTPAGE_ENAME = "Rent-Back from Rent Page"

        const val RENTFAIL_ENAME = "Rent - Fail"
        const val FAILUREREASON_EPROPERTY = "Failure Reason"

        const val RENTOPENRENTPAGE_ENAME = "Rent - open Rent Page"
        const val PLANTYPE_ENAME = "Plan Type"

        const val RENTPAYMENTOPTION_ENAME = "Rent - Payment Option"
        const val METHOD_EPROPERTY = "method"
        const val MODE_EPROPERTY = "Mode"

        const val CASTCONTENT_ENAME = "cast_content"
        const val CASTDEVICETYPE_EPROPERTY = "cast_device_type"


        const val CLICKED_SEARCH_ENAME = "clicked_search"
        const val SEARCHTEXTBOXTAPPED_ENAME = "search_textbox_tapped"
        const val RECENTSEARCHCOUNT_EPROPERTY = "Recent-Search-Count"
        const val RECENT_SEARCHCOUNT_EPROPERTY = "recent_search_count"
        const val AUTOSUGGEST_COUNT = "autosuggest_count"
        const val KEYWORD_ENTERED = "keyword_entered"
        const val SEARCH_EPROPERTY = "search"
        const val KEYWORD_EPROPERTY = "keyword"

        const val SEARCHRESULTFILTER_ENAME = "search_result_filter"
        const val RESULTCOUNT_EPROPERTY = "Result-Count"
        const val FILTERNAME_EPROPERTY = "Filter-Name"

        const val SKIP_ENAME = "Skip"

        const val STREAMFAILED_ENAME = "stream_failed"
        const val CONSUMPTIONTYPE_EPROPERTY = "Consumption Type"
        const val PCODE_EPROPERTY = "P Code"
        const val SCODE_EPROPERTY = "S Code"
        const val AP_EPROPERTY = "AP"
        const val BUFF_EPROPERTY = "buff"

        const val VIDEOPLAYERPAUSE_ENAME = "video_player_pause"
        const val CONTENTTYPESTREAMING_EPROPERTY = "content_type_streaming"
        const val PLAYERTYPE_EPROPERTY = "player_type"

        const val VIDEOPLAYERPLAY_ENAME = "video_player_play"

        const val VIDEOPLAYERSKIPINTRO_ENAME = "video_player_skip_intro"

        const val VIDEOPLAYERSKIPFORWARD_ENAME = "video_player_skip_forward"

        const val VIDEOPLAYERSKIPBACKWARD_ENAME = "video_player_skip_backward"

        const val VIDEOPLAYERAUDIOACTION_ENAME = "video_player_audio_action"

        const val VIDEOPLAYERBRIGHTNESSACTION_ENAME = "video_player_brightness_action"

        const val VIDEOPLAYERAUDIOLANGUAGESELECTED_ENAME = "video_player_audio_language_selected"

        const val VIDEOPLAYERSUBTITLESELECTED_ENAME = "video_player_subtitle_selected"

        const val VIDEOPLAYERVIDEOQUALITYSELECTED_ENAME = "video_player_video_quality_selected"
        const val STREAMQUALITYSELECTED_EPROPERTY = "stream_quality_selected"

        const val VIDEOPLAYERLOCKTAPPED_ENAME = "video_player_lock_tapped"

        const val VIDEOPLAYERBACKTAPPED_ENAME = "video_player_back_tapped"

        const val VIDEOPLAYERACTIONS_ENAME = "Video Player Actions"
        const val LISTINGSCREENNAME_EPROPERTY = "listing screen name"

        const val WEBVIEWPERFORMANCE_ENAME = "web_view_performance"
        const val LOADTIME_EPROPERTY = "load_time"

        const val AUTOCOMPLETERESULT_ENAME = "autocomplete_results"
        const val QUERY_EPROPERTY = "Query"
        const val RESULTTITLE_EPROPERTY = "Result-title"
        const val NOOFAPICALLS_EPROPERTY = "No of API calls"

        const val VOICETAP_ENAME = "voice_tap"

        const val SUBTITLESETTINGSCLICKED_ENAME = "subtitle settings clicked"

        const val AUDIOPLAYERPAUSE_ENAME = "audio_player_pause"

        const val AUDIOPLAYERPLAY_ENAME = "audio_player_play"

        const val AUDIOPLAYERSKIPFORWARD_ENAME = "audio_player_skip_forward"

        const val AUDIOPLAYERSKIPBACKWARD_ENAME = "audio_player_skip_backward"

        const val AUDIOPLAYERAUDIOACTION_ENAME = "audio_player_audio_action"

        const val AUDIOPLAYERQUALITYSELECTED_ENAME = "audio_player_quality_selected"

        const val AUDIOPLAYERBACKTAPPED_ENAME = "audio_player_back_tapped"

        const val BACKGROUND_ACTIVITY = "BackgroundActivity"


        const val CREATEDPLAYLIST_ENAME = "Created Playlist"
        const val SOURCENAME_EPROPERTY = "Source"
        const val ALBUM_ID_EPROPERTY = "Album ID"
        const val APPBOY_PUSH_RECEIVED_TIMESTAMP_EPROPERTY = "appboy_push_received_timestamp"
        const val AUDIO_QUALITY_EPROPERTY = "Audio Quality"
        const val CONNECTION_TYPE_EPROPERTY = "Connection Type"
        const val CONSUMPTION_TYPE_EPROPERTY = "Consumption Type"
        const val DEVICE_MODEL_EPROPERTY = "Device Model"
        const val DURATION_EPROPERTY = "duration"
        const val DURATION_BG_EPROPERTY = "duration_bg"
        const val DURATION_FG_EPROPERTY = "duration_fg"
        const val LABEL_EPROPERTY = "Label"
        const val LABEL_ID_EPROPERTY = "Label_id"
        const val LAST_SOURCE_EPROPERTY = "Last Source"
        const val LOGIN_STATUS_EPROPERTY = "Login Status"
        const val NAME_EPROPERTY = "Name"
        const val NID_EPROPERTY = "nid"
        const val ORIGINAL_ALBUM_NAME_EPROPERTY = "Original Album Name"
        const val PERCENTAGE_COMPLETION_EPROPERTY = "Percentage Completion"
        const val PLAYLIST_ID_EPROPERTY = "Playlist ID"
        const val PLAYLIST_NAME_EPROPERTY = "Playlist Name"
        const val PODCAST_ALBUM_NAME_EPROPERTY = "podcast_album_name"
        const val SONG_NAME_EPROPERTY = "Song Name"
        const val LENGTH_EPROPERTY = "length"
        const val CONTENT_NAME_EPROPERTY = "content_name"
        const val SUB_GENRE_EPROPERTY = "Sub Genre"
        const val LYRICS_TYPE_EPROPERTY = "lyrics_type"
        const val SUBSCRIPTION_STATUS_EPROPERTY = "Subscription Status"
        const val RATING_EPROPERTY = "rating"
        const val IS_ORIGINAL_EPROPERTY = "is_original"
        const val CATEGORY_EPROPERTY = "category"
        const val CAST_ENABLED_EPROPERTY = "cast_enabled"
        const val AGE_RATING_EPROPERTY = "age_rating"
        const val CONTENT_PAY_TYPE_EPROPERTY = "Content Pay Type"
        const val CRITIC_RATING_EPROPERTY = "Critic_Rating"
        const val KEYWORDS_EPROPERTY = "keywords"
        const val PTYPE_EPROPERTY = "ptype"
        const val PID_EPROPERTY = "pid"
        const val PNAME_EPROPERTY = "pname"
        const val RELEASE_DATE_EPROPERTY = "Release_Date"
        const val STATUS_EPROPERTY = "Status"
        const val SUBTITLE_LANGUAGE_SELECTED_EPROPERTY = "Subtitle Language Selected"
        const val SUBTITLE_ENABLE_EPROPERTY = "Subtitle_Enable"
        const val USER_RATING_EPROPERTY = "User_Rating"
        const val VIDEO_QUALITY_EPROPERTY = "Video_Quality"
        const val STORY_ID_EPROPERTY = "story_id"
        const val TITLE_EPROPERTY = "title"
        const val STORY_NAME_EPROPERTY = "name"
        const val TYPE_OF_THE_STORY_PAGE_EPROPERTY = "type of the story page"
        const val POLL_ID_EPROPERTY = "poll_id"
        const val OPTION_SELECTED_EPROPERTY = "option_selected"
        const val CAMPAIGN_ID_EPROPERTY = "campaign_id"
        const val TEMPLATE_ID_EPROPERTY = "Template_id"
        const val SUBTITLE_EPROPERTY = "subtitle"
        const val OPTION_EPROPERTY = "option"
        const val BOTTOM_NAV_POSITION_EPROPERTY = "bottom_nav_position"
        const val TOP_NAV_POSITION_EPROPERTY = "top_nav_position"
        const val POSITION_EPROPERTY = "position"
        const val OPTION_TAPPED_EPROPERTY = "option_tapped"


        const val TAPPED_TNC_CONTINUE_ENAME = "Tapped TnC Continue"
        const val STORY_ENAME = "story"
        const val COLLECTION_ENAME = "collection"
        const val LIVE_CONCERT_ENAME = "live_concert"
        const val HEARTBEAT_ENAME = "heartbeat"

        const val NAME = "Name"
        const val PHONE = "phone"
        const val IMEI = "IMEI"
        const val DEVICE_MODEL = "device_model"
        const val ADDED_ON = "ADDED_ON"
        const val AGE = "Age"
        const val AGE_18_PLUS = "age 18 plus"
        const val APP_CODE = "App Code"
        const val APP_LANGUAGE = "App Language"
        const val APP_VERSION = "app_version"
        const val BUILD_NUMBER = "Build Number"
        const val VERSION_CODE = "version_code"
        const val AUDIO_QUALITY = "Audio Quality"
        const val CURRENT_CONNECTIVITY = "Current_Connectivity"
        const val CATEGORY_SELECTED = "category selected"
        const val DEVICE_NAME = "device_name"
        const val DEVICE = "Device"
        const val PLATFORM = "Platform"
        const val EMAIL = "email"
        const val UNFAVOURITED_ALBUM = "Unfavourited_Album"
        const val UNFAVOURITED_ARTIST = "Unfavourited_Artist"
        const val UNFAVOURITED_ON_DEMAND_RADIO = "Unfavourited_On Demand Radio"
        const val UNFAVOURITED_PLAYLIST = "Unfavourited_Playlist"
        const val UNFAVOURITED_SONG = "Unfavourited_Song"
        const val UNFAVOURITED_VIDEO = "Unfavourited_Video"
        const val UNFAVOURITED_VIDEO_PLAYLIST = "Unfavourited_Video_Playlist"
        const val FAVOURITED_ALBUM = "Favourited_Album"
        const val FAVOURITED_ARTIST = "Favourited_Artist"
        const val FAVOURITED_ON_DEMAND_RADIO = "Favourited_On Demand Radio"
        const val FAVOURITED_PLAYLIST = "Favourited_Playlist"
        const val FAVOURITED_SONG = "Favourited_Song"
        const val FAVOURITED_VIDEO = "Favourited_Video"
        const val FAVOURITED_VIDEO_PLAYLIST = "Favourited_Video_Playlist"
        const val FIRST_NAME = "first_name"
        const val FREE_TRIAL_TAKEN = "Free Trial Taken"
        const val GENDER = "gender"
        const val INSTALL_SOURCE = "Install Source"
        const val ARTIST_SELECTED = "artist_selected"
        const val LANGUAGES_SELECTED = "Languages Selected"
        const val VIDEO_LANGUAGE_SELECTED = "video_language_selected"
        const val VIDEO_GENRE_SELECTED = "video_genre_selected"
        const val LAST_NAME = "last_name"
        const val LOG_IN_SOURCE = "log_in_source"
        const val LOG_IN_METHOD = "log_in_method"
        const val LOGGED_IN_STATUS = "log_in_status"
        const val MOBILE_ADVERTISING_ID = "Mobile Advertising ID"
        const val MOBILE_CARRIER = "Mobile Carrier"
        const val MUSIC_DOWNLOAD_QUALITY = "Music Download Quality"
        const val MY_AUDIO_PLAYLIST = "My Audio Playlist"
        const val Name = "Name"
        const val NOTIFICATION_TOKEN = "Notification Token"
        const val NPAY_CURRENCY = "npay_currency"
        const val NPAY_EXPIRY = "npay_expiry"
        const val NPAY_FREE_TRIAL_EXPIRY = "npay_free_trial_expiry"
        const val NPAY_IS_AUTORENEWABLE = "npay_is_autorenewable"
        const val NPAY_IS_FREE_TRIAL = "npay_is_free_trial"
        const val NPAY_LAST_CHARGING_DATE = "npay_last_charging_date"
        const val NPAY_LAST_CHARGING_MODE = "npay_last_charging_mode"
        const val NPAY_LAST_PRICE_CHARGED = "npay_last_price_charged"
        const val NPAY_PAYMENTMODE = "npay_paymentmode"
        const val NPAY_PLANID = "npay_planid"
        const val NPAY_PLANNAME = "npay_planname"
        const val NPAY_SUBSCRIBER_TYPE = "npay_subscriber_type"
        const val NPAY_TRANSACTION_MODE = "npay_transaction_mode"
        const val NUMBER_OF_DOWNLOADED_ALBUMS = "Number of Downloaded Albums"
        const val NUMBER_OF_DOWNLOADED_PLAYLISTS = "Number of Downloaded Playlists"
        const val NUMBER_OF_DOWNLOADED_SONGS = "Number of Downloaded Songs"
        const val NUMBER_OF_DOWNLOADED_VIDEOS = "Number of Downloaded Videos"
        const val NUMBER_OF_DOWNLOADED_SHORT_FILM = "Number of Downloaded Short Film"
        const val NUMBER_OF_DOWNLOADED_SHORT_VIDEO = "Number of Downloaded Short Video"
        const val NUMBER_OF_DOWNLOADED_VIDEOS_PLAYLISTS = "Number of Downloaded Videos Playlists"
        const val NUMBER_OF_FAVORITED_ALBUMS = "Number of Favorited Albums"
        const val NUMBER_OF_FAVORITED_ARTISTS = "Number of Favorited Artists"
        const val NUMBER_OF_FAVORITED_PLAYLISTS = "Number of Favorited Playlists"
        const val NUMBER_OF_FAVORITED_SONGS = "Number of Favorited Songs"
        const val NUMBER_OF_FAVORITED_VIDEOS = "Number of Favorited Videos"
        const val NUMBER_OF_LOCAL_SONGS = "Number of Local Songs"
        const val NUMBER_OF_USER_PLAYLISTS = "Number of User Playlists"
        const val OS = "OS"
        const val OS_VERSION = "os_version"
        const val PARTNER_ID = "partner_id"
        const val PARTNER_NAME = "partner_name"
        const val SUBSCRIPTION_END_DATE = "subscription_end_date"
        const val SUBSCRIPTION_PAYMENT_SOURCE = "subscription_payment_source"
        const val SUBSCRIPTION_PAYMENT_SOURCE_DETAIL = "subscription_payment_source_detail"
        const val SUBSCRIPTION_PLAN_TYPE = "subscription_plan_type"
        const val SUBSCRIPTION_START_DATE = "subscription_start_date"
        const val SUBSCRIPTION_STATUS = "subscription_status"
        const val VIDEO_DOWNLOAD_QUALITY = "Video Download Quality"
        const val COIN_BALANCE = "Coin balance"
        const val COINS_REDEEMED = "Coins Redeemed"
        const val MAX_STREAK = "Max streak"
        const val CURRENT_STREAK = "Current streak"
        const val HUNGAMA_UN = "hungama_un"


        const val LOAD_TIME = "load time"
        const val CONCERT_NAME = "concert_name"
        const val CONCERT_ID = "concert_id"
        const val DATE_OF_CONCERT = "date_of_concert"
        const val TIME_OF_CONCERT = "time_of_concert"
        const val PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER = "Full Player"
        const val PLAYERTYPE_EPROPERTY_VALUE_MINI_PLAYER = "Mini Player"
        const val PLAYERTYPE_EPROPERTY_VALUE_PORTRAIT_PLAYER = "portrait_player"
        const val CONSUMPTIONTYPE_ONLINE = "Online"
        const val CONSUMPTIONTYPE_OFFLINE = "Offline"
        const val CONSUMPTIONTYPE_LOCAL = "Local"
        const val VALUE = "value"
        const val TIME_STAMP = "time_stamp"
        const val COINS_EARN = "coins_earn"
        const val COINS_REDEEM = "coins_redeem"
        const val SUBSCRIPTION_POPUP = "subscription_popup"
        const val SUBSCRIPTION_SUCCESSFUL = "subscription_successful"
        const val SUBSCRIPTION_FAILED = "subscription_failed"
        const val RENT_FAILED = "Rent - Fail"
        const val PAYMENT_MODE = "payment_mode"
        const val PLAN_SELECTED = "plan_selected"
        const val SUBSCRIPTION_PLAN_PAGE_OPEN = "subscription_plan_page_open"
        const val PROMO = "promo"
        const val PLAYLIST_NAME = "playlist_name"
        const val PLAYLIST_ID = "playlist_id"
        const val PLAYLIST_DELETED = "Playlist_deleted"
        const val MUSIC_APPS = "music_apps"
        const val VIDEO_OTT_APPS = "video_ott_apps"
        const val PAYMENT_APPS = "payment_apps"
        const val NOTIFICATION_ON = "notification_on"
        const val HE_AVAILABLE = "he_available"
        const val HE_LOGIN = "he_login"

        const val AF_INVITE_ENAME = "af_app_invites"
        const val AF_USER_ID = "userId"
        const val CARPLAY = "carplay"
        const val USER_UNSUCCESSFUL_ATTEMPT_TO_RENT = "user unsuccessful attempt to rent"
        const val PAGESCROLL_SEARCHRECO = "pagescroll_searchreco"
        const val PAGESCROLL_SEARCHRESULTS = "pagescroll_searchresults"
        const val CLICK_SEARCH_BAR = "click_search_bar"
        const val AUTOSUGGEST_DISPLAYED = "autosuggest_displayed"
        const val SEARCH_PERFORMED = "search_performed"
        const val VOICE_SEARCH_CLICKED = "voice_search_clicked"
        const val VOICE_SEARCH_KEYWORD_UTTERED = "voice_search_keyword_uttered"
        const val SEARCH_RESULT_POPULATED = "search_result_populated"
        const val SEEARCH_RESULT_CLICKED = "search_result_clicked"
        const val NO_RESULT_TRY_AGAIN_CLICKED = "no_result_try_again_clicked"
        const val FROM_POSITION = "from_position"
        const val TO_POSITION = "to_position"
        const val RECENT_SEARCH_COUNT = "recent_search_count"
        const val PERFORMED_TYPE = "performed_type"
        const val AUTOSUGGESTION_CLICKED_POSITION = "autosuggestion_clicked_position"
        const val KEYWORD_UTTERED = "keyword_uttered"
        const val NO_OF_RETRY = "no_of retry"
        const val RESULT_COUNT_ALL = "result_count_all"
        const val RESULT_COUNT_SONG = "result_count_song"
        const val RESULT_COUNT_ALBUM = "result_count_album"
        const val RESULT_COUNT_PLAYLIST = "result_count_playlist"
        const val RESULT_COUNT_ARTIST = "result_count_artist"
        const val RESULT_COUNT_MOVIES = "result_count_movies"
        const val RESULT_COUNT_SHORTFILMS = "result_count_shortfilms"
        const val RESULT_COUNT_SHORTVIDEOS = "result_count_shortvideos"
        const val RESULT_COUNT_TVSHOW = "result_count_tvshow"
        const val RESULT_COUNT_LIVEEVENT = "result_count_liveevent"
        const val RESULT_COUNT_PODCAST = "result_count_podcast"
        const val RESULT_COUNT_MUSICVIDEOS = "result_count_musicvideos"
        const val NO_RESULT_FOUND = "no_result_found"
        const val PAGENAME = "pagename"
        const val FILTER_NAME = "filter_name"
        /*new events*/

        const val GAME_NAME = "game_name"
        const val GAME_URL = "game_url"
        const val GAME_VIEW = "game_view"
        const val GAME_TRIGGER = "game_trigger"
        const val GAME_START = "game_start"
        const val GAME_EXIT = "game_exit"
        const val RESPONSE_TIME = "response_time"

        const val TYPENudgeDrawerView = "nudge_drawer_view"
        const val TYPENudgeBannerView = "nudge_banner_view"

        const val NudgeDrawerClick = "nudge_drawer_click"
        const val NudgeBannerClick = "nudge_banner_click"
        const val NudgeDrawerDismiss = "nudge_drawer_dismiss"
        const val freeTrialEligibility = "free_trial_eligibility"
        const val button_text_2 = "button_text_2"
        const val button_text_1 = "button_text_1"
        const val plan_id = "plan_id"
        const val button_text = "button_text"
        const val deeplink = "deeplink"
        const val drawer_old_buy_old = "drawer_old_buyhungamagold"

        const val BannerClick = "Button Click"
        const val secondary_cta_option_selected = "secondary_cta_option_selected"
        const val Hero_card_viewed = "Hero card viewed"

        const val hero_card_position = "hero_card_position"
        const val banner_type = "banner_type"
        const val banner_title = "banner_title"
        const val hero_card_swiped = "hero_card_swiped"
        const val trailer_played = "trailer_played"
        const val ismuted = "ismuted"
        const val Primary_cta = "Primary_cta"
        const val secondary_cta = "secondary_cta"

        const val TOAST_MESSAGE = "toast_popup"
        const val BANDCOLOUR_EPROPERTY = "band_color"
        const val STRINGVALUE_EPROPERTY = "string_value"
        const val APINAME_EPROPERTY = "api_name"


    }
}