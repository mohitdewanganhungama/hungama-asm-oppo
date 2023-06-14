package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.convertByteToHumanReadableFormat
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils
import com.hungama.music.utils.fontmanger.FontDrawable

class MusicVideoDownloadAdapter(var context:Context, val onItemClick: OnItemClick?):RecyclerView.Adapter<MusicVideoDownloadAdapter.ViewHolder>() {

    var musicVideoList = ArrayList<DownloadedAudio>()

    internal fun setMusicVideoList(musicList: ArrayList<DownloadedAudio>){
        musicVideoList = musicList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var Title = itemView.findViewById(R.id.tvTitle)as TextView
        var subTitle = itemView.findViewById(R.id.tvSubTitle)as TextView
        var downloadSize = itemView.findViewById(R.id.tvMusicVideoDownloadSize)as TextView
        var image = itemView.findViewById(R.id.ivMusicVideoDownload)as ImageView
        var cvMain = itemView.findViewById(R.id.cvMain)as ConstraintLayout
        var ivinformation = itemView.findViewById(R.id.ivMoreInfo) as ImageView
        var download = itemView.findViewById(R.id.ivMovieDownloading) as ImageView
        var tvDownloadState = itemView.findViewById(R.id.tvDownload) as TextView
        var rlDownloadeStates = itemView.findViewById(R.id.rlDownloadeStates) as RelativeLayout
        var rlMoreInfo = itemView.findViewById(R.id.rlMoreInfo) as RelativeLayout
        var clSelection = itemView.findViewById(R.id.clSelection) as ConstraintLayout
        var ivSelection = itemView.findViewById(R.id.ivSelection) as ImageView
        var tvTime = itemView.findViewById(R.id.tvTime) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_music_video_download,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val musicdata = musicVideoList?.get(holder.adapterPosition)
        if (!CommonUtils.isUserHasGoldSubscription()){
            holder.cvMain.alpha = 0.4F
        }else{
            holder.cvMain.alpha = 1F
        }
        holder.Title.text = musicdata?.title

        holder.subTitle.text =musicdata?.artist
        holder.tvTime.text =DateUtils.formatElapsedTime(musicdata?.duration?.toLong()!!)
        setLog("MusicVideoDownloadAdapter", "musicdata:${musicdata}")
        if (!TextUtils.isEmpty(musicdata?.thumbnailPath)){
            ImageLoader.loadImage(context,holder.image,musicdata?.thumbnailPath!!,R.drawable.bg_gradient_placeholder)
        }else if (!TextUtils.isEmpty(musicdata?.image)){
            ImageLoader.loadImage(context,holder.image,musicdata?.image!!,R.drawable.bg_gradient_placeholder)
            setLog("MusicVideoDownloadAdapter", "2-thumbnailPath-${musicdata?.thumbnailPath}-image-${musicdata?.image}")
        }

        if (musicdata?.isSelected == 1){
            holder.clSelection?.visibility = View.VISIBLE
            holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
        }else if (musicdata?.isSelected == 2){
            holder.clSelection?.visibility = View.VISIBLE
            holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
        }else{
            holder.clSelection?.visibility = View.GONE
        }

        holder.cvMain.setOnClickListener {
            if (onItemClick != null) {
                onItemClick.onMusicItemVideoClick(position)
            }
        }

        holder.download.setOnClickListener {
            if (onItemClick != null){
                onItemClick.pauseOrResumeDownload(musicdata,position)
            }
        }

//        holder.rlMoreInfo.setOnClickListener{
//            setLog("TAG", "onBindViewHolder: rlmoreInfoClick")
//            if (onItemClick != null) {
//                onItemClick.onMusicVideoMenuClick(position)
//            }
//        }
        holder?.ivinformation?.setOnClickListener {
            setLog("TAG", "onBindViewHolder:three dot click")
            if(onItemClick!=null){
                onItemClick?.onMusicVideoMenuClick(holder.adapterPosition)
            }
        }

        holder.clSelection?.setOnClickListener {
            holder.clSelection?.visibility = View.VISIBLE
            if (musicdata?.isSelected == 1){
                musicdata?.isSelected = 2
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else if (musicdata?.isSelected == 2){
                musicdata?.isSelected = 1
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }
            if (onItemClick != null) {
                onItemClick.onItemSelection(musicdata, position, musicdata.isSelected)
            }
        }

        val ivMoreDrawable = FontDrawable(context, R.string.icon_option)
        ivMoreDrawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.ivinformation.setImageDrawable(ivMoreDrawable)

        val drawable = FontDrawable(context, R.string.icon_download)
        drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.download.setImageDrawable(drawable)

        val downloadedAudio = AppDatabase?.getInstance()?.downloadedAudio()?.findByContentId(musicdata?.contentId!!)
        val downloadQueue = AppDatabase?.getInstance()?.downloadQueue()?.findByContentId(musicdata?.contentId!!)

        if (downloadQueue != null){
            CommonUtils.setLog(
                "MusicVideoDownloadFragment",
                "MusicVideoDownloadAdapter-downloadQueue.downloadStatus-${downloadQueue.downloadStatus}"
            )
            holder.rlDownloadeStates.visibility = View.VISIBLE
            holder.downloadSize.text = ""+convertByteToHumanReadableFormat(musicdata.downloadedBytes)+"/"+convertByteToHumanReadableFormat(musicdata.totalDownloadBytes)
           // holder.downloadSize.text =  musicdata.language+ " • "+musicdata.f_playcount+" "+context.getString(R.string.discover_str_25)
            downloadIconStates(downloadQueue, holder.download, holder.tvDownloadState)
        }else{
            CommonUtils.setLog(
                "MusicVideoDownloadFragment",
                "MusicVideoDownloadAdapter-downloadQueue-null"
            )
        }

        if (downloadedAudio != null){
            CommonUtils.setLog(
                "MusicVideoDownloadFragment",
                "MusicVideoDownloadAdapter-downloadedAudio.downloadStatus-${downloadedAudio?.downloadStatus}"
            )
            if (downloadedAudio?.downloadStatus == Status.COMPLETED.value){
                try {
                    holder.rlDownloadeStates.visibility = View.GONE
                    holder.tvDownloadState.text = context.getString(R.string.video_player_str_2)
                    holder.downloadSize.text = ""+convertByteToHumanReadableFormat(musicdata.totalDownloadBytes)
                    val drawable1 = FontDrawable(context, R.string.icon_downloaded2)
                    drawable1.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                    holder.download.setImageDrawable(drawable1)
                }catch (e:Exception){

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicVideoList.size
    }

    interface OnItemClick {
        fun onMusicItemVideoClick(childPosition: Int)
        fun pauseOrResumeDownload(datalist: DownloadedAudio, position: Int)
        fun onMusicVideoMenuClick(childPosition: Int)
        fun onItemSelection(data: DownloadedAudio, childPosition: Int, isSelected: Int)
    }

    private fun downloadIconStates(
        downloadQueue: DownloadQueue,
        ivAudioDownload: ImageView,
        tvDownloadState: TextView
    ){
        when (downloadQueue.downloadStatus){
            Status.NONE.value -> {
                tvDownloadState.text = "-"
                val drawable = FontDrawable(context, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.QUEUED.value -> {
                tvDownloadState.text = context.getString(R.string.download_str_15)
                val drawable = FontDrawable(context, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.DOWNLOADING.value ->{
                tvDownloadState.text = context.getString(R.string.download_str_2) + " " + downloadQueue.percentDownloaded + "%"
                val drawable = FontDrawable(context, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.COMPLETED.value ->{
                tvDownloadState.text = context.getString(R.string.video_player_str_2)
                val drawable = FontDrawable(context, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.PAUSED.value ->{
                tvDownloadState.text = context.getString(R.string.download_str_16)
                ivAudioDownload.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_pause_round))
            }
            Status.FAILED.value ->{
                tvDownloadState.text = context.getString(R.string.download_str_17)
                ivAudioDownload.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_download_error))
            }
        }
    }
}