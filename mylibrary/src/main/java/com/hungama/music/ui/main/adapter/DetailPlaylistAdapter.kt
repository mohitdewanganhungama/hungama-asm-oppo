package com.hungama.music.ui.main.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
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
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.playlistNativeAds
import com.hungama.music.utils.fontmanger.FontDrawable
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList

class DetailPlaylistAdapter(
    val ctx: Context,
    var list: ArrayList<PlaylistModel.Data.Body.Row>,
    val onChildItemClick: OnChildItemClick?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var adLoader: AdLoader? = null

    // simple boolean to check the status of ad
    private var adLoaded = false
    private var isVideoAd = false
    private var isEdit = false

    fun refreshData(playlistSongList: ArrayList<PlaylistModel.Data.Body.Row>, isEdit: Boolean) {
        setLog("TAG", "refreshData: before list size:${list.size}")
        list = playlistSongList
        this.isEdit = isEdit
        notifyDataSetChanged()
        setLog("TAG", "refreshData: after list size:${list.size}")
    }

    inner class IType1000ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivDownload: ImageView = itemView.findViewById(R.id.ivDownload)
        val ivExplicit: ImageView = itemView.findViewById(R.id.ivE)
        val rlFreeStrip: RelativeLayout = itemView.findViewById(R.id.rlFreeStrip)
        val ivEditPlaylist: ImageView = itemView.findViewById(R.id.ivEditPlaylist)
        val llClose:LinearLayoutCompat = itemView.findViewById(R.id.cancelPlaylist)
        val vNowPlaying:View = itemView.findViewById(R.id.vNowPlaying)
        val ivEqualizer:ImageView = itemView.findViewById(R.id.ivEqualizer)
        val ivEqualizerAnim: LottieAnimationView = itemView.findViewById(R.id.ivEqualizerAnim)
        fun bind(holder: RecyclerView.ViewHolder) {
            val position = holder.adapterPosition
            if (isEdit == true) {
                setLog("TAG", "bind:isEdit " + isEdit)
                ivDownload.visibility = View.GONE
                ivMore.visibility = View.INVISIBLE
                ivEditPlaylist.visibility = View.VISIBLE
                llClose.visibility = View.VISIBLE
                ivEditPlaylist.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onChildItemClick?.onStartDrag(holder)
                    }
                    return@setOnTouchListener true
                }
                llClose.setOnClickListener {
                    onChildItemClick?.onContentRemoveClick(position, list.get(position).data.id)
                }
            } else {
                ivDownload.visibility = View.VISIBLE
                ivMore.visibility = View.VISIBLE
                ivEditPlaylist.visibility = View.GONE
                llClose.visibility = View.GONE
                ivEditPlaylist.setOnTouchListener(null)
                llClose.setOnTouchListener(null)
            }

            if(!CommonUtils.isUserHasGoldSubscription() && list?.get(position)?.data?.misc?.movierights?.contains("AMOD") == true && CommonUtils.getSongDurationConfig().enable_minutes_quota)
                rlFreeStrip.show() else  rlFreeStrip.hide()

            if (!list.isNullOrEmpty()) {
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

                val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
                ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlack))
                ivMore.setImageDrawable(ivMoreDrawable)

                ivDownload.setBackgroundResource(R.drawable.ic_chart_download)

                val downloadedAudio =
                    AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(list.id!!)
                val downloadQueue =
                    AppDatabase.getInstance()?.downloadQueue()?.findByContentId(list.id!!)
                if (downloadQueue != null) {
                    //if (!TextUtils.isEmpty(downloadQueue?.parentId!!)){
                    downloadIconStates(downloadQueue.downloadStatus, ivDownload)
                    //}

                }

                if (downloadedAudio != null) {
                    if (downloadedAudio.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(
                            downloadedAudio.downloadedFilePath
                        )
                    ) {
                        try {
                            val fileName = downloadedAudio.downloadedFilePath
                            val file = File(fileName)
                            if (file.exists()) {
                                downloadIconStates(downloadedAudio.downloadStatus, ivDownload)
                            } else {
                                AppDatabase.getInstance()?.downloadedAudio()
                                    ?.deleteDownloadQueueItemByContentId(
                                        downloadedAudio.contentId!!
                                    )
                                downloadIconStates(0, ivDownload)
                            }


                        } catch (e: Exception) {

                        } catch (fileNotFound: FileNotFoundException) {
                            downloadIconStates(0, ivDownload)
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
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)) {
                        onChildItemClick?.onUserClick(
                            position,
                            isMenuClick = false,
                            isDownloadClick = false
                        )
                        BaseActivity.setTouchData()
                    }
                }

                ivMore.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)) {
                        onChildItemClick?.onUserClick(
                            position,
                            isMenuClick = true,
                            isDownloadClick = false
                        )
                    }
                }

                ivDownload.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.misc.explicit)) {
                        onChildItemClick?.onUserClick(
                            position,
                            isMenuClick = false,
                            isDownloadClick = true
                        )

                        if(CommonUtils.isUserHasGoldSubscription()){
                            val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlack))
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
            if (!list.isNullOrEmpty()) {
                val list = list[position]
                //setLog("MyApp", "CommonUtils.isDisplayAds()-2"+CommonUtils.isDisplayAds())
                if (CommonUtils.isDisplayAds()) {
                    initNativeAds(clMain, nativeTemplateView, ctx, list)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == playlistNativeAds) {
            IType1011ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_playlist_detail_ads, parent, false)
            )
        } else {
            IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_playlist_detail_v1, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        //setLog("TAG", "DetailPlaylistAdapter bind size: ${list.size}")
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!list.isNullOrEmpty()) {
            setLog("DetailPlaylistAdapter", "itype-${list[position].itype}")
            if (list[position].itype == playlistNativeAds) {
                (holder as IType1011ViewHolder).bind(holder.adapterPosition)
            } else {
                (holder as IType1000ViewHolder).bind(holder)
            }
        } else {
            (holder as IType1000ViewHolder).bind(holder)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return try {
            list[position].itype
        }catch (e:Exception){
            99999
        }

    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isMenuClick: Boolean, isDownloadClick: Boolean)
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder){}
        fun onContentRemoveClick(childPosition: Int, contentId:String){}
    }

    private fun downloadIconStates(status: Int, ivDownload: ImageView) {
        when (status) {
            Status.NONE.value -> {
//                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlack))
                ivDownload.setBackgroundResource(R.drawable.ic_chart_download)
            }
            Status.QUEUED.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlack))
                ivDownload.setImageDrawable(drawable)
            }
            Status.DOWNLOADING.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlack))
                ivDownload.setImageDrawable(drawable)
            }
            Status.COMPLETED.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlack))
                ivDownload.setImageDrawable(drawable)
            }
            Status.FAILED.value -> {
                ivDownload.setImageDrawable(
                    ContextCompat.getDrawable(
                        ctx,
                        R.drawable.ic_cancel_song
                    )
                )
            }

            Status.PAUSED.value -> {
                ivDownload.setImageDrawable(
                    ContextCompat.getDrawable(
                        ctx,
                        R.drawable.ic_pause_round
                    )
                )
            }
        }
    }

    private fun initNativeAds(
        clMain: ConstraintLayout,
        nativeTemplateView: TemplateView,
        ctx: Context,
        list: PlaylistModel.Data.Body.Row
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

        adLoader = AdLoader.Builder(
            ctx,
            list.adUnitId
        ).forNativeAd(object : NativeAd.OnNativeAdLoadedListener {
            private val background: ColorDrawable? = null
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                val styles =
                    NativeTemplateStyle.Builder().withMainBackgroundColor(background).build()

                nativeTemplateView.setStyles(styles)
                nativeTemplateView.setNativeAd(nativeAd)
                adLoaded = true

                if (nativeAd.mediaContent != null) {
                    isVideoAd = nativeAd.mediaContent?.hasVideoContent()!!
                    if (isVideoAd) {

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
        }).withAdListener(object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                if (adLoaded) {
                    setLog("MyApp", "onAdLoaded")
                    clMain?.show()
                } else {
                    loadAds(ctx)
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                setLog("MyApp", "onAdFailedToLoad-"+p0.message)
                clMain.hide()
                val messageModel = MessageModel(p0.message+":-", MessageType.NEGATIVE, true)
                //CommonUtils.showToast(ctx, messageModel)
            }
        }).withNativeAdOptions(adOptions).build()
        loadAds(ctx)
    }

    private fun loadAds(ctx: Context) {
        val adRequest: AdRequest = AdRequest.Builder().build()

        //setLog("isEMSSSS", adRequest.isTestDevice(ctx).toString())
        // load Native Ad with the Request
        adLoader?.loadAd(adRequest)
    }

    /**
     * Function called to swap dragged items
     */
    fun swapItems(fromPosition: Int, toPosition: Int) {
        setLog("TAG", "fromPosition:"+fromPosition)
        setLog("TAG", "toPosition:"+toPosition)
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(list, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemSelected(viewHolder: IType1000ViewHolder) {
        viewHolder?.itemView?.translationZ = 10F
    }

    fun onItemClear(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is IType1000ViewHolder) {
            viewHolder?.itemView?.translationZ = 0F
        }
    }

}