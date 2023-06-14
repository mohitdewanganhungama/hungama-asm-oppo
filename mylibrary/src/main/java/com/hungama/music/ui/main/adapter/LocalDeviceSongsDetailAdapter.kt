package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.utils.ImageLoader

class LocalDeviceSongsDetailAdapter(
    context: Context,
    list: ArrayList<Track>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list:  ArrayList<Track>? = list

    private inner class AudioViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            if (list == null || list?.size!! <= position){
                return
            }
            val list = list?.get(position)

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list.subTitle != null) {
                tvSubTitle.text = list.subTitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }

            if (list.image != null) {
                // Load thumbnail of a specific media item.
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val thumbnail = ctx.contentResolver.loadThumbnail(
                            Uri.parse(list.image), Size(50, 50), null)
                        ImageLoader.loadImage(
                            ctx,
                            ivUserImage,
                            thumbnail,
                            R.drawable.bg_gradient_placeholder
                        )
                    } else {
                        ImageLoader.loadImage(
                            ctx,
                            ivUserImage,
                            list.image.toString(),
                            R.drawable.bg_gradient_placeholder
                        )
                    }

                }catch (e:Exception){

                }
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AudioViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_local_device_songs_layout, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AudioViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}