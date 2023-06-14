package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Typeface
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
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.customview.ShowMoreTextView
import com.hungama.music.utils.fontmanger.FontDrawable
import com.hungama.music.utils.hide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DownloadedEpsiodesAdapter(var context: Context, val onItemClick: OnItemClick?):RecyclerView.Adapter<DownloadedEpsiodesAdapter.ViewHolder>() {

    var epsiodesList = emptyList<DownloadedAudio>()
    internal fun setEpsiodes(epsiodesListdata :List<DownloadedAudio>){
        CoroutineScope(Dispatchers.Main).launch {
            epsiodesList = epsiodesListdata
            notifyDataSetChanged()
        }

    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var epsiodestitle = itemView.findViewById(R.id.tvTitle)as TextView
        var epsiodesdesc = itemView.findViewById(R.id.tvSubTitle)as TextView
        var epsiodeImage = itemView.findViewById(R.id.ivUserImage)as ImageView
        var llMain = itemView.findViewById(R.id.llMain)as LinearLayoutCompat
        var download = itemView.findViewById(R.id.ivDownload) as ImageView
        var ivinformation = itemView.findViewById(R.id.rlMoreInfo) as RelativeLayout
        var ivMore = itemView.findViewById(R.id.ivMore) as ImageView
        var clSelection = itemView.findViewById(R.id.clSelection) as ConstraintLayout
        var ivSelection = itemView.findViewById(R.id.ivSelection) as ImageView
        var tvSubTitle2 = itemView.findViewById(R.id.tvSubTitle2) as ShowMoreTextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_tvshow_downloaded_epsiodes,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var datalist = epsiodesList?.get(holder.adapterPosition)

        if (!CommonUtils.isUserHasGoldSubscription()){
            holder.llMain.alpha = 0.4F
        }else{
            holder.llMain.alpha = 1F
        }
        holder.epsiodestitle.text = datalist?.title

        var subTitle = datalist?.subTitle
        if (!TextUtils.isEmpty(datalist?.duration.toString()) && datalist?.duration!! > 0){
            if (!TextUtils.isEmpty(subTitle)){
                subTitle += " • " + TimeUnit.SECONDS.toMinutes(datalist?.duration!!).toString() + " " + context.getString(R.string.podcast_str_14)
            }else{
                subTitle = TimeUnit.SECONDS.toMinutes(datalist.duration!!).toString() + " " + context.getString(R.string.podcast_str_14)
            }
        }

        if (!TextUtils.isEmpty(subTitle)){
            holder.epsiodesdesc.text = subTitle
        }else{
            holder.epsiodesdesc.hide()
        }

        holder.tvSubTitle2.text = datalist?.description
        holder.tvSubTitle2.setShowingLine(2)
        holder.tvSubTitle2.addShowMoreText("read more")
        holder.tvSubTitle2.addShowLessText("read less")
        holder.tvSubTitle2.setShowMoreColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.tvSubTitle2.setShowLessTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.tvSubTitle2.setShowMoreStyle(Typeface.BOLD)
        holder.tvSubTitle2.setShowLessStyle(Typeface.BOLD)
        setLog("TAG", "DISCRIPTION = " +datalist?.subTitle )
        if (!TextUtils.isEmpty(datalist?.thumbnailPath)){
            ImageLoader.loadImage(context,holder.epsiodeImage,datalist?.thumbnailPath!!,R.drawable.bg_gradient_placeholder)
        }else if (!TextUtils.isEmpty(datalist?.image)){
            ImageLoader.loadImage(context,holder.epsiodeImage,datalist?.image!!,R.drawable.bg_gradient_placeholder)
        }

        if (datalist?.isSelected == 1){
            holder.clSelection?.visibility = View.VISIBLE
            holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
        }else if (datalist?.isSelected == 2){
            holder.clSelection?.visibility = View.VISIBLE
            holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
        }else{
            holder.clSelection?.visibility = View.GONE
        }

        holder?.llMain?.setOnClickListener {
            if (onItemClick != null) {
                onItemClick.onEpisodeClick(position)
            }
        }

        holder?.ivinformation?.setOnClickListener {
            if(onItemClick!=null){
                onItemClick?.showMovieDeleteDialog(holder.adapterPosition)
            }
        }

        holder.download.setOnClickListener {
            if (onItemClick != null){
                datalist?.let { it1 -> onItemClick.pauseOrResumeDownload(it1,position) }
            }
        }

        holder.clSelection?.setOnClickListener {
            holder.clSelection?.visibility = View.VISIBLE
            if (datalist?.isSelected == 1){
                datalist?.isSelected = 2
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else if (datalist?.isSelected == 2){
                datalist?.isSelected = 1
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }
            if (onItemClick != null) {
                datalist?.let { it1 -> onItemClick.onItemSelection(it1, position, datalist.isSelected) }
            }
        }

        val ivMoreDrawable = FontDrawable(context, R.string.icon_option)
        ivMoreDrawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder?.ivMore?.setImageDrawable(ivMoreDrawable)

        val drawable = FontDrawable(context, R.string.icon_download)
        drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.download.setImageDrawable(drawable)

        val downloadedAudio = AppDatabase?.getInstance()?.downloadedAudio()?.findByContentId(datalist?.contentId!!)
        val downloadQueue = AppDatabase?.getInstance()?.downloadQueue()?.findByContentId(datalist?.contentId!!)
        if (downloadQueue != null){
            downloadIconStates(downloadQueue.downloadStatus, holder.download)
        }

        if (downloadedAudio != null){
            if (downloadedAudio?.downloadStatus == Status.COMPLETED.value){
                try {
                    downloadIconStates(downloadedAudio.downloadStatus, holder.download)
                }catch (e:Exception){

                }
            }
        }
    }
    override fun getItemCount(): Int {
        return epsiodesList.size
    }

    interface OnItemClick {
        fun showMovieDeleteDialog(adapterPosition: Int)
        fun onEpisodeClick(childPosition: Int)
        fun pauseOrResumeDownload(datalist: DownloadedAudio, position: Int)
        fun onItemSelection(data: DownloadedAudio, childPosition: Int, isSelected: Int)
    }

    private fun downloadIconStates(status: Int, ivAudioDownload: ImageView){
        when (status){
            Status.NONE.value -> {
                val drawable = FontDrawable(context, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.QUEUED.value -> {
                val drawable = FontDrawable(context, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.DOWNLOADING.value ->{
                val drawable = FontDrawable(context, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.COMPLETED.value ->{
                val drawable = FontDrawable(context, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
            }
            Status.PAUSED.value ->{
                ivAudioDownload.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_pause_round))
            }
        }
    }
}