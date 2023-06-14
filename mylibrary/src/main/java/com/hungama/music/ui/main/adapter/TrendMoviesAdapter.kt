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
import com.hungama.music.data.model.HomeItem


/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class TrendMoviesAdapter(
    val context: Context,
    var arrayList: List<HomeItem?>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<TrendMoviesAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: List<HomeItem?>) {
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
//        if (arrayList?.get(holder.adapterPosition)?.title != null && arrayList?.get(holder.adapterPosition)?.showTitle!!) {
//            holder?.binding?.tvTitle1.text = arrayList?.get(holder.adapterPosition)?.title
//            holder?.binding?.tvTitle1.visibility = View.VISIBLE
//        } else {
//            holder?.binding?.tvTitle1.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.subTitle != null && arrayList?.get(holder.adapterPosition)?.showSubTitle!!) {
//            holder?.binding?.tvSubTitle1.text = arrayList?.get(holder.adapterPosition)?.subTitle
//            holder?.binding?.tvSubTitle1.visibility = View.VISIBLE
//        } else {
//            holder?.binding?.tvSubTitle1.visibility = View.GONE
//        }
//        ImageLoader.loadImageRound(
//            context,
//            holder.binding.ivUserImage,
//            arrayList.get(holder.adapterPosition)?.image!!,
//            R.drawable.bg_gradient_placeholder
//        )

    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}