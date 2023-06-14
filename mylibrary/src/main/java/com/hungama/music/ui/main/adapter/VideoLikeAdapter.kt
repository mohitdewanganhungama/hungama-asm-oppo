package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hungama.music.data.model.*
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.R

class VideoLikeAdapter(
    val context: Context,
    var arrayList: List<PlaylistModel.Data.Body.Row>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2
    private var adLoader: AdLoader? = null
    // simple boolean to check the status of ad
    private var adLoaded = false
    private var isVideoAd = false

    fun addData(list: List<PlaylistModel.Data.Body.Row>) {
        arrayList = list
        notifyDataSetChanged()
    }

    private inner class IType1000ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvLanguageView: TextView = itemView.findViewById(R.id.tvLanguageView)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (!arrayList.isNullOrEmpty()){
                val list = arrayList.get(position)
                if (!TextUtils.isEmpty(list.data.image)) {
                    ImageLoader.loadImage(
                        context,
                        ivUserImage,
                        list.data.image,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                if (!TextUtils.isEmpty(list.data.title)) {
                    tvTitle.text = list.data.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }

                if (!TextUtils.isEmpty(list.data.subtitle)) {
                    tvSubTitle.text =
                        list.data.subtitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                if (!list.data.misc.lang.isNullOrEmpty()) {
                        val language = list.data.misc.lang?.joinToString  (separator = "|") { it.toString() }
                    /*holder.binding.tvLanguageView.text =
                        language + " • " + CommonUtils.ratingWithSuffix(arrayList?.get(holder.adapterPosition)?.data?.misc?.playcount.toString()) + "+ Views"*/
                    tvLanguageView.text =
                        language + " • " + list?.data?.misc?.f_playcount + " " + context.getString(R.string.discover_str_25)
                    tvLanguageView.visibility = View.VISIBLE
                } else {
                    tvLanguageView.visibility = View.GONE
                }

                if (!TextUtils.isEmpty(list.data.duration) && list.data.duration.toInt() > 0) {
                    tvTime.text = DateUtils.formatElapsedTime(list.data.duration.toLong())
                    tvTime.visibility = View.VISIBLE
                } else {
                    tvTime.visibility = View.GONE
                }

                llMain.setOnClickListener {
                    if (onitemclick != null) {
                        onitemclick.onUserClick(position)
                    }
                }
            }
        }
    }

    private inner class IType1012ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        val nativeTemplateView: TemplateView = itemView.findViewById(R.id.nativeTemplateView)
        fun bind(position: Int) {
            if (!arrayList.isNullOrEmpty()){
                val list = arrayList.get(position)
                if (CommonUtils.isDisplayAds()){
                    initNativeAds(clMain, nativeTemplateView, context, list)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == Constant.musicVideoNativeAds){
            return IType1012ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.row_music_video_detail_ads, parent, false)
            )
        }else{
            return IType1000ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.row_itype_5_2, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!arrayList.isNullOrEmpty()){
            if (arrayList.get(position).itype == Constant.musicVideoNativeAds){
                holder?.adapterPosition?.let { (holder as IType1012ViewHolder).bind(it) }
            }else{
                holder?.adapterPosition?.let { (holder as IType1000ViewHolder).bind(it) }
            }
        }else{
            holder?.adapterPosition?.let { (holder as IType1000ViewHolder).bind(it) }
        }
    }//onBind

    override fun getItemViewType(position: Int): Int {
        return arrayList.get(position).itype
    }

    interface OnItemClick {
        fun onUserClick(position: Int)
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
                    clMain?.show()
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