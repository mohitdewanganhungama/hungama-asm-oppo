package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.fetch2.AbstractFetchListener
import com.hungama.fetch2.Download
import com.hungama.fetch2.Error
import com.hungama.fetch2.FetchListener
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.FavouritedEvent
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.DetailChartAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.HomeViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.CHART_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.CHART_DETAIL_PAGE
import com.hungama.music.utils.Constant.MODULE_FAVORITE
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_chart_detail_v1.*
import kotlinx.android.synthetic.main.fragment_chart_detail_v1.iv_banner
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.centerGradient
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.devider
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.headBlur
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.hsvReadMore
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivDownloadFullList
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivDownloadFullListActionBar
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivFavorite
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivFavoriteActionBar
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivPlayAll
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.iv_MainImage
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.iv_collapsingImageBg
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.llDetails
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.llDetails2
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.llPlayAll
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.llPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.playlistAlbumArtImageView
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.playlistAlbumArtImageViewLayer
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.playlistDetailroot
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.rlHeading
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.rvPlaylist
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.rvRecomendation
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.scrollView
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.shimmerLayout
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.topView
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvPlayAll
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvReadMoreDescription
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvSubTitle
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvSubTitle2
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvTitle
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL


class ChartDetailFragment : BaseFragment(), TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnDownloadQueueItemChanged,
    BaseFragment.OnMenuItemClicked, OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack, OnParentItemClickListener,
    BucketParentAdapter.OnMoreItemClick {

    var artImageUrl: String? = null
    var imageArray: ArrayList<String>? = null
    var variantImages: ArrayList<String>? = null

    //var artImageUrl = "https://files.hubhopper.com/podcast/313123/storytime-with-gurudev-sri-sri-ravi-shankar.jpg?v=1598432706&s=hungama"
    var selectedContentId: String? = null
    var playerType: String? = null

    var playlistListViewModel: PlaylistViewModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playlistSongList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    var ascending = true
    var playlistAdpter: DetailChartAdapter? = null
    var artworkProminentColor = 0
    var artworkHeight = 0
    var userViewModel: UserViewModel? = null
    var isFavourite = false
    var bookmarkDataModel: BookmarkDataModel? = null
    var playListModel: PlaylistDynamicModel? = null
    var isPlaying = false
    var isFromVerticalPlayer = false
    var ft: AdsConfigModel.Ft? = AdsConfigModel.Ft()
    var nonft: AdsConfigModel.Nonft? = AdsConfigModel.Nonft()

    companion object {
        var playlistRespModel: PlaylistDynamicModel? = null
        var chartSongItem: PlaylistModel.Data.Body.Row? = null
        var playableItemPosition = 0
        var varient: Int = 0
        fun newInstance(varient: Int): ChartDetailFragment {
            val fragment = ChartDetailFragment()
            this.varient = varient
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (activity != null && activity is MainActivity) {
            BaseActivity.isNewSwipablePlayerOpen = false
            (requireActivity() as MainActivity).showBottomNavigationBar()
        }
        (requireActivity() as MainActivity).showBottomNavigationBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (varient == 1) {
            return inflater.inflate(R.layout.fragment_chart_detail_v1, container, false)
        } else {
            return inflater.inflate(R.layout.fragment_chart_detail_v2, container, false)
        }

    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            setLog("ChartLifecycle", "initializeComponent-1")
            if (context != null) {
                setLog("ChartLifecycle", "initializeComponent-2")
                if (CommonUtils.isUserHasGoldSubscription()) {
                    CommonUtils.setAppButton2(requireContext(), llPlayAll)
                    CommonUtils.setAppButton2(requireContext(), llPlayAllActionBar)
                } else {
//                    if (varient == 1) {
//                        CommonUtils.setAppButton1(requireContext(), llPlayAll)
//                        CommonUtils.setAppButton1(requireContext(), llPlayAllActionBar)
//                    } else {
//                        CommonUtils.setAppButton4(requireContext(), llPlayAll)
//                        CommonUtils.setAppButton4(requireContext(), llPlayAllActionBar)
//                    }

                    CommonUtils.blackButton(requireContext(), llPlayAll)
                    CommonUtils.blackButton(requireContext(), llPlayAllActionBar)

                }
                if (arguments != null) {
                    selectedContentId = requireArguments().getString("id").toString()
                    if (requireArguments().containsKey(Constant.isFromVerticalPlayer)) {
                        isFromVerticalPlayer =
                            requireArguments().getBoolean(Constant.isFromVerticalPlayer)
                        setLog(TAG, "initializeComponent: selectedContentId " + selectedContentId)
                    }
                }


                Constant.screen_name ="Chart Detail Screen"
                ivBack?.setOnClickListener { backPress() }
                ivBack2?.setOnClickListener { backPress() }
                rlHeading.visibility = View.INVISIBLE

                rvPlaylist.visibility = View.VISIBLE
                //rvTrendingPlaylist.visibility = View.VISIBLE

                shimmerLayout.visibility = View.VISIBLE
                shimmerLayout.startShimmer()

                llPlayAll?.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(
                                requireContext(), llPlayAll!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    } catch (e: Exception) {

                    }
                    if (isOnClick()) {
                        if (playlistRespModel != null && !playlistRespModel?.data?.body?.rows.isNullOrEmpty()) {
                            playAllPlaylist()
                        }
                    }


                }
                llPlayAllActionBar?.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(
                                requireContext(), llPlayAllActionBar!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    } catch (e: Exception) {

                    }
                    if (isOnClick()) {
                        if (playlistRespModel != null && !playlistRespModel?.data?.body?.rows.isNullOrEmpty()) {
                            playAllPlaylist()
                        }
                    }
                }

                 CommonUtils.getbanner(requireContext(),iv_banner,Constant.nudge_playlist_banner)

                threeDotMenu?.setOnClickListener(this@ChartDetailFragment)
                threeDotMenu2?.setOnClickListener(this@ChartDetailFragment)
                ivFavorite?.setOnClickListener(this@ChartDetailFragment)
                ivFavoriteActionBar?.setOnClickListener(this@ChartDetailFragment)

                CommonUtils.setPageBottomSpacing(
                    scrollView,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
                scrollView?.fullScroll(View.FOCUS_DOWN)
                scrollView?.isSmoothScrollingEnabled = true
                scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@ChartDetailFragment)
                setUpPlaylistDetailListViewModel()
            }

        }
        tracksViewModel =
            TracksViewModel(Injection.provideTrackRepository(), this@ChartDetailFragment)
    }

    fun setArtImageBg(status: Boolean) {
        if (activity !== null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                artImageUrl
            ) && playlistDetailroot != null
        ) {
            val result: Deferred<Bitmap?> = baseIOScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }

            baseIOScope.launch {
                try {
                    // get the downloaded bitmap
                    val bitmap: Bitmap? = result.await()
                    val artImage = BitmapDrawable(resources, bitmap)
                    if (status) {
                        if (bitmap != null && isAdded && view != null) {

                            artworkProminentColor =
                                CommonUtils.calculateAverageColor(bitmap, 1)
                            withContext(Dispatchers.Main) {
                                if (context != null) {
                                    setLog(
                                        "ChartLifecycle",
                                        "setArtImageBg--$artworkProminentColor"
                                    )
                                    changeStatusbarcolor(artworkProminentColor)

                                    var artWorkHeight = 0
                                    val point = CommonUtils.getPointOfView(devider)
                                    if (point != null) {
                                        artWorkHeight =
                                            point.y - resources.getDimensionPixelSize(R.dimen.dimen_60)
                                    }
                                    setLog("artWorkHeight", artWorkHeight.toString())
                                    devider?.visibility = View.VISIBLE
                                    if (varient == 1) {
                                        iv_collapsingImageBg?.layoutParams?.height = artWorkHeight
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

                                        iv_collapsingImageBg?.background = artImage
                                        // We need to adjust the height if the width of the bitmap is
                                        // smaller than the view width, otherwise the image will be boxed.
                                        val viewWidthToBitmapWidthRatio =
                                            iv_MainImage?.getWidth()
                                                ?.toDouble()!! / bitmap.getWidth().toDouble()
                                        artworkHeight =
                                            ((bitmap.getHeight() * viewWidthToBitmapWidthRatio).toInt())
                                        iv_MainImage?.getLayoutParams()?.height = artworkHeight
                                        centerGradient?.getLayoutParams()?.height = artworkHeight
                                        iv_MainImage?.background = artImage
                                        Utils.setMarginsTop(centerGradient, (artworkHeight / 2))
                                        Utils.setMarginsTop(llDetails, (artworkHeight / 2))
                                        //centerGradient.visibility = View.VISIBLE
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
            baseMainScope.launch {
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
        baseMainScope.launch {
            if (isPlaying) {
                if (playlistSongList?.size!! > 0 && playlistSongList != null) {
                    playableItemPosition = 0
                    setEventModelDataAppLevel(
                        playlistSongList?.get(0)?.data?.id!!,
                        playlistSongList?.get(0)?.data?.title!!,
                        playListModel?.data?.head?.data?.title!!
                    )
                    setProgressBarVisible(true)
                    setUpPlayableContentListViewModel(playlistSongList?.get(0)?.data?.id!!)
                }
            } else {
                (requireActivity() as MainActivity).pausePlayer()
                playPauseStatusChange(true)
            }
        }

    }

    private fun setUpPlaylistDetailListViewModel() {
        try {
            if (ConnectionUtil(activity).isOnline) {
                playlistListViewModel = ViewModelProvider(
                    this
                ).get(PlaylistViewModel::class.java)
                playlistListViewModel?.getPlaylistDetailList(requireContext(), selectedContentId!!)
                    ?.observe(this,
                        Observer {
                            when (it.status) {
                                Status.SUCCESS -> {
//                                if(Constant.IS_SEARCH_RECOMMENDED_DISPLAY){
//                                    getTrendCall(it?.data)
//                                }else{
                                    fillChartDetail(it?.data)
//                                }

                                }

                                Status.LOADING -> {
                                    setProgressBarVisible(false)
                                }

                                Status.ERROR -> {
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
            }
        } catch (e: Exception) {

        }

    }

    private fun fillChartDetail(data: PlaylistDynamicModel?) {
        baseMainScope.launch {
            setPlaylistDetailsListData(data!!)
            setDetails(data, true)
            scrollView.visibility = View.VISIBLE
        }
    }

    fun getTrendCall(homedata: PlaylistDynamicModel?) {
        val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url = WSConstants.METHOD_TRENDING_PLAYLIST + "?contentId=" + selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
            Observer {
                when (it.status) {
                    Status.SUCCESS -> {

                        setLog(TAG, "getTrendPodcastCall: data:${it?.data} ")
                        setLog(
                            TAG,
                            "getTrendPodcastCall: before size:${homedata?.data?.body?.recomendation?.size}"
                        )

                        var dailyDoseIndex = 0
                        var podcastSize = homedata?.data?.body?.recomendation?.size
                        if (it?.data != null && it?.data?.data?.body != null && it?.data?.data?.body?.searchRecommendations?.size!! > 0) {
                            it?.data?.data?.body?.searchRecommendations?.forEach {
                                if (podcastSize != null) {
                                    homedata?.data?.body?.recomendation?.add(
                                        podcastSize + dailyDoseIndex,
                                        it
                                    )
                                }
                                dailyDoseIndex += 1

                                setLog(
                                    TAG,
                                    "getTrendPodcastCall: dailyDoseIndex:${dailyDoseIndex} "
                                )
                            }
                        }

                        getYouMayLikeCall(homedata)




                        setLog(
                            TAG,
                            "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}"
                        )
                    }

                    Status.LOADING -> {
                        setProgressBarVisible(true)
                    }

                    Status.ERROR -> {
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        fillChartDetail(homedata!!)
                    }
                }
            })
    }

    fun getYouMayLikeCall(homedata: PlaylistDynamicModel?) {
        val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url = WSConstants.METHOD_YOU_MAY_LIKE_PLAYLIST + "?contentId=" + selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
            Observer {
                when (it.status) {
                    Status.SUCCESS -> {

                        setLog(TAG, "getYouMayLikePodcastCall: data:${it?.data} ")
                        setLog(
                            TAG,
                            "getYouMayLikePodcastCall: before size:${homedata?.data?.body?.recomendation?.size}"
                        )

                        var dailyDoseIndex = 0
                        var podcastSize = homedata?.data?.body?.recomendation?.size
                        if (it?.data != null && it?.data?.data?.body != null && it?.data?.data?.body?.searchRecommendations?.size!! > 0) {
                            it?.data?.data?.body?.searchRecommendations?.forEach {
                                if (podcastSize != null) {
                                    homedata?.data?.body?.recomendation?.add(
                                        podcastSize + dailyDoseIndex,
                                        it
                                    )
                                }
                                dailyDoseIndex += 1

                                setLog(
                                    TAG,
                                    "getYouMayLikePodcastCall: dailyDoseIndex:${dailyDoseIndex} "
                                )
                            }
                        }
                        fillChartDetail(homedata!!)



                        setLog(
                            TAG,
                            "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}"
                        )
                    }

                    Status.LOADING -> {
                        setProgressBarVisible(true)
                    }

                    Status.ERROR -> {
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        fillChartDetail(homedata!!)
                    }
                }
            })
    }


    fun setPlaylistDetailsListData(playlistModel: PlaylistDynamicModel) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (playlistModel != null && playlistModel?.data?.body != null) {
                    playlistRespModel = playlistModel
                    setupUserViewModel()
                    if (playlistModel.data?.body?.rows!! != null && playlistModel.data?.body?.rows?.size!! > 0) {
                        playlistSongList = playlistModel.data?.body?.rows!!
                        ivDownloadFullList?.setOnClickListener(this@ChartDetailFragment)
                        ivDownloadFullListActionBar?.setOnClickListener(this@ChartDetailFragment)
                        //tvSongsCount.setText(""+playlistSongList?.size+" Songs")
                        rvPlaylist?.apply {
                            layoutManager =
                                GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                            //setRecycledViewPool(RecyclerView.RecycledViewPool())
                            isNestedScrollingEnabled = false
                            setHasFixedSize(true)

                        }
                        setPlayListSongAdapter(ascending)

                    }
                    if (playlistModel.data?.body?.recomendation != null && playlistModel.data?.body?.recomendation?.size!! > 0) {
                        rvRecomendation?.visibility = View.VISIBLE

                        val varient = Constant.ORIENTATION_HORIZONTAL

                        val bucketParentAdapter = BucketParentAdapter(
                            playlistModel.data?.body?.recomendation!!,
                            requireContext(),
                            this@ChartDetailFragment,
                            this@ChartDetailFragment,
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
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE
                rvRecomendation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        baseIOScope.launch {
                            val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                            val lastVisiable: Int =
                                mLayoutManager?.findLastCompletelyVisibleItemPosition()!!

                            setLog(
                                TAG,
                                "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                            )
                            if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                                val fromBucket =
                                    playlistModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                                val toBucket =
                                    playlistModel.data?.body?.recomendation?.get(lastVisiable)?.heading
                                val sourcePage =
                                    MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                                if (!fromBucket?.equals(toBucket, true)!!) {
                                    callPageScrolledEvent(
                                        sourcePage,
                                        "" + lastVisiable,
                                        fromBucket,
                                        toBucket!!
                                    )
                                }

                            }
                        }
                    }
                })


                        rvPlaylist.setPadding(0, 0, 0, 0)
                        rvRecomendation.setPadding(0, 0, 0, 0)
                    }
                    /*if (playlistModel.data?.body?.similar != null && playlistModel.data?.body?.similar?.size!! > 0) {
                        llTrendingPlaylist.visibility = View.VISIBLE

                        val trendingPlaylist = playlistModel.data?.body?.similar
                        ivMore.setOnClickListener {
                            redirectToMoreBucketListPage(trendingPlaylist,getString(R.string.chart_str_3))
                        }
                        rvTrendingPlaylist.apply {
                            layoutManager =
                                GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                            adapter = TopChartAdapter(context, trendingPlaylist,
                                object : TopChartAdapter.OnChildItemClick {
                                    override fun onUserClick(childPosition: Int) {

                                        var varient = 1
                                        if (!TextUtils.isEmpty(trendingPlaylist?.get(childPosition)?.data?.variant)) {
                                            if (trendingPlaylist?.get(childPosition)?.data?.variant.equals(
                                                    "v2",
                                                    true
                                                )
                                            ) {
                                                varient = 2
                                            }
                                        }
                                        val chartDetailFragment = ChartDetailFragment(varient)
                                        val bundle = Bundle()
                                        bundle.putString(
                                            "image",
                                            trendingPlaylist?.get(childPosition)?.data?.image!!
                                        )
                                        bundle.putString(
                                            "id",
                                            trendingPlaylist?.get(childPosition)?.data?.id
                                        )
                                        bundle.putString("playerType", playerType)
                                        chartDetailFragment.arguments = bundle
                                        addFragment(
                                            R.id.fl_container,
                                            this@ChartDetailFragment,
                                            chartDetailFragment,
                                            false
                                        )
                                    }
                                })
                            setRecycledViewPool(RecyclerView.RecycledViewPool())
                            setHasFixedSize(true)
                        }
                    }

                    if (playlistModel.data?.body?.recomendation != null && playlistModel.data?.body?.recomendation?.size!! > 0) {
                        llRecomandedPlaylist.visibility = View.VISIBLE
                        val recomandedPlaylist = playlistModel.data?.body?.recomendation
                        rvRecomandedPlaylist.visibility = View.VISIBLE
                        ivMoreRecomandeds.setOnClickListener {
                            redirectToMoreBucketListPage(playlistModel?.data?.body?.recomendation,getString(R.string.chart_str_4))
                        }
                        rvRecomandedPlaylist.apply {
                            layoutManager =
                                GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                            adapter = RecomandedChartAdapter(context, recomandedPlaylist,
                                object : RecomandedChartAdapter.OnChildItemClick {
                                    override fun onUserClick(childPosition: Int) {
                                        var varient = 1
                                        if (!TextUtils.isEmpty(recomandedPlaylist?.get(childPosition)?.data?.variant)) {
                                            if (recomandedPlaylist?.get(childPosition)?.data?.variant.equals(
                                                    "v2",
                                                    true
                                                )
                                            ) {
                                                varient = 2
                                            }
                                        }
                                        val chartDetailFragment = ChartDetailFragment(varient)
                                        val bundle = Bundle()
                                        bundle.putString(
                                            "image",
                                            recomandedPlaylist?.get(childPosition)?.data?.image!!
                                        )
                                        bundle.putString(
                                            "id",
                                            recomandedPlaylist?.get(childPosition)?.data?.id
                                        )
                                        bundle.putString("playerType", playerType)
                                        chartDetailFragment.arguments = bundle
                                        addFragment(
                                            R.id.fl_container,
                                            this@ChartDetailFragment,
                                            chartDetailFragment,
                                            false
                                        )
                                    }
                                })
                            setRecycledViewPool(RecyclerView.RecycledViewPool())
                            setHasFixedSize(true)
                        }
                    }*/

                    var isContentAutoPlay = 0
                    if (requireArguments()?.containsKey("isPlay") == true) {
                        isContentAutoPlay = requireArguments().getInt("isPlay")
                    }
                    setLog(TAG, "setPlayListSongAdapter: auto play${isContentAutoPlay}")
                    if (isContentAutoPlay == 1) {
                        llPlayAll?.performClick()
                    }
                    shimmerLayout.visibility = View.GONE
                    shimmerLayout.stopShimmer()
                }
            }
        }
    }

    private fun setPlayListSongAdapter(asc: Boolean) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                playlistAdpter = DetailChartAdapter(
                    requireContext(), playlistSongList,
                    object : DetailChartAdapter.OnChildItemClick {
                        override fun onUserClick(
                            childPosition: Int,
                            isMenuClick: Boolean,
                            isDownloadClick: Boolean
                        ) {
                            playableItemPosition = childPosition
                            BaseActivity.setTouchData()

                            setEventModelDataAppLevel(
                                "" + playlistSongList?.get(childPosition)?.data?.id!!,
                                playlistSongList?.get(childPosition)?.data?.title!!,
                                playListModel?.data?.head?.data?.title!!
                            )

                            if (isMenuClick) {
                                if (isOnClick()) {
                                    chartSongItem = playlistSongList?.get(childPosition)
                                    val isFavorite =
                                        playlistSongList?.get(childPosition)?.data?.isFavorite!!
                                    commonThreeDotMenuItemSetup(
                                        CHART_DETAIL_ADAPTER,
                                        this@ChartDetailFragment,
                                        isFavorite
                                    )
                                }

                            }
                            else if (isDownloadClick) {
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
                                dpm.restrictedDownload =
                                    RestrictedDownload.valueOf(playlistSongList?.get(childPosition)?.data?.misc?.restricted_download!!)
                                if (CommonUtils.userCanDownloadContent(
                                        requireContext(),
                                        playlistDetailroot,
                                        dpm,
                                        this@ChartDetailFragment,Constant.drawer_restricted_download
                                    )
                                ) {

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

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.misc?.f_playcount.toString()!!)) {
                                        dq.f_playcount =
                                            playlistSongList?.get(childPosition)?.data?.misc?.f_playcount.toString()
                                    }

                                    if (!TextUtils.isEmpty(playlistSongList?.get(childPosition)?.data?.misc?.f_FavCount.toString()!!)) {
                                        dq.f_fav_count =
                                            playlistSongList?.get(childPosition)?.data?.misc?.f_FavCount.toString()
                                    }

                                    dq.pType = DetailPages.CHART_DETAIL_PAGE.value
                                    dq.contentType = ContentTypes.AUDIO.value
                                    val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                                    dq.source = eventModel.sourceName

                                    val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                        ?.findByContentId(playlistSongList?.get(childPosition)?.data?.id!!.toString())
                                    val downloadedAudio =
                                        AppDatabase.getInstance()?.downloadedAudio()
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
                                        this@ChartDetailFragment,
                                        fetchMusicDownloadListener,
                                        false,
                                        true
                                    )
                                    //}
                                }
                            }
                            else {
                                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
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
                                    setEventModelDataAppLevel(
                                        playlistSongList.get(childPosition).data.id,
                                        playlistSongList.get(childPosition).data.title,
                                        playListModel?.data?.head?.data?.title!!
                                    )
                                    setUpPlayableContentListViewModel(
                                        playlistSongList.get(
                                            childPosition
                                        ).data.id
                                    )
                                }

                            }
                        }


                    })
                //playlistAdpter?.setHasStableIds(true)
                rvPlaylist?.adapter = playlistAdpter
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
                    setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-105")
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


    private fun setDetails(it: PlaylistDynamicModel?, status: Boolean) {
        playListModel = it
        baseMainScope.launch {
            if (isAdded && context != null) {
                playerType = "" + playListModel?.data?.head?.data?.type
                artImageUrl = playListModel?.data?.head?.data?.image
//        if (requireArguments()?.containsKey("imageArray")) {
//            imageArray = requireArguments().getStringArrayList("imageArray")
//        }
                setLog(TAG,"setDetails artImageUrl:${artImageUrl} varient:${varient} variantImages:${variantImages}")
                if (requireArguments()?.containsKey("variant_images") == true) {
                    variantImages = requireArguments().getStringArrayList("variant_images")
                    if (variantImages != null && variantImages?.size!! > 0) {
                        if(!TextUtils.isEmpty(variantImages?.get(0))){
                            artImageUrl = variantImages?.get(0)
                        }
                        setLog(TAG,"setDetails requireArguments artImageUrl:${artImageUrl} varient:${varient} variantImages:${variantImages} ${variantImages?.size}")
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
                            playlistAlbumArtImageViewLayer?.visibility = View.VISIBLE
                            artImageUrl = turl
                        } else {
                            playlistAlbumArtImageViewLayer?.visibility = View.GONE
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
                        playlistAlbumArtImageView?.visibility = View.GONE
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

                if (it?.data?.head?.data?.title != null && !TextUtils.isEmpty(it?.data?.head?.data?.title)) {
                    tvTitle?.text = it?.data?.head?.data?.title
                } else {
                    tvTitle?.text = ""
                }

                if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.description?.trim())) {
                    tvReadMoreDescription?.text = it?.data?.head?.data?.misc?.description
                    SaveState.isCollapse = true
                    tvReadMoreDescription?.setShowingLine(2)
                    tvReadMoreDescription?.addShowMoreText("read more")
                    tvReadMoreDescription?.addShowLessText("read less")
                    tvReadMoreDescription?.setShowMoreColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorWhite
                        )
                    )
                    tvReadMoreDescription?.setShowLessTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorWhite
                        )
                    )
                    tvReadMoreDescription?.setShowMoreStyle(Typeface.BOLD)

                    hsvReadMore?.visibility = View.GONE
                    tvReadMoreDescription?.visibility = View.VISIBLE

                } else {
                    hsvReadMore?.visibility = View.GONE
                    tvReadMoreDescription?.visibility = View.GONE
                }
                tvSubTitle?.text = getString(R.string.chart_str_1)


                if (it?.data?.head?.data?.misc != null) {
                    var subtitle = ""
                    if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.f_playcount)) {
                        subtitle += it?.data?.head?.data?.misc?.f_playcount + " " + getString(R.string.artist_str_4) + " • "
                    }
                    if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.favCount)) {
                        subtitle += CommonUtils.ratingWithSuffix(it?.data?.head?.data?.misc?.items!!.toString()) + " " + getString(
                            R.string.library_playlist_str_8
                        )
                    }
                    tvSubTitle2?.text = subtitle
                }
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE
                llDetails2?.visibility = View.VISIBLE
            }
        }


    }

    override fun onDestroy() {
        baseMainScope.launch {
            if (context != null) {
                changeStatusbarcolor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.home_bg_color
                    )
                )
            }
        }
        super.onDestroy()
        setLog("ChartLifecycle", "onDestroy-")

        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        if (isFromVerticalPlayer) {
            if (activity != null) {
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
        baseMainScope.launch {
            val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            Util.startForegroundService(getViewActivity(), intent)
            (activity as MainActivity).reBindService()

            playPauseStatusChange(false)
        }

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun setUpPlayableContentListViewModel(id: String) {
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBar(false)
                            if (it?.data != null) {
                                //setLog(TAG, "isViewLoading $it")
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    baseIOScope.launch {
                                        setPlayableContentListData(it?.data!!)
                                    }
                                } else {
                                    playableItemPosition = playableItemPosition + 1
                                    if (playableItemPosition < playlistSongList?.size!!) {
                                        setUpPlayableContentListViewModel(
                                            playlistSongList?.get(
                                                playableItemPosition
                                            )?.data?.id!!
                                        )
                                    }
                                }
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBar(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                            setEmptyVisible(false)
                            setProgressBar(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (isAdded && context != null) {
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

                        BaseActivity.setTrackListData(songDataList)
                        tracksViewModel.prepareTrackPlayback(0)
                    }
                }
            }
        }

    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlaylistSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row?>?,
        position: Int): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            track.id = playableItem?.get(position)?.data?.id?.toLong()!!
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

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
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
        } else if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)) {
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

        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        track.pType = DetailPages.CHART_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null) {
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null) {
            track.restrictedDownload = playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null) {
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
        return songDataList
    }

    override fun onScrollChanged() {
        baseMainScope.launch {
            if (isAdded && context != null) {

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
                    rlHeading.visibility = View.VISIBLE
                    if (artworkProminentColor == 0) {
                        rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                    } else {
                        rlHeading.setBackgroundColor(artworkProminentColor)
                    }

                } else {
                    //setLog("OnNestedScroll-m--", movement.toString())
                    //setLog("OnNestedScroll-d--", maxDistance.toString())
                    rlHeading.visibility = View.INVISIBLE
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
                    rlHeading.visibility = View.VISIBLE
                    rlHeading.setBackgroundColor(artworkProminentColor)
                    setLog("aaaaa1", alphaFactor.toString())
                } else {
                    rlHeading.visibility = View.INVISIBLE
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
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == threeDotMenu || v == threeDotMenu2) {
            baseMainScope.launch {
                commonThreeDotMenuItemSetup(CHART_DETAIL_PAGE)
            }

        } else if (v == ivDownloadFullList || v == ivDownloadFullListActionBar) {
            baseIOScope.launch {
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
                        this@ChartDetailFragment, Constant.drawer_download_all
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
                    if (playlistSongList.isNotEmpty()){
                    for (item in playlistSongList?.iterator()!!) {

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

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.misc?.f_FavCount!!)) {
                            dq.f_fav_count = playlistRespModel?.data?.head?.data?.misc?.f_FavCount!!
                        }

                        if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.misc?.f_playcount!!)) {
                            dq.f_playcount = playlistRespModel?.data?.head?.data?.misc?.f_playcount!!
                        }

                        dq.pType = DetailPages.CHART_DETAIL_PAGE.value
                        dq.contentType = ContentTypes.AUDIO.value
                        dq.downloadAll = 1
                        dq.source = eventModel.sourceName

                        if (!TextUtils.isEmpty(item?.data?.misc?.movierights.toString()!!)) {
                            dq.planName = item?.data?.misc?.movierights.toString()
                        }

                        if (existingQueueItemsCount > CommonUtils.getAvailableDownloadContentSize(requireContext())
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
                        count++
                    }
                    }

                    //if (downloadQueueList.size > 0){
                    (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                        downloadQueueList,
                        this@ChartDetailFragment,
                        fetchMusicDownloadListener,
                        false,
                        true
                    )
                    //}
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_downloading)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivDownloadFullList.setImageDrawable(drawable)
                    ivDownloadFullListActionBar.setImageDrawable(drawable)*/
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            ivDownloadFullList?.setBackgroundResource(
                                R.drawable.download_black
                            )
                            ivDownloadFullListActionBar?.setBackgroundResource(
                                R.drawable.download_black
                            )
                        }
                    }

                }
            }

        } else if (v == ivFavorite || v == ivFavoriteActionBar) {
            baseMainScope.launch {
                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                    playlistRespModel?.let { setAddOrRemoveFavourite(it) }
                }
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
        if (ConnectionUtil(context).isOnline) {
            isFavourite = !isFavourite
            val jsonObject = JSONObject()
            jsonObject.put("contentId", playlistRespModel?.data?.head?.data?.id!!)
            jsonObject.put("typeId", playlistRespModel?.data?.head?.data?.type!!)
            jsonObject.put("action", isFavourite)
            jsonObject.put("module", MODULE_FAVORITE)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()
            baseIOScope.launch {
                if (isFavourite) {
                    val messageModel = MessageModel(getString(R.string.added_to_favourite), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
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
                    var newContentId=playlistRespModel?.data?.head?.data?.id
                    var contentIdData=newContentId?.replace("playlist-","")

                    hashMap.put(
                        EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)
                    val contentType = playlistRespModel?.data?.head?.data?.type
                    setLog(
                        TAG,
                        "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" + contentType)} contentType:${contentType}"
                    )
                    hashMap.put(
                        EventConstant.CONTENTTYPE_EPROPERTY,
                        "" + Utils.getContentTypeName("" + contentType)
                    )

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
                        "" + playlistRespModel?.data?.head?.data?.title
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
                else{
                    val messageModel = MessageModel(getString(R.string.removed_from_favourite), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
            }


        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun getUserBookmarkedData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserBookmarkedData(requireContext(), MODULE_FAVORITE)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUI(it?.data)
                            }

                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun fillUI(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        baseIOScope.launch {
            if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0) {
                for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!) {

                    if (playlistRespModel?.data?.head?.data?.id?.equals(bookmark?.data?.id)!!) {
                        setLog("MYBookMarkedData","${playlistRespModel?.data?.head?.data?.id}")
                        setLog("MYBookMarkedData11","${bookmark?.data?.id}")

                        isFavourite = true
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
    }

    fun setFollowingStatus() {
        baseMainScope.launch {
            if (isFavourite) {
                //ivFavorite.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
                //ivFavoriteActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
                ivFavorite?.imageAssetsFolder = "image"
                ivFavorite?.playAnimation()
                ivFavoriteActionBar?.playAnimation()
            } else {
                //ivFavorite.setImageDrawable(requireContext().faDrawable(R.string.icon_like, R.color.colorWhite))
                //ivFavoriteActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_like, R.color.colorWhite))
                ivFavorite?.cancelAnimation()
                ivFavorite?.progress = 0f
                ivFavoriteActionBar?.cancelAnimation()
                ivFavoriteActionBar?.progress = 0f
            }
        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
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
                    setLog("DWProgrss-QUEUED", data.id.toString())
                    if (playlistAdpter != null) {
                        if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                playlistRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = playlistSongList?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            if (index != null) {
                                withContext(Dispatchers.Main) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            }
                        } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                playlistRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = playlistSongList?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            if (index != null) {
                                withContext(Dispatchers.Main) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            }
                        }
                    }
                }
                Reason.DOWNLOAD_STARTED -> {
                    setLog("DWProgrss-STARTED", data.id.toString())
                    if (playlistAdpter != null) {
                        if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                playlistRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = playlistSongList?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            if (index != null) {
                                withContext(Dispatchers.Main) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            }
                        } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                playlistRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = playlistSongList?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            if (index != null) {
                                withContext(Dispatchers.Main) {
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
                    setLog("DWProgrss-COMPLETED", data.id.toString())
                    if (playlistAdpter != null) {
                        if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                playlistRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = playlistSongList?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            if (index != null) {
                                withContext(Dispatchers.Main) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            }
                        } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                playlistRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = playlistSongList?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            if (index != null) {
                                withContext(Dispatchers.Main) {
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

    private val fetchMusicDownloadListener: FetchListener = object : AbstractFetchListener() {
        override fun onAdded(download: Download) {
            super.onAdded(download)

        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            super.onQueued(download, waitingOnNetwork)
            setLog("DWProgrss-onQueued", download.id.toString())
        }

        override fun onCompleted(download: Download) {
            super.onCompleted(download)
            setLog("DWProgrss-onCompleted", download.id.toString())
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            setLog("DWProgrss-onError", error.toString())
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
            setLog("DWProgrss", downloadedBytesPerSecond.toString())
        }

        override fun onPaused(download: Download) {
            super.onPaused(download)

        }

        override fun onResumed(download: Download) {
            super.onResumed(download)

        }

        override fun onCancelled(download: Download) {
            super.onCancelled(download)

        }

        override fun onRemoved(download: Download) {
            super.onRemoved(download)

        }

        override fun onDeleted(download: Download) {
            super.onDeleted(download)

        }
    }

    override fun onResume() {
        super.onResume()
        setLog("ChartLifecycle", "onResume-")
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            fetchMusicDownloadListener,
            true,
            false
        )
        if (!playlistSongList.isNullOrEmpty() && playlistSongList?.size!! > 0) {
            checkAllContentDownloadedOrNot(playlistSongList)
        } else {
            playPauseStatusChange(true)
        }
    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        if (playlistSongList != null && playlistSongList?.size!! > 0) {
            playlistSongList?.get(position)?.data?.isFavorite = isFavorite
        }
    }

    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1
    private suspend fun checkAllContentDWOrNot(playlistSongList: List<PlaylistModel.Data.Body.Row?>?):Boolean {
        try {
            if (isAdded && context != null) {
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
                                    val downloadedAudio =
                                        AppDatabase.getInstance()?.downloadedAudio()
                                            ?.getDownloadedAudioItemsByContentIdAndContentType(
                                                it.data.id, ContentTypes.AUDIO.value
                                            )
                                    if (downloadedAudio != null && downloadedAudio.contentId.equals(
                                            it.data.id
                                        )
                                    ) {
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
                    setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-3-$isCurrentContentPlayingFromThis"
                    )
                }
                setLog(
                    "isCurrentContentPlayingFromThis",
                    "isCurrentContentPlayingFromThis-4-$isCurrentContentPlayingFromThis"
                )
                setIsAllDownloadImage(isAllDownloadInQueue, isAllDownloaded)
                return isCurrentContentPlayingFromThis
            }
        } catch (e: Exception) {

        }

        return false
    }

    suspend fun setIsAllDownloadImage(isAllDownloadInQueue: Boolean, isAllDownloaded: Boolean) {
        baseMainScope.launch {
            if (isAllDownloadInQueue) {
                ivDownloadFullList.setBackgroundResource(
                    R.drawable.download_black
                )
                ivDownloadFullListActionBar.setBackgroundResource(
                    R.drawable.download_black
                )
            } else {
                if (isAllDownloaded) {
                    ivDownloadFullList.setBackgroundResource(
                        R.drawable.download_black
                    )
                    ivDownloadFullListActionBar.setBackgroundResource(
                        R.drawable.download_black
                    )
                } else {
                    ivDownloadFullList.setBackgroundResource(
                        R.drawable.download_black
                    )
                    ivDownloadFullListActionBar.setBackgroundResource(
                        R.drawable.download_black
                    )
                }
            }
        }
    }

    private fun checkAllContentDownloadedOrNot(playlistSongList: List<PlaylistModel.Data.Body.Row?>?) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!playlistSongList.isNullOrEmpty()) {
                    if (playlistRespModel != null && playlistRespModel?.data?.head?.data != null) {
                        setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-1-$isCurrentContentPlayingFromThis"
                        )
                        isCurrentContentPlayingFromThis =
                            withContext(Dispatchers.Default) {
                                setLog(
                                    "isCurrentContentPlayingFromThis",
                                    "isCurrentContentPlayingFromThis-2-$isCurrentContentPlayingFromThis"
                                )
                                checkAllContentDWOrNot(playlistSongList)
                            }
                        setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-5-$isCurrentContentPlayingFromThis"
                        )
                        withContext(Dispatchers.Main) {
                            setLog(
                                "isCurrentContentPlayingFromThis",
                                "isCurrentContentPlayingFromThis-6-$isCurrentContentPlayingFromThis"
                            )
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
                                    setLog("isCurrentPlaying", "DetailChartAdapter-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                                    playlistAdpter?.notifyItemChanged(lastPlayingContentIndex)
                                    playlistAdpter?.notifyItemChanged(currentPlayingContentIndex)
                                }
                                setLog(
                                    "isCurrentContentPlayingFromThis",
                                    "isCurrentContentPlayingFromThis-7-$isCurrentContentPlayingFromThis"
                                )
                            }
                        }

                        setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-8-$isCurrentContentPlayingFromThis"
                        )
                    }
                }
            }
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun playPauseStatusChange(status: Boolean) {
        isPlaying = status
        baseMainScope.launch {
            if (isAdded && context != null) {
                var color = R.color.colorWhite1
                if (varient == 1) {
                    color = R.color.colorWhite1
                } else {
                    color = R.color.colorWhite1
                }
                if (status) {
                    ivPlayAll?.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_play_2,
                            color
                        )
                    )
                    tvPlayAll?.text = getString(R.string.podcast_str_4)
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
                    ivPlayAllActionBar?.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_pause_3,
                            color
                        )
                    )
                    tvPlayAllActionBar?.text = getString(R.string.general_str)
                }
                CommonUtils.setGoldUserViewStyle(requireActivity(), tvPlayAll)
            }
        }
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
        setLog("ChartLifecycle", "onHiddenChanged-$hidden")
        if (!hidden) {
            if (!playlistSongList.isNullOrEmpty()) {
                checkAllContentDownloadedOrNot(playlistSongList)
            } else {
                playPauseStatusChange(true)
            }
            baseMainScope.launch {
                if (context != null) {
                    setLog("ChartLifecycle", "onHiddenChanged-$hidden--$artworkProminentColor")
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        } else {
            baseMainScope.launch {
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
        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-102")
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
        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-103")
        return playlistSongList
    }

    private fun redirectToMoreBucketListPage(
        bodyRowsItemsItem: ArrayList<BodyRowsItemsItem?>?,
        heading: String
    ) {
        val bundle = Bundle()
        val selectedMoreBucket = RowsItem()
        selectedMoreBucket.heading = heading
        selectedMoreBucket.items = bodyRowsItemsItem
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)
    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading)
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        baseMainScope.launch {
            val bundle = Bundle()
            bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
            setLog(TAG, "onMoreClick:selectedMoreBucket " + selectedMoreBucket?.heading)
            setLog(TAG, "onMoreClick:selectedMoreBucket " + selectedMoreBucket?.image)
            val moreBucketListFragment = MoreBucketListFragment()
            moreBucketListFragment.arguments = bundle
            addFragment(R.id.fl_container, this@ChartDetailFragment, moreBucketListFragment, false)

            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + selectedMoreBucket?.heading)
            dataMap.put(
                EventConstant.CONTENT_TYPE_EPROPERTY,
                "" + playlistRespModel?.data?.head?.data?.title
            )

            dataMap.put(
                EventConstant.SOURCEPAGE_EPROPERTY,
                "" + Utils.getContentTypeDetailName("" + selectedMoreBucket?.type)
            )
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }

    private fun setProgressBar(visible: Boolean) {
        if (pProgress != null) {
            if (visible) {
                pProgress?.visibility = View.VISIBLE
            } else {
                pProgress?.visibility = View.GONE
            }

        }
    }
}