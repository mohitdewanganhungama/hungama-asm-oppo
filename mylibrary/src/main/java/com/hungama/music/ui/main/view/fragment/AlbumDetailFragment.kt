package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
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
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.data.model.*
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.OnUserSubscriptionUpdate


import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.FavouritedEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.ui.main.adapter.DetailAlbumAdapter
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.calculateAverageColor
import com.hungama.music.utils.Constant.ALBUM_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.ALBUM_DETAIL_PAGE
import com.hungama.music.utils.Constant.MODULE_FAVORITE
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import com.hungama.music.ui.main.viewmodel.AlbumViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_album_detail.*
import kotlinx.android.synthetic.main.fragment_album_detail.iv_banner
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.HashMap

class AlbumDetailFragment : BaseFragment(), TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseFragment.OnMenuItemClicked,
    BaseActivity.OnDownloadQueueItemChanged, OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack, OnParentItemClickListener,
    BucketParentAdapter.OnMoreItemClick {

    var artImageUrl:String? = null
    var selectedContentId:String? = null
    var playerType:String? = null
    private var albumDetailBgArtImageDrawable: LayerDrawable? = null

    var albumListViewModel: AlbumViewModel? = null

    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()

    var albumSongList:ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    var ascending = true
    var albumAdpter: DetailAlbumAdapter? = null
    var artworkProminentColor = 0
    var artworkHeight = 0
    var userViewModel: UserViewModel? = null
    var isFavourite = false
    var bookmarkDataModel: BookmarkDataModel? = null
    var albumModel: PlaylistDynamicModel?=null
    var isPlaying = false
    var allDownload = false
    var isFromVerticalPlayer = false
    var ft: AdsConfigModel.Ft? = AdsConfigModel.Ft()
    var nonft: AdsConfigModel.Nonft? = AdsConfigModel.Nonft()

    companion object{
        var albumRespModel: PlaylistDynamicModel? = null
        var albumSongItem: PlaylistModel.Data.Body.Row? = null
        var playableItemPosition = 0
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
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album_detail, container, false)
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            applyButtonTheme(requireContext(), llPlayAll)
            applyButtonTheme(requireContext(), llPlayAllActionBar)
            Constant.screen_name ="Album Detail"
            if (arguments != null) {
                selectedContentId = requireArguments().getString("id").toString()
                if (requireArguments().containsKey(Constant.isFromVerticalPlayer)) {
                    isFromVerticalPlayer = requireArguments().getBoolean(Constant.isFromVerticalPlayer)
                    setLog(TAG, "initializeComponent: selectedContentId " + selectedContentId)
                }
            }


            ivBack?.setOnClickListener { backPress() }
            ivBack2?.setOnClickListener { backPress() }
            rlHeading?.visibility = View.INVISIBLE

            scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@AlbumDetailFragment)

            rvAlbumlist?.visibility =View.VISIBLE
            //rvSimilarAlbum.visibility =View.VISIBLE

            shimmerLayout?.visibility = View.VISIBLE
            shimmerLayout?.startShimmer()
            setUpAlbumDetailListViewModel()

            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@AlbumDetailFragment)
            llPlayAll?.setOnClickListener{
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(requireContext(), llPlayAll!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                }catch (e:Exception){

                }
                if (albumRespModel?.data?.body?.rows!!.isNotEmpty()){
                    setProgressBarVisible(true)
                    playAllAlbumList()
                }

            }
            llPlayAllActionBar?.setOnClickListener{
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(requireContext(), llPlayAllActionBar!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                }catch (e:Exception){

                }
                if (!albumRespModel?.data?.body?.rows.isNullOrEmpty()){
                    setProgressBarVisible(true)
                    playAllAlbumList()
                }
            }

            CommonUtils.getbanner(requireContext(),iv_banner,Constant.nudge_album_banner)

            threeDotMenu?.setOnClickListener(this@AlbumDetailFragment)
            threeDotMenu2?.setOnClickListener(this@AlbumDetailFragment)
            ivFavorite?.setOnClickListener(this@AlbumDetailFragment)
            ivFavoriteActionBar?.setOnClickListener(this@AlbumDetailFragment)
            CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        }
    }



    fun setArtImageBg(status: Boolean){
        baseIOScope.launch{
            if (activity!=null&& artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl)&& albumDetailroot != null) {
                val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
                withContext(Dispatchers.Main){
                    albumDetailroot?.background = bgColor
                    ImageLoader.loadImage(requireContext(),
                        iv_collapsingImageBg, artImageUrl.toString(),
                        R.drawable.bg_gradient_placeholder)
                    fullGradient?.visibility = View.VISIBLE
                }
                val result: Deferred<Bitmap?> =  baseIOScope.async {
                    val urlImage = URL(artImageUrl)
                    urlImage.toBitmap()
                }

                baseIOScope.launch{

                    try {
                        // get the downloaded bitmap
                        val bitmap : Bitmap? = result.await()
                        val artImage = BitmapDrawable(resources, bitmap)
                        if (status){
                            if (bitmap != null){
                                Palette.from(bitmap!!).generate { palette ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        artworkProminentColor = calculateAverageColor(bitmap, 1)
                                        baseMainScope.launch {
                                            if (context != null) {
                                                CommonUtils.setLog(
                                                    "ChartLifecycle",
                                                    "setArtImageBg--$artworkProminentColor"
                                                )
                                                changeStatusbarcolor(artworkProminentColor)
                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }catch (e:Exception){

                    }


                }
            }
        }


    }
    fun URL.toBitmap(): Bitmap?{
        return try {
            BitmapFactory.decodeStream(openStream())
        }catch (e: IOException){
            null
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

    private fun playAllAlbumList(){

            if (isPlaying) {
                if (albumSongList?.size!! > 0 && albumSongList != null) {
                    playableItemPosition = 0
                    baseIOScope.launch {
                        setEventModelDataAppLevel(
                            albumSongList?.get(playableItemPosition)?.data?.id!!, albumSongList?.get(
                                playableItemPosition
                            )?.data?.title!!,
                            albumModel?.data?.head?.data?.title!!
                        )
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        val hashMapPageView = HashMap<String, String>()

                        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = albumSongList.get(playableItemPosition).data.title
                        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                            "" + Utils.getContentTypeNameForStream("" + albumSongList?.get(playableItemPosition)?.data?.type)
                        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = albumSongList.get(playableItemPosition).data.id
                        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                            "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "details_Album"
                        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "details_Album"

                        setLog("VideoPlayerPageView", hashMapPageView.toString())
                        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                    }

                    setUpPlayableContentListViewModel(albumSongList?.get(playableItemPosition)?.data?.id!!)
                }
            }else{
                (requireActivity() as MainActivity).pausePlayer()
                baseIOScope.launch {
                    playPauseStatusChange(true)
                }

            }
    }

    private fun setUpAlbumDetailListViewModel() {
        try {
            if (ConnectionUtil(context).isOnline) {
                albumListViewModel = ViewModelProvider(
                    this
                ).get(AlbumViewModel::class.java)

                albumListViewModel?.getAlbumDetailList(requireContext(), selectedContentId!!)?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
//                            if(Constant.IS_SEARCH_RECOMMENDED_DISPLAY){
//                                getYouMayLikeCall(it?.data!!)
//                            }else{
                                fillAlbumDetail(it?.data!!)
//                            }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(false)
                            }

                            Status.ERROR ->{
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                loadOfflinePlaylist()
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
                loadOfflinePlaylist()
            }
        }catch (e:Exception){

        }

    }

    private fun fillAlbumDetail(data: PlaylistDynamicModel) {
        setDetails(data!!, true)
        albumDetailroot.visibility=View.VISIBLE
        setAlbumDetailsListData(data!!)
    }


    fun loadOfflinePlaylist(){
        baseIOScope.launch {
            val albumListoffline = AppDatabase.getInstance()?.downloadedAudio()
                ?.getAlbumList(selectedContentId.toString()) as ArrayList<DownloadedAudio>
            val albumModel = PlaylistDynamicModel()
            if (!albumListoffline.isNullOrEmpty()){
                albumModel.data.head.itype = 2
                albumModel.data.head.data.id = albumListoffline.get(0).parentId.toString()
                albumModel.data.head.data.title = albumListoffline.get(0).pName.toString()
                albumModel.data.head.data.type = 55555
                albumModel.data.head.data.image = albumListoffline.get(0).parentThumbnailPath.toString()
                albumModel.data.head.data.releasedate = albumListoffline.get(0).pReleaseDate.toString()
                albumModel.data.head.data.subtitle = albumListoffline.get(0).pSubName.toString()
            }
            val albumlistItems: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
            albumListoffline.forEachIndexed { index, da ->
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
                        row.data.misc.favCount = da.f_fav_count!!
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
                    albumlistItems.add(row)
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
                    albumlistItems.add(row)
                }

            }
            albumModel.data.body.rows = albumlistItems
            withContext(Dispatchers.Main){
                setDetails(albumModel, true)
                albumDetailroot?.visibility=View.VISIBLE
                setAlbumDetailsListData(albumModel)
            }
        }
    }

    fun setAlbumDetailsListData(albumModel: PlaylistDynamicModel) {
        this.albumModel=albumModel
        baseMainScope.launch {
            if (albumModel != null && albumModel?.data?.body != null) {
                albumRespModel = albumModel
                setupUserViewModel()
                if (!albumModel.data?.body?.rows.isNullOrEmpty()) {
                    albumSongList = albumModel.data.body.rows
                    ivDownloadFullList?.setOnClickListener(this@AlbumDetailFragment)
                    ivDownloadFullListActionBar?.setOnClickListener(this@AlbumDetailFragment)
                    rvAlbumlist?.apply {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                        //setRecycledViewPool(RecyclerView.RecycledViewPool())
                        isNestedScrollingEnabled = false
                        setHasFixedSize(true)

                    }
                    setAlbumSongAdapter(ascending)

                }
                if (albumModel.data?.body?.recomendation != null && albumModel.data?.body?.recomendation?.size!! > 0) {
                    rvRecomendation?.visibility = View.VISIBLE
                    rvRecomendation?.visibility = View.VISIBLE

                    val varient = Constant.ORIENTATION_HORIZONTAL

                    val bucketParentAdapter = BucketParentAdapter(
                        albumModel.data?.body?.recomendation!!,
                        requireContext(),
                        this@AlbumDetailFragment,
                        this@AlbumDetailFragment,
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


                    bucketParentAdapter.addData(albumModel.data.body.recomendation!!)
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
                                    albumModel.data.body.recomendation?.get(firstVisiable)?.heading
                                val toBucket =
                                    albumModel.data.body.recomendation?.get(lastVisiable)?.heading
                                val sourcePage =
                                    MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                                if (!fromBucket?.equals(toBucket, true)!!) {
                                    baseIOScope.launch {
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


                    rvAlbumlist?.setPadding(0, 0, 0, 0)
                    rvRecomendation?.setPadding(0, 0, 0, 0)
                }

                var isContentAutoPlay=0
                if(requireArguments().containsKey("isPlay")){
                    isContentAutoPlay = requireArguments().getInt("isPlay")
                }
                setLog(TAG, "setPlayListSongAdapter: auto play${isContentAutoPlay}")
                if (isContentAutoPlay == 1) {
                    llPlayAll?.performClick()
                }
            }
            shimmerLayout?.visibility = View.GONE
            shimmerLayout?.stopShimmer()
            scrollView?.visibility = View.VISIBLE
        }

    }

    private fun setAlbumSongAdapter(asc: Boolean) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                baseMainScope.launch {
                    albumAdpter = DetailAlbumAdapter(
                        requireContext(), albumSongList,
                        object : DetailAlbumAdapter.OnChildItemClick {
                            override fun onUserClick(
                                childPosition: Int,
                                isMenuClick: Boolean,
                                isDownloadClick: Boolean
                            ) {
                                albumSongItem = albumSongList?.get(childPosition)
                                playableItemPosition = childPosition
                                BaseActivity.setTouchData()
                                if (isMenuClick) {
                                    if (isOnClick()) {
                                        val isFavorite =
                                            albumSongList?.get(childPosition)?.data?.isFavorite!!
                                        commonThreeDotMenuItemSetup(
                                            ALBUM_DETAIL_ADAPTER,
                                            this@AlbumDetailFragment,
                                            isFavorite
                                        )
                                    }
                                } else if (isDownloadClick) {
                                    val dpm = DownloadPlayCheckModel()
                                    dpm.contentId =
                                        albumSongList?.get(childPosition)?.data?.id?.toString()!!
                                    dpm.contentTitle =
                                        albumSongList?.get(childPosition)?.data?.title?.toString()!!
                                    dpm.planName =
                                        albumSongList?.get(childPosition)?.data?.misc?.movierights.toString()
                                    dpm.isAudio = true
                                    dpm.isDownloadAction = true
                                    dpm.isShowSubscriptionPopup = true
                                    dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                                    dpm.restrictedDownload =
                                        RestrictedDownload.valueOf(albumSongList?.get(childPosition)?.data?.misc?.restricted_download!!)
                                    if (CommonUtils.userCanDownloadContent(
                                            requireContext(),
                                            albumDetailroot,
                                            dpm,
                                            this@AlbumDetailFragment,Constant.drawer_downloads_exhausted
                                        )
                                    ) {
                                        val downloadQueueList: ArrayList<DownloadQueue> =
                                            ArrayList()
                                        var dq = DownloadQueue()
                                        //for (item in albumSongList?.iterator()!!){

                                        dq = DownloadQueue()
                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.id.toString())) {
                                            dq.contentId =
                                                albumSongList?.get(childPosition)?.data?.id.toString()
                                        }

                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.title!!)) {
                                            dq.title =
                                                albumSongList?.get(childPosition)?.data?.title!!
                                        }

                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.subtitle!!)) {
                                            dq.subTitle =
                                                albumSongList?.get(childPosition)?.data?.subtitle!!
                                        }

                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.image!!)) {
                                            dq.image =
                                                albumSongList?.get(childPosition)?.data?.image!!
                                        }

                                        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.id!!)) {
                                            dq.parentId = albumRespModel?.data?.head?.data?.id!!
                                        }
                                        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.title!!)) {
                                            dq.pName = albumRespModel?.data?.head?.data?.title
                                        }

                                        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.subtitle!!)) {
                                            dq.pSubName = albumRespModel?.data?.head?.data?.subtitle
                                        }

                                        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.releasedate!!)) {
                                            dq.pReleaseDate =
                                                albumRespModel?.data?.head?.data?.releasedate
                                        }

                                        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.image!!)) {
                                            dq.pImage = albumRespModel?.data?.head?.data?.image
                                        }



                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.misc?.movierights.toString()!!)) {
                                            dq.planName =
                                                albumSongList?.get(childPosition)?.data?.misc?.movierights.toString()
                                        }

                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.misc?.f_FavCount.toString()!!)) {
                                            dq.f_fav_count =
                                                albumSongList?.get(childPosition)?.data?.misc?.f_FavCount.toString()
                                        }

                                        if (!TextUtils.isEmpty(albumSongList?.get(childPosition)?.data?.misc?.f_playcount.toString()!!)) {
                                            dq.f_playcount =
                                                albumSongList?.get(childPosition)?.data?.misc?.f_playcount.toString()
                                        }

                                        dq.pType = DetailPages.ALBUM_DETAIL_PAGE.value
                                        dq.contentType = ContentTypes.AUDIO.value
                                        val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                                        dq.source = eventModel.sourceName

                                        val downloadQueue =
                                            AppDatabase.getInstance()?.downloadQueue()
                                                ?.findByContentId(albumSongList?.get(childPosition)?.data?.id!!.toString())
                                        val downloadedAudio =
                                            AppDatabase.getInstance()?.downloadedAudio()
                                                ?.findByContentId(albumSongList?.get(childPosition)?.data?.id!!.toString())
                                        if ((!downloadQueue?.contentId.equals(
                                                albumSongList?.get(
                                                    childPosition
                                                )?.data?.id!!.toString()
                                            ))
                                            && (!downloadedAudio?.contentId.equals(
                                                albumSongList?.get(
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
                                            this@AlbumDetailFragment,
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
                                                ?.findByContentId(albumSongList?.get(childPosition)?.data?.id!!.toString())
                                        if (downloadedAudio != null && downloadedAudio?.contentId.equals(
                                                albumSongList?.get(childPosition)?.data?.id!!.toString()
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
                                                downloadedAudio.drmLicense!!
                                            playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                                                downloadedAudio.lyricsUrl
                                            setPlayableContentListData(playableContentModel)
                                        } else {
                                            setEventModelDataAppLevel(
                                                albumSongList?.get(childPosition)?.data?.id!!,
                                                albumSongList?.get(childPosition)?.data?.title!!,
                                                albumModel?.data?.head?.data?.title!!
                                            )
                                            setUpPlayableContentListViewModel(
                                                albumSongList?.get(
                                                    childPosition
                                                )?.data?.id!!
                                            )
                                        }
                                    }

                                }

                            }


                        })
                    rvAlbumlist?.adapter = albumAdpter
                }
            }
            setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-0")
            async { checkAllContentDownloadedOrNot(albumSongList) }.await()
            setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-100")
            withContext(Dispatchers.IO){
                delay(3000)
                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-101")
                albumSongList = getAdsData(albumSongList)
                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-104")
                withContext(Dispatchers.Main){
                    setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-105")
                    if (isAdded && context != null && albumAdpter != null){
                        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-106")
                        albumAdpter?.notifyDataSetChanged()
                    }
                }
            }
        }

    }


    private fun setDetails(it: PlaylistDynamicModel?, status: Boolean) {
        albumModel=it
        baseMainScope.launch {
            if (context != null){
                llDetails2?.visibility = View.VISIBLE
                centerDivider?.visibility = View.VISIBLE
                albumAlbumArtImageView?.visibility = View.VISIBLE
//        scrollView.visibility = View.VISIBLE

                artImageUrl=albumModel?.data?.head?.data?.image
                playerType=""+albumModel?.data?.head?.data?.type

                if (!TextUtils.isEmpty(artImageUrl)){
                    ImageLoader.loadImage(
                        requireContext(),
                        albumAlbumArtImageView,
                        artImageUrl!!,
                        R.drawable.bg_gradient_placeholder
                    )
                    setArtImageBg(true)
                }else{
                    ImageLoader.loadImage(
                        requireContext(),
                        albumAlbumArtImageView,
                        "",
                        R.drawable.bg_gradient_placeholder
                    )
                    staticToolbarColor()
                }
                if(it?.data?.head?.data?.title!=null&&!TextUtils.isEmpty(it?.data?.head?.data?.title)){
                    tvTitle?.text = it?.data?.head?.data?.title
                    tvTitle?.visibility = View.VISIBLE
                }else{
                    tvTitle?.visibility = View.GONE
                }

                if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.vendor)) {
                    tvSubTitle?.text = getString(R.string.playlist_str_2)+" "+it?.data?.head?.data?.misc?.vendor
                    tvSubTitle?.visibility=View.VISIBLE
                }else{
                    tvSubTitle?.visibility=View.GONE
                }


                if(it?.data?.head?.data?.misc!=null){
                    var subtitle=""
                    if(!TextUtils.isEmpty(""+it?.data?.head?.data?.misc?.f_playcount)){
                        subtitle+=""+it?.data?.head?.data?.misc?.f_playcount + " " + getString(R.string.artist_str_4)+" • "
                    }
                    subtitle+= ""+it?.data?.head?.data?.misc?.items + " " + getString(R.string.library_playlist_str_8)

                    tvSubTitle2?.text = subtitle

                    if(it?.data?.head?.data?.misc?.description!=null &&!TextUtils.isEmpty(it?.data?.head?.data?.misc?.description?.trim())){
                        SaveState.isCollapse = true
                        tvReadMore?.text = it?.data?.head?.data?.misc?.description
                        tvReadMore?.setShowingLine(2)
                        tvReadMore?.addShowMoreText("read more")
                        tvReadMore?.addShowLessText("read less")
                        tvReadMore?.setShowMoreColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                        tvReadMore?.setShowLessTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                        tvReadMore?.setShowMoreStyle(Typeface.BOLD)
                        tvReadMore?.setShowLessStyle(Typeface.BOLD)
                        tvReadMore?.visibility=View.VISIBLE
                    }else{
                        tvReadMore?.visibility=View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        baseMainScope.launch {
            if (context != null){
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
        super.onDestroy()
        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
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
        try {
            val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            Util.startForegroundService(getViewActivity(), intent)
            if (activity != null && activity is MainActivity){
                (activity as MainActivity).reBindService()

                playPauseStatusChange(false)
            }
        }catch (e:Exception){

        }

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setUpPlayableContentListViewModel(id:String) {
        try {
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
                                    if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                        setPlayableContentListData(it?.data!!)
                                    }else{
                                        playableItemPosition = playableItemPosition +1
                                        if (playableItemPosition < albumSongList?.size!!) {
                                            setUpPlayableContentListViewModel(albumSongList?.get(playableItemPosition)?.data?.id!!)
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
        }catch (e:Exception){

        }

    }


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (playableContentModel != null ) {
                if (!CommonUtils.checkExplicitContent(requireContext(), playableContentModel.data.head.headData.misc.explicit)){
                    setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
                    songDataList = arrayListOf()

                    for (i in albumSongList?.indices!!){
                        if (playableContentModel?.data?.head?.headData?.id == albumSongList?.get(i)?.data?.id){
                            setAlbumSongList(playableContentModel, albumSongList, playableItemPosition)
                        }else if(i > playableItemPosition){
                            setAlbumSongList(null, albumSongList, i)
                        }
                    }

                    BaseActivity.setTrackListData(songDataList)
                    tracksViewModel.prepareTrackPlayback(0)
                }
            }
        }

    }
    var songDataList:ArrayList<Track> = arrayListOf()
    fun setAlbumSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row?>?,
        position: Int
    ): ArrayList<Track> {
        val track:Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)){
            track.id = playableItem?.get(position)?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)){
            track.title = playableItem?.get(position)?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subtitle)){
            track.subTitle = playableItem?.get(position)?.data?.subtitle
        }else{
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
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
        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.title)){
            track.heading = albumRespModel?.data?.head?.data?.title
        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.playble_image)){
            track.image = playableItem?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)){
            track.image = playableItem?.get(position)?.data?.image
        }else{
            track.image = ""
        }
        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.id!!)){
            track.parentId = albumRespModel?.data?.head?.data?.id!!
        }
        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.title!!)){
            track.pName = albumRespModel?.data?.head?.data?.title
        }

        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.subtitle!!)){
            track.pSubName = albumRespModel?.data?.head?.data?.subtitle
        }

        if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.image!!)){
            track.pImage = albumRespModel?.data?.head?.data?.image
        }

        track.pType = DetailPages.ALBUM_DETAIL_PAGE.value
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
        return songDataList
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = 0
            maxDistance += topView.marginTop + albumAlbumArtImageView.marginTop + albumAlbumArtImageView.marginBottom + albumAlbumArtImageView.height + llDetails.marginTop + llDetails.marginBottom + llDetails.height + llDetails2.marginTop + llDetails2.marginBottom + llDetails2.height + resources.getDimensionPixelSize(R.dimen.dimen_10_5)
            //maxDistance  += (artworkHeight / 2) + resources.getDimensionPixelSize(R.dimen.dimen_127)

            /* how much we have scrolled */
            val movement = scrollView.scrollY

            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance){
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur?.visibility = View.INVISIBLE
                rlHeading?.visibility = View.VISIBLE
                if (artworkProminentColor == 0){
                    rlHeading?.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                }else{
                    rlHeading?.setBackgroundColor(artworkProminentColor)
                }

            }else{
                //setLog("OnNestedScroll-m--", movement.toString())
                //setLog("OnNestedScroll-d--", maxDistance.toString())
                rlHeading?.visibility = View.INVISIBLE
                headBlur?.visibility = View.INVISIBLE
                rlHeading?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.transparent
                    )
                )
            }

        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == threeDotMenu || v == threeDotMenu2){
            commonThreeDotMenuItemSetup(ALBUM_DETAIL_PAGE)
        }else if (v == ivFavorite || v == ivFavoriteActionBar){
            albumRespModel?.let { setAddOrRemoveFavourite(it) }
        }else if (v == ivDownloadFullList || v == ivDownloadFullListActionBar){
            baseIOScope.launch {
                if (isAdded && context != null){
                    val dpm = DownloadPlayCheckModel()
                    dpm.contentId = albumRespModel?.data?.head?.data?.id.toString()
                    dpm.planName = PlanNames.NONE.name
                    dpm.isAudio = true
                    dpm.isDownloadAction = true
                    dpm.isShowSubscriptionPopup = true
                    dpm.clickAction = ClickAction.FOR_ALL_CONTENT
                    dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT

                    if (CommonUtils.userCanDownloadContent(requireContext(), albumDetailroot, dpm, this@AlbumDetailFragment,Constant.drawer_download_all)){
                        val contentTypes:Array<Int> = arrayOf(ContentTypes.AUDIO.value, ContentTypes.PODCAST.value)
                        val allDownloadItemList = AppDatabase.getInstance()?.downloadedAudio()?.getContentsByContentType(contentTypes)
                        var existingQueueItemsCount = 0
                        if (allDownloadItemList != null) {
                            existingQueueItemsCount = allDownloadItemList.size
                        }
                        val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                        var dq = DownloadQueue()
                        var count = 1
                        val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                        for (item in albumSongList?.iterator()!!){

                            dq = DownloadQueue()

                            dq.contentId = item?.data?.id


                            if (!TextUtils.isEmpty(item?.data?.title)){
                                dq.title = item?.data?.title!!
                            }

                            if (!TextUtils.isEmpty(item?.data?.subtitle!!)){
                                dq.subTitle = item?.data?.subtitle!!
                            }

                            if (!TextUtils.isEmpty(item?.data?.image!!)){
                                dq.image = item?.data?.image!!
                            }

                            if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.id!!)){
                                dq.parentId = albumRespModel?.data?.head?.data?.id!!
                            }
                            if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.title!!)){
                                dq.pName = albumRespModel?.data?.head?.data?.title
                            }

                            if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.subtitle!!)){
                                dq.pSubName = albumRespModel?.data?.head?.data?.subtitle
                            }

                            if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.releasedate!!)){
                                dq.pReleaseDate = albumRespModel?.data?.head?.data?.releasedate
                            }

                            if (!TextUtils.isEmpty(albumRespModel?.data?.head?.data?.image!!)){
                                dq.pImage = albumRespModel?.data?.head?.data?.image
                            }

                            if (!TextUtils.isEmpty(item?.data?.misc?.movierights.toString()!!)){
                                dq.planName = item?.data?.misc?.movierights.toString()
                            }

                            if (!TextUtils.isEmpty(item?.data?.misc?.f_FavCount.toString()!!)){
                                dq.f_fav_count = item?.data?.misc?.f_FavCount.toString()
                            }

                            if (!TextUtils.isEmpty(item?.data?.misc?.f_playcount.toString()!!)){
                                dq.f_playcount = item?.data?.misc?.f_playcount.toString()
                            }



                            dq.pType = DetailPages.ALBUM_DETAIL_PAGE.value
                            dq.contentType = ContentTypes.AUDIO.value
                            dq.downloadAll = 1
                            dq.source = eventModel.sourceName



                            if (existingQueueItemsCount > CommonUtils.getAvailableDownloadContentSize(requireContext())){
                                break
                            }
                            existingQueueItemsCount += 1
                            if (!AppDatabase.getInstance()?.downloadQueue()?.findByContentId(item?.data?.id.toString())?.contentId.equals(item?.data?.id.toString())
                                && !AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(item?.data?.id.toString())?.contentId.equals(item?.data?.id.toString())){
                                //if (count <= 3){
                                downloadQueueList.add(dq)
                                //}
                            }
                            count++
                        }
                        //if (downloadQueueList.size > 0){
                        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                            downloadQueueList,
                            this@AlbumDetailFragment,
                            null,
                            false,
                            true
                        )
                        //}
                        /*val drawable = FontDrawable(requireContext(), R.string.icon_downloading)
                        drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                        ivDownloadFullList.setImageDrawable(drawable)
                        ivDownloadFullListActionBar.setImageDrawable(drawable)*/
                        withContext(Dispatchers.Main){
                            ivDownloadFullList?.setImageDrawable(requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite, resources.getDimensionPixelSize(R.dimen.font_16).toFloat()))
                            ivDownloadFullListActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite))
                        }
                    }
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

    private fun setAddOrRemoveFavourite(albumRespModel: PlaylistDynamicModel) {
        if (ConnectionUtil(context).isOnline) {
            isFavourite = !isFavourite
            val jsonObject = JSONObject()
            jsonObject.put("contentId", albumRespModel?.data?.head?.data?.id!!)
            jsonObject.put("typeId", albumRespModel?.data?.head?.data?.type!!)
            jsonObject.put("action",isFavourite)
            jsonObject.put("module",MODULE_FAVORITE)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()
            baseIOScope.launch {
                if(isFavourite){
                    val messageModel = MessageModel(getString(R.string.album_str_3), getString(R.string.album_str_4),
                        MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                    val hashMap = java.util.HashMap<String, String>()
                    hashMap.put(EventConstant.ACTOR_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.misc?.actorf!!))
                    hashMap.put(EventConstant.ALBUMID_EPROPERTY,""+albumRespModel?.data?.head?.data?.id)
                    hashMap.put(EventConstant.CATEGORY_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.category!!))
                    var newContentId=albumRespModel?.data?.head?.data?.id!!
                    var contentIdData=newContentId.replace("playlist-","")
                    hashMap.put(EventConstant.CONTENTID_EPROPERTY,""+contentIdData)
                    val albumType=albumRespModel?.data?.head?.data?.type
                    setLog(
                        TAG,
                        "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" +albumType)} albumType:${albumType}"
                    )
                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+albumRespModel?.data?.head?.data?.type!!))
                    hashMap.put(EventConstant.GENRE_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.genre!!))
                    hashMap.put(EventConstant.LANGUAGE_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.misc?.lang!!))
                    hashMap.put(EventConstant.LYRICIST_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.misc?.lyricist!!))
                    hashMap.put(EventConstant.MOOD_EPROPERTY,""+ albumRespModel?.data?.head?.data?.misc?.mood)
                    hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.misc?.musicdirectorf!!))
                    hashMap.put(EventConstant.NAME_EPROPERTY,""+ albumRespModel?.data?.head?.data?.title)
                    hashMap.put(EventConstant.PODCASTHOST_EPROPERTY,"")
                    hashMap.put(EventConstant.SINGER_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.misc?.singerf!!))
                    hashMap.put(EventConstant.SOURCE_EPROPERTY,""+albumRespModel?.data?.head?.data?.title)
                    hashMap.put(EventConstant.TEMPO_EPROPERTY,Utils.arrayToString(albumRespModel?.data?.head?.data?.misc?.tempo!!))
                    hashMap.put(EventConstant.CREATOR_EPROPERTY,"Hungama")
                    hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,albumRespModel?.data?.head?.data?.releasedate))
                    EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))

                }else{
                    val messageModel = MessageModel(getString(R.string.popup_str_83), getString(R.string.popup_str_84),
                        MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
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
                userViewModel?.getUserBookmarkedData(requireContext(), MODULE_FAVORITE, "1")?.observe(this,
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
                                setEmptyVisible(false)
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

    private fun fillUI(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        baseIOScope.launch {
            if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0){
                for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!){
                    if (albumRespModel?.data?.head?.data?.id?.equals(bookmark?.data?.id)!!){
                        isFavourite = true
                    }
                    if (!albumSongList.isNullOrEmpty()){
                        albumSongList?.forEachIndexed { index, row ->
                            if (row?.data?.id?.equals(bookmark?.data?.id)!!){
                                row?.data?.isFavorite = true
                            }
                        }
                    }
                }
                setFollowingStatus()
            }
        }

    }

    fun setFollowingStatus(){
        baseMainScope.launch {
            if (isFavourite){
                //ivFavorite.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
                //ivFavoriteActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
                ivFavorite?.playAnimation()
                ivFavoriteActionBar?.playAnimation()
            }else{
                //ivFavorite.setImageDrawable(requireContext().faDrawable(R.string.icon_like, R.color.colorWhite))
                //ivFavoriteActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_like, R.color.colorWhite))
                ivFavorite?.cancelAnimation()
                ivFavorite?.progress = 0f
                ivFavoriteActionBar?.cancelAnimation()
                ivFavoriteActionBar?.progress = 0f
            }
        }
    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        if (albumSongList != null && albumSongList?.size!! > 0){
            albumSongList?.get(position)?.data?.isFavorite = isFavorite
        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
            setLog("DWProgrss-onChangedid", data.id.toString())
            setLog("DWProgrss-onChanged", reason.toString())
            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByDownloadManagerId(data.id)
            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

            when(reason){
                Reason.DOWNLOAD_ADDED -> {
                    setLog("DWProgrss-ADDED", data.id.toString())
                }
                Reason.DOWNLOAD_QUEUED ->{
                    try {
                        setLog("DWProgrss-QUEUED", data.id.toString())
                        if (albumAdpter != null){
                            if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                    albumRespModel?.data?.head?.data?.id!!)!!){
                                val index = albumSongList?.indexOfFirst{
                                    it?.data?.id == downloadQueue.contentId
                                }
                                if (index != null) {
                                    withContext(Dispatchers.Main){
                                        albumAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                    albumRespModel?.data?.head?.data?.id!!)!!){
                                val index = albumSongList?.indexOfFirst{
                                    it?.data?.id == downloadedAudio.contentId!!
                                }
                                if (index != null) {
                                    withContext(Dispatchers.Main){
                                        albumAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }
                    }catch (e:Exception){

                    }
                }
                Reason.DOWNLOAD_STARTED->{
                    try {
                        setLog("DWProgrss-STARTED", data.id.toString())
                        if (albumAdpter != null){
                            if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                    albumRespModel?.data?.head?.data?.id!!)!!){
                                val index = albumSongList?.indexOfFirst{
                                    it?.data?.id == downloadQueue.contentId
                                }
                                if (index != null) {
                                    withContext(Dispatchers.Main){
                                        albumAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                    albumRespModel?.data?.head?.data?.id!!)!!){
                                val index = albumSongList?.indexOfFirst{
                                    it?.data?.id == downloadedAudio.contentId!!
                                }
                                if (index != null) {
                                    withContext(Dispatchers.Main){
                                        albumAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }
                    }catch (e:Exception){

                    }
                }
                Reason.DOWNLOAD_PROGRESS_CHANGED->{
                    setLog("DWProgrss-CHANGED", data.id.toString())
                }
                Reason.DOWNLOAD_RESUMED->{
                    setLog("DWProgrss-RESUMED", data.id.toString())
                }
                Reason.DOWNLOAD_PAUSED->{
                    setLog("DWProgrss-PAUSED", data.id.toString())
                }
                Reason.DOWNLOAD_COMPLETED->{
                    try {
                        setLog("DWProgrss-COMPLETED", data.id.toString())
                        if (albumAdpter != null){
                            if (downloadQueue != null && downloadQueue?.parentId != null && downloadQueue.parentId?.equals(
                                    albumRespModel?.data?.head?.data?.id!!)!!){
                                val index = albumSongList?.indexOfFirst{
                                    it?.data?.id == downloadQueue.contentId
                                }
                                if (index != null) {
                                    withContext(Dispatchers.Main){
                                        albumAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }else if (downloadedAudio != null && downloadedAudio?.parentId != null && downloadedAudio.parentId?.equals(
                                    albumRespModel?.data?.head?.data?.id!!)!!){
                                val index = albumSongList?.indexOfFirst{
                                    it?.data?.id == downloadedAudio.contentId!!
                                }
                                if (index != null) {
                                    withContext(Dispatchers.Main){
                                        albumAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                        if (!albumSongList.isNullOrEmpty()){
                            if (isAdded){
                                checkAllContentDownloadedOrNot(albumSongList)
                            }
                        }else{
                            playPauseStatusChange(true)
                        }
                    }catch (e:Exception){

                    }
                }
                Reason.DOWNLOAD_CANCELLED->{
                    setLog("DWProgrss-CANCELLED", data.id.toString())
                }
                Reason.DOWNLOAD_REMOVED->{
                    setLog("DWProgrss-REMOVED", data.id.toString())
                }
                Reason.DOWNLOAD_DELETED->{
                    setLog("DWProgrss-DELETED", data.id.toString())
                }
                Reason.DOWNLOAD_ERROR->{
                    setLog("DWProgrss-ERROR", data.id.toString())
                }
                Reason.DOWNLOAD_BLOCK_UPDATED->{
                    setLog("DWProgrss-UPDATED", data.id.toString())
                }
                Reason.DOWNLOAD_WAITING_ON_NETWORK->{
                    setLog("DWProgrss-NETWORK", data.id.toString())
                }
                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(ArrayList(),
            this,
            null,
            true,
            false
        )
        if (!albumSongList.isNullOrEmpty()){
            checkAllContentDownloadedOrNot(albumSongList)
        }else{
            playPauseStatusChange(true)
        }

        setLog(TAG, "onResume......................", )
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

    private fun checkAllContentDownloadedOrNot(albumSongList: List<PlaylistModel.Data.Body.Row?>?) {
        baseIOScope.launch {
            if (isAdded && context != null){
                var isCurrentContentPlayingFromThis = false
                if (!albumSongList.isNullOrEmpty()) {
                    if (albumRespModel != null && albumRespModel?.data?.head?.data != null) {
                        setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-1-$isCurrentContentPlayingFromThis")
                        isCurrentContentPlayingFromThis =
                            withContext(Dispatchers.Default) {
                                setLog("isCurrentContentPlayingFromThis", "isCurrentContentPlayingFromThis-2-$isCurrentContentPlayingFromThis")
                                checkAllContentDWOrNot(albumSongList)
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
                                if (albumAdpter != null) {
                                    //playlistAdpter?.notifyDataSetChanged()
                                    setLog("isCurrentPlaying", "DetailChartAdapter-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                                    albumAdpter?.notifyItemChanged(lastPlayingContentIndex)
                                    albumAdpter?.notifyItemChanged(currentPlayingContentIndex)
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

    private fun playPauseStatusChange(status:Boolean){
        isPlaying = status
        baseMainScope.launch {
            if (isAdded && context != null){
                if (status){
                    ivPlayAll?.setImageDrawable(requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite))
                    tvPlayAll?.text = getString(R.string.podcast_str_4)
                    ivPlayAllActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite))
                    tvPlayAllActionBar?.text = getString(R.string.podcast_str_4)
                }else{
                    ivPlayAll?.setImageDrawable(requireContext().faDrawable(R.string.icon_pause_3, R.color.colorWhite))
                    tvPlayAll?.text = getString(R.string.general_str)
                    ivPlayAllActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_pause_3, R.color.colorWhite))
                    tvPlayAllActionBar?.text = getString(R.string.general_str)
                }
            }
        }

    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (!albumSongList.isNullOrEmpty()){
                    checkAllContentDownloadedOrNot(albumSongList)
                }else{
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
            if (!albumSongList.isNullOrEmpty()){
                checkAllContentDownloadedOrNot(albumSongList)
            }else{
                playPauseStatusChange(true)
            }
            baseMainScope.launch {
                if (isAdded && context != null) {
                    CommonUtils.setLog(
                        "AlbumLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        }else {
            baseMainScope.launch {
                if (isAdded && context != null) {
                    changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
                }
            }
        }
    }

    private fun getAdsData(albumSongList: ArrayList<PlaylistModel.Data.Body.Row>): ArrayList<PlaylistModel.Data.Body.Row> {
        if (!albumSongList.isNullOrEmpty() && CommonUtils.isDisplayAds() && CommonUtils.getFirebaseConfigAdsData().playlistDetailsPage.displayAd){
            val adDisplayFirstPosition = CommonUtils.getFirebaseConfigAdsData().playlistDetailsPage.firstAdPositionAfterRows
            val adDisplayPositionFrequency = CommonUtils.getFirebaseConfigAdsData().playlistDetailsPage.repeatFrequencyAfterRows
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
            val iterator = albumSongList.listIterator()
            while (iterator.hasNext()) {
                //setLog("adInserted-1", i.toString())

                if (k>0 && k % adDisplayPosition == 0) {
                    if (isFirstAds){
                        k = 0
                        isFirstAds = false
                        adDisplayPosition = adDisplayPositionFrequency
                    }
                    //setLog("adInserted-2", i.toString())
                    //setLog("adInserted", "Befor==" + homeModel.data?.body?.rows?.get(i)?.heading)

                    val albumRow = PlaylistModel.Data.Body.Row()
                    albumRow.itype = Constant.playlistNativeAds

                    if (adTotalIds > adIdCount){
                        //setLog("adInserted-3", adIdCount.toString())
                        //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                        albumRow.adUnitId = adUnitIdList.get(adIdCount)
                        adIdCount++
                    }else{
                        adIdCount = 0
                        albumRow.adUnitId = adUnitIdList.get(adIdCount)
                        //setLog("adInserted-4", adIdCount.toString())
                        //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                        adIdCount++
                    }

                    iterator.add(albumRow)
                }
                val item = iterator.next()
                i++
                k++
            }
        }
        return albumSongList
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
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading.toString())
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        baseMainScope.launch {
            val bundle = Bundle()
            bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
            setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
            setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
            val moreBucketListFragment = MoreBucketListFragment()
            moreBucketListFragment.arguments = bundle
            addFragment(R.id.fl_container, this@AlbumDetailFragment, moreBucketListFragment, false)

            val dataMap= HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ albumRespModel?.data?.head?.data?.title)

            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,"" + Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }
    }
}