package com.hungama.music.ui.base


import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.cast.CastPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.mediarouter.app.MediaRouteButton
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.facebook.CallbackManager
import com.facebook.share.model.*
import com.facebook.share.widget.ShareDialog
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.eventanalytic.util.callbacks.inapp.InAppCallback
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.TracklistDataModel
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.videoplayer.AudioSubtitleSelectBottomSheetFragment
import com.hungama.music.player.videoplayer.SubtitleSelectBottomSheetFragment
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.main.adapter.BucketChildAdapter
import com.hungama.music.ui.main.view.activity.CommonWebViewActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.activity.StoryDisplayActivity
import com.hungama.music.ui.main.view.fragment.*
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.checkContentOrderStatus
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.removeDownloadedContent
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.ADD_TO_OTHER_PLAYLIST_MENU_ITEM
import com.hungama.music.utils.Constant.ADD_TO_PLAYLIST_MENU_ITEM
import com.hungama.music.utils.Constant.ADD_TO_QUEUE_MENU_ITEM
import com.hungama.music.utils.Constant.ADD_TO_THE_LIST_MENU_ITEM
import com.hungama.music.utils.Constant.ALBUM_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.ALBUM_DETAIL_PAGE
import com.hungama.music.utils.Constant.ARTIST_DETAIL_PAGE
import com.hungama.music.utils.Constant.CHART_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.CHART_DETAIL_PAGE
import com.hungama.music.utils.Constant.CLEAR_QUEUE_MENU_ITEM
import com.hungama.music.utils.Constant.COLLECTION_DETAIL_PAGE
import com.hungama.music.utils.Constant.CONTENT_ORDER_STATUS_NA
import com.hungama.music.utils.Constant.DELETE_PLAYLIST_MENU_ITEM
import com.hungama.music.utils.Constant.DOWNLOADED_CONTENT_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.DOWNLOADED_CONTENT_DETAIL_PAGE
import com.hungama.music.utils.Constant.DOWNLOADED_MUSIC_VIDEO_ADAPTER_PAGE
import com.hungama.music.utils.Constant.DOWNLOADED_PODCAST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.DOWNLOADED_PODCAST_DETAIL_PAGE
import com.hungama.music.utils.Constant.DOWNLOAD_MENU_ITEM
import com.hungama.music.utils.Constant.EDIT_PLAYLIST_MENU_ITEM
import com.hungama.music.utils.Constant.FAVORITED_CONTENT_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.FAVORITED_CONTENT_DETAIL_PAGE
import com.hungama.music.utils.Constant.FIND_MORE_SONGS_MENU_ITEM
import com.hungama.music.utils.Constant.GO_TO_ALBUM_MENU_ITEM
import com.hungama.music.utils.Constant.LANGUAGE_MENU_ITEM
import com.hungama.music.utils.Constant.LIKE_MENU_ITEM
import com.hungama.music.utils.Constant.MAKE_PLYALIST_PRIVATE_MENU_ITEM
import com.hungama.music.utils.Constant.MOVIE_DETAIL_PAGE
import com.hungama.music.utils.Constant.MUSIC_VIDEO_DETAIL_PAGE
import com.hungama.music.utils.Constant.MY_PLAYLIST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.MY_PLAYLIST_DETAIL_PAGE
import com.hungama.music.utils.Constant.PLAYLIST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.PLAYLIST_DETAIL_PAGE
import com.hungama.music.utils.Constant.PLAY_NEXT_MENU_ITEM
import com.hungama.music.utils.Constant.PODCAST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.PODCAST_DETAIL_PAGE
import com.hungama.music.utils.Constant.QUEUE_MANAGER_DETAIL_PAGE
import com.hungama.music.utils.Constant.REMOVE_DOWNLOADED_CONTENT_MENU_ITEM
import com.hungama.music.utils.Constant.REMOVE_FROM_LIBRARY
import com.hungama.music.utils.Constant.REMOVE_FROM_PLAYLIST_MENU_ITEM
import com.hungama.music.utils.Constant.REMOVE_FROM_WATCHLIST
import com.hungama.music.utils.Constant.SHARE_MENU_ITEM
import com.hungama.music.utils.Constant.SHARE_STORY_MENU_ITEM
import com.hungama.music.utils.Constant.SHUFFLE_PLAY_MENU_ITEM
import com.hungama.music.utils.Constant.SIMILAR_ALBUM_MENU_ITEM
import com.hungama.music.utils.Constant.SIMILAR_SONGS_MENU_ITEM
import com.hungama.music.utils.Constant.SIMILAR_SONG_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.SONG_DETAIL_MENU_ITEM
import com.hungama.music.utils.Constant.SONG_DETAIL_PAGE
import com.hungama.music.utils.Constant.SUBTITLE_MENU_ITEM
import com.hungama.music.utils.Constant.TVSHOW_DETAIL
import com.hungama.music.utils.Constant.TVSHOW_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.TVSHOW_DETAIL_PAGE
import com.hungama.music.utils.Constant.VIDEO_QUALITY_MENU_ITEM
import com.hungama.music.utils.Constant.VIDEO_WATCHLIST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.WATCH_WITH_FRIENDS_MENU_ITEM
import com.hungama.music.utils.Constant.defaultContentId
import com.hungama.music.utils.Constant.defaultContentImage
import com.hungama.music.utils.Constant.defaultContentPlayerType
import com.hungama.music.utils.Constant.defaultContentVarient
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.utils.fontmanger.FontDrawable
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.moengage.cards.MoECardHelper
import com.moengage.cards.ui.CardActivity
import com.moengage.inapp.MoEInAppHelper
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Runnable
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: Base of all fragment
 */
@OptIn(UnstableApi::class)
abstract class BaseFragment : Fragment(), View.OnClickListener,
    CommonThreeDotsMenuFragment.OnThreeDotMenuItemClick,
    VideoPlayBackSettingStreamQuality.OnItemClick {


    val TAG = this.javaClass.simpleName

    protected var tvCount: TextView? = null
    protected var ivUserPersonalImage: AppCompatImageView? = null
    protected var ivBack: AppCompatImageView? = null
    protected var ivSearch: AppCompatImageView? = null
    protected var rlNotification: RelativeLayout? = null
    protected var ivNotification: AppCompatImageView? = null
    protected var layoutEmptyMain: LinearLayout? = null
    protected var ivLogo: AppCompatImageView? = null
    /**
     * Contains last clicked time
     */
    var lastClickedTime: Long = 0


    /**
     * Shows progress indication in screens
     */
    protected var pbProgress: ProgressBar? = null


    var trackDataList: ArrayList<Track> = arrayListOf()
    var playableShuffledContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var userViewModelBookmark: UserViewModel? = null
    var onMenuItemClicked: OnMenuItemClicked? = null
    var userSubscriptionCheckViewModel: UserSubscriptionViewModel? = null
    var onUserContentOrderStatus: OnUserContentOrderStatus? = null
    var contentOrderStatusHandler: Handler? = null
    private val contentOrderStatusTimerInterval: Long = 1000 * 5 * 1 //5 seconds
    var mCallbackManager: CallbackManager? = null
    var shareDialog: ShareDialog? = null
    var isShareUserProfile=true

     var baseServiceJob = SupervisorJob()
     var baseMainScope = CoroutineScope(Dispatchers.Main + baseServiceJob)
     var baseIOServiceJob = SupervisorJob()
     var baseIOScope = CoroutineScope(Dispatchers.IO + baseIOServiceJob)

    companion object {
        var headerHomeMain: RelativeLayout? = null
        public var castPlayer: CastPlayer?=null
        var isCastPlayerAudio=false
        public var mediaRouteButton: MediaRouteButton?=null
    }


    override fun onAttach(context: Context) {
        LanguageUtil.getLocal(requireActivity())
        Constant.screen_name ="Home Screen"
        super.onAttach(context)

    }

    override fun onResume() {
        super.onResume()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
        (requireActivity() as MainActivity)?.updateProfile()
        try {
            setLog("BaseFragment", "he_api-${CommonUtils.getFirebaseConfigHEAPIData()}")

            System.runFinalization()
        }catch (exp:Exception){
            exp.printStackTrace()
        }

    }

    override fun onStart() {
        super.onStart()
        MoEInAppHelper.getInstance().showInApp(requireActivity())
    }



    /**
     * Initialize the components for Fragment's view
     *
     * @param view A View inflated into Fragment
     */
    protected abstract fun initializeComponent(view: View) //to initialize the fragments components


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LanguageUtil.getLocal(requireActivity())
        super.onViewCreated(view, savedInstanceState)
        if(activity != null && activity is MainActivity){
            (requireActivity() as MainActivity)?.updateViewBasedOnSubscription()
        }
        pbProgress = view.findViewById(R.id.pb_progress)
        ivUserPersonalImage = view.findViewById(R.id.ivUserPersonalImage)
        ivBack = view.findViewById(R.id.ivBack)
        rlNotification = view.findViewById(R.id.rlNotification)
        ivNotification = view.findViewById(R.id.ivNotification)
        layoutEmptyMain = view.findViewById(R.id.layoutEmptyMain)

        tvCount = view.findViewById(R.id.tvCount)
        ivSearch = view.findViewById(R.id.ivSearch)
        ivLogo = view.findViewById(R.id.ivLogo)

        mCallbackManager = CallbackManager.Factory.create();
        shareDialog = ShareDialog(this);
        if (ivUserPersonalImage != null) {
            ivUserPersonalImage?.setOnClickListener(this)
        }

        if (ivBack != null) {
            ivBack?.setOnClickListener(this)
        }

        if (rlNotification != null) {
            val icon_notification = FontDrawable(requireContext(), R.string.icon_notification)
            icon_notification.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            ivNotification?.setImageDrawable(icon_notification)
            //setLog(TAG, "onViewCreated: rlNotification not null"+rlNotification?.rootView?.isVisible)
            rlNotification?.setOnClickListener(this)
        } else {
            //setLog(TAG, "onViewCreated: rlNotification null ")
        }

/*        if (ivUserPersonalImage != null) {
            if (!TextUtils.isEmpty(
                    SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE, "")
                )
            ) {
                ImageLoader.loadImage(
                    requireActivity(),
                    ivUserPersonalImage!!,
                    SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE, ""),
                    R.drawable.bg_gradient_placeholder
                )
            }
        }
        setLog(TAG, "tvCoinCount 1 login:${SharedPrefHelper.getInstance().isUserLogdIn()} tvCoinCount:${tvCoinCount?.text}")
        if (SharedPrefHelper.getInstance().isUserLogdIn()) {
            val userCoinDetailRespModel = SharedPrefHelper?.getInstance()?.getObjectUserCoin(
                PrefConstant.USER_COIN
            )
            var gmfSDKCoins = GamificationSDK.getPoints()
            if (gmfSDKCoins < 0){
                gmfSDKCoins = 0
            }
            userCoinDetailRespModel?.actions?.get(0)?.total = gmfSDKCoins
            if (userCoinDetailRespModel != null && userCoinDetailRespModel?.actions != null) {
                tvCoinCount?.setText(
                    CommonUtils?.ratingWithSuffix(
                        "" + userCoinDetailRespModel?.actions?.get(
                            0
                        )?.total!!
                    )
                )
            } else {
                tvCoinCount?.setText(CommonUtils?.ratingWithSuffix(""+gmfSDKCoins!!))
            }
            setLog(TAG, "tvCoinCount 2 login:${tvCoinCount?.text} tvCoinCount:$tvCoinCount")
        }else{
            tvCoinCount?.setText("0")
            setLog(TAG, "tvCoinCount 3 without login coin:${tvCoinCount?.text} tvCoinCount:$tvCoinCount")
        }
        val icon_voice_search = FontDrawable(requireContext(), R.string.icon_voice_search)
        icon_voice_search.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        ivVoiceImage?.setImageDrawable(icon_voice_search)
        ivVoiceImage?.setOnClickListener(this)*/

        updateProfile()


        if(tvCount!=null){
            val newCardCount = ""+ MoECardHelper.getInstance().getNewCardCount(requireContext())
            setLog(TAG, "onViewCreated newCardCount:${newCardCount}")
            if(newCardCount!=null && !TextUtils.isEmpty(newCardCount)){
                tvCount?.setText(newCardCount)
                tvCount?.visibility=View.GONE
            }else{
                tvCount?.visibility=View.GONE
            }
        }

/*        val remoteConfig = Firebase.remoteConfig
        setLog(TAG, "onViewCreated-> share_user_profile: ${remoteConfig.getBoolean("share_user_profile")}")
        isShareUserProfile = remoteConfig.getBoolean("share_user_profile")*/
        baseServiceJob = SupervisorJob()
        baseIOServiceJob = SupervisorJob()
        baseMainScope = CoroutineScope(Dispatchers.Main + baseServiceJob)
        baseIOScope = CoroutineScope(Dispatchers.IO + baseIOServiceJob)

        initializeComponent(view)
        activity?.let { Utils.hideSoftKeyBoard(it, view) }

        ivLogo?.setOnClickListener {
            if (logoClickCount > 5){
                val messageModel = MessageModel(SharedPrefHelper.getInstance().getUserId().toString(),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
                logoClickCount = 0
            }else{
                logoClickCount += 1
                startLogoClickCallback()
            }

            if (BaseActivity.eventManagerStreamName != EventConstant.LOGO_CLICK){
                BaseActivity.eventManagerStreamName = EventConstant.LOGO_CLICK
                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap[EventConstant.ACTION_EPROPERTY] = "LogoClicked"
                    EventManager.getInstance().sendEvent(LogoClickEvent(hashMap))
                }
            }
        }
    }
    var logoClickCount = 0
    var logoClickHandler: Handler? = null
    private val callClick = object : Runnable{
        override fun run() {
            setLog("BaseFragment", "BaseFragment-logoClickCount-$logoClickCount")
            logoClickCount = 0
        }
    }
    private fun startLogoClickCallback(){
        if (logoClickHandler != null){
            setLog("BaseFragment", "BaseFragment-startLogoClickCallback-removeApiCallback()")
            removeLogoClickCallback()
            setLog("BaseFragment", "BaseFragment-startLogoClickCallback-If-postDelayed(20000)")
            logoClickHandler?.postDelayed(callClick, 20000)
        }else{
            logoClickHandler = Handler(Looper.getMainLooper())
            setLog("BaseFragment", "BaseFragment-startLogoClickCallback-Else-postDelayed(20000)")
            logoClickHandler?.postDelayed(callClick, 20000)
        }
    }
    private fun removeLogoClickCallback(){
        if (logoClickHandler != null){
            logoClickHandler?.removeCallbacks(callClick)
        }
    }

    protected fun setProgressBarVisible(visible: Boolean) {
        if (pbProgress != null) {
            if (visible) {
                pbProgress?.visibility = View.VISIBLE
            } else {
                pbProgress?.visibility = View.GONE
            }

        }
    }

    protected fun isProgressVisible():Boolean{
        if (pbProgress != null) {
           if (pbProgress?.visibility == View.VISIBLE){
               return true
           }
        }
        return false
    }

    protected fun setEmptyVisible(isVisible: Boolean) {
        if (layoutEmptyMain != null) {
            layoutEmptyMain!!.visibility = if (isVisible) View.GONE else View.GONE

        }
    }

    /**
     * Adds the Fragment into layout container.
     *
     * @param container               Resource id of the layout in which Fragment will be added
     * @param currentFragment         Current loaded Fragment to be hide
     * @param nextFragment            New Fragment to be loaded into container
     * @param requiredAnimation       true if screen transition animation is required
     * @param commitAllowingStateLoss true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws ClassCastException    Throws exception if getActivity() is not an instance of BaseActivity
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */
    @Throws(ClassCastException::class, IllegalStateException::class)
    protected fun addFragment(
        container: Int,
        currentFragment: Fragment,
        nextFragment: Fragment,
        commitAllowingStateLoss: Boolean
    ): Boolean {
        return if (activity != null) {
            if (activity is BaseActivity) {
                (activity as BaseActivity).addFragment(
                    container,
                    currentFragment,
                    nextFragment,
                    commitAllowingStateLoss
                )
            } else {
                //throw ClassCastException(BaseActivity::class.java.name)
                false
            }
        } else false
    }

    /**
     * Replaces the Fragment into layout container.
     *
     * @param container               Resource id of the layout in which Fragment will be added
     * @param fragmentManager         Activity fragment manager
     * @param nextFragment            New Fragment to be loaded into container
     * @param requiredAnimation       true if screen transition animation is required
     * @param commitAllowingStateLoss true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws ClassCastException    Throws exception if getActivity() is not an instance of BaseActivity
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */
    @Throws(ClassCastException::class, IllegalStateException::class)
    protected fun replaceFragment(
        container: Int,
        nextFragment: Fragment,
        commitAllowingStateLoss: Boolean,
        isFlipAnim: Int
    ): Boolean {

//        setLog("FragmentName",nextFragment.getN)
        return if (activity != null) {
            if (activity is BaseActivity) {
                (activity as BaseActivity).replaceFragment(
                    container,
                    nextFragment,
                    commitAllowingStateLoss
                )
            } else {
                //throw ClassCastException(BaseActivity::class.java.name)
                false
            }
        } else false
    }

    protected suspend fun changeStatusbarcolor(color: Int) {
        if (activity != null) {
            if (activity is BaseActivity) {
                (activity as BaseActivity).changeStatusbarcolor(color)
            }
        }

    }

    protected fun backPress() {
        setLog("onBackPressed", "1111")
        setLog("onBackPressed", "MusicVideoDetailsFragment-backPress-called")
        setLog("onBackPressed", MainActivity.lastItemClicked + " " + MainActivity.headerItemName + " " +MainActivity.tempLastItemClicked)
        hideKeyboard()
        if (activity != null) {
            if (activity is BaseActivity) {
                (activity as BaseActivity).onBackPressed()
            }
        }
    }

    open fun isOnClick(): Boolean {
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

    override fun onClick(v: View) {

        activity?.let { Utils.hideSoftKeyBoard(it, v) }

        if (isOnClick()) {
            when (v.id) {
                R.id.ivBack -> {
                    (activity as BaseActivity).onBackPressed()
                }

                R.id.rlNotification -> {
                    startActivity(Intent(requireActivity(), CardActivity::class.java))
                    CommonUtils.PageViewEvent("","","","",
                        MainActivity.headerItemName + "_" +MainActivity.lastItemClicked + MainActivity.subHeaderItemName,
                        "notification","")
                    MainActivity.subHeaderItemName = ""
                }

                R.id.ivUserPersonalImage -> {
                    addFragment(R.id.fl_container, this, ProfileFragment(), false)
                    CoroutineScope(Dispatchers.IO).launch {
                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ACTION_EPROPERTY, "ProfileClicked")
                        setLog("PROFILECLICKED", "profile${hashMap}")
                        EventManager.getInstance().sendEvent(ProfileClickedEvent(hashMap))
                    }

                }
                R.id.ivSearch -> {
                    if (activity != null) {
                        addFragment(R.id.fl_container, this, SearchAllTabFragment(), false)

                        CoroutineScope(Dispatchers.IO).launch {
                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, MainActivity.lastItemClicked+"_"+MainActivity.headerItemName)
                            hashMap.put(EventConstant.DEVICE, Utils.getDeviceName()!!)
                            hashMap.put(EventConstant.PLATFORM, Constant.DEVICE)
                            EventManager.getInstance().sendEvent(ClickedSearchEvent(hashMap))

                            /* Track Events in real time */
                            val eventValue: HashMap<String, Any> = HashMap()
                            eventValue.put(EventConstant.PLATFORM, Constant.DEVICE)
                            AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,
                                AFInAppEventType.SEARCH, eventValue)
                        }

                    }
                }
            }
        }

    }

    fun showSnakbar(isError: Boolean, message: String) {
        Utils.showSnakbar(requireContext(),requireView(), isError, message)
    }



    abstract fun onCreateOptionsMenu(menu: Menu?): Boolean
    abstract fun onPrepareOptionsMenu(menu: Menu?): Boolean


    fun isHeaderVisible(): Boolean {

        return headerHomeMain != null
    }

    fun isHeaderShowing(): Boolean {
        return headerHomeMain?.isVisible!!
    }

    fun hideHeader() {
        //(headerHomeMain?.findViewById(R.id.headerHome) as RelativeLayout).visibility = View.GONE
        headerHomeMain?.layoutParams?.height = resources.getDimensionPixelSize(R.dimen.dimen_52)
    }

    fun showHeader() {
        //(headerHomeMain?.findViewById(R.id.headerHome) as RelativeLayout).visibility = View.VISIBLE
        headerHomeMain?.layoutParams?.height = resources.getDimensionPixelSize(R.dimen.dimen_115)
    }

    var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            /*if (dy > 0) {
                setLog("Scroll-up", "UP")
            } else {
                setLog("Scroll-dn", "Down")
            }*/

            if (!isHeaderVisible()) {
                return
            }
            if (dy > 0) {
                //if (isHeaderShowing()){
                hideHeader()
                setLog("Scroll-up", "UP")
                //}
            } else {
                //if (!isHeaderShowing()){
                showHeader()
                setLog("Scroll-dn", "Down")
                //}
            }
        }
    }

    fun sendArtworkTappedEvent(
        parent: RowsItem,
        parentPosition: Int,
        childPosition: Int,
        headItemsItem: HeadItemsItem?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = java.util.HashMap<String, String>()
            setLog(TAG, "sendArtworkTappedEvent: parent:$parent")
            var feature = Utils.getContentTypeName(parent.items?.get(childPosition)?.data?.type!!)

            hashMap.put(EventConstant.CONTENTNAME_EPROPERTY, "" + parent.items?.get(childPosition)?.data?.title!!)
            hashMap.put(EventConstant.TOPNAVNAME_EPROPERTY, "" + MainActivity.headerItemName)
            if (headItemsItem != null){
                hashMap.put(EventConstant.FILTERSELECTED_EPROPERTY, "" + headItemsItem?.title)
            }
            hashMap.put(EventConstant.TOPNAVPOSITION_EPROPERTY, "" + MainActivity.headerItemPosition)
            if(parent?.heading?.contains("Good",true)!!){
                hashMap.put(EventConstant.BUCKETTYPE_EPROPERTY, EventConstant.CONTINUE_WATCHING_NAME)
            }else{
                hashMap.put(EventConstant.BUCKETTYPE_EPROPERTY, "" + parent?.heading)
            }

            hashMap.put(EventConstant.BUCKETPOSITION_EPROPERTY, "" + parentPosition)
            hashMap.put(EventConstant.PROGRAMMINGTYPE_EPROPERTY, "" + childPosition)
            hashMap.put(EventConstant.BUCKETID_EPROPERTY, "" + parent.id)

            if (!feature.equals("Games")) {
                hashMap.put(EventConstant.ARTWORKTYPE_EPROPERTY,feature)

                hashMap.put(
                    EventConstant.ARTISTNAME_EPROPERTY,
                    "" + parent.items?.get(childPosition)?.data?.misc?.sArtist?.size
                )
                hashMap.put(EventConstant.EXTRA_EPROPERTY, "")
                hashMap.put(
                    EventConstant.PLAYLISTNAME_EPROPERTY,
                    "" + parent.items?.get(childPosition)?.data?.title
                )
            }else {
                hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + parent?.heading)
            }

            var newContentId = parent.items?.get(childPosition)?.data?.id!!
            var contentIdData: String
            if (newContentId.contains("playlist-")) {
                contentIdData = newContentId.replace("playlist-", "")
            } else {
                contentIdData = newContentId.replace("artist-", "")
            }
            hashMap.put(EventConstant.CONTENTID_EPROPERTY, "123" + contentIdData)

            hashMap.put(EventConstant.POSITIONWITHINBUCKET_EPROPERTY, "" + childPosition)

            if (parent.heading?.contains("Good", true)!!) {
                hashMap.put(
                    EventConstant.BUCKETTYPE_EPROPERTY,
                    EventConstant.CONTINUE_WATCHING_NAME
                )
            } else {
                hashMap.put(EventConstant.BUCKETTYPE_EPROPERTY, "" + parent?.heading)
            }
            hashMap.put(
                EventConstant.SOURCEBUCKET_EPROPERTY, "" + childPosition
            )
            hashMap.put(
                EventConstant.SOURCEPAGE_EPROPERTY,
                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + Utils.getContentTypeDetailName(
                    "" + parent.items?.get(childPosition)?.data?.type
                )
            )

            if (feature.equals("Games")) {
                hashMap.remove(EventConstant.SOURCEPAGE_EPROPERTY)
                hashMap.remove(EventConstant.SOURCEBUCKET_EPROPERTY)
                hashMap.remove(EventConstant.PROGRAMMINGTYPE_EPROPERTY)
                hashMap.remove(EventConstant.BUCKETPOSITION_EPROPERTY)
                hashMap.remove(EventConstant.TOPNAVPOSITION_EPROPERTY)
                hashMap.remove(EventConstant.FILTERSELECTED_EPROPERTY)
                hashMap.remove(EventConstant.BUCKETTYPE_EPROPERTY)
                hashMap.remove(EventConstant.TOPNAVNAME_EPROPERTY)
            }

            EventManager.getInstance().sendEvent(ArtworkTappedEvent(hashMap))

            setEventModelDataAppLevel("" + parent.items?.get(childPosition)?.data?.id!!,"" + parent.items?.get(childPosition)?.data?.title!!,parent?.heading!!)
        }

    }

    public fun setEventModelDataAppLevel(
        contentID: String,
        songName: String,
        bucketName: String
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            var eventModel: EventModel? = HungamaMusicApp?.getInstance()?.getEventData(contentID!!)
            if (eventModel == null || TextUtils.isEmpty(eventModel?.contentID)) {
                eventModel = EventModel()
            }

            eventModel?.contentID = "" + contentID

            eventModel?.songName = "" + songName

            eventModel?.bucketName = "" + bucketName

            if(eventModel?.bucketName?.contains("Good",true) == true){
                eventModel?.bucketName=EventConstant.CONTINUE_WATCHING_NAME
            }
            eventModel?.connectionType = ConnectionUtil(activity).networkType

            var source = ""
            if (!TextUtils.isEmpty(MainActivity.lastItemClicked)){
                source = MainActivity.lastItemClicked
            }

            if (!TextUtils.isEmpty(MainActivity.headerItemName)){
                if (!TextUtils.isEmpty(source)){
                    source += "_"+MainActivity.headerItemName
                }else{
                    source = MainActivity.headerItemName
                }
            }

            if (!TextUtils.isEmpty(eventModel.bucketName)){
                if (!TextUtils.isEmpty(source)){
                    source += "_"+ eventModel.bucketName
                }else{
                    source = eventModel.bucketName
                }
            }

            eventModel.sourceName = source



            val userSubscriptionDetail =
                SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
            if (userSubscriptionDetail != null) {
                eventModel?.subscriptionStatus =
                    "" + userSubscriptionDetail?.data?.user?.userMembershipType
            }
            HungamaMusicApp.getInstance().setEventData(eventModel.contentID, eventModel)

            setLog(TAG, "setEventModelDataAppLevel: eventModel:"+eventModel?.toString())
            setLog(TAG, "setEventModelDataAppLevel: eventModel:" + eventModel)
        }

    }

    fun onItemDetailPageRedirection(parent: RowsItem, parentPosition: Int, childPosition: Int, parentTitle:String) {

        val deeplink_url = parent?.items?.get(childPosition)?.data?.deeplink_url
        val type = parent?.items?.get(childPosition)?.data?.type
        val image = parent?.items?.get(childPosition)?.data?.image
        val id = parent?.items?.get(childPosition)?.data?.id
        val title = parent?.items?.get(childPosition)?.data?.title
        val varientType = parent?.items?.get(childPosition)?.data?.variant
        setLog("DataTape","Type${type}")
        CoroutineScope(Dispatchers.IO).launch {
            callPageViewEvent(
                "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + parentTitle,
                "" + parentPosition,
                Utils.getContentTypeName("" + type),
                title!!,
                "" + varientType,
                "" + id,
                title
            )
        }

        setLog("PageViewEventCheck", MainActivity.lastItemClicked + " " +
                MainActivity.headerItemName + " " + MainActivity.tempLastItemClicked + "\n" +
                parentPosition + " " + type + " " + title + " " +varientType + " " + id + " " +  parentTitle)


        //type = "20"
        if (!TextUtils.isEmpty(deeplink_url)) {
            val intent =
                CommonUtils.getDeeplinkIntentData(Uri.parse(deeplink_url))
            intent.setClass(requireActivity(), MainActivity::class.java)
            activity?.startActivity(intent)
        } else if (type.equals("0", true)) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    bundle.putBoolean(defaultContentVarient, true)
                } else {
                    bundle.putBoolean(defaultContentVarient, false)
                }
            } else {
                bundle.putBoolean(defaultContentVarient, false)
            }
            val artistDetailsFragment = ArtistDetailsFragment()
            artistDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, artistDetailsFragment, false)

        } else if (type.equals("1", true)) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)

            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, albumDetailFragment, false)

        } else if (type.equals("15", true)
            || type.equals("44444", true)
            || type.equals("66666", true)
        ) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)

            val collectionDetailsFragment = CollectionDetailsFragment()
            collectionDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, collectionDetailsFragment, false)
        } else if (type.equals("19", true)) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            if (parent.items!!.get(childPosition)!!.data?.images != null && parent.items!!.get(
                    childPosition
                )!!.data?.images?.size!! > 0 && parent?.items?.get(childPosition)?.itype == 42
            ) {
                bundle.putStringArrayList(
                    "imageArray",
                    parent.items!!.get(childPosition)!!.data?.images as java.util.ArrayList<String>?
                )
            }

            if (parent.items!!.get(childPosition)!!.data?.variant_images != null && parent.items!!.get(
                    childPosition
                )!!.data?.variant_images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "variant_images",
                    parent.items!!.get(childPosition)!!.data?.variant_images as java.util.ArrayList<String>?
                )
            }


            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val chartDetailFragment = ChartDetailFragment.newInstance(varient)
            chartDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, chartDetailFragment, false)

        } else if (type.equals("20", true)) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            if (parent?.items?.get(childPosition)?.data?.misc?.artistid != null) {
                bundle.putString(
                    Constant.defaultArtistId,
                    parent?.items?.get(childPosition)?.data?.misc?.artistid
                )
            }

            bundle.putString(defaultContentPlayerType, type)
            bundle.putBoolean(defaultContentVarient, true)

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }

            /*id = "60f68ff50aa54e43e4570038"
            bundle.putString("id", id)
            bundle.putString("artistid","artist-81488978")*/

            val eventDetailFragment = EventDetailFragment()
            eventDetailFragment.arguments = bundle

//            var eventDetailFragment= EventPlayerFragment()
//            bundle.putString("eventName","Jubin Nautiyal Live_UN")
//            bundle.putString("id","62c5875f082fc2226812c471")
//            bundle.putString("liveEventUrl","https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
//            bundle.putInt("mode",1)//1-Portrait Mode, 2-Landscape Mode
//            eventDetailFragment.arguments=bundle

            addFragment(R.id.fl_container, this, eventDetailFragment, false)

        } else if (type.equals("25", true)) {

            var catKeywords=""
            val bundle = Bundle()
            setLog("basefragment","type:${type} parent.keywords:${parent.keywords}")
            if(!parent.keywords?.isNullOrEmpty()!!){
                catKeywords= parent?.keywords?.get(0).toString()
                bundle.putString(Constant.EXTRA_CATEGORY_NAME, catKeywords)

                setLog("basefragment","added type:${type} parent.keywords:${parent.keywords}")
            }

            bundle.putString(Constant.EXTRA_CATEGORY_ID, parent.items?.get(childPosition)?.data?.id!!.toString())
            bundle.putString("heading", parent.heading)
            val categoryDetailFragment = CategoryDetailFragment(
                parent.items?.get(childPosition)?.data?.id!!.toString(),
                parent?.type
            )
            categoryDetailFragment.arguments = bundle
            addFragment(R.id.fl_container, this, categoryDetailFragment, false)

            setLog("basefragment","bundle:${bundle}")
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(
                    EventConstant.TYPE_EPROPERTY,
                    "" + parent.items?.get(childPosition)?.data?.type
                )
                if(!catKeywords.isNullOrBlank()){
                    hashMap.put(
                        EventConstant.CONTENT_TYPE_EPROPERTY,
                        "" + parent?.keywords?.get(0).toString()
                    )
                }else{
                    hashMap.put(
                        EventConstant.CONTENT_TYPE_EPROPERTY,
                        "" + Utils.getContentTypeDetailName(parent.items?.get(childPosition)?.data?.type!!)
                    )

                }


                hashMap.put(
                    EventConstant.CATEGORYNAME_EPROPERTY,
                    "" + parent.items?.get(childPosition)?.data?.title
                )

                if(parent?.heading?.contains("Good",true)!!){
                    hashMap.put(
                        EventConstant.SOURCEPAGE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + EventConstant.CONTINUE_WATCHING_NAME
                    )
                }else{
                    hashMap.put(
                        EventConstant.SOURCEPAGE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + parent?.heading
                    )
                }



                EventManager.getInstance().sendEvent(CategoryClickedEvent(hashMap))
            }

        } else if ((type.equals(
                "93",
                true
            ) || type.equals(
                "4",
                true
            ) || type.equals(
                "65",
                true
            ) || type.equals("66", true))
            && (parent.keywords.isNullOrEmpty() && !(!parent.keywords.isNullOrEmpty() && (parent.keywords?.get(0).equals("movie-trailer") || parent.keywords?.get(0).equals("continue-watching"))))
        ) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)
            bundle.putBoolean(defaultContentVarient, true)

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                    bundle.putString(
                        "variant_image",
                        parent.items!!.get(childPosition)!!.data!!.variant_images?.get(0)
                    )
                }
            }
            val movieDetailsFragment = MovieV1Fragment(varient)
            movieDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, movieDetailsFragment, false)

        } else if ((type.equals(
                "96",
                true
            ) || type.equals(
                "97",
                true
            ) || type.equals(
                "98",
                true
            ) || type.equals(
                "102",
                true
            ) || type.equals("107", true))
            && (parent.keywords.isNullOrEmpty() && !(!parent.keywords.isNullOrEmpty() && (parent.keywords?.get(0).equals("movie-trailer") || parent.keywords?.get(0).equals("continue-watching"))))
        ) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            if (type.equals("98", true)){
                if (!parent.items.isNullOrEmpty() && parent.items?.get(childPosition)?.data?.misc != null
                    && !parent.items?.get(childPosition)?.data?.misc?.pid.isNullOrEmpty()){
                    bundle.putString(defaultContentId, parent.items?.get(childPosition)?.data?.misc?.pid?.get(0))
                }
            }else{
                bundle.putString(defaultContentId, id)
            }
            bundle.putParcelable("child_item", parent.items!!.get(childPosition))
            if (childPosition % 2 == 0)
                bundle.putBoolean(defaultContentVarient, true)
            else
                bundle.putBoolean(defaultContentVarient, false)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            bundle.putString(defaultContentPlayerType, type)
            val tvShowDetailsFragment = TvShowDetailsFragment(varient)
            tvShowDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, tvShowDetailsFragment, false)

        } else if (type.equals("109", true)) {

            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)
            //bundle.putParcelable("details", parent)
            //bundle.putInt("childPosition", childPosition)
            if (childPosition % 2 == 0)
                bundle.putBoolean(defaultContentVarient, true)
            else
                bundle.putBoolean(defaultContentVarient, false)


            val podcastDetailsFragment = PodcastDetailsFragment()
            podcastDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, podcastDetailsFragment, false)

        } else if (type.equals("55555", true)) {

            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)
            if (parent.items!!.get(childPosition)!!.data?.images != null && parent.items!!.get(
                    childPosition
                )!!.data?.images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "imageArray",
                    parent.items!!.get(childPosition)!!.data?.images as java.util.ArrayList<String>?
                )
            }

            if (parent.items!!.get(childPosition)!!.data?.variant_images != null && parent.items!!.get(
                    childPosition
                )!!.data?.variant_images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "variant_images",
                    parent.items!!.get(childPosition)!!.data?.variant_images as java.util.ArrayList<String>?
                )
            }

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
//            val playlistDetailFragment = PlaylistDetailFragmentDynamic(varient)
            val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
            playlistDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, playlistDetailFragment, false)

        } else if (type.equals(
                "22",
                true
            ) || type.equals(
                "53",
                true
            ) || type.equals("88888", true)
        ) {
            if (activity != null && activity is MainActivity){
                (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
            }
            val bundle = Bundle()
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)

            val videoDetailsFragment = MusicVideoDetailsFragment()
            videoDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, videoDetailsFragment, false)


        } else if (type.equals("99999", true)) {
            var varient = 1
            val myPlaylistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                override fun backPressItem(status: Boolean) {

                }

            })
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)
            myPlaylistDetailFragment.arguments = bundle
            addFragment(
                R.id.fl_container,
                this,
                myPlaylistDetailFragment,
                false
            )


        }else if (parent.itype==51) {
            var deepLink=parent?.items?.get(childPosition)?.data?.deepLink

            if(!deepLink?.isNullOrEmpty()!!){
                var intent=activity?.intent
                intent?.setClass(requireActivity(), CommonWebViewActivity::class.java)
                intent?.putExtra(Constant.EXTRA_URL,deepLink)
                activity?.startActivity(intent)
                setLog("TAG", "CommonWebViewActivity start main Activity")
            }

            setLog(TAG, "type 51 clicked")
        } else if (parent.itype == 1) {

            val storyUsersList = ArrayList<BodyDataItem>()
            val storyUser = parent.items
            var startPosition = childPosition
            storyUser?.forEachIndexed { index, bodyRowsItemsItem ->
                if(bodyRowsItemsItem?.data != null && !bodyRowsItemsItem.data?.isBrandHub!!){
                    storyUsersList.add(bodyRowsItemsItem.data!!)
                }else{
                    if (index < childPosition){
                        startPosition--
                    }
                }
            }
            setLog("BaseFragment", "onItemDetailPageRedirection-storyClick-startPosition-$startPosition")
            setLog(TAG, "onItemDetailPageRedirection is brand :${parent.items?.get(childPosition)?.data?.isBrandHub}")

            if(parent.items?.get(childPosition)?.data != null && parent.items?.get(childPosition)?.data?.isBrandHub!! && parent.items?.get(childPosition)?.data?.internal!!){
                setLog(TAG, "onItemDetailPageRedirection brand data:${parent.items?.get(childPosition)?.data}")
                val bundle = Bundle()
                bundle.putString(defaultContentImage, image)
                bundle.putString(defaultContentId, id)
                val mURL=parent.items?.get(childPosition)?.data?.deepLink+"&brandhub"
                bundle.putString(Constant.EXTRA_DEEPLINK, mURL)
//                bundle.putString(Constant.EXTRA_DEEPLINK, parent.items?.get(childPosition)?.data?.deepLink)

                val fragment = BrandHubFragment()
                fragment.arguments = bundle

                addFragment(R.id.fl_container, this, fragment, false)
            }else if(parent.items?.get(childPosition)?.data != null && parent.items?.get(childPosition)?.data?.isBrandHub!!&&!parent.items?.get(childPosition)?.data?.internal!!){
                setLog(TAG, "onItemDetailPageRedirection brand data:${parent.items?.get(childPosition)?.data}")
                val deepLink=parent.items?.get(childPosition)?.data?.deepLink
                if(!deepLink.isNullOrEmpty()){
                    val intent=activity?.intent
                    intent?.setClass(requireActivity(), CommonWebViewActivity::class.java)
                    intent?.putExtra(Constant.EXTRA_URL,deepLink)
                    activity?.startActivity(intent)
                    setLog("TAG", "CommonWebViewActivity start main Activity")
                }


            }else{
                if (activity != null){
                    val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                    if (status == Constant.pause){
                        SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                    }else{
                        SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                    }
                }
                val intent = Intent(requireActivity(), StoryDisplayActivity::class.java)
                intent.putExtra("position", startPosition)
                intent.putParcelableArrayListExtra("list", storyUsersList as ArrayList<BodyDataItem>)
                startActivityForResult(
                    intent,
                    DiscoverTabFragment.LAUNCH_STORY_DISPLAY_ACTIVITY
                )
            }


        } else if (!parent.items.isNullOrEmpty()
            && parent.items?.size!! > childPosition
            && parent.items?.get(childPosition)?.data != null
            && (parent.items?.get(childPosition)?.data?.type.equals("51", true)
                    || (!parent.keywords.isNullOrEmpty() && (parent.keywords?.get(0).equals("movie-trailer") || parent.keywords?.get(0).equals("continue-watching"))))){
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = parent.items?.get(childPosition)?.data?.id?.toString()!!
            dpm.contentTitle = parent.items?.get(childPosition)?.data?.title?.toString()!!
            dpm.planName = parent.items?.get(childPosition)?.data?.misc?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload = RestrictedDownload.valueOf(parent.items?.get(childPosition)?.data?.misc?.restricted_download!!)
            Constant.screen_name ="onItem DetailPage Redirection"

            if (CommonUtils.userCanDownloadContent(requireContext(), view, dpm, null,Constant.drawer_restricted_download)) {
                val intent =
                    Intent(requireContext(), VideoPlayerActivity::class.java)
                val serviceBundle = Bundle()
                serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                serviceBundle.putString(
                    Constant.SELECTED_CONTENT_ID,
                    parent.items?.get(childPosition)?.data?.id
                )
                val playedDuration = TimeUnit.SECONDS.toMillis(HungamaMusicApp.getInstance().getContentDuration(parent.items?.get(childPosition)?.data?.id!!)!!)
                setLog("VideoPlayerContent", "BaseFragment-playedDuration-$playedDuration")
                serviceBundle.putLong(Constant.VIDEO_START_POSITION, playedDuration)

                parent.items?.get(childPosition)?.data?.type?.toInt()
                    ?.let { serviceBundle.putInt(Constant.TYPE_ID, it) }
                intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                intent.putExtra(
                    "thumbnailImg",
                    parent.items?.get(childPosition)?.data?.image
                )
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (activity != null){
                    val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                    if (status == Constant.pause){
                        SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                    }else{
                        SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                    }
                    (activity as MainActivity).pausePlayer()
                }
                startActivity(intent)
            }
        }else if (type.equals("60", true)) {
            val bundle = Bundle()
            bundle.putString(defaultContentImage, image)
            bundle.putString(defaultContentId, id)
            bundle.putString(defaultContentPlayerType, type)

            val gameDetailFragment = GameDetailFragment()
            gameDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, gameDetailFragment, false)
        }

        else {
            Utils.showSnakbar(requireContext(),requireView(), false, "coming soon")
        }
    }

    fun callPageScrolledEvent(
        source: String,
        lastVisiableItem: String,
        fromBucket: String,
        toBucket: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.EXTRA_EPROPERTY, EventConstant.EXTRA_EPROPERTY)
            dataMap.put(EventConstant.LASTVISIBLEROWPOSITION_EPROPERTY, "" + lastVisiableItem)
            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY, source)
            if(fromBucket?.contains("good",true) == true){
                dataMap.put(EventConstant.FROMBUCKET_EPROPERTY, EventConstant.CONTINUE_WATCHING_NAME)
            }else{
                dataMap.put(EventConstant.FROMBUCKET_EPROPERTY, fromBucket!!)
            }

            if(toBucket?.contains("good",true) == true){
                dataMap.put(EventConstant.TOBUCKET_EPROPERTY, EventConstant.CONTINUE_WATCHING_NAME)
            }else{
                dataMap.put(EventConstant.TOBUCKET_EPROPERTY, toBucket)
            }
            setLog("PageType","Type${dataMap}")
            if (BaseActivity.eventManagerStreamName != EventConstant.PAGESCROLLED_ENAME){
                BaseActivity.eventManagerStreamName = EventConstant.PAGESCROLLED_ENAME
                EventManager.getInstance().sendEvent(PageScrolledEvent(dataMap))
            }
        }

    }

    fun callPageViewEvent(
        source: String,
        topNav: String,
        contentType: String,
        title: String,
        varientType: String,
        id: String, pageName:String) {
        CoroutineScope(Dispatchers.IO).async {
            val dataMap = HashMap<String, String>()

            dataMap[EventConstant.SOURCEPAGE_EPROPERTY] = source
            dataMap[EventConstant.TOPNAVPOSITION_EPROPERTY] = "" + topNav
            dataMap[EventConstant.CONTENT_TYPE_EPROPERTY] = contentType
            dataMap[EventConstant.CONTENTNAME_EPROPERTY] = title
            dataMap[EventConstant.VARIENT_EPROPERTY] = varientType
            dataMap[EventConstant.SOURCEPAGETYPE_EPROPERTY] = if (TextUtils.isEmpty(contentType)) "" else contentType.replace(" ","_").lowercase()
            setLog("pageTypeShow", if (TextUtils.isEmpty(contentType)) "" else contentType.replace(" ","_").lowercase())
            val newContentId=id
            val contentIdData:String
            if(newContentId.contains("playlist-")){
                contentIdData=newContentId.replace("playlist-","")
            }else{
                contentIdData=newContentId.replace("artist-","")
            }

            if (contentType == "Games") {
                dataMap[EventConstant.SOURCE] = MainActivity.lastItemClicked
                dataMap[EventConstant.SOURCEPAGETYPE_EPROPERTY] = "Game detail"
                dataMap[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                dataMap.remove(EventConstant.CONTENTNAME_EPROPERTY)
                dataMap.remove(EventConstant.TOPNAVPOSITION_EPROPERTY)
            }

            dataMap[EventConstant.CONTENTID_EPROPERTY] = contentIdData
            dataMap[EventConstant.PAGE_NAME_EPROPERTY] = "details_" + contentType.replace(" ","").lowercase()

            setLog("lanlgbljalghlas", dataMap.toString())

            EventManager.getInstance().sendEvent(PageViewEvent(dataMap))
        }
    }

    fun callPageViewEventForTab(sourcePageName:String, pageName:String, topNavPosition:String,
    pageType:String){
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap = HashMap<String, String>()
            dataMap[EventConstant.SOURCEPAGE_EPROPERTY] = sourcePageName
            dataMap[EventConstant.PAGE_NAME_EPROPERTY] = pageName
            dataMap[EventConstant.SOURCEPAGETYPE_EPROPERTY] = pageType
            dataMap[EventConstant.TOPNAVPOSITION_EPROPERTY] = topNavPosition

            setLog("PageViewEvent", dataMap.toString())
            EventManager.getInstance().sendEvent(PageViewEvent(dataMap))
        }
    }

    var commonThreeDotsMenuFragment: CommonThreeDotsMenuFragment? = null
    fun commonThreeDotMenuItemSetup(
        detailPageId: Int,
        onMenuItemClicked: OnMenuItemClicked? = null,
        isFavorite: Boolean = false
    ) {
        this.onMenuItemClicked = onMenuItemClicked
        commonThreeDotsMenuFragment =
            CommonThreeDotsMenuFragment.newInstance(getThreeDotMenuItemsData(detailPageId, isFavorite), this)
        setLog(
            TAG,
            "commonThreeDotMenuItemSetup: commonThreeDotsMenuFragment " + commonThreeDotsMenuFragment
        )
        if (commonThreeDotsMenuFragment != null && commonThreeDotsMenuFragment?.dialog != null && commonThreeDotsMenuFragment?.dialog?.isShowing!! && !commonThreeDotsMenuFragment?.isRemoving!!) {
            commonThreeDotsMenuFragment?.dismiss()
        } else {
            commonThreeDotsMenuFragment?.show(
                activity?.supportFragmentManager!!,
                "CommonThreeDotsMenuFragment"
            )
        }

    }

    fun eventThreeDotsMenuClick(source:String, menuTitle:String){
        CoroutineScope(Dispatchers.IO).launch {
            var hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+source)
            hashMap.put(EventConstant.ACTION_EPROPERTY, menuTitle)
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onThreeDotMenuItemClick(
        position: Int,
        commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel
    ) {
        val menuItemId = commonThreeDotsMenuItemModel.id
        val menuItemTitle = commonThreeDotsMenuItemModel.title

        setLog(TAG, "onThreeDotMenuItemClick:menuItemTitle "+menuItemTitle)
        setLog(TAG, "onThreeDotMenuItemClick:menuItemId "+menuItemId)
//        setLog(TAG, "onThreeDotMenuItemClick: menuItemTitle "+menuItemTitle)
//        setLog(TAG, "onThreeDotMenuItemClick: menuItemId "+menuItemId)
//        //Toast.makeText(requireContext(), "You click on $menuItemTitle", Toast.LENGTH_LONG).show()
//
//        setLog(TAG, "onThreeDotMenuItemClick title: ${commonThreeDotsMenuItemModel?.title}")

        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        var headTitle = ""
        if (detailPageId == ALBUM_DETAIL_PAGE) {
            val adf = AlbumDetailFragment()
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            if (albumRespModel != null) {
                if (!TextUtils.isEmpty(albumRespModel.data?.head?.data?.title)){
                    headTitle = albumRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == ARTIST_DETAIL_PAGE) {
            val artistRespModel = ArtistDetailsFragment?.artistModel
            if (artistRespModel != null){
                if (!TextUtils.isEmpty(artistRespModel?.data?.head?.data?.title)){
                    headTitle = artistRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == CHART_DETAIL_PAGE) {
            val cdf = ChartDetailFragment.newInstance(0)
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            if (playlistRespModel != null) {
                if (!TextUtils.isEmpty(playlistRespModel.data?.head?.data?.title)){
                    headTitle = playlistRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {
            val pdf = PlaylistDetailFragmentDynamic()
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            if (playlistRespModel != null) {
                if (!TextUtils.isEmpty(playlistRespModel.data?.head?.data?.title)){
                    headTitle = playlistRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == PODCAST_DETAIL_PAGE) {
            val pdf = PodcastDetailsFragment()
            val podcastRespModel = PodcastDetailsFragment.podcastRespModel
            if (podcastRespModel != null) {
                if (!TextUtils.isEmpty(podcastRespModel.data?.head?.data?.title)){
                    headTitle = podcastRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == SONG_DETAIL_PAGE) {
            val sdf = SongDetailFragment()
            val songDetailModel = SongDetailFragment.songDetailModel
            if (songDetailModel != null) {
                if (!TextUtils.isEmpty(songDetailModel.data?.head?.data?.title)){
                    headTitle = songDetailModel.data?.head?.data?.title.toString()
                }
            }
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {
            val pdf = PodcastDetailsFragment()
            val podcastRespModel = PodcastDetailsFragment.podcastRespModel
            if (podcastRespModel != null) {
                if (!TextUtils.isEmpty(podcastRespModel.data?.head?.data?.title)){
                    headTitle = podcastRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            val pdf = PlaylistDetailFragmentDynamic()
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            if (playlistRespModel != null) {
                if (!TextUtils.isEmpty(playlistRespModel.data?.head?.data?.title)){
                    headTitle = playlistRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            val adf = AlbumDetailFragment()
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            if (albumRespModel != null) {
                if (!TextUtils.isEmpty(albumRespModel.data?.head?.data?.title)){
                    headTitle = albumRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == CHART_DETAIL_ADAPTER) {
            val cdf = ChartDetailFragment.newInstance(0)
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            if (playlistRespModel != null) {
                if (!TextUtils.isEmpty(playlistRespModel.data?.head?.data?.title)){
                    headTitle = playlistRespModel.data.head.data.title
                }
            }
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_PAGE) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.AUDIO.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            if (downloadedRespModel != null) {
                headTitle = "Downloaded Songs"
            }
        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_PAGE) {
            val adf = FavoritedSongsDetailFragment()
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            if (favoritesSongsRespModel != null) {
                headTitle = "Favoeited Songs"
            }
        }
        else if(detailPageId == MY_PLAYLIST_DETAIL_PAGE){
            val myplaylistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playbleItemPosition = MyPlaylistDetailFragment.playableItemPosition
            if (myplaylistRespModel != null){
                if (!TextUtils.isEmpty(myplaylistRespModel.data?.head?.data?.title)){
                    headTitle = myplaylistRespModel.data.head.data.title
                }
            }
        } else if(detailPageId == DOWNLOAD_MENU_ITEM){
          val movielistRespModel = VideoWatchlistItemFragment.musicvideoList
            val playbleItemPosition = VideoWatchlistItemFragment.playableItemPosition
            if (movielistRespModel != null){
                if (!movielistRespModel.isNullOrEmpty() && movielistRespModel.size > playbleItemPosition) {
                    if (!TextUtils.isEmpty(movielistRespModel.get(playbleItemPosition).data?.title)) {
                        headTitle =
                            movielistRespModel.get(playbleItemPosition).data?.title.toString()
                    }
                }

            }
        }
        eventThreeDotsMenuClick(headTitle, commonThreeDotsMenuItemModel.title)
        if (menuItemId == SHUFFLE_PLAY_MENU_ITEM) {
            shufflePlayThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == SHARE_MENU_ITEM) {
            shareThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == ADD_TO_QUEUE_MENU_ITEM) {
            addToQueueThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == ADD_TO_PLAYLIST_MENU_ITEM) {
            addToPlaylistThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == SIMILAR_ALBUM_MENU_ITEM) {
        } else if (menuItemId == WATCH_WITH_FRIENDS_MENU_ITEM) {

        } else if (menuItemId == VIDEO_QUALITY_MENU_ITEM) {
            videoQualityThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == LANGUAGE_MENU_ITEM) {
            languageThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == SUBTITLE_MENU_ITEM) {
            subtitleThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == ADD_TO_THE_LIST_MENU_ITEM) {
        } else if (menuItemId == LIKE_MENU_ITEM) {
            likeThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == PLAY_NEXT_MENU_ITEM) {
            playNextThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == GO_TO_ALBUM_MENU_ITEM) {
            goToAlbumThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == SONG_DETAIL_MENU_ITEM) {
            goToSongDetailThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == DELETE_PLAYLIST_MENU_ITEM) {
            deletePlaylistThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == REMOVE_FROM_PLAYLIST_MENU_ITEM) {
          val  playlistDeleteBottomsheet = PlaylistDeleteBottomsheet(object : PlaylistDeleteBottomsheet.deleteFromPlaylist {
                    override fun deteleSong(status: Boolean) {
                        if (true) {
                                    deletePlaylistContentThreeDotMenuItem(
                                        commonThreeDotsMenuItemModel
                                    )

                            }
                        }

                })

            playlistDeleteBottomsheet.show(requireActivity().supportFragmentManager, "open")

        } else if (menuItemId == CLEAR_QUEUE_MENU_ITEM) {
            clearQueueThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == REMOVE_DOWNLOADED_CONTENT_MENU_ITEM) {
            removeDownloadedContentThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == SHARE_STORY_MENU_ITEM) {
            shareStoryThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if(menuItemId == ADD_TO_OTHER_PLAYLIST_MENU_ITEM){
            addToPlaylistThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == REMOVE_FROM_LIBRARY){
            removeFromLibraryThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if (menuItemId == REMOVE_FROM_WATCHLIST){
            removeFromLibraryThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if(menuItemId == DOWNLOAD_MENU_ITEM){
            startDownloadContentThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if(menuItemId == MAKE_PLYALIST_PRIVATE_MENU_ITEM){
            makePlaylistPrivateOrPublicThreeDotMenuItem(commonThreeDotsMenuItemModel)
        } else if(menuItemId == EDIT_PLAYLIST_MENU_ITEM){
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onEditMyPlaylistFromThreeDotMenu(true)
            }
        }
    }



    private fun shareStoryThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        if (commonThreeDotsMenuItemModel.detailPageId == MOVIE_DETAIL_PAGE) {
            val title = MovieV1Fragment.movieRespModel?.data?.head?.data?.title
            val subtitle = MovieV1Fragment.movieRespModel?.data?.head?.data?.subtitle
            val contentURL = MovieV1Fragment.movieRespModel?.data?.head?.data?.misc?.share
            val imageURL = MovieV1Fragment.movieRespModel?.data?.head?.data?.image
            val contentType = MovieV1Fragment.movieRespModel?.data?.head?.type!!
            val heading = MovieV1Fragment.movieRespModel?.data?.head?.itype.toString()

            setLog(TAG, "shareStoryThreeDotMenuItem:MOVIE_DETAIL_PAGE "+title)

            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.image = imageURL
            track.url = contentURL
            track.contentType = contentType
            track.heading = heading

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!, track)
            sheet.show(activity?.supportFragmentManager!!, "openStoryPlatformDialog")

        }
        else if(commonThreeDotsMenuItemModel.detailPageId == PODCAST_DETAIL_PAGE){
            val title = PodcastDetailsFragment.podcastRespModel?.data?.head?.data?.title
            val subtitle = "By"+" "+PodcastDetailsFragment.podcastRespModel?.data?.head?.data?.subtitle
            val contentURL = PodcastDetailsFragment.podcastRespModel?.data?.head?.data?.misc?.share
            val imageURL = PodcastDetailsFragment.podcastRespModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:PODCAST_DETAIL_PAGE "+title)

            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.image = imageURL
            track.url = contentURL
            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == PLAYLIST_DETAIL_PAGE){
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            val title = playlistRespModel?.data?.head?.data?.title
//            val subtitle = PlaylistDetailFragmentDynamic.playlistRespModel?.data?.head?.data?.subtitle
            val subtitle = getString(R.string.chart_str_8)
            val contentURL = playlistRespModel?.data?.head?.data?.misc?.share
            val imageURL = playlistRespModel?.data?.head?.data?.image
            setLog(TAG, "shareStoryThreeDotMenuItem:PLAYLIST_DETAIL_PAGE "+title)

            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.image = imageURL
            track.url = contentURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == ALBUM_DETAIL_PAGE){
            val title = AlbumDetailFragment.albumRespModel?.data?.head?.data?.title
            val subtitle = AlbumDetailFragment.albumRespModel?.data?.head?.data?.misc?.vendor
            val contentURL = AlbumDetailFragment.albumRespModel?.data?.head?.data?.misc?.share
            val imageURL = AlbumDetailFragment.albumRespModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:ALBUM_DETAIL_PAGE "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if (commonThreeDotsMenuItemModel.detailPageId == CHART_DETAIL_PAGE){
            val title = ChartDetailFragment.playlistRespModel?.data?.head?.data?.title
//            val subtitle = ChartDetailFragment.playlistRespModel?.data?.head?.data?.subtitle
            val subtitle = getString(R.string.chart_str_8)
            val contentURL = ChartDetailFragment.playlistRespModel?.data?.head?.data?.misc?.share
            val imageURL = ChartDetailFragment.playlistRespModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:CHART_DETAIL_PAGE "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if (commonThreeDotsMenuItemModel.detailPageId == MY_PLAYLIST_DETAIL_PAGE){
            val title =  MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title
            val subtitle = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.subtitle
            val contentURL = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.misc?.share
            val imageURL = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:MY_PLAYLIST_DETAIL_PAGE "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = imageURL
            track.image = contentURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")

        }
        else if(commonThreeDotsMenuItemModel.detailPageId == ARTIST_DETAIL_PAGE){
            val title = ArtistDetailsFragment.artistModel?.data?.head?.data?.title
            val subtitle = ArtistDetailsFragment.artistModel?.data?.head?.data?.subtitle
            val contentURL = ArtistDetailsFragment.artistModel?.data?.head?.data?.misc?.share
            val imageURL = ArtistDetailsFragment.artistModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:ARTIST_DETAIL_PAGE "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if (commonThreeDotsMenuItemModel.detailPageId == SONG_DETAIL_PAGE){
            val title = SongDetailFragment.songDetailModel?.data?.head?.data?.title
            val subtitle = SongDetailFragment.songDetailModel?.data?.head?.data?.subtitle
            val contentURL = SongDetailFragment.songDetailModel?.data?.head?.data?.misc?.share
            val imageURL = SongDetailFragment.songDetailModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:SONG_DETAIL_PAGE "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.image = imageURL
            track.url = contentURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER){
            val downloadRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            val title = downloadRespModel?.get(playableItemPosition)?.title
            val subtitle = downloadRespModel?.get(playableItemPosition)?.subTitle
            val contentURL = downloadRespModel?.get(playableItemPosition)?.contentShareLink
            val imageURL = downloadRespModel?.get(playableItemPosition)?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:DOWNLOADED_CONTENT_DETAIL_ADAPTER "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == DOWNLOADED_CONTENT_DETAIL_PAGE){

        }
        else if(commonThreeDotsMenuItemModel.detailPageId == PODCAST_DETAIL_ADAPTER){
            val playableItemPosition = PodcastDetailsFragment?.playableItemPosition
            val title = PodcastDetailsFragment?.podcastEpisode?.data?.title
            val subtitle = PodcastDetailsFragment?.podcastRespModel?.data?.head?.data?.title
            val contentURL = PodcastDetailsFragment?.podcastRespModel?.data?.head?.data?.misc?.share
            val imageURL = PodcastDetailsFragment?.podcastRespModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:PODCAST_DETAIL_ADAPTER "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == PLAYLIST_DETAIL_ADAPTER){
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            val title = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            //val title = PlaylistDetailFragmentDynamic?.playlistSongItem?.data?.title
//            val subtitle  = PlaylistDetailFragmentDynamic?.playlistRespModel?.data?.head?.data?.subtitle
            val subtitle  = getString(R.string.chart_str_8)
            val contentURL = PlaylistDetailFragmentDynamic?.playlistRespModel?.data?.head?.data?.misc?.share
            val imageURL = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:PLAYLIST_DETAIL_ADAPTER "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == ALBUM_DETAIL_ADAPTER){
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            val title = albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            //val title = AlbumDetailFragment?.albumSongItem?.data?.title
            val subtitle = AlbumDetailFragment?.albumRespModel?.data?.head?.data?.subtitle
            val contentURL = AlbumDetailFragment?.albumRespModel?.data?.head?.data?.misc?.share
            val imageURL = albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:ALBUM_DETAIL_ADAPTER "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == CHART_DETAIL_ADAPTER){
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            val title = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            //val title = ChartDetailFragment?.chartSongItem?.data?.title
//            val subtitle = ChartDetailFragment?.playlistRespModel?.data?.head?.data?.subtitle
            val subtitle = getString(R.string.chart_str_8)
            val contentURL = ChartDetailFragment?.playlistRespModel?.data?.head?.data?.misc?.share
            val imageURL = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:CHART_DETAIL_ADAPTER "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")

        }
        else if (commonThreeDotsMenuItemModel.detailPageId == MY_PLAYLIST_DETAIL_ADAPTER){
            val title = MyPlaylistDetailFragment?.playlistRespModel?.data?.head?.data?.title
            val subtitle = MyPlaylistDetailFragment?.playlistRespModel?.data?.head?.data?.subtitle
            val contentURL = MyPlaylistDetailFragment?.playlistRespModel?.data?.head?.data?.misc?.share
            val imageURL = MyPlaylistDetailFragment?.playlistRespModel?.data?.head?.data?.image

            setLog(TAG, "shareStoryThreeDotMenuItem:MY_PLAYLIST_DETAIL_ADAPTER "+title)


            val track = Track()
            track.title = title
            track.subTitle = subtitle
            track.url = contentURL
            track.image = imageURL

            val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!,track)
            sheet.show(activity?.supportFragmentManager!!,"openStoryPlatformDialog")
        }
        else if(commonThreeDotsMenuItemModel.detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER){
        }

        setLog(
            TAG,
            "shareStoryThreeDotMenuItem: commonThreeDotsMenuItemModel:${commonThreeDotsMenuItemModel}"
        )
    }

    private fun getThreeDotMenuItemsData(
        detailPageId: Int,
        isFavorite: Boolean
    ): ArrayList<CommonThreeDotsMenuItemModel> {
        val threeDotMenuItemsList = ArrayList<CommonThreeDotsMenuItemModel>()
        if (detailPageId == ALBUM_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
//            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SIMILAR_ALBUM_MENU_ITEM, detailPageId))
        } else if (detailPageId == ARTIST_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            setLog(TAG, "getThreeDotMenuItemsData: this is workin")
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
//                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
            //threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SIMILAR_ALBUM_MENU_ITEM, detailPageId))
        } else if (detailPageId == CHART_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
//            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == MOVIE_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(VIDEO_QUALITY_MENU_ITEM, detailPageId))
            //threeDotMenuItemsList.add(getThreeDotMenuItem(LANGUAGE_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SUBTITLE_MENU_ITEM, detailPageId))
            //threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_THE_LIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
            //threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == PODCAST_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
//            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId, isFavorite))
        } else if (detailPageId == SONG_DETAIL_PAGE) {
            //threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId, isFavorite))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SIMILAR_ALBUM_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(GO_TO_ALBUM_MENU_ITEM, detailPageId))
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {
            //threeDotMenuItemsList.add(getThreeDotMenuItem(WATCH_WITH_FRIENDS_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(VIDEO_QUALITY_MENU_ITEM, detailPageId))
            //threeDotMenuItemsList.add(getThreeDotMenuItem(LANGUAGE_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SUBTITLE_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_THE_LIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId, isFavorite))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(GO_TO_ALBUM_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SONG_DETAIL_MENU_ITEM, detailPageId))
            //threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_OTHER_PLAYLIST_MENU_ITEM,detailPageId))
        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId, isFavorite))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SONG_DETAIL_MENU_ITEM, detailPageId))
        } else if (detailPageId == CHART_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId, isFavorite))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SONG_DETAIL_MENU_ITEM, detailPageId))
        } else if (detailPageId == COLLECTION_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SIMILAR_ALBUM_MENU_ITEM, detailPageId))
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
//            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId, isFavorite))
            threeDotMenuItemsList.add(getThreeDotMenuItem(GO_TO_ALBUM_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SONG_DETAIL_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(
                getThreeDotMenuItem(
                    REMOVE_DOWNLOADED_CONTENT_MENU_ITEM,
                    detailPageId
                )
            )
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_PAGE) {
            //threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            //threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {
//            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(GO_TO_ALBUM_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SONG_DETAIL_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(REMOVE_FROM_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(REMOVE_FROM_LIBRARY, detailPageId))
        } else if (detailPageId == MY_PLAYLIST_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(EDIT_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHUFFLE_PLAY_MENU_ITEM, detailPageId))

            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
//                threeDotMenuItemsList.add(
//                    getThreeDotMenuItem(
//                        FIND_MORE_SONGS_MENU_ITEM,
//                        detailPageId
//                    )
//                )
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(MAKE_PLYALIST_PRIVATE_MENU_ITEM, detailPageId, isFavorite))
            threeDotMenuItemsList.add(getThreeDotMenuItem(DELETE_PLAYLIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == Constant.MY_PLAYLIST_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(Constant.SHARE_STORY_MENU_ITEM,detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(LIKE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_OTHER_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(GO_TO_ALBUM_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SONG_DETAIL_MENU_ITEM, detailPageId))
//            threeDotMenuItemsList.add(getThreeDotMenuItem(SIMILAR_SONGS_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(REMOVE_FROM_PLAYLIST_MENU_ITEM, detailPageId))

        } else if (detailPageId == TVSHOW_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
        } else if (detailPageId == QUEUE_MANAGER_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(CLEAR_QUEUE_MENU_ITEM, detailPageId))
        } else if (detailPageId == SIMILAR_SONG_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
        } else if (detailPageId == MUSIC_VIDEO_DETAIL_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(VIDEO_QUALITY_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(SUBTITLE_MENU_ITEM, detailPageId))
        } else if (detailPageId == DOWNLOADED_MUSIC_VIDEO_ADAPTER_PAGE) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(REMOVE_DOWNLOADED_CONTENT_MENU_ITEM, detailPageId))
        } else if (detailPageId == DOWNLOADED_PODCAST_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
                threeDotMenuItemsList.add(getThreeDotMenuItem(PLAY_NEXT_MENU_ITEM, detailPageId))
            }
            threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_PLAYLIST_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(
                getThreeDotMenuItem(
                    REMOVE_DOWNLOADED_CONTENT_MENU_ITEM,
                    detailPageId
                )
            )
        } else if (detailPageId == DOWNLOADED_PODCAST_DETAIL_PAGE) {
            if (BaseActivity.songDataList != null && BaseActivity.songDataList?.size!! > 0) {
                threeDotMenuItemsList.add(getThreeDotMenuItem(ADD_TO_QUEUE_MENU_ITEM, detailPageId))
            }
        }
        else if(detailPageId == TVSHOW_DETAIL){
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(VIDEO_QUALITY_MENU_ITEM,detailPageId))

        }else if (detailPageId == VIDEO_WATCHLIST_DETAIL_ADAPTER) {
            threeDotMenuItemsList.add(getThreeDotMenuItem(SHARE_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(DOWNLOAD_MENU_ITEM, detailPageId))
            threeDotMenuItemsList.add(getThreeDotMenuItem(REMOVE_FROM_WATCHLIST, detailPageId))
        }

        return threeDotMenuItemsList

    }

    private fun getThreeDotMenuItem(
        threeDotMenuItemId: Int,
        detailPageId: Int,
        isFavorite: Boolean = false
    ): CommonThreeDotsMenuItemModel {
        val threeDotMenuItemModel = CommonThreeDotsMenuItemModel()
        threeDotMenuItemModel.detailPageId = detailPageId
        if (threeDotMenuItemId == SHUFFLE_PLAY_MENU_ITEM) {
            threeDotMenuItemModel.id = SHUFFLE_PLAY_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_11)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_shuffle
        } else if (threeDotMenuItemId == SHARE_MENU_ITEM) {
            threeDotMenuItemModel.id = SHARE_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.popup_str_101)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_share_new
        } else if (threeDotMenuItemId == Constant.SHARE_STORY_MENU_ITEM) {
            threeDotMenuItemModel.id = Constant.SHARE_STORY_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.general_str_7)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_share_friends
        } else if (threeDotMenuItemId == ADD_TO_QUEUE_MENU_ITEM) {
            threeDotMenuItemModel.id = ADD_TO_QUEUE_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_2)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_add_to_queue
        } else if (threeDotMenuItemId == ADD_TO_PLAYLIST_MENU_ITEM) {
            threeDotMenuItemModel.id = ADD_TO_PLAYLIST_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_3)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_add_to_playlist
        } else if (threeDotMenuItemId == SIMILAR_ALBUM_MENU_ITEM) {
            threeDotMenuItemModel.id = SIMILAR_ALBUM_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_4)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_add_to_similar_album
        } else if (threeDotMenuItemId == WATCH_WITH_FRIENDS_MENU_ITEM) {
            threeDotMenuItemModel.id = WATCH_WITH_FRIENDS_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_5)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_watch_with_friends
        } else if (threeDotMenuItemId == VIDEO_QUALITY_MENU_ITEM) {
            threeDotMenuItemModel.id = VIDEO_QUALITY_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_6)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_video_quality
        } else if (threeDotMenuItemId == LANGUAGE_MENU_ITEM) {
            threeDotMenuItemModel.id = LANGUAGE_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_7)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_language
        } else if (threeDotMenuItemId == SUBTITLE_MENU_ITEM) {
            threeDotMenuItemModel.id = SUBTITLE_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_8)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_subtitle_new
        } else if (threeDotMenuItemId == ADD_TO_THE_LIST_MENU_ITEM) {
            threeDotMenuItemModel.id = ADD_TO_THE_LIST_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_9)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_add_to_wishlist
        } else if (threeDotMenuItemId == LIKE_MENU_ITEM) {
            threeDotMenuItemModel.id = LIKE_MENU_ITEM
            if (isFavorite) {
                threeDotMenuItemModel.title = getString(R.string.menu_str_22)
                threeDotMenuItemModel.isSelected = true
                threeDotMenuItemModel.icon = R.string.icon_liked
            } else {
                threeDotMenuItemModel.title = getString(R.string.menu_str_10)
                threeDotMenuItemModel.isSelected = false
                threeDotMenuItemModel.icon = R.string.icon_like
            }

        } else if (threeDotMenuItemId == PLAY_NEXT_MENU_ITEM) {
            threeDotMenuItemModel.id = PLAY_NEXT_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_12)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_play_next
        } else if (threeDotMenuItemId == GO_TO_ALBUM_MENU_ITEM) {
            threeDotMenuItemModel.id = GO_TO_ALBUM_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_13)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_album_detail
        } else if (threeDotMenuItemId == SONG_DETAIL_MENU_ITEM) {
            threeDotMenuItemModel.id = SONG_DETAIL_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_14)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_song_detail
        } else if (threeDotMenuItemId == EDIT_PLAYLIST_MENU_ITEM) {
            threeDotMenuItemModel.id = EDIT_PLAYLIST_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_15)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_edit_profile
        } else if (threeDotMenuItemId == MAKE_PLYALIST_PRIVATE_MENU_ITEM) {
            if (isFavorite) {
                threeDotMenuItemModel.id = MAKE_PLYALIST_PRIVATE_MENU_ITEM
                threeDotMenuItemModel.title = getString(R.string.menu_str_16)
                threeDotMenuItemModel.isSelected = false
                threeDotMenuItemModel.icon = R.string.icon_lock
            } else {
                threeDotMenuItemModel.id = MAKE_PLYALIST_PRIVATE_MENU_ITEM
                threeDotMenuItemModel.title = getString(R.string.make_playlist_public)
                threeDotMenuItemModel.isSelected = false
                threeDotMenuItemModel.icon = R.string.icon_lock
            }
        } else if (threeDotMenuItemId == FIND_MORE_SONGS_MENU_ITEM) {
            threeDotMenuItemModel.id = FIND_MORE_SONGS_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_17)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_search
        } else if (threeDotMenuItemId == DELETE_PLAYLIST_MENU_ITEM) {
            threeDotMenuItemModel.id = DELETE_PLAYLIST_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_18)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_delete
        } else if (threeDotMenuItemId == ADD_TO_OTHER_PLAYLIST_MENU_ITEM) {
            threeDotMenuItemModel.id = ADD_TO_OTHER_PLAYLIST_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_19)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_add_to_playlist
        } else if (threeDotMenuItemId == REMOVE_FROM_PLAYLIST_MENU_ITEM) {
            threeDotMenuItemModel.id = REMOVE_FROM_PLAYLIST_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_20)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_delete
        } else if (threeDotMenuItemId == SIMILAR_SONGS_MENU_ITEM) {
            threeDotMenuItemModel.id = SIMILAR_SONGS_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.menu_str_21)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_song_detail
        } else if (threeDotMenuItemId == CLEAR_QUEUE_MENU_ITEM) {
            threeDotMenuItemModel.id = CLEAR_QUEUE_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.music_player_str_29)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_clear_queue
        } else if (threeDotMenuItemId == REMOVE_DOWNLOADED_CONTENT_MENU_ITEM) {
            threeDotMenuItemModel.id = REMOVE_DOWNLOADED_CONTENT_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.general_str_2)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_delete
        }
        else if (threeDotMenuItemId == REMOVE_FROM_LIBRARY){
            threeDotMenuItemModel.id = REMOVE_FROM_LIBRARY
            threeDotMenuItemModel.title = getString(R.string.general_str_ll)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_delete
        }else if (threeDotMenuItemId == REMOVE_FROM_WATCHLIST){
            threeDotMenuItemModel.id = REMOVE_FROM_WATCHLIST
            threeDotMenuItemModel.title = getString(R.string.remove_from_watchlist)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_delete
        }else if (threeDotMenuItemId == DOWNLOAD_MENU_ITEM){
            threeDotMenuItemModel.id = DOWNLOAD_MENU_ITEM
            threeDotMenuItemModel.title = getString(R.string.video_player_str_1)
            threeDotMenuItemModel.isSelected = false
            threeDotMenuItemModel.icon = R.string.icon_download
        }
        return threeDotMenuItemModel
    }

    private fun shareThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {


        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + AlbumDetailFragment?.albumRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == ARTIST_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + ArtistDetailsFragment?.artistModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == CHART_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + ChartDetailFragment?.playlistRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)

        } else if (detailPageId == MOVIE_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + MovieV1Fragment.movieRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + PlaylistDetailFragmentDynamic?.playlistRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == PODCAST_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + PodcastDetailsFragment?.podcastRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == SONG_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + SongDetailFragment.songDetailModel?.data?.head?.data?.misc?.share
            shareurl += "play/"
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + TvShowDetailsFragment.tvShowDetailRespModel?.data?.head?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == TVSHOW_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + TVShowFragment.tvShowEpisode?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + PodcastDetailsFragment?.podcastEpisode?.data?.misc?.share
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + PlaylistDetailFragmentDynamic?.playlistSongItem?.data?.misc?.share
            shareurl += "play/"
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + AlbumDetailFragment?.albumSongItem?.data?.misc?.share
            shareurl += "play/"
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == CHART_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + ChartDetailFragment?.chartSongItem?.data?.misc?.share
            shareurl += "play/"
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == MY_PLAYLIST_DETAIL_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + MyPlaylistDetailFragment?.playlistRespModel?.data?.head?.data?.shareableURL
            if (MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.public == true){
                Utils.shareItem(requireActivity(), shareurl)
            }else{
                val sharePlaylistPopup = SharePlaylistDialog(object : SharePlaylistDialog.MakePlaylistPublic{
                    override fun onMakePlaylistPublic() {
                        makePlaylistPrivateOrPublicThreeDotMenuItem(commonThreeDotsMenuItemModel)
                        Utils.shareItem(requireActivity(), shareurl)
                    }
                })
                sharePlaylistPopup?.show(activity?.supportFragmentManager!!, "SharePlaylistDialog")
            }

        } else if (detailPageId == MY_PLAYLIST_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + MyPlaylistDetailFragment?.myplaylistSongItem?.data?.misc?.share
            shareurl += "play/"
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == DOWNLOADED_MUSIC_VIDEO_ADAPTER_PAGE) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + MusicVideoDownloadFragment.musicvideoList?.get(
                    MusicVideoDownloadFragment.playableItemPosition
                )?.contentShareLink
            Utils.shareItem(requireActivity(), shareurl)
        } else if (detailPageId == DOWNLOADED_PODCAST_DETAIL_ADAPTER || detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER) {
            var shareurl =
                getString(R.string.music_player_str_18) + " " + DownloadedContentDetailFragment.downloadedRespModel?.get(
                    DownloadedContentDetailFragment.playableItemPosition
                )?.contentShareLink
            Utils.shareItem(requireActivity(), shareurl)
        }
        else if(detailPageId == TVSHOW_DETAIL){
            var shareurl = getString(R.string.music_player_str_18)+ " " + TVShowFragment.tvShowEpisode?.data?.misc?.share
            Utils.shareItem(requireActivity(),shareurl)
        }else if (detailPageId == VIDEO_WATCHLIST_DETAIL_ADAPTER){
            val movielistRespModel = VideoWatchlistItemFragment.musicvideoList
            val playbleItemPosition = VideoWatchlistItemFragment.playableItemPosition
            if (!movielistRespModel.isNullOrEmpty() && movielistRespModel.size > playbleItemPosition) {
                var shareurl = getString(R.string.music_player_str_18)+ " " + movielistRespModel?.get(playbleItemPosition)?.data?.misc?.share
                Utils.shareItem(requireActivity(), shareurl)
            }
        }
    }

    private fun addToQueueThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {
            val adf = AlbumDetailFragment()
            val albumRespModel = AlbumDetailFragment.albumRespModel
            if (albumRespModel != null) {
                if (albumRespModel.data?.body?.rows!! != null && albumRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    for (i in albumRespModel.data?.body?.rows?.indices!!) {
                        trackDataList =
                            adf.setAlbumSongList(null, albumRespModel.data?.body?.rows, i)
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {
            val cdf = ChartDetailFragment.newInstance(0)
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    for (i in playlistRespModel.data?.body?.rows?.indices!!) {
                        trackDataList =
                            cdf.setPlaylistSongList(null, playlistRespModel.data?.body?.rows, i)
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {
            val pdf = PlaylistDetailFragmentDynamic()
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    for (i in playlistRespModel.data?.body?.rows?.indices!!) {
                        trackDataList =
                            pdf.setPlaylistSongList(null, playlistRespModel.data?.body?.rows, i)
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == PODCAST_DETAIL_PAGE) {
            val pdf = PodcastDetailsFragment()
            val podcastRespModel = PodcastDetailsFragment.podcastRespModel
            if (podcastRespModel != null) {
                if (podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.size!! > 0) {
                    trackDataList = arrayListOf()

                    for (i in podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.indices!!) {
                        trackDataList = pdf.setPodcastEpisodeList(
                            null,
                            podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks,
                            i
                        )
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == SONG_DETAIL_PAGE) {
            val sdf = SongDetailFragment()
            val songDetailModel = SongDetailFragment.songDetailModel
            if (songDetailModel != null) {
                if (songDetailModel.data?.head != null) {
                    val playableContentModel = PlayableContentModel()
                    playableContentModel.data?.head?.headData?.id =
                        songDetailModel.data.head.data.id
                    playableContentModel.data?.head?.headData?.title =
                        songDetailModel.data?.head?.data?.title!!
                    playableContentModel.data?.head?.headData?.subtitle =
                        songDetailModel.data?.head?.data?.subtitle!!
                    playableContentModel.data?.head?.headData?.image =
                        songDetailModel.data?.head?.data?.image!!
                    playableContentModel.data?.head?.headData?.misc?.pid =
                        songDetailModel.data.head.data.misc.pid
                    playableContentModel.data?.head?.headData?.misc?.pName =
                        songDetailModel.data.head.data.misc.p_name
                    playableContentModel.data?.head?.headData?.misc?.url = ""
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url = ""
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token = ""
                    playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link = ""
                    playableContentModel.data?.head?.headData?.type =
                        songDetailModel.data.head.data.type
                    trackDataList = arrayListOf()
                    trackDataList = sdf.setSongList(playableContentModel)
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {
            val pdf = PodcastDetailsFragment()
            val podcastRespModel = PodcastDetailsFragment.podcastRespModel
            var playableItemPosition = PodcastDetailsFragment.playableItemPosition
            if (podcastRespModel != null) {
                if (podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = pdf.setPodcastEpisodeList(
                        null,
                        podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks,
                        playableItemPosition
                    )
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            val pdf = PlaylistDetailFragmentDynamic()
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = pdf.setPlaylistSongList(
                        null,
                        playlistRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        }
            else if(detailPageId == MY_PLAYLIST_DETAIL_ADAPTER) {
                setLog(TAG, "addToQueueThreeDotMenuItem: working ")
                val mpdf = MyPlaylistDetailFragment(1,
                    object : MyPlaylistDetailFragment.onBackPreesHendel {
                        override fun backPressItem(status: Boolean) {

                        }
                    })
                val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
                val playbleItemPosition = MyPlaylistDetailFragment.playableItemPosition
                if (playlistRespModel != null) {
                    trackDataList = arrayListOf()
                    trackDataList = mpdf.setPlaylistSongList(
                        null,
                        playlistRespModel.data?.body?.rows,
                        playbleItemPosition
                    )
                    setLog(TAG, "addToQueueThreeDotMenuItem: trackDataList " + trackDataList)
                }
                val intent = Intent(context, AudioPlayerService::class.java)
                intent.apply {
                    action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                    putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                    putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                }
                context?.let {
                    Util.startForegroundService(it, intent)
                }
            }
         else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            val adf = AlbumDetailFragment()
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            if (albumRespModel != null) {
                if (albumRespModel.data?.body?.rows!! != null && albumRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = adf.setAlbumSongList(
                        null,
                        albumRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == CHART_DETAIL_ADAPTER) {
            val cdf = ChartDetailFragment.newInstance(0)
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = cdf.setPlaylistSongList(
                        null,
                        playlistRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.AUDIO.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            if (downloadedRespModel != null) {
                if (downloadedRespModel != null && downloadedRespModel?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    val freeSongLimit = CommonUtils.getMaxDownloadContentSize(requireContext())
                    if (CommonUtils.isUserHasGoldSubscription() || (downloadedRespModel.get(playableItemPosition).restrictedDownload != RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT.value && playableItemPosition < freeSongLimit)) {
                        trackDataList =
                            cdf.setDownloadedContentList(downloadedRespModel.get(playableItemPosition))
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                            putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                            putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                        val messageModel = MessageModel(getString(R.string.toast_str_40), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }else{
                        CommonUtils.openSubscriptionDialogPopup(
                            requireContext(),
                            PlanNames.SVOD.name,
                            "",
                            true,
                            null,
                            "",
                            null,Constant.drawer_restricted_download
                        )
                    }

                }
            }
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_PAGE) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.AUDIO.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            if (downloadedRespModel != null) {
                if (downloadedRespModel != null && downloadedRespModel?.size!! > 0) {
                    trackDataList = arrayListOf()
                    for (i in downloadedRespModel?.indices!!) {
                        trackDataList = cdf.setDownloadedContentList(downloadedRespModel.get(i))
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                    val messageModel = MessageModel(getString(R.string.toast_str_40), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
            }
        }else if (detailPageId == MY_PLAYLIST_DETAIL_PAGE){
            val fcdp = MyPlaylistDetailFragment(1,object:MyPlaylistDetailFragment.onBackPreesHendel{
                override fun backPressItem(status: Boolean) {

                }
            })
            val favoriteRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            if (favoriteRespModel != null){
                if (favoriteRespModel?.data?.body?.rows!! != null && favoriteRespModel.data.body.rows.size >= playableItemPosition){
                    trackDataList = arrayListOf()
                    trackDataList = fcdp.setPlaylistSongList(null,favoriteRespModel.data?.body?.rows,playableItemPosition)
                    val intent = Intent(context,AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST,true)
                        putExtra(Constant.TRACKS_LIST,ArrayList(trackDataList))
                    }
                    context?.let {  Util.startForegroundService(it, intent) }
                }
            }
        }
        else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {
            val adf = FavoritedSongsDetailFragment()
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            if (favoritesSongsRespModel != null) {
                if (favoritesSongsRespModel.data?.body?.rows!! != null && favoritesSongsRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = adf.setFavoritedSongList(
                        null,
                        favoritesSongsRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            if (downloadedRespModel != null) {
                if (downloadedRespModel != null && downloadedRespModel?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList =
                        cdf.setDownloadedContentList(downloadedRespModel.get(playableItemPosition))
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        } else if (detailPageId == DOWNLOADED_PODCAST_DETAIL_PAGE) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            if (downloadedRespModel != null) {
                if (downloadedRespModel != null && downloadedRespModel?.size!! > 0) {
                    trackDataList = arrayListOf()
                    for (i in downloadedRespModel?.indices!!) {
                        trackDataList = cdf.setDownloadedContentList(downloadedRespModel.get(i))
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        }
        else if (detailPageId == FAVORITED_CONTENT_DETAIL_PAGE) {
            val adf = FavoritedSongsDetailFragment()
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            if (favoritesSongsRespModel != null) {
                if (favoritesSongsRespModel.data?.body?.rows!! != null && favoritesSongsRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    for (i in favoritesSongsRespModel.data?.body?.rows?.indices!!) {
                        trackDataList = adf.setFavoritedSongList(
                            null,
                            favoritesSongsRespModel.data?.body?.rows,
                            i
                        )
                    }
                    val intent = Intent(context, AudioPlayerService::class.java)
                    intent.apply {
                        action = AudioPlayerService.PlaybackControls.ADD_TO_QUEUE.name
                        putExtra(Constant.IS_QUEUE_TRACK_LIST, true)
                        putExtra(Constant.TRACKS_LIST, ArrayList(trackDataList))
                    }
                    context?.let {
                        Util.startForegroundService(it, intent)
                    }
                }
            }
        }
    }

    private fun playNextThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {

        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {

        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_PAGE) {

        } else if (detailPageId == SONG_DETAIL_PAGE) {
            val sdf = SongDetailFragment()
            val songDetailRespModel = SongDetailFragment.songDetailModel
            val songDetailItemPosition = SongDetailFragment.playableItemPosition
            if (songDetailRespModel != null){
                trackDataList = arrayListOf()

                trackDataList = sdf.setSongListFromHead(songDetailRespModel)
            }
            if (trackDataList != null){
                val intent = Intent(context,AudioPlayerService::class.java)
                intent.apply {
                    action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                    putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                }
                context?.let {
                    Util.startForegroundService(it,intent)
                }
            }

        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {
            val pdf = PodcastDetailsFragment()
            val podcastRespModel = PodcastDetailsFragment.podcastRespModel
            var playableItemPosition = PodcastDetailsFragment.playableItemPosition
            if (podcastRespModel != null) {
                if (podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = pdf.setPodcastEpisodeList(
                        null,
                        podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks,
                        playableItemPosition
                    )
                    if (trackDataList != null && trackDataList.size > 0) {
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                    }
                }
            }
        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            val pdf = PlaylistDetailFragmentDynamic()
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = pdf.setPlaylistSongList(
                        null,
                        playlistRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    if (trackDataList != null && trackDataList.size > 0) {
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                    }
                }
            }
        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            val adf = AlbumDetailFragment()
            val playlistRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = adf.setAlbumSongList(
                        null,
                        playlistRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    if (trackDataList != null && trackDataList.size > 0) {
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                    }
                }
            }
        }else if (detailPageId == MY_PLAYLIST_DETAIL_ADAPTER){
            val bdf = MyPlaylistDetailFragment(1,object:MyPlaylistDetailFragment.onBackPreesHendel{
                override fun backPressItem(status: Boolean) {
                }
            })
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playbleItemPosition = MyPlaylistDetailFragment.playableItemPosition
            if (playlistRespModel != null){
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! >= playbleItemPosition){
                    trackDataList = arrayListOf()
                    trackDataList = bdf.setPlaylistSongList(null,playlistRespModel?.data?.body?.rows,playbleItemPosition)
                    //setLog("PlayNextSong", "NextSong-${trackDataList.get(0).title}")
                    if (playlistRespModel.data.body.rows!! != null && playlistRespModel.data.body.rows.size!! >= playbleItemPosition){
                        val intent = Intent(context,AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK,trackDataList.get(0))
                        }
                        context?.let{
                            Util.startForegroundService(it,intent)
                        }
                    }
                }
            }
        }else if (detailPageId == CHART_DETAIL_ADAPTER) {
            val cdf = ChartDetailFragment.newInstance(0)
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = cdf.setPlaylistSongList(
                        null,
                        playlistRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    if (trackDataList != null && trackDataList.size > 0) {
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                    }
                }
            }
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.AUDIO.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            if (downloadedRespModel != null) {
                if (downloadedRespModel != null && downloadedRespModel?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    val freeSongLimit = CommonUtils.getMaxDownloadContentSize(requireContext())
                    if (CommonUtils.isUserHasGoldSubscription() || (downloadedRespModel.get(playableItemPosition).restrictedDownload != RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT.value && playableItemPosition < freeSongLimit)) {
                        trackDataList =
                            cdf.setDownloadedContentList(downloadedRespModel.get(playableItemPosition))
                        if (trackDataList != null && trackDataList.size > 0) {
                            val intent = Intent(context, AudioPlayerService::class.java)
                            intent.apply {
                                action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                                putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                            }
                            context?.let {
                                Util.startForegroundService(it, intent)
                            }
                        }
                    }else{
                        CommonUtils.openSubscriptionDialogPopup(
                            requireContext(),
                            PlanNames.SVOD.name,
                            "",
                            true,
                            null,
                            "",
                            null,Constant.drawer_restricted_download
                        )
                    }
                }
            }
        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {
            val adf = FavoritedSongsDetailFragment()
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            if (favoritesSongsRespModel != null) {
                if (favoritesSongsRespModel.data?.body?.rows!! != null && favoritesSongsRespModel.data?.body?.rows?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList = adf.setFavoritedSongList(
                        null,
                        favoritesSongsRespModel.data?.body?.rows,
                        playableItemPosition
                    )
                    if (trackDataList != null && trackDataList.size > 0) {
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                    }
                }
            }
        } else if (detailPageId == DOWNLOADED_PODCAST_DETAIL_ADAPTER) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            if (downloadedRespModel != null) {
                if (downloadedRespModel != null && downloadedRespModel?.size!! >= playableItemPosition) {
                    trackDataList = arrayListOf()
                    trackDataList =
                        cdf.setDownloadedContentList(downloadedRespModel.get(playableItemPosition))
                    if (trackDataList != null && trackDataList.size > 0) {
                        val intent = Intent(context, AudioPlayerService::class.java)
                        intent.apply {
                            action = AudioPlayerService.PlaybackControls.PLAY_NEXT.name
                            putExtra(Constant.SELECTED_TRACK, trackDataList.get(0))
                        }
                        context?.let {
                            Util.startForegroundService(it, intent)
                        }
                    }
                }
            }
        }
    }

    private fun shufflePlayThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {
            val adf = AlbumDetailFragment()
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            if (albumRespModel != null) {
                if (albumRespModel.data?.body?.rows!! != null && albumRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    val playlistSongList: ArrayList<PlaylistModel.Data.Body.Row?> =
                        albumRespModel.data?.body?.rows as ArrayList<PlaylistModel.Data.Body.Row?>
                    playlistSongList?.shuffle()
                    for (i in playlistSongList?.indices!!) {
                        trackDataList =
                            adf.setAlbumSongList(null, albumRespModel.data?.body?.rows, i)
                    }
                    setUpShuffledPlayableContentListViewModel(trackDataList?.get(0)?.id.toString())
                }
            }
        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {
            val cdf = ChartDetailFragment.newInstance(0)
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    val playlistSongList: ArrayList<PlaylistModel.Data.Body.Row?> =
                        playlistRespModel.data?.body?.rows as ArrayList<PlaylistModel.Data.Body.Row?>
                    playlistSongList?.shuffle()
                    for (i in playlistSongList?.indices!!) {
                        trackDataList =
                            cdf.setPlaylistSongList(null, playlistRespModel.data?.body?.rows, i)
                    }
                    setUpShuffledPlayableContentListViewModel(trackDataList?.get(0)?.id.toString())
                }
            }
        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {
            val pdf = PlaylistDetailFragmentDynamic()
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            if (playlistRespModel != null) {
                if (playlistRespModel.data?.body?.rows!! != null && playlistRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    val playlistSongList: ArrayList<PlaylistModel.Data.Body.Row?> =
                        playlistRespModel.data?.body?.rows as ArrayList<PlaylistModel.Data.Body.Row?>
                    playlistSongList?.shuffle()
                    for (i in playlistSongList?.indices!!) {
                        trackDataList =
                            pdf.setPlaylistSongList(null, playlistRespModel.data?.body?.rows, i)
                    }
                    setUpShuffledPlayableContentListViewModel(trackDataList?.get(0)?.id.toString())
                }
            }
        } else if (detailPageId == PODCAST_DETAIL_PAGE) {

        } else if (detailPageId == SONG_DETAIL_PAGE) {

        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {

        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {

        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {

        } else if (detailPageId == CHART_DETAIL_ADAPTER) {

        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_PAGE) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.AUDIO.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            if (downloadedRespModel != null) {
                if (!downloadedRespModel.isNullOrEmpty()) {
                    trackDataList = arrayListOf()
                    var playlistSongList: ArrayList<DownloadedAudio> = ArrayList()
                    val freeSongLimit = CommonUtils.getMaxDownloadContentSize(requireContext())
                    if (CommonUtils.isUserHasGoldSubscription()){
                        playlistSongList = downloadedRespModel
                    }else{
                        downloadedRespModel.forEachIndexed { index, downloadedAudio ->
                            if (downloadedAudio.restrictedDownload != RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT.value && index < freeSongLimit) {
                                playlistSongList.add(downloadedAudio)
                            }
                        }
                    }
                    playlistSongList.shuffle()
                    for (i in playlistSongList.indices) {
                        trackDataList = cdf.setDownloadedContentList(playlistSongList.get(i))
                    }
                    //setUpShuffledPlayableContentListViewModel(trackDataList.get(0).id.toString())
                    setShuffledPlayableContentListData(null, true)
                }
            }
        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_PAGE) {
            val adf = FavoritedSongsDetailFragment()
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            if (favoritesSongsRespModel != null) {
                if (favoritesSongsRespModel.data?.body?.rows!! != null && favoritesSongsRespModel.data?.body?.rows?.size!! > 0) {
                    trackDataList = arrayListOf()
                    val playlistSongList: ArrayList<PlaylistModel.Data.Body.Row?> =
                        favoritesSongsRespModel.data?.body?.rows as ArrayList<PlaylistModel.Data.Body.Row?>
                    playlistSongList?.shuffle()
                    for (i in playlistSongList?.indices!!) {
                        trackDataList = adf.setFavoritedSongList(
                            null,
                            favoritesSongsRespModel.data?.body?.rows,
                            i
                        )
                    }
                    setUpShuffledPlayableContentListViewModel(trackDataList?.get(0)?.id.toString())
                }
            }
        }
        else if(detailPageId == MY_PLAYLIST_DETAIL_PAGE){
            val adf  = MyPlaylistDetailFragment(1,object :MyPlaylistDetailFragment.onBackPreesHendel{
                override fun backPressItem(status: Boolean) {

                }

            } )
            val myplaylistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playbleItemPosition = MyPlaylistDetailFragment.playableItemPosition
            if (myplaylistRespModel != null){
                if (myplaylistRespModel.data?.body?.rows!! != null && myplaylistRespModel.data?.body?.rows?.size!! > 0){
                    trackDataList = arrayListOf()
                    val myplaylistSongList : ArrayList<PlaylistModel.Data.Body.Row?> = myplaylistRespModel.data?.body?.rows as ArrayList<PlaylistModel.Data.Body.Row?>
                    myplaylistSongList?.shuffle()
                    for (i in myplaylistSongList?.indices!!){
                        trackDataList = adf.setPlaylistSongList(null,myplaylistRespModel?.data?.body?.rows,i)
                    }
                    setUpShuffledPlayableContentListViewModel(trackDataList?.get(0)?.id.toString())
                }
            }
        }
    }

    private fun goToAlbumThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {

        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {

        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_PAGE) {

        } else if (detailPageId == SONG_DETAIL_PAGE) {
            val songdetailRespModel = SongDetailFragment.songDetailModel
            val playableItemPosition = SongDetailFragment.playableItemPosition
            val bundle = Bundle()
            bundle.putString("id",""+songdetailRespModel?.data?.head?.data?.id)
            bundle.putString("image",""+songdetailRespModel?.data?.head?.data?.image)
            bundle.putString("playerType",""+songdetailRespModel?.data?.head?.data?.type)
            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle
            addFragment(R.id.fl_container,this,albumDetailFragment,false)

            setLog(TAG, "goToAlbumThreeDotMenuItem: songdetail ")


        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {

        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            val bundle = Bundle()
            val pidList =
                playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.misc?.pid
            if (!pidList.isNullOrEmpty()) {
                bundle.putString("id", "" + pidList[0])
                bundle.putString(
                    "image",
                    "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image
                )
                bundle.putString(
                    "playerType",
                    "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type
                )
                val albumDetailFragment = AlbumDetailFragment()
                albumDetailFragment.arguments = bundle
                addFragment(
                    R.id.fl_container,
                    this,
                    albumDetailFragment,
                    false
                )
            }

        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            val bundle = Bundle()
            bundle.putString(
                "id",
                "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            )
            bundle.putString(
                "image",
                "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image
            )
            bundle.putString(
                "playerType",
                "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type
            )
            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle
            addFragment(
                R.id.fl_container,
                this,
                albumDetailFragment,
                false
            )
        } else if (detailPageId == CHART_DETAIL_ADAPTER) {
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            val bundle = Bundle()
            bundle.putString("id", "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id)
            bundle.putString("image", "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image)
            bundle.putString("playerType", "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type)
            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle
            addFragment(R.id.fl_container, this, albumDetailFragment, false)
        }
        else if (detailPageId == MY_PLAYLIST_DETAIL_ADAPTER){
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            val bundle = Bundle()
            bundle.putString("id",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id)
            bundle.putString("image",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image)
            bundle.putString("playerType",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type)
            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle
            addFragment(R.id.fl_container,this,albumDetailFragment,false)
        }
        else if(detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER){
            val playlistRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            val bundle = Bundle()
            bundle.putString("id",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id)
            bundle.putString("image",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image)
            bundle.putString("playerType",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type)
            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle
            addFragment(R.id.fl_container,this,albumDetailFragment,false)
        }
        else if(detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER){
            val playlistRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            val bundle = Bundle()
            val pId = CommonUtils.getStringToArray(playlistRespModel?.get(playableItemPosition)?.pid.toString())
            setLog(TAG, "goToAlbumThreeDotMenuItem: pId"+pId)
            if (!pId.isNullOrEmpty()){
                for (i in pId){
                    bundle.putString("id",""+i)
                    if (pId.equals(i)){
                        setLog(TAG, "goToAlbumThreeDotMenuItem: "+i)
                    }
                }
                bundle.putString("image",""+playlistRespModel?.get(playableItemPosition)?.image)
                setLog(TAG, "goToAlbumThreeDotMenuItem: image"+playlistRespModel?.get(playableItemPosition)?.image)
                bundle.putString("playerType",""+playlistRespModel?.get(playableItemPosition)?.type)
                setLog(TAG, "goToAlbumThreeDotMenuItem: type "+playlistRespModel?.get(playableItemPosition)?.type)
                val albumDetailFragment = AlbumDetailFragment()
                albumDetailFragment.arguments = bundle
                addFragment(R.id.fl_container,this,albumDetailFragment,false)
            }
        }
    }

    private fun goToSongDetailThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        try {
            val detailPageId = commonThreeDotsMenuItemModel.detailPageId
            if (detailPageId == ALBUM_DETAIL_PAGE) {

            } else if (detailPageId == ARTIST_DETAIL_PAGE) {

            } else if (detailPageId == CHART_DETAIL_PAGE) {

            } else if (detailPageId == MOVIE_DETAIL_PAGE) {

            } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {

            } else if (detailPageId == PODCAST_DETAIL_PAGE) {

            } else if (detailPageId == SONG_DETAIL_PAGE) {

            } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

            } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {

            } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
                val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
                val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
                val bundle = Bundle()
                if (playlistRespModel != null && playlistRespModel?.data?.body?.rows?.size!! > playableItemPosition){
                    bundle.putString("id", "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id)
                    bundle.putString("image", "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image)
                    bundle.putString("playerType", "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type)
                    val songDetailFragment = SongDetailFragment()
                    songDetailFragment.arguments = bundle
                    addFragment(R.id.fl_container, this, songDetailFragment, false)
                }
            }else if(detailPageId == MY_PLAYLIST_DETAIL_ADAPTER ){
                val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
                val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
                val bundle = Bundle()
                if (playlistRespModel != null && playlistRespModel?.data?.body?.rows?.size!! > playableItemPosition){
                    bundle.putString("id",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id)
                    bundle.putString("image",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image)
                    bundle.putString("playerType",""+playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type)
                    val songDetailFragment = SongDetailFragment()
                    songDetailFragment.arguments = bundle
                    addFragment(R.id.fl_container,this,songDetailFragment,false)
                }
            }
            else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER){
                val playlistRespModel = DownloadedContentDetailFragment.downloadedRespModel
                val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
                val bundle = Bundle()
                if (playlistRespModel != null && playlistRespModel.size > playableItemPosition){
                    val pId = CommonUtils.getStringToArray(playlistRespModel?.get(playableItemPosition)?.pid.toString())
                    if (!pId.isNullOrEmpty()){

                        bundle.putString("id",""+pId.get(0))

                        setLog(TAG, "goToSongDetailThreeDotMenuItem: pId"+pId)
                        bundle.putString("image",""+playlistRespModel?.get(playableItemPosition)?.image)
                        setLog(TAG, "goToSongDetailThreeDotMenuItem: image "+playlistRespModel?.get(playableItemPosition)?.image)
                        bundle.putString("playerType",""+playlistRespModel?.get(playableItemPosition)?.type)
                        setLog(TAG, "goToSongDetailThreeDotMenuItem: type "+playlistRespModel?.get(playableItemPosition)?.type)
                        val songsDetailFragment = SongDetailFragment()
                        songsDetailFragment.arguments = bundle
                        setLog(TAG, "goToSongDetailThreeDotMenuItem: bundle "+bundle)
                        addFragment(R.id.fl_container,this,songsDetailFragment,false)
                    }
                }
            }
            else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
                val albumRespModel = AlbumDetailFragment.albumRespModel
                val playableItemPosition = AlbumDetailFragment.playableItemPosition
                val bundle = Bundle()
                if (albumRespModel != null && albumRespModel?.data?.body?.rows?.size!! > playableItemPosition){
                    bundle.putString(
                        "id",
                        "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
                    )
                    bundle.putString(
                        "image",
                        "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image
                    )
                    bundle.putString(
                        "playerType",
                        "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type
                    )
                    val songDetailFragment = SongDetailFragment()
                    songDetailFragment.arguments = bundle
                    addFragment(
                        R.id.fl_container,
                        this,
                        songDetailFragment,
                        false
                    )
                }
            } else if (detailPageId == CHART_DETAIL_ADAPTER) {
                val playlistRespModel = ChartDetailFragment.playlistRespModel
                val playableItemPosition = ChartDetailFragment.playableItemPosition
                val bundle = Bundle()
                if (playlistRespModel != null && playlistRespModel?.data?.body?.rows?.size!! > playableItemPosition){
                    bundle.putString(
                        "id",
                        "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
                    )
                    bundle.putString(
                        "image",
                        "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image
                    )
                    bundle.putString(
                        "playerType",
                        "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type
                    )
                    val songDetailFragment = SongDetailFragment()
                    songDetailFragment.arguments = bundle
                    addFragment(
                        R.id.fl_container,
                        this,
                        songDetailFragment,
                        false
                    )
                }
            } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {
                val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
                val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
                val bundle = Bundle()
                if (favoritesSongsRespModel?.data?.body?.rows?.size!! > playableItemPosition){
                    bundle.putString(
                        "id",
                        "" + favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
                    )
                    bundle.putString(
                        "image",
                        "" + favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.image
                    )
                    bundle.putString(
                        "playerType",
                        "" + favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type
                    )
                    val songDetailFragment = SongDetailFragment()
                    songDetailFragment.arguments = bundle
                    addFragment(
                        R.id.fl_container,
                        this,
                        songDetailFragment,
                        false
                    )
                }
            }
        }catch (e:Exception){

        }
    }

    private fun videoQualityThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == MOVIE_DETAIL_PAGE) {
            val sheet = VideoPlayBackSettingStreamQuality(
                CommonUtils.getStreamQualityDummyData(QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY),
                this
            )
            sheet.show(activity?.supportFragmentManager!!, "VideoPlayBackSettingStreamQuality")
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {
            val sheet = VideoPlayBackSettingStreamQuality(
                CommonUtils.getStreamQualityDummyData(QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY),
                this
            )
            sheet.show(activity?.supportFragmentManager!!, "VideoPlayBackSettingStreamQuality")
        } else if (detailPageId == MUSIC_VIDEO_DETAIL_PAGE) {
            val sheet = VideoPlayBackSettingStreamQuality(
                CommonUtils.getStreamQualityDummyData(QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY),
                this
            )
            sheet.show(activity?.supportFragmentManager!!, "VideoPlayBackSettingStreamQuality")
        }
        else if(detailPageId == TVSHOW_DETAIL){
            val sheet = VideoPlayBackSettingStreamQuality(
                CommonUtils.getStreamQualityDummyData(QualityAction.VIDEO_PLAYBACK_STREAM_QUALITY),
                this
            )
            sheet.show(activity?.supportFragmentManager!!, "VideoPlayBackSettingStreamQuality")
        }
    }

    private fun languageThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == MOVIE_DETAIL_PAGE) {
            var audioSubtitleSelectBottomSheetFragment: AudioSubtitleSelectBottomSheetFragment? =
                null
            val audioVideoListModel = PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
            var audioSubtitleList: ArrayList<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem> =
                ArrayList()

            audioVideoListModel.isSelected = true
            audioVideoListModel.lang = "English"
            audioVideoListModel.link = ""
            audioVideoListModel.langId = 1
            audioSubtitleList.add(audioVideoListModel)
            val audioVideoListModel2 =
                PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
            audioVideoListModel2.isSelected = false
            audioVideoListModel2.lang = "Hindi"
            audioVideoListModel2.link = ""
            audioVideoListModel2.langId = 2
            audioSubtitleList.add(audioVideoListModel2)
            val audioVideoListModel3 =
                PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
            audioVideoListModel3.isSelected = false
            audioVideoListModel3.lang = "Gujarati"
            audioVideoListModel3.link = ""
            audioVideoListModel3.langId = 3
            audioSubtitleList.add(audioVideoListModel3)
            audioSubtitleSelectBottomSheetFragment =
                AudioSubtitleSelectBottomSheetFragment(null, audioSubtitleList)
            //audioSubtitleSelectBottomSheetFragment?.addSubTitleListener(this@VideoPlayerActivity)
            audioSubtitleSelectBottomSheetFragment?.show(
                activity?.supportFragmentManager!!,
                "DemoBottomSheetFragment"
            )
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {
            var audioSubtitleSelectBottomSheetFragment: AudioSubtitleSelectBottomSheetFragment? =
                null
            val audioVideoListModel = PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
            var audioSubtitleList: ArrayList<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem> =
                ArrayList()

            audioVideoListModel.isSelected = true
            audioVideoListModel.lang = "English"
            audioVideoListModel.link = ""
            audioVideoListModel.langId = 1
            audioSubtitleList.add(audioVideoListModel)
            val audioVideoListModel2 =
                PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
            audioVideoListModel2.isSelected = false
            audioVideoListModel2.lang = "Hindi"
            audioVideoListModel2.link = ""
            audioVideoListModel2.langId = 2
            audioSubtitleList.add(audioVideoListModel2)
            val audioVideoListModel3 =
                PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem()
            audioVideoListModel3.isSelected = false
            audioVideoListModel3.lang = "Gujarati"
            audioVideoListModel3.link = ""
            audioVideoListModel3.langId = 3
            audioSubtitleList.add(audioVideoListModel3)
            audioSubtitleSelectBottomSheetFragment =
                AudioSubtitleSelectBottomSheetFragment(null, audioSubtitleList)
            //audioSubtitleSelectBottomSheetFragment?.addSubTitleListener(this@VideoPlayerActivity)
            audioSubtitleSelectBottomSheetFragment?.show(
                activity?.supportFragmentManager!!,
                "DemoBottomSheetFragment"
            )
        }
    }

    private fun subtitleThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == MOVIE_DETAIL_PAGE) {
            var subTitleSheetFragment: SubtitleSelectBottomSheetFragment? = null
            subTitleSheetFragment = SubtitleSelectBottomSheetFragment(null)
            //subTitleSheetFragment?.addSubTitleListener(this)
            subTitleSheetFragment?.show(
                activity?.supportFragmentManager!!,
                "DemoBottomSheetFragment"
            )
        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {
            var subTitleSheetFragment: SubtitleSelectBottomSheetFragment? = null
            subTitleSheetFragment = SubtitleSelectBottomSheetFragment(null)
            //subTitleSheetFragment?.addSubTitleListener(this)
            subTitleSheetFragment?.show(
                activity?.supportFragmentManager!!,
                "DemoBottomSheetFragment"
            )
        } else if (detailPageId == MUSIC_VIDEO_DETAIL_PAGE) {
            val videoListModel = MusicVideoDetailsFragment.videoListModel
            var subTitleSheetFragment: SubtitleSelectBottomSheetFragment? = null
            subTitleSheetFragment = SubtitleSelectBottomSheetFragment(videoListModel)
            //subTitleSheetFragment?.addSubTitleListener(this)
            subTitleSheetFragment?.show(
                activity?.supportFragmentManager!!,
                "DemoBottomSheetFragment"
            )
        }
    }

    private fun likeThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {

        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {

        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_PAGE) {

        } else if (detailPageId == SONG_DETAIL_PAGE) {
            val playlistRespModel = SongDetailFragment.songDetailModel
            val playlistItemPosition = SongDetailFragment.playableItemPosition
            val contentId  = playlistRespModel?.data?.head?.data?.id
            var newContentId=playlistRespModel?.data?.head?.data?.id
            setLog("gfggfgf","${newContentId?.replace(" playlist-", "")}")

            val typeId = playlistRespModel?.data?.head?.data?.type.toString()
            val isFavorite = !playlistRespModel?.data?.head?.data?.isFavorite!!
            if (onMenuItemClicked != null){
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite,playlistItemPosition)
            }
            setAddOrRemoveFavourite(contentId,typeId,isFavorite)

        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {

        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            setEventModelDataAppLevel(playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data!!, 4)
            val contentId = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val isFavorite =
                !playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.isFavorite!!
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite, playableItemPosition)
            }

            setAddOrRemoveFavourite(contentId, typeId, isFavorite)
        }
        else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER){
            val downloadRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            val contentId = downloadRespModel?.get(playableItemPosition)?.contentId
            val typeId = downloadRespModel?.get(playableItemPosition)?.type.toString()
            var favorite = false
            if (downloadRespModel?.get(playableItemPosition)?.isFavorite == 1){
                favorite = true
            }
            val isFavorite = !favorite
            AppDatabase.getInstance()?.downloadedAudio()?.updateDownloadedFavorite(isFavorite,contentId)
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite, playableItemPosition)
            }
            setAddOrRemoveFavourite(contentId, typeId, isFavorite)
        }
        else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            setEventModelDataAppLevel(albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data!!, 4)
            val contentId = albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id

            val typeId =
                albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val isFavorite =
                !albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.isFavorite!!
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite, playableItemPosition)
            }
            setAddOrRemoveFavourite(contentId, typeId, isFavorite)
        }
        else if(detailPageId == MY_PLAYLIST_DETAIL_ADAPTER){
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            if (!playlistRespModel?.data?.body?.rows.isNullOrEmpty() && playlistRespModel?.data?.body?.rows?.size!! > playableItemPosition
            ) {
                setEventModelDataAppLevel(
                    playlistRespModel.data.body.rows.get(
                        playableItemPosition
                    ).data, 4
                )
                val contentId =
                    playlistRespModel.data.body.rows.get(playableItemPosition).data.id
                val typeId =
                    playlistRespModel.data.body.rows.get(playableItemPosition).data.type.toString()
                val isFavorite =
                    !playlistRespModel.data.body.rows.get(playableItemPosition).data.isFavorite
                if (onMenuItemClicked != null) {
                    onMenuItemClicked?.onContentLikedFromThreeDotMenu(
                        isFavorite,
                        playableItemPosition
                    )
                }

                setAddOrRemoveFavourite(contentId, typeId, isFavorite)
            }

        }
        else if (detailPageId == CHART_DETAIL_ADAPTER) {
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            setEventModelDataAppLevel(playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data!!, 4)
            val contentId = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val isFavorite =
                !playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.isFavorite!!
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite, playableItemPosition)
            }

            setAddOrRemoveFavourite(contentId, typeId, isFavorite)
        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            val contentId =
                favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val isFavorite =
                !favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.isFavorite!!
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite, playableItemPosition)
            }
            setAddOrRemoveFavourite(contentId, typeId, isFavorite)
        }
    }
    private fun removeFromLibraryThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        if (commonThreeDotsMenuItemModel.detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER){
            val favoritedRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val favoritedItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            val contentId = favoritedRespModel?.data?.body?.rows?.get(favoritedItemPosition)?.data?.id
            val typeId = favoritedRespModel?.data?.body?.rows?.get(favoritedItemPosition)?.data?.type
            val isFavorite = !favoritedRespModel?.data?.body?.rows?.get(favoritedItemPosition)?.data?.isFavorite!!
            setLog(TAG, "removeFromLibraryThreeDotMenuItem: favoritedItemPosition  ${favoritedItemPosition}")
            setLog(TAG, "removeFromLibraryThreeDotMenuItem: contentId  ${contentId}")
            setLog(TAG, "removeFromLibraryThreeDotMenuItem: typeId  ${typeId}")
            if (onMenuItemClicked != null){
                onMenuItemClicked?.onContentLikedFromThreeDotMenu(isFavorite,favoritedItemPosition)
            }
            setAddOrRemoveFavourite(contentId,typeId,isFavorite)
            setLog(TAG, "removeFromLibraryThreeDotMenuItem: work")
        }else if (commonThreeDotsMenuItemModel.detailPageId == VIDEO_WATCHLIST_DETAIL_ADAPTER){
            val movielistRespModel = VideoWatchlistItemFragment.musicvideoList
            val playbleItemPosition = VideoWatchlistItemFragment.playableItemPosition
            if (!movielistRespModel.isNullOrEmpty() && movielistRespModel.size > playbleItemPosition && !TextUtils.isEmpty(movielistRespModel.get(playbleItemPosition).data.id)){
                setAddOrRemoveWatchlist(
                        movielistRespModel.get(playbleItemPosition).data.id,
                        "" + movielistRespModel.get(playbleItemPosition).data.type,
                        false,
                        Constant.MODULE_WATCHLIST)
                if (onMenuItemClicked != null){
                    onMenuItemClicked?.onContentLikedFromThreeDotMenu(false,playbleItemPosition)
                }

            }
        }
    }

    fun unFavoriteFromLibarary(){

    }

    fun addToPlaylistThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        var addToPlaylistMenuFragment: AddToPlaylistMenuFragment? = null
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == PODCAST_DETAIL_ADAPTER) {
            val playlistRespModel = PodcastDetailsFragment.podcastEpisode
            val playableItemPosition = PodcastDetailsFragment.playableItemPosition
            val contentId = "" + playlistRespModel?.data?.id
            val typeId = "" + playlistRespModel?.data?.type
            val contentName = "" + playlistRespModel?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {
            val playlistRespModel = PlaylistDetailFragmentDynamic.playlistRespModel
            val playableItemPosition = PlaylistDetailFragmentDynamic.playableItemPosition
            val contentId = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val contentName = "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {
            val albumRespModel = AlbumDetailFragment.albumRespModel
            val playableItemPosition = AlbumDetailFragment.playableItemPosition
            val contentId = albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val contentName = "" + albumRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == CHART_DETAIL_ADAPTER) {
            val playlistRespModel = ChartDetailFragment.playlistRespModel
            val playableItemPosition = ChartDetailFragment.playableItemPosition
            val contentId = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val contentName = "" + playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {
            val favoritesSongsRespModel = FavoritedSongsDetailFragment.favoritesSongsRespModel
            val playableItemPosition = FavoritedSongsDetailFragment.playableItemPosition
            val contentId =
                favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            val contentName = "" + favoritesSongsRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == MY_PLAYLIST_DETAIL_ADAPTER) {
            val respModel = MyPlaylistDetailFragment.myplaylistSongItem
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            val contentId = "" + respModel?.data?.id
            val typeId = "" + respModel?.data?.type
            val contentName = "" + respModel?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == SONG_DETAIL_PAGE) {
            val contentId = SongDetailFragment.songDetailModel?.data?.head?.data?.id
            val typeId = "" + SongDetailFragment.songDetailModel?.data?.head?.data?.type
            val contentName = "" + SongDetailFragment.songDetailModel?.data?.head?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == SIMILAR_SONG_DETAIL_ADAPTER) {
            val similarSongRespModel = SimilarSongsFragment.similarSongRespModel
            val playableItemPosition = SimilarSongsFragment.playableItemPosition
            val contentId =
                similarSongRespModel?.data?.body?.similar?.get(playableItemPosition)?.data?.id
            val typeId =
                similarSongRespModel?.data?.body?.similar?.get(playableItemPosition)?.data?.type.toString()
            val contentName = "" + similarSongRespModel?.data?.body?.similar?.get(playableItemPosition)?.data?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        } else if (detailPageId == DOWNLOADED_PODCAST_DETAIL_ADAPTER) {
            val cdf = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            val contentId =
                "" + downloadedRespModel?.get(playableItemPosition)?.contentId.toString()
            val typeId = "" + downloadedRespModel?.get(playableItemPosition)?.type.toString()
            val contentName = "" + downloadedRespModel?.get(playableItemPosition)?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        }else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER) {
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            val playableItemPosition = DownloadedContentDetailFragment.playableItemPosition
            val contentId =
                "" + downloadedRespModel?.get(playableItemPosition)?.contentId.toString()
            val typeId = "" + downloadedRespModel?.get(playableItemPosition)?.type.toString()
            val contentName = "" + downloadedRespModel?.get(playableItemPosition)?.title
            addToPlaylistMenuFragment = AddToPlaylistMenuFragment(contentId, contentName, typeId)

        }
        if (addToPlaylistMenuFragment != null) {
            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                addToPlaylistMenuFragment?.show(
                    activity?.supportFragmentManager!!,
                    "AddToPlaylistMenuFragment"
                )
            }
        }
    }

    private fun deletePlaylistThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {

        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {

        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_PAGE) {

        } else if (detailPageId == SONG_DETAIL_PAGE) {

        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {

        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {

        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {

        } else if (detailPageId == CHART_DETAIL_ADAPTER) {

        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {

        } else if (detailPageId == MY_PLAYLIST_DETAIL_PAGE) {
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val contentId = playlistRespModel?.data?.head?.data?.id
            val playlistName = playlistRespModel?.data?.head?.data?.title.toString()
            val typeId = "99999"

            if (!TextUtils.isEmpty(contentId)) {
                setDeleteMyPlaylist(contentId!!, typeId, playlistName)
            }
        } else if (detailPageId == MY_PLAYLIST_DETAIL_ADAPTER) {
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            val playlistId = playlistRespModel?.data?.head?.data?.id
            val contentId = playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.id
            val typeId =
                playlistRespModel?.data?.body?.rows?.get(playableItemPosition)?.data?.type.toString()
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onMyPlaylistContentDeletedFromThreeDotMenu(
                    true,
                    playableItemPosition
                )
            }
            if (!TextUtils.isEmpty(contentId) && !TextUtils.isEmpty(playlistId)) {
                setDeleteMyPlaylistContent(contentId!!, typeId, playlistId!!)
            }
        }
    }

    private fun deletePlaylistContentThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == ALBUM_DETAIL_PAGE) {

        } else if (detailPageId == ARTIST_DETAIL_PAGE) {

        } else if (detailPageId == CHART_DETAIL_PAGE) {

        } else if (detailPageId == MOVIE_DETAIL_PAGE) {

        } else if (detailPageId == PLAYLIST_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_PAGE) {

        } else if (detailPageId == SONG_DETAIL_PAGE) {

        } else if (detailPageId == TVSHOW_DETAIL_PAGE) {

        } else if (detailPageId == PODCAST_DETAIL_ADAPTER) {

        } else if (detailPageId == PLAYLIST_DETAIL_ADAPTER) {

        } else if (detailPageId == ALBUM_DETAIL_ADAPTER) {

        } else if (detailPageId == CHART_DETAIL_ADAPTER) {

        } else if (detailPageId == FAVORITED_CONTENT_DETAIL_ADAPTER) {

        } else if (detailPageId == MY_PLAYLIST_DETAIL_PAGE) {

        } else if (detailPageId == MY_PLAYLIST_DETAIL_ADAPTER) {
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            val playlistId = playlistRespModel?.data?.head?.data?.id
            var contentId = ""
            var typeId = ""
            if (!playlistRespModel?.data?.body?.rows.isNullOrEmpty() &&
                playlistRespModel?.data?.body?.rows?.size!! > playableItemPosition){
                contentId = playlistRespModel.data.body.rows.get(playableItemPosition).data.id
                typeId = playlistRespModel.data.body.rows.get(playableItemPosition).data.type.toString()
            }


            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onMyPlaylistContentDeletedFromThreeDotMenu(
                    true,
                    playableItemPosition
                )
            }
            if (!TextUtils.isEmpty(contentId) && !TextUtils.isEmpty(playlistId)) {
                setDeleteMyPlaylistContent(contentId!!, typeId, playlistId!!)
            }
        }
    }

    private fun clearQueueThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == QUEUE_MANAGER_DETAIL_PAGE) {
            if (onMenuItemClicked != null) {
                onMenuItemClicked?.onClearQueue(true)
            }
        }
    }

    private fun removeDownloadedContentThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == DOWNLOADED_MUSIC_VIDEO_ADAPTER_PAGE) {
            val position = MusicVideoDownloadFragment.playableItemPosition
            val musicVideoList = MusicVideoDownloadFragment.musicvideoList
            if (!musicVideoList.isNullOrEmpty() && musicVideoList.size > position) {
                if (isAdded && context != null){
                    removeDownloadedContent(musicVideoList.get(position), requireContext())
                }

                if (onMenuItemClicked != null) {
                    onMenuItemClicked?.onContentRemovedFromDownload(true, musicVideoList.get(position))
                }
            }
        } else if (detailPageId == DOWNLOADED_CONTENT_DETAIL_ADAPTER || detailPageId == DOWNLOADED_PODCAST_DETAIL_ADAPTER) {
            val position = DownloadedContentDetailFragment.playableItemPosition
            val downloadedRespModel = DownloadedContentDetailFragment.downloadedRespModel
            if (!downloadedRespModel.isNullOrEmpty() && downloadedRespModel.size > position) {
                if (isAdded && context != null){
                    removeDownloadedContent(downloadedRespModel.get(position), requireContext())
                }
                if (onMenuItemClicked != null) {
                    onMenuItemClicked?.onContentRemovedFromDownload(true, downloadedRespModel.get(position))
                }
            }
        }
    }

    private fun startDownloadContentThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == VIDEO_WATCHLIST_DETAIL_ADAPTER) {
            val movielistRespModel = VideoWatchlistItemFragment.musicvideoList
            val playbleItemPosition = VideoWatchlistItemFragment.playableItemPosition
            if (movielistRespModel != null){
                if (onMenuItemClicked != null) {
                    onMenuItemClicked?.onContentDownloadFromThreeDotMenu(playbleItemPosition)
                }
            }
        }
    }

    private fun makePlaylistPrivateOrPublicThreeDotMenuItem(commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
        val detailPageId = commonThreeDotsMenuItemModel.detailPageId
        if (detailPageId == MY_PLAYLIST_DETAIL_PAGE) {
            val playlistRespModel = MyPlaylistDetailFragment.playlistRespModel
            val playableItemPosition = MyPlaylistDetailFragment.playableItemPosition
            val playlistId = playlistRespModel?.data?.head?.data?.id
            val playlistName = playlistRespModel?.data?.head?.data?.title
            val isPublic = !playlistRespModel?.data?.head?.data?.public!!

            if (!TextUtils.isEmpty(playlistId)){
                setMakePlaylistPrivateOrPublic(playlistId.toString(), playlistName.toString(), isPublic)
                playlistRespModel?.data?.head?.data?.public = isPublic
            }
        }
    }

    override fun onUserClick(position: Int, settingType: Int) {
        saveVideoSetting(CommonUtils.getStreamQualityDummyData().get(position).title)
    }


    fun setUpShuffledPlayableContentListViewModel(id: String) {
        playableShuffledContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (isAdded && context != null && ConnectionUtil(context).isOnline) {
            playableShuffledContentViewModel.getPlayableContentList(requireContext(), id)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    //setLog(TAG, "isViewLoading $it")
                                    if (!TextUtils.isEmpty(it.data.data.head.headData.misc.url)) {
                                        setShuffledPlayableContentListData(it.data)
                                    }
                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }

    fun setShuffledPlayableContentListData(playableContentModel: PlayableContentModel?, isDownloadedContents:Boolean = false) {

        if (playableContentModel != null) {
            for (i in trackDataList?.indices!!) {
                if (playableContentModel?.data?.head?.headData?.id.equals(
                        trackDataList?.get(i)?.id.toString(),
                        true
                    )
                ) {
                    trackDataList.get(i).url = playableContentModel?.data?.head?.headData?.misc?.url
                }
            }

            BaseActivity.setTrackListData(trackDataList)
            val intent = Intent(requireActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, 0)
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            Util.startForegroundService(requireActivity(), intent)
            (activity as MainActivity).reBindService()
        }else if (isDownloadedContents){
            BaseActivity.setTrackListData(trackDataList)
            val intent = Intent(requireActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, 0)
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            Util.startForegroundService(requireActivity(), intent)
            (activity as MainActivity).reBindService()
        }
    }

    fun setAddOrRemoveFollow(contentId: String?, type: String?, isFavourite: Boolean) {
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            jsonObject.put("contentId", contentId)
            jsonObject.put("typeId", type)
            jsonObject.put("action", isFavourite)
            jsonObject.put("module", Constant.MODULE_FOLLOW)
            if (userViewModelBookmark == null) {
                userViewModelBookmark = ViewModelProvider(
                    this
                ).get(UserViewModel::class.java)
            }
            userViewModelBookmark?.callBookmarkApi(requireContext(), jsonObject.toString())

//            if (isFavourite) {
//                val messageModel = MessageModel(
//                    getString(R.string.library_playlist_str_28), getString(R.string.toast_str_47),
//                    MessageType.NEUTRAL, true
//                )
//                CommonUtils.showToast(requireContext(), messageModel)
//
//            } else {
//                val messageModel = MessageModel(
//                    getString(R.string.toast_str_48), getString(R.string.toast_str_48),
//                    MessageType.NEUTRAL, true
//                )
//                CommonUtils.showToast(requireContext(), messageModel)
//            }


        }
    }

    fun setMakePlaylistPrivateOrPublic(playlistId: String, playlistName: String, isPublic: Boolean?) {
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            jsonObject.put("uid", SharedPrefHelper.getInstance().getUserId())
            jsonObject.put("name", playlistName)
            jsonObject.put("public", isPublic)
            val playlistListViewModel: PlaylistViewModel = ViewModelProvider(
                this
            ).get(PlaylistViewModel::class.java)

            if (ConnectionUtil(context).isOnline) {
                playlistListViewModel.updatePlaylistData(requireContext(), jsonObject, playlistId)
            }
        }
    }

    public fun setEventModelDataAppLevel(playableContentModel: PlaylistModel.Data.Body.Row.Data, contentType: Int) {

        var eventModel: EventModel?=HungamaMusicApp?.getInstance()?.getEventData(playableContentModel.id)

        if(eventModel==null ||TextUtils.isEmpty(eventModel?.contentID)){
            eventModel= EventModel()
        }

        if(TextUtils.isEmpty(eventModel?.contentID)){
            eventModel?.contentID=playableContentModel.id
        }

        if(TextUtils.isEmpty(eventModel?.songName)){
            eventModel?.songName=""+playableContentModel?.title
        }
        eventModel?.name=""+playableContentModel?.title
        if(playableContentModel?.misc?.actorf!=null&&playableContentModel.misc.actorf.size>0){
//            eventModel?.actor=""+TextUtils.join(",",playableContentModel?.misc?.actorf)
            eventModel?.actor=playableContentModel.misc.actorf.toString()
        }
        setLog(TAG, "setEventModelDataAppLevel: eventModel?.actor:${eventModel?.actor}")

        if(playableContentModel?.misc?.pid!=null&&playableContentModel.misc.pid.size>0){
//            eventModel?.album_ID=""+TextUtils.join(",",playableContentModel?.misc?.pid)
            eventModel?.album_ID=playableContentModel.misc.pid.toString()
        }

        if(playableContentModel?.misc?.p_name!=null&&playableContentModel.misc.p_name.size>0){
//            eventModel?.album_name=""+TextUtils.join(",",playableContentModel?.misc?.p_name)
            eventModel?.album_name=playableContentModel.misc.p_name.toString()
        }


        if(playableContentModel?.misc?.p_name!=null&&playableContentModel.misc.p_name.size>0){
//            eventModel?.originalAlbumName=""+TextUtils.join(",",playableContentModel?.misc?.p_name)
            eventModel?.originalAlbumName=playableContentModel.misc.p_name.toString()
        }

        if(playableContentModel?.misc?.keywords!=null&&playableContentModel.misc.keywords.size>0){
//            eventModel?.keywords=""+TextUtils.join(",",playableContentModel?.misc?.keywords)
            eventModel?.keywords=playableContentModel.misc.keywords.toString()
        }

        eventModel?.critic_Rating=""+playableContentModel?.misc?.ratingCritic

        if(playableContentModel?.misc?.movierights!=null&&playableContentModel.misc.movierights.size>0){
//            eventModel?.content_Pay_Type=""+TextUtils.join(",",playableContentModel?.misc?.movierights)
            eventModel?.content_Pay_Type=playableContentModel.misc.movierights.toString()
        }

        eventModel?.release_Date=""+playableContentModel?.releasedate

        if(playableContentModel?.genre!=null&&playableContentModel.genre.size>0){
//            eventModel?.genre=""+TextUtils.join(",",playableContentModel?.genre)
            eventModel?.genre=playableContentModel.genre.toString()
        }

        if(playableContentModel?.misc?.attributeCensorRating!=null&&playableContentModel.misc.attributeCensorRating.size>0){
//            eventModel?.age_rating=""+TextUtils.join(",",playableContentModel?.misc?.attributeCensorRating)
            eventModel?.age_rating=playableContentModel.misc.attributeCensorRating.toString()
        }

        if(playableContentModel?.misc?.lyricistf!=null&&playableContentModel.misc.lyricistf.size>0){
//            eventModel?.lyricist=""+TextUtils.join(",",playableContentModel?.misc?.lyricistf)
            eventModel?.lyricist=playableContentModel.misc.lyricistf.toString()
        }

        if(playableContentModel?.misc?.musicdirectorf!=null&&playableContentModel.misc.musicdirectorf.size>0){
//            eventModel?.musicDirectorComposer=""+TextUtils.join(",",playableContentModel?.misc?.musicdirectorf)
            eventModel?.musicDirectorComposer=playableContentModel.misc.musicdirectorf.toString()
        }

        if(playableContentModel?.misc?.singerf!=null&&playableContentModel.misc.singerf.size>0){
//            eventModel?.singer=""+TextUtils.join(",",playableContentModel?.misc?.singerf)
            eventModel?.singer=playableContentModel.misc.singerf.toString()
        }
        eventModel?.mood=""+playableContentModel?.misc?.mood
        if(playableContentModel?.misc?.tempo!=null&&playableContentModel.misc.tempo.size>0){
//            eventModel?.tempo=""+TextUtils.join(",",playableContentModel?.misc?.tempo)
            eventModel?.tempo=playableContentModel.misc.tempo.toString()
        }

        if(playableContentModel?.misc?.lang!=null&&playableContentModel.misc.lang.size>0){
//            eventModel?.language=""+TextUtils.join(",",playableContentModel?.misc?.lang)
            eventModel?.language=playableContentModel.misc.lang.toString()
        }

        eventModel?.label=""+playableContentModel?.misc?.vendor
        eventModel?.label_id=""+playableContentModel?.misc?.vendorid
        eventModel?.share=""+playableContentModel?.misc?.share
        eventModel?.favCount=""+playableContentModel?.misc?.favCount
        eventModel?.f_fav_count=""+playableContentModel?.misc?.f_FavCount

        if(playableContentModel?.misc?.p_name!=null&&playableContentModel.misc.p_name.size>0){
//            eventModel?.ptype=""+""+TextUtils.join(",",playableContentModel?.misc?.p_name)
            eventModel?.ptype=playableContentModel.misc.p_name.toString()
        }

        if(playableContentModel?.misc?.pid!=null&&playableContentModel.misc.pid.size>0){
//            eventModel?.pid=""+""+TextUtils.join(",",playableContentModel?.misc?.pid)
            eventModel?.pid=playableContentModel.misc.pid.toString()
        }

        if(playableContentModel?.misc?.p_name!=null&&playableContentModel.misc.p_name.size>0){
//            eventModel?.pName=""+""+TextUtils.join(",",playableContentModel?.misc?.p_name)
            eventModel?.pName=playableContentModel.misc.p_name.toString()
        }

        val userSubscriptionDetail= SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
        if(userSubscriptionDetail!=null){
            eventModel?.subscriptionStatus=""+userSubscriptionDetail?.data?.user?.userMembershipType
        }
        setLog(TAG, "setEventModelDataAppLevel contentType: "+contentType)

        if(contentType==4){
            val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
            setLog(TAG, "setEventModelDataAppLevel userSettingRespModel:${userSettingRespModel} playableContentModel:${playableContentModel?.title}")
            if(userSettingRespModel!=null){
                if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                    eventModel?.audioQuality= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                    setLog(TAG, "setEventModelDataAppLevel userSettingRespModel: "+userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!)
                }
            }
            if(TextUtils.isEmpty(eventModel?.audioQuality)){
                eventModel?.audioQuality="Auto"
            }

            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.AUDIO_QUALITY, ""+ eventModel.audioQuality)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }else if(contentType==5){
            val userSettingVideoModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
            if(userSettingVideoModel!=null){
                if(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                    eventModel?.videoQuality= userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                }
            }

            if(TextUtils.isEmpty(eventModel?.videoQuality)){
                eventModel?.audioQuality="Auto"
            }

            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.VIDEO_DOWNLOAD_QUALITY, ""+ eventModel.videoQuality)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }

        HungamaMusicApp.getInstance().setEventData(eventModel?.contentID!!,eventModel)

        setLog(TAG, "callOfflineSongEventAnalytics eventModel sourceName:${eventModel?.sourceName}  bucketName: ${eventModel?.bucketName} downloadQueue.audioQuality:${eventModel.audioQuality}")
    }

    fun setAddOrRemoveFavourite(contentId: String?, type: String?, isFavourite: Boolean) {
        try {
            if (SharedPrefHelper.getInstance().isUserLoggedIn()){
            if (context != null && ConnectionUtil(context).isOnline) {
                setLog(TAG, "likeThreeDotMenuItem:setAddOrRemoveFavourite contentId " + contentId)
                setLog(TAG, "likeThreeDotMenuItem:setAddOrRemoveFavourite type " + type)
                setLog(
                    TAG,
                    "likeThreeDotMenuItem:setAddOrRemoveFavourite  isFavourite " + isFavourite
                )
                val jsonObject = JSONObject()
                jsonObject.put("contentId", contentId)
                jsonObject.put("typeId", type)
                jsonObject.put("action", isFavourite)
                jsonObject.put("module", Constant.MODULE_FAVORITE)
                if (userViewModelBookmark == null) {
                    userViewModelBookmark = ViewModelProvider(
                        this
                    ).get(UserViewModel::class.java)
                }
                userViewModelBookmark?.callBookmarkApi(requireContext(), jsonObject.toString())
                if (activity != null) {
                    (activity as MainActivity).updateFavoriteInCurrentPlayerList(
                        contentId.toString(),
                        isFavourite
                    )
                }
                if (isFavourite) {
                    CoroutineScope(Dispatchers.IO).launch {
                        /*val messageModel = MessageModel(
                       getString(R.string.library_playlist_str_28), getString(R.string.toast_str_47),
                       MessageType.NEUTRAL, true
                   )
                   CommonUtils.showToast(requireContext(), messageModel)*/
                        var eventModel = HungamaMusicApp.getInstance().getEventData("" + contentId)
                        val hashMap = java.util.HashMap<String, String>()
                        hashMap.put(EventConstant.ACTOR_EPROPERTY, "" + eventModel?.actor)
                        hashMap.put(EventConstant.ALBUMID_EPROPERTY, "" + eventModel?.album_ID)
                        hashMap.put(EventConstant.CATEGORY_EPROPERTY, "" + eventModel?.category)
                        hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentId)

                        val albumType = type
                        setLog(
                            TAG,
                            "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" + albumType)} albumType:${albumType}"
                        )
                        hashMap.put(
                            EventConstant.CONTENTTYPE_EPROPERTY,
                            "" + Utils.getContentTypeName("" + albumType)
                        )
                        hashMap.put(EventConstant.GENRE_EPROPERTY, "" + eventModel.genre)
                        hashMap.put(EventConstant.LANGUAGE_EPROPERTY, "" + eventModel?.language)
                        hashMap.put(EventConstant.LYRICIST_EPROPERTY, "" + eventModel?.lyricist)
                        hashMap.put(EventConstant.MOOD_EPROPERTY, "" + eventModel?.mood)
                        hashMap.put(
                            EventConstant.MUSICDIRECTOR_EPROPERTY,
                            "" + eventModel?.musicDirectorComposer
                        )
                        hashMap.put(EventConstant.NAME_EPROPERTY, "" + eventModel.name)
                        hashMap.put(
                            EventConstant.PODCASTHOST_EPROPERTY,
                            "" + eventModel.podcast_host
                        )
                        hashMap.put(EventConstant.SINGER_EPROPERTY, "" + eventModel.singer)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + eventModel.sourceName)
                        hashMap.put(EventConstant.TEMPO_EPROPERTY, "" + eventModel.tempo)
                        hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                        hashMap.put(
                            EventConstant.YEAROFRELEASE_EPROPERTY,
                            "" + DateUtils.convertDate(
                                DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                                DateUtils.DATE_YYYY,
                                eventModel?.release_Date
                            )
                        )
                        EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
                    }

                } else {
                    val messageModel = MessageModel(
                        getString(R.string.toast_str_49), getString(R.string.toast_str_49),
                        MessageType.NEUTRAL, true
                    )
                    CommonUtils.showToast(requireContext(), messageModel)
                }

             }
            }
        }catch (e:Exception){

        }
    }

    fun setAddOrRemoveWatchlist(
        contentId: String?,
        type: String?,
        isFavourite: Boolean,
        module: Int
    ) {
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            jsonObject.put("contentId", contentId)
            jsonObject.put("typeId", type)
            jsonObject.put("action", isFavourite)
            jsonObject.put("module", module)
            if (userViewModelBookmark == null) {
                userViewModelBookmark = ViewModelProvider(
                    this
                ).get(UserViewModel::class.java)
            }

            userViewModelBookmark?.callBookmarkApi(requireContext(), jsonObject.toString())

        }
    }

    fun setDeleteMyPlaylist(contentId: String, type: String?, playlistName:String) {
        val playlistListViewModel: PlaylistViewModel = ViewModelProvider(
            this
        ).get(PlaylistViewModel::class.java)

        if (context != null && ConnectionUtil(context).isOnline) {
            playlistListViewModel.deleteMyPlaylist(requireContext(), contentId)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (onMenuItemClicked != null) {
                                try {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val hashMap = HashMap<String,String>()
                                        hashMap.put(EventConstant.PLAYLIST_NAME, playlistName)
                                        hashMap.put(EventConstant.PLAYLIST_ID,contentId)
                                        EventManager.getInstance().sendEvent(PlaylistDeletedEvent(hashMap))
                                    }

                                }catch (e:Exception){

                                }

                                onMenuItemClicked?.onMyPlaylistDeletedFromThreeDotMenu(it?.data!!)
                            }
                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }
    }


    fun setDeleteMyPlaylistContent(contentId: String, type: String?, playlistId: String) {
        val playlistListViewModel: PlaylistViewModel = ViewModelProvider(
            this
        ).get(PlaylistViewModel::class.java)

        if (ConnectionUtil(context).isOnline) {
            playlistListViewModel.deleteMyPlaylistContent(requireContext(), contentId, playlistId)
        }
    }


    interface OnMenuItemClicked {
        public fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {}
        public fun onMyPlaylistDeletedFromThreeDotMenu(isDeleted: Boolean) {}
        public fun onMyPlaylistContentDeletedFromThreeDotMenu(isDeleted: Boolean, position: Int) {}
        public fun onClearQueue(isClear: Boolean) {}
        public fun onContentRemovedFromDownload(isRemoved: Boolean, content: DownloadedAudio) {}
        public fun onContentDownloadFromThreeDotMenu(position: Int){}
        public fun onEditMyPlaylistFromThreeDotMenu(isEdit:Boolean){}
    }

    interface OnUserContentOrderStatus {
        public fun onUserContentOrderStatusCheck(status: Int)
    }

    fun getContentOrderStatus(
        onUserContentOrderStatus: OnUserContentOrderStatus?,
        contentId: String
    ) {
        if (isAdded) {
            setLog("contentOrder-2", "getContentOrderStatus: Thread:${Thread.currentThread().name}")
            this.onUserContentOrderStatus = onUserContentOrderStatus
            userSubscriptionCheckViewModel = ViewModelProvider(
                this
            ).get(UserSubscriptionViewModel::class.java)
            if (ConnectionUtil(requireContext()).isOnline) {
                userSubscriptionCheckViewModel?.getUserContentOrderStatusCheck(
                    requireContext(), contentId
                )?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null && it?.data?.success!!) {
                                    if (onUserContentOrderStatus != null && it?.data?.data?.response != null
                                        && !TextUtils.isEmpty(it?.data?.data?.response?.paymentStatus)
                                    ) {
                                        val status = checkContentOrderStatus(
                                            it?.data?.data?.response?.paymentStatus!!,
                                            it?.data?.data?.response?.isContentActive!!
                                        )
                                        setLog("contentOrder-2", "status:${status.toString()} getContentOrderStatus: Thread:${Thread.currentThread().name}")
                                        onUserContentOrderStatus?.onUserContentOrderStatusCheck(
                                            status
                                        )
                                    }
                                }
                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setProgressBarVisible(false)
                                if (onUserContentOrderStatus != null) {
                                    onUserContentOrderStatus?.onUserContentOrderStatusCheck(
                                        CONTENT_ORDER_STATUS_NA
                                    )
                                }
                            }
                        }
                    })
            } else {
                if (onUserContentOrderStatus != null) {
                    onUserContentOrderStatus?.onUserContentOrderStatusCheck(CONTENT_ORDER_STATUS_NA)
                }
                val messageModel = MessageModel(
                    getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true
                )
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }


    var orderStatusContentId = ""
    fun runContentOrderStatusHandler(
        onUserContentOrderStatus: OnUserContentOrderStatus,
        contentId: String
    ) {
        setLog("contentOrder-3", contentId.toString())
        orderStatusContentId = contentId
        this.onUserContentOrderStatus = onUserContentOrderStatus
        setContentOrderStatusTimer()
    }

    private val updateContentOrderStatus = Runnable {
        getContentOrderStatus(onUserContentOrderStatus, orderStatusContentId)
    }

    private fun setContentOrderStatusTimer() {
        if (contentOrderStatusHandler != null) {
            removeContentOrderStatusTimerCallback()
            contentOrderStatusHandler?.postDelayed(
                updateContentOrderStatus,
                contentOrderStatusTimerInterval
            )
        } else {
            contentOrderStatusHandler = Handler(Looper.myLooper()!!)
            contentOrderStatusHandler?.postDelayed(
                updateContentOrderStatus,
                contentOrderStatusTimerInterval
            )
        }

    }

    fun removeContentOrderStatusTimerCallback() {
        if (contentOrderStatusHandler != null) {
            contentOrderStatusHandler?.removeCallbacks(updateContentOrderStatus)
        }
    }

    /**
     * Returns the adapter position of the first fully visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * @return The adapter position of the first fully visible item or
     * [RecyclerView.NO_POSITION] if there aren't any visible items.
     */
    open fun findFirstCompletelyVisibleItemPosition(recyclerView: RecyclerView): Int {
        val child: View = findOneVisibleChild(
            0,
            recyclerView?.layoutManager?.getChildCount()!!,
            true,
            false,
            recyclerView
        )!!
        return if (child == null) RecyclerView.NO_POSITION else recyclerView.getChildAdapterPosition(
            child
        )
    }

    /**
     * Returns the adapter position of the last fully visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * @return The adapter position of the last fully visible view or
     * [RecyclerView.NO_POSITION] if there aren't any visible items.
     */
    open fun findLastCompletelyVisibleItemPosition(recyclerView: RecyclerView): Int {
        if (recyclerView != null) {
            val child = findOneVisibleChild(
                recyclerView?.layoutManager?.getChildCount()!! - 1,
                -1,
                true,
                false,
                recyclerView
            )!!
            return if (child == null) RecyclerView.NO_POSITION else recyclerView.getChildAdapterPosition(
                child
            )
        }
        return -1
    }

    open fun findOneVisibleChild(
        fromIndex: Int, toIndex: Int, completelyVisible: Boolean,
        acceptPartiallyVisible: Boolean, recyclerView: RecyclerView
    ): View? {
        val helper: OrientationHelper
        helper = if (recyclerView?.layoutManager?.canScrollVertically()!!) {
            OrientationHelper.createVerticalHelper(recyclerView?.layoutManager!!)
        } else {
            OrientationHelper.createHorizontalHelper(recyclerView?.layoutManager!!)
        }
        val start: Int = helper.getStartAfterPadding()
        val end: Int = helper.getEndAfterPadding()
        val next = if (toIndex > fromIndex) 1 else -1
        var partiallyVisible: View? = null
        var i = fromIndex
        while (i != toIndex) {
            val child: View = recyclerView?.layoutManager?.getChildAt(i)!!
            val childStart: Int = helper.getDecoratedStart(child)
            val childEnd: Int = helper.getDecoratedEnd(child)
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child
                    }
                } else {
                    return child
                }
            }
            i += next
        }
        return partiallyVisible
    }

    private fun saveVideoSetting(title: String?) {
        var userViewModel: UserViewModel? = null
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {

            try {
                val userSettingRespModel = SharedPrefHelper.getInstance()
                    .getUserPlayBackSetting(Constant.TYPE_VIDEOPLAYBACK_SETTING)
                if (userSettingRespModel?.data != null) {
                    val mainJson = JSONObject()
                    val prefArrays = JSONArray()

                    val emailSettingJson = JSONObject()
                    val userSettingsData =
                        userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)
                    emailSettingJson.put("autoPlay", userSettingsData?.autoPlay)
                    emailSettingJson.put("streaming_quality", title)


                    prefArrays.put(emailSettingJson)
                    mainJson.put("type", Constant.TYPE_VIDEOPLAYBACK_SETTING)
                    mainJson.put("preference", prefArrays)


                    userViewModel?.saveUserPref(
                        requireContext(),
                        mainJson.toString(),
                        Constant.TYPE_VIDEOPLAYBACK_SETTING
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }



    fun addMoengageSelfHandleInAppData(homeModel: HomeModel, headItemsItem: HeadItemsItem): HomeModel =runBlocking(Dispatchers.IO) {
        if (InAppCallback.mInAppCampaignList.size > 0) {
            setLog("moengageData", Gson().toJson(InAppCallback.mInAppCampaignList))

            InAppCallback.mInAppCampaignList?.values?.forEachIndexed { index, moEInAppCampaign ->
                    try {
                        var topPos = headItemsItem?.id?.toInt()
                        setLog(
                            TAG,
                            "setMoengageData  bottom pos:${MainActivity.lastBottomItemPosClicked} ME bottom pos:${moEInAppCampaign?.bottom_nav_position} top pos:${topPos} ME top pos:${moEInAppCampaign?.top_nav_position} headItemsItem: ${headItemsItem} "
                        )
                        if (MainActivity.lastBottomItemPosClicked == moEInAppCampaign?.bottom_nav_position && topPos == moEInAppCampaign?.top_nav_position) {

                            setLog(
                                TAG,
                                "setMoengageData  set data bottom pos:${MainActivity.lastBottomItemPosClicked} ME bottom pos:${moEInAppCampaign?.bottom_nav_position} top pos:${topPos} ME top pos:${moEInAppCampaign?.top_nav_position}"
                            )

                            var adInApp = RowsItem()
                            adInApp.heading = moEInAppCampaign?.title

                            adInApp.numrow = 1
                            var items = ArrayList<BodyRowsItemsItem?>()
                            var model = BodyRowsItemsItem()

                            if (moEInAppCampaign?.templateId.equals(
                                    "" + BucketChildAdapter.ROW_ITYPE_101,
                                    true
                                )
                            ) {
                                adInApp.itype = BucketChildAdapter.ROW_ITYPE_101
                                model.itype = BucketChildAdapter.ROW_ITYPE_101
                            } else if (moEInAppCampaign?.templateId.equals(
                                    "" + BucketChildAdapter.ROW_ITYPE_102,
                                    true
                                )
                            ) {
                                adInApp.itype =
                                    com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_102
                                model.itype =
                                    com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_102
                            } else if (moEInAppCampaign?.templateId.equals(
                                    "" + BucketChildAdapter.ROW_ITYPE_103,
                                    true
                                )
                            ) {
                                adInApp.itype =
                                    com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_103
                                model.itype =
                                    com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_103
                            } else if (moEInAppCampaign?.templateId.equals(
                                    "" + BucketChildAdapter.ROW_ITYPE_104,
                                    true
                                )
                            ) {
                                adInApp.itype =
                                    com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_104
                                model.itype =
                                    com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_104
                            }

                            model.data = BodyDataItem()

                            model.data?.id = moEInAppCampaign.campaignId
                            model.data?.title = moEInAppCampaign.title
                            model.data?.subTitle = moEInAppCampaign.subTitle

                            model.data?.options = moEInAppCampaign.options
                            items.add(model)
                            adInApp.items = items


                            val userSubscriptionDetail = SharedPrefHelper.getInstance()
                                .getPayUserDetail(PrefConstant.USER_PAY_DATA)

                            setLog(
                                TAG,
                                "setMoengageData: InAppCallback display_userMembershipTypeId:${moEInAppCampaign?.display_userMembershipTypeId} userSubscriptionDetail?.data?.user?.userMembershipTypeId:${userSubscriptionDetail?.data?.user?.userMembershipTypeId}"
                            )

                            if (adInApp.itype != com.hungama.music.ui.main.adapter.BucketChildAdapter.ROW_ITYPE_104) {
                                homeModel?.data?.body?.rows?.add(
                                    moEInAppCampaign.position,
                                    adInApp
                                )
                            }
                            else {
                                if (userSubscriptionDetail != null && moEInAppCampaign?.display_userMembershipTypeId?.contains(
                                        userSubscriptionDetail?.data?.user?.userMembershipTypeId
                                    ) == true
                                ) {
                                    homeModel?.data?.body?.rows?.add(
                                        moEInAppCampaign.position,
                                        adInApp
                                    )
                                }

                            }

                        }
                        setLog("moengageData", "try ")
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }
//                }
            }


        } else {
            setLog(TAG, "setMoengageData: InAppCallback not added")
        }

//        val homeModelData = setAdsData(homeModel, headItemsItem)

        return@runBlocking homeModel
    }

    public fun refreshCurrentFragment(
        activity: FragmentActivity,
        fragment: Fragment
    ) {
        if (Utils?.getCurrentFragment(activity) != null) {
            if (!Utils?.getCurrentFragment(activity)?.javaClass?.simpleName.equals(
                    fragment.javaClass.simpleName, true
                )
            ) {
                addFragment(
                    R.id.fl_container,
                    Utils?.getCurrentFragment(activity)!!, fragment, false
                )
            } else {
                activity.supportFragmentManager.beginTransaction().remove(fragment).commit()
                activity.supportFragmentManager.popBackStack()
                addFragment(
                    R.id.fl_container,
                    Utils?.getCurrentFragment(activity)!!, fragment, false
                )
            }
        }
    }

    fun showBottomNavigationAndMiniplayer() {
        if (activity != null) {
            (activity as MainActivity).showMiniPlayer()
            (activity as MainActivity).showBottomNavigationBar()
        }
    }

    fun hideBottomNavigationAndMiniplayer() {
        if (activity != null) {
            (activity as MainActivity).hideMiniPlayer()
            (activity as MainActivity).hideBottomNavigationBar()
        }
    }

    fun setAdsData(homeModel: HomeModel, headItemsItem: HeadItemsItem?): HomeModel = runBlocking(Dispatchers.IO) {
        var isDisplayAd = CommonUtils.getHomescreenBannerAds().displayAd
        var adDisplayFirstPosition = CommonUtils.getHomescreenBannerAds().firstAdPositionAfterRows
        var adDisplayPositionFrequency =
            CommonUtils.getHomescreenBannerAds().repeatFrequencyAfterRows


        if (headItemsItem != null) {
            setLog("HomeBucketBannerAds", headItemsItem.page.toString())
            if (headItemsItem.page.toString().contains("originals")) {
                setLog("HomeBucketBannerAds", "originals")
            } else if (headItemsItem.page.toString().contains("charts")) {
                setLog("HomeBucketBannerAds", "charts")
                isDisplayAd = CommonUtils.getFirebaseConfigAdsData().chartListingScreen.displayAd
                adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().chartListingScreen.firstAdPositionAfterRows
                adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().chartListingScreen.repeatFrequencyAfterRows
            } else if (headItemsItem.page.toString().contains("podcast")) {
                setLog("HomeBucketBannerAds", "podcast")
                isDisplayAd = CommonUtils.getFirebaseConfigAdsData().podcastListingScreen.displayAd
                adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().podcastListingScreen.firstAdPositionAfterRows
                adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().podcastListingScreen.repeatFrequencyAfterRows
            } else if (headItemsItem.page.toString().contains("radio")) {
                setLog("HomeBucketBannerAds", "radio")
                isDisplayAd = CommonUtils.getFirebaseConfigAdsData().radioListingScreen.displayAd
                adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().radioListingScreen.firstAdPositionAfterRows
                adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().radioListingScreen.repeatFrequencyAfterRows
            } else if (headItemsItem.page.toString().contains("musicvideos")) {
                setLog("HomeBucketBannerAds", "musicvideos")
                isDisplayAd =
                    CommonUtils.getFirebaseConfigAdsData().musicVideoListingScreen.displayAd
                adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().musicVideoListingScreen.firstAdPositionAfterRows
                adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().musicVideoListingScreen.repeatFrequencyAfterRows
            } else if (headItemsItem.page.toString().contains("movies")) {
                setLog("HomeBucketBannerAds", "movies")
                isDisplayAd = CommonUtils.getFirebaseConfigAdsData().moviesListingScreen.displayAd
                adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().moviesListingScreen.firstAdPositionAfterRows
                adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().moviesListingScreen.repeatFrequencyAfterRows
            } else if (headItemsItem.page.toString().contains("shows")) {
                setLog("HomeBucketBannerAds", "shows")
                isDisplayAd = CommonUtils.getFirebaseConfigAdsData().tvShowsListingScreen.displayAd
                adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().tvShowsListingScreen.firstAdPositionAfterRows
                adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().tvShowsListingScreen.repeatFrequencyAfterRows
            } else if (headItemsItem.page.toString().contains("rent")) {
                setLog("HomeBucketBannerAds", "rent")
            } else if (headItemsItem.page.toString().contains("binge")) {
                setLog("HomeBucketBannerAds", "binge")
            } else if (headItemsItem.page.toString().contains("devotional")) {
                setLog("HomeBucketBannerAds", "devotional")
            } else if (headItemsItem.page.toString().contains("love")) {
                setLog("HomeBucketBannerAds", "love")
            } else if (headItemsItem.page.toString().contains("party")) {
                setLog("HomeBucketBannerAds", "party")
            } else if (headItemsItem.page.toString().contains("bhakti")) {
                setLog("HomeBucketBannerAds", "bhakti")
            } else if (headItemsItem.page.toString().contains("Cineplex")) {
                setLog("HomeBucketBannerAds", "Cineplex")
            } else if (headItemsItem.page.toString().contains("Kids")) {
                setLog("HomeBucketBannerAds", "Kids")
            } else if (headItemsItem.page.toString().contains("quicks")) {
                setLog("HomeBucketBannerAds", "quicks")
            }
        }
        if (!homeModel.data?.body?.rows.isNullOrEmpty() && CommonUtils.isHomeScreenBannerAds() && isDisplayAd) {
            setLog("HomeBucketBannerAds",  "setAdsData row size:${homeModel.data?.body?.rows?.size}")
            var adDisplayPosition = adDisplayFirstPosition
            var isFirstAds = true
            val bucketList = homeModel.data?.body?.rows!!
            var adUnitIdList=ArrayList<String>()
            setLog("HomeBucketBannerAds", "setAdsData: bottom pos:${MainActivity.lastBottomItemPosClicked} top pos:${MainActivity.headerItemPosition}")
            if(MainActivity.lastBottomItemPosClicked==0&&MainActivity.headerItemPosition==0){
                adUnitIdList = arrayListOf(
                    Constant.AD_UNIT_ID_HOME_BANNER_ID_1,
                    Constant.AD_UNIT_ID_HOME_BANNER_ID_2,
                    Constant.AD_UNIT_ID_HOME_BANNER_ID_3,
                    Constant.AD_UNIT_ID_HOME_BANNER_ID_4,
                    Constant.AD_UNIT_ID_HOME_BANNER_ID_5
                )

                setLog("HomeBucketBannerAds", "setAdsData: home banner bottom pos:${MainActivity.lastBottomItemPosClicked} top pos:${MainActivity.headerItemPosition}")
            }else if(MainActivity.lastBottomItemPosClicked==1&&MainActivity.headerItemPosition==0){
                adUnitIdList = arrayListOf(
                    Constant.AD_UNIT_ID_MUSIC_BANNER_ID_1,
                    Constant.AD_UNIT_ID_MUSIC_BANNER_ID_2,
                    Constant.AD_UNIT_ID_MUSIC_BANNER_ID_3,
                    Constant.AD_UNIT_ID_MUSIC_BANNER_ID_4,
                    Constant.AD_UNIT_ID_MUSIC_BANNER_ID_5
                )

                setLog("HomeBucketBannerAds", "setAdsData: music banner bottom pos:${MainActivity.lastBottomItemPosClicked} top pos:${MainActivity.headerItemPosition}")
            }else{
                adUnitIdList = arrayListOf(
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_1,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_2,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_3,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_4,
                    Constant.AD_UNIT_ID_OtherThanHome_BANNER_ID_5
                )

                setLog("HomeBucketBannerAds", "setAdsData: other than home banner bottom pos:${MainActivity.lastBottomItemPosClicked} top pos:${MainActivity.headerItemPosition}")
            }

            val adTotalIds = adUnitIdList.size
            var adIdCount = 0
            var i = 0
            var k = 0
            val iterator = bucketList.listIterator()



            while (iterator.hasNext()) {
                //setLog("adInserted-1", i.toString())

                if ((k > 0 && k % adDisplayPosition == 0)) {

                    if (isFirstAds) {
                        k = 0
                        isFirstAds = false
                        adDisplayPosition = adDisplayPositionFrequency
                    }

                    //setLog("adInserted-2", i.toString())
                    //setLog("adInserted", "Befor==" + homeModel.data?.body?.rows?.get(i)?.heading)
                    val adInApp = RowsItem()

                    adInApp.numrow = 1
                    val items = ArrayList<BodyRowsItemsItem?>()
                    val model = BodyRowsItemsItem()

                    adInApp.itype = BucketChildAdapter.ROW_ITYPE_201
                    model.itype = BucketChildAdapter.ROW_ITYPE_201
                    model.data = BodyDataItem()
                    model.data?.isVisible = true
                    if (adTotalIds > adIdCount) {
                        //setLog("adInserted-3", adIdCount.toString())
                        //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                        model.adUnitId = adUnitIdList.get(adIdCount)
                        adIdCount++
                    } else {
                        adIdCount = 0
                        model.adUnitId = adUnitIdList.get(adIdCount)
                        //setLog("adInserted-4", adIdCount.toString())
                        //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                        adIdCount++
                    }
                    items.add(model)
                    adInApp.items = items

                    iterator.add(adInApp)
                }
                val item = iterator.next()
                i++
                k++
            }
        }

        HungamaMusicApp.getInstance().setCacheAds("${MainActivity.lastItemClicked}_${headItemsItem?.page!!}","${MainActivity.lastItemClicked}_${headItemsItem?.page!!}")
        homeModel
    }

    fun shareContentOnFacebbok(
        context: Activity,
        fileUri: String,
        title: String,
        shareType: Constant.SHARE_FB_TYPE
    ) {

        setLog(
            TAG,
            "shareContentOnFacebbok: called with title:${title} url:${fileUri} type:${shareType}"
        )
        if (shareType.equals(Constant.SHARE_FB_TYPE.VIDEO)) {
            val video = ShareVideo.Builder()
                .setLocalUrl(Uri.parse(fileUri))
                .build()
            val content = ShareVideoContent.Builder()
                .setContentTitle(title)
                .setVideo(video)
                .setShareHashtag(
                    ShareHashtag.Builder()
                        .setHashtag("#HungamaMusic")
                        .build()
                )
                .build()
            shareDialog?.show(content);
            setLog(TAG, "shareContentOnFacebbok: VIDEO content:" + content)
        } else if (shareType.equals(Constant.SHARE_FB_TYPE.PHOTO)) {

            CoroutineScope(Dispatchers.IO).launch {

                val url = URL(fileUri!!)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                setLog(TAG, "shareContentOnFacebbok: PHOTO bitmap:" + bitmap)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        val photo = SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .build();
                        val content = SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .setShareHashtag(
                                ShareHashtag.Builder()
                                    .setHashtag("#HungamaMusic")
                                    .build()
                            )
                            .build()
                        setLog(TAG, "shareContentOnFacebbok: PHOTO content:" + content)
                        shareDialog?.show(content);
                    }

                }
            }


        } else if (shareType.equals(Constant.SHARE_FB_TYPE.LINK)) {
            val content = ShareLinkContent.Builder()
                .setQuote(title)
                .setContentUrl(Uri.parse(fileUri))
                .setShareHashtag(
                    ShareHashtag.Builder()
                        .setHashtag("#HungamaMusic")
                        .build()
                )
                .build()
            shareDialog?.show(content);
        } else if (shareType.equals(Constant.SHARE_FB_TYPE.STORY)) {
            val photo = SharePhoto.Builder().setImageUrl(Uri.parse(fileUri)).build()
            val content = ShareStoryContent.Builder()
                .setBackgroundAsset(photo)
                .setShareHashtag(
                    ShareHashtag.Builder()
                        .setHashtag("#HungamaMusic")
                        .build()
                )
                .build()
            shareDialog?.show(content);
        }

    }

    fun updateProfile() {
        try {
            setLog(com.hungama.music.ui.main.view.activity.TAG, "updateProfile tvCoinCount 1 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")

            if (ivUserPersonalImage != null) {
                if (!TextUtils.isEmpty(
                        SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE, "")
                    )
                ) {
                    ImageLoader.loadImage(
                        requireActivity()!!, ivUserPersonalImage!!,
                        SharedPrefHelper.getInstance().get(
                            PrefConstant.USER_IMAGE, ""
                        ), R.drawable.profile_icon
                    )
                    ivUserPersonalImage!!.setBackgroundColor(Color.parseColor("#000000"))
                } else {
                    ImageLoader.loadImage(
                        requireActivity()!!,
                        ivUserPersonalImage!!,
                        "",
                        R.drawable.profile_icon
                    )
                }
                if(SharedPrefHelper.getInstance().isUserLoggedIn()){

                    val userCoinDetailRespModel = SharedPrefHelper?.getInstance()?.getObjectUserCoin(
                        PrefConstant.USER_COIN
                    )
                    if (userCoinDetailRespModel != null && userCoinDetailRespModel?.actions != null) {
                        tvCoinCount?.text =
                            CommonUtils?.ratingWithSuffix("" + userCoinDetailRespModel?.actions?.get(0)?.total!!)
                    } else {
                    }
                    setLog(com.hungama.music.ui.main.view.activity.TAG, "updateProfile tvCoinCount 2 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")
                }else{
                    tvCoinCount?.text = "0"
                    setLog(com.hungama.music.ui.main.view.activity.TAG, "updateProfile tvCoinCount 3 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")
                }

                if (CommonUtils.isUserHasGoldSubscription()) {
                    ivMenuCount?.background = ContextCompat.getDrawable(
                        requireActivity()!!,
                        R.drawable.ic_round_count_bg_home_action_bar_gold
                    )
                    ivCoin?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity()!!,
                            R.drawable.bg_coin_profile_black
                        )
                    )
                    tvCoinCount?.setTextColor(ContextCompat.getColor(requireActivity()!!, R.color.colorBlack))
                } else {
                    ivMenuCount?.background = ContextCompat.getDrawable(
                        requireActivity()!!,
                        R.drawable.ic_round_count_bg_home_action_bar
                    )
                    ivCoin?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity()!!,
                            R.drawable.bg_coin_profile
                        )
                    )
                    tvCoinCount?.setTextColor(ContextCompat.getColor(requireActivity()!!, R.color.colorWhite))
                }
                ivMenuCount?.visibility=View.VISIBLE
            }
        } catch (e: Exception) {
e.printStackTrace()
        }
    }

    fun filterAndPlayAudioContent(songDataList: ArrayList<Track>, selectedTrackIndex: Int): TracklistDataModel {
        return CommonUtils.filterAudioContent(context, songDataList, selectedTrackIndex)
    }
    override fun onDestroy() {
        super.onDestroy()
        setLog("onDestroy","onDestroy  baseServiceJob: ${baseServiceJob?.isCancelled} baseServiceJob:${baseIOServiceJob?.isCancelled}")
        baseServiceJob.let {
            baseServiceJob.cancel()
            setLog("onDestroy","onDestroy baseServiceJob called ${baseServiceJob?.isCancelled}")
        }
        baseIOServiceJob.let {
            baseIOServiceJob.cancel()
            setLog("onDestroy","onDestroy baseServiceJob called ${baseIOServiceJob?.isCancelled}")
        }
    }


}