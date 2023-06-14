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
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.media3.common.util.Util
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment


import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.CollectionEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.utils.*
import com.hungama.music.utils.Constant.COLLECTION_DETAIL_PAGE
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.main.adapter.*
import com.hungama.music.ui.main.viewmodel.CollectionViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_choose_language.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fr_main.*
import kotlinx.android.synthetic.main.fragment_collection_details.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.HashMap


/**
 * A simple [Fragment] subclass.
 * Use the [ArtistDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CollectionDetailsFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnLocalBroadcastEventCallBack {
    var artImageUrl:String? = null
    var selectedBucketId:String? = null
    var playerType:String? = null
    private var chartDetailBgArtImageDrawable: LayerDrawable? = null

    var tabAdapter: TabAdapter? = null
    var collectionViewModel: CollectionViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    var bucketRespModel: HomeModel? = null
    var collectionRespModel = CollectionDetailModel()
    var rowList: MutableList<RowsItem?>? = null

    var topSongList: List<CollectionDetailModel.Data.Body.TSeriesSong?>? = null
    var selectedPodcast: RowsItem? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var selectedArtistSongImage = ""
    var selectedArtistSongTitle = ""
    var selectedArtistSongSubTitle = ""
    var selectedArtistSongHeading = ""
    var playableItemPosition = 0
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var artworkProminentColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection_details, container, false)
    }

    override fun initializeComponent(view: View) {
        tvActionBarHeading.text = "T- Searies Collection"
        selectedBucketId = requireArguments().getString("id").toString()

        ivBack?.setOnClickListener { v -> backPress() }
        ivBack2?.setOnClickListener { v -> backPress() }
        rlHeading.visibility = View.INVISIBLE

        scrollView.viewTreeObserver.addOnScrollChangedListener(this)

        setupArtistViewModel()
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)
        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }
    var episodeIndex = 0

    private fun playAllArtistTopSong(){
        if(topSongList?.size!! > 0 && topSongList != null){
            playableItemPosition = 0
            setUpPlayableContentListViewModel(topSongList?.get(0)?.data?.id!!)
            setEventModelDataAppLevel(topSongList?.get(0)?.data?.id!!!!,topSongList?.get(0)?.data?.title!!,collectionDetailModel?.data?.head?.data?.title!!)
        }
    }

    private fun staticToolbarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
            MainScope().launch {
                if (context != null) {
                    changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
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
        when (item.itemId){
            R.id.action_download_video ->{

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
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
        try {
            if (activity!=null&& artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl) && collectionDetailroot != null) {
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
                        val bitmap : Bitmap? = result.await()
                        val artImage = BitmapDrawable(resources, bitmap)
                        if (status){
                            if (bitmap != null){
                                Palette.from(bitmap!!).generate { palette ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        artworkProminentColor = CommonUtils.calculateAverageColor(bitmap, 1)

                                        //(activity as AppCompatActivity).window.statusBarColor = artworkProminentColor
                                        MainScope().launch {
                                            if (context != null) {
                                                CommonUtils.setLog(
                                                    "CollectionsLifecycle",
                                                    "setArtImageBg--$artworkProminentColor"
                                                )
                                                changeStatusbarcolor(artworkProminentColor)
                                            }
                                        }
                                    }
                                    //artworkProminentColor = palette?.getDominantColor(R.attr.colorPrimaryDark)!!
                                    chartDetailBgArtImageDrawable =
                                        LayerDrawable(arrayOf<Drawable>(bgColor, artImage, gradient!!))
                                    collectionDetailroot?.background = chartDetailBgArtImageDrawable
                                }

                            }

                        }
                    }catch (exp:Exception){
                        exp.printStackTrace()
                    }


                }
            }
        }catch (exp:Exception){
            exp.printStackTrace()
        }


    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setLog("onPageSelected", "Selected position:" + position)
        }
    }





    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {

    }

    private fun setupArtistViewModel() {



        if (ConnectionUtil(context).isOnline) {
            collectionViewModel = ViewModelProvider(
                this
            ).get(CollectionViewModel::class.java)
            collectionViewModel?.getCollectionDetail(requireContext(), selectedBucketId!!)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            collectionDetailModel=it?.data
                            //setLog(TAG, "isViewLoading $it")
                            collectionDetailroot.visibility=View.VISIBLE
                            setAllListDataListData(it?.data!!)
                            setDetails(it?.data!!, true)
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),recyclerLanguage, true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    var collectionDetailModel: CollectionDetailModel?=null


    private fun setDetails(it: CollectionDetailModel?, status: Boolean) {

        artImageUrl = collectionRespModel?.data?.head?.data?.image
        playerType = "15"
        if (!TextUtils.isEmpty(artImageUrl)){
            ImageLoader.loadImage(
                requireContext(),
                collectionAlbumArtImageView,
                artImageUrl!!,
                R.drawable.bg_gradient_placeholder
            )
            setArtImageBg(true)
        }else{
            ImageLoader.loadImage(
                requireContext(),
                collectionAlbumArtImageView,
                "",
                R.drawable.bg_gradient_placeholder
            )
            staticToolbarColor()
        }
        if(!TextUtils.isEmpty(it?.data?.head?.data?.title)){
            tvTitle.text = it?.data?.head?.data?.title
            tvActionBarHeading.text = it?.data?.head?.data?.title
        }else{
            tvTitle.text = ""
            tvActionBarHeading.text = ""
        }

        if(!TextUtils.isEmpty(it?.data?.head?.data?.subtitle)){
            tvPlay.text = it?.data?.head?.data?.subtitle
        }else{
            tvPlay.text = ""
        }

        val hashMap = HashMap<String,String>()
        hashMap.put(EventConstant.SOURCE_EPROPERTY,"Collection")
        hashMap.put(EventConstant.CONTENTNAME_EPROPERTY,""+it?.data?.head?.data?.title)
        setLog("TAG","login${hashMap}")
        EventManager.getInstance().sendEvent(CollectionEvent(hashMap))
    }

    fun setAllListDataListData(collectionDetailModel: CollectionDetailModel) {

        if (collectionDetailModel != null && collectionDetailModel?.data?.body != null) {
            collectionRespModel = collectionDetailModel
            setCollectionSongsData(collectionDetailModel)
            setCollectionAlbumData(collectionDetailModel)
            setCollectionMusicVideoData(collectionDetailModel)
            setCollectionMovieData(collectionDetailModel)
            setCollectionTVShowData(collectionDetailModel)
            setCollectionShortFilmsData(collectionDetailModel)


        }
    }

    fun setCollectionSongsData(collectionDetailModel: CollectionDetailModel) {
        if(collectionDetailModel?.data?.body!!.tSeriesSongs!=null&&collectionDetailModel?.data?.body!!.tSeriesSongs?.size!!>0){
            topSongList=collectionDetailModel?.data?.body?.tSeriesSongs!!
            rvTopSong.apply {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.HORIZONTAL, false)
                adapter = CollectionSongAdapter(context, collectionDetailModel?.data?.body!!.tSeriesSongs,
                    object : CollectionSongAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            selectedArtistSongImage = collectionDetailModel?.data?.body!!.tSeriesSongs?.get(childPosition)?.data?.image!!
                            selectedArtistSongTitle = collectionDetailModel?.data?.body!!.tSeriesSongs?.get(childPosition)?.data?.title!!
                            selectedArtistSongSubTitle = collectionDetailModel?.data?.body!!.tSeriesSongs?.get(childPosition)?.data?.subtitle!!
                            selectedArtistSongHeading = collectionDetailModel.data?.head?.data?.title!!
                            playableItemPosition = childPosition
                            setUpPlayableContentListViewModel(collectionDetailModel?.data?.body!!.tSeriesSongs?.get(childPosition)?.data?.id!!)
                            setEventModelDataAppLevel(collectionDetailModel?.data?.body!!.tSeriesSongs?.get(childPosition)?.data?.id!!,collectionDetailModel?.data?.body!!.tSeriesSongs?.get(childPosition)?.data?.title!!,collectionDetailModel?.data?.head?.data?.title!!)
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llTopSong.visibility=View.VISIBLE
        }else{
            llTopSong.visibility=View.GONE
        }

    }

    fun setCollectionAlbumData(collectionDetailModel: CollectionDetailModel) {
        if(collectionDetailModel.data?.body?.tSeriesAlbum!=null&&collectionDetailModel.data?.body?.tSeriesAlbum?.size!!>0) {
            tvHeaderAlbum?.text=collectionDetailModel?.data?.head?.data?.title+" "+getString(R.string.album_str_2)
            ivMore2.setOnClickListener {
                redirectToMoreBucketListPage(collectionDetailModel.data?.body?.tSeriesAlbum,getString(R.string.collection_str_1))
            }
            rvAlbum.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = CollectionAlbumAdapter(context, collectionDetailModel.data?.body?.tSeriesAlbum!!,
                    object : CollectionAlbumAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val albumDetailFragment = AlbumDetailFragment()
                            val bundle = Bundle()
                            bundle.putString("image", collectionDetailModel.data?.body?.tSeriesAlbum?.get(childPosition)?.data?.image!!)
                            bundle.putString("id", collectionDetailModel.data?.body?.tSeriesAlbum?.get(childPosition)?.data?.id)
                            bundle.putString("playerType", playerType)
                            albumDetailFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@CollectionDetailsFragment, albumDetailFragment, false)
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llAlbums.visibility=View.VISIBLE
        }else{
            llAlbums.visibility=View.GONE
        }
    }

    fun setCollectionMusicVideoData(collectionDetailModel: CollectionDetailModel) {
        if(collectionDetailModel.data?.body?.tSeriesVideos!=null&&collectionDetailModel.data?.body?.tSeriesVideos?.size!!>0) {
            ivmore3.setOnClickListener {
                redirectToMoreBucketListPage(collectionDetailModel?.data?.body?.tSeriesVideos,getString(R.string.artist_str_9))
            }
            rvMusicVideos.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = CollectionMusicVideoAdapter(context, collectionDetailModel.data?.body?.tSeriesVideos,
                    object : CollectionMusicVideoAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val songsList = CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                            val serviceBundle = Bundle()
                            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
                            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                            intent.putExtra("thumbnailImg", collectionDetailModel.data?.body?.tSeriesVideos?.get(childPosition)?.data?.image)
                            serviceBundle.putString(Constant.SELECTED_CONTENT_ID,collectionDetailModel.data?.body?.tSeriesVideos?.get(childPosition)?.data?.id)
                            serviceBundle.putInt(Constant.TYPE_ID,collectionDetailModel.data?.body?.tSeriesVideos?.get(childPosition)?.data?.type!!.toString().toInt())
                            intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
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
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llMusicVideos.visibility=View.VISIBLE
        }else{
            llMusicVideos.visibility=View.GONE
        }

    }

    fun setCollectionMovieData(collectionDetailModel: CollectionDetailModel) {
        if(collectionDetailModel.data?.body?.tSeriesMovies!=null&&collectionDetailModel.data?.body?.tSeriesMovies?.size!!>0) {
            ivMore4.setOnClickListener {
                redirectToMoreBucketListPage(collectionDetailModel?.data?.body?.tSeriesMovies,getString(R.string.artist_str_14))
            }
            rvMovies.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = CollectionMovieAdapter(context, collectionDetailModel.data?.body?.tSeriesMovies!!,
                    object : CollectionMovieAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val bundle = Bundle()
                            bundle.putString("image", collectionDetailModel.data?.body?.tSeriesMovies?.get(childPosition)?.data?.image)
                            bundle.putString("id", collectionDetailModel.data?.body?.tSeriesMovies?.get(childPosition)?.data?.id)
                            bundle.putString("playerType", ""+collectionDetailModel.data?.body?.tSeriesMovies?.get(childPosition)?.data?.type!!)
                            bundle.putBoolean("varient", true)

                            var varient = 1
                            if (!TextUtils.isEmpty(collectionDetailModel.data?.body?.tSeriesMovies?.get(childPosition)?.data?.variant)){
                                if (collectionDetailModel.data?.body?.tSeriesMovies?.get(childPosition)?.data?.variant.equals("v2", true)){
                                    varient = 2
                                }
                            }
                            val movieDetailsFragment = MovieV1Fragment(varient)
                            movieDetailsFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@CollectionDetailsFragment, movieDetailsFragment, false)
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llMovies.visibility=View.VISIBLE
        }else{
            llMovies.visibility=View.GONE
        }

    }

    fun setCollectionTVShowData(collectionDetailModel: CollectionDetailModel) {
        if(collectionDetailModel.data?.body?.tSeriesTVshows!=null&&collectionDetailModel.data?.body?.tSeriesTVshows?.size!!>0) {
            ivMore5.setOnClickListener {
                redirectToMoreBucketListPage(collectionDetailModel.data?.body?.tSeriesTVshows,getString(R.string.artist_str_10))
            }
            rvTVShows.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = CollectionTVShowAdapter(context, collectionDetailModel.data?.body?.tSeriesTVshows,
                    object : CollectionTVShowAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {

                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llTVShow.visibility=View.VISIBLE
        }else{
            llTVShow.visibility=View.GONE
        }

    }

    fun setCollectionShortFilmsData(collectionDetailModel: CollectionDetailModel) {
        if(collectionDetailModel.data?.body?.tSeriesShortFilms!=null&&collectionDetailModel.data?.body?.tSeriesShortFilms?.size!!>0) {
            ivMore6.setOnClickListener {
                redirectToMoreBucketListPage(collectionDetailModel.data?.body?.tSeriesShortFilms,getString(R.string.collection_str_2))
            }
            rvShortFilms.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = CollectionShortFilmAdapter(context, collectionDetailModel.data?.body?.tSeriesShortFilms!!,
                    object : CollectionShortFilmAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val bundle = Bundle()
                            bundle.putString("image", collectionDetailModel.data?.body?.tSeriesShortFilms?.get(childPosition)?.data?.image)
                            bundle.putString("id", collectionDetailModel.data?.body?.tSeriesShortFilms?.get(childPosition)?.data?.id)
                            bundle.putString("playerType", ""+collectionDetailModel.data?.body?.tSeriesShortFilms?.get(childPosition)?.data?.type!!)
                            bundle.putBoolean("varient", true)

                            var varient = 1
                            if (!TextUtils.isEmpty(collectionDetailModel.data?.body?.tSeriesShortFilms?.get(childPosition)?.data?.variant)){
                                if (collectionDetailModel.data?.body?.tSeriesShortFilms?.get(childPosition)?.data?.variant.equals("v2", true)){
                                    varient = 2
                                }
                            }
                            val movieDetailsFragment = MovieV1Fragment(varient)
                            movieDetailsFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@CollectionDetailsFragment, movieDetailsFragment, false)
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llShortFilms.visibility=View.VISIBLE
        }else{
            llShortFilms.visibility=View.GONE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
        MainScope().launch {
            if (context != null){
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
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
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun setPodcastEpisodeListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null ) {
            setLog("PodcastEpisode", playableContentModel?.data?.head?.headData?.id.toString())
            setPodcastSongDataList(playableContentModel, playerType, topSongList?.get(episodeIndex)?.data?.title, topSongList?.get(episodeIndex)?.data?.playble_image)
            if (episodeIndex == topSongList?.size!! - 1 || episodeIndex > Constant.MAX_PLAYLIST_SIZE){
                BaseActivity.setTrackListData(songDataList)
                setProgressBarVisible(false)
                tracksViewModel.prepareTrackPlayback(0)
            }else{
                episodeIndex++
                playAllArtistTopSong()
            }
        }
    }
    var songDataList:ArrayList<Track> = arrayListOf()
    fun setPodcastSongDataList(
        playableContentModel: PlayableContentModel,
        type: String?,
        heading: String?,
        image: String?
    ) {
        val track:Track = Track()
        track.id = playableContentModel.data.head.headData.id.toLong()
        track.title = playableContentModel.data.head.headData.title
        track.url = playableContentModel.data.head.headData.misc.url
        track.drmlicence = playableContentModel.data.head.headData.misc.downloadLink.drm.token
        track.playerType = type
        track.heading = heading
        track.image = image
        track.pType = DetailPages.COLLECTION_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        track.explicit = playableContentModel.data.head.headData.misc.explicit
        track.restrictedDownload = playableContentModel.data.head.headData.misc.restricted_download
        track.attributeCensorRating =
            playableContentModel.data.head.headData.misc.attributeCensorRating.toString()

        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        songDataList.add(track)
        setLog("SongData", BaseActivity.songDataList.toString())
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
                            setPlayableContentListData(it?.data!!)
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
            //setPodcastEpisodeList(playableContentModel)
            songDataList = arrayListOf()

            for (i in topSongList?.indices!!){
                if (playableContentModel?.data?.head?.headData?.id == topSongList?.get(i)?.data?.id){
                    setArtistContentList(playableContentModel, topSongList, playableItemPosition)
                }else if(i > playableItemPosition){
                    setArtistContentList(null, topSongList, i)
                }
            }

            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)
        }
    }

    fun setArtistContentList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<CollectionDetailModel.Data.Body.TSeriesSong?>?,
        position: Int
    ) {
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
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
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
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(selectedArtistSongHeading)){
            track.heading = selectedArtistSongHeading
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

        if (!TextUtils.isEmpty(collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.id!!)){
            track.parentId = collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.id!!
        }
        if (!TextUtils.isEmpty(collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.title!!)){
            track.pName = collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.title
        }

        if (!TextUtils.isEmpty(collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.subtitle!!)){
            track.pSubName = collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.subtitle
        }

        if (!TextUtils.isEmpty(collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.image!!)){
            track.pImage = collectionRespModel?.data?.body!!.tSeriesSongs?.get(position)?.data?.image
        }

        track.pType = DetailPages.COLLECTION_DETAIL_PAGE.value
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
        songDataList.add(track)
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            var maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_250)
            /* how much we have scrolled */
            val movement = scrollView.scrollY
            maxDistance = maxDistance
            if (movement >= maxDistance){
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.INVISIBLE
                rlHeading.visibility = View.VISIBLE
                rlHeading.setBackgroundColor(artworkProminentColor)
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
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2){
            commonThreeDotMenuItemSetup(COLLECTION_DETAIL_PAGE)
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            MainScope().launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "CollectionsLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        } else {
            MainScope().launch {
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
}