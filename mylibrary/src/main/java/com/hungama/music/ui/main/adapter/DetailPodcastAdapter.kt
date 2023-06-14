package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.utils.customview.ShowMoreTextView
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.data.model.PodcastDetailsRespModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.fontmanger.FontDrawable
import java.io.File
import java.io.FileNotFoundException

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class DetailPodcastAdapter(
    val context: Context,
    val list: List<PlaylistModel.Data.Body.Row.Data.Misc.Track>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adLoader: AdLoader? = null
    // simple boolean to check the status of ad
    private var adLoaded = false
    private var isVideoAd = false
    private val ctx: Context = context


    private inner class IType1000ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: ShowMoreTextView = itemView.findViewById(R.id.tvSubTitle2)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val pbSong: ProgressBar = itemView.findViewById(R.id.pbSong)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivDownload:ImageView = itemView.findViewById(R.id.ivDownload)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivPlay: ImageView = itemView.findViewById(R.id.ivPlay)
        fun bind(position: Int) {
            val dataItemModel = list!![position]!!.data
            setLog("TAG", "bind: content id : ${dataItemModel?.id} -> play track id: ${(ctx as BaseActivity).fetchTrackData().id} title:${(ctx as BaseActivity).fetchTrackData().title}")

            if (!BaseActivity?.songDataList.isNullOrEmpty() && BaseActivity?.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()){
                val currentPlayingContentId =
                    BaseActivity?.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                if (currentPlayingContentId?.toString()?.equals(dataItemModel?.id)!!) {
                    if ((context as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                        ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_pause_circle_filled))
                    } else if ((context as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                        ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                    } else {
                        ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                    }

                    ivPlay?.setOnClickListener {
                        if (onChildItemClick != null){
                            onChildItemClick?.onPlayPauseClick(position)
                        }
                        if (!BaseActivity?.songDataList.isNullOrEmpty() && BaseActivity?.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()){
                            val currentPlayingContentId =
                                BaseActivity?.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                            if (currentPlayingContentId?.toString()?.equals(dataItemModel?.id)!!) {
                                if ((context as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                    ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_pause_circle_filled))
                                } else if ((context as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                    ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                                } else {
                                    ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                                }
                            } else {
                                ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                            }
                        }else{
                            ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                        }

                    }
                } else {
                    ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
                }
            }else{
                ivPlay?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_play_circle_filled))
            }


            if (dataItemModel?.title != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    tvTitle.setText(Html.fromHtml(dataItemModel?.title!!, Html.FROM_HTML_MODE_LEGACY));
//                } else {
//                    tvTitle.setText(Html.fromHtml(dataItemModel?.title!!));
//                }
                tvTitle.setText(Html.fromHtml(dataItemModel?.title!!));
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.text = ""
            }
            var formattedDate: String = ""
            if (dataItemModel?.releasedate != null) {
                formattedDate = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_FORMAT_MMMM_DD_YYYY,dataItemModel.releasedate)
            } else {
                formattedDate = ""
            }


            if (dataItemModel?.duration != null && !TextUtils.isEmpty(dataItemModel?.duration.toString())) {
                val totalSecs = dataItemModel?.duration?.toLong()
                if (totalSecs?.toLong()!! >= 0) {
                    val formatTime = android.text.format.DateUtils.formatElapsedTime(totalSecs!!)
                    formattedDate += " • " + formatTime
                }
            }

            tvSubTitle.text = formattedDate
            tvSubTitle.visibility = View.VISIBLE

            if (dataItemModel?.misc?.description != null) {
                SaveState.isCollapse = true
                tvSubTitle2.text = dataItemModel.misc?.description
                tvSubTitle2.setShowingLine(2)
                tvSubTitle2.addShowMoreText("read more")
                tvSubTitle2.addShowLessText("read less")
                tvSubTitle2.setShowMoreColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                tvSubTitle2.setShowLessTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                tvSubTitle2.setShowMoreStyle(Typeface.BOLD)
                tvSubTitle2.setShowLessStyle(Typeface.BOLD)
                tvSubTitle2.visibility = View.VISIBLE
            } else {
                tvSubTitle2.text = ""
                tvSubTitle2.visibility = View.GONE
            }
            /*if (list.time != null) {
                tvTime.text = list.subTitle
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }*/
            if (dataItemModel?.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    dataItemModel?.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }

            val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
            ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivMore.setImageDrawable(ivMoreDrawable)

            val drawable = FontDrawable(ctx, R.string.icon_download)
            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivDownload.setImageDrawable(drawable)

            val downloadedAudio = AppDatabase?.getInstance()?.downloadedAudio()?.findByContentId(dataItemModel?.id!!)
            val downloadQueue = AppDatabase?.getInstance()?.downloadQueue()?.findByContentId(dataItemModel?.id!!)
            if (downloadQueue != null){
                if (!TextUtils.isEmpty(downloadQueue?.parentId!!)){
                    downloadIconStates(downloadQueue.downloadStatus, ivDownload)
                }
            }

            if (downloadedAudio != null){
                if (downloadedAudio?.parentId != null && !TextUtils.isEmpty(downloadedAudio?.parentId!!)){
                    if (downloadedAudio?.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(downloadedAudio.downloadedFilePath)){
                        try {
                            val fileName = downloadedAudio.downloadedFilePath
                            val file = File(fileName)
                            if (file.exists()){
                                downloadIconStates(downloadedAudio.downloadStatus, ivDownload)
                            }else{
                                AppDatabase?.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(
                                    downloadedAudio.contentId?.toString()!!
                                )
                                downloadIconStates(0, ivDownload)
                            }
                        }catch (e:Exception){

                        }catch (fileNotFound: FileNotFoundException){
                            downloadIconStates(0, ivDownload)
                        }


                    }
                }
            }


            if (HungamaMusicApp?.getInstance()?.getContentDuration(dataItemModel?.id!!)?.toInt()!! > 0){
                if(!TextUtils.isEmpty(dataItemModel?.duration) && dataItemModel?.duration!=null && dataItemModel.duration.toInt()>0){
                    pbSong.max=dataItemModel.duration.toInt()
                }

                pbSong.progress = HungamaMusicApp?.getInstance()?.getContentDuration(dataItemModel?.id!!)?.toInt()!!
                val leftTimeInSecond = dataItemModel.duration.toInt() - HungamaMusicApp?.getInstance()?.getContentDuration(dataItemModel?.id!!)?.toInt()!!
                val leftTimeInMinuts = ((leftTimeInSecond % 86400 ) % 3600 ) / 60
                tvTime.text = leftTimeInMinuts.toString() + ctx.getString(R.string.podcast_str_14)+" "+ ctx.getString(R.string.podcast_str_15)
                tvTime.visibility=View.VISIBLE
                pbSong.visibility=View.VISIBLE
            }else{
                tvTime.visibility=View.GONE
                pbSong.visibility=View.GONE
            }



            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, false, false)
                }
            }

            ivMore.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, true, false)
                }
            }

            ivDownload.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, false, true)
                }
            }
        }
    }

    private inner class IType1012ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        val nativeTemplateView: TemplateView = itemView.findViewById(R.id.nativeTemplateView)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty()){
                val list = list.get(position)
                if (CommonUtils.isDisplayAds()){
                    initNativeAds(clMain, nativeTemplateView, context, list)
                    nativeTemplateView.visibility = View.GONE
                    val layoutParams = clMain.layoutParams
                    val params = layoutParams as ViewGroup.MarginLayoutParams
                    params.setMargins(0
                        , ctx.resources.getDimensionPixelSize(R.dimen.dimen_16),
                        ctx.resources.getDimensionPixelSize(R.dimen.dimen_16),
                    0)
                    clMain.layoutParams = params
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == Constant.podcastNativeAds){
            return IType1012ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.row_podcast_detail_ads, parent, false)
            )
        }else{
            return IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_chart_detail_v3, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (!list.isNullOrEmpty()){
            if (list.get(position).itype == Constant.podcastNativeAds){
                (holder as IType1012ViewHolder).bind(holder.adapterPosition)
            }else{
                (holder as IType1000ViewHolder).bind(position)
            }
        }else{
            (holder as IType1000ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list.get(position).itype
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isMenuClick:Boolean, isDownloadClick:Boolean)
        fun onPlayPauseClick(position: Int)
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
        list: PlaylistModel.Data.Body.Row.Data.Misc.Track
    ) {
        //val testDeviceIds = Arrays.asList("E3E4B1D1BAF3339A44199F739A633150")
        //val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        //MobileAds.setRequestConfiguration(configuration)
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        //Initializing the AdLoader   objects

        adLoader = AdLoader.Builder(ctx,
            list.adUnitId
        ).forNativeAd(object : NativeAd.OnNativeAdLoadedListener{
            private val background: ColorDrawable? = null
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                val styles =
                    NativeTemplateStyle.Builder().withMainBackgroundColor(background).build()

                nativeTemplateView.setStyles(styles)
                nativeTemplateView.setNativeAd(nativeAd)
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
                    nativeTemplateView.visibility = View.VISIBLE
                }else{
                    loadAds(ctx)
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                clMain.hide()
                setLog("MyApp", "onAdFailedToLoad")
                val messageModel = MessageModel(p0.message+":-", MessageType.NEGATIVE, true)
                //CommonUtils.showToast(ctx, messageModel)
            }
        }).withNativeAdOptions(adOptions).build()
        loadAds(ctx)
    }
    private fun loadAds(ctx: Context) {
        val adRequest: AdRequest = AdRequest.Builder().build()

        setLog("isEMSSSS", adRequest.isTestDevice(ctx).toString())
        // load Native Ad with the Request
        adLoader?.loadAd(adRequest)
    }
}