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
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.HeadItemsItem
import com.hungama.music.data.model.OnParentItemClickListener
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.music.data.model.RowsItem
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.fragment_brand_hub.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.HashMap

class BrandHubFragment : BaseFragment(), BucketParentAdapter.OnMoreItemClick,
    OnParentItemClickListener, BaseActivity.OnLocalBroadcastEventCallBack{
    var selectedContentId: String? = null
    var deeplink: String? = ""
    var artImageUrl:String? = null
    var artworkProminentColor = 0
    var adsId = ""
    private var chartDetailBgArtImageDrawable: LayerDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brand_hub, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun initializeComponent(view: View) {


        if(arguments?.containsKey(Constant.EXTRA_DEEPLINK)!!){
            deeplink=arguments?.getString(Constant.EXTRA_DEEPLINK)
        }

        callBrandhubAPI()

        ivBack2?.setOnClickListener { backPress() }
        threeDotMenu2?.visibility=View.INVISIBLE

        setLocalBroadcast()
        setLog(TAG, "onResume called: ")
        CommonUtils.setPageBottomSpacing(
            svBrndHub,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )

    }

    private fun callBrandhubAPI() {

        if (ConnectionUtil(activity).isOnline) {
            val viewModel = ViewModelProvider(
                this
            ).get(PlaylistViewModel::class.java)
            viewModel?.getBrandHubData(
                requireContext(),
                deeplink!!
            )
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                fillPlaylistData(it?.data!!)
                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
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
        }
    }

    private fun fillPlaylistData(playlistModel: PlaylistDynamicModel) {
        adsId = playlistModel.data.head.data.adsId
        setLog("brandhubStickyAdsId", "fillPlaylistData-adsId-$adsId")
        if (activity != null && !TextUtils.isEmpty(adsId)) {
            (activity as MainActivity).loadBottomAds(true, adsId)
        }
        artImageUrl = playlistModel?.data?.head?.data?.image

        setLog(TAG, "fillPlaylistData: artImageUrl:${playlistModel?.data?.head?.data?.image} " +
                "title:${playlistModel?.data?.head?.data?.title}"+
                "subTitle:${playlistModel?.data?.head?.data?.subTitle}"
        )

        if (!TextUtils.isEmpty(artImageUrl!!)){
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
        if(!TextUtils.isEmpty(playlistModel?.data?.head?.data?.title)){
            tvTitle.text = playlistModel?.data?.head?.data?.title
            tvActionBarHeading.text = playlistModel?.data?.head?.data?.title
        }else{
            tvTitle.text = ""
            tvActionBarHeading.text = ""
        }

        if(!TextUtils.isEmpty(playlistModel?.data?.head?.data?.subTitle)){
            tvPlay.text = playlistModel?.data?.head?.data?.subTitle
        }else{
            tvPlay.text = ""
        }

        setLog(TAG, "fillPlaylistData: size:${playlistModel.data?.body?.recomendation?.size}")


        if (playlistModel.data?.body?.recomendation != null && playlistModel.data?.body?.recomendation?.size!! > 0) {
            rvRecomendation.visibility = View.VISIBLE

            var varient = Constant.ORIENTATION_HORIZONTAL

            val bucketParentAdapter = BucketParentAdapter(
                playlistModel.data?.body?.recomendation!!,
                requireContext(),
                this@BrandHubFragment,
                this,
                Constant.DISCOVER_TAB,
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


            bucketParentAdapter?.addData(playlistModel.data?.body?.recomendation!!)

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
                        var fromBucket =
                            playlistModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                        var toBucket =
                            playlistModel.data?.body?.recomendation?.get(lastVisiable)?.heading
                        var sourcePage =
                            MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                        if (!fromBucket?.equals(toBucket, true)!!) {
                            callPageScrolledEvent(
                                sourcePage,
                                "" + lastVisiable,
                                fromBucket!!,
                                toBucket!!
                            )
                        }

                    }
                }
            })
            rvRecomendation.setPadding(0, 0, 0, 0)
        }
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        CoroutineScope(Dispatchers.IO).launch {
            val dataMap= HashMap<String,String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
            dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ PlaylistDetailFragmentDynamic.playlistRespModel?.data?.head?.data?.title)

            dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "_" + parent.heading)
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

    fun URL.toBitmap(): Bitmap?{
        return try {
            BitmapFactory.decodeStream(openStream())
        }catch (e: IOException){
            null
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setLog(TAG, " called: hidden:${hidden}")
        if(!hidden){
            if (isAdded){
                setLog("brandhubStickyAdsId", "onHiddenChanged-true-1-adsId-$adsId")
                if (activity != null && !TextUtils.isEmpty(adsId)) {
                    setLog("brandhubStickyAdsId", "onHiddenChanged-true-2-adsId-$adsId")
                    (activity as MainActivity).loadBottomAds(true, adsId)
                }
                svBrndHub?.postDelayed(Runnable {
                    if (isAdded){
                        setLog(TAG, "hidden: miniplayerHeight${(context as MainActivity).miniplayerHeight}")
                        var bottomSpace=resources.getDimensionPixelSize(R.dimen.dimen_0)

                        setLog(TAG, "hidden: bottomSpace${bottomSpace} miniplayerHeight${(context as MainActivity).miniplayerHeight}")
                        CommonUtils.setPageBottomSpacing(
                            svBrndHub,
                            requireContext(),
                            resources.getDimensionPixelSize(R.dimen.dimen_0),
                            resources.getDimensionPixelSize(R.dimen.dimen_0),
                            resources.getDimensionPixelSize(R.dimen.dimen_0),
                            bottomSpace,
                        )
                    }
                },4000)
            }
        }else{
            if (isAdded) {
                setLog("brandhubStickyAdsId", "onHiddenChanged-false-1-adsId-$adsId")
                if (activity != null && !TextUtils.isEmpty(adsId)) {
                    setLog("brandhubStickyAdsId", "onHiddenChanged-false-2-adsId-$adsId")
                    (activity as MainActivity).loadBottomAds()
                }
            }
        }
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            setLog(TAG, "onLocalBroadcastEventCallBack: ")
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(svBrndHub, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setLog("brandhubStickyAdsId", "onDestroy-1-adsId-$adsId")
        if (activity != null && !TextUtils.isEmpty(adsId)) {
            setLog("brandhubStickyAdsId", "onDestroy-2-adsId-$adsId")
            (activity as MainActivity).loadBottomAds()
        }
    }
}