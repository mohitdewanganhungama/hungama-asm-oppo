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
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader

class RadioAdapter(
    context: Context,
    val list: ArrayList<BookmarkDataModel.Data.Body.Row>,
    val onChildItemClick: OnChildItemClick?
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
            val data = list[position]

            setLog("Adapter", "data:-->$data")
            if (!TextUtils.isEmpty(data.data.title)) {
                tvPlaylistName.text = data.data.title
                tvPlaylistName.visibility = View.VISIBLE
            } else {
                tvPlaylistName.visibility = View.GONE
            }

            tvPlaylistCount.visibility = View.GONE

            ImageLoader.loadImageRound(
                ctx,
                ivPlaylistImage,
                data.data.image!!,
                R.drawable.bg_gradient_placeholder
            )


            llMain.setOnClickListener {
                onChildItemClick?.onUserClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserProfileFollowerViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_all_library, parent, false)
        )
    }

    override fun getItemCount(): Int {

        return list.size

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