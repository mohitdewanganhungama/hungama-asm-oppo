package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Util
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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
import com.hungama.music.ui.main.adapter.*
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.ArtistViewModel
import com.hungama.music.ui.main.viewmodel.HomeViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.ARTIST_DETAIL_PAGE
import com.hungama.music.utils.Constant.EXTRA_IS_MORE_PAGE
import com.hungama.music.utils.Constant.EXTRA_MORE_PAGE_NAME
import com.hungama.music.utils.Constant.defaultContentId
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_artist_details.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

// Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ArtistDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArtistDetailsFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnLocalBroadcastEventCallBack,
    BucketParentAdapter.OnMoreItemClick {
    var artImageUrl: String? = null

    //var artImageUrl = "https://files.hubhopper.com/podcast/313123/storytime-with-gurudev-sri-sri-ravi-shankar.jpg?v=1598432706&s=hungama"
    var selectedContentId: String? = null
    var playerType: String? = null

    var tabAdapter: TabAdapter? = null
    var artistViewModel: ArtistViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    var bucketRespModel: HomeModel? = null
    var artistRespModel = PlaylistDynamicModel()
    var rowList: MutableList<RowsItem?>? = null

    var topSongList: ArrayList<BodyRowsItemsItem?>? = null
    var topSongIndex = -1
    var selectedPodcast: RowsItem? = null
    var selectedPodcastChildPosition: Int? = null
    private var tracksViewModel: TracksContract.Presenter? = null
    var selectedArtistSongImage = ""
    var selectedArtistSongTitle = ""
    var selectedArtistSongSubTitle = ""
    var selectedArtistSongHeading = ""
    var playableItemPosition = 0
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var artworkProminentColor = 0
    var userViewModel:UserViewModel? = null
    var isFollowing = false
    var userSocialData: UserSocialData? = null
    var isMorePage = false
    var morePageName = ""
    private var bucketParentAdapter: BucketParentAdapter? = null
    companion object{
        var artistModel: PlaylistDynamicModel?=null
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
        return inflater.inflate(R.layout.fragment_artist_details, container, false)
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            applyButtonTheme(requireContext(), llPlayAllArtist)
            applyButtonTheme(requireContext(), llPlayAllArtistActionBar)
            if (arguments != null){
                //        artImageUrl =requireArguments().getString("image").toString()
                if (requireArguments().containsKey(defaultContentId)){
                    selectedContentId = requireArguments().getString(defaultContentId).toString()
                }
//        playerType = requireArguments().getString("playerType").toString()
                if (requireArguments().containsKey(EXTRA_IS_MORE_PAGE)){
                    isMorePage = requireArguments().getBoolean(EXTRA_IS_MORE_PAGE, false)
                }
                if (requireArguments().containsKey(EXTRA_MORE_PAGE_NAME)){
                    morePageName = requireArguments().getString(EXTRA_MORE_PAGE_NAME).toString()
                }

            }
            scrollView?.hide()
            shimmerLayout?.show()
            shimmerLayout?.startShimmer()
            ivBack?.setOnClickListener { view -> backPress() }
            ivBack2?.setOnClickListener { view -> backPress() }
            rlHeading?.visibility = View.INVISIBLE

            scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@ArtistDetailsFragment)

            setupArtistViewModel()
            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@ArtistDetailsFragment)
            llPlayAllArtist?.setOnClickListener {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(requireContext(), llPlayAllArtist!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                }catch (e:Exception){

                }
                if (!topSongList.isNullOrEmpty()) {
                    setProgressBarVisible(true)
                    playAllArtistTopSong()
                }

            }
            llPlayAllArtistActionBar?.setOnClickListener {
                if (!topSongList.isNullOrEmpty()) {
                    setProgressBarVisible(true)
                    playAllArtistTopSong()
                }
            }

            threeDotMenu?.setOnClickListener(this@ArtistDetailsFragment)
            threeDotMenu2?.setOnClickListener(this@ArtistDetailsFragment)
            llFollow?.setOnClickListener(this@ArtistDetailsFragment)
            llFollowActionBar?.setOnClickListener(this@ArtistDetailsFragment)

            CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        }
    }

    var episodeIndex = 0

    private fun playAllArtistTopSong() {
        baseMainScope.launch {
            if (isPlaying) {
                if (!topSongList.isNullOrEmpty()) {
                    playableItemPosition = 0
                    baseIOScope.launch {
                        setEventModelDataAppLevel(
                            topSongList?.get(0)?.data?.id.toString(),
                            topSongList?.get(0)?.data?.title.toString(),
                            artistModel?.data?.head?.data?.title.toString()
                        )
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        val hashMapPageView = HashMap<String, String>()

                        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =topSongList?.get(0)?.data?.title.toString()
                        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                            "" + Utils.getContentTypeNameForStream("" + topSongList?.get(0)?.data?.type.toString())
                        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                            topSongList?.get(0)?.data?.id.toString()
                        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                            "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "Artist"
                        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "details_artist"

                        setLog("VideoPlayerPageView", hashMapPageView.toString())
                        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                    }

                    setUpPlayableContentListViewModel(topSongList?.get(0)?.data?.id.toString())
                }
            } else {
                (requireActivity() as MainActivity).pausePlayer()
                playPauseStatusChange(true)
            }
        }

    }

    private fun staticToolbarColor() {
        //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
        baseMainScope.launch {
                if (context != null) {
                    changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //(activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
        return onCreateOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //(activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
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
        baseIOScope.launch {
            try {
                if (activity != null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                        artImageUrl
                    ) && movieDetailroot != null
                ) {
                    val result: Deferred<Bitmap?> = baseIOScope.async {
                        val urlImage = URL(artImageUrl)
                        urlImage.toBitmap()
                    }

                    baseIOScope.launch(Dispatchers.IO) {
                        try {
                            // get the downloaded bitmap
                            val bitmap: Bitmap? = result.await()
                            val artImage = BitmapDrawable(resources, bitmap)
                            if (status) {
                                if (bitmap != null) {
                                    //val color = dynamicToolbarColor(bitmap)
                                    Palette.from(bitmap!!).generate { palette ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            artworkProminentColor = CommonUtils.calculateAverageColor(bitmap, 1)
                                            baseMainScope.launch {
                                                if (context != null) {
                                                    CommonUtils.setLog(
                                                        "ArtistLifecycle",
                                                        "setArtImageBg--$artworkProminentColor"
                                                    )
                                                    changeStatusbarcolor(artworkProminentColor)
                                                }
                                            }
                                        }
                                    }

                                }

                            }
                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }


                    }
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }

    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setLog("onPageSelected", "Selected position:" + position)
        }
    }

    private fun setupArtistViewModel() {
        try {
            if (isAdded && context != null){
                if (ConnectionUtil(context).isOnline) {
                artistViewModel = ViewModelProvider(
                    this
                ).get(ArtistViewModel::class.java)

                artistViewModel?.getArtistDetail(requireContext(), selectedContentId.toString())?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
//                            if(Constant.IS_SEARCH_RECOMMENDED_DISPLAY){
//                                getTrendCall(it?.data!!)
//                            }else{
                                fillArtistDetail(it?.data!!)
//                            }
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
            }

        }catch (e:Exception){

        }
    }

    fun getTrendCall(homedata: PlaylistDynamicModel?) {
        try {
            if (isAdded && context != null) {
                val homeViewModel = ViewModelProvider(
                    this
                ).get(HomeViewModel::class.java)
                val url = WSConstants.METHOD_TRENDING_ARTIST + "?contentId=" + selectedContentId
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
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
                                fillArtistDetail(homedata!!)
                            }
                        }
                    })
            }
        } catch (e: Exception) {

        }
    }

    fun getYouMayLikeCall(homedata: PlaylistDynamicModel?) {
        try {
            if (isAdded && context != null) {
                val homeViewModel = ViewModelProvider(
                    this
                ).get(HomeViewModel::class.java)
                val url = WSConstants.METHOD_YOU_MAY_LIKE_ARTIST + "?contentId=" + selectedContentId
                homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {

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
                                fillArtistDetail(homedata!!)



                                setLog(
                                    TAG,
                                    "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}"
                                )
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
                                fillArtistDetail(homedata!!)
                            }
                        }
                    })
            }
        } catch (e: Exception) {

        }
    }

    private fun fillArtistDetail(data: PlaylistDynamicModel) {
        setMovieTrailerListData(data)
        setDetails(data, true)
    }


    private fun setDetails(it: PlaylistDynamicModel?, status: Boolean) {
        artistModel =it
        artImageUrl= artistModel?.data?.head?.data?.image
        playerType=""+ artistModel?.data?.head?.data?.type
        baseMainScope.launch {
            if (isAdded && context != null){
                if (!TextUtils.isEmpty(artImageUrl)) {
                ImageLoader.loadImage(
                    requireContext(),
                    artistAlbumArtImageView,
                    artImageUrl!!,
                    R.drawable.bg_gradient_placeholder
                )
                setArtImageBg(true)
            } else {
                ImageLoader.loadImage(
                    requireContext(),
                    artistAlbumArtImageView,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                staticToolbarColor()
            }

            if (!TextUtils.isEmpty(it?.data?.head?.data?.title)) {
                tvTitle?.text = it?.data?.head?.data?.title
            } else {
                tvTitle?.text = ""
            }

            if (it?.data?.head?.data?.misc != null) {
                var subTitle = ""
                if (!TextUtils.isEmpty("" + it?.data?.head?.data?.misc?.f_playcount)) {
                    subTitle += "" + it?.data?.head?.data?.misc?.f_playcount + " " + getString(R.string.discover_str_24)
                }

                if (!TextUtils.isEmpty("" + it?.data?.head?.data?.misc?.f_FavCount)) {

                    subTitle += " • " + it?.data?.head?.data?.misc?.f_FavCount + " " + getString(R.string.artist_str_5)
                }
                tvPlay?.text = subTitle
            } else {
                tvPlay?.text = ""
            }

            shimmerLayout?.hide()
            shimmerLayout?.stopShimmer()
            scrollView?.show()
            }
        }
    }

    fun setMovieTrailerListData(artistModel: PlaylistDynamicModel) {

        if (artistModel != null && artistModel?.data?.body != null) {
            artistRespModel = artistModel
            setupUserViewModel()
            setUpAllBucket(artistModel)
            //setArtistTopSongData(artistModel)
            /*setArtistLatestNewsData(artistModel)
            setArtistNewReleaseData(artistModel)
            setArtistAlbumData(artistModel)
            setArtistMusicVideoData(artistModel)
            setArtistTVShowData(artistModel)
            setArtistUpcomingData(artistModel)
            setArtistMerchandiseData(artistModel)
            setArtistLikeData(artistModel)
            setMovieData(artistModel)*/
        }
    }

    private fun setUpAllBucket(artistModel: PlaylistDynamicModel){
        try {
            baseMainScope.launch {
                if (isAdded && context != null){
                    if (artistModel?.data?.body != null && artistModel.data?.body?.recomendation != null && !artistModel.data?.body?.recomendation.isNullOrEmpty()) {

                        artistModel.data.body.recomendation?.forEachIndexed { index, rowsItem ->
                            if (rowsItem != null && !rowsItem.keywords.isNullOrEmpty()) {
                                if (rowsItem.keywords?.get(0).equals("top-songs")) {
                                    topSongIndex = index
                                }
                            }
                        }

                        if(topSongIndex!=-1){
                            topSongList = artistModel.data.body.recomendation?.get(topSongIndex)?.items
                        }

                        rvRecomendation.visibility = View.VISIBLE

                        var varient = Constant.ORIENTATION_HORIZONTAL

                        bucketParentAdapter = BucketParentAdapter(
                            artistModel.data?.body?.recomendation!!,
                            requireContext(),
                            this@ArtistDetailsFragment,
                            this@ArtistDetailsFragment,
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


                        bucketParentAdapter?.addData(artistModel.data?.body?.recomendation!!)
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
                                        artistModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                                    var toBucket =
                                        artistModel.data?.body?.recomendation?.get(lastVisiable)?.heading
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
                        rvRecomendation.setPadding(0, 0, 0, 0)
                    }

                    async { checkAllContentDownloadedOrNot(topSongList) }.await()
                }
            }
        }catch (e:Exception){

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


    private fun setUpPodcastEpisodeListViewModel(id: String) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data!= null) {
                                setPodcastEpisodeListData(it?.data!!)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }
    fun setPodcastEpisodeListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (isAdded && context != null){
                if (playableContentModel != null) {
                    setLog("PodcastEpisode", playableContentModel?.data?.head?.headData?.id.toString())
                    setPodcastSongDataList(
                        playableContentModel,
                        playerType,
                        topSongList?.get(episodeIndex)?.data?.title,
                        topSongList?.get(episodeIndex)?.data?.playble_image
                    )
                    if (episodeIndex == topSongList?.size!! - 1 || episodeIndex > Constant.MAX_PLAYLIST_SIZE) {
                        BaseActivity.setTrackListData(songDataList)
                        setProgressBarVisible(false)
                        tracksViewModel?.prepareTrackPlayback(0)
                    } else {
                        episodeIndex++
                        playAllArtistTopSong()
                    }
                }
            }
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
        track.id = playableContentModel.data.head.headData.id.toLong()
        track.title = playableContentModel.data.head.headData.title
        track.url = playableContentModel.data.head.headData.misc.url
        track.drmlicence = playableContentModel.data.head.headData.misc.downloadLink.drm.token
        track.playerType = type
        track.heading = heading
        track.image = image
        track.pType = DetailPages.ARTIST_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        track.explicit = playableContentModel.data.head.headData.misc.explicit
        track.restrictedDownload = playableContentModel.data.head.headData.misc.restricted_download
        track.attributeCensorRating =
            playableContentModel.data.head.headData.misc.attributeCensorRating.toString()
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
        try {
            if (isAdded && context != null){
                playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)


            if (ConnectionUtil(context).isOnline) {
                playableContentViewModel.getPlayableContentList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    //setLog(TAG, "isViewLoading $it")
                                    if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                        setPlayableContentListData(it?.data!!)
                                    }else{
                                        playableItemPosition = playableItemPosition + 1
                                        if (playableItemPosition < topSongList?.size!!) {
                                            setUpPlayableContentListViewModel(topSongList?.get(playableItemPosition)?.data?.id!!)
                                        }
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
            }
        }catch (e:Exception){

        }


    }


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (isAdded && context != null){
                if (playableContentModel != null) {
            if (!CommonUtils.checkExplicitContent(requireContext(), playableContentModel.data.head.headData.misc.explicit)){
                setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            //setPodcastEpisodeList(playableContentModel)
            songDataList = arrayListOf()

            for (i in topSongList?.indices!!) {
                if (playableContentModel?.data?.head?.headData?.id == topSongList?.get(i)?.data?.id) {
                    setArtistContentList(playableContentModel, topSongList, playableItemPosition)
                } else if (i > playableItemPosition) {
                    setArtistContentList(null, topSongList, i)
                }
            }

            BaseActivity.setTrackListData(songDataList)
            tracksViewModel?.prepareTrackPlayback(0)
            }
        }
            }
        }

    }

    fun setArtistContentList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<BodyRowsItemsItem?>?,
        position: Int
    ) {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            try {
                track.id = Integer.parseInt(playableItem?.get(position)?.data?.id!!).toLong()
            } catch (e: NumberFormatException) {
                // Log error, change value of temperature, or do nothing
            }
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)) {
            track.title = playableItem?.get(position)?.data?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subTitle)) {
            track.subTitle = playableItem?.get(position)?.data?.subTitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }

        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.type.toString())) {
            track.playerType = playableItem?.get(position)?.data?.type.toString()
        } else {
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(selectedArtistSongHeading)) {
            track.heading = selectedArtistSongHeading
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

        if (!TextUtils.isEmpty(artistRespModel.data.head.data.id)){
            track.parentId = artistRespModel.data.head.data.id
        }
        if (!TextUtils.isEmpty(artistRespModel.data.head.data.title)){
            track.pName = artistRespModel.data.head.data.title
        }

        if (!TextUtils.isEmpty(artistRespModel.data.head.data.subtitle)){
            track.pSubName = artistRespModel.data.head.data.subtitle
        }

        if (!TextUtils.isEmpty(artistRespModel.data.head.data.image)){
            track.pImage = artistRespModel.data.head.data.image
        }
        track.pType = DetailPages.ARTIST_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_373)
            /* how much we have scrolled */
            val movement = scrollView.scrollY
            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance) {
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.INVISIBLE
                rlHeading.visibility = View.VISIBLE
                if (artworkProminentColor == 0){
                    rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                }else{
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

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2) {
            commonThreeDotMenuItemSetup(ARTIST_DETAIL_PAGE)
        }else if (id == llFollow.id || id == llFollowActionBar.id){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llFollow!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (artistRespModel != null){
                setFollowUnFollow(artistRespModel?.data?.head?.data)
            }
        }
    }

    private fun setupUserViewModel() {
        if (isAdded && context != null){
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)

            getUserSocialData()
        }

    }

    private fun setFollowUnFollow(data: PlaylistModel.Data.Head.Data?){
        if (ConnectionUtil(context).isOnline) {
            isFollowing = !isFollowing
            val jsonObject = JSONObject()
            jsonObject.put("followingId",data?.id)
            jsonObject.put("follow",isFollowing)
            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())

            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId",data?.id)
            jsonObject1.put("typeId",""+data?.type)
            jsonObject1.put("action",isFollowing)
            jsonObject1.put("module",Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())

            setFollowingStatus()

            if (isFollowing){
                val messageModel = MessageModel(getString(R.string.artist_str_3), getString(R.string.artist_str_17),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)

                baseIOScope.launch {
                    if (isAdded && context != null) {
                        val hashMap = java.util.HashMap<String, String>()
                        hashMap.put(
                            EventConstant.ACTOR_EPROPERTY,
                             Utils.arrayToString(artistRespModel?.data?.head?.data?.misc?.actorf!!)
                        )
                        hashMap.put(
                            EventConstant.ALBUMID_EPROPERTY,
                            "" + artistRespModel?.data?.head?.data?.id
                        )
                        hashMap.put(
                            EventConstant.CATEGORY_EPROPERTY,
                             Utils.arrayToString(artistRespModel?.data?.head?.data?.category!!)
                        )
                        var newContentId=artistRespModel?.data?.head?.data?.id!!

                        var contentIdData=newContentId.replace("artist-","")

                        hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" +contentIdData)


                        val contentType=artistRespModel?.data?.head?.data?.type
                        setLog(
                            TAG,
                            "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" +contentType)} contentType:${contentType}"
                        )
                        hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "" + Utils.getContentTypeName("" +contentType))

                        hashMap.put(
                            EventConstant.GENRE_EPROPERTY,
                            Utils.arrayToString(artistRespModel?.data?.head?.data?.genre!!)
                        )
                        hashMap.put(
                            EventConstant.LANGUAGE_EPROPERTY,
                            Utils.arrayToString(artistRespModel?.data?.head?.data?.misc?.lang!!)
                        )
                        hashMap.put(
                            EventConstant.LYRICIST_EPROPERTY,
                            Utils.arrayToString(artistRespModel?.data?.head?.data?.misc?.lyricist!!)
                        )
                        hashMap.put(
                            EventConstant.MOOD_EPROPERTY,
                            "" + artistRespModel?.data?.head?.data?.misc?.mood
                        )
                        hashMap.put(
                            EventConstant.MUSICDIRECTOR_EPROPERTY,
                            Utils.arrayToString(artistRespModel?.data?.head?.data?.misc?.musicdirectorf!!)
                        )
                        hashMap.put(
                            EventConstant.NAME_EPROPERTY,
                            "" + artistRespModel?.data?.head?.data?.title
                        )
                        hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.SINGER_EPROPERTY,
                            Utils.arrayToString(artistRespModel?.data?.head?.data?.misc?.singerf!!)
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + artistRespModel?.data?.head?.data?.title
                        )
                        hashMap.put(
                            EventConstant.TEMPO_EPROPERTY,
                            Utils.arrayToString(artistRespModel?.data?.head?.data?.misc?.tempo!!)
                        )
                        hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                        hashMap.put(
                            EventConstant.YEAROFRELEASE_EPROPERTY,
                            "" + DateUtils.convertDate(
                                DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                                DateUtils.DATE_YYYY,
                                artistRespModel?.data?.head?.data?.releasedate
                            )
                        )
                        EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
                    }
                }

            }else{
                val messageModel = MessageModel(getString(R.string.artist_str_18), getString(R.string.artist_str_19),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun getUserSocialData() {
        try {
            if (isAdded && context != null) {
                if (ConnectionUtil(requireContext()).isOnline) {
                    userViewModel?.getUserSocialData(
                        requireContext(),
                        SharedPrefHelper.getInstance().getUserId()!!
                    )?.observe(this,
                        Observer {
                            when(it.status){
                                Status.SUCCESS->{
                                    setProgressBarVisible(false)
                                    if (it?.data != null) {
                                        fillUI(it?.data)
                                    }

                                }

                                Status.LOADING ->{
                                    setProgressBarVisible(true)
                                }

                                Status.ERROR ->{
                                    setProgressBarVisible(false)
                                    Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                }
                            }
                        })
                }else{
                    val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                        MessageType.NEGATIVE, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
            }
        }catch (e:Exception){

        }
    }

    private fun fillUI(userSocialData: UserSocialData) {
        this.userSocialData = userSocialData
        baseIOScope.launch {
            if (isAdded && context != null) {
                if (userSocialData != null && userSocialData?.following != null && userSocialData?.following?.size!! > 0){
                    for (following in userSocialData?.following?.iterator()!!){
                        if (null != following) {
                            if (artistRespModel?.data?.head?.data?.id?.equals(following?.uId!!)!!) {
                                isFollowing = true
                            }
                        }
                    }
                    setFollowingStatus()
                }
            }
        }
    }

    fun setFollowingStatus(){
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (isFollowing){
                    tvFollow?.text = getString(R.string.profile_str_5)
                    tvFollowActionBar?.text = getString(R.string.profile_str_5)
                    ivFollow?.setImageDrawable(requireContext().faDrawable(R.string.icon_following, R.color.colorWhite))
                    ivFollowActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_following, R.color.colorWhite))
                }else{
                    tvFollow?.text = getString(R.string.profile_str_2)
                    tvFollowActionBar?.text = getString(R.string.profile_str_2)
                    ivFollow?.setImageDrawable(requireContext().faDrawable(R.string.icon_follow, R.color.colorWhite))
                    ivFollowActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_follow, R.color.colorWhite))
                }
            }
        }

    }



    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        if (!topSongList.isNullOrEmpty()) {
            checkAllContentDownloadedOrNot(topSongList)
        } else {
            playPauseStatusChange(true)
        }
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (!topSongList.isNullOrEmpty()) {
                    checkAllContentDownloadedOrNot(topSongList)
                } else {
                    playPauseStatusChange(true)
                }
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            if (!topSongList.isNullOrEmpty()) {
                checkAllContentDownloadedOrNot(topSongList)
            } else {
                playPauseStatusChange(true)
            }
            baseMainScope.launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "ChartLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        } else {
            baseMainScope.launch {
                if (context != null) {
                    changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
                }
            }
        }
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        baseMainScope.launch {
            val bundle = Bundle()
            bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
            setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
            setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
            val moreBucketListFragment = MoreBucketListFragment()
            moreBucketListFragment.arguments = bundle
            addFragment(R.id.fl_container, this@ArtistDetailsFragment, moreBucketListFragment, false)

            val dataMap= HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ artistModel?.data?.head?.data?.title)

            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        if (parent != null
            && !parent.items.isNullOrEmpty()
            && parent.items?.size!! > childPosition
            && parent.items?.get(childPosition)?.data != null
            && parent.items?.get(childPosition)?.data?.type.equals("21", true) && artistModel!=null){
            selectedArtistSongImage =
                parent.items?.get(childPosition)?.data?.image!!
            selectedArtistSongTitle =
                parent.items?.get(childPosition)?.data?.title!!
            selectedArtistSongSubTitle =
                parent.items?.get(childPosition)?.data?.subTitle!!
            selectedArtistSongHeading = artistModel?.data?.head?.data?.title!!
            playableItemPosition = childPosition
            setUpPlayableContentListViewModel(
                parent.items?.get(childPosition)?.data?.id!!
            )
            setEventModelDataAppLevel( parent.items?.get(childPosition)?.data?.id.toString(),
                parent.items?.get(childPosition)?.data?.title.toString(),
                artistModel?.data?.head?.data?.title.toString()
            )
        }else{
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading.toString())
        }
    }

    override fun onDestroy() {
        baseMainScope.launch {
            if (context != null){
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
        super.onDestroy()
        tracksViewModel?.onCleanup()
    }

    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1
    var isPlaying = false
    private suspend fun checkAllContentDWOrNot(topSongList: ArrayList<BodyRowsItemsItem?>?):Boolean {
        try {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!topSongList.isNullOrEmpty()) {
                    try {
                        topSongList.forEachIndexed { index, it ->
                            if (it != null){
                                if (!isCurrentContentPlayingFromThis && !BaseActivity.songDataList.isNullOrEmpty()
                                    && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                                ) {
                                    val currentPlayingContentId =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                                    if (currentPlayingContentId?.toString()?.equals(it.data?.id)!!) {
                                        it.data?.isCurrentPlaying = true
                                        isCurrentContentPlayingFromThis = true
                                        setLog("isCurrentPlaying", "DetailChartAdapter-checkAllContentDWOrNot-1-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex-index-$index")
                                        if (currentPlayingContentIndex >= 0){
                                        }else{
                                            lastPlayingContentIndex = index
                                        }
                                        currentPlayingContentIndex = index
                                    } else {
                                        if (it.data?.isCurrentPlaying == true){
                                            lastPlayingContentIndex = index
                                        }
                                        it.data?.isCurrentPlaying = false
                                        playPauseStatusChange(true)
                                    }
                                } else {
                                    if (it.data?.isCurrentPlaying == true){
                                        lastPlayingContentIndex = index
                                    }
                                    it.data?.isCurrentPlaying = false
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
                return isCurrentContentPlayingFromThis
            }
        } catch (e: Exception) {

        }

        return false
    }

    private fun checkAllContentDownloadedOrNot(topSongList: ArrayList<BodyRowsItemsItem?>?) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!topSongList.isNullOrEmpty()) {
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
                                checkAllContentDWOrNot(topSongList)
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
                                if (bucketParentAdapter != null) {
                                    setLog("isCurrentPlaying", "DetailChartAdapter-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                                    bucketParentAdapter?.notifyItemChanged(lastPlayingContentIndex)
                                    bucketParentAdapter?.notifyItemChanged(currentPlayingContentIndex)
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

    private fun playPauseStatusChange(status: Boolean) {
        isPlaying = status
        baseMainScope.launch {
            if (isAdded && context != null) {
                val color = R.color.colorWhite
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
}