package com.hungama.music.utils

import android.annotation.SuppressLint
import android.app.*
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.ClipboardManager
import android.content.Intent.EXTRA_CHOSEN_COMPONENT
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.hardware.display.DisplayManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.palette.graphics.Palette
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import com.appsflyer.CreateOneLinkHttpTask
import com.appsflyer.share.ShareInviteHelper
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.hungama.fetch2.Status
import com.hungama.fetch2.encryptiondecryption.CMEncryptor2
import com.hungama.fetch2.util.DEVICE_ID
import com.hungama.music.BuildConfig
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.eventanalytic.eventreporter.PreviewStreamTriggerEvent
import com.hungama.music.eventanalytic.eventreporter.StreamTriggerEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.TracklistDataModel
import com.hungama.music.player.download.DownloadTracker
import com.hungama.music.player.download.IntentUtil
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseActivity.Companion.getIsGoldUser
import com.hungama.music.ui.base.BaseActivity.Companion.setTrackListData
import com.hungama.music.ui.main.view.activity.CommonWebViewActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.ParentalControlPopup
import com.hungama.music.ui.main.view.fragment.SubscriptionDialogBottomsheetFragment
import com.hungama.music.ui.main.view.fragment.SubscriptionDialogBottomsheetFragmentFreeMinute
import com.hungama.music.ui.main.view.fragment.UserCensorRatingPopup
import com.hungama.music.utils.Constant.ADS_FREE
import com.hungama.music.utils.Constant.CONTENT_ORDER_STATUS_FAIL
import com.hungama.music.utils.Constant.CONTENT_ORDER_STATUS_IN_PROCESS
import com.hungama.music.utils.Constant.CONTENT_ORDER_STATUS_NA
import com.hungama.music.utils.Constant.CONTENT_ORDER_STATUS_PENDING
import com.hungama.music.utils.Constant.CONTENT_ORDER_STATUS_SUCCESS
import com.hungama.music.utils.Constant.GOLD
import com.hungama.music.utils.Constant.GOLD_WITH_ADS
import com.hungama.music.utils.Constant.GOLD_X_CONCERT
import com.hungama.music.utils.Constant.GOLD_X_TVOD
import com.hungama.music.utils.Constant.GOLD_X_TVOD_X_CONCERT
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_ADS_FREE
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_FREE
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_WITH_ADS
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_X_CONCERT
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_X_TVOD
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_X_TVOD_X_CONCERT
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_TVOD
import com.hungama.music.utils.Constant.SubscriptionProduct
import com.hungama.music.utils.Constant.TVOD
import com.hungama.music.utils.Constant.default_music_language_code
import com.hungama.music.utils.Constant.default_music_language_title
import com.hungama.music.utils.Constant.default_video_language_code
import com.hungama.music.utils.Constant.default_video_language_title
import com.hungama.music.utils.Constant.digitalProduct
import com.hungama.music.utils.Constant.physicalProduct
import com.hungama.music.utils.Constant.tvodProduct
import com.hungama.music.utils.DateUtils.*
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.utils.customview.fontview.DroidAwesome
import com.hungama.music.utils.download.Data
import com.hungama.music.utils.fontmanger.FontDrawable
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.fr_main.*
import kotlinx.android.synthetic.main.fr_my_playlist_detail.*
import kotlinx.android.synthetic.main.fragment_chart_detail_v1.*
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Math.abs
import java.lang.reflect.Type
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.text.CharacterIterator
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.text.StringCharacterIterator
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random


object CommonUtils {

    private val TAG = CommonUtils::class.java.name

    private var cache: Cache? = null

    fun getUrlWithoutParameters(url: String): String? {
        try {
            val uri = URI(url)
            return URI(
                uri.scheme,
                uri.authority,
                uri.path,
                null,  // Ignore the query part of the input url
                uri.fragment
            ).toString()
        } catch (exp: Exception) {
            exp.printStackTrace()
            return url
        }

    }

    @OptIn(UnstableApi::class)
    @Synchronized
    fun getCache(context: Context): Cache? {
        if (cache == null) {
            val cacheDirectory = File(context.getExternalFilesDir(null), "pods")
            cache = SimpleCache(cacheDirectory, NoOpCacheEvictor())
        }
        return cache
    }

    fun PageViewEvent(
        id: String,
        title: String,
        type: String,
        sourceDetails: String,
        sourcePageName: String,
        pageName: String,
        topNavPosition: String
    ) {
        val hashMapPageView = HashMap<String, String>()

        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] = type
        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = id
        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = title
        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = sourceDetails
        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] = sourcePageName
        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = pageName
        hashMapPageView[EventConstant.TOP_NAV_POSITION_EPROPERTY] = topNavPosition

        setLog("PageViewEvent", hashMapPageView.toString())
        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
    }

    fun PageViewEventTab(
        id: String,
        title: String,
        type: String,
        sourceDetails: String,
        sourcePageName: String,
        pageName: String,
        topNavPosition: String,
        pageType: String
    ) {
        val hashMapPageView = HashMap<String, String>()

        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] = type
        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = id
        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = title
        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = sourceDetails
        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] = sourcePageName
        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = pageName
        hashMapPageView[EventConstant.SOURCEPAGETYPE_EPROPERTY] = pageType
        hashMapPageView[EventConstant.TOP_NAV_POSITION_EPROPERTY] = topNavPosition

        setLog("PageViewEvent", hashMapPageView.toString())
        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
    }

    fun PageViewEventMore(
        id: String,
        title: String,
        type: String,
        sourceDetails: String,
        sourcePageName: String,
        pageName: String,
        topNavPosition: String
    ) {
        val hashMapPageView = HashMap<String, String>()

        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] = type
        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = id
        hashMapPageView[EventConstant.SOURCEPAGETYPE_EPROPERTY] = sourceDetails
        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] = sourcePageName
        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = pageName
        hashMapPageView[EventConstant.SOURCEPAGETYPE_EPROPERTY] = "listing"
        hashMapPageView[EventConstant.TOP_NAV_POSITION_EPROPERTY] = topNavPosition

        setLog("PageViewEvent", hashMapPageView.toString())
        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
    }

    fun getUserCarrier(context: Context): String? {
        if (isSIMInserted(context)) {
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val simOperator = tm.simOperatorName
                if (simOperator != null) {
                    return simOperator
                } else {
                    return "No Sim"
                }
            } catch (e: Exception) {
                setLog(TAG + e.message, e.message.toString())
            }
        } else {
            return "No Sim"
        }
        return null
    }

    fun isSIMInserted(context: Context): Boolean {
        return TelephonyManager.SIM_STATE_ABSENT != (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simState
    }

    fun getVideoDummyData(): MutableList<Track> {
        val songsList = ArrayList<Track>()
//        val objSongList = MusicModel()
//        objSongList.title = "Video 1"
//        objSongList.url = "https://85mum-content.hungama.com/1218/4/FF-2016-00001917/stream.mpd"
//        objSongList.drmlicence = "https://wv.service.expressplay.com/hms/wv/rights/?ExpressPlayToken=BQAAAA9JKcEAJDM5NjQ4NTYyLTBjZmQtNGM2OS1hYTZiLWRhMWE0ZDVkZWY1ZgAAAGBKP7nN7Om_pniOaQW77rRsU9c9NeGtlg4J-aRB7T2i37jG5MqUvpHwDuCXbxepG-QHtWK4TpctHx6xn5YL1V7Vcr0IfOX3t0jwHJFTULIYYJD0GtJhJlzzXoF3jW2ZjYsrtwi0DN7xS6FF7mD6VAMxZpTthg"
//        objSongList.subTitle = "Artist 1, Artist 2, Artist 3" + ", stream.mpd"
//        objSongList.drmlicence = ""
//        songsList.add(objSongList)

        /*val objSongList2 = MusicModel()
        objSongList2.title = "Video 2"
        objSongList2.url = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"
        objSongList2.drmlicence = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"
        objSongList2.subTitle = "Artist 1, Artist 2" + ", 105560.mpd"
        objSongList2.drmlicence = ""
        songsList.add(objSongList2)*/

        val track2 = Track()
        track2.id = 1
        track2.title = "Video 2"
        track2.subTitle = ""
        track2.image = ""
        //track2.url = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"
        track2.url =
            "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        track2.drmlicence = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"
        track2.albumArtBitmap = null
        track2.defaultAlbumArtRes = R.drawable.user_image
        songsList.add(track2)

//        val objSongList3 = MusicModel()
//        objSongList3.title = "Video 3"
//        objSongList3.url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
//        objSongList3.subTitle = "Artist 1, Artist 2" + ", BigBuckBunny_320x180.mp4"
//        songsList.add(objSongList3)
//
//        val objSongList4 = MusicModel()
//        objSongList4.title = "Video 4"
//        objSongList4.url = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd"
//        objSongList4.subTitle = "Artist 1, Artist 2, Artist 3" + ", 11331.mpd"
//        objSongList4.drmlicence = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"
//        songsList.add(objSongList4)
//
//        val objSongList5 = MusicModel()
//        objSongList5.title = "Video 5"
//        objSongList5.url = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"
//        objSongList5.subTitle = "Artist 1, Artist 2, Artist 3" + ", 11331.mpd"
//        objSongList5.drmlicence = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"
//        songsList.add(objSongList5)
//
//
//        val objSongList6 = MusicModel()
//        objSongList6.title = "Video 6"
//        objSongList6.url = "https://85mum-content.hungama.com/1524/FF-2015-00000079/stream.mpd"
//        objSongList6.subTitle = "Artist 1, Artist 2, Artist 3" + ", stream.mpd"
//        objSongList6.drmlicence = "https://wv.service.expressplay.com/hms/wv/rights/?ExpressPlayToken=BQAAAA9JKa8AJDM5NjQ4NTYyLTBjZmQtNGM2OS1hYTZiLWRhMWE0ZDVkZWY1ZgAAAGBvRonIpLtjgnHS-rSzA9KCoW9hcW1yYZE1Ba7t4X1b6D3UkXAFrYxyJoTmKPm7O5KfT4d2hGGB9oQKMmEFlfIxQbVd7bFdFiJ_bmHoFm4jwkl8kOfYDNgYmDJJPsQzeYTyz8mIFkXgSYIDNhAKQJcOPi0ScA"
//        songsList.add(objSongList6)
        return songsList
    }

    fun getVideoDummyData2(url: String): ArrayList<MusicModel> {
        val songsList = ArrayList<MusicModel>()

        val objSongList3 = MusicModel()
        objSongList3.title = "EP 02 - The Inventory"
//        objSongList3.url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        if (!TextUtils.isEmpty(url)) {
//            objSongList3.url = url
            objSongList3.url =
                "https://hunstream.hungama.com/c/5/c4c/a0d/66734379/66734379_,100,400,750,1000,1600,.mp4.m3u8?uSCASQqqoxwJTk4xT2rgf_j9kV5zzYg66ygkiZuUkX1HM9fRWHldMk9ueAXR3Vsiwe59_N-5CLlDSCP-RBL7J9Yslz85VPm37y0Uev9lq3S6mOqapYCHFC3W_mMb"
        } else {
            objSongList3.url =
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        }


        objSongList3.subTitle = "OLD MDN"
        objSongList3.drmlicence = ""
        songsList.add(objSongList3)

//        val objSongList2 = MusicModel()
//        objSongList2.title = "Lord ganesha"
//        objSongList2.url = "https://85mum-content.hungama.com/1524/EVN201700435-1/stream.mpd"
//        objSongList2.drmlicence = "https://wv.service.expressplay.com/hms/wv/rights/?ExpressPlayToken=BQAAAA9JKZUAJDM5NjQ4NTYyLTBjZmQtNGM2OS1hYTZiLWRhMWE0ZDVkZWY1ZgAAAGAn0YiM3yyarfFCGqZtGGS1rzGn9xYXdWO9GmCCxD14gOmgxxzInW7E5kdkJBdjsBD7qKC3W3RKfqZK6zVcjiZixr3TDdciXHyfS-fgu2gv9XsMz4bK4Yo5wQoN6yyGi817uuItqafBoNRjF8AuJU5CxT9THQ"
//        objSongList2.subTitle = "Artist 1"
//        songsList.add(objSongList2)


        return songsList
    }

    fun setTrackList(
        context: Context,
        songData: BodyDataItem?,
        allSongs: List<BodyRowsItemsItem?>,
        heading: String?,
        childPosition: Int
    ) {
        val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(context) }
        val songList = ArrayList<Track>()
        val track2 = Track()
        track2.id = 2
        //track2.title = songData?.title
        track2.title = "Lut Gaye"
        //track2.subTitle = songData?.subTitle
        track2.subTitle = "Jubin Nautiyal,Tanishk Bagchi"
        //track2.image = songData?.image
        track2.image = "https://stg-api.test.hungamagames.com/master/tempfiles/Jubin.jpg"
        //track2.url = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3"
        //track2.url = "https://townsquare.media/site/189/files/2011/03/Adele-RollingInTheDeep.mp3"
        track2.url = "https://stg-api.test.hungamagames.com/master/tempfiles/Wafa-Na-Raas-Aayee.mp3"
        track2.songLyricsUrl =
            "htts://stg-api.test.hungamagames.com/master/tempfiles/Wafa-Na-Raas-Aayee.lrc"
        /*track2.url = "http://townsquare.media/site/189/files/2011/03/Adele-RollingInTheDeep.mp3"
        track2.songLyricsUrl = "https://prakashp.000webhostapp.com/Adele-Rolling-in-the-Deep.lrc"*/
        track2.drmlicence = ""
        track2.albumArtBitmap = null
        track2.defaultAlbumArtRes = R.drawable.user_image
        track2.heading = "Best of Jubin nautiyal"
        track2.artistName = "Jubin Nautiyal,Tanishk Bagchi"
        track2.playerType = songData!!.type
        songList.add(track2)

        if (allSongs.size > childPosition + 1) {
            val track = Track()
            track.id = 1
            track.title = allSongs.get(childPosition + 1)!!.data!!.title
            track.subTitle = allSongs.get(childPosition + 1)!!.data!!.subTitle
            track.image = allSongs.get(childPosition + 1)!!.data!!.playble_image
            //track.url = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
            track.url = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3"
            track.songLyricsUrl = ""
            track.drmlicence = ""
            track.albumArtBitmap = null
            track.defaultAlbumArtRes = R.drawable.user_image
            track.heading = heading
            track.playerType = songData.type
            songList.add(track)
        }

        if (allSongs.size > childPosition + 2) {
            val track3 = Track()
            track3.id = 3
            track3.title = allSongs.get(childPosition + 2)!!.data!!.title
            track3.subTitle = allSongs.get(childPosition + 2)!!.data!!.subTitle
            track3.image = allSongs.get(childPosition + 2)!!.data!!.image
            track3.url =
                "https://storage.googleapis.com/uamp/Kai_Engel_-_Irsens_Tale/09_-_Outro.mp3"
            track3.songLyricsUrl = ""
            track3.drmlicence = ""
            track3.albumArtBitmap = null
            track3.defaultAlbumArtRes = R.drawable.user_image
            track3.heading = heading
            track3.playerType = songData.type
            songList.add(track3)
        }
        //preferenceHelper.setTrackList(Gson().toJson(songList))
        setTrackListData(songList)


        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.user_image)
        /*val track = Track()
        track.id = 1
        track.title = "Song 1"
        track.subTitle = "Artist 1"
        track.image = ""
        track.url = "https://85mum-content.hungama.com/1524/EVN201700435-1/stream.mpd"
        track.drmlicence = "https://wv.service.expressplay.com/hms/wv/rights/?ExpressPlayToken=BQAAAA9JKbMAJDM5NjQ4NTYyLTBjZmQtNGM2OS1hYTZiLWRhMWE0ZDVkZWY1ZgAAAGDUCWmon5pWRJuDjgj_yZe921dleZ36QE828WtAwFIZuNk1td_wbeuD5bvy6wCV-IA3Aptwz9bB2rt6GLa1qvMilwwlUXettn5LXlow6n71oevqp08rF97oSu_dCBIlLA0DLjgfRG1AOhgKdMHkHhfLkhjRxw"
        track.albumArtBitmap = null
        track.defaultAlbumArtRes = R.drawable.user_image
        songList.add(track)*/


        /*val track3 = Track()
        track3.id = 3
        track3.title = "Song 3"
        track3.subTitle = "Artist 3"
        track3.image = ""
        track3.url = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
        track3.drmlicence = ""
        track3.albumArtBitmap = null
        track3.defaultAlbumArtRes = R.drawable.user_image
        songList.add(track3)*/
    }

    fun getTrackList(context: Context): ArrayList<Track> {
        val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(context) }
        //setLog("getTrackList", preferenceHelper.getTrackList().toString())
        val gson = Gson()
        val listTrackType: Type? = object : TypeToken<ArrayList<Track>>() {}.type
        val songList: ArrayList<Track> = gson.fromJson(
            preferenceHelper.getTrackList().toString(), listTrackType
        )
        return songList
    }

    fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if ((serviceClass.name == service.service.className)) {
                return true
            }
        }
        return false
    }


    fun getDataFromJson(
        context: Context, parentPosition: Int, childPosition: Int
    ): ArrayList<BodyDataItem>? {
        val `is`: InputStream = context.resources.openRawResource(R.raw.home_tab)
        try {

            val writer: Writer = StringWriter()
            val buffer = CharArray(1024)
            val reader: Reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }

            val jsonString: String = writer.toString()


            val bucketRespModel = Gson().fromJson<HomeModel>(
                jsonString.toString(), HomeModel::class.java
            ) as HomeModel
            var storyUsersList: ArrayList<BodyDataItem>? = null
            storyUsersList = ArrayList<BodyDataItem>()
            val storyUser = bucketRespModel.data?.body!!.rows!!.get(parentPosition)!!.items
            storyUsersList.clear()
            for (story in storyUser!!.indices) {
                storyUsersList.add(storyUser[story]!!.data!!)
            }
            return storyUsersList
        } catch (exp: Exception) {
            exp.printStackTrace()
        } finally {
            `is`.close()
        }
        return null
    }

    // value in DP
    fun getValueInDP(context: Context, value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    fun getValueInDP(context: Context, value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics
        )
    }

    // value in PX
    fun getValueInPixel(context: Context, value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX, value.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    fun getValueInPixel(context: Context, value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX, value, context.resources.displayMetrics
        )
    }

    fun getValueInSP(context: Context, value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics
        )
    }

    fun ratingWithSuffix(count2: String): String? {
        if (!TextUtils.isEmpty(count2)) {
            val number = abs(count2.toFloat())

            var value = 0f
            var unit = ""
            if (Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)) {
                if (number >= 10000000) {
                    value = number / 10000000
                    unit = "Cr"
                } else if (number >= 100000) {
                    value = number / 100000
                    unit = "Lkh"
                } else if (number > 999 && number < 100000) {
                    value = number / 1000
                    unit = "K"
                }
                if (TextUtils.isEmpty(unit)) {
                    return covertNumberToCurrencyFormat(count2)
                } else {
                    val decimalFormat = DecimalFormat("##,##,###")
                    return String.format("%s%s", decimalFormat.format(value.toFloat()), unit)
                }
            } else {
                if (number > 999 && number < 1000000) {
                    value = number / 1000
                    unit = "K"
                } else if (number > 999999 && number < 1000000000) {
                    value = number / 1000000000
                    unit = "Mn"
                } else if (number > 999999999 && number < 1000000000000) {
                    value = number / 1000000000000
                    unit = "Bn"
                }
                if (TextUtils.isEmpty(unit)) {
                    return covertNumberToCurrencyFormat(count2)
                } else {
                    val decimalFormat = DecimalFormat("#.#")
                    return String.format("%s%s", decimalFormat.format(value.toFloat()), unit)
                }
            }
            return count2
        } else {
            return count2
        }

        // old code start
        /*
        if (!TextUtils.isEmpty(count2)) {
             var value = count2.toFloat()
             val arr = arrayOf("", "K", "M", "B", "T", "P", "E")
             var index = 0
             while (value / 1000 >= 1) {
                 value = value / 1000
                 index++
             }
             val decimalFormat = DecimalFormat("#.#")
             return String.format("%s%s", decimalFormat.format(value.toDouble()), arr[index])
         } else {
             return count2
         }
         */
        // old code end

    }

    fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }

    fun setArtImageDarkBg(status: Boolean, artImageUrl: String, view: ImageView): Int {

        var color: Int = 0

        if (artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl) && view != null) {
            try {
                setLog(TAG, "setArtImageDarkBg: $artImageUrl")
                val result: Deferred<Bitmap?> = GlobalScope.async {
                    val urlImage = URL(artImageUrl)
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // get the downloaded bitmap
                        val bitmap: Bitmap? = result.await()
                        if (status && bitmap != null) {
                            Palette.from(bitmap).generate { palette ->
                                color = palette!!.getMutedColor(R.color.colorPrimary)
                                view.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
//                    view.setBackgroundColor(color)
//                    rootLayout.setBackground(getGradientDrawable(getTopColor(palette), getCenterLightColor(palette), getBottomDarkColor(palette)))
                            }
                        }
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }


                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }

        return color
    }

    fun setArtImageBg(status: Boolean, artImageUrl: String, rootLayout: ViewGroup): Int {

        var color: Int = 0
        if (artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl) && rootLayout != null) {
            try {
                setLog(TAG, "setArtImageBg: $artImageUrl")
                val result: Deferred<Bitmap?> = GlobalScope.async {
                    val urlImage = URL(artImageUrl)
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // get the downloaded bitmap
                        val bitmap: Bitmap? = result.await()
                        if (status && bitmap != null) {
                            Palette.from(bitmap).generate { palette ->
//                                color = palette!!.getMutedColor(R.attr.colorPrimary)
                                color = palette!!.getMutedColor(R.color.colorPrimary)
                                //rootLayout.setBackgroundColor(color)
                                rootLayout.background = getGradientDrawable(
                                    getTopColor(palette),
                                    getCenterLightColor(palette),
                                    getBottomDarkColor(palette)
                                )
                            }
                        }
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }


                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }

        return color
    }

    fun setArtImageBgGradient(status: Boolean, artImageUrl: String, rootLayout: ImageView): Int {

        var color: Int = 0
        if (artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl) && rootLayout != null) {
            try {
                setLog(TAG, "setArtImageBg: $artImageUrl")
                val result: Deferred<Bitmap?> = GlobalScope.async {
                    val urlImage = URL(artImageUrl)
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // get the downloaded bitmap
                        val bitmap: Bitmap? = result.await()
                        if (status && bitmap != null) {
                            Palette.from(bitmap).generate { palette ->
                                //color = palette!!.getMutedColor(R.attr.colorPrimary)
                                color = palette!!.getMutedColor(R.color.colorPrimary)
                                //rootLayout.setBackgroundColor(color)
                                rootLayout.background = getGradientDrawable(
                                    getTopColor(palette),
                                    getCenterLightColor(palette),
                                    getBottomDarkColor(palette)
                                )
                            }
                        }
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }


                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }

        return color
    }

    /*
    Creating gradient drawable to be used as a background using three colors - top color ,center light color and bottom dark color
     */
    private fun getGradientDrawable(
        topColor: Int, centerColor: Int, bottomColor: Int
    ): GradientDrawable? {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.orientation = GradientDrawable.Orientation.TL_BR
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.colors = intArrayOf(
            topColor,
            /*centerColor,*/
            bottomColor
        )
        return gradientDrawable
    }

    /**
     * @param palette generated palette from image
     * @return return top color for gradient either muted or vibrant whatever is available
     */
    private fun getTopColor(palette: Palette): Int {
        try {
            return if (palette.vibrantSwatch != null || palette.mutedSwatch != null) if (palette.mutedSwatch != null) palette.mutedSwatch!!.rgb else palette.vibrantSwatch!!.rgb else Color.RED
        } catch (e: Exception) {
            return 0
        }

    }

    /**
     * @param palette generated palette from image
     * @return return center light color for gradient either muted or vibrant whatever is available
     */
    private fun getCenterLightColor(palette: Palette): Int {
        try {
            return if (palette.vibrantSwatch != null || palette.lightMutedSwatch != null) if (palette.lightMutedSwatch != null) palette.lightMutedSwatch!!.rgb else palette.lightVibrantSwatch!!.rgb else Color.GREEN
        } catch (e: Exception) {
            return 0
        }

    }

    /**
     * @param palette generated palette from image
     * @return return bottom dark color for gradient either muted or vibrant whatever is available
     */
    private fun getBottomDarkColor(palette: Palette): Int {
        try {
            return if (palette.darkVibrantSwatch != null || palette.darkMutedSwatch != null) if (palette.darkMutedSwatch != null) palette.darkMutedSwatch!!.rgb else palette.darkVibrantSwatch!!.rgb else Color.BLUE
        } catch (e: Exception) {
            return 0
        }

    }


    fun makeTextViewResizable(tv: TextView, maxLine: Int, expandText: String, viewMore: Boolean) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        val vto = tv.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = tv.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                if (maxLine == 0) {
                    val lineEndIndex = tv.layout.getLineEnd(0)
                    val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                        addClickablePartTextViewResizable(
                            Html.fromHtml(tv.text.toString()), tv, maxLine, expandText, viewMore
                        ), TextView.BufferType.SPANNABLE
                    )
                } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                    val lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                        addClickablePartTextViewResizable(
                            Html.fromHtml(tv.text.toString()), tv, maxLine, expandText, viewMore
                        ), TextView.BufferType.SPANNABLE
                    )
                } else {
                    val lineEndIndex = tv.layout.getLineEnd(tv.layout.lineCount - 1)
                    val text = tv.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                        addClickablePartTextViewResizable(
                            Html.fromHtml(tv.text.toString()),
                            tv,
                            lineEndIndex,
                            expandText,
                            viewMore
                        ), TextView.BufferType.SPANNABLE
                    )
                }
            }
        })
    }

    private fun addClickablePartTextViewResizable(
        strSpanned: Spanned, tv: TextView, maxLine: Int, spanableText: String, viewMore: Boolean
    ): SpannableStringBuilder? {
        val str = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spanableText)) {
            ssb.setSpan(object : MySpannable(false) {
                override fun onClick(widget: View) {
                    if (viewMore) {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        makeTextViewResizable(tv, -1, "read less", false)
                    } else {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        makeTextViewResizable(tv, 2, "... read more", true)
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
        }
        return ssb
    }

    /**
     * @param bitmap The source bitmap.
     * @param opacity a value between 0 (completely transparent) and 255 (completely
     * opaque).
     * @return The opacity-adjusted bitmap.  If the source bitmap is mutable it will be
     * adjusted and returned, otherwise a new bitmap is created.
     */
    fun adjustOpacity(bitmap: Bitmap, opacity: Int): Bitmap? {
        val mutableBitmap =
            if (bitmap.isMutable) bitmap else bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val colour = opacity and 0xFF shl 24
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN)
        return mutableBitmap
    }


    fun setVideoTrackList(
        context: Context,
        id: String,
        url: String,
        title: String,
        playerType: String,
        subTitle: String,
        artworkUrl: String,
        videoDrmLicense: String,
        contentType: Int = ContentTypes.VIDEO.value
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(context) }
            val songList = ArrayList<Track>()
            val track2 = Track()
            track2.id = id.toLong()
            track2.title = title
            track2.subTitle = subTitle
            track2.image = artworkUrl
            track2.url = url
            track2.songLyricsUrl = ""
            track2.drmlicence = videoDrmLicense
            track2.albumArtBitmap = null
            track2.defaultAlbumArtRes = R.drawable.bg_gradient_placeholder
            track2.heading = ""
            track2.artistName = ""
            track2.playerType = playerType
            track2.contentType = contentType
            songList.add(track2)

            //preferenceHelper.setTrackList(Gson().toJson(songList))
            val songDataList = BaseActivity.songDataList
            if (!songDataList.isNullOrEmpty() && songDataList.size > BaseActivity.nowPlayingCurrentIndex()) {
                songDataList.add(BaseActivity.nowPlayingCurrentIndex() + 1, track2)
            } else {
                setTrackListData(songList)
            }
        }

    }

    var greeting: MutableLiveData<String> = MutableLiveData<String>()
    private var lastTimeWhengetDayGreetingsCalled: Long = 0
    private val greetingeExpiryTime = 10L
    fun getDayGreetings(context: Context) {
        var timeDiff: Long = 0
        try {
            timeDiff =
                TimeUnit.MILLISECONDS.toSeconds(curreentTimeStamp() - lastTimeWhengetDayGreetingsCalled)
            setLog("AppAds", "getDayGreetings-1-timeDiff-$timeDiff")
        } catch (e: Exception) {
            timeDiff = greetingeExpiryTime + 1
            setLog("AppAds", "getDayGreetings-2")
        }
        setLog("AppAds", "getDayGreetings-timeDiff-$timeDiff-greeting-$greeting")
        if (timeDiff < greetingeExpiryTime && !TextUtils.isEmpty(greeting.toString())) {
            setLog("AppAds", "getDayGreetings-3")
            greeting
        } else {
            setLog("AppAds", "getDayGreetings-4")
            lastTimeWhengetDayGreetingsCalled = curreentTimeStamp()
            greeting.value = ""
        }

        val c = Calendar.getInstance()
        val hour = c[Calendar.HOUR_OF_DAY]

        if (hour >= 0 && hour < 5) {
            greeting.value = context.getString(R.string.profile_str_1)
        } else if (hour >= 12 && hour < 18) {
            greeting.value = context.getString(R.string.discover_str_19)
        } else if (hour >= 16 && hour < 24) {
            greeting.value = context.getString(R.string.discover_str_20)
        } /*else if(hour >= 21 && hour < 24){
            greeting = context.getString(R.string.good_night)
        }*/ else {
            greeting.value = context.getString(R.string.discover_str_22)
        }
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Fragment.showKeyboard() {
        view?.let { activity?.showKeyboard(it) }
    }

    fun Activity.showKeyboard() {
        showKeyboard(currentFocus ?: View(this))
    }

    fun Context.showKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /*
    pixelSpacing tells how many pixels to skip each pixel.
    If pixelSpacing > 1: the average color is an estimate, but higher values mean better performance
    If pixelSpacing == 1: the average color will be the real average
    If pixelSpacing < 1: the method will most likely crash (don't use values below 1)
    */
    fun calculateAverageColor(bitmap: Bitmap, pixelSpacing: Int): Int =
        runBlocking(Dispatchers.Default) {
            try {
                var R = 0
                var G = 0
                var B = 0
                val height = bitmap.height
                val width = bitmap.width
                var n = 0
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                var i = 0
                while (i < pixels.size) {
                    val color = pixels[i]
                    R += Color.red(color)
                    G += Color.green(color)
                    B += Color.blue(color)
                    n++
                    i += pixelSpacing
                }
                Color.rgb(R / n, G / n, B / n)
            } catch (e: Exception) {
                R.color.home_bg_color
            }
        }

    fun Fragment.addOnWindowFocusChangeListener(callback: (hasFocus: Boolean) -> Unit) =
        view?.viewTreeObserver?.addOnWindowFocusChangeListener(callback)

    fun getDeviceWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        return width
    }

    fun getDeviceHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        return height
    }


    fun setFirebaseAnalyticData(
        context: Context, eventName: String, eventProperties: JSONObject, isLogged: Boolean
    ) {
        try {
            setLog("setFirebaseAnalyticData", "isLogged $eventName: $isLogged")
            if (isLogged) {
                val bundle = Bundle()
                eventProperties.keys().forEach {
                    var tempStr = "" + eventProperties.opt(it)
                    tempStr = tempStr.replace(" ", "_")
                    val tempStrKey = it.replace(" ", "_")
                    bundle.putString(tempStrKey, "" + tempStr)
                }

                val eventNameLatest = eventName.replace(" ", "_")
                setLog(
                    "setFirebaseAnalyticData",
                    "setFirebaseAnalyticData $eventNameLatest: $bundle"
                )
                //sample track event

                FirebaseAnalytics.getInstance(context).logEvent(eventNameLatest, bundle)
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
        }

    }

    fun getStreamQualityDummyData(
        qualityAction: QualityAction = QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY,
        urlKey: String = "",
        screen: String = ""
    ): ArrayList<MusicPlaybackSettingStreamQualityModel> {
        var isGoldUser = false
        if (getIsGoldUser()) {
            isGoldUser = true
        }
        setLog(
            "playbackQuality",
            "CommonUtils-getStreamQualityDummyData-Before-${qualityAction.qualityActionName}-urlKey-${urlKey}"
        )
        var storedSelectedQualityId = getStoredSelectedQualityId(qualityAction)
        if (!TextUtils.isEmpty(urlKey)) {
            if (urlKey.equals(Quality.AUTO.serverKey)) {
                storedSelectedQualityId = Quality.AUTO.id
            } else if (urlKey.equals(Quality.HIGH.serverKey)) {
                storedSelectedQualityId = Quality.HIGH.id
            } else if (urlKey.equals(Quality.MEDIUM.serverKey)) {
                storedSelectedQualityId = Quality.MEDIUM.id
            } else if (isGoldUser && urlKey.equals(Quality.HD.serverKey)) {
                storedSelectedQualityId = Quality.HD.id
            } else if (isGoldUser && urlKey.equals(Quality.DOLBY.serverKey)) {
                storedSelectedQualityId = Quality.DOLBY.id
            }
        }

        //setLog("playbackQuality", "CommonUtils-getStreamQualityDummyData-after-storedSelectedQualityId-$storedSelectedQualityId -- storedSelectedQualityId == Quality.AUTO.id - ${storedSelectedQualityId == Quality.AUTO.id}")
        val streamQualityList = ArrayList<MusicPlaybackSettingStreamQualityModel>()
        val streamQualityModel1 = MusicPlaybackSettingStreamQualityModel()
        streamQualityModel1.id = Quality.AUTO.id
        streamQualityModel1.title = Quality.AUTO.qualityName
        streamQualityModel1.isGoldUser = isGoldUser
        streamQualityModel1.urlKey = urlKey
        streamQualityModel1.isSelected = storedSelectedQualityId == Quality.AUTO.id
        if (isGoldUser) {
            streamQualityList.add(streamQualityModel1)
        }

        val streamQualityModel2 = MusicPlaybackSettingStreamQualityModel()
        streamQualityModel2.id = Quality.HIGH.id
        streamQualityModel2.title = Quality.HIGH.qualityName
        streamQualityModel2.isSelected = storedSelectedQualityId == Quality.HIGH.id
        streamQualityModel2.isGoldUser = isGoldUser
        streamQualityModel2.urlKey = urlKey

        if (isGoldUser) {
            streamQualityList.add(streamQualityModel2)
        }

        val streamQualityModel3 = MusicPlaybackSettingStreamQualityModel()
        streamQualityModel3.id = Quality.MEDIUM.id
        streamQualityModel3.title = Quality.MEDIUM.qualityName
        streamQualityModel3.isSelected = storedSelectedQualityId == Quality.MEDIUM.id
        streamQualityModel3.isGoldUser = isGoldUser
        streamQualityModel3.urlKey = urlKey

        if (!isGoldUser) streamQualityModel3.isSelected = true

        streamQualityList.add(streamQualityModel3)

        val streamQualityModel4 = MusicPlaybackSettingStreamQualityModel()
        streamQualityModel4.id = Quality.HD.id
        streamQualityModel4.title = Quality.HD.qualityName
        streamQualityModel4.isSelected = storedSelectedQualityId == Quality.HD.id
        streamQualityModel4.isGoldUser = isGoldUser
        streamQualityModel4.urlKey = urlKey
        if ((isGoldUser && !screen.equals("music_player_screen")) || (isGoldUser && screen.equals("music_player_screen") && urlKey.equals(
                Quality.HD.serverKey,
                true
            ))
        ) {
            streamQualityModel4.isDolbyOrHDEnable = true
        }

        streamQualityList.add(streamQualityModel4)

        //https://hungama.atlassian.net/browse/HU-5409 - if download video quality - the remove dolby menu
        if (qualityAction != QualityAction.VIDEO_PLAYBACK_DOWNLOAD_QUALITY) {
            val streamQualityModel5 = MusicPlaybackSettingStreamQualityModel()
            streamQualityModel5.id = Quality.DOLBY.id
            streamQualityModel5.title = Quality.DOLBY.qualityName
            streamQualityModel5.isSelected = storedSelectedQualityId == Quality.DOLBY.id
            streamQualityModel5.isGoldUser = isGoldUser
            streamQualityModel5.urlKey = urlKey
            if ((isGoldUser && !screen.equals("music_player_screen")) || (isGoldUser && screen.equals(
                    "music_player_screen"
                ) && urlKey.equals(
                    Quality.DOLBY.serverKey, true
                ))
            ) {
                streamQualityModel5.isDolbyOrHDEnable = true
            }
            streamQualityList.add(streamQualityModel5)
        }
        setLog("ReturnstreamQualityList", "$streamQualityList")

        return streamQualityList
    }

    fun Context.getAppWidgetsIdsFor(clazz: Class<*>): IntArray {
        return AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, clazz))
    }

    fun getNextOnboardingScreen(screenNo: Int): Int {
        val onboardingUserScreen = getFirebaseConfigOnboardingData()
        var nextScreen = screenNo
        for (i in 1..4) {
            if (nextScreen == 1) {
                if (onboardingUserScreen.firstScreen?.screen.equals(
                        "music_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.firstScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicLanguage()) {
                            return 1 //music_language
                        } else {
                            nextScreen = 2
                        }
                    } else {
                        nextScreen = 2
                    }

                } else if (onboardingUserScreen.firstScreen?.screen.equals(
                        "music_artist", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.firstScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicArtist()) {
                            return 2 //music_artist
                        } else {
                            nextScreen = 2
                        }
                    } else {
                        nextScreen = 2
                    }

                } else if (onboardingUserScreen.firstScreen?.screen.equals(
                        "video_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.firstScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoLanguage()) {
                            return 3 //video_language
                        } else {
                            nextScreen = 2
                        }
                    } else {
                        nextScreen = 2
                    }

                } else if (onboardingUserScreen.firstScreen?.screen.equals(
                        "video_genre", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.firstScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoGenre()) {
                            return 4 //video_genre
                        } else {
                            nextScreen = 2
                        }
                    } else {
                        nextScreen = 2
                    }

                } else {
                    nextScreen = 2
                }
            } else if (nextScreen == 2) {

                if (onboardingUserScreen.secondScreen?.screen.equals(
                        "music_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.secondScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicLanguage()) {
                            return 1 //music_language
                        } else {
                            nextScreen = 3
                        }
                    } else {
                        nextScreen = 3
                    }

                } else if (onboardingUserScreen.secondScreen?.screen.equals(
                        "music_artist", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.secondScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicArtist()) {
                            return 2 //music_artist
                        } else {
                            nextScreen = 3
                        }
                    } else {
                        nextScreen = 3
                    }

                } else if (onboardingUserScreen.secondScreen?.screen.equals(
                        "video_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.secondScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoLanguage()) {
                            return 3 //video_language
                        } else {
                            nextScreen = 3
                        }
                    } else {
                        nextScreen = 3
                    }

                } else if (onboardingUserScreen.secondScreen?.screen.equals(
                        "video_genre", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.secondScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoGenre()) {
                            return 4 //video_genre
                        } else {
                            nextScreen = 3
                        }
                    } else {
                        nextScreen = 3
                    }

                } else {
                    nextScreen = 3
                }

            } else if (nextScreen == 3) {

                if (onboardingUserScreen.thirdScreen?.screen.equals(
                        "music_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.thirdScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicLanguage()) {
                            return 1 //music_language
                        } else {
                            nextScreen = 4
                        }
                    } else {
                        nextScreen = 4
                    }

                } else if (onboardingUserScreen.thirdScreen?.screen.equals(
                        "music_artist", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.thirdScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicArtist()) {
                            return 2 //music_artist
                        } else {
                            nextScreen = 4
                        }
                    } else {
                        nextScreen = 4
                    }

                } else if (onboardingUserScreen.thirdScreen?.screen.equals(
                        "video_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.thirdScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoLanguage()) {
                            return 3 //video_language
                        } else {
                            nextScreen = 4
                        }
                    } else {
                        nextScreen = 4
                    }

                } else if (onboardingUserScreen.thirdScreen?.screen.equals(
                        "video_genre", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.thirdScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoGenre()) {
                            return 4 //video_genre
                        } else {
                            nextScreen = 4
                        }
                    } else {
                        nextScreen = 4
                    }

                } else {
                    nextScreen = 4
                }

            } else if (nextScreen == 4) {

                if (onboardingUserScreen.fourthScreen?.screen.equals(
                        "music_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.fourthScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicLanguage()) {
                            return 1 //music_language
                        } else {
                            nextScreen = 0 // Close dialog
                        }
                    } else {
                        nextScreen = 0
                    }

                } else if (onboardingUserScreen.fourthScreen?.screen.equals(
                        "music_artist", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.fourthScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getMusicArtist()) {
                            return 2 //music_artist
                        } else {
                            nextScreen = 0 // Close dialog
                        }
                    } else {
                        nextScreen = 0
                    }

                } else if (onboardingUserScreen.fourthScreen?.screen.equals(
                        "video_language", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.fourthScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoLanguage()) {
                            return 3 //video_language
                        } else {
                            nextScreen = 0 // Close dialog
                        }
                    } else {
                        nextScreen = 0
                    }

                } else if (onboardingUserScreen.fourthScreen?.screen.equals(
                        "video_genre", true
                    )
                ) {
                    if (SharedPrefHelper.getInstance()
                            .getUserSession()!! >= onboardingUserScreen.fourthScreen?.numberOfSession!!
                    ) {
                        if (!SharedPrefHelper.getInstance().getVideoGenre()) {
                            return 4 //video_genre
                        } else {
                            nextScreen = 0 // Close dialog
                        }
                    } else {
                        nextScreen = 0
                    }

                } else {
                    nextScreen = 0
                }

            }
        }
        return nextScreen
    }

    fun checkOnboardingIsSet(): Boolean {
        return SharedPrefHelper.getInstance().getMusicLanguage() && SharedPrefHelper.getInstance()
            .getMusicArtist() && SharedPrefHelper.getInstance()
            .getVideoLanguage() && SharedPrefHelper.getInstance().getVideoGenre()
    }

    fun getFirebaseConfigOnboardingData(): OnboardingConfigModel {
        var onboardingConfigModel = OnboardingConfigModel()
        try {
            val remoteConfig = Firebase.remoteConfig
            val onboardingType = remoteConfig.getString("Onboarding_Type")
            val onboardingData = remoteConfig.getString(onboardingType)

            setLog("RemoteWorkDone-1.1", "OnboardingData:= " + onboardingData)

            onboardingConfigModel = Gson().fromJson<OnboardingConfigModel>(
                onboardingData, OnboardingConfigModel::class.java
            ) as OnboardingConfigModel
        } catch (e: Exception) {

        }

        return onboardingConfigModel
    }

    @OptIn(UnstableApi::class)
    fun setMediaItem(track: Track): MediaItem {
        var mimType = MimeTypes.BASE_TYPE_AUDIO
        if (track.url?.contains(".m3u8", true)!!) {
            mimType = MimeTypes.APPLICATION_M3U8
        } else if (track.url?.contains(".mp3", true)!!) {
            mimType = MimeTypes.BASE_TYPE_AUDIO
        } else if (track.url?.contains(".mpd", true)!!) {
            mimType = MimeTypes.APPLICATION_MPD
        } else if (track.url?.contains(".mp4", true)!!) {
            mimType = MimeTypes.APPLICATION_MP4
        }

        if (!TextUtils.isEmpty(track.drmlicence)) {
            val drmSchemeUuid = Util.getDrmUuid(C.WIDEVINE_UUID.toString())
            val mediaItem =
                MediaItem.Builder().setDrmUuid(drmSchemeUuid).setDrmLicenseUri(track.drmlicence)
                    .setUri(Uri.parse(track.url))
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(track.title).build())
                    .setMimeType(mimType).build()
            setLog("TAG", "common setMediaItem: DRM content play url:${track.url}")
            return mediaItem
        } else {
            val mediaItem = MediaItem.Builder().setUri(Uri.parse(track.url))
                .setMediaMetadata(MediaMetadata.Builder().setTitle(track.title).build())
                .setMimeType(mimType)
                /*.setSubtitles(Lists.newArrayList(subtitle))*/.build()
            setLog("TAG", "setMediaItem: Non DRM content play url:${track.url}")
            return mediaItem
        }

    }

    public fun getMimeType(context: Context, uri: Uri?): String? {
        val cR: ContentResolver = context.contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        var type: String = mime.getExtensionFromMimeType(cR.getType(uri!!)).toString()
        if (type == null) {
            type = "*/*"
        }
        return type
    }

    fun deleteFileAndContents(@NonNull file: File) {
        try {
            if (file.exists()) {
                if (file.isDirectory) {
                    val contents = file.listFiles()
                    if (contents != null) {
                        for (content in contents) {
                            deleteFileAndContents(content)
                        }
                    }
                }
                file.delete()
            }
        } catch (e: Exception) {

        }
    }

    fun deleteFileFromStorage(filePathList: ArrayList<String>) {
        try {
            for (filePath in filePathList) {
                deleteFileAndContents(File(filePath))
            }
            if (onFileDeleted != null) {
                onFileDeleted?.onFileDeleted(true)
            }
        } catch (e: Exception) {
            if (onFileDeleted != null) {
                onFileDeleted?.onFileDeleted(false)
            }
        }
    }

    fun removeDownloadedContent(
        content: DownloadedAudio,
        context: Context,
        onFileDeleted: OnFileDeleted? = null
    ) {
        try {
            if (content != null) {
                val removableId = content.aId
                val removableContentPath = content.downloadedFilePath
                val removableThumbnailPath = content.thumbnailPath!!
                val removableParentThumbnailPath = content.parentThumbnailPath!!
                val removableLyricsPath = content.lyricsFilePath!!
                if (removableId != null) {
                    this.onFileDeleted = onFileDeleted
                    AppDatabase.getInstance()?.downloadedAudio()
                        ?.deleteDownloadQueueItem(removableId)
                    AppDatabase.getInstance()?.downloadQueue()?.deleteDownloadQueueItem(removableId)
                    val removablePathList = arrayListOf<String>()
                    removablePathList.add(removableContentPath)
                    removablePathList.add(removableThumbnailPath)
                    removablePathList.add(removableParentThumbnailPath)
                    removablePathList.add(removableLyricsPath)
                    deleteFileFromStorage(removablePathList)
                }
            }
        } catch (e: Exception) {

        }
    }

    var onFileDeleted: OnFileDeleted? = null

    interface OnFileDeleted {
        fun onFileDeleted(isDeleted: Boolean)
    }

    fun getETAString(context: Context, etaInMilliSeconds: Long): String? {
        if (etaInMilliSeconds < 0) {
            return ""
        }
        var seconds = (etaInMilliSeconds / 1000).toInt()
        val hours = (seconds / 3600).toLong()
        seconds -= (hours * 3600).toInt()
        val minutes = (seconds / 60).toLong()
        seconds -= (minutes * 60).toInt()
        return if (hours > 0) {
            context.getString(R.string.download_eta_hrs, hours, minutes, seconds)
        } else if (minutes > 0) {
            context.getString(R.string.download_eta_min, minutes, seconds)
        } else {
            context.getString(R.string.download_eta_sec, seconds)
        }
    }

    fun getDownloadSpeedString(context: Context, downloadedBytesPerSecond: Long): String? {
        if (downloadedBytesPerSecond < 0) {
            return ""
        }
        val kb = downloadedBytesPerSecond.toDouble() / 1000.toDouble()
        val mb = kb / 1000.toDouble()
        val decimalFormat = DecimalFormat(".##")
        return if (mb >= 1) {
            context.getString(R.string.download_speed_mb, decimalFormat.format(mb))
        } else if (kb >= 1) {
            context.getString(R.string.download_speed_kb, decimalFormat.format(kb))
        } else {
            context.getString(R.string.download_speed_bytes, downloadedBytesPerSecond)
        }
    }

    fun createFile(filePath: String?): File? {
        val file = File(filePath)
        if (!file.exists()) {
            val parent = file.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    fun getProgress(downloaded: Long, total: Long): Int {
        return if (total < 1) {
            -1
        } else if (downloaded < 1) {
            0
        } else if (downloaded >= total) {
            100
        } else {
            (downloaded.toDouble() / total.toDouble() * 100).toInt()
        }
    }

    fun getUserSubscriptionTypeId(userSubscriptionModel: UserSubscriptionModel): Int {
        var subscription_type = 0
        if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                GOLD
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_GOLD
        } else if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                TVOD
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_TVOD
        } else if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                GOLD_X_TVOD_X_CONCERT
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_GOLD_X_TVOD_X_CONCERT
        } else if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                GOLD_X_TVOD
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_GOLD_X_TVOD
        } else if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                GOLD_X_CONCERT
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_GOLD_X_CONCERT
        } else if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                GOLD_WITH_ADS
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_GOLD_WITH_ADS
        } else if (!TextUtils.isEmpty(userSubscriptionModel.data?.user?.userMembershipTypeId!!) && userSubscriptionModel.data?.user?.userMembershipTypeId!!.equals(
                ADS_FREE
            )
        ) {
            subscription_type = SUBSCRIPTION_TYPE_ADS_FREE
        } else {
            subscription_type = SUBSCRIPTION_TYPE_FREE
        }
        return subscription_type
    }

    fun saveUserProfileDetails(it: UserProfileModel?) {
        if (it != null) {
            CoroutineScope(Dispatchers.IO).launch {
                //setLog("setProfileData","saveUserProfileDetails-1")
                if (it.statusCode == 200 && it.result != null && it.result?.size!! > 0) {
                    if (!TextUtils.isEmpty(it.result?.get(0)?.profileImage!!.toString())) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_IMAGE, it.result?.get(0)?.profileImage)
                    } else if (!TextUtils.isEmpty(it.result?.get(0)?.alternateProfileImage!!.toString())) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_IMAGE, it.result?.get(0)?.alternateProfileImage)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.USER_IMAGE, "")
                    }
                    if (!TextUtils.isEmpty(it.result?.get(0)?.handleName)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.HANDLE_NAME, it.result?.get(0)?.handleName)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.HANDLE_NAME, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.phone)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_MOBILE, it.result?.get(0)?.phone)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.USER_MOBILE, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.email)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_EMAIL, it.result?.get(0)?.email)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.USER_EMAIL, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.firstName)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.FIRST_NAME, it.result?.get(0)?.firstName)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.FIRST_NAME, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.lastName)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.LAST_NAME, it.result?.get(0)?.lastName)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.LAST_NAME, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.gender)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_GENDER, it.result?.get(0)?.gender)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.USER_GENDER, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.dob)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_DOB, it.result?.get(0)?.dob)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.USER_DOB, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.share)) {
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.USER_SHARE, it.result?.get(0)?.share)
                    } else {
                        SharedPrefHelper.getInstance().save(PrefConstant.USER_SHARE, "")
                    }

                    if (!TextUtils.isEmpty(it.result?.get(0)?.shopifyId)) {
                        SharedPrefHelper.getInstance()
                            .setUserShopifyId(it.result?.get(0)?.shopifyId.toString())
                    } else {
                        SharedPrefHelper.getInstance().setUserShopifyId("0")
                    }
                    setLog("profileData", SharedPrefHelper.getInstance().getUserShopifyId())
                    //setLog("setProfileData","saveUserProfileDetails-2")
                }
            }
        }
    }


    fun deleteUserProfileDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedPrefHelper.getInstance().save(PrefConstant.USER_IMAGE, "")
            SharedPrefHelper.getInstance().save(PrefConstant.USER_NAME, "")
            SharedPrefHelper.getInstance().save(PrefConstant.HANDLE_NAME, "")
            SharedPrefHelper.getInstance().save(PrefConstant.USER_MOBILE, "")
            SharedPrefHelper.getInstance().save(PrefConstant.USER_EMAIL, "")
            SharedPrefHelper.getInstance().save(PrefConstant.FIRST_NAME, "")
            SharedPrefHelper.getInstance().save(PrefConstant.LAST_NAME, "")
            SharedPrefHelper.getInstance().save(PrefConstant.USER_GENDER, "")
            SharedPrefHelper.getInstance().save(PrefConstant.USER_DOB, "")

            //Set Stream and Download quality auto
            SharedPrefHelper.getInstance().setMusicPlaybackStreamQualityId(Quality.AUTO.id)
            SharedPrefHelper.getInstance()
                .setMusicPlaybackStreamQualityTitle(Quality.AUTO.qualityName)
            SharedPrefHelper.getInstance().setMusicPlaybackDownloadQualityId(Quality.AUTO.id)
            SharedPrefHelper.getInstance()
                .setMusicPlaybackDownloadQualityTitle(Quality.AUTO.qualityName)
            SharedPrefHelper.getInstance().setVideoPlaybackStreamQualityId(Quality.AUTO.id)
            SharedPrefHelper.getInstance()
                .setVideoPlaybackStreamQualityTitle(Quality.AUTO.qualityName)
            SharedPrefHelper.getInstance().setVideoPlaybackDownloadQualityId(Quality.AUTO.id)
            SharedPrefHelper.getInstance()
                .setVideoPlaybackDownloadQualityTitle(Quality.AUTO.qualityName)
            SharedPrefHelper.getInstance().setMusicLanguageTitleList(default_music_language_title)
            SharedPrefHelper.getInstance().setMusicLanguageCodeList(default_music_language_code)
            SharedPrefHelper.getInstance().setVideoLanguageTitleList(default_video_language_title)
            SharedPrefHelper.getInstance().setVideoLanguageCodeList(default_video_language_code)
        }


    }

    fun copyFile(src: String, dst: String) {
        setLog("FileDecryption", "copyFile-src-$src")
        setLog("FileDecryption", "copyFile-dst-$dst")
        try {
            CoroutineScope(Dispatchers.Default).launch {
                val fileSrc = File(src)
                val fileDst = File(dst)
                try {
                    setLog("FileDecryption", "copyFile-start")
                    FileInputStream(fileSrc).use { `in` ->
                        FileOutputStream(fileDst).use { out ->
                            // Transfer bytes from in to out
                            val buf = ByteArray(1024)
                            var len: Int
                            val cmEncryptor = CMEncryptor2(DEVICE_ID)
                            while (`in`.read(buf).also { len = it } > 0) {
                                cmEncryptor.decrypt(buf, 0, len)
                                out.write(buf, 0, len)
                            }
                            setLog("FileDecryption", "copyFile-In Progress")
                        }
                        setLog("FileDecryption", "copyFile-In Progress End")
                    }
                    setLog("FileDecryption", "copyFile-Done")
                } catch (e: Exception) {
                    setLog("FileDecryption", "copyFile-Error-1-${e.message}")
                } catch (e: NoSuchFileException) {
                    setLog("FileDecryption", "copyFile-Error-2")
                }
            }
        } catch (e: Exception) {
            setLog("FileDecryption", "copyFile-Error-3")
        }

    }

    /**
     * Returns the protocol for a given URI or filename.
     *
     * @param source Determine the protocol for this URI or filename.
     *
     * @return The protocol for the given source.
     */
    private fun getProtocol(source: String?): String? {
        assert(source != null)
        var protocol: String? = null
        protocol = try {
            val uri = URI(source)
            if (uri.isAbsolute) {
                uri.scheme
            } else {
                val url = URL(source)
                url.protocol
            }
        } catch (e: java.lang.Exception) {
            if (source!!.startsWith("//")) {
                throw IllegalArgumentException("Relative context: $source")
            } else {
                val file = File(source)
                getProtocol(file)
            }
        }
        return protocol
    }

    /**
     * Returns the protocol for a given file.
     *
     * @param file Determine the protocol for this file.
     *
     * @return The protocol for the given file.
     */
    private fun getProtocol(file: File): String? {
        val result: String? = try {
            file.toURI().toURL().protocol
        } catch (e: java.lang.Exception) {
            "unknown"
        }
        return result
    }

    fun isFilePath(fileUrl: String): Boolean {
        val isFile = "file".equals(getProtocol(fileUrl), true)
        setLog("isFilePathOrUrl", "isFilePathOrUrl-isFile-$isFile")
        return isFile
    }

    fun isContentDownloaded(songDataList: ArrayList<Track>?, index: Int): Boolean {
        try {
            if (songDataList != null && songDataList.size > index) {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                    songDataList.get(index).id.toString()
                )
                if (downloadedAudio != null && downloadedAudio.contentId.equals(
                        songDataList.get(
                            index
                        ).id.toString()
                    )
                ) {
                    val fileName = downloadedAudio.downloadedFilePath
                    val file = File(fileName)
                    return if (file.exists()) {
                        true
                    } else {
                        if (downloadedAudio.contentType == ContentTypes.VIDEO.value || downloadedAudio.contentType == ContentTypes.MOVIES.value || downloadedAudio.contentType == ContentTypes.TV_SHOWS.value || downloadedAudio.contentType == ContentTypes.SHORT_FILMS.value || downloadedAudio.contentType == ContentTypes.SHORT_VIDEO.value) {

                        } else {
                            AppDatabase.getInstance()?.downloadedAudio()
                                ?.deleteDownloadQueueItemByContentId(
                                    downloadedAudio.contentId?.toString()!!
                                )
                        }
                        false
                    }

                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }

    fun isContentDownloaded(songDataList: Track): Boolean {
        try {
            if (songDataList != null) {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                    songDataList.id.toString()
                )
                if (downloadedAudio != null && downloadedAudio.contentId.equals(
                        songDataList.id.toString()
                    )
                ) {
                    val fileName = downloadedAudio.downloadedFilePath
                    val file = File(fileName)
                    return if (file.exists()) {
                        true
                    } else {
                        if (downloadedAudio.contentType == ContentTypes.VIDEO.value || downloadedAudio.contentType == ContentTypes.MOVIES.value || downloadedAudio.contentType == ContentTypes.TV_SHOWS.value || downloadedAudio.contentType == ContentTypes.SHORT_FILMS.value || downloadedAudio.contentType == ContentTypes.SHORT_VIDEO.value) {

                        } else {
                            AppDatabase.getInstance()?.downloadedAudio()
                                ?.deleteDownloadQueueItemByContentId(
                                    downloadedAudio.contentId?.toString()!!
                                )
                        }
                        false
                    }
                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }


    // extension function to save an image to internal storage
    fun Bitmap.saveToInternalStorage(
        context: Context,
        contentId: String,
        destinationPath: String
    ): Uri? = runBlocking(Dispatchers.IO) {
        // get the context wrapper instance
        val wrapper = ContextWrapper(context)

        // initializing a new file
        // bellow line return a directory in internal storage
        //var file = wrapper.getDir("images", Context.MODE_PRIVATE)

        // create a file to save the image
        //file = File(file, "${UUID.randomUUID()}.jpg")


        try {
            val mDir = File(Data.getSaveAudioDir(context))
            if (!mDir.exists()) {
                mDir.mkdir()
            }
            val mFilePath = File(destinationPath)
            if (!mFilePath.exists()) {
                mFilePath.createNewFile()
            }
            // get the file output stream
            val stream: OutputStream = FileOutputStream(destinationPath)

            // compress bitmap
            compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // flush the stream
            stream.flush()

            // close stream
            stream.close()

            // return the saved image uri
            return@runBlocking Uri.parse(destinationPath)
        } catch (e: Exception) { // catch the exception
            e.printStackTrace()
            return@runBlocking Uri.parse("")
        }
    }

    fun saveThumbnail(
        context: Context,
        imageUri: String,
        id: String,
        destinationPath: String,
        isParentTumbnail: Boolean
    ) {
        if (!TextUtils.isEmpty(imageUri)) {
            val urlImage = URL(imageUri)
            // async task to get / download bitmap from url
            val result: Deferred<Bitmap?> = GlobalScope.async(Dispatchers.IO) {
                urlImage.toBitmap()
            }

            GlobalScope.launch(Dispatchers.IO) {
                // get the downloaded bitmap
                val bitmap: Bitmap? = result.await()

                // if downloaded then saved it to internal storage
                bitmap?.apply {
                    // get saved bitmap internal storage uri
                    val savedUri: Uri? = saveToInternalStorage(context, id, destinationPath)
                    setLog("DWThumbnail-", savedUri.toString())
                    if (isParentTumbnail) {
                        AppDatabase.getInstance()?.downloadedAudio()
                            ?.updateDownloadedImageParentThumbnailPath(savedUri.toString(), id)
                    } else {
                        AppDatabase.getInstance()?.downloadedAudio()
                            ?.updateDownloadedImageThumbnailPath(savedUri.toString(), id)
                    }

                }
            }
        }
    }

    fun isContentThumbnailDownloaded(songDataList: ArrayList<Track>?, index: Int): Boolean {
        try {
            if (songDataList != null && songDataList.size > 0) {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                    songDataList.get(index).id.toString()
                )
                if (downloadedAudio != null && downloadedAudio.contentId.equals(
                        songDataList?.get(
                            index
                        )?.id.toString()
                    )
                ) {
                    val fileName = downloadedAudio.thumbnailPath
                    val file = File(fileName)
                    return file.exists()
                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }

    @Throws(IOException::class)
    fun copyThumbnailFile(src: String, dst: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileSrc = File(src)
                val fileDst = File(dst)

                FileInputStream(fileSrc).use { `in` ->
                    FileOutputStream(fileDst).use { out ->
                        // Transfer bytes from in to out
                        val buf = ByteArray(1024)
                        var len: Int
                        while (`in`.read(buf).also { len = it } > 0) {
                            out.write(buf, 0, len)
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }


    }

    fun mergeMultiple(parts: ArrayList<Bitmap>): Bitmap? = runBlocking(Dispatchers.IO) {
        try {

            //setLog("mergeMultiple", "mergeMultiple()-parts[0].width-"+parts[0].width)
            //setLog("mergeMultiple", "mergeMultiple()-parts[0].height-"+parts[0].height)
            val canvasWidth = parts[0].width * 2
            val canvasHeight = parts[0].height * 2
            //setLog("mergeMultiple", "mergeMultiple()-canvasWidth-$canvasWidth")
            //setLog("mergeMultiple", "mergeMultiple()-canvasHeight-$canvasHeight")
            val result = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint()
            for (i in parts.indices) {
                //setLog("mergeMultiple", "mergeMultiple()-parts[].width-$i="+parts[i].width)
                //setLog("mergeMultiple", "mergeMultiple()-parts[].height-$i="+parts[i].height)
                val imageWidth = parts[i].width
                val imageHeight = parts[i].height
                //setLog("mergeMultiple", "mergeMultiple()-imageWidth-$imageWidth")
                //setLog("mergeMultiple", "mergeMultiple()-imageHeight-$imageHeight")

                //setLog("mergeMultiple", "mergeMultiple()-left-"+(imageWidth * (i % 2)).toFloat().toString())
                //setLog("mergeMultiple", "mergeMultiple()-top-"+(imageHeight * (i / 2)).toFloat().toString())
                canvas.drawBitmap(
                    parts[i],
                    (imageWidth * (i % 2)).toFloat(),
                    (imageHeight * (i / 2)).toFloat(),
                    paint
                )
            }
            return@runBlocking result
        } catch (e: Exception) {

        }
        return@runBlocking null
    }

    fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        try {
            if (source.height >= source.width) {
                if (source.height <= maxLength) { // if image height already smaller than the required height
                    return source
                }

                val aspectRatio = source.width.toDouble() / source.height.toDouble()
                val targetWidth = (maxLength * aspectRatio).toInt()
                val result = Bitmap.createScaledBitmap(source, targetWidth, maxLength, false)
                return result
            } else {
                if (source.width <= maxLength) { // if image width already smaller than the required width
                    return source
                }

                val aspectRatio = source.height.toDouble() / source.width.toDouble()
                val targetHeight = (maxLength * aspectRatio).toInt()

                val result = Bitmap.createScaledBitmap(source, maxLength, targetHeight, false)
                return result
            }
        } catch (e: Exception) {
            return source
        }
    }

    fun downloadFile(urlString: String, destinationPath: String, id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create a URL object
                val url = URL(urlString)
                // Create a buffered reader object using the url object
                val reader = url.openStream().bufferedReader()

                // Enter filename in which you want to download
                val downloadFile = File(destinationPath).also { it.createNewFile() }
                // Create a buffered writer object for the file
                val writer = FileWriter(downloadFile).buffered()

                // read and write each line from the stream till the end
                var line: String
                while (reader.readLine().also { line = it?.toString() ?: "" } != null) writer.write(
                    line
                )

                // Close all open streams
                reader.close()
                writer.close()

                // Update UI for download is successful
                withContext(Dispatchers.Main) {
                    AppDatabase.getInstance()?.downloadedAudio()
                        ?.updateDownloadedlyricsFilePathPath(downloadFile.toString(), id)
                }

            } catch (e: Exception) {
                // Update UI for download has failed
                withContext(Dispatchers.Main) {
                    val incompleteFile = File(destinationPath)
                    if (incompleteFile.exists()) incompleteFile.delete()
                    e.printStackTrace()
                }
            }
        }
    }

    val File.extension: String
        get() = name.substringAfterLast('.', "")

    fun isContentLyricsDownloaded(songDataList: ArrayList<Track>?, index: Int): Boolean {
        try {
            if (songDataList != null && songDataList.size > 0) {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                    songDataList.get(index).id.toString()
                )
                if (downloadedAudio != null && downloadedAudio.contentId.equals(
                        songDataList.get(
                            index
                        ).id.toString()
                    )
                ) {
                    val fileName = downloadedAudio.lyricsFilePath
                    val file = File(fileName)
                    return file.exists()
                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }

    fun getDownloadedAudioModel(
        downloadQueue: DownloadQueue,
        isAudioContent: Boolean
    ): DownloadedAudio = runBlocking(Dispatchers.IO) {
        val da = DownloadedAudio()
        da.aId = downloadQueue.qId
        da.contentId = downloadQueue.contentId
        da.title = downloadQueue.title
        da.subTitle = downloadQueue.subTitle
        da.playableUrl = downloadQueue.playableUrl
        da.downloadUrl = downloadQueue.downloadUrl
        da.lyricsUrl = downloadQueue.lyricsUrl
        da.downloadManagerId = downloadQueue.downloadManagerId

        da.downloadedFilePath = downloadQueue.downloadedFilePath
        da.totalDownloadBytes = downloadQueue.totalDownloadBytes
        da.downloadedBytes = downloadQueue.downloadedBytes
        da.downloadStatus = downloadQueue.downloadStatus
        da.downloadNetworkType = downloadQueue.downloadNetworkType
        da.createdDT = downloadQueue.createdDT


        da.parentId = downloadQueue.parentId
        da.pName = downloadQueue.pName
        da.pType = downloadQueue.pType
        da.contentType = downloadQueue.contentType
        da.originalAlbumName = downloadQueue.originalAlbumName
        da.podcastAlbumName = downloadQueue.podcastAlbumName
        da.releaseDate = downloadQueue.releaseDate
        da.actor = downloadQueue.actor
        da.singer = downloadQueue.singer
        da.lyricist = downloadQueue.lyricist
        da.genre = downloadQueue.genre
        da.subGenre = downloadQueue.subGenre
        da.mood = downloadQueue.mood
        da.tempo = downloadQueue.tempo
        da.language = downloadQueue.language
        da.musicDirectorComposer = downloadQueue.musicDirectorComposer
        da.releaseYear = downloadQueue.releaseYear
        da.category = downloadQueue.category
        da.rating = downloadQueue.rating
        da.cast_enabled = downloadQueue.cast_enabled
        da.ageRating = downloadQueue.ageRating
        da.criticRating = downloadQueue.criticRating
        da.keywords = downloadQueue.keywords
        da.episodeNumber = downloadQueue.episodeNumber
        da.seasonNumber = downloadQueue.seasonNumber
        da.subtitleEnabled = downloadQueue.subtitleEnabled
        da.selectedSubtitleLanguage = downloadQueue.selectedSubtitleLanguage
        da.lyricsType = downloadQueue.lyricsType
        da.userRating = downloadQueue.userRating
        da.videoQuality = downloadQueue.videoQuality
        da.audioQuality = downloadQueue.audioQuality
        da.label = downloadQueue.label
        da.labelId = downloadQueue.labelId
        da.isOriginal = downloadQueue.isOriginal
        da.contentPayType = downloadQueue.contentPayType

        da.itype = downloadQueue.itype
        da.type = downloadQueue.type
        da.image = downloadQueue.image
        da.duration = downloadQueue.duration
        da.cast = downloadQueue.cast
        da.explicit = downloadQueue.explicit
        da.pid = downloadQueue.pid
        da.movierights = downloadQueue.movierights
        da.attribute_censor_rating = downloadQueue.attribute_censor_rating
        da.nudity = downloadQueue.nudity
        da.f_playcount = downloadQueue.f_playcount
        da.s_artist = downloadQueue.s_artist
        da.artist = downloadQueue.artist
        da.lyricsLanguage = downloadQueue.lyricsLanguage
        da.lyricsLanguageId = downloadQueue.lyricsLanguageId
        da.lyricsFilePath = downloadQueue.lyricsFilePath
        da.f_fav_count = downloadQueue.f_fav_count
        da.synopsis = downloadQueue.synopsis
        da.description = downloadQueue.description
        da.vendor = downloadQueue.vendor
        da.countEraFrom = downloadQueue.countEraFrom
        da.countEraTo = downloadQueue.countEraTo
        da.skipCreditET = downloadQueue.skipCreditET
        da.skipCreditST = downloadQueue.skipCreditST
        da.skipIntroET = downloadQueue.skipIntroET
        da.skipIntroST = downloadQueue.skipIntroST
        da.userId = downloadQueue.userId
        da.thumbnailPath = downloadQueue.thumbnailPath

        da.pSubName = downloadQueue.pSubName
        da.pReleaseDate = downloadQueue.pReleaseDate
        da.pDescription = downloadQueue.pDescription
        da.pNudity = downloadQueue.pNudity
        da.pRatingCritics = downloadQueue.pRatingCritics
        da.pMovieRights = downloadQueue.pMovieRights
        da.pGenre = downloadQueue.pGenre
        da.pLanguage = downloadQueue.pLanguage
        da.pImage = downloadQueue.pImage
        da.heading = downloadQueue.heading
        da.downloadAll = downloadQueue.downloadAll
        da.parentThumbnailPath = downloadQueue.parentThumbnailPath
        da.downloadRetry = downloadQueue.downloadRetry
        da.isFavorite = downloadQueue.isFavorite
        da.planName = downloadQueue.planName
        da.planType = downloadQueue.planType
        da.contentStreamDate = downloadQueue.contentStreamDate
        da.contentStreamDuration = downloadQueue.contentStreamDuration
        da.percentDownloaded = downloadQueue.percentDownloaded
        da.contentStartDate = downloadQueue.contentStartDate
        da.contentExpiryDate = downloadQueue.contentExpiryDate
        da.contentPlayValidity = downloadQueue.contentPlayValidity
        da.drmLicense = downloadQueue.drmLicense
        da.f_playcount = downloadQueue.f_playcount
        da.contentShareLink = downloadQueue.contentShareLink
        da
    }


    fun getDownloadedContent(contentId: String): DownloadedAudio? {
        return AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(contentId)
    }

    fun decryptAudioContent(currentFilePathUrl: String, newFilePathUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            setLog("FileDecryption", "decryptAudioContent-currentFilePathUrl-$currentFilePathUrl")
            setLog("FileDecryption", "decryptAudioContent-newFilePathUrl-$newFilePathUrl")
            var file: File? = null
            var currentFile: File? = null
            try {

                if (newFilePathUrl.contains(Constant.filePrefix)) {
                    val uri = URL(newFilePathUrl)
                    file = File(uri.toURI())
                    setLog("FileDecryption", "decryptAudioContent-uri-file-3-${file.path}")
                } else {
                    file = File(newFilePathUrl)
                    setLog("FileDecryption", "decryptAudioContent-newFilePathUrl-file-${file}")
                }
                if (currentFilePathUrl.contains(Constant.filePrefix)) {
                    val currentUri = URL(currentFilePathUrl)
                    currentFile = File(currentUri.toURI())
                    setLog(
                        "FileDecryption",
                        "decryptAudioContent-currentFile-file-3-${currentFile.path}"
                    )
                } else {
                    currentFile = File(currentFilePathUrl)
                    setLog(
                        "FileDecryption",
                        "decryptAudioContent-newFilePathUrl-currentFile-${currentFile}"
                    )
                }


                if (file.exists()) {
                    setLog("FileDecryption", "decryptAudioContent-fileExist-deletion process")
                    file.delete()
                } else {
                    setLog("FileDecryption", "decryptAudioContent-fileNotExist")
                }
                copyFile(currentFile.path, file.path)
            } catch (e: Exception) {
                setLog("FileDecryption", "decryptAudioContent-Exception-${e.message}")
            } catch (e: NoSuchFileException) {
                if (newFilePathUrl.contains(Constant.filePrefix)) {
                    val uri = URL(newFilePathUrl)
                    file = File(uri.toURI())
                    setLog(
                        "FileDecryption",
                        "NoSuchFileException-decryptAudioContent-uri-file-3-${file.path}"
                    )
                } else {
                    file = File(newFilePathUrl)
                    setLog(
                        "FileDecryption",
                        "NoSuchFileException-decryptAudioContent-newFilePathUrl-file-${file}"
                    )
                }
                copyFile(currentFilePathUrl, file.path)
            }
        }


    }

    fun getUserSubscriptionPlan(): Int {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        if (userSubscriptionDetail != null && userSubscriptionDetail.data?.subscription?.subscriptionStatus == 1) {
            val subscriptionTypeId = getUserSubscriptionTypeId(userSubscriptionDetail)
            setLog(
                "getUserSubscriptionPlan",
                "getUserSubscriptionPlan subscriptionTypeId:${subscriptionTypeId} getUserId:${
                    SharedPrefHelper.getInstance().getUserId()
                }"
            )

            if ("717783271".equals(SharedPrefHelper.getInstance().getUserId())) {
                return SUBSCRIPTION_TYPE_GOLD
            }
            when (subscriptionTypeId) {
                SUBSCRIPTION_TYPE_GOLD -> {
                    return SUBSCRIPTION_TYPE_GOLD
                }

                SUBSCRIPTION_TYPE_GOLD_WITH_ADS -> {
                    return SUBSCRIPTION_TYPE_GOLD_WITH_ADS
                }

                SUBSCRIPTION_TYPE_ADS_FREE -> {
                    return SUBSCRIPTION_TYPE_ADS_FREE
                }
            }
        }
        if ("717783271".equals(SharedPrefHelper.getInstance().getUserId())) {
            return SUBSCRIPTION_TYPE_GOLD
        }
        return SUBSCRIPTION_TYPE_FREE
    }

    fun isUserHasGoldSubscription(): Boolean {
        try {
            val subscriptionTypeId = getUserSubscriptionPlan()
            if (subscriptionTypeId == SUBSCRIPTION_TYPE_GOLD || subscriptionTypeId == SUBSCRIPTION_TYPE_GOLD_WITH_ADS) {
                return true
            }
            return false
        }catch (e:Exception){
            return false
        }
    }

    fun getMaxDownloadContentSize(context: Context): Int {
        var limit = SharedPrefHelper.getInstance().get(PrefConstant.Download_Limit, 0)
        if (isUserHasGoldSubscription()) {
            val userSubscriptionDetail = getUserSubscriptionProfileConfigData(context)
            if (userSubscriptionDetail?.download_limit != null) {
                if (userSubscriptionDetail.download_limit!! == 0) {
                    limit = 0
                }
                limit = userSubscriptionDetail.download_limit!!
            }
        }
        return limit
    }

    fun getAvailableDownloadContentSize(context: Context): Int {
        val totalDownloadedAudioContent =
            SharedPrefHelper.getInstance().getTotalDownloadedAudioContent()
        val maxDownloadLimit = getMaxDownloadContentSize(context)
        if (totalDownloadedAudioContent != null) {
            if (totalDownloadedAudioContent < maxDownloadLimit) {
                return maxDownloadLimit
            } else if (totalDownloadedAudioContent >= maxDownloadLimit) {
                return 0
            }
        }
        return maxDownloadLimit
    }

    @SuppressLint("LogNotTimber")
    fun userCanDownloadContent(
        context: Context,
        rootLayout: View?,
        downloadPlayCheck: DownloadPlayCheckModel,
        onUserSubscriptionUpdateCall: OnUserSubscriptionUpdate?,
        trigger_point: String = ""
    ): Boolean {
        //val contentId = "61409798"
        //val planName = "[TVOD]"
        val planType = getContentPlanType(downloadPlayCheck.planName)
        val planTxtName = getContentPlanName(downloadPlayCheck.planName)
        if (isUserHasGoldSubscription() && (planType == PlanTypes.SUBSCRIPTION.value || planType == PlanTypes.FREE.value)) {
            return true
        } else if (planType == PlanTypes.RENTAL.value) {
            if (isUserHasRentedSubscription(downloadPlayCheck.contentId)) {
                return true
            } else {
                openSubscriptionDialogPopup(
                    context,
                    planTxtName,
                    downloadPlayCheck.contentId,
                    downloadPlayCheck.isShowSubscriptionPopup,
                    onUserSubscriptionUpdateCall,
                    downloadPlayCheck.contentTitle,
                    downloadPlayCheck,
                    trigger_point
                )
                return false
            }
        } else if ((planType == PlanTypes.FREE.value || planType == PlanTypes.SUBSCRIPTION.value) && !downloadPlayCheck.isAudio) {
            if (planTxtName.contains(PlanNames.SVOD.name, true)) {
                openSubscriptionDialogPopup(
                    context,
                    planTxtName,
                    downloadPlayCheck.contentId,
                    downloadPlayCheck.isShowSubscriptionPopup,
                    onUserSubscriptionUpdateCall,
                    downloadPlayCheck.contentTitle,
                    downloadPlayCheck, trigger_point

                )
                return false
            } else if (planTxtName.contains(PlanNames.CVOD.name, true) || planTxtName.contains(
                    PlanNames.PCVOD.name, true
                )
            ) {
                return false
            } else {
                if (downloadPlayCheck.isDownloadAction) {
                    openSubscriptionDialogPopup(
                        context,
                        planTxtName,
                        downloadPlayCheck.contentId,
                        downloadPlayCheck.isShowSubscriptionPopup,
                        onUserSubscriptionUpdateCall,
                        downloadPlayCheck.contentTitle,
                        downloadPlayCheck,
                        trigger_point
                    )
                    return false
                } else {
                    return true
                }
            }
        } else if (downloadPlayCheck.isAudio && downloadPlayCheck.clickAction == ClickAction.FOR_SINGLE_CONTENT && downloadPlayCheck.restrictedDownload != RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT) {
            val audioContents =
                "(" + ContentTypes.AUDIO.value + "," + ContentTypes.PODCAST.value + ")"
            val contentTypes: Array<Int> =
                arrayOf(ContentTypes.AUDIO.value, ContentTypes.PODCAST.value)
            val allDownloadItemList =
                AppDatabase.getInstance()?.downloadedAudio()?.getContentsByContentType(contentTypes)
            setLog(TAG, "allDownloadItemList?.size:=> ${allDownloadItemList?.size}")
            setLog(
                "audioDownload",
                "CommonUtils-userCanDownloadContent-allDownloadItemList?.size-${allDownloadItemList?.size}"
            )
            setLog(
                "audioDownload",
                "CommonUtils-userCanDownloadContent-getMaxDownloadContentSize-${
                    getAvailableDownloadContentSize(context)
                }"
            )
            if (allDownloadItemList != null) {
                if (allDownloadItemList.size >= getAvailableDownloadContentSize(context)) {

                    openSubscriptionDialogPopup(
                        context,
                        planTxtName,
                        downloadPlayCheck.contentId,
                        downloadPlayCheck.isShowSubscriptionPopup,
                        onUserSubscriptionUpdateCall,
                        downloadPlayCheck.contentTitle,
                        downloadPlayCheck,
                        Constant.drawer_downloads_exhausted
                    )

                    if (SharedPrefHelper.getInstance()
                            .get(PrefConstant.COMPLETED_20_FREE_DOWNLOADS, true)
                    ) {
                        /* Track Events in real time */
                        val eventValue: MutableMap<String, Any> = HashMap()
                        AppsFlyerLib.getInstance().logEvent(
                            HungamaMusicApp.getInstance().applicationContext!!,
                            EventConstant.AF_COMPLETED_20_FREE_DOWNLOADS,
                            eventValue
                        )
                        SharedPrefHelper.getInstance()
                            .save(PrefConstant.COMPLETED_20_FREE_DOWNLOADS, false)
                    }
                    setLog(TAG, "download return=>")

                    return false
                } else {
                    return true
                }
            } else if (getAvailableDownloadContentSize(context) > 0) {
                return true
            }
            openSubscriptionDialogPopup(
                context,
                planTxtName,
                downloadPlayCheck.contentId,
                downloadPlayCheck.isShowSubscriptionPopup,
                onUserSubscriptionUpdateCall,
                downloadPlayCheck.contentTitle,
                downloadPlayCheck,
                trigger_point
            )
            return false
        }
        openSubscriptionDialogPopup(
            context,
            planTxtName,
            downloadPlayCheck.contentId,
            downloadPlayCheck.isShowSubscriptionPopup,
            onUserSubscriptionUpdateCall,
            downloadPlayCheck.contentTitle,
            downloadPlayCheck,
            trigger_point
        )
        return false
    }

    fun isUserHasRentedSubscription(contentId: String): Boolean {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        //val userSubscriptionDetail = getDummyPaySubscriptionData()
        if (!TextUtils.isEmpty(contentId) && userSubscriptionDetail != null && !userSubscriptionDetail.data?.tvod.isNullOrEmpty()) {
            for (item in userSubscriptionDetail.data?.tvod?.iterator()!!) {

                if (item?.contentId.equals(contentId) && item?.rentalStatus?.equals("1")!! && item.validity != 0) {
                    try {
                        val sdf = SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD_HH_MM_ss)
                        val strExpiryDate: Date = sdf.parse(item.expiryDate!!)
                        if (System.currentTimeMillis() < strExpiryDate.time) {
                            val contentDownloaded = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(contentId)
                            if (contentDownloaded != null) {
                                if (contentDownloaded.contentStreamDate > 0) {
                                    val contentStreamDate =
                                        convertLongTodateTime(contentDownloaded.contentStreamDate)
                                    val c = Calendar.getInstance()
                                    c.time = sdf.parse(contentStreamDate)
                                    c.add(Calendar.DATE, 2) // Adding 2 days for expire the content
                                    val newExpiryDate = sdf.format(c.time)

                                    return (System.currentTimeMillis() < sdf.parse(newExpiryDate).time && System.currentTimeMillis() < strExpiryDate.time)

                                } else {
                                    AppDatabase.getInstance()?.downloadedAudio()
                                        ?.updateDownloadedContentValidityData(
                                            item.startDate.toString(),
                                            item.expiryDate.toString(),
                                            item.validity?.toInt()!!,
                                            contentId
                                        )
                                }

                            }
                            return true
                        } else {
                            return false
                        }
                    } catch (e: Exception) {
                        return false
                    }
                }
            }
        }
        return false
    }


    fun isUserHasEliableFreeContent(): Boolean {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        return userSubscriptionDetail?.data?.Is_Free_Trial_Eligible?:false
    }

    fun isUserHasRentedContent(contentId: String): Boolean {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        //val userSubscriptionDetail = getDummyPaySubscriptionData()
        if (!TextUtils.isEmpty(contentId) && userSubscriptionDetail != null && !userSubscriptionDetail.data?.tvod.isNullOrEmpty()) {
            for (item in userSubscriptionDetail.data?.tvod?.iterator()!!) {
                if (item?.contentId.equals(contentId)) {
                    return true
                }
            }
        }
        return false
    }

    fun getRentedContentExpiryDate(contentId: String): String {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        //val userSubscriptionDetail = getDummyPaySubscriptionData()
        if (!TextUtils.isEmpty(contentId) && userSubscriptionDetail != null && !userSubscriptionDetail.data?.tvod.isNullOrEmpty()) {
            for (item in userSubscriptionDetail.data?.tvod?.iterator()!!) {
                if (item?.contentId.equals(contentId)) {
                    return item?.expiryDate.toString()
                }
            }
        }
        return ""
    }

    fun getRentedContentData(contentId: String): UserSubscriptionModel.Data.Tvod? {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        //val userSubscriptionDetail = getDummyPaySubscriptionData()
        if (!TextUtils.isEmpty(contentId) && userSubscriptionDetail != null && !userSubscriptionDetail.data?.tvod.isNullOrEmpty()) {
            for (item in userSubscriptionDetail.data?.tvod?.iterator()!!) {
                if (item?.contentId.equals(contentId)) {
                    return item
                }
            }
        }
        return null
    }

    fun isUserHasRentedEvent(context: Context, contentId: String): Boolean {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        //val userSubscriptionDetail = getDummyPaySubscriptionData()
        if (!TextUtils.isEmpty(contentId) && userSubscriptionDetail != null && !userSubscriptionDetail.data?.liveConcert.isNullOrEmpty()) {
            for (item in userSubscriptionDetail.data?.liveConcert?.iterator()!!) {

                if (item?.contentId.equals(contentId) && item?.rentalStatus?.equals("1")!! && !item.validity.equals(
                        "0",
                        true
                    )
                ) {
                    try {
                        val sdf = SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD_HH_MM_ss)
                        val strExpiryDate: Date = sdf.parse(item.expiryDate!!)
                        return System.currentTimeMillis() < strExpiryDate.time
                    } catch (e: Exception) {
                        return false
                    }
                }
            }
        }
        return false
    }

    @SuppressLint("LogNotTimber")
    fun getContentPlanType(planName: String): Int {
        /*if(planName.contains(PlanNames.TVOD.name, true)
            || planName.contains(PlanNames.PTVOD.name, true)){
            return PlanTypes.RENTAL.value
        }else{
            return PlanTypes.SUBSCRIPTION.value
        }*/
        val planList = getStringToArray(planName)
        var planType = PlanTypes.SUBSCRIPTION.value
        planList.forEach { it ->
            setLog("Values", "value=$it")
            if (it.contains(PlanNames.FVOD.name, true)) {
                planType = PlanTypes.FREE.value
                return planType
            } else if (it.contains(PlanNames.TVOD.name, true)) {
                planType = PlanTypes.RENTAL.value
                return planType
            } else if (it.contains(PlanNames.PTVOD.name, true)) {
                planType = PlanTypes.RENTAL.value
                return planType
            } else {
                planType = PlanTypes.SUBSCRIPTION.value
            }
        }
        return planType
    }

    @SuppressLint("LogNotTimber")
    fun getContentPlanName(planName: String): String {

        val planList = getStringToArray(planName)
        planList.forEach { it ->
            setLog("Values", "value=$it")

            if (it.contains(PlanNames.FVOD.name)) {
                return it
            } else if (it.contains(PlanNames.TVOD.name)) {
                return it
            } else if (it.contains(PlanNames.PTVOD.name)) {
                return it
            } else if (it.contains(PlanNames.SVOD.name)) {
                return it
            }
        }
        if (!planList.isNullOrEmpty()) {
            return planList.get(0).toString()
        }
        return ""
    }

    fun convertByteToHumanReadableFormat(bytes: Long): String? {
        var bytes = bytes
        if (-1000 < bytes && bytes < 1000) {
            if (bytes < 0) {
                bytes = 0
            }
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        if (bytes < 0) {
            bytes = 0
        }
        return java.lang.String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }

    fun getStringToArray(arrayString: String): List<String> {
        return try {
            var str = arrayString
            if (str.equals(PlanNames.NONE.name)) {
                str = listOf(str).toString()
            }
            val gson = Gson()
            gson.fromJson(str, Array<String>::class.java).toList()
        } catch (e: Exception) {
            ArrayList()
        }
    }

    fun getStringToIntArray(arrayString: String): List<Int> {
        return try {
            val gson = Gson()
            gson.fromJson(arrayString, Array<Int>::class.java).toList()
        } catch (e: Exception) {
            ArrayList()
        }
    }

    fun getCommaSeparatedStringToArray(commaSeparatedString: String): List<String> {
        return commaSeparatedString.split(",").map { it.trim() }
    }

    fun openSubscriptionDialogPopup(
        context: Context,
        planName: String,
        contentId: String,
        isShowSubscriptionPopup: Boolean,
        onUserSubscriptionUpdateCall: OnUserSubscriptionUpdate?,
        contentTitle: String,
        downloadPlayCheck: DownloadPlayCheckModel?,
        trigger_point: String = "",
        banner_type: String = "",
        onUserSubscriptionDialogIsFinish: OnUserSubscriptionDialogIsFinish? = null) {
        if (isShowSubscriptionPopup) {
            try {
                val subscriptionDialogBottomsheetFragment = SubscriptionDialogBottomsheetFragment(
                    context,
                    planName,
                    contentId,
                    onUserSubscriptionUpdateCall,
                    contentTitle,
                    downloadPlayCheck,
                    trigger_point,
                    banner_type, onUserSubscriptionDialogIsFinish
                )

                subscriptionDialogBottomsheetFragment.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    "subscriptionDialogBottomsheetFragment"
                )

            } catch (e: Exception) {

            }
        }
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun getDeviceId(context: Context): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getUserSubscriptionProfileConfigData(context: Context): UserSubscriptionModel.Data.ProfileAppConfig? {
        val userSubscriptionDetail =
            SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        if (userSubscriptionDetail != null && userSubscriptionDetail.data?.profile_app_config != null) {
            return userSubscriptionDetail.data?.profile_app_config
        } else {
            return null
        }
    }

    fun getStoredSelectedQualityId(qualityAction: QualityAction): Int? {
        return when (qualityAction) {
            QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY -> {
                SharedPrefHelper.getInstance().getMusicPlaybackStreamQualityId()
            }

            QualityAction.MUSIC_PLAYBACK_DOWNLOAD_QUALITY -> {
                SharedPrefHelper.getInstance().getMusicPlaybackDownloadQualityId()
            }

            QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY -> {
                SharedPrefHelper.getInstance().getVideoPlaybackStreamQualityId()
            }

            QualityAction.VIDEO_PLAYBACK_DOWNLOAD_QUALITY -> {
                SharedPrefHelper.getInstance().getVideoPlaybackDownloadQualityId()
            }

            else -> 0
        }
    }

    fun setStoredSelectedQualityId(qualityAction: QualityAction, id: Int, title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            setLog(
                "playbackQuality",
                "MusicPlaybackSettingQualityAdapter-setStoredSelectedQualityId-${qualityAction.qualityActionName}-Quality-${id} - title-${title}"
            )
            when (qualityAction) {
                QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY -> {
                    SharedPrefHelper.getInstance().setMusicPlaybackStreamQualityId(id)
                    SharedPrefHelper.getInstance().setMusicPlaybackStreamQualityTitle(title)
                }

                QualityAction.MUSIC_PLAYBACK_DOWNLOAD_QUALITY -> {
                    SharedPrefHelper.getInstance().setMusicPlaybackDownloadQualityId(id)
                    SharedPrefHelper.getInstance().setMusicPlaybackDownloadQualityTitle(title)
                }

                QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY -> {
                    SharedPrefHelper.getInstance().setVideoPlaybackStreamQualityId(id)
                    SharedPrefHelper.getInstance().setVideoPlaybackStreamQualityTitle(title)
                }

                QualityAction.VIDEO_PLAYBACK_DOWNLOAD_QUALITY -> {
                    SharedPrefHelper.getInstance().setVideoPlaybackDownloadQualityId(id)
                    SharedPrefHelper.getInstance().setVideoPlaybackDownloadQualityTitle(title)
                }

                else -> {}
            }
        }

    }


    fun copyToTheClipboard(context: Context, text: CharSequence) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    fun checkContentOrderStatus(paymentStatus: String, contentActive: Boolean): Int {
        if (paymentStatus.equals("success", true)) {
            if (contentActive) {
                return CONTENT_ORDER_STATUS_SUCCESS
            } else {
                return CONTENT_ORDER_STATUS_NA
            }

        } else if (paymentStatus.equals("pending", true)) {
            return CONTENT_ORDER_STATUS_PENDING
        } else if (paymentStatus.equals("in-process", true)) {
            return CONTENT_ORDER_STATUS_IN_PROCESS
        } else if (paymentStatus.equals("fail", true)) {
            return CONTENT_ORDER_STATUS_FAIL
        } else {
            return CONTENT_ORDER_STATUS_NA
        }
    }

    fun enableDisableView(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            val group = view
            for (idx in 0 until group.childCount) {
                enableDisableView(group.getChildAt(idx), enabled)
            }
        }
    }

    fun getPointOfView(view: View?): Point? {
        if (view != null) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            return Point(location[0], location[1])
        } else {
            return null
        }
    }

    fun Context.faDrawable(faResId: Int, colorRefId: Int) = DroidAwesome.DrawableBuilder(this)
        .icon(getString(faResId))
        .color(colorRefId)
        .build()

    fun Context.faDrawable(faResId: Int, colorRefId: Int, size: Float) =
        DroidAwesome.DrawableBuilder(this)
            .icon(getString(faResId))
            .color(colorRefId)
            .size(size)
            .build()

    fun isProbablyRunningOnEmulator(): Boolean {
        var result = false
        // Android SDK emulator
        result =
            (Build.FINGERPRINT.startsWith("google/sdk_gphone_") && Build.FINGERPRINT.endsWith(":user/release-keys") && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith(
                "sdk_gphone_"
            ) && Build.BRAND == "google" && Build.MODEL.startsWith("sdk_gphone_"))
                    //
                    || Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") || Build.MODEL.contains(
                "google_sdk"
            ) || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86")
                    //bluestacks
                    || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(
                Build.MANUFACTURER,
                ignoreCase = true
            ) //bluestacks
                    || Build.MANUFACTURER.contains("Genymotion") || Build.HOST == "Build2" //MSI App Player
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") || Build.PRODUCT == "google_sdk"
                    // another Android SDK emulator check
                    || SystemProperties.getProp("ro.kernel.qemu") == "1"
        return result
    }

    fun applyButtonTheme(context: Context, view: View) {
        if (getIsGoldUser()) {
            setAppButton2(context, view)
        } else {
            setAppButton1(context, view)
        }
    }

    //Button blue
    fun setAppButton1(context: Context, view: View) {
        val colors = intArrayOf(
            Color.parseColor("#E32E27"), Color.parseColor("#E32E27")
        )
        val position = floatArrayOf(
            0f, 1f
        )
        val startX = 84.564f
        val startY = 44.184f
        val endX = 268.92f
        val endY = 2.184f
        //val cornerRadius = 100f
        val cornerRadius = floatArrayOf(
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Top left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Top left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//top Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//top Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Bottom Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Bottom Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//bottom left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat()//bottom left
        )
        applyAppButtonGradient(
            startX,
            startY,
            endX,
            endY,
            cornerRadius,
            colors,
            position,
            context,
            view
        )
    }

    //Button gold
    fun setGoldUserViewStyle(context: Context, view: View) {
//        if (isUserHasGoldSubscription()){
//            if(view is TextView){
//                val tView:TextView=view
//                val typeface = ResourcesCompat.getFont(
//                    context,
//                    R.font.sf_pro_text_bold
//                )
//                tView.setTypeface(typeface!!,Typeface.NORMAL)
//                tView?.setTextColor(ContextCompat.getColor(view?.context!!,R.color.colorWhite))
//                setLog(TAG, "setAppButton2: TextView bold and color set ")
//            }else if(view is Button){
//                val btnView:Button=view
//                val typeface = ResourcesCompat.getFont(
//                    context,
//                    R.font.sf_pro_text_bold
//                )
//                btnView.setTypeface(typeface!!,Typeface.NORMAL)
//                btnView?.setTextColor(ContextCompat.getColor(view?.context!!,R.color.colorWhite))
//                setLog(TAG, "setAppButton2: Button bold and color set ")
//            }else{
//                setLog(TAG, "setAppButton2: view different")
//            }
//        }

    }

    //Button gold
    fun setAppButton2(context: Context, view: View) {
        val colors = intArrayOf(
            Color.parseColor("#b46a11"),
            Color.parseColor("#d68d15"),
            Color.parseColor("#e7ac18"),
            Color.parseColor("#f8c73d")
        )
        val position = floatArrayOf(
            0f,
            0.345f,
            0.675f,
            1f
        )
        val startX = -6.818111E-16f
        val startY = 21f
        val endX = 324f
        val endY = 21f
        //val cornerRadius = 100f
        val cornerRadius = floatArrayOf(
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Top left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Top left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//top Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//top Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Bottom Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Bottom Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//bottom left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat()//bottom left
        )
        applyAppButtonGradient(
            startX,
            startY,
            endX,
            endY,
            cornerRadius,
            colors,
            position,
            context,
            view
        )


    }

    //Button gray
    fun setAppButton3(context: Context, view: View) {
        val colors = intArrayOf(
            Color.parseColor("#C6C6C6"), Color.parseColor("#C6C6C6")
        )
        val position = floatArrayOf(
            0f, 1f
        )
        val startX = 34.191f
        val startY = 33.664f
        val endX = 108.73f
        val endY = 1.664f
        //val cornerRadius = 100f
        val cornerRadius = floatArrayOf(
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Top left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Top left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//top Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//top Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Bottom Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//Bottom Right
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat(),//bottom left
            context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat()//bottom left
        )
        applyAppButtonGradient(
            startX,
            startY,
            endX,
            endY,
            cornerRadius,
            colors,
            position,
            context,
            view
        )
    }

    //Button white
    fun setAppButton4(context: Context, view: View) {
        val colors = intArrayOf(
            Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF")
        )
        val position = floatArrayOf(
            0f, 1f
        )
        val startX = 34.191f
        val startY = 33.664f
        val endX = 108.73f
        val endY = 1.664f
        val corner = context.resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat()
        val cornerRadius = floatArrayOf(
            corner,//Top left
            corner,//Top left
            corner,//top Right
            corner,//top Right
            corner,//Bottom Right
            corner,//Bottom Right
            corner,//bottom left
            corner//bottom left
        )
        applyAppButtonGradient(
            startX,
            startY,
            endX,
            endY,
            cornerRadius,
            colors,
            position,
            context,
            view
        )
    }

    fun applyAppButtonGradient(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        cornerRadius: FloatArray,
        colors: IntArray,
        position: FloatArray,
        context: Context,
        view: View
    ) {
        MainScope().launch {
            val pd = PaintDrawable()
            withContext(Dispatchers.Default) {
                val sf: ShapeDrawable.ShaderFactory = object : ShapeDrawable.ShaderFactory() {
                    override fun resize(width: Int, height: Int): Shader? {
                        return LinearGradient(
                            startX,
                            startY,
                            endX,
                            endY,
                            colors,
                            position,  // start, center and end position
                            Shader.TileMode.CLAMP
                        )
                    }
                }


                pd.shape = RectShape()
                pd.setCornerRadii(cornerRadius)
                pd.shaderFactory = sf
            }
            view.background = pd
        }
    }

    fun applyAppLogo(context: Context, view: View) {
        MainScope().launch {
            if (isUserHasGoldSubscription()) {
                //view.layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.dimen_167_5)
                //setMargins(view, context.resources.getDimensionPixelSize(R.dimen.dimen_8))
                //view.requestLayout()
                //(view as AppCompatImageView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.gold_subscription_detail_bg))
                (view as AppCompatImageView).setImageResource(R.drawable.gold_subscription_detail_bg)

            } else {
                //view.layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.dimen_109_5)
                //setMargins(view, context.resources.getDimensionPixelSize(R.dimen.dimen_18))
                //view.requestLayout()
                //(view as AppCompatImageView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hungama_text_icon_new))
                (view as AppCompatImageView).setImageResource(R.drawable.hungama_text_icon_new)


            }
        }
    }

    fun getRedeemProductType(productType: String?): Int {
        if (productType.equals("TVOD", true) || productType.equals("PTVOD", true)) {
            return tvodProduct
        } else if (productType.equals("Subscription", true) || productType.equals("SVOD", true)) {
            return SubscriptionProduct
        } else if (productType.equals("COUPONS", true)) {
            return digitalProduct
        }
        return physicalProduct
    }

    fun getDeeplinkIntentData(appLinkData: Uri): Intent = runBlocking(Dispatchers.IO) {
        val pathArray = appLinkData.pathSegments
        var pageName = ""
        var pageDetailName = ""
        var pageDetailId = ""
        var pageContentPlay = ""
        var pageContentPayment = ""
        var isSeason = false
        var seasonNumber = 0
        var isMorePage = false
        var morePageName = ""
        var isSubTabSelected = false
        var isCategoryPage = false
        var categoryName = ""
        var categoryId = ""
        var isTrailer = false
        var trailerId = ""
        var isEpisode = false
        var episodeName = ""
        var episodeId = ""
        var queryParam = ""
        var liveShowArtistId = ""

        if (!pathArray.isNullOrEmpty()) {

            setLog("deepLinkUrl", "pathArray.size:${pathArray.size}")

            if (pathArray.size > 0) {
                pageName = pathArray.get(0).lowercase()
            }

            if (pathArray.size > 1) {
                val text = pathArray.get(1)
                setLog("deepLinkUrl", "pathArray-1-text:${text}")
                val categoryPageName = checkDeeplinkCategory(text)
                setLog(
                    "deepLinkUrl",
                    "getDeeplinkIntentData--categoryName=$text && categoryPageName=$categoryPageName"
                )
                if (!TextUtils.isEmpty(categoryPageName) && !pageName.equals("user-profile")) {
                    isCategoryPage = true
                    categoryName = text
                    pageDetailName = categoryPageName
                } else if (pageName.equals("games", true) || pageName.equals(
                        "stories",
                        true
                    ) || pageName.equals("user-playlist", true)
                ) {
                    pageDetailId = text
                } else if (pageName.equals("video", true)) {
                    morePageName = text
                } else {
                    pageDetailName = text
                }
            }

            if (pathArray.size > 2) {
                val text = pathArray[2].toString()
                if (isCategoryPage) {
                    categoryId = text
                } else if (pageName.equals("artist", true)
                    && (text.equals("songs", true)
                            || text.equals("movies", true)
                            || text.equals("albums", true)
                            || text.equals("videos", true))
                ) {
                    isMorePage = true
                    morePageName = text
                } else if (pageName.equals("library", true)) {
                    isSubTabSelected = true
                    morePageName = text
                } else if (pageName.equals("user-profile", true)) {
                    morePageName = text
                } else if (pageName.equals("video", true)) {
                    pageDetailId = pathArray[2].lowercase()
                    pageDetailName = text
                } else {
                    pageDetailId = text
                }
            }

            if (pathArray.size > 3) {
                val text = pathArray[3].toString()
                setLog("pathArray", "pathArray.get(3).toString():${text}")
                if (text.contains("season", true)) {
                    isSeason = true
                } else if (isMorePage) {
                    pageDetailId = text
                } else if (pageName.equals("video", true) && morePageName.equals(
                        "short-video",
                        true
                    )
                ) {
                    pageDetailId = text
                } else if (text == "tailer") {
                    isTrailer = true
                } else if (text.equals("payment", ignoreCase = true)) {
                    pageContentPayment = text
                } else if (text.equals("play", ignoreCase = true)) {
                    pageDetailId = pathArray.get(2).lowercase()
                    pageContentPlay = text
                } else {
                    liveShowArtistId = text
                }
            }
            if (pathArray.size > 4) {
                val text = pathArray.get(4).toString()
                if (isSeason) {
                    seasonNumber = text.toInt()
                } else if (isSubTabSelected) {
                    pageDetailId = text
                } else if (isTrailer) {
                    trailerId = text
                } else if (text.equals("payment", ignoreCase = true)) {
                    pageContentPayment = text
                } else if (text.contains("episode", true)) {
                    isEpisode = true
                    if (pathArray.size >= 7) {
                        pageDetailId = pathArray.get(3).lowercase()
                        episodeId = pathArray.get(6)
                    }
                }
            }
            if (pathArray.size > 5) {
                val text = pathArray[5].toString()
                if (text.contains("episode", true)) {
                    isEpisode = true
                    if (pathArray.size >= 7) {
                        pageDetailId = pathArray[3].lowercase()
                        episodeId = pathArray[6]
                    }
                }
            }
            if (pathArray.size > 6) {
                val text = pathArray[6].toString()
                if (isEpisode) {
                    episodeName = text
                }


            }

            if (pathArray.size > 7) {
                val text = pathArray[7].toString()
            }
        }

        try {
            if (appLinkData.query != null) {
                queryParam = appLinkData?.query!!
            }

        } catch (exp: Exception) {
            exp.printStackTrace()
        }

        setLog(TAG, "getDeeplinkIntentData: queryParam:${queryParam}")


        val intent = Intent()
        intent.putExtra(Constant.EXTRA_PAGE_NAME, pageName)
        intent.putExtra(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
        intent.putExtra(Constant.EXTRA_PAGE_DETAIL_ID, pageDetailId)
        intent.putExtra(Constant.EXTRA_PAGE_CONTENT_PLAY, pageContentPlay)
        intent.putExtra(Constant.EXTRA_PAGE_CONTENT_PAYMENT, pageContentPayment)
        intent.putExtra(Constant.EXTRA_QUERYPARAM, "" + queryParam)
        intent.putExtra(Constant.EXTRA_IS_SEASON, isSeason)
        intent.putExtra(Constant.EXTRA_SEASON_NUMBER, seasonNumber)
        intent.putExtra(Constant.EXTRA_IS_MORE_PAGE, isMorePage)
        intent.putExtra(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
        intent.putExtra(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
        intent.putExtra(Constant.EXTRA_IS_CATEGORY_PAGE, isCategoryPage)
        intent.putExtra(Constant.EXTRA_CATEGORY_NAME, categoryName)
        intent.putExtra(Constant.EXTRA_CATEGORY_ID, categoryId)
        intent.putExtra(Constant.EXTRA_IS_TRAILER, isTrailer)
        intent.putExtra(Constant.EXTRA_TRAILER_ID, trailerId)
        intent.putExtra(Constant.EXTRA_IS_EPISODE, isEpisode)
        intent.putExtra(Constant.EXTRA_EPISODE_NAME, episodeName)
        intent.putExtra(Constant.EXTRA_EPISODE_ID, episodeId)
        intent.putExtra(Constant.EXTRA_APPLINKURL, "" + appLinkData)
        intent.putExtra(Constant.EXTRA_LIVESHOWARTIST_ID, "" + liveShowArtistId)
        intent
    }

    fun openCommonWebView(link: String, context: Context) {
        val intent = Intent()
        intent.setClass(context, CommonWebViewActivity::class.java)
        intent.putExtra(Constant.EXTRA_URL, link)
        context.startActivity(intent)
    }

    private var adsConfigModel: AdsConfigModel? = null
    private var lastTimeWhenFCConfingCalled: Long = 0

    fun getFirebaseConfigHEAPIData(): String {
        try {

            if (!Constant.HE_API_URL.isNullOrBlank()) {
                setLog("he_api", "getFirebaseConfigHEAPIData-1-heApi=${Constant.HE_API_URL}")
                return Constant.HE_API_URL
            }
            val remoteConfig = Firebase.remoteConfig
            val heApiData = remoteConfig.getString("he_api")
            setLog("he_api", "getFirebaseConfigHEAPIData-2-heApiData=$heApiData")


            val dataModel = Gson().fromJson<AdsConfigModel.HEApi>(
                heApiData.toString(), AdsConfigModel.HEApi::class.java
            ) as AdsConfigModel.HEApi

            Constant.HE_API_URL = dataModel.url

            setLog("he_api", "getFirebaseConfigHEAPIData--heApi dataModel=${Constant.HE_API_URL}")
        } catch (e: Exception) {
            setLog("FConfigObject", e.toString())
        }

        return Constant.HE_API_URL
    }

    fun getNudgeAudioId(): String {
        var audioIds = ""
            if (isUserHasEliableFreeContent()) {
                val firAudioId = getSongDurationConfig().nudge_stream_preview.ft.audio_id
                if (firAudioId != null) {
                    audioIds = convertAudioId(firAudioId.toString())
                }
            } else {
                val firAudioId =
                    getSongDurationConfig().nudge_stream_preview.nonft.audio_id
                if (firAudioId != null) {
                    audioIds = convertAudioId(firAudioId.toString())
                }
            }
        return audioIds
    }

    fun getDrawerAudioId(): String {
        var audioIds = ""
        if (isUserHasEliableFreeContent()) {
            val firAudioId = getSongDurationConfig().drawer_minute_quota_exhausted.ft.audio_id
            if (firAudioId != null) {
                audioIds = convertAudioId(firAudioId.toString())
            }
        } else {
            val firAudioId =
                getSongDurationConfig().drawer_minute_quota_exhausted.nonft.audio_id
            if (firAudioId != null) {
                audioIds = convertAudioId(firAudioId.toString())
            }
        }
        return audioIds
    }

    fun getNudgePlanId(): String {
        var planId = ""
        if (!isUserHasGoldSubscription()) {
            planId = if (isUserHasEliableFreeContent()) {
                getSongDurationConfig().nudge_stream_preview.ft.plan_id

            } else {
                getSongDurationConfig().nudge_stream_preview.nonft.plan_id
            }
        }
        return planId
    }

    fun getNudgeMinuteQuotaExhaustedPlanId(): String {
        var planId = ""
        if (!isUserHasGoldSubscription()) {
            planId = if (isUserHasEliableFreeContent()) {
                getSongDurationConfig().nudge_minute_quota_exhausted.ft.plan_id

            } else {
                getSongDurationConfig().nudge_minute_quota_exhausted.nonft.plan_id
            }
        }
        return planId
    }

    fun getDrawerPlanId(): String {
        var planId = ""
        if (!isUserHasGoldSubscription()) {
            planId = if (isUserHasEliableFreeContent()) {
                getSongDurationConfig().drawer_minute_quota_exhausted.ft.plan_id

            } else {
                getSongDurationConfig().drawer_minute_quota_exhausted.nonft.plan_id
            }
        }
        return planId
    }

    fun getDesignTemplateId(): Int {
        var templateId = 1
            if (isUserHasEliableFreeContent()) {
                templateId = getSongDurationConfig().drawer_minute_quota_exhausted.ft.design_template_id

            } else {
                templateId = getSongDurationConfig().drawer_minute_quota_exhausted.nonft.design_template_id
            }
        return templateId
    }

    fun getNudgeImageUrl(): String {
        var imageUrl = ""
            imageUrl = if (isUserHasEliableFreeContent()) {
                getSongDurationConfig().nudge_stream_preview.ft.image_url

            } else {
                getSongDurationConfig().nudge_stream_preview.nonft.image_url
            }
        return imageUrl
    }

    fun getSongDurationConfig(): SongDurationConfigModel = runBlocking(Dispatchers.IO) {
        val remoteConfig = Firebase.remoteConfig
        val durationConfigModel = SongDurationConfigModel()
        durationConfigModel.enable_minutes_quota = remoteConfig.getBoolean("enable_minutes_quota")
        val nudge_stream_preview = remoteConfig.getString("nudge_stream_preview")
/*        val jsonObjec_nudge_stream_preview = JSONObject(nudge_stream_preview)
        durationConfigModel.nudge_stream_preview = Gson().fromJson(jsonObjec_nudge_stream_preview.toString(), SongDurationConfigModel.DrawerMinuteQuotaExhausted::class.java)*/
        val global_limited_minutes_quota = remoteConfig.getString("global_limited_minutes_quota")
        durationConfigModel.global_limited_minutes_quota =
            if (!global_limited_minutes_quota.isNullOrEmpty()) global_limited_minutes_quota.toInt() else 0
        durationConfigModel.is_free_trial_eligible = remoteConfig.getBoolean("is_free_trial_eligible")
        val drawer_minute_quota_exhausted = remoteConfig.getString("drawer_minute_quota_exhausted")
/*        val jsonObjec = JSONObject(drawer_minute_quota_exhausted)
        durationConfigModel.drawer_minute_quota_exhausted = Gson().fromJson(
            jsonObjec.toString(),
            SongDurationConfigModel.DrawerMinuteQuotaExhausted::class.java
        )*/
        val nudge_minute_quota_exhausted = remoteConfig.getString("nudge_minute_quota_exhausted")
/*        val jsonObject = JSONObject(nudge_minute_quota_exhausted)
        durationConfigModel.nudge_minute_quota_exhausted = Gson().fromJson(
            jsonObject.toString(),
            SongDurationConfigModel.DrawerMinuteQuotaExhausted::class.java
        )*/
        val global_limited_stream_preview_quota = remoteConfig.getString("global_limited_stream_preview_quota")
        durationConfigModel.global_limited_stream_preview_quota = if (!global_limited_stream_preview_quota.isNullOrEmpty()) global_limited_stream_preview_quota.toInt() else 0
        setLog("lahghoas", durationConfigModel.toString())
        durationConfigModel
    }

    fun convertAudioId(str: String): String {
        val audioId = ""
        val map: MutableMap<String, String> = HashMap()
        var sIterator: Iterator<String>? = null
        try {
            sIterator = JSONObject(str).keys()
            while (sIterator.hasNext()) {
                // get key
                val key = sIterator.next()
                // get value
                val value: String = JSONObject(str).getString(key)
                map[key] = value
            }

            if (!map.isNullOrEmpty()) {
                val currentLang = SharedPrefHelper.getInstance().getLanguage()
                setLog("objectDataFromeFirebase ", currentLang + " " + map.toString()+ " " + map[currentLang].toString())
                return map[currentLang].toString()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return audioId
    }

    fun getFirebaseConfigAdsData(): AdsConfigModel = runBlocking(Dispatchers.IO) {
        var timeDiff: Long = 0
        try {
            timeDiff =
                TimeUnit.MILLISECONDS.toSeconds(curreentTimeStamp() - lastTimeWhenFCConfingCalled)
            setLog("AppAds", "getFirebaseConfigAdsData-1-timeDiff-$timeDiff")
        } catch (e: Exception) {
            timeDiff = 31
            setLog("AppAds", "getFirebaseConfigAdsData-2")
        }

        if (timeDiff < 30 && adsConfigModel != null) {
            setLog("AppAds", "getFirebaseConfigAdsData-3")
            adsConfigModel as AdsConfigModel
        } else {
            setLog("AppAds", "getFirebaseConfigAdsData-4")
            lastTimeWhenFCConfingCalled = curreentTimeStamp()
            adsConfigModel = AdsConfigModel()
            try {
                val remoteConfig = Firebase.remoteConfig
                val splashAd = remoteConfig.getString("splash_ad")
                val newUserCoolingPeriod = remoteConfig.getString("new_user_cooling_period")
                val serveDisplayAd = remoteConfig.getBoolean("serve_display_Ad")
                val serveAudioAd = remoteConfig.getBoolean("serve_audio_Ad")
                val servePrerollAd = remoteConfig.getBoolean("serve_preroll_Ad")
                val audioAdPreference = remoteConfig.getString("audio_ad_preference")
                val homescreenBannerAds = remoteConfig.getString("homescreen_banner_ads")
                setLog(
                    "homescreenBannerAds",
                    "splash: " + splashAd + " serveAudioAd: " + serveAudioAd.toString() + " homescreenBannerAds:" + homescreenBannerAds.toString() + " Preroll: " + servePrerollAd.toString()
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

                val fConfigJsonObject = JsonObject()
                fConfigJsonObject.add("splash_ad", convertStringToJsonObject(splashAd))
                fConfigJsonObject.add(
                    "new_user_cooling_period", convertStringToJsonObject(newUserCoolingPeriod)
                )
                fConfigJsonObject.addProperty("serve_display_Ad", serveDisplayAd)
                setLog("ServeDisplayedAdd", serveDisplayAd.toString())
                fConfigJsonObject.addProperty("serve_audio_Ad", serveAudioAd)
                fConfigJsonObject.addProperty("serve_preroll_Ad", servePrerollAd)
                fConfigJsonObject.add(
                    "audio_ad_preference", convertStringToJsonObject(audioAdPreference)
                )
                fConfigJsonObject.add(
                    "homescreen_banner_ads", convertStringToJsonObject(homescreenBannerAds)
                )
                fConfigJsonObject.add(
                    "on_player_overlay", convertStringToJsonObject(onPlayerOverlay)
                )
                fConfigJsonObject.add(
                    "playlist_details_page", convertStringToJsonObject(playlistDetailsPage)
                )
                fConfigJsonObject.add(
                    "video_player_portrait_native_ad",
                    convertStringToJsonObject(videoPlayerPortraitNativeAd)
                )
                fConfigJsonObject.add(
                    "podcast_details_page_native_ad",
                    convertStringToJsonObject(podcastDetailsPageNativeAd)
                )
                fConfigJsonObject.add(
                    "chart_listing_screen", convertStringToJsonObject(chartListingScreen)
                )
                fConfigJsonObject.add(
                    "podcast_listing_screen", convertStringToJsonObject(podcastListingScreen)
                )
                fConfigJsonObject.add(
                    "radio_listing_screen", convertStringToJsonObject(radioListingScreen)
                )
                fConfigJsonObject.add(
                    "music_video_listing_screen", convertStringToJsonObject(musicVideoListingScreen)
                )
                fConfigJsonObject.add(
                    "movies_listing_screen", convertStringToJsonObject(moviesListingScreen)
                )
                fConfigJsonObject.add(
                    "tv_shows_listing_screen", convertStringToJsonObject(tvShowsListingScreen)
                )
                fConfigJsonObject.add("he_api", convertStringToJsonObject(he_api))

                fConfigJsonObject.add(
                    "drawer_download_all",
                    convertStringToJsonObject(drawer_download_all)
                )

                fConfigJsonObject.add(
                    "drawer_download_mymusic",
                    convertStringToJsonObject(drawer_download_mymusic)
                )
                fConfigJsonObject.add(
                    "drawer_downloads_exhausted",
                    convertStringToJsonObject(drawer_downloads_exhausted)
                )
                fConfigJsonObject.add(
                    "drawer_remove_ads",
                    convertStringToJsonObject(drawer_remove_ads)
                )
                fConfigJsonObject.add(
                    "drawer_streaming_quality",
                    convertStringToJsonObject(drawer_streaming_quality)
                )
                fConfigJsonObject.add(
                    "drawer_svod_download",
                    convertStringToJsonObject(drawer_svod_download)
                )
                fConfigJsonObject.add(
                    "drawer_restricted_download",
                    convertStringToJsonObject(drawer_restricted_download)
                )
                fConfigJsonObject.add(
                    "drawer_svod_purchase",
                    convertStringToJsonObject(drawer_svod_purchase)
                )
                fConfigJsonObject.add(
                    "drawer_svod_tvshow_episode",
                    convertStringToJsonObject(drawer_svod_tvshow_episode)
                )
                fConfigJsonObject.add(
                    "nudge_album_banner",
                    convertStringToJsonObject(nudge_album_banner)
                )
                fConfigJsonObject.add(
                    "nudge_playlist_banner",
                    convertStringToJsonObject(nudge_playlist_banner)
                )
                fConfigJsonObject.add(
                    "nudge_player_banner",
                    convertStringToJsonObject(nudge_player_banner)
                )
                fConfigJsonObject.add(
                    "nudge_home_header_banner",
                    convertStringToJsonObject(nudge_home_header_banner)
                )

                fConfigJsonObject.add(
                    "drawer_default_buy_hungama_gold",
                    convertStringToJsonObject(drawer_default_buy_hungama_gold)
                )
                fConfigJsonObject.addProperty("enable_payment_drawer", enable_payment_drawer)
                fConfigJsonObject.addProperty("enable_payment_nudge", enable_payment_nudge)


                getSongDurationConfig()
                //setLog("FConfigObject", fConfigJsonObject.toString())
                /*var splashAdData = AdsConfigModel.SplashAd()
                if (!TextUtils.isEmpty(splashAd)){
                    splashAdData = Gson().fromJson<AdsConfigModel.SplashAd>(
                        splashAd,
                        AdsConfigModel.SplashAd::class.java
                    ) as AdsConfigModel.SplashAd
                }*/

                if (!TextUtils.isEmpty(fConfigJsonObject.toString())) {
                    adsConfigModel = Gson().fromJson<AdsConfigModel>(
                        fConfigJsonObject, AdsConfigModel::class.java
                    ) as AdsConfigModel
                }
            } catch (e: Exception) {
                //setLog("FConfigObject", e.toString())
            }
            setLog("FConfigObject", adsConfigModel.toString())
            adsConfigModel as AdsConfigModel
        }
    }

    private fun removeQuotesAndUnescape(jsonString: String): String {
        return jsonString.replace("\\", "")
    }

    fun convertStringToJsonObject(jsonString: String): JsonObject {
        return try {
            JsonParser.parseString(jsonString).asJsonObject
        } catch (e: Exception) {
            JsonObject()
        }
    }

    fun isUserHasNoAdsSubscription(): Boolean = runBlocking(Dispatchers.IO) {
        var isUserHasNoAdsSubscription = false
        val subscriptionTypeId = withContext(Dispatchers.IO) { getUserSubscriptionPlan() }
        if (subscriptionTypeId == SUBSCRIPTION_TYPE_GOLD || subscriptionTypeId == SUBSCRIPTION_TYPE_ADS_FREE) {
            isUserHasNoAdsSubscription = true
        }
        isUserHasNoAdsSubscription
    }

    fun isAdsEnable(): Boolean = runBlocking(Dispatchers.IO) {
        var isAdsEnable = true
        setLog("AppAds", "isAdsEnable-1-$isAdsEnable")
        val isUserHasNoAdsSubscription =
            withContext(Dispatchers.IO) { !BaseActivity.getIsNoAdsUser() }
        setLog("AppAds", "isAdsEnable-2-$isUserHasNoAdsSubscription")
        val getNewUserCoolingPeriodDay =
            withContext(Dispatchers.IO) { getNewUserCoolingPeriodDay() }
        setLog("AppAds", "isAdsEnable-3-$getNewUserCoolingPeriodDay")
        val getUserAppInstallDay = withContext(Dispatchers.IO) { getUserAppInstallDay() }
        setLog("AppAds", "isAdsEnable-4-$getUserAppInstallDay")
        isAdsEnable =
            isUserHasNoAdsSubscription && (getNewUserCoolingPeriodDay < getUserAppInstallDay || getNewUserCoolingPeriodDay == 0)
        setLog("AppAds", "isAdsEnable-5-$isAdsEnable")
        isAdsEnable
    }


    fun isDisplayAds(): Boolean {
        //setLog("MyApp", "CommonUtils-isDisplayAds-1-"+getFirebaseConfigAdsData().serveDisplayAd)
        //setLog("MyApp", "CommonUtils-isDisplayAds-2-"+isAdsEnable())
        return getFirebaseConfigAdsData().serveDisplayAd && isAdsEnable()
    }

    fun isAudioAds(): Boolean {
        return getFirebaseConfigAdsData().serveAudioAd && isAdsEnable()
    }

    fun isPrerollAds(): Boolean {
        return getFirebaseConfigAdsData().servePrerollAd && isAdsEnable()
    }

    fun getNewUserCoolingPeriodDay(): Int {
        return getFirebaseConfigAdsData().newUserCoolingPeriod.coolingDays
    }

    fun getSplashWaitingTime(): Int {
        return getFirebaseConfigAdsData().splashAd.maxWaiting
    }

    fun getSplashAdsType(): String {
        return getFirebaseConfigAdsData().splashAd.splashType
    }

    fun getAudioAdPreference(): AdsConfigModel.AudioAdPreference {
        return getFirebaseConfigAdsData().audioAdPreference
    }

    fun getHomescreenBannerAds(): AdsConfigModel.HomescreenBannerAds {
        return getFirebaseConfigAdsData().homescreenBannerAds
    }

    fun isHomeScreenBannerAds(): Boolean {
        return isDisplayAds() && getHomescreenBannerAds().displayAd
    }

    fun getOnPlayerOverlayAds(): AdsConfigModel.OnPlayerOverlay {
        return getFirebaseConfigAdsData().onPlayerOverlay
    }

    fun isOnPlayerOverlayAds(): Boolean {
        return isDisplayAds() && getOnPlayerOverlayAds().displayAd
    }

    /**
     * Is the screen of the device on.
     * @param context the context
     * @return true when (at least one) screen is on
     */
    fun isScreenOn(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            var screenOn = false
            for (display in dm.displays) {
                if (display.state != Display.STATE_OFF) {
                    screenOn = true
                }
            }
            screenOn
        } else {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isScreenOn
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun getUserAppInstallDay(): Int {
        try {
            val today = getCurrentDate()
            val appInstallDate = SharedPrefHelper.getInstance().getUserAppInstallDate().toString()
            //val appInstallDate = "2021-11-25"
            var day = 0
            if (!TextUtils.isEmpty(appInstallDate)) {
                val date1: Date
                val date2: Date
                val dates = SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD)
                date1 = dates.parse(today)
                date2 = dates.parse(appInstallDate)
                val difference: Long = abs(date1.time - date2.time)
                val differenceDates = difference / (24 * 60 * 60 * 1000)
                day = differenceDates.toInt()
            }
            //setLog("RemoteWorkDone-Days", day.toString())
            return day
        } catch (e: Exception) {

        }
        return 0
    }

    fun loadBannerAds(
        context: Context,
        adUnitId: String,
        adSize: AdSize,
        frameLayout: FrameLayout
    ): AdManagerAdView {
        val adView = AdManagerAdView(context)
        frameLayout.addView(adView)
        adView.adUnitId = adUnitId
        adView.setAdSize(adSize)
        CoroutineScope(Dispatchers.Main).launch {
            // Create an ad request.
            val adRequest = AdManagerAdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
        return adView
    }

    fun isBluetoothDeviceConnected(device: BluetoothDevice?): Boolean {
        if (device != null) {
            try {
                return device::class.java.getMethod("isConnected").invoke(device) as Boolean
            } catch (e: java.lang.Exception) {
                return false
            }
        } else {
            return false
        }
    }

    fun getCurrentConnectedBTDevice(context: Context): BluetoothDevice? {
        /*try {
             val mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter()
             if(mBluetoothAdapter != null){
                 val pairedDevices = mBluetoothAdapter?.bondedDevices
                 setLog("Connected Devices: ", pairedDevices.size.toString() + "")
                 if(!pairedDevices.isNullOrEmpty()) {
                     for (device in pairedDevices.iterator()){
                         if (isBluetoothDeviceConnected(device)){
                             return device
                         }
                     }
                 }
             }
         }catch (e:Exception){

         }*/
        return null
    }

    fun setLog(logName: String = "hungamaLogs-:", logValue: String = "hungamaLogs") {
        if (BuildConfig.DEBUG) {
            Log.d(logName, logValue + " Thread:${Thread.currentThread().name}")
        }
    }

    fun setPageBottomSpacing(
        view: View?,
        context: Context,
        startPadding: Int,
        topPadding: Int,
        endPadding: Int,
        bottomPadding: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val mStartPadding = startPadding
                val mTopPadding = topPadding
                setLog("setPageBottomSpacing", "CommonUtils-mTopPadding: $mTopPadding")
                val mEndPadding = endPadding
                var mBottomPadding = bottomPadding
                val miniplayerHeight = (context as MainActivity).miniplayerHeight
                val bottomNavigationHeight = context.bottomNavigationHeight
                mBottomPadding += miniplayerHeight + bottomNavigationHeight
                setLog("setPageBottomSpacing", "CommonUtils-miniplayerHeight: $miniplayerHeight")
                setLog(
                    "setPageBottomSpacing",
                    "CommonUtils-bottomNavigationHeight: $bottomNavigationHeight"
                )
                setLog(
                    "setPageBottomSpacing",
                    "CommonUtils-isBottomStickyAdLoaded: ${context.isBottomStickyAdLoaded}"
                )
                if (isHomeScreenBannerAds() && context.isBottomNavigationVisible && context.isBottomStickyAdLoaded) {
                    mBottomPadding += context.resources.getDimensionPixelSize(R.dimen.dimen_70)
                    setLog(
                        "setPageBottomSpacing",
                        "CommonUtils-stickyAdHeight: ${context.resources.getDimensionPixelSize(R.dimen.dimen_70)}"
                    )
                }
                val extraSpace = context.resources.getDimensionPixelSize(R.dimen.dimen_10)
                setLog("setPageBottomSpacing", "CommonUtils-extraSpace: $extraSpace")
                mBottomPadding += extraSpace
                setLog("setPageBottomSpacing", "CommonUtils-totalBottomSpace: $mBottomPadding")
                view?.setPadding(mStartPadding, mTopPadding, mEndPadding, mBottomPadding)
                view?.requestLayout()
            } catch (e: Exception) {

            }
        }
    }

    private var userGeneralSettingRespModel: UserSettingRespModel? = null
    private var lastTimeWhenExplicitContentCalled: Long = 0
    private val expiryTime = 10L
    fun setExplicitContent(
        context: Context,
        mainView: View,
        explicit: Int,
        ivExplicit: ImageView? = null
    ) {
        var timeDiff: Long = 0
        try {
            timeDiff =
                TimeUnit.MILLISECONDS.toSeconds(curreentTimeStamp() - lastTimeWhenExplicitContentCalled)
            //setLog("AppAds", "setExplicitContent-1-timeDiff-$timeDiff")
        } catch (e: Exception) {
            timeDiff = expiryTime + 1
            //setLog("AppAds", "setExplicitContent-2")
        }
        //setLog("AppAds", "setExplicitContent-timeDiff-$timeDiff-userSettingRespModel-$userGeneralSettingRespModel")
        if (timeDiff < expiryTime && userGeneralSettingRespModel != null) {
            setLog("AppAds", "setExplicitContent-3")
            //userSettingRespModel as UserSettingRespModel
        } else {
            //setLog("AppAds", "setExplicitContent-4")
            lastTimeWhenExplicitContentCalled = curreentTimeStamp()
            userGeneralSettingRespModel =
                SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_GENERAL_SETTING)
        }

        var isExplicitAllow = false
        if (userGeneralSettingRespModel?.data != null) {
            isExplicitAllow =
                userGeneralSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitAudioContent!!
        }
        if (explicit == 1) {
            if (ivExplicit != null) {
                val drawable = FontDrawable(context, R.string.icon_explicit)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivExplicit.setImageDrawable(drawable)
                ivExplicit.visibility = View.VISIBLE
            }

            if (isExplicitAllow) {
                mainView.alpha = 0.4F
            } else {
                mainView.alpha = 1F
            }
        } else {
            if (ivExplicit != null) {
                ivExplicit.visibility = View.GONE
            }
            mainView.alpha = 1F
        }
    }

    fun checkExplicitContent(context: Context?, explicit: Int, showPopup: Boolean = true): Boolean {
        var timeDiff: Long = 0
        try {
            timeDiff =
                TimeUnit.MILLISECONDS.toSeconds(curreentTimeStamp() - lastTimeWhenExplicitContentCalled)
            //setLog("AppAds", "checkExplicitContent-1-timeDiff-$timeDiff")
        } catch (e: Exception) {
            timeDiff = expiryTime + 1
            //setLog("AppAds", "checkExplicitContent-2")
        }
        //setLog("AppAds", "setExplicitContent-timeDiff-$timeDiff-userSettingRespModel-$userGeneralSettingRespModel")
        if (timeDiff < expiryTime && userGeneralSettingRespModel != null) {
            //setLog("AppAds", "checkExplicitContent-3")
        } else {
            //setLog("AppAds", "checkExplicitContent-4")
            lastTimeWhenExplicitContentCalled = curreentTimeStamp()
            userGeneralSettingRespModel =
                SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_GENERAL_SETTING)
        }

        var isExplicitNotAllow = false
        if (userGeneralSettingRespModel?.data != null) {
            isExplicitNotAllow =
                userGeneralSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitAudioContent!!
        }
        setLog("ExplictValue", "${explicit}")
        setLog("isExplicitNotAllow", "${isExplicitNotAllow}")
        return if (explicit == 1) {
            if (isExplicitNotAllow) {
                if (context != null) {
                    setLog("Here", "${isExplicitNotAllow}")

                    openExplicitSettingPopup(context, showPopup)
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun openExplicitSettingPopup(context: Context, showPopup: Boolean) {
        if (showPopup) {
            //Toast.makeText(context, "To play it, go to Settings and allow \n" + "explicit content.", Toast.LENGTH_LONG).show()
            val sheet = ParentalControlPopup(context, true)
            sheet.show(
                (context as AppCompatActivity).supportFragmentManager,
                "ParentalControlPopup"
            )
        }
    }

    fun open13PlusVideoSettingPopup(context: Context, showPopup: Boolean) {
        if (showPopup) {
            setLog(
                "open13PlusVideoSettingPopup",
                "open13PlusVideoSettingPopup showPopup:${showPopup}"
            )
            //Toast.makeText(context, "To play it, go to Settings and allow \n" + "explicit content.", Toast.LENGTH_LONG).show()
            val sheet = ParentalControlPopup(context, false)
            sheet.show(
                (context as AppCompatActivity).supportFragmentManager,
                "ParentalControlPopup"
            )
        }
    }


    fun checkUserCensorRating(
        context: Context,
        rating: String,
        onUserCensorRatingChange: UserCensorRatingPopup.OnUserCensorRatingChange? = null,
        showPopup: Boolean = true
    ): Boolean {
        val userRating = SharedPrefHelper.getInstance().getUserCensorRating()
        val userSettingRespModel =
            SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_GENERAL_SETTING)
        var is13PlusVideoNotAllow = false
        if (userSettingRespModel?.data != null) {
            is13PlusVideoNotAllow =
                userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitVideoContent!!
        }
        if (is13PlusVideoNotAllow && (rating.contains("13+", true) || rating.contains(
                "18+",
                true
            ) || rating.contains("16+", true))
        ) {
            open13PlusVideoSettingPopup(context, showPopup)
            return true
        } else if (rating.contains("18+", true) && userRating != null && userRating < 18) {
            openUserCensorRatingPopup(context, onUserCensorRatingChange, showPopup)
            return true
        } else {
            return false
        }
    }

    fun openUserCensorRatingPopup(
        context: Context,
        onUserCensorRatingChange: UserCensorRatingPopup.OnUserCensorRatingChange? = null,
        showPopup: Boolean
    ) {
        if (showPopup) {
            //Toast.makeText(context, "You must be 18+ to view this video. Please verify your age.", Toast.LENGTH_LONG).show()
            val sheet = UserCensorRatingPopup(context, onUserCensorRatingChange)
            sheet.show(
                (context as AppCompatActivity).supportFragmentManager,
                "UserCensorRatingPopup"
            )
        }
    }

    fun addFragment(
        context: Context,
        fragmentContainerResourceId: Int,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        commitAllowingStateLoss: Boolean
    ): Boolean {
        try {
            if (currentFragment == null || nextFragment == null) {
                return false
            }
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

//            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            fragmentTransaction.add(
                fragmentContainerResourceId, nextFragment, nextFragment.javaClass.simpleName
            )
            fragmentTransaction.addToBackStack(nextFragment.javaClass.simpleName)
            val parentFragment = currentFragment.parentFragment
            fragmentTransaction.hide(parentFragment ?: currentFragment)

            if (!commitAllowingStateLoss) {
                fragmentTransaction.commit()
            } else {
                fragmentTransaction.commitAllowingStateLoss()
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun Context.isPackageInstalled(packageName: String): Boolean {
        // check if chrome is installed or not
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun bindSystemEqualizer(context: Context, audioSessionId: Int) {
        setLog("Equalizer", "SessionId4-$audioSessionId")
        if (audioSessionId != Constant.NO_AUDIO_SESSION_ID) {
            val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            context.sendBroadcast(equalizerIntent)
        }
    }

    fun unbindSystemEqualizer(context: Context, audioSessionId: Int) {
        setLog("Equalizer", "SessionId5-$audioSessionId")
        if (audioSessionId != Constant.NO_AUDIO_SESSION_ID) {
            val equalizerIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            context.sendBroadcast(equalizerIntent)
        }
    }

    fun generateAppsFlyerInviteLink(
        context: Context,
        linkGeneratorListener: CreateOneLinkHttpTask.ResponseListener,
        channel: String,
        amount: String,
        currency: String,
        domain: String
    ) {

        setLog("inviteLink", "generateAppsFlyerInviteLink-")

        AppsFlyerLib.getInstance().setAppInviteOneLink("4LX1")
        val linkGenerator = ShareInviteHelper.generateInviteUrl(context)
        linkGenerator.channel = channel
//        linkGenerator.addParameter("af_cost_value",amount)
//        linkGenerator.addParameter("af_cost_currency",currency)
        // optional - set a brand domain to the user invite link
        linkGenerator.brandDomain = domain
        linkGenerator.setReferrerCustomerId(SharedPrefHelper.getInstance().getUserId())
        linkGenerator.setReferrerName(
            SharedPrefHelper.getInstance().getUserFirstname() + " " + SharedPrefHelper.getInstance()
                .getUserLastname()
        )

        linkGenerator.generateLink(context, linkGeneratorListener)
    }

    fun shareLink(context: Context, mURL: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, mURL)
            // (Optional) Here we're setting the title of the content
            //putExtra(Intent.EXTRA_TITLE, context?.getString(R.string.music_player_str_18))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            type = "text/plain"
        }

        var pi: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pi = PendingIntent.getBroadcast(
                context,
                1099,
                Intent(context, ShareIntentReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            pi = PendingIntent.getBroadcast(
                context,
                1099,
                Intent(context, ShareIntentReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        setLog("TAG", "shareItem: mURL:$mURL")
        val shareIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent.createChooser(
                sendIntent, context.getString(R.string.music_player_str_19), pi.intentSender
            )
        } else {
            Intent.createChooser(
                sendIntent, context.getString(R.string.music_player_str_19)
            )
        }
        context.startActivity(shareIntent)
    }

    class ShareIntentReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clickedComponent: ComponentName? =
                intent?.getParcelableExtra(EXTRA_CHOSEN_COMPONENT)

            setLog("ShareIntentReceiver", "Component name: $clickedComponent")
            setLog("ShareIntentReceiver", "Component name: ${clickedComponent?.className}")
            setLog("ShareIntentReceiver", "Component name: ${clickedComponent?.packageName}")
            setLog("ShareIntentReceiver", "Component name: ${clickedComponent?.shortClassName}")
            val channel = context?.let {
                clickedComponent?.packageName?.let { it1 ->
                    getAppNameFromPkgName(
                        it, it1
                    )
                }
            }
            setLog("ShareIntentReceiver", "App name: $channel")
            //ShareInviteHelper.logInvite(context, channel, null);
        }
    }

    fun getAppNameFromPkgName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(info) as String
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun displayPopup(context: Context, eventModel: GCEventModel) {

        setLog(TAG, "onPointsAdded: displayPopup eventModel:${eventModel}")
        //then we will inflate the custom alert dialog xml that we created
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.gamification_popup_color, null, false)
        //Now we need an AlertDialog.Builder object
        val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.StyleCommonDialog)
        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView)

        //finally creating the alert dialog and displaying it
        var alertDialog: AlertDialog = builder.create()
        val share = dialogView.findViewById<View>(R.id.view1)
        val ivCancel = dialogView.findViewById<ImageView>(R.id.ivCancel)
        val tvCoin = dialogView.findViewById<TextView>(R.id.tvCoin)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
        val clView = dialogView.findViewById<ConstraintLayout>(R.id.clView)
        val msg =
            eventModel.popupText.toString().replace("@coin_amount", "" + eventModel.addedCoin)
        tvDescription.text = "" + msg
        tvCoin.text = "" + eventModel.addedCoin

        if (eventModel.popupType.equals("popup_random")) {
            val randomInt = Random.nextInt(1, 7)
            if (randomInt == 1) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_1)
            } else if (randomInt == 2) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_2)
            } else if (randomInt == 3) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_3)
            } else if (randomInt == 4) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_4)
            } else if (randomInt == 5) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_5)
            } else if (randomInt == 6) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_6)
            } else if (randomInt == 7) {
                clView?.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_7)
            }
        } else if (eventModel.popupType.equals("popup_1")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_1)
        } else if (eventModel.popupType.equals("popup_2")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_2)
        } else if (eventModel.popupType.equals("popup_3")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_3)
        } else if (eventModel.popupType.equals("popup_4")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_4)
        } else if (eventModel.popupType.equals("popup_5")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_5)
        } else if (eventModel.popupType.equals("popup_6")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_6)
        } else if (eventModel.popupType.equals("popup_7")) {
            clView?.background =
                ContextCompat.getDrawable(context, R.drawable.bg_gamification_popup_7)
        }

        share.setOnClickListener(View.OnClickListener {
            alertDialog.dismiss()
            shareLink(context, "https://www.hungama.com/redeem")
        })

        ivCancel.setOnClickListener(View.OnClickListener {
            alertDialog.dismiss()
        })

        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()


    }

    fun showToast(context: Context, messageModel: MessageModel) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                var time = Toast.LENGTH_LONG
                if (!messageModel.isDisplayForLongTime) {
                    time = Toast.LENGTH_SHORT
                }
                if (!TextUtils.isEmpty(messageModel.message)) {
                    val layout: View?
                    if (messageModel.messageType == MessageType.NEGATIVE) {
                        setLog("showToast", "MessageType.NEGATIVE")
                        layout = (context as AppCompatActivity).layoutInflater.inflate(
                            R.layout.toast_red, context.findViewById(R.id.toast_container)
                        )
                        val view1 = layout.findViewById<ImageView>(R.id.view1)
                        val colors = intArrayOf(
                            Color.parseColor("#FFE21E3E"), Color.parseColor("#FFF72C3A")
                        )
                        val position = floatArrayOf(
                            0f, 1f
                        )
                        val startX = 84.564f
                        val startY = -2.2727037E-16f
                        val endX = 317.844f
                        val endY = 30f
                        //val cornerRadius = 100f
                        val cornerRadius = floatArrayOf(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//Top left
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//Top left
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//top Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//top Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//Bottom Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//Bottom Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat(),//bottom left
                            context.resources.getDimensionPixelSize(R.dimen.dimen_100)
                                .toFloat()//bottom left
                        )
                        applyAppButtonGradient(
                            startX,
                            startY,
                            endX,
                            endY,
                            cornerRadius,
                            colors,
                            position,
                            context,
                            view1
                        )
                        view1.alpha = 1f
                        view1?.requestLayout()
                    } else if (messageModel.messageType == MessageType.GAMIFICATION) {
                        setLog("showToast", "MessageType.GAMIFICATION")
                        layout = (context as AppCompatActivity).layoutInflater.inflate(
                            R.layout.toast_gamification, context.findViewById(R.id.toast_container)
                        )
                        val view1 = layout.findViewById<ImageView>(R.id.view1)
                        val icon = layout.findViewById<ImageView>(R.id.ivToastCoin)
                        if (isUserHasGoldSubscription()) {
                            icon?.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context, R.drawable.bg_coin_profile_black
                                )
                            )
                            setAppButton2(context, view1)
                        } else {
                            icon?.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context, R.drawable.bg_coin_profile
                                )
                            )
                            setAppButton1(context, view1)
                        }

                        view1.alpha = 1f
                        view1?.requestLayout()
                    } else {
                        setLog("showToast", "MessageType.Other")

                        if (context is AppCompatActivity) {
                            layout = context.layoutInflater.inflate(
                                R.layout.toast_green, context.findViewById(R.id.toast_container)
                            )
                        } else {
                            val inflater =
                                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            layout = inflater.inflate(R.layout.toast_green, null)
                            //val view = layout.findViewById(R.id.toast_container) as ConstraintLayout

                        }
                        val view1 = layout.findViewById<ImageView>(R.id.view1)
                        /*val colors = intArrayOf(Color.parseColor("#FF55A628"),
                            Color.parseColor("#FFABE64A"))
                        val position = floatArrayOf(
                            0f,
                            1f
                        )
                        val startX = 84.564f
                        val startY = 63.12f
                        val endX = 268.92f
                        val endY = 3.12f
                        //val cornerRadius = 100f
                        val cornerRadius = floatArrayOf(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//bottom left
                            context.resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()//bottom left
                        )
                        applyAppButtonGradient(startX, startY, endX, endY, cornerRadius, colors, position, context, view1)*/
                        if (isUserHasGoldSubscription()) {
                            setAppButton2(context, view1)
                        } else {
                            setAppButton1(context, view1)
                        }

                        view1.alpha = 1f
                        view1?.requestLayout()
                    }

                    val tvMessage = layout.findViewById<TextView>(R.id.toastMessage)
                    tvMessage.text = messageModel.message
                    var mBottomMargin = context.resources.getDimensionPixelSize(R.dimen.dimen_65)
                    /*if (context is MainActivity){
                        val miniplayerHeight = 0
                        val bottomNavigationHeight = (context as MainActivity).bottomNavigationHeight
                        mBottomMargin = miniplayerHeight + bottomNavigationHeight
                        setLog("showToastBottomSpacing", "miniplayerHeight: $miniplayerHeight")
                        setLog("showToastBottomSpacing", "bottomNavigationHeight: $bottomNavigationHeight")
                        if (isHomeScreenBannerAds() && (context as MainActivity).isBottomNavigationVisible){
                            //mBottomMargin += context.resources.getDimensionPixelSize(R.dimen.dimen_70)
                            setLog("setPageBottomSpacing", "stickyAdHeight: 70")
                        }
                        val extraMargin = context.resources.getDimensionPixelSize(R.dimen.dimen_5)
                        mBottomMargin += extraMargin
                    }*/
                    setLog("showToastBottomSpacing", "totalBottomSpace: $mBottomMargin")

                    val myToast = Toast(context)

                    if (!messageModel.message.contains(context.getString(R.string.toast_message_5))) {
                        myToast.apply {
                            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, mBottomMargin)
                            duration = time
                            view = layout
                            show()
                        }
                    }
                }
            } catch (e: Exception) {
                setLog("showToastBottomSpacing", "Error:- " + e.message.toString())
            }
        }
    }

    @SuppressLint("NewApi")
    fun hapticVibration(context: Context, v: View, effect: Int, isAnimation: Boolean = true) {

        try {
            if (isAnimation) {
                setClickAnimation(context, v)
            }
            val hapticSond = MediaPlayer.create(context, R.raw.selection)
//        val  vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        var vibrationEffect : VibrationEffect
//            vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
//            hapticSond.start()
//            vibrator.cancel()
//            vibrator.vibrate(vibrationEffect)

            hapticSond.start()
            v.performHapticFeedback(effect, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        } catch (e: Exception) {

        }
    }

    fun setClickAnimation(context: Context, v: View?) {
        try {
            if (v != null) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.bounce)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        animation?.cancel()
                        animation?.reset()
                        v.animation = null
                    }

                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                })
                v.show()
                v.startAnimation(animation)
            }
        } catch (e: Exception) {

        }
    }

    fun getBitmapFromView(view: View): Bitmap? {
        try {
            //Define a bitmap with the same size as the view
            val returnedBitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            //Bind a canvas to it
            val canvas = Canvas(returnedBitmap)
            //Get the view's background
            val bgDrawable = view.background
            if (bgDrawable != null) {
                //has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas)
            } else {
                //does not have background drawable, then draw white background on the canvas
                canvas.drawColor(Color.TRANSPARENT)
            }
            // draw the view on the canvas
            view.draw(canvas)
            //return the bitmap
            return returnedBitmap
        } catch (exp: Exception) {
            exp.printStackTrace()
            return null
        }

    }

    enum class STORY_SHARE {
        FACEBOOK, INSTAGRAM,
    }

    enum class STORY_TYPE {
        PHOTO, VIDEO,
    }

    fun AppCompatTextView.setTextOrHide(value: String? = null) {
        if (!value.isNullOrBlank()) {
            text = value
            isVisible = true
        } else {
            isVisible = false
        }
    }

    fun getMusicPageArrayList(): ArrayList<String> {
        var arrayList = ArrayList<String>()
        arrayList.add("charts")
        arrayList.add("Podcast")
        arrayList.add("Podcasts")
        arrayList.add("Radio")
        arrayList.add("Music Videos")
        arrayList.add("music-videos")
        arrayList.add("Love")
        arrayList.add("Party")
        arrayList.add("Bhakti")
        return arrayList
    }

    fun getVideoPageArrayList(): ArrayList<String> {
        var arrayList = ArrayList<String>()
        arrayList.add("movies")
        arrayList.add("tv-shows")
        arrayList.add("originals")
        arrayList.add("Rent")
        arrayList.add("Shows")
        arrayList.add("Cineplex")
        arrayList.add("Kids")
        arrayList.add("Quicks")
        arrayList.add("videos")
        return arrayList
    }

    fun getDeeplinkCategoryList(): DeeplinkCategoryModel = runBlocking(Dispatchers.IO) {
        var deeplinkCategoryModel = DeeplinkCategoryModel()
        try {
            val remoteConfig = Firebase.remoteConfig
            val splashAd = remoteConfig.getString("deeplink_category_list")

            val fConfigJsonObject = JsonObject()
            fConfigJsonObject.add("deeplink_category_list", convertStringToJsonObject(splashAd))
            //setLog("deepLinkUrl", "getDeeplinkCategoryList--deeplinkCategoryList=$fConfigJsonObject")
            if (!TextUtils.isEmpty(fConfigJsonObject.toString())) {
                deeplinkCategoryModel = Gson().fromJson<DeeplinkCategoryModel>(
                    fConfigJsonObject, DeeplinkCategoryModel::class.java
                ) as DeeplinkCategoryModel
            }
        } catch (e: Exception) {
            setLog("FConfigObject", e.toString())
        }

        deeplinkCategoryModel
    }

    fun checkDeeplinkCategory(category: String): String {
        val deeplinkCategoryList = getDeeplinkCategoryList()
        //setLog("deepLinkUrl", "checkDeeplinkCategory--categoryName=$category && categoryPageName=$deeplinkCategoryList")
        if (deeplinkCategoryList.deeplinkCategoryList.music.toString().lowercase()
                .contains(category)
        ) {
            return "music"
        }
        if (deeplinkCategoryList.deeplinkCategoryList.podcasts.toString().lowercase()
                .contains(category)
        ) {
            return "podcast"
        }
        if (deeplinkCategoryList.deeplinkCategoryList.movies.toString().lowercase()
                .contains(category)
        ) {
            return "movies"
        }
        if (deeplinkCategoryList.deeplinkCategoryList.musicVideos.toString().lowercase()
                .contains(category)
        ) {
            return "musicvideos"
        }
        if (deeplinkCategoryList.deeplinkCategoryList.videos.toString().lowercase()
                .contains(category)
        ) {
            return "videos"
        }
        return ""
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlayerSongList(
        contentId: String,
        contentTitle: String,
        contentSubTitle: String,
        contentUrl: String,
        drmLicence: String,
        songLyricsUrl: String,
        playerType: Int,
        contentImage: String,
        heading: String,
        pId: String,
        pName: String,
        pSubName: String,
        pImage: String,
        pType: Int,
        contentType: Int,
        explicit: Int,
        restrictedDownload: Int,
        attributeCensorRating: String,
        movierights: String
    ): Track = runBlocking(Dispatchers.IO) {
        val track = Track()
        if (!TextUtils.isEmpty(contentId)) {
            track.id = contentId.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(contentTitle)) {
            track.title = contentTitle
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(contentSubTitle)) {
            track.subTitle = contentSubTitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(contentUrl)) {
            track.url = contentUrl
        } else {
            track.url = ""
        }

        if (!TextUtils.isEmpty(drmLicence)) {
            track.drmlicence = drmLicence
        } else {
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(songLyricsUrl)) {
            track.songLyricsUrl = songLyricsUrl
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playerType.toString())) {
            track.playerType = playerType.toString()
        } else {
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(heading)) {
            track.heading = heading
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(contentImage)) {
            track.image = contentImage
        } else {
            track.image = ""
        }

        if (!TextUtils.isEmpty(pId)) {
            track.parentId = pId
        }
        if (!TextUtils.isEmpty(pName)) {
            track.pName = pName
        }

        if (!TextUtils.isEmpty(pSubName)) {
            track.pSubName = pSubName
        }

        if (!TextUtils.isEmpty(pImage)) {
            track.pImage = pImage
        }

        if (!movierights.isNullOrEmpty())
            track.movierights = movierights

        track.pType = pType
        track.contentType = contentType
        track.explicit = explicit
        track.restrictedDownload = restrictedDownload
        track.attributeCensorRating = attributeCensorRating
        track
    }

    fun genratePaymentPageURL(context: Context, queryParam: String): String =
        runBlocking(Dispatchers.IO) {
            var plan_type = ""
            var url = ""

            plan_type = "subscription"

            if (queryParam.contains("plan_id")) {
                url = "https://payments.hungama.com/payment?"
                val url1 = queryParam
                val split = url1.split("&").toTypedArray()
                if (split.size >= 9) {
                    url = url + split[0] + "&"
                    plan_type = split[9].replace("plan_type=", "")
                } else {
                    if (queryParam.contains("37"))
                        plan_type = "event"
                }
            } else {
                url = "https://payments.hungama.com/plan?"
            }

            val auth = md5(Constant.PRODUCT_KEY + ":" + SharedPrefHelper.getInstance().getUserId())
            url += "auth=$auth"

            val identity = SharedPrefHelper.getInstance().getUserId()
            url += "&identity=$identity"

            val product_id = Constant.PRODUCT_ID
            url += "&product_id=$product_id"


            url += "&country=" + Constant.DEFAULT_COUNTRY_CODE

            val platform_id = Constant.PLATFORM_ID
            url += "&platform_id=$platform_id"

            url += "&plan_type=$plan_type"

            val app_version = BuildConfig.VERSION_NAME
            url += "&app_version=$app_version"

            val build_number = BuildConfig.VERSION_CODE
            url += "&build_number=$build_number"

            val packageManager = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_DEFAULT)
            mainIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            mainIntent.action = Intent.ACTION_VIEW
            val uri1: Uri = Uri.Builder().scheme("upi").authority("pay").build()
            mainIntent.data = uri1
            val pkgAppsList: List<*> = context.packageManager.queryIntentActivities(mainIntent, 0)
            val upilist: ArrayList<String> = ArrayList()
            for (i in pkgAppsList.indices) {
                val resolveInfo: ResolveInfo = pkgAppsList[i] as ResolveInfo
                setLog("TAG", "packageName: " + resolveInfo.activityInfo.packageName)
                setLog("TAG", "AppName: " + resolveInfo.loadLabel(packageManager))
                setLog("TAG", "AppIcon: " + resolveInfo.loadIcon(packageManager))
                upilist.add(resolveInfo.loadLabel(packageManager).toString())
            }


            url += "&upilist=${Utils.convertArrayToString(upilist)}"

            val hardware_id = getDeviceId(context!!)
            url += "&hardware_id=$hardware_id"

            val content_id = ""
            url += "&content_id=$content_id"


            if (!TextUtils.isEmpty(queryParam)) {
                CommonUtils.setLog("queryParam", "url:${url} queryParam:${queryParam}")
                url = url + "&" + queryParam
            }

            if (!url.contains("live_event_id", true)) {
                val live_event_id = ""
                url += "&live_event_id=$live_event_id"
            }

            if (!url.contains("aff_code", true)) {
                val aff_code = ""
                url += "&aff_code=$aff_code"
            }

            if (!url.contains("extra_data", true)) {
                val extra_data = ""
                url += "&extra_data=$extra_data"
            }

            if (!url.contains("utm_source", true)) {
                val utm_source = ""
                url += "&utm_source=$utm_source"
            }

            if (!url.contains("utm_medium", true)) {
                val utm_medium = ""
                url += "&utm_medium=$utm_medium"
            }

            if (!url.contains("utm_campaign", true)) {
                val utm_campaign = ""
                url += "&utm_campaign=$utm_campaign"
            }


            val lang = SharedPrefHelper.getInstance().getLanguage()
            url += "&lang=$lang"

            val amplitude_user_id = Amplitude.getInstance().userId
            url += "&amp_user_id=$amplitude_user_id"

            val amplitude_device_id = Amplitude.getInstance().deviceId
            url += "&amp_device_id=$amplitude_device_id"

            url
        }

    @OptIn(UnstableApi::class)
    fun createMediaItems(
        intent: Intent,
        downloadTracker: DownloadTracker?,
        trackList: ArrayList<Track>?
    ): List<MediaItem> = runBlocking(Dispatchers.IO) {
        val mediaItems: MutableList<MediaItem> = java.util.ArrayList()
        try {
            val mediaIntentItems: List<MediaItem> =
                IntentUtil.createMediaItemsFromIntent(intent, trackList)
            for (item in mediaIntentItems) {
                val downloadRequest =
                    downloadTracker?.getDownloadRequest(Assertions.checkNotNull(item.playbackProperties).uri)
                setLog(
                    "DWContentTitle",
                    "contentTitle-createMediaItems-${item.mediaMetadata.title}"
                )
                if (downloadRequest != null) {
                    val builder = item.buildUpon()
                    builder
                        .setMediaId(downloadRequest.id)
                        .setUri(downloadRequest.uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder().setTitle(item.mediaMetadata.title).build()
                        )
                        .setCustomCacheKey(downloadRequest.customCacheKey)
                        .setMimeType(downloadRequest.mimeType)
                        .setStreamKeys(downloadRequest.streamKeys)
                        .setDrmKeySetId(downloadRequest.keySetId)
                        .setDrmLicenseRequestHeaders(getDrmRequestHeaders(item))
                    mediaItems.add(builder.build())
                } else {
                    mediaItems.add(item)
                }
            }
        } catch (e: Exception) {

        }
        mediaItems
    }

    @OptIn(UnstableApi::class)
    private fun getDrmRequestHeaders(item: MediaItem): Map<String, String>? {
        val drmConfiguration = item.playbackProperties!!.drmConfiguration
        return drmConfiguration?.requestHeaders
    }

    /**
     * @param length remove index [0..length)
     */
    fun <E> MutableList<E>.removeItemsFromFirst(length: Int): MutableList<E> {
        if (length in 1..size) {
            subList(0, length).clear()
        }
        return this
    }

    /**
     * @param length remove index [(size - length)..size)
     */
    fun <E> MutableList<E>.removeItemsFromLast(length: Int): MutableList<E> {
        if (length in 1..size) {
            subList(size - length, size).clear()
        }
        return this
    }

    /**
     * Contains last clicked time
     */
    private var lastClickedTime: Long = 0
    fun preventDoubleClick(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Constant.MAX_CLICK_INTERVAL) {
            lastClickedTime = SystemClock.elapsedRealtime()
            return false
        }
        lastClickedTime = SystemClock.elapsedRealtime()
        return true
    }


    fun getLoginPlateformSequence(): ArrayList<LoginPlatformSequenceModel> {
        val remoteConfig = Firebase.remoteConfig
        val user_login = remoteConfig.getString("user_login")
        setLog("loginPlatformSequenceList", "user_login-$user_login")
        val firebaseLoginSequence = getStringToArray(user_login)
        setLog("loginPlatformSequenceList", "firebaseLoginSequence-$firebaseLoginSequence")
        val loginPlatformSequenceList: ArrayList<LoginPlatformSequenceModel> = ArrayList()
        for (item in firebaseLoginSequence.listIterator()) {
            val loginPlatformSequenceModel = LoginPlatformSequenceModel()
            if (item.equals("email")) {
                loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_EMAIL
                loginPlatformSequenceModel.name = ""
                loginPlatformSequenceModel.image = R.drawable.ic_email
                loginPlatformSequenceList.add(loginPlatformSequenceModel)
            } else if (item.equals("google")) {
                loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_GOOGLE
                loginPlatformSequenceModel.name = ""
                loginPlatformSequenceModel.image = R.drawable.ic_google
                loginPlatformSequenceList.add(loginPlatformSequenceModel)
            } else if (item.equals("facebook")) {
                loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_FACEBOOK
                loginPlatformSequenceModel.name = ""
                loginPlatformSequenceModel.image = R.drawable.ic_facebook
                loginPlatformSequenceList.add(loginPlatformSequenceModel)
            } else if (item.equals("apple")) {
                loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_APPLE
                loginPlatformSequenceModel.name = ""
                loginPlatformSequenceModel.image = R.drawable.ic_apple
                loginPlatformSequenceList.add(loginPlatformSequenceModel)
            }
        }
        setLog("loginPlatformSequenceList", "loginPlatformSequenceList-$loginPlatformSequenceList")
        if (loginPlatformSequenceList.isEmpty()) {
            var loginPlatformSequenceModel = LoginPlatformSequenceModel()
            loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_EMAIL
            loginPlatformSequenceModel.name = ""
            loginPlatformSequenceModel.image = R.drawable.ic_email
            loginPlatformSequenceList.add(loginPlatformSequenceModel)

            loginPlatformSequenceModel = LoginPlatformSequenceModel()
            loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_GOOGLE
            loginPlatformSequenceModel.name = ""
            loginPlatformSequenceModel.image = R.drawable.ic_google
            loginPlatformSequenceList.add(loginPlatformSequenceModel)

            loginPlatformSequenceModel = LoginPlatformSequenceModel()
            loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_FACEBOOK
            loginPlatformSequenceModel.name = ""
            loginPlatformSequenceModel.image = R.drawable.ic_facebook
            loginPlatformSequenceList.add(loginPlatformSequenceModel)

            loginPlatformSequenceModel = LoginPlatformSequenceModel()
            loginPlatformSequenceModel.id = Constant.SIGNIN_WITH_APPLE
            loginPlatformSequenceModel.name = ""
            loginPlatformSequenceModel.image = R.drawable.ic_apple
            loginPlatformSequenceList.add(loginPlatformSequenceModel)
        }
        setLog(
            "loginPlatformSequenceList",
            "loginPlatformSequenceList-2-$loginPlatformSequenceList"
        )
        return loginPlatformSequenceList
    }

    fun checkProUserBucket(model: HomeModel?): HomeModel = runBlocking(Dispatchers.IO) {
        /**
         * start Logic for pro user
         */
        if (model?.data?.body?.rows != null && model?.data?.body?.rows?.size!! > 0) {
            var isGlodUser = isUserHasGoldSubscription()
            var isFreeUser = !isGlodUser
            val updatedArrylist: ArrayList<RowsItem?>? = ArrayList()
            setLog(
                TAG,
                "checkProUserBucket before: size:${model?.data?.body?.rows?.size} isGlodUser:${isGlodUser} isFreeUser:${isFreeUser}"
            )
            model?.data?.body?.rows?.forEach {
                if (it?.pro_user?.equals("2")!!) {
                    updatedArrylist?.add(it)
                } else if (it.pro_user.equals("1") && isGlodUser) {
                    updatedArrylist?.add(it)
                } else if (it.pro_user.equals("0") && isFreeUser) {
                    updatedArrylist?.add(it)
                } else {
                    updatedArrylist?.add(it)
                }
            }
            /*            if (isGlodUser) {
                for (i in updatedArrylist?.get(0)?.items?.indices!!) {
                    if (updatedArrylist!![0]?.items?.get(i)!!.data!!.isBrandHub) {
                        updatedArrylist!![0]?.items!!.removeAt(i)
                    }
                }
            }*/

            model?.data?.body?.rows = updatedArrylist
            setLog(
                TAG,
                "checkProUserBucket: after size:${model?.data?.body?.rows?.size} isGlodUser:${isGlodUser} isFreeUser:${isFreeUser}"
            )


        }


        /**
         * end Logic for pro user
         */
        model!!
    }

    fun covertNumberToCurrencyFormat(value: String): String {
        if (!TextUtils.isEmpty(value)) {
            val myFormatter = DecimalFormat("##,##,###")
            val output = myFormatter.format(value.toLong())
            //println(output)
            setLog("covertNumberToCurrencyFormat()", "output-$output")
            return output
        }
        return value
    }

    fun isSongOrPodcastContent(track: Track): Boolean {
        if (track.playerType.equals(Constant.PLAYER_RADIO, true)
            || track.playerType.equals(Constant.PLAYER_LIVE_RADIO, true)
            || track.playerType.equals(Constant.PLAYER_MOOD_RADIO, true)
            || track.playerType.equals(Constant.PLAYER_ON_DEMAND_RADIO, true)
            || track.playerType.equals(Constant.PLAYER_ARTIST_RADIO, true)

            || track.playerType.equals(Constant.VIDEO_MOVIE, true)
            || track.playerType.equals(Constant.VIDEO_MUSIC_VIDEO_TRACK, true)
            || track.playerType.equals(Constant.VIDEO_EVENTS_BROADCAST_VIDEO, true)
            || track.playerType.equals(Constant.VIDEO_HD_MOVIE, true)
            || track.playerType.equals(Constant.VIDEO_SD_MOVIE, true)
            || track.playerType.equals(Constant.VIDEO_SHORT_FILMS, true)

            || track.playerType.equals(Constant.TV_SERIES, true)
            || track.playerType.equals(Constant.TV_SERIES_SEASON, true)
            || track.playerType.equals(Constant.TV_SERIES_EPISODE, true)
            || track.playerType.equals(Constant.TV_FASHION_TV, true)
            || track.playerType.equals(Constant.TV_LINEAR_TV_CHANNEL_ALBUM, true)
            || track.playerType.equals(Constant.TV_LINEAR_TV_CHANNEL, true)
        ) {
            return false
        }
        return true
    }

    fun filterAudioContent(
        context: Context?,
        songDataList: ArrayList<Track>,
        selectedTrackPosition: Int
    ): TracklistDataModel = runBlocking(Dispatchers.Default) {
        setLog(
            "filterAudioContent",
            "CommonUtils-filterAudioContent-songDataList-${songDataList.size}"
        )
        try {
            val finalTrackList = mutableListOf<Track>()
            var selectedTrackIndex = selectedTrackPosition
            var isSongOrPodcastContent = true
            if (!songDataList.isNullOrEmpty() && songDataList.size > selectedTrackIndex) {
                isSongOrPodcastContent =
                    CommonUtils.isSongOrPodcastContent(songDataList.get(selectedTrackIndex))
            }
            songDataList.forEachIndexed { index, track ->
                if ((isSongOrPodcastContent && selectedTrackIndex != index && !CommonUtils.isSongOrPodcastContent(
                        track
                    )) || CommonUtils.checkExplicitContent(
                        context,
                        track.explicit,
                        false
                    ) || (track.id == 0L && (track.contentType != ContentTypes.Audio_Ad.value || !CommonUtils.isAdsEnable() || (track.contentType == ContentTypes.Audio_Ad.value && selectedTrackIndex > index)))
                ) {
                    setLog(
                        "filterAudioContent",
                        "CommonUtils-Removed content: " + track.id + "-" + track.title
                    )
                    if (selectedTrackIndex > index) {
                        selectedTrackIndex -= 1
                    }
                } else {
                    finalTrackList.add(track)
                }
            }
            setLog(
                "filterAudioContent",
                "CommonUtils-filterAudioContent-finalTrackList-${finalTrackList.size}"
            )
            TracklistDataModel(finalTrackList, selectedTrackIndex)
        } catch (e: Exception) {
            setLog(
                "filterAudioContent",
                "CommonUtils-filterAudioContent-finalTrackList-Exception-${songDataList.size}"
            )
            setLog(
                "filterAudioContent",
                "CommonUtils-filterAudioContent-finalTrackList-Exception-${e.message}"
            )
            TracklistDataModel(songDataList, selectedTrackPosition)
        }
    }

    fun isMovieContent(type: String?): Boolean {
        if (!TextUtils.isEmpty(type)) {
            try {
                val typeId = type?.toInt()
                if (typeId == 4 || typeId == 65 || typeId == 66 || typeId == 93) {
                    return true
                }
            } catch (e: Exception) {

            }
        }
        return false
    }

    fun isTVShowContent(type: String?): Boolean {
        if (!TextUtils.isEmpty(type)) {
            try {
                val typeId = type?.toInt()
                if (typeId == 96 || typeId == 97 || typeId == 98 || typeId == 102 ||
                    typeId == 107 || typeId == 108
                ) {
                    return true
                }
            } catch (e: Exception) {

            }
        }

        return false
    }

    fun isMusicVideoContent(type: String?): Boolean {
        if (!TextUtils.isEmpty(type)) {
            try {
                val typeId = type?.toInt()
                if (typeId == 22 || typeId == 53 || typeId == 88888) {
                    return true
                }
            } catch (e: Exception) {

            }
        }
        return false
    }

    fun isRadioContent(type: String?): Boolean {
        if (!TextUtils.isEmpty(type)) {
            try {
                val typeId = type?.toInt()
                if (typeId == 15 || typeId == 33 || typeId == 34 || typeId == 35 || typeId == 36 || typeId == 77777) {
                    return true
                }
            } catch (e: Exception) {

            }
        }

        return false
    }

    fun isArtistContent(type: String?): Boolean {
        if (!TextUtils.isEmpty(type)) {
            try {
                val typeId = type?.toInt()
                if (typeId == 0) {
                    return true
                }
            } catch (e: Exception) {

            }
        }

        return false
    }

    fun getFormattedDateWithSuffixedDay(date: Date?): String {
        if (date != null) {
            val cal = Calendar.getInstance()
            cal.time = date
            //2nd of march 2015
            val day = cal[Calendar.DATE]
            return if (!(day > 10 && day < 19)) when (day % 10) {
                1 -> SimpleDateFormat("d'st' MMMM yyyy").format(date)
                2 -> SimpleDateFormat("d'nd' MMMM yyyy").format(date)
                3 -> SimpleDateFormat("d'rd' MMMM yyyy").format(date)
                else -> SimpleDateFormat("d'th' MMMM yyyy").format(date)
            } else SimpleDateFormat("d'th' MMMM yyyy").format(date)
        }
        return ""
    }

    fun downloadIconStates(
        context: Context, status: Int, ivPlayerAudioDownload: ImageView?, textSize: Float
    ) {
        try {
            if (ivPlayerAudioDownload != null) {
                var icon = R.string.icon_download
                when (status) {
                    Status.NONE.value -> {
                        icon = R.string.icon_download
                    }

                    Status.QUEUED.value -> {
                        icon = R.string.icon_download_queue
                    }

                    Status.PAUSED.value -> {
                        icon = R.string.icon_download_pause
                    }

                    Status.DOWNLOADING.value -> {
                        icon = R.string.icon_downloading
                    }

                    Status.COMPLETED.value -> {
                        icon = R.string.icon_downloaded2
                    }
                }
                ivPlayerAudioDownload.setImageDrawable(
                    context.faDrawable(
                        icon, R.color.colorWhite, textSize
                    )
                )
            }
        } catch (e: Exception) {

        }
    }

    suspend fun getSortedUserStoryByRead(
        storyList: ArrayList<BodyDataItem>, oldListData: ArrayList<BodyRowsItemsItem?>?
    ): ArrayList<BodyDataItem> = runBlocking(Dispatchers.IO) {
        //setLog("TestUserStory", "Sort-2")
        val tempAllReadStoryUsersList = ArrayList<BodyDataItem>()
        val tempAllRemainingStoryUsersList = ArrayList<BodyDataItem>()
        if (!storyList.isNullOrEmpty()) {
            for (item in storyList) {
                if (item.misc?.post?.count == item.misc?.post?.readCount) {
                    tempAllReadStoryUsersList.add(item)
                } else {
                    tempAllRemainingStoryUsersList.add(item)
                }
            }
            val sortedList =
                tempAllReadStoryUsersList.sortedWith(compareBy { it.misc?.post?.timestamp })
            if (!tempAllReadStoryUsersList.isNullOrEmpty()) {
                tempAllRemainingStoryUsersList.addAll(sortedList)
            }
        }

        if (!oldListData.isNullOrEmpty()) {
            oldListData.forEachIndexed { index, bodyRowsItemsItem ->
                if (bodyRowsItemsItem?.data != null && bodyRowsItemsItem.data?.isBrandHub!!) {
                    if (tempAllRemainingStoryUsersList.size > index) {
                        tempAllRemainingStoryUsersList.add(index, bodyRowsItemsItem.data!!)
                    }
                }
            }
        }
        setLog("TestUserStory", "Sort-2.1")
        tempAllRemainingStoryUsersList
    }

    fun isVideoAutoPlayEnable(): Boolean {
        var isEnable = true
        try {
            val userSettingVideoModel = SharedPrefHelper.getInstance()
                .getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
            if (userSettingVideoModel != null && userSettingVideoModel.data != null && !userSettingVideoModel.data?.data.isNullOrEmpty() && !userSettingVideoModel.data?.data?.get(
                    0
                )?.preference.isNullOrEmpty() && userSettingVideoModel.data?.data?.get(0)?.preference?.get(
                    0
                )?.autoPlay != null
            ) {
                isEnable = userSettingVideoModel.data?.data?.get(0)?.preference?.get(0)?.autoPlay!!
            }
        } catch (e: Exception) {

        }
        return isEnable
    }

    fun getStreamUrl(
        playable: PlayableContentModel.Body.Data.Url.Playable,
        playableContentModel: PlayableContentModel,
        streamQuality: String
    ): PlayableContentModel {
        if (playable.key.equals(streamQuality, true)) {
            playableContentModel.data.head.headData.misc.url =
                playable.data
            if (!TextUtils.isEmpty(playable.token)) {
                playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                    playable.token
            }
            playableContentModel.data.head.headData.misc.urlKey =
                playable.key
        }
        return playableContentModel
    }

    /**
     * Return true if this [Context] is available.
     * Availability is defined as the following:
     * + [Context] is not null
     * + [Context] is not destroyed (tested with [FragmentActivity.isDestroyed] or [Activity.isDestroyed])
     */
    fun Context?.isAvailable(): Boolean = runBlocking {
        if (this == null) {
            setLog("ctxAvailable", "isAvailable-1")
            return@runBlocking false
        } else if (this !is Application) {
            setLog("ctxAvailable", "isAvailable-2")
            if (this is FragmentActivity) {
                setLog("ctxAvailable", "isAvailable-3.1-!this.isDestroyed-${!this.isDestroyed}")
                return@runBlocking !this.isDestroyed
            } else if (this is Activity) {
                setLog("ctxAvailable", "isAvailable-3.2-!this.isDestroyed-${!this.isDestroyed}")
                return@runBlocking !this.isDestroyed
            }
        }
        setLog("ctxAvailable", "isAvailable-5")
        return@runBlocking true
    }

    fun callStreamTriggerEvent(
        context: Context,
        track: Track,
        playerType: String,
        duration: String
    ) {
        val eventModel = HungamaMusicApp.getInstance().getEventData("" + track.id)

        setLog(TAG, "callStreamTriggerEvent id ${track.id} eventModel:${eventModel}")

        val hashMap = HashMap<String, String>()

        hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel.actor)
        hashMap.put(EventConstant.AUDIO_QUALITY_EPROPERTY, "" + eventModel.audioQuality)
        hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + eventModel.bucketName)
        hashMap.put(
            EventConstant.CONNECTION_TYPE_EPROPERTY, ConnectionUtil(context).networkType
        )
        if (ConnectionUtil(context).isOnline(false)) {
            hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, "" + eventModel.consumptionType)
            setLog("consumptionType", "consumptionType-1-${eventModel.consumptionType}")
        } else {
            hashMap.put(
                EventConstant.CONSUMPTION_TYPE_EPROPERTY,
                "" + EventConstant.CONSUMPTIONTYPE_OFFLINE
            )
            setLog("consumptionType", "consumptionType-2-${EventConstant.CONSUMPTIONTYPE_OFFLINE}")
        }
        val newContentId = track.id
        val contentIdData = newContentId.toString().replace("playlist-", "")
        setLog("contentIdDataID", "${contentIdData}")
        hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)
        hashMap.put(
            EventConstant.CONTENTTYPE_EPROPERTY,
            "" + Utils.getContentTypeNameForStream("" + track.playerType)
        )

        hashMap.put(EventConstant.DURATION_EPROPERTY, "" + eventModel.duration)
        hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY, playerType)
        hashMap.put(EventConstant.GENRE_EPROPERTY, "" + eventModel.genre)
        hashMap.put(EventConstant.LABEL_EPROPERTY, "" + eventModel.label)
        hashMap.put(EventConstant.LABEL_ID_EPROPERTY, "" + eventModel.label_id)
        hashMap.put(EventConstant.LANGUAGE_EPROPERTY, "" + eventModel.language)


        hashMap.put(EventConstant.LYRICIST_EPROPERTY, "" + eventModel.lyricist)
        hashMap.put(EventConstant.MOOD_EPROPERTY, "" + eventModel.mood)
        hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY, "" + eventModel.musicDirectorComposer)
        hashMap.put(EventConstant.ORIGINAL_ALBUM_NAME_EPROPERTY, "" + eventModel.originalAlbumName)

        setLog("CallStreamLength ", eventModel.duration.toString())

        if (TextUtils.isEmpty(duration)) {
            if (!TextUtils.isEmpty(eventModel.duration)) {
                hashMap.put(EventConstant.LENGTH_EPROPERTY, eventModel.duration)
            } else {
                hashMap.put(EventConstant.LENGTH_EPROPERTY, "0")
            }
        } else {
            hashMap.put(EventConstant.LENGTH_EPROPERTY, duration)
        }
        hashMap.put(
            EventConstant.PODCAST_ALBUM_NAME_EPROPERTY, "" + "" + eventModel.podcast_album_name
        )

        hashMap.put(EventConstant.SINGER_EPROPERTY, "" + eventModel.singer)
        hashMap.put(EventConstant.SONG_NAME_EPROPERTY, "" + track.title)
        hashMap.put(EventConstant.CONTENT_NAME_EPROPERTY, "" + track.title)
        hashMap.put(EventConstant.CONTENT_TYPE_ID_EPROPERTY, "" + track.playerType)
        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, "" + eventModel.sourceName)
        hashMap.put(EventConstant.SUB_GENRE_EPROPERTY, "" + eventModel.subGenre)
        hashMap.put(EventConstant.LYRICS_TYPE_EPROPERTY, "" + eventModel.lyrics_type)

        hashMap.put(EventConstant.TEMPO_EPROPERTY, "" + eventModel.tempo)
        hashMap.put(
            EventConstant.YEAROFRELEASE_EPROPERTY, "" + convertDate(
                DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                DATE_YYYY,
                eventModel.release_Date
            )
        )
        hashMap.put(EventConstant.RATING_EPROPERTY, "" + eventModel.rating)
        hashMap.put(EventConstant.IS_ORIGINAL_EPROPERTY, "" + eventModel.is_original)
        hashMap.put(EventConstant.CATEGORY_EPROPERTY, "" + eventModel.category)
        hashMap.put(EventConstant.CAST_ENABLED_EPROPERTY, "False")
        hashMap.put(EventConstant.AGE_RATING_EPROPERTY, eventModel.age_rating)
        hashMap.put(EventConstant.CONTENT_PAY_TYPE_EPROPERTY, "" + eventModel.content_Pay_Type)
        hashMap.put(EventConstant.CRITIC_RATING_EPROPERTY, "" + eventModel.critic_Rating)
        hashMap.put(EventConstant.KEYWORDS_EPROPERTY, "" + eventModel.keywords)
        hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "" + eventModel.episodeNumber)
        hashMap.put(EventConstant.PTYPE_EPROPERTY, "" + eventModel.ptype)
        hashMap.put(EventConstant.PID_EPROPERTY, "" + eventModel.pid)
        hashMap.put(EventConstant.PNAME_EPROPERTY, "" + eventModel.pName)
        hashMap.put(
            EventConstant.RELEASE_DATE_EPROPERTY, "" + convertDate(
                DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                DATE_FORMAT_DD_MM_YYYY_slash,
                eventModel.release_Date
            )
        )
        hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "" + eventModel.season_Number)
        hashMap.put(EventConstant.SUBTITLE_ENABLE_EPROPERTY, "" + eventModel.subtitleEnable)
        hashMap.put(
            EventConstant.SUBTITLE_LANGUAGE_SELECTED_EPROPERTY,
            "" + eventModel.subtitleLanguageSelected
        )
        hashMap.put(EventConstant.USER_RATING_EPROPERTY, "" + eventModel.userRating)
        hashMap.put(EventConstant.VIDEO_QUALITY_EPROPERTY, "" + eventModel.videoQuality)
        hashMap.put(
            EventConstant.SOURCEPAGE_EPROPERTY,
            MainActivity.lastItemClicked + "," + MainActivity.headerItemName + "," + if (!TextUtils.isEmpty(
                    track.heading
                )
            ) "," + track.heading else "," + eventModel.sourceName
        )
        hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY, track.heading!!)
        hashMap.put(
            EventConstant.SOURCE_EPROPERTY,
            "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + if (!TextUtils.isEmpty(
                    track.heading
                )
            ) "_" + track.heading else "_" + eventModel.sourceName
        )
        if (Constant.API_DEVICE_TYPE.equals("Android", ignoreCase = true)) {
            hashMap.put(EventConstant.CARPLAY, "false")
        } else {
            hashMap.put(EventConstant.CARPLAY, "true")
        }

        setLog(TAG, "callStreamTriggerEvent stream triggered *** hashMap:$hashMap")

        if (!TextUtils.isEmpty(hashMap.get(EventConstant.SONG_NAME_EPROPERTY))) {
            hashMap.remove(EventConstant.DURATION_EPROPERTY)
            hashMap.remove(EventConstant.DURATION_BG_EPROPERTY)
            hashMap.remove(EventConstant.DURATION_FG_EPROPERTY)
            hashMap.remove(EventConstant.PERCENTAGE_COMPLETION_EPROPERTY)

            if(isUserHasGoldSubscription())
            {
                EventManager.getInstance().sendEvent(StreamTriggerEvent(hashMap))
            }
            else{
                setLog("CheckLocalDuration", " " + BaseActivity.localDuration.toString())
                if (BaseActivity.localDuration > 0) {
                    EventManager.getInstance().sendEvent(StreamTriggerEvent(hashMap))
                }
                else
                {
                    EventManager.getInstance().sendEvent(PreviewStreamTriggerEvent(hashMap))
                }
            }
        }

    }

    fun getAllUpiEnabledAppList(context: Context): ArrayList<String> = runBlocking(Dispatchers.IO) {
        val upilist: ArrayList<String> = ArrayList()
        try {
            val packageManager = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_DEFAULT)
            mainIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            mainIntent.action = Intent.ACTION_VIEW
            val uri1: Uri = Uri.Builder().scheme("upi").authority("pay").build()
            mainIntent.data = uri1
            val pkgAppsList: List<*> = packageManager.queryIntentActivities(mainIntent, 0)

            for (i in pkgAppsList.indices) {
                val resolveInfo: ResolveInfo = pkgAppsList[i] as ResolveInfo
                //setLog("TAG", "packageName: " + resolveInfo.activityInfo.packageName)
                //setLog("TAG", "AppName: " + resolveInfo.loadLabel(packageManager))
                //setLog("TAG", "AppIcon: " + resolveInfo.loadIcon(packageManager))
                upilist.add(resolveInfo.loadLabel(packageManager).toString())
            }
        } catch (e: Exception) {

        }

        upilist
    }

    fun getAllMusicEnabledAppList(context: Context): ArrayList<String> =
        runBlocking(Dispatchers.IO) {
            val musicApps: ArrayList<String> = ArrayList()
            try {
                val packageManager = context.packageManager
                val pkgAppsList: List<ApplicationInfo?> =
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                for (i in pkgAppsList.indices) {
                    val resolveInfo: ApplicationInfo = pkgAppsList[i] as ApplicationInfo
                    var musicAppName = ""
                    val appPackage = resolveInfo.packageName
                    //setLog("TAG", "packageName: " + appPackage)
                    //setLog("TAG", "AppName: " + appName)
                    //setLog("TAG", "AppIcon: " + resolveInfo.loadIcon(packageManager))

                    if (appPackage.equals("com.spotify.music") || appPackage.equals("com.gaana") || appPackage.equals(
                            "com.jio.media.jiobeats"
                        ) || appPackage.equals("com.bsbportal.music") || appPackage.equals("com.apple.android.music") || appPackage.equals(
                            "com.moonvideo.android.resso"
                        ) || appPackage.equals("com.amazon.dee.app")
                    ) {
                        val appName = resolveInfo.loadLabel(packageManager)
                        musicAppName = appName.toString()
                    }
                    if (!TextUtils.isEmpty(musicAppName)) {
                        musicApps.add(musicAppName)
                    }
                }
                //setLog("getAllMusicEnabledAppList", "getAllMusicEnabledAppList-${musicApps.toString()}")
            } catch (e: Exception) {

            }
            musicApps
        }

    fun getAllVideoEnabledAppList(context: Context): ArrayList<String> =
        runBlocking(Dispatchers.IO) {
            val videoApps: ArrayList<String> = ArrayList()
            try {
                val packageManager = context.packageManager
                val pkgAppsList: List<ApplicationInfo?> =
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)


                for (i in pkgAppsList.indices) {
                    val resolveInfo: ApplicationInfo = pkgAppsList[i] as ApplicationInfo
                    var videoAppName = ""
                    val appPackage = resolveInfo.packageName
                    //setLog("TAG", "packageName: " + appPackage)
                    //setLog("TAG", "AppName: " + appName)
                    //setLog("TAG", "AppIcon: " + resolveInfo.loadIcon(packageManager))

                    if (appPackage.equals("com.netflix.mediaclient") || appPackage.equals("com.tv.v18.viola") || appPackage.equals(
                            "in.startv.hotstar"
                        ) || appPackage.equals("in.startv.hotstar.dplus") || appPackage.equals("com.discoveryplus.mobile.android") || appPackage.equals(
                            "com.amazon.avod.thirdpartyclient"
                        ) || appPackage.equals("com.hungama.movies") || appPackage.equals("com.mxtech.videoplayer.ad") || appPackage.equals(
                            "tv.accedo.airtel.wynk"
                        ) || appPackage.equals("com.graymatrix.did") || appPackage.equals("com.vodafone.vodafoneplay") || appPackage.equals(
                            "com.amazon.dee.app"
                        ) || appPackage.equals("com.google.android.apps.googleassistant")
                    ) {
                        val appName = resolveInfo.loadLabel(packageManager)
                        videoAppName = appName.toString()
                    }
                    if (!TextUtils.isEmpty(videoAppName)) {
                        videoApps.add(videoAppName)
                    }
                }
                //setLog("getAllMusicEnabledAppList", "getAllMusicEnabledAppList-${videoApps.toString()}")
            } catch (e: Exception) {

            }
            videoApps
        }

    public suspend fun setDownloadEventModelDataAppLevel(selectedContentId: String) {
        setLog("setEventData", "setDownloadEventModelDataAppLevel contentID: ${selectedContentId}")

        val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
            ?.findByContentId(selectedContentId)

        if (downloadedAudio != null) {
            var eventModel = EventModel()


            if (!TextUtils.isEmpty(selectedContentId)) {
                eventModel?.contentID = selectedContentId
            }

            if (TextUtils.isEmpty(eventModel?.songName)) {
                eventModel?.songName = "" + downloadedAudio?.title
            }

            if (!TextUtils.isEmpty(downloadedAudio?.actor)) {
                eventModel?.actor = downloadedAudio?.actor!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.pid)) {
                eventModel?.album_ID = downloadedAudio?.pid!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.pName)) {
                eventModel?.album_name = downloadedAudio?.pName!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.pName)) {
                eventModel?.originalAlbumName = downloadedAudio?.pName!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.keywords)) {
                eventModel?.keywords = downloadedAudio?.keywords!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.category)) {
                eventModel?.category = downloadedAudio?.category!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.criticRating)) {
                eventModel?.critic_Rating = downloadedAudio?.criticRating!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.contentPayType)) {
                eventModel?.content_Pay_Type = downloadedAudio?.contentPayType!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.releaseDate)) {
                eventModel?.release_Date = downloadedAudio?.releaseDate!!
            }
            if (!TextUtils.isEmpty(downloadedAudio?.genre)) {
                eventModel?.genre = downloadedAudio?.genre!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.attribute_censor_rating)) {
                eventModel?.age_rating = downloadedAudio?.attribute_censor_rating!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.lyricist)) {
                eventModel?.lyricist = downloadedAudio?.lyricist!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.musicDirectorComposer)) {
                eventModel?.musicDirectorComposer = downloadedAudio?.musicDirectorComposer!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.singer)) {
                eventModel?.singer = downloadedAudio?.singer!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.mood)) {
                eventModel?.mood = downloadedAudio?.mood!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.tempo)) {
                eventModel?.tempo = downloadedAudio?.tempo!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.language)) {
                eventModel?.language = downloadedAudio?.language!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.lyricsType)) {
                eventModel?.lyrics_type = downloadedAudio?.lyricsType!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.label)) {
                eventModel?.label = downloadedAudio?.label!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.labelId)) {
                eventModel?.label_id = downloadedAudio?.labelId!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.contentShareLink)) {
                eventModel?.share = downloadedAudio?.contentShareLink!!
            }

            if (!TextUtils.isEmpty("" + downloadedAudio?.pType!!)) {
                eventModel?.ptype = "" + downloadedAudio?.pType!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.pid)) {
                eventModel?.pid = downloadedAudio?.pid!!
            }
            if (!TextUtils.isEmpty(downloadedAudio?.pName)) {
                eventModel?.pName = downloadedAudio?.pName!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.audioQuality)) {
                eventModel?.audioQuality = downloadedAudio?.audioQuality!!
            }

            if (!TextUtils.isEmpty(downloadedAudio?.videoQuality)) {
                eventModel?.videoQuality = downloadedAudio?.videoQuality!!
            }

            if (TextUtils.isEmpty(eventModel?.audioQuality)) {
                eventModel?.audioQuality = "Auto"
            }

            if (TextUtils.isEmpty(eventModel?.videoQuality)) {
                eventModel?.videoQuality = "Auto"
            }

            eventModel?.consumptionType = EventConstant.CONSUMPTIONTYPE_OFFLINE

            setLog(
                "setEventData",
                "DownloadContent contentID: ${eventModel?.contentID} setEventData:${eventModel}"
            )

            HungamaMusicApp.getInstance().setEventData(eventModel?.contentID!!, eventModel)
        }


    }

    fun gcd(p: Int, q: Int): Int {
        return if (q == 0) p else gcd(q, p % q)
    }

    fun ratio(a: Int, b: Int) {
        val gcd = gcd(a, b)
        if (a > b) {
            showAnswer(a / gcd, b / gcd)
        } else {
            showAnswer(b / gcd, a / gcd)
        }
    }

    fun showAnswer(a: Int, b: Int) {
        println("$a $b")
    }

    fun getStringResourceByName(aString: String): String? {
        val packageName: String = HungamaMusicApp.getInstance().packageName
        val resId: Int = HungamaMusicApp.getInstance().resources
            .getIdentifier(aString, "string", packageName)
        return HungamaMusicApp.getInstance().getString(resId)
    }

    fun Any?.checkNull(): String? {
        return null
    }

    fun getbanner(context: Context, iv_banner: ImageView, trigger: String) {
        if (context != null && iv_banner != null) {
            var ft: AdsConfigModel.Ft? = AdsConfigModel.Ft()
            var nonft: AdsConfigModel.Nonft? = AdsConfigModel.Nonft()
            if (!isUserHasGoldSubscription()) {

                if (getFirebaseConfigAdsData().enablePaymentNudge &&
                    Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)
                ) {
                    iv_banner.show()
                }

                if (trigger == Constant.nudge_album_banner) {
                    if (isUserHasEliableFreeContent()) {
                        ft = getFirebaseConfigAdsData().nudgeAlbumBanner.ft

                        if (!TextUtils.isEmpty(ft?.image_url)) {
                            ft?.image_url?.let {
                                ImageLoader.loadImageWithoutPlaceHolder(
                                    context,
                                    iv_banner,
                                    it
                                )
                            }
                        }

                    } else {
                        nonft = getFirebaseConfigAdsData().nudgeAlbumBanner.nonft

                        if (!TextUtils.isEmpty(nonft?.image_url)) {
                            if (nonft != null) {
                                ImageLoader.loadImageWithoutPlaceHolder(
                                    context,
                                    iv_banner,
                                    nonft.image_url
                                )
                            }
                        }
                    }
                } else {
                    if (isUserHasEliableFreeContent()) {
                        ft = getFirebaseConfigAdsData()?.nudge_playlist_banner?.ft

                        if (!TextUtils.isEmpty(ft?.image_url)) {
                            ft?.image_url?.let {
                                ImageLoader.loadImageWithoutPlaceHolder(
                                    context,
                                    iv_banner,
                                    it
                                )
                            }
                        }

                    } else {
                        nonft = getFirebaseConfigAdsData().nudge_playlist_banner.nonft

                        if (!TextUtils.isEmpty(nonft?.image_url)) {
                            nonft?.image_url?.let {
                                ImageLoader.loadImageWithoutPlaceHolder(
                                    context,
                                    iv_banner,
                                    it
                                )
                            }
                        }
                    }
                }
            }

            iv_banner.setOnClickListener {
                openSubscriptionDialogPopup(
                    context,
                    PlanNames.SVOD.name,
                    "",
                    true,
                    null,
                    "",
                    null,
                    trigger,
                    "banner"
                )
            }
        }
    }

    fun bottomMusicrestricMenu(cxt: Context): Boolean {
        if (!isUserHasGoldSubscription() && getFirebaseConfigAdsData().enablePaymentDrawer && Constant.DEFAULT_COUNTRY_CODE.equals(
                "IN",
                true
            )
        ) {

            openSubscriptionDialogPopup(
                cxt,
                PlanNames.SVOD.name,
                "",
                true,
                null,
                "",
                null,
                Constant.drawer_download_mymusic
            )

            return false
        } else return true

    }

    fun setButtonFromFirebase(
        context: Context, ft: SongDurationConfigModel.Ft?, nonft: SongDurationConfigModel.Nonft?,
        tvUpgradePlan: TextView, btnSeeAllPlan: TextView, it: Resource<PlanInfoContentModel>
    ) {
        //   dismissLoader()
        if (isUserHasEliableFreeContent() && ft?.button_text_1.equals("macro_payment_buy_ft_cta")) {

            tvUpgradePlan.text = String.format(context.getString(R.string.macro_payment_buy_ft_cta),it.data?.planInfo?.planName?.lowercase())

        } else if (isUserHasEliableFreeContent() && ft?.button_text_1.equals("macro_payment_buy_nonft_cta")
        ) {
            tvUpgradePlan.text = String.format(context.getString(R.string.macro_payment_buy_nonft_cta),it.data?.planInfo?.planCurrencySymbol,if (it.data?.planInfo?.planCurrency == "INR") it.data?.planInfo?.planPrice.toString().replace(".0","") else it.data?.planInfo?.planPrice,it.data?.planInfo?.planName?.lowercase())

        }else if (!isUserHasEliableFreeContent() && nonft?.button_text_1.equals("macro_payment_buy_nonft_cta")
        ) {
            tvUpgradePlan.text = String.format(context.getString(R.string.macro_payment_buy_nonft_cta),it.data?.planInfo?.planCurrencySymbol,if (it.data?.planInfo?.planCurrency == "INR") it.data?.planInfo?.planPrice.toString().replace(".0","") else it.data?.planInfo?.planPrice,it.data?.planInfo?.planName?.lowercase())

        }else if (!isUserHasEliableFreeContent() && nonft?.button_text_1.equals("macro_payment_buy_ft_cta")) {

            tvUpgradePlan.text = String.format(context.getString(R.string.macro_payment_buy_ft_cta),it.data?.planInfo?.planName?.lowercase())

        } else {
            tvUpgradePlan.text = context.getString(R.string.drawer_download_all_CTA)
            btnSeeAllPlan.text = context.getString(R.string.btn_seeall_plan_1)
        }
    }
}