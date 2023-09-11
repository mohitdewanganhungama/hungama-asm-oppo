package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.google.gson.Gson
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.model.*
import com.hungama.music.data.model.OnUserSubscriptionUpdate

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.LiveConcertEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.utils.*
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.main.viewmodel.ArtistViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.eventreporter.LiveConcertReminderEvent
import com.hungama.music.ui.main.adapter.*
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fr_main.shimmerLayout
import kotlinx.android.synthetic.main.fragment_event_detail.*
import kotlinx.android.synthetic.main.fragment_event_detail.ivShare
import kotlinx.android.synthetic.main.fragment_event_detail.scrollView
import kotlinx.android.synthetic.main.fragment_event_detail.tvReadMore
import kotlinx.android.synthetic.main.fragment_event_detail.tvSubTitle
import kotlinx.android.synthetic.main.fragment_event_detail.tvTitle
import kotlinx.android.synthetic.main.fragment_event_detail.view.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : BaseFragment(), TracksContract.View, ViewTreeObserver.OnScrollChangedListener,
    BaseFragment.OnUserContentOrderStatus, OnUserSubscriptionUpdate {

    private var isTimerRuning: Boolean=true;
    var artImageUrl: String? = null
    var eventTitle: String = ""
    var selectedContentId: String? = null
    var selectedArtistId: String? = null
    var isDirectPayment = 0
    var queryParam: String? = null
    var playerType: String? = null
    var planName = ""

    var aritstViewModel: ArtistViewModel? = null
    var playlistRespModel: PlaylistModel? = null
    var artistRespModel = LiveEventDetailModel()
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playableItemPosition = 0
    var playlistSongList: List<PlaylistModel.Data.Body.Row?>? = null
    var artWorkImageColor = 0
    var topSongList: List<BodyRowsItemsItem?>? = null
    var selectedArtistSongImage = ""
    var selectedArtistSongTitle = ""
    var selectedArtistSongSubTitle = ""
    var selectedArtistSongHeading = ""
    var songDataList: ArrayList<Track> = arrayListOf()
    var liveEventUrl: String = ""
    var contentOrderStatus = Constant.CONTENT_ORDER_STATUS_NA

    companion object{
        var  liveEventDetailRespModel: LiveEventDetailModel?=null
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideBottomNavigationAndMiniplayer()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun initializeComponent(view: View) {
        BucketParentAdapter.isVisible = false
        CommonUtils.applyButtonTheme(requireContext(), llEventButton)
        CommonUtils.applyButtonTheme(requireContext(), llEventButtonLoading)
        if (arguments != null){
            if (requireArguments().containsKey(Constant.defaultContentId)){
                selectedContentId = requireArguments().getString(Constant.defaultContentId).toString()
            }
            if (requireArguments().containsKey(Constant.defaultArtistId)){
                selectedArtistId = requireArguments().getString(Constant.defaultArtistId).toString()
            }

            if(requireArguments()?.containsKey(Constant.isPayment)!!){
                isDirectPayment = requireArguments().getInt(Constant.isPayment)
                queryParam = requireArguments().getString(Constant.EXTRA_QUERYPARAM)!!
            }
        }

        ivBack?.setOnClickListener { view -> backPress() }
        headBarBlur.visibility = View.INVISIBLE

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.stopShimmer()

        scrollView.viewTreeObserver.addOnScrollChangedListener(this@EventDetailFragment)

        setUpLiveEventDetailListViewModel()
        (activity as BaseActivity).tabMenu.hide()
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        ivShare?.setOnClickListener(this)
        llRemindMe?.setOnClickListener(this)
    }

    override fun onScrollChanged() {
        if (isAdded) {
            // get the maximum height which we have scroll before performing any action
            var maxDistance = 0

            maxDistance +=  resources.getDimensionPixelSize(R.dimen.dimen_238)
            // how much we have scrolled
            val movement = scrollView.scrollY
            setLog("OnNestedScroll-m", movement.toString())
            setLog("OnNestedScroll-d", maxDistance.toString())

            if (movement >= maxDistance){
                headBarBlur.visibility = View.VISIBLE
                tvActionBarHeading.visibility = View.VISIBLE
                headingBgColor.setBackgroundColor(artWorkImageColor)
            }else{
                headBarBlur.visibility = View.GONE
                tvActionBarHeading.visibility = View.GONE
                headingBgColor.setBackgroundColor(0)
            }
        }
    }



    fun setArtImageBg(status: Boolean, artImageUrl: String) {
        if (activity!=null&& artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl)) {
            val result: Deferred<Bitmap?> = GlobalScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }

            GlobalScope.launch(Dispatchers.IO) {
                // get the downloaded bitmap
                val bitmap: Bitmap? = result.await()
                try {
                    val artImage = BitmapDrawable(resources, bitmap)
                    if (status) {
                        if (bitmap != null) {
                            //artWorkImageColor = CommonUtils.calculateAverageColor(bitmap, 1)
                            Palette.from(bitmap!!).generate { palette ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    //window.statusBarColor = palette.getMutedColor(R.attr.colorPrimaryDark)
                                    /*(activity as AppCompatActivity).window.statusBarColor =
                                        palette?.getDominantColor(R.attr.colorPrimaryDark)!!*/
                                    //setStatusBarColor(artWorkImageColor)
                                }
                            }
                            bgMain.background = artImage
                        }

                    }else{
                        (activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.event_detail_menu, menu)
        return onCreateOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.event_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_share_event ->{

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



    private fun setUpLiveEventDetailListViewModel() {
        try {
            if (selectedContentId != null && selectedArtistId != null){
                if (ConnectionUtil(context).isOnline) {

                    aritstViewModel = ViewModelProvider(
                        this
                    ).get(ArtistViewModel::class.java)

                    aritstViewModel?.getLiveEventDetail(
                        requireContext(),
                        selectedContentId.toString(),
                        selectedArtistId.toString()
                    )?.observe(this,
                        Observer {
                            when(it.status){
                                Status.SUCCESS->{
                                    setLiveEventDetailsListData(it?.data!!)
                                    setDetails(it?.data!!)
                                }

                                Status.LOADING ->{
                                    setProgressBarVisible(false)
                                }

                                Status.ERROR ->{
                                    setEmptyVisible(false)
                                    setProgressBarVisible(false)
                                    Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                }
                            }
                        })
                } else {
                    val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                        MessageType.NEGATIVE, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
            }
        }catch (e:Exception){

        }
    }




    fun setDetails(model: LiveEventDetailModel) {
        liveEventDetailRespModel = model
        if (model != null && model?.data?.head?.event != null){
            if (!TextUtils.isEmpty(model?.data?.head?.event?.id)){
                getContentOrderStatus(this, model.data.head.event.id)
            }
        }
        if (model != null && model?.data?.body != null) {


            artImageUrl = ""+ liveEventDetailRespModel?.data?.head?.event?.image?.get(0)
            eventTitle = ""+liveEventDetailRespModel?.data?.head?.event?.name!!
            playerType = "20"

            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Live Event")
                hashMap.put(EventConstant.CONTENTNAME_EPROPERTY,""+model?.data?.head?.event?.name)
                setLog("TAG","login${hashMap}")
                EventManager.getInstance().sendEvent(LiveConcertEvent(hashMap))
            }


            if (!TextUtils.isEmpty(model?.data?.head?.event?.url)) {
                liveEventUrl = model.data.head.event.url

            }

            tvActionBarHeading.text = model?.data?.head?.event?.name

            if (!TextUtils.isEmpty(model?.data?.head?.event?.name)) {
                tvTitle.text = model?.data?.head?.event?.name
            } else {
                tvTitle.visibility = View.INVISIBLE
            }
            if (!TextUtils.isEmpty(model?.data?.head?.event?.artistName)) {
                tvArtistName.text = model?.data?.head?.event?.artistName
            } else {
                tvArtistName.visibility = View.INVISIBLE
            }


            planName = CommonUtils.getContentPlanName(liveEventDetailRespModel?.data?.head?.event?.movierights?.toString()!!)
            setLog("planName", planName)
            if (planName != "LE0")
                callPlanDetailApi()
            else
                contentOrderStatus = Constant.CONTENT_ORDER_STATUS_SUCCESS



            val keyName=SharedPrefHelper.getInstance().getUserId()+"_"+liveEventDetailRespModel?.data?.head?.event?.id
           if(!keyName.isNullOrEmpty())
            if(SharedPrefHelper.getInstance().has(keyName)){
                ivReminder?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_baseline_done_24))
                //llRemindMe?.isEnabled=false
            }else{
                ivReminder?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_clock))
                //llRemindMe?.isEnabled=true
            }


            if (!TextUtils.isEmpty(model?.data?.head?.event?.date)) {
                val date = DateUtils.convertDate(
                    DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                    DateUtils.DATE_FORMAT_DD_MMMM_YYYY,
                    model?.data?.head?.event?.date
                )
                val time = DateUtils.convertDate(
                    DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                    DateUtils.DATE_FORMAT_HH_MM_A,
                    model?.data?.head?.event?.date
                )
                tvDate.text = date
                tvTime.text = time

            } else {
                tvDate.visibility = View.GONE
                tvTime.visibility = View.GONE
            }

            if (model?.data?.head?.event?.image != null && model.data.head.event.image.size > 0) {
                ImageLoader.loadImage(
                    requireContext(),
                    ivEventView,
                    model?.data?.head?.event?.image?.get(0)!!,
                    R.drawable.bg_gradient_placeholder
                )
                setArtImageBg(true, model?.data?.head?.event?.image?.get(0)!!!!)

                setLog("ivEventView","ivEventView 1 :${model?.data?.head?.event?.image?.get(0)}")
            } else {
                ImageLoader.loadImage(
                    requireContext(),
                    ivEventView,
                    artImageUrl!!,
                    R.drawable.bg_gradient_placeholder
                )
                setArtImageBg(false, "")

                setLog("ivEventView","ivEventView 2 :${artImageUrl}")
            }

            if (model?.data?.head?.event?.thumbnail != null && model.data.head.event.thumbnail.size > 0) {
                artImageUrl=model?.data?.head?.event?.thumbnail?.get(0)!!
            }

            ImageLoader.loadImage(
                requireContext(),
                ivArtistUserImage!!,
                artImageUrl!!,
                R.drawable.bg_gradient_placeholder
            )
            if (!TextUtils.isEmpty(model.data.head.event.artistId)){
                rootParent?.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(Constant.defaultContentId, "artist-"+model.data.head.event.artistId)
                    val artistDetailsFragment = ArtistDetailsFragment()
                    artistDetailsFragment.arguments = bundle
                    addFragment(R.id.fl_container, this, artistDetailsFragment, false)
                }
            }
            if (model?.data?.head?.event?.artistName!=null&&!TextUtils.isEmpty(model?.data?.head?.event?.artistName)) {
                tvArtistTitle?.text = model?.data?.head?.event?.artistName
                tvHeadingAbout?.text = getString(R.string.artist_str_7) + " " + model?.data?.head?.event?.artistName.toString().lowercase().replaceFirstChar(Char::titlecase)
            } else {
                tvArtistTitle?.visibility = View.INVISIBLE
                tvHeadingAbout?.text = getString(R.string.artist_str_7)
            }

            if (!TextUtils.isEmpty(model?.data?.head?.data?.misc?.f_playcount)) {
                /*tvArtistFollower?.text =
                    CommonUtils.ratingWithSuffix("" + model?.data?.head?.data?.misc?.playcount!!)*/
                tvArtistFollower?.text = model?.data?.head?.data?.misc?.f_playcount
            } else {
                tvArtistFollower?.visibility = View.INVISIBLE
            }

            if (!TextUtils.isEmpty(model?.data?.head?.event?.about)) {
                tvAbout?.text = getString(R.string.live_events_str_3)+":"
                tvReadMore.text = model?.data?.head?.event?.about
                SaveState.isCollapse = true
                tvReadMore.setShowingLine(3)
                tvReadMore.addShowMoreText("read more")
                tvReadMore.addShowLessText("read less")

                tvReadMore.setShowMoreColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorWhite
                    )
                )
                tvReadMore.setShowLessTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorWhite
                    )
                )
                tvReadMore.setShowMoreStyle(Typeface.BOLD)
                tvReadMore.setShowLessStyle(Typeface.BOLD)
                tvReadMore.visibility = View.VISIBLE
            } else {
                tvAbout.visibility = View.GONE
                tvReadMore.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(model?.data?.head?.data?.misc?.description)) {
                tvAboutDetail.text = model?.data?.head?.data?.misc?.description
                SaveState.isCollapse = true
                tvAboutDetail.setShowingLine(3)
                tvAboutDetail.addShowMoreText("read more")
                tvAboutDetail.addShowLessText("read less")

                tvAboutDetail.setShowMoreColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorWhite
                    )
                )
                tvAboutDetail.setShowLessTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorWhite
                    )
                )
                tvAboutDetail.setShowMoreStyle(Typeface.BOLD)
                tvAboutDetail.setShowLessStyle(Typeface.BOLD)
                tvAboutDetail.visibility = View.VISIBLE
            } else {
                llAbout.visibility = View.GONE
                tvAboutDetail.visibility = View.GONE
            }
        }

        setContentActionButton(contentOrderStatus)

        rlActionBarHeader.visibility = View.VISIBLE
        scrollView.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()

        if (isDirectPayment == 1){
            directPayment()
        }
    }

    var countDownTimer:CountDownTimer?=null
    private fun startCountDown(artistModel: LiveEventDetailModel) {
        val formatter = SimpleDateFormat(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)
        val oldTime = DateUtils.convertDate(
            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
            artistModel?.data?.head?.event?.date
        )

        var newTime=""
        newTime = formatter.format(Date().time) //Timer date 2

        val oldDate: Date
        val newDate: Date
        var diff: Long = 0L
        try {
            oldDate = formatter.parse(oldTime)
            newDate = formatter.parse(newTime)
            val oldLong = oldDate.time
            val newLong = newDate.time
            diff = oldLong - newLong

            if (diff > 1) {
                countDownTimer=object : CountDownTimer(diff, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        if(isVisible){
                            val seconds = millisUntilFinished / 1000
                            val minutes = seconds / 60
                            val hours = minutes / 60
                            val days = hours / 24

                            val dHours = hours % 24
                            val dMinutes = minutes % 60
                            val dSeconds = seconds % 60

                            if (days >= 1) {
                                val time =
                                    days.toString() + " " + "d" + " :" + String.format(
                                        "%02d",
                                        dHours
                                    ) + ":" + String.format("%02d", dMinutes) + ":" + String.format(
                                        "%02d",
                                        dSeconds
                                    )
                                tvTimeDays?.setText(String.format("%02d", days))
                                tvHours?.setText(String.format("%02d", dHours))
                                tvMin?.setText(String.format("%02d", dMinutes))



                            }
                            else {
                                val time =
                                    "" + String.format("%02d", dHours) + ":" + String.format(
                                        "%02d",
                                        dMinutes
                                    ) + ":" + String.format("%02d", dSeconds)
                                tvHours?.setText(String.format("%02d", dHours))
                                tvMin?.setText(String.format("%02d", dMinutes))


                                tvTimeDays?.visibility = View.GONE
                                tvDaysLabel?.visibility = View.GONE
                            }

                            isTimerRuning = false
                        }

                    }

                    override fun onFinish() {
                        isTimerRuning = false
                        llCountdown?.visibility=View.GONE
                        llEventJoinButton?.visibility=View.VISIBLE
                        llEventJoinButton?.setOnClickListener(this@EventDetailFragment)
                    }
                }.start()
            }else{
                isTimerRuning=false
                llCountdown?.visibility=View.GONE
                llEventJoinButton?.visibility=View.VISIBLE
                llEventJoinButton?.setOnClickListener(this)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLiveEventDetailsListData(artistModel: LiveEventDetailModel) {

        if (artistModel != null && artistModel?.data?.body != null) {
            artistRespModel = artistModel
            setArtistTopSongData(artistModel)
            setArtistAlbumData(artistModel)
            setArtistMusicVideoData(artistModel)
            //setArtistTVShowData(artistModel)
            setArtistLikeData(artistModel)
            setNewReleaseData(artistModel)
//            setMovieData(artistModel)

        }
    }

    private fun setNewReleaseData(artistModel: LiveEventDetailModel) {
        if (artistModel?.data?.body?.newreleasesong != null && !artistModel?.data?.body?.newreleasesong?.items.isNullOrEmpty()) {
            ImageLoader.loadImage(
                requireContext(),
                ivNewReleaseImage,
                artistModel.data.body.newreleasesong.items.get(0).data.image,
                R.drawable.bg_gradient_placeholder
            )
            tvNewTitle.text = artistModel?.data?.body?.newreleasesong?.items?.get(0)?.data?.title
            tvSubTitle.text = artistModel?.data?.body?.newreleasesong?.items?.get(0)?.data?.subtitle

            llNewRelease?.visibility = View.VISIBLE

            llNewRelease?.setOnClickListener {


                selectedArtistSongImage =
                    artistModel.data.body.newreleasesong.items.get(0).data.image
                selectedArtistSongTitle =
                    artistModel.data.body.newreleasesong.items.get(0).data.title
                selectedArtistSongSubTitle =
                    artistModel.data.body.newreleasesong.items.get(0).data.subtitle
                selectedArtistSongHeading = artistModel.data?.head?.data?.title!!
                playableItemPosition = 0
                setUpPlayableContentListViewModel(
                    artistModel?.data?.body?.newreleasesong?.items?.get(0)?.data?.id!!
                ,201)
                setEventModelDataAppLevel(artistModel?.data?.body?.newreleasesong?.items?.get(0)?.data?.id!!,artistModel?.data?.body?.newreleasesong?.items?.get(0)?.data?.title!!,artistModel.data.head.event.name)
            }

        } else {
            llNewRelease?.visibility = View.GONE
        }
    }

    fun setArtistTopSongData(artistModel: LiveEventDetailModel) {
        if (artistModel?.data?.body?.songs != null && !artistModel?.data?.body?.songs?.items.isNullOrEmpty()) {
            topSongList = artistModel?.data?.body?.songs?.items
            llHeaderSongs?.setOnClickListener {
                redirectToMoreBucketListPage(artistModel?.data?.body?.songs?.items,"Top Songs")
            }
            rvSongs.apply {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.HORIZONTAL, false)
                adapter = EventTopSongAdapter(
                    context, artistModel?.data?.body?.songs?.items!!,
                    object : EventTopSongAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            selectedArtistSongImage =
                                artistModel?.data?.body?.songs?.items?.get(childPosition)?.data?.image!!
                            selectedArtistSongTitle =
                                artistModel?.data?.body?.songs?.items?.get(childPosition)?.data?.title!!
                            selectedArtistSongSubTitle =
                                artistModel?.data?.body?.songs?.items?.get(childPosition)?.data?.subTitle!!
                            selectedArtistSongHeading = artistModel.data?.head?.data?.title!!
                            playableItemPosition = childPosition
                            setUpPlayableContentListViewModel(
                                artistModel?.data?.body?.songs?.items?.get(
                                    childPosition
                                )?.data?.id!!,
                                202
                            )
                            setEventModelDataAppLevel(artistModel?.data?.body?.songs?.items?.get(
                                childPosition
                            )?.data?.id!!,artistModel?.data?.body?.songs?.items?.get(
                                childPosition
                            )?.data?.title!!,artistModel.data.head.event.name)
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llSongs.visibility = View.VISIBLE
        } else {
            llSongs.visibility = View.GONE
        }

    }

    fun setArtistAlbumData(artistModel: LiveEventDetailModel) {
        if (artistModel.data?.body?.albums != null && !artistModel.data?.body?.albums?.items.isNullOrEmpty()) {
            tvHeadingAlbums?.text =
                artistModel?.data?.head?.data?.title + " " + getString(R.string.album_str_2)
            ivMoreAlbums.setOnClickListener {
                redirectToMoreBucketListPage(artistModel.data.body.albums.items,"Album")
            }
            rvAlbums.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = EventAlbumAdapter(
                    context, artistModel.data?.body?.albums?.items,
                    object : EventAlbumAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val albumDetailFragment = AlbumDetailFragment()
                            val bundle = Bundle()
                            bundle.putString(
                                "image",
                                artistModel.data?.body?.albums?.items?.get(childPosition)?.data?.image!!
                            )
                            bundle.putString(
                                "id",
                                artistModel.data?.body?.albums?.items?.get(childPosition)?.data?.id
                            )
                            bundle.putString("playerType", playerType)
                            albumDetailFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@EventDetailFragment,
                                albumDetailFragment,
                                false
                            )
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llAlbums.visibility = View.VISIBLE
        } else {
            llAlbums.visibility = View.GONE
        }
    }

    fun setArtistMusicVideoData(artistModel: LiveEventDetailModel) {
        if (artistModel.data?.body?.videos != null && !artistModel.data?.body?.videos?.items.isNullOrEmpty()) {
            ivMoreMusicVideo.setOnClickListener {
                redirectToMoreBucketListPage(artistModel.data.body.videos.items,getString(R.string.artist_str_9))
            }
            rvMusicVideo.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = EventMusicVideoAdapter(
                    context, artistModel.data?.body?.videos?.items,
                    object : EventMusicVideoAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {

                            val type = artistModel.data?.body?.videos?.items?.get(childPosition)?.data?.type
                            if (type.equals(
                                    "22",
                                    true
                                ) || type.equals(
                                    "53",
                                    true
                                ) || type.equals("88888", true)
                            ) {
                                if (activity != null && activity is MainActivity){
                                    (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
                                }
                                val bundle = Bundle()
                                bundle.putString(Constant.defaultContentId, artistModel.data?.body?.videos?.items?.get(childPosition)?.data?.id)
                                bundle.putString(Constant.defaultContentPlayerType, type)

                                val musicVideoDetailsFragment = MusicVideoDetailsFragment()
                                musicVideoDetailsFragment.arguments = bundle
                                addFragment(R.id.fl_container, this@EventDetailFragment, musicVideoDetailsFragment, false)


                            }else{
                                val songsList =
                                CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                            val serviceBundle = Bundle()
                            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
                            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                            intent.putExtra(
                                "thumbnailImg",
                                artistModel.data?.body?.videos?.items?.get(childPosition)?.data?.image
                            )
                            serviceBundle.putString(
                                Constant.SELECTED_CONTENT_ID,
                                artistModel.data?.body?.videos?.items?.get(childPosition)?.data?.id
                            )
                            serviceBundle.putInt(
                                Constant.TYPE_ID,
                                artistModel.data?.body?.videos?.items?.get(childPosition)?.data?.type!!.toInt()
                            )
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


                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llMusicVideo.visibility = View.VISIBLE
        } else {
            llMusicVideo.visibility = View.GONE
        }

    }

    fun setArtistLikeData(artistModel: LiveEventDetailModel) {
        if (artistModel.data?.body?.similar != null && !artistModel.data?.body?.similar?.items.isNullOrEmpty()) {
            ivMoreLike.setOnClickListener {
                redirectToMoreBucketListPage(artistModel?.data?.body?.similar?.items,getString(R.string.artist_str_13))
            }
            rvLike.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = EventLikeAdapter(
                    context, artistModel.data?.body?.similar?.items,
                    object : EventLikeAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val bundle = Bundle()
                            bundle.putString(
                                "image",
                                artistModel.data?.body?.similar?.items?.get(childPosition)?.data?.image
                            )
                            bundle.putString(
                                "id",
                                artistModel.data?.body?.similar?.items?.get(childPosition)?.data?.id
                            )
                            bundle.putString("playerType", playerType)

                            val artistDetailsFragment = ArtistDetailsFragment()
                            artistDetailsFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@EventDetailFragment,
                                artistDetailsFragment,
                                false
                            )
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            llLike.visibility=View.VISIBLE
        }else{
            llLike.visibility=View.GONE
        }

    }


    override fun onDestroy() {
        showBottomNavigationAndMiniplayer()
        countDownTimer?.cancel()
        removeContentOrderStatusTimerCallback()
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

    private fun setUpPlayableContentListViewModel(id: String, type: Int) {
        /**
         * type 201 -> new release song
         * type 202 -> top song
         */
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data!= null) {
                                setPlayableContentListData(it?.data!!,type)
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
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setPlayableContentListData(playableContentModel: PlayableContentModel, type: Int) {

        if (playableContentModel != null ) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            //setPodcastEpisodeList(playableContentModel)
            songDataList = arrayListOf()

            if(type==201){
                setNewReleaseSong(playableContentModel, artistRespModel.data?.body?.newreleasesong?.items!!, 0)
            }else if (type ==202){
                for (i in topSongList?.indices!!){
                    if (playableContentModel?.data?.head?.headData?.id == topSongList?.get(i)?.data?.id){
                        setArtistContentList(playableContentModel, topSongList, playableItemPosition)
                    }else if(i > playableItemPosition){
                        setArtistContentList(null, topSongList, i)
                    }
                }
            }


            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)
        }
    }

    fun setNewReleaseSong(
        playableContentModel: PlayableContentModel?,
        playableItem: List<LiveEventDetailModel.Data.Body.Newreleasesong.Item>,
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
        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
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
        if (!TextUtils.isEmpty(playerType)){
            track.playerType = playerType
        }else{
            track.playerType = ""
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

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id!!)){
            track.parentId = playableItem?.get(position)?.data?.id!!
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title!!)){
            track.pName = playableItem?.get(position)?.data?.title
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subtitle!!)){
            track.pSubName = playableItem?.get(position)?.data?.subtitle
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image!!)){
            track.pImage = playableItem?.get(position)?.data?.image
        }

        if (playableItem?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }

        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        track.pType = DetailPages.EVENT_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value
        songDataList.add(track)
    }

    fun setArtistContentList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<BodyRowsItemsItem?>?,
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

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subTitle)){
            track.subTitle = playableItem?.get(position)?.data?.subTitle
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
        if (!TextUtils.isEmpty(playerType)){
            track.playerType = playerType
        }else{
            track.playerType = ""
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

        if (!TextUtils.isEmpty(artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.id!!)){
            track.parentId = artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.id!!
        }
        if (!TextUtils.isEmpty(artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.title!!)){
            track.pName = artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.title
        }

        if (!TextUtils.isEmpty(artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.subTitle!!)){
            track.pSubName = artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.subTitle
        }

        if (!TextUtils.isEmpty(artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.image!!)){
            track.pImage = artistRespModel?.data?.body?.songs?.items?.get(position)?.data?.image
        }

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

        track.pType = DetailPages.EVENT_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value
        songDataList.add(track)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
//        if (hidden){
//            showBottomNavigationAndMiniplayer()
//        }else{
            hideBottomNavigationAndMiniplayer()
//        }
    }

    override fun onClick(v: View) {
        super.onClick(v)

        if (v == llEventJoinButton){
/*            val fragment = TicketConfirmationFragment()
            val bundle = Bundle()
            bundle.putString("ticketCost", ""+liveEventDetailRespModel?.data?.head?.event?.ticketCost)
            bundle.putString("name", liveEventDetailRespModel?.data?.head?.event?.name)
            bundle.putString("date", liveEventDetailRespModel?.data?.head?.event?.date)
            bundle.putString("image", artImageUrl)
            bundle.putString("id", selectedContentId)
            bundle.putString("liveEventUrl", liveEventUrl)
            fragment.arguments = bundle
            addFragment(R.id.fl_container, this, fragment, false)*/

            val fragment= EventPlayerFragment()
            val bundle=Bundle()
            bundle.putString("eventName",liveEventDetailRespModel?.data?.head?.event?.name)
            bundle.putString("id",selectedContentId)
            bundle.putString("liveEventUrl",liveEventUrl)
            bundle.putString("screenmode", liveEventDetailRespModel?.data?.head?.event?.mode)
            fragment.arguments=bundle
            addFragment(R.id.fl_container, this, fragment, false)

        }
        else if (v == llCountdown){
            val fragment = TicketConfirmationFragment()
            val bundle = Bundle()
            bundle.putString("ticketCost", ""+liveEventDetailRespModel?.data?.head?.event?.ticketCost)
            bundle.putString("name", liveEventDetailRespModel?.data?.head?.event?.name)
            bundle.putString("date", liveEventDetailRespModel?.data?.head?.event?.date)
            bundle.putString("image", artImageUrl)
            bundle.putString("id", selectedContentId)
            bundle.putString("liveEventUrl", liveEventUrl)
            bundle.putString("screenmode", liveEventDetailRespModel?.data?.head?.event?.mode)
            fragment.arguments = bundle
            addFragment(R.id.fl_container, this, fragment, false)
        }
        else if (v == ivShare){
            val shareurl=getString(R.string.music_player_str_18)+" "+ liveEventDetailRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(),shareurl)
        }else if (v == llEventButton){
            if (CommonUtils.isUserHasRentedEvent(requireContext(), selectedContentId.toString())){
                val fragment = TicketConfirmationFragment()
                val bundle = Bundle()
                bundle.putString("ticketCost", ""+liveEventDetailRespModel?.data?.head?.event?.ticketCost)
                bundle.putString("name", liveEventDetailRespModel?.data?.head?.event?.name)
                bundle.putString("date", liveEventDetailRespModel?.data?.head?.event?.date)
                bundle.putString("image", artImageUrl)
                bundle.putString("id", selectedContentId)
                bundle.putString("liveEventUrl", liveEventUrl)
                bundle.putString("screenmode", liveEventDetailRespModel?.data?.head?.event?.mode)
                fragment.arguments = bundle
                addFragment(R.id.fl_container, this, fragment, false)
            }else{
                Constant.screen_name ="Event Detail Screen"
                CommonUtils.openSubscriptionDialogPopup(
                    requireContext(),
                    planName,
                    selectedContentId.toString(),
                    true,
                    this,
                    eventTitle,
                    null,Constant.drawer_svod_purchase
                )
            }
        }else if(v==llRemindMe){
            val keyName=SharedPrefHelper.getInstance().getUserId()+"_"+liveEventDetailRespModel?.data?.head?.event?.id
            if(!keyName.isNullOrEmpty())
            if(!SharedPrefHelper.getInstance().has(keyName)){

                ivReminder?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_baseline_done_24))
                //llRemindMe?.isEnabled=false

                SharedPrefHelper.getInstance().save(keyName,true)
                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.CONCERT_NAME, liveEventDetailRespModel?.data?.head?.event?.name!!)
                    hashMap.put(EventConstant.CONCERT_ID, liveEventDetailRespModel?.data?.head?.event?.id!!)
                    hashMap.put(EventConstant.DATE_OF_CONCERT, tvDate.text?.toString()!!)
                    hashMap.put(EventConstant.TIME_OF_CONCERT, tvTime.text?.toString()!!)
                    setLog("TAG","login${hashMap}")
                    EventManager.getInstance().sendEvent(LiveConcertReminderEvent(hashMap))
                }

            }else{
                SharedPrefHelper.getInstance().delete(keyName)
                ivReminder?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_clock))
                //llRemindMe?.isEnabled=true
            }
        }
    }

    override fun onUserContentOrderStatusCheck(status: Int) {
        planName = CommonUtils.getContentPlanName(liveEventDetailRespModel?.data?.head?.event?.movierights?.toString()!!)

        if (planName != "LE0") {
            contentOrderStatus = status
            llCountdown.llCountdown.isEnabled = true
        }
        else {
            contentOrderStatus = Constant.CONTENT_ORDER_STATUS_SUCCESS
            llCountdown.llCountdown.isEnabled = false
        }

        setContentActionButton(contentOrderStatus)
    }

    private fun setContentActionButton(status: Int):Boolean{
        setLog("contentOrder-4", status.toString())
        if (status == Constant.CONTENT_ORDER_STATUS_NA || status == Constant.CONTENT_ORDER_STATUS_FAIL){
            llCountdown?.visibility = View.GONE
            llEventButtonLoading?.visibility = View.GONE
            llEventJoinButton?.visibility = View.GONE
            llEventButton?.visibility = View.VISIBLE

            llEventJoinButton?.setOnClickListener(null)
            llCountdown?.setOnClickListener(null)
            llEventButtonLoading?.setOnClickListener(null)
            llEventButton?.setOnClickListener(this)
            return false
        }else if (status == Constant.CONTENT_ORDER_STATUS_IN_PROCESS || status == Constant.CONTENT_ORDER_STATUS_PENDING){
            llEventJoinButton?.visibility = View.GONE
            llCountdown?.visibility = View.GONE
            llEventButtonLoading?.visibility = View.VISIBLE
            llEventButton?.visibility = View.GONE

            llEventJoinButton?.setOnClickListener(null)
            llCountdown?.setOnClickListener(null)
            llEventButtonLoading?.setOnClickListener(this)
            llEventButton?.setOnClickListener(null)
            runContentOrderStatusHandler(this, selectedContentId!!)
            return true
        }else if (status == Constant.CONTENT_ORDER_STATUS_SUCCESS){
            llCountdown?.visibility = View.VISIBLE
            llEventButtonLoading?.visibility = View.GONE
            llEventButton?.visibility = View.GONE
            llEventJoinButton?.visibility = View.GONE

//            if(isTimerRuning){
                startCountDown(liveEventDetailRespModel!!)
//            }

            llCountdown?.setOnClickListener(this)
            llEventButtonLoading?.setOnClickListener(null)
            llEventButton?.setOnClickListener(null)
            return true
        }
        return false
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        setLog("contentOrder-1", status.toString()+"--"+contentId)
        getContentOrderStatus(this, contentId)
        if (status == Constant.CONTENT_ORDER_STATUS_NA){

        }else if(status == Constant.CONTENT_ORDER_STATUS_FAIL){

        }else if (status == Constant.CONTENT_ORDER_STATUS_IN_PROCESS || status == Constant.CONTENT_ORDER_STATUS_PENDING){

        }else if (status == Constant.CONTENT_ORDER_STATUS_SUCCESS){

        }

    }

    private fun directPayment(){
        if (liveEventDetailRespModel!=null) {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = liveEventDetailRespModel?.data?.head?.data?.id!!
            dpm.contentTitle = liveEventDetailRespModel?.data?.head?.data?.title!!
            dpm.planName = liveEventDetailRespModel?.data?.head?.event?.movierights?.toString()!!
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = true
            dpm.queryParam = queryParam!!
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT

            planName = CommonUtils.getContentPlanName(liveEventDetailRespModel?.data?.head?.event?.movierights?.toString()!!)
            Constant.screen_name ="Payment Screen"
            CommonUtils.openSubscriptionDialogPopup(
                requireContext(),
                planName,
                selectedContentId.toString(),
                true,
                this,
                eventTitle,
                dpm,Constant.drawer_svod_purchase
            )

        }
    }

    private fun callPlanDetailApi(){


        planName = CommonUtils.getContentPlanName(liveEventDetailRespModel?.data?.head?.event?.movierights?.toString()!!)

        setLog(TAG, "callPlanDetailApi: planName:${planName}")

        val userSubscriptionViewModel = ViewModelProvider(
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
                                        if (jsonData.has(planName)) {
                                            plan = jsonData.getJSONArray(planName)
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
                                                    val price = planModel.planPrice.toDouble()
                                                    val df = DecimalFormat("###.##")
                                                    val price2 = df.format(price)
                                                    if(Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
                                                        liveEventDetailRespModel?.data?.head?.event?.ticketCost=price
                                                        tvBuy.text = getString(R.string.live_events_str_9) + " @ " + getString(R.string._rupee) + price2
                                                    }else{
                                                        liveEventDetailRespModel?.data?.head?.event?.ticketCost=price
                                                        tvBuy.text = getString(R.string.live_events_str_9) + " @ " + getString(R.string._doller) + price2
                                                    }
                                                }
                                            }catch (e:Exception){

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