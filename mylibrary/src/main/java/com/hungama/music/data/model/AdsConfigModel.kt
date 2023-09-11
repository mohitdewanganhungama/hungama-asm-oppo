package com.hungama.music.data.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class AdsConfigModel(
    @SerializedName("audio_ad_preference")
    var audioAdPreference: AudioAdPreference = AudioAdPreference(),
    @SerializedName("homescreen_banner_ads")
    var homescreenBannerAds: HomescreenBannerAds = HomescreenBannerAds(),
    @SerializedName("on_player_overlay")
    var onPlayerOverlay: OnPlayerOverlay = OnPlayerOverlay(),
    @SerializedName("serve_audio_Ad")
    var serveAudioAd: Boolean = false,
    @SerializedName("serve_display_Ad")
    var serveDisplayAd: Boolean = false,
    @SerializedName("serve_preroll_Ad")
    var servePrerollAd: Boolean = false,
    @SerializedName("new_user_cooling_period")
    var newUserCoolingPeriod: NewUserCoolingPeriod = NewUserCoolingPeriod(),
    @SerializedName("splash_ad")
    var splashAd: SplashAd = SplashAd(),
    @SerializedName("playlist_details_page")
    var playlistDetailsPage: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("video_player_portrait_native_ad")
    var videoPlayerPortraitNativeAd: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("podcast_details_page_native_ad")
    var podcastDetailsPageNativeAd: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("chart_listing_screen")
    var chartListingScreen: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("podcast_listing_screen")
    var podcastListingScreen: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("radio_listing_screen")
    var radioListingScreen: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("music_video_listing_screen")
    var musicVideoListingScreen: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("movies_listing_screen")
    var moviesListingScreen: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("tv_shows_listing_screen")
    var tvShowsListingScreen: ListingAndDetailPageAds = ListingAndDetailPageAds(),
    @SerializedName("drawer_download_all")
    var drawerDownloadAll: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("drawer_download_mymusic")
    var drawerDownloadMymusic: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("drawer_downloads_exhausted")
    var drawerDownloadsExhausted: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("drawer_remove_ads")
    var drawerRemoveAds: DrawerDownloadAll? = DrawerDownloadAll(),

    @SerializedName("drawer_streaming_quality")
    var drawerStreamingQuality: DrawerDownloadAll? = DrawerDownloadAll(),

    @SerializedName("drawer_svod_download")
    var drawerSvodDownload: DrawerDownloadAll? = DrawerDownloadAll(),

    @SerializedName("drawer_restricted_download")
    var drawerRestrictedDownload: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("drawer_svod_purchase")
    var drawerSvodPurchase: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("drawer_svod_tvshow_episode")
    var drawerSvodTvshowEpisode: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("nudge_playlist_banner")
    var nudge_playlist_banner: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("nudge_player_banner")
    var nudgePlayerBanner: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("nudge_home_header_banner")
    var nudgeHomeHeaderBanner: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("nudge_album_banner")
    var nudgeAlbumBanner: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("drawer_default_buy_hungama_gold")
    var drawer_default_buy_hungama_gold: DrawerDownloadAll = DrawerDownloadAll(),

    @SerializedName("enable_payment_drawer")
    var enablePaymentDrawer: Boolean = false,

    @SerializedName("enable_payment_nudge")
    var enablePaymentNudge: Boolean = false,

    @SerializedName("hero_section_control")
    var hero_section_control: HeroSectionControl = HeroSectionControl(),

    @SerializedName("he_api")
    var he_api: HEApi = HEApi()
) : Parcelable {
    @Keep
    @Parcelize
    data class AudioAdPreference(
        @SerializedName("first_priority")
        var firstPriority: String = "",
        @SerializedName("first_serve")
        var firstServe: Int = 0,
        @SerializedName("next_priority")
        var nextPriority: String = "",
        @SerializedName("serving_frequency")
        var servingFrequency: Int = 0,
        @SerializedName("min_duration")
        var minPlayDurationSeconds: Int = 0
    ) : Parcelable

    @Keep
    @Parcelize
    data class HomescreenBannerAds(
        @SerializedName("display_ad")
        var displayAd: Boolean = false,
        @SerializedName("first_ad_position_after_rows")
        var firstAdPositionAfterRows: Int = 0,
        @SerializedName("repeat_frequency_after_rows")
        var repeatFrequencyAfterRows: Int = 0
    ) : Parcelable

    @Keep
    @Parcelize
    data class OnPlayerOverlay(
        @SerializedName("disappear_time_in_sec")
        var disappearTimeInSec: Int = 0,
        @SerializedName("display_ad")
        var displayAd: Boolean = false,
        @SerializedName("display_again_cooling_time_in_sec")
        var displayAgainCoolingTimeInSec: Int = 0,
        @SerializedName("min_time_after_player_open_in_sec")
        var minTimeAfterPlayerOpenInSec: Int = 0
    ) : Parcelable

    @Keep
    @Parcelize
    data class SplashAd(
        @SerializedName("max_waiting")
        var maxWaiting: Int = 0,
        @SerializedName("splash_type")
        var splashType: String = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class NewUserCoolingPeriod(
        @SerializedName("cooling_days")
        var coolingDays: Int = 0
    ) : Parcelable

    @Keep
    @Parcelize
    data class HEApi(
        @SerializedName("url")
        var url: String = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class ListingAndDetailPageAds(
        @SerializedName("display_ad")
        var displayAd: Boolean = false,
        @SerializedName("first_ad_position_after_rows")
        var firstAdPositionAfterRows: Int = 0,
        @SerializedName("first_ad_position_after_episodes")
        var firstAdPositionAfterEpisodes: Int = 0,
        @SerializedName("repeat_frequency")
        var repeatFrequencyAfterRows: Int = 0
    ) : Parcelable

    @Keep
    @Parcelize
    data class DrawerDownloadAll(
        @SerializedName("nonft")
        var nonft: Nonft? = null,
        @SerializedName("ft")
        var ft: Ft? = null,
    ) : Parcelable

    @Keep
    @Parcelize
    data class Ft(
        @SerializedName("button_text_1")
        var buttonText1: String = "",
        @SerializedName("button_text_2")
        var buttonText2: String = "",
        @SerializedName("plan_id")
        var planId: String = "",
        @SerializedName("image_url")
        var image_url: String = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class Nonft(
        @SerializedName("button_text_1")
        var buttonText1: String = "",
        @SerializedName("button_text_2")
        var buttonText2: String = "",
        @SerializedName("plan_id")
        var planId: String = "",
        @SerializedName("image_url")
        var image_url: String = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class HeroSectionControl(
        @SerializedName("auto_scroll")
        var auto_scroll: String = "false",
        @SerializedName("auto_scroll_time")
        var auto_scroll_time: String = "3.5"
    ) : Parcelable
}