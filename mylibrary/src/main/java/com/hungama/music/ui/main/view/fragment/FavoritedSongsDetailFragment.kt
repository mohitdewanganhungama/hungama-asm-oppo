package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status


import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.adapter.FavoritedSongsDetailAdapter
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.calculateAverageColor
import com.hungama.music.utils.Constant.FAVORITED_CONTENT_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.FAVORITED_CONTENT_DETAIL_PAGE
import com.hungama.music.utils.Constant.MODULE_FAVORITE
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.pause
import com.hungama.music.utils.Constant.playing
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.*
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.headBlur
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.ivDownloadFullList
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.ivDownloadFullListActionBar
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.ivFavorite
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.ivFavoriteActionBar
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.ivPlayAll
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.ivPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.iv_banner
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.iv_collapsingImageBg
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.llDetails
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.llDetails2
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.llPlayAll
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.llPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.rlHeading
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.scrollView
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.topView
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.tvPlayAll
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.tvPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.tvSubTitle
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.tvSubTitle2
import kotlinx.android.synthetic.main.fragment_favorited_songs_detail.tvTitle
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.HashMap

class FavoritedSongsDetailFragment : BaseFragment(), TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseFragment.OnMenuItemClicked,
    BaseActivity.OnDownloadQueueItemChanged, OnUserSubscriptionUpdate,
    BaseActivity.OnLocalBroadcastEventCallBack {
    private var albumDetailBgArtImageDrawable: LayerDrawable? = null

    var favoritesSongsViewModel: UserViewModel? = null

    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()

    var favoritesSongsList:List<BookmarkDataModel.Data.Body.Row> = ArrayList()
    var ascending = true
    var favoritesSongsAdpter: FavoritedSongsDetailAdapter? = null
    var artworkProminentColor = 0
    var artworkHeight = 0
    var isFavourite = false
    var bookmarkDataModel: BookmarkDataModel? = null
    var isPlaying = false
    companion object{
        var favoritesSongsRespModel: BookmarkDataModel? = null
        var playableItemPosition = 0
    }
    override fun initializeComponent(view: View) {
        applyButtonTheme(requireContext(), llPlayAll)
        applyButtonTheme(requireContext(), llPlayAllActionBar)
        ivBack?.setOnClickListener {backPress() }
        ivBack2?.setOnClickListener {backPress() }
        rlHeading.visibility = View.INVISIBLE
        Constant.screen_name ="Favorite Songs Detail"
        scrollView.viewTreeObserver.addOnScrollChangedListener(this)

        rvAlbumlist.visibility =View.VISIBLE
        setupUserViewModel()

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)
        ivFavorite?.setOnClickListener(this)
        ivFavoriteActionBar?.setOnClickListener(this)
        llPlayAll?.setOnClickListener(this)
        llPlayAllActionBar?.setOnClickListener(this)

        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
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
        return inflater.inflate(R.layout.fragment_favorited_songs_detail, container, false)
    }

    fun setArtImageBg(artWorkImages: ArrayList<String>) {
        if (activity!=null && artWorkImages != null && artWorkImages?.size!! > 0 && albumDetailroot != null) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
            val bgImage: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_bg_two)
            val gradient: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_gradient_drawable)



            GlobalScope.launch(Dispatchers.IO) {

                try {
                    var multiImag: Bitmap? = null
                    try {
                        if (artWorkImages.size > 3) {
                            val bitmaps = ArrayList<Bitmap>()

                            for (image in artWorkImages.iterator()) {
                                val result: Deferred<Bitmap?> = GlobalScope.async {

                                    val urlImage = URL(image)
                                    urlImage.toBitmap()
                                }
                                val bitmap : Bitmap? = result.await()
                                bitmaps.add(bitmap!!)
                            }

                            multiImag = CommonUtils.mergeMultiple(bitmaps)
                        } else {
                            val result: Deferred<Bitmap?> = GlobalScope.async {

                                val urlImage = URL(artWorkImages.get(0))
                                urlImage.toBitmap()
                            }
                            val bitmap : Bitmap? = result.await()
                            multiImag = bitmap
                        }
                    }catch (e:Exception){

                    }
                    val bitmap : Bitmap? = multiImag
                    val artImage = BitmapDrawable(resources, bitmap)
                    withContext(Dispatchers.Main){
                        albumAlbumArtImageView.setImageBitmap(bitmap)

                        if (bitmap != null){
                            //val color = dynamicToolbarColor(bitmap)
                            Palette.from(bitmap!!).generate { palette ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                    //(activity as AppCompatActivity).window.statusBarColor = palette.getDominantColor(R.attr.colorPrimaryDark)
                                            if (activity != null){
                                                (activity as AppCompatActivity).window.statusBarColor = calculateAverageColor(bitmap, 1)
                                            }
                                }
                                val color2 = ColorDrawable(palette?.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))!!)
                                artworkProminentColor =
                                    palette.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
                                albumDetailBgArtImageDrawable = LayerDrawable(arrayOf<Drawable>(bgColor, color2, gradient!!))
                                albumDetailroot?.background = bgColor
                                iv_collapsingImageBg?.background = artImage
                                fullGradient?.visibility = View.VISIBLE
                            }

                        }
                    }

                }catch (e:Exception){

                }


            }
        }else {
            ImageLoader.loadImage(
                requireContext(),
                albumAlbumArtImageView,
                "",
                R.drawable.bg_gradient_placeholder
            )
            staticToolbarColor()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
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
            if(favoritesSongsList?.size!! > 0 && favoritesSongsList != null){
                playableItemPosition = 0
                setUpPlayableContentListViewModel(favoritesSongsList?.get(playableItemPosition)?.data?.id!!)
                setEventModelDataAppLevel(favoritesSongsList?.get(playableItemPosition)?.data?.id!!,favoritesSongsList?.get(
                    playableItemPosition
                )?.data?.title!!,EventConstant.SOURCE_FAV_SONG)
                playPauseStatusChange(false)

                CoroutineScope(Dispatchers.Main).launch {
                    val hashMapPageView = HashMap<String, String>()

                    hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =
                        favoritesSongsList.get(DownloadedContentDetailFragment.playableItemPosition).data
                            .title.toString()
                    hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                        "" + Utils.getContentTypeNameForStream("" + favoritesSongsList.get(
                            DownloadedContentDetailFragment.playableItemPosition).data.type)
                    hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                        favoritesSongsList.get(DownloadedContentDetailFragment.playableItemPosition).data.id.toString()
                    hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                    hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "Favorite"
                    hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "Favorite"

                    setLog("VideoPlayerPageView", hashMapPageView.toString())
                    EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                }
            }
        }else{
            (requireActivity() as MainActivity).pausePlayer()
            playPauseStatusChange(true)
        }
    }

    fun setAlbumDetailsListData(bookmarkDataModel: BookmarkDataModel) {

        if (bookmarkDataModel != null && bookmarkDataModel?.data?.body != null) {
            favoritesSongsRespModel = bookmarkDataModel
            if (bookmarkDataModel.data?.body?.rows!! != null) {
                favoritesSongsList = bookmarkDataModel.data.body.rows
                ivDownloadFullList.setOnClickListener(this)
                ivDownloadFullListActionBar.setOnClickListener(this)
                val artworkImages = ArrayList<String>()
                for (i in favoritesSongsList?.indices!!) {
                    favoritesSongsList?.get(i)?.data?.isFavorite = true
                    if (i < 4) {
                        artworkImages.add(bookmarkDataModel.data?.body?.rows?.get(i)?.data?.image.toString())
                    }
                }
                CommonUtils.getbanner(requireContext(),iv_banner,Constant.nudge_album_banner)
                setArtImageBg(artworkImages)
                rvAlbumlist.apply {
                    layoutManager =
                        GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                    setHasFixedSize(true)

                }
                setAlbumSongAdapter(ascending)
                callFavSonglistEvent(favoritesSongsList)
            }
        }
    }

    private fun callFavSonglistEvent(arrayList: List<BookmarkDataModel.Data.Body.Row?>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val list=ArrayList<String>()
            arrayList?.forEach {
                list.add(it?.data?.title!!)
            }


            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.FAVOURITED_SONG,  Utils.arrayToString(list))
            userDataMap.put(EventConstant.NUMBER_OF_FAVORITED_SONGS, ""+ arrayList?.size)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }

    }
    private fun setAlbumSongAdapter(asc: Boolean) {
        baseMainScope.launch {
            checkAllContentDownloadedOrNot(favoritesSongsList)
            favoritesSongsAdpter = FavoritedSongsDetailAdapter(
                requireContext(), favoritesSongsList,
                object : FavoritedSongsDetailAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int, isMenuClick: Boolean, isDownloadClick: Boolean) {
                        playableItemPosition = childPosition
                        if (isMenuClick){
                            if(isOnClick()) {
                                val isFavorite = favoritesSongsList?.get(childPosition)?.data?.isFavorite!!
                                commonThreeDotMenuItemSetup(FAVORITED_CONTENT_DETAIL_ADAPTER, this@FavoritedSongsDetailFragment, isFavorite)
                            }

                        }else if (isDownloadClick){
                            val dpm = DownloadPlayCheckModel()
                            dpm.contentId = favoritesSongsList?.get(childPosition)?.data?.id?.toString()!!
                            dpm.contentTitle = favoritesSongsList?.get(childPosition)?.data?.title?.toString()!!
                            dpm.planName = favoritesSongsList?.get(childPosition)?.data?.misc?.movierights.toString()
                            dpm.isAudio = true
                            dpm.isDownloadAction = true
                            dpm.isShowSubscriptionPopup = true
                            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                            dpm.restrictedDownload = RestrictedDownload.valueOf(favoritesSongsList?.get(childPosition)?.data?.misc?.restricted_download!!)
                            if (CommonUtils.userCanDownloadContent(requireContext(), albumDetailroot, dpm, this@FavoritedSongsDetailFragment,Constant.drawer_restricted_download)){
                                val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                                var dq = DownloadQueue()
                                //for (item in favoritesSongsList?.iterator()!!){

                                dq = DownloadQueue()
                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.id.toString())){
                                    dq.contentId = favoritesSongsList?.get(childPosition)?.data?.id.toString()
                                }

                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.title!!)){
                                    dq.title = favoritesSongsList?.get(childPosition)?.data?.title!!
                                }

                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.subtitle!!)){
                                    dq.subTitle = favoritesSongsList?.get(childPosition)?.data?.subtitle!!
                                }

                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.image!!)){
                                    dq.image = favoritesSongsList?.get(childPosition)?.data?.image!!
                                }

                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.misc?.movierights.toString()!!)){
                                    dq.planName = favoritesSongsList?.get(childPosition)?.data?.misc?.movierights.toString()
                                }

                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.misc?.favCount.toString()!!)){
                                    dq.f_fav_count = favoritesSongsList?.get(childPosition)?.data?.misc?.favCount.toString()
                                }

                                if (!TextUtils.isEmpty(favoritesSongsList?.get(childPosition)?.data?.misc?.playcount.toString()!!)){
                                    dq.f_playcount = favoritesSongsList?.get(childPosition)?.data?.misc?.playcount.toString()
                                }

                                /*if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.id!!)){
                                    dq.parentId = favoritesSongsRespModel?.data?.head?.data?.id!!
                                }
                                if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.title!!)){
                                    dq.pName = favoritesSongsRespModel?.data?.head?.data?.title
                                }

                                if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.subtitle!!)){
                                    dq.pSubName = favoritesSongsRespModel?.data?.head?.data?.subtitle
                                }

                                if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.releasedate!!)){
                                    dq.pReleaseDate = favoritesSongsRespModel?.data?.head?.data?.releasedate
                                }

                                if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.image!!)){
                                    dq.pImage = favoritesSongsRespModel?.data?.head?.data?.image
                                }*/
                                dq.parentId = ""
                                dq.pName = ""
                                dq.pSubName = ""
                                dq.pImage = ""
                                dq.pReleaseDate = ""
                                dq.pType = DetailPages.FAVORITE_DETAIL_PAGE.value
                                dq.contentType = ContentTypes.AUDIO.value
                                dq.source = "favourite"

                                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(favoritesSongsList?.get(childPosition)?.data?.id!!.toString())
                                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(favoritesSongsList?.get(childPosition)?.data?.id!!.toString())
                                if ((!downloadQueue?.contentId.equals(favoritesSongsList?.get(childPosition)?.data?.id!!.toString()))
                                    && (!downloadedAudio?.contentId.equals(favoritesSongsList?.get(childPosition)?.data?.id!!.toString()))){
                                    downloadQueueList.add(dq)
                                }
                                // }
                                //if (downloadQueueList.size > 0){
                                (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                                    downloadQueueList,
                                    this@FavoritedSongsDetailFragment,
                                    null,
                                    false,
                                    true
                                )
                                //}
                            }
                        }else{
                            if(isOnClick()) {
                                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                    ?.findByContentId(favoritesSongsList?.get(childPosition)?.data?.id!!.toString())
                                if (downloadedAudio != null && downloadedAudio?.contentId.equals(
                                        favoritesSongsList?.get(childPosition)?.data?.id!!.toString()
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
                                        favoritesSongsList?.get(
                                            childPosition
                                        )?.data?.id!!
                                    )
                                    setEventModelDataAppLevel(favoritesSongsList?.get(childPosition)?.data?.id!!,favoritesSongsList?.get(childPosition)?.data?.title!!,EventConstant.SOURCE_FAV_SONG)
                                }
                            }

                        }

                    }


                })
            rvAlbumlist.adapter = favoritesSongsAdpter
        }
    }

    private fun setDetails(it: BookmarkDataModel) {
        llDetails2.visibility = View.VISIBLE

        /*val drawable = FontDrawable(requireContext(), R.string.icon_liked)
        drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        ivFavorite.setImageDrawable(drawable)
        ivFavoriteActionBar.setImageDrawable(drawable)*/
        ivFavorite.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
        ivFavoriteActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))

        tvTitle.text = getString(R.string.favrited_str_1)
        tvTitle.visibility = View.VISIBLE

        tvSubTitle.visibility=View.VISIBLE
        if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserFirstname())){
            tvSubTitle.text = getString(R.string.playlist_str_2) + " " + SharedPrefHelper.getInstance().getUserFirstname()
        }else if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserLastname())){
            tvSubTitle.text = getString(R.string.playlist_str_2) + " " + SharedPrefHelper.getInstance().getUserLastname()
        }else if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getHandleName())){
            tvSubTitle.text = getString(R.string.playlist_str_2) + " " + SharedPrefHelper.getInstance().getHandleName()
        }else{
            tvSubTitle.visibility=View.GONE
        }
        tvSubTitle?.isSelected = true
        var postfix = getString(R.string.library_playlist_str_8)
        if (it.data?.body?.rows?.size == 1){
            postfix = getString(R.string.song)
        }
        tvSubTitle2.text = it.data?.body?.rows?.size.toString() + " " + postfix
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
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
        playPauseStatusChange(false)
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setUpPlayableContentListViewModel(id:String) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)

        BaseActivity.setTouchData()
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setPlayableContentListData(it?.data!!)
                                }else{
                                    playableItemPosition = playableItemPosition +1
                                    if (playableItemPosition < favoritesSongsList?.size!!) {
                                        setUpPlayableContentListViewModel(favoritesSongsList?.get(playableItemPosition)?.data?.id!!)
                                        setEventModelDataAppLevel(favoritesSongsList?.get(playableItemPosition)?.data?.id!!,favoritesSongsList?.get(
                                            playableItemPosition
                                        )?.data?.title!!,EventConstant.SOURCE_FAV_SONG)
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


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null ) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()

            for (i in favoritesSongsList?.indices!!){
                if (playableContentModel?.data?.head?.headData?.id == favoritesSongsList?.get(i)?.data?.id){
                    setFavoritedSongList(playableContentModel, favoritesSongsList, playableItemPosition)
                }else if(i > playableItemPosition){
                    setFavoritedSongList(null, favoritesSongsList, i)
                }
            }

            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)
        }
    }
    var songDataList:ArrayList<Track> = arrayListOf()
    fun setFavoritedSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<BookmarkDataModel.Data.Body.Row?>?,
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
        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.type.toString())){
            track.playerType = playableItem?.get(position)?.data?.type.toString()
        }else{
            track.playerType = MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)){
            track.heading = playableItem?.get(position)?.data?.title
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
        track.isLiked = true

        /*if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.id!!)){
            track.parentId = favoritesSongsRespModel?.data?.head?.data?.id!!
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)){
            track.pName = playableItem?.get(position)?.data?.title
        }

        if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.subtitle!!)){
            track.pSubName = favoritesSongsRespModel?.data?.head?.data?.subtitle
        }

        if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.image!!)){
            track.pImage = favoritesSongsRespModel?.data?.head?.data?.image
        }*/

        track.pType = DetailPages.FAVORITE_DETAIL_PAGE.value
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
                    headBlur.visibility = View.VISIBLE
                    rlHeading.visibility = View.VISIBLE
                    if (artworkProminentColor == 0){
                        rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                    }else{
                        rlHeading.setBackgroundColor(artworkProminentColor)
                    }
                }else{
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
        if (v == threeDotMenu || v == threeDotMenu2){
            commonThreeDotMenuItemSetup(FAVORITED_CONTENT_DETAIL_PAGE)
        }else if (v == llPlayAll || v == llPlayAllActionBar){
            if (favoritesSongsRespModel?.data?.body?.rows!!.isNotEmpty()){
                playAllAlbumList()
            }
        }
        else if (v == ivDownloadFullList || v == ivDownloadFullListActionBar){
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = "0"
            dpm.planName = PlanNames.NONE.name
            dpm.isAudio = true
            dpm.isDownloadAction = true
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_ALL_CONTENT
            dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
            if (CommonUtils.userCanDownloadContent(requireContext(), albumDetailroot, dpm, this,Constant.drawer_download_all)) {
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
                for (item in favoritesSongsList?.iterator()!!){

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

                    /*if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.id!!)){
                        dq.parentId = favoritesSongsRespModel?.data?.head?.data?.id!!
                    }
                    if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.title!!)){
                        dq.pName = favoritesSongsRespModel?.data?.head?.data?.title
                    }

                    if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.subtitle!!)){
                        dq.pSubName = favoritesSongsRespModel?.data?.head?.data?.subtitle
                    }

                    if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.releasedate!!)){
                        dq.pReleaseDate = favoritesSongsRespModel?.data?.head?.data?.releasedate
                    }

                    if (!TextUtils.isEmpty(favoritesSongsRespModel?.data?.head?.data?.image!!)){
                        dq.pImage = favoritesSongsRespModel?.data?.head?.data?.image
                    }*/

                    dq.parentId = ""
                    dq.pName = ""
                    dq.pSubName = ""
                    dq.pImage = ""
                    dq.pReleaseDate = ""
                    dq.pType = DetailPages.FAVORITE_DETAIL_PAGE.value
                    dq.contentType = ContentTypes.AUDIO.value
                    dq.downloadAll = 1
                    dq.source = "favourite"

                    if (!TextUtils.isEmpty(item?.data?.misc?.movierights.toString()!!)){
                        dq.planName = item?.data?.misc?.movierights.toString()
                    }

                    if (!TextUtils.isEmpty(item?.data?.misc?.favCount.toString()!!)){
                        dq.f_fav_count = item?.data?.misc?.favCount.toString()
                    }

                    if (!TextUtils.isEmpty(item?.data?.misc?.playcount.toString()!!)){
                        dq.f_playcount = item?.data?.misc?.playcount.toString()
                    }

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
                ivDownloadFullList.setImageDrawable(requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite))
                ivDownloadFullListActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite))

            }

        }
    }

    private fun setupUserViewModel() {
        favoritesSongsViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        getUserBookmarkedData()
    }

    private fun getUserBookmarkedData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            favoritesSongsViewModel?.getUserBookmarkedData(requireContext(), MODULE_FAVORITE, "21,36")?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null && it?.data?.data?.body != null && it?.data?.data?.body?.rows!!.size > 0) {
                                setAlbumDetailsListData(it?.data)
                                setDetails(it?.data)
                            }else{
                                /*val intent = Intent(Constant.FAVORITE_CONTENT_EVENT)
                                intent.putExtra("EVENT", Constant.FAVORITE_CONTENT_RESULT_CODE)
                                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)*/
                                backPress()
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

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        if (favoritesSongsList != null && favoritesSongsList?.size!! > 0){
            favoritesSongsList?.get(position)?.data?.isFavorite = isFavorite
            GlobalScope.launch(Dispatchers.Main) {
                setupUserViewModel()
            }
        }
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope?.launch {
            if(isAdded()){
                setLog("DWProgrss-onChangedid", data.id.toString())
                setLog("DWProgrss-onChanged", reason.toString())
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByDownloadManagerId(data.id)
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

                when(reason){
                    Reason.DOWNLOAD_ADDED -> {
                        setLog("DWProgrss-ADDED", data.id.toString())
                    }
                    Reason.DOWNLOAD_QUEUED ->{
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-QUEUED", data.id.toString())
                            if (favoritesSongsAdpter != null){
                                if (downloadQueue != null){
                                    val index = favoritesSongsList?.indexOfFirst{
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        favoritesSongsAdpter?.notifyItemChanged(index)
                                    }
                                }else if (downloadedAudio != null){
                                    val index = favoritesSongsList?.indexOfFirst{
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        favoritesSongsAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
                        }

                    }
                    Reason.DOWNLOAD_STARTED->{
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-STARTED", data.id.toString())
                            if (favoritesSongsAdpter != null){
                                if (downloadQueue != null){
                                    val index = favoritesSongsList?.indexOfFirst{
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        favoritesSongsAdpter?.notifyItemChanged(index)
                                    }
                                }else if (downloadedAudio != null){
                                    val index = favoritesSongsList?.indexOfFirst{
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        favoritesSongsAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
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
                        withContext(Dispatchers.Main){
                            setLog("DWProgrss-COMPLETED", data.id.toString())
                            if (favoritesSongsAdpter != null){
                                if (downloadQueue != null){
                                    val index = favoritesSongsList?.indexOfFirst{
                                        it?.data?.id == downloadQueue.contentId
                                    }
                                    if (index != null) {
                                        favoritesSongsAdpter?.notifyItemChanged(index)
                                    }
                                }else if (downloadedAudio != null){
                                    val index = favoritesSongsList?.indexOfFirst{
                                        it?.data?.id == downloadedAudio.contentId!!
                                    }
                                    if (index != null) {
                                        favoritesSongsAdpter?.notifyItemChanged(index)
                                    }
                                }
                            }
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
        if (favoritesSongsList != null && favoritesSongsList?.size!! > 0){
            checkAllContentDownloadedOrNot(favoritesSongsList)
        }else{
            playPauseStatusChange(true)
        }
    }

    private fun checkAllContentDownloadedOrNot(favoritesSongsList: List<BookmarkDataModel.Data.Body.Row?>?) {
        if (isAdded){
            var isAllDownloaded = false
            var isAllDownloadInQueue = false
            var isCurrentContentPlayingFromThis = false
            if (favoritesSongsList != null && favoritesSongsList?.size!! > 0){
                if (favoritesSongsRespModel != null){
                    for (item in favoritesSongsList.iterator()){
                        val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.getDownloadQueueItemsByContentIdAndContentType(
                            item?.data?.id.toString(), ContentTypes.AUDIO.value)
                        if (downloadQueue?.contentId.equals(item?.data?.id)){
                            if (downloadQueue?.downloadAll == 1){
                                isAllDownloadInQueue = true
                                break
                            }else{
                                isAllDownloadInQueue = false
                            }
                        }
                        if (!isCurrentContentPlayingFromThis && !BaseActivity?.songDataList.isNullOrEmpty()
                            && BaseActivity?.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()) {
                            val currentPlayingContentId =
                                BaseActivity?.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                            if (currentPlayingContentId?.toString()?.equals(item?.data?.id)!!) {
                                item?.data?.isCurrentPlaying = true
                                isCurrentContentPlayingFromThis = true
                                if (activity != null){
                                    if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == playing) {
                                    playPauseStatusChange(false)
                                } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == pause) {
                                    playPauseStatusChange(true)
                                } else {
                                    playPauseStatusChange(true)
                                }
                                }
                            } else {
                                item?.data?.isCurrentPlaying = false
                                playPauseStatusChange(true)
                            }
                        }else{
                            item?.data?.isCurrentPlaying = false
                        }
                    }
                    if (isCurrentContentPlayingFromThis) {
                        if (favoritesSongsAdpter != null) {
                            favoritesSongsAdpter?.notifyDataSetChanged()
                        }
                    }
                    for (item in favoritesSongsList.iterator()){
                        val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.getDownloadedAudioItemsByContentIdAndContentType(
                            item?.data?.id.toString(), ContentTypes.AUDIO.value)

                        if (downloadedAudio?.contentId.equals(item?.data?.id)){
                            isAllDownloaded = true
                        }else{
                            isAllDownloaded = false
                            break
                        }
                    }
                }
            }
            if (isAllDownloadInQueue){
                /* val drawable = FontDrawable(requireContext(), R.string.icon_downloading)
                 drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                 ivDownloadFullList.setImageDrawable(drawable)
                 ivDownloadFullListActionBar.setImageDrawable(drawable)*/
                ivDownloadFullList.setImageDrawable(requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite))
                ivDownloadFullListActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_downloading, R.color.colorWhite))
            }else{
                if (isAllDownloaded){
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_downloaded2)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivDownloadFullList.setImageDrawable(drawable)
                    ivDownloadFullListActionBar.setImageDrawable(drawable)*/
                    ivDownloadFullList.setImageDrawable(requireContext().faDrawable(R.string.icon_downloaded2, R.color.colorWhite))
                    ivDownloadFullListActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_downloaded2, R.color.colorWhite))
                }else{
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_download)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivDownloadFullList.setImageDrawable(drawable)
                    ivDownloadFullListActionBar.setImageDrawable(drawable)*/
                    ivDownloadFullList.setImageDrawable(requireContext().faDrawable(R.string.icon_download, R.color.colorWhite))
                    ivDownloadFullListActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_download, R.color.colorWhite))
                }
            }
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
                if (favoritesSongsList != null && favoritesSongsList?.size!! > 0){
                    checkAllContentDownloadedOrNot(favoritesSongsList)
                }else{
                    playPauseStatusChange(true)
                }
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

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
            if (favoritesSongsList != null && favoritesSongsList?.size!! > 0){
                checkAllContentDownloadedOrNot(favoritesSongsList)
            }else{
                playPauseStatusChange(true)
            }
            MainScope().launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "FavoritedSongsDetailLifecycle",
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