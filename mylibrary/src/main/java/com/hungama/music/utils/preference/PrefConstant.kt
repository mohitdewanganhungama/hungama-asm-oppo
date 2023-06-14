package com.hungama.music.utils.preference

interface PrefConstant {
    companion object {
        //const val ISLOGIN = "ISLOGIN"
        const val ISLOGIN = "real_user"
        const val ISGUEST = "ISGUEST"
        const val IS_ON_BOARDING = "is_on_boarding"
        //const val USER_ID = "userID"
        const val USER_ID = "partner_user_id"
        const val USER_EMAIL = "user_email"
        const val USER_MOBILE = "user_mobile"
        //const val LOGIN_USER_ID = "LOGIN_USER_ID"
        //const val SILENT_USER_ID = "SILENT_USER_ID"
        const val SILENT_USER_ID = "silent_partner_user_id"
        const val USER_NAME = "user_name"
        const val HANDLE_NAME = "handle_name"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val USER_GENDER = "user_gender"
        const val USER_DOB = "user_dob"
        const val USER_SHARE = "user_share"
        const val LAST_VIDEO_QUALITY = "LAST_VIDEO_QUALITY"
        const val USER_IMAGE = "user_image"
        const val USER_COIN = "user_coin"
        const val LAST_PLAY_SONG_ID = "LAST_PLAY_SONG_ID"
        const val LAST_PLAY_SONG_NAME = "LAST_PLAY_SONG_NAME"
        //const val GUEST_ID = "guest_id"
        //const val GUEST_GUID = "guest_guid"
        //const val TOKEN = "token"

        //const val USER_SLECTED_AVATAR = "USER_SLECTED_AVATAR"
        const val USER_PAY_DATA = "USER_PAY_DATA"
        const val USER_DATA = "USER_DATA"
        const val LANG_DATA = "LANG_DATA"

        const val CHOOES_LANGUAGE = "CHOOES_LANGUAGE"


        const val PREFERENCE_LANGUAGE = "PREFERENCE_LANGUAGE"
        const val PREFERENCE_LANGUAGE_TITLE = "PREFERENCE_LANGUAGE_TITLE"
        const val PREFERENCE_MUSIC_LANGUAGE = "PREFERENCE_MUSIC_LANGUAGE"
        const val PREFERENCE_MUSIC_ARTIST = "PREFERENCE_MUSIC_ARTIST"
        const val PREFERENCE_VIDEO_LANGUAGE = "PREFERENCE_VIDEO_LANGUAGE"
        const val PREFERENCE_VIDEO_GENRE = "PREFERENCE_VIDEO_GENER"
        const val PREFERENCE_MOOD_RADIO_MOOD_FILTER_ID = "PREFERENCE_MOOD_RADIO_MOOD_FILTER_ID"
        const val PREFERENCE_MOOD_RADIO_MOOD_FILTER_TITLE = "PREFERENCE_MOOD_RADIO_MOOD_FILTER_TITLE"
        const val PREFERENCE_MOOD_RADIO_TEMPO_FILTER_ID = "PREFERENCE_MOOD_RADIO_TEMPO_FILTER_ID"
        const val PREFERENCE_MOOD_RADIO_TEMPO_FILTER_TITLE = "PREFERENCE_MOOD_RADIO_TEMPO_FILTER_TITLE"
        const val PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_ID = "PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_ID"
        const val PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_TITLE = "PREFERENCE_MOOD_RADIO_LANGUAGE_FILTER_TITLE"
        const val PREFERENCE_MOOD_RADIO_ERA_FILTER_MIN_RANGE = "PREFERENCE_MOOD_RADIO_ERA_FILTER_MIN_RANGE"
        const val PREFERENCE_MOOD_RADIO_ERA_FILTER_MAX_RANGE = "PREFERENCE_MOOD_RADIO_ERA_FILTER_MAX_RANGE"
        const val PREFERENCE_APPLE_CLIENT_SECRET = "PREFERENCE_APPLE_CLIENT_SECRET"
        const val PREFERENCE_APPLE_REFRESH_TOKEN = "PREFERENCE_APPLE_REFRESH_TOKEN"
        const val PREFERENCE_APPLE_VERIFY_REFRESH_TOKEN_TIMER = "PREFERENCE_APPLE_VERIFY_REFRESH_TOKEN_TIMER"
        const val PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_ID = "PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_ID"
        const val PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_TITLE = "PREFERENCE_MUSIC_PLAYBACK_STREAM_QUALITY_TITLE"
        const val PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_ID = "PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_ID"
        const val PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_TITLE = "PREFERENCE_MUSIC_PLAYBACK_DOWNLOAD_QUALITY_TITLE"
        const val PREFERENCE_USER_SESSION = "PREFERENCE_USER_SESSION"
        const val PREFERENCE_USER_CURRENT_SUBSCRIPTION_PLAN = "PREFERENCE_USER_CURRENT_SUBSCRIPTION_PLAN"
        const val PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_ID = "PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_ID"
        const val PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_TITLE = "PREFERENCE_VIDEO_PLAYBACK_STREAM_QUALITY_TITLE"
        const val PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_ID = "PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_ID"
        const val PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_TITLE = "PREFERENCE_VIDEO_PLAYBACK_DOWNLOAD_QUALITY_TITLE"
        const val PREFERENCE_USER_APP_INSTALL_DATE = "PREFERENCE_USER_APP_INSTALL_DATE"
        const val PREFERENCE_LAST_CONNECTED_DEVICE_NAME = "PREFERENCE_LAST_CONNECTED_DEVICE_NAME"
        const val PREFERENCE_USER_ORDERS = "PREFERENCE_USER_ORDERS"
        const val PREFERENCE_USER_CENSOR_RATING = "PREFERENCE_USER_CENSOR_RATING"

        const val SLEEP_TIMER = "SLEEP_TIMER"
        const val PRODUCT_LIST = "PRODUCT_LIST"
        //const val PREFERENCE_MUSIC_CODE_LIST = "PREFERENCE_MUSIC_CODE_LIST"
        const val PREFERENCE_MUSIC_TITLE_LIST = "PREFERENCE_MUSIC_TITLE_LIST"
        const val PREFERENCE_VIDEO_TITLE_LIST = "PREFERENCE_VIDEO_TITLE_LIST"
        var STREAM_10_VIDEO_MAP = "stream_10_video_map_"+SharedPrefHelper.getInstance().getUserId()
        var STREAM_20_MAP = "stream_20_map_"+SharedPrefHelper.getInstance().getUserId()
        var STREAM_20 = "stream_20_"+SharedPrefHelper.getInstance().getUserId()
        var STREAM_10_VIDEO = "stream_10_video_"+SharedPrefHelper.getInstance().getUserId()
        var FIRST_STREAM = "first_stream_"+SharedPrefHelper.getInstance().getUserId()
        var FIRST_STREAM_VIDEO = "first_stream_video_"+SharedPrefHelper.getInstance().getUserId()
        var FIRST_DOWNLOAD = "first_download_"+SharedPrefHelper.getInstance().getUserId()
        var FIRST_DOWNLOAD_VIDEO = "first_download_video_"+SharedPrefHelper.getInstance().getUserId()
        var COMPLETED_20_FREE_DOWNLOADS = "completed_20_free_downloads_"+SharedPrefHelper.getInstance().getUserId()
        const val AF_COMPLETE_REGISTRATION = "af_complete_registration"
        const val AF_REFERRER_CUSTOMER_ID = "af_referrer_customer_id"
        const val USER_SHOPIFY_ID = "userShopifyID"
        const val USER_IS_DISPLAY_DISCOVER = "isDisplayDiscover"
        const val IS_PAUSE = "isPause"
        const val RECENT_SEARCH_LIST = "RECENT_SEARCH_LIST"
        const val TOTAL_DOWNLOADED_AUDIO_CONTENT = "TOTAL_DOWNLOADED_AUDIO_CONTENT"
        const val USER_LAST_SHIPPING_DETAILS = "USER_LAST_SHIPPING_DETAILS"
        const val IS_Notification_ENABLE = "IS_Notification_ENABLE"
        const val IS_HE_AVAILABLE = "IS_HE_AVAILABLE"
        const val IS_HE_LOGIN = "IS_HE_LOGIN"

        //Migration app keys
        const val loginstatus = "loginstatus"
        const val login_method = "login_method"
        const val terms_accepted_v1_new = "terms_accepted_v1_new"
        const val is_mi_user = "is_mi_user"
        const val real_user = "real_user"
        const val is_user_registered = "is_user_registered"
        const val is_silent_user_registered = "is_silent_user_registered"
        const val user_login_phone_numnber = "user_login_phone_numnber"
        const val silent_user_login_phone_numnber = "silent_user_login_phone_numnber"

        const val partner_user_id = "partner_user_id"
        const val silent_partner_user_id = "silent_partner_user_id"

        const val gigya_login_session_token = "gigya_login_session_token"
        const val silent_user_gigya_login_session_token = "silent_user_gigya_login_session_token"
        const val gigya_login_session_secret = "gigya_login_session_secret"
        const val silent_user_gigya_login_session_secret = "silent_user_gigya_login_session_secret"
        const val silent_user_session_id = "silent_user_session_id"
        const val gametoken = "gametoken"

        const val mobile = "mobile"
        const val hungama_email = "hungama_email"
        const val hungama_first_name = "hungama_first_name"
        const val hungama_last_name = "hungama_last_name"

        const val loginsource = "loginsource"
        const val gigya_fb_thumb_url = "gigya_fb_thumb_url"
        const val gigya_fb_first_name = "gigya_fb_first_name"
        const val gigya_fb_last_name = "gigya_fb_last_name"
        const val gigya_fb_EMAIL = "gigya_fb_EMAIL"
        const val gigya_google_first_name = "gigya_google_first_name"
        const val gigya_google_last_name = "gigya_google_last_name"
        const val gigya_google_email = "gigya_google_email"
        const val prakash = "prakash"
        const val setlanguage = "setlanguage"
        const val app_language_code = "app_language_code"
        const val user_selected_language_text = "user_selected_language_text"
        const val user_language_list = "user_language_list"
        const val backgroundActivity= "BackgroundActivity"

        const val Download_Limit = "download_limit"
        //

    }
}