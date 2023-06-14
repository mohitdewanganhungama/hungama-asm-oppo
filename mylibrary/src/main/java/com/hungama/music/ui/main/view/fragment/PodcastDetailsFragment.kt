package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.media3.common.util.Util
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
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
import com.hungama.music.ui.base.BaseActivity.Companion.setTrackListData
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.DetailPodcastAdapter
import com.hungama.music.ui.main.adapter.TabAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PodcastViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.calculateAverageColor
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.PODCAST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.PODCAST_DETAIL_PAGE
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [PodcastDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PodcastDetailsFragment : BaseFragment(), OnParentItemClickListener,
    TabLayout.OnTabSelectedListener, TracksContract.View, BucketParentAdapter.OnMoreItemClick,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnDownloadQueueItemChanged,
    OnUserSubscriptionUpdate, BaseActivity.PlayItemChangeListener,
    BaseActivity.OnLocalBroadcastEventCallBack, BaseFragment.OnMenuItemClicked {
    var artImageUrl: String? = null
    var selectedContentId: String? = null
    var playerType: String? = null
    private var chartDetailBgArtImageDrawable: LayerDrawable? = null

    var tabAdapter: TabAdapter? = null
    var podcastListViewModel: PodcastViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    var isFavorite = false
    var bucketRespModel: HomeModel? = null

    var rowList: MutableList<RowsItem?>? = null
    private var bucketParentAdapter: BucketParentAdapter? = null
    var selectedPodcast1: RowsItem? = null
    var selectedPodcastChildPosition1: Int? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var podcastEpisodeList: List<PlaylistModel.Data.Body.Row.Data.Misc.Track> =
        ArrayList()
    var ascending = true
    var episodeAdpter: DetailPodcastAdapter? = null
    var artworkProminentColor = 0
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var page = 1
    var limit = 10
    var userViewModel: UserViewModel? = null
    var isFollowing = false
    var userSocialData: UserSocialData? = null

    var bookmarkDataModel: BookmarkDataModel? = null

    //var podcastDetailsRespModel: PodcastDetailsRespModel?=null
    var isPlaying = false
    var isFromVerticalPlayer = false
    var tempPodcastRespModel: PlaylistDynamicModel? = null
    var tempPodcastEpisode: PlaylistModel.Data.Body.Row.Data.Misc.Track? = null
    companion object {
        var podcastRespModel: PlaylistDynamicModel? = null
        var podcastEpisode: PlaylistModel.Data.Body.Row.Data.Misc.Track? = null
        var playableItemPosition = 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_podcast_details, container, false)
    }

    override fun initializeComponent(view: View) {
        applyButtonTheme(requireContext(), llPlayAll)
        applyButtonTheme(requireContext(), llPlayAllActionBar)
        /*val icon_sort = FontDrawable(requireContext(), R.string.icon_sort)
        icon_sort.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        ivShort?.setImageDrawable(icon_sort)*/
        if (arguments != null){
            if(requireArguments().containsKey(Constant.defaultContentId)){
                selectedContentId = requireArguments().getString(Constant.defaultContentId).toString()
                setLog(TAG, "initializeComponent: selectedContentId "+selectedContentId)
            }
            if(requireArguments().containsKey(Constant.isFromVerticalPlayer)){
                isFromVerticalPlayer = requireArguments().getBoolean(Constant.isFromVerticalPlayer)
                setLog(TAG, "initializeComponent: selectedContentId "+selectedContentId)
            }
        }
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        ivBack?.setOnClickListener { view -> backPress() }
        scrollView.viewTreeObserver.addOnScrollChangedListener(this)
        rvPodcastMain.visibility = View.VISIBLE
        //rvMain.visibility = View.VISIBLE
        rlSiri.visibility = View.GONE
        tabLayout.visibility = View.GONE
        tabView.visibility = View.GONE

        setUpPodcastDetailListViewModel()

        //setUpPodcastSimilarViewModel()
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        rlShowMore.setOnClickListener {
            setLog("rlShowMore","rlShowMore podcastEpisodeList?.size:${podcastEpisodeList?.size} count:${podcastRespModel?.data?.head?.data?.misc?.episodeCount} isLoading:${isLoading} isLastPage:${isLastPage}")
            if (podcastEpisodeList?.size!! < podcastRespModel?.data?.head?.data?.misc?.episodeCount!! && !isLoading && !isLastPage) {
                isLoading = true
                //you have to call loadmore items to get more data
                page++
                getMoreEpisode()
            }
        }

        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)
        llFollow?.setOnClickListener(this)
        llFollowActionBar?.setOnClickListener(this)


        shimmerLayoutPodcast.visibility = View.VISIBLE
        shimmerLayoutPodcast.startShimmer()

        (context as MainActivity).addPlayItemChangeListener(this)
        CommonUtils.setPageBottomSpacing(
            scrollView,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
    }

    var episodeIndex = 0
    private fun playAllPodcast() {
        /*val songData =
            selectedPodcast!!.items!!.get(selectedPodcastChildPosition!!)!!.data
        val allSongs = selectedPodcast!!.items!!
        val heading = selectedPodcast!!.heading
        //songsList = getMusicDummyData(songData)
        CommonUtils.setTrackList(
            requireContext(),
            songData,
            allSongs,
            heading,
            selectedPodcastChildPosition!!
        )*/
        /*if (podcastRespModel!!.body!!.items!!.size > 0 && episodeIndex <= podcastRespModel!!.body!!.items!!.size - 1){
            *//*for (episode in podcastRespModel!!.body!!.items!!){
                setUpPodcastEpisodeListViewModel(episode!!.data!!.id!!)
            }*//*
            setUpPodcastEpisodeListViewModel(podcastRespModel?.body?.items?.get(episodeIndex)?.data?.id!!)
        }*/

        //tracksViewModel.prepareTrackPlayback(0)
        if (isPlaying) {
            if (podcastEpisodeList != null && podcastEpisodeList?.size!! > 0) {
                playableItemPosition = 0
                setUpPlayableContentListViewModel(podcastEpisodeList?.get(playableItemPosition)?.data?.id!!)
                setEventModelDataAppLevel(
                    podcastEpisodeList?.get(
                        playableItemPosition
                    )?.data?.id!!, podcastEpisodeList?.get(
                        playableItemPosition
                    )?.data?.title!!, podcastRespModel?.data?.head?.data?.title!!
                )

            }
        } else {
            (requireActivity() as MainActivity).pausePlayer()
            playPauseStatusChange(true)
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

    fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }

    fun setArtImageBg(status: Boolean) {
        if (activity != null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                artImageUrl
            ) && chartDetailroot != null
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
                                    //(activity as AppCompatActivity).window.statusBarColor = palette?.getDominantColor(R.attr.colorPrimaryDark)!!
                                    artworkProminentColor = calculateAverageColor(bitmap, 1)
                                    //(activity as AppCompatActivity).window.statusBarColor = artworkProminentColor
                                    MainScope().launch {
                                        if (context != null) {
                                            CommonUtils.setLog(
                                                "PodcastLifecycle",
                                                "setArtImageBg--$artworkProminentColor"
                                            )
                                            changeStatusbarcolor(artworkProminentColor)
                                        }
                                    }
                                }

                                /*val hexColor = String.format(
                                    "#%06X",
                                    0xFFFFFF and (activity as AppCompatActivity).window.statusBarColor
                                )*/
                                /*val vibrant = palette.getVibrantSwatch()
                                val vibrantDark = palette.getDarkVibrantSwatch()
                                val vibrantLight = palette.getLightVibrantSwatch()
                                val muted = palette.getMutedSwatch()
                                val mutedDark = palette.getDarkMutedSwatch()
                                val mutedLight = palette.getLightMutedSwatch()*/
                                //artworkProminentColor = palette?.getDominantColor(R.attr.colorPrimaryDark)!!
                                val color2 =
                                    ColorDrawable(palette?.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))!!)
                                chartDetailBgArtImageDrawable =
                                    LayerDrawable(arrayOf<Drawable>(bgColor, color2, gradient!!))
                                //chartDetailroot.background = bgColor
                                //llHalf.background = artImage

                                // iv_collapsingImageBg.getLayoutParams().height = topView.height + resources.getDimensionPixelSize(R.dimen.dimen_60)
                                //realtimeBlurView.getLayoutParams().height = topView.height + resources.getDimensionPixelSize(R.dimen.dimen_60)
                                //mainGradientView.getLayoutParams().height = topView.height + resources.getDimensionPixelSize(R.dimen.dimen_60)
                                chartDetailroot?.background = artImage
                            }

                        }

                    }
                } catch (e: Exception) {

                }


            }
        }

    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setLog("onPageSelected", "Selected position:" + position)
        }
    }



    private fun setTabData(homeModel: PlaylistDynamicModel?) {
        if (!homeModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.isNullOrEmpty()!!) {
            homeModel.data.body.rows.get(0).data.misc.tracks.forEachIndexed { index, headItemsItem ->
                fragmentList.add(PodcastSeasonTrackFragment.newInstance(headItemsItem))
                fragmentName.add(headItemsItem.data.title)
                tabLayout.addTab(tabLayout.newTab().setText(headItemsItem?.data?.title!!))
            }
            viewPagerSetUp(homeModel)
            tabLayout?.visibility=View.VISIBLE
            tabLayout?.visibility=View.VISIBLE
        }
    }

    private fun viewPagerSetUp(homeModel: PlaylistDynamicModel) {
        tabAdapter = TabAdapter(requireActivity(), tabLayout.tabCount, fragmentList, fragmentName)
        viewPager.adapter = tabAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = fragmentName.get(position)
        }.attach()
        viewPager.offscreenPageLimit = 1
        //viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
        viewPager.isUserInputEnabled = false
        //viewPager.addOnPageChangeListener(this)


    }

    private fun getUserBookmarkedData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserBookmarkedData(requireContext(), Constant.MODULE_FAVORITE)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    fillUIs(it?.data)
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

    private fun fillUIs(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0) {
            for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!) {
                if (podcastRespModel?.data?.head?.data?.id.equals(bookmark?.data?.id)!!) {
                    setLog(TAG, "fillUI: after" + isFavorite)
                    isFavorite = true
                    setLog(TAG, "fillUI:before " + isFavorite)
                }
            }
        }
    }

    private fun setupTab() {
        for (k in 1..5) {
            tabLayout.addTab(tabLayout.newTab().setText("Season " + k))
        }
        tabLayout.addOnTabSelectedListener(this)
    }

    private fun setUpPodcastDetailListViewModel() {
        podcastListViewModel = ViewModelProvider(
            this
        ).get(PodcastViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getPodcastDetailList(requireContext(), selectedContentId!!)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")


                                fillPodcastData(it?.data!!)

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
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
        getUserBookmarkedData()
    }




    fun fillPodcastData(homedata: PlaylistDynamicModel?){

        var isPodcastHaveSeason=false
        var seasonTrack: PlaylistModel.Data.Body.Row.Data.Misc.Track? =null
        try {
            if (!homedata?.data?.body?.rows?.get(0)?.data?.misc?.tracks.isNullOrEmpty()) {
                homedata?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.forEach {
                    if (it != null && it?.data?.type == 111) {
                        seasonTrack = it
                        isPodcastHaveSeason = true
                        return@forEach
                    }
                }
            }
        } catch (e: Exception) {

        }


        setLog("Podcast","isPodcastHaveSeason:${isPodcastHaveSeason}")


        if (homedata != null) {
            //setLog(TAG, "isViewLoading $it")
            podcastRespModel = homedata
            chartDetailroot.visibility = View.VISIBLE
            view1.visibility = View.VISIBLE
            views.visibility = View.VISIBLE
            scrollView.visibility = View.VISIBLE
            setDetails(homedata!!, true)

        }
        if(isPodcastHaveSeason&&seasonTrack!=null){
            getSeasonTrackList(seasonTrack?.data?.id!!)
        }else{

            try{
                setPodcastDetailsListData(homedata!!)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }

    fun setPodcastDetailsListData(podcastModel: PlaylistDynamicModel) {

        if (podcastModel != null && podcastModel?.data?.body?.rows!= null
            && podcastModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks != null) {

            llPlayAll?.setOnClickListener(this)
            llPlayAllActionBar?.setOnClickListener(this)
            setupUserViewModel()
            if (podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks != null) {
                podcastEpisodeList = podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks!!
                //setPodcastSongDataList(podcastModel.body!!.items)
                tvEpisodesCount.setText(
                    podcastRespModel?.data?.head?.data?.misc?.episodeCount.toString() + " " + getString(
                        R.string.podcast_str_9
                    )
                )
                sortView.visibility = View.VISIBLE

                if (podcastEpisodeList?.size!! >= podcastRespModel?.data?.head?.data?.misc?.episodeCount!!) {
                    rlShowMore.visibility = View.GONE
                    isLastPage = true
                } else {
                    rlShowMore.visibility = View.VISIBLE
                }
                val layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                /*rvPodcastMain.apply {
                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                    setHasFixedSize(false)
                }*/
                rvPodcastMain.isNestedScrollingEnabled = false
                rvPodcastMain.layoutManager = layoutManager
                setPodcastEpisodeAdapter(!ascending)
                rlSort.setOnClickListener {
                    setPodcastEpisodeAdapter(ascending)
                    ascending = !ascending
                }

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val hashMapPageView = HashMap<String, String>()
                        if (podcastEpisodeList.size > 0) {
                            hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =
                                podcastEpisodeList.get(
                                    playableItemPosition
                                ).data.title
                            hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                                "" + Utils.getContentTypeNameForStream(
                                    "" + podcastEpisodeList?.get(
                                        playableItemPosition
                                    )?.data?.type
                                )
                            hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                                podcastEpisodeList.get(
                                    playableItemPosition
                                ).data.id
                            hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] =
                                MainActivity.lastItemClicked
                            hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                                "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "Podcast"
                            hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "Podcast"

                            setLog("VideoPlayerPageView", hashMapPageView.toString())
                            EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                        }
                    }catch (e:Exception){

                    }
                }
            }
            if (podcastModel.data?.body?.recomendation != null && podcastModel.data?.body?.recomendation?.size!! > 0) {
                rvRecomendation.visibility = View.VISIBLE
                rvRecomendation.visibility = View.VISIBLE

                val varient = Constant.ORIENTATION_HORIZONTAL

                val bucketParentAdapter = BucketParentAdapter(
                    podcastModel.data?.body?.recomendation!!,
                    requireContext(),
                    this,
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


                bucketParentAdapter?.addData(podcastModel.data?.body?.recomendation!!)
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
                            val fromBucket =
                                podcastModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                            val toBucket =
                                podcastModel.data?.body?.recomendation?.get(lastVisiable)?.heading
                            val sourcePage =
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
                rvRecomendation.setPadding(0, 0, 0, 0)
            }

            var isContentAutoPlay = 0
            if (requireArguments()?.containsKey("isPlay") == true) {
                isContentAutoPlay = requireArguments().getInt("isPlay")
            }
            setLog(TAG, "setPlayListSongAdapter: auto play${isContentAutoPlay}")
            if (isContentAutoPlay == 1) {
                llPlayAll?.performClick()
            }
        }
        setProgressBarVisible(false)
        shimmerLayoutPodcast?.visibility = View.GONE
        shimmerLayoutPodcast?.stopShimmer()
    }

    private fun setPodcastEpisodeAdapter(asc: Boolean) {
        val tempList: ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track> = ArrayList()
        setLog("podcastDetailsFragment", "setPodcastEpisodeAdapter-tempList.size-${tempList.size} - asc-$asc")
        podcastEpisodeList.forEachIndexed { index, track ->
            if (track!=null&&track.itype != Constant.podcastNativeAds){
                tempList.add(track)
            }
        }
        podcastEpisodeList = ArrayList()
        podcastEpisodeList = tempList
        if (asc) {
            podcastEpisodeList = podcastEpisodeList.sortedBy {
                it.data?.releasedate
            }
            tvSort.text = resources.getText(R.string.podcast_str_1)
            //ivShort.setRotation(0F)
        } else {
            podcastEpisodeList = podcastEpisodeList.sortedByDescending { it?.data?.releasedate }
            tvSort.text = resources.getText(R.string.podcast_str_2)
            //ivShort.setRotation(180F)
        }
        checkAllContentDownloadedOrNot(podcastEpisodeList)
        podcastEpisodeList = getAdsData(podcastEpisodeList)
        episodeAdpter = DetailPodcastAdapter(
            requireContext(), podcastEpisodeList,
            object : DetailPodcastAdapter.OnChildItemClick {
                override fun onUserClick(
                    childPosition: Int,
                    isMenuClick: Boolean,
                    isDownloadClick: Boolean
                ) {
                    podcastEpisode = podcastEpisodeList?.get(childPosition)
                    playableItemPosition = childPosition
                    if (isMenuClick) {
                        if (isOnClick()) {
                            commonThreeDotMenuItemSetup(PODCAST_DETAIL_ADAPTER)
                        }
                    } else if (isDownloadClick) {
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = podcastEpisodeList?.get(childPosition)?.data?.id?.toString()!!
                        dpm.contentTitle =
                            podcastEpisodeList?.get(childPosition)?.data?.title?.toString()!!
                        dpm.planName =
                            podcastEpisodeList?.get(childPosition)?.data?.misc?.movierights.toString()
                        dpm.isAudio = true
                        dpm.isDownloadAction = true
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload = RestrictedDownload.valueOf(podcastEpisodeList?.get(childPosition)?.data?.misc?.restricted_download!!)

                        Constant.screen_name ="Podcast Details"
                        if (CommonUtils.userCanDownloadContent(
                                requireContext(),
                                chartDetailroot,
                                dpm,
                                this@PodcastDetailsFragment,Constant.drawer_downloads_exhausted
                            )
                        ) {
                            val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()

                            val dq = DownloadQueue()
                            if (!TextUtils.isEmpty(podcastEpisodeList?.get(childPosition)?.data?.id.toString())) {
                                dq.contentId =
                                    podcastEpisodeList?.get(childPosition)?.data?.id.toString()
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList?.get(childPosition)?.data?.title!!)) {
                                dq.title = podcastEpisodeList?.get(childPosition)?.data?.title!!
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList?.get(childPosition)?.data?.subtitle!!)) {
                                dq.subTitle = podcastEpisodeList?.get(childPosition)?.data?.subtitle!!
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList?.get(childPosition)?.data?.image!!)) {
                                dq.image = podcastEpisodeList?.get(childPosition)?.data?.image!!
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.id!!)) {
                                dq.parentId = podcastRespModel?.data?.head?.data?.id!!
                            }
                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title!!)) {
                                dq.pName = podcastRespModel?.data?.head?.data?.title
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.subtitle!!)) {
                                dq.pSubName = podcastRespModel?.data?.head?.data?.subtitle
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.releasedate!!)) {
                                dq.pReleaseDate = podcastRespModel?.data?.head?.data?.releasedate
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.image!!)) {
                                dq.pImage = podcastRespModel?.data?.head?.data?.image
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList?.get(childPosition)?.data?.misc?.movierights.toString()!!)) {
                                dq.planName =
                                    podcastEpisodeList?.get(childPosition)?.data?.misc?.movierights.toString()
                            }


                            dq.pType = DetailPages.PODCAST_DETAIL_PAGE.value
                            dq.contentType = ContentTypes.PODCAST.value
                            val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                            dq.source = eventModel.sourceName

                            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                ?.findByContentId(podcastEpisodeList?.get(childPosition)?.data?.id!!.toString())
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(podcastEpisodeList?.get(childPosition)?.data?.id!!.toString())
                            if ((!downloadQueue?.contentId.equals(podcastEpisodeList?.get(childPosition)?.data?.id!!.toString()))
                                && (!downloadedAudio?.contentId.equals(
                                    podcastEpisodeList?.get(
                                        childPosition
                                    )?.data?.id!!.toString()
                                ))
                            ) {
                                downloadQueueList.add(dq)
                            }
                            (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                                downloadQueueList,
                                this@PodcastDetailsFragment,
                                null,
                                true,
                                true
                            )
                        }
                    } else {
                        if (isOnClick()) {
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(podcastEpisodeList?.get(childPosition)?.data?.id!!.toString())
                            if (downloadedAudio != null && downloadedAudio?.contentId.equals(
                                    podcastEpisodeList?.get(childPosition)?.data?.id!!.toString()
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
                                    podcastEpisodeList?.get(
                                        childPosition
                                    )?.data?.id!!
                                )

                                setEventModelDataAppLevel(
                                    podcastEpisodeList?.get(
                                        childPosition
                                    )?.data?.id!!, podcastEpisodeList?.get(
                                        childPosition
                                    )?.data?.title!!,
                                    podcastRespModel?.data?.head?.data?.title!!
                                )
                            }
                        }

                    }

                }

                override fun onPlayPauseClick(position: Int) {
                    if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList.size > position
                        && !BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()) {
                        val currentPlayingContentId =
                            BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                        if (currentPlayingContentId?.toString()
                                ?.equals(podcastEpisodeList.get(position).data.id)!!
                        ) {
                            if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                (requireActivity() as MainActivity).pausePlayer()
                            } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                (requireActivity() as MainActivity).playPlayer()
                            } else {
                                (requireActivity() as MainActivity).playPlayer()
                            }
                        }
                    }
                }


            })
        rvPodcastMain.adapter = episodeAdpter
    }

    private fun setDetails(it: PlaylistDynamicModel?, status: Boolean) {

        artImageUrl = podcastRespModel?.data?.head?.data?.image
        playerType = "" + podcastRespModel?.data?.head?.data?.type
        if (!TextUtils.isEmpty(artImageUrl)) {
            ImageLoader.loadImage(
                requireContext(),
                podcastAlbumArtImageView,
                artImageUrl!!,
                R.drawable.bg_gradient_placeholder
            )
            setArtImageBg(true)
        } else {
            ImageLoader.loadImage(
                requireContext(),
                podcastAlbumArtImageView,
                "",
                R.drawable.bg_gradient_placeholder
            )
            staticToolbarColor()
        }

        iv_collapsingImageBg?.visibility = View.VISIBLE
        realtimeBlurView?.visibility = View.VISIBLE
        mainGradientView?.visibility = View.VISIBLE
        if (it?.data?.head?.data?.title != null && !TextUtils.isEmpty(it?.data?.head?.data?.title)) {
            tvTitle.text = it?.data?.head?.data?.title
        } else {
            tvTitle.text = ""
        }

        if (it?.data?.head?.data?.misc?.artist != null && it?.data?.head?.data?.misc?.artist?.size!! > 0) {
            val artist = TextUtils.join("/", it?.data?.head?.data?.misc?.artist!!)
            tvSubTitle.text = getString(R.string.playlist_str_2) + " " + artist
        } else {
            tvSubTitle.text = ""
        }


        if (it?.data?.head?.data?.misc != null) {
            var subtitle = ""
            if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.f_playcount)) {
                subtitle += it?.data?.head?.data?.misc?.f_playcount + " " + context?.getString(R.string.podcast_str_7) + " • "
            }
            if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.f_FavCount)) {
                subtitle += it?.data?.head?.data?.misc?.f_FavCount + " " + context?.getString(R.string.podcast_str_8)
            }
            tvSubTitle2.text = subtitle
        } else {
            tvSubTitle2.text = ""
        }

        if (it?.data?.head?.data?.misc?.description != null && !TextUtils.isEmpty(it?.data?.head?.data?.misc?.description?.trim())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvReadMore.setText(
                    Html.fromHtml(
                        it?.data?.head?.data?.misc?.description!!,
                        Html.FROM_HTML_MODE_LEGACY
                    )
                );
            } else {
                tvReadMore.setText(Html.fromHtml(it?.data?.head?.data?.misc?.description!!));
            }
            SaveState.isCollapse = true
            tvReadMore.setShowingLine(2)
            tvReadMore.addShowMoreText("read more")
            tvReadMore.addShowLessText("read less")
            tvReadMore.setShowMoreColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvReadMore.setShowLessTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            tvReadMore.setShowMoreStyle(Typeface.BOLD)
            tvReadMore.setShowLessStyle(Typeface.BOLD)
        } else {
            tvReadMore.text = ""
        }



    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        setLog("TabSelected", "selected")
        //setUpPodcastDetailListViewModel()
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        setLog("PodcastDetailFragment", "onDestroy-isFromVerticalPlayer-$isFromVerticalPlayer")
        setLog("PodcastDetailFragment", "onDestroy-isNewSwipablePlayerOpen-${BaseActivity.isNewSwipablePlayerOpen}")
        if (isFromVerticalPlayer){
            if (activity != null){
                BaseActivity.isNewSwipablePlayerOpen = true
                (activity as MainActivity).hideMiniPlayer()
                (activity as MainActivity).hideStickyAds()
                (activity as MainActivity).showBottomNavigationAndMiniplayerBlurView()
            }
        }
        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        MainScope().launch {
            if (context != null) {
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        if (activity != null){
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


    private fun getSeasonTrackList(seasonId:String) {
        selectedContentId=seasonId
        podcastListViewModel = ViewModelProvider(
            this
        ).get(PodcastViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getPodcastDetailList(requireContext(), seasonId!!)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")

                                if(it?.data!=null&&it?.data?.data!=null){
                                    podcastRespModel?.data?.body?.rows=it?.data?.data?.body?.rows!!
                                    podcastRespModel?.data?.head?.data?.misc?.episodeCount= it.data.data.head.data.misc.episodeCount

                                    try{
                                        setPodcastDetailsListData(podcastRespModel!!)
                                    }catch (e : Exception){
                                        e.printStackTrace()
                                    }
                                }


                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
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

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPodcastSongDataList(
        playableContentModel: PlayableContentModel,
        type: String?,
        heading: String?,
        image: String?
    ) {
        val track: Track = Track()
        track.id = playableContentModel?.data?.head?.headData?.id!!.toLong()
        track.title = playableContentModel?.data?.head?.headData?.title
        track.url = playableContentModel?.data?.head?.headData?.misc?.url
        track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        track.playerType = type
        track.heading = heading
        track.image = image
        track.pType = DetailPages.PODCAST_DETAIL_PAGE.value
        track.contentType = ContentTypes.PODCAST.value

        track.explicit = playableContentModel?.data?.head?.headData?.misc?.explicit!!
        track.restrictedDownload =
            playableContentModel?.data?.head?.headData?.misc?.restricted_download!!
        track.attributeCensorRating =
            playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
        track.urlKey = playableContentModel.data.head.headData.misc.urlKey

        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        songDataList.add(track)
        setLog("SongData", BaseActivity.songDataList.toString())
    }


    private fun setUpPlayableContentListViewModel(id: String) {
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    setPlayableContentListData(it?.data)
                                } else {
                                    playableItemPosition = playableItemPosition + 1
                                    if (playableItemPosition < podcastEpisodeList?.size!!) {
                                        setUpPlayableContentListViewModel(
                                            podcastEpisodeList?.get(
                                                playableItemPosition
                                            )?.data?.id!!
                                        )
                                        setEventModelDataAppLevel(
                                            podcastEpisodeList?.get(
                                                playableItemPosition
                                            )?.data?.id!!,
                                            podcastEpisodeList?.get(
                                                playableItemPosition
                                            )?.data?.title!!,
                                            podcastRespModel?.data?.head?.data?.title!!
                                        )
                                    }
                                }

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
                setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
                //setPodcastEpisodeList(playableContentModel, podcastEpisodeList, playableItemPosition)
                songDataList = arrayListOf()

                for (i in podcastEpisodeList?.indices!!) {
                    if (playableContentModel?.data?.head?.headData?.id == podcastEpisodeList?.get(i)?.data?.id) {
                        setPodcastEpisodeList(
                            playableContentModel,
                            podcastEpisodeList,
                            playableItemPosition
                        )
                    } else if (i > playableItemPosition) {
                        setPodcastEpisodeList(null, podcastEpisodeList, i)
                    }
                }

                setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(
                    0,
                    HungamaMusicApp.getInstance()
                        .getContentDuration(playableContentModel?.data?.head?.headData?.id!!)!!
                )
            }
        }
    }

    fun setPodcastEpisodeList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row.Data.Misc.Track?>?,
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
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
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
            track.playerType = Constant.PLAYER_PODCAST_AUDIO_TRACK
        }

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title)) {
            track.heading = podcastRespModel?.data?.head?.data?.title
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

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.id!!)) {
            track.parentId = podcastRespModel?.data?.head?.data?.id!!
        }
        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title!!)) {
            track.pName = podcastRespModel?.data?.head?.data?.title
        }

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.subtitle!!)) {
            track.pSubName = podcastRespModel?.data?.head?.data?.subtitle
        }

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.image!!)) {
            track.pImage = podcastRespModel?.data?.head?.data?.image
        }

        track.pType = DetailPages.PODCAST_DETAIL_PAGE.value
        track.contentType = ContentTypes.PODCAST.value

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

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
        return songDataList
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        val dataMap= HashMap<String,String>()
        dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
        dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ podcastRespModel?.data?.head?.data?.title)

        dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
        EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_382)
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
            /* val alphaFactor: Float =
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

            /*val view = scrollView.getChildAt(scrollView.getChildCount() - 1) as View

            val diff: Int = view.bottom - (scrollView.getHeight() + scrollView
                .getScrollY())
            if (diff == 0){
                if (podcastEpisodeList?.size!! < podcastRespModel?.data?.head?.data?.misc?.episodeCount!! && !isLoading && !isLastPage){
                    isLoading = true
                    //you have to call loadmore items to get more data
                    page++
                    getMoreEpisode()
                }
            }*/
        }
    }

    private fun getMoreEpisode() {
        podcastListViewModel = ViewModelProvider(
            this
        ).get(PodcastViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getPodcastEpisodeList(requireContext(), selectedContentId.toString(), page)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setEpisodeList(it?.data)
                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
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


    private fun setEpisodeList(podcastDetailsRespModel: PlaylistDynamicModel) {
        isLoading = false
        val episodeList = ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track>()
        episodeList.addAll(podcastEpisodeList)
        episodeList.addAll(podcastDetailsRespModel.data.body.rows.get(0).data.misc.tracks)
        podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks = episodeList
        podcastEpisodeList = episodeList
        if (podcastEpisodeList.size >= podcastRespModel?.data?.head?.data?.misc?.episodeCount!!) {
            isLastPage = true
            rlShowMore.visibility = View.GONE
        }
        Handler(Looper.getMainLooper()).post {
            //episodeAdpter!!.notifyDataSetChanged()
            setPodcastEpisodeAdapter(ascending)
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2) {
            setLog(TAG, "onUserClick: is call")
            if (podcastRespModel!=null){
                commonThreeDotMenuItemSetup(PODCAST_DETAIL_PAGE, this, podcastRespModel?.data?.head?.data?.isFavorite!!)
                setLog(TAG, "onUserClick: isFavorite" + isFavorite)
            }
        } else if (id == llFollow.id || id == llFollowActionBar.id) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), llFollow!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
            if (podcastRespModel != null) {
                setFollowUnFollow(podcastRespModel?.data?.head?.data!!)
            }
        } else if (id == llPlayAll.id || id == llPlayAllActionBar.id) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), llPlayAll!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
            if (podcastRespModel != null && podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.size!! > 0) {
                playAllPodcast()
            }
        }
    }

    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getUserSocialData()
    }

    private fun setFollowUnFollow(data: PlaylistModel.Data.Head.Data) {
        if (ConnectionUtil(context).isOnline) {
            isFollowing = !isFollowing
            val jsonObject = JSONObject()
            jsonObject.put("followingId", data?.id)
            jsonObject.put("follow", isFollowing)
            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())

            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId", data?.id)
            jsonObject1.put("typeId", "" + data?.type)
            jsonObject1.put("action", isFollowing)
            jsonObject1.put("module", Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())

            setFollowingStatus()


            if(isFollowing){
                val messageModel = MessageModel(getString(R.string.artist_str_3), getString(R.string.toast_str_22),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
                baseIOScope.launch {
                    val hashMap = java.util.HashMap<String, String>()
                    hashMap.put(
                        EventConstant.ACTOR_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.actorf!!)
                    )
                    hashMap.put(
                        EventConstant.ALBUMID_EPROPERTY,
                        "" + podcastRespModel?.data?.head?.data?.id
                    )
                    hashMap.put(
                        EventConstant.CATEGORY_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.category!!)
                    )
                    var newContentId=podcastRespModel?.data?.head?.data?.id!!
                    var contentIdData=newContentId.replace("playlist-","")
                    hashMap.put(
                        EventConstant.CONTENTID_EPROPERTY, "" + contentIdData
                    )
                    val contentType=podcastRespModel?.data?.head?.data?.type!!
                    setLog(
                        TAG,
                        "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" +contentType)} contentType:${contentType}"
                    )
                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "" + Utils.getContentTypeName("" +contentType))

                    hashMap.put(
                        EventConstant.GENRE_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.genre!!)
                    )
                    hashMap.put(
                        EventConstant.LANGUAGE_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.lang!!)
                    )
                    hashMap.put(
                        EventConstant.LYRICIST_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.lyricist!!)
                    )
                    hashMap.put(
                        EventConstant.MOOD_EPROPERTY,
                        "" + podcastRespModel?.data?.head?.data?.misc?.mood
                    )
                    hashMap.put(
                        EventConstant.MUSICDIRECTOR_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.musicdirectorf!!)
                    )
                    hashMap.put(
                        EventConstant.NAME_EPROPERTY,
                        "" + podcastRespModel?.data?.head?.data?.title
                    )
                    hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "")
                    hashMap.put(
                        EventConstant.SINGER_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.singerf!!)
                    )
                    hashMap.put(
                        EventConstant.SOURCE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + podcastRespModel?.data?.head?.data?.title
                    )
                    hashMap.put(
                        EventConstant.TEMPO_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.tempo!!)
                    )
                    hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                    hashMap.put(
                        EventConstant.YEAROFRELEASE_EPROPERTY,
                        "" + DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_YYYY,
                            podcastRespModel?.data?.head?.data?.releasedate
                        )
                    )
                    EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
                }
            }else{
                val messageModel = MessageModel(getString(R.string.artist_str_18), getString(R.string.toast_str_24),
                    MessageType.NEUTRAL, true)
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

    private fun getUserSocialData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSocialData(
                requireContext(),
                SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUI(it?.data)
                            }

                        }

                        Status.LOADING -> {
                            setProgressBarVisible(false)
                        }

                        Status.ERROR -> {
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


    private fun fillUI(userSocialData: UserSocialData) {
        this.userSocialData = userSocialData
        if (userSocialData != null && userSocialData?.following != null && userSocialData?.following?.size!! > 0) {
            for (following in userSocialData?.following?.iterator()!!) {
                if (null != following) {
                    if (podcastRespModel?.data?.head?.data?.id?.equals(following?.uId!!)!!) {
                        isFollowing = true
                    }
                }
            }
            setFollowingStatus()
        }
    }

    fun setFollowingStatus() {
        if (isFollowing) {
            tvFollow?.text = getString(R.string.profile_str_5)
            tvFollowActionBar?.text = getString(R.string.profile_str_5)
            ivFollow.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_following,
                    R.color.colorWhite
                )
            )
            ivFollowActionBar.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_following,
                    R.color.colorWhite
                )
            )
            podcastRespModel?.data?.head?.data?.isFavorite=true
        } else {
            tvFollow?.text = getString(R.string.profile_str_2)
            tvFollowActionBar?.text = getString(R.string.profile_str_2)
            ivFollow.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_follow,
                    R.color.colorWhite
                )
            )
            ivFollowActionBar.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_follow,
                    R.color.colorWhite
                )
            )
            podcastRespModel?.data?.head?.data?.isFavorite=false
        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
            setLog("DWProgrss-onChangedid", data.id.toString())
            setLog("DWProgrss-onChanged", reason.toString())
            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByDownloadManagerId(data.id)
            val downloadedAudio =
                AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

            when (reason) {
                Reason.DOWNLOAD_ADDED -> {
                    setLog("DWProgrss-ADDED", data.id.toString())
                }
                Reason.DOWNLOAD_QUEUED -> {
                    setLog("DWProgrss-QUEUED", data.id.toString())
                    if (episodeAdpter != null) {
                        if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        }
                    }
                }
                Reason.DOWNLOAD_STARTED -> {
                    setLog("DWProgrss-STARTED", data.id.toString())
                    if (episodeAdpter != null) {
                        if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
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
                    if (episodeAdpter != null) {
                        if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        } else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
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
        if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList?.size!! > 0) {
            checkAllContentDownloadedOrNot(podcastEpisodeList!!)
        } else {
            playPauseStatusChange(true)
        }
        setLog("PodcastDetailFragment", "onResume-isFromVerticalPlayer-$isFromVerticalPlayer")
        setLog("PodcastDetailFragment", "onResume-isNewSwipablePlayerOpen-${BaseActivity.isNewSwipablePlayerOpen}")
        BaseActivity.isNewSwipablePlayerOpen = false
        showBottomNavigationAndMiniplayer()
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    override fun playItemChange() {
        if (episodeAdpter != null && isVisible) {
            setLog(
                "TAG",
                "bind: content id : play track id: ${(activity as BaseActivity).fetchTrackData().id} title:${(activity as BaseActivity).fetchTrackData().title}"
            )

            episodeAdpter?.notifyDataSetChanged()
        }
    }

    private fun checkAllContentDownloadedOrNot(podcastEpisodeList: List<PlaylistModel.Data.Body.Row.Data.Misc.Track?>) {
        if (isAdded){
            var isCurrentContentPlayingFromThis = false
            if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList?.size!! > 0) {
                if (podcastRespModel != null && podcastRespModel?.data?.head?.data != null) {
                    var index = 0
                    for (item in podcastEpisodeList.iterator()) {
                        if (!isCurrentContentPlayingFromThis && !BaseActivity?.songDataList.isNullOrEmpty()
                            && BaseActivity?.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                        ) {
                            val currentPlayingContentId =
                                BaseActivity?.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                            if (currentPlayingContentId?.toString()?.equals(item?.data?.id)!!) {
                                isCurrentContentPlayingFromThis = true
                                if (episodeAdpter != null) {
                                    episodeAdpter?.notifyDataSetChanged()
                                }
                                if (activity != null){
                                    if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                        playPauseStatusChange(false)
                                    } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
//                                playPauseStatusChange(true)
                                    } else {
                                        playPauseStatusChange(true)
                                    }
                                }

                            } else {
                                playPauseStatusChange(true)
                            }
                        }
                        index++
                    }
                }
            }
        }
    }

    private fun playPauseStatusChange(status: Boolean) {
        isPlaying = status
        if (status) {
            ivPlayAll?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_play_2,
                    R.color.colorWhite
                )
            )
            tvPlayAll?.text = getString(R.string.podcast_str_4)
            ivPlayAllActionBar?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_play_2,
                    R.color.colorWhite
                )
            )
            tvPlayAllActionBar?.text = getString(R.string.podcast_str_4)
        } else {
            ivPlayAll?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_pause,
                    R.color.colorWhite
                )
            )
            tvPlayAll?.text = getString(R.string.general_str)
            ivPlayAllActionBar?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_pause,
                    R.color.colorWhite
                )
            )
            tvPlayAllActionBar?.text = getString(R.string.general_str)
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
                if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList?.size!! > 0) {
                    checkAllContentDownloadedOrNot(podcastEpisodeList!!)
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
        setLog("podcastLifecycle", "onHiddenChanged-$hidden")
        if (!hidden) {
            podcastRespModel = tempPodcastRespModel
            podcastEpisode = tempPodcastEpisode
            if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList?.size!! > 0) {
                checkAllContentDownloadedOrNot(podcastEpisodeList!!)
            } else {
                playPauseStatusChange(true)
            }
            MainScope().launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "ChartLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        } else {
            tempPodcastRespModel = podcastRespModel
            tempPodcastEpisode = podcastEpisode
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

    private fun getAdsData(podcastList: List<PlaylistModel.Data.Body.Row.Data.Misc.Track>): ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track> {
        val podEpisodeList: ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track> =
            ArrayList()
        if (!podcastList.isNullOrEmpty()){
            podEpisodeList.addAll(podcastList)
            if (CommonUtils.isDisplayAds() && CommonUtils.getFirebaseConfigAdsData().podcastDetailsPageNativeAd.displayAd) {
                val adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().podcastDetailsPageNativeAd.firstAdPositionAfterEpisodes
                val adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().podcastDetailsPageNativeAd.repeatFrequencyAfterRows
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
                val iterator = podEpisodeList.listIterator()
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

                        val podcastRow = PlaylistModel.Data.Body.Row.Data.Misc.Track()
                        podcastRow.itype = Constant.podcastNativeAds

                        if (adTotalIds > adIdCount) {
                            //setLog("adInserted-3", adIdCount.toString())
                            //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                            podcastRow.adUnitId = adUnitIdList.get(adIdCount)
                            adIdCount++
                        } else {
                            adIdCount = 0
                            podcastRow.adUnitId = adUnitIdList.get(adIdCount)
                            //setLog("adInserted-4", adIdCount.toString())
                            //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                            adIdCount++
                        }

                        iterator.add(podcastRow)
                    }
                    val item = iterator.next()
                    i++
                    k++
                }
            }
        }
        return podEpisodeList
    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        super.onContentLikedFromThreeDotMenu(isFavorite, position)
        if (podcastEpisodeList != null && podcastEpisodeList?.size!! > 0) {
            podcastEpisodeList?.get(position)?.data?.isFavorite = isFavorite
        }
    }
    private fun redirectToMoreBucketListPage(bodyRowsItemsItem: ArrayList<BodyRowsItemsItem?>?, heading: String) {
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
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
    }
}