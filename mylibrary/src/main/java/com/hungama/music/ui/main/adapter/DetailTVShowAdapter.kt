package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.utils.customview.ShowMoreTextView
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.fontmanger.FontDrawable

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class DetailTVShowAdapter(
    context: Context,
    val list: List<PlaylistModel.Data.Body.Row.Season.SeasonData.Misc.Track?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context

    private inner class IType1000ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: ShowMoreTextView = itemView.findViewById(R.id.tvSubTitle2)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val pbSong: ProgressBar = itemView.findViewById(R.id.pbSong)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivDownload:ImageView = itemView.findViewById(R.id.ivDownload)
        fun bind(position: Int) {
            if (!list.isNullOrEmpty() && list.size > position){
                val list = list[position]?.data

                if (!TextUtils.isEmpty(list?.name)) {
                    tvTitle.text = list?.name
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.text=""
                }

                if (list?.subTitle != null&&!TextUtils.isEmpty(list.subTitle)) {
                    tvSubTitle.text = list.subTitle
                    setLog("TAG", "TV SUB TIYTLE = " +list.subTitle )
                    tvSubTitle.visibility = View.VISIBLE
                } else {
                    tvSubTitle.text=""
                    tvSubTitle.visibility = View.VISIBLE
                }
                if (list?.misc!= null) {
                    SaveState.isCollapse = true
                    tvSubTitle2.text = list.misc.description
                    tvSubTitle2.setShowingLine(2)
                    tvSubTitle2.addShowMoreText("read more")
                    tvSubTitle2.addShowLessText("read less")
                    tvSubTitle2.setShowMoreColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                    tvSubTitle2.setShowLessTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                    tvSubTitle2.setShowMoreStyle(Typeface.BOLD)
                    tvSubTitle2.setShowLessStyle(Typeface.BOLD)
                    //CommonUtils.makeTextViewResizable(tvSubTitle2, 2, "read more", true)
                    tvSubTitle2.visibility = View.VISIBLE
                } else {
                    tvSubTitle2.visibility = View.GONE
                }
                /*if (list.time != null) {
                    tvTime.text = list.subTitle
                    tvTime.visibility = View.VISIBLE
                } else {
                    tvTime.visibility = View.GONE
                }*/
                /*if (HungamaMusicApp?.getInstance()?.getContentDuration(list?.id!!)?.toInt()!! > 0){
                    pbSong.max=list?.misc?.duration!!
                    pbSong.progress = HungamaMusicApp?.getInstance()?.getContentDuration(list?.id!!)?.toInt()!!
                    pbSong.visibility = View.VISIBLE
                }else{
                    pbSong.visibility = View.GONE
                }*/

                if (HungamaMusicApp.getInstance().getContentDuration(list?.id!!)?.toInt()!! > 0){
                    if(!TextUtils.isEmpty(list.misc.duration) && list.misc.duration.toInt()>0){
                        pbSong.max=list.misc.duration.toInt()
                    }

                    pbSong.progress = HungamaMusicApp.getInstance().getContentDuration(list.id)?.toInt()!!
                    val leftTimeInSecond = list.misc.duration.toInt() - HungamaMusicApp.getInstance().getContentDuration(list.id)?.toInt()!!
                    val leftTimeInMinuts = ((leftTimeInSecond % 86400 ) % 3600 ) / 60
                    tvTime.text = leftTimeInMinuts.toString() + ctx.getString(R.string.podcast_str_14)+" "+ ctx.getString(R.string.podcast_str_15)
                    tvTime.visibility=View.VISIBLE
                    pbSong.visibility=View.VISIBLE
                }else{
                    tvTime.visibility=View.GONE
                    pbSong.visibility=View.GONE
                }

                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )

                val ivMoreDrawable = FontDrawable(ctx, R.string.icon_option)
                ivMoreDrawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivMore.setImageDrawable(ivMoreDrawable)

                val drawable = FontDrawable(ctx, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ctx.resources?.getDimensionPixelSize(R.dimen.font_18)?.let { CommonUtils.downloadIconStates(ctx, Status.NONE.value, ivDownload, it.toFloat()) }

                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(list.id)
                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(list.id)
                if (downloadQueue != null){
                    ctx.resources?.getDimensionPixelSize(R.dimen.font_18)?.let { CommonUtils.downloadIconStates(ctx, downloadQueue.downloadStatus, ivDownload, it.toFloat()) }
                }

                if (downloadedAudio != null){
                    if (downloadedAudio.downloadStatus == Status.COMPLETED.value){
                        try {
                            ctx.resources?.getDimensionPixelSize(R.dimen.font_18)?.let { CommonUtils.downloadIconStates(ctx, downloadedAudio.downloadStatus, ivDownload, it.toFloat()) }
                        }catch (e:Exception){

                        }
                    }

                }

                llMain.setOnClickListener {
                    onChildItemClick?.onUserClick(position, false, false)
                }

                ivMore.setOnClickListener {
                    onChildItemClick?.onUserClick(position, true, false)
                }

                ivDownload.setOnClickListener {
                    onChildItemClick?.onUserClick(position, false, true)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_tv_show_detail, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType1000ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list?.get(position)?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isMenuClick:Boolean, isDownloadClick:Boolean)
    }
}