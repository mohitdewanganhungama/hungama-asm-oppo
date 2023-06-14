package com.hungama.music.ui.main.view.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.hungama.music.BuildConfig
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.databinding.FragmentStoryDisplayBinding
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.StoryCTAClickedEvent
import com.hungama.music.eventanalytic.eventreporter.StoryPageViewedEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.ui.main.adapter.CommentsAdapter
import com.hungama.music.ui.main.view.activity.StoryDisplayActivity
import com.hungama.music.ui.main.viewmodel.UserStoryViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.DateUtils.convertDateTimeIntoMilesecond
import com.hungama.music.utils.customview.stories.StoriesProgressView
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [StoryDisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@OptIn(UnstableApi::class)
class StoryDisplayFragment : Fragment(),
    StoriesProgressView.StoriesListener {

    private var updateStoryUserList: ((Int, Int) -> Unit)? = null
    private var setVideoStoryType: ((Boolean, Boolean)-> Unit)? = null
    private var isKeyboardShowing: Boolean? = false
    private var commentsAdapter: CommentsAdapter? = null

    private var position: Int = 0

    private var storyMainId: String = ""



    private var simpleExoPlayer: ExoPlayer? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var pageViewOperator: PageViewOperator? = null
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L
    private var onResumeCalled = false
    private var onVideoPrepared = false

    lateinit var binding: FragmentStoryDisplayBinding
    private val EXTRA_POSITION = "EXTRA_POSITION"
    private val EXTRA_STORY_USER = "EXTRA_STORY_USER"
    companion object {
        public var stories: UserStoryModel? = null
        fun newInstance(position: Int, story: String, updateStoryUserList: (userIndex: Int, viewIndex: Int) -> Unit,
                        setVideoStoryType: (isVideoStory:Boolean, onResumeCalled: Boolean) -> Unit): StoryDisplayFragment {

            setLog("TAG", "StoryDisplayFragment newInstance: position${position} story${story}")
            return StoryDisplayFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putString(EXTRA_STORY_USER, story)
                }
                this.updateStoryUserList = updateStoryUserList
                this.setVideoStoryType = setVideoStoryType
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryDisplayBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLog("StoryView", "onViewCreated-called")
        binding.storyDisplayVideo.useController = false
        counter = restorePosition()
        //updateStoryUserList?.let { it(position,counter) }
        position = arguments?.getInt(EXTRA_POSITION) ?: 0
        storyMainId = arguments?.getString(EXTRA_STORY_USER).toString()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pageViewOperator = context as PageViewOperator
    }

    override fun onStart() {
        super.onStart()
        //Grishma commented this line
        counter = restorePosition()
        setLog("onStart","$counter")
    }

    override fun onResume() {
        super.onResume()
        setLog("StoryView", "onResume-called-storyMainId-$storyMainId-stories?.id-${stories?.id}-stories-$stories")
        if (!TextUtils.isEmpty(storyMainId)
            && (stories == null
                    || (stories != null && (TextUtils.isEmpty(stories?.id) || !stories?.id.equals(storyMainId))))){
            setupUserStory()
        }
        onResumeCalled = true
        //counter = restorePosition()
        updateStoryUserList?.let { it(position,counter) }
        updateStory()
        if(stories==null || stories?.child?.size!! <= 0 || stories?.child?.size!! <= counter){
            //setupUserStory()
            return
        }
        if (stories?.child?.get(counter)?.media.isNullOrEmpty()){
            return
        }
        if (stories?.isVideo(stories?.child?.get(counter)?.media?.get(0)!!)!! && !onVideoPrepared) {
            simpleExoPlayer?.playWhenReady = false
            setupUserStory()
            return
        }

        simpleExoPlayer?.seekTo(0)
        simpleExoPlayer?.playWhenReady = true
        setLog("StoryView", "onResume-counter-$counter")
        if (counter == 0) {
            binding.storiesProgressView.startStories()
        } else {
            // restart animation
            counter = StoryDisplayActivity.progressState.get(arguments?.getInt(EXTRA_POSITION) ?: 0)
            setLog("StoryView", "onResume-counter-EXTRA_POSITION-$counter")
            binding.storiesProgressView.startStories(counter)
        }

        setLog("TAG", "onResume: startStories counter${counter}")
    }

    override fun onPause() {
        super.onPause()
        setLog("StoryView", "StoryDisplayFragment-onPause()-storyMainId-$storyMainId")
        simpleExoPlayer?.playWhenReady = false
        binding.storiesProgressView.abandon()
    }

    override fun onComplete() {
        if (stories != null && !stories?.child.isNullOrEmpty() && stories?.child?.size!! > counter){
            setLog("StoryView", "StoryDisplayFragment-onComplete()")
            callPostStoryViewed(stories?.child?.get(counter)?.id)
        }
        simpleExoPlayer?.release()
        pageViewOperator?.nextPageView(false)
    }

    override fun onPrev() {
        setLog("StoryView", "onPrev-called")
        if (counter - 1 < 0) return
        --counter
        savePosition(counter)
        stories?.commentsList?.clear()
        commentsAdapter?.updateList(stories?.commentsList)
        updateStory()
    }

    override fun onNext() {
        setLog("StoryView", "onNext-called")
        if (stories?.child?.size!! <= counter + 1) {
            return
        }
        setLog("StoryView", "onNext: counter before increment: $counter")
        ++counter
        savePosition(counter)
        setLog("StoryView", "onNext: counter after increment: $counter")
        updateStory()
        stories?.commentsList = mutableListOf()
        commentsAdapter?.updateList(stories?.commentsList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        simpleExoPlayer?.release()
    }

    private fun updateStory() {
        if (isAdded){
            simpleExoPlayer?.stop()
            if(stories != null && !stories?.child.isNullOrEmpty() && stories?.child?.size!! > counter && !stories?.child?.get(counter)?.media.isNullOrEmpty()){
                if (!stories?.child?.get(counter)?.media.isNullOrEmpty() && stories?.isVideo(stories?.child?.get(counter)?.media?.get(0).toString())!!) {
                    setLog("StoryView","updateStory-VideoStory")
                    binding.storyDisplayVideo.show()
                    binding.storyDisplayImage.hide()
                    binding.storyDisplayVideoProgress.show()
                    initializePlayer()
                    //binding.view1.visibility = View.VISIBLE
//                    binding.ivUnMute.visibility = View.VISIBLE
                } else if (!stories?.child?.get(counter)?.media.isNullOrEmpty()) {
                    setLog("StoryView","updateStory-ImageStory")
                    binding.storyDisplayVideo.hide()
                    binding.storyDisplayVideoProgress.hide()
                    binding.storyDisplayImage.show()
                    Glide.with(this).load(stories?.child?.get(counter)?.media?.get(0)).into(binding.storyDisplayImage)
                    binding.ivMute.visibility = View.GONE
                    binding.ivUnMute.visibility = View.GONE
                    if (isVisible){
                        setLog("setVideoStoryType","updateStory-ImageStory-title-${stories?.name?.get(0)?.en}-onResumeCalled-$onResumeCalled")
                        setVideoStoryType?.let { it(false, onResumeCalled) }
                    }
                }else{
                    if (isVisible){
                        setLog("setVideoStoryType","updateStory-NoneStory--title-${stories?.name?.get(0)?.en}-onResumeCalled-$onResumeCalled")
                        setVideoStoryType?.let { it(false, onResumeCalled) }
                    }
                    return
                }
                setLog("StoryView","updateStory-VideoStory-stories.name-${stories?.name?.get(0)?.en}")
                setUpStorySurveyUi(stories?.child?.get(counter))
                try {
                    /*val cal: Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
                    timeInMillis = convertDateTimeIntoMilesecond(stories?.child?.get(counter)?.createdAt, DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T)
                }*/
                    //setLog("TAG", "updateStory: cal "+cal)
                    setLog("TAG", "updateStory: stories?.child?.get(counter)?.createdAt "+stories?.child?.get(counter)?.createdAt)
                    val today = DateUtils.getCurrentDateTime()
                    val date1: Date
                    val date2: Date
                    val dates = SimpleDateFormat(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_ss)
                    date1 = today
                    date2 = dates.parse(DateUtils.convertLongTodateTime(convertDateTimeIntoMilesecond(stories?.child?.get(counter)?.createdAt, DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T)))
                    val diff: Long = date1.time - date2.time
                    val seconds = diff / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24
                    setLog("StoryView", "StoryTime-days-$days hours-$hours minutes-$minutes seconds-$seconds")
                    //var timeInHourse = DateFormat.format("HH",cal.time ).toString()
                    //setLog("TAG", "updateStory:timeInHourse "+timeInHourse)
                    val t1 = getColoredSpanned("•", "#FFFFFF")
                    val t2 = getColoredSpanned(hours.toString()+ "h", "#999999")
                    binding.storyDisplayTime.text = Html.fromHtml(t1 + " " +t2)
                }catch (e:Exception){
                    binding.storyDisplayTime.hide()
                }


                if(!TextUtils.isEmpty(stories?.child?.get(counter)?.pollName)){
                    binding.llOfferView.hide()
                    binding.bottomOverlayView.hide()
                    binding.tvGetOffer.text = stories?.child?.get(counter)?.pollName
                }else{
                    binding.llOfferView.hide()
                    binding.bottomOverlayView.hide()
                }

                val dataMap=HashMap<String,String>()
                dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                stories?.name?.get(0)!!?.en?.let { dataMap.put(EventConstant.TITLE_EPROPERTY, it) }
                stories?.name?.get(0)!!?.en?.let {
                    dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                        it
                    )
                }
                dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                EventManager.getInstance().sendEvent(StoryPageViewedEvent(dataMap))
            }


        }
    }

    private fun initializePlayer() {
        if (isAdded){
            if (simpleExoPlayer == null) {
                simpleExoPlayer = SimpleExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext()))?.build()
            } else {
                simpleExoPlayer?.release()
                simpleExoPlayer = null
                simpleExoPlayer = SimpleExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext()))?.build()
            }

            mediaDataSourceFactory = DefaultDataSourceFactory(requireContext(), DemoUtil.getDataSourceFactory( /* context= */requireContext()))

            val track = Track()
            track.url = stories?.child?.get(counter)?.media?.get(0)
            val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
                CommonUtils.setMediaItem(track)
            )
            simpleExoPlayer?.prepare(mediaSource, false, false)
            setLog("StoryView","US-initializePlayer-onResumeCalled-$onResumeCalled")
            if (onResumeCalled) {
                simpleExoPlayer?.playWhenReady = true
            }

            binding.storyDisplayVideo.setShutterBackgroundColor(Color.BLACK)
            binding.storyDisplayVideo.player = simpleExoPlayer

            simpleExoPlayer?.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    setLog("TAG", "onPlayerError: ${error.message}")
                    if (stories != null && !stories?.child.isNullOrEmpty() && stories?.child?.size!! > counter){
                        setLog("StoryView", "StoryDisplayFragment-initializePlayer()-onPlayerError()")
                        callPostStoryViewed(stories?.child?.get(counter)?.id)
                    }
                    binding.storyDisplayVideoProgress.hide()
                    if (counter == stories?.child?.size?.minus(1)) {
                        pageViewOperator?.nextPageView(true)
                    } else {
                        binding.storiesProgressView.skip()
                    }
                    setLog("setVideoStoryType","onPlayerError-VideoStory-title-${stories?.name?.get(0)?.en}-onResumeCalled-$onResumeCalled")
                    setVideoStoryType?.let { it(false, onResumeCalled) }
                }

                override fun onLoadingChanged(isLoading: Boolean) {
                    super.onLoadingChanged(isLoading)
                    try {
                        if (isLoading) {
                            if (binding.storyDisplayVideoProgress != null){
                                binding.storyDisplayVideoProgress.show()
                            }

                            pressTime = System.currentTimeMillis()
                            setLog("StoryView", "onLoadingChanged called")
                            pauseCurrentStory()
                        } else {
                            if (binding.storyDisplayVideoProgress != null){
                                binding.storyDisplayVideoProgress.hide()
                                setLog("StoryView", "onLoadingChanged-"+stories?.child?.get(counter)?.pollName+"--"+(simpleExoPlayer?.duration ?: Constant.DEFAULT_PROGRESS_DURATION).toString())
                                val progressWithIndex = binding.storiesProgressView.getProgressWithIndex(counter)
                                if (progressWithIndex != null){
                                    binding.storiesProgressView.getProgressWithIndex(counter)?.setDuration(simpleExoPlayer?.duration ?: Constant.DEFAULT_PROGRESS_DURATION, true)
                                }
                            }
                            onVideoPrepared = true
                            CommonUtils.setLog("StoryView", "onLoadingChanged-isLoading-$isLoading")
                            resumeCurrentStory()
                            setLog("setVideoStoryType","onLoadingChanged-VideoStory-title-${stories?.name?.get(0)?.en}-onResumeCalled-$onResumeCalled")
                            setVideoStoryType?.let { it(true, onResumeCalled) }
                        }
                    }catch (e:Exception){

                    }
                }
            })


        }
    }

    private fun setUpUi() {
        val touchListener = object : OnSwipeTouchListener(requireActivity()) {
            override fun onSwipeTop() {
                val messageModel = MessageModel("onSwipeTop", MessageType.NEUTRAL, true)
                //CommonUtils.showToast(requireContext(), messageModel)
            }

            override fun onSwipeBottom() {
                val messageModel = MessageModel("onSwipeBottom", MessageType.NEUTRAL, true)
                //CommonUtils.showToast(requireContext(), messageModel)
            }

            override fun onClick(view: View) {
                when (view) {
                    binding.next -> {
                        callNextStory()
                    }
                    binding.previous -> {
                        callPreviousStory()
                    }
                }
            }

            override fun onLongClick() {
                hideStoryOverlay()
            }

            override fun onTouchView(view: View, event: MotionEvent): Boolean {
                super.onTouchView(view, event)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressTime = System.currentTimeMillis()
                        setLog("StoryView", "onTouchView-ACTION_DOWN")
                        pauseCurrentStory()
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        showStoryOverlay()
                        setLog("StoryView", "onTouchView-ACTION_UP")
                        resumeCurrentStory()
                        return limit < System.currentTimeMillis() - pressTime
                    }
                }
                return false
            }
        }
        binding.previous.setOnTouchListener(touchListener)
        binding.next.setOnTouchListener(touchListener)
        setLog("StoryView", "US-setUpUi-position-$position")
        setLog("StoryView", "US-setUpUi-position-EXTRA_POSITION-${arguments?.getInt(EXTRA_POSITION) ?: -1}")
        binding.storiesProgressView.setStoriesCountDebug(
            stories?.child?.size!!, position = arguments?.getInt(EXTRA_POSITION) ?: -1
        )
        binding.storiesProgressView.setAllStoryDuration(Constant.DEFAULT_PROGRESS_DURATION)
        binding.storiesProgressView.setStoriesListener(this)

        setLog("TAG", "setUpUi: startStories counter${counter}")
        setLog("StoryView", "US-setUpUi-counter-$counter")
        if (counter == 0) {
            binding.storiesProgressView.startStories()
        }else{
            binding.storiesProgressView.startStories(counter)
        }
        if (!stories?.media.isNullOrEmpty()){
            Glide.with(this).load(stories?.media?.get(0)).circleCrop().into(binding.storyDisplayProfilePicture)
        }
        if (!stories?.name.isNullOrEmpty() && !TextUtils.isEmpty(stories?.name?.get(0)?.en)){
            binding.storyDisplayNick.text = stories?.name?.get(0)?.en + " "
            //binding.storyDisplayNick.text = "abc abc abcd abc 123 abc 123 abc" + " • "
        }



        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.root.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            setLog("hi::", "keypadHeight = $keypadHeight")

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!isKeyboardShowing!!) {
                    isKeyboardShowing = true
                    onKeyboardVisibilityChanged(true)
                }
            }
            else {
                // keyboard is closed
                if (isKeyboardShowing == true) {
                    isKeyboardShowing = false
                    onKeyboardVisibilityChanged(false)
                }
            }
        }

        initRV()

        //val bottomSheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheet.commentsLayout)

        binding.bottomSheet.etComment.clearFocus()

        binding.bottomSheet.etComment.setOnClickListener {
            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


        binding.bottomSheet.btnSendComment.setOnClickListener{
            val commentsModel = CommentsModel()
            commentsModel.userComment = binding.bottomSheet.etComment.text.toString()
            if (!stories?.media.isNullOrEmpty()){
                commentsModel.userProfileUrl = stories?.media?.get(0)
            }

            stories?.commentsList?.add(commentsModel)
            commentsAdapter?.updateList(stories?.commentsList!!)
            binding.bottomSheet.rvComments.scrollToPosition(commentsAdapter?.itemCount!! - 1)
            binding.bottomSheet.etComment.setText("")
        }

        binding.ivCloseStory.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.ivShare.setOnClickListener {
            val messageModel = MessageModel("Perform Share Action!!", MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

        binding.llOfferView.setOnClickListener {
            setLog("StoryView", "binding.llOfferView-click")
            pauseCurrentStory()
            val bottomSheet = BottomSheet()
            bottomSheet.show(requireActivity().supportFragmentManager, stories?.child?.get(counter)?.media?.get(0))
            bottomSheet.setOnBottomSheetCloseListener(object :
                BottomSheet.OnBottomSheetCloseListener {
                override fun onBottomSheetClose() {
                    //Toast.makeText(requireActivity(), "closedd...", Toast.LENGTH_SHORT).show()
                    setLog("StoryView", "setUpUi-onBottomSheetClose")
                    resumeCurrentStory()
                }

            })

            val dataMap=HashMap<String,String>()
            dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
            stories?.name?.get(0)!!?.en?.let { it1 ->
                dataMap.put(EventConstant.TITLE_EPROPERTY,
                    it1
                )
            }
            stories?.name?.get(0)!!?.en?.let { it1 ->
                dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                    it1
                )
            }
            dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
            dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
            dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,stories?.child?.get(counter)?.media?.get(0)!!)
            EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
        }

    }


    private fun onKeyboardVisibilityChanged(opened: Boolean) {
        setLog("hi::", "onKeyboardVisibilityChanged: keyboard $opened")
        //isKeyboardOpen = opened
        if (opened) {
            setLog("StoryView", "onKeyboardVisibilityChanged-true")
            pauseCurrentStory()
        } else {
            setLog("StoryView", "onKeyboardVisibilityChanged-else")
            resumeCurrentStory()
        }
    }

    private fun initRV() {

        commentsAdapter = CommentsAdapter(mutableListOf(), requireActivity())
        binding.bottomSheet.rvComments.adapter = commentsAdapter

    }
    private fun showStoryOverlay() {
        if (binding.storyOverlay == null || binding.storyOverlay.alpha != 0F) return

        binding.storyOverlay.animate()
            .setDuration(100)
            .alpha(1F)
            .start()
    }

    private fun hideStoryOverlay() {
        if (binding.storyOverlay == null || binding.storyOverlay.alpha != 1F) return

        binding.storyOverlay.animate()
            .setDuration(200)
            .alpha(0F)
            .start()
    }

    private fun savePosition(pos: Int) {
        StoryDisplayActivity.progressState.put(position, pos)
        updateStoryUserList?.let { it(position,pos) }
    }

    private fun restorePosition(): Int {
        return StoryDisplayActivity.progressState.get(position)
    }

    fun pauseCurrentStory() {
        CommonUtils.setLog("StoryView", "pauseCurrentStory called")
        simpleExoPlayer?.playWhenReady = false
        binding.storiesProgressView.pause()
    }

    fun resumeCurrentStory() {
        CommonUtils.setLog("StoryView", "resumeCurrentStory-onResumeCalled-$onResumeCalled-storyMainId-$storyMainId")
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
            showStoryOverlay()
            binding.storiesProgressView.resume()
        }
    }



    /**
     * initialise view model and setup-observer
     */
    private fun setupUserStory() {
        if (ConnectionUtil(requireActivity()).isOnline) {
            val userStoryViewModel = ViewModelProvider(this
            ).get(UserStoryViewModel::class.java)
            setLog("StoryView","setupUserStory-API called")
            userStoryViewModel.getUserStoryDataLatest(requireContext(), storyMainId)?.observe(requireActivity(),
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            if (isAdded){
                                stories = it?.data
                                setLog("StoryView","setupUserStory-API called-setUpUi called- updateStory called")
                                setUpUi()
                                updateStory()
                            }
                        }

                        Status.LOADING ->{
                            //setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            //setProgressBarVisible(false)
                            //Utils.showSnakbar(requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }

    private fun setUpStorySurveyUi(survey: UserStoryModel.Child?) {
        binding.clStoryV1.visibility = View.GONE
        binding.clStoryV2.visibility = View.GONE
        binding.clStoryV3.visibility = View.GONE
        binding.clStoryV4.visibility = View.GONE
        binding.clStoryV5.visibility = View.GONE
        binding.clStoryV6.visibility = View.GONE

        if (survey?.polls?.varient.equals("v1", true)){
            if (!survey?.polls?.options.isNullOrEmpty()){
                binding.tvStoryV1.text = survey?.polls?.options?.get(0)?.name
                //binding.ivStoryV1.setImageDrawable(requireContext().faDrawable(R.string.icon_play_2, Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!)))
                binding.ivStoryV1.setIcon("\uD83D\uDD16",Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                binding.tvStoryV1.setTextColor(Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                val gd = GradientDrawable()
                gd.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat()
                gd.setColor(Color.parseColor(survey?.polls?.options?.get(0)?.bgcolor!!))
                binding.clStoryV1.background = gd
                binding.clStoryV1.visibility = View.VISIBLE
                binding.clStoryV1.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV1!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(0))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(0)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
            }
        }else if (survey?.polls?.varient.equals("v2", true)){
            if (!survey?.polls?.options.isNullOrEmpty()){
                binding.tvStoryV2.text = survey?.polls?.options?.get(0)?.name
                binding.tvStoryV2.setTextColor(Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                val gd = GradientDrawable()
                gd.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_100).toFloat()
                gd.setColor(Color.parseColor(survey?.polls?.options?.get(0)?.bgcolor!!))
                binding.clStoryV2.background = gd
                binding.clStoryV2.visibility = View.VISIBLE
                binding.clStoryV2.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV2!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(0))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(0)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
            }
        }else if (survey?.polls?.varient.equals("v3", true)){
            if (!survey?.polls?.options.isNullOrEmpty() && survey?.polls?.options?.size!! > 3){
                binding.tvStoryV3Question.text = survey.polls.title

                binding.tvStoryV3TextView1.text = survey.polls.options.get(0).name
                binding.tvStoryV3TextView2.text = survey.polls.options.get(1).name
                binding.tvStoryV3TextView3.text = survey.polls.options.get(2).name
                binding.tvStoryV3TextView4.text = survey.polls.options.get(3).name

                binding.tvStoryV3Question.setTextColor(Color.WHITE)
                binding.tvStoryV3TextView1.setTextColor(Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                binding.tvStoryV3TextView2.setTextColor(Color.parseColor(survey?.polls?.options?.get(1)?.fgcolor!!))
                binding.tvStoryV3TextView3.setTextColor(Color.parseColor(survey?.polls?.options?.get(2)?.fgcolor!!))
                binding.tvStoryV3TextView4.setTextColor(Color.parseColor(survey?.polls?.options?.get(3)?.fgcolor!!))

                val gd = GradientDrawable()
                gd.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd.setColor(Color.parseColor(survey?.polls?.options?.get(0)?.bgcolor!!))
                binding.clStoryV3Bg.background = gd
                binding.clStoryV3.background = gd

                val gd2 = GradientDrawable()
                gd2.cornerRadii = floatArrayOf(
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top Right
                    0f,
                    0f,
                    0f,
                    0f
                )
                gd2.setColor(Color.BLACK)
                binding.clStoryV3Question.background = gd2

                binding.clStoryV3.visibility = View.VISIBLE

                binding.clStoryV3Button1.setOnClickListener {
                    surveyRedirection(survey.polls.options.get(0))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(0)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
                binding.clStoryV3Button2.setOnClickListener {
                    surveyRedirection(survey.polls.options.get(1))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(1)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
                binding.clStoryV3Button3.setOnClickListener {
                    surveyRedirection(survey.polls.options.get(2))
                    val dataMap=HashMap<String,String>()
                    stories?.let { it1 -> dataMap.put(EventConstant.STORY_ID_EPROPERTY, it1.id) }
                    stories?.name?.get(0)?.en.let { it1 ->
                        it1?.let { it2 ->
                            dataMap.put(EventConstant.TITLE_EPROPERTY,
                                it2
                            )
                        }
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(2)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
                binding.clStoryV3Button4.setOnClickListener {
                    surveyRedirection(survey.polls.options.get(3))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(4)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
            }
        }else if (survey?.polls?.varient.equals("v4", true)){

            if (!survey?.polls?.options.isNullOrEmpty() && survey?.polls?.options?.size!! > 1){
                binding.tvStoryV4TextView1.text = survey.polls.options.get(0).name
                binding.tvStoryV4TextView2.text = survey.polls.options.get(1).name

                binding.tvStoryV4TextView1.setTextColor(Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                binding.tvStoryV4TextView2.setTextColor(Color.parseColor(survey?.polls?.options?.get(1)?.fgcolor!!))

                val gd = GradientDrawable()
                //gd.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd.cornerRadii = floatArrayOf(
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    0f,
                    0f,
                    0f,
                    0f,
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//bottom left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()//bottom left
                )
                gd.setColor(Color.parseColor(survey?.polls?.options?.get(0)?.bgcolor!!))
                binding.clStoryV4Button1.background = gd

                val gd2 = GradientDrawable()
                //gd2.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd2.cornerRadii = floatArrayOf(
                    0f,
                    0f,
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                    0f,
                    0f
                )
                gd2.setColor(Color.parseColor(survey?.polls?.options?.get(1)?.bgcolor!!))
                binding.clStoryV4Button2.background = gd2

                binding.clStoryV4.visibility = View.VISIBLE

                binding.clStoryV4Button1.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV4Button1!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(0))

                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(0)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
                binding.clStoryV4Button2.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV4Button2!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(1))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(1)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
            }
        }else if (survey?.polls?.varient.equals("v5", true)){
            if (!survey?.polls?.options.isNullOrEmpty() && survey?.polls?.options?.size!! > 1){
                binding.tvStoryV5TextView1.text = survey.polls.options.get(0).name
                binding.tvStoryV5TextView2.text = survey.polls.options.get(1).name

                binding.tvStoryV5TextView1.setTextColor(Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                binding.tvStoryV5TextView2.setTextColor(Color.parseColor(survey?.polls?.options?.get(1)?.fgcolor!!))

                val gd = GradientDrawable()
                //gd.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd.cornerRadii = floatArrayOf(
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    0f,
                    0f,
                    0f,
                    0f,
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//bottom left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()//bottom left
                )

                gd.setColor(Color.parseColor(survey?.polls?.options?.get(0)?.bgcolor!!))
                binding.clStoryV5Button1.background = gd

                val gd2 = GradientDrawable()
                //gd2.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd2.cornerRadii = floatArrayOf(
                    0f,
                    0f,
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                    0f,
                    0f
                )
                gd2.setColor(Color.parseColor(survey?.polls?.options?.get(1)?.bgcolor!!))
                binding.clStoryV5Button2.background = gd2

                binding.clStoryV5.visibility = View.VISIBLE

                binding.clStoryV5Button1.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV5Button1!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(0))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(0)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
                binding.clStoryV5Button2.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV5Button2!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(1))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)?.en?.let { it1 -> dataMap.put(EventConstant.TITLE_EPROPERTY, it1) }
                    stories?.name?.get(0)?.en?.let { it1 -> dataMap.put(EventConstant.STORY_NAME_EPROPERTY, it1) }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,survey.polls.options.get(1).name)
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
            }
        }else if (survey?.polls?.varient.equals("v6", true)){
            if (!survey?.polls?.options.isNullOrEmpty() && survey?.polls?.options?.size!! > 1){
                binding.tvStoryV6Question.text = survey.polls.title
                binding.tvStoryV6TextView1.text = survey.polls.options.get(0).name
                binding.tvStoryV6TextView2.text = survey.polls.options.get(1).name

                binding.tvStoryV6Question.setTextColor(Color.BLACK)
                binding.tvStoryV6TextView1.setTextColor(Color.parseColor(survey?.polls?.options?.get(0)?.fgcolor!!))
                binding.tvStoryV6TextView2.setTextColor(Color.parseColor(survey?.polls?.options?.get(1)?.fgcolor!!))

                val gd3 = GradientDrawable()
                gd3.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd3.setColor(Color.WHITE)
                binding.clStoryV6Question.background = gd3

                val gd = GradientDrawable()
                //gd.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd.cornerRadii = floatArrayOf(
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Top left
                    0f,
                    0f,
                    0f,
                    0f,
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//bottom left
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()//bottom left
                )
                gd.setColor(Color.parseColor(survey?.polls?.options?.get(0)?.bgcolor!!))
                binding.clStoryV6Button1.background = gd

                val gd2 = GradientDrawable()
                //gd2.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat()
                gd2.cornerRadii = floatArrayOf(
                    0f,
                    0f,
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//top Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                    resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat(),//Bottom Right
                    0f,
                    0f
                )
                gd2.setColor(Color.parseColor(survey?.polls?.options?.get(1)?.bgcolor!!))
                binding.clStoryV6Button2.background = gd2

                binding.clStoryV6.visibility = View.VISIBLE

                binding.clStoryV6Button1.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV6Button1!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(0))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(0)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
                binding.clStoryV6Button2.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(requireContext(), binding.clStoryV6Button2!!,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    }catch (e:Exception){

                    }
                    surveyRedirection(survey.polls.options.get(1))
                    val dataMap=HashMap<String,String>()
                    dataMap.put(EventConstant.STORY_ID_EPROPERTY,stories?.id!!)
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.TITLE_EPROPERTY,
                            it1
                        )
                    }
                    stories?.name?.get(0)!!?.en?.let { it1 ->
                        dataMap.put(EventConstant.STORY_NAME_EPROPERTY,
                            it1
                        )
                    }
                    dataMap.put(EventConstant.TYPE_OF_THE_STORY_PAGE_EPROPERTY,stories?.type!!)
                    dataMap.put(EventConstant.POLL_ID_EPROPERTY,stories?.child?.get(counter)?.poll!!)
                    survey?.polls?.options?.get(1)?.name?.let { it1 ->
                        dataMap.put(EventConstant.OPTION_SELECTED_EPROPERTY,
                            it1
                        )
                    }
                    EventManager.getInstance().sendEvent(StoryCTAClickedEvent(dataMap))
                }
            }
        }
    }

    private fun surveyRedirection(survey: UserStoryModel.Child.Polls.Option) {
        if (!TextUtils.isEmpty(survey.url)){
            requireActivity().onBackPressed()
            if (survey.external){
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(survey.url)
                startActivity(openURL)
            }else{
                try {

                    if (survey.url.contains("rewind-2022")) {
                        val url = "https://www.hungama.com/rewind-2022"
                        survey.url = url
                    }
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(survey.url)
//                    openURL.setPackage(BuildConfig.APPLICATION_ID)
                    startActivity(openURL)
                } catch (e: ActivityNotFoundException) {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(survey.url)
                    startActivity(openURL)
                }
            }
        }
    }

    private fun callPostStoryViewed(childStoryId: String?) {
        setLog("StoryView", "StoryDisplayFragment-callPostStoryViewed-childStoryId-$childStoryId")
        if (ConnectionUtil(requireActivity()).isOnline && !TextUtils.isEmpty(childStoryId)) {
            val userStoryViewModel = ViewModelProvider(this
            ).get(UserStoryViewModel::class.java)

            userStoryViewModel.postUserStory(requireContext(), childStoryId.toString())?.observe(requireActivity(),
                {
                    when(it.status){
                        Status.SUCCESS->{
                            if (it?.data != null){
                                if (!TextUtils.isEmpty(it.data.message)){
                                    setLog("userReadStory",it.data.message)
                                }
                            }
                        }

                        Status.LOADING ->{
                            //setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            //setProgressBarVisible(false)
                            //Utils.showSnakbar(requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            //Utils.showSnakbar(requireView(), false, getString(R.string.discover_str_4)
        }
    }

    private fun callNextStory(){
        setLog("StoryView", "callNextStory-story is null-${stories?.child.isNullOrEmpty()}-story size-${stories?.child?.size}-counter-$counter")
        if (stories != null && !stories?.child.isNullOrEmpty() && stories?.child?.size!! > counter){
            setLog("StoryView", "StoryDisplayFragment-setUpUi()-onClick()-next")
            callPostStoryViewed(stories?.child?.get(counter)?.id)
        }
        if (counter == stories?.child?.size!! - 1) {
            pageViewOperator?.nextPageView(false)
        } else {
            binding.storiesProgressView.skip()
        }
    }

    private fun callPreviousStory(){
        if (counter == 0) {
            pageViewOperator?.backPageView(false)
        } else {
            binding.storiesProgressView.reverse()
        }
    }
    private fun getColoredSpanned(text: String, color: String): String {
        return "<font color=$color>$text</font>"
    }
}