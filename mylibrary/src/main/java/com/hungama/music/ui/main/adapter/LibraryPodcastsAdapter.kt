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

class LibraryPodcastsAdapter(
    val ctx: Context,
    val list: ArrayList<BookmarkDataModel.Data.Body.Row>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPodcastName: TextView = itemView.findViewById(R.id.tvTitle)
        var tvPodcastCount: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivPodcastImage: ImageView = itemView.findViewById(R.id.ivLibraryMusic)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val data = list[position]

            setLog("Adapter", "data:-->$data")
            if (!TextUtils.isEmpty(data.data.title)) {
                tvPodcastName.text = data.data.title
                tvPodcastName.visibility = View.VISIBLE
            } else {
                tvPodcastName.visibility = View.GONE
            }
            var subtitle = ""



            /*if (!TextUtils.isEmpty(data.data.songCount!!)) {
                subtitle += data.data.songCount + " " + ctx.getString(R.string.podcast_str_9)
            }*/

            if (!TextUtils.isEmpty(data.data.subtitle!!)) {
                if (!TextUtils.isEmpty(subtitle)){
                    subtitle += " • " + data.data.subtitle.toString()
                }else{
                    subtitle += data.data.subtitle.toString()
                }

            }

            if (!TextUtils.isEmpty(subtitle)) {
                tvPodcastCount.text = subtitle
                tvPodcastCount.visibility = View.VISIBLE
            } else {
                tvPodcastCount.visibility = View.GONE
            }

            ImageLoader.loadImage(
                ctx,
                ivPodcastImage,
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
            LayoutInflater.from(ctx).inflate(R.layout.row_all_library_podcast, parent, false)
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