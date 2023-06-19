package com.hungama.music


import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.text.TextUtils
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.work.*
import com.android.volley.VolleyLog
import com.comscore.Analytics
import com.comscore.PublisherConfiguration
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.stetho.Stetho
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.hungama.fetch2.Fetch.Impl.setDefaultInstanceConfiguration
import com.hungama.fetch2.FetchConfiguration
import com.hungama.fetch2.HttpUrlConnectionDownloader
import com.hungama.fetch2core.Downloader
import com.hungama.music.data.database.oldappdata.DatabaseDataMigrationWorker
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventsubscriber.*
import com.hungama.music.utils.AppSignatureHelper
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.DateUtils.getCurrentDate
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException


/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: This class is application level class and attribute.
 */
class HungamaMusicApp : Application() {

    val TAG = javaClass.simpleName
    var activityVisible: Boolean = false
    var isFcmTokenPass: Boolean? = false
    var continueWhereLeftModel: ContinueWhereLeftModel? = null
    var userStreamList = HashMap<String, Long>()
    var userStreamIDList = ArrayList<String>()
    var playableContentList = HashMap<String, EventModel>()
    var cacheBottomTab = HashMap<String, HomeModel>()
    var cacheAdsLoad = HashMap<String, String>()
    val lang = Locale.getDefault().language
     var simpleCache: SimpleCache? = null
     private var isFirstLaunchSong = true
    //var anrWatchDog = ANRWatchDog(10000)
    var duration = 4

    companion object {
        var hungamaMusicApp: HungamaMusicApp? = null
        fun getInstance(): HungamaMusicApp {
            return hungamaMusicApp!!
        }

    }

    fun activityResumed() {
        activityVisible = true
    }

    fun activityPaused() {
        activityVisible = false
    }

    fun deleteCacheData(){
        cacheAdsLoad?.let {
            cacheAdsLoad?.clear()
        }

        cacheBottomTab?.let {
            cacheBottomTab?.clear()
        }
    }

    public fun setCacheAds(page:String, pageName: String){

        if(!cacheAdsLoad?.containsKey(page)!!){
            setLog("cache", "setCacheAds: id:${page} cacheBottomTab size:${cacheAdsLoad?.size}")

        }else{
            setLog("cache", "setCacheAds: duplicate id:${page} cacheBottomTab size:${cacheAdsLoad?.size}")
        }

        cacheAdsLoad?.put(page,pageName)
    }

    public fun getCacheAdsTab(page:String): String?{
        setLog("cache", "getCacheAdsTab 1: page:${page} cacheBottomTab size:${cacheAdsLoad?.size}")
        if(cacheAdsLoad?.containsKey(page)!!){
            return cacheAdsLoad?.get(page)!!
        }
        setLog("cache", "getCacheAdsTab 2: page:${page} cacheBottomTab size:${cacheAdsLoad?.size}")
        return ""
    }

    public fun setCacheBottomTab(page:String, model: HomeModel){

        if(!cacheBottomTab?.containsKey(page)!!){
            setLog("cache", "setCacheBottomTab: id:${page} cacheBottomTab size:${cacheBottomTab?.size}")
            cacheBottomTab?.put(page,model)
        }else{
            setLog("cache", "setCacheBottomTab: duplicate id:${page} cacheBottomTab size:${cacheBottomTab?.size}")
        }

    }

    public fun getCacheBottomTab(page:String): HomeModel?{
        setLog("cache", "getCacheBottomTab 1: page:${page} cacheBottomTab size:${cacheBottomTab?.size}")
        if(cacheBottomTab?.containsKey(page)!!){
           return cacheBottomTab?.get(page)!!
        }
        setLog("cache", "getCacheBottomTab 2: page:${page} cacheBottomTab size:${cacheBottomTab?.size}")
        return null
    }

    public fun setEventData(id:String, model: EventModel){
        setLog(TAG, "setEventDataMainClass: id:$id model:$model")
        playableContentList?.put(id,model)
    }

    public fun getEventData(id:String): EventModel {
        if(playableContentList?.containsKey(id)!!){
            return playableContentList?.get(id)!!
        }
       return EventModel()
    }

    fun <T> allowReads(block: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            return block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    @OptIn(UnstableApi::class) override fun onCreate() {
        //        /**
//         * Logic for app icon change
//         * FreeAlias=> free user
//         * GoldAlias=> Gold user
//         */
//        ChangeAppIconWorker.enqueue(this)

        super.onCreate()
        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            FirebaseApp.initializeApp(this)
            allowReads {  }
            hungamaMusicApp = this@HungamaMusicApp
            FacebookSdk.setIsDebugEnabled(false)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)
            FacebookSdk.fullyInitialize()
            //setAnrLogListener()
            val leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(90 * 1024 * 1024)
            val databaseProvider: DatabaseProvider = ExoDatabaseProvider(this@HungamaMusicApp)
            if (simpleCache == null) {
                simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, databaseProvider)
            }

            // Generate Hash Key >>>>>
            val appSignatureHelper=AppSignatureHelper(this@HungamaMusicApp)
            setLog(TAG, "AppSignatureHashHelper KeyHash: " + appSignatureHelper.appSignatures)
//            Utils.printHashKey(this@HungamaMusicApp)

            FirebaseMessaging.getInstance().isAutoInitEnabled = true
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)


            AppEventsLogger.activateApp(this@HungamaMusicApp);

            if(BuildConfig.DEBUG){
                VolleyLog.DEBUG = true;
            }


            EventManager.getInstance().registerSubscriber(AmplitudeSubscriber())
            EventManager.getInstance().registerSubscriber(MoengageSubscriber(this))
            EventManager.getInstance().registerSubscriber(AppsflyerSubscriber())
//            EventManager.getInstance().registerSubscriber(ApxorSubscriber())
            EventManager.getInstance().registerSubscriber(FirebaseAnalyticSubscriber())


            Stetho.initializeWithDefaults(this@HungamaMusicApp);

            SharedPrefHelper.getInstance()
                .setUserSession(SharedPrefHelper.getInstance().getUserSession()!! + 1)
            val installDate = SharedPrefHelper.getInstance().getUserAppInstallDate()
            if (TextUtils.isEmpty(installDate)){
                SharedPrefHelper.getInstance().setUserAppInstallDate(getCurrentDate())
            }

            Utils.getDeviceId(this@HungamaMusicApp)

            val fetchConfiguration = FetchConfiguration.Builder(this@HungamaMusicApp)
                .enableRetryOnNetworkGain(true)
                .setDownloadConcurrentLimit(3)
                .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.SEQUENTIAL)) // OR
                //.setHttpDownloader(getOkHttpDownloader())
                .build()
            setDefaultInstanceConfiguration(fetchConfiguration)


//            setUpStartAppRemoteConfig()
            comscoreSetUp()

//        sentryInit()

            //TooLargeTool.startLogging(this)
        }catch (e:Exception){
            try {
                if (!BuildConfig.DEBUG){
                    Firebase.crashlytics.recordException(e)
                }else{
                    //throw e
                }
            } catch (e:Exception) {

            }
        }
    }

//    private fun sentryInit() {
//        SentryAndroid.init(
//            this
//        ) { options: SentryAndroidOptions ->
//            options.dsn = "https://795f952de7444d40bdb58db311a3bb87@o1306370.ingest.sentry.io/6548877"
//            options.isAttachThreads=true
//            options.isEnableAutoSessionTracking =true
//            options.sessionTrackingIntervalMillis=60000
//        }
//
//        setLog(TAG, "sentryInit called")
//    }


    private fun comscoreSetUp() {
        val publisher = PublisherConfiguration.Builder()
            .publisherId("14186341")
            .build()

        Analytics.getConfiguration().addClient(publisher)
        Analytics.getConfiguration().enableImplementationValidationMode()
        Analytics.start(applicationContext)
    }


    public fun getContentDuration(contentID:String): Long? {
        if(userStreamList?.containsKey(contentID)!!){
            return userStreamList?.get(contentID)
        }else{
            return 0L
        }
    }


    public fun setContinueWhereLeftData(model: ContinueWhereLeftModel){
        continueWhereLeftModel=model
        CoroutineScope(Dispatchers.IO).launch{
            HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                userStreamList.put(it?.data?.id!!,it?.data?.durationPlay?.toLong()!!)
            }
        }


    }

    public fun getContinueWhereLeftData(): ContinueWhereLeftModel?{
        return continueWhereLeftModel
    }




    private fun setUpStartAppRemoteConfig(){
/*        Firebase.remoteConfig.apply {
            setDefaultsAsync(
                mapOf(
                    "user_login" to "[\"google\", \"facebook\", \"email\", \"apple\"]",
                    "launch_section_discover" to false,
                    "share_user_profile" to false,
                    "deeplink_category_list" to "{\"music\":[\"hindi\",\"english\",\"kannada\",\"tamil\",\"telugu\",\"marathi\",\"haryanavi\",\"bhojpuri\",\"gujarati\",\"sinhala\",\"malayalam\",\"bangla\",\"asamese\",\"rajasthani\",\"oriya\",\"retro\",\"romance\",\"party\",\"pop\",\"happy 90s\",\"devotional\",\"workout\",\"jazz\",\"rock\",\"chill\",\"2000s\",\"classical\",\"meditation\",\"sufi\",\"ghazal\",\"travel\"],\"videos\":[\"romance\"],\"podcasts\":[\"comedy\",\"sports\",\"spiritual\",\"kids\",\"romance\",\"lifestyle\",\"business\",\"news & views\",\"entertainment\",\"horror\",\"devotional crime\",\"science & technology\",\"education\",\"health\",\"literature\",\"motivational\",\"miscellaneous\"],\"music-videos\":[\"romance\"],\"movies\":[\"hindi\",\"tamil\",\"bhojpuri\",\"bengali\",\"english\",\"telugu\",\"malayalam\",\"odia\",\"punjabi\",\"kannada\",\"marathi\"]}",
                    "gc_stream" to "{\"is_display\":true,\"event_name\":\"Stream\",\"popup_type\":\"toast\",\"popup_text\":\"you have earned @coin_amount coins\"}",
                    "gc_added_to_playlist" to "{\"is_display\":true,\"event_name\":\"Added to Playlist\",\"popup_type\":\"popup\",\"popup_text\":\"you have earned @coin_amount coins\"}",
                    "gc_favourited" to "{\"is_display\":true,\"event_name\":\"Favourited\",\"popup_type\":\"toast\",\"popup_text\":\"you have earned @coin_amount coins\"}",
                    "gc_login_success" to "{\"is_display\":true,\"event_name\":\"Login - Success\",\"popup_type\":\"toast\",\"popup_text\":\"login earned you @coin_amount\"}",
                    "gc_rent_open_rent_page" to "{\"is_display\":false,\"event_name\":\"Rent - open Rent Page\",\"popup_type\":\"toast\",\"popup_text\":\"you have earned @coin_amount coins\"}",
                    "gc_added_to_watchlist" to "{\"is_display\":true,\"event_name\":\"Added To WatchList\",\"popup_type\":\"toast\",\"popup_text\":\"You are richer now!, earned @coin_amount for adding to your watchlist\"}",
                    "gc_language_changed" to "{\"is_display\":true,\"event_name\":\"Language Changed\",\"popup_type\":\"toast\",\"popup_text\":\"Exploring makes you richer!, earned @coin_amount coins\"}",
                    "gc_app_launch" to "{\"is_display\":true,\"event_name\":\"App Launch\",\"popup_type\":\"toast\",\"popup_text\":\"Welcome Back!, Token of appreciation @coin_amount coins\"}",
                    "gc_language_selected" to "{\"is_display\":true,\"event_name\":\"Language Selected\",\"popup_type\":\"toast\",\"popup_text\":\"you can be a great DJ!, here is the booking amount - booking @coin_amount coins\"}",
                    "gc_app_language_selected" to "{\"is_display\":true,\"event_name\":\"app_language_selected\",\"popup_type\":\"toast\",\"popup_text\":\"English is Awesome!, granted @coin_amount coins\"}",
                    "gc_movies_language_selected" to "{\"is_display\":true,\"event_name\":\"movies_language_selected\",\"popup_type\":\"toast\",\"popup_text\":\"Great taste!, you can be a movie critic, @coin_amount coins as your first income\"}",
                    "Onboarding_Type" to "onboarding_user_music",
                    "onboarding_user_music" to "{\"first_screen\":{\"number_of_session\":1,\"screen\":\"music_language\"},\"second_screen\":{\"number_of_session\":4,\"screen\":\"music_artist\"},\"third_screen\":{\"number_of_session\":2,\"screen\":\"video_language\"},\"fourth_screen\":{\"number_of_session\":7,\"screen\":\"video_genre\"}}",
                    "onboarding_user_movies" to "{\"first_screen\":{\"number_of_session\":1,\"screen\":\"video_language\"},\"second_screen\":{\"number_of_session\":2,\"screen\":\"video_genre\"},\"third_screen\":{\"number_of_session\":3,\"screen\":\"music_language\"},\"fourth_screen\":{\"number_of_session\":4,\"screen\":\"music_artist\"}}",
                    "splash_ad" to "{\"splash_type\":\"new\",\"max_waiting\":5}",
                    "new_user_cooling_period" to "{\"cooling_days\":7}",
                    "serve_preroll_Ad" to true,
                    "serve_audio_Ad" to true,
                    "serve_display_Ad" to true,
                    "audio_ad_preference" to "{\"first_priority\":\"Google\",\"next_priority\":\"triton\",\"first_serve\":2,\"serving_frequency\":3,\"min_duration\":30}",
                    "homescreen_banner_ads" to "{ \"display_ad\":true, \"first_ad_position_after_rows\":3, \"repeat_frequency_after_rows\":4}",
                    "on_player_overlay" to "{  \"display_ad\":true,  \"min_time_after_player_open_in_sec\":60,  \"disappear_time_in_sec\":15,  \"display_again_cooling_time_in_sec\":10}",
                    "playlist_details_page" to "{\"display_ad\":true,\"first_ad_position_after_rows\":4,\"repeat_frequency\":7}",
                    "video_player_portrait_native_ad" to "{\"display_ad\":true,\"first_ad_position_after_rows\":3,\"repeat_frequency\":4}",
                    "podcast_details_page_native_ad" to "{\"display_ad\":true,\"first_ad_position_after_episodes\":3,\"repeat_frequency\":4}",
                    "chart_listing_screen" to "{\"display_ad\":true,\"first_ad_position_after_rows\":4,\"repeat_frequency\":5}",
                    "podcast_listing_screen" to "{\"display_ad\":true,\"first_ad_position_after_rows\":4,\"repeat_frequency\":5}",
                    "radio_listing_screen" to "{\"display_ad\":true,\"first_ad_position_after_rows\":3,\"repeat_frequency\":4}",
                    "music_video_listing_screen" to "{\"display_ad\":true,\"first_ad_position_after_rows\":3,\"repeat_frequency\":4}",
                    "movies_listing_screen" to "{\"display_ad\":true,\"first_ad_position_after_rows\":3,\"repeat_frequency\":4}",
                    "tv_shows_listing_screen" to "{\"display_ad\":true,\"first_ad_position_after_rows\":3,\"repeat_frequency\":4}",
                    "he_api" to "{\"url\":\"\"}"
                )
            )
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 1
            })
        }*/
        RemoteConfigFetcherWorker.enqueue(this)

        /*val remoteConfig = Firebase.remoteConfig
        val add = remoteConfig.getString("splash_ad")
        val add2 = remoteConfig.getString("new_user_cooling_period")
        val add3 = remoteConfig.getBoolean("serve_display_Ad")
        val add4 = remoteConfig.getBoolean("serve_audio_Ad")
        val add5 = remoteConfig.getBoolean("serve_preroll_Ad")
        setLog("RemoteWorkDone-1.1", add)
        setLog("RemoteWorkDone-2.2", add2)
        setLog("RemoteWorkDone-3.3", add3.toString())
        setLog("RemoteWorkDone-4.4", add4.toString())
        setLog("RemoteWorkDone-5.5", add5.toString())*/

        DatabaseDataMigrationWorker.enqueue(this)
    }

    class RemoteConfigFetcherWorker(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams) {
        companion object {

            fun enqueue(context: Context) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequestBuilder =
                    OneTimeWorkRequestBuilder<RemoteConfigFetcherWorker>().setConstraints(
                        constraints
                    )

                WorkManager.getInstance(context).enqueueUniqueWork(
                    RemoteConfigFetcherWorker::class.java.simpleName,
                    ExistingWorkPolicy.KEEP, workRequestBuilder.build()
                )
            }
        }

        override suspend fun doWork(): Result {
            return try {


                MobileAds.initialize(HungamaMusicApp.getInstance().applicationContext)
                Utils.adTestUserEnable()

                // Block on the task for a maximum of 60 seconds, otherwise time out.
                //Tasks.await(Firebase.remoteConfig.fetchAndActivate(), 60, TimeUnit.SECONDS)
                setLog("RemoteWorkDone", "setUpRemoteConfig: 1")
                    try {
                        setLog("RemoteWorkDone", "setUpRemoteConfig: 1.1")
                        CoroutineScope(Dispatchers.IO).launch{
                            setLog("RemoteWorkDone", "setUpRemoteConfig: 1.2")
//                            Firebase.remoteConfig.fetchAndActivate()
                            setLog("RemoteWorkDone", "setUpRemoteConfig: 1.3")
                        }
                    }catch (e:Exception){
                        setLog("RemoteWorkDone", "setUpRemoteConfig: 1.4")
                    }
                setLog("RemoteWorkDone", "setUpRemoteConfig: 1.5")

                //setLog("RemoteWorkDone", "setUpRemoteConfig: 2")

                /*val remoteConfig = Firebase.remoteConfig
                Firebase.remoteConfig.apply {
                    fetchAndActivate().addOnCompleteListener { task ->
                        val updated = task.result
                        if (task.isSuccessful) {
                            val updated = task.result
                            setLog("RemoteWorkDone-0", "Config params updated: $updated")
                        } else {
                            setLog("RemoteWorkDone-0", "Config params updated: $updated")
                        }


                        val add = remoteConfig.getString("splash_ad")
                        val add2 = remoteConfig.getString("new_user_cooling_period")
                        val add3 = remoteConfig.getBoolean("serve_display_Ad")
                        val add4 = remoteConfig.getBoolean("serve_audio_Ad")
                        val add5 = remoteConfig.getBoolean("serve_preroll_Ad")
                        setLog("RemoteWorkDone-1.1", add)
                        setLog("RemoteWorkDone-2.2", add2)
                        setLog("RemoteWorkDone-3.3", add3.toString())
                        setLog("RemoteWorkDone-4.4", add4.toString())
                        setLog("RemoteWorkDone-5.5", add5.toString())

                    }

                    fetchAndActivate().addOnFailureListener {
                        setLog("TAG", "FirebaseRemoteConfig addOnFailureListener: ${it.message}")
                    }
                }*/

                /*val remoteConfig = Firebase.remoteConfig
                val add = remoteConfig.getString("splash_ad")
                val add2 = remoteConfig.getString("new_user_cooling_period")
                val add3 = remoteConfig.getBoolean("serve_display_Ad")
                val add4 = remoteConfig.getBoolean("serve_audio_Ad")
                val add5 = remoteConfig.getBoolean("serve_preroll_Ad")
                setLog("RemoteWorkDone-1", add)
                setLog("RemoteWorkDone-2", add2)
                setLog("RemoteWorkDone-3", add3.toString())
                setLog("RemoteWorkDone-4", add4.toString())
                setLog("RemoteWorkDone-5", add5.toString())*/
                return Result.success()
            } /*catch (e: ExecutionException) {
                // The Task failed, this is the same exception you'd get in a non-blocking failure handler.
                return if (e.cause is FirebaseRemoteConfigClientException && e.cause?.cause is IOException) {
                    Result.retry()
                } else {
                    // TODO Log the error here
                    Result.failure()
                }
            }*/ catch (e: InterruptedException) {
                // An interrupt occurred while waiting for the task to complete.
                return Result.retry()
            } catch (e: TimeoutException) {
                // Task timed out before it could complete.
                return Result.retry()
            }
        }
    }

    fun getIsFirstLaunchSong(): Boolean {
        return isFirstLaunchSong
    }

    fun setIsFirstLaunchSong(status:Boolean){
        setLog("setIsFirstLaunchSong", "HungamaMusicApp-before-isFirstLaunchSong-$isFirstLaunchSong")
        isFirstLaunchSong = status
        setLog("setIsFirstLaunchSong", "HungamaMusicApp-after-isFirstLaunchSong-$isFirstLaunchSong")
    }

    private fun setAnrLogListener(){
        /*if (!BuildConfig.DEBUG){
            anrWatchDog = ANRWatchDog(10000)
        }
        anrWatchDog
            .setANRListener { error ->
                setLog("ANRWatchDogCrash",  "setANRListener-Detected Application Not Responding!")
                try {
                    Firebase.crashlytics.recordException(error)
                } catch (e:Exception) {

                }
                setLog("ANRWatchDogCrash",  "Error was successfully serialized")
                if (BuildConfig.DEBUG){
                    //This will throw an error
                        // Don't remove this
                            //You have to solve this error.

                    throw error
                }
            }
            .setANRInterceptor { duration ->
                val ret: Long = this@HungamaMusicApp.duration * 1000 - duration
                if (ret > 0) setLog("ANRWatchDogCrash",
                    "setANRInterceptor-Intercepted ANR that is too short ($duration ms), postponing for $ret ms."
                )
                ret
            }
        anrWatchDog.start()
        //ANRWatchDog().setIgnoreDebugger(true).start()
        //ANRWatchDog().setReportThreadNamePrefix("APP:").start()
        //ANRWatchDog().setReportMainThreadOnly().start()*/
    }

//    private fun strictMode() {
//        //    https://developer.android.com/reference/android/os/StrictMode
//        //    StrictMode is a developer tool which detects things you might be doing by accident and
//        //    brings them to your attention so you can fix them.
//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(
//                StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectAll()
//                    .penaltyLog()
//                    .build()
//            )
//            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .penaltyDeath()
//                .build())
//        }
//    }
}