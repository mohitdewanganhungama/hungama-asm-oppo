package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowNewsPodcastBinding
import com.hungama.music.data.model.NewsPodcastModel
import com.hungama.music.utils.ImageLoader

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class NewsPodcastAdapter(
    val context: Context,
    var arrayList: ArrayList<NewsPodcastModel>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<NewsPodcastAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: ArrayList<NewsPodcastModel>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        if (viewType == ROW_TYPE_1) {

            val binding: RowNewsPodcastBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_news_podcast,
                parent,
                false
            )
            return ItemViewHolder(binding)
        } else {
            val binding: RowNewsPodcastBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_news_podcast,
                parent,
                false
            )
            return ItemViewHolder(binding)
        }

    }


    class ItemViewHolder(val binding: RowNewsPodcastBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))

        holder.binding.tvTitle1.text = arrayList.get(holder.adapterPosition).title1
        holder.binding.tvTitle2.text = arrayList.get(holder.adapterPosition).title2
        holder.binding.tvSubTitle1.text = arrayList.get(holder.adapterPosition).subtitle1
        holder.binding.tvSubTitle2.text = arrayList.get(holder.adapterPosition).subtitle2
        ImageLoader.loadImageWithFullScreen(
            context,
            holder.binding.ivUserImage,
            arrayList.get(holder.adapterPosition).image1!!,
            R.drawable.bg_gradient_placeholder
        )

        ImageLoader.loadImageWithFullScreen(
            context,
            holder.binding.ivNews2,
            arrayList.get(holder.adapterPosition).image2!!,
            R.drawable.bg_gradient_placeholder
        )


//        holderWinzobaazi.binding.cvMain.animation =
//            AnimationUtils.loadAnimation(
//                context,
//                R.anim.item_animation_translate
//            )
    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}