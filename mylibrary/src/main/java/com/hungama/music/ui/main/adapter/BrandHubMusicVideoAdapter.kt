package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowArtistMusicVideoBinding
import com.hungama.music.data.model.CollectionDetailModel

class BrandHubMusicVideoAdapter(
    val context: Context,

    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<BrandHubMusicVideoAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2

    var arrayList = emptyList<CollectionDetailModel>()
    fun addData(list: List<CollectionDetailModel>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowArtistMusicVideoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_artist_music_video,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowArtistMusicVideoBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
//        return arrayList?.size!!
        return 5
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))
//        if (arrayList?.get(holder.adapterPosition)?.data?.title != null) {
//            holder.binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.data?.title
//            holder.binding.tvTitle.visibility = View.VISIBLE
//        } else {
//            holder.binding.tvTitle.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.data?.subtitle != null) {
//            holder.binding.tvSubTitle.text =
//                arrayList?.get(holder.adapterPosition)?.data?.subtitle
//            holder.binding.tvSubTitle.visibility = View.VISIBLE
//        } else {
//            holder.binding.tvSubTitle.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.data?.image != null) {
//            ImageLoader.loadImage(
//                context,
//                holder.binding.ivUserImage,
//                arrayList?.get(holder.adapterPosition)?.data?.image!!,
//                R.drawable.bg_gradient_placeholder
//            )
//        }
//
//        holder.binding.llMain.setOnClickListener {
//            if (onitemclick != null) {
//                onitemclick.onUserClick(holder.adapterPosition)
//            }
//        }
    }//onBind

//    override fun getItemViewType(position: Int): Int {
//        return ROW_TYPE_1
//
//    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}