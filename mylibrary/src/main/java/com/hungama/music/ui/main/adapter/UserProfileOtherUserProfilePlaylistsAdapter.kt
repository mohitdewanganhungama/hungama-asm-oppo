package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PlaylistRespModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader

class UserProfileOtherUserProfilePlaylistsAdapter(
    context: Context,
    list: List<PlaylistRespModel.Data>,
    val onChildItemClick: OnChildItemClick?,
    val seeMore:Boolean = true
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<PlaylistRespModel.Data> = list

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPlaylistName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        var tvPlaylistCount: TextView = itemView.findViewById(R.id.tvPlaylistCount)
        val ivPlaylistImage: ImageView = itemView.findViewById(R.id.ivPlaylistImage)
        val llMain: ConstraintLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {

            //tvPlaylistName.text = "Playlist " + (position + 1)
            val list = list!![position]

            if ( !TextUtils.isEmpty(list!!.data?.title)) {
                tvPlaylistName.text = list.data?.title
                tvPlaylistName.visibility = View.VISIBLE
            } else {
                tvPlaylistName.visibility = View.GONE
            }
            var subtitle = ""
            if (list.public!!) {
                subtitle = ctx.getString(R.string.profile_str_28)
            } else {
                subtitle = ctx.getString(R.string.profile_str_29)
            }

            if (!TextUtils.isEmpty(""+list.data?.misc?.favCount!!)){
                subtitle += " • "+list.data?.misc?.favCount.toString() + " " + ctx.getString(R.string.profile_str_30)

                setLog("TAG", "bind: subtitle+list.data.misc.favCount.toString() "+subtitle+list.data.misc.favCount.toString())
            }else{
                subtitle += " • 0 " + ctx.getString(R.string.profile_str_30)
            }

            if (!TextUtils.isEmpty(subtitle)){
                tvPlaylistCount.text = subtitle
                tvPlaylistCount.visibility = View.VISIBLE
            }else{
                tvPlaylistCount.visibility = View.GONE
            }

            if (list.data?.image != null && !TextUtils.isEmpty(list.data?.image) ) {
                ImageLoader.loadImage(
                    ctx,
                    ivPlaylistImage,
                    list.data.image,
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
                LayoutInflater.from(ctx).inflate(R.layout.row_item_other_user_playlists, parent, false)
            )
    }

    override fun getItemCount(): Int {

        if (seeMore){
            return list?.size!!
        }else{
            return 5
        }

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