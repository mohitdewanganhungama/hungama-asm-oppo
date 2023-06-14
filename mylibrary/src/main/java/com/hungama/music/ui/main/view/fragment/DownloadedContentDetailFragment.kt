package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.fetch2.AbstractFetchListener
import com.hungama.fetch2.Download
import com.hungama.fetch2.Error
import com.hungama.fetch2.FetchListener
import com.hungama.fetch2core.Reason
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.player.audioplayer.Injection


import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.adapter.DownloadedContentDetailAdapter
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.toBitmap
import com.hungama.music.utils.Constant.DOWNLOADED_CONTENT_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.DOWNLOADED_CONTENT_DETAIL_PAGE
import com.hungama.music.utils.Constant.DOWNLOADED_PODCAST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.DOWNLOADED_PODCAST_DETAIL_PAGE
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_downloaded_content_detail.*
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import java.util.HashMap

//downloadType = ContentTypes.AUDIO.value for songs
//downloadType = ContentTypes.PODCAST.value for podcast
class DownloadedContentDetailFragment(val downloadContentTypes: Int) : BaseFragment(),
    TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnDownloadQueueItemChanged,
    BaseFragment.OnMenuItemClicked, OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack {

    var artImageUrl: String? = null
    private var downloadedDetailBgArtImageDrawable: LayerDrawable? = null

    private lateinit var tracksViewModel: TracksContract.Presenter
    var downloadedContentList: ArrayList<DownloadedAudio>? = null
    var ascending = true
    var downloadedAdpter: DownloadedContentDetailAdapter? = null
    var artworkProminentColor = 0
    var artworkHeight = 0
    var isFavourite = false
    var isFavorite =false

    companion object {
        var downloadedRespModel: ArrayList<DownloadedAudio>? = null
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
        // Inflate the layout for this fragmen
        return inflater.inflate(R.layout.fragment_downloaded_content_detail, container, false)
    }

    override fun initializeComponent(view: View) {

        Constant.screen_name ="Downloaded  Song Screen"
        setLog("alhglallga", "ShowMusicList")
        baseMainScope.launch {
            CommonUtils.applyButtonTheme(requireContext(), llPlayAll)
            CommonUtils.applyButtonTheme(requireContext(), llPlayAllActionBar)
            /*ImageLoader.loadImage(
                requireContext(),
                playlistAlbumArtImageView,
                "",
                R.drawable.bg_gradient_placeholder
            )
            staticToolbarColor()*/

//            ivBack?.setOnClickListener { backPress() }
            ivBack2?.setOnClickListener { backPress() }
            rlHeading.visibility = View.INVISIBLE

            scrollView.viewTreeObserver.addOnScrollChangedListener(this@DownloadedContentDetailFragment)

            rvPlaylist.visibility = View.VISIBLE
            rvTrendingPlaylist.visibility = View.VISIBLE
            setUpDownloadedDetailListViewModel()

            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@DownloadedContentDetailFragment)

            CommonUtils.getbanner(requireContext(),iv_banner,Constant.nudge_playlist_banner)

            threeDotMenu?.setOnClickListener(this@DownloadedContentDetailFragment)
            threeDotMenu2?.setOnClickListener(this@DownloadedContentDetailFragment)
            llPlayAll?.setOnClickListener(this@DownloadedContentDetailFragment)
            llPlayAllActionBar?.setOnClickListener(this@DownloadedContentDetailFragment)
            CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        }
    }



    fun setArtImageBg(artWorkImages: ArrayList<String>) {
        if (activity !== null && artWorkImages != null && artWorkImages?.size!! > 0 && playlistDetailroot != null) {
            val bgColor =
                ColorDrawable(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            val bgImage: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_bg_two)
            val gradient: Drawable? = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.audio_player_gradient_drawable
            )

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    var multiImag: Bitmap? = null
                    try {
                        if (artWorkImages.size > 3) {
                            val bitmaps = ArrayList<Bitmap>()

                            for (image in artWorkImages.iterator()) {
                                val imgFile = File(image)

                                if (imgFile.exists()) {
                                    val myBitmap: Bitmap =
                                        BitmapFactory.decodeFile(imgFile.absolutePath)
                                    val bitmap = CommonUtils.resizeBitmap(myBitmap, 500)
                                    bitmaps.add(bitmap)
                                }else if (!CommonUtils.isFilePath(image)){
                                    val result: Deferred<Bitmap?> = GlobalScope.async {
                                        val urlImage = URL(image)
                                        urlImage.toBitmap()
                                    }
                                    val bitmap: Bitmap? = result.await()
                                    bitmaps.add(bitmap!!)
                                }
                            }
                            multiImag = CommonUtils.mergeMultiple(bitmaps)
                        } else {
                            val imgFile = File(artWorkImages.get(0))
                            if (imgFile.exists()) {
                                val myBitmap: Bitmap =
                                    BitmapFactory.decodeFile(imgFile.absolutePath)
                                multiImag = myBitmap
                            }

                        }
                    }catch (e:Exception){

                    }

                    withContext(Dispatchers.Main){
                        try {
                            playlistAlbumArtImageView.setImageBitmap(multiImag);
                            val bitmap: Bitmap? = multiImag
                            val artImage = BitmapDrawable(resources, bitmap)
                            if (bitmap != null) {
                                //val color = dynamicToolbarColor(bitmap)
                                Palette.from(bitmap!!).generate { palette ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                        /*(activity as AppCompatActivity).window.statusBarColor =
                                            palette?.getDominantColor(R.attr.colorPrimaryDark)!!*/
                                        if (activity != null){
                                            (activity as AppCompatActivity).window.statusBarColor =
                                                CommonUtils.calculateAverageColor(bitmap, 1)
                                        }

                                    }
                                    val color2 =
                                        ColorDrawable(palette?.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))!!)
                                    artworkProminentColor =
                                        palette.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
                                    downloadedDetailBgArtImageDrawable =
                                        LayerDrawable(arrayOf<Drawable>(bgColor, color2, gradient!!))
                                    playlistDetailroot?.background = bgColor
                                    iv_collapsingImageBg?.background = artImage
                                    fullGradient?.visibility = View.VISIBLE
                                }

                            }
                        }catch (e:Exception){

                        }

                    }
                } catch (e: Exception) {

                }


            }
        } else {
            if (isAdded) {
                ImageLoader.loadImage(
                    requireContext(),
                    playlistAlbumArtImageView,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
                staticToolbarColor()
            }
        }

    }

    private fun staticToolbarColor() {
        (activity as AppCompatActivity).window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.home_bg_color)
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
        return onPrepareOptionsMenu(menu)
    }

    private fun playAllPlaylist() {
        if (isPlaying) {
            if (downloadedContentList != null && downloadedContentList?.size!! > 0) {
                playableItemPosition = 0
                setPlayableContentListData()

                CoroutineScope(Dispatchers.Main).launch {
                    val hashMapPageView = HashMap<String, String>()

                    hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =
                        downloadedContentList?.get(playableItemPosition)?.title.toString()
                    hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                        "" + Utils.getContentTypeNameForStream("" + downloadedContentList?.get(playableItemPosition)?.type)
                    hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                        downloadedContentList?.get(playableItemPosition)?.contentId.toString()
                    hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                    hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "Downloaded"
                    hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "Downloaded"

                    setLog("VideoPlayerPageView", hashMapPageView.toString())
                    EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                }
            }
        }else{
            (requireActivity() as MainActivity).pausePlayer()
            playPauseStatusChange(true)
        }
    }

    private fun setUpDownloadedDetailListViewModel() {

        CoroutineScope(Dispatchers.IO).launch {

            delay(1000)

        if (downloadContentTypes == ContentTypes.AUDIO.value) {

            downloadedContentList = AppDatabase.getInstance()?.downloadedAudio()?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value) as ArrayList<DownloadedAudio>?

            if (!downloadedContentList.isNullOrEmpty()) {
                setDownloadedSongsDetailsListData(downloadedContentList, ContentTypes.AUDIO.value)
                setDetails(downloadedContentList)
            }
        } else {
            downloadedContentList = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value) as ArrayList<DownloadedAudio>?

            if (!downloadedContentList.isNullOrEmpty()) {
                setDownloadedSongsDetailsListData(downloadedContentList, ContentTypes.PODCAST.value)
                setDetails(downloadedContentList)

            }
        }

        if (!downloadedContentList.isNullOrEmpty()) {
            val artworkImages = ArrayList<String>()
            for (i in downloadedContentList?.indices!!) {
                if(!downloadedContentList?.get(i)?.thumbnailPath.isNullOrEmpty()&&artworkImages?.size!!<4){
                    setLog(TAG, "setUpDownloadedDetailListViewModel artworkImages:${artworkImages?.size} image path:${downloadedContentList?.get(i)?.thumbnailPath.toString()}")
                    artworkImages.add(downloadedContentList?.get(i)?.thumbnailPath.toString())
                }
                setLog(TAG, "setUpDownloadedDetailListViewModel artworkImages:${artworkImages?.size}")
            }
            setArtImageBg(artworkImages)
        }else{
            /*val intent = Intent(Constant.DOWNLOADED_CONTENT_EVENT)
            intent.putExtra("EVENT", Constant.DOWNLOADED_CONTENT_RESULT_CODE)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)*/
            backPress()
        }
        setLog("ahfgoiahohgoahohgoai", " " + downloadedContentList!!.size.toString())
        }
    }


    private fun setDownloadedSongsDetailsListData(downloadedContentList: ArrayList<DownloadedAudio>?, downloadContentType: Int) {
        baseMainScope.launch {
            downloadedRespModel = downloadedContentList
            if (downloadedContentList != null && downloadedContentList?.size!! > 0) {
                //tvSongsCount.setText(""+downloadedContentList?.size+" Songs")
                rvPlaylist.apply {
                    layoutManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                    setHasFixedSize(true)

                }
                setDownloadedSongsSongAdapter(ascending, downloadContentType)
            }
        }
    }

    private fun setDownloadedSongsSongAdapter(asc: Boolean, downloadContentType: Int) {
        baseMainScope.launch {
            if (isAdded && context != null && !downloadedContentList.isNullOrEmpty()) {
                downloadedAdpter = DownloadedContentDetailAdapter(
                    requireContext(), downloadedContentList, downloadContentType,
                    object : DownloadedContentDetailAdapter.OnChildItemClick {
                        override fun onUserClick(
                            childPosition: Int,
                            isMenuClick: Boolean,
                            isDownloadClick: Boolean
                        ) {
                            playableItemPosition = childPosition
                            if (isMenuClick) {
                                if (isOnClick()) {
                                    if (downloadContentTypes == ContentTypes.AUDIO.value){
                                        if (!downloadedContentList.isNullOrEmpty() && downloadedContentList?.size!! > childPosition && downloadedContentList?.get(childPosition)?.isFavorite!! == 1){
                                            isFavorite = true
                                        }
                                        commonThreeDotMenuItemSetup(
                                            DOWNLOADED_CONTENT_DETAIL_ADAPTER,
                                            this@DownloadedContentDetailFragment,isFavorite
                                        )
                                    }else{
                                        commonThreeDotMenuItemSetup(
                                            DOWNLOADED_PODCAST_DETAIL_ADAPTER,
                                            this@DownloadedContentDetailFragment
                                        )
                                    }

                                }

                            } else {
                                if (isOnClick()) {
                                    if (downloadContentTypes == ContentTypes.AUDIO.value){
                                        setPlayableContentListData()
                                    }else{
                                        setPlayableContentListData()
                                    }
                                }

                            }
                        }
                    })
                //downloadedAdpter?.setHasStableIds(true)
                rvPlaylist.adapter = downloadedAdpter
                checkAllContentDownloadedOrNot(downloadedContentList)
            }

        }
    }
    private fun setDetails(it: ArrayList<DownloadedAudio>?) {
        baseMainScope.launch {
            llDetails2.visibility = View.VISIBLE

            if (downloadContentTypes == ContentTypes.AUDIO.value) {
                tvTitle.text = getString(R.string.library_all_str_1)
            } else {
                tvTitle.text = getString(R.string.library_all_str_2)
            }
            if (downloadContentTypes == ContentTypes.AUDIO.value){
                if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserFirstname())) {
                    tvSubTitle.text =
                        getString(R.string.playlist_str_2) + " " + SharedPrefHelper.getInstance().getUserFirstname()
                            .toString().trim()
                    /*tvSubTitle.text =
                        getString(R.string.playlist_str_2) + " djhgjfhkjghkjfhgkhkjfhjkghkhdjfncnbabcdefghijklmnopqr"*/
                    tvSubTitle?.isSelected = true

                } else {
                    tvSubTitle.visibility = View.GONE
                }
            }else {
                tvSubTitle.visibility = View.GONE
            }
            if (downloadContentTypes == ContentTypes.AUDIO.value){
                var postfix = getString(R.string.library_playlist_str_8)
                if (!it.isNullOrEmpty()){
                    if (it.size == 1){
                        postfix = getString(R.string.song)
                    }
                }
                tvSubTitle2.text = it?.size.toString() + " " + postfix
            }else{
                var postfix = getString(R.string.podcast_str_9)
                if (!it.isNullOrEmpty()){
                    if (it.size == 1){
                        postfix = getString(R.string.episode)
                    }
                }
                tvSubTitle2.text = it?.size.toString() + " " + postfix
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
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

    override fun getViewActivity(): AppCompatActivity {9
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun setPlayableContentListData() {
        if (!downloadedContentList.isNullOrEmpty()) {

            val dpm = DownloadPlayCheckModel()
            dpm.contentId = downloadedContentList?.get(playableItemPosition)?.contentId.toString()
            dpm.contentTitle = downloadedContentList?.get(playableItemPosition)?.title?.toString()!!
            dpm.planName = downloadedContentList?.get(playableItemPosition)?.movierights.toString()
            dpm.isAudio = true
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
            dpm.restrictedDownload =
                RestrictedDownload.valueOf(downloadedContentList?.get(playableItemPosition)?.restrictedDownload!!)
            val freeSongLimit = CommonUtils.getMaxDownloadContentSize(requireContext())
            if (BaseActivity.getIsGoldUser() || (dpm.restrictedDownload != RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT && playableItemPosition < freeSongLimit)) {

                songDataList = arrayListOf()

                for (i in downloadedContentList?.indices!!) {
                    if (i >= playableItemPosition) {
                        if (BaseActivity.getIsGoldUser() || (songDataList.size < freeSongLimit && downloadedContentList?.get(i)?.restrictedDownload != RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT.value && i < freeSongLimit)) {

                            setDownloadedContentList(downloadedContentList?.get(i)!!)
                         }
                    }
                }
                if(CommonUtils.bottomMusicrestricMenu(getViewActivity())) {
                    BaseActivity.setTrackListData(songDataList)
                }

                tracksViewModel.prepareTrackPlayback(0)
            }else if (CommonUtils.userCanDownloadContent(
                    requireContext(),
                    playlistDetailroot,
                    dpm,
                    this,Constant.drawer_downloads_exhausted
                )){

            }
        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setDownloadedContentList(da: DownloadedAudio): ArrayList<Track> {

            val track: Track = Track()
            if (!TextUtils.isEmpty(da.contentId)) {
                track.id = da.contentId?.toLong()!!
            } else {
                track.id = 0
            }
            if (!TextUtils.isEmpty(da.title)) {
                track.title = da.title
            } else {
                track.title = ""
            }

            if (!TextUtils.isEmpty(da.subTitle)) {
                track.subTitle = da.subTitle
            } else {
                track.subTitle = ""
            }

            if (!TextUtils.isEmpty(da.downloadedFilePath)) {
                track.url = Constant.filePrefix + da.downloadedFilePath
            } else {
                track.url = ""
            }
            if (!TextUtils.isEmpty(da.type.toString())) {
                track.playerType = da.type.toString()
            } else {
                track.playerType = MUSIC_PLAYER
            }
            if (!TextUtils.isEmpty(da.pName)) {
                track.heading = da.pName
            } else {
                track.heading = ""
            }
            if (!TextUtils.isEmpty(da.thumbnailPath)) {
                track.image = da.thumbnailPath
            } else {
                track.image = ""
            }

            if (!TextUtils.isEmpty(da.parentId.toString())) {
                track.parentId = da.parentId
            }
            if (!TextUtils.isEmpty(da.pName)) {
                track.pName = da.pName
            }

            if (!TextUtils.isEmpty(da.pSubName)) {
                track.pSubName = da.pSubName
            }

            if (!TextUtils.isEmpty(da.pImage)) {
                track.pImage = da.pImage
            }

            track.pType = da.pType
            track.contentType = da.contentType

            if (da.explicit != null) {
                track.explicit = da.explicit!!
            }
            if (da.restrictedDownload != null) {
                track.restrictedDownload = da.restrictedDownload
            }
            if (da.attribute_censor_rating != null) {
                track.attributeCensorRating =
                    da.attribute_censor_rating.toString()
            }

        if (!da.movierights.isNullOrEmpty()){
            track.movierights = da?.movierights.toString()
        }else{
            track.movierights = ""
        }
            //  track.gold_flag = da.gold_flag

            songDataList.add(track)
        return songDataList

    }

    override fun onScrollChanged() {
        if (isAdded) {
            var maxDistance = 0
            maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_373)
            /* how much we have scrolled */
            val movement = scrollView.scrollY

            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance) {
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.VISIBLE
                rlHeading.visibility = View.VISIBLE
                rlHeading.setBackgroundColor(artworkProminentColor)
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
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == threeDotMenu || v == threeDotMenu2) {
            if (downloadContentTypes == ContentTypes.AUDIO.value){
                commonThreeDotMenuItemSetup(DOWNLOADED_CONTENT_DETAIL_PAGE)
            }else{
                commonThreeDotMenuItemSetup(DOWNLOADED_PODCAST_DETAIL_PAGE)
            }

        } else if (v == llPlayAll || v == llPlayAllActionBar) {
            if (downloadContentTypes == ContentTypes.AUDIO.value){
                playAllPlaylist()
            }else{
                playAllPlaylist()
            }
        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
            //val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

            when (reason) {
                Reason.DOWNLOAD_ADDED -> {
                    //setLog("DWProgrss-ADDED", data.id.toString())
                }
                Reason.DOWNLOAD_QUEUED -> {
                    //setLog("DWProgrss-QUEUED", data.id.toString())
                }
                Reason.DOWNLOAD_STARTED -> {
                    //setLog("DWProgrss-STARTED", data.id.toString())

                }
                Reason.DOWNLOAD_PROGRESS_CHANGED -> {
                    //setLog("DWProgrss-CHANGED", data.id.toString())
                }
                Reason.DOWNLOAD_RESUMED -> {
                    //setLog("DWProgrss-RESUMED", data.id.toString())
                }
                Reason.DOWNLOAD_PAUSED -> {
                    //setLog("DWProgrss-PAUSED", data.id.toString())
                }
                Reason.DOWNLOAD_COMPLETED -> {
                    //setLog("DWProgrss-COMPLETED", data.id.toString())
                }
                Reason.DOWNLOAD_CANCELLED -> {
                    //setLog("DWProgrss-CANCELLED", data.id.toString())
                }
                Reason.DOWNLOAD_REMOVED -> {
                    //setLog("DWProgrss-REMOVED", data.id.toString())
                }
                Reason.DOWNLOAD_DELETED -> {
                    //setLog("DWProgrss-DELETED", data.id.toString())
                }
                Reason.DOWNLOAD_ERROR -> {
                    //setLog("DWProgrss-ERROR", data.id.toString())
                }
                Reason.DOWNLOAD_BLOCK_UPDATED -> {
                    //setLog("DWProgrss-UPDATED", data.id.toString())
                }
                Reason.DOWNLOAD_WAITING_ON_NETWORK -> {
                    //setLog("DWProgrss-NETWORK", data.id.toString())
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
        setLocalBroadcast()
        //(requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(ArrayList(), this, fetchMusicDownloadListener)
        if (downloadedContentList != null && downloadedContentList?.size!! > 0){
            checkAllContentDownloadedOrNot(downloadedContentList)
        }else{
            playPauseStatusChange(true)
        }
    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        if (downloadedContentList != null && downloadedContentList?.size!! > position){
            if (isFavorite){
                downloadedContentList?.get(position)?.isFavorite = 1
                MainScope().launch(Dispatchers.Main) {
                    delay(1000)
                    downloadedAdpter?.notifyDataSetChanged()
                }
            }
            else{
                downloadedContentList?.get(position)?.isFavorite = 0
            }
//            downloadedContentList?.get(position)?.isFavorite = isFavorite
        }
    }

    override fun onContentRemovedFromDownload(isRemoved: Boolean, content: DownloadedAudio) {
        super.onContentRemovedFromDownload(isRemoved, content)
        if (isRemoved){
            GlobalScope.launch(Dispatchers.IO){
                var isCurrentPlayingContent = false
                BaseActivity.songDataList?.forEachIndexed { index, track ->
                    if (track.id.toString().equals(content.contentId)){
                        track.url = ""
                        track.image = content.image
                        CommonUtils.setLog(
                            "onContentRemovedFromDownload",
                            "track.title-${track.title} - track.image-${track.image}"
                        )
                    }
                    if (BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()){
                        if (BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id.toString().equals(content.contentId)){
                            CommonUtils.setLog(
                                "onContentRemovedFromDownload",
                                "songDataList.title-${BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.title} - songDataList.image-${
                                    BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.image
                                }"
                            )
                            isCurrentPlayingContent = true
                        }
                    }
                }
                if (isCurrentPlayingContent){
                    withContext(Dispatchers.Main){
                        if (context != null && (context is MainActivity)){
                            (context as MainActivity).playCurrentSong()
                        }
                    }
                }
            }
            MainScope().launch(Dispatchers.Main) {
                delay(1000)
                CommonUtils.setLog(
                    "DownloadedContentDetailFragment",
                    "onContentRemovedFromDownload-isAdded-$isAdded-context != null-${context != null}"
                )
                if (isAdded && context != null){
                    setUpDownloadedDetailListViewModel()
                }
            }
            /*val intent = Intent(Constant.DOWNLOADED_CONTENT_EVENT)
            intent.putExtra("EVENT", Constant.DOWNLOADED_CONTENT_RESULT_CODE)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)*/
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (downloadedContentList != null && downloadedContentList?.size!! > 0){
                    checkAllContentDownloadedOrNot(downloadedContentList)
                }else{
                    playPauseStatusChange(true)
                }
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1
    private suspend fun checkAllContentDWOrNot(downloadedSongList: ArrayList<DownloadedAudio>?):Boolean {
        try {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!downloadedSongList.isNullOrEmpty()) {
                    try {
                        downloadedSongList.forEachIndexed { index, it ->
                            if (it != null){
                                if (!isCurrentContentPlayingFromThis && !BaseActivity.songDataList.isNullOrEmpty()
                                    && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                                ) {
                                    val currentPlayingContentId =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                                    if (currentPlayingContentId?.toString()?.equals(it.contentId)!!) {
                                        it.isSelected = 1
                                        isCurrentContentPlayingFromThis = true
                                        setLog("isCurrentPlaying", "DownloadedContentDetailAdapter-checkAllContentDWOrNot-1-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex-index-$index")
                                        if (currentPlayingContentIndex >= 0){

                                        }else{
                                            lastPlayingContentIndex = index
                                        }
                                        currentPlayingContentIndex = index
                                    } else {
                                        if (it.isSelected == 1){
                                            lastPlayingContentIndex = index
                                        }
                                        it.isSelected = 0
                                    }
                                } else {
                                    if (it.isSelected == 1){
                                        lastPlayingContentIndex = index
                                    }
                                    it.isSelected = 0
                                }
                            }
                        }
                        setLog("isCurrentPlaying", "DownloadedContentDetailAdapter-checkAllContentDWOrNot-2-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
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

    private fun checkAllContentDownloadedOrNot(downloadedSongList: ArrayList<DownloadedAudio>?) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!downloadedSongList.isNullOrEmpty()) {
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
                                checkAllContentDWOrNot(downloadedSongList)
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
                                if (downloadedAdpter != null) {
                                    //playlistAdpter?.notifyDataSetChanged()
                                    setLog("isCurrentPlaying", "DownloadedContentDetailAdapter-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex")
                                    downloadedAdpter?.notifyItemChanged(lastPlayingContentIndex)
                                    downloadedAdpter?.notifyItemChanged(currentPlayingContentIndex)
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

    /*private fun checkAllContentDownloadedOrNot(downloadedSongList: ArrayList<DownloadedAudio>?) {
        if (isAdded){
            var isCurrentContentPlayingFromThis = false
            var currentPlayingIndex = -1
            var index = 0
            if (downloadedSongList != null && downloadedSongList?.size!! > 0){
                for (item in downloadedSongList.iterator()){
                    if (!isCurrentContentPlayingFromThis && !BaseActivity?.songDataList.isNullOrEmpty()
                        && BaseActivity?.songDataList?.size!! > BaseActivity?.nowPlayingCurrentIndex()) {
                        val currentPlayingContentId =
                            BaseActivity?.songDataList?.get(BaseActivity?.nowPlayingCurrentIndex())?.id
                        if (currentPlayingContentId?.toString()?.equals(item.contentId)!!) {
                            currentPlayingIndex = index
                            isCurrentContentPlayingFromThis = true
                            if (activity != null) {
                                if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                    playPauseStatusChange(false)
                                } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                    playPauseStatusChange(true)
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
                if (currentPlayingIndex > -1) {
                    if (downloadedAdpter != null) {
                        downloadedAdpter?.updateCurrentPlayingIndex(currentPlayingIndex)
                    }
                }
            }
        }
    }*/

    var isPlaying = false
    private fun playPauseStatusChange(status:Boolean){
        isPlaying = status
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            if (downloadedContentList != null && downloadedContentList?.size!! > 0){
                checkAllContentDownloadedOrNot(downloadedContentList)
            }else{
                playPauseStatusChange(true)
            }
            MainScope().launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "DownloadedContentLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        }else {
            MainScope().launch {
                if (context != null) {
                    changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
                }
            }
        }
    }
}