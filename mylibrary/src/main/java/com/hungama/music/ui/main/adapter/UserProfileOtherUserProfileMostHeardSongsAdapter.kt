package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PlaylistModel

class UserProfileOtherUserProfileMostHeardSongsAdapter(
    context: Context,
    list: List<PlaylistModel.Data.Body.Row?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<PlaylistModel.Data.Body.Row?>? = list

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPlaylistName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        var tvPlaylistCount: TextView = itemView.findViewById(R.id.tvPlaylistCount)
        val ivPlaylistImage: ImageView = itemView.findViewById(R.id.ivPlaylistImage)
        val llMain: ConstraintLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {

            tvPlaylistName.text = "Playlist " + (position + 1)
            /*val list = list!![position]!!.data

            if (list!!.title != null) {
                tvPlaylistName.text = list.title
                tvPlaylistName.visibility = View.VISIBLE
            } else {
                tvPlaylistName.visibility = View.GONE
            }

            if (list.releasedate != null) {
                tvPlaylistCount.text = list.subtitle
                tvPlaylistCount.visibility = View.VISIBLE
            } else {
                tvPlaylistCount.visibility = View.GONE
            }

            if (list.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivPlaylistImage,
                    list.image!!,
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
                LayoutInflater.from(ctx).inflate(R.layout.row_item_other_user_most_listened_songs, parent, false)
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