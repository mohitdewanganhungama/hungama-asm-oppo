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
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.common.util.Util
import com.google.gson.Gson
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.AddedToWatchlist
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.eventanalytic.eventreporter.RemovedFromWatchListEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.player.download.DownloadTracker
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.*
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.*
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.CONTENT_MOVIE
import com.hungama.music.utils.Constant.MODULE_WATCHLIST
import com.hungama.music.utils.Constant.MOVIE_DETAIL_PAGE
import com.hungama.music.utils.Constant.VIDEO_ACTIVITY_RESULT_CODE
import com.hungama.music.utils.Constant.VIDEO_LIST_DATA
import com.hungama.music.utils.Constant.VIDEO_SHORT_FILMS
import com.hungama.music.utils.Constant.VIDEO_START_POSITION
import com.hungama.music.utils.Constant.defaultContentPlayerType
import com.hungama.music.utils.Constant.isPlay
import com.hungama.music.utils.Constant.stopReasonPause
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_movie_v1.*
import kotlinx.android.synthetic.main.fragment_movie_v1.contentStatusProgress
import kotlinx.android.synthetic.main.fragment_movie_v1.headBlur
import kotlinx.android.synthetic.main.fragment_movie_v1.ivDetailBtnIcon
import kotlinx.android.synthetic.main.fragment_movie_v1.ivDetailBtnIconActionBar
import kotlinx.android.synthetic.main.fragment_movie_v1.ivDownload
import kotlinx.android.synthetic.main.fragment_movie_v1.ivWatchlist
import kotlinx.android.synthetic.main.fragment_movie_v1.llDetails3
import kotlinx.android.synthetic.main.fragment_movie_v1.llDetailsActions
import kotlinx.android.synthetic.main.fragment_movie_v1.llPlayMovie
import kotlinx.android.synthetic.main.fragment_movie_v1.llPlayMovieActionBar
import kotlinx.android.synthetic.main.fragment_movie_v1.llToolbar
import kotlinx.android.synthetic.main.fragment_movie_v1.llWatchlist
import kotlinx.android.synthetic.main.fragment_movie_v1.movieAlbumArtImageView
import kotlinx.android.synthetic.main.fragment_movie_v1.movieDetailroot
import kotlinx.android.synthetic.main.fragment_movie_v1.rlDownload
import kotlinx.android.synthetic.main.fragment_movie_v1.rlHeading
import kotlinx.android.synthetic.main.fragment_movie_v1.rlNudity
import kotlinx.android.synthetic.main.fragment_movie_v1.rlShare
import kotlinx.android.synthetic.main.fragment_movie_v1.rvRecomendation
import kotlinx.android.synthetic.main.fragment_movie_v1.scrollView
import kotlinx.android.synthetic.main.fragment_movie_v1.shimmerLayout
import kotlinx.android.synthetic.main.fragment_movie_v1.tvDetailBtnTitle
import kotlinx.android.synthetic.main.fragment_movie_v1.tvDetailBtnTitleActionBar
import kotlinx.android.synthetic.main.fragment_movie_v1.tvDetailsContent
import kotlinx.android.synthetic.main.fragment_movie_v1.tvDetailsLanguage
import kotlinx.android.synthetic.main.fragment_movie_v1.tvDownload
import kotlinx.android.synthetic.main.fragment_movie_v1.tvRatingCritic
import kotlinx.android.synthetic.main.fragment_movie_v1.tvReadMore
import kotlinx.android.synthetic.main.fragment_movie_v1.tvSubTitle
import kotlinx.android.synthetic.main.fragment_movie_v1.tvTitle
import kotlinx.android.synthetic.main.fragment_movie_v1.tvNudity
import kotlinx.android.synthetic.main.fragment_movie_v2.*
import kotlinx.android.synthetic.main.fragment_search_bottom_sheet.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [MovieV1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MovieV1Fragment(val varient:Int) : BaseFragment(), OnParentItemClickListener, TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnDownloadVideoQueueItemChanged,
    BaseFragment.OnUserContentOrderStatus, OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack, BucketParentAdapter.OnMoreItemClick {
    var artImageUrl:String? = null
    var varientImageUrl:String? = null
    var selectedContentId:String? = null
    var playerType:String? = null
    private var chartDetailBgArtImageDrawable: LayerDrawable? = null

    var tabAdapter: TabAdapter? = null
    var movieViewModel: MovieViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    var rowList: MutableList<RowsItem?>? = null

    private var tracksViewModel: TracksContract.Presenter? = null
    var videoListViewModel: VideoViewModel? = null
    var centerGradient = 0
    var artworkProminentColor = 0
    var userViewModel: UserViewModel? = null
    var isAddWatchlist = false
    var bookmarkDataModel: BookmarkDataModel? = null
    var downloadState = Status.NONE.value
    private var downloadTracker: DownloadTracker? = null
    var contentOrderStatus = Constant.CONTENT_ORDER_STATUS_NA
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    var movieRight = ""
    var isDirectPlay = 0
    var isDirectPayment = 0
    var isTrailer = false
    var trailerId = ""
    var queryParam = ""
    var movieTrailerIndex = -1
    var tempMovieRespModel: PlaylistDynamicModel? = null
    companion object{
        var movieRespModel: PlaylistDynamicModel? = null

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun sum(p1:Double,p2:Double,gn:(Double,Double)->Double){
        var resullt=gn(p1,p2)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (varient == 2){
            //(activity as MainActivity).statusBarBg()
            return inflater.inflate(R.layout.fragment_movie_v1, container, false)
        }else{
            return inflater.inflate(R.layout.fragment_movie_v2, container, false)
        }
    }
    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            if (context != null){
                applyButtonTheme(requireContext(), llPlayMovie)
            applyButtonTheme(requireContext(), llPlayMovieActionBar)
            Constant.screen_name ="Movie Details"
            centerGradient = resources.getDimensionPixelSize(R.dimen.dimen_150)
//        artImageUrl = requireArguments().getString("image").toString()
            if (arguments != null){
                if(arguments?.containsKey("variant_image")!!){
                    varientImageUrl = requireArguments().getString("variant_image").toString()
                }

                if(arguments?.containsKey(isPlay)!!){
                    isDirectPlay = requireArguments().getInt(isPlay)
                }

                if(arguments?.containsKey(Constant.isPayment)!!){
                    isDirectPayment = requireArguments().getInt(Constant.isPayment)
                    queryParam = requireArguments().getString(Constant.EXTRA_QUERYPARAM)!!
                }

                if(arguments?.containsKey(Constant.EXTRA_IS_TRAILER)!!){
                    isTrailer = requireArguments().getBoolean(Constant.EXTRA_IS_TRAILER)
                }

                if(arguments?.containsKey(Constant.EXTRA_TRAILER_ID)!!){
                    trailerId = requireArguments().getString(Constant.EXTRA_TRAILER_ID).toString()
                }

                playerType = requireArguments().getString(defaultContentPlayerType).toString()
            }

            selectedContentId = requireArguments().getString("id").toString()
            downloadTracker = DemoUtil.getDownloadTracker(requireContext())
            val actionBar = (activity as AppCompatActivity).supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
            ivBack?.setOnClickListener {
                backPress()
            }
            llToolbar?.visibility = View.INVISIBLE

            scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@MovieV1Fragment)

            if (!TextUtils.isEmpty(selectedContentId.toString())){
                getContentOrderStatus(this@MovieV1Fragment, selectedContentId.toString())
            }
            shimmerLayout?.visibility = View.VISIBLE
            shimmerLayout?.startShimmer()

            setUpMovieTrailerListViewModel()

            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@MovieV1Fragment)
            threeDotMenu?.setOnClickListener(this@MovieV1Fragment)
            threeDotMenu2?.setOnClickListener(this@MovieV1Fragment)
            threeDotMenu?.hide()
            threeDotMenu2?.hide()
            llWatchlist?.setOnClickListener(this@MovieV1Fragment)
            rlShare?.setOnClickListener(this@MovieV1Fragment)


            CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }

    }
    private fun playAllMovies(isTrailer:Boolean = false, trailerId:String = ""){
        baseMainScope.launch {
            if (isAdded && context != null){
                if (context != null && context is MainActivity){
                    (context as MainActivity).closePIPVideoPlayer()
                    delay(1000)
                }

                setLog(TAG, "PLAY MOVIE = " )
            val songsList = CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
            val serviceBundle = Bundle()
            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
            if (isTrailer){
                serviceBundle.putString(Constant.SELECTED_CONTENT_ID,trailerId)
                serviceBundle.putInt(Constant.TYPE_ID,51)
            }else{
                serviceBundle.putString(Constant.SELECTED_CONTENT_ID,selectedContentId)
                serviceBundle.putInt(Constant.TYPE_ID,CONTENT_MOVIE)
            }
            serviceBundle.putInt(Constant.CONTENT_TYPE,CONTENT_MOVIE)

            intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            serviceBundle.putLong(VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(HungamaMusicApp.getInstance().getContentDuration(selectedContentId!!)!!))
            (requireActivity() as MainActivity).setLocalBroadcastEventCall(this@MovieV1Fragment, Constant.VIDEO_PLAYER_EVENT)
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
            /*startActivityForResult(
                intent,
                VIDEO_ACTIVITY_RESULT_CODE
            )*/
            }
        }
    }

    private fun playAllVideo(model: PlayableContentModel, trackPlayStartPosition:Long) {
        baseIOScope.launch {
            if (isAdded && context != null){
                val videoUrl = model.data?.head?.headData?.misc?.url
        val videoDrmLicense = model.data.head.headData.misc.downloadLink.drm.token
        val videoTitle = model.data.head.headData.title
        val videoSubTitle = model.data.head.headData.subtitle
        val videoArtwork = model.data.head.headData.image
        videoUrl?.let {
            videoTitle?.let { it1 ->
                playerType?.let { it2 ->
                    CommonUtils.setVideoTrackList(
                        requireContext(),
                        selectedContentId!!,
                        it,
                        it1,
                        it2,
                        videoSubTitle,
                        videoArtwork,
                        videoDrmLicense,
                        ContentTypes.MOVIES.value
                    )
                    tracksViewModel?.prepareTrackPlayback(BaseActivity.nowPlayingCurrentIndex()+1, trackPlayStartPosition)
                }
            }
        }
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
        when (item.itemId){
            R.id.action_download_video ->{

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

    fun URL.toBitmap(): Bitmap?{
        return try {
            BitmapFactory.decodeStream(openStream())
        }catch (e: IOException){
            null
        }
    }

    fun setArtImageBg(status: Boolean){
        baseIOScope.launch {
            try {
                if (isAdded&&context!=null&& artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl)&& movieDetailroot != null) {
                    val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
                    val gradient: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_gradient_drawable)
                    val result: Deferred<Bitmap?> = baseIOScope.async {
                        val urlImage = URL(artImageUrl)
                        urlImage.toBitmap()
                    }

                    baseIOScope.launch {
                        try {
                            // get the downloaded bitmap
                            val bitmap : Bitmap? = result.await()
                            val artImage = BitmapDrawable(resources, bitmap)
                            if (status){
                                if (bitmap != null){
                                    if(isAdded && bitmap!=null && context!=null){
                                        artworkProminentColor = CommonUtils.calculateAverageColor(bitmap, 1)
                                        baseMainScope.launch {
                                            if (context != null) {
                                                CommonUtils.setLog(
                                                    "MovieLifecycle",
                                                    "setArtImageBg--$artworkProminentColor"
                                                )
                                                changeStatusbarcolor(artworkProminentColor)
                                            }
                                        }
                                    }

                                    chartDetailBgArtImageDrawable = LayerDrawable(arrayOf<Drawable>(bgColor, artImage, gradient!!))
                                    movieDetailroot?.background = chartDetailBgArtImageDrawable

                                }

                            }

                        }catch (e:Exception){

                        }
                    }
                }
            }catch (e:Exception){

            }
        }
    }

    fun setProgressBar(it: Boolean) {
        if (it) {

            progress?.visibility = View.VISIBLE
        } else {
            progress?.visibility = View.GONE
        }
    }

    private fun setUpMovieTrailerListViewModel() {
        try {
            if (isAdded && context != null){
                movieViewModel = ViewModelProvider(
                this
            ).get(MovieViewModel::class.java)

            setProgressBar(true)
            if (ConnectionUtil(context).isOnline) {
                movieViewModel?.getMovieDetail(requireContext(), selectedContentId.toString())?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
//                            if(Constant.IS_SEARCH_RECOMMENDED_DISPLAY){
//                                getTrendCall(it?.data!!)
//                            }else{
                                fillMovieDetail(it?.data!!)
//                            }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBar(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setEmptyVisible(false)
                                setProgressBar(false)
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

    fun fillMovieDetail(data: PlaylistDynamicModel) {
        setProgressBar(false)
        scrollView.visibility=View.VISIBLE
        setMovieTrailerListData(data)
        setDetails(data)
    }

    fun getTrendCall(homedata: PlaylistDynamicModel?) {
        try {
            if (isAdded && context != null){
                val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url= WSConstants.METHOD_TRENDING_MOVIE+"?contentId="+selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(),url)?.observe(this,
            Observer {
                when(it.status){
                    com.hungama.music.data.webservice.utils.Status.SUCCESS->{

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

                    com.hungama.music.data.webservice.utils.Status.LOADING ->{
                        setProgressBarVisible(true)
                    }

                    com.hungama.music.data.webservice.utils.Status.ERROR ->{
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        fillMovieDetail(homedata!!)
                    }
                }
            })
            }
        }catch (e:Exception){

        }

    }

    fun getYouMayLikeCall(homedata: PlaylistDynamicModel?) {
        try {
            if (isAdded && context != null){
                val homeViewModel = ViewModelProvider(
            this
        ).get(HomeViewModel::class.java)
        val url= WSConstants.METHOD_YOU_MAY_LIKE_MOVIE+"?contentId="+selectedContentId
        homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
            Observer {
                when(it.status){
                    com.hungama.music.data.webservice.utils.Status.SUCCESS->{

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
                        fillMovieDetail(homedata!!)



                        setLog(TAG, "dailyDoseAPICall: after size:${homedata?.data?.body?.recomendation?.size}")
                    }

                    com.hungama.music.data.webservice.utils.Status.LOADING ->{
                        setProgressBarVisible(true)
                    }

                    com.hungama.music.data.webservice.utils.Status.ERROR ->{
                        setProgressBarVisible(false)
                        Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        fillMovieDetail(homedata!!)
                    }
                }
            })
            }
        }catch (e:Exception){

        }

    }

    fun setMovieTrailerListData(movieModel: PlaylistDynamicModel) {

        if (movieModel?.data?.body != null) {
            movieRespModel = movieModel
            tempMovieRespModel = movieRespModel
            setupUserViewModel()
            /*if (movieModel.data?.body?.rows?.size!!>0){
                if (movieModel.data?.body?.rows?.get(0) != null && movieModel.data?.body?.rows?.get(
                        0
                    )?.items?.size!! > 0
                ) {
                    llHeaderTitle.visibility = View.VISIBLE
                    ivMore.setOnClickListener {
                        redirectToMoreBucketListPage(movieModel.data?.body?.rows?.get(0)?.items,getString(R.string.movie_str_17))
                    }
                    rvTrailerMain.apply {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                        adapter = MovieTrailersExtra(context,
                            movieModel.data?.body?.rows?.get(0)?.items!!,
                            object : MovieTrailersExtra.OnChildItemClick {
                                override fun onUserClick(childPosition: Int) {
                                    val dpm = DownloadPlayCheckModel()
                                    dpm.contentId = movieModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.id?.toString()!!
                                    dpm.contentTitle = movieModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.title?.toString()!!
                                    dpm.planName = movieModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.misc?.movierights.toString()
                                    dpm.isAudio = false
                                    dpm.isDownloadAction = false
                                    dpm.isDirectPaymentAction = false
                                    dpm.queryParam = ""
                                    dpm.isShowSubscriptionPopup = true
                                    dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                                    dpm.restrictedDownload = RestrictedDownload.valueOf(movieModel.data?.body?.rows?.get(0)?.items?.get(childPosition)?.data?.misc?.restricted_download!!)
                                    if (CommonUtils.userCanDownloadContent(requireContext(), movieDetailroot, dpm, this@MovieV1Fragment)){
                                        val songsList =
                                            CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                                        val intent =
                                            Intent(requireContext(), VideoPlayerActivity::class.java)
                                        val serviceBundle = Bundle()
                                        serviceBundle.putParcelableArrayList(
                                            Constant.ITEM_KEY,
                                            songsList
                                        )
                                        serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                                        serviceBundle.putString(
                                            Constant.SELECTED_CONTENT_ID,
                                            movieModel.data?.body?.rows?.get(0)?.items?.get(
                                                childPosition
                                            )?.data?.id
                                        )
                                        serviceBundle.putInt(Constant.TYPE_ID,movieModel.data?.body?.rows?.get(0)?.items?.get(
                                            childPosition
                                        )?.data?.type!!.toInt())
                                        intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                                        intent.putExtra(
                                            "thumbnailImg",
                                            movieModel.data?.body?.rows?.get(0)?.items?.get(
                                                childPosition
                                            )?.data?.image
                                        )
                                        serviceBundle.putLong(VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(
                                            HungamaMusicApp.getInstance().getContentDuration(selectedContentId!!)!!)
                                        )
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                }
                            })
                        setRecycledViewPool(RecyclerView.RecycledViewPool())
                        setHasFixedSize(true)
                    }
                    llTrailers.visibility = View.VISIBLE
                } else {
                    llTrailers.visibility = View.GONE
                }


            }else{
                llTrailers.visibility = View.GONE
            }*/
        }
        baseMainScope.launch {
            if (isAdded && context != null){
                if (movieModel?.data?.body != null && !movieModel.data.body.recomendation.isNullOrEmpty()) {
            movieModel.data.body.recomendation?.forEachIndexed { index, rowsItem ->
                if (rowsItem != null && !rowsItem.keywords.isNullOrEmpty()) {
                    if (rowsItem.keywords?.get(0).equals("top-songs")) {
                        movieTrailerIndex = index
                    }
                }
            }
            rvRecomendation?.visibility = View.VISIBLE
            rvRecomendation?.visibility = View.VISIBLE

            var varient = Constant.ORIENTATION_HORIZONTAL

            val bucketParentAdapter = BucketParentAdapter(
                movieModel.data?.body?.recomendation!!,
                requireContext(),
                this@MovieV1Fragment,
                this@MovieV1Fragment,
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


            bucketParentAdapter.addData(movieModel.data.body.recomendation!!)
            shimmerLayout?.stopShimmer()
            shimmerLayout?.visibility = View.GONE
            rvRecomendation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisiable: Int = mLayoutManager.findFirstVisibleItemPosition()!!
                    val lastVisiable: Int =
                        mLayoutManager.findLastCompletelyVisibleItemPosition()

                    setLog(
                        TAG,
                        "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                    )
                    if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                        val fromBucket =
                            movieModel.data.body.recomendation?.get(firstVisiable)?.heading
                        val toBucket =
                            movieModel.data.body.recomendation?.get(lastVisiable)?.heading
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
            })

            rvRecomendation?.setPadding(0, 0, 0, 0)
        }
        shimmerLayout?.visibility = View.GONE
        shimmerLayout?.startShimmer()
        scrollView?.visibility = View.VISIBLE
        scrollView?.visibility = View.VISIBLE
            }
        }

    }

    private fun setDetails(movieModel: PlaylistDynamicModel) {
        val head = movieModel.data.head
        baseMainScope.launch {
            if (isAdded && context != null){
                if(varient==2) {
                    artImageUrl=varientImageUrl
                    if (varientImageUrl != null && !TextUtils.isEmpty(varientImageUrl)) {
                        ImageLoader.loadImage(
                            requireContext(),
                            movieAlbumArtImageView,
                            varientImageUrl!!,
                            R.drawable.bg_gradient_placeholder
                        )
                        setArtImageBg(true)
                    } else {
                        ImageLoader.loadImage(
                            requireContext(),
                            movieAlbumArtImageView,
                            "",
                            R.drawable.bg_gradient_placeholder
                        )
                        staticToolbarColor()
                    }
                }else{
                    artImageUrl=head.data.image
                    if (!TextUtils.isEmpty(artImageUrl)){
                        ImageLoader.loadImage(
                            requireContext(),
                            movieAlbumArtImageView,
                            artImageUrl!!,
                            R.drawable.bg_gradient_placeholder
                        )
                        setArtImageBg(true)
                    }else{
                        ImageLoader.loadImage(
                            requireContext(),
                            movieAlbumArtImageView,
                            "",
                            R.drawable.bg_gradient_placeholder
                        )
                        staticToolbarColor()
                    }
                }

                tvTitle?.text = head.data.title
                tvTitle?.isSelected = true
                llDetailsActions?.visibility = View.VISIBLE
                llDetails4?.visibility = View.VISIBLE
                /*if(!TextUtils.isEmpty(head.data.misc.nudity)){
                    if(head.data.misc.nudity.toInt() >0){
                        rlNudity?.visibility=View.VISIBLE
                    }else{
                        rlNudity?.visibility=View.GONE
                    }
                }else{
                    rlNudity?.visibility=View.GONE
                }*/

                var subTitleText=""
                if(!TextUtils.isEmpty(head.data.releasedate)){
                    val year=DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,head?.data?.releasedate)
                    subTitleText+=year
                }

                if(head.data.genre.size>0){
                    val genre=TextUtils.join("/",head.data.genre)
                    if (!TextUtils.isEmpty(subTitleText)){
                        subTitleText+=" • "+genre
                    }else{
                        subTitleText+=genre
                    }

                }

                if (!setContentActionButton(contentOrderStatus)) {
                    if (head.data.misc.movierights.size > 0) {
                        val movieRights = Utils.setMovieRightTextForDetail(
                            tvDetailBtnTitle,
                            head.data.misc.movierights,
                            requireContext(),
                            head.data.id.toString()
                        )
                        Utils.setMovieRightTextForDetail(
                            tvDetailBtnTitleActionBar,
                            head.data.misc.movierights,
                            requireContext(),
                            head.data.id.toString()
                        )
                        contentStatusProgress?.visibility = View.GONE
                        tvDetailBtnTitle?.visibility = View.VISIBLE
                        tvDetailBtnTitleActionBar?.visibility = View.VISIBLE
                        llDetails3?.visibility = View.VISIBLE
                        llPlayMovieActionBar?.visibility = View.VISIBLE
                        if (movieRights == 3) {
                            ivDetailBtnIcon?.visibility = View.VISIBLE
                            ivDetailBtnIconActionBar?.visibility = View.VISIBLE
                            ivDetailBtnIcon?.setImageDrawable(
                                requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite))
                            ivDetailBtnIconActionBar?.setImageDrawable(
                                requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite))
                        } else if (movieRights == 1) {
                            ivDetailBtnIcon?.visibility = View.VISIBLE
                            ivDetailBtnIconActionBar?.visibility = View.VISIBLE
                            ivDetailBtnIcon?.setImageDrawable(
                                requireContext().faDrawable(R.string.icon_rent, R.color.colorWhite, resources.getDimensionPixelSize(R.dimen.dimen_20).toFloat()))
                            ivDetailBtnIconActionBar?.setImageDrawable(
                                requireContext().faDrawable(R.string.icon_rent, R.color.colorWhite, resources.getDimensionPixelSize(R.dimen.dimen_20).toFloat()))
                        } else{
                            ivDetailBtnIcon?.visibility = View.VISIBLE
                            ivDetailBtnIconActionBar?.visibility = View.VISIBLE
                            ivDetailBtnIcon?.setImageDrawable(
                                requireContext().faDrawable(R.string.icon_crown, R.color.colorWhite))
                            ivDetailBtnIconActionBar?.setImageDrawable(
                                requireContext().faDrawable(R.string.icon_crown, R.color.colorWhite))
                        }
                    } else {
                        //tvDetailBtnTitle.visibility=View.GONE
                        llDetails3?.visibility = View.INVISIBLE
                        llPlayMovieActionBar?.visibility = View.INVISIBLE
                        centerGradient -= resources.getDimensionPixelSize(R.dimen.dimen_62)
                    }
                }

                if(!TextUtils.isEmpty(""+head.data.duration)){
                    val totalSecs=head.data.duration.toLong()
                    if(totalSecs.toLong()>=0){
                        val formatTime= DateUtils.convertTimeHM( TimeUnit.MINUTES.toMillis(totalSecs))

                        if (!TextUtils.isEmpty(subTitleText)){
                            subTitleText+=" • "+formatTime
                        }else{
                            subTitleText+=formatTime
                        }
                    }else{
                        //subTitleText+=""
                    }
                }else{
                    //subTitleText+=""
                }
                setLog(TAG, "subTitleText: $subTitleText")
                if(!TextUtils.isEmpty(subTitleText)){
                    tvSubTitle?.text=subTitleText
                    tvSubTitle?.visibility = View.VISIBLE
                }else{
                    //tvSubTitle.text=""
                    tvSubTitle?.visibility = View.GONE
                    centerGradient -= resources.getDimensionPixelSize(R.dimen.dimen_13)
                }


                if (varient == 2){
                    if(!TextUtils.isEmpty(""+head.data.misc.ratingCritic)){
                        tvRatingCritic?.text = ""+ head.data.misc.ratingCritic
                    }else{
                        tvRatingCritic?.text = ""
                    }
                    if (!head.data.misc.attributeCensorRating.isNullOrEmpty()){
                        tvNudity?.text = head.data.misc.attributeCensorRating.get(0)
                        rlNudity?.show()
                    }else{
                        rlNudity?.hide()
                    }
                }else{
                    var isRatingVisible = true
                    var isCensorRatingVisible = true
                    if(!TextUtils.isEmpty(""+head.data.misc.ratingCritic) && !head.data.misc.ratingCritic.toString().equals("0", true) && !head.data.misc.ratingCritic.toString().equals("0.0", true) && !head.data.misc.ratingCritic.toString().equals("NA", true)){
                        tvRatingCritic.text = ""+head.data.misc.ratingCritic
                        rlImdbRating?.show()
                        isRatingVisible = true
                    }else{
                        rlImdbRating?.hide()
                        isRatingVisible = false
                    }

                    if (!head.data.misc.attributeCensorRating.isNullOrEmpty()){
                        tvUA?.text = head.data.misc.attributeCensorRating.get(0)
                        ///tvNudity?.text = head.data.misc.attributeCensorRating.get(0)
                        rlNudity?.hide()
                        rlUA?.show()
                        isCensorRatingVisible = true
                    }else{
                        rlUA?.hide()
                        rlNudity?.hide()
                        isCensorRatingVisible = false
                    }

                    if (!isRatingVisible && !isCensorRatingVisible){
                        llDetails4?.hide()
                    }else{
                        llDetails4?.show()
                    }
                }

                if (!TextUtils.isEmpty(head.data.misc.description)){
                    tvReadMore?.text = head.data.misc.description
                    SaveState.isCollapse = true
                    tvReadMore?.setShowingLine(3)
                    tvReadMore?.addShowMoreText("read more")
                    tvReadMore?.addShowLessText("read less")

                    tvReadMore?.setShowMoreColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    tvReadMore?.setShowLessTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    tvReadMore?.setShowMoreStyle(Typeface.BOLD)
                    tvReadMore?.setShowLessStyle(Typeface.BOLD)
                    tvReadMore?.visibility = View.VISIBLE
                }else{
                    tvReadMore?.visibility = View.GONE
                    centerGradient -= resources.getDimensionPixelSize(R.dimen.dimen_37)
                }

                if (!head.data?.content_advisory?.isNullOrBlank()!!) {
                    tvDetailsContent.text = "Content Advisory: ${head.data?.content_advisory}"
                    tvDetailsContent?.show()
                } else {
                    tvDetailsContent?.hide()
                }

                if(head.data.misc.lang.size>0){
                    val lang=TextUtils.join(",",head.data.misc.lang)
                    tvDetailsLanguage?.text=getString(R.string.movie_str_8) + ": "+lang
                    tvDetailsLanguage?.visibility = View.VISIBLE
                }else{
                    val lang=TextUtils.join(",",head?.data?.misc?.lang!!)
                    //tvDetailsLanguage.text="Languages: NA"
                    tvDetailsLanguage?.visibility = View.GONE
                    centerGradient -= resources.getDimensionPixelSize(R.dimen.dimen_25)
                }
                /*val displayMetrics = DisplayMetrics()
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                val params: ViewGroup.LayoutParams = vCenterGradient.getLayoutParams()
                params.height = centerGradient
                params.width = displayMetrics.widthPixels
                vCenterGradient.requestLayout()*/
                if (!head.data.misc.movierights.isNullOrEmpty()){
                    movieRight = CommonUtils.getContentPlanName(head.data.misc.movierights.toString())
                    val planType = CommonUtils.getContentPlanType(head.data.misc.movierights.toString())
                    if(planType == PlanTypes.RENTAL.value){
                        if (movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                            val dpm = DownloadPlayCheckModel()
                            dpm.contentId = movieRespModel?.data?.head?.data?.id?.toString()!!
                            dpm.contentTitle = movieRespModel?.data?.head?.data?.title?.toString()!!
                            dpm.planName = movieRespModel?.data?.head?.data?.misc?.movierights.toString()
                            dpm.isAudio = false
                            dpm.isDownloadAction = false
                            dpm.isDirectPaymentAction = false
                            dpm.queryParam = ""
                            dpm.isShowSubscriptionPopup = false
                            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                            dpm.restrictedDownload =
                                RestrictedDownload.valueOf(movieRespModel?.data?.head?.data?.misc?.restricted_download!!)

                            if (!CommonUtils.userCanDownloadContent(requireContext(), movieDetailroot, dpm, this@MovieV1Fragment,Constant.drawer_svod_purchase)) {
                                callPlanDetailApi()
                            }
                        }
                    }
                }

                if (isDirectPlay == 1){
                    directPlay()
                }else if (isDirectPayment == 1){
                    directPayment()
                }else if (isTrailer){
                    if (TextUtils.isEmpty(trailerId) && !movieModel.data.body.recomendation.isNullOrEmpty()){
                        if (movieTrailerIndex >= 0 && !movieModel.data.body.recomendation?.get(movieTrailerIndex)?.keywords.isNullOrEmpty()
                            && movieModel.data.body.recomendation?.get(movieTrailerIndex)?.keywords?.get(0).equals("movie-trailer")) {
                            if (!movieModel.data.body.recomendation?.get(movieTrailerIndex)?.items.isNullOrEmpty()){
                                trailerId = movieModel.data.body.recomendation?.get(movieTrailerIndex)?.items?.get(0)?.data?.id.toString()
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(trailerId)){
                        playAllMovies(true, trailerId)
                    }
                }
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility =View.GONE
                scrollView?.visibility = View.VISIBLE
            }
        }
    }
    override fun onDestroy() {
        baseMainScope.launch {
            if (context != null){
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
        (requireActivity() as MainActivity).removeVideoDownloadListener()
        removeContentOrderStatusTimerCallback()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        super.onDestroy()
        tracksViewModel?.onCleanup()

    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
        intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
        //(activity as MainActivity).reBindService()
        if (activity != null){
            activity?.let {
                if (it != null) {
                    Util.startForegroundService(it, intent)
                    (it as MainActivity).reBindService()
                }
            }
        }
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun setVideoListData(model: PlayableContentModel) {
        if (model != null) {
            playAllVideo(model, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_ACTIVITY_RESULT_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                showMiniVideoPlayer(data)
            }
        }else{
            setLog("VIDEO_START_POSITION", requestCode.toString() + ", " + resultCode)
        }
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = 0
            if (varient == 1){
                maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_454)
            }else {
                maxDistance  = resources.getDimensionPixelSize(R.dimen.dimen_392)
            }
            /* how much we have scrolled */
            val movement = scrollView.scrollY
            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance){
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.INVISIBLE
                llToolbar.visibility = View.VISIBLE
                if (artworkProminentColor == 0){
                    rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                }else{
                    rlHeading.setBackgroundColor(artworkProminentColor)
                }

            }else{
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
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2){
            commonThreeDotMenuItemSetup(MOVIE_DETAIL_PAGE)
        }else if (id == llWatchlist.id){
            if (movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                var attributeCensorRating = ""
                if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                    attributeCensorRating = movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
                }
                if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating)){
                    movieRespModel?.let { setAddOrRemoveFavourite(it) }
                }
            }
        }else if(v == llPlayMovie || v == llPlayMovieActionBar){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), v,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            directPlay()
        }else if(v==rlShare){
            if (movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                var attributeCensorRating = ""
                if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                    attributeCensorRating = movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
                }
                if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating)){
                    val shareurl=getString(R.string.music_player_str_18)+" "+ movieRespModel?.data?.head?.data?.misc?.share
                    Utils.shareItem(requireActivity(),shareurl)
                }
            }
        }
    }

    private fun setupUserViewModel() {
        if (isAdded && context != null){
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)

            getUserBookmarkedData()
        }
    }

    private fun setAddOrRemoveFavourite(movieRespModel: PlaylistDynamicModel) {
        if (ConnectionUtil(context).isOnline) {
            isAddWatchlist = !isAddWatchlist
            val jsonObject = JSONObject()
            jsonObject.put("contentId", movieRespModel?.data?.head?.data?.id!!)
            jsonObject.put("typeId", movieRespModel?.data?.head?.data?.type!!)
            jsonObject.put("action",isAddWatchlist)
            jsonObject.put("module", MODULE_WATCHLIST)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()

            if(isAddWatchlist){
                if (movieRespModel?.data?.head?.data?.type.toString().equals(VIDEO_SHORT_FILMS, true)){
                    val messageModel = MessageModel(getString(R.string.toast_str_10),
                        MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }else{
                    val messageModel = MessageModel(getString(R.string.toast_str_9),
                    MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
                baseIOScope.launch {
                    val hashMap = HashMap<String,String>()
                    var newContentId= movieRespModel?.data?.head?.data?.id!!
                    var contentIdData=newContentId.replace("playlist-","")
                    hashMap.put(EventConstant.CONTENTID_EPROPERTY, contentIdData)
                    hashMap.put(
                        EventConstant.CONTENTNAME_EPROPERTY,
                        movieRespModel?.data?.head?.data?.title!!)
                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+ movieRespModel?.data?.head?.data?.type!!))
                    hashMap.put(EventConstant.GENRE_EPROPERTY,Utils.arrayToString(movieRespModel?.data?.head?.data?.genre!!))
                    hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY,"")
                    hashMap.put(EventConstant.LANGUAGE_EPROPERTY,Utils.arrayToString(movieRespModel?.data?.head?.data?.misc?.lang!!))
                    hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY,"")

                    var eventModel: EventModel?= HungamaMusicApp?.getInstance()?.getEventData(movieRespModel?.data?.head?.data?.id!!)
                    if(eventModel!=null&&!TextUtils.isEmpty(eventModel?.bucketName)){
                        hashMap.put(EventConstant.SOURCE_EPROPERTY,""+eventModel?.bucketName)
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,""+eventModel?.bucketName)
                    }

                    hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,movieRespModel?.data?.head?.data?.releasedate))
                    EventManager.getInstance().sendEvent(AddedToWatchlist(hashMap))
                }

            }else{
                if (movieRespModel?.data?.head?.data?.type.toString().equals(VIDEO_SHORT_FILMS, true)){
                    val messageModel = MessageModel(getString(R.string.toast_str_13),
                        MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }else{
                    val messageModel = MessageModel(getString(R.string.toast_str_12),
                        MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
                baseIOScope.launch {
                 val hashMap = HashMap<String,String>()
                    var newContentId= movieRespModel?.data?.head?.data?.id!!
                    var contentIdData=newContentId.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY,contentIdData )
                hashMap.put(
                    EventConstant.CONTENTNAME_EPROPERTY,
                    movieRespModel?.data?.head?.data?.title!!)
                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+ movieRespModel?.data?.head?.data?.type!!))
                hashMap.put(EventConstant.GENRE_EPROPERTY,Utils.arrayToString(movieRespModel?.data?.head?.data?.genre!!))
                hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY,"")
                hashMap.put(EventConstant.LANGUAGE_EPROPERTY,Utils.arrayToString(movieRespModel?.data?.head?.data?.misc?.lang!!))
                hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY,"")
                hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,movieRespModel?.data?.head?.data?.releasedate))
                val eventModel: EventModel?= HungamaMusicApp?.getInstance()?.getEventData(movieRespModel?.data?.head?.data?.id!!)
                if(eventModel!=null&&!TextUtils.isEmpty(eventModel?.bucketName)){
                    hashMap.put(EventConstant.SOURCE_EPROPERTY,""+eventModel?.bucketName)
                    hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY,""+eventModel?.bucketName)
                }
                EventManager.getInstance().sendEvent(RemovedFromWatchListEvent(hashMap))
                }
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun getUserBookmarkedData() {
        try {
            if (isAdded && context != null){
                if (ConnectionUtil(requireContext()).isOnline) {
                    userViewModel?.getUserBookmarkedData(requireContext(), MODULE_WATCHLIST)?.observe(this,
                        Observer {
                            when(it.status){
                                com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                    setProgressBar(false)
                                    if (it?.data != null) {
                                        fillUI(it?.data)
                                    }

                                }

                                com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                    setProgressBar(true)
                                }

                                com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                    setEmptyVisible(false)
                                    setProgressBar(false)
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


    private fun fillUI(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        baseIOScope.launch {
            if (isAdded && context != null){
                if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0){
                    for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!){
                        if (movieRespModel?.data?.head?.data?.id?.equals(bookmark?.data?.id)!!){
                            isAddWatchlist = true
                        }
                    }
                    setFollowingStatus()
                }
            }
        }

    }

    fun setFollowingStatus(){
        baseMainScope.launch {
            if (isAdded && context != null){
                if (isAddWatchlist){
                    ivWatchlist.setImageDrawable(
                        requireContext().faDrawable(R.string.icon_tick, R.color.colorWhite))
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_tick)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivWatchlist.setImageDrawable(drawable)*/
                }else{
                    ivWatchlist.setImageDrawable(
                        requireContext().faDrawable(R.string.icon_watchlist, R.color.colorWhite))
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_watchlist)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivWatchlist.setImageDrawable(drawable)*/
                }
            }
        }


    }

    private fun downloadIconStates(status: Int, ivAudioDownload: ImageView){
        baseMainScope.launch {
            if (isAdded && context != null){
                when (status){
                    Status.NONE.value -> {
                        /*val drawable = FontDrawable(requireContext(), R.string.icon_download)
                        drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                        ivAudioDownload.setImageDrawable(drawable)*/
                        ivAudioDownload.setImageDrawable(
                            requireContext().faDrawable(R.string.icon_download, R.color.colorWhite))
                    }
                    Status.QUEUED.value -> {
                        /* val drawable = FontDrawable(requireContext(), R.string.icon_download_queue)
                         drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                         ivAudioDownload.setImageDrawable(drawable)*/
                        ivAudioDownload.setImageDrawable(
                            requireContext().faDrawable(R.string.icon_download_queue, R.color.colorWhite))
                    }
                    Status.DOWNLOADING.value ->{
                        /*val drawable = FontDrawable(requireContext(), R.string.icon_downloading)
                        drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                        ivAudioDownload.setImageDrawable(drawable)*/
                        ivAudioDownload.setImageDrawable(
                            requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite))
                    }
                    Status.COMPLETED.value ->{
                        /*val drawable = FontDrawable(requireContext(), R.string.icon_downloaded2)
                        drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                        ivAudioDownload.setImageDrawable(drawable)*/
                        ivAudioDownload.setImageDrawable(
                            requireContext().faDrawable(R.string.icon_downloaded2, R.color.colorWhite))
                    }
                    Status.PAUSED.value ->{
                        ivAudioDownload.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause_round))
                    }

                }
            }
        }
    }

    private fun initDownloadMusic() {
        baseMainScope.launch {
            if (ivDownload != null) {
                ivDownload?.requestFocus()
                if (!TextUtils.isEmpty(selectedContentId)){
                    val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(
                        selectedContentId.toString())
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(
                        selectedContentId.toString())
                    if (downloadedAudio != null){
                        val isDownloaded = downloadTracker!!.isDownloaded(downloadedAudio.downloadUrl!!)
                        if (!isDownloaded){
                            AppDatabase.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(
                                selectedContentId.toString())
                        }else{
                            downloadState = Status.COMPLETED.value
                        }
                    } else if (downloadQueue != null){
                        if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.QUEUED.value){
                            downloadState = Status.QUEUED.value
                        }else if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.DOWNLOADING.value){
                            downloadState = Status.DOWNLOADING.value
                        }else if (downloadQueue?.contentId?.equals(selectedContentId.toString())!! && downloadQueue.downloadStatus == Status.PAUSED.value){
                            downloadState = Status.PAUSED.value
                        }
                    }
                }else{
                    return@launch
                }

                //isDownloaded = downloadTracker!!.isDownloaded(currentPlayingMediaItem)

                if(downloadState == Status.COMPLETED.value){
                    /*ivDownload?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_download_completed
                        )
                    )*/
                    downloadIconStates(Status.COMPLETED.value, ivDownload)
                    tvDownload?.text = getString(R.string.video_player_str_2)

                }else if(downloadState == Status.QUEUED.value){
                    downloadIconStates(Status.QUEUED.value, ivDownload)
                    tvDownload?.text = getString(R.string.download_str_11)

                }else if(downloadState == Status.DOWNLOADING.value){
                    downloadIconStates(Status.DOWNLOADING.value, ivDownload)
                    tvDownload?.text = getString(R.string.download_str_2)

                }else if(downloadState == Status.PAUSED.value){
                    downloadIconStates(Status.PAUSED.value, ivDownload)
                    tvDownload?.text = getString(R.string.general_setting_str_25)

                }else{
                    /*ivDownload?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_download
                        )
                    )*/
                    rlDownload?.setOnClickListener {
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = movieRespModel?.data?.head?.data?.id?.toString()!!
                        dpm.contentTitle = movieRespModel?.data?.head?.data?.title?.toString()!!
                        dpm.planName = movieRespModel?.data?.head?.data?.misc?.movierights.toString()
                        dpm.isAudio = false
                        dpm.isDownloadAction = true
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload = RestrictedDownload.valueOf(movieRespModel?.data?.head?.data?.misc?.restricted_download!!)
                        if (movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                            var attributeCensorRating = ""
                            if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                                attributeCensorRating = movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
                            }
                            if (CommonUtils.userCanDownloadContent(
                                    requireContext(),
                                    movieDetailroot,
                                    dpm,
                                    this@MovieV1Fragment,Constant.drawer_svod_download
                                )
                            ) {
                                if (!CommonUtils.checkUserCensorRating(
                                        requireContext(),
                                        attributeCensorRating
                                    )
                                ) {
                                    setLog("onDwClick", "Clicked")
                                    // self download
                                    /*startDRMDownloadSong()
                                    downloadIconStates(Download.STATE_QUEUED, ivDownload)
                                    tvDownload?.text = getString(R.string.in_queue)*/
                                    val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                                    var dq = DownloadQueue()
                                    //for (item in playlistSongList?.iterator()!!){

                                    dq = DownloadQueue()
                                    if (movieRespModel != null) {
                                        if (!TextUtils.isEmpty(movieRespModel?.data?.head?.data?.id!!)) {
                                            dq.parentId = movieRespModel?.data?.head?.data?.id!!
                                            dq.contentId = movieRespModel?.data?.head?.data?.id!!
                                        }
                                        if (!TextUtils.isEmpty(movieRespModel?.data?.head?.data?.title!!)) {
                                            dq.pName = movieRespModel?.data?.head?.data?.title
                                            dq.title = movieRespModel?.data?.head?.data?.title
                                        }

                                        if (!TextUtils.isEmpty(movieRespModel?.data?.head?.data?.subtitle!!)) {
                                            dq.pSubName = movieRespModel?.data?.head?.data?.subtitle
                                            dq.subTitle = movieRespModel?.data?.head?.data?.subtitle
                                        }

                                        if (!TextUtils.isEmpty(movieRespModel?.data?.head?.data?.releasedate!!)) {
                                            dq.pReleaseDate =
                                                movieRespModel?.data?.head?.data?.releasedate
                                        }

                                        if (!TextUtils.isEmpty(movieRespModel?.data?.head?.data?.image!!)) {
                                            dq.pImage = movieRespModel?.data?.head?.data?.image
                                            dq.image = movieRespModel?.data?.head?.data?.image
                                        }


                                        if (!TextUtils.isEmpty(movieRespModel?.data?.head?.data?.misc?.movierights.toString()!!)) {
                                            dq.planName =
                                                movieRespModel?.data?.head?.data?.misc?.movierights.toString()
                                            dq.planType = CommonUtils.getContentPlanType(dq.planName)
                                        }
                                        dq.pType = DetailPages.MOVIE_DETAIL_PAGE.value
                                        if (playerType != null && playerType.equals(
                                                VIDEO_SHORT_FILMS,
                                                true
                                            )
                                        ) {
                                            dq.contentType = ContentTypes.SHORT_FILMS.value
                                        } else {
                                            dq.contentType = ContentTypes.MOVIES.value
                                        }

                                        val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                                        dq.source = eventModel.sourceName

                                        val downloadQueue =
                                            AppDatabase.getInstance()?.downloadQueue()?.findByContentId(
                                                movieRespModel?.data?.head?.data?.id!!.toString()
                                            )
                                        val downloadedAudio =
                                            AppDatabase.getInstance()?.downloadedAudio()
                                                ?.findByContentId(
                                                    movieRespModel?.data?.head?.data?.id!!.toString()
                                                )
                                        if ((!downloadQueue?.contentId.equals(movieRespModel?.data?.head?.data?.id!!.toString()))
                                            && (!downloadedAudio?.contentId.equals(movieRespModel?.data?.head?.data?.id!!.toString()))
                                        ) {
                                            downloadQueueList.add(dq)
                                        }

                                        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                                            downloadQueueList,
                                            this@MovieV1Fragment,
                                            false,
                                            true
                                        )

                                        downloadIconStates(Status.DOWNLOADING.value, ivDownload)
                                        tvDownload?.text = getString(R.string.download_str_2)
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

    }

    override fun onDownloadVideoQueueItemChanged(
        downloadManager: DownloadManager,
        download: Download
    ) {
        try {
            baseMainScope.launch {
                if (isAdded && context != null&&movieRespModel!=null){
                    val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByPlayableUrl(download.request.uri.toString())
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByPlayableUrl(download.request.uri.toString())
                    if (downloadQueue != null && movieRespModel?.data?.head?.data?.id?.toString()?.equals(downloadQueue.contentId)!!){

                    }else if (downloadedAudio != null && movieRespModel?.data?.head?.data?.id?.toString()?.equals(downloadedAudio.contentId)!!){

                    }else{
                        return@launch
                    }
                    when(download.state) {
                        Download.STATE_DOWNLOADING -> {
                            setLog("VideoDownloadLog:1", download.state.toString())
                            //Toast.makeText(requireContext(), "Download started.", Toast.LENGTH_LONG).show()
                            downloadIconStates(Status.DOWNLOADING.value, ivDownload)
                            tvDownload?.text = getString(R.string.download_str_2)
                        }
                        Download.STATE_QUEUED->{
                            setLog("VideoDownloadLog:2", download.state.toString())
                            //Toast.makeText(requireContext(), "Download queue.", Toast.LENGTH_LONG).show()
                            downloadIconStates(Status.QUEUED.value, ivDownload)
                            tvDownload?.text = getString(R.string.download_str_11)
                        }
                        Download.STATE_STOPPED -> {
                            setLog("VideoDownloadLog:3", download.state.toString())
                            //Toast.makeText(requireContext(), "Download stopped.", Toast.LENGTH_LONG).show()
                            if(download.stopReason == stopReasonPause){
                                downloadIconStates(Status.PAUSED.value, ivDownload)
                                tvDownload?.text = getString(R.string.general_setting_str_25)
                            }
                        }
                        Download.STATE_COMPLETED -> {
                            setLog("VideoDownloadLog:4", download.state.toString())
                            //Toast.makeText(requireContext(), "Download completed.", Toast.LENGTH_LONG).show()
                            rlDownload?.setOnClickListener(null)
                            downloadIconStates(Status.COMPLETED.value, ivDownload)
                            tvDownload?.text = getString(R.string.video_player_str_2)
                        }
                        Download.STATE_REMOVING -> {
                            setLog("VideoDownloadLog:5", download.state.toString())
                            //Toast.makeText(requireContext(), "Download removed.", Toast.LENGTH_LONG).show()
                            downloadIconStates(Status.NONE.value, ivDownload)
                            tvDownload?.text = getString(R.string.movie_str_3)
                        }
                        Download.STATE_FAILED ->{
                            setLog("VideoDownloadLog:6", download.state.toString())
                            //Toast.makeText(requireContext(), "Download failed.", Toast.LENGTH_LONG).show()
                            downloadIconStates(Status.NONE.value, ivDownload)
                            tvDownload?.text = getString(R.string.movie_str_3)
                        }
                        Download.STATE_RESTARTING -> {
                            setLog("VideoDownloadLog:7", download.state.toString())
                        }
                    }
                }
            }
        }catch (e:Exception){

        }

    }

    override fun onDownloadProgress(downloads: List<Download?>?, progress: Int, currentExoDownloadPosition:Int) {

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        if (downloadsPaused!!){
            //setLog("VideoDownloadLog:9", downloadsPaused.toString())
        }else{
            //setLog("VideoDownloadLog:10", downloadsPaused.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        initDownloadMusic()
        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
            ArrayList(),
            this,
            true,
            false
        )
    }

    override fun onUserContentOrderStatusCheck(status: Int) {
        contentOrderStatus = status
        setContentActionButton(contentOrderStatus)
    }

    private fun setContentActionButton(status: Int):Boolean{
        if (isAdded && context != null){
            setLog("contentOrder-4", status.toString())
            llPlayMovie?.setOnClickListener(null)
            llPlayMovieActionBar?.setOnClickListener(null)
            if (status == Constant.CONTENT_ORDER_STATUS_NA || status == Constant.CONTENT_ORDER_STATUS_FAIL){
                contentStatusProgress?.visibility = View.GONE
                tvDetailBtnTitle.visibility = View.VISIBLE
                tvDetailBtnTitleActionBar.visibility = View.VISIBLE
                ivDetailBtnIcon.visibility = View.VISIBLE
                ivDetailBtnIconActionBar.visibility = View.VISIBLE
                llPlayMovie?.setOnClickListener(this)
                llPlayMovieActionBar?.setOnClickListener(this)
                return false
            }else if (status == Constant.CONTENT_ORDER_STATUS_IN_PROCESS || status == Constant.CONTENT_ORDER_STATUS_PENDING){
                contentStatusProgress?.visibility = View.VISIBLE
                tvDetailBtnTitle.visibility = View.GONE
                tvDetailBtnTitleActionBar.visibility = View.GONE
                ivDetailBtnIcon.visibility = View.GONE
                ivDetailBtnIconActionBar.visibility = View.GONE
                llDetails3.visibility = View.VISIBLE
                llPlayMovieActionBar.visibility = View.VISIBLE
                runContentOrderStatusHandler(this, selectedContentId!!)
                return true
            }else if (status == Constant.CONTENT_ORDER_STATUS_SUCCESS){
                if (isContentAvailableForPlay()){
                    setSuccessButtonDesign()
                }else{
                    getUserSubscriptionStatus()
                }
                return true
            }
        }

        return false
    }

    private fun setSuccessButtonDesign() {
        baseMainScope.launch {
            contentStatusProgress?.visibility = View.GONE
            tvDetailBtnTitle.visibility = View.VISIBLE
            tvDetailBtnTitleActionBar.visibility = View.VISIBLE
            tvDetailBtnTitle.text = getString(R.string.movie_str_7)
            tvDetailBtnTitleActionBar.text = getString(R.string.movie_str_7)
            ivDetailBtnIcon.visibility = View.VISIBLE
            ivDetailBtnIconActionBar.visibility = View.VISIBLE
            ivDetailBtnIcon.setImageDrawable(
                requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite)
            )
            ivDetailBtnIconActionBar.setImageDrawable(
                requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite)
            )
            llDetails3.visibility = View.VISIBLE
            llPlayMovieActionBar.visibility = View.VISIBLE
            llPlayMovie?.setOnClickListener(this@MovieV1Fragment)
            llPlayMovieActionBar?.setOnClickListener(this@MovieV1Fragment)
        }
    }

    private fun isContentAvailableForPlay(): Boolean {
        if (isAdded && context != null && movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = movieRespModel?.data?.head?.data?.id!!
            dpm.contentTitle = movieRespModel?.data?.head?.data?.title!!
            dpm.planName = movieRespModel?.data?.head?.data?.misc?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = false
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload =
                RestrictedDownload.valueOf(movieRespModel?.data?.head?.data?.misc?.restricted_download!!)
            var attributeCensorRating = ""
            if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()) {
                attributeCensorRating =
                    movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
            }

            return CommonUtils.userCanDownloadContent(
                requireContext(),
                movieDetailroot,
                dpm,
                this@MovieV1Fragment,Constant.drawer_svod_download
            )
        }
        return false
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        if(isAdded){
            setLog("contentOrder-1", status.toString()+"--"+contentId)
            getContentOrderStatus(this, contentId)
            if (status == Constant.CONTENT_ORDER_STATUS_NA){

            }else if(status == Constant.CONTENT_ORDER_STATUS_FAIL){

            }else if (status == Constant.CONTENT_ORDER_STATUS_IN_PROCESS || status == Constant.CONTENT_ORDER_STATUS_PENDING){

            }else if (status == Constant.CONTENT_ORDER_STATUS_SUCCESS){

            }
        }


    }

    private fun showMiniVideoPlayer(intent: Intent) {
        if (intent.hasExtra(VIDEO_START_POSITION)) {
            val video_start_position = intent.getLongExtra(VIDEO_START_POSITION, 0)
            val videoListModel:ArrayList<PlayableContentModel> = intent.getParcelableArrayListExtra(VIDEO_LIST_DATA)!!
            playAllVideo(videoListModel.get(0), video_start_position)
            setLog("VIDEO_START_POSITION-1", video_start_position.toString())
        }
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.VIDEO_ACTIVITY_RESULT_CODE) {
                showMiniVideoPlayer(intent)
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    private fun callPlanDetailApi(){
        try {
            if (isAdded && context != null){
                userSubscriptionViewModel = ViewModelProvider(
                this
            ).get(UserSubscriptionViewModel::class.java)


            if (ConnectionUtil(requireContext()).isOnline) {
                userSubscriptionViewModel?.getPlanDetail(
                    requireContext()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                if (it?.data != null){
                                    val detailModel = Gson().fromJson<ContentsPlanDetailModel>(
                                        it?.data,
                                        ContentsPlanDetailModel::class.java
                                    ) as ContentsPlanDetailModel
                                    if (detailModel?.success!! && detailModel?.data != null){
                                        if (it?.data != null) {
                                            val json = JSONObject(it?.data)
                                            val jsonData = json.getJSONObject("data")
                                            var plan = JSONArray()

                                            setLog("getPlanDetail","getPlanDetail movieRight:${movieRight} jsonData:${jsonData}")
                                            if (jsonData.has(movieRight.lowercase())){
                                                plan = jsonData.getJSONArray(movieRight.lowercase())

                                            }else if (jsonData.has(movieRight)){
                                                plan = jsonData.getJSONArray(movieRight)
                                            } else if (movieRight.equals("TVOD-Premium") && jsonData.has("ptvod")) {
                                                plan = jsonData.getJSONArray("ptvod")
                                            } else if (jsonData.has("tvod")) {
                                                plan = jsonData.getJSONArray("tvod")
                                            }
                                            if (!plan.isNull(0) && plan?.get(0) != null && !TextUtils.isEmpty(
                                                    plan.get(0).toString()
                                                )
                                            ) {
                                                try {
                                                    val planModel =
                                                        Gson().fromJson<ContentsPlanDetailModel.Data.Plan>(
                                                            plan.get(0).toString(),
                                                            ContentsPlanDetailModel.Data.Plan::class.java
                                                        ) as ContentsPlanDetailModel.Data.Plan

                                                    if (planModel != null && planModel.planPrice.toDouble() >= 0) {
                                                        val df = DecimalFormat("###.##")
                                                        val price = df.format(planModel.planPrice.toDouble())
                                                        if(Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
                                                            tvDetailBtnTitle.text = getString(R.string.movie_str_5) + " @ " + getString(R.string._rupee) + price
                                                            tvDetailBtnTitleActionBar.text = getString(R.string.movie_str_5) + " @ " + getString(R.string._rupee) + price
                                                        }else{
                                                            tvDetailBtnTitle.text = getString(R.string.movie_str_5) + " @ " + getString(R.string._doller) + price
                                                            tvDetailBtnTitleActionBar.text = getString(R.string.movie_str_5) + " @ " + getString(R.string._doller) + price
                                                        }
                                                    }
                                                }catch (e:Exception){
e.printStackTrace()
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{

                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{

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

    private fun directPlay(){
        baseMainScope.launch {
            CommonUtils.setLog("directPlay","directPlay 1")
            if (isAdded && context != null && movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                val dpm = DownloadPlayCheckModel()
                dpm.contentId = movieRespModel?.data?.head?.data?.id!!
                dpm.contentTitle = movieRespModel?.data?.head?.data?.title!!
                dpm.planName = movieRespModel?.data?.head?.data?.misc?.movierights.toString()
                dpm.isAudio = false
                dpm.isDownloadAction = false
                dpm.isDirectPaymentAction = false
                dpm.queryParam = ""
                dpm.isShowSubscriptionPopup = true
                dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                dpm.restrictedDownload =
                    RestrictedDownload.valueOf(movieRespModel?.data?.head?.data?.misc?.restricted_download!!)
                var attributeCensorRating = ""
                if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                    attributeCensorRating = movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
                }

                CommonUtils.setLog("directPlay","directPlay 2")

                if (CommonUtils.userCanDownloadContent(requireContext(), movieDetailroot, dpm, this@MovieV1Fragment,Constant.drawer_svod_purchase)) {
                    if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating, object : UserCensorRatingPopup.OnUserCensorRatingChange {
                            override fun onCensorRatingChange(rating: Int) {
                                if(rating!=0){
                                    playAllMovies()
                                    CommonUtils.setLog("directPlay","directPlay 3")
                                }
                            }

                        },true)) {
                        playAllMovies()
                    }else{

                        CommonUtils.setLog("directPlay","directPlay 3")
                    }
                }
            }
        }

    }

    private fun directPayment(){
        baseIOScope.launch {
            if (isAdded && context != null && movieRespModel != null && movieRespModel?.data != null && movieRespModel?.data?.head != null && movieRespModel?.data?.head?.data != null) {
                val dpm = DownloadPlayCheckModel()
                dpm.contentId = movieRespModel?.data?.head?.data?.id!!
                dpm.contentTitle = movieRespModel?.data?.head?.data?.title!!
                dpm.planName = movieRespModel?.data?.head?.data?.misc?.movierights.toString()
                dpm.isAudio = false
                dpm.isDownloadAction = false
                dpm.isDirectPaymentAction = true
                dpm.queryParam = queryParam
                dpm.isShowSubscriptionPopup = true
                dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                dpm.restrictedDownload =
                    RestrictedDownload.valueOf(movieRespModel?.data?.head?.data?.misc?.restricted_download!!)
                var attributeCensorRating = ""
                if (!movieRespModel?.data?.head?.data?.misc?.attributeCensorRating.isNullOrEmpty()){
                    attributeCensorRating = movieRespModel?.data?.head?.data?.misc?.attributeCensorRating?.get(0).toString()
                }

                if (CommonUtils.userCanDownloadContent(requireContext(), movieDetailroot, dpm, this@MovieV1Fragment,Constant.drawer_svod_download)) {
                    if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating)) {
                        playAllMovies()
                    }
                }

            }
        }
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            movieRespModel = tempMovieRespModel
            baseMainScope.launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "MovieLifecycle",
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

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        baseMainScope.launch {
            if (isAdded && context != null){
                if (selectedMoreBucket != null
                    && !selectedMoreBucket.items.isNullOrEmpty()
                    && selectedMoreBucket.items?.size!! > 0
                    && selectedMoreBucket.items?.get(0)?.data != null
                    && !selectedMoreBucket?.items?.get(0)?.itype.toString().equals("25", true)){
                    val bundle = Bundle()
                    bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
                    setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
                    setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
                    val moreBucketListFragment = MoreBucketListFragment()
                    moreBucketListFragment.arguments = bundle
                    addFragment(R.id.fl_container, this@MovieV1Fragment, moreBucketListFragment, false)

                    val dataMap= HashMap<String,String>()
                    dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
                    dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ movieRespModel?.data?.head?.data?.title)

                    dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
                    EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
                }
            }
        }
    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        /*if (parent != null
            && !parent.items.isNullOrEmpty()
            && parent.items?.size!! > childPosition
            && parent.items?.get(childPosition)?.data != null
            && (parent.items?.get(childPosition)?.data?.type.equals("51", true)
                    || (!parent.keywords.isNullOrEmpty() && parent.keywords?.get(0).equals("movie-trailer")))){

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
            if (CommonUtils.userCanDownloadContent(requireContext(), movieDetailroot, dpm, this@MovieV1Fragment)) {
                val intent =
                    Intent(requireContext(), VideoPlayerActivity::class.java)
                val serviceBundle = Bundle()
                serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                serviceBundle.putString(
                    Constant.SELECTED_CONTENT_ID,
                    parent.items?.get(childPosition)?.data?.id
                )
                parent.items?.get(childPosition)?.data?.type?.toInt()
                    ?.let { serviceBundle.putInt(Constant.TYPE_ID, it) }
                intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                intent.putExtra(
                    "thumbnailImg",
                    parent.items?.get(childPosition)?.data?.image
                )
                serviceBundle.putLong(
                    VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(
                        HungamaMusicApp.getInstance().getContentDuration(selectedContentId!!)!!
                    )
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
        }else{*/
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading)
        //}
    }


    fun getUserSubscriptionStatus() {
        userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)

        if (ConnectionUtil(requireContext()).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(requireContext())?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            if (isContentAvailableForPlay()){
                                BaseActivity.setIsGoldUser()
                                setSuccessButtonDesign()
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {

                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {

                        }
                    }
                })
        }
    }
}