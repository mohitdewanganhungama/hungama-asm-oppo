package com.heytap.music.wsdebug

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.hungama.music.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object CommonUtillls {
    fun getFirebaseConfigAdsData(): Unit = runBlocking(Dispatchers.IO) {
        var timeDiff: Long = 0
            try {
                val remoteConfig = Firebase.remoteConfig
                val splashAd = remoteConfig.getString("splash_ad")
                val newUserCoolingPeriod = remoteConfig.getString("new_user_cooling_period")
                val serveDisplayAd = remoteConfig.getBoolean("serve_display_Ad")
                val serveAudioAd = remoteConfig.getBoolean("serve_audio_Ad")
                val servePrerollAd = remoteConfig.getBoolean("serve_preroll_Ad")
                val audioAdPreference = remoteConfig.getString("audio_ad_preference")
                val homescreenBannerAds = remoteConfig.getString("homescreen_banner_ads")
                val munna_mbbs = remoteConfig.getLong("munna_mbbs")
                CommonUtils.setLog(
                    "homescreenBannerAds", " munna " + munna_mbbs.toString() +
                            " splash: " + splashAd + " serveAudioAd: " + serveAudioAd.toString() + " homescreenBannerAds:" + homescreenBannerAds.toString() + " Preroll: " + servePrerollAd.toString()
                )
                val onPlayerOverlay = remoteConfig.getString("on_player_overlay")
                val playlistDetailsPage = remoteConfig.getString("playlist_details_page")
                val videoPlayerPortraitNativeAd =
                    remoteConfig.getString("video_player_portrait_native_ad")
                val podcastDetailsPageNativeAd =
                    remoteConfig.getString("podcast_details_page_native_ad")
                val chartListingScreen = remoteConfig.getString("chart_listing_screen")
                val podcastListingScreen = remoteConfig.getString("podcast_listing_screen")
                val radioListingScreen = remoteConfig.getString("radio_listing_screen")
                val musicVideoListingScreen = remoteConfig.getString("music_video_listing_screen")
                val moviesListingScreen = remoteConfig.getString("movies_listing_screen")
                val tvShowsListingScreen = remoteConfig.getString("tv_shows_listing_screen")
                val he_api = remoteConfig.getString("he_api")
                val drawer_download_all = remoteConfig.getString("drawer_download_all")
                val drawer_download_mymusic = remoteConfig.getString("drawer_download_mymusic")
                val drawer_downloads_exhausted =
                    remoteConfig.getString("drawer_downloads_exhausted")
                val drawer_remove_ads = remoteConfig.getString("drawer_remove_ads")
                val drawer_streaming_quality = remoteConfig.getString("drawer_streaming_quality")
                val drawer_svod_download = remoteConfig.getString("drawer_svod_download")
                CommonUtils.setLog("drawerSvodDownd", " "+drawer_svod_download)
                val drawer_restricted_download =
                    remoteConfig.getString("drawer_restricted_download")
                val drawer_svod_purchase = remoteConfig.getString("drawer_svod_purchase")
                val drawer_svod_tvshow_episode =
                    remoteConfig.getString("drawer_svod_tvshow_episode")
                val nudge_album_banner = remoteConfig.getString("nudge_album_banner")
                val nudge_playlist_banner = remoteConfig.getString("nudge_playlist_banner")
                val nudge_player_banner = remoteConfig.getString("nudge_player_banner")
                val drawer_default_buy_hungama_gold =
                    remoteConfig.getString("drawer_default_buy_hungama_gold")
                val nudge_home_header_banner = remoteConfig.getString("nudge_home_header_banner")
                val enable_payment_drawer =
                    remoteConfig.getBoolean("enable_payment_drawer") ?: false
                val enable_payment_nudge = remoteConfig.getBoolean("enable_payment_nudge") ?: false

            } catch (e: Exception) {
                //setLog("FConfigObject", e.toString())
            }
    }
}