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
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.GameTriggerEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.DetailGameAdapter
import com.hungama.music.ui.main.view.activity.CommonWebViewWithToolbarActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.GamelistViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.databinding.FragmentGameDetailBinding
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL


class GameDetailFragment : BaseFragment(), BaseFragment.OnMenuItemClicked, OnUserSubscriptionUpdate,
    OnParentItemClickListener{

    var artImageUrl: String? = null
    var gameRating: String? = "0.0"
    var gameurl: String? = null
    var imageArray: ArrayList<String>? = null
    var variantImages: ArrayList<String>? = null

    var selectedContentId: String? = null
    var playerType: String? = null
    var flagPause: Boolean = false
    var gameListViewModel: GamelistViewModel? = null
    var ascending = true
    var gamelistAdpter: DetailGameAdapter? = null
    var artworkProminentColor = 0
    var artworkHeight = 0
    lateinit var binding: FragmentGameDetailBinding
    private var detailBgArtImageDrawable: LayerDrawable? = null
    var playListModel: GamelistModel? = null
    lateinit var imagePerViewDetails: ArrayList<String>



    companion object {
        var playlistRespModel: GamelistModel? = null
        var chartSongItem: PlaylistModel.Data.Body.Row? = null
        var playableItemPosition = 0
        var varient: Int = 0
        fun newInstance(varient: Int): GameDetailFragment {
            val fragment = GameDetailFragment()
            this.varient = varient
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (activity != null && activity is MainActivity) {
            BaseActivity.isNewSwipablePlayerOpen = false
            (requireActivity() as MainActivity).showBottomNavigationBar()
        }
        (requireActivity() as MainActivity).showBottomNavigationBar()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            setLog("ChartLifecycle", "initializeComponent-1")
            if (context != null) {
                setLog("ChartLifecycle", "initializeComponent-2")
                if (CommonUtils.isUserHasGoldSubscription()) {
                    CommonUtils.setAppButton2(requireContext(), binding.llPlayNow)
                    CommonUtils.setAppButton2(requireContext(), binding.llPlayNowActionBar)
                } else {

                    CommonUtils.setAppButton1(requireContext(), binding.llPlayNow)
                    CommonUtils.setAppButton1(requireContext(), binding.llPlayNowActionBar)
                }
                if (arguments != null) {
                    selectedContentId = requireArguments().getString("id").toString()
                }



                ivBack?.setOnClickListener { backPress() }
                binding.rlHeading.show()
                threeDotMenu.hide()
                binding.rvGamePreviewlist.hide()

                binding.shimmerLayout.show()
                binding.shimmerLayout.startShimmer()


                binding.llPlayNowActionBar.setOnClickListener {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(
                                requireContext(),
                                binding.llPlayNowActionBar,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                        }
                    } catch (e: Exception) {

                    }

                }

                CommonUtils.setPageBottomSpacing(
                    binding.scrollView,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
                binding.scrollView.isSmoothScrollingEnabled = true
                setUpPlaylistDetailListViewModel()
            }

        }
    }


    fun setArtImageBg(status: Boolean) {
        if (activity !== null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                artImageUrl
            ) && binding.gamelistDetailroot != null
        ) {
            val result: Deferred<Bitmap?> = baseIOScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }

            baseIOScope.launch {
                try {
                    // get the downloaded bitmap
                    val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
                    val bitmap: Bitmap? = result.await()
                    val artImage = BitmapDrawable(resources, bitmap)
                    if (status) {
                        if (bitmap != null && isAdded && view != null) {

                            artworkProminentColor = CommonUtils.calculateAverageColor(bitmap, 1)
                            withContext(Dispatchers.Main) {
                                if (context != null) {
                                    setLog(
                                        "ChartLifecycle", "setArtImageBg--$artworkProminentColor"
                                    )
                                    changeStatusbarcolor(artworkProminentColor)

                                    binding.ivCollapsingImageBg.show()
                                    binding.ivCollapsingImageBg.background = artImage
                                    binding.fullGradient.show()
                                }
                            }

                        }
                        val gradient: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_gradient_drawable)
                        detailBgArtImageDrawable = LayerDrawable(arrayOf<Drawable>(bgColor, artImage, gradient!!))
                        binding.gamelistDetailroot.background = detailBgArtImageDrawable

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
            //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
            baseMainScope.launch {
                if (context != null) {
                    changeStatusbarcolor(
                        ContextCompat.getColor(
                            requireContext(), R.color.home_bg_color
                        )
                    )
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.GAME_RESULT_CODE && flagPause) {
            val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
            if (status == Constant.pause){
                (activity as MainActivity).resumePlayer()
                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
            }else{
                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return onPrepareOptionsMenu(menu)
    }

    private fun setUpPlaylistDetailListViewModel() {
        try {
            if (ConnectionUtil(activity).isOnline) {
                gameListViewModel = ViewModelProvider(
                    this
                ).get(GamelistViewModel::class.java)
                gameListViewModel?.getGamelistDetailList(
                    requireContext(), selectedContentId!!)?.observe(this, Observer {

                    when (it.status) {
                        Status.SUCCESS -> {

                            fillGameDetail(it?.data)
                        }

                        Status.LOADING -> {
                            setProgressBarVisible(false)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(
                                requireContext(), requireView(), true, it.message!!
                            )
                        }
                    }
                })
            }
        } catch (e: Exception) {

        }
    }

    private fun fillGameDetail(it: GamelistModel?) {
        playListModel = it
        if (isAdded && context != null) {
            artImageUrl = playListModel?.data?.head?.data?.image

            setArtImageBg(true)

            if (!playListModel?.data?.head?.data?.image_preview.isNullOrEmpty()) {

                imagePerViewDetails = playListModel?.data?.head?.data?.image_preview!!
                var mode = playListModel?.data?.head?.data?.mode!!

                gamelistAdpter = DetailGameAdapter(requireContext(), mode, imagePerViewDetails)


                binding.rvGamePreviewlist.setPadding(65, 0, 0, 0)

                binding.tvGamePreviewLabel.show()
                binding.rvGamePreviewlist.show()
                binding.rvGamePreviewlist.apply {

                    layoutManager = LinearLayoutManager(
                        activity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                binding.rvGamePreviewlist?.adapter = gamelistAdpter
            }

            if (CommonUtils.isUserHasGoldSubscription()) {
                gameurl = playListModel?.data?.head?.data?.details_play
            } else {
                if (!playListModel?.data?.head?.data?.attribute_details_play_ad?.isEmpty()!!) {
                    gameurl = playListModel?.data?.head?.data?.attribute_details_play_ad?.get(0)
                }
            }

            if (!playListModel?.data?.head?.data?.attribute_game_rating?.isEmpty()!!) {
                gameRating = playListModel?.data?.head?.data?.attribute_game_rating?.get(0)
            }

            binding.icPlay.setImageDrawable(requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite))

            binding.llPlayNow.setOnClickListener {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(),
                            binding.llPlayNow,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                    var newContentId= playListModel?.data?.head?.data?.id!!
                    var contentIdData = newContentId.replace("gamelist-","")

                    CoroutineScope(Dispatchers.IO).launch {
                        val hashMap = HashMap<String, String>()

                        hashMap.put(EventConstant.CONTENTID_EPROPERTY,""+contentIdData)
                        hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "Game")
                        hashMap.put(
                            EventConstant.GAME_NAME,
                            playListModel?.data?.head?.data?.title!!
                        )
                        hashMap.put(
                            EventConstant.CONTENT_TYPE_ID_EPROPERTY,
                            "" + playListModel?.data?.head?.data!!.type
                        )

                        hashMap.put(EventConstant.GAME_URL, "" + gameurl)
                        hashMap.put(
                            EventConstant.GAME_VIEW,
                            "" + playListModel?.data?.head?.data!!.mode
                        )

                        EventManager.getInstance().sendEvent(GameTriggerEvent(hashMap))
                    }

                    val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                    if (status == Constant.pause){
                        SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                        flagPause = false
                    }else{
                        SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                        (requireActivity() as MainActivity).pausePlayer()
                         flagPause = true
                    }
                    (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()


                    (activity as MainActivity).closePIPVideoPlayer()



                    val intent = Intent()
                    intent.setClass(requireContext(), CommonWebViewWithToolbarActivity::class.java)
                    intent.putExtra(Constant.EXTRA_URL, gameurl)
                    intent.putExtra(Constant.EXTRA_PAGE_DETAIL_NAME, playListModel?.data?.head?.data?.title)
                    intent.putExtra(Constant.defaultContentId, contentIdData)
                    intent.putExtra(Constant.MODE, playListModel?.data?.head?.data?.mode)
                    intent.putExtra(Constant.orderId, playListModel?.data?.head?.data!!.type.toString())
                    intent.putExtra(Constant.FLAG, gameurl)
                    Constant.isRedirected = true
  /*                  gameListViewModel?.sendMessage(gameurl.toString())
                    gameListViewModel?.EXTRA_PAGE_DETAIL_NAME?.value = playListModel?.data?.head?.data?.title.toString()
                 //   gameListViewModel?.defaultContentId?.value = contentIdData
                    gameListViewModel?.MODE?.value = playListModel?.data?.head?.data?.mode
                    gameListViewModel?.orderId?.value = playListModel?.data?.head?.data!!.type.toString()
                    gameListViewModel?.FLAG?.value = gameurl
*/
                    startActivityForResult(intent, Constant.GAME_RESULT_CODE)



                } catch (e: Exception) {

                }
            }

            if (requireArguments().containsKey("variant_images")) {
                variantImages = requireArguments().getStringArrayList("variant_images")
                if (variantImages != null && variantImages?.size!! > 0) {
                    if (!TextUtils.isEmpty(variantImages?.get(0))) {
                        artImageUrl = variantImages?.get(0)
                    }
                }
            }



            if (!TextUtils.isEmpty(artImageUrl)) {
                artImageUrl?.let { it1 ->
                    ImageLoader.loadImage(
                        requireContext(),
                        binding.playlistAlbumArtImageView,
                        it1,
                        R.drawable.ic_game_placeholder
                    )
                }
                staticToolbarColor()
            }


            if (it?.data?.head?.data?.title != null && !TextUtils.isEmpty(it.data.head.data.title)) {
                binding.tvTitle.text = it.data.head.data.title
            } else {
                binding.tvTitle.text = ""
            }

            binding.tvRating.text = gameRating

            if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.description?.trim())) {
                binding.tvReadMoreDescription.text = it?.data?.head?.data?.misc?.description
                SaveState.isCollapse = true
                binding.tvReadMoreDescription.setShowingLine(3)
                binding.tvReadMoreDescription.addShowMoreText("read more")
                binding.tvReadMoreDescription.addShowLessText("read less")
                binding.tvReadMoreDescription.setShowMoreColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorWhite
                    )
                )
                binding.tvReadMoreDescription.setShowLessTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorWhite
                    )
                )
                binding.tvReadMoreDescription.setShowMoreStyle(Typeface.BOLD)

                binding.tvReadMoreDescription.visibility = View.VISIBLE

            } else {
                binding.tvReadMoreDescription.visibility = View.GONE
            }

            binding.shimmerLayout.stopShimmer()
            binding.playlistDetailrootSub.show()
            binding.shimmerLayout.visibility = View.GONE
        }


    }

    override fun onDestroy() {
        baseMainScope.launch {
            if (context != null) {
                changeStatusbarcolor(
                    ContextCompat.getColor(
                        requireContext(), R.color.home_bg_color
                    )
                )
            }
        }
        super.onDestroy()
        setLog("ChartLifecycle", "onDestroy-")
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()

    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
    }

    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1


    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading)
    }


    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
    }

}