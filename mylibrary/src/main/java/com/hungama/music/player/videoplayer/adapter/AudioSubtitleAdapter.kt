package com.hungama.music.player.videoplayer.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PlayableContentModel

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class AudioSubtitleAdapter(
    context: Context,
    val list: List<PlayableContentModel.Data.Head.HeadData.Misc.Sl.SubtitleItem?>?,
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
            val list2 = list!![position]

            if (list2!!.lang != null) {
                tvTitle.text = list2.lang
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }
            if (list2.isSelected){
                ivCheck.visibility = View.VISIBLE
            }else{
                ivCheck.visibility = View.INVISIBLE
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    for (i in 0 until list.size) {
                        list.get(i)!!.isSelected = false
                    }
                    list2.isSelected = true
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