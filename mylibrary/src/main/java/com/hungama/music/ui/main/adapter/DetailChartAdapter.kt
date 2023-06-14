package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.fontmanger.FontDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class DetailChartAdapter(
    val ctx: Context,
    val list: List<PlaylistModel.Data.Body.Row>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var adLoader: AdLoader? = null
    // simple boolean to check the status of ad
    private var adLoaded = false
    private var isVideoAd = false

    private inner class IType1000ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivDownload:ImageView = itemView.findViewById(R.id.ivDownload)
        val ivExplicit:ImageView = itemView.findViewById(R.id.ivE)
        val vNowPlaying:View = itemView.findViewById(R.id.vNowPlaying)
        val rlFreeStrip: RelativeLayout = itemView.findViewById(R.id.rlFreeStrip)

        val ivEqualizer:ImageView = itemView.findViewById(R.id.ivEqualizer)
        val ivEqualizerAnim: LottieAnimationView = itemView.findViewById(R.id.ivEqualizerAnim)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty()){
                val list = list[position].data

                if (list.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }


                if (list.releasedate != null) {
                    tvSubTitle.text = list.subtitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                CommonUtils.setExplicitContent(ctx, llMain, list.misc.explicit, ivExplicit)

                if(!CommonUtils.isUserHasGoldSubscription() && list.misc.movierights.contains("AMOD") && CommonUtils.getSongDurationConfig().enable_minutes_quota)
                    rlFreeStrip.show() else  rlFreeStrip.hide()

                if (list.misc.chart.indicator != null) {
                    when (list.misc.chart.indicator) {
                        -1 -> {
                            val drawable = FontDrawable(ctx, R.string.icon_down_song)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.color_chart_song_down))
                            ivStatus.setImageDrawable(drawable)
                            ivStatus.visibility=View.VISIBLE
                        }
                        0 -> {
                            val drawable = FontDrawable(ctx, R.string.icon_constant_song)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.font_color_sub_heading))
                            ivStatus.setImageDrawable(drawable)
                            ivStatus.visibility=View.VISIBLE
                        }
                        1 -> {
                            val drawable = FontDrawable(ctx, R.string.icon_up_song)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.green))
                            ivStatus.setImageDrawable(drawable)
                            ivStatus.visibility=View.VISIBLE
                        }
                        2 -> {
                            val drawable = FontDrawable(ctx, R.string.icon_new_song)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.color_chart_song_new))
                            ivStatus.setImageDrawable(drawable)
                            ivStatus.visibility=View.VISIBLE
                        }
                        else -> {
                            ivStatus.visibility=View.INVISIBLE
                        }
                    }
                }else{
                    ivStatus.visibility=View.INVISIBLE
                }

                val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
                ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivMore.setImageDrawable(ivMoreDrawable)

                val drawable = FontDrawable(ctx, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)

                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(list.id)
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(list.id)
                if (downloadQueue != null) {
                    if (downloadQueue.parentId != null && !TextUtils.isEmpty(downloadQueue.parentId)) {
                        downloadIconStates(downloadQueue.downloadStatus, ivDownload)
                    }
                   /* if (!CommonUtils.isUserHasGoldSubscription() && CommonUtils.getFirebaseConfigAdsData().drawerDownloadMymusic.nonft != null && Constant.DEFAULT_COUNTRY_CODE.equals(
                            "IN",
                            true
                        )
                    ) {
                        rlGoldStrip.hide()
                    } else rlGoldStrip.hide()*/
                }

                if (downloadedAudio != null){
                    if (downloadedAudio.parentId != null && !TextUtils.isEmpty(downloadedAudio.parentId)){
                        if (downloadedAudio.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(downloadedAudio.downloadedFilePath)){
                            try {
                                val fileName = downloadedAudio.downloadedFilePath
                                val file = File(fileName)
                                if (file.exists()){
                                    downloadIconStates(downloadedAudio.downloadStatus, ivDownload)
                                }else{
                                    AppDatabase.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(
                                        downloadedAudio.contentId!!
                                    )
                                    downloadIconStates(0, ivDownload)
                                }
                            }catch (e:Exception){

                            }catch (fileNotFound:FileNotFoundException){
                                downloadIconStates(0, ivDownload)
                            }
                       /*     if(!CommonUtils.isUserHasGoldSubscription() && downloadedAudio?.gold_flag==1 && Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
                                rlGoldStrip.hide()
                            }else  rlGoldStrip.hide()*/

                        }
                    }
                }

                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        list.image!!,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                if (list.isCurrentPlaying){
                    vNowPlaying.show()
                    //ivEqualizer?.show()
                    ivEqualizerAnim.show()
                    ivEqualizerAnim.playAnimation()
                }else{
                    vNowPlaying.hide()
                    ivEqualizer.hide()
                    ivEqualizerAnim.hide()
                    ivEqualizerAnim.cancelAnimation()
                    ivEqualizerAnim.progress = 0f
                }

                llMain.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)){
                        onChildItemClick?.onUserClick(position,
                            isMenuClick = false,
                            isDownloadClick = false
                        )
                    }
                }

                ivMore.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)){
                        onChildItemClick?.onUserClick(position, isMenuClick = true, isDownloadClick = false)
                    }
                }

                ivDownload.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)){
                        onChildItemClick?.onUserClick(position, isMenuClick = false, isDownloadClick = true)
                        if(CommonUtils.isUserHasGoldSubscription()){
                            val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                            ivDownload.setImageDrawable(drawable)
                            setLog(TAG, "bind: download img change")
                        }
                    }
                }
            }
        }
    }

    private inner class IType1011ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        val nativeTemplateView: TemplateView = itemView.findViewById(R.id.nativeTemplateView)

        fun bind(position: Int) {
            if (!list.isNullOrEmpty()){
                val list = list[position]
                if (CommonUtils.isDisplayAds()){
                    nativeTemplateView.visibility = View.GONE
                    clMain.setPadding(0,0,0,0)
                    setLog("MyApp", "onAdLoaded called")
                    initNativeAds(clMain, nativeTemplateView, ctx, list)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == Constant.playlistNativeAds){
            IType1011ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.row_charts_detail_ads, parent, false))
        }else{
            IType1000ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.row_chart_detail_v4, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!list.isNullOrEmpty()){
            if (list[position].itype == Constant.playlistNativeAds){
                (holder as IType1011ViewHolder).bind(position)
            }else{
                (holder as IType1000ViewHolder).bind(position)
            }
        }else{
            (holder as IType1000ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].itype
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isMenuClick:Boolean, isDownloadClick:Boolean)
    }

    private fun downloadIconStates(status: Int, ivDownload: ImageView){
        when (status){
            Status.NONE.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
            Status.QUEUED.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
            Status.DOWNLOADING.value ->{
                val drawable = FontDrawable(ctx, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
            Status.COMPLETED.value ->{
                val drawable = FontDrawable(ctx, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }
        }
    }

    private fun initNativeAds(
        clMain: ConstraintLayout,
        nativeTemplateView: TemplateView,
        ctx: Context,
        list: PlaylistModel.Data.Body.Row
    ) {
//        CoroutineScope(Dispatchers.IO).launch {
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

            adLoader = AdLoader.Builder(ctx,
                list.adUnitId
            ).forNativeAd(object : NativeAd.OnNativeAdLoadedListener{
                private val background: ColorDrawable? = null
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    val styles =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(background).build()

                    nativeTemplateView.setStyles(styles)
                    nativeTemplateView.setNativeAd(nativeAd)

                    //setLog("MyApp", "Video nativeAd:${nativeAd}")

                    adLoaded = true

                    if (nativeAd.mediaContent != null){
                        isVideoAd = nativeAd.mediaContent?.hasVideoContent()!!
                        if (isVideoAd){

                            nativeAd.mediaContent?.videoController?.videoLifecycleCallbacks =
                                object : VideoController.VideoLifecycleCallbacks() {
                                    /** Called when video playback first begins.  */
                                    override fun onVideoStart() {
                                        // Do something when the video starts the first time.
                                        setLog("MyApp", "Video Started")
                                    }

                                    /** Called when video playback is playing.  */
                                    override fun onVideoPlay() {
                                        // Do something when the video plays.
                                        setLog("MyApp", "Video Played")

                                    }

                                    /** Called when video playback is paused.  */
                                    override fun onVideoPause() {
                                        // Do something when the video pauses.
                                        setLog("MyApp", "Video Paused")
                                    }

                                    /** Called when video playback finishes playing.  */
                                    override fun onVideoEnd() {
                                        // Do something when the video ends.
                                        setLog("MyApp", "Video Ended")

                                    }

                                    /** Called when the video changes mute state.  */
                                    override fun onVideoMute(isMuted: Boolean) {
                                        // Do something when the video is muted.
                                        setLog("MyApp", "Video Muted")
                                    }
                                }
                        }
                    }
                }
            }).withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (adLoaded) {
                        setLog("MyApp", "onAdLoaded")
                        nativeTemplateView.show()
                         clMain.setPadding(0,
                             ctx.resources.getDimensionPixelSize(R.dimen.dimen_8),
                             0,
                             ctx.resources.getDimensionPixelSize(R.dimen.dimen_8))
                    }else{
                        loadAds(ctx)
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    setLog("MyApp", "LoadAdErrormessage:${p0.message} -->${p0}")
                    nativeTemplateView.hide()
                    val messageModel = MessageModel(p0.message+":-", MessageType.NEGATIVE, true)
                    //CommonUtils.showToast(ctx, messageModel)
                }
            }).withNativeAdOptions(adOptions).build()
            loadAds(ctx)
//        }
    }
    private fun loadAds(ctx: Context) {
        val adRequest: AdRequest = AdRequest.Builder().build()

        //setLog("isEMSSSS", adRequest.isTestDevice(ctx).toString())
        // load Native Ad with the Request
        adLoader?.loadAd(adRequest)
    }
}