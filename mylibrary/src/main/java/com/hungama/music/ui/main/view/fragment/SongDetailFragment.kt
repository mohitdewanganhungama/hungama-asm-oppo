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
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.google.gson.Gson
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.ui.main.adapter.SongDetailFeaturePlaylistAdapter
import com.hungama.music.ui.main.adapter.SongDetailFeaturedArtistAdapter
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.utils.*
import com.hungama.music.utils.Constant.SONG_DETAIL_PAGE
import com.hungama.music.ui.main.viewmodel.SongDetailsViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.defaultContentId
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_song_detail.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.HashMap


/**
 * A simple [Fragment] subclass.
 * Use the [SongDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongDetailFragment : BaseFragment(), TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack ,BaseFragment.OnMenuItemClicked,
    OnParentItemClickListener, BucketParentAdapter.OnMoreItemClick {
    var artImageUrl:String? = null
    var selectedContentId:String = ""
    var playerType:String? = null
    var isContentAutoPlay:Int = 0
    private var albumDetailBgArtImageDrawable: LayerDrawable? = null
    var songDetailsViewModel: SongDetailsViewModel? = null
    var artworkProminentColor = 0
    var userViewModel: UserViewModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var isFromVerticalPlayer = false
    companion object{
        var songDetailModel: SongDetailModel?=null
        var playableItemPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_detail, container, false)
    }

    override fun initializeComponent(view: View) {
        if (arguments != null){
            if(requireArguments().containsKey(defaultContentId)){
                selectedContentId = requireArguments().getString(defaultContentId).toString()
                setLog(TAG, "initializeComponent: selectedContentId "+selectedContentId)
            }
            if(requireArguments().containsKey(Constant.isFromVerticalPlayer)){
                isFromVerticalPlayer = requireArguments().getBoolean(Constant.isFromVerticalPlayer)
                setLog(TAG, "initializeComponent: selectedContentId "+selectedContentId)
            }

            if (arguments?.containsKey(Constant.isPlay) == true){
                isContentAutoPlay = arguments?.getInt(Constant.isPlay)!!
            }
        }
        setSongDetailViewModel()

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        toolbarTextAppernce()
        ivBack?.setOnClickListener { view -> backPress() }

        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)

        if (isContentAutoPlay == 1 && !TextUtils.isEmpty(selectedContentId)) {
            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                ?.findByContentId(selectedContentId)
            if (downloadedAudio != null && downloadedAudio?.contentId.equals(selectedContentId)
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
                setUpPlayableContentListViewModel(selectedContentId)
            }
        }
        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

    }

    fun setArtImageBg(status: Boolean){
        if (activity!=null&& artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl)&& songDetailroot != null) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
            val bgImage: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_bg_two)
            val gradient: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_gradient_drawable)
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
                            //val color = dynamicToolbarColor(bitmap)
                            Palette.from(bitmap).generate { palette ->
                                //collapsing_toolbar.setContentScrimColor(palette!!.getMutedColor(R.attr.colorPrimary))
                                //collapsing_toolbar.setContentScrimColor(palette!!.getDominantColor(R.attr.colorPrimaryDark))
                                //collapsing_toolbar.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimaryDark))
                                //collapsing_toolbar.setStatusBarScrimColor(palette.getDominantColor(R.attr.colorPrimaryDark))
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                    //(activity as AppCompatActivity).window.statusBarColor = palette.getDominantColor(R.attr.colorPrimaryDark)
                                    artworkProminentColor = CommonUtils.calculateAverageColor(bitmap, 1)
                                    //(activity as AppCompatActivity).window.statusBarColor = artworkProminentColor
                                    MainScope().launch {
                                        if (context != null) {
                                            CommonUtils.setLog(
                                                "SongDetailLifecycle",
                                                "setArtImageBg--$artworkProminentColor"
                                            )
                                            changeStatusbarcolor(artworkProminentColor)
                                        }
                                    }

                                }
                                val color2 = ColorDrawable(palette!!.getDominantColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark)))
                                albumDetailBgArtImageDrawable = LayerDrawable(arrayOf<Drawable>(bgColor, color2, gradient!!))
                                MainScope().launch {
                                    songDetailroot?.background = bgColor
                                    rlHeading?.setBackgroundColor(artworkProminentColor)
                                    iv_collapsingImageBg?.background = artImage
                                    fullGradient?.visibility = View.VISIBLE
                                }
                            }

                        }

                    }
                }catch (e:Exception){

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
            MainScope().launch {
            if (context != null){
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
        }
    }
    private fun toolbarTextAppernce() {
        //collapsing_toolbar.setCollapsedTitleTextAppearance(R.style.collapsedappbar)
        //collapsing_toolbar.setExpandedTitleTextAppearance(R.style.expandedappbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setSongDetailViewModel() {
        songDetailsViewModel = ViewModelProvider(
            this
        ).get(SongDetailsViewModel::class.java)

        if (ConnectionUtil(context).isOnline) {
            songDetailsViewModel?.getSongDetail(requireContext(), selectedContentId)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog(TAG, "shortFilmDetailResp: $it")
                                songDetailModel =it?.data
                                songDetailroot.visibility = View.VISIBLE
                                setDetails(it.data.data.head)
                                setRecommendationData()
                                setFeatureArtistData()
                                setFeatureArtistPlaylist()
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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun setRecommendationData(){
        if (songDetailModel?.data?.body?.recomendation != null && songDetailModel?.data?.body?.recomendation?.size!! > 0) {
            rvRecomendation?.visibility = View.VISIBLE

            val varient = Constant.ORIENTATION_HORIZONTAL

            val bucketParentAdapter = BucketParentAdapter(
                songDetailModel?.data?.body?.recomendation!!,
                requireContext(),
                this@SongDetailFragment,
                this@SongDetailFragment,
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


            bucketParentAdapter?.addData(songDetailModel?.data?.body?.recomendation!!)
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
                            songDetailModel?.data?.body?.recomendation?.get(firstVisiable)?.heading
                        val toBucket =
                            songDetailModel?.data?.body?.recomendation?.get(lastVisiable)?.heading
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
            rvRecomendation.setPadding(0, 0, 0, 0)
        }
    }


    fun setFeatureArtistData() {
        if(songDetailModel?.data?.body?.artist!=null&& songDetailModel?.data?.body?.artist!!.size!!>0) {
            rvFeaturedArtist.setPadding(context?.resources?.getDimensionPixelSize(R.dimen.dimen_18)!!,0,0,0)
            llFeatureArtist.visibility = View.VISIBLE
            ivMoress1.setOnClickListener {
                redirectToMoreBucketListPage(songDetailModel?.data?.body?.artist!!,getString(R.string.song_details_str_1))
            }
            rvFeaturedArtist.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = SongDetailFeaturedArtistAdapter(context, songDetailModel?.data?.body?.artist!!,
                    object : SongDetailFeaturedArtistAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val bundle = Bundle()
                            bundle.putString("image", songDetailModel?.data?.body?.artist!!.get(childPosition)?.data?.image)
                            bundle.putString("id", songDetailModel?.data?.body?.artist!!.get(childPosition)?.data?.id)
                            bundle.putString("playerType", playerType)
                            val artistDetailsFragment = ArtistDetailsFragment()
                            artistDetailsFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@SongDetailFragment, artistDetailsFragment, false)
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llFeatureArtist.visibility=View.VISIBLE
        }else{
            llFeatureArtist.visibility=View.GONE
        }

    }

    fun setFeatureArtistPlaylist() {
        if(songDetailModel?.data?.body?.playlist!=null&& songDetailModel?.data?.body?.playlist!!.size!!>0) {
            rvFeaturedPlaylist.setPadding(context?.resources?.getDimensionPixelSize(R.dimen.dimen_18)!!,0,0,0)
            llFeaturePlaylist.visibility = View.VISIBLE
            ivMoress.setOnClickListener {
                redirectToMoreBucketListPage(songDetailModel?.data?.body?.playlist!!,getString(R.string.song_details_str_2))
            }
            rvFeaturedPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = SongDetailFeaturePlaylistAdapter(context, songDetailModel?.data?.body?.playlist!!,
                    object : SongDetailFeaturePlaylistAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            try {
                                if (songDetailModel != null && songDetailModel?.data != null && songDetailModel?.data?.body != null
                                    && !songDetailModel?.data?.body?.playlist.isNullOrEmpty() && songDetailModel?.data?.body?.playlist?.size!! > childPosition
                                    && songDetailModel?.data?.body?.playlist?.get(childPosition)?.data != null){
                                    val bundle = Bundle()
                                    bundle.putString("image", songDetailModel?.data?.body?.playlist?.get(childPosition)?.data?.image)
//                            if(list?.get(childPosition)!!.data?.image!=null&&list.get(childPosition)!!.data?.images?.size!!>0){
//                                bundle.putStringArrayList("imageArray",
//                                    list.get(childPosition)!!.data?.images as java.util.ArrayList<String>?
//                                )
//                            }

                                    bundle.putString("id", songDetailModel?.data?.body?.playlist?.get(childPosition)?.data?.id)
                                    bundle.putString("playerType",
                                        songDetailModel?.data?.body?.playlist?.get(childPosition)?.data?.type.toString()
                                    )
                                    val varient = 1
                                    val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                                    playlistDetailFragment.arguments = bundle

                                    addFragment(R.id.fl_container, this@SongDetailFragment, playlistDetailFragment, false)
                                }
                            }catch (e:Exception){

                            }
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llFeaturePlaylist.visibility=View.VISIBLE
        }else{
            llFeaturePlaylist.visibility=View.GONE
        }

    }

    private fun setDetails(head: SongDetailModel.Data.Head) {

        setLog("SongDetailsPage", Gson().toJson(head))

        val hashMapPageView = HashMap<String, String>()

        hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = if (head.data.title != null) head.data.title else ""
        hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] = "details_" + Utils.getContentTypeNameForStream("" + head.data.type).replace("_","").lowercase()
        hashMapPageView[EventConstant.SOURCEPAGETYPE_EPROPERTY] = "" + Utils.getContentTypeNameForStream("" + head.data.type)
        hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = "" + head.data.id
        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] = "" + MainActivity.lastItemClicked + "," + MainActivity.headerItemName
        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "details_" + Utils.getContentTypeNameForStream("" + head.data.type).replace("_", "").lowercase()

        setLog("VideoPlayerPageView", hashMapPageView.toString())
        EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))

        artImageUrl = head?.data?.image
        playerType = ""+head?.data?.type
        if (!TextUtils.isEmpty(artImageUrl)){
            ImageLoader.loadImage(
                requireContext(),
                songDetailArtImageView,
                artImageUrl!!,
                R.drawable.bg_gradient_placeholder
            )
            setArtImageBg(true)
        }
        else{
            ImageLoader.loadImage(
                requireContext(),
                songDetailArtImageView,
                "",
                R.drawable.bg_gradient_placeholder
            )
            staticToolbarColor()
        }

        llOther.visibility = View.VISIBLE
        if(head?.data?.title!=null&&!TextUtils.isEmpty(head?.data?.title)){
            tvTitle.text=head?.data?.title
            tvTitle.visibility=View.VISIBLE
        }else{
            tvTitle.visibility=View.GONE
        }

        if(head?.data?.subtitle!=null&&!TextUtils.isEmpty(head?.data?.subtitle)){
            tvSubTitle.text=head?.data?.title
            tvSubTitle.visibility=View.VISIBLE
        }else{
            tvSubTitle.visibility=View.GONE
        }

        if(head?.data?.misc!=null&&!TextUtils.isEmpty(head?.data?.misc?.vendor)){
            tvLabel.text=head?.data?.misc?.vendor
            tvHeadLabel.visibility=View.VISIBLE
            tvLabel.visibility=View.VISIBLE
        }else{
            tvHeadLabel.visibility=View.GONE
            tvLabel.visibility=View.GONE
        }

        if(head?.data?.misc!=null&&head?.data?.misc?.lyricist!=null&&head?.data?.misc?.lyricist?.size!!>0){
            val lyricist=TextUtils.join("/",head?.data?.misc?.lyricist)
            tvLyricist.text=lyricist
            tvHeadLyricist.visibility=View.VISIBLE
            tvLyricist.visibility=View.VISIBLE
        }else{
            tvHeadLyricist.visibility=View.GONE
            tvLyricist.visibility=View.GONE
        }

        if(head?.data?.misc!=null){
            var subtitle=""
            if(!TextUtils.isEmpty(head?.data?.releasedate)){
                var year=DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,head?.data?.releasedate)
                subtitle+=""+year+" • "
            }
            if(!TextUtils.isEmpty(""+head?.data?.duration)){
                subtitle+= DateUtils.convertTimeMS(head.data.duration.toLong())
            }
            tvSubTitle2.text = subtitle

        }
    }


    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2 && songDetailModel!=null){
            var isFavorite = false
            if (songDetailModel != null && songDetailModel?.data != null
                && songDetailModel?.data?.head != null && songDetailModel?.data?.head?.data != null){
                isFavorite = songDetailModel?.data?.head?.data?.isFavorite!!
            }

            setLog(TAG, "onClick: isFavorite"+isFavorite)
            commonThreeDotMenuItemSetup(SONG_DETAIL_PAGE,this@SongDetailFragment,isFavorite)
        }
    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        if (songDetailModel != null){
            songDetailModel?.data?.head?.data?.isFavorite = isFavorite

        }
    }
    fun setUpPlayableContentListViewModel(id: String) {
        val playableContentViewModel: PlayableContentViewModel
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                //setLog(TAG, "isViewLoading $it")
                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setPlayableContentListData(it?.data!!)
                                }
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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }
    fun setUpRecomendedPlayableContentListViewModel(id: String) {
        val playableContentViewModel: PlayableContentViewModel
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                //setLog(TAG, "isViewLoading $it")
                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setRecommendedPlayableContentListData(it?.data!!)
                                }
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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            if (!CommonUtils.checkExplicitContent(requireContext(), playableContentModel.data.head.headData.misc.explicit)){
                setLog("PlayableItem", "setPlayableContentListData id:"+playableContentModel?.data?.head?.headData?.id.toString())
                setLog("PlayableItem", "setSongLyricsData setPlayableContentListData"+playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link.toString())

                songDataList = arrayListOf()

                if (playableContentModel?.data?.head?.headData?.id == selectedContentId) {
                    setSongList(playableContentModel)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    delay(3000)
                    BaseActivity.setTrackListData(songDataList)
                    tracksViewModel.prepareTrackPlayback(0)
                }
            }
        }
    }

    var songDataList:ArrayList<Track> = arrayListOf()
    fun setSongList(
        playableContentModel: PlayableContentModel
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.id)){
            track.id = playableContentModel.data.head.headData.id.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.title)){
            track.title = playableContentModel?.data?.head?.headData?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.subtitle)){
            track.subTitle = playableContentModel?.data?.head?.headData?.subtitle
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
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.type.toString())){
            track.playerType = playableContentModel?.data?.head?.headData?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
        /*if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.title)){
            track.heading = AlbumDetailFragment.albumRespModel?.data?.head?.data?.title
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.playble_image)){
            track.image = playableContentModel?.data?.head?.headData?.playble_image
        }else if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image)){
            track.image = playableContentModel?.data?.head?.headData?.image
        }else{
            track.image = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.pid.isNullOrEmpty()){
            track.parentId = playableContentModel?.data?.head?.headData?.misc?.pid?.get(0).toString()
        }

        if (!playableContentModel?.data?.head?.headData?.misc?.pName.isNullOrEmpty()){
            track.pName = playableContentModel?.data?.head?.headData?.misc?.pName?.get(0).toString()
        }

       /* if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.subtitle!!)){
            track.pSubName = AlbumDetailFragment.albumRespModel?.data?.head?.data?.subtitle
        }*/

        /*if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.image!!)){
            track.pImage = AlbumDetailFragment.albumRespModel?.data?.head?.data?.image
        }*/

        track.pType = DetailPages.SONG_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        track.explicit = playableContentModel?.data?.head?.headData?.misc?.explicit!!
        track.restrictedDownload = playableContentModel?.data?.head?.headData?.misc?.restricted_download!!
        track.attributeCensorRating =
            playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()


        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
        return songDataList
    }

    fun setSongListFromHead(
        songDetailModel: SongDetailModel
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.id)){
            track.id = songDetailModel.data.head.data.id.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.title)){
            track.title = songDetailModel?.data?.head?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.subtitle)){
            track.subTitle = songDetailModel?.data?.head?.data?.subtitle
        }else{
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.misc?.url)){
            track.url = songDetailModel?.data?.head?.data?.misc?.url
        }else{
            track.url = ""
        }

        if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.type.toString())){
            track.playerType = songDetailModel?.data?.head?.data?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
        /*if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.title)){
            track.heading = AlbumDetailFragment.albumRespModel?.data?.head?.data?.title
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.playble_image)){
            track.image = songDetailModel?.data?.head?.data?.playble_image
        }else if (!TextUtils.isEmpty(songDetailModel?.data?.head?.data?.image)){
            track.image = songDetailModel?.data?.head?.data?.image
        }else{
            track.image = ""
        }
        if (!songDetailModel?.data?.head?.data?.misc?.pid.isNullOrEmpty()){
            track.parentId = songDetailModel?.data?.head?.data?.misc?.pid?.get(0).toString()
        }

        if (!songDetailModel?.data?.head?.data?.misc?.p_name.isNullOrEmpty()){
            track.pName = songDetailModel?.data?.head?.data?.misc?.p_name?.get(0).toString()
        }

        /* if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.subtitle!!)){
             track.pSubName = AlbumDetailFragment.albumRespModel?.data?.head?.data?.subtitle
         }*/

        /*if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.image!!)){
            track.pImage = AlbumDetailFragment.albumRespModel?.data?.head?.data?.image
        }*/

        track.pType = DetailPages.SONG_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        track.explicit = songDetailModel?.data?.head?.data?.misc?.explicit!!
        track.restrictedDownload = songDetailModel?.data?.head?.data?.misc?.restricted_download!!
        track.attributeCensorRating =
            songDetailModel?.data?.head?.data?.misc?.attributeCensorRating.toString()

        songDataList.add(track)
        return songDataList
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

    override fun onDestroy() {
        super.onDestroy()
        setLog("SongDetailFragment", "onDestroy-isFromVerticalPlayer-$isFromVerticalPlayer")
        setLog("SongDetailFragment", "onDestroy-isNewSwipablePlayerOpen-${BaseActivity.isNewSwipablePlayerOpen}")
        if (isFromVerticalPlayer){
            if (activity != null){
                BaseActivity.isNewSwipablePlayerOpen = true
                (activity as MainActivity).hideMiniPlayer()
                (activity as MainActivity).hideStickyAds()
            }
        }
        tracksViewModel.onCleanup()
        MainScope().launch {
            if (context != null){
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }

    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        setLog("SongDetailFragment", "onResume-isFromVerticalPlayer-$isFromVerticalPlayer")
        setLog("SongDetailFragment", "onResume-isNewSwipablePlayerOpen-${BaseActivity.isNewSwipablePlayerOpen}")
        BaseActivity.isNewSwipablePlayerOpen = false
        showBottomNavigationAndMiniplayer()
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
                        "SonDetailLifecycle",
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

    var playableItem = RowsItem()
    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        setEventModelDataAppLevel(parent.items?.get(childPosition)?.data?.id!!,parent.items?.get(childPosition)?.data?.title!!,parent?.heading!!)

        if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("110",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("77777",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)) {
            playableItemPosition = childPosition
            playableItem = parent
            BaseActivity.setTouchData()
            setUpRecomendedPlayableContentListViewModel(parent.items?.get(childPosition)?.data?.id!!)
        }else {
            onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
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
            addFragment(R.id.fl_container, this@SongDetailFragment, moreBucketListFragment, false)

            val dataMap= HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ SongDetailFragment.songDetailModel?.data?.head?.data?.title)

            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }
    }

    fun setRecommendedPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (playableContentModel != null ) {
                setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
                songDataList = arrayListOf()
                setLog(TAG, "setPlayableContentListData playerType: ${playableItem?.items?.get(playableItemPosition)?.data?.type}")
                    for (i in playableItem.items!!.indices){
                        if (playableContentModel?.data?.head?.headData?.id == playableItem.items?.get(i)?.data?.id){
                            setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                        }else if (i > playableItemPosition){
                            setPlayableDataList(null, playableItem, i)
                        }

                    }
                    setLog(TAG, "onClick btn_next_play_mini size 2: ${songDataList?.size}")
                    val tracklistDataModel = filterAndPlayAudioContent(songDataList, 0)
                    BaseActivity.setTrackListData(tracklistDataModel.trackListData as ArrayList<Track>)

                        setLog(TAG, "onClick btn_next_play_mini size 6: ${songDataList?.size}")
                        tracksViewModel.prepareTrackPlayback(0)


            }
        }

    }

    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: RowsItem,
        position:Int
    ) {
        val track:Track = Track()

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.id)){
            track.id = playableItem.items?.get(position)?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.title)){
            track.title = playableItem.items?.get(position)?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.subTitle)){
            track.subTitle = playableItem.items?.get(position)?.data?.subTitle
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

        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)){
            track.playerType = playableItem.items?.get(position)?.data?.type
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (activity != null && !TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)) {
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_PODCAST){
                track.contentType = ContentTypes.PODCAST.value
            }else if (playType == Constant.CONTENT_MUSIC){
                track.contentType = ContentTypes.AUDIO.value
            }
        }
        track.pType = DetailPages.SONG_DETAIL_PAGE.value
        if (!TextUtils.isEmpty(playableItem.heading)){
            track.heading = playableItem.heading
        }else{
            track.heading = ""
        }

//        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image)){
//            track.image = playableContentModel?.data?.head?.headData?.image
//        }
        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.playble_image)){
            track.image = playableItem.items?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.image)) {
            track.image = playableItem.items?.get(position)?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem.items?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem.items?.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem.items?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.items?.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem.items?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.items?.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }
}