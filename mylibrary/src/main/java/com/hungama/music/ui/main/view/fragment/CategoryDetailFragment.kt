package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.media3.common.util.Util
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.CategoryClickedEvent
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.CategoryParentAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.CategoryViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.ORIENTATION_HORIZONTAL
import com.hungama.music.utils.Constant.ORIENTATION_VERTICAL
import com.hungama.music.utils.Constant.PLAY_CONTEXT_TYPE
import com.hungama.music.utils.Constant.SELECTED_TRACK_POSITION
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_selected_list.*
import kotlinx.coroutines.*
import java.io.*
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class CategoryDetailFragment(val contentId: String, val type: Int?) : BaseFragment(),
    OnParentItemClickListener, TracksContract.View, CategoryParentAdapter.OnMoreItemClick, ViewTreeObserver.OnScrollChangedListener,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var categoryViewModel: CategoryViewModel? = null

    var bucketRespModel: HomeModel? = null
    private var categoryParentAdapter: CategoryParentAdapter? = null
    private var parentPos: Int = 0
    var rowList: MutableList<RowsItem?>? = null
    var songsList = ArrayList<MusicModel>()
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    private lateinit var tracksViewModel: TracksContract.Presenter


    var playableItem = RowsItem()
    var playableItemPosition = 0
    var heading: String = ""
    var artImageUrl:String? = ""; //""https://files.hubhopper.com/podcast/313123/storytime-with-gurudev-sri-sri-ravi-shankar.jpg?v=1598432706&s=hungama"
    var artWorkImageColor = 0
    var CONTENT_TYPE = 0
    var categoryName = ""
    var categoryId = ""
    val displayMetrics = DisplayMetrics()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_selected_list, container, false)
    }

    /**
     * initialise view model and setup-observer
     */
    private fun setUpViewModel() {
        try {
          if (isAdded && context != null){
              if (ConnectionUtil(activity).isOnline) {

                  categoryViewModel = ViewModelProvider(
                      this
                  ).get(CategoryViewModel::class.java)

                  setLog("arg","category before category:${categoryName} type:${type}")

                  var url=""
                  var category = categoryName
                  if(category.isEmpty()){
                      if (type == 10){
                          category = "genre"
                      }else if (type == 15){
                          category = "mood"
                      }else{
                          category = "genre"
                      }
                  }

                  setLog("arg","category after category:${category} type:${type}")
                  val userSettingRespModelMusic= SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
                  var musicLang = ""
                  if(userSettingRespModelMusic!=null && userSettingRespModelMusic?.data != null){
                      musicLang = userSettingRespModelMusic?.data?.data?.get(0)?.preference?.get(0)?.music_language_preference!!
                  }
                  CommonUtils.setLog("deepLinkUrl", "CategoryDetailFragment-setUpViewModel--categoryName=$categoryName && categoryId=$categoryId && contentId=$contentId")
                  //setLog(TAG, "setUpViewModel: $heading")
                  //if(!TextUtils.isEmpty(heading)){
//                  url= WSConstants.METHOD_DETAIL_CONTENT+contentId+"/category/detail"+"?lang="+ SharedPrefHelper.getInstance().getLanguage()+"&category="+category+"&music-lang="+musicLang+"&video-lang=hi,en"
                  url= WSConstants.METHOD_DETAIL_CONTENT+contentId+"/category/detail"+"?lang="+ SharedPrefHelper.getInstance().getLanguage()+"&category="+category //+" &music-lang="+musicLang+"&video-lang=hi,en&uid="+user_id
                  setLog(TAG, "setUpViewModel url: $url")
                  /*}else{
      //                url= "http://35.200.196.46:3000/v2/app/home?lang="+ SharedPrefHelper.getInstance().getLanguage()
                  }*/


                  categoryViewModel?.getSelectedListData(requireActivity(),url)?.observe(this,
                      Observer {
                          when(it.status){
                              Status.SUCCESS->{
                                  setProgressBarVisible(false)
                                  setData(it?.data!!)
                                  setHeader(it?.data!!)
                              }

                              Status.LOADING ->{
                                  setProgressBarVisible(false)
                              }

                              Status.ERROR ->{
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
        }catch (e:Exception){

        }
    }





    override fun initializeComponent(view: View) {
        baseMainScope.launch {

            setLog("arg","category arg:${arguments}")
            if (arguments != null){
                if (arguments?.containsKey(Constant.heading)!!){
                    heading = requireArguments().getString(Constant.heading).toString()
                }
                if (arguments?.containsKey(Constant.EXTRA_CATEGORY_NAME)!!){
                    categoryName = arguments?.getString(Constant.EXTRA_CATEGORY_NAME)!!
                }
                if (arguments?.containsKey(Constant.EXTRA_CATEGORY_ID)!!){
                    categoryId = arguments?.getString(Constant.EXTRA_CATEGORY_ID)!!
                }
            }

            ivBack?.setOnClickListener { backPress() }
            headBarBlur?.visibility = View.INVISIBLE
            displayMetrics?.let {
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            }

            shimmerLayout?.visibility = View.VISIBLE
            shimmerLayout?.startShimmer()
            setUpViewModel()

            scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@CategoryDetailFragment)
            CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@CategoryDetailFragment)
        }
    }

    override fun onScrollChanged() {
        if (isAdded) {
            // get the maximum height which we have scroll before performing any action
            var maxDistance = 0

            //maxDistance +=  resources.getDimensionPixelSize(R.dimen.dimen_250)
            maxDistance += ivHeader.height + ivHeader.marginTop - ivHeader.marginBottom - tvHeaderTitle.height - tvHeaderTitle.marginTop - tvHeaderTitle.marginBottom - resources.getDimensionPixelSize(R.dimen.dimen_5)
            // how much we have scrolled
            val movement = scrollView.scrollY
            setLog("OnNestedScroll-m", movement.toString())
            setLog("OnNestedScroll-d", maxDistance.toString())

            if (movement >= maxDistance){
                headBarBlur.visibility = View.GONE
                tvActionBarHeading.visibility = View.VISIBLE
                rlActionBarHeader.setBackgroundColor(artWorkImageColor)
                tvHeaderTitle.visibility = View.GONE
            }else{
                headBarBlur.visibility = View.GONE
                tvHeaderTitle.visibility = View.VISIBLE
                tvActionBarHeading.visibility = View.GONE
                rlActionBarHeader.setBackgroundColor(0)
            }
        }
    }

    private fun setData(it: HomeModel) {
        bucketRespModel = it
        baseMainScope.launch {
            if (isAdded && context != null){
                if (bucketRespModel != null && bucketRespModel?.data?.body != null) {
            var varient = ORIENTATION_VERTICAL
            if (bucketRespModel?.data?.body?.rows?.size!! > 1){
                varient = ORIENTATION_HORIZONTAL
            }

            categoryParentAdapter = CategoryParentAdapter(
                bucketRespModel?.data?.body?.rows!!, requireContext(), this@CategoryDetailFragment, this@CategoryDetailFragment, varient
            )

            rvRecentHistory?.layoutManager =
                LinearLayoutManager(
                    activity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            //rvMain?.applyTopBarAndBottomBarInsetsCustom(0, resources.getDimensionPixelSize(R.dimen.dimen_142), requireContext())
            rvRecentHistory?.adapter = categoryParentAdapter
            rowList = (bucketRespModel?.data?.body?.rows as MutableList<RowsItem?>?)!!

            categoryParentAdapter?.addData(rowList!!)
            rvRecentHistory?.visibility=View.VISIBLE
            rvRecentHistory?.invalidate()
//            setEmptyVisible(false)
        }
        shimmerLayout?.visibility = View.GONE
        shimmerLayout?.stopShimmer()
            }
        }
    }

    private fun setHeader(it: HomeModel) {
        baseMainScope.launch {
            if (isAdded && context != null){
                tvHeaderTitle?.text = it.data?.head?.data?.title
        tvActionBarHeading?.text = it.data?.head?.data?.title

        val artImageUrl = it.data?.head?.data?.image
        if (!TextUtils.isEmpty(artImageUrl)){
            ImageLoader.loadImage(
                requireContext(),
                ivHeader,
                artImageUrl!!,
                R.drawable.bg_gradient_placeholder
            )
            setArtImageBg(true, artImageUrl)
        }else{
            ImageLoader.loadImage(
                requireContext(),
                ivHeader,
                "",
                R.drawable.bg_gradient_placeholder
            )
//            setArtImageBg(false, "")
        }
        shimmerLayout?.visibility = View.GONE
        shimmerLayout?.stopShimmer()
            }
        }
    }
    fun setArtImageBg(status: Boolean, artImageUrl: String) {
        baseIOScope.launch {
            if (activity!=null&& artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(artImageUrl)) {
                val result: Deferred<Bitmap?> = baseIOScope.async {
                    val urlImage = URL(artImageUrl)
                    urlImage.toBitmap()
                }

                baseIOScope.launch {
                    // get the downloaded bitmap
                    val bitmap: Bitmap? = result.await()
                    try {
                        val artImage = BitmapDrawable(resources, bitmap)
                        if (status) {
                            if (bitmap != null) {
                                artWorkImageColor = CommonUtils.calculateAverageColor(bitmap, 1)
                                Palette.from(bitmap!!).generate { palette ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        baseMainScope.launch {
                                            if (context != null) {
                                                CommonUtils.setLog(
                                                    "CategoryLifecycle",
                                                    "setArtImageBg--$artWorkImageColor"
                                                )
                                                changeStatusbarcolor(artWorkImageColor)
                                            }
                                        }
                                    }
                                }



                                baseMainScope.launch {
                                    val pd = PaintDrawable()
                                    withContext(Dispatchers.IO) {
                                        val sf: ShapeDrawable.ShaderFactory = object : ShapeDrawable.ShaderFactory() {
                                            override fun resize(width: Int, height: Int): Shader? {
                                                return LinearGradient(
                                                    (width / 2).toFloat(),
                                                    (0).toFloat(),
                                                    (width / 2).toFloat(),
                                                    (height).toFloat(),
                                                    intArrayOf(getTransparentColor(artWorkImageColor),
                                                        artWorkImageColor,
                                                        Color.parseColor("#00282828")),
                                                    floatArrayOf(
                                                        0f,
                                                        0.13f,
                                                        1f
                                                    ),  // start, center and end position
                                                    Shader.TileMode.CLAMP
                                                )
                                            }
                                        }


                                        pd.shape = RectShape()
                                        pd.shaderFactory = sf
                                        withContext(Dispatchers.Main){
                                            bgMain?.background = pd
                                        }
                                    }
                                }

                                /*val gd = GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    intArrayOf(getTransparentColor(artWorkImageColor), artWorkImageColor, Color.parseColor("#00282828"))
                                )
                                gd.cornerRadius = 0f
                                bgMain.setBackgroundDrawable(gd);*/
                                ivHeader?.visibility = View.VISIBLE
                            }

                        }else{
                            (activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


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

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        setLog("CategoryDetailPage", "CategoryDetailPage-onParentItemClick")
        setEventModelDataAppLevel(parent.items?.get(childPosition)?.data?.id!!, parent.items?.get(childPosition)?.data?.title!!, parent?.heading!!)

        if (parent.items!!.get(childPosition)?.data?.type!!.equals("93",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("4",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("65",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("66",true)) {

            val bundle = Bundle()
            bundle.putString("image", parent.items?.get(childPosition)!!.data!!.image)
            bundle.putString("id", parent.items?.get(childPosition)!!.data!!.id)
            bundle.putString("playerType", parent.items?.get(childPosition)!!.data!!.type!!)
            bundle.putBoolean("varient", true)
            /*if (childPosition % 2 == 0)
                bundle.putBoolean("varient", true)
            else
                bundle.putBoolean("varient", false)*/

            //val transaction = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            var varient = 1
            if (!TextUtils.isEmpty(parent?.items?.get(childPosition)?.data?.variant)){
                if (parent?.items?.get(childPosition)?.data?.variant.equals("v2", true)){
                    varient = 2
                }
            }
            val movieDetailsFragment = MovieV1Fragment(varient)
            movieDetailsFragment.arguments = bundle
            /*transaction.replace(R.id.fl_container, podcastDetailsFragment)
            transaction.addToBackStack(null)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.commit()*/
            addFragment(R.id.fl_container, this, movieDetailsFragment, false)

        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("96",true) || parent.items!!.get(childPosition)?.data?.type!!.equals("97",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("98",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("102",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("107",true)) {
            val bundle = Bundle()
            bundle.putString("image", parent.items?.get(childPosition)!!.data!!.image)
            bundle.putString("id", parent.items?.get(childPosition)!!.data!!.id)
            bundle.putParcelable("child_item", parent.items?.get(childPosition))
            bundle.putString("playerType", parent.items?.get(childPosition)!!.data!!.type!!)
            if (childPosition % 2 == 0)
                bundle.putBoolean("varient", true)
            else
                bundle.putBoolean("varient", false)

            var varient = 1
            if (!TextUtils.isEmpty(parent?.items?.get(childPosition)?.data?.variant)){
                if (parent?.items?.get(childPosition)?.data?.variant.equals("v2", true)){
                    varient = 2
                }
            }
            val tvShowDetailsFragment = TvShowDetailsFragment(varient)
            tvShowDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, tvShowDetailsFragment, false)

        }else if(parent.items!!.get(childPosition)?.data?.type!!.equals("22",true) || parent.items!!.get(childPosition)?.data?.type!!.equals("88888",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("53",true)){
            if (activity != null && activity is MainActivity){
                (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
            }


            val bundle = Bundle()
            bundle.putString("id", parent.items!!.get(childPosition)?.data?.id)

            val videoDetailsFragment = MusicVideoDetailsFragment()
            videoDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, videoDetailsFragment, false)
        }else if(parent.items!!.get(childPosition)?.data?.type!!.equals("22",true)){
            val songsList = CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
            val serviceBundle = Bundle()
            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
            serviceBundle.putString(Constant.SELECTED_CONTENT_ID,parent.items!!.get(childPosition)!!.data!!.id)
            serviceBundle.putInt(Constant.TYPE_ID, parent.items!!.get(childPosition)!!.data!!.type!!.toInt())
            intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
            intent.putExtra("thumbnailImg", parent.items!!.get(childPosition)!!.data!!.image)
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
        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("55555",true)) {
            //val intent = Intent(requireActivity(), ChartDetails::class.java)
            //startActivity(intent)

            val bundle = Bundle()
            bundle.putString("image", parent.items!!.get(childPosition)!!.data!!.image)
            bundle.putString("id", parent.items?.get(childPosition)!!.data!!.id)
            bundle.putString("playerType", parent.items?.get(childPosition)!!.data!!.type!!)
            if(parent.items?.get(childPosition)?.data?.images!=null&&parent.items!!.get(childPosition)?.data?.images?.size!!>0){
                bundle.putStringArrayList("imageArray",
                    parent.items?.get(childPosition)?.data?.images as java.util.ArrayList<String>?
                )
            }

            if(parent.items!!.get(childPosition)!!.data?.variant_images!=null&&parent.items!!.get(childPosition)!!.data?.variant_images?.size!!>0){
                bundle.putStringArrayList("variant_images",
                    parent.items!!.get(childPosition)!!.data?.variant_images as java.util.ArrayList<String>?
                )
            }
            var varient = 1
            if (!TextUtils.isEmpty(parent?.items?.get(childPosition)?.data?.variant)){
                if (parent?.items?.get(childPosition)?.data?.variant.equals("v2", true)){
                    varient = 2
                }
            }
            val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
            playlistDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, playlistDetailFragment, false)

        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("110",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("77777",true)||parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)) {
            if (parent.items!!.get(childPosition)?.data?.type!!.equals("21",true) || parent.items!!.get(childPosition)?.data?.type!!.equals("34",true)){
                playableItem = parent
                playableItemPosition = childPosition
                getPlayableContentUrl(parent.items?.get(childPosition)?.data?.id!!)
                BaseActivity.setTouchData()
            }else {
                val songData =
                    bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.items!!.get(
                        childPosition
                    )!!.data
                val allSongs = bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.items!!
                val heading = bucketRespModel?.data?.body!!.rows!!.get(parentPosition)!!.heading
                CommonUtils.setTrackList(
                    getApplicationContext(),
                    songData,
                    allSongs,
                    heading,
                    childPosition
                )
                tracksViewModel.prepareTrackPlayback(0)
            }
        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("1",true)) {
            val bundle = Bundle()
            bundle.putString("image", parent.items!!.get(childPosition)!!.data!!.image)
            bundle.putString("id", parent.items?.get(childPosition)!!.data!!.id)
            bundle.putString("playerType", parent.items?.get(childPosition)!!.data!!.type!!)

            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, albumDetailFragment, false)

        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("33",true)){
            setLog("DataTape","Type${type}")
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_MOOD_RADIO
            getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.moodid!!,
                Constant.CONTENT_MOOD_RADIO
            )

        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("35",true)){
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_ON_DEMAND_RADIO
            getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.id!!,
                Constant.CONTENT_ON_DEMAND_RADIO
            )
        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("36",true)){
            playableItem = parent
            playableItemPosition = childPosition
            CONTENT_TYPE = Constant.CONTENT_ARTIST_RADIO
            getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.id!!,
                Constant.CONTENT_ARTIST_RADIO
            )
        }else if (parent.items!!.get(childPosition)?.data?.type!!.equals("19",true)) {
            //val intent = Intent(requireActivity(), ChartDetails::class.java)
            //startActivity(intent)

            val bundle = Bundle()
            bundle.putString("image", parent.items!!.get(childPosition)!!.data!!.image)
            if(parent.items!!.get(childPosition)!!.data?.images!=null&&parent.items!!.get(childPosition)!!.data?.images?.size!!>0){
                bundle.putStringArrayList("imageArray",
                    parent.items!!.get(childPosition)!!.data?.images as java.util.ArrayList<String>?
                )
            }

            if(parent.items!!.get(childPosition)!!.data?.variant_images!=null&&parent.items!!.get(childPosition)!!.data?.variant_images?.size!!>0){
                bundle.putStringArrayList("variant_images",
                    parent.items!!.get(childPosition)!!.data?.variant_images as java.util.ArrayList<String>?
                )
            }

            bundle.putString("id", parent.items?.get(childPosition)!!.data!!.id)
            bundle.putString("playerType", parent.items?.get(childPosition)!!.data!!.type!!)
            var varient = 1
            if (!TextUtils.isEmpty(parent?.items?.get(childPosition)?.data?.variant)){
                if (parent?.items?.get(childPosition)?.data?.variant.equals("v2", true)){
                    varient = 2
                }
            }
            val chartDetailFragment = ChartDetailFragment.newInstance(varient)
            chartDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, chartDetailFragment, false)

        } else if (parent.items?.get(childPosition)!!.itype == 10 || parent.items?.get(childPosition)!!.itype == 15){
            val bundle = Bundle()
            bundle.putString("heading", parent.heading)
            val selectedListFragment = CategoryDetailFragment(
                parent.items?.get(childPosition)?.data?.id!!.toString(),
                parent?.type
            )
            selectedListFragment.arguments = bundle
            addFragment(R.id.fl_container, this, selectedListFragment, false)

            CoroutineScope(Dispatchers.IO).async {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.TYPE_EPROPERTY,""+parent.items?.get(childPosition)?.data?.type)
//                hashMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+Utils.getContentTypeName(parent.items?.get(childPosition)?.data?.type!!))
                hashMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,categoryName)
                hashMap.put(EventConstant.CATEGORYNAME_EPROPERTY,""+parent.items?.get(childPosition)?.data?.title)
                hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+parent?.heading)

                EventManager.getInstance().sendEvent(CategoryClickedEvent(hashMap))
            }


        } else {
            onItemDetailPageRedirection(parent,parentPosition,childPosition, "_" + parent.heading)
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

    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(SELECTED_TRACK_POSITION, selectedTrackPosition)
        if(trackPlayStartPosition>0){
            intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        }
        intent.putExtra(PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }


    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

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
                                    if (playableItemPosition < playableItem?.items?.size!!) {
                                        if (CONTENT_TYPE == Constant.CONTENT_MOOD_RADIO || CONTENT_TYPE == Constant.CONTENT_ON_DEMAND_RADIO || CONTENT_TYPE == Constant.CONTENT_ARTIST_RADIO) {
                                            getPlayableContentUrl(moodRadioListRespModel?.get(playableItemPosition)?.data?.id!!)
                                        } else {
                                            getPlayableContentUrl(playableItem.items?.get(playableItemPosition)?.data?.id!!)
                                        }
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

    /**
     * get Playable url for song : 33, 35
     *
     * @param id
     */
    private fun getPlayableMoodRadioList(id:String, type:Int){
        if (ConnectionUtil(context).isOnline) {
            setLog("MYTType","Type${type}")
            if (type == Constant.CONTENT_MOOD_RADIO){
                playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }else if (type == Constant.CONTENT_ARTIST_RADIO){
                playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }else {
                playableContentViewModel?.getOnDemandRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }

        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    var moodRadioListRespModel: MoodRadioListRespModel?=null


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {
        try {
            baseIOScope.launch {
                if (isAdded && context != null){
                    if (playableContentModel != null ) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()


            if(playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_MOOD_RADIO, true) || playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ON_DEMAND_RADIO, true)|| playableItem?.items?.get(playableItemPosition)?.data?.type!!.equals(Constant.PLAYER_ARTIST_RADIO, true)){
                for (i in moodRadioListRespModel?.indices!!){

                    if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(i)?.data?.id){
                        setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }else if(i > playableItemPosition){
                        setRadioDataList(null, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }

                }
                BaseActivity.setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(0)
            }else{
                for (i in playableItem.items!!.indices){

                    if (playableContentModel?.data?.head?.headData?.id == playableItem.items?.get(i)?.data?.id){
                        setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                    }else if(i > playableItemPosition){
                        setPlayableDataList(null, playableItem, i)
                    }

                }
                BaseActivity.setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(0)
            }


        }
                }
            }
        }catch (e:Exception){

        }
    }

    var songDataList:ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: RowsItem,
        position:Int
    ) {
        val track: Track = Track()
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
            track.playerType = ""
        }
        if (activity != null && !TextUtils.isEmpty(playableItem.items?.get(position)?.data?.type)) {
            val playType = (activity as MainActivity).getPlayerType(track.playerType)
            if (playType == Constant.CONTENT_PODCAST){
                track.contentType = ContentTypes.PODCAST.value
            }else if (playType == Constant.CONTENT_MUSIC){
                track.contentType = ContentTypes.AUDIO.value
            }
        }
        if (!TextUtils.isEmpty(playableItem.heading)){
            track.heading = playableItem.heading
        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.playble_image)){
            track.image = playableItem.items?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem.items?.get(position)?.data?.image)){
            track.image = playableItem.items?.get(position)?.data?.image
        }else{
            track.image = ""
        }

        track.pType = DetailPages.CATEGORY_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

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

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: RowsItem
    ) {
        val track: Track = Track()


        if (!TextUtils.isEmpty(playableItem?.data?.id)){
            track.id = playableItem?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.data?.title)){
            track.title = playableItem?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.subtitle)){
            track.subTitle = playableItem?.data?.subtitle
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

        if (!playableItem?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }


        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }
        if (!TextUtils.isEmpty(playableItem1.items?.get(position)?.data?.type)){
            track.playerType = playableItem1.items?.get(position)?.data?.type
        }else{
            track.playerType = Constant.PLAYER_RADIO
        }
        if (!TextUtils.isEmpty(playableItem1.heading)){
            track.heading = playableItem1.heading
        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem1.items?.get(position)?.data?.playble_image)){
            track.image = playableItem1.items?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }

        track.pType = DetailPages.CATEGORY_DETAIL_PAGE.value
        track.contentType = ContentTypes.RADIO.value

        if (playableItem1.items?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem1.items?.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem1.items?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem1.items?.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem1.items?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem1.items?.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?) {
        baseMainScope.launch {
            if (isAdded && context != null){
                //setLog("MoreItemHeading", heading)
                val bundle = Bundle()
                bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
                val moreBucketListFragment = MoreBucketListFragment()
                moreBucketListFragment.arguments = bundle
                addFragment(R.id.fl_container, this@CategoryDetailFragment, moreBucketListFragment, false)

                CoroutineScope(Dispatchers.IO).async {
                    val dataMap = HashMap<String, String>()
                    dataMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + selectedMoreBucket?.heading)
                    dataMap.put(
                        EventConstant.CONTENT_TYPE_EPROPERTY,
                        "" + bucketRespModel?.data?.head?.data?.title
                    )
                    dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,
                        MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))

                    EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
                }

            }
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            baseMainScope.launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "CategoryLifecycle",
                        "onHiddenChanged-$hidden--$artWorkImageColor"
                    )
                    changeStatusbarcolor(artWorkImageColor)
                }
            }
        } else {
            baseMainScope.launch {
                if (context != null) {
                    changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
                }
            }
        }
    }

    private fun getTransparentColor(color: Int): Int {
        var alpha = Color.alpha(color)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        // Set alpha based on your logic, here I'm making it 25% of it's initial value.
        alpha *= 0.25.toInt()
        return Color.argb(alpha, red, green, blue)
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
}