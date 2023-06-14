package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.FavouritedEvent
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.DetailPlaylistAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.HomeViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.MODULE_FAVORITE
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import com.hungama.music.utils.Constant.PLAYLIST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.PLAYLIST_DETAIL_PAGE
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_playlist_detail_dynamic.fullGradient
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.*
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.centerGradient
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.devider
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.headBlur
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.hsvReadMore
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.ivDownloadFullList
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.ivDownloadFullListActionBar
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.ivFavorite
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.ivFavoriteActionBar
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.ivPlayAll
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.ivPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.iv_collapsingImageBg
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.llDetails
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.llDetails2
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.llPlayAll
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.llPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.llPlayAllAnim
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.llToolbar
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.playlistAlbumArtImageView
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.playlistAlbumArtImageViewLayer
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.playlistDetailroot
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.rlHeading
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.rvRecomendation
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.rvPlaylist
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.scrollView
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.shimmerLayout
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.topView
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.tvPlayAll
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.tvPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.tvReadMoreDescription
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.tvSubTitle
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.tvSubTitle2
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.tvTitle
import kotlinx.android.synthetic.main.fragment_playlist_detail_v2_dynamic.iv_banner

import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.HashMap

class PlaylistDetailFragmentDynamic : BaseFragment(),OnParentItemClickListener,BucketParentAdapter.OnMoreItemClick, TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseFragment.OnMenuItemClicked,
    BaseActivity.OnDownloadQueueItemChanged, OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var artImageUrl: String? = null
    var imageArray: ArrayList<String>? = null
    var variantImages: ArrayList<String>? = null

    //var artImageUrl = "https://files.hubhopper.com/podcast/313123/storytime-with-gurudev-sri-sri-ravi-shankar.jpg?v=1598432706&s=hungama"
    var selectedContentId: String? = null
    var queryParam: String? = ""
    var playerType: String? = null
    private var playlistDetailBgArtImageDrawable: LayerDrawable? = null

    var playlistListViewModel: PlaylistViewModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playlistSongList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    var ascending = true
    var playlistAdpter: DetailPlaylistAdapter? = null
    var artworkProminentColor = 0
    var artworkHeight = 0
    var userViewModel: UserViewModel? = null
    var isFavourite = false
    var bookmarkDataModel: BookmarkDataModel? = null
    var playlistModel: PlaylistDynamicModel? = null
    var isPlaying = false
    val isIconAnimation = false
    var isFromVerticalPlayer = false

    companion object {
        var playlistRespModel: PlaylistDynamicModel? = null
        var playlistSongItem: PlaylistModel.Data.Body.Row? = null
        var playableItemPosition = 0
        var varient: Int = 0
        fun newInstance(varient: Int): PlaylistDetailFragmentDynamic{
            val fragment = PlaylistDetailFragmentDynamic()
            this.varient = varient
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (activity != null && activity is MainActivity){
            BaseActivity.isNewSwipablePlayerOpen = false
            (requireActivity() as MainActivity).showBottomNavigationBar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        if (varient == 1) {
            return inflater.inflate(R.layout.fragment_playlist_detail_dynamic, container, false)
        } else {
            return inflater.inflate(R.layout.fragment_playlist_detail_v2_dynamic, container, false)
        }

    }

    override fun initializeComponent(view: View) {
        if (CommonUtils.isUserHasGoldSubscription()) {
            CommonUtils.setAppButton2(requireContext(), llPlayAll)
            CommonUtils.setAppButton2(requireContext(), llPlayAllActionBar)
        } else {
            if (varient == 1) {
                CommonUtils.setAppButton1(requireContext(), llPlayAll)
                CommonUtils.setAppButton1(requireContext(), llPlayAllActionBar)
            } else {
                CommonUtils.setAppButton4(requireContext(), llPlayAll)
                CommonUtils.setAppButton4(requireContext(), llPlayAllActionBar)
            }
        }
        Constant.screen_name ="PlayList Detail"
        setPlayAllButton()
        if (arguments != null) {
            selectedContentId = requireArguments().getString("id").toString()
            if (requireArguments()?.containsKey(Constant.EXTRA_QUERYPARAM)!!) {
                queryParam = requireArguments()?.getString(Constant.EXTRA_QUERYPARAM)!!
            }
            if (requireArguments().containsKey(Constant.isFromVerticalPlayer)) {
                isFromVerticalPlayer = requireArguments().getBoolean(Constant.isFromVerticalPlayer)
                setLog(TAG, "initializeComponent: selectedContentId " + selectedContentId)
            }
        }


        ivBack?.setOnClickListener { view -> backPress() }
        llToolbar.visibility = View.INVISIBLE
        rlHeading?.hide()

        scrollView.viewTreeObserver.addOnScrollChangedListener(this)


        rvPlaylist.visibility = View.VISIBLE

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        scrollView?.hide()
        setUpPlaylistDetailListViewModel()

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        llPlayAll?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireActivity(), it!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
            setLog(TAG, "initializeComponent: llPlayAll click")
            if (!playlistRespModel?.data?.body?.rows.isNullOrEmpty()) {
                setProgressBarVisible(true)
                playAllPlaylist()
            }

        }
        llPlayAllAnim?.setOnClickListener {
            llPlayAll?.performClick()
            setPlayAllButton()
        }
        llPlayAllActionBar?.setOnClickListener {

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireActivity(), llPlayAllActionBar!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
            if (!playlistRespModel?.data?.body?.rows.isNullOrEmpty()) {
                setProgressBarVisible(true)
                playAllPlaylist()
            }
        }
        Constant.screen_name ="Playlist Detail Dynamic"
        CommonUtils.getbanner(requireContext(),iv_banner,Constant.nudge_playlist_banner)

        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)
        ivFavorite?.setOnClickListener(this)
        ivFavoriteActionBar?.setOnClickListener(this)
        CommonUtils.setPageBottomSpacing(
            scrollView,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
    }


    fun setArtImageBg(status: Boolean) {

        if (activity != null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                artImageUrl
            ) && playlistDetailroot != null
        ) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
            val bgImage: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_bg_two)
            val gradient: Drawable? = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.audio_player_gradient_drawable
            )
            val result: Deferred<Bitmap?> = GlobalScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // get the downloaded bitmap
                    val bitmap: Bitmap? = result.await()

                    val artImage = BitmapDrawable(resources, bitmap)
                    if (status) {
                        if (bitmap != null) {
                            //val color = dynamicToolbarColor(bitmap)
                            Palette.from(bitmap!!).generate { palette ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                    /* (activity as AppCompatActivity).window.statusBarColor =
                                         palette?.getDominantColor(R.attr.colorPrimaryDark)!!*/
                                    artworkProminentColor =
                                        CommonUtils.calculateAverageColor(bitmap, 1)
                                    //(activity as AppCompatActivity).window.statusBarColor = artworkProminentColor
                                    MainScope().launch {
                                        if (context != null) {
                                            setLog(
                                                "PlaylistLifecycle",
                                                "setArtImageBg--$artworkProminentColor"
                                            )
                                            changeStatusbarcolor(artworkProminentColor)
                                        }
                                    }

                                }
                                MainScope().launch {
                                    try {
                                        val color2 =
                                            ColorDrawable(palette?.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))!!)
                                        //artworkProminentColor = palette.getDominantColor(R.attr.colorPrimaryDark)
                                        playlistDetailBgArtImageDrawable =
                                            LayerDrawable(
                                                arrayOf<Drawable>(
                                                    bgColor,
                                                    color2,
                                                    gradient!!
                                                )
                                            )
                                        playlistDetailroot?.background = bgColor
                                        var artWorkHeight = 0
                                        //artWorkHeight = (CommonUtils.getDeviceHeight(requireContext()) * 60) / 100
                                        //setLog("artWorkHeight", artWorkHeight.toString())
                                        val point = CommonUtils.getPointOfView(devider)
                                        if (point != null) {
                                            artWorkHeight =
                                                point.y - resources.getDimensionPixelSize(R.dimen.dimen_60)
                                        }
                                        setLog("artWorkHeight", artWorkHeight.toString())
                                        devider?.visibility = View.VISIBLE
                                        if (varient == 1) {
                                            iv_collapsingImageBg?.layoutParams?.height =
                                                artWorkHeight
                                            iv_collapsingImageBg?.requestLayout()
                                            iv_collapsingImageBg?.background = artImage
                                            centerGradient?.visibility = View.GONE
                                            fullGradient?.visibility = View.VISIBLE
                                        } else {
                                            /*val displayMetrics = DisplayMetrics()
                                            requireActivity().windowManager.defaultDisplay.getMetrics(
                                                displayMetrics
                                            )
                                            var scaleSize = displayMetrics.widthPixels

                                            val w = bitmap.getWidth()
                                            val h = bitmap.getHeight()
                                            val aspectRatio: Float = w.toFloat() / h.toFloat()
                                            val deviceAspectRatio =
                                                displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()*/

                                            // We need to adjust the height if the width of the bitmap is
                                            // smaller than the view width, otherwise the image will be boxed.
                                            iv_collapsingImageBg?.background = artImage
                                            val viewWidthToBitmapWidthRatio =
                                                iv_MainImage.getWidth()
                                                    .toDouble() / bitmap.getWidth().toDouble()
                                            artworkHeight =
                                                ((bitmap.getHeight() * viewWidthToBitmapWidthRatio).toInt())
                                            iv_MainImage?.getLayoutParams()?.height = artworkHeight
                                            centerGradient?.getLayoutParams()?.height =
                                                artworkHeight
                                            iv_MainImage?.background = artImage
                                            Utils.setMarginsTop(centerGradient, (artworkHeight / 2))
                                            Utils.setMarginsTop(llDetails, (artworkHeight / 2))
                                            centerGradient?.visibility = View.GONE
                                        }
                                    } catch (e: Exception) {

                                    }
                                }
                            }

                        }

                    }
                } catch (e: Exception) {

                }


            }
        }

    }

    fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }

    private fun staticToolbarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
            MainScope().launch {
                if (context != null) {
                    changeStatusbarcolor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.home_bg_color
                        )
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
        return onCreateOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_download_video -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        /*if (isDownloaded){
            menu!!.findItem(R.id.action_download_video).title = resources.getString(R.string.downloaded)
        }else{
            menu!!.findItem(R.id.action_download_video).title = resources.getString(R.string.download)
        }*/

        return onPrepareOptionsMenu(menu)
    }

    private fun playAllPlaylist() {
        setLog(TAG, "playAllPlaylist: isPlaying:${isPlaying} playlistSongList:${playlistSongList?.size}")
        BaseActivity.setTouchData()
        if (isPlaying) {
            if (!playlistSongList.isNullOrEmpty()) {
                playableItemPosition = 0

                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                    ?.findByContentId(playlistSongList?.get(0)?.data?.id!!.toString())
                if (downloadedAudio != null && downloadedAudio?.contentId.equals(
                        playlistSongList?.get(0)?.data?.id!!.toString()
                    )
                ) {
                    val playableContentModel = PlayableContentModel()
                    playableContentModel.data?.head?.headData?.id =
                        downloadedAudio.contentId!!
                    playableContentModel.data?.head?.headData?.misc?.url =
                        downloadedAudio.downloadedFilePath
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url =
                        downloadedAudio.downloadUrl!!
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                        downloadedAudio.drmLicense
                    playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                        downloadedAudio.lyricsUrl
                    setPlayableContentListData(playableContentModel)
                } else {
                    setUpPlayableContentListViewModel(playlistSongList?.get(0)?.data?.id!!)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val hashMapPageView = HashMap<String, String>()

                    hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = playlistSongList.get(0).data.title
                    hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                        "" + Utils.getContentTypeNameForStream("" + playlistSongList?.get(0)?.data?.type)
                    hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = playlistSongList.get(0).data.id
                    hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                    hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_details_playList"
                    hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "details_playList"

                    setLog("VideoPlayerPageView", hashMapPageView.toString())
                    EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                }


                try {
                    setEventModelDataAppLevel(
                        playlistSongList?.get(0)?.data?.id!!,
                        playlistSongList?.get(0)?.data?.title!!,
                        playlistRespModel?.data?.head?.data?.title!!
                    )
                } catch (e: Exception) {

                }
            }
        } else {
            (requireActivity() as MainActivity).pausePlayer()
            playPauseStatusChange(true)
        }

    }

    private fun setUpPlaylistDetailListViewModel() {

        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            playlistListViewModel = ViewModelProvider(
                this
            ).get(PlaylistViewModel::class.java)
            playlistListViewModel?.getPlaylistDetailListDynamic(
                requireContext(),
                selectedContentId!!,queryParam)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
//                                if(Constant.IS_SEARCH_RECOMMENDED_DISPLAY){
//                                    getTrendCall(it?.data!!)
//                                }else{
                                    fillPlaylistData(it?.data!!)
//                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
                                loadOfflinePlaylist()
                            }
                        }
                    })
        }else{
            loadOfflinePlaylist()
        }
    }

    private fun fillPlaylistData(data: PlaylistDynamicModel) {

        playlistModel = data
        playlistDetailroot.visibility = View.VISIBLE
        setPlaylistDetailsListData(data!!)
        setDetails(data, true)
    }

    fun getTrendCall(homedata: PlaylistDynamicModel?) {
        val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url= WSConstants.METHOD_TRENDING_PLAYLIST+"?contentId="+selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(),url)?.observe(this,
            Observer {
                when(it.status){
                    Status.SUCCESS->{

                        setLog(TAG, "getTrendPodcastCall: data:${it?.data} ")
                        setLog(TAG, "getTrendPodcastCall: before size:${homedata?.data?.body?.recomendation?.size}")

                        var dailyDoseIndex=0
                        var podcastSize=homedata?.data?.body?.recomendation?.size
                        if(it?.data!=null&&it?.data?.data?.body!=null&&it?.data?.data?.body?.searchRecommendations?.size!!>0){
                            it?.data?.data?.body?.searchRecommendations?.forEach {
                                if (podcastSize != null) {
                                    homedata?.data?.body?.recomendation?.add(podcastSize+dailyDoseIndex,it)
                                }
                                dailyDoseIndex+=1

                                setLog(TAG, "getTrendPodcastCall: dailyDoseIndex:${dailyDoseIndex} ")
                            }
                        }

                        getYouMayLikeCall(homedata)




                        setLog(TAG, "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}")
                    }

                    Status.LOADING ->{
                        setProgressBarVisible(true)
                    }

                    Status.ERROR ->{
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        fillPlaylistData(homedata!!)
                    }
                }
            })
    }

    fun getYouMayLikeCall(homedata: PlaylistDynamicModel?) {
        val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url= WSConstants.METHOD_YOU_MAY_LIKE_PLAYLIST+"?contentId="+selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
            Observer {
                when(it.status){
                    Status.SUCCESS->{

                        setLog(TAG, "getYouMayLikePodcastCall: data:${it?.data} ")
                        setLog(TAG, "getYouMayLikePodcastCall: before size:${homedata?.data?.body?.recomendation?.size}")

                        var dailyDoseIndex=0
                        var podcastSize=homedata?.data?.body?.recomendation?.size
                        if(it?.data!=null&&it?.data?.data?.body!=null&&it?.data?.data?.body?.searchRecommendations?.size!!>0){
                            it?.data?.data?.body?.searchRecommendations?.forEach {
                                if (podcastSize != null) {
                                    homedata?.data?.body?.recomendation?.add(podcastSize+dailyDoseIndex,it)
                                }
                                dailyDoseIndex+=1

                                setLog(TAG, "getYouMayLikePodcastCall: dailyDoseIndex:${dailyDoseIndex} ")
                            }
                        }
                        fillPlaylistData(homedata!!)



                        setLog(TAG, "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}")
                    }

                    Status.LOADING ->{
                        setProgressBarVisible(true)
                    }

                    Status.ERROR ->{
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        fillPlaylistData(homedata!!)
                    }
                }
            })
    }

    fun loadOfflinePlaylist(){
        val playListoffline = AppDatabase.getInstance()?.downloadedAudio()
            ?.getPlayList(selectedContentId.toString()) as ArrayList<DownloadedAudio>
        val playlistDynamicModel = PlaylistDynamicModel()
        if (!playListoffline.isNullOrEmpty()){
            playlistDynamicModel.data.head.itype = 2
            playlistDynamicModel.data.head.data.id = playListoffline.get(0).parentId.toString()
            playlistDynamicModel.data.head.data.title = playListoffline.get(0).pName.toString()
            playlistDynamicModel.data.head.data.type = 55555
            playlistDynamicModel.data.head.data.image = playListoffline.get(0).parentThumbnailPath.toString()
            playlistDynamicModel.data.head.data.releasedate = playListoffline.get(0).pReleaseDate.toString()
            playlistDynamicModel.data.head.data.subtitle = playListoffline.get(0).pSubName.toString()
        }
        val playlistItems: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
        playListoffline.forEachIndexed { index, da ->
            try {
                val row = PlaylistModel.Data.Body.Row()
                row.itype = 8
                row.data.id = da.contentId.toString()
                row.data.title = da.title.toString()
                row.data.subtitle = da.subTitle.toString()
                row.data.type = da.type!!
                row.data.image = da.thumbnailPath.toString()
                row.data.releasedate = da.releaseDate.toString()
                row.data.genre = CommonUtils.getStringToArray(da.genre.toString())
                if (da.duration != null){
                    row.data.duration = da.duration.toString()
                }
                row.data.misc.explicit = da.explicit!!
                row.data.misc.restricted_download = da.restrictedDownload
                row.data.misc.pid = CommonUtils.getStringToIntArray(da.pid.toString())
                row.data.misc.p_name = CommonUtils.getStringToArray(da.pName.toString())
                row.data.misc.singerf = CommonUtils.getStringToArray(da.singer.toString())
                row.data.misc.actorf = CommonUtils.getStringToArray(da.actor.toString())
                row.data.misc.artist = CommonUtils.getStringToArray(da.artist.toString())
                row.data.misc.attributeCensorRating = CommonUtils.getStringToArray(da.attribute_censor_rating.toString())
                row.data.misc.description = da.description.toString()
                if (da.f_fav_count != null){
                    row.data.misc.favCount = ""+da.f_fav_count!!
                }
                if (da.f_playcount != null){
                    row.data.misc.playcount = da.f_playcount!!
                }
                row.data.misc.lang = CommonUtils.getStringToArray(da.language.toString())
                row.data.misc.movierights = CommonUtils.getStringToArray(da.movierights.toString())
                if (da.nudity != null){
                    row.data.misc.nudity = da.nudity!!
                }

                if (!TextUtils.isEmpty(da.criticRating)){
                    row.data.misc.ratingCritic = (da.criticRating)?.toDouble()!!
                }
                row.data.misc.sArtist = CommonUtils.getStringToArray(da.s_artist.toString())
                row.data.misc.synopsis = da.synopsis.toString()
                row.data.misc.share = da.contentShareLink
                playlistItems.add(row)
            }catch (e:Exception){
                val row = PlaylistModel.Data.Body.Row()
                row.itype = 8
                row.data.id = da.contentId.toString()
                row.data.title = da.title.toString()
                row.data.subtitle = da.subTitle.toString()
                row.data.type = da.type!!
                row.data.image = da.thumbnailPath.toString()
                row.data.misc.restricted_download = da.restrictedDownload
                row.data.misc.share = da.contentShareLink
                playlistItems.add(row)
            }

        }
        playlistDynamicModel.data.body.rows = playlistItems
        playlistModel = playlistDynamicModel
        playlistDetailroot.visibility = View.VISIBLE
        setPlaylistDetailsListData(playlistDynamicModel)
        setDetails(playlistDynamicModel, true)
    }
    fun setPlaylistDetailsListData(playlistModel: PlaylistDynamicModel) {

        if (playlistModel != null && playlistModel?.data?.body != null) {

            setupUserViewModel()
            if (!playlistModel.data.body.rows.isNullOrEmpty()) {
                playlistSongList = playlistModel.data.body.rows
                ivDownloadFullList.setOnClickListener(this)
                ivDownloadFullListActionBar.setOnClickListener(this)
                //tvSongsCount.setText(""+playlistSongList?.size+" Songs")
                rvPlaylist.apply {
                    layoutManager =
                        GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                    //setRecycledViewPool(RecyclerView.RecycledViewPool())
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)

                }
                setPlayListSongAdapter(ascending)

            }

            if (playlistModel.data?.body?.recomendation != null && playlistModel.data?.body?.recomendation?.size!! > 0) {
                rvRecomendation.visibility = View.VISIBLE
                rvRecomendation.visibility = View.VISIBLE

                var varient = Constant.ORIENTATION_HORIZONTAL

                val bucketParentAdapter = BucketParentAdapter(
                    playlistModel.data?.body?.recomendation!!,
                    requireContext(),
                    this@PlaylistDetailFragmentDynamic,
                    this,
                    Constant.WATCH_TAB,
                    HeadItemsItem(),
                    varient
                )

                val mLayoutManager = LinearLayoutManager(
                    activity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                rvRecomendation?.layoutManager = mLayoutManager
                rvRecomendation?.adapter = bucketParentAdapter


                bucketParentAdapter?.addData(playlistModel.data?.body?.recomendation!!)
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                rvRecomendation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                        val lastVisiable: Int =
                            mLayoutManager?.findLastCompletelyVisibleItemPosition()!!

                        setLog(
                            TAG,
                            "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                        )
                        if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                            var fromBucket =
                                playlistModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                            var toBucket =
                                playlistModel.data?.body?.recomendation?.get(lastVisiable)?.heading
                            var sourcePage =
                                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                            if (!fromBucket?.equals(toBucket, true)!!) {
                                callPageScrolledEvent(
                                    sourcePage,
                                    "" + lastVisiable,
                                    fromBucket!!,
                                    toBucket!!
                                )
                            }

                        }
                    }
                })


                rvPlaylist.setPadding(0, 0, 0, 0)
                rvRecomendation.setPadding(0, 0, 0, 0)
            }

            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            rlHeading?.show()
            scrollView?.show()
            setPlayAllButton()
        }
    }

    private fun setPlayListSongAdapter(asc: Boolean) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                playlistAdpter = DetailPlaylistAdapter(
                    requireContext(), playlistSongList,
                    object : DetailPlaylistAdapter.OnChildItemClick {
                        override fun onUserClick(
                            childPosition: Int,
                            isMenuClick: Boolean,
                            isDownloadClick: Boolean
                        ) {
                            playableItemPosition = childPosition
                            if (isMenuClick) {
                                if (isOnClick()) {
                                    playlistSongItem = playlistSongList?.get(childPosition)
                                    val isFavorite =
                                        playlistSongList?.get(childPosition)?.data?.isFavorite!!
                                    commonThreeDotMenuItemSetup(
                                        PLAYLIST_DETAIL_ADAPTER,
                                        this@PlaylistDetailFragmentDynamic,
                                        isFavorite
                                    )
                                }
                            } else if (isDownloadClick)
                            {
                                val dpm = DownloadPlayCheckModel()
                                dpm.contentId =
                                    playlistSongList?.get(childPosition)?.data?.id?.toString()!!
                                dpm.contentTitle =
                                    playlistSongList?.get(childPosition)?.data?.title?.toString()!!
                                dpm.planName =
                                    playlistSongList?.get(childPosition)?.data?.misc?.movierights.toString()
                                dpm.isAudio = true
                                dpm.isDownloadAction = true
                                dpm.isShowSubscriptionPopup = true
                                dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                                dpm.restrictedDownload = RestrictedDownload.valueOf(playlistSongList?.get(childPosition)?.data?.misc?.restricted_download!!)
                                if (CommonUtils.userCanDownloadContent(
                                        requireContext(),
                                        playlistDetailroot,
                                        dpm,
                                        this@PlaylistDetailFragmentDynamic,Constant.drawer_restricted_download)) {
                                    val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                                    var dq = DownloadQueue()
                                    //for (item in playlistSongList?.iterator()!!){

                                    dq = DownloadQueue()
                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.id.toString())) {
                                        dq.contentId =
                                            playlistSongList?.get(childPosition)?.data?.id.toString()
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.title!!)) {
                                        dq.title =
                                            playlistSongList?.get(childPosition)?.data?.title!!
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.subtitle!!)) {
                                        dq.subTitle =
                                            playlistSongList?.get(childPosition)?.data?.subtitle!!
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.image!!)) {
                                        dq.image =
                                            playlistSongList?.get(childPosition)?.data?.image!!
                                    }

                                    if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.id!!)) {
                                        dq.parentId = playlistRespModel?.data?.head?.data?.id!!
                                    }
                                    if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.title!!)) {
                                        dq.pName = playlistRespModel?.data?.head?.data?.title
                                    }

                                    if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.subtitle!!)) {
                                        dq.pSubName = playlistRespModel?.data?.head?.data?.subtitle
                                    }

                                    if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.releasedate!!)) {
                                        dq.pReleaseDate =
                                            playlistRespModel?.data?.head?.data?.releasedate
                                    }

                                    if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.image!!)) {
                                        dq.pImage = playlistRespModel?.data?.head?.data?.image
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.misc?.movierights.toString()!!)) {
                                        dq.planName =
                                            playlistSongList?.get(childPosition)?.data?.misc?.movierights.toString()
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.misc?.f_FavCount.toString()!!)) {
                                        dq.f_fav_count =
                                            playlistSongList?.get(childPosition)?.data?.misc?.f_FavCount.toString()
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.misc?.f_playcount.toString()!!)) {
                                        dq.f_playcount =
                                            playlistSongList?.get(childPosition)?.data?.misc?.f_playcount.toString()
                                    }

                                    dq.pType = DetailPages.PLAYLIST_DETAIL_PAGE.value
                                    dq.contentType = ContentTypes.AUDIO.value
                                    val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                                    dq.source = eventModel.sourceName

                                    val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                        ?.findByContentId(playlistSongList?.get(childPosition)?.data?.id!!.toString())
                                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                            ?.findByContentId(playlistSongList?.get(childPosition)?.data?.id!!.toString())
                                    if ((!downloadQueue?.contentId.equals(
                                            playlistSongList?.get(
                                                childPosition
                                            )?.data?.id!!.toString()
                                        ))
                                        && (!downloadedAudio?.contentId.equals(
                                            playlistSongList?.get(
                                                childPosition
                                            )?.data?.id!!.toString()
                                        ))
                                    ) {
                                        downloadQueueList.add(dq)
                                    }
                                    // }
                                    //if (downloadQueueList.size > 0){
                                    (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                                        downloadQueueList,
                                        this@PlaylistDetailFragmentDynamic,
                                        null,
                                        false,
                                        true
                                    )
                                    //}
                                }

                            } else {
                                if (isOnClick()) {
                                    val downloadedAudio =
                                        AppDatabase.getInstance()?.downloadedAudio()
                                            ?.findByContentId(playlistSongList?.get(childPosition)?.data?.id!!.toString())
                                    if (downloadedAudio != null && downloadedAudio?.contentId.equals(
                                            playlistSongList?.get(childPosition)?.data?.id!!.toString()
                                        )
                                    ) {
                                        val playableContentModel = PlayableContentModel()
                                        playableContentModel.data?.head?.headData?.id =
                                            downloadedAudio.contentId!!
                                        playableContentModel.data?.head?.headData?.misc?.url =
                                            downloadedAudio.downloadedFilePath
                                        playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url =
                                            downloadedAudio.downloadUrl!!
                                        playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                                            downloadedAudio.drmLicense
                                        playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                                            downloadedAudio.lyricsUrl
                                        setPlayableContentListData(playableContentModel)
                                    } else {
                                        setUpPlayableContentListViewModel(
                                            playlistSongList?.get(
                                                childPosition
                                            )?.data?.id!!
                                        )
                                        try {
                                            setEventModelDataAppLevel(
                                                playlistSongList?.get(childPosition)?.data?.id!!,
                                                playlistSongList?.get(childPosition)?.data?.title!!,
                                                playlistModel?.data?.head?.data?.title!!
                                            )
                                        } catch (e: Exception) {

                                        }
                                    }
                                }

                            }


                        }


                    })
                rvPlaylist.adapter = playlistAdpter

                var isContentAutoPlay = 0
                if (requireArguments()?.containsKey("isPlay") == true) {
                    isContentAutoPlay = requireArguments().getInt("isPlay")
                }
                setLog(TAG, "setPlayListSongAdapter: auto play${isContentAutoPlay}")
                if (isContentAutoPlay == 1) {
                    llPlayAll?.performClick()
                }

                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-0")
                async { checkAllContentDownloadedOrNot(playlistSongList) }.await()
                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-100")
                withContext(Dispatchers.IO) {
                    delay(3000)
                    setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-101")
                    playlistSongList = getAdsData(playlistSongList)
                    setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-104")
                    withContext(Dispatchers.Main) {
                        setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-105"
                        )
                        if (isAdded && context != null && playlistAdpter != null) {
                            setLog(
                                "isCurrentContentPlayingFromThis",
                                "isCurrentContentPlayingFromThis-106"
                            )
                            playlistAdpter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private fun setDetails(it: PlaylistDynamicModel?, status: Boolean) {
        playlistRespModel = it
        playerType = "" + playlistRespModel?.data?.head?.data?.type
        artImageUrl = "" + playlistRespModel?.data?.head?.data?.image

        if (requireArguments()?.containsKey("variant_images") == true) {
            variantImages = requireArguments().getStringArrayList("variant_images")
            if (variantImages != null && variantImages?.size!! > 0 && !variantImages?.get(0)?.isNullOrBlank()!!) {
                artImageUrl = variantImages?.get(0)
            }
        }

        if (varient == 1) {
            if (!TextUtils.isEmpty(artImageUrl)) {
                ImageLoader.loadImage(
                    requireContext(),
                    playlistAlbumArtImageView,
                    artImageUrl!!,
                    R.drawable.bg_gradient_placeholder
                )

                if (imageArray != null && imageArray?.size!! > 0) {
                    val turl = imageArray?.get((0..imageArray?.size!! - 1).random())
                    ImageLoader.loadImage(
                        requireContext(),
                        playlistAlbumArtImageViewLayer,
                        turl!!,
                        R.drawable.bg_gradient_placeholder
                    )
                    playlistAlbumArtImageViewLayer.visibility = View.VISIBLE
                    artImageUrl = turl
                } else {
//                    playlistAlbumArtImageViewLayer.visibility = View.GONE
                }
                setArtImageBg(true)
            } else {
                ImageLoader.loadImage(
                    requireContext(),
                    playlistAlbumArtImageView,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                staticToolbarColor()
            }
        } else {
            if (!TextUtils.isEmpty(artImageUrl)) {
                playlistAlbumArtImageView.visibility = View.GONE
                setArtImageBg(true)
            } else {
                ImageLoader.loadImage(
                    requireContext(),
                    playlistAlbumArtImageView,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                staticToolbarColor()
            }
        }

        llDetails2.visibility = View.VISIBLE
        if (it?.data?.head?.data?.title != null && !TextUtils.isEmpty(it?.data?.head?.data?.title)) {
            tvTitle.text = it?.data?.head?.data?.title
        } else {
            tvTitle.text = ""
        }

        if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.description?.trim())) {
            tvReadMoreDescription.text = it?.data?.head?.data?.misc?.description
            SaveState.isCollapse = true
            tvReadMoreDescription.setShowingLine(2)
            tvReadMoreDescription.addShowMoreText("read more")
            tvReadMoreDescription.addShowLessText("read less")
            tvReadMoreDescription.setShowMoreColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            tvReadMoreDescription.setShowLessTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            tvReadMoreDescription.setShowMoreStyle(Typeface.BOLD)

            hsvReadMore.visibility = View.GONE
            tvReadMoreDescription.visibility = View.VISIBLE

        } else {
            hsvReadMore.visibility = View.GONE
            tvReadMoreDescription.visibility = View.GONE
        }
        tvSubTitle.text = getString(R.string.chart_str_1)

        if (it?.data?.head?.data?.misc != null) {
            var subtitle = ""
            if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.f_playcount)) {
                subtitle += it?.data?.head?.data?.misc?.f_playcount + " " + getString(R.string.artist_str_4) + " • "
            }
            if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.f_FavCount)) {
                subtitle += CommonUtils.ratingWithSuffix(it?.data?.head?.data?.misc?.items!!.toString()) + " " + getString(
                    R.string.library_playlist_str_8
                )
            }
            tvSubTitle2.text = subtitle
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        MainScope().launch {
            if (context != null) {
                changeStatusbarcolor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.home_bg_color
                    )
                )
            }
        }
        if (isFromVerticalPlayer){
            if (activity != null){
                BaseActivity.isNewSwipablePlayerOpen = true
                (activity as MainActivity).hideMiniPlayer()
                (activity as MainActivity).hideStickyAds()
            }
        }
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        if(isAdded()&&getViewActivity()!=null){
            val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)

            /*if (BaseActivity?.songDataList != null && BaseActivity?.songDataList?.size!! > 0) {
                intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
            } else {*/
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            //}

            Util.startForegroundService(getViewActivity(), intent)
            (activity as MainActivity).reBindService()

            playPauseStatusChange(false)
        }

    }

    override fun getViewActivity(): AppCompatActivity {
        return requireActivity() as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setUpPlayableContentListViewModel(id: String) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                        setPlayableContentListData(it?.data)
                                    } else {
                                        playableItemPosition = playableItemPosition + 1
                                        if (playableItemPosition < playlistSongList?.size!!) {
                                            setUpPlayableContentListViewModel(
                                                playlistSongList?.get(
                                                    playableItemPosition
                                                )?.data?.id!!
                                            )
                                            setEventModelDataAppLevel(
                                                playlistSongList?.get(playableItemPosition)?.data?.id!!,
                                                playlistSongList?.get(
                                                    playableItemPosition
                                                )?.data?.title!!,
                                                playlistModel?.data?.head?.data?.title!!
                                            )
                                        }
                                    }
                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR -> {
                                setProgressBarVisible(false)
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
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


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            if (!CommonUtils.checkExplicitContent(
                    requireContext(),
                    playableContentModel.data.head.headData.misc.explicit
                )
            ) {
                setLog(
                    "PlayableItem",
                    "setPlayableContentListData id:" + playableContentModel?.data?.head?.headData?.id.toString()
                )
                setLog(
                    "PlayableItem",
                    "setSongLyricsData setPlayableContentListData" + playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link.toString()
                )
                songDataList = arrayListOf()

                for (i in playlistSongList?.indices!!) {
                    if (playableContentModel?.data?.head?.headData?.id == playlistSongList?.get(
                            i
                        )?.data?.id
                    ) {
                        setPlaylistSongList(
                            playableContentModel,
                            playlistSongList,
                            playableItemPosition
                        )
                    } else if (i > playableItemPosition) {
                        setPlaylistSongList(null, playlistSongList, i)
                    }
                }

                /*if (BaseActivity?.songDataList != null && BaseActivity?.songDataList?.size!! > 0) {
                    *//*(activity as BaseActivity).nowPlayingQueue?.addAlbumToQueue(
                    songDataList,
                    tracksViewModel
                )*//*
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    //tracksViewModel.prepareTrackPlayback(BaseActivity.queueNowPlayIndex)
                    tracksViewModel.prepareTrackPlayback(0)
                }, 2000)

            } else {*/
                BaseActivity.setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(0)
                //}
            }
        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlaylistSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row?>?,
        position: Int
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            track.id = playableItem?.get(position)?.data?.id!!.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)) {
            track.title = playableItem?.get(position)?.data?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subtitle)) {
            track.subTitle = playableItem?.get(position)?.data?.subtitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }
        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }


        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl =
                playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.type.toString())) {
            track.playerType = playableItem?.get(position)?.data?.type.toString()
        } else {
            track.playerType = MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.title)) {
            track.heading = playlistRespModel?.data?.head?.data?.title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.playble_image)) {
            track.image = playableItem?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)) {
            track.image = playableItem?.get(position)?.data?.image
        } else {
            track.image = ""
        }

        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.id!!)) {
            track.parentId = playlistRespModel?.data?.head?.data?.id!!
        }
        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.title!!)) {
            track.pName = playlistRespModel?.data?.head?.data?.title
        }

        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.subtitle!!)) {
            track.pSubName = playlistRespModel?.data?.head?.data?.subtitle
        }

        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.image!!)) {
            track.pImage = playlistRespModel?.data?.head?.data?.image
        }

        track.pType = DetailPages.PLAYLIST_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null) {
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null) {
            track.restrictedDownload =
                playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
        return songDataList
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = 0
            if (varient == 1) {
                maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_373)
            } else {
                maxDistance += topView.marginTop + llDetails.marginTop + llDetails.marginBottom + llDetails.height + llDetails2.marginTop + llDetails2.marginBottom + llDetails2.height + resources.getDimensionPixelSize(
                    R.dimen.dimen_10_5
                )
                //maxDistance  += (artworkHeight / 2) + resources.getDimensionPixelSize(R.dimen.dimen_127)
            }
            /* how much we have scrolled */
            val movement = scrollView.scrollY

            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance) {
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.INVISIBLE
                llToolbar.visibility = View.VISIBLE
                if (artworkProminentColor == 0) {
                    rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                } else {
                    rlHeading.setBackgroundColor(artworkProminentColor)
                }
            } else {
                //setLog("OnNestedScroll-m--", movement.toString())
                //setLog("OnNestedScroll-d--", maxDistance.toString())
                llToolbar.visibility = View.INVISIBLE
                headBlur.visibility = View.INVISIBLE
                rlHeading.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.transparent
                    )
                )
            }
            /*finally calculate the alpha factor and set on the view */
            /*val alphaFactor: Float =
                movement * 1.0f / (maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63))
            if (movement >= 0 && movement <= maxDistance) {
                *//*for image parallax with scroll *//*
                // iv_collapsingImageBg.setTranslationY((-movement / 2).toFloat())
                *//* set visibility *//*
                //toolbar.setAlpha(alphaFactor)
                //llToolbar.alpha = alphaFactor

                if (alphaFactor > 1) {
                    headBlur.visibility = View.VISIBLE
                    llToolbar.visibility = View.VISIBLE
                    rlHeading.setBackgroundColor(artworkProminentColor)
                    setLog("aaaaa1", alphaFactor.toString())
                } else {
                    llToolbar.visibility = View.INVISIBLE
                    headBlur.visibility = View.INVISIBLE
                    rlHeading.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.transparent
                        )
                    )
                    setLog("aaaaa2", alphaFactor.toString())
                }
            }*/
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == threeDotMenu || v == threeDotMenu2) {
            commonThreeDotMenuItemSetup(PLAYLIST_DETAIL_PAGE)
        } else if (v == ivFavorite || v == ivFavoriteActionBar) {
            playlistRespModel?.let {
                setAddOrRemoveFavourite(it)
            }

        } else if (v == ivDownloadFullList || v == ivDownloadFullListActionBar) {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = playlistRespModel?.data?.head?.data?.id.toString()
            dpm.planName = PlanNames.NONE.name
            dpm.isAudio = true
            dpm.isDownloadAction = true
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_ALL_CONTENT
            dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
            if (CommonUtils.userCanDownloadContent(
                    requireContext(),
                    playlistDetailroot,
                    dpm,
                    this,Constant.drawer_download_all
                )
            ) {
                val contentTypes: Array<Int> =
                    arrayOf(ContentTypes.AUDIO.value, ContentTypes.PODCAST.value)
                val allDownloadItemList = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getContentsByContentType(contentTypes)
                var existingQueueItemsCount = 0
                if (allDownloadItemList != null) {
                    existingQueueItemsCount = allDownloadItemList.size
                }

                val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                var dq = DownloadQueue()
                var count = 1
                val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                for (item in playlistSongList?.iterator()!!) {
                    if (!TextUtils.isEmpty(item?.data?.id)) {
                        dq = DownloadQueue()

                        dq.contentId = item?.data?.id


                        if (!TextUtils.isEmpty(item?.data?.title)) {
                            dq.title = item?.data?.title!!
                        }

                        if (!TextUtils.isEmpty(item?.data?.subtitle!!)) {
                            dq.subTitle = item?.data?.subtitle!!
                        }

                        if (!TextUtils.isEmpty(item?.data?.image!!)) {
                            dq.image = item?.data?.image!!
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.id!!)) {
                            dq.parentId = playlistRespModel?.data?.head?.data?.id!!
                        }
                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.title!!)) {
                            dq.pName = playlistRespModel?.data?.head?.data?.title
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.subtitle!!)) {
                            dq.pSubName = playlistRespModel?.data?.head?.data?.subtitle
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.releasedate!!)) {
                            dq.pReleaseDate = playlistRespModel?.data?.head?.data?.releasedate
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.image!!)) {
                            dq.pImage = playlistRespModel?.data?.head?.data?.image
                        }

                        dq.pType = DetailPages.PLAYLIST_DETAIL_PAGE.value
                        dq.contentType = ContentTypes.AUDIO.value
                        dq.downloadAll = 1
                        dq.source = eventModel.sourceName

                        if (!TextUtils.isEmpty(item?.data?.misc?.movierights.toString()!!)) {
                            dq.planName = item?.data?.misc?.movierights.toString()
                        }

                        if (!TextUtils.isEmpty(item?.data?.misc?.f_FavCount.toString()!!)) {
                            dq.f_fav_count = item?.data?.misc?.f_FavCount.toString()
                        }

                        if (!TextUtils.isEmpty(item?.data?.misc?.f_playcount.toString()!!)) {
                            dq.f_playcount = item?.data?.misc?.f_playcount.toString()
                        }

                        if (existingQueueItemsCount > CommonUtils.getAvailableDownloadContentSize(
                                requireContext()
                            )
                        ) {
                            break
                        }
                        existingQueueItemsCount += 1

                        if (!AppDatabase.getInstance()?.downloadQueue()
                                ?.findByContentId(item?.data?.id.toString())?.contentId.equals(item?.data?.id.toString())
                            && !AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(item?.data?.id.toString())?.contentId.equals(item?.data?.id.toString())
                        ) {
                            //if (count <= 3){
                            downloadQueueList.add(dq)
                            //}
                        }
                    }
                    count++
                }
                //if (downloadQueueList.size > 0){
                (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                    downloadQueueList,
                    this,
                    null,
                    false,
                    true
                )
                //}
                /*val drawable = FontDrawable(requireContext(), R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                ivDownloadFullList.setImageDrawable(drawable)
                ivDownloadFullListActionBar.setImageDrawable(drawable)*/
                ivDownloadFullList.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_downloading,
                        R.color.colorWhite
                    )
                )
                ivDownloadFullListActionBar.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_downloading,
                        R.color.colorWhite
                    )
                )

            }
        }
    }

    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getUserBookmarkedData()
    }

    private fun setAddOrRemoveFavourite(playlistRespModel: PlaylistDynamicModel) {
        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            isFavourite = !isFavourite
            val jsonObject = JSONObject()
            jsonObject.put("contentId", playlistRespModel?.data?.head?.data?.id!!)
            jsonObject.put("typeId", playlistRespModel?.data?.head?.data?.type!!)
            jsonObject.put("action", isFavourite)
            jsonObject.put("module", MODULE_FAVORITE)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()

            if (isFavourite) {
                val messageModel = MessageModel(
                    getString(R.string.toast_str_46),
                    getString(R.string.library_playlist_str_25),
                    MessageType.NEUTRAL,
                    true
                )
                CommonUtils.showToast(requireContext(), messageModel)
                CoroutineScope(Dispatchers.IO).async {
                    val hashMap = java.util.HashMap<String, String>()
                    hashMap.put(
                        EventConstant.ACTOR_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.misc?.actorf!!)
                    )
                    hashMap.put(
                        EventConstant.ALBUMID_EPROPERTY,
                        "" + playlistRespModel?.data?.head?.data?.id
                    )
                    hashMap.put(
                        EventConstant.CATEGORY_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.category!!)
                    )
                    var newContentId= playlistRespModel?.data?.head?.data?.id!!
                    var contentIdData=newContentId.replace("playlist-","")
                    hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)

                    val contentType=playlistRespModel?.data?.head?.data?.type!!
                    setLog(
                        TAG,
                        "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" +contentType)} contentType:${contentType}"
                    )
                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "" + Utils.getContentTypeName("" +contentType))

                    hashMap.put(
                        EventConstant.GENRE_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.genre!!)
                    )
                    hashMap.put(
                        EventConstant.LANGUAGE_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.misc?.lang!!)
                    )
                    hashMap.put(
                        EventConstant.LYRICIST_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.misc?.lyricist!!)
                    )
                    hashMap.put(
                        EventConstant.MOOD_EPROPERTY,
                        "" + playlistRespModel?.data?.head?.data?.misc?.mood
                    )
                    hashMap.put(
                        EventConstant.MUSICDIRECTOR_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.misc?.musicdirectorf!!)
                    )
                    hashMap.put(
                        EventConstant.NAME_EPROPERTY,
                        "" + playlistRespModel?.data?.head?.data?.title
                    )
                    hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "")
                    hashMap.put(
                        EventConstant.SINGER_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.misc?.singerf!!)
                    )
                    hashMap.put(
                        EventConstant.SOURCE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + playlistRespModel?.data?.head?.data?.title
                    )
                    hashMap.put(
                        EventConstant.TEMPO_EPROPERTY,
                         Utils.arrayToString(playlistRespModel?.data?.head?.data?.misc?.tempo!!)
                    )
                    hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                    hashMap.put(
                        EventConstant.YEAROFRELEASE_EPROPERTY,
                        "" + DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_YYYY,
                            playlistRespModel?.data?.head?.data?.releasedate
                        )
                    )
                    EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
                }

            } else {
                val messageModel = MessageModel(
                    getString(R.string.library_playlist_str_26),
                    getString(R.string.library_playlist_str_27),
                    MessageType.NEUTRAL,
                    true
                )
                CommonUtils.showToast(requireContext(), messageModel)
            }


        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun getUserBookmarkedData() {
        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            userViewModel?.getUserBookmarkedData(requireContext(), MODULE_FAVORITE, "55555")
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    fillUI(it?.data)
                                }

                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
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


    private fun fillUI(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0) {
            for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!) {
                if (playlistRespModel?.data?.head?.data?.id?.equals(bookmark?.data?.id)!!) {
                    setLog(TAG, "fillUI: after" + isFavourite)
                    isFavourite = true
                    setLog(TAG, "fillUI:before " + isFavourite)
                }
                if (!playlistSongList.isNullOrEmpty()) {
                    playlistSongList?.forEachIndexed { index, row ->
                        if (row?.data?.id?.equals(bookmark?.data?.id)!!) {
                            row?.data?.isFavorite = true
                        }
                    }
                }
            }
            setFollowingStatus()
        }
    }

    fun setFollowingStatus() {
        if (isFavourite) {
            ivFavorite?.playAnimation()
            ivFavoriteActionBar?.playAnimation()
        } else {
            ivFavorite?.cancelAnimation()
            ivFavorite?.progress = 0f
            ivFavoriteActionBar?.cancelAnimation()
            ivFavoriteActionBar?.progress = 0f
        }

    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        super.onContentLikedFromThreeDotMenu(isFavorite, position)
        if (playlistSongList != null && playlistSongList?.size!! > 0) {
            playlistSongList?.get(position)?.data?.isFavorite = isFavorite
        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
            if(isAdded()){
                setLog("DWProgrss-onChangedid", data.id.toString())
                setLog("DWProgrss-onChanged", reason.toString())
                val downloadQueue =
                    AppDatabase.getInstance()?.downloadQueue()?.findByDownloadManagerId(data.id)
                val downloadedAudio =
                    AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

                when (reason) {
                    Reason.DOWNLOAD_ADDED -> {
                        setLog("DWProgrss-ADDED", data.id.toString())
                    }
                    Reason.DOWNLOAD_QUEUED -> {
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-QUEUED", data.id.toString())
                            if (playlistAdpter != null && playlistRespModel!=null) {
                                if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                        playlistRespModel?.data?.head?.data?.id!!
                                    )!!
                                ) {
                                    val index = playlistSongList?.indexOfFirst {
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        playlistAdpter?.notifyItemChanged(index)
                                    }
                                } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                        playlistRespModel?.data?.head?.data?.id!!
                                    )!!
                                ) {
                                    val index = playlistSongList?.indexOfFirst {
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        playlistAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_STARTED -> {
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-STARTED", data.id.toString())
                            if (playlistAdpter != null && playlistRespModel!=null) {
                                if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                        playlistRespModel?.data?.head?.data?.id!!
                                    )!!
                                ) {
                                    val index = playlistSongList?.indexOfFirst {
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        playlistAdpter?.notifyItemChanged(index)
                                    }
                                } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                        playlistRespModel?.data?.head?.data?.id!!
                                    )!!
                                ) {
                                    val index = playlistSongList?.indexOfFirst {
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        playlistAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_PROGRESS_CHANGED -> {
                        setLog("DWProgrss-CHANGED", data.id.toString())
                    }
                    Reason.DOWNLOAD_RESUMED -> {
                        setLog("DWProgrss-RESUMED", data.id.toString())
                    }
                    Reason.DOWNLOAD_PAUSED -> {
                        setLog("DWProgrss-PAUSED", data.id.toString())
                    }
                    Reason.DOWNLOAD_COMPLETED -> {
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-COMPLETED", data.id.toString())
                            if (playlistAdpter != null && playlistRespModel!=null) {
                                if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                        playlistRespModel?.data?.head?.data?.id!!
                                    )!!
                                ) {
                                    val index = playlistSongList?.indexOfFirst {
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        playlistAdpter?.notifyItemChanged(index)
                                    }
                                } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                        playlistRespModel?.data?.head?.data?.id!!
                                    )!!
                                ) {
                                    val index = playlistSongList?.indexOfFirst {
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        playlistAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_CANCELLED -> {
                        setLog("DWProgrss-CANCELLED", data.id.toString())
                    }
                    Reason.DOWNLOAD_REMOVED -> {
                        setLog("DWProgrss-REMOVED", data.id.toString())
                    }
                    Reason.DOWNLOAD_DELETED -> {
                        setLog("DWProgrss-DELETED", data.id.toString())
                    }
                    Reason.DOWNLOAD_ERROR -> {
                        setLog("DWProgrss-ERROR", data.id.toString())
                    }
                    Reason.DOWNLOAD_BLOCK_UPDATED -> {
                        setLog("DWProgrss-UPDATED", data.id.toString())
                    }
                    Reason.DOWNLOAD_WAITING_ON_NETWORK -> {
                        setLog("DWProgrss-NETWORK", data.id.toString())
                    }
                    else -> {}
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )
        if (!playlistSongList.isNullOrEmpty()) {
            checkAllContentDownloadedOrNot(playlistSongList)
        } else {
            playPauseStatusChange(true)
        }

    }
    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1
    private suspend fun checkAllContentDWOrNot(playlistSongList: List<PlaylistModel.Data.Body.Row?>?):Boolean {
        try {
            if (isAdded && context != null){
                var isAllDownloaded = false
                var isAllDW = true
                var isAllDownloadInQueue = false
                var isCurrentContentPlayingFromThis = false
                if (!playlistSongList.isNullOrEmpty()) {
                    try {
                        playlistSongList.forEachIndexed { index, it ->
                            if (it != null){
                                if (!isAllDownloadInQueue){
                                    val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                        ?.getDownloadQueueItemsByContentIdAndContentType(
                                            it.data.id.toString(), ContentTypes.AUDIO.value
                                        )
                                    if (downloadQueue?.contentId.equals(it.data.id)) {
                                        if (downloadQueue?.downloadAll == 1) {
                                            isAllDownloadInQueue = true
                                        } else {
                                            isAllDownloadInQueue = false
                                        }
                                    }
                                }

                                if (isAllDW) {
                                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                        ?.getDownloadedAudioItemsByContentIdAndContentType(
                                            it.data.id, ContentTypes.AUDIO.value
                                        )
                                    if (downloadedAudio != null && downloadedAudio.contentId.equals(it.data.id)) {
                                        isAllDownloaded = true
                                    } else {
                                        isAllDW = false
                                        isAllDownloaded = false
                                    }
                                }


                                if (!isCurrentContentPlayingFromThis && !BaseActivity.songDataList.isNullOrEmpty()
                                    && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                                ) {
                                    val currentPlayingContentId =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                                    if (currentPlayingContentId?.toString()?.equals(it.data.id)!!) {
                                        it.data.isCurrentPlaying = true
                                        isCurrentContentPlayingFromThis = true
                                        setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-1-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex-index-$index")
                                        if (currentPlayingContentIndex >= 0){
                                            /*if (!playlistSongList.isNullOrEmpty()
                                                && playlistSongList.size > currentPlayingContentIndex
                                                && playlistSongList.get(currentPlayingContentIndex)?.itype == Constant.playlistNativeAds
                                            ){
                                                lastPlayingContentIndex = currentPlayingContentIndex+1
                                            }else{
                                                if (currentPlayingContentIndex != index){
                                                    lastPlayingContentIndex = currentPlayingContentIndex
                                                }
                                            }*/

                                        }else{
                                            lastPlayingContentIndex = index
                                        }
                                        currentPlayingContentIndex = index
                                    } else {
                                        if (it.data.isCurrentPlaying){
                                            lastPlayingContentIndex = index
                                        }
                                        it.data.isCurrentPlaying = false
                                        playPauseStatusChange(true)
                                    }
                                } else {
                                    if (it.data.isCurrentPlaying){
                                        lastPlayingContentIndex = index
                                    }
                                    it.data.isCurrentPlaying = false
                                }
                            }
                        }
                        setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-2-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                    }catch (e:Exception){

                    }
                    setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-3-$isCurrentContentPlayingFromThis")
                }
                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-4-$isCurrentContentPlayingFromThis")
                setIsAllDownloadImage(isAllDownloadInQueue, isAllDownloaded)
                return isCurrentContentPlayingFromThis
            }
        }catch (e:Exception){

        }

        return false
    }

    suspend fun setIsAllDownloadImage(isAllDownloadInQueue:Boolean, isAllDownloaded:Boolean){
        baseMainScope.launch {
            if (isAllDownloadInQueue) {
                ivDownloadFullList.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_downloading,
                        R.color.colorWhite
                    )
                )
                ivDownloadFullListActionBar.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_downloading,
                        R.color.colorWhite
                    )
                )
            } else {
                if (isAllDownloaded) {
                    ivDownloadFullList.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_downloaded2,
                            R.color.colorWhite
                        )
                    )
                    ivDownloadFullListActionBar.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_downloaded2,
                            R.color.colorWhite
                        )
                    )
                } else {
                    ivDownloadFullList.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_download,
                            R.color.colorWhite
                        )
                    )
                    ivDownloadFullListActionBar.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_download,
                            R.color.colorWhite
                        )
                    )
                }
            }
        }
    }

    private fun checkAllContentDownloadedOrNot(playlistSongList: List<PlaylistModel.Data.Body.Row?>?) {
        baseIOScope.launch {
            if (isAdded && context != null){
                var isCurrentContentPlayingFromThis = false
                if (!playlistSongList.isNullOrEmpty()) {
                    if (playlistRespModel != null && playlistRespModel?.data?.head?.data != null) {
                        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-1-$isCurrentContentPlayingFromThis")
                        isCurrentContentPlayingFromThis =
                            withContext(Dispatchers.Default) {
                                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-2-$isCurrentContentPlayingFromThis")
                                checkAllContentDWOrNot(playlistSongList)
                            }
                        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-5-$isCurrentContentPlayingFromThis")
                        withContext(Dispatchers.Main){
                            setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-6-$isCurrentContentPlayingFromThis")
                            if (isCurrentContentPlayingFromThis) {
                                if (activity != null) {
                                    if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                        playPauseStatusChange(false)
                                    } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                        playPauseStatusChange(true)
                                    } else {
                                        playPauseStatusChange(true)
                                    }
                                }
                                if (playlistAdpter != null) {
                                    //playlistAdpter?.notifyDataSetChanged()
                                    playlistAdpter?.notifyItemChanged(lastPlayingContentIndex)
                                    playlistAdpter?.notifyItemChanged(currentPlayingContentIndex)
                                }
                                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-7-$isCurrentContentPlayingFromThis")
                            }
                        }

                        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-8-$isCurrentContentPlayingFromThis")
                    }
                }
            }
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun playPauseStatusChange(status: Boolean) {
        setLog(TAG, "playPauseStatusChange: status:${status}")
        isPlaying = status
        var color = R.color.colorWhite
        if (varient == 1) {
            color = R.color.colorWhite
        } else {
            color = R.color.colorBlack
        }
        GlobalScope.launch(Dispatchers.Main){
            if (status) {
                ivPlayAll?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_play_2,
                        color
                    )
                )
                tvPlayAll?.text = getString(R.string.podcast_str_4)
                withContext(Dispatchers.Default){ delay(800)}
                ivPlayAllActionBar?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_play_2,
                        color
                    )
                )
                tvPlayAllActionBar?.text = getString(R.string.podcast_str_4)
            } else {
                ivPlayAll?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_pause_3,
                        color
                    )
                )
                tvPlayAll?.text = getString(R.string.general_str)
                withContext(Dispatchers.Default){ delay(800)}
                ivPlayAllActionBar?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_pause_3,
                        color
                    )
                )
                tvPlayAllActionBar?.text = getString(R.string.general_str)
            }
        }


        CommonUtils.setGoldUserViewStyle(requireContext(),tvPlayAll)
    }

    private fun setLocalBroadcast() {
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
            this,
            Constant.AUDIO_PLAYER_EVENT
        )
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (!playlistSongList.isNullOrEmpty()) {
                    checkAllContentDownloadedOrNot(playlistSongList)
                } else {
                    playPauseStatusChange(true)
                }
                CommonUtils.setPageBottomSpacing(
                    scrollView,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (!playlistSongList.isNullOrEmpty()) {
                checkAllContentDownloadedOrNot(playlistSongList)
            } else {
                playPauseStatusChange(true)
            }
            MainScope().launch {
                if (context != null) {
                    setLog("ChartLifecycle", "onHiddenChanged-$hidden--$artworkProminentColor")
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        } else {
            MainScope().launch {
                if (context != null) {
                    changeStatusbarcolor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.home_bg_color
                        )
                    )
                }
            }
        }
    }

    private fun getAdsData(playlistSongList: ArrayList<PlaylistModel.Data.Body.Row>): ArrayList<PlaylistModel.Data.Body.Row> {
        if (!playlistSongList.isNullOrEmpty() && CommonUtils.isDisplayAds() && CommonUtils.getFirebaseConfigAdsData().playlistDetailsPage.displayAd) {
            val adDisplayFirstPosition =
                CommonUtils.getFirebaseConfigAdsData().playlistDetailsPage.firstAdPositionAfterRows
            val adDisplayPositionFrequency =
                CommonUtils.getFirebaseConfigAdsData().playlistDetailsPage.repeatFrequencyAfterRows
            var adDisplayPosition = adDisplayFirstPosition
            //val adDisplayPosition = 4
            var isFirstAds = true
            val adUnitIdList = arrayListOf(
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_1,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_2,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_3,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_4,
                Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_5
            )
            val adTotalIds = adUnitIdList.size
            var adIdCount = 0
            var i = 0
            var k = 0
            val iterator = playlistSongList.listIterator()
            while (iterator.hasNext()) {
                //setLog("adInserted-1", i.toString())

                if (k > 0 && k % adDisplayPosition == 0) {
                    if (isFirstAds) {
                        k = 0
                        isFirstAds = false
                        adDisplayPosition = adDisplayPositionFrequency
                    }
                    //setLog("adInserted-2", i.toString())
                    //setLog("adInserted", "Befor==" + homeModel.data?.body?.rows?.get(i)?.heading)

                    val playlistRow = PlaylistModel.Data.Body.Row()
                    playlistRow.itype = Constant.playlistNativeAds

                    if (adTotalIds > adIdCount) {
                        //setLog("adInserted-3", adIdCount.toString())
                        //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                        playlistRow.adUnitId = adUnitIdList.get(adIdCount)
                        adIdCount++
                    } else {
                        adIdCount = 0
                        playlistRow.adUnitId = adUnitIdList.get(adIdCount)
                        //setLog("adInserted-4", adIdCount.toString())
                        //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                        adIdCount++
                    }

                    iterator.add(playlistRow)
                }
                val item = iterator.next()
                i++
                k++
            }
        }
        return playlistSongList
    }

    fun setPlayAllButton() {
        if (isIconAnimation) {
            llPlayAllAnim?.show()
            llPlayAll?.hide()
            Handler(Looper.getMainLooper()).postDelayed({
                llPlayAllAnim?.playAnimation()
            }, 1000L)
        } else {
            llPlayAllAnim?.hide()
            llPlayAll?.show()
        }
    }


    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        CoroutineScope(Dispatchers.IO).async {
            val dataMap= HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+playlistRespModel?.data?.head?.data?.title)

            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }
}