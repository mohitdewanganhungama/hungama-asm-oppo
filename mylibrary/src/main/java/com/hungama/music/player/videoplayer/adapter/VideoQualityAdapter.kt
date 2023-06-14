package com.hungama.music.player.videoplayer.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.VideoQuality

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class VideoQualityAdapter(
    context: Context,
    val list: ArrayList<VideoQuality>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context

    private inner class SubtitleViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvEng)
        val ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)
        val llMain: RelativeLayout = itemView.findViewById(R.id.rlSubtitle)
        fun bind(position: Int) {
            val model = list.get(position)



            if (model?.title != null && !TextUtils.isEmpty(model?.title)) {
                tvTitle.text = model?.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }
            if (model.isSelected) {
                ivCheck.visibility = View.VISIBLE
            } else {
                ivCheck.visibility = View.INVISIBLE
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    for (i in 0 until list.size) {
                        list.get(i)!!.isSelected = false
                    }
                    model.isSelected = true
                    onChildItemClick.onUserClick(position)
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SubtitleViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.subtitle_item_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SubtitleViewHolder).bind(position)
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}