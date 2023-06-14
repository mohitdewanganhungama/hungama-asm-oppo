package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.databinding.RowSelectTempoBinding
import com.hungama.music.data.model.MoodRadioFilterModel


/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class MoodFilterSelectTempoAdapter(
    val context: Context,
    var arrayList: List<MoodRadioFilterModel.Data.Body.Row.Item?>?,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<MoodFilterSelectTempoAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1


    fun addData(list: List<MoodRadioFilterModel.Data.Body.Row.Item?>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowSelectTempoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_select_tempo,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowSelectTempoBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList?.size!!
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
        val list = arrayList?.get(pos)

        if (!TextUtils.isEmpty(list?.title)){
            holder.binding.tvTitle.text = list?.title
            holder.binding.tvTitle.visibility = View.VISIBLE
        }else{
            holder.binding.tvTitle.visibility = View.GONE
        }
        if (list?.isSelected!! || SharedPrefHelper.getInstance().getMoodRadioTempoFilterId() == list.tempoid){
            holder.binding.ivTick.visibility = View.VISIBLE
        }else{
            holder.binding.ivTick.visibility = View.INVISIBLE
        }

        holder.binding.llMain.setOnClickListener {
            for (i in 0 until arrayList?.size!!) {
                arrayList?.get(i)?.isSelected = false
            }
            list?.isSelected = true
            if (onitemclick != null) {
                onitemclick.onUserClick(pos)
            }
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }

    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}