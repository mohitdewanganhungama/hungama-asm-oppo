package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PlaylistModel

class UserProfileOtherUserProfileMostListenedArtistsAdapter(
    context: Context,
    list: List<PlaylistModel.Data.Body.Row?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<PlaylistModel.Data.Body.Row?>? = list

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivUshape: ImageView = itemView.findViewById(R.id.ivUshape)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootParent)
        fun bind(position: Int) {

            /*val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {
                tvSubTitle.text = CommonUtils.ratingWithSuffix(list.subTitle)
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
            if (list.image != null) {
                CommonUtils.setArtImageDarkBg(true, list.image, ivUshape)
                CommonUtils.setArtImageBg(true, list.image, rootLayout)
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image,
                    R.drawable.bg_gradient_placeholder
                )

            }

            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return UserProfileFollowerViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_16, parent, false)
            )
    }

    override fun getItemCount(): Int {
        //return list!!.size
        return 10
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserProfileFollowerViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}