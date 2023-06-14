package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowUpcomingLivePerformanceBinding
import com.hungama.music.data.model.MorningRadioModel
import com.hungama.music.utils.ImageLoader

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class UpcomingPerformanceAdapter(
    val context: Context,
    var arrayList: ArrayList<MorningRadioModel>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<UpcomingPerformanceAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: ArrayList<MorningRadioModel>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        if (viewType == ROW_TYPE_1) {

            val binding: RowUpcomingLivePerformanceBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_upcoming_live_performance,
                parent,
                false
            )
            return ItemViewHolder(binding)
        } else {
            val binding: RowUpcomingLivePerformanceBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_upcoming_live_performance,
                parent,
                false
            )
            return ItemViewHolder(binding)
        }

    }


    class ItemViewHolder(val binding: RowUpcomingLivePerformanceBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
        holder.binding.tvTitle1.text = arrayList.get(holder.adapterPosition).title
        holder.binding.tvSubTitle1.text = arrayList.get(holder.adapterPosition).subTitle
        ImageLoader.loadImageFitCenter(
            context,
            holder.binding.ivUserImage,
            arrayList.get(holder.adapterPosition).image1!!,
            R.drawable.bg_gradient_placeholder
        )

    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}