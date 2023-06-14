package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.utils.customview.ShowMoreTextView
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.RestrictedDownload
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.fontmanger.FontDrawable
import com.hungama.music.utils.preference.SharedPrefHelper
import java.io.File
import java.io.FileNotFoundException

class DownloadedContentDetailAdapter(
    context: Context,
    list: ArrayList<DownloadedAudio>?,
    val downloadContentType: Int,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list:  ArrayList<DownloadedAudio>? = list
    val freeSongLimit = CommonUtils.getMaxDownloadContentSize(context)
    var currentPlayingIndex = -1

    private inner class AudioViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivDownload:ImageView = itemView.findViewById(R.id.ivDownload)
        val ivExplicit:ImageView = itemView.findViewById(R.id.ivE)
        val vNowPlaying:View = itemView.findViewById(R.id.vNowPlaying)
        val ivEqualizer:ImageView = itemView.findViewById(R.id.ivEqualizer)
        val ivEqualizerAnim: LottieAnimationView = itemView.findViewById(R.id.ivEqualizerAnim)
        val rlGoldStrip: RelativeLayout = itemView.findViewById(R.id.rlGoldStrip)
        val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty()){
                val list = list?.get(position)
                if (!BaseActivity.getIsGoldUser() && (position >= freeSongLimit || list?.restrictedDownload == RestrictedDownload.RESTRICT_DOWNLOAD_CONTENT.value)){
                    llMain.alpha = 0.4F
                }else{
                    llMain.alpha = 1F
                }
                if (list?.title != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }


                if (list?.subTitle != null) {
                    tvSubTitle.text = list.subTitle
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.visibility = View.GONE
                }

                CommonUtils.setExplicitContent(ctx, llMain, list?.explicit!!, ivExplicit)


                ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivMore.setImageDrawable(ivMoreDrawable)
                downloadIconStates(list.downloadStatus, ivDownload)

                if(!CommonUtils.isUserHasGoldSubscription() && Constant.DEFAULT_COUNTRY_CODE.equals("IN", true))
                    rlGoldStrip.show()


         /*       if(!CommonUtils.isUserHasGoldSubscription() && list.gold_flag==1 && Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
                    rlGoldStrip.hide()
                }else rlGoldStrip.hide()*/
                /*if (list.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(list.downloadedFilePath)){
                    try {
                        val fileName = list.downloadedFilePath
                        val file = File(fileName)
                        if (file.exists()){
                            downloadIconStates(list.downloadStatus, ivDownload)
                        }else{
                            AppDatabase.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(
                                list.contentId?.toString()!!
                            )
                            downloadIconStates(0, ivDownload)
                        }
                    }catch (e:Exception){

                    }catch (fileNotFound:FileNotFoundException){
                        downloadIconStates(0, ivDownload)
                    }


                }*/

                if (list.thumbnailPath != null) {
                    ImageLoader.loadImage(
                        ctx,
                        ivUserImage,
                        list.thumbnailPath.toString(),
                        R.drawable.bg_gradient_placeholder
                    )
                }

                if (list.isSelected == 1){
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
                    if(CommonUtils.bottomMusicrestricMenu(ctx)){
                    if (!CommonUtils.checkExplicitContent(ctx, list.explicit!!)) {
                        onChildItemClick?.onUserClick(position, false, false)
                      }
                    }
                }

                ivMore.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.explicit!!)){
                        onChildItemClick?.onUserClick(position, true, false)
                    }
                }

                ivDownload.setOnClickListener {
                    if (!CommonUtils.checkExplicitContent(ctx, list.explicit!!)){
                        if (onChildItemClick != null) {
                            //onChildItemClick.onUserClick(position, false, true)
                        }
                    }
                }
            }
        }
    }

    private inner class PodcastViewHolder(itemView: View) :
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
//        val rlGoldStrip: RelativeLayout = itemView.findViewById(R.id.rlGoldStrip)

        val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
        fun bind(position: Int) {
            val list = list!![position]!!

            if (list?.title != null) {
                tvTitle.setText(Html.fromHtml(list?.title!!));
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.text = ""
            }
            var formattedDate: String = ""
            if (list?.releaseDate != null) {
                formattedDate = DateUtils.convertDate(
                    DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                    DateUtils.DATE_FORMAT_MMMM_DD_YYYY,list.releaseDate)
            } else {
                formattedDate = ""
            }


            if (list?.duration != null && !TextUtils.isEmpty(list?.duration.toString())) {
                val totalSecs = list?.duration?.toLong()
                if (totalSecs?.toLong()!! >= 0) {
                    val formatTime = android.text.format.DateUtils.formatElapsedTime(totalSecs!!)
                    formattedDate += " • " + formatTime
                }
            }

            tvSubTitle.text = formattedDate
            tvSubTitle.visibility = View.VISIBLE

            if (list?.description != null) {
                SaveState.isCollapse = true
                tvSubTitle2.text = list.description
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
                tvSubTitle2.visibility = View.VISIBLE
            }
            /*if (list.time != null) {
                tvTime.text = list.subTitle
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }*/

            ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivMore.setImageDrawable(ivMoreDrawable)
            downloadIconStates(list.downloadStatus, ivDownload)


            /*if (list?.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(list.downloadedFilePath)){
                try {
                    val fileName = list.downloadedFilePath
                    val file = File(fileName)
                    if (file.exists()){
                        downloadIconStates(list?.downloadStatus, ivDownload)
                    }else{
                        AppDatabase?.getInstance()?.downloadedAudio()?.deleteDownloadQueueItemByContentId(
                            list.contentId?.toString()!!
                        )
                        downloadIconStates(0, ivDownload)
                    }
                }catch (e:Exception){

                }catch (fileNotFound:FileNotFoundException){
                    downloadIconStates(0, ivDownload)
                }


            }*/
            if (list?.thumbnailPath != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list?.thumbnailPath!!,
                    R.drawable.bg_gradient_placeholder
                )
            }


            if (HungamaMusicApp?.getInstance()?.getContentDuration(list?.contentId!!)?.toInt()!! > 0){
                if(list?.duration!=null&&list?.duration!!>0){
                    pbSong.max= list?.duration?.toInt()!!
                }

                pbSong.progress = HungamaMusicApp?.getInstance()?.getContentDuration(list?.contentId!!)?.toInt()!!
                val leftTimeInSecond = list?.duration!! - HungamaMusicApp?.getInstance()?.getContentDuration(list?.contentId!!)?.toInt()!!
                val leftTimeInMinuts = ((leftTimeInSecond % 86400 ) % 3600 ) / 60
                tvTime.text = leftTimeInMinuts.toString() + "Min"+" "+ "left"
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
                    //onChildItemClick.onUserClick(position, false, true)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (downloadContentType == ContentTypes.AUDIO.value){
            return AudioViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_download_detail, parent, false)
            )
        }else{
            return PodcastViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_chart_detail_v3, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (downloadContentType == ContentTypes.AUDIO.value){
            (holder as AudioViewHolder).bind(position)
        }else{
            (holder as PodcastViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list!![position]?.itype!!
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
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

    fun updateCurrentPlayingIndex(currentPlayingIndex:Int){
        this.currentPlayingIndex = currentPlayingIndex
        notifyDataSetChanged()
        setLog("downloadedContent", "DCA-updateCurrentPlayingIndex-currentPlayingIndex-$currentPlayingIndex")
    }
}