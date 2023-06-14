package com.hungama.music.ui.main.view.fragment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.*
import android.os.*
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.TimeBar
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.gson.Gson
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.base.BaseFragment.Companion.mediaRouteButton
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.addFragment
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getDeviceHeight
import com.hungama.music.utils.CommonUtils.getDeviceWidth
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.setTextOrHide
import com.hungama.music.utils.Constant.CONTENT_ARTIST_RADIO
import com.hungama.music.utils.Constant.CONTENT_LIVE_RADIO
import com.hungama.music.utils.Constant.CONTENT_MOOD_RADIO
import com.hungama.music.utils.Constant.CONTENT_ON_DEMAND_RADIO
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_swipable_player.*
import kotlinx.android.synthetic.main.fragment_swipable_player_view.*
import kotlinx.android.synthetic.main.layout_swipable_player_view.*
import kotlinx.android.synthetic.main.music_player_three_dots_bottom_sheet.*
import kotlinx.android.synthetic.main.swipable_player_controls_layout.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class SwipablePlayerViewFragment : Fragment(),
    View.OnClickListener, BaseActivity.OnSwipablePlayerListener, TimeBar.OnScrubListener,
    BaseActivity.OnTritonAdListener,MusicPlayerThreeDotsBottomSheetFragment.OnMusicMenuItemClick, MusicPlayBackSettingStreamQuality.OnItemClick {
    private var track: Track? = null
    private var songPosition: Int = -1

    var artworkProminentColor = 0
    var userCoins = 0
    var extraSpace = 50
    var extarSpaceBottomBlur = 170
    var currentPlayerType = Constant.CONTENT_MUSIC

    var lastClickedTime: Long = 0

    private var progressStatus = 0

    //The number of milliseconds in the future from the
    //call to start() until the count down is done
    private var millisInFuture: Long = 6000 //6 seconds (make it dividable by 1000)

    var ft: AdsConfigModel.Ft? = AdsConfigModel.Ft()
    var nonft: AdsConfigModel.Nonft? = AdsConfigModel.Nonft()
    //The interval along the way to receive onTick() callbacks
    private val countDownInterval: Long = 1000 //1 second (don't change this value)

    private val mainScope = CoroutineScope(Dispatchers.Main) + CoroutineName("SwipablePlayerViewFragment")
    private val ioScope = CoroutineScope(Dispatchers.IO) + CoroutineName("SwipablePlayerViewFragment")
    private var objectAnimator: ObjectAnimator? = null
    companion object {
        fun newInstance(track: Track, position: Int) = SwipablePlayerViewFragment()
            .apply {
                arguments = Bundle().apply {
                    putSerializable("KEY_STORY_DATA", track)
                    putInt("songPosition", position)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_swipable_player_view, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLog("alhglhal","MusicPlayer1")
        if (arguments != null) {
            track = requireArguments().getSerializable("KEY_STORY_DATA") as Track?
            songPosition = requireArguments().getInt("songPosition", -1)
        }
        headBarBlur?.hide()
        rlBack?.setOnClickListener {
            if (BaseActivity.includeFreeMinute.isVisible || BaseActivity.songPreviewModel.isVisible || BaseActivity.newPreviewModel.isVisible)
                return@setOnClickListener

            CommonUtils.PageViewEvent("","","","",
                MainActivity.lastItemClicked, MainActivity.tempLastItemClicked + "_" + MainActivity.headerItemName,
                MainActivity.headerItemPosition.toString())
            backPress()
        }
        println("SwipableView " +"Swippaasdlfgalshjlgnas")
        isDisplaySkeleton(true)
        setData()
        loadBottomAds()
        setSwipeListener()

        mediaRouteButton = view?.findViewById(R.id.media_route_button)
        mediaRouteButton?.setRemoteIndicatorDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_cast
            )
        )
        CastButtonFactory.setUpMediaRouteButton(requireActivity(), mediaRouteButton!!)
        //CastContext.getSharedInstance(requireActivity())
        CastContext.getSharedInstance()?.setReceiverApplicationId(getString(R.string.chormecast_app_id))
    }

    private fun setData() {
        mainScope.launch {
            if (isAdded) {
                if (activity != null && track != null) {
                    currentPlayerType = (activity as MainActivity).getPlayerType(track?.playerType)
                    setLog("SwipablePlayerViewFragment", "setData()-track.title=${track?.title}")
                    setLog(
                        "SwipablePlayerViewFragment",
                        "setData()11-track.contentType=${track?.contentType}"
                    )
                    isDisplaySkeleton(false)

                    setLog("CheckPageViewSwip", MainActivity.lastItemClicked + " " +
                            MainActivity.headerItemName + " " + MainActivity.tempLastItemClicked + "\n" +
                            Gson().toJson(track))
                }

                setLog("TAG", "setData: $track")

                if(track?.heading.equals("")) {
                    text_view_parent_tag?.setTextOrHide(value = track?.pName)
                }
                else{
                    text_view_parent_tag?.setTextOrHide(value = track?.heading)
                }
                text_view_parent_tag?.setOnClickListener {
                    track?.let { it1 -> redirectToDetailPage(it1) }
                }
               // updateFavouriteStatus(isFavorite=)
                track?.isLiked.let {it1 ->
                    if (it1 != null) {
                        updateFavouriteStatus(it1)
                    }
                }
                text_view_video_description?.setTextOrHide(value = track?.subTitle)
                text_view_music_title?.setTextOrHide(value = track?.title)

                image_view_option_comment_title?.text = "0"
                image_view_option_like_title?.text = "0"
                track?.image?.let {
                    if (ivFullImage != null) {
                        ImageLoader.loadImage(
                            requireContext(), ivFullImage,
                            it, R.drawable.bg_gradient_placeholder
                        )
                        setLog(TAG, "setData: playble_image:${it}")
                    }
                    if (topAlbumArtImageView != null) {
                        ImageLoader.loadImage(
                            requireContext(), topAlbumArtImageView,
                            it, R.drawable.bg_gradient_placeholder
                        )
                    }
                    if (bottomAlbumArtImageView != null) {
                        ImageLoader.loadImage(
                            requireContext(), bottomAlbumArtImageView,
                            it, R.drawable.bg_gradient_placeholder
                        )
                    }
                    ioScope.launch {
                        setArtImageBg(it)
                    }

                }
                extraSpace = resources.getDimensionPixelSize(R.dimen.dimen_50)
                setLog("SwipablePlayerViewFragment", "setData()-extraSpace=$extraSpace")
                val totalArtworkWidthHeight = getDeviceWidth(requireContext()) + extraSpace
                ivFullImage?.layoutParams?.width = totalArtworkWidthHeight
                ivFullImage?.layoutParams?.height = totalArtworkWidthHeight
                ivFullImage?.requestLayout()

                if(!CommonUtils.isUserHasGoldSubscription()) {

                    if(CommonUtils.getFirebaseConfigAdsData().enablePaymentNudge &&
                        Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){

                        iv_banner.show()
                    }


                    if (CommonUtils.isUserHasEliableFreeContent()) {
//                        setLog("ahlgla", CommonUtils.getFirebaseConfigAdsData().nudgeAlbumBanner.ft!!.toString())
                            ft = CommonUtils.getFirebaseConfigAdsData().nudgeAlbumBanner.ft

                            if (!TextUtils.isEmpty(ft?.image_url)) {
                                ImageLoader.loadImageWithoutPlaceHolder(
                                    requireContext(),
                                    iv_banner,
                                    ft?.image_url!!
                                )
                            }
                    } else {
                        nonft = CommonUtils.getFirebaseConfigAdsData().nudgeAlbumBanner.nonft

                        if (!TextUtils.isEmpty(nonft?.image_url)) {
                            ImageLoader.loadImageWithoutPlaceHolder(
                                requireContext(),
                                iv_banner,
                                nonft?.image_url!!
                            )
                        }
                    }
                }

                iv_banner.setOnClickListener {
                    Constant.screen_name ="Swipable Player Screen"
                   CommonUtils.openSubscriptionDialogPopup(requireContext(), PlanNames.SVOD.name, "", true, null, "", null,Constant.nudge_player_banner,"banner")
                }


                extarSpaceBottomBlur = resources.getDimensionPixelSize(R.dimen.dimen_170)
                setLog(
                    "SwipablePlayerViewFragment",
                    "setData()-extarSpaceBottomBlur=$extarSpaceBottomBlur"
                )
                val bottomBlureHeight =
                    (getDeviceHeight(requireContext()) - totalArtworkWidthHeight) + extarSpaceBottomBlur
                setLog(
                    "SwipablePlayerViewFragment",
                    "setData()-getDeviceHeight(requireContext())=${getDeviceHeight(requireContext())}"
                )
                setLog(
                    "SwipablePlayerViewFragment",
                    "setData()-bottomBlureHeight=$bottomBlureHeight"
                )
                bottomImageFade?.layoutParams?.height = bottomBlureHeight

                //setMarginsTop(bottomImageFade, bottomBlureHeight-extarSpaceBottomBlur)
                bottomImageFade?.requestLayout()
                ivFullImage?.postDelayed({
                    if (isAdded) {
                        val point = Point()
                        requireActivity().windowManager.defaultDisplay.getSize(point)
                        val width: Float = ivFullImage.measuredWidth.toFloat()
                        objectAnimator =
                            ObjectAnimator.ofFloat(
                                ivFullImage,
                                "translationX",
                                0f,
                                -(width - point.x)
                            )
                        objectAnimator?.repeatMode = ValueAnimator.REVERSE
                        objectAnimator?.repeatCount = ValueAnimator.INFINITE
                        objectAnimator?.duration = 8000
                        objectAnimator?.start()
                    }
                }, 2000)

                text_view_music_title.isSelected = true
                text_view_video_description.isSelected = true
                val userCoinDetailRespModel =
                    SharedPrefHelper.getInstance().getObjectUserCoin(PrefConstant.USER_COIN)

                if (userCoinDetailRespModel != null && !userCoinDetailRespModel.actions.isNullOrEmpty()) {
                    if (!TextUtils.isEmpty(userCoinDetailRespModel.actions?.get(0)?.total.toString())) {
                        userCoins = userCoinDetailRespModel.actions?.get(0)?.total!!
                    }
                }
                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                    tvCoinCount.text = CommonUtils.ratingWithSuffix(userCoins.toString())
                }

                val profileImage = SharedPrefHelper.getInstance().getProfileImage()
                if (profileImage != null && ivUserPersonalImage != null) {
                    ImageLoader.loadImage(
                        requireContext(), ivUserPersonalImage,
                        profileImage, R.drawable.ic_no_user_img
                    )
                }
                /*val simplePlayer = getPlayer()
                player_view_story.player = simplePlayer
                storyUrl = track?.url
                storyUrl?.let { prepareMedia(it) }*/

                ivPlayPause?.setOnClickListener(this@SwipablePlayerViewFragment)
                roundViewThree?.setOnClickListener(this@SwipablePlayerViewFragment)
                roundViewFour?.setOnClickListener(this@SwipablePlayerViewFragment)
                roundViewFive?.setOnClickListener(this@SwipablePlayerViewFragment)
                clCast?.setOnClickListener(this@SwipablePlayerViewFragment)
                roundViewDurationFive?.setOnClickListener(this@SwipablePlayerViewFragment)
                rlDataSaveTxt?.setOnClickListener(this@SwipablePlayerViewFragment)


                if (track?.pType != DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                    if (currentPlayerType == Constant.CONTENT_PODCAST) {
                        //image_view_option_like?.hide()
                        ivFavoriteAnim?.hide()
                        roundViewOne?.hide()
                        image_view_option_like_title?.hide()
                        roundViewOne?.setOnClickListener(null)
                    } else {
                        //image_view_option_like?.show()
                        ivFavoriteAnim?.show()
                        roundViewOne?.show()
                        image_view_option_like_title?.show()
                        roundViewOne?.setOnClickListener {
                            //if (likeButtonClick()) {
                                //setSongLike()
                                if (activity != null && activity is MainActivity){
                                    (activity as MainActivity).setSongLike()
                                }
//                            } else {
//                                setLog("TAG", "setData: wait")
//                            }
                        }
                    }

                    var isDownloaded = false
                    try {
                        isDownloaded = CommonUtils.isContentDownloaded(
                            BaseActivity.songDataList,
                            BaseActivity.nowPlayingCurrentIndex()
                        )
                    }catch (e:Exception){
                        isDownloaded = false
                    }

                    if (isDownloaded) {
                        if (isAdded && context != null && image_view_option_comment != null) {
                            context?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                                CommonUtils.downloadIconStates(
                                    requireContext(),
                                    Status.COMPLETED.value,
                                    image_view_option_comment,
                                    it.toFloat()
                                )
                            }
                        }
                    } else {
                        var downloadQueue: DownloadQueue? = null
                        try {
                            downloadQueue =
                                AppDatabase.getInstance()?.downloadQueue()?.findByContentId(
                                    track?.id.toString()
                                )
                        }catch (e:Exception){
                            downloadQueue = null
                        }

                        if (downloadQueue != null && (!downloadQueue.contentId.equals(
                                track?.id.toString()
                            ))
                        ) {
                            if (isAdded && context != null && image_view_option_comment != null) {
                                context?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                                    CommonUtils.downloadIconStates(
                                        requireContext(),
                                        downloadQueue.downloadStatus,
                                        image_view_option_comment,
                                        it.toFloat()
                                    )
                                }
                            }
                        } else {
                            if (isAdded && context != null && image_view_option_comment != null) {
                                context?.resources?.getDimensionPixelSize(R.dimen.font_16)?.toFloat()
                                    ?.let {
                                        CommonUtils.downloadIconStates(
                                            requireContext(),
                                            Status.NONE.value,
                                            image_view_option_comment,
                                            it
                                        )
                                    }
                            }
                        }
                        if (currentPlayerType == Constant.CONTENT_RADIO
                            || currentPlayerType == CONTENT_ON_DEMAND_RADIO
                            || currentPlayerType == CONTENT_MOOD_RADIO
                            || currentPlayerType == CONTENT_LIVE_RADIO
                            || currentPlayerType == CONTENT_ARTIST_RADIO) {
                            roundViewTwo?.setOnClickListener(null)
                        } else {
                            roundViewTwo?.setOnClickListener(this@SwipablePlayerViewFragment)
                        }

                    }
                } else {
                    image_view_option_comment?.alpha = 0.4F
                    roundViewTwo?.setOnClickListener(null)
                    image_view_option_share?.alpha = 0.4F
                    roundViewThree?.setOnClickListener(null)
                    //image_view_option_like?.alpha = 0.4F
                    ivFavoriteAnim?.alpha = 0.4F
                    roundViewOne?.setOnClickListener(null)
                    image_view_option_queue.alpha = 0.4f
                    roundViewFour?.setOnClickListener(null)
                }

                track?.let {
                    setLog(TAG, "setData: it ${it.isLiked}")
                    initFavoriteMusic(it)
                }
                updateViewByPlayerType(track)
                if (view != null) {
                    view?.isFocusableInTouchMode = true
                    view?.requestFocus()
                    view?.setOnKeyListener { v, keyCode, event ->
                        setLog("playerBackpress", "keyCode-$keyCode")
                        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                            if (activity != null && activity is MainActivity) {
                                (activity as MainActivity).unSelectAllTab()
                                (activity as MainActivity).setLastClickedBottomMenu(
                                    MainActivity.tempLastItemClicked,
                                    MainActivity.tempLastBottomItemPosClicked
                                )
                            }
                            false
                        } else false
                    }
                }
            }
        }
    }

    private fun setLyrics() {
        /*var number = "10"
        for (i in 0..6){
            covertNumberToCurrencyFormat(number)
            number +="0"
        }
        covertNumberToCurrencyFormat("123456789")*/
        track?.let {
            if (activity != null && activity is MainActivity){
                setLog("setSongLyricsData", "setSongLyricsData SwipablePlayerViewFragment-> track:${track?.songLyricsUrl}")
                if (!TextUtils.isEmpty(it.songLyricsUrl)){
                    (activity as MainActivity).callSongLyricsApi(it, lrc_view_full)
                }else{
                    lrc_view_full?.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onPause() {
        setLog("SwipablePlayerViewFragment", "onPause")
        setLog(
            "SwipablePlayerFragment",
            "SwipablePlayerViewFragment-onPause()-songPosition=${songPosition} currentViewPagerIndex:${SwipablePlayerFragment.currentViewPagerIndex} nowPlayingCurrentIndex:${BaseActivity.nowPlayingCurrentIndex()}"
        )
        super.onPause()
    }

    override fun onResume() {
        mainScope.launch {
            BaseActivity.showAd = 1
            BaseActivity.isTouch = true
            if(tvCount != null && pbDuration != null)
            {
                BaseActivity.tvSleepTimer = tvCount
                BaseActivity.pbDuration = pbDuration
            }
            val songDurationData = AppDatabase.getInstance()?.songDuration()?.getSongDuration()
            var totalPlayedSong = BaseActivity.totalPlayedSongDuration
            if (totalPlayedSong<0){
                totalPlayedSong = 0
                }
            val durationData = (BaseActivity.totalGetted.minus((totalPlayedSong/1000/60)))
            tvCount?.text = if (durationData in 0..9) "0$durationData" else durationData.toString()
            if (songDurationData != null) {
                if (songDurationData.stream_max_min_allowed != null) {

                    var progress = if(songDurationData.stream_max_min_allowed!!>0)((BaseActivity.maxMinAllowed.toDouble().div(
                        songDurationData.stream_max_min_allowed!!
                    ) * 100)) else 0
                    progress = "100".toDouble().minus(progress.toDouble())

/*                    var progress = if (songDurationData.stream_max_min_allowed!! > 0) ((durationData.toDouble()
                            .div(songDurationData.stream_max_min_allowed!!) * 100)) else 0
                    progress = "100".toDouble().minus(progress.toDouble())*/

                    pbDuration?.secondaryProgress = progress.toInt()
                    setLog("SongDurationData", "Swipable " + progress.toString() + " " + songDurationData.stream_max_min_allowed.toString() + " " + BaseActivity.maxMinAllowed.toString())
                }
            }

            if(CommonUtils.isUserHasGoldSubscription() || !CommonUtils.getSongDurationConfig().enable_minutes_quota){
                roundViewDurationFive.visibility = View.GONE
                pbDuration?.visibility = View.GONE
                tvCount?.visibility = View.GONE
                tvMinutesLeft.visibility = View.GONE
                rlDataSaveTxt.visibility = View.GONE
            }
            else{
                roundViewDurationFive.visibility = View.VISIBLE
                pbDuration?.visibility = View.VISIBLE
                tvCount?.visibility = View.VISIBLE
                tvMinutesLeft.visibility = View.VISIBLE
                rlDataSaveTxt.visibility = View.VISIBLE
            }

            if (BaseActivity.player11?.isPlaying == true){
                BaseActivity.newPreviewModel.visibility = View.VISIBLE
                BaseActivity.showjson()
            }
            else{
                BaseActivity.newPreviewModel.visibility = View.GONE
            }

            if (songPosition == SwipablePlayerFragment.currentViewPagerIndex) {
                if (songPosition > -1) {
                    if (BaseActivity.nowPlayingCurrentIndex() == songPosition) {
                        swipablePlayerView?.show()
                        swipableAdsView?.hide()
                    } else if (BaseActivity.nowPlayingCurrentIndex() < songPosition) {
                        if (activity != null && activity is MainActivity){
                            (activity as MainActivity).playNextSong(false)
                        }

                        swipablePlayerView?.show()
                        swipableAdsView?.hide()
                    } else if (BaseActivity.nowPlayingCurrentIndex() > songPosition) {
                        if (activity != null && activity is MainActivity){
                            (activity as MainActivity).playPreviousSong()
                        }

                        swipablePlayerView?.show()
                        swipableAdsView?.hide()
                    }
                }
            }
            if (activity != null && activity is MainActivity){
                player_view_story?.player = (activity as MainActivity).getAudioPlayerInstance()
                player_view_story?.player?.addListener(playerCallback)
                exo_progress2?.addListener(this@SwipablePlayerViewFragment)
            }
            setData()
            updateView()
            if (activity != null && activity is MainActivity){
                (activity as MainActivity).setPlayerProgressChangeEventCallBack(this@SwipablePlayerViewFragment)
            }

            if (BaseActivity.isAudioAdPlaying){
                swipablePlayerView?.hide()
                swipableAdsView?.show()
            }
            if (songPosition == SwipablePlayerFragment.currentViewPagerIndex) {
                setLyrics()
            }
        }

        super.onResume()
    }

    override fun onDestroy() {
        audioPlayerBackTappedEvent()
        if (objectAnimator != null){
            objectAnimator?.end()
        }
        timer?.cancel()
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    private val playerCallback: Player.Listener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            updateView()
            MainScope().launch {
                if (context != null) {
                    changeStatusbarcolor(artworkProminentColor)
                }
                if (activity != null && activity is MainActivity){
                    if ((activity as MainActivity).miniplayerHeight > 0 && (!Utils?.getCurrentFragment(
                            requireContext()
                        )?.javaClass?.simpleName.equals(
                            QueueFragment().javaClass.simpleName, true
                        ) && !Utils?.getCurrentFragment(
                            requireContext()
                        )?.javaClass?.simpleName.equals(
                            SongDetailFragment().javaClass.simpleName, true
                        ))
                    ) {
                        setLog("onPlayerStateChanged", "onPlayerStateChanged-hideMiniPlayer")
                        (activity as MainActivity).hideMiniPlayer()
                        (activity as MainActivity).hideStickyAds()
                    }
                }
            }
        }



    }

    override fun onUserClickOnQuality(position: Int, settingType: Int) {

    }

    override fun onClick(v: View?) {
        if (v == ivPlayPause) {
            Constant.isVideoStoryPlaying = false
            if (activity != null && activity is MainActivity){
                when {
                    (activity as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing -> {
                        (activity as MainActivity).pausePlayer()
                        ivPlayPause?.setImageDrawable(
                            requireContext().faDrawable(
                                R.string.icon_play,
                                R.color.colorWhite
                            )
                        )

                        CoroutineScope(Dispatchers.IO).async {
                            val hashMap = HashMap<String, String>()
                            //setLog("AudioContentType", "SwipablePlayerViewFragment-contentType- ${track?.playerType}")
                            if (!TextUtils.isEmpty(track?.playerType)) {
                                var type = Utils.getContentTypeName(track?.playerType.toString())
                                if (type.equals("Audio")) {
                                    type = "Music"
                                }
                                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, type)
                            }else{
                                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "")
                            }
                            hashMap.put(
                                EventConstant.PLAYERTYPE_EPROPERTY,
                                EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER
                            )
                            setLog("AUDIO", "PREVIOUS${hashMap}")
                            EventManager.getInstance().sendEvent(AudioPlayerPauseEvent(hashMap))
                        }


                    }
                    (activity as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause -> {
                        (activity as MainActivity).playPlayer()
                        if (currentPlayerType == Constant.CONTENT_RADIO) {
                            ivPlayPause?.setImageDrawable(
                                requireContext().faDrawable(
                                    R.string.icon_stop,
                                    R.color.colorWhite
                                )
                            )
                        } else {
                            ivPlayPause?.setImageDrawable(
                                requireContext().faDrawable(
                                    R.string.icon_pause_2,
                                    R.color.colorWhite
                                )
                            )
                        }
                        CoroutineScope(Dispatchers.IO).async {
                            val hashMap = HashMap<String, String>()
                            if (!TextUtils.isEmpty(track?.playerType)) {
                                var type = Utils.getContentTypeName(track?.playerType.toString())
                                if (type.equals("Audio")) {
                                    type = "Music"
                                }
                                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, type)
                            } else {
                                hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "")
                            }
                            hashMap.put(
                                EventConstant.PLAYERTYPE_EPROPERTY,
                                EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER
                            )
                            setLog("AUDIO", "PREVIOUS${hashMap}")
                            EventManager.getInstance().sendEvent(AudioPlayerPlayEvent(hashMap))
                        }


                    }
                    else -> {
                        ivPlayPause?.setImageDrawable(
                            requireContext().faDrawable(
                                R.string.icon_play,
                                R.color.colorWhite
                            )
                        )
                    }
                }
            }
        }
        else if(v == rlDataSaveTxt){
            val sheet = MusicPlayBackSettingStreamQuality(CommonUtils.getStreamQualityDummyData(
                QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY, "music_player_screen","swipable"),"",this,"swipable")
            sheet.show(activity?.supportFragmentManager!!, "MusicPlayBackSettingStreamQuality")
        }
        else if (v == roundViewTwo) {
            if (activity != null) {
                (activity as MainActivity).downloadAudioTrack()
            }
        } else if (v == roundViewFive) {
            addFragment(requireContext(), R.id.fl_container, this, ProfileFragment(), false)
            CoroutineScope(Dispatchers.IO).async {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, "ProfileClicked")
                setLog("PROFILECLICKED", "profile${hashMap}")
                EventManager.getInstance().sendEvent(ProfileClickedEvent(hashMap))
            }

        } else if (v == roundViewFour) {
            /*BaseActivity.isNewSwipablePlayerOpen = false
            addFragment(requireContext(),R.id.fl_container, this, QueueFragment(), false)
            (activity as MainActivity).changeMiniPlayerState(BottomSheetBehavior.STATE_COLLAPSED)*/
            if (activity != null) {
                (activity as MainActivity).openThreeDotPopup()
            }
        }else if (v == roundViewDurationFive) {
            if (activity != null) {
                Constant.screen_name ="Swipable Player Screen"
                CommonUtils.openSubscriptionDialogPopup(
                    requireContext(),
                    PlanNames.SVOD.name,
                    "player",
                    true,
                    null,
                    "",
                    null,Constant.drawer_default_buy_hungama_gold
                )
            }
        }
        else if (v == roundViewThree) {
            CoroutineScope(Dispatchers.IO).async {
                var hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY, getString(R.string.menu_str_1))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            }


            val eventData = HungamaMusicApp.getInstance().getEventData("" + track?.id)
            if (eventData != null && !TextUtils.isEmpty(eventData?.share)) {
                setLog("TAG", "rl_music_player_menu_share share url:${eventData?.share}")
                var shareurl = getString(R.string.music_player_str_18) + " " + eventData?.share
                shareurl += "play/"
                Utils.shareItem(requireActivity(), shareurl)
            } else {
                try {
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                        track?.id.toString())
                    if (downloadedAudio != null && downloadedAudio?.contentId.equals(track?.id.toString()) && !TextUtils.isEmpty(downloadedAudio.contentShareLink)){
                        var shareurl = getString(R.string.music_player_str_18) + " " + downloadedAudio.contentShareLink
                        shareurl += "play/"
                        Utils.shareItem(requireActivity(), shareurl)
                    }else{
                        setLog("TAG", "rl_music_player_menu_share share is empty")
                    }
                }catch (e:Exception){
                    setLog("TAG", "rl_music_player_menu_share share is empty")
                }
            }
        }else if (v == clCast || v == ll_cast){
            (activity as BaseActivity).setUpChormeCast()
            mediaRouteButton?.performClick()

            setLog("cast","mediaRouteButton called mediaRouteButton:${mediaRouteButton}")
            ivCast?.setImageDrawable(
                context?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                    requireContext().faDrawable(
                        R.string.icon_cast,
                        R.color.colorWhite,
                        it.toFloat()
                    )
                }
            )
            CoroutineScope(Dispatchers.IO).async {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY, getString(R.string.cast_screen))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            }

            //onMusicMenuItemClick(Constant.CAST_SCREEN_MENU_ITEM)
        }
    }

    private fun updateView() {
        if (activity != null && (!Utils?.getCurrentFragment(requireContext())?.javaClass?.simpleName.equals(
                QueueFragment().javaClass.simpleName, true
            ) && !Utils?.getCurrentFragment(requireContext())?.javaClass?.simpleName.equals(
                SongDetailFragment().javaClass.simpleName, true
            ))
        ) {
            //setLog("SwipablePlayerFragment", "SwipablePlayerViewFragment-updateView()-visible")
            BaseActivity.isNewSwipablePlayerOpen = true
            (activity as MainActivity).hideMiniPlayer()
            (activity as MainActivity).hideStickyAds()
        }
        changePlayPauseIcon()
    }

    fun setArtImageBg(artImageUrl: String) {
        try {
            val result: Deferred<Bitmap?> = ioScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }
            ioScope.launch {
                try {
                    val bitmap: Bitmap? = result.await()
                    if (bitmap != null) {
                        if (isAdded && bitmap != null && activity != null) {
                            artworkProminentColor = CommonUtils.calculateAverageColor(bitmap, 1)
                            //(activity as AppCompatActivity).window.statusBarColor = artworkProminentColor
                            mainScope.launch {
                                if (context != null) {
                                    CommonUtils.setLog(
                                        "SwipableLifecycle",
                                        "setArtImageBg--$artworkProminentColor"
                                    )
                                    changeStatusbarcolor(artworkProminentColor)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {

                }
            }
        } catch (e: Exception) {

        }
    }

    fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }

    protected suspend fun changeStatusbarcolor(color: Int) {
        if (activity != null) {
            if (activity is BaseActivity) {
                (activity as BaseActivity).changeStatusbarcolor(color)
            }
        }

    }

    override fun onPlayerProgressChange(currentPosition: Long?, duration: Long?, totDur: String) {
        mainScope.launch {
//            tvSleepTimer.text = BaseActivity.updateDuration()
            if (duration != null && currentPosition != null && duration > 0) {
                exo_progress2?.setPosition(currentPosition)
                exo_progress2?.setDuration(duration)
                exo_duration_new?.setText(totDur)
            }
        }
    }



    override fun onDownloadContentStateChange(status: Int) {
        if (image_view_option_comment != null) {
                mainScope.launch {
                    if (isAdded && context != null && image_view_option_comment != null) {
                        context?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                            CommonUtils.downloadIconStates(
                                requireContext(),
                                status,
                                image_view_option_comment,
                                it.toFloat()
                            )
                        }
                }
            }
        }
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {

    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {

    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        if (activity != null) {
            mainScope.launch {
                if (activity != null && activity is MainActivity){
                    (activity as MainActivity).setPlayerSeekProgress(position)
                }

            }
        }
    }

    fun initFavoriteMusic(track: Track) {
        if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex() && BaseActivity.songDataList?.get(
                BaseActivity.nowPlayingCurrentIndex()
            )?.pType != DetailPages.LOCAL_DEVICE_SONG_PAGE.value
        ) {
            setLog(TAG, "initFavoriteMusic: isLiked ${track.isLiked}")
            //onFavoritedContentStateChange(track.isLiked)

            val eventData = HungamaMusicApp.getInstance().getEventData("" + track?.id)
            if (eventData != null && !TextUtils.isEmpty(eventData?.f_fav_count)) {
                image_view_option_like_title?.text = eventData?.f_fav_count
            }
        } else {
            image_view_option_like_title?.text = "0"
        }
    }

    private fun updateViewByPlayerType(track: Track?) {
        if (track != null) {
            var icon = R.string.icon_music
            if (currentPlayerType == Constant.CONTENT_PODCAST) {
                roundViewDurationFive.visibility = View.GONE
                pbDuration?.visibility = View.GONE
                tvCount?.visibility = View.GONE
                tvMinutesLeft.visibility = View.GONE
                rlDataSaveTxt.visibility = View.GONE
                icon = R.string.icon_podcast
            } else if (currentPlayerType == Constant.CONTENT_RADIO) {
                roundViewDurationFive.visibility = View.GONE
                pbDuration?.visibility = View.GONE
                tvCount?.visibility = View.GONE
                tvMinutesLeft.visibility = View.GONE
                rlDataSaveTxt.visibility = View.GONE
                icon = R.string.icon_radio
                image_view_option_comment?.alpha = 0.4F
                text_view_parent_tag?.visibility = View.INVISIBLE
                exo_progress2?.visibility = View.INVISIBLE
                rlLive2?.visibility = View.VISIBLE
                llExoDuration2?.visibility = View.GONE
            } else if (currentPlayerType == Constant.CONTENT_MOOD_RADIO) {
                roundViewDurationFive.visibility = View.GONE
                pbDuration?.visibility = View.GONE
                tvCount?.visibility = View.GONE
                tvMinutesLeft.visibility = View.GONE
                rlDataSaveTxt.visibility = View.GONE
                icon = R.string.icon_radio
                image_view_option_comment?.alpha = 0.4F
            } else if (currentPlayerType == Constant.CONTENT_ON_DEMAND_RADIO) {
                roundViewDurationFive.visibility = View.GONE
                pbDuration?.visibility = View.GONE
                tvCount?.visibility = View.GONE
                tvMinutesLeft.visibility = View.GONE
                rlDataSaveTxt.visibility = View.GONE
                icon = R.string.icon_radio
                image_view_option_comment?.alpha = 0.4F
            } else {
                icon = R.string.icon_music
            }

            image_view_music_icon?.setImageDrawable(
                requireContext().faDrawable(
                    icon,
                    R.color.colorWhite,
                    resources.getDimensionPixelSize(R.dimen.font_18).toFloat()
                )
            )
            changePlayPauseIcon()
        }
    }

    private fun changePlayPauseIcon() {
        if (activity != null) {

            when {
                (activity as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing -> {
                    mainScope.launch {
                        if (currentPlayerType == Constant.CONTENT_RADIO) {
                            ivPlayPause?.setImageDrawable(
                                requireContext().faDrawable(
                                    R.string.icon_stop,
                                    R.color.colorWhite
                                )
                            )
                        } else {
                            ivPlayPause?.setImageDrawable(
                                requireContext().faDrawable(
                                    R.string.icon_pause_2,
                                    R.color.colorWhite
                                )
                            )
                        }
                    }
                }
                (activity as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause -> {
                    mainScope.launch {
                        ivPlayPause?.setImageDrawable(
                            requireContext().faDrawable(
                                R.string.icon_play,
                                R.color.colorWhite
                            )
                        )
                    }
                }
                else -> {
                    mainScope.launch {
                        ivPlayPause?.setImageDrawable(
                            requireContext().faDrawable(
                                R.string.icon_play,
                                R.color.colorWhite
                            )
                        )
                    }

                }
            }
        }
    }

    private fun likeButtonClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickedTime < Constant.LIKE_BUTTON_INTERVAL) {
            return false
        }
        lastClickedTime = SystemClock.elapsedRealtime()
        return true
    }

    var isBottomStickyAdLoaded = false
    private var initialLayoutComplete = false
    private fun loadBottomAds() {
        ioScope.launch {
            try{
                if (CommonUtils.isDisplayAds()) {
                    val observer = sticky_ad_view_container?.viewTreeObserver
                    observer?.addOnGlobalLayoutListener {
                        if (!initialLayoutComplete && context != null) {
                            initialLayoutComplete = true
                            val adView = CommonUtils.loadBannerAds(
                                requireContext(),
                                Constant.AD_UNIT_ID_STICKY_LIST,
                                AdSize.BANNER,
                                sticky_ad_view_container
                            )

                            adView.adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    // Code to be executed when an ad finishes loading.
                                    setLog("loadBottomAds", "Loaded")
                                    isBottomStickyAdLoaded = true
                                    clStickyAdsMain?.show()
                                    ivCloseStickyAd?.setOnClickListener {
                                        if (context != null) {
                                            Constant.screen_name ="Swipable Player Screen"

                                            CommonUtils.openSubscriptionDialogPopup(
                                                requireContext(),
                                                PlanNames.SVOD.name,
                                                "",
                                                true,
                                                null,
                                                "",
                                                null,Constant.drawer_remove_ads
                                            )
                                        }
                                    }
                                }

                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    // Code to be executed when an ad request fails.
                                    setLog("loadBottomAds", "Failed-" + adError.message)
                                    isBottomStickyAdLoaded = false
//                    clStickyAdsMain?.hide()
                                }

                                override fun onAdOpened() {
                                    // Code to be executed when an ad opens an overlay that
                                    // covers the screen.
                                }

                                override fun onAdClicked() {
                                    // Code to be executed when the user clicks on an ad.
                                }

                                override fun onAdClosed() {
                                    // Code to be executed when the user is about to return
                                    // to the app after tapping on an ad.
                                    isBottomStickyAdLoaded = false
                                }
                            }
                        }
                    }
                } else {
                    setLog("loadBottomAds", "CommonUtils.isAdsEnable():${CommonUtils.isAdsEnable()}")
                    clStickyAdsMain?.hide()
                }
            }catch (e:Exception){

            }
        }
    }

    var timer: CountDownTimer? = null
    private fun setProgressbar() {
        if(pb!=null && clProgressView!=null){
            try {
                clProgressView?.visibility = View.VISIBLE
                pb?.progressDrawable?.setColorFilter(Color.parseColor("#2b68e8"), PorterDuff.Mode.SRC_IN);
                progressStatus = 0

                //Cast long value to int value
                //When defining above variables, make sure 'progressBarMaximumValue' always rerun integer value
                val progressBarMaximumValue = (millisInFuture / countDownInterval).toInt()

                //Set ProgressBar maximum value
                //ProgressBar range (0 to maximum value)
                pb?.max = progressBarMaximumValue
                autoCloseAdsAfterSomeTime(millisInFuture)
            }catch (e:Exception){

            }


            //Initialize a new CountDownTimer instance
            timer = object : CountDownTimer(millisInFuture, countDownInterval) {
                override fun onTick(millisUntilFinished: Long) {
                    try {
                        //Another one second passed

                        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                        val text = "$minutes:$seconds"

                        if (tvProgress != null){
                            //tv.setText(millisUntilFinished / 1000.toString() + "  Seconds...")
                            tvProgress?.text = text + " sec"
                        }

                        //setLog("TimeStart", (millisUntilFinished / 1000).toString() + "  Seconds...")

                        if (pb != null){
                            //Each second ProgressBar progress counter added one
                            progressStatus += 1
                            pb?.progress = progressStatus
                        }
                    }catch (e:Exception){

                    }


                }

                override fun onFinish() {
                    try {
                        if (ivCloseAd != null){
                            ivCloseAd?.visibility = View.VISIBLE
                        }


                        if (pb != null){
                            //Do something when count down end.
                            progressStatus += 1
                            pb?.progress = progressStatus
                        }
                        onAudioAdsCompleted()
                    }catch (e:Exception){
                        onAudioAdsCompleted()
                    }


                }
            }.start()
        }

    }


    fun isDisplaySkeleton(isDisplay: Boolean) {
        if (isDisplay) {
            shimmerLayout.visibility = View.VISIBLE
            clMain.visibility = View.GONE
            shimmerLayout.startShimmer()
        } else {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            clMain.visibility = View.VISIBLE

        }
    }

    override fun onFavoritedContentStateChange(isFavorite: Boolean) {
        super.onFavoritedContentStateChange(isFavorite)
        mainScope.launch {
            setLog(TAG, "onFavoritedContentStateChange: isFacorite ${isFavorite}")
            var icon:Int
            if (isAdded && context != null) {
                 icon = R.string.icon_like
                if (isFavorite) {
                    icon = R.string.icon_liked
                }
                /*image_view_option_like.setImageDrawable(
                    requireContext().faDrawable(
                        icon,
                        R.color.colorWhite,
                        resources.getDimensionPixelSize(R.dimen.font_18).toFloat()
                    )
                )*/

                if (isFavorite) {
                    ivFavoriteAnim?.playAnimation()
                } else {
                    ivFavoriteAnim?.cancelAnimation()
                    ivFavoriteAnim?.progress = 0f
                }
                val eventData = HungamaMusicApp.getInstance().getEventData("" + track?.id)
                if (eventData != null && !TextUtils.isEmpty(eventData?.f_fav_count)) {
                    image_view_option_like_title?.text = eventData?.f_fav_count
                } else {
                    image_view_option_like_title?.text = "0"
                }
            }
        }
    }

    override fun onAudioAdsStarted(adsDuration:Long) {
        super.onAudioAdsStarted(adsDuration)
        mainScope.launch {
            setLog("PlayerAds:-", "SwipablePlayerViewFragment-onAudioAdsStarted=> adsDuration- $adsDuration")
            swipablePlayerView?.hide()
            swipableAdsView?.show()
            millisInFuture = adsDuration
            if (timer != null){
                timer?.cancel()
            }
            setProgressbar()
        }
    }

    override fun onAudioAdsCompleted() {
        super.onAudioAdsCompleted()
        mainScope.launch {
            setLog("PlayerAds:-", "SwipablePlayerViewFragment-onAudioAdsCompleted=> onAudioAdsCompleted.")
            swipablePlayerView?.show()
            swipableAdsView?.hide()
            timer?.cancel()
        }
    }

    override fun loadIntestitialAds() {
        super.loadIntestitialAds()
        setLog("PlayerAds:-", "loadInterstitialAds=> loadIntestitialAds ====>  isAdded - $isAdded")
        if (isAdded){
            loadInterstitialAds()
        }
    }

    var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    fun loadInterstitialAds(){
        mainScope.launch {
            val adRequest = AdManagerAdRequest.Builder().build()

            AdManagerInterstitialAd.load(requireActivity(),Constant.AD_UNIT_ID_SPLASH, adRequest, object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    setLog("PlayerAds:-", "InterstitialAd=> LoadAdError-${adError?.message}")
                    mAdManagerInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                    setLog("PlayerAds:-", "InterstitialAd=> onAdLoaded-Ad was loaded.")
                    mAdManagerInterstitialAd = interstitialAd
                    if (isAdded){
                        showInterstitial()
                        if (activity != null) {
                            (activity as MainActivity).updateAudioAdsParams()
                        }
                    }
                }
            })
        }
    }

    private fun showInterstitial() {
        if (mAdManagerInterstitialAd != null) {
            mAdManagerInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    setLog("PlayerAds:-", "InterstitialAd=> onAdDismissedFullScreenContent-Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mAdManagerInterstitialAd = null
                    if (activity != null) {
                        (activity as MainActivity).playPlayer()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    setLog("PlayerAds:-", "InterstitialAd=> onAdFailedToShowFullScreenContent-Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mAdManagerInterstitialAd = null
                    if (activity != null) {
                        (activity as MainActivity).playPlayer()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    setLog("PlayerAds:-", "InterstitialAd=> onAdShowedFullScreenContent-Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                    mAdManagerInterstitialAd = null
                    if (activity != null) {
                        (activity as MainActivity).pausePlayer()
                    }
                    /*Handler().postDelayed({
                        setLog("PlayerAds:-", "InterstitialAd=> onBackPressed-Ad was dismissed.")
                    }, 5000)*/
                }
            }
            mAdManagerInterstitialAd?.show(requireActivity())
        } else {
            setLog("PlayerAds:-", "InterstitialAd=> mAdManagerInterstitialAd-null-Ad wasn't loaded..")
        }
    }

    fun redirectToDetailPage(track:Track){
        try {
            if (activity != null && activity is MainActivity){
                (activity as MainActivity)?.unSelectAllTab()
            }
            if (track?.pType == DetailPages.PLAYLIST_DETAIL_PAGE.value || track?.pType == DetailPages.PLAYLIST_DETAIL_ADAPTER.value){
                if (activity is BaseActivity) {
                    val playableContentModel=HungamaMusicApp.getInstance().getEventData(""+track?.id.toString())
                    val bundle = Bundle()
                    if (!TextUtils.isEmpty(track.parentId)){
                        bundle.putString("image", track?.image)
                        bundle.putString("id", track?.parentId)
                        bundle.putString("playerType", track?.playerType)
                        if (!BaseActivity.isDisplayDiscover){
                            bundle.putBoolean(Constant.isFromVerticalPlayer, true)
                        }
                        val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(0)
                        playlistDetailFragment.arguments = bundle

                        (activity as BaseActivity).addFragment(
                            R.id.fl_container,
                            this,
                            playlistDetailFragment,
                            false
                        )
                    }else{
                        val messageModel = MessageModel(getString(R.string.no_playlist_found), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }
            }else if (track?.pType == DetailPages.CHART_DETAIL_PAGE.value || track?.pType == DetailPages.CHART_DETAIL_ADAPTER.value){
                if (activity is BaseActivity) {
                    val playableContentModel=HungamaMusicApp.getInstance().getEventData(""+track?.id.toString())
                    val bundle = Bundle()
                    if (!TextUtils.isEmpty(track.parentId)){
                        bundle.putString("image", track?.image)
                        bundle.putString("id", track?.parentId)
                        bundle.putString("playerType", track?.playerType)
                        val chartDetailFragment = ChartDetailFragment.newInstance(0)
                        chartDetailFragment.arguments = bundle

                        (activity as BaseActivity).addFragment(
                            R.id.fl_container,
                            this,
                            chartDetailFragment,
                            false
                        )
                    }else{
                        val messageModel = MessageModel(getString(R.string.no_playlist_found), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }
            }else if (track?.pType == DetailPages.ALBUM_DETAIL_PAGE.value || track?.pType == DetailPages.ALBUM_DETAIL_ADAPTER.value
                || track?.pType == DetailPages.RECOMMENDED_SONG_LIST_PAGE.value){
                if (activity is BaseActivity) {
                    val playableContentModel=HungamaMusicApp.getInstance().getEventData(""+track?.id.toString())
                    val bundle = Bundle()
                    val pidList =
                        CommonUtils.getCommaSeparatedStringToArray(playableContentModel.pid)
                    if (!pidList.isNullOrEmpty()){
                        val pid = pidList[0]
                        if (!TextUtils.isEmpty(pid)){
                            bundle.putString("image", track?.image)
                            bundle.putString("id", pid)
                            bundle.putString("playerType", track?.playerType)

                            val albumDetailFragment = AlbumDetailFragment()
                            albumDetailFragment.arguments = bundle

                            (activity as BaseActivity).addFragment(
                                R.id.fl_container,
                                this,
                                albumDetailFragment,
                                false
                            )
                        }
                    }else{
                        val messageModel = MessageModel(getString(R.string.no_album_found), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }
            }else if (track.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_ALBUM) || track.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_TRACK) || track?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_TRACK2)){
                if (!TextUtils.isEmpty(track.parentId)){
                    val bundle = Bundle()
                    bundle.putString("id",""+track.parentId)
                    bundle.putString("image", ""+track.pImage)
                    bundle.putString("playerType", ""+track.playerType)
                    val podcastDetailFragment = PodcastDetailsFragment()
                    podcastDetailFragment.arguments = bundle
                    (activity as BaseActivity).addFragment(
                        R.id.fl_container,
                        this,
                        podcastDetailFragment,
                        false
                    )
                }
            }
        }catch (e:Exception){

        }

    }

    private fun setSwipeListener(){
        if (isAdded && activity != null){
            swipablePlayerView?.setOnTouchListener(object : OnMusicPlayerSwipeTouchListener(requireActivity()) {
                override fun onSwipeRight() {
                    super.onSwipeRight()
                    backPress()
                }
            })
        }
    }
    private fun backPress(){
        if (activity != null && activity is MainActivity) {
            (activity as MainActivity).unSelectAllTab()
            (activity as MainActivity).onBackPressed()
            (activity as MainActivity).setLastClickedBottomMenu(
                MainActivity.tempLastItemClicked,
                MainActivity.tempLastBottomItemPosClicked
            )
        }

        }
    private fun updateFavouriteStatus(isFavorite: Boolean){

        CoroutineScope(Dispatchers.Main).launch {
            if (isAdded && context != null && ivFavoriteAnim != null) {
                var icon = R.string.icon_like
                if (isFavorite) {
                    icon = R.string.icon_liked
                }
                if (isFavorite) {
                    ivFavoriteAnim?.playAnimation()
                } else {
                    ivFavoriteAnim?.cancelAnimation()
                    ivFavoriteAnim?.progress = 0f
                }
                val eventData = HungamaMusicApp.getInstance().getEventData("" + track?.id)
                if (eventData != null && !TextUtils.isEmpty(eventData?.f_fav_count)) {
                    image_view_option_like_title?.text = eventData?.f_fav_count
                } else {
                    image_view_option_like_title?.text = "0"
                }

            }
        }
    }
        private fun audioPlayerBackTappedEvent() {
            CoroutineScope(Dispatchers.IO).async {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, "")
                if (!TextUtils.isEmpty(track?.playerType)) {
                    var type = Utils.getContentTypeName(track?.playerType.toString())
                    if (type.equals("Audio")) {
                        type = "Music"
                    }
                    hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, type)
                } else {
                    hashMap.put(EventConstant.CONTENTTYPESTREAMING_EPROPERTY, "")
                }
                hashMap.put(
                    EventConstant.PLAYERTYPE_EPROPERTY,
                    EventConstant.PLAYERTYPE_EPROPERTY_VALUE_FULL_PLAYER
                )
                setLog("backTappedEvent","CheckBackEvent${hashMap}")
                EventManager.getInstance().sendEvent(AudioPlayerBackTappedEvent(hashMap))
            }
        }

    override fun onMusicMenuItemClick(menuId: Int) {
        setLog(TAG, "onMusicMenuItemClick: " + menuId)
        if(menuId == Constant.CAST_SCREEN_MENU_ITEM){
            setLog(TAG, "onMusicMenuItemClick: menuId:${menuId} mediaRouteButton:${BaseFragment.mediaRouteButton}")
            mediaRouteButton?.performClick()
        }
    }

    private fun autoCloseAdsAfterSomeTime(millisInFuture: Long){
        setLog("PlayerAds:-", "autoCloseAdsAfterSomeTime-millisInFuture-$millisInFuture")
      Handler(Looper.getMainLooper()).postDelayed({
          setLog("PlayerAds:-", "autoCloseAdsAfterSomeTime-postDelayed-millisInFuture-$millisInFuture")
          if (isAdded && context != null && context is MainActivity){
              (context as MainActivity).updateAudioAdPlayingStatusAndProvider(false, (context as MainActivity).getCurrentAudioAdProvider())
              (context as MainActivity).audioAdsCompleted()
          }

          if (context != null){
              val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
              intent.putExtra("EVENT", Constant.AUDIO_PLAYER_ADS_END_RESULT_CODE)
              LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
          }

      }, millisInFuture)
    }
}