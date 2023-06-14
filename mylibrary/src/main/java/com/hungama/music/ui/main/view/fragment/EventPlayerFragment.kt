package com.hungama.music.ui.main.view.fragment

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Rational
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import com.hungama.music.data.model.PlayableContentModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.StreamEvent
import com.hungama.music.eventanalytic.eventreporter.StreamFailedEvent
import com.hungama.music.eventanalytic.eventreporter.StreamStartEvent
import com.hungama.music.eventanalytic.eventreporter.StreamTriggerEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.ArtistViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.setMediaItem
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.hide
import com.hungama.music.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_event_player.*
import kotlinx.android.synthetic.main.swipable_player_controls_layout.view.*
import kotlinx.android.synthetic.main.view_movie.view.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit


@UnstableApi class EventPlayerFragment : BaseFragment(), Player.Listener {

    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition: Long = 0
//    private val mp4Url = "https://live.hungama.com/linear/dil-se/playlist.m3u8"
////    private val mp4Url = "https://html5demos.com/assets/dizzy.mp4"
//    private val dashUrl = "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_uhd.mpd"
////    private val urlList = listOf(mp4Url to "default", dashUrl to "dash")
//    private val urlList = listOf(mp4Url to "default")
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playableContentModel: PlayableContentModel? = null
    var selectedContentId: String? = null
    var eventName: String? = null
    var liveEventUrl:String = ""
    var screenMode:String = ""
    var viewerCount = 0
    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(requireContext(), "exoplayer-sample")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        hideBottomNavigationAndMiniplayer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_player, container, false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        player_view.hideController()
        setLog("ScreenMode", screenMode)
        if (screenMode.equals("landscape",true)  && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rlActionBarHeader.visibility = View.GONE
            rlActionBarHeaderLandscape.visibility = View.VISIBLE
            ivLive.visibility = View.GONE
            ivView.visibility = View.GONE
            tvLiveEventCount.visibility = View.GONE
            ivOrientation.visibility = View.GONE
            tvOrientation.visibility = View.GONE

            if (viewerCount >0){
                ivLiveLandscape.visibility = View.VISIBLE
                tvLiveEventCountLandscape.visibility = View.VISIBLE
            }
            player_view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

        } else if (screenMode.equals("landscape",true) && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlActionBarHeader.visibility = View.VISIBLE
            rlActionBarHeaderLandscape.visibility = View.GONE
            ivLive.visibility = View.VISIBLE
            ivView.visibility = View.VISIBLE
            tvLiveEventCount.visibility = View.VISIBLE
            player_view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            ivOrientation.visibility = View.VISIBLE
            tvOrientation.visibility = View.VISIBLE
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
            //when user click on Close button of PIP this will trigger.
        }
        else if (getLifecycle().getCurrentState() == Lifecycle.State.STARTED){
            //when PIP maximize this will trigger
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    private fun enablePipMode() {
        // set aspect ratio
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val picInPicParamsBuilder = PictureInPictureParams.Builder()
             picInPicParamsBuilder.setAspectRatio(Rational(player_view.width, player_view.height))
             // enter the PiP mode
             activity?.enterPictureInPictureMode(picInPicParamsBuilder.build())
        } else {

        }


    }
    override fun initializeComponent(view: View) {
        eventName = requireArguments().getString("eventName").toString()
        selectedContentId = requireArguments().getString("id").toString()
        liveEventUrl = requireArguments().getString("liveEventUrl", "").toString()
        screenMode = requireArguments().getString("screenmode", "").toString()
        //setUpPlayableContentListViewModel(selectedContentId!!)
        player_view.hideController()
        ivBack?.setOnClickListener {
            backPress()
//            onPictureInPictureModeChanged(true)
//            enablePipMode()
        }
        ivBackLandscape?.setOnClickListener {
            backPress()
//            onPictureInPictureModeChanged(true)
        }
        tvActionBarHeading.text = eventName
        setLog("ScreenMode", screenMode + "\n"+liveEventUrl)

        if (screenMode.equals("landscape",true)  && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rlActionBarHeader.visibility = View.GONE
            rlActionBarHeaderLandscape.visibility = View.VISIBLE
            ivLive.visibility = View.GONE
            ivView.visibility = View.GONE
            tvLiveEventCount.visibility = View.GONE
            ivOrientation.visibility = View.GONE
            tvOrientation.visibility = View.GONE

        } else if (screenMode.equals("landscape",true) && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlActionBarHeader.visibility = View.VISIBLE
            rlActionBarHeaderLandscape.visibility = View.GONE
            ivLive.visibility = View.VISIBLE
            ivOrientation.visibility = View.VISIBLE
            tvOrientation.visibility = View.VISIBLE
        }
        ivView.visibility = View.GONE
        tvLiveEventCount.visibility = View.GONE
        ivShare?.setOnClickListener(this)
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
        return onPrepareOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }
    override fun onResume() {
        screenMode = requireArguments().getString("screenmode", "").toString()
        if (screenMode == "landscape")
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        super.onResume()
    }

    override fun onPause() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        /*val allocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(allocator)
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .setBufferDurationsMs(10000, 120000, 1000, 1000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()*/

        val isPause = (requireActivity() as MainActivity).isPause
        if(!isPause){
            (requireActivity() as MainActivity).pausePlayer()
        }
        val renderersFactory = DefaultRenderersFactory(requireActivity())

        simpleExoplayer = SimpleExoPlayer.Builder(requireContext(),renderersFactory)
            //.setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .build()
        //val randomUrl = urlList.random()
        //preparePlayer(randomUrl.first, randomUrl.second)
        startPlayer(liveEventUrl)


//        timer = Timer()
//        timer?.scheduleAtFixedRate(object : TimerTask() {
//            override fun run() {
//                callLiveEventCount()
//            }
//
//        }, 1000, 10*1000) //wait 0 ms before doing the action and do it evry 1000ms (10second)

        startUpdatesLiveEventCount()

    }


    var job: Job? = null

    fun startUpdatesLiveEventCount() {
        stopUpdates()
        job = baseMainScope.launch {
            while(true) {
                callLiveEventCount() // the function that should be ran every second
                delay(10*1000)
            }
        }
    }

    fun stopUpdates() {
        job?.cancel()
        job = null
    }

    private fun startPlayer(url: String?){
        if (!TextUtils.isEmpty(url)) {
            callStreamEvents(false)
            preparePlayer(url!!)
            player_view.player = simpleExoplayer
            //simpleExoplayer.seekTo(playbackPosition)
            simpleExoplayer.playWhenReady = true
            simpleExoplayer.addListener(this)
        }
    }

    fun callStreamEvents(isStream:Boolean){
        val hashMap = HashMap<String, String>()
        hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + selectedContentId)
        hashMap.put(EventConstant.URL_EPROPERTY, "" + liveEventUrl)
        hashMap.put(EventConstant.NAME, "" + eventName)
        hashMap.put(EventConstant.MODE_EPROPERTY, "" + screenMode)
        if (simpleExoplayer != null && simpleExoplayer?.currentPosition != null && simpleExoplayer?.currentPosition!! > 0){
            hashMap.put(EventConstant.DURATION_EPROPERTY, "" + TimeUnit.MILLISECONDS.toSeconds(simpleExoplayer.currentPosition))
        }
        if (Constant.API_DEVICE_TYPE == "Android"){
            hashMap.put(EventConstant.CARPLAY, "false")
        }else{
            hashMap.put(EventConstant.CARPLAY, "true")
        }
        if (isStream) {
            EventManager.getInstance().sendEvent(StreamEvent(hashMap))
        }
        else{
            hashMap.remove(EventConstant.DURATION_EPROPERTY)
            EventManager.getInstance().sendEvent(StreamTriggerEvent(hashMap))
            EventManager.getInstance().sendEvent(StreamStartEvent(hashMap))
        }
    }

    private fun buildMediaSource(track: Track): MediaSource {

        /*return if (type == "dash") {
            DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        }else if(uri.toString().contains("m3u8")){
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        }*/

        return if(track.url?.contains(".m3u8", true)!!){
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(setMediaItem(track))
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(setMediaItem(track))
        }
    }

    private fun preparePlayer(videoUrl: String) {
        val track = Track()
        track.id = 1
        track.url = videoUrl
        val mediaSource = buildMediaSource(track)
        //simpleExoplayer.prepare(mediaSource)
        simpleExoplayer.setMediaSource(mediaSource)
        simpleExoplayer.prepare()
        simpleExoplayer.playWhenReady = true
    }

    private fun releasePlayer() {
        if (simpleExoplayer != null){
            playbackPosition = simpleExoplayer.currentPosition
            simpleExoplayer.release()
        }
    }

    private fun callStreamFailedEvent(
        error: PlaybackException
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            if (playableContentModel!=null){
                val hashMap = HashMap<String, String>()


                hashMap.put(EventConstant.CONNECTION_TYPE_EPROPERTY, ConnectionUtil.NETWORK_TYPE)
                hashMap.put(EventConstant.CONSUMPTION_TYPE_EPROPERTY, EventConstant.CONSUMPTIONTYPE_ONLINE)
                var newContentId= playableContentModel?.data?.head?.headData?.id
                var contentIdData=newContentId?.replace("playlist-","")
                hashMap.put(EventConstant.CONTENTID_EPROPERTY, ""+contentIdData)
                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+playableContentModel?.data?.head?.headData?.type))
                hashMap.put(EventConstant.DURATION_EPROPERTY,"00:00")
                hashMap.put(EventConstant.ERRORCODE_EPROPERTY,"")
                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,""+error.errorCodeName)
                hashMap.put(EventConstant.SCREEN_NAME_EPROPERTY,"Event Player")
                hashMap.put(EventConstant.PCODE_EPROPERTY,""+playableContentModel?.data?.head?.headData?.misc?.pName)
                hashMap.put(EventConstant.SCODE_EPROPERTY,""+playableContentModel?.data?.head?.headData?.misc?.pName)
                hashMap.put(EventConstant.AP_EPROPERTY,"")
                hashMap.put(EventConstant.BUFF_EPROPERTY,"")
                hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
                hashMap.put(EventConstant.SOURCE_DETAILS_EPROPERTY,""+""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)

                EventManager.getInstance().sendEvent(StreamFailedEvent(hashMap))
            }
        }


    }
    override fun onPlayerError(error: PlaybackException) {
        // handle error

        setLog("TAG", "onPlayerError EventPlayerFragment callStreamFailedEvent:${error} ")

        callStreamFailedEvent(error)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            loading_exoplayer.visibility = View.VISIBLE
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED)
            loading_exoplayer.visibility = View.INVISIBLE
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
        if(v==ivShare){
            val shareurl=getString(R.string.music_player_str_18)+" "+ EventDetailFragment.liveEventDetailRespModel?.data?.head?.data?.misc?.share
            Utils.shareItem(requireActivity(),shareurl)
        }
    }


    private fun callLiveEventCount() {
        if (ConnectionUtil(context).isOnline) {
            val liveEventCountViewModel = ViewModelProvider(
                this@EventPlayerFragment
            ).get(ArtistViewModel::class.java)

            liveEventCountViewModel.getLiveEventCountDetail(
                requireContext(),
                selectedContentId!!
            ).observe(this@EventPlayerFragment,
                {
                    setLog("TAG", "getVolleyRequest: liveEventCountModel:${it}")
                    when(it.status){
                        Status.SUCCESS->{
                            if(it?.data!=null){
                                viewerCount = it.data.count
                                if (it.data.count>0){
                                    tvLiveEventCount.text = it.data.count.toString()
                                    tvLiveEventCountLandscape.text = it.data.count.toString()
                                   if (screenMode == "landscape" && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                                       tvLiveEventCount.visibility = View.GONE
                                       ivView.visibility = View.GONE
                                       ivViewLandscape.visibility = View.VISIBLE
                                       tvLiveEventCountLandscape.visibility = View.VISIBLE
                                   }
                                    else{
                                       tvLiveEventCount.visibility = View.VISIBLE
                                       ivView.visibility = View.VISIBLE
                                       ivViewLandscape.visibility = View.GONE
                                       tvLiveEventCountLandscape.visibility = View.GONE
                                   }
                                }
                            }
                        }
                        else -> {}
                    }
                })

        }


    }


    override fun onDestroy() {
        callStreamEvents(true)
        stopUpdates()
        super.onDestroy()
    }
}

