package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.fontmanger.FontDrawable

class DownloadQueueAdapter(
    context: Context,
    list: java.util.ArrayList<DownloadQueue>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list:  ArrayList<DownloadQueue>? = list

    private inner class AudioViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivDownload:ImageView = itemView.findViewById(R.id.ivDownload)
        val ivExplicit:ImageView = itemView.findViewById(R.id.ivE)
        fun bind(position: Int) {
            val list = list!![position]!!

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }


            if (list.subTitle != null) {
                tvSubTitle.text = list.subTitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
            CommonUtils.setExplicitContent(ctx, llMain, list.explicit, ivExplicit)

            downloadIconStates(list.downloadStatus, ivDownload)

            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (!CommonUtils.checkExplicitContent(ctx, list.explicit)){
                    onChildItemClick?.onUserClick(position, false, false)
                }
            }

            ivDownload.setOnClickListener {
                if (!CommonUtils.checkExplicitContent(ctx, list.explicit)){
                    if (onChildItemClick != null) {
                        //onChildItemClick.onUserClick(position, false, true)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AudioViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_download_in_progress, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AudioViewHolder).bind(position)
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
            Status.FAILED.value ->{
                ivDownload.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_cancel_song))
            }

            Status.PAUSED.value ->{
                ivDownload.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_pause_round))
            }
        }
    }
}