package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.RecommendedSongListRespModel
import com.hungama.music.utils.ImageLoader
import java.util.ArrayList

class FollowPodcastAdapter(
    context: Context,
    list: List<RecommendedSongListRespModel.Data.Body.Similar>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<RecommendedSongListRespModel.Data.Body.Similar?>? = list

    private inner class IType12ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivAddSong: ImageView = itemView.findViewById(R.id.ivAddSong)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (!TextUtils.isEmpty(list!!.title)) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(list!!.subtitle)) {
                tvSubTitle.text = list.subtitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }


            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onPlaySongClick(position)
                }
            }

            ivAddSong.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onAddSongClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IType12ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_add_podcast, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType12ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list!![position]?.itype!!
    }

    fun refreshList(filterList: ArrayList<RecommendedSongListRespModel.Data.Body.Similar>) {
        list=filterList
        notifyDataSetChanged()
    }

    interface OnChildItemClick {
        fun onPlaySongClick(childPosition: Int)
        fun onAddSongClick(childPosition: Int)
    }
}