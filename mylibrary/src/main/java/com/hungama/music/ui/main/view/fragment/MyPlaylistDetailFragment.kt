package com.hungama.music.ui.main.view.fragment

import  android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.google.gson.Gson
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.utils.preference.SharedPrefHelper
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
import com.hungama.music.data.database.oldappdata.DBOHandler
import com.hungama.music.data.model.OnUserSubscriptionUpdate
import com.hungama.music.ui.main.adapter.DetailPlaylistAdapter
import com.hungama.music.ui.main.adapter.RecommendedSongsAdapter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.AddedToPlaylistEvent
import com.hungama.music.eventanalytic.eventreporter.FavouritedEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.utils.*
import com.hungama.music.utils.Constant.MODULE_FAVORITE
import com.hungama.music.utils.Constant.MUSIC_PLAYER
import com.hungama.music.utils.Constant.MY_PLAYLIST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.MY_PLAYLIST_DETAIL_PAGE
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.applyButtonTheme1
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fr_my_playlist_detail.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URL

class MyPlaylistDetailFragment(val varient: Int,val listener : onBackPreesHendel) : BaseFragment(), TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnDownloadQueueItemChanged,
    OnUserSubscriptionUpdate, BaseFragment.OnMenuItemClicked,
    BaseActivity.OnLocalBroadcastEventCallBack, TextView.OnEditorActionListener {
    var artImageUrl: String? = null
    var imageArray: ArrayList<String>? = null
    var variantImages: ArrayList<String>? = null

    //var artImageUrl = "https://files.hubhopper.com/podcast/313123/storytime-with-gurudev-sri-sri-ravi-shankar.jpg?v=1598432706&s=hungama"
    var selectedContentId: String? = null
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
    var addSongItemPosition = 0
    var userViewModel: UserViewModel? = null
    var bookmarkDataModel: BookmarkDataModel? = null
    var playlistDetailModelItem: PlaylistModel? = null
    var isFavourite = false
    var isMyRecommended = false
    var addSongCount =0

    var CALL_FROM = 101 //101-> playlist song 102->recommended song

    var currentUserId = ""
    var playlistUserId = ""
    var isEdit = false
    var isPlaying = false
    lateinit var touchHelper: ItemTouchHelper

    var isFirstTimeRecommenndationApiCall = true
    var similarData : RecommendedSongListRespModel.Data.Body.Similar? = null

    companion object {
        var playlistRespModel: PlaylistModel? = null
        var myplaylistSongItem: PlaylistModel.Data.Body.Row? = null
        var playableItemPosition = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        return inflater.inflate(R.layout.fr_my_playlist_detail, container, false)

    }

    override fun initializeComponent(view: View) {
        artImageUrl = requireArguments().getString("image").toString()
        recommendedSongList = ArrayList()
        Constant.screen_name ="My Playlist Detail"
        if (requireArguments()?.containsKey("variant_images") == true) {
            variantImages = requireArguments().getStringArrayList("variant_images")
            if (variantImages != null && variantImages?.size!! > 0) {
                artImageUrl = variantImages?.get(0)
            }
        }

        selectedContentId = requireArguments().getString("id").toString()
        playerType = requireArguments().getString("playerType").toString()

        CommonUtils.getbanner(requireContext(),iv_banner,Constant.nudge_playlist_banner)

        ivBack?.setOnClickListener {
            listener.backPressItem(true)

            if (playlistSongList.isNotEmpty()) {
                CommonUtils.PageViewEvent(
                    "", "", "", "",
                    "details_" + playlistSongList[0].data.title.replace(" ", "").lowercase(),
                    MainActivity.lastItemClicked + "_" + MainActivity.headerItemName,
                    MainActivity.headerItemPosition.toString())
            }
            backPress()
        }
        llToolbar.visibility = View.INVISIBLE

        scrollView.viewTreeObserver.addOnScrollChangedListener(this)


        setUpPlaylistDetailListViewModel()

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        applyButtonTheme(requireContext(), llAddSongs)
        llAddSongs?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", selectedContentId)
            bundle.putString("playerType", playerType)
            bundle.putString("playlistName", etName?.text.toString())

            val addSongInPlayListFragment = AddSongInPlayListFragment()
            addSongInPlayListFragment.arguments = bundle

            addFragment(
                R.id.fl_container,
                this@MyPlaylistDetailFragment,
                addSongInPlayListFragment,
                false
            )

        }
        applyButtonTheme(requireContext(), llAddSongsActionBar)
        llAddSongsActionBar?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", selectedContentId)
            bundle.putString("playerType", playerType)
            bundle.putString("playlistName", etName?.text.toString())

            val addSongInPlayListFragment = AddSongInPlayListFragment()
            addSongInPlayListFragment.arguments = bundle

            addFragment(
                R.id.fl_container,
                this@MyPlaylistDetailFragment,
                addSongInPlayListFragment,
                false
            )
        }
        applyButtonTheme1(requireContext(), llPlayAll)
        llPlayAll?.setOnClickListener(this)
        applyButtonTheme1(requireContext(), llPlayAllActionBar)
        llPlayAllActionBar?.setOnClickListener(this)
        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)
        ivFavorite?.setOnClickListener(this)
        ivFavoriteActionBar?.setOnClickListener(this)
        ivDownloadFullList?.setOnClickListener(this)
        ivDownloadFullListActionBar?.setOnClickListener(this)
        etName?.setOnEditorActionListener(this)
        CommonUtils.setPageBottomSpacing(
            scrollView,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
        btnCancelEdit?.setOnClickListener(this)
        btnSaveEdit?.setOnClickListener(this)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        setLog(TAG, "onHiddenChanged: ")
        if (!hidden && isAdded) {
            baseMainScope.launch {
                setUpPlaylistDetailListViewModel()
            }
        }
    }

    var isFourImageSet = false
    fun setArtImageBg(artWorkImages: ArrayList<String>) {
        if (activity != null && artWorkImages != null && artWorkImages?.size!! > 0 && playlistDetailroot != null) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))

            baseIOScope.launch {

                try {
                    var multiImag: Bitmap? = null
                    try {
                        //setLog("isFourImageSet", "isFourImageSet-1-$isFourImageSet")
                        if (artWorkImages.size > 3) {
                            if (!isFourImageSet){
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
                                //setLog("isFourImageSet", "isFourImageSet-2-$isFourImageSet")
                                isFourImageSet = true
                            }
                        } else {
                            val imgFile = File(artWorkImages.get(0))
                            if (imgFile.exists()) {
                                val myBitmap: Bitmap =
                                    BitmapFactory.decodeFile(imgFile.absolutePath)
                                multiImag = myBitmap
                            } else {
                                val result: Deferred<Bitmap?> = baseIOScope.async {
                                    val urlImage = URL(artWorkImages.get(0))
                                    urlImage.toBitmap()
                                }
                                val bitmap: Bitmap? = result.await()
                                multiImag = bitmap
                            }
                            //setLog("isFourImageSet", "isFourImageSet-3-$isFourImageSet")
                            isFourImageSet = false
                        }
                    } catch (e: Exception) {

                    }
                        val bitmap: Bitmap? = multiImag
                    //setLog("isFourImageSet", "isFourImageSet-4-$isFourImageSet-bitmap-$bitmap")
                    if (bitmap != null){
                        withContext(Dispatchers.Main){
                            playlistAlbumArtImageView.setImageBitmap(bitmap)
                            val artImage = BitmapDrawable(resources, bitmap)

                            if (bitmap != null) {
                                //val color = dynamicToolbarColor(bitmap)
                                Palette.from(bitmap!!).generate { palette ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                        //(activity as AppCompatActivity).window.statusBarColor = palette.getDominantColor(R.attr.colorPrimaryDark)
                                        if (activity != null){
                                            (activity as AppCompatActivity).window.statusBarColor =
                                                CommonUtils.calculateAverageColor(bitmap, 1)
                                        }
                                    }
                                    artworkProminentColor =
                                        palette?.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))!!
                                    playlistDetailroot?.background = bgColor
                                    iv_collapsingImageBg?.background = artImage
                                    fullGradient?.visibility = View.VISIBLE
                                    //setLog("isFourImageSet", "isFourImageSet-5-$isFourImageSet")
                                }

                            }
                        }
                    }

                } catch (e: Exception) {

                }


            }
        } else {
            ImageLoader.loadImage(
                requireContext(),
                playlistAlbumArtImageView,
                "",
                R.drawable.playlist_bg_image
            )
            staticToolbarColor()
        }

    }

    fun setArtImageBg(status: Boolean) {

        if (activity != null && isAdded && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                artImageUrl
            ) && playlistDetailroot != null
        ) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
            val result: Deferred<Bitmap?> = GlobalScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }

            baseIOScope.launch {

                try {
                    // get the downloaded bitmap
                    val bitmap: Bitmap? = result.await()

                    val artImage = BitmapDrawable(resources, bitmap)
                    if (status) {
                        if (bitmap != null && isAdded) {
                            //val color = dynamicToolbarColor(bitmap)
                            Palette.from(bitmap!!).generate { palette ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                    /* (activity as AppCompatActivity).window.statusBarColor =
                                         palette?.getDominantColor(R.attr.colorPrimaryDark)!!*/
                                    if (activity != null) {
                                        (activity as AppCompatActivity).window.statusBarColor =
                                            CommonUtils.calculateAverageColor(bitmap, 1)
                                    }
                                }
                                val color2 =
                                    ColorDrawable(palette?.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))!!)
                                artworkProminentColor =
                                    palette.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
                                playlistDetailroot?.background = bgColor
                                if (varient == 1) {
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
                                    val viewWidthToBitmapWidthRatio =
                                        iv_collapsingImageBg?.getWidth()
                                            ?.toDouble()!! / bitmap.getWidth().toDouble()
                                    artworkHeight =
                                        ((bitmap.getHeight() * viewWidthToBitmapWidthRatio).toInt())
                                    iv_collapsingImageBg?.getLayoutParams()?.height = artworkHeight
                                    centerGradient?.getLayoutParams()?.height = artworkHeight
                                    /*ImageLoader.loadImage(
                                        requireContext(),
                                        iv_collapsingImageBg,
                                        artImageUrl!!,
                                        R.drawable.bg_gradient_placeholder
                                    )*/
                                    iv_collapsingImageBg?.background = artImage
                                    Utils.setMarginsTop(centerGradient, (artworkHeight / 2))
                                    Utils.setMarginsTop(llDetails, (artworkHeight / 2))
                                    centerGradient?.visibility = View.VISIBLE
                                    //iv_collapsingImageBg.background = BitmapDrawable(resources, resizeImageForImageView(bitmap,scaleSize))
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
            (activity as AppCompatActivity).window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.home_bg_color)
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

    private fun setUpPlaylistDetailListViewModel() {
        playlistListViewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)

        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {

            playlistListViewModel?.getMyPlaylistDetailList(requireContext(), selectedContentId!!)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                //setLog(TAG, "isViewLoading $it")
                                baseMainScope.launch {
                                        setProgressBarVisible(false)
                                        if (isAdded){

                                            if (isFirstTimeRecommenndationApiCall) {
                                                callRecommendationAPI(selectedContentId!!)
                                                isFirstTimeRecommenndationApiCall = false
                                            }

                                            playlistDetailroot?.visibility = View.VISIBLE
                                            setPlaylistDetailsListData(it?.data!!)
                                            setDetails(it?.data!!, true)
                                        }
                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
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
                            }
                        }
                    })

        } else{
            loadOfflinePlaylist()
        }

    }

    var recommendedSongList: ArrayList<RecommendedSongListRespModel.Data.Body.Similar>? = null
    var recommendedSongsAdapter: RecommendedSongsAdapter? = null


    private fun addSongIntoMyPlaylist(similar: RecommendedSongListRespModel.Data.Body.Similar) {
        baseMainScope.launch {
                val modelItem = PlaylistModel.Data.Body.Row()
                modelItem.itype = similar.itype
                modelItem.data?.image = similar.data.image
                modelItem.data?.duration = similar.data.duration
                modelItem.data?.id = similar.data.id
                modelItem.data?.title = similar.data.title
                modelItem.data?.subtitle = similar.data.subtitle
                modelItem.data?.type = similar.data.type
                modelItem.data?.misc?.artist = similar.data.misc.artist
                modelItem.data?.misc?.attributeCensorRating =
                    similar.data.misc.attributeCensorRating
                modelItem.data?.misc?.favCount = similar.data.misc.favCount
                modelItem.data?.misc?.playcount = similar.data.misc.playcount
                modelItem.data?.misc?.lang = similar.data.misc.lang
                modelItem.data?.misc?.explicit = similar.data.misc.explicit
                modelItem.data?.misc?.description = similar.data.misc.description
                setLog("TAG", "refreshData: before playlistSongList size:${playlistSongList?.size}")
                playlistSongList?.add(modelItem)
                if (playlistAdpter != null) {
                    playlistAdpter?.refreshData(playlistSongList, isEdit)
                    if (playlistSongList.size == 4) {
                        setLog(TAG, "onAddSongClick: " + playlistSongList.size)

                        val artworkImages = ArrayList<String>()
                        for (i in playlistSongList?.indices!!) {
                            if (i < 4) {
                                artworkImages.add(playlistSongList?.get(i)?.data?.image.toString())
                            }
                        }

                        setArtImageBg(artworkImages)

                    } else {
                        playlistAdpter?.refreshData(playlistSongList, isEdit)
                    }
                } else {
                    setPlayListSongAdapter(ascending)
                }
                if (!playlistSongList.isNullOrEmpty() && playlistSongList?.size!! >= 1) {
                    llDetails3?.visibility = View.VISIBLE
                    llPlaySongsActionBar?.visibility = View.VISIBLE
                    llDetails2?.visibility = View.INVISIBLE
                    tvSubTitle3?.visibility = View.GONE
                    llAddSongsActionBar?.visibility = View.GONE
                }
                updateSubTitle(playlistDetailModelItem)

            CoroutineScope(Dispatchers.Main).launch {

                setLog("CheckPageViewSwip", MainActivity.lastItemClicked + " " +
                        MainActivity.headerItemName + " " + MainActivity.tempLastItemClicked + "\n" +
                        Gson().toJson(playlistSongList[0]))

                CommonUtils.PageViewEvent(
                    playlistSongList[0].data.id,
                    playlistSongList[0].data.title,
                    Utils.getContentTypeNameForStream(playlistSongList[0].data.type.toString()),
                    MainActivity.lastItemClicked,
                    "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + MainActivity.subHeaderItemName,
                    "details_" + "PlayList", MainActivity.headerItemPosition.toString())
                MainActivity.subHeaderItemName = ""
            }
        }

        //setLog("TAG", "refreshData: before playlistSongList size:${playlistSongList?.size}")
//        setPlayListSongAdapter(ascending)
    }


    private fun callAddSong(model: RecommendedSongListRespModel.Data.Body.Similar) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    val jsonObject = JSONObject()
                    jsonObject.put("contentid", model?.data?.id)
                    playlistListViewModel?.addSong(requireContext(), jsonObject, selectedContentId!!)
                        ?.observe(this@MyPlaylistDetailFragment,
                            Observer {
                                when (it.status) {
                                    Status.SUCCESS -> {
                                        baseMainScope.launch {
                                            try {
                                                setProgressBarVisible(false)
                                                if (it?.data != null) {
                                                    if (!recommendedSongList.isNullOrEmpty() && addSongItemPosition <= recommendedSongList?.size!!) {
                                                        baseMainScope.launch {
                                                            try {
                                                                val hashMap = HashMap<String, String>()
                                                                val model = HungamaMusicApp.getInstance()
                                                                    .getEventData(recommendedSongList?.get(addSongItemPosition)?.data?.id!!)
                                                                hashMap.put(
                                                                    EventConstant.ACTOR_EPROPERTY,
                                                                    Utils.arrayToString(
                                                                        recommendedSongList?.get(addSongItemPosition)?.data?.misc?.actorf!!
                                                                    )
                                                                )
                                                                var newContentId= recommendedSongList?.get(addSongItemPosition)?.data?.id!!
                                                                var contentIdData=newContentId.replace("playlist-","")
                                                                hashMap.put(EventConstant.CONTENTID_EPROPERTY, contentIdData
                                                                )
                                                                hashMap.put(
                                                                    EventConstant.CONTENTNAME_EPROPERTY,
                                                                    recommendedSongList?.get(addSongItemPosition)?.data?.title!!
                                                                )

                                                                if (recommendedSongList?.get(addSongItemPosition)?.data?.type.toString().equals("21")){
                                                                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,"Song")
                                                                }else{
                                                                    hashMap.put(
                                                                        EventConstant.CONTENTTYPE_EPROPERTY,
                                                                        "" + Utils.getContentTypeName(
                                                                            "" + recommendedSongList?.get(addSongItemPosition)?.data?.type!!
                                                                        )
                                                                    )
                                                                }
                                                                hashMap.put(
                                                                    EventConstant.GENRE_EPROPERTY,
                                                                    Utils.arrayToString(
                                                                        recommendedSongList?.get(addSongItemPosition)?.data?.genre!!
                                                                    )
                                                                )
                                                                hashMap.put(
                                                                    EventConstant.LANGUAGE_EPROPERTY,
                                                                    Utils.arrayToString(
                                                                        recommendedSongList?.get(addSongItemPosition)?.data?.misc?.lang!!
                                                                    )
                                                                )
                                                                hashMap.put(
                                                                    EventConstant.LYRICIST_EPROPERTY,
                                                                    Utils.arrayToString(
                                                                        recommendedSongList?.get(addSongItemPosition)?.data?.misc?.lyricist!!
                                                                    )
                                                                )
                                                                hashMap.put(
                                                                    EventConstant.MOOD_EPROPERTY,
                                                                    "" + recommendedSongList?.get(addSongItemPosition)?.data?.misc?.mood
                                                                )
                                                                if (recommendedSongList?.get(addSongItemPosition)?.data?.misc?.musicdirectorf != null && recommendedSongList?.get(
                                                                        addSongItemPosition
                                                                    )?.data?.misc?.musicdirectorf?.size!! > 0
                                                                ) {
                                                                    hashMap.put(
                                                                        EventConstant.MUSICDIRECTOR_EPROPERTY,
                                                                        Utils.arrayToString(
                                                                            recommendedSongList?.get(addSongItemPosition)?.data?.misc?.musicdirectorf!!
                                                                        )
                                                                    )
                                                                } else {
                                                                    if (!TextUtils.isEmpty(model?.musicDirectorComposer)) {
                                                                        hashMap.put(
                                                                            EventConstant.MUSICDIRECTOR_EPROPERTY,
                                                                            "" + model?.musicDirectorComposer
                                                                        )
                                                                    }

                                                                }

//                                hashMap.put(EventConstant.PLAYLISTNAME_EPROPERTY,""+etName.setText())
                                                                hashMap.put(EventConstant.PODCASTNAME_EPROPERTY, "")
                                                                hashMap.put(
                                                                    EventConstant.PODCASTHOST_EPROPERTY,
                                                                    "" + model?.podcast_host
                                                                )
                                                                hashMap.put(
                                                                    EventConstant.SINGER_EPROPERTY,
                                                                    Utils.arrayToString(
                                                                        recommendedSongList?.get(addSongItemPosition)?.data?.misc?.singerf!!
                                                                    )
                                                                )

                                                                if (recommendedSongList?.get(addSongItemPosition)?.data?.misc?.tempo != null && recommendedSongList?.get(
                                                                        addSongItemPosition
                                                                    )?.data?.misc?.tempo?.size!! > 0
                                                                ) {
                                                                    hashMap.put(
                                                                        EventConstant.TEMPO_EPROPERTY,
                                                                        Utils.arrayToString(
                                                                            recommendedSongList?.get(addSongItemPosition)?.data?.misc?.tempo!!
                                                                        )
                                                                    )
                                                                } else {
                                                                    if (!TextUtils.isEmpty(model?.tempo)) {
                                                                        hashMap.put(
                                                                            EventConstant.TEMPO_EPROPERTY,
                                                                            "" + model?.tempo
                                                                        )
                                                                    }

                                                                }

                                                                if (recommendedSongList?.get(addSongItemPosition)?.data?.releasedate != null && !TextUtils.isEmpty(
                                                                        recommendedSongList?.get(addSongItemPosition)?.data?.releasedate
                                                                    )
                                                                ) {
                                                                    hashMap.put(
                                                                        EventConstant.YEAROFRELEASE_EPROPERTY,
                                                                        "" + DateUtils.convertDate(
                                                                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                                                                            DateUtils.DATE_YYYY,
                                                                            recommendedSongList?.get(addSongItemPosition)?.data?.releasedate!!
                                                                        )
                                                                    )
                                                                } else {
                                                                    if (!TextUtils.isEmpty(model?.release_Date)) {
                                                                        hashMap.put(
                                                                            EventConstant.YEAROFRELEASE_EPROPERTY,
                                                                            "" + model?.release_Date
                                                                        )
                                                                    }

                                                                }



                                                                EventManager.getInstance()
                                                                    .sendEvent(AddedToPlaylistEvent(hashMap))
                                                            }catch (e:Exception){

                                                            }
                                                        }

                                                        baseMainScope.launch {
                                                            try {
                                                                if (similarData != null) {
                                                                    addSongIntoMyPlaylist(
                                                                        similarData!!
                                                                    )
                                                                    recommendedSongList?.remove(similarData)
                                                                }
                                                                /*rvRecomandedSong?.adapter?.notifyItemRemoved(
                                                                    addSongItemPosition
                                                                )*/
                                                                recommendedSongsAdapter?.notifyItemRemoved(
                                                                    addSongItemPosition
                                                                )
                                                                //recommendedSongsAdapter?.refreshList(recommendedSongList!!)
                                                            }catch (e:Exception){

                                                            }

                                                        }
                                                        val messageModel = MessageModel(
                                                            getString(R.string.toast_str_42),
                                                            getString(R.string.toast_str_43),
                                                            MessageType.NEUTRAL,
                                                            true
                                                        )
                                                        CommonUtils.showToast(requireContext(), messageModel)
                                                    }
                                                }
                                            }catch (e:Exception){

                                            }
                                        }
                                    }

                                    Status.LOADING -> {
                                        setProgressBarVisible(true)
                                    }

                                    Status.ERROR -> {
                                        setProgressBarVisible(false)
                                        Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                                    }
                                }
                            })
                }
            }catch (e:Exception){

            }
        }
    }

    fun setPlaylistDetailsListData(playlistModel: PlaylistModel) {

        if (playlistModel != null && playlistModel?.data?.body != null) {
            playlistRespModel = playlistModel

            currentUserId = SharedPrefHelper?.getInstance()?.getUserId().toString()
            playlistUserId = playlistRespModel?.data?.head?.data?.uid.toString()
            //playlistUserId = "633041910"
            if (currentUserId.equals(playlistUserId, true)
                && (playlistModel.data?.body?.rows.isNullOrEmpty()
                        || playlistModel.data?.body?.rows?.size!! < 10)
            ) {
                setLog(
                    TAG,
                    "setPlaylistDetailsListData 1 currentUserId:${currentUserId} playlistUserId:${playlistUserId}"
                )
                setLog(TAG, "CONTENT ID = " +selectedContentId )


                rlPlaylistList.setPadding(0, 0, 0, 0)
            } else {
                setLog(
                    TAG,
                    "setPlaylistDetailsListData 2 currentUserId:${currentUserId} playlistUserId:${playlistUserId}"
                )
                rlPlaylistList.setPadding(
                    0,
                    0,
                    0,
                    resources.getDimensionPixelSize(R.dimen.dimen_120)
                )
                rlRecomandedList?.visibility = View.GONE
            }
            setupUserViewModel()
            setLog("MyPlaylistDetailFragment", "setPlaylistDetailsListData-playlistModel.data.body.rows.size-${playlistModel.data.body.rows.size}")
            if (!playlistModel.data.body.rows.isNullOrEmpty()) {
                playlistSongList = playlistModel.data.body.rows
                checkAllContentDownloadedOrNot(playlistSongList)
            }
            setPlayListSongAdapter(ascending)

        }
    }

    var recommendedListViewModel: PlaylistViewModel? = null

    fun getRecommendedContentList(id1:String, id2:String, id3:String) {
        if (ConnectionUtil(requireContext()).isOnline) {
            recommendedListViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
            recommendedListViewModel?.getRecommendedContentListMyPlayList(requireContext(), id1,id2,id3)?.observe(this)
            {
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it?.data != null) {
                            if (recommendedSongList != null)
                                recommendedSongList!!.clear()

                            for (item in it.data.data.body.rows) {

                                val misc = RecommendedSongListRespModel.Data.Body.Similar.Data.Misc()
                                misc.artist = item.data.misc.artist
                                misc.actorf = item.data.misc.actorf
                                misc.singerf = item.data.misc.singerf
                                misc.attributeCensorRating = item.data.misc.attributeCensorRating
                                misc.description = item.data.misc.description
                                misc.explicit = item.data.misc.explicit
                                misc.favCount = item.data.misc.favCount
                                misc.lang = item.data.misc.lang
                                misc.musicdirectorf = item.data.misc.musicdirectorf
                                misc.lyricist = item.data.misc.lyricistf
                                misc.mood = item.data.misc.mood
                                misc.movierights = item.data.misc.movierights
                                misc.nudity = item.data.misc.nudity
                                misc.pid = item.data.misc.pid
                                misc.pName = item.data.misc.p_name
                                misc.tempo = item.data.misc.tempo
                                misc.playcount = item.data.misc.playcount
                                misc.ratingCritic = item.data.misc.ratingCritic.toInt()
                                misc.sArtist = item.data.misc.sArtist
                                misc.synopsis = item.data.misc.synopsis
                                misc.vendor = item.data.misc.vendor
                                misc.restricted_download = item.data.misc.restricted_download

                                val data = RecommendedSongListRespModel.Data.Body.Similar.Data()
                                data.duration = item.data.duration
                                data.genre = item.data.genre
                                data.id = item.data.id
                                data.image = item.data.image
                                data.playble_image = item.data.playble_image
                                data.misc = misc
                                data.releasedate = item.data.releasedate
                                data.subtitle = item.data.subtitle
                                data.title = item.data.title
                                data.type = item.data.type

                                val similar = RecommendedSongListRespModel.Data.Body.Similar()
                                similar.data = data
                                similar.itype = item.itype

                                recommendedSongList!!.add(similar)

                            }
                            recommendedSongsAdapter?.refreshList(recommendedSongList!!)
                            recommendedSongsAdapter?.notifyDataSetChanged()

                        }
                    }

                    Status.LOADING -> {

                    }

                    Status.ERROR -> {
                        val messageModel = it.message?.let { it1 ->
                            MessageModel("", it1, MessageType.NEGATIVE, true)
                        }
                        if (messageModel != null) {
                            CommonUtils.showToast(requireContext(), messageModel)
                        }
                        updateRecommandedSong()
                    }
                }
            }
        }
    }

    private fun callRecommendationAPI(page:String) {
        playlistListViewModel?.getRecommendedSongList(requireContext(), page)
            ?.observe(this
            ) {
                when (it.status) {
                    Status.SUCCESS -> {
                        baseMainScope.launch {
                            try {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    recommendedSongList =
                                        ArrayList<RecommendedSongListRespModel.Data.Body.Similar>()
                                    setLog(
                                        TAG,
                                        "setPlaylistDetailsListData 3 recommendedListRespObserver"
                                    )
                                    //setLog(TAG, "isViewLoading $it")
                                    playlistDetailroot.visibility = View.VISIBLE

                                    if (it?.data?.data?.body?.similar != null && it?.data?.data?.body?.similar?.size!! > 0) {

                                        recommendedSongList?.addAll(it.data.data.body.similar)

                                        setLog(
                                            TAG,
                                            "setPlaylistDetailsListData 4 recommendedSongList${recommendedSongList?.size}"
                                        )


                                        if (recommendedSongsAdapter == null && requireActivity() != null && recommendedSongList?.size!! > 0) {
                                            recommendedSongsAdapter = RecommendedSongsAdapter(
                                                requireActivity(),
                                                recommendedSongList!!,
                                                object :
                                                    RecommendedSongsAdapter.OnChildItemClick {
                                                    override fun onPlaySongClick(childPosition: Int) {
                                                        CALL_FROM = 102
                                                        playableItemPosition = childPosition
                                                        //setUpPlayableContentListViewModel(recommendedSongList?.get(childPosition)?.data?.id!!)
                                                        //setEventModelDataAppLevel(recommendedSongList?.get(childPosition)?.data?.id!!,recommendedSongList?.get(childPosition)?.data?.title!!,playlistDetailModelItem?.data?.head?.data?.title!!)
                                                    }

                                                    override fun onAddSongClick(childPosition: Int) {
                                                        if (SharedPrefHelper.getInstance().isUserLoggedIn()) {

                                                        baseMainScope.launch {
                                                            if ((!recommendedSongList.isNullOrEmpty() && recommendedSongList?.size!! > childPosition
                                                                        && playlistSongList.isNullOrEmpty())
                                                                || (!playlistSongList.isNullOrEmpty() && playlistSongList?.size!! <= 10)
                                                            ) {

                                                                addSongItemPosition = childPosition
                                                                if (!recommendedSongList.isNullOrEmpty() && recommendedSongList?.size!! > addSongItemPosition){
                                                                    callAddSong(recommendedSongList?.get(addSongItemPosition)!!)
                                                                    similarData = recommendedSongList?.get(addSongItemPosition)!!

                                                                }
                                                                addSongCount += 1

                                                                if (addSongCount >= 3 && playlistSongList.size>=3){
                                                                    getRecommendedContentList(playlistSongList[playlistSongList.size-3].data.id, playlistSongList[playlistSongList.size-2].data.id, playlistSongList[playlistSongList.size-1].data.id)
                                                                }
                                                                if (playlistSongList.size >= 10) {
                                                                    rlRecomandedList.visibility =
                                                                        View.GONE
                                                                    rvRecomandedSong.visibility =
                                                                        View.GONE
                                                                }
                                                            }
                                                        }
                                                        if ((!recommendedSongList.isNullOrEmpty() && recommendedSongList?.size!! > childPosition) && playlistSongList.size < 10)
                                                        {
                                                            updateRecommandedSong()
                                                        }
                                                        }
                                                    }
                                                })
                                            if ((!recommendedSongList.isNullOrEmpty() && recommendedSongList?.size!! > 0
                                                        && playlistSongList.size < 10))
                                            {
                                                updateRecommandedSong()
                                            }

                                        } else {
                                            setLog(
                                                TAG,
                                                "setPlaylistDetailsListData recommendedSongsAdapter already craeted: "
                                            )
                                            recommendedSongsAdapter?.refreshList(
                                                recommendedSongList!!
                                            )
                                            recommendedSongsAdapter?.notifyDataSetChanged()
                                        }

                                        setLog(
                                            TAG,
                                            "setPlaylistDetailsListData 5 recommendedSongList${recommendedSongList?.size}"
                                        )

                                    } else {
                                        setLog(
                                            TAG,
                                            "setPlaylistDetailsListData 6 recommendedSongList${recommendedSongList?.size}"
                                        )
                                    }
                                }
                            }catch (e:Exception){

                            }
                        }
                    }

                    Status.LOADING -> {
                        setProgressBarVisible(true)
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
                    }
                }
            }
    }

    private fun updateRecommandedSong() {
        /*Handler(Looper.getMainLooper()).postDelayed(Runnable {
            rlRecomandedList.visibility = View.VISIBLE
            rvRecomandedSong.visibility=View.VISIBLE
            if (recommendedSongsAdapter != null){
                rvRecomandedSong.adapter=recommendedSongsAdapter
                rvRecomandedSong.setPadding(0, 0, 0, 0)
                rvRecomandedSong.layoutManager=LinearLayoutManager(requireActivity())

                setLog(TAG, "setPlaylistDetailsListData recommendedSongsAdapter is craeted: ${rvRecomandedSong?.adapter}")
                recommendedSongsAdapter?.refreshList(recommendedSongList!!)
                recommendedSongsAdapter?.notifyDataSetChanged()

                rvRecomandedSong.invalidate()
            }
        },1000)*/

        baseMainScope.launch {
            if (context != null && view != null){
                rlRecomandedList?.visibility = View.VISIBLE
                rvRecomandedSong?.visibility = View.VISIBLE
                if (recommendedSongsAdapter != null) {
                    rvRecomandedSong?.adapter = recommendedSongsAdapter
                    rvRecomandedSong?.setPadding(0, 0, 0, 0)
                    rvRecomandedSong?.layoutManager = LinearLayoutManager(requireActivity())

                    //recommendedSongsAdapter?.refreshList(recommendedSongList!!)
                    //recommendedSongsAdapter?.notifyDataSetChanged()
                    //rvRecomandedSong.invalidate()
                }
            }
        }
    }

    private fun setPlayListSongAdapter(asc: Boolean) {
        baseMainScope.launch {
                if (isAdded){
                    if (playlistSongList?.size!! >= 1 && currentUserId.equals(playlistUserId, true)) {
                        llDetails3?.visibility = View.VISIBLE
                        llPlaySongsActionBar?.visibility = View.VISIBLE
                        llDetails2?.visibility = View.INVISIBLE
                        llAddSongsActionBar?.visibility = View.GONE
                        tvSubTitle3?.visibility = View.GONE

                        setLog(TAG, "setPlayListSongAdapter: 1")
                    } else if (!currentUserId.equals(playlistUserId, true)) {
                        if(!playlistSongList.isNullOrEmpty()){
                            llDetails3?.visibility = View.VISIBLE
                            llPlaySongsActionBar?.visibility = View.VISIBLE
                        }else{
                            llDetails3?.visibility = View.GONE
                            llPlaySongsActionBar?.visibility = View.GONE
                        }
                        llDetails2?.visibility = View.INVISIBLE
                        llAddSongsActionBar?.visibility = View.GONE
                        tvSubTitle3?.visibility = View.GONE
                        setLog(TAG, "setPlayListSongAdapter: 2")
                    } else {
                        llDetails3?.visibility = View.GONE
                        llPlaySongsActionBar?.visibility = View.GONE
                        llDetails2?.visibility = View.VISIBLE
                        llAddSongsActionBar?.visibility = View.VISIBLE
                        tvSubTitle3?.visibility = View.VISIBLE
                        setLog(TAG, "setPlayListSongAdapter: 3")
                    }
                    playlistAdpter = DetailPlaylistAdapter(
                        requireContext(), playlistSongList,
                        object : DetailPlaylistAdapter.OnChildItemClick {
                            override fun onUserClick(childPosition: Int, isMenuClick: Boolean,
                                isDownloadClick: Boolean) {
                                playableItemPosition = childPosition
                                myplaylistSongItem = playlistSongList?.get(childPosition)
                                if (isMenuClick) {
                                    if (isOnClick()) {
                                        val isFavorite = playlistSongList?.get(childPosition)?.data?.isFavorite!!
                                        commonThreeDotMenuItemSetup(
                                            MY_PLAYLIST_DETAIL_ADAPTER,
                                            this@MyPlaylistDetailFragment,isFavorite)
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
                                            this@MyPlaylistDetailFragment,Constant.drawer_download_all
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

                                        dq.pType = DetailPages.MY_PLAYLIST_DETAIL_PAGE.value
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
                                            this@MyPlaylistDetailFragment,
                                            null,
                                            false,
                                            true
                                        )
                                        //}
                                    }
                                }
                                else {
                                    CALL_FROM = 101
                                    if (isOnClick()) {
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

                                            Log.d("sarvesh", "else")

                                            setUpPlayableContentListViewModel(
                                                playlistSongList?.get(
                                                    childPosition
                                                )?.data?.id!!
                                            )
                                            setEventModelDataAppLevel(
                                                playlistSongList?.get(
                                                    childPosition
                                                )?.data?.id!!,
                                                playlistSongList?.get(
                                                    childPosition
                                                )?.data?.title!!,
                                                playlistDetailModelItem?.data?.head?.data?.title!!
                                            )
                                        }
                                    }
                                }
                            }
                            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                                touchHelper?.startDrag(viewHolder)
                            }

                            override fun onContentRemoveClick(
                                childPosition: Int,
                                contentId: String
                            ) {
                                super.onContentRemoveClick(childPosition, contentId)
                                selectedContentId?.let {
                                    setDeleteMyPlaylistContent(contentId, "",
                                        it
                                    )
                                    if (!playlistSongList.isNullOrEmpty() && playlistSongList.size > childPosition){
                                        playlistSongList.removeAt(childPosition)
                                        //playlistAdpter?.notifyItemRemoved(childPosition)
                                        playlistAdpter?.refreshData(playlistSongList, isEdit)
                                    }
                                }
                            }
                        })
                    var mLayoutManager = LinearLayoutManager(
                        activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    rvPlaylist?.layoutManager = mLayoutManager
                    rvPlaylist.adapter = playlistAdpter
                    rvPlaylist.setPadding(0, 0, 0, 0)
                    rvPlaylist?.invalidate()
                    rvPlaylist.visibility = View.VISIBLE
                }
        }

    }

    private fun setDetails(it: PlaylistModel?, status: Boolean) {
        baseMainScope.launch {
            try {
                playlistDetailModelItem = it
                if (isAdded && context != null){
                    if (varient == 1) {
                        if (it?.data?.body?.rows?.size!! > 0) {
                            artImageUrl = it?.data?.body?.rows?.get(0)?.data?.image!!

                            val artworkImages = ArrayList<String>()
                            for (i in it?.data?.body?.rows?.indices!!) {
                                if (i < 4) {
                                    artworkImages.add(it?.data?.body?.rows?.get(i)?.data?.image.toString())
                                }
                            }
                            setArtImageBg(artworkImages)

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
                                playlistAlbumArtImageViewLayer.visibility = View.GONE
                            }
                            setArtImageBg(true)
                        } else {
                            ImageLoader.loadImage(
                                requireContext(),
                                playlistAlbumArtImageView,
                                "",
                                R.drawable.playlist_bg_image
                            )
                            staticToolbarColor()
                        }
                    }
                    else {
                        if (it?.data?.body?.rows?.size!! > 0) {
                            playlistAlbumArtImageView.visibility = View.GONE
                            setArtImageBg(true)
                        } else {
                            ImageLoader.loadImage(
                                requireContext(),
                                playlistAlbumArtImageView,
                                "",
                                R.drawable.playlist_bg_image
                            )
                            staticToolbarColor()
                        }
                    }
                    ivEditPlaylist.setOnClickListener {
                        editUserPlaylistName()
                    }
                    if (it?.data?.head?.data?.title != null && !TextUtils.isEmpty(it?.data?.head?.data?.title)) {
                        etName.setText(it?.data?.head?.data?.title)

                        setLog("laknglalk", Gson().toJson(it?.data?.head?.data))

                        CoroutineScope(Dispatchers.IO).async {
                            val dataMap = HashMap<String, String>()

                            dataMap[EventConstant.CONTENT_TYPE_EPROPERTY] = Utils.getContentTypeName(it?.data?.head?.data?.type.toString())
                            dataMap[EventConstant.CONTENTNAME_EPROPERTY] = "playlist"
                            dataMap[EventConstant.SOURCEPAGETYPE_EPROPERTY] = Utils.getContentTypeName(it?.data?.head?.data?.type.toString())
                            val newContentId= it?.data?.head?.data?.id
                            val contentIdData:String
                            if(newContentId?.contains("playlist-") == true){
                                contentIdData=newContentId.replace("playlist-","")
                            }else{
                                contentIdData= newContentId?.replace("artist-","").toString()
                            }

                            dataMap[EventConstant.CONTENTID_EPROPERTY] = contentIdData
                            dataMap[EventConstant.SOURCEPAGE_EPROPERTY] = MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + MainActivity.subHeaderItemName
                            dataMap[EventConstant.PAGE_NAME_EPROPERTY] = "details_playList_" + Utils.getContentTypeName(it?.data?.head?.data?.type.toString()).replace(" ","").lowercase()

                            setLog("lanlgbljalghlas", dataMap.toString())

                            EventManager.getInstance().sendEvent(PageViewEvent(dataMap))
                        }



                    } else {
                        etName.setText("")
                    }

                    if (it?.data?.head?.data?.handlename != null && !TextUtils.isEmpty(it?.data?.head?.data?.handlename)) {
                        tvSubTitle.text = "By " + it?.data?.head?.data?.handlename
                    } else {
                        tvSubTitle.text = ""
                        tvSubTitle?.visibility = View.GONE
                    }


                    updateSubTitle(it)

                    if (it?.data?.head?.data?.uid?.equals(
                            SharedPrefHelper.getInstance().getUserId()
                        )!!
                    ) {
                        ivEditPlaylist?.visibility = View.VISIBLE
                        threeDotMenu?.visibility = View.VISIBLE
                        threeDotMenu2?.visibility = View.VISIBLE
                    } else {
                        ivEditPlaylist?.visibility = View.GONE
                        threeDotMenu?.visibility = View.GONE
                        threeDotMenu2?.visibility = View.GONE
                    }
                }
            }catch (e:Exception){

            }
        }
    }

    fun updateSubTitle(it: PlaylistModel?){
        var subtitle = ""
        if (!TextUtils.isEmpty(it?.data?.head?.data?.playcount.toString())) {
            subtitle += it?.data?.head?.data?.playcount.toString() + " " + getString(R.string.artist_str_4) + " • "
        } else {
            subtitle += "0" + " " + getString(R.string.artist_str_4) + " • "
        }
        if (playlistSongList?.size!! > 0) {
            subtitle += CommonUtils.ratingWithSuffix("" + playlistSongList?.size) + " " + getString(
                R.string.library_playlist_str_8
            )
        } else {
            subtitle += "0" + " " + getString(R.string.library_playlist_str_8)
        }
        tvSubTitle2.text = subtitle
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
        baseMainScope.launch {
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
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setUpPlayableContentListViewModel(id: String) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {

                                    setPlayableContentListData(it?.data)
                                    callPlaylistCountApi()
                                } else {
                                    playableItemPosition = playableItemPosition + 1
                                    if (!playlistSongList.isNullOrEmpty() && playableItemPosition < playlistSongList?.size!!) {
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
                                            it.data.data.head.headData.title
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

        if (playableContentModel != null) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()

            if (CALL_FROM == 101) {
                for (i in playlistSongList?.indices!!) {
                    if (playableContentModel?.data?.head?.headData?.id == playlistSongList?.get(i)?.data?.id) {
                        setPlaylistSongList(
                            playableContentModel,
                            playlistSongList,
                            playableItemPosition
                        )
                    } else if (i > playableItemPosition) {
                        setPlaylistSongList(null, playlistSongList, i)
                    }
                }
            } else if (CALL_FROM == 102) {
                for (i in recommendedSongList?.indices!!) {
                    if (playableContentModel?.data?.head?.headData?.id == recommendedSongList?.get(i)?.data?.id) {
                        setRecomendedSongList(
                            playableContentModel,
                            recommendedSongList!!,
                            playableItemPosition
                        )
                    } else if (i > playableItemPosition) {
                        setRecomendedSongList(null, recommendedSongList!!, i)
                    }
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

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlaylistSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row?>?,
        position: Int
    ): ArrayList<Track> {
        if (!playableItem.isNullOrEmpty() && playableItem.size > position){
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
            if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
                track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
            }else{
                track.movierights = ""
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

        track.pType = DetailPages.MY_PLAYLIST_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null) {
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null) {
            track.restrictedDownload = playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableItem[position]?.data?.misc?.attributeCensorRating.toString()
        }

         //   track.gold_flag = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.goldFlag!!

            songDataList.add(track)
        }
        return songDataList
    }

    fun setRecomendedSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>,
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
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playerType)) {
            track.playerType = playerType
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

        track.pType = DetailPages.MY_PLAYLIST_DETAIL_PAGE.value
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

        songDataList.add(track)
        return songDataList
    }

    override fun onScrollChanged() {
        baseMainScope.launch {
            try {
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
                        headBlur.visibility = View.VISIBLE
                        llToolbar.visibility = View.VISIBLE
                        rlHeading.setBackgroundColor(artworkProminentColor)
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
            }catch (e:Exception){

            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == threeDotMenu || v == threeDotMenu2) {
            var isPublic = false
            if (playlistRespModel?.data?.head?.data?.public == true){
                isPublic = true
            }
            commonThreeDotMenuItemSetup(MY_PLAYLIST_DETAIL_PAGE, this, isPublic)
        } else if (v == llPlayAll || v == llPlayAllActionBar) {
            if (!playlistSongList.isNullOrEmpty()) {

                playAllPlaylist()
                BaseActivity.setTouchData()
            }
        } else if (v == ivFavorite || v == ivFavoriteActionBar) {
            playlistRespModel?.let { setAddOrRemoveFavourite(it) }
        } else if (v == ivDownloadFullList || v == ivDownloadFullListActionBar) {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = "0"
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

                    dq.pType = DetailPages.MY_PLAYLIST_DETAIL_PAGE.value
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
        }else if (v == btnCancelEdit){
            onEditCall(false)
        }else if (v == btnSaveEdit){
            savePlaylist()
        }
    }

    private fun playAllPlaylist() {
        baseMainScope.launch {
            if (isPlaying) {
                if (!playlistSongList.isNullOrEmpty()) {
                    playableItemPosition = 0
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(playlistSongList.get(0).data.id)
                    if (downloadedAudio != null && downloadedAudio.contentId.equals(
                            playlistSongList.get(0).data.id))
                    {
                        val playableContentModel = PlayableContentModel()
                        playableContentModel.data.head.headData.id =
                            downloadedAudio.contentId!!
                        playableContentModel.data.head.headData.misc.url =
                            downloadedAudio.downloadedFilePath
                        playableContentModel.data.head.headData.misc.downloadLink.drm.url =
                            downloadedAudio.downloadUrl!!
                        playableContentModel.data.head.headData.misc.downloadLink.drm.token =
                            downloadedAudio.drmLicense
                        playableContentModel.data.head.headData.misc.sl.lyric?.link =
                            downloadedAudio.lyricsUrl


                            setPlayableContentListData(playableContentModel)


                        setProgressBarVisible(false)
                    } else {
                        setProgressBarVisible(true)
                        setUpPlayableContentListViewModel(playlistSongList.get(0).data.id)
                    }

                    setEventModelDataAppLevel(
                        playlistSongList.get(0).data.id, playlistSongList.get(0).data.title,
                        playlistRespModel?.data?.head?.data?.title!!
                    )
                }
            }else {
                (requireActivity() as MainActivity).pausePlayer()
                playPauseStatusChange(true)
            }
        }
    }

    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getUserBookmarkedData()
    }

    private fun getUserBookmarkedData() {
        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            userViewModel?.getUserBookmarkedData(requireContext(), MODULE_FAVORITE)?.observe(this,
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
        if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0) {
            for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!) {
                if (playlistRespModel?.data?.head?.data?.id?.equals(bookmark?.data?.id)!!) {
                    isFavourite = true
                }
                if (!playlistSongList.isNullOrEmpty()){
                    playlistSongList?.forEachIndexed { index, row ->
                        if (row?.data?.id?.equals(bookmark?.data?.id)!!){
                            row?.data?.isFavorite = true
                        }
                    }
                }
            }
            setFollowingStatus()
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
                    withContext(Dispatchers.Main){
                        setLog("DWProgrss-QUEUED", data.id.toString())
                        if (playlistAdpter != null) {
                            if (downloadQueue != null) {
                                val index = playlistSongList.indexOfFirst {
                                    it?.data?.id == downloadQueue.contentId
                                }
                                if (index != null) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            } else if (downloadedAudio != null) {
                                val index = playlistSongList.indexOfFirst {
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
                        if (playlistAdpter != null) {
                            if (downloadQueue != null) {
                                val index = playlistSongList?.indexOfFirst {
                                    it?.data?.id == downloadQueue.contentId
                                }
                                if (index != null) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            } else if (downloadedAudio != null) {
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
                        if (playlistAdpter != null) {
                            if (downloadQueue != null) {
                                val index = playlistSongList?.indexOfFirst {
                                    it?.data?.id == downloadQueue.contentId
                                }
                                if (index != null) {
                                    playlistAdpter?.notifyItemChanged(index)
                                }
                            } else if (downloadedAudio != null) {
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

    override fun onResume() {
        super.onResume()

        setLog(TAG, "onResume: ")
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )

        if (!playlistSongList.isNullOrEmpty()) {
            checkAllContentDownloadedOrNot(playlistSongList)
        }else {
            playPauseStatusChange(true)
        }
        setLocalBroadcast()
    }



    private fun setAddOrRemoveFavourite(playlistRespModel: PlaylistModel) {
        setLog("setAddOrRemoveFavourite","setAddOrRemoveFavourite playlistRespModel:${playlistRespModel}")
        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            isFavourite = !isFavourite
            val jsonObject = JSONObject()
            jsonObject.put("contentId", playlistRespModel?.data?.head?.data?.id!!)
            jsonObject.put("typeId", 99999)
            jsonObject.put("action", isFavourite)
            jsonObject.put("module", MODULE_FAVORITE)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()

            if (isFavourite) {
                val messageModel = MessageModel(getString(R.string.album_str_3), getString(R.string.album_str_4),
                    MessageType.NEUTRAL, true)
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
                if (!TextUtils.isEmpty(playlistRespModel?.data?.head?.data?.releasedate)) {
                    hashMap.put(
                        EventConstant.YEAROFRELEASE_EPROPERTY,
                        "" + DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_YYYY,
                            playlistRespModel?.data?.head?.data?.releasedate
                        )
                    )
                } else {
                    hashMap.put(
                        EventConstant.YEAROFRELEASE_EPROPERTY,
                        "" + playlistRespModel?.data?.head?.data?.releasedate
                    )
                }

                EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
            }
            else{
                val messageModel = MessageModel(getString(R.string.popup_str_83), getString(R.string.popup_str_84),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }


        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setFollowingStatus() {
        if (isFavourite) {
            //ivFavorite.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
            //ivFavoriteActionBar.setImageDrawable(requireContext().faDrawable(R.string.icon_liked, R.color.colorWhite))
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

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        if (playlistSongList != null && playlistSongList?.size!! > 0){
            playlistSongList.get(position)?.data?.isFavorite = isFavorite
        }
    }

    private suspend fun checkAllContentDWOrNot(playlistSongList: List<PlaylistModel.Data.Body.Row?>?):Boolean {
        try {
            if (isAdded && context != null){
                var isAllDownloaded = false
                var isAllDW = true
                var isAllDownloadInQueue = false
                var isCurrentContentPlayingFromThis = false
                if (!playlistSongList.isNullOrEmpty()) {
                    try {
                        playlistSongList.forEach {
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
                                    } else {
                                        it.data.isCurrentPlaying = false
                                        playPauseStatusChange(true)
                                    }
                                } else {
                                    it.data.isCurrentPlaying = false
                                }
                            }
                        }
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

    fun setIsAllDownloadImage(isAllDownloadInQueue:Boolean, isAllDownloaded:Boolean){
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
                if (playlistSongList != null && playlistSongList?.size!! > 0) {
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
                                    playlistAdpter?.notifyDataSetChanged()
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

    private fun playPauseStatusChange(status: Boolean) {
        isPlaying = status
        baseMainScope.launch {
            if (isAdded && context != null) {
                var color = R.color.colorWhite
                if (varient == 1) {
                    color = R.color.colorWhite
                } else {
                    color = R.color.colorBlack
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

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    override fun onMyPlaylistDeletedFromThreeDotMenu(isDeleted: Boolean) {
        super.onMyPlaylistDeletedFromThreeDotMenu(isDeleted)
        if (isDeleted) {
            /*val intent = Intent(Constant.MYPLAYLIST_EVENT)
            intent.putExtra("EVENT", Constant.MYPLAYLIST_RESULT_CODE)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)*/
            backPress()
        }
    }

    override fun onMyPlaylistContentDeletedFromThreeDotMenu(isDeleted: Boolean, position: Int) {
        super.onMyPlaylistContentDeletedFromThreeDotMenu(isDeleted, position)
        if (!playlistSongList.isNullOrEmpty() && playlistSongList?.size!! > position) {
            setLog("TAG", "refreshData1: before list size:${playlistSongList?.size}")

            /*val iterator: MutableIterator<PlaylistModel.Data.Body.Row?> = playlistSongList?.iterator()!! // it will return iterator
            var index = 0
            while (iterator.hasNext()) {
                val track = iterator.next()
                if (position == index){
                    iterator.remove()
                }
                index++
            }*/

            playlistSongList?.removeAt(position)
            playlistAdpter?.notifyItemRemoved(position)
            playlistAdpter?.notifyItemRangeChanged(position, playlistSongList?.size!!)
            playlistAdpter?.refreshData(playlistSongList, isEdit)
            setLog("TAG", "refreshData2: after list size:${playlistSongList?.size}")

            updateSubTitle(playlistDetailModelItem)

            //playlistAdpter?.notifyDataSetChanged()

        }

        if (!playlistRespModel?.data?.body?.rows.isNullOrEmpty()) {
            //playlistRespModel?.data?.body?.rows?.removeAt(position)
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
                if (playlistSongList != null && playlistSongList?.size!! > 0) {
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
    interface onBackPreesHendel{
        fun backPressItem(status: Boolean)
    }

    override fun onEditMyPlaylistFromThreeDotMenu(isEdit: Boolean) {
        super.onEditMyPlaylistFromThreeDotMenu(isEdit)
        onEditCall(isEdit)
    }

    class DragManageAdapter(
        val context: Context, adapter: DetailPlaylistAdapter) : ItemTouchHelper.Callback() {
        var playlistAdpter = adapter
        var targetPosition = 0
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            setLog("onDrrag", "MoveFlag")
            return makeMovementFlags(dragFlags, 0)
        }
        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }
        override fun isLongPressDragEnabled(): Boolean {
            return false
        }
        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            setLog("onDrrag", "Move-${viewHolder.adapterPosition}-${target.adapterPosition}")
            targetPosition = target.adapterPosition
            return true
        }
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is DetailPlaylistAdapter.IType1000ViewHolder) {
                    playlistAdpter.onItemSelected(viewHolder)
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            setLog("onDrrag", "clear-${viewHolder.adapterPosition}-${targetPosition}")
            playlistAdpter.onItemClear(viewHolder)
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            setLog("onDrrag", "Swiped")
        }

        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            setLog("onDrrag", "Movedd-${fromPos}-${toPos}")
            targetPosition = toPos
            playlistAdpter.swapItems(fromPos, toPos)
        }

    }

    private fun editUserPlaylistName(){
        etName.requestFocus()
        etName.setSelection(etName.length())
        val imm: InputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun onEditCall(isEdit:Boolean){
        this.isEdit = isEdit
        if (isEdit && playlistAdpter != null){
            // Setup ItemTouchHelper
            val callback = DragManageAdapter(
                requireActivity(),
                playlistAdpter!!
            )
            touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(rvPlaylist)
            playlistAdpter?.refreshData(playlistSongList, isEdit)
            hideBottomNavigationAndMiniplayer()
            Handler(Looper.getMainLooper()).postDelayed({
                editUserPlaylistName()
                clEditBtn?.show()
            },1000)
        }else{
            playlistAdpter?.refreshData(playlistSongList, isEdit)
            clEditBtn?.hide()
            etName?.clearFocus()
            hideKeyboard()
            showBottomNavigationAndMiniplayer()
        }
    }

    private fun savePlaylist(){
        onEditCall(false)
        setMakePlaylistPrivateOrPublic(selectedContentId.toString(), etName?.text.toString().trim(), playlistRespModel?.data?.head?.data?.public!!)
        updatePlaylistContentReordered()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        setLog("actionSave", "On keyboard done click11")
        if (actionId == EditorInfo.IME_ACTION_DONE){
            savePlaylist()
            setLog("actionSave", "On keyboard done click")
            return true
        }
        return false
    }

    private fun callPlaylistCountApi(){
        playlistListViewModel?.setPlaylistCountData(requireContext(), selectedContentId!!)
    }

    fun stringToWords(s : String) = s.trim().splitToSequence(' ')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    fun loadOfflinePlaylist(){
        var playListoffline = AppDatabase.getInstance()?.downloadedAudio()
            ?.getUserPlayList(selectedContentId.toString()) as ArrayList<DownloadedAudio>

        val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()?.
        getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
        val dbDataold = DBOHandler.getAllCachedTracks(requireContext())

        Log.e("alkhsglaghlah", Gson().toJson(dbDataold))

        if(playListoffline.isNullOrEmpty() && !downloadedSongTotal.isNullOrEmpty()){
            val playOfflineData :ArrayList<DownloadedAudio> = ArrayList()


            val  oldDown = dbDataold
            val  offlineDown = downloadedSongTotal

            for (oldI in 0 until oldDown.size){
                val jsonObject = JSONObject(dbDataold[oldI].jsonDetails)
                if (selectedContentId == if (jsonObject.has("myPlaylistID")) jsonObject.getString("myPlaylistID") else "") {
                    for (off in 0 until offlineDown.size){

                        val jsonObj = JSONObject(jsonObject.getJSONObject("response").toString())
                        if (offlineDown[off].contentId == jsonObj.getString("content_id")){

                            val item =  downloadedSongTotal[off]
                            val downloadedAudio = DownloadedAudio()
                            downloadedAudio.contentId = item.contentId
                            downloadedAudio.title = item.title
                            downloadedAudio.subTitle = item.subTitle
                            downloadedAudio.type = item.type
                            downloadedAudio.image = item.image
                            downloadedAudio.thumbnailPath = item.thumbnailPath
                            downloadedAudio.releaseDate = item.releaseDate
                            downloadedAudio.genre = item.genre
                            downloadedAudio.duration = item.duration
                            downloadedAudio.explicit = item.explicit
                            downloadedAudio.restrictedDownload = item.restrictedDownload
                            downloadedAudio.pid = item.pid
                            downloadedAudio.pName = item.pName
                            downloadedAudio.singer = item.singer
                            downloadedAudio.actor = item.actor
                            downloadedAudio.artist = item.artist
                            downloadedAudio.attribute_censor_rating = item.attribute_censor_rating
                            downloadedAudio.description = item.description
                            downloadedAudio.f_fav_count = item.f_fav_count
                            downloadedAudio.f_playcount = item.f_playcount
                            downloadedAudio.language = item.language
                            downloadedAudio.movierights = item.movierights
                            downloadedAudio.nudity = item.nudity
                            downloadedAudio.criticRating = item.criticRating
                            downloadedAudio.s_artist = item.s_artist
                            downloadedAudio.synopsis = item.synopsis
                            downloadedAudio.contentShareLink = item.contentShareLink
                            downloadedAudio.restrictedDownload = item.restrictedDownload
                            downloadedAudio.contentShareLink = item.contentShareLink
                            downloadedAudio.downloadUrl = item.downloadedFilePath

                            playOfflineData.add(downloadedAudio)
                            setLog("alkhsglalhg", " " + offlineDown[off].title.toString())
                        }
                    }
                }
            }

            playListoffline = playOfflineData
        }


        val playlistDynamicModel = PlaylistModel()
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
                    row.data.misc.playcount = ""+da.f_playcount!!
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
        playlistRespModel = playlistDynamicModel
        playlistDetailroot.visibility = View.VISIBLE
        setPlaylistDetailsListData(playlistDynamicModel)
        setDetails(playlistDynamicModel, true)
    }

    private fun updatePlaylistContentReordered(){
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            if (!playlistSongList.isNullOrEmpty()){
                val productIdsJsonArray = JSONArray()
                playlistSongList.forEach {
                    productIdsJsonArray.put(it.data.id)
                }
                jsonObject.put("contentid", productIdsJsonArray)
                setLog("updatePlaylistContentReordered", "jsonObject-$jsonObject")
                if (playlistListViewModel == null){
                    playlistListViewModel = ViewModelProvider(
                        this
                    ).get(PlaylistViewModel::class.java)
                }

                playlistListViewModel?.updatePlaylistReorderedData(requireContext(), jsonObject, selectedContentId.toString())
            }

        }
    }
}