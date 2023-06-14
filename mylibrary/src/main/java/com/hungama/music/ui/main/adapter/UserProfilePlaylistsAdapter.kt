package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PlaylistRespModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader

class UserProfilePlaylistsAdapter(
    context: Context,
    val list: ArrayList<PlaylistRespModel.Data>,
    val onChildItemClick: OnChildItemClick?,
    val seeMore:Boolean = true
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPlaylistName: TextView = itemView.findViewById(R.id.tvTitle)
        var tvPlaylistCount: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivPlaylistImage: ImageView = itemView.findViewById(R.id.ivLibraryMusic)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val modelItem = list?.get(position)

            setLog("Adapter", "data:-->" + modelItem)
            if (!TextUtils.isEmpty(modelItem?.data?.title)) {
                tvPlaylistName.text = modelItem?.data?.title
                tvPlaylistName.visibility = View.VISIBLE
            } else {
                tvPlaylistName.visibility = View.GONE
            }
            if (modelItem?.data?.id?.equals("5001",true)!!) {
                tvPlaylistCount.visibility = View.GONE

                ivPlaylistImage?.setBackgroundResource(R.drawable.bottom_dialog_delete_btn_background)
                ivPlaylistImage?.setImageResource(R.drawable.ic_add_white)
            } else {
                var subtitle = ""
//                if (modelItem.public!!) {
//                    subtitle = ctx.getString(R.string.profile_str_28)
//                } else {
//                    subtitle = ctx.getString(R.string.profile_str_29)
//                }

//                if (!TextUtils.isEmpty(modelItem.data?.misc?.song_count!!)) {
//                    subtitle += ""+modelItem.data?.misc?.song_count+ " " + ctx.getString(R.string.songs_general)
//                }else if (!TextUtils.isEmpty(modelItem.data?.songCount!!)) {
//                    subtitle += ""+modelItem.data?.songCount+ " " + ctx.getString(R.string.songs_general)
//                } else {
//                    subtitle += " • 0 " + ctx.getString(R.string.songs_general)
//                }
//
//                if (!TextUtils.isEmpty(modelItem.data?.cp_subtitle!!)) {
//                    subtitle += " • " + ""+modelItem.data?.cp_subtitle
//                }

                if (!TextUtils.isEmpty(modelItem?.data?.subtitle)) {
                    tvPlaylistCount.text = modelItem?.data?.subtitle
                    tvPlaylistCount.visibility = View.VISIBLE
                } else {
                    tvPlaylistCount.visibility = View.GONE
                }

                ImageLoader.loadImage(
                    ctx,
                    ivPlaylistImage,
                    modelItem.data?.image?.toString()!!,
                    R.drawable.bg_gradient_placeholder
                )
            }


            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserProfileFollowerViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_all_library, parent, false)
        )
    }

    override fun getItemCount(): Int {

        return list?.size!!

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